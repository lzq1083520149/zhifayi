package com.hkcect.z12.ui;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.hkcect.z12.R;
import com.hkcect.z12.adapter.MyViewPagerAdapter;
import com.hkcect.z12.album.ListItem;
import com.hkcect.z12.example.PlaybackActivity;
import com.hkcect.z12.fragment.AboutFragment;
import com.hkcect.z12.fragment.DevicesFragment;
import com.hkcect.z12.fragment.DevicesFragment2;
import com.hkcect.z12.fragment.LocalMediaFragment;
import com.hkcect.z12.fragment.VideoFragment;
import com.hkcect.z12.utils.BottomNavigationViewEx;
import com.ntk.nvtkit.NVTKitModel;
import com.ntk.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener,
         Toolbar.OnMenuItemClickListener, BottomNavigationView.OnNavigationItemSelectedListener {

    private Toolbar mToolbar;
    private BottomNavigationView bottomNavigationView;
    private ViewPager mViewPager;


    private AlertDialog alertDialog;
    private static final String TAG = "MainActivity";
    // TabLayout中的tab标题
//    private String[] mTitles;
    // ViewPager的数据适配器
    private MyViewPagerAdapter mViewPagerAdapter;
    // 填充到ViewPager中的Fragment
    private List<Fragment> mFragments;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        new NVTKitModel(this);
        initView();
        initData();
    }

    private void initView() {
        mToolbar = (Toolbar) findViewById(R.id.id_toolbar);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigationView);
        bottomNavigationView.getMenu().getItem(0).setChecked(true);
        mViewPager = (ViewPager) findViewById(R.id.viewpager_home);
        // 设置显示Toolbar
        setSupportActionBar(mToolbar);
        mToolbar.setOnMenuItemClickListener(this);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                mViewPager.setCurrentItem(item.getItemId());
                return true;
            }
        });
//        bottomNavigationView.setupWithViewPager(mViewPager,true);
    }

    private void initData() {
        // Tab的标题采用string-array的方法保存，在res/values/arrays.xml中写
//        mTitles = getResources().getStringArray(R.array.tab_titles);
        //初始化填充到ViewPager中的Fragment集合
        mFragments = new ArrayList<>();
        for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
            if (i == 0) {
                DevicesFragment2 mediaFragment = DevicesFragment2.newInstance();
                mFragments.add(i, mediaFragment);
            } else if (i == 1) {
                LocalMediaFragment homeFragment = LocalMediaFragment.newInstance();
                mFragments.add(i, homeFragment);

            } else if (i == 2) {
                Bundle mBundle = new Bundle();
                mBundle.putInt("flag", i);
                AboutFragment mFragment = AboutFragment.newInstance();
                mFragment.setArguments(mBundle);
                mFragments.add(i, mFragment);
            }
        }
        // 初始化ViewPager的适配器，并设置给它
        mViewPagerAdapter = new MyViewPagerAdapter(getSupportFragmentManager(), mFragments);
        mViewPager.setAdapter(mViewPagerAdapter);
        // 设置ViewPager最大缓存的页面个数
        mViewPager.setOffscreenPageLimit(3);
        // 给ViewPager添加页面动态监听器（为了让Toolbar中的Title可以变化相应的Tab的标题）
        mViewPager.addOnPageChangeListener(this);
        bottomNavigationView.setOnNavigationItemSelectedListener(this);
    }

    @Override
    public void onPageSelected(int position) {
//        mToolbar.setTitle(mTitles[position]);
        mToolbar.setTitle(bottomNavigationView.getMenu().getItem(position).getTitle());
        bottomNavigationView.getMenu().getItem(position).setChecked(true);
    }


    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    private AlertDialog.Builder builder;
    private AlertDialog dialog;

    @Override
    protected void onResume() {
        super.onResume();
//        String result = NVTKitModel.devHeartBeat();
//        if (result == null) {
//            //没有查询到设备
//            builder = new AlertDialog.Builder(this);
//            builder.setTitle(R.string.no_query_device)
//                    .setMessage(R.string.connection_vifi)
//                    .setNegativeButton("exit", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            System.exit(0);
//                        }
//                    })
//                    .setPositiveButton(R.string.go_setting, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
//                        }
//                    });


//            dialog = builder.create();
//            dialog.setCancelable(false);
//            dialog.setCanceledOnTouchOutside(false);
//            dialog.show();
//            Log.e(TAG, "heartbeat fail");
//        } else if (result.equals("success")) {
//            NVTKitModel.setWifiEventListener(eventHandler);//设置监听，设备是否在录像等信息
//            if (NVTKitModel.devAPPSessionFuncEnabled()) {
//                if (NVTKitModel.devAPPSessionQryIsClosed()) {
//                    String code = NVTKitModel.devAPPSessionOpen();
//                    if (code != null) {
//                        if (dialog.isShowing()) {
//                            dialog.dismiss();
//                        }
//                    }
//                }
//            }
//        }
//
//        if (alertDialog != null && alertDialog.isShowing())
//            alertDialog.dismiss();

    }



//    /*
//    视频文件点击回调
//    item：数据源
//    id：点击的position
//     */
//    @Override
//    public void OnVideoClick(ListItem item, final int id, final ArrayList<ListItem> videoList) {
//        if (videoList.get(id).isDownload) {
//            //存在本地视频
//            Uri uri = Uri.fromFile(new File(Util.local_movie_path + videoList.get(id).getName()));
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//            intent.setDataAndType(uri, "video/mp4");
//            if (intent.resolveActivity(getPackageManager()) != null)
//                startActivity(intent);
//            else
//                Toast.makeText(MainActivity.this, R.string.no_available_player, Toast.LENGTH_LONG).show();
//        } else {
//            //不存在本地视频
//            //弹出Dialog确认是否在线查看视频。
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setMessage(getText(R.string.no_data_retry))
//                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            Intent intent = new Intent();
//                            intent.putExtra("url", videoList.get(id).getUrl());
//                            intent.setClass(MainActivity.this, PlaybackActivity.class);
//                            startActivity(intent);
//                        }
//                    })
//                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.dismiss();
//                        }
//                    });
//            AlertDialog dialog = builder.create();
//            dialog.show();
//        }
//    }


    /*
    Menu的Item点击事件
    */
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_device://扫描添加设备
                //startActivity(new Intent(MainActivity.this, WifiListActivity.class));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            1);
                } else {
                    startWiFi();
                }
                break;
        }
        return true;
    }

    private void startWiFi(){
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_WIFI_SETTINGS);
        startActivity(intent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.nav_devices:
                if (mViewPager.getCurrentItem() != 0)
                    mViewPager.setCurrentItem(0, true);
                break;
            case R.id.nav_home:
                if (mViewPager.getCurrentItem() != 1)
                    mViewPager.setCurrentItem(1, true);
                break;
            case R.id.nav_settings:
                if (mViewPager.getCurrentItem() != 2)
                    mViewPager.setCurrentItem(2, true);
                break;
        }

        return true;
    }

    //两次退出
    long startTime = 0;

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - startTime < 1500) {
            super.onBackPressed();
        } else {
            Toast.makeText(this, R.string.application_exit, Toast.LENGTH_SHORT).show();
            startTime = System.currentTimeMillis();
        }
    }

}
