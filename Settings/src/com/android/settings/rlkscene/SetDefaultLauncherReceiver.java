package com.android.settings.rlkscene;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

public class SetDefaultLauncherReceiver extends BroadcastReceiver{

	private PackageManager pm;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Intent mainIntent = new Intent(Intent.ACTION_MAIN);
		mainIntent.addCategory(Intent.CATEGORY_HOME);
 		pm = context.getPackageManager();
 		List<ResolveInfo> resolveInfoList = pm.queryIntentActivities(mainIntent, 0);
 		Log.d("zsc", "Settings.SetDefaultLauncherReceiver--resolveInfoList.size="+resolveInfoList.size());
 		if (resolveInfoList != null) {
 			int size = resolveInfoList.size();
 			for (int j = 0; j < size; j++) {
 				final ResolveInfo r = resolveInfoList.get(j);
 				if (r.activityInfo.packageName.equals("com.android.provision")
 						|| r.activityInfo.name.equals("com.android.settings.CryptKeeper")) {
 					resolveInfoList.remove(j);
 					size -= 1;
 					break;
 				}
 			}
 			ComponentName[] set = new ComponentName[size];
 			ComponentName defaultLauncher = null;
 			if ("com.android.launcher.CLASSICS_LAUNCHER".equals(action)) {
 				defaultLauncher = new ComponentName(
 						"com.android.launcher", "com.android.launcher2.Launcher");
 			}else if ("com.android.launcher.SCENE_LAUNCHER".equals(action)) {
 				defaultLauncher = new ComponentName(
 						"com.rlk.scene", "com.rlk.scene.MainActivity");
 			}
 			int defaultMatch = 0;
 			if (size <= 2 && null != defaultLauncher) {
 				Log.d("zsc", "Settings.SetDefaultLauncherReceiver--<=2--defaultLauncher="+defaultLauncher.getPackageName());
 				for (int i = 0; i < size; i++) {
 					final ResolveInfo resolveInfo = resolveInfoList.get(i);
 					set[i] = new ComponentName(
 							resolveInfo.activityInfo.packageName,
 							resolveInfo.activityInfo.name);
 						if (resolveInfo.match > defaultMatch)defaultMatch = resolveInfo.match;
 				
 				}
 				
 				IntentFilter filter = new IntentFilter();
 				filter.addAction(Intent.ACTION_MAIN);
 				filter.addCategory(Intent.CATEGORY_HOME);
 				filter.addCategory(Intent.CATEGORY_DEFAULT);
 	                        pm.clearPackagePreferredActivities("com.rlk.scene");
				pm.clearPackagePreferredActivities("com.android.launcher");
 				pm.addPreferredActivity(filter, defaultMatch, set,
 						defaultLauncher);
 			}
 		}
		
	}

}
