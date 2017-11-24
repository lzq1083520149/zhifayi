package com.hkcect.z12.fragment;


import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.VirtualDisplay;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
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
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hkcect.z12.R;
import com.hkcect.z12.example.MenuActivity;
import com.hkcect.z12.example.VideoActivity;
import com.hkcect.z12.ui.DownloadActivity;
import com.hkcect.z12.ui.SettingActivity;
import com.hkcect.z12.ui.WifiListActivity;
import com.hkcect.z12.util.ClientScanResult;
import com.hkcect.z12.util.FinishScanListener;
import com.hkcect.z12.util.WifiAPUtil;
import com.hkcect.z12.utils.InterfaceProxy;
import com.hkcect.z12.utils.OnWifiConnectListener;
import com.hkcect.z12.utils.OnWifiListener;
import com.hkcect.z12.utils.SharedPreferencesHelper;
import com.hkcect.z12.utils.StringUtils;
import com.hkcect.z12.utils.WifiUtils;
import com.ntk.nvtkit.NVTKitModel;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class DevicesFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private List<WifiConfiguration> wifiList;
    private static final String TAG = "DevicesFragment";
    private SharedPreferencesHelper sp;

    public DevicesFragment() {
    }

    private static DevicesFragment inStance = null;
    private RecyclerView recycler_devices;
    private DevicesAdapter adapter;
    private SwipeRefreshLayout swipe_devices;
    private WifiManager wifiManager;
    private List<WifiConfiguration> configuratedList;
    private ProgressDialog progressDialog;
    private NetworkReceiver networkReceiver;
    private ConnectivityManager connectManager;
    //    private String currentSSID;
    private WifiInfo connectionInfo;

    public static synchronized DevicesFragment newInstance() {
        if (inStance == null) {
            inStance = new DevicesFragment();
        }
        return inStance;
    }


    private static final int CANCELMSG = 0;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CANCELMSG:
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                        Toast.makeText(getContext(), getString(R.string.connect_failed),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        connectManager = (ConnectivityManager) getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        sp=new SharedPreferencesHelper(getContext());
    }

    private void getData() {
//        if (!wifiManager.isWifiEnabled()){
//            wifiManager.setWifiEnabled(true);
//        }

        if (configuratedList != null) {
            configuratedList.clear();
        }
        configuratedList = wifiManager.getConfiguredNetworks();
        //List<ScanResult> scanResults = wifiManager.getScanResults();
         connectionInfo = wifiManager.getConnectionInfo();
        //WifiInfo connectionInfo = wifiManager.getConnectionInfo();
        //Log.e(TAG, "wifi list: " + configuratedList.toString());
        wifiList = new ArrayList<>();
        if (wifiList != null) {
            wifiList.clear();
        }
        if (connectionInfo != null) {
            if (configuratedList != null) {
                for (int i = 0; i < configuratedList.size(); i++) {
                    String ssid = configuratedList.get(i).SSID;
                    if (ssid.equals(connectionInfo.getSSID())) {
                        wifiList.add(configuratedList.get(i));
                    }
                }
            }
        }
        if (connectionInfo != null) {
            if (configuratedList != null) {
                for (int i = 0; i < configuratedList.size(); i++) {
                    String ssid = configuratedList.get(i).SSID;
                    if (!ssid.equals(connectionInfo.getSSID())) {
                        wifiList.add(configuratedList.get(i));
                    }
                }
            }
        }

//        //添加第一个为已连接的WiFi
//        if (connectionInfo != null) {
//            if (configuratedList != null) {
//                for (int i = 0; i < configuratedList.size(); i++) {
//                    String ssid = configuratedList.get(i).SSID;
//                    if (ssid.startsWith("\"" + StringUtils.devices_name) &&ssid.equals(connectionInfo.getSSID())||ssid.startsWith("\"" + "CarDV")) {
//                        wifiList.add(configuratedList.get(i));
//                    }
//                }
//            }
//        }
//        if (configuratedList == null) {
//            //未找到数据
//        } else {
//            for (int i = 0; i < configuratedList.size(); i++) {
//                String ssid = configuratedList.get(i).SSID;
//
//                //NVT_CARD
//                if (ssid.startsWith("\"" + StringUtils.devices_name) && !ssid.equals(connectionInfo.getSSID())||ssid.startsWith("\"" + "CarDV")) {
//                    wifiList.add(configuratedList.get(i));
//                }
//            }
//        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_devices, container, false);
        swipe_devices = (SwipeRefreshLayout) view.findViewById(R.id.swipe_devices);
        swipe_devices.setOnRefreshListener(this);

        recycler_devices = (RecyclerView) view.findViewById(R.id.recycler_devices);
        recycler_devices.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new DevicesAdapter(wifiList);
        recycler_devices.setAdapter(adapter);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(getString(R.string.content_device));
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        getActivity().getMenuInflater().inflate(R.menu.device_menu, menu);
    }

    @Override
    public void onRefresh() {
        getData();
        adapter.setData(wifiList);
    }


    @Override
    public void onResume() {
        super.onResume();
        //Log.e("wifi Settings -> ",Settings.Global.WIFI_DEVICE_OWNER_CONFIGS_LOCKDOWN+"");
        getData();
        adapter.setData(wifiList);

        networkReceiver = new NetworkReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        intentFilter.addAction("android.net.wifi.STATE_CHANGE");
        getActivity().registerReceiver(networkReceiver, intentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(networkReceiver);
    }


    private class DevicesAdapter extends RecyclerView.Adapter<MyHolder> {

        private ConnectivityManager connectivityManager;
        private WifiManager wifiManager;
        private List<WifiConfiguration> data;

        private DevicesAdapter(List<WifiConfiguration> data) {
            this.data = data;
            connectivityManager = (ConnectivityManager) getContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            wifiManager = (WifiManager) getContext().getApplicationContext()
                    .getSystemService(Context.WIFI_SERVICE);
        }

        @Override
        public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MyHolder(LayoutInflater.from(getContext())
                    .inflate(R.layout.fragment_devices_item, parent, false));
        }

        @Override
        public void onBindViewHolder(MyHolder holder, final int position) {
            holder.tv_devices_item.setText(data.get(position).SSID.replace("\"", ""));
            holder.imgbtn_devices_item_settings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (WifiUtils.isWifiOpen(getContext())) {
                        boolean iswifi = WifiUtils.isNVTWifi(connectivityManager, wifiManager, wifiList.get(position).SSID);
                        if (iswifi) {
//                            new Thread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    if (NVTKitModel.devAPPSessionFuncEnabled()) {
//                                        if (NVTKitModel.devAPPSessionQryIsClosed()) {
//                                            String result = NVTKitModel.devAPPSessionOpen();
//                                            if (result != null) {
//                                                getActivity().runOnUiThread(new Runnable() {
//                                                    @Override
//                                                    public void run() {
//                                                        startActivity(new Intent(getContext(), SettingActivity.class));
//                                                    }
//                                                });
//                                            }
//                                        } else {
                            startActivity(new Intent(getContext(), SettingActivity.class));
//                                        }
//                                    }
//                                }
//                            }).start();
                        } else {
                            WifiUtils.getInstant(getContext()).connectionWifiByNetworkId(wifiList.get(position).SSID, wifiList.get(position).networkId, new OnWifiConnectListener() {
                                @Override
                                public void onFailure() {
                                }

                                @Override
                                public void onFinish() {
                                }

                                @Override
                                public void onStart(String ssid) {
                                    progressDialog.setMessage(String.format(getString(R.string.content_device), ssid));
                                    progressDialog.show();
                                    handler.postDelayed(runnable, 10000);

                                }
                            });
                        }
                    } else if (!WifiUtils.isWifiOpen(getContext())) {
                        Toast.makeText(getContext(), R.string.open_wifi, Toast.LENGTH_SHORT).show();
                    }

                }
            });

            holder.imgbtn_devices_item_down.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (WifiUtils.isWifiOpen(getContext())) {
                        boolean iswifi = WifiUtils.isNVTWifi(connectivityManager, wifiManager, wifiList.get(position).SSID);
                        if (iswifi) {


//                            new Thread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    if (NVTKitModel.devAPPSessionFuncEnabled()) {
//                                        if (NVTKitModel.devAPPSessionQryIsClosed()) {
//                                            String result = NVTKitModel.devAPPSessionOpen();
//                                            if (result != null) {
//                                                getActivity().runOnUiThread(new Runnable() {
//                                                    @Override
//                                                    public void run() {
//                                                        startActivity(new Intent(getContext(), DownloadActivity.class));
//                                                    }
//                                                });
//                                            }
//                                        } else {
                            startActivity(new Intent(getContext(), DownloadActivity.class));
//                                        }
//                                    }
//                                }
//                            }).start();

                        } else {
                            WifiUtils.getInstant(getContext()).connectionWifiByNetworkId(wifiList.get(position).SSID, wifiList.get(position).networkId, new OnWifiConnectListener() {
                                @Override
                                public void onFailure() {
                                }

                                @Override
                                public void onFinish() {
                                }

                                @Override
                                public void onStart(String ssid) {
//                                    type = TYPE.setting;
                                    progressDialog.setMessage(String.format(getString(R.string.content_device), ssid));

                                    progressDialog.show();
                                    handler.postDelayed(runnable, 10000);


                                }
                            });
                        }
                    } else if (!WifiUtils.isWifiOpen(getContext())) {
                        Toast.makeText(getContext(), R.string.open_wifi, Toast.LENGTH_SHORT).show();
                    }
                }
            });
            holder.imgbtn_devices_item_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    // TODO: 2017/3/21  删除已连接网络操作，安卓6.0之后不能对WifiConfiguration进行操作了。
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage(getString(R.string.delete_devices_message));
                    builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

