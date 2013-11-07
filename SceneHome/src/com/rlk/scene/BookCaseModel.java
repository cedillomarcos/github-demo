package com.rlk.scene;

import java.util.ArrayList; 
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.rlk.scene.items.BookCaseItem;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.drawable.Drawable; 
import android.net.Uri;
import android.util.Log;
import android.view.View;

public class BookCaseModel { 
//	private ArrayList<ItemInfo> mDesktopBookCaseItems;
	private Context mContext;  
	private String TAG = "BookCaseModel";     
	private PackageManager manager; 
	private ContentResolver contentResolver;
	public static int mShortAxisStartPadding = 26;
	public static int mLongAxisStartPadding = 135;
	public static int mCellWidth = 105;
	public static int mCellHeight = 117;
	public static int mShortAxisCells = 2;
	public static int mLongAxisCells = 4;
	private List<ApplicationInfo> mUpdateLocation;
	private boolean mPortrait;
	public static HashMap<Integer,ApplicationInfo> mBookmap;
    public static boolean[][] mOccupied;
	
	public BookCaseModel(Context context){
		mContext = context;
		contentResolver = mContext.getContentResolver();
		manager = mContext.getPackageManager();
		mUpdateLocation = new ArrayList<ApplicationInfo>();
		mBookmap = new HashMap<Integer,ApplicationInfo>();
		mOccupied = new boolean[mShortAxisCells][mLongAxisCells];
		mPortrait = true;
        if (mOccupied == null) {//���ڱ����ĸ���Ԫ��ʹ����
            if (mPortrait) {
                mOccupied = new boolean[mShortAxisCells][mLongAxisCells];
            } else {//����
                mOccupied = new boolean[mLongAxisCells][mShortAxisCells];
            }
        }
		initData();
	}
	
	public static int getBookCounts(){
		int count = 0;
		Set<Integer> mBookKeys = mBookmap
				.keySet();
		for (Integer i : mBookKeys) {
			final ApplicationInfo info = (ApplicationInfo) mBookmap
					.get(i);
			if (info != null) count++;
		}	
		return count;
	}
	
	public void refrashData(){
		final Cursor c = contentResolver.query(
				LauncherSettings.BookCase.CONTENT_URI, null, null,
				null, null);
		clearBookmap();
		
		try {
			final int idIndex = c
					.getColumnIndexOrThrow(LauncherSettings.BookCase._ID);
			final int intentIndex = c
					.getColumnIndexOrThrow(LauncherSettings.BookCase.INTENT);
			final int titleIndex = c
					.getColumnIndexOrThrow(LauncherSettings.BookCase.TITLE);
            final int pkgName = c.getColumnIndexOrThrow(LauncherSettings.BookCase.PACKAGENAME); 
			final int cellXIndex = c
					.getColumnIndexOrThrow(LauncherSettings.BookCase.CELLX);
			final int cellYIndex = c
					.getColumnIndexOrThrow(LauncherSettings.BookCase.CELLY);

			ApplicationInfo info = null;
			Intent intent = null;
			String intentDescription;
			while (c.moveToNext()) {
				intentDescription = c.getString(intentIndex);
				try {
					intent = Intent.parseUri(intentDescription, 0);
				} catch (java.net.URISyntaxException e) {
					continue;
				}
				info = getApplicationInfo(manager, intent, mContext,
						c.getString(pkgName));

				if (info == null) {
					info = new ApplicationInfo();
					info.intent = intent;
					info.icon = manager.getDefaultActivityIcon();
				}

				if (info != null) {  
					info.id = c.getLong(idIndex);
					info.title = c.getString(titleIndex);
					info.intent = intent; 
					info.cellX = c.getInt(cellXIndex);
					info.cellY = c.getInt(cellYIndex);
					mBookmap.put(info.cellX*10+info.cellY,info);
				}
			}
		} finally {
			c.close();
		}
		Set<Integer> mBookKeys = BookCaseModel.mBookmap
				.keySet();
//		Log.d(TAG, "refrashData mBookKeys=" + mBookKeys);
		MainActivity.instance.startBindBookCase(mBookmap); 
		getOccupiedCells();
	}
	
