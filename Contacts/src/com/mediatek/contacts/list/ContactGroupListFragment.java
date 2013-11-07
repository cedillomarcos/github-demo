
package com.mediatek.contacts.list;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ListView;

import com.android.contacts.R;
import com.android.contacts.group.GroupBrowseListAdapter;
import com.android.contacts.group.GroupBrowseListFragment;
import com.android.contacts.group.GroupListItem;
import com.android.contacts.util.WeakAsyncTask;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

public class ContactGroupListFragment extends GroupBrowseListFragment {

    private static final String TAG = ContactGroupListFragment.class.getSimpleName();

    private Context mContext;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

    @Override
    protected GroupBrowseListAdapter configAdapter() {
        return new ContactGroupListAdapter(mContext);
    }

    @Override
    protected OnItemClickListener configOnItemClickListener() {
        return new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CheckBox checkbox = (CheckBox) view.findViewById(android.R.id.checkbox);
                checkbox.setChecked(getListView().isItemChecked(position));
            }
        };
    }

    public void onOkClick() {
        if (getListView().getCheckedItemCount() == 0) {
            getActivity().setResult(Activity.RESULT_CANCELED, null);
            getActivity().finish();
        }
        String[] groupIds = new String[getListView().getCheckedItemCount()];
        GroupBrowseListAdapter adapter = (GroupBrowseListAdapter) getListView().getAdapter();
        int listSize = getListView().getCount();

        int groupIndex = 0;
        for (int position = 0; position < listSize; ++position) {
            if (getListView().isItemChecked(position)) {
                GroupListItem item = adapter.getItem(position);
                groupIds[groupIndex++] = String.valueOf(item.getGroupId());
            }
        }
        new GroupQueryTask(getActivity()).execute(groupIds);
    }

    public class GroupQueryTask extends WeakAsyncTask<String[], Void, long[], Activity> {
        private WeakReference<ProgressDialog> mProgress;

        public GroupQueryTask(Activity target) {
            super(target);
        }

        @Override
        protected void onPreExecute(final Activity target) {
            ProgressDialog progressDlg = ProgressDialog.show(target, null, target
                    .getText(R.string.please_wait), false, false);
            mProgress = new WeakReference<ProgressDialog>(progressDlg);
            super.onPreExecute(target);
        }

        @Override
        protected long[] doInBackground(Activity target, String[]... params) {
            String[] groupIds = params[0];

            if (groupIds == null || groupIds.length == 0) {
                Log.d(TAG, "doInBackground groupIds is empty");
                return null;
            }

            ArrayList<String> rawContactIdsList = new ArrayList<String>();
            ArrayList<Long> phoneIdsList = new ArrayList<Long>();

            final StringBuilder whereBuilder = new StringBuilder();

            final String[] groupQuestionMarks = new String[groupIds.length];
            Arrays.fill(groupQuestionMarks, "?");

            whereBuilder.append(Data.MIMETYPE + "='" + GroupMembership.CONTENT_ITEM_TYPE + "'");
            whereBuilder.append(" AND ");
            whereBuilder.append(Data.DATA1 + " IN (");
            whereBuilder.append(TextUtils.join(",", groupQuestionMarks));
            whereBuilder.append(")");

            Log.i(TAG, whereBuilder.toString());

            Cursor cursor = null;
            try {
                cursor = mContext.getContentResolver().query(Data.CONTENT_URI, 
                        new String[] { Data.RAW_CONTACT_ID }, 
                        whereBuilder.toString(), groupIds, null);
                if (cursor != null) {
                    cursor.moveToPosition(-1);
                    while (cursor.moveToNext()) {
                        long id = cursor.getLong(0);
                        Log.i(TAG, "rawContactid is " + String.valueOf(id));
                        rawContactIdsList.add(String.valueOf(id));
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }
            }

            if (rawContactIdsList.isEmpty()) {
                Log.d(TAG, "doInBackground rawContactIdsList is empty");
                return null;
            }

            final String[] rawContactQuestionMarks = new String[rawContactIdsList.size()];
            Arrays.fill(rawContactQuestionMarks, "?");

            whereBuilder.delete(0, whereBuilder.length());

            whereBuilder.append("(" + Data.MIMETYPE + " ='");
            whereBuilder.append(Phone.CONTENT_ITEM_TYPE + "' OR ");
            whereBuilder.append(Data.MIMETYPE + " ='");
            whereBuilder.append(Email.CONTENT_ITEM_TYPE + "') ");
            whereBuilder.append("AND " + Data.RAW_CONTACT_ID + " IN (");
            whereBuilder.append(TextUtils.join(",", rawContactQuestionMarks));
            whereBuilder.append(")");

            Log.i(TAG, whereBuilder.toString());

            try {
                cursor = mContext.getContentResolver().query(Data.CONTENT_URI, 
                        new String[] { Data._ID }, 
                        whereBuilder.toString(), 
                        rawContactIdsList.toArray(new String[0]), null);
                if (cursor != null) {
                    cursor.moveToPosition(-1);
                    while (cursor.moveToNext()) {
                        long id = cursor.getLong(0);
                        Log.i(TAG, "dataId is " + String.valueOf(id));
                        phoneIdsList.add(Long.valueOf(id));
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }
            }

            Log.d(TAG, "phoneIdsList size is " + phoneIdsList.size());
            long[] phoneIds = new long[phoneIdsList.size()];
            int index = 0;
            for (Long id : phoneIdsList) {
                phoneIds[index++] = id.longValue();
            }

            return phoneIds;
        }

        @Override
        protected void onPostExecute(final Activity target, long[] ids) {
            final ProgressDialog progress = mProgress.get();
            if (!target.isFinishing() && progress != null && progress.isShowing()) {
                progress.dismiss();
            }
            super.onPostExecute(target, ids);
            if (ids == null || ids.length == 0) {
                getActivity().setResult(Activity.RESULT_CANCELED, new Intent());
            } else {
                getActivity().setResult(
                        ContactListMultiChoiceActivity.CONTACTGROUPLISTACTIVITY_RESULT_CODE,
                        new Intent().putExtra("checkedids", ids));
            }
            target.finish();
        }
    }
}
