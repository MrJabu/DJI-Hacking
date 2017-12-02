# 3DRSoloFirmwareBinaries
Archive of 3DRobotics Solo Firmware binaries

```
Get Feeds.rb
https://github.com/MAVProxyUser/3DRSoloHacks/blob/master/Feeds.rb

$ ruby Feeds.rb sitescanproduction | grep solo
https://firmware-distribution.3dr.com/media/releases/2016/05/03/d7d1d000-04f2-42a3-ad42-e1c3d0d53abc/solo_3.0.0.tar.gz
https://firmware-distribution.3dr.com/media/releases/2016/03/08/solo_2.2.0.tar.gz
https://firmware-distribution.3dr.com/media/releases/2016/03/08/solo_2.2.0-1.tar.gz
$ ruby Feeds.rb prod | grep solo
https://firmware-distribution.3dr.com/media/releases/2016/09/27/cc2f55da-5358-4a53-9b9f-c9dd287d919a/solo_2.4.2.tar.gz
https://firmware-distribution.3dr.com/media/releases/2016/08/26/6bb41ec0-fe4e-4010-903b-5fd83eb5626a/solo_2.4.1-6.tar.gz
https://firmware-distribution.3dr.com/media/releases/2016/06/16/88162a24-2718-48a0-a1cf-695e829cb7f9/solo_2.4.0.tar.gz
https://firmware-distribution.3dr.com/media/releases/2016/04/21/b6f2b0fd-15e0-4cdc-a3d4-f4e6f6db9b1f/solo_2.3.0-1.tar.gz
https://firmware-distribution.3dr.com/media/releases/2016/03/23/9e5f4f49-845c-4e1a-a925-e16c82786b53/solo_2.1.1.tar.gz
https://firmware-distribution.3dr.com/media/releases/2016/03/05/solo_2.1.0.tar.gz
https://firmware-distribution.3dr.com/media/releases/2016/02/11/solo_2.0.0.tar.gz
https://firmware-distribution.3dr.com/media/releases/2015/11/19/solo_1.3.0.tar.gz
https://firmware-distribution.3dr.com/media/releases/2015/10/14/solo_1.2.0.tar.gz
https://firmware-distribution.3dr.com/media/releases/2015/09/03/solo_1.1.15.tar.gz
https://firmware-distribution.3dr.com/media/releases/2015/08/21/solo_1.1.12.tar.gz
https://firmware-distribution.3dr.com/media/releases/2015/06/26/solo_1.0.5.tar.gz
https://firmware-distribution.3dr.com/media/releases/2015/06/04/solo_1.0.0.tar.gz

$ for each in `ruby Feeds.rb sitescanproduction | grep solo`; do wget $each; done 
$ for each in `ruby Feeds.rb prod | grep solo`; do wget $each; done

Mount them all:

/tmp/mnt//solo_1.0.0.tar.gz_dir/3dr-solo-imx6solo_3dr_1080p.squashfs/firmware/ArduCopter-1.0.1.px4
/tmp/mnt//solo_1.0.5.tar.gz_dir/3dr-solo-imx6solo_3dr_1080p.squashfs/firmware/ArduCopter-1.0.9.px4
/tmp/mnt//solo_1.1.12.tar.gz_dir/3dr-solo-imx6solo_3dr_1080p.squashfs/firmware/ArduCopter-1.1.30.px4
/tmp/mnt//solo_1.1.15.tar.gz_dir/3dr-solo-imx6solo_3dr_1080p.squashfs/firmware/ArduCopter-1.1.38.px4
/tmp/mnt//solo_1.2.0.tar.gz_dir/3dr-solo-imx6solo_3dr_1080p.squashfs/firmware/ArduCopter-1.2.0.px4
/tmp/mnt//solo_1.3.0.tar.gz_dir/3dr-solo-imx6solo_3dr_1080p.squashfs/firmware/ArduCopter-1.2.19.px4
/tmp/mnt//solo_2.0.0.tar.gz_dir/3dr-solo-imx6solo_3dr_1080p.squashfs/firmware/ArduCopter-1.2.21.px4
/tmp/mnt//solo_2.1.0.tar.gz_dir/3dr-solo-imx6solo-3dr-1080p.squashfs/firmware/ArduCopter-1.2.23.px4
/tmp/mnt//solo_2.1.1.tar.gz_dir/3dr-solo-imx6solo-3dr-1080p.squashfs/firmware/ArduCopter-1.2.23.px4
/tmp/mnt//solo_2.2.0-1.tar.gz_dir/3dr-solo-imx6solo-3dr-1080p.squashfs/firmware/ArduCopter-1.2.23.px4
/tmp/mnt//solo_2.2.0.tar.gz_dir/3dr-solo-imx6solo-3dr-1080p.squashfs/firmware/ArduCopter-1.2.23.px4
/tmp/mnt//solo_2.3.0-1.tar.gz_dir/3dr-solo-imx6solo-3dr-1080p.squashfs/firmware/ArduCopter-1.2.23.px4
/tmp/mnt//solo_2.4.0.tar.gz_dir/3dr-solo-imx6solo-3dr-1080p.squashfs/firmware/ArduCopter-2.0.20.px4
/tmp/mnt//solo_2.4.1-6.tar.gz_dir/3dr-solo-imx6solo-3dr-1080p.squashfs/firmware/ArduCopter-1.3.1.px4
/tmp/mnt//solo_2.4.2.tar.gz_dir/3dr-solo-imx6solo-3dr-1080p.squashfs/firmware/ArduCopter-1.3.1.px4
/tmp/mnt//solo_3.0.0.tar.gz_dir/3dr-solo-imx6solo-3dr-1080p.squashfs/firmware/ArduCopter-1.2.24.px4


Collect them all:

MD5 (ArduCopter-1.0.1.px4) = 67ed1eba76156ee50b5a02be0eb6c867
MD5 (ArduCopter-1.0.9.px4) = 9f955ca2f947900a643d7e54f3a07bda
MD5 (ArduCopter-1.1.30.px4) = 4948be4f3a557a20b3a2c47168162fd0
MD5 (ArduCopter-1.1.38.px4) = 441b76d8c0bada65165b60193b6bd373
MD5 (ArduCopter-1.2.0.px4) = 0eda0b0bf4f13c0cf3689a06dcf632a0
MD5 (ArduCopter-1.2.19.px4) = b7743158907ccb18ed06535767f7e480
MD5 (ArduCopter-1.2.21.px4) = 0be6025036fb93942e75ba69db5cd149
MD5 (ArduCopter-1.2.23.px4) = 0ec544a20a033710ef41d03e8674b77c
MD5 (ArduCopter-1.2.24.px4) = 866f39082f134e76e95beed2e95ea2ae
MD5 (ArduCopter-1.3.1.px4) = 9cb58d4d1a580996329fc38afa6e5de8
MD5 (ArduCopter-2.0.20.px4) = c377b4a8003cadbc8844f4cdfa1740f9
```
