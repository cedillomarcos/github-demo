package com.mediatek.contacts.calllog;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.provider.Telephony.SIMInfo;
import android.util.Log;

import com.android.contacts.ContactsUtils;
import com.android.contacts.R;
import com.mediatek.phone.SIMInfoWrapper;

import java.util.List;

public class CallLogSimInfoHelper {

    static final int SLOT_ID_FIRST = 0;
    static final int SLOT_ID_SECOND = 1;
    
    private static final String TAG = "CallLogSimInfoHelper";

    private Resources mResources;
    private String mSipCallDisplayName = "";
    private Drawable mDrawableSimSipColor;
    private Drawable mDrawableSimLockedColor;
    private Drawable mDrawableSimColor1;
    private Drawable mDrawableSimColor2;
    private int mInsertSimColor1 = -1;
    private int mInsertSimColor2 = -1;

    /**
     * Construct function
     * @param resources need resources
     */
    public CallLogSimInfoHelper(Resources resources) {
        mResources = resources;
    }

    /**
     * get sim name by sim id
     * 
     * @param simId from datebase
     * @return string sim name
     */
    public String getSimDisplayNameById(int simId) {
        if (ContactsUtils.CALL_TYPE_SIP == simId) {
            if ("".equals(mSipCallDisplayName)) {
                mSipCallDisplayName = mResources.getString(R.string.call_sipcall);
            }
            return mSipCallDisplayName;
        } else if (ContactsUtils.CALL_TYPE_NONE == simId) {
            return "";
        } else {
            return SIMInfoWrapper.getDefault().getSimDisplayNameById(simId);
        }
    }

    /**
     * get sim color drawable by sim id
     * 
     * @param simId form datebases
     * @return Drawable sim color
     */
    public Drawable getSimColorDrawableById(int simId) {
        log("getSimColorDrawableById() simId == [" + simId + "]");
        int mCalllogSimnameHeight = (int) mResources
                .getDimension(R.dimen.calllog_list_item_simname_height);
        if (ContactsUtils.CALL_TYPE_SIP == simId) {
            // The request is sip color
            if (null == mDrawableSimSipColor) {
                Drawable dw = (NinePatchDrawable) mResources.getDrawable(R.drawable.sim_dark_internet_call);
                if (null != dw && dw instanceof NinePatchDrawable) {
                    mDrawableSimSipColor = dw;
                } else {
                    return null;
                }
            }
            return mDrawableSimSipColor.getConstantState().newDrawable();
        } else if (ContactsUtils.CALL_TYPE_NONE == simId) {
            return null;
        } else {
            int color = SIMInfoWrapper.getDefault().getInsertedSimColorById(simId);
            log("getSimColorDrawableById() color == [" + color + "]");
            if (-1 != color) {
                if (SLOT_ID_FIRST == SIMInfoWrapper.getDefault().getSlotIdBySimId(simId)) {
                    if (null == mDrawableSimColor1 || mInsertSimColor1 != color) {
                        int simColorResId = SIMInfoWrapper.getDefault()
                                .getSimBackgroundDarkResByColorId(color);
                        mInsertSimColor1 = color;
                        Drawable dw = (NinePatchDrawable) mResources.getDrawable(simColorResId);
                        if (null != dw && dw instanceof NinePatchDrawable) {
                            mDrawableSimColor1 = dw;
                        } else {
                            return null;
                        }
                    }
                    return mDrawableSimColor1;
                } else if (SLOT_ID_SECOND == SIMInfoWrapper.getDefault().getSlotIdBySimId(simId)) {
                    if (null == mDrawableSimColor2 || mInsertSimColor2 != color) {
                        int simColorResId = SIMInfoWrapper.getDefault()
                                .getSimBackgroundDarkResByColorId(color);
                        mInsertSimColor2 = color;
                        Drawable dw = (NinePatchDrawable) mResources.getDrawable(simColorResId);
                        if (null != dw && dw instanceof NinePatchDrawable) {
                            mDrawableSimColor2 = dw;
                        } else {
                            return null;
                        }
                    }
                    return mDrawableSimColor2;
                } else {
                    // should never been here
                    return null;
                }
            } else {
                // The request color is not inserted sim currently
                if (null == mDrawableSimLockedColor) {
                    Drawable dw = (NinePatchDrawable) mResources.getDrawable(R.drawable.sim_dark_not_activated);
                    if (null != dw && dw instanceof NinePatchDrawable) {
                        mDrawableSimLockedColor = dw;
                    } else {
                            return null;
                    }
                }
                // Not inserted sim has same background but different length,
                //so can not share same drawable
                return mDrawableSimLockedColor.getConstantState().newDrawable();
            }
        }
    }

    /**
     * clear cache info
     */
    /**
     * 
    public void resetCacheInfo() {
        mDrawableSimColor1 = null;
        mDrawableSimColor2 = null;
        mInsertSimColor1 = -1;
        mInsertSimColor2 = -1;
    }
     */

    /**
     * get sim id by slot id
     * 
     * @param slot slot id
     * @return sim id
     */
    public static int getSimIdBySlotID(final int slot) {
        List<SIMInfo> insertedSIMInfoList = SIMInfoWrapper.getDefault().getInsertedSimInfoList();

        if (null == insertedSIMInfoList) {
            return -1;
        }

        for (int i = 0; i < insertedSIMInfoList.size(); ++i) {
            if (slot == insertedSIMInfoList.get(i).mSlot) {
                return (int) insertedSIMInfoList.get(i).mSimId;
            }
        }
        return -1;
    }
    
    private void log(final String log) {
        Log.i(TAG, log);
    }
}
