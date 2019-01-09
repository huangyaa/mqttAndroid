package com.espressif.iot.esptouch.demo_activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ashokvarma.bottomnavigation.BadgeItem;
import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.espressif.iot.esptouch.demo_activity.dummy.DummyContent;
import com.espressif.iot.esptouch.demo_activity.dummy.MessageEvent;
import com.espressif.iot.esptouch.demo_activity.login.LoginActivity;
import com.espressif.iot.esptouch.demo_activity.mqtt_util.MqttManager;
import com.espressif.iot_esptouch_demo.R;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.bean.ZxingConfig;
import com.yzq.zxinglibrary.common.Constant;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.security.acl.Permission;
import java.util.List;

public class EsptouchDemoActivity extends AppCompatActivity implements BottomNavigationBar.OnTabSelectedListener {
        private BottomNavigationBar mBottomNavigationBar;
        private BlankFragment indexFragment;
        private ItemFragment personFragment;
        private BadgeItem badgeItem;
        private Activity activity;
        private Context context;
        private TextView mobileView;
        private TextView loginOutView;
        private MqttManager mqttManager;
        private int REQUEST_CODE_SCAN = 111;

        private static String mobile;
        private FragmentManager fm ;
        private FragmentTransaction transaction ;
        private String tab = "0";

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                //获取mobile信息
                Bundle bundle = this.getIntent().getExtras();
                //保存成功跳转的参数
                tab = bundle.getString("tab");
                mobile = bundle.getString("mobile");

                setContentView(R.layout.esptouch_demo_activity);
                initBadge();
                setDefaultFragment();
                InitNavigationBar();
                activity = EsptouchDemoActivity.this;
                mobileView = (TextView)findViewById(R.id.login_in_mobile);
                mobileView.setText(mobile);
                loginOutView = (TextView)findViewById(R.id.login_out);
                loginOutView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick( View v ) {
                                //退出到登录页面
                                Intent intent = new Intent(activity, LoginActivity.class);
                                startActivity(intent);
                                finish();
                        }
                });
                //初始化mqtt管理对象
                mqttManager = MqttManager.getInstance(this);
                mqttManager.connect();
                context = this;

                fm = getSupportFragmentManager();
                transaction = fm.beginTransaction();
                if("1".equals(tab)){
                        if (indexFragment == null) {
                                indexFragment = new BlankFragment(new MyBlankFragmentListen());
                        }
                        transaction.replace(R.id.fragment_container, indexFragment);
                        transaction.commit();
                }else if("2".equals(tab)){
                        if (personFragment == null) {
                                personFragment = new ItemFragment(new MyListFragmentListen(),EsptouchDemoActivity.this, this.mobile);
                        }
                        transaction.replace(R.id.fragment_container,personFragment);
                        transaction.commit();
                }
//               //注册订阅者
//                EventBus.getDefault().register(this);
        }

        private void InitNavigationBar() {
                mBottomNavigationBar = (BottomNavigationBar)findViewById(R.id.bottom_navigation_bar);
                mBottomNavigationBar.setTabSelectedListener(this);
                mBottomNavigationBar.setMode(BottomNavigationBar.MODE_SHIFTING);
                mBottomNavigationBar.setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_RIPPLE);
                mBottomNavigationBar
                .addItem(new BottomNavigationItem(R.drawable.add1,"设置").
                        setInactiveIcon(ContextCompat.getDrawable(this,R.drawable.add)))//非选中的图)
                .addItem(new BottomNavigationItem(R.drawable.myself1,"我的").
                        setInactiveIcon(ContextCompat.getDrawable(this,R.drawable.myself)))
                .setFirstSelectedPosition(0)
                .setActiveColor(R.color.listBg)
                .setBarBackgroundColor(R.color.green)
                .initialise();
        }

        //图标上的角码设置
        public void initBadge() {
                badgeItem = new BadgeItem()
                .setBorderWidth(2)
                .setBorderColor("#ff0000")
                .setBackgroundColor("#ff0000")
                .setGravity(Gravity.RIGHT| Gravity.TOP)
                .setText("2")
                .setTextColor("#ffffff")
                .setAnimationDuration(2000)
                .setHideOnSelect(true);
        }
        private void setDefaultFragment() {
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction transaction = fm.beginTransaction();
                indexFragment = new BlankFragment(new MyBlankFragmentListen());
                transaction.replace(R.id.fragment_container, indexFragment);
                transaction.commit();
        }


        @Override
        public void onTabSelected(int position) {
                Log.d("onTabSelected", "onTabSelected: " + position);
                fm = getSupportFragmentManager();
                transaction = fm.beginTransaction();
                switch (position) {
                case 0:
                        if (indexFragment == null) {
                           indexFragment = new BlankFragment(new MyBlankFragmentListen());
                        }
                        transaction.replace(R.id.fragment_container, indexFragment);
                        break;
                case 1:
                        if (personFragment == null) {
                            personFragment = new ItemFragment(new MyListFragmentListen(),EsptouchDemoActivity.this, this.mobile);
                        }
                        transaction.replace(R.id.fragment_container,personFragment);
                        break;
                default:
                        break;
                }
                // 事务提交
                transaction.commit();
        }

        @Override
        public void onTabUnselected(int position) {
             Log.d("onTabUnselected", "onTabUnselected: " + position);
        }

        @Override
        public void onTabReselected(int position) {
             Log.d("onTabReselected", "onTabReselected: " + position);
        }

        //实现listFrament的回调监听
        public class MyListFragmentListen implements ItemFragment.OnListFragmentInteractionListener{
                @Override
                public void onListFragmentInteraction(DummyContent.DummyItem item){
                        if("ON".equals(item.getStatus())){
                                //在线可以跳转
                                Intent intent = new Intent(activity, DeviceDetailActivity.class);
                                // 通过Bundle对象存储需要传递的数据
                                Bundle bundle = new Bundle();
                                bundle.putString("mac",item.getMac());
                                bundle.putString("switchNum",item.getContent().substring(0,1));
                                bundle.putString("mark", item.getDetails());
                                bundle.putString("deviceType", item.getDeviceType());
                                bundle.putString("mobile", mobile);
                                intent.putExtras(bundle);
                                startActivity(intent);
                        }
                }
        }

        //实现BlankFrament的回调监听
        public class MyBlankFragmentListen implements BlankFragment.OnFragmentInteractionListener{
                @Override
                public void onFragmentInteraction(int checkId){
                         if(R.id.wifi == checkId){
                                 //在线可以跳转
                                 Intent intent = new Intent(activity, AddDeviceActivity.class);
                                 Bundle bundle = new Bundle();
                                 bundle.putString("mobile",mobile);
                                 intent.putExtras(bundle);
                                 startActivity(intent);
                         }else{
                                 if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                                     // 申请权限
                                     ActivityCompat.requestPermissions(EsptouchDemoActivity.this, new String[]{Manifest.permission.CAMERA}, Constant.REQUEST_CODE_SCAN);
                                     return;
                                 }else{
                                     // 获得授权
                                     ZxingConfig config = new ZxingConfig();
                                     config.setPlayBeep(true);//是否播放扫描声音 默认为true
                                     config.setShake(true);//是否震动  默认为true
                                     config.setDecodeBarCode(true);//是否扫描条形码 默认为true
                                     config.setReactColor(R.color.colorAccent);//设置扫描框四个角的颜色 默认为白色
                                     config.setFrameLineColor(R.color.colorAccent);//设置扫描框边框颜色 默认无色
                                     config.setScanLineColor(R.color.colorAccent);//设置扫描线的颜色 默认白色
                                     config.setFullScreenScan(false);//是否全屏扫描  默认为true  设为false则只会在扫描框中扫描
                                     Intent intent = new Intent(EsptouchDemoActivity.this, CaptureActivity.class);
                                     intent.putExtra(Constant.INTENT_ZXING_CONFIG, config);
                                     startActivityForResult(intent, REQUEST_CODE_SCAN);
                                 }
                         }

                }
        }
