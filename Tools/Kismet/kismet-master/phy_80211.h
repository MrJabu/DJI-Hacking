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

   CODE IN BOTH phy_80211.cc AND phy_80211_dissectors.cc
   */

#ifndef __PHY_80211_H__
#define __PHY_80211_H__

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

#include "globalregistry.h"
#include "packetchain.h"
#include "timetracker.h"
#include "packet.h"
#include "gpstracker.h"
#include "uuid.h"

#include "devicetracker.h"
#include "devicetracker_component.h"
#include "kis_net_microhttpd.h"
#include "phy_80211_httpd_pcap.h"

#include "kaitai/kaitaistream.h"
#include "kaitai_parsers/wpaeap.h"
#include "kaitai_parsers/dot11_ie_11_qbss.h"
#include "kaitai_parsers/dot11_ie_54_mobility.h"
#include "kaitai_parsers/dot11_ie_61_ht.h"
#include "kaitai_parsers/dot11_ie_192_vht_operation.h"
#include "kaitai_parsers/dot11_ie_221_dji_droneid.h"

/*
 * 802.11 PHY handlers
 * Uses new devicetracker code
 *
 * Re-implements networktracker, packetdissectors
 * Ultimately all 802.11 related code will live here, such as alerts, etc.
 *
 * 802.11 data represents multiple tiers of data:
 *  - Device (could be client or AP)
 *      - AP
 *          - SSIDs (possibly multiple per BSSID)
 *          - AP Client
 *      - Client
 *          - SSIDs client has probed or connected to
 *          - BSSIDs of devices client has been observed joining/communicating
 */

#define PHY80211_MAC_LEN	6
// Dot11 SSID max len
#define DOT11_PROTO_SSID_LEN	32

// Wep keys
#define DOT11_WEPKEY_MAX		32
#define DOT11_WEPKEY_STRMAX		((DOT11_WEPKEY_MAX * 2) + DOT11_WEPKEY_MAX)

class dot11_wep_key {
    public:
        int fragile;
        mac_addr bssid;
        unsigned char key[DOT11_WEPKEY_MAX];
        unsigned int len;
        unsigned int decrypted;
        unsigned int failed;
};

// dot11 packet components

class dot11_packinfo_dot11d_entry {
    public:
        uint32_t startchan;
        uint32_t numchan;
        int32_t txpower;
};

// WPS state bitfield
#define DOT11_WPS_NO_WPS            0
#define DOT11_WPS_CONFIGURED        1
#define DOT11_WPS_NOT_CONFIGURED    (1 << 1)
#define DOT11_WPS_LOCKED            (1 << 2)

// SSID type bitfield
#define DOT11_SSID_NONE             0
#define DOT11_SSID_BEACON           1
#define DOT11_SSID_PROBERESP        (1 << 1)
#define DOT11_SSID_PROBEREQ         (1 << 2)
#define DOT11_SSID_FILE             (1 << 3)

// Packet info decoded by the dot11 phy decoder
// 
// Injected into the packet chain and processed later into the device records
class dot11_packinfo : public packet_component {
    public:
        dot11_packinfo() {
            self_destruct = 1; // Our delete() handles this
            corrupt = 0;
            header_offset = 0;
            type = packet_unknown;
            subtype = packet_sub_unknown;
            mgt_reason_code = 0;
            ssid_len = 0;
            ssid_blank = 0;
            source_mac = mac_addr(0);
            dest_mac = mac_addr(0);
            bssid_mac = mac_addr(0);
            other_mac = mac_addr(0);
            distrib = distrib_unknown;
            cryptset = 0;
            decrypted = 0;
            fuzzywep = 0;
            fmsweak = 0;
            ess = 0;
            ibss = 0;
            channel = "0";
            encrypted = 0;
            beacon_interval = 0;
            maxrate = 0;
            timestamp = 0;
            sequence_number = 0;
            frag_number = 0;
            fragmented = 0;
            retry = 0;
            duration = 0;
            datasize = 0;
            qos = 0;
            ssid_csum = 0;
            dot11d_country = "";
            ietag_csum = 0;
            wps = DOT11_WPS_NO_WPS;
            wps_manuf = "";
            wps_device_name = "";
            wps_model_name = "";
            wps_model_number = "";
        }

        // Corrupt 802.11 frame
        int corrupt;

        // Offset to data components in frame
        unsigned int header_offset;

        ieee_80211_type type;
        ieee_80211_subtype subtype;

        uint8_t mgt_reason_code;

        // Raw SSID
        string ssid;
        // Length of the SSID header field
        int ssid_len;
        // Is the SSID empty spaces?
        int ssid_blank;

        // Address set
        mac_addr source_mac;
        mac_addr dest_mac;
        mac_addr bssid_mac;
        mac_addr other_mac;

        ieee_80211_disttype distrib;

        uint64_t cryptset;
        int decrypted; // Might as well put this in here?
        int fuzzywep;
        int fmsweak;

        // Was it flagged as ess? (ap)
        int ess;
        int ibss;

        // What channel does it report
        std::string channel;

        // Is this encrypted?
        int encrypted;
        int beacon_interval;

        uint16_t qos;

        // Some cisco APs seem to fill in this info field
        string beacon_info;

        double maxrate;

        uint64_t timestamp;
        int sequence_number;
        int frag_number;
        int fragmented;
        int retry;

        int duration;

        int datasize;

        uint32_t ssid_csum;
        uint32_t ietag_csum;

        std::string dot11d_country;
        std::vector<dot11_packinfo_dot11d_entry> dot11d_vec;

        // WPS information
        uint8_t wps;
        // The field below is useful because some APs use
        // a MAC address with 'Unknown' OUI but will
        // tell their manufacturer in this field:
        std::string wps_manuf;
        // Some APs give out bogus information on these fields
        std::string wps_device_name;
        std::string wps_model_name;
        std::string wps_model_number;
        // There's also the serial number field but we don't care
        // about it because it's almost always bogus.

        // Direct kaitai structs pulled from the beacon
        std::shared_ptr<dot11_ie_11_qbss_t> qbss;
        std::shared_ptr<dot11_ie_54_mobility_t> dot11r_mobility;
        std::shared_ptr<dot11_ie_61_ht_t> dot11ht;
        std::shared_ptr<dot11_ie_192_vht_operation_t> dot11vht;

        std::shared_ptr<dot11_ie_221_dji_droneid_t> droneid;
};

class dot11_tracked_eapol : public tracker_component {
public:
    dot11_tracked_eapol(GlobalRegistry *in_globalreg, int in_id) :
        tracker_component(in_globalreg, in_id) {
            register_fields();
            reserve_fields(NULL);
        }

    dot11_tracked_eapol(GlobalRegistry *in_globalreg, int in_id,
            SharedTrackerElement e) : tracker_component(in_globalreg, in_id) {
        register_fields();
        reserve_fields(e);
    }

    virtual SharedTrackerElement clone_type() {
        return SharedTrackerElement(new dot11_tracked_eapol(globalreg, get_id()));
    }

