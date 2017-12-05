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
#include <list>
#include <map>
#include <vector>
#include <algorithm>
#include <string>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <iostream>
#include <limits.h>

#ifdef HAVE_LIBPCRE
#include <pcre.h>
#endif

#include "globalregistry.h"
#include "packetchain.h"
#include "timetracker.h"
#include "filtercore.h"
#include "gpstracker.h"
#include "packet.h"
#include "uuid.h"
#include "alertracker.h"
#include "manuf.h"
#include "configfile.h"

#include "base64.h"

#include "devicetracker.h"
#include "phy_80211.h"

#include "structured.h"
#include "msgpack_adapter.h"
#include "kismet_json.h"

#include "kis_httpd_registry.h"

#ifdef HAVE_LIBPCRE
#include <pcre.h>
#endif

extern "C" {
#ifndef HAVE_PCAPPCAP_H
#include <pcap.h>
#else
#include <pcap/pcap.h>
#endif
}

// Convert the beacon interval to # of packets per second
unsigned int Ieee80211Interval2NSecs(int in_interval) {
	double interval_per_sec;

	interval_per_sec = (double) in_interval * 1024 / 1000000;
	
	return (unsigned int) ceil(1.0f / interval_per_sec);
}

void dot11_tracked_eapol::register_fields() {
    tracker_component::register_fields();

    RegisterField("dot11.eapol.timestamp", TrackerDouble, 
            "packet timestamp (second.usecond)", &eapol_time);
    
    RegisterField("dot11.eapol.direction", TrackerUInt8,
            "packet direction (fromds/tods)", &eapol_dir);

    RegisterField("dot11.eapol.message_num", TrackerUInt8,
            "handshake message number", &eapol_msg_num);

    RegisterField("dot11.eapol.replay_counter", TrackerUInt64,
            "eapol frame replay counter", &eapol_replay_counter);

    RegisterField("dot11.eapol.install", TrackerUInt8,
            "eapol rsn key install", &eapol_install);

    RegisterField("dot11.eapol.nonce", TrackerByteArray,
            "eapol rsn nonce", &eapol_nonce);

    __RegisterComplexField(kis_tracked_packet, eapol_packet_id,
            "dot11.eapol.packet", "EAPOL handshake");
}

void dot11_tracked_eapol::reserve_fields(SharedTrackerElement e) {
    tracker_component::reserve_fields(e);

    if (e != NULL) {
        eapol_packet.reset(new kis_tracked_packet(globalreg, eapol_packet_id,
                    e->get_map_value(eapol_packet_id)));
    } else {
        eapol_packet.reset(new kis_tracked_packet(globalreg, eapol_packet_id));
    }

    add_map(eapol_packet);
}

void dot11_tracked_ssid_alert::register_fields() {
    tracker_component::register_fields();

    RegisterField("dot11.ssidalert.name", TrackerString,
            "Unique name of alert group", &ssid_group_name);
    RegisterField("dot11.ssidalert.regex", TrackerString,
            "Matching regex for SSID", &ssid_regex);
    RegisterField("dot11.ssidalert.allowed_macs", TrackerVector,
            "Allowed MAC addresses", &allowed_macs_vec);

    allowed_mac_id =
        RegisterField("dot11.ssidalert.allowed_mac", TrackerMac,
                "mac address");

}

void dot11_tracked_ssid_alert::set_regex(std::string s) {
#ifdef HAVE_LIBPCRE
    local_locker lock(&ssid_mutex);

    const char *compile_error, *study_error;
    int erroroffset;
    std::ostringstream errordesc;

    if (ssid_re)
        pcre_free(ssid_re);
    if (ssid_study)
        pcre_free(ssid_study);

    ssid_regex->set(s);

    ssid_re = pcre_compile(s.c_str(), 0, &compile_error, &erroroffset, NULL);

    if (ssid_re == NULL) {
        errordesc << "Could not parse PCRE: " << compile_error << 
            "at character " << erroroffset;
        throw std::runtime_error(errordesc.str());
    }

    ssid_study = pcre_study(ssid_re, 0, &study_error);

    if (ssid_study == NULL) {
        errordesc << "Could not parse PCRE, optimization failure: " << study_error;
        throw std::runtime_error(errordesc.str());
    } 
#endif
}

void dot11_tracked_ssid_alert::set_allowed_macs(std::vector<mac_addr> mvec) {
    local_locker lock(&ssid_mutex);

    TrackerElementVector v(allowed_macs_vec);

    v.clear();

    for (auto i : mvec) {
        SharedTrackerElement e(new TrackerElement(TrackerMac, allowed_mac_id));
        e->set(i);

        v.push_back(e);
    }
}

bool dot11_tracked_ssid_alert::compare_ssid(std::string ssid, mac_addr mac) {
    local_locker lock(&ssid_mutex);

    int rc;
    int ovector[128];

    rc = pcre_exec(ssid_re, ssid_study, ssid.c_str(), ssid.length(), 0, 0, ovector, 128);

    if (rc > 0) {
        TrackerElementVector v(allowed_macs_vec);

        // Look for a mac address which isn't in the allowed list
        for (auto m : v) {
            if (m->get_mac() != mac)
                return true;
        }
    }

    return false;

}

void dot11_tracked_nonce::register_fields() {
    tracker_component::register_fields();

    RegisterField("dot11.eapol.nonce.timestamp", TrackerDouble, 
            "packet timestamp (second.usecond)", &eapol_time);
    
    RegisterField("dot11.eapol.nonce.message_num", TrackerUInt8,
            "handshake message number", &eapol_msg_num);

    RegisterField("dot11.eapol.nonce.replay_counter", TrackerUInt64,
            "eapol frame replay counter", &eapol_replay_counter);

    RegisterField("dot11.eapol.nonce.install", TrackerUInt8,
            "eapol rsn key install", &eapol_install);

    RegisterField("dot11.eapol.nonce.nonce", TrackerByteArray,
            "eapol rsn nonce", &eapol_nonce);
}

void dot11_tracked_nonce::reserve_fields(SharedTrackerElement e) {
    tracker_component::reserve_fields(e);
}

void dot11_tracked_nonce::set_from_eapol(SharedTrackerElement in_tracked_eapol) {
    std::shared_ptr<dot11_tracked_eapol> e =
        std::static_pointer_cast<dot11_tracked_eapol>(in_tracked_eapol);

    set_eapol_time(e->get_eapol_time());
    set_eapol_msg_num(e->get_eapol_msg_num());
    set_eapol_replay_counter(e->get_eapol_replay_counter());


    set_eapol_install(e->get_eapol_install());
    set_eapol_nonce(e->get_eapol_nonce());
}

int phydot11_packethook_wep(CHAINCALL_PARMS) {
	return ((Kis_80211_Phy *) auxdata)->PacketWepDecryptor(in_pack);
}

int phydot11_packethook_dot11(CHAINCALL_PARMS) {
	return ((Kis_80211_Phy *) auxdata)->PacketDot11dissector(in_pack);
}

int phydot11_packethook_dot11tracker(CHAINCALL_PARMS) {
	return ((Kis_80211_Phy *) auxdata)->TrackerDot11(in_pack);
}

