package com.android.internal.policy.impl.keyguard;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.internal.R;
import com.mediatek.FMRadio.IFMRadioService;
import com.mediatek.common.featureoption.FeatureOption;

public class FMHelper implements ServiceConnection, OnClickListener {

	// private TextView mFMName;
	private TextView mFMChanel;

	private ImageView mFMPowerBtn;
	private ImageView mFMPreBtn;
	private ImageView mFMNextBtn;
	private ImageView mFMBg;
	private ImageView mFMBgHide;
	
	private ImageView mGotoMusicBtn;

	private View mFMMainView;

	private Drawable fmPowerOn;
	private Drawable fmPowerOff;

	private Resources mRes;
	private Context mContext;
	private AudioManager mAudioManager = null;

	private static final int PLAY = 1;
	private static final int PAUSE = 2;

	private final static int UPDATE_FREQUENCY = 1000;
	private static final int CONVERT_RATE = FeatureOption.MTK_FM_50KHZ_SUPPORT ? 100
			: 10;
	public static final int HIGHEST_STATION = FeatureOption.MTK_FM_50KHZ_SUPPORT ? 10800
			: 1080;
	public static final int LOWEST_STATION = FeatureOption.MTK_FM_50KHZ_SUPPORT ? 8750
			: 875;

	private IFMRadioService mService = null;
	private View mFMView;
	private CenterKeyListener mCenterKeyListener;

	private final static String UNKNOWN = "UNKNOWN";
	private final static String FM = "FM  ";

	private final static int MSG_CHANGE_STATE = 1;
	private final static int MSG_UPDATE_FM_INFO = 3;
	private final static int MSG_POWER_UP = 4;
	private final static int MSG_POWER_DOWN = 5;
//renxinquan add start
	private boolean isServiceBind = false;
//renxinquan add end
	public FMHelper(Context mContext, View mFMView,
			CenterKeyListener mCenterKeyListener) {
				//renxinquan add start
		isServiceBind = mContext.bindService(new Intent("com.mediatek.FMRadio.IFMRadioService"),
				this, Context.BIND_AUTO_CREATE);
				//renxinquan add end
		this.mFMView = mFMView;
		this.mContext = mContext;
		this.mCenterKeyListener = mCenterKeyListener;
		mRes = mContext.getResources();
		fmPowerOn = mRes.getDrawable(R.drawable.fm_power_on);
		fmPowerOff = mRes.getDrawable(R.drawable.fm_power_off);
		mAudioManager = (AudioManager) mContext
				.getSystemService(Context.AUDIO_SERVICE);
		initView(mFMView);
	}

