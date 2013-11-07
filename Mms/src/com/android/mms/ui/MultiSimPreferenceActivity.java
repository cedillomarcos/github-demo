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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.preference.PreferenceActivity;
import android.util.Log;

import com.android.internal.telephony.TelephonyIntents;
import com.mediatek.encapsulation.com.android.internal.telephony.EncapsulatedTelephonyService;
import com.android.mms.R;
import com.android.mms.ui.AdvancedCheckBoxPreference.GetSimInfo;
import com.mediatek.encapsulation.com.mediatek.common.featureoption.EncapsulatedFeatureOption;
import com.mediatek.encapsulation.android.telephony.EncapsulatedTelephony.SIMInfo;
import java.util.List;

/** M:
 * MultiSimPreferenceActivity
 */
public class MultiSimPreferenceActivity extends PreferenceActivity implements GetSimInfo {
    private static final String TAG = "MultiSimPreferenceActivity";

    private AdvancedCheckBoxPreference mSim1;
    private AdvancedCheckBoxPreference mSim2;
    private AdvancedCheckBoxPreference mSim3;
    private AdvancedCheckBoxPreference mSim4;

    private int mSimCount;
    private List<SIMInfo> mListSimInfo;

    private int mSim1CurrentId;
    private int mSim2CurrentId;

    private int mTitleId = 0;


    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        mListSimInfo = SIMInfo.getInsertedSIMList(this);
        mSimCount = mListSimInfo.size();

