package com.hkcect.z12.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.hkcect.z12.R;
import com.hkcect.z12.ui.DownloadActivity;
import com.hkcect.z12.util.FileDownload;
import com.hkcect.z12.utils.StringUtils;
import com.ntk.nvtkit.NVTKitModel;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class DownloadService extends Service {
    private ArrayList<FileDownload> urls;
    private NotificationCompat.Builder builder;
    private Notification notification;
    private NotificationManager mNotifyManager;

    @Override
    public void onCreate() {
        super.onCreate();
        builder = new NotificationCompat.Builder(getApplicationContext());
        notification = builder.setContentTitle(getResources().getString(R.string.download_photo))
                .setContentText(getResources().getString(R.string.download_in_progress))
                .setSmallIcon(R.drawable.ic_luanch).build();
        mNotifyManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        assert intent != null;
        urls = intent.getParcelableArrayListExtra("download");
        if (urls != null) {
            for (int i = 0; i < urls.size(); i++) {
                new DownloadFile().execute(String.valueOf(i),urls.get(i).getUrl(),urls.get(i).getName());
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }


    public class DownloadFile extends AsyncTask<String, String, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... params) {

            //position,fileUrl,name

            String position = params[0];
            String fileUrl = params[1];
            String name = params[2];

                File file = new File(StringUtils.local_media_path + "/" + params[2]);
                if (file.exists()) {
                    file.delete();
                }
                try {
                    publishProgress(position, name, "","","");
                    URL url = new URL(fileUrl);
                    URLConnection connection = url.openConnection();
                    connection.connect();
//                    int fileLength = connection.getContentLength();
                    InputStream input = new BufferedInputStream(connection.getInputStream());
                    OutputStream output = new FileOutputStream(StringUtils.local_media_path + name);
                    byte data[] = new byte[1024];
//                    long total = 0;
                    int count;
                    while ((count = input.read(data)) != -1) {
//                        total += count;
//                        publishProgress(position,name,"","",(int)(total/fileLength) + "");
                        output.write(data, 0, count);
                    }
                    output.flush();
                    output.close();
                    input.close();
                } catch (IOException e) {
                    publishProgress(position,name,"",fileUrl,"");
                    e.printStackTrace();
                }

                publishProgress(position,name,fileUrl,"","");
            return null;
        }

        private int current = -1;

        /*
            第一个参数为：当前下载项目position
            第二个参数为：当前下载文件的名字
            第三个参数为：下载进度
         */
        protected void onProgressUpdate(String... progress) {

            if (!progress[0].equals("")) {
                current = Integer.valueOf(progress[0]);
            }
            if (!progress[1].equals("")) {
                builder.setContentText(String.format(getString(R.string.downloading_file),progress[1]));
                builder.setContentTitle(progress[1]);
                builder.setProgress(100, 0, true);
            }
            if (!progress[2].equals("")) {
                builder.setContentText(getString(R.string.download_complete));
                builder.setProgress(100, 100, false);
            }
            if (!progress[3].equals("")){
                File file = new File(StringUtils.local_media_path + progress[1]);
                if (file.exists()&& file.isFile()){
                    file.delete();
                }
                builder.setContentText(getString(R.string.download_failed));
                builder.setProgress(100, 0, false);
            }
//            if (!progress[4].equals("")){
//                builder.setProgress(100, Integer.valueOf(progress[4]), false);
//            }
            mNotifyManager.notify(current, builder.build());

            //TODO 通知Download去除下载完成的项目

        }

        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded
            DownloadService.this.stopSelf();
        }


    }
}
