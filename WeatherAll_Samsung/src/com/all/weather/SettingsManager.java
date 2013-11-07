package com.all.weather;
 
import com.all.weather.R;

//import com.mediatek.featureoption.FeatureOption;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsManager {
	
//	protected static final long STORAGEID = 123456l;
    private static final String WEATHER_PERFERNCE = "weather_preferences";
	private static SettingsManager INSTANCE;
	private SettingListener mListener;
	public static String weathertyle = "1";
	private static String DATASRC = null;
	private SettingsManager() {
	}	
	
	public static SettingsManager getInstance() {
		if(INSTANCE == null) {
			INSTANCE = new SettingsManager();
		}
		return INSTANCE;
	}
 
//	public synchronized LocationType getLocationType(long storageId, Context context) {
//		String value = getSharedPreference(storageId, Setting.PREFERENCE_LOCATION_TYPE, context, LocationType.getDefault().name());
//		                              //   1 relative shareperference 2 the sharedperference's key name 3 context 4 default value.
//		return Enum.valueOf(LocationType.class, value);
//	}
//	
//	
    public synchronized String getDateSrcType(Context context) {
    	if(DATASRC==null){
//modify  BUG_ID:GWLESW-625 xiazhengguo  20120503(on)
    	//	if(FeatureOption.RGT_CUSTOM_G100_ODJ)    		
    		//	DATASRC = getSharedPreference(Setting.PREFERENCE_DATASRC_TYPE, context, "2");//1 表示默认国内数据源   2 表示国外数据源
    	//	else
    			DATASRC = getSharedPreference(Setting.PREFERENCE_DATASRC_TYPE, context, "1");//1 表示默认国内数据源   2 表示国外数据源
//modify  BUG_ID:GWLESW-625 xiazhengguo  20120503(off)
    		return DATASRC;
        }else
        {
        	return DATASRC;
        }
    }
    public synchronized void setDateSrcType(String value, Context context, boolean notify) {
    	DATASRC = value;
        putSharedPreference(Setting.PREFERENCE_DATASRC_TYPE, value, context, notify);
    }
    
//	public synchronized String getWeatherType(Context context) {
//		return getSharedPreference(Setting.PREFERENCE_WEATHER_TYPE, context, "1");
//	}
//	public synchronized void setWeatherType(String value, Context context, boolean notify) {
//		putSharedPreference(Setting.PREFERENCE_WEATHER_TYPE, value, context, notify);
//	}
	
	//
	   public synchronized String getLocationZipCode_w(Context context) {
		   String src = getDateSrcType(context);
	        if(src.equals("1"))
	            return getSharedPreference(Setting.PREFERENCE_CENTER_LOCATION_ZIP_W, context, context.getResources().getString(R.string.default_center_location_zip));
	        else if(src.equals("2"))
                return getSharedPreference(Setting.PREFERENCE_ACUU_LOCATION_ZIP_W, context, context.getResources().getString(R.string.default_location_zip));
	        else
	        	 return getSharedPreference(Setting.PREFERENCE_GOOGLE_LOCATION_CITY_W, context, context.getResources().getString(R.string.default_location_zip));
		       
	   }
	    
	    public synchronized void setLocationZipCode_w(String value, Context context, boolean notify) {
	    	String src = getDateSrcType(context);
	    	if(getDateSrcType(context).equals("1")){
	    		 putSharedPreference(Setting.PREFERENCE_CENTER_LOCATION_ZIP_W, value, context, notify);
	    	}
	           
	        else  if(src.equals("2"))
	            putSharedPreference(Setting.PREFERENCE_ACUU_LOCATION_ZIP_W, value, context, notify);
	        else
	        {
	        	putSharedPreference(Setting.PREFERENCE_GOOGLE_LOCATION_CITY_W, value, context, notify);
	        }
	    }
	public synchronized String getLocationZipCode(Context context) {
		String src = getDateSrcType(context);
	    if(src.equals("1")) 
		return getSharedPreference(Setting.PREFERENCE_CENTER_LOCATION_ZIP, context, context.getResources().getString(R.string.default_center_location_zip));
	    else if(src.equals("2"))
	    return getSharedPreference(Setting.PREFERENCE_ACUU_LOCATION_ZIP, context, context.getResources().getString(R.string.default_location_zip));
	    else 
	    	return getSharedPreference(Setting.PREFERENCE_GOOGLE_LOCATION_CITY, context, context.getResources().getString(R.string.default_location_zip));
	    
	    
	}
	
	public synchronized void setLocationZipCode(String value, Context context, boolean notify) {
		String src = getDateSrcType(context);
	    if(src.equals("1")) 
	        putSharedPreference(Setting.PREFERENCE_CENTER_LOCATION_ZIP, value, context, notify);
	    else if(src.equals("2"))
	        putSharedPreference(Setting.PREFERENCE_ACUU_LOCATION_ZIP, value, context, notify);
	    else
	    	putSharedPreference(Setting.PREFERENCE_GOOGLE_LOCATION_CITY, value, context, notify);
		   
	    
	
	}
	
	public synchronized String getLocationCity(Context context) {
	    if(getDateSrcType(context).equals("1")) 
	       return getSharedPreference(Setting.PREFERENCE_CENTER_LOCATION_CITY, context, context.getResources().getString(R.string.default_center_location_city));
	    
	    else
	        return getSharedPreference(Setting.PREFERENCE_ACUU_LOCATION_CITY, context, context.getResources().getString(R.string.default_location_city));
	    
	
	}
	
	public synchronized void setLocationCity(String value, Context context, boolean notify) {
	    if(getDateSrcType(context).equals("1")) 
	    putSharedPreference(Setting.PREFERENCE_CENTER_LOCATION_CITY, value, context, notify);
	    else
	    putSharedPreference(Setting.PREFERENCE_ACUU_LOCATION_CITY, value, context, notify);
	    
	
	}
	
	//***********

	public synchronized String getLocationCity_w(Context context) {
	    if(getDateSrcType(context).equals("1")) 
	    return getSharedPreference(Setting.PREFERENCE_CENTER_LOCATION_CITY_W, context, context.getResources().getString(R.string.default_center_location_city));
	    else
	    return getSharedPreference(Setting.PREFERENCE_ACUU_LOCATION_CITY_W, context, context.getResources().getString(R.string.default_location_city));
	    
	}
	
	public synchronized void setLocationCity_w(String value, Context context, boolean notify) {
	    if(getDateSrcType(context).equals("1")) 
	    putSharedPreference(Setting.PREFERENCE_CENTER_LOCATION_CITY_W, value, context, notify);
	    else
	    putSharedPreference(Setting.PREFERENCE_ACUU_LOCATION_CITY_W, value, context, notify);
	}
	public synchronized String getLocationArea_w(Context context) {
		return getSharedPreference(Setting.PREFERENCE_ACUU_LOCATION_AREA_W, context, context.getResources().getString(R.string.default_location_area));
	}
	
	public synchronized void setLocationArea_w( String value, Context context, boolean notify) {
		putSharedPreference(Setting.PREFERENCE_ACUU_LOCATION_AREA_W, value, context, notify);
	}
	
	
	public synchronized String getLocationArea(Context context) {
		return getSharedPreference(Setting.PREFERENCE_ACUU_LOCATION_AREA, context, context.getResources().getString(R.string.default_location_area));
	}
	
	public synchronized void setLocationArea(String value, Context context, boolean notify) {
		putSharedPreference(Setting.PREFERENCE_ACUU_LOCATION_AREA, value, context, notify);
	}
	
	public synchronized WeatherUnit getWeatherUnit(Context context) {
		String value = getSharedPreference(Setting.PREFERENCE_UNIT, context, WeatherUnit.getDefault().name());
		return Enum.valueOf(WeatherUnit.class, value);
	}
