package com.example.a97bt_usb_application;

import android.accessibilityservice.GestureDescription;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.com.example.usbserial.UsbSerialDevice;
import com.example.mylibrary.VisiontekUSB;
import com.visiontek.FTP.FTPClientActivity;
import com.visiontek.FTP.Power_Off;
import com.visiontek.IFD.IFDMenu;
import com.visiontek.RFID.RFIDActivity;
import com.visiontek.fingerprint.FingerPrintMenu;
import com.visiontek.printer.PrinterMenu;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;


public class MainActivity extends Activity {

    private Button mPrinter;
    private Button mIFD;
    private Button mFingerprint;
    private Button mRfid;
    private Button mInfo;
    private Button mLoad;
    private Button rd_Settings;
    private Button get_USB_Data;
    private Button mPin;
    private Button about;
    private ProgressDialog mmConnectingDlg;
    private Button reboot;
    private Button checkBatteryStatus;
    private Button rhms_Update;
    private Button check_Updates;
    private Button config_97BT;
    private ProgressDialog mProgressDlg;
    private ProgressDialog mConnectingDlg;
    private String TAG = "BTACT";
    private static Context context;
    UsbService us = new UsbService();
    Object instance = null;
    Method setTetheringOn = null;
    Method isTetheringOn = null;
    Object mutex = new Object();
    boolean tethering_Status = false;
    String bluetoothMacAddress = "";
    VisiontekUSB vusb = new VisiontekUSB();

    /*
     * Notifications from UsbService will be received here.
     */

    final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED

                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();

                    //showConnected();

                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
//                      showDisonnected();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
//                        showDisonnected();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
//                        showDisonnected();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
//                        showDisonnected();
                    break;
            }
        }
    };

    public UsbService usbService;
    private TextView display;
    private EditText editText;
    private CheckBox box9600, box38400;
    private MyHandler mHandler;
    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bt_activity);
        context = this;

        mPrinter = (Button) findViewById(R.id.btn_printer);
        mIFD = (Button) findViewById(R.id.btn_ifd);
        mFingerprint = (Button) findViewById(R.id.btn_fingerprint);
        mRfid = (Button) findViewById(R.id.btn_rfid);
        mInfo = (Button) findViewById(R.id.btn_info);
        mLoad = (Button) findViewById(R.id.btn_appload);
        rd_Settings = (Button) findViewById(R.id.rd);
        get_USB_Data = (Button) findViewById(R.id.otg_data);
        mPin = (Button) findViewById(R.id.btn_pin);
        check_Updates = (Button) findViewById(R.id.checkupdates);
        about = (Button) findViewById(R.id.about);
        reboot = (Button) findViewById(R.id.reboot);
        checkBatteryStatus = (Button) findViewById(R.id.checkbatterystatus);
        rhms_Update = (Button) findViewById(R.id.rhmsupdate);
        config_97BT = (Button) findViewById(R.id.config97bt);


        mPrinter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PrinterMenu.mUsbService = usbService;
                VisiontekUSB vusb = new VisiontekUSB();

                System.out.println("usbcommunication value : " + vusb.usbCommunication);

                String data = "Toast.makeText(getApplicationContext(),\"usbservice not null\",Toast.LENGTH_SHORT).show()\n\n\n";


                Intent i = new Intent(MainActivity.this, PrinterMenu.class);
                startActivity(i);
            }
        });
        mFingerprint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intnt = new Intent(MainActivity.this,
                        FingerPrintMenu.class);
                startActivity(intnt);
            }
        });
        mRfid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intnt = new Intent(MainActivity.this,
                        RFIDActivity.class);
                startActivity(intnt);
            }
        });

        mIFD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intnt = new Intent(MainActivity.this, IFDMenu.class);
                startActivity(intnt);
            }
        });
        mInfo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                showToastError(vusb.devInfo(UsbSerialDevice.outEndpoint, UsbSerialDevice.inEndpoint));

            }
        });
        rd_Settings.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (!mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.enable();


                }
//                try {
//                    Thread.sleep(2000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
                teth();
                try {

                    String macId = vusb.ble_teth_in_USB_interface(UsbSerialDevice.outEndpoint, UsbSerialDevice.inEndpoint, bluetoothMacAddress);//                BluetoothActivity bta = new BluetoothActivity();
                }catch(NullPointerException npe){
                    Toast.makeText(getApplicationContext(),"Device not connected ",Toast.LENGTH_SHORT).show();
                }
