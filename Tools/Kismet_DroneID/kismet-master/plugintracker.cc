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

#include <dirent.h>
#include <dlfcn.h>
#include <errno.h>
#include <fcntl.h>
#include <signal.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <unistd.h>
#include <limits.h>
#include <stdlib.h>

#include "configfile.h"
#include "getopt.h"
#include "globalregistry.h"
#include "messagebus.h"
#include "plugintracker.h"
#include "version.h"
#include "kis_httpd_registry.h"

Plugintracker::Plugintracker(GlobalRegistry *in_globalreg) :
    LifetimeGlobal(),
    Kis_Net_Httpd_CPPStream_Handler(in_globalreg) {
    globalreg = in_globalreg;

    int option_idx = 0;
    int cmdline_disable = 0;
    int config_disable = 0;

    // Longopts
    static struct option plugin_long_options[] = {
        {"disable-plugins", no_argument, 0, 10}, {0, 0, 0, 0}};

    optind = 0;

    while (1) {
        int r = getopt_long(globalreg->argc, globalreg->argv, "-",
                            plugin_long_options, &option_idx);

        if (r < 0) break;
        switch (r) {
            case 10:
                cmdline_disable = 1;
                break;
        }
    }

    if (globalreg->kismet_config->FetchOpt("allowplugins") == "true") {
        config_disable = 0;
    } else {
        config_disable = 1;
    }

    if (config_disable || cmdline_disable) {
        plugins_active = 0;
        _MSG(
            "Plugin system disabled by Kismet configuration file or "
            "command line",
            MSGFLAG_INFO);
        return;
    }

    plugins_active = 1;

    plugin_registry.reset(new TrackerElement(TrackerVector));
    plugin_registry_vec = TrackerElementVector(plugin_registry);
}

Plugintracker::~Plugintracker() {
    local_eol_locker lock(&plugin_lock);

    // Call the main shutdown, which should kill the vector allocations
    ShutdownPlugins();
}

void Plugintracker::Usage(char *name __attribute__((unused))) {
    printf(" *** Plugin Options ***\n");
    printf("     --disable-plugins		  Turn off the plugin "
           "system\n");
}

int Plugintracker::ScanPlugins() {
    local_locker lock(&plugin_lock);

    // Bail if plugins disabled
    if (plugins_active == 0) return 0;

    string plugin_path = string(LIB_LOC) + "/kismet/";
    DIR *plugdir;

    if ((plugdir = opendir(plugin_path.c_str())) == NULL) {
        _MSG("Could not open system plugin directory (" + plugin_path +
                 "), skipping: " + strerror(errno), MSGFLAG_INFO);
    } else {
        if (ScanDirectory(plugdir, plugin_path) < 0) return -1;
        closedir(plugdir);
    }

    string config_path;
    if ((config_path = globalreg->kismet_config->FetchOpt("configdir")) == "") {
        _MSG(
            "Failed to find a 'configdir' path in the Kismet config file, "
            "ignoring local plugins.",
            MSGFLAG_INFO);
        return 0;
    }

    plugin_path = globalreg->kismet_config->ExpandLogPath(
        config_path + "/plugins/", "", "", 0, 1);
    if ((plugdir = opendir(plugin_path.c_str())) == NULL) {
        _MSG("Did not find a user plugin directory (" + plugin_path +
                 "), skipping: " + strerror(errno), MSGFLAG_INFO);
    } else {
        if (ScanDirectory(plugdir, plugin_path) < 0) {
            closedir(plugdir);
            return -1;
        }
        closedir(plugdir);
    }

    return 1;
}

