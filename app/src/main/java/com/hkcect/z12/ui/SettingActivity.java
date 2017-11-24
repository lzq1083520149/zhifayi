package com.hkcect.z12.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.hkcect.z12.R;
import com.hkcect.z12.util.DefineTable;
import com.hkcect.z12.util.ProfileItem;
import com.hkcect.z12.utils.StringUtils;
import com.ntk.nvtkit.NVTKitModel;
import com.ntk.util.ParseResult;
import com.ntk.util.Util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


public class SettingActivity extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {


    private TextView device_name;//摄像机名字
    private TextView device_pwd;//摄像机密码
    private TextView tv_record_quality;//录像质量
    private TextView format;//格式化
    //private TextView advanced_settings;//高级设置
    private TextView tv_info;//摄像机信息
    private Switch switch_record;//自动录像开关
    private TextView record_time;//录制时长

    private boolean isAutoRecord = false;//已经保存的信息，是否自动录像。

    private boolean isLoading = false;
    private ProgressDialog pausedialog;
    private boolean isInitDone = false;

    private TreeMap menuListMap;
    private ArrayList<String> movie_res_indexList = null;
    private ArrayList<String> movie_res_infoList = null;

    private final static String TAG = "SettingActivity";


    private Handler eventHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            String info = msg.obj.toString();
            if (Util.isContainExactWord(info, "&")) {
                String[] cmd = info.split("&");
                switch (cmd[0]) {
                    case "100":
                        SettingActivity.this.finish();
                        break;
                    case "111":
                        record_info_recSize(Integer.parseInt(cmd[1]));
                        break;
                    case "112":
                        if (cmd[1].equals("1")) {
                            if (switch_record.isChecked()) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        final String ack3 = NVTKitModel.autoTestDone();
                                    }
                                }).start();
                            } else {
                                switch_record.setChecked(true);
                            }
                        } else if (cmd[1].equals("0")) {
                            if (!switch_record.isChecked()) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        final String ack3 = NVTKitModel.autoTestDone();
                                    }
                                }).start();
                            } else {
                                switch_record.setChecked(false);
                            }
                        }
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
            }
        }

        ;
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_activity_settings);
        setSupportActionBar(toolbar);
        initView();
    }


    @Override
    protected void onResume() {
        super.onResume();

        NVTKitModel.setWifiEventListener(eventHandler);
//        if(getRequestedOrientation()!= ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        }
        setLoading(true);
        initData();
        setDeviceStatus();
        initListener();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void initView() {
        device_name = (TextView) findViewById(R.id.device_name);
        device_pwd = (TextView) findViewById(R.id.device_pwd);
        tv_record_quality = (TextView) findViewById(R.id.tv_record_quality);
        format = (TextView) findViewById(R.id.format);
        //advanced_settings = (TextView) findViewById(R.id.advanced_settings);
        tv_info = (TextView) findViewById(R.id.tv_info);
        switch_record = (Switch) findViewById(R.id.switch_record);

        record_time = (TextView) findViewById(R.id.record_time);
        record_time.setOnClickListener(this);
        findViewById(R.id.btn_reset).setOnClickListener(this);

        pausedialog = new ProgressDialog(SettingActivity.this);
        pausedialog.setTitle(getString(R.string.dialog_title));
        pausedialog.setCancelable(false);
        pausedialog.setCanceledOnTouchOutside(false);

    }

    //加载数据
    private void initData() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String ssid = wifiManager.getConnectionInfo().getSSID();
        device_name.setText(ssid.substring(1, ssid.length() - 1));

        new Thread(new Runnable() {
            @Override
            public void run() {
                TreeMap itemMap = NVTKitModel.qryMenuItemList();
                if (itemMap != null) {
                    Set keys = itemMap.keySet();
                    for (Object key1 : keys) {
                        String key = (String) key1;
                        menuListMap = (TreeMap) itemMap.get(key);
                        final Set menuListKey = menuListMap.keySet();
                        for (Object aMenuListKey : menuListKey) {
                            final String menuListKeys = (String) aMenuListKey;
                            String menuListValue = (String) menuListMap.get(menuListKeys);
                            if (key.equals(DefineTable.WIFIAPP_CMD_CYCLIC_REC)) {
                                switch (menuListValue) {
                                    case "OFF":
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                record_time.setText(getString(R.string.off));
                                            }
                                        });
                                        break;
                                    case "3MIN":
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                record_time.setText(getString(R.string.min3));
                                            }
                                        });
                                        break;
                                    case "5MIN":
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                record_time.setText(getString(R.string.min5));
                                            }
                                        });
                                        break;
                                    case "10MIN":
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                record_time.setText(getString(R.string.min10));
                                            }
                                        });
                                        break;
                                }
                            }
                        }
                    }
                }
            }
        }).start();

    }

    private void initListener() {
        device_pwd.setOnClickListener(this);
        tv_record_quality.setOnClickListener(this);
        format.setOnClickListener(this);
        // advanced_settings.setOnClickListener(this);
        tv_info.setOnClickListener(this);
        switch_record.setOnCheckedChangeListener(this);
    }


    //点击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.device_pwd://更改密码
                final AlertDialog dialogView;
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                final View view = LayoutInflater.from(this).inflate(R.layout.change_pwd, null);
                builder.setView(view);
                dialogView = builder.create();
                dialogView.show();
                final EditText et_pwd1 = (EditText) view.findViewById(R.id.et_pwd1);
                final EditText et_pwd2 = (EditText) view.findViewById(R.id.et_pwd2);
                view.findViewById(R.id.btn_config_pwd).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String et1 = et_pwd1.getText().toString();
                        if (et1.length() < 8) {
                            Toast.makeText(SettingActivity.this, getString(R.string.pwd_edit2), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (!StringUtils.isPassword(et1)) {
                            Toast.makeText(SettingActivity.this, getString(R.string.pwd_edit), Toast.LENGTH_SHORT).show();
                            return;
                        }
                        //^[a-zA-Z]\w{5,17}$
//                        if (!StringUtils.isPWD(et_pwd1.getText().toString())) {
//                            Toast.makeText(SettingActivity.this, getString(R.string.pwd_edit), Toast.LENGTH_SHORT).show();
//                            return;
//                        }
                        if (et_pwd1.getText().toString().equals(et_pwd2.getText().toString())) {
                            changePWDDialog(true);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    final String result = NVTKitModel.netSetPassword("" + et_pwd1.getText());
                                    NVTKitModel.netReConnect();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (result != null) {
                                                //checkDevDialog();
                                                changePWDDialog(false);
                                                Toast.makeText(SettingActivity.this, getString(R.string.success_change_pwd),
                                                        Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent();
                                                intent.setAction(Settings.ACTION_WIFI_SETTINGS);
                                                startActivity(intent);
                                                SettingActivity.this.finish();
                                            } else {
                                                Toast.makeText(SettingActivity.this, getString(R.string.failed_change_pwd),
                                                        Toast.LENGTH_SHORT).show();
                                                changePWDDialog(false);
                                            }
                                        }
                                    });
                                }
                            }).start();
                        } else {
                            Toast.makeText(SettingActivity.this, getString(R.string.pwd_same), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                });
                view.findViewById(R.id.btn_cancel_pwd).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogView.dismiss();
                    }
                });
                break;
            case R.id.tv_record_quality://录制质量设置
                if (movie_res_infoList != null) {
                    String[] list = movie_res_infoList.toArray(new String[0]);
                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setSingleChoiceItems(list, -1, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, final int which) {
                                    setLoading(true);
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            final String ack = NVTKitModel.setMovieRecordSize(movie_res_indexList.get(which));
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    setLoading(false);
                                                    if (ack != null) {
                                                        tv_record_quality.setText(movie_res_infoList.get(which));
                                                    }
                                                }
                                            });
                                        }
                                    }).start();
                                    dialog.dismiss();
                                }
                            }).create();
                    dialog.show();
                }
                break;
            case R.id.format://格式化操作
                AlertDialog.Builder b = new AlertDialog.Builder(this);
                b.setMessage(getString(R.string.format_config));
                b.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setLoading(true);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                final String result = NVTKitModel.devFormatStorage("1");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (result != null) {
                                            Toast.makeText(SettingActivity.this,
                                                    getString(R.string.format), Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(SettingActivity.this,
                                                    getString(R.string.format_failed), Toast.LENGTH_SHORT).show();
                                        }
                                        setLoading(false);
                                    }
                                });

                            }
                        }).start();

                    }
                }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                b.create().show();
                break;
