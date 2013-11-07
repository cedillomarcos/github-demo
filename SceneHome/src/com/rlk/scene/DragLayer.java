package com.rlk.scene;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rlk.scene.R;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

/**
 * 拖放控制
 * A ViewGroup that coordinated dragging across its dscendants
 */
public class DragLayer extends FrameLayout implements DragController {
	private static final boolean DEBUG = true;
	private static final String TAG="IphoneDragLayer"; 
	
    private static final int SCROLL_DELAY = 200;	//当图标移到左边或者右边时，延时多长时间来换屏
    private static final int SCROLL_ZONE = LauncherValues.mScrollZone; 
     
    private static final int ANIMATION_SCALE_UP_DURATION = 110;

    private static final boolean PROFILE_DRAWING_DURING_DRAG = false;
    private static final float DRAG_SCALE = 8.0f;	//tony 修改
    
	private boolean mDragging = false;
    private boolean mShouldDrop;
    private float mLastMotionX;
    private float mLastMotionY;
    private Rect mRectTemp;

    /**
     * The bitmap that is currently being dragged
     */
    private Bitmap mDragBitmap = null;
    private View mOriginator;

    private int mBitmapOffsetX;
    private int mBitmapOffsetY;

    /**
     * X offset from where we touched on the cell to its upper-left corner
     */
    private float mTouchOffsetX;

    /**
     * Y offset from where we touched on the cell to its upper-left corner
     */
    private float mTouchOffsetY;

    /**
     * Utility rectangle
     */
    private Rect mDragRect = new Rect();

    /**
     * Where the drag originated
     */
    private DragSource mDragSource;

    /**
     * The data associated with the object being dragged
     */
    private Object mDragInfo;

    private final Rect mRect = new Rect();
    private final int[] mDropCoordinates = new int[2];

    private DragListener mListener;

    private DragScroller mDragScroller;

    private static final int SCROLL_OUTSIDE_ZONE = 0;
    private static final int SCROLL_WAITING_IN_ZONE = 1;

    private static final int SCROLL_LEFT = 0;
    private static final int SCROLL_RIGHT = 1;

    private int mScrollState = SCROLL_OUTSIDE_ZONE;

    private ScrollRunnable mScrollRunnable = new ScrollRunnable();
    private View mIgnoredDropTarget;

    private RectF mDragRegion;
    private boolean mEnteredRegion;
    private DropTarget mLastDropTarget;

    private final Paint mTrashPaint = new Paint();
    private Paint mDragPaint;

    private static final int ANIMATION_STATE_STARTING = 1;
    private static final int ANIMATION_STATE_RUNNING = 2;
    private static final int ANIMATION_STATE_DONE = 3;

    private static final int ANIMATION_TYPE_SCALE = 1;

    private float mAnimationFrom;
    private float mAnimationTo;
    private int mAnimationDuration;
    private long mAnimationStartTime;
    private int mAnimationType;
    private int mAnimationState = ANIMATION_STATE_DONE;
    private int mMaximunVelocity;
    
    private InputMethodManager mInputMethodManager;
    private Workspace workspace;
//    private TransparentPanel transparentPanel;
    
    private VelocityTracker mVelocityTracker;
    private int mMinimunVelocity = 100;
    
    private boolean mPutInDirection;
    private Drawable mFolderBgBlack;
    private Drawable mFolderBgGray;

    /**
     * Used to create a new DragLayer from XML.
     *
     * @param context The application's context.
     * @param attrs The attribtues set containing the Workspace's customization values.
     */
    public DragLayer(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Make estimated paint area in gray
        Resources res = context.getResources();
        final int srcColor = res.getColor(R.color.delete_color_filter);
        mTrashPaint.setColorFilter(new PorterDuffColorFilter(srcColor, PorterDuff.Mode.SRC_ATOP));
        
        int snagColor = res.getColor(R.color.snag_callout_color);
        Paint estimatedPaint = new Paint();
        estimatedPaint.setColor(snagColor);
        estimatedPaint.setStrokeWidth(3);
        estimatedPaint.setAntiAlias(true);

        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mMaximunVelocity = configuration.getScaledMaximumFlingVelocity();
        mRectTemp = new Rect();
    }
    
