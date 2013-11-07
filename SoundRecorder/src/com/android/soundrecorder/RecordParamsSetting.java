package com.android.soundrecorder;

import android.media.MediaRecorder;
import android.os.SystemProperties;

import com.android.soundrecorder.LogUtils;
import com.mediatek.common.featureoption.FeatureOption;
import com.mediatek.media.MediaRecorderEx;

/**
 * M: We use this class to do operations related with record params.
 * When recording, we can get all record params according to input params.
 */ 
public class RecordParamsSetting {
    
    //M: All params will be used when record
    static public class RecordParams {
        public int mAudioChannels = AUDIO_CHANNELS_MONO;
        public int mAudioEncoder = -1;
        public int mAudioEncodingBitRate = -1;
        // M: add for MMS, when launch from MMS, for more accurate timing
        // if AUDIO_AMR.equals(requestType), bit rate used for calculate remainingtime 
        // is not ENCODE_BITRATE_AMR 12200, but 12800 
        public int mRemainingTimeCalculatorBitRate = -1;
        public int mAudioSamplingRate = -1;
        public String mExtension = "";
        public String mMimeType = "";
        public int mHDRecordMode = -1;
        public int mOutputFormat = -1;
        public boolean[] mAudioEffect = null;
    }

    public static final int FORMAT_HIGH = 0;
    public static final int FORMAT_MID = 1;
    public static final int FORMAT_LOW = 2;
    public static final int MODE_NORMAL = 0;
    public static final int MODE_INDOOR = 1;
    public static final int MODE_OUTDOOR = 2;

    public static final int ENCODE_BITRATE_AMR = 12200;
    public static final int ENCODE_BITRATE_3GPP = 12200;
    public static final int ENCODE_BITRATE_AWB = 28500;
    public static final int ENCODE_BITRATE_AAC = 128000;
    public static final int ENCODE_BITRATE_VORBIS = 128000;

    public static final int SAMPLE_RATE_AAC = 48000;
    public static final int SAMPLE_RATE_AWB = 16000;
    public static final int SAMPLE_RATE_AMR = 8000;
    public static final int SAMPLE_RATE_VORBIS = 48000;

    public static final int AUDIO_CHANNELS_MONO = 1;
    public static final int AUDIO_CHANNELS_STEREO = 2;

    public static final String NOT_LIMIT_TYPE = "*/*";
    public static final String AUDIO_NOT_LIMIT_TYPE = "audio/*";
    public static final String AUDIO_3GPP = "audio/3gpp";
    public static final String AUDIO_VORBIS = "audio/vorbis";
    public static final String AUDIO_AMR = "audio/amr";
    public static final String AUDIO_AWB = "audio/awb";
    public static final String AUDIO_OGG = "application/ogg";
    public static final String AUDIO_AAC = "audio/aac";
    
    //M: add for adpcm
    public static final int ENCODE_BITRATE_ADPCM = 128000;
    public static final int SAMPLE_RATE_ADPCM = 48000;
    public static final String HIGH_RECORD_ENCODER = "high_record_encoder";
    private static final String AUDIO_WAV = "audio/wav";

    public static final int EFFECT_AEC = 0;
    public static final int EFFECT_NS = 1;
    public static final int EFFECT_AGC = 2;

    private static final String TAG = "SR/RecordParamsSetting";
    private static int[] sFormatArray = null;
    private static int[] sModeArray = null;
    private static CharSequence[] sFormatSuffixArray = null;

    private static int[] sEffectArray = new int[] { R.string.recording_effect_AEC,
            R.string.recording_effect_NS, R.string.recording_effect_AGC };

