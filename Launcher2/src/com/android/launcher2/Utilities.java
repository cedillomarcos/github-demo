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

package com.android.launcher2;

import java.util.Random;

import android.content.ComponentName;
import android.content.Context;
//renxinquan add start --page_count
import android.content.SharedPreferences;
//renxinquan add end --page_count
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import java.util.List;
import java.util.Arrays;
import com.mediatek.common.featureoption.FeatureOption;
import com.android.launcher.R;


/**
 * Various utilities shared amongst the Launcher's classes.
 */
final class Utilities {
    @SuppressWarnings("unused")
    private static final String TAG = "Launcher.Utilities";

    private static int sIconWidth = -1;
    private static int sIconHeight = -1;
    private static int sIconTextureWidth = -1;
    private static int sIconTextureHeight = -1;

    private static final Paint sBlurPaint = new Paint();
    private static final Paint sGlowColorPressedPaint = new Paint();
    private static final Paint sGlowColorFocusedPaint = new Paint();
    private static final Paint sDisabledPaint = new Paint();
    private static final Rect sOldBounds = new Rect();
    private static final Canvas sCanvas = new Canvas();

    static {
        sCanvas.setDrawFilter(new PaintFlagsDrawFilter(Paint.DITHER_FLAG,
                Paint.FILTER_BITMAP_FLAG));
    }
    static int sColors[] = { 0xffff0000, 0xff00ff00, 0xff0000ff };
    static int sColorIndex = 0;

    //renxinquan add start --page_count
    private static final String my_sharepreference = "my_shareference";
    public static final int defaultPageIndex = 2;
    public static final int defaultPageViewCount = 5;
    public static final int minPageViewCount = 3;
    public static final int maxPageViewCount = 9;
    
    public static int getPageViewCount(Context context){
    	SharedPreferences sp = context.getSharedPreferences(my_sharepreference, context.MODE_PRIVATE);
    	return sp.getInt("page_count", defaultPageViewCount);
    }
    
    public static void setPageViewCount(Context context,int count){
       	SharedPreferences sp = context.getSharedPreferences(my_sharepreference, context.MODE_PRIVATE);
       	sp.edit().putInt("page_count", count).commit();
    }
    
    //renxinquan add end --page_count
    
    
    /**
     * Returns a bitmap suitable for the all apps view. Used to convert pre-ICS
     * icon bitmaps that are stored in the database (which were 74x74 pixels at hdpi size)
     * to the proper size (48dp)
     */
    //GBLLSW-442 zsc 0315 +++
//    static Bitmap createIconBitmap(Bitmap icon, Context context) {
    static Bitmap createIconBitmap(Bitmap icon, Context context, boolean isAppBg) {
    //GBLLSW-442 zsc 0315 ---
    	Log.d(TAG, "createIconBitmap(Bitmap icon, Context context)");

        int textureWidth = sIconTextureWidth;
        int textureHeight = sIconTextureHeight;
        int sourceWidth = icon.getWidth();
        int sourceHeight = icon.getHeight();
        if (sourceWidth > textureWidth && sourceHeight > textureHeight) {
            // Icon is bigger than it should be; clip it (solves the GB->ICS migration case)
            return Bitmap.createBitmap(icon,
                    (sourceWidth - textureWidth) / 2,
                    (sourceHeight - textureHeight) / 2,
                    textureWidth, textureHeight);
        } else if (sourceWidth == textureWidth && sourceHeight == textureHeight) {
            // Icon is the right size, no need to change it
            return icon;
        } else {
            // Icon is too small, render to a larger bitmap
            final Resources resources = context.getResources();
            //GBLLSW-442 zsc 0315 +++
//            return createIconBitmap(new BitmapDrawable(resources, icon), context);
           if (isAppBg) {
        	    return createIconBitmap(new BitmapDrawable(resources, icon), context,true);
			}else{
				return createIconBitmap(new BitmapDrawable(resources, icon), context,false);
		  }
        //GBLLSW-442 zsc 0315 ---
        }
    }