//                           boolean isremove= wifiManager.removeNetwork(wifiList.get(position).networkId);
//                            Log.e("isremove",isremove+"  "+wifiList.get(position).networkId);
//                            wifiManager.saveConfiguration();
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_WIFI_SETTINGS);
                            startActivity(intent);

                        }

                    }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder.create().show();

                }
            });
            holder.iv_devices_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    intoLive(position);
                }
            });

            holder.tv_devices_item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    intoLive(position);
                }
            });

        }

        private void intoLive(int position) {
            if (WifiUtils.isWifiOpen(getContext())) {
                boolean iswifi = WifiUtils.isNVTWifi(connectivityManager, wifiManager, wifiList.get(position).SSID);
                if (iswifi) {
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (NVTKitModel.devAPPSessionFuncEnabled()) {
//                                if (NVTKitModel.devAPPSessionQryIsClosed()) {
//                                    String result = NVTKitModel.devAPPSessionOpen();
//                                    if (result != null) {
//                                        getActivity().runOnUiThread(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                startActivity(new Intent(getContext(), VideoActivity.class));
//                                            }
//                                        });
//                                    }
//                                } else {
                    startActivity(new Intent(getContext(), VideoActivity.class));
//                                }
//                            }
//                        }
//                    }).start();

                } else {
                    WifiUtils.getInstant(getContext()).connectionWifiByNetworkId(wifiList.get(position).SSID, wifiList.get(position).networkId, new OnWifiConnectListener() {
                        @Override
                        public void onFailure() {
                        }

                        @Override
                        public void onFinish() {
                        }

                        @Override
                        public void onStart(String ssid) {
//                                    type = TYPE.setting;
                            progressDialog.setMessage(String.format(getString(R.string.content_device), ssid));
                            progressDialog.show();
                            handler.postDelayed(runnable, 10000);

                        }
                    });
                }
            } else if (!WifiUtils.isWifiOpen(getContext())) {
                Toast.makeText(getContext(), R.string.open_wifi, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public int getItemCount() {
            return data == null ? 0 : data.size();
        }

        public void setData(List<WifiConfiguration> data) {
            this.data = data;
            notifyDataSetChanged();
            if (swipe_devices.isRefreshing()) {
                swipe_devices.setRefreshing(false);
            }
        }
    }


    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            handler.sendEmptyMessage(CANCELMSG);
        }
    };

    class MyHolder extends RecyclerView.ViewHolder {
        TextView tv_devices_item;
        ImageButton imgbtn_devices_item_settings;
        ImageButton imgbtn_devices_item_down;
        ImageButton imgbtn_devices_item_delete;
        ImageView iv_devices_item;

        public MyHolder(View itemView) {
            super(itemView);
            tv_devices_item = (TextView) itemView.findViewById(R.id.tv_devices_item);
            imgbtn_devices_item_settings = (ImageButton) itemView.findViewById(R.id.imgbtn_devices_item_settings);
            imgbtn_devices_item_down = (ImageButton) itemView.findViewById(R.id.imgbtn_devices_item_down);
            imgbtn_devices_item_delete = (ImageButton) itemView.findViewById(R.id.imgbtn_devices_item_delete);
            iv_devices_item = (ImageView) itemView.findViewById(R.id.iv_devices_item);
        }
    }

//    private TYPE type = null;
//
//    private enum TYPE {
//        setting, play, download
//    }


//    OnWifiListener wifiListener = new OnWifiListener() {
//        @Override
//        public void onFailure() {
//        }
//
//        @Override
//        public void onFinish() {
//            if (type != null) {
//                switch (type) {
//                    case setting:
//                        if (WifiUtils.isNVTWifi(connectManager, wifiManager, currentSSID)) {
//                            if (progressDialog != null && progressDialog.isShowing()) {
//                                progressDialog.dismiss();
//                            }
//                            startActivity(new Intent(getContext(), SettingActivity.class));
//                            type = null;
//                        }
//                        break;
//                    case play:
//                        if (WifiUtils.isNVTWifi(connectManager, wifiManager, currentSSID)) {
//                            if (progressDialog != null && progressDialog.isShowing()) {
//                                progressDialog.dismiss();
//                            }
//                            startActivity(new Intent(getContext(), VideoActivity.class));
//                            type = null;
//                        }
//
//                        break;
//                    case download:
//                        if (
//                                //NVTKitModel.devHeartBeat() != null &&
//                                WifiUtils.isNVTWifi(connectManager, wifiManager, currentSSID)) {
//                            if (progressDialog != null && progressDialog.isShowing()) {
//                                progressDialog.dismiss();
//                            }
//                            startActivity(new Intent(getContext(), DownloadActivity.class));
//                            type = null;
//                        }
//                        break;
//                }
//            }
//        }
//
//        @Override
//        public void onStart(String ssid) {
//        }
//    };


    public class NetworkReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // 监听wifi的连接状态即是否连上了一个有效无线路由
            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                Parcelable parcelableExtra = intent
                        .getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (null != parcelableExtra) {
                    // 获取联网状态的NetWorkInfo对象
                    NetworkInfo networkInfo = (NetworkInfo) parcelableExtra;
                    //获取的State对象则代表着连接成功与否等状态
                    NetworkInfo.State state = networkInfo.getState();
                    //判断网络是否已经连接
                    boolean isConnected = state == NetworkInfo.State.CONNECTED;
                    Log.e("TAG", "isConnected:" + isConnected);
                    if (isConnected) {
                        //连接到wifi
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        handler.removeCallbacks(runnable);
                        handler.removeMessages(CANCELMSG);
//                        wifiListener.onFinish();
                    } else {
                        //未连接到wifi
                    }
                }
            }

        }
    }

}
