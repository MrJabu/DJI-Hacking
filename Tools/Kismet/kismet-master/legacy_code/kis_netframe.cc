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
#include "util.h"
#include "configfile.h"
#include "packet.h"
#include "packetsourcetracker.h"
#include "packetchain.h"
#include "kis_netframe.h"
#include "tcpserver.h"
#include "getopt.h"
#include "dumpfile.h"
#include "phy_80211.h"

void KisNetframe_MessageClient::ProcessMessage(string in_msg, int in_flags) {
    // Local messages and alerts don't go out to the world.  Alerts are sent via
    // the ALERT protocol.
    if ((in_flags & MSGFLAG_LOCAL) || (in_flags & MSGFLAG_ALERT))
        return;

    STATUS_data sdata;
    sdata.text = in_msg;
    sdata.flags = in_flags;
    
    // Dispatch it out to the clients
    ((KisNetFramework *) auxptr)->SendToAll(_NPM(PROTO_REF_STATUS), (void *) &sdata);

}

int KisNetFrame_TimeEvent(Timetracker::timer_event *evt, void *parm, 
                          GlobalRegistry *globalreg) {
    // We'll just assume we'll never fail here and that the TIME protocol
    // always exists.  If this isn't the case, we'll fail horribly.
    time_t curtime = time(0);

    globalreg->kisnetserver->SendToAll(globalreg->netproto_map[PROTO_REF_TIME], 
                                       (void *) &curtime);
    
    return 1;
}

KisNetFramework::KisNetFramework() {
    fprintf(stderr, "FATAL OOPS:  KisNetFramework() called with no globalreg\n");
    exit(1);
}

void KisNetFramework::Usage(char *name) {
    printf(" *** Kismet Client/Server Options ***\n");
    printf(" -l, --server-listen          Override Kismet server listen options\n");
}

