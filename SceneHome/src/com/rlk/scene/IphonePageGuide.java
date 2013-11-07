package com.rlk.scene;

import java.util.ArrayList;
import java.util.List;

import com.rlk.scene.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class IphonePageGuide extends ViewGroup {
	public static boolean DEBUG = false;
	public static final String TAG = "IphonePageGuide";
	
	private int mDianHeight;
	private int mDianWidth;
//	private int mSearchHeight;
//	private int mSearchWidth;
	
	private int mDianPadding;
	private int mCurrentPage = -1;
	private int mNextPage = -1;
	private int mScreenWidth;
	private Workspace mWorkspace;

	private List<ImageView> mDianList;
	
	public IphonePageGuide(Context context) {
		this(context, null);
	}

	public IphonePageGuide(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		
		Resources resources = getContext().getResources();
		mDianHeight = /*(int)resources.getDimension(R.dimen.page_dian_height)*/8;
		mDianWidth = /*(int)resources.getDimension(R.dimen.page_dian_width)*/8;
//		mSearchHeight = (int)resources.getDimension(R.dimen.page_search_height);
//		mSearchWidth = (int)resources.getDimension(R.dimen.page_search_width);
		mDianPadding = (int)resources.getDimension(R.dimen.page_dian_padding);
		
		int pageCount = Launcher.getScreenCount();
		mDianList = new ArrayList<ImageView>();
//		mDianList.add(createGuideImage(R.drawable.iphone_search_dian_gray));
		for (int i = 0; i < pageCount; i++) {
			if(i == Launcher.getScreen()){
				mDianList.add(createGuideImage(R.drawable.iphone_dian_bai));
			}else{
				mDianList.add(createGuideImage(R.drawable.iphone_dian_gray));
			}
		}
		
		for (int i = 0; i < pageCount; i++) {
			addView(mDianList.get(i));
		}
		mCurrentPage = Launcher.getScreen();
		
		mScreenWidth = getContext().getApplicationContext().getResources().getDisplayMetrics().widthPixels;
	}

	private ImageView createGuideImage(int drawableId){
		ImageView image = new ImageView(getContext());
		LayoutParams params = new LayoutParams(mDianWidth, mDianHeight);
		image.setLayoutParams(params);
		image.setImageResource(drawableId);
		return image;
	}
	
	public void flush() {		
		mNextPage = Launcher.getScreen();
//		Log.d("ningyaoyun", "flush() mNextPage = " + mNextPage + "; mCurrentPage =" + mCurrentPage);
		if(mCurrentPage != mNextPage){
//			if(mNextPage == 0){
//				mDianList.get(mNextPage).setImageResource(R.drawable.iphone_search_dian_white);
//				mDianList.get(mCurrentPage).setImageResource(R.drawable.iphone_dian_gray);
//			}else{
//				if(mCurrentPage == 0){
//					mDianList.get(mCurrentPage).setImageResource(R.drawable.iphone_search_dian_gray);
//					mDianList.get(mNextPage).setImageResource(R.drawable.iphone_dian_bai);
//				}else{
					//Modify GWLLSW-735 ningyaoyun 20121018(on)
					try {
						mDianList.get(mCurrentPage).setImageResource(R.drawable.iphone_dian_gray);
						mDianList.get(mNextPage).setImageResource(R.drawable.iphone_dian_bai);
					} catch (Exception e) { 
					}
					//Modify GWLLSW-735 ningyaoyun 20121018(off)				
//				}
//			}
			mCurrentPage = mNextPage;
		}
	}
	
	public void addPage(){
	 	if(DEBUG) Log.d("page", "andpage");
		ImageView imageView = createGuideImage(R.drawable.iphone_dian_gray);
		mDianList.add(imageView);
		addView(imageView);
		requestLayout();
	}
	
	public void deletePage(int deleteCount){
		if(DEBUG) Log.d("page", "deletePage");
		int index = -1;
		ImageView image = null;
		while(deleteCount > 0){
			index = mDianList.size() - 1;
			image = mDianList.get(index);
			removeView(image);
			mDianList.remove(index);
			deleteCount--;
		}
		requestLayout();
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int pageCount = mDianList.size();
		if(pageCount <= 0){
			return;
		}
		int parentWidth = r - l;
		int parentHeight = b - t;
		int start_left = (parentWidth - ((pageCount-1) * mDianPadding + mDianWidth)) / 2;
		int start_top = (parentHeight - mDianHeight) / 2;
		
		int left;
		for (int i = 0; i < pageCount; i++) {
			
			ImageView image = mDianList.get(i);
			left = start_left + i * mDianPadding;
			
			if(i == Launcher.getInstance().getScreen()){
				image.layout(left, start_top, left + 14, start_top + 14);
			}else{
				image.layout(left, start_top+3, left + mDianWidth, start_top + mDianHeight+3);
			}
		}
	}

	public void setCurrentPage(int currentPage) {
		if(currentPage != mCurrentPage){
			mDianList.get(currentPage).setImageResource(R.drawable.iphone_dian_bai);
			if(mCurrentPage <= mDianList.size()){
				mDianList.get(mCurrentPage).setImageResource(R.drawable.iphone_dian_gray);
			}
			mCurrentPage = currentPage;
		}
	}

	public void setWorkspace(Workspace workspace) {
		mWorkspace = workspace;
	}
}
