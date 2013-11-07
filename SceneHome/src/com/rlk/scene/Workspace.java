package com.rlk.scene;

import static android.util.Log.d;
import static android.util.Log.e;

import java.util.ArrayList;
import java.util.List; 

import com.rlk.scene.R;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;
import android.view.inputmethod.InputMethodManager;
 

/**
 * The workspace is a wide area with a wallpaper and a finite number of screens.
 * Each screen contains a number of icons, folders or widgets the user can
 * interact with. A workspace is meant to be used with a fixed width only.
 */
public class Workspace extends ViewGroup implements DropTarget, DragSource,
		DragScroller {
	public static final boolean DEBUG = true;
	public static final boolean SCROLL_DEBUG = true;
	public static final String TAG_ANIM = "IphoneAnim";
	public static final String TAG = "Workspace"; // IphoneWorkspace
	public static final String LOG_TAG = "Workspace";

	private ArrayList<Animation> mAnim = null; // 建立图标抖动动画，暂时不处理
	private static final int ANIM_COUNT = 5;

	private Context mContext;
	
	private static final int INVALID_SCREEN = -1;
	private static final int FIRST_SCREEN_TRANSLATE = 170;
	/**
	 * The velocity at which a fling gesture will cause us to snap to the next
	 * screen prq modify from 1000 to 100, for improving fling switch screan
	 * experience
	 */
	private static final int SNAP_VELOCITY = 150;

	private int mDefaultScreen;// 默认的屏
	private int mCellLayoutPanBottom;

	// private final WallpaperManager mWallpaperManager; //背景处理，不需要了

	private boolean mFirstLayout = true;// 是否第一次layout,默认为true

	private int mCurrentScreen;//当前屏
    private int mNextScreen = INVALID_SCREEN;//下一屏
	private Scroller mScroller;//翻屏计算器
//    private CustomScroller mScroller;
    private VelocityTracker mVelocityTracker;//移动时的速度计算器

    private ApplicationInfo mDragInfo = new ApplicationInfo(); //当前被拖动的图标信息
    private int[] mLastCell = new int[]{-1,-1};//最后被移动的图标坐标
    
    /**
     * Target drop area calculated during last acceptDrop call.
     */
    private int[] mTargetCell = null;

    private float mLastMotionX;//最后触摸的点
    private float mLastMotionY;

    private final static int TOUCH_STATE_REST = 0;
    private final static int TOUCH_STATE_SCROLLING = 1; 
    private final static int TOUCH_STATE_DOWN = 2;
    private final static int TOUCH_STATE_TAP = 3;

    private int mTouchState = TOUCH_STATE_REST; //触摸状态

    private OnLongClickListener mLongClickListener;//长按键监听器

    private DragController mDragger;//拖放控制器
    
    /**
     * Cache of vacant cells, used during drag events and invalidated as needed.
     */
    private CellLayout.CellInfo mVacantCache = null;
     

    private boolean mAllowLongPress;//是否允许长按键
    private boolean mLocked;//当前屏幕是否被锁定状态，锁定状态不允许操作

    private int mTouchSlop;
    private int mMaximumVelocity;

    final Rect mDrawerBounds = new Rect();
    final Rect mClipBounds = new Rect();
    
    final Paint mPaint = new Paint();
    
    int mDrawerContentHeight;
    int mDrawerContentWidth;
    //2010-12-20
    private int mCurrentScrollX = 0;
//    private boolean mIsTransparentMode = false;
    
    
    private List<ApplicationInfo> mUpdateLocation;
    private List<DropTarget> mWorkspaceDropTargets;
    
    private int mScrollingBounce= 320;
    private int mAnimationDuration=170;
    
    private Runnable mPendingCheckForTap;
    
    /**
     * Used to inflate the Workspace from XML.
     *
     * @param context The application's context.
     * @param attrs The attribtues set containing the Workspace's customization values.
     */
    public Workspace(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    /**
     * Used to inflate the Workspace from XML.
     *
     * @param context The application's context.
     * @param attrs The attribtues set containing the Workspace's customization values.
     * @param defStyle Unused.
     */
    public Workspace(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
  
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Workspace, defStyle, 0);
        mDefaultScreen = a.getInt(R.styleable.Workspace_defaultScreen, 0); 
        mCellLayoutPanBottom = LauncherValues.mScreenHeight - LauncherValues.mLongAxisEndPadding;
        a.recycle();
        
        mContext = context;
        
        initWorkspace();
    }
    
    /**
     * Initializes various states for this workspace.
     */
    private void initWorkspace() {
    	mUpdateLocation = new ArrayList<ApplicationInfo>();
    	mWorkspaceDropTargets = new ArrayList<DropTarget>();
        mScroller = new Scroller(getContext()); 
        mCurrentScreen = mDefaultScreen;
        Launcher.setScreen(mCurrentScreen); 
        
        Log.d("ningyaoyun", "initWorkspace() mCurrentScreen=" + mCurrentScreen);
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();        
        mDeleteList = new ArrayList<CellLayout>();
    }
    
    public Scroller getScroller(){
    	return mScroller;
    }
    
    @Override
    public void addView(View child, int index, LayoutParams params) {
        if (!(child instanceof CellLayout)) {
            throw new IllegalArgumentException("A Workspace can only have CellLayout children.");
        }
        super.addView(child, index, params);
    }

    @Override
    public void addView(View child) {
        if (!(child instanceof CellLayout)) {
            throw new IllegalArgumentException("A Workspace can only have CellLayout children.");
        }
        super.addView(child);
    }

    @Override
    public void addView(View child, int index) {
        if (!(child instanceof CellLayout)) {
            throw new IllegalArgumentException("A Workspace can only have CellLayout children.");
        }
        super.addView(child, index);
    }

    @Override
    public void addView(View child, int width, int height) {
        if (!(child instanceof CellLayout)) {
            throw new IllegalArgumentException("A Workspace can only have CellLayout children.");
        }
        super.addView(child, width, height);
    }

	@Override
    public void addView(View child, LayoutParams params) {
        if (!(child instanceof CellLayout)) {
            throw new IllegalArgumentException("A Workspace can only have CellLayout children.");
        }
        super.addView(child, params);
    }
    
	boolean isDefaultScreenShowing() {
        return mCurrentScreen == mDefaultScreen;
    }

    /**
     * Returns the index of the currently displayed screen.
     *
     * @return The index of the currently displayed screen.
     */
    int getCurrentScreen() {
        return mCurrentScreen;
    }

    /**
     * Sets the current screen.
     *
     * @param currentScreen
     */
    void setCurrentScreen(int currentScreen) {
        clearVacantCache();
        mCurrentScreen = Math.max(0, Math.min(currentScreen, getChildCount() - 1));
        scrollTo(mCurrentScreen * getWidth(), 0);
        invalidate();
    }

    /**
     * Adds the specified child in the current screen. The position and dimensioan of
     * the child are defined by x, y, spanX and spanY.
     *
     * @param child The child to add in one of the workspace's screens.
     * @param x The X position of the child in the screen's grid.
     * @param y The Y position of the child in the screen's grid.
     * @param spanX The number of cells spanned horizontally by the child.
     * @param spanY The number of cells spanned vertically by the child.
     */
    void addInCurrentScreen(View child, int x, int y, int spanX, int spanY) {
        addInScreen(child, mCurrentScreen, x, y, spanX, spanY, false);
    }

    /**
     * Adds the specified child in the current screen. The position and dimension of
     * the child are defined by x, y, spanX and spanY.
     *
     * @param child The child to add in one of the workspace's screens.
     * @param x The X position of the child in the screen's grid.
     * @param y The Y position of the child in the screen's grid.
     * @param spanX The number of cells spanned horizontally by the child.
     * @param spanY The number of cells spanned vertically by the child.
     * @param insert When true, the child is inserted at the beginning of the children list.
     */
    void addInCurrentScreen(View child, int x, int y, int spanX, int spanY, boolean insert) {
        addInScreen(child, mCurrentScreen, x, y, spanX, spanY, insert);
    }

    /**
     * Adds the specified child in the specified screen. The position and dimension of
     * the child are defined by x, y, spanX and spanY.
     *
     * @param child The child to add in one of the workspace's screens.
     * @param screen The screen in which to add the child.
     * @param x The X position of the child in the screen's grid.
     * @param y The Y position of the child in the screen's grid.
     * @param spanX The number of cells spanned horizontally by the child.
     * @param spanY The number of cells spanned vertically by the child.
     */
    void addInScreen(View child, int screen, int x, int y, int spanX, int spanY) {
        addInScreen(child, screen, x, y, spanX, spanY, false);
    }

    /**
     * Adds the specified child in the specified screen. The position and dimension of
     * the child are defined by x, y, spanX and spanY.
     *
     * @param child The child to add in one of the workspace's screens.
     * @param screen The screen in which to add the child.
     * @param x The X position of the child in the screen's grid.
     * @param y The Y position of the child in the screen's grid.
     * @param spanX The number of cells spanned horizontally by the child.
     * @param spanY The number of cells spanned vertically by the child.
     * @param insert When true, the child is inserted at the beginning of the children list.
     */
    void addInScreen(View child, int screen, int x, int y, int spanX, int spanY, boolean insert) {
        if (screen < 0 || screen >= getChildCount()) {
            return;/*throw new IllegalStateException("The screen must be >= 0 and < " + getChildCount());*/
        }
        
        clearVacantCache();
        
        final CellLayout group = (CellLayout) getChildAt(screen);
        CellLayout.LayoutParams lp = new CellLayout.LayoutParams(x, y, spanX, spanY);;
        
        if(child instanceof ExpendFolder){
        	ExpendFolder expendFolder = (ExpendFolder)child;
        	expendFolder.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        	lp.height = expendFolder.getMeasuredHeight();
        	group.setupExpendFolder(lp);
        }else if(child instanceof IphoneBubbleTextView){
        	child.setOnLongClickListener(mLongClickListener);
        }
        
        group.addView(child, insert ? 0 : -1, lp);
    }

    void addWidget(View view, Widget widget, boolean insert) {
		//Modify GWLLSW-895 ningyaoyun 20121019(on)
        addInScreen(view, widget.screen, widget.cellX, widget.cellY, widget.spanX,
                3, insert);
		//Modify GWLLSW-895 ningyaoyun 20121019(off)
    }

    CellLayout.CellInfo findAllVacantCells(boolean[] occupied) {
        CellLayout group = (CellLayout) getChildAt(mCurrentScreen);
        if (group != null) {
            return group.findAllVacantCells(occupied, null);
        }
        return null;
    }

    public void clearVacantCache() {
        if (mVacantCache != null) {
            mVacantCache.clearVacantCells();
            mVacantCache = null;
        }
    }

    /**
     * Registers the specified listener on each screen contained in this workspace.
     *
     * @param l The listener used to respond to long clicks.
     */
    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        mLongClickListener = l;
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).setOnLongClickListener(l);
        }
    }

    public void computeScroll() { 
        if (mScroller.computeScrollOffset()) {
        	if(SCROLL_DEBUG) Log.d("test", "computeScroll mScroller.getCurrX() = " + mScroller.getCurrX());
        	scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
        } else if (mNextScreen != INVALID_SCREEN) {
            mCurrentScreen = Math.max(0, Math.min(mNextScreen, getChildCount() - 1));
            Launcher.setScreen(mCurrentScreen);
            mNextScreen = INVALID_SCREEN;
            clearChildrenCache(); 
            Launcher.getInstance().flushPageGuide();
            setupWorkspaceDropTargets();
        } 
    }
    
    @Override
	protected void dispatchDraw(Canvas canvas) {
		// if(false) Log.d(TAG,"workspace dispatchDraw");
		boolean restore = false;

		// 2010-12-23
		// 当滑动到查询界面
		// 如果继续向左滑动，此时屏蔽TransparentMode
//		if (mCurrentScreen == 0 && mCurrentScrollX == 0
//				&& mTouchState != TOUCH_STATE_SCROLLING) {
//			mIsTransparentMode = false;
//		}

		// 2010-12-23
		// 当滑动到查询界面
		// 如果非mIsTransparentMode模式

		// ViewGroup.dispatchDraw() supports many features we don't need:
		// clip to padding, layout animation, animation listener, disappearing

        boolean fastDraw = mTouchState != TOUCH_STATE_SCROLLING && mNextScreen == INVALID_SCREEN;
		// If we are not scrolling or flinging, draw only the current screen
 
		if (fastDraw) {
			Log.d(TAG, "child = " + getChildAt(mCurrentScreen));
			if(getChildAt(mCurrentScreen) != null){
				drawChild(canvas, getChildAt(mCurrentScreen), getDrawingTime());	
			}else{
				Launcher.getInstance().finish();
			}
		} else {
//			if (Launcher.mIphoneSnapScreenEffectEnable) {
//            	setIconAlpha(false);
//			}
			final long drawingTime = getDrawingTime();
//			Log.d(TAG, "getScrollX()=" + getScrollX() + ";getScrollX=" + getScrollX());
			final float scrollPos = (float) getScrollX() / getWidth();
			final int leftScreen = (int) scrollPos;
			final int rightScreen = leftScreen + 1;
			if (leftScreen >= 0) {
				if (getChildAt(leftScreen) != null) {  
						drawChild(canvas, getChildAt(leftScreen), drawingTime); 
				}
			}
			if (scrollPos != leftScreen && rightScreen < getChildCount()) {
				drawChild(canvas, getChildAt(rightScreen), drawingTime);
			}
		}
 

		if (restore) {
			canvas.restore();
		}
	}
    
    private int preAlpha;
    private void setIconAlpha(boolean reset) {
    	int scrollX = this.getScrollX();
//    	int currentScreen = mCurrentScreen;
    	//set alpha
    	int alpha = 0;	
		if (!reset) {
			if (scrollX <= 320) {
				alpha = (int) (255 * (320 - scrollX) / 320);
			}
			if (alpha < 64) {
				alpha = 0;
			} else if (alpha < 128) {
				alpha = 64;
			} else if (alpha < 192) {
				alpha = 128;
			} else if (alpha < 256) {
				alpha = 192;
			} else {
				alpha = 256;
			}
			if (preAlpha == alpha) {
				return;
			} else {
				preAlpha = alpha;
			}
		} else {
			preAlpha = alpha = 0;
		}
//		if (currentScreen == 0) {
//			final CellLayout screen0 = (CellLayout)getChildAt(0);
//			int childCount = screen0.getChildCount();
//			for (int i = 0; i < childCount ; i++) {
//				View child = screen0.getChildAt(i);
//				boolean result = true;
//				if (child instanceof IphoneBubbleTextView) {
//					result = ((IphoneBubbleTextView)child).setAlpha(alpha);
//				}  
//				if (!result) {
//					break;
//				}
//			}
			
//			final CellLayout screen = (CellLayout)getChildAt(0);
//		    childCount = screen.getChildCount();
//			for (int i = 0; i < childCount ; i++) {
//				View child = screen.getChildAt(i);
//				if (child instanceof IphoneBubbleTextView) {
//					boolean result = ((IphoneBubbleTextView)child).setAlpha(255);
//					if (!result) {
//						break;
//					}
//				}
//			}
//		} else if (currentScreen == 1) {
			alpha = 255 - alpha;
			final CellLayout screen0 = (CellLayout)getChildAt(0);
			if(screen0 != null){
			int childCount = screen0.getChildCount();
			for (int i = 0; i < childCount ; i++) {
				View child = screen0.getChildAt(i);
				boolean result = true;
				if (child instanceof IphoneBubbleTextView) {
					result = ((IphoneBubbleTextView)child).setAlpha(255);
				}  
				if (!result) {
					break;
				}
			}
			}
			
			final CellLayout screen1 = (CellLayout)getChildAt(1);
		    if(screen1 != null){
		    	int childCount = screen1.getChildCount();
			for (int i = 0; i < childCount ; i++) {
				View child = screen1.getChildAt(i);
				if (child instanceof IphoneBubbleTextView) {
					boolean result = ((IphoneBubbleTextView)child).setAlpha(alpha);
					if (!result) {
						break;
					}
					}
				}
			}
//		}
    }
      
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int width = MeasureSpec.getSize(widthMeasureSpec);
 
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
       // if (DEBUG)
       // 	Log.d(TAG," Workspace onMeasure width = " + width + ", widthMode = " + widthMode);
        if (widthMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("Workspace can only be used in EXACTLY mode.");
        }
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("Workspace can only be used in EXACTLY mode.");
        }

        // The children are given the same width and height as the workspace
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
        		getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }

        if (mFirstLayout) {
            scrollTo(mCurrentScreen * width, 0);
            mFirstLayout = false;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int childLeft = 0;

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                final int childWidth = child.getMeasuredWidth();
                child.layout(childLeft, 0, childLeft + childWidth, child.getMeasuredHeight());
                childLeft += childWidth;
            }
        }  
        if (mPageSpacing == 0) {
        	setPageSpacing(((right - left) - getChildAt(0).getMeasuredWidth()) / 2);
		}
    }

    @Override
    public boolean requestChildRectangleOnScreen(View child, Rect rectangle, boolean immediate) {
    	if (DEBUG) Log.d(TAG, "requestChildRectangleOnScreen: immediate " + immediate);
        int screen = indexOfChild(child);
        if (screen != mCurrentScreen || !mScroller.isFinished()) {
            if (!LauncherValues.getInstance().getLauncher().isWorkspaceLocked()) {
//                snapToScreen(screen);
            }
            return true;
        }
        return false;
    }

    @Override
    protected boolean onRequestFocusInDescendants(int direction, Rect previouslyFocusedRect) {
    	if (DEBUG) Log.d(TAG, "onRequestFocusInDescendants: direction " + direction);
        if (true) {
                int focusableScreen;
                if (mNextScreen != INVALID_SCREEN) {
                    focusableScreen = mNextScreen;
                } else {
                    focusableScreen = mCurrentScreen;
                }
                getChildAt(focusableScreen).requestFocus(direction, previouslyFocusedRect);
        }
        return false;
    }

    @Override
    public boolean dispatchUnhandledMove(View focused, int direction) {
    	if (DEBUG) Log.d(TAG, "dispatchUnhandledMove direction : " + direction);
        if (direction == View.FOCUS_LEFT) {
            if (getCurrentScreen() > 0) {
//                snapToScreen(getCurrentScreen() - 1);
                return true;
            }
        } else if (direction == View.FOCUS_RIGHT) {
            if (getCurrentScreen() < getChildCount() - 1) {
//                snapToScreen(getCurrentScreen() + 1);
                return true;
            }
        }
        return super.dispatchUnhandledMove(focused, direction);
    }

    @Override
    public void addFocusables(ArrayList<View> views, int direction, int focusableMode) {
    	if (DEBUG)  Log.d(TAG, "addFocusables: direction " + direction + " focusableMode " + focusableMode);
        if (true) {
            getChildAt(mCurrentScreen).addFocusables(views, direction);
            if (direction == View.FOCUS_LEFT) {
                if (mCurrentScreen > 0) {
                    getChildAt(mCurrentScreen - 1).addFocusables(views, direction);
                }
            } else if (direction == View.FOCUS_RIGHT){
                if (mCurrentScreen < getChildCount() - 1) {
                    getChildAt(mCurrentScreen + 1).addFocusables(views, direction);
                }
            }
        }
    }

    void enableChildrenCache() {
    	final int count = getChildCount();
        for (int i = 0; i < count; i++) {
        	//ADW: create cache only for current screen/previous/next.
        	if(i>=mCurrentScreen-1 || i<=mCurrentScreen+1){
        		final CellLayout layout = (CellLayout) getChildAt(i);
        		layout.setChildrenDrawnWithCacheEnabled(true);
        		layout.setChildrenDrawingCacheEnabled(true);
        	}
        }
    }
    
    void enableChildrenCache(int fromScreen, int toScreen){
        if (fromScreen > toScreen) {
            final int temp = fromScreen;
            fromScreen = toScreen;
            toScreen = temp;
        }
        
        final int count = getChildCount();

        fromScreen = Math.max(fromScreen, 0);
        toScreen = Math.min(toScreen, count - 1);
        for (int i = fromScreen; i <= toScreen; i++) {
            final CellLayout layout = (CellLayout) getChildAt(i);
            layout.setChildrenDrawnWithCacheEnabled(true);
            layout.setChildrenDrawingCacheEnabled(true);
        }
    }

