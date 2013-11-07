package com.all.weather;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import android.content.Context;
import android.util.Log;

public class AccuWeatherXMLHandler extends DefaultHandler {
	private static final String CURRENT_CONDITIONS = "currentconditions";
	private static final String CITY = "city";
	private static final String STATE = "state";
	private static final String DAY = "day";
	private static final String TEMPERATURE = "temperature";
	private static final String WIND_SPEED = "windspeed";
	private static final String WIND_DIRECTION = "winddirection";
	private static final String RAINAMOUNT = "rainamount>";
	private static final String HUMINIDY = "humidity";
	private static final String HIGH_TEMPERATURE = "hightemperature";
	private static final String LOW_TEMPERATURE = "lowtemperature";
	private static final String WEATHER_ICON = "weathericon";
	private static final String TXT_SHORT = "txtshort";
	private static final String NIGHTTIME = "nighttime";
	private String charsToSet;
	private boolean inCurrentConditions =true;
	private boolean skipNext = false;
	protected Weather weather;
	protected Context context; 
	protected SimpleWeatherInfo temsimple;
	
	public AccuWeatherXMLHandler(Context context) {
		this.context = context;
	}
	
	public Weather getWeather() {
		
		if(weather.getCurrentInfo()==null||weather.getCurrentInfo().getCu_tempreture()==null)
			return weather;
	int cu = Integer.parseInt(weather.getCurrentInfo().getCu_tempreture());
	int hi = 	Integer.parseInt(weather.getSimpleimfos().get(0).getHightempreture());
	int lo = 	Integer.parseInt(weather.getSimpleimfos().get(0).getLowtempreture());
	if(cu >hi)
		weather.getSimpleimfos().get(0).setHightempreture(""+cu);
		if(cu<lo)
			weather.getSimpleimfos().get(0).setLowtempreture(""+cu);
		return weather;
	}

	@Override
	public void startDocument() throws SAXException {	
		Log.i("xia","start Document()");
		this.weather = new Weather();
	}

	@Override
	public void startElement(String namespaceURI, String localName,	String qName, Attributes atts) throws SAXException {

		if (NIGHTTIME.equals(localName)) {
				skipNext = true;
		}else if (DAY.equals(localName)) {
			temsimple = new SimpleWeatherInfo();
			weather.getSimpleimfos().add(temsimple);
			skipNext = false;
			} 		
	}

	@Override
	public void endElement(String namespaceURI, String localName, String qName)
			throws SAXException {		
		if(inCurrentConditions)
		{
			if(CITY.equals(localName)) {
				weather.getCurrentInfo().setCity(charsToSet);
//			} 
//			else if(STATE.equals(localName)) {
//				weather.getCurrentInfo().setContry(charsToSet);
			}else if(TEMPERATURE.equals(localName))
				weather.getCurrentInfo().setCu_tempreture(charsToSet);
			else if ("weathertext".equals(localName))
				weather.getCurrentInfo().setWeathertext(charsToSet);
			else if(WEATHER_ICON.equals(localName)){
				weather.getCurrentInfo().setWeathericon(charsToSet);
				int iconid = Integer.parseInt(weather.getCurrentInfo().getWeathericon()); 
				weather.getCurrentInfo().setWeathertext(context.getResources().getString(AccuIconMapper.getWeatherDescription(iconid)));
				
			}
//			else if(WIND_SPEED.equals(localName))
//				weather.getCurrentInfo().setWindspeed(charsToSet);
//			else if(WIND_DIRECTION.equals(localName))
//				weather.getCurrentInfo().setWinddirection(charsToSet);
//			else if(HUMINIDY.equals(localName))
//				weather.getCurrentInfo().setHumidity(charsToSet);	
			else if(CURRENT_CONDITIONS.equals(localName))
				inCurrentConditions = false;			
		}
		else
		{			
			if(!skipNext)
			{
				if("obsdate".equals(localName)) 
				{
					this.temsimple.setData(charsToSet);
				} 
				else if("daycode".equals(localName)) 
				{
					this.temsimple.setWeek(charsToSet);
				}
				else if(TXT_SHORT.equals(localName))
				{
					this.temsimple.setTextshort(charsToSet);
				}
				else if(WEATHER_ICON.equals(localName))
				{
					this.temsimple.setIcon(charsToSet);
				}
				else if(HIGH_TEMPERATURE.equals(localName))
				{
					this.temsimple.setHightempreture(charsToSet);
				}
				else if(LOW_TEMPERATURE.equals(localName))
				{
					this.temsimple.setLowtempreture(charsToSet);
				}
//				else if(WIND_SPEED.equals(localName))
//				{
//					this.temsimple.setWindspeed(charsToSet);
//				}
//				else if(WIND_DIRECTION.equals(localName))
//				{
//					this.temsimple.setWinddirection(charsToSet);
//				}
//				else if(RAINAMOUNT.equals(localName))
//				{
//					this.temsimple.setRainamount(charsToSet);
//				}
			}
		}
	}
	
	@Override	
	public void characters(char ch[], int start, int length) {
		charsToSet = new String(ch).substring(start, start+length);
	}
	
}
