(
  typeof define === "function" ? function (m) { define("kismet-ui-base-js", m); } :
  typeof exports === "object" ? function (m) { module.exports = m(); } :
  function(m){ this.kismet_ui_base = m(); }
)(function () {

"use strict";

var exports = {};

// Flag we're still loading
exports.load_complete = 0;

// Load our css
$('<link>')
    .appendTo('head')
    .attr({
        type: 'text/css',
        rel: 'stylesheet',
        href: '/css/kismet.ui.base.css'
    });

/* Define some callback functions for the table */

exports.renderLastTime = function(data, type, row, meta) {
    return (new Date(data * 1000).toString()).substring(4, 25);
}

exports.renderDataSize = function(data, type, row, meta) {
    if (type === 'display')
        return kismet.HumanReadableSize(data);

    return data;
}

exports.renderMac = function(data, type, row, meta) {
    if (typeof(data) === 'undefined') {
        return "<i>n/a</i>";
    }
    return data;
}

exports.renderSignal = function(data, type, row, meta) {
    if (data == 0)
        return "<i>n/a</i>"
    return data;
}

exports.renderChannel = function(data, type, row, meta) {
    if (data == 0)
        return "<i>n/a</i>"
    return data;
}

exports.renderPackets = function(data, type, row, meta) {
    return "<i>Preparing graph</i>";
}

exports.drawPackets = function(dyncolumn, table, row) {
    // Find the column
    var rid = table.column(dyncolumn.name + ':name').index();
    var match = "td:eq(" + rid + ")";

    var data = row.data();

    // Simplify the RRD so that the bars are thicker in the graph, which
    // I think looks better.  We do this with a transform function on the
    // RRD function, and we take the peak value of each triplet of samples
    // because it seems to be more stable, visually
    //
    // We use the aliased field names we extracted from just the minute
    // component of the per-device packet RRD
    var simple_rrd =
        kismet.RecalcRrdData(
            data['packet.rrd.last_time'],
            data['packet.rrd.last_time'],
            kismet.RRD_SECOND,
            data['packet.rrd.minute_vec'], {
                transform: function(data, opt) {
                    var slices = 3;
                    var peak = 0;
                    var ret = new Array();

                    for (var ri = 0; ri < data.length; ri++) {
                        peak = Math.max(peak, data[ri]);

                        if ((ri % slices) == (slices - 1)) {
                            ret.push(peak);
                            peak = 0;
                        }
                    }

                    return ret;
                }
            });

    // Render the sparkline
    $(match, row.node()).sparkline(simple_rrd,
        { type: "bar",
            width: 100,
            height: 12,
            barColor: '#000000',
            nullColor: '#000000',
            zeroColor: '#000000'
        });
}

// Define the basic columns
kismet_ui.AddDeviceColumn('column_name', {
    sTitle: 'Name',
    field: 'kismet.device.base.name',
    description: 'Device name'
});

kismet_ui.AddDeviceColumn('column_type', {
    sTitle: 'Type',
    field: 'kismet.device.base.type',
    description: 'Device type',
    width: '4em',
});

kismet_ui.AddDeviceColumn('column_phy', {
    sTitle: 'Phy',
    field: 'kismet.device.base.phyname',
    description: 'Capture Phy name',
    width: "8em",
});

kismet_ui.AddDeviceColumn('column_signal', {
    sTitle: 'Signal',
    field: 'kismet.device.base.signal/kismet.common.signal.last_signal_dbm',
    description: 'Last-seen signal',
    width: "6em",
    renderfunc: function(d, t, r, m) {
        return exports.renderSignal(d, t, r, m);
    },
});

kismet_ui.AddDeviceColumn('column_channel', {
    sTitle: 'Channel',
    field: 'kismet.device.base.channel',
    description: 'Last-seen channel',
    width: "6em",
    renderfunc: function(d, t, r, m) {
        if (d != 0) {
            return d;
        } else if ('kismet.device.base.frequency' in r &&
            r['kismet.device.base_frequency'] != 0) {
            return kismet.HumanReadableFrequency(r['kismet.device.base.frequency']);
        } else {
            return "<i>n/a</i>";
        }
    },
});

kismet_ui.AddDeviceColumn('column_time', {
    sTitle: 'Last Seen',
    field: 'kismet.device.base.last_time',
    description: 'Last-seen time',
    renderfunc: function(d, t, r, m) {
        return exports.renderLastTime(d, t, r, m);
    },
});

kismet_ui.AddDeviceColumn('column_datasize', {
    sTitle: 'Data',
    field: 'kismet.device.base.datasize',
    description: 'Data seen',
    bUseRendered: false,
    renderfunc: function(d, t, r, m) {
        return exports.renderDataSize(d, t, r, m);
    },
});

// Fetch just the last time field, we use the hidden rrd_min_data field to assemble
// the rrd.  This is a hack to be more efficient and not send the house or day
// rrd records along with it.
kismet_ui.AddDeviceColumn('column_packet_rrd', {
    sTitle: 'Packets',
    field: ['kismet.device.base.packets.rrd/kismet.common.rrd.last_time',
            'packet.rrd.last_time'],
    name: 'packets',
    description: 'Packet history graph',
    renderfunc: function(d, t, r, m) {
        return exports.renderPackets(d, t, r, m);
    },
    drawfunc: function(d, t, r) {
        return exports.drawPackets(d, t, r);
    },
    orderable: false,
    searchable: false,
});

// Hidden col for packet minute rrd data
kismet_ui.AddDeviceColumn('column_rrd_minute_hidden', {
    sTitle: 'packets_rrd_min_data',
    field: ['kismet.device.base.packets.rrd/kismet.common.rrd.minute_vec',
            'packet.rrd.minute_vec'],
    name: 'packets_rrd_min_data',
    searchable: false,
    visible: false,
    selectable: false,
    orderable: false
});

// Hidden col for key, mappable, we need to be sure to
// fetch it so we can use it as an index
kismet_ui.AddDeviceColumn('column_device_key_hidden', {
    sTitle: 'Key',
    field: 'kismet.device.base.key',
    searchable: false,
    orderable: false,
    visible: false,
    selectable: false,
});

// Hidden col for mac address, searchable
kismet_ui.AddDeviceColumn('column_device_mac', {
    sTitle: 'MAC',
    field: 'kismet.device.base.macaddr',
    description: 'MAC address',
    searchable: true,
    orderable: true,
    visible: false,
    renderfunc: function(d, t, r, m) {
        return exports.renderMac(d, t, r, m);
    },
});

// Hidden column for computing freq in the absence of channel
kismet_ui.AddDeviceColumn('column_frequency', {
    sTitle: 'Frequency',
    field: 'kismet.device.base.frequency',
    description: 'Frequency',
    name: 'frequency',
    searchable: false,
    visible: false,
    orderable: true,
});

// Manufacturer name
kismet_ui.AddDeviceColumn('column_manuf', {
    sTitle: 'Manuf',
    field: 'kismet.device.base.manuf',
    description: 'Manufacturer',
    name: 'manuf',
    searchable: true,
    visible: false,
    orderable: true,
});


// Add the (quite complex) device details.
// It has a priority of -1000 because we want it to always come first.
//
// There is no filter function because we always have base device
// details
//
// There is no render function because we immediately fill it during draw.
//
// The draw function will populate the kismet devicedata when pinged
kismet_ui.AddDeviceDetail("base", "Device Info", -1000, {
    draw: function(data, target) {
        target.devicedata(data, {
            "id": "genericDeviceData",
            "fields": [
            {
                field: "kismet.device.base.name",
                title: "Name",
                empty: "<i>None</i>",
                help: "Device name, derived from device characteristics or set as a custom name by the user."
            },
            {
                field: "kismet.device.base.macaddr",
                title: "MAC Address",
                help: "Unique per-phy address of the transmitting device, when available.  Not all phy types provide MAC addresses, however most do.",
            },
            {
                field: "kismet.device.base.manuf",
                title: "Manufacturer",
                empty: "<i>Unknown</i>",
                help: "Manufacturer of the device, derived from the MAC address.  Manufacturers are registered with the IEEE and resolved in the files specified in kismet.conf under 'manuf='",
            },
            {
                field: "kismet.device.base.type",
                title: "Type",
                empty: "<i>Unknown</i>"
            },
            {
                field: "kismet.device.base.first_time",
                title: "First Seen",
                render: function(opts) {
                    return new Date(opts['value'] * 1000);
                }
            },
            {
                field: "kismet.device.base.last_time",
                title: "Last Seen",
                render: function(opts) {
                    return new Date(opts['value'] * 1000);
                }
            },
            {
                field: "group_frequency",
                groupTitle: "Frequencies",
                id: "group_frequency",

                fields: [
                {
                    field: "kismet.device.base.channel",
                    title: "Channel",
                    empty: "<i>None Advertised</i>",
                    help: "The phy-specific channel of the device, if known.  The advertised channel defines a specific, known channel, which is not affected by channel overlap.  Not all phy types advertise fixed channels, and not all device types have fixed channels.  If an advertised channel is not available, the primary frequency is used.",
                },
                {
                    field: "kismet.device.base.frequency",
                    title: "Main Frequency",
                    help: "The primary frequency of the device, if known.  Not all phy types advertise a fixed frequency in packets.",
                    render: function(opts) {
                        return kismet.HumanReadableFrequency(opts['value']);
                    },
                    filterOnZero: true,
                },
                {
                    field: "frequency_map",
                    span: true,
                    filter: function(opts) {
                        return (Object.keys(opts['data']['kismet.device.base.freq_khz_map']).length >= 1);
                    },
                    render: function(opts) {
                        return '<center>Packet Frequency Distribution</center><div class="freqbar" id="' + opts['key'] + '" />';
                    },
                    draw: function(opts) {
                        var bardiv = $('div', opts['container']);

                        // Make an array morris likes using our whole data record
                        var moddata = new Array();

                        for (var fk in opts['data']['kismet.device.base.freq_khz_map']) {
                            moddata.push({
                                y: kismet.HumanReadableFrequency(parseInt(fk)),
                                c: opts['data']['kismet.device.base.freq_khz_map'][fk]
                            });
                        }

                        Morris.Bar({
                            element: bardiv,
                            data: moddata,
                            xkey: 'y',
                            ykeys: ['c'],
                            labels: ['Packets'],
                            hideHover: 'auto'
                        });
                    }
                },
                ]
            },
            {
                field: "group_signal_data",
                groupTitle: "Signal",
                id: "group_signal_data",

                filter: function(opts) {
                    var db = kismet.ObjectByString(opts['data'], "kismet.device.base.signal/kismet.common.signal.last_signal_dbm");
                    var rssi = kismet.ObjectByString(opts['data'], "kismet.device.base.signal/kismet.common.signal.last_signal_rssi");

                    if (db == 0 && rssi == 0)
                        return false;

                    return true;
                },

                fields: [
                {
                    field: "kismet.device.base.signal/kismet.common.signal.signal_rrd",
                    filterOnZero: true,
                    title: "Monitor Signal",

                    render: function(opts) {
                        return '<div class="monitor pseudolink">Monitor</div>';
                    },
                    draw: function(opts) {
                        $('div.monitor', opts['container'])
                        .on('click', function() {
                            exports.DeviceSignalDetails(opts['data']['kismet.device.base.key']);
                        });
                    },

                    /* RRD - come back to this later
                    render: function(opts) {
                        return '<div class="rrd" id="' + opts['key'] + '" />';
                    },
                    draw: function(opts) {
                        var rrdiv = $('div', opts['container']);

                        var rrdata = kismet.RecalcRrdData(opts['data']['kismet.device.base.signal']['kismet.common.signal.signal_rrd']['kismet.common.rrd.last_time'], last_devicelist_time, kismet.RRD_MINUTE, opts['data']['kismet.device.base.signal']['kismet.common.signal.signal_rrd']['kismet.common.rrd.minute_vec'], {});

                        // We assume the 'best' a signal can usefully be is -20dbm,
                        // that means we're right on top of it.
                        // We can assume that -100dbm is a sane floor value for
                        // the weakest signal.
                        // If a signal is 0 it means we haven't seen it at all so
                        // just ignore that data point
                        // We turn signals into a 'useful' graph by clamping to
                        // -100 and -20 and then scaling it as a positive number.
                        var moddata = new Array();

                        for (var x = 0; x < rrdata.length; x++) {
                            var d = rrdata[x];

                            if (d == 0)
                                moddata.push(0);

                            if (d < -100)
                                d = -100;

                            if (d > -20)
                                d = -20;

                            // Normalize to 0-80
                            d = (d * -1) - 20;

                            // Reverse (weaker is worse), get as percentage
                            var rs = (80 - d) / 80;

                            moddata.push(100*rs);
                        }

                        rrdiv.sparkline(moddata, { type: "bar",
                            height: 12,
                            barColor: '#000000',
                            nullColor: '#000000',
                            zeroColor: '#000000'
                        });

                    }
                    */

                },
                { // Only show when dbm
                    field: "kismet.device.base.signal/kismet.common.signal.last_signal_dbm",
                    title: "Latest Signal",
                    help: "Most recent signal level seen, in dBm.  Signal levels may vary significantly depending on the data rates used by the device, and often, wireless drivers and devices cannot report strictly accurate signal levels.",
                    render: function(opts) {
                        return opts['value'] + " dBm";
                    },
                    filterOnZero: true,
                },
                { // Only show when rssi
                    field: "kismet.device.base.signal/kismet.common.signal.last_signal_rssi",
                    title: "Latest Signal",
                    help: "Most recent signal level seen, in RSSI.  RSSI signals are specific to the drivers reporting them and cannot be converted to a known dBm signal level.  Signal levels may vary significantly depending on the data rates used by the device, and often, wireless drivers and devices cannot report strictly accurate signal levels.",
                    render: function(opts) {
                        return opts['value'] + " RSSI";
                    },
                    filterOnZero: true,
                },
                { // Only show when dbm
                    field: "kismet.device.base.signal/kismet.common.signal.last_noise_dbm",
                    title: "Latest Noise",
                    help: "Most recent noise level seen, in dBm.  Few drivers can report noise levels.",
                    render: function(opts) {
                        return opts['value'] + " dBm";
                    },
                    filterOnZero: true,
                },
                { // Only show when rssi
                    field: "kismet.device.base.signal/kismet.common.signal.last_noise_rssi",
                    title: "Latest Noise",
                    help: "Most recent noise level seen, in RSSI.  RSSI levels are specific to the drivers reporting them and cannot be converted to a known dBm signal level.  Few drivers can report noise levels.",
                    render: function(opts) {
                        return opts['value'] + " RSSI";
                    },
                    filterOnZero: true,
                },
                { // Only show when dbm
                    field: "kismet.device.base.signal/kismet.common.signal.min_signal_dbm",
                    title: "Min. Signal",
                    help: "Weakest signal level seen, in dBm.  Signal levels may vary significantly depending on the data rates used by the device, and often, wireless drivers and devices cannot report strictly accurate signal levels.",
                    render: function(opts) {
                        return opts['value'] + " dBm";
                    },
                    filterOnZero: true,
                },
                { // Only show when rssi
                    field: "kismet.device.base.signal/kismet.common.signal.min_signal_rssi",
                    title: "Min. Signal",
                    help: "Weakest signal level seen, in RSSI.  RSSI values are specific to the capture driver and cannot be converted to standard dBm signal levels.  Signal levels may vary significantly depending on the data rates used by the device, and often, wireless drivers and devices cannot report strictly accurate signal levels.",
                    render: function(opts) {
                        return opts['value'] + " RSSI";
                    },
                    filterOnZero: true,
                },
                { // Only show when dbm
                    field: "kismet.device.base.signal/kismet.common.signal.max_signal_dbm",
                    title: "Max. Signal",
                    help: "Strongest signal level seen, in dBm.  Signal levels may vary significantly depending on the data rates used by the device, and often, wireless drivers and devices cannot report strictly accurate signal levels.",
                    render: function(opts) {
                        return opts['value'] + " dBm";
                    },
                    filterOnZero: true,
                },
                { // Only show when rssi
                    field: "kismet.device.base.signal/kismet.common.signal.max_signal_rssi",
                    title: "Max. Signal",
                    help: "Strongest signal level seen, in RSSI.  RSSI values are specific to the capture driver and cannot be converted to standard dBm signal levels.  Signal levels may vary significantly depending on the data rates used by the device, and often, wireless drivers and devices cannot report strictly accurate signal levels.",
                    filterOnZero: true,
                    render: function(opts) {
                        return opts['value'] + " RSSI";
                    },
                },
                { // Only show when dbm
                    field: "kismet.device.base.signal/kismet.common.signal.min_noise_dbm",
                    title: "Min. Noise",
                    filterOnZero: true,
                    help: "Least amount of interference or noise seen, in dBm.  Most capture drivers are not capable of measuring noise levels.",
                    render: function(opts) {
                        return opts['value'] + " dBm";
                    },
                },
                { // Only show when rssi
                    field: "kismet.device.base.signal/kismet.common.signal.min_noise_rssi",
                    title: "Min. Noise",
                    filterOnZero: true,
                    help: "Least amount of interference or noise seen, in RSSI.  Most capture drivers are not capable of measuring noise levels.",
                    render: function(opts) {
                        return opts['value'] + " RSSI";
                    },
                },
                { // Only show when dbm
                    field: "kismet.device.base.signal/kismet.common.signal.max_noise_dbm",
                    title: "Max. Noise",
                    filterOnZero: true,
                    help: "Largest amount of interference or noise seen, in dBm.  Most capture drivers are not capable of measuring noise levels.",
                    render: function(opts) {
                        return opts['value'] + " dBm";
                    },
                },
                { // Only show when rssi
                    field: "kismet.device.base.signal/kismet.common.signal.max_noise_rssi",
                    title: "Max. Noise",
                    filterOnZero: true,
                    help: "Largest amount of interference or noise seen, in dBm.  Most capture drivers are not capable of measuring noise levels.",
                    render: function(opts) {
                        return opts['value'] + " RSSI";
                    },
                },
                { // Pseudo-field of aggregated location, only show when the location is valid
                    field: "kismet.device.base.signal/kismet.common.signal.peak_loc",
                    title: "Peak Location",
                    help: "When a GPS location is available, the peak location is the coordinates at which the strongest signal level was recorded for this device.",
                    filter: function(opts) {
                        return kismet.ObjectByString(opts['data'], "kismet.device.base.signal/kismet.common.signal.peak_loc/kismet.common.location.valid") == 1;
                    },
                    render: function(opts) {
                        var loc =
                            kismet.ObjectByString(opts['data'], "kismet.device.base.signal/kismet.common.signal.peak_loc/kismet.common.location.lat") + ", " +
                            kismet.ObjectByString(opts['data'], "kismet.device.base.signal/kismet.common.signal.peak_loc/kismet.common.location.lon");

                        return loc;
                    },
                },

                ],
            },
            {
                field: "group_packet_counts",
                groupTitle: "Packets",
                id: "group_packet_counts",

                fields: [
                {
                    field: "graph_field_overall",
                    span: true,
                    render: function(opts) {
                        return '<div class="donut" id="' + opts['key'] + '" />';
                    },
                    draw: function(opts) {
                        var donutdiv = $('div', opts['container']);

                        // Make an array morris likes using our whole data record
                        var moddata = [
                        { label: "LLC/Management",
                            value: opts['data']['kismet.device.base.packets.llc'] },
                        { label: "Data",
                            value: opts['data']['kismet.device.base.packets.data'] }
                        ];

                        if (opts['data']['kismet.device.base.packets.error'] != 0)
                            moddata.push({ label: "Error",
                                value: opts['data']['kismet.device.base.packets.error'] });

                        Morris.Donut({
                            element: donutdiv,
                            data: moddata
                        });
                    }
                },
                {
                    field: "kismet.device.base.packets.total",
                    title: "Total Packets",
                    help: "Count of all packets of all types",
                },
                {
                    field: "kismet.device.base.packets.llc",
                    title: "LLC/Management",
                    help: "LLC (Link Layer Control) and Management packets are typically used for controlling and defining wireless networks.  Typically they do not carry data.",
                },
                {
                    field: "kismet.device.base.packets.error",
                    title: "Error/Invalid",
                    help: "Error and invalid packets indicate a packet was received and was partially processable, but was damaged or incorrect in some way.  Most error packets are dropped completely as it is not possible to associate them with a specific device.",
                },
                {
                    field: "kismet.device.base.packets.data",
                    title: "Data",
                    help: "Data frames carry messages and content for the device.",
                },
                {
                    field: "kismet.device.base.packets.crypt",
                    title: "Encrypted",
                    help: "Some data frames can be identified by Kismet as carrying encryption, either by the contents or by packet flags, depending on the phy type",
                },
                {
                    field: "kismet.device.base.packets.filtered",
                    title: "Filtered",
                    help: "Filtered packets are ignored by Kismet",
                },
                {
                    field: "kismet.device.base.datasize",
                    title: "Data Transferred",
                    help: "Amount of data transferred",
                    render: function(opts) {
                        return kismet.HumanReadableSize(opts['value']);
                    }
                }


                ]
            },

            {
                // Location is its own group
                groupTitle: "Avg. Location",
                // Spoofed field for ID purposes
                field: "group_avg_location",
                // Sub-table ID
                id: "group_avg_location",

                // Don't show location if we don't know it
                filter: function(opts) {
                    return (kismet.ObjectByString(opts['data'], "kismet.device.base.location/kismet.common.location.avg_loc/kismet.common.location.valid") == 1);
                },

                // Fields in subgroup
                fields: [
                {
                    field: "kismet.device.base.location/kismet.common.location.avg_loc/kismet.common.location.lat",
                    title: "Latitude"
                },
                {
                    field: "kismet.device.base.location/kismet.common.location.avg_loc/kismet.common.location.lon",
                    title: "Longitude"
                },
                {
                    field: "kismet.device.base.location/kismet.common.location.avg_loc/kismet.common.location.alt",
                    title: "Altitude (meters)",
                    filter: function(opts) {
                        return (kismet.ObjectByString(opts['data'], "kismet.device.base.location/kismet.common.location.avg_loc/kismet.common.location.fix") >= 3);
                    }

                }
                ],
            }
            ]
        });
    }
});

kismet_ui.AddDeviceDetail("packets", "Packet Graphs", 10, {
    render: function(data) {
        // Make 3 divs for s, m, h RRD
        return '<b>Packet Rates</b><br /><br />' +
            'Packets per second (last minute)<br /><div /><br />' +
            'Packets per minute (last hour)<br /><div /><br />' +
            'Packets per hour (last day)<br /><div />' +
            '<br /><b>Data</b><br /><br />' +
            'Data per second (last minute)<br /><div /><br />' +
            'Data per minute (last hour)<br /><div /><br />' +
            'Data per hour (last day)<br /><div />';
    },
    draw: function(data, target) {
        var m = $('div:eq(0)', target);
        var h = $('div:eq(1)', target);
        var d = $('div:eq(2)', target);

        var dm = $('div:eq(3)', target);
        var dh = $('div:eq(4)', target);
        var dd = $('div:eq(5)', target);

        var mdata = kismet.RecalcRrdData(data['kismet.device.base.packets.rrd']['kismet.common.rrd.last_time'], kismet_ui.last_timestamp, kismet.RRD_SECOND, data['kismet.device.base.packets.rrd']['kismet.common.rrd.minute_vec'], {});
        var hdata = kismet.RecalcRrdData(data['kismet.device.base.packets.rrd']['kismet.common.rrd.last_time'], kismet_ui.last_timestamp, kismet.RRD_MINUTE, data['kismet.device.base.packets.rrd']['kismet.common.rrd.hour_vec'], {});
        var ddata = kismet.RecalcRrdData(data['kismet.device.base.packets.rrd']['kismet.common.rrd.last_time'], kismet_ui.last_timestamp, kismet.RRD_HOUR, data['kismet.device.base.packets.rrd']['kismet.common.rrd.day_vec'], {});

        var dmdata = kismet.RecalcRrdData(data['kismet.device.base.datasize.rrd']['kismet.common.rrd.last_time'], kismet_ui.last_timestamp, kismet.RRD_SECOND, data['kismet.device.base.datasize.rrd']['kismet.common.rrd_minute_vec'], {});
        var dhdata = kismet.RecalcRrdData(data['kismet.device.base.datasize.rrd']['kismet.common.rrd.last_time'], kismet_ui.last_timestamp, kismet.RRD_MINUTE, data['kismet.device.base.datasize.rrd']['kismet.common.rrd.hour_vec'], {});
        var dddata = kismet.RecalcRrdData(data['kismet.device.base.datasize.rrd']['kismet.common.rrd.last_time'], kismet_ui.last_timestamp, kismet.RRD_HOUR, data['kismet.device.base.datasize.rrd']['kismet.common.rrd_day_vec'], {});

        m.sparkline(mdata, { type: "bar",
                height: 12,
                barColor: '#000000',
                nullColor: '#000000',
                zeroColor: '#000000'
            });
        h.sparkline(hdata,
            { type: "bar",
                height: 12,
                barColor: '#000000',
                nullColor: '#000000',
                zeroColor: '#000000'
            });
        d.sparkline(ddata,
            { type: "bar",
                height: 12,
                barColor: '#000000',
                nullColor: '#000000',
                zeroColor: '#000000'
            });

        dm.sparkline(dmdata,
            { type: "bar",
                height: 12,
                barColor: '#000000',
                nullColor: '#000000',
                zeroColor: '#000000'
            });
        dh.sparkline(dhdata,
            { type: "bar",
                height: 12,
                barColor: '#000000',
                nullColor: '#000000',
                zeroColor: '#000000'
            });
        dd.sparkline(dddata,
            { type: "bar",
                height: 12,
                barColor: '#000000',
                nullColor: '#000000',
                zeroColor: '#000000'
            });
    }
});

kismet_ui.AddDeviceDetail("seenby", "Seen By", 900, {
    filter: function(data) {
        return (Object.keys(data['kismet.device.base.seenby']).length > 1);
    },
    draw: function(data, target) {
        target.devicedata(data, {
            id: "seenbyDeviceData",

            fields: [
            {
                field: "kismet.device.base.seenby",
                id: "seenby_group",
                groupIterate: true,
                iterateTitle: function(opts) {
                    return opts['value'][opts['index']]['kismet.common.seenby.uuid'];
                },
                fields: [
                {
                    field: "kismet.common.seenby.uuid",
                    title: "UUID",
                    empty: "<i>None</i>"
                },
                {
                    field: "kismet.common.seenby.first_time",
                    title: "First Seen",
                    render: function(opts) {
                        return new Date(opts['value'] * 1000);
                    }
                },
                {
                    field: "kismet.common.seenby.last_time",
                    title: "Last Seen",
                    render: function(opts) {
                        return new Date(opts['value'] * 1000);
                    }
                },
                ]
            }]
        });
    },
});

kismet_ui.AddDeviceDetail("devel", "Dev/Debug Options", 10000, {
    render: function(data) {
        return 'Device JSON: <a href="/devices/by-key/' + data['kismet.device.base.key'] + '/device.json" target="_new">link</a><br />';
    }});

/* Sidebar:  Memory monitor
 *
 * The memory monitor looks at system_status and plots the amount of
 * ram vs number of tracked devices from the RRD
 */
kismet_ui_sidebar.AddSidebarItem({
    id: 'memory_sidebar',
    listTitle: '<i class="fa fa-tasks"></i> Memory Monitor',
    clickCallback: function() {
        exports.MemoryMonitor();
    },
});

kismet_ui_sidebar.AddSidebarItem({
    id: 'pcap_sidebar',
    priority: 10000,
    listTitle: '<i class="fa fa-download"></i> Download Pcap-NG',
    clickCallback: function() {
        location.href = "/datasource/pcap/all_sources.pcapng";
    },
});

var memoryupdate_tid;
var memory_panel = null;
var memory_chart = null;

exports.MemoryMonitor = function() {
    var w = $(window).width() * 0.75;
    var h = $(window).height() * 0.5;
    var offty = 20;

    if ($(window).width() < 450 || $(window).height() < 450) {
        w = $(window).width() - 5;
        h = $(window).height() - 5;
        offty = 0;
    }

    memory_chart = null;

    memory_panel = $.jsPanel({
        id: 'memory',
        headerTitle: '<i class="fa fa-tasks" /> Memory use',
        headerControls: {
            controls: 'closeonly',
            iconfont: 'jsglyph',
        },
        content: '<canvas id="k-mm-canvas" style="k-mm-canvas" />',
        onclosed: function() {
            clearTimeout(memoryupdate_tid);
        }
    }).resize({
        width: w,
        height: h
    }).reposition({
        my: 'center-top',
        at: 'center-top',
        of: 'window',
        offsetY: offty
    });

    memorydisplay_refresh();
}

function memorydisplay_refresh() {
    clearTimeout(memoryupdate_tid);

    if (memory_panel == null)
        return;

    if (memory_panel.is(':hidden'))
        return;

    $.get("/system/status.json")
    .done(function(data) {
        // Common rrd type and source field
        var rrdtype = kismet.RRD_MINUTE;
        var rrddata = 'kismet.common.rrd.hour_vec';

        // Common point titles
        var pointtitles = new Array();

        for (var x = 60; x > 0; x--) {
            if (x % 5 == 0) {
                pointtitles.push(x + 'm');
            } else {
                pointtitles.push(' ');
            }
        }

        var mem_linedata =
            kismet.RecalcRrdData(
                data['kismet.system.memory.rrd']['kismet.common.rrd.last_time'],
                data['kismet.system.timestamp.sec'],
                rrdtype,
                data['kismet.system.memory.rrd'][rrddata]);

        for (var p in mem_linedata) {
            mem_linedata[p] = Math.round(mem_linedata[p] / 1024);
        }

        var dev_linedata =
            kismet.RecalcRrdData(
                data['kismet.system.devices.rrd']['kismet.common.rrd.last_time'],
                data['kismet.system.timestamp.sec'],
                rrdtype,
                data['kismet.system.devices.rrd'][rrddata]);

        var datasets = [
            {
                label: 'Memory (MB)',
                fill: 'false',
                // yAxisID: 'mem-axis',
                borderColor: 'black',
                backgroundColor: 'transparent',
                data: mem_linedata,
            },
            {
                label: 'Devices',
                fill: 'false',
                // yAxisID: 'dev-axis',
                borderColor: 'blue',
                backgroundColor: 'rgba(100, 100, 255, 0.33)',
                data: dev_linedata,
            }
        ];

        if (memory_chart == null) {
            var canvas = $('#k-mm-canvas', memory_panel.content);

            memory_chart = new Chart(canvas, {
                type: 'line',
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    scales: {
                        yAxes: [
                            {
                                position: "left",
                                "id": "mem-axis",
                                ticks: {
                                    beginAtZero: true,
                                }
                            },
/*                          {
                                position: "right",
                                "id": "dev-axis",
                                ticks: {
                                    beginAtZero: true,
                                }
                            }
*/
                        ]
                    },
                },
                data: {
                    labels: pointtitles,
                    datasets: datasets
                }
            });

        } else {
            memory_chart.data.datasets = datasets;
            memory_chart.data.labels = pointtitles;
            memory_chart.update(0);
        }
    })
    .always(function() {
        memoryupdate_tid = setTimeout(memorydisplay_refresh, 5000);
    });
};

// Settings options
kismet_ui_settings.AddSettingsPane({
    id: 'base_units_measurements',
    listTitle: 'Units &amp; Measurements',
    create: function(elem) {
        elem.append(
            $('<form>', {
                id: 'form'
            })
            .append(
                $('<fieldset>', {
                    id: 'set_distance',
                })
                .append(
                    $('<legend>', { })
                    .html("Distance")
                )
                .append(
                    $('<input>', {
                        type: 'radio',
                        id: 'dst_metric',
                        name: 'distance',
                        value: 'metric',
                    })
                )
                .append(
                    $('<label>', {
                        for: 'dst_metric',
                    })
                    .html('Metric')
                )
                .append(
                    $('<input>', {
                        type: 'radio',
                        id: 'dst_imperial',
                        name: 'distance',
                        value: 'imperial',
                    })
                )
                .append(
                    $('<label>', {
                        for: 'dst_imperial',
                    })
                    .html('Imperial')
                )
            )
            .append(
                $('<br>', { })
            )
            .append(
                $('<fieldset>', {
                    id: 'set_speed'
                })
                .append(
                    $('<legend>', { })
                    .html("Speed")
                )
                .append(
                    $('<input>', {
                        type: 'radio',
                        id: 'spd_metric',
                        name: 'speed',
                        value: 'metric',
                    })
                )
                .append(
                    $('<label>', {
                        for: 'spd_metric',
                    })
                    .html('Metric')
                )
                .append(
                    $('<input>', {
                        type: 'radio',
                        id: 'spd_imperial',
                        name: 'speed',
                        value: 'imperial',
                    })
                )
                .append(
                    $('<label>', {
                        for: 'spd_imperial',
                    })
                    .html('Imperial')
                )
            )
            .append(
                $('<br>', { })
            )
            .append(
                $('<fieldset>', {
                    id: 'set_temp'
                })
                .append(
                    $('<legend>', { })
                    .html("Temperature")
                )
                .append(
                    $('<input>', {
                        type: 'radio',
                        id: 'temp_celcius',
                        name: 'temp',
                        value: 'celcius',
                    })
                )
                .append(
                    $('<label>', {
                        for: 'temp_celcius',
                    })
                    .html('Celcius')
                )
                .append(
                    $('<input>', {
                        type: 'radio',
                        id: 'temp_fahrenheit',
                        name: 'temp',
                        value: 'fahrenheit',
                    })
                )
                .append(
                    $('<label>', {
                        for: 'temp_fahrenheit',
                    })
                    .html('Fahrenheit')
                )
            )
        );

        $('#form', elem).on('change', function() {
            kismet_ui_settings.SettingsModified();
        });

        if (kismet.getStorage('kismet.base.unit.distance', 'metric') === 'metric') {
            $('#dst_metric', elem).attr('checked', 'checked');
        } else {
            $('#dst_imperial', elem).attr('checked', 'checked');
        }

        if (kismet.getStorage('kismet.base.unit.speed', 'metric') === 'metric') {
            $('#spd_metric', elem).attr('checked', 'checked');
        } else {
            $('#spd_imperial', elem).attr('checked', 'checked');
        }

        if (kismet.getStorage('kismet.base.unit.temp', 'celcius') === 'celcius') {
            $('#temp_celcius', elem).attr('checked', 'checked');
        } else {
            $('#temp_fahrenheit', elem).attr('checked', 'checked');
        }

        $('#set_distance', elem).controlgroup();
        $('#set_speed', elem).controlgroup();
        $('#set_temp', elem).controlgroup();

    },
    save: function(elem) {
        var dist = $("input[name='distance']:checked", elem).val();
        kismet.putStorage('kismet.base.unit.distance', dist);
        var spd = $("input[name='speed']:checked", elem).val();
        kismet.putStorage('kismet.base.unit.speed', spd);
        var tmp = $("input[name='temp']:checked", elem).val();
        kismet.putStorage('kismet.base.unit.temp', tmp);

        return true;
    },
});

kismet_ui_settings.AddSettingsPane({
    id: 'base_plugins',
    listTitle: 'Plugins',
    create: function(elem) {
        elem.append($('<i>').html('Loading plugin data...'));

        $.get("/plugins/all_plugins.json")
        .done(function(data) {
            elem.empty();
    
            if (data.length == 0) {
                elem.append($('<i>').html('No plugins loaded...'));
            }

            for (var pi in data) {
                var pl = data[pi];

                var sharedlib = $('<p>');

                if (pl['kismet.plugin.shared_object'].length > 0) {
                    sharedlib.html("Native code from " + pl['kismet.plugin.shared_object']);
                } else {
                    sharedlib.html("No native code");
                }

                elem.append(
                    $('<div>', { 
                        class: 'k-b-s-plugin-title',
                    })
                    .append(
                        $('<b>', {
                            class: 'k-b-s-plugin-title',
                        })
                        .html(pl['kismet.plugin.name'])
                    )
                    .append(
                        $('<span>', { })
                        .html(pl['kismet.plugin.version'])
                    )
                )
                .append(
                    $('<div>', {
                        class: 'k-b-s-plugin-content',
                    })
                    .append(
                        $('<p>', { })
                        .html(pl['kismet.plugin.description'])
                    )
                    .append(
                        $('<p>', { })
                        .html(pl['kismet.plugin.author'])
                    )
                    .append(sharedlib)
                );
            }
        });
    },
    save: function(elem) {

    },
});


kismet_ui_settings.AddSettingsPane({
    id: 'base_login_password',
    listTitle: 'Login &amp; Password',
    create: function(elem) {
        elem.append(
            $('<form>', {
                id: 'form'
            })
            .append(
                $('<fieldset>', {
                    id: 'fs_login'
                })
                .append(
                    $('<legend>', {})
                    .html('Server Login')
                )
                .append(
                    $('<p>')
                    .html('Kismet requires a username and password for functionality which changes the server, such as adding interfaces or changing configuration.')
                )
                .append(
                    $('<p>')
                    .html('By default, the first time Kismet runs it generates a random password, which is stored in the file <code>~/.kismet/kismet_httpd.conf</code> in the home directory of the user running Kismet.  You will need this password to configure data sources, download pcap and other logs, or change server-side settings.')
                )
                .append(
                    $('<p>')
                    .html('If you are a guest on this server you may continue without entering an admin password, but you will not be able to perform some actions or view some data.')
                )
                .append(
                    $('<br>')
                )
                .append(
                    $('<span style="display: inline-block; width: 8em;">')
                    .html('User name: ')
                )
                .append(
                    $('<input>', {
                        type: 'text',
                        name: 'user',
                        id: 'user'
                    })
                )
                .append(
                    $('<br>')
                )
                .append(
                    $('<span style="display: inline-block; width: 8em;">')
                    .html('Password: ')
                )
                .append(
                    $('<input>', {
                        type: 'password',
                        name: 'password',
                        id: 'password'
                    })
                )
                .append(
                    $('<span>', {
                        id: 'pwsuccessdiv',
                        style: 'padding-left: 5px',
                    })
                    .append(
                        $('<i>', {
                            id: 'pwsuccess',
                            class: 'fa fa-refresh fa-spin',
                        })
                    )
                    .append(
                        $('<span>', {
                            id: 'pwsuccesstext'
                        })
                    )
                    .hide()
                )
            )
        );

        $('#form', elem).on('change', function() {
            kismet_ui_settings.SettingsModified();
        });

        var checker_cb = function() {
            // Cancel any pending timer
            if (pw_check_tid > -1)
                clearTimeout(pw_check_tid);

            var checkerdiv = $('#pwsuccessdiv', elem);
            var checker = $('#pwsuccess', checkerdiv);
            var checkertext = $('#pwsuccesstext', checkerdiv);

            checker.removeClass('fa-exclamation-circle');
            checker.removeClass('fa-check-square');

            checker.addClass('fa-spin');
            checker.addClass('fa-refresh');
            checkertext.text("  Checking...");

            checkerdiv.show();

            // Set a timer for a second from now to call the actual check 
            // in case the user is still typing
            pw_check_tid = setTimeout(function() {
                exports.LoginCheck(function(success) {
                    if (!success) {
                        checker.removeClass('fa-check-square');
                        checker.removeClass('fa-spin');
                        checker.removeClass('fa-refresh');
                        checker.addClass('fa-exclamation-circle');
                        checkertext.text("  Invalid login");
                    } else {
                        checker.removeClass('fa-exclamation-circle');
                        checker.removeClass('fa-spin');
                        checker.removeClass('fa-refresh');
                        checker.addClass('fa-check-square');
                        checkertext.text("");
                    }
                }, $('#user', elem).val(), $('#password', elem).val());
            }, 1000);
        };

        var pw_check_tid = -1;
        jQuery('#password', elem).on('input propertychange paste', function() {
            kismet_ui_settings.SettingsModified();
            checker_cb();
        });
        jQuery('#user', elem).on('input propertychange paste', function() {
            kismet_ui_settings.SettingsModified();
            checker_cb();
        });

        $('#user', elem).val(kismet.getStorage('kismet.base.login.username', 'kismet'));
        $('#password', elem).val(kismet.getStorage('kismet.base.login.password', 'kismet'));

        if ($('#user', elem).val() === 'kismet' &&
        $('#password', elem).val() === 'kismet') {
            $('#defaultwarning').show();
        }

        $('fs_login', elem).controlgroup();

        // Check the current pw
        checker_cb();
    },
    save: function(elem) {
        kismet.putStorage('kismet.base.login.username', $('#user', elem).val());
        kismet.putStorage('kismet.base.login.password', $('#password', elem).val());
    },
});

/* Add the messages and channels tabs */
kismet_ui_tabpane.AddTab({
    id: 'messagebus',
    tabTitle: 'Messages',
    createCallback: function(div) {
        div.messagebus();
    },
    priority: -1001,
});

kismet_ui_tabpane.AddTab({
    id: 'channels',
    tabTitle: 'Channels',
    expandable: true,
    createCallback: function(div) {
        div.channels();
    },
    priority: -1000,
});

exports.DeviceSignalDetails = function(key) {
    var w = $(window).width() * 0.75;
    var h = $(window).height() * 0.5;

    var devsignal_chart = null;

    var devsignal_tid = -1;

    var content =
        $('<div>', {
            class: 'k-dsd-container'
        })
        .append(
            $('<div>', {
                class: 'k-dsd-info'
            })
            .append(
                $('<div>', {
                    class: 'k-dsd-title'
                })
                .html("Signal")
            )
            .append(
                $('<table>', {
                    class: 'k-dsd-table'
                })
                .append(
                    $('<tr>', {
                    })
                    .append(
                        $('<td>', {
                            width: '50%'
                        })
                        .html("Last Signal:")
                    )
                    .append(
                        $('<td>', {
                            width: '50%',
                        })
                        .append(
                            $('<span>', {
                                class: 'k-dsd-lastsignal',
                            })
                        )
                        .append(
                            $('<i>', {
                                class: 'fa k-dsd-arrow k-dsd-arrow-down',
                            })
                            .hide()
                        )
                    )
                )
                .append(
                    $('<tr>', {
                    })
                    .append(
                        $('<td>', {
                            width: '50%'
                        })
                        .html("Min Signal:")
                    )
                    .append(
                        $('<td>', {
                            width: '50%',
                            class: 'k-dsd-minsignal',
                        })
                        .html("n/a")
                    )
                )
                .append(
                    $('<tr>', {
                    })
                    .append(
                        $('<td>', {
                            width: '50%'
                        })
                        .html("Max Signal:")
                    )
                    .append(
                        $('<td>', {
                            width: '50%',
                            class: 'k-dsd-maxsignal',
                        })
                        .html("n/a")
                    )
                )
            )
        )
        .append(
            $('<div>', {
                class: 'k-dsd-graph'
            })
            .append(
                $('<canvas>', {
                    id: 'k-dsd-canvas',
                    class: 'k-dsd-canvas'
                })
            )
        );

    var devsignal_panel = $.jsPanel({
        id: 'devsignal' + key,
        headerTitle: '<i class="fa fa-signal" /> Signal',
        headerControls: {
            iconfont: 'jsglyph',
        },
        content: content,
        onclosed: function() {
            clearTimeout(devsignal_tid);
        }
    }).resize({
        width: w,
        height: h
    }).reposition({
        my: 'center-top',
        at: 'center-top',
        of: 'window',
        offsetY: 20
    });

    var emptyminute = new Array();
    for (var x = 0; x < 60; x++) {
        emptyminute.push(0);
    }

    devsignal_tid = devsignal_refresh(key, devsignal_panel,
        devsignal_chart, devsignal_tid, 0, emptyminute);
}

function devsignal_refresh(key, devsignal_panel, devsignal_chart,
    devsignal_tid, lastsignal, fakerrd) {
    clearTimeout(devsignal_tid);

    if (devsignal_panel == null)
        return;

    if (devsignal_panel.is(':hidden'))
        return;

    var signal = lastsignal;

    $.get("/devices/by-key/" + key + "/device.json")
    .done(function(data) {
        var title = '<i class="fa fa-signal" /> Signal ' +
            data['kismet.device.base.macaddr'] + ' ' +
            data['kismet.device.base.name'];
        devsignal_panel.headerTitle(title);

        var sigicon = $('.k-dsd-arrow', devsignal_panel.content);

        sigicon.removeClass('k-dsd-arrow-up');
        sigicon.removeClass('k-dsd-arrow-down');
        sigicon.removeClass('fa-arrow-up');
        sigicon.removeClass('fa-arrow-down');

        signal = data['kismet.device.base.signal']['kismet.common.signal.last_signal_dbm'];
        console.log(signal + " " + lastsignal);

        if (signal < lastsignal) {
            sigicon.addClass('k-dsd-arrow-down');
            sigicon.addClass('fa-arrow-down');
            sigicon.show();
        } else {
            sigicon.addClass('k-dsd-arrow-up');
            sigicon.addClass('fa-arrow-up');
            sigicon.show();
        }

        $('.k-dsd-lastsignal', devsignal_panel.content)
        .text(signal + " dBm");

        $('.k-dsd-minsignal', devsignal_panel.content)
        .text(data['kismet.device.base.signal']['kismet.common.signal.min_signal_dbm'] + " dBm");

        $('.k-dsd-maxsignal', devsignal_panel.content)
        .text(data['kismet.device.base.signal']['kismet.common.signal.max_signal_dbm'] + " dBm");

        // Common point titles
        var pointtitles = new Array();

        for (var x = 60; x > 0; x--) {
            if (x % 5 == 0) {
                pointtitles.push(x + 's');
            } else {
                pointtitles.push(' ');
            }
        }


        /*
        var rrdata = kismet.RecalcRrdData(
            data['kismet.device.base.signal']['kismet.common.signal.signal_rrd']['kismet.common.rrd.last_time'],
            data['kismet.device.base.signal']['kismet.common.signal.signal_rrd']['kismet.common.rrd.last_time'],
            kismet.RRD_SECOND,
            data['kismet.device.base.signal']['kismet.common.signal.signal_rrd']['kismet.common.rrd.minute_vec'], {});

        // We assume the 'best' a signal can usefully be is -20dbm,
        // that means we're right on top of it.
        // We can assume that -100dbm is a sane floor value for
        // the weakest signal.
        // If a signal is 0 it means we haven't seen it at all so
        // just ignore that data point
        // We turn signals into a 'useful' graph by clamping to
        // -100 and -20 and then scaling it as a positive number.
        var moddata = new Array();

        for (var x = 0; x < rrdata.length; x++) {
            var d = rrdata[x];

            if (d == 0) {
                moddata.push(0);
                continue;
            }

            if (d < -100)
                d = -100;

            if (d > -20)
                d = -20;

            // Normalize to 0-80
            d = (d * -1) - 20;

            // Reverse (weaker is worse), get as percentage
            var rs = (80 - d) / 80;

            moddata.push(100*rs);
        }
        */

        var msignal = signal;

        if (msignal == 0) {
            fakerrd.push(0);
        } else if (msignal < -100) {
            msignal = -100;
        } else if (msignal > -20) {
            msignal = -20;
        }

        msignal = (msignal * -1) - 20;
        var rs = (80 - msignal) / 80;

        fakerrd.push(100 * rs);

        fakerrd.splice(0, 1);

        var moddata = fakerrd;

        var datasets = [
            {
                label: 'Signal (%)',
                fill: 'false',
                borderColor: 'blue',
                backgroundColor: 'rgba(100, 100, 255, 0.83)',
                data: moddata,
            },
        ];

        if (devsignal_chart == null) {
            var canvas = $('#k-dsd-canvas', devsignal_panel.content);

            devsignal_chart = new Chart(canvas, {
                type: 'bar',
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    animation: false,
                    scales: {
                        yAxes: [ {
                            ticks: {
                                beginAtZero: true,
                                max: 100,
                            }
                        }],
                    },
                },
                data: {
                    labels: pointtitles,
                    datasets: datasets
                }
            });
        } else {
            devsignal_chart.data.datasets[0].data = moddata;
            devsignal_chart.update();
        }


    })
    .always(function() {
        devsignal_tid = setTimeout(function() {
                devsignal_refresh(key, devsignal_panel,
                    devsignal_chart, devsignal_tid, signal, fakerrd);
        }, 1000);
    });
};

