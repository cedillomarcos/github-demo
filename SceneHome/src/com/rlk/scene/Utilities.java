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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rlk.scene.R;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PixelFormat;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.PorterDuff.Mode;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Various utilities shared amongst the Launcher's classes.
 */
public final class Utilities {
	private static boolean DEBUG = false;
	private static String TAG = "Utilities";
	
    private static int sIconWidth = -1;
    private static int sIconHeight = -1;

    private static final Paint sPaint = new Paint();
    private static final Rect sBounds = new Rect();
    private static final Rect sOldBounds = new Rect();
    private static Canvas sCanvas = new Canvas();
    public static final List<String> systemOwnPictureAppsList = Arrays.asList(new String[]{
    	"com.mediatek.todos",
    	"com.mediatek.bluetooth", 
    	"com.mediatek.videoplayer",
    	"com.android.soundrecorder",
    	"com.android.settings",
    	"com.mediatek.notebook",
    	"com.android.music",
    	"com.android.videoeditor",
    	"com.mediatek.FMRadio",
    	"com.mediatek.filemanager",
    	"com.android.email",
    	"com.android.providers.downloads.ui",
    	"com.android.deskclock",
    	"com.android.cellbroadcastreceiver",
    	"com.android.calendar",
    	"com.android.calculator2",
    	"com.mediatek.datatransfer",
    	"com.android.gallery3d",
    	"com.android.browser",
    	"com.android.contacts",
    	"com.android.mms",
    });
    static {
        sCanvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
                Paint.FILTER_BITMAP_FLAG));
    }
    
    /**
     * Returns a Drawable representing the thumbnail of the specified Drawable.
     * The size of the thumbnail is defined by the dimension
     * android.R.dimen.launcher_application_icon_size.
     *
     * This method is not thread-safe and should be invoked on the UI thread only.
     *
     * @param icon The icon to get a thumbnail of.
     * @param context The application's context.
     *
     * @return A thumbnail for the specified icon or the icon itself if the
     *         thumbnail could not be created. 
     */
    static Drawable createIconThumbnail(Drawable icon, Context context) {
        final Resources resources = context.getResources();
        int width;
        int height;
        sIconWidth = (int) resources.getDimension(R.dimen.icon_width);
        sIconHeight = (int) resources.getDimension(R.dimen.icon_height);
        width = sIconWidth;
        height = sIconHeight;	 
        if (icon instanceof PaintDrawable) {
            PaintDrawable painter = (PaintDrawable) icon;
            painter.setIntrinsicWidth(width);
            painter.setIntrinsicHeight(height);
        } else if (icon instanceof BitmapDrawable) {

            // Ensure the bitmap has a density.
            BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (bitmap.getDensity() == Bitmap.DENSITY_NONE) {
                bitmapDrawable.setTargetDensity(context.getResources().getDisplayMetrics());
            }
        } 
    	int iconWidth = icon.getIntrinsicWidth();
        int iconHeight = icon.getIntrinsicHeight();
        Log.d(TAG, "createIconThumbnail  iconWidth=" + iconWidth + ";iconHeight=" + iconHeight);
        if (width > 0 && height > 0) {
            if (width <= iconWidth || height <= iconHeight) { 
                final Bitmap.Config c = Bitmap.Config.ARGB_8888;
                final Bitmap thumb = Bitmap.createBitmap(sIconWidth, sIconHeight, c);
                final Canvas canvas = sCanvas;
                canvas.setBitmap(thumb); 
                sOldBounds.set(icon.getBounds()); 
                icon.setBounds(13, 13, sIconWidth-13, sIconHeight-13); 
                icon.draw(canvas);
                icon.setBounds(sOldBounds);
                icon = new FastBitmapDrawable(thumb);
            } else if (iconWidth < width && iconHeight < height) {
                final Bitmap.Config c = Bitmap.Config.ARGB_8888;
                final Bitmap thumb = Bitmap.createBitmap(sIconWidth, sIconHeight, c);
                final Canvas canvas = sCanvas;
                canvas.setBitmap(thumb);
                sOldBounds.set(icon.getBounds()); 
                if(iconWidth > 80 || iconHeight > 80){
                	icon.setBounds(13, 13, sIconWidth-13, sIconHeight-13); 
                }else{
				final int x = (width - iconWidth) / 2;
				final int y = (height - iconHeight) / 2;
				icon.setBounds(x, y, x + iconWidth, y + iconHeight); 
                } 
                icon.draw(canvas);
                icon.setBounds(sOldBounds);
                icon = new FastBitmapDrawable(thumb);
            }
        } 
        return icon;
    }
    
    
    
    
    static Drawable createIconThumbnailForUnistall(Drawable icon, Context context, String pckName) {
        final Resources resources = context.getResources();
        int width;
        int height;
        sIconWidth = (int) resources.getDimension(R.dimen.icon_width);
        sIconHeight = (int) resources.getDimension(R.dimen.icon_height);
        width = sIconWidth;
        height = sIconHeight;	 
        if (icon instanceof PaintDrawable) {
            PaintDrawable painter = (PaintDrawable) icon;
            painter.setIntrinsicWidth(width);
            painter.setIntrinsicHeight(height);
        } else if (icon instanceof BitmapDrawable) {

            // Ensure the bitmap has a density.
            BitmapDrawable bitmapDrawable = (BitmapDrawable) icon;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (bitmap.getDensity() == Bitmap.DENSITY_NONE) {
                bitmapDrawable.setTargetDensity(context.getResources().getDisplayMetrics());
            }
        }
        Log.d(TAG, "pckName: " + pckName);
        boolean isSystemOwnApp = systemOwnPictureAppsList.contains(pckName);
        if(isSystemOwnApp){ 
        	final Bitmap.Config c = Bitmap.Config.ARGB_8888;
            final Bitmap thumb = Bitmap.createBitmap(sIconWidth, sIconHeight, c);
            final Canvas canvas = sCanvas;
            canvas.setBitmap(thumb);
            sOldBounds.set(icon.getBounds());  
            icon.setBounds(0, 0, sIconWidth, sIconHeight);
            icon.draw(canvas);
            icon.setBounds(sOldBounds);
            icon = new FastBitmapDrawable(thumb);
        }else{  
        	int iconWidth = icon.getIntrinsicWidth();
            int iconHeight = icon.getIntrinsicHeight();
            Log.d(TAG, "createIconThumbnailForUnistall  iconWidth=" + iconWidth + ";iconHeight=" + iconHeight);
            if (width > 0 && height > 0) {
                if (width < iconWidth || height < iconHeight) { 
                    final Bitmap.Config c = Bitmap.Config.ARGB_8888;
                    final Bitmap thumb = Bitmap.createBitmap(sIconWidth, sIconHeight, c);
                    final Canvas canvas = sCanvas;
                    canvas.setBitmap(thumb); 
                    
                    Drawable mFrameDrawable = context.getResources().getDrawable(R.drawable.icon_thirdpart);
         			mFrameDrawable.setBounds(0, 0, width, width);
         			mFrameDrawable.draw(canvas);
                    
                    sOldBounds.set(icon.getBounds()); 
                    icon.setBounds(13, 13, sIconWidth-13, sIconHeight-13); 
                    icon.draw(canvas);
                    icon.setBounds(sOldBounds);
                    icon = new FastBitmapDrawable(thumb);
                } else if (iconWidth < width && iconHeight < height) {
                    final Bitmap.Config c = Bitmap.Config.ARGB_8888;
                    final Bitmap thumb = Bitmap.createBitmap(sIconWidth, sIconHeight, c);
                    final Canvas canvas = sCanvas;
                    canvas.setBitmap(thumb);
                    sOldBounds.set(icon.getBounds()); 
                    if(iconWidth > 80 || iconHeight > 80){
                    	icon.setBounds(13, 13, sIconWidth-13, sIconHeight-13); 
					} else {
						final int x = (width - iconWidth) / 2;
						final int y = (height - iconHeight) / 2;
						icon.setBounds(x, y, x + iconWidth, y + iconHeight);
					} 
                    
                    Drawable mFrameDrawable = context.getResources().getDrawable(R.drawable.icon_thirdpart);
         			mFrameDrawable.setBounds(0, 0, width, width);
         			mFrameDrawable.draw(canvas);
                    
                    icon.draw(canvas);
                    icon.setBounds(sOldBounds);
                    icon = new FastBitmapDrawable(thumb);
                }
            }
        }  
        return icon;
    }

    /**
     * Returns a Bitmap representing the thumbnail of the specified Bitmap.
     * The size of the thumbnail is defined by the dimension
     * android.R.dimen.launcher_application_icon_size.
     *
     * This method is not thread-safe and should be invoked on the UI thread only.
     *
     * @param bitmap The bitmap to get a thumbnail of.
     * @param context The application's context.
     *
     * @return A thumbnail for the specified bitmap or the bitmap itself if the
     *         thumbnail could not be created.
     */
    static Bitmap createBitmapThumbnail(Bitmap bitmap, Context context) {
        final Resources resources = context.getResources();
        sIconWidth = (int) resources.getDimension(R.dimen.icon_width);
        sIconHeight = (int) resources.getDimension(R.dimen.icon_height);
        
        int width = sIconWidth;
        int height = sIconHeight;

        final int bitmapWidth = bitmap.getWidth();
        final int bitmapHeight = bitmap.getHeight();
        Log.d(TAG, "bitmapWidth=" + bitmapWidth + ";bitmapHeight=" + bitmapHeight);
        if (width > 0 && height > 0) {
            if (width < bitmapWidth || height < bitmapHeight) {
            	//Modify GWLLSW-912 ningyaoyun 20121022(on)
//                final float ratio = (float) bitmapWidth / bitmapHeight;
    
//                if (bitmapWidth > bitmapHeight) {
//                    height = (int) (width / ratio);
//                } else if (bitmapHeight > bitmapWidth) {
//                    width = (int) (height * ratio);
//                float scale = 0.8f;
//                width = (int)scale*width;
//                height = (int)scale*height;
    
                final Bitmap.Config c = (width == sIconWidth && height == sIconHeight) ?
                        bitmap.getConfig() : Bitmap.Config.ARGB_8888;
                final Bitmap thumb = Bitmap.createBitmap(sIconWidth, sIconHeight, c);
                final Canvas canvas = sCanvas;
                final Paint paint = sPaint;
                canvas.setBitmap(thumb);
                paint.setDither(false);
                paint.setFilterBitmap(true);
                sBounds.set(10, 10, sIconWidth - 10, sIconHeight - 10);
              //Modify GWLLSW-912 ningyaoyun 20121022(off)
                sOldBounds.set(0, 0, bitmapWidth, bitmapHeight);
                canvas.drawBitmap(bitmap, sOldBounds, sBounds, paint);
                return thumb;
            } else if (bitmapWidth < width || bitmapHeight < height) {
                final Bitmap.Config c = Bitmap.Config.ARGB_8888;
                final Bitmap thumb = Bitmap.createBitmap(sIconWidth, sIconHeight, c);
                final Canvas canvas = sCanvas;
                final Paint paint = sPaint;
                canvas.setBitmap(thumb);
                paint.setDither(false);
                paint.setFilterBitmap(true);
                canvas.drawBitmap(bitmap, (sIconWidth - bitmapWidth) / 2,
                        (sIconHeight - bitmapHeight) / 2, paint);
                return thumb;
            }
        }

        return bitmap;
    }
    
	private static int mMiniIconWidth;
	private static int mMiniIconHeight;
	private static int mMiniIconPaddingLeft;
	private static int mMiniIconPaddingTop;
	
	public static final int BLACK_BG = 1;
    public static final int GRAY_BG = 2;
    private static final int mLineCount = 3;
    
    public static void initValues(){
    	mMiniIconWidth = LauncherValues.mMiniIconWidth;
    	mMiniIconHeight = LauncherValues.mMiniIconHeight;
    	mMiniIconPaddingLeft = LauncherValues.mMiniIconPaddingLeft;
    	mMiniIconPaddingTop = LauncherValues.mMiniIconPaddingTop;
    }
    
    public static Drawable createFolderIcon(Context context, ItemInfo info){
    	Drawable folderBg = context.getResources().getDrawable(R.drawable.iphone_folder_bg);
    	int mWidth = (int) context.getResources().getDimension(R.dimen.icon_width);
		int mHeight = (int) context.getResources().getDimension(R.dimen.icon_height);
    	Bitmap bgBitmap = drawableToBitmap(folderBg,mWidth,mHeight);
    	Canvas canvas = new Canvas(bgBitmap);
    	
    	int x = 0;
		int y = 0;
		int left = mMiniIconPaddingLeft;
		int top = mMiniIconPaddingTop;
		ApplicationInfo applicationInfo = (ApplicationInfo)info;
		if(applicationInfo.smallIcon == null){
			applicationInfo.smallIcon = createSmallIcon(applicationInfo.icon, context);
		}
		canvas.drawBitmap(applicationInfo.smallIcon, left, top, null);
		return new BitmapDrawable(Launcher.resources, bgBitmap);
    }
    
	/**
	 * å°†Drawableè½¬æ�¢ä¸ºæŒ‡å®šé«˜åº¦å®½å¸¦çš„Bitmap
	 */
	public static Bitmap drawableToBitmap(Drawable drawable, int width,
			int height) {

		Bitmap bitmap = Bitmap.createBitmap(width, height, drawable
				.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		// canvas.setBitmap(bitmap);
		drawable.setBounds(0, 0, width, height);
		drawable.draw(canvas);
		return bitmap;
	}
    
    public static Drawable createFolderIcon(Context context, List<ApplicationInfo> items, int flag) {
    	Drawable grayBg = context.getResources().getDrawable(R.drawable.iphone_folder_bg);
    	Drawable transparenceBg = context.getResources().getDrawable(R.drawable.app_dir_bg);
    	Drawable folderBg = null;
    	int paddingLeft = 0;
    	int paddingTop = 0;
    	
		if(flag == GRAY_BG){
			folderBg = grayBg;
			paddingLeft = mMiniIconPaddingLeft;
			paddingTop = mMiniIconPaddingTop;
		}else if(flag == BLACK_BG) {
			folderBg = transparenceBg;
			int extraLeft = (transparenceBg.getIntrinsicWidth() - grayBg.getIntrinsicWidth()) / 2;
			int extraTop = (transparenceBg.getIntrinsicHeight() - grayBg.getIntrinsicHeight()) / 2;
			paddingLeft = mMiniIconPaddingLeft + extraLeft;
			paddingTop = mMiniIconPaddingTop + extraTop;
		}
		
		int mWidth = folderBg.getIntrinsicWidth();
		int mHeight = folderBg.getIntrinsicHeight();
		int hGap = (mWidth - mMiniIconWidth * mLineCount - paddingLeft * 2) / (mLineCount - 1);
		int vGap = (mHeight - mMiniIconHeight * mLineCount - paddingTop * 2) / (mLineCount - 1);
		
		Bitmap bgBitmap = drawableToBitmap(folderBg,mWidth,mHeight);
		Canvas canvas = new Canvas(bgBitmap);
		
		int x = 0;
		int y = 0;
		int left = 0;
		int top = 0;
		
		for (int i = 0; i < items.size(); i++) {
			x = i % mLineCount;
			y = i / mLineCount;
			
			left = hGap * x + mMiniIconWidth * x + paddingLeft;
			top = vGap * y + mMiniIconHeight * y + paddingTop;
			
			ApplicationInfo info = items.get(i);
			if(info.smallIcon == null){
				info.smallIcon = createSmallIcon(info.icon, context);
			}
			canvas.drawBitmap(info.smallIcon, left, top, null);
		}
		return new BitmapDrawable(Launcher.resources, bgBitmap);
	}
    
    public static String intArrayToString(List<Long> data){
    	StringBuilder builder = new StringBuilder();
    	for(Long value : data){
    		builder.append(value).append(",");
    	}
    	if(builder.length() > 0){
    		builder.deleteCharAt(builder.length() - 1);
    		return builder.toString();
    	}
    	return null;
    }
    
    public static ArrayList<Long> stringToArrayList(String data){
    	if(data != null && data.length() > 0){
    		String[] value = data.split(",");
    		ArrayList<Long> result = new ArrayList<Long>();
    		for (int i = 0; i < value.length; i++) {
    			result.add(Long.parseLong(value[i]));
			}
    		return result;
    	}
    	return null;
    }
    
    public static ItemInfo findItemInfoById(List<ItemInfo> desktopItems, long id) {
		for(ItemInfo info : desktopItems){
			if(info.id == id){
				desktopItems.remove(info);
				return info;
			}
		}
		return null;
	}
    
    public static Drawable convertGrayImg(Drawable drawable) {
    	Bitmap img1 = null;
    	if(drawable instanceof BitmapDrawable){
    		img1 = ((BitmapDrawable)drawable).getBitmap();
    	}else if(drawable instanceof FastBitmapDrawable){
    		img1 = ((FastBitmapDrawable)drawable).getBitmap();
    	}
    	
		BitmapDrawable d = new BitmapDrawable(Launcher.resources, grayBitmap(img1));
		d.setAlpha(0x40);
		d.setBounds(0, 0, img1.getWidth(), img1.getHeight());
		return d;
	}
    
	public static Bitmap createMirroBitmap(Bitmap originalBitmap, int height) {
		int originalWidth = originalBitmap.getWidth();
		int originalHeight = originalBitmap.getHeight();
		int startY = originalHeight - height;

		Matrix matrix = new Matrix();
		matrix.preScale(1, -1);
		Bitmap bitmap = Bitmap.createBitmap(originalBitmap, 0, startY,
				originalWidth, height, matrix, false);
		Canvas canvas = new Canvas(bitmap);

		final Paint paint = new Paint();
		LinearGradient shader = new LinearGradient(0, 0, 0, height,
				0xffffffff, 0x33000000, TileMode.CLAMP);
		paint.setShader(shader);
		paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));

		canvas.drawRect(0, 0, originalWidth, height, paint);
		return bitmap;
	}
    
    public static Bitmap grayBitmap(Bitmap bitmap){
    	int w = bitmap.getWidth(), h = bitmap.getHeight();
		int[] pix = new int[w * h];
		bitmap.getPixels(pix, 0, w, 0, 0, w, h);  
		Bitmap result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		result.setPixels(pix, 0, w, 0, 0, w, h);
		return result;
    }
    
    public static Drawable copyDrawable(Drawable drawable, Context context){
    	Bitmap bitmap = null;
    	sIconWidth = drawable.getIntrinsicWidth();
    	sIconHeight = drawable.getIntrinsicHeight();
    	bitmap = Bitmap.createBitmap(sIconWidth, sIconHeight, drawable
				.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, sIconWidth, sIconHeight);
		drawable.draw(canvas);
		return new BitmapDrawable(Launcher.resources, bitmap);
    }
    
    public static Bitmap createSmallIcon(Drawable icon, Context context) {
		Drawable smallIcon = copyDrawable(icon,context);
		Bitmap bitmap = drawableToBitmap(smallIcon, mMiniIconWidth, mMiniIconHeight);
		return bitmap;
	}
    
    public static Drawable mergeDrawble(Context context, Drawable src) {
    	
    	/*Drawable background = context.getResources().getDrawable(R.drawable.icon_thirdpart);
    	int width = (int) Launcher.getInstance().getResources().getDimension(R.dimen.icon_width);
    	int height = (int) Launcher.getInstance().getResources().getDimension(R.dimen.icon_height);
    	Log.d(TAG, "width=" + width + ";height=" + height);
    	Bitmap bitmap = Bitmap.createBitmap(width, height, background
				.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565);
    	Canvas canvas = new Canvas();
    	canvas.setBitmap(bitmap);
    	background.setBounds(0, 0, width, height);
    	background.draw(canvas);
    	int srcWidth = src.getIntrinsicWidth();
    	int srcHeight = src.getIntrinsicHeight(); 
    	Log.d(TAG, "srcWidth=" + srcWidth + ";srcHeight=" + srcHeight);
    	src.setBounds((width - srcWidth)/2, (height - srcHeight)/2, (width + srcWidth)/2, (height + srcHeight)/2);
    	src.setBounds(0, 0, 30, 30); 
    	src.setBounds(15, 15, width - 15, height - 15);
    	src.draw(canvas);*/
    
    	
    	Drawable background = context.getResources().getDrawable(R.drawable.icon_thirdpart);
    	int width = (int) Launcher.getInstance().getResources().getDimension(R.dimen.icon_width);
    	int height = (int) Launcher.getInstance().getResources().getDimension(R.dimen.icon_height);
    	Log.d(TAG, "width=" + width + ";height=" + height);
    	Bitmap bitmap = Bitmap.createBitmap(width, height, background
				.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565);
    	Canvas canvas = new Canvas();
    	canvas.setBitmap(bitmap);
    	background.setBounds(0, 0, width, height);
    	background.draw(canvas);
    	
    	int srcWidth = src.getIntrinsicWidth();
    	int srcHeight = src.getIntrinsicHeight();
    	Log.d(TAG, "srcWidth=" + srcWidth + ";srcHeight=" + srcHeight);
//    	src.setBounds((width - srcWidth)/2, (height - srcHeight)/2, (width + srcWidth)/2, (height + srcHeight)/2);
    	src.draw(canvas);
    	return new BitmapDrawable(Launcher.resources, bitmap);
    }
    
    public static void changeItemColor(View v, int flag, ViewGroup parent) {
    	
		int count = parent.getChildCount();
		View child;
		IphoneBubbleTextView tv;
		TextView textTitle;
		ApplicationInfo info;
		
		for (int i = 0; i < count; i++) {
			child = parent.getChildAt(i);
			
			if(child instanceof IphoneBubbleTextView){
				info = (ApplicationInfo)child.getTag();
				tv = (IphoneBubbleTextView) child;
				textTitle = (TextView)((ViewGroup)tv.getChildAt(0)).getChildAt(0);
				tv.setClickable(!tv.isClickable());
				tv.setEnabled(!tv.isEnabled());
				
				if(child != v){
					if(flag == Launcher.CHANGE_GRAY){
						if(info.grayIcon == null){
							info.grayIcon = Utilities.convertGrayImg(info.icon);
						} 
						textTitle.setCompoundDrawables(null, info.grayIcon, null, null); 
						textTitle.setTextColor(0x9F846b45); 
						textTitle.setShadowLayer(0, 0, 0, 0);
					}else if(flag == Launcher.CHANGE_COLOR){
						if (DEBUG) Log.d("LOGD", "info.title = " + info.title); 
						textTitle.setCompoundDrawables(null, info.icon, null, null);
						textTitle.setTextColor(0xFF846b45);
//						textTitle.setShadowLayer(2, 0, 0, 0xFF000000);
					}
				}
			}
		}
		parent.invalidate();
	}
    
    public static int getHeightGaps(Context context){
    	
    	int defaultLongAxisStartPadding = (int) context.getResources().getDimension(R.dimen.long_axis_start_Padding);
    	int defaultLongAxisEndPadding = (int) context.getResources().getDimension(R.dimen.long_axis_end_Padding);
    	int defaultCellHeight = (int) context.getResources().getDimension(R.dimen.cell_height);
    	
    	//Modify GWLLSW-258 ningyaoyun 20120924(on) 
//    	int longAxisCells = 4;
    	int longAxisCells = 5;
    	//Modify GWLLSW-258 ningyaoyun 20120924(off)
    	int numLongGaps = longAxisCells - 1;
        int heightGap = 0;
        	
        int vSpaceLeft = LauncherValues.mScreenHeight - defaultLongAxisStartPadding 
        		- defaultLongAxisEndPadding - (defaultCellHeight * longAxisCells);
        heightGap = vSpaceLeft / numLongGaps;
        Log.d(TAG, "heightGap = " + heightGap);
        return heightGap;
    }
    
    public static int getWidthGaps(Context context){
    	
    	int defaultShortAxisStartPadding = (int) context.getResources().getDimension(R.dimen.short_axis_start_Padding);
    	int defaultShortAxisEndPadding = (int) context.getResources().getDimension(R.dimen.short_axis_end_Padding);
    	int defaultCellWidth = (int) context.getResources().getDimension(R.dimen.cell_width);
    	
    	int shortAxisCells = 4;
    	int numShortGaps = shortAxisCells - 1;
        int widthGap = 0;
        
    	int hSpaceLeft = LauncherValues.mScreenWidth - defaultShortAxisStartPadding 
    			- defaultShortAxisEndPadding - (defaultCellWidth * shortAxisCells);
    	widthGap = hSpaceLeft / numShortGaps;

        return widthGap;
    }
    
    public static void showUpgradeDialog(Context context){
		new AlertDialog.Builder(context)
			.setTitle(R.string.iphone_textutils_experience_title)
			.setMessage(R.string.iphone_textutils_experience_msg)
			.setPositiveButton(R.string.iphone_textutils_experience_ok,
			new DialogInterface.OnClickListener() {
				 
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			}).show();
    }

	public static List<ApplicationInfo> getInfoByPackage(List<ItemInfo> desktopItem,
			String pName) {
		List<ApplicationInfo> appInfos = new ArrayList<ApplicationInfo>();
		for(ItemInfo info : desktopItem){
			ApplicationInfo item = (ApplicationInfo) info;
			if(item.intent.getComponent().getPackageName().equals(pName)){
				appInfos.add(item); 
			}
		}
		return appInfos;
	}
 
	public static Drawable getCustomerIcon(Context context, Intent intent){
    	ComponentName component = intent.getComponent();
    	int resourceId;
    	if(mIconByComponentName.get(component) == null){
    		String packageName = component.getPackageName();
    		if(mIconByPackageName.get(packageName) == null){
    			return null;
    		} else {
    			resourceId = mIconByPackageName.get(packageName);
    			return context.getResources().getDrawable(resourceId);
    		}
    	} else {
    		resourceId = mIconByComponentName.get(component);
			return context.getResources().getDrawable(resourceId);
    	}
    }
    
    public static Map<String, Integer> mIconByPackageName = new HashMap<String, Integer>();
    public static Map<ComponentName, Integer> mIconByComponentName = new HashMap<ComponentName, Integer>();
    public static void recycleBitmap(Bitmap bitmap)
    {
    	if(bitmap!=null&&!bitmap.isRecycled())
    	{
    		bitmap.recycle();
    	}
    }
    static{
    	/*mIconByPackageName.put("com.android.browser", R.drawable.app_browser);
    	mIconByPackageName.put("com.hskj.iphonecalculator", R.drawable.app_calculator);		
    	mIconByPackageName.put("com.android.calculator2", R.drawable.app_calculator);
    	mIconByPackageName.put("com.android.calendar", R.drawable.app_calendar);
    	mIconByPackageName.put("com.mediatek.camera", R.drawable.app_camera);
    	mIconByPackageName.put("com.android.providers.downloads.ui", R.drawable.app_downloads);
    	mIconByPackageName.put("com.android.email", R.drawable.app_email);
    	mIconByPackageName.put("com.htc.android.mail", R.drawable.app_email);
    	mIconByPackageName.put("com.android.deskclock", R.drawable.app_deskclock);
    	mIconByPackageName.put("com.android.gallery", R.drawable.app_gallery);
    	mIconByPackageName.put("com.cooliris.media", R.drawable.app_gallery3d);
    	mIconByPackageName.put("com.mediatek.filemanager", R.drawable.app_fileexplorer);
    	mIconByPackageName.put("com.android.settings", R.drawable.app_settings);
    	mIconByPackageName.put("com.android.vending", R.drawable.app_vending);
    	mIconByPackageName.put("com.android.voicedialer", R.drawable.app_voicedialer);
    	mIconByPackageName.put("com.android.monitor", R.drawable.app_monitor);
    	mIconByPackageName.put("com.google.android.maps.driveabout.app.DestinationActivity", R.drawable.app_maps);
    	mIconByPackageName.put("net.cactii.flash2", R.drawable.app_torch);
    	mIconByPackageName.put("com.google.android.talk", R.drawable.app_talk);
    	mIconByPackageName.put("com.chaozh.iReaderFree", R.drawable.app_ireader_free);
    	mIconByPackageName.put("com.clov4r.android.nil", R.drawable.app_nil);
    	mIconByPackageName.put("com.uc.browser", R.drawable.app_ucbrowser);
    	mIconByPackageName.put("com.tencent.qq", R.drawable.app_qq);
    	mIconByPackageName.put("com.mediatek.FMRadio", R.drawable.app_fmradio);
    	mIconByPackageName.put("com.bel.android.dspmanager", R.drawable.app_dspmanager);
    	mIconByPackageName.put("com.android.notes", R.drawable.app_notes);
    	mIconByPackageName.put("com.android.quicksearchbox", R.drawable.app_search);
    	mIconByPackageName.put("com.android.soundrecorder", R.drawable.app_recorder);
    	mIconByPackageName.put("com.baidu.BaiduMap", R.drawable.app_maps);
    	mIconByPackageName.put("com.mappn.gfan", R.drawable.app_vending);
    	
    	ComponentName component;
    	component = new ComponentName("com.android.contacts", "com.android.contacts.ContactsListActivity");
    	mIconByComponentName.put(component, R.drawable.app_contacts);
    	
    	component = new ComponentName("com.android.htccontacts", "com.android.htccontacts.BrowseLayerCarouselActivity");
    	mIconByComponentName.put(component, R.drawable.app_contacts);
    	
    	component = new ComponentName("com.htc.android.worldclock", "com.htc.android.worldclock.WorldClockTabControl");
    	mIconByComponentName.put(component, R.drawable.app_deskclock);
    	
    	component = new ComponentName("com.htc.album", "com.htc.album.AlbumMain.ActivityMainCarousel");
    	mIconByComponentName.put(component, R.drawable.app_gallery);
    	
    	component = new ComponentName("com.android.contacts", "com.android.contacts.DialtactsActivity");
    	mIconByComponentName.put(component, R.drawable.app_phone);
    	
    	component = new ComponentName("com.android.htcdialer", "com.android.htcdialer.Dialer");
    	mIconByComponentName.put(component, R.drawable.app_phone); 
    	
    	component = new ComponentName("com.htc.flashlight", "com.htc.flashlight.FlashlightActivity");
    	mIconByComponentName.put(component, R.drawable.app_torch);	
    	
    	component = new ComponentName("com.htc.ereader", "com.htc.ereader.activity.GlanceActivity");
    	mIconByComponentName.put(component, R.drawable.app_ireader_free);	
    	
    	component = new ComponentName("com.android.mms", "com.android.mms.ui.ConversationList");
    	mIconByComponentName.put(component, R.drawable.app_smsmms); 
    	
    	component = new ComponentName("com.htc.fm", "com.htc.fm.FMRadio"); 
    	mIconByComponentName.put(component, R.drawable.app_fmradio);
    	
    	component = new ComponentName("com.noshufou.android.su", "com.noshufou.android.su.Su"); 
    	mIconByComponentName.put(component, R.drawable.app_superuser);
    	
    	component = new ComponentName("com.htc.android.Stock", "com.htc.android.Stock.StockWidget"); 
    	mIconByComponentName.put(component, R.drawable.app_monitor);
    	
    	component = new ComponentName("com.htc.music", "com.htc.music.HtcMusic"); 
    	mIconByComponentName.put(component, R.drawable.app_music);
    	
    	component = new ComponentName("com.android.music", "com.android.music.MusicBrowserActivity");	
    	mIconByComponentName.put(component, R.drawable.app_music);
    	
    	component = new ComponentName("com.android.music", "com.android.music.VideoBrowserActivity");	
    	mIconByComponentName.put(component, R.drawable.app_video);
    	
    	component = new ComponentName("com.htc.album", "com.htc.album.TabPluginDevice.ActivityAllVideos");	
    	mIconByComponentName.put(component, R.drawable.app_video);
    	
    	component = new ComponentName("com.htc.Weather", "com.htc.Weather.WeatherActivity");	
    	mIconByComponentName.put(component, R.drawable.app_weather);
    	
    	component = new ComponentName("com.htc.soundrecorder", "com.htc.soundrecorder.SoundRecorderBG");	
    	mIconByComponentName.put(component, R.drawable.app_recorder);
    	
    	component = new ComponentName("com.android.stk", "com.android.stk.StkLauncherActivity");
    	mIconByComponentName.put(component, R.drawable.app_simtoolkit);*/
    }
	
	
