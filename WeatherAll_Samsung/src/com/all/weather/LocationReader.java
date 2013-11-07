package com.all.weather;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;


import android.util.Log;

public class LocationReader {
	
	private static LocationReader INSTANCE;
	private static final String ACCU_CITY_FIND_URL = "http://alamo.accu-weather.com/widget/alamo/city-find.asp?location=";
	private AccuLocationHandler handler;
	
	private LocationReader() {
		handler = new AccuLocationHandler();
	}
	
	public static LocationReader getInstance() {
		if(INSTANCE == null) {
			INSTANCE = new LocationReader();
		}
		return INSTANCE;
	}
	
	public List<City> getLocations(String location) {
		List<City> cityList = new ArrayList<City>();
		try {
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			URL url = new URL(HtmlUrlCoder.codeUrl(ACCU_CITY_FIND_URL + location));
			xr.setContentHandler(handler);
			InputSource is = new InputSource(url.openStream());
			if(is != null) {
				xr.parse(is);
				cityList = handler.getCityList();
			}			
		} catch (SAXParseException e) {
			Log.e("WeatherReader#getWeather", e.getMessage());
		} catch (ParserConfigurationException e) {
			Log.e("WeatherReader#getWeather", e.getMessage());
		} catch (SAXException e) { 
			Log.e("WeatherReader#getWeather", e.getMessage());
		} catch (IOException e) { 
			Log.e("WeatherReader#getWeather", e.getMessage());
		}
		return cityList;
	}
}
