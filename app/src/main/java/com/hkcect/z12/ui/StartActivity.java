package com.hkcect.z12.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.audiofx.BassBoost;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.hkcect.z12.R;

import java.util.ArrayList;
import java.util.List;

import static com.hkcect.z12.ui.StartActivity.ACCESS_COARSE_LOCATION_CODE;
import static com.hkcect.z12.ui.StartActivity.ACCESS_FINE_LOCATION_CODE;
import static com.hkcect.z12.ui.StartActivity.ACCESS_NETWORK_STATE_CODE;
import static com.hkcect.z12.ui.StartActivity.ACCESS_WIFI_STATE_CODE;
import static com.hkcect.z12.ui.StartActivity.CHANGE_NETWORK_STATE_CODE;
import static com.hkcect.z12.ui.StartActivity.CHANGE_WIFI_STATE_CODE;
import static com.hkcect.z12.ui.StartActivity.INTERNET_CODE;

public class StartActivity extends Activity {

    private static final String TAG = "StartActivity";
    public static final int ACCESS_FINE_LOCATION_CODE = 111;
    public static final int ACCESS_COARSE_LOCATION_CODE = 222;
    public static final int ACCESS_WIFI_STATE_CODE = 333;
    public static final int CHANGE_WIFI_STATE_CODE = 444;
    public static final int CHANGE_NETWORK_STATE_CODE = 555;
    public static final int INTERNET_CODE = 666;
    public static final int ACCESS_NETWORK_STATE_CODE = 777;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(StartActivity.this,MainActivity.class));
                    finish();
                }
            },3500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_start);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);//去掉信息栏

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPer();
        } else {
            Message message = handler.obtainMessage();
            message.what = 1;
            handler.sendMessageDelayed(message, 0);
        }
    }


    private void requestPer() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE},
                    1);
        } else {
            Message message = handler.obtainMessage();
            message.what = 1;
            handler.sendMessageDelayed(message, 0);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            List<String> needPer = new ArrayList<>();
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    needPer.add(permissions[i]);
                }
            }


            if (needPer.size() > 0) {
                requestPerOne(needPer);
            } else {
                Message message = handler.obtainMessage();
                message.what = 1;
                handler.sendMessageDelayed(message, 0);
            }
        }
    }
    private void requestPerOne(List<String> permission) {
        String list = "";
        for (int i = 0; i < permission.size(); i++) {
            if (i != (permission.size() - 1)) {
                list = list + permission.get(i) + ",";
            }
        }
        ActivityCompat.requestPermissions(this, new String[]{list}, 1);
    }



}
