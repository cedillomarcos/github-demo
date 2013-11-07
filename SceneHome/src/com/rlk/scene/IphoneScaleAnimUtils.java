package com.rlk.scene;

public class IphoneScaleAnimUtils {
	public final static int CLOSED = 1;
	public final static int OPEN = 2;
	public final static int CLOSING = 3;
	public final static int OPENING = 4;
	
    /**
	 * ADW: easing functions for animation
	 */
	static float easeOut(float time, float begin, float end, float duration) {
		float change = end - begin;
		return change * ((time = time / duration - 1) * time * time + 1)
				+ begin;
	}

	static float easeIn(float time, float begin, float end, float duration) {
		float change = end - begin;
		return change * (time /= duration) * time * time + begin;
	}

	static float easeInOut(float time, float begin, float end, float duration) {
		float change = end - begin;
		if ((time /= duration / 2.0f) < 1)
			return change / 2.0f * time * time * time + begin;
		return change / 2.0f * ((time -= 2.0f) * time * time + 2.0f) + begin;
	}
	
	
	
}
