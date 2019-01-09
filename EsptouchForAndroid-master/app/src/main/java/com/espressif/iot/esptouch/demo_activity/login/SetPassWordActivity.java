package com.espressif.iot.esptouch.demo_activity.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.espressif.iot.esptouch.demo_activity.Utils.EncrypAES;
import com.espressif.iot.esptouch.demo_activity.Utils.NetWorkUtils;
import com.espressif.iot.esptouch.demo_activity.entity.LoginReqDto;
import com.espressif.iot.esptouch.demo_activity.entity.ResultInfo;
import com.espressif.iot.esptouch.demo_activity.entity.SmsCodeReqDto;
import com.espressif.iot_esptouch_demo.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class SetPassWordActivity extends AppCompatActivity {
    private ImageView imageBack;
    private Activity activity;
    private EditText passWordOneView;
    private EditText passWordTwoView;
    private Button setPassWordBtn;
    private String mobile;
    //请求类型：resetPwd 或者 register
    private String type;

    private Handler handler;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_pass_word);
        activity = this;
        handler = new Handler();
        Bundle bundle = this.getIntent().getExtras();
        mobile = bundle.getString("mobile");
        type = bundle.getString("type");

        imageBack = findViewById(R.id.image_back);
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                //跳转到登录页面
                Intent intent = new Intent(activity, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        passWordOneView = (EditText) findViewById(R.id.passWord_one);
        passWordTwoView = (EditText) findViewById(R.id.passWord_two);
        setPassWordBtn = (Button) findViewById(R.id.set_passWord_button);
        setPassWordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                //发送密码,先验证俩密码是否一致
                if (passWordOneView.getText().toString().equals(passWordTwoView.getText().toString())) {
                    LoginReqDto loginReqDto = new LoginReqDto();
                    loginReqDto.setMobile(mobile);
                    loginReqDto.setPassWord(passWordTwoView.getText().toString());
                    if ("register".equals(type)) {
                        String path = "/user/register";
                        SetPassWordThread setPassWordThread = new SetPassWordThread(loginReqDto, path, type);
                        setPassWordThread.start();
                    } else {
                        String path = "/user/reset/pwd";
                        SetPassWordThread setPassWordThread = new SetPassWordThread(loginReqDto, path, type);
                        setPassWordThread.start();
                    }

                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "两次输入密码不一致", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
            }
        });
    }

    // 构建Runnable对象，在runnable中更新界面  
    Runnable RegisterSuccess = new Runnable() {
        @Override
        public void run() {
            //跳转到登录页面
            Intent intent = new Intent(activity, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    };

    //子线程：使用POST方法向服务器发送用数据
    class SetPassWordThread extends Thread {
        private LoginReqDto loginReqDto;
        private String path;
        private String reqType;

        public SetPassWordThread( LoginReqDto loginReqDto, String path, String reqType ) {
            this.loginReqDto = loginReqDto;
            this.path = path;
            this.reqType = reqType;
        }

        @Override
        public void run() {
            try {
                String res = NetWorkUtils.postData(path, loginReqDto);
                //解析Json串
                Gson gson = new Gson();
                java.lang.reflect.Type type = new TypeToken<ResultInfo>() {
                }.getType();
                ResultInfo resultInfo = gson.fromJson(res, type);
                res = (String) resultInfo.getData();
                if ("true".equals(res)) {
                    if (reqType.equals("register")){
                        res = "注册成功";
                    }else{
                        res = "密码重置成功";
                    }

                    handler.post(RegisterSuccess);
                } else if ("false".equals(res)) {
                    res = "密码重置失败，该用户还没有注册";
                } else {
                    res = "密码重置失败,信息发送异常";
                }
                Looper.prepare();
                Toast toast = Toast.makeText(getApplicationContext(), res, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                Looper.loop();
            } catch (Exception e) {
                e.printStackTrace();
                Looper.prepare();
                Toast toast = Toast.makeText(getApplicationContext(), "请求失败，请检查网络是否可用", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                Looper.loop();
            }
        }
    }
}
