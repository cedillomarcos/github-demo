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
import java.util.List;

import com.rlk.scene.R;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class CellLayout extends ViewGroup{
	public static final boolean DEBUG = true;
	public static final boolean SCROLL_DEBUG = true;
	public static final String TAG="IphoneDragLayer"; //  IphoneCellLayout
	public static final String LOG_TAG="LOG_TAG";
	private static String TAG_MEASURE = "measure";
	
    private boolean mPortrait;
    
    private int mCellWidth;
    private int mCellHeight;
    
    private int mLongAxisStartPadding;
    private int mLongAxisEndPadding;
    private int mShortAxisStartPadding;
    private int mShortAxisEndPadding;
    
    private int mFolderPaddingBottom;
    private int mFolderPanddingTop;
    private int mMaxFolderBottom;

    private int mShortAxisCells;
    private int mLongAxisCells;
    
    private int mWidthGap;
    private int mHeightGap;

    private final Rect mRect = new Rect();
    private final ApplicationInfo mCellInfo = new ApplicationInfo();	//mCellInfo是选中的单元信息
    
    private float mLastMotionX;
    private float mLastMotionY;
    private float mTouchOffsetX;
    private float mTouchOffsetY;
    private boolean mDrawAlpha = false; //是否需要绘制阴影层
    
    int[] mCellXY = new int[2];
    int[] mFirstCellXY = new int[2];
    
    boolean[][] mOccupied;

    private RectF mDragRect = new RectF();

    private boolean mDirtyTag;
    
    private VelocityTracker mVelocityTracker;
    private int mMaximunVelocity;
    private static int mChangeScreenVelocity = 70; 
  
    public CellLayout(Context context) {
        this(context, null);
    }

    public CellLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CellLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CellLayout, defStyle, 0);

        mCellWidth = a.getDimensionPixelSize(R.styleable.CellLayout_cellWidth, 10);
        mCellHeight = a.getDimensionPixelSize(R.styleable.CellLayout_cellHeight, 10);
        
//        if(Launcher.getInstance() != null && Launcher.getInstance().MAIN_MENU){
        	mLongAxisStartPadding = 
                    a.getDimensionPixelSize(R.styleable.CellLayout_longAxisStartPadding, 10);
            mLongAxisEndPadding = 
                a.getDimensionPixelSize(R.styleable.CellLayout_longAxisEndPadding, 10);
