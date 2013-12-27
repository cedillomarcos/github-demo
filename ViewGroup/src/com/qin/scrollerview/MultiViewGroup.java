package com.qin.scrollerview;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

//�Զ���ViewGroup �� ����������LinearLayout�ؼ�������ڲ�ͬ�Ĳ���λ�ã�ͨ��scrollBy����scrollTo�����л�
public class MultiViewGroup extends ViewGroup {

	private Context mContext;

	private static String TAG = "MultiViewGroup";

	public MultiViewGroup(Context context) {
		super(context);
		mContext = context;
		init();
	}

	public MultiViewGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
		Log.i(TAG, "MultiViewGroup ��getWidth()=" + getWidth()+","+"getHeight()="+getHeight());
		
		mContext = context;
		init();
	}

	private void init() {
		// ��ʼ��3�� LinearLayout�ؼ�
		LinearLayout oneLL = new LinearLayout(mContext);
		oneLL.setBackgroundColor(Color.RED);
        addView(oneLL);
		
		LinearLayout twoLL = new LinearLayout(mContext);
		twoLL.setBackgroundColor(Color.YELLOW);
		addView(twoLL);
		
		LinearLayout threeLL = new LinearLayout(mContext);
		threeLL.setBackgroundColor(Color.BLUE);
		addView(threeLL);
	}

	// measure����
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		Log.i(TAG, "--- start onMeasure --");

		// ���ø�ViewGroup�Ĵ�С
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		setMeasuredDimension(width, height);

		int childCount = getChildCount();
		Log.i(TAG, "--- onMeasure childCount is -->" + childCount);
		Log.i(TAG, "width=" + width+","+"height="+height);
		Log.i(TAG, "onMeasure ��getWidth()=" + getWidth()+","+"getHeight()="+getHeight());
		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			// ����ÿ������ͼ�Ĵ�С �� ��ȫ��
			child.measure(MultiScreenActivity.screenWidth, MultiScreenActivity.scrrenHeight);
		}
	}

	// layout����
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		Log.i(TAG, "--- start onLayout --");
		int startLeft = 0; // ÿ������ͼ����ʼ��������
		int startTop = 10; // �������Ϊ10px �൱�� android��marginTop= "10px"
		int childCount = getChildCount();
		Log.i(TAG, "--- onLayout childCount is -->" + childCount);
		Log.i(TAG, "onLayout ��getWidth()=" + getWidth()+","+"getHeight()="+getHeight());
		for (int i = 0; i < childCount; i++) {
			View child = getChildAt(i);
			child.layout(startLeft, startTop, 
					startLeft + MultiScreenActivity.screenWidth, 
					startTop + MultiScreenActivity.scrrenHeight);
			startLeft = startLeft + MultiScreenActivity.screenWidth ; //У׼ÿ����View����ʼ����λ��
			//��������ͼ������Ļ�еķֲ����� [0 , 320] / [320,640] / [640,960]
		}
	}

}
