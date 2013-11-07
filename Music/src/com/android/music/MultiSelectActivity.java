/* History:
 * 1.huangyujie@ragentek 20120615 BUG_ID:GYLLSW-1123
 *  Description: add multi-select feature to delete musics.
 * 2.huangyujie@ragentek 20120727 BUG_ID:GELJSW-387
 *  Description: cancel the choise when update the list.
 * 3.yujie.huang@ragentek 20120827 BUG_ID:GYELSW-247
 *  Description: if there is no artist name, show the string.
 * 4.zhengguo.xia@ragentek 20120830 BUG_ID:GYELSW-361
 *  Description: if there is no item choosed. set DELETE_CHOSEN invisible
 * 5.yujie.huang@reallytek.com 20121009 BUG_ID:GEYQSW-146
 *  Description: if change the system language, the state of checkboxes should be saved.
 * 6.kui.liu@reallytek.com 20121228 BUG_ID:GWLLSW-2069 
 *  Description: Play a song, delete all the songs in the music, the songs can still play.
 */

// add  BUG_ID:GYLLSW-1123 huangyujie  20120615(start)
package com.android.music;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
//add GWLLSW-2069 liukui 20121228 (on)
import android.os.RemoteException;
//add GWLLSW-2069 liukui 20121228 (off)

public class MultiSelectActivity extends Activity {
    static Cursor mCursor;
    private String mArtistId;
    private String mAlbumId;
    private String mPlaylist;	
    private String mGenre;
    private boolean mEditMode = false;
    private int mCardId = -1;
    private int mCurTrackPos = -1;
    private MultiSelectAdapter mAdapter;
    private long[] mNowPlaying;
    private IMediaPlaybackService mService;
    private static int mSize;
    static Activity mActivity;
    private ListView mTrackList;
    private static Context context = null;
// add BUG_ID:GEYQSW-146 huangyujie 20121009(start)
    static boolean[] itemStatus;
// add BUG_ID:GEYQSW-146 huangyujie 20121009(end)

    public static final int ID = 0;
    public static final int TITLE = 1; 
    public static final int ARTIST = 2;
    public static final int DATA = 3;
    public static final int ALBUM = 4;
    public static final int DISPLAY_NAME = 5;
    public static final int SIZE = 6;
    public static final int DURATION = 7;

    public static final int CHOOSE_ALL = 0;
    public static final int CANCEL_CHOOSE = 1;
    public static final int DELETE_CHOSEN  = 2;
    private static final int UPDATE_PLAYLIST = 3;
    //add GWLLSW-2069 liukui 20121228 (on)
    private static final String TAG = "MultiSelectActivity";
    RefleshReceiver refleshReceiver;
	//add GWLLSW-2069 liukui 20121228 (off)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        context = MultiSelectActivity.this;
        setContentView(R.layout.select_list);
		//add GWLLSW-2069 liukui 20121228 (on)
        IntentFilter intentfilter = new IntentFilter(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentfilter.addDataScheme("file");
        refleshReceiver = new RefleshReceiver();
        registerReceiver(refleshReceiver, intentfilter);
		//add GWLLSW-2069 liukui 20121228 (off)
        getData(context);
    // add BUG_ID:GEYQSW-146 huangyujie 20121009(start)
        itemStatus = new boolean[mSize];
        if(savedInstanceState != null){
            itemStatus = savedInstanceState.getBooleanArray("checkitem");
        }
    // add BUG_ID:GEYQSW-146 huangyujie 20121009(end)
        showCheckBoxListView(); 
    }
	
// add BUG_ID:GEYQSW-146 huangyujie 20121009(start)
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        outState.putBooleanArray("checkitem", itemStatus);
        super.onSaveInstanceState(outState);
    }
