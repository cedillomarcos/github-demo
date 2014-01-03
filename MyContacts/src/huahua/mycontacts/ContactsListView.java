package huahua.mycontacts;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ListView;

/*
 * 自定义联系人的ListView
 */

public class ContactsListView extends ListView {
	//选中的是哪个联系人的索引
	int m_chooseposition;
	public ContactsListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
    	if(ev.getAction() == MotionEvent.ACTION_DOWN)
    	{
    		//记录按下的是哪个联系人
 		   int x = (int) ev.getX();
 		   int y = (int) ev.getY();
 		  m_chooseposition = pointToPosition(x, y);
 		   Log.v("huahua", "SelectedItemId1111 = " + m_chooseposition);
    	}
    	else if(ev.getAction() == MotionEvent.ACTION_MOVE)
    	{
    		
    	}
    	else if(ev.getAction() == MotionEvent.ACTION_UP)
    	{
    		
    	}

		return super.dispatchTouchEvent(ev);
	}

}