        addPreferencesFromResource(R.xml.multicardselection);    
        Intent intent = getIntent();
        String preference = intent.getStringExtra("preference");
        //translate key to SIM-related key;
        Log.i("MultiSimPreferenceActivity, getIntent:", intent.toString());
        Log.i("MultiSimPreferenceActivity, getpreference:", preference);
        mTitleId = intent.getIntExtra("preferenceTitleId",0);
        if (mTitleId != 0) {
            setTitle(getString(mTitleId));
        }
        changeMultiCardKeyToSimRelated(preference);
        IntentFilter filter = new IntentFilter(TelephonyIntents.ACTION_SIM_INDICATOR_STATE_CHANGED);
        registerReceiver(mSimReceiver, filter);

    }

    protected void onResume() {
        super.onResume();
        mListSimInfo = SIMInfo.getInsertedSIMList(this);
        if (mSim1 != null) {
            mSim1.setNotifyChange(this);
        }
        if (mSim2 != null) {
            mSim2.setNotifyChange(this);
        }
    }

    private void changeMultiCardKeyToSimRelated(String preference) {
        mSim1 = (AdvancedCheckBoxPreference) findPreference("pref_key_sim1");
        mSim2 = (AdvancedCheckBoxPreference) findPreference("pref_key_sim2");
        if (mListSimInfo != null && mListSimInfo.get(0).getSlot() == 0) {
            mSim1CurrentId = 0;
            mSim2CurrentId = 1;
        } else {
            mSim1CurrentId = 1;
            mSim2CurrentId = 0;
        }
        Log.d(TAG, "changeMultiCardKeyToSimRelated mSim1CurrentId: " + mSim1CurrentId);
        mSim1.init(this, mSim1CurrentId);
        Log.d(TAG, "changeMultiCardKeyToSimRelated mSim2CurrentId: " + mSim2CurrentId);
        mSim2.init(this, mSim2CurrentId);
        mSim3 = (AdvancedCheckBoxPreference) findPreference("pref_key_sim3");
        mSim3.init(this, 2);
        mSim4 = (AdvancedCheckBoxPreference) findPreference("pref_key_sim4");
        mSim4.init(this, 3);
        //get the stored value
        SharedPreferences sp = getSharedPreferences("com.android.mms_preferences", MODE_WORLD_READABLE);

        if (mSimCount == 1) {
            getPreferenceScreen().removePreference(mSim2);
            getPreferenceScreen().removePreference(mSim3);
            getPreferenceScreen().removePreference(mSim4);
            mSim1.setKey(Long.toString(mListSimInfo.get(0).getSimId()) + "_" + preference);
            if (preference.equals(MmsPreferenceActivity.RETRIEVAL_DURING_ROAMING)) {
                mSim1.setEnabled(sp.getBoolean(Long.toString(mListSimInfo.get(0).getSimId())
                    + "_" + MmsPreferenceActivity.AUTO_RETRIEVAL, true));
            }
        } else if (mSimCount == 2) {
            getPreferenceScreen().removePreference(mSim3);
            getPreferenceScreen().removePreference(mSim4);
            
            mSim1.setKey(Long.toString(mListSimInfo.get(mSim1CurrentId).getSimId()) + "_" + preference);
            mSim2.setKey(Long.toString(mListSimInfo.get(mSim2CurrentId).getSimId()) + "_" + preference);
            if (preference.equals(MmsPreferenceActivity.RETRIEVAL_DURING_ROAMING)) {
                /// M: fix bug ALPS00438131, use mSim1/2CurrentId to update IsEnable, not 0/1,
                /// because mListSimInfo.get(0).getSlot() != 0 in some phones
                mSim1.setEnabled(sp.getBoolean(Long.toString(mListSimInfo.get(mSim1CurrentId).getSimId())
                    + "_" + MmsPreferenceActivity.AUTO_RETRIEVAL, true));
                mSim2.setEnabled(sp.getBoolean(Long.toString(mListSimInfo.get(mSim2CurrentId).getSimId())
                    + "_" + MmsPreferenceActivity.AUTO_RETRIEVAL, true));
            }
        } else if (mSimCount == 3) {
            getPreferenceScreen().removePreference(mSim4);
        
            mSim1.setKey(Long.toString(mListSimInfo.get(0).getSimId()) + "_" + preference);
            mSim2.setKey(Long.toString(mListSimInfo.get(1).getSimId()) + "_" + preference);
            mSim3.setKey(Long.toString(mListSimInfo.get(2).getSimId()) + "_" + preference);
            if (preference.equals(MmsPreferenceActivity.RETRIEVAL_DURING_ROAMING)) {
                mSim1.setEnabled(sp.getBoolean(Long.toString(mListSimInfo.get(0).getSimId())
                    + "_" + MmsPreferenceActivity.AUTO_RETRIEVAL, true));
                mSim2.setEnabled(sp.getBoolean(Long.toString(mListSimInfo.get(1).getSimId())
                    + "_" + MmsPreferenceActivity.AUTO_RETRIEVAL, true));
                mSim3.setEnabled(sp.getBoolean(Long.toString(mListSimInfo.get(2).getSimId())
                    + "_" + MmsPreferenceActivity.AUTO_RETRIEVAL, true));
            }
        } else {

            mSim1.setKey(Long.toString(mListSimInfo.get(0).getSimId()) + "_" + preference);
            mSim2.setKey(Long.toString(mListSimInfo.get(1).getSimId()) + "_" + preference);
            mSim3.setKey(Long.toString(mListSimInfo.get(2).getSimId()) + "_" + preference);
            mSim4.setKey(Long.toString(mListSimInfo.get(3).getSimId()) + "_" + preference);
            if (preference.equals(MmsPreferenceActivity.RETRIEVAL_DURING_ROAMING)) {
                mSim1.setEnabled(sp.getBoolean(Long.toString(mListSimInfo.get(0).getSimId())
                    + "_" + MmsPreferenceActivity.AUTO_RETRIEVAL, true));
                mSim2.setEnabled(sp.getBoolean(Long.toString(mListSimInfo.get(1).getSimId())
                    + "_" + MmsPreferenceActivity.AUTO_RETRIEVAL, true));
                mSim3.setEnabled(sp.getBoolean(Long.toString(mListSimInfo.get(2).getSimId())
                    + "_" + MmsPreferenceActivity.AUTO_RETRIEVAL, true));
                mSim4.setEnabled(sp.getBoolean(Long.toString(mListSimInfo.get(3).getSimId())
                    + "_" + MmsPreferenceActivity.AUTO_RETRIEVAL, true));
            }
        }

            if (preference.equals(MmsPreferenceActivity.AUTO_RETRIEVAL)) {
            mSim1.setChecked(sp.getBoolean(mSim1.getKey(), true));
        } else {
            mSim1.setChecked(sp.getBoolean(mSim1.getKey(), false));
        }

        if (mSim2 != null) {
            if (preference.equals(MmsPreferenceActivity.AUTO_RETRIEVAL)) {
                mSim2.setChecked(sp.getBoolean(mSim2.getKey(), true));
            }  else {
                mSim2.setChecked(sp.getBoolean(mSim2.getKey(), false));
            }
        }
        if (mSim3 != null) {
            if (preference.equals(MmsPreferenceActivity.AUTO_RETRIEVAL)) {
                mSim3.setChecked(sp.getBoolean(mSim3.getKey(), true));
            } else {
                mSim3.setChecked(sp.getBoolean(mSim3.getKey(), false));
            }
        }
        if (mSim4 != null) {
            if (preference.equals(MmsPreferenceActivity.AUTO_RETRIEVAL)) {
                mSim4.setChecked(sp.getBoolean(mSim4.getKey(), true));
            } else {
                mSim4.setChecked(sp.getBoolean(mSim4.getKey(), false));
            }
        }

        if (mSim1 != null) {
            if (preference.equals(MmsPreferenceActivity.READ_REPORT_MODE) 
                    || preference.equals(MmsPreferenceActivity.READ_REPORT_AUTO_REPLY)) {
                if (EncapsulatedFeatureOption.EVDO_DT_SUPPORT && isUSimType(mListSimInfo.get(mSim1CurrentId).getSlot())) {
                    mSim1.setEnabled(false);
                }
            }
        }
        if (mSim2 != null) {
            if (preference.equals(MmsPreferenceActivity.READ_REPORT_MODE)
                    || preference.equals(MmsPreferenceActivity.READ_REPORT_AUTO_REPLY)) {
                if (EncapsulatedFeatureOption.EVDO_DT_SUPPORT && isUSimType(mListSimInfo.get(mSim2CurrentId).getSlot())) {
                    mSim2.setEnabled(false);
                }
            }
        }
    }

    public String getSimName(int id) {
        return mListSimInfo.get(id).getDisplayName();
    }

    public String getSimNumber(int id) {
        return mListSimInfo.get(id).getNumber();
    }

    public int getSimColor(int id) {
        return mListSimInfo.get(id).getSimBackgroundLightRes();
    }

    public int getNumberFormat(int id) {
        return mListSimInfo.get(id).getDispalyNumberFormat();
    }

    public int getSimStatus(int id) {
        EncapsulatedTelephonyService teleService = EncapsulatedTelephonyService.getInstance();
        //int slotId = SIMInfo.getSlotById(this,mListSimInfo.get(id).mSimId);
        int slotId = mListSimInfo.get(id).getSlot();
        if (slotId != -1) {
            try {
                return teleService.getSimIndicatorStateGemini(slotId);
            } catch (RemoteException e) {
                Log.e(TAG, "getSimIndicatorStateGemini is failed.\n" + e.toString());
                return -1;
            }
        }
        return -1;
    }

    public boolean is3G(int id) {
        // int slotId = SIMInfo.getSlotById(this, listSimInfo.get(id).mSimId);
        int slotId = mListSimInfo.get(id).getSlot();
        Log.i(TAG, "SIMInfo.getSlotById id: " + id + " slotId: " + slotId);
        if (slotId == MessageUtils.get3GCapabilitySIM()) {
            return true;
        }
        return false;
    }

    public boolean isUSimType(int slot) {
        /** M: MTK Encapsulation ITelephony */
        // final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager
        //         .getService(Context.TELEPHONY_SERVICE));
        EncapsulatedTelephonyService iTel = EncapsulatedTelephonyService.getInstance();
        if (iTel == null) {
            Log.d(TAG, "[isUIMType]: iTel = null");
            return false;
        }

        try {
            return iTel.getIccCardTypeGemini(slot).equals("UIM");
        } catch (RemoteException e) {
            Log.e(TAG, "[isUIMType]: " + String.format("%s: %s", e.toString(), e.getMessage()));
        } catch (NullPointerException e) {
            Log.e(TAG, "[isUIMType]: " + String.format("%s: %s", e.toString(), e.getMessage()));
        }

        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSimReceiver != null) {
            unregisterReceiver(mSimReceiver);
        }
    }

    /// M: update sim state dynamically. @{
    private BroadcastReceiver mSimReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(TelephonyIntents.ACTION_SIM_INDICATOR_STATE_CHANGED)) {
                if (SIMInfo.getInsertedSIMCount(context) < 2) {
                     finish();
                }
            }
        }
    };

    @Override
    public boolean onNavigateUp() {
        finish();
        return true;
    }
}
