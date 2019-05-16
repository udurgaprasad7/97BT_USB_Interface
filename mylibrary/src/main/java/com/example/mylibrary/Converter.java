package com.example.mylibrary;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.StaticLayout;
import android.text.TextPaint;

public class Converter {

	// Convert Text to image Method
	// pass the paramter : text , size , stroke ,color,typeface
	public Bitmap textAsBitmap(String text, float textSize, float stroke,
			int color, Typeface typeface) {

		TextPaint paint = new TextPaint();
		paint.setColor(color);
		paint.setTextSize(textSize);
		paint.setStrokeWidth(stroke);
		paint.setTypeface(typeface);

		paint.setAntiAlias(true);
		paint.setTextAlign(Paint.Align.LEFT);

		float baseline = (int) (-paint.ascent() + 3f); // ascent() is negative

		/*
		 * StaticLayout staticLayout = new StaticLayout(text, 0, text.length(),
		 * paint, 435, android.text.Layout.Alignment.ALIGN_NORMAL, 1.0f, 1.0f,
		 * false);
		 */

		StaticLayout staticLayout = new StaticLayout(text, 0, text.length(),
				paint, 380, android.text.Layout.Alignment.ALIGN_CENTER, 1.0f,
				1.0f, false);

		int linecount = staticLayout.getLineCount();

		int height = (int) (baseline + paint.descent() + 3) * linecount + 10;

		Bitmap image = Bitmap
				.createBitmap(380, height, Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(image);
		canvas.drawARGB(0xFF, 0xFF, 0xFF, 0xFF);

		staticLayout.draw(canvas);

		return image;

	}

	// Adding Border to bitmap
	public Bitmap addBorder(Bitmap bmp, int borderSize, int borderColor) {
		Bitmap bmpWithBorder = Bitmap.createBitmap(bmp.getWidth() + borderSize
				* 2, bmp.getHeight() + borderSize * 2, bmp.getConfig());
		Canvas canvas = new Canvas(bmpWithBorder);
		canvas.drawColor(borderColor);
		canvas.drawBitmap(bmp, borderSize, borderSize, null);
		return bmpWithBorder;
	}

}