    __Proxy(eapol_time, double, double, double, eapol_time);
    __Proxy(eapol_dir, uint8_t, uint8_t, uint8_t, eapol_dir);
    __Proxy(eapol_replay_counter, uint64_t, uint64_t, uint64_t, eapol_replay_counter);
    __Proxy(eapol_msg_num, uint8_t, uint8_t, uint8_t, eapol_msg_num);
    __Proxy(eapol_install, uint8_t, bool, bool, eapol_install);

    __ProxyOnlyTrackable(eapol_nonce, SharedTrackerElement, eapol_nonce);

    void set_eapol_nonce(std::string in_n) {
        eapol_nonce->set_bytearray(in_n);
    }

    std::string get_eapol_nonce() {
        return eapol_nonce->get_bytearray_str();
    }

    __ProxyTrackable(eapol_packet, kis_tracked_packet, eapol_packet);

protected:
    virtual void register_fields();
    virtual void reserve_fields(SharedTrackerElement e);

    SharedTrackerElement eapol_time;
    SharedTrackerElement eapol_dir;
    SharedTrackerElement eapol_replay_counter;
    SharedTrackerElement eapol_msg_num;

    SharedTrackerElement eapol_install;
    SharedTrackerElement eapol_nonce;

    shared_ptr<kis_tracked_packet> eapol_packet;
    int eapol_packet_id;
};

class dot11_tracked_nonce : public tracker_component {
public:
    dot11_tracked_nonce(GlobalRegistry *in_globalreg, int in_id) :
        tracker_component(in_globalreg, in_id) {
            register_fields();
            reserve_fields(NULL);
        }

    dot11_tracked_nonce(GlobalRegistry *in_globalreg, int in_id,
            SharedTrackerElement e) : tracker_component(in_globalreg, in_id) {
        register_fields();
        reserve_fields(e);
    }

    virtual SharedTrackerElement clone_type() {
        return SharedTrackerElement(new dot11_tracked_nonce(globalreg, get_id()));
    }

    __Proxy(eapol_time, double, double, double, eapol_time);
    __Proxy(eapol_msg_num, uint8_t, uint8_t, uint8_t, eapol_msg_num);
    __Proxy(eapol_install, uint8_t, bool, bool, eapol_install);
    __Proxy(eapol_replay_counter, uint64_t, uint64_t, uint64_t, eapol_replay_counter);

    void set_eapol_nonce(std::string in_n) {
        eapol_nonce->set_bytearray(in_n);
    }

    std::string get_eapol_nonce() {
        return eapol_nonce->get_bytearray_str();
    }

    void set_from_eapol(SharedTrackerElement in_tracked_eapol);

protected:
    virtual void register_fields();
    virtual void reserve_fields(SharedTrackerElement e);

    SharedTrackerElement eapol_time;
    SharedTrackerElement eapol_msg_num;

    SharedTrackerElement eapol_install;
    SharedTrackerElement eapol_nonce;

    SharedTrackerElement eapol_replay_counter;

};

class dot11_tracked_ssid_alert : public tracker_component {
public:
    dot11_tracked_ssid_alert(GlobalRegistry *in_globalreg, int in_id) :
        tracker_component(in_globalreg, in_id) {

#ifdef HAVE_LIBPCRE
            ssid_re = NULL;
            ssid_study = NULL;
#endif

            register_fields();
            reserve_fields(NULL);
        }

    dot11_tracked_ssid_alert(GlobalRegistry *in_globalreg, int in_id,
            SharedTrackerElement e) : tracker_component(in_globalreg, in_id) {
#ifdef HAVE_LIBCPRE
        ssid_re = NULL;
        ssid_study = NULL;
#endif

        register_fields();
        reserve_fields(e);
    }

    virtual ~dot11_tracked_ssid_alert() {
#ifdef HAVE_LIBCPRE
        if (ssid_re != NULL)
            pcre_free(ssid_re);
        if (ssid_study != NULL)
            pcre_free(ssid_study);
#endif
    }

    virtual SharedTrackerElement clone_type() {
        return SharedTrackerElement(new dot11_tracked_ssid_alert(globalreg, get_id()));
    }

    __Proxy(group_name, std::string, std::string, std::string, ssid_group_name);

    // Control the regex.  MAY THROW std::runtime_error if the regex is invalid
    __ProxyGet(regex, std::string, std::string, ssid_regex);
    void set_regex(std::string s);

    __ProxyOnlyTrackable(allowed_macs_vec, SharedTrackerElement, allowed_macs_vec);

    void set_allowed_macs(std::vector<mac_addr> mvec);

    bool compare_ssid(std::string ssid, mac_addr mac);

protected:
    kis_recursive_timed_mutex ssid_mutex;

    virtual void register_fields();

    SharedTrackerElement ssid_group_name;
    SharedTrackerElement ssid_regex;
    SharedTrackerElement allowed_macs_vec;
    int allowed_mac_id;

#ifdef HAVE_LIBPCRE
    pcre *ssid_re;
    pcre_extra *ssid_study;
#endif
};

class dot11_11d_tracked_range_info : public tracker_component {
    public:
        dot11_11d_tracked_range_info(GlobalRegistry *in_globalreg, int in_id) :
            tracker_component(in_globalreg, in_id) {
                register_fields();
                reserve_fields(NULL);
            }

        dot11_11d_tracked_range_info(GlobalRegistry *in_globalreg, int in_id, 
                SharedTrackerElement e) : tracker_component(in_globalreg, in_id) {
            register_fields();
            reserve_fields(e);
        }

        virtual SharedTrackerElement clone_type() {
            return SharedTrackerElement(new dot11_11d_tracked_range_info(globalreg, get_id()));
        }


        __Proxy(startchan, uint32_t, uint32_t, uint32_t, startchan);
        __Proxy(numchan, uint32_t, unsigned int, unsigned int, numchan);
        __Proxy(txpower, int32_t, int, int, txpower);

    protected:
        virtual void register_fields() {
            RegisterField("dot11.11d.start_channel", TrackerUInt32,
                    "Starting channel of 11d range", &startchan);
            RegisterField("dot11.11d.num_channels", TrackerUInt32,
                    "Number of channels covered by range", &numchan);
            RegisterField("dot11.11d.tx_power", TrackerInt32,
                    "Maximum allowed transmit power", &txpower);
        }

        SharedTrackerElement startchan;
        SharedTrackerElement numchan;
        SharedTrackerElement txpower;
};

class dot11_probed_ssid : public tracker_component {
    public:
        dot11_probed_ssid(GlobalRegistry *in_globalreg, int in_id) : 
            tracker_component(in_globalreg, in_id) { 
                register_fields();
                reserve_fields(NULL);
            } 

        dot11_probed_ssid(GlobalRegistry *in_globalreg, int in_id, SharedTrackerElement e) : 
            tracker_component(in_globalreg, in_id) {

                register_fields();
                reserve_fields(e);
            }

        virtual SharedTrackerElement clone_type() {
            return SharedTrackerElement(new dot11_probed_ssid(globalreg, get_id()));
        }

