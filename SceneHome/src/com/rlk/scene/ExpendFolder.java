package com.rlk.scene;
 

import com.rlk.scene.R; 
import android.content.Context; 
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView; 

public class ExpendFolder extends ViewGroup {
	private static boolean DEBUG = true;
	private static String TAG = "ExpendFolder";
	private static String TAG_MEASURE = "IphoneCellLayout";
	
	private ImageView mTopCurrow;
	private ImageView mBottomCurrow;
	private FolderLinearLayout mBody;
	
	public FolderLinearLayout getBody() {
		return mBody;
	}

	private int mTopCurrowWidth;
	private int mTopCurrowHeight;
	private int mBottomCurrowWidth;
	private int mBottomCurrowHeight;
	private int mBodyWidth;
	private int mBodyHeight;
	private int mCurrowContractSize;
	private int mTopCurrowLeft;
	private int mBottomCurrowLeft;
	
	private LayoutInflater inflater;

	public ExpendFolder(Context context) {
		this(context,null);
	}

	public ExpendFolder(Context context, AttributeSet attrs) {
		super(context, attrs);
		setupView(context);
	}

	private void setupView(Context context) {
		mCurrowContractSize = (int)context.getResources().getDimension(R.dimen.folder_currow_contract_size);
		
		inflater = Launcher.getInstance().getLayoutInflater();
		mBody = (FolderLinearLayout)inflater.inflate(R.layout.iphone_folder_expend, null);
		LayoutParams bodyParams = new LayoutParams(LauncherValues.mScreenWidth, LayoutParams.WRAP_CONTENT);
		addView(mBody,bodyParams);
		
		mTopCurrow = new ImageView(context);
		mTopCurrow.setBackgroundResource(R.drawable.iphone_folder_top_corrow);
		mTopCurrow.setVisibility(GONE);
		mTopCurrow.setId(R.id.top_arrow);
		LayoutParams topParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		addView(mTopCurrow, topParams);
		
		mBottomCurrow = new ImageView(context);
		mBottomCurrow.setBackgroundResource(R.drawable.iphone_folder_bottom_corrow);
		mBottomCurrow.setVisibility(GONE);
		mBottomCurrow.setId(R.id.bottom_arrow);
		LayoutParams bottomParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		addView(mBottomCurrow, bottomParams);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		if(DEBUG) Log.d("FolderLinearLayout", "onLayout ExpendFolder onLayout"); 
		
		int bodyTop = mTopCurrowHeight - mCurrowContractSize;
		int bottomCurrowTop = mBodyHeight - mCurrowContractSize;
		if(mTopCurrow.getVisibility() == VISIBLE && mBottomCurrow.getVisibility() != VISIBLE){
			
			mBody.layout(0, bodyTop, mBodyWidth, bodyTop+mBodyHeight);
			
			mTopCurrow.layout(mTopCurrowLeft, 0, mTopCurrowLeft+mTopCurrowWidth, mTopCurrowHeight);
			
		}else if(mBottomCurrow.getVisibility() == VISIBLE && mTopCurrow.getVisibility() != VISIBLE){
			
			mBody.layout(0, 0, mBodyWidth, mBodyHeight);
			
			mBottomCurrow.layout(mBottomCurrowLeft, bottomCurrowTop
					, mBottomCurrowLeft+mBottomCurrowWidth, bottomCurrowTop+mBottomCurrowHeight);
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if(DEBUG) Log.d(TAG_MEASURE, "expendFolder measuer");
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int widthMeasure = MeasureSpec.getSize(widthMeasureSpec);
		
		if(mTopCurrow.getVisibility() != GONE){
			mTopCurrow.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		}
		if(mBottomCurrow.getVisibility() != GONE){
			mBottomCurrow.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		}
		mBody.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		
		mTopCurrowWidth = mTopCurrow.getMeasuredWidth();
		mTopCurrowHeight = mTopCurrow.getMeasuredHeight();
		mBottomCurrowWidth = mBottomCurrow.getMeasuredWidth();
		mBottomCurrowHeight = mBottomCurrow.getMeasuredHeight();
		mBodyHeight = mBody.getMeasuredHeight();
		mBodyWidth = mBody.getMeasuredWidth();
		
		int heightMeasure = mTopCurrowHeight + mBodyHeight + mBottomCurrowHeight - mCurrowContractSize;
		
		if(DEBUG) Log.d(TAG, "widthMeasure " + widthMeasure);
		if(DEBUG) Log.d(TAG, "heightMeasure " + heightMeasure);
		if(DEBUG) Log.d(TAG, "mTopCurrowHeight " + mTopCurrowHeight);
		if(DEBUG) Log.d(TAG, "mBottomCurrowHeight " + mBottomCurrowHeight);
		if(DEBUG) Log.d(TAG, "mBodyHeight " + mBodyHeight);
		if(DEBUG) Log.d(TAG, "mBodyWidth " + mBodyWidth);

		setMeasuredDimension(LauncherValues.mScreenWidth, heightMeasure);
	}

	public void setTopCurrowLeft(int topCurrowLeft) {
		mTopCurrowLeft = topCurrowLeft;
	}
	public void setBottomCurrowLeft(int bottomCurrowLeft) {
		mBottomCurrowLeft = bottomCurrowLeft;
	}

	public int getCurrowHeight() {
		return mTopCurrowHeight;
	}
}
