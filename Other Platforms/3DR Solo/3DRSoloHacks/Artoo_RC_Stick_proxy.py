# Code to request the RC stream from Solo
# Subsequently print out the RC packet data from 3DR Solo. 
#
# Rather than simulate a complicated version check / handshake we simply 
# create a /var/run/solo.ip file with the IP of our current machine (or where we want the stream to go)
#
# This utilizes code from - /usr/bin/rc_cli.py on 3DRobotics Solo
# PoC Code mashup by Kevin Finisterre of 3DR Solo Hacks Facebook Group
# https://www.facebook.com/groups/363840837156620/
# 
# Example: python Artoo_RC_Stick_proxy.py 10.1.1.121
#

import sys
import struct
import subprocess
import socket
import paramiko
from scapy.all import *


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

LENGTH = 26

# Input is binary packet (string)
# Output is tuple (timestamp, sequence, channels[])
def unpack(s):
#    s = s.lastlayer().load
    if len(s) != LENGTH:
        #print s.show()
        return "Wrong length ",  len(s)
    ts, seq = struct.unpack("<QH", s[:10])
    ch = []
    for i in range(10, 26, 2):
        ch.extend(struct.unpack("<H", s[i:i+2]))
    return (ts, seq, ch)

startstream = "echo "
ip = ""
try:
    print "Using IP from commandline"
    startstream += sys.argv[1]
    ip = sys.argv[1]
except IndexError as e:
    print "Using socket.gethostbyname() to determine IP - *warning* this may not be accurate"
    print "Use the first commandline arguemnt to specify an IP if you desire" 
    startstream += socket.gethostbyname(socket.gethostname()) # Be Careful if you have multiple interfaces
    ip = socket.gethostbyname(socket.gethostname())
startstream +=  " > /var/run/solo.ip"
startstream +=  ";killall stm32"
print startstream

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect("10.1.1.1", username="root", password="TjSDBkAu")
ssh_stdin, ssh_stdout, ssh_stderr = ssh.exec_command(startstream)
print "RC stream should be initiated"
sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.bind((ip, 5005))
 
while True:
     data, addr = sock.recvfrom(1024) # buffer size is 1024 bytes
     print unpack(data)

