package com.all.weather;

import java.util.List;

import com.all.weather.R;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class AddcityActivity extends Activity {
	private static final int SHOW_CITY_NOT_FOUND_ALERT = 1;
	private static final int SHOW_PROGRESS_DIALOG = 2;
	private static final int SHOW_NOINPUT= 3;
	private List<City> cities;
	private ProgressDialog progressDialog;
	private String cityname;	
	LinearLayout layout;
	AlertDialog malertdialog;
	TextView city_edittext;
	ImageView search;
	TextView  citynametext;
	TextView zipcodetext;
	TextView selectacity;
	ListView city_listview;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.city_add);
       
        boolean f = getIntent().getExtras().getBoolean("isadd");
        city_listview = (ListView)findViewById(R.id.city_listview);
        city_edittext = (EditText)findViewById(R.id.city_edittext);
        selectacity = (TextView)findViewById(R.id.selectacity);
        search = (ImageView)findViewById(R.id.search);    
        layout = (LinearLayout)findViewById(R.id.layout);  
        if(!f){
        	layout.setVisibility(View.GONE);
//        	layout.setBackgroundColor(Color.BLACK);
        	this.cities = CitySetting_Accu.mCities;
        	city_listview.setAdapter(new ARecordAdapter(AddcityActivity.this,cities));
         }
        else{
        	selectacity.setText("input ciytname:");
        	city_edittext.setVisibility(View.VISIBLE);
        	search.setVisibility(View.VISIBLE);
        	 search.setOnClickListener(new OnClickListener(){
 			 public void onClick(View v) {
 				   // TODO Auto-generated method stub
 				   cityname = city_edittext.getText().toString();
 				   if(cityname.equals(""))
 					{
 					      showDialog(SHOW_NOINPUT);					
 					      return;
 					}
 	    			      handleCityLocation(cityname);
 				}
 	        });
         }
       }
	
      Handler viewUpdateHandler = new Handler(){
		 @SuppressWarnings("unchecked")
		 @Override
        public void handleMessage(Message msg) {
        	cities = (List<City>)msg.obj;
			getProgressDialog().cancel();
			if (cities.size() == 0) {
				showDialog(SHOW_CITY_NOT_FOUND_ALERT);
			} else{
				city_listview.setAdapter(new ARecordAdapter(AddcityActivity.this,cities));
			}
          super.handleMessage(msg);
         }
	  }; 
	 
     private void handleCityLocation(final String location) {
		showDialog(SHOW_PROGRESS_DIALOG);
		new Thread() {
			@Override
			public void run() {
	    		Message m = new Message();	
				m.obj = LocationReader.getInstance().getLocations(location);
	          viewUpdateHandler.sendMessage(m); 
			}
		}.start();
	 }

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case SHOW_CITY_NOT_FOUND_ALERT: {
            return new AlertDialog.Builder(this)
            .setIcon(R.drawable.alert_dialog_icon)
            .setTitle(R.string.alert_dialog_city_not_found)
            .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            }).setCancelable(true)
            .create();
		} 
		case SHOW_PROGRESS_DIALOG: {
			return getProgressDialog();
		}
		case SHOW_NOINPUT:{
			return new AlertDialog.Builder(this)
            .setIcon(R.drawable.alert_dialog_icon)
            .setTitle(R.string.alert_dialog_city_noinput)
            .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            }).setCancelable(true)
            .create();			
		}
		
		
		
		default:
			break;
		}
		return null;
	}
	
	private ProgressDialog getProgressDialog() {
		if(progressDialog == null) {
			progressDialog = new ProgressDialog(this);			
			progressDialog.setMessage(getResources().getString(R.string.progress_search_city));
			progressDialog.setIndeterminate(false);
			progressDialog.setCancelable(true);
		}
		return progressDialog;
	}
	
	private class ARecordAdapter extends BaseAdapter  {		
		  private Context context;
		  private List<City> cities;
		  public ARecordAdapter(Context context,List<City> cities) 
		  {
			  this.context= context;
			  this.cities = cities;
			  Log.i("xia","   cities.size()        "+cities.size());
		  }
		  public int getCount() {
		      return cities.size();
		  }
		  public Object getItem(int position) {
		      return position;
		  }
		  public long getItemId(int position) {
		      return position;
		  }
		  public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater Inflater = LayoutInflater.from(context);
			final int tpostion = position;
			final  View view;
			final LinearLayout layout;
			view = Inflater.inflate(R.layout.cityselectitem, parent, false);
			layout = (LinearLayout) view;
			layout.setOnClickListener(new View.OnClickListener() {	
					public void onClick(View v){
						
						City aa = cities.get(tpostion);
					Cursor	Cursortemp = managedQuery(City.CityColumns.CONTENT_URI,null,
			    				   City.CityColumns.ZIPCODE+" =  ? " , new String[] {aa.getLocation()}, null);
						if(Cursortemp.getCount()==0)
						{ 
							ContentValues values = new ContentValues();
						    values.put(City.CityColumns.CITYNAME, aa.getCity());
						    values.put(City.CityColumns.CITYNAME1, aa.getState());
						    values.put(City.CityColumns.ZIPCODE, aa.getLocation());
						    getContentResolver().insert(City.CityColumns.CONTENT_URI, values);
						}
					SettingsManager.getInstance().setLocationZipCode(aa.getLocation(), AddcityActivity.this, false);
					SettingsManager.getInstance().setLocationCity(aa.getCity(), AddcityActivity.this, false);
					SettingsManager.getInstance().setLocationArea(aa.getState(), AddcityActivity.this, false);
					finish();
					}
				});
			final TextView name = (TextView) view.findViewById(R.id.text1);
			City city = cities.get(tpostion);
			name.setText(String.format("%s --- %s", city.getCity(), city.getState()));
			return view;
		  }
		}
}