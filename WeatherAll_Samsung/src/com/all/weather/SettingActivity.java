package com.all.weather;

import com.all.weather.SettingsManager.SettingListener;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;

public class SettingActivity extends Activity implements SettingListener {
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		// TODO Auto-generated method stub
		return super.dispatchKeyEvent(event);
	}

	public static String preWeatherFile ="ollweather.info";
	Weather weatherpre;
	String mWeekstr[];
    static final int CITYSETTING_ACUU_REQUEST_CODE = 1;
    static final int CITYSETTING_CENTER_REQUEST_CODE = 2;
    static final int CITYSETTING_GOOGLE_REQUEST_CODE = 3;
	static final int STYLESETTING_REQUEST_CODE = 4;	
	static final int NETBUSY = 5;
	static final int FAILED = 6;
	static final int SUCCESS = 7;
	static final int NONET = 8;
	public static SettingActivity setactity = null;
	Thread update;	
	public static  Weather currentWeather;
//	boolean failed= false;
	private boolean mIscitychanged = false;
	public static boolean mIsGetdataSuccess = false;
	CurrentInfo Weathercurrentinfo;
	ArrayList<SimpleWeatherInfo> forcastinfo;
//	String unit_active;
	String cc;
	String cc_c;
	String ff;
	TextView temperature;
	TextView high_temperature;
	TextView low_temperature;
 	Button city;
 	Button setcity;
	
	ImageView forcast0_image;
	
	ImageView forcast1_image;
	TextView  forcast1_weekanddate;
	TextView  forcast1_high;
	TextView  forcast1_low;
	
	ImageView forcast2_image;
	TextView  forcast2_weekanddate;
	TextView  forcast2_high;
	TextView  forcast2_low;
	
	ImageView forcast3_image;
	TextView  forcast3_weekanddate;
	TextView  forcast3_high;
	TextView  forcast3_low;
	
	ImageView forcast4_image;
	TextView  forcast4_weekanddate;
	TextView  forcast4_high;
	TextView  forcast4_low;
	
	public interface OnItemCompleteListener {
		 void onItemCompleted();
		 void updatecityonly();
	}
	
	Handler viewUpdateHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
        	if(msg.what==NETBUSY){
        		Toast.makeText(SettingActivity.this, R.string.netbusy,
    					Toast.LENGTH_SHORT).show();	
        	}else if(msg.what==FAILED){
//        	    	if(SettingsManager.getInstance().getDateSrcType(setactity).equals("1"))
        	    	    Toast.makeText(SettingActivity.this, R.string.chinanet_failed,//"国内网站 - 获取天气失败，请检查网络..."
        					Toast.LENGTH_SHORT).show();	
//        	    	else if(SettingsManager.getInstance().getDateSrcType(setactity).equals("2")){
//        	    		Toast.makeText(SettingActivity.this, R.string.chinanet_failed,//"国外网站 - 获取天气失败，请检查网络..."
//            					Toast.LENGTH_SHORT).show();	
//        	    	}
        			 String city_en = SettingsManager.getInstance().getLocationCity(SettingActivity.this);
        			 int citystr_id = AccuIconMapper.getCityName(city_en);
        			 if(citystr_id==0)
        				 city.setText(city_en);
        	    	 else
        	    		 city.setText(SettingActivity.this.getResources().getString(citystr_id));
        	    }
        	 else if(msg.what==NONET){
        		 Toast.makeText(SettingActivity.this, R.string.nonet,//"国外网站 - 获取天气失败，请检查网络..."
     					Toast.LENGTH_SHORT).show();	
        		 String city_en = SettingsManager.getInstance().getLocationCity(SettingActivity.this);
    			 int citystr_id = AccuIconMapper.getCityName(city_en);
    			 if(citystr_id==0)
    				 city.setText(city_en);
    	    	 else
    	    		 city.setText(SettingActivity.this.getResources().getString(citystr_id));
        		 
        	 }else {
        	      updateview();
        	 }
             super.handleMessage(msg);
        }
    }; 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(TimeUpdateService.mTuserver == null||TimeUpdateService.mTuserver.lisxxxx == null)							
		{
			TimeUpdateService.restartUpdateService(SettingActivity.this);
		}		
		mIsGetdataSuccess=false;
		mWeekstr =getResources().getStringArray(R.array.weeknames);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		String sSETTINGCITY = SettingsManager.getInstance().getLocationZipCode_w( this);
		SettingsManager.getInstance().setLocationZipCode(sSETTINGCITY, this, false);
		String cityname = SettingsManager.getInstance().getLocationCity_w( this);
		SettingsManager.getInstance().setLocationCity( cityname, this, false);
		
		setactity = this;
		setContentView(R.layout.weather_setting);
		cc =this.getResources().getString(R.string.Celsius);
	 	ff =this.getResources().getString(R.string.Fahrenheit);	
	 	cc_c =this.getResources().getString(R.string.Celsius_c);
		TimeUpdateService.ISSETTING = true;
		if(TimeUpdateService.weather!=null){
			currentWeather = TimeUpdateService.weather;
			Weathercurrentinfo = TimeUpdateService.weather.getCurrentInfo();
			forcastinfo = TimeUpdateService.weather.getSimpleimfos();
			initconponent();				
		}else{
			initconponent();
			flush_new_weather();
		}
	}
    private void after_url_changed(){
        // 1 写入控制数据源的shareperference对象。
        // 2 重新flush(一下)天气数据。
        // 3 通知桌面图标更新信息。
        // 4 在 SettingsManager里的get,set方法中，重设根据数据源来返回不同的数据源的值。
    }
    
    private String getdata(String data,String week)
     {
    	Log.i("xia",data+"  "+week);
    	 String[] datagroup = data.split("/");
    	 if(datagroup.length>1)
         {
    		 Log.i("xia", getweek(week) + "."+datagroup[1]);
    		return getweek(week) + "."+datagroup[1];
         }
    	 else {
    		 Log.i("xia", getweek(week) +"."+datagroup[0]);
    		 return getweek(week) +"."+datagroup[0];
    	 }
    		 
     }
    
    
    private String getweek(String week)
    {
    	if(week.equals("Sunday"))
		  {
    		if(mWeekstr[1].length()>3)
			 return mWeekstr[1].subSequence(0, 3).toString().trim();
    		else return mWeekstr[1];
		  }
		 else if(week.equals("Monday"))
		 {
			 if(mWeekstr[2].length()>3)
			 return mWeekstr[2].subSequence(0, 3).toString().trim();
			 else return mWeekstr[2];
		 }
		 else if(week.equals("Tuesday"))
		 {
			 if(mWeekstr[3].length()>3)
				 return mWeekstr[3].subSequence(0, 3).toString().trim();
				 else return mWeekstr[3];
		 }
		 else if(week.equals("Wednesday"))
		 {
			 if(mWeekstr[4].length()>3)
				 return mWeekstr[4].subSequence(0, 3).toString().trim();
				 else return mWeekstr[4];
		 }
		 else if(week.equals("Thursday"))
		 {
			 if(mWeekstr[5].length()>3)
				 return mWeekstr[5].subSequence(0, 3).toString().trim();
				 else return mWeekstr[5];
		 }
		 else if(week.equals("Friday"))
		 {
			 if(mWeekstr[6].length()>3)
				 return mWeekstr[6].subSequence(0, 3).toString().trim();
				 else return mWeekstr[6];
		 }
		 else 
			 if(mWeekstr[7].length()>3)
				 return mWeekstr[7].subSequence(0, 3).toString().trim();
				 else return mWeekstr[7];
    }
	private void updateview(){
		 if(Weathercurrentinfo.getWeathericon()==null)
			return;
		 String city_en = SettingsManager.getInstance().getLocationCity( this);
		 int citystr_id = AccuIconMapper.getCityName(city_en);
		 if(citystr_id==0)
			 city.setText(city_en);
    	 else
    		 city.setText(this.getResources().getString(citystr_id));
     	 forcast0_image.setBackgroundResource(AccuIconMapper.getDrawableIdByIconId(300+Integer.parseInt(Weathercurrentinfo.getWeathericon())));	        
         // next 1 	  
     	 forcast1_image.setBackgroundResource(AccuIconMapper.getDrawableIdByIconId(300+Integer.parseInt(forcastinfo.get(1).getIcon())));	        
       forcast1_weekanddate.setText(getdata(forcastinfo.get(1).getData(),forcastinfo.get(1).getWeek()));
		
        // next 2 	  
     	 forcast2_image.setBackgroundResource(AccuIconMapper.getDrawableIdByIconId(300+Integer.parseInt(forcastinfo.get(2).getIcon())));	        
        forcast2_weekanddate.setText(getdata(forcastinfo.get(2).getData(),forcastinfo.get(2).getWeek()));
        // next 3 	  
     	 forcast3_image.setBackgroundResource(AccuIconMapper.getDrawableIdByIconId(300+Integer.parseInt(forcastinfo.get(3).getIcon())));	        
       forcast3_weekanddate.setText(getdata(forcastinfo.get(3).getData(),forcastinfo.get(3).getWeek()));
        
        // next 4 	  
     	 forcast4_image.setBackgroundResource(AccuIconMapper.getDrawableIdByIconId(300+Integer.parseInt(forcastinfo.get(4).getIcon())));	        
        forcast4_weekanddate.setText(getdata(forcastinfo.get(4).getData(),forcastinfo.get(4).getWeek()));
        // next 5 
        
        String Unit =SettingsManager.getInstance().getTemperatureUnit( this); 
       
        if(Unit.equals(cc)){
        	if(Weathercurrentinfo.getCu_tempreture().equals("")){
        		temperature.setText("");
        	}
        	else
			   temperature.setText(Weathercurrentinfo.getCu_tempreture()+cc);
		     high_temperature.setText(forcastinfo.get(0).getHightempreture()+cc);
		     low_temperature.setText(forcastinfo.get(0).getLowtempreture()+cc);
		     
		     forcast1_high.setText(forcastinfo.get(1).getHightempreture()+cc);
		     forcast1_low.setText(forcastinfo.get(1).getLowtempreture()+cc);
		     
		     forcast2_high.setText(forcastinfo.get(2).getHightempreture()+cc);
		     forcast3_high.setText(forcastinfo.get(3).getHightempreture()+cc);
		     forcast4_high.setText(forcastinfo.get(4).getHightempreture()+cc);
		     forcast2_low.setText(forcastinfo.get(2).getLowtempreture()+cc);
		     forcast3_low.setText(forcastinfo.get(3).getLowtempreture()+cc);
		     forcast4_low.setText(forcastinfo.get(4).getLowtempreture()+cc);
        }else{
       	 
		    temperature.setText(ctof(Weathercurrentinfo.getCu_tempreture()));
		    high_temperature.setText(ctof(forcastinfo.get(0).getHightempreture()));
		    low_temperature.setText(ctof(forcastinfo.get(0).getLowtempreture()));
		    
		    forcast1_high.setText(ctof(forcastinfo.get(1).getHightempreture())+ff);
		    forcast1_low.setText(ctof(forcastinfo.get(1).getLowtempreture())+ff);
		    forcast2_high.setText(ctof(forcastinfo.get(2).getHightempreture())+ff);
		    forcast3_high.setText(ctof(forcastinfo.get(3).getHightempreture())+ff);
		    forcast4_high.setText(ctof(forcastinfo.get(4).getHightempreture())+ff);
		    
		    forcast2_low.setText(ctof(forcastinfo.get(2).getLowtempreture())+ff);
		    forcast3_low.setText(ctof(forcastinfo.get(3).getLowtempreture())+ff);
		    forcast4_low.setText(ctof(forcastinfo.get(4).getLowtempreture())+ff);
       }
	}
	
	private String ctof(String c){
		Double f = (Integer.parseInt(c)*9/5.0+32+0.5) ;
    	return String.valueOf(f.intValue());   	
	}
	
	private void initconponent(){
		temperature=(TextView)findViewById(R.id.temperature);
		high_temperature=(TextView)findViewById(R.id.high_temperature);
        low_temperature=(TextView)findViewById(R.id.low_temperature);	
     	setcity = (Button)findViewById(R.id.setcity);
     	setcity.setText(R.string.setcity);
     	setcity.setOnClickListener(SET_MY_CITY_LISTENER);
      	city=(Button)findViewById(R.id.city);
		
 		forcast0_image=(ImageView)findViewById(R.id.forcast0_image);
 		
        forcast1_image=(ImageView)findViewById(R.id.forcast1_image);
 		forcast1_weekanddate=(TextView)findViewById(R.id.forcast1_weekanddate);
 		forcast1_high=(TextView)findViewById(R.id.forcast1_high);
 		forcast1_low=(TextView)findViewById(R.id.forcast1_low);
 		
 		forcast2_image=(ImageView)findViewById(R.id.forcast2_image);
  		forcast2_weekanddate=(TextView)findViewById(R.id.forcast2_weekanddate);
  		forcast2_high=(TextView)findViewById(R.id.forcast2_high);
  		forcast2_low=(TextView)findViewById(R.id.forcast2_low);
  		  		
 		forcast3_image=(ImageView)findViewById(R.id.forcast3_image);
 		forcast3_weekanddate=(TextView)findViewById(R.id.forcast3_weekanddate);
       forcast3_high=(TextView)findViewById(R.id.forcast3_high);
 		forcast3_low=(TextView)findViewById(R.id.forcast3_low);
 		
 		forcast4_image=(ImageView)findViewById(R.id.forcast4_image);
 		forcast4_weekanddate=(TextView)findViewById(R.id.forcast4_weekanddate);
  		forcast4_high=(TextView)findViewById(R.id.forcast4_high);
  		forcast4_low=(TextView)findViewById(R.id.forcast4_low);
  		
  		
  		 String city_en = SettingsManager.getInstance().getLocationCity(this);
		 int citystr_id = AccuIconMapper.getCityName(city_en);
		 if(citystr_id==0)
			 city.setText(city_en);
    	 else
    		 city.setText(this.getResources().getString(citystr_id));
  		
      	city.setOnClickListener(CITY_SETTING_LISTENER);
      	
//  		unit_active = SettingsManager.getInstance().getTemperatureUnit(this);
 		
 		if(TimeUpdateService.weather==null){
 			
 			 if(weatherpre==null||weatherpre.getCurrentInfo()==null)
 			    readinfofromfile();
 			    CurrentInfo cu = weatherpre.getCurrentInfo(); 
                         if(weatherpre==null)
                      {
                        weatherpre = new Weather();
		        Calendar cal = Calendar.getInstance();
				  if(SettingsManager.getInstance().getDateSrcType(this).equals("1"))
					  weatherpre.setCurrentInfo(new CurrentInfo(getResources().getString(R.string.default_center_location_city),getResources().getString(R.string.default_location_area) , "",	"01",getResources().getString(R.string.icon0),	"4","75%","M"));
					  else if(SettingsManager.getInstance().getDateSrcType(this).equals("2")){
					  weatherpre.setCurrentInfo(new CurrentInfo(getResources().getString(R.string.default_location_city),getResources().getString(R.string.default_location_area) , "12",	"01", getResources().getString(R.string.icon0),	"4","75%","M"));
					  }
					  else
					  weatherpre.setCurrentInfo(new CurrentInfo(getResources().getString(R.string.default_location_city),getResources().getString(R.string.default_location_area) , "12",	"01", getResources().getString(R.string.icon0),	"4","75%","M"));
					 
		        weatherpre.getSimpleimfos().add(new SimpleWeatherInfo(datestring(cal), "7", "01","-2", "0", "sunshine",getweek(cal)+"day", "NW", "20"));
		        cal.add(Calendar.DATE, 1);
		        weatherpre.getSimpleimfos().add(new SimpleWeatherInfo(datestring(cal), "8", "01","0", "0", "sunshine",getweek(cal)+"day", "NW", "20"));
		        cal.add(Calendar.DATE, 1);
		        weatherpre.getSimpleimfos().add(new SimpleWeatherInfo(datestring(cal), "9", "02","1", "0", "sunshine",getweek(cal)+"day", "NW", "20"));
		        cal.add(Calendar.DATE, 1);
		        weatherpre.getSimpleimfos().add(new SimpleWeatherInfo(datestring(cal), "10", "15","2", "0", "sunshine",getweek(cal)+"day", "NW", "20"));
		        cal.add(Calendar.DATE, 1);
		        weatherpre.getSimpleimfos().add(new SimpleWeatherInfo(datestring(cal), "11", "06","5", "0", "sunshine",getweek(cal)+"day", "NW", "20"));
		        cal.add(Calendar.DATE, 1);
		        weatherpre.getSimpleimfos().add(new SimpleWeatherInfo(datestring(cal), "12", "23","6", "0", "sunshine",getweek(cal)+"day", "NW", "20"));
		        cal.add(Calendar.DATE, 1);
		        weatherpre.getSimpleimfos().add(new SimpleWeatherInfo(datestring(cal), "13", "12","6", "0", "sunshine",getweek(cal)+"day", "NW", "20"));
                       }			
 	 			ArrayList<SimpleWeatherInfo> forcast = weatherpre.getSimpleimfos();
 	 			if(cu==null||cu.getWeathericon()==null)
 	 				return; 			
		    forcast1_weekanddate.setText(getdata(forcast.get(1).getData(),forcast.get(1).getWeek()));
		    forcast2_weekanddate.setText(getdata(forcast.get(2).getData(),forcast.get(2).getWeek()));
		    forcast3_weekanddate.setText(getdata(forcast.get(3).getData(),forcast.get(3).getWeek()));
		    forcast4_weekanddate.setText(getdata(forcast.get(4).getData(),forcast.get(4).getWeek()));
		    int iconid = Integer.parseInt(cu.getWeathericon());
		    forcast0_image.setBackgroundResource(AccuIconMapper.getDrawableIdByIconId(300+iconid));	        
		    iconid = Integer.parseInt(forcast.get(1).getIcon());
		    forcast1_image.setBackgroundResource(AccuIconMapper.getDrawableIdByIconId(300+iconid));	        
		    iconid = Integer.parseInt(forcast.get(2).getIcon());
		    forcast2_image.setBackgroundResource(AccuIconMapper.getDrawableIdByIconId(300+iconid));       
		    iconid = Integer.parseInt(forcast.get(3).getIcon());
		    forcast3_image.setBackgroundResource(AccuIconMapper.getDrawableIdByIconId(300+iconid));        
		    iconid = Integer.parseInt(forcast.get(4).getIcon());
		    forcast4_image.setBackgroundResource(AccuIconMapper.getDrawableIdByIconId(300+iconid));
		    settemperature(cu,forcast);
  	    }
 		else {
 			updateview();
 		}
	}
	private String getstr(int date)
	{
		return (date>9)?date+"":"0"+date;
	}
	
	private void settemperature(CurrentInfo cu,ArrayList<SimpleWeatherInfo> forcast){
//		String unit = SettingsManager.getInstance().getTemperatureUnit(SettingsManager.STORAGEID, this);		
//	   if(unit.equals(cc)){
		if(cu.getCu_tempreture().equals(""))
			temperature.setText("");
		else
	    	temperature.setText(cu.getCu_tempreture()+cc);
	    	high_temperature.setText(forcast.get(0).getHightempreture()+cc);
	    	low_temperature.setText(forcast.get(0).getLowtempreture()+cc);
	    	forcast1_high.setText(forcast.get(1).getHightempreture()+cc);
	    	forcast1_low.setText(forcast.get(1).getLowtempreture()+cc);
	    	
	    	forcast2_high.setText(forcast.get(2).getHightempreture()+cc);
	    	forcast3_high.setText(forcast.get(3).getHightempreture()+cc);
	    	forcast4_high.setText(forcast.get(4).getHightempreture()+cc);
	    	forcast2_low.setText(forcast.get(2).getLowtempreture()+cc);
	    	forcast3_low.setText(forcast.get(3).getLowtempreture()+cc);
	    	forcast4_low.setText(forcast.get(4).getLowtempreture()+cc);
//	   }else
//	        {
//	    	   temperature.setText(ctof(cu.getCu_tempreture()));
//	    	   high_temperature.setText(ctof(forcast.get(0).getHightempreture()));
//	    	   low_temperature.setText(ctof(forcast.get(0).getLowtempreture()));
//	    	   forcast1_high.setText(ctof(forcast.get(1).getHightempreture())+ff);
//	    	   forcast1_low.setText(ctof(forcast.get(1).getLowtempreture())+ff);
//	    	   forcast2_high.setText(ctof(forcast.get(2).getHightempreture())+ff);
//	    	   forcast3_high.setText(ctof(forcast.get(3).getHightempreture())+ff);
//	    	   forcast4_high.setText(ctof(forcast.get(4).getHightempreture())+ff);
//	    	   forcast2_low.setText(ctof(forcast.get(2).getLowtempreture())+ff);
//	    	   forcast3_low.setText(ctof(forcast.get(3).getLowtempreture())+ff);
//	    	   forcast4_low.setText(ctof(forcast.get(4).getLowtempreture())+ff);
//	        
//	        }
	}
	
	private String getweek(Calendar today){
	    int a = today.get(Calendar.DAY_OF_WEEK);
	    return mWeekstr[a];
	}
	

    private boolean checkNetworkInfo()    {        
    	ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    	if (connectivity == null) {
    		return false;
    	} else {
    		NetworkInfo[] info = connectivity.getAllNetworkInfo();
	    	if (info != null) {
	    		for (int i = 0; i < info.length; i++){
		    		if (info[i].getState() == NetworkInfo.State.CONNECTED) {
		    		   return true;
		    		}
	    		}
	    	}
    	}
    	return false;
    	
//    	//mobile 3G Data Network 
//    	State mobile = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
//    	
//    	State wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
//    	if(mobile==State.CONNECTED||mobile==State.CONNECTING)            
//    		return true;        
//    	if(wifi==State.CONNECTED||wifi==State.CONNECTING)
//    		return true;  
////    	 startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
//    	return false;
    }
	
    private boolean flushnew(boolean isclickflush) {
    	Log.i("xia", ". .aa ... runing");
        String currentcityZipCode = SettingsManager.getInstance().getLocationZipCode(this);
        String oldcityZipCode = SettingsManager.getInstance().getLocationZipCode_w(this);
        if (!isclickflush) {
        	Log.i("xia"," --bb-- currentcityZipCode = "+currentcityZipCode+"\n##oldcityZipCode = "+oldcityZipCode);
            if (currentcityZipCode.equals(oldcityZipCode) && currentWeather != null)
                return true;
        } else {
        	
            if (WeatherReader.isgeting == true){
            	viewUpdateHandler.sendEmptyMessage(NETBUSY);
            	Log.i("xia", ". .bb ... isgeting return;");
                return false;
            }
        }
        
        
        if(checkNetworkInfo()){
	        if(SettingsManager.getInstance().getDateSrcType(this).equals("1")){
	        	currentWeather = WeatherReader.getCenterWeather(this, currentcityZipCode);
	        } else if(SettingsManager.getInstance().getDateSrcType(this).equals("2")) {
	            currentWeather = WeatherReader.getAcuuWeather(this, currentcityZipCode);
	        }else{
	        	currentWeather = WeatherReader.getGoogleWeather(this, currentcityZipCode);
	        }
        }
        else{
        	Message m = new Message();
            m.what=NONET;
            viewUpdateHandler.sendMessage(m);
        	return false;
        }
        
        Log.i("xia"," .. cc  ...  after get weather() ---- "+currentWeather);
       if (currentWeather == null) {
//           failed = true;
           Log.i("xia"," .. dd  ...  failed" );
           Message m = new Message();
           m.what=FAILED;
           viewUpdateHandler.sendMessage(m);
           return false;
       }
//       failed = false;
        Weathercurrentinfo = currentWeather.getCurrentInfo();
        forcastinfo = currentWeather.getSimpleimfos();
        Message m = new Message();
        viewUpdateHandler.sendMessage(m);
        return true;
        
        
    }
 
	@Override
	protected void onPause() {
		if(update!=null)
		update.interrupt();
		update=null;
		super.onPause();
	} 
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	private OnClickListener ICON_TYPE_SETTING_LISTENER = new OnClickListener() {
    	public void onClick(View v) {
    		Intent intent = new Intent();
    		intent.setClass(SettingActivity.this,Icontypesetting.class);
    		startActivityForResult(intent,STYLESETTING_REQUEST_CODE);
    		}
    	};
	
    	private OnClickListener SET_MY_CITY_LISTENER = new OnClickListener() {
        	public void onClick(View v) {
        		
        		
        	String	current_cityzipcode = SettingsManager.getInstance().getLocationZipCode(SettingActivity.this);
        	String	old_cityzipcode = SettingsManager.getInstance().getLocationZipCode_w(SettingActivity.this);
        	Log.i("xia", "current_cityzipcode:"+current_cityzipcode);
        	if((!current_cityzipcode.equals(old_cityzipcode))||current_cityzipcode.equals("ASI|HK|HK%2D%2D%2D|HONG+KONG")||current_cityzipcode.equals("ASI|CN|CH014|SHAOSHAN"))
        	{
        		if(mIsGetdataSuccess)
        		{
        			 ObjectOutputStream out = null;
        			  try{
        		         out = new ObjectOutputStream(openFileOutput(preWeatherFile,Context.MODE_PRIVATE));
        		         out.writeObject(currentWeather);
        		         out.flush();
        		         out.close();
        			  } 
        			  catch (FileNotFoundException ex2){Log.i("xia", "file Exception");} 
        			  catch (IOException ex2) {Log.i("xia", "file Exception");}
        			  catch(Exception ee){Log.i("xia", "file Exception");}
        		}
        		String current_cityname = SettingsManager.getInstance().getLocationCity(SettingActivity.this);	
        		String current_cityArea = SettingsManager.getInstance().getLocationArea(SettingActivity.this);	
        		
                SettingsManager.getInstance().setLocationCity_w(current_cityname, SettingActivity.this, false);
        		SettingsManager.getInstance().setLocationZipCode_w(current_cityzipcode, SettingActivity.this, false);
        		SettingsManager.getInstance().setLocationArea_w(current_cityArea, SettingActivity.this, false);
        		mIscitychanged = true;
        	}
        	Toast.makeText(SettingActivity.this, SettingActivity.this.getResources().getString(R.string.set), Toast.LENGTH_SHORT).show();
			TimeUpdateService.weather = currentWeather;
			TimeUpdateService.mTuserver.lisxxxx.onItemCompleted();
        		}
        	};
        	
	private OnClickListener CITY_SETTING_LISTENER = new OnClickListener() {
	   public void onClick(View v) {
    		Intent intent = new Intent();
    		 if(SettingsManager.getInstance().getDateSrcType(SettingActivity.this).equals("1"))
    		 {
    			 Log.i("xia","--------------CitySetting_Center.class");
    			 intent.setClass(SettingActivity.this,CitySetting_Center.class ); 
    		     startActivityForResult(intent,CITYSETTING_CENTER_REQUEST_CODE); 
    		 }
    		 else if(SettingsManager.getInstance().getDateSrcType(SettingActivity.this).equals("2")){
    			 Log.i("xia","--------------CitySetting_Accu.class");
    		     intent.setClass(SettingActivity.this,CitySetting_Accu.class );
    		     startActivityForResult(intent,CITYSETTING_ACUU_REQUEST_CODE); 
    		 }else{
    		     intent.setClass(SettingActivity.this,CitySetting_Google.class );
    		     startActivityForResult(intent,CITYSETTING_ACUU_REQUEST_CODE); 	 
    			 
    			 
    		 } 
    		}   
    	};
	private Object getProgressDialog;
    	
   
    	
    private void flush_new_weather(){
        update = new Thread(){  
            public void run(){
                Log.i("xia",".... runing");
                mIsGetdataSuccess = flushnew(false);
            }
        };
      update.start(); 
    }
    	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == CITYSETTING_ACUU_REQUEST_CODE){
	    	 String city_en = SettingsManager.getInstance().getLocationCity( this);
			 int citystr_id = AccuIconMapper.getCityName(city_en);
			 if(citystr_id==0)
				 city.setText(city_en);
	    	 else
	    		 city.setText(this.getResources().getString(citystr_id));
			 flush_new_weather();	    	
		}else if(requestCode == CITYSETTING_CENTER_REQUEST_CODE){
//			
//			String citycode = SettingsManager.getInstance().getLocationZipCode(this);
			String cityname = SettingsManager.getInstance().getLocationCity(this);
//			String shengname = SettingsManager.getInstance().getLocationArea(this);
//			Log.i("xia","citycode="+citycode+"  cityname = "+cityname+"   sheng "+shengname );
			city.setText(cityname);
            flush_new_weather();
		}else if(requestCode == CITYSETTING_GOOGLE_REQUEST_CODE){
			String cityname = SettingsManager.getInstance().getLocationCity(this);
			city.setText(cityname);
            flush_new_weather();
		}
		else if(requestCode == STYLESETTING_REQUEST_CODE){
			if (resultCode == RESULT_CANCELED)
				return;
			else if (resultCode == RESULT_OK){
			}
		}
	}
	
	@Override
	protected void onDestroy() {
		
		Log.d("xia", "enter SettingActiviy --> onDestry()");
		
		if(TimeUpdateService.mTuserver == null) {
			super.onDestroy();
			return;
		}
		
		if(this.mIscitychanged)//城市发生了变化
		{		
			Log.d("xia", "--- 1 SettingActiviy --> citychanged");
			TimeUpdateService.weather = this.currentWeather;
			TimeUpdateService.mTuserver.lisxxxx.onItemCompleted();
		}
		else//城市没有变化，但重新取到了数据
		{
			Log.d("xia", "--- 1 SettingActiviy --> not citychanged");
			String	current_cityzipcode = SettingsManager.getInstance().getLocationZipCode(SettingActivity.this);
    	   String	old_cityzipcode = SettingsManager.getInstance().getLocationZipCode_w( SettingActivity.this);
    
			if(this.mIsGetdataSuccess&&current_cityzipcode.equals(old_cityzipcode))
				{
				   Log.d("xia", "---  2  SettingActiviy --> chuan di le.");
				   TimeUpdateService.weather = this.currentWeather;
				   TimeUpdateService.mTuserver.lisxxxx.onItemCompleted();
				}
		}
		super.onDestroy();
	}
	
   private void readinfofromfile(){
    	try {    
			 FileInputStream bi = openFileInput(preWeatherFile);
			 ObjectInputStream inn = new ObjectInputStream(bi);
			 Object oo = inn.readObject();  
			 inn.close();
			 weatherpre = (Weather) oo;	   
        }catch (Exception exception){
		  exception.printStackTrace();
		  weatherpre = new Weather();
		  Calendar cal = Calendar.getInstance();
		  if(SettingsManager.getInstance().getDateSrcType(this).equals("1"))
		  weatherpre.setCurrentInfo(new CurrentInfo(getResources().getString(R.string.default_center_location_city),getResources().getString(R.string.default_location_area) , "",	"01", "清朗",	"4","75%","M"));
		  else if(SettingsManager.getInstance().getDateSrcType(this).equals("2")){
		  weatherpre.setCurrentInfo(new CurrentInfo(getResources().getString(R.string.default_location_city),getResources().getString(R.string.default_location_area) , "12",	"01", "清朗",	"4","75%","M"));
		  }
		  else{
		  weatherpre.setCurrentInfo(new CurrentInfo(getResources().getString(R.string.default_location_city),getResources().getString(R.string.default_location_area) , "12",	"01", "清朗",	"4","75%","M"));
		  }
		  weatherpre.getSimpleimfos().add(new SimpleWeatherInfo(datestring(cal), "7", "01","-2", "0", "sunshine",getweek(cal)+"day", "NW", "20"));
		  cal.add(Calendar.DATE, 1);
		  weatherpre.getSimpleimfos().add(new SimpleWeatherInfo(datestring(cal), "8", "01","0", "0", "sunshine",getweek(cal)+"day", "NW", "20"));
		  cal.add(Calendar.DATE, 1);
		  weatherpre.getSimpleimfos().add(new SimpleWeatherInfo(datestring(cal), "9", "02","1", "0", "sunshine",getweek(cal)+"day", "NW", "20"));
		  cal.add(Calendar.DATE, 1);
		  weatherpre.getSimpleimfos().add(new SimpleWeatherInfo(datestring(cal), "10", "15","2", "0", "sunshine",getweek(cal)+"day", "NW", "20"));
		  cal.add(Calendar.DATE, 1);
		  weatherpre.getSimpleimfos().add(new SimpleWeatherInfo(datestring(cal), "11", "06","5", "0", "sunshine",getweek(cal)+"day", "NW", "20"));
		  cal.add(Calendar.DATE, 1);
		  weatherpre.getSimpleimfos().add(new SimpleWeatherInfo(datestring(cal), "12", "23","6", "0", "sunshine",getweek(cal)+"day", "NW", "20"));
		  cal.add(Calendar.DATE, 1);
		  weatherpre.getSimpleimfos().add(new SimpleWeatherInfo(datestring(cal), "13", "12","6", "0", "sunshine",getweek(cal)+"day", "NW", "20"));
		  ObjectOutputStream out = null;
		  try{
		       out = new ObjectOutputStream(openFileOutput(preWeatherFile,Context.MODE_PRIVATE));
		       out.writeObject(weatherpre);
		       out.flush();
		       out.close();
		   }catch (FileNotFoundException ex2){}
			  catch (IOException ex2) {}
			  catch(Exception ee){}
	       }
   }
   
    private String datestring(Calendar today){
    	return today.get(Calendar.YEAR)+"/"+(today.get(Calendar.MONTH)+1)+"/"+today.get(Calendar.DATE);    	
    }

	public void settingUpdated(String name, String value) {
		// TODO Auto-generated method stub
	}	
	 public boolean onCreateOptionsMenu(Menu menu) {
	  menu.add(0, 1, 0, R.string.menu_cen); 
	  menu.add(0, 2, 0, R.string.menu_acu); 
	  menu.add(0, 3, 0, R.string.flush);
	  menu.add(0, 4, 0, R.string.aboutt);
	  return super.onCreateOptionsMenu(menu);
	 }
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		int now = item.getItemId();
		switch (now) {
		case 4 :
			Log.i("xia", "case : 4");
			Intent intent = new Intent();
			intent.setClass(SettingActivity.this,AboutAcitivity.class );
			this.startActivity(intent);
			break;
		case 1:
		case 2:
			Log.i("xia", "case : 1 2");
			if (String.valueOf(now) == SettingsManager.getInstance()
					.getDateSrcType(this)) {
				Toast.makeText(SettingActivity.this, R.string.menu_info,
						Toast.LENGTH_SHORT).show();
				return true;
			}
			SettingsManager.getInstance().setDateSrcType(
					String.valueOf(item.getItemId()), this, false);
			if (now == 1){
				Toast.makeText(SettingActivity.this, R.string.menu_cent,
						Toast.LENGTH_SHORT).show();
			}
			else if(now == 2){
				Toast.makeText(SettingActivity.this, R.string.menu_acuu,
						Toast.LENGTH_SHORT).show();
			}
			else{
				
			};
			
			update = new Thread() {
				public void run() {
					
					mIsGetdataSuccess = flushnew(true);
					if (mIsGetdataSuccess) {
						TimeUpdateService.weather = currentWeather;
						TimeUpdateService.mTuserver.lisxxxx.onItemCompleted();
					}
					else{
						TimeUpdateService.mTuserver.lisxxxx.updatecityonly();
					}
				}
			};
			update.start();		
			break;
			
		case 3:
			Log.i("xia", "case : 3");
			update = new Thread() {
				public void run() {
					
					mIsGetdataSuccess = flushnew(true);
					if (mIsGetdataSuccess) {
						TimeUpdateService.weather = currentWeather;
						TimeUpdateService.mTuserver.lisxxxx.onItemCompleted();
					}
					else{
						if(TimeUpdateService.mTuserver!=null&&TimeUpdateService.mTuserver.lisxxxx!=null)							
						{
							TimeUpdateService.mTuserver.lisxxxx.updatecityonly();
						}
						else
						{
							TimeUpdateService.restartUpdateService(SettingActivity.this);
						}
					}
				}
			};
			update.start();
			break;	
		
		}

		
		return true;
	}
	 
//	private Handler handler =  new Handler(){
//		@Override
//		public void handleMessage(Message msg) {
//			// TODO Auto-generated method stub
////			progressDialog.dismiss();
//			
//			super.handleMessage(msg);
//		}
//	  };
//	  private ProgressDialog progressDialog;
//	  private ProgressDialog getProgressDialog() {
//	        progressDialog = null;
//	        progressDialog = new ProgressDialog(this);
//	        progressDialog.setMessage("获取天气中...");
//	        progressDialog.setIndeterminate(true);
//	        progressDialog.setCancelable(true);
//	        return progressDialog;
//	    }
}
