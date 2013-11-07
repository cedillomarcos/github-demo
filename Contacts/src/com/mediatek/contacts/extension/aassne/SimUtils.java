package com.mediatek.contacts.extension.aassne;

import com.android.contacts.editor.LabeledEditorView;
import com.android.contacts.editor.TextFieldsEditorView;
import com.android.contacts.ext.Anr;
import com.android.contacts.ext.ContactAccountExtension;
import com.android.contacts.model.account.AccountType;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.dataitem.DataKind;
import com.android.contacts.model.RawContactDelta;
import com.android.contacts.model.RawContactDeltaList;
import com.android.contacts.model.RawContactModifier;
import com.android.contacts.model.account.AccountType.EditType;
import com.android.contacts.model.RawContactDelta.ValuesDelta;
import com.android.internal.telephony.AlphaTag;
import com.android.internal.telephony.IIccPhoneBook;
import com.android.internal.telephony.ITelephony;
import com.android.internal.telephony.EncodeException;
import com.android.internal.telephony.GsmAlphabet;
import com.mediatek.contacts.ExtensionManager;
import com.mediatek.contacts.SubContactsUtils.NamePhoneTypePair;
import com.mediatek.contacts.model.UsimAccountType;
import com.mediatek.phone.SIMInfoWrapper;

