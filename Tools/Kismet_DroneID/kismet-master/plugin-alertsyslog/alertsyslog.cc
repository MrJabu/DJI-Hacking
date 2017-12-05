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

#include <config.h>
#include <string>
#include <errno.h>
#include <time.h>

#include <pthread.h>

#include <sstream>
#include <iomanip>
#include <syslog.h>

#include <util.h>
#include <messagebus.h>
#include <packet.h>
#include <packetchain.h>
#include <timetracker.h>
#include <configfile.h>
#include <plugintracker.h>
#include <globalregistry.h>
#include <alertracker.h>
#include <version.h>

GlobalRegistry *globalreg = NULL;

int alertsyslog_chain_hook(CHAINCALL_PARMS) {
	kis_alert_component *alrtinfo = NULL;

	if (in_pack->error)
		return 0;

	// Grab the alerts
	alrtinfo = (kis_alert_component *) in_pack->fetch(_PCM(PACK_COMP_ALERT));

	if (alrtinfo == NULL)
		return 0;

	for (unsigned int x = 0; x < alrtinfo->alert_vec.size(); x++) {
		syslog(LOG_CRIT, "%s server-ts=%u bssid=%s source=%s dest=%s channel=%s %s",
			   alrtinfo->alert_vec[x]->header.c_str(),
			   (unsigned int) alrtinfo->alert_vec[x]->tm.tv_sec,
			   alrtinfo->alert_vec[x]->bssid.Mac2String().c_str(),
			   alrtinfo->alert_vec[x]->source.Mac2String().c_str(),
			   alrtinfo->alert_vec[x]->dest.Mac2String().c_str(),
			   alrtinfo->alert_vec[x]->channel.c_str(),
			   alrtinfo->alert_vec[x]->text.c_str());
	}

	return 1;
}

int alertsyslog_openlog(GlobalRegistry *in_globalreg) {
    // We can't use the templated FetchGlobalAs here because the template object code
    // won't exist in the server object
    shared_ptr<Packetchain> packetchain =
        static_pointer_cast<Packetchain>(in_globalreg->FetchGlobal(string("PACKETCHAIN")));
    shared_ptr<void> pcv = in_globalreg->FetchGlobal(string("PACKETCHAIN"));

    if (packetchain == NULL) {
        _MSG("Unable to register syslog plugin, packetchain was unavailable",
                MSGFLAG_ERROR);
        return -1;
    }

    openlog(in_globalreg->servername.c_str(), LOG_NDELAY, LOG_USER);

    packetchain->RegisterHandler(&alertsyslog_chain_hook, NULL,
            CHAINPOS_LOGGING, -100);

    return 1;
}

extern "C" {
    int kis_plugin_version_check(struct plugin_server_info *si) {
        si->plugin_api_version = KIS_PLUGINTRACKER_VERSION;
        si->kismet_major = VERSION_MAJOR;
        si->kismet_minor = VERSION_MINOR;
        si->kismet_tiny = VERSION_TINY;

        return 1;
    }

    int kis_plugin_activate(GlobalRegistry *in_globalreg) {
        return 1;
    }

    int kis_plugin_finalize(GlobalRegistry *in_globalreg) {
        return alertsyslog_openlog(in_globalreg);
    }

}

