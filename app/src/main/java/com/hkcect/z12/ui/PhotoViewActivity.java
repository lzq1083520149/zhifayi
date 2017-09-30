package com.hkcect.z12.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;
import com.hkcect.z12.R;
import com.hkcect.z12.album.ImageDownloaderTask;
import com.hkcect.z12.album.ListItem;
import com.hkcect.z12.utils.StringUtils;
import com.hkcect.z12.utils.ViewPagerFixed;
import com.ntk.util.Util;

import java.io.File;
import java.util.ArrayList;

public class PhotoViewActivity extends AppCompatActivity {

    private ArrayList<String> photoList;
    private PhotoViewAdapter mAdapter;
    private ViewPagerFixed photo_view_vp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_view);
        initData();
        
    }

    private void initData() {
        photo_view_vp = (ViewPagerFixed) findViewById(R.id.photo_view_vp);
        photoList = getIntent().getStringArrayListExtra("photoList");
        mAdapter = new PhotoViewAdapter(this, photoList);
        photo_view_vp.setAdapter(mAdapter);
        int position = getIntent().getIntExtra("position", -1);
        if (position > -1 && photoList != null) {
            mAdapter.notifyDataSetChanged();
            photo_view_vp.setCurrentItem(position);
        } else {
            Toast.makeText(this, R.string.no_data, Toast.LENGTH_SHORT).show();
        }
    }

    class PhotoViewAdapter extends PagerAdapter {
        private ArrayList<String> listItem;
        private Context mContext;

        public PhotoViewAdapter(Context context, ArrayList<String> listItem) {
            mContext = context;
            this.listItem = listItem;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            LayoutInflater layoutInflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(R.layout.image_view_pager, null);
            TextView tv = (TextView) view.findViewById(R.id.tv);
            tv.setText((position + 1) + " / "+listItem.size());
            PhotoView photoView = (PhotoView) view.findViewById(R.id.simpe);
            container.addView(view);
            Glide.with(PhotoViewActivity.this)
                    .load(listItem.get(position)).into(photoView);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return listItem == null ? 0 : listItem.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

}
