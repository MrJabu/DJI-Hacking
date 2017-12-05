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

#ifndef __GPSGPSD_V2_H__
#define __GPSGPSD_V2_H__

#include "config.h"

#include "kis_gps.h"
#include "timetracker.h"
#include "buffer_handler.h"
#include "ringbuf2.h"
#include "globalregistry.h"
#include "tcpclient2.h"
#include "pollabletracker.h"

// New GPSD interface
//
// This code uses the new buffer handler interface for communicating with a 
// gpsd host over TCP

class GPSGpsdV2 : public KisGps, public BufferInterface {
public:
    GPSGpsdV2(GlobalRegistry *in_globalreg, SharedGpsBuilder in_builder);
    virtual ~GPSGpsdV2();

    // BufferInterface API
    virtual void BufferAvailable(size_t in_amt);
    virtual void BufferError(string in_err);

    virtual bool open_gps(string in_definition);

    virtual bool get_location_valid();
    virtual bool get_device_connected();

protected:
    int error_reconnect_timer;

    shared_ptr<PollableTracker> pollabletracker;

    shared_ptr<TcpClientV2> tcpclient;
    BufferHandler<RingbufV2> *tcphandler;

    // Device
    string host;
    unsigned int port;

    // Last time we calculated the heading, don't do it more than once every 
    // few seconds or we get nasty noise
    time_t last_heading_time;

    // Decaying reconnection algorithm
    int reconnect_tid;
    int num_reconnects;
    static int time_event_reconnect(TIMEEVENT_PARMS);

    // Poll mode (do we know we're JSON, etc
    int poll_mode;
    // Units - different gpsd variants return it different ways
    int si_units;
    // Do we run in raw mode?
    int si_raw;
};

class GPSGpsdV2Builder : public KisGpsBuilder {
public:
    GPSGpsdV2Builder(GlobalRegistry *in_globalreg) : KisGpsBuilder(in_globalreg, 0) { 
        initialize();
    }

    virtual void initialize() {
        set_int_gps_class("gpsd");
        set_int_gps_class_description("networked GPSD server");
        set_int_gps_priority(-1000);
        set_int_singleton(false);
        set_int_default_name("gpsd");
    }

    virtual SharedGps build_gps(SharedGpsBuilder in_builder) {
        return SharedGps(new GPSGpsdV2(globalreg, in_builder));
    }
};

#endif

