package com.all.weather;
//import com.mediatek.featureoption.FeatureOption;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.Activity;
import android.app.ActivityManager.RunningTaskInfo;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

public class TimeUpdateService extends Service {
    int mIconid = 0;
    public static boolean isRemoteView = false;
	Calendar mCalendar;
	private int hour = 0,minute =	0;	
    private static String Tag = "xia";
	public  int sLastId=-1;
    public static String CITYCODE;
    public static Weather weather = null;
    Weather weatherpre;
    public static boolean ISSETTING = false;
    public static  TimeUpdateService mTuserver;
    private RemoteViews mViews = null;
    private ComponentName mWidgetComponent = null;
    private final Handler mHandler = new Handler();
    private static final long WEATHER_UPDATE_DELAY = 120;
    private static final long FAIL_UPDATE_WEATHER_PERIOD_IN_MINS = 15;//15;
    private static  long UPDATE_WEATHER_PERIOD_IN_MINS = WEATHER_UPDATE_DELAY;
    HashMap<String, String> mLauncherActivities;
    public static String preWeatherFile = "ollweather.info";
    String cc;
    String cc_c;
    String ff;
    String mWeekstr[];
    String mMonthsstr[];
    static String w[];
    //String weatherstyle;
    PendingIntent pendingIntent;    
    private boolean mSwitcher = false;
    private boolean mScreen_on = true;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    public SettingActivity.OnItemCompleteListener lisxxxx = new SettingActivity.OnItemCompleteListener() {
        public void onItemCompleted() {
        	updateItemUI();
            }
        public void updatecityonly(){
       	 String city_en = SettingsManager.getInstance().getLocationCity_w(TimeUpdateService.this);
       	        	       	 
       	 //modify by xia 0614 on
       	if(weather==null)
			weather=weatherpre;
       	 int iconid = Integer.parseInt(weather.getCurrentInfo().getWeathericon());
       	 if (SettingsManager.getInstance().getDateSrcType(TimeUpdateService.this).equals("1")) {
			//weather = WeatherReader.getCenterWeather(this, CITYCODE);
    	 mViews.setTextViewText(R.id.cloud,weather.getCurrentInfo().getWeathertext().trim());
    	 mViews.setTextViewText(R.id.city, city_en); 
    	 
		} else if (SettingsManager.getInstance().getDateSrcType(TimeUpdateService.this)
				.equals("2")) {
			//weather = WeatherReader.getAcuuWeather(this, CITYCODE);
			int citystr_id = AccuIconMapper.getCityName(city_en); 
			mViews.setTextViewText(R.id.cloud,getResources().getString(AccuIconMapper.getWeatherDescription(iconid)));
			 if(citystr_id==0)
	       		    mViews.setTextViewText(R.id.city, city_en); 
	       	 else
	                mViews.setTextViewText(R.id.city, TimeUpdateService.this.getResources().getString(citystr_id));

		}  
       	 //add by xia 0614 off
       	    updateRomoteView();
        }
    };
    
    
    private void updateRomoteView(){   
    	
    	Log.i("xia", "---updateRomoteView------");
    	Weather xweather = weather;
    	if(xweather==null)
    		xweather = weatherpre;
    	
    		String city_en = SettingsManager.getInstance().getLocationCity_w(this);
       	 int citystr_id = AccuIconMapper.getCityName(city_en);
       	 
       	 if(citystr_id!=0)
       		city_en = this.getResources().getString(citystr_id);   
       	 
            Intent tolauncher = new Intent("weather_info");
            tolauncher.putExtra("city", city_en);
            tolauncher.putExtra("h_tempretrue", xweather.getSimpleimfos().get(0).getHightempreture());
            tolauncher.putExtra("l_tempretrue", xweather.getSimpleimfos().get(0).getLowtempreture());
            tolauncher.putExtra("c_tempretrue", xweather.getCurrentInfo().getCu_tempreture());
            Time time = new Time();
            time.setToNow();
            //if(time.hour>=6&&time.hour<19) 
            //tolauncher.putExtra("Icon_id", 100+Integer.parseInt(weather.getCurrentInfo().getWeathericon()));
            //else 
            tolauncher.putExtra("Icon_id", 300+Integer.parseInt(xweather.getCurrentInfo().getWeathericon()));	 
            this.sendBroadcast(tolauncher);
    	 
    	if(isRemoteView){
          	 AppWidgetManager managera = AppWidgetManager.getInstance(TimeUpdateService.this);
               managera.updateAppWidget(mWidgetComponent, mViews);
          	 }
    }
    private boolean updateItemUI() {
    	
    	Log.i("xia", "----updateItemUI()----");
    	 String city_en = SettingsManager.getInstance().getLocationCity_w(this);
    	 int citystr_id = AccuIconMapper.getCityName(city_en);
    	 if(citystr_id==0)
    		 mViews.setTextViewText(R.id.city, city_en);
    	 else
             mViews.setTextViewText(R.id.city, this.getResources().getString(citystr_id));
    	  
    	 
    	 updateRomoteView();
         if(weather==null){ 
			weather=weatherpre;
			if(weather==null||weather.getCurrentInfo().getCu_tempreture()==null)
		          {
				return false;
			}
		}
         
        	if(SettingActivity.mIsGetdataSuccess) { //if get data from net success and same city
    			if(mSwitcher) {
                    mViews = new RemoteViews(getPackageName(), R.layout.weather_main);
                     mSwitcher=false;
  				} else
  				{
  					 mViews = new RemoteViews(getPackageName(), R.layout.weather_mainother);
  	               mSwitcher=true;
  				}
    			this.upDateWeather(weather);
    		SettingActivity.mIsGetdataSuccess=false;
    	} else { 
    			  new Thread(){
    	  			  public void run(){
    	  				  updateWeather();
    	  		      }}.start(); 
  		    SettingActivity.mIsGetdataSuccess=false;
    	}
        return true;
    }
    
