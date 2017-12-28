package com.example.protectyoureyes.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.protectyoureyes.BaseApplication;

/**
 * Created by net on 2017/12/27.
 */

public class SharedPreferenceUtil {

    private static SharedPreferenceUtil sharedPreferenceUtil;

    private SharedPreferences preferences;

    SharedPreferences.Editor editor;

    private static String FORALARMING = "for alarming";
    private static String millisUntilFinished = "millisUntilFinished";

    private  SharedPreferenceUtil(){
        preferences = BaseApplication.getApplication().getSharedPreferences("alarm", Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public void setTiming(boolean startAlarm){
        editor.putBoolean(FORALARMING,startAlarm).commit();
    }

    public boolean isTiming(){
        return preferences.getBoolean(FORALARMING,false);
    }

    public void saveMillis(long millis){
        editor.putLong(millisUntilFinished,millis).commit();
    }

    public Long getMillisUntilFinished(){
        return preferences.getLong(millisUntilFinished,-1);
    }

    public static SharedPreferenceUtil getInstance() {
        if (sharedPreferenceUtil == null){
            sharedPreferenceUtil = new SharedPreferenceUtil();
        }
        return sharedPreferenceUtil;
    }
}
