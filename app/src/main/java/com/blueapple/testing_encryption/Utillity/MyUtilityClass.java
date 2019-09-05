package com.blueapple.testing_encryption.Utillity;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MyUtilityClass {
    public static final int THUMBNAIL_HEIGHT = 200;
    public static final int THUMBNAIL_WIDTH = 180;
    private static final Pattern DIR_SEPORATOR = Pattern.compile("/");
    private static final String TAG = "MyUtilityClass";
    public static String MEMBLOCK;

    public static char RandomChar() {
        Random rad = new Random();
        int num = (int) (rad.nextDouble() * 200);
        return (char) num;
    }

   /* public static void callUs(Context context) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + context.getResources().getString(R.string.call_us_number)));
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            AlertUtil.showToast("Calling permission not enabled", context, true);
            return;
        }
        context.startActivity(intent);
    }*/

 /*   public static void emailUs(Context context) {
        String emailTo = context.getResources().getString(R.string.email_us_address);
        String emailSubject = context.getResources().getString(R.string.email_subject);
        String emailContent = context.getResources().getString(R.string.email_content);
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailTo});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, emailContent);
        emailIntent.setType("plain/text");
        emailIntent.setType("message/rfc822");
        context.startActivity(Intent.createChooser(emailIntent, "Select an Option:"));
    }*/

    public static boolean isvalidemail(String inputEmail) {
        String regExpn = "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?" + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?" + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$";
        Pattern patternObj = Pattern.compile(regExpn);
        Matcher matcherObj = patternObj.matcher(inputEmail);
        return matcherObj.matches();
    }

    public static boolean isValidMobile(String phone) {
        return android.util.Patterns.PHONE.matcher(phone).matches();
    }

    public static String getCountryName(Context context) {
        String country = context.getResources().getConfiguration().locale.getDisplayCountry();
        if (TextUtils.isEmpty(country))
            return "None";
        else
            return country;
    }
/*
    public static String getDeviceID(Context context) {
        String samsungid_temp = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
        if (StringHandler.isStringEmptyOrNull(samsungid_temp)) {
            return "";
        }
        return samsungid_temp;
    }*/


    public static String readappfile(String appsysfilepath) {
        String displayText = "";
        try {
            InputStream fileStream = new FileInputStream(appsysfilepath);
            int fileLen = fileStream.available();
            // Read the entire resource into a local byte buffer.
            byte[] fileBuffer = new byte[fileLen];
            fileStream.read(fileBuffer);
            fileStream.close();
            displayText = new String(fileBuffer);
        } catch (IOException e) {

            Log.d("exception",e.getMessage());
            // exception handling
        }
        return displayText;
    }

    public static boolean isContentOrThumbdirIsPresent(String ExternalSdcard) {
        boolean b;
        try {
            File file1 = new File(ExternalSdcard);
            b = file1.exists();
        } catch (Exception e) {
            return false;
        }
        return b;
    }

    public static long getDifferenceBwDates(Date startDate, Date endDate) {
        long diff = endDate.getTime() - startDate.getTime();
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        return days;
    }


    public static String decryptappsys(String secretkey, String appString) {
        /*
         * byte[] ivBytes = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x00, 0x01,
         * 0x02, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01 };
         * IvParameterSpec mybyte = new IvParameterSpec(ivBytes);
         */
        Log.e("appString", "" + appString);
        String mystring = null;

        SecretKeySpec sks = new SecretKeySpec(secretkey.getBytes(), "AES");
        Cipher cipher = null;
        try {
            try {
                cipher = Cipher.getInstance("AES");
            }
            catch (NoSuchAlgorithmException e) {

                e.printStackTrace();
            } catch (NoSuchPaddingException e) {

                e.printStackTrace();
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
        try {
            try {
                cipher.init(Cipher.DECRYPT_MODE, sks);

            } catch (InvalidKeyException e) {

                e.printStackTrace();
            }
        } catch (Exception e) {

            e.printStackTrace();
        }

        /*
         * SecretKeyFactory factory =
         * SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1"); KeySpec spec =
         * new PBEKeySpec(PBKDF2WithHmacSHA1.toCharArray(),
         * PBKDF2WithHmacSHA1.getBytes(), 128, 256); SecretKey tmp =
         * factory.generateSecret(spec); SecretKey key = new
         * SecretKeySpec(tmp.getEncoded(), ALGORITHM);
         *
         * Cipher cipher = Cipher.getInstance(ALGORITHM);
         *
         * cipher.init(Cipher.DECRYPT_MODE, key);
         */

        try {
            // String str = new String(bytes, "UTF-8")
            try {

                if (cipher!=null) {

                    byte[] b=cipher.doFinal(toByte(appString));

                    for (int i=0;i<b.length;i++)
                    {
                        Log.d("data "+i, String.valueOf(b[i]));
                    }

                    mystring = new String(cipher.doFinal(toByte(appString)));



                    Log.d("mystring", mystring);
                }
            }
            catch (Exception e) {

                e.printStackTrace();

                Log.d("exception_mystring",e.getMessage());
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
        return mystring;
    }


    // Algorithm used
    private final static String ALGORITHM = "AES";
    private final static String HEX = "0123456789ABCDEF";


    private static byte[] toByte(String hexString) {
        int len = hexString.length() / 2;

        byte[] result = new byte[len];

        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
        return result;
    }

    public static String toHex(byte[] stringBytes) {
        StringBuffer result = new StringBuffer(2 * stringBytes.length);
        for (int i = 0; i < stringBytes.length; i++) {
            result.append(HEX.charAt((stringBytes[i] >> 4) & 0x0f)).append(HEX.charAt(stringBytes[i] & 0x0f));
        }
        return result.toString();
    }


    public static String hitTimeZoneRequest(String url) {
        URL xmlURL = null;
        try {
            xmlURL = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        InputStream xml;
        try {
            xml = xmlURL.openStream();
        } catch (IOException e) {
            e.printStackTrace();
            return "-1";
        }
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        try {
            Document doc = db.parse(xml);
            NodeList lst = doc.getElementsByTagName("localtime");
            return lst.item(0).getTextContent();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "-1";


    }

/*
    public static String Encrypt(String toEncrypt) {
        String Encrypted = "";
        String Token = BuildConfig.TOKEN;
        ByteArrayInputStream fiss = new ByteArrayInputStream(toEncrypt.getBytes());
        // FileInputStream fiss = new FileInputStream(Source);
        // This stream write the encrypted text. This stream will be wrapped
        // by
        // another stream.
        ByteArrayOutputStream fos = new ByteArrayOutputStream();
        try {
            SecretKeySpec sks = new SecretKeySpec(Token.getBytes(), "AES");
            // Create cipher
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, sks);

            CipherOutputStream cos = new CipherOutputStream(fos, cipher);
            // Write bytes
            int b;
            byte[] d = new byte[8];
            while ((b = fiss.read(d)) != -1) {
                cos.write(d, 0, b);
            }
            cos.flush();
            cos.close();
            fiss.close();

            // Encrypted = fos.toByteArray().toString();
            Encrypted = toHex(cipher.doFinal(toEncrypt.getBytes()));
            fos.close();
        } catch (Exception e) {
            int i = 0;
            i = i + 2;
        }
        return Encrypted;
    }*/

   /* public static String EncryptWhole(String toEncrypt) {
        String Encrypted = "";
        String Token = BuildConfig.TOKEN_WHOLE;
        ByteArrayInputStream fiss = new ByteArrayInputStream(toEncrypt.getBytes());
        // FileInputStream fiss = new FileInputStream(Source);
        // This stream write the encrypted text. This stream will be wrapped
        // by
        // another stream.
        // ByteArrayOutputStream fos = new ByteArrayOutputStream();
        try {
            SecretKeySpec sks = new SecretKeySpec(Token.getBytes(), "AES");
            // Create cipher
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, sks);

            // CipherOutputStream cos = new CipherOutputStream(fos, cipher);
            // Write bytes
            // int b;
            // byte[] d = new byte[8];
            // while ((b = fiss.read(d)) != -1) {
            // cos.write(d, 0, b);
            // }
            // cos.flush();
            // cos.close();
            // fiss.close();
            // Encrypted = fos.toByteArray().toString();
            Encrypted = toHex(cipher.doFinal(toEncrypt.getBytes()));
            ;

            // fos.close();
        } catch (Exception e) {
            int i = 0;
            i = i + 2;
        }
        return Encrypted;
    }*/


/*    public static boolean updateLicence(String[] appfiledataarray, String modifiedlastaccessDate, int index,
                                        ContentResolver contentResolver)
    {
        String toWrite = "";
        for (int i = 0; i < appfiledataarray.length; i++) {
            if (i != index) {
                toWrite += appfiledataarray[i] + ",";
            } else {
                toWrite += RandomChar() + Encrypt(modifiedlastaccessDate) + RandomChar() + ",";
            }
        }
        toWrite = EncryptWhole(toWrite);
        try {

            File file = new File(ConfigUtil.getDirectoryPath(), "app.sys");
            FileOutputStream overWrite = new FileOutputStream(file, false);
            overWrite.write(toWrite.getBytes(Charset.forName("UTF-8")));
            overWrite.close();
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }*/

}
