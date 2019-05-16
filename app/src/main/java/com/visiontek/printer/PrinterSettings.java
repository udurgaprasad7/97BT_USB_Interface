package com.visiontek.printer;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.a97bt_usb_application.R;
import com.example.com.example.usbserial.UsbSerialDevice;
import com.example.mylibrary.VisiontekUSB;



/*
 *
 * @author Sreekanth <sreekanth.reddy@visiontek.co.in>
 *
 */

public class PrinterSettings extends Activity implements OnClickListener {

    private static final String TAG = "BTPRINTERSETTINGS";

    private Button fontSettings;
    private Button alignSettings;
    private Button langSettings;
    private Spinner alignFont, langSelect;
    private int align_Value = 0;
    private int lang_Value;
    public static String lang_unicode = "";
    VisiontekUSB vusb = new VisiontekUSB();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.printersettings);

        fontSettings = (Button) findViewById(R.id.fontset);
        alignSettings = (Button) findViewById(R.id.alignset);
        langSettings = (Button) findViewById(R.id.langset);

        fontSettings.setOnClickListener(this);
        alignSettings.setOnClickListener(this);
        langSettings.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        if (v.equals(fontSettings)) {
            Intent intnt = new Intent(PrinterSettings.this, FontSettings.class);
            startActivity(intnt);
        }
        if (v.equals(alignSettings)) {

            Builder builder = new Builder(
                    PrinterSettings.this);
            TextView title = new TextView(this);
            title.setText("select Alignment");
            title.setPadding(10, 10, 10, 10);
            title.setTextColor(Color.rgb(0, 153, 204));
            title.setTextSize(23);
            builder.setCustomTitle(title);

            LayoutInflater inflater = (LayoutInflater) getApplicationContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout_spinners = inflater.inflate(R.layout.spinner_align,
                    null);
            alignFont = (Spinner) layout_spinners.findViewById(R.id.alignset);

            builder.setView(layout_spinners);
            builder.setCancelable(false);

            ArrayList<String> lista_k = new ArrayList<String>();
            lista_k.add("Left Align");
            lista_k.add("Center Align");
            lista_k.add("Right Align");

            ArrayAdapter<String> alignarray = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, lista_k);
            alignarray
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            alignFont.setAdapter(alignarray);

            alignFont
                    .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent,
                                                   View view, int position, long id) {

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

            builder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            align_Value = alignFont.getSelectedItemPosition();

                            Log.d(TAG, "THE ALIGN VALUE : " + align_Value);

                            showToast(vusb.setFontAlign(UsbSerialDevice.outEndpoint, UsbSerialDevice.inEndpoint, align_Value));

                        }
                    });

            builder.show();

        }

        if (v.equals(langSettings)) {

            Builder builder = new Builder(this);
            TextView title = new TextView(this);
            title.setText("select Language");
            title.setPadding(10, 10, 10, 10);
            title.setTextColor(Color.rgb(0, 145, 204));
            title.setTextSize(23);
            builder.setCustomTitle(title);

            LayoutInflater inflater = (LayoutInflater) getApplicationContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout_spinners = inflater
                    .inflate(R.layout.spinner_lang, null);
            langSelect = (Spinner) layout_spinners.findViewById(R.id.langset);

            builder.setView(layout_spinners);
            builder.setCancelable(false);

            ArrayList<String> listlang = new ArrayList<String>();
            listlang.add("Devanagari");
            listlang.add("Telugu");
            listlang.add("Tamil");
            listlang.add("Bengali");
            listlang.add("Gujarathi");

            ArrayAdapter<String> langarray = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, listlang);
            langarray
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The
            // drop
            // down
            // view
            langSelect.setAdapter(langarray);

            langSelect
                    .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent,
                                                   View view, int position, long id) {

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

            builder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            lang_Value = 1 + langSelect
                                    .getSelectedItemPosition();

                            Log.d(TAG, "THE LANGUAGE VALUE : " + lang_Value);

                            if (lang_Value == 1) {
                                lang_unicode = "devanagari.hex";

                            } else if (lang_Value == 2) {
                                lang_unicode = "telugu.hex";

                            } else if (lang_Value == 3) {
                                lang_unicode = "tamil.hex";

                            } else if (lang_Value == 4) {
                                lang_unicode = "bengali.hex";

                            } else if (lang_Value == 5) {
                                lang_unicode = "gujarathi.hex";

                            } else {

                                if (!((Activity) PrinterSettings.this)
                                        .isFinishing()) {
                                    showToast("Default Language Selected");
                                }

                            }
                            Log.d(TAG, "THE LANGUAGE UNICODE : " + lang_unicode);
							showToast(vusb.setLanguage(UsbSerialDevice.outEndpoint,UsbSerialDevice.inEndpoint,lang_Value));

                        }
                    });

            builder.show();

        }
    }

    public void showToast(final String toast) {

        Builder alert = new Builder(PrinterSettings.this);
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
