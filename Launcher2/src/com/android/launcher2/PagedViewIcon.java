/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher2;

import java.util.Random;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.TextView;
//add zhaojiangwei test
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewParent;

import com.android.launcher.R;
import com.mediatek.common.featureoption.FeatureOption;

import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.content.ComponentName;
//add zhaojiangwei test
/**
 * An icon on a PagedView, specifically for items in the launcher's paged view (with compound
 * drawables on the top).
 */
public class PagedViewIcon extends TextView {
    /** A simple callback interface to allow a PagedViewIcon to notify when it has been pressed */
    public static interface PressedCallback {
        void iconPressed(PagedViewIcon icon);
    }

    @SuppressWarnings("unused")
    private static final String TAG = "PagedViewIcon";
    private static final float PRESS_ALPHA = 0.4f;

    private PagedViewIcon.PressedCallback mPressedCallback;
    private boolean mLockDrawableState = false;
    
    
    //zhuwei add 
    private boolean isSystemApp = false;
    private Drawable mIconDelete;
   	private int iconDeleteBgWidth;
   	private int iconDeletegHeight;
   	private Rect mIconDeleteBgBounds;
    private final ValueAnimator mRotateAnim = new ValueAnimator();
    private static final Random mRandom = new Random();
    private static final int MESSAGE_ROTATE = 0;
    private static final int MESSAGE_CANCEL_ANIM = 1;
    private int mTranslateX;
    private int mTranslateY;
    private float mScaleX = 1.0f;
    private float mScaleY = 1.0f;
    private Object obj = new Object();
    private boolean isClickDeleteIconDown;
   	private boolean isClickDeleteIconUp;
   	boolean isUninstallApp = false;
   	private RectF mDeleteRectF;
   	private long mDuration = 1000;
   	final static int extraDeleteIconRegion = 15;
    private float mMinScale = 0.9f;
    private float mMaxScale = 1.0f;
    private boolean isScaleUp = false;
    private Object scaleUpChangeObj = new Object();
    private Bitmap mOldIcon = null;
    private final Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == MESSAGE_CANCEL_ANIM) {
		        Log.e("zhuwei", "PagedViewIcon.mHandler--invalidate()--item="+getText());
		        cancelRotateAnimation();
			}
			invalidate();
		}
    	
    };
    //end

    private Bitmap mIcon;
//add zhaojiangwei test
    private Context mContext;
