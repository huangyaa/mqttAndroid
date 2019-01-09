package com.espressif.iot.esptouch.demo_activity;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.espressif.iot.esptouch.demo_activity.Utils.NetWorkUtils;
import com.espressif.iot.esptouch.demo_activity.entity.OprLogEntity;
import com.espressif.iot.esptouch.demo_activity.entity.ResultInfo;
import com.espressif.iot.esptouch.demo_activity.entity.TaskResInfo;
import com.espressif.iot_esptouch_demo.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scwang.smartrefresh.header.MaterialHeader;
import com.scwang.smartrefresh.header.WaveSwipeHeader;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;
import com.scwang.smartrefresh.layout.footer.BallPulseFooter;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.header.BezierRadarHeader;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;

import static com.espressif.iot.esptouch.demo_activity.mqtt_util.MqttManager.TAG;

public class OperateLogActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private String deviceCode;
    private OprLogAdapt oprLogAdapt;
    private Context context;
    private ProgressBar progressBar;
    private ImageView imageBack;
    private SmartRefreshLayout smartRefreshLayout;

    private Integer curPage = 1;
    private Integer curSize = 0;

    private List<OprLogEntity> oprLogEntityList = new ArrayList<>();
    //处理主线程UI刷新
    private Handler handler;

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operate_log);
        Bundle bundle = this.getIntent().getExtras();
        deviceCode = (String) bundle.get("deviceCode");
        handler = new Handler();
        context = this;
        //获取操作列表
        String path = "/log/get";
        GetLogThread getLogThread = new GetLogThread(path, deviceCode, curPage);
        getLogThread.start();

//        swipeRefreshLayout = findViewById(R.id.log_refresh);
//        swipeRefreshLayout.setOnRefreshListener(this);

//        progressBar = findViewById(R.id.log_progress);
//        progressBar.setVisibility(View.VISIBLE);
        oprLogAdapt = new OprLogAdapt(oprLogEntityList, this);
        recyclerView = findViewById(R.id.list_log);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(oprLogAdapt);
        oprLogAdapt.setOnItemClickListener(new OprLogAdapt.OnItemClickListener() {
            @Override
            public void onItemClick() {
            }

            @Override
            public void onItemLongClick() {

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
        smartRefreshLayout = findViewById(R.id.log_refresh);
        //设置 Header
        smartRefreshLayout.setRefreshHeader(new ClassicsHeader(this));
        //设置 Footer
        smartRefreshLayout.setRefreshFooter(new BallPulseFooter(this).setSpinnerStyle(SpinnerStyle.Scale));
        smartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh( RefreshLayout refreshlayout ) {
                refreshlayout.finishRefresh(2000);
                String path = "/log/get";
                curPage = 1;
                oprLogEntityList.clear();
                GetLogThread getLogThread = new GetLogThread(path, deviceCode, curPage);
                getLogThread.start();
            }
        });
        smartRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore( RefreshLayout refreshlayout ) {
                if(curSize < 10){
                    refreshlayout.finishLoadMore(true);
                }else{
                    String path = "/log/get";
                    curPage++;
                    GetLogThread getLogThread = new GetLogThread(path, deviceCode, curPage);
                    getLogThread.start();
                }
            }
        });
    }

//    @Override
//    public void onRefresh() {
//        //下拉刷新数据
//        String path = "/log/get";
//        GetLogThread getLogThread = new GetLogThread(path, deviceCode);
//        getLogThread.start();
//        swipeRefreshLayout.setRefreshing(false);
//        Toast toast = Toast.makeText(context, "刷新成功", Toast.LENGTH_SHORT);
//        toast.setGravity(Gravity.CENTER, 0, 0);
//        toast.show();
//    }

    // 构建Runnable对象，在runnable中更新界面  
    Runnable getLogList = new Runnable() {
        @Override
        public void run() {
            //更新界面
            oprLogAdapt.setOprLogEntitys(oprLogEntityList);
            oprLogAdapt.notifyDataSetChanged();
            smartRefreshLayout.finishRefresh(true);
            smartRefreshLayout.finishLoadMore(true);
        }
    };

    // 构建Runnable对象，在runnable中更新界面  
    Runnable requestFail = new Runnable() {
        @Override
        public void run() {
            smartRefreshLayout.finishRefresh(false);
            smartRefreshLayout.finishLoadMore(false);
        }
    };

    //子线程：通过GET方法向服务器发送信息获取列表
    class GetLogThread extends Thread {
        private String path;
        private String deviceCode;
        private Integer curPage;

        public GetLogThread( String path, String deviceCode, Integer curPage ) {
            this.path = path;
            this.curPage = curPage;
            this.deviceCode = deviceCode;
        }

        @Override
        public void run() {
            try {
                String result = NetWorkUtils.getData(path + "/" + deviceCode + "/" + curPage);
                //解析
                Gson gson = new Gson();
                java.lang.reflect.Type type = new TypeToken<ResultInfo<List<OprLogEntity>>>() {
                }.getType();
                ResultInfo<List<OprLogEntity>> resultInfos = gson.fromJson(result, type);
                //添加列表展示
                if (resultInfos.getData() != null) {
                    List<OprLogEntity> oprLogEntitys = resultInfos.getData();
                    curSize = oprLogEntitys.size();
                    oprLogEntityList.addAll(oprLogEntitys);
                    handler.post(getLogList);
                } else {
                    handler.post(requestFail);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Looper.prepare();
                Toast toast = Toast.makeText(context, "操作记录获取失败，请检查网络是否可用", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                Looper.loop();
            }

        }
    }
}
