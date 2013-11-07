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

import android.provider.BaseColumns;
import android.net.Uri;

/**
 * Settings related utilities.
 */
public class LauncherSettings {
    static interface BaseLauncherColumns extends BaseColumns {
        /**
         * Descriptive name of the gesture that can be displayed to the user.
         * <P>Type: TEXT</P>
         */
        static final String TITLE = "title";
        
        static final String PACKAGENAME = "packageName";
        static final String ISBOOKCASE = "isbookcase";

        /**
         * The Intent URL of the gesture, describing what it points to. This
         * value is given to {@link android.content.Intent#parseUri(String, int)} to create
         * an Intent that can be launched.
         * <P>Type: TEXT</P>
         */
        static final String INTENT = "intent";

        static final String ISUNINSTALL = "isuninstall";
        
        static final String LAUNCHERCOUNT = "launcherCount";
        
        static final int ISUNINSTALL_TRUE = 1;
        static final int ISUNINSTALL_FALSE = 0;
        static final int ISBOOKCASE_TRUE = 1;
        static final int ISBOOKCASE_FALSE = 0;
        
        /**
         * The type of the gesture
         *
         * <P>Type: INTEGER</P>
         */
        static final String ITEM_TYPE = "itemType";

        /**
         * The gesture is an application
         */
        static final int ITEM_TYPE_APPLICATION = 0;

        /**
         * The gesture is an application created shortcut
         */
        static final int ITEM_TYPE_SHORTCUT = 1;

        /**
         * The icon type.
         * <P>Type: INTEGER</P>
         */
        static final String ICON_TYPE = "iconType";

        /**
         * The icon is a resource identified by a package name and an integer id.
         */
        static final int ICON_TYPE_RESOURCE = 0;

        /**
         * The icon is a bitmap.
         */
        static final int ICON_TYPE_BITMAP = 1;

        /**
         * The icon package name, if icon type is ICON_TYPE_RESOURCE.
         * <P>Type: TEXT</P>
         */
        static final String ICON_PACKAGE = "iconPackage";

        /**
         * The icon resource id, if icon type is ICON_TYPE_RESOURCE.
         * <P>Type: TEXT</P>
         */
        static final String ICON_RESOURCE = "iconResource";

        /**
         * The custom icon bitmap, if icon type is ICON_TYPE_BITMAP.
         * <P>Type: BLOB</P>
         */
        static final String ICON = "icon";
    }
 
    static final class Favorites implements BaseLauncherColumns {
      
        static final Uri CONTENT_URI = Uri.parse("content://" +
                LauncherProvider.AUTHORITY + "/" + LauncherProvider.TABLE_FAVORITES +
                "?" + LauncherProvider.PARAMETER_NOTIFY + "=true");
 
        static final Uri CONTENT_URI_NO_NOTIFICATION = Uri.parse("content://" +
                LauncherProvider.AUTHORITY + "/" + LauncherProvider.TABLE_FAVORITES +
                "?" + LauncherProvider.PARAMETER_NOTIFY + "=false");
 
        static Uri getContentUri(long id, boolean notify) {
            return Uri.parse("content://" + LauncherProvider.AUTHORITY +
                    "/" + LauncherProvider.TABLE_FAVORITES + "/" + id + "?" +
                    LauncherProvider.PARAMETER_NOTIFY + "=" + notify);
        }
 
        static final String CONTAINER = "container";
 
        static final int CONTAINER_DESKTOP = -100;
        
        static final int CONTAINER_FOLDER = -101;
 
        static final String SCREEN = "screen";
 
        static final String CELLX = "cellX";
 
        static final String CELLY = "cellY";
 
        static final String SPANX = "spanX";
 
        static final String SPANY = "spanY";
 
        static final int ITEM_TYPE_USER_FOLDER = 2;
 
        static final int ITEM_TYPE_LIVE_FOLDER = 3;
 
        static final int ITEM_TYPE_APPWIDGET = 4;
         
 
        static final int ITEM_TYPE_WIDGET_CLOCK = 1000;
 
 
        static final int ITEM_TYPE_WIDGET_PHOTO_FRAME = 1002;
 
        static final String APPWIDGET_ID = "appWidgetId";
     
        @Deprecated
        static final String IS_SHORTCUT = "isShortcut";

       
        static final String URI = "uri";

        
        static final String DISPLAY_MODE = "displayMode";
        
        static boolean ON_FOLDER_TARGET = false;
        
        static final String ITEMIDS = "itemIds";
    }
    
 
 
 
 
 
 
  
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
     
    
    public static final class BookCase implements BaseLauncherColumns {
    	 
    	public static final Object QueryObj = new Object();  
 
        public static final Uri CONTENT_URI = Uri.parse("content://" +
                LauncherProvider.AUTHORITY + "/" + LauncherProvider.TABLE_BOOKCASE +
                "?" + LauncherProvider.PARAMETER_NOTIFY + "=true");
 
        static final Uri CONTENT_URI_NO_NOTIFICATION = Uri.parse("content://" +
                LauncherProvider.AUTHORITY + "/" + LauncherProvider.TABLE_BOOKCASE +
                "?" + LauncherProvider.PARAMETER_NOTIFY + "=false");
 
        static Uri getContentUri(long id, boolean notify) {
            return Uri.parse("content://" + LauncherProvider.AUTHORITY +
                    "/" + LauncherProvider.TABLE_BOOKCASE + "/" + id + "?" +
                    LauncherProvider.PARAMETER_NOTIFY + "=" + notify);
        }
 
 
  
 
        static final String CELLX = "cellX";
 
        static final String CELLY = "cellY";
 
 
      
 
        static final String CELLSUM = "cellSum";
 
     
    } 
    
}
