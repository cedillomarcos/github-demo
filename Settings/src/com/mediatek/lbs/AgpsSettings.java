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

/**
 * 
 */
package com.mediatek.lbs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.PhoneConstants;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.mediatek.common.agps.MtkAgpsManager;
import com.mediatek.common.agps.MtkAgpsProfile;
import com.mediatek.common.agps.MtkAgpsProfileManager;
import com.mediatek.common.featureoption.FeatureOption;
import com.mediatek.telephony.TelephonyManagerEx;
import com.mediatek.xlog.Xlog;

import java.util.ArrayList;
import java.util.List;

public class AgpsSettings extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {
    private static final String XLOGTAG = "Settings/Agps";
    public static final String KEY_AGPS_SHARE = "agps_share";
    public static final String SIM_STATUS = "sim_status";

    private static final String KEY_SELECT_PROFILE = "select_profile";

    private static final String KEY_SLP_ADDRESS = "slp_address";
    private static final String KEY_PORT = "port";
    private static final String KEY_TLS = "tls";
    private static final String KEY_MOBILE_DATACONN = "mobile_dataConn";
    private static final String KEY_ABOUT_AGPS = "about_agps";
    private static final String DISABLE_ON_REBOOT = "disable_agps_on_reboot";
    private static final String NETWORK_INITIATE = "Network_Initiate";
    // only local or local + Roaming
    private static final String NETWORK_USED = "Network_Used";
    // M: mtk54279 CR[ALPS00331520] for SharedPreference save key use
    private static final String KEY_OPERATOR_CODE = "saved_operator_code";

    private CheckBoxPreference mDisableOnRebootCB;
    private CheckBoxPreference mNetworkInitiateCB;
    private ListPreference mNetworkUsedListPref;

    private ListPreference mSelectProfileListPref;
    private Preference mNetworkPref;
    private Preference mAboutPref;

    private EditTextPreference mSLPAddressET;
    private EditTextPreference mPortET;
    private CheckBoxPreference mTLSCB;

    private String mOperatorCode;
    // used to describe current data connection status
    private String mDataConnItemTitle;
    private String mDataConnItemSummary;
    private static final boolean GEMINI_PHONE = FeatureOption.MTK_GEMINI_SUPPORT;

    private static final int ABOUT_AGPS_DIALOG_ID = 0;
    private static final int ROAMING_ALERT_DIALOG_ID = 1;

    private MtkAgpsManager mAgpsMgr;
    private ConnectivityManager mConnMgr;
    private WifiManager mWifiMgr;
    private TelephonyManager mTelephonyMgr;
    private TelephonyManagerEx mTelMgrEx;

    private MtkAgpsProfileManager mAgpsProfileManager;
    private MtkAgpsProfile mDefaultProfile;

    // phone data connection state change listener
    private BroadcastReceiver mDataConnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // update data connection title and summary
            updateDataConnStatus();

            // update profile list
            initSlpProfileList();
            log("mDataConnReceiver " + mAgpsMgr.getProfile().code + " " + mAgpsMgr.getProfile().name);
            updateSlpProfile(mAgpsMgr.getProfile());

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        log("onCreate");
        mAgpsProfileManager = new MtkAgpsProfileManager();
        mAgpsProfileManager.updateAgpsProfile("/etc/agps_profiles_conf.xml");
        mDefaultProfile = mAgpsProfileManager.getDefaultProfile();

        mAgpsMgr = (MtkAgpsManager) getSystemService(Context.MTK_AGPS_SERVICE);
        mConnMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        /// M: WifiManager memory leak , change context to getApplicationContext @{
        mWifiMgr = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        ///@}
        mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mTelMgrEx = TelephonyManagerEx.getDefault();
        
        if (mAgpsMgr == null || mConnMgr == null || mWifiMgr == null || mTelephonyMgr == null) {
            log("ERR: getSystemService failed mAgpsMgr=" + mAgpsMgr + " mConnMgr=" + mConnMgr + " mWifiMgr=" + mWifiMgr
                    + " mTelephonyMgr=" + mTelephonyMgr);
            return;
        }

