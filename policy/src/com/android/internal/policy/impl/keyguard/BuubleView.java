package com.android.internal.policy.impl.keyguard;
import android.R.bool;
import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.R;
import android.os.PowerManager;
import android.os.SystemClock;
import java.util.Random;
import android.content.res.Configuration;
public class BuubleView extends TextView {
	PowerManager mPw;
    int centerX, centerY;
	Drawable mIcon;
	Paint mPaint;
	int mX,mY;
    int lastX, lastY;
    int color_buuble=0;
	ComponentName mComponentName;
	RandomValue mRandomValue;
	Context mContext;
	CharSequence mLabel;
	CoatLockScreen mParent;
	DragIcon mLockImage;
	AnimationView mAnimationView;
	Handler mHandler = new Handler();
	boolean is_continue_anim = false;;
	int off_set = 2;
	int time_offset = 5;
	float slope = 0.0f;
	
	//public static int state_Buuble=0;
	
	class RandomValue {
		private int mMin=0,mMax=0;
		RandomValue(int min,int max){
			mMin = min;
			mMax = max;
		}
		int getRadomValue(){
			return new Random().nextInt(500);
		}
		
	};
	

	private void reset_postion(){
		//mLockImage.setBackgroundColor(Color.TRANSPARENT);
		            	mLockImage.setImageAlpha(255);
		 //mLockImage.setImageResource(R.drawable.unlockimage);
        //mX = new RandomValue(0, mParent.getWidth()).getRadomValue();
        //mY = new RandomValue(0, mParent.getHeight()).getRadomValue();
	}
	
	private boolean x_flag = false;
	private boolean y_flag = false;
	private boolean getRandom(){
		return new RandomValue(6,10).getRadomValue()%2 == 0 ? true : false ;
	}
	
	Runnable mRunable = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			//Log.i("renxinquan", "mRunable is_continue_anim = "  +is_continue_anim);
			if(!is_continue_anim)
				return;
		//	int v = new RandomValue(0,10).getRadomValue()%2;
			if(!x_flag)
				mX = mX+off_set;
			else
				mX = mX-off_set;
			int h = new RandomValue(0,10).getRadomValue()%2; 
			
