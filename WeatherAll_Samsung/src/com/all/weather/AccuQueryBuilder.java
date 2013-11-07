package com.all.weather;

import java.net.MalformedURLException;
import java.net.URL;
//import com.huawei.hid.weatherwidget.GPSLocationManager.GPSCoordinates;
import android.content.Context;
import android.util.Log;

public class AccuQueryBuilder {
	//example--http://alamo.accu-weather.com/widget/alamo/weather-data.asp?location=ASI|CN|CH015|NANJING&metric=1
	private static final String SERVICE_URL = "http://alamo.accu-weather.com/widget/alamo/weather-data.asp";
	private static final String LOCATION = "location";
	private static final String METRIC = "metric";
//	private static final String WLAT = "wlat";
//	private static final String WLON = "wlon";
	protected static final String EQUAL = "=";
	protected static final String AMP = "&";
	protected static final String QUESTION = "?";
	 
//	private String LocationZipCode;
	
	public URL getURL(Context context,String LocationZipCode) {
		URL url = null;
		try {
			String queryString = getQueryString(context,LocationZipCode);
			Log.d("BaseQueryBuilder#getURL url=1", queryString);
			//replace the html code.
			queryString = HtmlUrlCoder.codeUrl(queryString);
			Log.d("BaseQueryBuilder#getURL url=2", queryString);
			url = new URL(queryString);
		} catch (MalformedURLException e) {
			Log.e("BaseQueryBuilder#getUrl", e.getMessage());
		}
		return url;
	}
	protected String getServiceURL() {
		return SERVICE_URL;
	}
	protected String getQueryString(Context context,String LocationZipCode) {
		return new StringBuilder().append(SERVICE_URL).
		append(QUESTION).append(buidLocation(context,LocationZipCode)).
		append(AMP).append(buidMetric(context)).
		toString();
	}
	//SettingsManager.getInstance().getLocationZipCode(storageId, context)
	private String buidLocation(Context context,String LocationZipCode) {
		StringBuilder location = new StringBuilder();
//		LocationType locationType = SettingsManager.getInstance().getLocationType(storageId, context);
//		if(locationType == LocationType.AUTO) {
//			GPSCoordinates coordinates = GPSLocationManager.getInstatnce(context).getGPSCoordinates();
//			location.append(WLAT).append(EQUAL).append(coordinates == null ? 0 : coordinates.getLatitude()).
//			append(AMP).append(WLON).append(EQUAL).append(coordinates == null ? 0 : coordinates.getLangitude());
//		} else if(locationType == LocationType.ZIP_CITY_CODE) {
			location.append(LOCATION).append(EQUAL).append(LocationZipCode);
//		}
		return location.toString();
	}
	
	private String buidMetric(Context context) {
		WeatherUnit unit = SettingsManager.getInstance().getWeatherUnit(context);
		
		return new StringBuilder().append(METRIC).append(EQUAL).append((unit == WeatherUnit.ENGLISH) ? 0 : 1).toString();
	}
}	
