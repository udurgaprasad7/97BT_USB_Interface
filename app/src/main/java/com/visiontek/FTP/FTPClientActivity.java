package com.visiontek.FTP;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.channels.FileChannel;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.net.ftp.FTPClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StatFs;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a97bt_usb_application.R;
import com.example.com.example.usbserial.UsbSerialDevice;
import com.example.mylibrary.VisiontekUSB;


public class FTPClientActivity extends Activity implements OnClickListener {
	VisiontekUSB vusb = new VisiontekUSB();
	// BluetoothActivity bta=new BluetoothActivity();
	private static final String TAG = "MainActivity";
	private static final String TEMP_FILENAME = "TAGtest.txt";
	private Context cntx = null;

	private String filename = "MySampleFile";
	private String filepath = "MyFileStorage";
	File myInternalFile;

	FTPClient ftpClient = new FTPClient();

	private MyFTPClientFunctions ftpclient = null;

	private Button btnLoginFtp, btnUploadFile, btnDownloadFile, btnDisconnect,
			btnExit, btnUpdate, cust_App;
	private EditText edtHostName, edtUserName, edtPassword;
	private ProgressDialog pd;

	private String[] fileList;

	private Handler handler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			System.out.println();

			if (pd != null && pd.isShowing()) {
				pd.dismiss();
			}
			if (msg.what == 0) {
				getFTPFileList();
			} else if (msg.what == 1) {
				showCustomDialog(fileList);
			} else if (msg.what == 2) {
				System.out.println("In 2nd case");
				Toast.makeText(FTPClientActivity.this,
						"Downloaded Successfully!", Toast.LENGTH_LONG).show();
				listFilesAndFolders("/data/user/0/com.visiontek.app.btactivity/app_MyFileStorage");
			} else if (msg.what == 3) {
				Toast.makeText(FTPClientActivity.this,
						"Disconnected Successfully!", Toast.LENGTH_LONG).show();
			} else if (msg.what == 4) {
				System.out.println("in 4 th case");
				Toast.makeText(getApplicationContext(),
						"File sent successfully", Toast.LENGTH_LONG).show();
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				showToast("Installing RDS & Restarting Device.....");
			} else if (msg.what == 5) {
				Toast.makeText(getApplicationContext(), "File sent fail",
						Toast.LENGTH_LONG).show();
			} else if (msg.what == 6) {
				showToast("Device Disconnected");

			} else {
				Toast.makeText(FTPClientActivity.this,
						"Unable to Perform Action!", Toast.LENGTH_LONG).show();
			}

		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		cntx = this.getBaseContext();

		edtHostName = (EditText) findViewById(R.id.edtHostName);
		edtUserName = (EditText) findViewById(R.id.edtUserName);
		edtPassword = (EditText) findViewById(R.id.edtPassword);

		btnLoginFtp = (Button) findViewById(R.id.btnLoginFtp);

		btnUploadFile = (Button) findViewById(R.id.btnUploadFile);
		btnDownloadFile = (Button) findViewById(R.id.btndownloadFile);
		btnDisconnect = (Button) findViewById(R.id.btnDisconnectFtp);
		btnExit = (Button) findViewById(R.id.btnExit);
		btnUpdate = (Button) findViewById(R.id.btnUpdatetoDevice);
		cust_App = (Button) findViewById(R.id.cust_app);

		btnLoginFtp.setOnClickListener(this);
		btnUploadFile.setOnClickListener(this);
		btnDownloadFile.setOnClickListener(this);
		btnDisconnect.setOnClickListener(this);
		btnExit.setOnClickListener(this);
		btnUpdate.setOnClickListener(this);
		cust_App.setOnClickListener(this);

		// Create a temporary file. You can use this to upload
		// createDummyFile();

