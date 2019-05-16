package com.visiontek.IFD;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.example.a97bt_usb_application.R;
import com.example.com.example.usbserial.UsbSerialDevice;
import com.example.mylibrary.IFDCallback;
import com.example.mylibrary.VisiontekUSB;



/*
 * 
 * @author Sreekanth <sreekanth.reddy@visiontek.co.in>
 *
 */

public class IFDOperations extends Activity implements OnClickListener,
		IFDCallback {

	private static final String TAG = "BTIFD";

	private boolean value;
	private Button write;
	private Button read;
	private Button number;
	private Button format;

	private byte b;
	private static int count = 0;
	private static Context mContext;
	private static String text;

	VisiontekUSB vusb = new VisiontekUSB();

	private byte cmd[][] = {
			{ 0x00, 0x20, 0x00, 0x00, 0x05, 0x55, 0x55, 0x55, 0x55, 0x55 },// verify
			{ 0x00, (byte) 0xDA, 0x00, 0x00 },// write
			{ 0x00, 0x21, 0x00, 0x00, 0x05, 0x55, 0x55, 0x55, 0x55, 0x55 },// change

			// password
			{ 0x00, (byte) 0xDC, 0x00, 0x01, 0x05, 0x05, 0x04, 0x03, 0x02, 0x01 },// update/
			{ 0x00, (byte) 0xCA, 0x00, 0x01, 0x42 },// read
			{ 0x00, 0x22, 0x00, 0x00, 0x02 },// read records
			{ 0x00, (byte) 0x9C, 0x00, 0x00, 0x00 },// format
			{ 0x00, (byte) 0xA4, 0x00, 0x00, 0x02, 0x3f } };// LECT FILE

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ifd_operations);
		write = (Button) findViewById(R.id.writeButton);
		read = (Button) findViewById(R.id.readButton);
		number = (Button) findViewById(R.id.numButton);
		format = (Button) findViewById(R.id.formatButton);
		write.setOnClickListener(this);
		read.setOnClickListener(this);
		number.setOnClickListener(this);
		format.setOnClickListener(this);
		mContext = this;
		vusb.registerIFDCallback(IFDOperations.this);
	}

	@Override
	public void onClick(View v) {

		if (v.equals(write)) {

			count = 1;
			Builder alert = new Builder(IFDOperations.this,
					AlertDialog.THEME_HOLO_DARK);
			alert.setTitle("97BT");
			alert.setMessage("Enter The Text To Write Into Card");
			final EditText input = new EditText(getApplicationContext());

			alert.setView(input);
			alert.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

							dialog.dismiss();

							text = input.getText().toString();

							if (text.equals("")) {

								showToast("Please Enter Data");

							} else {
								String check = vusb.ifdCardCommandSend(
										UsbSerialDevice.outEndpoint,UsbSerialDevice.inEndpoint, 10, cmd);

								if (check.equals("OPERATION SUCCESS")) {
									System.out.println("check : "+check);
									try {
										Thread.sleep(1000);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									showToast(vusb.ifdWriteRecord(
											UsbSerialDevice.outEndpoint,UsbSerialDevice.inEndpoint, 4, cmd,
											text));
								} else {
									showToast(check);
								}

							}

						}
					});

			alert.show();
		}
		if (v.equals(read)) {

			count = 2;

			Builder alert = new Builder(IFDOperations.this,
					AlertDialog.THEME_HOLO_DARK);
			alert.setTitle("97BT");
			alert.setMessage("Enter Record Number");
			final EditText input = new EditText(getApplicationContext());
	
				input.setInputType(InputType.TYPE_CLASS_NUMBER
						| InputType.TYPE_NUMBER_FLAG_DECIMAL);
				alert.setView(input);
				alert.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {

								dialog.dismiss();
								try {
									text = input.getText().toString();

									showToast(vusb.ifdReadRecord(
											UsbSerialDevice.outEndpoint,UsbSerialDevice.inEndpoint, 5, cmd,
											Integer.parseInt(text)));
								} catch (NumberFormatException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

							}
						});
			
			alert.show();

		}
		if (v.equals(number)) {

			showToast(vusb.ifdNumberOfRecords(UsbSerialDevice.outEndpoint,UsbSerialDevice.inEndpoint, 5, cmd));

		}
		if (v.equals(format)) {

			count = 4;

			showToast(vusb.ifdFormatCards(UsbSerialDevice.outEndpoint,UsbSerialDevice.inEndpoint, 5, cmd));
		}

	}

	public void showToast(final String toast) {

		Builder alert = new Builder(IFDOperations.this);
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
