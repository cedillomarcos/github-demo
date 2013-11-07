package com.all.weather;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.io.InputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import android.content.Context;
import android.util.Log;

public class WeatherReader {
    public static long sCalenerlast = 0l;
    public static boolean isgeting = false;
	public static synchronized Weather getAcuuWeather(Context context,String LocationZipCode) {
	     isgeting = true;
		 Log.i("xia","++++++++++++++++++++ WeatherReader.getWeather()");
		 Weather weather = null;
		 SAXParser sp=null;
		 XMLReader xr=null;
		 AccuWeatherXMLHandler xmlHandler=null;
		 SAXParserFactory spf = SAXParserFactory.newInstance();
		 try{
			 sp = spf.newSAXParser();
			 xr = sp.getXMLReader();
			 //'WeatherServiceFactory' only is used to create WeatherServiceIF
			 //'WeatherServiceIF' a interface to  create BaseXMLHandler() and BaseQueryBuilder();
			 AccuWeatherService weatherService = WeatherServiceFactory.getInstance().createWeatherService();
			 //'BaseXMLHandler()' is SAXHandler ;
			 xmlHandler = weatherService.createHandler(context);
			 xr.setContentHandler(xmlHandler);
			 //'BaseQueryBuilder()' is to geturlstring.
			 URL url = weatherService.createQueryBuilder().getURL(context,LocationZipCode);
			 URLConnection connection=null;  
			 InputStream stream=null;
			 connection = url.openConnection();
			 connection.setConnectTimeout(10000);
			 connection.connect();
			 stream = connection.getInputStream();
			 InputSource is = new InputSource(stream);
			 if(is != null) {
				   xr.parse(is);
			 }
			 else{
				 weather = null;
			 }
		}catch(UnknownHostException e){
		    sCalenerlast = Calendar.getInstance().getTimeInMillis();
		    isgeting = false;
				    return null;
		}catch (IOException e) {
		    sCalenerlast = Calendar.getInstance().getTimeInMillis();
		    isgeting = false;
				 return null;
		}catch (ParserConfigurationException e) {
		    sCalenerlast = Calendar.getInstance().getTimeInMillis();
		    isgeting = false;
				 return null;
		}catch (SAXException e) {
		    sCalenerlast = Calendar.getInstance().getTimeInMillis();
		    isgeting = false;
				 return null;
		}catch(Exception e){
		    sCalenerlast = Calendar.getInstance().getTimeInMillis();
		    isgeting = false;
				 return null;
		}
		sCalenerlast = Calendar.getInstance().getTimeInMillis();
		Log.i("xia","-e-sCalenerlast,"+sCalenerlast);
		weather = xmlHandler.getWeather();
		isgeting = false;
		return weather;
	}
	public static synchronized Weather getCenterWeather(Context context,String LocationZipCode) {
	     isgeting = true;
	     Weather weather = null;
	     weather = WeatherCenterReader.getWeather(LocationZipCode);
		sCalenerlast = Calendar.getInstance().getTimeInMillis();
		isgeting = false;
		return weather;
	}
	
	public static synchronized Weather getGoogleWeather(Context context,String LocationZipCode) {
	     isgeting = true;
	     Weather weather = null;
	     weather = WeatherCenterReader.getWeather(LocationZipCode);
		sCalenerlast = Calendar.getInstance().getTimeInMillis();
		isgeting = false;
		return weather;
	}
}