		ftpclient = new MyFTPClientFunctions();
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnLoginFtp:
			if (isOnline(FTPClientActivity.this)) {
				connectToFTPAddress();
			} else {
				Toast.makeText(FTPClientActivity.this,
						"Please check your internet connection!",
						Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.btnUploadFile:
			pd = ProgressDialog.show(FTPClientActivity.this, "",
					"Uploading...", true, false);
			new Thread(new Runnable() {
				public void run() {
					boolean status = false;
					status = ftpclient.ftpUpload(
							Environment.getExternalStorageDirectory()
									+ "/TAGFtp/" + TEMP_FILENAME,
							TEMP_FILENAME, "/Apex Test jig/", cntx);
					if (status == true) {
						Log.d(TAG, "Upload success");
						handler.sendEmptyMessage(2);
					} else {
						Log.d(TAG, "Upload failed");
						handler.sendEmptyMessage(-1);
					}
				}
			}).start();
			break;

		case R.id.btndownloadFile:
			pd = ProgressDialog.show(FTPClientActivity.this, "",
					"Downloading...", true, false);
			new Thread(new Runnable() {
				public void run() {
					try {
						boolean status = false;
						String remoteFile1 = "RDS_Installer-Delta.zip";
						filename = remoteFile1;
						ContextWrapper contextWrapper = new ContextWrapper(
								getApplicationContext());
						File directory = contextWrapper.getDir(filepath,
								Context.MODE_PRIVATE);

						myInternalFile = new File(directory, filename);

						// File downloadFile1 = new File(getExternalFilesDir());

						OutputStream outputStream1 = new BufferedOutputStream(
								new FileOutputStream(myInternalFile));

						status = ftpclient.ftpDownload(remoteFile1,
								outputStream1);
						if (status == true) {
							Log.d(TAG, "Download success");

							handler.sendEmptyMessage(2);
						} else {
							Log.d(TAG, "Download failed");
							handler.sendEmptyMessage(-1);
						}

					} catch (FileNotFoundException fne) {
						System.out.println("file not found");

					} catch (IOException ioe) {
						System.out.println("IO exception found");

					}
				}
			}).start();
			break;

		case R.id.btnDisconnectFtp:
			pd = ProgressDialog.show(FTPClientActivity.this, "",
					"Disconnecting...", true, false);

			new Thread(new Runnable() {
				public void run() {
					ftpclient.ftpDisconnect();
					handler.sendEmptyMessage(3);
				}
			}).start();

			break;
		case R.id.btnExit:
			this.finish();
			break;
		case R.id.btnUpdatetoDevice:
			pd = ProgressDialog.show(FTPClientActivity.this, "",
					"File Updating...", true, false);
			new Thread(new Runnable() {
				public void run() {
								
				
//					String status = upDataFileinDevice("/data/user/0/com.visiontek.app.btactivity/app_MyFileStorage/RDS_Installer-Delta.zip");
					String status=upDataFileinDevice("/data/user/0/com.visiontek.app.btactivity/app_MyFileStorage/97BT-CUSTOMER-APP-DYN-1632-4.0.IMG");
					// System.out.println("status value : "+status);
					try {

						if (status.equals("File_Received")) {

							handler.sendEmptyMessage(4);

						} else if (status.equals("File_not_Received")) {
							handler.sendEmptyMessage(5);

						} else if (status.equals("BROKEN PIPE ERROR")) {
							handler.sendEmptyMessage(6);
							// showToast(status);

						}
					} catch (NullPointerException npe) {

					}
				}
			}).start();
		case R.id.cust_app:
			pd = ProgressDialog.show(FTPClientActivity.this, "",
					"Downloading...", true, false);
			new Thread(new Runnable() {
				public void run() {
					try {
						boolean status = false;
						String remoteFile1 = "97BT-CUSTOMER-APP-DYN-1632-4.0.IMG";
						filename = remoteFile1;
						ContextWrapper contextWrapper = new ContextWrapper(
								getApplicationContext());
						File directory = contextWrapper.getDir(filepath,
								Context.MODE_PRIVATE);

						myInternalFile = new File(directory, filename);

						// File downloadFile1 = new File(getExternalFilesDir());

						OutputStream outputStream1 = new BufferedOutputStream(
								new FileOutputStream(myInternalFile));

						status = ftpclient.ftpDownload(remoteFile1,
								outputStream1);
						System.out.println("status:" + status);
						if (status == true) {
							Log.d(TAG, "Download success");
							String custmerApp = "/data/user/0/com.visiontek.app.btactivity/app_MyFileStorage/97BT-CUSTOMER-APP-DYN-1632-4.0.IMG";
							File cust_Img_File = new File(custmerApp);

							FileInputStream fileInputStream = null;

							byte[] fileData = new byte[(int) cust_Img_File
									.length()];
							try {
								fileInputStream = new FileInputStream(
										cust_Img_File);
								try {
									fileInputStream.read(fileData);
									System.out.println("cust_img size : "+fileData.length);
									byte checksum = checkSum(fileData);
									System.out.println("check :" + checksum);
								} catch (IOException e) {
									e.printStackTrace();
								}
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							}

							handler.sendEmptyMessage(2);
						} else {
							Log.d(TAG, "Download failed");
							handler.sendEmptyMessage(-1);
						}

					} catch (FileNotFoundException fne) {
						System.out.println("file not found");

					} catch (IOException ioe) {
						System.out.println("IO exception found");

					}
				}
			}).start();

			break;
		}

	}

