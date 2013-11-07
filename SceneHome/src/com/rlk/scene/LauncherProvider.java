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

import static android.util.Log.w;

import android.appwidget.AppWidgetHost;
import android.content.ContentProvider;
import android.content.Context;
import android.content.ContentValues;
import android.content.Intent;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.content.res.TypedArray;
import android.content.pm.PackageManager;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;
import android.util.Xml;
import android.util.AttributeSet;
import android.net.Uri;
import android.text.TextUtils;
import android.os.*;
import android.provider.Settings;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParser; 

import com.rlk.scene.R;
import com.rlk.scene.LauncherSettings.BookCase;
import com.rlk.scene.LauncherSettings.Favorites;

public class LauncherProvider extends ContentProvider {
    private static final String LOG_TAG = "IphoneLauncherProvider";
    private static final boolean LOGD = true;
    private static final boolean DEBUG = true;

    private static final String DATABASE_NAME = "launcher.db";
    
    private static final int DATABASE_VERSION = 5;

    static final String AUTHORITY = "com.rlk.launcher.settings";
    
    static final String EXTRA_BIND_SOURCES = "com.android.launcher.settings.bindsources";
    static final String EXTRA_BIND_TARGETS = "com.android.launcher.settings.bindtargets";

    static final String TABLE_FAVORITES = "favorites";
    static final String TABLE_PANEL = "panel";
    static final String TABLE_BOOKCASE = "bookcase";
    static final String TABLE_GESTURES = "gestures";
    static final String PARAMETER_NOTIFY = "notify";

    /**
     * {@link Uri} triggered at any registered {@link android.database.ContentObserver} when
     * {@link AppWidgetHost#deleteHost()} is called during database creation.
     * Use this to recall {@link AppWidgetHost#startListening()} if needed.
     */
    static final Uri CONTENT_APPWIDGET_RESET_URI =
            Uri.parse("content://" + AUTHORITY + "/appWidgetReset");
    
    private SQLiteOpenHelper mOpenHelper;

    public boolean onCreate() {
        if (LOGD) Log.d(LOG_TAG, "onCreate");
        mOpenHelper = new DatabaseHelper(getContext());
        mOpenHelper.getWritableDatabase();
        return true;
    }

    @Override
    public String getType(Uri uri) {
        SqlArguments args = new SqlArguments(uri, null, null);
        if (TextUtils.isEmpty(args.where)) {
            return "vnd.android.cursor.dir/" + args.table;
        } else {
            return "vnd.android.cursor.item/" + args.table;
        }
    }


	public void updateBookcaseBatch(List<ApplicationInfo> infos) {
		if(DEBUG) Log.d("test_flag", "updateBatch infos.size = " + infos.size());
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			for(ApplicationInfo info : infos){
				db.execSQL("update favorites set isbookcase=? where intent=?", 
						new Object[]{info.isBookCase, info.intent.toUri(0)});
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			Log.e(LOG_TAG,"LauncherProvider UpdateBatch error");
		} finally{
			db.endTransaction();
		}
	}
    
	public void updateBatch(List<ApplicationInfo> infos) {
		if(DEBUG) Log.d("test_flag", "updateBatch infos.size = " + infos.size());
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			for(ApplicationInfo info : infos){
				db.execSQL("update favorites set cellX=?,cellY=?,container=?,screen=?,isbookcase=? where _id=?", 
						new Object[]{info.cellX, info.cellY, info.container, info.screen, info.isBookCase, info.id});
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			Log.e(LOG_TAG,"LauncherProvider UpdateBatch error");
		} finally{
			db.endTransaction();
		}
	}
	public void updateBookCaseBatch(List<ApplicationInfo> infos) {
		if(DEBUG) Log.d("test_flag", "updateBatch infos.size = " + infos.size());
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			for(ApplicationInfo info : infos){
				db.execSQL("update bookcase set cellX=?,cellY=?,cellSum=? where _id=?", 
						new Object[]{info.cellX, info.cellY, info.cellX*10+info.cellY, info.id});
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			Log.e(LOG_TAG,"LauncherProvider UpdateBatch error");
		} finally{
			db.endTransaction();
		}
	}
	public void deleteBookBatch(List<ApplicationInfo> infos, Uri uri) {
		SqlArguments args = new SqlArguments(uri);
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			for(ApplicationInfo info : infos){
				db.delete(args.table, "cellSum=?", new String[]{info.cellX*10 + info.cellY + ""});
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			Log.e(LOG_TAG,"LauncherProvider UpdateBatch error");
		} finally{
			db.endTransaction();
		}
	}
	public void insertBookData(ApplicationInfo info){
		ContentValues values = new ContentValues();
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		values.put(LauncherSettings.BookCase.CELLX, info.cellX);
		values.put(LauncherSettings.BookCase.CELLY, info.cellY);
		values.put(LauncherSettings.BookCase.CELLSUM, info.cellX*10 + info.cellY);
		values.put(LauncherSettings.BookCase.TITLE, info.title.toString());
		values.put(LauncherSettings.BookCase.PACKAGENAME, info.pkgName);
		values.put(LauncherSettings.BookCase.INTENT, info.intent.toUri(0));
		db.insert(TABLE_BOOKCASE, null, values);
	} 
	
