/*
    This file is part of Kismet

    Kismet is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    Kismet is distributed in the hope that it will be useful,
      but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Kismet; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

#include "config.h"

#include <stdio.h>
#include <time.h>
#include <vector>
#include <string>
#include <sstream>
#include <iomanip>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <microhttpd.h>
#include <msgpack.hpp>

#include <memory>

#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>

#include "globalregistry.h"
#include "messagebus.h"
#include "configfile.h"
#include "kis_net_microhttpd.h"
#include "base64.h"
#include "entrytracker.h"
#include "kis_httpd_websession.h"

Kis_Net_Httpd::Kis_Net_Httpd(GlobalRegistry *in_globalreg) {
    globalreg = in_globalreg;

    running = false;

    use_ssl = false;
    cert_pem = NULL;
    cert_key = NULL;

    if (globalreg->kismet_config == NULL) {
        fprintf(stderr, "FATAL OOPS: Kis_Net_Httpd called without kismet_config\n");
        exit(1);
    }

    http_port = globalreg->kismet_config->FetchOptUInt("httpd_port", 2501);

    string http_data_dir, http_aux_data_dir;

    http_data_dir = globalreg->kismet_config->FetchOpt("httpd_home");
    http_aux_data_dir = globalreg->kismet_config->FetchOpt("httpd_user_home");

    if (http_data_dir == "") {
        _MSG("No httpd_home defined in kismet.conf, disabling static file serving. "
                "This will disable the web UI, but the REST interface will still "
                "function.", MSGFLAG_ERROR);
        http_serve_files = false;
    } else {
        http_data_dir = 
            globalreg->kismet_config->ExpandLogPath(http_data_dir, "", "", 0, 1);
        _MSG("Serving static content from '" + http_data_dir + "'",
                MSGFLAG_INFO);
        http_serve_files = true;

        // Add it as a possible file dir
        RegisterStaticDir("/", http_data_dir);
    }

    if (http_aux_data_dir == "") {
        _MSG("No httpd_user_home defined in kismet.conf, disabling static file serving "
                "from user directory", MSGFLAG_ERROR);
        http_serve_user_files = false;
    } else {
        http_aux_data_dir = 
            globalreg->kismet_config->ExpandLogPath(http_aux_data_dir, "", "", 0, 1);
        _MSG("Serving static userdir content from '" + http_aux_data_dir + "'",
                MSGFLAG_INFO);
        http_serve_user_files = true;
        
        // Add it as a second possible source of '/' files
        RegisterStaticDir("/", http_aux_data_dir);
    }

    if (http_serve_files == false && http_serve_user_files == false) {
        RegisterHandler(new Kis_Net_Httpd_No_Files_Handler());
    }

    session_timeout = 
        globalreg->kismet_config->FetchOptUInt("httpd_session_timeout", 7200);

    use_ssl = globalreg->kismet_config->FetchOptBoolean("httpd_ssl", false);
    pem_path = globalreg->kismet_config->FetchOpt("httpd_ssl_cert");
    key_path = globalreg->kismet_config->FetchOpt("httpd_ssl_key");

    RegisterMimeType("html", "text/html");
    RegisterMimeType("svg", "image/svg+xml");
    RegisterMimeType("css", "text/css");
    RegisterMimeType("jpeg", "image/jpeg");
    RegisterMimeType("gif", "image/gif");
    RegisterMimeType("ico", "image/x-icon");
    RegisterMimeType("json", "application/json");
    RegisterMimeType("ekjson", "application/json");
    RegisterMimeType("pcap", "application/vnd.tcpdump.pcap");

    vector<string> mimeopts = globalreg->kismet_config->FetchOptVec("httpd_mime");
    for (unsigned int i = 0; i < mimeopts.size(); i++) {
        vector<string> mime_comps = StrTokenize(mimeopts[i], ":");

        if (mime_comps.size() != 2) {
            _MSG("Expected httpd_mime=extension:type", MSGFLAG_ERROR);
            continue;
        }

        _MSG("Adding user-defined MIME type " + mime_comps[1] + " for " + mime_comps[0],
                MSGFLAG_INFO);
        RegisterMimeType(mime_comps[0], mime_comps[1]);
        
    }

    // Do we store sessions?
    store_sessions = false;
    session_db = NULL;

    sessiondb_file = globalreg->kismet_config->FetchOpt("httpd_session_db");

    if (sessiondb_file != "") {
        sessiondb_file = 
            globalreg->kismet_config->ExpandLogPath(sessiondb_file, "", "", 0, 1);

        session_db = new ConfigFile(globalreg);

        store_sessions = true;

        struct stat buf;
        if (stat(sessiondb_file.c_str(), &buf) == 0) {
            session_db->ParseConfig(sessiondb_file.c_str());

            vector<string> oldsessions = session_db->FetchOptVec("session");

            if (oldsessions.size() > 0) 
                _MSG("Loading saved HTTP sessions", MSGFLAG_INFO);

            for (unsigned int s = 0; s < oldsessions.size(); s++) {
                vector<string> sestok = StrTokenize(oldsessions[s], ",");

                if (sestok.size() != 4)
                    continue;

                shared_ptr<Kis_Net_Httpd_Session> sess(new Kis_Net_Httpd_Session());

                sess->sessionid = sestok[0];

                if (sscanf(sestok[1].c_str(), "%lu", &(sess->session_created)) != 1) {
                    continue;
                }

                if (sscanf(sestok[2].c_str(), "%lu", &(sess->session_seen)) != 1) {
                    continue;
                }

                if (sscanf(sestok[3].c_str(), "%lu", &(sess->session_lifetime)) != 1) {
                    continue;
                }

                // Ignore old sessions
                if (sess->session_created + sess->session_lifetime < 
                        globalreg->timestamp.tv_sec) 
                    continue;

                // Don't use AddSession because we don't want to trigger a write, yet
                session_map.emplace(sess->sessionid, sess);
            }
        }
    }
}

Kis_Net_Httpd::~Kis_Net_Httpd() {
    local_eol_locker lock(&controller_mutex);

    globalreg->RemoveGlobal("HTTPD_SERVER");

    // Wipe out all handlers
    handler_vec.erase(handler_vec.begin(), handler_vec.end());

    if (running)
        StopHttpd();

    if (session_db) {
        delete(session_db);
    }

    session_map.clear();
}

void Kis_Net_Httpd::RegisterSessionHandler(shared_ptr<Kis_Httpd_Websession> in_session) {
    websession = in_session;
}

char *Kis_Net_Httpd::read_ssl_file(string in_fname) {
    FILE *f;
    stringstream str;
    char *buf = NULL;
    long sz;

    // Read errors are considered fatal
    if ((f = fopen(in_fname.c_str(), "rb")) == NULL) {
        str << "Unable to open SSL file " << in_fname <<
            ": " << kis_strerror_r(errno);
        _MSG(str.str(), MSGFLAG_FATAL);
        return NULL;
    }

    fseek(f, 0, SEEK_END);
    sz = ftell(f);
    rewind(f);

    if (sz <= 0) {
       str << "Unable to load SSL file " << in_fname << ": File is empty";
       _MSG(str.str(), MSGFLAG_FATAL);
       return NULL;
    }

    buf = new char[sz + 1];
    if (fread(buf, sz, 1, f) <= 0) {
        str << "Unable to read SSL file " << in_fname <<
            ": " << kis_strerror_r(errno);
        _MSG(str.str(), MSGFLAG_FATAL);
        return NULL;
    }
    fclose(f);

    // Null terminate the buffer
    buf[sz] = 0;

    return buf;
}

string Kis_Net_Httpd::GetSuffix(string url) {
    size_t lastdot = url.find_last_of(".");

    if (lastdot != string::npos)
        return url.substr(lastdot + 1, url.length() - lastdot);

    return "";
}

string Kis_Net_Httpd::StripSuffix(string url) {
    size_t lastdot = url.find_last_of(".");

    if (lastdot == std::string::npos)
        lastdot = url.length();

    return url.substr(0, lastdot);
}

void Kis_Net_Httpd::RegisterMimeType(string suffix, string mimetype) {
    local_locker lock(&controller_mutex);
    mime_type_map.emplace(StrLower(suffix), mimetype);
}

void Kis_Net_Httpd::RegisterStaticDir(string in_prefix, string in_path) {
    local_locker lock(&controller_mutex);

    static_dir_vec.push_back(static_dir(in_prefix, in_path));
}

void Kis_Net_Httpd::RegisterHandler(Kis_Net_Httpd_Handler *in_handler) {
    local_locker lock(&controller_mutex);

    handler_vec.push_back(in_handler);
}

void Kis_Net_Httpd::RemoveHandler(Kis_Net_Httpd_Handler *in_handler) {
    local_locker lock(&controller_mutex);

    for (unsigned int x = 0; x < handler_vec.size(); x++) {
        if (handler_vec[x] == in_handler) {
            handler_vec.erase(handler_vec.begin() + x);
            break;
        }
    }
}

int Kis_Net_Httpd::StartHttpd() {
    local_locker lock(&controller_mutex);

    if (use_ssl) {
        // If we can't load the SSL key files, crash and burn.  We can't safely
        // degrade to non-ssl when the user is expecting encryption.
        if (pem_path == "") {
            _MSG("SSL requested but missing httpd_ssl_cert= configuration option.",
                    MSGFLAG_FATAL);
            globalreg->fatal_condition = 1;
            return -1;
        }

        if (key_path == "") {
            _MSG("SSL requested but missing httpd_ssl_key= configuration option.",
                    MSGFLAG_FATAL);
            globalreg->fatal_condition = 1;
            return -1;
        }

        pem_path =
            globalreg->kismet_config->ExpandLogPath(pem_path, "", "", 0, 1);
        key_path =
            globalreg->kismet_config->ExpandLogPath(key_path, "", "", 0, 1);

        cert_pem = read_ssl_file(pem_path);
        cert_key = read_ssl_file(key_path);

        if (cert_pem == NULL || cert_key == NULL) {
            _MSG("SSL requested but unable to load cert and key files, check your "
                    "configuration!", MSGFLAG_FATAL);
            globalreg->fatal_condition = 1;
            return -1;
        }
    }


    if (!use_ssl) {
        microhttpd = MHD_start_daemon(MHD_USE_THREAD_PER_CONNECTION,
                http_port, NULL, NULL, 
                &http_request_handler, this, 
                MHD_OPTION_NOTIFY_COMPLETED, &http_request_completed, NULL,
                MHD_OPTION_END); 
    } else {
        microhttpd = MHD_start_daemon(MHD_USE_THREAD_PER_CONNECTION | MHD_USE_SSL,
                http_port, NULL, NULL, &http_request_handler, this, 
                MHD_OPTION_HTTPS_MEM_KEY, cert_key,
                MHD_OPTION_HTTPS_MEM_CERT, cert_pem,
                MHD_OPTION_END); 
    }


    if (microhttpd == NULL) {
        _MSG("Failed to start http server on port " + UIntToString(http_port),
                MSGFLAG_FATAL);
        globalreg->fatal_condition = 1;
        return -1;
    }

    MHD_set_panic_func(Kis_Net_Httpd::MHD_Panic, this);

    running = true;

    _MSG("Started http server on port " + UIntToString(http_port), MSGFLAG_INFO);

    return 1;
}

int Kis_Net_Httpd::StopHttpd() {
    local_locker lock(&controller_mutex);

    handler_vec.clear();
    static_dir_vec.clear();

    if (microhttpd != NULL) {
        // Formerly we quiesced the httpd daemon but that api seemed to have
        // problems on some builds; now we silence the panic handler and 
        // shut down the daemon
        
        running = false;
        MHD_stop_daemon(microhttpd);
        return 1;
    }

    return 0;
}

void Kis_Net_Httpd::MHD_Panic(void *cls, const char *file, unsigned int line,
        const char *reason) {
    Kis_Net_Httpd *httpd = (Kis_Net_Httpd *) cls;

    // Do nothing if we're already closing down
    if (!httpd->running)
        return;

    httpd->running = false;

    httpd->globalreg->fatal_condition = 1;
    httpd->globalreg->messagebus->InjectMessage("Unable to continue after "
            "MicroHTTPD fatal error: " + string(reason), MSGFLAG_FATAL);

    // Null out the microhttpd since it can't keep operating and can't be
    // trusted to close down properly
    httpd->microhttpd = NULL;
}

void Kis_Net_Httpd::AddSession(shared_ptr<Kis_Net_Httpd_Session> in_session) {
    local_locker lock(&controller_mutex);

    session_map.emplace(in_session->sessionid, in_session);
    WriteSessions();
}

void Kis_Net_Httpd::DelSession(string in_key) {
    local_locker lock(&controller_mutex);

    auto i = session_map.find(in_key);

    if (i != session_map.end())
        DelSession(i);

}

void Kis_Net_Httpd::DelSession(map<string, shared_ptr<Kis_Net_Httpd_Session> >::iterator in_itr) {
    local_locker lock(&controller_mutex);

    if (in_itr != session_map.end()) {
        session_map.erase(in_itr);
        WriteSessions();
    }
}

void Kis_Net_Httpd::WriteSessions() {
    local_locker lock(&controller_mutex);

    if (!store_sessions)
        return;

    vector<string> sessions;
    stringstream str;

    for (auto i : session_map) {
        str.str("");

        str << i.second->sessionid << "," << i.second->session_created << "," <<
            i.second->session_seen << "," << i.second->session_lifetime;

        sessions.push_back(str.str());
    }

    session_db->SetOptVec("session", sessions, true);

    // Ignore failures here I guess?
    session_db->SaveConfig(sessiondb_file.c_str());
}

int Kis_Net_Httpd::http_request_handler(void *cls, struct MHD_Connection *connection,
    const char *url, const char *method, const char *version __attribute__ ((unused)),
    const char *upload_data, size_t *upload_data_size, void **ptr) {

    //fprintf(stderr, "debug - HTTP request: '%s' method '%s'\n", url, method); 
    //
    Kis_Net_Httpd *kishttpd = (Kis_Net_Httpd *) cls;

    if (kishttpd->globalreg->spindown || kishttpd->globalreg->fatal_condition)
        return MHD_NO;
    
    // Update the session records if one exists
    shared_ptr<Kis_Net_Httpd_Session> s = NULL;
    const char *cookieval;
    int ret = MHD_NO;

    Kis_Net_Httpd_Connection *concls = NULL;

    cookieval = MHD_lookup_connection_value(connection, 
            MHD_COOKIE_KIND, KIS_SESSION_COOKIE);

    if (cookieval != NULL) {
        auto si = kishttpd->session_map.find(cookieval);

        if (si != kishttpd->session_map.end()) {
            s = si->second;

            if (s->session_lifetime != 0) {
                // Delete if the session has expired
                if (s->session_seen + s->session_lifetime < 
                        kishttpd->globalreg->timestamp.tv_sec) {
                    kishttpd->DelSession(si);
                }
            }

            // Update the last seen
            s->session_seen = kishttpd->globalreg->timestamp.tv_sec;
        }
    } 
    
    Kis_Net_Httpd_Handler *handler = NULL;

    
    {
        local_locker conclock(&(kishttpd->controller_mutex));
        /* Find a handler that can handle this path & method */
        for (unsigned int i = 0; i < kishttpd->handler_vec.size(); i++) {
            Kis_Net_Httpd_Handler *h = kishttpd->handler_vec[i];

            if (h->Httpd_VerifyPath(url, method)) {
                handler = h;
                break;
            }
        }
    }

    // If we don't have a connection state, make one
    if (*ptr == NULL) {
        concls = new Kis_Net_Httpd_Connection();
        // fprintf(stderr, "debug - allocated new connection state %p\n", concls);

        *ptr = (void *) concls;

        concls->httpd = kishttpd;
        concls->httpdhandler = handler;
        concls->session = s;
        concls->httpcode = MHD_HTTP_OK;
        concls->url = string(url);
        concls->connection = connection;

        /* Set up a POST handler */
        if (strcmp(method, "POST") == 0) {
            concls->connection_type = Kis_Net_Httpd_Connection::CONNECTION_POST;

            concls->postprocessor =
                MHD_create_post_processor(connection, KIS_HTTPD_POSTBUFFERSZ,
                        kishttpd->http_post_handler, (void *) concls);

            if (concls->postprocessor == NULL) {
                // fprintf(stderr, "debug - failed to make postprocessor\n");
                // This might get cleaned up elsewhere? The examples don't 
                // free it.
                // delete(concls);
                return MHD_NO;
            }
        } else {
            // Otherwise default to the get handler
            concls->connection_type = Kis_Net_Httpd_Connection::CONNECTION_GET;
        }

        // We're done
        return MHD_YES;
    } else {
        concls = (Kis_Net_Httpd_Connection *) *ptr;
    }

    if (handler == NULL) {
        // Try to check a static url
        if (handle_static_file(cls, concls, url, method) < 0) {
            // fprintf(stderr, "   404 no handler for request\n");

            string fourohfour = "404";

            struct MHD_Response *response = 
                MHD_create_response_from_buffer(fourohfour.length(), 
                        (void *) fourohfour.c_str(), MHD_RESPMEM_MUST_COPY);

            return MHD_queue_response(connection, MHD_HTTP_NOT_FOUND, response);
        }

        return MHD_YES;
    }

    if (strcmp(method, "POST") == 0) {
        // Handle post
        
        // If we still have data to process
        if (*upload_data_size != 0) {
            // Process regardless of size to get our completion
            MHD_post_process(concls->postprocessor, upload_data, *upload_data_size);

            // Continue processing post data
            *upload_data_size = 0;
            return MHD_YES;
        } 

        // Otherwise we've completed our post data processing, flag us
        // as completed so our post handler knows we're done
        
        // fprintf(stderr, "con %p post complete\n", concls);
        concls->post_complete = true;

        // Handle a post req inside the processor and return the results
        return (concls->httpdhandler)->Httpd_HandlePostRequest(kishttpd, concls, url,
                method, upload_data, upload_data_size);
    } else {
        // Handle GET + any others
        ret = handler->Httpd_HandleGetRequest(kishttpd, concls, url, method, 
                upload_data, upload_data_size);
    }

    return ret;
}

