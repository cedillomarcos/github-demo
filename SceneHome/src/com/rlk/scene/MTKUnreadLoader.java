
package com.rlk.scene;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.os.AsyncTask;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.SuperscriptSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml; 
import com.android.internal.util.XmlUtils;   

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

class UnreadSupportShortcut {
    public UnreadSupportShortcut(String pkgName, String clsName, String keyString, int type) {
        mComponent = new ComponentName(pkgName, clsName);
        mKey = keyString;
        mShortcutType = type;
        mUnreadNum = 0;
    }

    ComponentName mComponent;
    String mKey;
    int mShortcutType;
    int mUnreadNum;

    @Override
    public String toString() {
        return "{UnreadSupportShortcut[" + mComponent + "], key = " + mKey + ",type = "
                + mShortcutType + ",unreadNum = " + mUnreadNum + "}";
    }
}

/**
 * M: This class is a util class, implemented to do the following two things,:
 * 
 * 1.Read config xml to get the shortcuts which support displaying unread number,
 * then get the initial value of the unread number of each component and update
 * shortcuts and folders through callbacks implemented in Launcher. 
 * 
 * 2. Receive unread broadcast sent by application, update shortcuts and folders in
 * workspace, hot seat and update application icons in app customize paged view.
 */
public class MTKUnreadLoader extends BroadcastReceiver {
    private static final String TAG = "MTKUnreadLoader";
    private static final String TAG_UNREADSHORTCUTS = "unreadshortcuts";

    private static final SpannableStringBuilder EXCEED_STRING = new SpannableStringBuilder("99+");
    private static final ArrayList<UnreadSupportShortcut> UNREAD_SUPPORT_SHORTCUTS = new ArrayList<UnreadSupportShortcut>();

    private static int sUnreadSupportShortcutsNum = 0;
    private static final Object LOG_LOCK = new Object();

    private Context mContext;

    private WeakReference<UnreadCallbacks> mCallbacks;

    public MTKUnreadLoader(Context context) {
        mContext = context;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();
        if (Intent.MTK_ACTION_UNREAD_CHANGED.equals(action)) {
            final ComponentName componentName = (ComponentName) intent
                    .getExtra(Intent.MTK_EXTRA_UNREAD_COMPONENT);
            final int unreadNum = intent.getIntExtra(Intent.MTK_EXTRA_UNREAD_NUMBER, -1);
            Log.d(TAG, "Receive unread broadcast: componentName = " + componentName
                        + ", unreadNum = " + unreadNum + ", mCallbacks = " + mCallbacks
                        + getUnreadSupportShortcutInfo()); 

            if (mCallbacks != null && componentName != null && unreadNum != -1) {
                final int index = supportUnreadFeature(componentName);
                if (index >= 0) {
                    boolean ret = setUnreadNumberAt(index, unreadNum);
                    if (ret) {
                        final UnreadCallbacks callbacks = mCallbacks.get();
                        if (callbacks != null) {
                        	if(Launcher.getInstance() != null){
                        		Launcher.getInstance().updateNotificationCount(componentName.getClassName(), unreadNum);	
                        	}                        	
                            callbacks.updateNotificationCount(componentName.getClassName(), unreadNum);
                        }
                    }
                }
            }
        }
    }

    /**
     * Set this as the current Launcher activity object for the loader.
     */
    public void initialize(UnreadCallbacks callbacks) {
        mCallbacks = new WeakReference<UnreadCallbacks>(callbacks);
        Log.d(TAG, "initialize: callbacks = " + callbacks + ", mCallbacks = " + mCallbacks);
       
    }
    /**
     * Load and initialize unread shortcuts.
     * 
     * @param context
     */
    void loadAndInitUnreadShortcuts() {
    	loadUnreadSupportShortcuts();
        initUnreadNumberFromSystem();
    }
    

    /**
     * Initialize unread number by querying system settings provider.
     * 
     * @param context
     */
    private void initUnreadNumberFromSystem() {
        final ContentResolver cr = mContext.getContentResolver();
        final int shortcutsNum = sUnreadSupportShortcutsNum;
        UnreadSupportShortcut shortcut = null;
        for (int i = 0; i < shortcutsNum; i++) {
            shortcut = UNREAD_SUPPORT_SHORTCUTS.get(i);
            try {
                shortcut.mUnreadNum = android.provider.Settings.System.getInt(cr, shortcut.mKey);
                if(mCallbacks != null){
                	final UnreadCallbacks callbacks = mCallbacks.get();
                	if(callbacks != null){
                		callbacks.updateNotificationCount(shortcut.mComponent.getClassName(), shortcut.mUnreadNum);
                	}
                }
                Log.d(TAG, "ningyaoyun initUnreadNumberFromSystem: key = " + shortcut.mKey
                            + ", unreadNum = " + shortcut.mUnreadNum);
            } catch (android.provider.Settings.SettingNotFoundException e) {
                Log.e(TAG, "initUnreadNumberFromSystem SettingNotFoundException key = "
                        + shortcut.mKey + ", e = " + e.getMessage());
            }
        }
        Log.d(TAG, "initUnreadNumberFromSystem end:" + getUnreadSupportShortcutInfo());
        
    }

