package com.all.weather;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LaucherBroadcastReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
		 Log.i("xia","------weather - onReceive--------");
		Intent mIntent = new Intent();
        mIntent.setClass(context, TimeUpdateService.class);
        PendingIntent mPendingIntent = PendingIntent.getService(context, 0, mIntent, 0);
        mPendingIntent.cancel();
        context.startService(new Intent(context, TimeUpdateService.class));
	}
}
