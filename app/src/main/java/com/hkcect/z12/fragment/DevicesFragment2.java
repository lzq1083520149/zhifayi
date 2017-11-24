package com.hkcect.z12.fragment;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hkcect.z12.R;
import com.hkcect.z12.ZApplication;
import com.hkcect.z12.example.VideoActivity;
import com.hkcect.z12.ui.DownloadActivity;
import com.hkcect.z12.ui.FileActivity;
import com.hkcect.z12.ui.SettingActivity;
import com.hkcect.z12.util.ProgressDialogUtlis;
import com.hkcect.z12.utils.OnWifiConnectListener;
import com.hkcect.z12.utils.SharedPreferencesHelper;
import com.hkcect.z12.utils.StringUtils;
import com.hkcect.z12.utils.WifiUtils;
import com.ntk.nvtkit.NVTKitModel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.provider.MediaStore.Video.Thumbnails.MINI_KIND;
import static com.hkcect.z12.R.id.iv_photo;

/**
 * A simple {@link Fragment} subclass.
 */
public class DevicesFragment2 extends Fragment implements View.OnClickListener {


    private ArrayList<String> photoList;

    public DevicesFragment2() {
    }

    private RelativeLayout rl_wei_lian_jie,  rl_bo_fang;
    private LinearLayout rl_yi_lian_jie;
    private TextView tv_devices_ssid;
    private ImageView iv_xiang_ce;
    private LinearLayout imgbtn_devices_settings, imgbtn_devices_down, imgbtn_devices_qie_huan;
    private static DevicesFragment2 inStance = null;
    private WifiManager wifiManager;
    private ProgressDialog progressDialog;
    private ConnectivityManager connectManager;
    private WifiInfo connectionInfo;
    private String dv_ssid;
    private File file;
    private NetworkReceiver networkReceiver;

    public static synchronized DevicesFragment2 newInstance() {
        if (inStance == null) {
            inStance = new DevicesFragment2();
        }
        return inStance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        connectManager = (ConnectivityManager) getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_devices2, container, false);

        rl_wei_lian_jie = (RelativeLayout) view.findViewById(R.id.rl_wifi_wei_lian_jie);
        rl_yi_lian_jie = (LinearLayout) view.findViewById(R.id.rl_wifi_yi_lian_jie);
        rl_bo_fang = (RelativeLayout) view.findViewById(R.id.rl_bo_fang);
        iv_xiang_ce = (ImageView) view.findViewById(R.id.iv_xiang_ce);
        tv_devices_ssid = (TextView) view.findViewById(R.id.tv_devices_ssid);
        imgbtn_devices_settings = (LinearLayout) view.findViewById(R.id.imgbtn_devices_settings);
        imgbtn_devices_down = (LinearLayout) view.findViewById(R.id.imgbtn_devices_down);
        imgbtn_devices_qie_huan = (LinearLayout) view.findViewById(R.id.imgbtn_devices_qie_huan);

        initClick();

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(getString(R.string.content_device));


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        isCancel = true;
        networkReceiver = new NetworkReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        getActivity().registerReceiver(networkReceiver, intentFilter);
        Log.e("TAG--", "注册 监听");
    }

    @Override
    public void onPause() {
        super.onPause();
        if (networkReceiver != null) {
            getActivity().unregisterReceiver(networkReceiver);
        }

    }

    private void initClick() {
        rl_wei_lian_jie.setOnClickListener(this);
        rl_bo_fang.setOnClickListener(this);
        imgbtn_devices_settings.setOnClickListener(this);
        imgbtn_devices_down.setOnClickListener(this);
        imgbtn_devices_qie_huan.setOnClickListener(this);
        iv_xiang_ce.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_wifi_wei_lian_jie:
                //未连接
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_WIFI_SETTINGS);
                startActivity(intent);
                Log.e("TAG--", "未连接");
                break;
            case R.id.rl_bo_fang:
                Log.e("TAG--", "播放");
                //播放
                intoStart("bo_fang");
                break;
            case R.id.imgbtn_devices_settings:
                //设置
                intoStart("setting");
                Log.e("TAG--", "设置");
                break;
            case R.id.imgbtn_devices_down:
                //下载
                intoStart("download");
                Log.e("TAG--", "下载");
                break;
            case R.id.iv_xiang_ce:
                //相册
