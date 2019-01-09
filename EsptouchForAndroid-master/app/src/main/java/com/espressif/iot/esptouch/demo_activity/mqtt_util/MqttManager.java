package com.espressif.iot.esptouch.demo_activity.mqtt_util;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.espressif.iot.esptouch.demo_activity.MyItemRecyclerViewAdapter;
import com.espressif.iot.esptouch.demo_activity.Utils.DeviceUtils;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class MqttManager {
    public static final String TAG = MqttManager.class.getSimpleName();

    private String host = "tcp://yun.lytldz.com:1883";
    private String userName = "admin";
    private String passWord = "adminxy005";
    private String clientId = "";
    private Context context;
    private boolean useConnect = false;

    private TimerTask task;
    private static Timer timer = new Timer();

    private Map<String, String> devices;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage( android.os.Message msg ) {  //这个是发送过来的消息
            // Log.i(TAG, msg.what + "");
            // 处理从子线程发送过来的消息
            if (null != devices) {
                Map<String, Integer> checkNums = new HashMap<>();
                Set<String> ids = devices.keySet();
                for (String id : ids) {
                    checkNums.put(id, 0);
                }
                //循环遍历map集合
                Iterator<Map.Entry<String, String>> iterator = devices.entrySet().iterator();
                    while (devices.size() > 0 ) {
                        Map.Entry<String, String> entry;
                        if(iterator.hasNext()){
                            entry = iterator.next();
                        }else{
                            iterator = devices.entrySet().iterator();
                            entry = iterator.next();
                            try {
                                Thread.sleep(400);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                Log.e("mqttManager","睡眠线程被异常中断");
                            }
                        }

                    String id = entry.getKey();
                    String[] patams = devices.get(id).split("-");
                    if ("ON".equals(patams[1])) {
                        iterator.remove();
                        unSubscribe(id + "/drive");
                        recyclerViewAdapter.notifyItemRangeChanged(new Integer(patams[0]), 1, "ON");
                    } else {
                        Integer num = checkNums.get(id);
                        if (num > 2) {
                            iterator.remove();
                        } else {
                            checkNums.put(id, ++num);
                        }
                    }
                }
            }
            task = null;
        }

        ;
    };
    // public static final String TOPIC = "test";

    private static MqttManager mqttManager = null;
    private MqttAndroidClient client;
    private MqttConnectOptions connectOptions;

    private MyItemRecyclerViewAdapter recyclerViewAdapter;

