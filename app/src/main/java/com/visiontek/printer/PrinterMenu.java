package com.visiontek.printer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.example.a97bt_usb_application.MainActivity;
import com.example.a97bt_usb_application.R;
import com.example.a97bt_usb_application.UsbService;
import com.example.com.example.usbserial.UsbSerialDevice;
import com.example.mylibrary.VisiontekUSB;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/*
 *
 * @author Sreekanth <sreekanth.reddy@visiontek.co.in>
 *
 */

public class PrinterMenu extends Activity implements OnClickListener {

    private static final String TAG = "BTPRINTERMENU";
    MainActivity ma = new MainActivity();
    private Button printtext;
    private EditText editText;
    public static UsbService mUsbService;
    private Button printImage;
    private Button printBarcode;
    private Button storeImage;
    private Button printSavedImage;
    private Button printerSettings;
    private Button multiLang, hexmultiLanuage;
    private String ackStatus;
    private String btMessage;
    private boolean LOOP_FLAG;
    private String operationAck;
    private byte[] packetBytes = new byte[2000];
    private int PACKETBYTES_LENGTH = 2000;
    VisiontekUSB vusb = new VisiontekUSB();
    private String BT_MESSAGE;

    private Button printerReset;
    private Button diagnoseTest;
    private Button samplePrint;
    private Button printerfeed;
    private Button multiText_print;
    private Button printBill;
    private String ImagenameInPhn;
    private String ImgNameInDevice;
    private String text_bmp;
    private String getFromEdittext;
    //    private String hex_file;
//    private boolean value;
    private static Context mContext;
    private static int count = 0;
    // printing multi-lanuage without hex files
//    private Button multiLanuage;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prnter_menu);
        // editText = (EditText) findViewById(R.id.text);
        printtext = (Button) findViewById(R.id.print_text);
        printImage = (Button) findViewById(R.id.print_image);
        printBarcode = (Button) findViewById(R.id.print_barcode);
        storeImage = (Button) findViewById(R.id.store_image);
        printSavedImage = (Button) findViewById(R.id.print_stored_image);
        printerSettings = (Button) findViewById(R.id.printersettings);
        multiLang = (Button) findViewById(R.id.multilanguage);
        hexmultiLanuage = (Button) findViewById(R.id.hexmultilang);
        printerReset = (Button) findViewById(R.id.printer_reset);
        diagnoseTest = (Button) findViewById(R.id.printer_diagnose);
        samplePrint = (Button) findViewById(R.id.sample_print);
        printerfeed = (Button) findViewById(R.id.printer_feed);
        multiText_print = (Button) findViewById(R.id.Multitext);
        printBill = (Button) findViewById(R.id.print_bill);

        printtext.setOnClickListener(this);
        printImage.setOnClickListener(this);
        printBarcode.setOnClickListener(this);
        storeImage.setOnClickListener(this);
        printSavedImage.setOnClickListener(this);
        printerSettings.setOnClickListener(this);
        multiLang.setOnClickListener(this);
        //multiLanuage.setOnClickListener(this);
        printerReset.setOnClickListener(this);
        diagnoseTest.setOnClickListener(this);
        samplePrint.setOnClickListener(this);
        printerfeed.setOnClickListener(this);
        multiText_print.setOnClickListener(this);
        printBill.setOnClickListener(this);