     @Override
	protected void onFinishInflate() {
		super.onFinishInflate();
        workspace = (Workspace)findViewById(R.id.workspace);
//        transparentPanel = (TransparentPanel) findViewById(R.id.transparent_panel);
	}

	/**
      * tony 2010-03-26 所有的拖放从这里开始！
      * v是CellInfo.cell,类型是IphoneBubbleTextView, source是WorkSpace,dragInfo是CellInfo.cell.getTag,dragAction是动作。
      */
    public void startDrag(View v, DragSource source, Object dragInfo, int dragAction) {
    	
    	LauncherValues.getInstance().setAnim(true, this);

    	if(dragInfo instanceof FolderInfo){
    		LauncherValues.IGNORE_IPHONE_BUBBLE_TEXTVIEW = true;
    	}
    	
    	if(source instanceof FolderLinearLayout){
    		LauncherValues.IGNORE_IPHONE_BUBBLE_TEXTVIEW = true;
    		LauncherValues.dragIconStatu = LauncherValues.DRAG_ON_EXPEND_FOLDER;
    	}
    	
    	mDragView = v;
    	setIgnoredDropTarget(mDragView);
    	mDragViewInfo = (ApplicationInfo) ((IphoneBubbleTextView)v).getTag();
    	mDragInBitmap = null;
    	
        if (PROFILE_DRAWING_DURING_DRAG) {
            android.os.Debug.startMethodTracing("Launcher");
        }

        // Hide soft keyboard, if visible
        if (mInputMethodManager == null) {
            mInputMethodManager = (InputMethodManager)
                getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        mInputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);

        Rect r = mDragRect;
        if (DEBUG)
        	Log.d(TAG,"startDrag v.getScrollX() = " + v.getScrollX() + ",v.getScrollY() = " + v.getScrollY());
        r.set(v.getScrollX(), v.getScrollY(), 0, 0);
        if (DEBUG)
        	Log.d(TAG,"startDrag left = " + r.left + ",top = " + r.top + ",right = " + r.right + ",bottom = " + r.bottom);
        
        offsetDescendantRectToMyCoords(v, r);
        if (DEBUG) { 
        	Log.d(TAG,"startDrag left = " + v.getLeft() + ",top = " + v.getTop() + ",right = " + v.getRight() + ",bottom = " + v.getBottom());
        	Log.d(TAG,"startDrag left = " + r.left + ",top = " + r.top + ",right = " + r.right + ",bottom = " + r.bottom);
        }
        
        mTouchOffsetX = mLastMotionX - r.left;
        mTouchOffsetY = mLastMotionY - r.top; //mTouchOffsetX是指开始拖动时，触摸点离图标左边顶点的偏移量
        if (DEBUG) { 
        	Log.d(TAG,"mTouchOffsetX = " + mTouchOffsetX);
        	Log.d(TAG,"mTouchOffsetY = " + mTouchOffsetY);
        }
        
        v.clearFocus();
        v.setPressed(false);
        boolean willNotCache = v.willNotCacheDrawing();
        v.setWillNotCacheDrawing(false);

        // Reset the drawing cache background color to fully transparent
        // for the duration of this operation
        int color = v.getDrawingCacheBackgroundColor();
        v.setDrawingCacheBackgroundColor(0);

        if (color != 0) {
            v.destroyDrawingCache();
        }
        v.buildDrawingCache();
        Bitmap viewBitmap = v.getDrawingCache();
        	
        int width = viewBitmap.getWidth();
        int height = viewBitmap.getHeight();
        if (DEBUG)
        	Log.d(TAG,"startDrag width = " + width + ",height = " + height );
        Matrix scale = new Matrix();
        float scaleFactor = v.getWidth();
        scaleFactor = (scaleFactor + DRAG_SCALE) /scaleFactor;
        if (DEBUG)
        	Log.d(TAG,"startDrag scaleFactor = " + scaleFactor);
        scale.setScale(scaleFactor, scaleFactor);

        mAnimationTo = 1.0f;
        mAnimationFrom = 1.0f / scaleFactor;
        mAnimationDuration = ANIMATION_SCALE_UP_DURATION;
        mAnimationState = ANIMATION_STATE_STARTING;
        mAnimationType = ANIMATION_TYPE_SCALE;

        mDragBitmap = Bitmap.createBitmap(viewBitmap, 0, 0, width, height, scale, true);//建 立拖放图标，这个拖放图标是放大了的图标 width=width * scale(width),height = height * scale( height)

        v.destroyDrawingCache();
        v.setWillNotCacheDrawing(willNotCache);
        v.setDrawingCacheBackgroundColor(color);

        final Bitmap dragBitmap = mDragBitmap;
        mBitmapOffsetX = (dragBitmap.getWidth() - width) / 2;
        mBitmapOffsetY = (dragBitmap.getHeight() - height) / 2; 	//计算大图与小图之间的偏移量地址

        if (dragAction == DRAG_ACTION_MOVE) {						//只显示大图，不显示小图了
            v.setVisibility(GONE);
        }

        mDragPaint = null;
        mDragging = true;
        mShouldDrop = true;
        mOriginator = v;
        mDragSource = source;
        mDragInfo = dragInfo;
		
        v.getLocationOnScreen(mDropViewLocation);
        final int[] coordinates = mDropCoordinates;
        dropTarget = null;
        mLastDropTarget = null;

        workspace.setupWorkspaceDropTargets();
//        transparentPanel.setupPanelDropTargets();

        invalidate();
    }
    
