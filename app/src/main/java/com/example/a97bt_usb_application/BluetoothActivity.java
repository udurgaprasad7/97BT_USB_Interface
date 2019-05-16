package com.example.a97bt_usb_application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

public class BluetoothActivity {

	// android built in classes for bluetooth operations
	BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	BluetoothSocket mmSocket;
	BluetoothDevice mmDevice;
	public boolean conn_status = false;

	// needed for communication to bluetooth device / network
	static OutputStream mmOutputStream;
	static InputStream mmInputStream;
	Thread workerThread;

	byte[] readBuffer;
	int readBufferPosition;
	volatile boolean stopWorker;

	void sendData() throws IOException {

	}

	void beginListenForData() {
		try {
			final Handler handler = new Handler();

			// this is the ASCII code for a newline character
			final byte delimiter = 10;

			stopWorker = false;
			readBufferPosition = 0;
			readBuffer = new byte[1024];

			workerThread = new Thread(new Runnable() {
				public void run() {

					while (!Thread.currentThread().isInterrupted()
							&& !stopWorker) {

						try {

							int bytesAvailable = mmInputStream.available();

							if (bytesAvailable > 0) {

								byte[] packetBytes = new byte[bytesAvailable];
								mmInputStream.read(packetBytes);

								for (int i = 0; i < bytesAvailable; i++) {

									byte b = packetBytes[i];
									if (b == delimiter) {

										byte[] encodedBytes = new byte[readBufferPosition];
										System.arraycopy(readBuffer, 0,
												encodedBytes, 0,
												encodedBytes.length);

										// specify US-ASCII encoding
										final String data = new String(
												encodedBytes, "US-ASCII");
										readBufferPosition = 0;

										// tell the user data were sent to
										// bluetooth printer device
										handler.post(new Runnable() {
											public void run() {
												// myLabel.setText(data);
											}
										});

									} else {
										readBuffer[readBufferPosition++] = b;
									}
								}
							}

						} catch (IOException ex) {
							stopWorker = true;
						}

					}
				}
			});

			workerThread.start();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void openBT(BluetoothDevice device) throws IOException {
		try {
			BluetoothAdapter.getDefaultAdapter().enable();
			Thread.sleep(1000);

			// Standard SerialPortService ID
			UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
			mmSocket = device.createRfcommSocketToServiceRecord(uuid);
			System.out.println("before IoExce : " + mmSocket);
			if (mmSocket == null) {
				return;

			}

			mmSocket.connect();
			conn_status = true;

			mmOutputStream = mmSocket.getOutputStream();
			mmInputStream = mmSocket.getInputStream();

			// beginListenForData();

			// myLabel.setText("Bluetooth Opened");

		} catch (IOException e) {
			try {
				mmSocket = (BluetoothSocket) device
						.getClass()
						.getMethod("createRfcommSocket",
								new Class[] { int.class }).invoke(mmDevice, 1);
			} catch (IllegalAccessException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IllegalArgumentException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InvocationTargetException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (NoSuchMethodException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			System.out.println("before IoExce : " + mmSocket);
			mmSocket.connect();
			conn_status = true;

			mmOutputStream = mmSocket.getOutputStream();
			mmInputStream = mmSocket.getInputStream();

		} catch (InterruptedException ie) {

		}
	}

}