int Kis_Net_Httpd::http_post_handler(void *coninfo_cls, enum MHD_ValueKind kind, 
        const char *key, const char *filename, const char *content_type,
        const char *transfer_encoding, const char *data, 
        uint64_t off, size_t size) {

    Kis_Net_Httpd_Connection *concls = (Kis_Net_Httpd_Connection *) coninfo_cls;

    if (concls->httpdhandler->Httpd_UseCustomPostIterator()) {
        return (concls->httpdhandler)->Httpd_PostIterator(coninfo_cls, kind,
                key, filename, content_type, transfer_encoding, data, off, size);
    } else {
        // Cache all the variables by name until we're complete
        if (concls->variable_cache.find(key) == concls->variable_cache.end())
            concls->variable_cache[key] = 
                unique_ptr<std::stringstream>(new std::stringstream);

        concls->variable_cache[key]->write(data, size);

        return MHD_YES;
    }
}

void Kis_Net_Httpd::http_request_completed(void *cls __attribute__((unused)), 
        struct MHD_Connection *connection __attribute__((unused)),
        void **con_cls, 
        enum MHD_RequestTerminationCode toe __attribute__((unused))) {
    Kis_Net_Httpd_Connection *con_info = (Kis_Net_Httpd_Connection *) *con_cls;

    if (con_info == NULL)
        return;

    // Lock and shut it down
    {
        std::lock_guard<std::mutex> lk(con_info->connection_mutex);

        if (con_info->connection_type == Kis_Net_Httpd_Connection::CONNECTION_POST) {
            MHD_destroy_post_processor(con_info->postprocessor);
            con_info->postprocessor = NULL;
        }
    }

    // Destroy connection
    
    delete(con_info);
}

