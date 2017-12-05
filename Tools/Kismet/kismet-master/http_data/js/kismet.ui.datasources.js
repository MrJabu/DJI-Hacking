(
  typeof define === "function" ? function (m) { define("kismet-ui-datasource-js", m); } :
  typeof exports === "object" ? function (m) { module.exports = m(); } :
  function(m){ this.kismet_ui_datasources = m(); }
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
        href: '/css/kismet.ui.datasources.css'
    });


/* Sidebar:  Channel coverage
 *
 * The channel coverage looks at the data sources and plots a moving graph
 * of all channels and how they're covered; it reflects how the pattern will
 * work, but not, necessarily, reality itself.
 */
kismet_ui_sidebar.AddSidebarItem({
    id: 'datasource_channel_coverage',
    listTitle: '<i class="fa fa-bar-chart-o"></i> Channel Coverage',
    clickCallback: function() {
        exports.ChannelCoverage();
    },
});

var channelcoverage_backend_tid;
var channelcoverage_display_tid;
var channelcoverage_panel = null;
var channelcoverage_canvas = null;
var channelhop_canvas = null;
var channelcoverage_chart = null;
var channelhop_chart = null;
var cc_uuid_pos_map = {};

exports.ChannelCoverage = function() {
    var w = $(window).width() * 0.85;
    var h = $(window).height() * 0.75;
    var offy = 20;

    if ($(window).width() < 450 || $(window).height() < 450) {
        w = $(window).width() - 5;
        h = $(window).height() - 5;
        offy = 0;
    }

    channelcoverage_chart = null;
    channelhop_chart = null;

    var content =
        $('<div>', {
            id: "k-cc-main",
            class: "k-cc-main",
        })
        .append(
            $('<ul>', {
                id: "k-cc-tab-ul"
            })
            .append(
                $('<li>', { })
                .append(
                    $('<a>', {
                        href: '#k-cc-tab-coverage'
                    })
                    .html("Channel Coverage")
                )
            )
            .append(
                $('<li>', { })
                .append(
                    $('<a>', {
                        href: '#k-cc-tab-estimate'
                    })
                    .html("Estimated Hopping")
                )
            )
        )
        .append(
            $('<div>', {
                id: 'k-cc-tab-coverage',
                class: 'k-cc-canvas'
            })
            .append(
                $('<canvas>', {
                    id: 'k-cc-cover-canvas',
                    class: 'k-cc-canvas'
                })
            )
        )
        .append(
            $('<div>', {
                id: 'k-cc-tab-estimate',
                class: 'k-cc-canvas'
            })
            .append(
                $('<canvas>', {
                    id: 'k-cc-canvas',
                    class: 'k-cc-canvas'
                })
            )
        );

    channelcoverage_panel = $.jsPanel({
        id: 'channelcoverage',
        headerTitle: '<i class="fa fa-bar-chart-o" /> Channel Coverage',
        headerControls: {
            iconfont: 'jsglyph',
            minimize: 'remove',
            smallify: 'remove',
        },
        content: content,
        onclosed: function() {
            clearTimeout(channelcoverage_backend_tid);
            clearTimeout(channelcoverage_display_tid);
            channelhop_chart = null;
            channelhop_canvas = null;
            channelcoverage_canvas = null;
            channelcoverage_chart = null;
        },
        onresized: resize_channelcoverage,
        onmaximized: resize_channelcoverage,
        onnormalized: resize_channelcoverage,
    })
    .on('resize', function() {
        resize_channelcoverage();
    }).resize({
        width: w,
        height: h
    }).reposition({
        my: 'center-top',
        at: 'center-top',
        of: 'window',
        offsetY: offy,
    });

    content.tabs({
        heightStyle: 'fill'
    });


    channelcoverage_backend_refresh();
    channelcoverage_display_refresh();
}

