/**
 * This file defines the Scratcher Database "helper" that will be used by the SmartScratcher
 * app to manage statistics about the scratchers. It is filled through a fetch from the server,
 * and it used by ScratcherListList to display the scratchers.
 */

package jettiy.smartscratcher;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.util.Log;

public class ScratcherDatabaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "scratcherdata";
	private static final int DATABASE_VERSION = 1;
	
	/*Database columns
	1. Internal RowID (used as key)
	2. Scratcher name
	3. ID Number 
	4. Price
	5. Expectation
	6. Jackpot odds vs starting
	7. Overall grade
	8. Jackpot grade
	9. Any warnings?
	10. Warning text
	11. CA Lottery URL*/	
	private static final String DATABASE_CREATE = "create table scratchers (_id integer primary key autoincrement, "
		+ "name text not null, series_no integer not null, price integer not null, expectation real not null," +
				" jackpot_odds real not null, overall_grade integer not null, jackpot_grade integer not null, " +
				"warnings integer not null, warning_text string not null, calot_url string not null);";
	
	@Override
	public void onCreate(SQLiteDatabase arg0) {
		arg0.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(ScratcherDatabaseHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS scratchers");
		onCreate(db);
	}
	
	public ScratcherDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

}
