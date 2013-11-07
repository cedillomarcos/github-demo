package com.all.weather;

import com.all.weather.R;

public enum LocationType implements ITaskEnum{
	
	AUTO(R.string.location_auto_preference), ZIP_CITY_CODE(R.string.location_zip_city_preference);
	                   //Auto                                          US ZipCode, City Name
	private int resId;
	
	private LocationType(int resId) {
		this.resId = resId;
	}
	
	public int getResId() {
		return resId;
	}
	
	public static LocationType getDefault() {
		return ZIP_CITY_CODE;
	} 
}
