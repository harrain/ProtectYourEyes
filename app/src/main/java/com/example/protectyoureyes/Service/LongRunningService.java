package com.example.protectyoureyes.Service;


import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.WindowManager;

import com.example.protectyoureyes.R;
import com.example.protectyoureyes.bean.GlobalData;

public class LongRunningService extends Service {

	private final String TAG = "LongRunningService";
	private final IBinder iBinder = new MyBinder();
	private boolean startAlarm;
	@Override
	public void onCreate() {
		super.onCreate();
		startAlarm = false;
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.i(TAG,"onBind...");
		return iBinder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.i(TAG,"onUnbind...");
		return super.onUnbind(intent);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (startAlarm) {
			notificationAndVibrate();
			createAlertDialog();
			GlobalData.alarmType = !GlobalData.alarmType;
		}
		setAlarmManager();
		startAlarm = true;

		return super.onStartCommand(intent, flags, startId);
	}

	public void notificationAndVibrate(){
		//启用前台服务，主要是startForeground()
		try {
			Notification notification = null;
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
				Notification.Builder builder = new Notification.Builder(this);
				builder.setSmallIcon(R.mipmap.eye);
				builder.setLargeIcon(GlobalData.inform_bitmap);
				if (GlobalData.alarmType) {
					builder.setContentTitle(GlobalData.inform_title);
					builder.setContentText(GlobalData.inform_content);
				}else {
					builder.setContentTitle(GlobalData.interval_title);
					builder.setContentText(GlobalData.interval_content);
				}
				notification = builder.build();

			}
			//设置振动
			notification.vibrate = GlobalData.all_vibrate_type[GlobalData.vibrate_type_number];
			startForeground(1, notification);
		}catch (NullPointerException e){
			Log.d("notification","throw a NullPointerException");
			e.printStackTrace();
		}
	}

	public void setAlarmManager(){
		AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);

		//读者可以修改此处的Minutes从而改变提醒间隔时间
		//此处是设置每隔55分钟启动一次
		//这是55分钟的毫秒数
		int minutes = GlobalData.inform_time*60*1000;
		long triggerAtTime;
		int intervalMinutes = GlobalData.interval_time*60*1000;
		long intervalMillis = SystemClock.elapsedRealtime() + intervalMinutes;
		if (GlobalData.alarmType) {
			//SystemClock.elapsedRealtime()表示1970年1月1日0点至今所经历的时间
			triggerAtTime = SystemClock.elapsedRealtime() + minutes;
		}else {
			triggerAtTime = intervalMillis;
		}
		//此处设置开启AlarmReceiver这个BroadcastReceiver
		Intent i = new Intent(this, AlarmReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
		//ELAPSED_REALTIME_WAKEUP表示让定时任务的出发时间从系统开机算起，并且会唤醒CPU。
		manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);//只能固定一个时间间隔响铃
		//manager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,intervalMillis,pi);
		//上面这个方法不适合这里，因为这个是一次性的提醒，在广播器中再次启动服务，这样就通知震动
	}

	public void createAlertDialog(){
		try {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			if (GlobalData.alarmType) {
				builder.setMessage(GlobalData.inform_title);
			} else {
				builder.setMessage(GlobalData.interval_title);
			}
			builder.setPositiveButton("关闭", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					dialogInterface.dismiss();
				}
			});
			builder.setNegativeButton("知道了", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					dialogInterface.dismiss();
				}
			});
			AlertDialog alertDialog = builder.create();
			alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
			alertDialog.setCanceledOnTouchOutside(false);
			alertDialog.show();
		}catch (NullPointerException e){
			e.printStackTrace();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		//在Service结束后关闭AlarmManager
		AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
		Intent i = new Intent(this, AlarmReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
		manager.cancel(pi);



	}

	public class MyBinder extends Binder {

		public LongRunningService getService(){
			return LongRunningService.this;
		}

	}
}
