package com.android.internal.policy.impl;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.provider.Settings;

import java.lang.reflect.Method;

public class SettingItem {
	// 背光调暗
	
	private final static int BACKNIGHT = 0;
	private final static int SCREEN_OFF = 1;
	private static boolean super_power_switch = false; 
	private Context mContext;
	private PowerSaveModeUtils pt;
	private ConnectivityManager mCM;
	private int is_save_mode = 0;
	
	public SettingItem(Context context,boolean mode,int save_mode) {
		// TODO Auto-generated constructor stub
		super_power_switch = mode;
		mContext = context;
		pt = new PowerSaveModeUtils(context);
		is_save_mode = save_mode;
	}
	public SettingItem(Context context,int save_mode) {
		// TODO Auto-generated constructor stub
		//super_power_switch = mode;
		mContext = context;
		is_save_mode = save_mode;
		pt = new PowerSaveModeUtils(context);
	}
	
	void close_bluetooth(){
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		int state = adapter.getState();
		if(state != BluetoothAdapter.STATE_OFF && state != BluetoothAdapter.STATE_TURNING_OFF){
			adapter.disable();
			pt.setBooleanState("bluetooth", true);
		}else{
			pt.setBooleanState("bluetooth",false);
		}
	}
	
	
	void restore_bluetooth(){
		if(pt.getBooleanState("bluetooth")){
			BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
			adapter.enable();
		}
	}
	
	 //开启/关闭GPRS 
    private void setGprsEnabled(String methodName, boolean isEnable) 
    { 
        Class cmClass       = mCM.getClass(); 
        Class[] argClasses  = new Class[1]; 
        argClasses[0]       = boolean.class; 
         
        try 
        { 
            Method method = cmClass.getMethod(methodName, argClasses); 
            method.invoke(mCM, isEnable); 
        } catch (Exception e) 
        { 
            e.printStackTrace(); 
        } 
    } 
    
    private boolean gprsIsOpenMethod(String methodName) 
    { 
        Class cmClass       = mCM.getClass(); 
        Class[] argClasses  = null; 
        Object[] argObject  = null; 
         
        Boolean isOpen = false; 
        try 
        { 
            Method method = cmClass.getMethod(methodName, argClasses); 
 
            isOpen = (Boolean) method.invoke(mCM, argObject); 
        } catch (Exception e) 
        { 
            e.printStackTrace(); 
        } 
 
        return isOpen; 
    } 
    
	void close_gprs(){
		mCM = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		boolean isOpen = gprsIsOpenMethod("getMobileDataEnabled");
		if(isOpen){
			pt.setBooleanState("gprs", true);
			setGprsEnabled("setMobileDataEnabled", false); 
		}else {
			pt.setBooleanState("gprs", false);
		}
	}
	
	void restore_gprs(){
		if(pt.getBooleanState("gprs")){
			mCM = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
			boolean isOpen = gprsIsOpenMethod("getMobileDataEnabled");
			if(!isOpen)
				setGprsEnabled("setMobileDataEnabled", true); 
		}
	}
	
	
	void close_wifi(){
		WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
		if (wifiManager.isWifiEnabled()) {
			pt.setBooleanState("wifi", true);
			wifiManager.setWifiEnabled(false);
		}else{
			pt.setBooleanState("wifi", false);
		}
	}
	
	
	void restore_wifi(){
		if(pt.getBooleanState("wifi")){
			WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
			if (!wifiManager.isWifiEnabled())
				wifiManager.setWifiEnabled(true);
		}
	}

    
    void close_gps(){
    	if(Settings.Secure.isLocationProviderEnabled(mContext.getContentResolver(), LocationManager.GPS_PROVIDER)){
    		Settings.Secure.setLocationProviderEnabled(mContext.getContentResolver(), LocationManager.GPS_PROVIDER,false);
    		pt.setBooleanState("gps", true);
    	}else{
    		pt.setBooleanState("gps", false);
    	}
    }
    
    
    void restore_gps(){
    	if(pt.getBooleanState("gps")){
    		Settings.Secure.setLocationProviderEnabled(mContext.getContentResolver(), LocationManager.GPS_PROVIDER,true);
    	}
    }
    
	
	void set_small_volume(){
		AudioManager mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
		pt.setintvalue("volume", mAudioManager.getStreamVolume(AudioManager.STREAM_RING));
		mAudioManager.setStreamVolume(AudioManager.STREAM_RING, DefalutValue.default_normal_volume, AudioManager.FLAG_PLAY_SOUND);
	}

