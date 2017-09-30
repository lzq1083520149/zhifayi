package com.hkcect.z12.util;

import android.os.AsyncTask;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import com.hkcect.z12.utils.StringUtils;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadFileFromURL extends AsyncTask<String, String, String> {


    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }


    @Override
    protected String doInBackground(String... params) {
        String file_name;

        int count;
        try {
            file_name = params[1];
            URL url = new URL(params[0]);
            URLConnection conection = url.openConnection();
            conection.connect();
            // this will be useful so that you can show a tipical 0-100% progress bar
            int lenghtOfFile = conection.getContentLength();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream(), 8192);

            // Output stream
            OutputStream output;
            output = new FileOutputStream(StringUtils.local_media_path + "/" + file_name);

            byte data[] = new byte[1024];
            long total = 0;
            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                // After this onProgressUpdate will be called
//                publishProgress("" + (int) ((total * 100) / lenghtOfFile));
                // writing data to file
                output.write(data, 0, count);
            }

            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();

        } catch (Exception e) {
            Log.e("Error: ", e.getMessage());
        }

        return null;
    }

    protected void onProgressUpdate(String... progress) {
//            pDialog.setProgress(Integer.parseInt(progress[0]));


    }


    @Override
    protected void onPostExecute(String file_url) {
        // dismiss the dialog after the file was downloaded

    }

}