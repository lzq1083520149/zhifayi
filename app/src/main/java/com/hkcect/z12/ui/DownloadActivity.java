package com.hkcect.z12.ui;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hkcect.z12.R;
import com.hkcect.z12.adapter.DownloadAdapter;
import com.hkcect.z12.album.ListItem;
import com.hkcect.z12.service.DownloadService;
import com.hkcect.z12.util.FileBean;
import com.hkcect.z12.util.FileDownload;
import com.hkcect.z12.utils.StringUtils;
import com.ntk.nvtkit.NVTKitModel;
import com.ntk.util.FileItem;
import com.ntk.util.ParseResult;
import com.ntk.util.Util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class DownloadActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "DownloadActivity";
    private RecyclerView recycler_download;
    private LinearLayout ll_download;
    private static DownloadAdapter adapter;
    //    private RxDownload rxDownload;
    private static ProgressDialog progressDialog;
    private static Button btn_download, btn_delete;
    private static TextView tv_null_download;
//    private ProgressDialog  mProgressDialog;

    private static class MyHandler extends Handler {
        private final WeakReference<DownloadActivity> mActivity;

        MyHandler(DownloadActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            DownloadActivity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case 0:

                        ArrayList<ListItem> list = (ArrayList<ListItem>) msg.obj;
                        adapter.setData(list);

                        btn_download.setVisibility(View.VISIBLE);
                        btn_delete.setVisibility(View.VISIBLE);
                        item.getItem(0).setVisible(true);
                        tv_null_download.setVisibility(View.GONE);

                        if (progressDialog.isShowing())
                            progressDialog.cancel();
                        break;
                    case 1:
                        ArrayList<ListItem> list2 = (ArrayList<ListItem>) msg.obj;
                        adapter.setData(list2);

                        btn_download.setVisibility(View.GONE);
                        btn_delete.setVisibility(View.GONE);
                        item.getItem(0).setVisible(false);
                        tv_null_download.setVisibility(View.VISIBLE);

                        if (progressDialog.isShowing())
                            progressDialog.cancel();
                        break;
                }
            }
        }
    }

    private final MyHandler mHandler = new MyHandler(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        initView();
        initData();
//        initDownloadProgress();
    }


    /*
    初始化组件
     */
    private void initView() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.download);
        setSupportActionBar(toolbar);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading_data));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

//        mProgressDialog = new ProgressDialog(this);
        recycler_download = (RecyclerView) findViewById(R.id.recycler_download);
        ll_download = (LinearLayout) findViewById(R.id.ll_download);
        adapter = new DownloadAdapter(this);
        recycler_download.setLayoutManager(new LinearLayoutManager(this));
        recycler_download.setAdapter(adapter);

        tv_null_download = (TextView) findViewById(R.id.tv_null_download);
        btn_download = (Button) findViewById(R.id.btn_download);
        btn_delete = (Button) findViewById(R.id.btn_delete);
        btn_download.setOnClickListener(this);
        btn_delete.setOnClickListener(this);

    }

    /*
    加载数据
     */
    private void initData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
//                final String ack = NVTKitModel.changeMode(NVTKitModel.MODE_PLAYBACK);
                ParseResult result = NVTKitModel.getFileList();
                String[] fires = new String[0];
                boolean isexists;
                File dir = new File(StringUtils.local_media_path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                if (dir.isDirectory()) {
                    fires = dir.list();
                }
                if (result != null) {
                    final ArrayList<FileItem> fileItemArrayList = result.getFileItemList();
                    ArrayList<ListItem> listMockData = new ArrayList<>();

                    if (fileItemArrayList != null) {
                        for (int i = 0; i < fileItemArrayList.size(); i++) {
                            isexists = false;
                            for (String fire : fires) {
                                if (fire.equals(fileItemArrayList.get(i).NAME)) {
                                    isexists = true;
                                }
                            }
                            if (isexists) {
                                continue;
                            }

                            ListItem newsData = new ListItem();
                            String url = fileItemArrayList.get(i).FPATH;
                            String url1 = url.replace("A:", "http://" + Util.getDeciceIP() + "");
                            String url2 = url1.replace("\\", "/");
                            newsData.setUrl(url2);
                            newsData.setName(fileItemArrayList.get(i).NAME);
                            newsData.setFpath(fileItemArrayList.get(i).FPATH);
                            newsData.setTime(fileItemArrayList.get(i).TIME);
                            newsData.setSize(fileItemArrayList.get(i).SIZE);
                            newsData.setTIMECODE(fileItemArrayList.get(i).TIMECODE);
                            listMockData.add(newsData);
                        }
                        if (listMockData.size()>0){
                            isSelectBox = true;
                            mHandler.obtainMessage(0, listMockData).sendToTarget();
                        }else {
                            isSelectBox = false;
                            mHandler.obtainMessage(1,listMockData).sendToTarget();
                        }

                    }

                }

            }
        }).start();
    }

    /*
      监听下载进度
       */