KisNetFramework::KisNetFramework(GlobalRegistry *in_globalreg) {
    globalreg = in_globalreg;
    netserver = NULL;
    int port = 0, maxcli = 0;
    char srv_proto[11], srv_bindhost[129];
    TcpServer *tcpsrv;
    string listenline;
    next_netprotoref = 0;

    valid = 0;

    // Sanity check for timetracker
    if (globalreg->timetracker == NULL) {
        fprintf(stderr, "FATAL OOPS: KisNetFramework called without timetracker\n");
        exit(1);
    }

    if (globalreg->kismet_config == NULL) {
        fprintf(stderr, "FATAL OOPS: KisNetFramework called without kismet_config\n");
        exit(1);
    }

    if (globalreg->messagebus == NULL) {
        fprintf(stderr, "FATAL OOPS: KisNetFramework called without messagebus\n");
        exit(1);
    }

    // Commandline stuff
    static struct option netframe_long_options[] = {
        { "server-listen", required_argument, 0, 'l' },
        { 0, 0, 0, 0 }
    };
    int option_idx = 0;

    // Hack the extern getopt index
    optind = 0;

    while (1) {
        int r = getopt_long(globalreg->argc, globalreg->argv,
                            "-l:",
                            netframe_long_options, &option_idx);
        if (r < 0) break;

        switch (r) {
            case 'l':
                listenline = string(optarg);
                break;
        }
    }
    
    // Parse the config file and get the protocol and port info...  ah, abusing
    // evaluation shortcuts
    if (listenline.length() == 0 && 
        (listenline = globalreg->kismet_config->FetchOpt("listen")) == "") {
        _MSG("No 'listen' config line defined for the Kismet UI server; This "
             "usually means you have not upgraded your Kismet config file.  Copy "
             "the config file from the source directory and replace your "
             "current kismet.conf (Or specify the new config file manually)", 
             MSGFLAG_FATAL);
        globalreg->fatal_condition = 1;
        return;
    }

    if (sscanf(listenline.c_str(), 
               "%10[^:]://%128[^:]:%d", srv_proto, srv_bindhost, &port) != 3) {
        _MSG("Malformed 'listen' config line defined for the Kismet UI server",
             MSGFLAG_FATAL);
        globalreg->fatal_condition = 1;
        return;
    }

    if (globalreg->kismet_config->FetchOpt("maxclients") == "") {
        _MSG("No 'maxclients' config line defined for the Kismet UI server, "
             "defaulting to 5 clients.", MSGFLAG_INFO);
        maxcli = 5;
    } else if (sscanf(globalreg->kismet_config->FetchOpt("maxclients").c_str(), 
                      "%d", &maxcli) != 1) {
        _MSG("Malformed 'maxclients' config line defined for the Kismet UI server",
             MSGFLAG_FATAL);
        globalreg->fatal_condition = 1;
        return;
    }

    if (globalreg->kismet_config->FetchOpt("maxbacklog") == "") {
        _MSG("No 'maxbacklog' config line defined for the Kismet UI server, "
             "defaulting to 5000 lines", MSGFLAG_INFO);
        maxbacklog = 5000;
    } else if (sscanf(globalreg->kismet_config->FetchOpt("maxbacklog").c_str(), 
                      "%d", &maxbacklog) != 1) {
        _MSG("Malformed 'maxbacklog' config line defined for the Kismet UI server",
             MSGFLAG_FATAL);
        globalreg->fatal_condition = 1;
        return;
    }

    if (globalreg->kismet_config->FetchOpt("allowedhosts") == "") {
        _MSG("No 'allowedhosts' config line defined for the Kismet UI server",
             MSGFLAG_FATAL);
        globalreg->fatal_condition = 1;
        return;
    }

    // We only know how to set up a tcp server right now
    if (strncasecmp(srv_proto, "tcp", 4) == 0) {
        tcpsrv = new TcpServer(globalreg);
        // Expand the ring buffer size
        tcpsrv->SetRingSize(100000);
        tcpsrv->SetupServer(port, maxcli, srv_bindhost, 
                            globalreg->kismet_config->FetchOpt("allowedhosts"));
        netserver = tcpsrv;
        server_type = 0;
    } else {
        server_type = -1;
        _MSG("Invalid protocol in 'listen' config line for the Kismet UI server",
             MSGFLAG_FATAL);
        globalreg->fatal_condition = 1;
        return;
    }

    // Register the core Kismet protocols

    // Other protocols

    valid = 1;
}

int KisNetFramework::Activate() {
    if (server_type != 0) {
        _MSG("KisNetFramework unknown server type, something didn't initialize",
             MSGFLAG_FATAL);
        globalreg->fatal_condition = 1;
        return -1;
    }

    if (netserver->EnableServer() < 0 || globalreg->fatal_condition) {
        _MSG("Failed to enable TCP listener for the Kismet UI server",
             MSGFLAG_FATAL);
        globalreg->fatal_condition = 1;
        return -1;
    }
    netserver->RegisterServerFramework(this);

    return 1;
}

KisNetFramework::~KisNetFramework() {
    // Remove our message handler
    globalreg->messagebus->RemoveClient(kisnet_msgcli);
}

int KisNetFramework::Accept(int in_fd) {
    // Create their options
    client_opt *opt = new client_opt;
    client_optmap[in_fd] = opt;
    char temp[512];

    // Set the mandatory sentences.  We don't have to do error checking here because
    // it can't exist in the required vector if it isn't registered.
    for (unsigned int reqprot = 0; reqprot < required_protocols.size(); reqprot++) {
        int tref = required_protocols[reqprot];
        vector<int> reqfields;
        map<int, server_protocol *>::iterator spitr = protocol_map.find(tref);
        for (unsigned int fnum = 0; fnum < spitr->second->field_vec.size(); fnum++) {
            reqfields.push_back(fnum);
        }

        AddProtocolClient(in_fd, tref, reqfields);
    }

    // Send the mandatory stuff like the Kismet info
    KISMET_data kdat;

    kdat.version = "0.0.0";
    snprintf(temp, 512, "%u", (unsigned int) globalreg->start_time);
    kdat.starttime = string(temp);
    kdat.servername = globalreg->servername;
    kdat.timestamp = "0";
    kdat.newversion = globalreg->version_major + string(",") + 
        globalreg->version_minor + string(",") +
        globalreg->version_tiny;
    kdat.uid = geteuid();
   
    SendToClient(in_fd, globalreg->netproto_map[PROTO_REF_KISMET], 
                 (void *) &kdat, NULL);
  
    // Protocols
    SendToClient(in_fd, globalreg->netproto_map[PROTO_REF_PROTOCOL], 
                 (void *) &protocol_map, NULL);
    
    _MSG("Kismet server accepted connection from " +
         netserver->GetRemoteAddr(in_fd), MSGFLAG_INFO);

    return 1;
}

