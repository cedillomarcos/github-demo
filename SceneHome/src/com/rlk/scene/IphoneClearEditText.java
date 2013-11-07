package com.rlk.scene;

import com.rlk.scene.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ImageView;
 

public class IphoneClearEditText extends ViewGroup {
	private final String TAG = "IphoneClearEditText";
	private final boolean DEBUG = false;
	
	private Drawable mbg;	
	private int mEdtTextSize;	
	private int mEdtTextColor;
	private int mEdtTextPaddingLeft;
	private int mEdiTextWidth;
	
	private String mEdtHint;
	
	private EditText mEdt;
	private ImageView mImg;
	private int mMeasuredWidth;
	private int mMeasuredHeight;
	
	public IphoneClearEditText(Context context) {
		super(context);
	}

	public IphoneClearEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context,attrs);
		setupViews(context,attrs);
		DisplayMetrics metrics = new DisplayMetrics();
		((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
		mMeasuredWidth = 457;
		mMeasuredHeight = 39;
	}

	
	private void init(Context context, AttributeSet attrs){
		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.iphoneContactEditText);
		mbg = typedArray.getDrawable(R.styleable.iphoneContactEditText_contactEdtBg);
		mEdtTextSize = typedArray.getDimensionPixelOffset(R.styleable.iphoneContactEditText_contactEdtTextSize, 14);
		mEdtTextColor = typedArray.getColor(R.styleable.iphoneContactEditText_contactEdtTextColor, 0);
		mEdtHint = typedArray.getString(R.styleable.iphoneContactEditText_contactEdtHint);
		mEdtTextPaddingLeft = typedArray.getDimensionPixelOffset(R.styleable.iphoneContactEditText_contactEdtTextpaddingLeft, 0);
		mEdiTextWidth = typedArray.getDimensionPixelOffset(R.styleable.iphoneContactEditText_contactWidth, 0);
		
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
//		super.onLayout(changed, l, t, r, b);
		int mImgWidth;
		if (mImg.getVisibility() == View.GONE ) {
			mImgWidth = 0;
		} else {
			mImgWidth = mImg.getDrawable().getIntrinsicWidth();
		}
		int mImgHeight = mImg.getDrawable().getIntrinsicHeight();
		
//		if (false) {
//			Log.d(TAG, "mImgWidth " + mImgWidth);
//			Log.d(TAG, "mImgHeight " + mImgHeight);
//			Log.d(TAG, "mMeasuredWidth " + mMeasuredWidth);
//			Log.d(TAG, "mMeasuredHeight " + mMeasuredHeight);
//		} 
	    mImg.layout(mMeasuredWidth - 10 - mImgWidth, (mMeasuredHeight - mImgHeight)/2, mMeasuredWidth - 10, (mMeasuredHeight + mImgHeight)/2);	
		mEdt.layout(0, (mMeasuredHeight - mEdt.getMeasuredHeight())/2, mMeasuredWidth - 12 - mImgWidth, (mMeasuredHeight + mEdt.getMeasuredHeight())/2);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mEdt.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		if (false) {
			Log.d(TAG, "mEdt.getMeasuredWidth() " + mEdt.getMeasuredWidth());
		}
		mImg.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
		
	}

	private void setupViews(Context context,AttributeSet attrs) {
		mImg = new ImageView(context);
		LayoutParams lp0 = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);		
//		lp0.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//		lp0.addRule(RelativeLayout.CENTER_VERTICAL);
//		mImg.setPadding(0, 0, 10, 0);
		mImg.setImageResource(R.drawable.iphone_clean_icon);
		mImg.setVisibility(View.GONE);
		mImg.setId(R.id.iphonecontact_clear_edittext);
		addView(mImg,lp0);	
		
		mImg.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {
				mEdt.setText("");
			}
			
		});
		
		mEdt = new EditText(context);
		mEdt.setBackgroundResource(android.R.color.transparent);
		if(mbg!=null){
			setBackgroundDrawable(mbg);
		}
		mEdt.setPadding(mEdtTextPaddingLeft, 0, 0, 0);
		mEdt.setTextSize(mEdtTextSize);
		mEdt.setGravity(Gravity.CENTER_VERTICAL);
		if(mEdtTextColor != 0){
			mEdt.setTextColor(mEdtTextColor);
		}
		mEdt.setHint(mEdtHint);
		mEdt.setSingleLine(true);
		LayoutParams lp1 = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		addView(mEdt, lp1);
		mEdt.addTextChangedListener(new EditTextWatcher());
	}
	
	/***
	 * 添加输入框内容变化监听
	 * @param textwatcher
	 */

	
	public void addTextChangedListener(TextWatcher textwatcher){
		mEdt.addTextChangedListener(textwatcher);
	}

	//判断"清除"按钮是否显示
	public int getImgClearVisible(){
		return mImg.getVisibility();
	}
	
	//返回EditText输入框
	public EditText getIphoneEditText(){
		return mEdt;
	}
	
	//返回删除图标
	public ImageView getCleanImageView(){
		return mImg;
	}
	
	/***
	 * 设置输入框内容
	 */
	public void setText(String str){
		mEdt.setText(str);
	}
	
	//返回字符串
	public String getText(){
		return mEdt.getText().toString();
	}
	
	//设置hint的颜色
	public void setHintTextColor(int color){
		mEdt.setHintTextColor(color);
	}
	
	
	public void setTextSize(float size){
		mEdt.setTextSize(size);
	}
	
	public void setTextColor(int color){
		mEdt.setTextColor(color);
	}
	
	public void setImgPaddingRight(int paddingLeft){
		mImg.setPadding(0, 0, paddingLeft, 0);
	}
	
	public void setHint(String str){
		mEdt.setHint(str);
	}
	
	/***
	 * 改变输入框的背景图片
	 */
	/*public void setBackgroundResource(int resid){
		this.setBackgroundResource(resid);
	}*/

	/***
	 * 添加输入框内容变化监听
	 * @param textwatcher
	 */
	
	private class EditTextWatcher implements TextWatcher{

		public void afterTextChanged(Editable s) {
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			if(s.length() == 0){
				mImg.setVisibility(View.GONE);
			}else{
				mImg.setVisibility(View.VISIBLE);
			}
			
		}
		
	}
}
