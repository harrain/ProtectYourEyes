package com.example.protectyoureyes;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.example.protectyoureyes.Activity.MainActivity;
import com.example.protectyoureyes.Service.LongRunningService;
import com.example.protectyoureyes.util.SharedPreferenceUtil;
import com.xdandroid.hellodaemon.DaemonEnv;

/**
 * Created by stephen on 2016/10/30.
 */

public class BaseApplication extends Application {

    private static BaseApplication application;
    private static int mainTid;
    private static Handler handler;
    private String TAG = "BaseApplication";

    @Override
//  在主线程运行的
    public void onCreate() {
        super.onCreate();
        application=this;
        mainTid = android.os.Process.myTid();
        handler=new Handler();

        DaemonEnv.initialize(this, LongRunningService.class,DaemonEnv.DEFAULT_WAKE_UP_INTERVAL);
        Log.d(TAG, "BaseApplication: service running "+ServiceUtil.isServiceRunning(this, Constants.LONG_RUN_SERVICE_NAME));
        if (SharedPreferenceUtil.getInstance().isTiming()
                && !ServiceUtil.isServiceRunning(this,Constants.LONG_RUN_SERVICE_NAME)){
            startService(new Intent(this,LongRunningService.class));
            Log.d(TAG, "onCreate: timing true & isServiceRunning "+ServiceUtil.isServiceRunning(this,Constants.LONG_RUN_SERVICE_NAME));
        }
    }
    public static Context getApplication() {
        return application;
    }
    public static int getMainTid() {
        return mainTid;
    }
    public static Handler getHandler() {
        return handler;
    }
}
