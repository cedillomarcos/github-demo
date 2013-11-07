package com.rlk.scene;

import android.view.animation.TranslateAnimation;

public class IphoneTranslate extends TranslateAnimation{
	
	private IphoneBubbleTextView mBubbleTextView;
	
	public IphoneBubbleTextView getBubbleTextView() {
		return mBubbleTextView;
	}

	public IphoneTranslate(float fromXDelta, float toXDelta, float fromYDelta,
			float toYDelta, IphoneBubbleTextView bubbleTextView) {
		super(fromXDelta, toXDelta, fromYDelta, toYDelta);
		mBubbleTextView = bubbleTextView;
	}
}