    /**
     * Returns a bitmap suitable for the all apps view.
     */
  //GBLLSW-442 zsc 0315 +++
//    static Bitmap createIconBitmap(Drawable icon, Context context) {
    static Bitmap createIconBitmap(Drawable icon, Context context, boolean isAppBg) {
  //GBLLSW-442 zsc 0315 ---
    	Log.d(TAG, "createIconBitmap(Drawable icon, Context context)");
        synchronized (sCanvas) { // we share the statics :-(
            if (sIconWidth == -1) {
                initStatics(context);
            }

            int width = sIconWidth;
            int height = sIconHeight;

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

            boolean isFastBitmap = false;
            if (icon instanceof FastBitmapDrawable) {
                isFastBitmap = true;
            }

            int sourceWidth = icon.getIntrinsicWidth();
            int sourceHeight = icon.getIntrinsicHeight();
            if (sourceWidth > 0 && sourceHeight > 0) {
                // There are intrinsic sizes.
                if (width < sourceWidth || height < sourceHeight) {
                    // It's too big, scale it down.
                    final float ratio = (float) sourceWidth / sourceHeight;
                    if (sourceWidth > sourceHeight) {
                        height = (int) (width / ratio);
                    } else if (sourceHeight > sourceWidth) {
                        width = (int) (height * ratio);
                    }
                } else if (sourceWidth < width && sourceHeight < height) {
                    // Don't scale up the icon
                    width = sourceWidth;
                    height = sourceHeight;
                }
            }

            // no intrinsic size --> use default size
            int textureWidth = sIconTextureWidth;
            int textureHeight = sIconTextureHeight;

            final Bitmap bitmap = Bitmap.createBitmap(textureWidth, textureHeight,
                    Bitmap.Config.ARGB_8888);
            final Canvas canvas = sCanvas;
            canvas.setBitmap(bitmap);

            final int left = (textureWidth-width) / 2;
            final int top = (textureHeight-height) / 2;

            @SuppressWarnings("all") // suppress dead code warning
            final boolean debug = false;
            if (debug) {
                // draw a big box for the icon for debugging
                canvas.drawColor(sColors[sColorIndex]);
                if (++sColorIndex >= sColors.length) sColorIndex = 0;
                Paint debugPaint = new Paint();
                debugPaint.setColor(0xffcccc00);
                canvas.drawRect(left, top, left+width, top+height, debugPaint);
            }
            //GBLLSW-442 zsc 0315 +++
            if (isAppBg) {
              //Add GBLLSW-170 ningyaoyun 20130308(on)
              if(FeatureOption.RLK_GP818H_A1_SN_SUPPORT || FeatureOption.RLK_GP811H_A1_SUPPORT || Utilities.isAddAppBg){
               Drawable mFrameDrawable = context.getResources().getDrawable(R.drawable.all_apps_bg);
			   mFrameDrawable.setBounds(0, 0, textureWidth, textureHeight);
			   mFrameDrawable.draw(canvas);
			   }
            }
              //Add GBLLSW-170 ningyaoyun 20130308(off)
           //GBLLSW-442 zsc 0315 ---


          //GBLLSW-442 zsc 0315 +++
//            sOldBounds.set(icon.getBounds());
//            icon.setBounds(left, top, left+width, top+height);
//            icon.draw(canvas);
//            icon.setBounds(sOldBounds);
//            canvas.setBitmap(null);
//GPBYY-434 scl add
            if (isAppBg && !isFastBitmap) {
            	sOldBounds.set(icon.getBounds());
                int iconPaddingLeft = left;
            	int iconPaddingTop = top;
            	int iconPaddingRight = left+width;
            	int iconPaddingBottom = top+height;
            	if (width >=(FeatureOption.RLK_GP811H_A1_SUPPORT ? 115 : 82) || height >= (FeatureOption.RLK_GP811H_A1_SUPPORT ? 115 : 82)) {
            		int padding = 13-((FeatureOption.RLK_GP811H_A1_SUPPORT ? 128 : 95)-width);
					iconPaddingLeft = left+padding;
            		iconPaddingTop = top+padding;
            		iconPaddingRight = left+width-padding;
            		iconPaddingBottom = top+height-padding;
				}
				icon.setBounds(iconPaddingLeft, iconPaddingTop, iconPaddingRight, iconPaddingBottom);
                icon.draw(canvas);
                icon.setBounds(sOldBounds);
                canvas.setBitmap(null);
			}else{
				sOldBounds.set(icon.getBounds());
	            icon.setBounds(left, top, left+width, top+height);
	            icon.draw(canvas);
	            icon.setBounds(sOldBounds);
	            canvas.setBitmap(null);
			}
          //GBLLSW-442 zsc 0315 ---

            return bitmap;
        }
    }

