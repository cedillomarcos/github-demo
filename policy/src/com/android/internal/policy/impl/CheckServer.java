package com.android.internal.policy.impl;

import android.app.AlarmManager;
import android.app.ApplicationErrorReport.BatteryInfo;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;
import com.android.internal.R;
import java.util.Calendar;

public class CheckServer {
	
	
	Context mContext;
	PowerSaveModeUtils utils;
	public static final String TAG = "renxinquan";


	public CheckServer(Context context) {
		// TODO Auto-generated constructor stub
		mContext = context;
		utils = new PowerSaveModeUtils(context);
	}
		
	void start_setting(){
		//boolean super_power_switch = utils.getSuperPowerSwitch();
		//int setting_state = utils.getSettingState();
		

				//start normal power
				new SettingItem(mContext,false,0).start();
				utils.setSettingState(PowerSaveModeUtils.NORMAL_SET);
				Toast.makeText(mContext, R.string.start_narmal_toast, Toast.LENGTH_SHORT).show();

utils.setPowerSaveModeSwitch(true);

	}
	
	
	void restore_setting(){
		
		
		//if(setting_state != PowerSaveModeUtils.NO_SET){
			//restore_setting	
			Mylog.i("restore_setting");
			new SettingItem(mContext,1).restore();
			utils.setSettingState(PowerSaveModeUtils.NO_SET);
			Toast.makeText(mContext, R.string.end_normal_toast, Toast.LENGTH_SHORT).show();
		//}
utils.setPowerSaveModeSwitch(false);
	}
	
	boolean getState(){
		Mylog.i("zjw check server utils.getPowerSaveModeSwitch() = "+utils.getPowerSaveModeSwitch());
		return utils.getPowerSaveModeSwitch();
	}
	
	void setState(boolean state){
		Mylog.i("zjw check server utils.getPowerSaveModeSwitch() state= "+state);
		utils.setPowerSaveModeSwitch(state);
	}	


}
