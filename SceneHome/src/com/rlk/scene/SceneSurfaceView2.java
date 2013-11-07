package com.rlk.scene;
 
//liyang 0816 add start
import java.io.InputStream;
//liyang 0816 add end
import java.util.HashMap;
import java.util.Random; 
import java.util.Set; 
import com.rlk.scene.R;  
import com.rlk.scene.items.BookCaseItem;
import com.rlk.scene.items.CalendarItem;
import com.rlk.scene.items.ClockItem;
import com.rlk.scene.items.LightItem;
import com.rlk.scene.items.MusicItem;
import com.rlk.scene.items.MyButton;
import com.rlk.scene.items.MySeekbar;
import com.rlk.scene.items.NotesItem;
import com.rlk.scene.items.TouchableDrawableGroup;
import com.rlk.scene.items.TouchableDrawableItem; 
import com.rlk.scene.items.WeatherItem;
import android.os.Handler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect; 
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable; 
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.VelocityTracker;
import android.view.WindowManager; 
import android.view.View;
import com.mediatek.common.featureoption.FeatureOption;

public class SceneSurfaceView2 extends SurfaceView implements SurfaceHolder.Callback,TouchableDrawableItem.OnClickListener{

	private SurfaceHolder surfaceHolder;
	private Canvas canvas;
	private Paint paint; 

	public int screenW, screenH;
	private int middleBmpW,middleBmpH;

	private Bitmap viewBmp;
	private Bitmap wallBmp;
	private Bitmap allSurfaceBmp;
	private Bitmap bookDeleteBmp;
//	private Bitmap middleBmp;
//	private Bitmap nearBmp;
	private Rect src, dst;
 
	private int spacX = 0, spacY = 0;
	private static int MIDDLE_DIS = 0;
	public final double SPEED_DIS = 1.498148;
	
	private int middleSpacX = MIDDLE_DIS;
	private int mDX = 0;
	//liyang 0816
	public static int defaultX = -1;
	public static int defaultY = -1;
	
	private int viewPositionX = 0;
//	private boolean isLeftScroll = true;
	
	private static final int INVALID_POINTER_ID = -1;
	// The ??????active pointer?????? is the one currently moving our object.
	private int mActivePointerId = INVALID_POINTER_ID;
	float mLastTouchX;
	private float mLastDownX;
//	private float mScrollDis;
	float mLastTouchY;
	
	private VelocityTracker mVelocityTracker;
	private static final int SNAP_VELOCITY = 600;
	