    static void drawSelectedAllAppsBitmap(Canvas dest, int destWidth, int destHeight,
            boolean pressed, Bitmap src) {
        synchronized (sCanvas) { // we share the statics :-(
            if (sIconWidth == -1) {
                // We can't have gotten to here without src being initialized, which
                // comes from this file already.  So just assert.
                //initStatics(context);
                throw new RuntimeException("Assertion failed: Utilities not initialized");
            }

            dest.drawColor(0, PorterDuff.Mode.CLEAR);

            int[] xy = new int[2];
            Bitmap mask = src.extractAlpha(sBlurPaint, xy);

            float px = (destWidth - src.getWidth()) / 2;
            float py = (destHeight - src.getHeight()) / 2;
            dest.drawBitmap(mask, px + xy[0], py + xy[1],
                    pressed ? sGlowColorPressedPaint : sGlowColorFocusedPaint);

            mask.recycle();
        }
    }

    /**
     * Returns a Bitmap representing the thumbnail of the specified Bitmap.
     * The size of the thumbnail is defined by the dimension
     * android.R.dimen.launcher_application_icon_size.
     *
     * @param bitmap The bitmap to get a thumbnail of.
     * @param context The application's context.
     *
     * @return A thumbnail for the specified bitmap or the bitmap itself if the
     *         thumbnail could not be created.
     */
    static Bitmap resampleIconBitmap(Bitmap bitmap, Context context) {
        synchronized (sCanvas) { // we share the statics :-(
            if (sIconWidth == -1) {
                initStatics(context);
            }

            if (bitmap.getWidth() == sIconWidth && bitmap.getHeight() == sIconHeight) {
                return bitmap;
            } else {
                final Resources resources = context.getResources();
                //GBLLSW-442 zsc 0315 +++
//                return createIconBitmap(new BitmapDrawable(resources, bitmap), context);
               if (FeatureOption.RLK_GP818H_A1_SN_SUPPORT || FeatureOption.RLK_GP811H_A1_SUPPORT || Utilities.isAddAppBg) {
            	   return createIconBitmap(new BitmapDrawable(resources, bitmap), context,true);
  			   }else{
  				   return createIconBitmap(new BitmapDrawable(resources, bitmap), context,false);
  		       }
                //GBLLSW-442 zsc 0315 ---
            }
        }
    }

