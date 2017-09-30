package com.hkcect.z12.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hkcect.z12.R;
import com.hkcect.z12.util.DefineTable;
import com.hkcect.z12.utils.StringUtils;
import com.ntk.nvtkit.NVTKitModel;


public class InfoFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private TextView record_time;
    private TextView number_photo;
    private TextView fw_version;
    private TextView battery;
    private TextView card_status;
    private TextView card_size;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipe_setting;

    private enum Sync {
        START, DOME
    }

    private static InfoFragment instance = null;

    public static synchronized InfoFragment getInstance() {
        if (instance == null) {
            instance = new InfoFragment();
        }
        return instance;
    }


    private Sync state;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_main, container, false);
        record_time = (TextView) view.findViewById(R.id.record_time);
        number_photo = (TextView) view.findViewById(R.id.number_photo);
        fw_version = (TextView) view.findViewById(R.id.fw_version);
        battery = (TextView) view.findViewById(R.id.battery);
        card_status = (TextView) view.findViewById(R.id.card_status);
        card_size = (TextView) view.findViewById(R.id.card_size);
        progressBar = (ProgressBar) view.findViewById(R.id.pb);
        swipe_setting = (SwipeRefreshLayout) view.findViewById(R.id.swipe_setting);
        swipe_setting.setOnRefreshListener(this);
        swipe_setting.setEnabled(false);
        initData(false);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    private static final int RECORD_TIME = 0;
    private static final int LOAD_DONE = 5;
    private static final int NUMBER_PHOTO = 1;
    private static final int FW_VERSION = 3;
    private static final int ACK_BATTERY = 4;
    private static final int ACK_CARD_STATUS = 6;
    private static final int CARD_SIZE = 7;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RECORD_TIME:
                    String time = (String) msg.obj;
                    record_time.setText(StringUtils.fromatDate2(Long.valueOf(time)));
                    break;
                case LOAD_DONE:
                    state = Sync.DOME;
                    swipe_setting.setEnabled(true);
                    progressBar.setVisibility(View.GONE);
                    if (swipe_setting.isRefreshing()) {
                        swipe_setting.setRefreshing(false);
                    }
                    break;
                case NUMBER_PHOTO:
                    String number = (String) msg.obj;
                    number_photo.setText(number);
                    break;
                case FW_VERSION:
                    String version = (String) msg.obj;
                    fw_version.setText(version);
                    break;
                case ACK_BATTERY:
                    String ack_battery = (String) msg.obj;
                    switch (ack_battery) {
                        case DefineTable.NVTKitBatterStatus_FULL:
                            battery.setCompoundDrawables(ContextCompat.getDrawable(getContext(), R.drawable.battery_full), null, null, null);
                            battery.setText("100%");
                            break;
                        case DefineTable.NVTKitBatterStatus_MED:
                            battery.setCompoundDrawables(ContextCompat.getDrawable(getContext(), R.drawable.battery_75), null, null, null);
                            battery.setText("75%");
                            break;
                        case DefineTable.NVTKitBatterStatus_LOW:
                            battery.setCompoundDrawables(ContextCompat.getDrawable(getContext(), R.drawable.battery_half), null, null, null);
                            battery.setText("50%");
                            break;
                        case DefineTable.NVTKitBatterStatus_Exhausted:
                            battery.setCompoundDrawables(ContextCompat.getDrawable(getContext(), R.drawable.battery_25), null, null, null);
                            battery.setText("25%");
                            break;
                        case DefineTable.NVTKitBatterStatus_CHARGE:
                            battery.setCompoundDrawables(ContextCompat.getDrawable(getContext(), R.drawable.battery_charging), null, null, null);
                            battery.setText("15%");
                            break;
                        case DefineTable.NVTKitBatterStatus_EMPTY:
                            battery.setCompoundDrawables(ContextCompat.getDrawable(getContext(), R.drawable.battery_zero), null, null, null);
                            battery.setText("0%");
                            break;
                    }
                    break;
                case ACK_CARD_STATUS:
                    String ack_card_status = (String) msg.obj;
                    switch (ack_card_status) {
                        case "0":
                            card_status.setText(R.string.removed);
                            break;
                        case "1":
                            card_status.setText(R.string.inserted);
                            break;
                        case "2":
                            card_status.setText(R.string.locked);
                            break;
                    }
                    break;
                case CARD_SIZE:
                    String size = (String) msg.obj;
                    card_size.setText(StringUtils.byte2FitMemorySize(Long.valueOf(size)));
                    break;
            }
        }
    };


    private void initData(boolean isRefresh) {
        state = Sync.START;
        if (!isRefresh) {
            progressBar.setVisibility(View.VISIBLE);
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                state = Sync.START;
                //另起线程请求数据
                final String max_rec_time = NVTKitModel.qryMaxRecSec();//查询最大录影时间
                if (max_rec_time != null)
                    handler.obtainMessage(RECORD_TIME, max_rec_time).sendToTarget();

                final String free_capture_num = NVTKitModel.qryMaxPhotoNum();//剩余可拍照张数
                if (free_capture_num != null)
                    handler.obtainMessage(NUMBER_PHOTO, free_capture_num).sendToTarget();

                final String fw_version = NVTKitModel.qryFWVersion();
                if (fw_version != null)
                    handler.obtainMessage(FW_VERSION, fw_version).sendToTarget();

                final String ack_battery = NVTKitModel.qryBatteryStatus();
                if (ack_battery != null)
                    handler.obtainMessage(ACK_BATTERY, ack_battery).sendToTarget();

                final String ack_card_status = NVTKitModel.qryCardStatus();
                if (ack_card_status != null)
                    handler.obtainMessage(ACK_CARD_STATUS, ack_card_status).sendToTarget();

                final String result = NVTKitModel.qryDiskSpace();
                if (result != null)
                    handler.obtainMessage(CARD_SIZE, result).sendToTarget();
                handler.obtainMessage(LOAD_DONE).sendToTarget();

            }
        }).start();
    }


    //下拉刷新数据
    @Override
    public void onRefresh() {
        if (state == Sync.DOME) {
            initData(true);
        }
    }
}