int KisNetFramework::BufferDrained(int in_fd) {
    map<int, client_opt *>::iterator opitr = client_optmap.find(in_fd);
    if (opitr == client_optmap.end()) {
        snprintf(errstr, 1024, "KisNetFramework::SendToClient illegal client %d.", 
                 in_fd);
        globalreg->messagebus->InjectMessage(errstr, MSGFLAG_ERROR);
        return -1;
    }
    client_opt *opt = opitr->second;
    int ret = 0;

    if (opt->backlog.size() == 0)
        return 0;

    while (opt->backlog.size() > 0) {
        string outtext = opt->backlog[0];

        ret = netserver->WriteData(in_fd, (uint8_t *) outtext.c_str(), 
                                   outtext.length());

        // Catch "full buffer" error and stop trying to shove more down it
        if (ret == -2) 
            return 0;

        if (ret < 0)
            return ret;

        opt->backlog.erase(opt->backlog.begin());

        if (opt->backlog.size() == 0) {
            snprintf(errstr, 1024, "Flushed protocol data backlog for Kismet "
                     "client %d", in_fd);
            _MSG(errstr, (MSGFLAG_LOCAL | MSGFLAG_ERROR));
        }
    }

    return 1;
}

int KisNetFramework::ParseData(int in_fd) {
    int len, rlen;
    char *buf;
    string strbuf;

    len = netserver->FetchReadLen(in_fd);
    buf = new char[len + 1];
    
    if (netserver->ReadData(in_fd, buf, len, &rlen) < 0) {
        globalreg->messagebus->InjectMessage("KisNetFramework::ParseData failed to "
                                             "fetch data from the client.", 
                                             MSGFLAG_ERROR);
        delete[] buf;
        return -1;
    }
    buf[len] = '\0';

    // Parse without including partials, so we don't get a fragmented command 
    // out of the buffer
    vector<string> inptok = StrTokenize(buf, "\n", 0);
    delete[] buf;

    // Bail on no useful data
    if (inptok.size() < 1) {
        return 0;
    }


    for (unsigned int it = 0; it < inptok.size(); it++) {
        // No matter what we've dealt with this data block
        netserver->MarkRead(in_fd, inptok[it].length() + 1);

        // Handle funny trailing stuff from telnet and some other clients
        if (inptok[it][inptok[it].length() - 1] == '\r') {
            inptok[it] = inptok[it].substr(0, inptok[it].length() - 1);
        }
        
        vector<smart_word_token> cmdtoks = NetStrTokenize(inptok[it], " ");

        if (cmdtoks.size() < 2) {
            // Silently fail since there wasn't enough to deal with it
            continue;
        }

        int cmdid;
        if (sscanf(cmdtoks[0].word.c_str(), "!%d", &cmdid) != 1) {
            // Silently fail if we can't figure out how to generate the error, again
            continue;
        }

        // Nuke the first element of the command tokens (we just pulled it off to 
        // get the cmdid)
        cmdtoks.erase(cmdtoks.begin());

        // Find a command function to deal with this protocol
        CLIRESP_data rdat;
        rdat.cmdid = cmdid;

        map<string, KisNetFramework::client_command_rec *>::iterator ccitr = 
            client_cmd_map.find(StrLower(cmdtoks[0].word));
        if (ccitr != client_cmd_map.end()) {
            // Nuke the first word again - we just pulled it off to get the command
            // fprintf(stderr, "debug - ctoks '%s' %u %u %u\n", cmdtoks[0].word.c_str(), cmdtoks.size(), cmdtoks[0].end, inptok[it].length());
            cmdtoks.erase(cmdtoks.begin());

            // fprintf(stderr, "debug - fullcmd '%s' %u %u %u\n", cmdtoks[0].word.c_str(), cmdtoks.size(), cmdtoks[0].end, inptok[it].length());

            string fullcmd = 
                inptok[it].substr(cmdtoks[0].end, (inptok[it].length() - 
                                                   cmdtoks[0].end));
            // Call the processor and return error conditions and ack
            if ((*ccitr->second->cmd)
                (in_fd, this, globalreg, errstr, fullcmd, &cmdtoks, 
                 ccitr->second->auxptr) < 0) {
                rdat.resptext = string(errstr);
                SendToClient(in_fd, globalreg->netproto_map[PROTO_REF_ERROR], 
                             (void *) &rdat, NULL);
                _MSG("Failed Kismet client command: " + rdat.resptext, MSGFLAG_ERROR);
            } else {
                rdat.resptext = string("OK");
                SendToClient(in_fd, globalreg->netproto_map[PROTO_REF_ACK], 
                             (void *) &rdat, NULL);
            }
        } else {
            rdat.resptext = string("NO SUCH COMMAND");
            SendToClient(in_fd, globalreg->netproto_map[PROTO_REF_ERROR], 
                         (void *) &rdat, NULL);
        }

    }
    
    return 1;
}

