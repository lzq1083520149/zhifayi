package com.hkcect.z12.ui;

import android.app.ProgressDialog;
import android.content.Context;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hkcect.z12.R;
import com.hkcect.z12.album.ListItem;
import com.hkcect.z12.service.DownloadService;
import com.hkcect.z12.util.DownloadUtil;
import com.hkcect.z12.util.FileBean;
import com.hkcect.z12.util.FileDownload;
import com.hkcect.z12.util.NotificationsUtils;
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

import io.netopen.hotbitmapgg.library.view.RingProgressBar;


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

//        recycler_download.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//            }
//
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                super.onScrollStateChanged(recyclerView, newState);
//                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
//                //判断是当前layoutManager是否为LinearLayoutManager
//                // 只有LinearLayoutManager才有查找第一个和最后一个可见view位置的方法
//                if (layoutManager instanceof LinearLayoutManager) {
//                    LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;
//                    //获取最后一个可见view的位置
//                    last = linearManager.findLastVisibleItemPosition();
//                    //获取第一个可见view的位置
//                    first = linearManager.findFirstVisibleItemPosition();
//
//                    Log.e("item 可见范围", first + "   " + last);
//                }
//            }
//        });

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
                        if (listMockData.size() > 0) {
                            isSelectBox = true;
                            mHandler.obtainMessage(0, listMockData).sendToTarget();
                        } else {
                            isSelectBox = false;
                            mHandler.obtainMessage(1, listMockData).sendToTarget();
                        }

                    }

                }

            }
        }).start();
    }


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
                            file.setSelectPosstion(fileItemArrayList.get(i).getSelectPosstion());
                            urls.add(file);
                        }
                    }

                }
                if (urls.size() == 0) {
                    Toast.makeText(this, R.string.download, Toast.LENGTH_SHORT).show();
                    return;
                }

                startDownload(urls);

//                Intent intent = new Intent(this, DownloadService.class);
//                intent.putParcelableArrayListExtra("download", urls);
//                startService(intent);

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

    private void startDownload(final ArrayList<FileDownload> urls) {
        for (int i = 0; i < urls.size(); i++) {
            final int finalI = i;
            final int finalPossition = urls.get(finalI).getSelectPosstion();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //if (isCurrentGridViewItemVisible(finalPossition)){
                    //DownloadAdapter.ViewHold viewHolder = getViewHolder(finalPossition);
                    DownloadAdapter.ViewHold viewHolder = myViewHolderList.get(finalPossition);
                    viewHolder.rl_download.setVisibility(View.VISIBLE);
                    viewHolder.download_pb.setVisibility(View.VISIBLE);
                    //}
                }
            });
            DownloadUtil.get().download(urls.get(i).getUrl(), StringUtils.local_media_down_path, new DownloadUtil.OnDownloadListener() {
                @Override
                public void onDownloadSuccess() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // if (isCurrentGridViewItemVisible(finalPossition)){
                            //DownloadAdapter.ViewHold viewHolder = getViewHolder(finalPossition);
                            DownloadAdapter.ViewHold viewHolder = myViewHolderList.get(finalPossition);
                            viewHolder.download_pb.setVisibility(View.GONE);
                            viewHolder.cb_download_item.setChecked(false);
                            viewHolder.cb_download_item.setVisibility(View.GONE);
                            viewHolder.tv_download_state.setText(R.string.yi_xia_zai);
                            // }
                        }
                    });
                }

                @Override
                public void onDownloading(int progress) {

                    // if (isCurrentGridViewItemVisible(finalPossition)){
                    // DownloadAdapter.ViewHold viewHolder = getViewHolder(finalPossition);
                    DownloadAdapter.ViewHold viewHolder = myViewHolderList.get(finalPossition);
                    viewHolder.download_pb.setProgress(progress);
                    viewHolder.cb_download_item.setChecked(false);
                    //  }

                }

                @Override
                public void onDownloadFailed() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // if (isCurrentGridViewItemVisible(finalPossition)){
                            //DownloadAdapter.ViewHold viewHolder = getViewHolder(finalPossition);
                            DownloadAdapter.ViewHold viewHolder = myViewHolderList.get(finalPossition);
                            viewHolder.download_pb.setVisibility(View.GONE);
                            viewHolder.tv_download_state.setText("下载失败");
                            //  }
                        }
                    });
                }
            });
        }

    }

