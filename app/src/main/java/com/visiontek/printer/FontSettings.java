package com.visiontek.printer;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.a97bt_usb_application.R;
import com.example.com.example.usbserial.UsbSerialDevice;
import com.example.mylibrary.VisiontekUSB;



/*
 * 
 * @author Sreekanth <sreekanth.reddy@visiontek.co.in>
 *
 */

public class FontSettings extends Activity {

	private static final String TAG = "BTFONTSETTINGS";

	private Spinner fontStyle, fontType, fontSize;
	private Button setFonts;
	private int valueStyle;
	private int valueType;
	private int valueSize;

	VisiontekUSB vusb=new VisiontekUSB();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fontsettings);

		addItemsOnSpinner2();
		addItemsOnSpinner3();
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
		list.add("Tahoma");

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

	public void addListenerOnSpinnerItemSelection() {
		fontStyle = (Spinner) findViewById(R.id.style);
		fontStyle.setOnItemSelectedListener(new MyOnItemSelectedListener());
	}

	public void addListenerOnButton() {

		fontStyle = (Spinner) findViewById(R.id.style);
		fontType = (Spinner) findViewById(R.id.type);
		fontSize = (Spinner) findViewById(R.id.size);
		setFonts = (Button) findViewById(R.id.setfonts);

		setFonts.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				valueStyle = 1 + fontStyle.getSelectedItemPosition();
				valueType = 1 + fontType.getSelectedItemPosition();
				valueSize = 1 + fontSize.getSelectedItemPosition();

				Log.d(TAG, "THE SELECTED STYLE : " + valueStyle + " "
						+ valueType + " " + valueSize);

				showToast(vusb.fontSettings(UsbSerialDevice.outEndpoint,UsbSerialDevice.inEndpoint, valueType, valueStyle,
						valueSize));

				valueStyle = 0;
				valueType = 0;
				valueSize = 0;

			}

		});
	}

	public void showToast(final String toast) {

		Builder alert = new Builder(FontSettings.this);
		alert.setTitle("BT PRINTER");

		alert.setMessage(toast);
		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				dialog.dismiss();

			}
		});

		alert.show();

	}

}
