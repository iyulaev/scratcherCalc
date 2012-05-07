package jettiy.smartscratcher;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

//java utility stuff
import java.lang.Integer;

public class ScratcherListList extends Activity {
	
	String LOG_TAG = "ScratcherListList";
	
	/** Called when the activity is first created. Sets up all of the View elements. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.scratcherlistlist); //Set the current "screen" to main
        
        //Parse the filter passed by ScratchListFilter, telling us what price to filter by
        int price_filter;
        //String priceFilterString = savedInstanceState.getString("filter");
        String priceFilterString = this.getIntent().getExtras().getString("filter");
        if(priceFilterString != null) price_filter = Integer.parseInt(priceFilterString); 
        else price_filter = -1;
        
        Log.i(LOG_TAG,"onCreate() beginning DB loading, got price_filter = " + price_filter);
        
        //Query database for scratchers matching the query
        ScratcherDatabaseAdapter sdbAdapter = ((SmartScratcherApp) getApplication()).sdbAdapter;
        
        Cursor m_cursor;
        if(price_filter >= 0) m_cursor = sdbAdapter.fetchScratchersByPrice(price_filter);
        else m_cursor = sdbAdapter.fetchAllScratchers();
        if(m_cursor != null) populateList(m_cursor);
        else Log.e(LOG_TAG,"onCreate() says that m_cursor was NULL");
        m_cursor.close();
    }
    
    /** This method pulls data out of the database by iterating m_cursor, and then
     * fills the ScratcherListList main view with a series of lines, one for each
     * scratcher loaded from the database.
     * 
     * @param m_cursor The database cursor pointing to the database elements with which
     * the list will be filled. 
     */
    public void populateList(Cursor m_cursor) {
    	//Get parent linearlayout of the ScratcherListList View
    	LinearLayout parent_ll = (LinearLayout) this.findViewById(R.id.scratcherlistlist_main_ll);
    	
    	//Add something to it using a LayoutInflater
    	LayoutInflater mInflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
    	
    	Log.i(LOG_TAG,"Beginning to add scratchers to SLL.");
    	int scratcher_count=0;
    	
    	m_cursor.moveToFirst();
    	//This loop will fill parent_ll with scratcher entries, one for each query hit from the db
    	while(!m_cursor.isAfterLast()) {
    		
    		//Extract all of the data fields
    		/*
    		 * values.put(KEY_SCRATCHERNAME, scratcher_name);
				values.put(KEY_SERIESNO, scratcher_id);
				values.put(KEY_PRICE, price);
				values.put(KEY_EXPECTATION, expectation);
				values.put(KEY_JACKPOT_ODDS, jackpot_odds);
				values.put(KEY_OVERALL_GRADE, overall_grade);
				values.put(KEY_JACKPOT_GRADE, jackpot_grade);
				values.put(KEY_WARNINGS, warnings);
				values.put(KEY_WARNING_TEXT, warning_text);
    		 */
    		int m_rowid = Integer.parseInt(m_cursor.getString(0));
			String m_name_string = m_cursor.getString(1);
			String m_series_no = m_cursor.getString(2);
			int price = Integer.parseInt(m_cursor.getString(3));
			double expectation = Double.parseDouble(m_cursor.getString(4));
			double jackpot_odds = Double.parseDouble(m_cursor.getString(5));
			int overall_grade = Integer.parseInt(m_cursor.getString(6));
			int jackpot_grade = Integer.parseInt(m_cursor.getString(7));
			int warnings = Integer.parseInt(m_cursor.getString(8));
			String warning_text = m_cursor.getString(9);
			
			//Create a new scratcher object (a LinearLayout really) that we'll add to parent_ll
	    	LinearLayout newScratcher = (LinearLayout) mInflater.inflate(R.layout.scratcherlistlist_listitem, null);
	    	TextView newScratcherName = (TextView) newScratcher.findViewById(R.id.scratcherlistlist_item_scratchername);
	    	newScratcherName.setText(m_name_string + " (#" + m_series_no + ")");
	    	parent_ll.addView(newScratcher);
	    	//Handle filling in the icons correctly
	    	ImageView warningIcon = (ImageView) newScratcher.findViewById(R.id.scratcherlistlist_item_icon_warning);
	    	ImageView jackpotOddsIcon = (ImageView) newScratcher.findViewById(R.id.scratcherlistlist_item_icon_overall_odds);
	    	ImageView overallOddsIcon = (ImageView) newScratcher.findViewById(R.id.scratcherlistlist_item_icon_jackpot_odds);
	    	
	    	//Warnings icon
	    	if(warnings == 0) warningIcon.setImageResource(R.drawable.noexclamation_small);
	    	else warningIcon.setImageResource(R.drawable.exclamation_small);
	    	//Jackpot odds icon
	    	if(jackpot_grade == 1) jackpotOddsIcon.setImageResource(R.drawable.onebags_money);
	    	else if(jackpot_grade == 2) jackpotOddsIcon.setImageResource(R.drawable.twobags_money);
	    	else if(jackpot_grade == 3) jackpotOddsIcon.setImageResource(R.drawable.threebags_money);
	    	else jackpotOddsIcon.setImageResource(R.drawable.onebags_money); //should never happen
	    	//Overall odds icon
	    	if(overall_grade == 1) overallOddsIcon.setImageResource(R.drawable.red_x_small);
	    	else if(overall_grade == 2) overallOddsIcon.setImageResource(R.drawable.yellow_minus);
	    	else if(overall_grade == 3) overallOddsIcon.setImageResource(R.drawable.check_small);
	    	else overallOddsIcon.setImageResource(R.drawable.yellow_minus); //should never happen
	    	
	    	//Create OnClick event so that clicking on this scratcher causes the app to launch
	    	//a ScratcherView, displaying this scratcher
	    	newScratcher.setTag(new String(Integer.toString(m_rowid))); 
	    		//The above is necessary to "transplant" m_rowid into the new view
	    	newScratcher.setOnClickListener(new View.OnClickListener() {

				//@Override
				public void onClick(View view) {
					//Launch the scratcher browser
					Intent myIntent = new Intent(ScratcherListList.this, ScratcherView.class);
					myIntent.putExtra("rowid", (String) view.getTag());
					ScratcherListList.this.startActivity(myIntent);
				}
			});
			
	    	scratcher_count++;
			m_cursor.moveToNext(); //Advance cursor position
		}
    	
    	Log.i(LOG_TAG,"Added " + scratcher_count + " scratchers to SLL.");
    	
    	//return;
    } // [/populateList]
}
