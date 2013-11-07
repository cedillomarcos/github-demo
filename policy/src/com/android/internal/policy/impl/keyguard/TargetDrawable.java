/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.internal.policy.impl.keyguard;

import java.util.Arrays;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.util.Log;
import android.view.View;
import com.mediatek.common.featureoption.FeatureOption;

import com.android.internal.R;

public class TargetDrawable {
    private static final String TAG = "TargetDrawable";
    private static final boolean DEBUG = false;

    public static final int[] STATE_ACTIVE =
            { android.R.attr.state_enabled, android.R.attr.state_active };
    public static final int[] STATE_INACTIVE =
            { android.R.attr.state_enabled, -android.R.attr.state_active };
    public static final int[] STATE_FOCUSED =
            { android.R.attr.state_enabled, -android.R.attr.state_active,
                android.R.attr.state_focused };
    
    //zhuwei add
    private Bitmap mUnReadBg;
    private int unReadBgWidth;
    private int unReadBgHeight;
    private int unReadCount = 0;
    private boolean isPhoneOrMmsDrawable;
    private boolean drawUnReadView;
    private Bitmap mBg;
    
    private static final int MMS_UNREAD_POSITION_X = 40;
    private static final int MMS_UNREAD_POSITION_Y = -15;
    
    private static final int PHONE_UNREAD_POSITION_X = -10;
    private static final int PHONE_UNREAD_POSITION_Y = -15;
    
    private static final int UNREAD_COUNT_LAYOUT = 100;
    
    private static final int CENTER_KEY_WIDTH = 214;
    private static final int CENTER_KEY_HEIGHT = 214;
    
    //end

    private float mTranslationX = 0.0f;
    private float mTranslationY = 0.0f;
    private float mPositionX = 0.0f;
    private float mPositionY = 0.0f;
    private float mScaleX = 1.0f;
    private float mScaleY = 1.0f;
    private float mAlpha = 1.0f;
    private Drawable mDrawable;
    private boolean mEnabled = true;
    private final int mResourceId;

    /* package */ static class DrawableWithAlpha extends Drawable {
        private float mAlpha = 1.0f;
        private Drawable mRealDrawable;
        public DrawableWithAlpha(Drawable realDrawable) {
            mRealDrawable = realDrawable;
        }
        public void setAlpha(float alpha) {
            mAlpha = alpha;
        }
        public float getAlpha() {
            return mAlpha;
        }
        public void draw(Canvas canvas) {
            mRealDrawable.setAlpha((int) Math.round(mAlpha * 255f));
            mRealDrawable.draw(canvas);
        }
        @Override
        public void setAlpha(int alpha) {
            mRealDrawable.setAlpha(alpha);
        }
        @Override
        public void setColorFilter(ColorFilter cf) {
            mRealDrawable.setColorFilter(cf);
        }
        @Override
        public int getOpacity() {
            return mRealDrawable.getOpacity();
        }
    }

    public TargetDrawable(Context context, int resId) {
        mResourceId = resId;
        mContext = context;
        Resources res = context.getResources();
        //zhuwei add
        if (mResourceId ==  R.drawable.gp811_ic_lockscreen_phone
        		|| mResourceId ==  R.drawable.gp811_ic_lockscreen_sms) {
        	Drawable mUnReadBgDrawable = res.getDrawable(R.drawable.unread_bg);
        	BitmapDrawable mBitmapDrawable = (BitmapDrawable) mUnReadBgDrawable;
        	mUnReadBg = mBitmapDrawable.getBitmap();
        	unReadBgHeight = mUnReadBg.getHeight();
        	unReadBgWidth = mUnReadBg.getWidth();
        	isPhoneOrMmsDrawable = true;
        } 
        setDrawable(res, resId);
    }
    
    //zhuwei add
    private Context mContext;
    
    public Context getContext() {
        return mContext;
    }
    
    public void setUnReadCount(int count) {
    	Log.i("zhuwei_target","count -->"+count);
    	unReadCount = count;
    }
    //end
    

    public void setDrawable(Resources res, int resId) {
        // Note we explicitly don't set mResourceId to resId since we allow the drawable to be
        // swapped at runtime and want to re-use the existing resource id for identification.
        Drawable drawable = resId == 0 ? null : res.getDrawable(resId);
        // Mutate the drawable so we can animate shared drawable properties.
        mDrawable = drawable != null ? drawable.mutate() : null;
        resizeDrawables();
        setState(STATE_INACTIVE);
        //zhuwei add  for press
        if (FeatureOption.FEATURE_GP811_LOCK_SCREEN) {
        	 if (mResourceId != R.drawable.gp811_lockscreen_center_key) {
             	Drawable mBgDrawable = res.getDrawable(R.drawable.gp811_ic_lockscreen_handle_pressed);
             	BitmapDrawable mBitmapDrawable = (BitmapDrawable) mBgDrawable;
             	Bitmap mBitmap = mBitmapDrawable.getBitmap();
             	int width = mBitmap.getWidth();
             	int height = mBitmap.getHeight();
             	int newWidth = CENTER_KEY_WIDTH;
             	int newHeight = CENTER_KEY_HEIGHT;
             	if (!(newWidth <= 0 || newHeight <= 0)) {
             		float scaleWidth = ((float) newWidth) / width;
                 	float scaleHeight = ((float) newHeight) / height;
                 	Matrix matrix = new Matrix();
                 	matrix.postScale(scaleWidth, scaleHeight);
                         mBg = Bitmap.createBitmap(mBitmap, 0, 0, width, height, matrix,true);
             	}
             }
        }
        //end
    }