//
//                bta.mmDevice= mBluetoothAdapter.getRemoteDevice(macId);
//                try {
//                    bta.openBT(bta.mmDevice);
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

                Intent intent = new Intent(MainActivity.this,
                        Rd_Service.class);


                startActivity(intent);

            }
        });

        mLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this,
                        AlertDialog.THEME_HOLO_DARK);
                alert.setTitle("97BT App Loading");

                alert.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();

                                showToastError(vusb.appLoad(
                                        UsbSerialDevice.outEndpoint, UsbSerialDevice.inEndpoint));
                            }

                        });
                alert.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                dialog.dismiss();
                            }
                        });

                alert.show();

            }
        });

        get_USB_Data.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                showToastError(vusb.get_USB_Data(UsbSerialDevice.outEndpoint, UsbSerialDevice.inEndpoint, 6));

            }
        });

        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                PackageInfo packageInfo = null;
                try {
                    packageInfo = getPackageManager().getPackageInfo(
                            getPackageName(), 0);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                int versionNumber = packageInfo.versionCode;

                String appName = "97BT Android Application";
                String versionName = packageInfo.versionName;
                String versionCode = Integer.toString(versionNumber);
                String deviceName = android.os.Build.MODEL;
                String brandName = android.os.Build.BRAND;
                String osVersion = android.os.Build.VERSION.RELEASE;
                String sdkVersion = Integer
                        .toString(android.os.Build.VERSION.SDK_INT);
                String androidName = null;

                Field[] fields = Build.VERSION_CODES.class.getFields();
                for (Field field : fields) {
                    String fieldName = field.getName();
                    int fieldValue = -1;

                    try {
                        fieldValue = field.getInt(new Object());
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                    if (fieldValue == Build.VERSION.SDK_INT) {
                        androidName = fieldName;
                    }
                }
                String aboutApp = "App Name : "
                        + appName
                        + "\n\n"
                        + "App Version Name : "
                        + versionName
                        + "\n\n"
                        + "App Version Code : "
                        + versionCode
                        + "\n\n"
                        + "Device Name : "
                        + deviceName
                        + "\n\n"
                        + "Brand Name : "
                        + brandName
                        + "\n\n"
                        + "OS Version : "
                        + osVersion
                        + "\n\n"
                        + "SDK Version : "
                        + sdkVersion
                        + "\n\n"
                        + "Android OS Name : "
                        + androidName
                        + "\n\n"
                        + "Developed By : "
                        + "Sr33"
                        + "\n\n"
                        + "Device Info : "
                        + "\n\n"
                        + vusb.devInfo(UsbSerialDevice.outEndpoint, UsbSerialDevice.inEndpoint);
                showToastError(aboutApp);
            }
        });
        reboot.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Power_Off.class);
                startActivity(intent);

            }
        });
        checkBatteryStatus.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String Total_Status = vusb
                        .battery_Status_Info(UsbSerialDevice.outEndpoint, UsbSerialDevice.inEndpoint);
                System.out.println("Total status : " + Total_Status);
                try {
                    if (Total_Status.equals("Battery not present")) {
                        showToastError(Total_Status);

                    } else if (Total_Status
                            .contains("Adaptor Not Connected")) {
                        String adapterStatus = Total_Status
                                .substring(0, 21);
                        String batteryStatus = Total_Status.substring(22);
                        System.out.println("------" + batteryStatus);
                        // if(batteryStatus.equals("%")){
                        // batteryStatus=Total_Status.substring(22);
                        // System.out.println("------"+batteryStatus);
                        // }
                        String DisplayStatus = "Battery % = "
                                + batteryStatus + "\n" + adapterStatus;
                        showToastError(DisplayStatus);

                    } else {
                        String adapterStatus = Total_Status
                                .substring(0, 17);
                        String batteryStatus = Total_Status.substring(18);
                        String DisplayStatus = "Battery % = "
                                + batteryStatus + "\n" + adapterStatus;
                        showToastError(DisplayStatus);
                    }
                } catch (Exception e) {

                }

            }
        });
        rhms_Update.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                mmConnectingDlg = new ProgressDialog(MainActivity.this);
                mmConnectingDlg.setMessage("RHMS Updating......");
                mmConnectingDlg.show();


                Thread startRhms = new Thread(new rhmsRunnable());
                startRhms.start();

            }
        });
        config_97BT.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // List of items to be show in alert Dialog are stored in
                // array of strings/char sequences final
                final String[] items = {"printer-9701",
                        "printer+fingerprint-9702",
                        "printer+fingerprint+IFD-9704",
                        "printer+fingerprint+IFD+RFID-9706"};

                AlertDialog.Builder builder = new AlertDialog.Builder(
                        MainActivity.this);

                // set the title for alert dialog
                builder.setTitle("Choose Configuration: ");

                // set items to alert dialog. i.e. our array , which will be
                // shown as list view in alert dialog
                builder.setItems(items,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int item) {
                                // setting the button text to the selected
                                // itenm from the list
                                String get_config_Details = items[item];
                                String config = get_config_Details
                                        .substring(get_config_Details
                                                .length() - 4);
                                System.out.println("---------------"
                                        + config);
                                showToastError(vusb.configuration_97BT(
                                        UsbSerialDevice.outEndpoint, UsbSerialDevice.inEndpoint, config));
                            }
                        });

                // Creating CANCEL button in alert dialog, to dismiss the
                // dialog box when nothing is selected
                builder.setCancelable(false).setNegativeButton("CANCEL",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                // When clicked on CANCEL button the dalog
                                // will be dismissed
                                dialog.dismiss();
                            }
                        });

                // Creating alert dialog
                AlertDialog alert = builder.create();

                // Showingalert dialog
                alert.show();

            }
        });

        check_Updates.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,
                        FTPClientActivity.class);
                startActivity(intent);

            }
        });

        mHandler = new MyHandler(this);
        if (usbService != null) {
            usbService.changeBaudRate(115200);
        }


