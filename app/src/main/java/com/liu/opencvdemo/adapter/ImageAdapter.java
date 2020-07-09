package com.liu.opencvdemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.liu.opencvdemo.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 功能描述：
 *
 * @author liuhongshuo
 * @date 2020-07-03
 */
public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.MyHolder> {


    private String[] mNames;
    private int mSelected = 0;

    private Context mContext;

    public ItemClickListener onItemClickListener;

    public ImageAdapter(Context context) {
        mContext = context;
        mNames = new String[]{
                "均值模糊",
                "高斯模糊",
                "中值滤波",
                "最大值滤波",
                "最小值滤波",
                "高斯双边滤波",
                "均值迁移滤波",
                "自定义滤波-模糊",
                "自定义滤波-锐化",
                "自定义滤波-梯度",
                "膨胀",
                "腐蚀",
                "开操作",
                "闭操作",
                "黑帽",
                "顶帽",
                "基本梯度",
                "自动计算阀值",
                "自适应阀值化" };
    }

    public void setData(String[] data) {
        mNames = data;
        mSelected = 0;
        notifyDataSetChanged();
    }


    public void setSelectedIndex(int index) {
        mSelected = index;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new MyHolder(LayoutInflater.from(mContext).inflate(R.layout.item_image_filter, null));
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, final int position) {


        holder.mFilterName.setText(mNames[position]);
        if (position == mSelected) {
            holder.mFilterName.setSelected(true);
        } else {
            holder.mFilterName.setSelected(false);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setSelectedIndex(position);
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(position);
                }
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return null == mNames ? 0 : mNames.length;
    }

    class MyHolder extends RecyclerView.ViewHolder {

        TextView mFilterName;

        public MyHolder(View itemView) {
            super(itemView);

            mFilterName = itemView.findViewById(R.id.tv_name);

        }
    }

    public void setOnItemClickListener(ItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(int position);
    }

}