int KisNetFramework::KillConnection(int in_fd) {
    // Do a little testing here since we might not have an opt record
    map<int, client_opt *>::iterator citr = client_optmap.find(in_fd);
    if (citr != client_optmap.end()) {
        // Remove all our protocols
        map<int, vector<int> >::iterator clpitr;

        while ((clpitr = citr->second->protocols.begin()) != 
               citr->second->protocols.end()) {
            DelProtocolClient(in_fd, clpitr->first);
        }

            /*
        for (map<int, vector<int> >::iterator clpitr = 
             citr->second->protocols.begin(); 
             clpitr != citr->second->protocols.end(); ++clpitr)
            DelProtocolClient(in_fd, clpitr->first);
            */

        client_opt *sec = citr->second;
        client_optmap.erase(citr);
        delete sec;
    }

    return 1;
}

int KisNetFramework::Shutdown() {
    return ServerFramework::Shutdown();
}

int KisNetFramework::RegisterClientCommand(string in_cmdword, ClientCommand in_cmd,
                                           void *in_auxptr) {
    string lcmd = StrLower(in_cmdword);

    if (in_cmdword.length() > 16) {
        snprintf(errstr, 1024, "KisNetFramework::RegisterClientCommand refusing to "
                 "register '%s' as it is greater than 16 characters.",
                 in_cmdword.c_str());
        globalreg->messagebus->InjectMessage(errstr, MSGFLAG_ERROR);
        return -1;
    }

    if (client_cmd_map.find(lcmd) != client_cmd_map.end()) {
        snprintf(errstr, 1024, "KisNetFramework::RegisterClientCommand refusing to "
                 "register command '%s', command already exists.", lcmd.c_str());
        globalreg->messagebus->InjectMessage(errstr, MSGFLAG_ERROR);
        return -1;
    }

    client_command_rec *cmdrec = new client_command_rec;
    cmdrec->cmd = in_cmd;
    cmdrec->auxptr = in_auxptr;

    client_cmd_map[lcmd] = cmdrec;

    return 1;
}

int KisNetFramework::RemoveClientCommand(string in_cmdword) {
    if (client_cmd_map.find(in_cmdword) == client_cmd_map.end())
        return 0;

    delete client_cmd_map[in_cmdword];
    client_cmd_map.erase(in_cmdword);

    return 1;
}