//        }else{
//        	mLongAxisStartPadding = (int) context.getResources().getDimension(R.dimen.long_axis_shortcut_start_Padding);
//            mLongAxisEndPadding = (int) context.getResources().getDimension(R.dimen.long_axis_shortcut_end_Padding);
//        }
        
        mShortAxisStartPadding =
            a.getDimensionPixelSize(R.styleable.CellLayout_shortAxisStartPadding, 10);
        mShortAxisEndPadding = 
            a.getDimensionPixelSize(R.styleable.CellLayout_shortAxisEndPadding, 10);
        
        mShortAxisCells = a.getInt(R.styleable.CellLayout_shortAxisCells, 4);//比较短的一端的单元个数
        //Modify GWLLSW-258 ningyaoyun 20120924(on)
        mLongAxisCells = a.getInt(R.styleable.CellLayout_longAxisCells, 5);//比较长的一端的单元个数
        //Modify GWLLSW-258 ningyaoyun 20120924(off)
        a.recycle();
        
        mFolderPaddingBottom = (int) getContext().getResources().getDimension(R.dimen.folder_pandding_bottom);
        mFolderPanddingTop = (int) getContext().getResources().getDimension(R.dimen.folder_pandding_top);
        mMaxFolderBottom = (int) getContext().getResources().getDimension(R.dimen.max_show_folder_bottom);
               
        mMaximunVelocity = ViewConfiguration.get(getContext()).getScaledMaximumFlingVelocity();
        mPortrait = true;
        mWidthGap = Utilities.getWidthGaps(getContext());
        mHeightGap = Utilities.getHeightGaps(getContext());

        setAlwaysDrawnWithCacheEnabled(false);

        if (mOccupied == null) {//用于保存哪个单元被使用了
            if (mPortrait) {
                mOccupied = new boolean[mShortAxisCells][mLongAxisCells];
            } else {//横屏
                mOccupied = new boolean[mLongAxisCells][mShortAxisCells];
            }
        }
    }

    @Override
    public void cancelLongPress() { //不接受长按键
        super.cancelLongPress();
        mDrawAlpha = false;
        // Cancel long press for all children
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            child.cancelLongPress();
        }
    }

    int getCountX() {  //返回X轴方向的单元个数
        return mPortrait ? mShortAxisCells : mLongAxisCells;
    }

    int getCountY() {//返回Y轴方向的单元个数
        return mPortrait ? mLongAxisCells : mShortAxisCells;
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        // Generate an id for each view, this assumes we have at most 256x256 cells
        // per workspace screen
        final LayoutParams cellParams = (LayoutParams) params;
        cellParams.regenerateId = true;
        super.addView(child, index, params);
    }

    @Override
    public void requestChildFocus(View child, View focused) {
        super.requestChildFocus(child, focused);
        if (child != null) {
            Rect r = new Rect();
            child.getDrawingRect(r);
            requestRectangleOnScreen(r);
        }
    }

    @Override
    protected void onAttachedToWindow() {//第一次onDraw之前调用，在onDraw之前调用，在onMeasure之前，之后都有可能调用。
        super.onAttachedToWindow();
        mCellInfo.screen = ((ViewGroup) getParent()).indexOfChild(this);
    }
    
    private boolean found = false;
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {//触摸事件，在onTouchEvent之前调用。主要是为了记录触莫选中单元的信息
        
        CellLayout openFolderCellLayout = Launcher.getInstance().getOpenFolderCellLayout();
        if(openFolderCellLayout != null && this == openFolderCellLayout){
    		return false;
    	}
        
        final int ex = (int) ev.getX();
        final int ey = (int) ev.getY();
    	final int action = ev.getAction();
        if(mVelocityTracker == null){
        	mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
        
        if (action == MotionEvent.ACTION_DOWN) {
        	if(SCROLL_DEBUG) Log.d("test","celllayout onInterceptTouchEvent ACTION_DOWN !");
        	found = false;
            final Rect frame = mRect;
            mLastMotionX = ev.getX();
            mLastMotionY = ev.getY();
            final int x = (int) ev.getX() + this.getScrollX(); //计算坐标的X点，Y点，加scroll是当屏幕翻动后，还可以准确定位。
            final int y = (int) ev.getY() + this.getScrollY();
            final int count = getChildCount();
            
            for (int i = count - 1; i >= 0; i--) {
            	
            	if (!(getChildAt(i) instanceof IphoneBubbleTextView)){
            		continue;
            	}
            		
                final IphoneBubbleTextView child = (IphoneBubbleTextView)getChildAt(i);
                
                if ((child.getVisibility()) == VISIBLE || child.getAnimation() != null) {
                	
                    child.getHitRect(frame);//将view中的位置放到Rect中
                    if (frame.contains(x, y)) {// 选中了view
                        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                        final ApplicationInfo info = (ApplicationInfo) child.getTag();
                        
                        mCellInfo.id = info.id;
                        mCellInfo.iphoneBubbleTextView = child;
                        mCellInfo.cellX = lp.cellX;
                        mCellInfo.cellY = lp.cellY;
                        mCellInfo.spanX = lp.cellHSpan;
                        mCellInfo.spanY = lp.cellVSpan;
                        mCellInfo.screen = Launcher.getScreen();
                        found = true;
                        mDirtyTag = false;
                        mTouchOffsetX = mLastMotionX - frame.left;
                        mTouchOffsetY = mLastMotionY - frame.top; //mTouchOffsetX是指开始拖动时，触摸点离图标左边顶点的偏移量
                        mDrawAlpha = true;
                        invalidate();
                        break;
                    }
                }
            }
            if (DEBUG) Log.d(TAG,"found = " + found);
            
        } else if (action == MotionEvent.ACTION_UP) {//触摸 结束时，清空记录
        	if(SCROLL_DEBUG) Log.d("test","celllayout onInterceptTouchEvent ACTION_UP !");
        	if(mVelocityTracker != null){
        		mVelocityTracker.recycle();
        		mVelocityTracker = null;
        	}
        	mCellInfo.clean();
            mDirtyTag = false;
            mDrawAlpha = false;
            found = false;
            invalidate();
        } else if (action == MotionEvent.ACTION_MOVE){
        	if(SCROLL_DEBUG) Log.d("test","celllayout onInterceptTouchEvent ACTION_MOVE !");
        	mVelocityTracker.computeCurrentVelocity(1000, mMaximunVelocity);
        	if(Math.abs(mVelocityTracker.getXVelocity()) > mChangeScreenVelocity){
        		found = false;
        		return false;
        	}
        	
        	if(Math.abs(ex - mLastMotionX) > LauncherValues.mMinimumStartDropDistance
        			&& Math.abs(ey - mLastMotionY) > LauncherValues.mMinimumStartDropDistance) {
        		if(found){
            		if (LauncherValues.getInstance().isAnim()) {  //如果抖动动画在执行，则拖动图标
            			Launcher.getInstance().getWorkspace().startDrag(mCellInfo);
                    }
            	}
        	}
        }

        return false;
    }

    @Override
    public CellInfo getTag() {
        final CellInfo info = (CellInfo) super.getTag();
        if (mDirtyTag && info != null && info.valid) {
            final boolean portrait = mPortrait;
            final int xCount = portrait ? mShortAxisCells : mLongAxisCells;
            final int yCount = portrait ? mLongAxisCells : mShortAxisCells;

            final boolean[][] occupied = mOccupied;
            findOccupiedCells(xCount, yCount, occupied, null);

            findIntersectingVacantCells(info, info.cellX, info.cellY, xCount, yCount, occupied);

            mDirtyTag = false;
        }
        return info;
    }

    private static void findIntersectingVacantCells(CellInfo cellInfo, int x, int y,
            int xCount, int yCount, boolean[][] occupied) {

        cellInfo.maxVacantSpanX = Integer.MIN_VALUE;
        cellInfo.maxVacantSpanXSpanY = Integer.MIN_VALUE;
        cellInfo.maxVacantSpanY = Integer.MIN_VALUE;
        cellInfo.maxVacantSpanYSpanX = Integer.MIN_VALUE;
        cellInfo.clearVacantCells();

        if (occupied[x][y]) {
            return;
        }

        cellInfo.current.set(x, y, x, y);

        findVacantCell(cellInfo.current, xCount, yCount, occupied, cellInfo);
    }

    private static void findVacantCell(Rect current, int xCount, int yCount, boolean[][] occupied,
            CellInfo cellInfo) {

        addVacantCell(current, cellInfo);

        if (current.left > 0) {
            if (isColumnEmpty(current.left - 1, current.top, current.bottom, occupied)) {
                current.left--;
                findVacantCell(current, xCount, yCount, occupied, cellInfo);
                current.left++;
            }
        }

        if (current.right < xCount - 1) {
            if (isColumnEmpty(current.right + 1, current.top, current.bottom, occupied)) {
                current.right++;
                findVacantCell(current, xCount, yCount, occupied, cellInfo);
                current.right--;
            }
        }

        if (current.top > 0) {
            if (isRowEmpty(current.top - 1, current.left, current.right, occupied)) {
                current.top--;
                findVacantCell(current, xCount, yCount, occupied, cellInfo);
                current.top++;
            }
        }

        if (current.bottom < yCount - 1) {
            if (isRowEmpty(current.bottom + 1, current.left, current.right, occupied)) {
                current.bottom++;
                findVacantCell(current, xCount, yCount, occupied, cellInfo);
                current.bottom--;
            }
        }
    }

    private static void addVacantCell(Rect current, CellInfo cellInfo) {//将当前的单元设置为空闲。并且放到cellInfo中。
        CellInfo.VacantCell cell = CellInfo.VacantCell.acquire();
        cell.cellX = current.left;
        cell.cellY = current.top;
        cell.spanX = current.right - current.left + 1;
        cell.spanY = current.bottom - current.top + 1;
        if (cell.spanX > cellInfo.maxVacantSpanX) {
            cellInfo.maxVacantSpanX = cell.spanX;
            cellInfo.maxVacantSpanXSpanY = cell.spanY;
        }
        if (cell.spanY > cellInfo.maxVacantSpanY) {
            cellInfo.maxVacantSpanY = cell.spanY;
            cellInfo.maxVacantSpanYSpanX = cell.spanX;
        }
//        if (DEBUG) {
//        	Log.d(TAG,"addVacantCell current= " + current.left + "," + current.top + "," + current.right + "," + current.bottom);
//        	Log.d(TAG,cell.toString());
//        }
        cellInfo.vacantCells.add(cell);
//        if (DEBUG) 
//        	Log.d(TAG,"vacantCells.size() = " + cellInfo.vacantCells.size());
    }

    private static boolean isColumnEmpty(int x, int top, int bottom, boolean[][] occupied) {
        for (int y = top; y <= bottom; y++) {
            if (occupied[x][y]) {
                return false;
            }
        }
        return true;
    }

    private static boolean isRowEmpty(int y, int left, int right, boolean[][] occupied) {
        for (int x = left; x <= right; x++) {
            if (occupied[x][y]) {
                return false;
            }
        }
        return true;
    }

    CellInfo findAllVacantCells(boolean[] occupiedCells, View ignoreView) {
        final boolean portrait = mPortrait;
        final int xCount = portrait ? mShortAxisCells : mLongAxisCells;
        final int yCount = portrait ? mLongAxisCells : mShortAxisCells;
        boolean[][] occupied = mOccupied;

        if (occupiedCells != null) {
            for (int y = 0; y < yCount; y++) {
                for (int x = 0; x < xCount; x++) {
                    occupied[x][y] = occupiedCells[y * xCount + x];
                }
            }
        } else {
            findOccupiedCells(xCount, yCount, occupied, ignoreView);
        }

        return findAllVacantCellsFromOccupied(occupied, xCount, yCount);
    }

    /**
     * Variant of findAllVacantCells that uses LauncerModel as its source rather than the 
     * views.
     */
    CellInfo findAllVacantCellsFromOccupied(boolean[][] occupied,
            final int xCount, final int yCount) {
        CellInfo cellInfo = new CellInfo();

        cellInfo.cellX = -1;
        cellInfo.cellY = -1;
        cellInfo.spanY = 0;
        cellInfo.spanX = 0;
        cellInfo.maxVacantSpanX = Integer.MIN_VALUE;
        cellInfo.maxVacantSpanXSpanY = Integer.MIN_VALUE;
        cellInfo.maxVacantSpanY = Integer.MIN_VALUE;
        cellInfo.maxVacantSpanYSpanX = Integer.MIN_VALUE;
        cellInfo.screen = mCellInfo.screen;

        Rect current = cellInfo.current;

        for (int x = 0; x < xCount; x++) {
            for (int y = 0; y < yCount; y++) {
                if (!occupied[x][y]) {
                    current.set(x, y, x, y);
                    findVacantCell(current, xCount, yCount, occupied, cellInfo);
                    occupied[x][y] = true;
                }
            }
        }

        cellInfo.valid = cellInfo.vacantCells.size() > 0;
//        if(DEBUG) Log.d(TAG, "cellInfo.vacantCells.size() = " + cellInfo.vacantCells.size());
        return cellInfo;
    }
    
    public void cellMoveRight(int[] firstVacantLoacation, int[] startLocation, IphoneBubbleTextView dropView,
    		boolean updateDatabase, List<ApplicationInfo> updateLocation){
    	
    	if(firstVacantLoacation[0] == -1 && firstVacantLoacation[1] == -1 
    			|| startLocation[0] == -1 && startLocation[1] == -1 ){
    		return;
    	}
    	
		LayoutParams dropLayoutParams = null;
		ApplicationInfo dropInfo = null;
		if(dropView != null){
			dropLayoutParams = (LayoutParams) dropView.getLayoutParams();
			dropInfo = (ApplicationInfo) dropView.getTag();
		}
		
    	for (int i = 0; i < getChildCount(); i++) {
    		if(getChildAt(i) instanceof IphoneBubbleTextView 
    				&& getChildAt(i).getVisibility() == View.VISIBLE 
    				&& getChildAt(i) != dropView){
    			
    			IphoneBubbleTextView view = (IphoneBubbleTextView) getChildAt(i);
    			LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
    			ApplicationInfo target = (ApplicationInfo) view.getTag();
    			
				if(containInfo(firstVacantLoacation, startLocation, target)){
//					if(view.getAnimation() != null){
//						view.clearAnimation();
//						view.setStopAnim(true);
//					}
//					TranslateAnimation animation = null;
//					if(layoutParams.cellX == 3){
//						animation = new IphoneTranslate( 0, -(mCellWidth + mWidthGap)*3, 0, (mCellHeight+mHeightGap), view);
//					}else{
//						animation = new IphoneTranslate(0, (mCellWidth + mWidthGap), 0, 0, view);
//					}
//					
//					animation.setFillAfter(false);
//					animation.setDuration(1000);
//					animation.setAnimationListener(mAnimationListener);
//					view.startAnimation(animation);
					
					layoutParams.moveRight();
					target.moveTo(layoutParams);
					if(updateDatabase){
						updateLocation.add(target);
					}
				}
			}
    	}
    	if(dropLayoutParams != null){
			dropLayoutParams.moveTo(firstVacantLoacation);
		}
		if(dropInfo != null){
			dropInfo.moveTo(dropLayoutParams);
		}
		CellLayout.this.requestLayout();
    }

	public void cellMoveLeft(int[] firstVacantLoacation, int[] startLocation, IphoneBubbleTextView dropView,
			boolean updateDatabase, List<ApplicationInfo> updateLocation){
    	
    	if(firstVacantLoacation[0] == -1 && firstVacantLoacation[1] == -1 
    			|| startLocation[0] == -1 && startLocation[1] == -1 ){
    		return;
    	}
		
		LayoutParams dropLayoutParams = null;
		ApplicationInfo dropInfo = null;
		if(dropView != null){
			dropLayoutParams = (LayoutParams) dropView.getLayoutParams();
			dropInfo = (ApplicationInfo) dropView.getTag();
		}
		
    	for (int i = 0; i < getChildCount(); i++) {
    		if(getChildAt(i) instanceof IphoneBubbleTextView
					&& getChildAt(i).getVisibility() == View.VISIBLE 
					&& getChildAt(i) != dropView){
    			
    			IphoneBubbleTextView view = (IphoneBubbleTextView) getChildAt(i);
    			LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
    			ApplicationInfo target = (ApplicationInfo) view.getTag();
    			
				if(containInfo(firstVacantLoacation, startLocation, target)){
//					Log.d(LOG_TAG, "moveLeft intent = " + target.intent.toString());
//					Log.d(LOG_TAG, "x= " + target.cellX + "  y=" + target.cellY);
//					if(view.getAnimation() != null){
//						view.clearAnimation();
//					}
//					TranslateAnimation animation = null;
//					if(layoutParams.cellX == 0 && layoutParams.cellY != 0){
//						animation = new IphoneTranslate( 0, (mCellWidth + mWidthGap)*3, 0, -(mCellHeight+mHeightGap), view);
//					}else{
//						animation = new IphoneTranslate(0, -(mCellWidth + mWidthGap), 0, 0, view);
//					}
//					animation.setFillAfter(false); 
//					animation.setDuration(1000);
//					animation.setAnimationListener(mAnimationListener);
//					view.startAnimation(animation);
					
					layoutParams.moveLeft();
					target.moveTo(layoutParams);
					if(updateDatabase){
						updateLocation.add(target);
					}
				}
			}
    	}
    	if(dropLayoutParams != null){
    		dropLayoutParams.moveTo(startLocation);
    	}
    	if(dropInfo != null){
    		dropInfo.moveTo(dropLayoutParams);
    	}
		CellLayout.this.requestLayout();
    }
    
    
//    AnimationListener mAnimationListener = new AnimationListener() {
//		
//		@Override
//		public void onAnimationStart(Animation animation) {
//		}
//		
//		@Override
//		public void onAnimationRepeat(Animation animation) {
//		}
//		
//		@Override
//		public void onAnimationEnd(Animation animation) {
//			IphoneTranslate translate = (IphoneTranslate) animation;
//			IphoneBubbleTextView bubbleTextView = translate.getBubbleTextView();
//			Log.d(LOG_TAG, "bubbleTextView.getVisibility() = " + bubbleTextView.getVisibility());
//			ApplicationInfo info = (ApplicationInfo) bubbleTextView.getTag();
//			LauncherModel.updateItemInDatabase(getContext(), info);
//			if(info.lastAnimCell){
//				Log.d(LOG_TAG, "lastAnimCell finish");
//				CellLayout.this.requestLayout();
//			}
//		}
//	};
    
    public boolean containInfo(int[] start, int[] end, ApplicationInfo target) {
    	int startCellX = start[0] - 1;
    	int startCellY = start[1];
    	int endCellX = end[0];
    	int endCellY = end[1];
    	
    	while(startCellX != endCellX || startCellY != endCellY){
    		startCellX ++;
    		if(startCellX == mShortAxisCells){
    			startCellX = 0;
    			startCellY ++;
    		}
    		if(target.cellX == startCellX && target.cellY == startCellY){
    			if(target.cellX == endCellX && target.cellY == endCellY){
    				target.lastAnimCell = true;
    			}
    			return true;
    		}
    	}
		return false;
	}

    /**
     * Given a point, return the cell that strictly encloses that point 
     * @param x X coordinate of the point
     * @param y Y coordinate of the point
     * @param result Array of 2 ints to hold the x and y coordinate of the cell
     */
    void pointToCellExact(int x, int y, int[] result) {
        final boolean portrait = mPortrait;
        
        final int hStartPadding = portrait ? mShortAxisStartPadding : mLongAxisStartPadding;
        final int vStartPadding = portrait ? mLongAxisStartPadding : mShortAxisStartPadding;

        result[0] = (x - hStartPadding) / (mCellWidth + mWidthGap);
        result[1] = (y - vStartPadding) / (mCellHeight + mHeightGap);

        final int xAxis = portrait ? mShortAxisCells : mLongAxisCells;
        final int yAxis = portrait ? mLongAxisCells : mShortAxisCells;

        if (result[0] < 0) result[0] = 0;
        if (result[0] >= xAxis) result[0] = xAxis - 1;
        if (result[1] < 0) result[1] = 0;
        if (result[1] >= yAxis) result[1] = yAxis - 1;
    }
    
    /**
     * Given a cell coordinate, return the point that represents the upper left corner of that cell
     * 
     * @param cellX X coordinate of the cell 
     * @param cellY Y coordinate of the cell
     * 
     * @param result Array of 2 ints to hold the x and y coordinate of the point
     */
    void cellToPoint(int cellX, int cellY, int[] result) {
        final boolean portrait = mPortrait;
        
        final int hStartPadding = portrait ? mShortAxisStartPadding : mLongAxisStartPadding;
        final int vStartPadding = portrait ? mLongAxisStartPadding : mShortAxisStartPadding;


        result[0] = hStartPadding + cellX * (mCellWidth + mWidthGap);
        result[1] = vStartPadding + cellY * (mCellHeight + mHeightGap);
    }
    
    private boolean mHasExpendView;
	public void hasExpendView(boolean value) {
		mHasExpendView = value;
	}
	
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize =  MeasureSpec.getSize(widthMeasureSpec);
        
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize =  MeasureSpec.getSize(heightMeasureSpec);
        
    	if(DEBUG) Log.d("LOG_TAG", "onMeasure widthSpecSize = " + widthSpecSize 
    			+ "  heightSpecSize" + heightSpecSize + "  cellLayout measuer this " + this.toString());
        
        int count = getChildCount();
        if (DEBUG) Log.d("FolderLinearLayout","CellLayout count " + count);
        
        if (widthSpecMode == MeasureSpec.UNSPECIFIED && heightSpecMode == MeasureSpec.UNSPECIFIED) {
        	int height = 0;
        	if (mPortrait) {
        		if(getMeasuredHeight() <= 0){
        			
        			int lines = count % mShortAxisCells == 0 ? count / mShortAxisCells : count / mShortAxisCells + 1;
		    		height = lines * mCellHeight + (lines - 1) * mHeightGap + mLongAxisStartPadding + mLongAxisEndPadding;
		    		
		    		if(LauncherValues.dragIconStatu == LauncherValues.DRAG_ON_EXPEND_FOLDER 
		    				&& count % mShortAxisCells == 0){
		    			height += (mCellHeight + mHeightGap);
		    		}
//		    		Log.d("measure", "height = " + height + "  lines = " + lines + "  mCellHeight = " + mCellHeight
//		    				+ " mLongAxisStartPadding = " + mLongAxisStartPadding + "  mLongAxisEndPadding = " + mLongAxisEndPadding
//		    				+ "  mHeightGap = " + mHeightGap);
		    		setMeasuredDimension(LauncherValues.mScreenWidth, height);
        		}else{
        			setMeasuredDimension(LauncherValues.mScreenWidth, getMeasuredHeight());
        		}
        	}
        	
        	if (DEBUG) Log.d(TAG,"CellLayout measure height = " + height);
        	return;
        }
        
    	if (DEBUG) Log.d("FolderLinearLayout","CellLayout set child params");
    	for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if(!mHasExpendView){
            	if (mPortrait) {
            		lp.setup(mCellWidth, mCellHeight, mWidthGap, mHeightGap, mShortAxisStartPadding,//除2，2011-1-13 cell左右间距相同
            				mLongAxisStartPadding);
                } else {
            		lp.setup(mCellWidth, mCellHeight, mWidthGap, mHeightGap, mLongAxisStartPadding,
            				mShortAxisStartPadding);
                }
            }
            
            if (lp.regenerateId) {
                child.setId(((getId() & 0xFF) << 20) | (lp.cellX & 0xFF) << 8 | (lp.cellY & 0xFF));
                lp.regenerateId = false;
            }

            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(lp.width, MeasureSpec.EXACTLY);
            int childheightMeasureSpec =
                    MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY);
            child.measure(childWidthMeasureSpec, childheightMeasureSpec);
        }
        setMeasuredDimension(widthSpecSize, heightSpecSize);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    	
    	if (DEBUG) Log.d("LOG_TAG","CellLayout onLayout");
    	
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
	            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
	            
	            int childLeft = lp.x-0;  //3
	            int childTop = lp.y-0;  //17
	            child.layout(childLeft, childTop, childLeft + lp.width, childTop + lp.height);
            }
        }
    }

    @Override
    protected void setChildrenDrawingCacheEnabled(boolean enabled) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View view = getChildAt(i);
            view.setDrawingCacheEnabled(enabled);
            // Update the drawing caches
            view.buildDrawingCache(true);
        }
    }

    @Override
    protected void setChildrenDrawnWithCacheEnabled(boolean enabled) {
        super.setChildrenDrawnWithCacheEnabled(enabled);
    }
    
    @Override
	protected void dispatchDraw(Canvas canvas) {
    	//Modify GWLLSW-404 ningyaoyun 20120924(on)
//    	if (isOpenScaleAnimating) { //prq modify, drawing methods to do animation
//			long currentTime;
//			if (startTime == 0) {
//				startTime = SystemClock.uptimeMillis();
//				currentTime = 0;
//			} else {
//				currentTime = SystemClock.uptimeMillis() - startTime;
//			}
//			if (mStatus == IphoneScaleAnimUtils.OPENING) {
//				mScaleFactor = IphoneScaleAnimUtils.easeOut(currentTime, 2.0f, 1.0f, mAnimationDuration);
//			} else if (mStatus == IphoneScaleAnimUtils.CLOSING) {
//				mScaleFactor = IphoneScaleAnimUtils.easeIn(currentTime, 1.0f, 2.0f, mAnimationDuration);
//			}
//			if (currentTime >= mAnimationDuration) {
//				mScaleFactor = 1.0f;
//				isOpenScaleAnimating = false;
//				
//				if (mStatus == IphoneScaleAnimUtils.OPENING) {
//					mStatus = IphoneScaleAnimUtils.OPEN;
//				} else if (mStatus == IphoneScaleAnimUtils.CLOSING) {
//					mStatus = IphoneScaleAnimUtils.CLOSED;
//					setVisibility(View.GONE);
//				}
//				setChildrenDrawnWithCacheEnabled(false);
//			}
//	    	if (getVisibility() == View.VISIBLE) {
//	    		super.dispatchDraw(canvas);
//	    	}
//		} else {
			super.dispatchDraw(canvas);

	    	if (mDrawAlpha && mCellInfo.iphoneBubbleTextView != null) { //当在图标IphoneBubbleTextView上单击时画图标上的阴影
	    		if(getParent() instanceof Workspace && LauncherValues.getInstance().isHasFolderOpen()){
	    			return;
	    		}
	    		canvas.save();
	    		
		        IphoneBubbleTextView iphoneView = mCellInfo.iphoneBubbleTextView;
		        TextView t =(TextView)((ViewGroup)iphoneView.getChildAt(0)).getChildAt(0);
		        Rect rect = new Rect();
		        iphoneView.getHitRect(rect);
		        Drawable[] a = t.getCompoundDrawables();
		        Bitmap.Config c =a[1].getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
		        Bitmap bitmap = Bitmap.createBitmap(iphoneView.getWidth(), iphoneView.getHeight(), c);
		        Canvas canv  =new Canvas(bitmap);
		        iphoneView.draw(canv);						//将IphoneBubbleTextView画在一个bitmap上
		        Bitmap bitmap2 = bitmap.extractAlpha();		//获得IphoneBubbleTextView的alpha值的bitmap
		    	Paint mInnerPaint = new Paint();			//设置alpha值来实现画阴影
		    	mInnerPaint.setAlpha(this.getResources().getInteger(R.integer.icon_alpha));
		    	mInnerPaint.setAntiAlias(true);
		        canvas.drawBitmap(bitmap2,rect.left,rect.top, mInnerPaint);//画阴影
		        
	            canvas.restore();
	    	}
//		}
    	//Modify GWLLSW-404 ningyaoyun 20120924(off)
    }
	/**
     * Find a vacant area that will fit the given bounds nearest the requested
     * cell location. Uses Euclidean distance to score multiple vacant areas.
     * 
     * @param pixelX The X location at which you want to search for a vacant area.
     * @param pixelY The Y location at which you want to search for a vacant area.
     * @param spanX Horizontal span of the object.
     * @param spanY Vertical span of the object.
     * @param vacantCells Pre-computed set of vacant cells to search.
     * @param recycle Previously returned value to possibly recycle.
     * @return The X, Y cell of a vacant area that can contain this object,
     *         nearest the requested location.
     */
    int[] findNearestVacantArea(int pixelX, int pixelY, int spanX, int spanY,
            CellInfo vacantCells, int[] recycle) {
        
        // Keep track of best-scoring drop area
        final int[] bestXY = recycle != null ? recycle : new int[2];
        final int[] cellXY = mCellXY;
        double bestDistance = Double.MAX_VALUE;
        
        // Bail early if vacant cells aren't valid
        if (!vacantCells.valid) {
            return null;
        }

        // Look across all vacant cells for best fit
        final int size = vacantCells.vacantCells.size();
        
        //prq add for find out first vacant cell
        int nearlest = Integer.MAX_VALUE; 
        for (int i = 0; i < size; i++) {
            final CellInfo.VacantCell cell = vacantCells.vacantCells.get(i);
            
            // Reject if vacant cell isn't our exact size
            if (cell.spanX != spanX || cell.spanY != spanY) {
                continue;
            }
            
            // Score is center distance from requested pixel
            cellToPoint(cell.cellX, cell.cellY, cellXY);
            
            double distance = Math.sqrt(Math.pow(cellXY[0] - pixelX, 2) +
                    Math.pow(cellXY[1] - pixelY, 2));
            if (distance <= bestDistance) {
                bestDistance = distance;
                bestXY[0] = cell.cellX;
                bestXY[1] = cell.cellY;
            }
            
          //prq add for find out first vacant cell
            int tempIndex = cell.cellY * 4 + cell.cellX;
            if (nearlest > tempIndex) {
            	nearlest = tempIndex;
            }
        }
        
      //prq add for find out first vacant cell
        if (nearlest != Integer.MAX_VALUE && nearlest != bestXY[0] *4 + bestXY[1]) {
        	bestXY[0] = nearlest % 4;
            bestXY[1] = nearlest / 4;
            return bestXY;
        }

        // Return null if no suitable location found 
        if (bestDistance < Double.MAX_VALUE) {
            return bestXY;
        } else {
            return null;
        }
    }
    
    /**
     * Drop a child at the specified position
     *
     * @param child The child that is being dropped
     * @param targetXY Destination area to move to
     */
    void onDropChild(View child, int[] targetXY) {//拖放一个单元到指定的位置。同时设定这个指定的单元isDragging=false,清空mDragRect
    	mDrawAlpha = false;
    	if (child != null) {
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
//            if (DEBUG)
//            	Log.d(TAG,"onDropChild lp.cellX = " + lp.cellX + ",lp.cellY = " + lp.cellY);
            if(lp != null){
            lp.cellX = targetXY[0];
            lp.cellY = targetXY[1];
            lp.isDragging = false;
            } 
            mDragRect.setEmpty();
            child.setOnLongClickListener(Launcher.getInstance());
            child.requestLayout();
            invalidate();
        }
    }
    
    void onDropSortChild(View child, int[] targetXY) {//自动排序单元的移动
    	if (child != null) {
    		LayoutParams lp = (LayoutParams) child.getLayoutParams();
    		lp.cellX = targetXY[0];
            lp.cellY = targetXY[1];
            lp.isDragging = false;
            
            mDragRect.setEmpty();
            child.requestLayout();
            invalidate();
    	}
    }
    
    @Override
	public void removeViewInLayout(View view) {
    	mDrawAlpha = false;
    	mCellInfo.clean();
		super.removeViewInLayout(view);
	}

	void onDropAborted(View child) {
    	mDrawAlpha = false;
        if (child != null) {
            ((LayoutParams) child.getLayoutParams()).isDragging = false;
            invalidate();
        }
        mDragRect.setEmpty();
    }

    /**
     * Start dragging the specified child
     * 
     * @param child The child that is being dragged
     */
    void onDragChild(View child) {//开始拖放指定的单元。将指定单元的lp.isDragging设置为true,并且清空原来的拖放区域mDragRect
    	mDrawAlpha = false;
    	LayoutParams lp = (LayoutParams) child.getLayoutParams();
        lp.isDragging = true;
        mDragRect.setEmpty();
    }
    
    /**
     * Computes the required horizontal and vertical cell spans to always 
     * fit the given rectangle.
     *  
     * @param width Width in pixels
     * @param height Height in pixels
     */
    public int[] rectToCell(int width, int height) {//根据宽高计算间距
        // Always assume we're working with the smallest span to make sure we
        // reserve enough space in both orientations.
        final Resources resources = getResources();
        int actualWidth = resources.getDimensionPixelSize(R.dimen.icon_width);
        int actualHeight = resources.getDimensionPixelSize(R.dimen.icon_height);
        int smallerSize = Math.min(actualWidth, actualHeight);

        // Always round up to next largest cell
        int spanX = (width + smallerSize) / smallerSize;
        int spanY = (height + smallerSize) / smallerSize;

        return new int[] { spanX, spanY };
    }

    /**
     * Find the first vacant cell, if there is one.
     *
     * @param vacant Holds the x and y coordinate of the vacant cell
     * @param spanX Horizontal cell span.
     * @param spanY Vertical cell span.
     * 
     * @return True if a vacant cell was found
     */
    public boolean getVacantCell(int[] vacant, int spanX, int spanY, View ignore) {//查找第一个空白位置。
        final boolean portrait = mPortrait;
        final int xCount = portrait ? mShortAxisCells : mLongAxisCells;
        final int yCount = portrait ? mLongAxisCells : mShortAxisCells;
        final boolean[][] occupied = mOccupied;

        findOccupiedCells(xCount, yCount, occupied, ignore);

        return findVacantCell(vacant, spanX, spanY, xCount, yCount, occupied);
    }
    
    public boolean checkVacant(int[] cellXY, View ignore){
        final boolean portrait = mPortrait;
        final int xCount = portrait ? mShortAxisCells : mLongAxisCells;
        final int yCount = portrait ? mLongAxisCells : mShortAxisCells;
        final boolean[][] occupied = mOccupied;
        
    	findOccupiedCells(xCount, yCount, occupied, ignore);
    	
    	return occupied[cellXY[0]][cellXY[1]];
    }
    
    static boolean findVacantCell(int[] vacant, int spanX, int spanY,
            int xCount, int yCount, boolean[][] occupied) {
    	//Modify GWLLSW-258 ningyaoyun 20120924(on)
        for (int y = 0; y < yCount; y++) {
            for (int x = 0; x < xCount; x++) {
                boolean available = !occupied[x][y];
out:            for (int i = x; i < x + spanX - 1 && x < xCount; i++) {
                    for (int j = y; j < y + spanY - 1 && y < yCount; j++) {
                        available = available && !occupied[i][j];
                        if (!available) break out;
                    }
                }

                if (available) {
                    vacant[0] = x;
                    vacant[1] = y;
                    return true;
                }else{
                	vacant[0] = -1;
                	vacant[1] = -1;
                }
            }
        }
    	//Modify GWLLSW-258 ningyaoyun 20120924(off)
        return false;
    }
    //用一维数组存储哪些单元被占用了，顺序按照一行一行的扫描
    boolean[] getOccupiedCells() {
        final boolean portrait = mPortrait;
        final int xCount = portrait ? mShortAxisCells : mLongAxisCells;
        final int yCount = portrait ? mLongAxisCells : mShortAxisCells;
        final boolean[][] occupied = mOccupied;

        findOccupiedCells(xCount, yCount, occupied, null);

        final boolean[] flat = new boolean[xCount * yCount];
        for (int y = 0; y < yCount; y++) {
            for (int x = 0; x < xCount; x++) {
                flat[y * xCount + x] = occupied[x][y];
            }
        }

        return flat;
    }
     //在(4,4)的一个坐标上，标志哪些单元被占用了，结果放到occupied变量中，也就是mOccupied.
    private void findOccupiedCells(int xCount, int yCount, boolean[][] occupied, View ignoreView) {
        for (int x = 0; x < xCount; x++) {
            for (int y = 0; y < yCount; y++) {
                occupied[x][y] = false;
            }
        }

        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child == ignoreView || !(child instanceof IphoneBubbleTextView)) {
                continue;
            }
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (lp.cellX == -1 || lp.cellY == -1)
            	continue;
            for (int x = lp.cellX; x < lp.cellX + lp.cellHSpan && x < xCount; x++) {
                for (int y = lp.cellY; y < lp.cellY + lp.cellVSpan && y < yCount; y++) {
                	occupied[x][y] = true;
                }
            }
        }
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new CellLayout.LayoutParams(getContext(), attrs);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof CellLayout.LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new CellLayout.LayoutParams(p);
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        /**
         * Horizontal location of the item in the grid.
         */
        @ViewDebug.ExportedProperty
        public int cellX;

        /**
         * Vertical location of the item in the grid.
         */
        @ViewDebug.ExportedProperty
        public int cellY;

        /**
         * Number of cells spanned horizontally by the item.
         */
        @ViewDebug.ExportedProperty
        public int cellHSpan;

        /**
         * Number of cells spanned vertically by the item.
         */
        @ViewDebug.ExportedProperty
        public int cellVSpan;
        
        /**
         * Is this item currently being dragged
         */
        public boolean isDragging;

        @ViewDebug.ExportedProperty
        int x;
        @ViewDebug.ExportedProperty
        int y;
        
        int moveUpY;
        
        int moveDownY;

        boolean regenerateId;

        public void moveRight() {
        	if(cellX != 3){
    			cellX += 1;
    		}else {
    			if(cellY < 4){
    				cellX = 0;
    				cellY +=1;
    			}
    		}
		}

		public void moveTo(int[] startLocation) {
			cellX = startLocation[0];
			cellY = startLocation[1];
		}

		public void moveLeft() {
        	if(cellX != 0){
    			cellX -= 1;
    		}else {
    			if(cellY > 0){
    				cellX = 3;
    				cellY -=1;
    			}
    		}
		}
		
		public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            cellHSpan = 1;
            cellVSpan = 1;
        }

		public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
            cellHSpan = 1;
            cellVSpan = 1;
        }
        
        public LayoutParams(int cellX, int cellY, int cellHSpan, int cellVSpan) {
            super(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
            this.cellX = cellX;
            this.cellY = cellY;
            this.cellHSpan = cellHSpan;
            this.cellVSpan = cellVSpan;
        }

        public void setup(int cellWidth, int cellHeight, int widthGap, int heightGap,
                int hStartPadding, int vStartPadding) {
        	
            final int myCellHSpan = cellHSpan;
            final int myCellVSpan = cellVSpan;
            final int myCellX = cellX;
            final int myCellY = cellY;
            width = myCellHSpan * cellWidth + ((myCellHSpan - 1) * widthGap) -
                    leftMargin - rightMargin;
            height = myCellVSpan * cellHeight + ((myCellVSpan - 1) * heightGap) -
                    topMargin - bottomMargin;
            x = hStartPadding + myCellX * (cellWidth + widthGap) + leftMargin;
            y = vStartPadding + myCellY * (cellHeight + heightGap) + topMargin;
        }
    }
    
    private int[] mFolderLocation = new int[2];
    public void setupExpendFolder(LayoutParams lp) {
    	
        IphoneBubbleTextView textView = Launcher.getInstance().getFolderTarget();
        textView.getLocationOnScreen(mFolderLocation);
        
    	if(mPortrait){
//    		if(Launcher.getInstance().isFolderInPanel()){	
//				lp.x = 0;
//				lp.y = mFolderLocation[1] - lp.height - mFolderPaddingBottom;
//				lp.width = LauncherValues.mScreenWidth;
//			}else{
				LayoutParams layoutParams = (LayoutParams) textView.getLayoutParams();
				lp.width = LauncherValues.mScreenWidth;
	    		lp.x = 0;
	    		lp.y = layoutParams.y + mCellHeight - mFolderPanddingTop;
	    		
	    		if(lp.y + lp.height > mMaxFolderBottom){
	            	lp.moveUpY = lp.y + lp.height - mMaxFolderBottom;
	            	lp.moveDownY = mMaxFolderBottom - lp.y;
	            	lp.y -= lp.moveUpY;
	            }else{
	            	lp.moveUpY = 0;
	            	lp.moveDownY = lp.height;
	            }
//			}
    	}
		
    	LayoutParams clp = null;
		for (int i = 0; i < getChildCount(); i++) {
			
			View child = getChildAt(i);
			if(child instanceof IphoneBubbleTextView){
				clp = (LayoutParams) child.getLayoutParams();
//	        	if(Launcher.getInstance().isFolderInPanel()){
//					clp.y -= lp.height - mFolderPaddingBottom;
//				}else{ 
	                if(clp.cellY > lp.cellY-1 && child instanceof IphoneBubbleTextView && lp.moveDownY > 0){
	                	clp.y += lp.moveDownY - mFolderPanddingTop;
	                }
	                if(clp.cellY <= lp.cellY-1 && child instanceof IphoneBubbleTextView && lp.moveUpY > 0){
	                	clp.y -= lp.moveUpY;
	                }
//				}
			}
    	}
	}

    static final class CellInfo implements ContextMenu.ContextMenuInfo {
        /**
         * See View.AttachInfo.InvalidateInfo for futher explanations about
         * the recycling mechanism. In this case, we recycle the vacant cells
         * instances because up to several hundreds can be instanciated when
         * the user long presses an empty cell.
         */
        static final class VacantCell {
            int cellX;
            int cellY;
            int spanX;
            int spanY;

            // We can create up to 523 vacant cells on a 4x4 grid, 100 seems
            // like a reasonable compromise given the size of a VacantCell and
            // the fact that the user is not likely to touch an empty 4x4 grid
            // very often 
            private static final int POOL_LIMIT = 100;
            private static final Object sLock = new Object();

            private static int sAcquiredCount = 0;
            private static VacantCell sRoot;

            private VacantCell next;

            static VacantCell acquire() {
                synchronized (sLock) {
                    if (sRoot == null) {
                        return new VacantCell();
                    }

                    VacantCell info = sRoot;
                    sRoot = info.next;
                    sAcquiredCount--;

                    return info;
                }
            }

            void release() {
                synchronized (sLock) {
                    if (sAcquiredCount < POOL_LIMIT) {
                        sAcquiredCount++;
                        next = sRoot;
                        sRoot = this;
                    }
                }
            }

            @Override
            public String toString() {
                return "VacantCell[x=" + cellX + ", y=" + cellY + ", spanX=" + spanX +
                        ", spanY=" + spanY + "]";
            }
        }

        View cell;
        int cellX;
        int cellY;
        int spanX;
        int spanY;
        int screen;
        boolean valid;

        final ArrayList<VacantCell> vacantCells = new ArrayList<VacantCell>(VacantCell.POOL_LIMIT);
        int maxVacantSpanX;
        int maxVacantSpanXSpanY;
        int maxVacantSpanY;
        int maxVacantSpanYSpanX;
        final Rect current = new Rect();

        void clearVacantCells() {
            final ArrayList<VacantCell> list = vacantCells;
            final int count = list.size(); 
            for (int i = 0; i < count; i++) list.get(i).release(); 
            list.clear();
        }

        void findVacantCellsFromOccupied(boolean[] occupied, int xCount, int yCount) {
            if (cellX < 0 || cellY < 0) {
                maxVacantSpanX = maxVacantSpanXSpanY = Integer.MIN_VALUE;
                maxVacantSpanY = maxVacantSpanYSpanX = Integer.MIN_VALUE;
                clearVacantCells();
                return;
            }

            final boolean[][] unflattened = new boolean[xCount][yCount];
            for (int y = 0; y < yCount; y++) {
                for (int x = 0; x < xCount; x++) {
                    unflattened[x][y] = occupied[y * xCount + x];
                }
            }
            CellLayout.findIntersectingVacantCells(this, cellX, cellY, xCount, yCount, unflattened);
        }

        /**
         * This method can be called only once! Calling #findVacantCellsFromOccupied will
         * restore the ability to call this method.
         *
         * Finds the upper-left coordinate of the first rectangle in the grid that can
         * hold a cell of the specified dimensions.
         *
         * @param cellXY The array that will contain the position of a vacant cell if such a cell
         *               can be found.
         * @param spanX The horizontal span of the cell we want to find.
         * @param spanY The vertical span of the cell we want to find.
         *
         * @return True if a vacant cell of the specified dimension was found, false otherwise.
         */
        boolean findCellForSpan(int[] cellXY, int spanX, int spanY) {
            return findCellForSpan(cellXY, spanX, spanY, true);
        }

        boolean findCellForSpan(int[] cellXY, int spanX, int spanY, boolean clear) {
            final ArrayList<VacantCell> list = vacantCells;
            final int count = list.size();

            boolean found = false;

            if (this.spanX >= spanX && this.spanY >= spanY) {
                cellXY[0] = cellX;
                cellXY[1] = cellY;
                found = true;
            }

            // Look for an exact match first
            for (int i = 0; i < count; i++) {
                VacantCell cell = list.get(i);
                if (cell.spanX == spanX && cell.spanY == spanY) {
                    cellXY[0] = cell.cellX;
                    cellXY[1] = cell.cellY;
                    found = true;
                    break;
                }
            }

            // Look for the first cell large enough
            for (int i = 0; i < count; i++) {
                VacantCell cell = list.get(i);
                if (cell.spanX >= spanX && cell.spanY >= spanY) {
                    cellXY[0] = cell.cellX;
                    cellXY[1] = cell.cellY;
                    found = true;
                    break;
                }
            }

            if (clear) clearVacantCells();

            return found;
        }

        @Override
        public String toString() {  
            return "Cell[view=" + (cell == null ? "null" : cell.getClass()) + ", x=" + cellX +
                    ", y=" + cellY + "]";
        }
    }

	public void addFolderItems(ArrayList<ApplicationInfo> items) {
		if(DEBUG) Log.d(TAG, "expendFolder addFolderItems");
		ArrayList<ApplicationInfo> copyItems = new ArrayList<ApplicationInfo>(items);
		for (int i = 0; i < copyItems.size(); i++) {
			ApplicationInfo item = copyItems.get(i);
			item.cellX = i % mShortAxisCells;
			item.cellY = i / mLongAxisCells;
			item.isFolderItem = true;
			View v = Launcher.getInstance().createFolderShortcut(null,item);
			v.setOnLongClickListener(Launcher.getInstance());
			CellLayout.LayoutParams lp = new CellLayout.LayoutParams(item.cellX, item.cellY, 1, 1);
			addView(v,-1,lp);
		}
	}
	
	public void addFolderItems(ApplicationInfo info, boolean ready) {
		if(DEBUG) Log.d("FolderLinearLayout", "FolderLinearLayout addFolderItems");
		
		int cellX = 0;
		int cellY = 0;
		
		if(ready){
			cellX = info.cellX;
			cellY = info.cellY;
		}else{
			if(getChildCount() > 0){
				View child = getChildAt(getChildCount()-1);
				CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
				if(lp.cellX >= 0 && lp.cellX < mShortAxisCells - 1){
					cellX = lp.cellX + 1;
				}
				if(lp.cellY >= 0 && lp.cellY < mLongAxisCells && cellX == 0){
					cellY = lp.cellY + 1;
				}else{
					cellY = lp.cellY;
				}
			}
		}
		
		info.isFolderItem = true;
		View v = Launcher.getInstance().createShortcut(null,info);
		v.setOnLongClickListener(Launcher.getInstance());
		CellLayout.LayoutParams lp = new CellLayout.LayoutParams(cellX, cellY, 1, 1);
		info.cellX = cellX;
		info.cellY = cellY;
		if(DEBUG) Log.d("FolderLinearLayout", "FolderLinearLayout cellX " + cellX);
		if(DEBUG) Log.d("FolderLinearLayout", "FolderLinearLayout cellY " + cellY);
		addView(v,-1,lp);
	}

	public void stopAnimation(boolean value) {
		for (int i = 0; i < getChildCount(); i++) {
			if(getChildAt(i) instanceof IphoneBubbleTextView){
				IphoneBubbleTextView bubbleTextView = (IphoneBubbleTextView) getChildAt(i);
				bubbleTextView.setStopAnim(value);
			}
		}
	}
	
	public View getChildByCellXY(int cellX, int cellY) {
		int count = getChildCount();
		for(int i = 0; i < count; i++) {
			View cell = getChildAt(i);
			LayoutParams lp = (CellLayout.LayoutParams)cell.getLayoutParams();
			if (cellX == lp.cellX && cellY == lp.cellY) {
				return cell;
			}
		}
		return null;
	}

	public boolean isEmpty() {
		if(getChildCount() == 0){
			if(DEBUG) Log.d("page", "this cellLayout is empty = " + this);
			return true;
		}else if(getChildCount() > 0){
			return false;
		}
		return false;
	}
	
	// prq add for animation for lock screen to unlock screen
	// ADW: Animation vars
	private int mStatus = IphoneScaleAnimUtils.CLOSED;
	private boolean isOpenScaleAnimating;
	private long startTime;
	private float mScaleFactor;
	private int mAnimationDuration = 1000;

	@Override
	protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
		//Modify GWLLSW-404 ningyaoyun 20120924(on)
