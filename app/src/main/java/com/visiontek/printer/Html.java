package com.visiontek.printer;

import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;

public class Html extends PrintDocumentAdapter{

	@Override
	public void onLayout(PrintAttributes oldAttributes,
			PrintAttributes newAttributes,
			CancellationSignal cancellationSignal,
			LayoutResultCallback callback, Bundle extras) {
		
		

		
	}

	@Override
	public void onWrite(PageRange[] pages, ParcelFileDescriptor destination,
			CancellationSignal cancellationSignal, WriteResultCallback callback) {
		
	}
//	String html="<HTML><TITLE>Durgaprasad</TITLE>" +
//			"<BODY>"+"<FONT size=5>Visiontek"+
//			"</FONT></BODY></HTML>";
//	wv.loadData(html, "text/html", "UTF-8");

}
