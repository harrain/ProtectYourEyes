package com.example.protectyoureyes;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;

import java.util.List;

/**
 * Created by net on 2017/12/27.
 */

public class ServiceUtil {

    private static String TAG = "ServiceUtil";

    /**
     *<uses-permission android:name="android.permission.GET_TASKS"/>
     */
    public static boolean isServiceRunning(Context mContext, String className){
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        //用不了ActivityManagerCompat managerCompat = (ActivityManagerCompat) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceInfos = activityManager.getRunningServices(500);
        if (!(serviceInfos.size() > 0)){
            Log.i(TAG,"serviceInfos.size() <= 0");
            return false;
        }
        for (int i = 0; i < serviceInfos.size(); i++) {

//            Log.i(TAG,"service"+i+":"+serviceInfos.get(i).service.getClassName());
            if (serviceInfos.get(i).service.getClassName().equals(className)){
                return true;
            }
        }
        return false;
    }
}