// Scans a directory for sub-directories
int Plugintracker::ScanDirectory(DIR *in_dir, string in_path) {
    struct dirent *plugfile;

    while ((plugfile = readdir(in_dir)) != NULL) {
        if (plugfile->d_name[0] == '.') continue;

        struct stat sstat;

        // Is it a directory?
        if (stat(string(in_path + "/" + plugfile->d_name).c_str(), &sstat) < 0)
            continue;

        if (!S_ISDIR(sstat.st_mode))
            continue;

        // Load the plugin manifest
        ConfigFile cf(globalreg);

        string manifest = in_path + "/" + plugfile->d_name + "/manifest.conf";

        cf.ParseConfig(manifest.c_str());

        SharedPluginData preg(new PluginRegistrationData(globalreg, 0));

        preg->set_plugin_path(in_path + "/" + plugfile->d_name + "/");
        preg->set_plugin_dirname(plugfile->d_name);

        string s;

        if ((s = cf.FetchOpt("name")) == "") {
            _MSG("Missing 'name=' in plugin manifest '" + manifest + "', "
                    "cannot load plugin", MSGFLAG_ERROR);
            continue;
        }

        preg->set_plugin_name(s);

        if ((s = cf.FetchOpt("description")) == "") {
            _MSG("Missing 'description=' in plugin manifest '" + manifest + "', "
                    "cannot load plugin", MSGFLAG_ERROR);
            continue;
        }

        preg->set_plugin_description(s);


        if ((s = cf.FetchOpt("author")) == "") {
            _MSG("Missing 'author=' in plugin manifest '" + manifest + "', "
                    "cannot load plugin", MSGFLAG_ERROR);
            continue;
        }

        preg->set_plugin_author(s);


        if ((s = cf.FetchOpt("version")) == "") {
            _MSG("Missing 'version=' in plugin manifest '" + manifest + "', "
                    "cannot load plugin", MSGFLAG_ERROR);
            continue;
        }

        preg->set_plugin_version(s);


        if ((s = cf.FetchOpt("object")) != "") {
            if (s.find("/") != string::npos) {
                _MSG("Found path in 'object=' in plugin manifest '" + manifest +
                        "', object= should define the file name only", MSGFLAG_ERROR);
                continue;
            }

            preg->set_plugin_so(s);
        }

        if ((s = cf.FetchOpt("js")) != "") {
            if (s.find(",") == string::npos) {
                _MSG("Found an invalid 'js=' in plugin manifest '" + manifest +
                        "', js= should define module,path", MSGFLAG_ERROR);
                continue;
            }

            preg->set_plugin_js(s);
        }

        // Make sure we haven't already loaded another copy of the plugin
        // based on the so or the pluginname
        for (auto x : plugin_preload) {
            // Don't load the same plugin
            if (preg->get_plugin_so() != "" &&
                    x->get_plugin_so() == preg->get_plugin_so()) {
                continue;
            }

            if (x->get_plugin_name() == preg->get_plugin_name()) {
                continue;
            }
        }

        // We've gotten to here, it's valid, push it into the preload vector
        plugin_preload.push_back(preg);
    }

    return 1;
}

// Catch plugin failures so we can alert the user
string global_plugin_load;
void PluginServerSignalHandler(int sig __attribute__((unused))) {
    fprintf(stderr,
            "\n\n"
            "FATAL: Kismet crashed while loading a plugin...\n"
            "Plugin loading: %s\n\n"
            "This is either a bug in the plugin, or the plugin needs "
            "to be recompiled\n"
            "to match the version of Kismet you are using (especially "
            "if you are using\n"
            "development/git versions of Kismet or have recently "
            "upgraded.)\n\n"
            "Remove the plugin from the plugins directory, or start "
            "Kismet with \n"
            "plugins disabled (--no-plugins)\n\n",
            global_plugin_load.c_str());
    exit(1);
}

