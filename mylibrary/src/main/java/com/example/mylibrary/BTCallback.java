package com.example.mylibrary;

/**
 * 
 * @author Sreekanth Reddy B
 * @e-Mail sreekanth.reddy@visiontek.co.in
 * @company Linkwell Telesystems Pvt. Ltd.
 * @brand Visiontek
 * @department Research and Development
 * @team Application Team
 * @name 97BT Android Application
 * @version 3.2
 * @category Application
 * 
 */

public interface BTCallback {

	public void onPutFinger(String btmsg);

	public void onRemoveFinger(String btmsg);

	public void onMoveFingerDown(String btmsg);

	public void onMoveFingerUP(String btmsg);

	public void onMoveFingerRight(String btmsg);

	public void onMoveFingerLeft(String btmsg);

	public void onPressFingerHard(String btmsg);

	public void onSameFinger(String btmsg);

	public void onFingerPrintScannerTimeout(String btmsg);

	public void onEnrollSuccess(String btmsg);

	public void onEnrollFailed(String btmsg);

	public void onVerificationSuccess(String btmsg);

	public void onVerificationFailed(String btmsg);

	public void onFingerTemplateRecieved(byte[] templateData, int nfiq);

	public void onInternalFingerPrintModuleCommunicationerror(String btmsg);

	public void onFingerPrintInitializationFailed(String btmsg);

	public void onTemplateConversionFailed(String btmsg);

	public void onInvalidTemplateType(String btmsg);

	public void onBTCommunicationFailed(String btmsg);

	public void onInvalidTimeout(String btmsg);

	public void onInvalidImageType(String btmsg);

	public void onTemplateLimitExceeds(String btmsg);

	public void onInvalidData(String btmsg);

	public void onLengthSetFailed(String btmsg);

	public void onImageSaved(String btmsg);

	public void onImageFileNotFound(String btmsg);

	public void onBMPImageRecieved(byte[] bmpImageData, String file);

	public void onRAWImageRecieved(byte[] rawImageData);

	public void onWSQImageRecieved(byte[] wsqImageData);

}
