package com.hkcect.z12.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.hkcect.z12.R;
import com.hkcect.z12.album.ListItem;
import com.hkcect.z12.utils.StringUtils;
import java.util.ArrayList;

import io.netopen.hotbitmapgg.library.view.RingProgressBar;


public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.ViewHold> {


    private Context context;
    private ArrayList<ListItem> fileItemArrayList;

    public DownloadAdapter(Context context) {
        this.context = context;
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
    public ViewHold onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHold(LayoutInflater.from(context).inflate(R.layout.activity_download_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHold holder, final int position) {

        holder.tv_download_name.setText(fileItemArrayList.get(position).getName());
        holder.cb_download_item.setChecked(fileItemArrayList.get(position).isChecked);
        holder.tv_download_size.setText(StringUtils.byte2FitMemorySize(Long.valueOf(fileItemArrayList.get(position).getSIZE())));
        holder.tv_download_time.setText(fileItemArrayList.get(position).getTime());
        holder.cb_download_item.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                fileItemArrayList.get(holder.getAdapterPosition()).isChecked = isChecked;
            }
        });
        holder.download_pb.setProgress(0);


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
        }
    }
}

