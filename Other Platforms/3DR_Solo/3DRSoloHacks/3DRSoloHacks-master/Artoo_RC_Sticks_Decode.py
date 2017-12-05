# Code to print out RC packet data from 3DR Solo. 
# Please note that FIRST you must respond to a crafted handshake packet you send
# Current outbound heartbeat sends the string "1.0.51.0.2" 
# Code to simulate heartbeat will come soon. 
#
# This utilizes code from - /usr/bin/rc_cli.py on 3DRobotics Solo
# PoC Code mashup by Kevin Finisterre of 3DR Solo Hacks Facebook Group
# https://www.facebook.com/groups/363840837156620/
# 
import sys
import struct

"""
RC packet sent over network looks as follows. All fields are little-endian.

Start
Byte    Size    Description
0	8	Timestamp, usec since some epoch
8	2	Sequence number
10	2	Channel 1
12	2	Channel 2
14	2	Channel 3
16	2	Channel 4
18	2	Channel 5
20	2	Channel 6
22	2	Channel 7
24	2	Channel 8
26 (packet length)
"""

from scapy.all import *

LENGTH = 26

# Input is binary packet (string)
# Output is tuple (timestamp, sequence, channels[])
def unpack(s):
    s = s.lastlayer().load
    if len(s) != LENGTH:
        #print s.show()
        return "Wrong length ",  len(s)
    ts, seq = struct.unpack("<QH", s[:10])
    ch = []
    for i in range(10, 26, 2):
        ch.extend(struct.unpack("<H", s[i:i+2]))
    return (ts, seq, ch)

def rc_packet_callback(pkt):
        print unpack(pkt)

sniff(prn=rc_packet_callback, filter="port 5005", store=0)