	public void addBatch(List<ApplicationInfo> infos, Uri uri) {
		
		SqlArguments args = new SqlArguments(uri);
		ContentValues initialValues = new ContentValues();
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			for(ApplicationInfo info : infos){
				info.onAddToDatabase(initialValues);
				final long rowId = db.insert(args.table, null, initialValues);
				info.id = rowId;
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			Log.e(LOG_TAG,"LauncherProvider UpdateBatch error");
		} finally{
			db.endTransaction();
		}
	}
	
	public void deleteBatch(List<ApplicationInfo> infos, Uri uri) {

		SqlArguments args = new SqlArguments(uri);
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		db.beginTransaction();
		try {
			for(ApplicationInfo info : infos){
				final int delCount = db.delete(args.table, "_id=?", new String[]{info.id + ""});
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			Log.e(LOG_TAG,"LauncherProvider UpdateBatch error");
		} finally{
			db.endTransaction();
		}
	}

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {

        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(args.table);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Cursor result = qb.query(db, projection, args.where, args.args, null, null, sortOrder);
        result.setNotificationUri(getContext().getContentResolver(), uri);

        return result;
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        SqlArguments args = new SqlArguments(uri);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final long rowId = db.insert(args.table, null, initialValues);
        if (rowId <= 0) return null;

        uri = ContentUris.withAppendedId(uri, rowId);
        sendNotify(uri);

        return uri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SqlArguments args = new SqlArguments(uri);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            int numValues = values.length;
            for (int i = 0; i < numValues; i++) {
                if (db.insert(args.table, null, values[i]) < 0) return 0;
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        sendNotify(uri);
        return values.length;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = db.delete(args.table, args.where, args.args);
        if (count > 0) sendNotify(uri);

        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SqlArguments args = new SqlArguments(uri, selection, selectionArgs);

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = db.update(args.table, values, args.where, args.args);
        if (count > 0) sendNotify(uri);

        return count;
    }

    private void sendNotify(Uri uri) {
        String notify = uri.getQueryParameter(PARAMETER_NOTIFY);
        if (notify == null || "true".equals(notify)) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String TAG_FAVORITES = "favorites";
        private static final String TAG_FAVORITE = "favorite"; 

        private final Context mContext;
        private final AppWidgetHost mAppWidgetHost;

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mContext = context;
            mAppWidgetHost = new AppWidgetHost(context, Launcher.APPWIDGET_HOST_ID);
        }

        /**
         * Send notification that we've deleted the {@link AppWidgetHost},
         * probably as part of the initial database creation. The receiver may
         * want to re-call {@link AppWidgetHost#startListening()} to ensure
         * callbacks are correctly set.
         */
        private void sendAppWidgetResetNotify() {
            final ContentResolver resolver = mContext.getContentResolver();
            resolver.notifyChange(CONTENT_APPWIDGET_RESET_URI, null);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            if (LOGD) Log.d(LOG_TAG, "creating new launcher database");
            
            db.execSQL("CREATE TABLE favorites (" +
                    "_id INTEGER PRIMARY KEY," +
                    "title TEXT," +
                    "packageName TEXT," +
                    "isbookcase INTEGER," +
                    "intent TEXT," +
                    "isuninstall INTEGER," +
                    "launcherCount INTEGER," +
                    "container INTEGER," +
                    "screen INTEGER," +
                    "cellX INTEGER," +
                    "cellY INTEGER," +
                    "spanX INTEGER," +
                    "spanY INTEGER," +
                    "itemType INTEGER," +
                    "appWidgetId INTEGER NOT NULL DEFAULT -1," +
                    "isShortcut INTEGER," +
                    "iconType INTEGER," +
                    "iconPackage TEXT," +
                    "iconResource TEXT," +
                    "icon BLOB," +
                    "uri TEXT," +
                    "itemIds TEXT," +
                    "displayMode INTEGER" +
                    ");");
              
             db.execSQL("CREATE TABLE bookcase (" +
                     "_id INTEGER PRIMARY KEY," +
                     "title TEXT," +
                     "packageName TEXT," +
                     "intent TEXT," +
                     "screen INTEGER," +
                     "cellX INTEGER," +
                     "cellY INTEGER," +
                     "cellSum INTEGER" + 
                     ");"); 
             
             db.execSQL("CREATE TABLE gestures (" +
                    "_id INTEGER PRIMARY KEY," +
                    "title TEXT," +
                    "intent TEXT," +
                    "itemType INTEGER," +
                    "iconType INTEGER," +
                    "iconPackage TEXT," +
                    "iconResource TEXT," +
                    "icon BLOB" +
                    ");");

            // Database was just created, so wipe any previous widgets
            if (mAppWidgetHost != null) {
                mAppWidgetHost.deleteHost();
                sendAppWidgetResetNotify();
            }
            if (!convertDatabase(db)) {
                // Populate favorites table with initial favorites
//                loadFavorites(db);
            	loadIphoneFavorites(db);
            }
        }

        private boolean convertDatabase(SQLiteDatabase db) {
            if (LOGD) Log.d(LOG_TAG, "converting database from an older format, but not onUpgrade");
            boolean converted = false;

            final Uri uri = Uri.parse("content://" + Settings.AUTHORITY +
                    "/old_favorites?notify=true");
            final ContentResolver resolver = mContext.getContentResolver();
            Cursor cursor = null;

            try {
                cursor = resolver.query(uri, null, null, null, null);
            } catch (Exception e) {
	            // Ignore
            }

            // We already have a favorites database in the old provider
            if (cursor != null && cursor.getCount() > 0) {
                try {
                    converted = copyFromCursor(db, cursor) > 0;
                } finally {
                    cursor.close();
                }

                if (converted) {
                    resolver.delete(uri, null, null);
                }
            }
            
            if (converted) {
                // Convert widgets from this import into widgets
                if (LOGD) Log.d(LOG_TAG, "converted and now triggering widget upgrade");
                convertWidgets(db);
            }

            return converted;
        }

        private int copyFromCursor(SQLiteDatabase db, Cursor c) {
            final int idIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites._ID);
            final int intentIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.INTENT);
            final int isUninstallIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ISUNINSTALL);
            final int launcherCountIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.LAUNCHERCOUNT);
            final int titleIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.TITLE);
            final int pkgName = c.getColumnIndexOrThrow(LauncherSettings.Favorites.PACKAGENAME);
            final int iconTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON_TYPE);
            final int iconIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON);
            final int iconPackageIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON_PACKAGE);
            final int iconResourceIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ICON_RESOURCE);
            final int containerIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CONTAINER);
            final int itemTypeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.ITEM_TYPE);
            final int screenIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.SCREEN);
            final int cellXIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLX);
            final int cellYIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.CELLY);
            final int uriIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.URI);
            final int displayModeIndex = c.getColumnIndexOrThrow(LauncherSettings.Favorites.DISPLAY_MODE);

            ContentValues[] rows = new ContentValues[c.getCount()];
            int i = 0;
            while (c.moveToNext()) {
                ContentValues values = new ContentValues(c.getColumnCount());
                values.put(LauncherSettings.Favorites._ID, c.getLong(idIndex));
                values.put(LauncherSettings.Favorites.INTENT, c.getString(intentIndex));
                values.put(LauncherSettings.Favorites.PACKAGENAME, c.getString(pkgName));
                values.put(LauncherSettings.Favorites.ISUNINSTALL, c.getString(isUninstallIndex));
                values.put(LauncherSettings.Favorites.LAUNCHERCOUNT, c.getString(launcherCountIndex));
                values.put(LauncherSettings.Favorites.TITLE, c.getString(titleIndex));
                values.put(LauncherSettings.Favorites.ICON_TYPE, c.getInt(iconTypeIndex));
                values.put(LauncherSettings.Favorites.ICON, c.getBlob(iconIndex));
                values.put(LauncherSettings.Favorites.ICON_PACKAGE, c.getString(iconPackageIndex));
                values.put(LauncherSettings.Favorites.ICON_RESOURCE, c.getString(iconResourceIndex));
                values.put(LauncherSettings.Favorites.CONTAINER, c.getInt(containerIndex));
                values.put(LauncherSettings.Favorites.ITEM_TYPE, c.getInt(itemTypeIndex));
                values.put(LauncherSettings.Favorites.APPWIDGET_ID, -1);
                values.put(LauncherSettings.Favorites.SCREEN, c.getInt(screenIndex));
                values.put(LauncherSettings.Favorites.CELLX, c.getInt(cellXIndex));
                values.put(LauncherSettings.Favorites.CELLY, c.getInt(cellYIndex));
                values.put(LauncherSettings.Favorites.URI, c.getString(uriIndex));
                values.put(LauncherSettings.Favorites.DISPLAY_MODE, c.getInt(displayModeIndex));
                rows[i++] = values;
            }

            db.beginTransaction();
            int total = 0;
            try {
                int numValues = rows.length;
                for (i = 0; i < numValues; i++) {
                    if (db.insert(TABLE_FAVORITES, null, rows[i]) < 0) {
                        return 0;
                    } else {
                        total++;
                    }
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

            return total;
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (LOGD) Log.d(LOG_TAG, "onUpgrade triggered version =" + oldVersion 
            		+ "  newVersion = " + newVersion);
            
            int version = oldVersion;
            if (version < 3) {
                // upgrade 1,2 -> 3 added appWidgetId column
                db.beginTransaction();
                try {
                    // Insert new column for holding appWidgetIds
                    db.execSQL("ALTER TABLE favorites " +
                        "ADD COLUMN appWidgetId INTEGER NOT NULL DEFAULT -1;");
                    db.execSQL("ALTER TABLE panel ADD COLUMN items TEXT");
                    db.setTransactionSuccessful();
                    version = 3;
                } catch (SQLException ex) {
                    // Old version remains, which means we wipe old data
                    Log.e(LOG_TAG, ex.getMessage(), ex);
                } finally {
                    db.endTransaction();
                }
                
                // Convert existing widgets only if table upgrade was successful
                if (version == 3) {
                    convertWidgets(db);
                }
            }

            if (version < 4) {
                version = 4;
            }

            if (version < 5) {
                if (updateContactsShortcuts(db)) {
                    version = 5;
                }
            }
            
            if (version != DATABASE_VERSION) {
                Log.w(LOG_TAG, "Destroying all old data.");
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANEL);
                db.execSQL("DROP TABLE IF EXISTS " + TABLE_GESTURES);
                onCreate(db);
            }
        }

		private boolean updateContactsShortcuts(SQLiteDatabase db) {
            Cursor c = null;
            final String selectWhere = buildOrWhereString(LauncherSettings.Favorites.ITEM_TYPE,
                    new int[] { LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT });

            db.beginTransaction();
            try {
                // Select and iterate through each matching widget
                c = db.query(TABLE_FAVORITES, new String[] { LauncherSettings.Favorites._ID,
                        LauncherSettings.Favorites.INTENT }, selectWhere, null, null, null, null);
                
                if (LOGD) Log.d(LOG_TAG, "found upgrade cursor count=" + c.getCount());
                
                final ContentValues values = new ContentValues();
                final int idIndex = c.getColumnIndex(LauncherSettings.Favorites._ID);
                final int intentIndex = c.getColumnIndex(LauncherSettings.Favorites.INTENT);
                
                while (c != null && c.moveToNext()) {
                    long favoriteId = c.getLong(idIndex);
                    final String intentUri = c.getString(intentIndex);
                    if (intentUri != null) {
                        try {
                            Intent intent = Intent.parseUri(intentUri, 0);
                            android.util.Log.d("Home", intent.toString());
                            final Uri uri = intent.getData();
                            final String data = uri.toString();
                            if (Intent.ACTION_VIEW.equals(intent.getAction()) &&
                                    (data.startsWith("content://contacts/people/") ||
                                    data.startsWith("content://com.android.contacts/contacts/lookup/"))) {

                                intent = new Intent("com.android.contacts.action.QUICK_CONTACT");
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                        Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

                                intent.setData(uri);
                                intent.putExtra("mode", 3);
                                intent.putExtra("exclude_mimes", (String[]) null);

                                values.clear();
                                values.put(LauncherSettings.Favorites.INTENT, intent.toUri(0));
    
                                String updateWhere = LauncherSettings.Favorites._ID + "=" + favoriteId;
                                db.update(TABLE_FAVORITES, values, updateWhere, null);                                
                            }
                        } catch (RuntimeException ex) {
                            Log.e(LOG_TAG, "Problem upgrading shortcut", ex);
                        } catch (URISyntaxException e) {
                            Log.e(LOG_TAG, "Problem upgrading shortcut", e);                            
                        }
                    }
                }
                
                db.setTransactionSuccessful();
            } catch (SQLException ex) {
                Log.w(LOG_TAG, "Problem while upgrading contacts", ex);
                return false;
            } finally {
                db.endTransaction();
                if (c != null) {
                    c.close();
                }
            }

            return true;
        }
        
        
        /**
         * Upgrade existing clock and photo frame widgets into their new widget
         * equivalents. This method allocates appWidgetIds, and then hands off to
         * LauncherAppWidgetBinder to finish the actual binding.
         */
        private void convertWidgets(SQLiteDatabase db) {
            final int[] bindSources = new int[] {
                    Favorites.ITEM_TYPE_WIDGET_CLOCK,
                    Favorites.ITEM_TYPE_WIDGET_PHOTO_FRAME,
            };
            
            
            final ArrayList<ComponentName> bindTargets = new ArrayList<ComponentName>();
            bindTargets.add(new ComponentName("com.android.alarmclock",
                    "com.android.alarmclock.AnalogAppWidgetProvider"));
            bindTargets.add(new ComponentName("com.android.camera",
                    "com.android.camera.PhotoAppWidgetProvider"));
            
            final String selectWhere = buildOrWhereString(Favorites.ITEM_TYPE, bindSources);
            
            Cursor c = null;
            boolean allocatedAppWidgets = false;
            
            db.beginTransaction();
            try {
                // Select and iterate through each matching widget
                c = db.query(TABLE_FAVORITES, new String[] { Favorites._ID },
                        selectWhere, null, null, null, null);
                
                if (LOGD) Log.d(LOG_TAG, "found upgrade cursor count="+c.getCount());
                
                final ContentValues values = new ContentValues();
                while (c != null && c.moveToNext()) {
                    long favoriteId = c.getLong(0);
                    
                    // Allocate and update database with new appWidgetId
                    try {
                        int appWidgetId = mAppWidgetHost.allocateAppWidgetId();
                        
                        if (LOGD) Log.d(LOG_TAG, "allocated appWidgetId="+appWidgetId+" for favoriteId="+favoriteId);
                        
                        values.clear();
                        values.put(LauncherSettings.Favorites.APPWIDGET_ID, appWidgetId);
                        
                        // Original widgets might not have valid spans when upgrading
                        values.put(LauncherSettings.Favorites.SPANX, 2);
                        values.put(LauncherSettings.Favorites.SPANY, 2);

                        String updateWhere = Favorites._ID + "=" + favoriteId;
                        db.update(TABLE_FAVORITES, values, updateWhere, null);
                        
                        allocatedAppWidgets = true;
                    } catch (RuntimeException ex) {
                        Log.e(LOG_TAG, "Problem allocating appWidgetId", ex);
                    }
                }
                
                db.setTransactionSuccessful();
            } catch (SQLException ex) {
                Log.w(LOG_TAG, "Problem while allocating appWidgetIds for existing widgets", ex);
            } finally {
                db.endTransaction();
                if (c != null) {
                    c.close();
                }
            }
            
            // If any appWidgetIds allocated, then launch over to binder
            if (allocatedAppWidgets) {
                launchAppWidgetBinder(bindSources, bindTargets);
            }
        }

        /**
         * Launch the widget binder that walks through the Launcher database,
         * binding any matching widgets to the corresponding targets. We can't
         * bind ourselves because our parent process can't obtain the
         * BIND_APPWIDGET permission.
         */
        private void launchAppWidgetBinder(int[] bindSources, ArrayList<ComponentName> bindTargets) {
            final Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.android.settings",
                    "com.android.settings.LauncherAppWidgetBinder"));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            
            final Bundle extras = new Bundle();
            extras.putIntArray(EXTRA_BIND_SOURCES, bindSources);
            extras.putParcelableArrayList(EXTRA_BIND_TARGETS, bindTargets);
            intent.putExtras(extras);
            
            mContext.startActivity(intent);
        }
        
        /**
         * Loads the default set of favorite packages from an xml file.
         *
         * @param db The database to write the values into
         */
