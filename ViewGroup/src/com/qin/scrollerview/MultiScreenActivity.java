package com.qin.scrollerview;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


//���п����л�����Activity
public class MultiScreenActivity extends Activity implements OnClickListener {

	private Button bt_scrollLeft;
	private Button bt_scrollRight;
	private MultiViewGroup mulTiViewGroup  ;
	
	public static int screenWidth  ;  // ��Ļ���
	public static int scrrenHeight ;  //��Ļ�߶�
	
	private int curscreen = 0;   // ��ǰλ�ڵڼ���Ļ  ����3��"��Ļ"�� 3��LinearLayout
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        //�����Ļ�ֱ��ʴ�С
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		screenWidth = metric.widthPixels ;
		scrrenHeight = metric.heightPixels;		
		System.out.println("screenWidth * scrrenHeight --->" + screenWidth + " * " +scrrenHeight);
		
		setContentView(R.layout.multiview);
 
        //��ȡ�Զ�����ͼ�Ŀռ�����
		mulTiViewGroup = (MultiViewGroup)findViewById(R.id.mymultiViewGroup);
		
		bt_scrollLeft = (Button) findViewById(R.id.bt_scrollLeft);
		bt_scrollRight = (Button) findViewById(R.id.bt_scrollRight);

		bt_scrollLeft.setOnClickListener(this);
		bt_scrollRight.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		switch (v.getId()) {
		case R.id.bt_scrollLeft:
			if(curscreen > 0) {  //��ֹ��ĻԽ��
			    curscreen -- ;
			    Toast.makeText(MultiScreenActivity.this, "��" +(curscreen+1) + "��", 300).show();
			}
			else
				Toast.makeText(MultiScreenActivity.this, "��ǰ���ǵ�һ��",300).show();
			//mulTiViewGroup.scrollTo(curscreen * screenWidth , 0);
			int a = mulTiViewGroup.getScrollX();
			int b = mulTiViewGroup.getScrollY();
			//x>0��ʾ��ͼ(View��ViewGroup)�����ݴ������󻬶�;��֮���������һ���
			//y>0��ʾ��ͼ(View��ViewGroup)�����ݴ������ϻ���;��֮���������»���
			mulTiViewGroup.scrollBy(-curscreen * screenWidth, 0);
			break;
		case R.id.bt_scrollRight:
			if (curscreen < 2 ){ //��ֹ��ĻԽ��
				curscreen ++ ;
				Toast.makeText(MultiScreenActivity.this, "��" + (curscreen+1) + "��", 300).show();
			}
			else
				Toast.makeText(MultiScreenActivity.this, "��ǰ�������һ��",300).show();
			mulTiViewGroup.scrollTo(curscreen * screenWidth, 0);
			
			break;
		}
	}

}
