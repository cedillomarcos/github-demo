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

import static android.util.Log.d;
import static android.util.Log.e;
import static android.util.Log.w;

import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger; 
import android.content.ComponentName;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context; 
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Process;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;

/**
 * Maintains in-memory state of the Launcher. It is expected that there should be only one
 * LauncherModel object held in a static. Also provide APIs for updating the database state
 * for the Launcher.
 */
public class LauncherModel {
	static final boolean DEBUG = true;
    static final boolean DEBUG_LOADERS = true;
    static final String LOG_TAG = "IphoneHomeLoaders";
    static final String PACKAGE_TAG = "package";
    static final String database = "database";
     
    private static final long APPLICATION_NOT_RESPONDING_TIMEOUT = 5000;
    private static final int INITIAL_ICON_CACHE_CAPACITY = 50;

    private static final Collator sCollator = Collator.getInstance();
    private boolean mDesktopItemsLoaded;

    public static ArrayList<ItemInfo> mDesktopItems; 
    private List<ItemInfo> mAllItem;
    

    
    private List<ItemInfo> mDesktopFolders;
    private List<ItemInfo> mDesktopFolderItems;
    
    private Context context;
    
	private DesktopItemsLoader mDesktopItemsLoader;

	private Thread mDesktopLoaderThread;

	private final HashMap<ComponentName, ApplicationInfo> mAppInfoCache =
            new HashMap<ComponentName, ApplicationInfo>(INITIAL_ICON_CACHE_CAPACITY);

    synchronized void abortLoaders() {
        if (DEBUG_LOADERS) d(LOG_TAG, "aborting loaders");
        if (mDesktopItemsLoader != null && mDesktopItemsLoader.isRunning()) {
            if (DEBUG_LOADERS) d(LOG_TAG, "  --> aborting workspace loader");
            mDesktopItemsLoader.stop();
            mDesktopItemsLoaded = false;
        }
    }
    
    public void setContext(Context context){
    	this.context = context;
    }
    
    /**
     * Drop our cache of components to their lables & icons.  We do
     * this from Launcher when applications are added/removed.  It's a
     * bit overkill, but it's a rare operation anyway.
     */
    synchronized void dropApplicationCache() {
        mAppInfoCache.clear();
    }
    


        
    

    private static List<ResolveInfo> findActivitiesForPackage(PackageManager packageManager,
            String packageName) {

        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        final List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);
        final List<ResolveInfo> matches = new ArrayList<ResolveInfo>();

        if (apps != null) {
            // Find all activities that match the packageName
            int count = apps.size();
            for (int i = 0; i < count; i++) {
                final ResolveInfo info = apps.get(i);
                final ActivityInfo activityInfo = info.activityInfo;
                Log.d(PACKAGE_TAG, "activityInfo.packageName:" + activityInfo.packageName);
                if (packageName.equals(activityInfo.packageName)) {
                    matches.add(info);
                }
            }
        }