        __Proxy(ssid, string, string, string, ssid);
        __Proxy(ssid_len, uint32_t, unsigned int, unsigned int, ssid_len);
        __Proxy(bssid, mac_addr, mac_addr, mac_addr, bssid);
        __Proxy(first_time, uint64_t, time_t, time_t, first_time);
        __Proxy(last_time, uint64_t, time_t, time_t, last_time);

        __ProxyDynamicTrackable(location, kis_tracked_location, location, location_id);

        __Proxy(dot11r_mobility, uint8_t, bool, bool, dot11r_mobility);
        __Proxy(dot11r_mobility_domain_id, uint16_t, uint16_t, uint16_t, 
                dot11r_mobility_domain_id);

    protected:
        virtual void register_fields() {
            RegisterField("dot11.probedssid.ssid", TrackerString,
                    "probed ssid string (sanitized)", &ssid);
            RegisterField("dot11.probedssid.ssidlen", TrackerUInt32,
                    "probed ssid string length (original bytes)", &ssid_len);
            RegisterField("dot11.probedssid.bssid", TrackerMac,
                    "probed ssid BSSID", &bssid);
            RegisterField("dot11.probedssid.first_time", TrackerUInt64,
                    "first time probed", &first_time);
            RegisterField("dot11.probedssid.last_time", TrackerUInt64,
                    "last time probed", &last_time);

            __RegisterComplexField(kis_tracked_location, location_id, 
                    "dot11.probedssid.location", "location");

            RegisterField("dot11.probedssid.dot11r_mobility", TrackerUInt8,
                    "advertised dot11r mobility support", &dot11r_mobility);
            RegisterField("dot11.probedssid.dot11r_mobility_domain_id", TrackerUInt16,
                    "advertised dot11r mobility domain id", &dot11r_mobility_domain_id);
        }

        virtual void reserve_fields(SharedTrackerElement e) {
            tracker_component::reserve_fields(e);

            if (e != NULL) {
                location.reset(new kis_tracked_location(globalreg, location_id, 
                            e->get_map_value(location_id)));
            }

            add_map(location_id, location);
        }

        SharedTrackerElement ssid;
        SharedTrackerElement ssid_len;
        SharedTrackerElement bssid;
        SharedTrackerElement first_time;
        SharedTrackerElement last_time;

        SharedTrackerElement dot11r_mobility;
        SharedTrackerElement dot11r_mobility_domain_id;

        int location_id;
        shared_ptr<kis_tracked_location> location;
};

/* Advertised SSID
 *
 * SSID advertised by a device via beacon or probe response
 */
class dot11_advertised_ssid : public tracker_component {
    public:
        dot11_advertised_ssid(GlobalRegistry *in_globalreg, int in_id) : 
            tracker_component(in_globalreg, in_id) { 
                register_fields();
                reserve_fields(NULL);
            } 

        dot11_advertised_ssid(GlobalRegistry *in_globalreg, int in_id, 
                SharedTrackerElement e) : 
            tracker_component(in_globalreg, in_id) {

                register_fields();
                reserve_fields(e);
            }

        virtual SharedTrackerElement clone_type() {
            return SharedTrackerElement(new dot11_advertised_ssid(globalreg, get_id()));
        }

        __Proxy(ssid, string, string, string, ssid);
        __Proxy(ssid_len, uint32_t, unsigned int, unsigned int, ssid_len);

        __Proxy(ssid_beacon, uint8_t, bool, bool, ssid_beacon);
        __Proxy(ssid_probe_response, uint8_t, bool, bool, ssid_probe_response);

        __Proxy(channel, std::string, std::string, std::string, channel);
        __Proxy(ht_mode, std::string, std::string, std::string, ht_mode);
        __Proxy(ht_center_1, uint64_t, uint64_t, uint64_t, ht_center_1);
        __Proxy(ht_center_2, uint64_t, uint64_t, uint64_t, ht_center_2);

        __Proxy(first_time, uint64_t, time_t, time_t, first_time);
        __Proxy(last_time, uint64_t, time_t, time_t, last_time);

        __Proxy(beacon_info, string, string, string, beacon_info);

        __Proxy(ssid_cloaked, uint8_t, bool, bool, ssid_cloaked);

        __Proxy(crypt_set, uint64_t, uint64_t, uint64_t, crypt_set);

        __Proxy(maxrate, uint64_t, uint64_t, uint64_t, maxrate);

        __Proxy(beaconrate, uint32_t, uint32_t, uint32_t, beaconrate);
        __Proxy(beacons_sec, uint32_t, uint32_t, uint32_t, beacons_sec);
        __ProxyIncDec(beacons_sec, uint32_t, uint32_t, beacons_sec);

        __Proxy(ietag_checksum, uint32_t, uint32_t, uint32_t, ietag_checksum);

        __Proxy(dot11d_country, string, string, string, dot11d_country);

        __ProxyTrackable(dot11d_vec, TrackerElement, dot11d_vec);

        void set_dot11d_vec(vector<dot11_packinfo_dot11d_entry> vec) {
            TrackerElementVector d11vec(get_dot11d_vec());

            d11vec.clear();

            for (unsigned int x = 0; x < vec.size(); x++) {
                shared_ptr<dot11_11d_tracked_range_info> ri(new dot11_11d_tracked_range_info(globalreg, dot11d_country_entry_id));
                ri->set_startchan(vec[x].startchan);
                ri->set_numchan(vec[x].numchan);
                ri->set_txpower(vec[x].txpower);

                d11vec.push_back(ri);
            }
        }

        __Proxy(wps_state, uint32_t, uint32_t, uint32_t, wps_state);
        __Proxy(wps_manuf, string, string, string, wps_manuf);
        __Proxy(wps_device_name, string, string, string, wps_device_name);
        __Proxy(wps_model_name, string, string, string, wps_model_name);
        __Proxy(wps_model_number, string, string, string, wps_model_number);

        __ProxyDynamicTrackable(location, kis_tracked_location, location, location_id);

        __Proxy(dot11r_mobility, uint8_t, bool, bool, dot11r_mobility);
        __Proxy(dot11r_mobility_domain_id, uint16_t, uint16_t, uint16_t, 
                dot11r_mobility_domain_id);

        __Proxy(dot11e_qbss, uint8_t, bool, bool, dot11e_qbss);
        __Proxy(dot11e_qbss_stations, uint16_t, uint16_t, uint16_t, dot11e_qbss_stations);
        __Proxy(dot11e_qbss_channel_load, double, double, double, dot11e_qbss_channel_load);

