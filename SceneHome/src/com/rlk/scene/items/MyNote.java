package com.rlk.scene.items;

import java.util.Locale;

import com.rlk.scene.R;


import android.R.color;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;

public class MyNote extends TouchableDrawableItem {
	private String mTitle = "";
	private String mContent = "";
	private Paint mPaint;
	private NotesItem.NoteItem mNoteItem;
	private int mTextLineLength = 5;

	MyNote(Context context, int x, int y, int width, int height, int id,
			String title, String content) {
		super(context, x, y, width, height, id);
		// TODO Auto-generated constructor stub
		mTitle = title;
		mContent = content;
		mPaint = new Paint();
		mPaint.setColor(Color.BLACK);
	}

	MyNote(Context context, int x, int y, int width, int height,
			NotesItem.NoteItem mItem) {
		super(context, x, y, width, height);

		mNoteItem = mItem;
		if (mNoteItem != null) {
			mTitle = mNoteItem.noteTitle;
			mContent = mNoteItem.note;
			int resid = 0;
			if (mNoteItem.notegroup.equals("1")) {
				resid = R.drawable.notebook_item1;
			} else if (mNoteItem.notegroup.equals("2")) {
				resid = R.drawable.notebook_item2;
			} else if (mNoteItem.notegroup.equals("3")) {
				resid = R.drawable.notebook_item3;
			} else if (mNoteItem.notegroup.equals("4")) {
				resid = R.drawable.notebook_item4;
			} else {
				resid = R.drawable.notebook_item0;
			}
			setBackGroud(resid);
		}
		mPaint = new Paint();
		mPaint.setColor(Color.BLACK);
		mPaint.setAntiAlias(true);
		mPaint.setFilterBitmap(true);
		mPaint.setTypeface(Typeface.MONOSPACE);
	}

	/*
	 * @Override public void drawItemOntopLayout(Canvas canvas, int spanX) { //
	 * TODO Auto-generated method stub super.drawItemOntopLayout(canvas, spanX);
	 * 
	 * }
	 */

	@Override
public boolean  drawItem(Canvas canvas, int spanX, boolean drawed) {
		// TODO Auto-generated method stub
		if(!super.drawItem(canvas, spanX ,drawed))
			return false;
		Log.d("ningyaoyun", "MyNote  drawItem");
		canvas.save();
		mPaint.setTextSize(14);
		mPaint.setAlpha(255);
		canvas.translate(-spanX + mPositionX + mTranslateX, mPositionY
				+ mTranslateY);
		canvas.drawText(mTitle, 15, 25, mPaint);
		mPaint.setTextSize(12);
		mPaint.setAlpha(200);
		/*
		 * if(NotesItem.isEnglishLocal) { mTextLineLength =9; } else {
		 * mTextLineLength =5; }
		 */
		if (mContent.length() >= mTextLineLength) {
			canvas.drawText(mContent, 0, mTextLineLength, 10, 45, mPaint);
			if (mContent.length() >= mTextLineLength * 2) {
				canvas.drawText(mContent, mTextLineLength, mTextLineLength * 2,
						10, 65, mPaint);

				if (mContent.length() >= mTextLineLength * 3) {
					String temp = mContent.substring(mTextLineLength * 2,
							mTextLineLength * 3 - 2);
					temp = temp + "……";
					// canvas.drawText(mContent, mTextLineLength*2,
					// mTextLineLength*3, 10, 85, mPaint);
					canvas.drawText(temp, 10, 85, mPaint);
				} else {
					canvas.drawText(mContent, mTextLineLength * 2,
							mContent.length(), 10, 85, mPaint);
				}
			} else {
				canvas.drawText(mContent, mTextLineLength, mContent.length(),
						10, 65, mPaint);
			}
		} else {
			canvas.drawText(mContent, 0, mContent.length(), 10, 45, mPaint);
		}
		canvas.restore();
		return true;
	}
}
