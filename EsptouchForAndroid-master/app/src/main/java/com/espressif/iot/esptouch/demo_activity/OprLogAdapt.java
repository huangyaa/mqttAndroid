package com.espressif.iot.esptouch.demo_activity;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.espressif.iot.esptouch.demo_activity.entity.OperateTypeEnum;
import com.espressif.iot.esptouch.demo_activity.entity.OprLogEntity;
import com.espressif.iot_esptouch_demo.R;

import java.util.List;
import java.util.Map;

public class OprLogAdapt extends RecyclerView.Adapter<OprLogAdapt.ViewHolder> {
    private OnItemClickListener onItemClickListener;
    private Context context;
    private List<OprLogEntity> oprLogEntitys;
    private static Map<String ,String > valueMap;

    public OprLogAdapt( List<OprLogEntity> oprLogEntitys, Context context){
        this.context = context;
        this.oprLogEntitys = oprLogEntitys;
        valueMap = OperateTypeEnum.getValueMap();
    }

    @NonNull
    @Override
    public OprLogAdapt.ViewHolder onCreateViewHolder( @NonNull ViewGroup parent, int viewType ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.log_item, parent, false);
        return new OprLogAdapt.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder( @NonNull OprLogAdapt.ViewHolder holder, int position ) {
        int id = position + 1;
        holder.idView.setText(id + "");
        holder.mobileView.setText(oprLogEntitys.get(position).getMobile());
        if(oprLogEntitys.get(position).getContent().isEmpty()){
            holder.doView.setText(valueMap.get(oprLogEntitys.get(position).getType()));
        }else{
            holder.doView.setText(oprLogEntitys.get(position).getContent() + "--"
                    + valueMap.get(oprLogEntitys.get(position).getType()));
        }
        holder.timeView.setText(oprLogEntitys.get(position).getCreateTime());
        if((position+1)%2 != 0){
            holder.mView.setBackgroundColor(context.getResources().getColor(R.color.listBg));
        }else{
            holder.mView.setBackgroundColor(context.getResources().getColor(R.color.write));
        }
    }
    @Override
    public int getItemCount() {
        if(oprLogEntitys == null){
            return 0;
        }else{
            return oprLogEntitys.size();
        }
    }

    public void setOprLogEntitys (List<OprLogEntity> oprLogEntitys){
        this.oprLogEntitys = oprLogEntitys;
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;

        public final TextView idView;
        public final TextView mobileView;
        public final TextView doView;
        public final TextView timeView;


        public ViewHolder(View view) {
            super(view);
            mView = view;
            idView = (TextView) view.findViewById(R.id.log_item_id);
            mobileView = (TextView) view.findViewById(R.id.log_item_mobile);
            doView = (TextView) view.findViewById(R.id.log_item_do);
            timeView = (TextView) view.findViewById(R.id.log_item_time);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + doView.getText() + "'";
        }
    }

    // ① 定义点击回调接口
    public interface OnItemClickListener {
        void onItemClick();
        void onItemLongClick();
    }

    // ② 定义一个设置点击监听器的方法
    public void setOnItemClickListener(OprLogAdapt.OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }
}