    protected:
        virtual void register_fields() {
            RegisterField("dot11.advertisedssid.ssid", TrackerString,
                    "probed ssid string (sanitized)", &ssid);
            RegisterField("dot11.advertisedssid.ssidlen", TrackerUInt32,
                    "probed ssid string length (original bytes)", &ssid_len);
            RegisterField("dot11.advertisedssid.beacon", TrackerUInt8,
                    "ssid advertised via beacon", &ssid_beacon);
            RegisterField("dot11.advertisedssid.probe_response", TrackerUInt8,
                    "ssid advertised via probe response", 
                    &ssid_probe_response);

            RegisterField("dot11.advertisedssid.channel", TrackerString,
                    "channel", &channel);
            RegisterField("dot11.advertisedssid.ht_mode", TrackerString,
                    "HT (11n or 11ac) mode", &ht_mode);
            RegisterField("dot11.advertisedssid.ht_center_1", TrackerUInt64,
                    "HT/VHT Center Frequency (primary)", &ht_center_1);
            RegisterField("dot11.advertisedssid.ht_center_2", TrackerUInt64,
                    "HT/VHT Center Frequency (secondary, for 80+80 Wave2)",
                    &ht_center_2);

            RegisterField("dot11.advertisedssid.first_time", TrackerUInt64,
                    "first time seen", &first_time);
            RegisterField("dot11.advertisedssid.last_time", TrackerUInt64,
                    "last time seen", &last_time);
            RegisterField("dot11.advertisedssid.beacon_info", TrackerString,
                    "beacon info / vendor description", &beacon_info);
            RegisterField("dot11.advertisedssid.cloaked", TrackerUInt8,
                    "SSID is hidden / cloaked", &ssid_cloaked);
            RegisterField("dot11.advertisedssid.crypt_set", TrackerUInt64,
                    "bitfield of encryption options", &crypt_set);
            RegisterField("dot11.advertisedssid.maxrate", TrackerUInt64,
                    "advertised maximum rate", &maxrate);
            RegisterField("dot11.advertisedssid.beaconrate", TrackerUInt32,
                    "beacon rate", &beaconrate);
            RegisterField("dot11.advertisedssid.beacons_sec", TrackerUInt32,
                    "beacons seen in past second", &beacons_sec);
            RegisterField("dot11.advertisedssid.ietag_checksum", TrackerUInt32,
                    "checksum of all ie tags", &ietag_checksum);
            RegisterField("dot11.advertisedssid.dot11d_country", TrackerString,
                    "802.11d country", &dot11d_country);

            RegisterField("dot11.advertisedssid.dot11d_list", TrackerVector,
                    "802.11d channel list", &dot11d_vec);

            __RegisterComplexField(dot11_11d_tracked_range_info, dot11d_country_entry_id, 
                    "dot11.advertisedssid.dot11d_entry", "dot11d entry");

            RegisterField("dot11.advertisedssid.wps_state", TrackerUInt32,
                    "bitfield wps state", &wps_state);
            RegisterField("dot11.advertisedssid.wps_manuf", TrackerString,
                    "WPS manufacturer", &wps_manuf);
            RegisterField("dot11.advertisedssid.wps_device_name", TrackerString,
                    "wps device name", &wps_device_name);
            RegisterField("dot11.advertisedssid.wps_model_name", TrackerString,
                    "wps model name", &wps_model_name);
            RegisterField("dot11.advertisedssid.wps_model_number", TrackerString,
                    "wps model number", &wps_model_number);

            __RegisterComplexField(kis_tracked_location, location_id, 
                    "dot11.advertisedssid.location", "location");

            RegisterField("dot11.advertisedssid.dot11r_mobility", TrackerUInt8,
                    "advertised dot11r mobility support", &dot11r_mobility);
            RegisterField("dot11.advertisedssid.dot11r_mobility_domain_id", TrackerUInt16,
                    "advertised dot11r mobility domain id", &dot11r_mobility_domain_id);

            RegisterField("dot11.advertisedssid.dot11e_qbss", TrackerUInt8,
                    "SSID advertises 802.11e QBSS", &dot11e_qbss);
            RegisterField("dot11.advertisedssid.dot11e_qbss_stations", TrackerUInt16,
                    "802.11e QBSS station count", &dot11e_qbss_stations);
            RegisterField("dot11.advertisedssid.dot11e_channel_utilization_perc", TrackerDouble,
                    "802.11e QBSS reported channel utilization, as percentage", 
                    &dot11e_qbss_channel_load);

        }

        virtual void reserve_fields(SharedTrackerElement e) {
            tracker_component::reserve_fields(e);

            if (e != NULL) {
                location.reset(new kis_tracked_location(globalreg, location_id, 
                            e->get_map_value(location_id)));

                // If we're inheriting, it's our responsibility to kick submaps and vectors with
                // complex types as well; since they're not themselves complex objects
                TrackerElementVector dvec(dot11d_vec);
                for (auto d = dvec.begin(); d != dvec.end(); ++d) {
                    std::shared_ptr<dot11_11d_tracked_range_info> din(new dot11_11d_tracked_range_info(globalreg, dot11d_country_entry_id, *d));

                    // And assign it over the same key
                    *d = std::static_pointer_cast<TrackerElement>(din);
                }
            }

            add_map(location_id, location);
        }

        SharedTrackerElement ssid;
        SharedTrackerElement ssid_len;
        SharedTrackerElement ssid_beacon;
        SharedTrackerElement ssid_probe_response;
        SharedTrackerElement channel;
        SharedTrackerElement ht_mode;
        SharedTrackerElement ht_center_1;
        SharedTrackerElement ht_center_2;
        SharedTrackerElement first_time;
        SharedTrackerElement last_time;
        SharedTrackerElement beacon_info;
        SharedTrackerElement ssid_cloaked;
        SharedTrackerElement crypt_set;
        SharedTrackerElement maxrate;
        SharedTrackerElement beaconrate;
        SharedTrackerElement beacons_sec;
        SharedTrackerElement ietag_checksum;

        // IE tag dot11d country / power restrictions from 802.11d; 
        // deprecated but still in use
        SharedTrackerElement dot11d_country;
        SharedTrackerElement dot11d_vec;

        // dot11d vec component reference
        int dot11d_country_entry_id;

        // 802.11r mobility/fast roaming advertisements
        SharedTrackerElement dot11r_mobility;
        SharedTrackerElement dot11r_mobility_domain_id;

        // 802.11e QBSS
        SharedTrackerElement dot11e_qbss;
        SharedTrackerElement dot11e_qbss_stations;
        SharedTrackerElement dot11e_qbss_channel_load;

        // WPS components
        SharedTrackerElement wps_state;
        SharedTrackerElement wps_manuf;
        SharedTrackerElement wps_device_name;
        SharedTrackerElement wps_model_name;
        SharedTrackerElement wps_model_number;

        int location_id;
        shared_ptr<kis_tracked_location> location;
};

/* dot11 client
 *
 * Observed behavior as a client of a bssid.  Multiple records may exist
 * if this device has behaved as a client for multiple BSSIDs
 *
 */
class dot11_client : public tracker_component {
    public:
        dot11_client(GlobalRegistry *in_globalreg, int in_id) :
            tracker_component(in_globalreg, in_id) {
                register_fields();
                reserve_fields(NULL);
            }

        dot11_client(GlobalRegistry *in_globalreg, int in_id, SharedTrackerElement e) :
            tracker_component(in_globalreg, in_id) {
                register_fields();
                reserve_fields(e);
            }

        virtual SharedTrackerElement clone_type() {
            return SharedTrackerElement(new dot11_client(globalreg, get_id()));
        }

