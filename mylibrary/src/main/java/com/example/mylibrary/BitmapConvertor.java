package com.example.mylibrary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Environment;
import android.util.Log;

/**
 * 
 * @author 		Sreekanth Reddy B
 * @e-Mail 		sreekanth.reddy@visiontek.co.in
 * @company 	Linkwell Telesystems Pvt. Ltd.
 * @brand 		Visiontek
 * @department	Research and Development
 * @team		Application Team
 * @name		97BT Android Application
 * @version	3.2
 * @category	Application
 * 
 */

public class BitmapConvertor {

	private static final String TAG = "BMPCONVERTER";

	private int mDataWidth;
	private byte mRawBitmapData[];
	private byte[] mDataArray;
	private int mWidth, mHeight;
	private String mStatus;
	private String mFileName;
	private String targetBmpFile;

	public String convertBitmap(Bitmap inputBitmap, String fileName) {

		mWidth = 384;
		mHeight = inputBitmap.getHeight();
		mFileName = fileName;
		mDataWidth = ((mWidth + 31) / 32) * 4 * 8;
		mDataArray = new byte[(mDataWidth * mHeight)];
		mRawBitmapData = new byte[(mDataWidth * mHeight) / 8];

		convertArgbToGrayscale(inputBitmap, mWidth, mHeight);
		createRawMonochromeData();
		mStatus = saveImage(mFileName, mWidth, mHeight);
		return mStatus;
	}

	private void convertArgbToGrayscale(Bitmap bmpOriginal, int width,
			int height) {
		int pixel;
		int k = 0;
		int B = 0, G = 0, R = 0;
		try {
			for (int x = 0; x < height; x++) {
				for (int y = 0; y < width; y++, k++) {
					// get one pixel color
					pixel = bmpOriginal.getPixel(y, x);

					// retrieve color of all channels
					R = Color.red(pixel);
					G = Color.green(pixel);
					B = Color.blue(pixel);
					// take conversion up to one single value by calculating
					// pixel intensity.
					R = G = B = (int) (0.299 * R + 0.587 * G + 0.114 * B);
					// set new pixel color to output bitmap
					if (R > 128) {
						mDataArray[k] = 0;
					} else {
						mDataArray[k] = 1;
					}
				}
				if (mDataWidth > width) {
					for (int p = width; p < mDataWidth; p++, k++) {
						mDataArray[k] = 1;
					}
				}
			}
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}

	private void createRawMonochromeData() {
		int length = 0;
		for (int i = 0; i < mDataArray.length; i = i + 8) {
			byte first = mDataArray[i];
			for (int j = 0; j < 7; j++) {
				byte second = (byte) ((first << 1) | mDataArray[i + j]);
				first = second;
			}
			mRawBitmapData[length] = first;
			length++;
		}
	}

	private String saveImage(String fileName, int width, int height) {
		FileOutputStream fileOutputStream;
		BMPFile bmpFile = new BMPFile();
		File saveImageFile = new File(
				Environment.getExternalStorageDirectory(), fileName + ".bmp");
		targetBmpFile = saveImageFile.toString();
		try {
			saveImageFile.createNewFile();
			fileOutputStream = new FileOutputStream(saveImageFile);
		} catch (IOException e) {
			return "Memory Access Denied";
		}
		bmpFile.saveBitmap(fileOutputStream, mRawBitmapData, width, height);
		return targetBmpFile;
	}

}