exports.login_error = false;
exports.login_pending = false;

exports.LoginCheck = function(cb, user, pw) {
    user = user || kismet.getStorage('kismet.base.login.username', 'kismet');
    pw = pw || kismet.getStorage('kismet.base.login.password', 'kismet');

    $.ajax({
        url: "/session/check_login",

        beforeSend: function (xhr) {
            xhr.setRequestHeader ("Authorization", "Basic " + btoa(user + ":" + pw));
        },

        xhrFields: {
            withCredentials: false
        },

        error: function(jqXHR, textStatus, errorThrown) {
            cb(false);
        },

        success: function(data, textStatus, jqXHR) {
            cb(true);
        }

    });
}

exports.FirstLoginCheck = function() {
    if (kismet.getStorage('kismet.base.warn_login', false) == false) {
        var loginpanel = null; 

        var content = 
            $('<div>', {
                style: 'padding: 10px;'
            })
            .append(
                $('<h3>', { }
                )
                .append(
                    $('<i>', {
                        class: 'fa fa-exclamation-triangle',
                        style: 'color: red; padding-right: 5px;'
                    })
                )
                .append("Error")
            )
            .append(
                $('<p>')
                .html('Invalid admin login for the Kismet webserver.  To perform some functions (such as starting or stopping data sources, downloading captures, or changing server-side settings), you must be logged into Kismet.')
            )
            .append(
                $('<p>')
                .html('The kismet login is defined in kismet_httpd.conf; If you are a guest on this server, you can continue to view much of the information without logging in but you will not be able to change configurations')
            )
            .append(
                $('<form>', {
                    id: 'form'
                })
                .append(
                    $('<div>', {
                        style: 'float: right;',
                    })
                    .append(
                        $('<input>', {
                            type: 'checkbox',
                            id: 'dontwarn',
                            name: 'dontwarn',
                            value: '',
                        })
                    )
                    .append(
                        $('<label>', {
                            for: 'dontwarn',
                        })
                        .html('Don\'t warn again')
                    )
                )
            )
            .append(
                $('<div>', {
                    style: 'padding-top: 10px;'
                })
                .append(
                    $('<button>', {
                        class: 'k-wl-button-close',
                    })
                    .text('Continue')
                    .button()
                    .on('click', function() {
                        loginpanel.close();               
                    })
                )
                .append(
                    $('<button>', {
                        class: 'k-wl-button-settings',
                        style: 'position: absolute; right: 5px;',
                    })
                    .text('Settings')
                    .button()
                    .on('click', function() {
                        loginpanel.close();               
                        kismet_ui_settings.ShowSettings('base_login_password');
                    })
                )

            );

        $('#dontwarn', content)
            .checkboxradio()
            .on('change', function() {
                kismet.putStorage('kismet.base.warn_login', $(this).is(':checked'));
            });


        var w = ($(window).width() / 2) - 5;
        if (w < 450) {
            w = $(window).width() - 5;
        }

        exports.LoginCheck(function(success) {
            if (!success) {
                loginpanel = $.jsPanel({
                    id: "login-alert",
                    headerTitle: '<i class="fa fa-exclamation-triangle"></i> Login Error',
                    headerControls: {
                        controls: 'closeonly',
                        iconfont: 'jsglyph',
                    },
                    contentSize: w + " auto",
                    paneltype: 'modal',
                    content: content,
                });

                return true;
            }
        });

    }

}