	private void initView(View mFMView) {

		// mFMName = (TextView) (mFMView.findViewById(R.id.fm_chanel_name_txt));
		mFMChanel = (TextView) (mFMView.findViewById(R.id.fm_chanel_txt));
		// mFMName.setText(UNKNOWN);
		mFMChanel.setText(UNKNOWN);
//GPBYY-370 cuinana del
		//mFMChanel.setTextSize(20);

		mFMPowerBtn = (ImageView) (mFMView.findViewById(R.id.fm_power_btn));
		mFMPreBtn = (ImageView) (mFMView.findViewById(R.id.fm_pre_btn));
		mFMNextBtn = (ImageView) (mFMView.findViewById(R.id.fm_next_btn));
		mFMMainView = (View) (mFMView.findViewById(R.id.fm_main_view));
		
		mGotoMusicBtn =(ImageView) (mFMView.findViewById(R.id.goto_music_btn));

		mFMPowerBtn.setOnClickListener(this);
		mFMPreBtn.setOnClickListener(this);
		mFMNextBtn.setOnClickListener(this);
		mFMMainView.setOnClickListener(this);
		mGotoMusicBtn.setOnClickListener(this);

		mFMPreBtn.setEnabled(false);
		mFMNextBtn.setEnabled(false);
		mFMPowerBtn.setEnabled(false);

		mFMBgHide = (ImageView) (mFMView.findViewById(R.id.fm_off_img));
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			int msgID = msg.what;
			switch (msgID) {
			case MSG_UPDATE_FM_INFO: {
				mHandler.post(mUpdateThread);
				break;
			}
			case MSG_POWER_UP: {
				mFMMainView.setVisibility(View.VISIBLE);
				setFmChannelInfo();
				break;
			}
			case MSG_POWER_DOWN: {
				mFMMainView.setVisibility(View.INVISIBLE);
				break;
			}
			default:
				break;
			}
		}
	};

	private void setPlayBtnState(int state) {
		if (mFMMainView.getVisibility() != View.VISIBLE) {
			Log.i("zhuwei_fm",
					"set play state fm main view is not visible!state:" + state);
			return;
		}

		if (state == PLAY) {
			if (mCenterKeyListener != null) {
				mCenterKeyListener
						.changeState(CenterKeyListener.LOCKSCREEN_LEVEL_FM_PLAY);
			} else {
				Log.e("zhuwei",
						"FMHelper call centerkeyListener fm 11 == null!");
			}
			mFMNextBtn.setEnabled(true);
			mFMPreBtn.setEnabled(true);
			mFMPowerBtn.setBackground(fmPowerOff);
			mFMBgHide.setVisibility(View.GONE);
		} else if (state == PAUSE) {
			if (mCenterKeyListener != null) {
				mCenterKeyListener
						.changeState(CenterKeyListener.LOCKSCREEN_LEVEL_FM_PAUSE);
			} else {
				Log.e("zhuwei", "FMHelper call centerkeyListener fm 22== null!");
			}
			mFMNextBtn.setEnabled(false);
			mFMPreBtn.setEnabled(false);
			mFMPowerBtn.setBackground(fmPowerOn);
			mFMBgHide.setVisibility(View.VISIBLE);
		}
	}

	public void startUpdate() {
		if (mFMView == null || mFMView.getVisibility() == View.GONE)
			return;
		mHandler.sendEmptyMessage(MSG_UPDATE_FM_INFO);
	}

	public void stop() {
		mHandler.removeMessages(MSG_UPDATE_FM_INFO);
		mHandler.removeCallbacks(mUpdateThread);
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
				isPlaying = mService.isPowerUp() && mService.isDeviceOpen();
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		return isPlaying;
	}
	
	public void startFMService() {
		mContext.startService(new Intent("com.mediatek.FMRadio.IFMRadioService"));
	}


	private final Runnable mUpdateThread = new Runnable() {
		@Override
		public void run() {
			if (mService != null) {
				try {
					if (mService.isDeviceOpen()) {
						setFmChannelInfo();
					} else {
						Log.e("zhuwei_fm", "update thread fm is not open!");
					}

				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			mHandler.postDelayed(mUpdateThread, UPDATE_FREQUENCY);
		}
	};

	@Override
	public void onServiceConnected(ComponentName arg0, IBinder arg1) {
		mService = IFMRadioService.Stub.asInterface(arg1);

		/*if (mFMMainView.getVisibility() != View.VISIBLE) {
			Log.e("zhuwei_fm", "onServiceConnected mFM is not visible");
			return;
		}*/

		if (mService != null) {
			try {
				Log.e("zhuwei_fm",
						"onServiceConnected  mService.isDeviceOpen()->"
								+ mService.isDeviceOpen()
								+ ",mService.isPowerUp()->"
								+ mService.isPowerUp());
				if (mService.isDeviceOpen()) {
					
					if (mService.isPowerUp()) {
						
						mHandler.sendEmptyMessage(MSG_POWER_UP);
						
						
					} else {
						
						mHandler.sendEmptyMessage(MSG_POWER_DOWN);
						
					}
					
				}
			} catch (Exception e) {
				e.printStackTrace();
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
		case R.id.fm_power_btn:
			clickPlayBtn();
			break;
		case R.id.fm_pre_btn:
			clickPreBtn();
			break;
		case R.id.fm_next_btn:
			clickNextBtn();
			break;
		case R.id.goto_music_btn:
			clickGotoMusicBtn();
			break;
		default:
			break;
		}
		
	}

	private void clickPlayBtn() {
		if (mService == null) {
			Log.e("zhuwei_fm", "click play Btn fm == null!");
			return;
		}
		try {
			if (!mService.isDeviceOpen()) {
				mService.openDevice();
			}

			if (mService.isPowerUp()) {
				mService.powerDownAsync();
				//setFmChannelInfo();
				//GPBYY-442 liyang 20131024 add
                //setFmChannelInfo();
                setPlayBtnState(PAUSE);
                //GPBYY-442 liyang 20131024 end
			} else {
				if (mAudioManager.isWiredHeadsetOn()) {
					float currentStation = (float) mService.getFrequency()
							/ CONVERT_RATE;
					mService.powerUpAsync(currentStation);
					mFMChanel.setText(FM + (float) mService.getFrequency()
							/ CONVERT_RATE);
					if (!mService.isEarphoneUsed()) {
						mService.useEarphone(false);
					} else {
						mService.useEarphone(true);
					}
					startUpdate();
				} else {
					mFMChanel.setText(mContext
							.getString(R.string.insert_headset));
					stop();
				}
			}

		} catch (RemoteException e) {
			e.printStackTrace();
			stop();
		}
	}

	private void clickPreBtn() {
		if (mService != null) {
			float currentStation = 0.0f;
			try {
				if (mService.isPowerUp()) {
					currentStation = (float) mService.getFrequency()
							/ CONVERT_RATE;
					mService.seekStationAsync(currentStation, false);
					setFmChannelInfo();
				}
			} catch (RemoteException e) {
				e.printStackTrace();
				stop();
			}
		} else {
			Log.e("zhuwei_fm", "clickPreBtn mService --> null!!");
		}
	}

	private void setFmChannelInfo() {
		try {

			if (mService == null) {
				Log.e("zhuwei_fm", "setFmChannelInfo mService == null!");
				return;
			}

			mFMPowerBtn.setEnabled(true);
			
			float frequency = 0.0f;

			String fmchangel = "";

			frequency = (float) mService.getFrequency() / CONVERT_RATE;
			if (frequency == 0.0f) {
				fmchangel = UNKNOWN;
			} else {
				fmchangel = FM + frequency;
			}

			mFMChanel.setText(fmchangel);

			if (mService.isPowerUp()) {
				setPlayBtnState(PLAY);
			} else {
				setPlayBtnState(PAUSE);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void clickNextBtn() {
		if (mService != null) {
			float currentStation = 0.0f;
			try {
				if (mService.isPowerUp()) {
					currentStation = (float) mService.getFrequency()
							/ CONVERT_RATE;
					mService.seekStationAsync(currentStation, false);
					setFmChannelInfo();
				}
			} catch (RemoteException e) {
				e.printStackTrace();
				stop();
			}
		} else {
			Log.e("zhuwei_fm", "clickNextBtn mService --> null!!");
		}
	}
	
	private void clickGotoMusicBtn() {
		if (mCenterKeyListener != null) {
			boolean b = mCenterKeyListener.gotoMusic();
			Log.i("zhuwei_fm", "click go to music view :"+b);
		} else {
			Log.e("zhuwei_fm","go to music error ! mCenterKeyListener == null!");
		}
	}

	private void clickOther() {

		if (mService != null) {
			Intent mIntent = new Intent();
			mIntent.setAction(Intent.ACTION_MAIN);
			mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			mIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			mIntent.setClassName("com.android.FM",
					"com.android.FM.FMBrowserActivity");
			mContext.startActivity(mIntent);
			stop();
			if (mCenterKeyListener != null) {
				mCenterKeyListener.unlock();
			} else {
				Log.e("zhuwei", "FMHelper mCenterKeyListener == null!!");
			}
		}

	}
	
	//liyang 20131021 add start
	public String getFMChanel(){
	    return mFMChanel.toString();
	}
	//liyang 20131021 add end

}
