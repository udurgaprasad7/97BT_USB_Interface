package com.example.mylibrary;

import java.io.File;
import java.io.FileOutputStream;

import android.graphics.Bitmap;
import android.os.Environment;

public class SaveImage {

	public boolean storeImage(Bitmap imageData, String filename) {

		String sdcardPath = Environment.getExternalStorageDirectory()
				.toString();
		File pdfDir = new File(sdcardPath);
		pdfDir.mkdirs();

		File imageFile = new File(pdfDir, filename);
		if (imageFile.exists())
			imageFile.delete();
		try {
			FileOutputStream outputStream = new FileOutputStream(imageFile);
			imageData.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
			outputStream.flush();
			outputStream.close();

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
}