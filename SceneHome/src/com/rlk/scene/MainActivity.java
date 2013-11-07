package com.rlk.scene;
  
import java.util.ArrayList; 
import java.util.HashMap;
import java.util.List;

import com.mediatek.common.featureoption.FeatureOption;
import com.rlk.scene.BookCaseModel;
import com.rlk.scene.ItemInfo;
import com.rlk.scene.LauncherValues;
import com.rlk.scene.R; 
import com.rlk.scene.items.BookCaseItem;
import com.rlk.scene.items.MySeekbar;
import android.widget.RelativeLayout;
import android.os.Bundle;
import android.app.Activity; 
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log; 
import android.view.KeyEvent;
import android.view.LayoutInflater; 
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Window;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements View.OnClickListener,MTKUnreadLoader.UnreadCallbacks{ 
	private String TAG = "MainActivity";
	private Button smsBt;
	private Button homeBt;
	private Button phoneBt; 
	private TextView mUnreadSms;
	private TextView mUnreadPhone;
	public static MainActivity instance;    
	public BookCaseModel mBookModel;
	private LayoutInflater mInflater;   
    public MTKUnreadLoader mUnreadLoader;
         public static  RelativeLayout mLayout;
//    private View enterView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        setContentView(R.layout.main); 
        Log.d(TAG, "onCreate");
       mLayout=(RelativeLayout) findViewById(R.id.enterview);
        smsBt = (Button) findViewById(R.id.sms); 
        homeBt = (Button) findViewById(R.id.home); 
        phoneBt = (Button) findViewById(R.id.phone);  
        mUnreadSms = (TextView) findViewById(R.id.unread_sms);
        mUnreadPhone = (TextView) findViewById(R.id.unread_phone); 
        
        mInflater = getLayoutInflater();
        setupConfiguration();
//        Utilities.initBookMap(this);
        Utilities.initBookEnterMap(this);
        
        mBookModel = new BookCaseModel(this);
        
        homeBt.setOnClickListener(this);
        phoneBt.setOnClickListener(this);  
        smsBt.setOnClickListener(this);
        instance = this; 
        if (FeatureOption.MTK_LAUNCHER_UNREAD_SUPPORT) {
			mUnreadLoader = new MTKUnreadLoader(getApplicationContext());
			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.MTK_ACTION_UNREAD_CHANGED);
			registerReceiver(mUnreadLoader, filter);
			mUnreadLoader.initialize(this);
			mUnreadLoader.loadAndInitUnreadShortcuts();
		}
     }
  
    
    public LayoutInflater getInflater(){
    	return mInflater;
    }
    
    public void startBindBookCase(HashMap<Integer,ApplicationInfo> shortcuts){
    	SceneSurfaceView2.instance.bindBookCase(shortcuts);
    }
 
    
    private void setupConfiguration(){
    	LauncherValues.mScreenScale = getResources().getDisplayMetrics().scaledDensity;
        LauncherValues.mScreenHeight = getResources().getDisplayMetrics().heightPixels;
        LauncherValues.mScreenWidth = getResources().getDisplayMetrics().widthPixels;
        
        LauncherValues.mMinimumStartDropDistance = (int)getResources().getDimension(R.dimen.minimum_start_drop_distance);
        LauncherValues.mBubbleTextViewTranslateXY = 0.7f;
        
        LauncherValues.mLongAxisEndPadding = (int)getResources().getDimension(R.dimen.long_axis_end_Padding);
        LauncherValues.mLowestCellLocation = LauncherValues.mScreenHeight - LauncherValues.mLongAxisEndPadding;
        
        LauncherValues.mFolderTopCurrowLeft = (int)getResources().getDimension(R.dimen.folder_top_currow_left);
        LauncherValues.mFolderBottomCurrowLeft = (int)getResources().getDimension(R.dimen.folder_bottom_currow_left);
        LauncherValues.mGrayBgDrawLeft = (int)getResources().getDimension(R.dimen.gray_bg_draw_left);
        LauncherValues.mGrayBgDrawTop = (int)getResources().getDimension(R.dimen.gray_bg_draw_top);
        
        LauncherValues.mIconWdith = (int)getResources().getDimension(R.dimen.icon_width);
        LauncherValues.mIconHeiht = (int)getResources().getDimension(R.dimen.icon_height);
        LauncherValues.mMiniIconWidth = (int)getResources().getDimension(R.dimen.miniature_icon_width);
        LauncherValues.mMiniIconHeight = (int)getResources().getDimension(R.dimen.miniature_icon_height);
        LauncherValues.mMiniIconPaddingLeft = (int)getResources().getDimension(R.dimen.miniature_icon_padding_left);
        LauncherValues.mMiniIconPaddingTop = (int)getResources().getDimension(R.dimen.miniature_icon_padding_top);
        
        LauncherValues.mIconMirroPaddingLeft = (int)getResources().getDimension(R.dimen.icon_mirro_padding_left);
        LauncherValues.mIconMirroPaddingTop = (int)getResources().getDimension(R.dimen.icon_mirro_padding_top);
        
        LauncherValues.mCalendarWeakTextSize = (int) getResources().getInteger(R.integer.calendar_weak_text_size);
        LauncherValues.mCalendarWeakTextTop = (int) getResources().getDimension(R.dimen.calerdar_weak_text_top);
        LauncherValues.mCalendarDateTextSize = (int) getResources().getInteger(R.integer.calendar_weak_date_size);
        LauncherValues.mCalendarDateTextTop = (int) getResources().getDimension(R.dimen.calerdar_weak_date_top);
        
        LauncherValues.mScrollZone = (int) getResources().getDimension(R.dimen.start_scroll_zone);
        Utilities.initValues();
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) { 
         	if(!SceneSurfaceView2.instance.isTouchable)
          		return true;
    	return super.dispatchTouchEvent(ev);
    }
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) { 
		if (event.getAction() == KeyEvent.ACTION_DOWN) { 
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_BACK:
                    return true; 
            }
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_BACK:  
                    if(SceneSurfaceView2.isTranslateAnim){ 
                    	SceneSurfaceView2.isTranslateAnim = false;
//                    	SceneSurfaceView2.instance.mStopFrash = false;
                    	SceneSurfaceView2.instance.startRefrashWidget();
                	}
				//liyang add start
					/*Intent launcher = new Intent();
					launcher.setAction(Intent.ACTION_MAIN);
					launcher.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
					launcher.setComponent(new ComponentName("com.android.launcher","com.android.launcher2.Launcher")); 
					startActivity(launcher);
					finish();*/
				//liyang add end
                    return true; 
            }
        }
		return super.dispatchKeyEvent(event);
	}

	@Override
	protected void onResume() { 
		super.onResume();
		LauncherValues.SCENE_BOOKCASE_TEXTVIEW = true;
		Log.d(TAG, "onResume");
		//zsc sence_feature 0507 +++
//		setDefaultLauncher();
		Intent setDefaultIntent = new Intent();
   	    setDefaultIntent.setAction("com.android.launcher.SCENE_LAUNCHER");
   	    sendBroadcast(setDefaultIntent);
   	    Log.d(TAG, "MainActivity.onResume()--setDefaultLauncher");
		//zsc sence_feature 0507 ---
		SceneSurfaceView2.instance.onResume();
	}
	
    void setDefaultLauncher(){
 		Intent intent = new Intent(Intent.ACTION_MAIN);
 		intent.addCategory(Intent.CATEGORY_HOME);
 		PackageManager pm = getPackageManager();
 		List<ResolveInfo> resolveInfoList = pm.queryIntentActivities(intent, 0);
 		Log.d("zsc", "scene_Launcher.setDefaultLauncher()--resolveInfoList.size="+resolveInfoList.size());
 		if (resolveInfoList != null) {
 			int size = resolveInfoList.size();
 			for (int j = 0; j < size; j++) {
 				final ResolveInfo r = resolveInfoList.get(j);
 				if (r.activityInfo.packageName.equals("com.android.provision")
 						|| r.activityInfo.name.equals("com.android.settings.CryptKeeper")) {
 					resolveInfoList.remove(j);
 					size -= 1;
 					break;
 				}
 			}
 			ComponentName[] set = new ComponentName[size];
 			ComponentName defaultLauncher = new ComponentName(
 					"com.rlk.scene", "com.rlk.scene.MainActivity");
 			int defaultMatch = 0;
 			if (size <= 2) {
 				Log.d("zsc", "scene_Launcher.setDefaultLauncher()()--<=2");
 				for (int i = 0; i < size; i++) {
 					final ResolveInfo resolveInfo = resolveInfoList.get(i);
 					set[i] = new ComponentName(
 							resolveInfo.activityInfo.packageName,
 							resolveInfo.activityInfo.name);
 						if (resolveInfo.match > defaultMatch)defaultMatch = resolveInfo.match;
 				
 				}
 				
 				IntentFilter filter = new IntentFilter();
 				filter.addAction(Intent.ACTION_MAIN);
 				filter.addCategory(Intent.CATEGORY_HOME);
 				filter.addCategory(Intent.CATEGORY_DEFAULT);
 				pm.clearPackagePreferredActivities("com.android.launcher");
 				pm.clearPackagePreferredActivities(defaultLauncher
 						.getPackageName());
 				pm.addPreferredActivity(filter, defaultMatch, set,
 						defaultLauncher);
 			}
 		}
    }
	
    @Override
    protected void onPause() { 
    	super.onPause();
    	SceneSurfaceView2.instance.onPause();
    }
	@Override
	protected void onDestroy() { 
		SceneSurfaceView2.instance.recycle();
		SceneSurfaceView2.isTranslateAnim = false;
		BookCaseItem.isDrag = false;
//		SceneSurfaceView2.instance.mStopFrash = true;
		MySeekbar.isSeekbarTouched = false;
		SceneSurfaceView2.instance.mCurScreen = 0;
		if (FeatureOption.MTK_LAUNCHER_UNREAD_SUPPORT) {
            unregisterReceiver(mUnreadLoader);
        }
		Utilities.recycleAllBitmaps();
		super.onDestroy();		
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	@Override
	public void onClick(View v) { 
		if(v == smsBt){
			Intent intent = new Intent(); 
			intent.setClassName("com.android.mms", "com.android.mms.ui.ConversationList");
			startActivity(intent);
		}else if(v == homeBt){
			Intent intent = new Intent(); 
			intent.setAction(Intent.ACTION_MAIN);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			intent.setComponent(new ComponentName("com.rlk.scene","com.rlk.scene.Launcher"));  
			startActivity(intent);
		}else if(v == phoneBt){
			Intent intent = new Intent(); 
			intent.setClassName("com.android.contacts", "com.android.contacts.activities.DialtactsActivity");
			startActivity(intent);
		}
	}
	@Override
	public void updateNotificationCount(String className, int count) {
		if(className.equals("com.android.mms.ui.BootActivity")){
			if(count>0){
				mUnreadSms.setVisibility(View.VISIBLE);
				mUnreadSms.setText(Integer.toString(count));
			}else{
				mUnreadSms.setVisibility(View.INVISIBLE);
			}
		}else if(className.equals("com.android.contacts.activities.DialtactsActivity")){
			if(count>0){
				mUnreadPhone.setVisibility(View.VISIBLE);
				mUnreadPhone.setText(Integer.toString(count));
			}else{
				mUnreadPhone.setVisibility(View.INVISIBLE);
			}
		}
	}
	
    @Override
	public boolean onCreateOptionsMenu(Menu menu) { 
    	super.onCreateOptionsMenu(menu);
		menu.add(0, 1, 1, getResources().getString(R.string.menu_classic));  
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) { 
		Intent launcher = new Intent();
		launcher.setAction(Intent.ACTION_MAIN);
		launcher.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
		launcher.setComponent(new ComponentName("com.android.launcher","com.android.launcher2.Launcher")); 
		startActivity(launcher);
		finish();
		return true;
	}
    
    
}
