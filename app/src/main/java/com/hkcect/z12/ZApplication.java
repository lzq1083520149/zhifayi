package com.hkcect.z12;

import android.app.Application;
import android.content.Context;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatDelegate;
import android.text.TextUtils;

import com.aitangba.swipeback.ActivityLifecycleHelper;
import com.bumptech.glide.GlideBuilder;
import com.hkcect.z12.utils.StringUtils;
import com.tencent.bugly.crashreport.CrashReport;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

//import zlc.season.rxdownload2.RxDownload;


public class ZApplication extends Application {
    public static int i = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        GlideBuilder builder = new GlideBuilder(this);
//        builder.setDiskCache(new ExternalCacheDiskCacheFactory(this, Util.local_thumbnail_path, 8*1024*1024));
//        builder.setMemoryCache(new LruResourceCache(6*1024*1024));


        Context context = getApplicationContext();
        // 获取当前包名
        String packageName = context.getPackageName();
        // 获取当前进程名
        String processName = getProcessName(android.os.Process.myPid());
        // 设置是否为上报进程
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
        strategy.setUploadProcess(processName == null || processName.equals(packageName));

        CrashReport.initCrashReport(getApplicationContext(), "e6fe574823", true);
        registerActivityLifecycleCallbacks(ActivityLifecycleHelper.build());
//        RxDownload rxDownload = RxDownload.getInstance(this)
//                .defaultSavePath(StringUtils.local_media_path) //设置默认的下载路径
//                .maxThread(3)                     //设置最大线程
//                .maxRetryCount(3)                 //设置下载失败重试次数
//                .maxDownloadNumber(5);


        if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(StringUtils.night_mode, false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }


    /**
     * 获取进程号对应的进程名
     *
     * @param pid 进程号
     * @return 进程名
     */
    private static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }

    public static int getI() {
        return i + 1;
    }
}
