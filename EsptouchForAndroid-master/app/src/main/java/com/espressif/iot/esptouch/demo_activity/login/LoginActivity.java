package com.espressif.iot.esptouch.demo_activity.login;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.espressif.iot.esptouch.demo_activity.DetailOperatorActicity;
import com.espressif.iot.esptouch.demo_activity.DeviceDetailActivity;
import com.espressif.iot.esptouch.demo_activity.EsptouchDemoActivity;
import com.espressif.iot.esptouch.demo_activity.Utils.NetWorkUtils;
import com.espressif.iot.esptouch.demo_activity.down.UpdateService;
import com.espressif.iot.esptouch.demo_activity.entity.LoginReqDto;
import com.espressif.iot.esptouch.demo_activity.entity.ResultInfo;
import com.espressif.iot.esptouch.demo_activity.entity.TaskClassEntity;
import com.espressif.iot_esptouch_demo.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {
    private static final String VERSION = "1.0.1";
    //用于在主线程下更新UI
    private Handler handler = null;
    // UI references.
    private EditText mobileView;
    private EditText mPasswordView;
    private TextView registView;
    private TextView resetView;
    private Activity activity;
    private AlertDialog mDialog;

    private Context ctx;
    private static final String MOBILE_KEY = "loginInfo_mobile";
    private static final String PWD_KEY = "loginInfo_passWord";
    private String curVersion = "1.0.1";

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        handler = new Handler();
        activity = LoginActivity.this;
        ctx =  LoginActivity.this;
        // Set up the login form.
        mobileView = (EditText) findViewById(R.id.user_name);
        mPasswordView = (EditText) findViewById(R.id.pass_word);
        mPasswordView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        //读取本地记录的用户信息
        SharedPreferences sp = ctx.getSharedPreferences("jdcCacheInfo", MODE_PRIVATE);
        String mobile = sp.getString(MOBILE_KEY,"");
        String pwd = sp.getString(PWD_KEY,"");
        mobileView.setText(mobile);
        mPasswordView.setText(pwd);

        Button loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick( View view ) {
                //登录，获取输入信息
                if(mobileView.getText().toString().isEmpty() || mPasswordView.getText().toString().isEmpty()){
                    Toast toast = Toast.makeText(getApplicationContext(), "请输入有效登录信息", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                }
                LoginReqDto loginReqDto = new LoginReqDto();
                loginReqDto.setPassWord(mPasswordView.getText().toString());
                loginReqDto.setMobile(mobileView.getText().toString());
                LoginThread loginThread = new LoginThread(loginReqDto);
                loginThread.start();
            }
        });
        registView = (TextView) findViewById(R.id.regist);
        registView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick( View v ) {
                //页面跳转
                Intent intent = new Intent(activity, GetSmsActivity.class);
                //添加属性
                Bundle bundle = new Bundle();
                bundle.putString("type", "register");
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        resetView = (TextView) findViewById(R.id.reset);
        resetView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick( View v ) {
                //页面跳转,与注册用户的流程是一样的
                Intent intent = new Intent(activity, GetSmsActivity.class);
                //添加属性
                Bundle bundle = new Bundle();
                bundle.putString("type", "resetPwd");
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });
        //版本控制
        VersionThread versionThread = new VersionThread();
        versionThread.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void startUpdate(final String url){
        new Thread(new Runnable() {
            @Override
            public void run() {
                //启动服务
                Intent service = new Intent(LoginActivity.this,UpdateService.class);
                service.putExtra("url",url);
                startService(service);
            }
        }).start();
    }

    public void verifyStoragePermissions(Activity activity) {

        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 判定是否有权限下载
     * @return
     */
    private boolean canDownloadState() {
        try {
            int state = this.getPackageManager().getApplicationEnabledSetting("com.android.providers.downloads");
            if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                    || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER
                    || state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED) {
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    Runnable versionSuccess = new Runnable() {
        @Override
        public void run() {
            //比较版本号
            if(curVersion.compareTo(VERSION) > 0){
                new AlertDialog.Builder(ctx)
                        .setTitle("九达物联又更新啦！")
                        .setMessage("最新版本"+curVersion+",要不要体验下")
                        .setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!canDownloadState()) {
//                            String packageName = "com.android.providers.downloads";
//                            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                            intent.setData(Uri.parse("package:" + packageName));
//                            startActivity(intent);
                            Toast toast = Toast.makeText(getApplicationContext(), "请到下载管理界面启用改应用的下载功能", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER, 0, 0);
                            toast.show();
                            return;
                        }
                        verifyStoragePermissions(activity);
                        startUpdate("/version/android/download");
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create().show();
            }
        }
    };
    //获取当前版本号
    class VersionThread extends Thread {
        @Override
        public void run() {
            try {
                String path = "/version/get/code";
                String res = NetWorkUtils.getData(path);
                //解析Json串
                Gson gson = new Gson();
                java.lang.reflect.Type type = new TypeToken<ResultInfo>() {
                }.getType();
                ResultInfo resultInfo = gson.fromJson(res, type);
                curVersion = (String) resultInfo.getData();
                if ("000000".equals(resultInfo.getStatus())) {
                    handler.post(versionSuccess);
                } else{
                    Looper.prepare();
                    Toast toast = Toast.makeText(getApplicationContext(), resultInfo.getMessage(), Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    Looper.loop();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Looper.prepare();
                Toast toast = Toast.makeText(getApplicationContext(), "登录失败，请检查网络是否可用", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                Looper.loop();
            }
        }
    }

    // 构建Runnable对象，在runnable中更新界面  
    Runnable loginSuccess = new Runnable() {
        @Override
        public void run() {
            //并记录本地缓存文件
            SharedPreferences sp = ctx.getSharedPreferences("jdcCacheInfo", MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(MOBILE_KEY,mobileView.getText().toString());
            editor.putString(PWD_KEY, mPasswordView.getText().toString());
            editor.commit();
            //页面跳转
            Intent intent = new Intent(activity, EsptouchDemoActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("mobile", mobileView.getText().toString());
            intent.putExtras(bundle);
            startActivity(intent);
            //跳转完以后关闭本界面
            finish();
        }
    };

    //子线程：使用POST方法向服务器发送用数据
    class LoginThread extends Thread {
        private LoginReqDto loginReqDto;

        public LoginThread( LoginReqDto loginReqDto ) {
            this.loginReqDto = loginReqDto;
        }

        @Override
        public void run() {
            try {
                String path = "/user/login";
                String res = NetWorkUtils.postData(path, loginReqDto);
                //解析Json串
                Gson gson = new Gson();
                java.lang.reflect.Type type = new TypeToken<ResultInfo>() {
                }.getType();
                ResultInfo resultInfo = gson.fromJson(res, type);
                res = (String) resultInfo.getData();
                if ("true".equals(res)) {
                    handler.post(loginSuccess);
                } else{
                    if ("false".equals(res)){
                        res = "该手机号还没有注册";
                    } else if("error".equals(res)){
                        res = "密码错误";
                    }else{
                        res = "登录失败，信息发送异常";
                    }
                    Looper.prepare();
                    Toast toast = Toast.makeText(getApplicationContext(), res, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    Looper.loop();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Looper.prepare();
                Toast toast = Toast.makeText(getApplicationContext(), "登录失败，请检查网络是否可用", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                Looper.loop();
            }
        }
    }
}

