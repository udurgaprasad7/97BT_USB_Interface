package com.example.a97bt_usb_application;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.example.com.example.usbserial.UsbSerialDevice;
import com.example.mylibrary.VisiontekUSB;

public class Rd_Service extends Activity {
	// private Button wifiScan;
	private Button rd_status;
	private Button Rd_Apis;
	private Button mobile_Mac_ID, product_vendor_ID;

	Object instance = null;
	Method setTetheringOn = null;
	Method isTetheringOn = null;
	Object mutex = new Object();
	boolean tethering_Status = false;
	String macAddress;
	VisiontekUSB bt = new VisiontekUSB();

	public static final String SECURE_SETTINGS_BLUETOOTH_ADDRESS = "bluetooth_address";
	private static Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.rd_settings);
		// wifiScan = (Button) findViewById(R.id.scanwifi);
		rd_status = (Button) findViewById(R.id.rd_status);
		Rd_Apis = (Button) findViewById(R.id.rd_service);
		mobile_Mac_ID = (Button) findViewById(R.id.getmacid);
		product_vendor_ID = (Button) findViewById(R.id.productid);

		// wifiScan.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// Intent gotoWifiList = new Intent(Rd_Service.this,
		// Wifi_Activity.class);
		// startActivity(gotoWifiList);
		// }
		// });
		rd_status.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showToastDialog(bt.rd_Status_Info(UsbSerialDevice.outEndpoint,UsbSerialDevice.inEndpoint));

			}
		});

		Rd_Apis.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent gotoRdApis = new Intent(Rd_Service.this,
						Rd_Activity.class);
				startActivity(gotoRdApis);

			}
		});
		mobile_Mac_ID.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				teth();
				// getBluetoothMacAddress();

				//
				// String sClassName = "android.bluetooth.BluetoothPan";
				//
				// try {
				//
				// Class<?> classBluetoothPan = Class.forName(sClassName);
				//
				// Constructor<?> ctor = classBluetoothPan
				// .getDeclaredConstructor(Context.class,
				// BluetoothProfile.ServiceListener.class);
				// ctor.setAccessible(true);
				// // Set Tethering ON
				// Class[] paramSet = new Class[1];
				// paramSet[0] = boolean.class;
				//
				// synchronized (mutex) {
				// setTetheringOn = classBluetoothPan.getDeclaredMethod(
				// "setBluetoothTethering", paramSet);
				// isTetheringOn = classBluetoothPan.getDeclaredMethod(
				// "isTetheringOn", null);
				// = ctor.newInstance(getApplicationContext(),
				// new BTPanServiceListener(
				// getApplicationContext()));
				// }
				// } catch (ClassNotFoundException e) {
				// e.printStackTrace();
				// } catch (Exception e) {
				// e.printStackTrace();
				// }
				// Rd_Service.context = getApplicationContext();
				//
				// BluetoothManager ba = (BluetoothManager)
				// getSystemService(Context.BLUETOOTH_SERVICE);
				//
				// // String mac = ba.getAdapter().getAddress();
				//
				// macAddress = android.provider.Settings.Secure.getString(
				// context.getContentResolver(), "bluetooth_address");
				// System.out.println("Local Mac address  : " + macAddress);
				//
			}
		});
		findViewById(R.id.getmacofmobile).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {

						showToastDialog(bt.blutooth_MacID(UsbSerialDevice.outEndpoint,UsbSerialDevice.inEndpoint));
					}
				});
		product_vendor_ID.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showToastDialog(bt.product_Vendor_ID(UsbSerialDevice.outEndpoint,UsbSerialDevice.inEndpoint));
			}
		});
	}

	public void showToastDialog(final String toast) {

		Builder alert = new Builder(Rd_Service.this);
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

	// Tethering-OFF
	private void tethering_Off() {
		if (new Rd_Service().tethering_Status) {

			try {
				BluetoothProfile proxy = null;
				proxy.getClass()
						.getMethod("setBluetoothTethering",
								new Class[] { Boolean.TYPE })
						.invoke(proxy, new Object[] { Boolean.valueOf(false) });
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Toast.makeText(getApplicationContext(),
					"Turning bluetooth tethering off", Toast.LENGTH_SHORT)
					.show();
		}
	}

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
							showToastDialog(bt.mobile_Mac_ID(
									UsbSerialDevice.outEndpoint,UsbSerialDevice.inEndpoint, macAddress));
							Toast.makeText(getApplicationContext(), macAddress,
									Toast.LENGTH_LONG).show();
							tethering_Status = true;
							Toast.makeText(getApplicationContext(),
									"BT Tethering is on", Toast.LENGTH_LONG)
									.show();
						} else {
							showToastDialog("Check Internet Connection");
						}

					} else {
						Toast.makeText(getApplicationContext(),
								"BT Tethering is off", Toast.LENGTH_LONG)
								.show();
					}
				}
			} catch (InvocationTargetException e) {
				System.out.println("invocate");
				e.printStackTrace();
			} catch (IllegalAccessException e) {
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
		String bluetoothMacAddress = "";
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
		Rd_Service.context = getApplicationContext();

		BluetoothManager ba = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

		// String mac = ba.getAdapter().getAddress();

		// macAddress = android.provider.Settings.Secure.getString(
		// context.getContentResolver(), "bluetooth_address");
		// System.out.println("Local Mac address  : " + macAddress);

	}

	// protected void makeRequest() {
	// ActivityCompat.requestPermissions(this,
	// new String[]{Manifest.permission.RECORD_AUDIO},
	// RECORD_REQUEST_CODE);
	// }

}
