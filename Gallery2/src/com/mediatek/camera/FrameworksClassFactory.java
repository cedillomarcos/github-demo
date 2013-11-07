package com.mediatek.camera;

import android.hardware.Camera;
import android.media.MediaRecorder;
import android.util.Log;

import com.mediatek.common.featureoption.FeatureOption;
import com.mediatek.mock.hardware.MockCamera;
import com.mediatek.mock.media.MockMediaRecorder;

public class FrameworksClassFactory {
    private static final String TAG = "FrameworksClassFactory";
    private static final boolean LOG = true;
    private static final boolean MOCK_CAMERA;

    static {
        MOCK_CAMERA = FeatureOption.MTK_EMULATOR_SUPPORT;
    }

    public static boolean isMockCamera() {
        return MOCK_CAMERA;
    }

    public static ICamera openCamera(int cameraId) {
        if (MOCK_CAMERA) {
            return MockCamera.open(cameraId);
        } else {
            Camera camera = Camera.open(cameraId);
            if (null == camera) {
                Log.e(TAG, "openCamera:got null hardware camera!");
                return null;
            }
            // wrap it with ICamera
            return new AndroidCamera(camera);
        }
    }

    public static MediaRecorder getMediaRecorder() {
        if (MOCK_CAMERA) {
            return new MockMediaRecorder();
        } else {
            return new MediaRecorder();
        }
    }

}
