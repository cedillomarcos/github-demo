package com.android.soundrecorder;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.LayoutInflater.Factory;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
//Add GPBYB-57 20130809 (on)
import android.media.AudioManager;
//Add GPBYB-57 20130809 (off)
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class SoundRecorder extends Activity implements SoundRecorderService.OnEventListener,
        SoundRecorderService.OnErrorListener, SoundRecorderService.OnStateChangedListener,
        Button.OnClickListener {

    private static final String TAG = "SR/SoundRecorder";
    private static final String NULL_STRING = "";
    private static final int OPTIONMENU_SELECT_FORMAT = 0;
    private static final int OPTIONMENU_SELECT_MODE = 1;
    private static final int OPTIONMENU_SELECT_EFFECT = 2;
    private static final int DIALOG_SELECT_MODE = 0;
    private static final int DIALOG_SELECT_FORMAT = 1;
    public static final int DIALOG_SELECT_EFFECT = 2;
    private static final int TWO_BUTTON_WEIGHT_SUM = 2;
    private static final int THREE_BUTTON_WEIGHT_SUM = 3;
    private static final int REQURST_FILE_LIST = 1;
    private static final int TIME_BASE = 60;
    private static final int TIMER_VIEW_TEXT_SIZE_BIG = 120;// GPBYY-62 cuinana  90 to 120
    private static final int TIMER_VIEW_TEXT_SIZE_SMALL = 70;
    private static final int UPDATE_TIMER_VIRE_POST_DELAYED = 50;
    private static final long MAX_FILE_SIZE_NULL = -1L;
    private static final int TIME_NINE_MIN = 540;
    private static final String INTENT_ACTION_MAIN = "android.intent.action.MAIN";
    private static final String EXTRA_MAX_BYTES = android.provider.MediaStore.Audio.Media.EXTRA_MAX_BYTES;
    private static final String AUDIO_NOT_LIMIT_TYPE = "audio/*";
    private static final String DIALOG_TAG_SELECT_MODE = "SelectMode";
    private static final String DIALOG_TAG_SELECT_FORMAT = "SelectFormat";
    private static final String DIALOG_TAG_SELECT_EFFECT = "SelectEffect";
    private static final String SOUND_RECORDER_DATA = "sound_recorder_data";
    private static final String PATH = "path";
    public static final String PLAY = "play";
    public static final String RECORD = "record";
    public static final String INIT = "init";
    public static final String DOWHAT = "dowhat";
    public static final String EMPTY = "";

    private static final String SELECTED_RECORDING_FORMAT = "selected_recording_format";
    private static final String SELECTED_RECORDING_MODE = "selected_recording_mode";
    private static final String SELECTED_RECORDING_EFFECT_AEC = "selected_recording_effect_aec";
    private static final String SELECTED_RECORDING_EFFECT_AGC = "selected_recording_effect_agc";
    private static final String SELECTED_RECORDING_EFFECT_NS = "selected_recording_effect_ns";
    private static final String SELECTED_RECORDING_EFFECT_AEC_TMP = "selected_recording_effect_aec_tmp";
    private static final String SELECTED_RECORDING_EFFECT_AGC_TMP = "selected_recording_effect_agc_tmp";
    private static final String SELECTED_RECORDING_EFFECT_NS_TMP = "selected_recording_effect_ns_tmp";

    private int mSelectedFormat = -1;
    private int mSelectedMode = -1;
    private boolean[] mSelectEffectArray = new boolean[3];
    private boolean[] mSelectEffectArrayTemp = new boolean[3];

    private int mCurrentState = SoundRecorderService.STATE_IDLE;
    private String mRequestedType = AUDIO_NOT_LIMIT_TYPE;
    private String mTimerFormat = null;
    private String mFileName = "";
    private String mDoWhat = null;
    private String mDoWhatFilePath = null;
    private long mMaxFileSize = -1L;
    private boolean mRunFromLauncher = true;
    private boolean mHasFileSizeLimitation = false;
    private boolean mBackPressed = false;
    private boolean mOnSaveInstanceStateHasRun = false;
    private WakeLock mWakeLock = null;
    // M: used for saving record file when SoundRecorder launch from other
    // application
    private boolean mSetResultAfterSave = true;
    // private WakeLock mWakeLock = null;
    private SharedPreferences mPrefs = null;

    private Menu mMenu = null;
    private Button mAcceptButton;
    private Button mDiscardButton;
    private ImageButton mRecordButton;
    private ImageButton mPlayButton;
    private ImageButton mStopButton;
    private ImageButton mFileListButton;
    private ImageButton mPauseRecordingButton;
    // image view at the left of mStateTextView
    private ImageView mRecordingStateImageView;
    // image view at the left of mRecordingFileNameTextView
    private ImageView mPlayingStateImageView;
    private TextView mRemainingTimeTextView; // message below the state message
    private TextView mStateTextView; // state message with LED
    private TextView mTimerTextView;
    private TextView mRecordingFileNameTextView;
    private ProgressBar mStateProgressBar;
    private LinearLayout mExitButtons;
    private VUMeter mVUMeter;
    private LinearLayout mButtonParent;
    private OnScreenHint mStorageHint;
    private ImageView mFirstLine;
    private ImageView mSecondLine;
    private FrameLayout mFrameLayout;

    // M: add for long string in option menu
    private static final String LIST_MENUITEM_VIEW_NAME = "com.android.internal.view.menu.ListMenuItemView";
    private static final Class[] INFLATER_CONSTRUCTOR_SIGNATURE = new Class[] { Context.class,
            AttributeSet.class };
    private static Class sListMenuItemViewClass = null;
    private static Constructor sListMenuItemViewConstructor = null;

    private SoundRecorderService mService = null;
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            LogUtils.i(TAG, "<onServiceConnected> Service connected");
            mService = ((SoundRecorderService.SoundRecorderBinder) arg1).getService();
            initWhenHaveService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            LogUtils.i(TAG, "<onServiceDisconnected> Service dis connected");
            mService = null;
        }
    };

    private final Handler mHandler = new Handler();
    private final Runnable mUpdateTimer = new Runnable() {
        public void run() {
            updateTimerView();
        }
    };

    private DialogInterface.OnClickListener mSelectFormatListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int whichItemSelect) {
            LogUtils.i(TAG, "<mSelectFormatListener onClick>");
            setSelectedFormat(whichItemSelect);
            dialog.dismiss();
        }
    };

    private DialogInterface.OnClickListener mSelectModeListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int whichItemSelect) {
            LogUtils.i(TAG, "<mSelectModeListener onClick>");
            setSelectedMode(whichItemSelect);
            dialog.dismiss();
        }
    };

    private DialogInterface.OnClickListener mSelectEffectOkListener = new OnClickListener() {

        @Override
        public void onClick(DialogInterface arg0, int arg1) {
            mSelectEffectArray = mSelectEffectArrayTemp.clone();
        }
    };

    private DialogInterface.OnMultiChoiceClickListener 
        mSelectEffectMultiChoiceClickListener = new OnMultiChoiceClickListener() {

        @Override
        public void onClick(DialogInterface arg0, int arg1, boolean arg2) {
            mSelectEffectArrayTemp[arg1] = arg2;
        }
    };

    @Override
    public void onEvent(int eventCode) {
        switch (eventCode) {
        case SoundRecorderService.EVENT_SAVE_SUCCESS:
            LogUtils.i(TAG, "<onEvent> EVENT_SAVE_SUCCESS");
            Uri uri = mService.getSaveFileUri();
            if (uri != null) {
                mAcceptButton.setEnabled(true);
                mDiscardButton.setEnabled(true);
                mRecordButton.setEnabled(true);
                Toast.makeText(SoundRecorder.this, R.string.tell_save_record_success,
                        Toast.LENGTH_SHORT).show();
            }
            if (!mRunFromLauncher) {
                LogUtils.i(TAG, "<onEvent> mSetResultAfterSave = " + mSetResultAfterSave);
                if (mSetResultAfterSave) {
                    setResult(RESULT_OK, new Intent().setData(uri));
                    LogUtils.i(TAG, "<onEvent> finish");
                    LogUtils.i(TAG, "<onEvent> Activity = " + this.toString());
                    finish();
                } else {
                    mSetResultAfterSave = true;
                }
            }
            mService.reset();
            updateUi();
            long mEndSaveTime = System.currentTimeMillis();
            Log.i(TAG, "[Performance test][SoundRecorder] recording save end [" + mEndSaveTime
                    + "]");
            break;
        case SoundRecorderService.EVENT_DISCARD_SUCCESS:
            LogUtils.i(TAG, "<onEvent> EVENT_DISCARD_SUCCESS");
            if (mRunFromLauncher) {
                mService.reset();
                updateUi();
            } else {
                mService.reset();
                LogUtils.i(TAG, "<onEvent> finish");
                LogUtils.i(TAG, "<onEvent> Activity = " + this.toString());
                finish();
            }
            break;
        case SoundRecorderService.EVENT_STORAGE_MOUNTED:
            LogUtils.i(TAG, "<onEvent> EVENT_STORAGE_MOUNTED");
            // remove error dialog after sd card mounted
            removeOldFragmentByTag(ErrorHandle.ERROR_DIALOG_TAG);
            break;
        default:
            LogUtils.i(TAG, "<onEvent> event out of range, event code = " + eventCode);
            break;
        }
    }

    @Override
    public void onStateChanged(int stateCode) {
        LogUtils.i(TAG, "<onStateChanged> change from " + mCurrentState + " to " + stateCode);
        //renxinquan edit start GPBYL-922
        if(true){
       // if (!mRunFromLauncher) {
        //renxinquan edit end GPBYL-922
            if (stateCode == SoundRecorderService.STATE_RECORDING) {
                acquireWakeLock();
            } else {
                releaseWakeLock();
            }
        }
        mCurrentState = stateCode;
        // when state changes to IDLE, update timer view,
        // for hiding the 1s gap between the timer and the real recording length
        if (mCurrentState == SoundRecorderService.STATE_IDLE && mService.isCurrentFileWaitToSave()) {
            int time = mService.getCurrentProgressInSecond();
            String timerString = String.format(mTimerFormat, time / TIME_BASE, time % TIME_BASE);
            setTimerViewTextSize(time);
            mTimerTextView.setText(timerString);
        }
        updateOptionsMenu();
        updateUi();
    }

    @Override
    public void onError(int errorCode) {
        LogUtils.i(TAG, "<onError> errorCode = " + errorCode);
        // M: if OnSaveInstanceState has run, we do not show Dialogfragment now,
        // or else FragmentManager will throw IllegalStateException
        if (mOnSaveInstanceStateHasRun == false) {
            ErrorHandle.showErrorInfo(this, errorCode);
        }
    }

    /**
     * Handle the button
     * 
     * @param button
     *            which button has been clicked
     */
    public void onClick(View button) {
        if (!button.isEnabled()) {
            return;
        }
        LogUtils.i(TAG, "<onClick> Activity = " + this.toString());
        mHandler.removeCallbacks(mUpdateTimer);
        switch (button.getId()) {
        case R.id.recordButton:
            LogUtils.i(TAG, "<onClick> recordButton");
            onClickRecordButton();
            break;
        case R.id.playButton:
            LogUtils.i(TAG, "<onClick> playButton");
            onClickPlayButton();
            break;
        case R.id.stopButton:
            LogUtils.i(TAG, "<onClick> stopButton");
            onClickStopButton();
            break;
        case R.id.acceptButton:
            LogUtils.i(TAG, "<onClick> acceptButton");
            onClickAcceptButton();
            break;
        case R.id.discardButton:
            LogUtils.i(TAG, "<onClick> discardButton");
            onClickDiscardButton();
            break;
        case R.id.fileListButton:
            onClickFileListButton();
            break;
        case R.id.pauseRecordingButton:
            LogUtils.i(TAG, "<onClick> pauseRecordingButton");
            onClickPauseRecordingButton();
            break;
        default:
            break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogUtils.i(TAG, "<onCreate> start");
        LogUtils.i(TAG, "<onCreate> Activity = " + this.toString());
        super.onCreate(savedInstanceState);
        /**
         * M: process the string of menu item too long
         */
	//Add GPBYB-57 20130809 (on)
	setVolumeControlStream(AudioManager.STREAM_MUSIC);
	//Add GPBYB-57 20130809 (off)
        addOptionsMenuInflaterFactory();

        // init
        setContentView(R.layout.main);
        if (!initFromIntent()) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        //renxinquan edit start GPBYL-922
        if(true){
        //if (!mRunFromLauncher) {
        //renxinquan edit end GPBYL-922	
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            LogUtils.e(TAG, "<onCreate> PowerManager == " + pm);
            if (pm != null) {
                mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
            }
        }
        restoreRecordParamsSettings();
        LogUtils.i(TAG, "<onCreate> end");
    }

    @Override
    protected void onResume() {
        LogUtils.i(TAG, "<onResume> start mRunFromLauncher = " + mRunFromLauncher);
        LogUtils.i(TAG, "<onResume> Activity = " + this.toString());
        mOnSaveInstanceStateHasRun = false;
        super.onResume();

        if (mService == null) {
            // start service
            LogUtils.i(TAG, "<onResume> start service");
            if (null == startService(new Intent(SoundRecorder.this, SoundRecorderService.class))) {
                LogUtils.e(TAG, "<onResume> fail to start service");
                finish();
                return;
            }

            // bind service
            LogUtils.i(TAG, "<onResume> bind service");
            if (!bindService(new Intent(SoundRecorder.this, SoundRecorderService.class),
                    mServiceConnection, BIND_AUTO_CREATE)) {
                LogUtils.e(TAG, "<onResume> fail to bind service");
                finish();
                return;
            }
            
            // M: reset ui to initial state, or else the UI may be abnormal before service not bind
            resetUi();
        } else {
            // M: when switch SoundRecorder and RecordingFileList quickly, it's
            // possible that onStop was not been called,
            // but onResume is called, in this case, mService has not been
            // unbind, so mService != null
            // but we still should do some initial operation, such as play
            // recording file which select from RecordingFileList
            initWhenHaveService();
        }

        LogUtils.i(TAG, "<onResume> end");
    }

    @Override
    public void onBackPressed() {
        LogUtils.i(TAG, "<onBackPressed> start");
        LogUtils.i(TAG, "<onBackPressed> Activity = " + this.toString());
        mBackPressed = true;
		//GPBYL-261 liyang 20130517 (on)
		mRunFromLauncher = false;
		//GPBYL-261 liyang 20130517 (off)
        if (!mRunFromLauncher) {
            if (mService != null) {
                if (mCurrentState == SoundRecorderService.STATE_RECORDING) {
                    mService.stopRecord();
                }
                if (mService.isCurrentFileWaitToSave()) {
                    LogUtils.i(TAG, "<onBackPressed> mService.saveRecord()");
                    mService.saveRecord();
                } else {
                    // M: if not call saveRecord, we finish activity by ourself
                    finish();
                }
            } else {
                // M: if not call saveRecord, we finish activity by ourself
                finish();
            }
        } else {
            // M: if run from launcher, we do not run other operation when back
            // key pressed
            super.onBackPressed();
        }
        LogUtils.i(TAG, "<onBackPressed> end");
    }

    @Override
    protected void onPause() {
        LogUtils.i(TAG, "<onPause> start");
        LogUtils.i(TAG, "<onPause> Activity = " + this.toString());
        if (!mBackPressed && mService != null && !mRunFromLauncher) {
            if (mCurrentState == SoundRecorderService.STATE_RECORDING) {
                mService.stopRecord();
            }
            if (mService.isCurrentFileWaitToSave()) {
                LogUtils.i(TAG, "<onPause> mService.saveRecord()");
                mService.saveRecord();
            }
        }
	//GPBYL-258 liyang 20130520 (on)
	onClickStopButton();
	//GPBYL-258 liyang 20130520 (off)
        mBackPressed = false;
        super.onPause();
        LogUtils.i(TAG, "<onPause> end");
    }

    @Override
    protected void onStop() {
        LogUtils.i(TAG, "<onStop> start");
        LogUtils.i(TAG, "<onStop> Activity = " + this.toString());
        mHandler.removeCallbacks(mUpdateTimer);
        if (mRunFromLauncher && mService != null) {

            boolean stopService = (mCurrentState == SoundRecorderService.STATE_IDLE)
                    && !mService.isCurrentFileWaitToSave();

            // M: if another instance of soundrecorder has been resume,
            // the listener of service has changed to another instance, so we
            // cannot call setAllListenerSelf
            boolean isListener = mService.isListener(SoundRecorder.this);
            LogUtils.i(TAG, "<onStop> isListener = " + isListener);
            if (isListener) {
                // set listener of service as default,
                // so when error occurs, service can show error info in toast
                mService.setAllListenerSelf();
            }

            LogUtils.i(TAG, "<onStop> unbind service");
            unbindService(mServiceConnection);

            if (stopService && isListener) {
                LogUtils.i(TAG, "<onStop> stop service");
                stopService(new Intent(SoundRecorder.this, SoundRecorderService.class));
            }
            mService = null;
        }
        hideStorageHint();
        super.onStop();
        LogUtils.i(TAG, "<onStop> end");
    }

    @Override
    protected void onDestroy() {
        LogUtils.i(TAG, "<onDestroy> start");
        LogUtils.i(TAG, "<onDestroy> Activity = " + this.toString());
        if (mRunFromLauncher) {
            storeRecordParamsSettings();
        } else {
            releaseWakeLock();
        }
        super.onDestroy();
        LogUtils.i(TAG, "<onDestroy> end");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        LogUtils.i(TAG, "<onSaveInstanceState> start");
        mOnSaveInstanceStateHasRun = true;
        storeRecordParamsSettings();
        super.onSaveInstanceState(outState);
        LogUtils.i(TAG, "<onSaveInstanceState> end");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        LogUtils.i(TAG, "<onRestoreInstanceState> start");
        super.onRestoreInstanceState(savedInstanceState);
        restoreDialogFragment();
        restoreRecordParamsSettings();
        LogUtils.i(TAG, "<onRestoreInstanceState> end");
    }

    @Override
    /**
     * M: add option menu to select record mode and format
     */
    public boolean onCreateOptionsMenu(Menu menu) {
        LogUtils.i(TAG, "<onCreateOptionsMenu> begin");
        super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = new MenuInflater(getApplicationContext());
        menuInflater.inflate(R.menu.option_menu, menu);
        mMenu = menu;
        LogUtils.i(TAG, "<onCreateOptionsMenu> end");
        return true;
    }

    @Override
    /**
     * M: add option menu to select record mode and format
     */
    public boolean onPrepareOptionsMenu(Menu menu) {
        LogUtils.i(TAG, "<onPrepareOptionsMenu> start");
        super.onPrepareOptionsMenu(menu);
        mMenu = menu;
        updateOptionsMenu();
        LogUtils.i(TAG, "<onPrepareOptionsMenu> end");
        return true;
    }

    @Override
    /**
     * M: add option menu to select record mode and format,
     * when select one item, show corresponding dialog
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (R.id.record_format == id) {
            showDialogFragment(DIALOG_SELECT_FORMAT, null);
        } else if (R.id.record_mode == id) {
            showDialogFragment(DIALOG_SELECT_MODE, null);
        } else if (R.id.record_effect == id) {
            mSelectEffectArrayTemp = mSelectEffectArray.clone();
            showDialogFragment(DIALOG_SELECT_EFFECT, null);
        }
        return true;
    }

    @Override
    /**
     * M: add process of view focus, and save configuration change state when 
     * SoundRecorder is started by other application
     */
    public void onConfigurationChanged(Configuration newConfig) {
        LogUtils.i(TAG, "<onConfigurationChanged> start");
        super.onConfigurationChanged(newConfig);
        View viewFocus = this.getCurrentFocus();
        int viewId = -1;
        if (null != viewFocus) {
            viewId = viewFocus.getId();
        }
        setContentView(R.layout.main);
        
        if (null != mService) {
            initResourceRefs();
            updateUi();
        }
        
        if (viewId >= 0) {
            View view = findViewById(viewId);
            if (null != view) {
                view.setFocusable(true);
                view.requestFocus();
            }
        }
        LogUtils.i(TAG, "<onConfigurationChanged> end");
    }

    @Override
    /**
     * M: do record or play operation after press record 
     * or press one record item in RecordingFileList
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtils.i(TAG, "<onActivityResult> start");
        if (RESULT_OK != resultCode) {
            LogUtils.i(TAG, "<onActivityResult> resultCode != RESULT_OK, return");
            return;
        }
        if(mService != null && mFileListButton != null) {
            mFileListButton.setEnabled(true);
        }
        Intent intent = data;
        Bundle bundle = intent.getExtras();
        if (null == bundle) {
            LogUtils.i(TAG, "<onActivityResult> bundle == null, return");
            return;
        }
        mDoWhat = bundle.getString(DOWHAT);
        if (null != mDoWhat) {
            if (mDoWhat.equals(PLAY)) {
                if (null != intent.getExtras() && null != intent.getExtras().getString(PATH)) {
                    mDoWhatFilePath = intent.getExtras().getString(PATH);
                }
            }
        }
        // later, after mService connected, play/record
        LogUtils.i(TAG, "<onActivityResult> end");
    }

    private void initWhenHaveService() {
        LogUtils.i(TAG, "<initWhenHaveService> start");
        mService.setErrorListener(SoundRecorder.this);
        mService.setEventListener(SoundRecorder.this);
        mService.setStateChangedListener(SoundRecorder.this);
        mService.setShowNotification(mRunFromLauncher);
        mCurrentState = mService.getCurrentState();
        LogUtils.i(TAG, "<initWhenHaveService> mCurrentState = " + mCurrentState);
        initResourceRefs();
        // M: if run from other application, we will stop recording and auto
        // save the recording file
        // and reset SoundRecorder to innitial state
        if (!mRunFromLauncher) {
            if (mCurrentState == SoundRecorderService.STATE_RECORDING
                    || mCurrentState == SoundRecorderService.STATE_PAUSE_RECORDING) {
                LogUtils.i(TAG, "<initWhenHaveService> stop record when run from other ap");
                mService.stopRecord();
            }
            if (mService.isCurrentFileWaitToSave()) {
                // M: set mSetResultAfterSave = false ,and set
                // mSetResultAfterSave = true in onEvent
                mSetResultAfterSave = false;
                LogUtils.i(TAG, "<initWhenHaveService> save record when run from other ap");
                mService.saveRecord();
            } else {
                mService.reset();
            }
        }
        updateUi();
        updateOptionsMenu();
        // do action that need to bo in onActivityResult
        if (RECORD.equals(mDoWhat)) {
            onClickRecordButton();
        } else if (PLAY.equals(mDoWhat)) {
            mService.playFile(mDoWhatFilePath);
        }
        mDoWhat = null;
        mDoWhatFilePath = null;
        LogUtils.i(TAG, "<initWhenHaveService> end");
    }
    
    /**
     * Whenever the UI is re-created (due f.ex. to orientation change) we have
     * to reinitialize references to the views.
     */
    private void initResourceRefs() {
        LogUtils.i(TAG, "<initResourceRefs> start");
        initResourceRefsWhenNoService();
        /**
         * M: set related property according to if SoundRecorder is started by
         * launcher @{
         */
        if (mRunFromLauncher) {
            mPlayButton.setOnClickListener(this);
            mFileListButton.setOnClickListener(this);
            mPauseRecordingButton.setOnClickListener(this);
        } else {
            mPlayButton.setVisibility(View.GONE);
            mFileListButton.setVisibility(View.GONE);
            mPauseRecordingButton.setVisibility(View.GONE);
        }
        /** @} */
        mRecordButton.setOnClickListener(this);
        mStopButton.setOnClickListener(this);
        mAcceptButton.setOnClickListener(this);
        mDiscardButton.setOnClickListener(this);
        setTitle(getResources().getString(R.string.app_name));
        mVUMeter.setRecorder(mService.getRecorder());
        String timerString = String.format(mTimerFormat, 0, 0);
        setTimerViewTextSize(0);
        mTimerTextView.setText(timerString);
        LogUtils.i(TAG, "<initResourceRefs> end");
    }

    /**
     * init state when onCreate
     * 
     * @return whether success when init
     */
    private boolean initFromIntent() {
        LogUtils.i(TAG, "<initFromIntent> start");
        Intent i = getIntent();
        if (null != i) {
            LogUtils.i(TAG, "<initFromIntent> Intent is " + i.toString());
            /**
             * M: check if SoundRecorder is start by launcher or start by
             * SoundRecorderServicef
             */
            mRunFromLauncher = i.getAction().equals(INTENT_ACTION_MAIN)
                    || i.getAction().equals("com.android.soundrecorder.SoundRecorder");
            String typeString = i.getType();
            if (null != typeString) {
                if (RecordParamsSetting.isAvailableRequestType(typeString)) {
                    mRequestedType = typeString;
                } else {
                    LogUtils.i(TAG, "<initFromIntent> return false");
                    return false;
                }
            }
            mMaxFileSize = i.getLongExtra(EXTRA_MAX_BYTES, MAX_FILE_SIZE_NULL);
            /** M: if mMaxFileSize != -1, set mHasFileSizeLimitation as true. */
            mHasFileSizeLimitation = (mMaxFileSize != MAX_FILE_SIZE_NULL);
        }
        LogUtils.i(TAG, "<initFromIntent> end");
        return true;
    }

    /**
     * show dialog use DialogFragment
     * 
     * @param id
     *            the flag of dialog
     * @param args
     *            the parameters of create dialog
     * 
     *            M: use DialogFragment to show dialog, for showDialog() is
     *            deprecated in current version
     */
    private void showDialogFragment(int id, Bundle args) {
        LogUtils.i(TAG, "<showDialogFragment> start");
        DialogFragment newFragment = null;
        FragmentManager fragmentManager = getFragmentManager();
        switch (id) {
        case DIALOG_SELECT_FORMAT:
            removeOldFragmentByTag(DIALOG_TAG_SELECT_FORMAT);
            newFragment = SelectDialogFragment.newInstance(RecordParamsSetting
                    .getFormatStringIDArray(), RecordParamsSetting.getFormatSuffixStringArray(),
                    R.string.select_voice_quality, true, mSelectedFormat, null);
            ((SelectDialogFragment) newFragment).setOnClickListener(mSelectFormatListener);
            newFragment.show(fragmentManager, DIALOG_TAG_SELECT_FORMAT);
            LogUtils.i(TAG, "<showDialogFragment> show select format dialog");
            break;
        case DIALOG_SELECT_MODE:
            removeOldFragmentByTag(DIALOG_TAG_SELECT_MODE);
            newFragment = SelectDialogFragment.newInstance(RecordParamsSetting
                    .getModeStringIDArray(), null, R.string.select_recording_mode, true,
                    mSelectedMode, null);
            ((SelectDialogFragment) newFragment).setOnClickListener(mSelectModeListener);
            newFragment.show(fragmentManager, DIALOG_TAG_SELECT_MODE);
            LogUtils.i(TAG, "<showDialogFragment> show select mode dialog");
            break;
        case DIALOG_SELECT_EFFECT:
            removeOldFragmentByTag(DIALOG_TAG_SELECT_EFFECT);
            newFragment = SelectDialogFragment.newInstance(RecordParamsSetting
                    .getEffectStringIDArray(), null, R.string.select_recording_effect, false, 0,
                    mSelectEffectArray);
            ((SelectDialogFragment) newFragment).setOnClickListener(mSelectEffectOkListener);
            ((SelectDialogFragment) newFragment)
                    .setOnMultiChoiceListener(mSelectEffectMultiChoiceClickListener);
            newFragment.show(fragmentManager, DIALOG_TAG_SELECT_EFFECT);
            break;
        default:
            break;
        }
        fragmentManager.executePendingTransactions();
        LogUtils.i(TAG, "<showDialogFragment> end");
    }

    /**
     * remove old DialogFragment
     * 
     * @param tag
     *            the tag of DialogFragment to be removed
     */
    private void removeOldFragmentByTag(String tag) {
        LogUtils.i(TAG, "<removeOldFragmentByTag> start");
        FragmentManager fragmentManager = getFragmentManager();
        DialogFragment oldFragment = (DialogFragment) fragmentManager.findFragmentByTag(tag);
        LogUtils.i(TAG, "<removeOldFragmentByTag> oldFragment = " + oldFragment);
        if (null != oldFragment) {
            oldFragment.dismissAllowingStateLoss();
            LogUtils.i(TAG, "<removeOldFragmentByTag> remove oldFragment success");
        }
        LogUtils.i(TAG, "<removeOldFragmentByTag> end");
    }

    /**
     * set current record mode when user select an item in SelectDialogFragment
     * 
     * @param mode
     *            mode to be set
     */
    public void setSelectedMode(int which) {
        mSelectedMode = RecordParamsSetting.getSelectMode(which);
        LogUtils.i(TAG, "<setSelectedMode> mSelectedMode = " + mSelectedMode);
    }

    /**
     * set current record format when user select an item in
     * SelectDialogFragment
     * 
     * @param which
     *            which format has selected
     */
    public void setSelectedFormat(int which) {
        mSelectedFormat = RecordParamsSetting.getSelectFormat(which);
        LogUtils.i(TAG, "<setSelectedFormat> mSelectedFormat = " + mSelectedFormat);
    }

    /**
     * M: reset the UI to initial state when mService is not available, only
     * used in onResume
     */
    private void resetUi() {
        initResourceRefsWhenNoService();
        setTitle(getResources().getString(R.string.app_name));
        mRecordButton.setEnabled(true);
        mRecordButton.setFocusable(true);
        mRecordButton.setSoundEffectsEnabled(true);
        mStopButton.setEnabled(false);
        mStopButton.setFocusable(false);
        mButtonParent.setWeightSum(THREE_BUTTON_WEIGHT_SUM);
        mPlayButton.setVisibility(View.VISIBLE);
        mPlayButton.setEnabled(false);
        mPlayButton.setFocusable(false);
        mStopButton.setVisibility(View.GONE);
        mFileListButton.setVisibility(View.VISIBLE);
        mFileListButton.setEnabled(true);
        mFileListButton.setFocusable(true);
        mPauseRecordingButton.setVisibility(View.GONE);
        mPauseRecordingButton.setSoundEffectsEnabled(false);
        mRecordButton.requestFocus();
        mRemainingTimeTextView.setVisibility(View.INVISIBLE);
        mRecordingStateImageView.setVisibility(View.INVISIBLE);
        mStateTextView.setVisibility(View.INVISIBLE);
        mPlayingStateImageView.setVisibility(View.GONE);
        mRecordingFileNameTextView.setVisibility(View.INVISIBLE);
        mExitButtons.setVisibility(View.INVISIBLE);
        mVUMeter.setVisibility(View.VISIBLE);
        mVUMeter.mCurrentAngle = 0;
        mStateProgressBar.setVisibility(View.INVISIBLE);
        String timerString = String.format(mTimerFormat, 0, 0);
        setTimerViewTextSize(0);
        mTimerTextView.setText(timerString);
    }
    
    private void initResourceRefsWhenNoService() {
        mRecordButton = (ImageButton) findViewById(R.id.recordButton);
        mStopButton = (ImageButton) findViewById(R.id.stopButton);
        mPlayButton = (ImageButton) findViewById(R.id.playButton);
        mFileListButton = (ImageButton) findViewById(R.id.fileListButton);
        mPauseRecordingButton = (ImageButton) findViewById(R.id.pauseRecordingButton);
        mButtonParent = (LinearLayout) findViewById(R.id.buttonParent);
        mRecordingStateImageView = (ImageView) findViewById(R.id.stateLED);
        mRemainingTimeTextView = (TextView) findViewById(R.id.stateMessage1);
        mStateTextView = (TextView) findViewById(R.id.stateMessage2);
        mStateProgressBar = (ProgressBar) findViewById(R.id.stateProgressBar);
        mTimerTextView = (TextView) findViewById(R.id.timerView);
        mPlayingStateImageView = (ImageView) findViewById(R.id.currState);
        mRecordingFileNameTextView = (TextView) findViewById(R.id.recordingFileName);
        mExitButtons = (LinearLayout) findViewById(R.id.exitButtons);
        mVUMeter = (VUMeter) findViewById(R.id.uvMeter);
        mTimerFormat = getResources().getString(R.string.timer_format);
        mAcceptButton = (Button) findViewById(R.id.acceptButton);
        mDiscardButton = (Button) findViewById(R.id.discardButton);
        mFirstLine = (ImageView) findViewById(R.id.firstLine);
        mSecondLine = (ImageView) findViewById(R.id.secondLine);
        mFrameLayout = (FrameLayout) findViewById(R.id.frameLayout);
    }

    /**
     * Shows/hides the appropriate child views for the new state. M: use
     * different function in different state to update UI
     */
    public void updateUi() {
        LogUtils.i(TAG, "<updateUi> start");
        String filePath = mService.getCurrentFilePath();
        LogUtils.i(TAG, "<updateUi> mService.getCurrentFilePath() = " + filePath);
        mFileName = NULL_STRING;
        if (null != filePath) {
            mFileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1, filePath
                    .length());
            mFileName = (mFileName.endsWith(Recorder.SAMPLE_SUFFIX)) ? mFileName.substring(0,
                    mFileName.lastIndexOf(Recorder.SAMPLE_SUFFIX)) : mFileName;
        }
        LogUtils.i(TAG, "<updateUi> mRecordingFileNameTextView.setText : " + mFileName);
        mRecordingFileNameTextView.setText(mFileName);
        mAcceptButton.setText(R.string.accept);
        if (mRunFromLauncher) {
            mAcceptButton.setText(R.string.save_record);
        }
        hideStorageHint();
        switch (mService.getCurrentState()) {
        case SoundRecorderService.STATE_IDLE:
            updateUiOnIdleState();
            break;
        case SoundRecorderService.STATE_PAUSE_PLAYING:
            updateUiOnPausePlayingState();
            break;
        case SoundRecorderService.STATE_RECORDING:
            updateUiOnRecordingState();
            break;
        case SoundRecorderService.STATE_PAUSE_RECORDING:
            updateUiOnPauseRecordingState();
            break;
        case SoundRecorderService.STATE_PLAYING:
            updateUiOnPlayingState();
            break;
        default:
            break;
        }
        mVUMeter.invalidate();
        LogUtils.i(TAG, "<updateUi> end");
    }

    /**
     * M: Update UI on idle state
     */
    private void updateUiOnIdleState() {
        LogUtils.i(TAG, "<updateUiOnIdleState> start");
        String currentFilePath = mService.getCurrentFilePath();
        mStateProgressBar.setProgress(0);
        mHandler.removeCallbacks(mUpdateTimer);
        if (currentFilePath == null) {
            mFrameLayout.setBackgroundColor(getResources().getColor(R.color.frameLayoutGrayColor));
            mRecordButton.setEnabled(true);
            mRecordButton.setFocusable(true);
            mRecordButton.setSoundEffectsEnabled(true);
            mStopButton.setEnabled(false);
            mStopButton.setFocusable(false);
            if (mRunFromLauncher) {
                mButtonParent.setWeightSum(THREE_BUTTON_WEIGHT_SUM);
                mPlayButton.setVisibility(View.VISIBLE);
                mPlayButton.setEnabled(false);
                mPlayButton.setFocusable(false);
                mStopButton.setVisibility(View.GONE);
                mFileListButton.setVisibility(View.VISIBLE);
                mFileListButton.setEnabled(true);
                mFileListButton.setFocusable(true);
                mPauseRecordingButton.setVisibility(View.GONE);
                mPauseRecordingButton.setSoundEffectsEnabled(false);
            } else {
                mButtonParent.setWeightSum(TWO_BUTTON_WEIGHT_SUM);
                mPlayButton.setVisibility(View.GONE);
                mStopButton.setVisibility(View.VISIBLE);
                mStopButton.setEnabled(false);
                mStopButton.setFocusable(false);
                mFileListButton.setVisibility(View.GONE);
                mPauseRecordingButton.setVisibility(View.GONE);
            }
            mRecordButton.requestFocus();
            mRemainingTimeTextView.setVisibility(View.INVISIBLE);
            mRecordingStateImageView.setVisibility(View.INVISIBLE);
            mStateTextView.setVisibility(View.INVISIBLE);
            mPlayingStateImageView.setVisibility(View.GONE);
            mRecordingFileNameTextView.setVisibility(View.INVISIBLE);
            mExitButtons.setVisibility(View.INVISIBLE);
            mFirstLine.setVisibility(View.INVISIBLE);
            mSecondLine.setVisibility(View.VISIBLE);
            mVUMeter.setVisibility(View.VISIBLE);
            mVUMeter.mCurrentAngle = 0;
            mStateProgressBar.setVisibility(View.INVISIBLE);
            String timerString = String.format(mTimerFormat, 0, 0);
            setTimerViewTextSize(0);
            mTimerTextView.setText(timerString);
        } else {
            mRecordButton.setEnabled(true);
            mRecordButton.setFocusable(true);
            mRecordButton.setSoundEffectsEnabled(true);
            mButtonParent.setWeightSum(TWO_BUTTON_WEIGHT_SUM);
            if (mRunFromLauncher) {
                mButtonParent.setWeightSum(THREE_BUTTON_WEIGHT_SUM);
                mPlayButton.setVisibility(View.VISIBLE);
                mPlayButton.setEnabled(true);
                mPlayButton.setFocusable(true);
                mPlayButton.setImageResource(R.drawable.play);
                mStopButton.setVisibility(View.VISIBLE);
                mFileListButton.setVisibility(View.GONE);
                mPauseRecordingButton.setVisibility(View.GONE);
                mPauseRecordingButton.setSoundEffectsEnabled(false);
            }
            mStopButton.setEnabled(false);
            mStopButton.setFocusable(false);
            mRemainingTimeTextView.setVisibility(View.INVISIBLE);
            mRecordingStateImageView.setVisibility(View.INVISIBLE);
            mStateTextView.setVisibility(View.INVISIBLE);
            mPlayingStateImageView.setImageResource(R.drawable.stop);
            mPlayingStateImageView.setVisibility(View.VISIBLE);
            mRecordingFileNameTextView.setVisibility(View.VISIBLE);
            if (mService.isCurrentFileWaitToSave()) {
                mFrameLayout.setBackgroundColor(getResources().getColor(R.color.blackColor));
                mSecondLine.setVisibility(View.INVISIBLE);
                mFirstLine.setVisibility(View.VISIBLE);
                mExitButtons.setVisibility(View.VISIBLE);
            } else {
                mFrameLayout.setBackgroundColor(getResources().getColor(
                        R.color.frameLayoutGrayColor));
                mSecondLine.setVisibility(View.VISIBLE);
                mExitButtons.setVisibility(View.INVISIBLE);
                mFirstLine.setVisibility(View.INVISIBLE);
                mStopButton.setVisibility(View.GONE);
                mFileListButton.setVisibility(View.VISIBLE);
            }
            mVUMeter.setVisibility(View.INVISIBLE);
            mStateProgressBar.setVisibility(View.INVISIBLE);
            int time = (int) mService.getCurrentFileDurationInSecond();
            String timerString = String.format(mTimerFormat, time / TIME_BASE, time % TIME_BASE);
            setTimerViewTextSize(time);
            mTimerTextView.setText(timerString);
        }
        LogUtils.i(TAG, "<updateUiOnIdleState> end");
    }

    /**
     * M: Update UI on pause playing state
     */
    private void updateUiOnPausePlayingState() {
        LogUtils.i(TAG, "<updateUiOnPausePlayingState> start");
        mHandler.removeCallbacks(mUpdateTimer);
        mRecordButton.setEnabled(true);
        mRecordButton.setFocusable(true);
        mRecordButton.setSoundEffectsEnabled(true);
        mPlayButton.setVisibility(View.VISIBLE);
        mPlayButton.setEnabled(true);
        mPlayButton.setFocusable(true);
        mPlayButton.setImageResource(R.drawable.play);
        mStopButton.setVisibility(View.VISIBLE);
        mFileListButton.setVisibility(View.GONE);
        mPauseRecordingButton.setVisibility(View.GONE);
        mPauseRecordingButton.setSoundEffectsEnabled(false);
        mStopButton.setEnabled(true);
        mStopButton.setFocusable(true);
        mRemainingTimeTextView.setVisibility(View.INVISIBLE);
        mRecordingStateImageView.setVisibility(View.INVISIBLE);
        mStateTextView.setVisibility(View.INVISIBLE);
        mPlayingStateImageView.setImageResource(R.drawable.pause);
        mPlayingStateImageView.setVisibility(View.VISIBLE);
        mRecordingFileNameTextView.setVisibility(View.VISIBLE);
        if (!mService.isCurrentFileWaitToSave()) {
            mExitButtons.setVisibility(View.INVISIBLE);
            mFirstLine.setVisibility(View.INVISIBLE);
            mSecondLine.setVisibility(View.VISIBLE);
        } else {
            mExitButtons.setVisibility(View.VISIBLE);
            mFirstLine.setVisibility(View.VISIBLE);
            mSecondLine.setVisibility(View.INVISIBLE);
        }
        int time = (int) mService.getCurrentProgressInSecond();
        String timerString = String.format(mTimerFormat, time / TIME_BASE, time % TIME_BASE);
        setTimerViewTextSize(time);
        mTimerTextView.setText(timerString);
        mStateProgressBar.setVisibility(View.VISIBLE);
        mStateProgressBar.setProgress((int) (100 
                * mService.getCurrentProgressInMillSecond() 
                / mService.getCurrentFileDurationInMillSecond()));
        mVUMeter.setVisibility(View.INVISIBLE);
        LogUtils.i(TAG, "<updateUiOnPausePlayingState> end");
    }

    /**
     * M: Update UI on recording state
     */
    private void updateUiOnRecordingState() {
        LogUtils.i(TAG, "<updateUiOnRecordingState> start");
        mFrameLayout.setBackgroundColor(getResources().getColor(R.color.frameLayoutGrayColor));
        Resources res = getResources();
        mRecordButton.setEnabled(false);
        mRecordButton.setFocusable(false);
        mRecordButton.setSoundEffectsEnabled(true);
        mStopButton.setVisibility(View.VISIBLE);
        mStopButton.setEnabled(true);
        mStopButton.setFocusable(true);
        if (mRunFromLauncher) {
            mButtonParent.setWeightSum(THREE_BUTTON_WEIGHT_SUM);
            mPlayButton.setEnabled(false);
            mPlayButton.setFocusable(false);
            mPlayButton.setVisibility(View.GONE);
            mFileListButton.setVisibility(View.GONE);
            mPauseRecordingButton.setVisibility(View.VISIBLE);
            mPauseRecordingButton.setEnabled(true);
            mPauseRecordingButton.setFocusable(true);
            mPauseRecordingButton.setSoundEffectsEnabled(false);
        } else {
            mButtonParent.setWeightSum(TWO_BUTTON_WEIGHT_SUM);
            mPlayButton.setVisibility(View.GONE);
            mFileListButton.setVisibility(View.GONE);
            mPauseRecordingButton.setVisibility(View.GONE);
        }
        mRemainingTimeTextView.setText(EMPTY);
        mRemainingTimeTextView.setVisibility(View.VISIBLE);
        mRecordingStateImageView.setVisibility(View.VISIBLE);
        mRecordingStateImageView.setImageResource(R.drawable.recording_led);
        mStateTextView.setVisibility(View.VISIBLE);
        mStateTextView.setText(res.getString(R.string.recording));
        mPlayingStateImageView.setVisibility(View.GONE);
        mRecordingFileNameTextView.setVisibility(View.VISIBLE);
        mExitButtons.setVisibility(View.INVISIBLE);
        mFirstLine.setVisibility(View.INVISIBLE);
        mSecondLine.setVisibility(View.VISIBLE);
        mVUMeter.setVisibility(View.VISIBLE);
        mStateProgressBar.setVisibility(View.INVISIBLE);
        mHandler.postDelayed(mUpdateTimer, UPDATE_TIMER_VIRE_POST_DELAYED);
        LogUtils.i(TAG, "<updateUiOnRecordingState> end");
    }

    /**
     * M: Update UI on pause Recording state
     */
    private void updateUiOnPauseRecordingState() {
        LogUtils.i(TAG, "<updateUiOnPauseRecordingState> start");
        mHandler.removeCallbacks(mUpdateTimer);
        Resources res = getResources();
        mRecordButton.setEnabled(true);
        mRecordButton.setFocusable(true);
        mRecordButton.setSoundEffectsEnabled(false);
        mPlayButton.setVisibility(View.GONE);
        mFileListButton.setVisibility(View.GONE);
        mPauseRecordingButton.setVisibility(View.VISIBLE);
        mPauseRecordingButton.setEnabled(false);
        mPauseRecordingButton.setFocusable(false);
        mPauseRecordingButton.setSoundEffectsEnabled(false);
        mStopButton.setVisibility(View.VISIBLE);
        mStopButton.setEnabled(true);
        mStopButton.setFocusable(true);
        mRemainingTimeTextView.setVisibility(View.INVISIBLE);
        mRecordingStateImageView.setVisibility(View.VISIBLE);
        mRecordingStateImageView.setImageResource(R.drawable.idle_led);
        mStateTextView.setVisibility(View.VISIBLE);
        mStateTextView.setText(res.getString(R.string.recording_paused));
        mPlayingStateImageView.setVisibility(View.GONE);
        mRecordingFileNameTextView.setVisibility(View.VISIBLE);
        mExitButtons.setVisibility(View.INVISIBLE);
        mFirstLine.setVisibility(View.INVISIBLE);
        mSecondLine.setVisibility(View.VISIBLE);
        mVUMeter.setVisibility(View.VISIBLE);
        mVUMeter.mCurrentAngle = 0;
        mStateProgressBar.setVisibility(View.INVISIBLE);
        int time = (int) mService.getCurrentProgressInSecond();
        String timerString = String.format(mTimerFormat, time / TIME_BASE, time % TIME_BASE);
        setTimerViewTextSize(time);
        mTimerTextView.setText(timerString);
        LogUtils.i(TAG, "<updateUiOnPauseRecordingState> end");
    }

    /**
     * M: Update UI on playing state
     */
    private void updateUiOnPlayingState() {
        LogUtils.i(TAG, "<updateUiOnPlayingState> start");
        mRecordButton.setEnabled(true);
        mRecordButton.setFocusable(true);
        mRecordButton.setSoundEffectsEnabled(true);
        mPlayButton.setVisibility(View.VISIBLE);
        mPlayButton.setEnabled(true);
        mPlayButton.setFocusable(true);
        mPlayButton.setImageResource(R.drawable.pause);
        mFileListButton.setVisibility(View.GONE);
        mPauseRecordingButton.setVisibility(View.GONE);
        mPauseRecordingButton.setSoundEffectsEnabled(false);
        mStopButton.setVisibility(View.VISIBLE);
        mStopButton.setEnabled(true);
        mStopButton.setFocusable(true);
        mRemainingTimeTextView.setVisibility(View.INVISIBLE);
        mRecordingStateImageView.setVisibility(View.INVISIBLE);
        mStateTextView.setVisibility(View.INVISIBLE);
        mPlayingStateImageView.setVisibility(View.VISIBLE);
        mPlayingStateImageView.setImageResource(R.drawable.play);
        mRecordingFileNameTextView.setVisibility(View.VISIBLE);
        if (!mService.isCurrentFileWaitToSave()) {
            mExitButtons.setVisibility(View.INVISIBLE);
            mFirstLine.setVisibility(View.INVISIBLE);
            mSecondLine.setVisibility(View.VISIBLE);
        } else {
            mFrameLayout.setBackgroundColor(getResources().getColor(R.color.blackColor));
            mExitButtons.setVisibility(View.VISIBLE);
            mFirstLine.setVisibility(View.VISIBLE);
            mSecondLine.setVisibility(View.INVISIBLE);
        }
        mVUMeter.setVisibility(View.INVISIBLE);
        mStateProgressBar.setVisibility(View.VISIBLE);
        mHandler.postDelayed(mUpdateTimer, UPDATE_TIMER_VIRE_POST_DELAYED);
        LogUtils.i(TAG, "<updateUiOnPlayingState> end");
    }

    /**
     * process after click record button
     */
    void onClickRecordButton() {
        if (mService != null) {
            mService.record(RecordParamsSetting.getRecordParams(mRequestedType, mSelectedFormat,
                    mSelectedMode, mSelectEffectArray), (int) mMaxFileSize);
        }
        long mEndRecordingTime = System.currentTimeMillis();
        Log.i(TAG, "[Performance test][SoundRecorder] recording end [" + mEndRecordingTime + "]");
    }

    /**
     * process after click play button
     */
    void onClickPlayButton() {
        if (mService != null) {
            mService.playCurrentFile();
        }
    }

    /**
     * process after click stop button
     */
    void onClickStopButton() {
        if (mService == null) {
            long mEndStopTime = System.currentTimeMillis();
            Log.i(TAG, "[Performance test][SoundRecorder] recording stop end [" + mEndStopTime
                    + "]");
            return;
        }
        int state = mService.getCurrentState();
        if (state == SoundRecorderService.STATE_PAUSE_PLAYING
                || state == SoundRecorderService.STATE_PLAYING) {
            LogUtils.i(TAG, "<onClickStopButton> mService.stopPlay()");
            mService.stopPlay();
        } else if (state == SoundRecorderService.STATE_RECORDING
                || state == SoundRecorderService.STATE_PAUSE_RECORDING) {
            LogUtils.i(TAG, "<onClickStopButton> mService.stopRecord()");
            mService.stopRecord();
        }
        long mEndStopTime = System.currentTimeMillis();
        Log.i(TAG, "[Performance test][SoundRecorder] recording stop end [" + mEndStopTime + "]");
    }

    /**
     * process after click accept button
     */
    void onClickAcceptButton() {
        if (mService == null) {
            return;
        }
        mAcceptButton.setEnabled(false);
        mDiscardButton.setEnabled(false);
        mRecordButton.setEnabled(false);
        int state = mService.getCurrentState();
        if (state == SoundRecorderService.STATE_PAUSE_PLAYING
                || state == SoundRecorderService.STATE_PLAYING) {
            LogUtils.i(TAG, "<onClickAcceptButton> mService.stopPlay() first");
            mService.stopPlay();
        }
        mService.saveRecord();
        mFrameLayout.setBackgroundColor(getResources().getColor(R.color.frameLayoutGrayColor));
    }

    /**
     * process after click discard button
     */
    void onClickDiscardButton() {
        int state = mService.getCurrentState();
        if (state == SoundRecorderService.STATE_PAUSE_PLAYING
                || state == SoundRecorderService.STATE_PLAYING) {
            LogUtils.i(TAG, "<onClickDiscardButton> mService.stopPlay() first");
            mService.stopPlay();
        }
        mService.discardRecord();
        mFrameLayout.setBackgroundColor(getResources().getColor(R.color.frameLayoutGrayColor));
        mVUMeter.mCurrentAngle = 0;
        mVUMeter.invalidate();
    }

    /**
     * process after click file list button
     */
    void onClickFileListButton() {
        LogUtils.i(TAG, "<onClickFileListButton> mService.reset()");
        if (mService != null) {
            mService.reset();
        }
        // disable the file list button to guarantee it won't start multi
        // RecordingFileList
        mFileListButton.setEnabled(false);
        Intent mIntent = new Intent();
        mIntent.setClass(this, RecordingFileList.class);
        startActivityForResult(mIntent, REQURST_FILE_LIST);
    }

    /**
     * process after click pause recording button
     */
    void onClickPauseRecordingButton() {
        if (mService != null) {
            mService.pauseRecord();
        }
    }

    /**
     * Update the big MM:SS timer. If we are in play back, also update the
     * progress bar.
     */
    private void updateTimerView() {
        int time = mService.getCurrentProgressInSecond();
        int state = mService.getCurrentState();
        // update progress bar
        if (state == SoundRecorderService.STATE_PLAYING) {
            mStateProgressBar.setProgress((int) (100 
                    * mService.getCurrentProgressInMillSecond() 
        //GPBLJ-41 modify by cuinana 20130502 (start)   /mService.getCurrentFileDurationInMillSecond()));
                    / mService.getCurrentFileDurationInMillSecond()+1));
	//GPBLJ-41 modify by cuinana 20130502 (end)
        }
        // update timer
        String timerString = String.format(mTimerFormat, time / TIME_BASE, time % TIME_BASE);
        setTimerViewTextSize(time);
        mTimerTextView.setText(timerString);
        mHandler.postDelayed(mUpdateTimer, UPDATE_TIMER_VIRE_POST_DELAYED);
        // update remaining time, if remaining time == 0, stop recording
        if (mService.getCurrentState() == SoundRecorderService.STATE_RECORDING) {
            int remainingTime = (int) mService.getRemainingTime();
            if (mService.isStorageLower()) {
                showStorageHint(getString(R.string.storage_low));
            } else {
                hideStorageHint();
            }
            if (mHasFileSizeLimitation || remainingTime < TIME_NINE_MIN) {
                updateRemainingTimerView(remainingTime);
            }
        }
    }

    /**
     * set text size bigger if time >= 100
     * 
     * @param time
     *            time in minutes should be shown
     */
    private void setTimerViewTextSize(int time) {
        /** M: set text size bigger if >= 100 @{ */
        final int textSizeChangeBoundary = 100;
        if ((time / TIME_BASE) >= textSizeChangeBoundary) {
            mTimerTextView.setTextSize(TIMER_VIEW_TEXT_SIZE_SMALL);
        } else {
            mTimerTextView.setTextSize(TIMER_VIEW_TEXT_SIZE_BIG);
        }
        /** @} */
    }

    private void restoreDialogFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(DIALOG_TAG_SELECT_FORMAT);
        if (null != fragment) {
            ((SelectDialogFragment) fragment).setOnClickListener(mSelectFormatListener);
        }

        fragment = fragmentManager.findFragmentByTag(DIALOG_TAG_SELECT_MODE);
        if (null != fragment) {
            ((SelectDialogFragment) fragment).setOnClickListener(mSelectModeListener);
        }

        fragment = fragmentManager.findFragmentByTag(DIALOG_TAG_SELECT_EFFECT);
        if (null != fragment) {
            ((SelectDialogFragment) fragment).setOnMultiChoiceListener(mSelectEffectMultiChoiceClickListener);
            ((SelectDialogFragment) fragment).setOnClickListener(mSelectEffectOkListener);
        }
    }

    private void storeRecordParamsSettings() {
        if (null == mPrefs) {
            mPrefs = getSharedPreferences(SOUND_RECORDER_DATA, 0);
        }
        SharedPreferences.Editor ed = mPrefs.edit();
        ed.clear();
        ed.putInt(SELECTED_RECORDING_FORMAT, mSelectedFormat);
        ed.putInt(SELECTED_RECORDING_MODE, mSelectedMode);
        ed.putBoolean(SELECTED_RECORDING_EFFECT_AEC,
                mSelectEffectArray[RecordParamsSetting.EFFECT_AEC]);
        ed.putBoolean(SELECTED_RECORDING_EFFECT_AGC,
                mSelectEffectArray[RecordParamsSetting.EFFECT_AGC]);
        ed.putBoolean(SELECTED_RECORDING_EFFECT_NS,
                mSelectEffectArray[RecordParamsSetting.EFFECT_NS]);
        ed.putBoolean(SELECTED_RECORDING_EFFECT_AEC_TMP,
                mSelectEffectArrayTemp[RecordParamsSetting.EFFECT_AEC]);
        ed.putBoolean(SELECTED_RECORDING_EFFECT_AGC_TMP,
                mSelectEffectArrayTemp[RecordParamsSetting.EFFECT_AGC]);
        ed.putBoolean(SELECTED_RECORDING_EFFECT_NS_TMP,
                mSelectEffectArrayTemp[RecordParamsSetting.EFFECT_NS]);
        ed.commit();
    }

    private void restoreRecordParamsSettings() {
        if (null == mPrefs) {
            mPrefs = getSharedPreferences(SOUND_RECORDER_DATA, 0);
        }
        mSelectedFormat = mPrefs.getInt(SELECTED_RECORDING_FORMAT, RecordParamsSetting.FORMAT_LOW);
        mSelectedMode = mPrefs.getInt(SELECTED_RECORDING_MODE, RecordParamsSetting.MODE_NORMAL);
        mSelectEffectArray[RecordParamsSetting.EFFECT_AEC] = mPrefs.getBoolean(
                SELECTED_RECORDING_EFFECT_AEC, false);
        mSelectEffectArray[RecordParamsSetting.EFFECT_AGC] = mPrefs.getBoolean(
                SELECTED_RECORDING_EFFECT_AGC, false);
        mSelectEffectArray[RecordParamsSetting.EFFECT_NS] = mPrefs.getBoolean(
                SELECTED_RECORDING_EFFECT_NS, false);
        mSelectEffectArrayTemp[RecordParamsSetting.EFFECT_AEC] = mPrefs.getBoolean(
                SELECTED_RECORDING_EFFECT_AEC_TMP, false);
        mSelectEffectArrayTemp[RecordParamsSetting.EFFECT_AGC] = mPrefs.getBoolean(
                SELECTED_RECORDING_EFFECT_AGC_TMP, false);
        mSelectEffectArrayTemp[RecordParamsSetting.EFFECT_NS] = mPrefs.getBoolean(
                SELECTED_RECORDING_EFFECT_NS_TMP, false);
    }

    private void updateRemainingTimerView(int second) {
        String timeString = "";
        if (second < 0) {
            mRemainingTimeTextView.setText(NULL_STRING);
        } else if (second < TIME_BASE) {
            timeString = String.format(getString(R.string.sec_available), second);
        } else {
            if (second % TIME_BASE == 0) {
                timeString = String.format(getString(R.string.min_available), second / TIME_BASE);
            } else {
                timeString = String.format(getString(R.string.time_available), second / TIME_BASE,
                        second % TIME_BASE);
            }
        }
        LogUtils.i(TAG, "<updateRemainingTimerView> mRemainingTimeTextView.setText: "
                + timeString);
        mRemainingTimeTextView.setText(timeString);
        mRemainingTimeTextView.setVisibility(View.VISIBLE);
    }

    /**
     * M: release wake lock
     */
    private void releaseWakeLock() {
        // if mWakeLock is not release, release it
        if ((mWakeLock != null) && mWakeLock.isHeld()) {
            mWakeLock.release();
            LogUtils.i(TAG, "<releaseWakeLock>");
        }
    }

    /**
     * M: acquire wake lock
     */
    private void acquireWakeLock() {
        if ((mWakeLock != null) && !mWakeLock.isHeld()) {
            mWakeLock.acquire();
            LogUtils.i(TAG, "<acquireWakeLock>");
        }
    }

    /**
     * M: add for long string in option menu
     */
    protected void addOptionsMenuInflaterFactory() {
        final LayoutInflater infl = getLayoutInflater();
        infl.setFactory(new Factory() {
            public View onCreateView(final String name, final Context context,
                    final AttributeSet attrs) {
                // not create list menu item view
                if (!name.equalsIgnoreCase(LIST_MENUITEM_VIEW_NAME)) {
                    return null;
                }

                // get class and constructor
                if (sListMenuItemViewClass == null) {
                    try {
                        sListMenuItemViewClass = getClassLoader().loadClass(name);
                    } catch (ClassNotFoundException e) {
                        return null;
                    }
                }
                if (sListMenuItemViewClass == null) {
                    return null;
                }
                if (sListMenuItemViewConstructor == null) {
                    try {
                        sListMenuItemViewConstructor = sListMenuItemViewClass
                                .getConstructor(INFLATER_CONSTRUCTOR_SIGNATURE);
                    } catch (SecurityException e) {
                        return null;
                    } catch (NoSuchMethodException e) {
                        return null;
                    }
                }
                if (sListMenuItemViewConstructor == null) {
                    return null;
                }

                // create list menu item view
                View view = null;
                try {
                    Object[] args = new Object[] { context, attrs };
                    view = (View) (sListMenuItemViewConstructor.newInstance(args));
                } catch (IllegalArgumentException e) {
                    return null;
                } catch (InstantiationException e) {
                    return null;
                } catch (IllegalAccessException e) {
                    return null;
                } catch (InvocationTargetException e) {
                    return null;
                }
                if (null == view) {
                    return null;
                }

                final View viewTemp = view;
                new Handler().post(new Runnable() {
                    public void run() {
                        TextView textView = (TextView) viewTemp.findViewById(android.R.id.title);
                        LogUtils.e(TAG, "<create ListMenuItemView> setSingleLine");
                        // multi line if item string too long
                        textView.setSingleLine(false);
                    }
                });
                LogUtils.e(TAG, "<create ListMenuItemView> return view = " + view.toString());
                return view;
            }
        });
    }
    
    private void updateOptionsMenu() {
        LogUtils.i(TAG, "<updateOptionsMenu>");
        if (mMenu == null) {
            LogUtils.i(TAG, "<updateOptionsMenu> mMenu == null, return");
            return;
        }

        boolean allowSelectFormatAndMode = mRunFromLauncher;
        if (mService != null) {
            allowSelectFormatAndMode = mRunFromLauncher
                    && (mService.getCurrentState() == SoundRecorderService.STATE_IDLE);
        }

        if (RecordParamsSetting.canSelectFormat()) {
            MenuItem item1 = mMenu.getItem(OPTIONMENU_SELECT_FORMAT);
            if (null != item1) {
                item1.setVisible(allowSelectFormatAndMode);
            }
        }
        if (RecordParamsSetting.canSelectMode()) {
            MenuItem item2 = mMenu.getItem(OPTIONMENU_SELECT_MODE);
            if (null != item2) {
                item2.setVisible(allowSelectFormatAndMode);
            }
        }
        if (RecordParamsSetting.canSelectEffect()) {
            MenuItem item3 = mMenu.getItem(OPTIONMENU_SELECT_EFFECT);
            if (null != item3) {
                item3.setVisible(allowSelectFormatAndMode);
            }
        }
    }

    private void showStorageHint(String message) {
        if (null == mStorageHint) {
            mStorageHint = OnScreenHint.makeText(this, message);
        } else {
            mStorageHint.setText(message);
        }
        mStorageHint.show();
    }

    private void hideStorageHint() {
        if (null != mStorageHint) {
            mStorageHint.cancel();
            mStorageHint = null;
        }
    }
}
