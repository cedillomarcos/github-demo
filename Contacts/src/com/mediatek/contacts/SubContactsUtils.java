package com.mediatek.contacts;

import android.accounts.Account;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import android.util.Log;

import com.android.contacts.ContactsUtils;

import java.util.ArrayList;
import java.util.Set;

public class SubContactsUtils extends ContactsUtils {
   
    private static final String TAG = "SubContactsUtils";

    public static long queryForRawContactId(ContentResolver cr, long contactId) {
        Cursor rawContactIdCursor = null;
        long rawContactId = -1;
        try {
            rawContactIdCursor = cr.query(RawContacts.CONTENT_URI,
                    new String[] {RawContacts._ID},
                    RawContacts.CONTACT_ID + "=" + contactId, null, null);
            if (rawContactIdCursor != null && rawContactIdCursor.moveToFirst()) {
                // Just return the first one.
                rawContactId = rawContactIdCursor.getLong(0);
            }
        } finally {
            if (rawContactIdCursor != null) {
                rawContactIdCursor.close();
            }
        }
        return rawContactId;
    }

    // For index in SIM change feature, we add the 'int indexInSim' argument 
    // into the argument list.
    
    public static Uri insertToDB(Account mAccount, String name, String number, String email,
            String additionalNumber, ContentResolver resolver, long indicate,
            String simType, long indexInSim, Set<Long> grpAddIds) {
        Uri retUri = null;
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
            builder.withValue(Data.DATA2, 2);

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

    public static void buildInsertOperation(
            ArrayList<ContentProviderOperation> operationList,
            Account mAccount, String name, String number, String email,
            String additionalNumber, ContentResolver resolver, long indicate,
            String simType, long indexInSim, Set<Long> grpAddIds) {

        if (operationList == null) {
            return;
        }
        Uri retUri = null;
        int backRef = operationList.size();
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
            builder.withValue(Data.DATA2, 2);

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
    }
    
 // mtk80909 for ALPS00023212
    public static class NamePhoneTypePair {
        public String name;
        public int phoneType;
        public String phoneTypeSuffix;
        public NamePhoneTypePair(String nameWithPhoneType) {
            // Look for /W /H /M or /O at the end of the name signifying the type
            int nameLen = nameWithPhoneType.length();
            if (nameLen - 2 >= 0 && nameWithPhoneType.charAt(nameLen - 2) == '/') {
                char c = Character.toUpperCase(nameWithPhoneType.charAt(nameLen - 1));
                phoneTypeSuffix = String.valueOf(nameWithPhoneType.charAt(nameLen - 1));
                if (c == 'W') {
                    phoneType = Phone.TYPE_WORK;
                } else if (c == 'M' || c == 'O') {
                    phoneType = Phone.TYPE_MOBILE;
                } else if (c == 'H') {
                    phoneType = Phone.TYPE_HOME;
                } else {
                    phoneType = Phone.TYPE_OTHER;
                }
                name = nameWithPhoneType.substring(0, nameLen - 2);
            } else {
                phoneTypeSuffix = "";
                phoneType = Phone.TYPE_OTHER;
                name = nameWithPhoneType;
            }
        }
    }

}
