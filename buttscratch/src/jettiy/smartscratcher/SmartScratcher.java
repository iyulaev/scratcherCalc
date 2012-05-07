package jettiy.smartscratcher;

import android.app.Activity;

//Some default imports that I used
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.ProgressBar;

//For launching Activities
import android.content.Intent;

//Used for logging
import android.util.Log;

//For database
import android.database.Cursor;

//Used for sleep() - I'll remove this in the final version
import java.io.IOException;
import java.io.InputStream;
import java.lang.Thread;

//Misc java basic stuff
import java.lang.Long;

//Stuff for HTTP connections
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.InputStream;

public class SmartScratcher extends Activity {
	/* Some buttons and such that are part of the UI */
	protected Button but_update;
	protected Button but_browse;
	protected Button but_exit;
	
	//Database DEBUGGING/TESTING variables
	private final boolean DO_DB_TESTFILL = false;
	private final boolean DO_DB_TESTQUERY = false;
	
	private final String LOG_TAG = "SmartScratcherActivityLog";
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main); //Set the current "screen" to main
                
        //Create button pointers
        but_update = (Button) this.findViewById(R.id.update_button);
        but_browse = (Button) this.findViewById(R.id.browse_button);
        but_exit = (Button) this.findViewById(R.id.exit_button);
        //Run the function that creates button handers
        initAddButtonListeners();
        initDatabase();
        
        Log.i(LOG_TAG,"SmartScratcher just completed onCreate().");
    }


	/**
	 * Method is used to encapsulate the code that will handle the button presses.
	 * This method gets called during initialization to set up all of the button handers
	 * and other such things.
	 */
	protected void initAddButtonListeners() {
		
		//Set up the handler for update button to call doDataUpdate, in another thread
		if(but_update == null) return;
		but_update.setOnClickListener(new View.OnClickListener() {

			//@Override
			public void onClick(View view) {
				//Run the data updater in a separate thread
				new Thread(new Runnable() { 
					public void run() { doDataUpdate(); }
				}).run();
			}
		});
		
		//Set up the handler for browse button to launch the scratcher browser/filter menu
		if(but_browse == null) return;
		but_browse.setOnClickListener(new View.OnClickListener() {

			//@Override
			public void onClick(View view) {
				//Launch the scratcher browser
				Intent myIntent = new Intent(SmartScratcher.this, ScratcherListFilter.class);
				SmartScratcher.this.startActivity(myIntent);
			}
		});
		
		//Set up the exit for update button to exit (this isn't really an Android thing...)
		if(but_exit == null) return;
		but_exit.setOnClickListener(new View.OnClickListener() {

			//@Override
			public void onClick(View view) {
				finish();
			}
		});
	}
	
	/** This method begins an update of the data. It will make the progress bar visible. It will then update
	 * it as the data update keeps going, and at the end, it will hide the progress bar and display the
	 * 'view data' button.
	 * @returns Whether update was successful or not.
		 */
	public boolean doDataUpdate() {
		//Grab UI elements that we will modify
		final Button m_browsebutton = (Button) findViewById(R.id.browse_button);
		final ProgressBar m_progressbar = (ProgressBar) findViewById(R.id.browse_progressbar);
		//Set browse button visibility to GONE, progress bar to VISIBLE
		m_browsebutton.post(new Runnable() {
			public void run() {
				m_browsebutton.setVisibility(View.GONE);
			}
		});
		m_progressbar.post(new Runnable() {
			public void run() {
				m_progressbar.setVisibility(View.VISIBLE);
				m_progressbar.setProgress(0);
			}
		});
		
		//Grab file from server
		final int DL_BUFFER_SIZE = 4096; //size of dl_buffer (a "#define")
		int bytes_downloaded = 0;
		int total_bytes = 0;
		String bufferStr = new String(); //String used to store entire downloaded file
		byte[] dl_buffer = new byte [DL_BUFFER_SIZE]; //temporary buffer to store downloaded HTTP data
		
		try {
			//Open connection to server and grab the file
			//Code taken from http://www.androidsnippets.com/download-an-http-file-to-sdcard-with-progress-notification
			URL url = new URL("<server_database_file_URL>");
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
	        urlConnection.setDoOutput(true);
	        urlConnection.connect();
	        InputStream inputStream = urlConnection.getInputStream();
	
			while((bytes_downloaded = inputStream.read(dl_buffer)) > 0) {
				total_bytes += bytes_downloaded;
				bufferStr = bufferStr + new String(dl_buffer);
				dl_buffer = new byte[DL_BUFFER_SIZE];
			}
		}
		catch (MalformedURLException e) {
	        Log.e(LOG_TAG, "Got invalid URL in SmartScratcher.doDataUpdate().");
		} catch (IOException e) {
			Log.e(LOG_TAG, "Got IO Exception in SmartScratcher.doDataUpdate():");
			Log.e(LOG_TAG,e.toString());
		}
		
		//Update progress bar to show 50% completion
		m_progressbar.post(new Runnable() {
			public void run() {
				m_progressbar.setProgress(50);
			}
		});
		
		Log.i(LOG_TAG,"Finished getting file from test server. Now going to parse the file\n");
		
		//First, clear the database
		ScratcherDatabaseAdapter sdbAdapter = ((SmartScratcherApp) getApplication()).sdbAdapter;
		sdbAdapter.deleteAllScratchers();
		
		//Parse the data that we have in bufferStr; first split bufferStr into lines
		String[] dataLines = bufferStr.split("\n");
		int db_entries_added = 0;
		//Now we will enter each line into the database
		for(int i = 0; i < dataLines.length; i++) {
			String[] dataTokens = dataLines[i].split("\t"); //split by tab (file is tab delimited)
			
			//Shove dataTokens into the database
			if(dataTokens.length != 10) {
				Log.w(LOG_TAG, "Line " + i + " in scratchers file downloaded from server was invalid, wrong number of tokens.");
			}
			else {
				//sdbAdapter.createScratcher("test scratcher", 111, 1, 0.97, 0.60, 2, 2, 1, "This is a warning!", "http://www.google.com");
				sdbAdapter.createScratcher(dataTokens[0], 
						Integer.parseInt(dataTokens[1]),
						Integer.parseInt(dataTokens[2]),
						Double.parseDouble(dataTokens[3]),
						Double.parseDouble(dataTokens[4]),
						Integer.parseInt(dataTokens[5]),
						Integer.parseInt(dataTokens[6]),
						Integer.parseInt(dataTokens[7]),
						dataTokens[8],
						dataTokens[9] );
				db_entries_added++;
			}
		}
		
		Log.i(LOG_TAG,"Inserted " + db_entries_added + " entries into the database, in doDataUpdate().");
		
		//Now, hide the progress bar and show us the update button!
		m_browsebutton.post(new Runnable() {
			public void run() {
				m_browsebutton.setVisibility(View.VISIBLE);
			}
		});
		
		m_progressbar.post(new Runnable() {
			public void run() {
				m_progressbar.setVisibility(View.GONE);
			}
		});
		
		return(true);
	}
	
	/** This function initializes the ScratcherDatabaseAdaper db adapter */
	protected void initDatabase() {
		//Delete database
		//this.deleteDatabase("scratcherdata");
		
		ScratcherDatabaseAdapter sdbAdapter = ((SmartScratcherApp) getApplication()).sdbAdapter;
		if(sdbAdapter == null) {
			sdbAdapter = new ScratcherDatabaseAdapter(this);
			((SmartScratcherApp) getApplication()).sdbAdapter = sdbAdapter;
		}
		
		sdbAdapter.open();
		
		Log.i(LOG_TAG,"SmartScratcher just created a ScratcherDatabaseAdapter!");
		
		//This block tests the database by putting in some (bogus) scratchers into it
		if(DO_DB_TESTFILL) {
			sdbAdapter.deleteAllScratchers(); //Remove the previously created bogus scratchers
			long db_ids[] = new long[3];
			/* public long createScratcher(String scratcher_name, int scratcher_id, int price, double expectation, 
			double jackpot_odds, int overall_grade, int jackpot_grade, int warnings, String warning_text); */
			db_ids[0] = sdbAdapter.createScratcher("test scratcher", 111, 1, 0.97, 0.60, 2, 2, 1, "This is a warning!", "http://www.google.com");
			db_ids[1] = sdbAdapter.createScratcher("testicle scratcher", 222, 2, 0.95, 0.65, 3, 3, 0, "", "http://www.google.com");
			db_ids[2] = sdbAdapter.createScratcher("butt scratcher", 222, 1, 1.06, 1.65, 1, 1, 1, "Warning!!! #2!!!", "http://www.google.com");
			
			String m_log_string;
			m_log_string = "SmartScratcher just inserted 3 test scratchers into database, with ids ";
				
			for(int i = 0; i < 3; i++) m_log_string += (Long.toString(db_ids[i]) + ", ");
			
			Log.i(LOG_TAG,m_log_string);
			
			
		}
		
		//This block tests that the "bogus" scratchers written in by DO_DB_TESTFILL were filled
		//in correctly, and does a test query to read out some of the contents
		if(DO_DB_TESTFILL && DO_DB_TESTQUERY) {
			int scratcher_count = 0;
			
			Cursor m_cursor = sdbAdapter.fetchScratchersByPrice(1);
			
			m_cursor.moveToFirst();
			
			String m_name_string = new String("");
			
			while(!m_cursor.isAfterLast()) {
				m_name_string += m_cursor.getString(1) + " ";
				
				m_cursor.moveToNext();
				scratcher_count++;
			}
			m_cursor.close();
			
			String m_log_string;
			m_log_string = "Got " + Integer.toString(scratcher_count) + " query hits for price of 1.";
			Log.i(LOG_TAG, m_log_string);
			
			m_log_string = "These two scratchers had names " + m_name_string;
			Log.i(LOG_TAG, m_log_string);
		}		
		

	}
}