#!/usr/bin/perl -w
# genrun.pl modified by ifedayo oladapo 17.07.2009 
use Term::Size;
use FileHandle;
use Text::Tabs;
use strict;
STDOUT->autoflush(1);
if ($#ARGV < 2){
print "\nfatal error 001 insufficient arguments";
usage();
}
sub usage{
print "\n Usage :";
print "\n genrun.pl passwordlist ssidlist destination-path\n";
exit;
}
my ($wordlist, $SSIDlist,$outputpath )= @ARGV;

if (! -e $wordlist){
print "\nfatal error 002 password list file $wordlist does not exist";
usage();
}
if (! -e $SSIDlist){
print "\nfatal error 003 SSID list file $SSIDlist does not exist";
usage();
}
if (! -e $outputpath){
print "\nfatal error 004 destination path $outputpath does not exist";
usage();
}
print "\npassword list : $wordlist\nSSID list : $SSIDlist\ndestination path : $outputpath\n"; 
system('clear');
open(FILE, $SSIDlist); 
my @file = <FILE>; 
close FILE; 
my ($line, @line) = ( undef, undef ); 
my ($oldfilesize, $filesize) = ( 0, 0 );
my ($columns, $rows) = Term::Size::chars *STDOUT{IO};
my $path = "$outputpath";
my ($file,$count,$skipcount,$skipcnt,$totalcnt,$gencnt,$gcnt,$remainincnt,$rcnt)=(undef,0,0,0,$#file,0, 0,0);
print "\nskip | generated | remaining\n";
$skipcnt = 0 x ( 4 - length($skipcount) ) . $skipcount ;
$rcnt =	0 x ( 4 - length($remainincnt) ) . $remainincnt ;
$gcnt = 0 x ( 4 - length($gencnt) ) . $gencnt ;
while ($line = shift(@file))
{
#chomp;
if ($line =~ /^#/) 
{ 
print "Found a header line - skipping.\n";
}
else
{
@line = split(/\n/, $line); 
$file = join("",$path,$line[0]);
my ( $newwordlist, $found) = ( join("",$wordlist,".reduced"), undef ); 
$count++;
if ($count >= $rows){
$count = 0;
($columns, $rows) = Term::Size::chars *STDOUT{IO};	
print "skip | generated | remaining\n";
}
if (-e $file){
$skipcount++;
$filesize = -s "$file";

if (($filesize < $oldfilesize) and ($skipcount > 1)){
print "$skipcnt | $gcnt | $rcnt $file exists but filesize suspicous attempting incrementation\a\n";#deleting\a\n";
print " Size: $filesize < $oldfilesize\n";
my ( $tailbuffer, @hexdbuffer, $listbuffer, $offset, $guess1, $guess2 ) = ( undef, undef, undef , '32', undef, undef);
$tailbuffer = qx(tail '-n 1' $file);
#	$tailbuffer = reverse $tailbuffer;	
$listbuffer = qx(ls -la $file);
$listbuffer =~ /([-rwx]{10})\s+(\d*+\s+\w+\s+\w+\s+)(\d*)/;
$offset = $3 - 16;
@hexdbuffer = qx(hd '-s' $offset '-n 16' $file);
#	print "\noffset : $offset bytes\n $listbuffer";
$hexdbuffer[0] =~ /(.{61})(.*)/;
print "\r analyzing hexdump $file at $offset bytes";
select( undef, undef, undef, .8 );	
$guess1 = $2;
#	$guess1 =~ m/(.*)/g;
$tailbuffer =~ /(.*)/g;
my @tb = split (/\W/,$tailbuffer);
my @tailguesses;
my ( $count, $length ) = ( 0, 0 );
print "\n best guess $guess1\n";
print "\r tailing $file for legitmate guesses\n"; 
select( undef, undef, undef, .8 );	

foreach (@tb){	
next if ( ($_ !~ /\w*/g) | (length ($_) <= 7));
$length = length($_);
push @tailguesses, $_;
print "\r $count $length $_";
$count++;
select( undef, undef, undef, .01 );	
}
$guess2 = $tailguesses[$#tailguesses];
print "\r last of $count guesses $guess2\n checking if guesses have matching strings :";#$tailbuffer\n";1 $1\n2 $2\n
if ($guess1 =~ m/$guess2/g){
print " looking good trying $guess2\n";	
#for (1..4){	print "\r . "; select( undef, undef, undef, .5 ); print "\r "; select( undef, undef, undef, .8 )}
print "\r scanning password list $file\n";
open(PWFILE, $wordlist); 
my @word = <PWFILE>;
my ( @newword );
close PWFILE;
foreach (@word) {
if ($_ =~ $guess2){
print " found , next word is";
$found = 1;	
}
if ($found){
#print $_;
push @newword, $_ ;
}
} 
shift @newword;
my $new = spaces_begone($newword[0]);
print " $new\n building new password list $newwordlist\n";
open( NEW_PWFILE, ">$newwordlist" ) or die "Could not open $newwordlist";
foreach (@newword){
$_ = spaces_begone($_);
printf NEW_PWFILE ( "%s", "$_\n");
}
close( NEW_PWFILE );
system ("./genpmk -f $newwordlist -d '$file' -s '$line[0]' ");
print " incrementation failed\n" if (!defined($found));
}else{
print " incrementation failed\n";	
}
exit;
if (!defined ($found)){
unlink($file);
if (! -e $file) {
print "File $file deleted successfully.\n";
system ("./genpmk -f $wordlist -d '$file' -s '$line[0]' ");
} else {
print "File $file was not deleted skipping.";
}
}
}

$remainincnt = $totalcnt - ($gencnt+$skipcount);
$rcnt =	0 x ( 4 - length($remainincnt) ) . $remainincnt ;
$skipcnt = 0 x ( 4 - length($skipcount) ) . $skipcount ;
print "$skipcnt | $gcnt | $rcnt $file exists skipping \a\n";
$oldfilesize = $filesize;
}else{
$gencnt++;
$remainincnt = $totalcnt - ($gencnt+$skipcount);	
$rcnt =	0 x ( 4 - length($remainincnt) ) . $remainincnt ;
$gcnt = 0 x ( 4 - length($gencnt) ) . $gencnt ;
print "$skipcnt | $gcnt | $rcnt $file does not exist computing pairwise master key PMK \n";
select( undef, undef, undef, .4 );
system ("./genpmk -f $wordlist -d '$file' -s '$line[0]' ");
my $command = "echo pairwise master key successfully generated for S S I D $line[0] | espeak";
system("$command");

}


}
} 

################################################## ###################################



#	METHOD Remove Spaces



################################################## ###################################



sub spaces_begone {



my $string = shift;


$_ = expand($string); # Convert Tabs to Spaces


s/^\s+//g; # Remove spaces at start of line


s/\s+$//g; # Remove spaces at end of line


return $_;



} # end of remove spaces