static ssize_t file_reader(void *cls, uint64_t pos, char *buf, size_t max) {
    FILE *file = (FILE *) cls;

    fseek(file, pos, SEEK_SET);
    return fread(buf, 1, max, file);
}


static void free_callback(void *cls) {
    FILE *file = (FILE *) cls;
    fclose(file);
}

string Kis_Net_Httpd::GetMimeType(string ext) {
    std::map<string, string>::iterator mi = mime_type_map.find(ext);
    if (mi != mime_type_map.end()) {
        return mi->second;
    }

    return "";
}

int Kis_Net_Httpd::handle_static_file(void *cls, Kis_Net_Httpd_Connection *connection,
        const char *url, const char *method) {
    Kis_Net_Httpd *kishttpd = (Kis_Net_Httpd *) cls;

    if (strcmp(method, "GET") != 0)
        return -1;

    string surl(url);

    // Append index.html to directory requests
    if (surl[surl.length() - 1] == '/')
        surl += "index.html";

    local_locker lock(&(kishttpd->controller_mutex));

    for (auto sd : kishttpd->static_dir_vec) {
        if (strlen(url) < sd.prefix.size())
            continue;

        if (surl.find(sd.prefix) != 0) 
            continue;

        string modified_fpath = sd.path + "/" + 
            surl.substr(sd.prefix.length(), surl.length());

        char *modified_realpath;
        char *base_realpath = realpath(sd.path.c_str(), NULL);

        modified_realpath = realpath(modified_fpath.c_str(), NULL);

        // Couldn't resolve real path
        if (modified_realpath == NULL || base_realpath == NULL) {
            if (modified_realpath != NULL)
                free(modified_realpath);

            if (base_realpath != NULL)
                free(base_realpath);

            continue;
        }

        // Make sure real path resolves inside the served path
        if (strstr(modified_realpath, base_realpath) != modified_realpath) {
            if (modified_realpath != NULL)
                free(modified_realpath);

            if (base_realpath != NULL)
                free(base_realpath);

            continue;
        }

        // The path is resolved, try to open the file
        FILE *f = fopen(modified_realpath, "rb");

        free(modified_realpath);
        free(base_realpath);

        if (f != NULL) {
            struct MHD_Response *response;
            struct stat buf;

            int fd;

            fd = fileno(f);
            if (fstat(fd, &buf) != 0 || (!S_ISREG(buf.st_mode))) {
                fclose(f);
                return -1;
            }

            response = MHD_create_response_from_callback(buf.st_size, 32 * 1024,
                    &file_reader, f, &free_callback);

            if (response == NULL) {
                fclose(f);
                return -1;
            }

            if (connection->session != NULL) {
                std::stringstream cookiestr;
                std::stringstream cookie;

                cookiestr << KIS_SESSION_COOKIE << "=";
                cookiestr << connection->session->sessionid;
                cookiestr << "; Path=/";

                MHD_add_response_header(response, MHD_HTTP_HEADER_SET_COOKIE, 
                        cookiestr.str().c_str());
            }

            char lastmod[31];
            struct tm tmstruct;
            localtime_r(&(buf.st_ctime), &tmstruct);
            strftime(lastmod, 31, "%a, %d %b %Y %H:%M:%S %Z", &tmstruct);
            MHD_add_response_header(response, "Last-Modified", lastmod);

            string suffix = GetSuffix(surl);
            string mime = kishttpd->GetMimeType(suffix);

            if (mime != "") {
                MHD_add_response_header(response, "Content-Type", mime.c_str());
            } else {
                MHD_add_response_header(response, "Content-Type", "text/plain");
            }

            // Allow any?
            MHD_add_response_header(response, "Access-Control-Allow-Origin", "*");

            // Never let the browser cache our responses.  Maybe moderate this
            // in the future to cache for 60 seconds or something?
            MHD_add_response_header(response, "Cache-Control", "no-cache");
            MHD_add_response_header(response, "Pragma", "no-cache");
            MHD_add_response_header(response, 
                    "Expires", "Sat, 01 Jan 2000 00:00:00 GMT");

            MHD_queue_response(connection->connection, MHD_HTTP_OK, response);
            MHD_destroy_response(response);

            return 1;
        }
    }

    return -1;
}

