package com.blueapple.testing_encryption.htmlserver;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class HtmlServer extends NanoHTTPD {
    public static final int BYTES_BUFFER_SIZE = 32 * 1024;
    public static final String MIME_PLAINTEXT = "text/plain",
            MIME_HTML = "text/html",
            MIME_JS = "application/javascript",
            MIME_CSS = "text/css",
            MIME_PNG = "image/png",
            MIME_GIF = "image/gif",
            MIME_JPG = "image/jpg",
            MIME_MP3 = "audio/mpeg",
            MIME_MP4 = "video/mp4",
            MIME_DEFAULT_BINARY = "application/octet-stream",
            MIME_XML = "text/xml",
            MIME_ICO = "image/x-icon";
    //private final static int PORT = 8080;
    private final static String TAG = "My Server";

    public HtmlServer(int PORT) throws IOException {
        super(PORT);
        System.out.println("\nRunning! Point your browers to http://localhost:" + PORT);
        start();
    }

    public String getRelativeUrl(String relativePath) {
        return String.format("http://localhost:%d%s", getListeningPort(), relativePath);
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Log.d(TAG, "SERVE ::  URI " + uri);
        if (uri == null) {
            return null;
        }
        File file = new File(uri);
        if (file.exists() && file.isFile()) {
            try {
                if (uri.endsWith(".js") || uri.endsWith(".jpg") || uri.endsWith(".png") || uri.endsWith(".html") || uri.endsWith(".htm") || uri.endsWith(".css") || uri.endsWith(".mp4") || uri.endsWith(".mp3")) {
                    byte[] cipherStreamText = cipherInputStream(file);
                    ByteArrayInputStream stream = new ByteArrayInputStream(cipherStreamText);
                    return NanoHTTPD.newFixedLengthResponse(Response.Status.OK, contentType(uri), stream, stream.available());
                } else {
                    FileInputStream fileInputStream = new FileInputStream(file);
                    return NanoHTTPD.newFixedLengthResponse(Response.Status.OK, contentType(uri), fileInputStream, fileInputStream.available());
                }
            } catch (IOException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException ex) {
                Logger.getLogger(HtmlServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }


    private byte[] cipherInputStream(File file) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IOException {
        String Token = "blOe&^8Hc12^wTue";

        Log.e("TOKEN", "" + Token);
        SecretKeySpec sks = new SecretKeySpec(Token.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, sks);
        FileInputStream fileInputStream = new FileInputStream(file);
        ByteArrayOutputStream bodecrpt = new ByteArrayOutputStream(fileInputStream.available());
        CipherInputStream cis = new CipherInputStream(
                fileInputStream, cipher);
        int lastByte;
        byte[] bytes = new byte[BYTES_BUFFER_SIZE];
        while ((lastByte = cis.read(bytes)) != -1) {
            bodecrpt.write(bytes, 0, lastByte);
        }
        return bodecrpt.toByteArray();
    }

    private static String contentType(String fileName) {
        System.err.println("Filename - " + fileName);
        if (fileName.endsWith(".htm") || fileName.endsWith(".html")) {
            return MIME_HTML;
        }
        if (fileName.endsWith(".txt")) {
            return MIME_PLAINTEXT;
        }
        if (fileName.endsWith(".css")) {
            return MIME_CSS;
        }
        if (fileName.endsWith(".js")) {
            return MIME_JS;
        }
        if (fileName.endsWith(".png")) {
            return MIME_PNG;
        }
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return MIME_JPG;
        }
        if (fileName.endsWith(".gif")) {
            return MIME_GIF;
        }
        if (fileName.endsWith(".mp4")) {
            return MIME_MP4;
        }
        if (fileName.endsWith(".xml")) {
            return MIME_XML;
        }
        if (fileName.endsWith(".ico")) {
            return MIME_ICO;
        }
        if (fileName.endsWith(".mp3") || fileName.endsWith(".mpeg")) {
            return MIME_MP3;
        } else {
            return MIME_DEFAULT_BINARY;
        }
    }

}