	//liyang 0816
	private int AllViewCounts = 3; 
	public int mCurScreen; 
	private String TAG = "SceneSurfaceView2"; 
	public static SceneSurfaceView2 instance;
	private ClockItem mClock;
	//private CalendarItem mCalendar;
    private MusicItem mMusic;
    //private MyButton mMap;
    //private MyButton mBooks;
    private MyButton mCamera;
    //private MyButton mAppStore;
    //private MyButton mAppStoreBg;
    private MyButton mRadio;
    private MyButton mBrowser;
    private MyButton mPhone;
    private MyButton mSms;
    private MyButton mPlaces;
    private MyButton mHomebt;
    private MyButton mContacts;	
    private MyButton mSettings;
    private MyButton mVideo;
    private MyButton mGallery;
	private MyButton mCalculator;
    private NotesItem mNotes;
	//liyang 0907 start
	private MyButton mSuningEbuy;
	private MyButton mSuningCloud;
	private MyButton mSuningApps;
	private MyButton mSuningRead;
	//liyang 0907 end
    //private MyButton mNotesBg;
    private MyButton mGames;
    private LightItem mLight;
//    private MyButton mOutside;
    //private MyButton mAlarm;
    //private MyButton mFileManager;
    private WeatherItem mWeather;
    //private BookCaseItem mBookCase;
    //private MyButton mBookAddEnter;
    public static TouchableDrawableGroup mGroup;
    private TouchableDrawableGroup mGroup1;
    private TouchableDrawableGroup mGroup2;
    private TouchableDrawableGroup mGroup3;    
    private Context mContext;
    private Scroller mScroller; 
//    public boolean mStopFrash;
    public boolean isTouchable=false; 
    Handler mHandler=new Handler();
    
    
	public SceneSurfaceView2(Context context, AttributeSet attrs) {
		super(context); 
		mContext=context;
		Log.d(TAG, "SceneSurfaceView2 ");
		instance = this; 
		surfaceHolder = this.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setFormat(PixelFormat.RGBA_8888);
		paint = new Paint(); 
		paint.setColor(Color.WHITE); 
		setFocusable(true);
		setFocusableInTouchMode(true);
		 
		DisplayMetrics metrics = new DisplayMetrics();
		((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
		screenW = metrics.widthPixels/(AllViewCounts-1);
		screenH = metrics.heightPixels - 38;
		Log.d("liyang", "-->screenW = "  + screenW + "screenH = " + screenH);
		//liyang 0816
		middleBmpW = 2560;
		middleBmpH = 800;
		
		mScroller = new Scroller(context);
		
		initViewItems();
		//liyang 0822
		snapToScreen(1);
	}

	private void initViewItems() {
		mClock = new ClockItem(mContext, 1108, 462, 112, 112);
		mClock.setOnClickListener(this);
		//mCalendar = new CalendarItem(mContext, 540 + 300, 210, 100, 80);
		//mCalendar.setOnClickListener(this);
		mMusic = new MusicItem(mContext, 1369, 276, 241, 195);
		mMusic.setOnClickListener(this);
		//mMap = new MyButton(mContext, 36, 70, 200, 122, 0);
		//mMap.setOnClickListener(this);

		//mBooks = new MyButton(mContext, 2147, 370, 410, 219, 0);
		//mBooks.setOnClickListener(this);

		mCamera = new MyButton(mContext, 1944, 562, 201, 157, 0);
		mCamera.setOnClickListener(this);

		mRadio = new MyButton(mContext, 1078, 478, 240, 127, 0);
		mRadio.setOnClickListener(this);

		//mFileManager = new MyButton(mContext, 573, 631, 378, 143, 0);
		//mFileManager.setOnClickListener(this);

		mSettings = new MyButton(mContext, 2334, 94, 213, 228, 0);
		mSettings.setOnClickListener(this);
		//mAppStore = new MyButton(mContext, 241 + 540 + 269, 590, 434 - 241,
				//817 - 590, 0);
		//mAppStore.setOnClickListener(this);
		//mAppStoreBg = new MyButton(mContext, 1001, 638, 245, 187, R.drawable.appstore_bg);
		
		mLight = new LightItem(mContext, 1987, 38, 196, 51, 0);
		// mLight.setOnClickListener(this);
		mVideo = new MyButton(mContext, 1309, 66, 215, 195, 0);
		mVideo.setOnClickListener(this);

		mBrowser = new MyButton(mContext, 1619, 422, 301, 194, 0);
		mBrowser.setOnClickListener(this);
		mGallery = new MyButton(mContext, 1615, 147, 297, 179, 0);
		mGallery.setOnClickListener(this);
		mCalculator = new MyButton(mContext, 1967, 137, 335, 119, 0);
        mCalculator.setOnClickListener(this);
		mPhone = new MyButton(mContext, 853, 393, 170, 203, 0);
		mPhone.setOnClickListener(this);
		mSms = new MyButton(mContext, 1083, 256, 167, 173, 0);
		mSms.setOnClickListener(this);
		mContacts = new MyButton(mContext, 671, 498, 113, 148, 0);
		mContacts.setOnClickListener(this);		
		mPlaces = new MyButton(mContext, 44, 102, 146, 248, 0);
		mPlaces.setOnClickListener(this);		
		mHomebt = new MyButton(mContext, 806, 613, 137, 96, 0);
		mHomebt.setOnClickListener(this);
		mSuningEbuy = new MyButton(mContext, 0, 428, 421, 324, 0);
        mSuningEbuy.setOnClickListener(this);
		mSuningCloud = new MyButton(mContext, 640, 57, 257, 156, 0);
		mSuningCloud.setOnClickListener(this);
		mSuningApps = new MyButton(mContext, 1922, 261, 222, 212, 0);
		mSuningApps.setOnClickListener(this);
		mSuningRead = new MyButton(mContext, 2147, 370, 410, 219, 0);
		mSuningRead.setOnClickListener(this);
		mNotes = new NotesItem(mContext, 276, 242, 133, 162);
		mNotes.setOnClickListener(this);

		//mNotesBg = new MyButton(mContext, 2567, 365, 295, 557, R.drawable.notes_bg);
		//mNotesBg.setClickable(false);
		
		//mAlarm = new MyButton(mContext, 345 + 2160, 435, 80, 75, 0);
		//mAlarm.setOnClickListener(this);
		
		mWeather = new WeatherItem(mContext, 942, 25, 338, 229, 0);
		mWeather.setOnClickListener(this);
		
//		mOutside = new MyButton(mContext, 15 + 2160, 0, 500, 420, 0);
//		mOutside.setOnClickListener(this);
		
		//mBookCase = new BookCaseItem(mContext, 13 + 540, 119,211,456, 0);

		//mBookAddEnter = new MyButton(mContext, 540+7, 611-38, 230, 128, R.drawable.bookadd_bg);
		//mBookAddEnter.setOnClickListener(this);
		
		mGames = new MyButton(mContext, 1617, 686, 213, 121, 0);
		mGames.setOnClickListener(this);

		mGroup1 = new TouchableDrawableGroup(getContext(), 0, 0, getWidth(),
				getHeight());
		//mGroup1.addItem(mCalendar);
		mGroup1.addItem(mClock);
		//mGroup1.addItem(mMap);
		//mGroup1.addItem(mBooks);
		mGroup1.addItem(mCamera);
		mGroup1.addItem(mRadio);
		//mGroup1.addItem(mFileManager);
		mGroup1.addItem(mSettings);
//		mGroup1.addItem(mLight);
		mGroup1.addItem(mWeather); //liyang 0822
		//mGroup1.addItem(mBookAddEnter);
		mGroup1.addItem(mVideo);
		mGroup1.addItem(mGallery);
		mGroup1.addItem(mCalculator);
		//mGroup1.addItem(mAlarm);
		//mGroup1.addItem(mBookCase);
//		mGroup1.addItem(mOutside);

		mGroup1.addItem(mBrowser);
		mGroup1.addItem(mPhone);
		mGroup1.addItem(mSms);
		mGroup1.addItem(mContacts);
		mGroup1.addItem(mPlaces);
		mGroup1.addItem(mHomebt);
		mGroup1.addItem(mLight);
		mGroup1.addItem(mMusic);
		mGroup1.addItem(mNotes);		
		mGroup1.addItem(mGames); 
		mGroup1.addItem(mSuningEbuy);
		mGroup1.addItem(mSuningCloud);
		mGroup1.addItem(mSuningApps);
		mGroup1.addItem(mSuningRead);
		
		mGroup2 = new TouchableDrawableGroup(getContext(), 0, 0, getWidth(),
				getHeight());

		//mGroup2.addItem(mAppStore);
		//mGroup2.addItem(mAppStoreBg);
		//mGroup2.addItem(mBrowser);
		//mGroup2.addItem(mLight);
		//mGroup2.addItem(mMusic);
		//mGroup2.addItem(mNotesBg);
		//mGroup2.addItem(mNotes);		
		//mGroup2.addItem(mGames); 

		mGroup3 = new TouchableDrawableGroup(getContext(), 0, 0, getWidth(),
				getHeight());
		//mGroup3.addItem(mWeather); //liyang 0822
		
		mGroup = new TouchableDrawableGroup(getContext(), 0, 0, getWidth(),
				getHeight());
		mGroup.addItem(mGroup1);
		mGroup.addItem(mGroup2);  
		mGroup.addItem(mGroup3);
		
		mGroup.updateTouchRect(0);
	}
    
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.d(TAG, "surfaceChanged");
         /*     	new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() { 
				isTouchable=true;
			}
		}, 2000);
       //liuwei 20130515 GBLYSW-176 off
          */

	}
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }
    
