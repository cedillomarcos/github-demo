package com.mediatek.contacts.list;

import android.content.Context;

import com.android.contacts.R;

import com.android.contacts.group.GroupBrowseListAdapter;

public class ContactGroupListAdapter extends GroupBrowseListAdapter {

    public ContactGroupListAdapter(Context context) {
        super(context);
    }

    @Override
    protected int getGroupListItemLayout() {
        return R.layout.group_browse_list_item_with_checkbox;
    }

}