    static Bitmap drawDisabledBitmap(Bitmap bitmap, Context context) {
        synchronized (sCanvas) { // we share the statics :-(
            if (sIconWidth == -1) {
                initStatics(context);
            }
            final Bitmap disabled = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
                    Bitmap.Config.ARGB_8888);
            final Canvas canvas = sCanvas;
            canvas.setBitmap(disabled);
            
            canvas.drawBitmap(bitmap, 0.0f, 0.0f, sDisabledPaint);

            canvas.setBitmap(null);

            return disabled;
        }
    }

    private static void initStatics(Context context) {
        final Resources resources = context.getResources();
        final DisplayMetrics metrics = resources.getDisplayMetrics();
        final float density = metrics.density;

        sIconWidth = sIconHeight = (int) resources.getDimension(R.dimen.app_icon_size);
        sIconTextureWidth = sIconTextureHeight = sIconWidth;

        sBlurPaint.setMaskFilter(new BlurMaskFilter(5 * density, BlurMaskFilter.Blur.NORMAL));
        sGlowColorPressedPaint.setColor(0xffffc300);
        sGlowColorFocusedPaint.setColor(0xffff8e00);

        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0.2f);
        sDisabledPaint.setColorFilter(new ColorMatrixColorFilter(cm));
        sDisabledPaint.setAlpha(0x88);
    }

    /** Only works for positive numbers. */
    static int roundToPow2(int n) {
        int orig = n;
        n >>= 1;
        int mask = 0x8000000;
        while (mask != 0 && (n & mask) == 0) {
            mask >>= 1;
        }
        while (mask != 0) {
            n |= mask;
            mask >>= 1;
        }
        n += 1;
        if (n != orig) {
            n <<= 1;
        }
        return n;
    }

    static int generateRandomId() {
        return new Random(System.currentTimeMillis()).nextInt(1 << 24);
    }

    /**
     * M: Check whether the given component name is enabled.
     * 
     * @param context
     * @param cmpName
     * @return true if the component is in default or enable state, and the application is also in default or enable state,
     *         false if in disable or disable user state.
     */
    static boolean isComponentEnabled(final Context context, final ComponentName cmpName) {
        final String pkgName = cmpName.getPackageName();
        final PackageManager pm = context.getPackageManager();
        // Check whether the package has been uninstalled.
        PackageInfo pInfo = null;
        try {
            pInfo = pm.getPackageInfo(pkgName, 0);
        } catch (NameNotFoundException e) {
            LauncherLog.i(TAG, "isComponentEnabled NameNotFoundException: pkgName = " + pkgName);
        }

        if (pInfo == null) {
            LauncherLog.d(TAG, "isComponentEnabled return false because package " + pkgName + " has been uninstalled!");
            return false;
        }

        final int pkgEnableState = pm.getApplicationEnabledSetting(pkgName);
        if (LauncherLog.DEBUG) {
            LauncherLog.d(TAG, "isComponentEnabled: cmpName = " + cmpName + ",pkgEnableState = " + pkgEnableState);
        }
        if (pkgEnableState == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
                || pkgEnableState == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            final int cmpEnableState = pm.getComponentEnabledSetting(cmpName);
            if (LauncherLog.DEBUG) {
                LauncherLog.d(TAG, "isComponentEnabled: cmpEnableState = " + cmpEnableState);
            }
            if (cmpEnableState == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
                    || cmpEnableState == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                return true;
            }
        }

        return false;
    }
    
    //GBLLSW-442 zsc 0315 +++
    public static final boolean isAddAppBg = false;
    public static final int bitmapScale = 15;
    public static final List<String> systemAppsList = Arrays.asList(new String[]{
        	"com.mediatek.datatransfer",
        	"com.android.contacts",
        	"com.mediatek.todos",
        	"com.android.videoeditor",
        	"com.android.email",
        	"com.android.calculator2",
        	"com.mediatek.notebook",
        	"com.android.calculator2",
        	"com.android.browser",
        	"com.android.soundrecorder",
        	"com.android.calendar",
        	"com.android.settings",
        	"com.android.deskclock",
        	"com.mediatek.videoplayer",
        	"com.mediatek.FMRadio",
        	"com.android.gallery3d",
        	"com.mediatek.filemanager",
        	"com.android.quicksearchbox",
        	"com.mediatek.bluetooth",
        	"com.android.providers.downloads.ui",
        	"com.android.cellbroadcastreceiver",
        	"com.android.mms",
        	"com.android.music",
        	"com.mediatek.StkSelection"
    });
    //GBLLSW-442 zsc 0315 ---
}