function channelcoverage_backend_refresh() {
    clearTimeout(channelcoverage_backend_tid);

    if (channelcoverage_panel == null)
        return;

    if (channelcoverage_panel.is(':hidden'))
        return;

    $.get("/datasource/all_sources.json")
    .done(function(data) {
        // Build a list of all devices we haven't seen before and set their
        // initial positions to match
        for (var di = 0; di < data.length; di++) {
            if (data[di]['kismet.datasource.running'] == 0) {
                if ((data[di]['kismet.datasource.uuid'] in cc_uuid_pos_map)) {
                   delete cc_uuid_pos_map[data[di]['kismet.datasource.uuid']];
                }
            } else if (!(data[di]['kismet.datasource.uuid'] in cc_uuid_pos_map)) {
                cc_uuid_pos_map[data[di]['kismet.datasource.uuid']] = {
                    uuid: data[di]['kismet.datasource.uuid'],
                    name: data[di]['kismet.datasource.name'],
                    interface: data[di]['kismet.datasource.interface'],
                    hopping: data[di]['kismet.datasource.hopping'],
                    channel: data[di]['kismet.datasource.channel'],
                    channels: data[di]['kismet.datasource.hop_channels'],
                    offset: data[di]['kismet.datasource.hop_offset'],
                    position: data[di]['kismet.datasource.hop_offset'],
                    skip: data[di]['kismet.datasource.hop_shuffle_skip'],
                };
            }

        }
    })
    .always(function() {
        channelcoverage_backend_tid = setTimeout(channelcoverage_backend_refresh, 5000);
    });
}

function resize_channelcoverage() {
    if (channelcoverage_panel == null)
        return;

    var container = $('#k-cc-main', channelcoverage_panel.content);

    var tabs = $('#k-cc-tab-ul', container);

    var w = container.width();
    var h = container.height() - tabs.outerHeight();

    $('#k-cc-tab-estimate', container)
        .css('width', w)
        .css('height', h);

    if (channelhop_canvas != null) {
        channelhop_canvas
            .css('width', w)
            .css('height', h);

        if (channelhop_chart != null)
             channelhop_chart.resize();
    }

    $('#k-cc-tab-coverage', container)
        .css('width', w)
        .css('height', h);

    if (channelcoverage_canvas != null) {
        channelcoverage_canvas
            .css('width', w)
            .css('height', h);

        if (channelcoverage_chart != null)
             channelcoverage_chart.resize();
    }

}

