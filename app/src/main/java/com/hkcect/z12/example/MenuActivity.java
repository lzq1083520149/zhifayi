package com.hkcect.z12.example;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import com.hkcect.z12.R;
import com.ntk.nvtkit.NVTKitModel;
import com.hkcect.z12.util.ClientScanResult;
import com.hkcect.z12.util.DefineTable;
import com.hkcect.z12.util.FinishScanListener;
import com.ntk.util.ParseResult;
import com.hkcect.z12.util.ProfileItem;
import com.ntk.util.Util;
import com.hkcect.z12.util.WifiAPUtil;
import com.hkcect.z12.util.WifiAPUtil.WIFI_AP_STATE;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;

public class MenuActivity extends Activity {

	private final static String TAG = "MenuActivity";

	private boolean isInitDone = false;

	private boolean isAutoRecord = false;
	private boolean isMotionDetect = false;
	private boolean isAudio = false;
	private boolean isTime = false;
	private boolean isWDR = false;

	private int cyclicRecord;
	private int powerOff;
	private int gSensor;
	private int tvFormat;
	private boolean isSupGsensor = true;
	private boolean isTvFormat = true;

	private ArrayList<String> movie_res_indexList;
	private ArrayList<String> movie_res_infoList;

	private TextView textView_camera_info_SSID;
	private TextView textView_camera_info_pwd;
	private TextView textView_record_info_recSize;
	private TextView textView_adv_setting_auto_shotdown;
	private TextView textView_record_info_cyclicRecord;
	private TextView textView_adv_setting_Gsensor;
	private TextView textView_adv_setting_TVformat;
	private TextView textView_photo_info_photoSize;
	private TextView textView_card_format;
	private CheckBox checkBox_autoRecord;
	private CheckBox CheckBox_motionDetect;
	private CheckBox CheckBox_audio;
	private CheckBox CheckBox_time;
	private CheckBox CheckBox_WDR;
	private TextView textView_AP_switch_button;
	private Button button_system_reset;
	private TextView textView_network_cache;

	private WifiAPUtil mWifiAPUtil;
	private String device_mac;

	private ProgressDialog pausedialog;
	private boolean isLoading = false;

	private ProgressDialog psDialog;

