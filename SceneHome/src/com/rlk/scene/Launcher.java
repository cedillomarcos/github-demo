/*
w * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rlk.scene;

import android.util.Log;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Target;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List; 
import java.util.Set; 
import com.mediatek.common.featureoption.FeatureOption;
import com.rlk.scene.R; 
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.ActivityInfo;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.os.Parcelable;
import android.provider.LiveFolders;
import android.provider.Settings;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.method.TextKeyListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.MeasureSpec;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Scroller;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
 

/**
 * Default launcher application.
 */
public final class Launcher extends Activity implements View.OnClickListener, 
OnLongClickListener, MTKUnreadLoader.UnreadCallbacks{
    static final String LOG_TAG = "IphoneLauncher";
    static final String TAG = "Launcher";
    static final boolean LOGD = true;
   
    private static final boolean DEBUG = true; 
    private static final boolean PROFILE_DRAWER = false;
    private static final boolean PROFILE_ROTATE = false;
    private static final boolean DEBUG_USER_INTERFACE = false;
   
    static final String EXTRA_SHORTCUT_DUPLICATE = "duplicate";

    static final String EXTRA_CUSTOM_WIDGET = "custom_widget";
    static final String SEARCH_WIDGET = "search_widget";

    static final int WALLPAPER_SCREENS_SPAN = 1; 
    static final int DEFAULT_SCREN = 0;
    static final int DEFAULT_SCREN_COUNT = 1;
    static final int NUMBER_CELLS_X = 4;
     
    static final int NUMBER_CELLS_Y = 5; 
     
    static final int DIALOG_RENAME_FOLDER = 2;

    private static final String PREFERENCES = "launcher.preferences";

    // Type: int
    private static final String RUNTIME_STATE_CURRENT_SCREEN = "launcher.current_screen";
    // Type: boolean
    private static final String RUNTIME_STATE_ALL_APPS_FOLDER = "launcher.all_apps_folder"; 
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_SCREEN = "launcher.add_screen";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_CELL_X = "launcher.add_cellX";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_CELL_Y = "launcher.add_cellY";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_SPAN_X = "launcher.add_spanX";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_SPAN_Y = "launcher.add_spanY";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_COUNT_X = "launcher.add_countX";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_COUNT_Y = "launcher.add_countY";
    // Type: int[]
    private static final String RUNTIME_STATE_PENDING_ADD_OCCUPIED_CELLS = "launcher.add_occupied_cells"; 

    public static final LauncherModel sModel = new LauncherModel();

	private static final Object sLock = new Object();
    private static int sScreen = DEFAULT_SCREN;
    private static int mScreenCount = DEFAULT_SCREN_COUNT;
    
    public static final int CHANGE_GRAY = 1;
    public static final int CHANGE_COLOR = 2;

    private final BroadcastReceiver mApplicationsReceiver = new ApplicationsIntentReceiver();
    private final BroadcastReceiver mCloseSystemDialogsReceiver = new CloseSystemDialogsIntentReceiver();
    private final BroadcastReceiver mUpdateAppReceiver = new UpdateAppReceiver();
    
        
    private final ContentObserver mObserver = new FavoritesChangeObserver();
    private final NotificationLauncherHandler mNotificationHandler = new NotificationLauncherHandler();
    
    private LayoutInflater mInflater;

    private DragLayer mDragLayer;
    private Workspace mWorkspace;
    public MTKUnreadLoader mUnreadLoader;
    private IphonePageGuide mPageGuide;
    
    public static boolean mIphoneSnapScreenEffectEnable = false;
 
	private PackageManager mPM;
//	private StorageManager mStorageManager;

    static final int APPWIDGET_HOST_ID = 1024;

    private CellLayout.CellInfo mAddItemCellInfo;
    private CellLayout.CellInfo mMenuAddInfo;
    private final int[] mCellCoordinates = new int[2];

    private TransitionDrawable mHandleIcon;

    private boolean mDesktopLocked = true;
    private Bundle mSavedState;

    private SpannableStringBuilder mDefaultKeySsb = null;

    private boolean mDestroyed;
    
//    private boolean mIsNewIntent;

    private boolean mRestoring;
    private boolean mWaitingForResult;
    private boolean mLocaleChanged;

    private Bundle mSavedInstanceState;

    private DesktopBinder mBinder;
    
    private int mPostShowFolderDelay = 1000;
    private ShowFolderDelay mShowFolderDelay;
    
    public static Resources resources;
    
    private Handler handler = new Handler();
//    private IphoneStorageMounted storageListener = new IphoneStorageMounted();
    
    private class ShowFolderDelay implements Runnable{
    	
    	IphoneBubbleTextView bubbleTextView;
    	
    	public ShowFolderDelay(IphoneBubbleTextView textView){
    		bubbleTextView = textView;
    	}
    	
		public void run() {
			if(bubbleTextView != null){
				
				LauncherValues.dragIconStatu = LauncherValues.DRAG_ON_EXPEND_FOLDER;
				LauncherValues.getInstance().setHasFolderOpen(true);
				LauncherSettings.Favorites.ON_FOLDER_TARGET = false;	//放大拖动图标
				LauncherValues.IGNORE_IPHONE_BUBBLE_TEXTVIEW = true;
				LauncherValues.IGNORE_TRANSPARENTPANEL_TARGET = true;
				
				ApplicationInfo info = (ApplicationInfo)bubbleTextView.getTag();
				TextView textTitle = bubbleTextView.getTextTtile();
				info.icon.setAlpha(255);
				if(info.isFolder){
					textTitle.setCompoundDrawablesWithIntrinsicBounds(null, info.icon, null, null);
				}else{
					Drawable icon = Utilities.createFolderIcon(Launcher.this, info);
					textTitle.setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
				}
				bubbleTextView.setDrawFolderBg(false);
				expandFolder(getCurrentCellLayout(), bubbleTextView);
			}
		}
    }
    
    IphoneBubbleTextView mFolderTextView;
    public void postShowFolder(IphoneBubbleTextView textView){
    	this.mFolderTextView = textView;
    	mShowFolderDelay = new ShowFolderDelay(textView);
    	handler.postDelayed(mShowFolderDelay, mPostShowFolderDelay);
    }
    
    public void removeShowFolder(){
    	if(mShowFolderDelay != null){
    		handler.removeCallbacks(mShowFolderDelay);
    	}
    }

    /**
     * 2010-12-17
     */
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() { 
    	 
    	public void onReceive(Context context, Intent intent) {
    		String action = intent.getAction();
    		try {
				if ("policy.impl.PhoneWindowManager.LONG_PRESS_POWER".equals(action)) {
					mDragLayer.resetWorkspaceWhenLongClickPowerKey();
				}
			} catch (Exception e) {
				Log.e("zsc","Launcher.mBroadcastReceiver.onReceive--LONG_PRESS_POWER Exception!!!");
				e.printStackTrace();
			}
    	    if (Intent.ACTION_SCREEN_OFF.equals(action)) {
    	    	
				LauncherValues launcherValues = LauncherValues.getInstance();
				if(launcherValues.isAnim()){
					launcherValues.setAnim(false, null);
					cleanCellLayout();
				}
				if(launcherValues.isHasFolderOpen()){
					closeFolder(mOpenFolderCellLayout, mFolderTarget);
				}
				mDragLayer.setDragging(false);
			    mDragLayer.setShouldDrop(false);
			    mWorkspace.maybeSreenOffSnap();
			}  
			else if (Intent.ACTION_TIME_CHANGED.equals(action)) {
				if (DEBUG) Log.d(TAG,"mBroadcastReceiver ACTION_TIME_CHANGED");
//				refreshIconInf();
			}
    	}
    };
	
  
	protected void onCreate(Bundle savedInstanceState) {
		if (LOGD)
			Log.d(TAG, "Launcher onCreate!");

		super.onCreate(savedInstanceState);

		sModel.setContext(this);
		LauncherValues.getInstance().setLauncher(this);

		mInflater = getLayoutInflater();

		// setupConfiguration();

		
		setContentView(R.layout.launcher);
		setupViews();// 设置Home上元素的一些属性，监听事件等

		Intent intent = getIntent();
		if(intent.getBooleanExtra("shortcut", false)){
			MAIN_MENU = false;
		}else{
			MAIN_MENU = true;
		}
		
		registerIntentReceivers();// 当应用程序添加，删除，更改，背景图片改更时触发。
		registerContentObservers();
		checkForLocaleChange();
		mSavedState = savedInstanceState;
		restoreState(mSavedState);// 保存当前activity的一些信息

		if (LOGD)
			Log.d(TAG, "onCreate mRestoring=" + mRestoring);

		if (!mRestoring) {
			startLoaders();// 装载屏幕上的元素。
		}

		// For handling default keys
		mDefaultKeySsb = new SpannableStringBuilder();
		Selection.setSelection(mDefaultKeySsb, 0);
		instance = this;
		// MissedContentObserver.registerSelf(this);
		if (FeatureOption.MTK_LAUNCHER_UNREAD_SUPPORT) {
			mUnreadLoader = new MTKUnreadLoader(getApplicationContext());
			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.MTK_ACTION_UNREAD_CHANGED);
			registerReceiver(mUnreadLoader, filter);
			mUnreadLoader.initialize(this);
		}

	} 

    private static class LocaleConfiguration {
        public String locale;
        public int mcc = -1;
        public int mnc = -1;
    }
    
    private static void readConfiguration(Context context, LocaleConfiguration configuration) {
        DataInputStream in = null;
        try {
            in = new DataInputStream(context.openFileInput(PREFERENCES));
            configuration.locale = in.readUTF();
            configuration.mcc = in.readInt();
            configuration.mnc = in.readInt();
        } catch (FileNotFoundException e) {
            // Ignore
        } catch (IOException e) {
            // Ignore
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }
    
    private void checkForLocaleChange() {
        final LocaleConfiguration localeConfiguration = new LocaleConfiguration();
        readConfiguration(this, localeConfiguration);
        
        final Configuration configuration = getResources().getConfiguration();

        final String previousLocale = localeConfiguration.locale;
        final String locale = configuration.locale.toString();

        final int previousMcc = localeConfiguration.mcc;
        final int mcc = configuration.mcc;

        final int previousMnc = localeConfiguration.mnc;
        final int mnc = configuration.mnc;

        mLocaleChanged = !locale.equals(previousLocale) || mcc != previousMcc || mnc != previousMnc;

        if (mLocaleChanged) {
            localeConfiguration.locale = locale;
            localeConfiguration.mcc = mcc;
            localeConfiguration.mnc = mnc;

            writeConfiguration(this, localeConfiguration);
        }
    }
    private static void writeConfiguration(Context context, LocaleConfiguration configuration) {
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(context.openFileOutput(PREFERENCES, MODE_PRIVATE));
            out.writeUTF(configuration.locale);
            out.writeInt(configuration.mcc);
            out.writeInt(configuration.mnc);
            out.flush();
        } catch (FileNotFoundException e) {
            // Ignore
        } catch (IOException e) {
            //noinspection ResultOfMethodCallIgnored
            context.getFileStreamPath(PREFERENCES).delete();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }
    
	public void mayReceiveNotificationLauncher() {
    	if (LOGD)
    		Log.d(LOG_TAG,"mayReceiveNotificationLauncher");
		Intent intent = new Intent("com.hskj.intent.ACTION_MAY_RECEIVE_NOTIFICATION_LAUNCHER");
		sendBroadcast(intent);
    }  
    
    static int getScreenCount(){
    	return mScreenCount;
    }
    
    static void setScreenCount(int screenCount){
    	mScreenCount = screenCount;
    }
    
    static int getScreen() {
        synchronized (sLock) {
            return sScreen;
        }
    }

    static void setScreen(int screen) {
    	Log.d("ningyaoyun", "setScreen screen=" + screen);
        synchronized (sLock) {
            sScreen = screen;
        }
    }
    /**
     * 装载界面上的元素
     */
    private void startLoaders() {
    	if(DEBUG) Log.d(TAG, "mLocaleChanged = " + mLocaleChanged);
    	mScreenCount = DEFAULT_SCREN_COUNT;
    	
        sModel.loadUserItems(!mLocaleChanged, this, mLocaleChanged, false);
        mRestoring = false;
    }
 
 

     
    protected void onResume() {
    	if (LOGD) Log.d(TAG,"Launcher onResume!");
         
        super.onResume();
        if (LOGD) Log.d(TAG,"mRestoring="  + mRestoring);
        if (mRestoring) {
            startLoaders();
        } 
        LauncherValues.SCENE_BOOKCASE_TEXTVIEW = false;
        /*
        // If this was a new intent (i.e., the mIsNewIntent flag got set to true by
        // onNewIntent), then close the search dialog if needed, because it probably
        // came from the user pressing 'home' (rather than, for example, pressing 'back').
        if (mIsNewIntent) {
            // Post to a handler so that this happens after the search dialog tries to open
            // itself again.
            mWorkspace.post(new Runnable() {
                public void run() {
                    ISearchManager searchManagerService = ISearchManager.Stub.asInterface(
                            ServiceManager.getService(Context.SEARCH_SERVICE));
                    try {
                        searchManagerService.stopSearch();
                    } catch (RemoteException e) {
                        e(LOG_TAG, "error stopping search", e);
                    }    
                }
            });
        }
        */
    }

    protected void onPause() {
    	if (LOGD)Log.d(TAG,"Launcher onPause 前!");
    	  
        super.onPause();
//        if (LauncherSettings.Favorites.ON_FOLDER_TARGET) {
//        	Log.d("zsc", "Launcher.onPause()--ON_FOLDER_TARGET");
//        	removeShowFolder();
//        	if (null != mFolderTextView) {
//        		mShowFolderDelay = new ShowFolderDelay(mFolderTextView);
//        		Log.d("zsc", "Launcher.onPause()--post(mShowFolderDelay)");
//        		handler.post(mShowFolderDelay);
//        	}
//		}
        if (LOGD)Log.d(TAG,"Launcher onPause 后!");
    }

    /**
     * Overriden to track relevant focus changes.
     *
     * If a key is down and some time later the focus changes, we may
     * NOT recieve the keyup event; logically the keyup event has not
     * occured in this window.  This issue is fixed by treating a focus
     * changed event as an interruption to the keydown, making sure
     * that any code that needs to be run in onKeyUp is ALSO run here.
     *
     * Note, this focus change event happens AFTER the in-call menu is
     * displayed, so mIsMenuDisplayed should always be correct by the
     * time this method is called in the framework, please see:
     * {@link onCreatePanelView}, {@link onOptionsMenuClosed}
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        // the dtmf tones should no longer be played

    }	

    public Object onRetainNonConfigurationInstance() {
        // Flag any binder to stop early before switching
        if (mBinder != null) {
            mBinder.mTerminate = true;
        }

        if (PROFILE_ROTATE) {
            android.os.Debug.startMethodTracing("/sdcard/launcher-rotate");
        }
        return null;
    }

    private boolean acceptFilter() {
        final InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        return !inputManager.isFullscreenMode();
    }
    
    public void cleanCellLayout(){
    	int deleteCount = mWorkspace.deleteCellLayout();
    	if (LOGD) Log.d("page", "deleteCount = " + deleteCount);
    	if(deleteCount > 0){
    		mPageGuide.deletePage(deleteCount); 
        	mWorkspace.requestLayout();
    	}
    }
 
	public boolean onKeyDown(int keyCode, KeyEvent event) {

    	if (LOGD) Log.d(LOG_TAG,"onKeyDown!");
    	
        boolean handled = super.onKeyDown(keyCode, event);
        if (!handled && acceptFilter() && keyCode != KeyEvent.KEYCODE_ENTER) {
            boolean gotKey = TextKeyListener.getInstance().onKeyDown(mWorkspace, mDefaultKeySsb,
                    keyCode, event);
            if (gotKey && mDefaultKeySsb != null && mDefaultKeySsb.length() > 0) {
                // something usable has been typed - start a search
                // the typed text will be retrieved and cleared by
                // showSearchDialog()
                // If there are multiple keystrokes before the search dialog takes focus,
                // onSearchRequested() will be called for every keystroke,
                // but it is idempotent, so it's fine.
                return onSearchRequested();
            }
        } 
        
        return handled;
    }

    private String getTypedText() {
        return mDefaultKeySsb.toString();
    }

    private void clearTypedText() {
        mDefaultKeySsb.clear();
        mDefaultKeySsb.clearSpans();
        Selection.setSelection(mDefaultKeySsb, 0);
    }

    /**
     * Restores the previous state, if it exists.
     *
     * @param savedState The previous state.
     */
    private void restoreState(Bundle savedState) {
    	if(DEBUG) Log.d("test", "savedState == null");
        if (savedState == null) {
            return;
        }

        final int currentScreen = savedState.getInt(RUNTIME_STATE_CURRENT_SCREEN, -1);
        if(DEBUG) Log.d("test", "currentScreen = " + currentScreen);
        if (currentScreen > -1) {
            mWorkspace.setCurrentScreen(currentScreen);
        }

        final int addScreen = savedState.getInt(RUNTIME_STATE_PENDING_ADD_SCREEN, -1);
        if(DEBUG) Log.d("test", "addScreen = " + addScreen);
        if (addScreen > -1) {
            mAddItemCellInfo = new CellLayout.CellInfo();
            final CellLayout.CellInfo addItemCellInfo = mAddItemCellInfo;
            addItemCellInfo.valid = true;
            addItemCellInfo.screen = addScreen;
            addItemCellInfo.cellX = savedState.getInt(RUNTIME_STATE_PENDING_ADD_CELL_X);
            addItemCellInfo.cellY = savedState.getInt(RUNTIME_STATE_PENDING_ADD_CELL_Y);
            addItemCellInfo.spanX = savedState.getInt(RUNTIME_STATE_PENDING_ADD_SPAN_X);
            addItemCellInfo.spanY = savedState.getInt(RUNTIME_STATE_PENDING_ADD_SPAN_Y);
            addItemCellInfo.findVacantCellsFromOccupied(
                    savedState.getBooleanArray(RUNTIME_STATE_PENDING_ADD_OCCUPIED_CELLS),
                    savedState.getInt(RUNTIME_STATE_PENDING_ADD_COUNT_X),
                    savedState.getInt(RUNTIME_STATE_PENDING_ADD_COUNT_Y));
            mRestoring = true;
        }

    }
    
    /**
     * Finds all the views we need and configure them properly.
     */
    private void setupViews() {
    	
    	resources = getResources();
    	mPM = getPackageManager();
        mDragLayer = (DragLayer) findViewById(R.id.drag_layer);
        final DragLayer dragLayer = mDragLayer;

        mWorkspace = (Workspace) dragLayer.findViewById(R.id.workspace);
        final Workspace workspace = mWorkspace; 
         
        workspace.setOnLongClickListener(this);
        workspace.setDragger(dragLayer);
        mPageGuide = (IphonePageGuide)findViewById(R.id.dianImageView);
        mPageGuide.setWorkspace(mWorkspace);
        
        LinearLayout.LayoutParams lp = (LayoutParams) mPageGuide.getLayoutParams();
        lp.setMargins(0, 0, 0, 30); 
        mPageGuide.setLayoutParams(lp);
//        mPageGuide.setPadding(0, 0, 0, 30);
        
        mDoneBt = (Button) findViewById(R.id.button_done);
        mDoneBt.setVisibility(View.GONE);
        mDoneBt.setOnClickListener(this);
        
        dragLayer.setDragScoller(workspace); 
    }
    
    private Button mDoneBt;
    
    public void updateNotificationCount(String className, int count) {
//    	if (!mTransparentPanel.updateNotificationCount(className, count)) {
    		mWorkspace.updateNotificationCount(className, count);
//    	}
    }
    
    /**
     * Creates a view representing a shortcut.
     *
     * @param info The data structure describing the shortcut.
     *
     * @return A View inflated from R.layout.application.
     */
    View createShortcut(ApplicationInfo info) {
        return createIphoneShortcut(R.layout.iphone_application,(ViewGroup) mWorkspace.getChildAt(mWorkspace.getCurrentScreen()), info);
    }
    
    View createShortcut(ViewGroup parent,ApplicationInfo info) {
    	return createIphoneShortcut(R.layout.iphone_application, parent, info);
    }

    View createFolderShortcut(ViewGroup parent,ApplicationInfo info) {
    	return createFolderShortcut(R.layout.iphone_application, parent, info);
    }
    /**
     * Creates a view representing a shortcut inflated from the specified resource.
     *
     * @param layoutResId The id of the XML layout used to create the shortcut.
     * @param parent The group the shortcut belongs to.
     * @param info The data structure describing the shortcut.
     *
     * @return A View inflated from layoutResId.
     */
    View createShortcut(int layoutResId, ViewGroup parent, ApplicationInfo info) {
        TextView favorite = (TextView) mInflater.inflate(layoutResId, parent, false);

        if (!info.filtered) {
            info.icon = Utilities.createIconThumbnail(info.icon, this);
            info.filtered = true;
        }

        favorite.setCompoundDrawablesWithIntrinsicBounds(null, info.icon, null, null);
        favorite.setText(info.title);
        favorite.setTag(info);
        favorite.setOnClickListener(this);

        return favorite;
    }
    
    View createIphoneShortcut(int layoutResId, ViewGroup parent, ApplicationInfo info) {
    	
    	IphoneBubbleTextView iphoneView;
    	 
		iphoneView = (IphoneBubbleTextView)mInflater.inflate(layoutResId, parent, false); 
        	
    	if(info instanceof FolderInfo){
        	FolderInfo fi = (FolderInfo)info;
        	Drawable icon = Utilities.createFolderIcon(this, fi.items, Utilities.GRAY_BG);
        	info.icon = icon;
        	info.grayIcon = Utilities.convertGrayImg(info.icon);
            info.filtered = true;
        }
        
        if (!info.filtered) {
        	if (info.isUninstall) {
        		info.icon = Utilities.mergeDrawble(this, info.icon);
        	}
            info.grayIcon = Utilities.convertGrayImg(info.icon);
            info.smallIcon = Utilities.createSmallIcon(info.icon,this);
            info.filtered = true;
        }
        
        info.iphoneBubbleTextView = iphoneView;
        
        TextView favorite = (TextView) ((ViewGroup)iphoneView.getChildAt(0)).getChildAt(0);
        favorite.setCompoundDrawablesWithIntrinsicBounds(null, info.icon, null, null);
    	favorite.setCompoundDrawablePadding(10); 
        favorite.setText(info.title);    
//        favorite.setShadowLayer(1.0f, 0f, 2f, 0xFF000000); 
        favorite.setTextSize(13);
        iphoneView.setTag(info);
        iphoneView.setOnClickListener(this);
        iphoneView.updateNotificationCount(info.launcherCount);
        if (LauncherValues.getInstance().isAnim()) {
        	iphoneView.startRotateAnimation();
        }
        
        return iphoneView;
    } 

    View createFolderShortcut(int layoutResId, ViewGroup parent, ApplicationInfo info) {
    	IphoneBubbleTextView iphoneView;
		iphoneView = (IphoneBubbleTextView)mInflater.inflate(layoutResId, parent, false); 
    	if(info instanceof FolderInfo){
        	FolderInfo fi = (FolderInfo)info;
        	Drawable icon = Utilities.createFolderIcon(this, fi.items, Utilities.GRAY_BG);
        	info.icon = icon;
        	info.grayIcon = Utilities.convertGrayImg(info.icon);
            info.filtered = true;
        }
        if (!info.filtered) {
        	if (info.isUninstall) {
        		info.icon = Utilities.mergeDrawble(this, info.icon);
        	}
            info.grayIcon = Utilities.convertGrayImg(info.icon);
            info.smallIcon = Utilities.createSmallIcon(info.icon,this);
            info.filtered = true;
        }
        info.iphoneBubbleTextView = iphoneView;
        TextView favorite = (TextView) ((ViewGroup)iphoneView.getChildAt(0)).getChildAt(0);
        favorite.setCompoundDrawablesWithIntrinsicBounds(null, info.icon, null, null);
    	favorite.setCompoundDrawablePadding(10); 
        favorite.setText(info.title);     
        favorite.setTextColor(0xffffffff);
        favorite.setTextSize(13);
        iphoneView.setTag(info);
        iphoneView.setOnClickListener(this);
        iphoneView.updateNotificationCount(info.launcherCount);
        if (LauncherValues.getInstance().isAnim()) {
        	iphoneView.startRotateAnimation();
        }
        return iphoneView;
    }
    static ApplicationInfo addShortcut(Context context, Intent data,
            CellLayout.CellInfo cellInfo, boolean notify) {

        final ApplicationInfo info = infoFromShortcutIntent(context, data);
        LauncherModel.addItemToDatabase(context, info, LauncherSettings.Favorites.CONTAINER_DESKTOP,
                cellInfo.screen, cellInfo.cellX, cellInfo.cellY, notify);

        return info;
    }

    private static ApplicationInfo infoFromShortcutIntent(Context context, Intent data) {
        Intent intent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        String name = data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        Bitmap bitmap = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);

        Drawable icon = null;
        boolean filtered = false;
        boolean customIcon = false;
        ShortcutIconResource iconResource = null;

        if (bitmap != null) {
            icon = new FastBitmapDrawable(Utilities.createBitmapThumbnail(bitmap, context));
            filtered = true;
            customIcon = true;
        } else {
            Parcelable extra = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
            if (extra != null && extra instanceof ShortcutIconResource) {
                try {
                    iconResource = (ShortcutIconResource) extra;
                    final PackageManager packageManager = context.getPackageManager();
                    Resources resources = packageManager.getResourcesForApplication(
                            iconResource.packageName);
                    final int id = resources.getIdentifier(iconResource.resourceName, null, null);
                    icon = resources.getDrawable(id);
                } catch (Exception e) {
                    Log.w(LOG_TAG, "Could not load shortcut icon: " + extra);
                }
            }
        }

        if (icon == null) {
            icon = context.getPackageManager().getDefaultActivityIcon();
        }

        final ApplicationInfo info = new ApplicationInfo();
        info.icon = icon;
        info.filtered = filtered;
        info.title = name;
        info.intent = intent;
        info.customIcon = customIcon;
        info.iconResource = iconResource;

        return info;
    }

    void closeSystemDialogs() {
        getWindow().closeAllPanels();
        
        try {
//            dismissDialog(DIALOG_CREATE_SHORTCUT);
            dismissDialog(DIALOG_RENAME_FOLDER);
            mWorkspace.unlock();
        } catch (Exception e) {
        }
    }
    
    protected void onNewIntent(Intent intent) {
    	if(mDragLayer.isDragging()){
        	if(DEBUG) Log.d(TAG, "onNewIntent isDragging = " + mDragLayer.isDragging());
        	return;
        }
    	
        super.onNewIntent(intent);

        // Close the menu
        if (Intent.ACTION_MAIN.equals(intent.getAction())) {
            closeSystemDialogs();
            
            // Set this flag so that onResume knows to close the search dialog if it's open,
            // because this was a new intent (thus a press of 'home' or some such) rather than
            // for example onResume being called when the user pressed the 'back' button.
//            mIsNewIntent = true;
            if ((intent.getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) {
				
				if(LauncherValues.getInstance().isHasFolderOpen()){
		    		if (LauncherValues.getInstance().isAnim()) {
		    			LauncherValues.getInstance().setAnim(false, null);
		        		LauncherValues.TRY_TO_CREATE_FOLDER = false;
		        		
		        		FolderLinearLayout folderLinearLayout = getExpendFolderView().getBody();
		        		folderLinearLayout.hideTitleEditText();
		        		return;
		    		}else{
		    			closeFolder(getCurrentCellLayout(), getFolderTarget());
		    			cleanCellLayout();
		    			return;
		    		}
		    	}else{
		    		if (LauncherValues.getInstance().isAnim()) {
		    			LauncherValues.getInstance().setAnim(false, null);
		        		cleanCellLayout();
		        		return;
		    		}
		    	}
			
		        if (!mWorkspace.isDefaultScreenShowing()) {
		            mWorkspace.moveToDefaultScreen();
		        }
	
		        final View v = getWindow().peekDecorView();
		        if (v != null && v.getWindowToken() != null) {
		            InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
		            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		        }
			}
        }
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
    	if (LOGD)
    		Log.d(TAG,"Launcher onRestoreInstanceState!");

        // NOTE: Do NOT do this. Ever. This is a terrible and horrifying hack.
        //
        // Home loads the content of the workspace on a background thread. This means that
        // a previously focused view will be, after orientation change, added to the view
        // hierarchy at an undeterminate time in the future. If we were to invoke
        // super.onRestoreInstanceState() here, the focus restoration would fail because the
        // view to focus does not exist yet.
        //
        // However, not invoking super.onRestoreInstanceState() is equally bad. In such a case,
        // panels would not be restored properly. For instance, if the menu is open then the
        // user changes the orientation, the menu would not be opened in the new orientation.
        //
        // To solve both issues Home messes up with the internal state of the bundle to remove
        // the properties it does not want to see restored at this moment. After invoking
        // super.onRestoreInstanceState(), it removes the panels state.
        //
        // Later, when the workspace is done loading, Home calls super.onRestoreInstanceState()
        // again to restore focus and other view properties. It will not, however, restore
        // the panels since at this point the panels' state has been removed from the bundle.
        //
        // This is a bad example, do not do this.
        //
        // If you are curious on how this code was put together, take a look at the following
        // in Android's source code:
        // - Activity.onRestoreInstanceState()
        // - PhoneWindow.restoreHierarchyState()
        // - PhoneWindow.DecorView.onAttachedToWindow()
        //
        // The source code of these various methods shows what states should be kept to
        // achieve what we want here.

        Bundle windowState = savedInstanceState.getBundle("android:viewHierarchyState");
        SparseArray<Parcelable> savedStates = null;
        int focusedViewId = View.NO_ID;

        if (windowState != null) {
            savedStates = windowState.getSparseParcelableArray("android:views");
            windowState.remove("android:views");
            focusedViewId = windowState.getInt("android:focusedViewId", View.NO_ID);
            windowState.remove("android:focusedViewId");
        }

        super.onRestoreInstanceState(savedInstanceState);

        if (windowState != null) {
            windowState.putSparseParcelableArray("android:views", savedStates);
            windowState.putInt("android:focusedViewId", focusedViewId);
            windowState.remove("android:Panels");
        }

        mSavedInstanceState = savedInstanceState;
    }

    protected void onSaveInstanceState(Bundle outState) {
    	if (LOGD)
    		Log.d(TAG,"Launcher onSaveInstanceState!");

        super.onSaveInstanceState(outState);
  
        if (mAddItemCellInfo != null && mAddItemCellInfo.valid && mWaitingForResult) {
            final CellLayout.CellInfo addItemCellInfo = mAddItemCellInfo;
            final CellLayout layout = (CellLayout) mWorkspace.getChildAt(addItemCellInfo.screen);

            outState.putInt(RUNTIME_STATE_PENDING_ADD_SCREEN, addItemCellInfo.screen);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_CELL_X, addItemCellInfo.cellX);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_CELL_Y, addItemCellInfo.cellY);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_SPAN_X, addItemCellInfo.spanX);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_SPAN_Y, addItemCellInfo.spanY);
            outState.putInt(RUNTIME_STATE_PENDING_ADD_COUNT_X, layout.getCountX());
            outState.putInt(RUNTIME_STATE_PENDING_ADD_COUNT_Y, layout.getCountY());
            outState.putBooleanArray(RUNTIME_STATE_PENDING_ADD_OCCUPIED_CELLS,
                   layout.getOccupiedCells());
        }

    }

    public void onDestroy() {
    	if (LOGD) Log.d(TAG,"Launcher onDestroy!");
    	
    	mScreenCount = DEFAULT_SCREN_COUNT;
    	sScreen = DEFAULT_SCREN;
        mDestroyed = true;
 
        super.onDestroy();

        TextKeyListener.getInstance().release();

        try {
			if(LauncherValues.getInstance().isHasFolderOpen()){
				Log.d("zsc", "Launcher.onDestroy()--closeFolder");
				Launcher.getInstance().closeFolder(mOpenFolderCellLayout, mFolderTarget);
			}
		} catch (Exception e) {
			Log.e("zsc", "Launcher.onDestroy()--closeFolder--Exception!!!");
			e.printStackTrace();
		}
        sModel.unbind();
        sModel.abortLoaders();//停止loader
        cleanCellLayout();
        mPageGuide.requestLayout();

        getContentResolver().unregisterContentObserver(mObserver);
        unregisterReceiver(mApplicationsReceiver);
        unregisterReceiver(mCloseSystemDialogsReceiver);
        unregisterReceiver(mUpdateAppReceiver);
        //2010-12-17
        unregisterReceiver(mBroadcastReceiver); 
        if (FeatureOption.MTK_LAUNCHER_UNREAD_SUPPORT) {
            unregisterReceiver(mUnreadLoader);
        }
    }

     
    public void startActivityForResult(Intent intent, int requestCode) {
        if (requestCode >= 0) mWaitingForResult = true;
        super.startActivityForResult(intent, requestCode);
    }

     
	protected void onRestart() {
    	if (LOGD) Log.d(TAG,"Launcher onRestart!");
    	super.onRestart();
	}

	 
	protected void onStart() {
    	if (LOGD)
    		Log.d(TAG,"Launcher onStart!");
		super.onStart();
	}
	 
	protected void onStop() {
    	if (LOGD)
    		Log.d(TAG,"Launcher onStop!");
		super.onStop();
	}


	public boolean MAIN_MENU = true;
    @Override
	public boolean onCreateOptionsMenu(Menu menu) { 
		menu.add(0, 1, 1, getResources().getString(R.string.go_shortcuts_option)); 
		menu.add(0, 2, 1, getResources().getString(R.string.go_main_menu)); 
		return true;
	}

    
    
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if(MAIN_MENU){
			menu.getItem(0).setVisible(true);
			menu.getItem(1).setVisible(false);
		}else{
			menu.getItem(0).setVisible(false);
			menu.getItem(1).setVisible(true);
		}
		
		return true;
	}

	private int bookCounts;
	@Override
	public boolean onOptionsItemSelected(MenuItem item) { 
		if(MAIN_MENU){
			MAIN_MENU = false;
			LinearLayout.LayoutParams lp = (LayoutParams) mPageGuide.getLayoutParams();
	        lp.setMargins(0, 0, 0, 0); 
	        mPageGuide.setLayoutParams(lp);
			mDoneBt.setVisibility(View.VISIBLE);
			bookCounts = BookCaseModel.getBookCounts();
			mDoneBt.setText(getResources().getString(R.string.finish) + "(" + bookCounts + "/" + BookCaseModel.mBookmap.size() + ")");
		}else{
//	        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 
//	        		LinearLayout.LayoutParams.WRAP_CONTENT, 1);
			LinearLayout.LayoutParams lp = (LayoutParams) mPageGuide.getLayoutParams();
	        lp.setMargins(0, 0, 0, 30); 
	        mPageGuide.setLayoutParams(lp);
			mDoneBt.setVisibility(View.GONE);
			MAIN_MENU = true;
		}
		if(LauncherValues.getInstance().isAnim()){
    		LauncherValues.getInstance().setAnim(false, null);
    		Launcher.getInstance().cleanCellLayout();
    	}else if(LauncherValues.getInstance().IGNORE_TRANSPARENTPANEL_TARGET){
    		Launcher.getInstance().closeFolderForPress();
    	}
		int count = mWorkspace.getChildCount();
		for (int i = 0; i < count; i++) {
			CellLayout cl = (CellLayout)mWorkspace.getChildAt(i);
			int childCount = cl.getChildCount();
			for (int j = 0; j < childCount; j++) {
				View child = cl.getChildAt(j);
				if (child instanceof IphoneBubbleTextView) {
					((IphoneBubbleTextView)child).refrashIconState();
				}
			}
		} 
		return true;
	}
    /**
     * Indicates that we want global search for this activity by setting the globalSearch
     * argument for {@link #startSearch} to true.
     */
     
    public boolean onSearchRequested() {
        startSearch(null, false, null, true);
        return true;
    }

    public void removeShortcutsForPackage(String packageName) {
        if (packageName != null && packageName.length() > 0) {
            mWorkspace.removeShortcutsForPackage(packageName);
//            mTransparentPanel.removeShortcutsForPackage(packageName);
        }
    }

    private void updateShortcutsForPackage(String packageName) {
        if (packageName != null && packageName.length() > 0) {
            mWorkspace.updateShortcutsForPackage(packageName);
//            mTransparentPanel.updateShortcutsForPackage(packageName);
        }
    }



    void processShortcut(Intent intent, int requestCodeApplication, int requestCodeShortcut) {
        // Handle case where user selected "Applications"
        String applicationName = getResources().getString(R.string.group_applications);
        String shortcutName = intent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);

        if (applicationName != null && applicationName.equals(shortcutName)) {
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
            pickIntent.putExtra(Intent.EXTRA_INTENT, mainIntent);
            startActivityForResult(pickIntent, requestCodeApplication);
        } else {
            startActivityForResult(intent, requestCodeShortcut);
        }
    }


    private boolean findSlot(CellLayout.CellInfo cellInfo, int[] xy, int spanX, int spanY) {
        if (!cellInfo.findCellForSpan(xy, spanX, spanY)) {
            boolean[] occupied = mSavedState != null ?
                    mSavedState.getBooleanArray(RUNTIME_STATE_PENDING_ADD_OCCUPIED_CELLS) : null;
            cellInfo = mWorkspace.findAllVacantCells(occupied);
            if (!cellInfo.findCellForSpan(xy, spanX, spanY)) {
                Toast.makeText(this, getString(R.string.out_of_space), Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    private void startWallpaper() {
        final Intent pickWallpaper = new Intent(Intent.ACTION_SET_WALLPAPER);
        Intent chooser = Intent.createChooser(pickWallpaper,
                getText(R.string.chooser_wallpaper));
        WallpaperManager wm = (WallpaperManager)
                getSystemService(Context.WALLPAPER_SERVICE);
        WallpaperInfo wi = wm.getWallpaperInfo();
        if (wi != null && wi.getSettingsActivity() != null) {
            LabeledIntent li = new LabeledIntent(getPackageName(),
                    R.string.configure_wallpaper, 0);
            li.setClassName(wi.getPackageName(), wi.getSettingsActivity());
            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { li });
        }
        startActivity(chooser);
    }

    /**
     * Registers various intent receivers. The current implementation registers
     * only a wallpaper intent receiver to let other applications change the
     * wallpaper.
     */
    private void registerIntentReceivers() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        registerReceiver(mApplicationsReceiver, filter);
        
        filter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mCloseSystemDialogsReceiver, filter);
        
        filter = new IntentFilter("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE");
        registerReceiver(mUpdateAppReceiver, filter);
        
        //2010-12-17
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED); 
        intentFilter.addAction("policy.impl.PhoneWindowManager.LONG_PRESS_POWER");
	    registerReceiver(mBroadcastReceiver, intentFilter);
    }

    /**
     * Registers various content observers. The current implementation registers
     * only a favorites observer to keep track of the favorites applications.
     */
    private void registerContentObservers() {
        ContentResolver resolver = getContentResolver();
        resolver.registerContentObserver(LauncherSettings.Favorites.CONTENT_URI, true, mObserver);
    }
    
     
	boolean isLauncherKeyDown = false;
	Object objKeyDown = new Object();
	void setIsLauncherKeyDownTrue(){
		synchronized (objKeyDown) {
			isLauncherKeyDown = true;
		}
	}
	void setIsLauncherKeyDownFalse(){
		synchronized (objKeyDown) {
			isLauncherKeyDown = false;
		}
	}
    	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		Log.d("ningyaoyun", "dispatchKeyEvent  abortLoaders"); 
		sModel.abortLoaders(); 
		mDestroyed = true; 
		Log.e("test3", "onBackPressed");
		super.onBackPressed();
	}
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
        	setIsLauncherKeyDownTrue();
         //   switch (event.getKeyCode()) {
          //      case KeyEvent.KEYCODE_BACK:
          //          return true;  
          //  }
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_BACK: 
                	Log.d("ningyaoyun", "dispatchKeyEvent  event.isCanceled()=" + event.isCanceled()); 
                    if (!event.isCanceled()) {
                        mWorkspace.dispatchKeyEvent(event);
                    }
                    if (mDragLayer.isDragging()) {
                    	Log.d("zsc", "Launcher.dispatchKeyEvent()--retrun true--isDragging!!!");
					          	return true;
					} 
                    if (false == isLauncherKeyDown) {
                    	Log.d("ningyaoyun", "dispatchKeyEvent  false == isLauncherKeyDown"); 
            		//	sModel.abortLoaders(); 
            		//	mDestroyed = true; 
                	//	this.finish();
			//			return true;
                       	return super.dispatchKeyEvent(event);
                	} 
                    setIsLauncherKeyDownFalse();
                    if(LauncherValues.getInstance().isAnim()){
                		LauncherValues.getInstance().setAnim(false, null);
                		Launcher.getInstance().cleanCellLayout();
                	}else if(LauncherValues.getInstance().IGNORE_TRANSPARENTPANEL_TARGET){
                		Launcher.getInstance().closeFolderForPress();
                	}else{ 
					if (!sModel.isLoaderThreadRunning()) { 
				        if(!MAIN_MENU){
							doBackForBookCase();
						}
						curChoiseAddInfos.clear();
						curChoiseDeleteInfos.clear();
						
						if (MainActivity.instance != null
								&& MainActivity.instance.mBookModel != null) {
							MainActivity.instance.mBookModel.refrashData();
						}

						Log.d("ningyaoyun", "dispatchKeyEvent  abortLoaders");
            		//	sModel.abortLoaders(); 
            		//	mDestroyed = true; 
                	//	this.finish();
                         	return super.dispatchKeyEvent(event);
					} 
                	}
                    return true; 
            }
        }

        return super.dispatchKeyEvent(event);
    }

    /**
     * When the notification that favorites have changed is received, requests
     * a favorites list refresh.
     */
    private void onFavoritesChanged() {
    	if(DEBUG) Log.d("test_flag", "onFavoritesChanged");
        mDesktopLocked = true;
        sModel.loadUserItems(false, this, false, false);
    }
     
    
    void onDesktopItemsLoaded(ArrayList<ItemInfo> shortcuts) {
        if (mDestroyed) {
            if (LauncherModel.DEBUG_LOADERS) {
                Log.d(LauncherModel.LOG_TAG, "  ------> destroyed, ignoring desktop items");
            }
            this.finish();
            return;
        }
        if(LauncherModel.DEBUG_LOADERS) Log.d(LauncherModel.LOG_TAG,"------> starting bindDesktopItems!");
        bindDesktopItems(shortcuts);
    }
    
    /**
     * Refreshes the shortcuts shown on the workspace.
     */
    private void bindDesktopItems(ArrayList<ItemInfo> shortcuts) {

        if (shortcuts == null) {
            if (LauncherModel.DEBUG_LOADERS) Log.d(LauncherModel.LOG_TAG, "  ------> a source is null");            
            return;
        }

        final Workspace workspace = mWorkspace;
        int count = workspace.getChildCount();
        for (int i = 0; i < count; i++) {
            ((ViewGroup) workspace.getChildAt(i)).removeAllViewsInLayout();//清空workspace上的所有元素
        }

        if (DEBUG_USER_INTERFACE) {
            android.widget.Button finishButton = new android.widget.Button(this);
            finishButton.setText("Finish");
            workspace.addInScreen(finishButton, 1, 0, 0, 1, 1);

            finishButton.setOnClickListener(new android.widget.Button.OnClickListener() {
                public void onClick(View v) {
                    finish();
                }
            });
        }

        // Flag any old binder to terminate early
        if (mBinder != null) {
            mBinder.mTerminate = true;
        }
        if(LauncherModel.DEBUG_LOADERS) Log.d(LauncherModel.LOG_TAG,"------> new DesktopBinder!");
        mBinder = new DesktopBinder(this, shortcuts);
        mBinder.startBindingItems();
    }
    
    /**
     * 将图标显示在workspace上
     * @param binder
     * @param shortcuts
     * @param start
     * @param count
     */
    private void bindItems(Launcher.DesktopBinder binder,
            ArrayList<ItemInfo> shortcuts, int start, int count) {

        final Workspace workspace = mWorkspace;
        final boolean desktopLocked = mDesktopLocked;

        final int end = Math.min(start + DesktopBinder.ITEMS_COUNT, count);
        int i = start;
        Log.d(TAG, "bindItems start=" + start + ";end=" + end + ";count=" + count);
        for ( ; i < end; i++) {
            final ItemInfo item = shortcuts.get(i);
            switch (item.itemType) {
            	case LauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER:
                case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
                case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                	if(mDestroyed){
                		return;
                	}
                	while(mScreenCount < item.screen + 1){
                		mWorkspace.addCellLayout();
                	}
                    final View shortcut = createShortcut((ApplicationInfo) item);
//                    Log.d(TAG, "cellX=" + item.cellX + ";item.cellY=" + item.cellY);
                    workspace.addInScreen(shortcut, item.screen, item.cellX, item.cellY, 1, 1,
                            !desktopLocked);
                    break;  
            }
        }

        workspace.requestLayout();
        
//        sModel.refrashLostPackages(this);
        if (end >= count) {
            finishBindDesktopItems();
            binder.startBindingDrawer();
        } else {
            binder.obtainMessage(DesktopBinder.MESSAGE_BIND_ITEMS, i, count).sendToTarget();
        }
    }

               
                
    
    private void finishBindDesktopItems() {
    	
        if (mSavedState != null) {
            if (!mWorkspace.hasFocus()) {
                mWorkspace.getChildAt(mWorkspace.getCurrentScreen()).requestFocus();
            }
            final boolean allApps = mSavedState.getBoolean(RUNTIME_STATE_ALL_APPS_FOLDER, false);
            mSavedState = null;
        }

        if (mSavedInstanceState != null) {
            super.onRestoreInstanceState(mSavedInstanceState);
            mSavedInstanceState = null;
        } 
        cleanCellLayout(); 
        mPageGuide.requestLayout();
        mDesktopLocked = false;
        sModel.refrashLostPackages(this);
        mUnreadLoader.loadAndInitUnreadShortcuts();
        
        if(!MAIN_MENU){
			MAIN_MENU = false;
			LinearLayout.LayoutParams lp = (LayoutParams) mPageGuide.getLayoutParams();
	        lp.setMargins(0, 0, 0, 0); 
	        mPageGuide.setLayoutParams(lp);
			mDoneBt.setVisibility(View.VISIBLE);
			bookCounts = BookCaseModel.getBookCounts();
			mDoneBt.setText(getResources().getString(R.string.finish) + "(" + bookCounts + "/" + BookCaseModel.mBookmap.size() + ")");
		} 
		int count = mWorkspace.getChildCount();
		for (int i = 0; i < count; i++) {
			CellLayout cl = (CellLayout)mWorkspace.getChildAt(i);
			int childCount = cl.getChildCount();
			for (int j = 0; j < childCount; j++) {
				View child = cl.getChildAt(j);
				if (child instanceof IphoneBubbleTextView) {
					((IphoneBubbleTextView)child).refrashIconState();
				}
			}
		} 
 
    } 
    
    private CellLayout mOpenFolderCellLayout;	//当前展开文件夹的CellLayout
    private CellLayout mFolderCellLayout;
	private IphoneBubbleTextView mFolderTarget;	//点击的文件夹
	private ExpendFolder mExpendFolderView;		//包含展开文件夹图标的View
	
	public CellLayout getOpenFolderCellLayout() {
		return mOpenFolderCellLayout;
	}
	
	public CellLayout getFolderCellLayout(){
		return mFolderCellLayout;
	}
	
	public IphoneBubbleTextView getFolderTarget() {
		return mFolderTarget;
	}
	
	public ExpendFolder getExpendFolderView() {
		return mExpendFolderView;
	}
    
	
	private void selectFolderViewBg(ExpendFolder folderView, int itemCount){
		
		if(itemCount > 0 && itemCount < 5){
			if(LauncherValues.dragIconStatu == LauncherValues.DRAG_ON_EXPEND_FOLDER){
				folderView.getBody().setBackgroundResource(R.drawable.folder_body_middle);
			}else{
				folderView.getBody().setBackgroundResource(R.drawable.folder_body_small);
			}
		}else if(itemCount > 4 && itemCount < 7){
			if(LauncherValues.dragIconStatu == LauncherValues.DRAG_ON_EXPEND_FOLDER){
				folderView.getBody().setBackgroundResource(R.drawable.folder_body_large);
			}else{
				folderView.getBody().setBackgroundResource(R.drawable.folder_body_middle);
			}
		}else {
			folderView.getBody().setBackgroundResource(R.drawable.folder_body_large);
		}
	}
    
	public void closeFolderForPress(){
		closeFolder(getCurrentCellLayout(), getFolderTarget());
	}
    public void closeFolder(CellLayout cellLayout, IphoneBubbleTextView folderTarget) {
    	
    	if(DEBUG) Log.d("IphoneDragLayer", "closeFolder");
    	LauncherValues.getInstance().setHasFolderOpen(false);
    	LauncherValues.DO_NOT_REQUEST_TRANSPARENTPANEL_LAYOUT = false;
		LauncherValues.IGNORE_IPHONE_BUBBLE_TEXTVIEW = false;
		LauncherValues.IGNORE_TRANSPARENTPANEL_TARGET = false;
		LauncherValues.TRY_TO_CREATE_FOLDER = false;
		LauncherValues.dragIconStatu = LauncherValues.DRAG_NO_STATU;
		
    	ApplicationInfo info = (ApplicationInfo)folderTarget.getTag();
    	if(info.isFolder){
    		FolderInfo folderInfo = (FolderInfo)info;
    		folderInfo.mExpend = false;
    	}
		folderTarget.getTextTtile().setText(info.title);
		cellLayout.removeView(mExpendFolderView);
		cellLayout.stopAnimation(false);
		cellLayout.hasExpendView(false);
		
		Utilities.changeItemColor(folderTarget, CHANGE_COLOR, cellLayout);
		
		try {
			bottomLayout = mDragLayer.findViewById(R.id.bottomLayout);
			bottomLayout.setVisibility(View.VISIBLE);
		} catch (Exception e) {
			Log.e("zsc", "Launcher.closeFolder()---Exception!!");
			e.printStackTrace();
		}
		
		cleanCellLayout();
		
		if (null != currowImage) {
			currowImage.setVisibility(View.GONE);
		}
		mOpenFolderCellLayout = null;
		mFolderCellLayout = null;
		mFolderTarget = null;
		mExpendFolderView = null;
		mWorkspace.reSnapToCureentPage();
	}

	/**
     * 打开文件夹
     * @param cellLayout    
     * @param folderTarget  点击的文件夹
     */
    public void expandFolder(CellLayout cellLayout, IphoneBubbleTextView folderTarget) {
		 
    	Scroller scroller = mWorkspace.getScroller();
    	if(!scroller.isFinished()){
    		return;
    	}
    	
    	LauncherValues.getInstance().setHasFolderOpen(true);
    	mOpenFolderCellLayout = cellLayout;
    	mFolderTarget = folderTarget;
		LauncherValues.IGNORE_IPHONE_BUBBLE_TEXTVIEW = true;
		LauncherValues.IGNORE_TRANSPARENTPANEL_TARGET = true;
		
    	ApplicationInfo info = (ApplicationInfo)mFolderTarget.getTag();
		CellLayout.LayoutParams layoutParams = (CellLayout.LayoutParams) mFolderTarget.getLayoutParams();
		mFolderTarget.getTextTtile().setText("");
		
		ExpendFolder folderView = new ExpendFolder(this);
		mFolderCellLayout = (CellLayout)folderView.findViewById(R.id.cell_layout);
		if(info.isFolder){
			FolderInfo folderInfo = (FolderInfo)info;
			mFolderCellLayout.addFolderItems(folderInfo.items);
			folderInfo.mExpend = true;
			final int itemCount = folderInfo.items.size();
			selectFolderViewBg(folderView, itemCount);
		}else{
			FolderInfo folderInfo = new FolderInfo(this,info);
			ApplicationInfo copyInfo = new ApplicationInfo(info);
			folderInfo.icon = Utilities.createFolderIcon(Launcher.this, info);
			folderInfo.items.add(copyInfo);
			folderInfo.itemsId.add(copyInfo.id);
			copyInfo.cellX = 0;
			copyInfo.cellY = 0;
			copyInfo.isFolderItem = true;
			copyInfo.folderInfo = folderInfo;
			copyInfo.container = LauncherSettings.Favorites.CONTAINER_FOLDER;
			mFolderTarget.setTag(folderInfo);
			
			sModel.getDesktopItem().remove(info);
			sModel.getDesktopItem().add(folderInfo);
			LauncherModel.addItemToDatabase(this, folderInfo);
			LauncherModel.updateItemInDatabase(this, copyInfo);
			
			mFolderCellLayout.addFolderItems(copyInfo, false);
			folderView.getBody().setBackgroundResource(R.drawable.folder_body_small);
			LauncherValues.TRY_TO_CREATE_FOLDER = true;
		}
		
		if(LauncherValues.getInstance().isAnim()){
			folderView.getBody().showTitleEditText();
		}
		
		mExpendFolderView = folderView;
		mOpenFolderCellLayout.hasExpendView(true);
		mOpenFolderCellLayout.stopAnimation(true);

		Utilities.changeItemColor(mFolderTarget, CHANGE_GRAY, mOpenFolderCellLayout);
		 
		currowImage = (ImageView)folderView.findViewById(R.id.top_arrow);
		folderView.setTopCurrowLeft(layoutParams.x + LauncherValues.mFolderTopCurrowLeft);
		bottomLayout = mDragLayer.findViewById(R.id.bottomLayout);
		bottomLayout.setVisibility(View.GONE);  
			
		currowImage.setVisibility(View.VISIBLE);
		mWorkspace.addInScreen(folderView, getScreen(), -1, info.cellY+1, 4, 1);
	}
    
     
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		getDragLayer().requestLayout();
	}

    private ImageView currowImage = null;
    private View bottomLayout = null;
    
    
    private List<ApplicationInfo> curChoiseAddInfos = new ArrayList<ApplicationInfo>();
    private List<ApplicationInfo> curChoiseDeleteInfos = new ArrayList<ApplicationInfo>();
    private void doBackForBookCase(){
    	for(ApplicationInfo info : curChoiseAddInfos){ 
    		info.isBookCase = !info.isBookCase;
    	}
    	for(ApplicationInfo info : curChoiseDeleteInfos){ 
    		info.isBookCase = !info.isBookCase;
    	}
    }
    private void updateBookCase(){ 
    	ApplicationInfo bookInfo = null;
    	List<ApplicationInfo> deleteBookInfos = new ArrayList<ApplicationInfo>();
    	List<ApplicationInfo> updateInfos = new ArrayList<ApplicationInfo>();
    	for(ApplicationInfo info : curChoiseDeleteInfos){ 
    		updateInfos.add(info);  
			Set<Integer> mBookKeys = BookCaseModel.mBookmap.keySet(); 
    		int bookX = -1;
    		int bookY = -1;
    		
            for(Integer i: mBookKeys){ 
            	bookInfo = BookCaseModel.mBookmap.get(i);
            	if(bookInfo != null){
            		String bookIntentString = bookInfo.intent.toUri(0);
            		String infoIntentString = info.intent.toUri(0);
            		if(bookIntentString.equalsIgnoreCase(infoIntentString)){
            			bookX = i/10;
	                	bookY = i%10;
	                	deleteBookInfos.add(bookInfo);
            		}
            	}
            }	
            if(bookX == -1 || bookY == -1){
            	throw new IllegalStateException("ningyaoyun =====>  choice shortcuts wrong ");
            } 
            BookCaseModel.mBookmap.put(bookX*10 + bookY,null);
			BookCaseModel.mOccupied[bookX][bookY] = false;  
    	}
    	BookCaseModel.DeleteBookItemByBatch(this, deleteBookInfos, LauncherSettings.BookCase.CONTENT_URI);
    	
		for (ApplicationInfo info : curChoiseAddInfos) {
			updateInfos.add(info);
			int key = MainActivity.instance.mBookModel.findVacantCell();
			if (key == -1) {
				Toast.makeText(
						this,
						getResources().getString(
								R.string.can_add_shortcut_less_than_eight),
						Toast.LENGTH_SHORT).show();
				return;
			}
			ApplicationInfo addbookItem = new ApplicationInfo();
			addbookItem.cellX = key / 10;
			addbookItem.cellY = key % 10;
			addbookItem.pkgName = info.pkgName;
			addbookItem.title = info.title;
			addbookItem.intent = info.intent;
			addbookItem.icon = info.icon;
			BookCaseModel.mBookmap.put(key, addbookItem);
			BookCaseModel.mOccupied[addbookItem.cellX][addbookItem.cellY] = true;
			BookCaseModel.addBookItemByBatch(this, addbookItem);
		}
    	
    	LauncherModel.updateBatchInDatabase(this, updateInfos); 
    }

	public void onClick(View v) { // 单击事件：启动一个应用程序或者是打开一个文件夹
		if(v == mDoneBt){
			updateBookCase();
			curChoiseAddInfos.clear();
			curChoiseDeleteInfos.clear();
			if (MainActivity.instance != null
					&& MainActivity.instance.mBookModel != null) {
				MainActivity.instance.mBookModel.refrashData();
			}
			finish();
			return;
		}
		if (MAIN_MENU) {
			if (v.getTag() instanceof FolderInfo) {

				if (DEBUG)
					Log.d(LOG_TAG, "onClick FolderInfo  = " + v
							+ " mWorkspace.getScroller().isFinished() = "
							+ mWorkspace.getScroller().isFinished());
				if (mWorkspace.getScroller().isFinished() /* && getScreen() > 0 */) {
					CellLayout cellLayout = getCurrentCellLayout();
					FolderInfo folderInfo = (FolderInfo) v.getTag();

					if (folderInfo.mExpend) {
						closeFolder(cellLayout, folderInfo.iphoneBubbleTextView);
					} else {
						expandFolder(cellLayout,
								folderInfo.iphoneBubbleTextView);
					}
				}
			} else {
				if (!LauncherValues.getInstance().isAnim()) { // 桌面动画在播放，单击图标后放大图标并且可以拖动
					if (LOGD)
						Log.d(LOG_TAG, "click on the item icon");
					Object tag = v.getTag();
					if (tag instanceof ApplicationInfo) {
						final Intent intent = ((ApplicationInfo) tag).intent;
						int[] pos = new int[2];
						v.getLocationOnScreen(pos);
						startActivitySafely(intent);
					}
				}
			}
		} else {

			Object tag = v.getTag();
//			ApplicationInfo bookInfo;
			if (!(tag instanceof FolderInfo)) {
				ApplicationInfo info = (ApplicationInfo) tag; 
				boolean isRemove = false;
				 
				if (info.isBookCase) {
					if(curChoiseAddInfos.contains(info)){
						curChoiseAddInfos.remove(info);
						isRemove = true;
					}
					info.isBookCase = false; 
					if(!isRemove){
						curChoiseDeleteInfos.add(info);  
					}else{
						isRemove = false;
					}
					
					bookCounts--; 
				} else { 
					if (bookCounts == 8) {
						Toast.makeText(
								this,
								getResources()
										.getString(
												R.string.can_add_shortcut_less_than_eight),
								Toast.LENGTH_SHORT).show();
						return;
					}
					if(curChoiseDeleteInfos.contains(info)){
						curChoiseDeleteInfos.remove(info);
						isRemove = true;
					}
					info.isBookCase = true;
					if(!isRemove){
						curChoiseAddInfos.add(info);  
					}else{
						isRemove = false;
					}
					bookCounts++; 
				}
				v.invalidate();
				mDoneBt.setText(getResources().getString(R.string.finish) + "("
						+ bookCounts + "/"
						+ BookCaseModel.mBookmap.size() + ")");
			} else {
				Toast.makeText(this,
						getResources().getString(R.string.can_not_add_forder),
						Toast.LENGTH_SHORT).show();
			}
		}
	}

    

	void startActivitySafely(Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(LOG_TAG, "Launcher does not have the permission to launch " + intent +
                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                    "or use the exported attribute for this activity.", e);
        }
    }

    /**
     * Returns true if the workspace is being loaded. When the workspace is loading,
     * no user interaction should be allowed to avoid any conflict.
     *
     * @return True if the workspace is locked, false otherwise.
     */
    boolean isWorkspaceLocked() {
        return mDesktopLocked;
    }
    
	public boolean onLongClick(View v) {	//这里的view是CellLayout
        if (LOGD) Log.d(LOG_TAG,"onLongClick v = " + v);
        if(MAIN_MENU){
        if(v instanceof IphoneBubbleTextView){
        	
        	if(!LauncherValues.getInstance().isAnim() && mScreenCount > 0 
            		&& mScreenCount < LauncherValues.mMaxScreenCount){
            	mWorkspace.addCellLayout();
            }
        	
        	ApplicationInfo dropCellInfo;
        	
        		if(LauncherValues.getInstance().isHasFolderOpen()){
        			dropCellInfo = mFolderCellLayout.getDropCellInfo();
        	} else { 
        			dropCellInfo = ((CellLayout)mWorkspace.getChildAt(getScreen())).getDropCellInfo();
        		}
        		mWorkspace.startDrag(dropCellInfo);
        } 
        else{
        	Log.d("zsc", "Launcher.onLongClick()--else--return:false !!!");
        	return false;
            }
        }
        return true;
    }
	
	private static Launcher instance;
    
    public static Launcher getInstance(){
    	return instance;
    }

    static LauncherModel getModel() {
        return sModel;
    }
    
    Workspace getWorkspace() {
        return mWorkspace;
    }
    
    DragLayer getDragLayer() {
    	return mDragLayer;
    }
    