//        private int loadFavorites(SQLiteDatabase db) {
//            Intent intent = new Intent(Intent.ACTION_MAIN, null);
//            intent.addCategory(Intent.CATEGORY_LAUNCHER);
//            ContentValues values = new ContentValues();
//
//            PackageManager packageManager = mContext.getPackageManager();
//            int i = 0;
//            try {
//                XmlResourceParser parser = mContext.getResources().getXml(R.xml.default_workspace);
//                AttributeSet attrs = Xml.asAttributeSet(parser);
//                XmlUtils.beginDocument(parser, TAG_FAVORITES);
//
//                final int depth = parser.getDepth();
//
//                int type;
//                while (((type = parser.next()) != XmlPullParser.END_TAG ||
//                        parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {
//
//                    if (type != XmlPullParser.START_TAG) {
//                        continue;
//                    }
//
//                    boolean added = false;
//                    final String name = parser.getName();
//
//                    TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.Favorite);
//
//                    values.clear();                    
//                    values.put(LauncherSettings.Favorites.CONTAINER,
//                            LauncherSettings.Favorites.CONTAINER_DESKTOP);
//                    values.put(LauncherSettings.Favorites.SCREEN,
//                            a.getString(R.styleable.Favorite_screen));
//                    values.put(LauncherSettings.Favorites.CELLX,
//                            a.getString(R.styleable.Favorite_x));
//                    values.put(LauncherSettings.Favorites.CELLY,
//                            a.getString(R.styleable.Favorite_y));
//                    
//                    if (TAG_FAVORITE.equals(name)) {
//                        added = addAppShortcut(db, values, a, packageManager, intent);
//                    } else if (TAG_SEARCH.equals(name)) {
//                        added = addSearchWidget(db, values);
//                    } else if (TAG_CLOCK.equals(name)) {
//                        added = addClockWidget(db, values);
//                    } else if (TAG_SHORTCUT.equals(name)) {
//                        added = addShortcut(db, values, a);
//                    }
//
//                    if (added) i++;
//
//                    a.recycle();
//                }
//            } catch (XmlPullParserException e) {
//                Log.w(LOG_TAG, "Got exception parsing favorites.", e);
//            } catch (IOException e) {
//                Log.w(LOG_TAG, "Got exception parsing favorites.", e);
//            }
//
//            return i;
//        }

		private void loadIphoneFavorites(SQLiteDatabase db) {
			Intent intent = new Intent(Intent.ACTION_MAIN, null);
			intent.addCategory(Intent.CATEGORY_LAUNCHER);
			ContentValues values = new ContentValues();

			PackageManager packageManager = mContext.getPackageManager();
			int i = 0;
			int default_load_count = 0;
			int[] default_load_value = new int[50];
			String[] default_packagename = new String[50];
			String[] default_classname = new String[50];
			try {
				XmlResourceParser bookCaseParser = mContext.getResources().getXml(R.xml.default_bookcase);
				AttributeSet bookCaseAttrs = Xml.asAttributeSet(bookCaseParser);
				XmlUtils.beginDocument(bookCaseParser, TAG_FAVORITES);
				int depth,type ;
				depth = bookCaseParser.getDepth();
				while (((type = bookCaseParser.next()) != XmlPullParser.END_TAG || bookCaseParser
						.getDepth() > depth)
						&& type != XmlPullParser.END_DOCUMENT) {
					if (type != XmlPullParser.START_TAG) {
						continue;
					}
					final String bookCaseParserName = bookCaseParser.getName();
					TypedArray typeArray = mContext.obtainStyledAttributes(bookCaseAttrs,
							R.styleable.Favorite);
					values.clear();
					values.put(LauncherSettings.BookCase.CELLX,
							typeArray.getString(R.styleable.Favorite_x));
					values.put(LauncherSettings.BookCase.CELLY,
							typeArray.getString(R.styleable.Favorite_y));
					values.put(LauncherSettings.BookCase.CELLSUM, 
							typeArray.getInt(R.styleable.Favorite_x, -1)*10 + 
							typeArray.getInt(R.styleable.Favorite_y, -1));

					if (TAG_FAVORITE.equals(bookCaseParserName)) {
						addIphoneBookCase(db, values, typeArray,
								packageManager, intent);
					}
					typeArray.recycle();
				}
				
				XmlResourceParser parser = mContext.getResources().getXml(
						R.xml.default_workspace);
				AttributeSet attrs = Xml.asAttributeSet(parser);
				XmlUtils.beginDocument(parser, TAG_FAVORITES);

				depth = parser.getDepth();
 
				while (((type = parser.next()) != XmlPullParser.END_TAG || parser
						.getDepth() > depth)
						&& type != XmlPullParser.END_DOCUMENT) {

					if (type != XmlPullParser.START_TAG) {
						continue;
					}

					boolean added = false;
					final String name = parser.getName();

					TypedArray a = mContext.obtainStyledAttributes(attrs,
							R.styleable.Favorite);

					values.clear();
					values.put(LauncherSettings.Favorites.CONTAINER,
							LauncherSettings.Favorites.CONTAINER_DESKTOP);
					values.put(LauncherSettings.Favorites.SCREEN,
							a.getString(R.styleable.Favorite_screen));
					values.put(LauncherSettings.Favorites.CELLX,
							a.getString(R.styleable.Favorite_x));
					values.put(LauncherSettings.Favorites.ISBOOKCASE, 
							a.getString(R.styleable.Favorite_bookcase));
					values.put(LauncherSettings.Favorites.CELLY,
							a.getString(R.styleable.Favorite_y));

					if (TAG_FAVORITE.equals(name)) {
						added = addIphoneShortCut(db, values, a,
								packageManager, intent);
					}
					if (added) {
						int screen = a.getInt(R.styleable.Favorite_screen, 0);
						int cellx = a.getInt(R.styleable.Favorite_x, 0);
						int celly = a.getInt(R.styleable.Favorite_y, 0);
						default_packagename[i] = a
								.getString(R.styleable.Favorite_packageName);
						default_classname[i] = a
								.getString(R.styleable.Favorite_className); 
						
						default_load_value[i] = screen
								* Launcher.NUMBER_CELLS_X
								* Launcher.NUMBER_CELLS_Y + celly
								* Launcher.NUMBER_CELLS_X + cellx; 
						
						Log.i(LOG_TAG, "cellx=" + cellx
								+ " default_load_value[" + i + "]="
								+ default_load_value[i]
								+ " default_packagename="
								+ default_packagename[i]);
						i++;
					}
					default_load_count = i;
					a.recycle();
				}
			} catch (XmlPullParserException e) {
				Log.w(LOG_TAG, "Got exception parsing favorites.", e);
			} catch (IOException e) {
				Log.w(LOG_TAG, "Got exception parsing favorites.", e);
			}

			// int i = 0;
			int add_count = 0;

			Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
			mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

			final List<ResolveInfo> apps = packageManager
					.queryIntentActivities(mainIntent, 0);

			if (apps != null) { 
				final int count = apps.size(); 
				for (int j = 0; j < count; j++) { 
					ResolveInfo resolveInfo = apps.get(j);
					ActivityInfo info = resolveInfo.activityInfo; 
					boolean added = false; 
					String packageName = info.applicationInfo.packageName;
					String className = info.name;
					ComponentName component = new ComponentName(packageName,
							className);
					Log.i(LOG_TAG, "zqs packageName=" + packageName
							+ "className=" + className);  
		 
					final int pagecount = Launcher.NUMBER_CELLS_X
							* Launcher.NUMBER_CELLS_Y;
					int screen = add_count / pagecount;
					int cellx = add_count % Launcher.NUMBER_CELLS_X;
					int celly = (add_count - screen * pagecount)
							/ Launcher.NUMBER_CELLS_X;
					boolean default_founded = false;

					for (int k = 0; k < default_load_count; k++) {
						if ((default_packagename[k] != null)
								&& (default_classname[k] != null)) {
							if (default_packagename[k].equals(component
									.getPackageName())
									&& default_classname[k].equals(component
											.getClassName())) {
								default_founded = true;
								Log.i(LOG_TAG,
										"default_packagename="
												+ component.getPackageName()
												+ " getShortClassName="
												+ component.getClassName());
								break;
							}
						}
					}
					if (default_founded) {
						continue;
					}

					for (int k = 0; k < default_load_count; k++) {
						if (default_load_value[k] == add_count) {
							add_count++;
							screen = add_count / pagecount;
							cellx = add_count % Launcher.NUMBER_CELLS_X;
							celly = (add_count - screen * pagecount)
									/ Launcher.NUMBER_CELLS_X;
						}
					}
					Log.i(LOG_TAG, "screen=" + screen + "cellx=" + cellx
							+ "celly=" + celly);
					values.clear();
					boolean isUninstall = false;
					int flags = info.applicationInfo.flags;
					if ((flags & android.content.pm.ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
						isUninstall = true;
					} else if ((flags & android.content.pm.ApplicationInfo.FLAG_SYSTEM) == 0) {
						isUninstall = true;
					}

					values.put(LauncherSettings.Favorites.CONTAINER,
							LauncherSettings.Favorites.CONTAINER_DESKTOP);
					values.put(LauncherSettings.Favorites.SCREEN, screen);
					values.put(LauncherSettings.Favorites.CELLX, cellx);
					values.put(LauncherSettings.Favorites.CELLY, celly);
					values.put(LauncherSettings.Favorites.ISBOOKCASE, 0);
					if (isUninstall) {
						values.put(
								LauncherSettings.Favorites.ISUNINSTALL,
								LauncherSettings.BaseLauncherColumns.ISUNINSTALL_TRUE);
					} else {
						values.put(
								LauncherSettings.Favorites.ISUNINSTALL,
								LauncherSettings.BaseLauncherColumns.ISUNINSTALL_FALSE);
					}

					added = addIphoneShortCut(db, values, info, packageManager,
							intent);

					if (added) {
						i++;
						add_count++;
					}
					;

				}
			}
			Log.i(LOG_TAG, "i=" + i); 
			return;
		}
 
		private boolean addIphoneBookCase(SQLiteDatabase db, ContentValues values, TypedArray a,
	                PackageManager packageManager, Intent intent) {
			ActivityInfo info;		
			String packageName = a.getString(R.styleable.Favorite_packageName);
            String className = a.getString(R.styleable.Favorite_className);	
            try {
                ComponentName cn;
                try {
                    cn = new ComponentName(packageName, className);
                    info = packageManager.getActivityInfo(cn, 0);
                } catch (PackageManager.NameNotFoundException nnfe) {
                    String[] packages = packageManager.currentToCanonicalPackageNames(
                        new String[] { packageName });
                    cn = new ComponentName(packages[0], className);
                    info = packageManager.getActivityInfo(cn, 0);
                }

                intent.setComponent(cn);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
	            values.put(BookCase.INTENT, intent.toUri(0));
	            values.put(BookCase.TITLE, info.loadLabel(packageManager).toString());
	            values.put(BookCase.PACKAGENAME, packageName);
	            db.insert(TABLE_BOOKCASE, null, values);
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(LOG_TAG, "Unable to add BookCase: " + packageName +
                        "/" + className, e);
                return false;
            }               
            return true;
        }			
		
        private boolean addIphoneShortCut(SQLiteDatabase db, ContentValues values, ActivityInfo info,
                PackageManager packageManager, Intent intent) {
        	String packageName = info.applicationInfo.packageName; 
        	Log.d(LOG_TAG, "packageName= " + packageName);
        	if(packageName.equals("com.android.stk")||packageName.equals("com.android.stk2")
        			||packageName.equals("com.rlk.scene")){
        		return false;
        	} 
        	String className = info.name;
            ComponentName cn = new ComponentName(packageName, className);
            intent.setComponent(cn);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            values.put(Favorites.INTENT, intent.toUri(0));
            values.put(Favorites.TITLE, info.loadLabel(packageManager).toString());
            values.put(Favorites.PACKAGENAME, packageName);
            values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_APPLICATION);
            values.put(Favorites.SPANX, 1);
            values.put(Favorites.SPANY, 1);
            db.insert(TABLE_FAVORITES, null, values);
            
            return true;
        }
        private boolean addIphoneShortCut(SQLiteDatabase db, ContentValues values, TypedArray a,
                PackageManager packageManager, Intent intent) {
            ActivityInfo info;
            String packageName = a.getString(R.styleable.Favorite_packageName);
            String className = a.getString(R.styleable.Favorite_className);				
			try {
				ComponentName cn;
				try {
					cn = new ComponentName(packageName, className);
					info = packageManager.getActivityInfo(cn, 0);
				} catch (PackageManager.NameNotFoundException nnfe) {
                    String[] packages = packageManager.currentToCanonicalPackageNames(
                        new String[] { packageName });
					cn = new ComponentName(packages[0], className);
					info = packageManager.getActivityInfo(cn, 0);
				}

				intent.setComponent(cn);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
	            values.put(Favorites.INTENT, intent.toUri(0));
	            values.put(Favorites.TITLE, info.loadLabel(packageManager).toString());
	            values.put(Favorites.PACKAGENAME, packageName);
	            values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_APPLICATION);
	            values.put(Favorites.SPANX, 1);
	            values.put(Favorites.SPANY, 1);
	            db.insert(TABLE_FAVORITES, null, values);
            } catch (PackageManager.NameNotFoundException e) {
                Log.w(LOG_TAG, "Unable to add favorite: " + packageName +
                        "/" + className, e);
                return false;
            }            
            return true;
        }
