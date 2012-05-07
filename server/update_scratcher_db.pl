#!/usb/local/bin/perl

#Written I. Yulaev 2011-10-05 (ivan@yulaev.com)

#This script grabs the latest CA scratcher prize availability data (presumably 
#from http://www.calottery.com/Games/Scratchers/TopPrizes/) and then parses
#it. It creates an ASCII file that then gets read by CalcScratcherDB (a Java
#program), in order to update the SmartScratcher database.

#This script is part of the server side scratcher code for the SmartScratcher project

#Like "defines"
my($SKIP_DL_DATA) = 1; #set to 1 in order to NOT download data, and use a local
	#ca_scratcher_data.html file
my($SKIP_CALLING_JAVA) = 1; #set to 1 to avoid calling CalcScratcherDB
my($DEBUG_MODE) = 1;

#URL for retreiving the HTML file
my($CA_SCRATCHER_DATA_URL) = "http://www.calottery.com/Games/Scratchers/TopPrizes/";
#somehow this denotes the data table in the HTML file
my($DATA_TABLE_SIGNATURE) = "style=\"LINE-HEIGHT: 1em\""; 

#download the latest CA scratcher data
if($SKIP_DL_DATA == 0) {
	`wget -O ca_scratcher_data.html CA_SCRATCHER_DATA_URL`;
	
	if(-s "ca_scratcher_data.html" < 100 || ~(-e "ca_scratcher_data.html")) {
		print "ERROR: Unable to download CA Scratcher Data! Terminating...\n";
		exit;
	}
}

if($SKIP_CALLING_JAVA = 0) {
	`rm scratcher_dynamic_data.txt`;
}

#open the HTML file
open(INPUT, "<ca_scratcher_data.html") or die ("ERROR: Couldn't open the ca_scratcher_data.html file!\n");
open(OUTPUT, ">scratcher_dynamic_data.txt") or die ("ERROR: Couldn't open the scratcher_dynamic_data.txt output file!\n");

#go through the HTML file line by line; we're only interested in the very long line
#containing the table with all of the data in it
while(<INPUT>) {
	my($line) = $_;
	#Found the DATA_TABLE_SIGNATURE on this line? Parse it!
	if($line =~ m/$DATA_TABLE_SIGNATURE/) {
		#split the table into rows
		my(@trows) = split /<\/tr>/, $line;
		
		#This variable keeps track of whether the last row was 'even' or 'odd'
		#a change tells us that we have a new scratcher!
		my($prev_row_type) = "TableRowZero";
		
		foreach $row (@trows) {
			#these variables get filled in from the information int eh row
			my($scratcher_name); #name of the scratcher
			my($scratcher_num) = -1; #scratcher series #
			my($scratcher_price) = 0; #price of this scratcher
			my($scratcher_prize_amount); #prize amount
			my($scratcher_prize_avail); #number of prizes available
			my($scratch_prize_claimed); #number of prizes claimed
			my($this_row_type) = "TableRowZero"; #row type (<tr class=""...>)
			
			#identify the row type; note that if no row type is declared then
			#we ignore this row
			if($row =~ m/<tr class=\"(\S+)\"/) {
				$this_row_type = $1;
			}
			
			if($this_row_type ne "TableRowZero") {
				#split the row into columns
				my(@tcols) = split /\/td>/, $row;
				my(@coldata) = (); #This will be filled in with column data, a 
					#"processed" version of what's in the columns
				my($coldata_index) = 0;
				
				#Process the column data, extracting only what we need
				foreach $col (@tcols) {
					my($curr_coldata) = $col;
					
					#Extract what's after <td...> and before the next HTML tag
					if($curr_coldata =~ m/<td([^>]*)>([^<]+)</) {
						$curr_coldata = $2;
						$curr_coldata =~ s/[\$, ]//g; #remove $, ',', and whitespace
						$curr_coldata =~ s/&nbsp;//g; #remove "&nbsp;"
					} else {
						$curr_coldata = "ERROR: Couldn't extract td data.";
					}
					
					#put $curr_coldata into $coldata array
					$coldata[$coldata_index] = $curr_coldata;
					$coldata_index++;
				}
				
				#a new row type means it's time to parse a new scratcher!
				if($this_row_type ne $prev_row_type) {
					if($DEBUG_MODE==1) {print "\nNew scratcher row detected!\n";}
					
					#parse all of the entries in the columns of this (new scratcher) row
					$scratcher_price = $coldata[0];
					$scratcher_num = $coldata[1];
					$scratcher_name = $coldata[2];
					$scratcher_prize_amount = $coldata[3];
					$scratcher_prize_avail = $coldata[4];
					$scratch_prize_claimed = $coldata[5];
					
					if($DEBUG_MODE==1) {print "Scratcher $scratcher_name (#$scratcher_num)\tprice=$scratcher_price\n";}
					if($DEBUG_MODE==1) {print "Price amt=$scratcher_prize_amount\tavail=$scratcher_prize_avail\tclaimed=$scratch_prize_claimed\n";}
					
					print OUTPUT ("\n$scratcher_num\n");
					print OUTPUT ("$scratcher_prize_amount\t$scratcher_prize_avail\t$scratch_prize_claimed\n");
				}
				else {
					#parse all of the entries in this (not a new scratcher!) row
					$scratcher_prize_amount = $coldata[0];
					$scratcher_prize_avail = $coldata[1];
					$scratch_prize_claimed = $coldata[2];
					if($DEBUG_MODE==1) {print "Price amt=$scratcher_prize_amount\tavail=$scratcher_prize_avail\tclaimed=$scratch_prize_claimed\n";}
					
					#write result to ptuput
					print OUTPUT ("$scratcher_prize_amount\t$scratcher_prize_avail\t$scratch_prize_claimed\n");
				}
			}
			#Update row type based on the one we just processed
			if($this_row_type ne $prev_row_type) {
				$prev_row_type = $this_row_type;
			}
		}
	}
}

#Call Java code to crunch the data
if($SKIP_CALLING_JAVA = 0) {
	`rm scratcher_data.db`;
	`java ScratcherCruncher`;
}

#remove downloaded CA scratcher data HTML file
if($SKIP_DL_DATA == 0) {
	`rm ca_scratcher_data.html`;
}

print("Finished creating image browsing HTML files.\n");
close(INPUT);
close(OUTPUT);

