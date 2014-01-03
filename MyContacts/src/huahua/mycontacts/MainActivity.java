package huahua.mycontacts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.app.Activity;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	//联系人包含的信息
	public static class Persons {
		public String Name;  //姓名
		public String PY;      //姓名拼音 (花花大神:huahuadashen)
		public String Number;      //电话号码
		public String FisrtSpell;      //中文名首字母 (花花大神:hhds)
	} 
	//字母列视图View
	private AlphabetScrollBar m_asb;
	//显示选中的字母
	private TextView m_letterNotice;
	//联系人的列表
	private ContactsListView m_contactslist;
	//联系人列表的适配器
	private ListAdapter m_listadapter;
	//所有联系人数组
	private ArrayList<Persons> persons = new ArrayList<Persons>();
	//搜索过滤联系人EditText
	private EditText m_FilterEditText;
	//没有匹配联系人时显示的TextView
	private TextView m_listEmptyText;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//获取手机中的联系人,并将所有联系人保存perosns数组中
		//联系人比较多的话,初始化中会比较耗时,以后再优化
		getContacts();
		//得到字母列的对象,并设置触摸响应监听器
		m_asb = (AlphabetScrollBar)findViewById(R.id.alphabetscrollbar);
		m_asb.setOnTouchBarListener(new ScrollBarListener());
		m_letterNotice = (TextView)findViewById(R.id.pb_letter_notice);
		m_asb.setTextView(m_letterNotice);
		
		// 根据拼音为联系人数组进行排序
		Collections.sort(persons, new ComparatorPY());
		
		//得到联系人列表,并设置适配器
		m_contactslist = (ContactsListView)findViewById(R.id.pb_listvew);
		m_listadapter = new ListAdapter(this, persons);
		m_contactslist.setAdapter(m_listadapter);
		m_contactslist.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Log.i("huahua",""+arg2);
				
			}
		});
		
		m_listEmptyText = (TextView)findViewById(R.id.pb_nocontacts_notice);
		
    	//初始化搜索编辑框,设置文本改变时的监听器
		m_FilterEditText = (EditText)findViewById(R.id.pb_search_edit);
		m_FilterEditText.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
					
				if(!"".equals(s.toString().trim()))
				{  
					//根据编辑框值过滤联系人并更新联系列表
					filterContacts(s.toString().trim());
					m_asb.setVisibility(View.GONE);
				}
				else
				{
					m_asb.setVisibility(View.VISIBLE);
					m_listadapter.updateListView(persons);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
		});
	}
	
	public class ComparatorPY implements Comparator<Persons>{

		@Override
		public int compare(Persons lhs, Persons rhs) {
			String str1 = lhs.PY;
			String str2 = rhs.PY;
			return str1.compareToIgnoreCase(str2);
		}
	}
	
	public class ListAdapter extends BaseAdapter{
		private LayoutInflater m_inflater;
		private ArrayList<Persons> Persons;
    	private Context context;
    	
    	//将要添加到Viewpager中的3个View
		private View view1;
		private View view2;
		private View view3;
		private List<View> views;
		
        public ListAdapter(Context context,
        		ArrayList<Persons> persons) {
    	    this.m_inflater = LayoutInflater.from(context);
    	    this.Persons = persons;
    	    this.context = context;
        }
        
    	//当联系人列表数据发生变化时,用此方法来更新列表
    	public void updateListView(ArrayList<Persons> persons){
    		this.Persons = persons;
    		notifyDataSetChanged();
    	}

		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return Persons.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return Persons.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null)
			{
				convertView = m_inflater.inflate(R.layout.vp_item, null);
			}
			
			views = new ArrayList<View>();
			
			view1 = LayoutInflater.from(context).inflate(R.layout.fragment1, null);
			view2 = LayoutInflater.from(context).inflate(R.layout.list_item, null);
			view3 = LayoutInflater.from(context).inflate(R.layout.fragment3, null);
			views.add(view1);
			views.add(view2);
			views.add(view3);
            
            TextView name = (TextView) view2.findViewById(R.id.contacts_name);
            name.setText(Persons.get(position).Name);
    	    
            TextView number = (TextView) view2.findViewById(R.id.contacts_number);
            number.setText(Persons.get(position).Number);
            
            //每个listview的item都是一个Viewpager
            ItemsViewPager vp = (ItemsViewPager) convertView
				.findViewById(R.id.tabcontent_vp);
            //给viewpager设置适配器
			vp.setAdapter(new TabAdapter());
			//给viewpager设置滑动页监听器
			vp.setOnPageChangeListener(new ViewPager.OnPageChangeListener()
			{
				@Override
				public void onPageSelected(int position)
				{
					if(position == 0)//打电话
					{
						//震动一下
						Vibrator vib = (Vibrator)getSystemService(Service.VIBRATOR_SERVICE);
						vib.vibrate(50);
						
						Intent  intent = new Intent(Intent.ACTION_CALL,Uri.parse("tel://" + Persons.get(m_contactslist.m_chooseposition).Number));
						startActivity(intent);
					}
					else if(position == 2)//发短信
					{
						Vibrator vib = (Vibrator)getSystemService(Service.VIBRATOR_SERVICE);
						vib.vibrate(50);
						
						Intent intent = new Intent(Intent.ACTION_SENDTO,Uri.parse("smsto://" + Persons.get(m_contactslist.m_chooseposition).Number));
							startActivity(intent);
					}
				}
				
				@Override
				public void onPageScrollStateChanged(int arg0) {
					
				}

				@Override
				public void onPageScrolled(int arg0, float arg1, int arg2) {
					
				}
			});
			//第一次加载都是设置的显示第2个页面
			vp.setCurrentItem(1);
			
			//字母提示textview的显示 
			TextView letterTag = (TextView)convertView.findViewById(R.id.pb_item_LetterTag);
			//获得当前姓名的拼音首字母
			String firstLetter = Persons.get(position).PY.substring(0,1).toUpperCase();
			
			//如果是第1个联系人 那么letterTag始终要显示
			if(position == 0)
			{
				letterTag.setVisibility(View.VISIBLE);
				letterTag.setText(firstLetter);
			}			
			else
			{
				//获得上一个姓名的拼音首字母
				String firstLetterPre = Persons.get(position-1).PY.substring(0,1).toUpperCase();
				//比较一下两者是否相同
				if(firstLetter.equals(firstLetterPre))
				{
					letterTag.setVisibility(View.GONE);
				}
				else 
				{
					letterTag.setVisibility(View.VISIBLE);
					letterTag.setText(firstLetter);
				}
			}
			
			return convertView;
		}
		
		private class TabAdapter extends PagerAdapter
		{
			@Override
			public int getCount()
			{
				return views.size();
			}
			
			@Override
			public Object instantiateItem(View arg0, int arg1)
			{
	            ViewGroup p = (ViewGroup) views.get(arg1).getParent();  
	            if (p != null) {  
	                p.removeView(views.get(arg1));
	            }  
				
				((ViewPager) arg0).addView(views.get(arg1));
				
				return views.get(arg1);
			}
			
			@Override
			public void destroyItem(View arg0, int arg1, Object arg2)
			{
				
				((ViewPager) arg0).removeView(views.get(arg1));
			}
			
			@Override
			public boolean isViewFromObject(View arg0, Object arg1)
			{
				return arg0 == arg1;
			}
			
			@Override
			public void restoreState(Parcelable arg0, ClassLoader arg1)
			{
			}
			
			@Override
			public Parcelable saveState()
			{
				return null;
			}
			
			@Override
			public void startUpdate(View arg0)
			{
			}
			
			@Override
			public void finishUpdate(View arg0)
			{
			}
		}
		
	}
	
	//字母列触摸的监听器
	private class ScrollBarListener implements AlphabetScrollBar.OnTouchBarListener {

		@Override
		public void onTouch(String letter) {
			
			//触摸字母列时,将联系人列表更新到首字母出现的位置
	        for (int i = 0;   i < persons.size(); i++) {  
	            if (persons.get(i).PY.substring(0, 1).compareToIgnoreCase(letter) == 0) { 
	            	m_contactslist.setSelection(i);
	            	break;
	            }  
	        } 
		}
	}
	
    public void getContacts() {
            ContentResolver contentResolver = getContentResolver();
            // 获得所有联系人数据集的游标
            Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI ,null, null, null, null);
            // 循环遍历
            if (cursor.moveToFirst()) {
                    
                    int idColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID);
                    int displayNameColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                    int NumberColumn = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

                    while (cursor.moveToNext()){
                    		Persons person = new Persons();
                            // 获得联系人的ID号
                            String contactId = cursor.getString(idColumn);

                            // 获得联系人姓名
                            person.Name = cursor.getString(displayNameColumn);
                            person.PY = PinyinUtils.getPingYin(person.Name);
                            person.FisrtSpell = PinyinUtils.getFirstSpell(person.Name);
                            person.Number = cursor.getString(NumberColumn);
//                            Log.v("huahua", "名字:"+person.Name + "号码:"+person.Number + "姓名首字母:"+person.FisrtSpell );

                            persons.add(person);
                    }
                    cursor.close();
            }
    }
    
	private void filterContacts(String filterStr){
		ArrayList<Persons> filterpersons = new ArrayList<Persons>();
		
        //遍历所有联系人数组,筛选出包含关键字的联系人
        for (int i = 0; i < persons.size(); i++) {  
            //过滤的条件
              if (isStrInString(persons.get(i).Number,filterStr)
            		||isStrInString(persons.get(i).PY,filterStr)
            		||persons.get(i).Name.contains(filterStr)
            		||isStrInString(persons.get(i).FisrtSpell,filterStr)){
                //将筛选出来的联系人重新添加到filterpersons数组中
            	Persons filterperson = new Persons();
            	filterperson.Name = persons.get(i).Name;
            	filterperson.PY = persons.get(i).PY;
            	filterperson.Number = persons.get(i).Number;
            	filterperson.FisrtSpell = persons.get(i).FisrtSpell;
            	filterpersons.add(filterperson);
            }  
        }  
        
        //如果没有匹配的联系人
		if(filterpersons.isEmpty())
		{
			m_contactslist.setEmptyView(m_listEmptyText);
		}
        
        //将列表更新为过滤的联系人
        m_listadapter.updateListView(filterpersons);
	}
	
	public boolean isStrInString(String bigStr,String smallStr){
		  if(bigStr.toUpperCase().indexOf(smallStr.toUpperCase())>-1){
			  return true;
		  }else{
			  return false;
		  }
	}

}
