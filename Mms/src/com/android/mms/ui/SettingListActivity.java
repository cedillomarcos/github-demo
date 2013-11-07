package com.android.mms.ui;

import android.app.ListActivity;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.android.mms.MmsConfig;
import com.android.mms.R;
import com.mediatek.mms.ipmessage.IpMessageConsts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SettingListActivity extends ListActivity {
    private boolean mIsWithIsmsOrRcse = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(getResources().getString(R.string.menu_preferences));
        setContentView(R.layout.setting_list);
        String iSmsOrRces = MessageUtils.getResourceManager(SettingListActivity.this).getStringById(
            IpMessageConsts.ResourceId.STR_IPMESSAGE_SETTINGS);
        if (TextUtils.isEmpty(iSmsOrRces)) {
            iSmsOrRces = " ";
        }
        if (MessageUtils.getServiceManager(SettingListActivity.this).isFeatureSupported(
            IpMessageConsts.FeatureId.APP_SETTINGS)) {
            String[] settingList = new String[] {iSmsOrRces, getResources().getString(R.string.pref_setting_sms),
                getResources().getString(R.string.pref_setting_mms),
                getResources().getString(R.string.pref_setting_notification),
                getResources().getString(R.string.pref_setting_general)};
            setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, settingList));
            mIsWithIsmsOrRcse = true;
        } else {
            String[] settingListWithoutIsms = new String[] {getResources().getString(R.string.pref_setting_sms),
                getResources().getString(R.string.pref_setting_mms),
                getResources().getString(R.string.pref_setting_notification),
                getResources().getString(R.string.pref_setting_general)};
            setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, settingListWithoutIsms));
        }
    }

    @Override
    protected void onListItemClick(ListView arg0, View arg1, int arg2, long arg3) {
        if (mIsWithIsmsOrRcse) {
            switch (arg2) {
            case 0:
                Intent systemSettingsIntent = new Intent(IpMessageConsts.RemoteActivities.SYSTEM_SETTINGS);
                MessageUtils.startRemoteActivity(SettingListActivity.this, systemSettingsIntent);
                break;
            case 1:
                Intent smsPreferenceIntent = new Intent(SettingListActivity.this, SmsPreferenceActivity.class);
                startActivity(smsPreferenceIntent);
                break;
            case 2:
                Intent mmsPreferenceIntent = new Intent(SettingListActivity.this, MmsPreferenceActivity.class);
                startActivity(mmsPreferenceIntent);
                break;
            case 3:
                Intent notificationPreferenceIntent = new Intent(SettingListActivity.this,
                        NotificationPreferenceActivity.class);
                startActivity(notificationPreferenceIntent);
                break;
            case 4:
                Intent generalPreferenceIntent = new Intent(SettingListActivity.this, GeneralPreferenceActivity.class);
                startActivity(generalPreferenceIntent);
                break;
            default:
                break;
            }
        } else {
            switch (arg2) {
            case 0:
                Intent smsPreferenceIntent = new Intent(SettingListActivity.this, SmsPreferenceActivity.class);
                startActivity(smsPreferenceIntent);
                break;
            case 1:
                Intent mmsPreferenceIntent = new Intent(SettingListActivity.this, MmsPreferenceActivity.class);
                startActivity(mmsPreferenceIntent);
                break;
            case 2:
                Intent notificationPreferenceIntent = new Intent(SettingListActivity.this,
                        NotificationPreferenceActivity.class);
                startActivity(notificationPreferenceIntent);
                break;
            case 3:
                Intent generalPreferenceIntent = new Intent(SettingListActivity.this, GeneralPreferenceActivity.class);
                startActivity(generalPreferenceIntent);
                break;
            default:
                break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            // The user clicked on the Messaging icon in the action bar. Take them back from
            // wherever they came from
            finish();
            return true;
        }
        return false;
    }
}
