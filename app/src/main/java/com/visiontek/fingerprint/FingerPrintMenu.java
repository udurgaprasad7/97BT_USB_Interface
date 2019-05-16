package com.visiontek.fingerprint;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
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

public class FingerPrintMenu extends Activity implements OnClickListener {

    private static final String TAG = "BTFINGERPRINT";

    private Button enroll;
    private Button verify;
    private Button savedenroll;
    private Button fingerBMPImage;
    private Button fingerRAWImage;
    private Button generate_Licence;
    private Button enrollc;

    private String save_temp;
    private String temp_name;
    private String fp_image;

    private String callBackMsg = null;

    VisiontekUSB vusb = new VisiontekUSB();

    private static FingerPrintMenu parent;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fingerprint);

        enroll = (Button) findViewById(R.id.enrollButton);
        verify = (Button) findViewById(R.id.verifyButton);
        savedenroll = (Button) findViewById(R.id.savedButton);
        fingerBMPImage = (Button) findViewById(R.id.fingerBMPImage);
        fingerRAWImage = (Button) findViewById(R.id.fingerRAWImage);
        generate_Licence = (Button) findViewById(R.id.licence);

        enrollc = (Button) findViewById(R.id.enrollc);

        enroll.setOnClickListener(this);
        verify.setOnClickListener(this);
        savedenroll.setOnClickListener(this);
        fingerBMPImage.setOnClickListener(this);
        fingerRAWImage.setOnClickListener(this);
        enrollc.setOnClickListener(this);
        generate_Licence.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showToastFingerPrint(vusb.generate_Licence(UsbSerialDevice.outEndpoint, UsbSerialDevice.inEndpoint));

            }
        });

    }

    @Override
    public void onClick(View v) {
        if (v.equals(enroll)) {

            Builder alert = new Builder(FingerPrintMenu.this,
                    AlertDialog.THEME_HOLO_DARK);
            alert.setTitle("97BT");
            alert.setMessage("Enter File Name To Save Template");
            final EditText finger_name = new EditText(getApplicationContext());
            alert.setView(finger_name);
            alert.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            temp_name = finger_name.getText().toString();
                            if (temp_name.equals("")) {

                                showToastFingerPrint("Please Enter Template Name");

                            } else {

                                showToastFingerPrint(vusb.fingerprintEnroll(
                                        UsbSerialDevice.outEndpoint, UsbSerialDevice.inEndpoint,
                                        new File("/sdcard/" + temp_name
                                                + ".iso")));

                            }
                        }
                    });

            alert.show();

        }

        if (v.equals(verify)) {
            // String fp =
            // "RIYBsPKE8QHTbIHJAfeqgrgB96JDNwKFloTIAsxlgmwC8CyGvwODYEcqA53mg28DwMuCqQPUNoIBA+7ARBED7tOA6gTSxYQrBPvdh3YFR2+E+wV/ZEEsBcHKQpQF5d+EWQX+ZUZ4BgNySBkGCHpHGwaCeoGmBoxqRCYGl+1FLgahcIKUBqtmRk8HAXyEFgcLaEGCBy9sQ9oHTWxFCgdTeEKkB2JoRs4HYn6DVQe4bUcQB/CChPYH/3xEOggefIMOCChyReoIb4JC1gh5ekh0CKwMQlIIxYRDkgjkfIXGCPOHQtwJB4xFKQlPAoQWCZaGROcJpfQ=";
            // // System.out.println("fpbase : " + fpbase);
            // byte[] decode = Base64.decode(fp, Base64.DEFAULT);
            //
            // try {
            // FileOutputStream d = new FileOutputStream(new File(
            // "/sdcard/decode-"));
            // try {
            //
            // d.write(decode);
            // d.flush();
            //
            // } catch (IOException e) {
            // e.printStackTrace();
            // }
            // } catch (FileNotFoundException e) {
            //
            // e.printStackTrace();
            // }
            // System.out.println("decode : " + new String(decode).toString());

            showToastFingerPrint(vusb.fingerprintVerify(
                    UsbSerialDevice.outEndpoint, UsbSerialDevice.inEndpoint));

        }

        if (v.equals(savedenroll)) {

            Builder alert = new Builder(FingerPrintMenu.this,
                    AlertDialog.THEME_HOLO_DARK);
            alert.setTitle("97BT");
            alert.setMessage("Enter File Name To Enroll Template");
            final EditText finger_name = new EditText(getApplicationContext());
            alert.setView(finger_name);
            alert.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            save_temp = finger_name.getText().toString();
                            if (save_temp.equals("")) {

                                showToastFingerPrint("Please Enter Template Name");

                            } else {

                                showToastFingerPrint(vusb
                                        .fingerprintSaveTemplate(
                                                UsbSerialDevice.outEndpoint, UsbSerialDevice.inEndpoint,
                                                new File("/sdcard/" + save_temp
                                                        + ".iso")));

                            }
                        }
                    });

            alert.show();

        }

        if (v.equals(fingerBMPImage)) {
            Builder alert = new Builder(FingerPrintMenu.this,
                    AlertDialog.THEME_HOLO_DARK);
            alert.setTitle("97BT");
            alert.setMessage("Enter File Name To Save BMP Finger Image");
            final EditText finger_name = new EditText(getApplicationContext());
            alert.setView(finger_name);
            alert.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            fp_image = finger_name.getText().toString();
                            if (fp_image.equals("")) {

                                showToastFingerPrint("Please Enter Image Name");

                            } else {
                                showToastFingerPrint(vusb.fingerprintBMPImage(
                                        UsbSerialDevice.outEndpoint, UsbSerialDevice.inEndpoint, new File(
                                                "/sdcard/bmp1.bmp")));
                            }
                        }
                    });

            alert.show();
        }

        if (v.equals(fingerRAWImage)) {
            Builder alert = new Builder(FingerPrintMenu.this,
                    AlertDialog.THEME_HOLO_DARK);
            alert.setTitle("97BT");
            alert.setMessage("Enter File Name To Save RAW Finger Image");
            final EditText finger_name = new EditText(getApplicationContext());
            alert.setView(finger_name);
            alert.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            fp_image = finger_name.getText().toString();
                            if (fp_image.equals("")) {

                                showToastFingerPrint("Please Enter Image Name");

                            } else {
                                showToastFingerPrint(vusb.fingerprintRAWImage(
                                        UsbSerialDevice.outEndpoint, UsbSerialDevice.inEndpoint, new File(
                                                "/sdcard/raw")));

                            }
                        }
                    });

            alert.show();
        }

        if (v.equals(enrollc)) {
            // i = 0;
            Intent intent = new Intent(FingerPrintMenu.this,
                    MultipleEnrolls.class);
            startActivity(intent);

        }

    }

    public static final byte[] intToByteArray(int value) {

        byte[] integerBytes = new byte[2];

        integerBytes[0] = (byte) ((value >> 8) & 0xFF);
        integerBytes[1] = (byte) (value & 0xFF);
        return integerBytes;

    }

    public void showToastFingerPrint(final String toast) {

        Builder alert = new Builder(FingerPrintMenu.this);
        alert.setTitle("BT FINGERPRINT");

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
