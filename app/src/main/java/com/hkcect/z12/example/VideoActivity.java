package com.hkcect.z12.example;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.videolan.libvlc.VideoInterface;

import com.hkcect.z12.R;
import com.hkcect.z12.ui.SettingActivity;
import com.hkcect.z12.util.ClientScanResult;
import com.hkcect.z12.util.DefineTable;
import com.hkcect.z12.util.ErrorCode;
import com.hkcect.z12.util.FinishScanListener;
import com.hkcect.z12.util.ProfileItem;
import com.ntk.util.SocketHBModel;
import com.ntk.util.Util;
import com.hkcect.z12.util.WifiAPUtil;
import com.ntk.nvtkit.NVTKitModel;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Time;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class VideoActivity extends Activity implements VideoInterface {
    public final static String TAG = "VideoActivity";

    private Map deviceStatusMap;
    private ArrayList<String> movie_res_indexList;
    private ArrayList<String> movie_res_infoList;

    private boolean isHeartbeat = false;

    private int mode = NVTKitModel.MODE_MOVIE; // mjpg=0 , rtps=1

    private String max_rec_time;
    private String free_capture_num;

    private boolean isRecording = false;
    private boolean hidePanel = true;
    private boolean hideEV = true;

    private ImageView image_record;
    private Button button_record;
    private Button button_pic_on_record;
    private Button button_album;
    private Button button_changeMode;
    private Button button_MovieEV;
    private SeekBar seekBar_MovieEV;
    private Button button_menu;
    private Button button_capture;
    private TextView resTextView;
    private TextView recordTimeTextView;
    private ImageView imageView_battery;
    private RelativeLayout movie_leftPanel;
    private RelativeLayout movie_rightPanel;
    private RelativeLayout movie_topPanel;
    private RelativeLayout photo_rightPanel;
    private RelativeLayout layout_blank;

    private SurfaceView mSurface;
    private SurfaceHolder holder;

    private WifiAPUtil mWifiAPUtil;

    private ProgressDialog psDialog;
    private boolean isProcessing = false;

    private ProgressDialog pausedialog;
    private boolean isLoading = false;

    Timer blinkTimer;

    private Handler videoHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {

        }
    };

    private Handler eventHandler = new Handler() {
        @SuppressLint("NewApi")
        @Override
        public void handleMessage(android.os.Message msg) {
            String info = msg.obj.toString();
            if (info == null) {
                return;
            }
            if (Util.isContainExactWord(info, "&")) {
                String[] cmd = info.split("&");
                switch (cmd[0]) {
                    case "1":
                        button_changeMode.callOnClick();
                        break;
                    case "2":
                        setEV(cmd[1]);
                        break;
                    case "3":
                        button_menu.callOnClick();
                        break;
                    case "5":
                        button_record.callOnClick();
                        break;
                    case "6":
                        button_pic_on_record.callOnClick();
                        break;
                    default:
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                final String ack3 = NVTKitModel.autoTestDone();
                            }
                        }).start();
                        break;
                }


            } else if (Util.isContainExactWord(info, "qwer")) {
                Toast.makeText(VideoActivity.this, info, Toast.LENGTH_SHORT).show();
            } else if (Util.isContainExactWord(info, "SocketHBModel")) {
                if (Util.isContainExactWord(info, "on")) {
                    setLoading(true);
                } else if (Util.isContainExactWord(info, "off")) {
                    setLoading(false);
                }
            } else {
                if (info.equals(String.valueOf(ErrorCode.WIFIAPP_RET_POWER_OFF))) {
                    Toast.makeText(VideoActivity.this, "Device Power Off!!!", Toast.LENGTH_SHORT).show();
                    psDialog.setMessage("Device Power Off!!! close APP");
                    psDialog.setCancelable(false);

                    psDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    });
                    if (!VideoActivity.this.isFinishing()) {
                        psDialog.show();
                    }
                } else if (info.equals(String.valueOf(ErrorCode.WIFIAPP_RET_RECORD_STARTED))) {
                    Toast.makeText(VideoActivity.this, "Motion Detect!!!!!", Toast.LENGTH_SHORT).show();
                } else if (info.equals(String.valueOf(ErrorCode.WIFIAPP_RET_MOVIE_SLOW))) {
                    //Toast.makeText(VideoActivity.this, "Slow Card!!!!!", Toast.LENGTH_SHORT).show();
                } else if (info.equals(String.valueOf(ErrorCode.WIFIAPP_RET_REMOVE_BY_USER))) {
                    //Toast.makeText(VideoActivity.this, "Remove by other!!!!!", Toast.LENGTH_SHORT).show();
                } else if (info.equals(String.valueOf(ErrorCode.WIFIAPP_RET_CMD_SOCKET_TIMEOUT))) {
                    Toast.makeText(VideoActivity.this, "WIFIAPP_RET_CMD_SOCKET_TIMEOUT", Toast.LENGTH_SHORT).show();
                    psDialog.setMessage("WIFIAPP_RET_CMD_SOCKET_TIMEOUT!!! Close APP");
                    psDialog.setCancelable(false);
                    psDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    });
                    if (!VideoActivity.this.isFinishing()) {
                        psDialog.show();
                    }
                } else if (info.equals(String.valueOf(ErrorCode.WIFIAPP_RET_CMD_CONNECT_TIMEOUT))) {
                    Toast.makeText(VideoActivity.this, "WIFIAPP_RET_CMD_CONNECT_TIMEOUT", Toast.LENGTH_SHORT).show();
                    psDialog.setMessage("WIFIAPP_RET_CMD_CONNECT_TIMEOUT!!! Close APP");
                    psDialog.setCancelable(false);
                    psDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    });
                    if (!(VideoActivity.this).isFinishing()) {
                        psDialog.show();
                    }
                } else if (info.equals(String.valueOf(ErrorCode.WIFIAPP_RET_SENSOR_NUM_CHANGED))) {
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Map map = NVTKitModel.get_liveView_FMT();
                            if (map != null && map.get("MovieLiveViewLink") != null) {
                                Util.movie_url = map.get("MovieLiveViewLink").toString();
                                Util.photo_url = map.get("PhotoLiveViewLink").toString();
                            }
                            //设置延迟
                            NVTKitModel.setNetwork_cache(CACHE_TIME);
                            //破图容忍度
                            NVTKitModel.setVideoBrokenMapTolerate(Util.BLOCKING_LEVEL_HIGH);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    NVTKitModel.videoPlayForLiveView(VideoActivity.this, VideoActivity.this, videoHandler, holder, mSurface);
                                }
                            });
                        }

                    });
                    t.start();
                }
            }
        }
    };
    private static final int CACHE_TIME = 1000;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        //setLoading(true);

        new NVTKitModel(this);
        //NVTKitModel.setVideoAspectRatio(Util.ASPECTRARIO_FULLSCREEN);
        mWifiAPUtil = new WifiAPUtil(this);

        //设置设备时间同步手机
        initDate();


        psDialog = new ProgressDialog(VideoActivity.this);
        layout_blank = (RelativeLayout) findViewById(R.id.layout_blank);
        initMovieLeftPanel();
        initMovieRightPanel();
        initMovieTopPanel();
        initPhotoRightPanel();
    }

    private void initDate() {
        Time t = new Time(); // or Time t=new Time("GMT+8"); 加上Time Zone资料。
        t.setToNow(); // 取得系统时间。
        final String year = String.valueOf(t.year);
        final String month = String.valueOf(t.month + 1);
        final String date = String.valueOf(t.monthDay);
        final String hour = String.valueOf(t.hour); // 0-23
        final String minute = String.valueOf(t.minute);
        final String second = String.valueOf(t.second);

        Log.e(TAG, "initDate: " + year + "　" + month + "  " + date + "  " + hour + "  " + minute + "  " + second);
        new Thread(new Runnable() {
            @Override
            public void run() {
                //日期
                String result1 = NVTKitModel.setMovieDate(year, month, date);
                Log.e(TAG, "run: result " + result1 == null ? "null" : result1);
                //时间
                String result2 = Util.setMovie_time(hour,minute,second);
                Log.e(TAG, "run: result " + result2 == null ? "null" : result2);
            }
        }).start();

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_UP:
                if (hidePanel) {
                    hidePanel = false;
                    if (mode == NVTKitModel.MODE_MOVIE) {
                        setMovieVisible(View.VISIBLE);
                    } else if (mode == NVTKitModel.MODE_PHOTO) {
                        setPhotoVisible(View.VISIBLE);
                    }
                } else {
                    hidePanel = true;
                    setMovieVisible(View.GONE);
                    setPhotoVisible(View.GONE);
                }
                hideEV = true;
                seekBar_MovieEV.setVisibility(View.GONE);
                button_MovieEV.setBackgroundResource(R.drawable.ev_off);
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void onResumeTask() {
        hidePanel = true;
        seekBar_MovieEV.setVisibility(View.GONE);
        if (mWifiAPUtil.getWifiApState().equals(WifiAPUtil.WIFI_AP_STATE.WIFI_AP_STATE_DISABLED)) {

            NVTKitModel.setWifiEventListener(eventHandler);

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    NVTKitModel.devAPPSessionOpen();
                    new ProfileItem();

                    // init video surface
                    mSurface = (SurfaceView) findViewById(R.id.surface);
                    holder = mSurface.getHolder();
                    mode = 1;

                    if (!checkDeviceStatus()) {
                        return;
                    }

                    String result = NVTKitModel.changeMode(NVTKitModel.MODE_MOVIE);
                    if (result == null) {
                        Log.e(TAG, "mode_change fail");
                        return;
                    } else {
                        //设置延迟
                        NVTKitModel.setNetwork_cache(CACHE_TIME);
                        //破图容忍度
                        NVTKitModel.setVideoBrokenMapTolerate(Util.BLOCKING_LEVEL_HIGH);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                NVTKitModel.videoPlayForLiveView(VideoActivity.this, VideoActivity.this, videoHandler, holder, mSurface);
                            }
                        });
                        blinkTimer = new Timer(true);
                        blinkTimer.schedule(new MyTimerTask(), 1000, 1000);
                    }
                    final String ack3 = NVTKitModel.autoTestDone();
                }

            });
            t.start();

        } else if (mWifiAPUtil.getWifiApState().equals(WifiAPUtil.WIFI_AP_STATE.WIFI_AP_STATE_ENABLED)) {
            SharedPreferences settings2 = getSharedPreferences("device_info", 0);
            String mac = settings2.getString("device_mac", null);
            if (mac == null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        psDialog.setMessage("Device Not Find!!! Close APP");
                        psDialog.setCancelable(false);
                        psDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        finish();
                                    }
                                });
                        if (!VideoActivity.this.isFinishing()) {
                            psDialog.show();
                        }
                    }
                });
            } else {
                mWifiAPUtil.checkDeviceConnect(mac, true, new FinishScanListener() {
                    @Override
                    public void onFinishScan(ArrayList<ClientScanResult> clients) {
                    }

                    @Override
                    public void onDeviceConnect(String device_ip) {
                        Util.setDeciceIP(device_ip);
                        if (device_ip == null) {
                            Log.e(TAG, "device_ip == null");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    psDialog.setMessage("Device Not Find!!! Close APP");
                                    psDialog.setCancelable(false);
                                    psDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "OK",
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                    finish();
                                                }
                                            });
                                    if (!VideoActivity.this.isFinishing()) {
                                        psDialog.show();
                                    }
                                }
                            });
                        } else {
                            Util.setDeciceIP(device_ip);
                            // Log.e(TAG, device_ip);
                            NVTKitModel.setWifiEventListener(eventHandler);
                            // init video surface
                            mSurface = (SurfaceView) findViewById(R.id.surface);
                            holder = mSurface.getHolder();
                            mode = 1;
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    new ProfileItem();
                                    if (!checkDeviceStatus()) {
                                        return;
                                    }
                                    String result = NVTKitModel.changeMode(NVTKitModel.MODE_MOVIE);
                                    if (result == null) {
                                        Log.e(TAG, "mode_change fail");
                                        return;
                                    } else {
                                        //设置延迟
                                        NVTKitModel.setNetwork_cache(CACHE_TIME);
                                        //破图容忍度
                                        NVTKitModel.setVideoBrokenMapTolerate(Util.BLOCKING_LEVEL_HIGH);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                NVTKitModel.videoPlayForLiveView(VideoActivity.this, VideoActivity.this, videoHandler, holder, mSurface);
                                            }
                                        });
                                    }
                                    final String ack3 = NVTKitModel.autoTestDone();
                                }
                            }).start();

                            blinkTimer = new Timer(true);
                            blinkTimer.schedule(new MyTimerTask(), 1000, 1000);
                        }
                    }
                });
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (NVTKitModel.isHeartbeat) {
            setLoading(false);
        }
        onResumeTask();
    }

    @Override
    protected void onPause() {
        super.onPause();
        NVTKitModel.stopWifiEventListener();
        NVTKitModel.removeWifiEventListener();
        NVTKitModel.videoStop();
        setMovieVisible(View.GONE);
        setPhotoVisible(View.GONE);
        //blinkTimer.cancel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//		eventHandler.getLooper().quit();
        NVTKitModel.releaseNVTKitModel();
//        NVTKitModel.removeWifiEventListener();

//        eventHandler.removeCallbacksAndMessages(null);
//        videoHandler.removeCallbacksAndMessages(null);
    }

    // SetSizeListener callback
    @Override
    public void setSize(int width, int height) {
        LayoutParams lp = mSurface.getLayoutParams();
        lp.width = width;
        lp.height = height;
        mSurface.setLayoutParams(lp);
        mSurface.invalidate();

//        ViewGroup.LayoutParams lp = mSurface.getLayoutParams();
//
//        lp.height = LayoutParams.MATCH_PARENT;
//
//        lp.width = LayoutParams.MATCH_PARENT;
//        System.out.println(lp.width);
//        mSurface.setLayoutParams(lp);
    }

    private void setMovieVisible(int visible) {
        movie_leftPanel.setVisibility(visible);
        if (isRecording) {
            movie_leftPanel.setVisibility(View.INVISIBLE);
        }
        movie_rightPanel.setVisibility(visible);
        movie_topPanel.setVisibility(visible);
    }

    private void setPhotoVisible(int visible) {
        movie_leftPanel.setVisibility(visible);
        photo_rightPanel.setVisibility(visible);
        movie_topPanel.setVisibility(visible);
    }

    private boolean checkDeviceStatus() {


        new SocketHBModel(eventHandler);
        SocketHBModel.startSocketHB();

        // heartbeat
        String ack_heartbeat = NVTKitModel.devHeartBeat();
        if (ack_heartbeat == null) {
            Log.e(TAG, "heartbeat no response");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setProcessing(true);
                }
            });
            return false;
        } else if (NVTKitModel.getInitState() == NVTKitModel.INIT_FAIL_BAD_COMMAND) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    psDialog.setMessage("Bad Command!!");
                    psDialog.setCancelable(false);
