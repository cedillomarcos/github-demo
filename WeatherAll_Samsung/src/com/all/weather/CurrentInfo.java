package com.all.weather;

import java.io.Serializable;

public class CurrentInfo implements Serializable{

	private static final long serialVersionUID = 1L;
	private String cu_tempreture;
	private String city;
	private String weathertext;
	private String weathericon;
//	private String windspeed;
//	private String contry;
//	private String humidity ;
//	private String winddirection;

	public CurrentInfo(){}
	
	public CurrentInfo(String city, String contry, String cu_tempreture,
			String weathericon, String weathertext,
			String windspeed, String humidity,String winddirection) {
		super();
		this.city = city;
//		this.contry = contry;
		this.cu_tempreture = cu_tempreture;
		this.weathericon = weathericon;
		this.weathertext = weathertext;
//		this.windspeed = windspeed;
//		this.humidity = humidity;
//		this.winddirection = winddirection;
	}
	
//	public String getHumidity() {
//		return humidity;
//	}
//	public void setHumidity(String humidity) {
//		this.humidity = humidity;
//	}
//	public String getWinddirection() {
//		return winddirection;
//	}
//	public void setWinddirection(String winddirection) {
//		this.winddirection = winddirection;
//	}
//   public String getContry() {
//		return contry;
//	}
//	public void setContry(String contry) {
//		this.contry = contry;
//	}
   public String getCu_tempreture() {
		return cu_tempreture;
	}
	public void setCu_tempreture(String cu_tempreture) {
		this.cu_tempreture = cu_tempreture;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}

	public String getWeathertext() {
		return weathertext;
	}
	public void setWeathertext(String weathertext) {
		this.weathertext = weathertext;
	}
	public String getWeathericon() {
		return weathericon;
	}
	public void setWeathericon(String weathericon) {
		this.weathericon = weathericon;
	}
//	public String getWindspeed() {
//		return windspeed;
//	}
//	public void setWindspeed(String windspeed) {
//		this.windspeed = windspeed;
//	}



}
