package com.rlk.scene.items;

import java.util.ArrayList;
import java.util.Collection;

import android.R.integer;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;

public class TouchableDrawableGroup extends TouchableDrawableItem {
	protected ArrayList<TouchableDrawableItem> mItems;

	public TouchableDrawableGroup(Context context, int x, int y, int width,
			int height) { 
		super(context, x, y, width, height);
		mItems = new ArrayList<TouchableDrawableItem>();

	}

	public TouchableDrawableGroup(Context context, int x, int y, int width,
			int height, int id) { 
		super(context, x, y, width, height, id);
		mItems = new ArrayList<TouchableDrawableItem>();

	}

	public void addItem(TouchableDrawableItem item) {
		item.translate(mPositionX, mPositionY);
		// item.setScreenNumOnTopLayout(mScreenNumOnTopLayout);
		mItems.add(item);
	}

	public void clearItems() {
		for (TouchableDrawableItem mItem : mItems) {
			mItem.recycle();
		}
		mItems.clear();
	}

	@Override
	public boolean dispatchEvent(MotionEvent ev) {
		for (TouchableDrawableItem mItem : mItems) {
			if (mItem.dispatchEvent(ev))
				return true;
		}
		return super.dispatchEvent(ev);
	}

	@Override
	public void updateTouchRect(int spacX) { 
		super.updateTouchRect(spacX);
		for (TouchableDrawableItem mItem : mItems) {
			mItem.updateTouchRect(spacX);
		}
	}
	
	@Override
	boolean onTouchEvent(MotionEvent ev) {
		return super.onTouchEvent(ev);
	}

	@Override
	public boolean drawItem(Canvas canvas, int spanX ,boolean drawed) {
		super.drawItem(canvas, spanX, drawed);
		for (TouchableDrawableItem mItem : mItems) {
			mItem.drawItem(canvas, spanX, drawed);
		}
		return true;
	}

	@Override
	public void recycle() {
		for (TouchableDrawableItem mItem : mItems) {
			mItem.recycle();
		}
		super.recycle();
	}

}
