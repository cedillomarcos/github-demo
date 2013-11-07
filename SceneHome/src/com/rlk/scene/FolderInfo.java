package com.rlk.scene;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;
import android.view.View; 

public class FolderInfo extends ApplicationInfo {
	private static boolean DEBUG = true;
	private static final String TAG = "FolderInfo";
	
	ArrayList<ApplicationInfo> items;
	ArrayList<Long> itemsId;
	
	boolean mExpend = false;		//标识文件夹是否展开
	
	FolderInfo(Context context) {
        this(context,new ApplicationInfo()); 
    }
	
	FolderInfo(Context context, ApplicationInfo info){
		items = new ArrayList<ApplicationInfo>();
		itemsId = new ArrayList<Long>();
        cellX = info.cellX;
        cellY = info.cellY;
        screen = info.screen;
        title = info.title; 
        pkgName = info.pkgName;
        launcherCount = info.launcherCount;
        iphoneBubbleTextView = info.iphoneBubbleTextView;
        spanX = 1;
        spanY = 1;
        isFolder = true;
        isUninstall = false;
        isBookCase = false;
        packageName = "";
        intent = null;
        itemType = LauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER;
        container = LauncherSettings.Favorites.CONTAINER_DESKTOP;
	}

	@Override
	void onAddToDatabase(ContentValues values) {
		super.onAddToDatabase(values);
		if(DEBUG) Log.d(TAG, "on folder add database itemsId = " + Utilities.intArrayToString(itemsId));
		values.put(LauncherSettings.Favorites.ITEMIDS, Utilities.intArrayToString(itemsId));
	}
}
