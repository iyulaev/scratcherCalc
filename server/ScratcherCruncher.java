import java.lang.*;
import java.util.*;
import java.io.*;

public class ScratcherCruncher {
	private ArrayList <ScratcherThing> scratcherArray;
	
	private final int EMPTY_LINE_THRESHOLD = 1;
	private static final String SCRATCHER_DYNAMIC_TXT_FILE = "scratcher_dynamic_data_test.txt";
	private static final String SCRATCHER_STATIC_TXT_FILE = "scratcher_static_data_test.txt";
	private static final String SCRATCHER_DB_FILE = "scratcher_data.db";
	
	/** Default constructor - just initialize the ArrayList of scratchers.
	*/
	public ScratcherCruncher() {
		scratcherArray = new ArrayList <ScratcherThing> ();
	}
	
	/** Does a simple search of scratcherArray, finding the scratcher pointed to by
		scratcherNumber
		
		@param scratcherNumber The series # of the scratcher for which we return the
			corresponding ScratcherThing for.
			
		@return Corresponding ScratcherThing for scratcher having the series index scratcherNumber. 
			Otherwise, if not found return null.
	*/
	private ScratcherThing findScratcherByNumber(int scratcherNumber) {
		for(int index = 0; index < scratcherArray.size(); index++) {
			if(scratcherArray.get(index) != null && 
			scratcherArray.get(index).getScratcherNumber() == scratcherNumber) {
				return(scratcherArray.get(index));
			}
		}
		return(null);
	}
	
	/** This function loads in the static data from a scratcher static data text file. The formatting
	of the file expects there to be one blank line separating each scratcher. Then, the scratcher
	is defined by the line
	
	<Scratcher Name>	<Scratcher Series #>	<Scratcher Price>	<# tickets total>
	
	defining name, series #, price, and total number of tickets. Tab delimited. 
	Then we have zero or more lines like
	
	<prize amount>	<probability>
	
	also tab delimited. 
	*/
	private void LoadStaticData(String filename) {
		BufferedReader fileReader;
		String line;
		int line_number=1;
		
		//State variables for data loading "state machine"
		boolean new_scratcher=true;
		ScratcherThing newScratcher = null;
		
		//Try to read the file line by line
		try {
			fileReader = new BufferedReader(new FileReader(filename));
			
			//Go through the file line by line
			while((line=fileReader.readLine()) != null) {
				//Skip empty lines, although this tells us that we're to start a new scratcher
				if(line.length() < EMPTY_LINE_THRESHOLD) {
					new_scratcher=true;
				}
				//Not an empty line!
				else {
					String[] tokens = line.split("\t");
					//This statement executes if we are parsing a new scratcher
					if(new_scratcher) {
						//Catch invalid # of tokens on the line
						if(tokens.length != 5) {
							System.err.println("ERROR: LoadStaticData() got invalid # of tokens on line # " + Integer.toString(line_number) + "\n");
						}
						//Otherwise, parse the line and create a new scratcher object
						else {
							String scratcherName = tokens[0];
							int scratcherNumber = Integer.parseInt(tokens[1]);
							int s_price = Integer.parseInt(tokens[2]);
							int s_tickets_total = Integer.parseInt(tokens[3]);
							String scratcherURL = tokens[4];
							
							newScratcher = new ScratcherThing(scratcherName, 
								scratcherNumber, s_price, s_tickets_total, scratcherURL);
							scratcherArray.add(newScratcher);
							new_scratcher = false;
						}
					}
					//This conditional block executes if we are parsing a probability lines for a
					//scratcher that's already been added
					else {
						//Catch invalid # of tokens on the line
						if(tokens.length != 2) {
							System.err.println("ERROR: LoadStaticData() got invalid # of tokens on line # " + Integer.toString(line_number) + "\n");
						}
						//Otherwise, parse the line and add stuff to scratcher's internal data
						else {
							double price = Double.parseDouble(tokens[0]);
							double probability = Double.parseDouble(tokens[1]);
							
							newScratcher.addStaticOdds(price, probability);
						}
					}	
				}
			
				line_number++;
			}
			
			fileReader.close();
		}
		catch (Exception e) {
			System.err.println("Got Exception trying to run LoadStaticData()");
			System.err.println(e);
		}
	}
	
