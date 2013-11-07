package com.android.internal.policy.impl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

import java.util.Calendar;

@SuppressLint("WorldWriteableFiles")
public class PowerSaveModeUtils {
	


    public static final String PREF_POWER_WIDGET_INFO = "powersavemode_preferencess";
    public static final String POWER_SWITCH = "power_switch";      
    public static final String LOW_POWER_STATE= "low_power_mode_switch";
    public static final String LOW_POWER_MODE = "low_power_mode_effect"; 
    public static final String SMART_NIGHT_SWITCH = "smart_night_mode_switch";  
    public static final String SETTING_STATE = "is_setting";     
    public static final String SUPER_POWER = "super_power";  
    
    

    protected static final String BACK_NIGHT = "back_night";     
    protected static final String SCREEN_OFF = "screen_off"; 

    protected static final String START_H = "start_h";     
    protected static final String START_M = "start_m"; 
    protected static final String END_H = "end_h";     
    protected static final String END_M = "end_m"; 
    
    
    
    private static SharedPreferences pref = null;
    
    private static Context context;

    public PowerSaveModeUtils(Context ctx) {
        context = ctx;
        pref = context.getSharedPreferences(PREF_POWER_WIDGET_INFO, Context.MODE_WORLD_READABLE|Context.MODE_WORLD_WRITEABLE);  
    }


    public PowerSaveModeUtils(Context ctx,SharedPreferences s) {
        context = ctx;
        pref = s;//context.getSharedPreferences(PREF_POWER_WIDGET_INFO, 0);
    }
    
    
    public int getDefaultBackNight(){
    	return 150;
    }

    
    public void saveCurBackNight(){
    	;
    }
    
    public int getCurBackNight(){
    	return 100;
    }    


    public int getCurSaveBackNight(){
        int brightness = -1;  
       
        try{  
            brightness = Settings.Global.getInt(context.getContentResolver(), Settings.Global.SCREEN_SAVE_BRIGHTNESS);  
        }catch(SettingNotFoundException ex){  
            new Exception(ex.toString());  
        }
        return brightness;  
	//pref.getInt(BACK_NIGHT, getCurSystemBackNight());
    } 
  
    public void setCurSaveBackNight(int value){	
 	Settings.Global.putInt(
        context.getContentResolver(),
        Settings.Global.SCREEN_SAVE_BRIGHTNESS,
        value);
    	//pref.edit().putInt(BACK_NIGHT, value).commit();
    }   
    
    public int getCurSystemBackNight(){
        int brightness = -1;  
       
        try{  
            brightness = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);  
        }catch(SettingNotFoundException ex){  
            new Exception(ex.toString());  
        }
        return brightness;  
    }
    
    public void SetCurSystemBackNight(int brightness ){
    	Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,brightness);   
    }    


    
    public int getCurSystemScreenOffTime(){
        int screenOffTime=0;  
        try{  
            screenOffTime = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT);  
        }  
        catch (Exception localException){        
        }  
        return screenOffTime;  
    }
    
    public void setCurSystemScreenOffTime(int paramInt){  
        try{  
              Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, paramInt);  
            }catch (Exception localException){  
              localException.printStackTrace();  
            }  
      }  
    
    public int getCurSaveScreenOffTime(){
    	return pref.getInt(SCREEN_OFF, getCurSystemScreenOffTime());
    }
    
    public void setCurSaveScreenOffTime(int value){
    	pref.edit().putInt(SCREEN_OFF, value).commit();
    }
    

    public void setBooleanState(String tag,boolean state){
    	pref.edit().putBoolean(tag, state).commit();
    }
    public boolean getBooleanState(String tag){
    	return pref.getBoolean(tag, false);
    } 
    
    
    public void setintvalue(String tag,int state){
    	pref.edit().putInt(tag, state).commit();
    }
    public int getintvalue(String tag,int default_value){
    	return pref.getInt(tag,default_value);
    } 
    
    
    public boolean getPowerSaveModeSwitch(){
    	return pref.getBoolean(POWER_SWITCH, DefalutValue.default_power_mode_switch);
    }
    
    public void setPowerSaveModeSwitch(boolean state){
    	pref.edit().putBoolean(POWER_SWITCH, state).commit();
    }   
    
    public boolean getSmartNightSwitch(){
    	return pref.getBoolean(SMART_NIGHT_SWITCH, DefalutValue.default_smart_night_switch);
    } 
    
    public void setSmartNightSwitch(boolean state){
       	pref.edit().putBoolean(SMART_NIGHT_SWITCH, state).commit();
        
    }      

    public int[] getSmartNightStartTime(){
    	int start_time[] = new int[2];
    	start_time[0]=20;
    	start_time[1]=0;
    	start_time[0] = pref.getInt(START_H, DefalutValue.default_start_h);
    	start_time[1] = pref.getInt(START_M, DefalutValue.default_start_m);
    	return start_time;
    }     
    
    public void setSmartNightStartTime(int start_time[]){
    	pref.edit().putInt(START_H, start_time[0]).commit();
    	pref.edit().putInt(START_M, start_time[1]).commit();
    }

    public int[] getSmartNightEndTime(){
    	int end_time[] = new int[2];
    	end_time[0]=12;
    	end_time[1]=0;
    	end_time[0] = pref.getInt(END_H, DefalutValue.default_end_h);
    	end_time[1] = pref.getInt(END_M, DefalutValue.default_end_m);    	
    	return end_time;
    }  
    
    public void setSmartNightEndTime(int end_time[]){
    	pref.edit().putInt(END_H, end_time[0]).commit();
    	pref.edit().putInt(END_M, end_time[1]).commit();
    }
    

    public boolean getLowPowerSwitch(){
    	return pref.getBoolean(LOW_POWER_STATE, DefalutValue.default_low_power_switch);
    }     
    
    public void setLowPowerSwitch(boolean state){
       	pref.edit().putBoolean(LOW_POWER_STATE, state).commit();
        
    }  
    
    public int getLowPowerEffect(){
    	return pref.getInt(LOW_POWER_MODE, DefalutValue.default_low_power_effect);
    }

    public void setLowPowerEffect(int value){
    	pref.edit().putInt(LOW_POWER_MODE, value).commit();
    }   
 
    public boolean getSuperPowerSwitch(){
    	return pref.getBoolean(SUPER_POWER, DefalutValue.default_super_power_switch);
    }
    
    public void setSuperPowerSwitch(boolean state){
    	pref.edit().putBoolean(SUPER_POWER, state).commit();
    }
    
    
    

    
    public static final int NO_SET = 0;
    public static final int NORMAL_SET = 1;
    public static final int SUPER_SET = 2;    
    
    public int getSettingState(){
    	return pref.getInt(SETTING_STATE, NO_SET);
    }
    
    public void setSettingState(int state){
    	pref.edit().putInt(SETTING_STATE, state).commit();
    }
    
    
    static long calculateAlarm(int hour, int minute) {

        // start with now
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());

        int nowHour = c.get(Calendar.HOUR_OF_DAY);
        int nowMinute = c.get(Calendar.MINUTE);

        // if alarm is behind current time, advance one day
        if (hour < nowHour  ||
            hour == nowHour && minute <= nowMinute) {
            c.add(Calendar.DAY_OF_YEAR, 1);
        }
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }
    
    
    
}
