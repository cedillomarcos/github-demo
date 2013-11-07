package com.mediatek.contacts.simcontact;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.provider.Telephony.SIMInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.contacts.R;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.TelephonyProperties;
import com.google.common.annotations.VisibleForTesting;
import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.mediatek.phone.SIMInfoWrapper;
import com.mediatek.telephony.TelephonyManagerEx;

import java.util.HashMap;
import java.util.List;

public class SimCardUtils {
    public static final String TAG = "SimCardUtils";

    public interface SimSlot {
        int SLOT_NONE = -1;
        int SLOT_SINGLE = 0;
        int SLOT_ID1 = com.android.internal.telephony.PhoneConstants.GEMINI_SIM_1;
        int SLOT_ID2 = com.android.internal.telephony.PhoneConstants.GEMINI_SIM_2;
    }

    public interface SimType {
        String SIM_TYPE_USIM_TAG = "USIM";
        String SIM_TYPE_SIM_TAG = "SIM";

        int SIM_TYPE_SIM = 0;
        int SIM_TYPE_USIM = 1;

        //UIM
        int SIM_TYPE_UIM = 2;
        String SIM_TYPE_UIM_TAG = "UIM";
        //UIM
    }

    public static class SimUri {
        public static final String AUTHORITY = "icc";
        public static final Uri ICCURI = Uri.parse("content://icc/adn/");   
        public static final Uri ICCURI1 = Uri.parse("content://icc/adn1/");
        public static final Uri ICCURI2 = Uri.parse("content://icc/adn2/");
        
        public static final Uri ICCUSIMURI = Uri.parse("content://icc/pbr");
        public static final Uri ICCUSIM1URI = Uri.parse("content://icc/pbr1/");
        public static final Uri ICCUSIM2URI = Uri.parse("content://icc/pbr2/");
        
        public static final Uri SDNURI = Uri.parse("content://icc/sdn/");   
        public static final Uri SDNURI1 = Uri.parse("content://icc/sdn1/");
        public static final Uri SDNURI2 = Uri.parse("content://icc/sdn2/");
        
        public static Uri getSimUri(int slotId) {
            boolean isUsim = isSimUsimType(slotId);
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                if (slotId == 0) {
                    return isUsim ? ICCUSIM1URI : ICCURI1;
                } else {
                    return isUsim ? ICCUSIM2URI : ICCURI2;
                }
            } else {
                return isUsim ? ICCUSIMURI : ICCURI;
            }
        }
        
