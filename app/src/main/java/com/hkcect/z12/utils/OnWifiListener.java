package com.hkcect.z12.utils;

/**
 * Created by Administrator on 2017/4/19.
 */

public interface OnWifiListener {

    void onFailure();

    void onFinish();

    void onStart(String ssid);
}
