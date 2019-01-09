package com.espressif.iot.esptouch.demo_activity;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.espressif.iot.esptouch.demo_activity.entity.TaskClassEntity;
import com.espressif.iot_esptouch_demo.R;

import java.util.List;

public class TasksAdapt extends RecyclerView.Adapter<TasksAdapt.ViewHolder> {
    private OnItemClickListener onItemClickListener;
    private Context context;
    private List<TaskClassEntity> taskClasses;
    public TasksAdapt( List<TaskClassEntity> taskClasses, Context context){
        this.context = context;
        this.taskClasses = taskClasses;
    }
    @NonNull
    @Override
    public TasksAdapt.ViewHolder onCreateViewHolder( @NonNull final ViewGroup parent, int viewType ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tasks_item_list, parent, false);
        return new TasksAdapt.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder( @NonNull final TasksAdapt.ViewHolder holder, final int position ) {
        holder.taskNameView.setText(taskClasses.get(position).getSwitchName());
        if (taskClasses.get(position).getType().equals("0")) {
            holder.taskTypeView.setText("当天有效");
            holder.taskTimeView.setText(taskClasses.get(position).getExcuDate());
        } else {
            holder.taskTypeView.setText("永久有效");
            holder.taskTimeView.setText(taskClasses.get(position).getExcuTime());
        }
        if (taskClasses.get(position).getStatus().equals("0")) {
            holder.taskStatusView.setImageResource(R.drawable.task_close);
        } else {
            holder.taskStatusView.setImageResource(R.drawable.task_open);
        }
        holder.taskStatusView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                if (onItemClickListener != null) {
                    int taskId = new Integer(taskClasses.get(position).getId());
                    onItemClickListener.onItemClick(holder.taskStatusView, taskId, taskClasses.get(position).getStatus(), position);
                }
            }
        });
        holder.taskItemDelView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                if (onItemClickListener != null) {
                    int taskId = new Integer(taskClasses.get(position).getId());
                    onItemClickListener.onItemClick(holder.taskItemDelView, taskId, null,position);
                }
            }
        });
    }
    public void SetTaskClasses(List<TaskClassEntity> taskClasses){
        this.taskClasses = taskClasses;
    }

    @Override
    public int getItemCount() {
        if(taskClasses == null){
            return 0;
        }else{
            return taskClasses.size();
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;

        public final TextView taskNameView;
        public final TextView taskTimeView;
        public final TextView taskTypeView;
        public final ImageView taskStatusView;
        private TextView taskItemDelView;


        public ViewHolder(View view) {
            super(view);
            mView = view;
            taskNameView = (TextView) view.findViewById(R.id.task_name);
            taskTimeView = (TextView) view.findViewById(R.id.task_time);
            taskTypeView = (TextView) view.findViewById(R.id.task_type);
            taskStatusView = (ImageView) view.findViewById(R.id.task_status);
            taskItemDelView = (TextView) view.findViewById(R.id.taskItemDel);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + taskNameView.getText() + "'";
        }
    }

    // ① 定义点击回调接口
    public interface OnItemClickListener {
        void onItemClick(View view, int taskId ,String status,int position);
        void onItemLongClick(View view, int position);
    }

    // ② 定义一个设置点击监听器的方法
    public void setOnItemClickListener(TasksAdapt.OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
}
