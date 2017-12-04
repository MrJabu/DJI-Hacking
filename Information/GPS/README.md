found something about ce/fcc switching, this method is from dji china forum, i try to translate to english here

 

It is depend on dji go app

how it work:

dji go app will first get location from data network, if no data network, then it will get from sim card carrier/operator; if no data network and no sim card, then set to CE as default

so, you can fake it by a android with root

1) clean install, remove all cache from dji go app (not sure is it necessary)

2) disconnect from data network

3) use app to fake country operater code to US

http://androidadvices.com/fake-country-operator-carrier-download-paid-android-apps/

4) start dji go app

 

how to check result ?

the only version can check is 4.0.6

go to setting, keep click on "Flight Controller SN", then it will pop up a secret menu and show country code

 

actually there is a password to change code manually, and also device SN !

but he don't share the password because it is too danger to share to public (but I guess we can change SN by web socket command ?)