	private Handler eventHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			String info = msg.obj.toString();
			if (Util.isContainExactWord(info, "&")) {
				String[] cmd = info.split("&");
				switch (cmd[0]) {
					case "100":
						MenuActivity.this.finish();
						break;
					case "101":
						Log.e(TAG, "Auto test SSID not implemented");
						break;
					case "102":
						Log.e(TAG, "Auto test PWD not implemented");
						break;
					case "111":
						record_info_recSize(Integer.parseInt(cmd[1]));
						break;
					case "112":
						if(cmd[1].equals("1")) {
							if(checkBox_autoRecord.isChecked()) {
								new Thread(new Runnable() {
									@Override
									public void run() {
										final String ack3 = NVTKitModel.autoTestDone();
									}
								}).start();
							} else {
								checkBox_autoRecord.setChecked(true);
							}
						}
						else if(cmd[1].equals("0")) {
							if(!checkBox_autoRecord.isChecked()) {
								new Thread(new Runnable() {
									@Override
									public void run() {
										final String ack3 = NVTKitModel.autoTestDone();
									}
								}).start();
							} else {
								checkBox_autoRecord.setChecked(false);
							}
						}
						break;

					case "113":
						if(cmd[1].equals("1")) {
							if(CheckBox_motionDetect.isChecked()) {
								new Thread(new Runnable() {
									@Override
									public void run() {
										final String ack3 = NVTKitModel.autoTestDone();
									}
								}).start();
							} else {
								CheckBox_motionDetect.setChecked(true);
							}
						}
						else if(cmd[1].equals("0")) {
							if(!CheckBox_motionDetect.isChecked()) {
								new Thread(new Runnable() {
									@Override
									public void run() {
										final String ack3 = NVTKitModel.autoTestDone();
									}
								}).start();
							} else {
								CheckBox_motionDetect.setChecked(false);
							}
						}
						break;
					case "114":
						if(cmd[1].equals("1")) {
							if(CheckBox_audio.isChecked()) {
								new Thread(new Runnable() {
									@Override
									public void run() {
										final String ack3 = NVTKitModel.autoTestDone();
									}
								}).start();
							} else {
								CheckBox_audio.setChecked(true);
							}
						}
						else if(cmd[1].equals("0")) {
							if(!CheckBox_audio.isChecked()) {
								new Thread(new Runnable() {
									@Override
									public void run() {
										final String ack3 = NVTKitModel.autoTestDone();
									}
								}).start();
							} else {
								CheckBox_audio.setChecked(false);
							}
						}
						break;
					case "115":
						if(cmd[1].equals("1")) {
							if(CheckBox_time.isChecked()) {
								new Thread(new Runnable() {
									@Override
									public void run() {
										final String ack3 = NVTKitModel.autoTestDone();
									}
								}).start();
							} else {
								CheckBox_time.setChecked(true);
							}
						}
						else if(cmd[1].equals("0")) {
							if(!CheckBox_time.isChecked()) {
								new Thread(new Runnable() {
									@Override
									public void run() {
										final String ack3 = NVTKitModel.autoTestDone();
									}
								}).start();
							} else {
								CheckBox_time.setChecked(false);
							}
						}
						break;
					case "130":
						photo_info_photoSize(Integer.parseInt(cmd[1]));
						break;

					case "140":
						if(cmd[1].equals("1")) {
							if(CheckBox_WDR.isChecked()) {
								new Thread(new Runnable() {
									@Override
									public void run() {
										final String ack3 = NVTKitModel.autoTestDone();
									}
								}).start();
							} else {
								CheckBox_WDR.setChecked(true);
							}
						}
						else if(cmd[1].equals("0")) {
							if(!CheckBox_WDR.isChecked()) {
								new Thread(new Runnable() {
									@Override
									public void run() {
										final String ack3 = NVTKitModel.autoTestDone();
									}
								}).start();
							} else {
								CheckBox_WDR.setChecked(false);
							}
						}
						break;

					case "150":
						card_format();
						break;

					case "160":
						adv_setting_auto_shotdown(Integer.parseInt(cmd[1]));
						break;

					case "161":
						adv_setting_Gsensor(Integer.parseInt(cmd[1]));
						break;

					case "162":
						adv_setting_TVformat(Integer.parseInt(cmd[1]));
						break;

					case "170":
						system_reset();
						break;

					case "180":
						system_reset();
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
		};
	};


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);

		psDialog = new ProgressDialog(MenuActivity.this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		NVTKitModel.setWifiEventListener(eventHandler);

		mWifiAPUtil = new WifiAPUtil(MenuActivity.this);

		initUI();
		setLoading(true);
		setDeviceStatus();
	}

	@Override
	protected void onPause() {
		super.onPause();
		NVTKitModel.stopWifiEventListener();

		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				NVTKitModel.devSaveAllSettings();
			}
		});
		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		setLoading(false);
	}

	private void initUI() {

		checkBox_autoRecord = (CheckBox) findViewById(R.id.checkBox_autoRecord);
		CheckBox_motionDetect = (CheckBox) findViewById(R.id.CheckBox_motionDetect);
		CheckBox_audio = (CheckBox) findViewById(R.id.CheckBox_audio);
		CheckBox_time = (CheckBox) findViewById(R.id.CheckBox_time);
		CheckBox_WDR = (CheckBox) findViewById(R.id.CheckBox_WDR);

		textView_camera_info_SSID = (TextView) findViewById(R.id.textView_camera_info_SSID);
		textView_camera_info_SSID.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				AlertDialog.Builder editDialog = new AlertDialog.Builder(MenuActivity.this);
				editDialog.setTitle("--- Set New SSID ---");

				final EditText editText = new EditText(MenuActivity.this);
				editText.setText(textView_camera_info_SSID.getText());
				editDialog.setView(editText);

				editDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						setLoading(true);
						new Thread(new Runnable() {
							@Override
							public void run() {
								NVTKitModel.removeWifiEventListener();
								String result = NVTKitModel.netSetSSID(editText.getText().toString());
								if (result == null) {
									Log.e(TAG, "set_ssid fail");
								}
								try {
									Thread.sleep(6000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								setLoading(false);
								checkDevDialog();
							}
						}).start();
					}
				});

				editDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {

					}
				});
				editDialog.show();
			}
		});

		textView_camera_info_pwd = (TextView) findViewById(R.id.textView_camera_info_pwd);
		textView_camera_info_pwd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				AlertDialog.Builder editDialog = new AlertDialog.Builder(MenuActivity.this);
				editDialog.setTitle("--- Set New Password ---");

				final EditText editText = new EditText(MenuActivity.this);
				editText.setText("");
				editDialog.setView(editText);

				editDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						setLoading(true);
						NVTKitModel.removeWifiEventListener();
						new Thread(new Runnable() {
							@Override
							public void run() {
								String result = NVTKitModel.netSetPassword("" + editText.getText());
								if (result == null) {
									Log.e(TAG, "set_passphrase fail");
								}
								try {
									Thread.sleep(6000);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								setLoading(false);
								checkDevDialog();
							}
						}).start();
					}
				});

				editDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {

					}
				});
				editDialog.show();
			}
		});


		checkBox_autoRecord.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isInitDone) {
					setLoading(true);
					new Thread(new Runnable() {
						@Override
						public void run() {
							String result;
							if (isAutoRecord) {
								result = NVTKitModel.setMovieRecOnConnect(false);
								isAutoRecord = false;
							} else {
								result = NVTKitModel.setMovieRecOnConnect(true);
								isAutoRecord = true;
							}
							if (result == null) {
								Log.e(TAG, "set_auto_recording fail");
							}
							setLoading(false);
							final String ack3 = NVTKitModel.autoTestDone();
						}
					}).start();
				}
			}
		});


		CheckBox_motionDetect.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isInitDone) {
					setLoading(true);
					//Log.e(TAG, "CheckBox_motionDetect  " + isChecked + "");
					new Thread(new Runnable() {
						@Override
						public void run() {
							String result;
							if (isMotionDetect) {
								result = NVTKitModel.setMovieMotionDet(false);
								isMotionDetect = false;
							} else {
								result = NVTKitModel.setMovieMotionDet(true);
								isMotionDetect = true;
							}
							if (result == null) {
								Log.e(TAG, "motion_detect fail");
							}
							setLoading(false);
							final String ack3 = NVTKitModel.autoTestDone();
						}
					}).start();
				}
			}
		});


		CheckBox_audio.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isInitDone) {
					setLoading(true);
					//Log.e(TAG, "CheckBox_audio  " + isChecked + "");
					new Thread(new Runnable() {
						@Override
						public void run() {
							String result;
							if (isAudio) {
								result = NVTKitModel.setMovieAudio(false);
								isAudio = false;
							} else {
								result = NVTKitModel.setMovieAudio(true);
								isAudio = true;
							}
							if (result == null) {
								Log.e(TAG, "movie_audio fail");
							}
							setLoading(false);
							final String ack3 = NVTKitModel.autoTestDone();
						}
					}).start();
				}
			}
		});


		CheckBox_time.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isInitDone) {
					setLoading(true);
					//Log.e(TAG, "CheckBox_time  " + isChecked + "");
					new Thread(new Runnable() {
						@Override
						public void run() {
							String result;
							if (isTime) {
								result = NVTKitModel.setMovieDTOSD(false);
								isTime = false;
							} else {
								result = NVTKitModel.setMovieDTOSD(true);
								isTime = true;
							}
							if (result == null) {
								Log.e(TAG, "dateimprint fail");
							}
							setLoading(false);
							final String ack3 = NVTKitModel.autoTestDone();
						}
					}).start();
				}
			}
		});
		CheckBox_WDR.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isInitDone) {
					setLoading(true);
					//Log.e(TAG, "CheckBox_WDR  " + isChecked + "");
					new Thread(new Runnable() {
						@Override
						public void run() {
							String result;
							if (isWDR) {
								result = NVTKitModel.setMovieWDR(false);
								isWDR = false;
							} else {
								result = NVTKitModel.setMovieWDR(true);
								isWDR = true;
							}
							if (result == null) {
								Log.e(TAG, "movie_hdr fail");
							}
							setLoading(false);
							final String ack3 = NVTKitModel.autoTestDone();
						}
					}).start();
				}
			}
		});

		textView_adv_setting_auto_shotdown = (TextView) findViewById(R.id.textView_adv_setting_auto_shotdown);
		final ArrayAdapter adapter_PowerOff = new ArrayAdapter(this, android.R.layout.simple_spinner_item, ProfileItem.list_auto_power_off);
		textView_adv_setting_auto_shotdown.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				new AlertDialog.Builder(MenuActivity.this).setTitle("PowerOff").setAdapter(adapter_PowerOff, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, final int which) {
						setLoading(true);
						textView_adv_setting_auto_shotdown.setText(ProfileItem.list_auto_power_off.get(which));
						new Thread(new Runnable() {
							@Override
							public void run() {
								final String result = NVTKitModel.setPowerOffTime(ProfileItem.list_auto_power_off_index.get(which));
								if (result == null) {
									Log.e(TAG, "setPowerOffTime fail");
								}
								setLoading(false);
							}
						}).start();
						dialog.dismiss();
					}
				}).create().show();
			}
		});

		textView_record_info_cyclicRecord = (TextView) findViewById(R.id.textView_record_info_cyclicRecord);
		final ArrayAdapter adapter_CyclicRecord = new ArrayAdapter(this, android.R.layout.simple_spinner_item, ProfileItem.list_cyclic_rec);
		textView_record_info_cyclicRecord.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				new AlertDialog.Builder(MenuActivity.this).setTitle("Cyclic Record").setAdapter(adapter_CyclicRecord, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, final int which) {
						setLoading(true);
						textView_record_info_cyclicRecord.setText(ProfileItem.list_cyclic_rec.get(which));
						new Thread(new Runnable() {
							@Override
							public void run() {
								String result = NVTKitModel.setMovieCyclicRec(ProfileItem.list_cyclic_rec_index.get(which));
								if (result == null) {
									Log.e(TAG, "cyclic_rec fail");
								}
								setLoading(false);
							}
						}).start();
						dialog.dismiss();
					}
				}).create().show();
			}
		});

		textView_record_info_recSize = (TextView) findViewById(R.id.textView_record_info_recSize);
		textView_record_info_recSize.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				final ArrayAdapter adapter_TVFormat = new ArrayAdapter(MenuActivity.this,android.R.layout.simple_spinner_item, movie_res_infoList);
				new AlertDialog.Builder(MenuActivity.this).setTitle("rec size").setAdapter(adapter_TVFormat, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, final int which) {
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
										textView_record_info_recSize.setText(movie_res_infoList.get(which));
									}
								});

							}
						}).start();
						dialog.dismiss();
					}
				}).create().show();
			}
		});



		textView_photo_info_photoSize = (TextView) findViewById(R.id.textView_photo_info_photoSize);
		textView_photo_info_photoSize.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				final ArrayAdapter adapter_TVFormat = new ArrayAdapter(MenuActivity.this,android.R.layout.simple_spinner_item, ProfileItem.list_capturesize);
				new AlertDialog.Builder(MenuActivity.this).setTitle("photo size").setAdapter(adapter_TVFormat, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, final int which) {
						setLoading(true);
						new Thread(new Runnable() {
							@Override
							public void run() {
								String result = NVTKitModel.setPhotoSize(ProfileItem.list_capturesize_index.get(which));
								if (result == null) {
									Log.e(TAG, "capturesize fail");
								}
								setLoading(false);
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										textView_photo_info_photoSize.setText(ProfileItem.list_capturesize.get(which));
									}
								});
							}
						}).start();
						dialog.dismiss();
					}
				}).create().show();
			}
		});

		textView_adv_setting_Gsensor = (TextView) findViewById(R.id.textView_adv_setting_Gsensor);
		final ArrayAdapter adapter_Gsensor = new ArrayAdapter(this, android.R.layout.simple_spinner_item, ProfileItem.list_movie_gsensor_sens);
		textView_adv_setting_Gsensor.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(isSupGsensor == true){
					new AlertDialog.Builder(MenuActivity.this).setTitle("GSensor").setAdapter(adapter_Gsensor, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, final int which) {
							setLoading(true);
							textView_adv_setting_Gsensor.setText(ProfileItem.list_movie_gsensor_sens.get(which));
							new Thread(new Runnable() {
								@Override
								public void run() {
									String result = NVTKitModel.setMovieGSensorSens(ProfileItem.list_movie_gsensor_sens_index.get(which));
									if (result == null) {
										Log.e(TAG, "movie_gsensor_sens fail");
									}
									setLoading(false);
								}
							}).start();
							dialog.dismiss();
						}
					}).create().show();
				}
			}
		});

		textView_adv_setting_TVformat = (TextView) findViewById(R.id.textView_adv_setting_TVformat);
		final ArrayAdapter adapter_TVformat = new ArrayAdapter(this, android.R.layout.simple_spinner_item, ProfileItem.list_tvformat);
		textView_adv_setting_TVformat.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(isTvFormat == true) {
					new AlertDialog.Builder(MenuActivity.this).setTitle("TV Format").setAdapter(adapter_TVformat, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, final int which) {
							setLoading(true);
							textView_adv_setting_TVformat.setText(ProfileItem.list_tvformat.get(which));
							new Thread(new Runnable() {
								@Override
								public void run() {
									String result = NVTKitModel.setTVFormat(ProfileItem.list_tvformat_index.get(which));
									if (result == null) {
										Log.e(TAG, "tvformat fail");
									}
									setLoading(false);
								}
							}).start();
							dialog.dismiss();
						}
					}).create().show();
				}
			}
		});

		textView_card_format = (TextView) findViewById(R.id.textView_card_format);
		textView_card_format.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				AlertDialog.Builder editDialog = new AlertDialog.Builder(MenuActivity.this);
				editDialog.setTitle("Format SD Card");

				editDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						setLoading(true);
						new Thread(new Runnable() {
							@Override
							public void run() {
								String result = NVTKitModel.devFormatStorage("1");
								// get free space
								final String result4 = NVTKitModel.qryDiskSpace();
								Double space = Double.parseDouble(result4);
								Double space_in_kb = space / 1024;
								Double space_in_mb = space_in_kb / 1024;
								Double space_in_gb = space_in_mb / 1024;
								//Log.e(TAG, space_in_kb + " " + space_in_mb + " " + space_in_gb);
								final String final_value;
								if (space_in_gb < 0) {
									if (space_in_mb < 0) {
										final_value = new DecimalFormat("#.##").format(space_in_kb) + " KB";
									} else {
										final_value = new DecimalFormat("#.##").format(space_in_mb) + " MB";
									}
								} else {
									final_value = new DecimalFormat("#.##").format(space_in_gb) + " GB";
								}

								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										TextView textView_card_info_space = (TextView) findViewById(R.id.textView_card_info_space);
										textView_card_info_space.setText(final_value);
									}
								});
								setLoading(false);
							}
						}).start();
					}
				});

				editDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {

					}
				});
				editDialog.show();
			}
		});

		textView_AP_switch_button = (TextView) findViewById(R.id.textView_AP_switch_button);
		if (mWifiAPUtil.getWifiApState().equals(WIFI_AP_STATE.WIFI_AP_STATE_DISABLED)) {
			textView_AP_switch_button.setText("Hotspot is OFF now.");
			textView_camera_info_SSID.setVisibility(View.VISIBLE);
			textView_camera_info_pwd.setVisibility(View.VISIBLE);
		}
		else if (mWifiAPUtil.getWifiApState().equals(WIFI_AP_STATE.WIFI_AP_STATE_ENABLED)) {
			textView_AP_switch_button.setText("Hotspot is ON now.");
			textView_camera_info_SSID.setVisibility(View.GONE);
			textView_camera_info_pwd.setVisibility(View.GONE);
		}
		textView_AP_switch_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Log.e("WifiAPUtil: ", "WifiApState: " + mWifiAPUtil.getWifiApState() + "\n\n");
				Log.e("WifiAPUtil: ", "WifiApConfiguration: " + mWifiAPUtil.getWifiApSSID() + "  " + mWifiAPUtil.getWifiApPWD());

				AlertDialog.Builder editDialog = new AlertDialog.Builder(MenuActivity.this);

				if (mWifiAPUtil.getWifiApState().equals(WIFI_AP_STATE.WIFI_AP_STATE_DISABLED)) {
					editDialog.setTitle("Change to Hotspot Mode");
					editDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface arg0, int arg1) {
							setLoading(true);
							new Thread(new Runnable() {
								@Override
								public void run() {
									NVTKitModel.removeWifiEventListener();

									device_mac = mWifiAPUtil.getDeviceMac();
									//Log.e("device_mac", device_mac);

									try {
										String ack = NVTKitModel.send_hotspot_ssid_pwd(mWifiAPUtil.getWifiApSSID(), mWifiAPUtil.getWifiApPWD());
										Thread.sleep(500);
										NVTKitModel.set_station_mode(true);
										Thread.sleep(500);
										NVTKitModel.netReConnect();
									} catch (InterruptedException e1) {
										e1.printStackTrace();
									}


									try {
										Thread.sleep(200);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									mWifiAPUtil.setWifiApEnabled(null, true);
									mWifiAPUtil.checkDeviceConnect(device_mac, false, new FinishScanListener() {

										@Override
										public void onFinishScan(ArrayList<ClientScanResult> clients) {

										}

										@Override
										public void onDeviceConnect(String device_ip) {
											Util.setDeciceIP(device_ip);
											Log.e(TAG,"switch to AP mode, device new ip = " + device_ip);
											setLoading(false);
											textView_AP_switch_button.setText("Hotspot is ON now.");
											textView_camera_info_SSID.setVisibility(View.GONE);
											textView_camera_info_pwd.setVisibility(View.GONE);
											NVTKitModel.resetWifiEventListener();
										}
									});
								}
							}).start();
						}
					});

					editDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface arg0, int arg1) {

						}
					});
					editDialog.show();
				}

				else if (mWifiAPUtil.getWifiApState().equals(WIFI_AP_STATE.WIFI_AP_STATE_ENABLED)) {
					editDialog.setTitle("Change to Client Mode");
					editDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface arg0, int arg1) {
							setLoading(true);
							new Thread(new Runnable() {
								@Override
								public void run() {
									NVTKitModel.removeWifiEventListener();

									NVTKitModel.set_station_mode(false);
									try {
										Thread.sleep(500);
									} catch (InterruptedException e1) {
										e1.printStackTrace();
									}
									NVTKitModel.netReConnect();

									try {
										Thread.sleep(500);
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
									mWifiAPUtil.setWifiApEnabled(null, false);

									while(true) {
										try {
											Thread.sleep(500);
											//Log.e(TAG, "connecting to device");
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
										if(mWifiAPUtil.getDeviceMac() != null) {
											break;
										}
									}
									Util.setDeciceIP("192.168.1.254");
									setLoading(false);
									NVTKitModel.resetWifiEventListener();
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											textView_AP_switch_button.setText("Hotspot is OFF now.");
											textView_camera_info_SSID.setVisibility(View.VISIBLE);
											textView_camera_info_pwd.setVisibility(View.VISIBLE);
										}
									});

								}
							}).start();
						}
					});

					editDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface arg0, int arg1) {

						}
					});
					editDialog.show();
				}

			}
		});

		button_system_reset = (Button) findViewById(R.id.button_system_reset);
		button_system_reset.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				AlertDialog.Builder editDialog = new AlertDialog.Builder(MenuActivity.this);
				editDialog.setTitle("Reset System");

				editDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						setLoading(true);
						new Thread(new Runnable() {
							@Override
							public void run() {
								String result = NVTKitModel.devSysReset();
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										initUI();
									}
								});
								MenuActivity.this.finish();
							}
						}).start();
					}
				});

				editDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {

					}
				});
				editDialog.show();
			}
		});


		textView_network_cache = (TextView) findViewById(R.id.textView_network_cache);
		textView_network_cache.setText(NVTKitModel.getNetwork_cache() + " ms");
		textView_network_cache.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				AlertDialog.Builder editDialog = new AlertDialog.Builder(MenuActivity.this);
				editDialog.setTitle("Network Cache must > 0 and format is ms");

				final EditText editText = new EditText(MenuActivity.this);
				editText.setRawInputType(InputType.TYPE_CLASS_NUMBER);
				editDialog.setView(editText);

				editDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {
						NVTKitModel.setNetwork_cache(Integer.valueOf(editText.getText().toString()));
						textView_network_cache.setText(NVTKitModel.getNetwork_cache() + " ms");
					}
				});

				editDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface arg0, int arg1) {

					}
				});
				editDialog.show();
			}
		});

	}

	private void setDeviceStatus() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				final ParseResult result = NVTKitModel.qryDeviceRecSizeList();
				movie_res_indexList = result.getRecIndexList();
				movie_res_infoList = result.getRecInfoList();
				if (movie_res_indexList.isEmpty()) {
					Log.e(TAG, "query_movie_size fail");
				}

				// get SSID and pwd
				final Map resultq = NVTKitModel.qrySSID();

				WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService (Context.WIFI_SERVICE);
				final WifiInfo info = wifiManager.getConnectionInfo ();

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						textView_camera_info_SSID.setText(resultq.get("ssid").toString());
						textView_camera_info_SSID.setText(info.getSSID().subSequence(1, info.getSSID().length()-1));
						textView_camera_info_pwd.setText(resultq.get("passphrase").toString());
						textView_camera_info_pwd.setText("********");
					}
				});

				// hw cap
				String DeviceCapForTVOut= NVTKitModel.qryDeviceCapForTVOut();
				if((Integer.parseInt(DeviceCapForTVOut) & 0x01) == 0) {
					isTvFormat = false;
				}
				if((Integer.parseInt(DeviceCapForTVOut) & 0x10) == 0) {
					isSupGsensor = false;
				}
				int temp2 = Integer.parseInt(DeviceCapForTVOut) & 0x10;
				//Log.e("qryDeviceCapForTVOut", isTvFormat + "  " + isSupGsensor);

				// get device status
				Map result2= NVTKitModel.qryDeviceStatus();
				Iterator iter = result2.entrySet().iterator();

				while (iter.hasNext()) {
					Map.Entry entry = (Map.Entry) iter.next();
					String key = (String) entry.getKey();
					final String val = (String) entry.getValue();

					switch(key) {
						case DefineTable.WIFIAPP_CMD_CAPTURESIZE:
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									textView_photo_info_photoSize.setText(ProfileItem.list_capturesize.get(Integer.valueOf(val)));
								}
							});
							break;
						case DefineTable.WIFIAPP_CMD_MOVIE_REC_SIZE:
							int i = 0;
							while (i < movie_res_indexList.size()) {
								if (val.equals(movie_res_indexList.get(i))) {
									final int index = i;
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											textView_record_info_recSize.setText(movie_res_infoList.get(index));
										}
									});
									break;
								}
								i = i + 1;
							}
							break;

						case DefineTable.WIFIAPP_CMD_CYCLIC_REC:
							cyclicRecord = Integer.valueOf(val);
							break;
						case DefineTable.WIFIAPP_CMD_MOVIE_HDR:
							if (val.equals("1")) {
								isWDR = true;
							}
							break;
						case DefineTable.WIFIAPP_CMD_MOTION_DET:
							if (val.equals("1")) {
								isMotionDetect = true;
							}
							break;
						case DefineTable.WIFIAPP_CMD_MOVIE_AUDIO:
							if (val.equals("1")) {
								isAudio = true;
							}
							break;
						case DefineTable.WIFIAPP_CMD_DATEIMPRINT:
							if (val.equals("1")) {
								isTime = true;
							}
							break;
						case DefineTable.WIFIAPP_CMD_MOVIE_GSENSOR_SENS:
							gSensor = Integer.valueOf(val);
							break;
						case DefineTable.WIFIAPP_CMD_SET_AUTO_RECORDING:
							if (val.equals("1")) {
								isAutoRecord = true;
							}
							break;
						case DefineTable.WIFIAPP_CMD_POWEROFF:
							powerOff = Integer.valueOf(val);
							break;
						case DefineTable.WIFIAPP_CMD_TVFORMAT:
							tvFormat = Integer.valueOf(val);
							break;
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
						checkBox_autoRecord.setChecked(isAutoRecord);
						CheckBox_motionDetect.setChecked(isMotionDetect);
						CheckBox_audio.setChecked(isAudio);
						CheckBox_time.setChecked(isTime);
						CheckBox_WDR.setChecked(isWDR);

						textView_record_info_cyclicRecord.setText(ProfileItem.list_cyclic_rec.get(cyclicRecord));
						textView_adv_setting_auto_shotdown.setText(ProfileItem.list_auto_power_off.get(powerOff));
						if(!isSupGsensor) {
							textView_adv_setting_Gsensor.setText("Not support");
						}
						else {
							textView_adv_setting_Gsensor.setText(ProfileItem.list_movie_gsensor_sens.get(gSensor));
						}

						if(!isTvFormat) {
							textView_adv_setting_TVformat.setText("Not support");
						}
						else {
							textView_adv_setting_TVformat.setText(ProfileItem.list_tvformat.get(tvFormat));
						}

					}
				});

				final String result3 = NVTKitModel.qryCardStatus();
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						TextView textView_card_info_state = (TextView) findViewById(R.id.textView_card_info_state);
						switch (result3) {
							case DefineTable.NVTKitCardStatus_Removed:
								textView_card_info_state.setText("Removed");
								break;
							case DefineTable.NVTKitCardStatus_Inserted:
								textView_card_info_state.setText("Inserted");
								break;
							case DefineTable.NVTKitCardStatus_Locked:
								textView_card_info_state.setText("Locked");
								break;
							case DefineTable.NVTKitCardStatus_DiskError:
								textView_card_info_state.setText("DiskError");
								break;
							case DefineTable.NVTKitCardStatus_UnknownFormat:
								textView_card_info_state.setText("UnknownFormat");
								break;
							case DefineTable.NVTKitCardStatus_Unformatted:
								textView_card_info_state.setText("Unformatted");
								break;
							case DefineTable.NVTKitCardStatus_NotInit:
								textView_card_info_state.setText("NotInit");
								break;
							case DefineTable.NVTKitCardStatus_InitOK:
								textView_card_info_state.setText("InitOK");
								break;
							case DefineTable.NVTKitCardStatus_NumFull:
								textView_card_info_state.setText("NumFull");
								break;
						}

					}
				});

				// get free space
				final String result4 = NVTKitModel.qryDiskSpace();
				Double space = Double.parseDouble(result4);
				Double space_in_kb = space / 1024;
				Double space_in_mb = space_in_kb / 1024;
				Double space_in_gb = space_in_mb / 1024;
				//Log.e(TAG, space_in_kb + " " + space_in_mb + " " + space_in_gb);
				final String final_value;
				if (space_in_gb < 0) {
					if (space_in_mb < 0) {
						final_value = new DecimalFormat("#.##").format(space_in_kb) + " KB";
					} else {
						final_value = new DecimalFormat("#.##").format(space_in_mb) + " MB";
					}
				} else {
					final_value = new DecimalFormat("#.##").format(space_in_gb) + " GB";
				}

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						TextView textView_card_info_space = (TextView) findViewById(R.id.textView_card_info_space);
						textView_card_info_space.setText(final_value);
					}
				});

				// get version
				final String result5 = NVTKitModel.qryFWVersion();
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						TextView textView_system_FW_version = (TextView) findViewById(R.id.textView_system_FW_version);
						textView_system_FW_version.setText(result5);

						TextView textView_system_APP_version = (TextView) findViewById(R.id.textView_system_APP_version);
						try {
							textView_system_APP_version.setText("NovaCam (" + NVTKitModel.getVersion() + ")\nNVTKitModel (" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName +")");
						} catch (NameNotFoundException e) {
							e.printStackTrace();
						}

						setLoading(false);
					}
				});



				isInitDone = true;
				final String ack3 = NVTKitModel.autoTestDone();
			}
		}).start();
	}

	private void checkRecSizeList() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				final ParseResult result = NVTKitModel.qryDeviceRecSizeList();
				movie_res_indexList = result.getRecIndexList();
				movie_res_infoList = result.getRecInfoList();
				if (movie_res_indexList.isEmpty()) {
					Log.e(TAG, "query_movie_size fail");
				}
			}
		}).start();
	}
	/*
	private void setLoading(final boolean isLoading) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (isLoading == true) {
					findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
					setClickable(false);
				} else {
					findViewById(R.id.loadingPanel).setVisibility(View.GONE);
					setClickable(true);
				}
			}

		});
	}
	*/

	private void setLoading(final boolean isOpen) {
		// TODO Auto-generated method stub

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (isOpen) {
					if (!isLoading) {
						pausedialog = ProgressDialog.show(MenuActivity.this, "Processing", "Please wait...", true);
						isLoading = true;
					}
				} else {
					isLoading = false;
					pausedialog.dismiss();
				}
			}
		});
	}

	private void checkDevDialog() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				psDialog.setMessage("Please reconnect to your device!!");
				psDialog.setCancelable(false);
				psDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Try again",new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						new Thread(new Runnable() {
							@Override
							public void run() {
								String result = NVTKitModel.devHeartBeat();
								if(result != null) {
									NVTKitModel.resetWifiEventListener();
									Log.e(TAG, "success");
									psDialog.dismiss();
								} else {
									checkDevDialog();
								}

							}
						}).start();
					}
				});
				psDialog.show();
			}
		});
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
						textView_record_info_recSize.setText(movie_res_infoList.get(which));
					}
				});
				final String ack3 = NVTKitModel.autoTestDone();
			}
		}).start();
	}

	private void photo_info_photoSize(final int which) {
		setLoading(true);
		new Thread(new Runnable() {
			@Override
			public void run() {
				String result = NVTKitModel.setPhotoSize(ProfileItem.list_capturesize_index.get(which));
				if (result == null) {
					Log.e(TAG, "capturesize fail");
				}
				setLoading(false);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						textView_photo_info_photoSize.setText(ProfileItem.list_capturesize.get(which));
					}
				});
				final String ack3 = NVTKitModel.autoTestDone();
			}
		}).start();
	}


	private void card_format(){
		setLoading(true);
		new Thread(new Runnable() {
			@Override
			public void run() {
				String result = NVTKitModel.devFormatStorage("1");
				// get free space
				final String result4 = NVTKitModel.qryDiskSpace();
				Double space = Double.parseDouble(result4);
				Double space_in_kb = space / 1024;
				Double space_in_mb = space_in_kb / 1024;
				Double space_in_gb = space_in_mb / 1024;
				//Log.e(TAG, space_in_kb + " " + space_in_mb + " " + space_in_gb);
				final String final_value;
				if (space_in_gb < 0) {
					if (space_in_mb < 0) {
						final_value = new DecimalFormat("#.##").format(space_in_kb) + " KB";
					} else {
						final_value = new DecimalFormat("#.##").format(space_in_mb) + " MB";
					}
				} else {
					final_value = new DecimalFormat("#.##").format(space_in_gb) + " GB";
				}

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						TextView textView_card_info_space = (TextView) findViewById(R.id.textView_card_info_space);
						textView_card_info_space.setText(final_value);
					}
				});
				setLoading(false);
				final String ack3 = NVTKitModel.autoTestDone();
			}
		}).start();
	}

	private void adv_setting_auto_shotdown(final int which) {
		setLoading(true);
		textView_adv_setting_auto_shotdown.setText(ProfileItem.list_auto_power_off.get(which));
		new Thread(new Runnable() {
			@Override
			public void run() {
				final String result = NVTKitModel.setPowerOffTime(ProfileItem.list_auto_power_off_index.get(which));
				if (result == null) {
					Log.e(TAG, "setPowerOffTime fail");
				}
				setLoading(false);
				final String ack3 = NVTKitModel.autoTestDone();
			}
		}).start();
	}

	private void adv_setting_Gsensor(final int which) {
		//if (isSupGsensor == true) {
		setLoading(true);
		textView_adv_setting_Gsensor.setText(ProfileItem.list_movie_gsensor_sens.get(which));
		new Thread(new Runnable() {
			@Override
			public void run() {
				String result = NVTKitModel
						.setMovieGSensorSens(ProfileItem.list_movie_gsensor_sens_index.get(which));
				if (result == null) {
					Log.e(TAG, "movie_gsensor_sens fail");
				}
				setLoading(false);
				final String ack3 = NVTKitModel.autoTestDone();
			}
		}).start();
		//}
	}

	private void adv_setting_TVformat(final int which) {
		//if (isTvFormat == true) {
		setLoading(true);
		textView_adv_setting_TVformat.setText(ProfileItem.list_tvformat.get(which));
		new Thread(new Runnable() {
			@Override
			public void run() {
				String result = NVTKitModel.setTVFormat(ProfileItem.list_tvformat_index.get(which));
				if (result == null) {
					Log.e(TAG, "tvformat fail");
				}
				setLoading(false);
				final String ack3 = NVTKitModel.autoTestDone();
			}
		}).start();
		//}
	}

	private void system_reset() {
		setLoading(true);
		new Thread(new Runnable() {
			@Override
			public void run() {
				String result = NVTKitModel.devSysReset();
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						initUI();
					}
				});
				MenuActivity.this.finish();
			}
		}).start();
	}

	private void AP_switch_button() {
		if (mWifiAPUtil.getWifiApState().equals(WIFI_AP_STATE.WIFI_AP_STATE_DISABLED)) {
			setLoading(true);
			new Thread(new Runnable() {
				@Override
				public void run() {
					NVTKitModel.removeWifiEventListener();

					device_mac = mWifiAPUtil.getDeviceMac();
					//Log.e("device_mac", device_mac);

					try {
						String ack = NVTKitModel.send_hotspot_ssid_pwd(mWifiAPUtil.getWifiApSSID(), mWifiAPUtil.getWifiApPWD());
						Thread.sleep(500);
						NVTKitModel.set_station_mode(true);
						Thread.sleep(500);
						NVTKitModel.netReConnect();
						Thread.sleep(200);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					mWifiAPUtil.setWifiApEnabled(null, true);
					mWifiAPUtil.checkDeviceConnect(device_mac, false, new FinishScanListener() {
						@Override
						public void onFinishScan(ArrayList<ClientScanResult> clients) {
						}

						@Override
						public void onDeviceConnect(String device_ip) {
							// TODO Auto-generated method stub
							Util.setDeciceIP(device_ip);
							Log.e(TAG,"switch to AP mode, device new ip = " + device_ip);
							setLoading(false);
							final String ack3 = NVTKitModel.autoTestDone();
							textView_AP_switch_button.setText("Hotspot is ON now.");
							textView_camera_info_SSID.setVisibility(View.GONE);
							textView_camera_info_pwd.setVisibility(View.GONE);
							NVTKitModel.resetWifiEventListener();
						}
					});
				}
			}).start();
		}
		else if (mWifiAPUtil.getWifiApState().equals(WIFI_AP_STATE.WIFI_AP_STATE_ENABLED)) {
			setLoading(true);
			new Thread(new Runnable() {
				@Override
				public void run() {
					NVTKitModel.removeWifiEventListener();

					NVTKitModel.set_station_mode(false);
					try {
						Thread.sleep(500);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					NVTKitModel.netReConnect();

					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					mWifiAPUtil.setWifiApEnabled(null, false);

					while (true) {
						try {
							Thread.sleep(500);
							// Log.e(TAG, "connecting to device");
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						if (mWifiAPUtil.getDeviceMac() != null) {
							break;
						}
					}
					Util.setDeciceIP("192.168.1.254");
					setLoading(false);
					final String ack3 = NVTKitModel.autoTestDone();
					NVTKitModel.resetWifiEventListener();
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							textView_AP_switch_button.setText("Hotspot is OFF now.");
							textView_camera_info_SSID.setVisibility(View.VISIBLE);
							textView_camera_info_pwd.setVisibility(View.VISIBLE);
						}
					});

				}
			}).start();
		}
	}
}
