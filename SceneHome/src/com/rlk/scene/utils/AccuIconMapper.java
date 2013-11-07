package com.rlk.scene.utils;

import java.util.HashMap;
import java.util.Map;

import com.rlk.scene.R;




public class AccuIconMapper {
//	private static Map<Integer, Integer> iconsMap = new HashMap<Integer, Integer>();
	private static Map<Integer, Integer> weatherMap = new HashMap<Integer, Integer>();
//	static { 
//		iconsMap.put(301, R.drawable.common_m_weather_01);
//		iconsMap.put(302, R.drawable.common_m_weather_02);
//		iconsMap.put(303, R.drawable.common_m_weather_03);
//		iconsMap.put(304, R.drawable.common_m_weather_04);
//		iconsMap.put(305, R.drawable.common_m_weather_05);
//		iconsMap.put(306, R.drawable.common_m_weather_06);
//		iconsMap.put(307, R.drawable.common_m_weather_07);
//		iconsMap.put(308, R.drawable.common_m_weather_08);
//		iconsMap.put(311, R.drawable.common_m_weather_11);
//		iconsMap.put(312, R.drawable.common_m_weather_12);
//		iconsMap.put(313, R.drawable.common_m_weather_13);
//		iconsMap.put(314, R.drawable.common_m_weather_14);
//		iconsMap.put(315, R.drawable.common_m_weather_15);
//		iconsMap.put(316, R.drawable.common_m_weather_16);
//		iconsMap.put(317, R.drawable.common_m_weather_17);
//		iconsMap.put(318, R.drawable.common_m_weather_18);
//		iconsMap.put(319, R.drawable.common_m_weather_19);
//		iconsMap.put(320, R.drawable.common_m_weather_20);
//		iconsMap.put(321, R.drawable.common_m_weather_21);
//		iconsMap.put(322, R.drawable.common_m_weather_22);
//		iconsMap.put(323, R.drawable.common_m_weather_23);
//		iconsMap.put(324, R.drawable.common_m_weather_24);
//		iconsMap.put(325, R.drawable.common_m_weather_25);
//		iconsMap.put(326, R.drawable.common_m_weather_26);		
//		iconsMap.put(329, R.drawable.common_m_weather_29);
//		iconsMap.put(330, R.drawable.common_m_weather_30);
//		iconsMap.put(331, R.drawable.common_m_weather_31);
//		iconsMap.put(332, R.drawable.common_m_weather_32);
//		iconsMap.put(333, R.drawable.common_m_weather_33);
//		iconsMap.put(334, R.drawable.common_m_weather_34);
//		iconsMap.put(335, R.drawable.common_m_weather_35);
//		iconsMap.put(336, R.drawable.common_m_weather_36);
//		iconsMap.put(337, R.drawable.common_m_weather_37);
//		iconsMap.put(338, R.drawable.common_m_weather_38);
//		iconsMap.put(339, R.drawable.common_m_weather_39);
//		iconsMap.put(340, R.drawable.common_m_weather_40);
//		iconsMap.put(341, R.drawable.common_m_weather_41);
//		iconsMap.put(342, R.drawable.common_m_weather_42);
//		iconsMap.put(343, R.drawable.common_m_weather_43);
//		iconsMap.put(344, R.drawable.common_m_weather_44);
//	}
	
	static { 
		weatherMap.put(301, R.drawable.sum);
		weatherMap.put(302, R.drawable.little_cloudy);
		weatherMap.put(303, R.drawable.little_cloudy);
		weatherMap.put(304, R.drawable.little_cloudy);
		weatherMap.put(305, R.drawable.little_cloudy);
		weatherMap.put(306, R.drawable.little_cloudy);
		weatherMap.put(307, R.drawable.much_cloudy);
		weatherMap.put(308, R.drawable.much_cloudy);
		weatherMap.put(311, R.drawable.much_cloudy);
		weatherMap.put(312, R.drawable.rain);
		weatherMap.put(313, R.drawable.rain);
		weatherMap.put(314, R.drawable.rain);
		weatherMap.put(315, R.drawable.thunder);
		weatherMap.put(316, R.drawable.thunder);
		weatherMap.put(317, R.drawable.thunder);
		weatherMap.put(318, R.drawable.rain);
		weatherMap.put(319, R.drawable.snow);
		weatherMap.put(320, R.drawable.snow);
		weatherMap.put(321, R.drawable.snow);
		weatherMap.put(322, R.drawable.snow);
		weatherMap.put(323, R.drawable.snow);
		weatherMap.put(324, R.drawable.snow);
		weatherMap.put(325, R.drawable.snow);
		weatherMap.put(326, R.drawable.snow);		
		weatherMap.put(329, R.drawable.snow);
		weatherMap.put(330, R.drawable.sum);
		weatherMap.put(331, R.drawable.snow);
		weatherMap.put(332, R.drawable.no_sum);
		weatherMap.put(333, R.drawable.night_sum);
		weatherMap.put(334, R.drawable.night_cloudy);
		weatherMap.put(335, R.drawable.night_cloudy);
		weatherMap.put(336, R.drawable.night_cloudy);
		weatherMap.put(337, R.drawable.night_cloudy);
		weatherMap.put(338, R.drawable.night_cloudy);
		weatherMap.put(339, R.drawable.night_rain);
		weatherMap.put(340, R.drawable.night_rain);
		weatherMap.put(341, R.drawable.night_thunder);
		weatherMap.put(342, R.drawable.night_thunder);
		weatherMap.put(343, R.drawable.night_snow);
		weatherMap.put(344, R.drawable.night_snow);
	}

//	public static int getDrawableIdByIconId(Integer iconCode) {
//		Integer code = iconsMap.get(iconCode);
//		if(code==null)
//		{			
//				return 0;			
//		}
//		return code;
//	}
	public static int getWeatherDrawableIdByIconId(Integer iconCode){
		Integer code = weatherMap.get(iconCode);
		if(code==null)
		{			
				return R.drawable.unknown;	
		}
		return code;
	}
	

}
