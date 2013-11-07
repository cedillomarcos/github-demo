package com.android.internal.policy.impl.keyguard;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.android.music.IMediaPlaybackService;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.internal.R;

public class MusicHelper implements ServiceConnection, OnClickListener {

	private TextView mMusicName;
	private TextView mMusicArist;
	private TextView mMusicTime;
	private TextView mMusicTotalTime;
	private ProgressBar mProgressBar;

	private ImageView mMusicPlayBtn;
	private ImageView mMusicPreBtn;
	private ImageView mMusicNextBtn;
	private ImageView mMusicBg;
	private ImageView mMusicBgHide;

	private ImageView mFmBtn;

	private View mMusicMainView;

	private AnimationDrawable mMusicAnim;

	private Resources mRes;
	private Context mContext;

	private Drawable mPlayDrawable;
	private Drawable mPauseDrawable;

	private static final int PLAY = 1;
	private static final int PAUSE = 2;

	private final static int MAX_PROGRESS = 100;
	private final static int INIT_PROGRESS = 0;
	private final static int UPDATE_FREQUENCY = 1000;

	private IMediaPlaybackService mService = null;
	private View mMusicView;
	private CenterKeyListener mCenterKeyListener;

	private final static String UNKNOWN = "";

	private final static int MSG_CHANGE_STATE = 1;
	private final static int MSG_UPDATE_PROGRESS = 2;
	private final static int MSG_UPDATE_MUSIC_INFO = 3;
	private final static int MSG_SET_VIEW_GONE = 4;
	private final static int MSG_UPDATE_ART_WORK = 5;

	private static final String sExternalMediaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
			.toString();
	private static final Uri sArtworkUri = Uri
			.parse("content://media/external/audio/albumart");
	private static Bitmap mCachedBit = null;
	private static final BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();

	private AlbumArtWorker mAsyncAlbumArtWorker = null;
//renxinquan add start
	private boolean isServiceBind = false;
//renxinquan add end
	public MusicHelper(Context mContext, View musicView,
			CenterKeyListener mCenterKeyListener) {
		this.mMusicView = musicView;
		this.mContext = mContext;
		this.mCenterKeyListener = mCenterKeyListener;
		mRes = mContext.getResources();
		mMusicMainView = musicView;
		mPlayDrawable = mRes.getDrawable(R.drawable.gp811_music_pause);
		mPauseDrawable = mRes.getDrawable(R.drawable.gp811_music_play);
		initView(musicView);
		//renxinquan add start
		isServiceBind = mContext.bindService(new Intent(
				"com.android.music.MediaPlaybackService"), this,
				Context.BIND_AUTO_CREATE);
				//renxinquan add end
		// sBitmapOptions
		sBitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
		sBitmapOptions.inDither = false;
	}

