package com.blueapple.testing_encryption;


import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.blueapple.testing_encryption.Model.VideoDetails;
import com.blueapple.testing_encryption.Utillity.Environment4;
import com.blueapple.testing_encryption.Utillity.MyUtilityClass;
import com.google.gson.Gson;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import fr.maxcom.http.LocalSingleHttpServer;
import fr.maxcom.libmedia.Licensing;


public class Video_Activity extends AppCompatActivity
{
    private static final String TAG = "MediaPlayerDemo";
    private int mVideoWidth;
    private int mVideoHeight;
    private MediaPlayer mMediaPlayer;
    private SurfaceHolder holder;
    private String path,final_path;
    String title;
    private boolean mIsVideoSizeKnown = false;
    private boolean mIsVideoReadyToBePlayed = false;
 //   VideoControllerView controller;


    String token = "blOe&^8Hc12^wTue";


   // blOe&^8Hc12^wTue
    VideoView videoView_main;

    VideoDetails videoDetails;
   LocalSingleHttpServer mServer;

    boolean isFromSnopsis = false;
    int currentIndexPlaying = 0;
    List<VideoDetails> mPlayingList;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_video_);


        Licensing.allow(this);

        StorageManager storageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {

            if (storageManager != null)
            {
                List<StorageVolume> disks = storageManager.getStorageVolumes();

                for (StorageVolume volume : disks) {
                    try {
                        Method method = volume.getClass().getMethod("getPath");

                        path = (String) method.invoke(volume);
                        Log.e("path", "" + path);


                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        else {
            try {
                Environment4.Device[] listOfDevices = Environment4.getExternalStorage(this);
                for (Environment4.Device device : listOfDevices) {
                    Log.e("storage", device.getPath());

                    path=device.getPath();

               /*     if (MyUtilityClass.isContentOrThumbdirIsPresent(device.getPath() + "/" + ConstantUtil.JSON_FILE_NAME)
                            && MyUtilityClass
                            .isContentOrThumbdirIsPresent(device.getPath() + "/" + ConstantUtil.SYS_FILE_NAME))
                    {
                        Config.LOG("sd path", device.getPath() + "/" + ConstantUtil.JSON_FILE_NAME);
                        ConfigUtil.setDirectoryPath(device.getPath());

                    }*/
                }
            } catch (Exception e1) {
                e1.printStackTrace();

            }
        }


        final String jsonFileName = ( path+ "/"+"content_demo/learning.json");

        final String jsonContent = MyUtilityClass.readappfile(jsonFileName);

        videoDetails=new Gson().fromJson(jsonContent,VideoDetails.class);

        //  Licensing.allow(getApplicationContext());

        videoView_main=findViewById(R.id.videoView_main_id);

        Log.d("jsonfilename",jsonFileName);



        Uri uri= Uri.parse(
                Environment.getExternalStorageDirectory().getAbsolutePath()+videoDetails.getVideo());

        Log.d("uri_mainpath", String.valueOf(uri));


        //videoView.setVideoPath( videoDetails.getVideo());


        playENCVideo(String.valueOf(uri));


    }

    public void playENCVideo(String path)
    {


            Cipher decipher = null;
        try {

            mServer = new LocalSingleHttpServer();

            String secretkey = token;

            SecretKeySpec sks = new SecretKeySpec(secretkey.getBytes(), "AES");

            decipher = Cipher.getInstance("AES");

            decipher.init(Cipher.DECRYPT_MODE, sks);


            mServer.setCipher(decipher);
            mServer.start();

            final_path = mServer.getURL(path);

            Uri uri = Uri.parse(final_path);

            videoView_main.setVideoURI(uri);

            videoView_main.setMediaController(new MediaController(this));
            videoView_main.start();


          //  videoView_main.setVideoPath(final_path);

            Log.d("finalpath",final_path);

            videoView_main.start();
        }
        catch (Exception e)
        {
            Log.d("exception_main",e.getMessage());
        }


    }

}
