package com.espressif.iot.esptouch.demo_activity.down;

import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import com.espressif.iot.esptouch.demo_activity.Utils.Const;

import java.io.File;

public class UpdateService extends Service {

    public UpdateService() {

    }
    /** 安卓系统下载类 **/
    DownloadManager manager;

    /** 接收下载完的广播 **/
    DownloadCompleteReceiver receiver;

    /**
     * 设置状态栏中显示Notification
     */
    private void setNotification(DownloadManager.Request request ) {
        //设置Notification的标题
        request.setTitle( "九达物联" ) ;

        //设置描述
        request.setDescription( "新版本下载" ) ;

        //request.setNotificationVisibility( Request.VISIBILITY_VISIBLE ) ;

        request.setNotificationVisibility( DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED ) ;

        //request.setNotificationVisibility( Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION ) ;

        //request.setNotificationVisibility( Request.VISIBILITY_HIDDEN ) ;
    }

    /** 初始化下载器 **/
    private void initDownManager(String url) {

        manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

        receiver = new DownloadCompleteReceiver();

        //设置下载地址
        Uri parse = Uri.parse(Const.baseUrl + url);
        DownloadManager.Request down = new DownloadManager.Request(parse);

        // 设置允许使用的网络类型，这里是移动网络和wifi都可以
        down.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);

        // 下载时，通知栏显示途中
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setNotification(down);
        }

        // 显示下载界面
        down.setVisibleInDownloadsUi(true);
        //设置下载文件的类型
        down.setMimeType("application/vnd.android.package-archive");

        //判断是否有旧的安装包，有的话删除
        File oldApkFile =new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), Const.apkName);
        if(oldApkFile.exists()){
            oldApkFile.delete();
        }
        //创建目录，设置下载后文件存放的位置
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdir() ;
        down.setDestinationInExternalPublicDir( Environment.DIRECTORY_DOWNLOADS, Const.apkName);

        // 将下载请求放入队列
        manager.enqueue(down);

        //注册下载广播
        registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // 调用下载
        initDownManager(intent.getStringExtra("url"));

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind( Intent intent) {

        return null;
    }

    @Override
    public void onDestroy() {

        // 注销下载广播
        if (receiver != null)
            unregisterReceiver(receiver);

        super.onDestroy();
    }

    // 接受下载完成后的intent
    class DownloadCompleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive( Context context, Intent intent) {
            //判断是否下载完成的广播
            if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                //获取下载的文件id
                long downId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                Log.e("tag","下载id="+downId);
                Toast.makeText( context , "更新下载完成了" ,  Toast.LENGTH_SHORT ).show() ;
                //自动安装apk
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    Uri uriForDownloadedFile = manager.getUriForDownloadedFile(downId);
                    Log.e("tag","uri="+uriForDownloadedFile);
                    installApkNew(uriForDownloadedFile);
                }

                //停止服务并关闭广播
                UpdateService.this.stopSelf();
            }
        }

        //安装apk
        protected void installApkNew(Uri uri) {
            File apkFile =
                    new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), Const.apkName);
            Intent intent = new Intent();
            //执行动作
            intent.setAction(Intent.ACTION_VIEW);
            //执行的数据类型
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(uri.fromFile(apkFile), "application/vnd.android.package-archive");
            if ((Build.VERSION.SDK_INT >= 24)) {//判读版本是否在7.0以上
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); //添加这一句表示对目标应用临时授权该Uri所代表的文件
            }
            try {
                startActivity(intent);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}