    static RecordParams getRecordParams(String requestType, int selectFormat, int selectMode,
            boolean[] selectEffect) {
        RecordParams recordParams = new RecordParams();
        if (canSelectEffect()) {
            recordParams.mAudioEffect = selectEffect;
        }

        if (AUDIO_NOT_LIMIT_TYPE.equals(requestType) || NOT_LIMIT_TYPE.equals(requestType)) {
            if (FeatureOption.HAVE_VORBISENC_FEATURE) {
                requestType = AUDIO_VORBIS;
            } else {
                requestType = AUDIO_3GPP;
            }
        }

        if (AUDIO_AMR.equals(requestType)) {
            recordParams.mAudioEncodingBitRate = ENCODE_BITRATE_AMR;
            recordParams.mRemainingTimeCalculatorBitRate = 12800;
            recordParams.mAudioEncoder = MediaRecorder.AudioEncoder.AMR_NB;
            recordParams.mAudioSamplingRate = SAMPLE_RATE_AMR;
            recordParams.mExtension = ".amr";
            recordParams.mMimeType = AUDIO_AMR;
            recordParams.mOutputFormat = MediaRecorder.OutputFormat.AMR_NB;
        } else if (AUDIO_AWB.equals(requestType)) {
            recordParams.mAudioEncodingBitRate = ENCODE_BITRATE_AWB;
            recordParams.mRemainingTimeCalculatorBitRate = ENCODE_BITRATE_AWB;
            recordParams.mAudioSamplingRate = SAMPLE_RATE_AWB;
            recordParams.mAudioEncoder = MediaRecorder.AudioEncoder.AMR_WB;
            recordParams.mExtension = ".awb";
            recordParams.mMimeType = AUDIO_AWB;
            recordParams.mOutputFormat = MediaRecorder.OutputFormat.THREE_GPP;
        } else if (AUDIO_AAC.equals(requestType)) {
            recordParams.mAudioChannels = AUDIO_CHANNELS_STEREO;
            recordParams.mAudioEncoder = MediaRecorder.AudioEncoder.AAC;
            recordParams.mAudioEncodingBitRate = ENCODE_BITRATE_AAC;
            recordParams.mRemainingTimeCalculatorBitRate = ENCODE_BITRATE_AAC;
            recordParams.mAudioSamplingRate = SAMPLE_RATE_AAC;
            recordParams.mExtension = ".aac";
            recordParams.mMimeType = AUDIO_AAC;
            recordParams.mOutputFormat = MediaRecorder.OutputFormat.AAC_ADTS;
        } else if (AUDIO_3GPP.equals(requestType) || (AUDIO_VORBIS.equals(requestType))) {
            switch (selectFormat) {
            case FORMAT_HIGH:
                if (FeatureOption.HAVE_VORBISENC_FEATURE) {
                    recordParams.mAudioChannels = AUDIO_CHANNELS_STEREO;
                    recordParams.mAudioEncoder = MediaRecorder.AudioEncoder.VORBIS;
                    recordParams.mAudioEncodingBitRate = ENCODE_BITRATE_VORBIS;
                    recordParams.mRemainingTimeCalculatorBitRate = ENCODE_BITRATE_VORBIS;
                    recordParams.mAudioSamplingRate = SAMPLE_RATE_VORBIS;
                    recordParams.mExtension = ".ogg";
                    recordParams.mMimeType = AUDIO_OGG;
                    recordParams.mOutputFormat = MediaRecorder.OutputFormat.OUTPUT_FORMAT_OGG;
                } else if (FeatureOption.HAVE_AACENCODE_FEATURE) {
                    recordParams.mAudioEncoder = MediaRecorder.AudioEncoder.AAC;
                    recordParams.mAudioEncodingBitRate = ENCODE_BITRATE_AAC;
                    recordParams.mRemainingTimeCalculatorBitRate = ENCODE_BITRATE_AAC;
                    recordParams.mAudioSamplingRate = SAMPLE_RATE_AAC;
                    recordParams.mExtension = ".3gpp";
                    recordParams.mMimeType = AUDIO_3GPP;
                    recordParams.mOutputFormat = MediaRecorder.OutputFormat.THREE_GPP;
                }
                // M: add for adpcm
                setRecordParamsFromSystemProperties(recordParams);
                break;
            case FORMAT_MID:
                recordParams.mAudioEncoder = MediaRecorder.AudioEncoder.AMR_WB;
                recordParams.mAudioEncodingBitRate = ENCODE_BITRATE_AWB;
                recordParams.mRemainingTimeCalculatorBitRate = ENCODE_BITRATE_AWB;
                recordParams.mAudioSamplingRate = SAMPLE_RATE_AWB;
                recordParams.mExtension = ".3gpp";
                recordParams.mMimeType = AUDIO_3GPP;
                recordParams.mOutputFormat = MediaRecorder.OutputFormat.THREE_GPP;
                break;
            case FORMAT_LOW:
                recordParams.mAudioEncoder = MediaRecorder.AudioEncoder.AMR_NB;
                recordParams.mAudioEncodingBitRate = ENCODE_BITRATE_AMR;
                recordParams.mRemainingTimeCalculatorBitRate = ENCODE_BITRATE_AMR;
                recordParams.mAudioSamplingRate = SAMPLE_RATE_AMR;
                recordParams.mExtension = ".amr";
                recordParams.mMimeType = AUDIO_AMR;
                recordParams.mOutputFormat = MediaRecorder.OutputFormat.AMR_NB;
                break;
            default:
                LogUtils.e(TAG, "<getRecordParams> selectFormat is out of range");
                break;
            }
        } else {
            throw new IllegalArgumentException("Invalid output file type requested");
        }

        if (FeatureOption.MTK_AUDIO_HD_REC_SUPPORT) {
            switch (selectMode) {
            case MODE_NORMAL:
                recordParams.mHDRecordMode = MediaRecorderEx.HDRecordMode.NORMAL;
                LogUtils.i(TAG, "<getRecordParams> mHDRecordMode" + "is MODE_NORMAL");
                break;
            case MODE_INDOOR:
                recordParams.mHDRecordMode = MediaRecorderEx.HDRecordMode.INDOOR;
                LogUtils.i(TAG, "<getRecordParams> mHDRecordMode" + "is MODE_INDOOR");
                break;
            case MODE_OUTDOOR:
                recordParams.mHDRecordMode = MediaRecorderEx.HDRecordMode.OUTDOOR;
                LogUtils.i(TAG, "<getRecordParams> mHDRecordMode" + "is MODE_OUTDOOR");
                break;
            default:
                LogUtils.e(TAG, "<getRecordParams> selectMode is out of range");
                break;
            }
        }

        return recordParams;
    }

