package com.android.internal.policy.impl;

import android.util.Log;

public class Mylog{
	protected final static String TAG = "renxinquan" ;
	protected final static boolean Debug = true;
	public static void i(String msg){
		if(Debug)
			Log.i(TAG, msg);
	}
}