    public void onPause()
    {
//    	mDestroyed = true;
    		mClock.onPause();
    		//mCalendar.onPause();
    		mMusic.onPause();
    		mWeather.onPause();
    }
    public void onResume()
    {
//    	mDestroyed = false;
//		startRefrashWidget();
    	isBookRunning = false;
    	mClock.onResume();
		//mCalendar.onResume();
    	mMusic.onResume();
    	mNotes.onResume();
    	mWeather.onResume();
    	if(allSurfaceBmp != null){
    		snapToScreen(mCurScreen);	
    	}
    	
    }
    
       
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }
	
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "surfaceCreated ");
		init();    
//		mStopFrash = false;
		
	} 
//	private long firstTime;
	int velocityX;
	 
	public boolean onTouchEvent(MotionEvent event) {
		final int action = MotionEventCompat.getActionMasked(event);
		
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain(); 
		}
		mVelocityTracker.addMovement(event);
		
//		Log.d(TAG, "action = " + action);
		switch (action) {
		case MotionEvent.ACTION_DOWN: {
//			count = 0;
			final int pointerIndex = MotionEventCompat.getActionIndex(event);
			final float x = MotionEventCompat.getX(event, pointerIndex);
			final float y = MotionEventCompat.getY(event, pointerIndex); 
//			Log.d(TAG, "event.getDownTime()=" + downTime); 
//			firstTime = System.currentTimeMillis();
			// Remember where we started (for dragging) 
			
			mLastTouchX = x;
			isTouch = true;
			mLastDownX = x;
			mLastTouchY = y;
			// Save the ID of this pointer (for dragging)
			mActivePointerId = MotionEventCompat.getPointerId(event, 0);
			break;
		}

		case MotionEvent.ACTION_MOVE: {
			// Find the index of the active pointer and fetch its position
			final int pointerIndex = MotionEventCompat.findPointerIndex(event, mActivePointerId);

			final float x = MotionEventCompat.getX(event, pointerIndex);
			final float y = MotionEventCompat.getY(event, pointerIndex);
 
			// Calculate the distance moved
			final float dx = x - mLastTouchX;
			final float dy = y - mLastTouchY; 
			
			
			// Remember this touch position for the next move event
			mLastTouchX = x; 
			mLastTouchY = y;
			 
			mDX = Math.round(dx); 
			
//			Log.d(TAG, "x=" + x + ";dx=" + dx + ";mLastDownX=" + mLastDownX +
//					";mLastTouchX=" + mLastTouchX + ";mDX=" + mDX);
			if (mDX != 0) {
				spacX -= mDX;
				middleSpacX -= SPEED_DIS*mDX;
			} 
			if(wallBmp!=null)
			{
			if (spacX >= wallBmp.getWidth() - screenW*(AllViewCounts-1)) {
				spacX = wallBmp.getWidth() - screenW*(AllViewCounts-1); 
			} else if (spacX <= 0) {
				spacX = 0; 
			}
			}
			if (middleSpacX >= middleBmpW - screenW) {
				middleSpacX = middleBmpW - screenW;
			} else if (middleSpacX <= MIDDLE_DIS) {
				middleSpacX = MIDDLE_DIS;
			}
 
//			Log.d(TAG, "spacX=" + spacX + ";deltaX=" + deltaX);
			
			drawScene((int)x, (int)y);	  
			 
			break;
		} 
		case MotionEvent.ACTION_UP: { 
//			long finishTime = System.currentTimeMillis();
//			Log.d(TAG, "Move Time=" + (finishTime - firstTime) + ";count=" + count + ";count/time=" + count/(finishTime - firstTime + 0.0)*1000);  
			isTouch = false;
			mActivePointerId = INVALID_POINTER_ID;
			final int pointerIndex = MotionEventCompat.getActionIndex(event);
			final float x = MotionEventCompat.getX(event, pointerIndex);
			final float y = MotionEventCompat.getY(event, pointerIndex);
  
				final VelocityTracker velocityTracker = mVelocityTracker;
				velocityTracker.computeCurrentVelocity(1000);
				velocityX = (int) velocityTracker.getXVelocity();
				
//				Log.d(TAG, "velocityX:" + velocityX); 
				if ((velocityX > SNAP_VELOCITY && mCurScreen > 0) 
						|| (spacX < mCurScreen*screenW -screenW/2 && mCurScreen > 0)) {
					// Fling enough to move left
//					Log.d(TAG, "snap left");    
					snapToScreen(mCurScreen - 1);
				} else if ((velocityX < -SNAP_VELOCITY&& mCurScreen < getChildCount() - 1)
						|| (spacX > mCurScreen*screenW + screenW/2 && mCurScreen < getChildCount() - 1)){
					// Fling enough to move right
//					Log.d(TAG, "snap right");  
					snapToScreen(mCurScreen + 1);
				} else {
//					Log.d(TAG, "mLastDownX=" + mLastDownX + ";mLastTouchX=" + mLastTouchX);  
					snapToDestination();
				}

				if (mVelocityTracker != null) {
					mVelocityTracker.recycle();
					mVelocityTracker = null;
				} 
			break;
		}

		case MotionEvent.ACTION_CANCEL: {
//			Log.d(TAG, "MotionEvent.ACTION_CANCEL");
			snapToDestination();
			mActivePointerId = INVALID_POINTER_ID;
			break;
		}

		case MotionEvent.ACTION_POINTER_UP: {

			final int pointerIndex = MotionEventCompat.getActionIndex(event);
			final int pointerId = MotionEventCompat.getPointerId(event, pointerIndex);

			if (pointerId == mActivePointerId) {
				// This was our active pointer going up. Choose a new
				// active pointer and adjust accordingly.
				final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
				mLastTouchX = MotionEventCompat.getX(event, newPointerIndex);
				mLastTouchY = MotionEventCompat.getY(event, newPointerIndex);
				mActivePointerId = MotionEventCompat.getPointerId(event, newPointerIndex);
			}
			break;
		}
		}
		
		return true;
	}
  
	
	public void init() {
		mHandler.post(new Runnable() {
			@Override
			public void run() { 
				if (wallBmp == null) {
					//liyang 0816 change the way of decode start
					/*Options mOptions = new Options();
					mOptions.inPreferredConfig = Config.RGB_565;
					wallBmp = BitmapFactory.decodeResource(getResources(),
							R.drawable.scene_f, mOptions);*/

					Options opt = new Options();
					opt.inPreferredConfig = Config.RGB_565;
					opt.inPurgeable = true;
					opt.inInputShareable = true;
					//get source image
					InputStream is = getResources().openRawResource(R.drawable.scene_f);
					wallBmp = BitmapFactory.decodeStream(is, null, opt);
					//liyang 0816 change the way of decode end
					Log.d("liyang", "-->wallBmp.getWidth(): " + wallBmp.getWidth() + ", wallBmp.getHeight(): " + wallBmp.getHeight() );
				}
				Log.e("test3", "handler decode finish!!!!!");
				startRefrashWidget();
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() { 
						isTouchable = true;
						MainActivity.mLayout.setVisibility(View.GONE);
					}
				}, 2000);

				Log.e("test3", "handler startRefrashWidget!!!!!");
				// drawScene(defaultX, defaultY);
			}
		});
		bookDeleteBmp = BitmapFactory.decodeResource(getResources(),
				R.drawable.bookcase_delete);
	}
	
	public void bindBookCase(HashMap<Integer,ApplicationInfo> shortcuts){  
		drawScene(SceneSurfaceView2.defaultX,SceneSurfaceView2.defaultY);
	}
	
	private Bitmap drawableToBitmap(Drawable drawable) {   
        int w = drawable.getIntrinsicWidth();  
        int h = drawable.getIntrinsicHeight();  
   
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888  
                : Bitmap.Config.RGB_565;   
        Bitmap bitmap = Bitmap.createBitmap(w+10, h+12, config);     
        Canvas canvas = new Canvas(bitmap);  
        Bitmap iconBitmap;
        if(drawable instanceof FastBitmapDrawable){
        	iconBitmap = ((FastBitmapDrawable) drawable).getBitmap();
        }else{
        	iconBitmap = ((BitmapDrawable) drawable).getBitmap();
        } 
        canvas.drawBitmap(iconBitmap, 8, 10, paint);
        if(isTranslateAnim){
        	canvas.drawBitmap(bookDeleteBmp, -2, -2, paint);	
        }         
        return bitmap;  
    }
	
    private final Random mRandom = new Random();
    private float mTranslateX;
    private float mTranslateY;
    private static final float TRANSLATE_DIS = 0.8f;
    public static boolean isTranslateAnim = false;

	public synchronized void startRefrashWidget(){
//		new Thread(new Runnable() {
//			@Override
//			public void run() {  
//				while(!mStopFrash){
					Log.d(TAG, "startRefrashWidget mScroller.mFinished=" + mScroller.mFinished 
							+ ";isTouch=" + isTouch + "isTranslateAnim =" + isTranslateAnim);
//					try { 
						if(wallBmp != null){ 
							if(allSurfaceBmp == null){
								final Bitmap.Config c = Bitmap.Config.ARGB_8888;
								allSurfaceBmp = Bitmap.createBitmap(wallBmp.getWidth(),
										wallBmp.getHeight(), c);
							}
							Canvas canvas = new Canvas(allSurfaceBmp); 
							mGroup3.drawItem(canvas, 0, true);
							canvas.drawBitmap(wallBmp, 0, 0, paint);  
							if(!isTranslateAnim){
								if (BookCaseModel.mBookmap != null) {
									Bitmap bitmap = null;
									Set<Integer> mBookKeys = BookCaseModel.mBookmap
											.keySet();
									for (Integer i : mBookKeys) {
										final ApplicationInfo info = (ApplicationInfo) BookCaseModel.mBookmap
												.get(i);
										if (info != null) {
											bitmap = drawableToBitmap(info.icon);
											canvas.drawBitmap(
													bitmap,
													info.mTranslateX 
															+ screenW
															+ info.cellX
															* BookCaseModel.mCellWidth
															+ BookCaseModel.mShortAxisStartPadding - 10,
													info.mTranslateY
															+ info.cellY
															* BookCaseModel.mCellHeight
															+ BookCaseModel.mLongAxisStartPadding - 10,
													paint);
										}
									}
									if (bitmap != null) {
										bitmap.recycle();
										bitmap = null;
									}
								}
								
								
							}
//							else{
//								mStopFrash = true;
//							}
//							mGroup1.setAlwaysDraw(true);
							mGroup1.drawItem(canvas, 0, true);
//							allSurfaceBmp = Bitmap.createBitmap(surfaceBp);
//							surfaceBp.recycle();  
						} 
//						if(!isTouch){ 
							drawScene(defaultX, defaultY);
//						}
//						Thread.sleep(900);
//					} catch (InterruptedException e) { 
//						e.printStackTrace();
//					}
//				}
//			}
//		}).start();
	}
	private void getTranslateXandY() {
		float f = mRandom.nextInt(3);
		if (f == 2) {
			mTranslateX = TRANSLATE_DIS;
		} else if (f == 0) {
			mTranslateX = -TRANSLATE_DIS;
		} else {
			mTranslateX = 0;
		}
		f = mRandom.nextInt(3);
		if (f == 2) {
			mTranslateY = TRANSLATE_DIS;
		} else if (f == 0) {
			mTranslateY = -TRANSLATE_DIS;
		} else {
			mTranslateY = 0;
		} 	
	}
	
	public void startTranslateThread(){
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(isTranslateAnim){ 
					if (BookCaseModel.mBookmap != null) { 
						Set<Integer> mBookKeys = BookCaseModel.mBookmap
								.keySet();
						for (Integer i : mBookKeys) {
							final ApplicationInfo info = (ApplicationInfo) BookCaseModel.mBookmap
									.get(i);
							if(info != null){
								getTranslateXandY(); 
								info.mTranslateX = mTranslateX;
								info.mTranslateY = mTranslateY;
							}
						}	
					}
					if(!BookCaseItem.isDrag){
						drawScene(SceneSurfaceView2.defaultX,SceneSurfaceView2.defaultY);	
					} 
					try {
						Thread.sleep(100);
					} catch(Exception e) {
						Log.e(TAG,"Thread Exception " + e.toString());
					}
				} 
				mTranslateX = 0;
				mTranslateY = 0;
			}
		}).start();
	}

	public boolean shouldRefrash(int posScreen, int posStartX, int posEndY){
		if(spacX > posStartX + (posScreen-1)*screenW
				&& spacX < posScreen*screenW + posStartX + 
				posEndY){
			return true;
		}
		return false;
	}
	
	
	
	Matrix mMatrix;
