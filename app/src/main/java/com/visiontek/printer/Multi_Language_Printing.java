package com.visiontek.printer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.example.a97bt_usb_application.R;
import com.example.com.example.usbserial.UsbSerialDevice;
import com.example.mylibrary.Converter;
import com.example.mylibrary.SaveImage;
import com.example.mylibrary.VisiontekUSB;

import static com.example.a97bt_usb_application.UsbService.TAG;


public class Multi_Language_Printing extends Activity {
    private Button print_Telugu;
    private Button print_Hindi;
    private Button print_Tamil;
    private Button print_Bengali;
    private Button print_Gujarati;
    private String print_Language;
    File sdcard, hwFile;
    String bmp_file;
    VisiontekUSB vusb = new VisiontekUSB();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.multi_language_printing);
        print_Telugu = (Button) findViewById(R.id.telugu);
        print_Hindi = (Button) findViewById(R.id.hindi);
        print_Bengali = (Button) findViewById(R.id.bengali);
        print_Gujarati = (Button) findViewById(R.id.gujarathi);
        print_Tamil = (Button) findViewById(R.id.tamil);
        print_Telugu.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                print_Telugu();
            }
        });
        print_Hindi.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                print_Hindi();
            }
        });
        print_Bengali.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                print_Bengali();
            }
        });
        print_Gujarati.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                print_Gujarati();
            }
        });
        print_Tamil.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                print_Tamil();
            }
        });

    }

    private void print_Telugu() {
        print_Language = "వాషింగ్టన్‌: అమెరికా అధ్యక్షుడిగా బాధ్యతలు చేపట్టిన తర్వాత "
                + "డొనాల్డ్‌ ట్రంప్‌ తొలి సంతకం 'ఒబామా కేర్‌' ఆరోగ్య పథకం"
                + " నిబంధనల సడలింపు దస్త్రంపై చేశారు. ఎన్నికల సమయంలో ఆయన"
                + " పలుమార్లు ఒబామాకేర్‌ను రద్దుచేస్తానని హామీ ఇచ్చిన విధంగానే తొలి సంతకం ఆ ఫైల్‌పైనే చేశారు."
                + " ట్రంప్‌ ప్రమాణస్వీకారోత్సవం ముగిసిన తర్వాత ఓవల్‌ కార్యాలయానికి వెళ్లి 'ఎఫర్డబుల్‌ కేర్‌ యాక్ట్‌'పై సంతకం "
                + "చేసినట్లు వైట్‌హౌస్‌ ప్రెస్‌ సెక్రటరీ సీన్‌ స్పైసర్‌ వెల్లడించారు. ఒబామా కేర్‌ వల్ల వివిధ విభాగాలు, ఏజెన్సీలపై పడుతున్న ఆర్థిక భారాన్ని తగ్గించాలని దాన్ని ఉపసంహరిస్తున్నట్లు పేర్కొన్నారు";

        String telugu_Bill = "AMMA UNAVAGAM (A.P) BILL\n"
                + "Date 01/08/18                  Time 11:09PM      \n"
                + "BILL # 3\n"
                + "--------------------------------------------\n"
                + "#ITEM             PRICE       QTY        VALUE   \n"
                + "-------------------------------------- -----\n"
                + "శనగ పప్పు           25     250gm            25       \n"
                + "కంది పప్పు           95    1000gm           95        \n"
                + "మినపప్పు           110    1000gm         110       \n"
                + "పసుపు                 50     250gm            50       \n"
                + "సగ్గుబియ్యం         40     200gm            40       \n"
                + "బియ్యం               45    6000gm            27      \n"
                + "చింతపండు         70     500gm            70      \n"
                + "--------------------------------------------\n"
                + "Total                                            Rs : 660/- \n"
                + "--------------------------------------------";

        String sampleTelugu = "రుణ కలెక్షన్ రసీదు\n";
        String data = "------------------------------------------------\n"
                + "బ్రాంచ్ | SM ID             :     10: 08 | 71702\n"
                + "తేదీ | సమయం              :     25/05/2018 | 12:13 PM\n"
                + "సభ్యుడు ID                 :     10: 08: 032: 11: 006\n"
                + "లోన్ పద్ధతి                   :    LTL2 / MTL6\n"
                + "నసీం బాను\n" + "అమ్ట్ డియు (PRI. + INT.) (రూ.):  1199/495\n"
                + "O / S PRI. బాల. (రూ.)       :  12306/3897\n"
                + "------------------------------------------------ ";

        String BoldData = GetUnicode(sampleTelugu, "Bold_Data");
        String normal_Data = GetUnicode(data, "Normal_Data");

        if (printMultiLanguage(telugu_Bill).equals("SUCCESS")) {

            sdcard = Environment.getExternalStorageDirectory();

            Log.d(TAG, "SDCARD PATH : " + sdcard);
            bmp_file = sdcard + "/" + "print.png";
            // Log.d(TAG, "BMP FILE PATH : " + bmp_file);

            hwFile = new File(bmp_file);

            vusb.printDynamicImage(UsbSerialDevice.outEndpoint, UsbSerialDevice.inEndpoint, hwFile.getAbsoluteFile());


        }

    }

    private void print_Hindi() {

        print_Language = "आपके इस स्नेह्पूर्ण और जोरदार स्वागत से मेरा"
                + "\n"
                + " हृदय आपार हर्ष से भर गया है। मैं आपको दुनिया के सबसे पौराणिक भिक्षुओं की"
                + " तरफ से धन्यवाद् देता हूँ। मैं आपको सभी धर्मों की जननी कि तरफ से धन्यवाद् देता हूँ,"
                + " और मैं आपको सभी जाति-संप्रदाय के लाखों-करोड़ों हिन्दुओं की तरफ से धन्यवाद् देता हूँ।"
                + " मेरा धन्यवाद् उन वक्ताओं को भी जिन्होंने ने इस मंच से यह कहा कि दुनिया में शहनशीलता का विचार सुदूर पूरब के देशों से फैला है।";
        String sample = "    धरणगाव नगर परीषद\n"
                + "        घरपटी बिल\n"
                + "**************************\n"
                + "वर्ष : 2016-2017  दिनांक: 01/06/2016\n"
                + "बिल क्रमांक : 2001\n"
                + "ग्राहक नाव: विजय जाधव\n"
                + " कर              मागील          चालू          एकूण\n"
                + "कर               722           394         116.0\n"
                + "वि.स्व.कर       210            100          102 .0\n"
                + "शिक्षण फंड        16               9            25.0\n"
                + "वृ.कर            163            234           97.0\n"
                + "रो.ह.कर            0               0              0.0\n"
                // + "अ.से.शु      16           8         23.0\n"
                // + "दि.कर      279      152        431.0\n"
                // + "थ.र.व्याज    70       38        108.0\n"
                // + "एकूण       0.0       635.0   1800.0\n"
                + "बिल देणाऱ्याचे  नाव   विजय जाधव\n"
                + "रोख   रु 1800.0 मिळाले\n" + "***************************\n"
                + "हि अस्थायी कर पावती आहे\n" + "मूळ पावती पालिकेतून घ्यावी\n";

        // bt.setFontAlign(BTConnector.mOutputStream, BTConnector.mInputStream,
        // 0);
        if (printMultiLanguage(sample).equals("SUCCESS")) {

            sdcard = Environment.getExternalStorageDirectory();

            // Log.d(TAG, "SDCARD PATH : " + sdcard);
            bmp_file = sdcard + "/" + "print.png";
            // Log.d(TAG, "BMP FILE PATH : " + bmp_file);

            hwFile = new File(bmp_file);
			vusb.setFontAlign(UsbSerialDevice.outEndpoint,UsbSerialDevice.inEndpoint, 2);

			vusb.printDynamicImage(UsbSerialDevice.outEndpoint,UsbSerialDevice.inEndpoint, hwFile.getAbsoluteFile());

        }

    }

    private void print_Tamil() {
        print_Language = "சுவாமி விவேகானந்தர் அவர்கள், "
                + "வேதாந்த தத்துவத்தின் மிக செல்வாக்கு மிக்க"
                + " ஆன்மீக தலைவர்களுள் ஒருவராக தலைச்சிறந்து"
                + " விளங்குபவர். அவர் ராமகிருஷ்ணா பரமஹம்சரின் தலைமை"
                + "சீடராவார். மேலும் ‘ஸ்ரீ ராமகிருஷ்ணர் மடம்’ மற்றும் ஸ்ரீ ‘ராமகிருஷ்ணா"
                + " மிஷன்’ போன்ற அமைப்புகளையும் நிறுவியவர். சுவாமி விவேகானந்தர் அவர்கள்,";
        if (printMultiLanguage(print_Language).equals("SUCCESS")) {

            sdcard = Environment.getExternalStorageDirectory();

             Log.d(TAG, "SDCARD PATH : " + sdcard);
            bmp_file = sdcard + "/" + "print.png";
             Log.d(TAG, "BMP FILE PATH : " + bmp_file);

            hwFile = new File(bmp_file);

			vusb.printDynamicImage(UsbSerialDevice.outEndpoint,UsbSerialDevice.inEndpoint, hwFile.getAbsoluteFile());

        }
    }

    private void print_Bengali() {
        print_Language = "আজকাল লোকে 'যোগ্যতমের উদবর্তন' —রূপ নূতন"
                + " মতবাদ লইয়া অনেক কথা বলিয়া থাকে। তাহারা মনে করে—যাহার"
                + " গায়ের জোর যত বেশী, সেই তত অধিক দিন জীবিত থাকিবে। যদি"
                + " তাহাই সত্য হইত, তবেপ্রাচীনকালের যে-সকল জাতি কেবল অন্যান্য "
                + "জাতির সহিত যুদ্ধ—বিগ্রহে কাটাইয়াছে, তাহারাই মহাগৌরবের সহিত"
                + " আজও জীবিত থাকিত এবং এই হিন্দুজাতি, যাহারা অপর একটি জাতিকে জয়";
        if (printMultiLanguage(print_Language).equals("SUCCESS")) {

            sdcard = Environment.getExternalStorageDirectory();

             Log.d(TAG, "SDCARD PATH : " + sdcard);
            bmp_file = sdcard + "/" + "print.png";
             Log.d(TAG, "BMP FILE PATH : " + bmp_file);

            hwFile = new File(bmp_file);

			vusb.printDynamicImage(UsbSerialDevice.outEndpoint,UsbSerialDevice.inEndpoint, hwFile.getAbsoluteFile());

        }
    }

    private void print_Gujarati() {
        print_Language = "સ્વામી વિવેકાનંદ (બંગાળી: স্বামী বিবেকানন্দ, શામી બિબેકાનંદો) "
                + "(૧૨ જાન્યુઆરી, ૧૮૬    ૩–૪ જુલાઇ, ૧૯૦૨), જન્મે નરેન્દ્રનાથ દત્ત [૨]"
                + " ૧૯મી સદીના ગુઢવાદી સંત રામકૃષ્ણના પરમ શિષ્ય રામકૃષ્ણ મિશનના સ્થાપક "
                + "છે.[૩] યુરોપ અને અમેરિકામાં વેદાંત અને યોગના જન્મદાતા ગણવામાં આવે"
                + " છે[૩] અને તેમને પરસ્પરની આસ્થા ઉભી કરવાનો તથા ૧૯મી સદીના અંતે"
                + " હિન્દુ ધર્મને વિશ્વકક્ષાએ માન્યતા અપાવવાનો શ્રેય આપવામાં આવે છે";
        if (printMultiLanguage(print_Language).equals("SUCCESS")) {

            sdcard = Environment.getExternalStorageDirectory();

             Log.d(TAG, "SDCARD PATH : " + sdcard);
            bmp_file = sdcard + "/" + "print.png";
             Log.d(TAG, "BMP FILE PATH : " + bmp_file);

            hwFile = new File(bmp_file);

			vusb.printDynamicImage(UsbSerialDevice.outEndpoint,UsbSerialDevice.inEndpoint, hwFile.getAbsoluteFile());

        }
    }

    public String printMultiLanguage(String printData) {
        Converter convert = new Converter();

        Bitmap bmp = convert.textAsBitmap(printData, 16, 5, Color.RED,
                Typeface.DEFAULT_BOLD);

        Bitmap image = convert.addBorder(bmp, 2, Color.WHITE);

        String filename = "print.png";

        SaveImage saveimg = new SaveImage();
        boolean check = saveimg.storeImage(image, filename);
        System.out.println("CHECK : " + check);

        if (check == true) {
            return "SUCCESS";
        } else {
            return "FAILED";
        }

    }

    public static String GetUnicode(String str_code, String typeOfData) {
        String myunicode = null;
        String hexcode = null;
        for (int i = 0; i < str_code.length(); i++) {
            System.out.println("loop : " + i);

            hexcode = Integer.toHexString(str_code.codePointAt(i));
            String hexCodeWithAllLeadingZeros = "0000" + hexcode;
            String hexCodeWithLeadingZeros = hexCodeWithAllLeadingZeros
                    .substring(hexCodeWithAllLeadingZeros.length() - 4);
            myunicode += "\\u" + hexCodeWithLeadingZeros;
            System.out.println("\\u" + hexCodeWithLeadingZeros);

        }
        writeToFile(myunicode, typeOfData);

        System.out.println("myunicode : " + myunicode);
        return myunicode;
    }

    public static void writeToFile(String data, String typeOfData) {
        // Get the directory for the user's public pictures directory.
        String path = Environment.getExternalStorageDirectory()
                + File.separator + "FileData";
        // Create the folder.
        File folder = new File(path);
        folder.mkdirs();
        System.out.println("Folder created");

        // Create the file.
        File file = new File(folder, typeOfData);
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

}