        __Proxy(bssid, mac_addr, mac_addr, mac_addr, bssid);
        __Proxy(bssid_key, TrackedDeviceKey, TrackedDeviceKey, TrackedDeviceKey, bssid_key);
        __Proxy(client_type, uint32_t, uint32_t, uint32_t, client_type);

        __Proxy(first_time, uint64_t, time_t, time_t, first_time);
        __Proxy(last_time, uint64_t, time_t, time_t, last_time);

        __Proxy(dhcp_host, string, string, string, dhcp_host);
        __Proxy(dhcp_vendor, string, string, string, dhcp_vendor);

        __Proxy(tx_cryptset, uint64_t, uint64_t, uint64_t, tx_cryptset);
        __Proxy(rx_cryptset, uint64_t, uint64_t, uint64_t, rx_cryptset);

        __Proxy(eap_identity, string, string, string, eap_identity);

        __Proxy(cdp_device, string, string, string, cdp_device);
        __Proxy(cdp_port, string, string, string, cdp_port);

        __Proxy(decrypted, uint8_t, bool, bool, decrypted);

        __ProxyDynamicTrackable(ipdata, kis_tracked_ip_data, ipdata, ipdata_id);

        __Proxy(datasize, uint64_t, uint64_t, uint64_t, datasize);
        __ProxyIncDec(datasize, uint64_t, uint64_t, datasize);

        __Proxy(datasize_retry, uint64_t, uint64_t, uint64_t, datasize_retry);
        __ProxyIncDec(datasize_retry, uint64_t, uint64_t, datasize_retry);

        __Proxy(num_fragments, uint64_t, uint64_t, uint64_t, num_fragments);
        __ProxyIncDec(num_fragments, uint64_t, uint64_t, num_fragments);

        __Proxy(num_retries, uint64_t, uint64_t, uint64_t, num_retries);
        __ProxyIncDec(num_retries, uint64_t, uint64_t, num_retries);

        __ProxyDynamicTrackable(location, kis_tracked_location, location, location_id);

    protected:
        virtual void register_fields() {
            RegisterField("dot11.client.bssid", TrackerMac,
                    "bssid", &bssid);
            RegisterField("dot11.client.bssid_key", TrackerKey,
                    "key of BSSID record", &bssid_key);
            RegisterField("dot11.client.first_time", TrackerUInt64,
                    "first time seen", &first_time);
            RegisterField("dot11.client.last_time", TrackerUInt64,
                    "last time seen", &last_time);
            RegisterField("dot11.client.type", TrackerUInt32,
                    "type of client", &client_type);
            RegisterField("dot11.client.dhcp_host", TrackerString,
                    "dhcp host", &dhcp_host);
            RegisterField("dot11.client.dhcp_vendor", TrackerString,
                    "dhcp vendor", &dhcp_vendor);
            RegisterField("dot11.client.tx_cryptset", TrackerUInt64,
                    "bitset of transmitted encryption",
                    &tx_cryptset);
            RegisterField("dot11.client.rx_cryptset", TrackerUInt64,
                    "bitset of received enryption",
                    &rx_cryptset);
            RegisterField("dot11.client.eap_identity", TrackerString,
                    "EAP identity", &eap_identity);
            RegisterField("dot11.client.cdp_device", TrackerString,
                    "CDP device", &cdp_device);
            RegisterField("dot11.client.cdp_port", TrackerString,
                    "CDP port", &cdp_port);
            RegisterField("dot11.client.decrypted", TrackerUInt8,
                    "client decrypted", &decrypted);

            __RegisterComplexField(kis_tracked_ip_data, ipdata_id, 
                    "dot11.client.ipdata", "IP");

            RegisterField("dot11.client.datasize", TrackerUInt64,
                    "data in bytes", &datasize);
            RegisterField("dot11.client.datasize_retry", TrackerUInt64,
                    "retry data in bytes", &datasize_retry);
            RegisterField("dot11.client.num_fragments", TrackerUInt64,
                    "number of fragmented packets", &num_fragments);
            RegisterField("dot11.client.num_retries", TrackerUInt64,
                    "number of retried packets", &num_retries);

            __RegisterComplexField(kis_tracked_location, location_id, 
                    "client.location", "location");
        }

        virtual void reserve_fields(SharedTrackerElement e) {
            tracker_component::reserve_fields(e);

            if (e != NULL) {
                ipdata.reset(new kis_tracked_ip_data(globalreg, ipdata_id, 
                            e->get_map_value(ipdata_id)));
                location.reset(new kis_tracked_location(globalreg, location_id, 
                            e->get_map_value(location_id)));
            }

            add_map(ipdata_id, ipdata);
            add_map(location_id, location);
        }


        SharedTrackerElement bssid;
        SharedTrackerElement bssid_key;
        SharedTrackerElement first_time;
        SharedTrackerElement last_time;
        SharedTrackerElement client_type;
        SharedTrackerElement dhcp_host;
        SharedTrackerElement dhcp_vendor;
        SharedTrackerElement tx_cryptset;
        SharedTrackerElement rx_cryptset;
        SharedTrackerElement eap_identity;
        SharedTrackerElement cdp_device;
        SharedTrackerElement cdp_port;
        SharedTrackerElement decrypted;

        int ipdata_id;
        shared_ptr<kis_tracked_ip_data> ipdata;

        SharedTrackerElement datasize;
        SharedTrackerElement datasize_retry;
        SharedTrackerElement num_fragments;
        SharedTrackerElement num_retries;

        int location_id;
        shared_ptr<kis_tracked_location> location;
};

// Bitset of top-level device types for easy sorting/browsing
#define DOT11_DEVICE_TYPE_UNKNOWN       0
// This device has beaconed
#define DOT11_DEVICE_TYPE_BEACON_AP     1
// This device has acted like an adhoc device
#define DOT11_DEVICE_TYPE_ADHOC         (1 << 1)
// This device has acted like a client
#define DOT11_DEVICE_TYPE_CLIENT        (1 << 2)
// This device appears to be a wired device bridged to wifi
#define DOT11_DEVICE_TYPE_WIRED         (1 << 3)
// WDS distribution network
#define DOT11_DEVICE_TYPE_WDS           (1 << 4)
// Old-school turbocell
#define DOT11_DEVICE_TYPE_TURBOCELL     (1 << 5)
// We haven't seen this device directly but we're guessing it's there
// because something has talked to it over wireless (ie, cts or ack to it)
#define DOT11_DEVICE_TYPE_INFERRED_WIRELESS (1 << 6)
// We haven't seen this device directly but we've seen something talking to it
#define DOT11_DEVICE_TYPE_INFERRED_WIRED    (1 << 7)

// Dot11 device
//
// Device-level data, additional data stored in the client and ssid arrays
class dot11_tracked_device : public tracker_component {
    public:
        dot11_tracked_device(GlobalRegistry *in_globalreg, int in_id) :
            tracker_component(in_globalreg, in_id) { 
                register_fields();
                reserve_fields(NULL);
            }

        virtual SharedTrackerElement clone_type() {
            return SharedTrackerElement(new dot11_tracked_device(globalreg, get_id()));
        }

