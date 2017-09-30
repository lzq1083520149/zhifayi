package com.hkcect.z12.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.hkcect.z12.fragment.VideoFragment;
import com.ntk.nvtkit.NVTKitModel;
import com.ntk.util.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;


public class VideoRecyclerViewAdapter extends RecyclerView.Adapter<VideoRecyclerViewAdapter.ViewHolder> {

    public final ArrayList<ListItem> mValues;
    private final VideoFragment.OnVideoClickListener mListener;
    private RecyclerView recyclerView;
    private boolean loadDate = false;
    private Context mContext;
    private ArrayList<Integer> mSelectedIndices;
    private boolean firstCreate = true;

    public VideoRecyclerViewAdapter(Context context, ArrayList<ListItem> items, VideoFragment.OnVideoClickListener listener, RecyclerView recyclerView) {
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
        holder.mImage.setTag(holder.mItem.getUrl());
        if (loadDate || firstCreate) {
            //加载缩略图
            if (Util.isContainExactWord(holder.mItem.getUrl(), "http")) {
                File f = new File(Util.local_thumbnail_path + "/" + holder.mItem.getName());
                if (!f.exists()) {
                    holder.mDownLoad.setVisibility(View.VISIBLE);
//                    Glide.with(holder.mView.getContext()).load(holder.mItem.getUrl()).into(holder.mImage);
//                    DownLoadThumb(holder.mImage, holder.mItem.getName(), holder.mItem.getUrl());

                } else {
                    holder.mDownLoad.setVisibility(View.GONE);
//                    Bitmap myBitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
//                    holder.mImage.setImageBitmap(myBitmap);
                    Glide.with(mContext).load(f).into(holder.mImage);
                }

                File file = new File(Util.local_movie_path + "/" + holder.mItem.getName());
                mValues.get(position).isDownload = file.exists();

            } else {
                Bitmap myBitmap = BitmapFactory.decodeFile(holder.mItem.getUrl());
                holder.mImage.setImageBitmap(myBitmap);
            }
        }


        holder.mDownLoad.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
                    mListener.OnVideoClick(holder.mItem, holder.getAdapterPosition(), mValues);
                }
            }
        });
        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setTitle(mContext.getString(R.string.delete_video))
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
            holder.mDownLoad.setChecked(true);
        } else {
            holder.mDownLoad.setChecked(false);
        }

    }

    private void DownLoadThumb(final ImageView mImage, final String name, final String url) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = NVTKitModel.getThumbnailImageFromURL(url);
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
        private final CheckBox mDownLoad;
        private ListItem mItem;

        private ViewHolder(View view) {
            super(view);
            mView = view;
            mImage = (ImageView) view.findViewById(R.id.iv);
            mName = (TextView) view.findViewById(R.id.name);
            mDownLoad = (CheckBox) view.findViewById(R.id.download);
        }


    }


}
