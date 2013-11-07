package com.rlk.scene.items;


import android.R.integer;
import android.content.Context;
import android.graphics.Bitmap;
import android.nfc.tech.IsoDep;
import android.view.MotionEvent;

public class MySeekbar extends TouchableDrawableItem {
   Bitmap mThum;
   boolean isDown;	
   private int mMax=100;
   private MSeekbarListenerInfo mListenerInfo;
   private int mProgress=0;
   public static boolean isSeekbarTouched=false;
	static class MSeekbarListenerInfo {
		private OnStopTrackingListener mOnStopTrackingListener;
		private OnStartTrackingListener mOnStartTrackingListener;
		private OnProgressChangedListener mOnProgressChangedListener;
	}
	MSeekbarListenerInfo getSeekbarListenerInfo() {
        if (mListenerInfo != null) {
            return mListenerInfo;
        }
        mListenerInfo = new MSeekbarListenerInfo();
        return mListenerInfo;
    }
   
	MySeekbar(Context context, int x, int y, int width, int height) {
		super(context, x, y, width, height); 
	}
	
	@Override
		boolean onTouchEvent(MotionEvent event) { 
	     if (!isEnable()&&isInRegion(event.getX(), event.getY())) {
	            return true;
	        }
	        
	        switch (event.getAction()) {
	            case MotionEvent.ACTION_DOWN:
	            	if (isInRegion(event.getX(), event.getY())) {
	            	    setPressed(true);
	  	                onStartTrackingTouch();
	  	                trackTouchEvent(event);
	  	                isSeekbarTouched=true;
					}
	                break;
	                
	            case MotionEvent.ACTION_MOVE:
	            	if(isPressed())
	                trackTouchEvent(event);
	                break;
	                
	            case MotionEvent.ACTION_UP:
	            	if(isPressed())
	            	{
	            		trackTouchEvent(event);
	 	                onStopTrackingTouch();
	 	                setPressed(false);
	 	               isSeekbarTouched=false;
	            	}
	                break;
	                
	            case MotionEvent.ACTION_CANCEL:
	            	if(isPressed())
	            	{
	            		 onStopTrackingTouch();
	 	                setPressed(false);
	 	               isSeekbarTouched=false;
	            	}
	               
	                break;
	        }
	        return isPressed();
		}
    private void trackTouchEvent(MotionEvent event) {
        final int available = mWidth;
        int x = (int)event.getX();
        float scale;
        float progress = 0;
         scale = (float)(x - mPositionX-mTranslateX) / (float)available;
        final int max = getMax();
        progress += scale * max;
        
        setProgress((int) progress, true);
    }
	public void setMax( int max)
	{
		mMax=max;
	}
	public int getMax()
	{
		return mMax;
	}
	private void onStartTrackingTouch()
	{
		MSeekbarListenerInfo li = mListenerInfo;
		if (li != null && li.mOnStartTrackingListener != null) {
			li.mOnStartTrackingListener.OnStart(this);
		}
		
	}
	private void onStopTrackingTouch()
	{
		MSeekbarListenerInfo li = mListenerInfo;
		if (li != null && li.mOnStopTrackingListener != null) {
			li.mOnStopTrackingListener.onStop(this);
		}
		
	}
	private void onProgressChanged()
	{
		MSeekbarListenerInfo li = mListenerInfo;
		if (li != null && li.mOnProgressChangedListener != null) {
			li.mOnProgressChangedListener.OnChanged(this, mProgress);
		}
		
	}
	
	public void setOnStartTrackingListener(MySeekbar.OnStartTrackingListener l) {
		getSeekbarListenerInfo().mOnStartTrackingListener = l;
	}
	public void setOnStopTrackingListener(MySeekbar.OnStopTrackingListener l) {
		getSeekbarListenerInfo().mOnStopTrackingListener = l;
	}
	public void setOnProgressChangedListener(MySeekbar.OnProgressChangedListener l) {
		getSeekbarListenerInfo().mOnProgressChangedListener = l;
	}
	
	
	
    synchronized void setProgress(int progress, boolean fromUser) {

        if (progress < 0) {
            progress = 0;
        }

        if (progress > mMax) {
            progress = mMax;
        }

        if (progress != mProgress) {
            mProgress = progress;
            onProgressChanged();
        }
    }
	 public interface OnStopTrackingListener {
		void onStop(TouchableDrawableItem item);
	}
	 public interface OnStartTrackingListener {
			void OnStart(TouchableDrawableItem item);
		}
	 public interface OnProgressChangedListener {
			void OnChanged(TouchableDrawableItem item,int progress);
		}
}
