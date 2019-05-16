package com.visiontek.printer;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.example.a97bt_usb_application.R;
import com.example.com.example.usbserial.UsbSerialDevice;
import com.example.mylibrary.VisiontekUSB;


public class PrintBill extends Activity implements OnClickListener {

	private static final String TAG = "BTPRINTBILL";

	private Button PrintBill;
	private Button AddData;
	private Button PrintData;
	private Button FontStyle;
	private Button PreBill;
	private EditText data;

	private String msg;
	private String bill;
	VisiontekUSB vusb=new VisiontekUSB();

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.print_bill);
		PrintBill = (Button) findViewById(R.id.print_bill);
		AddData = (Button) findViewById(R.id.add_data);
		PrintData = (Button) findViewById(R.id.print_data);
		FontStyle = (Button) findViewById(R.id.font);
		PreBill = (Button) findViewById(R.id.pre_bill);
		data = (EditText) findViewById(R.id.data);

		PrintBill.setOnClickListener(this);
		AddData.setOnClickListener(this);
		PrintData.setOnClickListener(this);
		FontStyle.setOnClickListener(this);
		PreBill.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {

		if (v.equals(PrintBill)) {
			Builder alert = new Builder(PrintBill.this,
					AlertDialog.THEME_HOLO_DARK);
			alert.setTitle("97BT");
			alert.setMessage("Enter Bill Name: ");
			final EditText file_name = new EditText(getApplicationContext());
			alert.setView(file_name);
			alert.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							bill = file_name.getText().toString();
							if (bill.equals("")) {

								showToast("Please Enter File Name");

							} else {

								showToast(vusb.printBillFile(
										UsbSerialDevice.outEndpoint,UsbSerialDevice.inEndpoint,new File(
												"/sdcard/bill")));

							}
						}
					});

			alert.show();

		}
		if (v.equals(AddData)) {

			msg = data.getText().toString();

			showToast(vusb.addDataToPrint(UsbSerialDevice.outEndpoint,UsbSerialDevice.inEndpoint, TextFont.valueType,
					TextFont.valueStyle, TextFont.valueSize,
					TextFont.valueAlign, msg));

		}
		if (v.equals(PrintData)) {
			showToast(vusb.printAddData(UsbSerialDevice.outEndpoint,UsbSerialDevice.inEndpoint));

		}
		if (v.equals(PreBill)) {



			String pre_bill = "0000000000144532090403855~33~4~1~1~1~2\n"
					+

					"---------------Sample Copy-----------------~41~4~1~1~0~2\n"
					+

					"                SAMPLE BILL~27~4~2~1~0~1\n"
					+

					"           Regd Office: 24 Park~30~6~1~1~1~2\n"
					+ "          Street, Kolkata-700016~32~6~1~1~1~2\n"
					+ "        Phone: 033-44017200/350~31~6~1~1~1~2\n"
					+

					"Date:15-09-2015             Time: 15:20:26~42~6~1~1~1~2\n"
					+

					"------------------------------------------~41~4~1~1~1~2\n"
					+ "MR No: 03855C20150000000149~27~4~1~1~0~2\n"
					+ "Proposal No: PG/0011/C/08/000498~32~4~1~1~0~2\n"
					+ "Div: VISIONTEK LTD~18~4~1~1~0~2\n"
					+ "Customer: Android~17~4~1~1~0~2\n"
					+ "Received From:Linux~19~4~1~1~0~2\n"
					+ "Relation: SELF~14~4~1~1~0~2\n"
					+ "Remarks:~8~4~1~1~0~2\n"
					+ "Payment Type: CASH~18~4~1~1~0~2\n"
					+ "The sum of Rs: 200.00~21~4~1~1~0~2\n"
					+ "   TWO HUNDRED RUPEES ONLY~26~4~1~1~0~2\n"
					+ "On account of~13~4~1~1~0~2\n"
					+ "INSTALLMENT                        Rs 0.00~42~4~1~1~0~2\n"
					+ "OD Intt.                           Rs 0.00~42~4~1~1~0~2\n"
					+ "CBP                                Rs 0.00~42~4~1~1~0~2\n"
					+ "OTHERS                           Rs 200.00~42~4~1~1~0~2\n"
					+

					"------------------------------------------~41~4~1~1~1~2\n"
					+

					"TOTAL                            Rs 200.00~42~4~1~1~0~2\n"
					+

					"Signature of Collector:~23~4~1~1~0~2\n"
					+ "[Android ,Z00005]~17~4~1~1~0~2\n"
					+

					"Signature of Depositor:~23~4~1~1~0~2\n"
					+ "[Linux]~7~4~1~1~0~2\n"
					+ "------------------------------------------~41~4~1~1~1~2\n"
					+ "\n";

			showToast(vusb.printBillString(UsbSerialDevice.outEndpoint,UsbSerialDevice.inEndpoint, pre_bill));

			vusb.printerLineFeed(UsbSerialDevice.outEndpoint,UsbSerialDevice.inEndpoint, 5);

		}
		if (v.equals(FontStyle)) {
			Intent intnt = new Intent(PrintBill.this, TextFont.class);
			startActivity(intnt);
		}

	}

	public void showToast(final String toast) {

		Builder alert = new Builder(PrintBill.this);
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
