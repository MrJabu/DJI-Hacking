# firm_cache
firmware cache from DJI Assistant.app 
as extracted from dji_system.bin files, and collected by end users. 
Accompanying .bin files can be located here: https://github.com/MAVProxyUser/dji_system.bin

<img src=https://media.giphy.com/media/wNR8ZhO4fObRu/giphy.gif>


### List of the Modules and What they Effect

Individual .fw.sig files can be thought of as individual sections or modules within the entire firmware.   
The naming convention is as follows: 
`<model>_<module>_<version>_<date>.fw.sig`  

#### How to use the list

The 1st value in the file name is the Model.      
The 2nd value is what the module effects.      
The following files are all related to "Camera Upgrade" for the respective model
because the second number value 0100 is the Module #.    

- wm220_0100_v02.00.55.69_20161215.pro.fw.sig
- wm330_0100_v01.19.52.66_20160623.fw.sig
- wm331_0100_v01.04.15.46_20170317.pro.fw.sig
- etc.


#### Model #s

|Number (first value in file name) =  Model |
|:------------------------------------------|
wm100    =  Spark
wm220    =  Mavic
wm220_gl =  Goggles 
GL200A   =  GL200A # Mavic Controller
wm330    =  P4
wm331    =  P4P
wm620    =  Inspire2

| Module # -  Models that have a file containg this Module  -  Module Function|
|:----------------------------|
0100 - P4P, P4, i2, Mavic Camera Upgrade
0101 - P4, Mavic Camera Loader Upgrade
0104 - P4P Lens_Controller Upgrade
0106 - CAMFPGA (XLNX), XiLinx CAM FPGA? 
0305 - P4, i2 FlyCtrl_Loader, Spark, Mavic FC Loader Upgrade
0306 - P4P, P4, i2 FlyCtrl, Spark, Mavic FC APP Upgrade
0400 - P4P, P4, Spark, Mavic Gimbal Upgrade
0401 - P4P, P4 Gimbal 5223#1, i2 Gimbal_ESC Upgrade
0402 - P4P, P4 Gimbal 5223 #2, i2 SSD_Controller Upgrade
0404 - FPV_Gimbal Upgrade
0500 - i2 CenterBoard Upgrade
0501 - i2 Gear_Controller Upgrade
0600 - GLB200A MCU_051_gnd Upgrade (not encrypted)
0601 - Goggles MCU_031_gls Upgrade
0603 - Goggles MCU_051_gls Upgrade
0801 - Android recovery ROM?
0802 - Modvidius ma2155 VPU firmware, "DJI_IMX377" (CMOS image sensor) firmware, Veri Silicon Hantaro Video IP encoder/decoder ?
0803 - 
0804 - "System Initialized" ?
0805 - upgrade.zip (calibration for VPS?)
0900 - P4 OFDM, P4P, i2 LightBridge Upgrade
0905 - NFZ Database (nfz.db and bfz.sig)
0907 - Mavic modem/arm/dsp/gnd/uav "upgrade file" (unencrypted)
1100 - i2 Battery_0, P4, Spark, Mavic Battery Upgrade
1101 - i2 Battery_1 Upgrade
1200 - P4, i2, Spark, Mavic ESC0 Upgrade
1201 - P4, i2, spark, Mavic ESC1 Upgrade
1202 - P4, i2, Spark, Mavic ESC2 Upgrade
1203 - P4, i2, Spark, Mavic ESC3 Upgrade
1301 - OTA.zip?
1407 - GLB200A modem/arm/dsp/gnd/uav "upgrade file" (unencrypted)
2801 - Mavic modem/arm/dsp/gnd/uav "upgrade file" (unencrypted)
2803 -  
2807 - Mavic modem/arm/dsp/gnd/uav "upgrade file" (unencrypted)


### #DeejayeyeHackingClub information repos aka "The OG's" (Original Gangsters)

http://dji.retroroms.info/ - "Wiki"

https://github.com/fvantienen/dji_rev - This repository contains tools for reverse engineering DJI product firmware images.

https://github.com/Bin4ry/deejayeye-modder - APK "tweaks" for settings & "mods" for additional / altered functionality

https://github.com/hdnes/pyduml - Assistant-less firmware pushes and DUMLHacks referred to as DUMBHerring when used with "fireworks.tar" from RedHerring. DJI silently changes Assistant? great... we will just stop using it.

https://github.com/MAVProxyUser/P0VsRedHerring - RedHerring, aka "July 4th Independence Day exploit", "FTPD directory transversal 0day", etc. (Requires Assistant). We all needed a public root exploit... why not burn some 0day?

https://github.com/MAVProxyUser/dji_system.bin - Current Archive of dji_system.bin files that compose firmware updates referenced by MD5 sum. These can be used to upgrade and downgrade, and root your I2, P4, Mavic, Spark, Goggles, and Mavic RC to your hearts content. (Use with pyduml or DUMLDore)

https://github.com/MAVProxyUser/firm_cache - Extracted contents of dji_system.bin, in the future will be used to mix and match pieces of firmware for custom upgrade files. This repo was previously private... it is now open.

https://github.com/MAVProxyUser/DUMLrub - Ruby port of PyDUML, and firmware cherry picking tool. Allows rolling of custom firmware images.

https://github.com/jezzab/DUMLdore - Even windows users need some love, so DUMLDore was created to help archive, and flash dji_system.bin files on windows platforms.

https://github.com/MAVProxyUser/DJI_ftpd_aes_unscramble - DJI has modified the GPL Busybox ftpd on Mavic, Spark, & Inspire 2 to include AES scrambling of downloaded files... this tool will reverse the scrambling

https://github.com/darksimpson/jdjitools - Java DJI Tools, a collection of various tools/snippets tied in one CLI shell-like application
