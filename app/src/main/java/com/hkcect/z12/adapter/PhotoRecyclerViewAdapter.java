package com.hkcect.z12.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hkcect.z12.R;
import com.hkcect.z12.album.ListItem;
import com.hkcect.z12.fragment.PhotoFragment;
import com.hkcect.z12.fragment.PhotoFragment.OnListFragmentInteractionListener;
import com.ntk.nvtkit.NVTKitModel;
import com.ntk.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;


public class PhotoRecyclerViewAdapter extends RecyclerView.Adapter<PhotoRecyclerViewAdapter.ViewHolder> {

    public final ArrayList<ListItem> mValues;
    private final OnListFragmentInteractionListener mListener;
    private RecyclerView recyclerView;
    private boolean loadDate = false;
    private Context mContext;
    private ArrayList<Integer> mSelectedIndices;
    private boolean firstCreate = true;


    public PhotoRecyclerViewAdapter(Context context, ArrayList<ListItem> items, OnListFragmentInteractionListener listener, RecyclerView recyclerView) {
        mValues = items;
        mListener = listener;
        this.recyclerView = recyclerView;
        mContext = context;
        mSelectedIndices = new ArrayList<>();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //滑动停止，加载图片
                    loadDate = true;
                    notifyDataSetChanged();
                } else {
                    firstCreate = false;
                    loadDate = false;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            }
        });
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_photo_items, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        holder.mItem = mValues.get(position);
        holder.mName.setText(mValues.get(position).getName());