			if(!y_flag)
				mY = mY+off_set;
			else
				mY = mY-off_set;
			
			
			if(mX < 0){
					mX=0;
					x_flag = getRandom();
			}
			if((mX+getWidth())>mParent.getWidth()){
					mX = mParent.getWidth()-getWidth();
					x_flag = getRandom();
			}
			if(mY < 0){
					mY=0;
					y_flag = getRandom();
			}
			if((mY+getHeight())>mParent.getHeight()){
					mY = mParent.getHeight()-getHeight();
					y_flag = getRandom();
			}
            layout(mX, mY, mX+getWidth(), mY+getHeight());	
            mHandler.postDelayed(mRunable,time_offset);
		}
	};
	
	
	
		Runnable YRunable = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
            start_animation();
		}
	};
	
	int count = 0;
	void start_animation(){
		if(is_continue_anim)
			return;
		count = 0;
		is_continue_anim = true;
        mHandler.postDelayed(mRunable,time_offset);

	}

	void stop_animation(){
		
		count = 0;
		is_continue_anim = false;
 	mHandler.removeCallbacksAndMessages(null);
	}
	
	public BuubleView(CoatLockScreen parent_layout,ComponentName componentName,DragIcon img,Context context,AnimationView animation){
		super(context);
		x_flag = getRandom();
		y_flag = getRandom();
		mContext = context;
		mPw = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
		mParent = parent_layout;
		mAnimationView = animation;
        ActivityInfo info;
		try {
			info = mContext.getPackageManager().getActivityInfo(componentName, 0);
	        mIcon = info.loadIcon(context.getPackageManager());
	        mLabel = info.loadLabel(mContext.getPackageManager());	
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setTextSize(18);
		setTextAppearance(this.getContext(),android.R.style.TextAppearance_Medium);
		mLockImage = img;
		setText(mLabel);
		color_buuble = new RandomValue(0, parent_layout.getWidth()).getRadomValue()%3;
		if(color_buuble == 0)
			setBackgroundResource(R.drawable.blue);
		else if(color_buuble == 1)
			setBackgroundResource(R.drawable.red);
		else	
			setBackgroundResource(R.drawable.yellow);
		//setBackground(R.drawable.);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setFakeBoldText(true);
        mPaint.setTextAlign(Align.CENTER);	
        mComponentName = componentName;
        mX = new RandomValue(0, parent_layout.getWidth()).getRadomValue();
        mY = new RandomValue(0, parent_layout.getHeight()).getRadomValue();
       // slope = (float)(float)new RandomValue(-100, 100).getRadomValue()/(float)100f;
      //  if(slope > 1f || slope < 1f)
       // 	slope = 0.5f;
        setGravity(Gravity.CENTER);
        parent_layout.addView(this);
		            	mLockImage.setImageAlpha(255);
       // mLockImage.setBackgroundColor(Color.TRANSPARENT);
                   		//mLockImage.setImageResource(R.drawable.unlockimage);
     //   layout(mX,mY,mX+getWidth(),mY+getHeight());
        //Log.i("renxinquan","hello world");
       mHandler.postDelayed(YRunable,1000);
	}
	
	private boolean isContain(){
		int buubleLocation[] = new int[2];
		int mLockImageLocation[] = new int[2];
		
		getLocationInWindow(buubleLocation);
		mLockImage.getLocationInWindow(mLockImageLocation);
		
		int center_x,center_y;
		center_x = buubleLocation[0] + getWidth()/2;
		center_y = buubleLocation[1] + getHeight()/2;
       	if(center_x >= mLockImageLocation[0] && center_x <= mLockImageLocation[0] + mLockImage.getWidth() 
        		&& center_y >=mLockImageLocation[1] && center_y <= mLockImageLocation[1]+mLockImage.getHeight()){
       		return true;
       	}
       	return false;
	}
	
	private boolean change_background = false;
	

	
	
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int ea = event.getAction();
        switch (ea) {
        case MotionEvent.ACTION_DOWN:
        	stop_animation();
		mPw.userActivity(SystemClock.uptimeMillis(),false);
            lastX = (int) event.getRawX();
            lastY = (int) event.getRawY();
           // mParent.removeView(this);
            //setText(null);
           // mParent.addView(this, getLayoutParams());
          //  mDragLayer.mVibrator.vibrate(50);
         //   mUnreadCount++;
         //   mDragLayer.moveId = getId();
           // invalidate();
            //AnimUtils.startShakeAnim(getContext(), this, 1);
            break;

        case MotionEvent.ACTION_MOVE:
            int dx = (int) event.getRawX() - lastX;
            int dy = (int) event.getRawY() - lastY;

            int l = getLeft() + dx;
            int b = getBottom() + dy;
            int r = getRight() + dx;
            int t = getTop() + dy;

            if (l < 0) {
                l = 0;
                r = l + getWidth();
            }

            if (t < 0) {
                t = 0;
                b = t + getHeight();
            }

            if (r > mParent.getWidth()) {
                r = mParent.getWidth();
                l = r - getWidth();
            }

            if (b > mParent.getHeight()) {
                b = mParent.getHeight();
                t = b - getHeight();
            }
            layout(l, t, r, b);
            
            if(isContain()){
            	if(!change_background){
            		mPw.userActivity(SystemClock.uptimeMillis(),false);
            		change_background = true;
            	//mLockImage.setImageResource(R.drawable.unlockimgdown);
            	mLockImage.setImageAlpha(50);
            //	mLockImage.setBackgroundColor(Color.RED);
            		if(mLockImage.mVibrator!=null)
            			mLockImage.mVibrator.vibrate(100);
            	}
            }else{
            	if(change_background){
            		change_background = false;
            		//mLockImage.setImageResource(R.drawable.unlockimage);
            		//mLockImage.setBackgroundColor(Color.TRANSPARENT);
            		            	mLockImage.setImageAlpha(255);
            		if(mLockImage.mVibrator!=null)
            			mLockImage.mVibrator.vibrate(100);
            	}
            }
            lastX = (int) event.getRawX();
            lastY = (int) event.getRawY();
            centerX = (r + l) / 2;
            centerY = (int) ((b + t) / 2 - getTextSize());
            break;
        case MotionEvent.ACTION_UP:
    		mX=getLeft();
        	mY=getTop();
        	if(isContain()){
        		setVisibility(INVISIBLE);
        		mAnimationView.setResource(color_buuble);
        		mAnimationView.setInstance(mComponentName, mLockImage,this,mParent);
        		mAnimationView.start();
        	}else{

        		start_animation();
        	}
        	break;
        }
        return true;
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
    	// TODO Auto-generated method stub
    	super.onWindowFocusChanged(hasWindowFocus);
    	if(hasWindowFocus){
    		//Log.i("renxinquan","onWindowFocusChanged hasWindowFocus");
    		start_animation();
    	}else {
    		//Log.i("renxinquan","onWindowFocusChanged");
    		stop_animation();
    		reset_postion();
    	}
    }
    

    
 
    
}
