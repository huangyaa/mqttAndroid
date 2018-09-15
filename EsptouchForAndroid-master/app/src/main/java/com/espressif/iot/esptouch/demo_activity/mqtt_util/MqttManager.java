package com.espressif.iot.esptouch.demo_activity.mqtt_util;

import android.content.Context;
import android.util.Log;
import android.webkit.WebMessagePort;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttManager {
    public static final String TAG = MqttManager.class.getSimpleName();

    private String host = "tcp://yun.lytldz.com:1883";
    private String userName = "admin";
    private String passWord = "adminxy005";
    private String clientId = "";
    private Context context;
    public static final String TOPIC = "test";

    private static MqttManager mqttManager = null;
    private MqttAndroidClient client;
    private MqttConnectOptions connectOptions;

    public MqttManager(Context context){
        this.context = context;
        clientId = MqttClient.generateClientId();
    }

    public static MqttManager getInstance(Context context){
        if(mqttManager == null){
            mqttManager = new MqttManager(context);
        }
        return mqttManager;
    }

    public void connect(){
        try{
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
            connectOptions.setWill(TOPIC, "the client will stop !".getBytes(), 0, false);
            client.setCallback(mqttCallback);

            client.connect(connectOptions,iMqttActionListener);
        }catch (MqttException e){
            e.printStackTrace();
        }
    }

    public void subscribe(String topic,int qos){
        if(client != null){
            int[] Qos = {qos};
            String[] topic1 = {topic};
            try {
                client.subscribe(topic1, Qos);
                Log.d(TAG,"订阅topic : "+topic);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    public void publish(String topic,String msg,boolean isRetained,int qos) {
        try {
            if (client!=null) {
                MqttMessage message = new MqttMessage();
                message.setQos(qos);
                message.setRetained(isRetained);
                message.setPayload(msg.getBytes());
                client.publish(topic, message);
            }else{
                Log.d(TAG,"没有连接到服务端");
            }
        } catch (MqttPersistenceException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    // MQTT是否连接成功，连接回调接口
    private IMqttActionListener iMqttActionListener = new IMqttActionListener() {

        @Override
        public void onSuccess(IMqttToken arg0) {
            Log.i(TAG, "连接成功 ");
            try {
                // 连接成功后，订阅myTopic话题
                client.subscribe("myTopic",0);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(IMqttToken arg0, Throwable arg1) {
            // 连接失败，重连
            Log.i(TAG, "连接成功 ");
            arg1.printStackTrace();
        }
    };
    private MqttCallback mqttCallback = new MqttCallback() {
        @Override
        public void connectionLost(Throwable cause) {
            Log.i(TAG,"connection lost");
        }

        //订阅topic后，通过设置的此回调接口进行消息接收
        @Override
        public void messageArrived(String topic, MqttMessage message){
            Log.i(TAG,"received topic : " + topic);
            String payload = new String(message.getPayload());
            Log.i(TAG,"received msg : " + payload);
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            Log.i(TAG,"deliveryComplete");
        }
    };

    public boolean isConnect(){
        return client.isConnected();
    }
}