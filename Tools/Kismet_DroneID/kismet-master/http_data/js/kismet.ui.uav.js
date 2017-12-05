(
  typeof define === "function" ? function (m) { define("kismet-ui-uav-js", m); } :
  typeof exports === "object" ? function (m) { module.exports = m(); } :
  function(m){ this.kismet_ui_uav = m(); }
)(function () {

"use strict";

var exports = {};

// Flag we're still loading
exports.load_complete = 0;

/* Highlight UAV devices */
kismet_ui.AddDeviceRowHighlight({
    name: "UAV/Drones",
    description: "UAV and Drone devices",
    priority: 100,
    defaultcolor: "#f49e42",
    defaultenable: false,
    fields: [
        'uav.device'
    ],
    selector: function(data) {
        return ('uav.device' in data && data['uav.device'] != 0);
    }
});

kismet_ui.AddDeviceDetail("uav", "UAV/Drone", 0, {
    filter: function(data) {
        return ('uav.device' in data && data['uav.device'] != 0);
    },
    draw: function(data, target) {
        target.devicedata(data, {
            "id": "uavdata",
            "fields": [
            {
                field: "uav.device/uav.serialnumber",
                title: "Serial Number",
                empty: "<i>Unknown</i>",
                help: "Serial numbers are available from UAV devices which broadcast the DroneID protocol.  Currently only DJI devices advertise this protocol.",
            },
            {
                field: "uav.device/uav.match_type",
                title: "ID Method",
                empty: "<i>Unknown</i>",
                help: "Kismet can identify a UAV device by several methods; 'WifiMatch' compares the MAC address and SSID.  'DroneID' matches the DJI DroneID protocol added to packets from the device.",
            },
            {
                field: "last_telem",
                groupTitle: "Telemetry",
                id: "last_telem",
                fields: [
                {
                    field: "uav.device/uav.last_telemetry/uav.telemetry.motor_on",
                    title: "Motor",
                    render: function(opts) {
                        if (opts['value'])
                            return "On";
                        return "Off";
                    },
                    empty: "<i>Unknown</i>",
                    help: "The UAV device advertised that the props are currently on",
                },
                {
                    field: "uav.device/uav.last_telemetry/uav.telemetry.airborne",
                    title: "Airborne",
                    render: function(opts) {
                        if (opts['value'])
                            return "Yes";
                        return "No";
                    },
                    empty: "<i>Unknown</i>",
                    help: "The UAV device advertised that it is airborne",
                },
                {
                    field: "uav_location",
                    title: "Location",
                    render: function(opts) {
                        var loc =
                            kismet.ObjectByString(opts['data'], "uav.device/uav.last_telemetry/uav.telemetry.location/kismet.common.location.lat") + ", " +
                            kismet.ObjectByString(opts['data'], "uav.device/uav.last_telemetry/uav.telemetry.location/kismet.common.location.lon");

                        return loc;
                    },
                    help: "Last advertised location",
                },
                {
                    field: "home_location",
                    title: "Home Location",
                    render: function(opts) {
                        var loc =
                            kismet.ObjectByString(opts['data'], "uav.device/uav.telemetry.home_location/kismet.common.location.lat") + ", " +
                            kismet.ObjectByString(opts['data'], "uav.device/uav.telemetry.home_location/kismet.common.location.lon");

                        return loc;
                    },
                    help: "Last advertised <b>home</b> location.  The home location is where a UAV will return to if signal is lost or a return-to-home is received.",
                },
                {
                    field: "uav.device/uav.last_telemetry/uav.telemetry.location/kismet.common.location.alt",
                    title: "Altitude (meters)",
                    help: "Last advertised altitude",
                    filter: function(opts) {
                        return (kismet.ObjectByString(opts['data'], "uav.device/uav.last_telemetry/uav.telemetry.location/kismet.common.location.fix") >= 3);
                    }
                },
                {
                    field: "uav.device/uav.last_telemetry/uav.telemetry.height",
                    title: "Height (meters)",
                    help: "Advertised height above ground",
                },

                ],
            }

            ],
        });
    },
});

// We're done loading
exports.load_complete = 1;

return exports;

});
