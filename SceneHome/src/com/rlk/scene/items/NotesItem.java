package com.rlk.scene.items;
import java.util.ArrayList;
import java.util.Locale;

import com.rlk.scene.R;
import com.rlk.scene.SceneSurfaceView2;



import android.R.raw;
import android.content.AsyncQueryHandler;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Canvas;
import android.net.Uri;
import android.util.Log;
import android.view.MotionEvent;

public class NotesItem extends TouchableDrawableGroup{
    String preferString="Notes_category";
	Uri mUri=Uri.parse("content://com.mediatek.notebook.NotePad/notes");
    ArrayList<NoteItem> mNoteItemsList;
    public static boolean isEnglishLocal=false;
	public static final String[] PROJECTION = new String[] {
        "_id",
        "note",
        "notegroup",
        "modified",
        "created"};
	
	public NotesItem(Context context, int x, int y, int width, int height) {
		super(context, x, y, width, height);
		// TODO Auto-generated constructor stub
	}
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Context otherAppsContext;
		boolean groupMode=true;
		try {
			otherAppsContext = getContext().createPackageContext("com.mediatek.notebook", 2);
			SharedPreferences sharedPreferences = otherAppsContext.getSharedPreferences(preferString, Context.MODE_WORLD_READABLE);
		    groupMode=sharedPreferences.getBoolean("category",false);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String mOrder;
		if(groupMode)
			mOrder="notegroup DESC, modified DESC";
		else {
			mOrder="modified DESC";
		}
		QueryHandler qh = new QueryHandler(getContext().getContentResolver(), getContext());
        qh.startQuery(0, 
                null, 
                mUri, 
                PROJECTION, 
                null, 
                null, 
                mOrder);
		
	}
	
	public void updateNotes()
	{
		clearItems();
		if(mNoteItemsList!=null)
		{
			String str = Locale.getDefault().getLanguage();
            if(str.startsWith("en"))
            {
            	isEnglishLocal=true;
            }
            else {
            	isEnglishLocal=false;
			}
			int index =0;
			for (final NoteItem noteItem : mNoteItemsList) {
				MyNote mNote =new MyNote(getContext(), 34+index*87, 60, 83, 108,noteItem);
				mNote.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(TouchableDrawableItem item, MotionEvent event) {
						// TODO Auto-generated method stub
						 Uri uri = ContentUris.withAppendedId(mUri, noteItem.id);
						  Intent mIntent=new Intent(Intent.ACTION_VIEW);
						  mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						  mIntent.setComponent(new ComponentName("com.mediatek.notebook","com.mediatek.notebook.NoteReading")); 
						  mIntent.setData(uri);
						  mIntent.putExtra("isFromScence", true);
						  getContext().startActivity(mIntent);
					}
				});
				addItem(mNote);
				index++;
			}
		}
		SceneSurfaceView2.instance.drawScene(SceneSurfaceView2.defaultX, SceneSurfaceView2.defaultY);
	}
	class QueryHandler extends AsyncQueryHandler
	{
		 public QueryHandler(ContentResolver cr, Context context) {
		        super(cr);
		    }
		 
		    public void onQueryComplete(int token, Object cookie, Cursor cursor) {  
		               setData(cursor, token);
		               updateNotes();
		      }
		    public void setData(Cursor cursor, int token) {
		        NoteItem item;
		        String note;
		        String notegroup;
		        String modifyTime;
		        int id;
		        mNoteItemsList=new ArrayList<NotesItem.NoteItem>();
			if(cursor!=null){
		         if (cursor.moveToFirst()) {
		            int idColumn = cursor.getColumnIndex(PROJECTION[0]);
		            int titleColumn = cursor.getColumnIndex(PROJECTION[1]);
		            int groupColumn = cursor.getColumnIndex(PROJECTION[2]);
		            int modifyColumn = cursor.getColumnIndex(PROJECTION[3]);
		            int index=0;
		            do {
		                id = cursor.getInt(idColumn);
		                note = cursor.getString(titleColumn);
		                notegroup = cursor.getString(groupColumn);
		                modifyTime = cursor.getString(modifyColumn);
		                item = new NoteItem();
		                item.id = id;
		                item.note = note;
		                item.notegroup=notegroup;
		                item.modify_time = modifyTime;
		                item.noteTitle = getGroup(notegroup);
		                mNoteItemsList.add(item);
		                index++;
		            } while(cursor.moveToNext()&&index<3);
		       	 }
			}
		        cursor.close();
			cursor=null;
		    }
		
	}
	 public String getGroup(String i) {
	        Resources resource = getContext().getResources();
	        String groupNone = (String) resource.getString(R.string.menu_none); 
	        String groupWork = (String) resource.getString(R.string.menu_work); 
	        String groupPersonal = (String) resource.getString(R.string.menu_personal);
	        String groupFamily = (String) resource.getString(R.string.menu_family);
	        String groupStudy = (String) resource.getString(R.string.menu_study);
	        if (i.equals("1")) {
	            return groupWork;
	        } else if (i.equals("2")) {
	            return groupPersonal;
	        } else if (i.equals("3")) {
	            return groupFamily;
	        } else if (i.equals("4")) {
	            return groupStudy;
	        } else {
	            return groupNone;
	        }
	    }
	 public class NoteItem {
         public int id;
         public String note;
         public String create_time;
         public boolean isselect;
         public String notegroup;
         public String noteTitle;
         public String modify_time;
     }
}