void Kis_Net_Httpd::AppendHttpSession(Kis_Net_Httpd *httpd, Kis_Net_Httpd_Connection *connection) {

    if (connection->session != NULL) {
        std::stringstream cookiestr;
        std::stringstream cookie;

        cookiestr << KIS_SESSION_COOKIE << "=";
        cookiestr << connection->session->sessionid;
        cookiestr << "; Path=/";

        MHD_add_response_header(connection->response, MHD_HTTP_HEADER_SET_COOKIE, 
                cookiestr.str().c_str());
    }
}

void Kis_Net_Httpd::AppendStandardHeaders(Kis_Net_Httpd *httpd,
        Kis_Net_Httpd_Connection *connection, const char *url) {

    // Last-modified is always now
    char lastmod[31];
    struct tm tmstruct;
    time_t now;
    time(&now);
    gmtime_r(&now, &tmstruct);
    strftime(lastmod, 31, "%a, %d %b %Y %H:%M:%S %Z", &tmstruct);
    MHD_add_response_header(connection->response, "Last-Modified", lastmod);

    string suffix = GetSuffix(url);
    string mime = httpd->GetMimeType(suffix);

    if (mime != "") {
        MHD_add_response_header(connection->response, "Content-Type", mime.c_str());
    } else {
        MHD_add_response_header(connection->response, "Content-Type", "text/plain");
    }

    // If we have an optional filename set, set our disposition type and then
    // add the filename attribute
    if (connection->optional_filename != "") {
        string disp = "attachment; filename=\"" + connection->optional_filename + "\"";
        MHD_add_response_header(connection->response, "Content-Disposition", disp.c_str());
    }

    // Allow any?  This lets us handle webuis hosted elsewhere
    MHD_add_response_header(connection->response, 
            "Access-Control-Allow-Origin", "*");

    // Never let the browser cache our responses.  Maybe moderate this
    // in the future to cache for 60 seconds or something?
    MHD_add_response_header(connection->response, "Cache-Control", "no-cache");
    MHD_add_response_header(connection->response, "Pragma", "no-cache");
    MHD_add_response_header(connection->response, 
            "Expires", "Sat, 01 Jan 2000 00:00:00 GMT");

}

