package com.espressif.iot.esptouch.demo_activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.espressif.iot.esptouch.EsptouchTask;
import com.espressif.iot.esptouch.IEsptouchListener;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.espressif.iot.esptouch.IEsptouchTask;
import com.espressif.iot.esptouch.task.__IEsptouchTask;
import com.espressif.iot.esptouch.util.ByteUtil;
import com.espressif.iot.esptouch.util.EspAES;
import com.espressif.iot.esptouch.util.EspNetUtil;
import com.espressif.iot_esptouch_demo.R;
import com.ldoublem.loadingviewlib.view.LVCircularJump;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AddDeviceActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "AddDeviceActivity";

    private static final boolean AES_ENABLE = false;// AES对称加密
    private static final String AES_SECRET_KEY = "1234567890123456"; // TODO modify your own key

    private TextView mApSsidTV;
    private TextView mApBssidTV;
    private EditText mApPasswordET;
    private Button mConfirmBtn;
    private ImageView imageBack;
    private ImageView rememberView;
    private String mobile;
    private int curSetCount = 1;
    private boolean rememberStatus = true;
    private LinearLayout firstLayout;
    private LinearLayout secondLayout;
    private LinearLayout errorLayout;
    private TextView configTimeView;

    private LVCircularJump mLVCircularJump;
    private Context ctx;
    private SharedPreferences sp;

    private String step = "first";

    //消息提示框
    private AlertDialog.Builder ab;

    private IEsptouchListener myListener = new IEsptouchListener() {

        @Override
        public void onEsptouchResultAdded(final IEsptouchResult result) {
            onEsptoucResultAddedPerform(result);
        }
    };

    private EsptouchAsyncTask4 mTask;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }

            switch (action) {
                case WifiManager.NETWORK_STATE_CHANGED_ACTION:
                    WifiInfo wifiInfo = intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                    onWifiChanged(wifiInfo);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_device);
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            mobile = bundle.getString("mobile");
        }
        ctx = this;

        firstLayout = (LinearLayout) findViewById(R.id.first);
        secondLayout = (LinearLayout) findViewById(R.id.second);
        errorLayout = (LinearLayout)findViewById(R.id.error);
        firstLayout.setVisibility(View.VISIBLE);
        secondLayout.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);

        mApSsidTV = findViewById(R.id.ap_ssid_text);
        mApBssidTV = findViewById(R.id.ap_bssid_text);
        mApPasswordET = findViewById(R.id.ap_password_edit);
        mConfirmBtn = findViewById(R.id.confirm_btn);
        mConfirmBtn.setEnabled(false);
        mConfirmBtn.setOnClickListener(this);
        imageBack = findViewById(R.id.image_back);
        imageBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                finish();
            }
        });
        rememberView = (ImageView) findViewById(R.id.remember);
        rememberView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick( View v ) {
                if(rememberStatus){
                    rememberView.setImageResource(R.drawable.radio_black);
                    rememberStatus = false;
                }else{
                    rememberView.setImageResource(R.drawable.radio_blue);
                    rememberStatus = true;
                }
            }
        });
        configTimeView = (TextView) findViewById(R.id.configTime);
        mLVCircularJump = (LVCircularJump) findViewById(R.id.lv_circularJump);
        mLVCircularJump.setViewColor(Color.rgb(135, 206, 250));
        ab =new AlertDialog.Builder(this);  //(普通消息框)
        IntentFilter filter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(mReceiver, filter);
        sp = ctx.getSharedPreferences("jdcCacheInfo", MODE_PRIVATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(mReceiver);
    }

    private void onWifiChanged(WifiInfo info) {
        if (info == null) {
            mApSsidTV.setText("");
            mApSsidTV.setTag(null);
            mApBssidTV.setTag("");
            mConfirmBtn.setEnabled(false);
            mConfirmBtn.setTag(null);

            if (mTask != null) {
                mTask.cancelEsptouch();
                mTask = null;
                new AlertDialog.Builder(AddDeviceActivity.this)
                        .setMessage("Wifi 连接失败")
                        .setNegativeButton(android.R.string.cancel, null)
                        .show();
            }
        } else {
            String ssid = info.getSSID();
            if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
                ssid = ssid.substring(1, ssid.length() - 1);
            }
            mApSsidTV.setText(ssid);
            mApSsidTV.setTag(ByteUtil.getBytesByString(ssid));
            byte[] ssidOriginalData = EspUtils.getOriginalSsidBytes(info);
            mApSsidTV.setTag(ssidOriginalData);

            String bssid = info.getBSSID();
            mApBssidTV.setText(bssid);

            String wifiPw = sp.getString(ssid,"");
            mApPasswordET.setText(wifiPw);

            mConfirmBtn.setEnabled(true);
            mConfirmBtn.setTag(Boolean.FALSE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int frequence = info.getFrequency();
                if (frequence > 4900 && frequence < 5900) {
                    // Connected 5G wifi. Device does not support 5G
                    mConfirmBtn.setTag(Boolean.TRUE);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mConfirmBtn) {
            if(step.equals("first")){
                if ((Boolean) mConfirmBtn.getTag()) {
                    Toast.makeText(this, R.string.wifi_5g_message, Toast.LENGTH_SHORT).show();
                    return;
                }
                if(mApPasswordET.getText().toString().isEmpty()){
                    Toast.makeText(this, "密码不能为空，请输入有效密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                //存wifi密码
                if(rememberStatus){
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString(mApSsidTV.getText().toString(),mApPasswordET.getText().toString());
                    editor.commit();
                }
                firstLayout.setVisibility(View.GONE);
                secondLayout.setVisibility(View.VISIBLE);
                errorLayout.setVisibility(View.GONE);
                mLVCircularJump.startAnim();

                byte[] ssid = mApSsidTV.getTag() == null ? ByteUtil.getBytesByString(mApSsidTV.getText().toString())
                        : (byte[]) mApSsidTV.getTag();
                byte[] password = ByteUtil.getBytesByString(mApPasswordET.getText().toString());
                byte [] bssid = EspNetUtil.parseBssid2bytes(mApBssidTV.getText().toString());
                byte[] deviceCount = (curSetCount+"").getBytes();

                if(mTask != null) {
                    mTask.cancelEsptouch();
                }
                mTask = new EsptouchAsyncTask4(this);
                mTask.execute(ssid, bssid, password, deviceCount);
                step = "second";
                mConfirmBtn.setText("取消");
                countDownTimer.start();
            }else if(step.equals("second")){
                synchronized (mTask.getmLock()) {
                    if (__IEsptouchTask.DEBUG) {
                        Log.i(TAG, "progress dialog back pressed canceled");
                    }
                    if (mTask.getmEsptouchTask()!= null) {
                        mTask.getmEsptouchTask().interrupt();
                    }
                }
                firstLayout.setVisibility(View.GONE);
                secondLayout.setVisibility(View.GONE);
                errorLayout.setVisibility(View.VISIBLE);
                mConfirmBtn.setText("重新开始");
                mLVCircularJump.stopAnim();
                step = "error";
            }else if(step.equals("error")){
                firstLayout.setVisibility(View.GONE);
                secondLayout.setVisibility(View.VISIBLE);
                errorLayout.setVisibility(View.GONE);
                mLVCircularJump.startAnim();

                byte[] ssid = mApSsidTV.getTag() == null ? ByteUtil.getBytesByString(mApSsidTV.getText().toString())
                        : (byte[]) mApSsidTV.getTag();
                byte[] password = ByteUtil.getBytesByString(mApPasswordET.getText().toString());
                byte [] bssid = EspNetUtil.parseBssid2bytes(mApBssidTV.getText().toString());
                byte[] deviceCount = (curSetCount+"").getBytes();

                if(mTask != null) {
                    mTask.cancelEsptouch();
                }
                mTask = new EsptouchAsyncTask4(this);
                mTask.execute(ssid, bssid, password, deviceCount);
                step = "second";
                mConfirmBtn.setText("取消");
            }
        }
    }

    private void onEsptoucResultAddedPerform(final IEsptouchResult result) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
//                String text = result.getBssid() + " 连上了wifi";
//                Toast.makeText(AddDeviceActivity.this, text,
//                        Toast.LENGTH_LONG).show();
            }

        });
    }

    private  class EsptouchAsyncTask4 extends AsyncTask<byte[], Void, List<IEsptouchResult>> {
        private WeakReference<AddDeviceActivity> mActivity;

        // without the lock, if the user tap confirm and cancel quickly enough,
        // the bug will arise. the reason is follows:
        // 0. task is starting created, but not finished
        // 1. the task is cancel for the task hasn't been created, it do nothing
        // 2. task is created
        // 3. Oops, the task should be cancelled, but it is running
        private final Object mLock = new Object();
//        private ProgressDialog mProgressDialog;
        private AlertDialog mResultDialog;
        private IEsptouchTask mEsptouchTask;

        public Object getmLock() {
            return mLock;
        }

        public IEsptouchTask getmEsptouchTask() {
            return mEsptouchTask;
        }

        EsptouchAsyncTask4(AddDeviceActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        void cancelEsptouch() {
            cancel(true);
//            if (mProgressDialog != null) {
//                mProgressDialog.dismiss();
//            }
            if (mResultDialog != null) {
                mResultDialog.dismiss();
            }
            if (mEsptouchTask != null) {
                mEsptouchTask.interrupt();
            }
        }

        @Override
        protected void onPreExecute() {
//            Activity activity = mActivity.get();
//            mProgressDialog = new ProgressDialog(activity);
////            mProgressDialog.setMessage("正在配置网络, 请等待");
////            mProgressDialog.setCanceledOnTouchOutside(false);
////            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {//点击返回键事件监听
////                @Override
////                public void onCancel(DialogInterface dialog) {
////                            synchronized (mLock) {
////                                if (__IEsptouchTask.DEBUG) {
////                                    Log.i(TAG, "progress dialog back pressed canceled");
////                                }
////                                if (mEsptouchTask != null) {
////                                    mEsptouchTask.interrupt();
////                                }
////                            }
////                            mLVCircularJump.stopAnim();
////                        }
////                    });//设置确定按钮
////            mProgressDialog.show();
        }

        @Override
        protected List<IEsptouchResult> doInBackground(byte[]... params) {
            AddDeviceActivity activity = mActivity.get();
            int taskResultCount;
            synchronized (mLock) {
                // !!!NOTICE
                byte[] apSsid = params[0];
                byte[] apBssid = params[1];
                byte[] apPassword = params[2];
                byte[] deviceCountData = params[3];
                taskResultCount = deviceCountData.length == 0 ? -1 : Integer.parseInt(new String(deviceCountData));
                Context context = activity.getApplicationContext();
                if (AES_ENABLE) {
                    byte[] secretKey = AES_SECRET_KEY.getBytes();
                    EspAES aes = new EspAES(secretKey);
                    mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword, aes, context);
                } else {
                    mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword, null, context);
                }
                mEsptouchTask.setEsptouchListener(activity.myListener);
            }
            return mEsptouchTask.executeForResults(taskResultCount);
        }

        @Override
        protected void onPostExecute(List<IEsptouchResult> result) {
            AddDeviceActivity activity = mActivity.get();
       //     mProgressDialog.dismiss();
            mLVCircularJump.stopAnim();

            mResultDialog = new AlertDialog.Builder(activity)
                    .setPositiveButton(android.R.string.ok, null)
                    .create();
            mResultDialog.setCanceledOnTouchOutside(false);
            if (result == null) {
                mResultDialog.setMessage("扫描设备失败，端口被占用");
                mResultDialog.show();
                return;
            }

            IEsptouchResult firstResult = result.get(0);
            // check whether the task is cancelled and no results received
            if (!firstResult.isCancelled()) {
                int count = 0;
                int totalCount = 0;
                // max results to be displayed, if it is more than maxDisplayCount,
                // just show the count of redundant ones
                final int maxDisplayCount = curSetCount;
                // the task received some results including cancelled while
                // executing before receiving enough results
                if (firstResult.isSuc()) {
                    StringBuilder sb = new StringBuilder();
                    List<HashMap<String, Object>> data = new ArrayList<HashMap<String,Object>>();
                    for (IEsptouchResult resultInList : result) {
                        //需要处理mac地址，获得的mac地址没有：
                        String bssid = resultInList.getBssid();
                        StringBuffer mac = new StringBuffer();
                        int begin = 0;
                        if(!bssid.contains(":")){
                            while (begin < bssid.length()){
                                mac = mac.append(bssid.substring(begin,begin+2));
                                if(begin != bssid.length() - 2){
                                    mac.append(":");
                                }
                                begin = begin + 2;
                            }
                        }
                        //跳转到编辑页面，修改备注信息
                        Intent intent = new Intent(AddDeviceActivity.this, DetailOperatorActicity.class);

                        /* 通过Bundle对象存储需要传递的数据 */
                        Bundle bundle = new Bundle();
                        bundle.putString("bssid", mac.toString().toString());
                        bundle.putString("mark", "未配置");
                        bundle.putString("type", "1");
                        bundle.putString("mobile", mobile);
                        intent.putExtras(bundle);
                        startActivity(intent);

//                        SharedPreferences sp = getSharedPreferences("devConfig", Context.MODE_PRIVATE);
//                        //wifi设备用mac地址标识，带：
//                        String name = sp.getString(mac.toString(), null);
//                        if(name == null) {
                            //说明未被配置过
//                            HashMap<String, Object> item = new HashMap<String, Object>();
//                            item.put("id", count);
//                            item.put("bssid", mac.toString());
//                            item.put("mark", "未配置");
//                            data.add(item);
//                            count++;
////                        }
                        totalCount ++;
                        if (totalCount >= maxDisplayCount) {
                            mResultDialog.setMessage("设备超过扫描数量限制，请关闭多余的设备");
                            break;
                        }
                    }
//                    //创建SimpleAdapter适配器将数据绑定到item显示控件上
//                    SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(), data, R.layout.item,
//                            new String[]{"bssid", "id","mark"}, new int[]{R.id.bssid, R.id.id,R.id.mark});
//                    //实现列表的显示
//                    listView.setAdapter(adapter);
//                    //条目点击事件
//                    listView.setOnItemClickListener(new ItemClickListener());
//                    if (count < result.size()) {
//                        sb.append("\n还有 ")
//                                .append(result.size() - count)
//                                .append(" 个设备没有展示\n");//显示多余未显示的设备数
//                        mResultDialog.setMessage(sb.toString());//配网成功
//                    }
                } else {
                    mResultDialog.setMessage("扫描设备失败");//成功但没有返回值
                }

                mResultDialog.show();
            }

            activity.mTask = null;
        }
    }

