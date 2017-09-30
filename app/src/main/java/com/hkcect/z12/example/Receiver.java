package com.hkcect.z12.example;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.ntk.nvtkit.NVTKitModel;

public class Receiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo != null && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
			//NVTKitModel.resetWifiEventListener();
			//NVTKitModel.videoResumePlay();
			//Log.e("wifi", "connected");
		} else {
			NVTKitModel.removeWifiEventListener();
			//Log.e("wifi", "NOT connected");
		}
	}
}