int Kis_Net_Httpd::SendHttpResponse(Kis_Net_Httpd *httpd,
        Kis_Net_Httpd_Connection *connection) {

    MHD_queue_response(connection->connection, connection->httpcode, 
            connection->response);

    MHD_destroy_response(connection->response);

    return MHD_YES;
}

int Kis_Net_Httpd::SendStandardHttpResponse(Kis_Net_Httpd *httpd,
        Kis_Net_Httpd_Connection *connection, const char *url) {
    AppendHttpSession(httpd, connection);
    AppendStandardHeaders(httpd, connection, url);
    return SendHttpResponse(httpd, connection);
}

Kis_Net_Httpd_Handler::Kis_Net_Httpd_Handler(GlobalRegistry *in_globalreg) {
    httpd = NULL;
    http_globalreg = in_globalreg;

    Bind_Httpd_Server(in_globalreg);

}

Kis_Net_Httpd_Handler::~Kis_Net_Httpd_Handler() {
    httpd = 
        static_pointer_cast<Kis_Net_Httpd>(http_globalreg->FetchGlobal("HTTPD_SERVER"));

    if (httpd != NULL)
        httpd->RemoveHandler(this);
}

void Kis_Net_Httpd_Handler::Bind_Httpd_Server(GlobalRegistry *in_globalreg) {
    if (in_globalreg != NULL) {
        http_globalreg = in_globalreg;

        httpd = 
            static_pointer_cast<Kis_Net_Httpd>(in_globalreg->FetchGlobal("HTTPD_SERVER"));
        if (httpd != NULL)
            httpd->RegisterHandler(this);

        entrytracker = 
            static_pointer_cast<EntryTracker>(http_globalreg->FetchGlobal("ENTRY_TRACKER"));
    }
}

