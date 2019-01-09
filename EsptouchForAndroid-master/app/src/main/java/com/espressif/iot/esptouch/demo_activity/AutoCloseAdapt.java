package com.espressif.iot.esptouch.demo_activity;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.espressif.iot.esptouch.demo_activity.entity.AutoSwitchSetEntity;
import com.espressif.iot_esptouch_demo.R;

import java.util.List;

public class AutoCloseAdapt extends RecyclerView.Adapter<AutoCloseAdapt.ViewHolder> {
    private OnItemClickListener onItemClickListener;
    private Context context;
    private List<AutoSwitchSetEntity> mValues;

    public void setmValues( List<AutoSwitchSetEntity> mValues ) {
        this.mValues = mValues;
    }

    public AutoCloseAdapt( List<AutoSwitchSetEntity> items, Context context ) {
        this.mValues = items;
        this.context = context;
    }

    @NonNull
    @Override
    public AutoCloseAdapt.ViewHolder onCreateViewHolder( @NonNull ViewGroup parent, int viewType ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.auto_task_list, parent, false);
        return new AutoCloseAdapt.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder( @NonNull final AutoCloseAdapt.ViewHolder holder, final int position ) {
        holder.taskIdView.setText(mValues.get(position).getId());
        holder.switchNameView.setText(mValues.get(position).getSwitchName());
        holder.delayTimeView.setText(mValues.get(position).getDelayTime());
        if (mValues.get(position).getTaskStatus().equals("0")) {
            holder.taskStatusView.setImageResource(R.drawable.task_close);
        } else {
            holder.taskStatusView.setImageResource(R.drawable.task_open);
        }
        holder.taskStatusView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                if (onItemClickListener != null) {
                    //存储在临时文件中的id
                    int taskId = new Integer(mValues.get(position).getId());
                    String delayTime = mValues.get(position).getDelayTime();
                    onItemClickListener.onItemClick(holder.taskStatusView, taskId, delayTime.substring(0, delayTime.length() - 1), mValues.get(position).getTaskStatus());
                }
            }
        });
        holder.taskItemDelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                if (onItemClickListener != null) {
                    int taskId = new Integer(mValues.get(position).getId());
                    onItemClickListener.onItemClick(holder.taskItemDelView, taskId, null, null);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mValues == null) {
            return 0;
        } else {
            return mValues.size();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;

        public final TextView taskIdView;
        public final TextView switchNameView;
        public final TextView delayTimeView;
        public final ImageView taskStatusView;
        private TextView taskItemDelView;


        public ViewHolder( View view ) {
            super(view);
            mView = view;
            taskIdView = (TextView) view.findViewById(R.id.task_id);
            switchNameView = (TextView) view.findViewById(R.id.switch_name);
            delayTimeView = (TextView) view.findViewById(R.id.delay_time);
            taskStatusView = (ImageView) view.findViewById(R.id.task_status);
            taskItemDelView = (TextView) view.findViewById(R.id.taskItemDel);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + switchNameView.getText() + "'";
        }
    }

    // ① 定义点击回调接口
    public interface OnItemClickListener {
        void onItemClick( View view, int taskId, String delayTime, String status );

        void onItemLongClick( View view );
    }

    // ② 定义一个设置点击监听器的方法
    public void setOnItemClickListener( OnItemClickListener listener ) {
        this.onItemClickListener = listener;
    }
}
