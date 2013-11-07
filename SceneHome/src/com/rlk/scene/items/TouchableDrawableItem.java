package com.rlk.scene.items;
 
import com.rlk.scene.SceneSurfaceView2;
import com.rlk.scene.Utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

public abstract class TouchableDrawableItem {
	private String TAG = "TouchableDrawableItem";
	private RectF touchRect = new RectF();
	private ListenerInfo mListenerInfo;
	protected int mPositionX = 0;
	protected int mPositionY = 0;
	protected int mWidth = 0;
	protected int mHeight = 0;
	private Matrix matrix = new Matrix();
	private Context mContext;
	private boolean isDown = false;
	private RectF srcrRectF;
	protected float mTranslateX = 0;
	protected float mTranslateY = 0;
	private Bitmap mBgButton;
	private Bitmap mDisableBitmap;
	private int mId = 0;
	private boolean isPressed;
	public boolean isDrawBlackBg = false;
	private Paint mPaint;
	private float mDownX;
	private float mDownY;
	private boolean isEnable = true;
    public static final int SCREENWIDTH=540;
    float mLastTouchX;
    float mLastTouchY;
    private boolean isLongClickable;
    public static  boolean mHasPerformedLongPress;
    private CheckForLongPress mPendingCheckForLongPress;
	static class ListenerInfo {
		private OnClickListener mOnClickListener;
		private OnLongClickListener mOnLongClickListener;
		private OnKeyListener mOnKeyListener;
		private OnTouchListener mOnTouchListener;
		private onDragListener  mOnDragListener;
	}
    static Handler mHandler=new Handler();
	ListenerInfo getListenerInfo() {
		if (mListenerInfo != null) {
			return mListenerInfo;
		}
		mListenerInfo = new ListenerInfo();
		return mListenerInfo;
	}

	public void setOnKeyListener(TouchableDrawableItem.OnKeyListener l) {
		getListenerInfo().mOnKeyListener = l;
	}

	public void setOnClickListener(TouchableDrawableItem.OnClickListener l) {
		getListenerInfo().mOnClickListener = l;
	}

	public void setOnLongClickListener(TouchableDrawableItem.OnLongClickListener l){
		isLongClickable = true;
		getListenerInfo().mOnLongClickListener = l;
	}
	
	public void setOnTouchListener(TouchableDrawableItem.OnTouchListener l) {
		getListenerInfo().mOnTouchListener = l;
	}
	public void setOnDragListener(TouchableDrawableItem.onDragListener l) {
		getListenerInfo().mOnDragListener = l;
	}
	public interface OnClickListener {
		void onClick(TouchableDrawableItem item, MotionEvent event);
	}
	public interface onDragListener {
		void onDrag(TouchableDrawableItem item, float x,float y);
	}
	public interface OnLongClickListener {
		boolean onLongClick(TouchableDrawableItem item);
	}

	public interface OnKeyListener {
		boolean onKey(TouchableDrawableItem v, int keyCode, KeyEvent event);
	}

	public interface OnTouchListener {
		boolean onTouch(TouchableDrawableItem v, MotionEvent event);
	}

	protected boolean isInRegion(float f, float g) {
		if (touchRect != null) {
			if (touchRect.left < f && touchRect.right > f && touchRect.top < g
					&& touchRect.bottom > g)
				return true;
		}
		return false;
	}

