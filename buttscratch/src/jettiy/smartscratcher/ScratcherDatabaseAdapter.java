/**
 * This adapter will allow us to access the Scratcher database. It will be used to fill
 * the database and also to query it, and to purge all entries if necessary (like when
 * we are performing an update on the database).
 */

package jettiy.smartscratcher;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import android.util.Log;

import java.lang.Integer;

public class ScratcherDatabaseAdapter {
	
	/* The below string is used to create the database in ScratcherDatabaseHelper (for reference):
	 * 
	 * private static final String DATABASE_CREATE = "create table scratchers (_id integer primary key autoincrement, "
	 * 	+ "name text not null, series_no integer not null, price integer not null, expectation integer not null," +
	 * 			" jackpot_odds integer not null, overall_grade integer not null, jackpot_grade integer not null, " +
	 * 			"warnings integer not null, warning_text string not null);"; */

	// Database fields
	public static final String KEY_ROWID = "_id";
	public static final String KEY_SCRATCHERNAME = "name";
	public static final String KEY_SERIESNO = "series_no";
	public static final String KEY_PRICE = "price";
	public static final String KEY_EXPECTATION = "expectation";
	public static final String KEY_JACKPOT_ODDS = "jackpot_odds";
	public static final String KEY_OVERALL_GRADE = "overall_grade";
	public static final String KEY_JACKPOT_GRADE = "jackpot_grade";
	public static final String KEY_WARNINGS = "warnings";
	public static final String KEY_WARNING_TEXT = "warning_text";
	public static final String KEY_CALOT_URL = "calot_url";

	private static final String DATABASE_TABLE = "scratchers";
	private Context context;
	private SQLiteDatabase database;
	private ScratcherDatabaseHelper dbHelper;

	public ScratcherDatabaseAdapter(Context context) {
		this.context = context;
	}

	public ScratcherDatabaseAdapter open() throws SQLException {
		dbHelper = new ScratcherDatabaseHelper(context);
		database = dbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		dbHelper.close();
	}

	
/** Creates a new scratcher DB entry, from the provided input fields. */

	public long createScratcher(String scratcher_name, int scratcher_id, int price, double expectation, 
			double jackpot_odds, int overall_grade, int jackpot_grade, int warnings, String warning_text, String calot_url) {
		
		//Pack the values into a ContentValues object
		ContentValues scratcherValues = packValues(scratcher_name, scratcher_id, price, expectation, 
				jackpot_odds, overall_grade, jackpot_grade, warnings, warning_text, calot_url);
		//Insert these values into the database
		
		if(database == null) {
			Log.e("ScratcherDatabaseHelper", "database was null in createScratcher!");
			return(-1);
		}
		else if(scratcherValues == null) {
			Log.e("ScratcherDatabaseHelper", "scratcherValues was null in createScratcher!");
			return(-1);
		}
		else return database.insert(DATABASE_TABLE, null, scratcherValues);
	}

	
/** Update the todo */

	public boolean updateScratcher(long rowId, String scratcher_name, int scratcher_id, int price, double expectation, 
			double jackpot_odds, int overall_grade, int jackpot_grade, int warnings, String warning_text, String calot_url) {
		ContentValues updateValues = packValues(scratcher_name, scratcher_id, price, expectation, 
				jackpot_odds, overall_grade, jackpot_grade, warnings, warning_text, calot_url);

		return database.update(DATABASE_TABLE, updateValues, KEY_ROWID + "="
				+ rowId, null) > 0;
	}

	
/** Deletes a single scratcher specified by rowId */
	public boolean deleteScratcher(long rowId) {
		return database.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
	}
	
/** Deletes all scratchers */
	public boolean deleteAllScratchers() {
		return database.delete(DATABASE_TABLE, "1", null) > 0;
	}

	
/** Return a Cursor over the list of all scratchers in the database * * @return Cursor over all notes */

	public Cursor fetchAllScratchers() {
		return database.query(DATABASE_TABLE, new String[] {
				KEY_ROWID, KEY_SCRATCHERNAME, KEY_SERIESNO, KEY_PRICE, KEY_EXPECTATION, KEY_JACKPOT_ODDS,
				KEY_OVERALL_GRADE, KEY_JACKPOT_GRADE, KEY_WARNINGS, KEY_WARNING_TEXT, KEY_CALOT_URL}, null, null, null,
				null, null);
	}

	
/** Return a Cursor positioned at a scratcher that is denoted by rowID */

	public Cursor fetchScratcher(long rowId) throws SQLException {
		Cursor mCursor = database.query(true, DATABASE_TABLE, new String[] {
				KEY_ROWID, KEY_SCRATCHERNAME, KEY_SERIESNO, KEY_PRICE, KEY_EXPECTATION, KEY_JACKPOT_ODDS,
				KEY_OVERALL_GRADE, KEY_JACKPOT_GRADE, KEY_WARNINGS, KEY_WARNING_TEXT, KEY_CALOT_URL},
				KEY_ROWID + "=" + rowId, null, null, null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;
	}
	
	/** Return a Cursor over the list of all scratchers in the database, that have price equal to parameter price.
	 * @return Cursor over all notes */

	public Cursor fetchScratchersByPrice(int price) {
		String query_string = KEY_PRICE + " like " + Integer.toString(price);
		
		return database.query(DATABASE_TABLE, new String[] {
				KEY_ROWID, KEY_SCRATCHERNAME, KEY_SERIESNO, KEY_PRICE, KEY_EXPECTATION, KEY_JACKPOT_ODDS,
				KEY_OVERALL_GRADE, KEY_JACKPOT_GRADE, KEY_WARNINGS, KEY_WARNING_TEXT, KEY_CALOT_URL}, query_string, null, null,
				null, null);
	}


	private ContentValues packValues(String scratcher_name, int scratcher_id, int price, double expectation, 
			double jackpot_odds, int overall_grade, int jackpot_grade, int warnings, String warning_text, String calot_url) {
		ContentValues values = new ContentValues();
		
		values.put(KEY_SCRATCHERNAME, scratcher_name);
		values.put(KEY_SERIESNO, scratcher_id);
		values.put(KEY_PRICE, price);
		values.put(KEY_EXPECTATION, expectation);
		values.put(KEY_JACKPOT_ODDS, jackpot_odds);
		values.put(KEY_OVERALL_GRADE, overall_grade);
		values.put(KEY_JACKPOT_GRADE, jackpot_grade);
		values.put(KEY_WARNINGS, warnings);
		values.put(KEY_WARNING_TEXT, warning_text);
		values.put(KEY_CALOT_URL, calot_url);
		
		return values;
	}
}