package com.example.mylibrary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
//import bmpconverter.BitmapConvertor;
//import callback.BTCallback;
//import callback.IFDCallback;

public class VisiontekUSB {
    private static final String TAG = "VISIONTEKBT";
    public static UsbDeviceConnection usbCommunication;
    public static final int ANSI378 = 1;
    public static final int ISO_FMC_NS = 2;
    public static final int ISO_FMC_CS = 3;
    public static final int ISO_FMR = 4;
    public static final int BIR = 5;
    byte[] buffer = new byte[1000];


    public static final int RAW_IMAGE = 1;
    public static final int BMP_IMAGE = 2;
    public static final int WSQ_IMAGE = 3;
    public static final int NO_IMAGE = 0;
    public static final int DUPLICATION = 1;
    public static final int DE_DUPLICATION = 0;

    public String fingerStatus = "";
    public String check_RDS_Status = "";
    private static final int START_BIT = 13;
    private static final int STOP_BIT = 10;
    private static final int MAX_CHUNK_VALUE = 4092;

    private int PACKETBYTES_LENGTH = 2000;
    private byte fingerBMPIMage[] = new byte[2];
    private byte fingerIMageTypes[] = new byte[2];

    private byte[] packetBytes = new byte[PACKETBYTES_LENGTH];

    private int chunkLength = 0, quotient = 0, remainder = 0;
    private boolean LOOP_FLAG;
    private boolean ATR_FLAG = false;
    private String btMessage = null;
    private String BT_MESSAGE = null;
    private String RD_SDK_VER = null;
    private String SDK_VER = null;

    private String operationAck = null;
    private String ackStatus = null;
    private String atrResponceCheck = null;
    private int imageTypeValue = 0;
    private int enrollScanCount = 1;
    private int enrollSuccessCount = 0;
    private int enrollFingerIndex = 0;
    private int intImageLength = 0;

    private Thread workerThread = null;

    private File imageTypeFilename = null;
    private File enrollTemplateName;

    BTCallback btCallback = null;
    IFDCallback ifdCallback = null;

    public void registerBTCallback(BTCallback fpcallback) {
        btCallback = fpcallback;
    }

    public void registerIFDCallback(IFDCallback callback) {
        ifdCallback = callback;
    }


