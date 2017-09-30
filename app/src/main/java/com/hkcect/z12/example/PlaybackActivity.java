package com.hkcect.z12.example;


import org.videolan.libvlc.VideoInterface;

import com.hkcect.z12.R;
import com.hkcect.z12.util.VideoEvent;
import com.ntk.nvtkit.NVTKitModel;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class PlaybackActivity extends Activity implements SurfaceHolder.Callback, VideoInterface {
    public final static String TAG = "PlaybackActivity";

    private static String videoPath;
    private long videoLength;

    private SurfaceView mSurface;
    private static Context mContext;
    private static VideoInterface mVideoInterface;
    private static SurfaceHolder holder;

    private static SeekBar seekBar_videotime;
    private static TextView textView_time;
    private static TextView textView_length;
    private static ImageButton button_play;
    private LinearLayout ll_seekbar;

    private static Handler videoHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            String info = msg.obj.toString();
            if (info.equals(String.valueOf(VideoEvent.MediaPlayerPositionChanged))) {
                float progress = 100 * NVTKitModel.videoQryCurtime() / NVTKitModel.videoQryLenth();
                seekBar_videotime.setProgress((int) progress);
                int sec = (int) (NVTKitModel.videoQryLenth() / 1000);
                textView_length.setText(String.format("%02d", sec / 60) + ":" + String.format("%02d", sec % 60));

                int sec2 = (int) (NVTKitModel.videoQryCurtime() / 1000);
                textView_time.setText(String.format("%02d", sec2 / 60) + ":" + String.format("%02d", sec2 % 60));

            } else if (info.equals(String.valueOf(VideoEvent.MediaPlayerEndReached))) {
                seekBar_videotime.setProgress(0);
                textView_time.setText("00:00");
                button_play.setImageResource(android.R.drawable.ic_media_play);
            }
        }
    };

    private ProgressDialog psDialog;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            String info = msg.obj.toString();
            if (info.equals("6")) {
                psDialog = new ProgressDialog(PlaybackActivity.this);
                psDialog.setMessage("APP");
                psDialog.setCancelable(false);
                psDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "APP", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
                psDialog.show();
            } else if (info.equals("1")) {
                Toast.makeText(PlaybackActivity.this, "Motion Detect!!!!!", Toast.LENGTH_SHORT).show();
            } else if (info.equals("-9")) {
                Toast.makeText(PlaybackActivity.this, "Slow Card!!!!!", Toast.LENGTH_SHORT).show();
            }
        }
    };



    private Handler hideHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    ll_seekbar.setVisibility(View.GONE);
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playback);

        mContext = PlaybackActivity.this;
        mVideoInterface = PlaybackActivity.this;

        //NVTKitModel.setWifiEventListener(handler);
        ll_seekbar = (LinearLayout) findViewById(R.id.ll_seekbar);
        mSurface = (SurfaceView) findViewById(R.id.surface);
        mSurface.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ll_seekbar.getVisibility() == View.VISIBLE){
                    ll_seekbar.setVisibility(View.GONE);
                    hideHandler.removeMessages(1);
                }else if (ll_seekbar.getVisibility() == View.GONE){
                    ll_seekbar.setVisibility(View.VISIBLE);
                    hideHandler.sendEmptyMessageDelayed(1,3000);
                }
            }
        });

        videoPath = getIntent().getStringExtra("url");
        button_play = (ImageButton) findViewById(R.id.button_play);
        button_play.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {

                if (NVTKitModel.isVideoEngineNull()) {
                    NVTKitModel.videoPlayForFile(videoPath, mContext, mVideoInterface, videoHandler, holder, mSurface);
                    button_play.setImageResource(android.R.drawable.ic_media_pause);
                } else {
                    if (NVTKitModel.videoQryisPlaying()) {
                        NVTKitModel.videoPause();
                        button_play.setImageResource(android.R.drawable.ic_media_play);
                    } else {
//                        NVTKitModel.videoPlayForFile(videoPath, mContext, mVideoInterface, videoHandler, holder, mSurface);
                        NVTKitModel.videoResumePlay();
                        button_play.setImageResource(android.R.drawable.ic_media_pause);
                    }
                }
                hideHandler.removeMessages(1);
                hideHandler.sendEmptyMessageDelayed(1,3000);

            }
        });

        seekBar_videotime = (SeekBar) findViewById(R.id.seekBar_videotime);
        seekBar_videotime.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                float progress = seekBar.getProgress();
                NVTKitModel.videoSetPosition((long) ((progress / 100) * NVTKitModel.videoQryLenth()));
                hideHandler.removeMessages(1);
                hideHandler.sendEmptyMessageDelayed(1,3000);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }
        });

        textView_time = (TextView) findViewById(R.id.textView_time);
        textView_length = (TextView) findViewById(R.id.textView_length);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onResume() {
        super.onResume();
        holder = mSurface.getHolder();
        holder.addCallback(this);
        Log.e("url", videoPath);
        NVTKitModel.videoPlayForFile(videoPath, mContext, mVideoInterface, videoHandler, holder, mSurface);
        videoLength = (long) NVTKitModel.videoQryLenth();
        if (NVTKitModel.videoQryisPlaying()) {
            button_play.setImageResource(android.R.drawable.ic_media_play);
        } else {
            button_play.setImageResource(android.R.drawable.ic_media_pause);
        }
        hideHandler.sendEmptyMessageDelayed(1,3000);

    }

    @Override
    protected void onPause() {
        super.onPause();
        NVTKitModel.videoStop();
    }


    /*
    自己添加的。
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        NVTKitModel.videoStop();
        videoHandler.removeMessages(VideoEvent.MediaPlayerPositionChanged);
        videoHandler.removeMessages(VideoEvent.MediaPlayerEndReached);
        holder = null;

    }

    // Surface callback
    public void surfaceCreated(SurfaceHolder holder) {
    }

    public void surfaceChanged(SurfaceHolder surfaceholder, int format, int width, int height) {

    }

    public void surfaceDestroyed(SurfaceHolder surfaceholder) {
    }

    // SetSizeListener callback
    @Override
    public void setSize(int width, int height) {
//        LayoutParams lp = mSurface.getLayoutParams();
//        lp.width = width;
//        lp.height = height;
//        mSurface.setLayoutParams(lp);
//        mSurface.invalidate();


        //设置全屏播放
        ViewGroup.LayoutParams lp = mSurface.getLayoutParams();

        lp.height = LayoutParams.MATCH_PARENT;

        lp.width = LayoutParams.MATCH_PARENT;
        System.out.println(lp.width);
        mSurface.setLayoutParams(lp);
    }
}
