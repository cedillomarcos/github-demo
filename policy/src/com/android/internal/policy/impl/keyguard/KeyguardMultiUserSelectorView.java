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

package com.android.internal.policy.impl.keyguard;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.content.pm.UserInfo;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.android.internal.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public class KeyguardMultiUserSelectorView extends FrameLayout implements View.OnClickListener {
    private static final String TAG = "KeyguardMultiUserSelectorView";

    private ViewGroup mUsersGrid;
    private KeyguardMultiUserAvatar mActiveUserAvatar;
    private KeyguardHostView.UserSwitcherCallback mCallback;
    private static final int FADE_OUT_ANIMATION_DURATION = 100;

    public KeyguardMultiUserSelectorView(Context context) {
        this(context, null, 0);
    }

    public KeyguardMultiUserSelectorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KeyguardMultiUserSelectorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onFinishInflate () {
        int resId = R.id.keyguard_users_grid;
        if(KeyguardUpdateMonitor.isAlarmBoot()){
            resId = com.mediatek.internal.R.id.keyguard_users_grid;
        }
        mUsersGrid = (ViewGroup) findViewById(resId);
        mUsersGrid.removeAllViews();
        setClipChildren(false);
        setClipToPadding(false);

    }

    public void setCallback(KeyguardHostView.UserSwitcherCallback callback) {
        mCallback = callback;
    }

    public void addUsers(Collection<UserInfo> userList) {
        UserInfo activeUser;
        try {
            activeUser = ActivityManagerNative.getDefault().getCurrentUser();
        } catch (RemoteException re) {
            activeUser = null;
        }

        ArrayList<UserInfo> users = new ArrayList<UserInfo>(userList);
        Collections.sort(users, mOrderAddedComparator);

        for (UserInfo user: users) {
            KeyguardMultiUserAvatar uv = createAndAddUser(user);
            if (user.id == activeUser.id) {
                mActiveUserAvatar = uv;
                mActiveUserAvatar.setActive(true, false, null);
            } else {
                uv.setActive(false, false, null);
            }
        }
    }

    Comparator<UserInfo> mOrderAddedComparator = new Comparator<UserInfo>() {
        @Override
        public int compare(UserInfo lhs, UserInfo rhs) {
            return (lhs.serialNumber - rhs.serialNumber);
        }
    };

    private KeyguardMultiUserAvatar createAndAddUser(UserInfo user) {
        KeyguardMultiUserAvatar uv = KeyguardMultiUserAvatar.fromXml(
                R.layout.keyguard_multi_user_avatar, mContext, this, user);
        mUsersGrid.addView(uv);
        return uv;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if(event.getActionMasked() != MotionEvent.ACTION_CANCEL && mCallback != null) {
            mCallback.userActivity();
        }
        return false;
    }

    private void setAllClickable(boolean clickable)
    {
        for(int i = 0; i < mUsersGrid.getChildCount(); i++) {
            View v = mUsersGrid.getChildAt(i);
            v.setClickable(clickable);
            v.setPressed(false);
        }
    }

    @Override
    public void onClick(View v) {
        if (!(v instanceof KeyguardMultiUserAvatar)) return;
        final KeyguardMultiUserAvatar avatar = (KeyguardMultiUserAvatar) v;
        if (avatar.isClickable()) { // catch race conditions
            if (mActiveUserAvatar == avatar) {
                // If they click the currently active user, show the unlock hint
                mCallback.showUnlockHint();
                return;
            } else {
                // Reset the previously active user to appear inactive
                mCallback.hideSecurityView(FADE_OUT_ANIMATION_DURATION);
                setAllClickable(false);
                mActiveUserAvatar.setActive(false, true, new Runnable() {
                    @Override
                    public void run() {
                        mActiveUserAvatar = avatar;
                        mActiveUserAvatar.setActive(true, true, new Runnable() {
                            @Override
                            public void run() {
                                if (this.getClass().getName().contains("internal")) {
                                    try {
                                        ActivityManagerNative.getDefault()
                                                .switchUser(avatar.getUserInfo().id);
                                    } catch (RemoteException re) {
                                        Log.e(TAG, "Couldn't switch user " + re);
                                    }
                                } else {
                                    setAllClickable(true);
                                }
                            }
                        });
                    }
                });
            }
        }
    }
}
