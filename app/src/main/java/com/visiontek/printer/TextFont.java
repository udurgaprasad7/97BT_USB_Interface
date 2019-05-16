package com.visiontek.printer;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.example.a97bt_usb_application.R;



/*
 * 
 * @author Sreekanth <sreekanth.reddy@visiontek.co.in>
 *
 */

public class TextFont extends Activity {

	private static final String TAG = "TEXTFONT";

	private Spinner fontType, fontStyle, fontSize, fontAlign;
	private Button setFonts;
	static int valueType;
	static int valueStyle;

	static int valueSize;
	static int valueAlign;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.text_font);

		addItemsOnSpinner2();
		addItemsOnSpinner3();
		addItemsOnSpinner4();
		addListenerOnButton();
		addListenerOnSpinnerItemSelection();
	}

	// add items into spinner dynamically
	public void addItemsOnSpinner2() {

		fontType = (Spinner) findViewById(R.id.type);
		List<String> list = new ArrayList<String>();
		list.add("Rockwell");
		list.add("Ariel");
		list.add("Calibri");
		list.add("Verdana");
		list.add("Californian");
		list.add("Century");
		list.add("Dejavu_Serif");
		list.add("Trebuchet_MS");
		list.add("Berlin_Sans_FB");
		list.add("Bell_MT");

		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, list);
		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		fontType.setAdapter(dataAdapter);
	}

	public void addItemsOnSpinner3() {

		fontSize = (Spinner) findViewById(R.id.size);
		List<String> list = new ArrayList<String>();
		list.add("Small");
		list.add("Medium");
		list.add("Large");

		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, list);
		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		fontSize.setAdapter(dataAdapter);
	}

	public void addItemsOnSpinner4() {

		fontAlign = (Spinner) findViewById(R.id.align);
		List<String> list = new ArrayList<String>();
		list.add("Left");
		list.add("Center");
		list.add("Right");

		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, list);
		dataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		fontAlign.setAdapter(dataAdapter);
	}

	public void addListenerOnSpinnerItemSelection() {
		fontStyle = (Spinner) findViewById(R.id.style);
		fontStyle.setOnItemSelectedListener(new MyOnItemSelectedListener());
	}

	public void addListenerOnButton() {
		fontType = (Spinner) findViewById(R.id.type);
		fontStyle = (Spinner) findViewById(R.id.style);

		fontSize = (Spinner) findViewById(R.id.size);
		fontAlign = (Spinner) findViewById(R.id.align);

		setFonts = (Button) findViewById(R.id.setfonts);

		setFonts.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				valueType = 1 + fontType.getSelectedItemPosition();
				valueStyle = 1 + fontStyle.getSelectedItemPosition();
				valueSize = 1 + fontSize.getSelectedItemPosition();
				valueAlign = fontAlign.getSelectedItemPosition();
				
				Log.d(TAG, "THE SELECTED STYLE : " + valueStyle + " "
						+ valueType + " " + valueSize);

			}

		});
	}

}