import android.accounts.Account;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SimUtils {
    private static final String TAG = "AasExt";

    private static final boolean MTK_GEMINI_SUPPORT = com.mediatek.common.featureoption.FeatureOption.MTK_GEMINI_SUPPORT;
    private static final int SLOT_ID1 = com.android.internal.telephony.PhoneConstants.GEMINI_SIM_1;
    private static final int SLOT_ID2 = com.android.internal.telephony.PhoneConstants.GEMINI_SIM_2;
    private static final String SIMPHONEBOOK_SERVICE = "simphonebook";
    private static final String SIMPHONEBOOK2_SERVICE = "simphonebook2";

    private static final int SIM_TYPE_SIM = 0;
    private static final int SIM_TYPE_USIM = 1;
    private static final int SIM_TYPE_UIM = 2;
    private static final String SIM_TYPE_SIM_TAG = "SIM";
    private static final String SIM_TYPE_USIM_TAG = "USIM";
    private static final String SIM_TYPE_UIM_TAG = "UIM";

    public static final String ACCOUNT_TYPE_SIM = "SIM Account";
    public static final String ACCOUNT_TYPE_USIM = "USIM Account";
    public static final String ACCOUNT_TYPE_UIM = "UIM Account";

    public static final String IS_ADDITIONAL_NUMBER = "1";

    private static int[] MAX_USIM_AAS_NAME_LENGTH = { -1, -1 };
    private static int[] MAX_USIM_AAS_COUNT = { -1, -1 };
    private static int[] MAX_USIM_ANR_COUNT = { -1, -1 };
    private static HashMap<Integer, List<AlphaTag>> sAasMap = new HashMap<Integer, List<AlphaTag>>(2);

    private static final int ERROR = -1;

    public static String getAccountTypeBySlot(int slotId) {
        Log.d(TAG, "[getAccountTypeBySlot] slotId:" + slotId);
        if (slotId < SLOT_ID1 || slotId > SLOT_ID2) {
            Log.e(TAG, "[getAccountTypeBySlot]Error slotid:" + slotId);
            return null;
        }
        int simtype = SIM_TYPE_SIM;
        String simAccountType = null;

        if (isSimInserted(slotId)) {
            simtype = getSimTypeBySlot(slotId);
            if (SIM_TYPE_USIM == simtype) {
                simAccountType = ACCOUNT_TYPE_USIM;
            } else if (SIM_TYPE_SIM == simtype) {
                simAccountType = ACCOUNT_TYPE_SIM;
            } else if (SIM_TYPE_UIM == simtype) {
                simAccountType = ACCOUNT_TYPE_UIM;
            }
        } else {
            Log.e(TAG, "[getAccountTypeBySlot]Error slotId:" + slotId + " no sim inserted!");
            simAccountType = null;
        }
        Log.d(TAG, "[getAccountTypeBySlot] accountType:" + simAccountType);
        return simAccountType;
    }

    private static boolean isSimInserted(int slotId) {
        final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
        boolean isSimInsert = false;
        try {
            if (iTel != null) {
                if (MTK_GEMINI_SUPPORT) {
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

    private static int getSimTypeBySlot(int slotId) {
        final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
        int simType = -1;
        try {
            if (MTK_GEMINI_SUPPORT) {
                if (SIM_TYPE_USIM_TAG.equals(iTel.getIccCardTypeGemini(slotId))) {
                    simType = SIM_TYPE_USIM;
                } else if (SIM_TYPE_UIM_TAG.equals(iTel.getIccCardTypeGemini(slotId))) {
                    simType = SIM_TYPE_UIM;
                } else if (SIM_TYPE_SIM_TAG.equals(iTel.getIccCardTypeGemini(slotId))) {
                    simType = SIM_TYPE_SIM;
                }
            } else {
                if (SIM_TYPE_USIM_TAG.equals(iTel.getIccCardType())) {
                    simType = SIM_TYPE_USIM;
                } else if (SIM_TYPE_UIM_TAG.equals(iTel.getIccCardType())) {
                    simType = SIM_TYPE_UIM;
                } else if (SIM_TYPE_SIM_TAG.equals(iTel.getIccCardType())) {
                    simType = SIM_TYPE_SIM;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "[getSimTypeBySlot] catched exception.");
            e.printStackTrace();
        }
        return simType;
    }

    /**
     * refresh local aas list. after you change the USim card aas info, please refresh local info.
     * @param slot
     * @return
     */
    public static boolean refreshAASList(int slot) {
        if (slot < SLOT_ID1 || slot > SLOT_ID2) {
            Log.d(TAG, "refreshAASList() slot=" + slot);
            return false;
        }

        try {
            final IIccPhoneBook iIccPhb = getIIccPhoneBook(slot);
            if (iIccPhb != null) {
                List<AlphaTag> atList = iIccPhb.getUSIMAASList();
                Iterator<AlphaTag> iter = atList.iterator();
                while (iter.hasNext()) {
                    AlphaTag entry = iter.next();
                    String tag = entry.getAlphaTag();
                    if (TextUtils.isEmpty(tag)) {
                        iter.remove();
                    }
                    Log.d(TAG, "refreshAASList. tag=" + tag);
                }
                sAasMap.put(slot, atList);
            }
        } catch (Exception e) {
            Log.d(TAG, "catched exception.");
            sAasMap.put(slot, null);
        }

        return true;
    }

    /**
     * get USim card aas info without null tag. It will return all aas info that can be used in
     * application.
     * @param slot
     * @return
     */
    public static List<AlphaTag> getAAS(int slot) {
        List<AlphaTag> atList = new ArrayList<AlphaTag>();
        if (slot < SLOT_ID1 || slot > SLOT_ID2) {
            Log.e(TAG, "getAAS() slot=" + slot);
            return atList;
        }
        // Here, force to refresh the list.
        refreshAASList(slot);

        List<AlphaTag> list = sAasMap.get(slot);

        return list != null ? list : atList;
    }

    /**
     * Get the max length of AAS.
     * @param slot The USIM Card slotId
     * @return
     */
    public static int getAASTextMaxLength(int slot) {
        if (slot < SLOT_ID1 || slot > SLOT_ID2) {
            Log.e(TAG, "getAASMaxLength() slot=" + slot);
            return ERROR;
        }
        Log.d(TAG, "getAASMaxLength() slot:" + slot + "|maxNameLen:" + MAX_USIM_AAS_NAME_LENGTH[slot]);
        if (MAX_USIM_AAS_NAME_LENGTH[slot] < 0) {
            try {
                final IIccPhoneBook iIccPhb = getIIccPhoneBook(slot);
                if (iIccPhb != null) {
                    MAX_USIM_AAS_NAME_LENGTH[slot] = iIccPhb.getUSIMAASMaxNameLen();
                }
            } catch (android.os.RemoteException e) {
                Log.d(TAG, "catched exception.");
                MAX_USIM_AAS_NAME_LENGTH[slot] = -1;
            }
        }
        Log.d(TAG, "getAASMaxLength() end slot:" + slot + "|maxNameLen:" + MAX_USIM_AAS_NAME_LENGTH[slot]);
        return MAX_USIM_AAS_NAME_LENGTH[slot];
    }

    public static IIccPhoneBook getIIccPhoneBook(int slotId) {
        Log.d(TAG, "[getIIccPhoneBook]slotId:" + slotId);
        String serviceName;
        if (MTK_GEMINI_SUPPORT) {
            serviceName = (slotId == SLOT_ID2) ? SIMPHONEBOOK2_SERVICE : SIMPHONEBOOK_SERVICE;
        } else {
            serviceName = SIMPHONEBOOK_SERVICE;
        }
        final IIccPhoneBook iIccPhb = IIccPhoneBook.Stub.asInterface(ServiceManager.getService(serviceName));
        return iIccPhb;
    }

    /**
     * Check whether the data in entry is additional number.
     * @param entry
     * @return
     */
    public static boolean isAdditionalNumber(ValuesDelta entry) {
        final String key = Data.IS_ADDITIONAL_NUMBER;
        Integer isAnr = entry.getAsInteger(key);
        return isAnr != null && 1 == isAnr.intValue();
    }

    /**
     * Check whether the data in ContentValues is additional number.
     * @param after
     * @param before
     * @return
     */
    public static boolean isAdditionalNumber(final ContentValues cv) {
        final String key = Data.IS_ADDITIONAL_NUMBER;
        Integer isAnr = null;
        if (cv != null && cv.containsKey(key)) {
            isAnr = cv.getAsInteger(key);
        }
        return isAnr != null && 1 == isAnr.intValue();
    }

    public static boolean updateDataKind(Context context, int slotId, DataKind kind) {
        if (kind == null) return false;
        final String accountType = SimUtils.getAccountTypeBySlot(slotId);
        Log.d(TAG, "updateDataKind() mSlotId=" + slotId + ", curAccountType=" + accountType);
        if (ExtensionManager.getInstance().getContactAccountExtension().isFeatureAccount(accountType,
                ExtensionManager.COMMD_FOR_AAS)
                && ExtensionManager.getInstance().getContactAccountExtension().isPhone(kind.mimeType, ExtensionManager.COMMD_FOR_AAS)) {
            UsimAccountType.updatePhoneType(slotId, kind);
            return true;
        }
        return false;
    }

    /**
     * reset the selection position for Spinner. AAS Feature.
     * @param label
     * @param adapter
     * @param item
     * @param kind
     * @return
     */
    public static boolean rebuildLabelSelection(Spinner label, ArrayAdapter<EditType> adapter, EditType item,
            DataKind kind) {
        if (item == null || kind == null) {
            return false;
        }
        ContactAccountExtension cae = ExtensionManager.getInstance().getContactAccountExtension();
        int slotId = cae.getCurrentSlot(ExtensionManager.COMMD_FOR_AAS);
        Log.i(TAG, "rebuildLabelSelection() slotId=" + slotId);
        if (cae.isFeatureAccount(SimUtils.getAccountTypeBySlot(slotId), ExtensionManager.COMMD_FOR_AAS)
                && cae.isPhone(kind.mimeType, ExtensionManager.COMMD_FOR_AAS) && isAasPhoneType(item.rawValue)) {
            for (int i = 0; i < adapter.getCount(); i++) {
                EditType type = adapter.getItem(i);
                if (type.customColumn != null && type.customColumn.equals(item.customColumn)) {
                    label.setSelection(i);
                    Log.i(TAG, "rebuildLabelSelection() position=" + i);
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isAasPhoneType(int type) {
        return Anr.TYPE_AAS == type;
        // ContactAccountExtension cae =
        // ExtensionManager.getInstance().getContactAccountExtension();
        // final int slotId = cae.getCurrentSlot(ContactAccountExtension.FEATURE_AAS);
        // final String accountType = getAccountTypeBySlot(slotId);
        // Log.d(TAG, "isAasPhoneType() slotId=" + slotId + " accountType=" + accountType);
        // if (cae.isFeatureAccount(accountType, ContactAccountExtension.FEATURE_AAS)) {
        // Log.d(TAG, "isAasPhoneType() " + (Anr.TYPE_AAS == type));
        // return Anr.TYPE_AAS == type;
        // }
        // return false;
    }

    public static boolean updatemEntryValue(ValuesDelta entry, EditType type) {
        if (isAasPhoneType(type.rawValue)) {
            entry.put(Phone.LABEL, type.customColumn);
            return true;
        }
        return false;
    }

    public static boolean ensureAASKindExists(RawContactDelta state, AccountType accountType, String mimeType, DataKind kind) {
        ContactAccountExtension cae = ExtensionManager.getInstance().getContactAccountExtension();

        if (kind != null && cae.isFeatureAccount(accountType.accountType, ExtensionManager.COMMD_FOR_AAS)
                && cae.isPhone(mimeType, ExtensionManager.COMMD_FOR_AAS)) {
            ArrayList<ValuesDelta> values = state.getMimeEntries(mimeType);
            final int slotId = ExtensionManager.getInstance().getContactAccountExtension().getCurrentSlot(ExtensionManager.COMMD_FOR_AAS);
            final int slotAnrSize = ExtensionManager.getInstance().getContactDetailExtension().getAdditionNumberCount(
                    slotId, ExtensionManager.COMMD_FOR_AAS);
            if (values != null && values.size() == slotAnrSize + 1) {
                // primary number + slotNumber size
                Log.d(TAG, "ensureAASKindExists() size=" + values.size() + " slotAnrSize=" + slotAnrSize);
                return true;
            }
            if (values == null || values.isEmpty()) {
                Log.d(TAG, "ensureAASKindExists() Empty, insert primary:1 and anr:" + slotAnrSize);
                // Create child when none exists and valid kind
                final ValuesDelta child = RawContactModifier.insertChild(state, kind);
                if (kind.mimeType.equals(Phone.CONTENT_ITEM_TYPE)) {
                    child.setFromTemplate(true);
                }

                for (int i = 0; i < slotAnrSize; i++) {
                    final ValuesDelta slotChild = RawContactModifier.insertChild(state, kind);
                    slotChild.put(Data.IS_ADDITIONAL_NUMBER, 1);
                }
            } else {
                int pnrSize = 0;
                int anrSize = 0;
                if (values != null) {
                    for (ValuesDelta value : values) {
                        Integer isAnr = value.getAsInteger(Data.IS_ADDITIONAL_NUMBER);
                        if (isAnr != null && (isAnr.intValue() == 1)) {
                            anrSize++;
                        } else {
                            pnrSize++;
                        }
                    }
                }
                Log.d(TAG, "ensureAASKindExists() pnrSize=" + pnrSize + ", anrSize=" + slotAnrSize);
                if (pnrSize < 1) {
                    // insert a empty primary number if not exists.
                    RawContactModifier.insertChild(state, kind);
                }
                for (; anrSize < slotAnrSize; anrSize++) {
                    // insert additional numbers if not full.
                    final ValuesDelta slotChild = RawContactModifier.insertChild(state, kind);
                    slotChild.put(Data.IS_ADDITIONAL_NUMBER, 1);
                }
            }
            return true;
        }
        return false;
    }

    public static EditType getAasEditType(ValuesDelta entry, DataKind kind, int phoneType) {
        if (ExtensionManager.getInstance().getContactAccountExtension().isFeatureEnabled(
                ExtensionManager.COMMD_FOR_AAS)
                && phoneType == Anr.TYPE_AAS) {
            String customColumn = entry.getAsString(Data.DATA3);
            Log.d(TAG, "getAasEditType() customColumn=" + customColumn);
            if (customColumn != null) {
                for (EditType type : kind.typeList) {
                    if (type.rawValue == Anr.TYPE_AAS && customColumn.equals(type.customColumn)) {
                        Log.d(TAG, "getAasEditType() type");
                        return type;
                    }
                }
            }
            return null;
        }
        Log.e(TAG, "getAasEditType() error Not Anr.TYPE_AAS, type=" + phoneType);
        return null;
    }

    public static CharSequence getLabelForBindData(Resources res, int type, String customLabel, String mimeType,
            Cursor cursor, CharSequence defaultValue) {
        if (ExtensionManager.getInstance().getContactAccountExtension().isFeatureEnabled(
                ExtensionManager.COMMD_FOR_AAS)) {
            CharSequence label = defaultValue;
            final int indicate = cursor.getColumnIndex(Contacts.INDICATE_PHONE_SIM);
            int simId = -1;
            if (indicate != -1) {
                simId = cursor.getInt(indicate);
            }
            final int slotId = SIMInfoWrapper.getDefault().getSimSlotById(simId);
            String accountType = getAccountTypeBySlot(slotId);
            if (ExtensionManager.getInstance().getContactAccountExtension().isFeatureAccount(accountType,
                    ExtensionManager.COMMD_FOR_AAS) && mimeType.equals(Email.CONTENT_ITEM_TYPE)) {
                label = "";
            } else {
                label = ExtensionManager.getInstance().getContactAccountExtension().getTypeLabel(res, type,
                        customLabel, slotId, ExtensionManager.COMMD_FOR_AAS);
            }
            return label;
        }
        return defaultValue;
    }

    /**
     * For AAS or SNE feature insert data to DB. If the feature is not disable, return null.
     * @param accountType
     * @param mAccount
     * @param name
     * @param number
     * @param email
     * @param additionalNumber
     * @param resolver
     * @param indicate
     * @param simType
     * @param indexInSim
     * @param grpAddIds
     * @param anrsList
     * @param nickname
     * @return
     */
    public static Uri insertToDB(String accountType, Account mAccount, String name, String number,
            String email, String additionalNumber, ContentResolver resolver,
            long indicate, String simType, long indexInSim,
            Set<Long> grpAddIds, /* M:AAS */ArrayList<Anr> anrsList,/* M:SNE */
            String nickname) {
        Uri retUri = null;
        if (!ExtensionManager.getInstance().getContactAccountExtension().isFeatureAccount(accountType,
                ExtensionManager.COMMD_FOR_AAS)
                && !ExtensionManager.getInstance().getContactAccountExtension().isFeatureAccount(accountType,
                        ExtensionManager.COMMD_FOR_SNE)) {
            Log.d(TAG, "insertToDB()-Not AAS or SNE, return null, go default.");
            return retUri;
        }

        final ArrayList<ContentProviderOperation> operationList = 
            new ArrayList<ContentProviderOperation>();
        ContentProviderOperation.Builder builder = ContentProviderOperation
                .newInsert(RawContacts.CONTENT_URI);
        ContentValues contactvalues = new ContentValues();
        contactvalues.put(RawContacts.ACCOUNT_NAME, mAccount.name);
        contactvalues.put(RawContacts.ACCOUNT_TYPE, mAccount.type);
        contactvalues.put(RawContacts.INDICATE_PHONE_SIM, indicate);
        contactvalues.put(RawContacts.AGGREGATION_MODE,
                RawContacts.AGGREGATION_MODE_DISABLED);

        contactvalues.put(RawContacts.INDEX_IN_SIM, indexInSim); // index in SIM
        
        builder.withValues(contactvalues);

        operationList.add(builder.build());

        int phoneType = 7;
        String phoneTypeSuffix = "";
        // mtk80909 for ALPS00023212
        if (!TextUtils.isEmpty(name)) {
            final NamePhoneTypePair namePhoneTypePair = new NamePhoneTypePair(name);
        name = namePhoneTypePair.name;
                phoneType = namePhoneTypePair.phoneType;
                phoneTypeSuffix = namePhoneTypePair.phoneTypeSuffix;
        }

        //insert number
        if (!TextUtils.isEmpty(number)) {
//          number = PhoneNumberFormatUtilEx.formatNumber(number);
            builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            builder.withValueBackReference(Phone.RAW_CONTACT_ID, 0);
            builder.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
            builder.withValue(Phone.NUMBER, number);
            // mtk80909 for ALPS00023212
//            builder.withValue(Phone.TYPE, phoneType);

            /** M:AAS USim contact's primary phone number does not have type. @ { */
            ExtensionManager.getInstance().getContactAccountExtension().checkOperationBuilder(
                    mAccount.type, builder, null, ContactAccountExtension.TYPE_OPERATION_INSERT, ExtensionManager.COMMD_FOR_AAS);
            /** M: @ } */

            if (!TextUtils.isEmpty(phoneTypeSuffix)) {
                builder.withValue(Data.DATA15, phoneTypeSuffix);
            }
            operationList.add(builder.build());
        } 

        //insert name
        if (!TextUtils.isEmpty(name)) {
            builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            builder.withValueBackReference(StructuredName.RAW_CONTACT_ID, 0);
            builder.withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
            builder.withValue(StructuredName.DISPLAY_NAME, name);
            operationList.add(builder.build());
        }
        

        //if USIM
        if (simType.equals("USIM")) {
            //insert email          
        if (!TextUtils.isEmpty(email)) {
//            for (String emailAddress : emailAddressArray) {
                Log.i(TAG, "In actuallyImportOneSimContact email is " + email);
                builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                builder.withValueBackReference(Email.RAW_CONTACT_ID, 0);
                builder.withValue(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
                builder.withValue(Email.TYPE, Email.TYPE_MOBILE);
                builder.withValue(Email.DATA, email);
                operationList.add(builder.build());
//            }
            }

            /** M:SNE @ { */
            ExtensionManager.getInstance().getContactAccountExtension().buildOperation(
                    mAccount.type, operationList, null, nickname, 0,
                    ContactAccountExtension.TYPE_OPERATION_SNE, ExtensionManager.COMMD_FOR_SNE);
            /** M: @ } */

            /** M:AAS @ { */
            if (!ExtensionManager.getInstance().getContactAccountExtension().buildOperation(
                    mAccount.type, operationList, anrsList, additionalNumber, 0,
                    ContactAccountExtension.TYPE_OPERATION_AAS, ExtensionManager.COMMD_FOR_AAS)) {
                /** M: @ } */
                if (!TextUtils.isEmpty(additionalNumber)) {
                    // additionalNumber =
                    // PhoneNumberFormatUtilEx.formatNumber(additionalNumber);
                    Log.i(TAG, "additionalNumber is " + additionalNumber);
                    builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                    builder.withValueBackReference(Phone.RAW_CONTACT_ID, 0);
                    builder.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
                    // builder.withValue(Phone.TYPE, phoneType);
                    builder.withValue(Data.DATA2, 7);
                    builder.withValue(Phone.NUMBER, additionalNumber);
                    builder.withValue(Data.IS_ADDITIONAL_NUMBER, 1);
                    operationList.add(builder.build());
                }
            }

            //USIM Group begin
            if (grpAddIds != null && grpAddIds.size() > 0) {
                Long [] grpIdArray = grpAddIds.toArray(new Long[0]);
                for (Long grpId: grpIdArray) {
                    builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                    builder.withValueBackReference(Phone.RAW_CONTACT_ID, 0);
                    builder.withValue(Data.MIMETYPE, GroupMembership.CONTENT_ITEM_TYPE);
                    builder.withValue(GroupMembership.GROUP_ROW_ID, grpId);
                    operationList.add(builder.build());
                }
            }
            //USIM group end
        }

        try {
            ContentProviderResult[] result = resolver.applyBatch(
                    ContactsContract.AUTHORITY, operationList); //saved in database
            Uri rawContactUri = result[0].uri;
            Log.w(TAG, "[insertToDB]rawContactUri:" + rawContactUri);
            retUri = RawContacts.getContactLookupUri(resolver, rawContactUri);
            Log.w(TAG, "[insertToDB]retUri:" + retUri);
        } catch (RemoteException e) {
            Log.e(TAG, String.format("%s: %s", e.toString(), e
                    .getMessage()));
        } catch (OperationApplicationException e) {
            Log.e(TAG, String.format("%s: %s", e.toString(), e
                    .getMessage()));
        }
        
        return retUri;
        
    }

    /**
     * if build buildInsertOperation return success, else return false
     * @param operationList
     * @param mAccount
     * @param name
     * @param number
     * @param email
     * @param additionalNumber
     * @param resolver
     * @param indicate
     * @param simType
     * @param indexInSim
     * @param grpAddIds
     * @param anrsList
     * @param nickname
     * @return
     */
    public static boolean buildInsertOperation(ArrayList<ContentProviderOperation> operationList, Account account,
            String name, String number, String email, String additionalNumber, ContentResolver resolver, long indicate,
            String simType, long indexInSim, Set<Long> grpAddIds, ArrayList<Anr> anrsList, String nickname) {
        if (operationList == null) {
            return false;
        }
        ContactAccountExtension cae = ExtensionManager.getInstance().getContactAccountExtension();
        if (!cae.isFeatureAccount(account.type, ExtensionManager.COMMD_FOR_AAS)
                && !cae.isFeatureAccount(account.type, ExtensionManager.COMMD_FOR_SNE)) {
            return false;
        }
        Uri retUri = null;
        int backRef = operationList.size();
        ContentProviderOperation.Builder builder = ContentProviderOperation
                .newInsert(RawContacts.CONTENT_URI);
        ContentValues contactvalues = new ContentValues();
        contactvalues.put(RawContacts.ACCOUNT_NAME, account.name);
        contactvalues.put(RawContacts.ACCOUNT_TYPE, account.type);
        contactvalues.put(RawContacts.INDICATE_PHONE_SIM, indicate);
        contactvalues.put(RawContacts.AGGREGATION_MODE,
                RawContacts.AGGREGATION_MODE_DISABLED);
        contactvalues.put(RawContacts.INDEX_IN_SIM, indexInSim); // index in SIM
        builder.withValues(contactvalues);
    
        operationList.add(builder.build());
    
        int phoneType = 7;
        String phoneTypeSuffix = "";
        // ALPS00023212
        if (!TextUtils.isEmpty(name)) {
            final NamePhoneTypePair namePhoneTypePair = new NamePhoneTypePair(name);
            name = namePhoneTypePair.name;
            phoneType = namePhoneTypePair.phoneType;
            phoneTypeSuffix = namePhoneTypePair.phoneTypeSuffix;
        }
    
        // insert phone number
        if (!TextUtils.isEmpty(number)) {
            builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            builder.withValueBackReference(Phone.RAW_CONTACT_ID, backRef);
            builder.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
            builder.withValue(Phone.NUMBER, number);

            /** M:AAS, primary number has no type. */
            ExtensionManager.getInstance().getContactAccountExtension().checkOperationBuilder(
                    account.type, builder, null, ContactAccountExtension.TYPE_OPERATION_INSERT, ExtensionManager.COMMD_FOR_AAS);
            /** M: @ } */

            if (!TextUtils.isEmpty(phoneTypeSuffix)) {
                builder.withValue(Data.DATA15, phoneTypeSuffix);
            }
            operationList.add(builder.build());
        }
    
        // insert name
        if (!TextUtils.isEmpty(name)) {
            builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            builder.withValueBackReference(StructuredName.RAW_CONTACT_ID, backRef);
            builder.withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
            builder.withValue(StructuredName.DISPLAY_NAME, name);
            operationList.add(builder.build());
        }
    
        // if USIM
        if (simType.equals("USIM")) {
            // insert email
            if (!TextUtils.isEmpty(email)) {
                // for (String emailAddress : emailAddressArray) {
                Log.i(TAG, "In actuallyImportOneSimContact email is " + email);
                builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                builder.withValueBackReference(Email.RAW_CONTACT_ID, backRef);
                builder.withValue(Data.MIMETYPE, Email.CONTENT_ITEM_TYPE);
                builder.withValue(Email.TYPE, Email.TYPE_MOBILE);
                builder.withValue(Email.DATA, email);
                operationList.add(builder.build());
                // }
            }

            /** M:AAS @ { */
            if (!ExtensionManager.getInstance().getContactAccountExtension().buildOperation(
                    account.type, operationList, anrsList, additionalNumber, backRef,
                    ContactAccountExtension.TYPE_OPERATION_AAS, ExtensionManager.COMMD_FOR_AAS)) {
                /** M: @ } */
                if (!TextUtils.isEmpty(additionalNumber)) {
                    // additionalNumber =
                    // PhoneNumberFormatUtilEx.formatNumber(additionalNumber);
                    Log.i(TAG, "additionalNumber is " + additionalNumber);
                    builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                    builder.withValueBackReference(Phone.RAW_CONTACT_ID, backRef);
                    builder.withValue(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
                    // builder.withValue(Phone.TYPE, phoneType);
                    builder.withValue(Data.DATA2, 7);
                    builder.withValue(Phone.NUMBER, additionalNumber);
                    builder.withValue(Data.IS_ADDITIONAL_NUMBER, 1);
                    operationList.add(builder.build());
                }
            }

            /** M:SNE @ { */
            Log.d(TAG, "buildInsertOperation, Nickname is " + nickname);
            ExtensionManager.getInstance().getContactAccountExtension().buildOperation(
                    account.type, operationList, null, nickname, backRef,
                    ContactAccountExtension.TYPE_OPERATION_SNE, ExtensionManager.COMMD_FOR_SNE);
            /** M: @ { */

            // for USIM Group
            if (grpAddIds != null && grpAddIds.size() > 0) {
                Long[] grpIdArray = grpAddIds.toArray(new Long[0]);
                for (Long grpId : grpIdArray) {
                    builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                    builder.withValueBackReference(Phone.RAW_CONTACT_ID, backRef);
                    builder.withValue(Data.MIMETYPE, GroupMembership.CONTENT_ITEM_TYPE);
                    builder.withValue(GroupMembership.GROUP_ROW_ID, grpId);
                    operationList.add(builder.build());
                }
            }
        }
        return true;
    }

    /**
     * refresh Datakind & update views
     * @param context
     * @param slotId
     * @param state
     * @param viewGroup
     */
    public static void updateLabelViews(Context context, int slotId, RawContactDeltaList state, ViewGroup viewGroup) {
        final String curAccountType = SimUtils.getAccountTypeBySlot(slotId);
        Log.d(TAG, "onStart() mSlotId=" + slotId + ", curAccountType=" + curAccountType);
        if (ExtensionManager.getInstance().getContactAccountExtension().isFeatureAccount(curAccountType,
                ExtensionManager.COMMD_FOR_AAS)) {
            int numRawContacts = state.size();
            for (RawContactDelta entity : state) {
                final ValuesDelta values = entity.getValues();
                if (!values.isVisible()) continue;

                final String accountType = values.getAsString(RawContacts.ACCOUNT_TYPE);
                final String dataSet = values.getAsString(RawContacts.DATA_SET);
                final AccountType type = AccountTypeManager.getInstance(context).getAccountType(
                        accountType, dataSet);
                Log.d(TAG, "onStart() AccountType:" + type.accountType);
                if (curAccountType.equals(type.accountType)) {
                    DataKind dataKind = type.getKindForMimetype(Phone.CONTENT_ITEM_TYPE);
                    SimUtils.updateDataKind(context, slotId, dataKind);
                    updateEditorViewsLabel(viewGroup);
                    break;
                }
            }
        }
    }

    private static void updateEditorViewsLabel(ViewGroup viewGroup) {
        int count = viewGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            View v = viewGroup.getChildAt(i);
            if (v instanceof TextFieldsEditorView) {
                ((LabeledEditorView) v).updateValues();
            } else if (v instanceof ViewGroup) {
                updateEditorViewsLabel((ViewGroup) v);
            }
        }
    }

    /**
     * ensure phone kind updated and exists
     * @param type
     * @param slotId
     * @param entity
     */
    public static void ensurePhoneKindForEditorExt(AccountType type, int slotId, RawContactDelta entity) {
        if (ExtensionManager.getInstance().getContactAccountExtension().isFeatureAccount(type.accountType,
                ExtensionManager.COMMD_FOR_AAS)) {
            DataKind dataKind = type.getKindForMimetype(Phone.CONTENT_ITEM_TYPE);
            if (dataKind != null) {
                UsimAccountType.updatePhoneType(slotId, dataKind);
            }
            RawContactModifier.ensureKindExists(entity, type, Phone.CONTENT_ITEM_TYPE);
        }
    }
}