package com.hkcect.z12.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.hkcect.z12.R;
import com.hkcect.z12.adapter.VideoRecyclerViewAdapter;
import com.hkcect.z12.album.ListItem;
import com.hkcect.z12.util.DownloadFileFromURL;
import com.ntk.nvtkit.NVTKitModel;
import com.ntk.util.FileItem;
import com.ntk.util.Util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Random;

public class VideoFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    private static final String ARG_COLUMN_COUNT = "column-count";
    private int mColumnCount = 2;
    private OnVideoClickListener mListener;
    private ArrayList<ListItem> listItems;
    private VideoRecyclerViewAdapter mAdapter;
    private ProgressBar progressBar;//入场时出现的加载框
    private VideoSyncTask videoSyncTask;//异步加载图片的Task
    private SwipeRefreshLayout swipeRefreshLayout;//下拉刷新的，根View
    private LinearLayout ll_select;
    private ArrayList<ListItem> mSelectValues;
    private ProgressDialog pdg;
    private static final int SyncStart = 1;
    private static final int SyncDone = 2;
    private static final int SyncFail = 3;
    private static final int DeleteItem = 4;
    private static final int DeleteDone = 5;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SyncStart:
//                    Toast.makeText(getActivity(), "加载数据中……", Toast.LENGTH_SHORT).show();
                    //同步数据开始
                    //显示一个进度条
                    if (!swipeRefreshLayout.isRefreshing())
                        progressBar.setVisibility(View.VISIBLE);
                    break;
                case SyncDone://同步数据完成
                    ArrayList<FileItem> fileItems = (ArrayList<FileItem>) msg.obj;
                    if (progressBar.getVisibility() == View.VISIBLE) {
                        progressBar.setVisibility(View.GONE);
                    }
                    //同步完数据
                    listItems.clear();
                    for (int i = 0; i < fileItems.size(); i++) {
                        ListItem newsData = new ListItem();
                        if (Util.isContainExactWord(fileItems.get(i).NAME, "JPG")) {
                            continue;
                        } else {
                            String url = fileItems.get(i).FPATH;
                            String url1 = url.replace("A:", "http://" + Util.getDeciceIP() + "");
                            String url2 = url1.replace("\\", "/");
                            //Log.e("JPG", url2);
                            newsData.setUrl(url2);
                        }
                        newsData.setName(fileItems.get(i).NAME);
                        newsData.setFpath(fileItems.get(i).FPATH);
                        newsData.setTime(fileItems.get(i).TIME);
                        listItems.add(newsData);
                    }

                    //拿到同步到的图片集合 listItems 类型：ArrayList<ListItem>
                    //通知recyView更新数据，显示显示图片
                    mAdapter.notifyDataSetChanged();
                    if (swipeRefreshLayout.isRefreshing())
                        swipeRefreshLayout.setRefreshing(false);
                    break;
                case SyncFail:

                    if (progressBar.getVisibility() == View.VISIBLE) {
                        progressBar.setVisibility(View.GONE);
                    }
                    if (swipeRefreshLayout.isRefreshing())
                        swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(getActivity(), R.string.no_data_retry, Toast.LENGTH_SHORT).show();
                    break;
                case DeleteItem://删除一个item
                    ListItem listItem = (ListItem) msg.obj;
                    pdg.setMessage(getText(R.string.now_delete_item) + listItem.getName());
                    if (!pdg.isShowing())
                        pdg.show();
                    mAdapter.mValues.remove(listItem);
                    break;
                case DeleteDone://删除数据后，重新加载剩余的数据
                    if (pdg.isShowing())
                        pdg.dismiss();
                    mAdapter.cancelALLSelect();
                    mAdapter.notifyDataSetChanged();
                    ll_select.setVisibility(View.GONE);
                    break;
            }
        }
    };


    public VideoFragment() {
    }

    private  static VideoFragment inStance = null;

    public static synchronized VideoFragment newInstance(int columnCount) {
        if (inStance == null) {
            inStance = new VideoFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_COLUMN_COUNT, columnCount);
            inStance.setArguments(args);
        }
        return inStance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
        inStance = this;
        listItems= new ArrayList<>();//Adapter里的数据源
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        pdg = null;
        listItems = null;
        progressBar = null;
        videoSyncTask = null;
        mSelectValues = null;
        handler.removeMessages(SyncStart);
        handler.removeMessages(SyncDone);
        handler.removeMessages(SyncFail);
        handler.removeMessages(DeleteItem);
        handler.removeMessages(DeleteDone);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
        ll_select = (LinearLayout) view.findViewById(R.id.ll_select);
        view.findViewById(R.id.select_all).setOnClickListener(this);
        view.findViewById(R.id.cancel_all).setOnClickListener(this);
        view.findViewById(R.id.download_select).setOnClickListener(this);
        view.findViewById(R.id.delete_select).setOnClickListener(this);
        mSelectValues = new ArrayList<>();
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        progressBar = (ProgressBar) view.findViewById(R.id.pb);
        pdg = new ProgressDialog(getContext());
        pdg.setCancelable(false);
        pdg.setCanceledOnTouchOutside(false);
        Context context = view.getContext();
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        mAdapter = new VideoRecyclerViewAdapter(getActivity(), listItems, mListener, recyclerView);
        recyclerView.setAdapter(mAdapter);
        mAdapter.setSelectionListener(new VideoRecyclerViewAdapter.SelectionListener() {
            @Override
            public void onDragSelectionChanged(ArrayList<ListItem> mValues) {
                if (mValues != null) {
                    mSelectValues.clear();
                    for (int i = 0; i < mValues.size(); i++) {
                        mSelectValues.add(mValues.get(i));
                    }
                    if (isSelected(mValues)) {
                        ll_select.setVisibility(View.VISIBLE);
                    } else {
                        ll_select.setVisibility(View.GONE);
                    }
                }
            }
        });
        initData();

        return view;
    }


    private boolean isSelected(ArrayList<ListItem> mValues) {
        for (int i = 0; i < mValues.size(); i++) {
            if (mValues.get(i).isChecked) {
                return true;
            }
        }
        return false;
    }


    //同步图片数据
    public void initData() {
        videoSyncTask = new VideoSyncTask();
        videoSyncTask.execute();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnVideoClickListener) {
            mListener = (OnVideoClickListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void SyncStart() {
        handler.obtainMessage(SyncStart).sendToTarget();
    }

    public void SyncDone(ArrayList<FileItem> fileItems) {
        handler.obtainMessage(SyncDone, fileItems).sendToTarget();
    }


    public void SyncFail() {
        handler.obtainMessage(SyncFail).sendToTarget();
    }

    //下拉刷新监听
    @Override
    public void onRefresh() {
        initData();
    }


    /*
    点击事件
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.select_all://选择全部
                mAdapter.setAllSelect();
                break;
            case R.id.cancel_all://取消选择
                mAdapter.cancelALLSelect();
                break;
            case R.id.download_select://下载所择
                for (int i = 0; i < mSelectValues.size(); i++) {
                    if (mSelectValues.get(i).isChecked && !mSelectValues.get(i).isDownload) {
                        new DownloadFileFromURL().execute(mSelectValues.get(i).getUrl(), mSelectValues.get(i).getName());
                    }
                }
                Toast.makeText(getContext(), R.string.begain_download, Toast.LENGTH_SHORT).show();
                break;
            case R.id.delete_select://删除所选
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < mSelectValues.size(); i++) {
                            if (mSelectValues.get(i).isChecked) {
                                String encodedurl = null;
                                try {
                                    encodedurl = java.net.URLEncoder.encode(mSelectValues.get(i).getFpath().toString(), "ISO-8859-1");
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                                String ack = NVTKitModel.delFileFromUrl(encodedurl);
                                if (ack != null && ack.equals("success")){
                                    Message msg = Message.obtain();
                                    msg.what = DeleteItem;
                                    msg.obj = mSelectValues.get(i);
                                    handler.sendMessage(msg);
                                }
                            }
                        }
                        handler.obtainMessage(DeleteDone).sendToTarget();
                    }
                }).start();
                Toast.makeText(getContext(), R.string.begain_delete, Toast.LENGTH_SHORT).show();
                break;
        }
    }


    public interface OnVideoClickListener {
        void OnVideoClick(ListItem item, int id, ArrayList<ListItem> photoList);
    }


    class VideoSyncTask extends AsyncTask<Void, Void, ArrayList<FileItem>> {


        @Override
        protected void onCancelled() {
            super.onCancelled();
            SyncFail();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            SyncStart();
        }

        @Override
        protected ArrayList<FileItem> doInBackground(Void... params) {
            if (!isCancelled()) {
                final String ack = NVTKitModel.changeMode(NVTKitModel.MODE_PLAYBACK);
                com.ntk.util.ParseResult result = NVTKitModel.getFileList();
                if (result != null && result.getFileItemList() != null) {
                    return result.getFileItemList();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<FileItem> fileItems) {
            super.onPostExecute(fileItems);
            if (fileItems == null){
                SyncFail();
            }else {
                SyncDone(fileItems);
            }
        }
    }
}