	/** This function loads in the dynamic data from a scratcher dynamic data text file. The formatting
	of the file expects there to be one blank line separating each scratcher. Then, the scratcher
	is defined by the line
	
	<Scratcher Series #>
	
	defining the series # for the scratcher. Tab delimited. 
	Then we have zero or more lines like
	
	<prize amount>	<prizes claimed>	<prizes available>
	
	also tab delimited. 
	*/
	private void LoadDynamicData(String filename) {
		BufferedReader fileReader;
		String line;
		int line_number=1;
		
		//State variables for data loading "state machine"
		boolean new_scratcher=true;
		ScratcherThing newScratcher = null;
		
		//Try to read the file line by line
		try {
			fileReader = new BufferedReader(new FileReader(filename));
			
			//Go through the file line by line
			while((line=fileReader.readLine()) != null) {
				//Skip empty lines, although this tells us that we're to start a new scratcher
				if(line.length() < EMPTY_LINE_THRESHOLD) {
					new_scratcher=true;
					
					//Load the current newScratcher into the array (since we're done with it)
					//if it's not null of course
					if(newScratcher != null) {
						newScratcher = null;
					}
				}
				//Not an empty line!
				else {
					String[] tokens = line.split("\t");
					
					//System.out.println("DEBUG: Parsing line " + Integer.toString(line_number) + " as a " + (new_scratcher?"new scratcher":"dynamic data") + " line.");
					
					//This statement executes if we are parsing a new scratcher
					if(new_scratcher) {
						//Catch invalid # of tokens on the line
						if(tokens.length != 1) {
							System.err.println("ERROR: LoadDynamicData() got invalid # of tokens on line # " + Integer.toString(line_number) + ", expected 1");
							System.err.println("Got " + Integer.toString(tokens.length) + " tokens.");
						}
						//Otherwise, parse the line and create a new scratcher object
						else {
							newScratcher = findScratcherByNumber(Integer.parseInt(tokens[0]));
							
							if(newScratcher == null) {
								System.err.println("Got invalid series number \"" + tokens[0] +"\", or don't have static data loaded for it.");
							}
						}
						
						new_scratcher = false;
					}
					//This conditional block executes if we are parsing a probability lines for a
					//scratcher that's already been added
					else {
						//Catch invalid # of tokens on the line
						if(tokens.length != 3) {
							System.err.println("ERROR: LoadDynamicData() got invalid # of tokens on line # " + Integer.toString(line_number) + ", expected 3");
							System.err.println("Got " + Integer.toString(tokens.length) + " tokens.");
						}
						//Otherwise, parse the line and add stuff to scratcher's internal data
						else {
							int price = Integer.parseInt(tokens[0]);
							int claimed = Integer.parseInt(tokens[1]);
							int available = Integer.parseInt(tokens[2]);
							
							if(newScratcher != null) newScratcher.addDynamicOdds(price, claimed, available);
						}
					}
				}
			
				line_number++;
			}
			
			fileReader.close();
		}
		catch (Exception e) {
			System.err.println("Got Exception trying to run LoadDynamicData()");
			System.err.println(e);
		}
	}
	
	/** This method fills in the overall grades for the scratchers */
	private void fillInOverallGrades() {
		//First, add all of the expectations into an ArrayList
		ArrayList <Double> expectationsList = new ArrayList <Double> ();
		
		for(int index = 0; index < scratcherArray.size(); index++) {
			expectationsList.add(new Double(scratcherArray.get(index).expectationValue()));
		}
		
		//Then sort it
		Collections.sort(expectationsList);
		
		//Now, identify the top and bottom quartile cut-offs
		double top_quartile = expectationsList.get((int) (expectationsList.size() * 0.75)).doubleValue();
		double bot_quartile = expectationsList.get((int) (expectationsList.size() * 0.25)).doubleValue();
		
		//Now, fill in the grades for each scratcher
		for(int index = 0; index < scratcherArray.size(); index++) {
			ScratcherThing currScratcher = scratcherArray.get(index);
			double expected_value = currScratcher.expectationValue();
			
			if(expected_value >= top_quartile) currScratcher.s_overall_grade = 3;
			else if(expected_value < bot_quartile) currScratcher.s_overall_grade = 1;
			else currScratcher.s_overall_grade = 2;
		}
		
	}
	
	/** This method fills in the jackpot grades for the scratchers */
	private void fillInJackpotGrades() {
		//First, add all of the expectations into an ArrayList
		ArrayList <Double> jackpotList = new ArrayList <Double> ();
		
		for(int index = 0; index < scratcherArray.size(); index++) {
			jackpotList.add(new Double(scratcherArray.get(index).jackpotOdds()));
		}
		
		//Then sort it
		Collections.sort(jackpotList);
		
		//Now, identify the top and bottom quartile cut-offs
		double top_quartile = jackpotList.get((int) (jackpotList.size() * 0.66)).doubleValue();
		double bot_quartile = jackpotList.get((int) (jackpotList.size() * 0.33)).doubleValue();
		
		//Now, fill in the grades for each scratcher
		for(int index = 0; index < scratcherArray.size(); index++) {
			ScratcherThing currScratcher = scratcherArray.get(index);
			double jackpot_odds = currScratcher.jackpotOdds();
			
			if(jackpot_odds >= top_quartile) currScratcher.s_jackpot_grade = 3;
			else if(jackpot_odds < bot_quartile) currScratcher.s_jackpot_grade = 1;
			else currScratcher.s_jackpot_grade = 2;
		}
		
	}
	
	/** Dumps debugging info for this ScratcherCruncher. Basically, does a toString() call
		on every ScratcherThing in our scratcherArray.
	*/
	private void dumpDebuggingInfo () {
		for(int index = 0; index < scratcherArray.size(); index++) {
			System.out.print(scratcherArray.get(index).toString());
			System.out.print("\n");
		}
	}
	
	/** Writes out the database to a new database file defined by filename.
	
		@param filename The file name for the Scratchers DB file, to output to. 
	*/
	public void writeDBFile(String filename) {
		try {
			//Create a new BufferedWriter; note that we set the append parameter for the FileWriter
			//to false, indicating that we will overwrite the existing DB
			BufferedWriter outputStream = new BufferedWriter(new FileWriter(filename, false));
			
			//Write out a DB line for each scratcher in our array.
			//Note that we prepend (index+1) as the RowID, and append a new line at the end
			for(int index = 0; index < scratcherArray.size(); index++) {
				String outputLine = new String(Integer.toString(index+1) + "\t" + scratcherArray.get(index).toDBLineString() + "\n");
				outputStream.write(outputLine);
			}
			
			outputStream.close();
		} catch (Exception e) {
			System.err.println("ERROR: writeDBFile encountered a File IO Error");
			System.err.println(e);
		}
	}
	
	public static void main ( String [] args ) {
		ScratcherCruncher engine = new ScratcherCruncher();
		
		engine.LoadStaticData(SCRATCHER_STATIC_TXT_FILE);
		engine.LoadDynamicData(SCRATCHER_DYNAMIC_TXT_FILE);
		
		engine.fillInOverallGrades();
		engine.fillInJackpotGrades();
		
		engine.dumpDebuggingInfo();
		engine.writeDBFile(SCRATCHER_DB_FILE);
	}
}
