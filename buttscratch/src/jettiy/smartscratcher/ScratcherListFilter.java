package jettiy.smartscratcher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ScratcherListFilter extends Activity {
	
	Button but_viewall;
	Button but_view1;
	Button but_view2;
	Button but_view3;
	Button but_view5;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.scratcherlistfilter); //Set the current "screen" to main
                
        //Create list item pointers
        but_viewall = (Button) this.findViewById(R.id.filter_viewall);
        but_view1 = (Button) this.findViewById(R.id.filter_view1);
        but_view2 = (Button) this.findViewById(R.id.filter_view2);
        but_view3 = (Button) this.findViewById(R.id.filter_view3);
        but_view5 = (Button) this.findViewById(R.id.filter_view5);
        //Run the function that creates button handers
        initAddButtonListeners();
       
    }
    
    /** This function sets up all of the listeners for the buttons */
    public void initAddButtonListeners() {
    	//Set up the handler for "view all" button
		if(but_viewall == null) return;
		but_viewall.setOnClickListener(new View.OnClickListener() {

			//@Override
			public void onClick(View view) {
				//Launch the scratcher browser with no scratcher types filtered
				Intent myIntent = new Intent(ScratcherListFilter.this, ScratcherListList.class);
				myIntent.putExtra("filter", "-1");
				ScratcherListFilter.this.startActivity(myIntent);
			}
		});
		//Set up the handler for "view $1 scratchers" button
		if(but_view1 == null) return;
		but_view1.setOnClickListener(new View.OnClickListener() {

			//@Override
			public void onClick(View view) {
				//Launch the scratcher browser with no scratcher types filtered
				Intent myIntent = new Intent(ScratcherListFilter.this, ScratcherListList.class);
				myIntent.putExtra("filter", "1");
				ScratcherListFilter.this.startActivity(myIntent);
			}
		});
		//Set up the handler for "view $2 scratchers" button
		if(but_view2 == null) return;
		but_view2.setOnClickListener(new View.OnClickListener() {

			//@Override
			public void onClick(View view) {
				//Launch the scratcher browser with no scratcher types filtered
				Intent myIntent = new Intent(ScratcherListFilter.this, ScratcherListList.class);
				myIntent.putExtra("filter", "2");
				ScratcherListFilter.this.startActivity(myIntent);
			}
		});
		//Set up the handler for "view $3 scratchers" button
		if(but_view3 == null) return;
		but_view3.setOnClickListener(new View.OnClickListener() {

			//@Override
			public void onClick(View view) {
				//Launch the scratcher browser with no scratcher types filtered
				Intent myIntent = new Intent(ScratcherListFilter.this, ScratcherListList.class);
				myIntent.putExtra("filter", "3");
				ScratcherListFilter.this.startActivity(myIntent);
			}
		});
		//Set up the handler for "view $5 scratchers" button
		if(but_view5 == null) return;
		but_view5.setOnClickListener(new View.OnClickListener() {

			//@Override
			public void onClick(View view) {
				//Launch the scratcher browser with no scratcher types filtered
				Intent myIntent = new Intent(ScratcherListFilter.this, ScratcherListList.class);
				myIntent.putExtra("filter", "5");
				ScratcherListFilter.this.startActivity(myIntent);
			}
		});
		
    }
    
    
	
	public ScratcherListFilter() {
		super();
	}

}
