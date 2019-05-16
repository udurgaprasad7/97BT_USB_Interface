package com.visiontek.printer;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;


/*
 * 
 * @author Sreekanth <sreekanth.reddy@visiontek.co.in>
 *
 */

public class MyOnItemSelectedListener implements OnItemSelectedListener {

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {

		/*Toast.makeText(parent.getContext(),
				"Selected Style : " + parent.getItemAtPosition(pos).toString(),
				Toast.LENGTH_SHORT).show();*/

	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}

}
