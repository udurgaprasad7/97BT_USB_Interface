package com.visiontek.RFID;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.example.a97bt_usb_application.R;
import com.example.com.example.usbserial.UsbSerialDevice;
import com.example.mylibrary.VisiontekUSB;



/*
 * 
 * @author Sreekanth <sreekanth.reddy@visiontek.co.in>
 *
 */

public class RFIDActivity extends Activity implements OnClickListener {

	private static final String TAG = "BTPRINTTEXT";

	private Button rfidWrite;
	private Button rfidRead;
	private EditText enterMsg;

	private String msg;
	private boolean value;
	// private String data = "1431431431431431";
	private int rfidDataLen;

	private byte buffer[] = new byte[4096];
	private byte array[] = new byte[4092];
	private byte[] packetBytes = new byte[900];

	int Auth_block_addr = 7;
	byte key[] = { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff,
			(byte) 0xff, (byte) 0xff };
	int Write_block_addr = 4;
	int Read_block_addr = 4;
	int KEYA = 10;

	VisiontekUSB vusb = new VisiontekUSB();

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rfid_acticity);
		enterMsg = (EditText) findViewById(R.id.entermessage);

		rfidWrite = (Button) findViewById(R.id.rfid_write);
		rfidRead = (Button) findViewById(R.id.rfid_read);

		rfidWrite.setOnClickListener(this);
		rfidRead.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {

		if (v.equals(rfidWrite)) {

			Builder alert = new Builder(RFIDActivity.this,
					AlertDialog.THEME_HOLO_DARK);
			alert.setTitle("97BT RFID");
			alert.setMessage("Enter the RFID DATA");
			final EditText file_name = new EditText(getApplicationContext());
			alert.setView(file_name);
			alert.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							msg = file_name.getText().toString();
							if (msg.length()>= 1 && msg.length() <= 16) {

								showToast(vusb.rfidWrite(
										UsbSerialDevice.outEndpoint,UsbSerialDevice.inEndpoint, 1,
										Auth_block_addr, KEYA,
										Write_block_addr, key, msg));

							} else if (msg.length() > 16) {
								showToast("Datalimit Exceeded");

							} else  {
								showToast("Please Enter Data");
							}
						}
					});

			alert.show();

		}

		if (v.equals(rfidRead)) {

			showToast(vusb.rfidRead(UsbSerialDevice.outEndpoint,UsbSerialDevice.inEndpoint, 1, Auth_block_addr, KEYA,
					Read_block_addr, key));
		}

	}

	public void showToast(final String toast) {
		Builder alert = new Builder(RFIDActivity.this);
		alert.setTitle("BT RFID");
		alert.setMessage(toast);
		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				dialog.dismiss();
			}
		});
		alert.show();
	}

	public static String toHexString(byte[] ba) {
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < ba.length; i++)
			str.append(String.format("%x ", ba[i]));
		return str.toString();
	}
}