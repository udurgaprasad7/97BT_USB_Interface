package com.visiontek.printer;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.example.a97bt_usb_application.R;

public class Print_MultiFont_Settings extends Activity implements OnClickListener {
	public Button text_Settings;
	public Button text_Print;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.print_multisettings);
		text_Settings=(Button)findViewById(R.id.multisetting);
		text_Print=(Button)findViewById(R.id.text);
		text_Settings.setOnClickListener(this);
		text_Print.setOnClickListener(this);
		
	}
	public void onClick(View v) {

		if (v.equals(text_Settings)) {
			Intent intnt = new Intent(Print_MultiFont_Settings.this, Select_Text_Format.class);
			startActivity(intnt);
		}
		if(v.equals(text_Print)){
			Intent intnt = new Intent(Print_MultiFont_Settings.this, MultiFontText_Printing.class);
			startActivity(intnt);
			
		}
	}
	
	

}
