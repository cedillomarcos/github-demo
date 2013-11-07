package com.android.internal.policy.impl.keyguard;

import android.content.ComponentName;
import android.content.Context;
import android.util.AttributeSet;
import android.util.BubbleUtils;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.internal.R;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import android.graphics.drawable.AnimationDrawable;
public class CoatLockScreen extends RelativeLayout {
	AnimationView mAnimationView;
	AnimationDrawable anim;
	Context mContext;
	public OnHitListener mListener;
    public static interface OnHitListener {
        void onHitComplete();
    }
	public CoatLockScreen(Context context) {
		super(context);
		mContext = context;
		// TODO Auto-generated constructor stub
	
	}
	public CoatLockScreen(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		// TODO Auto-generated constructor stub

	}
	public CoatLockScreen(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
		// TODO Auto-generated constructor stub
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		return false;
	}
@Override
protected void onDetachedFromWindow() {
	// TODO Auto-generated method stub
	super.onDetachedFromWindow();
	anim.stop();
}
	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		boolean is_remove = true;
		super.onFinishInflate();
		ImageView gesture = (ImageView)findViewById(R.id.gesture_id);
		gesture.setImageResource(R.drawable.lock_gesture);
		anim = (AnimationDrawable) gesture.getDrawable();
		anim.stop();
		anim.start();
		DragIcon drag = (DragIcon) findViewById(R.id.unLock);
		AnimationView animation = (AnimationView) findViewById(R.id.animation);
		drag.setInstance((FrameLayout)findViewById(R.id.unLockParent),this);
		BubbleUtils bt = new BubbleUtils(mContext);
		PackageManager pManager = mContext.getPackageManager();
		final Intent mainIntent = new Intent(Intent.ACTION_MAIN,null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		final List<ResolveInfo> infoList = pManager.queryIntentActivities(mainIntent, 0);
    for(String ss : bt.getList()){
    	is_remove = true;
    	for(ResolveInfo info : infoList){
    		if(info.activityInfo.name.equals(ss)){
    			new BuubleView(this, new ComponentName(info.activityInfo.packageName, info.activityInfo.name), (DragIcon)findViewById(R.id.unLock),mContext, animation);
    			is_remove = false;
    			infoList.remove(info);
    			break;
    		}
    		}
    		if(is_remove == true)
    			bt.setList(ss,false);
    	
    }
		
        new BuubleView(this, new ComponentName("com.android.settings", "com.android.settings.MyappListView"), (DragIcon)findViewById(R.id.unLock),mContext, animation);
   //     new BuubleView(this, new ComponentName("com.android.contacts", "com.android.contacts.activities.DialtactsActivity"),(DragIcon)findViewById(R.id.unLock), mContext, animation);
  //      new BuubleView(this, new ComponentName("com.android.email", "com.android.email.activity.Welcome"), (DragIcon)findViewById(R.id.unLock),mContext, animation);
        
	}
    public void setHitListener(OnHitListener listener) {
        mListener = listener;
    }	
}