        dot11_tracked_device(GlobalRegistry *in_globalreg, int in_id, 
                SharedTrackerElement e) :
            tracker_component(in_globalreg, in_id) {
                register_fields();
                reserve_fields(e);
            }

        static void attach_base_parent(shared_ptr<dot11_tracked_device> self, 
                shared_ptr<kis_tracked_device_base> parent) {
            parent->add_map(self);
        }

        __Proxy(type_set, uint64_t, uint64_t, uint64_t, type_set);
        __ProxyBitset(type_set, uint64_t, type_set);

        __ProxyTrackable(client_map, TrackerElement, client_map);
        shared_ptr<dot11_client> new_client() {
            return shared_ptr<dot11_client>(new dot11_client(globalreg, client_map_entry_id));
        }

        __ProxyTrackable(advertised_ssid_map, TrackerElement, advertised_ssid_map);
        shared_ptr<dot11_advertised_ssid> new_advertised_ssid() {
            return shared_ptr<dot11_advertised_ssid>(new dot11_advertised_ssid(globalreg, advertised_ssid_map_entry_id));
        }

        __ProxyTrackable(probed_ssid_map, TrackerElement, probed_ssid_map);
        shared_ptr<dot11_probed_ssid> new_probed_ssid() {
            return shared_ptr<dot11_probed_ssid>(new dot11_probed_ssid(globalreg, probed_ssid_map_entry_id));
        }

        __ProxyTrackable(associated_client_map, TrackerElement, associated_client_map);

        __Proxy(client_disconnects, uint64_t, uint64_t, uint64_t, client_disconnects);
        __ProxyIncDec(client_disconnects, uint64_t, uint64_t, client_disconnects);

        __Proxy(last_sequence, uint64_t, uint64_t, uint64_t, last_sequence);
        __Proxy(bss_timestamp, uint64_t, uint64_t, uint64_t, bss_timestamp);

        __Proxy(num_fragments, uint64_t, uint64_t, uint64_t, num_fragments);
        __ProxyIncDec(num_fragments, uint64_t, uint64_t, num_fragments);

        __Proxy(num_retries, uint64_t, uint64_t, uint64_t, num_retries);
        __ProxyIncDec(num_retries, uint64_t, uint64_t, num_retries);

        __Proxy(datasize, uint64_t, uint64_t, uint64_t, datasize);
        __ProxyIncDec(datasize, uint64_t, uint64_t, datasize);

        __Proxy(datasize_retry, uint64_t, uint64_t, uint64_t, datasize_retry);
        __ProxyIncDec(datasize_retry, uint64_t, uint64_t, datasize_retry);

        __Proxy(last_bssid, mac_addr, mac_addr, mac_addr, last_bssid);

        __Proxy(last_probed_ssid, string, string, string, last_probed_ssid);
        __Proxy(last_probed_ssid_csum, uint32_t, uint32_t, 
                uint32_t, last_probed_ssid_csum);

        __Proxy(last_beaconed_ssid, string, string, string, last_beaconed_ssid);
        __Proxy(last_beaconed_ssid_csum, uint32_t, uint32_t, 
                uint32_t, last_beaconed_ssid_csum);

        __Proxy(last_beacon_timestamp, uint64_t, time_t, 
                time_t, last_beacon_timestamp);

        __Proxy(wps_m3_count, uint64_t, uint64_t, uint64_t, wps_m3_count);
        __ProxyIncDec(wps_m3_count, uint64_t, uint64_t, wps_m3_count);

        __Proxy(wps_m3_last, uint64_t, uint64_t, uint64_t, wps_m3_last);

        __ProxyTrackable(wpa_key_vec, TrackerElement, wpa_key_vec);
        shared_ptr<dot11_tracked_eapol> create_eapol_packet() {
            return static_pointer_cast<dot11_tracked_eapol>(entrytracker->GetTrackedInstance(wpa_key_entry_id));
        }

        __Proxy(wpa_present_handshake, uint8_t, uint8_t, uint8_t, wpa_present_handshake);

        __ProxyTrackable(wpa_nonce_vec, TrackerElement, wpa_nonce_vec);
        __ProxyTrackable(wpa_anonce_vec, TrackerElement, wpa_anonce_vec);
        shared_ptr<dot11_tracked_nonce> create_tracked_nonce() {
            return static_pointer_cast<dot11_tracked_nonce>(entrytracker->GetTrackedInstance(wpa_nonce_entry_id));
        }

    protected:
        virtual void register_fields() {
            RegisterField("dot11.device.typeset", TrackerUInt64,
                    "bitset of device type", &type_set);

            RegisterField("dot11.device.client_map", TrackerMacMap,
                    "client behavior", &client_map);

            shared_ptr<dot11_client> client_builder(new dot11_client(globalreg, 0));
            client_map_entry_id =
                RegisterComplexField("dot11.device.client", client_builder, "client record");

            // Advertised SSIDs keyed by ssid checksum
            RegisterField("dot11.device.advertised_ssid_map", TrackerIntMap,
                    "advertised SSIDs", &advertised_ssid_map);

            shared_ptr<dot11_advertised_ssid> adv_ssid_builder(new dot11_advertised_ssid(globalreg, 0));
            advertised_ssid_map_entry_id =
                RegisterComplexField("dot11.device.advertised_ssid",
                        adv_ssid_builder, "advertised ssid");

            // Probed SSIDs keyed by int checksum
            RegisterField("dot11.device.probed_ssid_map", TrackerIntMap,
                    "probed SSIDs", &probed_ssid_map);

            shared_ptr<dot11_probed_ssid> probe_ssid_builder(new dot11_probed_ssid(globalreg, 0));
            probed_ssid_map_entry_id =
                RegisterComplexField("dot11.device.probed_ssid",
                        probe_ssid_builder, "probed ssid");

            RegisterField("dot11.device.associated_client_map", TrackerMacMap,
                    "associated clients", &associated_client_map);

            // Key of associated device, indexed by mac address
            associated_client_map_entry_id =
                RegisterField("dot11.device.associated_client", TrackerKey,
                        "associated client");

            RegisterField("dot11.device.client_disconnects", TrackerUInt64,
                    "client disconnects in last second", 
                    &client_disconnects);

            RegisterField("dot11.device.last_sequence", TrackerUInt64,
                    "last sequence number", &last_sequence);

            RegisterField("dot11.device.bss_timestamp", TrackerUInt64,
                    "last BSS timestamp", &bss_timestamp);

            RegisterField("dot11.device.num_fragments", TrackerUInt64,
                    "number of fragmented packets", &num_fragments);
            RegisterField("dot11.device.num_retries", TrackerUInt64,
                    "number of retried packets", &num_retries);

            RegisterField("dot11.device.datasize", TrackerUInt64,
                    "data in bytes", &datasize);

            RegisterField("dot11.device.datasize_retry", TrackerUInt64,
                    "retried data in bytes", &datasize_retry);

            RegisterField("dot11.device.last_probed_ssid", TrackerString,
                    "last probed ssid", &last_probed_ssid);

            RegisterField("dot11.device.last_probed_ssid_csum", TrackerUInt32,
                    "last probed ssid checksum", &last_probed_ssid_csum);

            RegisterField("dot11.device.last_beaconed_ssid", TrackerString,
                    "last beaconed ssid", &last_beaconed_ssid);

            RegisterField("dot11.device.last_beaconed_ssid_checksum", TrackerUInt32,
                    "last beaconed ssid checksum", &last_beaconed_ssid_csum);

            RegisterField("dot11.device.last_bssid", TrackerMac,
                    "last BSSID", &last_bssid);

            RegisterField("dot11.device.last_beacon_timestamp", TrackerUInt64,
                    "unix timestamp of last beacon frame", 
                    &last_beacon_timestamp);

            RegisterField("dot11.device.wps_m3_count", TrackerUInt64,
                    "WPS M3 message count", &wps_m3_count);
            RegisterField("dot11.device.wps_m3_last", TrackerUInt64,
                    "WPS M3 last message", &wps_m3_last);

            RegisterField("dot11.device.wpa_handshake_list", TrackerVector,
                    "WPA handshakes", &wpa_key_vec);

            __RegisterComplexField(dot11_tracked_eapol, wpa_key_entry_id, 
                    "dot11.eapol.key", "WPA handshake key");

            RegisterField("dot11.device.wpa_nonce_list", TrackerVector,
                    "Previous WPA Nonces", &wpa_nonce_vec);

            RegisterField("dot11.device.wpa_anonce_list", TrackerVector,
                    "Previous WPA ANonces", &wpa_anonce_vec);

            RegisterField("dot11.device.wpa_present_handshake", TrackerUInt8,
                    "handshake sequences seen (bitmask)", &wpa_present_handshake);

            shared_ptr<dot11_tracked_nonce> nonce_builder(new dot11_tracked_nonce(globalreg, 0));
            wpa_nonce_entry_id =
                RegisterComplexField("dot11.device.wpa_nonce", nonce_builder, 
                        "wpa nonce exchange");
        }

