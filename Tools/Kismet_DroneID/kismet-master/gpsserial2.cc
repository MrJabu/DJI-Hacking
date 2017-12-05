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

#include <time.h>

#include "gpsserial2.h"
#include "util.h"
#include "gpstracker.h"
#include "pollabletracker.h"

GPSSerialV2::GPSSerialV2(GlobalRegistry *in_globalreg, SharedGpsBuilder in_builder) : 
    KisGps(in_globalreg, in_builder) {

    // Defer making buffers until open, because we might be used to make a 
    // builder instance
    
    serialclient = NULL;
    serialhandler = NULL;

    ever_seen_gps = false;

    last_heading_time = time(0);

    pollabletracker =
        Globalreg::FetchGlobalAs<PollableTracker>(globalreg, "POLLABLETRACKER");

    auto timetracker = 
        Globalreg::FetchGlobalAs<Timetracker>(globalreg, "TIMETRACKER");
    error_reconnect_timer = 
        timetracker->RegisterTimer(SERVER_TIMESLICES_SEC * 10, NULL, 1,
                [this](int) -> int {
                    if (get_device_connected()) 
                        return 1;

                    open_gps(get_gps_definition());

                    return 1;
                });
}

GPSSerialV2::~GPSSerialV2() {
    local_eol_locker lock(&gps_mutex);

    if (serialhandler != NULL)
        delete(serialhandler);

    pollabletracker->RemovePollable(serialclient);

    shared_ptr<Timetracker> timetracker = 
        Globalreg::FetchGlobalAs<Timetracker>(globalreg, "TIMETRACKER");
    timetracker->RemoveTimer(error_reconnect_timer);
}

bool GPSSerialV2::open_gps(string in_opts) {
    local_locker lock(&gps_mutex);

    if (!KisGps::open_gps(in_opts))
        return false;

    set_int_device_connected(false);

    // Delete any existing serial interface before we parse options
    if (serialhandler != NULL) {
        delete serialhandler;
        serialhandler = NULL;
    }

    if (serialclient != NULL) {
        pollabletracker->RemovePollable(serialclient);
        serialclient.reset();
    }

    string proto_device;
    string proto_baud_s;
    string proto_name;
    unsigned int proto_baud;

    proto_device = FetchOpt("device", source_definition_opts);
    proto_baud_s = FetchOpt("baud", source_definition_opts);
    proto_name = FetchOpt("name", source_definition_opts);

    if (proto_device == "") {
        _MSG("GPSSerial expected device= option, none found.", MSGFLAG_ERROR);
        return -1;
    }

    if (proto_baud_s != "") {
        if (sscanf(proto_baud_s.c_str(), "%u", &proto_baud) != 1) {
            _MSG("GPSSerial expected baud rate in baud= option.", MSGFLAG_ERROR);
            return -1;
        }
    } else {
        proto_baud = 4800;
        _MSG("GPSSerial defaulting to 4800 baud for GPS device, specify baud= option "
                "if your device uses a different speed.", MSGFLAG_INFO);
    }

    // We never write to a serial gps so don't make a write buffer
    serialhandler = new BufferHandler<RingbufV2>(2048, 0);
    // Set the read handler to us
    serialhandler->SetReadBufferInterface(this);
    // Link it to a serial port
    serialclient.reset(new SerialClientV2(globalreg, serialhandler));
    serialclient->OpenDevice(proto_device, proto_baud);

    pollabletracker->RegisterPollable(serialclient);

    serial_device = proto_device;
    baud = proto_baud;

    set_int_device_connected(true);

    return 1;
}

bool GPSSerialV2::get_location_valid() {
    local_locker lock(&gps_mutex);

    if (gps_location == NULL) {
        return false;
    }

    if (gps_location->fix < 2) {
        return false;
    }

    time_t now = time(0);

    if (now - gps_location->tv.tv_sec > 10) {
        return false;
    }

    return true;
}

bool GPSSerialV2::get_device_connected() {
    local_locker lock(&gps_mutex);

    if (serialclient == NULL)
        return false;

    return serialclient->FetchConnected();
}

