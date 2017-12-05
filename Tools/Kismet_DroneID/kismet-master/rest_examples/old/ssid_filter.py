#!/usr/bin/env python

import sys, KismetRest

# SSID Filtering
#
# Applies one or more regexes to the list of known SSIDs via the
# phy80211 regex API.
#
# This method will return ALL FIELDS in the matching devices, which
# may place a considerable load on the server and parser depending on
# how it is used.
#
# For a much more efficient version, look at the smart_ssid_filter.py
# example

if len(sys.argv) < 2:
    print "Expected server URI"
    sys.exit(1)

kr = KismetRest.KismetConnector(sys.argv[1])

kr.set_debug(True)

# Get summary of devices
devices = kr.device_filtered_dot11_summary(sys.argv[2:])

if len(devices) == 0:
    print "No devices found"
    sys.exit(0)

# Print the SSID for every device we can.  Stupid print; no comparison
# of the phy type, no handling empty ssid, etc.
for d in devices:
    if not d['kismet.device.base.phyname'] == "IEEE802.11":
        continue

    k = d['kismet.device.base.key']
    ssid = kr.device_field(k, "dot11.device/dot11.device.last_beaconed_ssid")

    if ssid == "":
        continue

    print "MAC", d['kismet.device.base.macaddr'],
    print "Type", d['kismet.device.base.type'],
    print "Channel",d['kismet.device.base.channel'],
    print "SSID", ssid