//            case R.id.advanced_settings://高级设置
//                startActivity(new Intent(SettingActivity.this, AdvancedActivity.class));
//                break;
            case R.id.tv_info://摄像机信息
                startActivity(new Intent(SettingActivity.this, InfoActivity.class));
                break;


            case R.id.btn_reset: //重置

                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setMessage(getString(R.string.reset_config))
                        .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        final String result = NVTKitModel.devSysReset();
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (result != null) {
                                                    Toast.makeText(SettingActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                } else {
                                                    Toast.makeText(SettingActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                }).start();

                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                builder1.create().show();
                break;
            case R.id.record_time://录制时长


                final String[] timeList = new String[]{getString(R.string.off), getString(R.string.min3),
                        getString(R.string.min5), getString(R.string.min10)};
                final String[] timeListValue = new String[]{"00", "01", "02", "03"};
                AlertDialog timeDialog = new AlertDialog.Builder(this)
                        .setSingleChoiceItems(timeList, -1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, final int which) {
                                setLoading(true);
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        final String ack = NVTKitModel.setMovieCyclicRec(timeListValue[which]);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                setLoading(false);
                                                if (ack != null) {
                                                    record_time.setText(timeList[which]);
                                                }
                                            }
                                        });
                                    }
                                }).start();
                                dialog.dismiss();
                            }
                        }).create();
                timeDialog.show();

                break;
        }

    }

    private void changePWDDialog(final boolean isShow) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pausedialog.setTitle(R.string.dialog_title2);
                pausedialog.setMessage(getString(R.string.reconnect_device2));
                if (isShow) {
                    pausedialog.show();
                } else {
                    pausedialog.dismiss();
                }

            }
        });
    }

    private void checkDevDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pausedialog.setMessage(getString(R.string.reconnect_device));
                pausedialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.try_again),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String result = NVTKitModel.devHeartBeat();
                                        if (result != null) {
                                            NVTKitModel.resetWifiEventListener();
                                            Log.e(TAG, "success");
                                            pausedialog.dismiss();
                                        } else {
                                            checkDevDialog();
                                        }
                                    }
                                }).start();
                            }
                        });
                pausedialog.show();
            }
        });
    }

    //自动录像开关
    @Override
    public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                String result;
                if (!isChecked) {
                    result = NVTKitModel.setMovieRecOnConnect(false);
                    isAutoRecord = false;
                } else {
                    result = NVTKitModel.setMovieRecOnConnect(true);
                    isAutoRecord = true;
                }
                if (result == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SettingActivity.this,
                                    getString(R.string.failed_set_switch), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();

    }


    private void setLoading(final boolean isOpen) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isOpen) {
                    if (!isLoading) {
                        if (!SettingActivity.this.isFinishing()) {
                            pausedialog.show();
                            isLoading = true;
                        }
                    }
                } else {
                    isLoading = false;
                    if (pausedialog.isShowing())
                        pausedialog.dismiss();
                }
            }
        });
    }

    private void setDeviceStatus() {
        new Thread(new Runnable() {
            @Override
            public void run() {
//                new ProfileItem();
                final ParseResult result = NVTKitModel.qryDeviceRecSizeList();
                if (result != null) {
                    movie_res_indexList = result.getRecIndexList();
                    movie_res_infoList = result.getRecInfoList();

                    if (movie_res_indexList == null || movie_res_infoList == null) {
                        return;
                    }
                } else {
                    return;
                }
                // get device status
                Map result2 = NVTKitModel.qryDeviceStatus();
                if (result2 != null) {
                    Iterator iter = result2.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry entry = (Map.Entry) iter.next();
                        String key = (String) entry.getKey();
                        final String val = (String) entry.getValue();

                        switch (key) {
                            case DefineTable.WIFIAPP_CMD_MOVIE_REC_SIZE:
                                int i = 0;
                                while (i < movie_res_indexList.size()) {
                                    if (val.equals(movie_res_indexList.get(i))) {
                                        final int index = i;
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                tv_record_quality.setText(movie_res_infoList.get(index));
                                            }
                                        });
                                        break;
                                    }
                                    i = i + 1;
                                }
                                break;
                            case DefineTable.WIFIAPP_CMD_SET_AUTO_RECORDING:
                                if (val.equals("1")) {
                                    isAutoRecord = true;
                                }
                                break;
                        }
                    }
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switch_record.setChecked(isAutoRecord);

                    }
                });

                setLoading(false);
                isInitDone = true;
                final String ack3 = NVTKitModel.autoTestDone();
            }
        }).start();
    }

    private void record_info_recSize(final int which) {
        setLoading(true);
        final String command = movie_res_indexList.get(which);
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String ack = NVTKitModel.setMovieRecordSize(movie_res_indexList.get(which));
                setLoading(false);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_record_quality.setText(movie_res_infoList.get(which));
                    }
                });
                final String ack3 = NVTKitModel.autoTestDone();
            }
        }).start();
    }


}