Kis_80211_Phy::Kis_80211_Phy(GlobalRegistry *in_globalreg, 
		Devicetracker *in_tracker, int in_phyid) : 
	Kis_Phy_Handler(in_globalreg, in_tracker, in_phyid),
    Kis_Net_Httpd_CPPStream_Handler(in_globalreg) {

    alertracker =
        Globalreg::FetchGlobalAs<Alertracker>(globalreg, "ALERTTRACKER");

    packetchain =
        Globalreg::FetchGlobalAs<Packetchain>(globalreg, "PACKETCHAIN");

    timetracker =
        Globalreg::FetchGlobalAs<Timetracker>(globalreg, "TIMETRACKER");

	// Initialize the crc tables
	crc32_init_table_80211(globalreg->crc32_table);

    SetPhyName("IEEE802.11");

    shared_ptr<dot11_tracked_device> dot11_builder(new dot11_tracked_device(globalreg, 0));
    dot11_device_entry_id =
        entrytracker->RegisterField("dot11.device", dot11_builder, 
                "IEEE802.11 device");

	// Packet classifier - makes basic records plus dot11 data
	packetchain->RegisterHandler(&CommonClassifierDot11, this,
            CHAINPOS_CLASSIFIER, -100);

	packetchain->RegisterHandler(&phydot11_packethook_wep, this,
            CHAINPOS_DECRYPT, -100);
	packetchain->RegisterHandler(&phydot11_packethook_dot11, this,
            CHAINPOS_LLCDISSECT, -100);

	packetchain->RegisterHandler(&phydot11_packethook_dot11tracker, this,
											CHAINPOS_TRACKER, 100);

	// If we haven't registered packet components yet, do so.  We have to
	// co-exist with the old tracker core for some time
	pack_comp_80211 = _PCM(PACK_COMP_80211) =
		packetchain->RegisterPacketComponent("PHY80211");

	pack_comp_basicdata = 
		packetchain->RegisterPacketComponent("BASICDATA");

	pack_comp_mangleframe = 
		packetchain->RegisterPacketComponent("MANGLEDATA");

	pack_comp_checksum =
		packetchain->RegisterPacketComponent("CHECKSUM");

	pack_comp_linkframe = 
		packetchain->RegisterPacketComponent("LINKFRAME");

	pack_comp_decap =
		packetchain->RegisterPacketComponent("DECAP");

	pack_comp_common = 
		packetchain->RegisterPacketComponent("COMMON");

	pack_comp_datapayload =
		packetchain->RegisterPacketComponent("DATAPAYLOAD");

	pack_comp_gps =
		packetchain->RegisterPacketComponent("GPS");

    pack_comp_l1info =
        packetchain->RegisterPacketComponent("RADIODATA");

    ssid_regex_vec =
        entrytracker->RegisterAndGetField("phy80211.ssid_alerts", TrackerVector,
                "Regex SSID alert configuration");
    ssid_regex_vec_element_id =
        entrytracker->RegisterField("phy80211.ssid_alert", 
                shared_ptr<dot11_tracked_ssid_alert>(new dot11_tracked_ssid_alert(globalreg, 0)),
                "ssid alert");

	// Register the dissector alerts
	alert_netstumbler_ref = 
		alertracker->ActivateConfiguredAlert("NETSTUMBLER", 
                "Netstumbler (and similar older Windows tools) may generate unique "
                "beacons which can be used to identify these tools in use.  These "
                "tools and the cards which generate these frames are uncommon.",
                phyid);
	alert_nullproberesp_ref =
		alertracker->ActivateConfiguredAlert("NULLPROBERESP", 
                "A probe response with a SSID length of 0 can be used to crash the "
                "firmware in specific older Orinoco cards.  These cards are "
                "unlikely to be in use in modern systems.",
                phyid);
	alert_lucenttest_ref =
		alertracker->ActivateConfiguredAlert("LUCENTTEST", 
                "Specific Lucent Orinoco test tools generate identifiable frames, "
                "which can indicate these tools are in use.  These tools and the "
                "cards which generate these frames are uncommon.",
                phyid);
	alert_msfbcomssid_ref =
		alertracker->ActivateConfiguredAlert("MSFBCOMSSID", 
                "Old versions of the Broadcom Windows drivers (and Linux NDIS drivers) "
                "are vulnerable to overflow exploits.  The Metasploit framework "
                "can attack these vulnerabilities.  These drivers are unlikely to "
                "be found in modern systems, but seeing these malformed frames "
                "indicates an attempted attack is occurring.",
                phyid);
	alert_msfdlinkrate_ref =
		alertracker->ActivateConfiguredAlert("MSFDLINKRATE", 
                "Old versions of the D-Link Windows drivers are vulnerable to "
                "malformed rate fields.  The Metasploit framework can attack these "
                "vulnerabilities.  These drivers are unlikely to be found in "
                "modern systems, but seeing these malformed frames indicates an "
                "attempted attack is occurring.",
                phyid);
	alert_msfnetgearbeacon_ref =
		alertracker->ActivateConfiguredAlert("MSFNETGEARBEACON", 
                "Old versions of the Netgear windows drivers are vulnerable to "
                "malformed beacons.  The Metasploit framework can attack these "
                "vulnerabilities.  These drivers are unlikely to be found in "
                "modern systems, but seeing these malformed frames indicates an "
                "attempted attack is occurring.",
                phyid);
	alert_longssid_ref =
		alertracker->ActivateConfiguredAlert("LONGSSID", 
                "The Wi-Fi standard allows for 32 characters in a SSID. "
                "Historically, some drivers have had vulnerabilities related to "
                "invalid over-long SSID fields.  Seeing these frames indicates that "
                "significant corruption or an attempted attack is occurring.",
                phyid);
	alert_disconinvalid_ref =
		alertracker->ActivateConfiguredAlert("DISCONCODEINVALID", 
                "The 802.11 specification defines reason codes for disconnect "
                "and deauthentication events.  Historically, various drivers "
                "have been reported to improperly handle invalid reason codes.  "
                "An invalid reason code indicates an improperly behaving device or "
                "an attempted attack.",
                phyid);
	alert_deauthinvalid_ref =
		alertracker->ActivateConfiguredAlert("DEAUTHCODEINVALID", 
                "The 802.11 specification defines reason codes for disconnect "
                "and deauthentication events.  Historically, various drivers "
                "have been reported to improperly handle invalid reason codes.  "
                "An invalid reason code indicates an improperly behaving device or "
                "an attempted attack.",
                phyid);
    alert_wmm_ref =
        alertracker->ActivateConfiguredAlert("WMMOVERFLOW",
                "The Wi-Fi standard specifies 24 bytes for WMM IE tags.  Over-sized "
                "WMM fields may indicate an attempt to exploit bugs in Broadcom chipsets "
                "using the Broadpwn attack",
                phyid);
#if 0
	alert_dhcpclient_ref =
		alertracker->ActivateConfiguredAlert("DHCPCLIENTID", phyid);
#endif

	// Register the tracker alerts
	alert_chan_ref =
		alertracker->ActivateConfiguredAlert("CHANCHANGE", 
                "An access point has changed channel.  This may occur on "
                "enterprise equipment or on personal equipment with automatic "
                "channel selection, but may also indicate a spoofed or "
                "'evil twin' network.",
                phyid);
	alert_dhcpcon_ref =
		alertracker->ActivateConfiguredAlert("DHCPCONFLICT", 
                "A DHCP exchange was observed and a client was given an IP via "
                "DHCP, but is not using the assigned IP.  This may be a "
                "mis-configured client device, or may indicate client spoofing.",
                phyid);
	alert_bcastdcon_ref =
		alertracker->ActivateConfiguredAlert("BCASTDISCON", 
                "A broadcast disconnect packet forces all clients on a network "
                "to disconnect.  While these may rarely occur in some environments, "
                "typically a broadcast disconnect indicates a denial of service "
                "attack or an attempt to attack the network encryption by forcing "
                "clients to reconnect.",
                phyid);
	alert_airjackssid_ref = 
		alertracker->ActivateConfiguredAlert("AIRJACKSSID", 
                "Very old wireless tools used the SSID 'Airjack' while configuring "
                "card state.  It is very unlikely to see these tools in operation "
                "in modern environments.",
                phyid);
	alert_wepflap_ref =
		alertracker->ActivateConfiguredAlert("CRYPTODROP", 
                "A previously encrypted SSID has stopped advertising encryption.  "
                "This may rarely occur when a network is reconfigured to an open "
                "state, but more likely indicates some form of network spoofing or "
                "'evil twin' attack.",
                phyid);
	alert_dhcpname_ref =
		alertracker->ActivateConfiguredAlert("DHCPNAMECHANGE", 
                "The DHCP protocol allows clients to put the host name and "
                "DHCP client / vendor / operating system details in the DHCP "
                "Discovery packet.  These values should old change if the client "
                "has changed drastically (such as a dual-boot system with multiple "
                "operating systems).  Changing values can often indicate a client "
                "spoofing or MAC cloning attempt.",
                phyid);
	alert_dhcpos_ref =
		alertracker->ActivateConfiguredAlert("DHCPOSCHANGE", 
                "The DHCP protocol allows clients to put the host name and "
                "DHCP client / vendor / operating system details in the DHCP "
                "Discovery packet.  These values should old change if the client "
                "has changed drastically (such as a dual-boot system with multiple "
                "operating systems).  Changing values can often indicate a client "
                "spoofing or MAC cloning attempt.",
                phyid);
	alert_adhoc_ref =
		alertracker->ActivateConfiguredAlert("ADHOCCONFLICT", 
                "The same SSID is being advertised as an access point and as an "
                "ad-hoc network.  This may indicate a misconfigured or misbehaving "
                "device, or could indicate an attempt at spoofing or an 'evil twin' "
                "attack.",
                phyid);
	alert_ssidmatch_ref =
		alertracker->ActivateConfiguredAlert("APSPOOF", 
                "Kismet may be given a list of authorized MAC addresses for "
                "a SSID.  If a beacon or probe response is seen from a MAC address "
                "not listed in the authorized list, this alert will be raised.",
                phyid);
	alert_dot11d_ref =
		alertracker->ActivateConfiguredAlert("DOT11D", 
                "Conflicting 802.11d (country code) data has been advertised by the "
                "same SSID.  It is unlikely this is a normal configuration change, "
                "and can indicate a spoofed or 'evil twin' network, or an attempt "
                "to perform a denial of service on clients by restricting their "
                "frequencies.  802.11d has been phased out and is unlikely to be "
                "seen on modern devices, but it is still supported by many systems.",
                phyid);
	alert_beaconrate_ref =
		alertracker->ActivateConfiguredAlert("BEACONRATE", 
                "The advertised beacon rate of a SSID has changed.  In an "
                "enterprise or multi-SSID environment this may indicate a normal "
                "configuration change, but can also indicate a spoofed or "
                "'evil twin' network.",
                phyid);
	alert_cryptchange_ref =
		alertracker->ActivateConfiguredAlert("ADVCRYPTCHANGE", 
                "A SSID has changed the advertised supported encryption standards.  "
                "This may be a normal change when reconfiguring an access point, "
                "but can also indicate a spoofed or 'evil twin' attack.",
                phyid);
	alert_malformmgmt_ref =
		alertracker->ActivateConfiguredAlert("MALFORMMGMT", 
                "Malformed management frames may indicate errors in the capture "
                "source driver (such as not discarding corrupted packets), but can "
                "also be indicative of an attempted attack against drivers which may "
                "not properly handle malformed frames.",
                phyid);
	alert_wpsbrute_ref =
		alertracker->ActivateConfiguredAlert("WPSBRUTE", 
                "Excessive WPS events may indicate a malformed client, or an "
                "attack on the WPS system by a tool such as Reaver.",
                phyid);
    alert_l33t_ref = 
        alertracker->ActivateConfiguredAlert("KARMAOUI",
                "Probe responses from MAC addresses with an OUI of 00:13:37 often "
                "indicate an Karma AP impersonation attack.",
                phyid);
    alert_tooloud_ref =
        alertracker->ActivateConfiguredAlert("OVERPOWERED",
                "Signal levels are abnormally high, when using an external amplifier "
                "this could indicate that the gain is too high.  Over-amplified signals "
                "may miss packets entirely.",
                phyid);
    alert_nonce_zero_ref =
        alertracker->ActivateConfiguredAlert("NONCEDEGRADE",
                "A WPA handshake with an empty NONCE was observed; this could indicate "
                "a WPA degradation attack such as the vanhoefm attack against BSD "
                "(https://github.com/vanhoefm/blackhat17-pocs/tree/master/openbsd)",
                phyid);
    alert_nonce_duplicate_ref =
        alertracker->ActivateConfiguredAlert("NONCEREUSE",
                "A WPA handshake has attempted to re-use a previous nonce value; this may "
                "indicate an attack against the WPA keystream such as the vanhoefm "
                "KRACK attack (https://www.krackattacks.com/)");
    alert_atheros_wmmtspec_ref =
        alertracker->ActivateConfiguredAlert("WMMTSPEC",
                "Too many WMMTSPEC options were seen in a probe response; this "
                "may be triggered by CVE-2017-11013 as described at "
                "https://pleasestopnamingvulnerabilities.com/");
    alert_atheros_rsnloop_ref =
        alertracker->ActivateConfiguredAlert("RSNLOOP",
                "Invalid RSN (802.11i) tags in beacon frames can be used to cause "
                "loops in some Atheros drivers, as described in "
                "CVE-2017-9714 and https://pleasestopnamingvulnerabilities.com/");
    alert_11kneighborchan_ref =
        alertracker->ActivateConfiguredAlert("BCOM11KCHAN",
                "Invalid channels in 802.11k neighbor report frames "
                "can be used to exploit certain Broadcom HardMAC implementations, typically used "
                "in mobile devices, as described in "
                "https://bugs.chromium.org/p/project-zero/issues/detail?id=1289");

    // Threshold
    signal_too_loud_threshold = 
        globalreg->kismet_config->FetchOptInt("dot11_max_signal", -10);

	// Do we process the whole data packet?
    if (globalreg->kismet_config->FetchOptBoolean("hidedata", 0) ||
		globalreg->kismet_config->FetchOptBoolean("dontbeevil", 0)) {
		_MSG("hidedata= set in Kismet config.  Kismet will ignore the contents "
			 "of data packets entirely", MSGFLAG_INFO);
		dissect_data = 0;
	} else {
		dissect_data = 1;
	}

    // Do we process phy and control frames?  They seem to be the glitchiest
    // on many cards including the ath9k which is otherwise excellent
    if (globalreg->kismet_config->FetchOptBoolean("dot11_process_phy", 0)) {
        _MSG("PHY802.11 will process Wi-Fi 'phy' and 'control' type frames, which "
                "gives the most complete view of device traffic but may result in "
                "false devices due to driver and firmware quirks.", MSGFLAG_INFO);
        process_ctl_phy = true;
    } else {
        _MSG("PHY802.11 will not process Wi-Fi 'phy' and 'control' frames; these "
                "typically are the most susceptible to corruption resulting in "
                "false devices.  This can be re-enabled with dot11_process_phy=true",
                MSGFLAG_INFO);
        process_ctl_phy = false;
    }

	dissect_strings = 0;
	dissect_all_strings = 0;

	// Load the wep keys from the config file
	if (LoadWepkeys() < 0) {
		globalreg->fatal_condition = 1;
		return;
	}

    // TODO turn into REST endpoint
    if (globalreg->kismet_config->FetchOptBoolean("allowkeytransmit", 0)) {
        _MSG("Allowing Kismet clients to view WEP keys", MSGFLAG_INFO);
        client_wepkey_allowed = 1;
    } else {
		client_wepkey_allowed = 0;
	}

	// Build the wep identity
	for (unsigned int wi = 0; wi < 256; wi++)
		wep_identity[wi] = wi;

	string_filter = new FilterCore(globalreg);
	vector<string> filterlines = 
		globalreg->kismet_config->FetchOptVec("filter_string");
	for (unsigned int fl = 0; fl < filterlines.size(); fl++) {
		if (string_filter->AddFilterLine(filterlines[fl]) < 0) {
			_MSG("Failed to add filter_string config line from the Kismet config "
				 "file.", MSGFLAG_FATAL);
			globalreg->fatal_condition = 1;
			return;
		}
	}

    // Set up the device timeout
    device_idle_expiration =
        globalreg->kismet_config->FetchOptInt("tracker_device_timeout", 0);

    if (device_idle_expiration != 0) {
        stringstream ss;
        ss << "Removing dot11 device info which has been inactive for "
            "more than " << device_idle_expiration << " seconds.";
        _MSG(ss.str(), MSGFLAG_INFO);

        device_idle_timer =
            timetracker->RegisterTimer(SERVER_TIMESLICES_SEC * 60, NULL, 
                1, this);
    } else {
        device_idle_timer = -1;
    }

	conf_save = globalreg->timestamp.tv_sec;

	ssid_conf = new ConfigFile(globalreg);
	ssid_conf->ParseConfig(ssid_conf->ExpandLogPath(globalreg->kismet_config->FetchOpt("configdir") + "/" + "ssid_map.conf", "", "", 0, 1).c_str());
	globalreg->InsertGlobal("SSID_CONF_FILE", shared_ptr<ConfigFile>(ssid_conf));

    httpd_pcap.reset(new Phy_80211_Httpd_Pcap(globalreg));

    // Register js module for UI
    shared_ptr<Kis_Httpd_Registry> httpregistry = 
        Globalreg::FetchGlobalAs<Kis_Httpd_Registry>(globalreg, "WEBREGISTRY");
    httpregistry->register_js_module("kismet_ui_dot11", "/js/kismet.ui.dot11.js");

    // Set up the de-duplication list
    recent_packet_checksums_sz = 
        globalreg->kismet_config->FetchOptUInt("packet_dedup_size", 2048);
    recent_packet_checksums = new uint32_t[recent_packet_checksums_sz];
    for (unsigned int x = 0; x < recent_packet_checksums_sz; x++) {
        recent_packet_checksums[x] = 0;
    }
    recent_packet_checksum_pos = 0;

    // Parse the ssid regex options
    auto apspoof_lines = globalreg->kismet_config->FetchOptVec("apspoof");

    TrackerElementVector apv(ssid_regex_vec);

    for (auto l : apspoof_lines) {
        size_t cpos = l.find(':');
        
        if (cpos == std::string::npos) {
            _MSG("Invalid 'apspoof' configuration line, expected 'name:ssid=\"...\","  
                    "validmacs=\"...\" but got '" + l + "'", MSGFLAG_ERROR);
            continue;
        }

        std::string name = l.substr(0, cpos);

        std::vector<opt_pair> optvec;
        StringToOpts(l.substr(cpos + 1, l.length()), ",", &optvec);

        std::string ssid = FetchOpt("ssid", &optvec);

        if (ssid == "") {
            _MSG("Invalid 'apspoof' configuration line, expected 'name:ssid=\"...\","  
                    "validmacs=\"...\" but got '" + l + "'", MSGFLAG_ERROR);
            continue;
        }

        std::vector<mac_addr> macvec;
        for (auto m : StrTokenize(FetchOpt("validmacs", &optvec), ",", true)) {
            mac_addr ma(m);

            if (ma.error) {
                macvec.clear();
                break;
            }

            macvec.push_back(ma);
        }

        if (macvec.size() == 0) {
            _MSG("Invalid 'apspoof' configuration line, expected 'name:ssid=\"...\","  
                    "validmacs=\"...\" but got '" + l + "'", MSGFLAG_ERROR);
            continue;
        }

        shared_ptr<dot11_tracked_ssid_alert> 
            ssida(new dot11_tracked_ssid_alert(globalreg, ssid_regex_vec_element_id));

        try {
            ssida->set_group_name(name);
            ssida->set_regex(ssid);
            ssida->set_allowed_macs(macvec);
        } catch (std::runtime_error &e) {
            _MSG("Invalid 'apspoof' configuration line '" + l + "': " + e.what(),
                    MSGFLAG_ERROR);
            continue;
        }

        apv.push_back(ssida);

    }

}

