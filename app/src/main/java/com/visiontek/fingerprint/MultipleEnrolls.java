package com.visiontek.fingerprint;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.a97bt_usb_application.R;
import com.example.com.example.usbserial.UsbSerialDevice;
import com.example.mylibrary.BTCallback;
import com.example.mylibrary.VisiontekUSB;


public class MultipleEnrolls extends Activity implements BTCallback {

	private Spinner templateTypes;

	private Spinner imageTypes;
	public static int i = 0;
	private Button enroll;
	private Button verifyFingerprint;
	private Button viewBMPIMAGE;
	private int tempType, imageType;
	private String TAG = "MULTIENROLLS";
	private String imageFilename = null;

	VisiontekUSB vusb = new VisiontekUSB();
	private TextView ts;
	private String callBackMsg = null;
	boolean flag = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.multipleenroll);

		addItemsOnSpinner1();
		addItemsOnSpinner2();
		addListenerOnButton();
		verifyFingerprint = (Button) findViewById(R.id.verifyfingerprint);
		ts = (TextView) findViewById(R.id.textView);
		viewBMPIMAGE = (Button) findViewById(R.id.viewbmp);
		vusb.registerBTCallback(MultipleEnrolls.this);

		viewBMPIMAGE.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				try {
					System.out.println("IMAGE : " + imageFilename);
					File imageFile = new File(imageFilename);
					setContentView(R.layout.imageview);
					ImageView BMPView = (ImageView) findViewById(R.id.imageview);
					Bitmap bitmap = BitmapFactory.decodeFile(imageFile
							.getAbsolutePath());
					BMPView.setImageBitmap(bitmap);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		verifyFingerprint.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				try {
					List<Byte> fileLenList = new ArrayList<Byte>();
					List<Byte> fileDataList = new ArrayList<Byte>();

					File sdcard = Environment.getExternalStorageDirectory();
					Log.d(TAG, "THE SDCARD PATH : " + sdcard);

					System.out.println("i value : " + i);
					for (int j = 1; j <= i; j++) {

						String templateFilePath = "/sdcard/template-" + j;
						Log.d(TAG, "THE TEMPLATE PATH : " + templateFilePath);

						File templateFile = new File(templateFilePath);

						byte[] fingerData = null;

						if (templateFile.exists()) {
							FileInputStream fileInputStream = null;
							fingerData = new byte[(int) templateFile.length()];

							try {
								fileInputStream = new FileInputStream(
										templateFile);
								try {
									fileInputStream.read(fingerData);

									for (int k = 0; k < fingerData.length; k++)
										fileDataList.add(fingerData[k]);

								} catch (IOException e) {
									e.printStackTrace();
								}
							} catch (FileNotFoundException e) {
								e.printStackTrace();
							}
						}

						int templateSize = fingerData.length;

						System.out.println("temp  size : " + templateSize);

						byte templateLengthArray[] = intToByteArray(templateSize);

						for (int k = 0; k < templateLengthArray.length; k++)
							fileLenList.add(templateLengthArray[k]);

						System.out.println(templateLengthArray.length);

						System.out.println("DATA" + templateLengthArray[0]);
						System.out.println("DATA " + templateLengthArray[1]);
					}

					System.out.println(fileLenList);
					System.out.println(fileDataList);

					byte[] fileLenListBuffer = new byte[fileLenList.size()];
					for (int i = 0; i < fileLenList.size(); i++) {
						fileLenListBuffer[i] = fileLenList.get(i);
					}

					byte[] fileDataListBuffer = new byte[fileDataList.size()];
					for (int j = 0; j < fileDataList.size(); j++) {
						fileDataListBuffer[j] = fileDataList.get(j);
					}
					System.out.println("temp type : " + tempType);

					vusb.fingerprintMultipleTemplatesVerification(
							UsbSerialDevice.outEndpoint,UsbSerialDevice.inEndpoint, 10, 2, tempType,
							fileLenListBuffer, fileDataListBuffer);
				} catch (Exception e) {

					e.printStackTrace();
				}

			}
		});

	}

	public void addItemsOnSpinner1() {
		templateTypes = (Spinner) findViewById(R.id.temptypes);

		List<String> templist = new ArrayList<String>();
		templist.add("ANSI378");
		templist.add("ISO_FMC_NS");
		templist.add("ISO_FMC_CS");
		templist.add("ISO_FMR");
		templist.add("BIR");

		ArrayAdapter<String> tempdataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, templist);
		tempdataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		templateTypes.setAdapter(tempdataAdapter);
	}

	public void addItemsOnSpinner2() {
		imageTypes = (Spinner) findViewById(R.id.imagetypes);

		List<String> imagelist = new ArrayList<String>();
		imagelist.add("NO_IMAGE");
		imagelist.add("RAW_IMAGE");
		imagelist.add("BMP_IMAGE");
		imagelist.add("WSQ_IMAGE");

		ArrayAdapter<String> imagedataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, imagelist);
		imagedataAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		imageTypes.setAdapter(imagedataAdapter);
	}

	public static final byte[] intToByteArray(int value) {

		byte[] integerBytes = new byte[2];

		integerBytes[0] = (byte) ((value >> 8) & 0xFF);
		integerBytes[1] = (byte) (value & 0xFF);
		return integerBytes;

	}

	public void addListenerOnButton() {

		templateTypes = (Spinner) findViewById(R.id.temptypes);
		imageTypes = (Spinner) findViewById(R.id.imagetypes);
		enroll = (Button) findViewById(R.id.enrollbymultiple);

		enroll.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				i = 0;
				tempType = 1 + templateTypes.getSelectedItemPosition();
				imageType = imageTypes.getSelectedItemPosition();

				Log.d(TAG, "THE SELECTED temptype : " + tempType + " "
						+ imageType);
				vusb.fingerprintEnrollment(UsbSerialDevice.outEndpoint,UsbSerialDevice.inEndpoint,new File("/sdcard/bmp/"),
						tempType, 10, 2, imageType, VisiontekUSB.DE_DUPLICATION);

				// iso_fmr,wsq

			}

		});
	}

	public void showToast(final String toast) {
		Builder alert = new Builder(MultipleEnrolls.this);
		alert.setTitle("97BT");
		alert.setMessage(toast);
		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		alert.show();

	}

	@Override
	public void onPutFinger(String btmsg) {

		callBackMsg = btmsg;

		runOnUiThread(new Thread(new Runnable() {
			public void run() {

				ts.setText(callBackMsg);

			}

		}));

	}

	@Override
	public void onRemoveFinger(String btmsg) {
		callBackMsg = btmsg;
		runOnUiThread(new Thread(new Runnable() {
			public void run() {

				ts.setText(callBackMsg);

			}

		}));
	}

	@Override
	public void onMoveFingerDown(String btmsg) {
		callBackMsg = btmsg;
		runOnUiThread(new Thread(new Runnable() {
			public void run() {

				ts.setText(callBackMsg);

			}

		}));
	}

	@Override
	public void onMoveFingerUP(String btmsg) {
		callBackMsg = btmsg;
		runOnUiThread(new Thread(new Runnable() {
			public void run() {

				ts.setText(callBackMsg);

			}

		}));

	}

	@Override
	public void onMoveFingerRight(String btmsg) {
		callBackMsg = btmsg;
		runOnUiThread(new Thread(new Runnable() {
			public void run() {

				ts.setText(callBackMsg);

			}

		}));

	}

	@Override
	public void onMoveFingerLeft(String btmsg) {
		callBackMsg = btmsg;
		runOnUiThread(new Thread(new Runnable() {
			public void run() {

				ts.setText(callBackMsg);

			}

		}));

	}

	@Override
	public void onPressFingerHard(String btmsg) {
		callBackMsg = btmsg;
		runOnUiThread(new Thread(new Runnable() {
			public void run() {

				ts.setText(callBackMsg);

			}

		}));

	}

	@Override
	public void onSameFinger(String btmsg) {
		callBackMsg = btmsg;
		runOnUiThread(new Thread(new Runnable() {
			public void run() {

				ts.setText(callBackMsg);

			}

		}));

	}

	@Override
	public void onFingerPrintScannerTimeout(String btmsg) {
		callBackMsg = btmsg;
		runOnUiThread(new Thread(new Runnable() {
			public void run() {

				ts.setText(callBackMsg);

			}

		}));

	}

	@Override
	public void onEnrollSuccess(String btmsg) {
		callBackMsg = btmsg;
		runOnUiThread(new Thread(new Runnable() {
			public void run() {

				ts.setText(callBackMsg);

			}

		}));

	}

	@Override
	public void onEnrollFailed(String btmsg) {
		callBackMsg = btmsg;
		runOnUiThread(new Thread(new Runnable() {
			public void run() {

				ts.setText(callBackMsg);

			}

		}));

	}

	@Override
	public void onVerificationSuccess(String btmsg) {
		callBackMsg = btmsg;
		runOnUiThread(new Thread(new Runnable() {
			public void run() {

				ts.setText(callBackMsg);

			}

		}));

	}

	@Override
	public void onVerificationFailed(String btmsg) {
		callBackMsg = btmsg;
		runOnUiThread(new Thread(new Runnable() {
			public void run() {

				ts.setText(callBackMsg);

			}

		}));

	}

	byte[] tempData;

	@Override
	public void onFingerTemplateRecieved(byte[] templateData,
			final int nfiqValue) {
		System.out.println("Multiplefingerprint temp received");
		tempData = new byte[templateData.length];

		tempData = templateData;
		System.out.println("enroll template size : " + tempData.length);

		++i;

		MultipleEnrolls.this.runOnUiThread(new Thread(new Runnable() {
			public void run() {

				System.out.println("TEMP DATA : "
						+ new String(tempData).toString());
				String fpbase = Base64.encodeToString(tempData, Base64.DEFAULT);
				
				// System.out.println("fpbase : " + fpbase);
				byte[] decode = Base64.decode(fpbase, Base64.DEFAULT);
				System.out.println("decode : " + new String(decode).toString());
				System.out.println("i value : " + i);
				try {
					FileOutputStream fos = new FileOutputStream(new File(
							"/sdcard/template-" + i));
					FileOutputStream d = new FileOutputStream(new File(
							"/sdcard/decode-"));
					try {
						fos.write(tempData);
						fos.flush();
						d.write(decode);
						d.flush();
						showToast("NFIQ is : " + nfiqValue);

					} catch (IOException e) {
						e.printStackTrace();
					}
				} catch (FileNotFoundException e) {

					e.printStackTrace();
				}

			}

		}));

	}

	@Override
	public void onInternalFingerPrintModuleCommunicationerror(String btmsg) {
		callBackMsg = btmsg;
		runOnUiThread(new Thread(new Runnable() {
			public void run() {

				ts.setText(callBackMsg);

			}

		}));

	}

	@Override
	public void onFingerPrintInitializationFailed(String btmsg) {
		callBackMsg = btmsg;
		runOnUiThread(new Thread(new Runnable() {
			public void run() {

				ts.setText(callBackMsg);

			}

		}));

	}

	@Override
	public void onTemplateConversionFailed(String btmsg) {
		callBackMsg = btmsg;
		runOnUiThread(new Thread(new Runnable() {
			public void run() {

				ts.setText(callBackMsg);

			}

		}));

	}

	@Override
	public void onInvalidTemplateType(String btmsg) {
		callBackMsg = btmsg;
		runOnUiThread(new Thread(new Runnable() {
			public void run() {

				ts.setText(callBackMsg);

			}

		}));

	}

	@Override
	public void onBTCommunicationFailed(String btmsg) {
		callBackMsg = btmsg;
		runOnUiThread(new Thread(new Runnable() {
			public void run() {

				ts.setText(callBackMsg);

			}

		}));

	}

	@Override
	public void onInvalidTimeout(String btmsg) {
		callBackMsg = btmsg;
		runOnUiThread(new Thread(new Runnable() {
			public void run() {

				ts.setText(callBackMsg);

			}

		}));

	}

	@Override
	public void onInvalidImageType(String btmsg) {
		callBackMsg = btmsg;
		runOnUiThread(new Thread(new Runnable() {
			public void run() {

				ts.setText(callBackMsg);

			}

		}));

	}

	@Override
	public void onTemplateLimitExceeds(String btmsg) {
		callBackMsg = btmsg;
		runOnUiThread(new Thread(new Runnable() {
			public void run() {

				ts.setText(callBackMsg);

			}

		}));

	}

	@Override
	public void onInvalidData(String btmsg) {
		// TODO Auto-generated method stub

		callBackMsg = btmsg;

		runOnUiThread(new Thread(new Runnable() {
			public void run() {

				ts.setText(callBackMsg);

			}

		}));
	}

	@Override
	public void onLengthSetFailed(String btmsg) {
		// TODO Auto-generated method stub

		callBackMsg = btmsg;

		runOnUiThread(new Thread(new Runnable() {
			public void run() {

				ts.setText(callBackMsg);

			}

		}));
	}

	@Override
	public void onImageSaved(String btmsg) {
		// TODO Auto-generated method stub

		callBackMsg = btmsg;

		runOnUiThread(new Thread(new Runnable() {
			public void run() {

				ts.setText(callBackMsg);
				if (callBackMsg.equals("BMP IMAGE SAVED")) {

					try {
						Thread.sleep(100);
						viewBMPIMAGE.setEnabled(true);
					} catch (InterruptedException e) {

						e.printStackTrace();
					}
				} else {
					viewBMPIMAGE.setEnabled(false);
				}

			}

		}));
	}

	@Override
	public void onImageFileNotFound(String btmsg) {
		// TODO Auto-generated method stub

		callBackMsg = btmsg;

		runOnUiThread(new Thread(new Runnable() {
			public void run() {

				ts.setText(callBackMsg);

			}

		}));
	}

	byte[] raw;
	byte[] bmp;
	byte[] wsq;

	@Override
	public void onBMPImageRecieved(byte[] bmpImageData, String file) {
		// bmp = new byte[bmpImageData.length];
		//
		// bmp = bmpImageData;
		try {
			imageFilename = file;
			bmp = new byte[bmpImageData.length];

			bmp = bmpImageData;
		} catch (Exception e) {

			e.printStackTrace();
		}

		runOnUiThread(new Thread(new Runnable() {
			public void run() {

				System.out.println("BMP DATA : " + new String(bmp).toString());

				Bitmap bMap = BitmapFactory.decodeByteArray(bmp, 0, bmp.length);
				// SaveImage(bMap);

			}

		}));

	}

	@Override
	public void onRAWImageRecieved(byte[] rawImageData) {
		raw = new byte[rawImageData.length];

		raw = rawImageData;

		runOnUiThread(new Thread(new Runnable() {
			public void run() {

				System.out.println("RAW DATA : " + new String(raw).toString());

			}

		}));
	}

	@Override
	public void onWSQImageRecieved(byte[] wsqImageData) {
		wsq = new byte[wsqImageData.length];

		wsq = wsqImageData;

		runOnUiThread(new Thread(new Runnable() {
			public void run() {

				System.out.println("WSQ IMAGE DATA : "
						+ new String(wsq).toString());

			}

		}));
	}

	private void SaveImage(Bitmap finalBitmap) {

		String root = Environment.getExternalStorageDirectory().toString();
		File myDir = new File(root + "/jpg");
		myDir.mkdirs();
		Random generator = new Random();
		int n = 10000;
		n = generator.nextInt(n);
		String fname = "Image-" + n + ".jpg";
		File file = new File(myDir, fname);
		if (file.exists())
			file.delete();
		try {
			FileOutputStream out = new FileOutputStream(file);
			finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
			out.flush();
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
