pyrit -u mysql://root:root@localhost/pyrit eval
pyrit -u mysql://root:root@localhost/pyrit -i dict.txt import_passwords
for each in `cat SSIDs.txt` ; do  pyrit -u mysql://root:root@localhost/pyrit -e $each create_essid; done
pyrit -u mysql://root:root@localhost/pyrit eval
pyrit -u mysql://root:root@localhost/pyrit batch
pyrit -u mysql://root:root@localhost/pyrit verify
pyrit -u mysql://root:root@localhost/pyrit -o SoloRainBowTable_Pyrit export_hashdb
