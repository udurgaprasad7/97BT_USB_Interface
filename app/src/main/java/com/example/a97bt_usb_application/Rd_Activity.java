package com.example.a97bt_usb_application;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;

import com.example.com.example.usbserial.UsbSerialDevice;
import com.example.mylibrary.VisiontekUSB;

public class Rd_Activity extends Activity {
	private Button Zlog, device_Ids, rdVersion_Info, replace_scanner;
	ProgressBar progressBar;
	private String actualResponse, finalResponse;
	VisiontekUSB bt = new VisiontekUSB();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rd_layout);
		Zlog = (Button) findViewById(R.id.Zlog);
		device_Ids = (Button) findViewById(R.id.deviceids);
		rdVersion_Info = (Button) findViewById(R.id.rdVersion_info);
		replace_scanner = (Button) findViewById(R.id.replace_HW);
		Zlog.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				showToast(bt.zLog(UsbSerialDevice.outEndpoint,UsbSerialDevice.inEndpoint));

			}
		});
		device_Ids.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				showToast(bt.getDeviceIDs(UsbSerialDevice.outEndpoint,UsbSerialDevice.inEndpoint));

			}
		});
		rdVersion_Info.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showToast(bt.RD_Version(UsbSerialDevice.outEndpoint,UsbSerialDevice.inEndpoint));

				// bt.SDK_Version(BTConnector.mOutputStream,
				// BTConnector.mInputStream);
			}
		});
		replace_scanner.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showToast(bt.scanner_Replaced(UsbSerialDevice.outEndpoint,UsbSerialDevice.inEndpoint));

			}
		});
	}

	public void rd_discovery(View v) {

		showToast(bt.rd_Discovery(UsbSerialDevice.outEndpoint,UsbSerialDevice.inEndpoint));

	}

	public void rd_fp_capture(View v) {

		progressBar = (ProgressBar) findViewById(R.id.progress_bar);

		String xml_Data = "<PidOptions ver=\"1.0\">"
				+ "<Opts fCount=\"1\" fType=\"0\" env=\"PP\" format=\"0\" pidVer=\"2.0\" "
				+ "timeout=\"10000\" otp=\"\" wadh=\"Qks7UygOsvuP4j+JtIJgHGZ5qksBAJo8Q9J5gKloQlo=\"/>"
				+ "<Demo></Demo><CustOpts></CustOpts><Bios></Bios></PidOptions>\n";
		// progressBar.setVisibility(View.VISIBLE);

		// Thread timer = new Thread() {
		// private int progressBarStatus = 0;
		//
		// public void run() {
		// try {
		// sleep(5000);
		// while (progressBarStatus < 5000) {
		// Rd_Activity.this.runOnUiThread(new Runnable() {
		// public void run() {
		// progressBar.setProgress(progressBarStatus);
		// progressBarStatus += 1000;
		// System.out.println("progress bar running....");
		//
		// }
		// });
		//
		// }
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// }
		//
		// };
		// timer.start();
		showToast(bt.rd_Fp_Capture(UsbSerialDevice.outEndpoint,UsbSerialDevice.inEndpoint, xml_Data));

		// progressBar.setVisibility(View.GONE);

	}

	public void rd_fp_deviceinfo(View v) {

		showToast(bt.rd_Fp_deviceInfo(UsbSerialDevice.outEndpoint,UsbSerialDevice.inEndpoint));

	}

	public void showToast(final String toast) {

		Builder alert = new Builder(Rd_Activity.this);
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