bool Kis_Net_Httpd_Handler::Httpd_CanSerialize(string path) {
    return entrytracker->CanSerialize(httpd->GetSuffix(path));
}

string Kis_Net_Httpd_Handler::Httpd_GetSuffix(string path) {
    return httpd->GetSuffix(path);
}

string Kis_Net_Httpd_Handler::Httpd_StripSuffix(string path) {
    return httpd->StripSuffix(path);
}

bool Kis_Net_Httpd_CPPStream_Handler::Httpd_Serialize(string path, 
        std::stringstream &stream, SharedTrackerElement e, 
        TrackerElementSerializer::rename_map *name_map) {
    return entrytracker->Serialize(httpd->GetSuffix(path), stream, e, name_map);
}

int Kis_Net_Httpd_CPPStream_Handler::Httpd_HandleGetRequest(Kis_Net_Httpd *httpd, 
        Kis_Net_Httpd_Connection *connection,
        const char *url, const char *method, const char *upload_data,
        size_t *upload_data_size) {

    std::stringstream stream;
    int ret;

    if (connection != NULL)
        std::lock_guard<std::mutex> lk(connection->connection_mutex);

    Httpd_CreateStreamResponse(httpd, connection, url, method, upload_data,
            upload_data_size, stream);

    if (connection->response == NULL) {
        connection->response = 
            MHD_create_response_from_buffer(stream.str().length(),
                    (void *) stream.str().data(), MHD_RESPMEM_MUST_COPY);

        ret = httpd->SendStandardHttpResponse(httpd, connection, url);

        return ret;
    }
    
    return MHD_YES;
}

int Kis_Net_Httpd_CPPStream_Handler::Httpd_HandlePostRequest(Kis_Net_Httpd *httpd, 
        Kis_Net_Httpd_Connection *connection,
        const char *url, const char *method, const char *upload_data,
        size_t *upload_data_size) {

    // Call the post complete and populate our stream
    if (connection != NULL)
        std::lock_guard<std::mutex> lk(connection->connection_mutex);

    Httpd_PostComplete(connection);

    if (connection->response == NULL) {
        connection->response = 
            MHD_create_response_from_buffer(connection->response_stream.str().length(),
                    (void *) connection->response_stream.str().data(), 
                    MHD_RESPMEM_MUST_COPY);

        return httpd->SendStandardHttpResponse(httpd, connection, url);
    } 

    return MHD_YES;
}


bool Kis_Net_Httpd::HasValidSession(Kis_Net_Httpd_Connection *connection,
        bool send_invalid) {
    if (connection->session != NULL)
        return true;

    shared_ptr<Kis_Net_Httpd_Session> s;
    const char *cookieval;

    cookieval = MHD_lookup_connection_value(connection->connection,
            MHD_COOKIE_KIND, KIS_SESSION_COOKIE);

    if (cookieval != NULL) {
        auto si = session_map.find(cookieval);

        if (si != session_map.end()) {

            s = si->second;

            // Does the session never expire?
            if (s->session_lifetime == 0) {
                connection->session = s;
                return true;
            }

            // Is the session still valid?
            if (globalreg->timestamp.tv_sec < s->session_created + s->session_lifetime) {
                connection->session = s;
                return true;
            } else {
                DelSession(si);
                connection->session = NULL;
            }
        }
    }

    // If we got here, we either don't have a session, or the session isn't valid.
    if (websession != NULL && websession->validate_login(connection->connection)) {
        CreateSession(connection, NULL, session_timeout);
        return true;
    }

    // If we got here it's invalid.  Do we automatically send an invalidation 
    // response?
    if (send_invalid) {
        string respstr = "Login Required";

        connection->response = 
            MHD_create_response_from_buffer(respstr.length(),
                    (void *) respstr.c_str(), MHD_RESPMEM_MUST_COPY);

        MHD_queue_basic_auth_fail_response(connection->connection,
                "Kismet Admin", connection->response);
    }

    return false;
}

void Kis_Net_Httpd::CreateSession(Kis_Net_Httpd_Connection *connection, 
        struct MHD_Response *response, time_t in_lifetime) {
    
    shared_ptr<Kis_Net_Httpd_Session> s;

    // Use 128 bits of entropy to make a session key

    char rdata[16];
    FILE *urandom;

    if ((urandom = fopen("/dev/urandom", "rb")) == NULL) {
        _MSG("Failed to open /dev/urandom to create a HTTPD session, unable to "
                "assign a sessionid, not creating session", MSGFLAG_ERROR);
        return;
    }

    if (fread(rdata, 16, 1, urandom) != 1) {
        _MSG("Failed to read entropy from /dev/urandom to create a HTTPD session, "
                "unable to assign a sessionid, not creating session", MSGFLAG_ERROR);
        fclose(urandom);
        return;
    }
    fclose(urandom);

    std::stringstream cookiestr;
    std::stringstream cookie;
    
    cookiestr << KIS_SESSION_COOKIE << "=";

    for (unsigned int x = 0; x < 16; x++) {
        cookie << std::uppercase << std::setfill('0') << std::setw(2) 
            << std::hex << (int) (rdata[x] & 0xFF);
    }

    cookiestr << cookie.str();

    cookiestr << "; Path=/";

    if (response != NULL) {
        if (MHD_add_response_header(response, MHD_HTTP_HEADER_SET_COOKIE, 
                    cookiestr.str().c_str()) == MHD_NO) {
            _MSG("Failed to add session cookie to response header, unable to create "
                    "a session", MSGFLAG_ERROR);
            return;
        }
    }

    s.reset(new Kis_Net_Httpd_Session());
    s->sessionid = cookie.str();
    s->session_created = globalreg->timestamp.tv_sec;
    s->session_seen = s->session_created;
    s->session_lifetime = in_lifetime;

    if (connection != NULL)
        connection->session = s;

    AddSession(s);
}