//    private void initDownloadProgress() {
//
//        ArrayList<FileItem> fileItemArrayList = adapter.getData();
//        for (int i = 0; i < fileItemArrayList.size(); i++) {
//            String url1 = fileItemArrayList.get(i).FPATH.replace("A:", "http://" + Util.getDeciceIP() + "");
//            final String url2 = url1.replace("\\", "/");
//            RxDownload.getInstance(this).receiveDownloadStatus(url2)
//                    .subscribe(new Consumer<DownloadEvent>() {
//                        @Override
//                        public void accept(DownloadEvent event) throws Exception {
//                            //当事件为Failed时, 才会有异常信息, 其余时候为null.
//                            if (event.getFlag() == DownloadFlag.FAILED) {
//                                Throwable throwable = event.getError();
//                                Log.w("Error", throwable);
//                            } else {
//                                if (event.getDownloadStatus().getTotalSize() > 0) {
//                                    final long l = event.getDownloadStatus().getDownloadSize() / (event.getDownloadStatus().getTotalSize());
//
//                                }
//                            }
//                        }
//                    });
//        }
//    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_download://下载

                if (!StringUtils.hasSpace(this)) {
                    Toast.makeText(this, getString(R.string.have_not_space),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                //先获取到选中的项目
                //对选中的item的url进行下载操作
                final ArrayList<FileDownload> urls = new ArrayList<>();
                ArrayList<ListItem> fileItemArrayList = adapter.getData();
                if (fileItemArrayList != null) {
                    for (int i = 0; i < fileItemArrayList.size(); i++) {
                        if (fileItemArrayList.get(i).isChecked) {
                            FileDownload file = new FileDownload();
                            file.setUrl(fileItemArrayList.get(i).getUrl());
                            file.setName(fileItemArrayList.get(i).getName());
                            urls.add(file);
                        }
                    }

                }
                if (urls.size() == 0) {
                    return;
                }

//                RxDownload.getInstance(this)
//                        .serviceMultiDownload("download", urls)
//                        .subscribe(new Consumer<Object>() {
//                            @Override
//                            public void accept(Object o) throws Exception {
//                                Toast.makeText(DownloadActivity.this, R.string.start_download, Toast.LENGTH_SHORT).show();
//                            }
//                        }, new Consumer<Throwable>() {
//                            @Override
//                            public void accept(Throwable throwable) throws Exception {
//                                Log.w(TAG, throwable);
//                                Toast.makeText(DownloadActivity.this, R.string.failed_add, Toast.LENGTH_SHORT).show();
//                            }
//                        }, new Action() {
//                            @Override
//                            public void run() throws Exception {
//                                initData();
//                            }
//                        });

                Intent intent = new Intent(this, DownloadService.class);
                intent.putParcelableArrayListExtra("download", urls);
                startService(intent);

                break;
            case R.id.btn_delete://删除

                final List<FileBean> urlList = new ArrayList<>();
                ArrayList<ListItem> list = adapter.getData();
                if (list != null) {
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i).isChecked) {
                            FileBean bean = new FileBean();
                            bean.setFpath(list.get(i).getFpath());
                            bean.setUrl(list.get(i).getUrl());
                            urlList.add(bean);
                        }
                    }
                } else {
                    return;
                }

                if (urlList.size() == 0) {
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.delete_config))
                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                progressDialog.setMessage(getString(R.string.deleting));
                                progressDialog.show();


                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String encodedurl = null;
                                        for (int i = 0; i < urlList.size(); i++) {
                                            try {
                                                encodedurl = java.net.URLEncoder.encode(urlList.get(i).getFpath(), "ISO-8859-1");
                                            } catch (UnsupportedEncodingException e) {
                                                e.printStackTrace();
                                            }
                                            String delete = NVTKitModel.delFileFromUrl(encodedurl);
                                        }
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                initData();
                                            }
                                        });
                                    }
                                }).start();
                            }
                        });
                builder.create().show();

                break;
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        item=menu;
        getMenuInflater().inflate(R.menu.download_menu, menu);
        return true;
    }

    private boolean isSelectBox = false;
    private static Menu item ;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.download_select_all:
                    item.setChecked(!item.isChecked());
                    item.setIcon(item.isChecked() ? R.drawable.ic_check_box_white_24dp : R.drawable.ic_check_box_outline_blank_white_24dp);
                    Log.i(TAG, "onOptionsItemSelected: " + item.isChecked());

                    if (item.isChecked()) {
                        adapter.selectAll();
                    } else {
                        adapter.removeAll();
                    }

                break;
        }
        return super.onOptionsItemSelected(item);
    }


//
//    private class DownloadReceiver extends ResultReceiver {
//        public DownloadReceiver(Handler handler) {
//            super(handler);
//        }
//        @Override
//        protected void onReceiveResult(int resultCode, Bundle resultData) {
//            super.onReceiveResult(resultCode, resultData);
//            if (resultCode == DownloadService.UPDATE_PROGRESS) {
//                int progress = resultData.getInt("progress");
//                String name  = resultData.getString("name");
//
//                mProgressDialog.setProgress(progress);
//                mProgressDialog.setMessage(name);
//                if (progress == 100) {
//                    mProgressDialog.dismiss();
//                }
//            }
//        }
//    }
}
