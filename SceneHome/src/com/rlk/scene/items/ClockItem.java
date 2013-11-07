package com.rlk.scene.items;

import java.util.TimeZone;

import com.rlk.scene.R;
import com.rlk.scene.SceneSurfaceView2;
import com.rlk.scene.Utilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.format.Time;
import android.util.Log;

public class ClockItem extends TouchableDrawableItem{
	public static Time mCalendar;
    private float mSeconds;
    private float mMinutes;
    private float mHour;
    private String mTimeZoneId;
    private boolean mChanged;
    private final Bitmap  mClockHour;
    private final Bitmap  mClockMinute; 
    //private final Bitmap  mClockSecond; 
    //private final Bitmap  mClockDial;
    private int mClockWidth=0;
    private int mClockHeight=0;
    private Matrix matrix=new Matrix();
    private Paint mPaint;
    private boolean isPaused=true;
    private Handler mHandler=new Handler();
    RectF mF;
    public ClockItem(Context context,int x,int y ,int width,int height) {
       super(context, x, y, width, height);
	    mCalendar = new Time();
		Resources r = context.getResources();
       mClockHour=BitmapFactory.decodeResource(r, R.drawable.clock_analog_hour);
       mClockMinute=BitmapFactory.decodeResource(r, R.drawable.clock_analog_minute);
       //mClockSecond=BitmapFactory.decodeResource(r, R.drawable.clock_analog_second);
       //mClockDial=BitmapFactory.decodeResource(r, R.drawable.clock_analog_dial);
       
       
       
       mClockWidth=mClockHour.getWidth()/2;
       mClockHeight=mClockHour.getHeight()/2;
       mPaint=new Paint();
       mPaint.setColor(Color.BLACK);
       mPaint.setTextSize(25);
       mPaint.setTypeface(Typeface.DEFAULT_BOLD);
       mPaint.setAntiAlias(true);
       mPaint.setFilterBitmap(true);
       
       mF=new RectF(0,0,mClockWidth*2,mClockHeight*2);
       
	}
	   private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {
	        	
	        	if(!isPaused)
	        	{
	        		 if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
	 	                String tz = intent.getStringExtra("time-zone");
	 	                mCalendar = new Time(TimeZone.getTimeZone(tz).getID());
	 	            }
	 	            onTimeChanged();
	 	            if(isInsight()){
	 	            	SceneSurfaceView2.instance.startRefrashWidget(); 	
	 	            }
	 	           
	        	}
	        }

	    };
	public void stopClock(){
		mHandler.removeCallbacks(mClockTick);
	}    
	public void startClock(){
		if(SceneSurfaceView2.instance.mCurScreen == 2){
        	mHandler.removeCallbacks(mClockTick);
	        mHandler.post(mClockTick);	
        }
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	       isPaused=true;
         getContext().unregisterReceiver(mIntentReceiver);
         mHandler.removeCallbacks(mClockTick);
		
	}
	@Override
	  public void onResume()
	  {
		  super.onResume();
		        isPaused = false;
	            IntentFilter filter = new IntentFilter();
	            filter.addAction(Intent.ACTION_TIME_TICK);
	            filter.addAction(Intent.ACTION_TIME_CHANGED);
	            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
	            getContext().registerReceiver(mIntentReceiver, filter, null, new Handler());
	        // NOTE: It's safe to do these after registering the receiver since the receiver always runs
	        // in the main thread, therefore the receiver can't run before this method returns.

	        // The time zone may have changed while the receiver wasn't registered, so update the Time
	        mCalendar = new Time();

	        // Make sure we update to the current time
	        onTimeChanged();

	        // tick the seconds
	        if(SceneSurfaceView2.instance.mCurScreen == 2){
	        	mHandler.removeCallbacks(mClockTick);
		        mHandler.post(mClockTick);	
	        }
	        
	  }
	  public boolean  drawItem(Canvas canvas,int spacX, boolean drawed)
	  {
		  	
			if(!super.drawItem(canvas, spacX, drawed))
				return false;
//			Log.d("ningyaoyun", "ClockItem drawItem");
		    canvas.save();
		    canvas.translate(-spacX + mPositionX, mPositionY);
		    //Matrix matrix=new Matrix();
		    matrix.reset();
		    //canvas.drawBitmap(mClockDial, matrix, null);
		    
	        matrix.setRotate((mHour / 12.0f )* 360.0f+180f, mClockWidth, mClockHeight);
	        canvas.drawBitmap(mClockHour, matrix, mPaint);
	        
	        matrix.setRotate((mMinutes / 60.0f) * 360.0f+180f, mClockWidth, mClockHeight);
	        canvas.drawBitmap(mClockMinute, matrix, mPaint);
	        matrix.setRotate(((mSeconds+1) / 60.0f) * 360.0f+180f,  mClockWidth, mClockHeight);
	        //canvas.drawBitmap(mClockSecond, matrix, mPaint);
	        canvas.restore();
	        return true;
	  }
	
	    private final Runnable mClockTick = new Runnable () {
	        @Override
	        public void run() {
	        	if(!isPaused)
	        	{
	        		onTimeChanged(); 
//	        		Log.d("ningyaoyun", "ClockItem isInsight()=" + isInsight() + ";isScrollFinished="
//	        				+ SceneSurfaceView2.instance.isScrollFinished());
	        		if(isInsight() && SceneSurfaceView2.instance.isScrollFinished()){
	        			SceneSurfaceView2.instance.startRefrashWidget();	
	        		} 
		            mHandler.postDelayed(mClockTick, 850);
	        	}
	        }
	    };
		
	    public void setTimeZone(String id) {
	        mTimeZoneId = id;
	        onTimeChanged();
	    }
	    
	    public void recycle()
	    {
	    	Utilities.recycleBitmap(mClockHour);
	    	Utilities.recycleBitmap(mClockMinute);
	    	//Utilities.recycleBitmap(mClockSecond);
	    	//Utilities.recycleBitmap(mClockDial);
	    	isPaused=true;
	    }

		private void onTimeChanged() {
			// TODO Auto-generated method stub
	        mCalendar.setToNow();
	        if (mTimeZoneId != null) {
	            mCalendar.switchTimezone(mTimeZoneId);
	        }
	        int hour = mCalendar.hour;
	        int minute = mCalendar.minute;
	        int second = mCalendar.second;
	  //      long millis = System.currentTimeMillis() % 1000;

	        mSeconds = second;//(float) ((second * 1000 + millis) / 166.666);
	        mMinutes = minute + second / 60.0f;
	        mHour = hour + mMinutes / 60.0f;
	        mChanged = true;

		}
	
}
