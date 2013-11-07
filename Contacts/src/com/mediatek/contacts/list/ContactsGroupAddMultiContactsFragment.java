package com.mediatek.contacts.list;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.android.contacts.group.GroupEditorFragment;
import com.android.contacts.list.ContactEntryListAdapter;
import com.android.contacts.list.ContactListFilter;

public class ContactsGroupAddMultiContactsFragment extends MultiContactsPickerBaseFragment
        implements GroupEditorFragment.ScrubListener {

    private static final String TAG = ContactsGroupAddMultiContactsFragment.class.getSimpleName();

    @Override
    protected void configureAdapter() {
        super.configureAdapter();

        final ContactEntryListAdapter adapter = getAdapter();
        // get contacts list by account
        final Intent intent = getArguments().getParcelable(FRAGMENT_ARGS);
        String accountType = (String) intent.getStringExtra("account_type");
        String accountName = (String) intent.getStringExtra("account_name");

        if (!TextUtils.isEmpty(accountType) && !TextUtils.isEmpty(accountName)) {
            ContactListFilter filter = ContactListFilter.createAccountFilter(accountType,
                    accountName, null, null);
            adapter.setFilter(filter);
            Log.d(TAG, "account type = " + accountType + "; account name = " + accountName);
        }
    }
    
    public boolean isAccountFilterEnable() {
        return false;
    }

    public void scrubAffinity() {
        getActivity().finish();
    }

    @Override
    public void onCreate(Bundle savedState) {
        Log.d(TAG, "onCreate setScrubListener");
        super.onCreate(savedState);
        GroupEditorFragment.setScrubListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GroupEditorFragment.removeScrubListener(this);
    }
}