// Create an output string based on the clients
// This looks very complex - and it is - but almost all of the "big" ops like
// find are done with integer references.  They're cheap.
// This takes the struct to be sent and pumps it through the dynamic protocol/field
// system.
int KisNetFramework::SendToClient(int in_fd, int in_refnum, const void *in_data,
                                  kis_protocol_cache *in_cache) {
    // Make sure this is a valid client
    map<int, client_opt *>::iterator opitr = client_optmap.find(in_fd);
    if (opitr == client_optmap.end()) {
        snprintf(errstr, 1024, "KisNetFramework::SendToClient illegal client %d.", 
                 in_fd);
        globalreg->messagebus->InjectMessage(errstr, MSGFLAG_ERROR);
        return -1;
    }
    client_opt *opt = opitr->second;

    // See if this client even handles this protocol...
    map<int, vector<int> >::iterator clprotitr = opt->protocols.find(in_refnum);
    if (clprotitr == opt->protocols.end())
        return 0;

    const vector<int> *fieldlist = &clprotitr->second;

    // Find this protocol now - we only do this after we're sure we want to print to
    // it.
    map<int, server_protocol *>::iterator spitr = protocol_map.find(in_refnum);
    if (spitr == protocol_map.end()) {
        snprintf(errstr, 1024, "KisNetFramework::SendToClient Protocol %d not "
                 "registered.", in_refnum);
        globalreg->messagebus->InjectMessage(errstr, MSGFLAG_ERROR);
        return -1;
    }
    server_protocol *prot = spitr->second;

    if (prot->cacheable && in_cache == NULL) {
        snprintf(errstr, 1024, "KisNetFramework::SendToClient protocol %s "
                 "requires caching but got a NULL cache ref, fix me",
                 prot->header.c_str());
        globalreg->messagebus->InjectMessage(errstr, MSGFLAG_ERROR);
        return -1;
    }

    // Bounce through the printer function
    string fieldtext;
    if ((*prot->printer)(fieldtext, fieldlist, in_data, prot->auxptr,
                         in_cache, globalreg) == -1) {
        snprintf(errstr, 1024, "%s", fieldtext.c_str());
        return -1;
    }

    // Assemble a line for them:
    // *HEADER: DATA\n
    //  16      x   1
    int ret = 0;

    // Check the size
    int blogsz = opt->backlog.size();

    // Bail gracefully for now
    if (blogsz >= maxbacklog) {
        return 0;
    }
    
    int nlen = prot->header.length() + fieldtext.length() + 5; // *..: \n\0
    char *outtext = new char[nlen];
    snprintf(outtext, nlen, "*%s: %s\n", prot->header.c_str(), fieldtext.c_str());

    // Look in the backlog vector and backlog it if we're already over-full
    if (blogsz > 0) {
        opt->backlog.push_back(outtext);
        delete[] outtext;
        return 0;
    }

    ret = netserver->WriteData(in_fd, (uint8_t *) outtext, strlen(outtext));

    // Catch "full buffer" error
    if (ret == -2) {
        snprintf(errstr, 1024, "Client %d ring buffer full, storing Kismet protocol "
                 "data in backlog vector", in_fd);
        _MSG(errstr, (MSGFLAG_LOCAL | MSGFLAG_INFO));
        opt->backlog.push_back(outtext);
        delete[] outtext;
        return 0;
    }
    
    delete[] outtext;

    if (ret < 0)
        return ret;

    return nlen;
}

int KisNetFramework::SendToAll(int in_refnum, const void *in_data) {
    vector<int> clvec;
    int nsent = 0;

    if (netserver == NULL)
        return 0;

    kis_protocol_cache cache;

    netserver->FetchClientVector(&clvec);

    for (unsigned int x = 0; x < clvec.size(); x++) {
        if (SendToClient(clvec[x], in_refnum, in_data, &cache) > 0)
            nsent++;
    }

    return nsent;
}

