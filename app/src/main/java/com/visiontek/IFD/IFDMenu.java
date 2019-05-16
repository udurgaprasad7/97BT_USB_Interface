package com.visiontek.IFD;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import com.example.a97bt_usb_application.R;
import com.example.com.example.usbserial.UsbSerialDevice;
import com.example.mylibrary.IFDCallback;
import com.example.mylibrary.VisiontekUSB;



/*
 * 
 * @author Sreekanth <sreekanth.reddy@visiontek.co.in>
 *
 */

public class IFDMenu extends Activity implements OnClickListener, IFDCallback {

	private Button ok;
	private int slot;
	private int opSelect;

	/**
	 * @param args
	 */
	private Spinner spinner1, spinner2;
	VisiontekUSB vusb = new VisiontekUSB();

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ifd_menu);

		vusb.registerIFDCallback(IFDMenu.this);

		ok = (Button) findViewById(R.id.ok);
		spinner1 = (Spinner) findViewById(R.id.operation);
		spinner2 = (Spinner) findViewById(R.id.cards);

		ok.setOnClickListener(this);

		List<String> list1 = new ArrayList<String>();
		list1.add("POWER DOWN");
		list1.add("POWER UP");
		list1.add("CARD SELECTION");

		List<String> list2 = new ArrayList<String>();
		list2.add("CARD 1(EXTERNAL)");
		list2.add("CARD 2(INTERNAL)");
		list2.add("CARD 3(SAM)");

		ArrayAdapter<String> dataAdapter1 = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, list1);

		dataAdapter1
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spinner1.setAdapter(dataAdapter1);

		ArrayAdapter<String> dataAdapter2 = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, list2);

		dataAdapter2
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spinner2.setAdapter(dataAdapter2);

		spinner1.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// Toast.makeText(
				// parent.getContext(),
				// "Operation Selected : \n"
				// + parent.getItemAtPosition(position).toString()
				// + " : " + position, Toast.LENGTH_LONG).show();
				opSelect = position;

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		spinner2.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// Toast.makeText(
				// parent.getContext(),
				// "Operation Selected : \n"
				// + parent.getItemAtPosition(position).toString()
				// + " : " + position, Toast.LENGTH_LONG).show();
				slot = 1 + position;

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

	}

	@Override
	public void onClick(View v) {
		if (v.equals(ok)) {

			System.out.println("IFD OPERATION : " + opSelect);
			System.out.println("IFD CARD : " + slot);

			String btAck = vusb.ifdCardSelection(UsbSerialDevice.outEndpoint,UsbSerialDevice.inEndpoint, slot, opSelect);

			if (btAck.equals("OPERATION SUCCESS")) {
				if (opSelect == 0) {
					showToast("CARD POWER DOWN SUCCESS");
				} else if (opSelect == 1) {
					Intent intnt = new Intent(IFDMenu.this, IFDOperations.class);
					startActivity(intnt);
				} else if (opSelect == 2) {
					Intent intnt = new Intent(IFDMenu.this, IFDOperations.class);
					startActivity(intnt);
				}
			} else if (btAck.equals("MAXIMUM LENGTH EXCEEDED")) {
				showToast("MAXIMUM LENGTH EXCEEDED");
			} else if (btAck.equals("LENGTH ERROR")) {
				showToast("LENGTH ERROR");
			} else if (btAck.equals("SCR OPEN ERROR")) {
				showToast("SCR OPEN ERROR");
			} else if (btAck.equals("CARD SELECTION FAILED")) {
				showToast("CARD SELECTION FAILED");
			} else if (btAck.equals("CARD NOT PRESENT")) {
				showToast("CARD NOT PRESENT");
			} else if (btAck.equals("IFD POWER UP FAILED")) {
				showToast("IFD POWER UP FAILED");
			} else if (btAck.equals("OPERATION FAILED")) {
				showToast("OPERATION FAILED");
			}
		}
	}

	public void showToast(final String toast) {

		Builder alert = new Builder(IFDMenu.this);
		alert.setTitle("BT IFD");

		alert.setMessage(toast);
		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				dialog.dismiss();

			}
		});
		alert.show();

	}

	@Override
	public void onIFDResponce(byte[] ifdResponce) {
		System.out.println("RESPONCE : " + new String(ifdResponce));
	}

	@Override
	public void onIFDATRResponce(byte[] ifdResponce) {
		System.out.println("ATR RESPONCE : " + new String(ifdResponce));

	}

}