        virtual void reserve_fields(SharedTrackerElement e) {
            tracker_component::reserve_fields(e);

            if (e != NULL) {
                // If we're inheriting, it's our responsibility to kick submap and vecs with
                // complex types as well; since they're not themselves complex objects
                TrackerElementIntMap m(advertised_ssid_map);
                for (auto as = m.begin(); as != m.end(); ++as) {
                    std::shared_ptr<dot11_advertised_ssid> assid(new dot11_advertised_ssid(globalreg, advertised_ssid_map_entry_id, as->second));
                    as->second = std::static_pointer_cast<TrackerElement>(assid);
                }

                m = TrackerElementIntMap(probed_ssid_map);
                for (auto ps = m.begin(); ps != m.end(); ++ps) {
                    std::shared_ptr<dot11_probed_ssid> pssid(new dot11_probed_ssid(globalreg, probed_ssid_map_entry_id, ps->second));
                    ps->second = std::static_pointer_cast<TrackerElement>(pssid);
                }

                TrackerElementMacMap mm(client_map);
                for (auto ci = mm.begin(); ci != mm.end(); ++ci) {
                    std::shared_ptr<dot11_client> cli(new dot11_client(globalreg, client_map_entry_id, ci->second));
                    ci->second = std::static_pointer_cast<TrackerElement>(cli);
                }

                // We don't have to deal with the client map because it's a map of
                // simplistic types

                TrackerElementVector v(wpa_key_vec);

                for (auto k = v.begin(); k != v.end(); ++k) {
                    std::shared_ptr<dot11_tracked_eapol> eap(new dot11_tracked_eapol(globalreg, wpa_key_entry_id, *k));
                    *k = std::static_pointer_cast<TrackerElement>(eap);
                }

                // We have to update the nonce and anonce vecs
                TrackerElementVector nv(wpa_nonce_vec);

                for (auto k = nv.begin(); k != nv.end(); ++k) {
                    std::shared_ptr<dot11_tracked_nonce> nonce(new dot11_tracked_nonce(globalreg, wpa_nonce_entry_id, *k));

                    *k = std::static_pointer_cast<TrackerElement>(nonce);
                }

                TrackerElementVector av(wpa_anonce_vec);

                for (auto k = av.begin(); k != av.end(); ++k) {
                    std::shared_ptr<dot11_tracked_nonce> nonce(new dot11_tracked_nonce(globalreg, wpa_nonce_entry_id, *k));

                    *k = std::static_pointer_cast<TrackerElement>(nonce);
                }

            }

        }

        SharedTrackerElement type_set;

        // Records of this device behaving as a client
        SharedTrackerElement client_map;
        int client_map_entry_id;

        // Records of this device advertising SSIDs
        SharedTrackerElement advertised_ssid_map;
        int advertised_ssid_map_entry_id;

        // Records of this device probing for a network
        SharedTrackerElement probed_ssid_map;
        int probed_ssid_map_entry_id;

        // Mac addresses of clients who have talked to this network
        SharedTrackerElement associated_client_map;
        int associated_client_map_entry_id;

        SharedTrackerElement client_disconnects;
        SharedTrackerElement last_sequence;
        SharedTrackerElement bss_timestamp;
        SharedTrackerElement num_fragments;
        SharedTrackerElement num_retries;
        SharedTrackerElement datasize;
        SharedTrackerElement datasize_retry;
        SharedTrackerElement last_probed_ssid;
        SharedTrackerElement last_probed_ssid_csum;
        SharedTrackerElement last_beaconed_ssid;
        SharedTrackerElement last_beaconed_ssid_csum;
        SharedTrackerElement last_bssid;
        SharedTrackerElement last_beacon_timestamp;
        SharedTrackerElement wps_m3_count;
        SharedTrackerElement wps_m3_last;

        SharedTrackerElement wpa_key_vec;
        int wpa_key_entry_id;

        SharedTrackerElement wpa_nonce_vec;
        SharedTrackerElement wpa_anonce_vec;
        int wpa_nonce_entry_id;

        SharedTrackerElement wpa_present_handshake;
};

class dot11_ssid_alert {
    public:
        dot11_ssid_alert() {
#ifdef HAVE_LIBPCRE
            ssid_re = NULL;
            ssid_study = NULL;
#endif
        }
        string name;

#ifdef HAVE_LIBPCRE
        pcre *ssid_re;
        pcre_extra *ssid_study;
        string filter;
#endif
        string ssid;

        macmap<int> allow_mac_map;
};

