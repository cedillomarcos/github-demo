package com.android.internal.policy.impl.keyguard;


import android.app.Service;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.R;
import android.os.PowerManager;
import android.os.SystemClock;
import android.content.res.Configuration;
public class DragIcon extends ImageView {
	PowerManager mPw;
	private int lastX, lastY;
    private boolean is_ok = false;
    private FrameLayout mParentLayout;
    Vibrator mVibrator;
    CoatLockScreen mCoatLockScreen;
    boolean isLand = false;
	public DragIcon(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		if(mVibrator == null)
			mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		if(mPw == null)
					mPw = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
		isLand = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;			
	}
	
    public void setInstance(FrameLayout parent_layout,CoatLockScreen coat) {
    	mParentLayout = parent_layout;
    	mCoatLockScreen = coat;
    	
    }
    
	public DragIcon(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		if(mVibrator == null)
			mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		if(mPw == null)
					mPw = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
		isLand = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;			
	}
	public DragIcon(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		if(mVibrator == null)
			mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		if(mPw == null)
					mPw = (PowerManager)mContext.getSystemService(Context.POWER_SERVICE);
		isLand = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;			
	}

	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		int ea = event.getAction();
        switch (ea) {
        case MotionEvent.ACTION_DOWN:
        	mPw.userActivity(SystemClock.uptimeMillis(),false);
        	//this.setBackgroundColor(Color.GREEN);
        	setImageAlpha(50);
            lastX = (int) event.getRawX();
            lastY = (int) event.getRawY();
            Log.i("renxinquan","getWidth =" +getWidth() + "getHeight =" + getHeight() + "mParentLayout.getWidth() =" + mParentLayout.getWidth());
            break;

        case MotionEvent.ACTION_MOVE:
        		
            int dx = 0;//(int) event.getRawX() - lastX;
            int dy = (int) event.getRawY() - lastY;
						if(isLand){
							dx = (int) event.getRawX() - lastX;
							dy = 0;
						}else{
							dx = 0;
							dy = (int) event.getRawY() - lastY;
						}
						
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
						
            if (r > mParentLayout.getWidth()) {
                r = mParentLayout.getWidth();
                l = r - getWidth();
                if(isLand)
                	   is_ok = true;
            }else{
            	if(isLand)
            		is_ok = false;
            }

						if(b > mParentLayout.getHeight()){
							b = mParentLayout.getHeight();
							t = b - getHeight();
							if(!isLand)
								is_ok = true;
						}else{
							if(!isLand)
								is_ok = false;
						}

            layout(l, t, r, b);
            Log.i("renxinquan","left====="+getLeft() + "right ====" +getRight());

            lastX = (int) event.getRawX();
            lastY = (int) event.getRawY();

            break;
        case MotionEvent.ACTION_UP:
            //this.setBackgroundColor(Color.TRANSPARENT);
            //setImageResource(R.drawable.unlockimg);
            setImageAlpha(255);
        	if(is_ok){
        		if(mCoatLockScreen.mListener!=null)
        			mCoatLockScreen.mListener.onHitComplete();
        	}else{
        		goBack();
        	}
        	
            break;
        }
        return true;
		//return super.onTouchEvent(event);
	}
	
	void goBack(){
		if(!isLand){
		int y = getTop();
		int r;
		for(;;y = y - 1){
			if(y < 0){
				y= 0;
				r = y+getHeight();
				layout(getLeft(),y,getRight(),r);
				break;
			}
			//Log.i("renxinquan", "up lastX = " + x + "mParentLayout.getWidth()-lastX" + (mParentLayout.getWidth()-getWidth()-x));
			r = y+getHeight();
			layout(getLeft(),y,getRight(),r);
		}}else{
				int x = getLeft();
				int r;
		for(;;x = x - 1){
			if(x < 0){
				x= 0;
				r = x+getWidth();
				layout(x,getTop(),x+getWidth(),getBottom());
				break;
			}
			//Log.i("renxinquan", "up lastX = " + x + "mParentLayout.getWidth()-lastX" + (mParentLayout.getWidth()-getWidth()-x));
			r = x+getWidth();
			layout(x,getTop(),x+getWidth(),getBottom());
		}
		}
	}
	

}