// add BUG_ID:GEYQSW-146 huangyujie 20121009(end)

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        Log.d("hyj", "multidel Destroy");
        super.onDestroy();
		//add GWLLSW-2069 liukui 20121228 (on)
        unregisterReceiver(refleshReceiver);
		//add GWLLSW-2069 liukui 20121228 (off)
        mCursor.close();
    }

    public static void getData(Context context){
        int i = 0;
        mCursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
                new String[] {
                    MediaStore.Audio.Media._ID,
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.DISPLAY_NAME,
                    MediaStore.Audio.Media.SIZE,
                    MediaStore.Audio.Media.DURATION}, 
                null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if (mCursor != null) {
            mSize = mCursor.getCount();
            Log.d("hyj", "mSize = " + mSize);
        }
    }

    public void showCheckBoxListView() {
        mAdapter = new MultiSelectAdapter();
        mTrackList = (ListView)findViewById(R.id.SelectList);
        mTrackList.setAdapter(mAdapter);
        mTrackList.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapter, View arg1, int position, long id) {
                mAdapter.toggle(position);
            }
        });
        mTrackList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }
	
    // add  BUG_ID:GYELSW-361 xiazhengguo  20120830(start)
    public int getChoosedItemNum() {
    	int num = 0;
    	for(int i = 0; i < mSize; i++) {
            mCursor.moveToPosition(i);
        // modify BUG_ID:GEYQSW-146 huangyujie 20121009(start)
            //if (mAdapter.itemStatus[i] == true) {
            if (itemStatus[i] == true) {
        // modify BUG_ID:GEYQSW-146 huangyujie 20121009(end)
                num++;
            } 
        }	
    	Log.v("MultiSelect", "getChoosedItemNum = " + num);
    	return  num;
    }
    // add  BUG_ID:GYELSW-361 xiazhengguo  20120830(end)
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // TODO Auto-generated method stub
        super.onCreateOptionsMenu(menu);
    	
        menu.add(0, CHOOSE_ALL, 0, R.string.choose_all);
        menu.add(0, CANCEL_CHOOSE, 0, R.string.cancel_chosen);
        menu.add(0, DELETE_CHOSEN, 0, R.string.delete_chosen);
        menu.add(0, UPDATE_PLAYLIST, 0, R.string.update_playlist);
    	
        return true;
    }

    // add  BUG_ID:GYELSW-361 xiazhengguo  20120830(start)
    @Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		if(getChoosedItemNum() == 0){
			menu.findItem(DELETE_CHOSEN).setVisible(false);
			menu.findItem(CANCEL_CHOOSE).setVisible(false);
		}else{
			menu.findItem(DELETE_CHOSEN).setVisible(true);
			menu.findItem(CANCEL_CHOOSE).setVisible(true);
		}
    	return super.onPrepareOptionsMenu(menu);		
	}
    // add  BUG_ID:GYELSW-361 xiazhengguo  20120830(end)

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch(item.getItemId()) {
            case CHOOSE_ALL:
                choose_all_item();
                return true;
            case CANCEL_CHOOSE:
                cancel_choose_item();
                return true;
            case DELETE_CHOSEN:
                delete_chosen_item();
                return true;
            case UPDATE_PLAYLIST:
                Intent intent = new Intent("com.android.music.musicservicecommand");
                intent.putExtra("command", "updatelist");
                intent.putExtra("ismusicupdate", true);
                sendBroadcast(intent);
                // add  BUG_ID:GELJSW-387 huangyujie  20120727(start)
                cancel_choose_item();
                // add  BUG_ID:GELJSW-387 huangyujie  20120727(end)
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void choose_all_item() {
        for(int i = 0; i < mSize; i++) {
        // modify BUG_ID:GEYQSW-146 huangyujie 20121009(start)
            //mAdapter.itemStatus[i] = true;
            itemStatus[i] = true;
        // modify BUG_ID:GEYQSW-146 huangyujie 20121009(end)
        }
        mAdapter.notifyDataSetChanged();
    }
	
    public void cancel_choose_item() {
        for(int i = 0; i < mSize; i++) {
        // modify BUG_ID:GEYQSW-146 huangyujie 20121009(start)
            //mAdapter.itemStatus[i] = false;
            itemStatus[i] = false;
        // modify BUG_ID:GEYQSW-146 huangyujie 20121009(end)
        }
        mAdapter.notifyDataSetChanged();
    }
	
    public void delete_chosen_item() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle(R.string.delete_chosen);
        alert.setMessage(R.string.alert_delete);
        alert.setPositiveButton(R.string.delete_confirm_button_text, 
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        boolean choose = false;
                        Toast.makeText(context, R.string.delete_progress_message, Toast.LENGTH_LONG).show();
                        if (mSize <= 0) {
                            Toast.makeText(context, R.string.delete_progress_message, Toast.LENGTH_LONG).cancel();
                            return;
                        }
						
                        for(int i = 0; i < mSize; i++) {
                            mCursor.moveToPosition(i);
                        // modify BUG_ID:GEYQSW-146 huangyujie 20121009(start)
                            //if (mAdapter.itemStatus[i] == false) {
                            if (itemStatus[i] == false) {
                        // modify BUG_ID:GEYQSW-146 huangyujie 20121009(end)
                                continue;
                            } else {
                            	//add GWLLSW-2069 liukui 20121228 (on)
                                //Toast.makeText(context, R.string.delete_progress_message, Toast.LENGTH_LONG).show();
                            	//add GWLLSW-2069 liukui 20121228 (off)
                            	choose = true;
								//add GWLLSW-2069 liukui 20121228 (on)
								itemStatus[i] = false;
                                try {
                                // remove from current playlist
                                	long id = mCursor.getLong(0);
									MusicUtils.sService.removeTrack(id);
								} catch (RemoteException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								//add GWLLSW-2069 liukui 20121228 (off)
                             
                                String uri = mCursor.getString(DATA);
                                File file = new File(uri);
								//modify GWLLSW-2069 liukui 20121228 (on)
                                //file.delete();
                                try {  // File.delete can throw a security exception
                                    if (!file.delete()) {
                                        // I'm not sure if we'd ever get here (deletion would
                                        // have to fail, but no exception thrown)
                                        MusicLogUtils.e(TAG, "Failed to delete file,uri =  " + uri);
                                    }
                                } catch (SecurityException ex) {
                                }
								//modify GWLLSW-2069 liukui 20121228 (off)
                            }
                        }
                        if (choose == true) {
							//del GWLLSW-2069 liukui 20121228 (on)
//                            IntentFilter intentfilter = new IntentFilter(Intent.ACTION_MEDIA_SCANNER_FINISHED);
//                            intentfilter.addDataScheme("file");
//                            RefleshReceiver refleshReceiver = new RefleshReceiver();
//                            registerReceiver(refleshReceiver, intentfilter);
							//del GWLLSW-2069 liukui 20121228 (on)
                            sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+ Environment.getExternalStorageDirectory().getAbsolutePath())));
                            Toast.makeText(context, R.string.update_playlist, Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(context, R.string.no_chosen, Toast.LENGTH_LONG).show();
                        }
                    }
                }).setNegativeButton(R.string.cancel, 
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                    }
                });

        alert.create();
        alert.show();
    }
	
    public class RefleshReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            String action = intent.getAction(); 
            if(Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
                getData(context);
                showCheckBoxListView();
                Toast.makeText(context, R.string.weekpicker_set, Toast.LENGTH_LONG).show();
            }
        }
    }

    public static class MultiSelectAdapter extends BaseAdapter {

    // modify BUG_ID:GEYQSW-146 huangyujie 20121009(start)
        //boolean[] itemStatus = new boolean[mSize];
    // modify BUG_ID:GEYQSW-146 huangyujie 20121009(end)
        private String keyString[] = null;
        private int idValue[] = null;
        private String itemString = null;
        private LayoutInflater inflater = null;
		
        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mSize;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;

            if (holder == null) {
                holder = new ViewHolder();
                if (convertView == null) {
                    inflater = LayoutInflater.from(context);
                    convertView = inflater.inflate(R.layout.check_list_item, null);
                }
                holder.artistText = (TextView) convertView
                        .findViewById(R.id.artistTextEdit);
                holder.titleText = (TextView) convertView
                        .findViewById(R.id.titleTextEdit);
                holder.checkBox = (CheckBox) convertView
                        .findViewById(R.id.checkBoxEdit);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            convertView.setMinimumHeight(64);

            mCursor.moveToPosition(position);
            holder.titleText.setText(mCursor.getString(TITLE));
        // modify  BUG_ID:GYELSW-247 huangyujie  20120827(start)
            //holder.artistText.setText(mCursor.getString(ARTIST));
            if (mCursor.getString(ARTIST) == null || mCursor.getString(ARTIST).equals(MediaStore.UNKNOWN_STRING)) {
                holder.artistText.setText(context.getString(R.string.unknown_artist_name));
            } else {
                holder.artistText.setText(mCursor.getString(ARTIST));
            }
        // modify  BUG_ID:GYELSW-247 huangyujie  20120827(end)
            holder.checkBox.setOnCheckedChangeListener(new CheckBoxChangedListener(position));
			//GPBYB-332 liyang 20130912 add start
			position %= mSize;
			//GPBYB-332 liyang 20130912 add end
            if (itemStatus[position] == true) {
                holder.checkBox.setChecked(true);
            } else {
                holder.checkBox.setChecked(false);
            }
            return convertView;
        }

        static class ViewHolder {
            TextView titleText;
            TextView artistText;
            CheckBox checkBox;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        public void toggle(int position){
            if(itemStatus[position] == true){
                itemStatus[position] = false;
            }else{
                itemStatus[position] = true;
            }
            this.notifyDataSetChanged();//date changed and we should refresh the view
        }

        class CheckBoxChangedListener implements OnCheckedChangeListener {
            int position;

            CheckBoxChangedListener(int position) {
                this.position = position;
            }

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                System.out.println("" + position + "Checked?:" + isChecked);
                if (isChecked)
                    itemStatus[position] = true;
                else
                    itemStatus[position] = false;
            }
        }

    }
	
}
//add  BUG_ID:GYLLSW-1123 huangyujie  20120615(end)
