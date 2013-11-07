package android.util;

import android.content.Context;
import android.provider.Settings;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class BubbleUtils {

	List<String> myList = new ArrayList<String>();
	boolean is_init = false;
	Context mContext;
	
	void check_init(){
			if(is_init == false)
				initList();
			is_init = true;
	}
	public BubbleUtils(Context context){
		mContext = context;
	}
	public boolean getState(){
        return Settings.Global.getString(mContext.getContentResolver(),Settings.Global.BUBBLE_ENABLED).equals("0") ? false : true;
	}
	
	public void setState(boolean valse){
		Settings.Global.putString(mContext.getContentResolver(),Settings.Global.BUBBLE_ENABLED,valse ? "1" : "0");
	}
	
	public int getCount(){
		int i=0;
		for(String qs:myList){
			i++;
		}
		return i;
	}
	
	public void initList(){
		String ss[] = Settings.Global.getString(mContext.getContentResolver(),Settings.Global.BUBBLE_APPLICATION).split(";");
		for(String s:ss){
			if( s.equals("") || s.length() == 0)
				continue;
			myList.add(s);
		}
	}
	
	public boolean is_contain_inList(String s){
		check_init();
		for(String ss:myList){
			if(s.equals(ss))
				return true;
		}
		return false;
	}
	
	public List<String> getList(){
		check_init();
		return myList;
	}
	
	public void setList(String s,boolean flag){
		check_init();
		if(flag){
		Log.d("liyang", "-->s: " + s);
			for(String ss:myList){
				if(ss.equals(s)){
					return;
				}}
			myList.add(s);
		}else{
			for(String ss:myList){
				if(ss.equals(s)){
					myList.remove(s);
					break;
				}}
		}
		String aaa = new String();
		for(String ssss : myList){
			aaa+=ssss;
			aaa+=";";	
	  }
		Settings.Global.putString(mContext.getContentResolver(),Settings.Global.BUBBLE_APPLICATION,aaa);
	}
}