Kis_80211_Phy::~Kis_80211_Phy() {
	packetchain->RemoveHandler(&phydot11_packethook_wep, CHAINPOS_DECRYPT);
	packetchain->RemoveHandler(&phydot11_packethook_dot11, 
										  CHAINPOS_LLCDISSECT);
	/*
	globalreg->packetchain->RemoveHandler(&phydot11_packethook_dot11data, 
										  CHAINPOS_DATADISSECT);
	globalreg->packetchain->RemoveHandler(&phydot11_packethook_dot11string,
										  CHAINPOS_DATADISSECT);
										  */
	packetchain->RemoveHandler(&CommonClassifierDot11,
            CHAINPOS_CLASSIFIER);

	packetchain->RemoveHandler(&phydot11_packethook_dot11tracker, 
            CHAINPOS_TRACKER);

    timetracker->RemoveTimer(device_idle_timer);

    delete[] recent_packet_checksums;
}

int Kis_80211_Phy::LoadWepkeys() {
    // Convert the WEP mappings to our real map
    vector<string> raw_wepmap_vec;
    raw_wepmap_vec = globalreg->kismet_config->FetchOptVec("wepkey");
    for (size_t rwvi = 0; rwvi < raw_wepmap_vec.size(); rwvi++) {
        string wepline = raw_wepmap_vec[rwvi];

        size_t rwsplit = wepline.find(",");
        if (rwsplit == string::npos) {
            _MSG("Malformed 'wepkey' option in the config file", MSGFLAG_FATAL);
			globalreg->fatal_condition = 1;
			return -1;
        }

        mac_addr bssid_mac = wepline.substr(0, rwsplit).c_str();

        if (bssid_mac.error == 1) {
            _MSG("Malformed 'wepkey' option in the config file", MSGFLAG_FATAL);
			globalreg->fatal_condition = 1;
			return -1;
        }

        string rawkey = wepline.substr(rwsplit + 1, wepline.length() - (rwsplit + 1));

        unsigned char key[WEPKEY_MAX];
        int len = Hex2UChar((unsigned char *) rawkey.c_str(), key);

        if (len != 5 && len != 13 && len != 16) {
			_MSG("Invalid key '" + rawkey + "' length " + IntToString(len) + 
				 " in a wepkey= config file entry", MSGFLAG_FATAL);
			globalreg->fatal_condition = 1;
			return -1;
        }

        dot11_wep_key *keyinfo = new dot11_wep_key;
        keyinfo->bssid = bssid_mac;
        keyinfo->fragile = 0;
        keyinfo->decrypted = 0;
        keyinfo->failed = 0;
        keyinfo->len = len;
        memcpy(keyinfo->key, key, sizeof(unsigned char) * WEPKEY_MAX);

        wepkeys.insert(bssid_mac, keyinfo);

		_MSG("Using key '" + rawkey + "' for BSSID " + bssid_mac.Mac2String(),
			 MSGFLAG_INFO);
    }

	return 1;
}

// Classifier is responsible for processing a dot11 packet and filling in enough
// of the common info for the system to make a device out of it.
int Kis_80211_Phy::CommonClassifierDot11(CHAINCALL_PARMS) {
    Kis_80211_Phy *d11phy = (Kis_80211_Phy *) auxdata;

    // Don't process errors, blocked, or dupes
    if (in_pack->error || in_pack->filtered || in_pack->duplicate)
        return 0;

    // Get the 802.11 info
    dot11_packinfo *dot11info = 
        (dot11_packinfo *) in_pack->fetch(d11phy->pack_comp_80211);

	kis_layer1_packinfo *pack_l1info =
		(kis_layer1_packinfo *) in_pack->fetch(d11phy->pack_comp_l1info);

    if (dot11info == NULL)
        return 0;

    if (pack_l1info != NULL && pack_l1info->signal_dbm > d11phy->signal_too_loud_threshold
            && pack_l1info->signal_dbm < 0 && 
            d11phy->alertracker->PotentialAlert(d11phy->alert_tooloud_ref)) {

        stringstream ss;

        ss << "Saw packet with a reported signal level of " <<
            pack_l1info->signal_dbm << " which is above the threshold of " <<
            d11phy->signal_too_loud_threshold << ".  Excessively high signal levels can " <<
            "be caused by misconfigured external amplifiers and lead to lost " <<
            "packets.";

        d11phy->alertracker->RaiseAlert(d11phy->alert_tooloud_ref, in_pack, 
                dot11info->bssid_mac, dot11info->source_mac, 
                dot11info->dest_mac, dot11info->other_mac, 
                dot11info->channel, ss.str());
    }

    // Get the checksum info
    kis_packet_checksum *fcs =
        (kis_packet_checksum *) in_pack->fetch(d11phy->pack_comp_checksum);

    // We don't do anything if the packet is invalid;  in the future we might want
    // to try to attach it to an existing network if we can understand that much
    // of the frame and then treat it as an error, but that artificially inflates 
    // the error condition on a network when FCS errors are pretty normal.
    // By never creating a common info record we should prevent any handling of this
    // nonsense.
    if (fcs != NULL && fcs->checksum_valid == 0) {
        return 0;
    }

    kis_common_info *ci = 
        (kis_common_info *) in_pack->fetch(d11phy->pack_comp_common);

    if (ci == NULL) {
        ci = new kis_common_info;
        in_pack->insert(d11phy->pack_comp_common, ci);
    } 

    ci->phyid = d11phy->phyid;

    if (dot11info->type == packet_management) {
        ci->type = packet_basic_mgmt;

        // We track devices/nets/clients by source mac, bssid if source
        // is impossible
        if (dot11info->source_mac == globalreg->empty_mac) {
            if (dot11info->bssid_mac == globalreg->empty_mac) {
                fprintf(stderr, "debug - dot11info bssid and src are empty and mgmt\n");
                ci->error = 1;
            }

            ci->device = dot11info->bssid_mac;
        } else {
            ci->device = dot11info->source_mac;
        }

        ci->source = dot11info->source_mac;

        ci->dest = dot11info->dest_mac;

        ci->transmitter = dot11info->bssid_mac;
    } else if (dot11info->type == packet_phy) {
        if (dot11info->subtype == packet_sub_ack || dot11info->subtype == packet_sub_cts) {
            // map some phys as a device since we know they're being talked to
            ci->device = dot11info->dest_mac;
        } else if (dot11info->source_mac == globalreg->empty_mac) {
            ci->error = 1;
        } else {
            ci->device = dot11info->source_mac;
        }

        ci->type = packet_basic_phy;

        ci->transmitter = ci->device;

    } else if (dot11info->type == packet_data) {
        // Data packets come from the source address.  Wired devices bridged
        // from an AP are considered wired clients of that AP and classified as
        // clients normally
        ci->type = packet_basic_data;

        ci->device = dot11info->source_mac;
        ci->source = dot11info->source_mac;

        ci->dest = dot11info->dest_mac;

        ci->transmitter = dot11info->bssid_mac;

        // Something is broken with the data frame
        if (dot11info->bssid_mac == globalreg->empty_mac ||
                dot11info->source_mac == globalreg->empty_mac ||
                dot11info->dest_mac == globalreg->empty_mac) {
            fprintf(stderr, "debug - dot11info macs are empty and data\n");
            ci->error = 1;
        }
    } 

    if (dot11info->type == packet_noise || dot11info->corrupt ||
            in_pack->error || dot11info->type == packet_unknown ||
            dot11info->subtype == packet_sub_unknown) {
        fprintf(stderr, "debug - noise, corrupt, error, etc %d %d %d %d %d\n", dot11info->type == packet_noise, dot11info->corrupt, in_pack->error, dot11info->type == packet_unknown, dot11info->subtype == packet_sub_unknown);
        ci->error = 1;
    }

    ci->channel = dot11info->channel;

    ci->datasize = dot11info->datasize;

    if (dot11info->cryptset == crypt_none) {
        ci->basic_crypt_set = KIS_DEVICE_BASICCRYPT_NONE;
    } else {
        ci->basic_crypt_set = KIS_DEVICE_BASICCRYPT_ENCRYPTED;
    }

    // Fill in basic l2 and l3 encryption
    if (dot11info->cryptset & crypt_l2_mask) {
        ci->basic_crypt_set |= KIS_DEVICE_BASICCRYPT_L2;
    } if (dot11info->cryptset & crypt_l3_mask) {
        ci->basic_crypt_set |= KIS_DEVICE_BASICCRYPT_L3;
    }

    return 1;
}

void Kis_80211_Phy::SetStringExtract(int in_extr) {
	if (in_extr == 0 && dissect_strings == 2) {
		_MSG("SetStringExtract(): String dissection cannot be disabled because "
			 "it is required by another active component.", MSGFLAG_ERROR);
		return;
	}

	// If we're setting the extract here, we have to turn it on for all BSSIDs
	dissect_strings = in_extr;
	dissect_all_strings = in_extr;
}

