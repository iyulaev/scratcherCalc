import java.lang.*;
import java.util.*;

/** This class defines a single Scratcher. It has data storage for static and dynamic data
	describing the scratcher and has built-in functions to calculate, f. ex., expectation, jackpot odds, and stuff like that. 
*/

public class ScratcherThing {
	private String scratcherName;
	private int scratcherNumber;
	private String scratcherURL;
	
	private double s_price;
	private int s_tickets_total;
	
	private ArrayList <ArrayList> s_static_odds; //2-tuple: (price, probability)
	private ArrayList <ArrayList> s_dynamic_odds; //3-tuple: (price, claimed, available)
	
	//These data elements aren't ever accessed by ScratcherThing functions; they're just 
	//used to store grade data (filled in by ScratcherCruncher) until it gets read out later
	public int s_overall_grade;
	public int s_jackpot_grade;
	
	private int s_hasWarning;
	private String warningText;
	
	//Tickets available is a method, not an object

	/** Basic Constructor. We give it the name of the scratcher, the series number, the scratcher
		price, and the total # of tickets. Note that this means total tickets, static odds, and 
		dynamic odds will be loaded later.
		
		@param n_scratcherName The name of this scratcher.
		@param n_scratcherNumber The seris number for this scratcher.
		@param n_s_price The cost of this scratcher.
		@param n_s_tickets_total The total number of tickets for this scratcher.
		@param n_scratcherURL The CA Lottery URL for this scratcher
	*/		
	public ScratcherThing(String n_scratcherName, int n_scratcherNumber, double n_s_price, 
			int n_s_tickets_total, String n_scratcherURL) {
		scratcherName = new String(n_scratcherName);
		scratcherNumber = n_scratcherNumber;
		scratcherURL = new String(n_scratcherURL);
		s_price = n_s_price;
		s_tickets_total = n_s_tickets_total;
		
		s_static_odds = new ArrayList<ArrayList>();
		s_dynamic_odds = new ArrayList<ArrayList>();
		
		s_overall_grade = -1;
		s_jackpot_grade = -1;
		
		//By default, scratchers are initialized without a warning
		s_hasWarning = 0;
		warningText = new String("No warning for this scratcher.");
	}
	
	public int getScratcherNumber() {
		return(scratcherNumber);
	}
	
	public String getScratcherName() {
		return(new String(scratcherName));
	}
	
	public double getScratcherPrice() {
		return(s_price);
	} 
	
	public int getScratcherTicketsTotal() {
		return(s_tickets_total);
	}
	
	/** This function sets the warning for this scratcher. Also hasWarning gets set to 1.
	
		@param warnString the new warning string
	*/
	public void setWarning(String warnString) {
		warningText = new String(warnString);
		s_hasWarning = 1;
	}
	
	/** Adds a static odds 2-tuple to this ScratcherThing's static dataset 
		@param price The return value of this prize.
		@param probability The probability of winning this prize.
	*/
	public void addStaticOdds(double price, double probability) {
		ArrayList <Double> sOddsTuple = new ArrayList<Double>();
		
		sOddsTuple.add(new Double(price));
		sOddsTuple.add(new Double(probability));
		
		s_static_odds.add(sOddsTuple);
	}
	
	/** Adds a dynamic odds 3-tuple to this ScratcherThing's static dataset 
		@param price The return value of this prize.
		@param claimed The number of this prize that have already been claimed.
		@param available The number of this prize that are still available.
	*/
	public void addDynamicOdds(int price, int claimed, int available) {
		ArrayList <Integer> dOddsTuple = new ArrayList<Integer>();
		
		dOddsTuple.add(new Integer(price));
		dOddsTuple.add(new Integer(claimed));
		dOddsTuple.add(new Integer(available));
		
		s_dynamic_odds.add(dOddsTuple);
	}
	
	/** This function estimates the number of tickets available based on the formula
	
		tickets available = (lowest dynamic price available / lowest dynamic price total) *
			number of tickets total
			
		It's kind of a crude measure but should get us pretty close. 
		
		@return Estimate of the number of tickets available for this scratcher.*/
	private int numberTicketsAvailable() {
		//Find the lowest price dynamic odds entry
		int lowest_index = -1;
		int lowest_price = 9999;
		
		for(int count = 0; count < s_dynamic_odds.size(); count++) {
			ArrayList <Integer> dOddsTuple = s_dynamic_odds.get(count);
			//Compare price of the count-th entry in s_dynamic_odds to lowest_price; if less,
			//then the count-th entry now has the lowest price!
			if(dOddsTuple.get(0).intValue() < lowest_price) {
				lowest_index = count;
				lowest_price = dOddsTuple.get(0).intValue();
			}
		}
		
		ArrayList <Integer> dOddsTuple = s_dynamic_odds.get(lowest_index);
		
		//Calculate the number of tickets available based on the formula defined in the JavaDocs
		int tix_available = (int) ( (dOddsTuple.get(2).doubleValue()/(dOddsTuple.get(1).doubleValue() + dOddsTuple.get(2).doubleValue())) * ((double)s_tickets_total) );
		
		//System.out.println("numberTicketsAvailable() decided that for (tickets total, claimed, available) = (" + s_tickets_total + ", " + dOddsTuple.get(1) + ", " +  dOddsTuple.get(2) + ") the number available is: " + tix_available);
		
		return(tix_available);
	}
	