int Plugintracker::ActivatePlugins() {
#ifdef SYS_CYGWIN
    _sig_func_ptr old_segv = SIG_DFL;
#else
    sig_t old_segv = SIG_DFL;
#endif

    local_locker lock(&plugin_lock);

    shared_ptr<Kis_Httpd_Registry> httpdregistry =
        Globalreg::FetchGlobalAs<Kis_Httpd_Registry>(globalreg, "WEBREGISTRY");

    // Set the new signal handler, remember the old one; if something goes
    // wrong loading the plugins we need to catch it and return a special
    // error
    old_segv = signal(SIGSEGV, PluginServerSignalHandler);

    for (auto x : plugin_preload) {
        // Does this plugin load a SO?
        if (x->get_plugin_so() != "") {
            global_plugin_load = x->get_plugin_path() + "/" + x->get_plugin_so();

            void *dlfile = dlopen(global_plugin_load.c_str(), RTLD_LAZY);

            if (dlfile == NULL) {
                _MSG("Failed to open plugin '" + x->get_plugin_path() +
                        "' as a shared library: " + kis_strerror_r(errno),
                        MSGFLAG_ERROR);
                continue;
            }

            x->set_plugin_dlfile(dlfile);

            // Find the symbol for kis_plugin_version_check
            plugin_version_check vcheck_sym = 
                (plugin_version_check) dlsym(dlfile, "kis_plugin_version_check");

            if (vcheck_sym == NULL) {
                _MSG("Failed to get plugin version check function from plugin '" +
                        x->get_plugin_path() +
                        "': Ensure that all plugins have "
                        "been recompiled for the proper version of Kismet, "
                        "especially if you're using a development or git version "
                        "of Kismet.", MSGFLAG_ERROR);
                continue;
            }

            struct plugin_server_info sinfo;
            sinfo.plugin_api_version = KIS_PLUGINTRACKER_VERSION;

            if ((*vcheck_sym)(&sinfo) < 0) {
                _MSG("Plugin '" + x->get_plugin_path() +
                        "' could not perform "
                        "a version check.  Ensure that all plugins have been "
                        "recompiled for the proper version of Kismet, especially "
                        "if you're using a development or git version of Kismet.",
                        MSGFLAG_ERROR);
                continue;
            }

            if (sinfo.plugin_api_version != KIS_PLUGINTRACKER_VERSION ||
                    sinfo.kismet_major != globalreg->version_major ||
                    sinfo.kismet_minor != globalreg->version_minor ||
                    sinfo.kismet_tiny != globalreg->version_tiny) {
                _MSG("Plugin '" + x->get_plugin_path() +
                        "' was compiled "
                        "with a different version of Kismet; Please recompile "
                        "the plugin and re-install it, or remove it entirely.",
                        MSGFLAG_ERROR);
                continue;
            }

            plugin_activation act_sym =
                (plugin_activation) dlsym(dlfile, "kis_plugin_activate");

            if (act_sym == NULL) {
                _MSG("Failed to get plugin registration function from plugin '" +
                        x->get_plugin_path() +
                        "': Ensure that all plugins have "
                        "been recompiled for the proper version of Kismet, "
                        "especially if you're using a development or git version "
                        "of Kismet.", MSGFLAG_ERROR);
                continue;
            }

            if ((act_sym)(globalreg) < 0) {
                _MSG("Plugin '" + x->get_plugin_path() + "' failed to activate, "
                        "skipping.", MSGFLAG_ERROR);
                continue;
            }
        }

        // If we have a JS module, load it
        if (x->get_plugin_js() != "") {
            string js = x->get_plugin_js();
            size_t cpos = js.find(",");

            if (cpos == string::npos || cpos >= js.length() - 2) {
                _MSG("Plugin '" + x->get_plugin_path() + "' could not parse "
                        "JS plugin module, expected modulename,path",
                        MSGFLAG_ERROR);
                continue;
            }

            string module = js.substr(0, cpos);
            string path = js.substr(cpos + 1, js.length());

            if (!httpdregistry->register_js_module(module, path)) {
                _MSG("Plugin '" + x->get_plugin_path() + "' could not "
                        "register JS plugin module", MSGFLAG_ERROR);
                continue;
            }
        }

        // Alias the plugin directory
        httpd->RegisterStaticDir("/plugin/" + x->get_plugin_dirname() + "/",
                x->get_plugin_path() + "/httpd/");

        _MSG("Plugin '" + x->get_plugin_name() + "' loaded...", MSGFLAG_INFO);

        plugin_registry_vec.push_back(x);
    }

    // Reset the segv handler
    signal(SIGSEGV, old_segv);

    plugin_preload.clear();

    return 1;
}

int Plugintracker::FinalizePlugins() {
    // Look only at plugins that have a dl file, and attempt to run the finalize
    // function in each
    for (auto x : plugin_registry_vec) {
        SharedPluginData pd = dynamic_pointer_cast<PluginRegistrationData>(x);

        void *dlfile;

        if ((dlfile = pd->get_plugin_dlfile()) != NULL) {
            plugin_activation final_sym = 
                (plugin_activation) dlsym(dlfile, "kis_plugin_finalize");

            if (final_sym == NULL)
                continue;

            if ((final_sym)(globalreg) < 0) {
                _MSG("Plugin '" + pd->get_plugin_path() + "' failed to complete "
                        "activation...", MSGFLAG_ERROR);
                continue;
            }
        }
    }
    
    return 1;
}

int Plugintracker::ShutdownPlugins() {
    local_locker lock(&plugin_lock);

    _MSG("Shutting down plugins...", MSGFLAG_INFO);

    plugin_registry_vec.clear();
    plugin_preload.clear();

    return 0;
}

bool Plugintracker::Httpd_VerifyPath(const char *path, const char *method) {
    if (strcmp(method, "GET") != 0) 
        return false;

    if (!Httpd_CanSerialize(path))
        return false;

    string stripped = Httpd_StripSuffix(path);

    if (stripped == "/plugins/all_plugins")
        return true;

    return false;
}

void Plugintracker::Httpd_CreateStreamResponse(Kis_Net_Httpd *httpd,
        Kis_Net_Httpd_Connection *connection,
        const char *path, const char *method, const char *upload_data,
        size_t *upload_data_size, std::stringstream &stream) {

    string stripped = Httpd_StripSuffix(path);

    if (stripped == "/plugins/all_plugins") {
        local_locker locker(&plugin_lock);

        Httpd_Serialize(path, stream, plugin_registry);
    }
}