//	
	public synchronized String getTemperatureUnit(Context context) {
		String value = getSharedPreference(Setting.PREFERENCE_TEMPERATUREUNIT, context, context.getResources().getString(R.string.Celsius));
		return value;
	}
//	
//	public synchronized void setTemperatureUnit(long storageId, String value, Context context, boolean notify) {
//		putSharedPreference(storageId, Setting.PREFERENCE_TEMPERATUREUNIT, value, context, notify);
//	}
//	
//	public synchronized UpdateMethod getUpdateMethod(long storageId, Context context) {
//		String value = getSharedPreference(storageId, Setting.PREFERENCE_UPDATE_METHOD, context, UpdateMethod.getDefault().name());
//		return Enum.valueOf(UpdateMethod.class, value);
//	}
//		
    private synchronized String getSharedPreference(Setting setting, Context context, String defValue) {
    	return getStorage(context).getString(setting.name(), defValue);
    }
    
    private SharedPreferences getStorage(Context context) {
    	return context.getSharedPreferences(WEATHER_PERFERNCE, Context.MODE_WORLD_WRITEABLE);
    }
    
//    private synchronized void removeSharedPreference(long storageId, Setting setting, Context context) {
//    	SharedPreferences storage = getStorage(storageId, context);
//    	storage.edit().remove(setting.name()).commit();
//    }
    
    private synchronized void putSharedPreference(Setting setting, String value, Context context, boolean notify) {
    	putSharedPreference(setting.name(), value, context, notify);
    }
    
    public synchronized void putSharedPreference(String name, String value, Context context, boolean notify) {
    	SharedPreferences storage = getStorage(context);
    	storage.edit().putString(name, value).commit();    	
//    	if(true) {
//    		notifyListeners(name, value);
//    	}
    }    
//    public synchronized void addSettingListener(long storageId, SettingListener listener) {
//    	listeners.put(storageId, listener);
//    }    
    
//    public synchronized void removeSettingListener(long storageId) {
//    	listeners.remove(storageId);
//    }    
    
    public void setListener(SettingListener listener) {
    	mListener = listener;
    }
    
//    private synchronized void notifyListeners(String name, String value) {
//    	if(mListener != null) {
//    		mListener.settingUpdated(name, value);
//    	}
//    }
    
    public interface SettingListener {
    	void settingUpdated(String name, String value); 
    };
}
