package com.all.weather;

import java.io.Serializable;

public class SimpleWeatherInfo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String data;
	private String week;
	private String icon;
	private String hightempreture;
	private String lowtempreture;
//	private String rainamount;
	private String textshort;
//	private String windspeed;
//	private String winddirection;
	private String imageids;
	private String imagetitles;
	
	public SimpleWeatherInfo()
	{
//		this.rainamount = "5.0";		
	}
	
	public SimpleWeatherInfo(String data, String hightempreture, String icon,
			String lowtempreture, String rainamount, String textshort,
			String week, String winddirection, String windspeed) {
		super();
		this.data = data;
		this.hightempreture = hightempreture;
		this.icon = icon;
		this.lowtempreture = lowtempreture;
//		this.rainamount = rainamount;
		this.textshort = textshort;
		this.week = week;
//		this.winddirection = winddirection;
//		this.windspeed = windspeed;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

//	public String getWindspeed() {
//		return windspeed;
//	}
//
//	public void setWindspeed(String windspeed) {
//		this.windspeed = windspeed;
//	}
//
//	public String getWinddirection() {
//		return winddirection;
//	}
//
//	public void setWinddirection(String winddirection) {
//		this.winddirection = winddirection;
//	}

	public String getTextshort() {
		return textshort;
	}

	public void setTextshort(String textshort) {
		this.textshort = textshort;
	}
	public String getWeek() {
		return week;
	}

	public void setWeek(String week) {
		this.week = week;
	}

	public String getHightempreture() {
		return hightempreture;
	}
	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public void setHightempreture(String hightempreture) {
		this.hightempreture = hightempreture;
	}
	
	public String getLowtempreture() {
		return lowtempreture;
	}
	
	public void setLowtempreture(String lowtempreture) {
		this.lowtempreture = lowtempreture;
	}

	public String getImagetitles() {
		return imagetitles;
	}

	public void setImagetitles(String imagetitles) {
		this.imagetitles = imagetitles;
	}

	public String getImageids() {
		return imageids;
	}

	public void setImageids(String imageids) {
		this.imageids = imageids;
	}
	
//	public String getRainamount() {
//		return rainamount;
//	}
//
//	public void setRainamount(String rainamount) {
//		this.rainamount = rainamount;
//	}
	
}
