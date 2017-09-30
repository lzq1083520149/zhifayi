package com.hkcect.z12.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hkcect.z12.R;
import com.hkcect.z12.utils.WifiAutoConnectManager;
import com.hkcect.z12.utils.WifiUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class WifiListActivity extends BaseActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "WifiListActivity";
    private EditText wifi_pwd;
    private RecyclerView recycler_wifi;
    private final Timer mTimer = new Timer();
    private MyAdapter myAdapter;
    private WifiManager mWifiManager;
    private TimerTask mTask;
    protected static final int WIFI_AP_STATE_DISABLED = 11;
    private List<ScanResult> results;
    private SwipeRefreshLayout swipe_layout;
    private TextView tv_wifi_name;
    private WifiAutoConnectManager wac;
    public static ProgressDialog dialog;
    public static  String receiverAction = "com.hkcect.z12.wifiListActivity.wifiConnected";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_list);
        initView();
        initData();
    }


    /*
    初始化组件
     */
    private void initView() {

        findViewById(R.id.btn_ok).setOnClickListener(this);
        wifi_pwd = (EditText) findViewById(R.id.wifi_pwd);

        tv_wifi_name = (TextView) findViewById(R.id.tv_wifi_name);

        recycler_wifi = (RecyclerView) findViewById(R.id.recycler_wifi);
        recycler_wifi.setLayoutManager(new LinearLayoutManager(this));
        myAdapter = new MyAdapter(results);
        recycler_wifi.setAdapter(myAdapter);

        swipe_layout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        swipe_layout.setOnRefreshListener(this);
    }

    /*
    扫描wifi，获取数据
     */
    private void initData() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    1);
        } else {
            scanWifi();
        }

    }


    private void scanWifi() {
        mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wac = new WifiAutoConnectManager(mWifiManager);

        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        wac.mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // 操作界面
                String info = (String) msg.obj;
                switch (info) {
                    case "opened"://开始连wifi
                        dialog.setMessage(getString(R.string.is_contenting_wifi));
                        dialog.show();
                        break;
                    case "done"://连接
                        dialog.dismiss();
                        break;
                }
            }
        };

        try {
            Method method1 = mWifiManager.getClass().getMethod("setWifiApEnabled",
                    WifiConfiguration.class, boolean.class);
            method1.invoke(mWifiManager, null, false);
            while (WifiUtils.getWifiAPState(mWifiManager) != WIFI_AP_STATE_DISABLED) {
                Thread.sleep(200);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
        mWifiManager.startScan();

        results = mWifiManager.getScanResults();

        myAdapter.setData(results);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            scanWifi();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok://确定按钮的点击事件

                String pwd = wifi_pwd.getText().toString();
                if (pwd.isEmpty()) {
                    Toast.makeText(this, R.string.input_pwd, Toast.LENGTH_SHORT).show();
                    return;
                }

                Boolean selected = false;
                ScanResult scanResult = null;

                for (int i = 0; i < myAdapter.isChecked.size(); i++) {
                    if (myAdapter.isChecked.get(i)) {
                        selected = true;
                        scanResult = myAdapter.scanList.get(i);
                    }
                }

                if (!selected) {
                    Toast.makeText(this, R.string.select_wifi, Toast.LENGTH_SHORT).show();
                    return;
                }
                Log.e(TAG, "onClick: wifissidpwd"+scanResult.SSID+"  "+pwd);
                wac.connect(scanResult.SSID, pwd,
                        pwd.equals("") ? WifiAutoConnectManager.WifiCipherType.WIFICIPHER_NOPASS : WifiAutoConnectManager.WifiCipherType.WIFICIPHER_WPA);

                finish();
                break;
        }
    }

    @Override
    public void onRefresh() {
        mWifiManager.startScan();

        results = mWifiManager.getScanResults();

        myAdapter.setData(results);

    }


    private class MyAdapter extends RecyclerView.Adapter<MyViewHoder> {

        private List<ScanResult> scanList;
        private HashMap<Integer, Boolean> isChecked;

        private MyAdapter(List<ScanResult> list) {
            this.scanList = list;
            isChecked = new HashMap<>();
            if (scanList != null) {
                for (int i = 0; i < scanList.size(); i++) {
                    isChecked.put(i, false);
                }
            }
        }

        public void setData(List<ScanResult> list) {
            if (scanList != null)
                scanList.clear();
            scanList = list;

            if (scanList != null) {
                for (int i = 0; i < scanList.size(); i++) {
                    isChecked.put(i, false);
                }
            }
            notifyDataSetChanged();
            if (swipe_layout.isRefreshing()) {
                swipe_layout.setRefreshing(false);
            }
        }

        @Override
        public MyViewHoder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MyViewHoder(LayoutInflater.from(WifiListActivity.this)
                    .inflate(R.layout.activity_wifi_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(final MyViewHoder holder, int position) {

//            if (holder.itemView.getTag()==null){
//                holder.itemView.setTag(position);
//            }
//
//            if (holder.itemView.getTag().equals(position)) {
                holder.tv_wifi.setText(scanList.get(position).SSID);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    switch (scanList.get(position).channelWidth) {
                        case 0:
                            holder.tv_type.setText("20MHZ");
                            break;
                        case 1:
                            holder.tv_type.setText("40MHZ");
                            break;
                        case 2:
                            holder.tv_type.setText("80MHZ");
                            break;
                        case 3:
                            holder.tv_type.setText("160MHZ");
                            break;
                        case 4:
                            holder.tv_type.setText("80MHZ_PLUS_MHZ");
                            break;
                    }

                if (isChecked.get(position)) {
                    holder.itemView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                } else {
                    holder.itemView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
                }

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (int i = 0; i < isChecked.size(); i++) {
                            if (isChecked.get(i)) {
                                isChecked.put(i, false);
                            }
                        }
                        tv_wifi_name.setText(String.format(getString(R.string.input_wifi_pwd), scanList.get(holder.getAdapterPosition()).SSID));
                        isChecked.put(holder.getAdapterPosition(), true);
                        holder.itemView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
                        notifyDataSetChanged();
                    }
                });

//                int level = scanList.get(position).level;
//
//                if (holder.iv_wifi != null) {
//                    if (level <= 0 && level >= -25) {
                        //信号最好
//                        Glide.with(WifiListActivity.this).load(R.drawable.wifi).into(holder.iv_wifi);
//                    } else if (level < -25 && level >= -45) {
//                         信号较好
//                        Glide.with(WifiListActivity.this).load(R.drawable.ic_wifi_live_3).into(holder.iv_wifi);
//                    } else if (level < -45 && level >= -60) {
//                         信号一般
//                        Glide.with(WifiListActivity.this).load(R.drawable.ic_wifi_live_2).into(holder.iv_wifi);
//                    } else if (level < -60 && level >= -100) {
//                        Glide.with(WifiListActivity.this).load(R.drawable.ic_wifi_live_1).into(holder.iv_wifi);
//                        信号较差
//                    } else {
//                        无信号
//                        Glide.with(WifiListActivity.this).load(R.drawable.ic_wifi_live_1).into(holder.iv_wifi);
//                    }
//                }
//            }

        }

        @Override
        public int getItemCount() {
            return scanList == null ? 0 : scanList.size();
        }
    }

    class MyViewHoder extends RecyclerView.ViewHolder {

        TextView tv_wifi;
        ImageView iv_wifi;
        TextView tv_type;

        MyViewHoder(View itemView) {
            super(itemView);
            tv_wifi = (TextView) itemView.findViewById(R.id.tv_wifi);
            tv_type = (TextView) itemView.findViewById(R.id.tv_type);
            iv_wifi = (ImageView) findViewById(R.id.iv_wifi);

        }
    }

}