	/** Method calculates the odds of getting a Jackpot
	
		@return This Scratcher's jackpot odds.
	*/
	public double jackpotOdds() {
		//Find the highest price dynamic odds entry
		int highest_index = -1;
		int highest_price = -1;
		
		for(int count = 0; count < s_dynamic_odds.size(); count++) {
			ArrayList <Integer> dOddsTuple = s_dynamic_odds.get(count);
			//Compare price of the count-th entry in s_dynamic_odds to highest_price; if greater
			//then that becomes the highest price entry!
			if(dOddsTuple.get(0).intValue() > highest_price) {
				highest_index = count;
				highest_price = dOddsTuple.get(0).intValue();
			}
		}
		
		if(highest_index != -1) {
			ArrayList <Integer> dOddsTuple = s_dynamic_odds.get(highest_index);
			//Jackpot odds = jackpot_available / tickets_available
			return(dOddsTuple.get(2).doubleValue() / ((double)numberTicketsAvailable()));
		}
		
		//If we didn't find anything in s_dynamic_odds (presumably because it is empty)
		//try in static odds
		else {
			for(int count = 0; count < s_static_odds.size(); count++) {
				ArrayList <Double> sOddsTuple = s_static_odds.get(count);
				//Compare price of the count-th entry in s_dynamic_odds to highest_price; if greater
				//then that becomes the highest price entry!
				if(sOddsTuple.get(0).intValue() > highest_price) {
					highest_index = count;
					highest_price = sOddsTuple.get(0).intValue();
				}
			}
		}
		
		if(highest_index != -1) {
			ArrayList <Double> sOddsTuple = s_static_odds.get(highest_index);
			//Jackpot odds = jackpot_available / tickets_available
			return(sOddsTuple.get(1).doubleValue());
		}
		else {
			System.err.println("jackpotOdds got no entries for scratcher " + scratcherName);
			return(0.0);
		}
	}
	
	/** This method calculates the expectation for this Scratcher
	
		@return this Scratcher's expectation */
	public double expectationValue() {

		double runningExp = 0.0;
		//we'll record this to avoid double-counting probabilities between static and dynamic data
		double smallest_dynamic_prize_size  = -1; 
		
		for(int count = 0; count < s_dynamic_odds.size(); count++) {
			ArrayList <Integer> dOddsTuple = s_dynamic_odds.get(count);
			runningExp += dOddsTuple.get(0).intValue() * dOddsTuple.get(2).intValue() / numberTicketsAvailable();
			
			if(smallest_dynamic_prize_size < dOddsTuple.get(0).doubleValue())
				smallest_dynamic_prize_size = dOddsTuple.get(0).doubleValue();
		}
		
		smallest_dynamic_prize_size -= 0.1; //decrease it slightly, to avoid double-counting and
			//double precision issues
		
		for(int count = 0; count < s_static_odds.size(); count++) {
			ArrayList <Double> sOddsTuple = s_static_odds.get(count);
			if(sOddsTuple.get(0).doubleValue() < smallest_dynamic_prize_size)
				runningExp += sOddsTuple.get(0).doubleValue() * sOddsTuple.get(1).doubleValue();
		}
		
		return runningExp;
	}
	
	/** A very verbose toString() method.
		@return A very verbose description of this ScratcherThing.
	*/
	public String toString() {
		String output = new String("");
		
		output += "Scratcher \"" + scratcherName + "\" (# " + Integer.toString(scratcherNumber) + ")\n";
		output += "Price: $" + Double.toString(s_price) + "\n";
		output += "Static odds: \n";
		
		for(int count = 0; count < s_static_odds.size(); count++) {
			ArrayList <Double> sOddsTuple = s_static_odds.get(count);
			output += "\tPrize: " + sOddsTuple.get(0) + ", Probability: " + sOddsTuple.get(1) + "\n";
		}
		
		output += "Dynamic odds: \n";
		
		for(int count = 0; count < s_dynamic_odds.size(); count++) {
			ArrayList <Integer> sOddsTuple = s_dynamic_odds.get(count);
			output += "\tPrize: " + sOddsTuple.get(0) + ", Claimed: " + sOddsTuple.get(1);
			output += ", Available: " + sOddsTuple.get(2) + "\n";
		}
		
		return(output);
	}

	/** Generates the database string that represents this scratcher object.
		A database string has very specific formatting requirements. It is a tab-delimited line
		that contains the following fields:
		
		1. Internal RowID (NOT present in string generated by this function)
		2. Scratcher Name
		3. Scratcher Series #
		4. Price
		5. Expectation
		6. Jackpot odds
		7. Overall grade
		8. Jackpot grade
		9. Warnings present?
		10. Warning text.
		11. CA Lottery URL
		
		@return The db line representing this scratcher */
	public String toDBLineString() {
		String output = new String("");
		
		output += scratcherName;
		output += "\t";
		output += Integer.toString(scratcherNumber);
		output += "\t";
		output += Double.toString(s_price);
		output += "\t";
		output += Double.toString(expectationValue());
		output += "\t";
		output += Double.toString(jackpotOdds());
		output += "\t";
		output += Integer.toString(s_overall_grade);
		output += "\t";
		output += Integer.toString(s_jackpot_grade);
		output += "\t";
		output += Integer.toString(s_hasWarning);
		output += "\t";
		output += warningText;
		output += "\t";
		output += scratcherURL;
		
		return(output);
	}
		
}