        return matches;
    }
    
    public ArrayList<String> mDataBasePkg;   
    public ArrayList<String> mScanPkg;
    public void getAllDatabaseAppName(Launcher launcher){
    	if(mDataBasePkg == null){
    		mDataBasePkg = new ArrayList<String>();
    	}else{
    		mDataBasePkg.clear();
    	}
    	final ContentResolver contentResolver = launcher.getContentResolver();
      
    	final Cursor favoritesCursor = contentResolver.query(
                LauncherSettings.Favorites.CONTENT_URI, new String[] {
                		LauncherSettings.Favorites.PACKAGENAME
		        }, null, null, null);
    	String title = null;
    	while(favoritesCursor.moveToNext()){
    		title = favoritesCursor.getString(0);
    		if(title != null){
    			if(!mDataBasePkg.contains(title)){
    				mDataBasePkg.add(title);
    			}
    		}
    	}
    	
    	
    	favoritesCursor.close();
    	Log.d(PACKAGE_TAG, "mDataBasePkg sizes= " + mDataBasePkg.size());
    }
    

    public void refrashLostPackages(Launcher launcher){
    	getAllDatabaseAppName(launcher);
    	addLostPackages(launcher, mDataBasePkg);
    	mDataBasePkg.clear();
    }
    
    public void addLostPackages(Launcher launcher,ArrayList<String> databasePkg){
    	final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        PackageManager packageManager = launcher.getPackageManager();
        
        final List<ResolveInfo> apps = packageManager.queryIntentActivities(mainIntent, 0);
        Log.d(PACKAGE_TAG, "apps size:" + apps.size()); 
        final List<ResolveInfo> matches = new ArrayList<ResolveInfo>();
        if(mScanPkg == null){
        	mScanPkg = new ArrayList<String>();
        } else{
        	mScanPkg.clear();
        }
        if (apps != null) {
            // Find all activities that match the packageName
            int count = apps.size();
            for (int i = 0; i < count; i++) {
                final ResolveInfo info = apps.get(i);
                final ActivityInfo activityInfo = info.activityInfo;
                //Add GWLLSW-1306 ningyaoyun 20121123(on)
                if(info.activityInfo.packageName.equals("com.android.stk") || info.activityInfo.packageName.equals("com.android.stk2")
                		|| info.activityInfo.packageName.equals("com.rlk.scene")){
                	continue;
                }
                //Add GWLLSW-1306 ningyaoyun 20121123(off)                
//                String label = getLabel(packageManager,activityInfo);
                String packageName = info.activityInfo.packageName;
                Log.d(PACKAGE_TAG, "packageManager packageName after:" + info.activityInfo.packageName );
                if(!mScanPkg.contains(packageName)){
                	mScanPkg.add(packageName);
                } 
                if (!databasePkg.contains(packageName)) {
                	Log.d(PACKAGE_TAG, "add  packageName:" + packageName);
                    matches.add(info);
                }
            }
        }
    
    



        for(int i=0; i<databasePkg.size();i++){

        	String name = databasePkg.get(i);



        	if(!mScanPkg.contains(name)){
        		Log.d(PACKAGE_TAG, "remove packageName:" + name);

            	Launcher.getInstance().removeShortcutsForPackage(name);
                }
        }

        addLostPackage(launcher, matches);
        }



 



    Drawable getApplicationInfoIcon(PackageManager manager, ApplicationInfo info) {
        final ResolveInfo resolveInfo = manager.resolveActivity(info.intent, 0);
        if (resolveInfo == null) {
            return null;
        }

        ComponentName componentName = new ComponentName(
                resolveInfo.activityInfo.applicationInfo.packageName,
                resolveInfo.activityInfo.name);
        ApplicationInfo application = mAppInfoCache.get(componentName);

        if (application == null) {
            return resolveInfo.activityInfo.loadIcon(manager);
        }

        return application.icon;
    }

    private static ApplicationInfo makeAndCacheApplicationInfo(PackageManager manager,
            HashMap<ComponentName, ApplicationInfo> appInfoCache, ResolveInfo info,
            Context context) {

        ComponentName componentName = new ComponentName(
                info.activityInfo.applicationInfo.packageName,
                info.activityInfo.name);
        ApplicationInfo application = appInfoCache.get(componentName);

        if (application == null) {
            application = new ApplicationInfo();
            application.container = ItemInfo.NO_ID;

            updateApplicationInfoTitleAndIcon(manager, info, application, context);

            application.setActivity(componentName,
                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            appInfoCache.put(componentName, application);
        }

        return application;
    }

    private static void updateApplicationInfoTitleAndIcon(PackageManager manager, ResolveInfo info,
            ApplicationInfo application, Context context) {

        application.title = info.loadLabel(manager); 
        
        application.pkgName = info.activityInfo.packageName;
        
        if (application.title == null) {
            application.title = info.activityInfo.name;
        }
        Drawable icon = null;
        if (icon == null){
        	application.icon =
    			Utilities.createIconThumbnail(info.activityInfo.loadIcon(manager), context);
        }
        
        application.filtered = false;
        application.grayIcon = Utilities.convertGrayImg(application.icon);
        application.smallIcon = Utilities.createSmallIcon(application.icon, context);
    }
  
    private static final AtomicInteger sWorkspaceLoaderCount = new AtomicInteger(1);
  
    static class ApplicationInfoComparator implements Comparator<ApplicationInfo> {
        public final int compare(ApplicationInfo a, ApplicationInfo b) {
            return sCollator.compare(a.title.toString(), b.title.toString());
        }
    }

    boolean isDesktopLoaded() {
    	boolean isSame = false;
    	if(Launcher.getInstance() != null){
    		final ContentResolver contentResolver = Launcher.getInstance().getContentResolver();
            final PackageManager manager = Launcher.getInstance().getPackageManager();  
            final Cursor c = contentResolver.query(
                    LauncherSettings.Favorites.CONTENT_URI, null, null, null, null);
            if(mDesktopItems != null)
            isSame = c.getCount() == mDesktopItems.size();
            c.close();
    	}
        return mDesktopItems != null && mDesktopItems.size() > 0 && isSame && mDesktopItemsLoaded;
    }

    /**
     * Loads all of the items on the desktop, in folders, or in the dock.
     * These can be apps, shortcuts or widgets 
     * isLaunching=true, localeChanged=false, loadApplications=false
     */
    void loadUserItems(boolean isLaunching, Launcher launcher, boolean localeChanged,
            boolean loadApplications) {
        if (DEBUG_LOADERS) d(LOG_TAG, "loading user items");

        if (isLaunching && isDesktopLoaded()) {
            if (DEBUG_LOADERS) d(LOG_TAG, "  --> items loaded, return");
            
            launcher.onDesktopItemsLoaded(mDesktopItems); 
            return;
        }

        if (mDesktopItemsLoader != null && mDesktopItemsLoader.isRunning()) {
            if (DEBUG_LOADERS) d(LOG_TAG, "  --> stopping workspace loader");
            mDesktopItemsLoader.stop();
            // Wait for the currently running thread to finish, this can take a little
            // time but it should be well below the timeout limit
            try {
                mDesktopLoaderThread.join(APPLICATION_NOT_RESPONDING_TIMEOUT);
            } catch (InterruptedException e) {
                // Empty
            }

            // If the thread we are interrupting was tasked to load the list of
            // applications make sure we keep that information in the new loader
            // spawned below
            // note: we don't apply this to localeChanged because the thread can
            // only be stopped *after* the localeChanged handling has occured
            loadApplications = mDesktopItemsLoader.mLoadApplications;
        }

        if (DEBUG_LOADERS) d(LOG_TAG, "  --> starting workspace loader");
        mDesktopItemsLoaded = false;
        mDesktopItemsLoader = new DesktopItemsLoader(launcher, localeChanged, loadApplications,
                isLaunching);
        mDesktopLoaderThread = new Thread(mDesktopItemsLoader, "Desktop Items Loader");
        mDesktopLoaderThread.start();
    }
    public boolean isLoaderThreadRunning(){
    	return mDesktopItemsLoader.isRunning();
    }
     
    
	/**
	 * 应用程序的名称有变，需要重新更新数据库
	 * @param resolver
	 * @param manager
	 */
    private static void updateShortcutLabels(ContentResolver resolver, PackageManager manager) {
    	if(DEBUG_LOADERS) d(LOG_TAG,"updateShortcutLables CONTENT_URI="+LauncherSettings.Favorites.CONTENT_URI);
        final Cursor c = resolver.query(LauncherSettings.Favorites.CONTENT_URI,
                new String[] { LauncherSettings.Favorites._ID, LauncherSettings.Favorites.TITLE,
                        LauncherSettings.Favorites.INTENT, LauncherSettings.Favorites.ITEM_TYPE },
                null, null, null);

        final int idIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);
        final int intentIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.INTENT);
        final int itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEM_TYPE);
        final int titleIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.TITLE);
 

        try {
            while (c.moveToNext()) {
                try {
                    if (c.getInt(itemTypeIndex) !=
                            LauncherSettings.Favorites.ITEM_TYPE_APPLICATION) {
                        continue;
                    }

                    final String intentUri = c.getString(intentIndex);
                    if (intentUri != null) {
                        final Intent shortcut = Intent.parseUri(intentUri, 0);
                        if (Intent.ACTION_MAIN.equals(shortcut.getAction())) {
                            final ComponentName name = shortcut.getComponent();
                            if (name != null) {
                                final ActivityInfo activityInfo = manager.getActivityInfo(name, 0);
                                final String title = c.getString(titleIndex);
                                String label = getLabel(manager, activityInfo);

                                if (title == null || !title.equals(label) && label != null) {
                                    final ContentValues values = new ContentValues();
                                    values.put(LauncherSettings.Favorites.TITLE, label);

                                    resolver.update(
                                            LauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION,
                                            values, "_id=?",
                                            new String[] { String.valueOf(c.getLong(idIndex)) });

                                    // changed = true;
                                }
                            }
                        }
                    }
                } catch (URISyntaxException e) {
                    // Ignore
                } catch (PackageManager.NameNotFoundException e) {
                    // Ignore
                }
            }
        } finally {
            c.close();
        } 
    }

    private static String getLabel(PackageManager manager, ActivityInfo activityInfo) {
        String label = activityInfo.loadLabel(manager).toString();
        if (label == null) {
            label = manager.getApplicationLabel(activityInfo.applicationInfo).toString();
            if (label == null) {
                label = activityInfo.name;
            }
        }
        return label;
    }
 
    private class DesktopItemsLoader implements Runnable {
        private volatile boolean mStopped;
        private volatile boolean mRunning;

        private final WeakReference<Launcher> mLauncher;
        private final boolean mLocaleChanged;
        private final boolean mLoadApplications; 
        private final int mId;        

        DesktopItemsLoader(Launcher launcher, boolean localeChanged, boolean loadApplications,
                boolean isLaunching) {
            mLoadApplications = loadApplications;
            if(DEBUG_LOADERS) d(LOG_TAG,"DesktopItemsLoader() loadApplications="+loadApplications); 
            mLauncher = new WeakReference<Launcher>(launcher); 
            mLocaleChanged = localeChanged;
            mId = sWorkspaceLoaderCount.getAndIncrement();
        }

        void stop() {
            mStopped = true;
        }

        boolean isRunning() {
            return mRunning;
        }

        public void run() {
            if (DEBUG_LOADERS) d(LOG_TAG, "  ----> running workspace loader (" + mId + ")");
            
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
            
            mRunning = true;
            final Launcher launcher = mLauncher.get();
            
            final ContentResolver contentResolver = launcher.getContentResolver();
            final PackageManager manager = launcher.getPackageManager();
            if (mLocaleChanged) {
                updateShortcutLabels(contentResolver, manager);  //如果更改了语言和地区，应用程序的名称需要做相应的更改。
            }
              
            final Cursor c = contentResolver.query(
                    LauncherSettings.Favorites.CONTENT_URI, null, null, null, null);
            
            mDesktopItems = new ArrayList<ItemInfo>(); 
            mAllItem = new ArrayList<ItemInfo>();
            mDesktopFolders = new ArrayList<ItemInfo>();
            mDesktopFolderItems = new ArrayList<ItemInfo>();
             
        	try {
                final int idIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);
                final int intentIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.INTENT);
                final int isUninstallIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ISUNINSTALL);
                final int launcherCountIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.LAUNCHERCOUNT);
                final int titleIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.TITLE);
                final int pkgName = c.getColumnIndexOrThrow(LauncherSettings.Favorites.PACKAGENAME); 
                final int isBookCaseIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ISBOOKCASE); 
                final int iconPackageIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON_PACKAGE);
                final int containerIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CONTAINER);
                final int itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEM_TYPE);
                final int screenIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SCREEN);
                final int cellXIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLX);
                final int cellYIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLY); 
                final int itemIdsIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEMIDS);

                ApplicationInfo info = null;
                FolderInfo folderInfo;
                String intentDescription; 
                int container; 
                Intent intent;
                
                while (!mStopped && c.moveToNext()) {
                	Log.d("ningyaoyun", "DesktopItemsLoader  mStopped=" + mStopped);
                    try {
                        int itemType = c.getInt(itemTypeIndex);
                        switch (itemType) {
                        case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
                        case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                            intentDescription = c.getString(intentIndex);
                            try {
                                intent = Intent.parseUri(intentDescription, 0);
                            } catch (java.net.URISyntaxException e) {
                                continue;
                            }
                            
                            if (itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION) {
                            	int a = c.getInt(isUninstallIndex);
                            	boolean isUninstall = false;
                            	if ( a == LauncherSettings.Favorites.ISUNINSTALL_TRUE) {
                                	isUninstall = true; 
                                } else {
                                	isUninstall = false;
                                }
                                info = getApplicationInfo(manager, intent, launcher, isUninstall,c.getString(pkgName));
                            }  
                            
                            if(info == null){
                            	info = new ApplicationInfo();
                            	info.intent = intent;
                            	info.icon = manager.getDefaultActivityIcon();
                            	if(DEBUG_LOADERS) Log.d("id", "workspace null id = " + c.getLong(idIndex));
                            }

                            if (info != null) {
                                int a = c.getInt(isUninstallIndex);
                                if ( a == LauncherSettings.Favorites.ISUNINSTALL_TRUE) {
                                	info.isUninstall = true;
                            		info.icon = Utilities.mergeDrawble(context, info.icon);
                                    info.grayIcon = Utilities.convertGrayImg(info.icon);
                                    info.smallIcon = Utilities.createSmallIcon(info.icon,context);
                                    info.filtered = true;
                                } else {
                                	info.isUninstall = false;
                                }
                                int b = c.getInt(isBookCaseIndex);
                                if(b == LauncherSettings.Favorites.ISBOOKCASE_TRUE){
                                	info.isBookCase = true;
                                }else{
                                	info.isBookCase = false;
                                }
                                
                                info.packageName = c.getString(iconPackageIndex);
                                info.pkgName = c.getString(pkgName);
                                info.launcherCount = c.getInt(launcherCountIndex);
                                info.title = c.getString(titleIndex);
                                info.intent = intent;
                                info.id = c.getLong(idIndex);
                                container = c.getInt(containerIndex);
                                info.container = container;
                                info.screen = c.getInt(screenIndex);
                                info.cellX = c.getInt(cellXIndex);
                                info.cellY = c.getInt(cellYIndex);
                                mAllItem.add(info);
                                if(container == LauncherSettings.Favorites.CONTAINER_DESKTOP){
                                	mDesktopItems.add(info);
                                }else if(container == LauncherSettings.Favorites.CONTAINER_FOLDER){
                                	mDesktopFolderItems.add(info);
                                }
                                
                            }
                            break;
                        case LauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER:
                        	folderInfo = new FolderInfo(context);
                        	long folderId = c.getLong(idIndex);
                        	int cellX = c.getInt(cellXIndex);
                        	int cellY = c.getInt(cellYIndex);
                        	int screen = c.getInt(screenIndex);
                        	int contain = c.getInt(containerIndex);
                        	String itemIds = c.getString(itemIdsIndex);
                        	folderInfo.title = c.getString(titleIndex);
                        	folderInfo.id = folderId;
                        	folderInfo.cellX = cellX;
                        	folderInfo.cellY = cellY;
                        	folderInfo.screen = screen;
                        	folderInfo.container = contain;
                        	folderInfo.launcherCount = c.getInt(launcherCountIndex);
                        	folderInfo.itemsId = Utilities.stringToArrayList(itemIds);
                        	mDesktopFolders.add(folderInfo);
                        	if(contain == LauncherSettings.Favorites.CONTAINER_DESKTOP){
                        		mDesktopItems.add(folderInfo);
                            }
                        	break; 
                        }
                    } catch (Exception e) {
                        w(Launcher.LOG_TAG, "Desktop items loading interrupted:", e);
                    }
                }
                
                setFolderItemInfo(mDesktopFolders, mDesktopFolderItems);
            } finally {
                c.close();
                if(mStopped){
                	if (DEBUG_LOADERS) d(LOG_TAG, "  ---->finally  mStopped = true  finish");
                	Launcher.getInstance().finish();
                	return;
                }
            } 
             
            synchronized(LauncherModel.this) {
                if (!mStopped) {

                    final ArrayList<ItemInfo> uiDesktopItems = new ArrayList<ItemInfo>(mDesktopItems); 
                    if (!mStopped) {
                        launcher.runOnUiThread(new Runnable() {
                            public void run() {
                                if (DEBUG_LOADERS) d(LOG_TAG, "  ----> onDesktopItemsLoaded()");
  
                                launcher.onDesktopItemsLoaded(uiDesktopItems);
 
                            }
                        });
                    }
                    mDesktopItemsLoaded = true;
                }else{
                	if (DEBUG_LOADERS) d(LOG_TAG, "  ---->runOnUiThread  mStopped = true  finish");
                	Launcher.getInstance().finish();
                } 
            }
            mRunning = false;
            
            launcher.mayReceiveNotificationLauncher();
        }

		private void setFolderItemInfo(List<ItemInfo> folders, List<ItemInfo> items) {
			
			for(ItemInfo item : folders){
				FolderInfo folder = (FolderInfo)item;
				
				for(Long id : folder.itemsId){
					ApplicationInfo info = (ApplicationInfo) Utilities.findItemInfoById(items, id);
					if(info != null){
						folder.items.add(info);
						info.isFolderItem = true;
						info.folderInfo = folder; 
					}
				}
			}
			folders = null;
			items = null;
		}
    }

    /**
     * Remove the callback for the cached drawables or we leak the previous
     * Home screen on orientation change.
     */
    void unbind() {
        // Interrupt the applications loader before setting the adapter to null
        unbindDrawables(mDesktopItems);
        unbindCachedIconDrawables();
    }

    /**
     * Remove the callback for the cached drawables or we leak the previous
     * Home screen on orientation change.
     */
    private void unbindDrawables(ArrayList<ItemInfo> desktopItems) {
        if (desktopItems != null) {
            final int count = desktopItems.size();
            for (int i = 0; i < count; i++) {
                ItemInfo item = desktopItems.get(i);
                switch (item.itemType) {
                case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
                case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
                case LauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER:
                	if(((ApplicationInfo)item).icon != null){
                		((ApplicationInfo)item).icon.setCallback(null);
                	}
                    break;
                }
            }
        }
    }

    /**
     * Remove the callback for the cached drawables or we leak the previous
     * Home screen on orientation change.
     */
    private void unbindAppDrawables(ArrayList<ApplicationInfo> applications) {
        if (applications != null) {
            final int count = applications.size();
            for (int i = 0; i < count; i++) {
                applications.get(i).icon.setCallback(null);
            }
        }
    }
    
    /**
     * Remove the callback for the cached drawables or we leak the previous
     * Home screen on orientation change.
     */
    private void unbindCachedIconDrawables() {
        for (ApplicationInfo appInfo : mAppInfoCache.values()) {
            appInfo.icon.setCallback(null);
        }
    }

    /**
     * Add the footprint of the specified item to the occupied array
     */
    private void addOccupiedCells(boolean[][] occupied, int screen,
            ItemInfo item) {
        if (item.screen == screen) {
            if (item == null || item.cellX == -1 || item.cellY == -1)
            	return;
            for (int xx = item.cellX; xx < item.cellX + item.spanX; xx++) {
                for (int yy = item.cellY; yy < item.cellY + item.spanY; yy++) {
                    occupied[xx][yy] = true;
                }
            }
        }
    }
    
    public List<ItemInfo> getAllItem(){
    	return mAllItem;
    }
    
    public void removeAllItem(ItemInfo info){
    	mAllItem.remove(info);
    }
    
    public void addAllItem(ItemInfo info){
    	mAllItem.add(info);
    }
    
    public List<ItemInfo> getDesktopItem(){
    	return mDesktopItems;
    }

    public void removeDesktopItem(ItemInfo info) {
        mDesktopItems.remove(info);
    }
    
    public void addDesktopItem(ItemInfo info){
    	mDesktopItems.add(info);
    }
    
