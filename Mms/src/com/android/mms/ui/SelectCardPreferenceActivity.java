/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2010. All rights reserved.
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
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
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
package com.android.mms.ui;

import static android.content.res.Configuration.KEYBOARDHIDDEN_NO;

import android.R.color;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.telephony.SmsManager;
import android.provider.Telephony;
import android.os.ServiceManager;

import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.TelephonyIntents;
import com.mediatek.encapsulation.com.android.internal.telephony.EncapsulatedPhone;
import com.mediatek.encapsulation.com.mediatek.telephony.EncapsulatedTelephonyManagerEx;
import com.mediatek.encapsulation.MmsLog;
import com.mediatek.encapsulation.com.android.internal.telephony.EncapsulatedTelephonyService;
import com.mediatek.encapsulation.com.mediatek.common.featureoption.EncapsulatedFeatureOption;
import com.mediatek.encapsulation.android.telephony.EncapsulatedSmsManager;
import com.mediatek.encapsulation.android.telephony.EncapsulatedTelephony.SIMInfo;

import com.android.mms.R;
import com.android.mms.ui.AdvancedEditorPreference.GetSimInfo;
import java.util.List;

/** M:
 * SelectCardPreferenceActivity
 */
public class SelectCardPreferenceActivity extends PreferenceActivity implements GetSimInfo{
    private static final String TAG = "Mms/SelectCardPreferenceActivity";

    private AdvancedEditorPreference mSim1;
    private AdvancedEditorPreference mSim2;
    private AdvancedEditorPreference mSim3;
    private AdvancedEditorPreference mSim4;

    private String mSim1Number;
    private String mSim2Number;
    private String mSim3Number;
    private String mSim4Number;

    private int mSimCount;

