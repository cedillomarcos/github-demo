package com.all.weather;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class AccuLocationHandler extends DefaultHandler{
	protected List<City> cityList = new ArrayList<City>();
	private static String LOCATION = "location";
	private static String STATE = "state";
	private static String CITY = "city";
	
	public List<City> getCityList() {
		Log.i("xia","cityList.size: "+cityList.size());
		return cityList;
	}
	
	@Override
	public void startDocument() throws SAXException {
		cityList.clear();
	}
	
	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {		
		
		if (localName.equals(LOCATION)) {
			cityList.add(new City(atts.getValue(CITY),atts.getValue(STATE), atts.getValue(LOCATION)));
			Log.i("xia", atts.getValue(CITY));
		}
	}
}