//                    if (usbService != null) { // if UsbService was correctly binded, Send data
////                        Toast.makeText(getApplicationContext(),"usbservice not null",Toast.LENGTH_SHORT).show();
//                        usbService.write(data.getBytes());
//                    }


    }

    public class rhmsRunnable implements Runnable {

        @Override
        public void run() {
            String var = vusb.rhms_Update(UsbSerialDevice.outEndpoint, UsbSerialDevice.inEndpoint);
            mmConnectingDlg.dismiss();

        }

    }

    @Override
    public void onResume() {
        super.onResume();
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
    }

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */
    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;

        public MyHandler(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    String data = (String) msg.obj;
                    mActivity.get().display.append(data);
                    break;
                case UsbService.CTS_CHANGE:
                    Toast.makeText(mActivity.get(), "CTS_CHANGE", Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
                    Toast.makeText(mActivity.get(), "DSR_CHANGE", Toast.LENGTH_LONG).show();
                    break;
                case UsbService.SYNC_READ:
                    String buffer = (String) msg.obj;
                    mActivity.get().display.append(buffer);
                    break;
            }
        }
    }


    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT)
                .show();
    }

    public void showToastError(final String toast) {
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
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

    //Beloe APIs are tethering purpose
    public class BTPanServiceListener implements
            BluetoothProfile.ServiceListener {

        private final Context context;

        public BTPanServiceListener(final Context context) {
            this.context = context;
        }

        @Override
        public void onServiceConnected(final int profile,
                                       final BluetoothProfile proxy) {
            // Some code must be here or the compiler will optimize away this
            // callback.
            System.out.println("callback");

            try {

                synchronized (mutex) {


                    setTetheringOn.invoke(instance, true);

                    if ((Boolean) isTetheringOn.invoke(instance, null)) {
                        if (haveNetworkConnection()) {

                            tethering_Status = true;
                            Toast.makeText(getApplicationContext(),
                                    "BT Tethering is on", Toast.LENGTH_LONG)
                                    .show();
                        } else {
                            showToastError("Check Internet Connection");
                        }

                    } else {
                        Toast.makeText(getApplicationContext(),
                                "BT Tethering is off", Toast.LENGTH_LONG)
                                .show();
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(final int profile) {
        }
    }

    public String getBluetoothMacAddress() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter
                .getDefaultAdapter();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            try {
                Field mServiceField = bluetoothAdapter.getClass()
                        .getDeclaredField("mService");
                mServiceField.setAccessible(true);

                Object btManagerService = mServiceField.get(bluetoothAdapter);

                if (btManagerService != null) {
                    bluetoothMacAddress = (String) btManagerService.getClass()
                            .getMethod("getAddress").invoke(btManagerService);
                    System.out.println("mac address above kitkat : "
                            + bluetoothMacAddress);
                }
            } catch (NoSuchFieldException e) {
                System.out.println("filed not found");

            } catch (NoSuchMethodException e) {
                System.out.println("method not found");

            } catch (IllegalAccessException e) {
                System.out.println("illegal access");

            } catch (InvocationTargetException e) {

                System.out.println("target invoked failed");
            }
        } else {
            bluetoothMacAddress = bluetoothAdapter.getAddress();
            System.out.println("mac address below kitkat : "
                    + bluetoothMacAddress);

        }
        return bluetoothMacAddress;
    }

    public void teth() {
        String sClassName = "android.bluetooth.BluetoothPan";

        try {
            String macAddress;

            macAddress = getBluetoothMacAddress();
            System.out.println("mac address : " + macAddress);

            Class<?> classBluetoothPan = Class.forName(sClassName);

            Constructor<?> ctor = classBluetoothPan.getDeclaredConstructor(
                    Context.class, BluetoothProfile.ServiceListener.class);
            ctor.setAccessible(true);
            // Set Tethering ON
            Class[] paramSet = new Class[1];
            paramSet[0] = boolean.class;

            synchronized (mutex) {
                setTetheringOn = classBluetoothPan.getDeclaredMethod(
                        "setBluetoothTethering", paramSet);
                isTetheringOn = classBluetoothPan.getDeclaredMethod(
                        "isTetheringOn", null);
                instance = ctor.newInstance(getApplicationContext(),
                        new BTPanServiceListener(getApplicationContext()));
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        MainActivity.context = getApplicationContext();

        BluetoothManager ba = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        // String mac = ba.getAdapter().getAddress();

        // macAddress = android.provider.Settings.Secure.getString(
        // context.getContentResolver(), "bluetooth_address");
        // System.out.println("Local Mac address  : " + macAddress);

    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }
}