exports.FirstTimeCheck = function() {
    var welcomepanel = null; 
    if (kismet.getStorage('kismet.base.seen_welcome', false) == false) {
        var content = 
            $('<div>', {
                style: 'padding: 10px;'
            })
            .append(
                $('<p>', { }
                )
                .html("Welcome!")
            )
            .append(
                $('<p>')
                .html('This is the first time you\'ve used this Kismet server in this browser.')
            )
            .append(
                $('<p>')
                .html('Kismet stores local settings in the HTML5 storage of your browser.')
            )
            .append(
                $('<p>')
                .html('You should configure your preferences and login settings in the settings panel!')
            )
            .append(
                $('<div>', {})
                .append(
                    $('<button>', {
                        class: 'k-w-button-settings'
                    })
                    .text('Settings')
                    .button()
                    .on('click', function() {
                        welcomepanel.close();               
                        kismet_ui_settings.ShowSettings();
                    })
                )
                .append(
                    $('<button>', {
                        class: 'k-w-button-close',
                        style: 'position: absolute; right: 5px;',
                    })
                    .text('Continue')
                    .button()
                    .on('click', function() {
                        welcomepanel.close();
                    })
                )

            );

        welcomepanel = $.jsPanel({
            id: "welcome-alert",
            headerTitle: '<i class="fa fa-power-off"></i> Welcome',
            headerControls: {
                controls: 'closeonly',
                iconfont: 'jsglyph',
            },
            contentSize: "auto auto",
            paneltype: 'modal',
            content: content,
        });

        kismet.putStorage('kismet.base.seen_welcome', true);

        return true;
    }

    return false;
}

/* Highlight active devices */
kismet_ui.AddDeviceRowHighlight({
    name: "Active",
    description: "Device has been active in the past 10 seconds",
    priority: 500,
    defaultcolor: "#cee1ff",
    defaultenable: false,
    fields: [
        'kismet.device.base.last_time'
    ],
    selector: function(data) {
        var ts = data['kismet.device.base.last_time'];

        return (kismet.timestamp_sec - ts < 10);
    }
});

// We're done loading
exports.load_complete = 1;

return exports;

});
