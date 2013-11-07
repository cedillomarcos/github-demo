package com.all.weather;

import java.io.Serializable;
import java.util.ArrayList;

public class Weather implements Serializable{	
	
	private static final long serialVersionUID = 1L;
	private CurrentInfo currentInfo= new CurrentInfo();
	private ArrayList<SimpleWeatherInfo> simpleimfos = new ArrayList<SimpleWeatherInfo>(7);

	public CurrentInfo getCurrentInfo() {
		return currentInfo;
	}
	
	public void setCurrentInfo(CurrentInfo currentInfo) {
		this.currentInfo = currentInfo;
	}
	
	public ArrayList<SimpleWeatherInfo> getSimpleimfos() {
		return simpleimfos;
	}
	
	public void setSimpleimfos(ArrayList<SimpleWeatherInfo> simpleimfos) {
		this.simpleimfos = simpleimfos;
	}
}
