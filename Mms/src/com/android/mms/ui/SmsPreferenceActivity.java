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
import android.os.RemoteException;
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
import com.mediatek.encapsulation.android.telephony.EncapsulatedTelephony.Sms;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;

import android.text.InputFilter;
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
import com.mediatek.encapsulation.com.android.internal.telephony.EncapsulatedTelephonyService;
import com.mediatek.encapsulation.com.mediatek.common.featureoption.EncapsulatedFeatureOption;
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
public class SmsPreferenceActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "SmsPreferenceActivity";

    private static final boolean DEBUG = false;

    public static final String SMS_DELIVERY_REPORT_MODE = "pref_key_sms_delivery_reports";

    public static final String SMS_QUICK_TEXT_EDITOR = "pref_key_quick_text_editor";

    public static final String SMS_SERVICE_CENTER = "pref_key_sms_service_center";

    public static final String SMS_VALIDITY_PERIOD = "pref_key_sms_validity_period";

    public static final String SMS_MANAGE_SIM_MESSAGES = "pref_key_manage_sim_messages";

    public static final String SMS_SAVE_LOCATION = "pref_key_sms_save_location";

    public static final String SMS_INPUT_MODE = "pref_key_sms_input_mode";

    public static final String SMS_FORWARD_WITH_SENDER = "pref_key_forward_with_sender";

    public static final String SMS_SETTINGS = "pref_key_sms_settings";

    public static final String SETTING_SAVE_LOCATION = "Phone";

    /// M: fix bug ALPS00437648, restoreDefaultPreferences in tablet
    public static final String SETTING_SAVE_LOCATION_TABLET = "Device";

    public static final String SETTING_INPUT_MODE = "Automatic";

    private static final String sMmsPreference = "com.android.mms_preferences";

    private static final String sUim = "UIM";

    public static final String SDCARD_DIR_PATH = "//sdcard//message//";

    // Menu entries
    private static final int MENU_RESTORE_DEFAULTS = 1;

    private final int MAX_EDITABLE_LENGTH = 20;

    private Preference mSmsQuickTextEditorPref;

    private Preference mManageSimPref;

    private Preference mSmsServiceCenterPref;

    private Preference mSmsValidityPeriodPref;

    // MTK_OP01_PROTECT_END
    // all preferences need change key for single sim card
    private CheckBoxPreference mSmsDeliveryReport;

    private CheckBoxPreference mSmsForwardWithSender;

    // all preferences need change key for multiple sim card
    private Preference mSmsDeliveryReportMultiSim;

    private Preference mSmsServiceCenterPrefMultiSim;

    private Preference mSmsValidityPeriodPrefMultiSim;

    private Preference mManageSimPrefMultiSim;

    private Preference mSmsSaveLoactionMultiSim;

    private ListPreference mSmsLocation;

    private ListPreference mSmsInputMode;

    private static final String LOCATION_PHONE = "Phone";

    private static final String LOCATION_SIM = "Sim";

    private EditText mNumberText;

    private List<SIMInfo> listSimInfo;

    int slotId;

    private EditText inputNumber;

    private AlertDialog mNumberTextDialog;

    private int currentSimCount = 0;
	//GPBYL-319 chenbo modify 20130522 (start)
	private boolean isTablet;
	//GPBYL-319 chenbo modify 20130522 (end)

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setListPrefSummary();
    }

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

    private void setListPrefSummary() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        // For mSmsLocation;
        String saveLocation = null;
        if (MmsConfig.getSmsMultiSaveLocationEnabled()) {
            if (EncapsulatedFeatureOption.MTK_GEMINI_SUPPORT == true) {
                int currentSimCount = SIMInfo.getInsertedSIMCount(this);
                int slotId = 0;
                if (currentSimCount == 1) {
                    slotId = SIMInfo.getInsertedSIMList(this).get(0).getSlot();
                    saveLocation = sp.getString((Long.toString(slotId) + "_" + SMS_SAVE_LOCATION), "Phone");
                }
            } else {
                saveLocation = sp.getString(SMS_SAVE_LOCATION, "Phone");
            }
        }

        if (saveLocation == null) {
            /// M: fix bug ALPS00429244, change "Phone" to "Device"
            if (!getResources().getBoolean(R.bool.isTablet)) {
                saveLocation = sp.getString(SMS_SAVE_LOCATION, "Phone");
            } else {
                saveLocation = sp.getString(SMS_SAVE_LOCATION, "Device");
            }
        }
        if (!getResources().getBoolean(R.bool.isTablet)) {
            mSmsLocation.setSummary(getVisualTextName(saveLocation, R.array.pref_sms_save_location_choices,
                R.array.pref_sms_save_location_values));
        } else {
            mSmsLocation.setSummary(getVisualTextName(saveLocation, R.array.pref_tablet_sms_save_location_choices,
                R.array.pref_tablet_sms_save_location_values));
        }
    }

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        MmsLog.d(TAG, "onCreate");
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(getResources().getString(R.string.actionbar_sms_setting));
        actionBar.setDisplayHomeAsUpEnabled(true);
		//GPBYL-319 chenbo modify 20130522 (start)
		isTablet = getResources().getBoolean(R.bool.isTablet);
		//GPBYL-319 chenbo modify 20130522 (end)
        setMessagePreferences();
    }

    private void setMessagePreferences() {
        currentSimCount = SIMInfo.getInsertedSIMCount(this);
        if (EncapsulatedFeatureOption.MTK_GEMINI_SUPPORT == true) {
            MmsLog.d(TAG, "MTK_GEMINI_SUPPORT is true");
            MmsLog.d(TAG, "currentSimCount is :" + currentSimCount);
            if (currentSimCount <= 1) {
                addPreferencesFromResource(R.xml.smspreferences);
            } else {
                addPreferencesFromResource(R.xml.smsmulticardpreferences);
            }
        } else {
            addPreferencesFromResource(R.xml.smspreferences);
        }
        mSmsQuickTextEditorPref = findPreference(SMS_QUICK_TEXT_EDITOR);
        mSmsLocation = (ListPreference) findPreference(SMS_SAVE_LOCATION);
        mSmsLocation.setOnPreferenceChangeListener(this);
        PreferenceCategory smsCategory = (PreferenceCategory) findPreference(SMS_SETTINGS);
        if (EncapsulatedFeatureOption.MTK_GEMINI_SUPPORT == true) {
            // remove SMS validity period feature for non-OP01 project
            {
                if (!MmsConfig.getSmsValidityPeriodEnabled()) {
                    mSmsValidityPeriodPref = findPreference(SMS_VALIDITY_PERIOD);
                    if (mSmsValidityPeriodPref != null) {
                        smsCategory.removePreference(mSmsValidityPeriodPref);
                    }
                }
            }
            if (currentSimCount == 0) {
                // No SIM card, remove the SIM-related prefs
                // smsCategory.removePreference(mManageSimPref);
                // If there is no SIM, this item will be disabled and can not be accessed.
                mManageSimPref = findPreference(SMS_MANAGE_SIM_MESSAGES);
                mManageSimPref.setEnabled(false);
                if (!MmsConfig.getSIMSmsAtSettingEnabled()) {
                    smsCategory.removePreference(mManageSimPref);
                }
                // MTK_OP02_PROTECT_END
                mSmsServiceCenterPref = findPreference(SMS_SERVICE_CENTER);
                mSmsServiceCenterPref.setEnabled(false);
                mSmsDeliveryReport = (CheckBoxPreference) findPreference(SMS_DELIVERY_REPORT_MODE);
                mSmsDeliveryReport.setEnabled(false);
                // MTK_OP01_PROTECT_START
                if (MmsConfig.getSmsValidityPeriodEnabled()) {
                    mSmsValidityPeriodPref = findPreference(SMS_VALIDITY_PERIOD);
                    mSmsValidityPeriodPref.setEnabled(false);
                }
                // MTK_OP01_PROTECT_END
            }
        } else {
            // remove SMS validity period feature for non-Gemini project
            {
                mSmsValidityPeriodPref = findPreference(SMS_VALIDITY_PERIOD);
                smsCategory.removePreference(mSmsValidityPeriodPref);
            }
            if (!MmsApp.getApplication().getTelephonyManager().hasIccCard()) {
                // smsCategory.removePreference(mManageSimPref);
                // If there is no SIM, this item will be disabled and can not be accessed.
                mManageSimPref = findPreference(SMS_MANAGE_SIM_MESSAGES);
                mManageSimPref.setEnabled(false);
                if (!MmsConfig.getSIMSmsAtSettingEnabled()) {
                    smsCategory.removePreference(mManageSimPref);
                }
                // MTK_OP02_PROTECT_END
                mSmsServiceCenterPref = findPreference(SMS_SERVICE_CENTER);
                mSmsServiceCenterPref.setEnabled(false);
            } else {
                mManageSimPref = findPreference(SMS_MANAGE_SIM_MESSAGES);
                if (!MmsConfig.getSIMSmsAtSettingEnabled()) {
                    smsCategory.removePreference(mManageSimPref);
                }
                // MTK_OP02_PROTECT_END
                mSmsServiceCenterPref = findPreference(SMS_SERVICE_CENTER);
            }
        }
        addSmsInputModePreference();
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
        if (MmsConfig.getForwardWithSenderEnabled()) {
            mSmsForwardWithSender = (CheckBoxPreference) findPreference(SMS_FORWARD_WITH_SENDER);
            SharedPreferences sp = getSharedPreferences("com.android.mms_preferences", MODE_WORLD_READABLE);
            if (mSmsForwardWithSender != null) {
                mSmsForwardWithSender.setChecked(sp.getBoolean(mSmsForwardWithSender.getKey(), true));
            }
            //mManageSimPref = findPreference(SMS_MANAGE_SIM_MESSAGES);
            if (MmsConfig.getMmsDirMode() && mManageSimPref != null) {
                ((PreferenceCategory)findPreference("pref_key_sms_settings")).removePreference(mManageSimPref);
            }
        } else {
            mSmsForwardWithSender = (CheckBoxPreference) findPreference(SMS_FORWARD_WITH_SENDER);
            smsCategory.removePreference(mSmsForwardWithSender);
        }
        if(currentSimCount == 0){
            mSmsLocation.setKey(SMS_SAVE_LOCATION);
			//GPBYL-319 chenbo modify 20130522 (start)
            mSmsLocation.setValue(isTablet ? SETTING_SAVE_LOCATION_TABLET : SETTING_SAVE_LOCATION);
			//GPBYL-319 chenbo modify 20130522 (end)
            mSmsLocation.setEnabled(false);
        }
    }

    // add input mode setting for op03 request, if not remove it.
    private void addSmsInputModePreference() {
        if (MmsConfig.getSmsEncodingTypeEnabled()) {
            mSmsInputMode = (ListPreference) findPreference(SMS_INPUT_MODE);
       } else {
            PreferenceCategory smsCategory = (PreferenceCategory)findPreference("pref_key_sms_settings");
            mSmsInputMode = (ListPreference) findPreference(SMS_INPUT_MODE);
            if (mSmsInputMode != null) {
               smsCategory.removePreference(mSmsInputMode);
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
        mSmsDeliveryReport = (CheckBoxPreference) findPreference(SMS_DELIVERY_REPORT_MODE);
        mSmsServiceCenterPref = findPreference(SMS_SERVICE_CENTER);
        // MTK_OP01_PROTECT_START
        mSmsValidityPeriodPref = findPreference(SMS_VALIDITY_PERIOD);
        // MTK_OP01_PROTECT_END
        mManageSimPref = findPreference(SMS_MANAGE_SIM_MESSAGES);
        mManageSimPrefMultiSim = null;
        PreferenceCategory smsCategory = (PreferenceCategory) findPreference("pref_key_sms_settings");
        if (MmsConfig.getSmsMultiSaveLocationEnabled()) {
            int slotid = listSimInfo.get(0).getSlot();
            mSmsLocation = (ListPreference) findPreference(SMS_SAVE_LOCATION);
            mSmsLocation.setKey(Long.toString(slotid) + "_" + SMS_SAVE_LOCATION);
            SharedPreferences spr = getSharedPreferences("com.android.mms_preferences", MODE_WORLD_READABLE);
			//GPBYL-319 chenbo modify 20130522 (start)
            mSmsLocation.setValue(spr.getString((Long.toString(slotid) + "_" + SMS_SAVE_LOCATION), isTablet ? SETTING_SAVE_LOCATION_TABLET : SETTING_SAVE_LOCATION));
			//GPBYL-319 chenbo modify 20130522 (end)
        }

        if (!MmsConfig.getSIMSmsAtSettingEnabled()) {
            if (mManageSimPref != null) {
                smsCategory.removePreference(mManageSimPref);
            }
        }
        // MTK_OP02_PROTECT_END
        mSmsDeliveryReport.setKey(Long.toString(simId) + "_" + SMS_DELIVERY_REPORT_MODE);
        // get the stored value
        SharedPreferences sp = getSharedPreferences(sMmsPreference, MODE_WORLD_READABLE);
        if (mSmsDeliveryReport != null) {
            mSmsDeliveryReport.setChecked(sp.getBoolean(mSmsDeliveryReport.getKey(), false));
        }
    }

    private void setMultiCardPreference() {
        mSmsDeliveryReportMultiSim = findPreference(SMS_DELIVERY_REPORT_MODE);
        mSmsServiceCenterPrefMultiSim = findPreference(SMS_SERVICE_CENTER);
        mSmsValidityPeriodPrefMultiSim = findPreference(SMS_VALIDITY_PERIOD);
        mManageSimPrefMultiSim = findPreference(SMS_MANAGE_SIM_MESSAGES);
        mManageSimPref = null;
        PreferenceCategory smsCategory = (PreferenceCategory) findPreference(SMS_SETTINGS);
        if (MmsConfig.getSmsMultiSaveLocationEnabled()) {
            if (mSmsLocation != null) {
                smsCategory.removePreference(mSmsLocation);
                Preference saveLocationMultiSim = new Preference(this);
                saveLocationMultiSim.setKey(SMS_SAVE_LOCATION);
                saveLocationMultiSim.setTitle(R.string.sms_save_location);
                saveLocationMultiSim.setSummary(R.string.sms_save_location);
                smsCategory.addPreference(saveLocationMultiSim);
                mSmsSaveLoactionMultiSim = findPreference(SMS_SAVE_LOCATION);
           }
        }

        if (!MmsConfig.getSIMSmsAtSettingEnabled()) {
            if (mManageSimPrefMultiSim != null) {
                smsCategory.removePreference(mManageSimPrefMultiSim);
            }
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
        if (preference == mManageSimPref) {
            if (EncapsulatedFeatureOption.MTK_GEMINI_SUPPORT == true) {
                listSimInfo = SIMInfo.getInsertedSIMList(this);
                int slotId = listSimInfo.get(0).getSlot();
                MmsLog.d(TAG, "slotId is : " + slotId);
                if (slotId != -1) {
                    Intent it = new Intent();
                    it.setClass(this, ManageSimMessages.class);
                    it.putExtra("SlotId", slotId);
                    startActivity(it);
                }
            } else {
                startActivity(new Intent(this, ManageSimMessages.class));
            }
        } else if (preference == mSmsQuickTextEditorPref) {
            Intent intent = new Intent();
            intent.setClass(this, SmsTemplateEditActivity.class);
            startActivity(intent);
        } else if (preference == mSmsDeliveryReportMultiSim) {
            Intent it = new Intent();
            it.setClass(this, MultiSimPreferenceActivity.class);
            it.putExtra("preference", preference.getKey());
            it.putExtra("preferenceTitleId", R.string.pref_title_sms_delivery_reports);
            startActivity(it);
        } else if (preference == mSmsServiceCenterPref) {
            listSimInfo = SIMInfo.getInsertedSIMList(this);
            if (listSimInfo != null && listSimInfo.isEmpty()) {
                MmsLog.d(TAG, "there is no sim card");
                return true;
            }
            int id = listSimInfo.get(0).getSlot();
            if (EncapsulatedFeatureOption.EVDO_DT_SUPPORT == true && isUSimType(id)) {
                showToast(R.string.cdma_not_support);
            } else {
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                mNumberText = new EditText(dialog.getContext());
                mNumberText.setHint(R.string.type_to_compose_text_enter_to_send);
                mNumberText.computeScroll();
                mNumberText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(MAX_EDITABLE_LENGTH)});
                // mNumberText.setKeyListener(new DigitsKeyListener(false, true));
                mNumberText.setInputType(EditorInfo.TYPE_CLASS_TEXT | EditorInfo.TYPE_CLASS_PHONE);
                EncapsulatedTelephonyService teleService = EncapsulatedTelephonyService.getInstance();
                String gotScNumber;
                try {
                    if (EncapsulatedFeatureOption.MTK_GEMINI_SUPPORT == true) {
                        int slotId = listSimInfo.get(0).getSlot();
                        gotScNumber = teleService.getScAddressGemini(slotId);
                    } else {
                        gotScNumber = teleService.getScAddressGemini(0);
                    }
                } catch (RemoteException e) {
                    gotScNumber = null;
                    MmsLog.e(MmsApp.TXN_TAG, "getScAddressGemini is failed.\n" + e.toString());
                }
                MmsLog.d(TAG, "gotScNumber is: " + gotScNumber);
                mNumberText.setText(gotScNumber);
		mNumberText.setSelection(gotScNumber.length());//cuinana GPBYB-171 add
                mNumberTextDialog = dialog.setIcon(R.drawable.ic_dialog_info_holo_light).setTitle(
                    R.string.sms_service_center).setView(mNumberText).setPositiveButton(R.string.OK,
                    new PositiveButtonListener()).setNegativeButton(R.string.Cancel, new NegativeButtonListener())
                        .show();
            }
        } else if (preference == mSmsValidityPeriodPref) {
            if (EncapsulatedFeatureOption.MTK_GEMINI_SUPPORT == true) {
                int slotId = listSimInfo.get(0).getSlot();
                final int[] validity_peroids = {SmsManager.VALIDITY_PERIOD_NO_DURATION,
                    SmsManager.VALIDITY_PERIOD_ONE_HOUR, SmsManager.VALIDITY_PERIOD_SIX_HOURS,
                    SmsManager.VALIDITY_PERIOD_TWELVE_HOURS, SmsManager.VALIDITY_PERIOD_ONE_DAY,
                    SmsManager.VALIDITY_PERIOD_MAX_DURATION,};
                final CharSequence[] validity_items = {getResources().getText(R.string.sms_validity_period_nosetting),
                    getResources().getText(R.string.sms_validity_period_1hour),
                    getResources().getText(R.string.sms_validity_period_6hours),
                    getResources().getText(R.string.sms_validity_period_12hours),
                    getResources().getText(R.string.sms_validity_period_1day),
                    getResources().getText(R.string.sms_validity_period_max)};
                /* check validity index */
                final String validityKey = Long.toString(slotId) + "_" + SmsPreferenceActivity.SMS_VALIDITY_PERIOD;
                int vailidity = PreferenceManager.getDefaultSharedPreferences(this).getInt(validityKey,
                    SmsManager.VALIDITY_PERIOD_NO_DURATION);
                int currentPosition = 0;
                MmsLog.d(TAG, "validity found the res = " + vailidity);
                for (int i = 0; i < validity_peroids.length; i++) {
                    if (vailidity == (validity_peroids[i])) {
                        MmsLog.d(TAG, "validity found the position = " + i);
                        currentPosition = i;
                    }
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getResources().getText(R.string.sms_validity_period));
                builder.setSingleChoiceItems(validity_items, currentPosition, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(
                            SmsPreferenceActivity.this).edit();
                        editor.putInt(validityKey, validity_peroids[item]);
                        editor.commit();
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        } else if (preference == mSmsServiceCenterPrefMultiSim || preference == mSmsValidityPeriodPrefMultiSim
            || preference == mManageSimPrefMultiSim || (preference == mSmsSaveLoactionMultiSim && currentSimCount > 1)) {
            Intent it = new Intent();
            it.setClass(this, SelectCardPreferenceActivity.class);
            it.putExtra("preference", preference.getKey());
            if (preference == mSmsServiceCenterPrefMultiSim) {
                it.putExtra("preferenceTitleId", R.string.sms_service_center);
            } else if (preference == mSmsValidityPeriodPrefMultiSim) {
                it.putExtra("preferenceTitleId", R.string.sms_validity_period);
            } else if (preference == mManageSimPrefMultiSim) {
                it.putExtra("preferenceTitleId", R.string.pref_title_manage_sim_messages);
            } else if (preference == mSmsSaveLoactionMultiSim) {
                it.putExtra("preferenceTitleId", R.string.sms_save_location);
            }
            startActivity(it);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    // / M: added for bug ALPS00314789 begin
    private boolean isValidAddr(String address) {
        boolean ret = true;
        if (address.isEmpty()) {
            return ret;
        }
        if (address.charAt(0) == '+') {
            for (int i = 1, count = address.length(); i < count; i++) {
                if (address.charAt(i) < '0' || address.charAt(i) > '9') {
                    ret = false;
                    break;
                }
            }
        } else {
            for (int i = 0, count = address.length(); i < count; i++) {
                if (address.charAt(i) < '0' || address.charAt(i) > '9') {
                    ret = false;
                    break;
                }
            }
        }
        return ret;
    }

    // / M: added for bug ALPS00314789 end
    private class PositiveButtonListener implements OnClickListener {
        public void onClick(DialogInterface dialog, int which) {
            // write to the SIM Card.
            // / M: added for bug ALPS00314789 begin
            if (!isValidAddr(mNumberText.getText().toString())) {
                String num = mNumberText.getText().toString();
                String strUnSpFormat = getResources().getString(R.string.unsupported_media_format, "");
                Toast.makeText(getApplicationContext(), strUnSpFormat, Toast.LENGTH_SHORT).show();
                return;
            }
            // / M: added for bug ALPS00314789 end
            final EncapsulatedTelephonyService teleService = EncapsulatedTelephonyService.getInstance();
            if (EncapsulatedFeatureOption.MTK_GEMINI_SUPPORT == true) {
                slotId = listSimInfo.get(0).getSlot();
            } else {
                slotId = 0;
            }
            new Thread(new Runnable() {
                public void run() {
                    try {
                        teleService.setScAddressGemini(mNumberText.getText().toString(), slotId);
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

    private void restoreDefaultPreferences() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(SmsPreferenceActivity.this)
                .edit();
        if (EncapsulatedFeatureOption.MTK_GEMINI_SUPPORT == true) {
            listSimInfo = SIMInfo.getInsertedSIMList(this);
            if (listSimInfo != null) {
                int simCount = listSimInfo.size();
                if (simCount > 0) {
                    for (int i = 0; i < simCount; i++) {
                        Long simId = listSimInfo.get(i).getSimId();
                        editor.putBoolean(Long.toString(simId) + "_" + SMS_DELIVERY_REPORT_MODE, false);
                        int slotid = listSimInfo.get(i).getSlot();
                        if (MmsConfig.getSmsMultiSaveLocationEnabled()) {
                            /// M: fix bug ALPS00437648, restoreDefaultPreferences in tablet
                            if (!isTablet) {
                                editor.putString(Long.toString(slotid) + "_" + SMS_SAVE_LOCATION, SETTING_SAVE_LOCATION);
                            } else {
                                editor.putString(Long.toString(slotid) + "_" + SMS_SAVE_LOCATION, SETTING_SAVE_LOCATION_TABLET);
                            }
                        }
                        if (MmsConfig.getSmsValidityPeriodEnabled()) {
                            editor.putInt(Long.toString(slotid) + "_" + SMS_VALIDITY_PERIOD,
                                    SmsManager.VALIDITY_PERIOD_NO_DURATION);
                        }
                    }
                }
            }
        } else {
            editor.putBoolean(SMS_DELIVERY_REPORT_MODE, false);
            /// M: fix bug ALPS00437648, restoreDefaultPreferences in tablet
            if (!getResources().getBoolean(R.bool.isTablet)) {
                editor.putString(SMS_SAVE_LOCATION, SETTING_SAVE_LOCATION);
            } else {
                editor.putString(SMS_SAVE_LOCATION, SETTING_SAVE_LOCATION_TABLET);
            }

        }
        if (MmsConfig.getSmsEncodingTypeEnabled()) {
            editor.putString(SMS_INPUT_MODE, SETTING_INPUT_MODE);
        }
        if (MmsConfig.getForwardWithSenderEnabled()) {
            editor.putBoolean(SMS_FORWARD_WITH_SENDER, true);
        }
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
        if (SMS_SAVE_LOCATION.equals(key) && !(currentSimCount > 1 && MmsConfig.getSmsMultiSaveLocationEnabled())) {
            if (!getResources().getBoolean(R.bool.isTablet)) {
                mSmsLocation.setSummary(getVisualTextName(stored, R.array.pref_sms_save_location_choices,
                    R.array.pref_sms_save_location_values));
            } else {
                mSmsLocation.setSummary(getVisualTextName(stored, R.array.pref_tablet_sms_save_location_choices,
                    R.array.pref_tablet_sms_save_location_values));
            }
        } else if ((Long.toString(slotId) + "_" + SMS_SAVE_LOCATION).equals(key)) {
            if (!getResources().getBoolean(R.bool.isTablet)) {
                mSmsLocation.setSummary(getVisualTextName(stored, R.array.pref_sms_save_location_choices,
                    R.array.pref_sms_save_location_values));
            } else {
                mSmsLocation.setSummary(getVisualTextName(stored, R.array.pref_tablet_sms_save_location_choices,
                    R.array.pref_tablet_sms_save_location_values));
            }
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

    /// M: update sim state dynamically. @{
    private BroadcastReceiver mSimReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Dialog LocationDialog = mSmsLocation.getDialog();
            if (action.equals(TelephonyIntents.ACTION_SIM_INDICATOR_STATE_CHANGED)) {
                if(mNumberTextDialog!= null && mNumberTextDialog.isShowing()){
                    mNumberTextDialog.dismiss();
                }
                if(LocationDialog != null && LocationDialog.isShowing()) {
                    LocationDialog.dismiss();
                }
                setPreferenceScreen(null);
                setMessagePreferences();
                setListPrefSummary();
            }
        }
    };
}