    private void loadUnreadSupportShortcuts() {
        long start = System.currentTimeMillis();
        Log.d(TAG, "loadUnreadSupportShortcuts begin: start = " + start);
        

        // Clear all previous parsed unread shortcuts.
        UNREAD_SUPPORT_SHORTCUTS.clear();

        try {
            XmlResourceParser parser = mContext.getResources().getXml(
                    R.xml.unread_support_shortcuts);
            AttributeSet attrs = Xml.asAttributeSet(parser);
            XmlUtils.beginDocument(parser, TAG_UNREADSHORTCUTS);

            final int depth = parser.getDepth();

            int type = -1;
            while (((type = parser.next()) != XmlPullParser.END_TAG || parser.getDepth() > depth)
                    && type != XmlPullParser.END_DOCUMENT) {

                if (type != XmlPullParser.START_TAG) {
                    continue;
                }

                TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.UnreadShortcut);
                synchronized (LOG_LOCK) {
                    UNREAD_SUPPORT_SHORTCUTS.add(new UnreadSupportShortcut(a
                            .getString(R.styleable.UnreadShortcut_unreadPackageName), a
                            .getString(R.styleable.UnreadShortcut_unreadClassName), a
                            .getString(R.styleable.UnreadShortcut_unreadKey), a.getInt(
                            R.styleable.UnreadShortcut_unreadType, 0)));
                }
                a.recycle();

            }
        } catch (XmlPullParserException e) {
            Log.w(TAG, "Got XmlPullParserException while parsing unread shortcuts.", e);
        } catch (IOException e) {
            Log.w(TAG, "Got IOException while parsing unread shortcuts.", e);
        }
        sUnreadSupportShortcutsNum = UNREAD_SUPPORT_SHORTCUTS.size();
        Log.d(TAG, "loadUnreadSupportShortcuts end: time used = "
                    + (System.currentTimeMillis() - start) + ",sUnreadSupportShortcutsNum = "
                    + sUnreadSupportShortcutsNum + getUnreadSupportShortcutInfo());
        
    }
    
    /**
     * Get unread support shortcut information, since the information are stored
     * in an array list, we may query it and modify it at the same time, a lock
     * is needed.
     * 
     * @return
     */
    private static String getUnreadSupportShortcutInfo() {
        String info = " Unread support shortcuts are ";
        synchronized (LOG_LOCK) {
            info += UNREAD_SUPPORT_SHORTCUTS.toString();
        }
        return info;
    }

    /**
     * Whether the given component support unread feature.
     * 
     * @param component
     * @return
     */
    static int supportUnreadFeature(ComponentName component) {
        Log.d(TAG, "supportUnreadFeature: component = " + component);
        
        if (component == null) {
            return -1;
        }

        final int size = UNREAD_SUPPORT_SHORTCUTS.size();
        for (int i = 0, sz = size; i < sz; i++) {
            if (UNREAD_SUPPORT_SHORTCUTS.get(i).mComponent.equals(component)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Set the unread number of the item in the list with the given unread number.
     * 
     * @param index
     * @param unreadNum
     * @return
     */
    static synchronized boolean setUnreadNumberAt(int index, int unreadNum) {
        if (index >= 0 || index < sUnreadSupportShortcutsNum) {
            Log.d(TAG, "setUnreadNumberAt: index = " + index + ",unreadNum = " + unreadNum
                        + getUnreadSupportShortcutInfo());
            
            if (UNREAD_SUPPORT_SHORTCUTS.get(index).mUnreadNum != unreadNum) {
                UNREAD_SUPPORT_SHORTCUTS.get(index).mUnreadNum = unreadNum;
                return true;
            }
        }
        return false;
    }

    /**
     * Get unread number of application at the given position in the supported
     * shortcut list.
     * 
     * @param index
     * @return
     */
    static synchronized int getUnreadNumberAt(int index) {
        if (index < 0 || index >= sUnreadSupportShortcutsNum) {
            return 0;
        }
        Log.d(TAG, "getUnreadNumberAt: index = " + index
                    + getUnreadSupportShortcutInfo());
        
        return UNREAD_SUPPORT_SHORTCUTS.get(index).mUnreadNum;
    }

    /**
     * Get unread number for the given component.
     * 
     * @param component
     * @return
     */
    static int getUnreadNumberOfComponent(ComponentName component) {
        final int index = supportUnreadFeature(component);
        return getUnreadNumberAt(index);
    }

    /**
     * Get the exceed text.
     * 
     * @return a spannable string with text "99+".
     */
    static CharSequence getExceedText() {
        return EXCEED_STRING;
    }

    /**
     * Generate a text contains specified span to display the unread information
     * when the value is more than 99, do not use toString to convert it to
     * string, that may cause the span invalid.
     */
    static {
        EXCEED_STRING.setSpan(new SuperscriptSpan(), 2, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        EXCEED_STRING.setSpan(new AbsoluteSizeSpan(22), 2, 3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public interface UnreadCallbacks {
        
        void updateNotificationCount(String className, int count);
 
    }
}
