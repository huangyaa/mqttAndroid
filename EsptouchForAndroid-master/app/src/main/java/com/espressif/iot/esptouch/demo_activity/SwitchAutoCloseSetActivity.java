package com.espressif.iot.esptouch.demo_activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.espressif.iot.esptouch.demo_activity.AutoCloseAdapt;
import com.espressif.iot.esptouch.demo_activity.entity.AutoSwitchSetEntity;
import com.espressif.iot_esptouch_demo.R;

import java.util.ArrayList;
import java.util.List;

public class SwitchAutoCloseSetActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageView imageBack;
    private Spinner switchNameView;
    private ArrayAdapter<String> switchAdapter;
    private EditText delayView;
    private Button confirmBtn;

    private AutoCloseAdapt setSwitchAdapt;
    private RecyclerView recyclerView;

    private String deviceCode;
    private Integer switchPos;
    private Context context;

    List<AutoSwitchSetEntity> switchSetEntitys;
    List<String> switchs;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.switch_auto_close_set);
        context = this;
        Bundle bundle = this.getIntent().getExtras();
        deviceCode = (String) bundle.get("deviceCode");
        //设置开关名称下拉列表框数据
        switchs = bundle.getStringArrayList("switchNames");
        switchNameView = findViewById(R.id.spinner_switch);
        delayView = findViewById(R.id.delay_second);
        switchAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, switchs);
        switchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        switchNameView.setAdapter(switchAdapter);
        switchNameView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                     @Override
                                                     public void onItemSelected( AdapterView<?> parent, View view, int position, long id ) {
                                                         switchPos = position + 1;
                                                     }

                                                     @Override
                                                     public void onNothingSelected( AdapterView<?> parent ) {
                                                         switchPos = 1;
                                                     }
                                                 }

        );
        confirmBtn = findViewById(R.id.confirm_btn);
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                String delayTime = delayView.getText().toString();
                try {
                    Integer.parseInt(delayTime);
                } catch (Exception e) {
                    Toast toast = Toast.makeText(getApplicationContext(), "请输入有效的延迟时间", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                }
//                if(delayTime.isEmpty()){
//                    Toast toast = Toast.makeText(getApplicationContext(), "请输入有效的延迟时间", Toast.LENGTH_SHORT);
//                    toast.setGravity(Gravity.CENTER, 0, 0);
//                    toast.show();
//                    return;
//                }
                //保存设置，添加临时文件，刷新列表 key:deviceCode+auto_+id;value:延迟时间_配置状态.id从1开始;开关状态：0：关,1:打开
                SharedPreferences sp = context.getSharedPreferences("AutoSwitchConfig", MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(deviceCode + "auto_" + switchPos, delayTime + "_0");
                editor.commit();
                fillData(switchSetEntitys);
                setSwitchAdapt.setmValues(switchSetEntitys);
                setSwitchAdapt.notifyDataSetChanged();
                Toast toast = Toast.makeText(getApplicationContext(), "保存成功", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });

        //列表数据从临时文件中读取
        switchSetEntitys = new ArrayList<>();
        fillData(switchSetEntitys);

        recyclerView = findViewById(R.id.recycle_set_switch);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        setSwitchAdapt = new AutoCloseAdapt(switchSetEntitys, context);
        setSwitchAdapt.setOnItemClickListener(new AutoCloseAdapt.OnItemClickListener() {
            @Override
            public void onItemClick( View view, int taskId, String delayTime, String status ) {
                String res = "";
                SharedPreferences sp = context.getSharedPreferences("AutoSwitchConfig", MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                if (view.getId() == R.id.task_status) {
                    //修改开关状态
                    if ("0".equals(status)) {
                        editor.putString(deviceCode + "auto_" + taskId, delayTime + "_1");
                        editor.commit();
                    } else {
                        editor.putString(deviceCode + "auto_" + taskId, delayTime + "_0");
                        editor.commit();
                    }
                    res = "修改状态成功";
                }else{
                    //删除指定数据
                    editor.remove(deviceCode + "auto_" + taskId);
                    editor.commit();
                    res = "删除成功";
                }
                fillData(switchSetEntitys);
                setSwitchAdapt.setmValues(switchSetEntitys);
                setSwitchAdapt.notifyDataSetChanged();
                Toast toast = Toast.makeText(getApplicationContext(), res, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }

            @Override
            public void onItemLongClick( View view ) {

            }
        });
        recyclerView.setAdapter(setSwitchAdapt);

        imageBack = findViewById(R.id.image_back);
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                //销毁，再次打开从新create
                finish();
            }
        });
    }

    /**
     * 缓存数据填充
     *
     * @param switchSetEntitys
     */
    private void fillData( List<AutoSwitchSetEntity> switchSetEntitys ) {
        switchSetEntitys.clear();
        SharedPreferences sp = context.getSharedPreferences("AutoSwitchConfig", MODE_PRIVATE);
        for (int i = 0; i < switchs.size(); i++) {
            AutoSwitchSetEntity switchSetEntity = new AutoSwitchSetEntity();
            int position = i + 1;
            String value = sp.getString(deviceCode + "auto_" + position, null);
            if (value != null) {
                String[] strs = value.split("_");
                switchSetEntity.setId(position + "");
                switchSetEntity.setSwitchName(switchs.get(i));
                switchSetEntity.setDelayTime(strs[0]+"s");
                switchSetEntity.setTaskStatus(strs[1]);
                switchSetEntitys.add(switchSetEntity);
            }
        }
    }

    @Override
    public void onClick( View v ) {

    }
}