	private void clearBookmap(){
		for(int i=0;i<mShortAxisCells;i++){
			for(int j=0;j<mLongAxisCells;j++){
				mBookmap.put(i*10+j, null);
			}
		}
	}
	
	public void initData() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				final Cursor c = contentResolver.query(
						LauncherSettings.BookCase.CONTENT_URI, null, null,
						null, null);
				clearBookmap();
				
				try {
					final int idIndex = c
							.getColumnIndexOrThrow(LauncherSettings.BookCase._ID);
					final int intentIndex = c
							.getColumnIndexOrThrow(LauncherSettings.BookCase.INTENT);
					final int titleIndex = c
							.getColumnIndexOrThrow(LauncherSettings.BookCase.TITLE);
		            final int pkgName = c.getColumnIndexOrThrow(LauncherSettings.BookCase.PACKAGENAME); 
					final int cellXIndex = c
							.getColumnIndexOrThrow(LauncherSettings.BookCase.CELLX);
					final int cellYIndex = c
							.getColumnIndexOrThrow(LauncherSettings.BookCase.CELLY);

					ApplicationInfo info = null;
					Intent intent = null;
					String intentDescription;
					while (c.moveToNext()) {
						intentDescription = c.getString(intentIndex);
						try {
							intent = Intent.parseUri(intentDescription, 0);
						} catch (java.net.URISyntaxException e) {
							continue;
						}
						info = getApplicationInfo(manager, intent, mContext,
								c.getString(pkgName));

						if (info == null) {
							info = new ApplicationInfo();
							info.intent = intent;
							info.icon = manager.getDefaultActivityIcon();
						}

						if (info != null) {
							info.id = c.getLong(idIndex);

							info.title = c.getString(titleIndex);
							info.intent = intent;
							info.cellX = c.getInt(cellXIndex);
							info.cellY = c.getInt(cellYIndex);
							mBookmap.put(info.cellX*10+info.cellY,info);
						}
					}
				} finally {
					c.close();
				} 
				MainActivity.instance.runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						Set<Integer> mBookKeys = BookCaseModel.mBookmap
								.keySet();
//						Log.d(TAG, "initData mBookKeys=" + mBookKeys);
						MainActivity.instance.startBindBookCase(mBookmap); 
						getOccupiedCells();
					}
				});
				
			}
		}).start(); 
	}
	
	
	private ApplicationInfo getApplicationInfo(PackageManager manager,
			Intent intent, Context context, String packageName) {
		final ResolveInfo resolveInfo = manager.resolveActivity(intent, 0);

		if (resolveInfo == null) {
			return null;
		}

		final ApplicationInfo info = new ApplicationInfo();
		final ActivityInfo activityInfo = resolveInfo.activityInfo;

		Drawable customerIcon = Utilities.getCustomerIcon(context, intent);
		if (customerIcon != null) {
			info.icon = Utilities.createIconThumbnailForUnistall(customerIcon,
					context, packageName);
		} else {
			info.icon = Utilities.createIconThumbnailForUnistall(
					activityInfo.loadIcon(manager), context, packageName);
		}

		if (info.title == null || info.title.length() == 0) {
			info.title = activityInfo.loadLabel(manager);
		}
		if (info.title == null) {
			info.title = "";
		}
		return info;
	}
	public int findVacantCell(){
		for (int i = 0; i < mShortAxisCells; i++) {
            for (int j = 0; j < mLongAxisCells; j++) {
//            	Log.d(TAG, "findVacantCell  i=" + i + ";j=" + j + ";mOccupied[i][j]" + mOccupied[i][j]);
            }
        }  
		for (int i = 0; i < mShortAxisCells; i++) {
            for (int j = 0; j < mLongAxisCells; j++) {
            	if(!mOccupied[i][j]) 
            		return i*10+j;
            }
        }  
		return -1;
	}
	public void getOccupiedCells(){
		for (int i = 0; i < mShortAxisCells; i++) {
            for (int j = 0; j < mLongAxisCells; j++) {
            	mOccupied[i][j] = false;
            }
        }
		Set<Integer> mBookKeys = mBookmap.keySet();
//        Log.d(TAG, "mBookKeys=" + mBookKeys);
        for(Integer i: mBookKeys){
        	int x = i/10;
        	int y = i%10;
        	if(mBookmap.get(i) == null){
        		mOccupied[x][y] = false;
        	}else{
        		mOccupied[x][y] = true;
        	} 
//        	Log.d(TAG, "getOccupiedCells key i=" + i + ";mOccupied=" + mOccupied[x][y]);
        }
	}
	public ApplicationInfo mDragInfo;
	public void onDragOver(int mX, int mY,ApplicationInfo dragInfo) { 
		if(dragInfo == null){
			return;
		} 
		getOccupiedCells();
		mDragInfo = dragInfo;
        int cellX = (int) ((mX - BookCaseModel.mShortAxisStartPadding)/BookCaseModel.mCellWidth);
		int cellY = (int) ((mY - BookCaseModel.mLongAxisStartPadding)/BookCaseModel.mCellHeight);
		if(mX < BookCaseModel.mShortAxisStartPadding){
			cellX = 0;
			mX = BookCaseModel.mShortAxisStartPadding;
		} 
		if(cellX > mShortAxisCells-1){
			cellX = mShortAxisCells-1;
			mX = (cellX + 1)*mCellWidth + BookCaseModel.mShortAxisStartPadding;
		}
		if(mY < BookCaseModel.mLongAxisStartPadding){
			cellY = 0;
			mY = BookCaseModel.mLongAxisStartPadding;
		}
		if(cellY > mLongAxisCells-1){
			cellY = mLongAxisCells-1;
			mY = (cellY+2)*mCellHeight + 16;
		}
		int dragCellX = dragInfo.cellX;
        int dragCellY = dragInfo.cellY;
        int moveNum = cellY*mShortAxisCells + cellX + 1;
        int dragNum = dragCellY*mShortAxisCells + dragCellX + 1;
        mUpdateLocation.clear();
//        Log.d(TAG, "cellX=" + cellX + ";cellY=" + cellY + ";drogCellX=" + dragCellX + ";drogCellY=" + dragCellY);
        if(mOccupied[cellX][cellY]){ 
        for(int i = 0; i < Math.abs(moveNum - dragNum); i++){

        	if(moveNum > dragNum){//drag down
        		int changeNum = dragNum + 1 + i;
            	int changeCellX = (changeNum -1) %mShortAxisCells;
            	int changeCellY = (changeNum - 1)/mShortAxisCells;
            	ApplicationInfo changeInfo = mBookmap.get(changeCellX*10 + changeCellY);
            	if(changeInfo != null){
            		if(changeCellX -1 < 0){
            			changeInfo.cellX = mShortAxisCells-1;
            			changeInfo.cellY = changeCellY -1;
            		}else{
            			changeInfo.cellX = changeCellX -1;
                    	changeInfo.cellY = changeCellY;	
            		}
                	mBookmap.put(changeInfo.cellX*10 + changeInfo.cellY, changeInfo);
                	mOccupied[changeInfo.cellX][changeInfo.cellY] = true;
                	mUpdateLocation.add(changeInfo);
                	}else{
                		if(changeCellX -1 < 0){ 
                			mBookmap.put((mShortAxisCells-1)*10 + changeCellY -1, null);
                        	mOccupied[mShortAxisCells-1][changeCellY -1] = false;
                		}else{
                			mBookmap.put((changeCellX -1)*10 + changeCellY, null);
                        	mOccupied[changeCellX -1][changeCellY] = false;
                		}
            	}
        	}else{//drag up
        		int changeNum = dragNum - 1 - i;
            	int changeCellX = (changeNum - 1)%mShortAxisCells ;
            	int changeCellY = (changeNum - 1)/mShortAxisCells;
            	ApplicationInfo changeInfo = mBookmap.get(changeCellX*10 + changeCellY);
            	if(changeInfo != null){
            		if(changeCellX +1 > mShortAxisCells-1){
            			changeInfo.cellX = 0;
            			changeInfo.cellY = changeCellY +1;
            		}else{
            			changeInfo.cellX = changeCellX +1;
                    	changeInfo.cellY = changeCellY;	
            		}
                	mUpdateLocation.add(changeInfo);
                	mBookmap.put(changeInfo.cellX*10 + changeInfo.cellY, changeInfo);
                	mOccupied[changeInfo.cellX][changeInfo.cellY] = true;
                	}else{
                		if(changeCellX +1 > mShortAxisCells-1){
                			mBookmap.put(0*10 + changeCellY +1, null);
                        	mOccupied[0][changeCellY +1] = false;
                		}else{
                			mBookmap.put((changeCellX +1)*10 + changeCellY, null);
                        	mOccupied[changeCellX +1][changeCellY] = false;
                		}
                	} 
            	} 
        	} 
    	}else{
    		 mBookmap.put(dragCellX*10 + dragCellY,null);
             mOccupied[dragCellX][dragCellY] = false;
        }
        mDragInfo.cellX = cellX;
        mDragInfo.cellY = cellY;
        Log.d(TAG, "BookCaseItem.isDrag=" + BookCaseItem.isDrag);
        if(BookCaseItem.isDrag){ 
            mBookmap.put(cellX*10 + cellY,null);
            mOccupied[cellX][cellY] = false;
        }else{ 
            mBookmap.put(cellX*10 + cellY,mDragInfo); 
            mOccupied[cellX][cellY] = true;
//            Log.d(TAG, "mDragInfo.cellX=" + mDragInfo.cellX + ";mDragInfo.cellY=" + mDragInfo.cellY);
            mUpdateLocation.add(mDragInfo);
            mDragInfo = null;
        } 
        getOccupiedCells();
		SceneSurfaceView2.instance.drawScene(mX, mY);
		if(mUpdateLocation.size() > 0){
			updateBookBatchInDatabase(mContext, mUpdateLocation);	
		}		 
	}
	static void updateBookBatchInDatabase(Context context, List<ApplicationInfo> infos){
    	if(infos != null && infos.size() > 0){ 
        	final ContentResolver cr = context.getContentResolver();
        	ContentProviderClient providerClient = cr.acquireContentProviderClient(LauncherSettings.BookCase.CONTENT_URI);
        	LauncherProvider provider = (LauncherProvider) providerClient.getLocalContentProvider();
        	if(provider != null){
        		provider.updateBookCaseBatch(infos);   
        	}
        	providerClient.release();
    	}
    }
    public static void DeleteBookItemByBatch(Context context, List<ApplicationInfo> infos, Uri uri){
    	if(infos != null && infos.size() > 0){ 
        	final ContentResolver cr = context.getContentResolver();
        	ContentProviderClient providerClient = cr.acquireContentProviderClient(uri);
        	LauncherProvider provider = (LauncherProvider) providerClient.getLocalContentProvider();
        	if(provider != null){
        		provider.deleteBookBatch(infos, uri);   
        	}
        	providerClient.release();
    	}
    }
    static void addBookItemByBatch(Context context, ApplicationInfo info){
    	if(info != null){ 
        	final ContentResolver cr = context.getContentResolver();
        	ContentProviderClient providerClient = cr.acquireContentProviderClient(LauncherSettings.BookCase.CONTENT_URI);
        	LauncherProvider provider = (LauncherProvider) providerClient.getLocalContentProvider();
        	if(provider != null){
        		provider.insertBookData(info);   
        	}
        	providerClient.release();
    	}
    }
}
