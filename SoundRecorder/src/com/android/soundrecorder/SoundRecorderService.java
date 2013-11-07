package com.android.soundrecorder;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.android.soundrecorder.RecordParamsSetting.RecordParams;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SoundRecorderService extends Service implements Player.PlayerListener,
        Recorder.RecorderListener, MediaScannerConnectionClient {

    private static final String TAG = "SR/SoundRecorderService";
    public static final int STATE_IDLE = 1;
    public static final int STATE_RECORDING = 2;
    public static final int STATE_PAUSE_RECORDING = 3;
    public static final int STATE_PLAYING = 4;
    public static final int STATE_PAUSE_PLAYING = 5;
    public static final int EVENT_SAVE_SUCCESS = 1;
    public static final int EVENT_DISCARD_SUCCESS = 2;
    public static final int EVENT_STORAGE_MOUNTED = 3;
    public static final long LOW_STORAGE_THRESHOLD = 2048 * 1024L;
    private static final long WAIT_TIME = 45;

    // Broadcast messages from other sounder APP or Camera
    public static final String SOUND_POWER_DOWN_MSG = "com.android.music.musicservicecommand";
    public static final String CMDPAUSE = "pause";
    
    // the intent action from notification
    private static final String ACTION_GOON_RECORD = "goon record";
    private static final String ACTION_GOON_PLAY = "goon play";
    private static final String ACTION_STOP = "stop";
    private static final String ACTION_PAUSE = "pause";
    private static final String RECORDING = "Recording";

    private static final int START_NOTIFICATION_ID = 1;
    private static final long FACTOR_FOR_SECOND_AND_MINUTE = 1000;
    private static final int PLAYLIST_ID_NULL = -1;

    private SoundRecorderBinder mBinder = new SoundRecorderBinder();
    private OnErrorListener mOnErrorListener = null;
    private OnStateChangedListener mOnStateChangedListener = null;
    private OnEventListener mOnEventListener = null;

    private AudioManager mAudioManager = null;
    private StorageManager mStorageManager = null;
    private Recorder mRecorder = null;
    private Player mPlayer = null;
    private RemainingTimeCalculator mRemainingTimeCalculator = null;
    private RecordParams mCurrentRecordParams = null;
    private MediaScannerConnection mConnection = null;
    private Uri mUri = null;
    private String mCurrentFilePath = null;
    private String mFilePathToScan = null;
    private long mCurrentFileDuration = -1;
    private long mTotalRecordingDuration = -1;
    private int mCurrentState = STATE_IDLE;
    private BroadcastReceiver mStorageBroastReceiver = null;
    private BroadcastReceiver mOtherBroastReceiver = null;
    private boolean mRunForeground = false;
    private boolean mShowNotifiaction = true;
    private RecordingFileObserver mFileObserver = null;
    private Handler mFileObserverHandler = null;
    private OnAudioFocusChangeListener mFocusChangeListener = null;
    private boolean mGetFocus = false;
    private long mRemainingTime;
    private final Handler mHandler  = new Handler();

    // notificaton related
    private RemoteViews mNotificationView = null;

    public interface OnErrorListener {
        public void onError(int errorCode);
    }

    public interface OnStateChangedListener {
        public void onStateChanged(int stateCode);
    }

    public interface OnEventListener {
        public void onEvent(int eventCode);
    }

    @Override
    public void onError(Player player, int errorCode) {
        LogUtils.i(TAG, "<Player onError> errorCode = " + errorCode);
        reset();
        mOnErrorListener.onError(errorCode);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogUtils.i(TAG, "<onStartCommand> start");
        if (intent == null) {
            return super.onStartCommand(intent, flags, startId);
        }

        String action = intent.getAction();
        LogUtils.i(TAG, "<onStartCommand> action = " + action);

        if (action == null) {
            return super.onStartCommand(intent, flags, startId);
        }

        if (action.equals(ACTION_GOON_RECORD) && mCurrentState == STATE_PAUSE_RECORDING) {
            LogUtils.i(TAG, "<onStartCommand> ACTION_GOON_RECORD");
            record(mCurrentRecordParams, 0);
        } else if (action.equals(ACTION_GOON_PLAY) && mCurrentState == STATE_PAUSE_PLAYING) {
            LogUtils.i(TAG, "<onStartCommand> ACTION_GOON_PLAY");
            playCurrentFile();
        } else if (action.equals(ACTION_PAUSE)) {
            LogUtils.i(TAG, "<onStartCommand> ACTION_PAUSE");
            if (mCurrentState == STATE_RECORDING) {
                pauseRecord();
            } else if (mCurrentState == STATE_PLAYING) {
                pausePlay();
            }
        } else if (action.equals(ACTION_STOP)) {
            LogUtils.i(TAG, "<onStartCommand> ACTION_STOP");
            if (mCurrentState == STATE_RECORDING || mCurrentState == STATE_PAUSE_RECORDING) {
                stopRecord();
            } else if (mCurrentState == STATE_PLAYING || mCurrentState == STATE_PAUSE_PLAYING) {
                stopPlay();
            }
        }
        LogUtils.i(TAG, "<onStartCommand> end");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onStateChanged(Player player, int stateCode) {
        LogUtils.i(TAG, "<Player onStateChanged> stateCode = " + stateCode);
        if (stateCode == STATE_PLAYING) {
            mCurrentFileDuration = mPlayer.getFileDuration();
        } else if ((stateCode == STATE_IDLE || stateCode == STATE_PAUSE_PLAYING)
                && mCurrentState == STATE_PLAYING) {
            abandonAudioFocus();
        }
        
        setState(stateCode);

        if (stateCode != STATE_IDLE) {
            if (mRunForeground) {
                LogUtils.i(TAG, "<Player onStateChanged> update notificaton");
                showNotification(SoundRecorderService.this);
            } else {
                LogUtils.i(TAG, "<Player onStateChanged> show notificaton");
                showNotification(SoundRecorderService.this);
            }
        } else {
            if (mRunForeground) {
                hideNotifiaction();
            }
        }
    }

    @Override
    public void onError(Recorder recorder, int errorCode) {
        LogUtils.i(TAG, "<Recorder onError> errorCode = " + errorCode);
        reset();
        mHandler.removeCallbacks(mRemainingTimeCalculateRunnable);
        mOnErrorListener.onError(errorCode);
    }

    @Override
    public void onStateChanged(Recorder recorder, int stateCode) {
        LogUtils.i(TAG, "<Recorder onStateChanged> stateCode = " + stateCode);
        if ((mCurrentState == STATE_PAUSE_RECORDING || mCurrentState == STATE_RECORDING)
                && stateCode == STATE_IDLE) {
            abandonAudioFocus();
            getRecordInfoAfterStopRecord();
            if (mCurrentFileDuration < 1000 && mCurrentFilePath != null
                    && mCurrentFilePath.endsWith(Recorder.SAMPLE_SUFFIX)) {
                String deleteFilePath = mCurrentFilePath;
                // set current file path first, stop watching
                setCurrentFilePath(null);
                File file = new File(deleteFilePath);
                file.delete();
                LogUtils.i(TAG,
                        "<Recorder onStateChanged> delete recording file for the duration < 1s");
                mCurrentFileDuration = 0;
            }
        } else if (stateCode == STATE_RECORDING) {
            setCurrentFilePath(mRecorder.getSampleFilePath());
            mHandler.post(mRemainingTimeCalculateRunnable);
            LogUtils.i(TAG, "post: mRemainingTimeCalculateRunnable");
        }
        setState(stateCode);

        if (stateCode != STATE_IDLE) {
            if (mRunForeground) {
                LogUtils.i(TAG, "<Recorder onStateChanged> update notificaton");
                showNotification(SoundRecorderService.this);
            } else {
                LogUtils.i(TAG, "<Recorder onStateChanged> show notificaton");
                showNotification(SoundRecorderService.this);
            }
        } else {
            if (mRunForeground) {
                hideNotifiaction();
            }
        }
    }

    @Override
    /**
     * M: after add record to Media database, 
     * use MediaScanner to scan file which has been added
     */
    public void onMediaScannerConnected() {
        LogUtils.i(TAG, "<onMediaScannerConnected> scan file: " + mFilePathToScan);
        mConnection.scanFile(mFilePathToScan, null);
    }

    @Override
    /**
     * M: after scan completed, update record item
     */
    public void onScanCompleted(String path, Uri uri) {
        LogUtils.i(TAG, "<onScanCompleted> start, path = " + path);
        Resources res = getResources();
        long current = System.currentTimeMillis();
//GPBYB-85 20130812 modify start
        /*Date date = new Date(current);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getResources().getString(
                R.string.audio_db_title_format));
        String title = simpleDateFormat.format(date);*/
	String title = path.substring(path.lastIndexOf("/") + 1, path.length()); 
//GPBYB-85 20130812 modify end
        final String where = MediaStore.Audio.Media.DATA + " LIKE '%"
                + path.replaceFirst("file:///", "") + "'";
        final int size = 6;
        ContentValues cv = new ContentValues(size);
        cv.put(MediaStore.Audio.Media.IS_MUSIC, "0");
        cv.put(MediaStore.Audio.Media.TITLE, title);
        cv.put(MediaStore.Audio.Media.DATE_ADDED, (int) (current / FACTOR_FOR_SECOND_AND_MINUTE));
        cv.put(MediaStore.Audio.Media.DATA, path);
        cv.put(MediaStore.Audio.Media.ARTIST, res.getString(R.string.unknown_artist_name));
        cv.put(MediaStore.Audio.Media.ALBUM, res.getString(R.string.audio_db_album_name));
        ContentResolver resolver = getContentResolver();
        Uri base = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        LogUtils.i(TAG, "<onScanCompleted> resolver.update");
        int result = resolver.update(base, cv, where, null);
        if (result > 0) {
            LogUtils.i(TAG, "<onScanCompleted> update success");
        } else {
            LogUtils.i(TAG, "<onScanCompleted> update fail");
        }
        mConnection.disconnect();
        LogUtils.i(TAG, "<onScanCompleted> end");
    }

    public void setErrorListener(OnErrorListener listener) {
        mOnErrorListener = listener;
    }

    public void setEventListener(OnEventListener listener) {
        mOnEventListener = listener;
    }

    public void setStateChangedListener(OnStateChangedListener listener) {
        mOnStateChangedListener = listener;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        LogUtils.i(TAG, "<onBind>");
        return mBinder;
    }

    public void setAllListenerSelf() {
        // set mOnErrorListener as a new listener when activity stopped/killed,
        // when error occours, show error info in toast
        LogUtils.i(TAG, "<setAllListenerSelf> set new mOnErrorListener");
        mOnErrorListener = new OnErrorListener() {
            public void onError(int errorCode) {
                ErrorHandle.showErrorInfoInToast(getApplicationContext(), errorCode);
            };
        };
        mOnEventListener = null;
        mOnStateChangedListener = null;
    }

    public void setShowNotification(boolean show) {
        mShowNotifiaction = show;
    }

    public class SoundRecorderBinder extends Binder {
        SoundRecorderService getService() {
            return SoundRecorderService.this;
        }
    }

    @Override
    public void onCreate() {
        LogUtils.i(TAG, "<onCreate> start");
        super.onCreate();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
        mRecorder = new Recorder(mStorageManager, this);
        mPlayer = new Player(this);
        mConnection = new MediaScannerConnection(getApplicationContext(), this);
        mFocusChangeListener = new OnAudioFocusChangeListener() {
            @Override
            public void onAudioFocusChange(int focusChange) {
                LogUtils.i(TAG, "<onAudioFocusChange> audio focus changed to " + focusChange);
                if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                    LogUtils.i(TAG, "<onAudioFocusChange> audio focus changed to AUDIOFOCUS_GAIN");
                    mGetFocus = true;
                } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS
                        || focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                    LogUtils.i(TAG, "<onAudioFocusChange> audio focus loss, stop recording");
                    mGetFocus = false;
                    if (mCurrentState == STATE_RECORDING || mCurrentState == STATE_PAUSE_RECORDING) {
                        stopRecord();
                    } else if (mCurrentState == STATE_PLAYING) {
                        stopPlay();
                    }
                    
                    if(isCurrentFileWaitToSave()) {
                        saveRecord();
                    }
                }
            }
        };

        registerBroadcastReceivcer();

        IntentFilter commandFilter = new IntentFilter();
        commandFilter.addAction(ACTION_GOON_PLAY);
        commandFilter.addAction(ACTION_GOON_RECORD);
        commandFilter.addAction(ACTION_PAUSE);
        commandFilter.addAction(ACTION_STOP);
        LogUtils.i(TAG, "<onCreate> end");
    }

    @Override
    public void onDestroy() {
        LogUtils.i(TAG, "<onDestroy>");
        unregisterBroadcastReceivcer();
        super.onDestroy();
    }

    public boolean record(RecordParams params, int fileSizeLimit) {
        if (mCurrentState == STATE_RECORDING) {
            LogUtils.i(TAG, "<record> still in STATE_RECORDING, do nothing");
            return true;
        } else if (mCurrentState == STATE_PAUSE_RECORDING) {
            LogUtils.i(TAG, "<record> in STATE_PAUSE_RECORDING, mRecorder.goonRecording()");
            if (requestAudioFocus() && mRecorder.goonRecording()) {
                return true;
            }
            abandonAudioFocus();
            return false;
        } else {
            if (mCurrentState == STATE_PAUSE_PLAYING || mCurrentState == STATE_PLAYING) {
                LogUtils.i(TAG, "<record> in pause playing or playing, stopPlay first");
                stopPlay();
            }
            if (isCurrentFileEndWithTmp()) {
                LogUtils.i(TAG, "<record> delete not saved file: " + mCurrentFilePath);
                stopWatching();
                File file = new File(mCurrentFilePath);
                file.delete();
            }
            if (!isStorageMounted()) {
                LogUtils.i(TAG, "<record> no storage mounted");
                mOnErrorListener.onError(ErrorHandle.ERROR_NO_SD);
                reset();
                setState(STATE_IDLE);
                return false;
            } else if (isStorageFull(params)) {
                LogUtils.i(TAG, "<record> storage is full");
                mOnErrorListener.onError(ErrorHandle.ERROR_STORAGE_FULL_WHEN_LAUNCH);
                reset();
                setState(STATE_IDLE);
                return false;
            } else {
                mRemainingTimeCalculator = new RemainingTimeCalculator(mStorageManager, this);
                mRemainingTimeCalculator.setBitRate(params.mRemainingTimeCalculatorBitRate);
                LogUtils.i(TAG, "<record> start record");
                boolean res = requestAudioFocus()
                        && mRecorder.startRecording(params, fileSizeLimit);
                LogUtils.i(TAG, "<record> mRecorder.startRecording res = " + res);
                mCurrentRecordParams = params;
                if (res && fileSizeLimit > 0) {
                    mRemainingTimeCalculator.setFileSizeLimit(mRecorder.getSampFile(),
                            fileSizeLimit);
                }
                if (!res) {
                    abandonAudioFocus();
                }
                return res;
            }
        }
    }

    public boolean pauseRecord() {
        mRemainingTimeCalculator.setPauseTimeRemaining(true);
        if (mCurrentState == STATE_PAUSE_RECORDING) {
            LogUtils.i(TAG, "<pauseRecord> still in STATE_PAUSE_RECORDING, do nothing, return");
            return true;
        } else if (mCurrentState == STATE_RECORDING) {
            // M: we do not abandon audio focus when pause recording
            boolean result = mRecorder.pauseRecording();
            mHandler.removeCallbacks(mRemainingTimeCalculateRunnable);
            return result;
        } else {
            return false;
        }
    }

    public boolean stopRecord() {
        if (mCurrentState != STATE_PAUSE_RECORDING && mCurrentState != STATE_RECORDING) {
            LogUtils.i(TAG, "<stopRecord> not in pause record or record state, return");
            return false;
        }
        abandonAudioFocus();
        boolean result = mRecorder.stopRecording();
        mHandler.removeCallbacks(mRemainingTimeCalculateRunnable);
        return result;
    }

    public boolean saveRecord() {
        if (mCurrentFilePath == null || !mCurrentFilePath.endsWith(Recorder.SAMPLE_SUFFIX)) {
            LogUtils.i(TAG, "<saveRecord> no file need to be saved");
            return false;
        }
        if (!isStorageMounted()) {
            LogUtils.i(TAG, "<saveRecord> no storage mounted");
            mOnErrorListener.onError(ErrorHandle.ERROR_NO_SD);
            return false;
        }
        String currentFilePath = deleteRecordingFileTmpSuffix();
        if (currentFilePath != null) {
            mUri = addToMediaDB(new File(currentFilePath));
            mCurrentFileDuration = 0;
            setCurrentFilePath(null);
            mTotalRecordingDuration = 0;
            if (mOnEventListener != null) {
                mOnEventListener.onEvent(EVENT_SAVE_SUCCESS);
            } else {
                //M: when mOnEventListener == null, we use service to show toast 
                Toast.makeText(getApplicationContext(), R.string.tell_save_record_success,
                        Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return false;
        // SaveDataTask saveTask = new SaveDataTask();
        // saveTask.execute();
    }

    public boolean discardRecord() {
        if (!isStorageMounted()) {
            LogUtils.i(TAG, "<discardRecord> no storage mounted");
            mOnErrorListener.onError(ErrorHandle.ERROR_NO_SD);
            return false;
        }
        if (mCurrentFilePath == null || mCurrentFilePath.equals("")) {
            return true;
        }

        LogUtils.i(TAG, "<discardRecord> mCurrentFilePath = " + mCurrentFilePath);
        String deleteFilePath = mCurrentFilePath;
        // set current file path first, stop watching
        setCurrentFilePath(null);
        File file = new File(deleteFilePath);
        file.delete();
        mCurrentFileDuration = 0;

        if (mOnEventListener != null) {
            mOnEventListener.onEvent(EVENT_DISCARD_SUCCESS);
        }
        return true;
    }

    public boolean playCurrentFile() {
        if (mCurrentState == STATE_RECORDING || mCurrentState == STATE_PAUSE_RECORDING) {
            LogUtils.i(TAG, "<playCurrentFile> in record or pause record state, can't play");
            return false;
        } else if (mCurrentState == STATE_PAUSE_PLAYING) {
            LogUtils.i(TAG, "<playCurrentFile> in pause play state, goon play");
            return requestAudioFocus() && mPlayer.goonPlayback();
        } else if (mCurrentState == STATE_PLAYING) {
            LogUtils.i(TAG, "<playCurrentFile> in play state, pause play");
            abandonAudioFocus();
            return mPlayer.pausePlayback();
        } else {
            LogUtils.i(TAG, "<playCurrentFile> in idle state, start play");
            mPlayer.setCurrentFilePath(mCurrentFilePath);
            return requestAudioFocus() && mPlayer.startPlayback();
        }
    }

    public boolean playFile(String path) {
        LogUtils.i(TAG, "<playFile> path = " + path);
        if (path == null) {
            return false;
        }
        setCurrentFilePath(path);
        mCurrentFileDuration = 0;
        playCurrentFile();
        return true;
    }

    public boolean pausePlay() {
        if (mCurrentState != STATE_PLAYING) {
            LogUtils.i(TAG, "<pausePlay> not in play state, can't pause play");
            return false;
        }
        abandonAudioFocus();
        mPlayer.pausePlayback();
        return true;
    }

    public boolean stopPlay() {
        if (mCurrentState != STATE_PAUSE_PLAYING && mCurrentState != STATE_PLAYING) {
            LogUtils.i(TAG, "<stopPlay> not in play or pause play state, can't stop play");
            return false;
        }
        abandonAudioFocus();
        mPlayer.stopPlayback();
        return true;
    }

    public Recorder getRecorder() {
        return mRecorder;
    }

    public long getCurrentFileDurationInSecond() {
        if (isCurrentFileEndWithTmp()) {
            return mTotalRecordingDuration / 1000;
        } else {
            long res = mCurrentFileDuration / 1000;
            res = res > 0 ? res : 0;
            return res;
        }
    }

    public long getCurrentFileDurationInMillSecond() {
    // GPBLJ-41 modify by cuinana 20130502 (start) 
        //if (isCurrentFileEndWithTmp()) {
        if (isCurrentFileEndWithTmp() && mCurrentState != STATE_PLAYING) {
    // GPBLJ-41 modify by cuinana 20130502 (end){
            return mTotalRecordingDuration;
        } else {
            return mCurrentFileDuration;
        }
    }

    public String getCurrentFilePath() {
        return mCurrentFilePath;
    }

    public Uri getSaveFileUri() {
        return mUri;
    }

    public long getRemainingTime() {
        return mRemainingTime;
    }

    public int getCurrentState() {
        return mCurrentState;
    }

    public int getCurrentProgressInSecond() {
        return (int) (getCurrentProgressInMillSecond() / 1000L);
    }

    public long getCurrentProgressInMillSecond() {
        if (mCurrentState == STATE_PAUSE_PLAYING || mCurrentState == STATE_PLAYING) {
            return mPlayer.getCurrentProgress();
        } else if (mCurrentState == STATE_PAUSE_RECORDING || mCurrentState == STATE_RECORDING) {
            return mRecorder.getCurrentProgress();
        }
        return 0;
    }

    public boolean isCurrentFileWaitToSave() {
        if (mCurrentFilePath == null) {
            return false;
        } else {
            return mCurrentFilePath.endsWith(Recorder.SAMPLE_SUFFIX);
        }
    }

    public boolean isStorageFull(RecordParams params) {
        RemainingTimeCalculator remainingTimeCalculator = new RemainingTimeCalculator(
                mStorageManager, this);
        remainingTimeCalculator.setBitRate(params.mAudioEncodingBitRate);
        return remainingTimeCalculator.timeRemaining(false) < 2;
    }

    public boolean isStorageMounted() {
        String storageState = mStorageManager.getVolumeState(mStorageManager.getDefaultPath());
        if ((null == storageState) || storageState.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        }
        return false;
    }

    public boolean reset() {
        if (!mRecorder.reset()) {
            mOnErrorListener.onError(ErrorHandle.ERROR_RECORDING_FAILED);
        }
        mPlayer.reset();
        if (mCurrentFilePath != null && mCurrentFilePath.endsWith(Recorder.SAMPLE_SUFFIX)) {
            File file = new File(mCurrentFilePath);
            file.delete();
        }
        setCurrentFilePath(null);
        mCurrentFileDuration = 0;
        hideNotifiaction();
        setState(STATE_IDLE);
        return true;
    }

    public class SaveDataTask extends AsyncTask<Void, Object, Uri> {
        /**
         * save recording file to database
         * 
         * @return the URI
         */
        protected Uri doInBackground(Void... params) {
            return addToMediaDB(new File(mCurrentFilePath));
        }

        protected void onPostExecute(Uri result) {
            mUri = result;
            mCurrentFileDuration = 0;
            setCurrentFilePath(null);
            mTotalRecordingDuration = 0;
            if (mOnEventListener != null) {
                mOnEventListener.onEvent(EVENT_SAVE_SUCCESS);
            } else {
                Toast.makeText(getApplicationContext(), R.string.tell_save_record_success,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private Uri addToMediaDB(File file) {
        LogUtils.i(TAG, "<addToMediaDB> begin");
        if (null == file) {
            LogUtils.i(TAG, "<addToMediaDB> file is null, return null");
            return null;
        }
        SoundRecorderUtils.deleteFileFromMediaDB(getApplicationContext(), file.getAbsolutePath());
        Resources res = getResources();
        long current = System.currentTimeMillis();
//GPBYB-85 20130812 modify start
        /*Date date = new Date(current);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getResources().getString(
                R.string.audio_db_title_format));
        String title = simpleDateFormat.format(date);*/
	String path = file.getAbsolutePath();
	String title = path.substring(path.lastIndexOf("/") + 1, path.length());
//GPBYB-85 20130812 modify end
        final int size = 6;
        ContentValues cv = new ContentValues(size);
        cv.put(MediaStore.Audio.Media.IS_MUSIC, "0");
        cv.put(MediaStore.Audio.Media.TITLE, title);
        cv.put(MediaStore.Audio.Media.DATE_ADDED, (int) (current / FACTOR_FOR_SECOND_AND_MINUTE));
        LogUtils.v(TAG, "<addToMediaDB> File type is " + mCurrentRecordParams.mMimeType);
        cv.put(MediaStore.Audio.Media.MIME_TYPE, mCurrentRecordParams.mMimeType);
        cv.put(MediaStore.Audio.Media.ARTIST, res.getString(R.string.unknown_artist_name));
        cv.put(MediaStore.Audio.Media.ALBUM, res.getString(R.string.audio_db_album_name));
        cv.put(MediaStore.Audio.Media.DATA, file.getAbsolutePath());
        cv.put(MediaStore.Audio.Media.DURATION, mTotalRecordingDuration);
        LogUtils.d(TAG, "<addToMediaDB> Reocrding time output to database is :DURATION= "
                + mCurrentFileDuration);
        ContentResolver resolver = getContentResolver();
        Uri base = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        LogUtils.d(TAG, "<addToMediaDB> ContentURI: " + base);
        Uri result = null;
        /** M: add exception process @{ */
        try {
            result = resolver.insert(base, cv);
        } catch (UnsupportedOperationException e) {
            LogUtils.e(TAG, "<addToMediaDB> Save in DB failed: " + e.getMessage());
        }
        /** @} */
        if (null == result) {
            mOnErrorListener.onError(ErrorHandle.ERROR_SAVE_FILE_FAILED);
        } else {
            LogUtils.i(TAG, "<addToMediaDB> Save susceeded in DB");
            if (getPlaylistId(res) == PLAYLIST_ID_NULL) {
                createPlaylist(res, resolver);
            }
            int audioId = Integer.valueOf(result.getLastPathSegment());
            if (getPlaylistId(res) != PLAYLIST_ID_NULL) {
                addToPlaylist(resolver, audioId, getPlaylistId(res));
            }
            // Notify those applications such as Music listening to the
            // scanner events that a recorded audio file just created.
            /**
             * M: use MediaScanner to scan record file just be added, replace
             * send broadcast to scan all
             */
            mFilePathToScan = file.getAbsolutePath();
            mConnection.connect();
        }
        return result;
    }

    /**
     * Create a play list with the given default play list name, if no such play
     * list exists.
     * 
     * @param res
     *            resource
     * @param resolver
     *            ContentResolver
     * @return the URI of play list that has just be created
     */
    private Uri createPlaylist(Resources res, ContentResolver resolver) {
        ContentValues cv = new ContentValues(1);
        cv.put(MediaStore.Audio.Playlists.NAME, res.getString(R.string.audio_db_playlist_name));
        Uri uri = null;
        /** M: add exception process @{ */
        try {
            uri = resolver.insert(MediaStore.Audio.Playlists.getContentUri("external"), cv);
        } catch (UnsupportedOperationException e) {
            LogUtils.e(TAG, "<createPlaylist> insert in DB failed: " + e.getMessage());
            uri = null;
        }
        /** @} */

        if (null == uri) {
            /** M: use Handler to show database error dialog */
            mOnErrorListener.onError(ErrorHandle.ERROR_SAVE_FILE_FAILED);
        }
        return uri;
    }

    /**
     * Obtain the id for the default play list from the audio_playlists table.
     * 
     * @param res
     *            resource
     * @return get play list id
     */
    private int getPlaylistId(Resources res) {
        Uri uri = MediaStore.Audio.Playlists.getContentUri("external");
        final String[] ids = new String[] { MediaStore.Audio.Playlists._ID };
        final String where = MediaStore.Audio.Playlists.NAME + "=?";
        final String[] args = new String[] { res.getString(R.string.audio_db_playlist_name) };
        Cursor cursor = SoundRecorderUtils.query(getApplicationContext(), uri, ids, where, args,
                null);
        int id = PLAYLIST_ID_NULL;
        /** M: add exception process @{ */
        try {
            if (null == cursor) {
                LogUtils.e(TAG, "<getPlaylistId> query returns null");
            } else {
                cursor.moveToFirst();
                if (!cursor.isAfterLast()) {
                    id = cursor.getInt(0);
                }
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        /** @} */
        return id;
    }

    /**
     * Add the given audioId to the play list with the given playlistId; and
     * maintain the play_order in the play list.
     * 
     * @param resolver
     *            ContentResolver
     * @param audioId
     *            the audio id that will be added to play list
     * @param playlistId
     *            the play list id which will add to
     */
    private void addToPlaylist(ContentResolver resolver, int audioId, long playlistId) {
        String[] cols = new String[] { "count(*)" };
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        Cursor cur = resolver.query(uri, cols, null, null, null);
        if (null == cur) {
            LogUtils.e(TAG, "<addToPlaylist> cursor is null");
            return;
        }
        cur.moveToFirst();
        final int base = cur.getInt(0);
        cur.close();
        ContentValues values = new ContentValues(2);
        values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, Integer.valueOf(base + audioId));
        values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, audioId);
        /** M: add exception process @{ */
        Uri result = null;
        try {
            result = resolver.insert(uri, values);
        } catch (UnsupportedOperationException e) {
            LogUtils.e(TAG, "<addToPlaylist> insert in DB failed: " + e.getMessage());
            result = null;
        }
        if (null == result) {
            mOnErrorListener.onError(ErrorHandle.ERROR_SAVE_FILE_FAILED);
        }
        /** @} */
    }

    private void getRecordInfoAfterStopRecord() {
        mTotalRecordingDuration = mRecorder.getSampleLength();
        mCurrentFileDuration = mRecorder.getSampleLength();
        setCurrentFilePath(mRecorder.getSampleFilePath());
        // M:Add for stop fail case.
        if (!mRecorder.reset()) {
            mOnErrorListener.onError(ErrorHandle.ERROR_RECORDING_FAILED);
        }
    }

    private boolean isCurrentFileEndWithTmp() {
        if (null == mCurrentFilePath) {
            return false;
        }
        return mCurrentFilePath.endsWith(Recorder.SAMPLE_SUFFIX);
    }

    private void receiveBroadcast(Context context, Intent intent) {
        String action = intent.getAction();
        String command = intent.getStringExtra("command");
        LogUtils.i(TAG, "<onReceive> action = " + action);
        if (Intent.ACTION_MEDIA_EJECT.equals(action)
                || Intent.ACTION_MEDIA_UNMOUNTED.equals(action)) {
            // M: when sd card ejected is the current operation path,
            // call onError
            if (isCurrentAccessStorage(intent.getData())) {
                if (mCurrentState == STATE_RECORDING || mCurrentState == STATE_PAUSE_RECORDING) {
                    mOnErrorListener.onError(ErrorHandle.ERROR_SD_UNMOUNTED_ON_RECORD);
                } else {
                    mOnErrorListener.onError(ErrorHandle.ERROR_SD_UNMOUNTED_WHEN_IDLE);
                }
                reset();
            }
        } else if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
            // tell activity to remove SD_UNMOUNTED dialog
            if (mOnEventListener != null) {
                mOnEventListener.onEvent(EVENT_STORAGE_MOUNTED);
            }
        } else if (Intent.ACTION_CONFIGURATION_CHANGED.equals(action)) {
            if (mCurrentState != STATE_IDLE) {
                // when change language, update the language of
                // notification
                showNotification(getApplicationContext());
                return;
            }
        } else if (SOUND_POWER_DOWN_MSG.equals(action) && CMDPAUSE.equals(command)) {
            // M: camera begin to record when soundRecorder is
            // background record,
            // it will send SOUND_POWER_DOWN_MSG, we receive and stop
            // record
            // The process is same as audio focus loss
            if (mCurrentState == STATE_RECORDING || mCurrentState == STATE_PAUSE_RECORDING) {
                stopRecord();
            } else if (mCurrentState == STATE_PLAYING) {
                stopPlay();
            }

            if (isCurrentFileWaitToSave()) {
                saveRecord();
            }
        }
    }

    private void registerBroadcastReceivcer() {
        if (null == mStorageBroastReceiver) {
            mStorageBroastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    receiveBroadcast(context, intent);
                }
            };
            IntentFilter iFilter = new IntentFilter();
            iFilter.addAction(Intent.ACTION_MEDIA_EJECT);
            iFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
            iFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
            iFilter.addDataScheme("file");
            registerReceiver(mStorageBroastReceiver, iFilter);
            LogUtils.i(TAG, "<registerExternalStorageListener> register mStorageBroastReceiver");
        }

        if (null == mOtherBroastReceiver) {
            mOtherBroastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    receiveBroadcast(context, intent);
                }
            };
            IntentFilter iFilter = new IntentFilter();
            iFilter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
            iFilter.addAction(SOUND_POWER_DOWN_MSG);
            registerReceiver(mOtherBroastReceiver, iFilter);
            LogUtils.i(TAG, "<registerExternalStorageListener> register mOtherBroastReceiver");
        }
    }

    private void unregisterBroadcastReceivcer() {
        if (mStorageBroastReceiver != null) {
            unregisterReceiver(mStorageBroastReceiver);
        }

        if (mOtherBroastReceiver != null) {
            unregisterReceiver(mOtherBroastReceiver);
        }
    }

    private String deleteRecordingFileTmpSuffix() {
        if (!mCurrentFilePath.endsWith(Recorder.SAMPLE_SUFFIX)) {
            return null;
        }
        File file = new File(mCurrentFilePath);
        if (!file.exists()) {
            return null;
        }
        String newPath = mCurrentFilePath.substring(0, mCurrentFilePath
                .lastIndexOf(Recorder.SAMPLE_SUFFIX));
        stopWatching();
        File newFile = new File(newPath);
        boolean result = file.renameTo(newFile);
        if (result) {
            return newFile.getAbsolutePath();
        } else {
            return null;
        }
    }

    private void setState(int stateCode) {
        mCurrentState = stateCode;
        if (mOnStateChangedListener != null) {
            mOnStateChangedListener.onStateChanged(stateCode);
        } else {
            LogUtils.i(TAG, "<setState> mOnStateChangedListener = null, mCurrentState = "
                    + mCurrentState);
        }
    }

    private void showNotification(Context context) {
        if (!mShowNotifiaction) {
            hideNotifiaction();
            LogUtils.i(TAG, "<showNotification> mShowNotifiaction == false, return");
            return;
        }
        if (mCurrentState == STATE_IDLE) {
            LogUtils.i(TAG, "<showNotification> not show in STATE_IDLE, return");
            return;
        }
        if (mNotificationView == null) {
            LogUtils.i(TAG, "<showNotificatoin> create mNotificationView");
            mNotificationView = new RemoteViews(getPackageName(), R.layout.notification);
        }
        mNotificationView.setTextViewText(R.id.app_name, getResources()
                .getString(R.string.app_name));

        Intent intent;
        PendingIntent pIntent;

        intent = new Intent(ACTION_GOON_RECORD);
        intent.setClass(context, SoundRecorderService.class);
        pIntent = PendingIntent.getService(context, 0, intent, 0);
        mNotificationView.setOnClickPendingIntent(R.id.btn_goon_record, pIntent);

        intent = new Intent(ACTION_GOON_PLAY);
        intent.setClass(context, SoundRecorderService.class);
        pIntent = PendingIntent.getService(context, 0, intent, 0);
        mNotificationView.setOnClickPendingIntent(R.id.btn_goon_play, pIntent);

        intent = new Intent(ACTION_STOP);
        intent.setClass(context, SoundRecorderService.class);
        pIntent = PendingIntent.getService(context, 0, intent, 0);
        mNotificationView.setOnClickPendingIntent(R.id.btn_stop, pIntent);

        intent = new Intent(ACTION_PAUSE);
        intent.setClass(context, SoundRecorderService.class);
        pIntent = PendingIntent.getService(context, 0, intent, 0);
        mNotificationView.setOnClickPendingIntent(R.id.btn_pause, pIntent);

        intent = new Intent("com.android.soundrecorder.SoundRecorder");
        pIntent = PendingIntent.getActivity(context, 0, intent, 0);
        mNotificationView.setOnClickPendingIntent(R.id.app_icon, pIntent);

        Notification notification = new Notification();
        notification.contentView = mNotificationView;
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        notification.icon = R.drawable.notification_ic_small;
        notification.contentIntent = pIntent;

        // before first show notification, setNotification according state
        setNotification();

        LogUtils.i(TAG, "<showNotificatoin> startForeground");
        startForeground(START_NOTIFICATION_ID, notification);
        mRunForeground = true;
    }

    private void hideNotifiaction() {
        LogUtils.i(TAG, "<hideNotifiaction>");
        stopForeground(true);
        mRunForeground = false;
    }

    private void setNotification() {
        LogUtils.i(TAG, "<setNotification>");

        String filePath = getCurrentFilePath();
        String fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1, filePath
                .length());
        fileName = (fileName.endsWith(Recorder.SAMPLE_SUFFIX)) ? fileName.substring(0, fileName
                .lastIndexOf(Recorder.SAMPLE_SUFFIX)) : fileName;
        mNotificationView.setTextViewText(R.id.file_name, fileName);

        switch (mCurrentState) {
        case STATE_PAUSE_PLAYING:
            mNotificationView.setViewVisibility(R.id.btn_goon_play, View.VISIBLE);
            mNotificationView.setViewVisibility(R.id.btn_goon_record, View.GONE);
            mNotificationView.setViewVisibility(R.id.btn_pause, View.GONE);
            break;
        case STATE_PAUSE_RECORDING:
            mNotificationView.setViewVisibility(R.id.btn_goon_play, View.GONE);
            mNotificationView.setViewVisibility(R.id.btn_goon_record, View.VISIBLE);
            mNotificationView.setViewVisibility(R.id.btn_pause, View.GONE);
            break;
        case STATE_RECORDING:
        case STATE_PLAYING:
            mNotificationView.setViewVisibility(R.id.btn_goon_play, View.GONE);
            mNotificationView.setViewVisibility(R.id.btn_goon_record, View.GONE);
            mNotificationView.setViewVisibility(R.id.btn_pause, View.VISIBLE);
            break;
        default:
            break;
        }
        mNotificationView.setViewVisibility(R.id.btn_stop, View.VISIBLE);
    }

    private class RecordingFileObserver extends FileObserver {
        private String mWatchingPath = null;
        // use this to be sure mFileObserverHandler.sendEmptyMessage(0) will be
        // run only once
        private boolean mHasSendMessage = false;

        public RecordingFileObserver(String path) {
            super(path);
            mWatchingPath = path;
        }

        public RecordingFileObserver(String path, int mask) {
            super(path, mask);
            mWatchingPath = path;
        }

        @Override
        public void onEvent(int event, String path) {
            LogUtils.i(TAG, "<RecordingFileObserver.onEvent> event = " + event);
            if (!mHasSendMessage) {
                if (event == FileObserver.DELETE_SELF || event == FileObserver.ATTRIB
                        || event == FileObserver.MOVE_SELF) {
                    LogUtils.i(TAG, "<RecordingFileObserver.onEvent> " + mWatchingPath
                            + " has been deleted/renamed/moved");
                    mFileObserverHandler.sendEmptyMessage(0);
                    mHasSendMessage = true;
                }
            }
        }
    }

    private void setCurrentFilePath(String path) {
        mCurrentFilePath = path;
        if (mFileObserver != null) {
            mFileObserver.stopWatching();
            mFileObserver = null;
            // M: remove message that has not been processed
            mFileObserverHandler.removeMessages(0);
        }
        if (mCurrentFilePath != null) {
            mFileObserver = new RecordingFileObserver(mCurrentFilePath, FileObserver.DELETE_SELF
                    | FileObserver.ATTRIB | FileObserver.MOVE_SELF);
            if (mFileObserverHandler == null) {
                mFileObserverHandler = new Handler(getMainLooper()) {
                    public void handleMessage(android.os.Message msg) {
                        LogUtils.i(TAG, "<mFileObserverHandler handleMessage> reset()");
                        reset();
                    };
                };
            }
            LogUtils.i(TAG, "<setCurrentFilePath> start watching file <" + mCurrentFilePath + ">");
            mFileObserver.startWatching();
        }
    }

    private void stopWatching() {
        if (mFileObserver != null) {
            mFileObserver.stopWatching();
            mFileObserver = null;
        }
    }

    private boolean requestAudioFocus() {
        if (!mGetFocus) {
            int result = mAudioManager.requestAudioFocus(mFocusChangeListener,
                    AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                LogUtils.i(TAG, "<requestAudioFocus> request audio focus fail");
                mGetFocus = false;
            } else {
                LogUtils.i(TAG, "<requestAudioFocus> request audio focus success");
                mGetFocus = true;
            }
        }
        return mGetFocus;
    }

    /**
     * M: new private function for abandon audio focus, it will be called when
     * stop or pause play back
     */
    private void abandonAudioFocus() {
        if (mGetFocus && mAudioManager != null && mFocusChangeListener != null) {
            if (mAudioManager.abandonAudioFocus(mFocusChangeListener) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                LogUtils.i(TAG, "<abandonAudioFocus()> abandon audio focus success");
                mGetFocus = false;
            } else {
                LogUtils.e(TAG, "<abandonAudioFocus()> abandon audio focus faild");
                mGetFocus = true;
            }
        }
    }

    public boolean isListener(OnStateChangedListener listener) {
        return mOnStateChangedListener == listener;
    }
    
    /**
     * M: check if the storage Uri is current operation path
     * @param storageUri
     *          same as file:///storage/sdcard0
     * @return
     */
    private boolean isCurrentAccessStorage(Uri storageUri) {
        if(mCurrentFilePath == null || storageUri == null 
                || !storageUri.getScheme().endsWith("file")) {
            return false;
        }
        String storagePath = storageUri.getPath();
        String currentOperationPath = mCurrentFilePath.substring(0, storagePath.length());
        return currentOperationPath.equals(storagePath);
    }
    
    /**
     *  check if current availabel storage is lower than LOW_STORAGE_THRESHOLD
     *  
     * @return if current availabel storage is lower than LOW_STORAGE_THRESHOLD
     */
    public boolean isStorageLower() {
        try {
            String storageState = mStorageManager
                    .getVolumeState(mStorageManager.getDefaultPath());
            if ((null == storageState)
                    || storageState.equals(Environment.MEDIA_MOUNTED)) {
                String storageDirectory = mStorageManager.getDefaultPath();
                /**
                 * M:Modified for SD card hot plug-in/out. Should to check the
                 * savePath of the current file rather than default write path.@{
                 */
                if (mCurrentFilePath != null) {
                    int index = mCurrentFilePath.indexOf(RECORDING, 0) - 1;
                    storageDirectory = mCurrentFilePath.substring(0, index);
                }
                /**@}*/
                StatFs stat = new StatFs(storageDirectory);
                return ((long) stat.getAvailableBlocks()
                        * (long) stat.getBlockSize()) < LOW_STORAGE_THRESHOLD;
            } else {
                return true;
            }
        } catch (IllegalStateException ex) {
            // if we can't stat the filesystem then we don't know how many
            // free bytes exist. It might be zero but just leave it
            // blank since we really don't know.
            return true;
        }
    }

    private final Runnable mRemainingTimeCalculateRunnable = new Runnable() {
        public void run() {
            mRemainingTime = mRemainingTimeCalculator.timeRemaining(false);
            LogUtils.i(TAG, "mRemainingTime is :"+ mRemainingTime);
            if (mRemainingTime <= 0) {
                stopRecord();
            } else {
                mHandler.postDelayed(mRemainingTimeCalculateRunnable, WAIT_TIME);
            }
        }
    };

}