    private void readinfofromfile(){

    	try {    
		 FileInputStream bi = openFileInput(preWeatherFile);
		 ObjectInputStream inn = new ObjectInputStream(bi);
		 Object oo = inn.readObject();  
		 inn.close();
		 weatherpre = (Weather) oo;
		 String icon = weatherpre.getCurrentInfo().getWeathericon();

		 weatherpre.getCurrentInfo().setWeathericon(icon);
		 Log.i("xia", "Current id:  "+icon);
    } 
	  catch (Exception exception){
		  Log.i("xia", "11file Exception");
		  weatherpre = new Weather();
		  Calendar cal = Calendar.getInstance();
		  if(SettingsManager.getInstance().getDateSrcType(this).equals("1"))
		  weatherpre.setCurrentInfo(new CurrentInfo(getResources().getString(R.string.default_center_location_city),getResources().getString(R.string.default_location_area) , "",	"01", getResources().getString(R.string.icon0),	"4","75%","M"));
		  else if(SettingsManager.getInstance().getDateSrcType(this).equals("2")){
		  weatherpre.setCurrentInfo(new CurrentInfo(getResources().getString(R.string.default_location_city),getResources().getString(R.string.default_location_area) , "12",	"01", getResources().getString(R.string.icon0),	"4","75%","M"));
		  }
		  else
		  weatherpre.setCurrentInfo(new CurrentInfo(getResources().getString(R.string.default_location_city),getResources().getString(R.string.default_location_area) , "12",	"01", getResources().getString(R.string.icon0),	"4","75%","M"));
		  
		  
		  weatherpre.getSimpleimfos().add(new SimpleWeatherInfo(datestring(cal), "19", "01","8", "0", "sunshine",getweek(cal), "NW", "20"));
		  cal.add(Calendar.DATE, 1);
		  weatherpre.getSimpleimfos().add(new SimpleWeatherInfo(datestring(cal), "18", "01","6", "0", "sunshine",getweek(cal), "NW", "20"));
		  cal.add(Calendar.DATE, 1);
		  weatherpre.getSimpleimfos().add(new SimpleWeatherInfo(datestring(cal), "9", "02","1", "0", "sunshine",getweek(cal), "NW", "20"));
		  cal.add(Calendar.DATE, 1);
		  weatherpre.getSimpleimfos().add(new SimpleWeatherInfo(datestring(cal), "10", "15","2", "0", "sunshine",getweek(cal), "NW", "20"));
		  cal.add(Calendar.DATE, 1);
		  weatherpre.getSimpleimfos().add(new SimpleWeatherInfo(datestring(cal), "11", "06","5", "0", "sunshine",getweek(cal), "NW", "20"));
		  cal.add(Calendar.DATE, 1);
		  weatherpre.getSimpleimfos().add(new SimpleWeatherInfo(datestring(cal), "12", "23","6", "0", "sunshine",getweek(cal), "NW", "20"));
		  cal.add(Calendar.DATE, 1);
		  weatherpre.getSimpleimfos().add(new SimpleWeatherInfo(datestring(cal), "13", "12","6", "0", "sunshine",getweek(cal), "NW", "20"));
		  ObjectOutputStream out = null;
		  try{
	         out = new ObjectOutputStream(openFileOutput(preWeatherFile,Context.MODE_PRIVATE));
	         out.writeObject(weatherpre);
	         out.flush();
	         out.close();
		  } 
		  catch (FileNotFoundException ex2){Log.i("xia", "22file Exception");} 
		  catch (IOException ex2) {Log.i("xia", "33file Exception");}
		  catch(Exception ee){Log.i("xia", "44file Exception");}
          }
    }
    @Override
    public void onCreate() {
    	mCalendar = Calendar.getInstance(TimeZone.getDefault());
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory("android.intent.category.HOME");
        final PackageManager packageManager = this.getPackageManager();
        List<ResolveInfo> apps = null;
        apps = packageManager.queryIntentActivities(mainIntent, 0);
        mLauncherActivities = new HashMap<String, String>();
        for (int i = 0; i < apps.size(); i++) {
            ResolveInfo info = apps.get(i);
            mLauncherActivities.put(info.activityInfo.name, info.activityInfo.name);
        } 
        mLauncherActivities.put("com.android.settings.AppWidgetPickActivity","xxx");
    	ISSETTING = false;
    	mWeekstr = getResources().getStringArray(R.array.weeknames);
    	mMonthsstr = getResources().getStringArray(R.array.monthnames);
		w = getResources().getStringArray(R.array.w);
    	//read the pre weatherinfo from file preWeatherFile;
    	//if the first time,the file is not exit.will throw Exception.
    	 Log.i(Tag, "############ SERVICE CREATE #############");   
    	 CITYCODE = SettingsManager.getInstance().getLocationZipCode_w(this);    	 
    	 mTuserver = this;
        Intent intent = new Intent(this, SettingActivity.class);
        pendingIntent = PendingIntent.getActivity(this,0 /* no requestCode */, intent, 0 /* no flags */); 
        cc = this.getResources().getString(R.string.Celsius);
        ff = this.getResources().getString(R.string.Fahrenheit);  	   
        cc_c = this.getResources().getString(R.string.Celsius_c);
        mWidgetComponent = new ComponentName(this, WeatherAppWidgetProvider.class);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_ALL_APPS);
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF); 
        filter.addAction("launcher_onresume"); 
        getApplicationContext().registerReceiver(mIntentReceiver, filter, null, mHandler);
        readinfofromfile();// read the file to get the oldinfo.
        mViews = new RemoteViews(getPackageName(), R.layout.weather_main);
        upDateWeather(weatherpre);
        new Thread() {
            public void run(){
                try {
                    Log.i("xia", " @@@@@@@@@@@@@ this.sleep(3000) @@@@@@@@@@@@@@@@ ");
                    Thread.currentThread().sleep(3000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                Log.i("xia","+++++++++WeatherReader.isgeting = "+WeatherReader.isgeting);
                if(WeatherReader.isgeting==false){
                    Log.i("xia","+++++++++++ oncreat updateweather()");
                updateWeather();
                }
            }
        }.start();
        super.onCreate();
    }



    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(Tag, "############ onStartCommand() #############");
        this.updateDateTime(false);
        if (weather != null) {
            upDateWeather(weather);
        } else {
            upDateWeather(weatherpre);
        }
        return super.onStartCommand(intent, flags, startId);
    }
    
    private void upDateWeather(Weather weather){  
    	 if(mSwitcher){
   		  mViews = null;
             mViews = new RemoteViews(getPackageName(), R.layout.weather_main);
             mSwitcher=false;
      	  }else{
      		  mViews = null;
             mViews = new RemoteViews(getPackageName(), R.layout.weather_mainother);
             mSwitcher=true;
      	  }
        Log.i(Tag, "----upDateWeather(weather)----");
    	 String city_en = SettingsManager.getInstance().getLocationCity_w(this);
    	 int citystr_id = AccuIconMapper.getCityName(city_en);
    	 
		 if(citystr_id==0){
    		 mViews.setTextViewText(R.id.city, city_en);
		 }
    	 else{
    		 city_en = this.getResources().getString(citystr_id);
             mViews.setTextViewText(R.id.city, city_en);
             
    	 }
         mViews.setTextViewText(R.id.d, datestring());
         if(weather.getCurrentInfo().getCu_tempreture().equals("")){
        	 mViews.setTextViewText(R.id.t, "");
         } 
    	 else {
    		 mViews.setTextViewText(R.id.t, weather.getCurrentInfo().getCu_tempreture()+cc); 
    	 }
    	 mViews.setTextViewText(R.id.h, weather.getSimpleimfos().get(0).getLowtempreture()+cc+"~"+weather.getSimpleimfos().get(0).getHightempreture()+cc);
    	 mViews.setTextViewText(R.id.l, weather.getSimpleimfos().get(0).getLowtempreture()+cc);
//modify by xiazhengguo for add htc weatherwidget (on)
//if(3646633 == Activity.RGT_FEATURE_VALUE){
    mViews.setOnClickPendingIntent(R.id.icon, pendingIntent);
//}
//modify by xiazhengguo for add htc weatherwidget (off)
         mIconid = Integer.parseInt(weather.getCurrentInfo().getWeathericon()); 
       //modify by xia 0614 on         
        if (SettingsManager.getInstance().getDateSrcType(this).equals("1")) {
				//weather = WeatherReader.getCenterWeather(this, CITYCODE);
        	 mViews.setTextViewText(R.id.cloud,weather.getCurrentInfo().getWeathertext());
			} else if (SettingsManager.getInstance().getDateSrcType(this)
					.equals("2")) {
				//weather = WeatherReader.getAcuuWeather(this, CITYCODE);
				 mViews.setTextViewText(R.id.cloud,getResources().getString(AccuIconMapper.getWeatherDescription(mIconid)));
			} else {
				//weather = WeatherReader.getGoogleWeather(this, CITYCODE);
				 mViews.setTextViewText(R.id.cloud,weather.getCurrentInfo().getWeathertext());
			}
        //modify by xia 0614 off    
//         mViews.setImageViewResource(R.id.t6, AccuIconMapper.getDrawableIdByIconId(100+mIconid));
//         mViews.setImageViewResource(R.id.t7, AccuIconMapper.getDrawableIdByIconId(200+mIconid));
         mViews.setImageViewResource(R.id.t8, AccuIconMapper.getDrawableIdByIconId(300+mIconid));
        
         Time time = new Time();
         time.setToNow();
         if(time.hour>=6&&time.hour<19) { 
        	 Log.i("xia ", "     day    "+mIconid);
         mViews.setImageViewResource(R.id.bigback, AccuIconMapper.getDrawableIdByIconId(100+mIconid));
         mViews.setImageViewResource(R.id.forback, AccuIconMapper.getDrawableIdByIconId(200+mIconid));
         mViews.setImageViewResource(R.id.threeback, AccuIconMapper.getDrawableIdByIconId(400+mIconid));
          }
	    else    	
	    {
	    	Log.i("xia ", "     night     "+mIconid);
	         mViews.setImageViewResource(R.id.bigback, AccuIconMapper.getDrawableIdByIconId(150+mIconid));
	         mViews.setImageViewResource(R.id.forback, AccuIconMapper.getDrawableIdByIconId(250+mIconid));
	         mViews.setImageViewResource(R.id.threeback, AccuIconMapper.getDrawableIdByIconId(450+mIconid));
	    }
         this.updateDateTime(false);
         updateRomoteView();
    }
    
    @Override
    public void onDestroy(){     
        Log.i(Tag, "############ SERVICE STOP #############");
        ISSETTING = false;
        weather=null;
        weatherpre=null;
        getApplicationContext().unregisterReceiver(mIntentReceiver);
        super.onDestroy();
    }

    private String datestring() {
        Calendar ca = Calendar.getInstance();
        int month = ca.get(Calendar.MONTH);
        int d     = ca.get(Calendar.DAY_OF_MONTH);
        int week  = ca.get(Calendar.DAY_OF_WEEK);
        if (mWeekstr[0].endsWith("en")) {
            return this.mMonthsstr[month].substring(0, 3) + " " + d + ","
                    + this.mWeekstr[week].substring(0, 3);

        } else if (mWeekstr[0].endsWith("zh")) {
            return this.mWeekstr[week].trim()+ " " +  (month + 1) + getResources().getString(R.string.month) + d
                    + getResources().getString(R.string.day) ;
        } else {
            String aa = Settings.System
                    .getString(getContentResolver(), Settings.System.DATE_FORMAT);
            Log.i("xia==", aa + "");
            if (aa == null || aa.equals(""))
                aa = "dd-MM";
            String temstr = this.getResources().getString(R.string.datesplite);
            
            Date date = (Date) Calendar.getInstance().getTime();
            SimpleDateFormat sd = new SimpleDateFormat(aa);
            aa = sd.format(date);
            return getweek().substring(0, 3) + "," + aa;
        }
    }
    
  
    private String datestring(Calendar today){
    	String aa = "yyyy-MM-dd";
        aa = aa.replace('-', '/');
       Date date = (Date)today.getTime();
   	    SimpleDateFormat  sd = new SimpleDateFormat(aa);
   	    aa = sd.format(date);
       return aa; 
    }
    private String getweek(){
    	Calendar today = Calendar.getInstance();
    	int  a = today.get(Calendar.DAY_OF_WEEK);
    	return mWeekstr[a]+"    ";
    }
    private String getweek(Calendar today){ 	
    	int  a = today.get(Calendar.DAY_OF_WEEK);
    	switch(a)
    	{
    	case 1 :return "Sunday";
		case 2 :return "Monday";
    	case 3 :return "Tuesday";
        case 4 :return "Wednesday";
		case 5 :return "Thursday";
    	case 6 :return "Friday";
    	case 7 :return "Saturday";
    	}
       return mWeekstr[a];
    }
    
    
	private void getweather() {
		CITYCODE = SettingsManager.getInstance().getLocationZipCode_w(this);
		if (checkNetworkInfo()) {
			if (SettingsManager.getInstance().getDateSrcType(this).equals("1")) {
				weather = WeatherReader.getCenterWeather(this, CITYCODE);
			} else if (SettingsManager.getInstance().getDateSrcType(this)
					.equals("2")) {
				weather = WeatherReader.getAcuuWeather(this, CITYCODE);
			} else {
				weather = WeatherReader.getGoogleWeather(this, CITYCODE);
			}
//			if(weather == null&&mViews!=null){
//				 mViews.setTextViewText(R.id.cloud, this.getResources().getString(R.string.failedget));
//		       	 AppWidgetManager managera = AppWidgetManager.getInstance(TimeUpdateService.this);
//		            managera.updateAppWidget(mWidgetComponent, mViews);
//		    }			
		}else{
//			if(mViews!=null){
//			 mViews.setTextViewText(R.id.cloud, this.getResources().getString(R.string.nonet));
//	       	 AppWidgetManager managera = AppWidgetManager.getInstance(TimeUpdateService.this);
//	            managera.updateAppWidget(mWidgetComponent, mViews);
//			}
		}
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
    }
    
    private void updateWeather(){

    	if(!this.ISSETTING){
    		//不是从设置界面返回,就正常去取网络信息
    	    getweather();
          if(weather == null) {
        	  //没有取到数据，就只更新时间。
               UPDATE_WEATHER_PERIOD_IN_MINS = FAIL_UPDATE_WEATHER_PERIOD_IN_MINS;
               return;
          }else{
        	  //取到了，就保存一份到本地
        	    if(weather.getCurrentInfo().getWeathericon()!=null)
        	    {
	        		 try{
		      			ObjectOutputStream out = new ObjectOutputStream(this.openFileOutput(TimeUpdateService.preWeatherFile,Context.MODE_PRIVATE));
		      			out.writeObject(weather);
		      			out.flush();
		      			out.close();
		      		} 
		      		catch (FileNotFoundException ex2){} 
		      		catch (IOException ex2) {}
		      		catch(Exception ee){}	
        	     }
          }
          Log.i("xia"," @@@@@@@@@@@@ weather != null ");
          updateWeatherStyle2();         
    	}else{
    		this.ISSETTING = false;
    		if(this.weather==null)
    			return;
    		//从设置界面返回来，同时选择了新的城市。
//    		if(!this.CITYCODE.equals(SettingsManager.getInstance().getLocationZipCode_w(SettingsManager.STORAGEID, this)))
//    		{
//    		    getweather();
//    			if(weather == null) {
//    	           UPDATE_WEATHER_PERIOD_IN_MINS = FAIL_UPDATE_WEATHER_PERIOD_IN_MINS;
//    	           return;
//    	        }
//    			else
//    			{
//    				try{
//    					ObjectOutputStream out = new ObjectOutputStream(this.openFileOutput(TimeUpdateService.preWeatherFile,Context.MODE_PRIVATE));
//    					out.writeObject(weather);
//    					out.flush();
//    					out.close();
//    				} 
//    				catch (FileNotFoundException ex2){} 
//    				catch (IOException ex2) {}
//    				catch(Exception ee){}
//    			}    	          	       
//    		}
    		
    		 updateWeatherStyle1();
    	}
        UPDATE_WEATHER_PERIOD_IN_MINS = WEATHER_UPDATE_DELAY;
    }
    
    
    private void   updateWeatherStyle2(){ 
      //if(this.isLauncherOnTop()&&mScreen_on)
    	if(mScreen_on)
              updateWeatherStyle1(); 
    }
    
   
    private void   updateWeatherStyle1(){
	 if(weather==null)
		 return;
	  CurrentInfo currentInfo = weather.getCurrentInfo();
	  if(currentInfo.getWeathericon()==null){
         return;
   	  }
	  upDateWeather(weather);
 }
    
    private String ctof(String c){
    	Double f = (Integer.parseInt(c)*9/5.0+32+0.5) ;
    	return String.valueOf(f.intValue());
    }
    private void updateDateTime(boolean flush){
    	
            if(mViews==null){
    	          mViews = new RemoteViews(getPackageName(), R.layout.weather_main);
            }
            
            
/*            
            
            Time time = new Time();
            time.setToNow();
//            boolean is24Hour = true;
            boolean is24Hour = DateFormat.is24HourFormat(this);
//            try {
//                if (Settings.System.getString(this.getContentResolver(), Settings.System.TIME_12_24)
//                        .equals("12"))
//                    is24Hour = false;
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            String currentTime = "";
            if (is24Hour) {
                currentTime = (time.hour < 10 ? "0" + time.hour : time.hour) + ":"
                        + (time.minute < 10 ? "0" + time.minute : time.minute);
                mViews.setImageViewResource(R.id.t5, 0);
            } else {
            	if(time.hour == 12){            		
                    currentTime = 12 + ":"
                    + (time.minute < 10 ? "0" + time.minute : time.minute);
                    mViews.setImageViewResource(R.id.t5, R.drawable.clock_day_pm); 
                } else if(time.hour == 0){
                	  currentTime = 12 + ":"
                              + (time.minute < 10 ? "0" + time.minute : time.minute);
                              mViews.setImageViewResource(R.id.t5, R.drawable.clock_day_am);                 	
                	
                } else if (time.hour < 12) {
                    currentTime = (time.hour < 10 ? "0" + time.hour : time.hour) + ":"
                            + (time.minute < 10 ? "0" + time.minute : time.minute);
                    mViews.setImageViewResource(R.id.t5, R.drawable.clock_day_am);
                } else  {
                    int our = (time.hour - 12);
                    currentTime = (our<10?"0" + our : our) + ":"
                            + (time.minute < 10 ? "0" + time.minute : time.minute);
                    mViews.setImageViewResource(R.id.t5, R.drawable.clock_day_pm);
                }
            }
            mViews.setImageViewResource(R.id.t1,
                    AccuIconMapper.getDrawableIdByIconId(currentTime.charAt(0)));
            mViews.setImageViewResource(R.id.t2,
                    AccuIconMapper.getDrawableIdByIconId(currentTime.charAt(1)));
            mViews.setImageViewResource(R.id.t3,
                    AccuIconMapper.getDrawableIdByIconId(currentTime.charAt(3)));
            mViews.setImageViewResource(R.id.t4,
                    AccuIconMapper.getDrawableIdByIconId(currentTime.charAt(4)));
*/     	
   	     boolean b24 = DateFormat.is24HourFormat(this);
   	     SimpleDateFormat sd;
   	     mCalendar.setTimeInMillis(System.currentTimeMillis());
   	     Date date = mCalendar.getTime();
   	     String aa;
    	if(b24){
    		sd = new SimpleDateFormat("HH:mm a");
    	    aa = sd.format(date);
    	   mViews.setImageViewResource(R.id.t1, AccuIconMapper.getDrawableIdByIconId(aa.charAt(0)));
           mViews.setImageViewResource(R.id.t2, AccuIconMapper.getDrawableIdByIconId(aa.charAt(1)));
           mViews.setImageViewResource(R.id.t3, AccuIconMapper.getDrawableIdByIconId(aa.charAt(3)));
           mViews.setImageViewResource(R.id.t4, AccuIconMapper.getDrawableIdByIconId(aa.charAt(4))); 
           mViews.setTextViewText(R.id.t5,"");
    	}
    	else{
    	    sd = new SimpleDateFormat("hh:mm a");
    	    aa = sd.format(date);
    	    mViews.setImageViewResource(R.id.t1, AccuIconMapper.getDrawableIdByIconId(aa.charAt(0)));
            mViews.setImageViewResource(R.id.t2, AccuIconMapper.getDrawableIdByIconId(aa.charAt(1)));
            mViews.setImageViewResource(R.id.t3, AccuIconMapper.getDrawableIdByIconId(aa.charAt(3)));
            mViews.setImageViewResource(R.id.t4, AccuIconMapper.getDrawableIdByIconId(aa.charAt(4))); 
    		String tmp =aa.substring(6);
    		mViews.setTextViewText(R.id.t5, tmp.equals(getResources().getString(R.string.am))||tmp.equals("AM")?"AM":"PM");
       }
    	Log.i(Tag, "----- updateDateTime() ----"+aa);
        mViews.setTextViewText(R.id.d, datestring());
       // mViews.setTextViewText(R.id.time, aa.substring(0, 5));
        mViews.setTextViewText(R.id.l, datestring(mCalendar)+" "+aa.substring(0, 5));
        Time time = new Time();
        time.setToNow();
        Log.i("xia", " mIconid = "+mIconid);
        Log.i("xia", "time.hour = "+time.hour);
        Log.i("xia", "time.minute = "+time.minute);
        if((time.hour==6&&time.minute==0)||(time.hour==9&&time.minute==0)) {       
        	if(time.hour>=6&&time.hour<19) {
        		Log.i("xia", "----------day");
                mViews.setImageViewResource(R.id.bigback, AccuIconMapper.getDrawableIdByIconId(100+mIconid));
                mViews.setImageViewResource(R.id.forback, AccuIconMapper.getDrawableIdByIconId(200+mIconid));
                mViews.setImageViewResource(R.id.threeback, AccuIconMapper.getDrawableIdByIconId(400+mIconid));
                 }
       	    else    	
       	    {
       	    	 Log.i("xia", "----------night");
       	         mViews.setImageViewResource(R.id.bigback, AccuIconMapper.getDrawableIdByIconId(150+mIconid));
       	         mViews.setImageViewResource(R.id.forback, AccuIconMapper.getDrawableIdByIconId(250+mIconid));
       	         mViews.setImageViewResource(R.id.threeback, AccuIconMapper.getDrawableIdByIconId(450+mIconid));
       	    }
        }
        
        if(flush){
         Log.i(Tag, "----- updateDateTime() ----");
         if(this.isRemoteView){
         AppWidgetManager managera = AppWidgetManager.getInstance(TimeUpdateService.this);
         managera.updateAppWidget(mWidgetComponent, mViews);
         }
		 }
    }

    private boolean isLauncherOnTop(){
        ActivityManager   mactivitymanager =(ActivityManager)getSystemService(ACTIVITY_SERVICE);
        List<RunningTaskInfo> list = mactivitymanager.getRunningTasks(1);
        ComponentName cn;
        if(list==null||list.size()==0){
            return false;
        }            
        else{
			cn = list.get(0).topActivity;
        }
        boolean islauncher = mLauncherActivities.containsKey(cn.getClassName());
        Log.i("xia","========top activity:-"+cn.getClassName()+"==islauncher:-"+islauncher);
        return islauncher;
    }
    long temp = 0;
   private static Calendar tempc = null;
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent arg1) {
            String action = arg1.getAction();
			 Log.i("xia", "----->>>>>>>>>"+action);
            if (action.equals("android.intent.action.CONFIGURATION_CHANGED")) {
                // 当语言变化后，替换星期数组
                Log.i("xia", "----- android.intent.action.CONFIGURATION_CHANGED() ----");
                mWeekstr = getResources().getStringArray(R.array.weeknames);
                mMonthsstr = getResources().getStringArray(R.array.monthnames);
                //updateDateTime(true);
                //add by xia 0614 on  
                if(weather!=null)
                upDateWeather(weather);
              //add by xia 0614 off  
            }
            else if("launcher_onresume".equals(action)){
            	
            	tempc = Calendar.getInstance();
				Log.i("xia", " ----- android.intent.action.launcher_onresume ----");
				 Log.i("xia","last hour minute = "+hour+","+minute);
				 Log.i("xia","current :hour minute  "+tempc.HOUR_OF_DAY+","+tempc.MINUTE);
				if ((tempc.get(Calendar.MINUTE) != minute) || (tempc.get(Calendar.HOUR_OF_DAY) != hour)) {
					updateDateTime(true);
					if (weather != null)
						upDateWeather(weather);
					hour =tempc.get(Calendar.HOUR_OF_DAY);
					minute = tempc.get(Calendar.MINUTE);
				}
            }else if(Intent.ACTION_SCREEN_OFF.equals(action)){
                
                Log.i(Tag, "----- android.intent.action.ACTION_SCREEN_OFF() ----");
                mScreen_on = false;
            }
            else if(Intent.ACTION_SCREEN_ON.equals(action)){
                Log.i(Tag, "----- android.intent.action.ACTION_SCREEN_ON() ----");
//                updateDateTime(true);
                mScreen_on = true;
                updateDateTime(true);
            }
             else if("android.intent.action.TIME_SET".equals(action)){            	 
            	updateDateTime(true);
//            	if (weather != null)
//					upDateWeather(weather);
            }else if(Intent.ACTION_TIMEZONE_CHANGED.equals(action)){ 
            	 String tz = arg1.getStringExtra("time-zone");
                 mCalendar = Calendar.getInstance(TimeZone.getTimeZone(tz));
            	updateDateTime(true);
            }
             
             else{    
            	tempc = Calendar.getInstance();
            	Calendar tempc = Calendar.getInstance();
            	temp = tempc.getTimeInMillis();
                 if(isLauncherOnTop()&&mScreen_on){
                      updateDateTime(true);
                      hour = tempc.get(Calendar.HOUR_OF_DAY);
                      minute = tempc.get(Calendar.MINUTE);
                 }
                long sCalenerlast = temp - WeatherReader.sCalenerlast;
             //   Log.i("xia","-s-,"+sCalenerlast);
                long past_munites = sCalenerlast/1000L/60L;
                Log.i("xia","---past_munites:"+past_munites);
                if (past_munites>= UPDATE_WEATHER_PERIOD_IN_MINS) {
                    new Thread() {
                        public void run() {
                            Log.i("xia"," from oncreat receiver() updateWeather()");
                            if(WeatherReader.isgeting==false) {                
                                updateWeather();  
                            }
                        }
                    }.start();
                }
            }
        }
    };
    
    public static void restartUpdateService(Context context) {
        Log.i(Tag, "----- restartUpdateService() ----");
        Intent mIntent = new Intent();
        mIntent.setClass(context, TimeUpdateService.class);
        PendingIntent mPendingIntent = PendingIntent.getService(context, 0, mIntent, 0);
        mPendingIntent.cancel();
        context.startService(new Intent(context, TimeUpdateService.class));
    } 
}
