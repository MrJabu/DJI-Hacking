# For Linux: apt-get install sshpass
# For OSX: brew install https://raw.githubusercontent.com/kadwanev/bigboybrew/master/Library/Formula/sshpass.rb
# For Windows: Go piss off! =] 

ARTOO="10.1.1.1"
REPO=http://downloads.yoctoproject.org/releases/yocto/yocto-1.5.1/rpm/cortexa9hf_vfp_neon/
RPMLIST="binutils-2.23.2-r4.cortexa9hf_vfp_neon binutils-dev-2.23.2-r4.cortexa9hf_vfp_neon cpp-4.8.1-r0.cortexa9hf_vfp_neon cpp-symlinks-4.8.1-r0.cortexa9hf_vfp_neon eglibc-extra-nss-2.18-r0.cortexa9hf_vfp_neon g++-4.8.1-r0.cortexa9hf_vfp_neon gcc-4.8.1-r0.cortexa9hf_vfp_neon gcc-symlinks-4.8.1-r0.cortexa9hf_vfp_neon libc6-dev-2.18-r0.cortexa9hf_vfp_neon libcidn1-2.18-r0.cortexa9hf_vfp_neon libgcc-s-dev-4.8.1-r0.cortexa9hf_vfp_neon libgmp-dev-5.1.1-r0.cortexa9hf_vfp_neon libgmp10-5.1.1-r0.cortexa9hf_vfp_neon libgmpxx4-5.1.1-r0.cortexa9hf_vfp_neon libmpc-dev-1.0.1-r0.cortexa9hf_vfp_neon libmpc-staticdev-1.0.1-r0.cortexa9hf_vfp_neon libmpc3-1.0.1-r0.cortexa9hf_vfp_neon libmpfr-dev-3.1.2-r0.cortexa9hf_vfp_neon libmpfr-staticdev-3.1.2-r0.cortexa9hf_vfp_neon libmpfr4-3.1.2-r0.cortexa9hf_vfp_neon libthread-db1-2.18-r0.cortexa9hf_vfp_neon m4-1.4.16-r4.cortexa9hf_vfp_neon make-3.82-r3.cortexa9hf_vfp_neon linux-libc-headers-dev-3.10-r0.cortexa9hf_vfp_neon"
for each in $RPMLIST
do 
SCPLIST=$SCPLIST" "$each".rpm"
wget $REPO$each.rpm
done

echo "Transfering RPMS's to Artoo TX"
sshpass -p TjSDBkAu scp $SCPLIST root@$ARTOO:/tmp/

echo "Happy compile time!"
sshpass -p TjSDBkAu ssh root@$ARTOO "cd /tmp; rpm -ivh --nodeps $SCPLIST"
sshpass -p TjSDBkAu ssh root@$ARTOO


