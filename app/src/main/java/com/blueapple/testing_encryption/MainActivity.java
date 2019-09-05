package com.blueapple.testing_encryption;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.blueapple.testing_encryption.Model.VideoDetails;
import com.blueapple.testing_encryption.Utillity.Environment4;
import com.blueapple.testing_encryption.Utillity.MyUtilityClass;
import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.CALL_PHONE;
import static android.Manifest.permission.INSTALL_SHORTCUT;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;


public class MainActivity extends AppCompatActivity
{

    String path;

    ImageLoader imageLoader;
    VideoDetails videoDetails;
    VideoView videoView;
    ImageView imageView;
    DisplayImageOptions defaultOptions;

    private MediaPlayer mMediaPlayer;
    private SurfaceHolder holder;

    Button btn;

    private ArrayList<String> permissionsToRequest;
    ArrayList<String> permissions = new ArrayList<>();
    Handler mLoadingHandler = new Handler();
    private final static int ALL_PERMISSIONS_RESULT = 107;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        videoView=findViewById(R.id.videoView_id);
        imageView=findViewById(R.id.imageView_id);

        permissions.clear();
        permissions.add(INTERNET);
        permissions.add(ACCESS_NETWORK_STATE);
        permissions.add(READ_EXTERNAL_STORAGE);
        permissions.add(WRITE_EXTERNAL_STORAGE);
        permissions.add(CALL_PHONE);
        permissions.add(ACCESS_COARSE_LOCATION);
        permissions.add(ACCESS_FINE_LOCATION);
        permissions.add(INSTALL_SHORTCUT);
        requestPermission();

        imageLoader=ImageLoader.getInstance();
        btn=findViewById(R.id.btn_id);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,Video_Activity.class));
            }
        });

        defaultOptions = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true)
                .showImageForEmptyUri(R.drawable.ic_launcher_background).showImageOnLoading(R.drawable.ic_launcher_foreground)
                .showImageOnFail(R.drawable.ic_launcher_foreground).bitmapConfig(Bitmap.Config.RGB_565)
                .displayer(new SimpleBitmapDisplayer()).build();


        ImageLoaderConfiguration mImageLoaderConfig = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions).writeDebugLogs().build();
        imageLoader.init(mImageLoaderConfig);

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



        // Toast.makeText(this, "filenamepath"+path, Toast.LENGTH_SHORT).show();

        Log.d("jsonfilename",jsonFileName);
        Log.d("jsoncontent",jsonContent);

        videoDetails=new Gson().fromJson(jsonContent,VideoDetails.class);

        Log.d("videopath",videoDetails.getVideo());

   //   loadImageSD( Environment.getExternalStorageDirectory().getAbsolutePath()+videoDetails.getPic(),imageView);

         imageView.setImageURI(Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath()+videoDetails.getPic()));


     //   videoView.setVideoPath(videoDetails.getVideo());

        Uri uri= Uri.parse(
                Environment.getExternalStorageDirectory().getAbsolutePath()+videoDetails.getVideo());

        Log.d("uripath", String.valueOf(uri));

        videoView.setVideoURI(uri);

        //videoView.setVideoPath( videoDetails.getVideo());
        videoView.setMediaController(new MediaController(this));
        videoView.start();

      //  Log.d("decryptpath",path);

   //     playVideo(MainActivity.this);


    }


    public void loadImageSD(String url, ImageView imageView)
    {

        Log.d("image_url",url);

        if (url == null || imageView == null)
            return;
        String finalUrl = "file://" + path + url;

        Log.e("image_finalurl", finalUrl);

        imageLoader.displayImage(finalUrl, imageView);
    }







    private void requestPermission() {
        permissionsToRequest = findUnAskedPermissions(permissions);
        if (permissionsToRequest.size() > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
            }

        }
    }

    private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<String>();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            return (this.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
        }
        return true;



    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }


}