//add zhaojiangwei test
    public PagedViewIcon(Context context) {
        this(context, null);
//add zhaojiangwei test
	if (FeatureOption.RLK_POWER_MODE){
	mContext = context;
	}
//add zhaojiangwei test
    }

    public PagedViewIcon(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
//add zhaojiangwei test
	if (FeatureOption.RLK_POWER_MODE){
	mContext = context;
	}
//add zhaojiangwei test
    }

    public PagedViewIcon(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        //zhuwei add 
        mIconDelete = getContext().getResources().getDrawable(R.drawable.gp811_icon_delete);
        iconDeleteBgWidth = mIconDelete.getIntrinsicWidth();
        iconDeletegHeight = mIconDelete.getIntrinsicHeight();
        mIconDeleteBgBounds = new Rect(0, 0, iconDeleteBgWidth, iconDeletegHeight);
        //end
        
//add zhaojiangwei test
	if (FeatureOption.RLK_POWER_MODE){
	mContext = context;
	}
//add zhaojiangwei test
    }

    public void applyFromApplicationInfo(ApplicationInfo info, boolean scaleUp,
            PagedViewIcon.PressedCallback cb) {
        mIcon = info.iconBitmap;
        mPressedCallback = cb;
	//add zhaojiangwei test
	if (false){
	boolean inPowerMode = Settings.Global.getInt(mContext.getContentResolver(), Settings.Global.POWER_MODE_ON, 0) != 0;
	ComponentName mComponentName = info.componentName;
	if(mComponentName!=null){
	String packageName = mComponentName.getPackageName();
	//Log.i("zjw", "zjw applyFromApplicationInfo packageName ="+ packageName);
	if ("com.android.browser".equals(packageName) && inPowerMode){		
		mIcon = Utilities.drawDisabledBitmap(mIcon,mContext);
	}
	}
	}
	//add zhaojiangwei test
        setCompoundDrawablesWithIntrinsicBounds(null, new FastBitmapDrawable(mIcon), null, null);
        setText(info.title);
        setTag(info);
        
        //zhuwei add
        ComponentName componentName = info.componentName;
        if (null != componentName && !isSystemApp) {
        	String packageName = componentName.getPackageName();
        	if ((info.flags & ApplicationInfo.DOWNLOADED_FLAG) == 0) {
			    isSystemApp = true;
			}
		}
        //end
    }
    
    @Override
    public void draw(Canvas canvas) {
    	// TODO Auto-generated method stub
    	super.draw(canvas);
    	//zhuwei add 
        drawDeleteIcon(canvas);
    	this.setScaleX(mScaleX);
    	this.setScaleY(mScaleY);
        //end
    }

    public void lockDrawableState() {
        mLockDrawableState = true;
    }

    public void resetDrawableState() {
        mLockDrawableState = false;
        post(new Runnable() {
            @Override
            public void run() {
                refreshDrawableState();
            }
        });
    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();

        // We keep in the pressed state until resetDrawableState() is called to reset the press
        // feedback
        if (isPressed()) {
            setAlpha(PRESS_ALPHA);
            if (mPressedCallback != null) {
                mPressedCallback.iconPressed(this);
            }
        } else if (!mLockDrawableState) {
            setAlpha(1f);
        }
    }
    
    //zhuwei add
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	boolean result = super.onTouchEvent(event);
        int left;
  		int top;
  		int right;
  		int bottom;
  		float actionDownX;
  		float actionDownY;
  		   
          switch (event.getAction()) {
              case MotionEvent.ACTION_DOWN:
                  if (Launcher.isAppsItemUninstallState && getIsAnimating()) {
                  	if (isUninstallApp) {
                  		isUninstallApp = false;
                  	}
                  	if (isClickDeleteIconDown) {
                  		isClickDeleteIconDown = false;
                  	}
                  	left = getScrollX();
                  	top = getScrollY();
                  	right = left+getWidth();
                  	bottom = top+getHeight();
                  	actionDownX = left+event.getX();
                  	actionDownY = top+event.getY();
                  	isClickDeleteIconDown = isClickDeleteIcon(left,top,right,bottom,actionDownX,actionDownY);
                  }
                  break;
              case MotionEvent.ACTION_CANCEL:
              case MotionEvent.ACTION_UP:
                  if (Launcher.isAppsItemUninstallState && getIsAnimating()) {
                  	if (isClickDeleteIconUp) {
                  		isClickDeleteIconUp = false;
                  	}
                  	left = getScrollX();
                  	top = getScrollY();
                  	right = left+getWidth();
                  	bottom = top+getHeight();
                  	actionDownX = left+event.getX();
                  	actionDownY = top+event.getY();
                  	isClickDeleteIconUp = isClickDeleteIcon(left,top,right,bottom,actionDownX,actionDownY);
                  	if (isClickDeleteIconDown && isClickDeleteIconUp) {
                  		Object tag = getTag();
                  		ItemInfo itemInfo = null;
                                Log.i("zhuwei","tag->"+tag);
                  		if (tag != null && tag instanceof ItemInfo) {
                  			itemInfo = (ItemInfo) tag;
                  		}
                  		if (itemInfo != null) {
                  			ViewParent parent = getParent();
                  			if (parent != null) {
                  				parent = parent.getParent();
                  			}
                  			if (parent instanceof PagedViewCellLayout || parent instanceof PagedViewCellLayoutChildren) {
                  				isUninstallApp = true;
                  			}
                  		}
                  	}
                  	Log.d("zhuwei", "PagedViewIcon.onTouchEvent--ACTION_CANCEL||ACTION_UP:isClickDeleteIconUp="+isClickDeleteIconUp+
                  				",isUninstallApp="+isUninstallApp);
                  	if (isClickDeleteIconDown) {
                  		isClickDeleteIconDown = false;
                  	}
                  	if (isClickDeleteIconUp) {
                  		isClickDeleteIconUp = false;
                  	}
                  }
                  break;
          }
          return result;
    }
    
    
    public void drawDeleteIcon(Canvas canvas){
        if (Launcher.isAppsItemUninstallState && getIsAnimating() && !isSystemApp) {
        	if (mIconDelete != null && mIconDeleteBgBounds != null) {
        		int iconDeleteBgPosX = getScrollX();
        		int iconDeleteBgPosY = getScrollY();
        		
        		canvas.save();
        		canvas.translate(iconDeleteBgPosX + 10, iconDeleteBgPosY);
        		mIconDelete.setBounds(mIconDeleteBgBounds);
        		mIconDelete.draw(canvas);
        		canvas.restore();
        	}
		}
    }
    
    public boolean getIsAnimating(){
    	if (null != mRotateAnim && mRotateAnim.isRunning()) {
			return true;
		}
    	return false;
    }
    public void startRotateAnimation() {
         Log.i("zhuwei","PagedViewIcon startRotateAnimation isSystemApp->"+isSystemApp);
    	if (isSystemApp) {
    		mOldIcon = mIcon;
    		mIcon = Utilities.drawDisabledBitmap(mIcon,mContext);
    		setCompoundDrawablesWithIntrinsicBounds(null, new FastBitmapDrawable(mIcon), null, null);
			return;
		}
    	isUninstallApp = false;
    	if (mRotateAnim != null) {
    		if (mRotateAnim.isRunning()) {
    			mRotateAnim.cancel();
			}
            initAnim();
    		mRotateAnim.start();
		}
    }
    
    private boolean isClickDeleteIcon(int left, int top, int right, int bottom, float actionDownX, float actionDownY){
    	try {
			mDeleteRectF = new RectF(left, top, right, bottom);
			if (mDeleteRectF.contains(actionDownX, actionDownY)) {
		        return true;
			}
		} catch (Exception e) {
			//nothing
		}
		return false;
    }
    
   private void initAnim() {
    	mRotateAnim.setDuration(mDuration);
    	mRotateAnim.setRepeatCount(-1);
    	if (isScaleUp) {
    		mRotateAnim.setFloatValues(mMinScale, mMaxScale,mMinScale);
		}else{
			mRotateAnim.setFloatValues(mMaxScale, mMinScale,mMaxScale);
		}
    	AnimatorUpdateListener update = new AnimatorUpdateListener() {
    		
    		@Override
    		public void onAnimationUpdate(ValueAnimator animation) {
    			final float percent = (Float) animation.getAnimatedValue();
    			mScaleX = percent;
    			mScaleY = percent;
    			mHandler.sendEmptyMessage(MESSAGE_ROTATE);
    			
    			if (isUninstallApp) {
    				mHandler.sendEmptyMessageDelayed(MESSAGE_CANCEL_ANIM, 200);
				}
    			
    			if (mRotateAnim.isRunning() && !Launcher.isAppsItemUninstallState) {
    				mHandler.sendEmptyMessageDelayed(MESSAGE_CANCEL_ANIM, 200);
				}
    		}
    	};
    	mRotateAnim.addUpdateListener(update);
    	mRotateAnim.addListener(new AnimatorListener() {
    		
    		@Override
    		public void onAnimationStart(Animator animation) {
    			
    		}
    		
    		@Override
    		public void onAnimationRepeat(Animator animation) {
    			
    		}
    		
    		@Override
    		public void onAnimationEnd(Animator animation) {
    			
    		}
    		
    		@Override
    		public void onAnimationCancel(Animator animation) {
    			
    		}
    	});
	}
   
   void setIsScaleUpValue(boolean bool){
       synchronized (scaleUpChangeObj) {
   	       isScaleUp = bool;
	   }
   }
   
   void initIsScaleUpValue(float value){
       int i = (int)value;
       setIsScaleUpValue((i%2) == 0);
   	   mDuration = i;
   	   mMinScale = (float) (Math.random()*0.015f + 0.925f);
   }
   
   public void cancelRotateAnimation() {
       if (isSystemApp) {
    	   if (mOldIcon != null) {
    		   mIcon = mOldIcon;
    		   setCompoundDrawablesWithIntrinsicBounds(null, new FastBitmapDrawable(mIcon), null, null); 
    	   }
	       return;
	   }
   	
   	   if (mRotateAnim != null && mRotateAnim.isRunning()) {
   	       mRotateAnim.cancel();
	   }
       mScaleX = 1.0f;
	   mScaleY = 1.0f;
       invalidate();
   }
  //end
    
    
}
