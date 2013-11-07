package com.rlk.scene.items;

import com.rlk.scene.R;
import com.rlk.scene.Utilities;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.Log;

public class CalendarItem extends TouchableDrawableItem {
	private final Bitmap mMonth;
	private final Bitmap mDay;
	private int mMonthText = 0;
	private int mDayText = 0;
	private Matrix matrix = new Matrix();
	private Paint mPaint;
	private Handler mHandler = new Handler();
	private boolean isPaused = true;
    private int mMonthWidth;
    private int mMonthHeight;
	public CalendarItem(Context context,int x,int y ,int width,int height) {
		super(context, x, y, width, height);
		Resources r = context.getResources();
		mMonth = BitmapFactory.decodeResource(r, R.drawable.calendarmonth);
		mDay = BitmapFactory.decodeResource(r, R.drawable.calendarday);
		mPaint = new Paint();
		mPaint.setColor(Color.BLACK);
		mPaint.setTextSize(25);
		mPaint.setTypeface(Typeface.DEFAULT_BOLD);
		mPaint.setAntiAlias(true);
		mPaint.setFilterBitmap(true);
		mMonthWidth =mMonth.getWidth();
		mMonthHeight=mMonth.getHeight();
	}
	

	public boolean drawItem(Canvas canvas, int spacX ,boolean drawed) {
		
		if(!super.drawItem(canvas, spacX, drawed))
			return false;
		canvas.save();
		canvas.translate(-spacX + mPositionX, mPositionY);
		matrix.setTranslate(0, 0);
		canvas.drawBitmap(mMonth, matrix, null);
		matrix.postTranslate(mMonthWidth, 0);
		canvas.drawBitmap(mDay, matrix, null);
		if (mMonthText < 10) {
			canvas.drawText(mMonthText + "", 21, 50, mPaint);
		} else {
			canvas.drawText(mMonthText + "", 14, 50, mPaint);
		}
		if (mDayText < 10)
			canvas.drawText(mDayText + "", mMonthWidth + 14, 50, mPaint);
		else {
			canvas.drawText(mDayText + "", mMonthWidth + 7, 50, mPaint);
		}
		canvas.restore();
		return true;
	}

	@Override
	public void recycle() {
		super.recycle();
		Utilities.recycleBitmap(mMonth);
		Utilities.recycleBitmap(mDay);
	}

	private final Runnable mClockTick = new Runnable() {
		@Override
		public void run() {
			if (!isPaused) {
				onTimeChanged();
				//SceneSurfaceView2.instance.drawScene();
				mHandler.postDelayed(mClockTick, 1000);
			}
		}
	};

	public void onResume() {
		super.onResume();
		isPaused = false;
		onTimeChanged();
		mHandler.removeCallbacks(mClockTick);
		mHandler.post(mClockTick);
	}

	public void onPause() {
		super.onPause();
		isPaused = true;
		mHandler.removeCallbacks(mClockTick);
	}

	private void onTimeChanged() {

		mMonthText = ClockItem.mCalendar.month + 1;
		mDayText = ClockItem.mCalendar.monthDay;
	}
}
