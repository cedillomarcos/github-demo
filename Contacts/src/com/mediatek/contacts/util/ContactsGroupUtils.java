package com.mediatek.contacts.util;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.RawContacts;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.contacts.R;
import com.android.internal.telephony.IIccPhoneBook;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.UsimGroup;
import com.android.internal.telephony.gsm.UsimPhoneBookManager;

import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;
import com.mediatek.contacts.simcontact.AbstractStartSIMService;
import com.mediatek.contacts.simcontact.AbstractStartSIMService.ServiceWorkData;
import com.mediatek.contacts.simcontact.SimCardUtils;
import com.mediatek.telephony.TelephonyManagerEx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ContactsGroupUtils {

    public static final String TAG = "ContactsGroupUtils";
    private static final boolean DBG = true;

    public static final String SIMPHONEBOOK_SERVICE = "simphonebook";
    public static final String SIMPHONEBOOK2_SERVICE = "simphonebook2";
    public static final int SINGLE_SLOT = 0;
    public static final int GEMINI_SLOT1 = com.android.internal.telephony.PhoneConstants.GEMINI_SIM_1;
    public static final int GEMINI_SLOT2 = com.android.internal.telephony.PhoneConstants.GEMINI_SIM_2;
    public static final int SLOT_COUNT = FeatureOption.MTK_GEMINI_SUPPORT ? 2 : 1;
    public static int sArrayData;
    public static final boolean DEBUG = true;
    public static final String CONTACTS_IN_GROUP_SELECT =
    " IN "
            + "(SELECT " + RawContacts.CONTACT_ID
            + " FROM " + "raw_contacts"
            + " WHERE " + "raw_contacts._id" + " IN "
                    + "(SELECT " + "data." + Data.RAW_CONTACT_ID
                    + " FROM " + "data "
                    + "JOIN mimetypes ON (data.mimetype_id = mimetypes._id)"
                    + " WHERE " + Data.MIMETYPE + "='" + GroupMembership.CONTENT_ITEM_TYPE
                            + "' AND " + GroupMembership.GROUP_ROW_ID + "="
                            + "(SELECT " + "groups" + "." + Groups._ID
                            + " FROM " + "groups"
                            + " WHERE " + Groups.DELETED + "=0 AND " + Groups.TITLE + "=?))" 
             + " AND " + RawContacts.DELETED + "=0)";

    public static IIccPhoneBook getIIccPhoneBook(int slotId) {
        logd(TAG, "[getIIccPhoneBook]slotId:" + slotId);
        String serviceName;
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            serviceName = (slotId == GEMINI_SLOT2) ? SIMPHONEBOOK2_SERVICE : SIMPHONEBOOK_SERVICE;
        } else {
            serviceName = SIMPHONEBOOK_SERVICE;
        }
        final IIccPhoneBook iIccPhb = IIccPhoneBook.Stub
                .asInterface(ServiceManager.getService(serviceName));
        return iIccPhb;
    }

    public static final class USIMGroup {
        public static final String TAG = ContactsGroupUtils.TAG;

        public static final String SIM_TYPE_USIM = "USIM";

        // A inner class USED for Usim group cache.
        private static final class USimGroupArray {
            private ArrayList<ArrayList<UsimGroup>> mUgrpArray;
            private int mSize = 0;

            USimGroupArray(int size) {
                mUgrpArray = new ArrayList<ArrayList<UsimGroup>>();
                for (int i = 0; i < size; i++) {
                    ArrayList<UsimGroup> ugrpList = new ArrayList<UsimGroup>();
                    mUgrpArray.add(ugrpList);
                }
                mSize = size;
            }

            ArrayList<UsimGroup> get(int slot) {
                if (slot < 0 || slot >= mSize) {
                    return null;
                }
                return mUgrpArray.get(slot);
            }

            boolean addItem(int slot, UsimGroup usimGroup) {
                if (slot < 0 || slot >= mSize) {
                    return false;
                }
                return mUgrpArray.get(slot).add(usimGroup);
            }

            boolean removeItem(int slot, int usimGroupId) {
                if (slot < 0 || slot >= mSize) {
                    return false;
                }
                ArrayList<UsimGroup> ugrpList = mUgrpArray.get(slot);
                int i = 0;
                for (UsimGroup ug : ugrpList) {
                    Log.i(TAG, "ug---index:" + ug.getRecordIndex() + " || name:"
                                    + ug.getAlphaTag());
                    if (ug.getRecordIndex() == usimGroupId) {
                        break;
                    }
                    Log.i(TAG, "ug---i count:" + i);
                    i++;
                }
                Log.i(TAG, "ug---size:" + ugrpList.size());
                if (i < ugrpList.size()) {
                    ugrpList.remove(i);
                    Log.i(TAG, "ug---size after remove:" + ugrpList.size());
                    return true;
                }
                return false;
            }

            UsimGroup getItem(int slot, int usimGroupId) {
                if (slot < 0 || slot >= mSize) {
                    return null;
                }
                ArrayList<UsimGroup> ugrpList = mUgrpArray.get(slot);
                int i = 0;
                for (UsimGroup ug : ugrpList) {
                    if (ug.getRecordIndex() == usimGroupId) {
                        return ug;
                    }
                }
                return null;
            }

        }

        // Framework interface, here should be change in future.
        // These values may be changed when booting phone to get the really usim
        // capability.
        private static int[] sMaxUsimGroupNameLength = {
                -1, -1
        };
        private static int[] sMaxUsimGroupCount = {
                -1, -1
        };
        private static final USimGroupArray UGRP_LISTARRAY = new USimGroupArray(SLOT_COUNT);

        // Framework interface, here should be change in future.
        public static int hasExistGroup(int slotId, String grpName) throws RemoteException {
            int grpId = -1;
            final IIccPhoneBook iIccPhb = getIIccPhoneBook(slotId);
            logd(TAG, "grpName:" + grpName + "|iIccPhb:" + iIccPhb);
            if (TextUtils.isEmpty(grpName) || iIccPhb == null) {
                return grpId;
            }
            ArrayList<UsimGroup> ugrpList = UGRP_LISTARRAY.get(slotId);
            logd(TAG, "[hasExistGroup]ugrpList---size:" + ugrpList.size());
            if (ugrpList.isEmpty()) {
                List<UsimGroup> uList = iIccPhb.getUsimGroups();
                for (UsimGroup ug : uList) {
                    String gName = ug.getAlphaTag();
                    int gIndex = ug.getRecordIndex();
                    if (!TextUtils.isEmpty(gName) && gIndex > 0) {
                        ugrpList.add(new UsimGroup(gIndex, gName));
                        logd(TAG, "[hasExistGroup]gName:" + gName + "||gIndex:" + gIndex);
                        if (gName.equals(grpName)) {
                            grpId = gIndex;
                        }
                    }
                }
            } else {
                for (UsimGroup ug : ugrpList) {
                    logd(TAG, "[hasExistGroup]ug---index:" + ug.getRecordIndex() + " || name:"
                            + ug.getAlphaTag());
                    if (grpName.equals(ug.getAlphaTag())) {
                        grpId = ug.getRecordIndex();
                        break;
                    }
                }
            }
            logd(TAG, "ugrpList size:" + ugrpList.size());
            return grpId;
        }

        public static int syncUSIMGroupNewIfMissing(int slotId, String name)
                throws RemoteException, USIMGroupException {
            int nameLen = 0;
            logd(TAG, "[syncUSIMGroupNewIfMissing]name:" + name);
            if (TextUtils.isEmpty(name)) {
                return -1;
            }
            try {
                nameLen = name.getBytes("GBK").length;
            } catch (java.io.UnsupportedEncodingException e) {
                nameLen = name.length();
            }
            logd(TAG, "[syncUSIMGroupNewIfMissing]nameLen:" + nameLen
                    + " ||getUSIMGrpMaxNameLen(slotId):" + getUSIMGrpMaxNameLen(slotId));
            if (nameLen > getUSIMGrpMaxNameLen(slotId)) {
                throw new USIMGroupException(
                        USIMGroupException.ERROR_STR_GRP_NAME_OUTOFBOUND,
                        USIMGroupException.GROUP_NAME_OUT_OF_BOUND, slotId);
            }
            final IIccPhoneBook iIccPhb = getIIccPhoneBook(slotId);
            int grpId = -1;
            grpId = hasExistGroup(slotId, name);
            if (grpId < 1 && iIccPhb != null) {
                grpId = iIccPhb.insertUSIMGroup(name);
                Log.i(TAG, "[syncUSIMGroupNewIfMissing]inserted grpId:" + grpId);
                if (grpId > 0) {
                    UGRP_LISTARRAY.addItem(slotId, new UsimGroup(grpId, name));
                }
            }
            logd(TAG, "[syncUSIMGroupNewIfMissing]grpId:" + grpId);
            if (grpId < 1) {
                switch (grpId) {
                    case USIMGroupException.USIM_ERROR_GROUP_COUNT:
                        throw new USIMGroupException(
                                USIMGroupException.ERROR_STR_GRP_COUNT_OUTOFBOUND,
                                USIMGroupException.GROUP_NUMBER_OUT_OF_BOUND, slotId);

                        // Name len has been check before new group.
                        // However, do protect here just for full logic.
                    case USIMGroupException.USIM_ERROR_NAME_LEN:
                        throw new USIMGroupException(
                                USIMGroupException.ERROR_STR_GRP_NAME_OUTOFBOUND,
                                USIMGroupException.GROUP_NAME_OUT_OF_BOUND, slotId);
                    default:
                        throw new USIMGroupException(
                                USIMGroupException.ERROR_STR_GRP_GENERIC_ERROR,
                                USIMGroupException.GROUP_GENERIC_ERROR, slotId);
                }
            }
            return grpId;
        }

        /**
         * If a group has to change name, the mapping group of USIM card should
         * also be changed
         * 
         * @return
         */
        public static int syncUSIMGroupUpdate(int slotId, String oldName, String newName)
                throws RemoteException, USIMGroupException {
            final IIccPhoneBook iIccPhb = getIIccPhoneBook(slotId);
            int grpId = hasExistGroup(slotId, oldName);
            logd(TAG, "grpId:" + grpId + "|slotId:" + slotId + "|oldName:" + oldName + "|newName:"
                    + newName);
            if (grpId > 0) {
                int nameLen = 0;
                try {
                    if (!TextUtils.isEmpty(newName)) {
                        nameLen = newName.getBytes("GBK").length;
                    } else {
                        return grpId;
                    }
                } catch (java.io.UnsupportedEncodingException e) {
                    nameLen = newName.length();
                }
                if (getUSIMGrpMaxNameLen(slotId) < nameLen) {
                    throw new USIMGroupException(
                            USIMGroupException.ERROR_STR_GRP_NAME_OUTOFBOUND,
                            USIMGroupException.GROUP_NAME_OUT_OF_BOUND, slotId);
                }
                int ret = iIccPhb.updateUSIMGroup(grpId, newName);
                if (ret == USIMGroupException.USIM_ERROR_NAME_LEN) {
                    throw new USIMGroupException(
                            USIMGroupException.ERROR_STR_GRP_COUNT_OUTOFBOUND,
                            USIMGroupException.GROUP_NUMBER_OUT_OF_BOUND, slotId);
                }

                UsimGroup usimGrp = UGRP_LISTARRAY.getItem(slotId, grpId);
                logd(TAG, "[syncUSIMGroupUpdate]: usimGrp is null = " + (usimGrp == null));
                if (usimGrp != null) {
                    usimGrp.setAlphaTag(newName);
                }

            }
            return grpId;
        }


        public static int deleteUSIMGroup(int slotId, String name) {
            final IIccPhoneBook iIccPhb = getIIccPhoneBook(slotId);
            int errCode = -2;
            try {
                int grpId = hasExistGroup(slotId, name);
                if (grpId > 0) {
                    if (iIccPhb.removeUSIMGroupById(grpId)) {
                        UGRP_LISTARRAY.removeItem(slotId, grpId);
                        errCode = 0;
                    } else {
                        errCode = -1;
                    }
                }
            } catch (android.os.RemoteException e) {
                logd(TAG, "catched exception");
            }
            return errCode;
        }

        public static boolean addUSIMGroupMember(int slotId, int simIndex, int grpId) {
            boolean succFlag = false;
            try {
                if (grpId > 0) {
                    final IIccPhoneBook iIccPhb = getIIccPhoneBook(slotId);
                    if (iIccPhb != null) {
                        succFlag = iIccPhb.addContactToGroup(simIndex, grpId);
                        succFlag = true;// Only for test, should be removed
                                        // after framework is ready.
                    }
                }
            } catch (android.os.RemoteException e) {
                logd(TAG, "catched exception");
                succFlag = false;
            }
            logd(TAG, "[addUSIMGroupMember]succFlag" + succFlag);
            return succFlag;
        }

        public static boolean deleteUSIMGroupMember(int slotId, int simIndex, int grpId) {
            logd(TAG, "[deleteUSIMGroupMember]slotId:" + slotId
                    + "|simIndex:" + simIndex + "|grpId:" + grpId);
            boolean succFlag = false;
            try {
                if (grpId > 0) {
                    final IIccPhoneBook iIccPhb = getIIccPhoneBook(slotId);
                    if (iIccPhb != null) {
                        succFlag = iIccPhb.removeContactFromGroup(simIndex, grpId);
                    }
                }
            } catch (android.os.RemoteException e) {
                logd(TAG, "catched exception.");
                succFlag = false;
            }
            logd(TAG, "[deleteUSIMGroupMember]result:" + succFlag);
            return succFlag;
        }

        /**
         * Sync USIM group
         * 
         * @param context
         * @param slotId
         * @param grpIdMap The pass in varible must not be null.
         */
        public static synchronized void syncUSIMGroupContactsGroup(Context context,
                final ServiceWorkData workData, HashMap<Integer, Integer> grpIdMap) {
            logd(TAG, "syncUSIMGroupContactsGroup begin");

            if (workData.mSimType != SimCardUtils.SimType.SIM_TYPE_USIM) {
                return;
            }
            final int slotId = workData.mSlotId;
            final int simId = workData.mSimId;
            final int workType = workData.mWorkType;

            ArrayList<UsimGroup> ugrpList = UGRP_LISTARRAY.get(slotId);
            if (workType == AbstractStartSIMService.SERVICE_WORK_REMOVE) {
                deleteUSIMGroupOnPhone(context, slotId);
                ugrpList.clear();
                logd(TAG, "syncUSIMGroupContactsGroup end. deleteUSIMGroupOnPhone.");
                return;
            }
            // Get All groups in USIM
            ugrpList.clear();
            final IIccPhoneBook iIccPhb = getIIccPhoneBook(slotId);
            if (iIccPhb == null) {
                return;
            }
            try {
                List<UsimGroup> uList = iIccPhb.getUsimGroups();
                if (uList == null) {
                    return;
                }
                for (UsimGroup ug : uList) {
                    String gName = ug.getAlphaTag();
                    int gIndex = ug.getRecordIndex();
                    Log.i(TAG, "[syncUSIMGroupContactsGroup]gName:" + gName + "|gIndex: " + gIndex);

                    if (!TextUtils.isEmpty(gName) && gIndex > 0) {
                        ugrpList.add(new UsimGroup(gIndex, gName));
                    }
                }
            } catch (android.os.RemoteException e) {
                logd(TAG, "catched exception");
                e.printStackTrace();
            }

            try {
                Log.i(TAG, "getUSIMGrpMaxNameLen begin");
                sMaxUsimGroupNameLength[slotId] = iIccPhb.getUSIMGrpMaxNameLen();
                Log.i(TAG, "getUSIMGrpMaxNameLen end. slot:" + slotId
                        + "|NAME_LENGTH:"
                        + sMaxUsimGroupNameLength[slotId]);
                Log.i(TAG, "getUSIMGrpMaxCount begin.");
                sMaxUsimGroupCount[slotId] = iIccPhb.getUSIMGrpMaxCount();
                Log.i(TAG, "getUSIMGrpMaxCount end. slot:" + slotId
                        + "|GROUP_COUNT:"
                        + sMaxUsimGroupCount[slotId]);
            } catch (android.os.RemoteException e) {
                sMaxUsimGroupNameLength[slotId] = -1;
                sMaxUsimGroupCount[slotId] = -1;
            }

            // Query SIM info to get simId
            // Query to get all groups in Phone
            ContentResolver cr = context.getContentResolver();
            Cursor c = cr.query(Groups.CONTENT_SUMMARY_URI, null,
                    Groups.DELETED + "=0 AND " +
                            Groups.ACCOUNT_TYPE + "='USIM Account' AND "
                            + Groups.ACCOUNT_NAME + "=" + "'USIM" + slotId + "'", null, null);
            // Query all Group including deleted group

            HashMap<String, Integer> noneMatchedMap = new HashMap<String, Integer>();
            if (c != null) {
                c.moveToPosition(-1);
                while (c.moveToNext()) {
                    String grpName = c.getString(c.getColumnIndexOrThrow(Groups.TITLE));
                    int grpId = c.getInt(c.getColumnIndexOrThrow(Groups._ID));
                    if (!noneMatchedMap.containsKey(grpName)) {
                        noneMatchedMap.put(grpName, grpId);
                    }
                }
                c.close();
            }

            if (ugrpList != null) {
                boolean hasMerged = false;
                for (UsimGroup ugrp : ugrpList) {
                    String ugName = ugrp.getAlphaTag();
                    hasMerged = false;
                    long groupId = -1;
                    if (!TextUtils.isEmpty(ugName)) {
                        int ugId = ugrp.getRecordIndex();
                        if (noneMatchedMap.containsKey(ugName)) {
                            groupId = noneMatchedMap.get(ugName);
                            noneMatchedMap.remove(ugName);
                            hasMerged = true;
                        }

                        if (!hasMerged) {
                            // Need to create on phone
                            ContentValues values = new ContentValues();
                            values.put(Groups.TITLE, ugName);
                            values.put(Groups.GROUP_VISIBLE, 1);
                            values.put(Groups.SYSTEM_ID, 0);
                            values.put(Groups.ACCOUNT_NAME, "USIM" + slotId);
                            values.put(Groups.ACCOUNT_TYPE, "USIM Account");
                            Uri uri = cr.insert(Groups.CONTENT_URI, values);
                            groupId = (uri == null) ? 0 : ContentUris.parseId(uri);
                        }
                        if (groupId > 0) {
                            grpIdMap.put(ugId, (int) groupId);
                        }
                    }
                }

                if (noneMatchedMap.size() > 0) {
                    Integer[] groupIdArray = noneMatchedMap.values().toArray(new Integer[0]);
                    StringBuilder delGroupIdStr = new StringBuilder();
                    for (Integer i : groupIdArray) {
                        int delGroupId = i;
                        delGroupIdStr.append(delGroupId).append(",");
                    }
                    if (delGroupIdStr.length() > 0) {
                        delGroupIdStr.deleteCharAt(delGroupIdStr.length() - 1);
                    }
                    if (delGroupIdStr.length() > 0) {
                        cr.delete(Groups.CONTENT_URI, Groups._ID + " IN ("
                                + delGroupIdStr.toString() + ")", null);
                    }
                }
                logd(TAG, "syncUSIMGroupContactsGroup end");
            } else {
                deleteUSIMGroupOnPhone(context, slotId);
            }
        }

        public static int getUSIMGrpMaxNameLen(final int slot) {
            if (slot < 0 || slot > 1) {
                return -1;
            }
            boolean phoneBookReady = SimCardUtils.isPhoneBookReady(slot); 
            if (!phoneBookReady) {
                sMaxUsimGroupNameLength[slot] = -1;
            }
            logd(TAG, "[getUSIMGrpMaxNameLen]phoneBookReady:" + phoneBookReady + " |slot:" + slot
                    + " |maxNameLen:" + sMaxUsimGroupNameLength[slot]);
            if (sMaxUsimGroupNameLength[slot] < 0 && phoneBookReady) {
                new Thread() {
                    public void run() {
                        try {
                            final IIccPhoneBook iIccPhb = getIIccPhoneBook(slot);
                            if (iIccPhb != null) {
                                sMaxUsimGroupNameLength[slot] = iIccPhb.getUSIMGrpMaxNameLen();
                            }
                        } catch (android.os.RemoteException e) {
                            logd(TAG, "catched exception.");
                            sMaxUsimGroupNameLength[slot] = -1;
                        }
                    }
                }.start();
            }
            logd(TAG, "[getUSIMGrpMaxNameLen]end slot:" + slot
                    + "|maxNameLen:" + sMaxUsimGroupNameLength[slot]);
            return sMaxUsimGroupNameLength[slot];
        }

        /**
         * Check the SIM state of a slot whether is ready.
         * 
         * @param context
         * @param slotId
         * @return If SIM is locked or not ready, it will return false.
         */
        public static boolean checkSimStateBySlot(Context context, final int slotId) {
            try {
                final ITelephony iTel = ITelephony.Stub
                        .asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
                if (iTel == null) {
                    return false;
                }

                ContentResolver resolver = context.getContentResolver();
                if (FeatureOption.MTK_GEMINI_SUPPORT) {
                    TelephonyManagerEx telephonyMgrEx = TelephonyManagerEx.getDefault();
                    boolean simPUKReq = TelephonyManager.SIM_STATE_PUK_REQUIRED == telephonyMgrEx
                            .getSimState(slotId);
                    boolean simPINReq = TelephonyManager.SIM_STATE_PIN_REQUIRED == telephonyMgrEx
                            .getSimState(slotId);
                    // boolean simReady = ContactsUtils.simStateReady(slotId);
                    int dualSimSet = Settings.System.getInt(resolver,
                            Settings.System.DUAL_SIM_MODE_SETTING, 3);
                    boolean isRadioOn = (Settings.Global.getInt(resolver,
                            Settings.Global.AIRPLANE_MODE_ON, 0) == 0)
                            && ((slotId + 1 == dualSimSet) || (3 == dualSimSet));
                    Log.d(TAG, "[checkSimState|GE]slotId: " + slotId + "simPUKReq: "
                            + simPUKReq + "||simPINReq: " + simPINReq + "||isRadioOn" + isRadioOn
                            + "||iTel.isFDNEnabled()" + iTel.isFDNEnabled());
                    if (simPUKReq || simPINReq || !isRadioOn || iTel.isFDNEnabledGemini(slotId)) {
                        return false;
                    }
                } else {
                    TelephonyManager telephonyMgr = TelephonyManager.getDefault();
                    boolean simPUKReq = TelephonyManager.SIM_STATE_PUK_REQUIRED == telephonyMgr
                            .getSimState();
                    boolean simPINReq = TelephonyManager.SIM_STATE_PIN_REQUIRED == telephonyMgr
                            .getSimState();
                    // boolean simReady = ContactsUtils.simStateReady();
                    boolean isRadioOn = Settings.Global.getInt(resolver,
                            Settings.Global.AIRPLANE_MODE_ON, 0) == 0;
                    Log.d(TAG, "[checkSimState|SL]simPUKReq: " + simPUKReq
                            + "||simPINReq: " + simPINReq + "||isRadioOn" + isRadioOn
                            + "||iTel.isFDNEnabled()" + iTel.isFDNEnabled());
                    if (simPUKReq || simPINReq || !isRadioOn || iTel.isFDNEnabled()) {
                        return false;
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "catched Exception");
                e.printStackTrace();
                return false;
            }
            return true;
        }

        public static void deleteUSIMGroupOnPhone(Context context, int slotId) {
            ContentResolver cr = context.getContentResolver();
            cr.delete(Groups.CONTENT_URI, Groups.ACCOUNT_TYPE + "='USIM Account' AND "
                    + Groups.ACCOUNT_NAME + "=" + "'USIM" + slotId + "'", null);
        }

    }


    public static class USIMGroupException extends Exception {

        private static final long serialVersionUID = 1L;

        public static final String ERROR_STR_GRP_NAME_OUTOFBOUND = "Group name out of bound";
        public static final String ERROR_STR_GRP_COUNT_OUTOFBOUND = "Group count out of bound";
        public static final String ERROR_STR_GRP_GENERIC_ERROR = "Group generic error";
        public static final int GROUP_NAME_OUT_OF_BOUND = 1;
        public static final int GROUP_NUMBER_OUT_OF_BOUND = 2;
        public static final int GROUP_GENERIC_ERROR = 3;
        // Exception type definination in framework.
        public static final int USIM_ERROR_NAME_LEN = UsimPhoneBookManager.USIM_ERROR_NAME_LEN;
        public static final int USIM_ERROR_GROUP_COUNT = UsimPhoneBookManager.USIM_ERROR_GROUP_COUNT;

        int mErrorType;
        int mSlotId;

        public USIMGroupException(String msg, int errorType, int slotId) {
            super(msg);
            mErrorType = errorType;
            mSlotId = slotId;
        }

        public int getErrorType() {
            return mErrorType;
        }

        public int getErrorSlotId() {
            return mSlotId;
        }

        @Override
        public String getMessage() {
            return "Details message: errorType:" + mErrorType + "\n"
                    + super.getMessage();
        }

        public static int getErrorToastId(int errorType) {
            int retMsgId;
            switch(errorType) {
                case GROUP_NAME_OUT_OF_BOUND:
                    retMsgId = R.string.usim_group_name_exceed_limit;
                    break;
                case GROUP_NUMBER_OUT_OF_BOUND:
                    retMsgId = R.string.usim_group_count_exceed_limit;
                    break;
                default:
                    retMsgId = R.string.generic_failure;
            }
            return retMsgId;
        }
    }

    static void logd(String mTAG, String msg) {
        if (DBG) {
            Log.d(mTAG, msg);
        }
    }

    public static class ContactsGroupArrayData {
        private int mSimIndex;
        private int mSimIndexPhoneOrSim;

        public int getmSimIndex() {
            return mSimIndex;
        }

        public int getmSimIndexPhoneOrSim() {
            return mSimIndexPhoneOrSim;
        }

        public ContactsGroupArrayData initData(int simIndex, int mSimIndexPhoneorSim) {
            mSimIndex = simIndex;
            mSimIndexPhoneOrSim = mSimIndexPhoneorSim;
            return this;
        }
    }

    public static final String SELECTION_MOVE_GROUP_DATA = RawContacts.CONTACT_ID
                                + " IN (%1) AND "
                                + Data.MIMETYPE
                                + "='"
                                + GroupMembership.CONTENT_ITEM_TYPE
                                + "' AND "
                                + GroupMembership.GROUP_ROW_ID + "='%2'";

    private static final int MAX_OP_COUNT_IN_ONE_BATCH = 150;

    /**
     * Move contacts from one USIM group to another
     * 
     * @param data Contacts data.
     * @param ugrpIdArray Must be created before calling, and the array length
     *            must 4. the first one indicates old USIM group id, and the
     *            second one indicates the target USIM group id.
     * @param isInTargetGroup This variable indicates whether a contacts is
     *            already in target group.
     */
    public static boolean moveUSIMGroupMember(ContactsGroupArrayData data, int slotId,
            boolean isInTargetGroup, int fromUgrpId, int toUgrpId) {
        boolean ret = false;
        int simId = data.mSimIndexPhoneOrSim;
        if (DEBUG) {
            Log.i(TAG, simId + "--------simId[moveUSIMGroupMember]");
            Log.i(TAG, slotId + "--------slotId[moveUSIMGroupMember]");
        }
        if (simId >= 0) {
            if (slotId >= 0) {

                // Add group data into new USIM group if it is not in new USIM
                // group
                boolean addSucess = false;

                if (!isInTargetGroup) {

                    if (DEBUG) {
                        Log.i(TAG, slotId + "--------slotId");
                        Log.i(TAG, data.mSimIndex + "--------data.mSimIndex");
                        Log.i(TAG, toUgrpId + "--------toUgrpId");
                    }
                    addSucess = ContactsGroupUtils.USIMGroup.addUSIMGroupMember(slotId,
                            data.mSimIndex,
                                    toUgrpId);
                    if (DEBUG) {
                        Log.i(TAG, addSucess + "--------addSucess");
                    }

                }
                // Delete group data in old USIM group.
                if (isInTargetGroup
                        || (fromUgrpId > 0 && toUgrpId > 0 && addSucess)) {
                    ret = true;
                    ContactsGroupUtils.USIMGroup.deleteUSIMGroupMember(slotId, data.mSimIndex,
                            fromUgrpId);
                    if (DEBUG) {
                        Log.i(TAG, fromUgrpId + "--------fromUgrpId");
                    }
                }
            }
        }
        return ret;
    }
}