    public String printerLineFeed(UsbEndpoint writeData, UsbEndpoint readData, int lineFeedCount) {

        StringBuilder lineFeedBuilder = new StringBuilder();
        for (int i = 0; i < lineFeedCount; i++) {
            lineFeedBuilder.append("\n");
        }

        String lineFeed = lineFeedBuilder.toString();

        String status = setLength(lineFeed.length() + 4, writeData, readData);

        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> defaultTextList = new ArrayList<Byte>();

            defaultTextList.add((byte) START_BIT);
            defaultTextList.add((byte) 1);
            defaultTextList.add((byte) 1);
            defaultTextList.add((byte) 1);

            byte defaultTextArray[] = lineFeed.getBytes();
            int length = defaultTextArray.length;
            byte defaultTextLength[] = intTo2ByteArray(length);

            defaultTextList.add(defaultTextLength[0]);
            defaultTextList.add(defaultTextLength[1]);

            for (int j = 0; j < length; j++)
                defaultTextList.add(defaultTextArray[j]);

            byte[] checkSum = new byte[defaultTextList.size()];
            for (int i = 1, k = 0; i < defaultTextList.size(); i++, k++) {
                checkSum[k] = defaultTextList.get(i);
            }

            defaultTextList.add(checkSum(checkSum));
            defaultTextList.add((byte) STOP_BIT);

            byte[] defaultTextBuffer = new byte[defaultTextList.size()];
            for (int i = 0; i < defaultTextList.size(); i++) {
                defaultTextBuffer[i] = defaultTextList.get(i);
            }

            try {
                usbCommunication.bulkTransfer(writeData, defaultTextBuffer, defaultTextBuffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btPrinterErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }

        return btMessage;
    }

    // setting multiple- font in same line
    public String multiFont_Setting(UsbEndpoint writeData, UsbEndpoint readData, int font1, int font2, int font_Style,
                                    int font_Size1, int font_Size2) {
        String status = setLength(7, writeData, readData);

        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> fontList = new ArrayList<Byte>();

            fontList.add((byte) START_BIT);
            fontList.add((byte) 1);
            fontList.add((byte) 1);
            fontList.add((byte) 14);
            fontList.add((byte) font1);
            fontList.add((byte) font2);
            fontList.add((byte) font_Style);
            fontList.add((byte) font_Size1);
            fontList.add((byte) font_Size2);

            byte[] checkSum = new byte[fontList.size()];
            for (int i = 1, k = 0; i < fontList.size(); i++, k++) {
                checkSum[k] = fontList.get(i);
            }

            fontList.add(checkSum(checkSum));
            fontList.add((byte) STOP_BIT);

            byte[] fontBuffer = new byte[fontList.size()];
            for (int i = 0; i < fontList.size(); i++) {
                fontBuffer[i] = fontList.get(i);
            }

            try {
                usbCommunication.bulkTransfer(writeData, fontBuffer, fontBuffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btPrinterErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }
        return btMessage;

    }

    // print multi-font in same line
    public String printMultiFontData(UsbEndpoint writeData, UsbEndpoint readData, int dataLen1, int dataLen2,
                                     String totalData) {

        if (dataLen1 < 256 && dataLen2 < 256
                && dataLen1 + dataLen2 == totalData.length()) {
            String status = setLength(totalData.length() + 4, writeData, readData);
            if (status.equals("LENGTH SUCCESS")) {

                List<Byte> textList = new ArrayList<Byte>();

                textList.add((byte) START_BIT);
                textList.add((byte) 1);
                textList.add((byte) 1);
                textList.add((byte) 15);
                textList.add((byte) dataLen1);
                textList.add((byte) dataLen2);

                byte textArray[] = totalData.getBytes();
                int length = textArray.length;

                for (int j = 0; j < length; j++)
                    textList.add(textArray[j]);

                byte[] checkSum = new byte[textList.size()];
                for (int i = 1, k = 0; i < textList.size(); i++, k++) {
                    checkSum[k] = textList.get(i);
                }

                textList.add(checkSum(checkSum));
                textList.add((byte) STOP_BIT);

                byte[] textBuffer = new byte[textList.size()];
                for (int i = 0; i < textList.size(); i++) {
                    textBuffer[i] = textList.get(i);
                }

                try {
                    usbCommunication.bulkTransfer(writeData, textBuffer, textBuffer.length, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                    return "BROKEN PIPE ERROR";
                }

                ackStatus = readAckByte(readData);
                if (ackStatus.equals("A")) {
                    btMessage = btPrinterErrorMessages(readData);
                } else if (ackStatus.equals("N")) {
                    return "RESEND THE DATA";
                }

            } else if (status.equals("LENGTH FAILED")) {
                return "LENGTH SET FAILED";
            } else if (status.equals("BROKEN PIPE ERROR")) {
                return "BROKEN PIPE ERROR";
            } else {
                return "ERROR IN APP";
            }
        } else {
            return "MAXIMUM TEXT LENGTH EXCEEDS";
        }
        return btMessage;

    }

    public String printText(String text, UsbEndpoint writeData, UsbEndpoint readData) {
        System.out.println("some data in library : " + text);

        if (text.length() < 700) {
            String status = setLength(text.length() + 4, writeData, readData);
            if (status.equals("LENGTH SUCCESS")) {

                List<Byte> textList = new ArrayList<Byte>();

                textList.add((byte) START_BIT);
                textList.add((byte) 1);
                textList.add((byte) 1);
                textList.add((byte) 1);

                byte textArray[] = text.getBytes();
                int length = textArray.length;
                byte textLength[] = intTo2ByteArray(length);

                textList.add(textLength[0]);
                textList.add(textLength[1]);

                for (int j = 0; j < length; j++)
                    textList.add(textArray[j]);

                byte[] checkSum = new byte[textList.size()];
                for (int i = 1, k = 0; i < textList.size(); i++, k++) {
                    checkSum[k] = textList.get(i);
                }

                textList.add(checkSum(checkSum));
                textList.add((byte) STOP_BIT);

                byte[] textBuffer = new byte[textList.size()];
                for (int i = 0; i < textList.size(); i++) {
                    textBuffer[i] = textList.get(i);
                }

                try {
                    System.out.println("usbcommunication port : " + usbCommunication);
                    usbCommunication.bulkTransfer(writeData, textBuffer, textBuffer.length, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                    return "BROKEN PIPE ERROR";
                }

                ackStatus = readAckByte(readData);
                if (ackStatus.equals("A")) {
                    btMessage = btPrinterErrorMessages(readData);
                } else if (ackStatus.equals("N")) {
                    return "RESEND THE DATA";
                }

            } else if (status.equals("LENGTH FAILED")) {
                return "LENGTH SET FAILED";
            } else if (status.equals("BROKEN PIPE ERROR")) {
                return "BROKEN PIPE ERROR";
            } else {
                return "ERROR IN APP";
            }
        } else {
            return "MAXIMUM TEXT LENGTH EXCEEDS";
        }
        return btMessage;
    }

    public String printDefaultText(UsbEndpoint writeData, UsbEndpoint readData) {

        String defaultText = "At VISIONTEK we provide extraordinary opportunities for growth. Exposure to various functions within the organization provides unmatched value towards the learning of an individual"
                + "Human Resource plays a significant role in contributing and facilitating the achievement of the strategic business goals of the company and HR at Visiontek takes great responsibility in strengthening the organization capabilities, building a dynamic, vibrant and meritocratic culture and instilling accountability in all the employees.";

        if (defaultText.length() < 700) {

            String status = setLength(defaultText.length() + 4, writeData, readData);

            if (status.equals("LENGTH SUCCESS")) {

                List<Byte> defaultTextList = new ArrayList<Byte>();

                defaultTextList.add((byte) START_BIT);
                defaultTextList.add((byte) 1);
                defaultTextList.add((byte) 1);
                defaultTextList.add((byte) 1);

                byte defaultTextArray[] = defaultText.getBytes();
                int length = defaultTextArray.length;
                byte defaultTextLength[] = intTo2ByteArray(length);

                defaultTextList.add(defaultTextLength[0]);
                defaultTextList.add(defaultTextLength[1]);

                for (int j = 0; j < length; j++)
                    defaultTextList.add(defaultTextArray[j]);

                byte[] checkSum = new byte[defaultTextList.size()];
                for (int i = 1, k = 0; i < defaultTextList.size(); i++, k++) {
                    checkSum[k] = defaultTextList.get(i);
                }

                defaultTextList.add(checkSum(checkSum));
                defaultTextList.add((byte) STOP_BIT);

                byte[] defaultTextBuffer = new byte[defaultTextList.size()];
                for (int i = 0; i < defaultTextList.size(); i++) {
                    defaultTextBuffer[i] = defaultTextList.get(i);
                }

                try {
                    usbCommunication.bulkTransfer(writeData, defaultTextBuffer, defaultTextBuffer.length, 0);

                } catch (Exception e) {
                    e.printStackTrace();
                    return "BROKEN PIPE ERROR";
                }

                ackStatus = readAckByte(readData);
                if (ackStatus.equals("A")) {
                    btMessage = btPrinterErrorMessages(readData);
                } else if (ackStatus.equals("N")) {
                    return "RESEND THE DATA";
                }

            } else if (status.equals("LENGTH FAILED")) {
                return "LENGTH SET FAILED";
            } else if (status.equals("BROKEN PIPE ERROR")) {
                return "BROKEN PIPE ERROR";
            } else {
                return "ERROR IN APP";
            }
        } else {
            return "MAXIMUM TEXT LENGTH EXCEEDS";
        }
        return btMessage;
    }

    public String printBarcode(UsbEndpoint writeData, UsbEndpoint readData, int barcodeType, String barCodeText) {

        String status = setLength(barCodeText.length() + 5, writeData, readData);

        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> barCodeList = new ArrayList<Byte>();

            barCodeList.add((byte) START_BIT);
            barCodeList.add((byte) 1);
            barCodeList.add((byte) 1);
            barCodeList.add((byte) 5);
            barCodeList.add((byte) barcodeType);

            byte barCodeTextArray[] = barCodeText.getBytes();
            int length = barCodeTextArray.length;
            byte barCodeTextLength[] = intTo2ByteArray(length);

            barCodeList.add(barCodeTextLength[0]);
            barCodeList.add(barCodeTextLength[1]);

            for (int j = 0; j < length; j++)
                barCodeList.add(barCodeTextArray[j]);

            byte[] checkSum = new byte[barCodeList.size()];
            for (int i = 1, k = 0; i < barCodeList.size(); i++, k++) {
                checkSum[k] = barCodeList.get(i);
            }

            barCodeList.add(checkSum(checkSum));
            barCodeList.add((byte) STOP_BIT);

            byte[] barCodeTextBuffer = new byte[barCodeList.size()];
            for (int i = 0; i < barCodeList.size(); i++) {
                barCodeTextBuffer[i] = barCodeList.get(i);
            }

            try {
                usbCommunication.bulkTransfer(writeData, barCodeTextBuffer, barCodeTextBuffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btPrinterErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }
        return btMessage;
    }

    public String fontSettings(UsbEndpoint writeData, UsbEndpoint readData, int fontType, int fontStyle, int fontSize) {

        String status = setLength(6, writeData, readData);

        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> fontList = new ArrayList<Byte>();

            fontList.add((byte) START_BIT);
            fontList.add((byte) 1);
            fontList.add((byte) 1);
            fontList.add((byte) 7);
            fontList.add((byte) 1);
            fontList.add((byte) fontType);
            fontList.add((byte) fontStyle);
            fontList.add((byte) fontSize);

            byte[] checkSum = new byte[fontList.size()];
            for (int i = 1, k = 0; i < fontList.size(); i++, k++) {
                checkSum[k] = fontList.get(i);
            }

            fontList.add(checkSum(checkSum));
            fontList.add((byte) STOP_BIT);

            byte[] fontBuffer = new byte[fontList.size()];
            for (int i = 0; i < fontList.size(); i++) {
                fontBuffer[i] = fontList.get(i);
            }

            try {
                usbCommunication.bulkTransfer(writeData, fontBuffer, fontBuffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btPrinterErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }
        return btMessage;
    }

    public String printerReset(UsbEndpoint writeData, UsbEndpoint readData) {

        String status = setLength(2, writeData, readData);

        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> printerResetList = new ArrayList<Byte>();

            printerResetList.add((byte) START_BIT);
            printerResetList.add((byte) 1);
            printerResetList.add((byte) 1);
            printerResetList.add((byte) 8);

            byte[] checkSum = new byte[printerResetList.size()];
            for (int i = 1, k = 0; i < printerResetList.size(); i++, k++) {
                checkSum[k] = printerResetList.get(i);
            }

            printerResetList.add(checkSum(checkSum));
            printerResetList.add((byte) STOP_BIT);

            byte[] printerResetBuffer = new byte[printerResetList.size()];
            for (int i = 0; i < printerResetList.size(); i++) {
                printerResetBuffer[i] = printerResetList.get(i);
            }

            try {
                usbCommunication.bulkTransfer(writeData, printerResetBuffer, printerResetBuffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btPrinterErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }
        return btMessage;
    }

    public String printerDiagnose(UsbEndpoint writeData, UsbEndpoint readData) {

        String status = setLength(2, writeData, readData);

        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> printerDiagnoseList = new ArrayList<Byte>();

            printerDiagnoseList.add((byte) START_BIT);
            printerDiagnoseList.add((byte) 1);
            printerDiagnoseList.add((byte) 1);
            printerDiagnoseList.add((byte) 9);

            byte[] checkSum = new byte[printerDiagnoseList.size()];
            for (int i = 1, k = 0; i < printerDiagnoseList.size(); i++, k++) {
                checkSum[k] = printerDiagnoseList.get(i);
            }

            printerDiagnoseList.add(checkSum(checkSum));
            printerDiagnoseList.add((byte) STOP_BIT);

            byte[] printerDiagnoseBuffer = new byte[printerDiagnoseList.size()];
            for (int i = 0; i < printerDiagnoseList.size(); i++) {
                printerDiagnoseBuffer[i] = printerDiagnoseList.get(i);
            }

            try {
                usbCommunication.bulkTransfer(writeData, printerDiagnoseBuffer, printerDiagnoseBuffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btPrinterErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }
        return btMessage;
    }

    public String setFontAlign(UsbEndpoint writeData, UsbEndpoint readData, int alignValue) {

        String status = setLength(4, writeData, readData);

        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> fontAlignList = new ArrayList<Byte>();

            fontAlignList.add((byte) START_BIT);
            fontAlignList.add((byte) 1);
            fontAlignList.add((byte) 1);
            fontAlignList.add((byte) 7);
            fontAlignList.add((byte) 2);
            fontAlignList.add((byte) alignValue);

            byte[] checkSum = new byte[fontAlignList.size()];
            for (int i = 1, k = 0; i < fontAlignList.size(); i++, k++) {
                checkSum[k] = fontAlignList.get(i);
            }

            fontAlignList.add(checkSum(checkSum));
            fontAlignList.add((byte) STOP_BIT);

            byte[] fontAlignBuffer = new byte[fontAlignList.size()];
            for (int i = 0; i < fontAlignList.size(); i++) {
                fontAlignBuffer[i] = fontAlignList.get(i);
            }

            try {
                usbCommunication.bulkTransfer(writeData, fontAlignBuffer, fontAlignBuffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btPrinterErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }
        return btMessage;
    }

    public String setLanguage(UsbEndpoint writeData, UsbEndpoint readData, int languageID) {

        String status = setLength(4, writeData, readData);

        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> languageList = new ArrayList<Byte>();

            languageList.add((byte) START_BIT);
            languageList.add((byte) 1);
            languageList.add((byte) 1);
            languageList.add((byte) 7);
            languageList.add((byte) 3);
            languageList.add((byte) languageID);

            byte[] checkSum = new byte[languageList.size()];
            for (int i = 1, k = 0; i < languageList.size(); i++, k++) {
                checkSum[k] = languageList.get(i);
            }

            languageList.add(checkSum(checkSum));
            languageList.add((byte) STOP_BIT);

            byte[] languageBuffer = new byte[languageList.size()];
            for (int i = 0; i < languageList.size(); i++) {
                languageBuffer[i] = languageList.get(i);
            }

            try {
                usbCommunication.bulkTransfer(writeData, languageBuffer, languageBuffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btPrinterErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }
        return btMessage;
    }

    public String printStoredBMP(UsbEndpoint writeData, UsbEndpoint readData, String bmpFileName) {

        String status = setLength(bmpFileName.length() + 3, writeData, readData);

        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> printStoredBMPList = new ArrayList<Byte>();

            printStoredBMPList.add((byte) START_BIT);
            printStoredBMPList.add((byte) 1);
            printStoredBMPList.add((byte) 1);
            printStoredBMPList.add((byte) 4);

            byte[] nameBytes = bmpFileName.getBytes();
            int nameLength = nameBytes.length;
            printStoredBMPList.add((byte) nameLength);

            for (int i = 0; i < nameLength; i++)
                printStoredBMPList.add(nameBytes[i]);

            byte[] checkSum = new byte[printStoredBMPList.size()];
            for (int i = 1, k = 0; i < printStoredBMPList.size(); i++, k++) {
                checkSum[k] = printStoredBMPList.get(i);
            }

            printStoredBMPList.add(checkSum(checkSum));
            printStoredBMPList.add((byte) STOP_BIT);

            byte[] printStoredBMPBuffer = new byte[printStoredBMPList.size()];
            for (int i = 0; i < printStoredBMPList.size(); i++) {
                printStoredBMPBuffer[i] = printStoredBMPList.get(i);
            }

            try {
                usbCommunication.bulkTransfer(writeData, printStoredBMPBuffer, printStoredBMPBuffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btPrinterErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }
        return btMessage;
    }

    public String addDataToPrint(UsbEndpoint writeData, UsbEndpoint readData, int fontType, int fontStyle, int fontSize,
                                 int fontAlign, String addData) {

        String status = setLength(addData.length() + 8, writeData, readData);

        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> addDataList = new ArrayList<Byte>();

            addDataList.add((byte) START_BIT);
            addDataList.add((byte) 1);
            addDataList.add((byte) 1);
            addDataList.add((byte) 10);

            addDataList.add((byte) fontType);
            addDataList.add((byte) fontStyle);
            addDataList.add((byte) fontSize);
            addDataList.add((byte) fontAlign);

            byte addDataArray[] = addData.getBytes();
            int length = addDataArray.length;
            byte addDataLength[] = intTo2ByteArray(length);

            addDataList.add(addDataLength[0]);
            addDataList.add(addDataLength[1]);

            for (int i = 0; i < length; i++)
                addDataList.add(addDataArray[i]);

            byte[] checkSum = new byte[addDataList.size()];
            for (int i = 1, k = 0; i < addDataList.size(); i++, k++) {
                checkSum[k] = addDataList.get(i);
            }

            addDataList.add(checkSum(checkSum));
            addDataList.add((byte) STOP_BIT);

            byte[] addDataBuffer = new byte[addDataList.size()];
            for (int i = 0; i < addDataList.size(); i++) {
                addDataBuffer[i] = addDataList.get(i);
            }

            try {
                usbCommunication.bulkTransfer(writeData, addDataBuffer, addDataBuffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btPrinterErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }
        return btMessage;
    }

    public String printAddData(UsbEndpoint writeData, UsbEndpoint readData) {

        String status = setLength(2, writeData, readData);

        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> printDataList = new ArrayList<Byte>();

            printDataList.add((byte) START_BIT);
            printDataList.add((byte) 1);
            printDataList.add((byte) 1);
            printDataList.add((byte) 11);

            byte[] checkSum = new byte[printDataList.size()];
            for (int i = 1, k = 0; i < printDataList.size(); i++, k++) {
                checkSum[k] = printDataList.get(i);
            }

            printDataList.add(checkSum(checkSum));
            printDataList.add((byte) STOP_BIT);

            byte[] printDataBuffer = new byte[printDataList.size()];
            for (int i = 0; i < printDataList.size(); i++) {
                printDataBuffer[i] = printDataList.get(i);
            }

            try {
                usbCommunication.bulkTransfer(writeData, printDataBuffer, printDataBuffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btPrinterErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }
        return btMessage;
    }

    public String printBillFile(UsbEndpoint writeData, UsbEndpoint readData, File billFile) {

        List<Byte> tempBufferList = new ArrayList<Byte>();
        List<Byte> billFileBufferList = new ArrayList<Byte>();

        if (billFile.exists()) {
            FileInputStream fileInputStream = null;

            byte[] billData = new byte[(int) billFile.length()];
            try {
                fileInputStream = new FileInputStream(billFile);
                try {
                    fileInputStream.read(billData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            int billLength = billData.length;
            String status = setLength(billLength + 4, writeData, readData);

            if (status.equals("LENGTH SUCCESS")) {

                int billSize = billData.length;
                byte billLengthArray[] = intTo2ByteArray(billSize);

                quotient = billSize / MAX_CHUNK_VALUE;
                remainder = billSize % MAX_CHUNK_VALUE;

                if (remainder != 0) {
                    int SizeChunk = billSize + (MAX_CHUNK_VALUE - remainder);
                    quotient = SizeChunk / MAX_CHUNK_VALUE;
                    chunkLength = quotient;
                }

                tempBufferList.add((byte) 1);
                tempBufferList.add((byte) 12);
                tempBufferList.add(billLengthArray[0]);
                tempBufferList.add(billLengthArray[1]);
                for (int i = 0; i < billSize; i++)
                    tempBufferList.add(billData[i]);

                billFileBufferList.add((byte) START_BIT);
                billFileBufferList.add((byte) chunkLength);
                for (int j = 0; j < tempBufferList.size(); j++)
                    billFileBufferList.add(tempBufferList.get(j));
                billFileBufferList.add((byte) 5);
                billFileBufferList.add((byte) STOP_BIT);

                for (int i = 0; i < chunkLength; i++) {

                    if (quotient == 1) {

                        if (quotient == 1 && i > 0) {

                            List<Byte> chunkFile = new ArrayList<Byte>();
                            chunkFile.add((byte) 13);
                            chunkFile.add((byte) (chunkLength - i));

                            for (int c = 0; c < remainder + 4; c++)
                                chunkFile.add(tempBufferList.get(c + 4092 * i));

                            byte[] checkSum = new byte[chunkFile.size()];
                            for (int k = 1, j = 0; k < chunkFile.size(); j++, k++) {
                                checkSum[j] = chunkFile.get(k);
                            }

                            chunkFile.add(checkSum(checkSum));
                            chunkFile.add((byte) 10);

                            byte[] billBuffer = new byte[chunkFile.size()];
                            for (int n = 0; n < chunkFile.size(); n++) {
                                billBuffer[n] = chunkFile.get(n);
                            }

                            try {
                                usbCommunication.bulkTransfer(writeData, billBuffer, billBuffer.length, 0);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        } else {
                            billFileBufferList.set(1, (byte) (chunkLength - i));

                            byte[] checkSum = new byte[remainder + 6];
                            for (int k = 1, j = 0; k < remainder + 6; j++, k++) {
                                checkSum[j] = billFileBufferList.get(k);
                            }

                            billFileBufferList.set(remainder + 6,
                                    checkSum(checkSum));
                            billFileBufferList.set(remainder + 7,
                                    (byte) STOP_BIT);

                            byte[] billBuffer = new byte[remainder + 8];
                            for (int n = 0; n < remainder + 8; n++) {
                                billBuffer[n] = billFileBufferList.get(n);
                            }

                            try {
                                usbCommunication.bulkTransfer(writeData, billBuffer, billBuffer.length, 0);
                            } catch (Exception e) {
                                e.printStackTrace();
                                return "BROKEN PIPE ERROR";
                            }
                        }

                    } else if (quotient != 0) {

                        billFileBufferList.set(0, (byte) START_BIT);
                        billFileBufferList.set(1, (byte) (chunkLength - i));

                        for (int j = 0, k = 2; j < 4092; j++, k++) {
                            billFileBufferList.set(k,
                                    tempBufferList.get(j + 4092 * i));
                        }

                        byte[] checkSum = new byte[4094];
                        for (int k = 1, j = 0; k < 4094; k++, j++) {
                            checkSum[j] = billFileBufferList.get(k);
                        }

                        billFileBufferList.set(4094, checkSum(checkSum));
                        billFileBufferList.set(4095, (byte) STOP_BIT);

                        byte[] billBuffer = new byte[4096];
                        for (int n = 0; n < 4096; n++) {
                            billBuffer[n] = billFileBufferList.get(n);
                        }

                        try {
                            usbCommunication.bulkTransfer(writeData, billBuffer, billBuffer.length, 0);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return "BROKEN PIPE ERROR";
                        }
                        quotient--;

                    } else {
                        return "CHUNK FAILED";
                    }
                }

                ackStatus = readAckByte(readData);
                if (ackStatus.equals("A")) {
                    btMessage = btPrinterErrorMessages(readData);
                } else if (ackStatus.equals("N")) {
                    return "RESEND THE DATA";
                }

            } else if (status.equals("LENGTH FAILED")) {
                return "LENGTH SET FAILED";
            } else if (status.equals("BROKEN PIPE ERROR")) {
                return "BROKEN PIPE ERROR";
            } else {
                return "ERROR IN APP";
            }
        } else {
            return "FILE NOT FOUND IN SDCARD";
        }
        return btMessage;
    }

    public String printBillString(UsbEndpoint writeData, UsbEndpoint readData, String billData) {

        List<Byte> tempBufferList = new ArrayList<Byte>();
        List<Byte> billStringBufferList = new ArrayList<Byte>();

        byte billDataArray[] = billData.getBytes();
        int billLength = billDataArray.length;
        String status = setLength(billLength + 4, writeData, readData);

        if (status.equals("LENGTH SUCCESS")) {

            int billSize = billDataArray.length;
            byte billLengthArray[] = intTo2ByteArray(billSize);

            quotient = billSize / MAX_CHUNK_VALUE;
            remainder = billSize % MAX_CHUNK_VALUE;

            if (remainder != 0) {
                int SizeChunk = billSize + (MAX_CHUNK_VALUE - remainder);
                quotient = SizeChunk / MAX_CHUNK_VALUE;
                chunkLength = quotient;
            }

            tempBufferList.add((byte) 1);
            tempBufferList.add((byte) 12);
            tempBufferList.add(billLengthArray[0]);
            tempBufferList.add(billLengthArray[1]);
            System.out.println("billsize : " + billSize);
            System.out.println("billdataarray :" + billDataArray.length);
            for (int i = 0; i < billSize; i++)
                tempBufferList.add(billDataArray[i]);

            billStringBufferList.add((byte) START_BIT);
            billStringBufferList.add((byte) chunkLength);
            for (int j = 0; j < tempBufferList.size(); j++)
                billStringBufferList.add(tempBufferList.get(j));
            billStringBufferList.add((byte) 5);
            billStringBufferList.add((byte) STOP_BIT);

            for (int i = 0; i < chunkLength; i++) {

                if (quotient == 1) {
                    if (quotient == 1 && i > 0) {

                        List<Byte> chunkFile = new ArrayList<Byte>();
                        chunkFile.add((byte) 13);
                        chunkFile.add((byte) (chunkLength - i));

                        for (int c = 0; c < remainder + 4; c++)
                            chunkFile.add(tempBufferList.get(c + 4092 * i));

                        byte[] checkSum = new byte[chunkFile.size()];
                        for (int k = 1, j = 0; k < chunkFile.size(); j++, k++) {
                            checkSum[j] = chunkFile.get(k);
                        }

                        chunkFile.add(checkSum(checkSum));
                        chunkFile.add((byte) 10);

                        byte[] billBuffer = new byte[chunkFile.size()];
                        for (int n = 0; n < chunkFile.size(); n++) {
                            billBuffer[n] = chunkFile.get(n);
                        }

                        try {
                            usbCommunication.bulkTransfer(writeData, billBuffer, billBuffer.length, 0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {
                        billStringBufferList.set(1, (byte) (chunkLength - i));

                        byte[] checkSum = new byte[remainder + 6];
                        for (int k = 1, j = 0; k < remainder + 6; j++, k++) {
                            checkSum[j] = billStringBufferList.get(k);
                        }

                        billStringBufferList.set(remainder + 6,
                                checkSum(checkSum));
                        billStringBufferList
                                .set(remainder + 7, (byte) STOP_BIT);

                        byte[] billBuffer = new byte[remainder + 8];
                        for (int n = 0; n < remainder + 8; n++) {
                            billBuffer[n] = billStringBufferList.get(n);
                        }

                        try {
                            usbCommunication.bulkTransfer(writeData, billBuffer, billBuffer.length, 0);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return "BROKEN PIPE ERROR";
                        }

                    }

                } else if (quotient != 0) {

                    billStringBufferList.set(0, (byte) START_BIT);
                    billStringBufferList.set(1, (byte) (chunkLength - i));

                    for (int j = 0, k = 2; j < 4092; j++, k++) {
                        billStringBufferList.set(k,
                                tempBufferList.get(j + 4092 * i));
                    }

                    byte[] checkSum = new byte[4094];
                    for (int k = 1, j = 0; k < 4094; k++, j++) {
                        checkSum[j] = billStringBufferList.get(k);
                    }

                    billStringBufferList.set(4094, checkSum(checkSum));
                    billStringBufferList.set(4095, (byte) STOP_BIT);

                    byte[] billBuffer = new byte[4096];
                    for (int n = 0; n < 4096; n++) {
                        billBuffer[n] = billStringBufferList.get(n);
                    }

                    try {
                        usbCommunication.bulkTransfer(writeData, billBuffer, billBuffer.length, 0);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return "BROKEN PIPE ERROR";
                    }
                    quotient--;

                } else {
                    return "CHUNK FAILED";
                }
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btPrinterErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }
        return btMessage;
    }

    public String printImage(File imageFile, UsbEndpoint writeData, UsbEndpoint readData) {

        List<Byte> tempBufferList = new ArrayList<Byte>();
        List<Byte> imageBufferList = new ArrayList<Byte>();

        if (imageFile.exists()) {
            FileInputStream fileInputStream = null;

            byte[] imageData = new byte[(int) imageFile.length()];
            try {
                fileInputStream = new FileInputStream(imageFile);
                try {
                    fileInputStream.read(imageData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            int imageLength = imageData.length;
            String status = setLength(imageLength + 5, writeData, readData);

            if (status.equals("LENGTH SUCCESS")) {

                int imageSize = imageData.length;
                byte imageLengthArray[] = intTo4ByteArray(imageSize);

                quotient = imageSize / MAX_CHUNK_VALUE;
                remainder = imageSize % MAX_CHUNK_VALUE;

                if (remainder != 0) {
                    int SizeChunk = imageSize + (MAX_CHUNK_VALUE - remainder);
                    quotient = SizeChunk / MAX_CHUNK_VALUE;
                    chunkLength = quotient;
                }

                tempBufferList.add((byte) 1);
                tempBufferList.add((byte) 2);
                tempBufferList.add(imageLengthArray[1]);
                tempBufferList.add(imageLengthArray[2]);
                tempBufferList.add(imageLengthArray[3]);

                for (int i = 0; i < imageSize; i++)
                    tempBufferList.add(imageData[i]);

                imageBufferList.add((byte) START_BIT);
                imageBufferList.add((byte) chunkLength);
                for (int j = 0; j < tempBufferList.size(); j++)
                    imageBufferList.add(tempBufferList.get(j));
                imageBufferList.add((byte) 5);
                imageBufferList.add((byte) STOP_BIT);

                for (int i = 0; i < chunkLength; i++) {
                    if (quotient == 1) {

                        if (quotient == 1 && i > 0) {

                            List<Byte> chunkImage = new ArrayList<Byte>();
                            chunkImage.add((byte) 13);
                            chunkImage.add((byte) (chunkLength - i));

                            for (int c = 0; c < remainder + 5; c++)
                                chunkImage
                                        .add(tempBufferList.get(c + 4092 * i));

                            byte[] checkSum = new byte[chunkImage.size()];
                            for (int k = 1, j = 0; k < chunkImage.size(); j++, k++) {
                                checkSum[j] = chunkImage.get(k);
                            }

                            chunkImage.add(checkSum(checkSum));
                            chunkImage.add((byte) 10);

                            byte[] imageBuffer = new byte[chunkImage.size()];
                            for (int n = 0; n < chunkImage.size(); n++) {
                                imageBuffer[n] = chunkImage.get(n);
                            }

                            try {
                                usbCommunication.bulkTransfer(writeData, imageBuffer, imageBuffer.length, 0);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            imageBufferList.set(1, (byte) (chunkLength - i));

                            byte[] checkSum = new byte[remainder + 7];
                            for (int k = 1, j = 0; k < remainder + 7; j++, k++) {
                                checkSum[j] = imageBufferList.get(k);
                            }

                            imageBufferList.set(remainder + 7,
                                    checkSum(checkSum));
                            imageBufferList.set(remainder + 8, (byte) STOP_BIT);

                            byte[] imageBuffer = new byte[remainder + 9];
                            for (int n = 0; n < remainder + 9; n++) {
                                imageBuffer[n] = imageBufferList.get(n);
                            }

                            try {
                                usbCommunication.bulkTransfer(writeData, imageBuffer, imageBuffer.length, 0);
                            } catch (Exception e) {
                                e.printStackTrace();
                                return "BROKEN PIPE ERROR";
                            }
                        }

                    } else if (quotient != 0) {

                        imageBufferList.set(0, (byte) START_BIT);
                        imageBufferList.set(1, (byte) (chunkLength - i));

                        for (int j = 0, k = 2; j < 4092; j++, k++) {
                            imageBufferList.set(k,
                                    tempBufferList.get(j + 4092 * i));
                        }

                        byte[] checkSum = new byte[4094];
                        for (int k = 1, j = 0; k < 4094; k++, j++) {
                            checkSum[j] = imageBufferList.get(k);
                        }

                        imageBufferList.set(4094, checkSum(checkSum));
                        imageBufferList.set(4095, (byte) STOP_BIT);

                        byte[] imageBuffer = new byte[4096];
                        for (int n = 0; n < 4096; n++) {
                            imageBuffer[n] = imageBufferList.get(n);
                        }

                        try {
                            usbCommunication.bulkTransfer(writeData, imageBuffer, imageBuffer.length, 0);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return "BROKEN PIPE ERROR";
                        }
                        quotient--;

                    } else {
                        return "CHUNK FAILED";
                    }
                }

                ackStatus = readAckByte(readData);
                if (ackStatus.equals("A")) {
                    btMessage = btPrinterErrorMessages(readData);
                } else if (ackStatus.equals("N")) {
                    return "RESEND THE DATA";
                }

            } else if (status.equals("LENGTH FAILED")) {
                return "LENGTH SET FAILED";
            } else if (status.equals("BROKEN PIPE ERROR")) {
                return "BROKEN PIPE ERROR";
            } else {
                return "ERROR IN APP";
            }
        } else {
            return "FILE NOT FOUND IN SDCARD";
        }
        return btMessage;
    }

    /*
     * public String printPdfFile() {
     *
     * List<Byte> tempBufferList = new ArrayList<Byte>(); List<Byte>
     * imageBufferList = new ArrayList<Byte>();
     *
     * String pdfFilesPath = Environment.getExternalStorageDirectory()
     * .toString() + "/pdftobmp";
     *
     * File pdfFile = new File(pdfFilesPath); File pdfFilesCount[] =
     * pdfFile.listFiles();
     *
     * // for (int p = 0; p < pdfFilesCount.length; p++) {
     *
     * for (int p = 0; p < 1; p++) {
     *
     * String pdfImageFileName = pdfFilesCount[p].getName();
     *
     * // File pdfImageFile = new File(pdfFilesPath + "/" + //
     * pdfImageFileName);
     *
     * File pdfImageFile = new File(pdfFilesPath + "/" + "pdf-1.png");
     *
     * // System.out.println(" FILE " + pdfImageFile.toString());
     *
     * Log.d("Files", "FileName:" + pdfImageFile);
     *
     * if (pdfImageFile.exists()) {
     *
     * String scaleImage = scaleImage(pdfImageFile.toString(), 384); String
     * bmpImage = bmpConversion(scaleImage);
     *
     * File finalImageFile = new File(bmpImage);
     *
     * if (finalImageFile.exists()) { FileInputStream fileInputStream = null;
     *
     * byte[] imageData = new byte[(int) finalImageFile.length()]; try {
     * fileInputStream = new FileInputStream(finalImageFile); try {
     * fileInputStream.read(imageData); } catch (IOException e) {
     * e.printStackTrace(); } } catch (FileNotFoundException e) {
     * e.printStackTrace(); }
     *
     * int imageLength = imageData.length; String status =
     * setLength(mOutputStream, mInputStream, imageLength + 5);
     *
     * if (status.equals("LENGTH SUCCESS")) {
     *
     * int imageSize = imageData.length; byte imageLengthArray[] =
     * intTo4ByteArray(imageSize);
     *
     * quotient = imageSize / MAX_CHUNK_VALUE; remainder = imageSize %
     * MAX_CHUNK_VALUE;
     *
     * if (remainder != 0) { int SizeChunk = imageSize + (MAX_CHUNK_VALUE -
     * remainder); quotient = SizeChunk / MAX_CHUNK_VALUE; chunkLength =
     * quotient; }
     *
     * tempBufferList.add((byte) 1); tempBufferList.add((byte) 2);
     * tempBufferList.add(imageLengthArray[1]);
     * tempBufferList.add(imageLengthArray[2]);
     * tempBufferList.add(imageLengthArray[3]);
     *
     * for (int i = 0; i < imageSize; i++) tempBufferList.add(imageData[i]);
     *
     * imageBufferList.add((byte) START_BIT); imageBufferList.add((byte)
     * chunkLength); for (int j = 0; j < tempBufferList.size(); j++)
     * imageBufferList.add(tempBufferList.get(j)); imageBufferList.add((byte)
     * 5); imageBufferList.add((byte) STOP_BIT);
     *
     * for (int i = 0; i < chunkLength; i++) {
     *
     * if (quotient == 1) {
     *
     * if (quotient == 1 && i > 0) {
     *
     * List<Byte> chunkImage = new ArrayList<Byte>(); chunkImage.add((byte) 13);
     * chunkImage.add((byte) (chunkLength - i));
     *
     * for (int c = 0; c < remainder + 5; c++)
     * chunkImage.add(tempBufferList.get(c + MAX_CHUNK_VALUE * i));
     *
     * byte[] checkSum = new byte[chunkImage .size()]; for (int k = 1, j = 0; k
     * < chunkImage .size(); j++, k++) { checkSum[j] = chunkImage.get(k); }
     *
     * chunkImage.add(checkSum(checkSum)); chunkImage.add((byte) 10);
     *
     * byte[] imageBuffer = new byte[chunkImage .size()]; for (int n = 0; n <
     * chunkImage.size(); n++) { imageBuffer[n] = chunkImage.get(n); }
     *
     * try { mOutputStream.write(imageBuffer); mOutputStream.flush(); } catch
     * (IOException e) { e.printStackTrace(); } } else { imageBufferList.set(1,
     * (byte) (chunkLength - i));
     *
     * byte[] checkSum = new byte[remainder + 7]; for (int k = 1, j = 0; k <
     * remainder + 7; j++, k++) { checkSum[j] = imageBufferList.get(k); }
     *
     * imageBufferList.set(remainder + 7, checkSum(checkSum));
     * imageBufferList.set(remainder + 8, (byte) STOP_BIT);
     *
     * byte[] imageBuffer = new byte[remainder + 9]; for (int n = 0; n <
     * remainder + 9; n++) { imageBuffer[n] = imageBufferList.get(n); }
     *
     * try { mOutputStream.write(imageBuffer); mOutputStream.flush(); } catch
     * (IOException e) { e.printStackTrace(); return "BROKEN PIPE ERROR"; } }
     *
     * } else if (quotient != 0) {
     *
     * imageBufferList.set(0, (byte) START_BIT); imageBufferList .set(1, (byte)
     * (chunkLength - i));
     *
     * for (int j = 0, k = 2; j < 4092; j++, k++) { imageBufferList.set(k,
     * tempBufferList.get(j + 4092 * i)); }
     *
     * byte[] checkSum = new byte[4094]; for (int k = 1, j = 0; k < 4094; k++,
     * j++) { checkSum[j] = imageBufferList.get(k); }
     *
     * imageBufferList.set(4094, checkSum(checkSum)); imageBufferList.set(4095,
     * (byte) STOP_BIT);
     *
     * byte[] imageBuffer = new byte[4096]; for (int n = 0; n < 4096; n++) {
     * imageBuffer[n] = imageBufferList.get(n); }
     *
     * try { mOutputStream.write(imageBuffer); mOutputStream.flush(); } catch
     * (IOException e) { e.printStackTrace(); return "BROKEN PIPE ERROR"; }
     * quotient--;
     *
     * } else { return "CHUNK FAILED"; } }
     *
     * ackStatus = readAckByte(mInputStream); if (ackStatus.equals("A")) {
     * btMessage = btPrinterErrorMessages(mInputStream); } else if
     * (ackStatus.equals("N")) { return "RESEND THE DATA"; }
     *
     * } else if (status.equals("LENGTH FAILED")) { return "LENGTH SET FAILED";
     * } else if (status.equals("BROKEN PIPE ERROR")) { return
     * "BROKEN PIPE ERROR"; } else { return "ERROR IN APP"; } } else { return
     * "FILE NOT FOUND IN SDCARD"; } } else { return "FILE NOT FOUND IN SDCARD";
     * } } return btMessage; }
     */

    public String printDynamicImage(UsbEndpoint writeData, UsbEndpoint readData, File imageFile) {

        List<Byte> tempBufferList = new ArrayList<Byte>();
        List<Byte> imageBufferList = new ArrayList<Byte>();

        if (imageFile.exists()) {

            String scaleImage = scaleImage(imageFile.toString(), 384);
            String bmpImage = bmpConversion(scaleImage);

            File finalImageFile = new File(bmpImage);
            System.out.println("final image file : " + finalImageFile);

            if (finalImageFile.exists()) {
                FileInputStream fileInputStream = null;

                byte[] imageData = new byte[(int) finalImageFile.length()];
                try {
                    fileInputStream = new FileInputStream(finalImageFile);
                    try {
                        fileInputStream.read(imageData);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                int imageLength = imageData.length;


                String status = setLength(imageLength + 5, writeData, readData);

                if (status.equals("LENGTH SUCCESS")) {

                    int imageSize = imageData.length;
                    byte imageLengthArray[] = intTo4ByteArray(imageSize);

                    quotient = imageSize / MAX_CHUNK_VALUE;
                    remainder = imageSize % MAX_CHUNK_VALUE;

                    if (remainder != 0) {
                        int SizeChunk = imageSize
                                + (MAX_CHUNK_VALUE - remainder);
                        quotient = SizeChunk / MAX_CHUNK_VALUE;
                        chunkLength = quotient;
                    }

                    tempBufferList.add((byte) 1);
                    tempBufferList.add((byte) 2);
                    tempBufferList.add(imageLengthArray[1]);
                    tempBufferList.add(imageLengthArray[2]);
                    tempBufferList.add(imageLengthArray[3]);

                    for (int i = 0; i < imageSize; i++)
                        tempBufferList.add(imageData[i]);

                    imageBufferList.add((byte) START_BIT);
                    imageBufferList.add((byte) chunkLength);
                    for (int j = 0; j < tempBufferList.size(); j++)
                        imageBufferList.add(tempBufferList.get(j));
                    imageBufferList.add((byte) 5);
                    imageBufferList.add((byte) STOP_BIT);

                    for (int i = 0; i < chunkLength; i++) {

                        if (quotient == 1) {

                            if (quotient == 1 && i > 0) {

                                List<Byte> chunkImage = new ArrayList<Byte>();
                                chunkImage.add((byte) 13);
                                chunkImage.add((byte) (chunkLength - i));

                                for (int c = 0; c < remainder + 5; c++)
                                    chunkImage.add(tempBufferList.get(c
                                            + MAX_CHUNK_VALUE * i));

                                byte[] checkSum = new byte[chunkImage.size()];
                                for (int k = 1, j = 0; k < chunkImage.size(); j++, k++) {
                                    checkSum[j] = chunkImage.get(k);
                                }

                                chunkImage.add(checkSum(checkSum));
                                chunkImage.add((byte) 10);

                                byte[] imageBuffer = new byte[chunkImage.size()];
                                for (int n = 0; n < chunkImage.size(); n++) {
                                    imageBuffer[n] = chunkImage.get(n);
                                }

                                try {
                                    usbCommunication.bulkTransfer(writeData, imageBuffer, imageBuffer.length, 0);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                imageBufferList
                                        .set(1, (byte) (chunkLength - i));

                                byte[] checkSum = new byte[remainder + 7];
                                for (int k = 1, j = 0; k < remainder + 7; j++, k++) {
                                    checkSum[j] = imageBufferList.get(k);
                                }

                                imageBufferList.set(remainder + 7,
                                        checkSum(checkSum));
                                imageBufferList.set(remainder + 8,
                                        (byte) STOP_BIT);

                                byte[] imageBuffer = new byte[remainder + 9];
                                for (int n = 0; n < remainder + 9; n++) {
                                    imageBuffer[n] = imageBufferList.get(n);
                                }

                                try {
                                    usbCommunication.bulkTransfer(writeData, imageBuffer, imageBuffer.length, 0);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return "BROKEN PIPE ERROR";
                                }
                            }

                        } else if (quotient != 0) {

                            imageBufferList.set(0, (byte) START_BIT);
                            imageBufferList.set(1, (byte) (chunkLength - i));

                            for (int j = 0, k = 2; j < 4092; j++, k++) {
                                imageBufferList.set(k,
                                        tempBufferList.get(j + 4092 * i));
                            }

                            byte[] checkSum = new byte[4094];
                            for (int k = 1, j = 0; k < 4094; k++, j++) {
                                checkSum[j] = imageBufferList.get(k);
                            }

                            imageBufferList.set(4094, checkSum(checkSum));
                            imageBufferList.set(4095, (byte) STOP_BIT);

                            byte[] imageBuffer = new byte[4096];
                            for (int n = 0; n < 4096; n++) {
                                imageBuffer[n] = imageBufferList.get(n);
                            }

                            try {
                                usbCommunication.bulkTransfer(writeData, imageBuffer, imageBuffer.length, 0);
                            } catch (Exception e) {
                                e.printStackTrace();
                                return "BROKEN PIPE ERROR";
                            }
                            quotient--;

                        } else {
                            return "CHUNK FAILED";
                        }
                    }

                    ackStatus = readAckByte(readData);
                    if (ackStatus.equals("A")) {
                        btMessage = btPrinterErrorMessages(readData);
                    } else if (ackStatus.equals("N")) {
                        return "RESEND THE DATA";
                    }

                } else if (status.equals("LENGTH FAILED")) {
                    return "LENGTH SET FAILED";
                } else if (status.equals("BROKEN PIPE ERROR")) {
                    return "BROKEN PIPE ERROR";
                } else {
                    return "ERROR IN APP";
                }
            } else {
                return "FILE NOT FOUND IN SDCARD";
            }
        } else {
            return "FILE NOT FOUND IN SDCARD";
        }
        return btMessage;
    }

    public String storeBMPToDevice(UsbEndpoint writeData, UsbEndpoint readData, String imageFile, String deviceFileName) {

        List<Byte> tempBufferList = new ArrayList<Byte>();
        List<Byte> storeBmpBufferList = new ArrayList<Byte>();

        FileInputStream fileInputStream = null;

        byte[] imageData = new byte[(int) imageFile.length()];
        try {
            fileInputStream = new FileInputStream(imageFile);
            try {
                fileInputStream.read(imageData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int imageLength = imageData.length;
        String status = setLength(imageLength + deviceFileName.length() + 6, writeData, readData);

        if (status.equals("LENGTH SUCCESS")) {

            byte[] imageNameBytes = deviceFileName.getBytes();
            int imageNameLength = imageNameBytes.length;

            int imageSize = imageData.length;
            byte imageLengthArray[] = intTo4ByteArray(imageSize);

            quotient = imageSize / MAX_CHUNK_VALUE;
            remainder = imageSize % MAX_CHUNK_VALUE;

            if (remainder != 0) {
                int SizeChunk = imageSize + (MAX_CHUNK_VALUE - remainder);
                quotient = SizeChunk / MAX_CHUNK_VALUE;
                chunkLength = quotient;
            }

            tempBufferList.add((byte) 1);
            tempBufferList.add((byte) 3);
            tempBufferList.add(imageLengthArray[1]);
            tempBufferList.add(imageLengthArray[2]);
            tempBufferList.add(imageLengthArray[3]);
            tempBufferList.add((byte) imageNameLength);
            for (int i = 0; i < imageNameLength; i++)
                tempBufferList.add(imageNameBytes[i]);

            for (int i = 0; i < imageSize; i++)
                tempBufferList.add(imageData[i]);

            storeBmpBufferList.add((byte) START_BIT);
            storeBmpBufferList.add((byte) chunkLength);
            for (int j = 0; j < tempBufferList.size(); j++)
                storeBmpBufferList.add(tempBufferList.get(j));
            storeBmpBufferList.add((byte) 5);
            storeBmpBufferList.add((byte) STOP_BIT);

            for (int i = 0; i < chunkLength; i++) {

                if (quotient == 1) {

                    if (quotient == 1 && i > 0) {

                        List<Byte> chunkImage = new ArrayList<Byte>();
                        chunkImage.add((byte) 13);
                        chunkImage.add((byte) (chunkLength - i));

                        for (int c = 0; c < remainder + imageNameLength + 6; c++)
                            chunkImage.add(tempBufferList.get(c + 4092 * i));

                        byte[] checkSum = new byte[chunkImage.size()];
                        for (int k = 1, j = 0; k < chunkImage.size(); j++, k++) {
                            checkSum[j] = chunkImage.get(k);
                        }

                        chunkImage.add(checkSum(checkSum));
                        chunkImage.add((byte) 10);

                        byte[] storeBmpBuffer = new byte[chunkImage.size()];
                        for (int n = 0; n < chunkImage.size(); n++) {
                            storeBmpBuffer[n] = chunkImage.get(n);
                        }

                        try {
                            usbCommunication.bulkTransfer(writeData, storeBmpBuffer, storeBmpBuffer.length, 0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        storeBmpBufferList.set(1, (byte) (chunkLength - i));

                        byte[] checkSum = new byte[remainder + imageNameLength
                                + 8];
                        for (int k = 1, j = 0; k < remainder + imageNameLength
                                + 8; j++, k++) {
                            checkSum[j] = storeBmpBufferList.get(k);
                        }

                        storeBmpBufferList.set(remainder + imageNameLength + 8,
                                checkSum(checkSum));
                        storeBmpBufferList.set(remainder + imageNameLength + 9,
                                (byte) STOP_BIT);

                        byte[] storeBmpBuffer = new byte[remainder
                                + imageNameLength + 10];
                        for (int n = 0; n < remainder + imageNameLength + 10; n++) {
                            storeBmpBuffer[n] = storeBmpBufferList.get(n);
                        }

                        try {
                            usbCommunication.bulkTransfer(writeData, storeBmpBuffer, storeBmpBuffer.length, 0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                } else if (quotient != 0) {

                    storeBmpBufferList.set(0, (byte) START_BIT);
                    storeBmpBufferList.set(1, (byte) (chunkLength - i));

                    for (int j = 0, k = 2; j < 4092; j++, k++) {
                        storeBmpBufferList.set(k,
                                tempBufferList.get(j + 4092 * i));
                    }

                    byte[] checkSum = new byte[4094];
                    for (int k = 1, j = 0; k < 4094; k++, j++) {
                        checkSum[j] = storeBmpBufferList.get(k);
                    }

                    storeBmpBufferList.set(4094, checkSum(checkSum));
                    storeBmpBufferList.set(4095, (byte) STOP_BIT);

                    byte[] storeBmpBuffer = new byte[4096];
                    for (int n = 0; n < 4096; n++) {
                        storeBmpBuffer[n] = storeBmpBufferList.get(n);
                    }

                    try {
                        // mOutputStream.write(storeBmpBuffer);
                        // mOutputStream.flush();
                    } catch (Exception e) {
                        return "BROKEN PIPE ERROR";
                    }
                    quotient--;
                    ackStatus = readAckByte(readData);
                    if (ackStatus.equals("A")) {
                        btMessage = btPrinterErrorMessages(readData);
                    } else if (ackStatus.equals("N")) {
                        return "RESEND THE DATA";
                    }

                } else {
                    return "CHUNK FAILED";
                }
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btPrinterErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }

        return btMessage;
    }

    public String printImageConvert(File imageFile, UsbEndpoint writeData, UsbEndpoint readData) {

        List<Byte> tempBufferList = new ArrayList<Byte>();
        List<Byte> imageBufferList = new ArrayList<Byte>();

        if (imageFile.exists()) {

            String scaleImage = scaleImage(imageFile.toString(), 384);
            String bmpImage = bmpConversion(scaleImage);

            File finalImageFile = new File(bmpImage);

            if (finalImageFile.exists()) {
                FileInputStream fileInputStream = null;

                byte[] imageData = new byte[(int) finalImageFile.length()];
                try {
                    fileInputStream = new FileInputStream(finalImageFile);
                    try {
                        fileInputStream.read(imageData);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                int imageLength = imageData.length;
                String status = setLength(imageLength + 5, writeData, readData);

                if (status.equals("LENGTH SUCCESS")) {

                    int imageSize = imageData.length;
                    byte imageLengthArray[] = intTo4ByteArray(imageSize);

                    quotient = imageSize / MAX_CHUNK_VALUE;
                    remainder = imageSize % MAX_CHUNK_VALUE;

                    if (remainder != 0) {
                        int SizeChunk = imageSize
                                + (MAX_CHUNK_VALUE - remainder);
                        quotient = SizeChunk / MAX_CHUNK_VALUE;
                        chunkLength = quotient;
                    }

                    tempBufferList.add((byte) 1);
                    tempBufferList.add((byte) 2);
                    tempBufferList.add(imageLengthArray[1]);
                    tempBufferList.add(imageLengthArray[2]);
                    tempBufferList.add(imageLengthArray[3]);

                    for (int i = 0; i < imageSize; i++)
                        tempBufferList.add(imageData[i]);

                    imageBufferList.add((byte) START_BIT);
                    imageBufferList.add((byte) chunkLength);
                    for (int j = 0; j < tempBufferList.size(); j++)
                        imageBufferList.add(tempBufferList.get(j));
                    imageBufferList.add((byte) 5);
                    imageBufferList.add((byte) STOP_BIT);

                    for (int i = 0; i < chunkLength; i++) {

                        if (quotient == 1) {

                            if (quotient == 1 && i > 0) {

                                List<Byte> chunkImage = new ArrayList<Byte>();
                                chunkImage.add((byte) 13);
                                chunkImage.add((byte) (chunkLength - i));

                                for (int c = 0; c < remainder + 5; c++)
                                    chunkImage.add(tempBufferList.get(c
                                            + MAX_CHUNK_VALUE * i));

                                byte[] checkSum = new byte[chunkImage.size()];
                                for (int k = 1, j = 0; k < chunkImage.size(); j++, k++) {
                                    checkSum[j] = chunkImage.get(k);
                                }

                                chunkImage.add(checkSum(checkSum));
                                chunkImage.add((byte) 10);

                                byte[] imageBuffer = new byte[chunkImage.size()];
                                for (int n = 0; n < chunkImage.size(); n++) {
                                    imageBuffer[n] = chunkImage.get(n);
                                }

                                try {
                                    usbCommunication.bulkTransfer(writeData, imageBuffer, imageBuffer.length, 0);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                imageBufferList
                                        .set(1, (byte) (chunkLength - i));

                                byte[] checkSum = new byte[remainder + 7];
                                for (int k = 1, j = 0; k < remainder + 7; j++, k++) {
                                    checkSum[j] = imageBufferList.get(k);
                                }

                                imageBufferList.set(remainder + 7,
                                        checkSum(checkSum));
                                imageBufferList.set(remainder + 8,
                                        (byte) STOP_BIT);

                                byte[] imageBuffer = new byte[remainder + 9];
                                for (int n = 0; n < remainder + 9; n++) {
                                    imageBuffer[n] = imageBufferList.get(n);
                                }

                                try {
                                    usbCommunication.bulkTransfer(writeData, imageBuffer, imageBuffer.length, 0);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return "BROKEN PIPE ERROR";
                                }
                            }

                        } else if (quotient != 0) {

                            imageBufferList.set(0, (byte) START_BIT);
                            imageBufferList.set(1, (byte) (chunkLength - i));

                            for (int j = 0, k = 2; j < 4092; j++, k++) {
                                imageBufferList.set(k,
                                        tempBufferList.get(j + 4092 * i));
                            }

                            byte[] checkSum = new byte[4094];
                            for (int k = 1, j = 0; k < 4094; k++, j++) {
                                checkSum[j] = imageBufferList.get(k);
                            }

                            imageBufferList.set(4094, checkSum(checkSum));
                            imageBufferList.set(4095, (byte) STOP_BIT);

                            byte[] imageBuffer = new byte[4096];
                            for (int n = 0; n < 4096; n++) {
                                imageBuffer[n] = imageBufferList.get(n);
                            }

                            try {
                                usbCommunication.bulkTransfer(writeData, imageBuffer, imageBuffer.length, 0);
                            } catch (Exception e) {
                                e.printStackTrace();
                                return "BROKEN PIPE ERROR";
                            }
                            quotient--;

                        } else {
                            return "CHUNK FAILED";
                        }
                    }

                    ackStatus = readAckByte(readData);
                    if (ackStatus.equals("A")) {
                        btMessage = btPrinterErrorMessages(readData);
                    } else if (ackStatus.equals("N")) {
                        return "RESEND THE DATA";
                    }

                } else if (status.equals("LENGTH FAILED")) {
                    return "LENGTH SET FAILED";
                } else if (status.equals("BROKEN PIPE ERROR")) {
                    return "BROKEN PIPE ERROR";
                } else {
                    return "ERROR IN APP";
                }
            } else {
                return "FILE NOT FOUND IN SDCARD";
            }
        } else {
            return "FILE NOT FOUND IN SDCARD";
        }
        return btMessage;
    }

    public String storeBMPToDeviceImageConvert(UsbEndpoint writeData, UsbEndpoint readData, String imageName,
                                               String deviceFileName) {

        List<Byte> tempBufferList = new ArrayList<Byte>();
        List<Byte> storeBmpBufferList = new ArrayList<Byte>();
        File sdcardPath = Environment.getExternalStorageDirectory();
        Log.d(TAG, "THE SDCARD PATH : " + sdcardPath);

        String imagePath = sdcardPath + "/" + imageName;
        Log.d(TAG, "THE IMAGE PATH : " + imagePath);

        File imageFile = new File(imagePath);

        if (imageFile.exists()) {

            String scaleImage = scaleImage(imageFile.toString(), 384);
            String bmpImage = bmpConversion(scaleImage);

            File finalImageFile = new File(bmpImage);

            if (finalImageFile.exists()) {
                FileInputStream fileInputStream = null;

                byte[] imageData = new byte[(int) finalImageFile.length()];
                try {
                    fileInputStream = new FileInputStream(finalImageFile);
                    try {
                        fileInputStream.read(imageData);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                int imageLength = imageData.length;
                String status = setLength(imageLength + deviceFileName.length()
                        + 6, writeData, readData);

                if (status.equals("LENGTH SUCCESS")) {

                    byte[] imageNameBytes = deviceFileName.getBytes();
                    int imageNameLength = imageNameBytes.length;

                    int imageSize = imageData.length;
                    byte imageLengthArray[] = intTo4ByteArray(imageSize);

                    quotient = imageSize / MAX_CHUNK_VALUE;
                    remainder = imageSize % MAX_CHUNK_VALUE;

                    if (remainder != 0) {
                        int SizeChunk = imageSize
                                + (MAX_CHUNK_VALUE - remainder);
                        quotient = SizeChunk / MAX_CHUNK_VALUE;
                        chunkLength = quotient;
                    }

                    tempBufferList.add((byte) 1);
                    tempBufferList.add((byte) 3);
                    tempBufferList.add(imageLengthArray[1]);
                    tempBufferList.add(imageLengthArray[2]);
                    tempBufferList.add(imageLengthArray[3]);
                    tempBufferList.add((byte) imageNameLength);
                    for (int i = 0; i < imageNameLength; i++)
                        tempBufferList.add(imageNameBytes[i]);

                    for (int i = 0; i < imageSize; i++)
                        tempBufferList.add(imageData[i]);

                    storeBmpBufferList.add((byte) START_BIT);
                    storeBmpBufferList.add((byte) chunkLength);
                    for (int j = 0; j < tempBufferList.size(); j++)
                        storeBmpBufferList.add(tempBufferList.get(j));
                    storeBmpBufferList.add((byte) 5);
                    storeBmpBufferList.add((byte) STOP_BIT);

                    for (int i = 0; i < chunkLength; i++) {

                        if (quotient == 1) {

                            if (quotient == 1 && i > 0) {

                                List<Byte> chunkImage = new ArrayList<Byte>();
                                chunkImage.add((byte) 13);
                                chunkImage.add((byte) (chunkLength - i));

                                for (int c = 0; c < remainder + imageNameLength
                                        + 6; c++)
                                    chunkImage.add(tempBufferList.get(c + 4092
                                            * i));

                                byte[] checkSum = new byte[chunkImage.size()];
                                for (int k = 1, j = 0; k < chunkImage.size(); j++, k++) {
                                    checkSum[j] = chunkImage.get(k);
                                }

                                chunkImage.add(checkSum(checkSum));
                                chunkImage.add((byte) 10);

                                byte[] storeBmpBuffer = new byte[chunkImage
                                        .size()];
                                for (int n = 0; n < chunkImage.size(); n++) {
                                    storeBmpBuffer[n] = chunkImage.get(n);
                                }

                                try {
                                    usbCommunication.bulkTransfer(writeData, storeBmpBuffer, storeBmpBuffer.length, 0);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                storeBmpBufferList.set(1,
                                        (byte) (chunkLength - i));

                                byte[] checkSum = new byte[remainder
                                        + imageNameLength + 8];
                                for (int k = 1, j = 0; k < remainder
                                        + imageNameLength + 8; j++, k++) {
                                    checkSum[j] = storeBmpBufferList.get(k);
                                }

                                storeBmpBufferList.set(remainder
                                                + imageNameLength + 8,
                                        checkSum(checkSum));
                                storeBmpBufferList.set(remainder
                                        + imageNameLength + 9, (byte) STOP_BIT);

                                byte[] storeBmpBuffer = new byte[remainder
                                        + imageNameLength + 10];
                                for (int n = 0; n < remainder + imageNameLength
                                        + 10; n++) {
                                    storeBmpBuffer[n] = storeBmpBufferList
                                            .get(n);
                                }

                                try {
                                    usbCommunication.bulkTransfer(writeData, storeBmpBuffer, storeBmpBuffer.length, 0);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                        } else if (quotient != 0) {

                            storeBmpBufferList.set(0, (byte) START_BIT);
                            storeBmpBufferList.set(1, (byte) (chunkLength - i));

                            for (int j = 0, k = 2; j < 4092; j++, k++) {
                                storeBmpBufferList.set(k,
                                        tempBufferList.get(j + 4092 * i));
                            }

                            byte[] checkSum = new byte[4094];
                            for (int k = 1, j = 0; k < 4094; k++, j++) {
                                checkSum[j] = storeBmpBufferList.get(k);
                            }

                            storeBmpBufferList.set(4094, checkSum(checkSum));
                            storeBmpBufferList.set(4095, (byte) STOP_BIT);

                            byte[] storeBmpBuffer = new byte[4096];
                            for (int n = 0; n < 4096; n++) {
                                storeBmpBuffer[n] = storeBmpBufferList.get(n);
                            }

                            try {
                                usbCommunication.bulkTransfer(writeData, storeBmpBuffer, storeBmpBuffer.length, 0);
                            } catch (Exception e) {
                                return "BROKEN PIPE ERROR";
                            }
                            quotient--;

                        } else {
                            return "CHUNK FAILED";
                        }
                    }

                    ackStatus = readAckByte(readData);
                    if (ackStatus.equals("A")) {
                        btMessage = btPrinterErrorMessages(readData);
                    } else if (ackStatus.equals("N")) {
                        return "RESEND THE DATA";
                    }

                } else if (status.equals("LENGTH FAILED")) {
                    return "LENGTH SET FAILED";
                } else if (status.equals("BROKEN PIPE ERROR")) {
                    return "BROKEN PIPE ERROR";
                } else {
                    return "ERROR IN APP";
                }
            } else {
                return "FILE NOT FOUND IN SDCARD";
            }
        }

        return btMessage;
    }

    public String printImageScaling(File imageFile, UsbEndpoint writeData, UsbEndpoint readData) {

        List<Byte> tempBufferList = new ArrayList<Byte>();
        List<Byte> imageBufferList = new ArrayList<Byte>();

        if (imageFile.exists()) {

            String scaleImage = scaleImage(imageFile.toString(), 384);

            File finalImageFile = new File(scaleImage);

            if (finalImageFile.exists()) {
                FileInputStream fileInputStream = null;

                byte[] imageData = new byte[(int) finalImageFile.length()];
                try {
                    fileInputStream = new FileInputStream(finalImageFile);
                    try {
                        fileInputStream.read(imageData);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                int imageLength = imageData.length;
                String status = setLength(imageLength + 5, writeData, readData);

                if (status.equals("LENGTH SUCCESS")) {

                    int imageSize = imageData.length;
                    byte imageLengthArray[] = intTo4ByteArray(imageSize);

                    quotient = imageSize / MAX_CHUNK_VALUE;
                    remainder = imageSize % MAX_CHUNK_VALUE;

                    if (remainder != 0) {
                        int SizeChunk = imageSize
                                + (MAX_CHUNK_VALUE - remainder);
                        quotient = SizeChunk / MAX_CHUNK_VALUE;
                        chunkLength = quotient;
                    }

                    tempBufferList.add((byte) 1);
                    tempBufferList.add((byte) 2);
                    tempBufferList.add(imageLengthArray[1]);
                    tempBufferList.add(imageLengthArray[2]);
                    tempBufferList.add(imageLengthArray[3]);

                    for (int i = 0; i < imageSize; i++)
                        tempBufferList.add(imageData[i]);

                    imageBufferList.add((byte) START_BIT);
                    imageBufferList.add((byte) chunkLength);
                    for (int j = 0; j < tempBufferList.size(); j++)
                        imageBufferList.add(tempBufferList.get(j));
                    imageBufferList.add((byte) 5);
                    imageBufferList.add((byte) STOP_BIT);

                    for (int i = 0; i < chunkLength; i++) {

                        if (quotient == 1) {

                            if (quotient == 1 && i > 0) {

                                List<Byte> chunkImage = new ArrayList<Byte>();
                                chunkImage.add((byte) 13);
                                chunkImage.add((byte) (chunkLength - i));

                                for (int c = 0; c < remainder + 5; c++)
                                    chunkImage.add(tempBufferList.get(c
                                            + MAX_CHUNK_VALUE * i));

                                byte[] checkSum = new byte[chunkImage.size()];
                                for (int k = 1, j = 0; k < chunkImage.size(); j++, k++) {
                                    checkSum[j] = chunkImage.get(k);
                                }

                                chunkImage.add(checkSum(checkSum));
                                chunkImage.add((byte) 10);

                                byte[] imageBuffer = new byte[chunkImage.size()];
                                for (int n = 0; n < chunkImage.size(); n++) {
                                    imageBuffer[n] = chunkImage.get(n);
                                }

                                try {
                                    usbCommunication.bulkTransfer(writeData, imageBuffer, imageBuffer.length, 0);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                imageBufferList
                                        .set(1, (byte) (chunkLength - i));

                                byte[] checkSum = new byte[remainder + 7];
                                for (int k = 1, j = 0; k < remainder + 7; j++, k++) {
                                    checkSum[j] = imageBufferList.get(k);
                                }

                                imageBufferList.set(remainder + 7,
                                        checkSum(checkSum));
                                imageBufferList.set(remainder + 8,
                                        (byte) STOP_BIT);

                                byte[] imageBuffer = new byte[remainder + 9];
                                for (int n = 0; n < remainder + 9; n++) {
                                    imageBuffer[n] = imageBufferList.get(n);
                                }

                                try {
                                    usbCommunication.bulkTransfer(writeData, imageBuffer, imageBuffer.length, 0);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    return "BROKEN PIPE ERROR";
                                }
                            }

                        } else if (quotient != 0) {

                            imageBufferList.set(0, (byte) START_BIT);
                            imageBufferList.set(1, (byte) (chunkLength - i));

                            for (int j = 0, k = 2; j < 4092; j++, k++) {
                                imageBufferList.set(k,
                                        tempBufferList.get(j + 4092 * i));
                            }

                            byte[] checkSum = new byte[4094];
                            for (int k = 1, j = 0; k < 4094; k++, j++) {
                                checkSum[j] = imageBufferList.get(k);
                            }

                            imageBufferList.set(4094, checkSum(checkSum));
                            imageBufferList.set(4095, (byte) STOP_BIT);

                            byte[] imageBuffer = new byte[4096];
                            for (int n = 0; n < 4096; n++) {
                                imageBuffer[n] = imageBufferList.get(n);
                            }

                            try {
                                usbCommunication.bulkTransfer(writeData, imageBuffer, imageBuffer.length, 0);
                            } catch (Exception e) {
                                e.printStackTrace();
                                return "BROKEN PIPE ERROR";
                            }
                            quotient--;

                        } else {
                            return "CHUNK FAILED";
                        }
                    }

                    ackStatus = readAckByte(readData);
                    if (ackStatus.equals("A")) {
                        btMessage = btPrinterErrorMessages(readData);
                    } else if (ackStatus.equals("N")) {
                        return "RESEND THE DATA";
                    }

                } else if (status.equals("LENGTH FAILED")) {
                    return "LENGTH SET FAILED";
                } else if (status.equals("BROKEN PIPE ERROR")) {
                    return "BROKEN PIPE ERROR";
                } else {
                    return "ERROR IN APP";
                }
            } else {
                return "FILE NOT FOUND IN SDCARD";
            }
        } else {
            return "FILE NOT FOUND IN SDCARD";
        }
        return btMessage;
    }

    public String storeBMPToDeviceImageScaling(UsbEndpoint writeData, UsbEndpoint readData, String imageName,
                                               String deviceFileName) {

        List<Byte> tempBufferList = new ArrayList<Byte>();
        List<Byte> storeBmpBufferList = new ArrayList<Byte>();

        File sdcardPath = Environment.getExternalStorageDirectory();
        Log.d(TAG, "THE SDCARD PATH : " + sdcardPath);

        String imagePath = sdcardPath + "/" + imageName;
        Log.d(TAG, "THE IMAGE PATH : " + imagePath);

        File imageFile = new File(imagePath);

        if (imageFile.exists()) {

            String scaleImage = scaleImage(imagePath, 384);

            File finalImageFile = new File(scaleImage);

            if (finalImageFile.exists()) {
                FileInputStream fileInputStream = null;

                byte[] imageData = new byte[(int) finalImageFile.length()];
                try {
                    fileInputStream = new FileInputStream(finalImageFile);
                    try {
                        fileInputStream.read(imageData);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                int imageLength = imageData.length;
                String status = setLength(imageLength + deviceFileName.length()
                        + 6, writeData, readData);

                if (status.equals("LENGTH SUCCESS")) {

                    byte[] imageNameBytes = deviceFileName.getBytes();
                    int imageNameLength = imageNameBytes.length;

                    int imageSize = imageData.length;
                    byte imageLengthArray[] = intTo4ByteArray(imageSize);

                    quotient = imageSize / MAX_CHUNK_VALUE;
                    remainder = imageSize % MAX_CHUNK_VALUE;

                    if (remainder != 0) {
                        int SizeChunk = imageSize
                                + (MAX_CHUNK_VALUE - remainder);
                        quotient = SizeChunk / MAX_CHUNK_VALUE;
                        chunkLength = quotient;
                    }

                    tempBufferList.add((byte) 1);
                    tempBufferList.add((byte) 3);
                    tempBufferList.add(imageLengthArray[1]);
                    tempBufferList.add(imageLengthArray[2]);
                    tempBufferList.add(imageLengthArray[3]);
                    tempBufferList.add((byte) imageNameLength);
                    for (int i = 0; i < imageNameLength; i++)
                        tempBufferList.add(imageNameBytes[i]);

                    for (int i = 0; i < imageSize; i++)
                        tempBufferList.add(imageData[i]);

                    storeBmpBufferList.add((byte) START_BIT);
                    storeBmpBufferList.add((byte) chunkLength);
                    for (int j = 0; j < tempBufferList.size(); j++)
                        storeBmpBufferList.add(tempBufferList.get(j));
                    storeBmpBufferList.add((byte) 5);
                    storeBmpBufferList.add((byte) STOP_BIT);

                    for (int i = 0; i < chunkLength; i++) {

                        if (quotient == 1) {

                            if (quotient == 1 && i > 0) {

                                List<Byte> chunkImage = new ArrayList<Byte>();
                                chunkImage.add((byte) 13);
                                chunkImage.add((byte) (chunkLength - i));

                                for (int c = 0; c < remainder + imageNameLength
                                        + 6; c++)
                                    chunkImage.add(tempBufferList.get(c + 4092
                                            * i));

                                byte[] checkSum = new byte[chunkImage.size()];
                                for (int k = 1, j = 0; k < chunkImage.size(); j++, k++) {
                                    checkSum[j] = chunkImage.get(k);
                                }

                                chunkImage.add(checkSum(checkSum));
                                chunkImage.add((byte) 10);

                                byte[] storeBmpBuffer = new byte[chunkImage
                                        .size()];
                                for (int n = 0; n < chunkImage.size(); n++) {
                                    storeBmpBuffer[n] = chunkImage.get(n);
                                }

                                try {
                                    usbCommunication.bulkTransfer(writeData, storeBmpBuffer, storeBmpBuffer.length, 0);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                storeBmpBufferList.set(1,
                                        (byte) (chunkLength - i));

                                byte[] checkSum = new byte[remainder
                                        + imageNameLength + 8];
                                for (int k = 1, j = 0; k < remainder
                                        + imageNameLength + 8; j++, k++) {
                                    checkSum[j] = storeBmpBufferList.get(k);
                                }

                                storeBmpBufferList.set(remainder
                                                + imageNameLength + 8,
                                        checkSum(checkSum));
                                storeBmpBufferList.set(remainder
                                        + imageNameLength + 9, (byte) STOP_BIT);

                                byte[] storeBmpBuffer = new byte[remainder
                                        + imageNameLength + 10];
                                for (int n = 0; n < remainder + imageNameLength
                                        + 10; n++) {
                                    storeBmpBuffer[n] = storeBmpBufferList
                                            .get(n);
                                }

                                try {
                                    usbCommunication.bulkTransfer(writeData, storeBmpBuffer, storeBmpBuffer.length, 0);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                            }
                        } else if (quotient != 0) {

                            storeBmpBufferList.set(0, (byte) START_BIT);
                            storeBmpBufferList.set(1, (byte) (chunkLength - i));

                            for (int j = 0, k = 2; j < 4092; j++, k++) {
                                storeBmpBufferList.set(k,
                                        tempBufferList.get(j + 4092 * i));
                            }

                            byte[] checkSum = new byte[4094];
                            for (int k = 1, j = 0; k < 4094; k++, j++) {
                                checkSum[j] = storeBmpBufferList.get(k);
                            }

                            storeBmpBufferList.set(4094, checkSum(checkSum));
                            storeBmpBufferList.set(4095, (byte) STOP_BIT);

                            byte[] storeBmpBuffer = new byte[4096];
                            for (int n = 0; n < 4096; n++) {
                                storeBmpBuffer[n] = storeBmpBufferList.get(n);
                            }

                            try {
                                usbCommunication.bulkTransfer(writeData, storeBmpBuffer, storeBmpBuffer.length, 0);
                            } catch (Exception e) {
                                return "BROKEN PIPE ERROR";
                            }
                            quotient--;

                        } else {
                            return "CHUNK FAILED";
                        }
                    }

                    ackStatus = readAckByte(readData);
                    if (ackStatus.equals("A")) {
                        btMessage = btPrinterErrorMessages(readData);
                    } else if (ackStatus.equals("N")) {
                        return "RESEND THE DATA";
                    }

                } else if (status.equals("LENGTH FAILED")) {
                    return "LENGTH SET FAILED";
                } else if (status.equals("BROKEN PIPE ERROR")) {
                    return "BROKEN PIPE ERROR";
                } else {
                    return "ERROR IN APP";
                }
            } else {
                return "FILE NOT FOUND IN SDCARD";
            }
        } else {
            return "FILE NOT FOUND IN SDCARD";
        }
        return btMessage;
    }

    public String printLanguage(UsbEndpoint writeData, UsbEndpoint readData, File languageHexFile) {

        List<Byte> tempBufferList = new ArrayList<Byte>();
        List<Byte> languageBufferList = new ArrayList<Byte>();

        if (languageHexFile.exists()) {
            FileInputStream fileInputStream = null;

            byte[] languageHexData = new byte[(int) languageHexFile.length()];
            try {
                fileInputStream = new FileInputStream(languageHexFile);
                try {
                    fileInputStream.read(languageHexData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            int languageLength = languageHexData.length;

            String status = setLength(languageLength + 5, writeData, readData);

            if (status.equals("LENGTH SUCCESS")) {

                int languageSize = languageHexData.length;
                byte languageLengthArray[] = intTo4ByteArray(languageSize);

                quotient = languageSize / MAX_CHUNK_VALUE;
                remainder = languageSize % MAX_CHUNK_VALUE;

                if (remainder != 0) {
                    int SizeChunk = languageSize
                            + (MAX_CHUNK_VALUE - remainder);
                    quotient = SizeChunk / MAX_CHUNK_VALUE;
                    chunkLength = quotient;
                }

                tempBufferList.add((byte) 1);
                tempBufferList.add((byte) 6);
                tempBufferList.add(languageLengthArray[1]);
                tempBufferList.add(languageLengthArray[2]);
                tempBufferList.add(languageLengthArray[3]);

                for (int i = 0; i < languageSize; i++)
                    tempBufferList.add(languageHexData[i]);

                languageBufferList.add((byte) START_BIT);
                languageBufferList.add((byte) chunkLength);
                for (int j = 0; j < tempBufferList.size(); j++)
                    languageBufferList.add(tempBufferList.get(j));
                languageBufferList.add((byte) 5);
                languageBufferList.add((byte) STOP_BIT);

                for (int i = 0; i < chunkLength; i++) {

                    if (quotient == 1) {

                        if (quotient == 1 && i > 0) {

                            List<Byte> chunkLanguage = new ArrayList<Byte>();
                            chunkLanguage.add((byte) 13);
                            chunkLanguage.add((byte) (chunkLength - i));

                            for (int c = 0; c < remainder + 5; c++)
                                chunkLanguage.add(tempBufferList.get(c + 4092
                                        * i));

                            byte[] checkSum = new byte[chunkLanguage.size()];
                            for (int k = 1, j = 0; k < chunkLanguage.size(); j++, k++) {
                                checkSum[j] = chunkLanguage.get(k);
                            }

                            chunkLanguage.add(checkSum(checkSum));
                            chunkLanguage.add((byte) 10);

                            byte[] billBuffer = new byte[chunkLanguage.size()];
                            for (int n = 0; n < chunkLanguage.size(); n++) {
                                billBuffer[n] = chunkLanguage.get(n);
                            }

                            try {
                                usbCommunication.bulkTransfer(writeData, billBuffer, billBuffer.length, 0);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        } else {
                            languageBufferList.set(1, (byte) (chunkLength - i));

                            byte[] checkSum = new byte[remainder + 7];
                            for (int k = 1, j = 0; k < remainder + 7; j++, k++) {
                                checkSum[j] = languageBufferList.get(k);
                            }

                            languageBufferList.set(remainder + 7,
                                    checkSum(checkSum));
                            languageBufferList.set(remainder + 8,
                                    (byte) STOP_BIT);

                            byte[] languageBuffer = new byte[remainder + 9];
                            for (int n = 0; n < remainder + 9; n++) {
                                languageBuffer[n] = languageBufferList.get(n);
                            }

                            try {
                                usbCommunication.bulkTransfer(writeData, languageBuffer, languageBuffer.length, 0);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    } else if (quotient != 0) {

                        languageBufferList.set(0, (byte) START_BIT);
                        languageBufferList.set(1, (byte) (chunkLength - i));

                        for (int j = 0, k = 2; j < 4092; j++, k++) {
                            languageBufferList.set(k,
                                    tempBufferList.get(j + 4092 * i));
                        }

                        byte[] checkSum = new byte[4094];
                        for (int k = 1, j = 0; k < 4094; k++, j++) {
                            checkSum[j] = languageBufferList.get(k);
                        }

                        languageBufferList.set(4094, checkSum(checkSum));
                        languageBufferList.set(4095, (byte) STOP_BIT);

                        byte[] languageBuffer = new byte[4096];
                        for (int n = 0; n < 4096; n++) {
                            languageBuffer[n] = languageBufferList.get(n);
                        }

                        try {
                            usbCommunication.bulkTransfer(writeData, languageBuffer, languageBuffer.length, 0);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return "BROKEN PIPE ERROR";
                        }
                        quotient--;

                    } else {
                        return "CHUNK FAILED";
                    }
                }

                ackStatus = readAckByte(readData);
                if (ackStatus.equals("A")) {
                    btMessage = btPrinterErrorMessages(readData);
                } else if (ackStatus.equals("N")) {
                    return "RESEND THE DATA";
                }

            } else if (status.equals("LENGTH FAILED")) {
                return "LENGTH SET FAILED";
            } else if (status.equals("BROKEN PIPE ERROR")) {
                return "BROKEN PIPE ERROR";
            } else {
                return "ERROR IN APP";
            }
        } else {
            return "FILE NOT FOUND IN SDCARD";
        }
        return btMessage;
    }

    // print multiple formats of local language like bold and normal
    public String Multi_format_Language_Print(File BoldData, File NormalData,
                                              int lang_Type, int fontSize, UsbEndpoint writeData, UsbEndpoint readData) {

        List<Byte> tempBufferList = new ArrayList<Byte>();
        List<Byte> languageBufferList = new ArrayList<Byte>();

        if (BoldData.exists() & NormalData.exists()) {
            System.out.println("files exist");
            FileInputStream fileInputStream = null;

            int DataLength1, DataLength2, totalDataLen;

            DataLength1 = (int) BoldData.length();
            DataLength2 = (int) NormalData.length();
            totalDataLen = DataLength1 + DataLength2;
            System.out.println("Bold length : " + DataLength1);
            System.out.println("Normal length : " + DataLength2);
            byte[] languageHexData1, languageHexData2, TotalHexData = new byte[totalDataLen];
            languageHexData1 = new byte[DataLength1];
            languageHexData2 = new byte[DataLength2];

            try {

                fileInputStream = new FileInputStream(BoldData);

                int data1 = fileInputStream.read(languageHexData1);

                for (int i = 0; i < data1; i++) {
                    TotalHexData[i] = languageHexData1[i];

                }
                System.out.println("totalhexdata1:"
                        + Arrays.toString(TotalHexData));
                fileInputStream = new FileInputStream(NormalData);

                int data2 = fileInputStream.read(languageHexData2);

                for (int i = languageHexData1.length, j = 0; j < data2; i++, j++) {

                    TotalHexData[i] = languageHexData2[j];

                }
                System.out.println("totalhexdata2:"
                        + Arrays.toString(TotalHexData));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            int languageLength = TotalHexData.length;

            String status = setLength(languageLength + 5, writeData, readData);

            if (status.equals("LENGTH SUCCESS")) {

                int languageSize = TotalHexData.length;
                byte languageLengthArray[] = intTo4ByteArray(languageSize);
                System.out.println("lang array : "
                        + Arrays.toString(languageLengthArray));

                byte BoldDataArray[] = intTo4ByteArray(DataLength1);
                System.out.println("Bold array : "
                        + Arrays.toString(BoldDataArray));
                quotient = languageSize / MAX_CHUNK_VALUE;
                remainder = languageSize % MAX_CHUNK_VALUE;

                if (remainder != 0) {
                    int SizeChunk = languageSize
                            + (MAX_CHUNK_VALUE - remainder);
                    quotient = SizeChunk / MAX_CHUNK_VALUE;
                    chunkLength = quotient;
                }

                tempBufferList.add((byte) 1);
                tempBufferList.add((byte) 14);
                tempBufferList.add(languageLengthArray[1]);
                tempBufferList.add(languageLengthArray[2]);
                tempBufferList.add(languageLengthArray[3]);
                // tempBufferList.add((BoldDataArray[1]));
                tempBufferList.add((BoldDataArray[2]));
                tempBufferList.add((BoldDataArray[3]));
                tempBufferList.add((byte) lang_Type);
                tempBufferList.add((byte) fontSize);
                System.out.println("total temp buff : " + tempBufferList);
                for (int i = 0; i < languageSize; i++)
                    tempBufferList.add(TotalHexData[i]);

                languageBufferList.add((byte) START_BIT);
                languageBufferList.add((byte) chunkLength);
                for (int j = 0; j < tempBufferList.size(); j++)
                    languageBufferList.add(tempBufferList.get(j));
                languageBufferList.add((byte) 5);
                languageBufferList.add((byte) STOP_BIT);

                for (int i = 0; i < chunkLength; i++) {

                    if (quotient == 1) {

                        if (quotient == 1 && i > 0) {

                            List<Byte> chunkLanguage = new ArrayList<Byte>();
                            chunkLanguage.add((byte) 13);
                            chunkLanguage.add((byte) (chunkLength - i));

                            for (int c = 0; c < remainder + 5; c++)
                                chunkLanguage.add(tempBufferList.get(c + 4092
                                        * i));

                            byte[] checkSum = new byte[chunkLanguage.size()];
                            for (int k = 1, j = 0; k < chunkLanguage.size(); j++, k++) {
                                checkSum[j] = chunkLanguage.get(k);
                            }

                            chunkLanguage.add(checkSum(checkSum));
                            chunkLanguage.add((byte) 10);

                            byte[] billBuffer = new byte[chunkLanguage.size()];
                            for (int n = 0; n < chunkLanguage.size(); n++) {
                                billBuffer[n] = chunkLanguage.get(n);
                            }

                            try {
                                usbCommunication.bulkTransfer(writeData, billBuffer, billBuffer.length, 0);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        } else {
                            languageBufferList.set(1, (byte) (chunkLength - i));

                            byte[] checkSum = new byte[remainder + 7];
                            for (int k = 1, j = 0; k < remainder + 7; j++, k++) {
                                checkSum[j] = languageBufferList.get(k);
                            }

                            languageBufferList.set(remainder + 7,
                                    checkSum(checkSum));
                            languageBufferList.set(remainder + 8,
                                    (byte) STOP_BIT);

                            byte[] languageBuffer = new byte[remainder + 9];
                            for (int n = 0; n < remainder + 9; n++) {
                                languageBuffer[n] = languageBufferList.get(n);
                            }

                            try {
                                usbCommunication.bulkTransfer(writeData, languageBuffer, languageBuffer.length, 0);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        }
                    } else if (quotient != 0) {

                        languageBufferList.set(0, (byte) START_BIT);
                        languageBufferList.set(1, (byte) (chunkLength - i));

                        for (int j = 0, k = 2; j < 4092; j++, k++) {
                            languageBufferList.set(k,
                                    tempBufferList.get(j + 4092 * i));
                        }

                        byte[] checkSum = new byte[4094];
                        for (int k = 1, j = 0; k < 4094; k++, j++) {
                            checkSum[j] = languageBufferList.get(k);
                        }

                        languageBufferList.set(4094, checkSum(checkSum));
                        languageBufferList.set(4095, (byte) STOP_BIT);

                        byte[] languageBuffer = new byte[4096];
                        for (int n = 0; n < 4096; n++) {
                            languageBuffer[n] = languageBufferList.get(n);
                        }

                        try {
                            usbCommunication.bulkTransfer(writeData, languageBuffer, languageBuffer.length, 0);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return "BROKEN PIPE ERROR";
                        }
                        quotient--;

                    } else {
                        return "CHUNK FAILED";
                    }
                }

                ackStatus = readAckByte(readData);
                if (ackStatus.equals("A")) {
                    btMessage = btPrinterErrorMessages(readData);
                } else if (ackStatus.equals("N")) {
                    return "RESEND THE DATA";
                }

            } else if (status.equals("LENGTH FAILED")) {
                return "LENGTH SET FAILED";
            } else if (status.equals("BROKEN PIPE ERROR")) {
                return "BROKEN PIPE ERROR";
            } else {
                return "ERROR IN APP";
            }
        } else {
            return "FILE NOT FOUND IN SDCARD";
        }
        return btMessage;
    }

    // engineers days api

//	public String print_Bold_normal(File BoldData, String normalData) {
//
//		List<Byte> tempBufferList = new ArrayList<Byte>();
//		List<Byte> languageBufferList = new ArrayList<Byte>();
//
//		if (BoldData.exists()) {
//			FileInputStream fileInputStream = null;
//
//			int DataLength1, DataLength2, bothDataLen;
//
//			DataLength1 = (int) BoldData.length();
//			byte[] normaldataLen = normalData.getBytes();
//			int billSize = normaldataLen.length;
//				tempBufferList.add((byte) 15);
//
//				tempBufferList.add(totLengthArray[1]);
//				tempBufferList.add(totLengthArray[2]);
//				tempBufferList.add(totLengthArray[3]);
//
//				tempBufferList.add((BoldDataArray[2]));
//				tempBufferList.add((BoldDataArray[3]));
//				//
//				// tempBufferList.add(billLengthArray[0]);
//				// tempBufferList.add(billLengthArray[1]);
//				//
//
//				// for (int i = 0; i <billSize; i++)
//				// tempBufferList.add(normaldataLen[i]);
//
//				System.out.println("total temp buff : " + tempBufferList);
//
//				for (int i = 0; i < totalDataLeng; i++)
//					tempBufferList.add(TotalHexData[i]);
//
//				languageBufferList.add((byte) START_BIT);
//				languageBufferList.add((byte) chunkLength);
//				for (int j = 0; j < tempBufferList.size(); j++)
//					languageBufferList.add(tempBufferList.get(j));
//
//				languageBufferList.add((byte) 5);
//				languageBufferList.add((byte) STOP_BIT);
//
//				for (int i = 0; i < chunkLength; i++) {
//					if (quotient == 1) {
//
//						if (quotient == 1 && i > 0) {
//
//							List<Byte> chunkLanguage = new ArrayList<Byte>();
//							chunkLanguage.add((byte) 13);
//							chunkLanguage.add((byte) (chunkLength - i));
//
//							for (int c = 0; c < remainder + 5; c++)
//								chunkLanguage.add(tempBufferList.get(c + 4092
//										* i));
//
//							byte[] checkSum = new byte[chunkLanguage.size()];
//							for (int k = 1, j = 0; k < chunkLanguage.size(); j++, k++) {
//								checkSum[j] = chunkLanguage.get(k);
//							}
//
//							chunkLanguage.add(checkSum(checkSum));
//							chunkLanguage.add((byte) 10);
//
//							byte[] billBuffer = new byte[chunkLanguage.size()];
//							for (int n = 0; n < chunkLanguage.size(); n++) {
//								billBuffer[n] = chunkLanguage.get(n);
//							}
//
//							try {
//								// mOutputStream.write(billBuffer);
//								// mOutputStream.flush();
//							} catch (Exception e) {
//								e.printStackTrace();
//							}
//
//						} else {
//							languageBufferList.set(1, (byte) (chunkLength - i));
//
//							byte[] checkSum = new byte[remainder + 7];
//							for (int k = 1, j = 0; k < remainder + 7; j++, k++) {
//								checkSum[j] = languageBufferList.get(k);
//							}
//
//							languageBufferList.set(remainder + 7,
//									checkSum(checkSum));
//							languageBufferList.set(remainder + 8,
//									(byte) STOP_BIT);
//
//							byte[] languageBuffer = new byte[remainder + 9];
//							for (int n = 0; n < remainder + 9; n++) {
//								languageBuffer[n] = languageBufferList.get(n);
//							}
//
//							try {
//								// mOutputStream.write(languageBuffer);
//								// mOutputStream.flush();
//							} catch (Exception e) {
//								e.printStackTrace();
//							}
//
//						}
//					} else if (quotient != 0) {
//
//						languageBufferList.set(0, (byte) START_BIT);
//						languageBufferList.set(1, (byte) (chunkLength - i));
//
//						for (int j = 0, k = 2; j < 4092; j++, k++) {
//							languageBufferList.set(k,
//									tempBufferList.get(j + 4092 * i));
//						}
//
//						byte[] checkSum = new byte[4094];
//						for (int k = 1, j = 0; k < 4094; k++, j++) {
//							checkSum[j] = languageBufferList.get(k);
//						}
//
//						languageBufferList.set(4094, checkSum(checkSum));
//						languageBufferList.set(4095, (byte) STOP_BIT);
//
//						byte[] languageBuffer = new byte[4096];
//						for (int n = 0; n < 4096; n++) {
//							languageBuffer[n] = languageBufferList.get(n);
//						}
//
//						try {
//							// mOutputStream.write(languageBuffer);
//							// mOutputStream.flush();
//						} catch (Exception e) {
//							e.printStackTrace();
//							return "BROKEN PIPE ERROR";
//						}
//						quotient--;
//
//					} else {
//						return "CHUNK FAILED";
//					}
//				}
//
//				ackStatus = readAckByte();
//				if (ackStatus.equals("A")) {
//					btMessage = btPrinterErrorMessages();
//				} else if (ackStatus.equals("N")) {
//					return "RESEND THE DATA";
//				}
//
//			} else if (status.equals("LENGTH FAILED")) {
//				return "LENGTH SET FAILED";
//			} else if (status.equals("BROKEN PIPE ERROR")) {
//				return "BROKEN PIPE ERROR";
//			} else {
//				return "ERROR IN APP";
//			}
//		} else {
//			return "FILE NOT FOUND IN SDCARD";
//		}
//		return btMessage;
//	}

    // store template in buffer instead of sdcard for RD-purpose

//    public String fingerprintStoreinBuff(File fingerName,UsbEndpoint writeData, UsbEndpoint readData) {
//
//        enrollTemplateName = fingerName;
//        String status = setLength(2,writeData,readData);
//
//        if (status.equals("LENGTH SUCCESS")) {
//
//            List<Byte> fingerEnrollList = new ArrayList<Byte>();
//
//            fingerEnrollList.add((byte) START_BIT);
//            fingerEnrollList.add((byte) 1);
//            fingerEnrollList.add((byte) 2);
//            fingerEnrollList.add((byte) 1);
//
//            intTo2ByteArray(billSize);
//
//            DataLength2 = billSize;
//            bothDataLen = DataLength1 + DataLength2;
//
//            System.out.println("Bold length : " + DataLength1);
//            System.out.println("Normal length : " + DataLength2);
//
//            byte[] BoldHexData, normalData2, TotalHexData = new byte[bothDataLen];
//
//            BoldHexData = new byte[DataLength1];
//            normalData2 = new byte[DataLength2];
//
//            try {
//
//                fileInputStream = new FileInputStream(BoldData);
//
//                int data1 = fileInputStream.read(BoldHexData);
//
//                for (int i = 0; i < data1; i++) {
//                    TotalHexData[i] = BoldHexData[i];
//                }
//
//                for (int i = 0; i < billSize; i++)
//                    normalData2[i] = normaldataLen[i];
//
//                System.out
//                        .println("Bolddata :" + Arrays.toString(TotalHexData));
//
//                System.out.println("normal data :" + DataLength2);
//                System.out.println("boldlength : " + BoldHexData.length);
//                System.out.println("DataLength2 : " + DataLength2);
//                System.out.println("normalData2 : "
//                        + Arrays.toString(normalData2));
//
//                for (int i = BoldHexData.length, j = 0; j < DataLength2; i++, j++) {
//                    TotalHexData[i] = normalData2[j];
//                }
//
//                System.out.println("Total data Bold & Normal :"
//                        + Arrays.toString(TotalHexData));
//
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            int totalDataLeng = TotalHexData.length;
//
//            String status = setLength(totalDataLeng + 5);
//
//            if (status.equals("LENGTH SUCCESS")) {
//
//                byte totLengthArray[] = intTo4ByteArray(totalDataLeng);
//                byte BoldDataArray[] = intTo4ByteArray(DataLength1);
//
//                System.out.println("Bold array : "
//                        + Arrays.toString(BoldDataArray));
//
//                quotient = totalDataLeng / MAX_CHUNK_VALUE;
//                remainder = totalDataLeng % MAX_CHUNK_VALUE;
//
//                if (remainder != 0) {
//                    int SizeChunk = totalDataLeng
//                            + (MAX_CHUNK_VALUE - remainder);
//                    quotient = SizeChunk / MAX_CHUNK_VALUE;
//                    chunkLength = quotient;
//                }
//
//                tempBufferList.add((byte) 1);
//                byte[] checkSum = new byte[fingerEnrollList.size()];
//                for (int i = 1, k = 0; i < fingerEnrollList.size(); i++, k++) {
//                    checkSum[k] = fingerEnrollList.get(i);
//                }
//
//                fingerEnrollList.add(checkSum(checkSum));
//                fingerEnrollList.add((byte) STOP_BIT);
//
//                byte[] fingerEnrollBuffer = new byte[fingerEnrollList.size()];
//                for (int i = 0; i < fingerEnrollList.size(); i++) {
//                    fingerEnrollBuffer[i] = fingerEnrollList.get(i);
//                }
//
//                try {
//                    // mOutputStream.write(fingerEnrollBuffer);
//                    // mOutputStream.flush();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    return "BROKEN PIPE ERROR";
//                }
//
//                ackStatus = readAckByte();
//                if (ackStatus.equals("A")) {
//                    btMessage = btFingerPrintErrorMessagesforRD();
//                } else if (ackStatus.equals("N")) {
//                    return "RESEND THE DATA";
//                }
//
//            } else if (status.equals("LENGTH FAILED")) {
//                return "LENGTH SET FAILED";
//            } else if (status.equals("BROKEN PIPE ERROR")) {
//                return "BROKEN PIPE ERROR";
//            } else {
//                return "ERROR IN APP";
//            }
//
//        }
//        return btMessage;
//    }


    public String fingerprintEnroll(UsbEndpoint writeData, UsbEndpoint readData, File fingerName) {
        Log.d(TAG, "file path : " + fingerName);
        enrollTemplateName = fingerName;
        String status = setLength(2, writeData, readData);

        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> fingerEnrollList = new ArrayList<Byte>();

            fingerEnrollList.add((byte) START_BIT);
            fingerEnrollList.add((byte) 1);
            fingerEnrollList.add((byte) 2);
            fingerEnrollList.add((byte) 1);

            byte[] checkSum = new byte[fingerEnrollList.size()];
            for (int i = 1, k = 0; i < fingerEnrollList.size(); i++, k++) {
                checkSum[k] = fingerEnrollList.get(i);
            }

            fingerEnrollList.add(checkSum(checkSum));
            fingerEnrollList.add((byte) STOP_BIT);

            byte[] fingerEnrollBuffer = new byte[fingerEnrollList.size()];
            for (int i = 0; i < fingerEnrollList.size(); i++) {
                fingerEnrollBuffer[i] = fingerEnrollList.get(i);
            }

            try {
                usbCommunication.bulkTransfer(writeData, fingerEnrollBuffer, fingerEnrollBuffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btFingerPrintErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }
        return btMessage;
    }

    public String fingerprintSaveTemplate(UsbEndpoint writeData, UsbEndpoint readData, File savedTemplateFile) {

        List<Byte> fingerSaveTemplateList = new ArrayList<Byte>();

        if (savedTemplateFile.exists()) {
            Log.d(TAG, "saved file path : " + savedTemplateFile + "file size : " + savedTemplateFile.getAbsolutePath());
            FileInputStream fileInputStream = null;

            byte[] fingerData = new byte[(int) savedTemplateFile.length()];
            try {
                fileInputStream = new FileInputStream(savedTemplateFile);
                try {
                    fileInputStream.read(fingerData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            int templateLength = fingerData.length;
            Log.d(TAG, "template len: " + templateLength);

            String status = setLength(templateLength + 4, writeData, readData);

            if (status.equals("LENGTH SUCCESS")) {

                fingerSaveTemplateList.add((byte) START_BIT);
                fingerSaveTemplateList.add((byte) 1);
                fingerSaveTemplateList.add((byte) 2);
                fingerSaveTemplateList.add((byte) 2);

                int templateSize = fingerData.length;
                byte templateLengthArray[] = intTo2ByteArray(templateSize);

                fingerSaveTemplateList.add(templateLengthArray[0]);
                fingerSaveTemplateList.add(templateLengthArray[1]);

                for (int j = 0; j < templateSize; j++)
                    fingerSaveTemplateList.add(fingerData[j]);

                byte[] checkSum = new byte[fingerSaveTemplateList.size()];
                for (int i = 1, k = 0; i < fingerSaveTemplateList.size(); i++, k++) {
                    checkSum[k] = fingerSaveTemplateList.get(i);
                }

                fingerSaveTemplateList.add(checkSum(checkSum));
                fingerSaveTemplateList.add((byte) STOP_BIT);

                byte[] fingerSaveTemplateBuffer = new byte[fingerSaveTemplateList
                        .size()];
                for (int i = 0; i < fingerSaveTemplateList.size(); i++) {
                    fingerSaveTemplateBuffer[i] = fingerSaveTemplateList.get(i);
                }
                System.out.println("fulltron template : "
                        + new String(fingerSaveTemplateBuffer).toString());

                try {
                    usbCommunication.bulkTransfer(writeData, fingerSaveTemplateBuffer, fingerSaveTemplateBuffer.length, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                    return "BROKEN PIPE ERROR";
                }

                ackStatus = readAckByte(readData);
                if (ackStatus.equals("A")) {
                    btMessage = btFingerPrintErrorMessages(readData);
                } else if (ackStatus.equals("N")) {
                    return "RESEND THE DATA";
                }

            } else if (status.equals("LENGTH FAILED")) {
                return "LENGTH SET FAILED";
            } else if (status.equals("BROKEN PIPE ERROR")) {
                return "BROKEN PIPE ERROR";
            } else {
                return "ERROR IN APP";
            }
        } else {
            return "FILE NOT FOUND IN SDCARD";
        }
        return btMessage;
    }

    public String fingerprintVerify(UsbEndpoint writeData, UsbEndpoint readData) {

        String status = setLength(2, writeData, readData);

        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> fingerVerifyList = new ArrayList<Byte>();

            fingerVerifyList.add((byte) START_BIT);
            fingerVerifyList.add((byte) 1);
            fingerVerifyList.add((byte) 2);
            fingerVerifyList.add((byte) 3);

            byte[] checkSum = new byte[fingerVerifyList.size()];
            for (int i = 1, k = 0; i < fingerVerifyList.size(); i++, k++) {
                checkSum[k] = fingerVerifyList.get(i);
            }

            fingerVerifyList.add(checkSum(checkSum));
            fingerVerifyList.add((byte) STOP_BIT);

            byte[] fingerVerifyBuffer = new byte[fingerVerifyList.size()];
            for (int i = 0; i < fingerVerifyList.size(); i++) {
                fingerVerifyBuffer[i] = fingerVerifyList.get(i);
            }

            try {
                usbCommunication.bulkTransfer(writeData, fingerVerifyBuffer, fingerVerifyBuffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btFingerPrintErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }
        return btMessage;
    }

    public void fingerprintEnrollment(UsbEndpoint writeData, UsbEndpoint readData, File fileName, int templateType,
                                      int timeout, int fingerScanCount, int imageType, int multipleEnroll) {

        imageTypeFilename = fileName;
        imageTypeValue = imageType;
        enrollScanCount = fingerScanCount;

        String status = setLength(7, writeData, readData);

        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> fingerEnrollList = new ArrayList<Byte>();

            fingerEnrollList.add((byte) START_BIT);
            fingerEnrollList.add((byte) 1);
            fingerEnrollList.add((byte) 2);
            fingerEnrollList.add((byte) 5);
            fingerEnrollList.add((byte) templateType);
            fingerEnrollList.add((byte) timeout);
            fingerEnrollList.add((byte) fingerScanCount);
            fingerEnrollList.add((byte) imageType);
            fingerEnrollList.add((byte) multipleEnroll);

            byte[] checkSum = new byte[fingerEnrollList.size()];
            for (int i = 1, k = 0; i < fingerEnrollList.size(); i++, k++) {
                checkSum[k] = fingerEnrollList.get(i);
            }

            fingerEnrollList.add(checkSum(checkSum));
            fingerEnrollList.add((byte) STOP_BIT);

            byte[] fingerEnrollBuffer = new byte[fingerEnrollList.size()];
            for (int i = 0; i < fingerEnrollList.size(); i++) {
                fingerEnrollBuffer[i] = fingerEnrollList.get(i);
            }

            try {
                usbCommunication.bulkTransfer(writeData, fingerEnrollBuffer, fingerEnrollBuffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                btCallback.onBTCommunicationFailed("BROKEN PIPE ERROR");
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btFingerPrintErrorCallbackMessages(writeData, readData);
            } else if (ackStatus.equals("N")) {
                btCallback.onInvalidData("RESEND THE DATA");
            }

        } else if (status.equals("LENGTH FAILED")) {
            btCallback.onLengthSetFailed("LENGTH SET FAILED");

        } else if (status.equals("BROKEN PIPE ERROR")) {
            btCallback.onBTCommunicationFailed("BROKEN PIPE ERROR");

        }

    }

    public void fingerprintMultipleTemplatesVerification(UsbEndpoint writeData, UsbEndpoint readData, int timeout,
                                                         int fingerTemplateCount, int templateType, byte[] templatesSize,
                                                         byte[] templatesData) {

        String status = setLength(5 + templatesSize.length
                + templatesData.length, writeData, readData);

        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> fingerVerifyList = new ArrayList<Byte>();

            fingerVerifyList.add((byte) START_BIT);
            fingerVerifyList.add((byte) 1);
            fingerVerifyList.add((byte) 2);
            fingerVerifyList.add((byte) 6);
            fingerVerifyList.add((byte) timeout);
            fingerVerifyList.add((byte) fingerTemplateCount);
            fingerVerifyList.add((byte) templateType);

            for (int i = 0; i < templatesSize.length; i++)
                fingerVerifyList.add(templatesSize[i]);

            for (int j = 0; j < templatesData.length; j++)
                fingerVerifyList.add(templatesData[j]);

            byte[] checkSum = new byte[fingerVerifyList.size()];
            for (int i = 1, k = 0; i < fingerVerifyList.size(); i++, k++) {
                checkSum[k] = fingerVerifyList.get(i);
            }

            fingerVerifyList.add(checkSum(checkSum));
            fingerVerifyList.add((byte) STOP_BIT);

            byte[] fingerVerifyBuffer = new byte[fingerVerifyList.size()];
            for (int i = 0; i < fingerVerifyList.size(); i++) {
                fingerVerifyBuffer[i] = fingerVerifyList.get(i);
            }

            try {
                usbCommunication.bulkTransfer(writeData, fingerVerifyBuffer, fingerVerifyBuffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                btCallback.onBTCommunicationFailed("BROKEN PIPE ERROR");
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btFingerPrintErrorCallbackMessages(writeData, readData);
            } else if (ackStatus.equals("N")) {
                btCallback.onInvalidData("RESEND THE DATA");
            }
        } else if (status.equals("LENGTH FAILED")) {
            btCallback.onLengthSetFailed("LENGTH SET FAILED");

        } else if (status.equals("BROKEN PIPE ERROR")) {
            btCallback.onBTCommunicationFailed("BROKEN PIPE ERROR");

        }

    }

    public String generate_Licence(UsbEndpoint writeData, UsbEndpoint readData) {

        String status = setLength(2, writeData, readData);

        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> fingerVerifyList = new ArrayList<Byte>();

            fingerVerifyList.add((byte) START_BIT);
            fingerVerifyList.add((byte) 1);
            fingerVerifyList.add((byte) 2);
            fingerVerifyList.add((byte) 7);

            byte[] checkSum = new byte[fingerVerifyList.size()];
            for (int i = 1, k = 0; i < fingerVerifyList.size(); i++, k++) {
                checkSum[k] = fingerVerifyList.get(i);
            }

            fingerVerifyList.add(checkSum(checkSum));
            fingerVerifyList.add((byte) STOP_BIT);

            byte[] fingerVerifyBuffer = new byte[fingerVerifyList.size()];
            for (int i = 0; i < fingerVerifyList.size(); i++) {
                fingerVerifyBuffer[i] = fingerVerifyList.get(i);
            }

            try {
                usbCommunication.bulkTransfer(writeData, fingerVerifyBuffer, fingerVerifyBuffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btFingerPrintErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }
        return btMessage;
    }

    public String fingerprintBMPImage(UsbEndpoint writeData, UsbEndpoint readData, File fingerImageName) {

        String status = setLength(1, writeData, readData);

        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> fingerImageList = new ArrayList<Byte>();

            fingerImageList.add((byte) START_BIT);
            fingerImageList.add((byte) 1);
            fingerImageList.add((byte) 8);

            byte[] checkSum = new byte[fingerImageList.size()];
            for (int i = 1, k = 0; i < fingerImageList.size(); i++, k++) {
                checkSum[k] = fingerImageList.get(i);
            }

            fingerImageList.add(checkSum(checkSum));
            fingerImageList.add((byte) STOP_BIT);

            byte[] fingerImageBuffer = new byte[fingerImageList.size()];
            for (int i = 0; i < fingerImageList.size(); i++) {
                fingerImageBuffer[i] = fingerImageList.get(i);
            }

            try {
                usbCommunication.bulkTransfer(writeData, fingerImageBuffer, fingerImageBuffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            btMessage = getFingerBMPImage(fingerImageName, writeData, readData);

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }
        return btMessage;
    }

    public String fingerprintRAWImage(UsbEndpoint writeData, UsbEndpoint readData, File fingerImageName) {

        String status = setLength(1, writeData, readData);

        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> fingerImageList = new ArrayList<Byte>();

            fingerImageList.add((byte) START_BIT);
            fingerImageList.add((byte) 1);
            fingerImageList.add((byte) 9);

            byte[] checkSum = new byte[fingerImageList.size()];
            for (int i = 1, k = 0; i < fingerImageList.size(); i++, k++) {
                checkSum[k] = fingerImageList.get(i);
            }

            fingerImageList.add(checkSum(checkSum));
            fingerImageList.add((byte) STOP_BIT);

            byte[] fingerImageBuffer = new byte[fingerImageList.size()];
            for (int i = 0; i < fingerImageList.size(); i++) {
                fingerImageBuffer[i] = fingerImageList.get(i);
            }

            try {
                usbCommunication.bulkTransfer(writeData, fingerImageBuffer, fingerImageBuffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            btMessage = getFingerRAWImage(fingerImageName, writeData, readData);

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }
        return btMessage;
    }

    public String ifdCardSelection(UsbEndpoint writeData, UsbEndpoint readData, int cardNumber, int cardOperation) {

        String status = setLength(4, writeData, readData);

        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> ifdCardList = new ArrayList<Byte>();

            ifdCardList.add((byte) START_BIT);
            ifdCardList.add((byte) 1);
            ifdCardList.add((byte) 3);
            ifdCardList.add((byte) 1);
            ifdCardList.add((byte) cardNumber);
            ifdCardList.add((byte) cardOperation);

            byte[] checkSum = new byte[ifdCardList.size()];
            for (int i = 1, k = 0; i < ifdCardList.size(); i++, k++) {
                checkSum[k] = ifdCardList.get(i);
            }

            ifdCardList.add(checkSum(checkSum));
            ifdCardList.add((byte) STOP_BIT);

            byte[] ifdCardBuffer = new byte[ifdCardList.size()];
            for (int i = 0; i < ifdCardList.size(); i++) {
                ifdCardBuffer[i] = ifdCardList.get(i);
            }

            try {
                usbCommunication.bulkTransfer(writeData, ifdCardBuffer, ifdCardBuffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btIfdErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }
        return btMessage;
    }

    public String ifdCardCommandSend(UsbEndpoint writeData, UsbEndpoint readData, int commandLength, byte[][] commandData) {

        String status = setLength(commandLength + 3, writeData, readData);

        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> ifdCardCommandSend = new ArrayList<Byte>();

            ifdCardCommandSend.add((byte) START_BIT);
            ifdCardCommandSend.add((byte) 1);
            ifdCardCommandSend.add((byte) 3);
            ifdCardCommandSend.add((byte) 2);
            ifdCardCommandSend.add((byte) commandLength);
            for (int i = 0; i < commandLength; i++)
                ifdCardCommandSend.add(commandData[0][i]);

            byte[] checkSum = new byte[ifdCardCommandSend.size()];
            for (int i = 1, k = 0; i < ifdCardCommandSend.size(); i++, k++) {
                checkSum[k] = ifdCardCommandSend.get(i);
            }

            ifdCardCommandSend.add(checkSum(checkSum));
            ifdCardCommandSend.add((byte) STOP_BIT);

            byte[] ifdCardCommandSendBuffer = new byte[ifdCardCommandSend
                    .size()];
            for (int i = 0; i < ifdCardCommandSend.size(); i++) {
                ifdCardCommandSendBuffer[i] = ifdCardCommandSend.get(i);
            }

            try {
                usbCommunication.bulkTransfer(writeData, ifdCardCommandSendBuffer, ifdCardCommandSendBuffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btIfdErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }
        return btMessage;
    }

    public String ifdWriteRecord(UsbEndpoint writeData, UsbEndpoint readData, int commandLength, byte[][] commandData,
                                 String data) {
        Log.d(TAG, "Enter into length");

        String status = setLength(4 + commandLength + data.length(), writeData, readData);

        if (status.equals("LENGTH SUCCESS")) {

            Log.d(TAG, "After length got success");

            List<Byte> ifdWriteRecordList = new ArrayList<Byte>();

            ifdWriteRecordList.add((byte) START_BIT);
            ifdWriteRecordList.add((byte) 1);
            ifdWriteRecordList.add((byte) 3);
            ifdWriteRecordList.add((byte) 2);

            byte writeDataBytes[] = data.getBytes();
            int writeDataLength = writeDataBytes.length;

            ifdWriteRecordList.add((byte) (writeDataLength + 5));

            for (int i = 0; i < commandLength; i++)
                ifdWriteRecordList.add(commandData[1][i]);

            ifdWriteRecordList.add((byte) writeDataLength);

            for (int i = 0; i < writeDataLength; i++)
                ifdWriteRecordList.add(writeDataBytes[i]);

            byte[] checkSum = new byte[ifdWriteRecordList.size()];
            for (int i = 1, k = 0; i < ifdWriteRecordList.size(); i++, k++) {
                checkSum[k] = ifdWriteRecordList.get(i);
            }

            ifdWriteRecordList.add(checkSum(checkSum));
            ifdWriteRecordList.add((byte) STOP_BIT);

            byte[] ifdWriteRecordBuffer = new byte[ifdWriteRecordList.size()];
            for (int i = 0; i < ifdWriteRecordList.size(); i++) {
                ifdWriteRecordBuffer[i] = ifdWriteRecordList.get(i);
            }

            try {
                Log.d(TAG, "writing main bytes");
                usbCommunication.bulkTransfer(writeData, ifdWriteRecordBuffer, ifdWriteRecordBuffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                Log.d(TAG, "Ack A got");
                btMessage = btIfdErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }
        return btMessage;
    }

    public String ifdReadRecord(UsbEndpoint writeData, UsbEndpoint readData, int commandLength, byte[][] commandData,
                                int recordNumber) {

        String status = setLength(3 + commandLength, writeData, readData);

        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> ifdReadRecordList = new ArrayList<Byte>();

            ifdReadRecordList.add((byte) START_BIT);
            ifdReadRecordList.add((byte) 1);
            ifdReadRecordList.add((byte) 3);
            ifdReadRecordList.add((byte) 2);
            ifdReadRecordList.add((byte) commandLength);

            commandData[4][2] = 0;
            commandData[4][3] = (byte) recordNumber;

            for (int i = 0; i < commandLength; i++)
                ifdReadRecordList.add(commandData[4][i]);

            byte[] checkSum = new byte[ifdReadRecordList.size()];
            for (int i = 1, k = 0; i < ifdReadRecordList.size(); i++, k++) {
                checkSum[k] = ifdReadRecordList.get(i);
            }

            ifdReadRecordList.add(checkSum(checkSum));
            ifdReadRecordList.add((byte) STOP_BIT);

            byte[] ifdReadRecordBuffer = new byte[ifdReadRecordList.size()];
            for (int i = 0; i < ifdReadRecordList.size(); i++) {
                ifdReadRecordBuffer[i] = ifdReadRecordList.get(i);
            }

            try {
                usbCommunication.bulkTransfer(writeData, ifdReadRecordBuffer, ifdReadRecordBuffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btIfdErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }
        return btMessage;
    }

    public String ifdNumberOfRecords(UsbEndpoint writeData, UsbEndpoint readData, int commandLength, byte[][] commandData) {

        String status = setLength(3 + commandLength, writeData, readData);

        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> ifdNumberOfRecordsList = new ArrayList<Byte>();

            ifdNumberOfRecordsList.add((byte) START_BIT);
            ifdNumberOfRecordsList.add((byte) 1);
            ifdNumberOfRecordsList.add((byte) 3);
            ifdNumberOfRecordsList.add((byte) 2);
            ifdNumberOfRecordsList.add((byte) commandLength);

            for (int i = 0; i < commandLength; i++)
                ifdNumberOfRecordsList.add(commandData[5][i]);

            byte[] checkSum = new byte[ifdNumberOfRecordsList.size()];
            for (int i = 1, k = 0; i < ifdNumberOfRecordsList.size(); i++, k++) {
                checkSum[k] = ifdNumberOfRecordsList.get(i);
            }

            ifdNumberOfRecordsList.add(checkSum(checkSum));
            ifdNumberOfRecordsList.add((byte) STOP_BIT);

            byte[] ifdNumberOfRecordsBuffer = new byte[ifdNumberOfRecordsList
                    .size()];
            for (int i = 0; i < ifdNumberOfRecordsList.size(); i++) {
                ifdNumberOfRecordsBuffer[i] = ifdNumberOfRecordsList.get(i);
            }

            try {
                usbCommunication.bulkTransfer(writeData, ifdNumberOfRecordsBuffer, ifdNumberOfRecordsBuffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btIfdErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }
        return btMessage;
    }

    public String ifdFormatCards(UsbEndpoint writeData, UsbEndpoint readData, int commandLength, byte[][] commandData) {

        String status = setLength(3 + commandLength, writeData, readData);

        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> ifdFormatCardsList = new ArrayList<Byte>();

            ifdFormatCardsList.add((byte) START_BIT);
            ifdFormatCardsList.add((byte) 1);
            ifdFormatCardsList.add((byte) 3);
            ifdFormatCardsList.add((byte) 2);
            ifdFormatCardsList.add((byte) commandLength);

            for (int i = 0; i < commandLength; i++)
                ifdFormatCardsList.add(commandData[6][i]);

            byte[] checkSum = new byte[ifdFormatCardsList.size()];
            for (int i = 1, k = 0; i < ifdFormatCardsList.size(); i++, k++) {
                checkSum[k] = ifdFormatCardsList.get(i);
            }

            ifdFormatCardsList.add(checkSum(checkSum));
            ifdFormatCardsList.add((byte) STOP_BIT);

            byte[] ifdFormatCardsBuffer = new byte[ifdFormatCardsList.size()];
            for (int i = 0; i < ifdFormatCardsList.size(); i++) {
                ifdFormatCardsBuffer[i] = ifdFormatCardsList.get(i);
            }

            try {
                usbCommunication.bulkTransfer(writeData, ifdFormatCardsBuffer, ifdFormatCardsBuffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btIfdErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }
        return btMessage;
    }

    public String rfidWrite(UsbEndpoint writeData, UsbEndpoint readData, int cardNumber, int athenticationBlockAddress,
                            int KEYA, int writeBlockAddress, byte[] rfidKey,
                            String rfidWriteData) {

        if (rfidWriteData.length() < 16) {
            String status = setLength(rfidKey.length + rfidWriteData.length()
                    + 7, writeData, readData);

            if (status.equals("LENGTH SUCCESS")) {

                List<Byte> rfidWriteList = new ArrayList<Byte>();

                rfidWriteList.add((byte) START_BIT);
                rfidWriteList.add((byte) 1);
                rfidWriteList.add((byte) 7);
                rfidWriteList.add((byte) 1);
                rfidWriteList.add((byte) cardNumber);
                rfidWriteList.add((byte) athenticationBlockAddress);
                rfidWriteList.add((byte) KEYA);
                rfidWriteList.add((byte) writeBlockAddress);
                rfidWriteList.add((byte) rfidWriteData.length());

                byte rfidWriteDataBytes[] = rfidWriteData.getBytes();

                for (int i = 0; i < rfidKey.length; i++)
                    rfidWriteList.add(rfidKey[i]);

                for (int i = 0; i < rfidWriteDataBytes.length; i++)
                    rfidWriteList.add(rfidWriteDataBytes[i]);

                byte[] checkSum = new byte[rfidWriteList.size()];
                for (int i = 1, k = 0; i < rfidWriteList.size(); i++, k++) {
                    checkSum[k] = rfidWriteList.get(i);
                }

                rfidWriteList.add(checkSum(checkSum));
                rfidWriteList.add((byte) STOP_BIT);

                byte[] rfidWriteBuffer = new byte[rfidWriteList.size()];
                for (int i = 0; i < rfidWriteList.size(); i++) {
                    rfidWriteBuffer[i] = rfidWriteList.get(i);
                }

                try {
                    usbCommunication.bulkTransfer(writeData, rfidWriteBuffer, rfidWriteBuffer.length, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                    return "BROKEN PIPE ERROR";
                }

                ackStatus = readAckByte(readData);
                if (ackStatus.equals("A")) {
                    btMessage = btRfidErrorMessages(readData);
                } else if (ackStatus.equals("N")) {
                    return "RESEND THE DATA";
                }

            } else if (status.equals("LENGTH FAILED")) {
                return "LENGTH SET FAILED";
            } else if (status.equals("BROKEN PIPE ERROR")) {
                return "BROKEN PIPE ERROR";
            } else {
                return "ERROR IN APP";
            }
        } else {
            return "DATA SHOULD NOT EXCEED 16";
        }
        return btMessage;
    }

    public String rfidRead(UsbEndpoint writeData, UsbEndpoint readData, int cardNumber, int athenticationBlockAddress,
                           int KEYA, int readBlockAddress, byte[] rfidKey) {

        String status = setLength(rfidKey.length + 6, writeData, readData);

        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> rfidReadList = new ArrayList<Byte>();

            rfidReadList.add((byte) START_BIT);
            rfidReadList.add((byte) 1);
            rfidReadList.add((byte) 7);
            rfidReadList.add((byte) 2);
            rfidReadList.add((byte) cardNumber);
            rfidReadList.add((byte) athenticationBlockAddress);
            rfidReadList.add((byte) KEYA);
            rfidReadList.add((byte) readBlockAddress);

            for (int i = 0; i < rfidKey.length; i++)
                rfidReadList.add(rfidKey[i]);

            byte[] checkSum = new byte[rfidReadList.size()];
            for (int i = 1, k = 0; i < rfidReadList.size(); i++, k++) {
                checkSum[k] = rfidReadList.get(i);
            }

            rfidReadList.add(checkSum(checkSum));
            rfidReadList.add((byte) STOP_BIT);

            byte[] rfidReadListBuffer = new byte[rfidReadList.size()];
            for (int i = 0; i < rfidReadList.size(); i++) {
                rfidReadListBuffer[i] = rfidReadList.get(i);
            }

            try {
                usbCommunication.bulkTransfer(writeData, rfidReadListBuffer, rfidReadListBuffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btRfidErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }
        return btMessage;
    }

    public String btDeviceSamplePrint(UsbEndpoint writeData, UsbEndpoint readData) {

        String status = setLength(2, writeData, readData);

        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> deviceTestPrintList = new ArrayList<Byte>();

            deviceTestPrintList.add((byte) START_BIT);
            deviceTestPrintList.add((byte) 1);
            deviceTestPrintList.add((byte) 1);
            deviceTestPrintList.add((byte) 13);

            byte[] checkSum = new byte[deviceTestPrintList.size()];
            for (int i = 1, k = 0; i < deviceTestPrintList.size(); i++, k++) {
                checkSum[k] = deviceTestPrintList.get(i);
            }

            deviceTestPrintList.add(checkSum(checkSum));
            deviceTestPrintList.add((byte) STOP_BIT);

            byte[] deviceTestPrintListBuffer = new byte[deviceTestPrintList
                    .size()];
            for (int i = 0; i < deviceTestPrintList.size(); i++) {
                deviceTestPrintListBuffer[i] = deviceTestPrintList.get(i);
            }

            try {
                usbCommunication.bulkTransfer(writeData, deviceTestPrintListBuffer, deviceTestPrintListBuffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btPrinterErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }
        return btMessage;
    }

    public String devInfo(UsbEndpoint writeData, UsbEndpoint readData) {

        String status = setLength(2, writeData, readData);

        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> devInfoList = new ArrayList<Byte>();

            devInfoList.add((byte) START_BIT);
            devInfoList.add((byte) 1);
            devInfoList.add((byte) 5);
            devInfoList.add((byte) 5);

            byte[] checkSum = new byte[devInfoList.size()];
            for (int i = 1, k = 0; i < devInfoList.size(); i++, k++) {
                checkSum[k] = devInfoList.get(i);
            }

            devInfoList.add(checkSum(checkSum));
            devInfoList.add((byte) STOP_BIT);

            byte[] devInfoBuffer = new byte[devInfoList.size()];
            for (int i = 0; i < devInfoList.size(); i++) {
                devInfoBuffer[i] = devInfoList.get(i);
            }

            try {
                usbCommunication.bulkTransfer(writeData, devInfoBuffer, devInfoBuffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btDeviceErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }
        return btMessage;
    }

    // battery status
    public String battery_Status_Info(UsbEndpoint writeData, UsbEndpoint readData) {

        String status = setLength(1, writeData, readData);

        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> devInfoList = new ArrayList<Byte>();

            devInfoList.add((byte) START_BIT);
            devInfoList.add((byte) 1);
            devInfoList.add((byte) 16);
            // devInfoList.add((byte) 1);

            byte[] checkSum = new byte[devInfoList.size()];
            for (int i = 1, k = 0; i < devInfoList.size(); i++, k++) {
                checkSum[k] = devInfoList.get(i);
            }

            devInfoList.add(checkSum(checkSum));
            devInfoList.add((byte) STOP_BIT);

            byte[] devInfoBuffer = new byte[devInfoList.size()];
            for (int i = 0; i < devInfoList.size(); i++) {
                devInfoBuffer[i] = devInfoList.get(i);
            }

            try {
                usbCommunication.bulkTransfer(writeData, devInfoBuffer, devInfoBuffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                System.out.println("AAAAAAA : " + ackStatus);
                btMessage = btDeviceErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }
        System.out.println("BTTTTT : " + btMessage);
        return btMessage;
    }

    // hardware Configuration

    public String configuration_97BT(UsbEndpoint writeData, UsbEndpoint readData, String config_Value) {

        String status = setLength(3 + config_Value.length(), writeData, readData);

        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> configList = new ArrayList<Byte>();

            configList.add((byte) START_BIT);
            configList.add((byte) 1);
            configList.add((byte) 18);
            byte configData[] = config_Value.getBytes();
            int config_Value_leng = configData.length;
            byte configLengthArray[] = intTo2ByteArray(config_Value_leng);

            configList.add(configLengthArray[0]);
            configList.add(configLengthArray[1]);

            for (int j = 0; j < config_Value_leng; j++)
                configList.add(configData[j]);

            byte[] checkSum = new byte[configList.size()];
            for (int i = 1, k = 0; i < configList.size(); i++, k++) {
                checkSum[k] = configList.get(i);
            }

            configList.add(checkSum(checkSum));
            configList.add((byte) STOP_BIT);

            byte[] configInfoBuffer = new byte[configList.size()];
            for (int i = 0; i < configList.size(); i++) {
                configInfoBuffer[i] = configList.get(i);
            }

            System.out.println("Total buffer : "
                    + Arrays.toString(configInfoBuffer));
            try {
                usbCommunication.bulkTransfer(writeData, configInfoBuffer, configInfoBuffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btDeviceErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }
        System.out.println("BTTTTT : " + btMessage);
        return btMessage;
    }

    // RHMS update
    public String rhms_Update(UsbEndpoint writeData, UsbEndpoint readData) {

        String status = setLength(1, writeData, readData);

        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> devInfoList = new ArrayList<Byte>();

            devInfoList.add((byte) START_BIT);
            devInfoList.add((byte) 1);
            devInfoList.add((byte) 19);
            // devInfoList.add((byte) 1);

            byte[] checkSum = new byte[devInfoList.size()];
            for (int i = 1, k = 0; i < devInfoList.size(); i++, k++) {
                checkSum[k] = devInfoList.get(i);
            }

            devInfoList.add(checkSum(checkSum));
            devInfoList.add((byte) STOP_BIT);

            byte[] devInfoBuffer = new byte[devInfoList.size()];
            for (int i = 0; i < devInfoList.size(); i++) {
                devInfoBuffer[i] = devInfoList.get(i);
            }

            try {
                usbCommunication.bulkTransfer(writeData, devInfoBuffer, devInfoBuffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btDeviceErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }
        System.out.println("BTTTTT : " + btMessage);
        return btMessage;
    }

    // get BluetoothmacID of mobile Device
    public String blutooth_MacID(UsbEndpoint writeData, UsbEndpoint readData) {

        String status = setLength(1, writeData, readData);

        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> devInfoList = new ArrayList<Byte>();

            devInfoList.add((byte) START_BIT);
            devInfoList.add((byte) 1);
            devInfoList.add((byte) 20);
            // devInfoList.add((byte) 1);

            byte[] checkSum = new byte[devInfoList.size()];
            for (int i = 1, k = 0; i < devInfoList.size(); i++, k++) {
                checkSum[k] = devInfoList.get(i);
            }

            devInfoList.add(checkSum(checkSum));
            devInfoList.add((byte) STOP_BIT);

            byte[] devInfoBuffer = new byte[devInfoList.size()];
            for (int i = 0; i < devInfoList.size(); i++) {
                devInfoBuffer[i] = devInfoList.get(i);
            }

            try {
                usbCommunication.bulkTransfer(writeData, devInfoBuffer, devInfoBuffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btDeviceErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }
        System.out.println("BTTTTT : " + btMessage);
        return btMessage;
    }

    // vendor&product id
    public String product_Vendor_ID(UsbEndpoint writeData, UsbEndpoint readData) {

        String status = setLength(1, writeData, readData);

        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> devInfoList = new ArrayList<Byte>();

            devInfoList.add((byte) START_BIT);
            devInfoList.add((byte) 1);
            devInfoList.add((byte) 17);
            // devInfoList.add((byte) 1);

            byte[] checkSum = new byte[devInfoList.size()];
            for (int i = 1, k = 0; i < devInfoList.size(); i++, k++) {
                checkSum[k] = devInfoList.get(i);
            }

            devInfoList.add(checkSum(checkSum));
            devInfoList.add((byte) STOP_BIT);

            byte[] devInfoBuffer = new byte[devInfoList.size()];
            for (int i = 0; i < devInfoList.size(); i++) {
                devInfoBuffer[i] = devInfoList.get(i);
            }

            try {
                usbCommunication.bulkTransfer(writeData, devInfoBuffer, devInfoBuffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btDeviceErrorMessages(readData);

            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }
        return btMessage;
    }

    public String appLoad(UsbEndpoint writeData, UsbEndpoint readData) {

        String status = setLength(1, writeData, readData);

        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> appLoadList = new ArrayList<Byte>();

            appLoadList.add((byte) START_BIT);
            appLoadList.add((byte) 1);
            appLoadList.add((byte) 6);
            // appLoadList.add((byte) 1);

            byte[] checkSum = new byte[appLoadList.size()];
            for (int i = 1, k = 0; i < appLoadList.size(); i++, k++) {
                checkSum[k] = appLoadList.get(i);
            }

            appLoadList.add(checkSum(checkSum));
            appLoadList.add((byte) STOP_BIT);

            byte[] appLoadBuffer = new byte[appLoadList.size()];
            for (int i = 0; i < appLoadList.size(); i++) {
                appLoadBuffer[i] = appLoadList.get(i);
            }

            try {
                usbCommunication.bulkTransfer(writeData, appLoadBuffer, appLoadBuffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btDeviceErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }
        return btMessage;
    }

    public String get_USB_Data(UsbEndpoint writeData, UsbEndpoint readData, int timeOut) {

        String status = setLength(2, writeData, readData);
        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> rd_DiscoveryList = new ArrayList<Byte>();

            rd_DiscoveryList.add((byte) START_BIT);
            rd_DiscoveryList.add((byte) 1);
            rd_DiscoveryList.add((byte) 13);
            rd_DiscoveryList.add((byte) timeOut);

            byte[] checkSum = new byte[rd_DiscoveryList.size()];
            for (int i = 1, k = 0; i < rd_DiscoveryList.size(); i++, k++) {
                checkSum[k] = rd_DiscoveryList.get(i);
            }

            rd_DiscoveryList.add(checkSum(checkSum));
            rd_DiscoveryList.add((byte) STOP_BIT);

            byte[] rd_Buffer = new byte[rd_DiscoveryList.size()];
            for (int i = 0; i < rd_DiscoveryList.size(); i++) {
                rd_Buffer[i] = rd_DiscoveryList.get(i);
            }

            try {
                usbCommunication.bulkTransfer(writeData, rd_Buffer, rd_Buffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btPrinterErrorMessages(readData);

            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            } else if (ackStatus.equals("DF")) {
                btMessage = btPrinterErrorMessages(readData);

            } else {
                return ackStatus;
            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }

        return btMessage;
    }

    public String rd_Discovery(UsbEndpoint writeData, UsbEndpoint readData) {

        String status = setLength(3, writeData, readData);
        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> rd_DiscoveryList = new ArrayList<Byte>();

            rd_DiscoveryList.add((byte) START_BIT);
            rd_DiscoveryList.add((byte) 1);
            rd_DiscoveryList.add((byte) 12);
            rd_DiscoveryList.add((byte) 1);
            rd_DiscoveryList.add((byte) 0);// debug

            byte[] checkSum = new byte[rd_DiscoveryList.size()];
            for (int i = 1, k = 0; i < rd_DiscoveryList.size(); i++, k++) {
                checkSum[k] = rd_DiscoveryList.get(i);
            }

            rd_DiscoveryList.add(checkSum(checkSum));
            rd_DiscoveryList.add((byte) STOP_BIT);

            byte[] rd_Buffer = new byte[rd_DiscoveryList.size()];
            for (int i = 0; i < rd_DiscoveryList.size(); i++) {
                rd_Buffer[i] = rd_DiscoveryList.get(i);
            }
            System.out.println("Final data to send : " + rd_Buffer.length);

            try {
                usbCommunication.bulkTransfer(writeData, rd_Buffer, rd_Buffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btPrinterErrorMessages(readData);

            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            } else if (ackStatus.equals("DF")) {
                btMessage = btPrinterErrorMessages(readData);

            } else {
                System.out.println("IN else");
                return ackStatus;
            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }

        return btMessage;
    }

    public String rd_Fp_Capture(UsbEndpoint writeData, UsbEndpoint readData, String xml_Data) {
        int length_of_pidopt = xml_Data.length();
        System.out.println("length of pid : " + xml_Data.length());

        String fp_Head = "CAPTURE /rd/capture HTTP/1.1\nAccept: text/xml\nContent-Type: text/xml\n"
                + "User-Agent: axios/0.15.3\nHOST: 127.0.0.1:11100\n"
                + "Connection: close\nContent-Length:"
                + length_of_pidopt
                + "\n";
        System.out.println("xml pid  : " + xml_Data);
//        System.out.println("before adding  : " + fp_Head);
        fp_Head += xml_Data;
        System.out.println("total xml : " + fp_Head);
        String status = setLength(fp_Head.length() + 4, writeData, readData);
        System.out.print("status: "+status);
        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> rd_FpCaptureList = new ArrayList<Byte>();

            rd_FpCaptureList.add((byte) START_BIT);
            rd_FpCaptureList.add((byte) 1);
            rd_FpCaptureList.add((byte) 12);
            rd_FpCaptureList.add((byte) 2);

            byte xmltextArray[] = fp_Head.getBytes();
            int length = xmltextArray.length;

            byte xmltextLength[] = intTo2ByteArray(length);

            rd_FpCaptureList.add(xmltextLength[0]);
            rd_FpCaptureList.add(xmltextLength[1]);

            for (int j = 0; j < length; j++) {
                rd_FpCaptureList.add(xmltextArray[j]);

            }

            byte[] checkSum = new byte[rd_FpCaptureList.size()];
            for (int i = 1, k = 0; i < rd_FpCaptureList.size(); i++, k++) {
                checkSum[k] = rd_FpCaptureList.get(i);
            }

            rd_FpCaptureList.add(checkSum(checkSum));

            rd_FpCaptureList.add((byte) STOP_BIT);

            byte[] rd_Buffer = new byte[rd_FpCaptureList.size()];
            for (int i = 0; i < rd_FpCaptureList.size(); i++) {
                rd_Buffer[i] = rd_FpCaptureList.get(i);
            }
            System.out.println("Final buffer in fp capture : " + rd_Buffer.length);
            try {
                usbCommunication.bulkTransfer(writeData, rd_Buffer, rd_Buffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);

            if (ackStatus.equals("A")) {
                System.out.println("usb communication : " + usbCommunication);
                btMessage = btPrinterErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            } else if (ackStatus.equals("DF")) {
                btMessage = btPrinterErrorMessages(readData);

            } else if (ackStatus.equals("SS")) {

                try {
                    int rd_Data = usbCommunication.bulkTransfer(readData, buffer, buffer.length, 0);
                    ;
                    byte[] ackBytes = new byte[rd_Data];
                    for (int i = 0; i < rd_Data; i++) {
                        byte b = packetBytes[i];
                        ackBytes[i] = b;
                    }

                    String byteValue = new String(ackBytes).toString();

                    ackStatus = byteValue.replaceAll("\n", "")
                            .replaceAll("\f", "").replaceAll("�", "").trim();
                    ackStatus = ackStatus.replace((char) 0, (char) 32).trim();
                    ackStatus = ackStatus.replaceAll(" ", "").trim();

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }
        // return Err code to the user

        if (!btMessage.equals("Capture Failed")) {

            // Success Case of PID Generation
            if (btMessage.contains("<Resp errCode=\"0\"")) {

                int getPosition = btMessage.indexOf("<PidData>");
                String pidBlock = btMessage.substring(getPosition);
                return pidBlock;

            }
            // Failed case
            else {
                try {
                    System.out.println(btMessage);
                    int getErrResCode = btMessage.indexOf("<Resp errCode");
                    int Fcount = btMessage.indexOf("fCount");
                    System.out.println(getErrResCode + " " + Fcount);
                    String showErrCode = btMessage.substring(getErrResCode, Fcount);
                    System.out.println("show error code : " + showErrCode);
                    return showErrCode;
                } catch (Exception e) {

                }

            }

        }

        return btMessage;

    }

    //switch on bluetooth before calling this API for RD Service in USB mode
    public String ble_teth_in_USB_interface(UsbEndpoint writeData, UsbEndpoint readData, String macadd) {


        String status = setLength(3 + macadd.length(), writeData, readData);

        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> configList = new ArrayList<Byte>();

            configList.add((byte) START_BIT);
            configList.add((byte) 1);
            configList.add((byte) 21);
            byte macData[] = macadd.getBytes();
            int config_Value_leng = macData.length;
            byte configLengthArray[] = intTo2ByteArray(config_Value_leng);

            configList.add(configLengthArray[0]);
            configList.add(configLengthArray[1]);

            for (int j = 0; j < config_Value_leng; j++)
                configList.add(macData[j]);

            byte[] checkSum = new byte[configList.size()];
            for (int i = 1, k = 0; i < configList.size(); i++, k++) {
                checkSum[k] = configList.get(i);
            }

            configList.add(checkSum(checkSum));
            configList.add((byte) STOP_BIT);

            byte[] configInfoBuffer = new byte[configList.size()];
            for (int i = 0; i < configList.size(); i++) {
                configInfoBuffer[i] = configList.get(i);
            }

            System.out.println("Total buffer : "
                    + Arrays.toString(configInfoBuffer));
            try {
                usbCommunication.bulkTransfer(writeData, configInfoBuffer, configInfoBuffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btDeviceErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }
        System.out.println("BTTTTT : " + btMessage);
        return btMessage;
    }


    public String rd_Fp_deviceInfo(UsbEndpoint writeData, UsbEndpoint readData) {

        String status = setLength(2, writeData, readData);
        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> rd_DeviceInfo = new ArrayList<Byte>();

            rd_DeviceInfo.add((byte) START_BIT);
            rd_DeviceInfo.add((byte) 1);
            rd_DeviceInfo.add((byte) 12);
            rd_DeviceInfo.add((byte) 3);

            byte[] checkSum = new byte[rd_DeviceInfo.size()];
            for (int i = 1, k = 0; i < rd_DeviceInfo.size(); i++, k++) {
                checkSum[k] = rd_DeviceInfo.get(i);
            }

            rd_DeviceInfo.add(checkSum(checkSum));
            rd_DeviceInfo.add((byte) STOP_BIT);

            byte[] rd_Buffer = new byte[rd_DeviceInfo.size()];
            for (int i = 0; i < rd_DeviceInfo.size(); i++) {
                rd_Buffer[i] = rd_DeviceInfo.get(i);
            }

            try {
                usbCommunication.bulkTransfer(writeData, rd_Buffer, rd_Buffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btPrinterErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            } else if (ackStatus.equals("IF")) {
                btMessage = btPrinterErrorMessages(readData);

            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }

        return btMessage;
    }

    // RD_Status

    public String rd_Status_Info(UsbEndpoint writeData, UsbEndpoint readData) {

        String status = setLength(2, writeData, readData);
        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> rd_Info = new ArrayList<Byte>();

            rd_Info.add((byte) START_BIT);
            rd_Info.add((byte) 1);
            rd_Info.add((byte) 12);
            rd_Info.add((byte) 9);

            byte[] checkSum = new byte[rd_Info.size()];
            for (int i = 1, k = 0; i < rd_Info.size(); i++, k++) {
                checkSum[k] = rd_Info.get(i);
            }

            rd_Info.add(checkSum(checkSum));
            rd_Info.add((byte) STOP_BIT);

            byte[] rd_Buffer = new byte[rd_Info.size()];
            for (int i = 0; i < rd_Info.size(); i++) {
                rd_Buffer[i] = rd_Info.get(i);
            }

            try {
                usbCommunication.bulkTransfer(writeData, rd_Buffer, rd_Buffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btPrinterErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            } else if (ackStatus.equals("IF")) {
                btMessage = btPrinterErrorMessages(readData);

            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }

        return btMessage;
    }

    public String rd_Send_Wifi_Details(String network_details, UsbEndpoint writeData, UsbEndpoint readData) {

        String status = setLength(network_details.length() + 4, writeData, readData);
        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> rd_FpCaptureList = new ArrayList<Byte>();

            rd_FpCaptureList.add((byte) START_BIT);
            rd_FpCaptureList.add((byte) 1);
            rd_FpCaptureList.add((byte) 12);
            rd_FpCaptureList.add((byte) 4);

            byte xmltextArray[] = network_details.getBytes();
            int length = xmltextArray.length;

            byte xmltextLength[] = intTo2ByteArray(length);

            rd_FpCaptureList.add(xmltextLength[0]);
            rd_FpCaptureList.add(xmltextLength[1]);

            for (int j = 0; j < length; j++) {
                rd_FpCaptureList.add(xmltextArray[j]);

            }

            byte[] checkSum = new byte[rd_FpCaptureList.size()];
            for (int i = 1, k = 0; i < rd_FpCaptureList.size(); i++, k++) {
                checkSum[k] = rd_FpCaptureList.get(i);
            }

            rd_FpCaptureList.add(checkSum(checkSum));

            rd_FpCaptureList.add((byte) STOP_BIT);

            byte[] rd_Buffer = new byte[rd_FpCaptureList.size()];
            for (int i = 0; i < rd_FpCaptureList.size(); i++) {
                rd_Buffer[i] = rd_FpCaptureList.get(i);
            }
            System.out.println("Final data to send : " + rd_Buffer.length);
            try {
                usbCommunication.bulkTransfer(writeData, rd_Buffer, rd_Buffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);

            if (ackStatus.equals("A")) {
                btMessage = btPrinterErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            } else if (ackStatus.equals("DF")) {
                btMessage = btPrinterErrorMessages(readData);

            } else if (ackStatus.equals("SS")) {

                try {
                    int rd_Data = usbCommunication.bulkTransfer(readData, buffer, buffer.length, 0);
                    byte[] ackBytes = new byte[rd_Data];
                    for (int i = 0; i < rd_Data; i++) {
                        byte b = packetBytes[i];
                        ackBytes[i] = b;
                    }

                    String byteValue = new String(ackBytes).toString();

                    ackStatus = byteValue.replaceAll("\n", "")
                            .replaceAll("\f", "").replaceAll("�", "").trim();
                    ackStatus = ackStatus.replace((char) 0, (char) 32).trim();
                    ackStatus = ackStatus.replaceAll(" ", "").trim();

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }

        return btMessage;

    }

    public String mobile_Mac_ID(UsbEndpoint writeData, UsbEndpoint readData, String mac_ID) {
        System.out.println("mac Id in jar :" + mac_ID);

        String status = setLength(mac_ID.length() + 4, writeData, readData);
        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> rd_FpCaptureList = new ArrayList<Byte>();

            rd_FpCaptureList.add((byte) START_BIT);
            rd_FpCaptureList.add((byte) 1);
            rd_FpCaptureList.add((byte) 12);
            rd_FpCaptureList.add((byte) 5);

            byte xmltextArray[] = mac_ID.getBytes();
            int length = xmltextArray.length;

            byte xmltextLength[] = intTo2ByteArray(length);

            rd_FpCaptureList.add(xmltextLength[0]);
            rd_FpCaptureList.add(xmltextLength[1]);

            for (int j = 0; j < length; j++) {
                rd_FpCaptureList.add(xmltextArray[j]);

            }

            byte[] checkSum = new byte[rd_FpCaptureList.size()];
            for (int i = 1, k = 0; i < rd_FpCaptureList.size(); i++, k++) {
                checkSum[k] = rd_FpCaptureList.get(i);
            }

            rd_FpCaptureList.add(checkSum(checkSum));

            rd_FpCaptureList.add((byte) STOP_BIT);

            byte[] rd_Buffer = new byte[rd_FpCaptureList.size()];
            for (int i = 0; i < rd_FpCaptureList.size(); i++) {
                rd_Buffer[i] = rd_FpCaptureList.get(i);
            }
            // System.out.println("Final data to send : " + rd_Buffer.length);
            try {
                usbCommunication.bulkTransfer(writeData, rd_Buffer, rd_Buffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);

            if (ackStatus.equals("A")) {
                btMessage = btPrinterErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            } else if (ackStatus.equals("DF")) {
                btMessage = btPrinterErrorMessages(readData);

            } else if (ackStatus.equals("SS")) {

                try {
                    int rd_Data = usbCommunication.bulkTransfer(readData, buffer, buffer.length, 0);
                    ;
                    byte[] ackBytes = new byte[rd_Data];
                    for (int i = 0; i < rd_Data; i++) {
                        byte b = packetBytes[i];
                        ackBytes[i] = b;
                    }

                    String byteValue = new String(ackBytes).toString();

                    ackStatus = byteValue.replaceAll("\n", "")
                            .replaceAll("\f", "").replaceAll("�", "").trim();
                    ackStatus = ackStatus.replace((char) 0, (char) 32).trim();
                    ackStatus = ackStatus.replaceAll(" ", "").trim();

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }

        return btMessage;

    }

    public String zLog(UsbEndpoint writeData, UsbEndpoint readData) {
        String filePath = "/home/rdservice/zlog.log";
        String status = setLength(filePath.length() + 4, writeData, readData);

        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> rd_DeviceInfo = new ArrayList<Byte>();

            rd_DeviceInfo.add((byte) START_BIT);
            rd_DeviceInfo.add((byte) 1);
            rd_DeviceInfo.add((byte) 12);
            rd_DeviceInfo.add((byte) 6);
            byte filePathArray[] = filePath.getBytes();
            int length = filePathArray.length;

            byte fileLength[] = intTo2ByteArray(length);

            rd_DeviceInfo.add(fileLength[0]);
            rd_DeviceInfo.add(fileLength[1]);

            for (int j = 0; j < length; j++) {
                rd_DeviceInfo.add(filePathArray[j]);

            }

            byte[] checkSum = new byte[rd_DeviceInfo.size()];
            for (int i = 1, k = 0; i < rd_DeviceInfo.size(); i++, k++) {
                checkSum[k] = rd_DeviceInfo.get(i);
            }

            rd_DeviceInfo.add(checkSum(checkSum));
            rd_DeviceInfo.add((byte) STOP_BIT);

            byte[] rd_Buffer = new byte[rd_DeviceInfo.size()];
            for (int i = 0; i < rd_DeviceInfo.size(); i++) {
                rd_Buffer[i] = rd_DeviceInfo.get(i);
            }

            try {
                usbCommunication.bulkTransfer(writeData, rd_Buffer, rd_Buffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btPrinterErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            } else if (ackStatus.equals("IF")) {
                btMessage = btPrinterErrorMessages(readData);

            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }

        return btMessage;
    }

    public String RD_Version(UsbEndpoint writeData, UsbEndpoint readData) {
        String filePath = "/home/rdservice/config/config.ini";
        String status = setLength(filePath.length() + 4, writeData, readData);

        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> rd_DeviceInfo = new ArrayList<Byte>();

            rd_DeviceInfo.add((byte) START_BIT);
            rd_DeviceInfo.add((byte) 1);
            rd_DeviceInfo.add((byte) 12);
            rd_DeviceInfo.add((byte) 6);
            byte filePathArray[] = filePath.getBytes();
            int length = filePathArray.length;

            byte fileLength[] = intTo2ByteArray(length);

            rd_DeviceInfo.add(fileLength[0]);
            rd_DeviceInfo.add(fileLength[1]);

            for (int j = 0; j < length; j++) {
                rd_DeviceInfo.add(filePathArray[j]);

            }

            byte[] checkSum = new byte[rd_DeviceInfo.size()];
            for (int i = 1, k = 0; i < rd_DeviceInfo.size(); i++, k++) {
                checkSum[k] = rd_DeviceInfo.get(i);
            }

            rd_DeviceInfo.add(checkSum(checkSum));
            rd_DeviceInfo.add((byte) STOP_BIT);

            byte[] rd_Buffer = new byte[rd_DeviceInfo.size()];
            for (int i = 0; i < rd_DeviceInfo.size(); i++) {
                rd_Buffer[i] = rd_DeviceInfo.get(i);
            }

            try {
                usbCommunication.bulkTransfer(writeData, rd_Buffer, rd_Buffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btPrinterErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            } else if (ackStatus.equals("IF")) {
                btMessage = btPrinterErrorMessages(readData);

            }
            BufferedReader bufReader = new BufferedReader(new StringReader(
                    btMessage));
            String line, RD_Version = null;
            try {
                while (true) {
                    if ((line = bufReader.readLine()) != null) {
                        if (line.contains("rd_ver")) {
                            RD_Version = line;
                            RD_Version.replaceAll(" ", "");
                            break;
                        }

                    }
                }

                // call SDK version API
                Thread.sleep(2000);
                SDK_Version(writeData, readData);
                SDK_VER = btMessage;
                RD_SDK_VER = RD_Version + "\n\n" + SDK_VER;
                return RD_SDK_VER;

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }

        return btMessage;
    }

    public String SDK_Version(UsbEndpoint writeData, UsbEndpoint readData) {
        String filePath = "/etc/rd_info.txt";
        String status = setLength(filePath.length() + 4, writeData, readData);

        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> rd_DeviceInfo = new ArrayList<Byte>();

            rd_DeviceInfo.add((byte) START_BIT);
            rd_DeviceInfo.add((byte) 1);
            rd_DeviceInfo.add((byte) 12);
            rd_DeviceInfo.add((byte) 6);
            byte filePathArray[] = filePath.getBytes();
            int length = filePathArray.length;

            byte fileLength[] = intTo2ByteArray(length);

            rd_DeviceInfo.add(fileLength[0]);
            rd_DeviceInfo.add(fileLength[1]);

            for (int j = 0; j < length; j++) {
                rd_DeviceInfo.add(filePathArray[j]);

            }

            byte[] checkSum = new byte[rd_DeviceInfo.size()];
            for (int i = 1, k = 0; i < rd_DeviceInfo.size(); i++, k++) {
                checkSum[k] = rd_DeviceInfo.get(i);
            }

            rd_DeviceInfo.add(checkSum(checkSum));
            rd_DeviceInfo.add((byte) STOP_BIT);

            byte[] rd_Buffer = new byte[rd_DeviceInfo.size()];
            for (int i = 0; i < rd_DeviceInfo.size(); i++) {
                rd_Buffer[i] = rd_DeviceInfo.get(i);
            }

            try {
                usbCommunication.bulkTransfer(writeData, rd_Buffer, rd_Buffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btPrinterErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            } else if (ackStatus.equals("IF")) {
                btMessage = btPrinterErrorMessages(readData);

            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }

        return btMessage;
    }

    public String getDeviceIDs(UsbEndpoint writeData, UsbEndpoint readData) {

        String status = setLength(2, writeData, readData);
        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> rd_DeviceInfo = new ArrayList<Byte>();

            rd_DeviceInfo.add((byte) START_BIT);
            rd_DeviceInfo.add((byte) 1);
            rd_DeviceInfo.add((byte) 12);
            rd_DeviceInfo.add((byte) 7);

            byte[] checkSum = new byte[rd_DeviceInfo.size()];
            for (int i = 1, k = 0; i < rd_DeviceInfo.size(); i++, k++) {
                checkSum[k] = rd_DeviceInfo.get(i);
            }

            rd_DeviceInfo.add(checkSum(checkSum));
            rd_DeviceInfo.add((byte) STOP_BIT);

            byte[] rd_Buffer = new byte[rd_DeviceInfo.size()];
            for (int i = 0; i < rd_DeviceInfo.size(); i++) {
                rd_Buffer[i] = rd_DeviceInfo.get(i);
            }

            try {
                usbCommunication.bulkTransfer(writeData, rd_Buffer, rd_Buffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btPrinterErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            } else if (ackStatus.equals("IF")) {
                btMessage = btPrinterErrorMessages(readData);

            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }

        return btMessage;
    }

    // if scanner Hardware replaced need to call following API
    public String scanner_Replaced(UsbEndpoint writeData, UsbEndpoint readData) {

        String status = setLength(2, writeData, readData);
        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> rd_DeviceInfo = new ArrayList<Byte>();

            rd_DeviceInfo.add((byte) START_BIT);
            rd_DeviceInfo.add((byte) 1);
            rd_DeviceInfo.add((byte) 12);
            rd_DeviceInfo.add((byte) 8);

            byte[] checkSum = new byte[rd_DeviceInfo.size()];
            for (int i = 1, k = 0; i < rd_DeviceInfo.size(); i++, k++) {
                checkSum[k] = rd_DeviceInfo.get(i);
            }

            rd_DeviceInfo.add(checkSum(checkSum));
            rd_DeviceInfo.add((byte) STOP_BIT);

            byte[] rd_Buffer = new byte[rd_DeviceInfo.size()];
            for (int i = 0; i < rd_DeviceInfo.size(); i++) {
                rd_Buffer[i] = rd_DeviceInfo.get(i);
            }

            try {
                usbCommunication.bulkTransfer(writeData, rd_Buffer, rd_Buffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btPrinterErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            } else if (ackStatus.equals("IF")) {
                btMessage = btPrinterErrorMessages(readData);

            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }

        return btMessage;
    }

    // read macid from device

    public String getMobileMacID(OutputStream mOutputStream,
                                 InputStream mInputStream, UsbEndpoint writeData, UsbEndpoint readData) {

        String status = setLength(2, writeData, readData);
        if (status.equals("LENGTH SUCCESS")) {

            List<Byte> rd_DeviceInfo = new ArrayList<Byte>();

            rd_DeviceInfo.add((byte) START_BIT);
            rd_DeviceInfo.add((byte) 1);
            rd_DeviceInfo.add((byte) 12);
            rd_DeviceInfo.add((byte) 4);

            byte[] checkSum = new byte[rd_DeviceInfo.size()];
            for (int i = 1, k = 0; i < rd_DeviceInfo.size(); i++, k++) {
                checkSum[k] = rd_DeviceInfo.get(i);
            }

            rd_DeviceInfo.add(checkSum(checkSum));
            rd_DeviceInfo.add((byte) STOP_BIT);

            byte[] rd_Buffer = new byte[rd_DeviceInfo.size()];
            for (int i = 0; i < rd_DeviceInfo.size(); i++) {
                rd_Buffer[i] = rd_DeviceInfo.get(i);
            }

            try {
                usbCommunication.bulkTransfer(writeData, rd_Buffer, rd_Buffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btPrinterErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            } else if (ackStatus.equals("IF")) {
                btMessage = btPrinterErrorMessages(readData);

            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }

        return btMessage;
    }

    //
    public String deviceReboot(UsbEndpoint writeData, UsbEndpoint readData) {
        // System.out.println("beofre length");
        String status = setLength(1, writeData, readData);
        if (status.equals("LENGTH SUCCESS")) {
            List<Byte> deviceRebootList = new ArrayList<Byte>();
            deviceRebootList.add((byte) START_BIT);
            deviceRebootList.add((byte) 1);
            deviceRebootList.add((byte) 10);
            byte[] checkSum = new byte[deviceRebootList.size()];
            for (int i = 1, k = 0; i < deviceRebootList.size(); i++, k++) {
                checkSum[k] = deviceRebootList.get(i);
            }
            deviceRebootList.add(checkSum(checkSum));
            deviceRebootList.add((byte) STOP_BIT);
            byte[] deviceBuffer = new byte[deviceRebootList.size()];
            for (int i = 0; i < deviceRebootList.size(); i++) {
                deviceBuffer[i] = deviceRebootList.get(i);
            }
            try {
                usbCommunication.bulkTransfer(writeData, deviceBuffer, deviceBuffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btDeviceErrorMessages(readData);
            }
        }
        return btMessage;
    }

    public String devicePower_Off(UsbEndpoint writeData, UsbEndpoint readData) {
        String status = setLength(1, writeData, readData);

        if (status.equals("LENGTH SUCCESS")) {
            List<Byte> devicePower_Off_List = new ArrayList<Byte>();
            devicePower_Off_List.add((byte) START_BIT);
            devicePower_Off_List.add((byte) 1);
            devicePower_Off_List.add((byte) 11);
            byte[] checkSum = new byte[devicePower_Off_List.size()];
            for (int i = 1, k = 0; i < devicePower_Off_List.size(); i++, k++) {
                checkSum[k] = devicePower_Off_List.get(i);
            }
            devicePower_Off_List.add(checkSum(checkSum));
            devicePower_Off_List.add((byte) STOP_BIT);
            byte[] deviceBuffer = new byte[devicePower_Off_List.size()];
            for (int i = 0; i < devicePower_Off_List.size(); i++) {
                deviceBuffer[i] = devicePower_Off_List.get(i);
            }
            try {
                usbCommunication.bulkTransfer(writeData, deviceBuffer, deviceBuffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btDeviceErrorMessages(readData);
            }
        }
        return btMessage;
    }

    public String enablingBash(UsbEndpoint writeData, UsbEndpoint readData) {
        String status = setLength(1, writeData, readData);

        if (status.equals("LENGTH SUCCESS")) {
            List<Byte> devicePower_Off_List = new ArrayList<Byte>();
            devicePower_Off_List.add((byte) START_BIT);
            devicePower_Off_List.add((byte) 1);
            devicePower_Off_List.add((byte) 14);
            byte[] checkSum = new byte[devicePower_Off_List.size()];
            for (int i = 1, k = 0; i < devicePower_Off_List.size(); i++, k++) {
                checkSum[k] = devicePower_Off_List.get(i);
            }
            devicePower_Off_List.add(checkSum(checkSum));
            devicePower_Off_List.add((byte) STOP_BIT);
            byte[] deviceBuffer = new byte[devicePower_Off_List.size()];
            for (int i = 0; i < devicePower_Off_List.size(); i++) {
                deviceBuffer[i] = devicePower_Off_List.get(i);
            }
            try {
                usbCommunication.bulkTransfer(writeData, deviceBuffer, deviceBuffer.length, 0);
            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btDeviceErrorMessages(readData);
            }
        }
        return btMessage;
    }

//	public String setBTPin(String btPin) {
//
//		String status = setLength(4 + btPin.length());
//
//		if (status.equals("LENGTH SUCCESS")) {
//
//			List<Byte> setBTPinList = new ArrayList<Byte>();
//
//			setBTPinList.add((byte) START_BIT);
//			setBTPinList.add((byte) 1);
//
//			setBTPinList.add((byte) 4);
//			setBTPinList.add((byte) 1);
//
//			byte pinData[] = btPin.getBytes();
//			int pinLength = pinData.length;
//			byte pinLengthArray[] = intTo2ByteArray(pinLength);
//
//			setBTPinList.add(pinLengthArray[0]);
//			setBTPinList.add(pinLengthArray[1]);
//
//			for (int j = 0; j < pinLength; j++)
//				setBTPinList.add(pinData[j]);
//
//			byte[] checkSum = new byte[setBTPinList.size()];
//			for (int i = 1, k = 0; i < setBTPinList.size(); i++, k++) {
//				checkSum[k] = setBTPinList.get(i);
//			}
//
//			setBTPinList.add(checkSum(checkSum));
//			setBTPinList.add((byte) STOP_BIT);
//
//			byte[] setBTPinBuffer = new byte[setBTPinList.size()];
//			for (int i = 0; i < setBTPinList.size(); i++) {
//				setBTPinBuffer[i] = setBTPinList.get(i);
//			}
//
//			try {
//				// mOutputStream.write(setBTPinBuffer);
//				// mOutputStream.flush();
//			} catch (Exception e) {
//				e.printStackTrace();
//				return "BROKEN PIPE ERROR";
//			}
//
//			ackStatus = readAckByte();
//			if (ackStatus.equals("A")) {
//				btMessage = btDeviceErrorMessages();
//			} else if (ackStatus.equals("N")) {
//				return "RESEND THE DATA";
//			}
//
//		} else if (status.equals("LENGTH FAILED")) {
//			return "LENGTH SET FAILED";
//		} else if (status.equals("BROKEN PIPE ERROR")) {
//			return "BROKEN PIPE ERROR";
//		} else {
//			return "ERROR IN APP";
//		}
//		return btMessage;
//	}

    private String setLength(int length, UsbEndpoint writeData, UsbEndpoint readData) {

        System.out.println("length method was called : " + length);

        List<Byte> lengthList = new ArrayList<Byte>();
        byte dataLength[] = intTo4ByteArray(length);

        lengthList.add((byte) START_BIT);
        lengthList.add(dataLength[1]);
        lengthList.add(dataLength[2]);
        lengthList.add(dataLength[3]);
        lengthList.add(checkSum(dataLength));
        lengthList.add((byte) STOP_BIT);

        byte[] lengthBuffer = new byte[lengthList.size()];
        for (int i = 0; i < lengthList.size(); i++) {
            lengthBuffer[i] = lengthList.get(i);
        }
        System.out.println("sending First 6 bytes : " + Arrays.toString(lengthBuffer));

        try {
            System.out.println("usb communication : " + usbCommunication);
            Log.d(TAG, " final buffer in Lengthchecking : " + Arrays.toString(lengthBuffer));

            int i = usbCommunication.bulkTransfer(writeData, lengthBuffer, lengthBuffer.length, 0);
//            Log.d(TAG, "data writing : " + i);

        } catch (Exception e) {
            e.printStackTrace();
            return "BROKEN PIPE ERROR";
        }
        String btStatus = btSetLengthErrorMessages(readData);
        Log.d(TAG, "Last STATUS in Length Checking : " + btStatus);
        return btStatus;

    }

    private static final byte[] intTo4ByteArray(int value) {
        return new byte[]{(byte) (value >>> 24), (byte) (value >>> 16),
                (byte) (value >>> 8), (byte) value};
    }

    private static final byte[] intTo5ByteArray(int value) {
        return new byte[]{(byte) (value >>> 32), (byte) (value >>> 24),
                (byte) (value >>> 16), (byte) (value >>> 8), (byte) value};
    }

    private static final byte[] intTo2ByteArray(int value) {

        byte[] integerBytes = new byte[2];

        integerBytes[0] = (byte) ((value >> 8) & 0xFF);
        integerBytes[1] = (byte) (value & 0xFF);
        return integerBytes;

    }

    private static final byte checkSum(byte[] bytes) {
        byte sum = 0;
        for (byte b : bytes) {
            sum ^= b;
        }
        return sum;
    }

    private static final int byteArrayTo2Int(byte[] b) {
        int i = 0;
        i |= b[0] & 0xFF;
        i <<= 8;
        i |= b[1] & 0xFF;
        return i;
    }

    private static int byteArrayTo4Int(byte[] bytes) {
        return bytes[0] << 24 | (bytes[1] & 0xFF) << 16
                | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }

    private static String toHexString(byte[] hexData) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < hexData.length; i++)
            stringBuilder.append(String.format("%x", hexData[i]));
        return stringBuilder.toString();
    }

    private static String hexToString(String hexDeciamlValue) {

        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < hexDeciamlValue.length() - 1; i += 2) {
            String outputData = hexDeciamlValue.substring(i, (i + 2));
            int decimal = Integer.parseInt(outputData, 16);
            stringBuilder.append((char) decimal);
        }

        return stringBuilder.toString();
    }

    @SuppressLint("SdCardPath")
    private String scaleImage(String scaleImageName, int userWidth) {

        String filePath = null;

        final Options imageOptions = new BitmapFactory.Options();
        imageOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(scaleImageName, imageOptions);
        try {
            int optionsWidth = imageOptions.outWidth;
            int optionsHeight = imageOptions.outHeight;

            Log.d(TAG, "OPT WIDTH AND OPT HEIGHT : " + optionsWidth + " "
                    + optionsHeight);
            Log.d(TAG, "IN IMAGE SCALING");
            System.out.println("scaleimagename" + scaleImageName);
            Bitmap bitmap = BitmapFactory.decodeFile(scaleImageName);
            System.out.println("scaleimagename" + scaleImageName);
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();

            Log.d(TAG, "WIDTH AND HEIGHT : " + width + " " + height);

            float xScale = ((float) userWidth) / width;
            float yScale = ((float) optionsHeight) / height;

            Matrix matrix = new Matrix();
            matrix.postScale(xScale, yScale);

            Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width,
                    height, matrix, true);

            File scaleImagefile = new File(
                    Environment.getExternalStorageDirectory(),
                    "scale_image.png");
            filePath = scaleImagefile.toString();

            OutputStream outputStream = new FileOutputStream(filePath);
            scaledBitmap.compress(CompressFormat.PNG, 100, outputStream);
            outputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return filePath;
    }

    private String bmpConversion(String bmpfile) {

        Log.d(TAG, "IN BMP CONERSION");

        Bitmap inputBitmap = BitmapFactory.decodeFile(bmpfile);
        BitmapConvertor convertor = new BitmapConvertor();
        String bmpPath = convertor.convertBitmap(inputBitmap, "bmp_image");
        return bmpPath;
    }

    private String btPrinterErrorMessages(UsbEndpoint readData) {

        LOOP_FLAG = true;
        Log.d(TAG, "THE LOOP CHECKING IN btPrinterErrorMessages : " + LOOP_FLAG);

        while (LOOP_FLAG) {
            try {
                //reading data
                if (usbCommunication!=null) {
                    System.out.println("usb communication : " + usbCommunication);
                    int bytesAvailable = usbCommunication.bulkTransfer(readData, buffer, buffer.length, 0);
                    Log.d(TAG, "received bytes : " + bytesAvailable);
                    String ackValue="";
                    if(bytesAvailable>0) {

                        byte[] received = new byte[bytesAvailable];

                        System.arraycopy(buffer, 0, received, 0, bytesAvailable);
                        ackValue = new String(received);
                        Log.d(TAG, "ackvalue : " + ackValue);
                    }

                    if (bytesAvailable == 1 || bytesAvailable == 2) {


                        if (bytesAvailable == 2) {
//                        String byteValue = new String(ackValue).toString();
//                        Log.d(TAG, "THE 97BT ACK : " + byteValue);
//
//                        String btAck = byteValue.replaceAll("\0", "")
//                                .replaceAll("\n", "").replaceAll("\f", "")
//                                .replaceAll("�", "").trim();
//                        Log.d(TAG, "THE 97BT ACK : " + btAck);
                            String btAck = ackValue;
                            Log.d(TAG, "THE 97BT ACK : " + btAck);
                            if (btAck.equals("99")) {
                                BT_MESSAGE = "OPERATION SUCCESS";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("IO")) {
                                BT_MESSAGE = "INVALID-FONT-SELECTION";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("02")) {

                                BT_MESSAGE = "PRINTER OPEN ERROR";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("03")) {
                                BT_MESSAGE = "PAPER NOT PRESENT";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("04")) {
                                BT_MESSAGE = "LID IS OPEN PLEASE CLOSE THE LID";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("05")) {
                                BT_MESSAGE = "PRINTER WRTITE TEXT ERROR";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("06")) {
                                BT_MESSAGE = "MAXIMUM LENGTH EXCEEDED";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("07")) {
                                BT_MESSAGE = "FILE NOT FOUND";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("08")) {
                                BT_MESSAGE = "MAXIMUM BMP SIZE EXCEEDED";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("09")) {
                                BT_MESSAGE = "PRINTER SELECT BMP ERROR";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("10")) {
                                BT_MESSAGE = "PRINTER BMP ERROR";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("11")) {
                                BT_MESSAGE = "PRINTER BMP STORE ERROR";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("12")) {
                                BT_MESSAGE = "LENGTH ERROR";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("13")) {
                                BT_MESSAGE = "LOW VOLTAGE";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("14")) {
                                BT_MESSAGE = "BMP FILE ERROR";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("15")) {
                                BT_MESSAGE = "NO DATA";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("16")) {
                                BT_MESSAGE = "PARAMETERS ERROR";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("17")) {
                                BT_MESSAGE = "NO RESPONSE";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("18")) {
                                BT_MESSAGE = "NOT SUPPORTED";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("29")) {
                                BT_MESSAGE = "NO LINES TO PRINT";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("30")) {
                                BT_MESSAGE = "MAX LINES EXCEEDED";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("33")) {
                                BT_MESSAGE = "DATA INVALID FORMAT";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("DF")) {
                                BT_MESSAGE = "Discovery Failed";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("EF")) {
                                BT_MESSAGE = "Device-IDs Failed";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("CF")) {
                                check_RDS_Status = btAck;

                                BT_MESSAGE = "Capture Failed";
                                LOOP_FLAG = false;

                            } else if (btAck.equals("IF")) {
                                BT_MESSAGE = "Device_Info_Failed";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("PF")) {
                                BT_MESSAGE = "Data_get_Failed";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("NS")) {
                                BT_MESSAGE = "Network_Setting_Updated";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("TS")) {
                                BT_MESSAGE = "Tethering_Success";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("TF")) {
                                BT_MESSAGE = "Tethering_Fail";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("FF")) {
                                BT_MESSAGE = "Zlog_Fail";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("VS")) {
                                BT_MESSAGE = "File_Received";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("VF")) {
                                BT_MESSAGE = "File_not_Received";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("RS")) {
                                BT_MESSAGE = "Replace_Success";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("RF")) {
                                BT_MESSAGE = "Replace_Failed";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("RN")) {

                                BT_MESSAGE = "RD_NOT_INSTALLED";
                                LOOP_FLAG = false;

                            } else if (btAck.equals("RR")) {

                                BT_MESSAGE = "RD_Running_Successfully";
                                LOOP_FLAG = false;

                            } else if (btAck.equals("RP")) {

                                BT_MESSAGE = "RD_INSTALLED" + "\n"
                                        + "But_RD_Not_Running";
                                LOOP_FLAG = false;

                            } else if (btAck.equals("LN")) {
                                BT_MESSAGE = "Innovatric_Licence not found.\n"
                                        + "Please generate licence file";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("KF")) {
                                // BT_MESSAGE = "Keep_Finger";
                                check_RDS_Status = btAck;
                                // LOOP_FLAG = false;
                            } else if (btAck.equals("SS")) {
                                fingerStatus = btAck;
                                check_RDS_Status = btAck;

                                int xmlData = 0, totalBytes = 0, len = 0;

                                byte[] lenOf2Bytes = new byte[2];
                                try {
                                    String tempData = null;
                                    int count = 1;
                                    // method3

                                    while (true) {

                                        byte[] readBytes = null;

                                        xmlData = usbCommunication.bulkTransfer(readData, buffer, buffer.length, 0);

                                        totalBytes += xmlData;

                                        readBytes = new byte[xmlData];

                                        for (int i = 0; i < xmlData; i++) {
                                            byte b = packetBytes[i];

                                            readBytes[i] = b;

                                        }

                                        if (count == 1) {

                                            for (int j = 0, k = 1; j < lenOf2Bytes.length; j++, k++) {
                                                lenOf2Bytes[j] = readBytes[k];
                                            }
                                            System.out.println("2 bytes length : "
                                                    + Arrays.toString(lenOf2Bytes));
                                            len = byteArrayTo2Int(lenOf2Bytes);
                                            System.out.println("2 bytes length : "
                                                    + len);

                                            String xmlStringData1 = new String(
                                                    readBytes).toString();

                                            tempData = xmlStringData1;
                                            // BT_MESSAGE = tempData;
                                            // BT_MESSAGE = BT_MESSAGE
                                            // .replaceAll("\n", "")
                                            // .replaceAll("\f", "")
                                            // .replaceAll("�", "").trim();
                                            // BT_MESSAGE = BT_MESSAGE.replace(
                                            // (char) 0, (char) 32).trim();
                                            // BT_MESSAGE =
                                            // BT_MESSAGE.replaceAll(" ",
                                            // "").trim();

                                            count = 0;

                                        } else if (count != 1) {

                                            String xmlStringData2 = new String(
                                                    readBytes).toString();

                                            // tempData = BT_MESSAGE +
                                            // xmlStringData2;
                                            // System.out.println("first temp data : "+tempData);
                                            tempData += xmlStringData2;

                                            // BT_MESSAGE = tempData;
                                            // BT_MESSAGE = BT_MESSAGE
                                            // .replaceAll("\n", "")
                                            // .replaceAll("\f", "")
                                            // .replaceAll("�", "").trim();
                                            // BT_MESSAGE = BT_MESSAGE.replace(
                                            // (char) 0, (char) 32).trim();
                                            // BT_MESSAGE =
                                            // BT_MESSAGE.replaceAll(" ",
                                            // "").trim();

                                        }
                                        System.out.println("Total Bytes : "
                                                + totalBytes);

                                        if (totalBytes == len + 7) {
                                            System.out.println("in if");
                                            // int dataLength = BT_MESSAGE.length();
                                            // byte[] bytes = BT_MESSAGE.getBytes();
                                            // byte[] totalBytesarr=new
                                            // byte[totalBytes];
                                            //
                                            // for (int i = 0; i < totalBytes; i++)
                                            // {
                                            // byte b = packetBytes[i];
                                            //
                                            // totalBytesarr[i] = b;
                                            //
                                            // }
                                            // System.out.println("Total Bytes : "
                                            // + Arrays.toString(readBytes));

                                            // byte[] checksumBytes = null;
                                            // checksumBytes = Arrays.copyOfRange(
                                            // readBytes, 0,
                                            // readBytes.length - 3);
                                            //
                                            // byte checksumFinalByte =
                                            // checkSum(checksumBytes);
                                            // System.out.println("check finalbyte : "
                                            // + checksumFinalByte);
                                            // System.out.println("Total Bytes : "+Arrays.toString(readBytes));
                                            // System.out.println("Checksum Bytes : "+Arrays.toString(checksumBytes));
                                            // System.out.println("length - 3 : "+readBytes[readBytes.length
                                            // - 3]);
                                            //
                                            // if (checksumFinalByte ==
                                            // readBytes[readBytes.length - 3]) {
                                            // System.out
                                            // .println("checksum checked at ss");
                                            // BT_MESSAGE = BT_MESSAGE
                                            // .replaceAll("\f", "")
                                            // .replaceAll("�", "").trim();
                                            // BT_MESSAGE = BT_MESSAGE.replace(
                                            // (char) 0, (char) 32).trim();
                                            // BT_MESSAGE = BT_MESSAGE.replaceAll(
                                            // " ", "").trim();
                                            // return BT_MESSAGE;
                                            //
                                            // } else {
                                            // BT_MESSAGE = "CheckSum Failed";
                                            //
                                            // }


                                            break;
                                        }

                                    }

                                    BT_MESSAGE = tempData.substring(3,
                                            tempData.length() - 4);
                                    writeToFile(BT_MESSAGE);
                                    // System.out.println("file created");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                LOOP_FLAG = false;
                            }

                        } else if (bytesAvailable == 1) {
//                        String byteValue = new String(ackValue).toString();
//                        Log.d(TAG, "THE 97BT ACK : " + byteValue);
//
                            String btAck = ackValue.replaceAll("\0", "")
                                    .replaceAll("\n", "").replaceAll("\f", "")
                                    .replaceAll("�", "").trim();
                            Log.d(TAG, "THE 97BT ACK : " + btAck);


                            if (btAck.equals("0")) {
                                BT_MESSAGE = "DIAGNOSE SUCCESS";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("2")) {
                                BT_MESSAGE = "LID IS OPEN PLEASE CLOSE THE LID";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("1")) {
                                BT_MESSAGE = "PAPER NOT PRESENT";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("3")) {
                                BT_MESSAGE = "LID IS OPEN PLEASE CLOSE THE LID AND PAPER NOT PRESENT";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("4")) {

                                BT_MESSAGE = "OVER TEMPARATURE";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("8")) {
                                BT_MESSAGE = "LOW BATTERY";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("N")) {
                                BT_MESSAGE = "RESEND THE DATA";
                                LOOP_FLAG = false;
                            }
                        }
                    } else {
                        System.out.println("More Data");
                        LOOP_FLAG = false;
                    }
                }else{
                    System.out.println("Device is null");
                }
            } catch (Exception ex) {
                System.out.println("catch block");
                ex.printStackTrace();
                return "" + ex;
            }

        }
        System.out.println("BT_MESSAGE : " + BT_MESSAGE);
        return BT_MESSAGE;
    }

    // store template in buffer for RD

    private String btFingerPrintErrorMessagesforRD(UsbEndpoint readData) {
        byte[] finaltemplate = null;
        LOOP_FLAG = true;
        Log.d(TAG, "THE LOOP CHECKING IN FINGERPRINT : " + LOOP_FLAG);

        while (LOOP_FLAG) {
            try {

                int bytesAvailable = usbCommunication.bulkTransfer(readData, buffer, buffer.length, 0);
                if (bytesAvailable == 1 || bytesAvailable == 2) {

                    byte[] ackBytes = new byte[bytesAvailable];

                    Log.d(TAG, "THE AVAILABLE BYTES : " + bytesAvailable);

                    for (int i = 0; i < bytesAvailable; i++) {
                        byte b = packetBytes[i];
                        Log.d(TAG, "THE BYTE VALUE : " + b);

                        ackBytes[i] = b;
                    }
                    if (bytesAvailable == 2) {
                        String byteValue = new String(ackBytes).toString();
                        Log.d(TAG, "THE 97BT ACK : " + byteValue);

                        String btAck = byteValue.replaceAll("\0", "")
                                .replaceAll("\n", "").replaceAll("\f", "")
                                .replaceAll("�", "").trim();
                        Log.d(TAG, "THE 97BT ACK : " + btAck);

                        if (btAck.equals("99")) {
                            BT_MESSAGE = "OPERATION SUCCESS";
                            LOOP_FLAG = false;
                        } else if (btAck.equals("ES")) {
                            // BT_MESSAGE = "ENROLL SUCCESS";
                            // LOOP_FLAG = false;
                        } else if (btAck.equals("GS")) {
                            BT_MESSAGE = "GENERATION SUCCESS";
                            LOOP_FLAG = false;
                        } else if (btAck.equals("GF")) {
                            BT_MESSAGE = "FUSED BUT GENERATION FAILED";
                            LOOP_FLAG = false;
                        } else if (btAck.equals("NF")) {
                            BT_MESSAGE = "FUSE NOT DONE";
                            LOOP_FLAG = false;
                        } else if (btAck.equals("FF")) {
                            BT_MESSAGE = "FUSE FAILED";
                            LOOP_FLAG = false;
                        } else if (btAck.equals("20")) {
                            BT_MESSAGE = "FP INITIALIZATION FAILED";

                            LOOP_FLAG = false;
                        } else if (btAck.equals("21")) {
                            BT_MESSAGE = "ENROLL FAILED";
                            LOOP_FLAG = false;
                        } else if (btAck.equals("22")) {
                            BT_MESSAGE = "VERIFICATION FAILED";
                            LOOP_FLAG = false;
                        } else if (btAck.equals("23")) {
                            BT_MESSAGE = "TEMPLATE NOT FOUND";
                            LOOP_FLAG = false;
                        }
                    } else if (bytesAvailable == 1) {
                        String byteValue = new String(ackBytes).toString();
                        Log.d(TAG, "THE 97BT ACK : " + byteValue);

                        String btAck = byteValue.replaceAll("\0", "")
                                .replaceAll("\n", "").replaceAll("\f", "")
                                .replaceAll("�", "").trim();
                        Log.d(TAG, "THE 97BT ACK : " + btAck);

                        if (btAck.equals("N")) {
                            BT_MESSAGE = "RESEND THE DATA";
                            LOOP_FLAG = false;
                        }
                    }
                } else if (bytesAvailable == 600) {
                    Log.d(TAG, "THE AVAILABLE BYTES : " + bytesAvailable);

                    byte[] fingerTemplateData = new byte[bytesAvailable];

                    for (int i = 0; i < bytesAvailable; i++) {
                        byte b = packetBytes[i];
                        if (b == 12) {

                        } else {
                            fingerTemplateData[i] = b;
                        }

                        if (b == 12) {

                            byte[] fingerTemplateLength = new byte[2];
                            fingerTemplateLength[0] = fingerTemplateData[1];
                            fingerTemplateLength[1] = fingerTemplateData[2];

                            int templateLength = byteArrayTo2Int(fingerTemplateLength);
                            byte[] templateData = Arrays.copyOfRange(
                                    fingerTemplateData, 3, templateLength + 3);
                            finaltemplate = templateData;
                            System.out.println("Temp data : "
                                    + new String(templateData).toString());
                            // FileOutputStream templateFileOutputStream = null;
                            //
                            // try {
                            //
                            // templateFileOutputStream = new FileOutputStream(
                            // enrollTemplateName);
                            //
                            // templateFileOutputStream.write(templateData);
                            //
                            // } catch (FileNotFoundException e) {
                            // Log.d(TAG, "FILE NOT FOUND : " + e);
                            // } catch (IOException ioe) {
                            // Log.d(TAG, "EXCEPTION IN FILE WRITING : " + ioe);
                            //
                            // } finally {
                            // try {
                            // if (templateFileOutputStream != null) {
                            // templateFileOutputStream.close();
                            // }
                            // } catch (IOException ioe) {
                            // Log.d(TAG, "ERROR WHILE CHOOSING STREAM : "
                            // + ioe);
                            // }
                            // }
                        }

                    }

                    BT_MESSAGE = new String(finaltemplate);
                    LOOP_FLAG = false;

                } else {
                    LOOP_FLAG = false;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                return "BROKEN PIPE ERROR";
            }
        }
        return BT_MESSAGE;
    }

    //	@SuppressLint("SdCardPath")
    private String btFingerPrintErrorMessages(UsbEndpoint readData) {

        LOOP_FLAG = true;
        Log.d(TAG, "THE LOOP CHECKING IN FINGERPRINT : " + LOOP_FLAG);

        while (LOOP_FLAG) {
            try {
                int bytesAvailable = usbCommunication.bulkTransfer(readData, buffer, buffer.length, 0);
//                byte[] received = new byte[bytesAvailable];
                Log.d(TAG, "received bytes : " + bytesAvailable);
//
//                System.arraycopy(buffer, 0, received, 0, bytesAvailable);
//                String ackValue = new String(received);
//                Log.d(TAG, "::::::::" + ackValue);
                if (bytesAvailable == 1 || bytesAvailable == 2) {

                    byte[] ackBytes = new byte[bytesAvailable];

                    Log.d(TAG, "THE AVAILABLE BYTES : " + bytesAvailable);

                    for (int i = 0; i < bytesAvailable; i++) {
                        byte b = buffer[i];
                        Log.d(TAG, "THE BYTE VALUE : " + b);

                        ackBytes[i] = b;
                    }
                    if (bytesAvailable == 2) {
                        String byteValue = new String(ackBytes).toString();
                        Log.d(TAG, "THE 97BT ACK : " + byteValue);

                        String btAck = byteValue.replaceAll("\0", "")
                                .replaceAll("\n", "").replaceAll("\f", "")
                                .replaceAll("�", "").trim();
                        Log.d(TAG, "THE 97BT ACK : " + btAck);

                        if (btAck.equals("99")) {
                            BT_MESSAGE = "OPERATION SUCCESS";
                            LOOP_FLAG = false;
                        } else if (btAck.equals("ES")) {

                            // BT_MESSAGE = "ENROLL SUCCESS";
                            // LOOP_FLAG = false;
                        } else if (btAck.equals("GS")) {
                            BT_MESSAGE = "GENERATION SUCCESS";
                            LOOP_FLAG = false;
                        } else if (btAck.equals("GF")) {
                            BT_MESSAGE = "FUSED BUT GENERATION FAILED";
                            LOOP_FLAG = false;
                        } else if (btAck.equals("NF")) {
                            BT_MESSAGE = "FUSE NOT DONE";
                            LOOP_FLAG = false;
                        } else if (btAck.equals("FF")) {
                            BT_MESSAGE = "FUSE FAILED";
                            LOOP_FLAG = false;
                        } else if (btAck.equals("20")) {
                            BT_MESSAGE = "FP INITIALIZATION FAILED";

                            LOOP_FLAG = false;
                        } else if (btAck.equals("21")) {
                            BT_MESSAGE = "ENROLL FAILED";
                            LOOP_FLAG = false;
                        } else if (btAck.equals("22")) {
                            BT_MESSAGE = "VERIFICATION FAILED";
                            LOOP_FLAG = false;
                        } else if (btAck.equals("23")) {
                            BT_MESSAGE = "TEMPLATE NOT FOUND";
                            LOOP_FLAG = false;
                        }
                    } else if (bytesAvailable == 1) {

                        String byteValue = new String(ackBytes).toString();
                        Log.d(TAG, "THE 97BT ACK : " + byteValue);

                        String btAck = byteValue.replaceAll("\0", "")
                                .replaceAll("\n", "").replaceAll("\f", "")
                                .replaceAll("�", "").trim();
                        Log.d(TAG, "THE 97BT ACK : " + btAck);

                        if (btAck.equals("N")) {
                            BT_MESSAGE = "RESEND THE DATA";
                            LOOP_FLAG = false;
                        }

                    }
                } else if (bytesAvailable == 600) {
                    Log.d(TAG, "THE AVAILABLE BYTES : " + bytesAvailable);

                    byte[] fingerTemplateData = new byte[bytesAvailable];

                    for (int i = 0; i < bytesAvailable; i++) {
                        byte b = buffer[i];
                        if (b == 12) {

                        } else {
                            fingerTemplateData[i] = b;
                        }
//                        Log.d(TAG, "byte is : " + b);
                        b = 12;
                        if (b == 12) {

                            byte[] fingerTemplateLength = new byte[2];
                            fingerTemplateLength[0] = fingerTemplateData[1];
                            fingerTemplateLength[1] = fingerTemplateData[2];

                            int templateLength = byteArrayTo2Int(fingerTemplateLength);
                            byte[] templateData = Arrays.copyOfRange(
                                    fingerTemplateData, 3, templateLength + 3);
                            System.out.println("Temp data : "
                                    + new String(templateData).toString());
                            FileOutputStream templateFileOutputStream = null;

                            try {

                                templateFileOutputStream = new FileOutputStream(
                                        enrollTemplateName);

//                                Log.d(TAG, "writing in file");
                                templateFileOutputStream.write(templateData);
//                                Log.d(TAG, "writing completed");

                            } catch (FileNotFoundException e) {
                                Log.d(TAG, "FILE NOT FOUND : " + e);
                            } catch (IOException ioe) {
                                Log.d(TAG, "EXCEPTION IN FILE WRITING : " + ioe);

                            } finally {
                                try {
                                    if (templateFileOutputStream != null) {
                                        templateFileOutputStream.close();
                                    }
                                } catch (IOException ioe) {
                                    Log.d(TAG, "ERROR WHILE CHOOSING STREAM : "
                                            + ioe);
                                }
                            }
                        }
                    }

                    BT_MESSAGE = "ENROLL SUCCESS";
                    LOOP_FLAG = false;

                } else {
                    LOOP_FLAG = false;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                return "BROKEN PIPE ERROR";
            }
        }
        return BT_MESSAGE;
    }

    //	@SuppressLint("SdCardPath")
    private String getFingerRAWImage(File rawImageFingerName, UsbEndpoint writeData, UsbEndpoint readData) {

        String ackCheck = null;
        String imageMessage = null;

        LOOP_FLAG = true;
        Log.d(TAG, "THE LOOP CHECKING IN FINGERPRINT BMP IMAGE : " + LOOP_FLAG);

        while (LOOP_FLAG) {
            try {
                int bytesAvailable = usbCommunication.bulkTransfer(readData, buffer, buffer.length, 0);
                if (bytesAvailable == 1) {
                    byte[] ackBytes = new byte[bytesAvailable];
                    for (int i = 0; i < bytesAvailable; i++) {
                        byte packetData = packetBytes[i];
                        ackBytes[i] = packetData;
                    }
                    if (bytesAvailable == 1) {
                        String byteValue = new String(ackBytes).toString();

                        String btAck = byteValue.replaceAll("\0", "")
                                .replaceAll("\n", "").replaceAll("\f", "")
                                .replaceAll("�", "").trim();

                        if (btAck.equals("A")) {
                            ackCheck = btAck;
                            LOOP_FLAG = false;
                        }
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

        }
        if (ackCheck.equals("A")) {
            LOOP_FLAG = true;

            while (LOOP_FLAG) {
                try {

                    int bytesAvailable = usbCommunication.bulkTransfer(readData, buffer, buffer.length, 0);
                    if (bytesAvailable == 2) {

                        byte[] ackBytes = new byte[bytesAvailable];

                        for (int i = 0; i < bytesAvailable; i++) {
                            byte packetData = packetBytes[i];
                            ackBytes[i] = packetData;
                        }
                        if (bytesAvailable == 2) {
                            String byteValue = new String(ackBytes).toString();

                            String btAck = byteValue.replaceAll("\0", "")
                                    .replaceAll("\n", "").replaceAll("\f", "")
                                    .replaceAll("�", "").trim();

                            if (btAck.equals("99")) {
                                ackCheck = btAck;
                                imageMessage = "RAW IMAGE OPERATION SUCCESS";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("07")) {
                                imageMessage = "FILE NOT FOUND";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("41")) {
                                imageMessage = "FILE CREATION FAILED";
                                LOOP_FLAG = false;
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return "BROKEN PIPE ERROR";
                }
            }
        }

        if (ackCheck.equals("99")) {
            LOOP_FLAG = true;

            byte[] packetBytesImage = new byte[4096];
            List<Byte> dataList = new ArrayList<Byte>();
            List<Byte> fingerBMPImageList = new ArrayList<Byte>();

            int length = 0;
            for (int j = 0; j < 24; j++) {
                LOOP_FLAG = true;
                while (LOOP_FLAG) {
                    try {
                        int bytesAvailable = usbCommunication.bulkTransfer(readData, buffer, buffer.length, 0);
                        if (bytesAvailable > 0) {

                            for (int i = 0; i < bytesAvailable; i++) {
                                byte packetData = packetBytesImage[i];
                                dataList.add(packetData);
                            }

                            length = bytesAvailable + length;
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return "BROKEN PIPE ERROR";
                    }
                    if (length >= 4096) {

                        byte[] dataBuffer = new byte[dataList.size()];
                        for (int i = 0; i < dataList.size(); i++) {
                            dataBuffer[i] = dataList.get(i);
                        }

                        byte[] bmpFinger = Arrays.copyOfRange(dataBuffer, 2,
                                4094);

                        if (j == 0) {

                            byte[] imageLength = new byte[4];

                            imageLength[0] = 0;
                            imageLength[1] = dataBuffer[2];
                            imageLength[2] = dataBuffer[3];
                            imageLength[3] = dataBuffer[4];

                            intImageLength = byteArrayTo4Int(imageLength);

                        }

                        for (int f = 0; f < bmpFinger.length; f++) {
                            fingerBMPImageList.add(bmpFinger[f]);
                        }

                        fingerBMPIMage[0] = 0x0D;
                        fingerBMPIMage[1] = 0x0A;
                        try {
                            usbCommunication.bulkTransfer(writeData, fingerBMPIMage, fingerBMPIMage.length, 0);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return "BROKEN PIPE ERROR";
                        }

                        length = 0;
                        dataList.clear();
                        LOOP_FLAG = false;
                    }
                }

            }

            byte[] finalDataBuffer = new byte[fingerBMPImageList.size()];
            for (int i = 0; i < fingerBMPImageList.size(); i++) {
                finalDataBuffer[i] = fingerBMPImageList.get(i);
            }

            byte[] finalBMPFinger = Arrays.copyOfRange(finalDataBuffer, 3,
                    finalDataBuffer.length - (98205 - intImageLength));

            FileOutputStream fos = null;

            try {

                fos = new FileOutputStream(rawImageFingerName);
                fos.write(finalBMPFinger);

            } catch (FileNotFoundException e) {
                imageMessage = "FILE NOT FOUND";
                Log.d(TAG, "FILE NOT FOUND : " + e);
            } catch (IOException ioe) {
                imageMessage = "FILE WRITING FAILED";
                Log.d(TAG, "EXCEPTION IN FILE WRITING : " + ioe);

            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException ioe) {
                    imageMessage = "ERROR";
                    Log.d(TAG, "ERROR WHILE CHOOSING STREAM : " + ioe);
                }
            }
        }

        return imageMessage;
    }

    //	@SuppressLint("SdCardPath")
    private String getFingerBMPImage(File bmpImageFingerName, UsbEndpoint writeData, UsbEndpoint readData) {

        String ackCheck = null;
        String imageMessage = null;

        LOOP_FLAG = true;
        Log.d(TAG, "THE LOOP CHECKING IN FINGERPRINT BMP IMAGE : " + LOOP_FLAG);

        while (LOOP_FLAG) {
            try {
                int bytesAvailable = usbCommunication.bulkTransfer(readData, buffer, buffer.length, 0);
                Log.d(TAG, "bytes ::::::" + bytesAvailable);
                if (bytesAvailable == 1) {
                    byte[] ackBytes = new byte[bytesAvailable];
                    for (int i = 0; i < bytesAvailable; i++) {
                        byte packetData = buffer[i];
                        ackBytes[i] = packetData;
                    }
                    if (bytesAvailable == 1) {
                        String byteValue = new String(ackBytes).toString();

                        String btAck = byteValue.replaceAll("\0", "")
                                .replaceAll("\n", "").replaceAll("\f", "")
                                .replaceAll("�", "").trim();

                        if (btAck.equals("A")) {
                            ackCheck = btAck;
                            LOOP_FLAG = false;
                        }
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                return "BROKEN PIPE ERROR";
            }

        }
        if (ackCheck.equals("A")) {
            LOOP_FLAG = true;

            while (LOOP_FLAG) {
                try {

                    int bytesAvailable = usbCommunication.bulkTransfer(readData, buffer, buffer.length, 0);
                    Log.d(TAG, "bytes sec : " + bytesAvailable);
                    if (bytesAvailable == 2) {

                        byte[] ackBytes = new byte[bytesAvailable];

                        for (int i = 0; i < bytesAvailable; i++) {
                            byte packetData = buffer[i];
                            ackBytes[i] = packetData;
                        }
                        if (bytesAvailable == 2) {
                            String byteValue = new String(ackBytes).toString();

                            String btAck = byteValue.replaceAll("\0", "")
                                    .replaceAll("\n", "").replaceAll("\f", "")
                                    .replaceAll("�", "").trim();

                            if (btAck.equals("99")) {
                                ackCheck = btAck;
                                imageMessage = "BMP IMAGE OPERATION SUCCESS";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("07")) {
                                imageMessage = "FILE NOT FOUND";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("41")) {
                                imageMessage = "FILE CREATION FAILED";
                                LOOP_FLAG = false;
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return "BROKEN PIPE ERROR";
                }
            }
        }

        if (ackCheck.equals("99")) {
            Log.d(TAG, "99 got");
            LOOP_FLAG = true;

            byte[] packetBytesImage = new byte[4096];
            List<Byte> dataList = new ArrayList<Byte>();
            List<Byte> fingerBMPImageList = new ArrayList<Byte>();

            int length = 0;
            for (int j = 0; j < 24; j++) {
                LOOP_FLAG = true;
                while (LOOP_FLAG) {
                    try {
                        int bytesAvailable = usbCommunication.bulkTransfer(readData, packetBytesImage, packetBytesImage.length, 0);

                        Log.d(TAG, "4096 bytes ::" + bytesAvailable);
                        if (bytesAvailable > 0) {

                            for (int i = 0; i < bytesAvailable; i++) {
                                byte packetData = packetBytesImage[i];
                                dataList.add(packetData);
                            }
                            length = bytesAvailable + length;
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                        return "BROKEN PIPE ERROR";
                    }
                    if (length == 4096) {


                        byte[] dataBuffer = new byte[dataList.size()];
                        for (int i = 0; i < dataList.size(); i++) {
                            dataBuffer[i] = dataList.get(i);
                        }

                        byte[] bmpFinger = Arrays.copyOfRange(dataBuffer, 2,
                                4094);

                        if (j == 0) {

                            byte[] imageLength = new byte[4];

                            imageLength[0] = 0;
                            imageLength[1] = dataBuffer[2];
                            imageLength[2] = dataBuffer[3];
                            imageLength[3] = dataBuffer[4];

                            intImageLength = byteArrayTo4Int(imageLength);

                        }

                        for (int f = 0; f < bmpFinger.length; f++) {
                            fingerBMPImageList.add(bmpFinger[f]);
                        }
                        fingerBMPIMage[0] = 0x0D;
                        fingerBMPIMage[1] = 0x0A;
                        try {
                            Log.d(TAG, "sending final data : " + Arrays.toString(fingerBMPIMage));
                            //Thread.sleep(10000);
                            int t = usbCommunication.bulkTransfer(writeData, fingerBMPIMage, fingerBMPIMage.length, 0);
                            System.out.println("check value : " + t);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return "BROKEN PIPE ERROR";
                        }

                        length = 0;
                        dataList.clear();
                        LOOP_FLAG = false;
                    }
                }

            }

            byte[] finalDataBuffer = new byte[fingerBMPImageList.size()];
            for (int i = 0; i < fingerBMPImageList.size(); i++) {
                finalDataBuffer[i] = fingerBMPImageList.get(i);
            }

            byte[] finalBMPFinger = Arrays.copyOfRange(finalDataBuffer, 3,
                    finalDataBuffer.length - (98205 - intImageLength));

            FileOutputStream fos = null;

            try {

                fos = new FileOutputStream(bmpImageFingerName);
                fos.write(finalBMPFinger);

            } catch (FileNotFoundException e) {
                imageMessage = "FILE NOT FOUND";
                Log.d(TAG, "FILE NOT FOUND : " + e);
            } catch (IOException ioe) {
                imageMessage = "FILE WRITING FAILED";
                Log.d(TAG, "EXCEPTION IN FILE WRITING : " + ioe);

            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                } catch (IOException ioe) {
                    imageMessage = "ERROR";
                    Log.d(TAG, "ERROR WHILE CHOOSING STREAM : " + ioe);
                }
            }
        }

        return imageMessage;
    }

    //	@SuppressLint("SdCardPath")
    private void btFingerPrintErrorCallbackMessages(final UsbEndpoint writeData, final UsbEndpoint readData) {

        LOOP_FLAG = true;
        Log.d(TAG, "THE LOOP CHECKING IN FINGERPRINT : " + LOOP_FLAG);

        workerThread = new Thread(new Runnable() {
            public void run() {
                for (int ef = 1; ef <= enrollScanCount; ef++) {
                    while (LOOP_FLAG) {
                        try {

                            int bytesAvailable = usbCommunication.bulkTransfer(readData, buffer, buffer.length, 0);

                            if (bytesAvailable == 1 || bytesAvailable == 2) {

                                byte[] ackBytes = new byte[bytesAvailable];

                                Log.d(TAG, "THE AVAILABLE BYTES : "
                                        + bytesAvailable);

                                for (int i = 0; i < bytesAvailable; i++) {
                                    byte b = packetBytes[i];
                                    Log.d(TAG, "THE BYTE VALUE : " + b);

                                    ackBytes[i] = b;
                                }
                                if (bytesAvailable == 2) {
                                    String byteValue = new String(ackBytes)
                                            .toString();
                                    Log.d(TAG, "THE 97BT ACK : " + byteValue);

                                    String btAck = byteValue
                                            .replaceAll("\0", "")
                                            .replaceAll("\n", "")
                                            .replaceAll("\f", "")
                                            .replaceAll("�", "").trim();
                                    Log.d(TAG, "THE 97BT ACK : " + btAck);

                                    if (btAck.equals("99")) {
                                        BT_MESSAGE = "OPERATION SUCCESS";
                                        LOOP_FLAG = false;
                                    } else if (btAck.equals("ES")) {
                                        enrollSuccessCount++;
                                        ++enrollFingerIndex;

                                        // BT_MESSAGE = "ENROLL SUCCESS";
                                        // LOOP_FLAG = false;
                                    } else if (btAck.equals("20")) {
                                        BT_MESSAGE = "FP INITIALIZATION FAILED";
                                        btCallback
                                                .onFingerPrintInitializationFailed(BT_MESSAGE);
                                        LOOP_FLAG = false;
                                    } else if (btAck.equals("21")) {
                                        BT_MESSAGE = "ENROLL FAILED";
                                        btCallback.onEnrollFailed(BT_MESSAGE);
                                        LOOP_FLAG = false;
                                    } else if (btAck.equals("22")) {
                                        BT_MESSAGE = "VERIFICATION FAILED";
                                        btCallback
                                                .onVerificationFailed(BT_MESSAGE);
                                        LOOP_FLAG = false;
                                    } else if (btAck.equals("23")) {
                                        BT_MESSAGE = "TEMPLATE NOT FOUND";
                                        LOOP_FLAG = false;
                                    } else if (btAck.equals("41")) {
                                        BT_MESSAGE = "TEMPLATE CONVERSION FAILED";
                                        btCallback
                                                .onTemplateConversionFailed(BT_MESSAGE);
                                        LOOP_FLAG = false;
                                    } else if (btAck.equals("42")) {
                                        BT_MESSAGE = "INVALID TEMPLATE TYPE";
                                        btCallback
                                                .onInvalidTemplateType(BT_MESSAGE);
                                        LOOP_FLAG = false;
                                    } else if (btAck.equals("43")) {
                                        BT_MESSAGE = "INVALID TIMEOUT";
                                        btCallback.onInvalidTimeout(BT_MESSAGE);
                                        LOOP_FLAG = false;
                                    } else if (btAck.equals("44")) {
                                        BT_MESSAGE = "FP TIMEOUT";
                                        btCallback
                                                .onFingerPrintScannerTimeout(BT_MESSAGE);
                                        LOOP_FLAG = false;

                                        enrollSuccessCount = 0;
                                        enrollFingerIndex = 0;

                                    } else if (btAck.equals("45")) {
                                        BT_MESSAGE = "FP COMMUNICATION ERROR";
                                        btCallback
                                                .onInternalFingerPrintModuleCommunicationerror(BT_MESSAGE);
                                        LOOP_FLAG = false;
                                    } else if (btAck.equals("46")) {
                                        BT_MESSAGE = "INVALID IMAGE TYPE";
                                        btCallback
                                                .onInvalidImageType(BT_MESSAGE);
                                        LOOP_FLAG = false;
                                    } else if (btAck.equals("47")) {
                                        BT_MESSAGE = "NO OF TEMPLATES EXCEEDED";
                                        btCallback
                                                .onTemplateLimitExceeds(BT_MESSAGE);
                                        LOOP_FLAG = false;
                                    } else if (btAck.equals("48")) {
                                        BT_MESSAGE = "VERIFICATION SUCCESS";
                                        btCallback
                                                .onVerificationSuccess(BT_MESSAGE);
                                        LOOP_FLAG = false;
                                    } else if (btAck.equals("C1")) {

                                        btCallback
                                                .onMoveFingerDown("MOVE FINGER DOWN");

                                    } else if (btAck.equals("C2")) {

                                        btCallback
                                                .onMoveFingerLeft("MOVE FINGER LEFT");

                                    } else if (btAck.equals("C3")) {

                                        btCallback
                                                .onMoveFingerRight("MOVE FINGER RIGHT");

                                    } else if (btAck.equals("C4")) {

                                        btCallback
                                                .onMoveFingerUP("MOVE FINGER UP");

                                    } else if (btAck.equals("C5")) {

                                        btCallback.onPutFinger("PUT FINGER");

                                    } else if (btAck.equals("C6")) {

                                        btCallback
                                                .onPressFingerHard("PRESS FINGER HARD");

                                    } else if (btAck.equals("C7")) {

                                        btCallback
                                                .onRemoveFinger("REMOVE FINGER");

                                    } else if (btAck.equals("51")) {

                                        btCallback
                                                .onSameFinger("SAME FINGER DETECTED");

                                    } else if (btAck.equals("52")) {

                                        btCallback
                                                .onInvalidData("INVALID DUPLICATE PARAMETERS");

                                    }

                                } else if (bytesAvailable == 1) {
                                    String byteValue = new String(ackBytes)
                                            .toString();
                                    Log.d(TAG, "THE 97BT ACK : " + byteValue);

                                    String btAck = byteValue
                                            .replaceAll("\0", "")
                                            .replaceAll("\n", "")
                                            .replaceAll("\f", "")
                                            .replaceAll("�", "").trim();
                                    Log.d(TAG, "THE 97BT ACK : " + btAck);

                                    if (btAck.equals("N")) {
                                        BT_MESSAGE = "RESEND THE DATA";
                                        btCallback
                                                .onInvalidData("RESEND THE DATA");
                                        LOOP_FLAG = false;
                                    }
                                }
                            } else if (bytesAvailable == 600) {

                                Log.d(TAG, "THE AVAILABLE BYTES : "
                                        + bytesAvailable);

                                byte[] fingerTemplateData = new byte[bytesAvailable];
                                byte[] templateData = null;
                                int nfiq = 0;
                                for (int i = 0; i < bytesAvailable; i++) {
                                    byte b = packetBytes[i];
                                    // System.out.println("BYTES in b : " + b);
                                    if (b == 12) {

                                    } else {

                                        fingerTemplateData[i] = b;
                                    }
                                    if (b == 12) {

                                        byte[] fingerTemplateLength = new byte[2];
                                        nfiq = fingerTemplateData[597];
                                        // System.out.println("NFIQ : " + nfiq);
                                        fingerTemplateLength[0] = fingerTemplateData[1];
                                        fingerTemplateLength[1] = fingerTemplateData[2];

                                        int templateLength = byteArrayTo2Int(fingerTemplateLength);
                                        System.out.println("temp length : "
                                                + templateLength);
                                        templateData = Arrays.copyOfRange(
                                                fingerTemplateData, 3,
                                                templateLength + 3);
                                        System.out.println("before template received"
                                                + Arrays.toString(templateData));
                                    }

                                }
                                btCallback.onFingerTemplateRecieved(
                                        templateData, nfiq);

                                BT_MESSAGE = "ENROLL SUCCESS : "
                                        + enrollFingerIndex;

                                // btCallback.onEnrollSuccess(BT_MESSAGE);
                                btCallback.onRemoveFinger("REMOVE FINGER");

                                // try {
                                // Thread.sleep(900);
                                // } catch (InterruptedException e1) {
                                // e1.printStackTrace();
                                // }

                                btCallback.onEnrollSuccess(BT_MESSAGE);
                                // btCallback.onRemoveFinger("REMOVE FINGER");
                                // LOOP_FLAG = false;

                                // try {
                                // Thread.sleep(1100);
                                // } catch (InterruptedException e1) {
                                // e1.printStackTrace();
                                // }

                                byte[] imageBufferAck = new byte[2];

                                imageBufferAck[0] = 0x0D;
                                imageBufferAck[1] = 0x0A;

                                try {
                                    usbCommunication.bulkTransfer(writeData, imageBufferAck, imageBufferAck.length, 0);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    btCallback
                                            .onBTCommunicationFailed("BROKEN PIPE ERROR");
                                }

                                if (imageTypeValue == 1 || imageTypeValue == 2) {
                                    Log.d(TAG, "image type is 1||2");

                                    if (true) {

                                        byte[] packetBytesImage = new byte[4096];
                                        List<Byte> dataList = new ArrayList<Byte>();
                                        List<Byte> fingerImageTypeList = new ArrayList<Byte>();

                                        int length = 0;
                                        for (int j = 0; j < 24; j++) {
                                            boolean LOOP_FLAG1 = true;
                                            while (LOOP_FLAG1) {
                                                try {
                                                    int bytesAvailable1 = usbCommunication.bulkTransfer(readData, buffer, buffer.length, 0);
                                                    if (bytesAvailable1 > 0) {

                                                        for (int i = 0; i < bytesAvailable1; i++) {
                                                            byte packetData = packetBytesImage[i];
                                                            dataList.add(packetData);
                                                        }
                                                        length = bytesAvailable1
                                                                + length;
                                                    }

                                                } catch (Exception ex) {
                                                    ex.printStackTrace();
                                                }
                                                if (length == 4096) {

                                                    byte[] dataBuffer = new byte[dataList
                                                            .size()];
                                                    for (int i = 0; i < dataList
                                                            .size(); i++) {
                                                        dataBuffer[i] = dataList
                                                                .get(i);
                                                    }

                                                    byte[] imageTypeFinger = Arrays
                                                            .copyOfRange(
                                                                    dataBuffer,
                                                                    2, 4094);

                                                    if (j == 0) {

                                                        byte[] imageLength = new byte[4];

                                                        imageLength[0] = 0;
                                                        imageLength[1] = dataBuffer[2];
                                                        imageLength[2] = dataBuffer[3];
                                                        imageLength[3] = dataBuffer[4];

                                                        intImageLength = byteArrayTo4Int(imageLength);

                                                    }

                                                    for (int f = 0; f < imageTypeFinger.length; f++) {
                                                        fingerImageTypeList
                                                                .add(imageTypeFinger[f]);
                                                    }
                                                    fingerIMageTypes[0] = 0x0D;
                                                    fingerIMageTypes[1] = 0x0A;
                                                    try {
                                                        usbCommunication.bulkTransfer(writeData, fingerIMageTypes, fingerIMageTypes.length, 0);
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                        btCallback
                                                                .onBTCommunicationFailed("BROKEN PIPE ERROR");
                                                    }

                                                    length = 0;
                                                    dataList.clear();
                                                    Log.d(TAG,
                                                            "image type value : "
                                                                    + imageTypeValue);
                                                    if (imageTypeValue == 1
                                                            && imageTypeFilename != null) {
                                                        btCallback
                                                                .onImageSaved("RAW IMAGE SAVED");
                                                    } else if (imageTypeValue == 2
                                                            && imageTypeFilename != null) {
                                                        // System.out.println("before bmp image saved");
                                                        btCallback
                                                                .onImageSaved("BMP IMAGE SAVED");
                                                    }

                                                    LOOP_FLAG1 = false;
                                                }
                                            }

                                        }

                                        byte[] finalDataBuffer = new byte[fingerImageTypeList
                                                .size()];
                                        for (int i = 0; i < fingerImageTypeList
                                                .size(); i++) {
                                            finalDataBuffer[i] = fingerImageTypeList
                                                    .get(i);
                                        }

                                        byte[] finalFingerImageTypes = Arrays
                                                .copyOfRange(
                                                        finalDataBuffer,
                                                        3,
                                                        finalDataBuffer.length
                                                                - (98205 - intImageLength));

                                        FileOutputStream fos = null;

                                        try {

                                            if (imageTypeValue == 1) {

                                                Calendar c = Calendar
                                                        .getInstance();
                                                SimpleDateFormat df = new SimpleDateFormat(
                                                        "yyyy-MM-dd@HH:mm:ss");
                                                String dateAndTime = df
                                                        .format(c.getTime());

                                                if (imageTypeFilename == null) {
                                                    btCallback
                                                            .onRAWImageRecieved(finalFingerImageTypes);

                                                } else {
                                                    String rawImageFileName = imageTypeFilename
                                                            .toString()
                                                            + "-"
                                                            + dateAndTime;

                                                    fos = new FileOutputStream(
                                                            new File(
                                                                    rawImageFileName));
                                                    fos.write(finalFingerImageTypes);
                                                    btCallback
                                                            .onRAWImageRecieved(finalFingerImageTypes);

                                                }

                                            } else if (imageTypeValue == 2) {

                                                Calendar c = Calendar
                                                        .getInstance();
                                                SimpleDateFormat df = new SimpleDateFormat(
                                                        "yyyy-MM-dd@HH:mm:ss");
                                                String dateAndTime = df
                                                        .format(c.getTime());

                                                if (imageTypeFilename == null) {
                                                    btCallback
                                                            .onBMPImageRecieved(
                                                                    finalFingerImageTypes,
                                                                    "");
                                                } else {
                                                    String bmpImageFileName = imageTypeFilename
                                                            .toString()
                                                            + "-"
                                                            + dateAndTime
                                                            + ".bmp";

                                                    fos = new FileOutputStream(
                                                            new File(
                                                                    bmpImageFileName));
                                                    fos.write(finalFingerImageTypes);
                                                    btCallback
                                                            .onBMPImageRecieved(
                                                                    finalFingerImageTypes,
                                                                    bmpImageFileName);

                                                }

                                            }

                                        } catch (FileNotFoundException e) {
                                            Log.d(TAG, "FILE NOT FOUND : " + e);
                                            btCallback
                                                    .onImageFileNotFound("FILE NOT FOUND");
                                            LOOP_FLAG = false;
                                        } catch (IOException ioe) {
                                            Log.d(TAG,
                                                    "EXCEPTION IN FILE WRITING : "
                                                            + ioe);

                                        } finally {
                                            try {
                                                if (fos != null) {
                                                    fos.close();
                                                }
                                            } catch (IOException ioe) {
                                                Log.d(TAG,
                                                        "ERROR WHILE CHOOSING STREAM : "
                                                                + ioe);
                                            }
                                        }
                                    }
                                }

                                if (imageTypeValue == 3) {

                                    if (true) {

                                        byte[] packetBytesImage = new byte[4096];
                                        List<Byte> dataList = new ArrayList<Byte>();
                                        List<Byte> fingerImageTypeList = new ArrayList<Byte>();

                                        int length = 0;
                                        for (int j = 0; j < 3; j++) {
                                            boolean LOOP_FLAG1 = true;
                                            while (LOOP_FLAG1) {
                                                try {
                                                    int bytesAvailable1 = usbCommunication.bulkTransfer(readData, buffer, buffer.length, 0);
                                                    if (bytesAvailable1 > 0) {

                                                        for (int i = 0; i < bytesAvailable1; i++) {
                                                            byte packetData = packetBytesImage[i];
                                                            dataList.add(packetData);
                                                        }
                                                        length = bytesAvailable1
                                                                + length;
                                                    }

                                                } catch (Exception ex) {
                                                    ex.printStackTrace();
                                                }
                                                if (length == 4096) {

                                                    byte[] dataBuffer = new byte[dataList
                                                            .size()];
                                                    for (int i = 0; i < dataList
                                                            .size(); i++) {
                                                        dataBuffer[i] = dataList
                                                                .get(i);
                                                    }

                                                    byte[] imageTypeFinger = Arrays
                                                            .copyOfRange(
                                                                    dataBuffer,
                                                                    2, 4094);

                                                    if (j == 0) {

                                                        byte[] imageLength = new byte[4];

                                                        imageLength[0] = 0;
                                                        imageLength[1] = dataBuffer[2];
                                                        imageLength[2] = dataBuffer[3];
                                                        imageLength[3] = dataBuffer[4];

                                                        intImageLength = byteArrayTo4Int(imageLength);

                                                    }

                                                    for (int f = 0; f < imageTypeFinger.length; f++) {
                                                        fingerImageTypeList
                                                                .add(imageTypeFinger[f]);
                                                    }
                                                    fingerIMageTypes[0] = 0x0D;
                                                    fingerIMageTypes[1] = 0x0A;
                                                    try {
                                                        usbCommunication.bulkTransfer(writeData, fingerIMageTypes, fingerIMageTypes.length, 0);
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                        btCallback
                                                                .onBTCommunicationFailed("BROKEN PIPE ERROR");
                                                    }

                                                    length = 0;
                                                    dataList.clear();

                                                    if (imageTypeValue == 1
                                                            && imageTypeFilename != null) {
                                                        btCallback
                                                                .onImageSaved("RAW IMAGE SAVED");
                                                    } else if (imageTypeValue == 2
                                                            && imageTypeFilename != null) {
                                                        btCallback
                                                                .onImageSaved("BMP IMAGE SAVED");
                                                    } else if (imageTypeValue == 3
                                                            && imageTypeFilename != null) {
                                                        btCallback
                                                                .onImageSaved("WSQ IMAGE SAVED");
                                                    }

                                                    LOOP_FLAG1 = false;
                                                }
                                            }

                                        }

                                        byte[] finalDataBuffer = new byte[fingerImageTypeList
                                                .size()];
                                        for (int i = 0; i < fingerImageTypeList
                                                .size(); i++) {
                                            finalDataBuffer[i] = fingerImageTypeList
                                                    .get(i);
                                        }

                                        byte[] finalFingerImageTypes = Arrays
                                                .copyOfRange(
                                                        finalDataBuffer,
                                                        3,
                                                        finalDataBuffer.length
                                                                - (12273 - intImageLength));

                                        FileOutputStream fos = null;

                                        try {

                                            if (imageTypeValue == 1) {

                                                Calendar c = Calendar
                                                        .getInstance();
                                                SimpleDateFormat df = new SimpleDateFormat(
                                                        "yyyy-MM-dd@HH:mm:ss");
                                                String dateAndTime = df
                                                        .format(c.getTime());

                                                if (imageTypeFilename == null) {
                                                    btCallback
                                                            .onRAWImageRecieved(finalFingerImageTypes);

                                                } else {
                                                    String rawImageFileName = imageTypeFilename
                                                            .toString()
                                                            + "-"
                                                            + dateAndTime;

                                                    fos = new FileOutputStream(
                                                            new File(
                                                                    rawImageFileName));
                                                    fos.write(finalFingerImageTypes);
                                                    btCallback
                                                            .onRAWImageRecieved(finalFingerImageTypes);

                                                }

                                            } else if (imageTypeValue == 2) {

                                                Calendar c = Calendar
                                                        .getInstance();
                                                SimpleDateFormat df = new SimpleDateFormat(
                                                        "yyyy-MM-dd@HH:mm:ss");
                                                String dateAndTime = df
                                                        .format(c.getTime());

                                                if (imageTypeFilename == null) {
                                                    btCallback
                                                            .onBMPImageRecieved(
                                                                    finalFingerImageTypes,
                                                                    "");
                                                } else {
                                                    String bmpImageFileName = imageTypeFilename
                                                            .toString()
                                                            + "-"
                                                            + dateAndTime
                                                            + ".bmp";

                                                    fos = new FileOutputStream(
                                                            new File(
                                                                    bmpImageFileName));
                                                    fos.write(finalFingerImageTypes);
                                                    btCallback
                                                            .onBMPImageRecieved(
                                                                    finalFingerImageTypes,
                                                                    bmpImageFileName);

                                                }

                                            } else if (imageTypeValue == 3) {
                                                Calendar c = Calendar
                                                        .getInstance();
                                                SimpleDateFormat df = new SimpleDateFormat(
                                                        "yyyy-MM-dd@HH:mm:ss");
                                                String dateAndTime = df
                                                        .format(c.getTime());

                                                if (imageTypeFilename == null) {
                                                    btCallback
                                                            .onWSQImageRecieved(finalFingerImageTypes);
                                                } else {
                                                    String bmpImageFileName = imageTypeFilename
                                                            .toString()
                                                            + "-"
                                                            + dateAndTime
                                                            + ".wsq";

                                                    fos = new FileOutputStream(
                                                            new File(
                                                                    bmpImageFileName));
                                                    fos.write(finalFingerImageTypes);
                                                    btCallback
                                                            .onWSQImageRecieved(finalFingerImageTypes);

                                                }
                                            }

                                        } catch (FileNotFoundException e) {
                                            Log.d(TAG, "FILE NOT FOUND : " + e);
                                            btCallback
                                                    .onImageFileNotFound("FILE NOT FOUND");
                                            LOOP_FLAG = false;
                                        } catch (IOException ioe) {
                                            Log.d(TAG,
                                                    "EXCEPTION IN FILE WRITING : "
                                                            + ioe);

                                        } finally {
                                            try {
                                                if (fos != null) {
                                                    fos.close();
                                                }
                                            } catch (IOException ioe) {
                                                Log.d(TAG,
                                                        "ERROR WHILE CHOOSING STREAM : "
                                                                + ioe);
                                            }
                                        }
                                    }

                                }

                                if (enrollSuccessCount == enrollScanCount) {

                                    LOOP_FLAG = false;
                                    enrollSuccessCount = 0;
                                    enrollFingerIndex = 0;
                                }

                            } else {
                                LOOP_FLAG = false;
                            }

                        } catch (Exception ex) {
                            ex.printStackTrace();
                            BT_MESSAGE = "BROKEN PIPE ERROR";
                            btCallback.onBTCommunicationFailed(BT_MESSAGE);
                        }

                    }

                }

            }
        });

        workerThread.start();

    }

    private String btIfdErrorMessages(UsbEndpoint readData) {

        LOOP_FLAG = true;
        Log.d(TAG, "Entered in btIfdErrorMessages");
        Log.d(TAG, "THE LOOP CHECKING IN IFD : " + LOOP_FLAG);

        while (LOOP_FLAG) {
            try {
                int bytesAvailable = usbCommunication.bulkTransfer(readData, buffer, buffer.length, 0);
                byte[] received = new byte[bytesAvailable];
                Log.d(TAG, "received bytes : " + bytesAvailable);

                System.arraycopy(buffer, 0, received, 0, bytesAvailable);
                String ackValue = new String(received);
                Log.d(TAG, "::::::::" + ackValue);

                if (bytesAvailable == 1 || bytesAvailable == 2) {

                    if (bytesAvailable == 2) {


                        String btAck = ackValue.replaceAll("\0", "")
                                .replaceAll("\n", "").replaceAll("\f", "")
                                .replaceAll("�", "").trim();
                        Log.d(TAG, "THE 97BT ACK : " + btAck);

                        if (btAck.equals("99")) {
                            BT_MESSAGE = "OPERATION SUCCESS";
                            if (ATR_FLAG = false) {
                                atrResponceCheck = "ATR RESPONCE SUCCESS";
                                ATR_FLAG = true;
                            }
                            // LOOP_FLAG = false;
                        } else if (btAck.equals("06")) {
                            BT_MESSAGE = "MAXIMUM LENGTH EXCEEDED";
                            LOOP_FLAG = false;
                        } else if (btAck.equals("12")) {
                            BT_MESSAGE = "LENGTH ERROR";
                            LOOP_FLAG = false;
                        } else if (btAck.equals("24")) {
                            BT_MESSAGE = "SCR OPEN ERROR";
                            LOOP_FLAG = false;
                        } else if (btAck.equals("25")) {
                            BT_MESSAGE = "CARD SELECTION FAILED";
                            LOOP_FLAG = false;
                        } else if (btAck.equals("26")) {
                            BT_MESSAGE = "CARD NOT PRESENT";
                            LOOP_FLAG = false;
                        } else if (btAck.equals("27")) {
                            BT_MESSAGE = "IFD POWER UP FAILED";
                            LOOP_FLAG = false;
                        } else if (btAck.equals("28")) {
                            BT_MESSAGE = "OPERATION FAILED";
                            LOOP_FLAG = false;
                        }
                    } else if (bytesAvailable == 1) {
                        String byteValue = new String(ackValue).toString();
                        Log.d(TAG, "THE 97BT ACK : " + byteValue);

                        String btAck = byteValue.replaceAll("\0", "")
                                .replaceAll("\n", "").replaceAll("\f", "")
                                .replaceAll("�", "").trim();
                        Log.d(TAG, "THE 97BT ACK : " + btAck);

                        if (btAck.equals("N")) {
                            BT_MESSAGE = "RESEND THE DATA";
                            LOOP_FLAG = false;
                        }
                    }
                } else if (bytesAvailable == 300) {

                    Log.d(TAG, "IF the AVAILABLE BYTES is 300 : " + bytesAvailable);


                    byte[] ifdDataLength = new byte[2];
                    ifdDataLength[0] = received[1];
                    ifdDataLength[1] = received[2];

                    int ifdResponseDataLength = byteArrayTo2Int(ifdDataLength);

                    byte[] ifdResponse = new byte[ifdResponseDataLength];

                    for (int i = 3, j = 0; i < ifdResponseDataLength + 3; i++, j++)
                        ifdResponse[j] = received[i];


                    Log.d(TAG, "IFD Resp is : " + ifdResponse.length);
                    if (ifdResponse.length > 0) {

                        ifdCallback.onIFDATRResponce(ifdResponse);

                        if (ifdResponse.length == 2) {

                            if (ifdResponse[0] == -112 && ifdResponse[1] == 0) {
                                BT_MESSAGE = "OPERATION SUCCESS";
                                LOOP_FLAG = false;
                            } else if (ifdResponse[0] == 105
                                    && ifdResponse[1] == -123) {
                                BT_MESSAGE = "NO RECORDS FOUND";
                                LOOP_FLAG = false;
                            } else if (ifdResponse[0] == 106
                                    && ifdResponse[1] == -122) {
                                BT_MESSAGE = "0 NO RECORD NOT FOUND";
                                LOOP_FLAG = false;
                            }
                        } else if (ifdResponseDataLength == 4) {

                            if (ifdResponse[ifdResponseDataLength - 2] == -112
                                    && ifdResponse[ifdResponseDataLength - 1] == 0) {

                                byte[] ifdRecordNumberBuffer = new byte[2];

                                ifdRecordNumberBuffer[0] = ifdResponse[0];
                                ifdRecordNumberBuffer[1] = ifdResponse[1];

                                int records = byteArrayTo2Int(ifdRecordNumberBuffer);

                                BT_MESSAGE = "Number of Records : "
                                        + Integer.toString(records);
                                LOOP_FLAG = false;
                            }
                        } else if (ifdResponse.length > 4) {

                            List<Byte> ifdRecordList = new ArrayList<Byte>();

                            if (ifdResponse[ifdResponseDataLength - 2] == -112
                                    && ifdResponse[ifdResponseDataLength - 1] == 0) {
                                for (int f = 0; f < ifdResponseDataLength; f++) {
                                    if (ifdResponse[f] == -1) {
                                    } else {
                                        ifdRecordList.add(ifdResponse[f]);
                                    }
                                }

                                byte[] ifdRecordBuffer = new byte[ifdRecordList
                                        .size()];
                                for (int n = 0; n < ifdRecordList.size() - 2; n++) {
                                    ifdRecordBuffer[n] = ifdRecordList.get(n);
                                }

                                BT_MESSAGE = "Record Data : "
                                        + new String(ifdRecordBuffer)
                                        .toString();
                                LOOP_FLAG = false;
                            } else if (ifdResponse[ifdResponseDataLength - 2] == -64
                                    && ifdResponse[ifdResponseDataLength - 1] == 33) {
                                BT_MESSAGE = "CARD NOT PRESENT";
                                LOOP_FLAG = false;
                            } else {
                                if (ATR_FLAG == true
                                        && atrResponceCheck
                                        .equals("ATR RESPONCE SUCCESS")) {
                                    BT_MESSAGE = "OPERATION SUCCESS";
                                    ifdCallback.onIFDATRResponce(ifdResponse);
                                    ATR_FLAG = false;
                                    LOOP_FLAG = false;
                                } else {
                                    LOOP_FLAG = false;
                                }

                            }
                        }

                    }

                } else {
                    LOOP_FLAG = false;
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                return "BROKEN PIPE ERROR";
            }
        }
        return BT_MESSAGE;
    }

    private String btRfidErrorMessages(UsbEndpoint readData) {

        LOOP_FLAG = true;
        Log.d(TAG, "THE LOOP CHECKING : " + LOOP_FLAG);

        while (LOOP_FLAG) {
            try {

                int bytesAvailable = usbCommunication.bulkTransfer(readData, buffer, buffer.length, 0);
                byte[] received = new byte[bytesAvailable];
                Log.d(TAG, "received bytes : " + bytesAvailable);

                System.arraycopy(buffer, 0, received, 0, bytesAvailable);
                String ackValue = new String(received);
                Log.d(TAG, "::::::::" + ackValue);

                if (bytesAvailable == 1 || bytesAvailable == 2) {

                    if (bytesAvailable == 2) {

                        String btAck = ackValue.replaceAll("\0", "")
                                .replaceAll("\n", "").replaceAll("\f", "")
                                .replaceAll("�", "").trim();

                        Log.d(TAG, "THE RFID ACK : " + btAck);

                        if (btAck.equals("34")) {
                            BT_MESSAGE = "TAMA OPEN ERROR";
                            LOOP_FLAG = false;
                        } else if (btAck.equals("35")) {
                            BT_MESSAGE = "TAMA AUTH FAILED";
                            LOOP_FLAG = false;
                        } else if (btAck.equals("36")) {
                            BT_MESSAGE = "TAMA WRITE FAILED";
                            LOOP_FLAG = false;
                        } else if (btAck.equals("37")) {
                            BT_MESSAGE = "TAMA READ FAILED";
                            LOOP_FLAG = false;
                        } else if (btAck.equals("38")) {
                            BT_MESSAGE = "TAMA TARGET NOT FOUND";
                            LOOP_FLAG = false;
                        } else if (btAck.equals("39")) {
                            // BT_MESSAGE = "TAMA READ SUCCESS";
                            // LOOP_FLAG = false;
                        } else if (btAck.equals("40")) {
                            BT_MESSAGE = "TAMA WRITE SUCCESS";
                            LOOP_FLAG = false;
                        }
                    } else if (bytesAvailable == 1) {
                        String byteValue = new String(ackValue).toString();
                        Log.d(TAG, "THE 97BT ACK : " + byteValue);

                        String btAck = byteValue.replaceAll("\0", "")
                                .replaceAll("\n", "").replaceAll("\f", "")
                                .replaceAll("�", "").trim();
                        Log.d(TAG, "THE 97BT ACK : " + btAck);

                        if (btAck.equals("N")) {
                            BT_MESSAGE = "RESEND THE DATA";
                            LOOP_FLAG = false;
                        }
                    }
                } else if (bytesAvailable == 32) {

//                    byte[] rfidDataBytes = new byte[bytesAvailable];
//
//                    Log.d(TAG, "THE AVAILABLE BYTES : " + bytesAvailable);
//                    for (int i = 0; i < bytesAvailable; i++) {
//                        byte b = packetBytes[i];
//                        // Log.d(TAG, "THE BYTE VALUE : " + b);
//                        rfidDataBytes[i] = b;
//                    }
//                    String rfidData = toHexString(rfidDataBytes);

                    BT_MESSAGE = "RFID DATA : " + ackValue;
                    LOOP_FLAG = false;
                } else if (bytesAvailable == 16) {

//                    byte[] rfidDataBytes = new byte[bytesAvailable];
//
//                    Log.d(TAG, "THE AVAILABLE BYTES : " + bytesAvailable);
//                    for (int i = 0; i < bytesAvailable; i++) {
//                        byte b = packetBytes[i];
//                        // Log.d(TAG, "THE BYTE VALUE : " + b);
//                        rfidDataBytes[i] = b;
//                    }
//                    String rfidData = toHexString(rfidDataBytes);

                    BT_MESSAGE = "RFID DATA : " + ackValue;
                    LOOP_FLAG = false;
                } else {
                    Log.d(TAG, "THE AVAILABLE BYTES : " + bytesAvailable);
                    LOOP_FLAG = false;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                return "BROKEN PIPE ERROR";
            }
        }
        return BT_MESSAGE;
    }

    private String btDeviceErrorMessages(UsbEndpoint readData) {

        LOOP_FLAG = true;
        Log.d(TAG, "THE LOOP CHECKING IN DEVICE : " + LOOP_FLAG);
        int k = 1;
        while (LOOP_FLAG) {
            try {
                System.out.println("sub communication : "+usbCommunication);
                if(usbCommunication!=null) {

                    System.out.println("loop Count : " + k++);

//                Thread.sleep(3000);
                    for (int i = 0; i < buffer.length; i++) {
                        buffer[i] = 0;
                    }

                    System.out.println("buffer leng : " + buffer.length + "  elements : " + Arrays.toString(buffer));

                    int bytesAvailable = usbCommunication.bulkTransfer(readData, buffer, buffer.length, 0);
                    System.out.println("buffer leng : " + buffer.length + "  elements : " + Arrays.toString(buffer));
                    System.out.println("show bytes : " + bytesAvailable);


                    if (bytesAvailable == 1 || bytesAvailable == 2) {
                        byte[] ackBytes = new byte[bytesAvailable];

                        Log.d(TAG, "THE AVAILABLE BYTES : " + bytesAvailable);
                        for (int i = 0; i < bytesAvailable; i++) {
                            byte b = buffer[i];
                            Log.d(TAG, "THE BYTE VALUE : " + b);
                            ackBytes[i] = b;
                        }

                        if (bytesAvailable == 2) {

                            String byteValue = new String(ackBytes).toString();
                            Log.d(TAG, "THE 97BT ACK : " + byteValue);

                            String btAck = byteValue.replaceAll("\0", "")
                                    .replaceAll("\n", "").replaceAll("\f", "")
                                    .replaceAll("�", "").trim();
                            Log.d(TAG, "THE 97BT ACK : " + btAck);

                            if (btAck.equals("99")) {
                                BT_MESSAGE = "OPERATION SUCCESS";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("19")) {
//                            Thread.sleep(5000);
                                // BT_MESSAGE = "DEVICE INFO SUCCESS";
                                // LOOP_FLAG = false;
                            } else if (btAck.equals("TS")) {
                                BT_MESSAGE = " TETHERING SUCCESS";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("HS")) {
                                BT_MESSAGE = " H/W ID SUCCESS";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("HW")) {
                                BT_MESSAGE = "Please SET H/W configuration\n"
                                        + "Set by clicking 97BT_Config Button";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("HF")) {
                                BT_MESSAGE = "H/W ID set failed";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("VP")) {
                                BT_MESSAGE = "Scanner not present";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("BN")) {
                                BT_MESSAGE = "Battery not present";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("31")) {

                                BT_MESSAGE = "APP LOAD FAIL";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("32")) {
                                BT_MESSAGE = "USB NOT FOUND";
                                LOOP_FLAG = false;
                            } else if (btAck.equals("RS")) {
                                BT_MESSAGE = "REBOOT SUCCESS";
                                LOOP_FLAG = false;

                            } else if (btAck.equals("IO")) {
                                BT_MESSAGE = "REBOOT FAILED";
                                LOOP_FLAG = false;

                            } else if (btAck.equals("PS")) {
                                BT_MESSAGE = "POWER-OFF-SUCCESS";
                                LOOP_FLAG = false;

                            } else if (btAck.equals("IO")) {
                                BT_MESSAGE = "POWER-OFF-FAILED";
                                LOOP_FLAG = false;

                            } else if (btAck.equals("BE")) {
                                BT_MESSAGE = "BASH-ENABLED";
                                LOOP_FLAG = false;

                            } else if (btAck.equals("NF")) {
                                BT_MESSAGE = "Network failed";
                                LOOP_FLAG = false;

                            } else if (btAck.equals("MC")) {
                                BT_MESSAGE = "Machine_ID not set";
                                LOOP_FLAG = false;

                            } else if (btAck.equals("MW")) {
                                BT_MESSAGE = "Machine_ID Wrong";
                                LOOP_FLAG = false;

                            } else if (btAck.equals("MI")) {
                                BT_MESSAGE = "MacId failed";
                                LOOP_FLAG = false;

                            } else if (btAck.equals("UI")) {
                                BT_MESSAGE = "UID not set";
                                LOOP_FLAG = false;

                            } else if (btAck.equals("XL")) {
                                BT_MESSAGE = "xml creation failed";
                                LOOP_FLAG = false;

                            } else if (btAck.equals("SM")) {
                                BT_MESSAGE = "Server communication success";
                                LOOP_FLAG = false;

                            } else if (btAck.equals("SE")) {
                                BT_MESSAGE = "Server Connetion Error";
                                LOOP_FLAG = false;

                            } else if (btAck.equals("SC")) {
                                BT_MESSAGE = "Server Connected";
                                LOOP_FLAG = false;

                            } else if (btAck.equals("SF")) {
                                BT_MESSAGE = "Server Updation Failed";
                                LOOP_FLAG = false;

                            } else if (btAck.equals("UP")) {
                                BT_MESSAGE = "Updates already available";
                                LOOP_FLAG = false;

                            } else if (btAck.equals("RS")) {
                                BT_MESSAGE = "FTP path Response_error";
                                LOOP_FLAG = false;

                            } else if (btAck.equals("UF")) {
                                BT_MESSAGE = "Updates found";
                                LOOP_FLAG = false;

                            } else if (btAck.equals("UN")) {
                                BT_MESSAGE = "Unable to connect Internet";
                                LOOP_FLAG = false;

                            } else if (btAck.equals("DN")) {
                                BT_MESSAGE = "Downloading";
                                LOOP_FLAG = false;

                            } else if (btAck.equals("DS")) {
                                BT_MESSAGE = "File Downloaded";
                                LOOP_FLAG = false;

                            } else if (btAck.equals("DF")) {
                                BT_MESSAGE = "File Downloading failed";
                                LOOP_FLAG = false;

                            } else if (btAck.equals("AL")) {
                                BT_MESSAGE = "Your patch is Up to date";
                                LOOP_FLAG = false;

                            } else if (btAck.equals("PD")) {
                                BT_MESSAGE = "Patch Downloaded ";
                                LOOP_FLAG = false;

                            } else if (btAck.equals("PF")) {
                                BT_MESSAGE = "Patch Download Failed";
                                LOOP_FLAG = false;

                            } else if (btAck.equals("AF")) {
                                BT_MESSAGE = "Failed to get versions";
                                LOOP_FLAG = false;

                            } else if (btAck.equals("CF")) {
                                BT_MESSAGE = "checksum Failed";
                                LOOP_FLAG = false;

                            } else if (btAck.equals("US")) {
                                BT_MESSAGE = "UPDATE SUCCESS";
                                LOOP_FLAG = false;

                            } else if (btAck.equals("UF")) {
                                BT_MESSAGE = "UPDATE FAILED";
                                LOOP_FLAG = false;

                            } else if (btAck.equals("HV")) {
                                BT_MESSAGE = "HIGHER VERSION NOT AVAILABLE";
                                LOOP_FLAG = false;

                            } else {
                                BT_MESSAGE = "Wrong Data Received";
                                LOOP_FLAG = false;
                            }
                        } else if (bytesAvailable == 1) {


                            String byteValue = new String(ackBytes).toString();
                            Log.d(TAG, "THE 97BT ACK : " + byteValue);

                            String btAck = byteValue.replaceAll("\0", "")
                                    .replaceAll("\n", "").replaceAll("\f", "")
                                    .replaceAll("�", "").trim();
                            Log.d(TAG, "THE 97BT ACK : " + btAck);

                            if (btAck.equals("N")) {
                                BT_MESSAGE = "RESEND THE DATA";
                                LOOP_FLAG = false;
                            }
                        }
                    } else if (bytesAvailable == 325) {
                        Log.d(TAG, "THE AVAILABLE BYTES : " + bytesAvailable);

                        byte[] deviceInfodata = new byte[bytesAvailable];
                        for (int i = 0; i < bytesAvailable; i++) {
                            byte b = buffer[i];

                            deviceInfodata[i] = b;
                        }

                        byte[] ubootVersionData = Arrays.copyOfRange(
                                deviceInfodata, 0, 60);

                        String ubootVersion = new String(ubootVersionData)
                                .toString();

                        ubootVersion = ubootVersion.replace((char) 0, (char) 32)
                                .trim();

                        ubootVersion = ubootVersion.replaceAll("  ", "").trim();

                        Log.d(TAG, "U-Boot Version : " + ubootVersion);

                        byte[] kernelVersionData = Arrays.copyOfRange(
                                deviceInfodata, 60, 110);

                        String kernelVersion = new String(kernelVersionData)
                                .toString();
                        kernelVersion = kernelVersion.replace((char) 0, (char) 32)
                                .trim();

                        kernelVersion = kernelVersion.replaceAll("  ", "").trim();
                        Log.d(TAG, "Kernel Version : " + kernelVersion);

                        byte[] rootfsVersionData = Arrays.copyOfRange(
                                deviceInfodata, 110, 210);

                        String rootfsVersion = new String(rootfsVersionData)
                                .toString();
                        rootfsVersion = rootfsVersion.replace((char) 0, (char) 32)
                                .trim();

                        rootfsVersion = rootfsVersion.replaceAll("  ", "").trim();
                        Log.d(TAG, "Rootfs Version : " + rootfsVersion);

                        byte[] pinpadIdData = Arrays.copyOfRange(deviceInfodata,
                                210, 225);

                        String pinpadId = new String(pinpadIdData).toString();
                        pinpadId = pinpadId.replace((char) 0, (char) 32).trim();

                        pinpadId = pinpadId.replaceAll("  ", "").trim();
                        Log.d(TAG, "Pinpad ID : " + pinpadId);

                        byte[] appVersionData = Arrays.copyOfRange(deviceInfodata,
                                225, 325);

                        String appVersion = new String(appVersionData).toString();
                        appVersion = appVersion.replace((char) 0, (char) 32).trim();

                        appVersion = appVersion.replaceAll("  ", "").trim();
                        Log.d(TAG, "App Version : " + appVersion);

                        String devInfo = "U-Boot Version:  " + ubootVersion
                                + "\n\n" + "Kernel Version:  " + kernelVersion
                                + "\n\n" + "Rootfs Version:  " + rootfsVersion
                                + "\n\n" + "App Version:  " + appVersion + "\n\n"
                                + "Device ID:  " + pinpadId;

                        LOOP_FLAG = false;
                        return devInfo;

                    } else if (bytesAvailable == 28) {
                        Log.d(TAG, "THE AVAILABLE BYTES : " + bytesAvailable);

                        byte[] TotalBytes = new byte[bytesAvailable];
                        for (int i = 0; i < bytesAvailable; i++) {
                            byte b = buffer[i];

                            TotalBytes[i] = b;
                        }

                        byte[] temp_Storage = Arrays.copyOfRange(TotalBytes, 0, 28);

                        // int batterypercentage = temp_Storage[24];

                        String status_of_battery = new String(temp_Storage)
                                .toString();

                        status_of_battery = status_of_battery.replace((char) 0,
                                (char) 32).trim();

                        status_of_battery = status_of_battery.replaceAll("  ", "")
                                .trim();
                        String finalStatus = status_of_battery + "%";
                        // + batterypercentage
                        // + "%";

                        Log.d(TAG, "show Battery & adapter status : " + finalStatus);

                        LOOP_FLAG = false;
                        return finalStatus;

                    } else if (bytesAvailable == 30) {

                        Log.d(TAG, "THE AVAILABLE BYTES : " + bytesAvailable);

                        byte[] TotalBytes = new byte[bytesAvailable];
                        for (int i = 0; i < bytesAvailable; i++) {
                            byte b = buffer[i];

                            TotalBytes[i] = b;
                        }

                        byte[] temp_Storage = Arrays.copyOfRange(TotalBytes, 0, 30);

                        // int batterypercentage = temp_Storage[24];

                        String product_Vendor_ID = new String(temp_Storage)
                                .toString();

                        product_Vendor_ID = product_Vendor_ID.replace((char) 0,
                                (char) 32).trim();

                        product_Vendor_ID = product_Vendor_ID.replaceAll("  ", "")
                                .trim();
                        String finalStatus = product_Vendor_ID;

                        Log.d(TAG, "product_Vendor_ID: " + finalStatus);

                        LOOP_FLAG = false;
                        return finalStatus;

                    } else if (bytesAvailable == 20) {

                        Log.d(TAG, "THE AVAILABLE BYTES : " + bytesAvailable);

                        byte[] TotalBytes = new byte[bytesAvailable];
                        for (int i = 0; i < bytesAvailable; i++) {
                            byte b = buffer[i];

                            TotalBytes[i] = b;
                        }

                        byte[] temp_Storage = Arrays.copyOfRange(TotalBytes, 0, 30);

                        // int batterypercentage = temp_Storage[24];

                        String bluetooth_mobile_Mac = new String(temp_Storage)
                                .toString();

                        bluetooth_mobile_Mac = bluetooth_mobile_Mac.replace(
                                (char) 0, (char) 32).trim();

                        bluetooth_mobile_Mac = bluetooth_mobile_Mac.replaceAll(
                                "  ", "").trim();
                        String finalStatus = bluetooth_mobile_Mac;

                        Log.d(TAG, "bluetooth_mobile_Mac: " + finalStatus);

                        LOOP_FLAG = false;
                        return finalStatus;

                    } else {

                        LOOP_FLAG = false;
                    }
                }else{
                    System.out.println("Device is null");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                return "BROKEN PIPE ERROR";
            }
        }
        return BT_MESSAGE;
    }

    private String btSetLengthErrorMessages(UsbEndpoint readData) {

        LOOP_FLAG = true;
        Log.d(TAG, "THE LOOP CHECKING IN LENGTH SET : " + LOOP_FLAG);

        while (LOOP_FLAG) {
            try {
                //byte[] buffer = new byte[1000];
                int bytesAvailable = usbCommunication.bulkTransfer(readData, buffer, buffer.length, 0);
                Log.d(TAG, "bytes : " + bytesAvailable);

                if (bytesAvailable == 1 || bytesAvailable == 2) {

                    byte[] received = new byte[bytesAvailable];
                    Log.d(TAG, "received bytes : " + Arrays.toString(received));

                    System.arraycopy(buffer, 0, received, 0, bytesAvailable);
                    String ackBytes = new String(received);
                    Log.d(TAG, "::::::::" + ackBytes);


                    if (bytesAvailable == 2) {
                        String byteValue = new String(ackBytes).toString();
                        Log.d(TAG, "THE 97BT ACK : " + byteValue);

                        String btAck = byteValue.replaceAll("\0", "")
                                .replaceAll("\n", "").replaceAll("\f", "")
                                .replaceAll("�", "").trim();
                        Log.d(TAG, "THE 97BT ACK : " + btAck);

                        if (btAck.equals("LS")) {
                            BT_MESSAGE = "LENGTH SUCCESS";
                            LOOP_FLAG = false;
                        } else if (btAck.equals("LF")) {
                            BT_MESSAGE = "LENGTH FAILED";
                            LOOP_FLAG = false;
                        } else if (btAck.equals("IF")) {
                            BT_MESSAGE = "INVALID FONT";
                            LOOP_FLAG = false;
                        }

                    } else if (bytesAvailable == 1) {
                        String byteValue = new String(ackBytes).toString();
                        Log.d(TAG, "THE 97BT ACK : " + byteValue);

                        String btAck = byteValue.replaceAll("\0", "")
                                .replaceAll("\n", "").replaceAll("\f", "")
                                .replaceAll("�", "").trim();
                        Log.d(TAG, "THE 97BT ACK : " + btAck);

                        if (btAck.equals("N")) {
                            BT_MESSAGE = "RESEND THE DATA";
                            LOOP_FLAG = false;
                        }
                    }
                } else {
                    System.out.println("Check bytes in else case : "
                            + bytesAvailable);
                    for (int i = 0; i < bytesAvailable; i++) {
                        byte b = packetBytes[i];
                        Log.d(TAG, "THE BYTE VALUE : " + b);
                        // ackBytes[i] = b;
                    }
                    LOOP_FLAG = false;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                return "BROKEN PIPE ERROR";
            }
        }
        return BT_MESSAGE;
    }

    private String readAckByte(UsbEndpoint readData) {

        LOOP_FLAG = true;
        Log.d(TAG, "THE LOOP CHECKING IN READ ACK BYTE : " + LOOP_FLAG);

        while (LOOP_FLAG) {
            try {

//				byte[] buffer=new byte[1000];
                int bytesAvailable = usbCommunication.bulkTransfer(readData, buffer, buffer.length, 0);
                Log.d(TAG, "THE AVAILABLE BYTES11 : " + bytesAvailable);
                byte[] received = new byte[bytesAvailable];

                System.arraycopy(buffer, 0, received, 0, bytesAvailable);
                String ackValue = new String(received);
                Log.d(TAG, "::::::::" + ackValue);


                if (bytesAvailable == 1) {

                    //byte[] ackBytes = new byte[bytesAvailable];
//                    for (int i = 0; i < bytesAvailable; i++) {
//                        byte b = packetBytes[i];
//                        ackBytes[i] = b;
//                    }

//                    String byteValue = new String(ackBytes).toString();
//                    Log.d(TAG, "THE value of ackbytes : " + byteValue);

                    operationAck = ackValue.replaceAll("\n", "")
                            .replaceAll("\f", "").replaceAll("�", "").trim();
                    operationAck = operationAck.replace((char) 0, (char) 32)
                            .trim();
                    operationAck = operationAck.replaceAll(" ", "").trim();

                    Log.d(TAG, "CHECKSUM CHECKING : " + operationAck);
                    LOOP_FLAG = false;
                } else if (bytesAvailable == 2) {
                    byte[] ackBytes = new byte[bytesAvailable];
                    for (int i = 0; i < bytesAvailable; i++) {
                        byte b = packetBytes[i];
                        ackBytes[i] = b;
                    }
                    String byteValue = new String(ackBytes).toString();

                    operationAck = byteValue.replaceAll("\n", "")
                            .replaceAll("\f", "").replaceAll("�", "").trim();
                    operationAck = operationAck.replace((char) 0, (char) 32)
                            .trim();
                    operationAck = operationAck.replaceAll(" ", "").trim();

                    Log.d(TAG, "CHECKSUM CHECKING : " + operationAck);
                    LOOP_FLAG = false;

                } else if (bytesAvailable == 3) {
                    byte[] ackBytes = new byte[bytesAvailable];
                    for (int i = 0; i < bytesAvailable; i++) {
                        byte b = packetBytes[i];
                        ackBytes[i] = b;
                    }
                    String byteValue = new String(ackBytes).toString();

                    operationAck = byteValue.replaceAll("\n", "")
                            .replaceAll("\f", "").replaceAll("�", "").trim();
                    operationAck = operationAck.replace((char) 0, (char) 32)
                            .trim();
                    operationAck = operationAck.replaceAll(" ", "").trim();

                    Log.d(TAG, "CHECKSUM CHECKING : " + operationAck);
                    LOOP_FLAG = false;

                } else if (bytesAvailable == 29) {
                    byte[] ackBytes = new byte[bytesAvailable];
                    for (int i = 0; i < bytesAvailable; i++) {
                        byte b = packetBytes[i];
                        ackBytes[i] = b;
                    }
                    String byteValue = new String(ackBytes).toString();

                    operationAck = byteValue.replaceAll("\n", "")
                            .replaceAll("\f", "").replaceAll("�", "").trim();
                    operationAck = operationAck.replace((char) 0, (char) 32)
                            .trim();
                    operationAck = operationAck.replaceAll(" ", "").trim();

                    Log.d(TAG, "CHECKSUM CHECKING : " + operationAck);
                    LOOP_FLAG = false;

                } else {
                    operationAck = "Wrong Data Recevied";
                    LOOP_FLAG = false;
                }

            } catch (Exception e) {
                e.printStackTrace();
                return "BROKEN PIPE ERROR";
            }
        }


        return operationAck;
    }

//	public static int readInputStreamWithTimeout(byte[] b, int timeoutMillis)
//			throws IOException {
//
//		int bufferOffset = 0;
//		long maxTimeMillis = System.currentTimeMillis() + timeoutMillis;
//
//		boolean flag1, flag2;
//
//		while ((flag1 = System.currentTimeMillis() < maxTimeMillis)
//				&& (flag2 = bufferOffset < b.length)) {
//			System.out.println("FLAG VALUE 1: " + flag1);
//			System.out.println("FLAG VALUE 2: " + flag2);
//
//			System.out.println("Last looop");
//			int readLength = java.lang.Math.min(is.available(), b.length
//					- bufferOffset);
//			System.out.println("readlength : " + readLength);
//			// can alternatively use bufferedReader, guarded by isReady():
//			int readResult = is.read(b, bufferOffset, readLength);
//			System.out.println("readresult : " + readResult);
//			if (readResult == -1)
//				break;
//			bufferOffset += readResult;
//			System.out.println("bufferoffcet : " + bufferOffset);
//		}
//		System.out.println("bufferoffcet : " + bufferOffset);
//		return bufferOffset;
//
//	}

    public void writeToFile(String data) {
        // Get the directory for the user's public pictures directory.
        String path = Environment.getExternalStorageDirectory()
                + File.separator + "fp_Data";
        // Create the folder.
        File folder = new File(path);
        folder.mkdirs();

        // Create the file.
        File file = new File(folder, "data.txt");
        try {
            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(data);

            myOutWriter.close();

            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    public String updateFileinDevice(UsbEndpoint writeData, UsbEndpoint readData, File updatedFile, String MobileFileName) {
        List<Byte> tempBufferList = new ArrayList<Byte>();
        List<Byte> storeBmpBufferList = new ArrayList<Byte>();

        FileInputStream fileInputStream = null;

        byte[] fileData = new byte[(int) updatedFile.length()];
        try {
            fileInputStream = new FileInputStream(updatedFile);
            try {
                fileInputStream.read(fileData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int FileLength = fileData.length;
        String status = setLength(FileLength + MobileFileName.length() + 5, writeData, readData);

        if (status.equals("LENGTH SUCCESS")) {

            byte[] fileNameBytes = MobileFileName.getBytes();
            int fileNameLength = fileNameBytes.length;

            int fileSize = fileData.length;
            byte fileLengthArray[] = intTo4ByteArray(fileSize);

            System.out.println("filesize/chunkvalue : " + fileSize + " & "
                    + MAX_CHUNK_VALUE);

            quotient = fileSize / MAX_CHUNK_VALUE;
            remainder = fileSize % MAX_CHUNK_VALUE;

            if (remainder != 0) {
                int SizeChunk = fileSize + (MAX_CHUNK_VALUE - remainder);
                quotient = SizeChunk / MAX_CHUNK_VALUE;

                chunkLength = quotient;
            }

            // tempBufferList.add((byte) 1);
            tempBufferList.add((byte) 15);
            tempBufferList.add(fileLengthArray[1]);
            tempBufferList.add(fileLengthArray[2]);
            tempBufferList.add(fileLengthArray[3]);
            byte fileNameArray[] = MobileFileName.getBytes();
            int length = fileNameArray.length;
            intTo2ByteArray(length);

            // tempBufferList.add(fileTextLength[0]);
            // tempBufferList.add(fileTextLength[1]);
            //
            // for (int j = 0; j < length; j++)
            tempBufferList.add((byte) length);
            for (int i = 0; i < fileNameLength; i++)
                tempBufferList.add(fileNameBytes[i]);

            tempBufferList.add((byte) updatedFile.length());

            for (int i = 1; i < updatedFile.length(); i++) {
                tempBufferList.add(fileData[i]);

            }

            // tempBufferList.add(fileLengthArray[1]);
            // tempBufferList.add(fileLengthArray[2]);
            // tempBufferList.add(fileLengthArray[3]);
            // tempBufferList.add((byte) fileNameLength);
            // for (int i = 0; i < fileNameLength; i++)
            // tempBufferList.add(fileNameBytes[i]);
            //
            // for (int i = 0; i < fileSize; i++)
            // tempBufferList.add(fileData[i]);

            storeBmpBufferList.add((byte) START_BIT);
            storeBmpBufferList.add((byte) chunkLength);
            for (int j = 0; j < tempBufferList.size(); j++)
                storeBmpBufferList.add(tempBufferList.get(j));
            storeBmpBufferList.add((byte) 5);
            storeBmpBufferList.add((byte) STOP_BIT);

            for (int i = 0; i < chunkLength; i++) {
                System.out.println("Quotient : " + quotient);
                if (quotient == 1) {
                    System.out.println("Quo value 1 : ");

                    if (quotient == 1 && i > 0) {

                        List<Byte> chunkImage = new ArrayList<Byte>();
                        chunkImage.add((byte) 13);
                        chunkImage.add((byte) (chunkLength - i));

                        for (int c = 0; c < remainder + fileNameLength + 5; c++) {

                            chunkImage.add(tempBufferList.get(c + 4092 * i));
                        }

                        byte[] checkSum = new byte[chunkImage.size()];
                        for (int k = 1, j = 0; k < chunkImage.size(); j++, k++) {
                            checkSum[j] = chunkImage.get(k);
                        }

                        chunkImage.add(checkSum(checkSum));
                        chunkImage.add((byte) 10);

                        byte[] storeBmpBuffer = new byte[chunkImage.size()];
                        for (int n = 0; n < chunkImage.size(); n++) {
                            storeBmpBuffer[n] = chunkImage.get(n);
                        }

                        try {
                            usbCommunication.bulkTransfer(writeData, storeBmpBuffer, storeBmpBuffer.length, 0);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        storeBmpBufferList.set(1, (byte) (chunkLength - i));

                        byte[] checkSum = new byte[remainder + fileNameLength
                                + 8];
                        for (int k = 1, j = 0; k < remainder + fileNameLength
                                + 8; j++, k++) {
                            checkSum[j] = storeBmpBufferList.get(k);
                        }

                        storeBmpBufferList.set(remainder + fileNameLength + 8,
                                checkSum(checkSum));
                        storeBmpBufferList.set(remainder + fileNameLength + 9,
                                (byte) STOP_BIT);

                        byte[] storeBmpBuffer = new byte[remainder
                                + fileNameLength + 10];
                        for (int n = 0; n < remainder + fileNameLength + 10; n++) {
                            storeBmpBuffer[n] = storeBmpBufferList.get(n);
                        }
                        System.out.println("Total buffer size : "
                                + storeBmpBuffer.length);
                        try {
                            usbCommunication.bulkTransfer(writeData, storeBmpBuffer, storeBmpBuffer.length, 0);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                } else if (quotient != 0) {

                    storeBmpBufferList.set(0, (byte) START_BIT);
                    storeBmpBufferList.set(1, (byte) (chunkLength - i));

                    for (int j = 0, k = 2; j < 4092; j++, k++) {
                        storeBmpBufferList.set(k,
                                tempBufferList.get(j + 4092 * i));
                    }

                    byte[] checkSum = new byte[4094];
                    for (int k = 1, j = 0; k < 4094; k++, j++) {
                        checkSum[j] = storeBmpBufferList.get(k);
                    }

                    storeBmpBufferList.set(4094, checkSum(checkSum));
                    storeBmpBufferList.set(4095, (byte) STOP_BIT);

                    byte[] storeBmpBuffer = new byte[4096];
                    for (int n = 0; n < 4096; n++) {
                        storeBmpBuffer[n] = storeBmpBufferList.get(n);
                    }
                    System.out.println("Total buffer size if que !=0: "
                            + storeBmpBuffer.length);

                    try {
                        usbCommunication.bulkTransfer(writeData, storeBmpBuffer, storeBmpBuffer.length, 0);

                    } catch (Exception e) {
                        return "BROKEN PIPE ERROR";
                    }
                    quotient--;
                    System.out.println("Reading : " + ackStatus);

                    ackStatus = readAckByte(readData);
                    System.out.println("Reading : " + ackStatus);
                    if (ackStatus.equals("A")) {
                        System.out.println("Reading : " + ackStatus);
                        btMessage = btPrinterErrorMessages(readData);
                    } else if (ackStatus.equals("N")) {
                        return "RESEND THE DATA";

                    }

                } else {
                    return "CHUNK FAILED";
                }
            }

            ackStatus = readAckByte(readData);
            if (ackStatus.equals("A")) {
                btMessage = btPrinterErrorMessages(readData);
            } else if (ackStatus.equals("N")) {
                return "RESEND THE DATA";
            }

        } else if (status.equals("LENGTH FAILED")) {
            return "LENGTH SET FAILED";
        } else if (status.equals("BROKEN PIPE ERROR")) {
            return "BROKEN PIPE ERROR";
        } else {
            return "ERROR IN APP";
        }
        System.out.println("btMessage " + btMessage);
        return btMessage;

    }


}
