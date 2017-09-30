package com.ntk.util;


import com.hkcect.z12.util.WifiAPUtil;
import com.hkcect.z12.util.onHBListener;
import com.ntk.nvtkit.NVTKitModel;
import com.ntk.nvtkit.ae;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.videolan.libvlc.VlcEngine;

public class SocketHBModel implements onHBListener {

    private static String TAG = "SocketHBModel";
    private static Handler eventHandler;
    private static int count_HB = 0;
    private static boolean isWorking = false;

    public SocketHBModel(final Handler eventHandler) {
        NVTKitModel.setHBCallback(SocketHBModel.this);
        this.eventHandler = eventHandler;
    }

    public static void startSocketHB() {

        if (!isWorking) {
            isWorking = true;
            new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        NVTKitModel.closeNotifySocket();
                        Thread.sleep(100);
                        NVTKitModel.initNotifySocket();
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    while (isWorking) {
                        while (count_HB < 5) {
                            boolean isHeartbeat = NVTKitModel.isHeartbeat;
                            String result = null;
                            if (isHeartbeat) {
                                result = devHeartBeat();
//                                result = NVTKitModel.devHeartBeat();
                            } else {
                                return;
                            }
                            if (result != null) {
                                NVTKitModel.sendSockectHB();
                            }
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        Message msg = eventHandler.obtainMessage(1, "SocketHBModel on");
                        eventHandler.sendMessage(msg);

                        String ack = NVTKitModel.devHeartBeat();
                        int count_devHeartBeat = 0;
                        while (ack == null) {

                            try {
                                Thread.sleep(1000);
                                Log.e(TAG, "devHeartBeat no response");
                                ack = NVTKitModel.devHeartBeat();

                                count_devHeartBeat = count_devHeartBeat + 1;
                                if (count_devHeartBeat >= 5) {
                                    WifiAPUtil.setWifiEnabled(false);
                                    Log.e(TAG, "setWifiApEnabled false");
                                    Thread.sleep(3000);
                                    WifiAPUtil.setWifiEnabled(true);
                                    Log.e(TAG, "setWifiApEnabled true");
                                    count_devHeartBeat = 0;
                                }


                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }

                        //Log.e(TAG, ack);
                        if (ack.equals("-22")) {
                            NVTKitModel.devAPPSessionOpen();
                        }

                        count_HB = 0;
                        NVTKitModel.closeNotifySocket();
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        NVTKitModel.initNotifySocket();

                        if (NVTKitModel.videoQryLenth() >=0L){
                            if (!NVTKitModel.isVideoEngineNull()) {
                                NVTKitModel.videoStopPlay();
                                NVTKitModel.videoResumePlay();
                            }
                        }

                        Message msg2 = eventHandler.obtainMessage(1, "SocketHBModel off");
                        eventHandler.sendMessage(msg2);
                    }
                    isWorking = false;
                }
            }).start();
        }
    }

    public void SocketHBStart() {

    }

    public void SocketHBStop() {

    }

    @Override
    public void onHBReturn() {
        Log.e("onHBReturn", "onHBReturn");
        count_HB = count_HB + 1;
    }


    private static String devHeartBeat() {
        String var0 = ae.b("http://" + Util.getDeciceIP() + "/?custom=1&cmd=3016");
        ParseResult var1 = ae.c(var0);
        return var1 == null ? null : (var1.getStatus() != null ? (var1.getStatus().equals("0") ? "success" : var1.getStatus()) : null);
    }
}