class Kis_80211_Phy : public Kis_Phy_Handler, 
    public Kis_Net_Httpd_CPPStream_Handler, public TimetrackerEvent {

public:
    // Stub
    ~Kis_80211_Phy();

    // Inherited functionality
    Kis_80211_Phy(GlobalRegistry *in_globalreg) :
        Kis_Phy_Handler(in_globalreg) { };

    // Build a strong version of ourselves
    virtual Kis_Phy_Handler *CreatePhyHandler(GlobalRegistry *in_globalreg,
            Devicetracker *in_tracker,
            int in_phyid) {
        return new Kis_80211_Phy(in_globalreg, in_tracker, in_phyid);
    }

    // Strong constructor
    Kis_80211_Phy(GlobalRegistry *in_globalreg, Devicetracker *in_tracker,
            int in_phyid);

    int WPACipherConv(uint8_t cipher_index);
    int WPAKeyMgtConv(uint8_t mgt_index);

    // Dot11 decoders, wep decryptors, etc
    int PacketWepDecryptor(kis_packet *in_pack);
    int PacketDot11dissector(kis_packet *in_pack);

    // Special decoders, not called as part of a chain

    // Is packet a WPS M3 message?  Used to detect Reaver, etc
    int PacketDot11WPSM3(kis_packet *in_pack);

    // Is the packet a WPA handshake?  Return an eapol tracker element if so
    shared_ptr<dot11_tracked_eapol> PacketDot11EapolHandshake(kis_packet *in_pack,
            shared_ptr<dot11_tracked_device> dot11device);

    // static incase some other component wants to use it
    static kis_datachunk *DecryptWEP(dot11_packinfo *in_packinfo,
            kis_datachunk *in_chunk, 
            unsigned char *in_key, int in_key_len,
            unsigned char *in_id);

    // TODO - what do we do with the strings?  Can we make them phy-neutral?
    // int packet_dot11string_dissector(kis_packet *in_pack);

    // 802.11 packet classifier to common for the devicetracker layer
    static int CommonClassifierDot11(CHAINCALL_PARMS);

    // Dot11 tracker for building phy-specific elements
    int TrackerDot11(kis_packet *in_pack);

    int AddFilter(string in_filter);
    int AddNetcliFilter(string in_filter);

    void SetStringExtract(int in_extr);

    void AddWepKey(mac_addr bssid, uint8_t *key, unsigned int len, int temp);

    static string CryptToString(uint64_t cryptset);

    // HTTPD API
    virtual bool Httpd_VerifyPath(const char *path, const char *method);

    virtual void Httpd_CreateStreamResponse(Kis_Net_Httpd *httpd,
            Kis_Net_Httpd_Connection *connection,
            const char *url, const char *method, const char *upload_data,
            size_t *upload_data_size, std::stringstream &stream);

    virtual int Httpd_PostComplete(Kis_Net_Httpd_Connection *concls);

    // Timetracker event handler
    virtual int timetracker_event(int eventid);

    // Restore stored dot11 records
    virtual void LoadPhyStorage(SharedTrackerElement in_storage,
            SharedTrackerElement in_device);

protected:
    std::shared_ptr<Alertracker> alertracker;
    std::shared_ptr<Packetchain> packetchain;
    std::shared_ptr<Timetracker> timetracker;

    // Checksum of recent packets for duplication filtering
    uint32_t *recent_packet_checksums;
    size_t recent_packet_checksums_sz;
    unsigned int recent_packet_checksum_pos;

    void HandleSSID(shared_ptr<kis_tracked_device_base> basedev, 
            shared_ptr<dot11_tracked_device> dot11dev,
            kis_packet *in_pack,
            dot11_packinfo *dot11info,
            kis_gps_packinfo *pack_gpsinfo);

    void HandleProbedSSID(shared_ptr<kis_tracked_device_base> basedev, 
            shared_ptr<dot11_tracked_device> dot11dev,
            kis_packet *in_pack,
            dot11_packinfo *dot11info,
            kis_gps_packinfo *pack_gpsinfo);

    void HandleClient(shared_ptr<kis_tracked_device_base> basedev, 
            shared_ptr<dot11_tracked_device> dot11dev,
            kis_packet *in_pack,
            dot11_packinfo *dot11info,
            kis_gps_packinfo *pack_gpsinfo,
            kis_data_packinfo *pack_datainfo);

    void GenerateHandshakePcap(shared_ptr<kis_tracked_device_base> dev, 
            Kis_Net_Httpd_Connection *connection,
            std::stringstream &stream);

    int dot11_device_entry_id;

    int LoadWepkeys();

    map<mac_addr, string> bssid_cloak_map;

    string ssid_cache_path, ip_cache_path;
    int ssid_cache_track, ip_cache_track;

    // Device components
    int dev_comp_dot11, dev_comp_common;

    // Packet components
    int pack_comp_80211, pack_comp_basicdata, pack_comp_mangleframe,
        pack_comp_strings, pack_comp_checksum, pack_comp_linkframe,
        pack_comp_decap, pack_comp_common, pack_comp_datapayload,
        pack_comp_gps, pack_comp_l1info;

    // Do we do any data dissection or do we hide it all (legal safety
    // cutout)
    int dissect_data;

    // Do we pull strings?
    int dissect_strings, dissect_all_strings;

    FilterCore *string_filter;
    macmap<int> string_nets;

    // SSID regex filter
    SharedTrackerElement ssid_regex_vec;
    int ssid_regex_vec_element_id;

    // Dissector alert references
    int alert_netstumbler_ref, alert_nullproberesp_ref, alert_lucenttest_ref,
        alert_msfbcomssid_ref, alert_msfdlinkrate_ref, alert_msfnetgearbeacon_ref,
        alert_longssid_ref, alert_disconinvalid_ref, alert_deauthinvalid_ref,
        alert_dhcpclient_ref, alert_wmm_ref, alert_nonce_zero_ref, 
        alert_nonce_duplicate_ref, alert_11kneighborchan_ref;

    // Are we allowed to send wepkeys to the client (server config)
    int client_wepkey_allowed;
    // Map of wepkeys to BSSID (or bssid masks)
    macmap<dot11_wep_key *> wepkeys;

    // Generated WEP identity / base
    unsigned char wep_identity[256];

    // Tracker alert references
    int alert_chan_ref, alert_dhcpcon_ref, alert_bcastdcon_ref, alert_airjackssid_ref,
        alert_wepflap_ref, alert_dhcpname_ref, alert_dhcpos_ref, alert_adhoc_ref,
        alert_ssidmatch_ref, alert_dot11d_ref, alert_beaconrate_ref,
        alert_cryptchange_ref, alert_malformmgmt_ref, alert_wpsbrute_ref, 
        alert_l33t_ref, alert_tooloud_ref, alert_atheros_wmmtspec_ref,
        alert_atheros_rsnloop_ref;

    int signal_too_loud_threshold;

    // Command refs
    int addfiltercmd_ref, addnetclifiltercmd_ref;

    // Filter core for tracker
    FilterCore *track_filter;
    // Filter core for network client
    FilterCore *netcli_filter;

    int proto_ref_ssid, proto_ref_device, proto_ref_client;

    // SSID cloak file as a config file
    ConfigFile *ssid_conf;
    time_t conf_save;

    // probe assoc to owning network
    map<mac_addr, kis_tracked_device_base *> probe_assoc_map;

    // Do we time out components of devices?
    int device_idle_expiration;
    int device_idle_timer;

    // Pcap handlers
    unique_ptr<Phy_80211_Httpd_Pcap> httpd_pcap;

    // Do we process control and phy frames?
    bool process_ctl_phy;
    };

#endif