//    public static Map<Integer, Bitmap> mBookAnimMap = new HashMap<Integer, Bitmap>();
//    public static void initBookMap(Context context){
//    	mBookAnimMap.put(0, BitmapFactory.decodeResource(context
//				.getResources(), R.drawable.book_anim0));
//    	mBookAnimMap.put(1, BitmapFactory.decodeResource(context
//				.getResources(), R.drawable.book_anim1));
//    	mBookAnimMap.put(2, BitmapFactory.decodeResource(context
//				.getResources(), R.drawable.book_anim2));
//    	mBookAnimMap.put(3, BitmapFactory.decodeResource(context
//				.getResources(), R.drawable.book_anim3));
//    	mBookAnimMap.put(4, BitmapFactory.decodeResource(context
//				.getResources(), R.drawable.book_anim4));
//    	mBookAnimMap.put(5, BitmapFactory.decodeResource(context
//				.getResources(), R.drawable.book_anim5));
//    	mBookAnimMap.put(6, BitmapFactory.decodeResource(context
//				.getResources(), R.drawable.book_anim6));
//    }
     
    public static Map<Integer, Bitmap> mBookEnterAnimMap = new HashMap<Integer, Bitmap>();
    public static void initBookEnterMap(Context context){
    	mBookEnterAnimMap.put(0, BitmapFactory.decodeResource(context
				.getResources(), R.drawable.book_enter_anim0));
    	mBookEnterAnimMap.put(1, BitmapFactory.decodeResource(context
				.getResources(), R.drawable.book_enter_anim1));
    	mBookEnterAnimMap.put(2, BitmapFactory.decodeResource(context
				.getResources(), R.drawable.book_enter_anim2));
    	mBookEnterAnimMap.put(3, BitmapFactory.decodeResource(context
				.getResources(), R.drawable.book_enter_anim3));
    	mBookEnterAnimMap.put(4, BitmapFactory.decodeResource(context
				.getResources(), R.drawable.book_enter_anim4));
    	mBookEnterAnimMap.put(5, BitmapFactory.decodeResource(context
				.getResources(), R.drawable.book_enter_anim5)); 
    }
    
    public static void recycleAllBitmaps(){
    	for(int i=0; i<mBookEnterAnimMap.size(); i++){
    		Bitmap bitmap = mBookEnterAnimMap.get(i);
    		if(bitmap != null){
    			bitmap.recycle();
    		}
    	}
    }
}