//    void enableChildrenCache() {
//        final int count = getChildCount();
//        for (int i = 0; i < count; i++) {
//            final CellLayout layout = (CellLayout) getChildAt(i);
//            layout.setChildrenDrawnWithCacheEnabled(true);
//            layout.setChildrenDrawingCacheEnabled(true);
//        }
//    }

    void clearChildrenCache() {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final CellLayout layout = (CellLayout) getChildAt(i);
            layout.setChildrenDrawnWithCacheEnabled(false);
        }
    }
    
    public void addCellLayout(){
    	Log.d("page", "addCellLayout");
    	LayoutInflater inflater = Launcher.getInstance().getLayoutInflater();
    	CellLayout cellLayout = (CellLayout)inflater.inflate(R.layout.workspace_screen, null);
    	cellLayout.setOnLongClickListener(mLongClickListener);
    	addView(cellLayout);
    	Launcher.setScreenCount(Launcher.getScreenCount() + 1);
    	IphonePageGuide pageGuide = Launcher.getInstance().getPageGuide();
    	pageGuide.addPage();
    }
    
    private List<CellLayout> mDeleteList;
    public int deleteCellLayout(){
    	mDeleteList.clear();
    	int childCount = getChildCount();
    	if(DEBUG) Log.d("page", "check childCount = " + childCount + " mCurrentScreen=" + mCurrentScreen);
    	
    	//从第始终保留第一页和第二页
    	for (int i = 2; i < childCount; i++) {
    		if(DEBUG) Log.d("page", "check cellLayout = " + getChildAt(i) + "  i = " + i);
			if(getChildAt(i) instanceof CellLayout){
				CellLayout cellLayout = (CellLayout)getChildAt(i);
				if(cellLayout.isEmpty()){
					mDeleteList.add(cellLayout);
					if(i <= mCurrentScreen){
						mCurrentScreen--;
					}
				}
			}
		}
    	
    	for(CellLayout layout : mDeleteList){
    		removeView(layout);
    	}
    	
    	if(mDeleteList.size() > 0){
    		if(DEBUG) Log.d("page", "after delete getChildCount() = " + getChildCount());
    		Launcher.setScreenCount(getChildCount());
    		if(DEBUG) Log.d("page", "currentPage = " + mCurrentScreen);
			Launcher.getInstance().getPageGuide().setCurrentPage(mCurrentScreen);
			snapToScreen(mCurrentScreen);
    		updateCellInfo();	//更新数据库
    	}
    	
    	return mDeleteList.size();
    }
    
    private void updateCellInfo() {
		for (int ii = 0; ii < getChildCount(); ii++) {
			CellLayout cellLayout = (CellLayout) getChildAt(ii);
			for (int jj = 0; jj < cellLayout.getChildCount(); jj++) {
				if(cellLayout.getChildAt(jj) instanceof IphoneBubbleTextView){
					IphoneBubbleTextView textView = (IphoneBubbleTextView) cellLayout.getChildAt(jj);
					ApplicationInfo info = (ApplicationInfo) textView.getTag();
					if(info.screen != ii){
						if(DEBUG) Log.d("page", "updateinfo cellLayout = " + cellLayout);
						info.screen = ii;
						LauncherModel.updateItemInDatabase(getContext(), info);
					}
				}
			}
		}
	}
     
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//    	if(DEBUG) Log.d(TAG, "workspace onInterceptTouchEvent");
    	
        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE) && (mTouchState != TOUCH_STATE_REST)) {
            return true;
        }
        
        final float x = ev.getX();
        final float y = ev.getY();

        switch (action & MotionEvent.ACTION_MASK) {
        
	        case MotionEvent.ACTION_DOWN: {
	        	if(SCROLL_DEBUG) Log.d("test", "onInterceptTouchEvent ACTION_DOWN");
	            mLastMotionX = x;
	            mLastMotionY = y;
	            mAllowLongPress = true;
	            mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;
	            break;
	        }
	        
            case MotionEvent.ACTION_MOVE: {
            	if(SCROLL_DEBUG) Log.d("test", "onInterceptTouchEvent ACTION_MOVE");
                final int xDiff = (int) Math.abs(x - mLastMotionX);
                final int yDiff = (int) Math.abs(y - mLastMotionY);

                final int touchSlop = mTouchSlop;
                boolean xMoved = xDiff > touchSlop;
                boolean yMoved = yDiff > touchSlop;
                
                if(SCROLL_DEBUG) Log.d("test", "xMoved = " + xMoved + " yMoved = " + yMoved);
                if (xMoved || yMoved) {
                    if (xMoved) {
                        mTouchState = TOUCH_STATE_SCROLLING;
                        //enableChildrenCache();
			            enableChildrenCache(mCurrentScreen - 1, mCurrentScreen + 1);
                    }
                    if (mAllowLongPress) {
                        mAllowLongPress = false;

                        final View currentScreen = getChildAt(mCurrentScreen);
                        currentScreen.cancelLongPress();
                    }
                }
                break;
            }
            
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
            	if(SCROLL_DEBUG) Log.d("test", "onInterceptTouchEvent ACTION_UP");

                clearChildrenCache();
                mTouchState = TOUCH_STATE_REST;
                mAllowLongPress = false;
                break;
        }
        
        if(SCROLL_DEBUG) Log.d("test", "onInterceptTouchEvent return = " + (mTouchState != TOUCH_STATE_REST));
        return mTouchState != TOUCH_STATE_REST;
    }
    
    private int mActionMoveCount = 0;
    private int mMoveDeltaX = 0; 
    private boolean isScrollLeft = false; 
    @Override
    public boolean onTouchEvent(MotionEvent ev) { 
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
        final int action = ev.getAction();
        final float x = ev.getX();
        final float y = ev.getY();
        
        switch (action) {
        case MotionEvent.ACTION_DOWN:
        	if(SCROLL_DEBUG) Log.d("test", "onTouchEvent ACTION_DOWN");
        	
            if (!mScroller.isFinished()) {
                mScroller.abortAnimation();
				enableChildrenCache(mCurrentScreen - 1, mCurrentScreen + 1);
                mTouchState = TOUCH_STATE_DOWN;
            } 
            mLastMotionX = x;
            mLastMotionY = y;
            break;
        case MotionEvent.ACTION_MOVE:
            //transparent panel不能滑动屏幕	2011－1－5
        	//mFolderView != null 当有文件夹展开时不能滑动屏幕
        	
        	if (mLastMotionY > mCellLayoutPanBottom || LauncherValues.getInstance().isHasFolderOpen()) {
        		mTouchState = TOUCH_STATE_REST;
        	} else if (mTouchState == TOUCH_STATE_SCROLLING || mTouchState == TOUCH_STATE_DOWN) {
        		mActionMoveCount++;
                int deltaX = (int)(mLastMotionX - x);
                mMoveDeltaX = deltaX;
            	mTouchState = TOUCH_STATE_SCROLLING;
            	mLastMotionX = x;
            	
//            	convertToTransparentMode(getScrollX());
                
                if(SCROLL_DEBUG) Log.d("test","ACTION_MOVE deltaX = " + deltaX);
                
                if (deltaX < 0) {
                	isScrollLeft = false; 
					if (mCurrentScreen > 0) {
						scrollBy(Math.min(deltaX, mScrollingBounce), 0);
					} else if (mCurrentScreen == 0) {
						if (getScrollX() > 0) {
							if (Math.abs(deltaX) > getScrollX()) {
								deltaX = -getScrollX();
							}
							scrollBy(deltaX, 0);
						}
					} 
                } else if (deltaX > 0) {
                	isScrollLeft = true;
                    final float availableToScroll = getChildAt(getChildCount() - 1).getRight() -
                    		getScrollX() - getWidth() + mScrollingBounce;
                    if (availableToScroll > 0) {
                    	scrollBy(deltaX, 0);
                    }
                }
            }
            break;
        case MotionEvent.ACTION_UP:
        	if(SCROLL_DEBUG) Log.d("test", "onTouchEvent ACTION_UP");
        	
            if (mTouchState == TOUCH_STATE_SCROLLING) {     
            	setIconAlpha(true);//add for GWWLSW-359 hujiaxuan 20130207
                Log.d(TAG, "snapToScreen LauncherValues.getInstance().isHasFolderOpen() = " + LauncherValues.getInstance().isHasFolderOpen());	 
                if(LauncherValues.getInstance().isHasFolderOpen()){
                	Launcher.getInstance().closeFolderForPress();
                }
            	
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int velocityX = (int) velocityTracker.getXVelocity();
                
                int deltaX = (int)(mLastMotionX - x);
                if(SCROLL_DEBUG) Log.d("test", "velocityX = "  + velocityX + " SNAP_VELOCITY = " + SNAP_VELOCITY 
                		+ " mMaximumVelocity = " + mMaximumVelocity + " deltaX = " + deltaX); 
                if(velocityX == 0 && mActionMoveCount == 1 && mMoveDeltaX < 0 && mCurrentScreen > 0){
                	snapToScreen(mCurrentScreen - 1);
                } else if (velocityX == 0 && mActionMoveCount == 1 && mMoveDeltaX > 0 && mCurrentScreen < getChildCount() - 1){
                	snapToScreen(mCurrentScreen + 1);
                } else if(velocityX == 0 && deltaX < 0 && mCurrentScreen > 0){
                	snapToScreen(mCurrentScreen - 1);
                } else if(velocityX == 0 && deltaX > 0 && mCurrentScreen < getChildCount() - 1){
                	snapToScreen(mCurrentScreen + 1);
                } else if (velocityX > SNAP_VELOCITY && mCurrentScreen > 0) {
                	snapToScreen(mCurrentScreen - 1);
                } else if (velocityX < -SNAP_VELOCITY && mCurrentScreen < getChildCount() - 1) {
                	snapToScreen(mCurrentScreen + 1);
                } else {
                	snapToDestination();
                }
                
                mTouchState = TOUCH_STATE_REST;
                if (velocityTracker != null) {
                	velocityTracker.recycle();
                    mVelocityTracker = null;
                }
                mActionMoveCount = 0;
            } 
        case MotionEvent.ACTION_CANCEL:
            mTouchState = TOUCH_STATE_REST;
            break;
        }

        return true;
    }
    
    public void maybeSreenOffSnap() {
    	if (mCurrentScreen == 0) {
    		moveToDefaultScreen();
    	} else {
    		snapToDestination();
    	}
    }
    
	public void setupWorkspaceDropTargets(){
		mWorkspaceDropTargets.clear();
		CellLayout currentLayout = (CellLayout) getChildAt(Launcher.getScreen());
		for (int i = 0; i < currentLayout.getChildCount(); i++) {
			View child = currentLayout.getChildAt(i);
			if(child instanceof DropTarget){
				mWorkspaceDropTargets.add((DropTarget) child);
			}
		}
		mWorkspaceDropTargets.add((DropTarget) this);
	}
	
	public List<DropTarget> getDropTargets(){
		return mWorkspaceDropTargets;
	}

    private void snapToDestination() {
        final int screenWidth = getWidth();
        final int whichScreen = (getScrollX() + (screenWidth / 2)) / screenWidth;
        snapToScreen(whichScreen);
    }

    void snapToScreen(int whichScreen) {
    	Log.d(LOG_TAG, "snapToScreen whichScreen=" + whichScreen);
        if (!mScroller.isFinished()) return;
        
        Launcher.getInstance().removeShowFolder();
        clearVacantCache();
        enableChildrenCache();

        whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
        boolean changingScreens = whichScreen != Launcher.getScreen();
 
        mNextScreen = whichScreen;
        
        View focusedChild = getFocusedChild();
        if (focusedChild != null && changingScreens && focusedChild == getChildAt(mCurrentScreen)) {
            focusedChild.clearFocus();
        } 
        
        final int newX = whichScreen * getWidth();
        final int deltaX = newX - getScrollX(); 
        
        mScroller.startScroll(getScrollX(), 0, deltaX, 0, Math.abs(deltaX) * 2); 
        Launcher.setScreen(whichScreen);
        invalidate();
    }
    
    void startDrag(ApplicationInfo cellInfo) {
        View child = cellInfo.iphoneBubbleTextView;		//拖动的单元，是IphoneBubbleTextView
        
        if (child == null || cellInfo.id == -1){
        	return;
        }
        
        // Make sure the drag was started by a long press as opposed to a long click.
        // Note that Search takes focus when clicked rather than entering touch mode
        
        
        mDragInfo = cellInfo;
        mStartMoveLocation[0] = -1;
        mStartMoveLocation[1] = -1;
        mFirstVacantLocation[0] = -1;
        mFirstVacantLocation[1] = -1;
        
        CellLayout current = ((CellLayout) getChildAt(Launcher.getScreen()));  
        if(LauncherValues.getInstance().isHasFolderOpen()){
        	FolderLinearLayout folderLinearLayout = Launcher.getInstance().getExpendFolderView().getBody();
        	CellLayout parent = folderLinearLayout.getCellLayout();
        	parent.onDragChild(child);
        	folderLinearLayout.showTitleEditText();
        	folderLinearLayout.resetParameter();
        	mDragger.startDrag(child, folderLinearLayout, child.getTag(), DragController.DRAG_ACTION_MOVE);
        } else {                                                                                                                                                                                                                                                  
            current.onDragChild(child);
        	mDragger.startDrag(child, this, child.getTag(), DragController.DRAG_ACTION_MOVE);
        }
        invalidate();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final SavedState state = new SavedState(super.onSaveInstanceState());
        state.currentScreen = mCurrentScreen;
        return state;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        if(DEBUG) Log.d(TAG, "savedState.currentScreen = " + savedState.currentScreen);
        if (savedState.currentScreen != -1) {
            mCurrentScreen = savedState.currentScreen;
            Launcher.setScreen(mCurrentScreen);
        }
    }

    void addApplicationShortcut(ApplicationInfo info, CellLayout.CellInfo cellInfo,
            boolean insertAtFirst) {
        final CellLayout layout = (CellLayout) getChildAt(cellInfo.screen);
        final int[] result = new int[2];

        layout.cellToPoint(cellInfo.cellX, cellInfo.cellY, result);
        onDropExternal(null,result[0], result[1], info, layout, insertAtFirst);
    }

    public void onDrop(DragSource source, int x, int y, int xOffset, int yOffset, Object dragInfo) {
    	if(DEBUG) Log.d(LOG_TAG, "----Workspace onDrop!");
    	
    	reset();
    	ApplicationInfo info = (ApplicationInfo) dragInfo;
    	
		if(LauncherValues.dragIconStatu == LauncherValues.DRAG_ON_EXPEND_FOLDER){ //添加图标到展开的目录中
			ExpendFolder expendFolder = Launcher.getInstance().getExpendFolderView();
			FolderLinearLayout layout = (FolderLinearLayout) expendFolder.findViewById(R.id.body);
			layout.onDrop(source, x, y, xOffset, yOffset, dragInfo);
			LauncherValues.dragIconStatu = LauncherValues.DRAG_NO_STATU;
		}else{
			
			int index = mScroller.isFinished() ? mCurrentScreen : mNextScreen;   
			final CellLayout cellLayout = (CellLayout) getChildAt(index);
			if(Launcher.getScreen() == getChildCount() - 1 && Launcher.getScreen() < LauncherValues.mMaxScreenCount){	//当把图标放到最后一页，并不是最大页数时加一页
				addCellLayout();
			}
			
	        
            if (mDragInfo != null) {
            	
                View cell = mDragInfo.iphoneBubbleTextView;
                if (index != mDragInfo.screen && source instanceof Workspace) {
                    final CellLayout originalCellLayout = (CellLayout) getChildAt(mDragInfo.screen);
                    originalCellLayout.removeView(cell);
                    cellLayout.addView(cell);
                    if(info instanceof FolderInfo){
                    	FolderInfo folderInfo = (FolderInfo) info;
                    	mUpdateLocation.clear();
                    	for(ApplicationInfo ai : folderInfo.items){
                    		ai.screen = index;
                    		mUpdateLocation.add(ai);
                    	}
                    	LauncherModel.updateBatchInDatabase(getContext(), mUpdateLocation);
                    }
                    info.screen = index;
                }
                
                if(source instanceof FolderLinearLayout){
                	info.isFolderItem = false;
                	cell = Launcher.getInstance().createShortcut(cellLayout, info);
                	cellLayout.addView(cell);
                	Launcher.getModel().addDesktopItem(info);
                } 
                
                mTargetCell = estimateDropCell(x - xOffset, y - yOffset,
                		mDragInfo.spanX, mDragInfo.spanY, cell, cellLayout, mTargetCell);
                
                cellLayout.onDropChild(cell, mTargetCell);
                
                CellLayout.LayoutParams lp = (CellLayout.LayoutParams) cell.getLayoutParams();
               	LauncherModel.moveItemInDatabase(getContext(), info, LauncherSettings.Favorites.CONTAINER_DESKTOP
               			, index, lp.cellX, lp.cellY);
            }
		}
    }

	private void reset() {
        mStartMoveLocation[0] = -1;
        mStartMoveLocation[1] = -1;
        mFirstVacantLocation[0] = -1;
        mFirstVacantLocation[1] = -1;
	}
	
	public void onDragEnter(DragSource source, int x, int y, int xOffset, int yOffset,
            Object dragInfo) {
    	if(DEBUG) Log.d(LOG_TAG,"Workspace onDragEnter!");
		if(LauncherValues.dragIconStatu == LauncherValues.DRAG_ON_EXPEND_FOLDER){
			return;
		}else{
			clearVacantCache();
		}
    }
    
    private int[] mStartMoveLocation = new int[]{-1,-1};
    private int[] mFirstVacantLocation = new int[]{-1,-1};
    private int[] mLastVacantLocation = new int[]{-1,-1};
    private int[] mCellXY = new int[]{-1,-1};
    
	private int[] mNextCellLayoutStart = new int[]{-1,-1};
    private int[] mNextCellLayoutEnd = new int[]{-1,-1};
    
    public void onDragOver(DragSource source, int x, int y, int xOffset, int yOffset,
            Object dragInfo, boolean isDrop) { 
//    	if(DEBUG) Log.d(LOG_TAG,"Workspace onDragOver!");
    	
    	CellLayout cellLayout = null;
    	int sortScreen = -1;
    	ApplicationInfo info = (ApplicationInfo) dragInfo;
    	int[] cellXY = mCellXY;
    	int[] firstVacantLoacation = mFirstVacantLocation;
    	
    	if(!mScroller.isFinished() && !isDrop || LauncherValues.getInstance().isHasFolderOpen()
    			|| LauncherValues.dragIconStatu == LauncherValues.DRAG_ON_EXPEND_FOLDER){
    		return;
    	}else if(!mScroller.isFinished() && isDrop){
    		if(mNextScreen != INVALID_SCREEN){
    			cellLayout = (CellLayout) getChildAt(mNextScreen);
    			sortScreen = mNextScreen;
    		}
    	}else if(mScroller.isFinished()){
    		cellLayout = (CellLayout) getChildAt(Launcher.getScreen());
    		sortScreen = Launcher.getScreen();
    	}
    	
    	cellLayout.pointToCellExact(x, y, cellXY);
    	
//    	if(DEBUG) Log.d(LOG_TAG, "-----sortScreen = " + sortScreen);
//    	if(DEBUG) Log.d(LOG_TAG, "-----info.screen = "  + info.screen);
//    	if(DEBUG) Log.d(LOG_TAG, "cellX = " + cellXY[0] + "  cellY = " + cellXY[1]);
//    	if(DEBUG) Log.d(LOG_TAG, "firstVacantLoacation[0] = " + firstVacantLoacation[0] 
//                             + "  firstVacantLoacation[1] = " + firstVacantLoacation[1]);

    	if((mStartMoveLocation[0] == cellXY[0] && mStartMoveLocation[1] == cellXY[1] 
                && info.screen == Launcher.getScreen())){
//    		Log.d(LOG_TAG, "return true");
    		return;
    	}
    	
    	
    	mStartMoveLocation[0] = cellXY[0];
    	mStartMoveLocation[1] = cellXY[1];
    	mUpdateLocation.clear();
    	
    	if(info.screen > 0 && info.screen < sortScreen){
    		
    		CellLayout previousLayout = (CellLayout) getChildAt(info.screen);
    		previousLayout.getVacantCell(mNextCellLayoutStart, 1, 1, info.iphoneBubbleTextView);
    		previousLayout.getLastCellLocation(mNextCellLayoutEnd, 1);
    		previousLayout.cellMoveLeft(mNextCellLayoutStart, mNextCellLayoutEnd, info.iphoneBubbleTextView, true, mUpdateLocation);
    		info.screen = sortScreen;
    		
    		if(cellLayout.getVacantCell(firstVacantLoacation, 1, 1, info.iphoneBubbleTextView)){
    			if(checkMoveLocation(mStartMoveLocation, firstVacantLoacation) < 0){
    				cellLayout.cellMoveRight(mStartMoveLocation, firstVacantLoacation, info.iphoneBubbleTextView, true, mUpdateLocation);
    			}
    		}else {
				//Modify GWLLSW-946 ningyaoyun 20121023(on)
    			firstVacantLoacation[0] = 3;
    			firstVacantLoacation[1] = 4;
				//Modify GWLLSW-946 ningyaoyun 20121023(off)
    			IphoneBubbleTextView lastTextView = cellLayout.getLastCell();
    			cellLayout.cellMoveRight(mStartMoveLocation, firstVacantLoacation, null, true, mUpdateLocation);
				info.cellX = mStartMoveLocation[0];
				info.cellY = mStartMoveLocation[1];
    			fullCellLayoutMove(cellLayout,lastTextView);
    		}
    	}else if (info.screen > sortScreen && info.screen < LauncherValues.mMaxScreenCount){
    		
    		CellLayout previousLayout = (CellLayout) getChildAt(info.screen);
    		info.screen = sortScreen;
    		
    		if(cellLayout.getVacantCell(firstVacantLoacation, 1, 1, info.iphoneBubbleTextView)){
    			
    			previousLayout.getLastCellLocation(mNextCellLayoutEnd, 1);
    			previousLayout.getVacantCell(mNextCellLayoutStart, 1, 1, info.iphoneBubbleTextView);
        		
        		if(checkMoveLocation(mNextCellLayoutStart, mNextCellLayoutEnd) < 0){
        			previousLayout.cellMoveLeft(mNextCellLayoutStart, mNextCellLayoutEnd, info.iphoneBubbleTextView, true, mUpdateLocation);
        		}
    			if(checkMoveLocation(mStartMoveLocation, firstVacantLoacation) < 0){
    				cellLayout.cellMoveRight(mStartMoveLocation, firstVacantLoacation, info.iphoneBubbleTextView, true, mUpdateLocation);
    			}
    		}else {
    			firstVacantLoacation[0] = 3;
								//Modify GWLLSW-946 ningyaoyun 20121023(on)
    			firstVacantLoacation[1] = 4;
								//Modify GWLLSW-946 ningyaoyun 20121023(off)
    			IphoneBubbleTextView lastTextView = cellLayout.getLastCell();
    			ApplicationInfo lastInfo = (ApplicationInfo) lastTextView.getTag();
    			changeToFirstLocation(lastTextView);
    			cellLayout.cellMoveRight(mStartMoveLocation, firstVacantLoacation, null, true, mUpdateLocation);
    			cellLayout.removeView(lastTextView);
				previousLayout.cellMoveRight(new int[]{0,0}, new int[]{info.cellX, info.cellY}, null, true, mUpdateLocation);
    			previousLayout.addView(lastTextView);
    			LauncherModel.updateItemInDatabase(getContext(), lastInfo);
				info.cellX = mStartMoveLocation[0];
				info.cellY = mStartMoveLocation[1];
    		}
    	}else {
			info.cellX = mStartMoveLocation[0];
			info.cellY = mStartMoveLocation[1];
			info.screen = Launcher.getScreen();
			if(cellLayout.getVacantCell(firstVacantLoacation, 1, 1, info.iphoneBubbleTextView)){
				cellLayout.getLastCellLocation(mLastVacantLocation, 1);
				if(checkMoveLocation(firstVacantLoacation, mStartMoveLocation) < 0
						&& checkMoveLocation(mLastVacantLocation, firstVacantLoacation) > 0){
					cellLayout.cellMoveLeft(firstVacantLoacation, mStartMoveLocation, info.iphoneBubbleTextView, true, mUpdateLocation);
				}else if(checkMoveLocation(firstVacantLoacation, mStartMoveLocation) > 0){
					cellLayout.cellMoveRight(mStartMoveLocation, firstVacantLoacation, info.iphoneBubbleTextView, true, mUpdateLocation);
				}
			}else{
				firstVacantLoacation[0] = 3;
								//Modify GWLLSW-946 ningyaoyun 20121023(on)
    			firstVacantLoacation[1] = 4;
								//Modify GWLLSW-946 ningyaoyun 20121023(off)
    			IphoneBubbleTextView lastTextView = cellLayout.getLastCell();
    			cellLayout.cellMoveRight(mStartMoveLocation, firstVacantLoacation, null, true, mUpdateLocation);
				info.cellX = mStartMoveLocation[0];
				info.cellY = mStartMoveLocation[1];
    			fullCellLayoutMove(cellLayout,lastTextView);
			}
    	}
    	
    	LauncherModel.updateBatchInDatabase(getContext(), mUpdateLocation);
    }
    
	public void onCompleteTranslate(DragSource source, int x, int y, int xOffset, int yOffset, Object dragInfo) {
    	if(DEBUG) Log.d(LOG_TAG, "onCompleteTranslate");
    	
    	int[] firstVacantLoacation = mFirstVacantLocation;
    	int[] lastLocation = mStartMoveLocation;
		CellLayout cellLayout = (CellLayout) getChildAt(Launcher.getScreen());
		ApplicationInfo info = (ApplicationInfo) dragInfo;
		cellLayout.getVacantCell(firstVacantLoacation, 1, 1, info.iphoneBubbleTextView);
		cellLayout.getLastCellLocation(lastLocation, 1);
		if(checkMoveLocation(firstVacantLoacation, lastLocation) < 0){
			mUpdateLocation.clear();
			cellLayout.cellMoveLeft(firstVacantLoacation, lastLocation, info.iphoneBubbleTextView, true, mUpdateLocation);
			LauncherModel.updateBatchInDatabase(getContext(), mUpdateLocation);
		}
	}
    
    public void completeTranslate(int screen){
    	if(DEBUG) Log.d("ttt", "translateAfterCloseFolder");
    	
    	int[] firstVacantLoacation = mFirstVacantLocation;
    	int[] lastLocation = mStartMoveLocation;
    	CellLayout cellLayout = (CellLayout) getChildAt(screen);
    	cellLayout.getVacantCell(firstVacantLoacation, 1, 1, null);
    	cellLayout.getLastCellLocation(lastLocation, 1);
    	
    	if(checkMoveLocation(firstVacantLoacation, lastLocation) < 0 &&
    			firstVacantLoacation[0] != -1 && firstVacantLoacation[1] != -1){
    		mUpdateLocation.clear();
    		cellLayout.cellMoveLeft(firstVacantLoacation, lastLocation, null, true, mUpdateLocation);
    		LauncherModel.updateBatchInDatabase(getContext(), mUpdateLocation);
    	}
    }
    
    public void onDropCompleted(View target, boolean success, Object dragInfo) {
        // This is a bit expensive but safe
    	if(DEBUG) Log.d(TAG,"Workspace onDropCompleted! success = " + success + "  target= " + target);
        clearVacantCache();
        if (success){
        	
        	View dropView = mDragInfo.iphoneBubbleTextView;
        	ApplicationInfo info = (ApplicationInfo) dragInfo;
        	if(dropView != null){
        		
                if (target != this) {
                	
                	
                	Launcher.getModel().removeDesktopItem(info);
                    final CellLayout cellLayout = (CellLayout) getChildAt(mDragInfo.screen);
                    cellLayout.removeView(dropView);
                }
                
                if(mDragInfo.screen != Launcher.getScreen()){
                	completeTranslate(mDragInfo.screen);
                }
                completeTranslate(Launcher.getScreen());
        	}
        }
        mLastCell[0] = mLastCell[1] = -1;
    }
    
    public void onDragExit(DragSource source, int x, int y, int xOffset, int yOffset,
            Object dragInfo) {
    	if(DEBUG) Log.d(LOG_TAG,"Workspace onDragExit!");
        clearVacantCache();
    }
    
    private void changeToFirstLocation(IphoneBubbleTextView view){
    	ApplicationInfo info = (ApplicationInfo) view.getTag();
		info.cellX = 0;
		info.cellY = 0;
		info.screen += 1;
		CellLayout.LayoutParams params = (CellLayout.LayoutParams) view.getLayoutParams();
		params.cellX = 0;
		params.cellY = 0;
    }
    
    public int checkMoveLocation(int[] start, int[] end) {
		if(end[1] > start[1] || (end[1] == start[1] && end[0] > start[0])){
			return -1;
		}else if(start[0] == end[0] && start[1] == end[1]){
			return 0;
		}
		return 1;
	}
    
    private void fullCellLayoutMove(CellLayout cellLayout, IphoneBubbleTextView lastView) {
    	
    	cellLayout.removeView(lastView);
    	ApplicationInfo info = (ApplicationInfo) lastView.getTag();
    	changeToFirstLocation(lastView);
		
		CellLayout nextCellLayout = (CellLayout) getChildAt(info.screen);
    	
    	IphoneBubbleTextView nextLayoutLastView = null;
						//Modify GWLLSW-946 ningyaoyun 20121023(on)
    	if(nextCellLayout.getVisibaleChild() == 20){
    		nextLayoutLastView = nextCellLayout.getLastCell();
    		mNextCellLayoutStart[0] = 0;
    		mNextCellLayoutStart[1] = 0;
    		mNextCellLayoutEnd[0] = 3;
    		mNextCellLayoutEnd[1] = 4;
    		nextCellLayout.cellMoveRight(mNextCellLayoutStart, mNextCellLayoutEnd, null, true, mUpdateLocation);
    	}else{
    		mNextCellLayoutStart[0] = 0;
    		mNextCellLayoutStart[1] = 0;
    		nextCellLayout.getLastCellLocation(mNextCellLayoutEnd, 0);
    		nextCellLayout.cellMoveRight(mNextCellLayoutStart, mNextCellLayoutEnd, null, true, mUpdateLocation);
    	}
    					//Modify GWLLSW-946 ningyaoyun 20121023(off)
    	nextCellLayout.addView(lastView);
    	LauncherModel.updateItemInDatabase(getContext(), info);
    	if(nextLayoutLastView != null){
    		fullCellLayoutMove(nextCellLayout, nextLayoutLastView);
    	}
	}
    
    private void onDropExternal(DragSource source,int x, int y, Object dragInfo, CellLayout cellLayout) {
        onDropExternal(source,x, y, dragInfo, cellLayout, false);
    }
    
    private void onDropExternal(DragSource source,int x, int y, Object dragInfo, CellLayout cellLayout,
            boolean insertAtFirst) {
        // Drag from somewhere else
    	if(DEBUG) Log.d(LOG_TAG, "onDropExternal");
    	
        ApplicationInfo info = (ApplicationInfo) dragInfo;
        
        switch (info.itemType) {
        case LauncherSettings.Favorites.ITEM_TYPE_USER_FOLDER:
        case LauncherSettings.Favorites.ITEM_TYPE_APPLICATION:
        case LauncherSettings.Favorites.ITEM_TYPE_SHORTCUT:
        	
        	int screenIndex = mScroller.isFinished() ? mCurrentScreen : mNextScreen;

    		if(info instanceof FolderInfo){
        		FolderInfo folderInfo = (FolderInfo)dragInfo;
        		mUpdateLocation.clear();
        		folderInfo.itemsId.clear();
        		for(ApplicationInfo ai : folderInfo.items){
        			ai.screen = screenIndex;
        			mUpdateLocation.add(ai);
        		}
        		
        	 
        		LauncherModel.addItemByBatch(getContext(), mUpdateLocation, LauncherSettings.Favorites.CONTENT_URI);
//        		Launcher.getModel().removePanelItem(folderInfo);
        		
        		for (ApplicationInfo item : mUpdateLocation) {
        			folderInfo.itemsId.add(item.id);
				}
        	}
    		
//    		if(!info.isFolderItem){
//    			Launcher.getModel().removePanelItem(info);
//    		} else {
//    			info.isFolderItem = false;
//    			LauncherModel.deleteItemFromPanelDatabase(getContext(), info);
//    		}
    		
	        View view = Launcher.getInstance().createShortcut(cellLayout, info);
	        cellLayout.addView(view, insertAtFirst ? 0 : -1);
	        view.setOnLongClickListener(mLongClickListener);
	        
	        mTargetCell = estimateDropCell(x, y, 1, 1, view, cellLayout, mTargetCell);
	        cellLayout.onDropChild(view, mTargetCell);
	        CellLayout.LayoutParams lp = (CellLayout.LayoutParams) view.getLayoutParams();
	        
    		LauncherModel.addItemToDatabase(getContext(), info,LauncherSettings.Favorites.CONTAINER_DESKTOP
    				, screenIndex, lp.cellX, lp.cellY,false); 
    		Launcher.getModel().addDesktopItem(info);
	        break;
        default:
            throw new IllegalStateException("Unknown item type: " + info.itemType);
        }
    }
    
    /**
     * Return the current {@link CellLayout}, correctly picking the destination
     * screen while a scroll is in progress.
     */
    public CellLayout getCurrentDropLayout() {
        int index = mScroller.isFinished() ? mCurrentScreen : mNextScreen;
        return (CellLayout) getChildAt(index);
    }

    /**
     * {@inheritDoc}
     */
    public boolean acceptDrop(DragSource source, int x, int y,
            int xOffset, int yOffset, Object dragInfo) {

//    	if (mCurrentScreen == 0){
//    		return false;
//    	}
    	return true;
    }
    
    /**
     * 查找workspace中第一个可以放元素的celllayout
     * @param vacant
     * @return
     */
    public boolean getVacantCell(int[] vacant) {
    	boolean result = false;
        int[] cell = new int[2];
        int count = getChildCount();
        for (int i = 0 ; i < count ; i++) {
       	 	CellLayout child = (CellLayout)getChildAt(i);
       	 	boolean b = child.getVacantCell(cell, 1, 1, null);
       	 	if (b) {
       	 		result = true;
       	 		vacant[0] = i;
       	 		vacant[1] = cell[0];
       	 		vacant[2] = cell[1];
       	 		break;
       	 	}
        }
        return result;
    }

    /**
     * Calculate the nearest cell where the given object would be dropped.
     */
    private int[] estimateDropCell(int pixelX, int pixelY,
            int spanX, int spanY, View ignoreView, CellLayout layout, int[] recycle) {
        // Create vacant cell cache if none exists
        if (mVacantCache == null) {
            mVacantCache = layout.findAllVacantCells(null, ignoreView);
        }

        // Find the best target drop location
        return layout.findNearestVacantArea(pixelX, pixelY, spanX, spanY, mVacantCache, recycle);
    }

    public boolean updateNotificationCount(String className,int count) {
    	if (DEBUG) Log.d(TAG,"updateNotificationCount className = " + className + ", count = " + count);
    	
    	int cellCount = getChildCount();
    	for (int i = 0;i < cellCount; i++) {
    		CellLayout one = (CellLayout)getChildAt(i);
    		int childCount = one.getChildCount();
    		for (int j = 0; j < childCount; j++) {
    			/**
    			 * 2010-10-20
    			 * 修改view的取得方式
    			 */
    			View child = one.getChildAt(j);
    			if (!(child instanceof IphoneBubbleTextView)) {
    				continue;
    			}
    			IphoneBubbleTextView t = (IphoneBubbleTextView)child;
    			ApplicationInfo a  = (ApplicationInfo)t.getTag();
    			if(a instanceof FolderInfo){
    				
    				FolderInfo folderInfo = (FolderInfo)a;
    				int folderLauncherCount = 0;
    				for(ApplicationInfo info : folderInfo.items){
        				if(info.intent.getComponent().getClassName().equals(className)){
        					if(count == 0){
        						folderInfo.launcherCount -= info.launcherCount;
        					}
        					if(folderInfo.launcherCount < 0){
        						folderInfo.launcherCount = 0;
        					}
        					info.launcherCount = count;
    						LauncherModel.updateItemInDatabase(getContext(), info);
    						if(LauncherValues.getInstance().isHasFolderOpen()){
    							//Modify GWLLSW-940 ningyaoyun 20121023(on)
    							if(info.iphoneBubbleTextView != null){
    								info.iphoneBubbleTextView.updateNotificationCount(count);
    							} 
    							//Modify GWLLSW-940 ningyaoyun 20121023(off)
    						}
        				}
        				folderLauncherCount += info.launcherCount;
        			}
    				folderInfo.launcherCount = folderLauncherCount;
    				LauncherModel.updateItemInDatabase(getContext(), folderInfo);
    				t.updateNotificationCount(folderInfo.launcherCount);
    				if(folderLauncherCount > 0){
    					return true;
    				}
    			}else{
        			if (a.intent.getComponent().getClassName().equals(className)) {
        				if (DEBUG) Log.d(TAG,"updateNotificationCount found!");
        				
        				t.updateNotificationCount(count);
            			a.launcherCount = count;
        				LauncherModel.updateItemInDatabase(getContext(), a);
        				return true;
        			}
    			}
    		}
    	}
    	return false;
    }
    
    public void setDragger(DragController dragger) {
        mDragger = dragger;
    }

    public void scrollLeft() {
        clearVacantCache();
        if (mNextScreen == INVALID_SCREEN && mCurrentScreen >= 1 && mScroller.isFinished()) {
            snapToScreen(mCurrentScreen - 1);
        }
    }

    public void scrollRight() {
        clearVacantCache();

        if (mNextScreen == INVALID_SCREEN && mCurrentScreen < getChildCount() -1 &&
                mScroller.isFinished()) {
            snapToScreen(mCurrentScreen + 1);
        }
    }

    public int getScreenForView(View v) {
        int result = -1;
        if (v != null) {
            ViewParent vp = v.getParent();
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                if (vp == getChildAt(i)) {
                    return i;
                }
            }
        }
        return result;
    }

    public View getViewForTag(Object tag) {
        int screenCount = getChildCount();
        for (int screen = 0; screen < screenCount; screen++) {
            CellLayout currentScreen = ((CellLayout) getChildAt(screen));
            int count = currentScreen.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = currentScreen.getChildAt(i);
                if (child.getTag() == tag) {
                    return child;
                }
            }
        }
        return null;
    }

    /**
     * Unlocks the SlidingDrawer so that touch events are processed.
     *
     * @see #lock()
     */
    public void unlock() {
        mLocked = false;
    }

    /**
     * Locks the SlidingDrawer so that touch events are ignores.
     *
     * @see #unlock()
     */
    public void lock() {
        mLocked = true;
    }
    
    /**
     * @return True is long presses are still allowed for the current touch
     */
    public boolean allowLongPress() {
        return mAllowLongPress;
    }
    
    /**
     * Set true to allow long-press events to be triggered, usually checked by
     * {@link Launcher} to accept or block dpad-initiated long-presses.
     */
    public void setAllowLongPress(boolean allowLongPress) {
        mAllowLongPress = allowLongPress;
    }

    void removeShortcutsForPackage(String packageName) {
    	
        final ArrayList<View> childrenToRemove = new ArrayList<View>();
        List<ApplicationInfo> deleteItemList = new ArrayList<ApplicationInfo>();
        final LauncherModel model = Launcher.getModel();
        final int count = getChildCount();
        int[] lastLocation = new int[2];
        int[] firstLocation = new int[2];

        for (int i = 0; i < count; i++) {
        	
            final CellLayout layout = (CellLayout) getChildAt(i);
            int childCount = layout.getChildCount();
            childrenToRemove.clear();

            for (int j = 0; j < childCount; j++) {
                final View view = layout.getChildAt(j);
                ApplicationInfo info = null;
                if(view != null && view instanceof IphoneBubbleTextView && view.getTag() instanceof ApplicationInfo){
                	info = (ApplicationInfo) view.getTag();
                }else{
                	continue;
                }
                
                if (!info.isFolder) {
                    final Intent intent = info.intent;
                    final ComponentName name = intent.getComponent();

                    if (Intent.ACTION_MAIN.equals(intent.getAction()) && name != null 
                    		&& packageName.equals(name.getPackageName())) {
                    	
                        LauncherModel.deleteItemFromDatabase(getContext(), info);
                        childrenToRemove.add(view);
                        model.removeDesktopItem(info);
                        model.removeAllItem(info);
                        
                        firstLocation[0] = info.cellX;
                        firstLocation[1] = info.cellY;
                    	layout.getLastCellLocation(lastLocation, 0);
                    	mUpdateLocation.clear();
                    	layout.cellMoveLeft(firstLocation, lastLocation, info.iphoneBubbleTextView, true, mUpdateLocation);
                    	LauncherModel.updateBatchInDatabase(getContext(), mUpdateLocation);
                    }
                }else {
                	
                	deleteItemList.clear();
                	FolderInfo folderInfo = (FolderInfo) info;
                	for(ApplicationInfo appInfo : folderInfo.items){
                		final Intent intent = appInfo.intent;
                        final ComponentName name = intent.getComponent();
                        if (Intent.ACTION_MAIN.equals(intent.getAction()) && name != null && packageName.equals(name.getPackageName())) {
                        	deleteItemList.add(appInfo);
                        }
                	}
                	
                	for(ApplicationInfo delItem : deleteItemList){
                		folderInfo.items.remove(delItem);
                		folderInfo.itemsId.remove(delItem.id);
                		LauncherModel.deleteItemFromDatabase(getContext(), delItem);
                		model.removeAllItem(delItem);
                	}
                	
                	if(folderInfo.items.size() > 0 ){
                		
            			folderInfo.icon = Utilities.createFolderIcon(getContext(), folderInfo.items, Utilities.GRAY_BG);
                    	folderInfo.grayIcon = Utilities.convertGrayImg(folderInfo.icon);
                		TextView textView = folderInfo.iphoneBubbleTextView.getTextTtile();
                		textView.setCompoundDrawablesWithIntrinsicBounds(null,folderInfo.icon, null, null);
                		LauncherModel.updateItemInDatabase(getContext(), folderInfo);
            		}else if(folderInfo.items.size() == 0 ){
            			
            			childrenToRemove.add(view);
            			LauncherModel.deleteItemFromDatabase(getContext(), folderInfo);
            			model.removeDesktopItem(info);
            			
                        firstLocation[0] = info.cellX;
                        firstLocation[1] = info.cellY;
                    	layout.getLastCellLocation(lastLocation, 0);
                    	mUpdateLocation.clear();
                    	layout.cellMoveLeft(firstLocation, lastLocation, info.iphoneBubbleTextView, true, mUpdateLocation);
                    	LauncherModel.updateBatchInDatabase(getContext(), mUpdateLocation);
            		}
                } 
            }

            childCount = childrenToRemove.size();
            for (int j = 0; j < childCount; j++) {
                layout.removeViewInLayout(childrenToRemove.get(j));
                ((DragLayer)mDragger).endDrag();
            }

            if (childCount > 0) {
                layout.requestLayout();
                layout.invalidate();
            }
        }
    }

    void updateShortcutsForPackage(String packageName) {
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final CellLayout layout = (CellLayout) getChildAt(i);
            int childCount = layout.getChildCount();
            for (int j = 0; j < childCount; j++) {
                final ViewGroup view = (ViewGroup)layout.getChildAt(j);
                Object tag = view.getTag();
                if(!(tag instanceof FolderInfo)){
                	if (tag instanceof ApplicationInfo) {
                		ApplicationInfo info = (ApplicationInfo) tag;
                    	// We need to check for ACTION_MAIN otherwise getComponent() might
                        // return null for some shortcuts (for instance, for shortcuts to
                        // web pages.)
                        final Intent intent = info.intent;
                        final ComponentName name = intent.getComponent();
                        if (info.itemType == LauncherSettings.Favorites.ITEM_TYPE_APPLICATION &&
                                Intent.ACTION_MAIN.equals(intent.getAction()) && name != null &&
                                packageName.equals(name.getPackageName())) {
                        	
                            Drawable icon = Utilities.getCustomerIcon(getContext(), intent);
                            if(icon == null){
                            	icon = Launcher.getModel().getApplicationInfoIcon(
                                        LauncherValues.getInstance().getLauncher().getPackageManager(), info);
                            }
                            
                            if (icon != null && icon != info.icon) {
                                info.icon.setCallback(null);
                                info.icon = Utilities.createIconThumbnail(icon, this.getContext());
                                
                              
                            	if (info.isUninstall) {
                            		info.icon = Utilities.mergeDrawble(getContext(), info.icon);
                            	}
                                info.grayIcon = Utilities.convertGrayImg(info.icon);
                                info.smallIcon = Utilities.createSmallIcon(info.icon, mContext);
                                
                                // prq add begin, for calendar and thirdPart application icon need deal with
                                
                                info.filtered = true;
                                TextView textView = (TextView) ((ViewGroup)view.getChildAt(0)).getChildAt(0);
                                textView.setCompoundDrawablesWithIntrinsicBounds(null,info.icon, null, null);
                                LauncherModel.updateItemInDatabase(LauncherValues.getInstance().getLauncher(), info);
                            }
                        }
                    }
                }
            }
        }
    }

    void moveToDefaultScreen() {
        snapToScreen(mDefaultScreen);
        getChildAt(mDefaultScreen).requestFocus();
    }

    public static class SavedState extends BaseSavedState {
        int currentScreen = -1;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            currentScreen = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(currentScreen);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    public void setNextScreen(int nextScreen) {
		mNextScreen = nextScreen;
	}
    
    public ApplicationInfo getDropCellInfo(){
    	return mDragInfo;
    }
    private int mPageSpacing;
    public void setPageSpacing(int pageSpacing) {
        mPageSpacing = pageSpacing;
    }
    void reSnapToCureentPage(){
		Log.d(LOG_TAG,"Workspace.reSnapToCureentPage()--mCurrentScreen="+mCurrentScreen);
		final View firstChild = getChildAt(0);
        if (firstChild != null && getScrollX() % ( firstChild.getMeasuredWidth() + mPageSpacing * 2 ) != 0) {
			Log.d("zsc","Workspace.reSnapToCureentPage()--snapToDestination()-getScrollX()="+getScrollX());
        	snapToDestination();
        }
	}
}
