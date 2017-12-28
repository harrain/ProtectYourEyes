package com.example.protectyoureyes.bean;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;

import com.example.protectyoureyes.R;
import com.example.protectyoureyes.util.UiUtil;

import java.io.File;

// 保存振动类型、文段内容
public class GlobalData {
    //时间默认设置为55分钟
    public static long inform_time = 55;
    //休息间隔时间默认设置为10分钟
    public static long interval_time = 10;
    //当前振动类型的序号
    public static int vibrate_type_number = 4;
    //振动的类型的long数组
    public static long[][] all_vibrate_type = {
            {0, 5000},
            {0, 300, 300, 300, 300, 300, 300, 300, 300, 300, 300, 300, 300, 300, 300, 300, 300},
            {0, 400, 400, 800, 400, 400, 400, 800, 400, 400, 400, 800, 400, 400, 400, 800},
            {0, 300, 300, 600, 300, 900, 300, 1200, 300, 2400, 300, 3000},
            {0, 2000, 1000, 1000, 500, 3000, 800, 1000, 500, 500, 200, 2000},
            {0, 200, 200, 200, 200, 1000, 200, 200, 200, 200, 200, 1000, 200, 200, 200, 200, 200, 1000}
    };
    //振动类型的名字
    public static String[] vibrate_type_name = {"震动1",
            UiUtil.getString(R.string.alarm_vibrate2), UiUtil.getString(R.string.alarm_vibrate3),
            UiUtil.getString(R.string.alarm_vibrate4), UiUtil.getString(R.string.alarm_vibrate5),
            UiUtil.getString(R.string.alarm_vibrate6)};
    //提示的标题
    public static String inform_title = UiUtil.getString(R.string.inform_title);
    //提示的内容
    public static String inform_content = UiUtil.getString(R.string.inform_content);
    //提示图片的Uri
    public static Uri imageUri;
    //用来存储提示图片的Bitmap
    public static Bitmap inform_bitmap;

    public static boolean alarmType = true;
    public static Bitmap interval_bitmap;
    public static String interval_title;
    public static String interval_content;
}