        mContext = this;
    }

    @SuppressLint("NewApi")
    @Override
    public void onClick(View v) {

        if (v.equals(printtext)) {

            count = 1;


            Intent intnt = new Intent(PrinterMenu.this, PrintText.class);
            startActivity(intnt);

        }


        if (v.equals(printImage)) {
            count = 2;

            Builder alert = new Builder(PrinterMenu.this,
                    AlertDialog.THEME_HOLO_DARK);
            alert.setTitle("97BT");
            alert.setMessage("Enter File Name To Print Image");
            final EditText file_name = new EditText(getApplicationContext());
            alert.setView(file_name);
            alert.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            text_bmp = file_name.getText().toString();
                            if (text_bmp.equals("")) {

                                showToast("Please Enter File Name");

                            } else {

                                File sdcard = Environment
                                        .getExternalStorageDirectory();

                                // File n = Environment
                                //
                                // .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

                                // System.out.println("bbb"+n.toString());

                                Log.d(TAG, "SDCARD PATH : " + sdcard);
                                String bmp_file = sdcard + "/" + text_bmp;
                                Log.d(TAG, "BMP FILE PATH : " + bmp_file);

                                File hwFile = new File(bmp_file);

                                // The Above code sample image file path
                                // It is placed in Sdcard
                                // u can give your own path

                                if (hwFile.exists()) {
                                    showToast(vusb.printDynamicImage(UsbSerialDevice.outEndpoint, UsbSerialDevice.inEndpoint,
                                            hwFile.getAbsoluteFile()));


                                } else {
                                    showToast("FILE NOT FOUND");
                                }
                            }

                        }
                    });

            alert.show();

        }
        if (v.equals(printBarcode)) {
            count = 3;

            Intent intnt = new Intent(PrinterMenu.this, Barcode.class);
            startActivity(intnt);

        }
        if (v.equals(storeImage)) {

            count = 4;

            LayoutInflater factory = LayoutInflater.from(this);

            final View textName = factory.inflate(R.layout.store_bmp, null);

            final EditText phoneName = (EditText) textName
                    .findViewById(R.id.mobilename);
            final EditText deviceName = (EditText) textName
                    .findViewById(R.id.devicename);

            final AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setIcon(R.drawable.ic_launcher_background)
                    .setTitle("Enter The File Names:")
                    .setView(textName)
                    .setPositiveButton("Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {

                                    Log.i("AlertDialog", "TextEntry 1 Entered "
                                            + phoneName.getText().toString());
                                    Log.i("AlertDialog", "TextEntry 2 Entered "
                                            + deviceName.getText().toString());

                                    ImagenameInPhn = phoneName.getText().toString();
                                    ImgNameInDevice = deviceName.getText().toString();

                                    if (ImagenameInPhn.equals("") && ImgNameInDevice.equals("")) {

                                        showToast("Please Enter File Names");

                                    } else {

                                        try {
                                            File sdcard = Environment
                                                    .getExternalStorageDirectory();

                                            Log.d(TAG, "SDCARD PATH : "
                                                    + sdcard);
                                            String bmp_file = sdcard + "/"
                                                    + ImagenameInPhn;
                                            Log.d(TAG, "BMP FILE PATH : "
                                                    + bmp_file);

                                            File hwFile = new File(bmp_file);
                                            if (hwFile.exists()) {

                                                Bitmap bitmap = BitmapFactory
                                                        .decodeFile(bmp_file);

                                                int width = bitmap.getWidth();
                                                int height = bitmap.getHeight();

                                                System.out.println("W N H"
                                                        + width + " " + height);

                                                String filenameArray[] = ImagenameInPhn
                                                        .split("\\.");
                                                String extension = filenameArray[filenameArray.length - 1];
                                                System.out.println("EXten : "
                                                        + extension);

                                                if (extension.equals("bmp")) {


                                                    showToast(vusb
                                                            .storeBMPToDeviceImageConvert(UsbSerialDevice.outEndpoint, UsbSerialDevice.inEndpoint,
                                                                    ImagenameInPhn,
                                                                    ImgNameInDevice));
                                                } else if (width == 384) {
                                                    showToast(vusb
                                                            .storeBMPToDevice(UsbSerialDevice.outEndpoint, UsbSerialDevice.inEndpoint,
                                                                    ImagenameInPhn,
                                                                    ImgNameInDevice));
                                                } else {

                                                    showToast(vusb
                                                            .storeBMPToDeviceImageScaling(UsbSerialDevice.outEndpoint, UsbSerialDevice.inEndpoint,
                                                                    ImagenameInPhn,
                                                                    ImgNameInDevice));
                                                }

                                            } else {
                                                showToast("FILE NOT FOUND");
                                            }
                                        } catch (Exception e) {
                                            // TODO Auto-generated catch block
                                            e.printStackTrace();
                                        }
                                    }

                                }
                            })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {

                                }
                            });
            alert.show();

        }
        if (v.equals(printSavedImage)) {
            count = 5;
            Builder alert = new Builder(PrinterMenu.this,
                    AlertDialog.THEME_HOLO_DARK);
            alert.setTitle("97BT");
            alert.setMessage("Enter File Name to Print Stored Image");
            final EditText input = new EditText(getApplicationContext());
            alert.setView(input);
            alert.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            dialog.dismiss();
                            getFromEdittext = input.getText().toString();

                            if (getFromEdittext.equals("")) {

                                showToast("Please Enter File Name");

                            } else {
//
                                showToast(vusb.printStoredBMP(
                                        UsbSerialDevice.outEndpoint, UsbSerialDevice.inEndpoint, getFromEdittext));
                            }

                        }
                    });

            alert.show();
        }

        if (v.equals(printerSettings)) {
            count = 6;

            Intent intnt = new Intent(PrinterMenu.this, PrinterSettings.class);
            startActivity(intnt);

        }

        if (v.equals(hexmultiLanuage)) {
            count = 7;

            File sdcard = Environment.getExternalStorageDirectory();

            String hexlang_file = sdcard + "/" + PrinterSettings.lang_unicode;

            showToast(vusb.printLanguage(UsbSerialDevice.outEndpoint, UsbSerialDevice.inEndpoint, new File(hexlang_file)));
        }
        if (v.equals(multiLang)) {
            Intent intent = new Intent(PrinterMenu.this,
                    Multi_Language_Printing.class);
            startActivity(intent);

        }
        if (v.equals(printerReset)) {
            count = 8;

            Builder alert = new Builder(PrinterMenu.this);
            alert.setTitle("97BT");
            alert.setMessage("Printer Resetting:");
            alert.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            showToast(vusb.printerReset(
                                    UsbSerialDevice.outEndpoint, UsbSerialDevice.inEndpoint));

                        }
                    });

            alert.show();

        }
        if (v.equals(diagnoseTest)) {
            count = 9;

            showToast(vusb.printerDiagnose(UsbSerialDevice.outEndpoint, UsbSerialDevice.inEndpoint));

        }
        if (v.equals(samplePrint)) {
            count = 9;

            showToast(vusb.btDeviceSamplePrint(UsbSerialDevice.outEndpoint, UsbSerialDevice.inEndpoint));

        }
        if (v.equals(printBill)) {

            Intent intnt = new Intent(PrinterMenu.this, PrintBill.class);
            startActivity(intnt);

        }

        if (v.equals(printerfeed)) {

            showToast(vusb.printerLineFeed(UsbSerialDevice.outEndpoint, UsbSerialDevice.inEndpoint, 1));

        }
        if (v.equals(multiText_print)) {
            Intent intent = new Intent(PrinterMenu.this,
                    Print_MultiFont_Settings.class);
            startActivity(intent);

        }

    }


    public void showToast(final String toast) {

        Builder alert = new Builder(PrinterMenu.this);
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
