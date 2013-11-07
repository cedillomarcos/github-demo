package com.all.weather;

import com.all.weather.R;


public enum UpdateMethod implements ITaskEnum{
	MANUAL(R.string.update_manual_preference), AUTO(R.string.update_auto_preference);
	
	private int resId;
	
	private UpdateMethod(int resId) {
		this.resId = resId;
	}
	
	public int getResId() {
		return resId;
	}
	
	public static UpdateMethod getDefault() {
		return MANUAL;
	} 
	
}
