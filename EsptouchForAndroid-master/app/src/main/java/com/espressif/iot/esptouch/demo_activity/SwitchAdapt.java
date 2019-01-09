package com.espressif.iot.esptouch.demo_activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.espressif.iot.esptouch.demo_activity.dummy.DummyContent;
import com.espressif.iot.esptouch.demo_activity.entity.SwitchEntity;
import com.espressif.iot_esptouch_demo.R;

import java.util.ArrayList;
import java.util.List;

public class SwitchAdapt extends RecyclerView.Adapter<SwitchAdapt.ViewHolder> {
    private OnItemClickListener onItemClickListener;
    private Context context;
    private List<SwitchEntity> mValues = new ArrayList<>();

    public SwitchAdapt( List<SwitchEntity> items, Context context ) {
        mValues.clear();
        this.mValues.addAll(items);
        this.context = context;
    }

    public void setmValues( List<SwitchEntity> items ) {
        mValues.clear();
        this.mValues.addAll(items);
    }

    @NonNull
    @Override
    public SwitchAdapt.ViewHolder onCreateViewHolder( @NonNull ViewGroup parent, int viewType ) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.switchs, parent, false);
        return new SwitchAdapt.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder( @NonNull final ViewHolder holder, final int position ) {
        final String mac = mValues.get(position).getDeviceCode();
        holder.switchName.setText(mValues.get(position).getSwitchName());
        holder.setSwitchName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                if (onItemClickListener != null) {
                    int pos = holder.getLayoutPosition();
                    onItemClickListener.onItemClick(holder.switchName, pos, mac, "", holder.switchName.getText().toString());
                }
            }
        });
        if ("OFF".equals(mValues.get(position).getSwitchStatus())) {
            holder.switchStatus.setText("OFF");
            holder.switchStatus.setTextColor(context.getResources().getColor(R.color.red));
            holder.oprBtn.setImageResource(R.drawable.close);
        } else if ("ON".equals(mValues.get(position).getSwitchStatus())) {
            holder.switchStatus.setText("ON");
            holder.switchStatus.setTextColor(context.getResources().getColor(R.color.blue));
            holder.oprBtn.setImageResource(R.drawable.open);
        } else {
            holder.switchStatus.setText("获取中");
            holder.oprBtn.setImageResource(R.drawable.wait);
        }
        holder.oprBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                if (onItemClickListener != null) {
                    if("UN_KNOW".equals(mValues.get(position).getSwitchStatus())){
                        Toast toast = Toast.makeText(context, "当前开关状态未知，无法保证操作的准确性", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }else{
                        int pos = holder.getLayoutPosition();
                        onItemClickListener.onItemClick(holder.oprBtn, pos, mac,
                                holder.switchStatus.getText().toString(), holder.switchName.getText().toString());
                    }
                }
            }
        });
//        holder.closeBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick( View v ) {
//                if(onItemClickListener != null) {
//                    int pos = holder.getLayoutPosition();
//                    onItemClickListener.onItemClick(holder.closeBtn, pos,mac,"");
//                }
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView switchName;
        public final ImageView oprBtn;
        public final TextView switchStatus;
        public final LinearLayout setSwitchName;


        public ViewHolder( View view ) {
            super(view);
            mView = view;
            switchName = (TextView) view.findViewById(R.id.switch_name);
            oprBtn = (ImageView) view.findViewById(R.id.opr_btn);
            switchStatus = (TextView) view.findViewById(R.id.switch_status);
            setSwitchName = (LinearLayout) view.findViewById(R.id.set_switch_name);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + switchName.getText() + "'";
        }
    }

    // ① 定义点击回调接口
    public interface OnItemClickListener {
        void onItemClick( View view, int position, String mac, String switchStatus, String switchName );

        void onItemLongClick( View view, int position, String mac );
    }

    // ② 定义一个设置点击监听器的方法
    public void setOnItemClickListener( SwitchAdapt.OnItemClickListener listener ) {
        this.onItemClickListener = listener;
    }

}
