package org.ueu.uninet.it;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class IragarkiaHoniburuz extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//Gordetako iragarkiak ezabatu, sareko iragarki berriekin eguneratzeko.
		setContentView(R.layout.honiburuz);
		TextView bertsioa = (TextView) findViewById(R.id.bertsioa);
		bertsioa.setText("Bertsioa " + bertsioa.getText());
	}

}
