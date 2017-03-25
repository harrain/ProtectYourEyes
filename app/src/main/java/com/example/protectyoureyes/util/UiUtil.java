package com.example.protectyoureyes.util;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import com.example.protectyoureyes.BaseApplication;

/**
 * Created by stephen on 2016/10/30.
 */

public class UiUtil {

    public static Resources getResource(){
        return BaseApplication.getApplication().getResources();
    }

    public static Context getContext(){
        return BaseApplication.getApplication();
    }

    public static String[] getStringArray(int tabNames){
        return getResource().getStringArray(tabNames);
    }

    /**
     * 把Runnable 方法提交到主线程运行
     * @param runnable
     */
    public static void runOnUiThread(Runnable runnable) {
        // 在主线程运行
        if(android.os.Process.myTid()==BaseApplication.getMainTid()){
            runnable.run();
        }else{
            //获取handler
            BaseApplication.getHandler().post(runnable);
        }
    }
    //runOnUIThread和POSTTaskSafely一样
    /**安全的执行一个任务*/
    public static void postTaskSafely(Runnable task) {
        int curThreadId = android.os.Process.myTid();

        if (curThreadId == BaseApplication.getMainTid()) {// 如果当前线程是主线程
            task.run();
        } else {// 如果当前线程不是主线程
            BaseApplication.getHandler().post(task);
        }

    }

    /**
     * 延迟执行 任务
     * @param run   任务
     * @param time  延迟的时间
     */
    public static void postDelayed(Runnable run, int time) {
        BaseApplication.getHandler().postDelayed(run, time); // 调用Runable里面的run方法
    }
    /**
     * 取消任务
     * @param auToRunTask
     */
    public static void cancel(Runnable auToRunTask) {
        BaseApplication.getHandler().removeCallbacks(auToRunTask);
    }

    public static int dip2px(int dip){
        final float scale = getResource().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f);
    }

    public static int px2dip(int px){
        final float scale = getResource().getDisplayMetrics().density;
        return (int) (px/scale +  0.5f);
    }

    public static Drawable getDrawable(int id) {
        return  getResource().getDrawable(id);
    }

    public static int getColor(int id){ return getResource().getColor(id); }

    public static int getDimen(int dimen){ return (int)getResource().getDimension(dimen); }

    public static String getString(int id){
        return getResource().getString(id);
    }

    public static void startActivity(Intent intent) {
         /**如果不在activity里去打开activity  需要指定任务栈  需要设置标签*/

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(intent);

    }
}
