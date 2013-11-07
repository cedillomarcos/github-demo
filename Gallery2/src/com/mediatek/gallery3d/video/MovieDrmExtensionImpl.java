package com.mediatek.gallery3d.video;

import android.content.Context;
import android.drm.DrmManagerClient;
import android.drm.DrmManagerClient.DrmOperationListener;
import android.drm.DrmStore;
import android.drm.DrmUtils;
import android.drm.DrmUtils.DrmProfile;
import android.net.Uri;

import com.mediatek.gallery3d.ext.IMovieDrmExtension.IMovieDrmCallback;
import com.mediatek.gallery3d.ext.IMovieItem;
import com.mediatek.gallery3d.ext.MovieDrmExtension;
import com.mediatek.gallery3d.ext.MtkLog;

public class MovieDrmExtensionImpl extends MovieDrmExtension {
    private static final String TAG = "MovieDrmExtensionImpl";
    private static final boolean LOG = true;
    
    @Override
    public boolean handleDrmFile(final Context context, final IMovieItem item, final IMovieDrmCallback callback) {
        boolean handle = false;
        if (handleDrmFile(context, item.getUri(), new DrmOperationListener() {

            public void onOperated(int type) {
                if (LOG) {
                    MtkLog.v(TAG, "onOperated(" + type + ")");
                }
                switch (type) {
                case DrmOperationListener.CONTINUE:
                    consume(context, item.getUri(), DrmStore.Action.PLAY);
                    if (callback != null) {
                        callback.onContinue();
                    }
                    break;
                case DrmOperationListener.STOP:
                    if (callback != null) {
                        callback.onStop();
                    }
                    break;
                default:
                    break;
                }
            }

        })) {
            handle = true;
        }
        return handle;
    }
    
    @Override
    public boolean canShare(final Context context, final IMovieItem item) {
        return canShare(context, item.getOriginalUri());
    }
    
    private static DrmManagerClient sDrmClient;
    private static DrmManagerClient ensureDrmClient(final Context context) {
        if (sDrmClient == null) {
            sDrmClient = new DrmManagerClient(context.getApplicationContext());
        }
        return sDrmClient;
    }
    // used for movie player to check for videos. Action type PLAY
    private static boolean handleDrmFile(final Context context, final Uri uri, final DrmOperationListener listener) {
        if (LOG) {
            MtkLog.v(TAG, "handleDrmFile(" + uri + ", " + listener + ")");
        }
        final DrmManagerClient client = ensureDrmClient(context);
        boolean result = false;
        final DrmProfile info = DrmUtils.getDrmProfile(context, uri, client);
        if (info != null && info.isDrm && info.method != DrmStore.DrmMethod.METHOD_FL) {
            int rightsStatus = DrmStore.RightsStatus.RIGHTS_INVALID;
            try {
                rightsStatus = client.checkRightsStatusForTap(uri, DrmStore.Action.PLAY);
            } catch (final IllegalArgumentException e) {
                MtkLog.w(TAG, "handleDrmFile() : raise exception, we assume invalid rights");
            }
            switch (rightsStatus) {
            case DrmStore.RightsStatus.RIGHTS_VALID:
                DrmManagerClient.showConsume(context, listener);
                result = true;
                break;
            case DrmStore.RightsStatus.RIGHTS_INVALID:
                client.showLicenseAcquisition(context, uri, listener);
                result = true;
                break;
            case DrmStore.RightsStatus.SECURE_TIMER_INVALID:
                DrmManagerClient.showSecureTimerInvalid(context, listener);
                result = true;
                break;
            default:
                break;
            }
        }
        if (LOG) {
            MtkLog.v(TAG, "handleDrmFile() return " + result);
        }
        return result;
    }
    
    private static int consume(final Context context, final Uri uri, final int action) {
        final DrmManagerClient client = ensureDrmClient(context);
        final int result = client.consume(uri, action);
        if (LOG) {
            MtkLog.v(TAG, "consume(" + uri + ", action=" + action + ") return " + result);
        }
        return result;
    }
    
    private static boolean canShare(final Context context, final Uri uri) {
        if (LOG) {
            MtkLog.v(TAG, "canShare(" + uri + ")");
        }
        final DrmManagerClient client = ensureDrmClient(context);
        boolean share = false;
        boolean isDrm = false;
        try {
            isDrm = client.canHandle(uri, null);
        } catch (final IllegalArgumentException e) {
            MtkLog.w(TAG, "canShare() : raise exception, we assume it's not a OMA DRM file");
        }

        if (isDrm) {
            int rightsStatus = DrmStore.RightsStatus.RIGHTS_INVALID;
            try {
                rightsStatus = client.checkRightsStatus(uri, DrmStore.Action.TRANSFER);
            } catch (final IllegalArgumentException e) {
                MtkLog.w(TAG, "canShare() : raise exception, we assume it has no rights to be shared");
            }
            share = (DrmStore.RightsStatus.RIGHTS_VALID == rightsStatus);
            if (LOG) {
                MtkLog.v(TAG, "canShare(" + uri + "), rightsStatus=" + rightsStatus);
            }
        } else {
            share = true;
        }
        if (LOG) {
            MtkLog.v(TAG, "canShare(" + uri + "), share=" + share);
        }
        return share;
    }
}