        public static Uri getSimSdnUri(int slotId) {           
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                if (slotId == 0) {
                    return SDNURI1;
                } else {
                    return SDNURI2;
                }
            } else {
                return  SDNURI;
            }
        }
        
    }
    
    public static boolean isSimPukRequest(int slotId) {
        Boolean v = (Boolean) getPresetObject(String.valueOf(slotId), SIM_KEY_WITHSLOT_PUK_REQUEST);
        if (v != null) {
            return v;
        }
        
        boolean isPukRequest = false;
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            isPukRequest = (TelephonyManager.SIM_STATE_PUK_REQUIRED == TelephonyManagerEx
                    .getDefault().getSimState(slotId));
        } else {
            isPukRequest = (TelephonyManager.SIM_STATE_PUK_REQUIRED == TelephonyManager
                    .getDefault().getSimState());
        }
        return isPukRequest;
    }

    public static boolean isSimPinRequest(int slotId) {
        Boolean v = (Boolean) getPresetObject(String.valueOf(slotId), SIM_KEY_WITHSLOT_PIN_REQUEST);
        if (v != null) {
            return v;
        }
        
        boolean isPinRequest = false;
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            isPinRequest = (TelephonyManager.SIM_STATE_PIN_REQUIRED == TelephonyManagerEx
                    .getDefault().getSimState(slotId));
        } else {
            isPinRequest = (TelephonyManager.SIM_STATE_PIN_REQUIRED == TelephonyManager
                    .getDefault().getSimState());
        }
        return isPinRequest;
    }

    public static boolean isSimStateReady(int slotId) {
        Boolean v = (Boolean) getPresetObject(String.valueOf(slotId), SIM_KEY_WITHSLOT_STATE_READY);
        if (v != null) {
            return v;
        }
        
        boolean isSimStateReady = false;
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            isSimStateReady = (TelephonyManager.SIM_STATE_READY == TelephonyManagerEx
                    .getDefault().getSimState(slotId));
        } else {
            isSimStateReady = (TelephonyManager.SIM_STATE_READY == TelephonyManager
                    .getDefault().getSimState());
        }
        return isSimStateReady;
    }
    
    public static boolean isSimInserted(int slotId) {
        Boolean v = (Boolean) getPresetObject(String.valueOf(slotId), SIM_KEY_WITHSLOT_SIM_INSERTED);
        if (v != null) {
            return v;
        }
        
        final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
                .getService(Context.TELEPHONY_SERVICE));
        boolean isSimInsert = false;
        try {
            if (iTel != null) {
                if (FeatureOption.MTK_GEMINI_SUPPORT) {
                    isSimInsert = iTel.isSimInsert(slotId);
                } else {
                    isSimInsert = iTel.isSimInsert(0);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            isSimInsert = false;
        }
        return isSimInsert;
    }
    
    public static boolean isFdnEnabed(int slotId) {
        Boolean v = (Boolean) getPresetObject(String.valueOf(slotId), SIM_KEY_WITHSLOT_FDN_ENABLED);
        if (v != null) {
            return v;
        }
        
        final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
                .getService(Context.TELEPHONY_SERVICE));
        boolean isFdnEnabled = false;
        try {
            if (iTel != null) {
                if (FeatureOption.MTK_GEMINI_SUPPORT) {
                    isFdnEnabled = iTel.isFDNEnabledGemini(slotId);
                } else {
                    isFdnEnabled = iTel.isFDNEnabled();
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            isFdnEnabled = false;
        }
        return isFdnEnabled;
    }
    
    public static boolean isSetRadioOn(ContentResolver resolver, int slotId) {
        Boolean v = (Boolean) getPresetObject(String.valueOf(slotId), SIM_KEY_WITHSLOT_RADIO_ON);
        if (v != null) {
            return v;
        }
        
        boolean isRadioOn = false;
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            int dualSimSet = Settings.System.getInt(resolver,
                    Settings.System.DUAL_SIM_MODE_SETTING, 3);
            isRadioOn = (Settings.Global.getInt(resolver,
                    Settings.Global.AIRPLANE_MODE_ON, 0) == 0)
                    && ((slotId + 1 == dualSimSet) || (3 == dualSimSet));
        } else {
            isRadioOn = Settings.Global.getInt(resolver,
                    Settings.Global.AIRPLANE_MODE_ON, 0) == 0;
        }
        return isRadioOn;
    }
    
    /**
     * check PhoneBook State is ready if ready, then return true.
     * 
     * @param slotId
     * @return
     */
    public static boolean isPhoneBookReady(int slotId) {
        Boolean v = (Boolean) getPresetObject(String.valueOf(slotId), SIM_KEY_WITHSLOT_PHB_READY);
        if (v != null) {
            return v;
        }
        
        final ITelephony iPhb = ITelephony.Stub.asInterface(ServiceManager
                .getService(Context.TELEPHONY_SERVICE));
        if (null == iPhb) {
            Log.d(TAG, "checkPhoneBookState, iPhb == null");
            return false;
        }
        boolean isPbReady = false;
        try {
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                isPbReady = iPhb.isPhbReadyGemini(slotId);
                Log.d(TAG, "isPbReady:" + isPbReady + "||slotId:" + slotId);

            } else {
                isPbReady = iPhb.isPhbReady();
                Log.d(TAG, "isPbReady:" + isPbReady + "||slotId:" + slotId);
            }
        } catch (Exception e) {
            Log.w(TAG, "e.getMessage is " + e.getMessage());
        }
        return isPbReady;
    }
    
    public static int getSimTypeBySlot(int slotId) {
        Integer v = (Integer) getPresetObject(String.valueOf(slotId), SIM_KEY_WITHSLOT_SIM_TYPE);
        if (v != null) {
            return v;
        }
        
        final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
                .getService(Context.TELEPHONY_SERVICE));
        int simType = -1;
        try {
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                if (SimType.SIM_TYPE_USIM_TAG.equals(iTel.getIccCardTypeGemini(slotId))) {
                    simType = SimType.SIM_TYPE_USIM;
                } else if (SimType.SIM_TYPE_UIM_TAG.equals(iTel.getIccCardTypeGemini(slotId))) {
                    simType = SimType.SIM_TYPE_UIM;
                } else if (SimType.SIM_TYPE_SIM_TAG.equals(iTel.getIccCardTypeGemini(slotId))) {
                    simType = SimType.SIM_TYPE_SIM;
                }
            } else {
                if (SimType.SIM_TYPE_USIM_TAG.equals(iTel.getIccCardType())) {
                    simType = SimType.SIM_TYPE_USIM;
                } else if (SimType.SIM_TYPE_UIM_TAG.equals(iTel.getIccCardType())) {
                    simType = SimType.SIM_TYPE_UIM;
                } else if (SimType.SIM_TYPE_SIM_TAG.equals(iTel.getIccCardType())) {
                    simType = SimType.SIM_TYPE_SIM;
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "catched exception.");
            e.printStackTrace();
        }
        return simType;
    }
    
    public static boolean isSimUsimType(int slotId) {
        Boolean v = (Boolean) getPresetObject(String.valueOf(slotId), SIM_KEY_WITHSLOT_IS_USIM);
        if (v != null) {
            return v;
        }
        
        final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
                .getService(Context.TELEPHONY_SERVICE));
        boolean isUsim = false;
        try {
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                if (SimType.SIM_TYPE_USIM_TAG.equals(iTel.getIccCardTypeGemini(slotId))) {
                    isUsim = true;
                }
            } else {
                if (SimType.SIM_TYPE_USIM_TAG.equals(iTel.getIccCardType())) {
                    isUsim = true;
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "catched exception.");
            e.printStackTrace();
        }
        return isUsim;
    }
    
    public static boolean isSimInfoReady() {
        Boolean v = (Boolean) getPresetObject(String.valueOf(NO_SLOT), SIM_KEY_SIMINFO_READY);
        if (v != null) {
            return v;
        }
        
        String simInfoReady = SystemProperties.get(TelephonyProperties.PROPERTY_SIM_INFO_READY);
        return "true".equals(simInfoReady);
    }
    
    
    /**
     * For test
     */
    private static HashMap<String, ContentValues> sPresetSimData = null;
    
    @VisibleForTesting
    public static void clearPreSetSimData() {
        sPresetSimData = null;
    }
    
    private static Object getPresetObject(String key1, String key2) {
        if (sPresetSimData != null) {
            ContentValues values = sPresetSimData.get(key1);
            if (values != null) {
                Object v = values.get(key2);
                if (v != null) {
                    return v;
                }
            }
        }
        return null;
    }
    
    private static final String NO_SLOT = String.valueOf(-1);
    private static final String SIM_KEY_WITHSLOT_PUK_REQUEST = "isSimPukRequest";
    private static final String SIM_KEY_WITHSLOT_PIN_REQUEST = "isSimPinRequest";
    private static final String SIM_KEY_WITHSLOT_STATE_READY = "isSimStateReady";
    private static final String SIM_KEY_WITHSLOT_SIM_INSERTED = "isSimInserted";
    private static final String SIM_KEY_WITHSLOT_FDN_ENABLED = "isFdnEnabed";
    private static final String SIM_KEY_WITHSLOT_RADIO_ON = "isSetRadioOn";
    private static final String SIM_KEY_WITHSLOT_PHB_READY = "isPhoneBookReady";
    private static final String SIM_KEY_WITHSLOT_SIM_TYPE = "getSimTypeBySlot";
    private static final String SIM_KEY_WITHSLOT_IS_USIM = "isSimUsimType";
    private static final String SIM_KEY_SIMINFO_READY = "isSimInfoReady";
    
    @VisibleForTesting
    public static void preSetSimData(int slot, Boolean fdnEnabled,
            Boolean isUsim, Boolean phbReady, Boolean pinRequest,
            Boolean pukRequest, Boolean isRadioOn, Boolean isSimInserted,
            Integer simType, Boolean simStateReady, Boolean simInfoReady) {
        ContentValues value1 = new ContentValues();
        if (fdnEnabled != null) {
            value1.put(SIM_KEY_WITHSLOT_FDN_ENABLED, fdnEnabled);
        }
        if (isUsim != null) {
            value1.put(SIM_KEY_WITHSLOT_IS_USIM, isUsim);
        }
        if (phbReady != null) {
            value1.put(SIM_KEY_WITHSLOT_PHB_READY, phbReady);
        }
        if (pinRequest != null) {
            value1.put(SIM_KEY_WITHSLOT_PIN_REQUEST, pinRequest);
        }
        if (pukRequest != null) {
            value1.put(SIM_KEY_WITHSLOT_PUK_REQUEST, pukRequest);
        }
        if (isRadioOn != null) {
            value1.put(SIM_KEY_WITHSLOT_RADIO_ON, isRadioOn);
        }
        if (isSimInserted != null) {
            value1.put(SIM_KEY_WITHSLOT_SIM_INSERTED, isSimInserted);
        }
        if (simType != null) {
            value1.put(SIM_KEY_WITHSLOT_SIM_TYPE, simType);
        }
        if (simStateReady != null) {
            value1.put(SIM_KEY_WITHSLOT_STATE_READY, simStateReady);
        }
        if (sPresetSimData == null) {
            sPresetSimData = new HashMap<String, ContentValues>(); 
        }
        if (value1 != null && value1.size() > 0) {
            String key1 = String.valueOf(slot);
            if (sPresetSimData.containsKey(key1)) {
                sPresetSimData.remove(key1);
            }
            sPresetSimData.put(key1, value1);
        }
        
        ContentValues value2 = new ContentValues();
        if (simInfoReady != null) {
            value2.put(SIM_KEY_SIMINFO_READY, simInfoReady);
        }
        if (value2 != null && value2.size() > 0) {
            if (sPresetSimData.containsKey(NO_SLOT)) {
                sPresetSimData.remove(NO_SLOT);
            } 
            sPresetSimData.put(NO_SLOT, value2);
        }
    }

    public static class ShowSimCardStorageInfoTask extends AsyncTask<Void, Void, Void> {
        private static ShowSimCardStorageInfoTask sInstance = null;
        private boolean mIsCancelled = false;
        private boolean mIsException = false;
        private String mDlgContent = null;
        private Context mContext = null;
        private static boolean sNeedPopUp = false;
        private static HashMap<Integer, Integer> sSurplugMap = new HashMap<Integer, Integer>();

        public static void showSimCardStorageInfo(Context context, boolean needPopUp) {
            sNeedPopUp = needPopUp;
            Log.i(TAG, "[ShowSimCardStorageInfoTask]_beg");
            if (sInstance != null) {
                sInstance.cancel();
                sInstance = null;
            }
            sInstance = new ShowSimCardStorageInfoTask(context);
            sInstance.execute();
            Log.i(TAG, "[ShowSimCardStorageInfoTask]_end");
        }

        public ShowSimCardStorageInfoTask(Context context) {
            mContext = context;
            Log.i(TAG, "[ShowSimCardStorageInfoTask] onCreate()");
        }

        @Override
        protected Void doInBackground(Void... args) {
            Log.i(TAG, "[ShowSimCardStorageInfoTask]: doInBackground_beg");
            sSurplugMap.clear();
            List<SIMInfo> simInfos = SIMInfoWrapper.getDefault().getInsertedSimInfoList();
            Log.i(TAG, "[ShowSimCardStorageInfoTask]: simInfos.size = " + simInfos.size());
            if (!mIsCancelled && simInfos.size() > 0) {
                StringBuilder build = new StringBuilder();
                int simId = 0;
                for (SIMInfo simInfo : simInfos) {
                    if (simId > 0) {
                        build.append("\n\n");
                    }
                    simId++;
                    int[] storageInfos = null;
                    Log.i(TAG, "[ShowSimCardStorageInfoTask] simName = " + simInfo.mDisplayName
                            + "; simSlot = " + simInfo.mSlot + "; simId = " + simInfo.mSimId);
                    build.append(simInfo.mDisplayName);
                    build.append(":\n");
                    try {
                        ITelephony phone = ITelephony.Stub.asInterface(ServiceManager
                                .checkService("phone"));
                        if (!mIsCancelled && phone != null) {
                            storageInfos = phone.getAdnStorageInfo(simInfo.mSlot);
                            if (storageInfos == null) {
                                mIsException = true;
                                Log.i(TAG, " storageInfos is null");
                                return null;
                            }
                            Log.i(TAG, "[ShowSimCardStorageInfoTask] infos: "
                                    + storageInfos.toString());
                        } else {
                            Log.i(TAG, "[ShowSimCardStorageInfoTask]: phone = null");
                            mIsException = true;
                            return null;
                        }
                    } catch (RemoteException ex) {
                        Log.i(TAG, "[ShowSimCardStorageInfoTask]_exception: " + ex);
                        mIsException = true;
                        return null;
                    }
                    Log.i(TAG, "slotId:" + simInfo.mSlot + "||storage:"
                            + (storageInfos == null ? "NULL" : storageInfos[1]) + "||used:"
                            + (storageInfos == null ? "NULL" : storageInfos[0]));
                    if (storageInfos != null && storageInfos[1] > 0) {
                        sSurplugMap.put(simInfo.mSlot, storageInfos[1] - storageInfos[0]);
                    }
                    build.append(mContext.getResources().getString(R.string.dlg_simstorage_content,
                            storageInfos[1], storageInfos[0]));
                    if (mIsCancelled) {
                        return null;
                    }
                }
                mDlgContent = build.toString();
            }
            Log.i(TAG, "[ShowSimCardStorageInfoTask]: doInBackground_end");
            return null;
        }

        public void cancel() {
            super.cancel(true);
            mIsCancelled = true;
            Log.i(TAG, "[ShowSimCardStorageInfoTask]: mIsCancelled = true");
        }

        @Override
        protected void onPostExecute(Void v) {
            sInstance = null;
            if (!mIsCancelled && !mIsException && sNeedPopUp) {
                new AlertDialog.Builder(mContext).setIcon(
                        R.drawable.ic_menu_look_simstorage_holo_light).setTitle(
                        R.string.look_simstorage).setMessage(mDlgContent).setPositiveButton(
                        android.R.string.ok, null).setCancelable(true).create().show();
            }
            mIsCancelled = false;
            mIsException = false;
        }

        public static int getSurplugCount(int slotId) {
            Log.i(TAG, "getSurplugCount sSurplugMap : " + sSurplugMap);
            if (null != sSurplugMap && sSurplugMap.containsKey(slotId)) {
                int result = sSurplugMap.get(slotId);
                Log.i(TAG, "getSurplugCount result : " + result);
                return result;
            } else {
                Log.i(TAG, "getSurplugCount return -1");
                return -1;
            }
        }
    }
}
