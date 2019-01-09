package com.espressif.iot.esptouch.demo_activity;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.espressif.iot.esptouch.demo_activity.Utils.NetWorkUtils;
import com.espressif.iot.esptouch.demo_activity.Utils.PickerUtilClass;
import com.espressif.iot.esptouch.demo_activity.entity.ResultInfo;
import com.espressif.iot.esptouch.demo_activity.entity.TaskClassEntity;
import com.espressif.iot.esptouch.task.__IEsptouchTask;
import com.espressif.iot_esptouch_demo.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TaskSetActivity extends AppCompatActivity {
    private ImageView imageBack;
    private TextView deviceView;
    private Spinner switchNameView;
    private Spinner remaindTypeView;
    private Spinner commandView;
    private DatePicker dateView;
    private TimePicker timeView;
    private Button confirmBtn;
    private ArrayAdapter<String> switchAdapter;
    private Context context;
    private ProgressBar addProgress;
    //下拉列表选中的数据
    private String deviceCode;
    private int switchPos;
    private String name;
    private String type;
    private String command;
    private String date;
    private String time;
    private String mobile;

    //处理主线程UI刷新
    private Handler handler;

    //消息提示框
    private AlertDialog.Builder ab;

    //定时器内容
    private TaskClassEntity taskClassEntity;


    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_set);
        handler = new Handler();
        ab =new AlertDialog.Builder(this);  //(普通消息框)
        //初始化界面控件
        deviceView = (TextView)findViewById(R.id.input_deviceId);
        switchNameView = (Spinner)findViewById(R.id.spinner_switch);
        remaindTypeView = (Spinner)findViewById(R.id.spinner_remaind);
        commandView = (Spinner)findViewById(R.id.spinner_command);
        dateView = (DatePicker)findViewById(R.id.datePicker);
        timeView = (TimePicker)findViewById(R.id.timePicker);
        confirmBtn = (Button)findViewById(R.id.confirm_btn_timer);
        //设置时间控件字体大小
        PickerUtilClass.resizePicker(dateView);
        PickerUtilClass.resizePicker(timeView);

        Bundle bundle = this.getIntent().getExtras();
        deviceCode = (String) bundle.get("deviceCode");
        mobile = bundle.getString("mobile");
        //设置开关名称下拉列表框数据
        List<String> switchs = bundle.getStringArrayList("swtithNames");
        deviceView.setText(deviceCode);
        switchAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, switchs);
        switchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        switchNameView.setAdapter(switchAdapter);
        switchNameView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                 @Override
                 public void onItemSelected( AdapterView<?> parent, View view, int position, long id ) {
                     name = (String) switchNameView.getItemAtPosition(position);//从spinner中获取被选择的数据
                     switchPos = position + 1;
                 }

                 @Override
                 public void onNothingSelected( AdapterView<?> parent ) {
                     name = (String) switchNameView.getItemAtPosition(0);//从spinner中获取被选择的数据
                     switchPos = 1;
                 }
             }

        );
        remaindTypeView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected( AdapterView<?> parent, View view, int position, long id ) {
                String typePaste = (String) remaindTypeView.getItemAtPosition(position);//从spinner中获取被选择的数据
                type = position+"";
                if (type.equals("1")) {
                    //1 为永久
                    dateView.setVisibility(View.GONE);
                } else {
                    //0 为一次
                    dateView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected( AdapterView<?> parent ) {

            }
        });

        commandView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected( AdapterView<?> parent, View view, int position, long id ) {
                String typePaste = (String) commandView.getItemAtPosition(position);//从spinner中获取被选择的数据
                command = position+"";
                if (command.equals("0")) {
                    //0 为打开
                    command = "p";
                } else {
                    //1 为关闭
                    command = "n";
                }
            }

            @Override
            public void onNothingSelected( AdapterView<?> parent ) {

            }
        });
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        PickerUtilClass.resizePicker(dateView);
        dateView.init(year, month, day, new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged( DatePicker view, int year, int monthOfYear, int dayOfMonth ) {
                // 获取一个日历对象，并初始化为当前选中的时间
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, monthOfYear, dayOfMonth);
                SimpleDateFormat format = new SimpleDateFormat(
                        "yyyy-MM-dd");
                date = format.format(calendar.getTime());
            }
        });
        PickerUtilClass.resizePicker(timeView);
        timeView.setIs24HourView(true);
        timeView.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged( TimePicker view, int hourOfDay, int minute ) {
                if(minute < 10){
                    time = hourOfDay + ":0" + minute + ":00";
                }else{
                    time = hourOfDay + ":" + minute + ":00";
                }

            }
        });
        addProgress = findViewById(R.id.add_progress);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                //发送请求到后台；

                taskClassEntity = new TaskClassEntity();
                taskClassEntity.setDeviceCode(deviceCode);
                taskClassEntity.setType(type);
                //设置开命令
                taskClassEntity.setCommand(command + switchPos);
                //设置默认date，time必须设置
                if(time == null){
                    Toast toast = Toast.makeText(TaskSetActivity.this, "请选择有效时间", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                }
                if(date == null && type.equals("0")){
//                    Toast toast = Toast.makeText(TaskSetActivity.this, "请选择有效日期", Toast.LENGTH_SHORT);
//                    toast.setGravity(Gravity.CENTER, 0, 0);
//                    toast.show();
//                    return;
                    Date curDate = new Date();
                    SimpleDateFormat format = new SimpleDateFormat(
                            "yyyy-MM-dd");
                    date = format.format(curDate);
                }
                if(type.equals("0")){
                    taskClassEntity.setExcuDate(date +" "+time);
                }else{
                    taskClassEntity.setExcuTime(time);
                }

                taskClassEntity.setSwitchName(name);
                taskClassEntity.setMobile(mobile);
//                String params = "deviceCode="+deviceCode+"&switchPos="+switchPos+"&name="+name
//                                + "&type="+type + "&date="+date+" "+time;
//                Toast.makeText(TaskSetActivity.this, params, Toast.LENGTH_SHORT).show();

                ab.setTitle("定时器设置");  //设置标题
                ab.setMessage("设置的时间：" + date + " "+ time);//设置消息内容
                ab.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick( DialogInterface dialog, int which ) {
                        //开启发送线程
                        new PostThread(taskClassEntity).start();
                        addProgress.setVisibility(View.VISIBLE);
                    }
                });//设置确定按钮
                ab.setNegativeButton("取消",null);//设置取消按钮
                ab.show();//显示弹出框
            }
        });
        imageBack = findViewById(R.id.image_back);
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                //销毁，再次打开从新create
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    //隐藏界面progressBar
    Runnable hideProgressBar = new Runnable() {
        @Override
        public void run() {
            //更新界面
            addProgress.setVisibility(View.GONE);
        }
    };

    //子线程：使用POST方法向服务器数据
    class PostThread extends Thread {

        private TaskClassEntity taskClassEntity;

        public PostThread(TaskClassEntity taskClassEntity) {
            this.taskClassEntity = taskClassEntity;
        }

        @Override
        public void run() {
            try {
                String path = "/timer/add/command";
                String res = NetWorkUtils.postData(path,taskClassEntity);
                //解析Json串
                Gson gson = new Gson();
                java.lang.reflect.Type type = new TypeToken<ResultInfo>() {
                }.getType();
                ResultInfo resultInfo = gson.fromJson(res, type);
                res = (String) resultInfo.getData();
                if("true".equals(res)){
                    res = "定时器设置成功";
                }else{
                    res =resultInfo.getMessage();
                }
                handler.post(hideProgressBar);
                Looper.prepare();
                Toast toast = Toast.makeText(getApplicationContext(), res, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                Looper.loop();
            } catch (Exception e) {
                e.printStackTrace();
                //非主线程中调用toast需要加上此方式
                Looper.prepare();
                Toast toast = Toast.makeText(getApplicationContext(), "定时器设置发生异常，请检查网络是否可用", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                Looper.loop();
            }
        }
    }
}
