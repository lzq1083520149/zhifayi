package com.hkcect.z12.fragment;


import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hkcect.z12.R;
import com.hkcect.z12.bean.MeadiaInformation;
import com.hkcect.z12.ui.FileActivity;
import com.hkcect.z12.utils.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.provider.MediaStore.Video.Thumbnails.MINI_KIND;

public class LocalMediaFragment extends Fragment implements View.OnClickListener {


    private static final String TAG = "LocalMediaFragment";
    private ImageView iv_photo, iv_video;
    private TextView photo_number, video_number;
    private ArrayList<String> photoList;
    private ArrayList<String> videoList;
    private File file;
    private Bitmap bitmap;

    public LocalMediaFragment() {
    }

    private static LocalMediaFragment inStance = null;

    public static synchronized LocalMediaFragment newInstance() {
        if (inStance == null) {
            inStance = new LocalMediaFragment();
        }
        return inStance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_local_media, container, false);
        iv_photo = (ImageView) view.findViewById(R.id.iv_photo);
        iv_video = (ImageView) view.findViewById(R.id.iv_video);
        view.findViewById(R.id.relative_photo).setOnClickListener(this);
        view.findViewById(R.id.relative_video).setOnClickListener(this);
        photo_number = (TextView) view.findViewById(R.id.photo_number);
        video_number = (TextView) view.findViewById(R.id.video_number);

        //initData();
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG, "onResume: photo--video  ==  on");
        initData();
    }

    private void initData() {

        file = new File(StringUtils.local_media_path);
        if (file.exists()) {
            String[] list = file.list();
            photoList = new ArrayList<>();
            videoList = new ArrayList<>();

            for (String aList : list) {
                if (aList.endsWith("JPG")) {
                    photoList.add(StringUtils.local_media_path + aList);
                } else if (aList.endsWith("MOV")) {
                    videoList.add(StringUtils.local_media_path + aList);
                }
            }

            if (photoList.size() > 0) {
                Glide.with(getContext()).load(photoList.get(0)).into(iv_photo);
            }

            if (videoList.size() > 0) {
               new Thread(new Runnable() {
                   @Override
                   public void run() {
                       bitmap = ThumbnailUtils.createVideoThumbnail(videoList.get(0), MINI_KIND);
                       //extractThumbnail 方法二次处理,以指定的大小提取居中的图片,获取最终我们想要的图片
                       bitmap = ThumbnailUtils.extractThumbnail(bitmap, 300, 300, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);

                   getActivity().runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           iv_video.setImageBitmap(bitmap);
                       }
                   });
                   }
               }).start();


                photo_number.setText(photoList.size() + "");
                video_number.setText(videoList.size() + "");
                file = null;
            }
            } else {
                photo_number.setText("0");
                video_number.setText("0");
            }

        }

        @Override
        public void onClick (View v){
            switch (v.getId()) {
                case R.id.relative_photo://全部图片

                    if (photoList != null && photoList.size() > 0) {
                        Bundle bundle = new Bundle();
                        bundle.putStringArrayList("photo", photoList);
                        bundle.putStringArrayList("video", videoList);
                        bundle.putString("type", "photo");
                        startActivity(new Intent(getActivity(), FileActivity.class).putExtra("data", bundle));
                    }
                    break;
                case R.id.relative_video://全部视频
                    if (videoList != null && videoList.size() > 0) {
                        Bundle bundles = new Bundle();
                        bundles.putStringArrayList("photo", photoList);
                        bundles.putStringArrayList("video", videoList);
                        bundles.putString("type", "video");
                        startActivity(new Intent(getActivity(), FileActivity.class).putExtra("data", bundles));
                    }
                    break;
            }
        }
    }
