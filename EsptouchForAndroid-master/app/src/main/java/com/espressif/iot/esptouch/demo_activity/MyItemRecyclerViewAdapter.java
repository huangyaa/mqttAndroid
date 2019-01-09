package com.espressif.iot.esptouch.demo_activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.espressif.iot.esptouch.demo_activity.ItemFragment.OnListFragmentInteractionListener;
import com.espressif.iot.esptouch.demo_activity.dummy.DummyContent.DummyItem;
import com.espressif.iot_esptouch_demo.R;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link DummyItem} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {
    private static final String WIFI = "1";
    private static final String GPRS = "2";

    private final List<DummyItem> mValues;
    private final OnListFragmentInteractionListener mListener;
    private OnItemClickListener onItemClickListener;
    private final Context context;

    public MyItemRecyclerViewAdapter(List<DummyItem> items, OnListFragmentInteractionListener listener, Context context) {
        mValues = items;
        mListener = listener;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mItem = mValues.get(position);
        if(WIFI.equals(mValues.get(position).getDeviceType())){
            holder.mIdCardView.setImageResource(R.drawable.wifi_card);//列表展示的id值比实际的position大1
        }else{
            holder.mIdCardView.setImageResource(R.drawable.tel_card);
        }

        holder.mContentView.setText(mValues.get(position).content);
        holder.mName.setText(mValues.get(position).details);
        holder.status.setText(mValues.get(position).status);
        if("OFF".equals(mValues.get(position).status)){
            holder.status.setTextColor(context.getResources().getColor(R.color.red));
        }else{
            holder.status.setTextColor(context.getResources().getColor(R.color.blue));
        }

        if((position+1)%2 != 0){
            holder.mView.setBackgroundColor(context.getResources().getColor(R.color.listBg));
        }

        holder.lvFraListid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });

        holder.delView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                if("2".equals(mValues.get(position).getShareStatus())){
                    Toast toast = Toast.makeText(context, "该设备是别人分享的，无权删除", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                }
                if(onItemClickListener != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage("是否删除当前设备？");
                    builder.setTitle("九达诚");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int pos = holder.getLayoutPosition();
                            String deviceId = mValues.get(pos).mac;
                            onItemClickListener.onItemClick(holder.delView, pos,deviceId);
                        }
                    });
                    builder.setNegativeButton("取消", null);
                    builder.show();

                }

            }
        });
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position,List<Object> payloads) {
        if(payloads.isEmpty()){
            onBindViewHolder(holder,position);
        }else{
            //更新是否在线
            String payload = (String) payloads.get(0);
            DummyItem item = mValues.get(position);
            ViewHolder viewHolder = (ViewHolder) holder;
            if(!viewHolder.status.getText().equals(payload)){
                viewHolder.status.setText(payload);
                item.setStatus(payload);
            }
            if("OFF".equals(payload)){
                holder.status.setTextColor(context.getResources().getColor(R.color.colorAccent));
            }else{
                holder.status.setTextColor(context.getResources().getColor(R.color.blue));
            }
        }
    }

    // ① 定义点击回调接口
    public interface OnItemClickListener {
        void onItemClick(View view, int position,String mac);
    }

    // ② 定义一个设置点击监听器的方法
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView mIdCardView;
        public final TextView mContentView;
        public final TextView mName;
        public final TextView status;
        public final TextView delView;
        public final LinearLayout lvFraListid;

        public DummyItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdCardView = (ImageView) view.findViewById(R.id.cardType);
            mContentView = (TextView) view.findViewById(R.id.type);
            mName = (TextView) view.findViewById(R.id.name);
            status = (TextView) view.findViewById(R.id.status);
            delView = (TextView) view.findViewById(R.id.deviceItemDel);
            lvFraListid =  view.findViewById(R.id.lvFraList_id);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'"+ mName.getText();
        }
    }
}
