package com.example.mylibrary;

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

public interface IFDCallback {

	public void onIFDResponce(byte[] ifdResponce);

	public void onIFDATRResponce(byte[] ifdResponce);

}
