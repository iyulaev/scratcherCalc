package jettiy.smartscratcher;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;

import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class ScratcherView extends Activity {
	
	String LOG_TAG = "ScratcherView";

	/** Called when the activity is first created. Sets up all of the View elements. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        
        setContentView(R.layout.scratcherview); //Set the current "screen" to main
        
        //Parse the filter passed by ScratchListFilter, telling us what price to filter by
        int scratcher_rowid;
        //String priceFilterString = savedInstanceState.getString("filter");
        String scratcher_rowid_str = this.getIntent().getExtras().getString("rowid");
        if(scratcher_rowid_str != null) scratcher_rowid = Integer.parseInt(scratcher_rowid_str); 
        else {
        	scratcher_rowid = 0;
        	Log.e(LOG_TAG, "Didn't get a valid Scratcher row ID.");
        }
        
        Log.i(LOG_TAG, "Got " + scratcher_rowid_str + " as row_id for this scratcher.");
        
        //Query DB for the scratcher pointed to by scratcher_rowid, and extract the data from it
        ScratcherDatabaseAdapter sdbAdapter = ((SmartScratcherApp) getApplication()).sdbAdapter;
        Cursor m_cursor = sdbAdapter.fetchScratcher((long)scratcher_rowid);
        if(m_cursor != null) m_cursor.moveToFirst();
        
        //Extract data elements
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
		String url_text = m_cursor.getString(10);
		
		m_cursor.close();
		
		Log.i(LOG_TAG, "Starting to write stuff to scratcherview, f.ex. the name will be: " + m_name_string);
		
		//Post the data elements to the ScratcherView
		TextView nameText = (TextView) this.findViewById(R.id.scratcherview_scratchername);
		nameText.setText(m_name_string +  " (#" + m_series_no + ")");
		
		
		TextView expectedValueText = (TextView) this.findViewById(R.id.scratcherview_expectedvaluetext);
		ImageView expectedValueIcon = (ImageView) this.findViewById(R.id.scratcherview_expectedvalueicon);
		expectedValueText.setText(new String("Expected Value: " + expectation));
		if(overall_grade == 1) expectedValueIcon.setImageResource(R.drawable.red_x_small);
    	else if(overall_grade == 2) expectedValueIcon.setImageResource(R.drawable.yellow_minus);
    	else if(overall_grade == 3) expectedValueIcon.setImageResource(R.drawable.check_small);
    	else expectedValueIcon.setImageResource(R.drawable.yellow_minus); //should never happen
		
		TextView jackpotOddsText = (TextView) this.findViewById(R.id.scratcherview_jackpotoddstext);
		ImageView jackpotOddsIcon = (ImageView) this.findViewById(R.id.scratcherview_jackpotoddsicon);
		jackpotOddsText.setText(new String("Jackpot Odds (vs. Starting): " + jackpot_odds));
		if(jackpot_grade == 1) jackpotOddsIcon.setImageResource(R.drawable.onebags_money);
    	else if(jackpot_grade == 2) jackpotOddsIcon.setImageResource(R.drawable.twobags_money);
    	else if(jackpot_grade == 3) jackpotOddsIcon.setImageResource(R.drawable.threebags_money);
    	else jackpotOddsIcon.setImageResource(R.drawable.onebags_money); //should never happen
		
		TextView warningsText = (TextView) this.findViewById(R.id.scratcherview_warningstext);
		ImageView warningsIcon = (ImageView) this.findViewById(R.id.scratcherview_warningsicon);
		if(warnings == 0); //do nothing
		else warningsText.setText(new String(warning_text));
		if(warnings == 0) warningsIcon.setImageResource(R.drawable.noexclamation_small);
    	else warningsIcon.setImageResource(R.drawable.exclamation_small);
		
		TextView urlText = (TextView) this.findViewById(R.id.scratcherview_urltext);
		urlText.setMovementMethod(LinkMovementMethod.getInstance());
		String text = "<a href=\"" + url_text + "\">CA Lottery Web Link</a>";
		urlText.setText(Html.fromHtml(text));
    	
    }
}