//	int count ; 
	public synchronized void drawScene(int x, int y) {
//		count++; 
		long firstTime = System.currentTimeMillis();
		Log.d(TAG, "drawScene");
		if(isBookRunning){
			return;
		}
		canvas = surfaceHolder.lockCanvas(); 
//		try {
			if (canvas != null && allSurfaceBmp != null) { 
				src = new Rect(spacX, spacY, spacX + screenW, screenH);
				dst = new Rect(0, 0, screenW, screenH);
				canvas.drawColor(Color.BLACK);   
				if(mMatrix == null){
					mMatrix = new Matrix();
				}
				mMatrix.setTranslate(-spacX, 0);
				canvas.drawBitmap(allSurfaceBmp, mMatrix, paint);	
//					mClock.drawItem(canvas, spacX ,false); 
				
				if (isTranslateAnim) {
					if (shouldRefrash(1, BookCaseModel.mShortAxisStartPadding,
							BookCaseModel.mCellWidth
									* BookCaseModel.mShortAxisCells)) {
						if (BookCaseModel.mBookmap != null) {
							Bitmap bitmap = null;
							Set<Integer> mBookKeys = BookCaseModel.mBookmap
									.keySet();
							for (Integer i : mBookKeys) {
								final ApplicationInfo info = (ApplicationInfo) BookCaseModel.mBookmap
										.get(i);
								if (info != null) {
									bitmap = drawableToBitmap(info.icon);
									canvas.drawBitmap(
											bitmap,
											info.mTranslateX
													- spacX
													+ screenW
													+ info.cellX
													* BookCaseModel.mCellWidth
													+ BookCaseModel.mShortAxisStartPadding - 10,
											info.mTranslateY
													+ info.cellY
													* BookCaseModel.mCellHeight
													+ BookCaseModel.mLongAxisStartPadding - 10,
											paint);
								}
							}
							if (bitmap != null) {
								bitmap.recycle();
							}
						}
					}
					if (MainActivity.instance.mBookModel.mDragInfo != null
							&& x != defaultX && y != defaultY) {
						Bitmap dragBitmap = drawableToBitmap(MainActivity.instance.mBookModel.mDragInfo.icon);
						canvas.drawBitmap(dragBitmap, x
								- BookCaseModel.mCellWidth / 2, y
								- BookCaseModel.mCellHeight / 2, paint);
					}
				}   
//				mGroup2.setAlwaysDraw(false);
				mGroup2.drawItem(canvas, middleSpacX, false);  
			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		} finally {
			if (canvas != null) {
				surfaceHolder.unlockCanvasAndPost(canvas);
			}
//		}
		long finishTime = System.currentTimeMillis();
//		Log.d(TAG, "firstTime - finishTime=" + (firstTime - finishTime));
	}
	 
	
	public void recycle(){ 
		if (wallBmp != null) {
			wallBmp.recycle();
		} 
		if(allSurfaceBmp != null){
			allSurfaceBmp.recycle();
		} 
		mGroup.recycle(); 
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "surfaceDestroyed"); 
//		mStopFrash = true;
		isTranslateAnim = false;
		isTouch = false;
		BookCaseItem.isDrag = false;
		MySeekbar.isSeekbarTouched = false;
	}
	
	public int getCurScreen() {
		return mCurScreen;
	}
	
	private int getChildCount(){
		return AllViewCounts;
	}
	