    // M: add for adpcm
    static void setRecordParamsFromSystemProperties(RecordParams recordParams) {
        // M: add for adpcm
        // highRecordEncoder == 0 or -1 or other values vorbis->aac
        // highRecordEncoder == 1 vorbis
        // highRecordEncoder == 2 aac
        // highRecordEncoder == 3 adpcm
        int highRecordEncoder = SystemProperties.getInt(HIGH_RECORD_ENCODER,-1);
        LogUtils.i(TAG, "<setRecordRelatedParamsFromSystemProperties> highRecordEncoder = " + highRecordEncoder);
        switch (highRecordEncoder){
        case 1:
            if (FeatureOption.HAVE_VORBISENC_FEATURE) {
                LogUtils.i(TAG, "<setRecordParamsFromSystemProperties> highRecordEncoder = " 
                        + highRecordEncoder + ", vorbis");
                recordParams.mAudioChannels = AUDIO_CHANNELS_STEREO;
                recordParams.mAudioEncoder = MediaRecorder.AudioEncoder.VORBIS;
                recordParams.mAudioEncodingBitRate = ENCODE_BITRATE_VORBIS;
                recordParams.mRemainingTimeCalculatorBitRate = ENCODE_BITRATE_VORBIS;
                recordParams.mAudioSamplingRate = SAMPLE_RATE_VORBIS;
                recordParams.mExtension = ".ogg";
                recordParams.mMimeType = AUDIO_OGG;
                recordParams.mOutputFormat = MediaRecorder.OutputFormat.OUTPUT_FORMAT_OGG;
            }
            break;
        case 2:
            if (FeatureOption.HAVE_AACENCODE_FEATURE) {
                LogUtils.i(TAG, "<setRecordParamsFromSystemProperties> highRecordEncoder = " 
                        + highRecordEncoder + ", aac");
                recordParams.mAudioEncoder = MediaRecorder.AudioEncoder.AAC;
                recordParams.mAudioEncodingBitRate = ENCODE_BITRATE_AAC;
                recordParams.mRemainingTimeCalculatorBitRate = ENCODE_BITRATE_AAC;
                recordParams.mAudioSamplingRate = SAMPLE_RATE_AAC;
                recordParams.mExtension = ".3gpp";
                recordParams.mMimeType = AUDIO_3GPP;
                recordParams.mOutputFormat = MediaRecorder.OutputFormat.THREE_GPP;
            }
            break;
        case 3:
            LogUtils.i(TAG, "<setRecordParamsFromSystemProperties> highRecordEncoder = " 
                    + highRecordEncoder + ", adpcm");
            recordParams.mAudioEncoder = MediaRecorder.AudioEncoder.ADPCM;
            recordParams.mAudioEncodingBitRate = ENCODE_BITRATE_ADPCM;
            recordParams.mRemainingTimeCalculatorBitRate = ENCODE_BITRATE_ADPCM;
            recordParams.mAudioSamplingRate = SAMPLE_RATE_ADPCM;
            recordParams.mExtension = ".wav";
            recordParams.mMimeType = AUDIO_WAV;
            recordParams.mOutputFormat = MediaRecorder.OutputFormat.OUTPUT_FORMAT_WAV;
            break;
        default:
            break;
        }
    }
    