function channelcoverage_display_refresh() {
    clearTimeout(channelcoverage_display_tid);

    if (channelcoverage_panel == null)
        return;

    if (channelcoverage_panel.is(':hidden'))
        return;

    // Now we know all the sources; make a list of all channels and figure
    // out if we're on any of them; each entry in total_channel_list contains
    // an array of UUIDs on this channel in this sequence
    var total_channel_list = {}

    for (var du in cc_uuid_pos_map) {
        var d = cc_uuid_pos_map[du];

        if (d['hopping']) {
            for (var ci = 0; ci < d['channels'].length; ci++) {
                var chan = d['channels'][ci];
                if (!(chan in total_channel_list)) {
                    total_channel_list[chan] = [ ];
                }

                if ((d['position'] % d['channels'].length) == ci) {
                    total_channel_list[chan].push(du);
                }
            }

            // Increment the virtual channel position for the graph
            if (d['skip'] == 0) {
                d['position'] = d['position'] + 1;
            } else {
                d['position'] = d['position'] + d['skip'];
            }
        } else {
            // Non-hopping sources are always on their channel
            var chan = d['channel'];

            if (!(chan in total_channel_list)) {
                total_channel_list[chan] = [ du ];
            } else {
                total_channel_list[chan].push(du);
            }
        }
    }

    // Create the channel index for the x-axis, used in both the hopping and the coverage
    // graphs
    var chantitles = new Array();
    for (var ci in total_channel_list) {
        chantitles.push(ci);
    }

    // Perform a natural sort on it to get it in order
    var ncollator = new Intl.Collator(undefined, {numeric: true, sensitivity: 'base'});
    chantitles.sort(ncollator.compare);

    // Create the source datasets for the animated estimated hopping graph, covering all
    // channels and highlighting the channels we have a UUID in
    var source_datasets = new Array()

    var ndev = 0;

    for (var du in cc_uuid_pos_map) {
        var d = cc_uuid_pos_map[du];

        var dset = [];

        for (var ci in chantitles) {
            var clist = total_channel_list[chantitles[ci]];

            if (clist.indexOf(du) < 0) {
                dset.push(0);
            } else {
                dset.push(1);
            }
        }

        var color = "hsl(" + parseInt(255 * (ndev / Object.keys(cc_uuid_pos_map).length)) + ", 100%, 50%)";

        source_datasets.push({
            label: d['name'],
            data: dset,
            borderColor: color,
            backgroundColor: color,
        });

	    ndev++;
    }

    // Create the source list for the Y axis of the coverage graph; we make an intermediary
    // which is sorted by name but includes UUID, then assemble the final one
    var sourcetitles_tmp = new Array();
    var sourcetitles = new Array();

    for (var ci in cc_uuid_pos_map) {
        sourcetitles_tmp.push({
            name: cc_uuid_pos_map[ci].name,
            uuid: ci
        });
    }

    sourcetitles_tmp.sort(function(a, b) {
        return a.name.localeCompare(b.name);
    });

    // Build the titles
    for (var si in sourcetitles_tmp) {
        sourcetitles.push(sourcetitles_tmp[si].name);
    }

    var bubble_dataset = new Array();

    // Build the bubble data
    ndev = 0;
    for (var si in sourcetitles_tmp) {
        var d = cc_uuid_pos_map[sourcetitles_tmp[si].uuid];
        var ds = new Array;

        if (d.hopping) {
            for (var ci in d.channels) {
                var c = d.channels[ci];

                var cp = chantitles.indexOf(c);

                if (cp < 0)
                    continue;

                ds.push({
                    x: cp,
                    y: si,
                    r: 5
                });
            }
        } else {
            var cp = chantitles.indexOf(d.channel);
            if (cp >= 0) {
                ds.push({
                    x: cp,
                    y: si,
                    r: 5
                });
            }
        }

        var color = "hsl(" + parseInt(255 * (ndev / Object.keys(cc_uuid_pos_map).length)) + ", 100%, 50%)";

        bubble_dataset.push({
            label: d.name,
            data: ds,
            borderColor: color,
            backgroundColor: color,
        });

        ndev++;

    }

    if (channelhop_canvas == null) {
        channelhop_canvas = $('#k-cc-canvas', channelcoverage_panel.content);

        var bp = 5.0;

        if (chantitles.length < 14)
            bp = 2;

        channelhop_chart = new Chart(channelhop_canvas, {
            type: "bar",
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    xAxes: [{ barPercentage: bp, }],
                },
            },
            data: {
                labels: chantitles,
                datasets: source_datasets,
            },
        });
    } else {
        channelhop_chart.data.datasets = source_datasets;
        channelhop_chart.data.labels = chantitles;
        channelhop_chart.update(0);
    }

    if (channelcoverage_canvas == null && sourcetitles.length != 0) {
        channelcoverage_canvas = $('#k-cc-cover-canvas', channelcoverage_panel.content);

        channelcoverage_chart = new Chart(channelcoverage_canvas, {
            type: 'bubble',
            options: {
                title: {
                    display: true,
                    text: 'Per-Source Channel Coverage',
                },
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    xAxes: [{
                        ticks: {
                            autoSkip: false,
                            stepSize: 1,
                            callback: function(value, index, values) {
                                return chantitles[value];
                            },
                            min: 0,
                            max: chantitles.length,
                            position: 'bottom',
                            type: 'linear',
                        }
                    }],
                    yAxes: [{
                        ticks: {
                            autoSkip: false,
                            stepSize: 1,
                            callback: function(value, index, values) {
                                return sourcetitles[value];
                            },
                            min: 0,
                            max: sourcetitles.length,
                            position: 'left',
                            type: 'linear',
                        },
                    }],
                },
            },
            data: {
                labels: chantitles,
                yLabels: sourcetitles,
                datasets: bubble_dataset,
            },
        });
    } else if (sourcetitles.length != 0) {
        channelcoverage_chart.data.datasets = bubble_dataset;

        channelcoverage_chart.data.labels = chantitles;
        channelcoverage_chart.data.yLabels = sourcetitles;

        channelcoverage_chart.options.scales.xAxes[0].ticks.min = 0;
        channelcoverage_chart.options.scales.xAxes[0].ticks.max = chantitles.length;

        channelcoverage_chart.options.scales.yAxes[0].ticks.min = 0;
        channelcoverage_chart.options.scales.yAxes[0].ticks.max = sourcetitles.length;

        channelcoverage_chart.update(0);
    }

    channelcoverage_display_tid = setTimeout(channelcoverage_display_refresh, 500);
}

/* Sidebar:  Data sources (new)
 *
 * Data source management panel
 */
kismet_ui_sidebar.AddSidebarItem({
    id: 'datasource_sources2',
    listTitle: '<i class="fa fa-cogs"></i> Data Sources',
    priority: -500,
    clickCallback: function() {
        exports.DataSources2();
    },
});