void Kis_80211_Phy::AddWepKey(mac_addr bssid, uint8_t *key, unsigned int len, 
							  int temp) {
	if (len > WEPKEY_MAX)
		return;

    dot11_wep_key *winfo = new dot11_wep_key;

	winfo->decrypted = 0;
	winfo->failed = 0;
    winfo->bssid = bssid;
	winfo->fragile = temp;
    winfo->len = len;

    memcpy(winfo->key, key, len);

    // Replace exiting ones
	if (wepkeys.find(winfo->bssid) != wepkeys.end()) {
		delete wepkeys[winfo->bssid];
		wepkeys[winfo->bssid] = winfo;
		return;
	}

	wepkeys.insert(winfo->bssid, winfo);
}

void Kis_80211_Phy::HandleSSID(shared_ptr<kis_tracked_device_base> basedev,
        shared_ptr<dot11_tracked_device> dot11dev,
        kis_packet *in_pack,
        dot11_packinfo *dot11info,
        kis_gps_packinfo *pack_gpsinfo) {

    SharedTrackerElement adv_ssid_map = dot11dev->get_advertised_ssid_map();

    shared_ptr<dot11_advertised_ssid> ssid;

    TrackerElement::map_iterator ssid_itr;

    if (adv_ssid_map == NULL) {
        fprintf(stderr, "debug - dot11phy::HandleSSID can't find the adv_ssid_map or probe_ssid_map struct, something is wrong\n");
        return;
    }

    if (dot11info->subtype == packet_sub_beacon ||
            dot11info->subtype == packet_sub_probe_resp) {
        ssid_itr = adv_ssid_map->find((int32_t) dot11info->ssid_csum);

        if (ssid_itr == adv_ssid_map->end()) {
            ssid = dot11dev->new_advertised_ssid();
            adv_ssid_map->add_intmap((int32_t) dot11info->ssid_csum, ssid);

            ssid->set_crypt_set(dot11info->cryptset);
            ssid->set_first_time(in_pack->ts.tv_sec);
            ssid->set_ietag_checksum(dot11info->ietag_csum);
            ssid->set_channel(dot11info->channel);

            ssid->set_dot11d_country(dot11info->dot11d_country);
            ssid->set_dot11d_vec(dot11info->dot11d_vec);

            // TODO handle loading SSID from the stored file
            ssid->set_ssid(dot11info->ssid);
            if (dot11info->ssid_len == 0 || dot11info->ssid_blank) {
                ssid->set_ssid_cloaked(true);
            }
            ssid->set_ssid_len(dot11info->ssid_len);

            ssid->set_crypt_set(dot11info->cryptset);

            ssid->set_beacon_info(dot11info->beacon_info);

            ssid->set_wps_state(dot11info->wps);
            ssid->set_wps_manuf(dot11info->wps_manuf);
            ssid->set_wps_model_name(dot11info->wps_model_name);
            ssid->set_wps_model_number(dot11info->wps_model_number);

            // Do we not know the basedev manuf?
            if (basedev->get_manuf() == "" && dot11info->wps_manuf != "")
                basedev->set_manuf(dot11info->wps_manuf);

            if (ssid->get_last_time() < in_pack->ts.tv_sec)
                ssid->set_last_time(in_pack->ts.tv_sec);

            // If we have a new ssid and we can consider raising an alert, do the 
            // regex compares to see if we trigger apspoof
            if (dot11info->ssid_len != 0 &&
                    globalreg->alertracker->PotentialAlert(alert_ssidmatch_ref)) {
                TrackerElementVector sv(ssid_regex_vec);

                for (auto s : sv) {
                    std::shared_ptr<dot11_tracked_ssid_alert> sa =
                        std::static_pointer_cast<dot11_tracked_ssid_alert>(s);

                    if (sa->compare_ssid(dot11info->ssid, dot11info->source_mac)) {
                        std::string ntype = 
                            dot11info->subtype == packet_sub_beacon ? std::string("advertising") :
                            std::string("responding for");

                        string al = "IEEE80211 Unauthorized device (" + 
                            dot11info->source_mac.Mac2String() + std::string(") ") + ntype + 
                            " for SSID '" + dot11info->ssid + "', matching APSPOOF "
                            "rule " + sa->get_group_name() + 
                            std::string(" which may indicate spoofing or impersonation.");

                        globalreg->alertracker->RaiseAlert(alert_ssidmatch_ref, in_pack, 
                                dot11info->bssid_mac, 
                                dot11info->source_mac, 
                                dot11info->dest_mac, 
                                dot11info->other_mac, 
                                dot11info->channel, al);
                        break;
                    }
                }
            }
        } else {
            ssid = static_pointer_cast<dot11_advertised_ssid>(ssid_itr->second);
            if (ssid->get_last_time() < in_pack->ts.tv_sec)
                ssid->set_last_time(in_pack->ts.tv_sec);
        }

        if (dot11info->subtype == packet_sub_beacon) {
            // Update the base device records
            dot11dev->set_last_beaconed_ssid(ssid->get_ssid());
            dot11dev->set_last_beaconed_ssid_csum(dot11info->ssid_csum);

            ssid->inc_beacons_sec();

            if (alertracker->PotentialAlert(alert_airjackssid_ref) &&
                        ssid->get_ssid() == "AirJack" ) {

                string al = "IEEE80211 Access Point BSSID " +
                    basedev->get_macaddr().Mac2String() + " broadcasting SSID "
                    "\"AirJack\" which implies an attempt to disrupt "
                    "networks.";

                alertracker->RaiseAlert(alert_airjackssid_ref, in_pack, 
                        dot11info->bssid_mac, dot11info->source_mac, 
                        dot11info->dest_mac, dot11info->other_mac, 
                        dot11info->channel, al);
            }

            if (ssid->get_ssid() != "") {
                basedev->set_devicename(ssid->get_ssid());
            } else {
                basedev->set_devicename(basedev->get_macaddr().Mac2String());
            }

            // Set the type
            ssid->set_ssid_beacon(true);

            // Set the mobility
            if (dot11info->dot11r_mobility != NULL) {
                ssid->set_dot11r_mobility(true);
                ssid->set_dot11r_mobility_domain_id(dot11info->dot11r_mobility->mobility_domain());
            }

            // Set QBSS
            if (dot11info->qbss != NULL) {
                ssid->set_dot11e_qbss(true);
                ssid->set_dot11e_qbss_stations(dot11info->qbss->station_count());

                // Percentage is value / max (1 byte, 255)
                double chperc = (double) ((double) dot11info->qbss->channel_utilization() / 
                    (double) 255.0f) * 100.0f;
                ssid->set_dot11e_qbss_channel_load(chperc);
            }

            // Do we have HT or VHT data?  I don't think we can have one
            // without the other
            if (dot11info->dot11vht != NULL && dot11info->dot11ht != NULL) {
                // Grab the primary channel from the HT data
                ssid->set_channel(IntToString(dot11info->dot11ht->primary_channel()));

                if (dot11info->dot11vht->channel_width() ==
                        dot11_ie_192_vht_operation_t::CHANNEL_WIDTH_CH_80) {
                    ssid->set_ht_mode("HT40");
                    ssid->set_ht_center_1(5000 + (5 * dot11info->dot11vht->center1()));
                    ssid->set_ht_center_2(0);
                } else if (dot11info->dot11vht->channel_width() ==
                        dot11_ie_192_vht_operation_t::CHANNEL_WIDTH_CH_160) {
                    ssid->set_ht_mode("HT160");
                    ssid->set_ht_center_1(5000 + (5 * dot11info->dot11vht->center1()));
                    ssid->set_ht_center_2(0);
                } else if (dot11info->dot11vht->channel_width() ==
                        dot11_ie_192_vht_operation_t::CHANNEL_WIDTH_CH_80_80) {
                    ssid->set_ht_mode("HT80+80");
                    ssid->set_ht_center_1(5000 + (5 * dot11info->dot11vht->center1()));
                    ssid->set_ht_center_2(5000 + (5 * dot11info->dot11vht->center2()));
                } else if (dot11info->dot11vht->channel_width() ==
                        dot11_ie_192_vht_operation_t::CHANNEL_WIDTH_CH_20_40) {
                    if (dot11info->dot11ht->ht_info_chan_offset_none()) {
                        ssid->set_ht_mode("HT20");
                    } else if (dot11info->dot11ht->ht_info_chan_offset_above()) {
                        ssid->set_ht_mode("HT40+");
                    } else if (dot11info->dot11ht->ht_info_chan_offset_below()) {
                        ssid->set_ht_mode("HT40-");
                    }

                    ssid->set_ht_center_1(0);
                    ssid->set_ht_center_2(0);

                } 
            } else if (dot11info->dot11ht != NULL) {
                // Only HT info no VHT
                if (dot11info->dot11ht->ht_info_chan_offset_none()) {
                    ssid->set_ht_mode("HT20");
                } else if (dot11info->dot11ht->ht_info_chan_offset_above()) {
                    ssid->set_ht_mode("HT40+");
                } else if (dot11info->dot11ht->ht_info_chan_offset_below()) {
                    ssid->set_ht_mode("HT40-");
                }

                ssid->set_ht_center_1(0);
                ssid->set_ht_center_2(0);
                ssid->set_channel(IntToString(dot11info->dot11ht->primary_channel()));
            }


        } else if (dot11info->subtype == packet_sub_probe_resp) {
            if (mac_addr((uint8_t *) "\x00\x13\x37\x00\x00\x00", 6, 24) == 
                    dot11info->source_mac) {

                if (alertracker->PotentialAlert(alert_l33t_ref)) {
                    string al = "IEEE80211 probe response from OUI 00:13:37 seen, "
                        "which typically implies a Karma AP impersonation attack.";

                    alertracker->RaiseAlert(alert_l33t_ref, in_pack, 
                            dot11info->bssid_mac, dot11info->source_mac, 
                            dot11info->dest_mac, dot11info->other_mac, 
                            dot11info->channel, al);
                }

            }

            ssid->set_ssid_probe_response(true);
            dot11dev->set_last_probed_ssid(ssid->get_ssid());
            dot11dev->set_last_probed_ssid_csum(dot11info->ssid_csum);
        }
    }

    // TODO alert on change on SSID IE tags?
    if (ssid->get_ietag_checksum() != dot11info->ietag_csum) {
        // fprintf(stderr, "debug - dot11phy:HandleSSID %s ietag checksum changed\n", basedev->get_macaddr().Mac2String().c_str());

        // Things to check:
        // dot11d values
        // channel
        // WPS
        // Cryptset
        

        if (ssid->get_crypt_set() != dot11info->cryptset) {
            if (ssid->get_crypt_set() && dot11info->cryptset == crypt_none &&
                    alertracker->PotentialAlert(alert_wepflap_ref)) {

                string al = "IEEE80211 Access Point BSSID " +
                    basedev->get_macaddr().Mac2String() + " SSID \"" +
                    ssid->get_ssid() + "\" changed advertised encryption from " +
                    CryptToString(ssid->get_crypt_set()) + " to Open which may "
                    "indicate AP spoofing/impersonation";

                alertracker->RaiseAlert(alert_wepflap_ref, in_pack, 
                        dot11info->bssid_mac, dot11info->source_mac, 
                        dot11info->dest_mac, dot11info->other_mac, 
                        dot11info->channel, al);
            } else if (ssid->get_crypt_set() != dot11info->cryptset &&
                    alertracker->PotentialAlert(alert_cryptchange_ref)) {

                string al = "IEEE80211 Access Point BSSID " +
                    basedev->get_macaddr().Mac2String() + " SSID \"" +
                    ssid->get_ssid() + "\" changed advertised encryption from " +
                    CryptToString(ssid->get_crypt_set()) + " to " + 
                    CryptToString(dot11info->cryptset) + " which may indicate "
                    "AP spoofing/impersonation";

                alertracker->RaiseAlert(alert_cryptchange_ref, in_pack, 
                        dot11info->bssid_mac, dot11info->source_mac, 
                        dot11info->dest_mac, dot11info->other_mac, 
                        dot11info->channel, al);
            }

            ssid->set_crypt_set(dot11info->cryptset);
        }

        if (ssid->get_channel() != dot11info->channel && 
                dot11info->channel != "0") {

            string al = "IEEE80211 Access Point BSSID " +
                basedev->get_macaddr().Mac2String() + " SSID \"" +
                ssid->get_ssid() + "\" changed advertised channel from " +
                ssid->get_channel() + " to " + 
                dot11info->channel + " which may "
                "indicate AP spoofing/impersonation";

            alertracker->RaiseAlert(alert_chan_ref, in_pack, 
                    dot11info->bssid_mac, dot11info->source_mac, 
                    dot11info->dest_mac, dot11info->other_mac, 
                    dot11info->channel, al);

            ssid->set_channel(dot11info->channel); 
        }

        if (ssid->get_dot11d_country() != dot11info->dot11d_country) {
            fprintf(stderr, "debug - dot11phy:HandleSSID %s dot11d country changed\n", basedev->get_macaddr().Mac2String().c_str());

            ssid->set_dot11d_country(dot11info->dot11d_country);

            // TODO raise alert
        }

        bool dot11dmismatch = false;

        TrackerElementVector dot11dvec(ssid->get_dot11d_vec());
        for (unsigned int vc = 0; 
                vc < dot11dvec.size() && vc < dot11info->dot11d_vec.size(); vc++) {
            shared_ptr<dot11_11d_tracked_range_info> ri =
                static_pointer_cast<dot11_11d_tracked_range_info>(dot11dvec[vc]);

            if (ri->get_startchan() != dot11info->dot11d_vec[vc].startchan ||
                    ri->get_numchan() != dot11info->dot11d_vec[vc].numchan ||
                    ri->get_txpower() != dot11info->dot11d_vec[vc].txpower) {
                dot11dmismatch = true;
                break;
            }

        }

        if (dot11dmismatch) {
            ssid->set_dot11d_vec(dot11info->dot11d_vec);

            if (alertracker->PotentialAlert(alert_dot11d_ref)) {

					string al = "IEEE80211 Access Point BSSID " +
						basedev->get_macaddr().Mac2String() + " SSID \"" +
						ssid->get_ssid() + "\" advertised conflicting 802.11d "
                        "information which may indicate AP spoofing/impersonation";

					alertracker->RaiseAlert(alert_dot11d_ref, in_pack, 
                            dot11info->bssid_mac, dot11info->source_mac, 
                            dot11info->dest_mac, dot11info->other_mac, 
                            dot11info->channel, al);

            }
        }

        if (ssid->get_wps_state() != dot11info->wps) {
            ssid->set_wps_state(dot11info->wps);

        }

        if (dot11info->beacon_interval && ssid->get_beaconrate() != 
                Ieee80211Interval2NSecs(dot11info->beacon_interval)) {

            if (ssid->get_beaconrate() != 0 && 
                    alertracker->PotentialAlert(alert_beaconrate_ref)) {
                string al = "IEEE80211 Access Point BSSID " +
                    basedev->get_macaddr().Mac2String() + " SSID \"" +
                    ssid->get_ssid() + "\" changed beacon rate from " +
                    IntToString(ssid->get_beaconrate()) + " to " + 
                    IntToString(Ieee80211Interval2NSecs(dot11info->beacon_interval)) + 
                    " which may indicate AP spoofing/impersonation";

                alertracker->RaiseAlert(alert_beaconrate_ref, in_pack, 
                        dot11info->bssid_mac, dot11info->source_mac, 
                        dot11info->dest_mac, dot11info->other_mac, 
                        dot11info->channel, al);
            }

            ssid->set_beaconrate(Ieee80211Interval2NSecs(dot11info->beacon_interval));
        }

        ssid->set_maxrate(dot11info->maxrate);

        ssid->set_ietag_checksum(dot11info->ietag_csum);
    }

    // Add the location data, if any
    if (pack_gpsinfo != NULL && pack_gpsinfo->fix > 1) {
        ssid->get_location()->add_loc(pack_gpsinfo->lat, pack_gpsinfo->lon,
                pack_gpsinfo->alt, pack_gpsinfo->fix);

    }

}