	private boolean isMoved(float x, float y, float x2, float y2) {
		float flag = (x2 - x) * (x2 - x) + (y2 - y) * (y2 - y);
		flag = (float) Math.sqrt(flag);
//		Log.e("test", "flag= "+flag);
		if (flag < 10)
			return false;
		else {
			return true;
		}
	}
    class CheckForLongPress implements Runnable {
        public void run() {
            if (isPressed()) {
                if (performLongClick()) {
                    mHasPerformedLongPress = true;
                }
            }
        }
    }
    private boolean isClickable = true;
    public void setClickable(boolean clickable)
    {
    	isClickable=clickable;
    }
    public boolean performLongClick() {
//    	Log.e("test2", "performLongClick");
    	  boolean handled = false;
    	ListenerInfo li = mListenerInfo;
		if (li != null && li.mOnLongClickListener != null) {
			handled=li.mOnLongClickListener.onLongClick(this);
		}
        return handled;
    }
	boolean onTouchEvent(MotionEvent ev) {
		boolean isInRegion = isInRegion(ev.getX(), ev.getY());
		if(!isClickable && isInRegion){
			return false;
		}
		  if (!isEnable&&isInRegion) {
			return true;
		  }
		ListenerInfo li = mListenerInfo;
		if (li != null && li.mOnTouchListener != null) {
			li.mOnTouchListener.onTouch(this, ev);
		}

		switch (ev.getAction()) {
		case MotionEvent.ACTION_UP:
			if (isInRegion) {
				 if (!mHasPerformedLongPress) {
                     removeLongPressCallback();
				if (isDown) {
					isDown = false;
					setPressed(false);
     					return performClick(ev);
				}
				 }
			}
			setPressed(false);
			break;
		case MotionEvent.ACTION_DOWN:
			if (isInRegion) {
				  if (mPendingCheckForLongPress == null) {
					  mPendingCheckForLongPress = new CheckForLongPress();
                  }
                  mHasPerformedLongPress = false;
                  if(isLongClickable)
                  mHandler.postDelayed(mPendingCheckForLongPress, 500);
				 mLastTouchX = getActiveX(ev);
	                mLastTouchY = getActiveY(ev);
				isDown = true;
				setPressed(true);
				mDownX = ev.getX();
				mDownY = ev.getY();
				return true;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			 final float x = getActiveX(ev);
             final float y = getActiveY(ev);
			if (isDown && isMoved(mDownX, mDownY, ev.getX(), ev.getY())) {
				isDown = false;
				setPressed(false);
				removeLongPressCallback();
			} 
			 if(mHasPerformedLongPress)
			 performDrag(x - mLastTouchX, y - mLastTouchY);
             mLastTouchX = x;
             mLastTouchY = y;
			
			break;
		case MotionEvent.ACTION_SCROLL:
			break;
		case MotionEvent.ACTION_CANCEL:
			isDown = false;
			setPressed(false);
			removeLongPressCallback();
			break;
		}
		return false;
	}
    float getActiveX(MotionEvent ev) {
        return ev.getX();
    }

    float getActiveY(MotionEvent ev) {
        return ev.getY();
    }
    private void removeLongPressCallback() {
        if (mPendingCheckForLongPress != null) {
        	mHandler.removeCallbacks(mPendingCheckForLongPress);
        }
    }
	/**
	 * when we add items to groups ,we should translate the items in groups
	 */
	public void translate(float x, float y) {
		mTranslateX = x;
		mTranslateY = y;
	}

	private boolean performClick(MotionEvent event) {
		ListenerInfo li = mListenerInfo;
		if (li != null && li.mOnClickListener != null) {
			li.mOnClickListener.onClick(this,event);
			return true;
		}
		return false;

	}

	private boolean performDrag(float x,float y ) {
//		Log.e("test2", "performDrag");
		ListenerInfo li = mListenerInfo;
		if (li != null && li.mOnDragListener != null) {
			li.mOnDragListener.onDrag(this, x, y);
			return true;
		}
		return false;

	}
	
	
	TouchableDrawableItem(Context context, int x, int y, int width, int height) {
		mContext = context;
		mPositionX = x;
		mPositionY = y;
		mWidth = width;
		mHeight = height;
		srcrRectF = new RectF(0, 0, mWidth, mHeight);
		mPaint = new Paint();
		mPaint.setColor(Color.argb(100, 0, 0, 255));
	}

	TouchableDrawableItem(Context context, int x, int y, int width, int height,
			int id) {
		this(context, x, y, width, height);
		if (id != 0)
			mBgButton = BitmapFactory.decodeResource(getContext()
					.getResources(), id);
	}

	TouchableDrawableItem(Context context, int x, int y, int width, int height,
			int id1, int id2) {
		this(context, x, y, width, height);
		if (id1 != 0)
			mBgButton = BitmapFactory.decodeResource(getContext()
					.getResources(), id1);
		if (id2 != 0)
			mDisableBitmap = BitmapFactory.decodeResource(getContext()
					.getResources(), id2);
	}

	public void setBackGroud(int id) {
		if (id != 0)
			mBgButton = BitmapFactory.decodeResource(getContext()
					.getResources(), id);
	}

	public void setBackGroudAndInvalidate(int id) {
		if (id != 0)
			mBgButton = BitmapFactory.decodeResource(getContext()
					.getResources(), id);
		invalidate();
	}

	public void setBackGroudDisable(int id) {
		if (id != 0)
			mDisableBitmap = BitmapFactory.decodeResource(getContext()
					.getResources(), id);
	}
    public void setBackGroud(Bitmap bmp )
    {
    	mBgButton=bmp;
    }
	public void setBackGroudDisable(Bitmap bmp )
	{
		mDisableBitmap=bmp;
	}
	public void setBackGroudAndInvalidate(Bitmap bmp ) {
		mBgButton = bmp;
		invalidate();
	}
	public void invalidate() {
		SceneSurfaceView2.instance.drawScene(SceneSurfaceView2.defaultX,
				SceneSurfaceView2.defaultY);
	}

	public void setEnable(boolean enable) {
		if (isEnable != enable) {
			isEnable = enable;
			if (isEnable) {
				if (mBgButton != null)
					invalidate();
			} else {
				if (mDisableBitmap != null)
					invalidate();
			}

		}
	}
	public boolean isEnable()
	{
		return isEnable;
	}

	TouchableDrawableItem(Context context) {
		mContext = context;
	}

	public Context getContext() {
		return mContext;
	}

	public void setTouchRect(RectF rect) {
		touchRect = rect;
	}

	public void onPause() {

	}

	public void onResume() {

	}

	public void recycle() {
		Utilities.recycleBitmap(mBgButton);
		Utilities.recycleBitmap(mDisableBitmap);
	}

	public void setSize() {

	}

	public void setPosition(int x, int y) {
		mPositionX = x;
		mPositionY = y;
	}

	public boolean isInsight() {
//		return true;
		boolean res = false;
		if (touchRect != null) {
			if ((touchRect.left >= 0 && touchRect.left <= SCREENWIDTH)
					|| (touchRect.right <= SCREENWIDTH && touchRect.right >= 0))
				res = true;
		}
		return res;
	}

	public boolean isInsight(int spanX) {
		matrix.setTranslate(-spanX + mPositionX + mTranslateX, mPositionY
				+ mTranslateY);
		RectF mRectF = new RectF();
		matrix.mapRect(mRectF, srcrRectF);
		boolean res = false;
		if (mRectF != null) {
			if ((mRectF.left >= 0 && mRectF.left <= SCREENWIDTH)
					|| (mRectF.right <= SCREENWIDTH && mRectF.right >= 0))
				res = true;
		}
		return res;
	}

	public void updateTouchRect(int spacX){
//		Log.d(TAG, "updateTouchRect spacX=" + spacX);
		matrix.setTranslate(-spacX + mPositionX + mTranslateX, mPositionY
				+ mTranslateY);
		RectF mRectF = new RectF();
		matrix.mapRect(mRectF, srcrRectF);
		setTouchRect(mRectF);
	}

	public boolean drawItem(Canvas canvas, int spacX ,boolean drawed) {
		matrix.setTranslate(-spacX + mPositionX + mTranslateX, mPositionY
				+ mTranslateY);
		RectF mRectF = new RectF();
		matrix.mapRect(mRectF, srcrRectF);
		if(!drawed){
			setTouchRect(mRectF);	
		}		
		if (!drawed && !isInsight())
			return false;
//        Log.e("SceneSurfaceView2", "drawItem");
		if (mDisableBitmap != null && mBgButton != null) {
			if (isEnable) {
				if (mBgButton != null) {
					matrix.postTranslate(
							(mRectF.right - mRectF.left - mBgButton.getWidth()) / 2,
							(mRectF.bottom - mRectF.top - mBgButton.getHeight()) / 2);
					canvas.drawBitmap(mBgButton, matrix, null);
				}
			} else {
				if (mDisableBitmap != null) {
					matrix.postTranslate(
							(mRectF.right - mRectF.left - mDisableBitmap
									.getWidth()) / 2,
							(mRectF.bottom - mRectF.top - mDisableBitmap
									.getHeight()) / 2);
					canvas.drawBitmap(mDisableBitmap, matrix, null);
				}
			}

		} else {
			if (mBgButton != null) {
				matrix.postTranslate(
						(mRectF.right - mRectF.left - mBgButton.getWidth()) / 2,
						(mRectF.bottom - mRectF.top - mBgButton.getHeight()) / 2);
				canvas.drawBitmap(mBgButton, matrix, null);
			}
		}

		if (isDrawBlackBg)
			canvas.drawRect(mRectF, new Paint());
//		if (isPressed)
//			canvas.drawRect(mRectF, mPaint);
		return true;
	}

	public boolean dispatchEvent(MotionEvent ev) {
		return onTouchEvent(ev);

	}

	public void setId(int id) {
		mId = id;
	}

	public int getId() {
		return mId;
	}

	public void setPressed(boolean isPressed) {
		if (this.isPressed != isPressed) {
			this.isPressed = isPressed;

			SceneSurfaceView2.instance.drawScene(SceneSurfaceView2.defaultX,
					SceneSurfaceView2.defaultY);
		}

	}

	public boolean isPressed() {
		return isPressed;
	}
}
