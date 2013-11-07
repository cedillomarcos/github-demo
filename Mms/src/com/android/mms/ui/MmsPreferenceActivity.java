/*
 * Copyright (C) 2007-2008 Esmertec AG.
 * Copyright (C) 2007-2008 The Android Open Source Project
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.mms.ui;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.SharedPreferences;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.ServiceManager;

import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.SearchRecentSuggestions;
import com.mediatek.encapsulation.android.telephony.EncapsulatedTelephony.SIMInfo;
import android.provider.Telephony.Sms;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;

import android.text.InputFilter;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.android.internal.telephony.ITelephony;
import com.android.mms.data.WorkingMessage;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.R;
import com.android.mms.util.Recycler;
import com.android.internal.telephony.Phone;
import com.mediatek.encapsulation.com.android.internal.telephony.EncapsulatedPhone;
import com.mediatek.encapsulation.com.mediatek.common.featureoption.EncapsulatedFeatureOption;
import com.mediatek.telephony.TelephonyManagerEx;
import com.mediatek.encapsulation.MmsLog;
import com.android.internal.telephony.TelephonyIntents;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * With this activity, users can set preferences for MMS and SMS and
 * can access and manipulate SMS messages stored on the SIM.
 */
public class MmsPreferenceActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "MmsPreferenceActivity";

    private static final boolean DEBUG = false;

    // Symbolic names for the keys used for preference lookup
    public static final String MMS_DELIVERY_REPORT_MODE = "pref_key_mms_delivery_reports";

    public static final String EXPIRY_TIME = "pref_key_mms_expiry";

    public static final String PRIORITY = "pref_key_mms_priority";

    public static final String READ_REPORT_MODE = "pref_key_mms_read_reports";

    public static final String MMS_SIZE_LIMIT = "pref_key_mms_size_limit";

    // M: add this for read report
    public static final String READ_REPORT_AUTO_REPLY = "pref_key_mms_auto_reply_read_reports";

    public static final String AUTO_RETRIEVAL = "pref_key_mms_auto_retrieval";

    public static final String RETRIEVAL_DURING_ROAMING = "pref_key_mms_retrieval_during_roaming";

    public static final String CREATION_MODE = "pref_key_mms_creation_mode";

    public static final String MMS_ENABLE_TO_SEND_DELIVERY_REPORT = "pref_key_mms_enable_to_send_delivery_reports";

    public static final String MMS_SETTINGS = "pref_key_mms_settings";
    // Menu entries
    private static final int MENU_RESTORE_DEFAULTS = 1;

    /// M: google jb.mr1 patch, add for group mms
    public static final String GROUP_MMS_MODE = "pref_key_mms_group_mms";

    private static final String sUim = "UIM";

    private final int MAX_EDITABLE_LENGTH = 20;

    // all preferences need change key for single sim card
    private CheckBoxPreference mMmsDeliveryReport;

    private CheckBoxPreference mMmsEnableToSendDeliveryReport;

    private CheckBoxPreference mMmsReadReport;

    // M: add this for read report
    private CheckBoxPreference mMmsAutoReplyReadReport;

    private CheckBoxPreference mMmsAutoRetrieval;

    private CheckBoxPreference mMmsRetrievalDuringRoaming;

    // M: google jb.mr1 patch, add for group mms
    private CheckBoxPreference mMmsGroupMms;

    // all preferences need change key for multiple sim card
    private Preference mMmsDeliveryReportMultiSim;

    private Preference mMmsEnableToSendDeliveryReportMultiSim;

    private Preference mMmsReadReportMultiSim;

    // M: add this for read report
    private Preference mMmsAutoReplyReadReportMultiSim;

    private Preference mMmsAutoRetrievalMultiSim;

    private Preference mMmsRetrievalDuringRoamingMultiSim;

    private ListPreference mMmsPriority;

    private ListPreference mMmsCreationMode;

    private ListPreference mMmsSizeLimit;

    private static final String PRIORITY_HIGH = "High";

    private static final String PRIORITY_LOW = "Low";

    private static final String PRIORITY_NORMAL = "Normal";

    private static final String LOCATION_PHONE = "Phone";

    private static final String LOCATION_SIM = "Sim";

    private static final String CREATION_MODE_RESTRICTED = "RESTRICTED";

    private static final String CREATION_MODE_WARNING = "WARNING";

    private static final String CREATION_MODE_FREE = "FREE";

    private static final String SIZE_LIMIT_100 = "100";

    private static final String SIZE_LIMIT_200 = "200";

    private static final String SIZE_LIMIT_300 = "300";

    private Handler mSMSHandler = new Handler();

    private Handler mMMSHandler = new Handler();

    private EditText mNumberText;

    private AlertDialog mNumberTextDialog;

    private List<SIMInfo> listSimInfo;

    private TelephonyManagerEx mTelephonyManager;

    int slotId;

    private EditText inputNumber;

    /* import or export SD card */
    private ProgressDialog progressdialog = null;

    private int currentSimCount = 0;

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setListPrefSummary();
    }

    private void setListPrefSummary() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        // For mMmsPriority;
        String stored = sp.getString(PRIORITY, getString(R.string.priority_normal));
        mMmsPriority.setSummary(getVisualTextName(stored, R.array.pref_key_mms_priority_choices,
            R.array.pref_key_mms_priority_values));
        // For mMmsCreationMode
        stored = sp.getString(CREATION_MODE, CREATION_MODE_FREE);
        mMmsCreationMode.setSummary(getVisualTextName(stored, R.array.pref_mms_creation_mode_choices,
            R.array.pref_mms_creation_mode_values));
        // For mMmsSizeLimit
        stored = sp.getString(MMS_SIZE_LIMIT, SIZE_LIMIT_300);
        mMmsSizeLimit.setSummary(getVisualTextName(stored, R.array.pref_mms_size_limit_choices,
            R.array.pref_mms_size_limit_values));
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        MmsLog.d(TAG, "onCreate");
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(getResources().getString(R.string.actionbar_mms_setting));
        actionBar.setDisplayHomeAsUpEnabled(true);
        setMessagePreferences();
    }

    private void setMessagePreferences() {
        if (EncapsulatedFeatureOption.MTK_GEMINI_SUPPORT == true) {
            MmsLog.d(TAG, "MTK_GEMINI_SUPPORT is true");
            currentSimCount = SIMInfo.getInsertedSIMCount(this);
            MmsLog.d(TAG, "currentSimCount is :" + currentSimCount);
            /// M: fix bug ALPS00421364       
            if (currentSimCount == 0) {
                addPreferencesFromResource(R.xml.mmspreferences);
                if (MmsConfig.getDeliveryReportAllowed()) {
                    mMmsEnableToSendDeliveryReport = (CheckBoxPreference) findPreference(MMS_ENABLE_TO_SEND_DELIVERY_REPORT);
                    mMmsEnableToSendDeliveryReport.setEnabled(false);
                } else {
                    mMmsEnableToSendDeliveryReport = (CheckBoxPreference) findPreference(MMS_ENABLE_TO_SEND_DELIVERY_REPORT);
                    PreferenceCategory mmsCategory = (PreferenceCategory) findPreference("pref_key_mms_settings");
                    mmsCategory.removePreference(mMmsEnableToSendDeliveryReport);
                }
				//GPBYY-85 liyang 20130829 delete start
                //mMmsDeliveryReport = (CheckBoxPreference) findPreference(MMS_DELIVERY_REPORT_MODE);
                //mMmsDeliveryReport.setEnabled(false);
				//GPBYY-85 liyang 20130829 delete end
                mMmsReadReport = (CheckBoxPreference) findPreference(READ_REPORT_MODE);
                mMmsReadReport.setEnabled(false);
                mMmsAutoReplyReadReport = (CheckBoxPreference) findPreference(READ_REPORT_AUTO_REPLY);
                mMmsAutoReplyReadReport.setEnabled(false);
                mMmsAutoRetrieval = (CheckBoxPreference) findPreference(AUTO_RETRIEVAL);
                mMmsAutoRetrieval.setEnabled(false);
                mMmsRetrievalDuringRoaming = (CheckBoxPreference) findPreference(RETRIEVAL_DURING_ROAMING);
                mMmsRetrievalDuringRoaming.setEnabled(false);
            /// @}
            } else if (currentSimCount == 1) {
                addPreferencesFromResource(R.xml.mmspreferences);
                if (MmsConfig.getDeliveryReportAllowed()) {
                    mMmsEnableToSendDeliveryReport = (CheckBoxPreference) findPreference(MMS_ENABLE_TO_SEND_DELIVERY_REPORT);
                } else {
                    mMmsEnableToSendDeliveryReport = (CheckBoxPreference) findPreference(MMS_ENABLE_TO_SEND_DELIVERY_REPORT);
                    PreferenceCategory mmsCategory = (PreferenceCategory) findPreference("pref_key_mms_settings");
                    mmsCategory.removePreference(mMmsEnableToSendDeliveryReport);
                }
            } else {
                addPreferencesFromResource(R.xml.mmsmulticardpreferences);
            }
        } else {
            addPreferencesFromResource(R.xml.mmspreferences);
            if (MmsConfig.getDeliveryReportAllowed()) {
                mMmsEnableToSendDeliveryReport = (CheckBoxPreference) findPreference(MMS_ENABLE_TO_SEND_DELIVERY_REPORT);
            } else {
                mMmsEnableToSendDeliveryReport = (CheckBoxPreference) findPreference(MMS_ENABLE_TO_SEND_DELIVERY_REPORT);
                PreferenceCategory mmsCategory = (PreferenceCategory) findPreference("pref_key_mms_settings");
                mmsCategory.removePreference(mMmsEnableToSendDeliveryReport);
           }
        }
        // M: add for read report
        if (EncapsulatedFeatureOption.MTK_SEND_RR_SUPPORT == false) {
            // remove read report entry
            MmsLog.d(MmsApp.TXN_TAG, "remove the read report entry, it should be hidden.");
            PreferenceCategory mmOptions = (PreferenceCategory) findPreference(MMS_SETTINGS);
            mmOptions.removePreference(findPreference(READ_REPORT_AUTO_REPLY));
        }
        // M: google jb.mr1 patch, add for group mms
        if (!MmsConfig.getGroupMmsEnabled()) {
            // remove group mms entry
            MmsLog.d(MmsApp.TXN_TAG, "remove the group mms entry, it should be hidden.");
            PreferenceCategory mmOptions = (PreferenceCategory) findPreference(MMS_SETTINGS);
            mmOptions.removePreference(findPreference(GROUP_MMS_MODE));
        }
        mMmsPriority = (ListPreference) findPreference(PRIORITY);
        mMmsPriority.setOnPreferenceChangeListener(this);
        mMmsCreationMode = (ListPreference) findPreference(CREATION_MODE);
        mMmsCreationMode.setOnPreferenceChangeListener(this);
        mMmsSizeLimit = (ListPreference) findPreference(MMS_SIZE_LIMIT);
        mMmsSizeLimit.setOnPreferenceChangeListener(this);
        if (EncapsulatedFeatureOption.MTK_GEMINI_SUPPORT == true) {
        } else {
            if (!MmsApp.getApplication().getTelephonyManager().hasIccCard()) {
            } else {
                listSimInfo = SIMInfo.getInsertedSIMList(this);
                mMmsReadReport = (CheckBoxPreference) findPreference(READ_REPORT_MODE);
                mMmsAutoReplyReadReport = (CheckBoxPreference) findPreference(READ_REPORT_AUTO_REPLY);
                if (EncapsulatedFeatureOption.EVDO_DT_SUPPORT == true && isUSimType(listSimInfo.get(0).getSlot())) {
                    mMmsAutoReplyReadReport.setEnabled(false);
                    mMmsReadReport.setEnabled(false);
                }
            }
        }
        if (!MmsConfig.getMmsEnabled()) {
            // No Mms, remove all the mms-related preferences
            PreferenceCategory mmsOptions = (PreferenceCategory) findPreference(MMS_SETTINGS);
            getPreferenceScreen().removePreference(mmsOptions);
        }
        // Change the key to the SIM-related key, if has one SIM card, else set default value.
        if (EncapsulatedFeatureOption.MTK_GEMINI_SUPPORT == true) {
            MmsLog.d(TAG, "MTK_GEMINI_SUPPORT is true");
            if (currentSimCount == 1) {
                MmsLog.d(TAG, "single sim");
                changeSingleCardKeyToSimRelated();
            } else if (currentSimCount > 1) {
                setMultiCardPreference();
            }
        }
    }

    private void changeSingleCardKeyToSimRelated() {
        // get to know which one
        listSimInfo = SIMInfo.getInsertedSIMList(this);
        SIMInfo singleCardInfo = null;
        if (listSimInfo.size() != 0) {
            singleCardInfo = listSimInfo.get(0);
        }
        if (singleCardInfo == null) {
            return;
        }
        Long simId = listSimInfo.get(0).getSimId();
        MmsLog.d(TAG, "changeSingleCardKeyToSimRelated Got simId = " + simId);
        // translate all key to SIM-related key;
		//GPBYY-85 liyang 20130829 delete start
        //mMmsDeliveryReport = (CheckBoxPreference) findPreference(MMS_DELIVERY_REPORT_MODE);
		//GPBYY-85 liyang 20130829 delete end
        mMmsReadReport = (CheckBoxPreference) findPreference(READ_REPORT_MODE);
        // M: add this for read report
        mMmsAutoReplyReadReport = (CheckBoxPreference) findPreference(READ_REPORT_AUTO_REPLY);
        if (EncapsulatedFeatureOption.EVDO_DT_SUPPORT == true && isUSimType(listSimInfo.get(0).getSlot())) {
            mMmsAutoReplyReadReport.setEnabled(false);
            mMmsReadReport.setEnabled(false);
        }
        mMmsAutoRetrieval = (CheckBoxPreference) findPreference(AUTO_RETRIEVAL);
        mMmsRetrievalDuringRoaming = (CheckBoxPreference) findPreference(RETRIEVAL_DURING_ROAMING);
		//GPBYY-85 liyang 20130829 delete start
        //mMmsDeliveryReport.setKey(Long.toString(simId) + "_" + MMS_DELIVERY_REPORT_MODE);
		//GPBYY-85 liyang 20130829 delete end
        mMmsReadReport.setKey(Long.toString(simId) + "_" + READ_REPORT_MODE);
        // M: add this for read report
        if (mMmsAutoReplyReadReport != null) {
            mMmsAutoReplyReadReport.setKey(Long.toString(simId) + "_" + READ_REPORT_AUTO_REPLY);
        }
        // M: google jb.mr1 patch, add for group mms
        mMmsGroupMms = (CheckBoxPreference) findPreference(GROUP_MMS_MODE);
        if (mMmsGroupMms != null) {
            mMmsGroupMms.setKey(GROUP_MMS_MODE);
        }
        mMmsAutoRetrieval.setKey(Long.toString(simId) + "_" + AUTO_RETRIEVAL);
        mMmsRetrievalDuringRoaming.setDependency(Long.toString(simId) + "_" + AUTO_RETRIEVAL);
        mMmsRetrievalDuringRoaming.setKey(Long.toString(simId) + "_" + RETRIEVAL_DURING_ROAMING);
        
        if (MmsConfig.getDeliveryReportAllowed()) {
            mMmsEnableToSendDeliveryReport = (CheckBoxPreference) findPreference(MMS_ENABLE_TO_SEND_DELIVERY_REPORT);
            mMmsEnableToSendDeliveryReport.setKey(Long.toString(simId) + "_" + MMS_ENABLE_TO_SEND_DELIVERY_REPORT);
        } else {
            mMmsEnableToSendDeliveryReport = (CheckBoxPreference) findPreference(MMS_ENABLE_TO_SEND_DELIVERY_REPORT);
            if (mMmsEnableToSendDeliveryReport != null) {
                mMmsEnableToSendDeliveryReport.setKey(Long.toString(simId) + "_" + MMS_ENABLE_TO_SEND_DELIVERY_REPORT);
                PreferenceCategory mmsCategory = (PreferenceCategory)findPreference("pref_key_mms_settings");
                mmsCategory.removePreference(mMmsEnableToSendDeliveryReport);
            }
        }
        // get the stored value
        SharedPreferences sp = getSharedPreferences("com.android.mms_preferences", MODE_WORLD_READABLE);
        if (mMmsDeliveryReport != null) {
            mMmsDeliveryReport.setChecked(sp.getBoolean(mMmsDeliveryReport.getKey(), false));
        }
        if (mMmsEnableToSendDeliveryReport != null) {
            mMmsEnableToSendDeliveryReport.setChecked(sp.getBoolean(mMmsEnableToSendDeliveryReport.getKey(), false));
        }
        if (mMmsReadReport != null) {
            mMmsReadReport.setChecked(sp.getBoolean(mMmsReadReport.getKey(), false));
        }
        // M: add for read report
        if (mMmsAutoReplyReadReport != null) {
            mMmsAutoReplyReadReport.setChecked(sp.getBoolean(mMmsAutoReplyReadReport.getKey(), false));
        }
        if (mMmsAutoRetrieval != null) {
            mMmsAutoRetrieval.setChecked(sp.getBoolean(mMmsAutoRetrieval.getKey(), true));
        }
        if (mMmsRetrievalDuringRoaming != null) {
            mMmsRetrievalDuringRoaming.setChecked(sp.getBoolean(mMmsRetrievalDuringRoaming.getKey(), false));
        }
        // M: google jb.mr1 patch, add for group mms
        if (mMmsGroupMms != null) {
            mMmsGroupMms.setChecked(sp.getBoolean(mMmsGroupMms.getKey(), false));
        }
    }

    private void setMultiCardPreference() {
        mMmsDeliveryReportMultiSim = findPreference(MMS_DELIVERY_REPORT_MODE);
        
        if (MmsConfig.getDeliveryReportAllowed()) {
            mMmsEnableToSendDeliveryReportMultiSim = findPreference(MMS_ENABLE_TO_SEND_DELIVERY_REPORT);
        } else {
            mMmsEnableToSendDeliveryReportMultiSim = findPreference(MMS_ENABLE_TO_SEND_DELIVERY_REPORT);
            PreferenceCategory mmsCategory =
                (PreferenceCategory)findPreference("pref_key_mms_settings");
            mmsCategory.removePreference(mMmsEnableToSendDeliveryReportMultiSim);
        }
        mMmsReadReportMultiSim = findPreference(READ_REPORT_MODE);
        // M: add this for read report
        mMmsAutoReplyReadReportMultiSim = findPreference(READ_REPORT_AUTO_REPLY);
        mMmsAutoRetrievalMultiSim = findPreference(AUTO_RETRIEVAL);
        mMmsRetrievalDuringRoamingMultiSim = findPreference(RETRIEVAL_DURING_ROAMING);
        // M: google jb.mr1 patch, add for group mms
        // get the stored value
        SharedPreferences sp = getSharedPreferences("com.android.mms_preferences", MODE_WORLD_READABLE);
        mMmsGroupMms = (CheckBoxPreference) findPreference(GROUP_MMS_MODE);
        if (mMmsGroupMms != null) {
            mMmsGroupMms.setKey(GROUP_MMS_MODE);
        }
        if (mMmsGroupMms != null) {
            mMmsGroupMms.setChecked(sp.getBoolean(mMmsGroupMms.getKey(), false));
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.clear();
        menu.add(0, MENU_RESTORE_DEFAULTS, 0, R.string.restore_default);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_RESTORE_DEFAULTS:
            restoreDefaultPreferences();
            return true;
        case android.R.id.home:
            // The user clicked on the Messaging icon in the action bar. Take them back from
            // wherever they came from
            finish();
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mMmsDeliveryReportMultiSim || preference == mMmsEnableToSendDeliveryReportMultiSim
            || preference == mMmsReadReportMultiSim
            // M: add this for read report
            || preference == mMmsAutoReplyReadReportMultiSim || preference == mMmsAutoRetrievalMultiSim
            || preference == mMmsRetrievalDuringRoamingMultiSim) {
            Intent it = new Intent();
            it.setClass(this, MultiSimPreferenceActivity.class);
            it.putExtra("preference", preference.getKey());
            if (preference == mMmsDeliveryReportMultiSim) {
                it.putExtra("preferenceTitleId", R.string.pref_title_mms_delivery_reports);
            } else if (preference == mMmsEnableToSendDeliveryReportMultiSim) {
                it.putExtra("preferenceTitleId",R.string.pref_title_mms_enable_to_send_delivery_reports);
            } else if (preference == mMmsReadReportMultiSim) {
                it.putExtra("preferenceTitleId", R.string.pref_title_mms_read_reports);
            } else if (preference == mMmsAutoReplyReadReportMultiSim) {
                it.putExtra("preferenceTitleId", R.string.pref_title_mms_auto_reply_read_reports);
            } else if (preference == mMmsAutoRetrievalMultiSim) {
                it.putExtra("preferenceTitleId", R.string.pref_title_mms_auto_retrieval);
            } else if (preference == mMmsRetrievalDuringRoamingMultiSim) {
                it.putExtra("preferenceTitleId", R.string.pref_title_mms_retrieval_during_roaming);
            }
            startActivity(it);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void restoreDefaultPreferences() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MmsPreferenceActivity.this)
                .edit();
        if (EncapsulatedFeatureOption.MTK_GEMINI_SUPPORT == true) {
            listSimInfo = SIMInfo.getInsertedSIMList(this);
            if (listSimInfo != null) {
                int simCount = listSimInfo.size();
                if (simCount > 0) {
                    for (int i = 0; i < simCount; i++) {
                        Long simId = listSimInfo.get(i).getSimId();
                        editor.putBoolean(Long.toString(simId) + "_" + MMS_DELIVERY_REPORT_MODE, false);
                        editor.putBoolean(Long.toString(simId) + "_" + MMS_ENABLE_TO_SEND_DELIVERY_REPORT, false);
                        editor.putBoolean(Long.toString(simId) + "_" + READ_REPORT_MODE, false);
                        editor.putBoolean(Long.toString(simId) + "_" + READ_REPORT_AUTO_REPLY, false);
                        editor.putBoolean(Long.toString(simId) + "_" + AUTO_RETRIEVAL, true);
                        editor.putBoolean(Long.toString(simId) + "_" + RETRIEVAL_DURING_ROAMING, false);
                    }
                }
            }
        } else {
            editor.putBoolean(MMS_DELIVERY_REPORT_MODE, false);
            editor.putBoolean(MMS_ENABLE_TO_SEND_DELIVERY_REPORT, false);
            editor.putBoolean(READ_REPORT_MODE, false);
            editor.putBoolean(READ_REPORT_AUTO_REPLY, false);
            editor.putBoolean(AUTO_RETRIEVAL, true);
            editor.putBoolean(RETRIEVAL_DURING_ROAMING, false);
        }

        editor.putString(CREATION_MODE, CREATION_MODE_FREE);
        editor.putString(MMS_SIZE_LIMIT, SIZE_LIMIT_300);
        editor.putString(PRIORITY, PRIORITY_NORMAL);
        /// M: fix bug ALPS00432361, restore default preferences
        /// about GroupMms and ShowEmailAddress @{
        editor.putBoolean(GROUP_MMS_MODE, false);
        /// @}
        editor.apply();
        setPreferenceScreen(null);
        setMessagePreferences();
        setListPrefSummary();
    }

    @Override
    public boolean onPreferenceChange(Preference arg0, Object arg1) {
        final String key = arg0.getKey();
        int slotId = 0;
        if (EncapsulatedFeatureOption.MTK_GEMINI_SUPPORT == true && MmsConfig.getSmsMultiSaveLocationEnabled()) {
            int currentSimCount = SIMInfo.getInsertedSIMCount(this);
            if (currentSimCount == 1) {
                slotId = SIMInfo.getInsertedSIMList(this).get(0).getSlot();
            }
        }
        String stored = (String) arg1;
        if (PRIORITY.equals(key)) {
            mMmsPriority.setSummary(getVisualTextName(stored, R.array.pref_key_mms_priority_choices,
                R.array.pref_key_mms_priority_values));
        } else if (CREATION_MODE.equals(key)) {
            mMmsCreationMode.setSummary(getVisualTextName(stored, R.array.pref_mms_creation_mode_choices,
                R.array.pref_mms_creation_mode_values));
            mMmsCreationMode.setValue(stored);
            WorkingMessage.updateCreationMode(this);
        } else if (MMS_SIZE_LIMIT.equals(key)) {
            mMmsSizeLimit.setSummary(getVisualTextName(stored, R.array.pref_mms_size_limit_choices,
                R.array.pref_mms_size_limit_values));
            MmsConfig.setUserSetMmsSizeLimit(Integer.valueOf(stored));
        }
        return true;
    }

    private CharSequence getVisualTextName(String enumName, int choiceNameResId, int choiceValueResId) {
        CharSequence[] visualNames = getResources().getTextArray(choiceNameResId);
        CharSequence[] enumNames = getResources().getTextArray(choiceValueResId);
        // Sanity check
        if (visualNames.length != enumNames.length) {
            return "";
        }
        for (int i = 0; i < enumNames.length; i++) {
            if (enumNames[i].equals(enumName)) {
                return visualNames[i];
            }
        }
        return "";
    }

    private void showToast(int id) {
        Toast t = Toast.makeText(getApplicationContext(), getString(id), Toast.LENGTH_SHORT);
        t.show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        MmsLog.d(TAG, "onConfigurationChanged: newConfig = " + newConfig + ",this = " + this);
        super.onConfigurationChanged(newConfig);
        this.getListView().clearScrapViewsIfNeeded();
    }

    public boolean isUSimType(int slot) {
        final ITelephony iTel = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
        if (iTel == null) {
            Log.d(TAG, "[isUIMType]: iTel = null");
            return false;
        }
        try {
            if (EncapsulatedFeatureOption.MTK_GEMINI_SUPPORT) {
                return iTel.getIccCardTypeGemini(slot).equals(sUim);
            } else {
                return iTel.getIccCardType().equals(sUim);
            }
        } catch (Exception e) {
            Log.e(TAG, "[isUSIMType]: " + String.format("%s: %s", e.toString(), e.getMessage()));
        }
        return false;
    }

    // For the group mms feature to be enabled, the following must be true:
    //  1. the feature is enabled in mms_config.xml (currently on by default)
    //  2. the feature is enabled in the mms settings page
    //  3. the SIM knows its own phone number
    public static boolean getIsGroupMmsEnabled(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean groupMmsPrefOn = prefs.getBoolean(MmsPreferenceActivity.GROUP_MMS_MODE, false);
        boolean isKnowNumber = false;
        if (EncapsulatedFeatureOption.MTK_GEMINI_SUPPORT) {
            isKnowNumber =
               !TextUtils.isEmpty(MessageUtils.getLocalNumberGemini(EncapsulatedPhone.GEMINI_SIM_1))
            || !TextUtils.isEmpty(MessageUtils.getLocalNumberGemini(EncapsulatedPhone.GEMINI_SIM_2));
        } else {
            isKnowNumber = !TextUtils.isEmpty(MessageUtils.getLocalNumber());
        }
        return MmsConfig.getGroupMmsEnabled() && groupMmsPrefOn;// && isKnowNumber;
    }

    /// M: fix bug ALPS00421364, update sim state dynamically. @{
    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(TelephonyIntents.ACTION_SIM_INDICATOR_STATE_CHANGED);
        registerReceiver(mSimReceiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mSimReceiver != null) {
            unregisterReceiver(mSimReceiver);
        }
    }

    private BroadcastReceiver mSimReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(TelephonyIntents.ACTION_SIM_INDICATOR_STATE_CHANGED)) {
                setPreferenceScreen(null);
                setMessagePreferences();
                setListPrefSummary();
            }
        }
    };
    /// @}
}