//    int last = 0;
//    int first = 0;
//
//    //判断item是否在可见范围内
//    private boolean isCurrentGridViewItemVisible(final int position) {
//        return first <= position && position <= last;
//    }
//
//    //获取某个item的holder
//    private DownloadAdapter.ViewHold getViewHolder(int position) {
//        int childPosition = position - first;
//        View view = recycler_download.getChildAt(childPosition);
//        return (DownloadAdapter.ViewHold) recycler_download.getChildViewHolder(view);
//    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        item = menu;
        getMenuInflater().inflate(R.menu.download_menu, menu);
        return true;
    }

    private boolean isSelectBox = false;
    private static Menu item;

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

    List<DownloadAdapter.ViewHold> myViewHolderList;

    public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.ViewHold> {


        private Context context;
        private ArrayList<ListItem> fileItemArrayList;

        public DownloadAdapter(Context context) {
            this.context = context;
            myViewHolderList = new ArrayList<>();
        }

        public void setData(ArrayList<ListItem> list) {
            if (fileItemArrayList == null) {
                fileItemArrayList = list;
            } else {
                fileItemArrayList.clear();
                fileItemArrayList.addAll(list);
            }
            notifyDataSetChanged();
        }


        public void selectAll() {

            if (fileItemArrayList != null) {
                for (int i = 0; i < fileItemArrayList.size(); i++) {
                    fileItemArrayList.get(i).isChecked = true;
                }
            }

            notifyDataSetChanged();
        }

        public void removeAll() {

            if (fileItemArrayList != null) {
                for (int i = 0; i < fileItemArrayList.size(); i++) {
                    fileItemArrayList.get(i).isChecked = false;
                }
            }
            notifyDataSetChanged();
        }

        public ArrayList<ListItem> getData() {
            return fileItemArrayList;
        }


        @Override
        public DownloadAdapter.ViewHold onCreateViewHolder(ViewGroup parent, int viewType) {
            return new DownloadAdapter.ViewHold(LayoutInflater.from(context).inflate(R.layout.activity_download_item, parent, false));
        }

        @Override
        public void onBindViewHolder(final DownloadAdapter.ViewHold holder, final int position) {
            final ViewHold viewHolder = (ViewHold) holder;
            //用holder绑定对应的position
            viewHolder.setDataPosition(position);
            //判断list里面是否含有该holder，没有就增加
            //因为list已经持有holder的引用，所有数据自动会改变
            if (!(myViewHolderList.contains(viewHolder))) {
                myViewHolderList.add(viewHolder);
            }
            holder.tv_download_name.setText(fileItemArrayList.get(position).getName());
            holder.cb_download_item.setChecked(fileItemArrayList.get(position).isChecked);
            holder.tv_download_size.setText(StringUtils.byte2FitMemorySize(Long.valueOf(fileItemArrayList.get(position).getSIZE())));
            holder.tv_download_time.setText(fileItemArrayList.get(position).getTime());
            holder.cb_download_item.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    fileItemArrayList.get(holder.getAdapterPosition()).isChecked = isChecked;
                    if (isChecked) {
                        fileItemArrayList.get(holder.getAdapterPosition()).setSelectPosstion(holder.getAdapterPosition());
                    }

                }
            });
            //holder.download_pb.setProgress(0);


            if (fileItemArrayList.get(position).getUrl().endsWith("JPG")) {
                Glide.with(context).load(fileItemArrayList.get(position).getUrl()).into(holder.iv_download_item);
            } else {

            }

        }


        @Override
        public int getItemCount() {
            return fileItemArrayList == null ? 0 : fileItemArrayList.size();
        }

        class ViewHold extends RecyclerView.ViewHolder {
            TextView tv_download_state;
            TextView tv_download_name;
            TextView tv_download_time;
            TextView tv_download_size;
            ImageView iv_download_item;
            //        ProgressBar pb_download;
            CheckBox cb_download_item;
            RingProgressBar download_pb;
            RelativeLayout rl_download;
            int position;

            private void setDataPosition(int position) {
                this.position = position;
            }

            public ViewHold(View itemView) {
                super(itemView);
                tv_download_state = (TextView) itemView.findViewById(R.id.tv_download_state);
                tv_download_name = (TextView) itemView.findViewById(R.id.tv_download_name);
                tv_download_time = (TextView) itemView.findViewById(R.id.tv_download_time);
                tv_download_size = (TextView) itemView.findViewById(R.id.tv_download_size);
                iv_download_item = (ImageView) itemView.findViewById(R.id.iv_download_item);
//            pb_download = (ProgressBar) itemView.findViewById(R.id.pb_download);
                cb_download_item = (CheckBox) itemView.findViewById(R.id.cb_download_item);
                download_pb = (RingProgressBar) itemView.findViewById(R.id.download_pb);
                rl_download = (RelativeLayout) itemView.findViewById(R.id.rl_download);
            }
        }
    }
}