bool Kis_Net_Httpd_No_Files_Handler::Httpd_VerifyPath(const char *path, 
        const char *method) {

    if (strcmp(method, "GET") != 0)
        return false;

    if (strcmp(path, "/index.html") == 0 ||
            strcmp(path, "/") == 0)
        return true;

    return false;
}


void Kis_Net_Httpd_No_Files_Handler::Httpd_CreateStreamResponse(Kis_Net_Httpd *httpd __attribute__((unused)),
        Kis_Net_Httpd_Connection *connection __attribute__((unused)),
        const char *url __attribute__((unused)), 
        const char *method __attribute__((unused)), 
        const char *upload_data __attribute__((unused)),
        size_t *upload_data_size __attribute__((unused)), 
        std::stringstream &stream) {

    stream << "<html>";
    stream << "<head><title>Web UI Disabled</title></head>";
    stream << "<body>";
    stream << "<h2>Sorry</h2>";
    stream << "<p>The Web UI in Kismet is disabled because Kismet cannot serve ";
    stream << "static web pages.";
    stream << "<p>Check the output of kismet_server and make sure your ";
    stream << "<blockquote><pre>httpd_home=...</pre>";
    stream << "and/or<br>";
    stream << "<pre>httpd_user_home=...</pre></blockquote>";
    stream << "configuration values are set in kismet.conf or kismet_httpd.conf ";
    stream << "and restart Kismet";
    stream << "</body>";
    stream << "</html>";
}

Kis_Net_Httpd_Buffer_Stream_Aux::Kis_Net_Httpd_Buffer_Stream_Aux(
        Kis_Net_Httpd_Buffer_Stream_Handler *in_handler,
        Kis_Net_Httpd_Connection *in_httpd_connection,
        shared_ptr<BufferHandlerGeneric> in_ringbuf_handler,
        void *in_aux, function<void (Kis_Net_Httpd_Buffer_Stream_Aux *)> in_free_aux) {

    httpd_stream_handler = in_handler;
    httpd_connection = in_httpd_connection;
    ringbuf_handler = in_ringbuf_handler;
    aux = in_aux;
    free_aux_cb = in_free_aux;

    cl.reset(new conditional_locker<int>());
    cl->lock();

    in_error = false;

    // If the buffer encounters an error, unlock the variable and set the error state
    ringbuf_handler->SetProtocolErrorCb([this]() {
            trigger_error();
        });

    // Lodge ourselves as the write handler
    ringbuf_handler->SetWriteBufferInterface(this);
}

Kis_Net_Httpd_Buffer_Stream_Aux::~Kis_Net_Httpd_Buffer_Stream_Aux() {
    // Get out of the lock and flag an error so we end
    local_demand_locker dlock(&error_mutex);
    dlock.lock();
    in_error = true;
    dlock.unlock();

    cl->unlock(0);

    // Lock that the buffer callback has ended!
    local_demand_locker bclock(&buffer_event_mutex);
    bclock.lock();

    // Lock out the interface itself
    local_demand_locker lock(&aux_mutex);
    lock.lock();

    if (ringbuf_handler) {
        ringbuf_handler->RemoveWriteBufferInterface();
        ringbuf_handler->SetProtocolErrorCb(NULL);
    }
}

void Kis_Net_Httpd_Buffer_Stream_Aux::BufferAvailable(size_t in_amt) {
    // All we need to do here is unlock the conditional lock; the 
    // buffer_event_cb callback will unlock and read from the buffer, then
    // re-lock and block
    // fprintf(stderr, "debug - knmh - unlocking %lu\n", in_amt);
    cl->unlock(1);
}

void Kis_Net_Httpd_Buffer_Stream_Aux::block_until_data() {
    // Protect until we lock
    local_demand_locker lock(&aux_mutex);

    // Scope this block
    lock.lock();

    // Immediately return if we have pending data
    shared_ptr<BufferHandlerGeneric> rbh = get_rbhandler();
    if (rbh->GetReadBufferUsed()) {
        return;
    }

    // Immediately return so we can flush out the buffer before we fail
    if (get_in_error()) {
        return;
    }

    lock.unlock();

    cl->lock();

    // Block outside of the mutex protection
    cl->block_until();
}

Kis_Net_Httpd_Buffer_Stream_Handler::~Kis_Net_Httpd_Buffer_Stream_Handler() {

}

