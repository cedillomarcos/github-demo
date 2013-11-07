package com.android.internal.policy.impl.keyguard;

import android.content.ActivityNotFoundException;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Intents;
import android.provider.ContactsContract.QuickContact;
import android.util.Log;
import android.widget.QuickContactBadge;

import com.mediatek.xlog.Xlog;

public abstract class UnReadObserver extends ContentObserver {
    static final String TAG = "UnReadObserver";
    
     //zhuwei modify
    LockScreenNewEventView mNewEventView = null;
    //final LockScreenNewEventView mNewEventView;
    TargetDrawable mTargetDrawable = null;
    //end
    
    long mCreateTime;
    
    //zhuwei modify change LockScreenNewEventView->Object
    public UnReadObserver(Handler handler, Object newEventView, long createTime) {
        super(handler);
        if (newEventView instanceof LockScreenNewEventView) {
        	 mNewEventView = (LockScreenNewEventView) newEventView;
        	 Log.i("zhuwei_unread", "newEventView instanceof LockScreenNewEventView");
        } else if (newEventView instanceof TargetDrawable) {
        	mTargetDrawable = (TargetDrawable) newEventView;
        	Log.i("zhuwei_unread", "newEventView instanceof TargetDrawable");
        }
        //mNewEventView = newEventView;
        mCreateTime = createTime;
    }
    
    public void onChange(boolean selfChange) {
        refreshUnReadNumber();
    }
    
    public abstract void refreshUnReadNumber();
    
    public final void upateNewEventNumber(final int unreadNumber) {
      //zhuwei adds
        if (mTargetDrawable != null) {
            mTargetDrawable.setUnReadCount(unreadNumber);
            Log.i("zhuwei", "mTargetDrawable set unreadNumber-->"+unreadNumber);
        }
        
        if (mNewEventView != null) {
            mNewEventView.setNumber(unreadNumber);                    
        } else {
        	Xlog.e(TAG, "mNewEventView is null");
        }
    }
    
    // When queryt base time changed, we need to reset new event number
    public void updateQueryBaseTime(long newBaseTime) {
        mCreateTime = newBaseTime;
        upateNewEventNumber(0);
    }

}