//    //获取条目点击事件
//    private final  class ItemClickListener implements AdapterView.OnItemClickListener {
//
//        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            ListView listView = (ListView) parent;
//            HashMap<String, Object> data = (HashMap<String, Object>) listView.getItemAtPosition(position);
//            String deviceId = data.get("id").toString();
//            Toast.makeText(getApplicationContext(), deviceId, Toast.LENGTH_LONG).show();
//            //跳转到编辑页面，修改备注信息
//            Intent intent = new Intent(AddDeviceActivity.this, DetailOperatorActicity.class);
//
//            /* 通过Bundle对象存储需要传递的数据 */
//            Bundle bundle = new Bundle();
//            bundle.putString("bssid", data.get("bssid").toString());
//            bundle.putString("mark", data.get("mark").toString());
//            bundle.putString("type", "1");
//            bundle.putString("mobile", mobile);
//            intent.putExtras(bundle);
//            startActivity(intent);
//        }
//    }
    /**
     * CountDownTimer 实现倒计时
     */
    private CountDownTimer countDownTimer = new CountDownTimer(60*1000, 1000) {
        @Override
        public void onTick(long millisUntilFinished) {
            String value = String.valueOf((int) (millisUntilFinished / 1000));
            configTimeView.setText("("+value+"s)");
        }

        @Override
        public void onFinish() {

        }
    };
}