//        private boolean addAppShortcut(SQLiteDatabase db, ContentValues values, TypedArray a,
//                PackageManager packageManager, Intent intent) {

//            ActivityInfo info;
//            String packageName = a.getString(R.styleable.Favorite_packageName);
//            String className = a.getString(R.styleable.Favorite_className);
//            try {
//                ComponentName cn = new ComponentName(packageName, className);
//                info = packageManager.getActivityInfo(cn, 0);
//                intent.setComponent(cn);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
//                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
//                values.put(Favorites.INTENT, intent.toUri(0));
//                values.put(Favorites.TITLE, info.loadLabel(packageManager).toString());
//                values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_APPLICATION);
//                values.put(Favorites.SPANX, 1);
//                values.put(Favorites.SPANY, 1);
//                db.insert(TABLE_FAVORITES, null, values);
//            } catch (PackageManager.NameNotFoundException e) {
//                Log.w(LOG_TAG, "Unable to add favorite: " + packageName +
//                        "/" + className, e);
//                return false;
//            }
//            return true;
//        }
        
//        private boolean addShortcut(SQLiteDatabase db, ContentValues values, TypedArray a) {
//            Resources r = mContext.getResources();
            
//            final int iconResId = a.getResourceId(R.styleable.Favorite_icon, 0);
//            final int titleResId = a.getResourceId(R.styleable.Favorite_title, 0);
            
