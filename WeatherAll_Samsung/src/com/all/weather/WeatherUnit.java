package com.all.weather;

import com.all.weather.R;

public enum WeatherUnit implements ITaskEnum {
	ENGLISH(R.string.unit_english_preference), METRIC(R.string.unit_metric_preference);
	
	private int resId;
	
	private WeatherUnit(int resId) {
		this.resId = resId;
	}
	
	public int getResId() {
		return resId;
	}
	
	public static WeatherUnit getDefault() {
		return METRIC;
	} 
	
}
