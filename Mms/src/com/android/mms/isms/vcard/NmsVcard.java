/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

/*
 * Copyright (C) 2007-2008 Esmertec AG.
 * Copyright (C) 2007-2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.mms.isms.vcard;

import java.io.UnsupportedEncodingException;

import android.content.Context;
import android.database.Cursor;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.text.TextUtils;

import com.mediatek.encapsulation.MmsLog;

public class NmsVcard {

    private static final String ISMS_TAG = "Mms/isms/vcard";
    private static String mContactId = null;
    private static Context mContext = null;
    // private static ContactStruct contact;
    private static NmsContactStruct Montact;

    public NmsVcard(Context context, NmsContactStruct struct, String cId) {
        mContext = context;
        Montact = struct;
        mContactId = cId;
    }

    public void initAllData() {
        Montact.UID = mContactId;
        try {
            getPhone();
            getEmail();
            getFormartName();
            getTimeStamp();
            getOthers();
        } catch (Exception e) {
            MmsLog.e(ISMS_TAG, "NmsVcard.initAllData(): ", e);
        }
    }

    private void getOthers() {
        Montact.others = "";
        getPostal();
        getOrganization();
        getNote();
    }

    private void getTimeStamp() {

        // Cursor c =
        // context.getContentResolver().query(RawContacts.CONTENT_URI, new
        // String[]{RawContacts.VERSION},
        // ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " +
        // contactId,
        // null, null);
        // while (c.moveToNext()) {
        // int index = c.getColumnIndex(ContactsContract.RawContacts.VERSION);
        // contact.TimeStamp =
        // NmsConverter.int2String(NmsTimer.NmsGetSystemTime());
        // }
        // c.close();
    }

    public NmsContactStruct getContact() {
        mContext = null;
        mContactId = null;
        return Montact;
    }

    private void getPhone() {
        Cursor phones = mContext.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.LABEL,
                        ContactsContract.CommonDataKinds.Phone.IS_PRIMARY },
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + mContactId, null, null);
        while (phones.moveToNext()) {
            String phoneNumber = phones.getString(phones
                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            int phoneType = phones.getInt(phones
                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
            String phoneLabel = phones.getString(phones
                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL));
            String phoneIsPrimary = phones.getString(phones
                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.IS_PRIMARY));

            phoneNumber = replaceExceptionalChars(phoneNumber);
            Montact.addPhone(phoneType, phoneNumber, phoneLabel, phoneIsPrimary.equals("0") ? false
                    : true);
        }
        phones.close();
    }

    private void getEmail() {
        Cursor emails = mContext.getContentResolver().query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                new String[] { ContactsContract.CommonDataKinds.Email.DATA,
                        ContactsContract.CommonDataKinds.Email.TYPE,
                        ContactsContract.CommonDataKinds.Email.IS_PRIMARY },
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + mContactId, null, null);
        if (emails != null) {
            while (emails.moveToNext()) {
                String emailAddress = emails.getString(emails
                        .getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                int emailType = emails.getInt(emails
                        .getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
                String emailIsPrimary = emails.getString(emails
                        .getColumnIndex(ContactsContract.CommonDataKinds.Email.IS_PRIMARY));
                Montact.addContactmethod(Contacts.KIND_EMAIL, emailType, emailAddress, "",
                        emailIsPrimary.equals("0") ? false : true);
            }
            emails.close();
        }
    }

    private void getIm() {
        Cursor ims = mContext.getContentResolver().query(
                Data.CONTENT_URI,
                new String[] { ContactsContract.CommonDataKinds.Im.TYPE,
                        ContactsContract.CommonDataKinds.Im.DATA,
                        ContactsContract.CommonDataKinds.Im.IS_PRIMARY },
                ContactsContract.CommonDataKinds.Im.CONTACT_ID + "=" + mContactId + " and "
                        + Data.MIMETYPE + "='"
                        + ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE + "'", null, null);
        if (ims != null) {
            while (ims.moveToNext()) {
                int ImType = ims.getInt(ims
                        .getColumnIndex(ContactsContract.CommonDataKinds.Im.TYPE));
                String ImData = ims.getString(ims
                        .getColumnIndex(ContactsContract.CommonDataKinds.Im.DATA));
                String ImLabel = ims.getString(ims
                        .getColumnIndex(ContactsContract.CommonDataKinds.Im.LABEL));
                String ImIsPrimary = ims.getString(ims
                        .getColumnIndex(ContactsContract.CommonDataKinds.Im.IS_PRIMARY));
                Montact.addContactmethod(Contacts.KIND_IM, ImType, ImData, ImLabel,
                        ImIsPrimary.equals("0") ? false : true);
            }
            ims.close();
        }
    }

    private void getPostal() {
        String addrWhere = ContactsContract.Data.CONTACT_ID + " = ? AND "
                + ContactsContract.Data.MIMETYPE + " = ?";
        String[] addrWhereParams = new String[] { mContactId,
                ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE };
        Cursor addrCur = mContext.getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                null, addrWhere, addrWhereParams, null);
        while (addrCur != null && addrCur.moveToNext()) {
            String postal = "ADR;";
            int type = addrCur.getInt(addrCur
                    .getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE));

            if (type == ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK) {
                postal += "WORK;";
            } else {
                postal += "HOME;";
            }
            postal += "ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:";
            try {
                String poBox = addrCur.getString(addrCur
                        .getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POBOX));
                postal += ConvertString2VCardUTF8(poBox) + ";";

                String street = addrCur.getString(addrCur
                        .getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
                postal += ConvertString2VCardUTF8(street) + ";";

                String city = addrCur.getString(addrCur
                        .getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
                postal += ConvertString2VCardUTF8(city) + ";";

                String state = addrCur.getString(addrCur
                        .getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION));
                postal += ConvertString2VCardUTF8(state) + ";";

                String country = addrCur.getString(addrCur
                        .getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY));
                postal += ConvertString2VCardUTF8(country) + ";\r\n";
                Montact.others += postal;
            } catch (Exception e) {
                MmsLog.e(ISMS_TAG, "NmsVcard.getPostal(): ", e);
            }
        }
        addrCur.close();

    }

    private void getOrganization() {

        Cursor organization = mContext.getContentResolver().query(
                Data.CONTENT_URI,
                new String[] { ContactsContract.CommonDataKinds.Organization.TYPE,
                        ContactsContract.CommonDataKinds.Organization.COMPANY,
                        ContactsContract.CommonDataKinds.Organization.PHONETIC_NAME,
                        ContactsContract.CommonDataKinds.Organization.TITLE,
                        ContactsContract.CommonDataKinds.Organization.IS_PRIMARY },
                ContactsContract.CommonDataKinds.Organization.CONTACT_ID + " = " + mContactId
                        + " and " + Data.MIMETYPE + "='"
                        + ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE + "'",
                null, null);
        if (organization != null) {
            while (organization.moveToNext()) {
                int type = organization.getInt(organization
                        .getColumnIndex(ContactsContract.CommonDataKinds.Organization.TYPE));

                String company = organization.getString(organization
                        .getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY));

                String positionName = organization
                        .getString(organization
                                .getColumnIndex(ContactsContract.CommonDataKinds.Organization.PHONETIC_NAME));
                String title = organization.getString(organization
                        .getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE));
                company = company + " " + title;

                String isPrimary = organization.getString(organization
                        .getColumnIndex(ContactsContract.CommonDataKinds.Organization.IS_PRIMARY));

                if (!TextUtils.isEmpty(company)) {
                    try {
                        company = "ORG;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:"
                                + ConvertString2VCardUTF8(company) + "\r\n";
                        Montact.others = Montact.others + company;
                    } catch (UnsupportedEncodingException e) {
                        MmsLog.e(ISMS_TAG, "NmsVcard.getOrganization(): ", e);
                    }
                }
                if (!TextUtils.isEmpty(positionName)) {
                    try {
                        positionName = "ADR;WORK;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:"
                                + ConvertString2VCardUTF8(positionName) + "\r\n";
                        Montact.others = Montact.others + positionName;
                    } catch (UnsupportedEncodingException e) {
                        MmsLog.e(ISMS_TAG, "NmsVcard.getOrganization(): ", e);
                    }
                }
            }
            organization.close();
        }
    }

    private void getNote() {
        Cursor note = mContext.getContentResolver()
                .query(Data.CONTENT_URI,
                        new String[] { ContactsContract.CommonDataKinds.Note.NOTE },
                        ContactsContract.CommonDataKinds.Note.CONTACT_ID + " = " + mContactId
                                + " and " + Data.MIMETYPE + "='"
                                + ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE + "'",
                        null, null);

        if (note != null) {
            while (note.moveToNext()) {
                String notes = note.getString(note
                        .getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE));
                if (!TextUtils.isEmpty(notes)) {
                    try {
                        notes = "NOTE;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:"
                                + ConvertString2VCardUTF8(notes) + "\r\n";
                    } catch (UnsupportedEncodingException e) {
                        MmsLog.e(ISMS_TAG, "NmsVcard.getNote(): ", e);
                    }
                    Montact.others += notes;
                }
            }
            note.close();
        }
    }

    public static byte ConverAsc2Char(byte aChar) {
        if (aChar >= 0xA && aChar <= 0xF)
            return (byte) (aChar + 65 - 10);
        else if (aChar >= 0 && aChar <= 9)
            return (byte) (aChar + 48);
        else {
            return 0;
        }
    }

    public static String ConvertString2VCardUTF8(String str) throws UnsupportedEncodingException {
        if (TextUtils.isEmpty(str))
            return "";
        byte[] pStrUtf8 = str.getBytes("UTF-8");
        byte[] pStrVCardUtf8 = new byte[pStrUtf8.length * 4];
        int j = 0;

        for (int i = 0; i < pStrUtf8.length; i++) {

            pStrVCardUtf8[j] = (byte) '=';
            j++;

            byte temp = pStrUtf8[i];
            temp = (byte) (temp >> 4);
            byte temp1 = (byte) 0x0f;

            temp = (byte) (temp & temp1);

            pStrVCardUtf8[j] = temp;
            pStrVCardUtf8[j] = ConverAsc2Char(pStrVCardUtf8[j]);
            j++;

            pStrVCardUtf8[j] = (byte) (pStrUtf8[i] & temp1);
            pStrVCardUtf8[j] = ConverAsc2Char(pStrVCardUtf8[j]);
            j++;

        }
        return new String(pStrVCardUtf8, 0, j);
    }

    private void getFormartName() {
        Cursor cursor = mContext.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI,
                new String[] { ContactsContract.Contacts.DISPLAY_NAME },
                ContactsContract.Contacts._ID + "=" + mContactId, null, null);
        while (cursor != null && cursor.moveToNext()) {
            Montact.name = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        }
        if (cursor != null) {
            cursor.close();
        }

        if (Montact.name != null) {
            // here should delete
            Montact.name = Montact.name.trim();
            Montact.sourceName = Montact.name;
            String temp = "ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:";
            try {
                temp += ConvertString2VCardUTF8(Montact.name);
                Montact.name = temp;

            } catch (UnsupportedEncodingException e) {
                MmsLog.e(ISMS_TAG, "NmsVcard.getFormartName(): ", e);
            }
        }
    }

    private void getPhoto() {
        Cursor photo = mContext.getContentResolver().query(
                Data.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Photo.CONTACT_ID + " = " + mContactId + " and "
                        + Data.MIMETYPE + "='"
                        + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'", null,
                null);
        if (photo != null) {
            while (photo.moveToNext()) {
                Montact.photoBytes = photo.getBlob(photo
                        .getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO));
            }
            photo.close();
        }
    }

    private String replaceExceptionalChars(String sourceString) {

        String desString = sourceString.replace("-", "");
        desString = desString.replace("(", "");
        desString = desString.replace(")", "");

        MmsLog.d(ISMS_TAG, "finish to replace excetional chars,target String:"
                + desString.toString() + ", source String:" + sourceString);

        // return targetString.toString();
        return desString;
    }
}