        addPreferencesFromResource(R.xml.agps_settings);
        initPreference();

    }

    @Override
    public void onPause() {
        super.onPause();
        log("^_^ onPause");

        // / M: mtk54279 CR[ALPS00331520] for save mOperatorCode @{
        SharedPreferences sharedPref = getActivity().getSharedPreferences("agps_operator", 0);
        SharedPreferences.Editor editor = sharedPref.edit();
        log("save mOperatorCode to sharedpreference " + mOperatorCode);
        editor.putString(KEY_OPERATOR_CODE, mOperatorCode);
        editor.commit();

        getActivity().unregisterReceiver(mDataConnReceiver);
        // / M: @} end
    }

    @Override
    public void onResume() {
        super.onResume();
        log("onResume");
        // / M: mtk54279 CR[ALPS00331520] for restore saved mOperatorCode @{
        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        getActivity().registerReceiver(mDataConnReceiver, intentFilter);

        SharedPreferences sharedPref = getActivity().getSharedPreferences("agps_operator", 0);
        mOperatorCode = sharedPref.getString(KEY_OPERATOR_CODE, null);
        log("restored mOperatorCode " + mOperatorCode);
        // / M: @} end
        updateDataConnStatus();
        initSlpProfileList();
        updatePage();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        log("^_^ onDestroy");
    }

    private void updatePage() {
        if (mAgpsMgr.getRoamingStatus()) {
            mNetworkUsedListPref.setSummary(R.string.Network_Local_and_Roaming_Summary);
            mNetworkUsedListPref.setValueIndex(1);
        } else {
            mNetworkUsedListPref.setSummary(R.string.Network_Only_Local_Summary);
            mNetworkUsedListPref.setValueIndex(0);
        }
        log("updatePage " + mAgpsMgr.getProfile().name);
        updateSlpProfile(mAgpsMgr.getProfile());
        mNetworkInitiateCB.setChecked(mAgpsMgr.getNiStatus());
    }

    /**
     * initiate profile list according to current SIM status
     */
    private void updateDataConnStatus() {  //MTK_CS_IGNORE_THIS_LINE
        int sim1Satus = -1;
        int sim2Satus = -1;

        mDataConnItemTitle = getString(R.string.MobileNetwork_DataConn_off);
        mDataConnItemSummary = getString(R.string.MobileNetwork_off_Summary);

        int networkType = -1;
        NetworkInfo networkInfo = mConnMgr.getActiveNetworkInfo();
        if (networkInfo != null) {
            networkType = networkInfo.getType();
        } else {
            log("WARNING: no active network");
        }
        log("updateDataConnStatus");
        mOperatorCode = null;
        if (networkType == ConnectivityManager.TYPE_MOBILE) {

            if (GEMINI_PHONE) {

                sim1Satus = mTelMgrEx.getSimState(PhoneConstants.GEMINI_SIM_1);
                sim2Satus = mTelMgrEx.getSimState(PhoneConstants.GEMINI_SIM_2);

                log("sim1 status=" + sim1Satus);
                log("sim2 status=" + sim2Satus);

                if (TelephonyManager.SIM_STATE_READY == sim1Satus && TelephonyManager.SIM_STATE_READY != sim2Satus) {
                    getMobileConnectionInfo(true, PhoneConstants.GEMINI_SIM_1);
                } else if (TelephonyManager.SIM_STATE_READY != sim1Satus && TelephonyManager.SIM_STATE_READY == sim2Satus) {
                    getMobileConnectionInfo(true, PhoneConstants.GEMINI_SIM_2);
                } else if (TelephonyManager.SIM_STATE_READY == sim1Satus && TelephonyManager.SIM_STATE_READY == sim2Satus) {
                    if (!getMobileConnectionInfo(true, PhoneConstants.GEMINI_SIM_1)) {
                        getMobileConnectionInfo(true, PhoneConstants.GEMINI_SIM_2);
                    }
                }
            } else {
                sim1Satus = mTelephonyMgr.getSimState();
                if (TelephonyManager.SIM_STATE_READY == sim1Satus) {
                    // In this case ,it is the single card platform and the sim id is nonsense.
                    getMobileConnectionInfo(false, 0);
                }
            }
        } else if (networkType == ConnectivityManager.TYPE_WIFI && mWifiMgr != null && networkInfo != null
                && networkInfo.isConnected() && networkInfo.isAvailable()) {
            mDataConnItemTitle = getString(R.string.WiFiNetwork_on_title);
            mDataConnItemSummary = getString(R.string.MobileNetwork_on_Summary);
        }

        mNetworkPref.setTitle(mDataConnItemTitle);
        mNetworkPref.setSummary(mDataConnItemSummary);
    }

    // when mobile connection is on ,get the profile list,data connection title and summary
    private boolean getMobileConnectionInfo(boolean isGemini, int simid) {
        if (isGemini) {
            mOperatorCode = mTelMgrEx.getSimOperator(simid);
            if (mTelMgrEx.getDataState(simid) == TelephonyManager.DATA_CONNECTED) {
                mDataConnItemTitle = getString(R.string.MobileNetwork_SIM_Active, simid + 1);
                mDataConnItemSummary = getString(R.string.MobileNetwork_on_Summary);
                return true;
            }
        } else {
            mOperatorCode = mTelephonyMgr.getSimOperator();
            if (mTelephonyMgr.getDataState() == TelephonyManager.DATA_CONNECTED) {
                mDataConnItemTitle = getString(R.string.MobileNetwork_SIM_Active, "");
                mDataConnItemSummary = getString(R.string.MobileNetwork_on_Summary);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        super.onPreferenceTreeClick(preferenceScreen, preference);

        if ((preference.getKey()).equals(NETWORK_INITIATE)) {
            CheckBoxPreference niCheckBox = (CheckBoxPreference) preference;
            mAgpsMgr.setNiEnable(niCheckBox.isChecked());
        } else if ((preference.getKey()).equals(DISABLE_ON_REBOOT)) {
            Intent intent = new Intent(MtkAgpsManager.AGPS_DISABLE_UPDATE);
            intent.putExtra("status", mDisableOnRebootCB.isChecked());
            getActivity().sendBroadcast(intent);
        } else if (mAboutPref != null && mAboutPref.getKey().equals(preference.getKey())) {
            showDialog(ABOUT_AGPS_DIALOG_ID);
        }
        return false;
    }

    private void initPreference() {

        mDisableOnRebootCB = (CheckBoxPreference) findPreference(DISABLE_ON_REBOOT);
        SharedPreferences prefs = getActivity().getSharedPreferences("agps_disable", 0);
        boolean disableAfterReboot = false;
        if (prefs.getBoolean("changed", false)) {
            disableAfterReboot = prefs.getBoolean("status", false);
        }
        mDisableOnRebootCB.setChecked(disableAfterReboot);

        mNetworkInitiateCB = (CheckBoxPreference) findPreference(NETWORK_INITIATE);

        mNetworkUsedListPref = (ListPreference) findPreference(NETWORK_USED);
        mNetworkUsedListPref.setOnPreferenceChangeListener(this);

        /* Address */
        mSLPAddressET = (EditTextPreference) findPreference(KEY_SLP_ADDRESS);
        mSLPAddressET.setEnabled(false);

        /* Port */
        mPortET = (EditTextPreference) findPreference(KEY_PORT);
        mPortET.setEnabled(false);

        /* TLS */
        mTLSCB = (CheckBoxPreference) findPreference(KEY_TLS);
        mTLSCB.setEnabled(false);

        // MobieNetwork data connection
        mNetworkPref = (Preference) findPreference(KEY_MOBILE_DATACONN);

        // About A-GPS
        mAboutPref = (Preference) findPreference(KEY_ABOUT_AGPS);

        // Agps Profile list
        mSelectProfileListPref = (ListPreference) findPreference(KEY_SELECT_PROFILE);
        mSelectProfileListPref.setOnPreferenceChangeListener(this);

    }

    private void initSlpProfileList() {
        SharedPreferences prefs = getActivity().getSharedPreferences("omacp_profile", 0);
        if (prefs.getBoolean("changed", false)) {
            MtkAgpsProfile profile = new MtkAgpsProfile();
            profile.name = prefs.getString("name", null);
            profile.addr = prefs.getString("addr", null);
            profile.backupSlpNameVar = prefs.getString("backupSlpNameVar", null);
            profile.port = prefs.getInt("port", 0);
            profile.tls = prefs.getInt("tls", 0);
            profile.showType = prefs.getInt("showType", 0);
            profile.code = prefs.getString("code", null);
            profile.addrType = prefs.getString("addrType", null);
            profile.defaultApn = prefs.getString("defaultApn", null);
            profile.providerId = prefs.getString("providerId", null);
            mAgpsProfileManager.insertProfile(profile);
        }

        log("opeator code " + mOperatorCode);
        List<MtkAgpsProfile> availableProfiles = new ArrayList<MtkAgpsProfile>();

        List<MtkAgpsProfile> profiles = mAgpsProfileManager.getAllProfile();
        for (MtkAgpsProfile profile : profiles) {
            if (profile.code.equals(mAgpsProfileManager.getDefaultProfile().code)) {
                log("default profile code" + profile.code);
                availableProfiles.add(profile);
            } else if (profile.showType == 0) {
                log("showType == 0 profile code" + profile.code);
                availableProfiles.add(profile);
            } else if (profile.showType == 2 && profile.code.equals(mOperatorCode)) {
                log("showType == 2 profile code" + profile.code);
                availableProfiles.add(profile);
            }

        }

        String entries[] = new String[availableProfiles.size()];
        String values[] = new String[availableProfiles.size()];
        int num = 0;
        for (MtkAgpsProfile profile : availableProfiles) {
            entries[num] = profile.name;
            values[num] = profile.code;
            num++;
        }
        mSelectProfileListPref.setEntries(entries);
        mSelectProfileListPref.setEntryValues(values);

        boolean flag = false;
        MtkAgpsProfile selectProfile = mAgpsMgr.getProfile();
        log("select profile code" + selectProfile.code);
        for (MtkAgpsProfile profile : availableProfiles) {
            if (selectProfile.code.equals(profile.code)) {
                flag = true;
                break;
            }
        }

        if (!flag) {
            log("set current profile code" + mDefaultProfile.code);
            mAgpsMgr.setProfile(mDefaultProfile);
        }

    }

    private void updateSlpProfile(MtkAgpsProfile selectProfile) {
        mSelectProfileListPref.setValue(selectProfile.code);
        mSelectProfileListPref.setSummary(selectProfile.name);

        mSLPAddressET.setText(selectProfile.addr);
        mSLPAddressET.setSummary(selectProfile.addr);

        mPortET.setText(String.valueOf(selectProfile.port));
        mPortET.setSummary(String.valueOf(selectProfile.port));

        mTLSCB.setChecked(1 == selectProfile.tls);
    }

    /**
     * onPreferenceChange
     * 
     * @param preference  Preference
     * @param value  Object
     * @return boolean
     */
    public boolean onPreferenceChange(Preference preference, Object value) {

        final String key = preference.getKey();

        if (KEY_SELECT_PROFILE.equals(key)) {
            String code = value.toString();
            log("onPreferenceChange " + code);
            MtkAgpsProfile selectProfile = new MtkAgpsProfile();
            for (MtkAgpsProfile profile : mAgpsProfileManager.getAllProfile()) {
                if (profile.code.equals(code)) {
                    selectProfile = profile;
                    break;
                }
            }
            updateSlpProfile(selectProfile);
            log("onPreferenceChange set profile to mAgpsMgr");
            mAgpsMgr.setProfile(selectProfile);
        } else if (mNetworkUsedListPref.getKey().equals(key)) {
            int index = mNetworkUsedListPref.findIndexOfValue(value.toString());
            if (index == 0) {
                mAgpsMgr.setRoamingEnable(false);
                updatePage();
            } else if (index == 1) {
                if (!mAgpsMgr.getRoamingStatus()) {
                    showDialog(ROAMING_ALERT_DIALOG_ID);
                }
            }
        }

        return true;
    }

    /**
     * onCreateDialog invoke when create a dialog
     * 
     * @param id
     *            int
     * @return Dialog object
     */
    public Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        if (id == ABOUT_AGPS_DIALOG_ID) {
            dialog = new AlertDialog.Builder(getActivity()).setTitle(R.string.about_agps_title)
                    .setIcon(com.android.internal.R.drawable.ic_dialog_info).setMessage(R.string.about_agps_message)
                    .setPositiveButton(R.string.agps_OK, null).create();
        } else if (id == ROAMING_ALERT_DIALOG_ID) {
            dialog = new AlertDialog.Builder(getActivity()).setTitle(R.string.Network_Roaming_dialog_title)
                    .setIcon(com.android.internal.R.drawable.ic_dialog_alert)
                    .setMessage(R.string.Network_Roaming_dialog_content)
                    .setPositiveButton(R.string.agps_OK, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialoginterface, int i) {
                            mAgpsMgr.setRoamingEnable(true);
                            updatePage();
                        }
                    }).setNegativeButton(R.string.agps_enable_confirm_deny, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialoginterface, int i) {
                            updatePage();
                        }
                    }).create();
            updatePage();
        } else {
            log("WARNING: onCreateDialog unknown id recv");
        }
        return dialog;
    }

    private void log(String msg) {
        Xlog.d(XLOGTAG, "[AGPS Setting] " + msg);
    }

}
