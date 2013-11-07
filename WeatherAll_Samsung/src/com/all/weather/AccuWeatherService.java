package com.all.weather;



import android.content.Context;

public class AccuWeatherService {
	public AccuWeatherXMLHandler createHandler(Context context) {
		return new AccuWeatherXMLHandler(context);
	}
	
	public AccuQueryBuilder createQueryBuilder() {
		return new AccuQueryBuilder();
	}
}
