package com.hkcect.z12.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hkcect.z12.R;
import com.hkcect.z12.example.PlaybackActivity;
import com.hkcect.z12.ui.PhotoViewActivity;
import com.hkcect.z12.utils.StringUtils;

import java.io.File;
import java.util.ArrayList;

import static android.provider.MediaStore.Video.Thumbnails.MINI_KIND;

/**
 * Created by Administrator on 2017/8/15.
 */

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.ViewHolder> {
    private File file;
    private Context context;
    private ArrayList<String> photoList;
    private ArrayList<Bitmap> videoMaplist;
    private ArrayList<String> videoList;
    private String type;
        public FileListAdapter(Context context, String type, ArrayList<String> photoList, ArrayList<String> videoList, ArrayList<Bitmap> videoMaplist) {
            super();
            this.context = context;
            this.type = type;
            this.photoList = photoList;
            this.videoList = videoList;
            this.videoMaplist = videoMaplist;
        }

        @Override
        public FileListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(context)
                    .inflate(R.layout.content_file_item, parent, false));
        }

        @Override
        public void onBindViewHolder(FileListAdapter.ViewHolder holder, final int position) {

            if (type.equals("video")) {
                 file= new File(videoList.get(position));
                holder.tv_download_name.setText(file.getName());
                holder.tv_download_size.setText(StringUtils.byte2FitMemorySize(file.length()));
                holder.tv_download_time.setText(StringUtils.fromatDate(file.lastModified()));
//                 bitmap = ThumbnailUtils.createVideoThumbnail(videoList.get(position), MINI_KIND);
//                //extractThumbnail 方法二次处理,以指定的大小提取居中的图片,获取最终我们想要的图片
//                bitmap = ThumbnailUtils.extractThumbnail(bitmap, 300, 300, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);

                if (videoMaplist!=null){
                    if (videoMaplist.size()>0){
                        holder.iv_download_item.setImageBitmap(videoMaplist.get(position));
                    }
                }

                file=null;
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        context.startActivity(new Intent(context, PlaybackActivity.class).putExtra("url",
                                videoList.get(position)));
                    }
                });

            } else if (type.equals("photo")) {

                 file = new File(photoList.get(position));
                holder.tv_download_name.setText(file.getName());
                holder.tv_download_size.setText(StringUtils.byte2FitMemorySize(file.length()));
                holder.tv_download_time.setText(StringUtils.fromatDate(file.lastModified()));
                Glide.with(context).load(photoList.get(position))
                        .into(holder.iv_download_item);
                file = null;
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putStringArrayList("photoList", photoList);
                        bundle.putInt("position", position);
                        context.startActivity(new Intent(context, PhotoViewActivity.class).putExtras(bundle));
                    }
                });

            }

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    //File file = null;
                    if (type.equals("photo")) {
                        file = new File(photoList.get(position));
                    } else {
                        file = new File(videoList.get(position));
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(context.getString(R.string.delete_or_not) + file.getName());
                    final File finalFile = file;
                    builder.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            boolean isDelete = finalFile.delete();
                            dialog.dismiss();
                            if (isDelete) {
                                if (type.equals("photo")) {
                                    photoList.remove(position);
                                } else if (type.equals("video")) {
                                    videoList.remove(position);
                                }
//                                notifyItemRemoved(position);
                                notifyDataSetChanged();
                            }
                        }
                    }).setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder.create().show();
                    return true;
                }
            });


        }

        @Override
        public int getItemCount() {
            if (type.equals("video")) {
                return videoList.size();
            } else if (type.equals("photo")) {
                return photoList.size();
            }
            return 0;
        }


      static   class ViewHolder extends RecyclerView.ViewHolder {
            TextView tv_download_name;
            TextView tv_download_time;
            TextView tv_download_size;
            ImageView iv_download_item;

            public ViewHolder(View itemView) {
                super(itemView);
                tv_download_name = (TextView) itemView.findViewById(R.id.tv_download_name);
                tv_download_time = (TextView) itemView.findViewById(R.id.tv_download_time);
                tv_download_size = (TextView) itemView.findViewById(R.id.tv_download_size);
                iv_download_item = (ImageView) itemView.findViewById(R.id.iv_download_item);
            }

        }
    }

