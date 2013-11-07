package com.all.weather;

import java.util.HashMap;
import java.util.Map;

public class HtmlUrlCoder {
	private static Map<String, String> htmlEquivalentCodes = new HashMap<String, String>();
	
	static {
		htmlEquivalentCodes.put(" ", "%20");
		htmlEquivalentCodes.put("|", "%7C");
	}
	
	private HtmlUrlCoder() {
	}
	
	public static String codeUrl(String url) {
		for(Map.Entry<String, String> htmlEquivalentEntry : htmlEquivalentCodes.entrySet()) {
			url = url.replace(htmlEquivalentEntry.getKey(), htmlEquivalentEntry.getValue());
		}
		return url;
	} 
}
