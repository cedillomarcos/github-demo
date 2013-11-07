package com.all.weather;

import android.net.Uri;
import android.provider.BaseColumns;

public final class City {
	
	private String city;
	private String state;
	private String location;

	public City(String city, String state, String location) {
		
		this.city = city;
		this.state = state;
		this.location = location;
	}
	
	public String getCity() {
		return city;
    }

    public void setCity(String city) {
		this.city = city;
    }
    
	public String getState() {
		return state;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
	    if(getCity().equals("Xiangtan")){
	        return String.format("%s --- %s", getCity(), getState().replace("Sichuan", "Hunan"));
	    }
	    else{
	        return String.format("%s --- %s", getCity(), getState());
	    }
		
	}

	public String getLocation() {
		return location;
	}
	
	public void setState(String state) {
		this.state = state;
	}

	public void setLocation(String location) {
		this.location = location;
	}
	
	
	
    public static final String AUTHORITY = "com.ragentek.weather.citycontentprovider";
    public static final class CityColumns implements BaseColumns {
        private CityColumns() {}
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/diaries");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.google.diary";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.google.diary";
        public static final String DEFAULT_SORT_ORDER = "cityname ASC";
        public static final String CITYNAME = "cityname";
        public static final String CITYNAME1 = "cityname1";
        public static final String ZIPCODE = "zipcode";
    }
    
}
