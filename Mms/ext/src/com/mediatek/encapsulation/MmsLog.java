package com.mediatek.encapsulation;

import android.os.Build;
import android.util.Log;

import com.mediatek.encapsulation.EncapsulationConstant;
import com.mediatek.xlog.Xlog;

/**
 * M: JB, add it <b>permanent</b>, remove "Xlog" mark when dependency is available.
 *
 * this change will affect some classes which import com.mediatek.xlog.Xlog.
 *
 * remove step:
 * remove the class and reference, and remove mark "import com.mediatek.xlog.Xlog;" in related classes,
 * if class com.mediatek.xlog.Xlog is available.
 */

public class MmsLog {

    public static final boolean DEBUG = Build.TYPE.equals("eng") ? true : false;

    public static void v(String tag, String msg) {
        if (EncapsulationConstant.USE_MTK_PLATFORM) {
            Xlog.v(tag, msg);
        } else {
            if (DEBUG) {
                Log.v(tag, msg);
            }
        }
    }

    public static void e(String tag, String msg) {
        if (EncapsulationConstant.USE_MTK_PLATFORM) {
            Xlog.e(tag, msg);
        } else {
            if (DEBUG) {
                Log.e(tag, msg);
            }
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (EncapsulationConstant.USE_MTK_PLATFORM) {
            Xlog.e(tag, msg, tr);
        } else {
            if (DEBUG) {
                Log.e(tag, msg, tr);
            }
        }
    }

    public static void i(String tag, String msg) {
        if (EncapsulationConstant.USE_MTK_PLATFORM) {
            Xlog.i(tag, msg);
        } else {
            if (DEBUG) {
                Log.i(tag, msg);
            }
        }
    }

    public static void d(String tag, String msg) {
        if (EncapsulationConstant.USE_MTK_PLATFORM) {
            Xlog.d(tag, msg);
        } else {
            if (DEBUG) {
                Log.d(tag, msg);
            }
        }
    }

    public static void d(String tag, String msg, Throwable tr) {
        if (EncapsulationConstant.USE_MTK_PLATFORM) {
            Xlog.d(tag, msg, tr);
        } else {
            if (DEBUG) {
                Log.d(tag, msg, tr);
            }
        }
    }

    public static void w(String tag, String msg) {
        if (EncapsulationConstant.USE_MTK_PLATFORM) {
            Xlog.w(tag, msg);
        } else {
            if (DEBUG) {
                Log.w(tag, msg);
            }
        }
    }

    public static void w(String tag, String msg, Throwable tr) {
        if (EncapsulationConstant.USE_MTK_PLATFORM) {
            Xlog.w(tag, msg, tr);
        } else {
            if (DEBUG) {
                Log.w(tag, msg, tr);
            }
        }
    }
}