    private int mCurrentSim = -1;
    private EditText mNumberText;
    private AlertDialog mNumberTextDialog;
    private List<SIMInfo> mListSimInfo;
    String mIntentPreference;
    private static final int MAX_EDITABLE_LENGTH = 20;
    public static final String SUB_TITLE_NAME = "sub_title_name";
    private SharedPreferences mSpref;
    private int mTitleId = 0;
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        mSpref = PreferenceManager.getDefaultSharedPreferences(this);
        addPreferencesFromResource(R.xml.multicardeditorpreference);
        Intent intent = getIntent();
        mIntentPreference = intent.getStringExtra("preference");
        mTitleId = intent.getIntExtra("preferenceTitleId",0);
        if (mTitleId != 0) {
            setTitle(getString(mTitleId));
        }
    }

    protected void onResume() {
        super.onResume();
        mListSimInfo = SIMInfo.getInsertedSIMList(this);
        mSimCount = mListSimInfo.size();
        changeMultiCardKeyToSimRelated(mIntentPreference);
        if (mSim1 != null) {
            mSim1.setNotifyChange(this);
        }
        if (mSim2 != null) {
            mSim2.setNotifyChange(this);
        }
    }

    private void changeMultiCardKeyToSimRelated(String preference) {

        mSim1 = (AdvancedEditorPreference) findPreference("pref_key_sim1");
        if (mSim1 != null) {
            mSim1.init(this, 0, preference);
        }
        mSim2 = (AdvancedEditorPreference) findPreference("pref_key_sim2");
        if (mSim2 != null) {
            mSim2.init(this, 1, preference);
        }
        mSim3 = (AdvancedEditorPreference) findPreference("pref_key_sim3");
        if (mSim3 != null) {
            mSim3.init(this, 2, preference);
        }
        mSim4 = (AdvancedEditorPreference) findPreference("pref_key_sim4");
        if (mSim4 != null) {
            mSim4.init(this, 3, preference);
        }
        // get the stored value
        // SharedPreferences sp =getSharedPreferences("com.android.mms_preferences",MODE_WORLD_READABLE);
        if (mSimCount == 0) {
            if (mSim1 != null)
                getPreferenceScreen().removePreference(mSim1);
            if (mSim2 != null)
                getPreferenceScreen().removePreference(mSim2);
            if (mSim3 != null)
                getPreferenceScreen().removePreference(mSim3);
            if (mSim4 != null)
                getPreferenceScreen().removePreference(mSim4);
        } else if (mSimCount == 1) {
            if (mSim2 != null)
                getPreferenceScreen().removePreference(mSim2);
            if (mSim3 != null)
                getPreferenceScreen().removePreference(mSim3);
            if (mSim4 != null)
                getPreferenceScreen().removePreference(mSim4);
        } else if (mSimCount == 2) {
            if (mSim3 != null)
                getPreferenceScreen().removePreference(mSim3);
            if (mSim4 != null)
                getPreferenceScreen().removePreference(mSim4);
        } else if (mSimCount == 3) {
            if (mSim4 != null)
                getPreferenceScreen().removePreference(mSim4);
        }
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        int currentId = ((AdvancedEditorPreference)preference).getSlotId();

        if (mIntentPreference.equals(SmsPreferenceActivity.SMS_MANAGE_SIM_MESSAGES)) {
            setTitle(R.string.pref_title_manage_sim_messages);
            startManageSimMessages(currentId);
        } else if (mIntentPreference.equals(GeneralPreferenceActivity.CELL_BROADCAST)) {
            startCellBroadcast(currentId);
        }  else if (mIntentPreference.equals(SmsPreferenceActivity.SMS_SERVICE_CENTER)) {
            //mSim1.setKey(Long.toString(listSimInfo.get(0).mSimId) + "_" + preference);
            mCurrentSim = currentId;
            setServiceCenter(mCurrentSim);
        } else if(mIntentPreference.equals(SmsPreferenceActivity.SMS_SAVE_LOCATION)){
            setSaveLocation(currentId, (AdvancedEditorPreference)preference, this);
        } else if(mIntentPreference.equals(SmsPreferenceActivity.SMS_VALIDITY_PERIOD)){
            // final Preference pref = preference;
            // int slotId = listSimInfo.get(currentId).mSlot;
            int slotId = currentId;
            final int [] peroids = {
                EncapsulatedSmsManager.VALIDITY_PERIOD_NO_DURATION,
                EncapsulatedSmsManager.VALIDITY_PERIOD_ONE_HOUR,
                EncapsulatedSmsManager.VALIDITY_PERIOD_SIX_HOURS,
                EncapsulatedSmsManager.VALIDITY_PERIOD_TWELVE_HOURS,
                EncapsulatedSmsManager.VALIDITY_PERIOD_ONE_DAY,
                EncapsulatedSmsManager.VALIDITY_PERIOD_MAX_DURATION,
            };
            final CharSequence[] items = {
                getResources().getText(R.string.sms_validity_period_nosetting), 
                getResources().getText(R.string.sms_validity_period_1hour), 
                getResources().getText(R.string.sms_validity_period_6hours), 
                getResources().getText(R.string.sms_validity_period_12hours), 
                getResources().getText(R.string.sms_validity_period_1day), 
                getResources().getText(R.string.sms_validity_period_max)};

            /* check validity index*/
            final String validityKey = Long.toString(slotId) + "_" + SmsPreferenceActivity.SMS_VALIDITY_PERIOD;
            int vailidity = mSpref.getInt(validityKey, EncapsulatedSmsManager.VALIDITY_PERIOD_NO_DURATION);
            int currentPosition = 0;
            MmsLog.d(TAG, "validity found the res = " + vailidity);
            for (int i = 0; i < peroids.length; i++) {
                if (vailidity == peroids[i]) {
                    MmsLog.d(TAG, "validity found the position = " + i);
                    currentPosition = i;
                    break;
                }
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getResources().getText(R.string.sms_validity_period));
            builder.setSingleChoiceItems(items, currentPosition, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    SharedPreferences.Editor editor = mSpref.edit();
                    editor.putInt(validityKey, peroids[item]);
                    editor.commit();
                    dialog.dismiss();
                }
            });
            builder.show();
            builder.create();
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
    
    private void setSaveLocation(long id, final AdvancedEditorPreference mSim, final Context context){
        MmsLog.d(TAG, "currentSlot is: " + id);
        //the key value for each saveLocation
        final String [] saveLocation; 
        //the diplayname for each saveLocation
        final String [] saveLocationDisp;
        
        if (!getResources().getBoolean(R.bool.isTablet)) {
            saveLocation = getResources().getStringArray(R.array.pref_sms_save_location_values);
            saveLocationDisp = getResources().getStringArray(R.array.pref_sms_save_location_choices);
        } else {
            saveLocation = getResources().getStringArray(R.array.pref_tablet_sms_save_location_values);
            saveLocationDisp = getResources().getStringArray(R.array.pref_tablet_sms_save_location_choices);
        }
 
           if (saveLocation == null || saveLocationDisp == null){
               MmsLog.d(TAG, "setSaveLocation is null");
               return;
           }

        final String saveLocationKey = Long.toString(id) + "_" + SmsPreferenceActivity.SMS_SAVE_LOCATION;
        int pos = getSelectedPosition(saveLocationKey, saveLocation);
        new AlertDialog.Builder(this)
            .setTitle(R.string.sms_save_location)
            .setNegativeButton(R.string.Cancel, new NegativeButtonListener())
            .setSingleChoiceItems(saveLocationDisp, pos, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        SharedPreferences.Editor editor = mSpref.edit();
                        editor.putString(saveLocationKey, saveLocation[whichButton]);
                        editor.commit();
                        dialog.dismiss();
                        mSim.setNotifyChange(context);
                    }
            }).show();
    }

    // get the position which is selected before
    private int getSelectedPosition(String inputmodeKey, String[] modes) {
        String res = mSpref.getString(inputmodeKey, "Phone");
        MmsLog.d(TAG, "getSelectedPosition found the res = " + res);
        for (int i = 0; i < modes.length; i++) {
            if (res.equals(modes[i])) {
                MmsLog.d(TAG, "getSelectedPosition found the position = " + i);
                return i;
            }
        }
        MmsLog.d(TAG, "getSelectedPosition not found the position");

        return 0;
    }

    public void setServiceCenter(int id) {

        if (EncapsulatedFeatureOption.EVDO_DT_SUPPORT && isUSimType(id)) {
            showToast(R.string.cdma_not_support);
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            mNumberText = new EditText(dialog.getContext());
            mNumberText.setHint(R.string.type_to_compose_text_enter_to_send);
            mNumberText.setFilters(new InputFilter[] {
                new InputFilter.LengthFilter(MAX_EDITABLE_LENGTH)
            });
            // mNumberText.setKeyListener(new DigitsKeyListener(false, true));
            mNumberText.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_CLASS_PHONE);
            mNumberText.computeScroll();
            MmsLog.d(TAG, "currentSlot is: " + id);
            String scNumber = getServiceCenter(id);
            MmsLog.d(TAG, "getScNumber is: " + scNumber);
            mNumberText.setText(scNumber);
            mNumberTextDialog = dialog.setIcon(R.drawable.ic_dialog_info_holo_light)
                    .setTitle(R.string.sms_service_center).setView(mNumberText)
                    .setPositiveButton(R.string.OK, new PositiveButtonListener())
                    .setNegativeButton(R.string.Cancel, new NegativeButtonListener()).show();

        }

    }

    public void startManageSimMessages(int id) {
        if (mListSimInfo == null || id > mListSimInfo.size()) {
            MmsLog.e(TAG, "startManageSimMessages mListSimInfo is null ");
            return;
        }
        Intent it = new Intent();
        MmsLog.d(TAG, "currentSlot is: " + id);
        MmsLog.d(TAG, "currentSlot name is: " + mListSimInfo.get(0).getDisplayName());
        it.setClass(this, ManageSimMessages.class);
        it.putExtra("SlotId", id);
        startActivity(it);
    }

    private void startCellBroadcast(int num) {
        if (mListSimInfo == null || num > mListSimInfo.size()) {
            MmsLog.e(TAG, "startCellBroadcast mListSimInfo is null ");
            return;
        }
        // int slotId = listSimInfo.get(num).getSlot();

        if(EncapsulatedFeatureOption.EVDO_DT_SUPPORT & isUSimType(num)) {
            showToast(R.string.cdma_not_support);
        } else {

            Intent it = new Intent();
            MmsLog.i(TAG, "currentSlot is: " + num);
            MmsLog.i(TAG, "currentSlot name is: " + getSimName(num));
            it.setClassName("com.android.phone", "com.mediatek.settings.CellBroadcastActivity");
            it.setAction(Intent.ACTION_MAIN);
            it.putExtra(EncapsulatedPhone.GEMINI_SIM_ID_KEY, num);
            it.putExtra(SUB_TITLE_NAME, SIMInfo.getSIMInfoBySlot(this, num).getDisplayName());
            startActivity(it);

        }

    }

    public String getSimName(int id) {
        MmsLog.d(TAG, "getSimName" + id);
        for (SIMInfo simInfo : mListSimInfo) {
            if (simInfo.getSlot() == id) {
                return simInfo.getDisplayName();
            }
        }
        return "";
    }

    public String getSimNumber(int id) {
        for (SIMInfo simInfo : mListSimInfo) {
            if (simInfo.getSlot() == id) {
                return simInfo.getNumber();
            }
        }
        return "";
    }

    public int getSimColor(int id) {
        for (SIMInfo simInfo : mListSimInfo) {
            if (simInfo.getSlot() == id) {
                return simInfo.getSimBackgroundLightRes();
            }
        }
        return 0;
    }

    public int getNumberFormat(int id) {
        for (SIMInfo simInfo : mListSimInfo) {
            if (simInfo.getSlot() == id) {
                return simInfo.getDispalyNumberFormat();
            }
        }
        return 0;
    }

    public int getSimStatus(int id) {
        EncapsulatedTelephonyService teleService = EncapsulatedTelephonyService.getInstance();
        // int slotId = SIMInfo.getSlotById(this,mListSimInfo.get(id).mSimId);
        if (id != -1) {
            try {
                return teleService.getSimIndicatorStateGemini(id);
            } catch (RemoteException e) {
                MmsLog.e(TAG, "getSimIndicatorStateGemini is failed.\n" + e.toString());
                return -1;
            }
        } else {
            return -1;
        }
    }

    public boolean is3G(int id) {
        if (id == MessageUtils.get3GCapabilitySIM()) {
            return true;
        } else {
            return false;
        }
    }

    private String getServiceCenter(int id) {
        EncapsulatedTelephonyService teleService = EncapsulatedTelephonyService.getInstance();
        try {
            return teleService.getScAddressGemini(id);
        } catch (RemoteException e) {
            MmsLog.e(TAG, "getScAddressGemini is failed.\n" + e.toString());
            return null;
        }
    }

    private boolean setServiceCenter(String sCnumber, int id) {
        EncapsulatedTelephonyService teleService = EncapsulatedTelephonyService.getInstance();
        MmsLog.d(TAG, "setScAddressGemini is: " + sCnumber);
        try {
            teleService.setScAddressGemini(sCnumber, id);
            return true;
        } catch(RemoteException e1) {
            MmsLog.e(TAG,"setScAddressGemini is failed.\n" + e1.toString());
            return false;
        } catch(NullPointerException e2) {
            MmsLog.e(TAG,"setScAddressGemini is failed.\n" + e2.toString());
            return false;
        }
    }

    private void tostScOK() {
        Toast.makeText(this, R.string.set_service_center_OK, 0);
    }

    private void tostScFail() {
        Toast.makeText(this, R.string.set_service_center_fail, 0);
    }

    private class PositiveButtonListener implements OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            final EncapsulatedTelephonyService teleService = EncapsulatedTelephonyService.getInstance();
            String scNumber = mNumberText.getText().toString();
            MmsLog.d(TAG, "setScNumber is: " + scNumber);
            MmsLog.d(TAG, "mCurrentSim is: " + mCurrentSim);
            //setServiceCenter(scNumber, mCurrentSim);
            new Thread(new Runnable() {
                public void run() {
                    try {
                        teleService.setScAddressGemini(mNumberText.getText().toString(), mCurrentSim);
                    } catch(RemoteException e1) {
                        MmsLog.e(TAG,"setScAddressGemini is failed.\n" + e1.toString());
                    } catch(NullPointerException e2) {
                        MmsLog.e(TAG,"setScAddressGemini is failed.\n" + e2.toString());
                    }
                }
            }).start();
        }
    }

    private class NegativeButtonListener implements OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            // cancel
            dialog.dismiss();
        }
    }

    public boolean isUSimType(int slot) {
        /** M: MTK Encapsulation ITelephony */
        // final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
        EncapsulatedTelephonyService iTel = EncapsulatedTelephonyService.getInstance();
        if (iTel == null) {
            Log.d(TAG, "[isUIMType]: iTel = null");
            return false;
        }

        try {
            String type = iTel.getIccCardTypeGemini(slot);
            return iTel.getIccCardTypeGemini(slot).equals("UIM");
        } catch (RemoteException e) {
            Log.e(TAG, "[isUSIMType]: " + String.format("%s: %s", e.toString(), e.getMessage()));
        } catch (NullPointerException e) {
            Log.e(TAG, "[isUSIMType]: " + String.format("%s: %s", e.toString(), e.getMessage()));
        }

        return false;
    }

    private void showToast(int id) {
        Toast t = Toast.makeText(getApplicationContext(), getString(id), Toast.LENGTH_SHORT);
        t.show();
    }
    /// M: update sim state dynamically. @{
    private BroadcastReceiver mSimReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(TelephonyIntents.ACTION_SIM_INDICATOR_STATE_CHANGED)) {
                int slotId = intent.getIntExtra(TelephonyIntents.INTENT_KEY_ICC_SLOT, -1);
                if (slotId >= 0) {
                    updateSimState(slotId);
                }
                if (SIMInfo.getInsertedSIMCount(context) < 2) {
                    finish();
                }
                if(slotId == mCurrentSim){
                    if(mNumberTextDialog!= null && mNumberTextDialog.isShowing()){
                        mNumberTextDialog.dismiss();
                        Log.e(TAG,"mNumberTextDialog.dismiss()");
                    }
                }
            }
        }
    };

    private void updateSimState(int slotId) {
        AdvancedEditorPreference pref = null;
        if (mSim1 != null && slotId == mSim1.getSlotId()) {
            pref = mSim1;
        } else if (mSim2 != null && slotId == mSim2.getSlotId()) {
            pref = mSim2;
        } else if (mSim3 != null && slotId == 2) {
            pref = mSim3;
        } else if (mSim4 != null && slotId == 3) {
            pref = mSim4;
        }
        if (pref == null) {
            return;
        }
        pref.notifyChanged();
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(TelephonyIntents.ACTION_SIM_INDICATOR_STATE_CHANGED);
        this.registerReceiver(mSimReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        this.unregisterReceiver(mSimReceiver);
    }
    /// @}

    @Override
    public boolean onNavigateUp() {
        finish();
        return true;
    }
}