	private void initView(View musicView) {

		mMusicName = (TextView) (musicView.findViewById(R.id.music_name));
		mMusicArist = (TextView) (musicView.findViewById(R.id.music_artist));
		mMusicTime = (TextView) (musicView.findViewById(R.id.music_time));
		mMusicTotalTime = (TextView) (musicView
				.findViewById(R.id.music_time_total));
		mProgressBar = (ProgressBar) (musicView
				.findViewById(R.id.music_progress));

		mMusicName.setText(UNKNOWN);
		mMusicArist.setText(UNKNOWN);
		mMusicTime.setText(duringToString(0));
		mMusicTotalTime.setText(duringToString(0));
		mProgressBar.setProgress(INIT_PROGRESS);
		mProgressBar.setMax(MAX_PROGRESS);

		mMusicPlayBtn = (ImageView) (musicView
				.findViewById(R.id.music_play_or_pause_btn));
		mMusicPreBtn = (ImageView) (musicView.findViewById(R.id.music_pre_btn));
		mMusicNextBtn = (ImageView) (musicView
				.findViewById(R.id.music_next_btn));

		mFmBtn = (ImageView) (musicView.findViewById(R.id.goto_fm_btn));

		mMusicPlayBtn.setOnClickListener(this);
		mMusicPreBtn.setOnClickListener(this);
		mMusicNextBtn.setOnClickListener(this);
		mMusicView.setOnClickListener(this);
		if (mFmBtn != null) {
			mFmBtn.setOnClickListener(this);
		} else {
			Log.e("zhuwei_music", "mGoto Fm Btn ==  null!!!!");
		}

		mMusicPlayBtn.setEnabled(false);
		mMusicPreBtn.setEnabled(false);
		mMusicNextBtn.setEnabled(false);

		mMusicBg = (ImageView) musicView.findViewById(R.id.music_play_bg);
		mMusicBgHide = (ImageView) musicView.findViewById(R.id.music_play_hide);

		mMusicAnim = (AnimationDrawable) mMusicBg.getDrawable();

	}

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			int msgID = msg.what;
			switch (msgID) {
			case MSG_CHANGE_STATE: {
				break;
			}
			case MSG_UPDATE_PROGRESS: {
				break;
			}
			case MSG_UPDATE_MUSIC_INFO: {
				mMusicMainView.setVisibility(View.VISIBLE);
				mHandler.post(mClockTick);
				break;
			}
			case MSG_SET_VIEW_GONE: {
				mMusicMainView.setVisibility(View.INVISIBLE);
				break;
			}
			case MSG_UPDATE_ART_WORK: {
				Drawable drawable = (Drawable) msg.obj;
				mMusicBg.setBackground(drawable);
				break;
			}
			default:
				break;
			}
		}
	};

	private void setPlayBtnState(int state) {
		if (state == PLAY) {
			mMusicAnim.setVisible(true, true);
			mMusicAnim.start();
			if (mCenterKeyListener != null) {
				mCenterKeyListener
						.changeState(CenterKeyListener.LOCKSCREEN_LEVEL_MUSIC_PLAY);
			} else {
				Log.e("zhuwei", "MusicHelper call centerkeyListener == null!");
			}
			mMusicBgHide.setVisibility(View.GONE);
			mMusicPlayBtn.setImageDrawable(mPlayDrawable);
		} else if (state == PAUSE) {
			mMusicAnim.stop();
			mMusicAnim.setVisible(false, true);
			if (mCenterKeyListener != null) {
				mCenterKeyListener
						.changeState(CenterKeyListener.LOCKSCREEN_LEVEL_MUSIC_PAUSE);
			} else {
				Log.e("zhuwei", "MusicHelper call centerkeyListener == null!");
			}
			mMusicBgHide.setVisibility(View.VISIBLE);
			mMusicPlayBtn.setImageDrawable(mPauseDrawable);
		}
	}

	private void updateMusic(boolean enable) {
		if (mService == null) {
			Log.e("zhuwei", "init music error!");
		}
		if (mMusicMainView.getVisibility() == View.GONE) {
			Log.e("zhuwei", "musicMainView is gone ! update return!");
			return;
		}
		if (enable) {
			long position = 0;
			long duration = 0;
			String trackName = "";
			String ArtistName = "";
			try {
				mAsyncAlbumArtWorker = new AlbumArtWorker();
				mAsyncAlbumArtWorker.execute(Long.valueOf(mService
						.getAlbumId()));
				position = mService.position();
				duration = mService.duration();
				trackName = mService.getTrackName();
				ArtistName = mService.getArtistName();
			} catch (RemoteException e) {

			}
			mMusicName.setText(trackName);
			mMusicArist.setText(ArtistName);
			mMusicTime.setText(duringToString(position));
			mMusicTotalTime.setText(duringToString(duration));
			mProgressBar.setProgress(getProgress(duration, position));
		} else {
			mMusicName.setText(UNKNOWN);
			mMusicArist.setText(UNKNOWN);
			mMusicTime.setText(duringToString(0));
			mMusicTotalTime.setText(duringToString(0));
			mProgressBar.setProgress(INIT_PROGRESS);
		}
		mMusicPlayBtn.setEnabled(enable);
		mMusicPreBtn.setEnabled(enable);
		mMusicNextBtn.setEnabled(enable);
	}

	private int getProgress(long duration, long position) {
		if (duration < 0 || position < 0)
			return INIT_PROGRESS;
		return (int) ((position * MAX_PROGRESS) / duration);
	}

	public void startUpdate() {
		if (mMusicView == null || mMusicView.getVisibility() == View.GONE)
			return;
		mHandler.sendEmptyMessage(MSG_UPDATE_MUSIC_INFO);
	}

	public void stop() {
		mHandler.removeMessages(MSG_UPDATE_MUSIC_INFO);
		mHandler.removeCallbacks(mClockTick);
	}

	public void destroy() {
		//renxinquan add start
		if(isServiceBind){
			isServiceBind = false;
			mContext.unbindService(this);
		}
		//renxinquan add end
	}

	public boolean isPlaying() {
		boolean isPlaying = false;
		if (mService != null) {
			try {
				if (mService.isPlaying()) {
					isPlaying = true;
				}
			} catch (RemoteException e) {
				// TODO: handle exception
			}
		}
		return isPlaying;
	}

	public void startMusicService() {
		mContext.startService(new Intent(
				"com.android.music.MediaPlaybackService"));
	}

	private final Runnable mClockTick = new Runnable() {
		@Override
		public void run() {
			if (mMusicMainView.getVisibility() != View.VISIBLE) {
				return;
			}
			if (mService != null) {
				try {
					if (mService.isPlaying()) {
						setPlayBtnState(PLAY);
					} else {
						setPlayBtnState(PAUSE);
					}
					updateMusic(true);
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (Exception e) {

				}
			}
			mHandler.postDelayed(mClockTick, UPDATE_FREQUENCY);
		}
	};

	//liyang 20121022 add start
	public String mmMusicName;
	
	private final Runnable getMusicName = new Runnable() {
        
        @Override
        public void run() {
            // TODO Auto-generated method stub
            if (mService != null && mService.isPlaying()) {
                mmMusicName = mService.getTrackName();
            }else {
                mmMusicName = null;
            }
        }
    };
	
    public IMediaPlaybackService getService() {
        if (mService != null) {
            return mService;
        }
    }
    //liyang 20121022 add end
	
	private String duringToString(long during) {
		if (during <= 0)
			return "00:00";
		double duration = 0;
		int minute = 0, second = 0, hour = 0;
		duration = during;
		duration = Math.ceil(duration / 1000);
		hour = (int) (duration) / 3600;
		minute = (int) (duration) / 60 - hour * 60;
		second = (int) (duration) % 60;
		if (hour <= 0)
			return String.format("%02d:%02d", minute, second);
		else {
			return String.format("%02d:%02d:%02d", hour, minute, second);
		}
	}

	@Override
	public void onServiceConnected(ComponentName arg0, IBinder arg1) {
		mService = IMediaPlaybackService.Stub.asInterface(arg1);
		if (mService != null) {
			try {
				if (mService.isPlaying()) {
					mHandler.sendEmptyMessage(MSG_UPDATE_MUSIC_INFO);
				} else {
					mHandler.sendEmptyMessage(MSG_SET_VIEW_GONE);
				}
			} catch (RemoteException e) {
				// TODO: handle exception
			}
		}
	}

	@Override
	public void onServiceDisconnected(ComponentName arg0) {
		mService = null;
	}

	@Override
	public void onClick(View arg0) {
		int id = arg0.getId();
		switch (id) {
		case R.id.music_play_or_pause_btn: {
			clickPlayBtn();
			break;
		}
		case R.id.music_pre_btn: {
			clickPreBtn();//GPBYY-360
			break;
		}
		case R.id.music_next_btn: {
			clickNextBtn();
			break;
		}

		case R.id.music_main_view: {
			// clickOther();
			break;
		}

		case R.id.goto_fm_btn: {
			clickGotoFMBtn();
		}

		default:
			break;
		}
	}

	private void clickPlayBtn() {
		if (mService != null) {
			try {
				if (mService.isPlaying()) {
					mService.pause();
					//GPBYY-442 liyang 20131024 add
                    setPlayBtnState(PAUSE);
                    //GPBYY-442 liyang 20131024 end
				} else {
					mService.play();
					//GPBYY-442 liyang 20131024 add
                    setPlayBtnState(PLAY);
                    //GPBYY-442 liyang 20131024 end
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private void clickPreBtn() {
		if (mService != null) {
			try {
				mService.prev();
			} catch (RemoteException e) {
				e.printStackTrace();
			}

		}
	}

	private void clickNextBtn() {
		if (mService != null) {
			try {
				mService.next();
			} catch (RemoteException e) {
				e.printStackTrace();
			}

		}
	}

	private void clickGotoFMBtn() {
		if (mCenterKeyListener != null) {
			boolean b = mCenterKeyListener.gotoFM();
			Log.i("zhuwei_music", "click go to fm :" + b);
		} else {
			Log.e("zhuwei_music",
					"click go to fm error mCenterKeyListener == null");
		}
	}

	private void clickOther() {
		if (mService != null) {
			Intent mIntent = new Intent();
			mIntent.setAction(Intent.ACTION_MAIN);
			mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			mIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			mIntent.setClassName("com.android.music",
					"com.android.music.MusicBrowserActivity");
			mContext.startActivity(mIntent);
			stop();
			if (mCenterKeyListener != null) {
				mCenterKeyListener.unlock();
			} else {
				Log.e("zhuwei", "MusicHelper mCenterKeyListener == null!!");
			}
		}
	}

	private class AlbumArtWorker extends AsyncTask<Long, Void, Bitmap> {
		protected Bitmap doInBackground(Long... albumId) {
			Bitmap bm = null;
			try {
				long id = albumId[0].longValue();
				bm = getArtwork(mContext, -1, id, true);
				if (bm == null) {
					bm = getDefaultArtwork(mContext);
				}
			} catch (IllegalArgumentException ex) {
				return null;
			}
			return bm;
		}

		protected void onPostExecute(Bitmap bm) {
			bm = getRoundedCornerBitmap(bm);
			Drawable drawable = new BitmapDrawable(bm);
			Message msg = new Message();
			msg.what = MSG_UPDATE_ART_WORK;
			msg.obj = drawable;
			mHandler.sendMessage(msg);
		}
	}

	private static Bitmap getArtwork(Context context, long song_id,
			long album_id, boolean allowdefault) {
		if (album_id < 0) {
			if (song_id >= 0) {
				Bitmap bm = getArtworkFromFile(context, song_id, -1);
				if (bm != null) {
					return bm;
				}
			}
			if (allowdefault) {
				return getDefaultArtwork(context);
			}
			return null;
		}

		ContentResolver res = context.getContentResolver();
		Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
		if (uri != null) {
			InputStream in = null;
			try {
				in = res.openInputStream(uri);
				return BitmapFactory.decodeStream(in, null, sBitmapOptions);
			} catch (FileNotFoundException ex) {
				Bitmap bm = getArtworkFromFile(context, song_id, album_id);
				if (bm != null) {
					if (bm.getConfig() == null) {
						bm = bm.copy(Bitmap.Config.RGB_565, false);
						if (bm == null && allowdefault) {
							return getDefaultArtwork(context);
						}
					}
				} else if (allowdefault) {
					bm = getDefaultArtwork(context);
				}
				return bm;
			} finally {
				try {
					if (in != null) {
						in.close();
					}
				} catch (IOException ex) {
				}
			}
		}

		return null;
	}

	private static Bitmap getArtworkFromFile(Context context, long songid,
			long albumid) {
		Bitmap bm = null;
		byte[] art = null;
		String path = null;

		if (albumid < 0 && songid < 0) {
			throw new IllegalArgumentException(
					"Must specify an album or a song id");
		}

		try {
			if (albumid < 0) {
				Uri uri = Uri.parse("content://media/external/audio/media/"
						+ songid + "/albumart");
				ParcelFileDescriptor pfd = context.getContentResolver()
						.openFileDescriptor(uri, "r");
				if (pfd != null) {
					FileDescriptor fd = pfd.getFileDescriptor();
					bm = BitmapFactory.decodeFileDescriptor(fd);
				}
			} else {
				Uri uri = ContentUris.withAppendedId(sArtworkUri, albumid);
				ParcelFileDescriptor pfd = context.getContentResolver()
						.openFileDescriptor(uri, "r");
				if (pfd != null) {
					FileDescriptor fd = pfd.getFileDescriptor();
					bm = BitmapFactory.decodeFileDescriptor(fd);
				}
			}
		} catch (IllegalStateException ex) {
		} catch (FileNotFoundException ex) {
		}
		if (bm != null) {
			mCachedBit = bm;
		}
		return bm;
	}

	static Bitmap getDefaultArtwork(Context context) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
		// get default albumart
		return BitmapFactory.decodeStream(context.getResources()
				.openRawResource(R.drawable.music_img_on), null, opts);
	}
	
	private static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                int widthRect = width;
                int heightRect = height;
		Bitmap output = Bitmap.createBitmap(width,
		height, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
                
                final int color = 0xff424242;
		final Paint paint = new Paint();
		final Rect rect = new Rect(16, 16, widthRect-16, heightRect-16);
		final RectF rectF = new RectF(rect);
		final float roundPx = bitmap.getWidth()/2;

		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint); 
		return output;
	}
	
}
