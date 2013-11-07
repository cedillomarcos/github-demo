/*
 * Copyright (C) 2008 The Android Open Source Project
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
 

import com.rlk.scene.CellLayout.LayoutParams;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

/**
 * Represents a launchable application. An application is made of a name (or title),
 * an intent and an icon.
 */
public class ApplicationInfo extends ItemInfo {

    /**
     * The application name.
     */
    CharSequence title;
    
    String packageName;

    String pkgName;
    /**
     * The intent used to start the application.
     */
    public Intent intent;
    
    IphoneBubbleTextView iphoneBubbleTextView;	//图标所对应的 View
    
    FolderInfo folderInfo;	//如果在文件夹中，这是文件夹的 tag

    /**
     * app flags 
     */
    boolean isUninstall;	//是否是第三方软件
    
    public boolean isBookCase;    //是否是书柜里的快捷方式 
    float mTranslateX;
    float mTranslateY;
    boolean isFolder;	//是否是文件夹
    
    boolean isFolderItem;	//是否是文件夹中的图标
    
    
    boolean hasMoveOnFolderLinearLayout;
    
    int launcherCount;	
    
    /**
     * The application icon.
     */
    Drawable icon;
    
    Drawable grayIcon;
    
    Bitmap smallIcon;

    /**
     * When set to true, indicates that the icon has been resized.
     */
    boolean filtered;

    /**
     * Indicates whether the icon comes from an application's resource (if false)
     * or from a custom Bitmap (if true.)
     */
    boolean customIcon;
    
    boolean lastAnimCell = false;

    /**
     * If isShortcut=true and customIcon=false, this contains a reference to the
     * shortcut icon as an application's resource.
     */
    Intent.ShortcutIconResource iconResource;

    
    ApplicationInfo() {
        itemType = LauncherSettings.BaseLauncherColumns.ITEM_TYPE_APPLICATION;
    }
    
    public ApplicationInfo(ApplicationInfo info) {
        super(info);
        title = info.title.toString();
        packageName = info.packageName;
        pkgName = info.pkgName;
        intent = new Intent(info.intent);
        iphoneBubbleTextView = info.iphoneBubbleTextView;
        isUninstall = info.isUninstall;
        isBookCase = info.isBookCase;
        launcherCount = info.launcherCount;
        icon = info.icon;
        grayIcon = info.grayIcon;
        filtered = info.filtered;
        customIcon = info.customIcon;
        if (info.iconResource != null) {
            iconResource = new Intent.ShortcutIconResource();
            iconResource.packageName = info.iconResource.packageName;
            iconResource.resourceName = info.iconResource.resourceName;
        }
    }
    
    public void clean(){
    	super.clean();
        title = null;
        packageName = null;
        intent = null;
        iphoneBubbleTextView = null;
        folderInfo = null;
        isUninstall = false;
        isBookCase = false;
        isFolder = false;
        isFolderItem = false;
        hasMoveOnFolderLinearLayout = false;
        launcherCount = 0;	
        icon = null;;
        grayIcon = null;
        smallIcon = null;
        filtered = false;
        customIcon = false;
        lastAnimCell = false;
    }

    /**
     * Creates the application intent based on a component name and various launch flags.
     * Sets {@link #itemType} to {@link LauncherSettings.BaseLauncherColumns#ITEM_TYPE_APPLICATION}.
     *
     * @param className the class name of the component representing the intent
     * @param launchFlags the launch flags
     */
    final void setActivity(ComponentName className, int launchFlags) {
        intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setComponent(className);
        intent.setFlags(launchFlags);
        itemType = LauncherSettings.BaseLauncherColumns.ITEM_TYPE_APPLICATION;
    }
    
    @Override
    void onAddToDatabase(ContentValues values) {
        super.onAddToDatabase(values);

        String titleStr = title != null ? title.toString() : null;
        values.put(LauncherSettings.BaseLauncherColumns.TITLE, titleStr); 
        values.put(LauncherSettings.BaseLauncherColumns.PACKAGENAME, pkgName);
        String uri = intent != null ? intent.toUri(0) : null;
        values.put(LauncherSettings.BaseLauncherColumns.INTENT, uri);

        if (customIcon) {
            values.put(LauncherSettings.BaseLauncherColumns.ICON_TYPE,
                    LauncherSettings.BaseLauncherColumns.ICON_TYPE_BITMAP);
            Bitmap bitmap = ((FastBitmapDrawable) icon).getBitmap();
            writeBitmap(values, bitmap);
        } else {
            values.put(LauncherSettings.BaseLauncherColumns.ICON_TYPE,
                    LauncherSettings.BaseLauncherColumns.ICON_TYPE_RESOURCE);
            if (iconResource != null) {
                values.put(LauncherSettings.BaseLauncherColumns.ICON_PACKAGE,
                        iconResource.packageName);
                values.put(LauncherSettings.BaseLauncherColumns.ICON_RESOURCE,
                        iconResource.resourceName);
            }
        }       
        if (isUninstall) {
        	values.put(LauncherSettings.BaseLauncherColumns.ISUNINSTALL, LauncherSettings.BaseLauncherColumns.ISUNINSTALL_TRUE);
        } else {
        	values.put(LauncherSettings.BaseLauncherColumns.ISUNINSTALL, LauncherSettings.BaseLauncherColumns.ISUNINSTALL_FALSE);
        }

        if(isBookCase){
        	values.put(LauncherSettings.BaseLauncherColumns.ISBOOKCASE, LauncherSettings.BaseLauncherColumns.ISBOOKCASE_TRUE);
        }else{
        	values.put(LauncherSettings.BaseLauncherColumns.ISBOOKCASE, LauncherSettings.BaseLauncherColumns.ISBOOKCASE_FALSE);
        }
        values.put(LauncherSettings.BaseLauncherColumns.LAUNCHERCOUNT, launcherCount);
    }

    @Override
    public String toString() {
        return title.toString();
    }

	public void moveRight() {
		if(cellX != 3){
			cellX += 1;
		}else{
			cellX = 0;
			cellY +=1;
		}
	}

	public void moveTo(LayoutParams layoutParams) {
		cellX = layoutParams.cellX;
		cellY = layoutParams.cellY;
	} 
}
