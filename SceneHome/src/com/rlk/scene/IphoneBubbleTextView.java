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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.rlk.scene.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Scroller;
import android.widget.TextView;


/**
 * TextView that draws a bubble behind the text. We cannot use a LineBackgroundSpan
 * because we want to make the bubble taller than the text and TextView's clip is
 * too aggressive.
 */
public class IphoneBubbleTextView extends FrameLayout implements DropTarget{

	private static final boolean DEBUG = true;
	private static final String TAG = "IphoneBubbleTextView";
	
    private static final Random mRandom = new Random();
    private float mTranslateX;
    private float mTranslateY;
    private static final int MESSAGE_ROTATE = 0;
    private final Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			invalidate();
		}
    	
    };
    private final class TranslateThread extends Thread {

		@Override
		public void run() {
			while(LauncherValues.getInstance().isAnim()) {
				float f = mRandom.nextInt(3);
				if (f == 2) {
					mTranslateX = LauncherValues.mBubbleTextViewTranslateXY;
				} else if (f == 0) {
					mTranslateX = -LauncherValues.mBubbleTextViewTranslateXY;
				} else  {
					mTranslateX = 0;
				}
				f = mRandom.nextInt(3);
				if (f == 2) {
					mTranslateY = LauncherValues.mBubbleTextViewTranslateXY;
				} else if (f == 0) {
					mTranslateY = -LauncherValues.mBubbleTextViewTranslateXY;
				} else  {
					mTranslateY = 0;
				}
//				translateY = translateRandom.nextInt(3) - 1;
//				translateX = translateRandom.nextFloat() - 0.5f;
//				translateY = translateRandom.nextFloat() - 0.5f;
				mHandler.sendEmptyMessage(MESSAGE_ROTATE);
				try {
					sleep(100);
				} catch(Exception e) {
					Log.e(TAG,"Thread Exception " + e.toString());
				}
			}
			mHandler.sendEmptyMessage(MESSAGE_ROTATE);//stop animation, so invalidate at last
		}
    	
    };
    public void startRotateAnimation() {
    	new TranslateThread().start();
    }
    public void refrashIconState(){
    	mHandler.sendEmptyMessage(MESSAGE_ROTATE);
    }
    private TextView mTextView;
    private TextView mTextTitle;
    private Context mContext;
    
    private boolean mShouldDrawMirror = false;
    private boolean mShouldDrawFolder = false;

	private static Paint mPaint;
	
	private int mAlpha = 0xff;  //2011-01-12 add for draw alpha
    
    private List<Drawable> mFolderIcons = new LinkedList<Drawable>();
    
    public List<Drawable> getFolderIcons() {
		return mFolderIcons;
	}

    private Bitmap mMirroBitmap;
    private static Bitmap mAppDirBitmap;
    
    public IphoneBubbleTextView(Context context) {
        this(context,null);
    }
    public IphoneBubbleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }
    private void init(Context context,AttributeSet attrs) {
    	mContext = context;
    	
    	if (mPaint == null) {
    		mPaint = new Paint();
    		mPaint.setAlpha(90);
    	}
    }
    
    public TextView getTextTtile(){
    	return mTextTitle;
    }
    
    @Override
	protected void onFinishInflate() {
		super.onFinishInflate();
    	mTextView = (TextView)((ViewGroup)getChildAt(1)).getChildAt(1);
    	mTextTitle = (TextView)((ViewGroup)getChildAt(0)).getChildAt(0);
    	
    	View uninstallView = ((ViewGroup)getChildAt(1)).getChildAt(0);
    	uninstallView.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (DEBUG) Log.d(TAG, "onClick");
				final ApplicationInfo info = (ApplicationInfo)IphoneBubbleTextView.this.getTag();
				String title = mContext.getString(R.string.remove_package_alert_title, info.title);
				String message = mContext.getString(R.string.remove_package_alert_message, info.title);
				new AlertDialog.Builder(mContext)
					.setTitle(title)
					.setMessage(message)
					.setNegativeButton(R.string.cancel, null)
					.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {
//							Utilities.showUpgradeDialog(getContext());
							PackageManager pm = mContext.getPackageManager();
			        		String packageName = info.intent.getComponent().getPackageName();
			        		android.content.pm.ApplicationInfo ap = null;
			        		try {
			        			ap = pm.getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
			        		}catch(Exception e) { }
			        		if (ap != null) {
			        			pm.deletePackage(ap.packageName, null, 0);
//			        	        Uri packageURI = Uri.parse("package:"+packageName);
//			        	        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
//			        	        mContext.startActivity(uninstallIntent);
			        		}
			        		//Add GWLLSW-1599 ningyaoyun 20121217(on) 
			        		else{
			        			Launcher.getInstance().removeShortCut(packageName);
			        		}
			        		//Add GWLLSW-1599 ningyaoyun 20121217(off)
			        		return;
						}
				}).show();
			}
    	});
	}
	public void updateNotificationCount(int count) {
		if (DEBUG)
			Log.d(TAG,"updateNotificationCount count = " + count);
		if (count <= 0) {
			mTextView.setVisibility(View.INVISIBLE);
    		mTextView.setText("");
    	} else {
    		mTextView.setVisibility(View.VISIBLE);
    		mTextView.setText(Integer.toString(count));
    	}
    }
	
	private boolean mDrawFolderBg = false;
	private boolean mStopAnim = false;

	public void setDrawFolderBg(boolean flag){
		mDrawFolderBg = flag;
		mStopAnim = flag;
	}
	public void setStopAnim(boolean flag){
		mStopAnim = flag;
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
//		if (DEBUG) Log.d(TAG, "IphoneBubbleTextView dispatchDraw " );

	    ApplicationInfo ap = (ApplicationInfo)this.getTag();
	    final TextView textview = mTextView;
	    
	    if(Launcher.getInstance().MAIN_MENU){
	    if(LauncherValues.getInstance().isHasFolderOpen()){
	    	if (ap.launcherCount > 0 && (this == Launcher.getInstance().getFolderTarget() || ap.isFolderItem)) {
		    	textview.setVisibility(View.VISIBLE); //当展开文件夹时，将图标右上角显示数目的TextView隐藏
		    }else{
		    	textview.setVisibility(View.INVISIBLE);
		    }
	    }else{
	    	if (ap.launcherCount <= 0) {
	    		textview.setVisibility(View.INVISIBLE);
			} else {
				textview.setVisibility(View.VISIBLE);
			}
	    }
	    
        if(mDrawFolderBg){
			if (mAppDirBitmap == null) {
				initAppDirBitmap();
			}
			canvas.drawBitmap(mAppDirBitmap, LauncherValues.mGrayBgDrawLeft, LauncherValues.mGrayBgDrawTop, null);
		}
        
        if (LauncherValues.SHOW_TRANSPARENT_PANEL_MIRRO && mShouldDrawMirror) {
			drawMirro(canvas);
		}
        
		if (LauncherValues.getInstance().isAnim() && !mStopAnim) {//动画抖动
			if (ap.isUninstall) {
				((ViewGroup)getChildAt(1)).getChildAt(0).setVisibility(View.VISIBLE);
			}
				((ViewGroup) getChildAt(1)).getChildAt(2).setVisibility(
						View.INVISIBLE);
			canvas.save(); 
//			float rotate = random.nextBoolean() ? ROTATE_RIGHT : ROTATE_LEFT;
//			canvas.rotate(rotate, getWidth()/2, getHeight()/2 - 8);
			canvas.translate(mTranslateX, mTranslateY);
			super.dispatchDraw(canvas);
			canvas.restore();
		} else {
				((ViewGroup) getChildAt(1)).getChildAt(2).setVisibility(
						View.INVISIBLE);
			((ViewGroup)getChildAt(1)).getChildAt(0).setVisibility(View.INVISIBLE);
				super.dispatchDraw(canvas);
			}
	    }else{
	    	if(ap.isBookCase){
	    		((ViewGroup)getChildAt(1)).getChildAt(2).setVisibility(View.VISIBLE);
			super.dispatchDraw(canvas);
	    	}else{
	    		((ViewGroup)getChildAt(1)).getChildAt(2).setVisibility(View.INVISIBLE); 
	    		super.dispatchDraw(canvas); 
		}
	    }
	    
	}
	
	private void drawMirro(Canvas canvas) {
		if (mMirroBitmap == null ){
			initMirroBitmap();
		} 
		final int startX = LauncherValues.mIconMirroPaddingLeft;
        final int startY = LauncherValues.mIconHeiht + LauncherValues.mIconMirroPaddingTop;
        canvas.drawBitmap(mMirroBitmap, startX, startY, mPaint);
	}
    
    public void setShouldDrawMirror(boolean value) {
    	mShouldDrawMirror = value;
    }
    
    public boolean shouldDrawMirror() {
    	return mShouldDrawMirror;
    }
    
    public boolean isShouldDrawFolder() {
		return mShouldDrawFolder;
	}
	public void setShouldDrawFolder(boolean shouldDrawFolder) {
		mShouldDrawFolder = shouldDrawFolder;
	}
    
    public void initMirroBitmap() {
    	
    	final Drawable[] drawables = mTextTitle.getCompoundDrawables();
    	Drawable mirroDrawable = drawables[1];
    	if(mirroDrawable != null){
    		final int mirroHeight = mirroDrawable.getIntrinsicHeight();
    		final int mirroWidth = mirroDrawable.getIntrinsicWidth();
    		Bitmap bitmap = Utilities.drawableToBitmap(mirroDrawable, mirroWidth, mirroHeight);
    		int startY = (int) (mirroHeight / 3);
    		mMirroBitmap = Utilities.createMirroBitmap(bitmap, startY);
    	}else{
        	Log.e(TAG, "mirroDrawable == null");
        	return;
    	}
    }
    
    private void initAppDirBitmap() {
    	Drawable grayBackground = getResources().getDrawable(R.drawable.app_dir_bg);
    	int width = grayBackground.getIntrinsicWidth();
    	int height = grayBackground.getIntrinsicHeight();
    	mAppDirBitmap = Utilities.drawableToBitmap(grayBackground, width, height);
    }
    
	public boolean acceptDrop(DragSource source, int x, int y, int offset,
			int offset2, Object dragInfo) {
		if(DEBUG) Log.d(TAG, "acceptDrop");
		ApplicationInfo info = (ApplicationInfo)getTag(); 
		Scroller scroller = Launcher.getInstance().getWorkspace().getScroller();
		if(!info.isFolder || !(scroller.isFinished())){
		    return false;
		}
		
		if(!LauncherValues.IGNORE_IPHONE_BUBBLE_TEXTVIEW){
			if(info.isFolder){
				FolderInfo folderInfo = (FolderInfo)info;
				int itemSize = folderInfo.items.size();
				return itemSize > 0 && itemSize < LauncherValues.mMaxFolderChildCount ? true : false;
			}else{
				return (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION 
						|| info.itemType == LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT) 
						&& info.container != info.id;
			}
		}
		
		return false;
	}
	
	public void onDragEnter(DragSource source, int x, int y, int offset,
			int offset2, Object dragInfo) {
		if(DEBUG) Log.d(TAG, "onDragEnter");
		ApplicationInfo ap = (ApplicationInfo) getTag();
		if(ap.itemType == LauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER){
			FolderInfo folderInfo = (FolderInfo) getTag();
			Drawable drawable = Utilities.createFolderIcon(getContext(),folderInfo.items,Utilities.BLACK_BG);
			mTextTitle.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
			setStopAnim(true);
		}else{
			ap.icon.setAlpha(150);
			setDrawFolderBg(true);
		}
		mTextTitle.setText("");
		LauncherSettings.Favorites.ON_FOLDER_TARGET = true;
		Launcher.getInstance().postShowFolder(this); 
	}
	
	public void onDragExit(DragSource source, int x, int y, int offset,
			int offset2, Object dragInfo) {
		if(DEBUG) Log.d(TAG, "onDragExit");
		if(LauncherValues.dragIconStatu == LauncherValues.DRAG_ON_EXPEND_FOLDER){
			return;
		}else{
			Launcher.getInstance().removeShowFolder();
			ApplicationInfo ap = (ApplicationInfo) getTag();
			mTextTitle.setText(ap.title);
			ap.icon.setAlpha(255);
			mTextTitle.setCompoundDrawablesWithIntrinsicBounds(null, ap.icon, null, null);
			setDrawFolderBg(false);
			LauncherSettings.Favorites.ON_FOLDER_TARGET = false;
		}
	}
	
	public void onDragOver(DragSource source, int x, int y, int xoffset,
			int yoffset, Object dragInfo, boolean isDrop) {
		if(DEBUG) Log.d(TAG, "onDragOver");
	}
	
	public void onDrop(DragSource source, int x, int y, int xOffset,
			int yOffset, Object dragInfo) {
		if(DEBUG) Log.d(TAG, "onDrop");
		
		Launcher.getInstance().removeShowFolder();
		ApplicationInfo appTargetInfo = (ApplicationInfo)getTag();
		ApplicationInfo appDragInfo = (ApplicationInfo)dragInfo;
		
		appDragInfo.screen = appTargetInfo.screen;
		appDragInfo.container = LauncherSettings.Favorites.CONTAINER_FOLDER;
		appDragInfo.isFolderItem = true;
		 
 
		
		FolderInfo folderInfo = null;
		if(appTargetInfo.isFolder){
			folderInfo = (FolderInfo) getTag();
			folderInfo.items.add(appDragInfo);
			folderInfo.itemsId.add(appDragInfo.id);
			folderInfo.launcherCount += appDragInfo.launcherCount;
			appDragInfo.folderInfo = folderInfo;
			updateNotificationCount(folderInfo.launcherCount);
			
				LauncherModel.updateItemInDatabase(getContext(), appDragInfo);
				LauncherModel.updateItemInDatabase(getContext(), folderInfo);
				Launcher.getModel().removeDesktopItem(appDragInfo);
			
			if(LauncherValues.dragIconStatu == LauncherValues.DRAG_NO_STATU){
				mTextTitle.setText(folderInfo.title);
			}
		} else {
			folderInfo = new FolderInfo(getContext(), appTargetInfo);
			folderInfo.itemsId.add(appTargetInfo.id);
			folderInfo.itemsId.add(appDragInfo.id);
			folderInfo.items.add(appTargetInfo);
			folderInfo.items.add(appDragInfo);
			folderInfo.launcherCount += appDragInfo.launcherCount;
			folderInfo.iphoneBubbleTextView = this;
			updateNotificationCount(folderInfo.launcherCount);
			setTag(folderInfo);

    		appTargetInfo.container = LauncherSettings.Favorites.CONTAINER_FOLDER;
    		appTargetInfo.isFolderItem = true;
    		appTargetInfo.folderInfo = folderInfo;
    		appDragInfo.folderInfo = folderInfo;
    		
    		LauncherModel.addItemToDatabase(getContext(), folderInfo);
    		LauncherModel.updateItemInDatabase(getContext(), appTargetInfo);
    		LauncherModel.updateItemInDatabase(getContext(), appDragInfo);
    		
    		Launcher.getModel().addDesktopItem(folderInfo);
    		Launcher.getModel().removeDesktopItem(appTargetInfo);
    		Launcher.getModel().removeDesktopItem(appDragInfo);

    		if(!LauncherValues.getInstance().isHasFolderOpen()){
    			CellLayout cellLayout = Launcher.getInstance().getCurrentCellLayout();
    			Launcher.getInstance().expandFolder(cellLayout, this);
    		}
    		LauncherValues.TRY_TO_CREATE_FOLDER = true;
		}
		
		folderInfo.icon = Utilities.createFolderIcon(getContext(),folderInfo.items,Utilities.GRAY_BG);
		mMirroBitmap = null;
		folderInfo.grayIcon = Utilities.convertGrayImg(folderInfo.icon);
		mTextTitle.setCompoundDrawablesWithIntrinsicBounds(null, folderInfo.icon, null, null);
	}
	
	/*
	 * //2011-01-12 add for draw alpha
	*/
	public boolean setAlpha(int alpha) {
		if (mAlpha != alpha) {
			mAlpha = alpha;
			//child 0 
			final TextView tv = mTextTitle;
			alpha = alpha << 24;
            int textColor =  (0xFF846b45) | alpha;
			tv.setTextColor(textColor);
			final Drawable[] drawables = tv.getCompoundDrawables();
	        if (drawables[1] != null) {
	        	drawables[1].setAlpha(mAlpha);
	        } else {
	        	Log.e(TAG, "background  null");
	        }
	        //child1 显示launcher count的Textview
		    final TextView tv1 = mTextView;
		    if (tv1.getVisibility() == View.VISIBLE) {
		    	tv1.setTextColor(textColor);
			    Drawable tv1Bg = tv1.getBackground();
			    Drawable tv2BgSetAlpha = getResources().getDrawable(R.drawable.icon_dian_set_alpha);
			    if (mAlpha == 255) {
			    	tv1Bg = getResources().getDrawable(R.drawable.icon_dian);
			    	tv1.setBackgroundDrawable(tv1Bg);
			    	return true;
			    } else if (tv1Bg != tv2BgSetAlpha) {
			    	tv1Bg = tv2BgSetAlpha;
			    	tv1.setBackgroundDrawable(tv1Bg);
			    }
			    if (tv1Bg != null ) {
			    	tv1Bg.setAlpha(mAlpha);
			    }
		    }
	        return true;
		}
		return false;
	}
	
	public void onCompleteTranslate(DragSource source, int x, int y, int xOffset, int yOffset, Object dragInfo) {
	}
	
	public void clearMirro() {
		mMirroBitmap = null;
	}
}
