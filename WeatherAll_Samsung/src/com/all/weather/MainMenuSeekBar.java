package com.all.weather;


import com.all.weather.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.SeekBar;

public class MainMenuSeekBar extends SeekBar implements SeekBar.OnSeekBarChangeListener{
	
	private static int PROGRESS_DRAWABLE_WIDTH = 774;
	private static int PROGRESS_DRAWABLE_HEIGHT = 15;
	private Drawable mScrollDrawBodyTemp = null;
	private Drawable mScrollDrawLeftTemp = null;
	private Drawable mScrollDrawRightTemp = null;
	
	private Drawable mScrollDrawBody = null;
	private Drawable mScrollDrawLeft = null;
	private Drawable mScrollDrawRight = null;
	
	private Drawable mScrollDrawBodyActive = null;
	private Drawable mScrollDrawLeftActive = null;
	private Drawable mScrollDrawRightActive = null;
	
	private Drawable mProgressDraw = null;
	
	private Drawable mScrollDraw = null;
//	private AllAppsGridView mAppsGridView = null;
	private int mWidthSize = 0;
	private int mBodyWidth = 0;		
	private int mLeftWidth = 0;		
	private int mRightWidth = 0;
	private int mHeightSize = 0;
	private int mIconWidth = 0;
	private int mMax = 0;
	private int mThumbPos = 0;
	
	
	public MainMenuSeekBar(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public MainMenuSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
    /**
     * Used to inflate the Workspace from XML.
     *
     * @param context The application's context.
     * @param attrs The attribtues set containing the Workspace's customization values.
     */
	public MainMenuSeekBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
    

	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub
		Log.i(" Gaoxusong Trace ", " onProgressChanged ");
		mThumbPos = getDrawPositon(progress);
//		mAppsGridView.scrollGridBySeekBar(progress);		
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		mScrollDrawLeftTemp = mScrollDrawLeftActive;
		mScrollDrawBodyTemp = mScrollDrawBodyActive;
		mScrollDrawRightTemp = mScrollDrawRightActive;
//		mAppsGridView.setFocusChange(true);
		invalidate();
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
		Log.i(" Gaoxusong Trace ", " onStopTrackingTouch ");
		mScrollDrawLeftTemp = mScrollDrawLeft;
		mScrollDrawBodyTemp = mScrollDrawBody;
		mScrollDrawRightTemp = mScrollDrawRight;		
//		mAppsGridView.setCompleteItemShow();
		invalidate();
		// TODO Auto-generated method stub		

	}
	
	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		setOnSeekBarChangeListener(this);
		
		setMax(100);
		mMax = 100;
		
		mScrollDrawLeft = getResources().getDrawable(R.drawable.hid_scroll_left_arrow);
		mScrollDrawBody = getResources().getDrawable(R.drawable.hid_scroll_body);
		mScrollDrawRight = getResources().getDrawable(R.drawable.hid_scroll_right_arrow);
		
		mScrollDrawLeftActive = getResources().getDrawable(R.drawable.hid_scroll_active_left_arrow);
		mScrollDrawBodyActive = getResources().getDrawable(R.drawable.hid_scroll_active_body);
		mScrollDrawRightActive = getResources().getDrawable(R.drawable.hid_scroll_active_right_arrow);
		
		mProgressDraw = getResources().getDrawable(R.drawable.hid_scroll_background);
		
		mScrollDrawLeftTemp = mScrollDrawLeft;
		mScrollDrawBodyTemp = mScrollDrawBody;
		mScrollDrawRightTemp = mScrollDrawRight;
		
		mLeftWidth = mScrollDrawLeftTemp.getIntrinsicWidth();
		mBodyWidth = mScrollDrawBodyTemp.getIntrinsicWidth();
		mRightWidth = mScrollDrawRightTemp.getIntrinsicWidth();
		mHeightSize = mScrollDrawBodyTemp.getIntrinsicHeight();
		setProgressDrawable(createProgressDrawable());
		
	}
	
	public void setThumbWidthAboutScroll(int screenWidth, int gridWidth) {
		

		int iconWidth = Math.max(50, (getWidth() * screenWidth)/gridWidth);
		
		mIconWidth = iconWidth;
		int bodyWidth = iconWidth - mScrollDrawLeft.getIntrinsicWidth()
		- mScrollDrawRight.getIntrinsicWidth();
		mBodyWidth = bodyWidth;
		mScrollDraw = createDrawableChangeSize(iconWidth, bodyWidth);	
		setThumb(mScrollDraw); 
	}
	
	private Drawable createDrawableChangeSize(int iconWidth, int bodyWidth) {
		
//		mScrollDrawLeft.setBounds(0, 0, mLeftWidth, mHeightSize);
		mScrollDrawRight.setBounds(mLeftWidth + bodyWidth, 0, iconWidth, mHeightSize);		
//		mScrollDrawBody.setBounds(mLeftWidth, 0, mLeftWidth + bodyWidth, mHeightSize);
		
		Bitmap resizedBitmap = Bitmap.createBitmap(iconWidth, mHeightSize,
				Bitmap.Config.ARGB_8888);
		final Canvas canvas = new Canvas();
		canvas.setBitmap(resizedBitmap);
		
//		mScrollDrawLeft.draw(canvas);
		mScrollDrawBody.draw(canvas);
//		mScrollDrawRight.draw(canvas);			 
	
		return new BitmapDrawable(resizedBitmap);
		
	}
	
	private Drawable createProgressDrawable( ) {
		mProgressDraw.setBounds(0, 0, PROGRESS_DRAWABLE_WIDTH, 
				PROGRESS_DRAWABLE_HEIGHT);
		Bitmap resizedBitmap = Bitmap.createBitmap(PROGRESS_DRAWABLE_WIDTH,
				PROGRESS_DRAWABLE_HEIGHT,
				Bitmap.Config.ARGB_8888); 
		final Canvas canvas = new Canvas();
		canvas.setBitmap(resizedBitmap);
		mProgressDraw.draw(canvas);
		return new BitmapDrawable(resizedBitmap);
	}
   
	@Override
	protected synchronized void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		
		super.onDraw(canvas);
		
		if (mScrollDraw != null) {
			mScrollDrawLeftTemp.setBounds(0, 0, mLeftWidth, mHeightSize);
			mScrollDrawRightTemp.setBounds(mLeftWidth + mBodyWidth, 0, mIconWidth, mHeightSize);		
			mScrollDrawBodyTemp.setBounds(mLeftWidth, 0, mLeftWidth + mBodyWidth, mHeightSize);
			
			canvas.save();
			// Translate the padding. For the x, we need to allow the thumb to
			// draw in its extra space		
			
			canvas.translate(mThumbPos, getPaddingTop());			

			mScrollDrawLeftTemp.draw(canvas);
			mScrollDrawRightTemp.draw(canvas);
			mScrollDrawBodyTemp.draw(canvas);
			canvas.restore();
		}		
	}
	
	
//	public void setAppsGridView(AllAppsGridView appsGridView) {
//		 mAppsGridView = appsGridView;
//	}
	
	private int getDrawPositon(int progress) {
		
		float scale = mMax > 0 ? (float) progress / (float) mMax : 0;
		int available = getWidth() - getPaddingLeft() - getPaddingRight();
		int thumbWidth = mIconWidth;
		available -= thumbWidth;
		int thumbPos = (int) (scale * available);
		return thumbPos;

	}
	
	
}
