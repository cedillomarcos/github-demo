package com.rlk.scene.items;
  
import com.rlk.scene.R;
import com.rlk.scene.SceneSurfaceView2;
import com.rlk.scene.Utilities;
import com.rlk.scene.utils.AccuIconMapper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class WeatherItem extends TouchableDrawableItem {
	private Context mContext;
	private String TAG = "WeatherItem";
	private String city;
	private String tempretrueHigh;
//	private String tempretrueCur;
	private String tempretrueLow;
//	private Bitmap iconBtp;
	private Bitmap weatherBtp;
	private Paint weatherPaint;
	
	public WeatherItem(Context context, int x, int y, int width, int height, int id) {
		super(context, x, y, width, height, id); 
		mContext = context;
		weatherPaint = new Paint(); 
		weatherPaint.setColor(Color.WHITE);
               weatherPaint.setTextAlign(Paint.Align.CENTER);
	}
	@Override
	public boolean drawItem(Canvas canvas, int spacX, boolean drawed) { 
		if(!super.drawItem(canvas, spacX , drawed))
			return false;
		
		Log.d(TAG, "WeatherItem drawItem");
		canvas.save();
	    canvas.translate(-spacX + mPositionX, mPositionY);
	    weatherPaint.setTextSize(20);  
	    if(weatherBtp != null){
	    	canvas.drawBitmap(weatherBtp, (mWidth - weatherBtp.getWidth())/2, 0, weatherPaint); 
//			canvas.drawBitmap(iconBtp, (mWidth - iconBtp.getWidth())/2, 20, weatherPaint);
			canvas.drawText(city, 300, 160, weatherPaint);
		    canvas.drawText(tempretrueHigh + "~" + tempretrueLow, 300, 200, weatherPaint);
	    }
	    
		canvas.restore();
        return  true;
	}
	
	@Override
	public void onPause() { 
		super.onPause();
		mContext.unregisterReceiver(mIntentReceiver);
	}
	@Override
	public void recycle() { 
		super.recycle();
		Utilities.recycleBitmap(weatherBtp);
	}
	@Override
	public void onResume() {
		Log.d(TAG, "WeatherItem onResume");
		IntentFilter filter = new IntentFilter();         
        filter.addAction("weather_info"); 
        mContext.registerReceiver(mIntentReceiver, filter, null, null); 
        Intent tolauncher = new Intent("laucherbooted");       
        mContext.sendBroadcast(tolauncher); 
		super.onResume();
	}
	
	private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			
			Bundle bundle = arg1.getExtras();
			city = bundle.getString("city");
			tempretrueHigh = bundle.getString("h_tempretrue");
			tempretrueLow = bundle.getString("l_tempretrue");
//			tempretrueCur = bundle.getString("c_tempretrue"); 
//			iconBtp = BitmapFactory.decodeResource(mContext.getResources(), 
//					AccuIconMapper.getDrawableIdByIconId(bundle
//							.getInt("Icon_id")));
			weatherBtp = BitmapFactory.decodeResource(mContext.getResources(), 
					AccuIconMapper.getWeatherDrawableIdByIconId(bundle
							.getInt("Icon_id")));
			if(isInsight()){
    			SceneSurfaceView2.instance.startRefrashWidget();	
    		} 
			Log.i(TAG, "-------laucher  onReceive--city=" + city + ";tempretrueHigh=" + tempretrueHigh); 
		}
	};
	
	
	
}