//    public ArrayList<ItemInfo> getPanelItems() {
//		return mPanelItems;
//	}
    
//    public void addPanelItem(ItemInfo info){
//    	mPanelItems.add(info);
//    }
    
//    public void removePanelItem(ItemInfo info){
//    	mPanelItems.remove(info);
//    }

    /**
     * Make an ApplicationInfo object for an application
     */
    private ApplicationInfo getApplicationInfo(PackageManager manager, Intent intent,
                                                      Context context, boolean isUninstall,String packageName) {
        final ResolveInfo resolveInfo = manager.resolveActivity(intent, 0);

        if (resolveInfo == null) {
            return null;
        }
        
        final ApplicationInfo info = new ApplicationInfo();
        final ActivityInfo activityInfo = resolveInfo.activityInfo;
      
        
        Drawable customerIcon = Utilities.getCustomerIcon(context, intent);
        if(customerIcon != null){
        	info.icon = Utilities.createIconThumbnailForUnistall(customerIcon, context , packageName);
        } else {
        	info.icon = Utilities.createIconThumbnailForUnistall(activityInfo.loadIcon(manager), context, packageName);
        }
        info.grayIcon = Utilities.convertGrayImg(info.icon);
        info.smallIcon = Utilities.createSmallIcon(info.icon,context);
        
        if (info.title == null || info.title.length() == 0) {
            info.title = activityInfo.loadLabel(manager);
        }
        if (info.title == null) {
            info.title = "";
        }
        info.itemType = LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
        return info;
    }

    /**
     * Make an ApplicationInfo object for a sortcut
     */
