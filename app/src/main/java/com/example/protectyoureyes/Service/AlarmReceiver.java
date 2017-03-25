package com.example.protectyoureyes.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.protectyoureyes.bean.GlobalData;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		//再次开启LongRunningService这个服务，从而可以
		Intent i = new Intent(context, LongRunningService.class);
		context.startService(i);
	}


}
