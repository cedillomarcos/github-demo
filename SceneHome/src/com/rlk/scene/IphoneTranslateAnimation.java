package com.rlk.scene;

import android.view.View;
import android.view.animation.TranslateAnimation;



public class IphoneTranslateAnimation extends TranslateAnimation {
	
	private View mAnimView;
	private int mToCellY;
	private int mToCellX;
	
	private boolean mIsLastAmin;

	public IphoneTranslateAnimation(View animView, float fromXDelta, float toXDelta,
			float fromYDelta, float toYDelta, int toCellX, int toCellY, boolean isLastAnim) {
		super(fromXDelta, toXDelta, fromYDelta, toYDelta);
		mAnimView = animView;
		mToCellX = toCellX;
		mToCellY = toCellY;
		mIsLastAmin = isLastAnim;
	}
	
	public View getAnimView() {
		return mAnimView;
	}
	
	public int getToCellY() {
		return mToCellY;
	}
	
	public int getToCellX() {
		return mToCellX;
	}
	
	public boolean isLastAnim() {
		return mIsLastAmin;
	}
	
}