int KisNetFramework::RegisterProtocol(string in_header, int in_required, int in_cache,
                                      const char **in_fields,
                                      int (*in_printer)(PROTO_PARMS),
                                      void (*in_enable)(PROTO_ENABLE_PARMS),
                                      void *in_auxdata) {
    // First, see if we're already registered and return a -1 if we are.  You can't
    // register a protocol twice.
    if (FetchProtocolRef(in_header) != -1) {
        snprintf(errstr, 1024, "KisNetFramework::RegisterProtocol refusing to "
                 "register '%s' as it is already a registered protocol.",
                 in_header.c_str());
        globalreg->messagebus->InjectMessage(errstr, MSGFLAG_ERROR);
        return -1;
    }

    if (in_header.length() > 16) {
        snprintf(errstr, 1024, "KisNetFramework::RegisterProtocol refusing to "
                 "register '%s' as it is greater than 16 characters.",
                 in_header.c_str());
        globalreg->messagebus->InjectMessage(errstr, MSGFLAG_ERROR);
        return -1;
    }

    int refnum = next_netprotoref++;

    server_protocol *sen = new server_protocol;
    sen->ref_index = refnum;
    sen->header = in_header;

    int x = 0;
    while (in_fields[x] != NULL) {
        sen->field_map[in_fields[x]] = x;
        sen->field_vec.push_back(in_fields[x]);
        x++;
    }
    sen->printer = in_printer;
    sen->enable = in_enable;
    sen->required = in_required;
    sen->cacheable = in_cache;
    sen->auxptr = in_auxdata;

    // Put us in the map
    protocol_map[refnum] = sen;
    ref_map[StrLower(in_header)] = refnum;

    if (in_required)
        required_protocols.push_back(refnum);

    return refnum;
}

int KisNetFramework::RemoveProtocol(int in_protoref) {
    // Efficiency isn't the biggest deal here since it happens rarely
    
    if (in_protoref < 0)
        return 0;

    if (protocol_map.find(in_protoref) == protocol_map.end())
        return 0;

    string cmdheader = protocol_map[in_protoref]->header;
    delete protocol_map[in_protoref];
    protocol_map.erase(in_protoref);
    ref_map.erase(cmdheader);

    for (unsigned int x = 0; x < required_protocols.size(); x++) {
        if (required_protocols[x] == in_protoref) {
            required_protocols.erase(required_protocols.begin() + x);
            break;
        }
    }

    return 1;
}

int KisNetFramework::FetchProtocolRef(string in_header) {
    map<string, int>::iterator rmitr = ref_map.find(StrLower(in_header));
    if (rmitr == ref_map.end())
        return -1;

    return rmitr->second;
}

KisNetFramework::server_protocol *KisNetFramework::FetchProtocol(int in_ref) {
    KisNetFramework::server_protocol *ret = NULL;

    map<int, KisNetFramework::server_protocol *>::iterator spi =
        protocol_map.find(in_ref);

    if (spi != protocol_map.end())
        ret = spi->second;
    
    return ret;
}

int KisNetFramework::FetchNumClientRefs(int in_refnum) {
    map<int, int>::iterator cmpitr = client_mapped_protocols.find(in_refnum);
    if (cmpitr != client_mapped_protocols.end())
        return cmpitr->second;

    return 0;
}

int KisNetFramework::FetchNumClients() {
    return netserver->FetchNumClients();
}

void KisNetFramework::AddProtocolClient(int in_fd, int in_refnum, vector<int> in_fields) {
    map<int, client_opt *>::iterator citr = client_optmap.find(in_fd);
    if (citr == client_optmap.end()) {
        return;
    }

    // Find out if it already exists and increment the use count if it does
    map<int, vector<int> >::iterator clpitr = citr->second->protocols.find(in_refnum);
    if (clpitr == citr->second->protocols.end())
        client_mapped_protocols[in_refnum]++;

    citr->second->protocols[in_refnum] = in_fields;
}

void KisNetFramework::DelProtocolClient(int in_fd, int in_refnum) {
    map<int, client_opt *>::iterator citr = client_optmap.find(in_fd);
    if (citr == client_optmap.end())
        return;

    map<int, vector<int> >::iterator clpitr = citr->second->protocols.find(in_refnum);
    if (clpitr != citr->second->protocols.end()) {
        citr->second->protocols.erase(clpitr);
        client_mapped_protocols[in_refnum]--;
    }
}
