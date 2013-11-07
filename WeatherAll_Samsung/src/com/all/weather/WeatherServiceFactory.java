package com.all.weather;

public class WeatherServiceFactory {
	private static WeatherServiceFactory INSTANCE;
	
	private AccuWeatherService weatherService;
	
	private WeatherServiceFactory() {
	}
	
	public static WeatherServiceFactory getInstance(){
		if(INSTANCE == null) {
			INSTANCE = new WeatherServiceFactory();
		}
		return INSTANCE;
	}
	
	public AccuWeatherService createWeatherService() {
		if(weatherService == null) {
			weatherService = new AccuWeatherService(); 
		}
		return weatherService;
	}
}