void Kis_80211_Phy::HandleProbedSSID(shared_ptr<kis_tracked_device_base> basedev,
        shared_ptr<dot11_tracked_device> dot11dev,
        kis_packet *in_pack,
        dot11_packinfo *dot11info,
        kis_gps_packinfo *pack_gpsinfo) {

    TrackerElementIntMap probemap(dot11dev->get_probed_ssid_map());

    shared_ptr<dot11_probed_ssid> probessid = NULL;
    TrackerElement::int_map_iterator ssid_itr;

    if (dot11info->subtype == packet_sub_probe_req ||
            dot11info->subtype == packet_sub_reassociation_req) {

        ssid_itr = probemap.find(dot11info->ssid_csum);

        if (ssid_itr == probemap.end()) {
            probessid = dot11dev->new_probed_ssid();
            TrackerElement::int_map_pair p(dot11info->ssid_csum, probessid);
            probemap.insert(p);

            probessid->set_ssid(dot11info->ssid);
            probessid->set_ssid_len(dot11info->ssid_len);
            probessid->set_first_time(in_pack->ts.tv_sec);
        } else {
            probessid = std::static_pointer_cast<dot11_probed_ssid>(ssid_itr->second);
        }

        if (probessid != NULL) {
            if (probessid->get_last_time() < in_pack->ts.tv_sec)
                probessid->set_last_time(in_pack->ts.tv_sec);

            // Add the location data, if any
            if (pack_gpsinfo != NULL && pack_gpsinfo->fix > 1) {
                probessid->get_location()->add_loc(pack_gpsinfo->lat, pack_gpsinfo->lon,
                        pack_gpsinfo->alt, pack_gpsinfo->fix);

            }

            if (dot11info->dot11r_mobility != NULL) {
                probessid->set_dot11r_mobility(true);
                probessid->set_dot11r_mobility_domain_id(dot11info->dot11r_mobility->mobility_domain());
            }
        }
    }

}

void Kis_80211_Phy::HandleClient(shared_ptr<kis_tracked_device_base> basedev,
        shared_ptr<dot11_tracked_device> dot11dev,
        kis_packet *in_pack,
        dot11_packinfo *dot11info,
        kis_gps_packinfo *pack_gpsinfo,
        kis_data_packinfo *pack_datainfo) {

    // If we can't map to a bssid then we can't associate this as a client
    if (dot11info->bssid_mac == globalreg->empty_mac)
        return;

    // We don't link broadcasts
    if (dot11info->bssid_mac == globalreg->broadcast_mac)
        return;

    TrackerElementMacMap client_map(dot11dev->get_client_map());

    shared_ptr<dot11_client> client = NULL;

    TrackerElement::mac_map_iterator client_itr;

    client_itr = client_map.find(dot11info->bssid_mac);

    bool new_client = false;
    if (client_itr == client_map.end()) {
        client = dot11dev->new_client();
        TrackerElement::mac_map_pair cp(dot11info->bssid_mac, client);
        client_map.insert(cp);
        new_client = true;
    } else {
        client = static_pointer_cast<dot11_client>(client_itr->second);
    }

    if (new_client) {
        client->set_bssid(dot11info->bssid_mac);
        client->set_first_time(in_pack->ts.tv_sec);
    }

    if (client->get_last_time() < in_pack->ts.tv_sec)
        client->set_last_time(in_pack->ts.tv_sec);

    if (dot11info->type == packet_data) {
        client->inc_datasize(dot11info->datasize);

        if (dot11info->fragmented) {
            client->inc_num_fragments(1);
        }

        if (dot11info->retry) {
            client->inc_num_retries(1);
            client->inc_datasize_retry(dot11info->datasize);
        }

        if (pack_datainfo != NULL) {
            if (pack_datainfo->proto == proto_eap) {
                if (pack_datainfo->auxstring != "") {
                    client->set_eap_identity(pack_datainfo->auxstring);
                }
            }

            if (pack_datainfo->discover_vendor != "") {
                if (client->get_dhcp_vendor() != "" &&
                        client->get_dhcp_vendor() != pack_datainfo->discover_vendor &&
						alertracker->PotentialAlert(alert_dhcpos_ref)) {
						string al = "IEEE80211 network BSSID " + 
							client->get_bssid().Mac2String() +
							" client " + 
							basedev->get_macaddr().Mac2String() + 
							"changed advertised DHCP vendor from '" +
							client->get_dhcp_vendor() + "' to '" +
							pack_datainfo->discover_vendor + "' which may indicate "
							"client spoofing or impersonation";

                        alertracker->RaiseAlert(alert_dhcpos_ref, in_pack,
                                dot11info->bssid_mac, dot11info->source_mac,
                                dot11info->dest_mac, dot11info->other_mac,
                                dot11info->channel, al);
                }

                client->set_dhcp_vendor(pack_datainfo->discover_vendor);
            }

            if (pack_datainfo->discover_host != "") {
                if (client->get_dhcp_host() != "" &&
                        client->get_dhcp_host() != pack_datainfo->discover_host &&
						alertracker->PotentialAlert(alert_dhcpname_ref)) {
						string al = "IEEE80211 network BSSID " + 
							client->get_bssid().Mac2String() +
							" client " + 
							basedev->get_macaddr().Mac2String() + 
							"changed advertised DHCP hostname from '" +
							client->get_dhcp_host() + "' to '" +
							pack_datainfo->discover_host + "' which may indicate "
							"client spoofing or impersonation";

                        alertracker->RaiseAlert(alert_dhcpname_ref, in_pack,
                                dot11info->bssid_mac, dot11info->source_mac,
                                dot11info->dest_mac, dot11info->other_mac,
                                dot11info->channel, al);
                }

                client->set_dhcp_host(pack_datainfo->discover_host);
            }

            if (pack_datainfo->cdp_dev_id != "") {
                client->set_cdp_device(pack_datainfo->cdp_dev_id);
            }

            if (pack_datainfo->cdp_port_id != "") {
                client->set_cdp_port(pack_datainfo->cdp_port_id);
            }
        }
    }

    if (pack_gpsinfo != NULL && pack_gpsinfo->fix > 1) {
        client->get_location()->add_loc(pack_gpsinfo->lat, pack_gpsinfo->lon,
                pack_gpsinfo->alt, pack_gpsinfo->fix);
    }

    // Try to make the back-record of us in the device we're a client OF
    TrackedDeviceKey backkey(globalreg->server_uuid_hash, phyname_hash, dot11info->bssid_mac);
    shared_ptr<kis_tracked_device_base> backdev = devicetracker->FetchDevice(backkey);

    // Always set a key since keys are now consistent
    client->set_bssid_key(backkey);

    if (backdev != NULL) {
        shared_ptr<dot11_tracked_device> backdot11 = 
            static_pointer_cast<dot11_tracked_device>(backdev->get_map_value(dot11_device_entry_id));

        if (backdot11 != NULL) {
            if (backdot11->get_associated_client_map()->mac_find(basedev->get_macaddr()) ==
                    backdot11->get_associated_client_map()->mac_end()) {

                backdot11->get_associated_client_map()->add_macmap(basedev->get_macaddr(), basedev->get_tracker_key());
            }
        }
    }
}

static int packetnum = 0;