void GPSSerialV2::BufferAvailable(size_t in_amt) {
    local_locker lock(&gps_mutex);

    size_t buf_sz;
    char *buf;

    // Peek at all the data we have available
    buf_sz = serialhandler->PeekReadBufferData((void **) &buf, 
            serialhandler->GetReadBufferAvailable());

    // Aggregate into a new location; then copy into the main location
    // depending on what we found.  Locations can come in multiple sentences
    // so if we're within a second of the previous one we can aggregate them
    vector<string> inptok = StrTokenize(string(buf, buf_sz), "\n", 0);

    // We've tokenized a copy of the buffer so unlock what we peeked
    serialhandler->PeekFreeReadBufferData(buf);

    if (inptok.size() < 1) {
        return;
    }

    kis_gps_packinfo *new_location = new kis_gps_packinfo;
    bool set_lat_lon;
    bool set_alt;
    bool set_speed;
    bool set_fix;

    set_lat_lon = false;
    set_alt = false;
    set_speed = false;
    set_fix = false;

    // TODO fix assumptions about token length and spacing...
	for (unsigned int it = 0; it < inptok.size(); it++) {
        // Consume the data from the ringbuffer
        serialhandler->ConsumeReadBufferData(inptok[it].length() + 1);

		if (inptok[it].length() < 4)
			continue;

		// $GPGGA,012527.000,4142.6918,N,07355.8711,W,1,07,1.2,57.8,M,-34.0,M,,0000*57

		vector<string> gpstoks = StrTokenize(inptok[it], ",");

		if (gpstoks.size() == 0)
			continue;

		if (gpstoks[0] == "$GPGGA") {
			int tint;
			float tfloat;

			if (gpstoks.size() < 15)
				continue;

			// Parse the basic gps coodinate string
			// $GPGGA,time,lat,NS,lon,EW,quality,#sats,hdop,alt,M,geopos,M,
			// dgps1,dgps2,checksum

			if (sscanf(gpstoks[2].c_str(), "%2d%f", &tint, &tfloat) != 2)
				continue;
            new_location->lat = (float) tint + (tfloat / 60);
			if (gpstoks[3] == "S")
				new_location->lat *= -1;

			if (sscanf(gpstoks[4].c_str(), "%3d%f", &tint, &tfloat) != 2)
				continue;
			new_location->lon = (float) tint + (tfloat / 60);
			if (gpstoks[5] == "W")
				new_location->lon *= -1;

            set_lat_lon = true;
            if (new_location->fix < 2)
                new_location->fix = 2;

			if (sscanf(gpstoks[9].c_str(), "%f", &tfloat) != 1)
				continue;

			new_location->alt = tfloat;
            set_alt = true;
            if (new_location->fix < 3)
                new_location->fix = 3;
            set_fix = true;

			// printf("debug - %f, %f alt %f\n", in_lat, in_lon, in_alt);

			continue;
		}

		if (gpstoks[0] == "$GPRMC") {
			// recommended minimum
			// $GPRMC,time,valid,lat,lathemi,lon,lonhemi,speed-knots,bearing,utc,,checksum
			int tint;
			float tfloat;
			
			if (gpstoks.size() < 12)
				continue;

			if (gpstoks[2] == "A") {
				// Kluge - if we have a 3d fix, we're getting another sentence
				// which contains better information, so we don't override it. 
				// If we < a 2d fix, we up it to 2d.
                if (new_location->fix < 2)
                    new_location->fix = 2;
                set_fix = true;
			} else {
				continue;
			}

			if (sscanf(gpstoks[3].c_str(), "%2d%f", &tint, &tfloat) != 2)
				continue;
			new_location->lat = (float) tint + (tfloat / 60);
			if (gpstoks[4] == "S")
				new_location->lat *= -1;

			if (sscanf(gpstoks[5].c_str(), "%3d%f", &tint, &tfloat) != 2)
				continue;
			new_location->lon = (float) tint + (tfloat / 60);
			if (gpstoks[6] == "W")
				new_location->lon *= -1;

            if (new_location->fix < 2)
                new_location->fix = 2;
            set_fix = true;

			if (sscanf(gpstoks[7].c_str(), "%f", &tfloat) != 1) 
				continue;
            new_location->speed = tfloat;
            set_speed = true;

            // This sentence doesn't have altitude, so don't set it.  If another
            // sentence in this same block sets it we'll use that.

			continue;
		}

		// GPS DOP and active sats
        // TODO do something smart with these?
#if 0
		if (gpstoks[0] == "$GPGSA") {
			/*
			http://www.gpsinformation.org/dale/nmea.htm#GSA
		    $GPGSA,A,3,04,05,,09,12,,,24,,,,,2.5,1.3,2.1*39

			Where:
			GSA      Satellite status
			A        Auto selection of 2D or 3D fix (M = manual) 
			3        3D fix - values include: 1 = no fix
			2 = 2D fix
			3 = 3D fix
			04,05... PRNs of satellites used for fix (space for 12) 
			2.5      PDOP (dilution of precision) 
			1.3      Horizontal dilution of precision (HDOP) 
			2.1      Vertical dilution of precision (VDOP)
		    *39      the checksum data, always begins with *
			 */
			int tint;

			gps_connected = 1;

			if (gpstoks.size() < 18)
				continue;

			if (sscanf(gpstoks[2].c_str(), "%d", &tint) != 1)
				continue;

			/* Account for jitter after the first set */
			if (tint >= last_mode) {
				in_mode = tint;
				last_mode = tint;
				set_mode = 1;
				// printf("debug - mode %d\n", in_mode);
			} else {
				last_mode = tint;
			}
		}
#endif

		// Travel made good, also a source of speed
		if (gpstoks[0] == "$GPVTG") {
			// $GPVTG,,T,,M,0.00,N,0.0,K,A*13
			float tfloat;

			if (gpstoks.size() < 10) {
				continue;
			}

            // Only use VTG if we didn't get our speed from another sentence
            // in this series
			if (set_speed == 0) {
				if (sscanf(gpstoks[7].c_str(), "%f", &tfloat) != 1) 
					continue;
                new_location->speed = tfloat;
                set_speed = 1;
			}

			continue;
		} 
       
        // Satellites in view
        // TODO figure out if we can use this data and so something smarter with it
        if (inptok[it].substr(0, 6) == "$GPGSV") {
			// $GPGSV,3,1,09,22,80,170,40,14,58,305,19,01,46,291,,18,44,140,33*7B
			// $GPGSV,3,2,09,05,39,105,31,12,34,088,32,30,31,137,31,09,26,047,34*72
			// $GPGSV,3,3,09,31,26,222,31*46
			//
			// # of sentences for data
			// sentence #
			// # of sats in view
			//
			// sat #
			// elevation
			// azimuth
			// snr

#if 0
			vector<string> svvec = StrTokenize(inptok[it], ",");
			GPSCore::sat_pos sp;

			gps_connected = 1;

			if (svvec.size() < 6) {
				continue;
			}

			// If we're on the last sentence, move the new vec to the transmitted one
			if (svvec[1] == svvec[2]) {
				sat_pos_map = sat_pos_map_tmp;
				sat_pos_map_tmp.clear();
			}

			unsigned int pos = 4;
			while (pos + 4 < svvec.size()) {
				if (sscanf(svvec[pos++].c_str(), "%d", &sp.prn) != 1) 
					break;
				if (sscanf(svvec[pos++].c_str(), "%d", &sp.elevation) != 1)
					break;
				if (sscanf(svvec[pos++].c_str(), "%d", &sp.azimuth) != 1)
					break;
				if (sscanf(svvec[pos++].c_str(), "%d", &sp.snr) != 1)
					sp.snr = 0;

				sat_pos_map_tmp[sp.prn] = sp;
			}
#endif

			continue;
		}

	}

    if (set_alt || set_speed || set_lat_lon || set_fix) {
        ever_seen_gps = true;

        if (gps_location != NULL) {
            // Copy the current location to the last one
            if (gps_last_location != NULL)
                delete gps_last_location;
            gps_last_location = new kis_gps_packinfo(gps_location);
        } else {
            gps_location = new kis_gps_packinfo();
        }

        // Copy whatever we know about the new location into the current
        if (set_lat_lon) {
            gps_location->lat = new_location->lat;
            gps_location->lon = new_location->lon;
        }

        if (set_alt)
            gps_location->alt = new_location->alt;

        if (set_speed) {
            gps_location->speed = new_location->speed;
            // NMEA reports speed in knots, convert
            gps_location->speed *= 0.514;
        }

        if (set_fix) {
            gps_location->fix = new_location->fix;
        }

        gettimeofday(&(gps_location->tv), NULL);

		if (globalreg->timestamp.tv_sec - last_heading_time > 5 &&
                gps_last_location != NULL &&
                gps_last_location->fix >= 2) {
			gps_location->heading = 
                GpsCalcHeading(gps_location->lat, gps_location->lon, 
                        gps_last_location->lat, gps_last_location->lon);
            last_heading_time = gps_location->tv.tv_sec;
		}
    }

    // Sync w/ the tracked fields
    update_locations();

    delete new_location;
}

void GPSSerialV2::BufferError(string in_error) {
    local_locker lock(&gps_mutex);

    _MSG("GPS device '" + get_gps_name() + "' encountered a serial error: " + in_error,
            MSGFLAG_ERROR);

    set_int_device_connected(false);
}