//        holder.mImage.setTag(holder.mItem.getUrl());
        if (loadDate || firstCreate) {
            //加载缩略图
            if (Util.isContainExactWord(holder.mItem.getUrl(), "http")) {
                File f = new File(Util.local_thumbnail_path + "/" + holder.mItem.getName());
                if (!f.exists()) {
                    holder.checkBox.setVisibility(View.VISIBLE);
                    //下载缩略图
                    //不能在这进行下载操作，另起线程也不行（除非设置图片的三级缓存）。
                    //在PhotoFragment中下载全部缩略图，然后在这加载
//                    new DownLoadThumbTask().execute(holder);
//                    DownLoadThumb(holder.mImage, holder.mItem.getName(), holder.mItem.getUrl());
//                    Glide.with(holder.mView.getContext()).load(holder.mItem.getUrl()).into(holder.mImage);
                } else {
                    holder.checkBox.setVisibility(View.GONE);
                    Glide.with(mContext).load(f).into(holder.mImage);
//                    Bitmap myBitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
//                    holder.mImage.setImageBitmap(myBitmap);
                }
                File file = new File(Util.local_photo_path + "/" + holder.mItem.getName());
                mValues.get(position).isDownload = file.exists();
            } else {
                Bitmap myBitmap = BitmapFactory.decodeFile(holder.mItem.getUrl());
                holder.mImage.setImageBitmap(myBitmap);
            }
        }


        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mValues.get(holder.getAdapterPosition()).isChecked = isChecked;
                fireSelectionListener();
            }
        });
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onListFragmentInteraction(holder.mItem, holder.getAdapterPosition(), mValues);
                }
            }
        });
        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(mContext.getString(R.string.delete_photo))
                        .setMessage(mContext.getString(R.string.delete_message) + holder.mItem.getName() + "？")
                        .setNegativeButton(mContext.getText(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setPositiveButton(mContext.getText(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
//                                            final String ack = NVTKitModel.delFileFromUrl(holder.mItem.getUrl());
                                        final String ack = "success";
                                        ((Activity) mContext).runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (ack.equals("success")) {
                                                    notifyItemRemoved(holder.getAdapterPosition());
                                                    PhotoFragment.newInstance(1).initData();
                                                }
                                            }
                                        });
                                    }
                                }).start();
                                //删除图片
                                Toast.makeText(mContext, "删除操作", Toast.LENGTH_SHORT).show();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.setCancelable(true);
                dialog.setCanceledOnTouchOutside(true);
                dialog.show();
                return true;
            }
        });

        if (mValues.get(position).isChecked) {
            holder.checkBox.setChecked(true);
        } else {
            holder.checkBox.setChecked(false);
        }

    }

    private void DownLoadThumb(final ImageView mImage, final String name, final String url) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                final Bitmap bitmap = NVTKitModel.getThumbnailImageFromURL(url);
                DownloadHolder downloadHolder = new DownloadHolder();
                downloadHolder.mImage = mImage;
                downloadHolder.url = url;
                downloadHolder.bitmap = bitmap;
                handler.obtainMessage(0, downloadHolder).sendToTarget();
                //保存缩略图
                File file = new File(Util.local_thumbnail_path + "/" + name);
                FileOutputStream out;
                try {
                    out = new FileOutputStream(file);
                    if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
                        out.flush();
                        out.close();
                    }
                } catch (Exception e) {
                    file.delete();
                    e.printStackTrace();
                }
            }
        }).start();

    }


    private class DownloadHolder {
        ImageView mImage;
        String url;
        Bitmap bitmap;
    }

    private android.os.Handler handler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            DownloadHolder downloadHolder = (DownloadHolder) msg.obj;
            if (downloadHolder.mImage.getTag().toString().equals(downloadHolder.url)) {
                downloadHolder.mImage.setImageBitmap(downloadHolder.bitmap);
            }
        }
    };


    @Override
    public int getItemCount() {
        return mValues == null ? 0 : mValues.size();
    }


    public void cancelALLSelect() {
        for (int i = 0; i < mValues.size(); i++) {
            mValues.get(i).isChecked = false;
        }
        notifyDataSetChanged();
    }

    public void setAllSelect() {
        for (int i = 0; i < mValues.size(); i++) {
            mValues.get(i).isChecked = true;
        }
        notifyDataSetChanged();
    }

    private void fireSelectionListener() {
        if (mSelectionListener != null)
            mSelectionListener.onDragSelectionChanged(mValues);
    }

    public interface SelectionListener {
        void onDragSelectionChanged(ArrayList<ListItem> mValues);
    }

    private SelectionListener mSelectionListener;

    public void setSelectionListener(SelectionListener selectionListener) {
        this.mSelectionListener = selectionListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final View mView;
        private final ImageView mImage;
        private final TextView mName;
        private final CheckBox checkBox;
        private ListItem mItem;

        private ViewHolder(View view) {
            super(view);
            mView = view;
            mImage = (ImageView) view.findViewById(R.id.iv);
            mName = (TextView) view.findViewById(R.id.name);
            checkBox = (CheckBox) view.findViewById(R.id.download);
        }


    }

    private  class DownLoadThumbTask extends AsyncTask<ViewHolder,Void,DownloadHolder>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(DownloadHolder downloadHolder) {
            if (downloadHolder.mImage.getTag().toString().equals(downloadHolder.url)) {
                downloadHolder.mImage.setImageBitmap(downloadHolder.bitmap);
            }
        }

        @Override
        protected DownloadHolder doInBackground(ViewHolder... params) {
            ViewHolder v = params[0];
            final Bitmap bitmap = NVTKitModel.getThumbnailImageFromURL(v.mItem.getUrl());
            DownloadHolder downloadHolder = new DownloadHolder();
            downloadHolder.mImage = v.mImage;
            downloadHolder.url = v.mItem.getUrl();
            downloadHolder.bitmap = bitmap;
            handler.obtainMessage(0, downloadHolder).sendToTarget();
            //保存缩略图
            File file = new File(Util.local_thumbnail_path + "/" + v.mItem.getName());
            FileOutputStream out;
            try {
                out = new FileOutputStream(file);
                if (bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)) {
                    out.flush();
                    out.close();
                }
            } catch (Exception e) {
                file.delete();
                e.printStackTrace();
            }

            return downloadHolder;

        }
    }


}