function update_datasource2(data, state) {
    var set_row = function(sdiv, id, title, content) {
        var r = $('tr#' + id, sdiv);

        if (r.length == 0) {
            r = $('<tr>', { id: id })
            .append($('<td>'))
            .append($('<td>'));

            $('.k-ds-table', sdiv).append(r);
        }

        $('td:eq(0)', r).replaceWith($('<td>').append(title));
        $('td:eq(1)', r).replaceWith($('<td>').append(content));
    }

    var top_row = function(sdiv, id, title, content) {
        var r = $('tr#' + id, sdiv);

        if (r.length == 0) {
            r = $('<tr>', { id: id })
            .append($('<td>'))
            .append($('<td>'));

            $('.k-ds-table', sdiv).prepend(r);
        }

        $('td:eq(0)', r).replaceWith($('<td>').append(title));
        $('td:eq(1)', r).replaceWith($('<td>').append(content));
    }

    // Defer if we're waiting for a command to finish; do NOTHING else
    if ('defer_command_progress' in state && state['defer_command_progress'])
        return;

    // Clean up missing probed interfaces
    $('.interface', state['content']).each(function(i) {
        var found = false;

        for (var intf of state['kismet_interfaces']) {
            if ($(this).attr('id') === intf['kismet.datasource.probed.interface']) {
                if (intf['kismet.datasource.probed.in_use_uuid'] !== '00000000-0000-0000-0000-000000000000') {
                    break;
                }
                found = true;
                break;
            }
        }

        if (!found) {
            $(this).remove();
        }
    });

    // Clean up missing sources
    $('.source', state['content']).each(function(i) {
        var found = false;

        for (var source of state['kismet_sources']) {
            if ($(this).attr('id') === source['kismet.datasource.uuid']) {
                found = true;
                break;
            }
        }

        if (!found) {
            $(this).remove();
        }
    });

    var defer = false;

    // Defer if we're waiting for a system update
    if ('defer_source_update' in state && state['defer_source_update']) 
        defer = true;


    for (var intf of state['kismet_interfaces']) {
        if (intf['kismet.datasource.probed.in_use_uuid'] !== '00000000-0000-0000-0000-000000000000') {
            $('#' + intf['kismet.datasource.probed.interface'], state['content']).remove();
            continue;
        }

        var idiv = $('#' + intf['kismet.datasource.probed.interface'], state['content']);

        if (idiv.length == 0) {
            idiv = $('<div>', {
                id: intf['kismet.datasource.probed.interface'],
                class: 'accordion interface',
                })
            .append(
                $('<h3>', {
                    id: 'header',
                })
                .append(
                    $('<span>', {
                        class: 'k-ds-source',
                    })
                    .html("Available Interface: " + intf['kismet.datasource.probed.interface'] + ' (' + intf['kismet.datasource.type_driver']['kismet.datasource.driver.type'] + ')')
                )
            ).append(
                $('<div>', {
                    id: 'content',
                    class: 'k-ds-content',
                })
            );

            var table = $('<table>', {
                class: 'k-ds-table'
                });

            $('#content', idiv).append(table);

            idiv.accordion({ collapsible: true, active: false });

            state['content'].append(idiv);
        }

        set_row(idiv, 'interface', '<b>Interface</b>', intf['kismet.datasource.probed.interface']);
        set_row(idiv, 'driver', '<b>Capture Driver</b>', intf['kismet.datasource.type_driver']['kismet.datasource.driver.type']);
        set_row(idiv, 'description', '<b>Type</b>', intf['kismet.datasource.type_driver']['kismet.datasource.driver.description']);

        var addbutton = $('#add', idiv);
        if (addbutton.length == 0) {
            addbutton =
                $('<button>', {
                    id: 'addbutton',
                    interface: intf['kismet.datasource.probed.interface'],
                    intftype: intf['kismet.datasource.type_driver']['kismet.datasource.driver.type'],
                })
                .html('Enable Source')
                .button()
                .on('click', function() {
                    var jscmd = {
                        "definition": $(this).attr('interface') + ':type=' + $(this).attr('intftype')
                    };

                    state['defer_command_progress'] = true;

                    var postdata = "json=" + encodeURIComponent(JSON.stringify(jscmd));
                    $.post("/datasource/add_source.cmd", postdata, "json")
                    .always(function() {
                        state['defer_command_progress'] = false;
                    });

                });
        }

        set_row(idiv, 'addsource', $('<span>'), addbutton);

        idiv.accordion("refresh");
    }

    for (var source of state['kismet_sources']) {
        var sdiv = $('#' + source['kismet.datasource.uuid'], state['content']);

        if (sdiv.length == 0) {
            sdiv = $('<div>', {
                id: source['kismet.datasource.uuid'],
                class: 'accordion source',
                })
            .append(
                $('<h3>', {
                    id: 'header',
                })
                .append(
                    $('<span>', {
                        id: 'error',
                    })
                )
                .append(
                    $('<span>', {
                        id: 'paused',
                    })
                )
                .append(
                    $('<span>', {
                        class: 'k-ds-source',
                    })
                    .html(source['kismet.datasource.name'])
                )
                .append(
                    $('<span>', {
                        id: 'rrd',
                        class: 'k-ds-rrd',
                    })
                )
            ).append(
                $('<div>', {
                    id: 'content',
                    class: 'k-ds-content',
                })
            );

            var table = $('<table>', {
                class: 'k-ds-table'
                });

            $('#content', sdiv).append(table);

            sdiv.accordion({ collapsible: true, active: false });

            state['content'].append(sdiv);
        }

        if (typeof(source['kismet.datasource.packets_rrd']) !== 'undefined' &&
                source['kismet.datasource.packets_rrd'] != 0) {

            var simple_rrd =
                kismet.RecalcRrdData(
                    source['kismet.datasource.packets_rrd'],
                    source['kismet.datasource.packets_rrd']['kismet.common.rrd.last_time'],
                    kismet.RRD_SECOND,
                    source['kismet.datasource.packets_rrd']['kismet.common.rrd.minute_vec'], {
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
            $('#rrd', sdiv).sparkline(simple_rrd, {
                type: "bar",
                width: 100,
                height: 12,
                barColor: '#000000',
                nullColor: '#000000',
                zeroColor: '#000000'
                });
        }

        // Find the channel buttons
        var chanbuttons = $('#chanbuttons', sdiv);

        if (chanbuttons.length == 0) {
            // Make a new one of all possible channels
            chanbuttons = $('<div>', {
                id: 'chanbuttons',
                uuid: source['kismet.datasource.uuid']
            });

            chanbuttons.append(
              $('<button>', {
                id: "all",
                uuid: source['kismet.datasource.uuid']
              }).html("All")
              .button()
              .on('click', function(){
                state['defer_command_progress'] = true;

                var uuid = $(this).attr('uuid');
                var chans = [];
                $('button.chanbutton[uuid=' + uuid + ']', state['content']).each(function(i) {
                    chans.push($(this).attr('channel'));
                });
                var jscmd = {
                    "cmd": "hop",
                    "channels": chans,
                    "uuid": uuid
                };
                var postdata = "json=" + encodeURIComponent(JSON.stringify(jscmd));
                $.post("/datasource/by-uuid/"+uuid+"/set_channel.cmd", postdata, "json")
                .always(function() {
                        state['defer_command_progress'] = false;
                    });

                $('button.chanbutton[uuid=' + uuid + ']', state['content']).each(function(i){
                      $(this).removeClass('disable-chan-system');
                      $(this).removeClass('enable-chan-system');
                      $(this).removeClass('disable-chan-user');
                      $(this).addClass('enable-chan-user');
                    })
                })
              );

            for (var c of source['kismet.datasource.channels']) {
                chanbuttons.append(
                    $('<button>', {
                        id: c,
                        channel: c,
                        uuid: source['kismet.datasource.uuid'],
                        class: 'chanbutton'
                    }).html(c)
                    .button()
                    .on('click', function() {
                        var uuid = $(this).attr('uuid');
                        var tid = 'tid' + uuid;

                        // If we're in channel lock mode, we highlight a single channel
                        if ($('#lock[uuid=' + uuid + ']').hasClass('enable-chan-user')) {
                            // Only do something if we're not selected
                            if (!($(this).hasClass('enable-chan-user'))) {
                                // Remove from all lock channels
                                $('button.chanbutton[uuid=' + uuid + ']').each(function(i) {
                                        $(this).removeClass('enable-chan-user');
                                    });
                                $('button.chanbutton[uuid=' + uuid + ']').removeClass('enable-chan-system');
                                // Set this channel
                                $(this).addClass('enable-chan-user');

                            } else {
                                return;
                            }

                            state['defer_source_update'] = true;
                            state['defer_command_progress'] = true;

                            // Clear any existing timer
                            if (tid in state)
                               clearTimeout(state[tid]);

                            // Immediately post
                            var jscmd = {
                                "cmd": "lock",
                                "uuid": uuid,
                                "channel": $(this).attr('channel'),
                            };

                            var postdata = "json=" + encodeURIComponent(JSON.stringify(jscmd));
                            $.post("/datasource/by-uuid/"+uuid+"/set_channel.cmd", postdata, "json")
                            .always(function() {
                                state['defer_command_progress'] = false;
                            });

                            return;
                        } else {
                            if ($(this).hasClass('disable-chan-user')) {
                                $(this).removeClass('disable-chan-user');
                                $(this).addClass('enable-chan-user');
                            } else {
                                $(this).removeClass('enable-chan-user');
                                $(this).removeClass('enable-chan-system');
                                $(this).addClass('disable-chan-user');
                            }
                        }

                        // Either way, set the timer
                        if (tid in state)
                            clearTimeout(state[tid]);

                        state[tid] = setTimeout(function() {
                            state['defer_command_progress'] = true;
                            state['defer_source_update'] = true;

                            var chans = [];

                            $('button.chanbutton[uuid=' + uuid + ']', state['content']).each(function(i) {
                                // If we're hopping, collect user and system
                                if ($(this).hasClass('enable-chan-user') ||
                                        $(this).hasClass('enable-chan-system')) {
                                    state['refresh' + uuid] = true;
                                    chans.push($(this).attr('channel'));
                                }
                            });

                            var jscmd = {
                                "cmd": "hop",
                                "uuid": uuid,
                                "channels": chans
                            };

                            var postdata = "json=" + encodeURIComponent(JSON.stringify(jscmd));
                            $.post("/datasource/by-uuid/"+uuid+"/set_channel.cmd", postdata, "json")
                            .always(function() {
                                state['defer_command_progress'] = false;
                            });
                        }, 2000);
                    })
                );
            }
        }

        var pausediv = $('#pausediv', sdiv);
        if (pausediv.length == 0) {
            pausediv = $('<div>', {
                id: 'pausediv',
                uuid: source['kismet.datasource.uuid']
            });

            pausediv.append(
                $('<button>', {
                    id: "pausecmd",
                    uuid: source['kismet.datasource.uuid']
                }).html('Paused')
                .button()
                .on('click', function() {
                    state['defer_command_progress'] = true;
                    state['defer_source_update'] = true;

                    var uuid = $(this).attr('uuid');

                    $('#resumecmd[uuid=' + uuid + ']', state['content']).removeClass('enable-chan-user');
                    $(this).addClass('enable-chan-user');
                    $('#pausetext[uuid=' + uuid + ']', state['content']).show();

                    $.get('/datasource/by-uuid/' + uuid + '/pause_source.cmd')
                    .always(function() {
                            state['defer_command_progress'] = false;
                    });

                })
            );

            pausediv.append(
                $('<button>', {
                    id: "resumecmd",
                    uuid: source['kismet.datasource.uuid']
                }).html('Running')
                .button()
                .on('click', function() {
                    state['defer_command_progress'] = true;
                    state['defer_source_update'] = true;

                    var uuid = $(this).attr('uuid');

                    $('#pausecmd[uuid=' + uuid + ']', state['content']).removeClass('enable-chan-user');
                    $(this).addClass('enable-chan-user');
                    $('#pausetext[uuid=' + uuid + ']', state['content']).hide();

                    $.get('/datasource/by-uuid/' + uuid + '/resume_source.cmd')
                    .always(function() {
                            state['defer_command_progress'] = false;
                    });

                })
            );

            pausediv.append(
                $('<p>', {
                    id: 'pausetext',
                    uuid: source['kismet.datasource.uuid']
                })
                .html('Paused sources will not report packets.  Any packets seen by a paused source will be lost.')
            );

        }

        var quickopts = $('#quickopts', sdiv);
        if (quickopts.length == 0) {
          quickopts = $('<div>', {
              id: 'quickopts',
              uuid: source['kismet.datasource.uuid']
          });

          quickopts.append(
            $('<button>', {
              id: "lock",
              uuid: source['kismet.datasource.uuid']
            }).html("Lock")
            .button()
            .on('click', function(){
              state['defer_source_update'] = true;
              state['defer_command_progress'] = true;

              var uuid = $(this).attr('uuid');

              $('#hop[uuid=' + uuid + ']', state['content']).removeClass('enable-chan-user');
              $('#lock[uuid=' + uuid + ']', state['content']).addClass('enable-chan-user');

              var firstchanobj = $('button.chanbutton[uuid=' + uuid + ']', state['content']).first();

              var chan = firstchanobj.attr('channel');

              var jscmd = {
                  "cmd": "lock",
                  "channel": chan,
                  "uuid": uuid
              };
              var postdata = "json=" + encodeURIComponent(JSON.stringify(jscmd));
              $.post("/datasource/by-uuid/"+uuid+"/set_channel.cmd", postdata, "json")
              .always(function() {
                      state['defer_command_progress'] = false;
                      });

              $('button.chanbutton[uuid='+ uuid + ']', state['content']).each(function(i) {
                      $(this).removeClass('enable-chan-system');
                      $(this).removeClass('disable-chan-user');
              });

              // Disable all but the first available channel
              firstchanobj.removeClass('disabled-chan-user');
              firstchanobj.removeClass('enable-chan-system');
              firstchanobj.addClass('enable-chan-user')

              })
            );

          quickopts.append(
            $('<button>', {
              id: "hop",
              uuid: source['kismet.datasource.uuid']
            }).html("Hop")
            .button()
            .on('click', function(){
              state['defer_source_update'] = true;
              state['defer_command_progress'] = true;

              var uuid = $(this).attr('uuid');

              $('#hop[uuid=' + uuid + ']', state['content']).addClass('enable-chan-user');
              $('#lock[uuid=' + uuid + ']', state['content']).removeClass('enable-chan-user');

              var chans = [];
              $('button.chanbutton[uuid=' + uuid + ']', state['content']).each(function(i) {
                      chans.push($(this).attr('channel'));
                });

              var jscmd = {
                  "cmd": "hop",
                  "channels": chans,
                  "uuid": uuid
              };

              var postdata = "json=" + encodeURIComponent(JSON.stringify(jscmd));
              $.post("/datasource/by-uuid/"+uuid+"/set_channel.cmd", postdata, "json")
              .always(function() {
                state['defer_command_progress'] = false;
              });

              $('button.chanbutton[uuid='+ uuid + ']', state['content']).each(function(i) {
                  // Disable all but the first available channel
                  if ($(this).attr('channel') == 1) {
                      $(this).removeClass('disabled-chan-user');
                      $(this).removeClass('enable-chan-system');
                      $(this).addClass('enable-chan-user')
                  } else {
                      $(this).removeClass('enable-chan-system');
                      $(this).removeClass('disable-chan-user');
                  }
              });
              })
            );
          }

        var uuid = source['kismet.datasource.uuid'];
        var hop_chans = source['kismet.datasource.hop_channels'];
        var lock_chan = source['kismet.datasource.channel'];
        var hopping = source['kismet.datasource.hopping'];

        if (!defer) {
            if (source['kismet.datasource.hopping']) {
                $('#hop', quickopts).addClass('enable-chan-user');
                $('#lock', quickopts).removeClass('enable-chan-user');
            } else {
                $('#hop', quickopts).removeClass('enable-chan-user');
                $('#lock', quickopts).addClass('enable-chan-user');
            }

            $('button.chanbutton', chanbuttons).each(function(i) {
                var chan = $(this).attr('channel');

                // If locked, only highlight locked channel
                if (!hopping) {
                    if (chan === lock_chan) {
                        $(this).addClass('enable-chan-user');
                        $(this).removeClass('enable-chan-system');
                    } else {
                        $(this).removeClass('enable-chan-user');
                        $(this).removeClass('enable-chan-system');
                    }

                    return;
                }

                // Flag the channel if it's found, and not explicitly disabled
                if (hop_chans.indexOf(chan) != -1 && !($(this).hasClass('disable-chan-user'))) {
                    $(this)
                    .addClass('enable-chan-system');
                } else {
                    $(this)
                    .removeClass('enable-chan-system');
                }
            });

            if (source['kismet.datasource.paused']) {
                $('#pausecmd', pausediv).addClass('enable-chan-user');
                $('#resumecmd', pausediv).removeClass('enable-chan-user');
                $('#pausetext', pausediv).show();
            } else {
                $('#pausecmd', pausediv).removeClass('enable-chan-user');
                $('#resumecmd', pausediv).addClass('enable-chan-user');
                $('#pausetext', pausediv).hide();
            }

        }

        var s = source['kismet.datasource.interface'];

        if (source['kismet.datasource.interface'] !==
                source['kismet.datasource.capture_interface']) {
            s = s + "(" + source['kismet.datasource.capture_interface'] + ")";
        }

        if (source['kismet.datasource.error']) {
            $('#error', sdiv).html('<i class="k-ds-error fa fa-exclamation-circle"></i>');
            top_row(sdiv, 'error', '<i class="k-ds-error fa fa-exclamation-circle"></i><b>Error</b>',
                    source['kismet.datasource.error_reason']);
        } else {
            $('#error', sdiv).empty();
            $('tr#error', sdiv).remove();
        }

        if (source['kismet.datasource.paused']) {
            $('#paused', sdiv).html('<i class="k-ds-paused fa fa-pause-circle"></i>');
        } else {
            $('#paused', sdiv).empty();
        }

        set_row(sdiv, 'interface', '<b>Interface</b>', s);
        set_row(sdiv, 'uuid', '<b>UUID</b>', source['kismet.datasource.uuid']);
        set_row(sdiv, 'packets', '<b>Packets</b>', source['kismet.datasource.num_packets']);

        var rts = "";
        if (source['kismet.datasource.remote']) {
            rts = 'Remote sources are not re-opened by Kismet, but will be re-opened when the '
                'remote source reconnects.';
        } else if (source['kismet.datasource.retry']) {
            rts = 'Kismet will try to re-open this source if an error occurs';
            if (source['kismet.datasource.retry_attempts'] && 
                    source['kismet.datasource.running'] == 0) {
                rts = rts + ' (Tried ' + source['kismet.datasource.retry_attempts'] + ' times)';
            }
        } else {
            rts = 'Kismet will not re-open this source';
        }

        set_row(sdiv, 'retry', '<b>Retry on Error</b>', rts);

        if (source['kismet.datasource.running']) {
            set_row(sdiv, 'pausing', '<b>Active</b>', pausediv);
            if (source['kismet.datasource.type_driver']['kismet.datasource.driver.tuning_capable']) {
                set_row(sdiv, 'chanopts', '<b>Channel Options</b>', quickopts);
                set_row(sdiv, 'channels', '<b>Channels</b>', chanbuttons);
            } else {
                $('#trchanopts', sdiv).remove();
                $('#trchannels', sdiv).remove();
            }
        } else {
            $('#trpausing', sdiv).remove();
            $('#trchanopts', sdiv).remove();
            $('#trchannels', sdiv).remove();
        }

        sdiv.accordion("refresh");
    }
}

exports.DataSources2 = function() {
    var w = $(window).width() * 0.95;
    var h = $(window).height() * 0.75;
    var offy = 20;

    if ($(window).width() < 450 || $(window).height() < 450) {
        w = $(window).width() - 5;
        h = $(window).height() - 5;
        offy = 0;
    }

    var state = {};

    var content =
        $('<div>', {
            class: 'k-ds-contentdiv',
        })

    state['panel'] = $.jsPanel({
        id: 'datasources',
        headerTitle: '<i class="fa fa-cogs" /> Data Sources',
        headerControls: {
            iconfont: 'jsglyph',
            minimize: 'remove',
            smallify: 'remove',
        },
        content: content,

        resizable: {
            stop: function(event, ui) {
                $('div.accordion', ui.element).accordion("refresh");
            }
        },

        onmaximized: function() {
            $('div.accordion', this.content).accordion("refresh");
        },

        onnormalized: function() {
            $('div.accordion', this.content).accordion("refresh");
        },

        onclosed: function() {
            clearTimeout(state['datasource_list_tid']);
        }
    })
    .resize({
        width: w,
        height: h
    })
    .reposition({
        my: 'center-top',
        at: 'center-top',
        of: 'window',
        offsetY: offy,
    })
    .contentResize();

    state["content"] = content;
    state["kismet_sources"] = [];
    state["kismet_interfaces"] = [];

    datasource_source_refresh(state, function(data) {
            update_datasource2(data, state);
        });
    datasource_interface_refresh(state, function(data) {
            update_datasource2(data, state);
        });
}

/* Get the list of active sources */
function datasource_source_refresh(state, cb) {
    if ('datasource_get_tid' in state)
        clearTimeout(state['datasource_get_tid']);

    $.get("/datasource/all_sources.json")
    .done(function(data) {
        state['kismet_sources'] = data;
        cb(data);
        state['defer_source_update'] = false;
    })
    .always(function() {
        state['datasource_get_tid'] = setTimeout(function() {
            datasource_source_refresh(state, cb)
        }, 1000);
    });
}

/* Get the list of potential interfaces */
function datasource_interface_refresh(state, cb) {
    if ('datasource_interface_tid' in state)
        clearTimeout(state['datasource_interface_tid']);

    $.get("/datasource/list_interfaces.json")
    .done(function(data) {
        state['kismet_interfaces'] = data;
        cb(data);
        state['defer_interface_update'] = false;
    })
    .always(function() {
        state['datasource_interface_tid'] = setTimeout(function() {
            datasource_interface_refresh(state, cb)
        }, 3000);
    });
}

// We're done loading
exports.load_complete = 1;

return exports;

});
