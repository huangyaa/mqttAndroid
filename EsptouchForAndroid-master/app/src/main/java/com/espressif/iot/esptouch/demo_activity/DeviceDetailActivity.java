package com.espressif.iot.esptouch.demo_activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.espressif.iot.esptouch.demo_activity.Utils.Const;
import com.espressif.iot.esptouch.demo_activity.Utils.NetWorkUtils;
import com.espressif.iot.esptouch.demo_activity.entity.DeviceAuthEntity;
import com.espressif.iot.esptouch.demo_activity.entity.ResultInfo;
import com.espressif.iot.esptouch.demo_activity.entity.ShareDeviceEntity;
import com.espressif.iot.esptouch.demo_activity.entity.StreamEntity;
import com.espressif.iot.esptouch.demo_activity.entity.SwitchEntity;
import com.espressif.iot.esptouch.demo_activity.entity.TaskClassEntity;
import com.espressif.iot.esptouch.demo_activity.entity.TaskResInfo;
import com.espressif.iot.esptouch.demo_activity.mqtt_util.MqttManager;
import com.espressif.iot_esptouch_demo.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;
import com.scwang.smartrefresh.layout.footer.BallPulseFooter;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.HttpUrl;

import static com.espressif.iot.esptouch.demo_activity.mqtt_util.MqttManager.TAG;

public class DeviceDetailActivity extends AppCompatActivity {
    //    private LinearLayout deviceOutLayout;
    private TextView title;
    private LinearLayout streamView;
    private ImageView imageBack;
    private ImageView moreView;
    private TextView shareView;
    private TextView logView;
    private LinearLayout imageAdd;
    private LinearLayout oprMoreView;
    private ImageView authRefreshView;
    private RecyclerView recyclerView;
    private RecyclerView recyclerTimeView;
    private RelativeLayout progress;
    private MqttManager mqttManager;
    private SwitchAdapt switchAdapt;
    private TasksAdapt tasksAdapt;
    private Context context;
    private Activity activity;
    //流量信息内容
    private StreamEntity streamEntity;
    private TextView carrierView;
    private TextView statusView;
    private TextView dateView;
    private TextView usedCountView;
    private LinearLayout streamLayout;
    //设置开关名称对话框
    private LinearLayout setDialog;
    private TextView subName;
    private EditText subNameEdit;
    private Button confirm;
    private Button cancle;

    private LinearLayout allOprLayout;
    private TextView allOpenView;
    private TextView allCloseView;
    private boolean allOpen = false;

    private int switchPosition;
    private String switchCurStatus;

    private SmartRefreshLayout smartRefreshLayout;
    //private static Map<String,Integer> devices = new HashMap<>();
    private String deviceCode;
    private String mobile;
    private String switchNum;
    //设置开关名的当前位置
    private int curPosition;
    //开关名称
    private Map<String, String> switchs = new HashMap<>();
    private List<SwitchEntity> items;

    //用于在主线程下更新UI
    private Handler handler = null;

    private boolean isModifySwitchName = false;
    private String deviceType;
    //待更新的开关名称
    private String newSwitchName;

    //用于task列表改变
    private List<TaskClassEntity> taskClassEntities;
    private int taskModifyPos;
    private String taskModifyStatus;

    private int taskDelPosition;

    private TextView autoView;
    //分享设备页面
    private LinearLayout sharelayLout;
    private EditText shareMobileText;
    private CheckBox checkBoxAdd;
    private CheckBox checkBoxDel;
    private CheckBox checkBoxUpdate;
    private Button shareConfirmBtn;
    private String addAuthority = "1";//默认没有此权限
    private String delAuthority = "1";
    private String updateAuthority = "1";

    private boolean moreVisible;

    private DeviceAuthEntity deviceAuthEntity;
    private SharedPreferences sp;
    //    private TimerTask task;
//    private static LocalReceiver localReceiver;
    private LocalReceiver localReceiverOld;