//    TransparentPanel getTransparentPanel() {
//    	return mTransparentPanel;
//    }
    
    CellLayout getCurrentCellLayout(){
    	return (CellLayout) mWorkspace.getChildAt(getScreen());
    }
    
    public NotificationLauncherHandler getNotificationLauncherHandler() {
    	return mNotificationHandler;
    }
    
    private class UpdateAppReceiver extends BroadcastReceiver {

		public void onReceive(Context context, Intent intent) {
			if("android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE".equals(intent.getAction())){
				if (LOGD) Log.d("media", "ACTION_EXTERNAL_APPLICATIONS_AVAILABLE");
				String[] packages = intent.getStringArrayExtra("android.intent.extra.changed_package_list");
				for(String string : packages){
					Log.d("media", "packages=" + string);
				}
				handler.post(new UpdateExtraApplication(packages)); 
			}
		}
    }

    private class CloseSystemDialogsIntentReceiver extends BroadcastReceiver {
         
        public void onReceive(Context context, Intent intent) {
            closeSystemDialogs();
        }
    }

    public void removeShortCut(String packageName){
    	if(LauncherValues.getInstance().isHasFolderOpen()){
    		Launcher.getInstance().closeFolder(mOpenFolderCellLayout, mFolderTarget);
    	}
    	Log.d(TAG, "packageName=" + packageName);
        removeShortcutsForPackage(packageName);
        cleanCellLayout();
    }
    
    /**
     * Receives notifications when applications are added/removed.
     */
    private class ApplicationsIntentReceiver extends BroadcastReceiver {
         
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            final String packageName = intent.getData().getSchemeSpecificPart();
            final boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);

            if (LauncherModel.DEBUG_LOADERS) {
                Log.d(LauncherModel.PACKAGE_TAG, "ApplicationsIntentReceiver action = " + action + ", replacing=" + replacing);
                Log.d(LauncherModel.PACKAGE_TAG, "intent.getData() = " + intent.getData());
                Log.d(LauncherModel.PACKAGE_TAG, "packageName = " + packageName);
            }
            
            if(Intent.ACTION_PACKAGE_CHANGED.equals(action)){
            	if (LauncherModel.DEBUG_LOADERS) Log.d(LauncherModel.LOG_TAG, "sync package " + packageName);
//              sModel.syncPackage(Launcher.this, packageName);
            } else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)){
            	if (!replacing) { 
                    if(LauncherValues.getInstance().isHasFolderOpen()){
                		Launcher.getInstance().closeFolder(mOpenFolderCellLayout, mFolderTarget);
                	}
                	
                    removeShortcutsForPackage(packageName);
                    cleanCellLayout();
                    
                    if (LauncherModel.DEBUG_LOADERS) Log.d(LauncherModel.PACKAGE_TAG, "remove package");
                }
            } else if (Intent.ACTION_PACKAGE_ADDED.equals(action)){
            	if (!replacing) {
                    if (LauncherModel.DEBUG_LOADERS) Log.d(LauncherModel.PACKAGE_TAG, "add package");
                     
                    if (LauncherValues.getInstance().isAnim()) {
                    	LauncherValues.getInstance().setAnim(false, null);
                    }
                    if(LauncherValues.getInstance().isHasFolderOpen()){
                        closeFolder(mOpenFolderCellLayout, mFolderTarget);
                    } 
                    if(packageName.equals("com.android.stk") || packageName.equals("com.android.stk2")){
                    	return;
                    }              
                    sModel.addPackage(Launcher.this, packageName);
                } else {
                    if (LauncherModel.DEBUG_LOADERS) Log.d(LauncherModel.PACKAGE_TAG, "update package");

                    updateShortcutsForPackage(packageName);
                }
            }
        }
    }
    
    public boolean checkFullScreen(){
    	int count = mWorkspace.getChildCount();
    	int[] cell = new int[2];
    	boolean hasFindVacant = false;
    	for (int i = 1 ; i < count ; i++) {
    		CellLayout child = (CellLayout)mWorkspace.getChildAt(i);
    		if(child.getVacantCell(cell, 1, 1, null)){
    			hasFindVacant = true;
    		}
    	}
    	return hasFindVacant;
    }
    
    public void flushPageGuide() {
		mPageGuide.flush();
		mPageGuide.requestLayout();
	}
	
	public IphonePageGuide getPageGuide(){
		return mPageGuide;
	}

    /**
     * Receives notifications whenever the user favorites have changed.
     */
    private class FavoritesChangeObserver extends ContentObserver {
        public FavoritesChangeObserver() {
            super(new Handler());
        }

         
        public void onChange(boolean selfChange) {
            onFavoritesChanged();
        }
    }
    
    private class DrawerManager implements SlidingDrawer.OnDrawerOpenListener,
            SlidingDrawer.OnDrawerCloseListener, SlidingDrawer.OnDrawerScrollListener {
        private boolean mOpen;

        public void onDrawerOpened() {
            if (!mOpen) {
                mHandleIcon.reverseTransition(150);

                final Rect bounds = mWorkspace.mDrawerBounds;

                mOpen = true;
            }
        }

        private void offsetBoundsToDragLayer(Rect bounds, View view) {
            view.getDrawingRect(bounds);

            while (view != mDragLayer) {
                bounds.offset(view.getLeft(), view.getTop());
                view = (View) view.getParent();
            }
        }

        public void onDrawerClosed() {
            if (mOpen) {
                mHandleIcon.reverseTransition(150);
                mWorkspace.mDrawerBounds.setEmpty();
                mOpen = false;
            }

        }

        public void onScrollStarted() {
            if (PROFILE_DRAWER) {
                android.os.Debug.startMethodTracing("/sdcard/launcher-drawer");
            }
        }

        public void onScrollEnded() {
            if (PROFILE_DRAWER) {
                android.os.Debug.stopMethodTracing();
            }
        }
    }

    private static class DesktopBinder extends Handler implements MessageQueue.IdleHandler {
        static final int MESSAGE_BIND_ITEMS = 0x1;
        static final int MESSAGE_BIND_APPWIDGETS = 0x2;
        static final int MESSAGE_BIND_DRAWER = 0x3;

        // Number of items to bind in every pass
        static final int ITEMS_COUNT = 6;

        private final ArrayList<ItemInfo> mShortcuts;
        private final WeakReference<Launcher> mLauncher;

        public boolean mTerminate = false;

        DesktopBinder(Launcher launcher, ArrayList<ItemInfo> shortcuts) {

            mLauncher = new WeakReference<Launcher>(launcher);
            mShortcuts = shortcuts;

            // Sort widgets so active workspace is bound first
            final int currentScreen = launcher.mWorkspace.getCurrentScreen();
            if (LauncherModel.DEBUG_LOADERS) {
            	Log.d(Launcher.LOG_TAG, "------> binding " + shortcuts.size() + " items");
            }
        }

        public void startBindingItems() {
            if (LauncherModel.DEBUG_LOADERS) Log.d(Launcher.LOG_TAG, "------> start binding items");
            obtainMessage(MESSAGE_BIND_ITEMS, 0, mShortcuts.size()).sendToTarget();
        }

        public void startBindingDrawer() {
            if (LauncherModel.DEBUG_LOADERS) Log.d(Launcher.LOG_TAG, "------> start binding drawer");
            obtainMessage(MESSAGE_BIND_DRAWER).sendToTarget();
        }

        public void startBindingAppWidgetsWhenIdle() {
            // Ask for notification when message queue becomes idle
            final MessageQueue messageQueue = Looper.myQueue();
            messageQueue.addIdleHandler(this);
        }

        public boolean queueIdle() {
            // Queue is idle, so start binding items
            startBindingAppWidgets();
            return false;
        }

        public void startBindingAppWidgets() {
            obtainMessage(MESSAGE_BIND_APPWIDGETS).sendToTarget();
        }

         
        public void handleMessage(Message msg) {
            Launcher launcher = mLauncher.get();
            if (launcher == null || mTerminate) {
                return;
            }

            switch (msg.what) {
                case MESSAGE_BIND_ITEMS: {
                	  if(LOGD) Log.d(LOG_TAG,"DesttopBinder.handleMessage msg.what is MESSAGE_BIND_ITEMS");
                    launcher.bindItems(this, mShortcuts, msg.arg1, msg.arg2);
                    break;
                }
            }
        }
    }
    
    public class NotificationLauncherHandler extends Handler {

		 
		public void handleMessage(Message msg) {
			if (LOGD) Log.d(LOG_TAG,"NotificationLauncherHandler handleMessage msg!");
			updateNotificationCount(msg.obj.toString(),msg.arg1);
		}
    }

	private class UpdateExtraApplication implements Runnable {
		
		private String[] updatePackages;
		
		public UpdateExtraApplication(String[] packages){
			updatePackages = packages;
		}

		 
		public void run() {
			List<ItemInfo> allItem = sModel.getAllItem();
			
			for (int i = 0; i < updatePackages.length; i++) {
				String pName = updatePackages[i];
				//Modify GWLLSW-1305 ningyaoyun 20121126(on)
				List<ApplicationInfo> updateInfos = Utilities.getInfoByPackage(allItem, pName);
				
				for(ApplicationInfo updateInfo: updateInfos){
					if(updateInfo == null){
						continue;
					}
					
					try {
						ResolveInfo resolveInfo = mPM.resolveActivity(updateInfo.intent, 0);
						updateInfo.icon =  Utilities.createIconThumbnail(resolveInfo.loadIcon(mPM), Launcher.this);
						updateInfo.icon = Utilities.mergeDrawble(Launcher.this, updateInfo.icon);
						updateInfo.grayIcon = Utilities.convertGrayImg(updateInfo.icon);
						updateInfo.smallIcon = Utilities.createSmallIcon(updateInfo.icon, Launcher.this);
						
						if(updateInfo.isFolderItem){
							FolderInfo folderInfo = updateInfo.folderInfo;
							folderInfo.icon = Utilities.createFolderIcon(Launcher.this, folderInfo.items, Utilities.GRAY_BG);
							folderInfo.grayIcon = Utilities.convertGrayImg(folderInfo.icon);
							IphoneBubbleTextView folderTextView = folderInfo.iphoneBubbleTextView;
							folderTextView.getTextTtile().setCompoundDrawablesWithIntrinsicBounds(null, folderInfo.icon, null, null);
						} else {
							IphoneBubbleTextView textView = updateInfo.iphoneBubbleTextView;
							if(textView != null){					
					        textView.getTextTtile().setCompoundDrawablesWithIntrinsicBounds(null, updateInfo.icon, null, null);
							}
						}	
					} catch (Exception e) {  
					} 
					
				}
				//Modify GWLLSW-1305 ningyaoyun 20121126(off)
			}
		}
	}
}
