package com.all.weather;

import com.all.weather.R;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class CityAcuuAdapter {
	private static final String TAG = "CITYDbAdapter";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	private static final String DATABASE_NAME = "weather.db";
	private static final String CITY_TABLE_NAME = "city";
	private static final int DATABASE_VERSION = 1;

	private  final  Context context;

	
	private static class DatabaseHelper extends SQLiteOpenHelper {
	    private  final  Context context;
	    
		//构造函数创建数据库
		DatabaseHelper(Context context) {		    
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			this.context = context;
		}

		@Override  //创建表
		public void onCreate(SQLiteDatabase db) {
	          String sql = "CREATE TABLE " + CITY_TABLE_NAME + " ("
              + City.CityColumns._ID+ " INTEGER PRIMARY KEY,"
              + City.CityColumns.CITYNAME + " TEXT," + City.CityColumns.CITYNAME1
              + " TEXT," + City.CityColumns.ZIPCODE + " TEXT" + ");";         
      Log.i("xia", "sql="+sql);
      db.execSQL(sql);
      
      String[] defaultcities = context.getResources().getStringArray(R.array.defaultcities);
      String insertstr = "insert into "+CITY_TABLE_NAME+
      " ("+City.CityColumns.CITYNAME+","+City.CityColumns.CITYNAME1+","+City.CityColumns.ZIPCODE+
      ") ";
      for(int i = 0;i<defaultcities.length;i++)
      {
          Log.i("xia", insertstr+defaultcities[i]);
          db.execSQL(insertstr+defaultcities[i]);
      }

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS "+DATABASE_NAME);   //版本升级，删掉数据库。
			onCreate(db);    //重新创建表
		}
	}

	public CityAcuuAdapter(Context ctx) {
		this.context = ctx;
	}
	
	//打开数据库
	public CityAcuuAdapter open() throws SQLException {
		mDbHelper = new DatabaseHelper(context);
		mDb = mDbHelper.getWritableDatabase();
		return this;
	}
    //关闭数据库
	public void closeclose() {
		mDbHelper.close();
	}
    //插入一条数据
	public long insertCity(ContentValues initialValues) {
		return mDb.insert(CITY_TABLE_NAME, null, initialValues);
	}

	//删除一条数据
	public boolean deleteCity(String rowId) {
		return mDb.delete(CITY_TABLE_NAME, City.CityColumns._ID + "=" + rowId, null) > 0;
	}
    public Cursor query(String selection,String[] selectionArgs) {
        Cursor c = mDb.query(CITY_TABLE_NAME, null, selection, selectionArgs, null, null, null);
        return c;
    }
	

	//条件查询
	public Cursor getDiary(long rowId) throws SQLException {

		Cursor mCursor =

		mDb.query(true, CITY_TABLE_NAME, null, City.CityColumns._ID  + "=" + rowId, null, null,
				null, null, null);
		if (mCursor != null) {
			mCursor.moveToFirst();
		}
		return mCursor;

	}
   //更新一条数据
	public boolean updateDiary(long rowId, String CITYNAME, String STATE,String ZIPCODE) {
		ContentValues values = new ContentValues();
		 values.put(City.CityColumns.CITYNAME, CITYNAME);
         values.put(City.CityColumns.CITYNAME1, STATE);
         values.put(City.CityColumns.ZIPCODE, ZIPCODE);         
		return mDb.update(CITY_TABLE_NAME, values, City.CityColumns._ID + "=" + rowId, null) > 0;
	}
}