int Kis_80211_Phy::TrackerDot11(kis_packet *in_pack) {
    packetnum++;

	// We can't do anything w/ it from the packet layer
	if (in_pack->error || in_pack->filtered || in_pack->duplicate) {
        // fprintf(stderr, "debug - error packet\n");
		return 0;
	}

	// Fetch what we already know about the packet.  
	dot11_packinfo *dot11info =
		(dot11_packinfo *) in_pack->fetch(pack_comp_80211);

	// Got nothing to do
	if (dot11info == NULL) {
        // fprintf(stderr, "debug - no dot11info\n");
		return 0;
    }

	kis_common_info *commoninfo =
		(kis_common_info *) in_pack->fetch(pack_comp_common);

	if (commoninfo == NULL) {
        // fprintf(stderr, "debug - no commoninfo\n");
		return 0;
    }

	if (commoninfo->error) {
        // fprintf(stderr, "debug - common error\n");
		return 0;
    }

    // There's nothing we can sensibly do with completely corrupt packets, 
    // so we just get rid of them.
    // TODO make sure phy corrupt packets are handled for statistics
    if (dot11info->corrupt)  {
        return 0;
    }

    // If we don't process phys...
    if (commoninfo->type == packet_basic_phy && !process_ctl_phy)
        return 0;

    // Find & update the common attributes of our base record.
    // We want to update signal, frequency, location, packet counts, devices,
    // and encryption, because this is the core record for everything we do.
    // We do this early on because we want to track things even if they're unknown
    // or broken.
    shared_ptr<kis_tracked_device_base> basedev =
        devicetracker->UpdateCommonDevice(commoninfo->device, this, in_pack, 
                (UCD_UPDATE_SIGNAL | UCD_UPDATE_FREQUENCIES |
                 UCD_UPDATE_PACKETS | UCD_UPDATE_LOCATION |
                 UCD_UPDATE_SEENBY | UCD_UPDATE_ENCRYPTION));

	kis_data_packinfo *pack_datainfo =
		(kis_data_packinfo *) in_pack->fetch(pack_comp_basicdata);

	// We can't do anything useful
	if (dot11info->corrupt || dot11info->type == packet_noise ||
		dot11info->type == packet_unknown || 
		dot11info->subtype == packet_sub_unknown) {
        // fprintf(stderr, "debug - unknown or noise packet\n");
		return 0;
    }

	kis_gps_packinfo *pack_gpsinfo =
		(kis_gps_packinfo *) in_pack->fetch(pack_comp_gps);

    // Something bad has happened if we can't find our device
    if (basedev == NULL) {
        fprintf(stderr, "debug - phydot11 got to tracking stage with no devicetracker device for %s.  Something is wrong?\n", commoninfo->device.Mac2String().c_str());
        return 0;
    }

    local_locker basedevlocker(&(basedev->device_mutex));

    // Store it in the common info for future use
    commoninfo->base_device = basedev;

    shared_ptr<dot11_tracked_device> dot11dev =
        static_pointer_cast<dot11_tracked_device>(basedev->get_map_value(dot11_device_entry_id));

    if (dot11dev == NULL) {
        stringstream ss;
        ss << "Detected new 802.11 Wi-Fi device " << commoninfo->device.Mac2String() << " packet " << packetnum;
        _MSG(ss.str(), MSGFLAG_INFO);

        dot11dev.reset(new dot11_tracked_device(globalreg, dot11_device_entry_id));
        dot11_tracked_device::attach_base_parent(dot11dev, basedev);
    }

    // Update the last beacon timestamp
    if (dot11info->type == packet_management && dot11info->subtype == packet_sub_beacon) {
        dot11dev->set_last_beacon_timestamp(in_pack->ts.tv_sec);
    }

    // Handle beacons and SSID responses from the AP.  This is still all the same
    // basic device
    if (dot11info->type == packet_management && 
            (dot11info->subtype == packet_sub_beacon ||
             dot11info->subtype == packet_sub_probe_resp)) {
        HandleSSID(basedev, dot11dev, in_pack, dot11info, pack_gpsinfo);
    }

    // Handle probe reqs
    if (dot11info->type == packet_management &&
            (dot11info->subtype == packet_sub_probe_req ||
             dot11info->subtype == packet_sub_reassociation_req)) {
        HandleProbedSSID(basedev, dot11dev, in_pack, dot11info, pack_gpsinfo);
    }

    // Increase data size for ourselves, if we're a data packet
    if (dot11info->type == packet_data) {
        dot11dev->inc_datasize(dot11info->datasize);

        if (dot11info->fragmented) {
            dot11dev->inc_num_fragments(1);
        }

        if (dot11info->retry) {
            dot11dev->inc_num_retries(1);
            dot11dev->inc_datasize_retry(dot11info->datasize);
        }
    }

    if (dot11info->distrib == distrib_inter) {
        basedev->bitset_basic_type_set(KIS_DEVICE_BASICTYPE_PEER);

        // If we're /only/ a IBSS peer
        if (basedev->get_basic_type_set() == KIS_DEVICE_BASICTYPE_PEER) {
            basedev->set_type_string("Wi-Fi Peer");
            basedev->set_devicename(basedev->get_macaddr().Mac2String());
        }
    }

	if (dot11info->type == packet_phy) {
        // Phy to a known device mac, we know it's a wifi device
        basedev->bitset_basic_type_set(KIS_DEVICE_BASICTYPE_CLIENT);

        // If we're /only/ a client, set the type name and device name
        if (basedev->get_basic_type_set() == KIS_DEVICE_BASICTYPE_CLIENT) {
            basedev->set_type_string("Wi-Fi Client");
            basedev->set_devicename(basedev->get_macaddr().Mac2String());
        }
    } else if (dot11info->ess) { 
        // ESS from-ap packets mean we must be an AP
        basedev->bitset_basic_type_set(KIS_DEVICE_BASICTYPE_AP);

        // If we're an AP always set the type and name because that's the
        // "most important" thing we can be
        basedev->set_type_string("Wi-Fi AP");

		// Throw alert if device changes between bss and adhoc
        if (dot11dev->bitcheck_type_set(DOT11_DEVICE_TYPE_ADHOC) &&
                !dot11dev->bitcheck_type_set(DOT11_DEVICE_TYPE_BEACON_AP) &&
                alertracker->PotentialAlert(alert_adhoc_ref)) {
				string al = "IEEE80211 Network BSSID " + 
					dot11info->bssid_mac.Mac2String() + 
					" previously advertised as AP network, now advertising as "
					"Ad-Hoc/WDS which may indicate AP spoofing/impersonation";

                alertracker->RaiseAlert(alert_adhoc_ref, in_pack,
                        dot11info->bssid_mac, dot11info->source_mac,
                        dot11info->dest_mac, dot11info->other_mac,
                        dot11info->channel, al);
        }

        dot11dev->bitset_type_set(DOT11_DEVICE_TYPE_BEACON_AP);
    } else if (dot11info->distrib == distrib_inter) {
        // Adhoc
        basedev->bitset_basic_type_set(KIS_DEVICE_BASICTYPE_PEER);

        if (basedev->get_basic_type_set() == KIS_DEVICE_BASICTYPE_PEER)
            basedev->set_type_string("Wi-Fi Ad-hoc / WDS ");

		// Throw alert if device changes to adhoc
        if (!dot11dev->bitcheck_type_set(DOT11_DEVICE_TYPE_ADHOC) &&
                dot11dev->bitcheck_type_set(DOT11_DEVICE_TYPE_BEACON_AP) &&
                alertracker->PotentialAlert(alert_adhoc_ref)) {
				string al = "IEEE80211 Network BSSID " + 
					dot11info->bssid_mac.Mac2String() + 
					" previously advertised as AP network, now advertising as "
					"Ad-Hoc/WDS which may indicate AP spoofing/impersonation";

                alertracker->RaiseAlert(alert_adhoc_ref, in_pack,
                        dot11info->bssid_mac, dot11info->source_mac,
                        dot11info->dest_mac, dot11info->other_mac,
                        dot11info->channel, al);
        }

        dot11dev->bitset_type_set(DOT11_DEVICE_TYPE_ADHOC);
    } 
   
    // Sent by ap, data, not from AP, means it's bridged from somewhere else
    if (dot11info->distrib == distrib_from &&
            dot11info->bssid_mac != basedev->get_macaddr() &&
            dot11info->type == packet_data) {
        basedev->bitset_basic_type_set(KIS_DEVICE_BASICTYPE_WIRED);

        // Set the typename and device name if we've only been seen as wired
        if (basedev->get_basic_type_set() == KIS_DEVICE_BASICTYPE_WIRED) {
            basedev->set_type_string("Wi-Fi Bridged Device");
            basedev->set_devicename(basedev->get_macaddr().Mac2String());
        }

        dot11dev->bitset_type_set(DOT11_DEVICE_TYPE_WIRED);

        basedev->set_devicename(basedev->get_macaddr().Mac2String());

        HandleClient(basedev, dot11dev, in_pack, dot11info,
                pack_gpsinfo, pack_datainfo);
    } else if (dot11info->bssid_mac != basedev->get_macaddr() &&
            dot11info->distrib == distrib_to) {

        if (dot11info->bssid_mac != globalreg->broadcast_mac)
            dot11dev->set_last_bssid(dot11info->bssid_mac);

        basedev->bitset_basic_type_set(KIS_DEVICE_BASICTYPE_CLIENT);

        // If we're only a client, set the type name and device name
        if (basedev->get_basic_type_set() == KIS_DEVICE_BASICTYPE_CLIENT) {
            basedev->set_type_string("Wi-Fi Client");
            basedev->set_devicename(basedev->get_macaddr().Mac2String());
        }

        HandleClient(basedev, dot11dev, in_pack, dot11info,
                pack_gpsinfo, pack_datainfo);
    }

    // Look for WPA handshakes
    if (dot11info->type == packet_data) {
        shared_ptr<dot11_tracked_eapol> eapol = PacketDot11EapolHandshake(in_pack, dot11dev);

        // fprintf(stderr, "debug - data - eapol %p\n", eapol.get());

        if (eapol != NULL) {
            // Look for the AP of the exchange
            TrackedDeviceKey eapolkey(globalreg->server_uuid_hash, phyname_hash, 
                    dot11info->bssid_mac);
            shared_ptr<kis_tracked_device_base> eapolbase = devicetracker->FetchDevice(eapolkey);

            // Look for the target
            TrackedDeviceKey targetkey(globalreg->server_uuid_hash, phyname_hash, 
                    dot11info->dest_mac);
            shared_ptr<kis_tracked_device_base> targetbase = devicetracker->FetchDevice(targetkey);

            // fprintf(stderr, "debug - ebase %p tbase %p\n", eapolbase.get(), targetbase.get());

            // Look at BSSID records; we care about the handshake counts and want to
            // associate all the entries
            if (eapolbase != NULL) {
                local_locker eapoldevlocker(&(eapolbase->device_mutex));

                shared_ptr<dot11_tracked_device> eapoldot11 = 
                    static_pointer_cast<dot11_tracked_device>(eapolbase->get_map_value(dot11_device_entry_id));

                if (eapoldot11 != NULL) {
                    TrackerElementVector vec(eapoldot11->get_wpa_key_vec());

                    // Start doing something smart here about eliminating
                    // records - we want to do our best to keep a 1, 2, 3, 4
                    // handshake sequence, so find out what duplicates we have
                    // and eliminate the oldest one of them if we need to
                    uint8_t keymask = 0;

                    if (vec.size() > 16) {
                        for (TrackerElementVector::iterator kvi = vec.begin();
                                kvi != vec.end(); ++kvi) {
                            shared_ptr<dot11_tracked_eapol> ke =
                                static_pointer_cast<dot11_tracked_eapol>(*kvi);

                            uint8_t knum = (1 << ke->get_eapol_msg_num());

                            // If this is a duplicate handshake number, we can get
                            // rid of this one
                            if ((keymask & knum) == knum) {
                                vec.erase(kvi);
                                break;
                            }

                            // Otherwise put this key in the keymask
                            keymask |= knum;
                        }
                    }

                    vec.push_back(eapol);

                    // Calculate the key mask of seen handshake keys
                    keymask = 0;
                    for (TrackerElementVector::iterator kvi = vec.begin(); 
                            kvi != vec.end(); ++kvi) {
                        shared_ptr<dot11_tracked_eapol> ke =
                            static_pointer_cast<dot11_tracked_eapol>(*kvi);

                        keymask |= (1 << ke->get_eapol_msg_num());
                    }

                    eapoldot11->set_wpa_present_handshake(keymask);
                }
            }

            // Look for replays against the target (which might be the bssid, or might
            // be a client, depending on the direction); we track the EAPOL records per
            // destination MAC address
            if (targetbase != NULL) {
                local_locker targetdevlocker(&(targetbase->device_mutex));

                // Get the dot11 record for the destination device, if we can
                shared_ptr<dot11_tracked_device> eapoldot11 = 
                    static_pointer_cast<dot11_tracked_device>(targetbase->get_map_value(dot11_device_entry_id));

                if (eapoldot11 != NULL) {
                    bool dupe_nonce = false;
                    bool new_nonce = true;

                    // Look for replay attacks; only compare non-zero nonces
                    if (eapol->get_eapol_msg_num() == 3 &&
                            eapol->get_eapol_nonce().find_first_not_of(std::string("\x00", 1)) != string::npos) {
                        TrackerElementVector ev(eapoldot11->get_wpa_nonce_vec());
                        dupe_nonce = false;
                        new_nonce = true;

                        for (auto i : ev) {
                            std::shared_ptr<dot11_tracked_nonce> nonce =
                                std::static_pointer_cast<dot11_tracked_nonce>(i);

                            // If the nonce strings match
                            if (nonce->get_eapol_nonce() == eapol->get_eapol_nonce()) {
                                new_nonce = false;

                                if (eapol->get_eapol_replay_counter() <=
                                        nonce->get_eapol_replay_counter()) {

                                    // Is it an earlier (or equal) replay counter? Then we
                                    // have a problem; inspect the timestamp
                                    double tdif = 
                                        eapol->get_eapol_time() - 
                                        nonce->get_eapol_time();

                                    // Retries should fall w/in this range 
                                    if (tdif > 0.10f || tdif < -0.10f)
                                        dupe_nonce = true;
                                } else {
                                    // Otherwise increment the replay counter we record
                                    // for this nonce
                                    nonce->set_eapol_replay_counter(eapol->get_eapol_replay_counter());
                                }
                                break;
                            }
                        }

                        if (!dupe_nonce) {
                            if (new_nonce) {
                                std::shared_ptr<dot11_tracked_nonce> n = 
                                    eapoldot11->create_tracked_nonce();

                                n->set_from_eapol(eapol);

                                // Limit the size of stored nonces
                                if (ev.size() > 128)
                                    ev.erase(ev.begin());

                                ev.push_back(n);
                            }
                        } else {
                            std::stringstream ss;
                            std::string nonce = eapol->get_eapol_nonce();

                            for (size_t b = 0; b < nonce.length(); b++) {
                                ss << std::uppercase << std::setfill('0') << std::setw(2) <<
                                    std::hex << (int) (nonce[b] & 0xFF);
                            }

                            alertracker->RaiseAlert(alert_nonce_duplicate_ref, in_pack,
                                    dot11info->bssid_mac, dot11info->source_mac, 
                                    dot11info->dest_mac, dot11info->other_mac,
                                    commoninfo->channel,
                                    "WPA EAPOL RSN frame seen with a previously used nonce; "
                                    "this may indicate a KRACK-style WPA attack (nonce: " + 
                                    ss.str() + ")");
                        }
                    } else if (eapol->get_eapol_msg_num() == 1 &&
                            eapol->get_eapol_nonce().find_first_not_of(std::string("\x00", 1)) != string::npos) {
                        // Don't compare zero nonces
                        TrackerElementVector eav(eapoldot11->get_wpa_anonce_vec());
                        dupe_nonce = false;
                        new_nonce = true;

                        for (auto i : eav) {
                            std::shared_ptr<dot11_tracked_nonce> nonce =
                                std::static_pointer_cast<dot11_tracked_nonce>(i);

                            // If the nonce strings match
                            if (nonce->get_eapol_nonce() == eapol->get_eapol_nonce()) {
                                new_nonce = false;

                                if (eapol->get_eapol_replay_counter() <=
                                        nonce->get_eapol_replay_counter()) {
                                    // Is it an earlier (or equal) replay counter? Then we
                                    // have a problem; inspect the retry and timestamp
                                    if (dot11info->retry) {
                                        double tdif = 
                                            eapol->get_eapol_time() - 
                                            nonce->get_eapol_time();

                                        // Retries should fall w/in this range 
                                        if (tdif > 0.10f || tdif < -0.10f)
                                            dupe_nonce = true;
                                    } else {
                                        // Otherwise duplicate w/ out retry is immediately bad
                                        dupe_nonce = true;
                                    }
                                } else {
                                    // Otherwise increment the replay counter
                                    nonce->set_eapol_replay_counter(eapol->get_eapol_replay_counter());
                                }
                                break;
                            }
                        }

                        if (!dupe_nonce) {
                            if (new_nonce) {
                                std::shared_ptr<dot11_tracked_nonce> n = 
                                    eapoldot11->create_tracked_nonce();

                                n->set_from_eapol(eapol);

                                // Limit the size of stored nonces
                                if (eav.size() > 128)
                                    eav.erase(eav.begin());

                                eav.push_back(n);
                            }
                        } else {
                            std::stringstream ss;
                            std::string nonce = eapol->get_eapol_nonce();

                            for (size_t b = 0; b < nonce.length(); b++) {
                                ss << std::uppercase << std::setfill('0') << std::setw(2) <<
                                    std::hex << (int) (nonce[b] & 0xFF);
                            }

                            alertracker->RaiseAlert(alert_nonce_duplicate_ref, in_pack,
                                    dot11info->bssid_mac, dot11info->source_mac, 
                                    dot11info->dest_mac, dot11info->other_mac,
                                    commoninfo->channel,
                                    "WPA EAPOL RSN frame seen with a previously used anonce; "
                                    "this may indicate a KRACK-style WPA attack (anonce: " +
                                    ss.str() + ")");
                        }
                    }
                }
            }
        }
    }

	if (dot11info->type == packet_data &&
		dot11info->source_mac == dot11info->bssid_mac) {
		int wps = 0;
		string ssidchan = "0";
		string ssidtxt = "<Unknown>";
        TrackerElementIntMap ssidmap(dot11dev->get_advertised_ssid_map());

        for (TrackerElementIntMap::iterator si = ssidmap.begin();
                si != ssidmap.end(); ++si) {
            shared_ptr<dot11_advertised_ssid> ssid = 
                static_pointer_cast<dot11_advertised_ssid>(si->second);
            if (ssid->get_crypt_set() & crypt_wps) {
                ssidchan = ssid->get_channel();
                ssidtxt = ssid->get_ssid();
                break;
            }
        }

        wps = PacketDot11WPSM3(in_pack);

        if (wps) {
            // if we're w/in time of the last one, update, otherwise clear
            if (globalreg->timestamp.tv_sec - 
                    dot11dev->get_wps_m3_last() > (60 * 5))
                dot11dev->set_wps_m3_count(1);
            else
                dot11dev->inc_wps_m3_count(1);

            dot11dev->set_wps_m3_last(globalreg->timestamp.tv_sec);

            if (dot11dev->get_wps_m3_count() > 5) {
                if (alertracker->PotentialAlert(alert_wpsbrute_ref)) {
                    string al = "IEEE80211 AP '" + ssidtxt + "' (" + 
                        dot11info->bssid_mac.Mac2String() +
                        ") sending excessive number of WPS messages which may "
                        "indicate a WPS brute force attack such as Reaver";

                    alertracker->RaiseAlert(alert_wpsbrute_ref, 
                            in_pack, 
                            dot11info->bssid_mac, dot11info->source_mac, 
                            dot11info->dest_mac, dot11info->other_mac, 
                            ssidchan, al);
                }

                dot11dev->set_wps_m3_count(1);
            }
        }
    }

	if (dot11info->type == packet_management &&
		(dot11info->subtype == packet_sub_disassociation ||
		 dot11info->subtype == packet_sub_deauthentication) &&
		dot11info->dest_mac == globalreg->broadcast_mac &&
		alertracker->PotentialAlert(alert_bcastdcon_ref)) {

		string al = "IEEE80211 Access Point BSSID " +
			basedev->get_macaddr().Mac2String() + " broadcast deauthentication or "
			"disassociation of all clients, AP is shutting down or possible denial "
            "of service.";
			
        alertracker->RaiseAlert(alert_bcastdcon_ref, in_pack, 
                dot11info->bssid_mac, dot11info->source_mac, 
                dot11info->dest_mac, dot11info->other_mac, 
                dot11info->channel, al);
    }

    /*
    if (basedev->get_type_string().length() == 0) {
        fprintf(stderr, "debug - unclassed device as of packet %d typeset %lu\n", packetnum, basedev->get_basic_type_set());
    }
    */


#if 0


	if (ssid_new) {
		string printssid;
		string printssidext;
		string printcrypt;
		string printtype;
		string printdev;
		string printchan;
		string printmanuf;

		printssid = ssid->ssid;

		if (ssid->ssid_len == 0 || ssid->ssid == "") {
			if (ssid->type == dot11_ssid_probereq)  {
				printssid = "<Broadcast>";
				printssidext = " (probing for any SSID)";
			} else {
				printssid = "<Hidden SSID>";
			}
		}

		// commondev->name = printssid;

		if (ssid->ssid_cloaked) {
			printssidext = " (cloaked)";
		}

		if (ssid->type == dot11_ssid_beacon) {
			// commondev->name = printssid;

			printtype = "AP";

			if (ssid->cryptset) {
				printcrypt = "encrypted (" + CryptToString(ssid->cryptset) + ")";
			} else {
				printcrypt = "unencrypted";
			}

			printdev = "BSSID " + dot11info->bssid_mac.Mac2String();

			printchan = ", channel " + IntToString(ssid->channel);
		} else if (ssid->type == dot11_ssid_probereq) {
			printtype = "probing client";
			
			if (ssid->cryptset)
				printcrypt = "encrypted";
			else
				printcrypt = "unencrypted";

			printdev = "client " + dot11info->source_mac.Mac2String();
		} else if (ssid->type == dot11_ssid_proberesp) {
			printtype = "responding AP";

			if (ssid->cryptset)
				printcrypt = "encrypted";
			else
				printcrypt = "unencrypted";

			printdev = "BSSID " + dot11info->bssid_mac.Mac2String();
		} else {
			printtype = "unknown " + IntToString(ssid->type);
			printdev = "BSSID " + dot11info->bssid_mac.Mac2String();
		}

		if (commondev->manuf != "")
			printmanuf = " (" + commondev->manuf + ")";

		_MSG("Detected new 802.11 " + printtype + " SSID \"" + printssid + "\"" + 
			 printssidext + ", " + printdev + printmanuf + ", " + printcrypt + 
			 printchan,
			 MSGFLAG_INFO);

	} else if (net_new) {
		// If we didn't find a new SSID, and we found a network, talk about that
		string printcrypt;

		if (dot11info->cryptset)
			printcrypt = "encrypted";
		else
			printcrypt = "unencrypted";

		_MSG("Detected new 802.11 network BSSID " + dot11info->bssid_mac.Mac2String() +
			 ", " + printcrypt + ", no beacons seen yet", MSGFLAG_INFO);
	}

	if (dot11info->type == packet_management &&
		(dot11info->subtype == packet_sub_disassociation ||
		 dot11info->subtype == packet_sub_deauthentication) &&
		dot11info->dest_mac == globalreg->broadcast_mac &&
		globalreg->alertracker->PotentialAlert(alert_bcastdcon_ref) &&
		apdev != NULL) {

		string al = "IEEE80211 Access Point BSSID " +
			apdev->key.Mac2String() + " broadcast deauthentication or "
			"disassociation of all clients, probable denial of service";
			
		globalreg->alertracker->RaiseAlert(alert_bcastdcon_ref, in_pack, 
										   dot11info->bssid_mac, 
										   dot11info->source_mac, 
										   dot11info->dest_mac, 
										   dot11info->other_mac, 
										   dot11info->channel, al);
	}
#endif

	return 1;
}

