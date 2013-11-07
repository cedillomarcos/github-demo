package com.rlk.scene;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

public class NotificationReceiver extends BroadcastReceiver{
	public static final String HSKJ_ACTION_NOTIFICATION_LAUNCHER_COUNT = "com.hskj.intent.ACTION_NOTIFICATION_LAUNCHER_COUNT";
	public static final String HSKJ_DATA_NOTIFICATION_LAUNCHER_COUNT_KEY = "className";
	public static final String HSKJ_DATA_NOTIFICATION_LAUNCHER_COUNT_VALUE = "count";
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action.equals(HSKJ_ACTION_NOTIFICATION_LAUNCHER_COUNT)) {
			String className = intent.getStringExtra(HSKJ_DATA_NOTIFICATION_LAUNCHER_COUNT_KEY);
			int count = intent.getIntExtra(HSKJ_DATA_NOTIFICATION_LAUNCHER_COUNT_VALUE,0);
			if (count >= 0) {
				Message msg =new Message();
				msg.arg1 = count;
				msg.obj = className;
				Launcher launcher = LauncherValues.getInstance().getLauncher();
				if (launcher != null) {
					Handler h = launcher.getNotificationLauncherHandler();
					if (h != null)
						h.sendMessage(msg);
				}
			}
		}
	}

}