    static boolean canSelectFormat() {
        return (FeatureOption.HAVE_AACENCODE_FEATURE 
                || FeatureOption.HAVE_AWBENCODE_FEATURE 
                || FeatureOption.HAVE_VORBISENC_FEATURE);
    }

    static boolean canSelectMode() {
        return FeatureOption.MTK_AUDIO_HD_REC_SUPPORT;
    }

    static boolean canSelectEffect() {
        return FeatureOption.NATIVE_AUDIO_PREPROCESS_ENABLE;
    }

    static boolean isAvailableRequestType(String requestType) {
        return (AUDIO_AMR.equals(requestType) || AUDIO_3GPP.equals(requestType)
                || AUDIO_NOT_LIMIT_TYPE.equals(requestType) || NOT_LIMIT_TYPE.equals(requestType));
    }

    static int[] getModeStringIDArray() {
        int[] modeIDArray = new int[3];
        modeIDArray[MODE_NORMAL] = R.string.recording_mode_nomal;
        modeIDArray[MODE_INDOOR] = R.string.recording_mode_meeting;
        modeIDArray[MODE_OUTDOOR] = R.string.recording_mode_lecture;
        sModeArray = new int[3];
        sModeArray[0] = MODE_NORMAL;
        sModeArray[1] = MODE_INDOOR;
        sModeArray[2] = MODE_OUTDOOR;
        return modeIDArray;
    }

    static int[] getFormatStringIDArray() {
        String suffixOgg = "(.ogg)";
        String suffix3gpp = "(.3gpp)";
        String suffixAmr = "(.amr)";
        int format0 = -1;
        int format1 = -1;
        int format2 = -1;
        int[] formartStringIDArray = null;
        if (FeatureOption.HAVE_AACENCODE_FEATURE || FeatureOption.HAVE_VORBISENC_FEATURE) {
            if (FeatureOption.HAVE_AWBENCODE_FEATURE) {
                sFormatSuffixArray = new CharSequence[3];
                formartStringIDArray = new int[3];
                sFormatSuffixArray[0] = FeatureOption.HAVE_VORBISENC_FEATURE ? suffixOgg
                        : suffix3gpp;
                sFormatSuffixArray[1] = suffix3gpp;
                sFormatSuffixArray[2] = suffixAmr;
                formartStringIDArray[0] = R.string.recording_format_high;
                formartStringIDArray[1] = R.string.recording_format_mid;
                formartStringIDArray[2] = R.string.recording_format_low;
                format0 = FORMAT_HIGH;
                format1 = FORMAT_MID;
                format2 = FORMAT_LOW;
            } else {
                sFormatSuffixArray = new CharSequence[2];
                formartStringIDArray = new int[2];
                sFormatSuffixArray[0] = FeatureOption.HAVE_VORBISENC_FEATURE ? suffixOgg
                        : suffix3gpp;
                sFormatSuffixArray[1] = suffixAmr;
                formartStringIDArray[0] = R.string.recording_format_high;
                formartStringIDArray[1] = R.string.recording_format_low;
                format0 = FORMAT_HIGH;
                format1 = FORMAT_LOW;
            }
        } else if (FeatureOption.HAVE_AWBENCODE_FEATURE) {
            sFormatSuffixArray = new CharSequence[1];
            formartStringIDArray = new int[1];
            sFormatSuffixArray[0] = suffixAmr;
            formartStringIDArray[0] = R.string.recording_format_low;
            format0 = FORMAT_LOW;
        } else {
            LogUtils.e(TAG, "<dlgChooseChannel> No featureOption enable");
        }
        sFormatArray = new int[3];
        sFormatArray[0] = format0;
        sFormatArray[1] = format1;
        sFormatArray[2] = format2;

        return formartStringIDArray;
    }

    static int[] getEffectStringIDArray() {
        return sEffectArray;
    }

    static CharSequence[] getFormatSuffixStringArray() {
        if (null == sFormatSuffixArray) {
            getFormatStringIDArray();
        }
        return sFormatSuffixArray;
    }

    static int getSelectFormat(int which) {
        return sFormatArray[which];
    }

    static int getSelectMode(int which) {
        return sModeArray[which];
    }
}