package com.rlk.scene.items;
 
import android.content.Context;
import android.content.Intent; 
import android.view.MotionEvent;
public class LightItem extends TouchableDrawableItem { 
	private Context mContext;
	public LightItem(Context context, int x, int y, int width, int height,int id) {
		super(context, x, y, width, height,id);
		mContext = context; 
		setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(TouchableDrawableItem item, MotionEvent event) { 
				mContext.sendBroadcast(new Intent("com.ragentek_bu1.launcher.SCENE_BRIGHTNESS_CHANGED")); 
			}
		});
	}
	 
	 
	 
}