//            Intent intent;
//            String uri = null;
//            try {
//                uri = a.getString(R.styleable.Favorite_uri);
//                intent = Intent.parseUri(uri, 0);
//            } catch (URISyntaxException e) {
//                w(LauncherModel.LOG_TAG, "Shortcut has malformed uri: " + uri);
//                return false; // Oh well
//            }
            
//            if (iconResId == 0 || titleResId == 0) {
//                w(LauncherModel.LOG_TAG, "Shortcut is missing title or icon resource ID");
//                return false;
//            }
            
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            values.put(Favorites.INTENT, intent.toUri(0));
//            values.put(Favorites.TITLE, r.getString(titleResId)); 
//            values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_SHORTCUT);
//            values.put(Favorites.SPANX, 1);
//            values.put(Favorites.SPANY, 1);
//            values.put(Favorites.ICON_TYPE, Favorites.ICON_TYPE_RESOURCE);
//            values.put(Favorites.ICON_PACKAGE, mContext.getPackageName());
//            values.put(Favorites.ICON_RESOURCE, mContext.getResources().getResourceName(iconResId));

//            db.insert(TABLE_FAVORITES, null, values);

//            return true;
//        }

//        private boolean addSearchWidget(SQLiteDatabase db, ContentValues values) {
//            // Add a search box
//            values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_WIDGET_SEARCH);
//            values.put(Favorites.SPANX, 4);
//            values.put(Favorites.SPANY, 1);
//            db.insert(TABLE_FAVORITES, null, values);
//
//            return true;
//        }

