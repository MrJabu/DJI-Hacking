# Code from - https://github.com/PX4/Firmware/blob/master/Tools/px_uploader.py
# Solo Gimbal firmware extract... 
# Get the firmware from the squashFS images in the firmware updates
# wget https://firmware-distribution.3dr.com/media/releases/2015/09/13/solo_1.1.16.tar.gz
# squashfuse 3dr-solo-imx6solo_3dr_1080p.squashfs /tmp/b

import json
import zlib
import base64

# Read file /firmware/gimbal_firmware_1.1.0.ax
# read the file
f = open("gimbal_firmware_1.1.0.ax", "r")
desc = json.load(f)
f.close()

image = bytearray(zlib.decompress(base64.b64decode(desc['image'])))


# pad image to 4-byte length
while ((len(image) % 4) != 0):
    image.append('\xff')

print image 
