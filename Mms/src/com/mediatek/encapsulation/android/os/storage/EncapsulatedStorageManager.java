
package com.mediatek.encapsulation.android.os.storage;

import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.os.SystemProperties;
import android.os.Environment;
import android.os.RemoteException;
import android.content.Context;
import android.util.Log;
import com.mediatek.encapsulation.EncapsulationConstant;
import com.mediatek.encapsulation.MmsLog;

import java.io.File;
import java.io.IOException;

public class EncapsulatedStorageManager {

    private static final String PROP_SD_DEFAULT_PATH = "persist.sys.sd.defaultpath";
    private File mMTKExternalCacheDir;
    /** M: MTK ADD */
    private static StorageManager mStorageMamatger;
    private static final String TAG = "EncapsulatedStorageManager";

    /** M: MTK ADD */
    public EncapsulatedStorageManager(Context context) {
        if (null == mStorageMamatger && null != context) {
            mStorageMamatger = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        }
    }

    /**
     * Gets the state of a volume via its mountpoint.
     * 
     */
    public String getVolumeState(String mountPoint) {
        if (EncapsulationConstant.USE_MTK_PLATFORM) {
            return mStorageMamatger.getVolumeState(mountPoint);
        } else {
            /** M: Can not complete for this branch. */
            MmsLog.d("Encapsulation issue", "EncapsulatedStorageManager -- getVolumeState()");
            return new String();
        }
    }

    /**
     * Returns list of all mountable volumes.
     */
    public StorageVolume[] getVolumeList() {
        if (EncapsulationConstant.USE_MTK_PLATFORM) {
            return mStorageMamatger.getVolumeList();
        } else {
            /** M: Can not complete for this branch. */
            MmsLog.d("Encapsulation issue", "EncapsulatedStorageManager -- getVolumeList()");
            return null;
        }
    }

    /**
     * Returns default path for writing.
     * 
     */
    public static String getDefaultPath() {
        if (EncapsulationConstant.USE_MTK_PLATFORM) {
            return mStorageMamatger.getDefaultPath();
        } else {
            String path = Environment.getExternalStorageDirectory().getPath();

            try {
                path = SystemProperties.get(PROP_SD_DEFAULT_PATH, path);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "IllegalArgumentException when get default path:" + e);
            }

            // MOTA upgrade from ICS to JB, update DefaultPath to JB design
            if (path.equals("/mnt/sdcard")) {
                path = "/storage/sdcard0";
                setDefaultPath(path);
            } else if (path.equals("/mnt/sdcard2")) {
                path = "/storage/sdcard1";
                setDefaultPath(path);
            }

            Log.i(TAG, "getDefaultPath path=" + path);
            return path;
        }
    }

    /**
     * set default path for APP to storage data. this ONLY can used by settings.
     * 
     */
    public static void setDefaultPath(String path) {
        if (EncapsulationConstant.USE_MTK_PLATFORM) {
            mStorageMamatger.setDefaultPath(path);
        } else {

            Log.i(TAG, "setDefaultPath path=" + path);
            if (path == null) {
                Log.e(TAG, "setDefaultPath error! path=null");
                return;
            }

            try {
                SystemProperties.set(PROP_SD_DEFAULT_PATH, path);
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "IllegalArgumentException when set default path:" + e);
            }
        }
    }

    /**
     * Generates the path to Gallery.
     * 
     */
    public File getMTKExternalCacheDir(String packageName) {
        if (EncapsulationConstant.USE_MTK_PLATFORM) {
            return mStorageMamatger.getMTKExternalCacheDir(packageName);
        } else {
            if (null == packageName) {
                Log.w(TAG, "packageName = null!");
                return null;
            }
            String path = getDefaultPath();
            if (path.equals(Environment.getExternalStorageDirectory().getPath())) {
                mMTKExternalCacheDir = Environment.getExternalStorageAppCacheDirectory(packageName);
                if (!mMTKExternalCacheDir.exists()) {
                    try {
                        (new File(Environment.getExternalStorageAndroidDataDir(), ".nomedia"))
                                .createNewFile();
                    } catch (IOException e) {
                        // do nothing
                    }
                    if (!mMTKExternalCacheDir.mkdirs()) {
                        Log.w(TAG, "Unable to create external cache directory");
                        return null;
                    }
                }
            } else {
                mMTKExternalCacheDir = new File(new File(new File(new File(new File(path),
                        "Android"), "data"), packageName), "cache");
                if (!mMTKExternalCacheDir.exists()) {
                    try {
                        (new File(new File(new File(new File(path), "Android"), "data"), ".nomedia"))
                                .createNewFile();
                    } catch (IOException e) {
                        // do nothing
                    }
                    if (!mMTKExternalCacheDir.mkdirs()) {
                        Log.w(TAG, "Unable to create external cache directory");
                        return null;
                    }
                }
            }
            return mMTKExternalCacheDir;
        }
    }

    /**
     * Returns external SD card path.
     * 
     */
    public String getExternalStoragePath() {
        if (EncapsulationConstant.USE_MTK_PLATFORM) {
            return mStorageMamatger.getExternalStoragePath();
        } else {
            /** M: Can not complete for this branch. */
            MmsLog.d("Encapsulation issue", "EncapsulatedStorageManager -- getExternalStoragePath()");
            return new String();
        }
    }

    /**
     * Returns internal Storage path.
     * 
     */
    public String getInternalStoragePath() {
        if (EncapsulationConstant.USE_MTK_PLATFORM) {
            return mStorageMamatger.getInternalStoragePath();
        } else {
            /** M: Can not complete for this branch. */
            MmsLog.d("Encapsulation issue", "EncapsulatedStorageManager -- getInternalStoragePath()");
            return new String();
        }
    }

}