//	public void setToScreen(int whichScreen) {
//		whichScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
//		mCurScreen = whichScreen;
//		scrollTo(whichScreen * getWidth(), 0);
//	}
	
	public void snapToDestination() { 
		final int destScreen = mCurScreen;
//		Log.d(TAG, "snapToDestination destScreen=" + destScreen + ";mCurScreen=" + mCurScreen + ";spacX=" + spacX);
		snapToScreen(destScreen);
	}
	
	public void snapToScreen(int whichScreen) {  
		mCurScreen = whichScreen;
		startScroll(whichScreen);
	}
	
//	private static final int REFRESH_WIDGET = 0;
//	Handler mHandler = new Handler(){
//
//		@Override
//		public void handleMessage(Message msg) {
//			switch (msg.what) {
//			case REFRESH_WIDGET:
//				mStopFrash = false;
//				startRefrashWidget();
//				break; 
//			default:
//				break;
//			}
//		}
//		
//	}; 
	private boolean isBookRunning;
	private void startBookEnterAnim(){
		new Thread(new Runnable() {
			
			@Override
			public void run() { 
				int count = 0;  
//				mStopFrash = true; 
				isBookRunning = true;
				while(count<6 ){  
					long startTime = System.currentTimeMillis(); 
					canvas = surfaceHolder.lockCanvas(new Rect(7, 611-38+3, 7+230, 611-38+128+3)); 
					try {  
						canvas.drawColor(Color.BLACK);   
						if(mMatrix == null){
							mMatrix = new Matrix();
						}
						mMatrix.setTranslate(-spacX, 0);
						canvas.drawBitmap(wallBmp, mMatrix, paint);
						canvas.drawBitmap(Utilities.mBookEnterAnimMap.get(count), 7, 611-38, null);	 
					} catch (Exception e) { 
					}finally{
						surfaceHolder.unlockCanvasAndPost(canvas);
					}  
					long endTime = System.currentTimeMillis();
//					Log.d(TAG, "startBookEnterAnim endTime - startTime=" + (endTime - startTime));
					try { 
						if(endTime-startTime < 60){
							Thread.sleep(60-(endTime-startTime));
						}
						
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					count++; 
				
				}   
//				Intent mIntent=new Intent();
//				mIntent.putExtra("shortcut", true);
//				mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
//				mIntent.setComponent(new ComponentName("com.rlk.scene","com.rlk.scene.Launcher"));
//				mContext.startActivity(mIntent);
			}
			
		}).start();
	}
	
//	private void startBookAnim(){
//		new Thread(new Runnable() {
//			
//			@Override
//			public void run() { 
//				int count = 0;  
//				mStopFrash = true;
//				mHandler.removeMessages(REFRESH_WIDGET);
//				while(!isTouch && count<7 && mScroller.isFinished()){  
//					long startTime = System.currentTimeMillis(); 
//					canvas = surfaceHolder.lockCanvas(new Rect(7, 611-38+10, 7+240, 611-38+10+132)); 
//					try {  
//						canvas.drawColor(Color.BLACK);   
//						if(mMatrix == null){
//							mMatrix = new Matrix();
//						}
//						mMatrix.setTranslate(-spacX, 0);
//						canvas.drawBitmap(wallBmp, mMatrix, paint);
//						canvas.drawBitmap(Utilities.mBookAnimMap.get(count), 7, 611-38, null);	 
//					} catch (Exception e) { 
//					}finally{
//						surfaceHolder.unlockCanvasAndPost(canvas);
//					}  
//					long endTime = System.currentTimeMillis();
//					Log.d(TAG, "startBookAnim endTime - startTime=" + (endTime - startTime));
//					try { 
//						if(endTime-startTime < 50){
//							Thread.sleep(50-(endTime-startTime));
//						}
//						
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//					count++; 
//				
//				} 
//				mHandler.removeMessages(REFRESH_WIDGET);
//				mHandler.sendEmptyMessageDelayed(REFRESH_WIDGET, 1500);
//			}
//		}).start();
//	}
	
	public boolean isScrollFinished(){
		return mScroller.isFinished();
	}
	
	public boolean isTouch = false;   
	private void startScroll(int curScreen) {  
		
		final int deltaX = curScreen*screenW - spacX;   
 	 
		double multiple;
		int v = Math.abs(velocityX);
		if(v > 12000){
			v = 12000;
		}
		multiple = 2 - v/8000.0;
 
//		Log.d(TAG, "multiple=" + multiple + ";deltaX=" + deltaX + 
//				";duration=" + (int)(Math.abs(deltaX)*multiple));
		mMusic.stopClock();
		mClock.stopClock();
		
		mScroller.startScroll(spacX, 0, deltaX, 0, (int)(Math.abs(deltaX)*multiple));
		new Thread(new Runnable() {
			
			@Override
			public void run() {  
//				Log.d(TAG, "mScroller.getCurrX():" + mScroller.getCurrX() 
//						+ ";mScroller.computeScrollOffset():" + mScroller.computeScrollOffset()
//						+ ";isTouch=" + isTouch);
				android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
				while(mScroller.computeScrollOffset()&& !isTouch){  
					long startTime = System.currentTimeMillis();
					drawScene(defaultX, defaultX);
					spacX = mScroller.getCurrX(); 
//					Log.d(TAG, "mScroller.getCurrX():" + mScroller.getCurrX()); 
					middleSpacX = (int) (spacX*SPEED_DIS + MIDDLE_DIS);
					long endTime = System.currentTimeMillis();
					Log.d(TAG, "one endTime - startTime=" + (endTime - startTime));
					if(endTime - startTime < 20){
						try { 
							Thread.sleep(20 - (endTime - startTime));
						} catch (InterruptedException e) { 
							e.printStackTrace();
						}
					}else{
						int count = (int) ((endTime - startTime)/30);
						for(int i=1; i<count; i++){
							mScroller.computeScrollOffset();
						}
					}
					endTime = System.currentTimeMillis();
					Log.d(TAG, "two endTime - startTime=" + (endTime - startTime));
				}
//				Log.d(TAG, "mScroller.mFinished true");   
				mGroup1.updateTouchRect(spacX);
				mGroup3.updateTouchRect(spacX);
//				mGroup2.updateTouchRect(middleSpacX);
//				if(mCurScreen == 1 && Math.abs(mLastDownX - mLastTouchX)>20){
//					startBookAnim();
//				} 
				drawScene(defaultX, defaultX);
				mMusic.startClock();
				mClock.startClock();
			}
		}).start(); 
	}
	
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) { 
//		Log.d(TAG, "dispatchKeyEvent getKeyCode=" + event.getKeyCode());
		if (event.getAction() == KeyEvent.ACTION_DOWN) { 
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_HOME:
                    return true; 
            }
        } else if (event.getAction() == KeyEvent.ACTION_UP) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_HOME:   
                    return true; 
            }
        }
		return super.dispatchKeyEvent(event);
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) { 
		mGroup.dispatchEvent(event);
//		Log.d(TAG, "BookCaseItem.isDrag=" + BookCaseItem.isDrag + 
//				";TouchableDrawableItem.mHasPerformedLongPress" + TouchableDrawableItem.mHasPerformedLongPress);
		if(MySeekbar.isSeekbarTouched)
			return true;
		if(isTranslateAnim){
			isTouch = false;
			return true;
		}
		if(isBookRunning){
			return true;
		}
		return super.dispatchTouchEvent(event);
	}	
	@Override
	 public boolean onKeyDown(int keyCode, KeyEvent event) {
		return super.onKeyDown(keyCode, event);
	 }

	@Override
	public void onClick(TouchableDrawableItem item, MotionEvent event) {
//		Log.d(TAG, "onClick item=" + item);
		Intent mIntent=null; 
		if(FeatureOption.RLK_GP818H_A1_SN_SUPPORT){
		 if (item==mClock) {
			mIntent=new Intent();
			mIntent.setComponent(new ComponentName("com.android.deskclock","com.android.deskclock.DeskClock"));   
		}/*else if (item==mBooks) {
			mIntent=new Intent();
			mIntent.setComponent(new ComponentName("com.chaozh.iReaderFree15","com.chaozh.iReader.ui.activity.WelcomeActivity"));   ////////
		}*/else if (item==mCamera) {
			mIntent=new Intent();
			mIntent.setComponent(new ComponentName("com.android.gallery3d","com.android.camera.CameraLauncher"));   
		}else if (item==mRadio) {
			mIntent=new Intent();
			mIntent.setComponent(new ComponentName("com.mediatek.FMRadio","com.mediatek.FMRadio.FMRadioActivity"));   
		}else if (item==mBrowser) {
			mIntent=new Intent();
			mIntent.setComponent(new ComponentName("com.android.browser","com.android.browser.BrowserActivity"));   
		}else if (item==mVideo) {
			mIntent=new Intent();
			mIntent.setComponent(new ComponentName("com.mediatek.videoplayer","com.mediatek.videoplayer.MovieListActivity"));   
		}else if (item==mGallery) {
			mIntent=new Intent();
			mIntent.setComponent(new ComponentName("com.android.gallery3d","com.android.gallery3d.app.Gallery")); 
		}else if (item==mCalculator){
			mIntent=new Intent();
			mIntent.setComponent(new ComponentName("com.android.calculator2","com.android.calculator2.Calculator")); 
		}else if (item == mSuningEbuy){
			mIntent = new Intent();
			mIntent.setComponent(new ComponentName("com.suning.pad.ebuy", "com.suning.pad.ebuy.ui.WelcomeScreenActivity"));
		}else if (item == mSuningCloud){
			mIntent = new Intent();
			mIntent.setComponent(new ComponentName("com.sn.cloudsync.activity", "com.sn.cloudsync.screen.StartActivity"));
		}else if (item == mSuningApps){
			mIntent=new Intent();
			mIntent.setComponent(new ComponentName("com.suning.markethd","com.suning.markethd.activity.StartActivity")); 
		}else if (item ==mSuningRead){
			mIntent=new Intent();
			mIntent.setComponent(new ComponentName("com.suning.mobile.magazine","com.suning.mobile.magazine.activity.initialize.SplashActivity")); 
		}else if (item==mNotes) {
			mIntent=new Intent();
			mIntent.setComponent(new ComponentName("com.mediatek.notebook","com.mediatek.notebook.NotesList")); 
		}else if (item==mGames) {
			mIntent=new Intent();
			mIntent.setComponent(new ComponentName("com.vee.easyGame","com.vee.easyGame.LogoActivity")); 
		}else if (item==mMusic) {
			mIntent=new Intent();
			mIntent.setComponent(new ComponentName("com.android.music","com.android.music.MusicBrowserActivity")); 
		}/*else if (item==mFileManager) {
			mIntent=new Intent();
			mIntent.setComponent(new ComponentName("com.mediatek.filemanager","com.mediatek.filemanager.FileManagerOperationActivity")); 
		}*/else if(item==mWeather){
//			Log.d(TAG, "onClick item==mWeather");
			mIntent=new Intent();
			mIntent.setComponent(new ComponentName("com.all.weather","com.all.weather.SettingActivity"));
		}else if(item==mSettings){
//			Log.d(TAG, "onClick item==mWeather");
			mIntent=new Intent();
			mIntent.setComponent(new ComponentName("com.android.settings","com.android.settings.Settings"));
		}else if(item==mPhone){
			mIntent=new Intent();
			mIntent.setComponent(new ComponentName("com.android.contacts","com.android.contacts.activities.DialtactsActivity")); 
		}else if(item==mSms){
			mIntent=new Intent();
			mIntent.setComponent(new ComponentName("com.android.mms","com.android.mms.ui.BootActivity")); 
		}else if(item==mContacts){
			mIntent=new Intent();
			mIntent.setComponent(new ComponentName("com.android.contacts","com.android.contacts.activities.PeopleActivity")); 
		}else if(item==mPlaces){
			mIntent=new Intent();
			mIntent.setComponent(new ComponentName("com.google.android.apps.maps","com.google.android.maps.PlacesActivity")); 
		}else if(item==mHomebt){
			mIntent=new Intent();
			mIntent.setComponent(new ComponentName("com.rlk.scene","com.rlk.scene.Launcher")); 
		}
		if(mIntent!=null)
		{
			try {
				getContext().startActivity(mIntent);
			} catch (Exception e) { 
				Log.d(TAG, "Exception");
			}
		}
		
	}
	} 

	
 
	
}
