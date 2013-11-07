package com.rlk.scene.items;

import java.util.ArrayList;
import java.util.List;

import com.rlk.scene.ApplicationInfo;
import com.rlk.scene.BookCaseModel;
import com.rlk.scene.ItemInfo;
import com.rlk.scene.LauncherModel;
import com.rlk.scene.LauncherSettings;
import com.rlk.scene.MainActivity;
import com.rlk.scene.R;
import com.rlk.scene.SceneSurfaceView2;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Handler;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

public class BookCaseItem extends TouchableDrawableItem{
	private Context mContext;
	private String TAG = "BookCaseItem";  
	private int cellX;
	private int cellY;
	private float downX;
	private float downY;
	public static boolean isDrag;
	private boolean isDown;
	
	BookCaseItem(Context context) {
		super(context); 
	}
	BookCaseItem(Context context,int x,int y ,int width,int height,int id1,int id2) {
		super(context,x,y,width,height,id1,id2); 
	}
	public BookCaseItem(Context context,int x,int y ,int width,int height,int id) { 
		super(context,x,y,width,height,id);  
		mContext = context;
 
		setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(TouchableDrawableItem item, MotionEvent event) {
				Log.d(TAG, "BookCaseItem  onClick X = " + event.getX() + ";y=" + event.getY());   
				if(!SceneSurfaceView2.isTranslateAnim && isInRegion(event.getX(), event.getY())){
					cellX = (int) ((event.getX() - BookCaseModel.mShortAxisStartPadding)/BookCaseModel.mCellWidth);
					cellY = (int) ((event.getY() - BookCaseModel.mLongAxisStartPadding)/BookCaseModel.mCellHeight);
					Log.d(TAG, "cellX=" + cellX + ";cellY=" + cellY);  
					ApplicationInfo info = BookCaseModel.mBookmap.get(cellX*10 + cellY);
					if(info != null){
						mHandler.removeCallbacks(checkForLongClickEvent);
						Intent intent = info.intent;
						startActivitySafely(intent);
					}
				} 
				
			}
			 
		}); 
		
		setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(TouchableDrawableItem v, MotionEvent event) { 
				if(isInRegion(event.getX(), event.getY())||(SceneSurfaceView2.isTranslateAnim)&& SceneSurfaceView2.instance.mCurScreen == 1){
					final int action = MotionEventCompat.getActionMasked(event);
//					Log.d(TAG, "BookCaseItem  onTouch action =" + action);
					switch(action){
					case MotionEvent.ACTION_DOWN:
						downX = event.getX(); 
						downY = event.getY(); 
						cellX = (int) ((downX - BookCaseModel.mShortAxisStartPadding)/BookCaseModel.mCellWidth);
						cellY = (int) ((downY - BookCaseModel.mLongAxisStartPadding)/BookCaseModel.mCellHeight);
						isDown = true;
						isLongClick = false;
						isDrag = false;
						if(isInRegion(event.getX(), event.getY())){ 
							mHandler.removeCallbacks(checkForLongClickEvent);
							mHandler.postDelayed(checkForLongClickEvent,500);
							MainActivity.instance.mBookModel.mDragInfo = BookCaseModel.mBookmap.get(cellX*10 + cellY);
							 
							Log.d(TAG, "BookCaseItem  ACTION_DOWN cellX =" + cellX + ";cellY=" + cellY);
							boolean isDeleteClick = false;
							if(cellX*BookCaseModel.mCellWidth + BookCaseModel.mShortAxisStartPadding - 5 < downX && downX < cellX*BookCaseModel.mCellWidth + BookCaseModel.mShortAxisStartPadding + 40
									&& cellY*BookCaseModel.mCellHeight + BookCaseModel.mLongAxisStartPadding - 5 < downY
									&& cellY*BookCaseModel.mCellHeight + BookCaseModel.mLongAxisStartPadding + 40 > downY){
								isDeleteClick = true;
							}
							if(SceneSurfaceView2.isTranslateAnim && isDeleteClick){
								List<ApplicationInfo> infos = new ArrayList<ApplicationInfo>();
								ApplicationInfo bookInfo = BookCaseModel.mBookmap.get(cellX*10 + cellY);
								if(bookInfo != null){
									infos.add(bookInfo);
									bookInfo.isBookCase = false;
									BookCaseModel.mBookmap.put(cellX*10 + cellY,null); 
									BookCaseModel.mOccupied[cellX][cellY] = false; 
									BookCaseModel.DeleteBookItemByBatch(mContext, infos, LauncherSettings.BookCase.CONTENT_URI);
									if(LauncherModel.mDesktopItems != null){
										String bookIntentString = bookInfo.intent.toUri(0);
										for(ItemInfo info : LauncherModel.mDesktopItems){  
					                		String infoIntentString = ((ApplicationInfo)info).intent.toUri(0);
											if(bookIntentString.equalsIgnoreCase(infoIntentString)){ 
												((ApplicationInfo)info).isBookCase = false;
											}
										}
									}
									int count = MainActivity.instance.mBookModel.getBookCounts();
									if(count == 0){
										SceneSurfaceView2.isTranslateAnim = false;
									}
									LauncherModel.updateBookcaseInDatabase(mContext, infos); 
								}
								
							} 
							if(BookCaseModel.mBookmap.get(cellX*10+cellY) == null){
								isDown = false;
							}
						} 
						
						break;
					case MotionEvent.ACTION_MOVE:
						final float moveX = event.getX(); 
						final float moveY = event.getY(); 
						cellX = (int) ((moveX - BookCaseModel.mShortAxisStartPadding)/BookCaseModel.mCellWidth);
						cellY = (int) ((moveY - BookCaseModel.mLongAxisStartPadding)/BookCaseModel.mCellHeight);
 
//						Log.d(TAG, "ACTION_MOVE isLongClick=" + isLongClick + ";isDown=" + isDown);
						if(isMoved(moveX, moveY, downX, downY) && isDown){
							isDown = false;
						}
						
						
						if(isDown && isLongClick){
							if(!SceneSurfaceView2.isTranslateAnim){//LongClick
								SceneSurfaceView2.isTranslateAnim = true;
								SceneSurfaceView2.instance.startTranslateThread();
								SceneSurfaceView2.instance.startRefrashWidget();
							}
							isDrag = true;  
						}
//						Log.d(TAG, "BookCaseItem  onTouch  isDrag " + isDrag);
						if(isDrag){  
							MainActivity.instance.mBookModel.onDragOver((int)moveX, (int)moveY, 
									MainActivity.instance.mBookModel.mDragInfo);
						}
						break;	
					
					case MotionEvent.ACTION_UP:
						
						isDown = false;
						mHandler.removeCallbacks(checkForLongClickEvent);
						final float upX = event.getX(); 
						final float upY = event.getY(); 
						if(isDrag){ 
							Log.d(TAG, "BookCaseItem  onTouch  drag upX=" + upX + ";upY=" + upY);
							isDrag = false;
							MainActivity.instance.mBookModel.onDragOver((int)upX, (int)upY, 
									MainActivity.instance.mBookModel.mDragInfo);
						}
						
						break;
					case MotionEvent.ACTION_CANCEL:	
						isDrag = false;
						break;
					}
					return false;
				} 
				return false;
			}
		}); 
	}
	private Handler mHandler=new Handler();
	private boolean isLongClick;
	private Runnable checkForLongClickEvent = new Runnable() {
		
		@Override
		public void run() {  
			Log.d(TAG, "checkForLongClickEvent");
			isLongClick = true; 		 
		}
	};
	
	private boolean isMoved(float x, float y, float x2, float y2) {
		float flag = (x2 - x) * (x2 - x) + (y2 - y) * (y2 - y);
		flag = (float) Math.sqrt(flag);
		Log.e("test", "flag= "+flag);
		if (flag < 8)
			return false;
		else {
			return true;
		}
	}
	
	@Override
	public boolean drawItem(Canvas canvas, int spanX, boolean drawed) { 
		return super.drawItem(canvas, spanX, drawed);
	}
	 
	
	void startActivitySafely(Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
        	mContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(mContext, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(mContext, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Launcher does not have the permission to launch " + intent +
                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                    "or use the exported attribute for this activity.", e);
        }
    }

}
