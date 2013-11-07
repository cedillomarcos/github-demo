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
package com.android.settings.inputmethod;

import android.app.Fragment;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;

import com.android.settings.R;
import com.android.settings.inputmethod.UserDictionaryAddWordContents.LocaleRenderer;
import com.mediatek.xlog.Xlog;

import java.util.ArrayList;
import java.util.Locale;
//GPBLJ-177 liyang 20130428 (on)
import android.widget.Toast;
//GPBLJ-177 liyang 20130428 (off)
/**
 * Fragment to add a word/shortcut to the user dictionary.
 *
 * As opposed to the UserDictionaryActivity, this is only invoked within Settings
 * from the UserDictionarySettings.
 */
public class UserDictionaryAddWordFragment extends Fragment
        implements AdapterView.OnItemSelectedListener,
        com.android.internal.app.LocalePicker.LocaleSelectionListener {

    private static final int OPTIONS_MENU_DELETE = Menu.FIRST;

    private UserDictionaryAddWordContents mContents;
    private View mRootView;
    private boolean mIsDeleting = false;
    private InputMethodManager mImm;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        Context ctx = getActivity();
        mImm = (InputMethodManager)getActivity().getSystemService(ctx.INPUT_METHOD_SERVICE);
        Xlog.d("InputMethod", "onActivityCreated input manager " + ((mImm == null) ? "is" : "is not") + " null");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedState) {
        mRootView = inflater.inflate(R.layout.user_dictionary_add_word_fullscreen, null);
        mIsDeleting = false;
        if (null == mContents) {
            mContents = new UserDictionaryAddWordContents(mRootView, getArguments());
        }
        return mRootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem actionItem = menu.add(0, OPTIONS_MENU_DELETE, 0, R.string.delete)
                .setIcon(android.R.drawable.ic_menu_delete);
        actionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM |
                MenuItem.SHOW_AS_ACTION_WITH_TEXT);
    }

    /**
     * Callback for the framework when a menu option is pressed.
     *
     * This class only supports the delete menu item.
     * @param MenuItem the item that was pressed
     * @return false to allow normal menu processing to proceed, true to consume it here
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == OPTIONS_MENU_DELETE) {
	    //GPBLJ-177 liyang 20130428 (on)
	    if(mContents.ismWordEditTextNull()){
			Toast.makeText(getActivity(), R.string.no_word, Toast.LENGTH_SHORT).show();
	    }
	    //GPBLJ-177 liyang 20130428 (off)
            mContents.delete(getActivity());
            mIsDeleting = true;
            getActivity().onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        // We are being shown: display the word
        updateSpinner();
    }

    private void updateSpinner() {
        final ArrayList<LocaleRenderer> localesList = mContents.getLocalesList(getActivity());

        final Spinner localeSpinner =
                (Spinner)mRootView.findViewById(R.id.user_dictionary_add_locale);
        final ArrayAdapter<LocaleRenderer> adapter = new ArrayAdapter<LocaleRenderer>(getActivity(),
                android.R.layout.simple_spinner_item, localesList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        localeSpinner.setAdapter(adapter);
        localeSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        ///M: add by MTK to solve bug
        boolean autoHide = getResources().getBoolean(R.bool.auto_hide_keyboard_when_onpause);
        if (autoHide) {
            Xlog.d("InputMethod", "onPause input manager " + ((mImm == null) ? "is" : "is not") + " null");
            if(mImm != null) {
                mImm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            }
        }
        // We are being hidden: commit changes to the user dictionary, unless we were deleting it
        if (!mIsDeleting) {
            mContents.apply(getActivity(), null);
        }
    }

    @Override
    public void onItemSelected(final AdapterView<?> parent, final View view, final int pos,
            final long id) {
        final LocaleRenderer locale = (LocaleRenderer)parent.getItemAtPosition(pos);
        if (locale.isMoreLanguages()) {
            PreferenceActivity preferenceActivity = (PreferenceActivity)getActivity();
            preferenceActivity.startPreferenceFragment(new UserDictionaryLocalePicker(this), true);
        } else {
            mContents.updateLocale(locale.getLocaleString());
        }
    }

    @Override
    public void onNothingSelected(final AdapterView<?> parent) {
        // I'm not sure we can come here, but if we do, that's the right thing to do.
        final Bundle args = getArguments();
        mContents.updateLocale(args.getString(UserDictionaryAddWordContents.EXTRA_LOCALE));
    }

    // Called by the locale picker
    @Override
    public void onLocaleSelected(final Locale locale) {
        mContents.updateLocale(locale.toString());
        getActivity().onBackPressed();
    }
}
