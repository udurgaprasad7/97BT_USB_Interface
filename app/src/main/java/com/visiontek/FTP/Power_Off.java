package com.visiontek.FTP;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.example.a97bt_usb_application.R;
import com.example.com.example.usbserial.UsbSerialDevice;
import com.example.mylibrary.VisiontekUSB;


public class Power_Off extends Activity {
	private Button power_off, reboot, enable_Bash;

	VisiontekUSB bt = new VisiontekUSB();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rebootmenu);
		power_off = (Button) findViewById(R.id.poweroff);
		reboot = (Button) findViewById(R.id.reboot);
		enable_Bash = (Button) findViewById(R.id.bash);
		//
		// AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// // Get the layout inflater
		// LayoutInflater inflater = getLayoutInflater();
		// // Inflate and set the layout for the dialog
		// // Pass null as the parent view because its going in the dialog
		// // layout
		// builder.setView(inflater.inflate(R.layout.rebootmenu, null));
		// AlertDialog ad = builder.create();
		// ad.setTitle("BT MENU");
		// ad.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel",
		// new DialogInterface.OnClickListener() {
		// public void onClick(DialogInterface dialog, int which) {
		// }
		// });
		// ad.show();
		reboot.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				bt.deviceReboot(UsbSerialDevice.outEndpoint,UsbSerialDevice.inEndpoint);
				finish();

			}

		});
		power_off.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				bt.devicePower_Off(UsbSerialDevice.outEndpoint,UsbSerialDevice.inEndpoint);
				finish();

			}
		});
		enable_Bash.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showToastError(bt.enablingBash(UsbSerialDevice.outEndpoint,UsbSerialDevice.inEndpoint));


			}
		});

	}

	public void showToastError(final String toast) {

		Builder alert = new Builder(Power_Off.this);
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