string Kis_80211_Phy::CryptToString(uint64_t cryptset) {
	string ret;

	if (cryptset == crypt_none)
		return "none";

	if (cryptset == crypt_unknown)
		return "unknown";

	if (cryptset & crypt_wps)
		ret = "WPS";

	if ((cryptset & crypt_protectmask) == crypt_wep)
		return StringAppend(ret, "WEP");

	if (cryptset & crypt_wpa)
		ret = StringAppend(ret, "WPA");

	if (cryptset & crypt_psk)
		ret = StringAppend(ret, "WPA-PSK");

	if (cryptset & crypt_eap)
		ret = StringAppend(ret, "EAP");

	if (cryptset & crypt_peap)
		ret = StringAppend(ret, "WPA-PEAP");
	if (cryptset & crypt_leap)
		ret = StringAppend(ret, "WPA-LEAP");
	if (cryptset & crypt_ttls)
		ret = StringAppend(ret, "WPA-TTLS");
	if (cryptset & crypt_tls)
		ret = StringAppend(ret, "WPA-TLS");

	if (cryptset & crypt_wpa_migmode)
		ret = StringAppend(ret, "WPA-MIGRATION");

	if (cryptset & crypt_wep40)
		ret = StringAppend(ret, "WEP40");
	if (cryptset & crypt_wep104)
		ret = StringAppend(ret, "WEP104");
	if (cryptset & crypt_tkip)
		ret = StringAppend(ret, "TKIP");
	if (cryptset & crypt_aes_ocb)
		ret = StringAppend(ret, "AES-OCB");
	if (cryptset & crypt_aes_ccm)
		ret = StringAppend(ret, "AES-CCMP");

	if (cryptset & crypt_layer3)
		ret = StringAppend(ret, "Layer 3");

	if (cryptset & crypt_isakmp)
		ret = StringAppend(ret, "ISA KMP");

	if (cryptset & crypt_pptp)
		ret = StringAppend(ret, "PPTP");

	if (cryptset & crypt_fortress)
		ret = StringAppend(ret, "Fortress");

	if (cryptset & crypt_keyguard)
		ret = StringAppend(ret, "Keyguard");

	if (cryptset & crypt_unknown_protected)
		ret = StringAppend(ret, "L3/Unknown");

	if (cryptset & crypt_unknown_nonwep)
		ret = StringAppend(ret, "Non-WEP/Unknown");

	return ret;
}


