package com.example.bubble;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.util.BubbleUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyappListView extends ListActivity {
	
	private List<Map<String,Object>> mData;
	private Map<String,Object> mMap ;
	private PackageManager pManager ;
	private Switch actionBarSwitch ;
	int mPostion;
	BubbleUtils mBubbleUtils;
	public final class ViewHolder{
		public ImageView icon;
		public TextView name;
		public CheckBox check_box;
	}
	

	
	public class Myadapter extends BaseAdapter{
		private LayoutInflater mInflater;
		public Myadapter(Context context) {
			// TODO Auto-generated constructor stub
			this.mInflater = LayoutInflater.from(context);
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mData.size();
		}
		
		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			Log.i("renxinquan", "getView" );
			ViewHolder holder = null;
			if(convertView == null){
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.applist_layout, null);
				holder.icon = (ImageView)convertView.findViewById(R.id.app_icon);
				holder.name = (TextView)convertView.findViewById(R.id.app_name);
				holder.check_box = (CheckBox)convertView.findViewById(R.id.checkBox1);
				convertView.setTag(holder);
			}else{
				holder = (ViewHolder)convertView.getTag();
			}
			
			holder.icon.setImageDrawable((Drawable)mData.get(position).get("icon"));
			holder.name.setText((String)mData.get(position).get("name"));
			boolean is_checked =mData.get(position).get("is_checked").equals("false")? false : true ;
			holder.check_box.setChecked((is_checked));

			Log.i("renxinquan", "onListItemClick aaa: " + is_checked);
			return convertView;
		}
	}	
	
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		ViewHolder holder = null;
		mMap = mData.get(position);
		Log.i("renxinquan", "onListItemClick : " + position);
	    CheckBox cbx = (CheckBox) v.findViewById(R.id.checkBox1); 
	    holder = (ViewHolder)v.getTag();
	    if (holder.check_box != null) {  
	    	holder.check_box.toggle();
	    	mBubbleUtils.setList((String)mMap.get("package_name"),holder.check_box.isChecked());
	            holder.check_box.setChecked(holder.check_box.isChecked());
	            mMap.put("is_checked", holder.check_box.isChecked() ?  "true" : "false");
	    }
	 
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mBubbleUtils = new BubbleUtils(this);

		actionBarSwitch = new Switch(this);
		
        actionBarSwitch.setPadding(0, 0,20, 0);
        getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
               ActionBar.DISPLAY_SHOW_CUSTOM);
        getActionBar().setCustomView(actionBarSwitch, new ActionBar.LayoutParams(
               ActionBar.LayoutParams.WRAP_CONTENT,
               ActionBar.LayoutParams.WRAP_CONTENT,
               Gravity.CENTER_VERTICAL | Gravity.END));
        actionBarSwitch.setChecked(mBubbleUtils.getState());
        actionBarSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				mBubbleUtils.setState(isChecked);
			}
		});
		

		pManager = getPackageManager();
		mData = getData();
		Myadapter myAdapter = new Myadapter(this);
		setListAdapter(myAdapter);
	}
	
	private List<Map<String,Object>> getData(){
		final Intent mainIntent = new Intent(Intent.ACTION_MAIN,null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		final List<ResolveInfo> infoList = pManager.queryIntentActivities(mainIntent, 0);
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		
		for(ResolveInfo info : infoList){
			Map<String, Object> map = new HashMap<String, Object>();
			CharSequence appName = info.loadLabel(pManager); 
		    if(appName == null){  
		        appName = info.activityInfo.name;  
		    }  
			map.put("name",appName.toString());
			Drawable draw = info.activityInfo.loadIcon(pManager);
			map.put("icon",draw);
			map.put("is_checked",mBubbleUtils.is_contain_inList(info.activityInfo.name)? "true" : "false");
			map.put("package_name",info.activityInfo.name);
			list.add(map);
		}
		return list;
	}
	
}
