#!/usr/bin/perl -w
open(FILE, 'SSID.txt'); 
@file = <FILE>; 
close FILE; 

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
    system ("./genpmk -f passwords2.txt -d '@line[0]' -s '@line[0]' ");
   }
 } 