bool Kis_80211_Phy::Httpd_VerifyPath(const char *path, const char *method) {
    if (strcmp(method, "GET") == 0) {
        vector<string> tokenurl = StrTokenize(path, "/");

        // we care about
        // /phy/phy80211/by-key/[key]/pcap/[mac]-handshake.pcap
        if (tokenurl.size() < 7)
            return false;

        if (tokenurl[1] != "phy")
            return false;

        if (tokenurl[2] != "phy80211")
            return false;

        if (tokenurl[3] != "by-key")
            return false;

        TrackedDeviceKey key(tokenurl[4]);
        if (key.get_error())
            return false;

        if (tokenurl[5] != "pcap")
            return false;

        // Valid requested file?
        if (tokenurl[6] != tokenurl[4] + "-handshake.pcap")
            return false;

        // Does it exist?
        if (devicetracker->FetchDevice(key) != NULL)
            return true;
    }

    return false;
}

void Kis_80211_Phy::GenerateHandshakePcap(shared_ptr<kis_tracked_device_base> dev, 
        Kis_Net_Httpd_Connection *connection, std::stringstream &stream) {
    // We need to make a temp file and then use that to make the pcap log
    int pcapfd, readfd;
    FILE *pcapw;

    pcap_t *pcaplogger;
    pcap_dumper_t *dumper;

    // Packet header
    struct pcap_pkthdr hdr;

    // Temp file name
    char tmpfname[PATH_MAX];

    snprintf(tmpfname, PATH_MAX, "/tmp/kismet_wpa_handshake_XXXXXX");

    // Can't do anything if we fail to make a pipe
    if ((pcapfd = mkstemp(tmpfname)) < 0) {
        _MSG("Failed to create a temporary handshake pcap file: " +
                kis_strerror_r(errno), MSGFLAG_ERROR);
        return;
    }

    // Open the tmp file
    readfd = open(tmpfname, O_RDONLY);
    // Immediately unlink it
    unlink(tmpfname);

    if ((pcapw = fdopen(pcapfd, "wb")) == NULL) {
        _MSG("Failed to open temp file for handshake pcap file: " +
                kis_strerror_r(errno), MSGFLAG_ERROR);
        close(readfd);
        return;
    }

    // We always open as 802.11 DLT because that's how we save the handshakes
    pcaplogger = pcap_open_dead(KDLT_IEEE802_11, 2000);
    dumper = pcap_dump_fopen(pcaplogger, pcapw);

    if (dev != NULL) {
        shared_ptr<dot11_tracked_device> dot11dev =
            static_pointer_cast<dot11_tracked_device>(dev->get_map_value(dot11_device_entry_id));

        if (dot11dev != NULL) {
            // Make a filename
            string dmac = dev->get_macaddr().Mac2String();
            std::replace(dmac.begin(), dmac.end(), ':', '-');

            string ssid = "";

            if (dot11dev->get_last_beaconed_ssid().length() != 0) 
                ssid = " " + dot11dev->get_last_beaconed_ssid();

            connection->optional_filename = "handshake " + dmac + ssid + ".pcap";

            TrackerElementVector hsvec(dot11dev->get_wpa_key_vec());

            for (TrackerElementVector::iterator i = hsvec.begin(); 
                    i != hsvec.end(); ++i) {
                shared_ptr<dot11_tracked_eapol> eapol = 
                    static_pointer_cast<dot11_tracked_eapol>(*i);

                shared_ptr<kis_tracked_packet> packet = eapol->get_eapol_packet();

                // Make a pcap header
                hdr.ts.tv_sec = packet->get_ts_sec();
                hdr.ts.tv_usec = packet->get_ts_usec();
                
                hdr.len = packet->get_data()->get_bytearray_size();
                hdr.caplen = hdr.len;

                // Dump the raw data
                pcap_dump((u_char *) dumper, &hdr, 
                        packet->get_data()->get_bytearray().get());
            }

        }

    }

    // Close the dumper
    pcap_dump_flush(dumper);
    pcap_dump_close(dumper);

    // Read our buffered stuff out into the stream
    char buf[128];
    size_t len;
    int total = 0;

    while ((len = read(readfd, buf, 128)) >= 0) {
        if (len == 0) {
            if (errno == EAGAIN || errno == EINTR)
                continue;
            break;
        }

        total += len;

        stream.write(buf, len);
    }

    // Pcapw and write pipe is already closed so just close read descriptor
    close(readfd);
}

void Kis_80211_Phy::Httpd_CreateStreamResponse(Kis_Net_Httpd *httpd,
        Kis_Net_Httpd_Connection *connection,
        const char *url, const char *method, const char *upload_data,
        size_t *upload_data_size, std::stringstream &stream) {

    if (strcmp(method, "GET") != 0) {
        return;
    }

    vector<string> tokenurl = StrTokenize(url, "/");

    // /phy/phy80211/by-key/[key]/pcap/[mac]-handshake.pcap
    if (tokenurl.size() < 7)
        return;

    if (tokenurl[1] != "phy")
        return;

    if (tokenurl[2] != "phy80211")
        return;

    if (tokenurl[3] != "by-key")
        return;

    TrackedDeviceKey key(tokenurl[4]);
    if (key.get_error()) {
        stream << "invalid mac";
        return;
    }

    if (tokenurl[5] != "pcap")
        return;

    // Valid requested file?
    if (tokenurl[6] != tokenurl[4] + "-handshake.pcap") {
        stream << "invalid file";
        return;
    }

    // Does it exist?
    auto dev = devicetracker->FetchDevice(key);

    if (dev == NULL) {
        stream << "unknown device";
        return;
    }

    // Validate the session and return a basic auth prompt
    if (httpd->HasValidSession(connection, true)) {
        // It should exist and we'll handle if it doesn't in the stream
        // handler
        local_locker devlocker(&(dev->device_mutex));
        GenerateHandshakePcap(devicetracker->FetchDevice(key), connection, stream);
    } else {
        stream << "Login required";
        return;
    }

    return;
}

int Kis_80211_Phy::Httpd_PostComplete(Kis_Net_Httpd_Connection *concls) {
    bool handled = false;

    string stripped = Httpd_StripSuffix(concls->url);

    // If we didn't handle it and got here, we don't know what it is, throw an
    // error.
    if (!handled) {
        concls->response_stream << "Invalid request";
        concls->httpcode = 400;
    } else {
        // Return a generic OK.  msgpack returns shouldn't get to here.
        concls->response_stream << "OK";
    }

    return 1;
}

class phy80211_devicetracker_expire_worker : public DevicetrackerFilterWorker {
public:
    phy80211_devicetracker_expire_worker(GlobalRegistry *in_globalreg, 
            unsigned int in_timeout, int entry_id) {
        globalreg = in_globalreg;
        dot11_device_entry_id = entry_id;
        timeout = in_timeout;
    }

    virtual ~phy80211_devicetracker_expire_worker() { }

    // Compare against our PCRE and export msgpack objects if we match
    virtual void MatchDevice(Devicetracker *devicetracker, 
            shared_ptr<kis_tracked_device_base> device) {

        shared_ptr<dot11_tracked_device> dot11dev =
            static_pointer_cast<dot11_tracked_device>(device->get_map_value(dot11_device_entry_id));

        // Not 802.11?  nothing we can do
        if (dot11dev == NULL) {
            return;
        }

        // Iterate over all the SSID records
        TrackerElementIntMap adv_ssid_map(dot11dev->get_advertised_ssid_map());
        shared_ptr<dot11_advertised_ssid> ssid = NULL;
        TrackerElementIntMap::iterator int_itr;

        for (int_itr = adv_ssid_map.begin(); int_itr != adv_ssid_map.end(); ++int_itr) {
            // Always leave one
            if (adv_ssid_map.size() <= 1)
                break;

            ssid = static_pointer_cast<dot11_advertised_ssid>(int_itr->second);

            if (globalreg->timestamp.tv_sec - ssid->get_last_time() > timeout) {
                fprintf(stderr, "debug - forgetting dot11ssid %s expiration %d\n", ssid->get_ssid().c_str(), timeout);
                adv_ssid_map.erase(int_itr);
                int_itr = adv_ssid_map.begin();
                devicetracker->UpdateFullRefresh();
            }
        }

        TrackerElementIntMap probe_map(dot11dev->get_probed_ssid_map());
        shared_ptr<dot11_probed_ssid> pssid = NULL;

        for (int_itr = probe_map.begin(); int_itr != probe_map.end(); ++int_itr) {
            // Always leave one
            if (probe_map.size() <= 1)
                break;

            pssid = static_pointer_cast<dot11_probed_ssid>(int_itr->second);

            if (globalreg->timestamp.tv_sec - pssid->get_last_time() > timeout) {
                fprintf(stderr, "debug - forgetting dot11probessid %s expiration %d\n", pssid->get_ssid().c_str(), timeout);
                probe_map.erase(int_itr);
                int_itr = probe_map.begin();
                devicetracker->UpdateFullRefresh();
            }
        }

        TrackerElementMacMap client_map(dot11dev->get_client_map());
        shared_ptr<dot11_client> client = NULL;
        TrackerElementMacMap::iterator mac_itr;

        for (mac_itr = client_map.begin(); mac_itr != client_map.end(); ++mac_itr) {
            // Always leave one
            if (client_map.size() <= 1)
                break;

            client = static_pointer_cast<dot11_client>(mac_itr->second);

            if (globalreg->timestamp.tv_sec - client->get_last_time() > timeout) {
                fprintf(stderr, "debug - forgetting client link from %s to %s expiration %d\n", device->get_macaddr().Mac2String().c_str(), mac_itr->first.Mac2String().c_str(), timeout);
                client_map.erase(mac_itr);
                mac_itr = client_map.begin();
                devicetracker->UpdateFullRefresh();
            }
        }
    }

protected:
    GlobalRegistry *globalreg;
    int dot11_device_entry_id;
    unsigned int timeout;
};

int Kis_80211_Phy::timetracker_event(int eventid) {
    // Spawn a worker to handle this
    if (eventid == device_idle_timer) {
        phy80211_devicetracker_expire_worker worker(globalreg,
                device_idle_expiration, dot11_device_entry_id);
        devicetracker->MatchOnDevices(&worker);
    }

    // Loop
    return 1;
}

void Kis_80211_Phy::LoadPhyStorage(SharedTrackerElement in_storage,
        SharedTrackerElement in_device) {
    if (in_storage == NULL || in_device == NULL)
        return;

    // Does the imported record have dot11?
    auto d11devi = in_storage->find(dot11_device_entry_id);

    // Adopt it into a dot11
    if (d11devi != in_storage->end()) {
        shared_ptr<dot11_tracked_device> d11dev(new dot11_tracked_device(globalreg, dot11_device_entry_id, d11devi->second));
        in_device->add_map(d11dev);
    }
}

