package com.android.internal.policy.impl.keyguard;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import com.android.internal.R;
public class AnimationView extends ImageView {
	ComponentName mComponentName;
	AnimationDrawable anim;
	Context mContext;
	DragIcon mdrag;
	BuubleView mBuubleView;
	CoatLockScreen mCoatLockScreen;
	void setInstance(ComponentName componentName ,DragIcon drag,BuubleView b,CoatLockScreen parent){
		mComponentName = componentName;
		mdrag = drag;
		mBuubleView =b;
		mCoatLockScreen = parent;
	}
	public AnimationView(Context context) {
		super(context);
		mContext = context;
		// TODO Auto-generated constructor stub
	
	}
	public AnimationView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		// TODO Auto-generated constructor stub

	}
	public AnimationView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		// TODO Auto-generated constructor stub
	}
	void setResource(int id){
		if(id == 0)
			setImageResource(R.drawable.blue_pop);
		else if(id == 1)
			setImageResource(R.drawable.red_pop);
		else
			setImageResource(R.drawable.yellow_pop);
		
		anim = (AnimationDrawable) getDrawable();
	}
	
	void start(){
		setVisibility(VISIBLE);
		int center_x,center_y;
		int loc[]= new int[2];
		mdrag.getLocationInWindow(loc);
		center_x = loc[0]+mdrag.getWidth()/2;
		center_y = loc[1]+mdrag.getHeight()/2;
		//layout(center_x-getWidth()/2,center_y-getHeight()/2, center_x+getWidth()/2, center_y+getHeight()/2);		
		mdrag.setVisibility(INVISIBLE);
		mBuubleView.setVisibility(INVISIBLE);
		anim.stop();
		anim.start();
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub

						if(mCoatLockScreen.mListener != null)
        				mCoatLockScreen.mListener.onHitComplete();
					

							new Handler().postDelayed(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
        		Intent intent = new Intent();
        		intent.setComponent(mComponentName);
        		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        		mContext.startActivity(intent);

				}}, 1000);	
				new Handler().postDelayed(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						anim.stop();
						setVisibility(INVISIBLE);
						mdrag.setVisibility(VISIBLE);
						mBuubleView.setVisibility(VISIBLE);

				}}, 2000);

			}
		},600);
	}
	
}
