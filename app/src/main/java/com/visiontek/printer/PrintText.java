package com.visiontek.printer;

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
import android.widget.Toast;

import com.example.a97bt_usb_application.MainActivity;
import com.example.a97bt_usb_application.R;
import com.example.a97bt_usb_application.UsbService;
import com.example.com.example.usbserial.UsbSerialDevice;
import com.example.mylibrary.VisiontekUSB;

import java.util.Arrays;

import static com.visiontek.printer.PrinterMenu.mUsbService;

/*
 *
 * @author Sreekanth <sreekanth.reddy@visiontek.co.in>
 *
 */

public class PrintText extends Activity {

    private static final String TAG = "BTPRINTTEXT";

    private Button printing, printDirectly;
    private EditText enterMsg;
    private String msg;


    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.printtext);
        enterMsg = (EditText) findViewById(R.id.entermessage);
        printing = (Button) findViewById(R.id.printingButton);
        printDirectly = (Button) findViewById(R.id.printDirectly);
        printing.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                msg = enterMsg.getText().toString();

                VisiontekUSB vusb = new VisiontekUSB();
                MainActivity ma = new MainActivity();

                String text = "Visiontek is embedded systems based company with " + "\n"
                        + "25 years of Experience" + "\n\n\n";

                if (msg.equals("")) {
                    if (PrinterMenu.mUsbService != null) {
                        Log.d(TAG, "Edit text : " + text.getBytes());
//						PrinterMenu.mUsbService.write(text.getBytes());
                       // String data = new String(msg);
                        System.out.println("OutEndPoint : " + UsbSerialDevice.outEndpoint + "," + "InEndPoint : " + UsbSerialDevice.inEndpoint);
                        System.out.println("Serial port : " + UsbService.serialPort);
                        showToast(vusb.printText(text, UsbSerialDevice.outEndpoint, UsbSerialDevice.inEndpoint));


                    } else {
                        Log.d(TAG, "usb service null");
                    }
                } else {
                    Log.d(TAG, "Edit text : " + msg.getBytes());
                    if (PrinterMenu.mUsbService != null) {
                        showToast(vusb.printText(msg, UsbSerialDevice.outEndpoint, UsbSerialDevice.inEndpoint));
                    } else {
                        Toast.makeText(getApplicationContext(),"USB device Disconnected",Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "usb service null");
                    }
                }
            }

        });
        printDirectly.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                msg = enterMsg.getText().toString();
                if (msg.equals("")) {
                    String defaultTxt = "<<< VIZZCO TOPUP VOUCHER >>> \n"
                            + "DUPLICATE\n"
                            + "TXN Date : 12/12/2018" + "\n"
                            + "Terminal Id : 665544" + "\n"
                            + "Distributor ID : ethiolinki" + "\n"
                            + "Retailer : paratet" + "\n"
                            + "Txn ID : 0181212000000038" + "\n"
                            + "-------------------------------\n"
                            + "pin No : 5000 0000 0000 0075" + "\n"
                            + "------------------------------------\n"
                            + "Pin value : BIRR 50" + "\n"
                            + "Serial NO : 60000000000075" + "\n"
                            + "Use before : 12/12/2022" + "\n"

                            + "      Please Visit us Again.    " + "\n"
                            + "                     Thank you\n\n\n"
                            + "   <<< www.vizzco.com >>> \n\n\n\n";

                    /*
                     * "At VISIONTEK we provide extraordinary opportunities for growth. Exposure to various functions within the organization provides unmatched value towards the learning of an individualHuman Resource plays a significant role in contributing and facilitating the achievement of the strategic business goals of the company and HR at Visiontek takes great responsibility in strengthening the organization capabilities, building a dynamic, vibrant and meritocratic culture and instilling accountability in all the employees."
                     * +
                     * "At VISIONTEK we provide extraordinary opportunities for growth. Exposure to various functions within the organization provides unmatched value towards the learning of an individualHuman Resource plays a significant role in contributing and facilitating the achievement of the strategic business goals of the company and HR at Visiontek takes great responsibility in strengthening the organization capabilities, building a dynamic, vibrant and meritocratic culture and instilling accountability in all the employees."
                     * + "\n" +
                     * "At VISIONTEK we provide extraordinary opportunities for growth. Exposure to various functions within the organization provides unmatched value towards the learning of an individualHuman Resource plays a significant role in contributing and facilitating the achievement of the strategic business goals of the company and HR at Visiontek takes great responsibility in strengthening the organization capabilities, building a dynamic, vibrant and meritocratic culture and instilling accountability in all the employees."
                     * +
                     * "At VISIONTEK we provide extraordinary opportunities for growth. Exposure to various functions within the organization provides unmatched value towards the learning of an individualHuman Resource plays a significant role in contributing and facilitating the achievement of the strategic business goals of the company and HR at Visiontek takes great responsibility in strengthening the organization capabilities, building a dynamic, vibrant and meritocratic culture and instilling accountability in all the employees."
                     *
                     * +
                     * "At VISIONTEK we provide extraordinary opportunities for growth. Exposure to various functions within the organization provides unmatched value towards the learning of an individualHuman Resource plays a significant role in contributing and facilitating the achievement of the strategic business goals of the company and HR at Visiontek takes great responsibility in strengthening the organization capabilities, building a dynamic, vibrant and meritocratic culture and instilling accountability in all the employees."
                     * +
                     * "At VISIONTEK we provide extraordinary opportunities for growth. Exposure to various functions within the organization provides unmatched value towards the learning of an individualHuman Resource plays a significant role in contributing and facilitating the achievement of the strategic business goals of the company and HR at Visiontek takes great responsibility in strengthening the organization capabilities, building a dynamic, vibrant and meritocratic culture and instilling accountability in all the employees."
                     * + "\n" +
                     * "At VISIONTEK we provide extraordinary opportunities for growth. Exposure to various functions within the organization provides unmatched value towards the learning of an individualHuman Resource plays a significant role in contributing and facilitating the achievement of the strategic business goals of the company and HR at Visiontek takes great responsibility in strengthening the organization capabilities, building a dynamic, vibrant and meritocratic culture and instilling accountability in all the employees."
                     * +
                     * "At VISIONTEK we provide extraordinary opportunities for growth. Exposure to various functions within the organization provides unmatched value towards the learning of an individualHuman Resource plays a significant role in contributing and facilitating the achievement of the strategic business goals of the company and HR at Visiontek takes great responsibility in strengthening the organization capabilities, building a dynamic, vibrant and meritocratic culture and instilling accountability in all the employees."
                     *
                     * + "\n";
                     */
                    System.out.println("lenght : " + defaultTxt.length());
                    IntentPrint(defaultTxt);

                } else {
                    System.out.println("DATA : " + msg);

                    System.out.println("DEVICE CONNECTED");
                    IntentPrint(msg);

                    System.out.println("Completed");

                }

            }
        });

    }

    public void showToast(final String toast) {

        Builder alert = new Builder(PrintText.this);
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

    public void IntentPrint(String txtvalue) {
        byte[] buffer = txtvalue.getBytes();

        String value = null;
        byte[] nextLine = {'\n'};

        try {

            for (int i = 0; i <= buffer.length - 1; i++) {
                PrinterMenu.mUsbService.write(txtvalue.getBytes());

            }

//			BTConnector.mOutputStream.write(nextLine);
//			BTConnector.mOutputStream.flush();


        } catch (Exception ex) {
            value += ex.toString() + "\n" + "Excep IntentPrint \n";

        }
    }

}