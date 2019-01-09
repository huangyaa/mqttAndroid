package com.espressif.iot.esptouch.demo_activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.espressif.iot.esptouch.demo_activity.Utils.Const;
import com.espressif.iot.esptouch.demo_activity.Utils.NetWorkUtils;
import com.espressif.iot.esptouch.demo_activity.entity.DeviceInfoEntity;
import com.espressif.iot.esptouch.demo_activity.entity.DeviceInfoReqDto;
import com.espressif.iot.esptouch.demo_activity.entity.DeviceInfoRes;
import com.espressif.iot.esptouch.demo_activity.entity.ResultInfo;
import com.espressif.iot.esptouch.demo_activity.mqtt_util.MqttManager;
import com.espressif.iot_esptouch_demo.R;
import com.espressif.iot.esptouch.demo_activity.dummy.DummyContent;
import com.espressif.iot.esptouch.demo_activity.dummy.DummyContent.DummyItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;
import com.scwang.smartrefresh.layout.footer.BallPulseFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.HttpUrl;

import static android.content.Context.MODE_PRIVATE;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class ItemFragment extends Fragment  { //implements SwipeRefreshLayout.OnRefreshListener

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String ALL = "1";
    private static final String SHARE = "2";
    private static final String SHARED = "3";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private Activity activityMain;
    private Context context;
    private OnListFragmentInteractionListener mListener;
    private MqttManager mqttManager;
    private MyItemRecyclerViewAdapter recyclerViewAdapter;
    private SmartRefreshLayout smartRefreshLayout;
    private TextView allView;
    private View view1;
    private View view2;
    private View view3;
    private TextView shareToOtherView;
    private TextView sharedWithOtherView;

    private String mobile;
    private Handler handler;
    //设备信息相关变量
    private List<DeviceInfoEntity> deviceInfoEntities;
    private Map<String, Integer> deviceInfos = new HashMap<>();
    private String delDeviceCode;
    private int delPosition;

    private String curRequst = ALL;

    private static Map<String, String> deviceStatus = new HashMap<>();

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ItemFragment() {
    }

    @SuppressLint("ValidFragment")
    public ItemFragment( OnListFragmentInteractionListener listener, Context context, String mobile ) {
        mListener = listener;
        this.context = context;
        this.mobile = mobile;
        handler = new Handler();
        mqttManager = MqttManager.getInstance(context);
        if (!mqttManager.isConnect()) {
            if (NetWorkUtils.isNetSystemUsable(context)) {
                mqttManager.setContext(context);
                mqttManager.connect();
            } else {
                Toast toast = Toast.makeText(context, "当前网络不可用", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        }
    }

    //刷新数据
    public void interItem( DummyItem dummyItem ) {
        int length = DummyContent.ITEMS.size();
        dummyItem.setId((length - 1) + "");
        DummyContent.ITEMS.add(dummyItem);
        recyclerViewAdapter.notifyItemInserted(length - 1);
    }

    // TODO: Customize parameter initialization
    public static ItemFragment newInstance( int columnCount ) {
        ItemFragment fragment = new ItemFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                              Bundle savedInstanceState ) {
        View view = inflater.inflate(R.layout.fragment_item_list, container, false);
        smartRefreshLayout = view.findViewById(R.id.refresh);
        //        smartRefreshLayout.setOnRefreshListener(this);
        //设置 Header
        smartRefreshLayout.setRefreshHeader(new ClassicsHeader(context));
        smartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh( RefreshLayout refreshlayout ) {
                refreshlayout.finishRefresh(2000);
                if (mqttManager.isConnect()) {
                    //当前网络可用，刷新数据
                    GetDeviceThread getDeviceThread = new GetDeviceThread(mobile,curRequst);
                    getDeviceThread.start();
                } else {
                    new AlertDialog.Builder(context)
                            .setTitle("提示")
                            .setMessage("连接服务器失败，不能发布信息，请检查网络")
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick( DialogInterface dialog, int which ) {
                                    DummyContent.ITEMS.clear();
//                                    mqttManager.setUseConnect(false);
                                    mqttManager.connect();
                                    recyclerViewAdapter.notifyDataSetChanged();
                                    dialog.cancel();
                                }
                            })
                            .show();
                }
            }
        });

        // Set the adapter
        Context context = view.getContext();
        allView = (TextView) view.findViewById(R.id.all_device);
        view1 = (View) view.findViewById(R.id.all_device_view);
        allView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                //触发事件
                allView.setTextColor(getResources().getColor(R.color.blue));
                view1.setBackgroundColor(getResources().getColor(R.color.blue));
                shareToOtherView.setTextColor(getResources().getColor(R.color.colorPrimary));
                view2.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                sharedWithOtherView.setTextColor(getResources().getColor(R.color.colorPrimary));
                view3.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                //获取数据
                GetDeviceThread getDeviceThread = new GetDeviceThread(mobile,ALL);
                getDeviceThread.start();
                curRequst = ALL;
            }
        });
        shareToOtherView = (TextView) view.findViewById(R.id.share_device_to_other);
        view2 = (View) view.findViewById(R.id.share_device_to_other_view);
        shareToOtherView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                //触发事件
                allView.setTextColor(getResources().getColor(R.color.colorPrimary));
                view1.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                shareToOtherView.setTextColor(getResources().getColor(R.color.blue));
                view2.setBackgroundColor(getResources().getColor(R.color.blue));
                sharedWithOtherView.setTextColor(getResources().getColor(R.color.colorPrimary));
                view3.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                //获取设备数据
                GetDeviceThread getDeviceThread = new GetDeviceThread(mobile,SHARE);
                getDeviceThread.start();
                curRequst = SHARE;
            }
        });

        sharedWithOtherView = (TextView) view.findViewById(R.id.shared_device_with_other);
        view3 = (View) view.findViewById(R.id.shared_device_with_other_view);
        sharedWithOtherView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                //触发事件
                allView.setTextColor(getResources().getColor(R.color.colorPrimary));
                view1.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                shareToOtherView.setTextColor(getResources().getColor(R.color.colorPrimary));
                view2.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                sharedWithOtherView.setTextColor(getResources().getColor(R.color.blue));
                view3.setBackgroundColor(getResources().getColor(R.color.blue));
                //获取设备数据
                GetDeviceThread getDeviceThread = new GetDeviceThread(mobile,SHARED);
                getDeviceThread.start();
                curRequst = SHARED;
            }
        });
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));

        //获取设备数据
        GetDeviceThread getDeviceThread = new GetDeviceThread(this.mobile,ALL);
        getDeviceThread.start();

        recyclerViewAdapter = new MyItemRecyclerViewAdapter(DummyContent.ITEMS, mListener, context);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerViewAdapter.setOnItemClickListener(new MyItemRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick( View view, int position, String deviceCode ) {
                DelDeviceThread delDeviceThread = new DelDeviceThread(deviceInfos.get(deviceCode));
                delDeviceThread.start();
                delDeviceCode = deviceCode;
                delPosition = position;
                //取消订阅
                mqttManager.unSubscribe(deviceCode + "/drive");
            }
        });

        //用于更新数据用
        mqttManager.setRecyclerViewAdapter(recyclerViewAdapter);
        return view;
    }


    @Override
    public void onAttach( Context context ) {
        super.onAttach(context);
        activityMain = getActivity();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction( DummyItem item );
    }


    // 构建Runnable对象，在runnable中更新界面  
    Runnable getDeviceInfoSuccess = new Runnable() {
        @Override
        public void run() {
            DummyContent.ITEMS.clear();
            int id = 0;
            for (DeviceInfoEntity deviceInfoEntity : deviceInfoEntities) {
                String deviceCode = deviceInfoEntity.getDeviceCode();
                String switchNum = deviceInfoEntity.getSwitchNum() + "路型开关";
                String name = deviceInfoEntity.getDeviceName();
                String deviceType = deviceInfoEntity.getType();
                deviceInfos.put(deviceCode, deviceInfoEntity.getId());
                DummyItem dummyItem = new DummyItem(id + 1 + "", switchNum, name, deviceCode, deviceType);
                dummyItem.setStatus("OFF");
                dummyItem.setShareStatus(deviceInfoEntity.getStatus());
                DummyContent.ITEMS.add(dummyItem);
                deviceStatus.put(deviceCode, id + "-OFF");//position+状态
                id++;
                if (mqttManager.isConnect()) {
                    //订阅消息
                    mqttManager.subscribe(deviceCode + "/drive", 0);
                    //发送消息
                    mqttManager.publish(deviceCode + "/user", "h", false, 0);
                }
            }
            //设置检测数据
            mqttManager.setDevices(deviceStatus);
            mqttManager.excuteTask(3 * 1000);
            //刷新列表
            recyclerViewAdapter.notifyDataSetChanged();
            smartRefreshLayout.finishRefresh(true);
        }
    };

    /**
     * 获取设备信息
     */
    class GetDeviceThread extends Thread {
        private String mobile;
        //1：所有的； 2：分享出去的；3：被分享的
        private String type;

        public GetDeviceThread( String mobile ,String type) {
            this.mobile = mobile;
            this.type = type;
        }

        @Override
        public void run() {
            try {
                String path = "/deviceInfo/get/" + this.mobile + "/" + this.type;
                String res = NetWorkUtils.getData(path);
                //解析Json串
                Gson gson = new Gson();
                java.lang.reflect.Type type = new TypeToken<DeviceInfoRes>() {
                }.getType();
                DeviceInfoRes resultInfo = gson.fromJson(res, type);
                if ("000000".equals(resultInfo.getStatus())) {
                    res = "获取设备信息成功";
                    deviceInfoEntities = resultInfo.getData();
                    handler.post(getDeviceInfoSuccess);
                } else {
                    res = "获取设备信息失败";
                    smartRefreshLayout.finishRefresh(false);
                }
                Looper.prepare();
                Toast toast = Toast.makeText(context, res, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                Looper.loop();
            } catch (Exception e) {
                e.printStackTrace();
                Looper.prepare();
                Toast toast = Toast.makeText(context, "获取设备信息出错，请检查网络是否可用", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                Looper.loop();
            }
        }
    }

    // 构建Runnable对象，在runnable中更新界面  
    Runnable delDeviceSuccess = new Runnable() {
        @Override
        public void run() {
            deviceInfos.remove(delDeviceCode);
            DummyContent.ITEMS.remove(delPosition);
            //刷新列表
            recyclerViewAdapter.notifyDataSetChanged();
        }
    };

    /**
     * 删除设备
     */
    class DelDeviceThread extends Thread {
        private Integer deviceId;

        public DelDeviceThread( Integer deviceId ) {
            this.deviceId = deviceId;
        }

        @Override
        public void run() {
            try {
                String path = "/deviceInfo/del/" + this.deviceId;
                String res = NetWorkUtils.getData(path);
                //解析Json串
                Gson gson = new Gson();
                java.lang.reflect.Type type = new TypeToken<ResultInfo>() {
                }.getType();
                ResultInfo resultInfo = gson.fromJson(res, type);
                if ("true".equals((String) resultInfo.getData())) {
                    res = "删除设备成功";
                    handler.post(delDeviceSuccess);
                } else {
                    res = "删除设备失败,不能删除别人分享的设备";
                }
                Looper.prepare();
                Toast toast = Toast.makeText(context, res, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                Looper.loop();
            } catch (Exception e) {
                e.printStackTrace();
                Looper.prepare();
                Toast toast = Toast.makeText(context, "删除设备出错，请检查网络是否可用", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                Looper.loop();
            }
        }
    }
}