//					psDialog.setCanceledOnTouchOutside(false);
                    psDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    });
                    if (!VideoActivity.this.isFinishing()) {
                        psDialog.show();
                    }
                }
            });
        } else if (NVTKitModel.getInitState() == NVTKitModel.INIT_FAIL_UNKNOWN_DEVICE) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    psDialog.setMessage("Unknown Device!!");
                    psDialog.setCancelable(false);

                    psDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    });
                    if (!VideoActivity.this.isFinishing()) {
                        psDialog.show();
                    }
                }
            });
        } else {
            // save device_mac for station mode <-> AP mode
            isHeartbeat = true;
            if (mWifiAPUtil.getWifiApState().equals(WifiAPUtil.WIFI_AP_STATE.WIFI_AP_STATE_DISABLED)) {
                SharedPreferences settings = getSharedPreferences("device_info", 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("device_mac", mWifiAPUtil.getDeviceMac());
                editor.apply();
            }
        }

        if (isHeartbeat) {
            //get stream url
            Map map = NVTKitModel.get_liveView_FMT();
            if (map != null && map.get("MovieLiveViewLink") != null) {
                Util.movie_url = map.get("MovieLiveViewLink").toString();
                Util.photo_url = map.get("PhotoLiveViewLink").toString();
            }

            // get battery status
            final String ack_battery = NVTKitModel.qryBatteryStatus();
            if (ack_battery == null) {
                Log.e(TAG, "battery no response");
                return false;
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        switch (ack_battery) {
                            case DefineTable.NVTKitBatterStatus_FULL:
                                imageView_battery.setBackgroundResource(R.drawable.battery_full);
                                break;
                            case DefineTable.NVTKitBatterStatus_MED:
                                imageView_battery.setBackgroundResource(R.drawable.battery_75);
                                break;
                            case DefineTable.NVTKitBatterStatus_LOW:
                                imageView_battery.setBackgroundResource(R.drawable.battery_half);
                                break;
                            case DefineTable.NVTKitBatterStatus_EMPTY:
                                imageView_battery.setBackgroundResource(R.drawable.battery_zero);
                                break;
                            case DefineTable.NVTKitBatterStatus_Exhausted:
                                imageView_battery.setBackgroundResource(R.drawable.battery_25);
                                break;
                            case DefineTable.NVTKitBatterStatus_CHARGE:
                                imageView_battery.setBackgroundResource(R.drawable.battery_charging);
                                break;
                        }
                    }
                });
            }

            // get resolution list
            final com.ntk.util.ParseResult result = NVTKitModel.qryDeviceRecSizeList();
            if (result == null) {
                return false;
            } else {
                movie_res_indexList = result.getRecIndexList();
                movie_res_infoList = result.getRecInfoList();
                if (movie_res_indexList.isEmpty()) {
                    Log.e(TAG, "query_movie_size fail");
                    return false;
                }
            }

            // get device status
            deviceStatusMap = NVTKitModel.qryDeviceStatus();
            if (deviceStatusMap == null) {
                return false;
            } else {
                Iterator iter = deviceStatusMap.entrySet().iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    String key = (String) entry.getKey();
                    final String val = (String) entry.getValue();
                    Log.e("key val " + key, val);

                    if ((key.equals(DefineTable.WIFIAPP_CMD_MOVIE_REC_SIZE)) && (mode == NVTKitModel.MODE_MOVIE)) {
                        int i = 0;
                        while (i < movie_res_indexList.size()) {
                            if (val.equals(movie_res_indexList.get(i))) {
                                final int index = i;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        resTextView.setText(movie_res_infoList.get(index));
                                    }
                                });
                                break;
                            }
                            i = i + 1;
                        }
                    }
                    if (key.equals(DefineTable.WIFIAPP_CMD_MOVIE_EV)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                seekBar_MovieEV.setProgress(Integer.valueOf(val));
                            }
                        });
                    }
                    if ((key.equals(DefineTable.WIFIAPP_CMD_CAPTURESIZE)) && (mode == NVTKitModel.MODE_PHOTO)) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                resTextView.setText(ProfileItem.list_capturesize.get(Integer.valueOf(val)));
                            }
                        });
                    }
                }
            }

            // movie max record time , free capture number
            max_rec_time = NVTKitModel.qryMaxRecSec();
            if (max_rec_time == null) {
                return false;
            }
            free_capture_num = NVTKitModel.qryMaxPhotoNum();
            if (free_capture_num == null) {
                return false;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    image_record = (ImageView) findViewById(R.id.image_onRecord);
                    // check is recording
                    isRecording = deviceStatusMap.get(DefineTable.WIFIAPP_CMD_MOVIE_RECORDING_TIME).equals("1");
                    setRecordUI();

                    if (mode == 0) {
                        recordTimeTextView.setText(free_capture_num);
                    } else if (mode == 1) {
                        int sec = Integer.valueOf(max_rec_time);
                        recordTimeTextView.setText(String.format("%02d", sec / 3600) + ":"
                                + String.format("%02d", sec / 60 % 60) + ":" + String.format("%02d", sec % 60));
                    }
                }
            });
        }
        return true;
    }

    private void initMovieLeftPanel() {
        movie_leftPanel = (RelativeLayout) findViewById(R.id.layout_left);
        movie_leftPanel.setVisibility(View.GONE);

        button_changeMode = (Button) findViewById(R.id.button_mode_switch);
        button_changeMode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (mode == NVTKitModel.MODE_MOVIE) {
                    changeMode(NVTKitModel.MODE_PHOTO);
                } else {
                    changeMode(NVTKitModel.MODE_MOVIE);
                }
            }
        });

        button_MovieEV = (Button) findViewById(R.id.button_MovieEV);
        button_MovieEV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {

                if (!isRecording) {
                    if (hideEV) {
                        hideEV = false;
                        seekBar_MovieEV.setVisibility(View.VISIBLE);
                        button_MovieEV.setBackgroundResource(R.drawable.ev_adjust);
                    } else {
                        hideEV = true;
                        seekBar_MovieEV.setVisibility(View.GONE);
                        button_MovieEV.setBackgroundResource(R.drawable.ev_off);
                    }
                } else {
                    Toast.makeText(VideoActivity.this, "EV!!!!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        seekBar_MovieEV = (SeekBar) findViewById(R.id.seekBar_MovieEV);
        seekBar_MovieEV.setVisibility(View.GONE);
        button_MovieEV.setBackgroundResource(R.drawable.ev_off);
        seekBar_MovieEV.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (!isRecording) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final String ack = NVTKitModel.setMovieEV(String.valueOf(seekBar_MovieEV.getProgress()));
                            if (ack == null) {
                                return;
                            }
                        }
                    }).start();
                } else {
                    Toast.makeText(VideoActivity.this, "Recoding!!", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }
        });

        button_menu = (Button) findViewById(R.id.button_menu);
        button_menu.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (!isRecording) {
                    Intent intent = new Intent();
                    intent.setClass(VideoActivity.this, SettingActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(VideoActivity.this, "Recoding!!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initMovieRightPanel() {
        movie_rightPanel = (RelativeLayout) findViewById(R.id.layout_right);
        movie_rightPanel.setVisibility(View.GONE);

        button_album = (Button) findViewById(R.id.button_album);
        button_album.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {

                if (!isRecording) {
                    Intent intent = new Intent(VideoActivity.this, AlbumActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(VideoActivity.this, "Recoding!!", Toast.LENGTH_SHORT).show();
                }


            }
        });

        button_record = (Button) findViewById(R.id.button_record);
        button_record.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {

                seekBar_MovieEV.setVisibility(View.GONE);
                setLoading(true);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isRecording) {
                            final String result = NVTKitModel.recordStart();
                            if (result == null) {
                                setLoading(false);
                                return;
                            }
                            isRecording = true;
                            setRecordUI();
                        } else {
                            final String result = NVTKitModel.recordStop();
                            if (result == null) {
                                setLoading(false);
                                return;
                            }
                            isRecording = false;
                            if (!checkDeviceStatus()) {
                                setLoading(false);
                                return;
                            }
                            setRecordUI();
                        }
                        setLoading(false);

                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //设置延迟
                        NVTKitModel.setNetwork_cache(CACHE_TIME);
                        //破图容忍度
                        NVTKitModel.setVideoBrokenMapTolerate(Util.BLOCKING_LEVEL_HIGH);
                        NVTKitModel.videoPlayForLiveView(VideoActivity.this, VideoActivity.this, videoHandler, holder, mSurface);
                        final String ack3 = NVTKitModel.autoTestDone();
                    }
                }).start();
            }
        });

        button_pic_on_record = (Button) findViewById(R.id.button_pic_on_record);
        button_pic_on_record.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {

                if (isRecording) {
                    setLoading(true);
                    layout_blank.setVisibility(View.GONE);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final String result = NVTKitModel.takePictureOnRecord();
                            if (result != null) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //layout_blank.setVisibility(View.VISIBLE);
                                    }
                                });
                            } else {
                                setLoading(false);
                                return;
                            }
                            setLoading(false);
                            final String ack3 = NVTKitModel.autoTestDone();
                        }
                    }).start();
                } else {
                    Toast.makeText(VideoActivity.this, "LK!!!!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initMovieTopPanel() {
        movie_topPanel = (RelativeLayout) findViewById(R.id.layout_top);
        movie_topPanel.setVisibility(View.GONE);
        resTextView = (TextView) findViewById(R.id.textView_top_resolution);
        recordTimeTextView = (TextView) findViewById(R.id.textView_top_max_record_time);
        imageView_battery = (ImageView) findViewById(R.id.imageView_battery);
    }

    private void initPhotoRightPanel() {
        photo_rightPanel = (RelativeLayout) findViewById(R.id.photo_layout_right);
        photo_rightPanel.setVisibility(View.GONE);

        Button button_photo_album = (Button) findViewById(R.id.button_photo_album);
        button_photo_album.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(VideoActivity.this, AlbumActivity.class);
                startActivity(intent);
            }
        });

        button_capture = (Button) findViewById(R.id.button_capture);
        button_capture.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                setLoading(true);
                layout_blank.setVisibility(View.GONE);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final Map result = NVTKitModel.takePhoto();
                        if (result == null) {
                            setLoading(false);
                            return;
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //layout_blank.setVisibility(View.VISIBLE);
                                free_capture_num = (String) result.get("FREEPICNUM");
                                recordTimeTextView.setText(free_capture_num);
                            }
                        });
                        setLoading(false);
                    }
                }).start();
            }
        });
    }

    private void changeMode(final int mode) {
        this.mode = mode;
        setLoading(true);
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String ack = NVTKitModel.changeMode(mode);
                if (ack != null) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //设置延迟
                    NVTKitModel.setNetwork_cache(CACHE_TIME);

//                    Util.BLOCKING_LEVEL_NONE 無
//                    Util.BLOCKING_LEVEL_LOW 低
//                    Util.BLOCKING_LEVEL_MID  中
//                    Util.BLOCKING_LEVEL_HIGH 高
                    //破图容忍度
                    NVTKitModel.setVideoBrokenMapTolerate(Util.BLOCKING_LEVEL_HIGH);
                    if (mode == NVTKitModel.MODE_PHOTO) {
                        NVTKitModel.videoPlayForPhotoCapture(VideoActivity.this, VideoActivity.this, videoHandler, holder, mSurface);
                    } else if (mode == NVTKitModel.MODE_MOVIE) {
                        NVTKitModel.videoPlayForLiveView(VideoActivity.this, VideoActivity.this, videoHandler, holder, mSurface);
                    }
                } else {
                    Toast.makeText(VideoActivity.this, "changeMode fail!!!", Toast.LENGTH_SHORT).show();
                    return;
                }
                setLoading(false);
                if (!checkDeviceStatus()) {
                    return;
                }

                final String ack3 = NVTKitModel.autoTestDone();
            }
        }).start();

        if (mode == NVTKitModel.MODE_MOVIE) {
            button_changeMode.setBackgroundResource(R.drawable.mode_changeto_still);
        } else {
            button_changeMode.setBackgroundResource(R.drawable.mode_changeto_video);
        }
        setMovieVisible(View.GONE);
        setPhotoVisible(View.GONE);
        seekBar_MovieEV.setVisibility(View.GONE);
    }

    private void setRecordUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!isRecording) {
                    image_record.setVisibility(View.INVISIBLE);
                    button_record.setBackgroundResource(R.drawable.shutter_rec_start);
                    button_pic_on_record.setBackgroundResource(R.drawable.shutter_capture_instill_lock);
                    button_album.setBackgroundResource(R.drawable.pbk);
                    setMovieVisible(View.INVISIBLE);
                    hidePanel = true;
                } else {
                    image_record.setVisibility(View.VISIBLE);
                    button_record.setBackgroundResource(R.drawable.shutter_rec_stop);
                    button_pic_on_record.setBackgroundResource(R.drawable.shutter_capture_instill);
                    button_album.setBackgroundResource(R.drawable.pbk_lock);
                    setMovieVisible(View.INVISIBLE);
                    recordTimeTextView.setText("(Recording...)");
                    hidePanel = true;
                }
            }
        });
    }

    public class MyTimerTask extends TimerTask {
        boolean isOn = true;

        public void run() {
            if (isRecording) {
                if (isOn) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            image_record.setVisibility(View.GONE);
                        }
                    });
                    isOn = false;
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            image_record.setVisibility(View.VISIBLE);
                        }
                    });
                    isOn = true;
                }
            }
        }
    }
