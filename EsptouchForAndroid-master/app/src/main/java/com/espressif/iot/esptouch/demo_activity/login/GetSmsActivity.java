package com.espressif.iot.esptouch.demo_activity.login;

import android.app.Activity;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.espressif.iot.esptouch.demo_activity.Utils.EncrypAES;
import com.espressif.iot.esptouch.demo_activity.Utils.NetWorkUtils;
import com.espressif.iot.esptouch.demo_activity.entity.CodeValidateReqDto;
import com.espressif.iot.esptouch.demo_activity.entity.ResultInfo;
import com.espressif.iot.esptouch.demo_activity.entity.SmsCodeReqDto;
import com.espressif.iot.esptouch.demo_activity.entity.TaskClassEntity;
import com.espressif.iot_esptouch_demo.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.common.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

public class GetSmsActivity extends AppCompatActivity {
    private final String key = "4f3ef09822b48ec28f0ff3dbf923344f116315d22f28017be39eb119555a9582";
    private ImageView imageBack;
    private EditText mobileView;
    private EditText smsCodeView;
    private Button getSmsBtn;
    private Button nextBtn;
    private Handler handler;
    private Activity activity;
    //请求类型：resetPwd 或者 register
    private String type;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_sms);
        handler = new Handler();
        activity = this;
        Bundle bundle = this.getIntent().getExtras();
        type = bundle.getString("type");

        imageBack = findViewById(R.id.image_back);
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                finish();
            }
        });
        mobileView = (EditText) findViewById(R.id.mobile);
        smsCodeView = (EditText) findViewById(R.id.sms_code);
        getSmsBtn = (Button) findViewById(R.id.getSms_button);
        getSmsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                //获取短信验证码
                SmsCodeReqDto smsCodeReqDto = new SmsCodeReqDto();
                String mobile = mobileView.getText().toString();
                if(mobile.length() != 11){
                    Toast toast = Toast.makeText(getApplicationContext(), "请输入正确的手机号", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                }
                smsCodeView.setFocusable(true);
                smsCodeView.requestFocus();
                smsCodeReqDto.setPhone(mobile);
                //android 类型为 "1"
                smsCodeReqDto.setPlatform("1");
                smsCodeReqDto.setSmsType(type);
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                df.setTimeZone(TimeZone.getTimeZone("GMT+8"));
                smsCodeReqDto.setTime(df.format(new Date()).toString());
                smsCodeReqDto.setUniqueCode(UUID.randomUUID().toString().replaceAll("-", ""));
                try{
                    Map<String, String> reqData = new HashMap<>(16);
                    reqData.put("smsType", smsCodeReqDto.getSmsType());
                    reqData.put("phone", mobile);
                    reqData.put("uniqueCode",smsCodeReqDto.getUniqueCode());
                    reqData.put("platform", smsCodeReqDto.getPlatform());
                    reqData.put("time", smsCodeReqDto.getTime());
                    smsCodeReqDto.setSign(EncrypAES.generateSignature(reqData,key,"MD5"));
                    SmsCodeThread smsCodeThread = new SmsCodeThread(smsCodeReqDto);
                    smsCodeThread.start();
                }catch (Exception e){
                    e.printStackTrace();
                    Log.e("GetSmsActivity","生成sign出错");
                    Toast toast = Toast.makeText(getApplicationContext(), "获取短信验证码失败", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                }
                getSmsBtn.setEnabled(false);
                countDownTimer.start();
            }
        });
        nextBtn = (Button)findViewById(R.id.login_next_button);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                //验证短信验证码有效性
                CodeValidateReqDto validateReqDto = new CodeValidateReqDto();
                if(smsCodeView.getText().toString().length() <= 0){
                    Toast toast = Toast.makeText(getApplicationContext(), "请输入有效验证码", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                }
                validateReqDto.setPhone(mobileView.getText().toString());

                validateReqDto.setSmsType(type);
                validateReqDto.setSmsCode(smsCodeView.getText().toString());
                ValidateSmsCodeThread validateSmsCodeThread = new ValidateSmsCodeThread(validateReqDto);
                validateSmsCodeThread.start();
            }
        });
    }

    // 构建Runnable对象，在runnable中更新界面  
    Runnable getValidateSuccess = new Runnable() {
        @Override
        public void run() {
            //页面跳转，并记录本地缓存文件
            Intent intent = new Intent(activity, SetPassWordActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("mobile", mobileView.getText().toString());
            bundle.putString("type", type);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    };

    /**
     * CountDownTimer 实现倒计时
     */
    private CountDownTimer countDownTimer = new CountDownTimer(60*1000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            String value = String.valueOf((int) (millisUntilFinished / 1000));
            getSmsBtn.setText("已发送("+value+")");
        }

        @Override
        public void onFinish() {
            getSmsBtn.setText("重新获取");
            getSmsBtn.setEnabled(true);
        }
    };

    //子线程：使用POST方法向服务器发送用数据
    class SmsCodeThread extends Thread {
        private SmsCodeReqDto smsCodeReqDto;

        public SmsCodeThread( SmsCodeReqDto smsCodeReqDto ) {
            this.smsCodeReqDto = smsCodeReqDto;
        }

        @Override
        public void run() {
            try {
                String path = "/sms/sendCode";
                String res = NetWorkUtils.postData(path, smsCodeReqDto);
                //解析Json串
                Gson gson = new Gson();
                java.lang.reflect.Type type = new TypeToken<ResultInfo>() {
                }.getType();
                ResultInfo resultInfo = gson.fromJson(res, type);
                res = (String) resultInfo.getData();
                if ("true".equals(res)) {
                    res = "获取短信验证码成功";
                } else {
                    res = resultInfo.getMessage();
                }
                Looper.prepare();
                Toast toast = Toast.makeText(getApplicationContext(), res, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                Looper.loop();
            } catch (Exception e) {
                e.printStackTrace();
                Looper.prepare();
                Toast toast = Toast.makeText(getApplicationContext(), "获取短信验证码失败，请检查网络是否可用", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                Looper.loop();
            }
        }
    }

    class ValidateSmsCodeThread extends Thread {
        private CodeValidateReqDto validateReqDto;

        public ValidateSmsCodeThread( CodeValidateReqDto validateReqDto ) {
            this.validateReqDto = validateReqDto;
        }

        @Override
        public void run() {
            try {
                String path = "/sms/validateSmsCode";
                String res = NetWorkUtils.postData(path, validateReqDto);
                //解析Json串
                Gson gson = new Gson();
                java.lang.reflect.Type type = new TypeToken<ResultInfo>() {
                }.getType();
                ResultInfo resultInfo = gson.fromJson(res, type);
                res = (String) resultInfo.getData();
                if ("true".equals(res)) {
                    res = "短信验证码验证成功";
                    handler.post(getValidateSuccess);
                } else {
                    res = "短信验证码不正确";
                }
                Looper.prepare();
                Toast toast = Toast.makeText(getApplicationContext(), res, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                Looper.loop();
            } catch (Exception e) {
                e.printStackTrace();
                Looper.prepare();
                Toast toast = Toast.makeText(getApplicationContext(), "获取短信验证码失败，请检查网络是否可用", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                Looper.loop();
            }
        }
    }
}
