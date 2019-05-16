package com.visiontek.printer;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.example.a97bt_usb_application.R;
import com.example.com.example.usbserial.UsbSerialDevice;
import com.example.mylibrary.VisiontekUSB;


public class MultiFontText_Printing extends Activity {
	private EditText enterFirstData;
	private EditText enterSecondData;
	private Button print_Multi_Font;
	private String data1;
	private String data2;
	private int datalen1;
	private int datalen2;
	private String totalData;
	VisiontekUSB vusb=new VisiontekUSB();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.text_printing);
		enterFirstData = (EditText) findViewById(R.id.edttxt1);
		enterSecondData = (EditText) findViewById(R.id.edttxt2);
		print_Multi_Font = (Button) findViewById(R.id.prn_Multi_Font);
		print_Multi_Font.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				data1 = enterFirstData.getText().toString();
				datalen1 = data1.length();
				System.out.println("First data length : " + datalen1);
				data2 = enterSecondData.getText().toString();
				datalen2 = data2.length();
				System.out.println("Second data length : " + datalen2);
				totalData = data1 + data2;
				System.out.println("Total data : " + totalData);
				vusb.printMultiFontData(UsbSerialDevice.outEndpoint,UsbSerialDevice.inEndpoint, datalen1, datalen2, totalData);

			}
		});
	}

}