//    private ApplicationInfo getApplicationInfoShortcut(Cursor c, Context context,
//            int iconTypeIndex, int iconPackageIndex, int iconResourceIndex, int iconIndex) {
//
//        final ApplicationInfo info = new ApplicationInfo();
//        info.itemType = LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT;
//
//        int iconType = c.getInt(iconTypeIndex);
//        switch (iconType) {
//            case LauncherSettings.Favorites.ICON_TYPE_RESOURCE:
//                String packageName = c.getString(iconPackageIndex);
//                String resourceName = c.getString(iconResourceIndex);
//                PackageManager packageManager = context.getPackageManager();
//                try {
//                    Resources resources = packageManager.getResourcesForApplication(packageName);
//                    final int id = resources.getIdentifier(resourceName, null, null);
//                    info.icon = Utilities.createIconThumbnail(resources.getDrawable(id), context);
//                } catch (Exception e) {
//                    info.icon = packageManager.getDefaultActivityIcon();
//                }
//                info.iconResource = new Intent.ShortcutIconResource();
//                info.iconResource.packageName = packageName;
//                info.iconResource.resourceName = resourceName;
//                info.customIcon = false;
//                break;
//            case LauncherSettings.Favorites.ICON_TYPE_BITMAP:
//                byte[] data = c.getBlob(iconIndex);
//                try {
//                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//                    info.icon = new FastBitmapDrawable(
//                            Utilities.createBitmapThumbnail(bitmap, context));
//                } catch (Exception e) {
//                    packageManager = context.getPackageManager();
//                    info.icon = packageManager.getDefaultActivityIcon();
//                }
//                info.filtered = true;
//                info.customIcon = true;
//                break;
//            default:
//                info.icon = context.getPackageManager().getDefaultActivityIcon();
//                info.customIcon = false;
//                break;
//        }
//        info.grayIcon = Utilities.convertGrayImg(info.icon);
//        info.smallIcon = Utilities.createSmallIcon(info.icon,context);
//        return info;
//    }
    
    /**
     * 往桌面上添加一个应用程序
     * @param launcher
     * @param packageName
     */
    synchronized void addPackage(Launcher launcher, String packageName) {
        if (packageName != null && packageName.length() > 0) {
            final PackageManager packageManager = launcher.getPackageManager();
            final List<ResolveInfo> matches = findActivitiesForPackage(packageManager, packageName);
            
            if (DEBUG_LOADERS) d(PACKAGE_TAG, "addPackage matches size = " + matches.size());
            
            if (matches.size() > 0) {
                final HashMap<ComponentName, ApplicationInfo> cache = mAppInfoCache;

                for (ResolveInfo info : matches) {
                	if(!Launcher.getInstance().checkFullScreen()){
                		Launcher.getInstance().getWorkspace().addCellLayout();
                	}
                	ApplicationInfo aInfo = makeAndCacheApplicationInfo(packageManager, cache, info, launcher);
                	aInfo.itemType = LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
                	aInfo.isUninstall = true;
                	aInfo.isBookCase = false;
                	int[] cell = new int[3];
                	launcher.getWorkspace().getVacantCell(cell);
                	aInfo.screen = cell[0];
                	aInfo.cellX = cell[1];
                	aInfo.cellY = cell[2];
//                	String name = info.activityInfo.packageName;
//                	aInfo.pkgName = name;
                	Log.d(PACKAGE_TAG, "aInfo.screen:" + aInfo.screen + ";aInfo.cellX:" + aInfo.cellX + ";aInfo.cellY:" + aInfo.cellY);
                	LauncherModel.addOrMoveItemInDatabase(launcher, aInfo, LauncherSettings.Favorites.CONTAINER_DESKTOP
                			, aInfo.screen, aInfo.cellX, aInfo.cellY);
                	View view =  launcher.createShortcut(aInfo);
                	launcher.getWorkspace().addInScreen(view,aInfo.screen, aInfo.cellX, aInfo.cellY, 1, 1, true);
                	addDesktopItem(aInfo);
                	addAllItem(aInfo);
                }
            }
        }
    }
    
    synchronized void addLostPackage(Launcher launcher, List<ResolveInfo> matches) {
//        if (packageName != null && packageName.length() > 0) {
            final PackageManager packageManager = launcher.getPackageManager(); 
            
            if (DEBUG_LOADERS) d(PACKAGE_TAG, "addPackage matches size = " + matches.size());
            
            if (matches.size() > 0) {
                final HashMap<ComponentName, ApplicationInfo> cache = mAppInfoCache;

                for (ResolveInfo info : matches) {
                	if(!Launcher.getInstance().checkFullScreen()){
                		Launcher.getInstance().getWorkspace().addCellLayout();
                	}
                	ApplicationInfo aInfo = makeAndCacheApplicationInfo(packageManager, cache, info, launcher);
                	aInfo.itemType = LauncherSettings.Favorites.ITEM_TYPE_APPLICATION;
                	aInfo.isUninstall = true;
                	aInfo.isBookCase = false;
                	int[] cell = new int[3];
                	launcher.getWorkspace().getVacantCell(cell);
                	aInfo.screen = cell[0];
                	aInfo.cellX = cell[1];
                	aInfo.cellY = cell[2];
//                	String name = info.activityInfo.packageName;
//                	aInfo.pkgName = name;
                	Log.d(PACKAGE_TAG, "aInfo.screen:" + aInfo.screen + ";aInfo.cellX:" + aInfo.cellX + ";aInfo.cellY:" + aInfo.cellY);
                	LauncherModel.addOrMoveItemInDatabase(launcher, aInfo, LauncherSettings.Favorites.CONTAINER_DESKTOP
                			, aInfo.screen, aInfo.cellX, aInfo.cellY);
                	View view =  launcher.createShortcut(aInfo);
                	launcher.getWorkspace().addInScreen(view,aInfo.screen, aInfo.cellX, aInfo.cellY, 1, 1, true);
                	addDesktopItem(aInfo);
                	addAllItem(aInfo);
                }
            }
//        }
    }
    
    
    /**
     * Adds an item to the DB if it was not created previously, or move it to a new
     * <container, screen, cellX, cellY>
     */
    static void addOrMoveItemInDatabase(Context context, ItemInfo item, long container,
            int screen, int cellX, int cellY) {
        if (item.container == ItemInfo.NO_ID) {
            addItemToDatabase(context, item, container, screen, cellX, cellY, false);
        } else {
            moveItemInDatabase(context, item, container, screen, cellX, cellY);
        }
    }
    
    
    /**
     * Move an item in the DB to a new <container, screen, cellX, cellY>
     */
    static void moveItemInDatabase(Context context, ItemInfo item, long container, int screen,
            int cellX, int cellY) {
        item.container = container;
        item.screen = screen;
        item.cellX = cellX;
        item.cellY = cellY;

        final ContentValues values = new ContentValues();
        final ContentResolver cr = context.getContentResolver();

        values.put(LauncherSettings.Favorites.CONTAINER, item.container);
        values.put(LauncherSettings.Favorites.CELLX, item.cellX);
        values.put(LauncherSettings.Favorites.CELLY, item.cellY);
        values.put(LauncherSettings.Favorites.SCREEN, item.screen);

        cr.update(LauncherSettings.Favorites.getContentUri(item.id, false), values, null, null);
    }
    


    
    /**
     * Returns true if the shortcuts already exists in the database.
     * we identify a shortcut by its title and intent.
     */
    static boolean shortcutExists(Context context, String title, Intent intent) {
        final ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(LauncherSettings.Favorites.CONTENT_URI,
            new String[] { "title", "intent" }, "title=? and intent=?",
            new String[] { title, intent.toUri(0) }, null);
        boolean result = false;
        try {
            result = c.moveToFirst();
        } finally {
            c.close();
        }
        return result;
    }
    
    /**
     * Add an item to the database in a specified container. Sets the container, screen, cellX and
     * cellY fields of the item. Also assigns an ID to the item.
     */
    static void addItemToDatabase(Context context, ItemInfo item, long container,
            int screen, int cellX, int cellY, boolean notify) {
        item.container = container;
        item.screen = screen;
        item.cellX = cellX;
        item.cellY = cellY;

        final ContentValues values = new ContentValues();
        final ContentResolver cr = context.getContentResolver();

        item.onAddToDatabase(values);

        Uri result = cr.insert(notify ? LauncherSettings.Favorites.CONTENT_URI :
                LauncherSettings.Favorites.CONTENT_URI_NO_NOTIFICATION, values);

        if (result != null) {
            item.id = Integer.parseInt(result.getPathSegments().get(1));
        }
    }
    
    static void addItemToDatabase(Context context, ItemInfo item){
    	addItemToDatabase(context, item, item.container, item.screen, item.cellX, item.cellY, false);
    }
    
    public static void updateBookcaseInDatabase(Context context, List<ApplicationInfo> infos){
    	if(infos != null && infos.size() > 0){
        	final ContentValues values = new ContentValues();
        	final ContentResolver cr = context.getContentResolver();
        	ContentProviderClient providerClient = cr.acquireContentProviderClient(LauncherSettings.Favorites.CONTENT_URI);
        	LauncherProvider provider = (LauncherProvider) providerClient.getLocalContentProvider();
        	if(provider != null){
        		provider.updateBookcaseBatch(infos);   
        	}
        	providerClient.release();
    	}
    }
 
    
    static void updateBatchInDatabase(Context context, List<ApplicationInfo> infos){
    	if(infos != null && infos.size() > 0){
        	final ContentValues values = new ContentValues();
        	final ContentResolver cr = context.getContentResolver();
        	ContentProviderClient providerClient = cr.acquireContentProviderClient(LauncherSettings.Favorites.CONTENT_URI);
        	LauncherProvider provider = (LauncherProvider) providerClient.getLocalContentProvider();
        	if(provider != null){
        		provider.updateBatch(infos);   
        	}
        	providerClient.release();
    	}
    }
    
    static void addItemByBatch(Context context, List<ApplicationInfo> infos, Uri uri){
    	if(infos != null && infos.size() > 0){
        	final ContentValues values = new ContentValues();
        	final ContentResolver cr = context.getContentResolver();
        	ContentProviderClient providerClient = cr.acquireContentProviderClient(uri);
        	LauncherProvider provider = (LauncherProvider) providerClient.getLocalContentProvider();
        	if(provider != null){
        		provider.addBatch(infos, uri);  
        	}
        	providerClient.release();
    	}
    }
    
    static void DeleteItemByBatch(Context context, List<ApplicationInfo> infos, Uri uri){
    	if(infos != null && infos.size() > 0){
        	final ContentValues values = new ContentValues();
        	final ContentResolver cr = context.getContentResolver();
        	ContentProviderClient providerClient = cr.acquireContentProviderClient(uri);
        	LauncherProvider provider = (LauncherProvider) providerClient.getLocalContentProvider();
        	if(provider != null){
        		provider.deleteBatch(infos, uri);   
        	}
        	providerClient.release();
    	}
    }

    /**
     * Update an item to the database in a specified container.
     */
    static void updateItemInDatabase(Context context, ItemInfo item) {
        final ContentValues values = new ContentValues();
        final ContentResolver cr = context.getContentResolver();

        item.onAddToDatabase(values);

        cr.update(LauncherSettings.Favorites.getContentUri(item.id, false), values, null, null);
    }
    


    /**
     * Removes the specified item from the database
     * @param context
     * @param item
     */
    static void deleteItemFromDatabase(Context context, ItemInfo item) {
        final ContentResolver cr = context.getContentResolver();
        if (LauncherModel.DEBUG_LOADERS)
        	Log.d(LauncherModel.LOG_TAG,"deleteItemfromDatabase id = " + item.id);
        cr.delete(LauncherSettings.Favorites.getContentUri(item.id, false), null, null);
    }
    
//    static void deleteItemFromPanelDatabase(Context context, ItemInfo item) {
//        final ContentResolver cr = context.getContentResolver();
//        cr.delete(LauncherSettings.Panel.getContentUri(item.id, false), null, null);
//        if (LauncherModel.DEBUG_LOADERS)
//        	Log.d(LauncherModel.LOG_TAG,"deleteItemFromPanelDatabase id = " + item.id);
//    }
    
}
