package com.example.protectyoureyes.Service;


import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.WindowManager;

import com.example.protectyoureyes.Constants;
import com.example.protectyoureyes.R;
import com.example.protectyoureyes.ServiceUtil;
import com.example.protectyoureyes.bean.GlobalData;
import com.example.protectyoureyes.util.SharedPreferenceUtil;
import com.xdandroid.hellodaemon.AbsWorkService;

import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormat;
import org.joda.time.format.PeriodFormatter;

public class LongRunningService extends AbsWorkService {

	private final String TAG = "LongRunningService";
	private final IBinder iBinder = new MyBinder();
	private boolean startAlarm;

	@Override
	public void onCreate() {
		super.onCreate();
		startAlarm = false;
		Log.d(TAG, "onCreate: "+ ServiceUtil.isServiceRunning(this, Constants.LONG_RUN_SERVICE_NAME));
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
	public Boolean shouldStopService(Intent intent, int flags, int startId) {
		return !SharedPreferenceUtil.getInstance().isTiming();
	}

	@Override
	public void startWork(Intent intent, int flags, int startId) {
		Log.d(TAG, "startWork: ");
		if (startAlarm) {
			notificationAndVibrate();
			if (mlistener!=null) mlistener.onShowDialog();
			GlobalData.alarmType = !GlobalData.alarmType;
		}
		setAlarmManager();
		startAlarm = true;
		if (GlobalData.alarmType) {
			if (countDownTimer!=null) countDownTimer.cancel();
			startCountingTime(GlobalData.inform_time*60*1000,1000);
		}
		else {
			if (countDownTimer!=null) countDownTimer.cancel();
			startCountingTime(GlobalData.interval_time*60*1000,1000);
		}
	}

	@Override
	public void stopWork(Intent intent, int flags, int startId) {
		Log.d(TAG, "stopWork: ");
		//在Service结束后关闭AlarmManager
		AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
		Intent i = new Intent(this, AlarmReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
		manager.cancel(pi);

		cancelJobAlarmSub();
	}

	/**
	 * isWorkRunning 为true，则不会再调用 startWork
	 */
	@Override
	public Boolean isWorkRunning(Intent intent, int flags, int startId) {
		Log.d(TAG, "isWorkRunning: ");
		return false;
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent, Void alwaysNull) {
		return null;
	}

	@Override
	public void onServiceKilled(Intent rootIntent) {
		Log.d(TAG, "onServiceKilled: ");
	}

	public void notificationAndVibrate(){
		//启用前台服务，主要是startForeground()
		try {
			Notification notification = null;
			if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
				Notification.Builder builder = new Notification.Builder(this);
				builder.setSmallIcon(R.mipmap.little_robot);
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
		long minutes = GlobalData.inform_time*60*1000;
		long triggerAtTime;
		long intervalMinutes = GlobalData.interval_time*60*1000;
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



	@Override
	public void onDestroy() {
		super.onDestroy();

	}

	public class MyBinder extends Binder {

		public LongRunningService getService(){
			return LongRunningService.this;
		}

	}


	private String formatTime;
	private CountTimeShowListener listener;
	private CountDownTimer countDownTimer;

	public void startCountingTime(final long millisInFuture, long countDownInternal){
		countDownTimer = new CountDownTimer(millisInFuture,countDownInternal) {
			@Override
			public void onTick(long millisUntilFinished) {

//				Period period = new Period(millisUntilFinished);
//				StringBuilder sb = new StringBuilder();
//				sb.append( period.getMinutes());
//				sb.append(":");
//				sb.append(period.getSeconds());
//				formatTime = sb.toString();
				LocalTime localTime = new LocalTime(millisUntilFinished);
				formatTime = localTime.toString("HH:mm:ss");//必须按规定来yyyy-MM-dd HH:mm:ss ,不能是"HH:MM:SS"
				if (listener!=null)listener.showFormatTime(formatTime);
			}

			@Override
			public void onFinish() {
				formatTime = "00:00:00";
				if (listener!=null)listener.showFormatTime(formatTime);
			}
		};
		countDownTimer.start();
	}

	public String getFormatTime(){
		if (formatTime == null) formatTime = new LocalTime().toString("HH:MM:SS");
		return formatTime;
	}

	public CountDownTimer getCountDownTimer(){
		return countDownTimer;
	}

	public void setCountTimeShowListener(CountTimeShowListener timeShowListener){
		listener = timeShowListener;
	}

	private ShowInformDialogListener mlistener;

	public void setShowDialogListener(ShowInformDialogListener listener){
		mlistener = listener;
	}

	public interface CountTimeShowListener{
		void showFormatTime(String formatTime);
	}

	public interface ShowInformDialogListener{
		void onShowDialog();
	}

}
