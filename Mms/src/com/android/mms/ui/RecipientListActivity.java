/*
 * Copyright (C) 2012 The Android Open Source Project
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

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Contacts;
import android.provider.ContactsContract.Profile;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.QuickContactBadge;
import android.widget.TextView;

import com.android.mms.R;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;

/**
 * Display a list of recipients for a group conversation. This activity expects to receive a
 * threadId in the intent's extras.
 */
public class RecipientListActivity extends ListActivity implements Contact.UpdateListener {
    private final static String TAG = "RecipientListActivity";

    private long mThreadId;

    /// M: fix bug ALPS00433593, For posting UI update Runnables from other threads
    private Handler mHandler = new Handler();
    private ContactList mContacts;
    private Conversation mConv;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        if (icicle != null) {
            // Retrieve previously saved state of this activity.
            mThreadId = icicle.getLong(ComposeMessageActivity.THREAD_ID);
        } else {
            mThreadId = getIntent().getLongExtra(ComposeMessageActivity.THREAD_ID, 0);
        }
        if (mThreadId == 0) {
            Log.w(TAG, "No thread_id specified in extras or icicle. Finishing...");
            finish();
            return;
        }

        mConv = Conversation.get(this, mThreadId, true);
        if (mConv == null) {
            Log.w(TAG, "No conversation found for threadId: " + mThreadId + ". Finishing...");
            finish();
            return;
        }
        mContacts = mConv.getRecipients();
        getListView().setAdapter(new RecipientListAdapter(this, R.layout.recipient_list_item,
                mContacts));

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        int cnt = mContacts.size();
        actionBar.setSubtitle(getResources().getQuantityString(R.plurals.recipient_count,
                cnt, cnt));

        /// M: fix bug ALPS00433593, refresh View when update Contact
        Contact.addListener(this);
        mFirstEnterActivity = true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(ComposeMessageActivity.THREAD_ID, mThreadId);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingListActivity.class);
                startActivityIfNeeded(intent, -1);
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.recipient_list_menu, menu);
        return true;
    }

    /// M: fix bug ALPS00433593, refresh View when update Contact @{
    public void onUpdate(Contact updated) {
        Log.v(TAG, "Contact onUpdate");
        mHandler.post(new Runnable() {
            public void run() {
                getListView().invalidateViews();
            }
        });
    }

    private boolean mFirstEnterActivity;

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        if (!mFirstEnterActivity) {
            Log.v(TAG, "onStart: Contact reload thread start");
            new Thread(new Runnable() {
                public void run() {
                    mContacts = mConv.getRecipients();
                    for (int i = 0; i < mContacts.size(); i++) {
                        mContacts.get(i).reload(false);
                    }
                }
            }).start();
        }
        mFirstEnterActivity = false;
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        Contact.removeListener(this);
        super.onDestroy();
    }
    /// @}

    private static class RecipientListAdapter extends ArrayAdapter<Contact> {
        private final int mResourceId;
        private final LayoutInflater mInflater;
        private final Drawable mDefaultContactImage;

        public RecipientListAdapter(Context context, int resource,
                ContactList recipients) {
            super(context, resource, recipients);

            mResourceId = resource;
            mInflater = LayoutInflater.from(context);
            mDefaultContactImage =
                    context.getResources().getDrawable(R.drawable.ic_contact_picture);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View listItemView =  mInflater.inflate(mResourceId, null);

            final TextView nameView = (TextView)listItemView.findViewById(R.id.name);
            final TextView numberView = (TextView)listItemView.findViewById(R.id.number);

            final Contact contact = getItem(position);
            final String name = contact.getName();
            final String number = contact.getNumber();
            if (!name.equals(number)) {
                nameView.setText(name);
                numberView.setText(number);
            } else {
                nameView.setText(number);
                numberView.setText(null);
            }

            QuickContactBadge badge = (QuickContactBadge)listItemView.findViewById(R.id.avatar);
            if (contact.existsInDatabase()) {
                badge.assignContactUri(contact.getUri());
            } else {
                badge.assignContactFromPhone(contact.getNumber(), true);
            }
            final Drawable avatarDrawable = contact.getAvatar(getContext(), mDefaultContactImage, -1);
            badge.setImageDrawable(avatarDrawable);

            return listItemView;
        }
    }
}
