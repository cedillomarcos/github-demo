
package com.all.weather;

import com.all.weather.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class CitySetting_Center extends Activity {

    private ProgressDialog progressDialog;

    ListView city_listview1;

    Cursor mCursor = null;

    ImageView search;

    EditText city_edittext;

    RecordAdapter ada;

    CityCenterAdapter mDbHelper;

    View mCurrentfocuseditemview = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("xia", " --- CitySetting_center --- ");
        setContentView(R.layout.weather_city_setting);
        SettingActivity.mIsGetdataSuccess = false;
        city_edittext = (EditText) findViewById(R.id.city_edittext);
        city_edittext.setHint(getResources().getString(R.string.c_hint));
        city_edittext.setText(SettingsManager.getInstance().getLocationCity_w(this).substring(0, 1));
        Editable etext = city_edittext.getText();
        int position = etext.length();
        city_edittext.setSelection(position);
        city_listview1 = (ListView) findViewById(R.id.city_listview1);
        search = (ImageView) findViewById(R.id.search);
        search.setClickable(false);
        mDbHelper = new CityCenterAdapter(this);
        mDbHelper.open();        
        initListView();
        addlisteners();


    }

    @Override
    protected void onResume() {
        Log.i("xia", " --- onResume() --- ");
        super.onResume();
    }

    private void initListView() {
        Log.i("xia", " --- initListView() --- ");
        String temp = city_edittext.getText().toString();
        if (temp.contains("\n")){
        	temp = temp.replace("\n", "");
        	city_edittext.setText(temp);
        	city_edittext.setSelection(temp.length());
        	
        }      
        if (temp.trim().length() == 0)
            return;

//        this.getProgressDialog().show();
        try {
            if (mCursor != null)
                mCursor.close();
            mCursor = mDbHelper.query(City.CityColumns.CITYNAME + " like ? ", new String[] {
               "%"+ temp.trim() + "%"
            });
            Log.i("xia", " --- mCursor() --- "+mCursor.getCount());
            ada = new RecordAdapter(this, mCursor);
            city_listview1.setAdapter(ada);
        } catch (SQLException e) {
            e.printStackTrace();
        }
//        this.getProgressDialog().cancel();
    }

    @Override
    protected void onDestroy() {
        if (mCursor != null)
            mCursor.close();
        mDbHelper.closeclose();
        super.onDestroy();
    }

    private void addlisteners() {
        // city_listview1.setOnItemLongClickListener(new
        // OnItemLongClickListener() {
        //
        // public boolean onItemLongClick(AdapterView<?> parent, View view, int
        // position, long id) {
        // if (position == 0)
        // return true;
        // String[] str = new String[] {
        // CitySetting_center.this.getResources().getString(R.string.delete_warning),
        // CitySetting_center.this.getResources().getString(R.string.delete_message),
        // CitySetting_center.this.getResources().getString(R.string.add_ok),
        // CitySetting_center.this.getResources().getString(R.string.add_cancel)
        // };
        // final int pos = position;
        // final TextView name = (TextView) view.findViewById(R.id.text1);
        // String deletecity = name.getText().toString();
        // new AlertDialog.Builder(CitySetting_center.this).setTitle(str[0])
        // .setMessage(str[1] + deletecity + " ?")
        // .setPositiveButton(str[2], new DialogInterface.OnClickListener() {
        // public void onClick(DialogInterface dialog, int which) {
        // // TODO Auto-generated method stub
        // mCursor.moveToPosition(pos - 1);
        // String index = mCursor.getString(0);
        // mDbHelper.deleteCity(index);
        // // flush listview
        // initListView();
        // }
        // }).setNegativeButton(str[3], new DialogInterface.OnClickListener() {
        //
        // public void onClick(DialogInterface dialog, int which) {
        // }
        // }).show();
        // return true;
        // }
        // });

        city_listview1.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Log.i("xia"," onItemClick  position     "+position);
                if (position == 0)
                    return;
                mCursor.moveToPosition(position - 1);
                SettingsManager.getInstance().setLocationZipCode(mCursor.getString(1),
                        CitySetting_Center.this, false);
                String cityname = mCursor.getString(2);
    			if(cityname.contains(".")&&cityname.length()>7){
    				cityname = cityname.replace(".","=").split("=")[1];
 			    }
                SettingsManager.getInstance().setLocationCity(cityname,
                        CitySetting_Center.this, false);
                SettingsManager.getInstance().setLocationArea(mCursor.getString(3),
                        CitySetting_Center.this, false);
                setResult(RESULT_OK, new Intent());
                finish();
            }
        });
        // search.setOnClickListener(SEARCHLISTENER);
        city_edittext.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                initListView();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
    }

    @Override
    protected void onStop() {
        setResult(RESULT_OK, new Intent());
        super.onStop();
    }

    OnClickListener ADDCITYLISTENER = new OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(CitySetting_Center.this, AddcityActivity.class);
            intent.putExtra("isadd", true);
            CitySetting_Center.this.startActivity(intent);
        }
    };

    private ProgressDialog getProgressDialog() {
        progressDialog = null;
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.progress_search_city));
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);
        return progressDialog;
    }

    private class RecordAdapter extends BaseAdapter {
        private Context context;

        private Cursor mCursor;

        public RecordAdapter(Context context, Cursor mCursor) {
            this.context = context;
            this.mCursor = mCursor;
        }

        public int getCount() {
            return mCursor.getCount() + 1;
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
			final View view;
			final LinearLayout layout;
			if (position == 0) {
				view = Inflater.inflate(R.layout.cityadditem, parent, false);
			} else {
				view = Inflater.inflate(R.layout.cityselectitem, parent, false);
				layout = (LinearLayout) view;
			}
			if (position == 0) {
				final TextView name = (TextView) view.findViewById(R.id.text1);
				String t = SettingsManager.getInstance().getLocationArea_w(
						CitySetting_Center.this);
				name.setText(getcname(SettingsManager.getInstance()
						.getLocationCity_w(CitySetting_Center.this))
//						+ "  "	+ replacechinaname(t)
						);
			} else {
				final TextView name = (TextView) view.findViewById(R.id.text1);
				mCursor.moveToPosition(tpostion - 1);
				if (mCursor.getString(2).startsWith(mCursor.getString(3))) {
					name.setText(mCursor.getString(2));
				} else {
					name.setText(mCursor.getString(3) + "  "
							+ mCursor.getString(2));
				}
				// name.setText(getcname(mCursor.getString(3)) + "  "
				// + replacechinaname(mCursor.getString(2)));
			}
			return view;
		}
	}

    String getcname(String city_en) {
        int citystr_id = AccuIconMapper.getCityName(city_en);
        if (citystr_id == 0)
            return city_en;
        else
            return this.getResources().getString(citystr_id);
    }

    String replacechinaname(String cityandcuntory) {
        if (cityandcuntory.contains("China")) {
            int po = cityandcuntory.indexOf("(");
            if (po == -1)
                return this.getResources().getString(R.string.china);
            else {
                String tem = cityandcuntory.substring(po + 1, cityandcuntory.length() - 1);
                int citystr_id = AccuIconMapper.getCityName(tem);
                if (citystr_id == 0)
                    return this.getResources().getString(R.string.china) + "(" + tem + ")";
                else
                    return this.getResources().getString(R.string.china) + "("
                            + this.getResources().getString(citystr_id) + ")";
            }
        } else
            return cityandcuntory;
    }
}
