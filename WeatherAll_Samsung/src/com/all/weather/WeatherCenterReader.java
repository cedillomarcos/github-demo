package com.all.weather;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.text.format.Time;
import android.util.Log;

public class WeatherCenterReader {

	public static Weather getWeather(String code) {
		Weather weather = new Weather();
		String result = null;
		try {
			URL url = new URL("http://m.weather.com.cn/data/"+code+".html");
			result = getString(url);
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
        Log.i("xia","result:"+result);
		JSONObject jsonObject;
		try {
			if(result==null)
				return null;
			jsonObject = new JSONObject(result).getJSONObject("weatherinfo");
//			Log.i("xia","1   "+jsonObject.getString("weather1"));
//			Log.i("xia","2   "+jsonObject.getString("weather2"));
//			Log.i("xia","3   "+jsonObject.getString("weather3"));
//			Log.i("xia","4   "+jsonObject.getString("weather4"));
//			Log.i("xia","5   "+jsonObject.getString("weather5"));
			weather.getCurrentInfo().setCu_tempreture("");
			weather.getCurrentInfo().setWeathericon(getweather_icon_one(jsonObject.getString("weather1")));
			weather.getCurrentInfo().setWeathertext(
					jsonObject.getString("weather1"));

			SimpleWeatherInfo sim;
			int p;
			String temp;
			// 1
			Calendar cal = Calendar.getInstance();
			sim = new SimpleWeatherInfo();
			sim.setData(datestring(cal));
			sim.setWeek(getweek(cal));
			sim.setIcon(getweather_icon_one(jsonObject.getString("weather1")));
			sim.setTextshort(jsonObject.getString("weather1"));Log.i("xia",""+jsonObject.getString("weather1"));
//			sim.setImageids(jsonObject.getString("img1") + ","
//					+ jsonObject.getString("img2"));
//			Log.i("xia", " ==1= "+jsonObject.getString("img1") + ","+ jsonObject.getString("img2"));
//			sim.setImagetitles(jsonObject.getString("img_title1") + ","
//					+ jsonObject.getString("img_title2"));
			int aa[] = gethltempreture(jsonObject.getString("temp1"));
			sim.setLowtempreture(""+aa[0]);
			sim.setHightempreture(""+aa[1]);
			weather.getSimpleimfos().add(sim);
			// 2
			cal.add(Calendar.DATE, 1);
			sim = new SimpleWeatherInfo();
			sim.setData(datestring(cal));
			sim.setWeek(getweek(cal));
			sim.setTextshort(jsonObject.getString("weather2"));
			sim.setIcon(getweather_icon(jsonObject.getString("weather2")));
//			sim.setImageids(jsonObject.getString("img3") + ","
//					+ jsonObject.getString("img4"));
//			Log.i("xia", " ==2= "+jsonObject.getString("img1") + ","+ jsonObject.getString("img2"));
//			sim.setImagetitles(jsonObject.getString("img_title3") + ","
//					+ jsonObject.getString("img_title4"));
			aa = gethltempreture(jsonObject.getString("temp2"));
			sim.setLowtempreture(""+aa[0]);
			sim.setHightempreture(""+aa[1]);
			weather.getSimpleimfos().add(sim);
			// 3
			cal.add(Calendar.DATE, 1);
			sim = new SimpleWeatherInfo();
			sim.setData(datestring(cal));
			sim.setWeek(getweek(cal));
			sim.setIcon(getweather_icon(jsonObject.getString("weather3")));
			sim.setTextshort(jsonObject.getString("weather3"));
//			sim.setImageids(jsonObject.getString("img5") + ","
//					+ jsonObject.getString("img6"));
//			Log.i("xia", " ==3= "+jsonObject.getString("img1") + ","+ jsonObject.getString("img2"));
//			sim.setImagetitles(jsonObject.getString("img_title5") + ","
//					+ jsonObject.getString("img_title6"));
			aa= gethltempreture(jsonObject.getString("temp3"));
			sim.setLowtempreture(""+aa[0]);
			sim.setHightempreture(""+aa[1]);
			weather.getSimpleimfos().add(sim);
			// 4
			cal.add(Calendar.DATE, 1);
			sim = new SimpleWeatherInfo();
			sim.setData(datestring(cal));
			sim.setWeek(getweek(cal));
			sim.setIcon(getweather_icon(jsonObject.getString("weather4")));
			sim.setTextshort(jsonObject.getString("weather4"));
//			sim.setImageids(jsonObject.getString("img7") + ","
//					+ jsonObject.getString("img8"));
//			Log.i("xia", " ==4= "+jsonObject.getString("img1") + ","+ jsonObject.getString("img2"));
//			sim.setImagetitles(jsonObject.getString("img_title7") + ","
//					+ jsonObject.getString("img_title8"));
			aa = gethltempreture(jsonObject.getString("temp4"));
			sim.setLowtempreture(""+aa[0]);
			sim.setHightempreture(""+aa[1]);
			weather.getSimpleimfos().add(sim);
			// 5
			cal.add(Calendar.DATE, 1);
			sim = new SimpleWeatherInfo();
			sim.setData(datestring(cal));
			sim.setWeek(getweek(cal));
			sim.setIcon(getweather_icon(jsonObject.getString("weather5")));
			sim.setTextshort(jsonObject.getString("weather5"));
//			sim.setImageids(jsonObject.getString("img9") + ","
//					+ jsonObject.getString("img10"));
//			Log.i("xia", " ==5= "+jsonObject.getString("img1") + ","+ jsonObject.getString("img2"));
//			sim.setImagetitles(jsonObject.getString("img_title9") + ","
//					+ jsonObject.getString("img_title10"));
			aa = gethltempreture(jsonObject.getString("temp5"));
			sim.setLowtempreture(""+aa[0]);
			sim.setHightempreture(""+aa[1]);
			weather.getSimpleimfos().add(sim);

		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}

		return weather;
	}
	private static int[] gethltempreture(String highandlow){
		int p = highandlow.indexOf('~');
		int [] aa = new int[2];
		int low  = Integer.parseInt(highandlow.substring(0, p - 1));
		int high = Integer.parseInt(highandlow.substring(p + 1, highandlow.length() - 1));
		if(low > high){
			aa[0] = high;aa[1] = low;
		}else{
			aa[1] = high;aa[0] = low;
		}
		return aa;
	}

private static String getweather_icon(String weathertext){
    	if(weathertext.contains(TimeUpdateService.w[0])){
    		return "32";
    	}else if(weathertext.contains(TimeUpdateService.w[1])){
    		return "31";
    	}else if(weathertext.contains(TimeUpdateService.w[2])){
    		return "25";
    	}else if(weathertext.contains(TimeUpdateService.w[3])){
    		return "11";
    	}else if(weathertext.contains(TimeUpdateService.w[4])){
    		return "16";
	}else if(weathertext.contains(TimeUpdateService.w[5])){
    		return "29";
    	}else if(weathertext.contains(TimeUpdateService.w[6])){
    		return "19";
    	}else if(weathertext.contains(TimeUpdateService.w[7])){
    		return "18";
    	}else if(weathertext.contains(TimeUpdateService.w[8])){
    		return "8";
    	}else if(weathertext.contains(TimeUpdateService.w[9])){
    		return "3";	
    	}else if(weathertext.contains(TimeUpdateService.w[10])){
    		return "1";
    	}else
    	{
    		return "23";
    	}
    }
     private static String getweather_icon_one(String weathertext){    	
    	 Time time = new Time();
         time.setToNow();
    	if(time.hour>=19||time.hour<6){
    		Log.i("xia", "=="+weathertext);Log.i("xia", "=="+TimeUpdateService.w[0]+"==");
    		if(weathertext.contains(TimeUpdateService.w[0])){
	    		return "32";
	    	}else if(weathertext.contains(TimeUpdateService.w[1])){
	    		return "31";
	    	}else if(weathertext.contains(TimeUpdateService.w[2])){
	    		return "25";
			}else if(weathertext.contains(TimeUpdateService.w[3])){
	    		return "11";
	    	}else if(weathertext.contains(TimeUpdateService.w[4])){
	    		return "42";	
	    	}else if(weathertext.contains(TimeUpdateService.w[5])){
	    		return "29";
	    	}else if(weathertext.contains(TimeUpdateService.w[6])){
	    		return "43";
	    	}else if(weathertext.contains(TimeUpdateService.w[7])){
	    		return "40";	
	    	}else if(weathertext.contains(TimeUpdateService.w[8])){
	    		return "8";
	    	}else if(weathertext.contains(TimeUpdateService.w[9])){
	    		return "35";	
	    	}else if(weathertext.contains(TimeUpdateService.w[10])){
	    		return "33";
	    	}else
	    	{
	    		return "23";
	    	}
    	}
    	else{
    		return getweather_icon(weathertext);
    	}
    }
/*
    private static String getweather_icon(String weathertext){
    	if(weathertext.contains("风")){
    		return "32";
    	}else if(weathertext.contains("霜")){
    		return "31";
    	}else if(weathertext.contains("雹")){
    		return "25";
    	}else if(weathertext.contains("雾")){
    		return "11";
    	}else if(weathertext.contains("雨夹雪")){
    		return "29";
    	}else if(weathertext.contains("雷")){
    		return "16";	
    	}else if(weathertext.contains("雪")){
    		return "19";
    	}else if(weathertext.contains("雨")){
    		return "18";	
    	}else if(weathertext.contains("阴")){
    		return "8";
    	}else if(weathertext.contains("云")){
    		return "3";	
    	}else if(weathertext.contains("晴")){
    		return "1";
    	}else
    	{
    		return "23";
    	}
    }
    private static String getweather_icon_one(String weathertext){
    	 Time time = new Time();
         time.setToNow();
    	if(time.hour>=19||time.hour<6){
    		if(weathertext.contains("风")){
	    		return "32";
	    	}else if(weathertext.contains("霜")){
	    		return "31";
	    	}else if(weathertext.contains("雹")){
	    		return "25";
	    	}else if(weathertext.contains("雾")){
	    		return "11";
	    	}else if(weathertext.contains("雷")){
	    		return "42";	
	    	}else if(weathertext.contains("雨夹雪")){
	    		return "29";
	    	}else if(weathertext.contains("雪")){
	    		return "43";
	    	}else if(weathertext.contains("雨")){
	    		return "40";	
	    	}else if(weathertext.contains("阴")){
	    		return "8";
	    	}else if(weathertext.contains("云")){
	    		return "35";	
	    	}else if(weathertext.contains("晴")){
	    		return "33";
	    	}else
	    	{
	    		return "23";
	    	}
    	}
    	else{
	    	if(weathertext.contains("风")){
	    		return "32";
	    	}else if(weathertext.contains("霜")){
	    		return "31";
	    	}else if(weathertext.contains("雹")){
	    		return "25";
	    	}else if(weathertext.contains("雾")){
	    		return "11";
	    	}else if(weathertext.contains("雨夹雪")){
	    		return "29";
	    	}else if(weathertext.contains("雷")){
	    		return "16";	
	    	}else if(weathertext.contains("雪")){
	    		return "19";
	    	}else if(weathertext.contains("雨")){
	    		return "18";	
	    	}else if(weathertext.contains("阴")){
	    		return "8";
	    	}else if(weathertext.contains("云")){
	    		return "3";	
	    	}else if(weathertext.contains("晴")){
	    		return "1";
	    	}else
	    	{
	    		return "23";
	    	}
    	}
    }
*/
	private static String getweek(Calendar today) {
		int a = today.get(Calendar.DAY_OF_WEEK);
		switch (a) {
		case 1:
			return "Sunday";
		case 2:
			return "Monday";
		case 3:
			return "Tuesday";
		case 4:
			return "Wednesday";
		case 5:
			return "Thursday";
		case 6:
			return "Friday";
		case 7:
			return "Saturday";
		}
		return "Wednesday";
	}

	private static String datestring(Calendar today) {
		String aa = "MM-dd-yyyy";
		aa = aa.replace('-', '/');
		Date date = (Date) today.getTime();
		SimpleDateFormat sd = new SimpleDateFormat(aa);
		aa = sd.format(date);
		return aa;
	}

	public static String getString(URL url) {
		URLConnection connection = null;
		byte[] b = new byte[256];
		InputStream in = null;
		String result = null;
		ByteArrayOutputStream bo = new ByteArrayOutputStream();

		try {
			connection = url.openConnection();
			connection.setConnectTimeout(10000);
			connection.connect();
			in = connection.getInputStream();
			int i;
			while ((i = in.read(b)) != -1) {
				bo.write(b, 0, i);
			}
			result = bo.toString();
			bo.reset();
		}catch(UnknownHostException e){
				    return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return result;
	}
}
