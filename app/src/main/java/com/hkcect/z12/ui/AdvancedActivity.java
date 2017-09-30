package com.hkcect.z12.ui;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hkcect.z12.R;
import com.hkcect.z12.util.DefineTable;
import com.ntk.nvtkit.NVTKitModel;

import java.util.Set;
import java.util.TreeMap;


public class AdvancedActivity extends BaseActivity implements View.OnClickListener {

    private TextView tv_switch;
    private TextView record_time;
    private TreeMap menuListMap;
    private boolean isLoading = false;
    private ProgressDialog pausedialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initView();
    }

    private void initView() {

        pausedialog = new ProgressDialog(this);
        pausedialog.setTitle(getString(R.string.dialog_title));
        pausedialog.setMessage(getString(R.string.dialog_wait_message));
        pausedialog.setCancelable(false);
        pausedialog.setCanceledOnTouchOutside(false);

        findViewById(R.id.btn_reset).setOnClickListener(this);
        tv_switch = (TextView) findViewById(R.id.tv_switch);
        tv_switch.setOnClickListener(this);

        record_time = (TextView) findViewById(R.id.record_time);
        record_time.setOnClickListener(this);

    }


    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    private void initData() {

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
                            switch (key) {
//                                    case DefineTable.WIFIAPP_CMD_CAPTURESIZE:
//                                        switch (menuListValue) {
//                                            case "12M":
//                                                break;
//                                            case "10M":
//                                                break;
//                                            case "8M":
//                                                break;
//                                        }
//                                        break;
//                                    case DefineTable.WIFIAPP_CMD_MOVIE_REC_SIZE:
//                                        break;
                                case DefineTable.WIFIAPP_CMD_CYCLIC_REC:
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
                                    break;
//                                    case DefineTable.WIFIAPP_CMD_MOVIE_HDR:
//                                    case DefineTable.WIFIAPP_CMD_MOVIE_EV:
//                                    case DefineTable.WIFIAPP_CMD_MOTION_DET://查询物体移动侦测设定
//
//                                    case DefineTable.WIFIAPP_CMD_MOVIE_AUDIO:
//                                    case DefineTable.WIFIAPP_CMD_DATEIMPRINT:
                                case DefineTable.WIFIAPP_CMD_MOVIE_GSENSOR_SENS://查询设备当前碰撞感应敏感度 index 与对应 value
                                    switch (menuListValue) {
//                                            menuListMap
                                        case "OFF":
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (menuListKeys.equals("OFF")){
                                                        tv_switch.setText(R.string.off);
                                                    }
                                                }
                                            });
                                            break;
                                        case "LOW":
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (menuListKey.equals("LOW")){
                                                    tv_switch.setText(R.string.low);
                                                    }
                                                }
                                            });
                                            break;
                                        case "MED":
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (menuListKey.equals("MED")){
                                                    tv_switch.setText(R.string.med);

                                                    }
                                                }
                                            });
                                            break;
                                        case "HIGH":
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    if (menuListKey.equals("HIGH")){

                                                    tv_switch.setText(R.string.high);
                                                    }
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

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_reset: //重置

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.reset_config))
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
                                                    Toast.makeText(AdvancedActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                                    finish();
                                                } else {
                                                    Toast.makeText(AdvancedActivity.this, "Failed", Toast.LENGTH_SHORT).show();
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
                builder.create().show();
                break;
            case R.id.tv_switch://碰撞感应灵敏度设置

                final String[] list = new String[]{getString(R.string.off), getString(R.string.low),
                        getString(R.string.med), getString(R.string.high)};
                final String[] listValue = new String[]{"00", "01", "02", "03"};
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setSingleChoiceItems(list, -1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, final int which) {
                                setLoading(true);
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        final String ack = NVTKitModel.setMovieGSensorSens(listValue[which]);
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                setLoading(false);
                                                if (ack != null) {
                                                    tv_switch.setText(list[which]);
                                                }
                                            }
                                        });
                                    }
                                }).start();
                                dialog.dismiss();
                            }
                        }).create();
                dialog.show();
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

    private void setLoading(final boolean isOpen) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isOpen) {
                    if (!isLoading) {
                        if (!AdvancedActivity.this.isFinishing()) {
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

}