	private void connectToFTPAddress() {

		final String host = edtHostName.getText().toString().trim();
		final String username = edtUserName.getText().toString().trim();
		final String password = edtPassword.getText().toString().trim();

		if (host.length() < 1) {
			Toast.makeText(FTPClientActivity.this,
					"Please Enter Host Address!", Toast.LENGTH_LONG).show();
		} else if (username.length() < 1) {
			Toast.makeText(FTPClientActivity.this, "Please Enter User Name!",
					Toast.LENGTH_LONG).show();
		} else if (password.length() < 1) {
			Toast.makeText(FTPClientActivity.this, "Please Enter Password!",
					Toast.LENGTH_LONG).show();
		} else {

			pd = ProgressDialog.show(FTPClientActivity.this, "",
					"Connecting...", true, false);

			new Thread(new Runnable() {
				public void run() {
					boolean status = false;
					status = ftpclient.ftpConnect(host, username, password, 21);
					if (status == true) {
						Log.d(TAG, "Connection Success");
						handler.sendEmptyMessage(0);
					} else {
						Log.d(TAG, "Connection failed");
						handler.sendEmptyMessage(-1);
					}
				}
			}).start();
		}
	}

	private void getFTPFileList() {
		pd = ProgressDialog.show(FTPClientActivity.this, "",
				"Getting Files...", true, false);

		new Thread(new Runnable() {

			@Override
			public void run() {
				fileList = ftpclient.ftpPrintFilesList("/");
				handler.sendEmptyMessage(1);
			}
		}).start();
	}

	public void createDummyFile() {

		try {
			File root = new File(Environment.getExternalStorageDirectory(),
					"TAGFtp");
			if (!root.exists()) {
				root.mkdirs();
			}
			File gpxfile = new File(root, TEMP_FILENAME);
			FileWriter writer = new FileWriter(gpxfile);
			writer.append("Hi this is a sample file to upload for android FTP client example from TheAppGuruz!");
			writer.flush();
			writer.close();
			Toast.makeText(this, "Saved : " + gpxfile.getAbsolutePath(),
					Toast.LENGTH_LONG).show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean isOnline(Context context) {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnected()) {
			return true;
		}
		return false;
	}

	private void showCustomDialog(String[] fileList) {
		// custom dialog
		final Dialog dialog = new Dialog(FTPClientActivity.this);
		dialog.setContentView(R.layout.custom);
		dialog.setTitle("/ Directory File List");

		TextView tvHeading = (TextView) dialog.findViewById(R.id.tvListHeading);
		tvHeading.setText(":: File List ::");

		if (fileList != null && fileList.length > 0) {

			Toast.makeText(getApplicationContext(), "FTP Conncted",
					Toast.LENGTH_LONG).show();
			ListView listView = (ListView) dialog
					.findViewById(R.id.lstItemList);

			ArrayAdapter<String> fileListAdapter = new ArrayAdapter<String>(
					this, android.R.layout.simple_list_item_1, fileList);

			listView.setAdapter(fileListAdapter);

		} else {
			tvHeading.setText(":: No Files ::");
		}

		Button dialogButton = (Button) dialog.findViewById(R.id.btnOK);
		// if button is clicked, close the custom dialog
		dialogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		// dialog.show();
	}

	public String internalstoragePath() {

		File path = Environment.getDataDirectory();
		System.out.println("internal path :  " + path);
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return Formatter.formatFileSize(this, availableBlocks * blockSize);
	}

	public void listFilesAndFolders(String directoryName) {
		System.out.println("method was called");
		File directory = new File(directoryName);
		// get all the files from a directory
		File[] fList = directory.listFiles();
		for (File file : fList) {

			System.out.println("File Names are : " + file.getName() + "\n");
		}
	}

	public String upDataFileinDevice(String filePathFromMobile) {
//		String zipFile = filePathFromMobile;
//		String unzipLocation = Environment.getExternalStorageDirectory()
//				+ "/unzipped/";
//
//		Decompress d = new Decompress(zipFile, unzipLocation);
//		d.unzip();
//
//		File storedFile = new File(unzipLocation + "/RDS_Installer-Delta");
		
		File storedFile=new File(Environment.getExternalStorageDirectory()+"/97BT-CUSTOMER-APP-DYN-1632-4.0.IMG");
		
	

		String status = vusb.updateFileinDevice(UsbSerialDevice.outEndpoint,UsbSerialDevice.inEndpoint, storedFile, "97BT-CUSTOMER-APP-DYN-1632-4.0.IMG");

		return status;
	}

	public void showToast(final String toast) {

		AlertDialog.Builder alert = new AlertDialog.Builder(
				FTPClientActivity.this);
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

	private static final byte checkSum(byte[] bytes) {
		byte sum = 0;
		for (byte b : bytes) {
			sum ^= b;
		}
		return sum;
	}

}