    private Runnable runnableAuto = new Runnable() {
        @Override
        public void run() {
            // 发送自动关闭
            if (mqttManager.isConnect()) {
                progress.setVisibility(View.VISIBLE);
                mqttManager.publish(deviceCode + "/user", "n" + (switchPosition + 1), false, 0);
                switchCurStatus = "ON";
            } else {
                mqttManager.connect();
            }
        }
    };

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_detail);
        //初始化基本对象
        context = DeviceDetailActivity.this;
        activity = this;
        //创建属于主线程的handler
        handler = new Handler();
        moreVisible = false;
        //初始化开关列表
        items = new ArrayList<>();
        Bundle bundle = this.getIntent().getExtras();
        deviceCode = (String) bundle.get("mac");
        deviceType = (String) bundle.get("deviceType");
        mobile = (String) bundle.get("mobile");
        switchNum = (String) bundle.get("switchNum");

        progress = findViewById(R.id.progress_detail);
        progress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                //do nothing;
            }
        });
        //流量控件初始化
        carrierView = (TextView) findViewById(R.id.carrier_view);
        statusView = (TextView) findViewById(R.id.status_view);
        dateView = (TextView) findViewById(R.id.active_date_view);
        usedCountView = (TextView) findViewById(R.id.used_count_view);

        streamLayout = (LinearLayout) findViewById(R.id.set_stream);
        streamLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                streamLayout.setVisibility(View.GONE);
            }
        });

        GetSwitchThread getSwitchThread = new GetSwitchThread(deviceCode);
        getSwitchThread.start();
        progress.setVisibility(View.VISIBLE);

        //下拉刷新功能
        smartRefreshLayout = findViewById(R.id.device_detil_out);
        smartRefreshLayout.setRefreshHeader(new ClassicsHeader(this));

        smartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh( RefreshLayout refreshlayout ) {
                mqttManager.setContext(context);
                if (!mqttManager.isConnect()) {
                    if (NetWorkUtils.isNetSystemUsable(context)) {
                        mqttManager.connect();
                    } else {
                        Toast toast = Toast.makeText(context, "当前网络不可用", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                } else {
                    //更新开关状态信息
                    for(SwitchEntity switchEntity : items){
                        switchEntity.setSwitchStatus("UN_KNOW");
                    }
                    // 获取完开关信息后再发送请求
                    mqttManager.publish(deviceCode + "/user", "s", true, 0);
                }
            }
        });
        //开关列表
        sp = context.getSharedPreferences("AutoSwitchConfig", MODE_PRIVATE);
        switchAdapt = new SwitchAdapt(items, this);
        recyclerView = findViewById(R.id.recyclerId);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        recyclerView.setAdapter(switchAdapt);
        switchAdapt.setOnItemClickListener(new SwitchAdapt.OnItemClickListener() {
            @Override
            public void onItemClick( View view, int position, String mac, String switchStatus, String switchName ) {
                //点击响应事件
                if (view.getId() == R.id.opr_btn) {
                    //发送开、关指令 p开，n关
                    switchCurStatus = switchStatus;
                    switchPosition = position;
                    String path = "/log/add";
                    if (switchStatus.equals("OFF")) {
                        if (mqttManager.isConnect()) {
                            progress.setVisibility(View.VISIBLE);
                            mqttManager.publish(mac + "/user", "p" + (position + 1), false, 0);
                            String info = sp.getString(deviceCode + "auto_" + (position + 1), null);
                            if (null != info) {
                                //设置自动关闭定时任务
                                String[] params = info.split("_");
                                if ("1".equals(params[1])) {
                                    //如果为有效状态的话，开启自动定时任务
//                                    Timer timer = new Timer();
//                                    timer.schedule(task,Integer.parseInt(params[0])*1000);
                                    handler.postDelayed(runnableAuto, Integer.parseInt(params[0]) * 1000);
                                }
                            }
                            GetAddLogThread getAddLogThread = new GetAddLogThread(path, deviceCode, mobile, "1", switchName);
                            getAddLogThread.start();
                        } else {
                            mqttManager.connect();
                        }
                    } else {
                        if (mqttManager.isConnect()) {
                            progress.setVisibility(View.VISIBLE);
                            mqttManager.publish(mac + "/user", "n" + (position + 1), false, 0);
                            GetAddLogThread getAddLogThread = new GetAddLogThread(path, deviceCode, mobile, "2", switchName);
                            getAddLogThread.start();
                        } else {
                            mqttManager.connect();
                        }
                    }
                } else if (view.getId() == R.id.switch_name) {
                    setDialog.setVisibility(View.VISIBLE);
                    //设置开关名称
                    subNameEdit.setText(switchName);
                    curPosition = position;
                    subNameEdit.setEnabled(true);
                }
            }

            @Override
            public void onItemLongClick( View view, int position, String mac ) {

            }
        });
        oprMoreView = findViewById(R.id.opr_more);