//    public void setUseConnect( boolean useConnect){
//        this.useConnect = useConnect;
//    }

    private MqttManager( Context context ) {
        this.context = context;
        clientId = MqttClient.generateClientId() + Build.SERIAL;
          /*
            对于采用mqttClient如果不指定MqttClientPersistence存储对象，默认采用MqttDefaultFilePersistence实现类产生存储对象
             */
        // client = new MqttClient(host,clientId,new MemoryPersistence());
            /*MqttClientPersistence,当qos不等于0的时候需要，因为要进行数据重传，
            如果现在不指定对象，调用的函数会自动产生 MemoryPersistence实现类的对象*/
        client = new MqttAndroidClient(context, host, clientId);
        connectOptions = new MqttConnectOptions();
        connectOptions.setUserName(userName);
        connectOptions.setPassword(passWord.toCharArray());
        // 设置超时时间 单位为秒
        connectOptions.setConnectionTimeout(30);
        // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
        connectOptions.setKeepAliveInterval(30);
        //设置最终端口的通知消息,客户端意外掉线时，topic推送的消息，第一次在上线时会收到此推送
        // connectOptions.setWill(TOPIC, "the client will stop !".getBytes(), 0, false);
        client.setCallback(mqttCallback);
    }

    public void setRecyclerViewAdapter( MyItemRecyclerViewAdapter recyclerViewAdapter ) {
        this.recyclerViewAdapter = recyclerViewAdapter;
    }

    //设置当前需要监听的设备
    public void setDevices( Map<String, String> devices ) {
        this.devices = devices;
    }

    public static MqttManager getInstance( Context context ) {
        if (mqttManager == null) {
            mqttManager = new MqttManager(context);
        }
        return mqttManager;
    }

    public void connect() {
        try {
            if (!useConnect) {
                client.connect(connectOptions,null, iMqttActionListener);
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribe( String topic, int qos ) {
        if (client != null) {
            int[] Qos = {qos};
            String[] topic1 = {topic};
            try {
                client.subscribe(topic1, Qos);
                Log.d(TAG, "订阅topic : " + topic);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    public void publish( String topic, String msg, boolean isRetained, int qos ) {
        try {
            if (client != null) {
                Log.d(TAG, "发送消息：" + msg);
                MqttMessage message = new MqttMessage();
                message.setQos(qos);
                message.setRetained(isRetained);
                message.setPayload(msg.getBytes());
                client.publish(topic, message);
            } else {
                Log.d(TAG, "没有连接到服务端");
            }
        } catch (MqttPersistenceException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void unSubscribe( String topic ) {
        try {
            if (client != null) {
                client.unsubscribe(topic);
            } else {
                Log.d(TAG, "没有连接到服务端");
            }
        } catch (MqttPersistenceException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * 延迟执行在线状态刷新
     *
     * @param delay
     */
    public void excuteTask( int delay ) {

        task = new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                //传参用
                message.what = 1;
                handler.sendMessage(message);
            }
        };
        timer.schedule(task, delay);
    }

    // MQTT是否连接成功，连接回调接口
    private IMqttActionListener iMqttActionListener = new IMqttActionListener() {

        @Override
        public void onSuccess( IMqttToken arg0 ) {
            //连接成功创建设备在线监听事件
            useConnect = true;
            Log.i(TAG, "连接成功 ");
        }

        @Override
        public void onFailure( IMqttToken arg0, Throwable arg1 ) {
            // 连接失败
            Log.i(TAG, "连接失败 ");
            //重连
            useConnect = false;
            Toast toast = Toast.makeText(context, "连接消息服务器失败，请检查网络", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            arg1.printStackTrace();
        }
    };
    private MqttCallback mqttCallback = new MqttCallback() {
        @Override
        public void connectionLost( Throwable cause ) {
            Log.i(TAG, "connection lost");
            Toast toast = Toast.makeText(context, "MQTT连接丢失", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
            //重连
            useConnect = false;
        }

        //订阅topic后，通过设置的此回调接口进行消息接收
        @Override
        public void messageArrived( String topic, MqttMessage message ) {
            Log.i(TAG, "received topic : " + topic);
            String payload = new String(message.getPayload());
            Log.i(TAG, "received msg : " + payload);
            //4. 发送广播
            if (payload.equals("s")) {
                //开关指令是否成功
                Intent intent = new Intent();
                intent.setAction("RspInfo");
                intent.putExtra("RspInfo", payload);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            } else if (payload.contains("t")) {
                //设备类型
                Intent intent = new Intent();
                intent.setAction("typeInfo");
                intent.putExtra("typeInfo", payload.charAt(1) + "");
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            } else if (payload.contains("h")) {
                String[] strs = topic.split("/");
                //设置设备状态
                String[] params = devices.get(strs[0]).split("-");
                devices.put(strs[0], params[0] + "-ON");
            } else {
                //开关状态
                Intent intent = new Intent();
                intent.setAction("RspInfo");
                intent.putExtra("RspInfo", payload);
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        }

        /**
         * 发布消息的回调
         */
        @Override
        public void deliveryComplete( IMqttDeliveryToken token ) {
            Log.i(TAG, "deliveryComplete");
        }
    };

    public Handler getHandler() {
        return handler;
    }

    public Timer getTimer() {
        return timer;
    }

    public void setContext( Context context ) {
        this.context = context;
    }

    public boolean isConnect() {
        return client.isConnected();
    }

    /**
     * 取消连接
     *
     * @throws MqttException
     */
    public void disConnect() throws MqttException {
        if (client != null && client.isConnected()) {
            client.disconnect();
        }
    }

    public void close() {
        if (client != null && client.isConnected()) {
            try {
                handler = null;
                client.disconnect();
                mqttManager = null;
                useConnect = false;
            } catch (MqttException e) {
                Log.e(TAG, "mq " + e.toString());
                e.printStackTrace();
            }
        }
    }
}