//        //定义处理接收的方法 EventBus
//        @Subscribe(threadMode = ThreadMode.MAIN)
//        public void messageEventBus(MessageEvent messageEvent){
//                if("save".equals(messageEvent.getMessage())){
//                        //列表新增，更新列表数据
//                        if (personFragment == null) {
//                                personFragment = new ItemFragment(new MyListFragmentListen(),this);
//                        }else{
//                                DummyContent.DummyItem dummyItem = new DummyContent.DummyItem("",
//                                           messageEvent.getType()+"路型开关",messageEvent.getName(),messageEvent.getMac());
//                                dummyItem.setStatus("OFF");
//                                personFragment.interItem(dummyItem);
//                        }
//                }
//        }

        @Override
        protected void onDestroy() {
                super.onDestroy();
                //注销注册
             //   EventBus.getDefault().unregister(this);
                mqttManager.close();
                mqttManager = null;
        }

        @Override
        public void onRequestPermissionsResult( int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                switch (requestCode) {
                        case Constant.REQUEST_CODE_SCAN:
                                // 摄像头权限申请
                                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                                        // 获得授权
                                        ZxingConfig config = new ZxingConfig();
                                        config.setPlayBeep(true);//是否播放扫描声音 默认为true
                                        config.setShake(true);//是否震动  默认为true
                                        config.setDecodeBarCode(true);//是否扫描条形码 默认为true
                                        config.setReactColor(R.color.colorAccent);//设置扫描框四个角的颜色 默认为白色
                                        config.setFrameLineColor(R.color.colorAccent);//设置扫描框边框颜色 默认无色
                                        config.setScanLineColor(R.color.colorAccent);//设置扫描线的颜色 默认白色
                                        config.setFullScreenScan(false);//是否全屏扫描  默认为true  设为false则只会在扫描框中扫描
                                        Intent intent = new Intent(EsptouchDemoActivity.this, CaptureActivity.class);
                                        intent.putExtra(Constant.INTENT_ZXING_CONFIG, config);
                                        startActivityForResult(intent, REQUEST_CODE_SCAN);
                                } else {
                                        // 被禁止授权
                                        Toast.makeText(EsptouchDemoActivity.this, "请至权限中心打开本应用的相机访问权限", Toast.LENGTH_LONG).show();
                                }
                                break;
                }
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
                super.onActivityResult(requestCode, resultCode, data);

                // 扫描二维码/条码回传
                if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
                        if (data != null) {

                                String content = data.getStringExtra(Constant.CODED_CONTENT);
                                Intent intent = new Intent(activity, DetailOperatorActicity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("bssid",content);
                                bundle.putString("mark","新设备"+content);
                                bundle.putString("type","2");
                                bundle.putString("mobile",mobile);
                                intent.putExtras(bundle);
                                startActivity(intent);
                        }
                }
        }
}
