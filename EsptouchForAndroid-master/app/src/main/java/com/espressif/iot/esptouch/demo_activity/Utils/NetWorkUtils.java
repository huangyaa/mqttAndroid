package com.espressif.iot.esptouch.demo_activity.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.provider.Telephony;

import com.espressif.iot.esptouch.demo_activity.entity.SmsCodeReqDto;
import com.espressif.iot.esptouch.demo_activity.entity.StreamReqEntity;
import com.espressif.iot.esptouch.demo_activity.entity.TaskClassEntity;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

public class NetWorkUtils {
    /**
     * 判断当前网络是否可用(6.0以上版本)
     * 实时
     *
     * @param context
     * @return
     */
    public static boolean isNetSystemUsable( Context context ) {
        boolean isNetUsable = false;
        ConnectivityManager manager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            NetworkCapabilities networkCapabilities =
                    manager.getNetworkCapabilities(manager.getActiveNetwork());
            if (networkCapabilities != null) {
                isNetUsable = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
            }
        }else{
            isNetUsable = isNetPingUsable();
        }
        return isNetUsable;
    }
    /**
     * 判断当前网络是否可用(通用方法)
     * 耗时12秒
     * @return
     */
    public static boolean isNetPingUsable(){
        Runtime runtime = Runtime.getRuntime();
        try {
            Process process = runtime.exec("ping -c 3 www.baidu.com");
            int ret = process.waitFor();
            if (ret == 0){
                return true;
            }else {
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * get发送网络请求
     *
     * @param path
     * @throws Exception
     */
    public static String getData( String path) throws Exception {
        String result = "";
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url(Const.baseUrl+path).build();
        try {

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                result = response.body().string();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    /**
     * get发送网络请求
     *
     * @param url
     * @throws Exception
     */
    public static String getDataByParam( HttpUrl  url) throws Exception {
//                String path = Const.baseUrl + "/deviceInfo/get";
//                        /*  下面方法可为HttpUrl添加query部分内容，添加结果为：../../..?systemID = 系统id值&dishesID = 接口编号&data = 参数数据  */
//                HttpUrl url = HttpUrl.parse(path).newBuilder()
//                        .addQueryParameter("mobile","123456789")
//                        .build();
        String result = "'";
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url(url).build();
        try {

            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                result = response.body().string();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    /**
     * post 发送数据
     *
     * @param path
     * @param pairs
     * @throws Exception
     */
    public static <T> String postData( String path, T pairs ) throws Exception {
        String result = "";
        OkHttpClient okHttpClient = new OkHttpClient();
        String baseUrl = Const.baseUrl + path;
        Gson gson = new Gson();
        String param = gson.toJson(pairs).toString();
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), param);
        final Request request = new Request.Builder()
                .url(baseUrl)
                .post(requestBody)
                .build();
        Response response = okHttpClient.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response);
        } else {
            result = response.body().string();
        }
        return result;
    }
}
