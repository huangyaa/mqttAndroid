package com.espressif.iot.esptouch.demo_activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.espressif.iot.esptouch.demo_activity.Utils.NetWorkUtils;
import com.espressif.iot.esptouch.demo_activity.entity.CodeValidateReqDto;
import com.espressif.iot.esptouch.demo_activity.entity.DeviceInfoReqDto;
import com.espressif.iot.esptouch.demo_activity.entity.ResultInfo;
import com.espressif.iot.esptouch.demo_activity.login.SetPassWordActivity;
import com.espressif.iot.esptouch.demo_activity.mqtt_util.MqttManager;
import com.espressif.iot_esptouch_demo.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import static com.espressif.iot.esptouch.demo_activity.mqtt_util.MqttManager.TAG;

public class DetailOperatorActicity extends Activity implements View.OnClickListener {
    private TextView mApBssidTV;
    private EditText mDeviceEdit;
    private TextView mDeviceType;
    private Button saveBtn;
    private Button getTypeBtn;
    private MqttManager mqttManager;
    private ImageView imageBack;
    private Context contextDetail;
    private Handler handler;
    private String deviceId;
    private String deviceType;
    private String mobile;
    private Context context;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.operator);
        context = this;
        handler = new Handler();
        contextDetail = DetailOperatorActicity.this;
        mApBssidTV = findViewById(R.id.detil_bssid_text);
        //设备类型，从mqtt服务器中接收
        mDeviceType = findViewById(R.id.detail_device_type);
        //设备备注
        mDeviceEdit = findViewById(R.id.detail_device_name);
        saveBtn = findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(this);

        getTypeBtn = findViewById(R.id.get_type);
        getTypeBtn.setOnClickListener(this);

        imageBack = findViewById(R.id.image_back);
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                finish();
            }
        });
        //接收传值
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            deviceId = bundle.getString("bssid");
            mApBssidTV.setText(bundle.getString("bssid"));
            mDeviceEdit.setText(bundle.getString("mark"));
            mDeviceEdit.setSelection(mDeviceEdit.getText().length());
            deviceType = bundle.getString("type");
            mobile = bundle.getString("mobile");
        }
        //初始化mqtt管理对象
        mqttManager = MqttManager.getInstance(this);
        //获取类型
        //  Toast.makeText(this,"connect success?:" +new Boolean(result).toString(),Toast.LENGTH_SHORT).show();
        if (mqttManager.isConnect()) {
            //  Toast.makeText(this,deviceId+"/user",Toast.LENGTH_SHORT).show();
            //获取类型消息
            getTypeInfo(deviceId);
            mqttManager.publish(deviceId + "/user", "t", false, 0);
        }else{
            if(NetWorkUtils.isNetSystemUsable(this)){
                mqttManager.setContext(this);
                mqttManager.connect();
            }else{
                Toast toast = Toast.makeText(this, "当前网络不可用", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    //1. 自定义广播接收者
    public class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive( Context context, Intent intent ) {
            //获取消息类型
            Log.w(TAG, "intent:" + intent);
            String typeInfo = intent.getStringExtra("typeInfo");
            try {
                int type = new Integer(typeInfo);
                Log.w(TAG, "typeInfo:" + typeInfo);
                mDeviceType.setText(typeInfo + "路型开关");
            } catch (Exception e) {
                Log.w(TAG, e.getCause());
                Toast.makeText(contextDetail, "获取类型信息失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * TODO
     *
     * @return
     */
    private void getTypeInfo( String bssid ) {
        Log.w(TAG, "bssid:" + bssid);
        if (mqttManager.isConnect()) {
            LocalReceiver localReceiver = new LocalReceiver();
            //2. 注册广播
            LocalBroadcastManager.getInstance(this).registerReceiver(localReceiver, new IntentFilter("typeInfo"));

            mqttManager.subscribe(bssid + "/drive", 0);//qos:0,1,2;直接设置可用，具体验证方式已经被封装好
        } else {
            Toast.makeText(this, "连接服务器失败，不能发布信息，请检查网络", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:////主键id 必须这样写
                onBackPressed();//按返回图标直接回退上个界面
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mqttManager = null;
    }

    @Override
    public void onClick( View v ) {
        switch (v.getId()) {
            case R.id.save_btn:
                //保存到服务器

                if (mDeviceEdit.getText().length() <= 0) {
                    Toast toast = Toast.makeText(this, "设备备注不能为空", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                } else if (mDeviceType.getText().length() <= 0) {
                    Toast toast = Toast.makeText(this, "设备类型获取中，请等待", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                } else {
                    DeviceInfoReqDto deviceInfoReqDto = new DeviceInfoReqDto();
                    deviceInfoReqDto.setDeviceCode(mApBssidTV.getText().toString());
                    String type = mDeviceType.getText().toString().substring(0, 1);
                    deviceInfoReqDto.setSwitchNum(new Integer(type));
                    if(mDeviceEdit.getText().toString().isEmpty()){
                        Toast toast = Toast.makeText(this, "设备名称不能为空", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                        return;
                    }
                    deviceInfoReqDto.setDeviceName(mDeviceEdit.getText().toString());
                    deviceInfoReqDto.setMobile(mobile);
                    deviceInfoReqDto.setStatus("1");
                    deviceInfoReqDto.setType(deviceType);
                    SaveDeviceThread saveDeviceThread = new SaveDeviceThread(deviceInfoReqDto);
                    saveDeviceThread.start();
                    //通知设备数据改变
//                    String mac = (String) mApBssidTV.getText();
//                    String type =(String) mDeviceType.getText();
//                    String name = mDeviceEdit.getText().toString();
                    //动态通知取消，改为手动刷新
                    //  EventBus.getDefault().post(new MessageEvent("save",mac,type,name));
                }
                break;
            case R.id.get_type:
                if (mqttManager.isConnect()) {
                    mqttManager.publish(deviceId + "/user", "t", false, 0);
                } else {
                    //优化的话，可以取消
                   // if(NetWorkUtils.isNetSystemUsable(this)){
                        mqttManager.connect();
//                    }else{
//                        Toast toast = Toast.makeText(this, "当前网络不可用", Toast.LENGTH_SHORT);
//                        toast.setGravity(Gravity.CENTER, 0, 0);
//                        toast.show();
//                    }
                }

                break;

        }
    }

    // 构建Runnable对象，在runnable中更新界面  
    Runnable getValidateSuccess = new Runnable() {
        @Override
        public void run() {
            mDeviceEdit.setEnabled(false);
            //跳转到首页列表页面

            Intent intent = new Intent(context, EsptouchDemoActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("mobile",mobile);
            bundle.putString("tab","2");
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }
    };

    /**
     * 保存设备
     */
    class SaveDeviceThread extends Thread {
        private DeviceInfoReqDto deviceInfoReqDto;

        public SaveDeviceThread( DeviceInfoReqDto deviceInfoReqDto ) {
            this.deviceInfoReqDto = deviceInfoReqDto;
        }

        @Override
        public void run() {
            try {
                String path = "/deviceInfo/save";
                String res = NetWorkUtils.postData(path, deviceInfoReqDto);
                //解析Json串
                Gson gson = new Gson();
                java.lang.reflect.Type type = new TypeToken<ResultInfo>() {
                }.getType();
                ResultInfo resultInfo = gson.fromJson(res, type);
                res = (String) resultInfo.getData();
                if ("true".equals(res)) {
                    res = "设备信息添加成功";
                    handler.post(getValidateSuccess);
                } else if("exit".equals(res)){
                    res = "设备添加失败,该设备已经被添加过";
                }else{
                    res = "设备添加失败,该用户没有注册过";
                }
                Looper.prepare();
                Toast toast = Toast.makeText(getApplicationContext(), res, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                Looper.loop();
            } catch (Exception e) {
                e.printStackTrace();
                Looper.prepare();
                Toast toast = Toast.makeText(getApplicationContext(), "设备添加失败，请检查网络是否可用", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                Looper.loop();
            }
        }
    }
}
