package com.visiontek.printer;

import java.io.File;
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

import com.example.a97bt_usb_application.R;
import com.example.com.example.usbserial.UsbSerialDevice;
import com.example.mylibrary.VisiontekUSB;


public class Select_Text_Format extends Activity {
	public Spinner font_Selection1;
	public Spinner font_Selection2;
	public Spinner font_Style;
	public Spinner size1;
	public Spinner size2;
	public Button set_Font;
	public int font1, font2, fontstyle, font_Size1, font_Size2;
	VisiontekUSB vusb=new VisiontekUSB();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select_text_format);
		addItemsOnSpinner1();
		addItemsOnSpinner2();
		addItemOnSpinner3();
		addItemOnSpinner4();
		addItemOnSpinner5();
		addListenerOnButton();

	}

	public void addItemsOnSpinner1() {
		font_Selection1 = (Spinner) findViewById(R.id.fontselection1);

		List<String> font_Selection_list1 = new ArrayList<String>();
		font_Selection_list1.add("Rockwell");
		font_Selection_list1.add("Arial");
		font_Selection_list1.add("Calibri");
		font_Selection_list1.add("Trebuchet_MS");

		ArrayAdapter<String> tempdataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, font_Selection_list1);
		tempdataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		font_Selection1.setAdapter(tempdataAdapter);
	}

	public void addItemsOnSpinner2() {
		font_Selection2 = (Spinner) findViewById(R.id.fontselection2);

		List<String> font_Selection_list2 = new ArrayList<String>();
		font_Selection_list2.add("Rockwell");
		font_Selection_list2.add("Arial");
		font_Selection_list2.add("Calibri");
		font_Selection_list2.add("Trebuchet_MS");

		ArrayAdapter<String> tempdataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, font_Selection_list2);
		tempdataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		font_Selection2.setAdapter(tempdataAdapter);
	}

	public void addItemOnSpinner3() {
		font_Style = (Spinner) findViewById(R.id.fontstyle);
		List<String> font_Style_List = new ArrayList<String>();
		font_Style_List.add("Regular");
		font_Style_List.add("Bold");
		ArrayAdapter<String> tempdataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, font_Style_List);
		tempdataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		font_Style.setAdapter(tempdataAdapter);

	}

	private void addItemOnSpinner4() {
		size1 = (Spinner) findViewById(R.id.fontsize1);
		List<String> size_List1 = new ArrayList<String>();
		size_List1.add("Small");
		size_List1.add("Medium");
		size_List1.add("Large");
		ArrayAdapter<String> tempdataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, size_List1);
		tempdataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		size1.setAdapter(tempdataAdapter);

	}

	private void addItemOnSpinner5() {
		size2 = (Spinner) findViewById(R.id.fontsize2);
		List<String> size_List2 = new ArrayList<String>();
		size_List2.add("Small");
		size_List2.add("Medium");
		size_List2.add("Large");
		ArrayAdapter<String> tempdataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, size_List2);
		tempdataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		size2.setAdapter(tempdataAdapter);

	}

	public void addListenerOnButton() {

		font_Selection1 = (Spinner) findViewById(R.id.fontselection1);
		font_Selection2 = (Spinner) findViewById(R.id.fontselection2);
		font_Style = (Spinner) findViewById(R.id.fontstyle);
		size1 = (Spinner) findViewById(R.id.fontsize1);
		size2 = (Spinner) findViewById(R.id.fontsize2);
		set_Font = (Button) findViewById(R.id.set);
		set_Font.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				font1 = 1 + font_Selection1.getSelectedItemPosition();
				font2 = 1 + font_Selection2.getSelectedItemPosition();
				fontstyle = 1 + font_Style.getSelectedItemPosition();
				font_Size1 = 1 + size1.getSelectedItemPosition();
				font_Size2 = 1 + size2.getSelectedItemPosition();
				Log.d("sss", "THE SELECTED font 1 : " + font1 + " font 2 : "
						+ font2 + "font style :" + fontstyle + "fontSize 1 : "
						+ font_Size1 + "font size 2 : " + font_Size2);
				showToast(vusb.multiFont_Setting(UsbSerialDevice.outEndpoint, UsbSerialDevice.inEndpoint, font1, font2, fontstyle,
						font_Size1, font_Size2));

			}

		});
	}

	public void showToast(final String toast) {

		Builder alert = new Builder(Select_Text_Format.this);
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
