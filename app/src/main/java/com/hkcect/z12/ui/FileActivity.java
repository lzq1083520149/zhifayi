package com.hkcect.z12.ui;

import android.animation.Animator;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hkcect.z12.R;
import com.hkcect.z12.adapter.FileListAdapter;
import com.hkcect.z12.example.PlaybackActivity;
import com.hkcect.z12.util.ProgressDialogUtlis;
import com.hkcect.z12.utils.StringUtils;

import java.io.File;
import java.util.ArrayList;

import static android.provider.MediaStore.Video.Thumbnails.MINI_KIND;

public class FileActivity extends BaseActivity {

    private ArrayList<Bitmap> videoBitmap;
    private ArrayList<String> photoList;
    private ArrayList<String> videoList;
    private String type;
    private RecyclerView recycler_files;
    //private ProgressBar loading_video;
    private FileListAdapter adapter;
    private Bitmap bitmap;
    private Thread thread;
    private Handler handler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file);
          handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case 0:
                        recycler_files.setLayoutManager(new LinearLayoutManager(FileActivity.this));
                        adapter = new FileListAdapter(FileActivity.this, type, photoList, videoList, videoBitmap);
                        recycler_files.setAdapter(adapter);
                        break;
                }

            }
        };
        intentData();
        initView();
    }

    private void intentData() {
        Bundle bundle = getIntent().getBundleExtra("data");
        photoList = bundle.getStringArrayList("photo");
        videoList = bundle.getStringArrayList("video");
        type = bundle.getString("type");
    }

    private void initView() {
        recycler_files = (RecyclerView) findViewById(R.id.recycler_files);
       // loading_video = (ProgressBar) findViewById(R.id.loading_video);
        Toolbar toolbar = (Toolbar) findViewById(R.id.id_toolbar);
        toolbar.setTitleTextColor(Color.WHITE);
        if (type.equals("video")) {
            toolbar.setTitle(getString(R.string.all_video));
            if (videoList.size() > 0) {
                videoBitmap = new ArrayList<>();
                thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        loadingVideo(true);
                        for (int i = 0; i < videoList.size(); i++) {
                            bitmap = ThumbnailUtils.createVideoThumbnail(videoList.get(i), MINI_KIND);
                            //extractThumbnail 方法二次处理,以指定的大小提取居中的图片,获取最终我们想要的图片
                            bitmap = ThumbnailUtils.extractThumbnail(bitmap, 300, 300, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                            videoBitmap.add(bitmap);
                        }

                        handler.sendEmptyMessage(0);
                        loadingVideo(false);
                    }
                });
                thread.start();
            }
        } else if (type.equals("photo")) {
            toolbar.setTitle(getString(R.string.all_photo));
            loadingVideo(true);
            recycler_files.setLayoutManager(new LinearLayoutManager(FileActivity.this));
            adapter = new FileListAdapter(FileActivity.this, type, photoList, videoList, videoBitmap);
            recycler_files.setAdapter(adapter);
            loadingVideo(false);
        }
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (thread != null) {
            thread = null;
        }
    }
    private void loadingVideo(final boolean isLoading){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isLoading){
                    ProgressDialogUtlis.showProgressDialog(FileActivity.this);
                    //loading_video.setVisibility(View.VISIBLE);
                    recycler_files.setVisibility(View.GONE);
                }else {
                    ProgressDialogUtlis.dismissProgressDialog();
                    //loading_video.setVisibility(View.GONE);
                    recycler_files.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