//        oprMoreView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick( View v ) {
//                if(oprMoreView.getVisibility() == View.GONE){
//                    oprMoreView.setVisibility(View.VISIBLE);
//                }else{
//                    oprMoreView.setVisibility(View.GONE);
//                }
//            }
//        });
        //开关设置选项
        autoView = findViewById(R.id.auto_close);
        autoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                ArrayList<String> switchNames = new ArrayList<>(16);
                if (switchs.isEmpty()) {
                    Toast toast = Toast.makeText(getApplicationContext(), "请等待开关信息加载完毕", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                }
                //自定义id的顺序与库中存储的开关顺序是一致的
                initListContent(switchNames,switchs.size());
                for (String id : switchs.keySet()) {
                    String name = switchs.get(id);
                    //按路数顺序插入
                    int index = new Integer(id.substring(id.length() - 1)) - 1;
                    switchNames.set(index, name);
                }
                Intent intent = new Intent(activity, SwitchAutoCloseSetActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("deviceCode", deviceCode);
                bundle.putStringArrayList("switchNames", switchNames);
                intent.putExtras(bundle);
                startActivity(intent);
                oprMoreView.setVisibility(View.GONE);
                moreVisible = false;
            }
        });
        streamView = findViewById(R.id.check_stream);
        if ("2".equals(deviceType)) {
            streamView.setVisibility(View.VISIBLE);
            streamView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick( View v ) {
                    //获取流量信息
                    progress.setVisibility(View.VISIBLE);
                    String path = "/deviceInfo/getGprs/info";
                    new GetStreamThread(path, deviceCode).start();
                    oprMoreView.setVisibility(View.GONE);
                    moreVisible = false;
                }
            });
        } else {
            streamView.setVisibility(View.GONE);
        }

        //设置开关名称
        setDialog = findViewById(R.id.set_open_name);
        setDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                setDialog.setVisibility(View.GONE);
                if (isModifySwitchName) {
                    //修改过开关名称，更新开关信息,更新开关状态
                    switchAdapt.setmValues(items);
                    switchAdapt.notifyDataSetChanged();
                    isModifySwitchName = false;
                }
            }
        });
        subName = findViewById(R.id.sub_name);
        subNameEdit = findViewById(R.id.sub_name_edit);
        confirm = findViewById(R.id.confirm_btn);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                //根据curPosition,保存新名字到文件中，并更新switchs信息
                //获取当前编辑框的值
                newSwitchName = subNameEdit.getText().toString();
                if (newSwitchName != null || !newSwitchName.equals("")) {
                    Integer switchId = items.get(curPosition).getId();
                    isModifySwitchName = true;
                    subNameEdit.setEnabled(false);
                    UpdateSwitchThread updateSwitchThread = new UpdateSwitchThread(switchId, newSwitchName);
                    updateSwitchThread.start();
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "请输入有效的开关名称", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }

            }
        });
        cancle = findViewById(R.id.cancel_btn);
        cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                setDialog.setVisibility(View.GONE);
                //修改过开关名称，更新开关信息,
                if (isModifySwitchName) {
                    switchAdapt.setmValues(items);
                    switchAdapt.notifyDataSetChanged();
                    isModifySwitchName = false;
                }
            }
        });

        allOpenView = findViewById(R.id.all_open);
        allOpenView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                if (mqttManager.isConnect()) {
                    allOpen = true;
                    String path = "/log/add";
                    progress.setVisibility(View.VISIBLE);
                    mqttManager.publish(deviceCode + "/user", "ao", false, 0);
                    GetAddLogThread getAddLogThread = new GetAddLogThread(path, deviceCode, mobile, "7", "");
                    getAddLogThread.start();

                } else {
                    mqttManager.connect();
                }
            }
        });
        allCloseView = findViewById(R.id.all_close);
        allCloseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                if (mqttManager.isConnect()) {
                    String path = "/log/add";
                    allOpen = false;
                    progress.setVisibility(View.VISIBLE);
                    mqttManager.publish(deviceCode + "/user", "ac", false, 0);
                    GetAddLogThread getAddLogThread = new GetAddLogThread(path, deviceCode, mobile, "8", "");
                    getAddLogThread.start();
                } else {
                    mqttManager.connect();
                }
            }
        });
        allOprLayout = findViewById(R.id.all_opr);
        if (Integer.parseInt(switchNum) > 1) {
            allOprLayout.setVisibility(View.VISIBLE);
        } else {
            allOprLayout.setVisibility(View.GONE);
        }
        //分享设备
        sharelayLout = findViewById(R.id.share_device_dialog);
        shareMobileText = findViewById(R.id.share_mobile);
        checkBoxAdd = findViewById(R.id.box_add);
        checkBoxAdd.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged( CompoundButton buttonView, boolean isChecked ) {
                if (isChecked) {
                    addAuthority = "1";
                } else {
                    addAuthority = "0";
                }
            }
        });
        checkBoxDel = findViewById(R.id.box_del);
        checkBoxDel.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged( CompoundButton buttonView, boolean isChecked ) {
                if (isChecked) {
                    delAuthority = "1";
                } else {
                    delAuthority = "0";
                }
            }
        });
        checkBoxUpdate = findViewById(R.id.box_update);
        checkBoxUpdate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged( CompoundButton buttonView, boolean isChecked ) {
                if (isChecked) {
                    updateAuthority = "1";
                } else {
                    updateAuthority = "0";
                }
            }
        });
        shareConfirmBtn = findViewById(R.id.share_confirm);
        shareConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                //发送分享设备信息
                String sharedMobile = shareMobileText.getText().toString();
                if (sharedMobile.isEmpty() || sharedMobile.length() < 11) {
                    Toast toast = Toast.makeText(context, "请输入有效的分享人信息", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                }
                if (sharedMobile.equals(mobile)) {
                    Toast toast = Toast.makeText(context, "设备不能分享给自己", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                }
                String path = "/deviceInfo/share";
                ShareDeviceEntity shareDeviceEntity = new ShareDeviceEntity();
                shareDeviceEntity.setAddAuthority(addAuthority);
                shareDeviceEntity.setDelAuthority(delAuthority);
                shareDeviceEntity.setUpdateAuthority(updateAuthority);
                shareDeviceEntity.setDeviceCode(deviceCode);
                shareDeviceEntity.setSharedMobile(shareMobileText.getText().toString());
                PostShareThread postShareThread = new PostShareThread(shareDeviceEntity, path);
                postShareThread.start();
            }
        });
        sharelayLout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                sharelayLout.setVisibility(View.GONE);
                shareMobileText.setText("");
                checkBoxAdd.setChecked(true);
                addAuthority = "1";
                checkBoxDel.setChecked(true);
                delAuthority = "1";
                checkBoxUpdate.setChecked(true);
                updateAuthority = "1";
                oprMoreView.setVisibility(View.GONE);
                moreVisible = false;
            }
        });

        //设备信息
        title = findViewById(R.id.title);
        imageBack = findViewById(R.id.image_back);
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                //销毁，再次打开从新create
                finish();
            }
        });
        moreView = findViewById(R.id.more);
        moreView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                if (moreVisible) {
                    oprMoreView.setVisibility(View.GONE);
                    moreVisible = false;
                } else {
                    oprMoreView.setVisibility(View.VISIBLE);
                    moreVisible = true;
                }
            }
        });
        shareView = findViewById(R.id.share_device);
        shareView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                sharelayLout.setVisibility(View.VISIBLE);
            }
        });
        logView = findViewById(R.id.opr_log);
        logView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                //跳转到操作记录表中
                Intent intent = new Intent(activity, OperateLogActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("deviceCode", deviceCode);
                intent.putExtras(bundle);
                startActivity(intent);
                oprMoreView.setVisibility(View.GONE);
                moreVisible = false;
            }
        });
        imageAdd = findViewById(R.id.imageAdd);
        imageAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                if (deviceAuthEntity == null) {
                    Toast toast = Toast.makeText(context, "请点击刷新获取有效的权限信息", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                if (deviceAuthEntity != null && Const.INVALID.equals(deviceAuthEntity.getIsAdd())) {
                    Toast toast = Toast.makeText(context, "无新增权限", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                }
                //頁面跳轉
                Intent intent = new Intent(activity, TaskSetActivity.class);
                // 通过Bundle对象存储需要传递的数据
                Bundle bundle = new Bundle();
                bundle.putString("deviceCode", deviceCode);
                //設置設備名稱列表，从switchs中读取
                ArrayList<String> switchNames = new ArrayList<>();
                initListContent(switchNames,switchs.size());
                // Collection<String> names = switchs.values();
                for (String id : switchs.keySet()) {
                    String name = switchs.get(id);
                    //按路数顺序插入
                    int index = new Integer(id.substring(id.length() - 1)) - 1;
                    switchNames.set(index, name);
                }
                bundle.putStringArrayList("swtithNames", switchNames);
                bundle.putString("mobile", mobile);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        authRefreshView = findViewById(R.id.auth_refresh);
        authRefreshView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                //重新获取用户权限
                String authPath = "/deviceInfo/getAuth";
                GetAuthThread getAuthThread = new GetAuthThread(authPath, deviceCode, mobile);
                getAuthThread.start();
            }
        });
        //定时任务列表
        recyclerTimeView = findViewById(R.id.recycleTimer);
        tasksAdapt = new TasksAdapt(null, this);
        recyclerTimeView.setLayoutManager(new LinearLayoutManager(this));
        recyclerTimeView.setAdapter(tasksAdapt);
        tasksAdapt.setOnItemClickListener(new TasksAdapt.OnItemClickListener() {
            @Override
            public void onItemClick( View view, int taskId, String status, int position ) {
                if (deviceAuthEntity == null) {
                    Toast toast = Toast.makeText(context, "请点击刷新获取有效的权限信息", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                if (view.getId() == R.id.task_status) {
                    if (deviceAuthEntity != null && Const.INVALID.equals(deviceAuthEntity.getIsUpdate())) {
                        Toast toast = Toast.makeText(context, "无修改状态权限", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        return;
                    }
                    progress.setVisibility(View.VISIBLE);
                    //更新任务状态为相反状态
                    if ("0".equals(status)) {
                        TaskClassEntity taskClassEntity = new TaskClassEntity();
                        taskClassEntity.setId(taskId);
                        taskClassEntity.setStatus("1");
                        taskClassEntity.setMobile(mobile);
                        new PostThread(taskClassEntity).start();
                        taskModifyStatus = "1";
                    } else {
                        TaskClassEntity taskClassEntity = new TaskClassEntity();
                        taskClassEntity.setId(taskId);
                        taskClassEntity.setStatus("0");
                        taskClassEntity.setMobile(mobile);
                        new PostThread(taskClassEntity).start();
                        taskModifyStatus = "0";
                    }
                    taskModifyPos = position;
                } else if (view.getId() == R.id.taskItemDel) {
                    if (deviceAuthEntity != null && Const.INVALID.equals(deviceAuthEntity.getIsDel())) {
                        Toast toast = Toast.makeText(context, "无删除权限", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        return;
                    }
                    progress.setVisibility(View.VISIBLE);
                    taskDelPosition = position;
                    String path = "/timer/delete/" + taskId;
                    new DelThread(path, mobile).start();
                }
            }

            @Override
            public void onItemLongClick( View view, int position ) {

            }
        });
        //获取权限信息
        String authPath = "/deviceInfo/getAuth";
        GetAuthThread getAuthThread = new GetAuthThread(authPath, deviceCode, mobile);
        getAuthThread.start();

        //接收传值
        title.setText(bundle.getString("mark"));
        //初始化mqtt管理对象
        mqttManager = MqttManager.getInstance(this);

        //注册广播，接收mqtt回复消息
        this.registBroadCast(deviceCode);
        //发送查询请求，获取定时任务数据
        String path = "/timer/getInfo/" + deviceCode;
        GetThread getThread = new GetThread(path);
        getThread.start();
    }

    /**
     * 初始化列表项，为了add填充
     *
     * @param switchNames
     */
    private void initListContent( List<String> switchNames, int initSize ) {
        for (int i = 0; i < initSize; i++) {
            switchNames.add(i+"");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        progress.setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * 回退时刷新数据
     */
    @Override
    protected void onResume() {
        super.onResume();
        String path = "/timer/getInfo/" + deviceCode;
        GetThread getThread = new GetThread(path);
        getThread.start();
    }

    //1. 自定义广播接收者
    public class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive( Context context, Intent intent ) {
            //获取消息
            String info = intent.getStringExtra("RspInfo");
            Log.w("DeviceDetailActivity", "RspInfo:" + info);
            if ("s".equals(info)) {
                if (items.isEmpty()) {
                    return;
                }
                String res = "";
                SwitchEntity switchEntity = items.get(switchPosition);
                if ("OFF".equals(switchCurStatus)) {
                    switchEntity.setSwitchStatus("ON");
                    res = "打开成功";
                } else {
                    switchEntity.setSwitchStatus("OFF");
                    res = "关闭成功";
                }
                switchAdapt.notifyItemChanged(switchPosition);
                Toast.makeText(context, res, Toast.LENGTH_SHORT).show();
                progress.setVisibility(View.GONE);
            } else if ("a".equals(info)) {
                if (items.isEmpty()) {
                    return;
                }
                for (SwitchEntity switchEntity : items) {
                    if (allOpen) {
                        switchEntity.setSwitchStatus("ON");
                    } else {
                        switchEntity.setSwitchStatus("OFF");
                    }
                }
                switchAdapt.setmValues(items);
                switchAdapt.notifyDataSetChanged();
                Toast.makeText(context, "操作成功", Toast.LENGTH_SHORT).show();
                progress.setVisibility(View.GONE);
            } else {
                //该部分执行的是请求所有开关状态的回复处理，更新开关当前状态
                /*Toast.makeText(context, "开关状态" + info, Toast.LENGTH_SHORT).show();*/
                char[] status = info.toCharArray();
                boolean isRepeat = false;
                if (items.size() == status.length) {
                    for (int i = 0; i < status.length; i++) {
                        SwitchEntity switchEntity = items.get(i);
                        if (!switchEntity.getSwitchStatus().equals("UN_KNOW")) {
                            Log.e("DeviceDetailActivity", "重复接收了该广播");
                            LocalBroadcastManager.getInstance(context).unregisterReceiver(localReceiverOld);
                            isRepeat = true;
                            break;
                        }
                        if ('0' == status[i]) {
                            switchEntity.setSwitchStatus("OFF");
                        } else {
                            switchEntity.setSwitchStatus("ON");
                        }
                    }
                    if (!isRepeat) {
                        switchAdapt.setmValues(items);
                        switchAdapt.notifyDataSetChanged();
                        smartRefreshLayout.finishRefresh(true);
                    }
                }
            }
        }
    }

    /**
     * 订阅消息
     *
     * @param mac
     */
    private void registBroadCast( String mac ) {
        if (mqttManager.isConnect()) {
//            if(localReceiver == null){
//                localReceiver = new LocalReceiver();
//                //2. 注册广播
//                LocalBroadcastManager.getInstance(this).registerReceiver(localReceiver, new IntentFilter("RspInfo"));
//            }
            localReceiverOld = new LocalReceiver();
            //2. 注册广播
            LocalBroadcastManager.getInstance(this).registerReceiver(localReceiverOld, new IntentFilter("RspInfo"));

            mqttManager.subscribe(mac + "/drive", 0);//qos:0,1,2;直接设置可用，具体验证方式已经被封装好
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("提示")
                    .setMessage("连接服务器失败，不能发布信息，请检查网络")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick( DialogInterface dialog, int which ) {
                            //点击确定按钮处理
                            mqttManager.connect();
                            dialog.cancel();
                        }
                    })
                    .show();
        }
    }

    // 构建Runnable对象，在runnable中更新流量信息 
    Runnable updateStreamView = new Runnable() {
        @Override
        public void run() {
            //更新界面
            progress.setVisibility(View.GONE);
            streamLayout.setVisibility(View.VISIBLE);
            carrierView.setText(streamEntity.getCarrieroperator());
            statusView.setText(streamEntity.getStatus());
            String[] strDate = streamEntity.getActivateDate().split(" ");
            dateView.setText(strDate[0]);
            String useStream = "";
            if(streamEntity.getUseStream().length() < 4){
                useStream = streamEntity.getUseStream();
            }else{
                useStream = streamEntity.getUseStream().substring(0, 4);
            }
            usedCountView.setText(useStream + "M");
        }
    };

    // 构建Runnable对象，在runnable中更新界面  
    Runnable updateTimerList = new Runnable() {
        @Override
        public void run() {
            //更新界面
            tasksAdapt.notifyDataSetChanged();
            progress.setVisibility(View.GONE);
        }
    };

    //更新單項列表數據
    Runnable updateTimerItem = new Runnable() {
        @Override
        public void run() {
            //更新界面
            TaskClassEntity taskClassEntity = taskClassEntities.get(taskModifyPos);
            taskClassEntity.setStatus(taskModifyStatus);
            tasksAdapt.notifyItemChanged(taskModifyPos, taskClassEntity);
            progress.setVisibility(View.GONE);
        }
    };

    //更新單項列表數據
    Runnable delTimerItem = new Runnable() {
        @Override
        public void run() {
            //更新界面
            //tasksAdapt.notifyDataSetChanged();
            taskClassEntities.remove(taskDelPosition);
            tasksAdapt.notifyDataSetChanged();
            progress.setVisibility(View.GONE);
        }
    };

    //发送请求失败
    Runnable requestFail = new Runnable() {
        @Override
        public void run() {
            //更新界面
            progress.setVisibility(View.GONE);
        }
    };

    class DelThread extends Thread {
        private String path;
        private String mobile;

        public DelThread( String path, String mobile ) {
            this.path = path;
            this.mobile = mobile;
        }

        @Override
        public void run() {
            try {
                String result = NetWorkUtils.getData(path + "/" + this.mobile);
                //解析
                Gson gson = new Gson();
                java.lang.reflect.Type type = new TypeToken<ResultInfo>() {
                }.getType();
                ResultInfo resultInfo = gson.fromJson(result, type);
                Log.d(TAG, "run: " + resultInfo.getData());
                //添加列表展示
                if (resultInfo.getData() != null && "true".equals((String) resultInfo.getData())) {
                    handler.post(delTimerItem);
                    result = "删除定时器成功";
                } else {
                    result = resultInfo.getMessage();
                    handler.post(requestFail);
                }
                Looper.prepare();
                Toast toast = Toast.makeText(context, result, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                Looper.loop();
            } catch (Exception e) {
                e.printStackTrace();
                Looper.prepare();
                Toast toast = Toast.makeText(context, "删除定时器失败，请检查网络是否可用", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                Looper.loop();
                handler.post(requestFail);
            }

        }
    }

    //子线程：通过post方法向服务器发送信息获取卡信息
    class GetStreamThread extends Thread {
        private String path;
        private String deviceCode;

        public GetStreamThread( String path, String deviceCode ) {
            this.path = path;
            this.deviceCode = deviceCode;
        }

        @Override
        public void run() {
            try {
                String result = NetWorkUtils.getData(path + "/" + this.deviceCode);
                //解析
                Gson gson = new Gson();
                java.lang.reflect.Type type = new TypeToken<ResultInfo<StreamEntity>>() {
                }.getType();
                ResultInfo resInfo = gson.fromJson(result, type);
                Log.d(TAG, "run: " + resInfo.getData());
                //添加列表展示
                if (resInfo.getData() != null) {
                    streamEntity = (StreamEntity) resInfo.getData();
                    handler.post(updateStreamView);
                } else {
                    handler.post(requestFail);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Looper.prepare();
                Toast toast = Toast.makeText(context, "流量信息获取失败，请检查网络是否可用", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                Looper.loop();
                handler.post(requestFail);
            }

        }
    }

    //子线程：通过GET方法向服务器发送信息获取列表
    class GetThread extends Thread {
        private String path;

        public GetThread( String path ) {
            this.path = path;
        }

        @Override
        public void run() {
            try {
                String result = NetWorkUtils.getData(path);
                //解析
                Gson gson = new Gson();
                java.lang.reflect.Type type = new TypeToken<TaskResInfo>() {
                }.getType();
                TaskResInfo resultInfo = gson.fromJson(result, type);
                Log.d(TAG, "run: " + resultInfo.getData());
                //添加列表展示
                if (resultInfo.getData() != null) {
                    taskClassEntities = resultInfo.getData();
                    tasksAdapt.SetTaskClasses(taskClassEntities);
                    handler.post(updateTimerList);
                } else {
                    handler.post(requestFail);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Looper.prepare();
                Toast toast = Toast.makeText(context, "定时器获取失败，请检查网络是否可用", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                Looper.loop();
                handler.post(requestFail);
            }

        }
    }

    //子线程：使用POST方法向服务器发送用数据
    class PostThread extends Thread {
        private TaskClassEntity taskClassEntity;

        public PostThread( TaskClassEntity taskClassEntity ) {
            this.taskClassEntity = taskClassEntity;
        }

        @Override
        public void run() {
            try {
                String path = "/timer/modify/status";
                String res = NetWorkUtils.postData(path, taskClassEntity);
                //解析Json串
                Gson gson = new Gson();
                java.lang.reflect.Type type = new TypeToken<ResultInfo>() {
                }.getType();
                ResultInfo resultInfo = gson.fromJson(res, type);
                res = (String) resultInfo.getData();
                if ("true".equals(res)) {
                    if ("1".equals(taskClassEntity.getStatus())) {
                        res = "打开定时任务成功";
                    } else {
                        res = "关闭定时任务成功";
                    }
                    handler.post(updateTimerItem);
                } else {
                    res = resultInfo.getMessage();
                    handler.post(requestFail);
                }
                Looper.prepare();
                Toast toast = Toast.makeText(context, res, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                Looper.loop();
            } catch (Exception e) {
                e.printStackTrace();
                Looper.prepare();
                Toast toast = Toast.makeText(getApplicationContext(), "操作定时任务发生异常，请检查网络是否可用", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                Looper.loop();
                handler.post(requestFail);
            }
        }
    }

    // 构建Runnable对象，在runnable中更新界面  
    Runnable getSwitchInfoSuccess = new Runnable() {
        @Override
        public void run() {
            int id = 1;

            for (SwitchEntity switchEntity : items) {
                switchEntity.setSeq(id + "");
                switchEntity.setSwitchStatus("UN_KNOW");
                //switchs 为了后面的开关下滑列表使用
                switchs.put(switchEntity.getDeviceCode() + id, switchEntity.getSwitchName());
                id++;
            }
            //刷新列表,先更新列表数据信息
            switchAdapt.setmValues(items);
            switchAdapt.notifyDataSetChanged();

            mqttManager.setContext(context);
            if (!mqttManager.isConnect()) {
                if (NetWorkUtils.isNetSystemUsable(context)) {
                    mqttManager.connect();
                } else {
                    Toast toast = Toast.makeText(context, "当前网络不可用", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            } else {
                // 获取完开关信息后再发送请求
                mqttManager.publish(deviceCode + "/user", "s", true, 0);
            }
            progress.setVisibility(View.GONE);
        }
    };

    /**
     * 获取开关信息
     */
    class GetSwitchThread extends Thread {
        private String deviceCode;

        public GetSwitchThread( String deviceCode ) {
            this.deviceCode = deviceCode;
        }

        @Override
        public void run() {
            try {
                String path = "/switch/get/" + this.deviceCode;
                String res = NetWorkUtils.getData(path);
                //解析Json串
                Gson gson = new Gson();
                java.lang.reflect.Type type = new TypeToken<ResultInfo<List<SwitchEntity>>>() {
                }.getType();
                ResultInfo<List<SwitchEntity>> resultInfo = gson.fromJson(res, type);
                if ("000000".equals(resultInfo.getStatus())) {
                    List<SwitchEntity> recvItems = resultInfo.getData();
                    items.clear();
                    items.addAll(recvItems);
                    handler.post(getSwitchInfoSuccess);
                } else {
                    res = "获取设备信息失败";
                    Looper.prepare();
                    Toast toast = Toast.makeText(context, res, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    Looper.loop();
                    handler.post(requestFail);
                }

            } catch (Exception e) {
                e.printStackTrace();
                Looper.prepare();
                Toast toast = Toast.makeText(context, "获取设备信息出错，请检查网络是否可用", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                Looper.loop();
                handler.post(requestFail);
            }
        }
    }

    // 构建Runnable对象，在runnable中更新界面  
    Runnable updateSwitchNameSuccess = new Runnable() {
        @Override
        public void run() {
            //switchs存储时位置从1开始  TODO 后台保存成功后再操作
            int at = curPosition + 1;
            switchs.put(deviceCode + at, newSwitchName);
            //更新列表中的姓名
            items.get(curPosition).setSwitchName(newSwitchName);
            Toast toast = Toast.makeText(getApplicationContext(), "保存成功", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

            setDialog.setVisibility(View.GONE);
            //修改过开关名称，更新开关信息,
            if (isModifySwitchName) {
                switchAdapt.setmValues(items);
                switchAdapt.notifyDataSetChanged();
                isModifySwitchName = false;
            }
        }
    };

    /**
     * 设置开关名称
     */
    class UpdateSwitchThread extends Thread {
        private Integer switchId;
        private String newName;

        public UpdateSwitchThread( Integer switchId, String newName ) {
            this.switchId = switchId;
            this.newName = newName;
        }

        @Override
        public void run() {
            try {
                String path = Const.baseUrl + "/switch/modify";
                /*  下面方法可为HttpUrl添加query部分内容，添加结果为：../../..?systemID = 系统id值&dishesID = 接口编号&data = 参数数据  */
                HttpUrl url = HttpUrl.parse(path).newBuilder()
                        .addQueryParameter("switchId", String.valueOf(switchId))
                        .addQueryParameter("switchName", newName)
                        .build();
                String res = NetWorkUtils.getDataByParam(url);
                //解析Json串
                Gson gson = new Gson();
                java.lang.reflect.Type type = new TypeToken<ResultInfo<String>>() {
                }.getType();
                ResultInfo<String> resultInfo = gson.fromJson(res, type);
                if ("true".equals(resultInfo.getData())) {
                    handler.post(updateSwitchNameSuccess);
                } else {
                    res = "设置开关名称失败";
                    Looper.prepare();
                    Toast toast = Toast.makeText(context, res, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    Looper.loop();
                    handler.post(requestFail);
                }

            } catch (Exception e) {
                e.printStackTrace();
                Looper.prepare();
                Toast toast = Toast.makeText(context, "设置开关名称出错，请检查网络是否可用", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                Looper.loop();
                handler.post(requestFail);
            }
        }
    }

    //子线程：通过post方法向服务器发送设备分享请求
    class PostShareThread extends Thread {
        private ShareDeviceEntity shareDeviceEntity;
        private String path;

        public PostShareThread( ShareDeviceEntity shareDeviceEntity, String path ) {
            this.shareDeviceEntity = shareDeviceEntity;
            this.path = path;
        }

        @Override
        public void run() {
            try {
                String res = "";
                String result = NetWorkUtils.postData(path, shareDeviceEntity);
                //解析
                Gson gson = new Gson();
                java.lang.reflect.Type type = new TypeToken<ResultInfo<String>>() {
                }.getType();
                ResultInfo resInfo = gson.fromJson(result, type);
                Log.d(TAG, "run: " + resInfo.getData());
                //添加列表展示
                if ("true".equals(resInfo.getData())) {
                    res = "设备分享成功";
                } else if ("P1000010".equals(resInfo.getStatus())) {
                    res = "指定的分享人需要先注册";
                } else if ("P1000016".equals(resInfo.getStatus())) {
                    res = "设备不能重复分享给一个人";
                }
                Looper.prepare();
                Toast toast = Toast.makeText(context, res, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                Looper.loop();
            } catch (Exception e) {
                e.printStackTrace();
                Looper.prepare();
                Toast toast = Toast.makeText(context, "设备分享获取失败，请检查网络是否可用", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                Looper.loop();
            }

        }
    }

    //子线程：通过GET方法向服务器发送信息获取列表
    class GetAddLogThread extends Thread {
        private String path;
        private String deviceCode;
        private String mobile;
        private String type;
        private String switchName;

        public GetAddLogThread( String path, String deviceCode, String mobile, String type, String switchName ) {
            this.path = path;
            this.deviceCode = deviceCode;
            this.mobile = mobile;
            this.type = type;
            this.switchName = switchName;
        }

        @Override
        public void run() {
            try {
                String path = Const.baseUrl + this.path;
                /*  下面方法可为HttpUrl添加query部分内容，添加结果为：../../..?systemID = 系统id值&dishesID = 接口编号&data = 参数数据  */
                HttpUrl url = HttpUrl.parse(path).newBuilder()
                        .addQueryParameter("mobile", this.mobile)
                        .addQueryParameter("deviceCode", this.deviceCode)
                        .addQueryParameter("type", this.type)
                        .addQueryParameter("switchName", this.switchName)
                        .build();
                String result = NetWorkUtils.getDataByParam(url);
                //解析
                Gson gson = new Gson();
                java.lang.reflect.Type type = new TypeToken<ResultInfo<String>>() {
                }.getType();
                ResultInfo<String> resultInfo = gson.fromJson(result, type);
                //添加列表展示
                String res = "";
                if ("true".equals(resultInfo.getData())) {
                    res = "新增操作记录成功";
                } else {
                    res = "新增操作记录失败";
                }
                Log.e("GetAddLogThread", res);
            } catch (Exception e) {
                e.printStackTrace();
                Looper.prepare();
                Toast toast = Toast.makeText(context, "新增操作记录失败，请检查网络是否可用", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                Looper.loop();
            }

        }
    }


    // 构建Runnable对象，在runnable中更新界面  
    Runnable getAuthSuccess = new Runnable() {
        @Override
        public void run() {
            //更新列表中的姓名
            authRefreshView.setVisibility(View.GONE);
        }
    };

    // 构建Runnable对象，在runnable中更新界面  
    Runnable getAuthFail = new Runnable() {
        @Override
        public void run() {
            authRefreshView.setVisibility(View.VISIBLE);
            Toast toast = Toast.makeText(getApplicationContext(), "获取权限信息失败，请点击刷新重新获取", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    };

    //子线程：通过GET方法获取设备权限
    class GetAuthThread extends Thread {
        private String path;
        private String deviceCode;
        private String mobile;

        public GetAuthThread( String path, String deviceCode, String mobile ) {
            this.path = path;
            this.deviceCode = deviceCode;
            this.mobile = mobile;
        }

        @Override
        public void run() {
            try {
                String path = this.path + "/" + mobile + "/" + deviceCode;
                String result = NetWorkUtils.getData(path);
                //解析
                Gson gson = new Gson();
                java.lang.reflect.Type type = new TypeToken<ResultInfo<DeviceAuthEntity>>() {
                }.getType();
                ResultInfo<DeviceAuthEntity> resultInfo = gson.fromJson(result, type);
                //添加列表展示
                String res = "";
                if ("000000".equals(resultInfo.getStatus())) {
                    deviceAuthEntity = resultInfo.getData();
                    handler.post(getAuthSuccess);
                } else {
                    res = resultInfo.getMessage();
                    handler.post(getAuthFail);
                }
                Log.e("GetAddLogThread", res);
            } catch (Exception e) {
                e.printStackTrace();
                Looper.prepare();
                Toast toast = Toast.makeText(context, "获取设备权限失败，请检查网络是否可用", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                Looper.loop();
            }

        }
    }
}