//		if (isOpenScaleAnimating) {
//			float scale = mScaleFactor;
//			postInvalidate();
//			Bitmap bp = child.getDrawingCache();
//			if (!(child instanceof IphoneBubbleTextView) || bp == null) {
//				super.drawChild(canvas, child, drawingTime);
//				return true;
//			}
//			int distH = (child.getLeft() + (child.getWidth() / 2))
//					- (getWidth() / 2);
//			int distV = (child.getTop() + (child.getHeight() / 2))
//					- (getHeight() / 2);
//			float x = child.getLeft() + (distH * (scale - 1)) * (scale);
//			float y = child.getTop() + (distV * (scale - 1)) * (scale);
//
//			canvas.save();
//			canvas.translate(x, y + child.getPaddingTop());
//			canvas.scale(scale, scale);
//			canvas.drawBitmap(bp, 0, 0, null);
//			canvas.restore();
//		} else {
			super.drawChild(canvas, child, drawingTime);
//		}
		//Modify GWLLSW-404 ningyaoyun 20120924(on)
		return true;
	}

	/**
	 * Open/close public methods
	 */
	public void open(boolean animate) {
		 setChildrenDrawnWithCacheEnabled(true);
        setChildrenDrawingCacheEnabled(true);
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
        	View child = getChildAt(i);
        	child.destroyDrawingCache();
        	child.buildDrawingCache();
        }

		if (getChildCount() <= 0)
        	animate=false;
		if (animate) {
			isOpenScaleAnimating = true;
			mStatus = IphoneScaleAnimUtils.OPENING;
		} else {
			isOpenScaleAnimating = false;
			mStatus = IphoneScaleAnimUtils.OPEN;
		}
		startTime = 0;
