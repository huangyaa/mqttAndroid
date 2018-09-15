package com.espressif.iot.esptouch.demo_activity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Selection;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.espressif.iot.esptouch.IEsptouchTask;
import com.espressif.iot.esptouch.demo_activity.mqtt_util.MqttManager;
import com.espressif.iot.esptouch.util.ByteUtil;
import com.espressif.iot.esptouch.util.EspNetUtil;
import com.espressif.iot_esptouch_demo.R;

public class DetailOperatorActicity extends Activity implements View.OnClickListener {
    private TextView mApAddrTV;
    private TextView mApBssidTV;
    private EditText mDeviceEdit;
    private Button saveBtn;
    private Button openBtn;
    private Button closeBtn;
    private MqttManager mqttManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.operator);

        mApBssidTV = findViewById(R.id.detil_bssid_text);
        mApAddrTV = findViewById(R.id.detil_addr_text);
        mDeviceEdit = findViewById(R.id.detail_device_edit);
        saveBtn = findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(this);
        openBtn = findViewById(R.id.open_btn);
        openBtn.setOnClickListener(this);
        closeBtn = findViewById(R.id.close_btn);
        closeBtn.setOnClickListener(this);
        //接收传值
        Bundle bundle = this.getIntent().getExtras();
        mApBssidTV.setText(bundle.getString("bssid"));
        mApAddrTV.setText(bundle.getString("address"));
        mDeviceEdit.setText(bundle.getString("mark"));
        mDeviceEdit.setSelection(mDeviceEdit.getText().length());
        //初始化mqtt管理对象
        mqttManager = MqttManager.getInstance(this);
        mqttManager.connect();
    }
    @Override
    protected void onStart() {
        super.onStart();
        ActionBar actionBar =  this.getActionBar();
//        ActionBar supportActionBar = getSupportActionBar();
//        supportActionBar.setTitle("gender");//设置ActionBar的标题
//        supportActionBar.setHomeButtonEnabled(true);//主键按钮能否可点击
       actionBar.setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.save_btn:
                //保存到本地文件
                Context ctx = DetailOperatorActicity.this;
                SharedPreferences sp = ctx.getSharedPreferences("devConfig" , MODE_PRIVATE);

                if(mDeviceEdit.getText().length()<= 0){
                    Toast.makeText(this, "设备名称不能为空", Toast.LENGTH_SHORT).show();
                }else{
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString(mApBssidTV.getText().toString(), mDeviceEdit.getText().toString());
                    editor.commit();
                    mDeviceEdit.setCursorVisible(false);
                    Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.open_btn:
                //发送开指令,mqtt协议
                mDeviceEdit.setCursorVisible(false);
                if(mqttManager.isConnect()){
                    mqttManager.publish("84:F3:EB:84:35:A1/user","d",false,0);//qos:0,1,2;直接设置可用，具体验证方式已经被封装好
                }else{
                    Toast.makeText(this, "连接服务器失败，不能发布信息，请检查网络", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.close_btn:
                //发送关指令，mqtt协议
                mDeviceEdit.setCursorVisible(false);
                if(mqttManager.isConnect()){
                    mqttManager.publish("84:F3:EB:84:35:A1/user","c",true,0);
                }else{
                    Toast.makeText(this, "连接服务器失败,不能订阅信息，请检查网络", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
