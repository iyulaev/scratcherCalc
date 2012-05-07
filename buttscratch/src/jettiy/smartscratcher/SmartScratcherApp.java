package jettiy.smartscratcher;

import android.app.Application;

/** The SmartScratcherApp class extends the default android.app.Application class.
 * The only added functionality (for now) is that the ScratcherDB database of scratcher
 * data is stored in this class as a "global variable".
 * @author ivany
 *
 */

public class SmartScratcherApp extends Application {
	//Database variables
	public ScratcherDatabaseAdapter sdbAdapter;
	
	public SmartScratcherApp() {
		super();
	}

}