//                initPhoto();
//                if (photoList != null && photoList.size() > 0) {
//                    Bundle bundle = new Bundle();
//                    bundle.putStringArrayList("photo", photoList);
//                    bundle.putString("type", "photo");
//                    startActivity(new Intent(getActivity(), FileActivity.class).putExtra("data", bundle));
//                } else {
//                    Toast.makeText(getContext(), getString(R.string.ben_di_mei_tu_pina),
//                            Toast.LENGTH_SHORT).show();
//                }
                break;
            case R.id.imgbtn_devices_qie_huan:
                //切换
                Log.e("TAG--", "切换");
                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.wifi_Switch)
                        .setMessage(R.string.wifi_Switch_msg)
                        .setNegativeButton(R.string.wifi_pwd_confirm, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent1 = new Intent();
                                intent1.setAction(Settings.ACTION_WIFI_SETTINGS);
                                startActivity(intent1);
                            }
                        }).setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();


                break;
        }
    }

    private void initPhoto() {
        file = new File(StringUtils.local_media_path);
        if (file.exists()) {
            String[] list = file.list();
            photoList = new ArrayList<>();
            for (String aList : list) {
                if (aList.endsWith("JPG")) {
                    photoList.add(StringUtils.local_media_path + aList);
                }
            }

        }
    }

    private void intoStart(final String type) {
        switch (type) {
            case "bo_fang":
                startActivity(new Intent(getContext(), VideoActivity.class));
                break;
            case "setting":
                startActivity(new Intent(getContext(), SettingActivity.class));
                break;
            case "download":
                startActivity(new Intent(getContext(), DownloadActivity.class));
                break;

        }
    }


    public class NetworkReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("TAG--", "WIFI 监听");
            // 监听网络连接，包括wifi和移动数据的打开和关闭,以及连接上可用的连接都会接到监听
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                Log.e("TAG--", "网络 监听");
                //获取联网状态的NetworkInfo对象
                NetworkInfo info = intent
                        .getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                if (info != null) {
                    Log.e("TAG--", "网络 连上");

                    //如果当前的网络连接成功
                    if (NetworkInfo.State.CONNECTED == info.getState()) {
                        if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                            Log.e("TAG--", "wifi 连上");
                            isQuzhengyi();
                        } else {
                            Log.e("TAG--", "移动网络 连上");
                            showConnectWifi(true);
                        }
                    } else {
                        Log.e("TAG--", "没有网络");
                        showConnectWifi(true);
                    }
                }

            }

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (networkReceiver != null) {
            getActivity().unregisterReceiver(networkReceiver);
        }

    }

    //是否显示连接WiFi的item
    private void showConnectWifi(boolean isShowConnectWifi) {
        if (isShowConnectWifi) {
            rl_yi_lian_jie.setVisibility(View.GONE);
            rl_wei_lian_jie.setVisibility(View.VISIBLE);
            tv_devices_ssid.setText("");
        } else {
            connectionInfo = wifiManager.getConnectionInfo();
            dv_ssid = connectionInfo.getSSID();
            rl_yi_lian_jie.setVisibility(View.VISIBLE);
            rl_wei_lian_jie.setVisibility(View.GONE);
            tv_devices_ssid.setText(dv_ssid.replace("\"", ""));
        }

    }
    private boolean isCancel = true;
    private void isQuzhengyi() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //判断是否为运动相机  随便调用两条命令看返回值是否为空
                //查询SDK版本
                final String sdkVersions = NVTKitModel.getVersion();
                //查询电池状态
                final String batteryStatus = NVTKitModel.qryBatteryStatus();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (sdkVersions != null && batteryStatus != null) {

                            Log.e("sdkVersions = > ", sdkVersions);
                            showConnectWifi(false);
                        } else {
                            showConnectWifi(true);
                            if (isCancel){
                                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setTitle(R.string.ti_shi_title)
                                        .setMessage(R.string.ti_shi_msg)
                                        .setNegativeButton(R.string.wifi_pwd_confirm, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent intent1 = new Intent();
                                                intent1.setAction(Settings.ACTION_WIFI_SETTINGS);
                                                startActivity(intent1);
                                            }
                                        }).setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        isCancel = false;
                                        dialog.dismiss();
                                    }
                                }).show();
                            }

                        }
                    }
                });

            }
        }).start();

    }
}
