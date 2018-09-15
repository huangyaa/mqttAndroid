package com.espressif.iot.esptouch.demo_activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.espressif.iot.esptouch.EsptouchTask;
import com.espressif.iot.esptouch.IEsptouchListener;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.espressif.iot.esptouch.IEsptouchTask;
import com.espressif.iot.esptouch.demo_activity.mqtt_util.MqttManager;
import com.espressif.iot.esptouch.task.__IEsptouchTask;
import com.espressif.iot.esptouch.util.ByteUtil;
import com.espressif.iot.esptouch.util.EspAES;
import com.espressif.iot.esptouch.util.EspNetUtil;
import com.espressif.iot_esptouch_demo.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EsptouchDemoActivity extends AppCompatActivity implements OnClickListener {
    private static final String TAG = "EsptouchDemoActivity";

    private static final boolean AES_ENABLE = false;// AES对称加密
    private static final String AES_SECRET_KEY = "1234567890123456"; // TODO modify your own key

    private TextView mApSsidTV;
    private TextView mApBssidTV;
    private EditText mApPasswordET;
    private EditText mDeviceCountET;
    private Button mConfirmBtn;
    private ListView listView;

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
        setContentView(R.layout.esptouch_demo_activity);
        listView = (ListView)findViewById(R.id.listView);
        mApSsidTV = findViewById(R.id.ap_ssid_text);
        mApBssidTV = findViewById(R.id.ap_bssid_text);
        mApPasswordET = findViewById(R.id.ap_password_edit);
        mDeviceCountET = findViewById(R.id.device_count_edit);
        mDeviceCountET.setText("1");
        mConfirmBtn = findViewById(R.id.confirm_btn);
        mConfirmBtn.setEnabled(false);
        mConfirmBtn.setOnClickListener(this);

        TextView versionTV = findViewById(R.id.version_tv);
        versionTV.setText(IEsptouchTask.ESPTOUCH_VERSION);

        IntentFilter filter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        registerReceiver(mReceiver, filter);
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
                new AlertDialog.Builder(EsptouchDemoActivity.this)
                        .setMessage("Wifi disconnected or changed")
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
//            if ((Boolean) mConfirmBtn.getTag()) {
//                Toast.makeText(this, R.string.wifi_5g_message, Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            byte[] ssid = mApSsidTV.getTag() == null ? ByteUtil.getBytesByString(mApSsidTV.getText().toString())
//                    : (byte[]) mApSsidTV.getTag();
//            byte[] password = ByteUtil.getBytesByString(mApPasswordET.getText().toString());
//            byte [] bssid = EspNetUtil.parseBssid2bytes(mApBssidTV.getText().toString());
//            byte[] deviceCount = mDeviceCountET.getText().toString().getBytes();
//
//            if(mTask != null) {
//                mTask.cancelEsptouch();
//            }
//            mTask = new EsptouchAsyncTask4(this,listView);
//            mTask.execute(ssid, bssid, password, deviceCount);
            //跳转到编辑页面，修改备注信息
            Intent intent = new Intent(EsptouchDemoActivity.this, DetailOperatorActicity.class);

            /* 通过Bundle对象存储需要传递的数据 */
            Bundle bundle = new Bundle();
            bundle.putString("bssid", "84:F3:EB:84:35:A1");
            bundle.putString("address", "192.168.0.102");
            bundle.putString("mark", "设备1");
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    private void onEsptoucResultAddedPerform(final IEsptouchResult result) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                String text = result.getBssid() + " is connected to the wifi";
                Toast.makeText(EsptouchDemoActivity.this, text,
                        Toast.LENGTH_LONG).show();
            }

        });
    }

    private  class EsptouchAsyncTask4 extends AsyncTask<byte[], Void, List<IEsptouchResult>> {
        private WeakReference<EsptouchDemoActivity> mActivity;

        // without the lock, if the user tap confirm and cancel quickly enough,
        // the bug will arise. the reason is follows:
        // 0. task is starting created, but not finished
        // 1. the task is cancel for the task hasn't been created, it do nothing
        // 2. task is created
        // 3. Oops, the task should be cancelled, but it is running
        private final Object mLock = new Object();
        private ProgressDialog mProgressDialog;
        private AlertDialog mResultDialog;
        private IEsptouchTask mEsptouchTask;
        private ListView listView;

        EsptouchAsyncTask4(EsptouchDemoActivity activity,ListView listView) {
            mActivity = new WeakReference<>(activity);
            this.listView = listView;
        }

        void cancelEsptouch() {
            cancel(true);
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
            if (mResultDialog != null) {
                mResultDialog.dismiss();
            }
            if (mEsptouchTask != null) {
                mEsptouchTask.interrupt();
            }
        }

        @Override
        protected void onPreExecute() {
            Activity activity = mActivity.get();
            mProgressDialog = new ProgressDialog(activity);
            mProgressDialog.setMessage("Esptouch is configuring, please wait for a moment...");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setOnCancelListener(new OnCancelListener() {//点击返回键事件监听
                @Override
                public void onCancel(DialogInterface dialog) {
                    synchronized (mLock) {
                        if (__IEsptouchTask.DEBUG) {
                            Log.i(TAG, "progress dialog back pressed canceled");
                        }
                        if (mEsptouchTask != null) {
                            mEsptouchTask.interrupt();
                        }
                    }
                }
            });
            mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, activity.getText(android.R.string.cancel),
                    new DialogInterface.OnClickListener() {//添加取消按钮监听事件
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            synchronized (mLock) {
                                if (__IEsptouchTask.DEBUG) {
                                    Log.i(TAG, "progress dialog cancel button canceled");
                                }
                                if (mEsptouchTask != null) {
                                    mEsptouchTask.interrupt();
                                }
                            }
                        }
                    });
            mProgressDialog.show();
        }

        @Override
        protected List<IEsptouchResult> doInBackground(byte[]... params) {
            EsptouchDemoActivity activity = mActivity.get();
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
            EsptouchDemoActivity activity = mActivity.get();
            mProgressDialog.dismiss();
            mResultDialog = new AlertDialog.Builder(activity)
                    .setPositiveButton(android.R.string.ok, null)
                    .create();
            mResultDialog.setCanceledOnTouchOutside(false);
            if (result == null) {
                mResultDialog.setMessage("Create Esptouch task failed, the esptouch port could be used by other thread");
                mResultDialog.show();
                return;
            }

            IEsptouchResult firstResult = result.get(0);
            // check whether the task is cancelled and no results received
            if (!firstResult.isCancelled()) {
                int count = 0;
                // max results to be displayed, if it is more than maxDisplayCount,
                // just show the count of redundant ones
                final int maxDisplayCount = 10;
                // the task received some results including cancelled while
                // executing before receiving enough results
                if (firstResult.isSuc()) {
                    StringBuilder sb = new StringBuilder();
                    List<HashMap<String, Object>> data = new ArrayList<HashMap<String,Object>>();
                    for (IEsptouchResult resultInList : result) {//需要处理mac地址，获得的mac地址没有：
//                        sb.append("Esptouch success, bssid = ")
////                                .append(resultInList.getBssid())
////                                .append(", InetAddress = ")
////                                .append(resultInList.getInetAddress().getHostAddress())
////                                .append("\n");
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
                        //获取到集合数据
                        HashMap<String, Object> item = new HashMap<String, Object>();
                        item.put("id", count);
                        item.put("bssid", bssid);
                        item.put("address", resultInList.getInetAddress().getHostAddress());

                        SharedPreferences sp = getSharedPreferences("devConfig", Context.MODE_PRIVATE);
                        String name = sp.getString(resultInList.getBssid(), null);
                        if(name != null){
                            item.put("mark", name);
                        }else{
                            item.put("mark", "设备"+count);
                        }
                        data.add(item);

                        count++;
                        if (count >= maxDisplayCount) {
                            break;
                        }
                    }
                    //创建SimpleAdapter适配器将数据绑定到item显示控件上
                    SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(), data, R.layout.item,
                            new String[]{"bssid", "address", "mark"}, new int[]{R.id.bssid, R.id.address, R.id.mark});
                    //实现列表的显示
                    listView.setAdapter(adapter);
                    //条目点击事件
                    listView.setOnItemClickListener(new ItemClickListener());
                    if (count < result.size()) {
                        sb.append("\nthere's ")
                                .append(result.size() - count)
                                .append(" more result(s) without showing\n");//显示多余未显示的设备数
                        mResultDialog.setMessage(sb.toString());//配网成功
                    }
                } else {
                    mResultDialog.setMessage("Esptouch fail");//成功但没有返回值
                }

                mResultDialog.show();
            }

            activity.mTask = null;
        }
    }

    //获取条目点击事件
    private final  class ItemClickListener implements AdapterView.OnItemClickListener {

        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ListView listView = (ListView) parent;
            HashMap<String, Object> data = (HashMap<String, Object>) listView.getItemAtPosition(position);
            String deviceId = data.get("id").toString();
            Toast.makeText(getApplicationContext(), deviceId, Toast.LENGTH_LONG).show();
            //跳转到编辑页面，修改备注信息
            Intent intent = new Intent(EsptouchDemoActivity.this, DetailOperatorActicity.class);

            /* 通过Bundle对象存储需要传递的数据 */
            Bundle bundle = new Bundle();
            bundle.putString("bssid", data.get("bssid").toString());
            bundle.putString("address", data.get("address").toString());
            bundle.putString("mark", data.get("mark").toString());
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

}
