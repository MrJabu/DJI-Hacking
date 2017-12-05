#Known Good SoloLink SSID's
#
# SoloLink_33F7EF
# SoloLink_33F8D8
# SoloLink_33F891
# SoloLink_33F948

# This covers the first 4096 Solos. Let me know when you see one not confirming to SoloLink_33FXXX
# Colin Guinn wants 100,000 the first year... claims it will happen in 3 months. 
# 2:30 http://video.foxbusiness.com/v/4204114461001/worlds-first-smart-drone-takes-to-the-skies/?#sp=show-clips

for x in {0..15} # 0xf = 15
do 
	for y in {0..255}
	do 

		printf -v GeneratedSoloMAC "SoloLink_33F%01X%02X" $x $y 
		printf "$GeneratedSoloMAC\n"

        done
done