    private int[] mDropViewLocation = new int[2];

    @Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
    	if (DEBUG)
    		Log.d(TAG,"onKeyUp!");
		return super.onKeyUp(keyCode, event);
	}
	@Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return mDragging || super.dispatchKeyEvent(event);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
//    	if(DEBUG) Log.d(TAG,"DragLayer dispatchDraw!");
    	
        if (mDragging && mDragBitmap != null) {
            if (mAnimationState == ANIMATION_STATE_STARTING) {
                mAnimationStartTime = SystemClock.uptimeMillis();
                mAnimationState = ANIMATION_STATE_RUNNING;
            }
            if (DEBUG)
            	Log.d(TAG,"mAnimationState = " + mAnimationState);
            if (mAnimationState == ANIMATION_STATE_RUNNING) {
                float normalized = (float) (SystemClock.uptimeMillis() - mAnimationStartTime) /
                        mAnimationDuration;
                if (normalized >= 1.0f) {  //计算scale的时间。110ms
                    mAnimationState = ANIMATION_STATE_DONE;
                }
                normalized = Math.min(normalized, 1.0f);
                final float value = mAnimationFrom  + (mAnimationTo - mAnimationFrom) * normalized;
                if (DEBUG)
                	Log.d(TAG,"dispatchDraw value = " + value);
                switch (mAnimationType) {
                    case ANIMATION_TYPE_SCALE:
                    	
                        final Bitmap dragBitmap = mDragBitmap;
                        
                        canvas.save();
                        
                        canvas.translate(this.getScrollX() + mLastMotionX - mTouchOffsetX - mBitmapOffsetX,
                                this.getScrollY() + mLastMotionY - mTouchOffsetY - mBitmapOffsetY);
//                        canvas.translate((dragBitmap.getWidth() * (1.0f - value)) / 2,
//                                (dragBitmap.getHeight() * (1.0f - value)) / 2);

//                        canvas.scale(value, value);
                       	canvas.scale(1.0f, 1.0f);
                        if (mDragPaint == null)
                        	mDragPaint = new Paint();
                        mDragPaint.setAlpha(this.getResources().getInteger(R.integer.icon_alpha));
                        canvas.drawBitmap(dragBitmap, 0.0f, 0.0f, mDragPaint);
                                       	
                        canvas.restore();
                        
                        break;
                }
            } else {
                if(LauncherSettings.Favorites.ON_FOLDER_TARGET){
                	if(mDragInBitmap == null){
                		ApplicationInfo ap = (ApplicationInfo)mDragView.getTag();
                		mDragInBitmap = Utilities.drawableToBitmap(ap.icon, LauncherValues.mIconWdith, LauncherValues.mIconHeiht);
                	}
                	if(mDragInBitmap != null){
                		canvas.drawBitmap(mDragInBitmap,
                                this.getScrollX() + mLastMotionX - mTouchOffsetX + mBitmapOffsetX,
                                this.getScrollY() + mLastMotionY - mTouchOffsetY + mBitmapOffsetY, mDragPaint);
                	}
                }else{
                    canvas.drawBitmap(mDragBitmap,
                            this.getScrollX() + mLastMotionX - mTouchOffsetX - mBitmapOffsetX,
                            this.getScrollY() + mLastMotionY - mTouchOffsetY - mBitmapOffsetY, mDragPaint); //在相对于bitmap偏移量位置画图
                }
            }
        }
    }
    
    private View mDragView;
    private Bitmap mDragInBitmap;
    public void endDrag() {
  	    if(DEBUG) Log.d(TAG,"endDrag!");
        if (mDragging) {
        	LauncherValues.IGNORE_IPHONE_BUBBLE_TEXTVIEW = false;
            mDragging = false;
            isShowEnough = false;
            mDragViewInfo = null;
            if (mDragBitmap != null) {  //释放拖放图标
                mDragBitmap.recycle();
            }
            if (mOriginator != null) {//显示原来的图片
                mOriginator.setVisibility(VISIBLE);
            }
            if(mDragInBitmap != null){
            	mDragInBitmap.recycle();
            }
        }
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
 	    if(DEBUG) Log.d(TAG,"DragLayer onInterceptTouchEvent!");
        final int action = ev.getAction();

        final float x = ev.getX();
        final float y = ev.getY();

        switch (action) {
            case MotionEvent.ACTION_MOVE:
            	if(DEBUG) Log.d(TAG,"DragLayer onInterceptTouchEvent ACTION_MOVE");
                break;
            case MotionEvent.ACTION_DOWN:
            	if (DEBUG) Log.d(TAG,"DragLayer onInterceptTouchEvent ACTION_DOWN");
//                 Remember location of down touch
                mLastMotionX = x;
                mLastMotionY = y;
                mLastDropTarget = null;
                mEnteredRegion = false;
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
            	if (DEBUG) Log.d(TAG,"DragLayer onInterceptTouchEvent ACTION_UP");
            	if(LauncherValues.getInstance().isHasFolderOpen()){
            		
            		CellLayout currentCellLayout = Launcher.getInstance().getCurrentCellLayout();
            		ExpendFolder expendFolderView = Launcher.getInstance().getExpendFolderView();
            		IphoneBubbleTextView folderTarget = Launcher.getInstance().getFolderTarget();
            		
            		if (null != expendFolderView) {
            		try {
            		CellLayout.LayoutParams lp = (CellLayout.LayoutParams) expendFolderView.getLayoutParams();
            		int currowHeight = expendFolderView.getCurrowHeight();
            		
						if(y < lp.y + currowHeight || y > lp.y + lp.height){
            			
            			FolderLinearLayout folderLinearLayout = expendFolderView.getBody();
            			folderLinearLayout.hideTitleEditText(); 
        				Launcher.getInstance().closeFolder(currentCellLayout, folderTarget);
        					Launcher.getInstance().getWorkspace().completeTranslate(Launcher.getScreen());
						}
					} catch (Exception e) {
						Log.e("zsc", "DragLayer onInterceptTouchEvent ACTION_UP--Exception!!!");
						e.printStackTrace();
					}
//        				}
            		}
            	}
            	
        		if (mShouldDrop && drop(x, y)) {
                    mShouldDrop = false;
                }
                endDrag();
			try {
				Workspace workspace = (Workspace) getChildAt(0);
                int childCount = workspace.getChildCount();
                for (int i = 0; i < childCount; i++) {
                	View cellLayout = workspace.getChildAt(i);
                	if (null != cellLayout) {
                		cellLayout.invalidate();
					}
				}
			} catch (Exception e) {
				Log.e("zsc", "DragLayer.onInterceptTouchEvent()--ACTION_UP" +
						"--cellLayout invalidate Excepiton!!");
				e.printStackTrace();
			}
			try {
				LinearLayout layout = (LinearLayout) getChildAt(1);
                View panel = layout.getChildAt(1);
                if (null != panel) {
					panel.invalidate();
				}
			} catch (Exception e) {
				Log.e("zsc", "DragLayer.onInterceptTouchEvent()--ACTION_UP" +
						"--panel invalidate Excepiton!!");
				e.printStackTrace();
			}
                break;
        }
//        if (DEBUG) Log.d(TAG,"onInterceptTouchEvent mDragging = " + mDragging);
        return mDragging;
    }


	ApplicationInfo mDragViewInfo = null;
	DropTarget dropTarget;
	boolean isShowEnough = false;
	
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
//	     if(DEBUG) Log.d(TAG,"onTouchEvent!");
        if (!mDragging) {
            return false;
        }
        
        if(mVelocityTracker == null){
        	mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(ev);
        final int action = ev.getAction();
        final float x = ev.getX();
        final float y = ev.getY();
      if (DEBUG) Log.d(TAG,"x = " + x + ",x = " + y);
        
        synchronized (obj) {
        	mX = x;
        	mY = y;
		}
        switch (action) {
        case MotionEvent.ACTION_DOWN:
            // Remember where the motion event started
            mLastMotionX = x;
            mLastMotionY = y;
            if ((x < SCROLL_ZONE) || (x > getWidth() - SCROLL_ZONE)) {
            	if (DEBUG) Log.d(TAG," postDelayed!");
                mScrollState = SCROLL_WAITING_IN_ZONE;
                postDelayed(mScrollRunnable, SCROLL_DELAY);
            } else {
                mScrollState = SCROLL_OUTSIDE_ZONE;
            }
            break;
        case MotionEvent.ACTION_MOVE:
//        	Log.d(TAG,"onTouchEvent ACTION_MOVE x " + ev.getX() + " y "+ ev.getY() + " time " + ev.getEventTime());
            final int scrollX = this.getScrollX();
            final int scrollY = this.getScrollY();

            final float touchX = mTouchOffsetX;
            final float touchY = mTouchOffsetY;//mTouchOffsetX是指开始拖动时，触摸点离图标左边顶点的偏移量

            final int offsetX = mBitmapOffsetX;//计算大图与小图之间的偏移量地址
            final int offsetY = mBitmapOffsetY;

            int left = (int) (scrollX + mLastMotionX - touchX - offsetX);
            int top = (int) (scrollY + mLastMotionY - touchY - offsetY);

            final Bitmap dragBitmap = mDragBitmap;
            final int width = dragBitmap.getWidth();
            final int height = dragBitmap.getHeight();

            final Rect rect = mRect;
            rect.set(left - 1, top - 1, left + width + 1, top + height + 1);

            left = (int) (scrollX + x - touchX - offsetX);
            top = (int) (scrollY + y - touchY - offsetY);

            // Invalidate current icon position
            rect.union(left - 1, top - 1, left + width + 1, top + height + 1);//两个矩形并集

            final int[] coordinates = mDropCoordinates;
            isShowEnough = slowEnough(); 
            if (DEBUG) Log.d(TAG,"isShowEnough=" + isShowEnough + "  mLastDropTarget == null  " + (mLastDropTarget == null));
            
            if (isShowEnough || mLastDropTarget == null) {
            	dropTarget = findDropTarget((int) x, (int) y, coordinates);
            } else {
            	dropTarget = mLastDropTarget;
            }

            if (DEBUG) Log.d(TAG,"onTouchEvent findDropTarget = " + dropTarget);
            
            if(/*dropTarget instanceof TransparentPanel && */mLastDropTarget instanceof Workspace){
            	mLastDropTarget.onCompleteTranslate(mDragSource, coordinates[0], coordinates[1],
                                (int) mTouchOffsetX, (int) mTouchOffsetY, mDragInfo);
            }
            			
            if (dropTarget != null) {
                if (mLastDropTarget == dropTarget) {
                	if(isShowEnough){
                		dropTarget.onDragOver(mDragSource, coordinates[0], coordinates[1],
                                (int) mTouchOffsetX, (int) mTouchOffsetY, mDragInfo, false);
                	}
                } else {
                    if (mLastDropTarget != null) {
                        mLastDropTarget.onDragExit(mDragSource, coordinates[0], coordinates[1],
                            (int) mTouchOffsetX, (int) mTouchOffsetY, mDragInfo);
                    }
                    dropTarget.onDragEnter(mDragSource, coordinates[0], coordinates[1],
                        (int) mTouchOffsetX, (int) mTouchOffsetY, mDragInfo);
                }
            } else {
                if (mLastDropTarget != null) {
                    mLastDropTarget.onDragExit(mDragSource, coordinates[0], coordinates[1],
                        (int) mTouchOffsetX, (int) mTouchOffsetY, mDragInfo);
                }
            }
            
            mLastMotionX = x;
            mLastMotionY = y;
            
            invalidate(rect);    
            mLastDropTarget = dropTarget;
            
            //回收站
            boolean inDragRegion = false;
            if (mDragRegion != null) {
                final RectF region = mDragRegion;
                final boolean inRegion = region.contains(ev.getRawX(), ev.getRawY());
                if (!mEnteredRegion && inRegion) {
                    mDragPaint = mTrashPaint;
                    mEnteredRegion = true;
                    inDragRegion = true;
                } else if (mEnteredRegion && !inRegion) {
                    mDragPaint = null;
                    mEnteredRegion = false;
                }
            }
            
            //如果当前拖放是属于TransparentPanel，那么不需要换屏
            if (/*mDragSource instanceof TransparentPanel &&*/ y > LauncherValues.mLowestCellLocation) {	
            	break;
            }

            //下面的是换屏代码 在有文件夹展开的时候不需要换屏
            if(!LauncherValues.getInstance().isHasFolderOpen()){
            	if (!inDragRegion && x < SCROLL_ZONE ) {
                    if (mScrollState == SCROLL_OUTSIDE_ZONE) {
                        mScrollState = SCROLL_WAITING_IN_ZONE;
                        mScrollRunnable.setDirection(SCROLL_LEFT);
                        postDelayed(mScrollRunnable, SCROLL_DELAY);
                    }
                } else if (!inDragRegion && x > getWidth() - SCROLL_ZONE) {
                    if (mScrollState == SCROLL_OUTSIDE_ZONE) {
                        mScrollState = SCROLL_WAITING_IN_ZONE;
                        mScrollRunnable.setDirection(SCROLL_RIGHT);
                        postDelayed(mScrollRunnable, SCROLL_DELAY);
                    }
                } else {
                    if (mScrollState == SCROLL_WAITING_IN_ZONE) {
                        mScrollState = SCROLL_OUTSIDE_ZONE;
                        mScrollRunnable.setDirection(SCROLL_RIGHT);
                        removeCallbacks(mScrollRunnable);
                    }
                }
            }
            break;
        case MotionEvent.ACTION_UP:
        	if (DEBUG) Log.d(TAG,"onTouchEvent MotionEvent.ACTION_UP");
        	if(mVelocityTracker != null){
        		mVelocityTracker.recycle();
        		mVelocityTracker = null;
        	}
            removeCallbacks(mScrollRunnable);
            if (mShouldDrop) {
                drop(x, y);
                mShouldDrop = false;
            }
            endDrag();

            break;
        case MotionEvent.ACTION_CANCEL:
        	try {
				if (mDragging) {
					if(mVelocityTracker != null){
						mVelocityTracker.recycle();
						mVelocityTracker = null;
					}
				    removeCallbacks(mScrollRunnable);
				    if (mShouldDrop) {
				        drop(x, y);
				        mShouldDrop = false;
				    }
				}
			} catch (Exception e) {
				Log.d("zsc", "DragLayer.onTouchEvent()--ACTION_CANCEL--dragging:Exception!!!");
				e.printStackTrace();
			}
            endDrag();
        }

        return true;
    }
    
    public void setDragging(boolean value){
    	mDragging = value;
    }
    
    public boolean isDragging(){
    	return mDragging;
    }
    
    public void setShouldDrop(boolean value){
    	mShouldDrop = value;
    }
    
	private boolean slowEnough() {
		mVelocityTracker.computeCurrentVelocity(1000, mMaximunVelocity);
		int XVelocity = (int)Math.abs(mVelocityTracker.getXVelocity());
		int YVelocity = (int)Math.abs(mVelocityTracker.getYVelocity());
		if(DEBUG) Log.d("Velocity", "XVelocity = " + XVelocity);
		if(DEBUG) Log.d("Velocity", "YVelocity = " + YVelocity);
		if(DEBUG) Log.d("Velocity", "XVelocity < mMinimunVelocity && YVelocity < mMinimunVelocity = " 
				+ (XVelocity < mMinimunVelocity && YVelocity < mMinimunVelocity));
		if(XVelocity < mMinimunVelocity && YVelocity < mMinimunVelocity){
			return true;
		}
		return false;
	}

	private boolean drop(float x, float y) {
        invalidate();

        final int[] coordinates = mDropCoordinates;
        DropTarget dropTarget = findDropTarget((int) x, (int) y, coordinates);
        ApplicationInfo info = (ApplicationInfo) mDragInfo;
        
        if(DEBUG) 
        	Log.d(TAG, "on drop dropTarget = " + dropTarget + "  isShowEnough= " + isShowEnough);
        if(DEBUG) 
        	Log.d(TAG, "on drop mDragSource = " + mDragSource);
        
        if(mDragSource instanceof FolderLinearLayout
        		&& LauncherValues.getInstance().isHasFolderOpen() && info.isFolderItem){
        	IphoneBubbleTextView bubbleTextView = Launcher.getInstance().getFolderTarget();
        	FolderInfo folderInfo = (FolderInfo) bubbleTextView.getTag();
        	if(folderInfo.items.contains(info)){
        		info.iphoneBubbleTextView.setVisibility(View.VISIBLE);	
        		return true;
        	}
        }
        
        if (dropTarget != null) {
        	
         	if(!(dropTarget instanceof FolderLinearLayout)){
        		dropTarget.onDragExit(mDragSource, coordinates[0], coordinates[1],
                        (int) mTouchOffsetX, (int) mTouchOffsetY, mDragInfo);
        	}
        	
            if (dropTarget.acceptDrop(mDragSource, coordinates[0], coordinates[1],
                    (int) mTouchOffsetX, (int) mTouchOffsetY, mDragInfo)) {
            	dropTarget.onDragOver(mDragSource, coordinates[0], coordinates[1], 
            			(int) mTouchOffsetX, (int) mTouchOffsetY, mDragInfo, true); 
                dropTarget.onDrop(mDragSource, coordinates[0], coordinates[1],
                        (int) mTouchOffsetX, (int) mTouchOffsetY, mDragInfo);
                mDragSource.onDropCompleted((View) dropTarget, true, mDragInfo);
                return true;
            } else {
                mDragSource.onDropCompleted((View) dropTarget, false, mDragInfo);
                return true;
            }
        }
        return false;
    }
	
    DropTarget findDropTarget(int x, int y, int[] dropCoordinates) { 
        Rect r = mRectTemp;
        List<DropTarget> dropTargets = null;
         
        if(LauncherValues.getInstance().isHasFolderOpen()){
        	ExpendFolder expendFolder = Launcher.getInstance().getExpendFolderView();
        	FolderLinearLayout folderLinearLayout = expendFolder.getBody();
        	if(folderLinearLayout != null){
        		dropTargets = folderLinearLayout.getDropTargets();
        	}
        } else {
            	
            	dropTargets = workspace.getDropTargets();
            	Log.d("ningyaoyun", "findDropTarget dropTargets.size()=" 
            			+ dropTargets.size());
        }
        
    	if(dropTargets != null && dropTargets.size() > 0){
    		
    		int count = dropTargets.size();
    		for (int i = 0; i < count; i++) {
				DropTarget target = dropTargets.get(i);
				if(target.getVisibility() == View.VISIBLE && target != mIgnoredDropTarget){
					target.getHitRect(r);
					target.getLocationOnScreen(dropCoordinates);
					r.offset(dropCoordinates[0] - target.getLeft(), dropCoordinates[1] - target.getTop());
					if(target instanceof IphoneBubbleTextView){
	                	int left = r.left + r.width()/4;
	                	int top = r.top + r.height()/5;
	                	int right = left + 2*r.width()/3;
	                	int bottom = top + 2*r.height()/4;
	                 	r.set(left, top, right, bottom);
	                }
					if(r.contains(x, y)){
						if (target.acceptDrop(mDragSource, x, y, 0, 0, mDragInfo)) {
							dropCoordinates[0] = x;
			                dropCoordinates[1] = y;
			                Log.d("ningyaoyun", "findDropTarget target=" + target);
			                return target;
						}
					}	
				}
			}
    	}
    	Log.d("ningyaoyun", "findDropTarget target=" + null);
    	return null;
    }
    
    public DropTarget findDropTarget(ViewGroup container, int x, int y, int[] dropCoordinates) {
        final Rect r = mDragRect;
        final int count = container.getChildCount();
        if (DEBUG) Log.d(TAG,"-----findDropTarget count = " + count +" ,x = " + x + ", y = " + y); 
        final int scrolledX = x + container.getScrollX();
        final int scrolledY = y + container.getScrollY();
        final View ignoredDropTarget = mIgnoredDropTarget;

        for (int i = count - 1; i >= 0; i--) {
            final View child = container.getChildAt(i);
            if (child.getVisibility() == VISIBLE && child != ignoredDropTarget) {
                child.getHitRect(r);
                if(child instanceof IphoneBubbleTextView){
                	int left = r.left + r.width()/4;
                	int top = r.top + r.height()/4;
                	int right = left + 2*r.width()/3;
                	int bottom = top + 2*r.height()/3;
                 	r.set(left, top, right, bottom);
                }
                if (r.contains(scrolledX, scrolledY)) {
                	if (DEBUG) Log.d(TAG," findDropTarget is r.contains(scrolledX, scrolledY)");
                    DropTarget target = null;
                    if (child instanceof ViewGroup) {
                        x = scrolledX - child.getLeft();
                        y = scrolledY - child.getTop();
                        target = findDropTarget((ViewGroup) child, x, y, dropCoordinates);
                    }
                    if (target == null) {
                        if (child instanceof DropTarget && child.getAnimation() == null) {
                        	DropTarget childTarget = (DropTarget) child;
                        	if (childTarget.acceptDrop(mDragSource, x, y, 0, 0, mDragInfo)) {
	                            dropCoordinates[0] = x;
	                            dropCoordinates[1] = y;
	                            return (DropTarget) child;
                        	}else{
                        		return null;
                        	}
                        }
                    } else {
                        return target;
                    }
                }
            }
        }

        return null;
    }

    public void setDragScoller(DragScroller scroller) {
        mDragScroller = scroller;
    }

    public void setDragListener(DragListener l) {
        mListener = l;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void removeDragListener(DragListener l) {
        mListener = null;
    }

    /**
     * Specifies the view that must be ignored when looking for a drop target.
     *
     * @param view The view that will not be taken into account while looking
     *        for a drop target.
     */
    void setIgnoredDropTarget(View view) {
        mIgnoredDropTarget = view;
    }

    /**
     * Specifies the delete region.
     *
     * @param region The rectangle in screen coordinates of the delete region.
     */
    void setDeleteRegion(RectF region) {
        mDragRegion = region;
    }

    private class ScrollRunnable implements Runnable {
        private int mDirection;

        ScrollRunnable() {
        }

        public void run() {
//        	if(DEBUG) Log.d(TAG,"ScrollRunnable.run!");
    		if (mDragScroller != null) {
                if (mDirection == SCROLL_LEFT) {
                    mDragScroller.scrollLeft();
//                        storeStopItemCoordinate(null);
                } else {
                    mDragScroller.scrollRight();
//                        storeStopItemCoordinate(null);
                }
                mScrollState = SCROLL_OUTSIDE_ZONE;
            }
        }

        void setDirection(int direction) {
            mDirection = direction;
        }
    }
    Object obj = new Object();
    float mX;
    float mY;
    void resetWorkspaceWhenLongClickPowerKey(){
    	try {
    		if (mDragging) {
    		   if(mVelocityTracker != null){
        		   mVelocityTracker.recycle();
        		   mVelocityTracker = null;
        	   }
				removeCallbacks(mScrollRunnable);
			    drop(mX,mY);
			    endDrag();
			}
		} catch (Exception e) {
			Log.e("zsc", "DragLayer.resetWorkspaceWhenLongClickPowerKey()--Exception!!!");
			e.printStackTrace();
		}
    }
}