	void restore_small_volume(){
		AudioManager mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		if( (mAudioManager.getStreamVolume(AudioManager.STREAM_RING)) == DefalutValue.default_normal_volume){
			mAudioManager.setStreamVolume(AudioManager.STREAM_RING, pt.getintvalue("value", DefalutValue.default_normal_volume), AudioManager.FLAG_PLAY_SOUND);
		}
	}
	
	

	
	void setScreenOff(){
		int cur_time = pt.getCurSystemScreenOffTime();
		//int save_b = pt.getCurSaveScreenOffTime();
		int time = 0;
		time = pt.getSuperPowerSwitch() ? DefalutValue.default_super_screenof : DefalutValue.default_normal_screenof ;
		if(is_save_mode == PowerSaveModeUtils.NO_SET)
			pt.setCurSaveScreenOffTime(cur_time);
		pt.setCurSystemScreenOffTime(time);	
	}
	
	void setBackNight(){
		int cur_b = pt.getCurSystemBackNight();
		//int save_b = pt.getCurSaveBackNight();
		int brightness = 0;
		
		brightness = pt.getSuperPowerSwitch() ? DefalutValue.default_super_backnight : DefalutValue.default_normal_backnight ;
		//if(cur_b == save_b){
		//	Mylog.i("Setting BACKNIGHT: brightness =" + brightness + "cur_b =" + cur_b + "save_b =" + save_b);
			if(is_save_mode == PowerSaveModeUtils.NO_SET)
				pt.setCurSaveBackNight(cur_b);
			pt.SetCurSystemBackNight(brightness);
		//}
	}
	
	
	void restoreScreenoff(){
		int cur = pt.getCurSystemScreenOffTime();
		int save = pt.getCurSaveScreenOffTime();
		int time = 0;
		time = pt.getSuperPowerSwitch() ? DefalutValue.default_super_screenof : DefalutValue.default_normal_screenof ;
		if(cur == time)
			pt.setCurSystemScreenOffTime(save);
	}
	
	
	void restoreBackNight(){
		int save_b = pt.getCurSaveBackNight();
		int cur_b = pt.getCurSystemBackNight();
		int brightness = super_power_switch ? DefalutValue.default_super_backnight : DefalutValue.default_normal_backnight ;
		if(cur_b == brightness)
			pt.SetCurSystemBackNight(save_b);
	}
	
	
	
	Handler mHandle = new Handler(){
		public void handleMessage(android.os.Message msg) {
				switch(msg.what){
					case BACKNIGHT:
						setBackNight();
						Mylog.i("Setting BACKNIGHT");
						setScreenOff();
						Mylog.i("setScreenOff");
						set_small_volume();
						Mylog.i("set_small_volume");
						//close_gps();
						//Mylog.i("close_gps");
						close_gprs();
						Mylog.i("close_gprs");
						close_wifi();
						Mylog.i("close_wifi");
						close_bluetooth();
						Mylog.i("close_bluetooth");
						break;
					default:
						break;
				}
		};
	};
	
	
	
	
	Thread thread = new Thread(new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				mHandle.sendEmptyMessage(BACKNIGHT);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	});
	
	public  void start(){
		thread.start();
		return;
	}
	
	Thread thread_restore = new Thread(new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				mHandleRestore.sendEmptyMessage(BACKNIGHT);
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	});
	
	Handler mHandleRestore = new Handler(){
		public void handleMessage(android.os.Message msg) {
				switch(msg.what){
					case BACKNIGHT:
						Mylog.i("Setting BACKNIGHT");
						restoreBackNight();
						restoreScreenoff();
						//restore_gps();
						restore_wifi();
						restore_gprs();
						restore_bluetooth();
						restore_small_volume();
					//	mHandleRestore.sendEmptyMessage(SCREEN_OFF);
						break;
					default:
						break;
				}
		};
	};
	public void restore(){
		thread_restore.start();
	}
	
	
}