//		setVisibility(View.VISIBLE);
		invalidate();
	}

	public void close(boolean animate) {
        if (getChildCount() <= 0)
        	animate = false;
		if (animate) {
			mStatus = IphoneScaleAnimUtils.CLOSING;
			isOpenScaleAnimating = true;
		} else {
			mStatus = IphoneScaleAnimUtils.CLOSED;
			isOpenScaleAnimating = false;
//			setVisibility(View.GONE);
		}
		startTime = 0;
		invalidate();
	}
	
	public void setAnimationSpeed(int speed) {
		mAnimationDuration = speed;
	}
	
	public ApplicationInfo getDropCellInfo(){
		return mCellInfo;
	}

	public IphoneBubbleTextView getLastCell() {
		int count = getVisibaleChild();
		int cellX = (count - 1) % 4;
		int cellY = count % 5 == 0 ? count / 5 : count / 5 - 1;
		for (int i = 0; i < getChildCount(); i++) {
			if(getChildAt(i) instanceof IphoneBubbleTextView){
				IphoneBubbleTextView child = (IphoneBubbleTextView) getChildAt(i);
				ApplicationInfo ai = (ApplicationInfo) child.getTag();
				if(ai.cellX == cellX && ai.cellY == cellY){
					return child;
				}	
			}
		}
		return null;
	}
	
	public void getLastCellLocation(int[] location, int adjust){
		int count = getChildCount() + adjust;
		int cellX = (count - 1) % 4;
		int cellY = count % 4 == 0 ? count / 4 - 1 : count / 4;
		location[0] = cellX;
		location[1] = cellY;
	}

	public int getVisibaleChild() {
		int count = 0;
		for (int i = 0; i < getChildCount(); i++) {
			if(getChildAt(i) instanceof IphoneBubbleTextView
					&& getChildAt(i).getVisibility() == View.VISIBLE){
				count++;
			}
		}
		return count;
	}

	public void getOrderList(ArrayList<Long> itemIds, ArrayList<ApplicationInfo> items) {
		itemIds.clear();
		items.clear();
		int count = getChildCount();
		int cellY = count % 5 == 0 ? count / 5 - 1 : count / 5;
		
		if(DEBUG) Log.d("FolderLinearLayout", "count=" + count);
		if(DEBUG) Log.d("FolderLinearLayout", "cellY=" + cellY);
		
		for (int i = 0; i < cellY + 1; i++) {
			for (int j = 0; j < 5; j++) {
				for (int k = 0; k < getChildCount(); k++) {
					LayoutParams params = (LayoutParams) getChildAt(k).getLayoutParams();
					ApplicationInfo info = (ApplicationInfo) getChildAt(k).getTag();
					if(params.cellX == j && params.cellY == i){
						itemIds.add(info.id);
						items.add(info);
						Log.d("FolderLinearLayout", "add id = " + info.id);
					}
				}
			}
		}
	}
}


