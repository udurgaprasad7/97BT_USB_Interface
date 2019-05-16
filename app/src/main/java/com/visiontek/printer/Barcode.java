package com.visiontek.printer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.a97bt_usb_application.R;
import com.example.com.example.usbserial.UsbSerialDevice;
import com.example.mylibrary.VisiontekUSB;


/*
 * 
 * @author Sreekanth <sreekanth.reddy@visiontek.co.in>
 *
 */

public class Barcode extends Activity {

	private static final String TAG = "BTBARCODE";

	private Button printBarcode;
	private EditText enterText;
	VisiontekUSB vusb = new VisiontekUSB();

	private int item_value = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.barcode_list);
		Spinner dropdown = (Spinner) findViewById(R.id.select);
		String[] items = new String[] { "Code 128", "PDF 417", "Data Matrix" };
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, items);
		dropdown.setAdapter(adapter);

		dropdown.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> adapter, View v,
					int position, long id) {
				String select_item = adapter.getItemAtPosition(position)
						.toString();

				Log.d(TAG, "SELECTED ITEM : " + select_item);
				if (select_item.equals("Code 128")) {
					item_value = 20;
				} else if (select_item.equals("PDF 417")) {
					item_value = 55;
				} else if (select_item.equals("Data Matrix")) {
					item_value = 71;
				} else {
					// showToast("Default item Selected");
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {

			}
		});

		enterText = (EditText) findViewById(R.id.entermessage);
		printBarcode = (Button) findViewById(R.id.printBarcode);

		printBarcode.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showToast(vusb.printBarcode(UsbSerialDevice.outEndpoint,UsbSerialDevice.inEndpoint,item_value, enterText
								.getText().toString()));
			}

		});

	}

	public void showToast(final String toast) {

		Builder alert = new Builder(Barcode.this);
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