//        private boolean addClockWidget(SQLiteDatabase db, ContentValues values) {
//            final int[] bindSources = new int[] {
//                    Favorites.ITEM_TYPE_WIDGET_CLOCK,
//            };
//
//            final ArrayList<ComponentName> bindTargets = new ArrayList<ComponentName>();
//            bindTargets.add(new ComponentName("com.android.alarmclock",
//                    "com.android.alarmclock.AnalogAppWidgetProvider"));
//
//            boolean allocatedAppWidgets = false;
//
//            // Try binding to an analog clock widget
//            try {
//                int appWidgetId = mAppWidgetHost.allocateAppWidgetId();
//
//                values.put(Favorites.ITEM_TYPE, Favorites.ITEM_TYPE_WIDGET_CLOCK);
//                values.put(Favorites.SPANX, 2);
//                values.put(Favorites.SPANY, 2);
//                values.put(Favorites.APPWIDGET_ID, appWidgetId);
//                db.insert(TABLE_FAVORITES, null, values);
//
//                allocatedAppWidgets = true;
//            } catch (RuntimeException ex) {
//                Log.e(LOG_TAG, "Problem allocating appWidgetId", ex);
//            }
//
//            // If any appWidgetIds allocated, then launch over to binder
//            if (allocatedAppWidgets) {
//                launchAppWidgetBinder(bindSources, bindTargets);
//            }
//
//            return allocatedAppWidgets;
//        }
    }

    /**
     * Build a query string that will match any row where the column matches
     * anything in the values list.
     */
    static String buildOrWhereString(String column, int[] values) {
        StringBuilder selectWhere = new StringBuilder();
        for (int i = values.length - 1; i >= 0; i--) {
            selectWhere.append(column).append("=").append(values[i]);
            if (i > 0) {
                selectWhere.append(" OR ");
            }
        }
        return selectWhere.toString();
    }

    static class SqlArguments {
		public final String table;
        public final String where;
        public final String[] args;

        SqlArguments(Uri url, String where, String[] args) {
        	if(DEBUG) Log.d("database", "url.getPathSegments().size() = " + url.getPathSegments().size());
            if (url.getPathSegments().size() == 1) {
                this.table = url.getPathSegments().get(0);
                this.where = where;
                this.args = args;
            } else if (url.getPathSegments().size() != 2) {
                throw new IllegalArgumentException("Invalid URI: " + url);
            } else if (!TextUtils.isEmpty(where)) {
                throw new UnsupportedOperationException("WHERE clause not supported: " + url);
            } else {
                this.table = url.getPathSegments().get(0);
                this.where = "_id=" + ContentUris.parseId(url);                
                this.args = null;
            }
        }

        SqlArguments(Uri url) {
            if (url.getPathSegments().size() == 1) {
                table = url.getPathSegments().get(0);
                where = null;
                args = null;
            } else {
                throw new IllegalArgumentException("Invalid URI: " + url);
            }
        }
    }
}
