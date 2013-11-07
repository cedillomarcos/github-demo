
package com.all.weather;

import java.util.List;
import com.all.weather.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class CitySetting_Accu extends Activity {

    private static final int SHOW_CITY_NOT_FOUND_ALERT = 1;

    private static final int SHOW_PROGRESS_DIALOG = 2;

    private static final int SHOW_PROGRESS_DIALOG_O = 12;

    private static final int SHOW_PROGRESS_DIALOG_OO = 22;

    private static final int SHOW_NOINPUT = 3;

    private static final int SHOW_CITIES = 4;

    private int SHOW_CITIES_FLAG = 1;

    private static final int SHOW_CITIES_O = 5;

    private static final int SHOW_CITIES_OO = 6;

    private ProgressDialog progressDialog;

    private ProgressDialog progressDialog_O;

    private ProgressDialog progressDialog_OO;

    public static List<City> mCities;

    ListView city_listview1;

    Cursor mCursor = null;

    ImageView search;

    EditText city_edittext;

    RecordAdapter ada;

    CityAcuuAdapter mDbHelper;

    private boolean isActivited;
    
    View mCurrentfocuseditemview = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        SHOW_CITIES_FLAG = 1;
        setContentView(R.layout.weather_city_setting);
        SettingActivity.mIsGetdataSuccess = false;
        city_edittext = (EditText) findViewById(R.id.city_edittext);
        city_listview1 = (ListView) findViewById(R.id.city_listview1);
        search = (ImageView) findViewById(R.id.search);
        addlisteners();
        mDbHelper = new CityAcuuAdapter(this);
        mDbHelper.open();

    }

    @Override
    protected void onResume() {

        isActivited = true;
        initListView();
        super.onResume();
    }

    private void initListView() {
    	String temp = city_edittext.getText().toString();
        if (temp.contains("\n")){
        	temp = temp.replace("\n", "");
        	city_edittext.setText(temp);
        	city_edittext.setSelection(temp.length());
        }      
//       if (temp.trim().length() == 0)
//            return;
        try {
            if (mCursor != null)
                mCursor.close();
                mCursor = mDbHelper.query(City.CityColumns.CITYNAME + " like ? ", new String[] {
                temp.trim() + "%"
            });
            ada = new RecordAdapter(this, mCursor);
            city_listview1.setAdapter(ada);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
		if(mCursor!=null)
        mCursor.close();
        mDbHelper.closeclose();
        super.onDestroy();
    }
    private void addlisteners() {
        city_listview1.setOnItemLongClickListener(new OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0)
                    return true;
                String[] str = new String[] {
                        CitySetting_Accu.this.getResources().getString(R.string.delete_warning),
                        CitySetting_Accu.this.getResources().getString(R.string.delete_message),
                        CitySetting_Accu.this.getResources().getString(R.string.add_ok),
                        CitySetting_Accu.this.getResources().getString(R.string.add_cancel)
                };
                final int pos = position;
                final TextView name = (TextView) view.findViewById(R.id.text1);
                String deletecity = name.getText().toString();
                new AlertDialog.Builder(CitySetting_Accu.this).setTitle(str[0])
                        .setMessage(str[1] + deletecity + " ?")
                        .setPositiveButton(str[2], new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                mCursor.moveToPosition(pos - 1);
                                String index = mCursor.getString(0);
                                mDbHelper.deleteCity(index);
                                // flush listview
                                initListView();
                            }
                        }).setNegativeButton(str[3], new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();
                return true;
            }

        });

        city_listview1.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Log.i("xia"," onItemClick  position     "+position);
                if (position == 0)
                    return;
                mCursor.moveToPosition(position - 1);
                Log.i("onItemSelected",
                        "--------+++++++++-------- layout.setOnClickListener()----------");
                SettingsManager.getInstance().setLocationZipCode(
                        mCursor.getString(3), CitySetting_Accu.this, false);
                SettingsManager.getInstance().setLocationCity(
                        mCursor.getString(1), CitySetting_Accu.this, false);
                SettingsManager.getInstance().setLocationArea(
                        mCursor.getString(2), CitySetting_Accu.this, false);
                Intent mIntent = new Intent();
                setResult(RESULT_OK, mIntent);
                finish();
            }
        });
        search.setOnClickListener(SEARCHLISTENER);
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
        // TODO Auto-generated method stub
        isActivited = false;
        Intent mIntent = new Intent();
        setResult(RESULT_OK, mIntent);

        super.onStop();
    }

    OnClickListener SEARCHLISTENER = new OnClickListener() {
        public void onClick(View v) {
            String inpuitcityname = city_edittext.getText().toString().trim();
            if (inpuitcityname.equals("")) {
                showDialog(SHOW_NOINPUT);
            } else
                handleCityLocation(inpuitcityname);
        }
    };

    OnClickListener ADDCITYLISTENER = new OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(CitySetting_Accu.this, AddcityActivity.class);
            intent.putExtra("isadd", true);
            CitySetting_Accu.this.startActivity(intent);
        }
    };

    private void handleCityLocation(final String inpuitcityname) {
        if (SHOW_CITIES_FLAG == 1)
            showDialog(SHOW_PROGRESS_DIALOG);
        else if (SHOW_CITIES_FLAG == 2)
            showDialog(SHOW_PROGRESS_DIALOG_O);
        else
            showDialog(SHOW_PROGRESS_DIALOG_OO);
        new Thread() {
            @Override
            public void run() {
                Message m = new Message();
                mCities = LocationReader.getInstance().getLocations(inpuitcityname);
                viewUpdateHandler.sendMessage(m);
            }
        }.start();
    }

    Handler viewUpdateHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            getProgressDialog().cancel();
            if (mCities.size() == 0) {
                showDialog(SHOW_CITY_NOT_FOUND_ALERT);
            } else {
                if (SHOW_CITIES_FLAG == 1) {
                    showDialog(SHOW_CITIES);
                    SHOW_CITIES_FLAG = 2;

                } else if (SHOW_CITIES_FLAG == 2) {
                    showDialog(SHOW_CITIES_O);
                    SHOW_CITIES_FLAG = 3;
                } else {
                    showDialog(SHOW_CITIES_OO);
                    SHOW_CITIES_FLAG = 1;
                }
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected Dialog onCreateDialog(int id) {
        if (isActivited) {

            switch (id) {
                case SHOW_CITY_NOT_FOUND_ALERT: {
                    return new AlertDialog.Builder(this)
                            .setIcon(R.drawable.alert_dialog_icon)
                            .setTitle(R.string.alert_dialog_city_not_found)
                            .setPositiveButton(R.string.alert_dialog_ok,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                        }
                                    }).setCancelable(true).create();
                }
                case SHOW_PROGRESS_DIALOG: {
                    return getProgressDialog();
                }
                case SHOW_PROGRESS_DIALOG_O: {
                    return getProgressDialog_O();
                }
                case SHOW_PROGRESS_DIALOG_OO: {
                    return getProgressDialog_OO();
                }

                case SHOW_NOINPUT: {
                    return new AlertDialog.Builder(this)
                            .setIcon(R.drawable.alert_dialog_icon)
                            .setTitle(R.string.alert_dialog_city_noinput)
                            .setPositiveButton(R.string.alert_dialog_ok,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                        }
                                    }).setCancelable(true).create();
                }
                case SHOW_CITIES:
                    CharSequence[] items = new CharSequence[mCities.size()];
                    for (int i = 0; i < mCities.size(); i++) {
                        items[i] = mCities.get(i).toString();
                    }
                    return new AlertDialog.Builder(this)
                            .setIcon(R.drawable.alert_dialog_icon)
                            .setTitle(R.string.alert_dialog_city_list)

                            .setItems(items, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichcountry) {
                                    City city = null;
                                    try {
                                        city = mCities.get(whichcountry);
                                    } catch (Exception e) {
                                        city = mCities.get(whichcountry);
                                        Log.i("xia", "mCities.size: " + mCities.size()
                                                + "  location: " + whichcountry);
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                        return;
                                    }

                                    String CITYNAME = city.getCity();
                                    String ZIPCODE = null;
                                    String STATE = city.getState();
                                    if (CITYNAME.equals("Xiangtan")) {
                                        ZIPCODE = "ASI|CN|CH014|SHAOSHAN";
                                        STATE = "China(Hunan)";
                                    } else if (CITYNAME.equals("Shenzhen")) {
                                        ZIPCODE = "ASI|HK|HK%2D%2D%2D|HONG+KONG";
                                    } else {
                                        ZIPCODE = city.getLocation();
                                    }
                                    Cursor Cursortemp = mDbHelper.query(City.CityColumns.ZIPCODE + " =  ? ", new String[] {
                                            ZIPCODE
                                    });

                                    if (Cursortemp.getCount() == 0) {
                                        ContentValues values = new ContentValues();
                                        values.put(City.CityColumns.CITYNAME, CITYNAME);
                                        values.put(City.CityColumns.CITYNAME1, STATE);

                                        values.put(City.CityColumns.ZIPCODE, ZIPCODE);
                                        mDbHelper.insertCity(values);
                                    }
                                    SettingsManager.getInstance().setLocationZipCode(
                                             ZIPCODE, CitySetting_Accu.this,
                                            false);
                                    SettingsManager.getInstance().setLocationCity(
                                            CITYNAME, CitySetting_Accu.this,
                                            false);
                                    SettingsManager.getInstance().setLocationArea(
                                            STATE, CitySetting_Accu.this,
                                            false);
                                    // initListView();
                                    dialog.dismiss();
                                    finish();
                                }
                            })
                            .setNegativeButton(R.string.add_cancel,
                                    new DialogInterface.OnClickListener() {
                                        // @Override
                                        public void onClick(DialogInterface d, int which) {
                                            d.dismiss();
                                        }
                                    }).create();

                case SHOW_CITIES_O:
                    CharSequence[] item = new CharSequence[mCities.size()];
                    for (int i = 0; i < mCities.size(); i++) {
                        item[i] = mCities.get(i).toString();
                    }
                    return new AlertDialog.Builder(this)
                            .setIcon(R.drawable.alert_dialog_icon)
                            .setTitle(R.string.alert_dialog_city_list)
                            .setItems(item, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichcountry) {
                                    City aa = null;
                                    try {
                                        aa = mCities.get(whichcountry);
                                    } catch (Exception e) {
                                        aa = mCities.get(whichcountry);
                                        Log.i("xia", "mCities.size: " + mCities.size()
                                                + "  location: " + whichcountry);
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                        return;
                                    }
                                    Cursor Cursortemp = mDbHelper.query(City.CityColumns.ZIPCODE + " =  ? ", new String[] {
                                            aa.getLocation()
                                    });
                                    
                                    if (Cursortemp.getCount() == 0) {
                                        ContentValues values = new ContentValues();
                                        values.put(City.CityColumns.CITYNAME, aa.getCity());
                                        values.put(City.CityColumns.CITYNAME1, aa.getState());
                                        values.put(City.CityColumns.ZIPCODE, aa.getLocation());
                                        mDbHelper.insertCity(
                                                values);
                                    }
                                    SettingsManager.getInstance().setLocationZipCode(
                                            aa.getLocation(),
                                            CitySetting_Accu.this, false);
                                    SettingsManager.getInstance().setLocationCity(
                                             aa.getCity(),
                                            CitySetting_Accu.this, false);
                                    SettingsManager.getInstance().setLocationArea(
                                            aa.getState(),
                                            CitySetting_Accu.this, false);
                                    // initListView();
                                    dialog.dismiss();
                                    finish();
                                }
                            })
                            .setNegativeButton(R.string.add_cancel,
                                    new DialogInterface.OnClickListener() {
                                        // @Override
                                        public void onClick(DialogInterface d, int which) {
                                            d.dismiss();
                                        }
                                    }).create();
                case SHOW_CITIES_OO:
                    CharSequence[] ite = new CharSequence[mCities.size()];
                    for (int i = 0; i < mCities.size(); i++) {
                        ite[i] = mCities.get(i).toString();
                    }
                    return new AlertDialog.Builder(this)
                            .setIcon(R.drawable.alert_dialog_icon)
                            .setTitle(R.string.alert_dialog_city_list)
                            .setItems(ite, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichcountry) {
                                    City aa = null;
                                    try {
                                        aa = mCities.get(whichcountry);
                                    } catch (Exception e) {
                                        aa = mCities.get(whichcountry);
                                        Log.i("xia", "mCities.size: " + mCities.size()
                                                + "  location: " + whichcountry);
                                        e.printStackTrace();
                                        return;
                                    }
                                    Cursor Cursortemp = mDbHelper.query(City.CityColumns.ZIPCODE
                                            + " =  ? ", new String[] {
                                        aa.getLocation()
                                    });
                                    if (Cursortemp.getCount() == 0) {
                                        ContentValues values = new ContentValues();
                                        values.put(City.CityColumns.CITYNAME, aa.getCity());
                                        values.put(City.CityColumns.CITYNAME1, aa.getState());
                                        values.put(City.CityColumns.ZIPCODE, aa.getLocation());
                                        mDbHelper.insertCity(values);
                                    }
                                    SettingsManager.getInstance().setLocationZipCode(
                                            aa.getLocation(),
                                            CitySetting_Accu.this, false);
                                    SettingsManager.getInstance().setLocationCity(
                                            aa.getCity(),
                                            CitySetting_Accu.this, false);
                                    SettingsManager.getInstance().setLocationArea(
                                            aa.getState(),
                                            CitySetting_Accu.this, false);
                                    // initListView();
                                    dialog.dismiss();
                                    finish();
                                }
                            })
                            .setNegativeButton(R.string.add_cancel,
                                    new DialogInterface.OnClickListener() {
                                        // @Override
                                        public void onClick(DialogInterface d, int which) {
                                            d.dismiss();
                                        }
                                    }).create();
                default:
                    break;
            }
        }
        return null;
    }

    private ProgressDialog getProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getResources().getString(R.string.progress_search_city));
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(true);
        }
        return progressDialog;
    }

    private ProgressDialog getProgressDialog_O() {
        if (progressDialog_O == null) {
            progressDialog_O = new ProgressDialog(this);
            progressDialog_O.setMessage(getResources().getString(R.string.progress_search_city));
            progressDialog_O.setIndeterminate(true);
            progressDialog_O.setCancelable(true);
        }
        return progressDialog;
    }

    private ProgressDialog getProgressDialog_OO() {
        if (progressDialog_OO == null) {
            progressDialog_OO = new ProgressDialog(this);
            progressDialog_OO.setMessage(getResources().getString(R.string.progress_search_city));
            progressDialog_OO.setIndeterminate(true);
            progressDialog_OO.setCancelable(true);
        }
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
                        CitySetting_Accu.this);
                name.setText(getcname(SettingsManager.getInstance().getLocationCity_w(
                        CitySetting_Accu.this))
//                        + "  " + replacechinaname(t)
                        );
            } else {
                final TextView name = (TextView) view.findViewById(R.id.text1);
                mCursor.moveToPosition(tpostion - 1);

                name.setText(getcname(mCursor.getString(1)) + "  "
                        + replacechinaname(mCursor.getString(2)));

                Log.i("xia",
                        mCursor.getString(1) + "||" + mCursor.getString(2) + "||"
                                + mCursor.getString(3));
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
        if(cityandcuntory.contains("China")){
            int po = cityandcuntory.indexOf("(");
            if(po == -1)
                return this.getResources().getString(R.string.china);
            else{
                String tem = cityandcuntory.substring(po + 1, cityandcuntory.length() - 1);
                int citystr_id = AccuIconMapper.getCityName(tem);
                if(citystr_id == 0)
                    return this.getResources().getString(R.string.china) + "(" + tem + ")";
                else
                    return this.getResources().getString(R.string.china) + "("
                            + this.getResources().getString(citystr_id) + ")";
            }
        }else
            return cityandcuntory;
    }
}