ssize_t Kis_Net_Httpd_Buffer_Stream_Handler::buffer_event_cb(void *cls, uint64_t pos,
        char *buf, size_t max) {
    Kis_Net_Httpd_Buffer_Stream_Aux *stream_aux = (Kis_Net_Httpd_Buffer_Stream_Aux *) cls;

    // Protect that we have to exit the buffer cb before the stream can be killed, don't
    // use an automatic locker because we can't let it time out
    stream_aux->get_buffer_event_mutex()->lock();

    shared_ptr<BufferHandlerGeneric> rbh = stream_aux->get_rbhandler();

    size_t read_sz = 0;

    // Read from the buffer; currently we have to force a copy into our existing
    // buffer unfortunately
    unsigned char *zbuf;

    while (read_sz == 0) {
        // We get called as soon as the webserver has either a) processed our request
        // or b) sent what we gave it; we need to hold the thread until we
        // get more data in the buf, so we block until we have data
        stream_aux->block_until_data();

        read_sz = rbh->ZeroCopyPeekWriteBufferData((void **) &zbuf, max);

        if (read_sz == 0) {
            rbh->PeekFreeWriteBufferData(zbuf);

            if (stream_aux->get_in_error()) {
                // fprintf(stderr, "debug - buffer event %p end of stream\n", stream_aux);
                stream_aux->get_buffer_event_mutex()->unlock();
                return MHD_CONTENT_READER_END_OF_STREAM;
            }
        }
    }

    if (read_sz != 0 && zbuf != NULL && buf != NULL) {
        memcpy(buf, zbuf, read_sz);
    }

    rbh->PeekFreeWriteBufferData(zbuf);
    rbh->ConsumeWriteBufferData(read_sz);

    stream_aux->get_buffer_event_mutex()->unlock();

    return (ssize_t) read_sz;
}

static void free_buffer_aux_callback(void *cls) {
    Kis_Net_Httpd_Buffer_Stream_Aux *aux = (Kis_Net_Httpd_Buffer_Stream_Aux *) cls;

    // fprintf(stderr, "debug - free aux callback %p\n", cls);

    // Wait for the thread to complete
    aux->generator_thread.join();

    // fprintf(stderr, "debug - generator unlocked %p\n", cls);

    if (aux->free_aux_cb != NULL) {
        aux->free_aux_cb(aux);
    }

    aux->ringbuf_handler->ProtocolError();

    delete(aux);
}

int Kis_Net_Httpd_Buffer_Stream_Handler::Httpd_HandleGetRequest(Kis_Net_Httpd *httpd, 
        Kis_Net_Httpd_Connection *connection,
        const char *url, const char *method, const char *upload_data,
        size_t *upload_data_size) {

    if (connection != NULL)
        std::lock_guard<std::mutex> lk(connection->connection_mutex);

    if (connection->response == NULL) {
        shared_ptr<BufferHandlerGeneric> rbh(allocate_buffer());

        Kis_Net_Httpd_Buffer_Stream_Aux *aux = 
            new Kis_Net_Httpd_Buffer_Stream_Aux(this, connection, rbh, NULL, NULL);
        connection->custom_extension = aux;

        // Set up a locker to make sure the thread is up and running
        conditional_locker<int> cl;
        cl.lock();

        // Run it in its own thread and set up the connection streaming object; we MUST pass
        // the aux as a direct pointer because the microhttpd backend can delete the 
        // connection BEFORE calling our cleanup on our response!
        aux->generator_thread =
            std::thread([this, &cl, aux, httpd, connection, url, method, upload_data, upload_data_size]{
                // Unlock the thread as soon as we've spawned it
                cl.unlock(1);

                int r = 
                    Httpd_CreateStreamResponse(httpd, connection, url, method, upload_data,
                        upload_data_size);

                // Trigger 'error' when the function is complete, causing us to finish 
                // the stream
                if (r == MHD_YES) {
                    aux->sync();
                    aux->trigger_error();
                }
                });

        cl.block_until();

        connection->response = 
            MHD_create_response_from_callback(MHD_SIZE_UNKNOWN, 32 * 1024,
                    &buffer_event_cb, aux, &free_buffer_aux_callback);

        return httpd->SendStandardHttpResponse(httpd, connection, url);
    }

    return MHD_NO;
}

int Kis_Net_Httpd_Buffer_Stream_Handler::Httpd_HandlePostRequest(Kis_Net_Httpd *httpd,
        Kis_Net_Httpd_Connection *connection, 
        const char *url, const char *method, const char *upload_data,
        size_t *upload_data_size) {

    if (connection != NULL)
        std::lock_guard<std::mutex> lk(connection->connection_mutex);

    if (connection->response == NULL) {
        // No read, default write
        shared_ptr<BufferHandlerGeneric> rbh(allocate_buffer());

        Kis_Net_Httpd_Buffer_Stream_Aux *aux = 
            new Kis_Net_Httpd_Buffer_Stream_Aux(this, connection, rbh, NULL, NULL);
        connection->custom_extension = aux;

        // fprintf(stderr, "debug - made post aux %p\n", aux);

        // Call the post complete and populate our stream;
        // Run it in its own thread and set up the connection streaming object; we MUST pass
        // the aux as a direct pointer because the microhttpd backend can delete the 
        // connection BEFORE calling our cleanup on our response!
        //
        // Set up a locker to make sure the thread is up and running
        conditional_locker<int> cl;
        cl.lock();

        aux->generator_thread =
            std::thread([this, &cl, aux, connection] {
                cl.unlock(1);

                int r = Httpd_PostComplete(connection);

                // Trigger 'error' when the function is complete, causing us to finish 
                // the stream
                if (r == MHD_YES) {
                    aux->sync();
                    aux->trigger_error();
                }
                });

        cl.block_until();

        connection->response = 
            MHD_create_response_from_callback(MHD_SIZE_UNKNOWN, 32 * 1024,
                    &buffer_event_cb, aux, &free_buffer_aux_callback);

        return httpd->SendStandardHttpResponse(httpd, connection, url);
    }

    return MHD_NO;
}