/*
    private void setLoading(final boolean isLoading) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (isLoading == true) {
					findViewById(R.id.loading).setVisibility(View.VISIBLE);
					setClickable(false);
				} else {
					findViewById(R.id.loading).setVisibility(View.INVISIBLE);
					setClickable(true);
				}
			}

		});
	}
	*/

    private void setLoading(final boolean isOpen) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isOpen) {
                    if (!isLoading) {
                        pausedialog = new ProgressDialog(VideoActivity.this);
                        pausedialog.setTitle(getString(R.string.dialog_title));
                        pausedialog.setMessage(getString(R.string.dialog_wait_message));
                        if (!VideoActivity.this.isFinishing()) {
                            pausedialog.show();
                        }
                        isLoading = true;
                    }
                } else {
                    isLoading = false;
                    if (pausedialog != null)
                        pausedialog.dismiss();
                }
            }
        });
    }

    private void setClickable(final boolean isClickable) {
        button_record.setClickable(isClickable);
        button_pic_on_record.setClickable(isClickable);
        button_album.setClickable(isClickable);
        button_changeMode.setClickable(isClickable);
        button_MovieEV.setClickable(isClickable);
        seekBar_MovieEV.setClickable(isClickable);
        button_menu.setClickable(isClickable);
        button_capture.setClickable(isClickable);
    }

    private void setProcessing(final boolean is) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (is) {
                    if (!isProcessing) {
                        isProcessing = true;

                        psDialog.setMessage("Connection Fail!!!");
                        psDialog.setCancelable(false);
                        psDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Local File",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        setProcessing(false);
                                        Intent intent = new Intent(VideoActivity.this, LocalFileActivity.class);
                                        startActivity(intent);
                                    }
                                });
                        psDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Try again",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // psDialog.dismiss();
                                        setProcessing(false);
                                        onResumeTask();
                                    }
                                });

                        if (!VideoActivity.this.isFinishing()) {
                            psDialog.show();
                        }
                    }
                } else {
                    isProcessing = false;
                    psDialog.dismiss();
                }
            }
        });
    }

    private void setEV(final String which) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String ack = NVTKitModel.setMovieEV(which);
                if (ack == null) {
                    return;
                }
                final String ack3 = NVTKitModel.autoTestDone();
            }
        }).start();
    }
}
