package com.android.camera;

import com.mediatek.common.featureoption.FeatureOption;

public class FeatureSwitcher {
    private static final String TAG = "FeatureSwitcher";
    private static final boolean LOG = Log.LOGV;
    
    public static boolean isVssEnabled() {
        boolean enabled = FeatureOption.MTK_VSS_SUPPORT;
        if (LOG) {
            Log.v(TAG, "isVssEnabled() return " + enabled);
        }
        return enabled;
    }
    
    public static boolean isHdRecordingEnabled() {
        boolean enabled = FeatureOption.MTK_AUDIO_HD_REC_SUPPORT;
        if (LOG) {
            Log.v(TAG, "isHdRecordingEnabled() return " + enabled);
        }
        return enabled;
    }
    
    public static boolean isStereo3dEnable() {
        return false;
    }
    
    public static boolean isStereoSingle3d() {
        return false;
    }
    
    // M: used as a flag to decide enable video live effect or not.
    public static boolean isVideoLiveEffectEnabled() {
        boolean enabled = true;
        if (LOG) {
            Log.v(TAG, "isVideoLiveEffectEnabled() return " + enabled);
        }
        return enabled;
    }
    
    // M: used as a flag to decide enable front camera video live effect or not.
    public static boolean isFrontVideoLiveEffectEnabled() {
        boolean enabled = true;
        if (LOG) {
            Log.v(TAG, "isFrontVideoLiveEffectEnabled() return " + enabled);
        }
        return enabled;
    }
    
    // M: used as a flag to decide can slide to gallery or not
    public static boolean isSlideEnabled() {
        boolean enabled = true;
        if (LOG) {
            Log.v(TAG, "isSlideEnabled() return " + enabled);
        }
        return enabled;
    }

    // M: used as a flag to save origin or not in HDR mode
    public static boolean isHdrOriginalPictureSaved() {
        boolean enabled = true;
        if (LOG) {
            Log.v(TAG, "isHdrOriginalPictureSaved() return " + enabled);
        }
        return enabled;
    }

    // M: used as a flag to save origin or not in FaceBeauty mode
    public static boolean isFaceBeautyOriginalPictureSaved() {
        boolean enabled = true;
        if (LOG) {
            Log.v(TAG, "isFaceBeautyOriginalPictureSaved() return " + enabled);
        }
        return enabled;
    }

    public static boolean isContinuousFocusEnabledWhenTouch() {
        boolean enabled = false;
        if (LOG) {
            Log.v(TAG, "isContinuousFocusEnabledWhenTouch() return " + enabled);
        }
        return enabled;
    }
    
    public static boolean isThemeEnabled() {
        boolean enabled = FeatureOption.MTK_THEMEMANAGER_APP;
        if (LOG) {
            Log.v(TAG, "isThemeEnabled() return " + enabled);
        }
        return enabled;
    }

    public static boolean isVoiceEnabled() {
        boolean enabled = FeatureOption.MTK_VOICE_UI_SUPPORT;
        if (LOG) {
            Log.v(TAG, "isVoiceEnabled() return " + enabled);
        }
        return enabled;
    }
}