    public TargetDrawable(TargetDrawable other) {
        mResourceId = other.mResourceId;
        // Mutate the drawable so we can animate shared drawable properties.
        mDrawable = other.mDrawable != null ? other.mDrawable.mutate() : null;
        resizeDrawables();
        setState(STATE_INACTIVE);
    }
    
    public int[] getState() {
        int [] state = null;
        if (mDrawable instanceof StateListDrawable) {
            StateListDrawable d = (StateListDrawable) mDrawable;
            state = d.getState();
        }
        return state;
    }

    public void setState(int [] state) {
        if (mDrawable instanceof StateListDrawable) {
            StateListDrawable d = (StateListDrawable) mDrawable;
            d.setState(state);
        }
    }

    public boolean hasState(int [] state) {
        if (mDrawable instanceof StateListDrawable) {
            StateListDrawable d = (StateListDrawable) mDrawable;
            // TODO: this doesn't seem to work
            return d.getStateDrawableIndex(state) != -1;
        }
        return false;
    }

    /**
     * Returns true if the drawable is a StateListDrawable and is in the focused state.
     *
     * @return
     */
    public boolean isActive() {
        if (mDrawable instanceof StateListDrawable) {
            StateListDrawable d = (StateListDrawable) mDrawable;
            int[] states = d.getState();
            for (int i = 0; i < states.length; i++) {
                if (states[i] == android.R.attr.state_focused) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true if this target is enabled. Typically an enabled target contains a valid
     * drawable in a valid state. Currently all targets with valid drawables are valid.
     *
     * @return
     */
    public boolean isEnabled() {
        return mDrawable != null && mEnabled;
    }

    /**
     * Makes drawables in a StateListDrawable all the same dimensions.
     * If not a StateListDrawable, then justs sets the bounds to the intrinsic size of the
     * drawable.
     */
    private void resizeDrawables() {
        if (mDrawable instanceof StateListDrawable) {
            StateListDrawable d = (StateListDrawable) mDrawable;
            int maxWidth = 0;
            int maxHeight = 0;
            for (int i = 0; i < d.getStateCount(); i++) {
                Drawable childDrawable = d.getStateDrawable(i);
                maxWidth = Math.max(maxWidth, childDrawable.getIntrinsicWidth());
                maxHeight = Math.max(maxHeight, childDrawable.getIntrinsicHeight());
            }
            if (DEBUG) KeyguardUtils.xlogD(TAG, "union of childDrawable rects " + d + " to: "
                        + maxWidth + "x" + maxHeight);
            d.setBounds(0, 0, maxWidth, maxHeight);
            for (int i = 0; i < d.getStateCount(); i++) {
                Drawable childDrawable = d.getStateDrawable(i);
                if (DEBUG) KeyguardUtils.xlogD(TAG, "sizing drawable " + childDrawable + " to: "
                            + maxWidth + "x" + maxHeight);
                childDrawable.setBounds(0, 0, maxWidth, maxHeight);
            }
        } else if (mDrawable != null) {
            mDrawable.setBounds(0, 0,
                    mDrawable.getIntrinsicWidth(), mDrawable.getIntrinsicHeight());
        }
    }

    public void setX(float x) {
        mTranslationX = x;
    }

    public void setY(float y) {
        mTranslationY = y;
    }

    public void setScaleX(float x) {
        mScaleX = x;
    }

    public void setScaleY(float y) {
        mScaleY = y;
    }

    public void setAlpha(float alpha) {
        mAlpha = alpha;
    }

    public float getX() {
        return mTranslationX;
    }

    public float getY() {
        return mTranslationY;
    }

    public float getScaleX() {
        return mScaleX;
    }

    public float getScaleY() {
        return mScaleY;
    }

    public float getAlpha() {
        return mAlpha;
    }

    public void setPositionX(float x) {
        mPositionX = x;
    }

    public void setPositionY(float y) {
        mPositionY = y;
    }

    public float getPositionX() {
        return mPositionX;
    }

    public float getPositionY() {
        return mPositionY;
    }

    public int getWidth() {
        return (int) (mDrawable != null ? mDrawable.getIntrinsicWidth() * mScaleX : 0);
    }

    public int getHeight() {
        return (int) (mDrawable != null ? mDrawable.getIntrinsicHeight() * mScaleY : 0);
    }

    public void draw(Canvas canvas) {
        if (mDrawable == null || !mEnabled) {
            return;
        }
        canvas.save(Canvas.MATRIX_SAVE_FLAG);
        canvas.translate(mTranslationX + mPositionX, mTranslationY + mPositionY);
        canvas.translate(-0.5f * getWidth(), -0.5f * getHeight());
        canvas.scale(mScaleX, mScaleY);
        mDrawable.setAlpha((int) Math.round(mAlpha * 255f));
        mDrawable.draw(canvas);
        
        
        //zhuwei add for press
        if (FeatureOption.FEATURE_GP811_LOCK_SCREEN) {
        	 drawUnreadAndPressState(canvas);
        }
        canvas.restore();
    }

	private void drawUnreadAndPressState(Canvas canvas) {
		if (Arrays.equals(getState(), STATE_ACTIVE) || Arrays.equals(getState(), STATE_FOCUSED)) {
		 	if (mBg != null) {
		 		Paint p = new Paint();
		 		p.setAlpha(255);
		 		float left = -getWidth()/2;
		 		float top = -getHeight()/2;
		 		canvas.drawBitmap(mBg, left, top,p);
		 	}
		 }
		 
		 if (isPhoneOrMmsDrawable && drawUnReadView) {
		 	if (unReadCount != 0) {
		 		 float width = unReadBgWidth * mScaleX;
		 		 float height = unReadBgHeight * mScaleY;
		 		 //Log.i("zhuwei_target", "width-->"+width);
		 		// Log.i("zhuwei_target", "height-->"+height);
		 		 float x =  mTranslationX + mPositionX;
		 		 float y =  mTranslationY + mPositionY;
		                 // Log.i("zhuwei_target", "x-->"+x);
		                 // Log.i("zhuwei_target", "y-->"+y);
		                 // Log.i("zhuwei_target","mTranslationX->"+mTranslationX+",mPositionX->"+mPositionX);
		                 // Log.i("zhuwei_target","mTranslationY->"+mTranslationY+",mPositionY->"+mPositionY);
		                 // Log.i("zhuwei_target","getWidth-->"+getWidth());
		                 //.i("zhuwei_target","getHegiht-->"+getHeight());
		                 // Log.i("zhuwei_target", "canvas height-->"+canvas.getHeight());
		 		// Log.i("zhuwei_target", "canvas width-->"+canvas.getWidth());
		 		/* if (mResourceId ==  R.drawable.gp811_ic_lockscreen_phone
		 	        		|| mResourceId ==  R.drawable.gp811_ic_lockscreen_sms)*/
		 		 if (mResourceId ==  R.drawable.gp811_ic_lockscreen_phone) {
		 			 x = PHONE_UNREAD_POSITION_X;
		 			 y = PHONE_UNREAD_POSITION_Y;
		              //unReadCount = 20;
		                         // Log.i("zhuwei_target","phone change x-->"+x+",y-->"+y);
		 		 } else if (mResourceId ==  R.drawable.gp811_ic_lockscreen_sms) {
		 			 x = MMS_UNREAD_POSITION_X;
		 			 y = MMS_UNREAD_POSITION_Y;
		              //unReadCount = 10;
		                          // Log.i("zhuwei_target","mms change x-->"+x+",y-->"+y);
		 		 }
		 		 Paint p = new Paint();
		 		 p.setAlpha((int) Math.round(mAlpha * 255f));
		 		 canvas.drawBitmap(mUnReadBg, x, y, p);
		 		 p.setColor(Color.argb((int) Math.round(mAlpha * 255f), 255, 255, 255));
		 		 int offset = 0;
		 		 p.setTextSize(p.getTextSize() + 2);
		 		 if (unReadCount < UNREAD_COUNT_LAYOUT) {
		 			 offset = (int) (width/4);
		 		 } else {
		 			 offset = (int) (width/3);
		 		 }
		 		 canvas.drawText(String.valueOf(unReadCount),x + width/2 - offset, y + height/2, p);
		 	}
		 }
	}

    public void setEnabled(boolean enabled) {
        KeyguardUtils.xlogD(TAG, "setEnable enabled=" + enabled);
        mEnabled  = enabled;
    }

    public int getResourceId() {
        return mResourceId;
    }
    
    //zhuwei add
    public void setLevel(int level) {
    	if (mDrawable != null) {
    		mDrawable.setLevel(level);
    	}
    }
    
    
    public void setUnReadVisibility(int visiblity) {
    	if (visiblity == View.VISIBLE) {
    		drawUnReadView = true;
    		Log.i("zhuwei", "set drawUnReadView -->true");
    	} else {
    		drawUnReadView = false;
    		Log.i("zhuwei", "set drawUnReadView -->false");
    	}
    }
   
    //end
}
