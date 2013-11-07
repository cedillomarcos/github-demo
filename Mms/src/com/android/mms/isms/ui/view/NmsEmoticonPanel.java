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

/*
 * Copyright (C) 2007-2008 Esmertec AG.
 * Copyright (C) 2007-2008 The Android Open Source Project
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
package com.android.mms.isms.ui.view;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.android.mms.MmsConfig;
import com.android.mms.R;
import com.android.mms.isms.ui.view.NmsLevelControlLayout.OnScrollToScreenListener;
import com.android.mms.isms.util.message.MessageConsts;
import com.mediatek.encapsulation.MmsLog;
import com.mediatek.mms.ipmessage.IpMessageConsts.IpMessageServiceId;

public class NmsEmoticonPanel extends LinearLayout implements OnCheckedChangeListener {

    private Handler mHandler;
    private Context mContext;
    private View convertView;
    private NmsLevelControlLayout mScrollLayout;
    private LinearLayout mSharePanelMain;
    private RadioButton mDotFirst;
    private RadioButton mDotSec;
    private RadioButton mDotThird;
    private RadioButton mDotForth;
    private Button mDelEmoticon;
    private RadioButton mNormalTab;
    private RadioButton mLargeTab;
    private RadioButton mDynamicTab;
    private RadioButton mXmTab;
    private RadioButton mAdTab;

    private int mOrientation = 0;
    private int[] mColumnArray;
    private String[] mEmoticonName;
    private String[] mLargeName;
    private String[] mDynamicName;
    private String[] mAdName;
    private String[] mXmName;
    private int mNormalIndex = 0;
    private int mLargeIndex = 0;
    private int mDynamicIndex = 0;
    private int mAdIndex = 0;
    private int mXmIndex = 0;
    private EditEmoticonListener mListener;
    private NmsEmoticonPreview mPreview;

    private DelEmoticonThread mDelEmoticonThread;
    private Object mObject = new Object();
    private boolean mNeedQuickDelete = false;


    private final static String TAG = "Mms/EmoticonPanel";

    public NmsEmoticonPanel(Context context) {
        super(context);
        mContext = context;
    }

    public NmsEmoticonPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        if (MmsConfig.getIpMessagServiceId(context) == IpMessageServiceId.NO_SERVICE) {
            convertView = inflater.inflate(R.layout.emoticon_panel_common, this, true);
        } else {
            convertView = inflater.inflate(R.layout.emoticon_panel, this, true);
        }
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mScrollLayout = (NmsLevelControlLayout) convertView.findViewById(R.id.emoticon_panel_zone);
        mSharePanelMain = (LinearLayout) convertView.findViewById(R.id.ll_emoticon_panel);
        mDotFirst = (RadioButton) convertView.findViewById(R.id.rb_dot_first);
        mDotSec = (RadioButton) convertView.findViewById(R.id.rb_dot_sec);
        mDotThird = (RadioButton) convertView.findViewById(R.id.rb_dot_third);
        mDotForth = (RadioButton) convertView.findViewById(R.id.rb_dot_forth);
        mNormalTab = (RadioButton) convertView.findViewById(R.id.smiley_panel_normal_btn);
        mLargeTab = (RadioButton) convertView.findViewById(R.id.smiley_panel_large_btn);
        mDynamicTab = (RadioButton) convertView.findViewById(R.id.smiley_panel_dynamic_btn);
        mXmTab = (RadioButton) convertView.findViewById(R.id.smiley_panel_xm_btn);
        mAdTab = (RadioButton) convertView.findViewById(R.id.smiley_panel_ad_btn);
        mEmoticonName = getResources().getStringArray(R.array.emoticon_name);
        mLargeName = getResources().getStringArray(R.array.large_emoticon_name);
        mDynamicName = getResources().getStringArray(R.array.dynamic_emoticon_name);
        mAdName = getResources().getStringArray(R.array.ad_emoticon_name);
        mXmName = getResources().getStringArray(R.array.xm_emoticon_name);
        /// M: add default/gift panel for common version. @{
        mDefaultTab = (RadioButton) convertView.findViewById(R.id.smiley_panel_default_btn);
        mGiftTab = (RadioButton) convertView.findViewById(R.id.smiley_panel_gift_btn);
        mDefaultName = getResources().getStringArray(R.array.default_emoticon_name);
        mGiftName = getResources().getStringArray(R.array.gift_emoticon_name);
        OnClickListener panelClickListener = new LinearLayout.OnClickListener() {
            public void onClick(View v) {
                int clickedId = v.getId();
                if (R.id.default_panel == clickedId) {
                    mDefaultTab.setChecked(true);
                } else if (R.id.gift_panel == clickedId) {
                    mGiftTab.setChecked(true);
                }
            }
        };
        LinearLayout defaultPanel = (LinearLayout) convertView.findViewById(R.id.default_panel);
        LinearLayout giftPanel = (LinearLayout) convertView.findViewById(R.id.gift_panel);
        /// @}
        mDelEmoticon = (Button) convertView.findViewById(R.id.smiley_panel_del_btn);

        mDelEmoticon.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mListener.doAction(EditEmoticonListener.delEmoticon, "");
                    startDelEmoticon();
                    break;
                case MotionEvent.ACTION_UP:
                    stopDelEmoticon();
                    break;
                default:
                    break;
                }
                return false;
            }
        });
        if (MmsConfig.getIpMessagServiceId(mContext) == IpMessageServiceId.NO_SERVICE) {
            defaultPanel.setOnClickListener(panelClickListener);
            giftPanel.setOnClickListener(panelClickListener);
            mDefaultTab.setOnCheckedChangeListener(this);
            mGiftTab.setOnCheckedChangeListener(this);
        } else {
            mNormalTab.setOnCheckedChangeListener(this);
            mLargeTab.setOnCheckedChangeListener(this);
            mDynamicTab.setOnCheckedChangeListener(this);
            mXmTab.setOnCheckedChangeListener(this);
            mAdTab.setOnCheckedChangeListener(this);
        }

        mPreview = new NmsEmoticonPreview(mContext, this);
        resetShareItem();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int checkedId = buttonView.getId();
        if (!isChecked) {
            return;
        }
        if (MmsConfig.getIpMessagServiceId(mContext) == IpMessageServiceId.NO_SERVICE) {
            if (R.id.smiley_panel_default_btn == checkedId) {
                mDefaultTab.setChecked(true);
                mGiftTab.setChecked(false);
                addDefaultPanel();
            } else if (R.id.smiley_panel_gift_btn == checkedId) {
                mDefaultTab.setChecked(false);
                mGiftTab.setChecked(true);
                addGiftPanel();
            }
        } else {
            if (R.id.smiley_panel_normal_btn == checkedId) {
                mNormalTab.setChecked(true);
                mLargeTab.setChecked(false);
                mDynamicTab.setChecked(false);
                mXmTab.setChecked(false);
                mAdTab.setChecked(false);
                addNormalPanel();
            } else if (R.id.smiley_panel_large_btn == checkedId) {
                mNormalTab.setChecked(false);
                mLargeTab.setChecked(true);
                mDynamicTab.setChecked(false);
                mXmTab.setChecked(false);
                mAdTab.setChecked(false);
                addLargePanel();
            } else if (R.id.smiley_panel_dynamic_btn == checkedId) {
                mNormalTab.setChecked(false);
                mLargeTab.setChecked(false);
                mDynamicTab.setChecked(true);
                mXmTab.setChecked(false);
                mAdTab.setChecked(false);
                addDynamicPanel();
            } else if (R.id.smiley_panel_xm_btn == checkedId) {
                mNormalTab.setChecked(false);
                mLargeTab.setChecked(false);
                mDynamicTab.setChecked(false);
                mXmTab.setChecked(true);
                mAdTab.setChecked(false);
                addXmPanel();
            } else if (R.id.smiley_panel_ad_btn == checkedId) {
                mNormalTab.setChecked(false);
                mLargeTab.setChecked(false);
                mDynamicTab.setChecked(false);
                mXmTab.setChecked(false);
                mAdTab.setChecked(true);
                addAdPanel();
            }
        }
    }

    /**
     * Build share item.
     */
    public void resetShareItem() {
        if (MmsConfig.getIpMessagServiceId(mContext) == IpMessageServiceId.NO_SERVICE) {
            if (mDefaultTab.isChecked()) {
                addDefaultPanel();
            } else if (mGiftTab.isChecked()) {
                addGiftPanel();
            }
        } else {
            if (mNormalTab.isChecked()) {
                addNormalPanel();
            } else if (mLargeTab.isChecked()) {
                addLargePanel();
            } else if (mDynamicTab.isChecked()) {
                addDynamicPanel();
            } else if (mAdTab.isChecked()) {
                addAdPanel();
            } else if (mXmTab.isChecked()) {
                addXmPanel();
            }
        }
        mScrollLayout.autoRecovery();
    }

    /**
     * Display normal emoticon page.
     */
    private void addNormalPanel() {
        mColumnArray = getResources().getIntArray(R.array.emoticon_column);
        mOrientation = getResources().getConfiguration().orientation;
        if (mScrollLayout.getChildCount() != 0) {
            mScrollLayout.removeAllViews();
        }
        mScrollLayout.setDefaultScreen(mNormalIndex);
        mOrientation = getResources().getConfiguration().orientation;
        mDotFirst.setVisibility(View.VISIBLE);
        mDotSec.setVisibility(View.VISIBLE);
        mDotThird.setVisibility(View.VISIBLE);
        mDotForth.setVisibility(View.VISIBLE);
        int num = calculateNormalPageCount(mOrientation);
        for (int i = 0; i < num; i++) {
            addNormalPage(i);
        }
        mScrollLayout.setToScreen(mNormalIndex);
        mScrollLayout.setOnScrollToScreen(new OnScrollToScreenListener() {
            @Override
            public void doAction(int whichScreen) {
                mNormalIndex = whichScreen;
                if (whichScreen == 0) {
                    mDotFirst.setChecked(true);
                } else if (whichScreen == 1) {
                    mDotSec.setChecked(true);
                } else if (whichScreen == 2) {
                    mDotThird.setChecked(true);
                } else {
                    mDotForth.setChecked(true);
                }
            }
        });
    }

    /**
     * Display large emoticon page.
     */
    private void addLargePanel() {
        mColumnArray = getResources().getIntArray(R.array.share_column);
        mOrientation = getResources().getConfiguration().orientation;
        if (mScrollLayout.getChildCount() != 0) {
            mScrollLayout.removeAllViews();
        }
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            int num = calculateLargePageCount(mOrientation);
            for (int i = 0; i < num; i++) {
                addLargePage(i);
            }
            mDotSec.setVisibility(View.VISIBLE);
            mDotFirst.setVisibility(View.VISIBLE);
            mDotThird.setVisibility(View.VISIBLE);
            mDotForth.setVisibility(View.GONE);
            if (mLargeIndex == 3) {
                mLargeIndex = 2;
            }
            mScrollLayout.setToScreen(mLargeIndex);
            mScrollLayout.setOnScrollToScreen(new OnScrollToScreenListener() {
                @Override
                public void doAction(int whichScreen) {
                    mLargeIndex = whichScreen;
                    if (whichScreen == 0) {
                        mDotFirst.setChecked(true);
                    } else if (whichScreen == 1) {
                        mDotSec.setChecked(true);
                    } else if (whichScreen == 2) {
                        mDotThird.setChecked(true);
                    } else {
                        mDotForth.setChecked(true);
                    }
                }
            });
        } else {
            int num = calculateLargePageCount(mOrientation);
            for (int i = 0; i < num; i++) {
                addLargePage(i);
            }
            mDotSec.setVisibility(View.VISIBLE);
            mDotFirst.setVisibility(View.VISIBLE);
            mDotThird.setVisibility(View.VISIBLE);
            mDotForth.setVisibility(View.VISIBLE);
            mDotFirst.setChecked(true);
            mScrollLayout.setToScreen(mLargeIndex);
            mScrollLayout.setOnScrollToScreen(new OnScrollToScreenListener() {
                @Override
                public void doAction(int whichScreen) {
                    mLargeIndex = whichScreen;
                    if (whichScreen == 0) {
                        mDotFirst.setChecked(true);
                    } else if (whichScreen == 1) {
                        mDotSec.setChecked(true);
                    } else if (whichScreen == 2) {
                        mDotThird.setChecked(true);
                    } else {
                        mDotForth.setChecked(true);
                    }
                }
            });
        }
        mScrollLayout.setDefaultScreen(mLargeIndex);
        if (mLargeIndex == 0) {
            mDotFirst.setChecked(true);
        } else if (mLargeIndex == 1) {
            mDotSec.setChecked(true);
        } else if (mLargeIndex == 2) {
            mDotThird.setChecked(true);
        } else {
            mDotForth.setChecked(true);
        }
    }

    /**
     * Display animation emoticon page.
     */
    private void addDynamicPanel() {
        mColumnArray = getResources().getIntArray(R.array.share_column);
        mOrientation = getResources().getConfiguration().orientation;
        if (mScrollLayout.getChildCount() != 0) {
            mScrollLayout.removeAllViews();
        }
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            int num = calculateDynamicPageCount(mOrientation);
            for (int i = 0; i < num; i++) {
                addDynamicPage(i);
            }
            mDotSec.setVisibility(View.VISIBLE);
            mDotFirst.setVisibility(View.VISIBLE);
            mDotThird.setVisibility(View.VISIBLE);
            mDotForth.setVisibility(View.GONE);
            if (mDynamicIndex == 3) {
                mDynamicIndex = 2;
            }
            mScrollLayout.setToScreen(mDynamicIndex);
            mScrollLayout.setOnScrollToScreen(new OnScrollToScreenListener() {
                @Override
                public void doAction(int whichScreen) {
                    mDynamicIndex = whichScreen;
                    if (whichScreen == 0) {
                        mDotFirst.setChecked(true);
                    } else if (whichScreen == 1) {
                        mDotSec.setChecked(true);
                    } else if (whichScreen == 2) {
                        mDotThird.setChecked(true);
                    } else {
                        mDotForth.setChecked(true);
                    }
                }
            });
        } else {
            int num = calculateDynamicPageCount(mOrientation);
            for (int i = 0; i < num; i++) {
                addDynamicPage(i);
            }
            mDotSec.setVisibility(View.VISIBLE);
            mDotFirst.setVisibility(View.VISIBLE);
            mDotThird.setVisibility(View.VISIBLE);
            mDotForth.setVisibility(View.VISIBLE);
            mScrollLayout.setToScreen(mDynamicIndex);
            mScrollLayout.setOnScrollToScreen(new OnScrollToScreenListener() {
                @Override
                public void doAction(int whichScreen) {
                    mDynamicIndex = whichScreen;
                    if (whichScreen == 0) {
                        mDotFirst.setChecked(true);
                    } else if (whichScreen == 1) {
                        mDotSec.setChecked(true);
                    } else if (whichScreen == 2) {
                        mDotThird.setChecked(true);
                    } else {
                        mDotForth.setChecked(true);
                    }
                }
            });
        }
        mScrollLayout.setDefaultScreen(mDynamicIndex);
        if (mDynamicIndex == 0) {
            mDotFirst.setChecked(true);
        } else if (mDynamicIndex == 1) {
            mDotSec.setChecked(true);
        } else if (mDynamicIndex == 2) {
            mDotThird.setChecked(true);
        } else {
            mDotForth.setChecked(true);
        }
    }

    private void addAdPanel() {
        mColumnArray = getResources().getIntArray(R.array.share_column);
        mOrientation = getResources().getConfiguration().orientation;
        if (mScrollLayout.getChildCount() != 0) {
            mScrollLayout.removeAllViews();
        }
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            int num = calculateDynamicPageCount(mOrientation);
            for (int i = 0; i < num; i++) {
                addAdPage(i);
            }
            mDotSec.setVisibility(View.VISIBLE);
            mDotFirst.setVisibility(View.VISIBLE);
            mDotThird.setVisibility(View.VISIBLE);
            mDotForth.setVisibility(View.GONE);
            if (mAdIndex == 3) {
                mAdIndex = 2;
            }
            mScrollLayout.setToScreen(mAdIndex);
            mScrollLayout.setOnScrollToScreen(new OnScrollToScreenListener() {
                @Override
                public void doAction(int whichScreen) {
                    mAdIndex = whichScreen;
                    if (whichScreen == 0) {
                        mDotFirst.setChecked(true);
                    } else if (whichScreen == 1) {
                        mDotSec.setChecked(true);
                    } else if (whichScreen == 2) {
                        mDotThird.setChecked(true);
                    } else {
                        mDotForth.setChecked(true);
                    }
                }
            });
        } else {
            int num = calculateDynamicPageCount(mOrientation);
            for (int i = 0; i < num; i++) {
                addAdPage(i);
            }
            mDotSec.setVisibility(View.VISIBLE);
            mDotFirst.setVisibility(View.VISIBLE);
            mDotThird.setVisibility(View.VISIBLE);
            mDotForth.setVisibility(View.VISIBLE);
            mScrollLayout.setToScreen(mAdIndex);
            mScrollLayout.setOnScrollToScreen(new OnScrollToScreenListener() {
                @Override
                public void doAction(int whichScreen) {
                    mAdIndex = whichScreen;
                    if (whichScreen == 0) {
                        mDotFirst.setChecked(true);
                    } else if (whichScreen == 1) {
                        mDotSec.setChecked(true);
                    } else if (whichScreen == 2) {
                        mDotThird.setChecked(true);
                    } else {
                        mDotForth.setChecked(true);
                    }
                }
            });
        }
        mScrollLayout.setDefaultScreen(mAdIndex);
        if (mAdIndex == 0) {
            mDotFirst.setChecked(true);
        } else if (mAdIndex == 1) {
            mDotSec.setChecked(true);
        } else if (mAdIndex == 2) {
            mDotThird.setChecked(true);
        } else {
            mDotForth.setChecked(true);
        }
    }

    private void addXmPanel() {
        mColumnArray = getResources().getIntArray(R.array.share_column);
        mOrientation = getResources().getConfiguration().orientation;
        if (mScrollLayout.getChildCount() != 0) {
            mScrollLayout.removeAllViews();
        }
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            int num = calculateDynamicPageCount(mOrientation);
            for (int i = 0; i < num; i++) {
                addXmPage(i);
            }
            mDotSec.setVisibility(View.VISIBLE);
            mDotFirst.setVisibility(View.VISIBLE);
            mDotThird.setVisibility(View.VISIBLE);
            mDotForth.setVisibility(View.GONE);
            if (mXmIndex == 3) {
                mXmIndex = 2;
            }
            mScrollLayout.setToScreen(mXmIndex);
            mScrollLayout.setOnScrollToScreen(new OnScrollToScreenListener() {
                @Override
                public void doAction(int whichScreen) {
                    mXmIndex = whichScreen;
                    if (whichScreen == 0) {
                        mDotFirst.setChecked(true);
                    } else if (whichScreen == 1) {
                        mDotSec.setChecked(true);
                    } else if (whichScreen == 2) {
                        mDotThird.setChecked(true);
                    } else {
                        mDotForth.setChecked(true);
                    }
                }
            });
        } else {
            int num = calculateDynamicPageCount(mOrientation);
            for (int i = 0; i < num; i++) {
                addXmPage(i);
            }
            mDotSec.setVisibility(View.VISIBLE);
            mDotFirst.setVisibility(View.VISIBLE);
            mDotThird.setVisibility(View.VISIBLE);
            mDotForth.setVisibility(View.VISIBLE);
            mScrollLayout.setToScreen(mXmIndex);
            mScrollLayout.setOnScrollToScreen(new OnScrollToScreenListener() {
                @Override
                public void doAction(int whichScreen) {
                    mXmIndex = whichScreen;
                    if (whichScreen == 0) {
                        mDotFirst.setChecked(true);
                    } else if (whichScreen == 1) {
                        mDotSec.setChecked(true);
                    } else if (whichScreen == 2) {
                        mDotThird.setChecked(true);
                    } else {
                        mDotForth.setChecked(true);
                    }
                }
            });
        }
        mScrollLayout.setDefaultScreen(mAdIndex);
        if (mXmIndex == 0) {
            mDotFirst.setChecked(true);
        } else if (mXmIndex == 1) {
            mDotSec.setChecked(true);
        } else if (mXmIndex == 2) {
            mDotThird.setChecked(true);
        } else {
            mDotForth.setChecked(true);
        }
    }

    /**
     * Return the count of normal emoticon page.
     *
     * @param orientation
     *            The screen orientation.
     * @return The count of normal emoticon page.
     */
    private int calculateNormalPageCount(int orientation) {
        int onePage;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            onePage = mColumnArray[0] * 4;
        } else {
            onePage = mColumnArray[1] * 2;
        }
        int total = MessageConsts.emoticonIdList.length;
        int count = total / onePage;
        if (total > count * onePage) {
            count++;
        }
        return count;
    }

    /**
     * Return the count of large emoticon page.
     *
     * @param orientation
     *            The screen orientation.
     *
     * @return The count of large emoticon page.
     */
    private int calculateLargePageCount(int orientation) {
        int onePage;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            onePage = mColumnArray[0] * 2;
        } else {
            onePage = mColumnArray[1];
        }
        int total = MessageConsts.largeIconArr.length;
        int count = total / onePage;
        if (total > count * onePage) {
            count++;
        }
        return count;
    }

    /**
     * Return count of animation emoticon page.
     *
     * @param orientation
     *            The screen orientation
     * @return The count of animation emoticon page.
     */
    private int calculateDynamicPageCount(int orientation) {
        int onePage;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            onePage = mColumnArray[0] * 2;
        } else {
            onePage = mColumnArray[1];
        }
        int total = MessageConsts.dynamicIconArr.length;
        int count = total / onePage;
        if (total > count * onePage) {
            count++;
        }
        return count;
    }

    /**
     * Create the child of normal emoticon page, and add it to parent.
     *
     * @param index
     *            the index of child.
     */
    private void addNormalPage(int index) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.normal_emoticon_flipper,
                mScrollLayout, false);
        GridView gridView = (GridView) v.findViewById(R.id.gv_normal_emoticon_gridview);
        android.view.ViewGroup.LayoutParams params = mSharePanelMain.getLayoutParams();
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            params.height = getResources().getDimensionPixelOffset(R.dimen.share_panel_lan_height);
        } else {
            params.height = getResources().getDimensionPixelOffset(R.dimen.share_panel_port_height);
        }

        mSharePanelMain.setLayoutParams(params);
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            gridView.setNumColumns(mColumnArray[0]);
        } else {
            gridView.setNumColumns(mColumnArray[1]);
        }
        EmoticonAdapter adapter = new EmoticonAdapter(getNormalIconArray(index));
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = getEmoticonName(position);
                if (TextUtils.isEmpty(name)) {
                    return;
                }
                mListener.doAction(EditEmoticonListener.addEmoticon, name);
            }
        });
        mScrollLayout.addView(v);
    }

    /**
     * Create the child of large emoticon page, and add it to parent.
     *
     * @param index
     *            the index of child.
     */
    private void addLargePage(int index) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.large_emoticon_flipper,
                mScrollLayout, false);
        GridView gridView = (GridView) v.findViewById(R.id.gv_large_emoticon_gridview);
        android.view.ViewGroup.LayoutParams params = mSharePanelMain.getLayoutParams();
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            params.height = getResources().getDimensionPixelOffset(R.dimen.share_panel_lan_height);
        } else {
            params.height = getResources().getDimensionPixelOffset(R.dimen.share_panel_port_height);
        }

        mSharePanelMain.setLayoutParams(params);
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            gridView.setNumColumns(mColumnArray[0]);
        } else {
            gridView.setNumColumns(mColumnArray[1]);
        }
        LargeAdapter adapter = new LargeAdapter(getLargeIconArray(index));
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!mPreview.isShow()) {
                    String name = getLargeName(position);
                    mListener.doAction(EditEmoticonListener.sendEmoticon, name);
                }
            }
        });
        mScrollLayout.addView(v);
    }

    /**
     * Create the child of animation emoticon page, and add it to parent.
     *
     * @param index
     *            the index of child.
     */
    private void addDynamicPage(int index) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.dynamic_emoticon_flipper,
                mScrollLayout, false);
        GridView gridView = (GridView) v.findViewById(R.id.gv_dynamic_emoticon_gridview);
        android.view.ViewGroup.LayoutParams params = mSharePanelMain.getLayoutParams();
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            params.height = getResources().getDimensionPixelOffset(R.dimen.share_panel_lan_height);
        } else {
            params.height = getResources().getDimensionPixelOffset(R.dimen.share_panel_port_height);
        }

        mSharePanelMain.setLayoutParams(params);
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            gridView.setNumColumns(mColumnArray[0]);
        } else {
            gridView.setNumColumns(mColumnArray[1]);
        }
        LargeAdapter adapter = new LargeAdapter(getDynamicIconArray(index));
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!mPreview.isShow()) {
                    String name = getLargeName(position);
                    mListener.doAction(EditEmoticonListener.sendEmoticon, name);
                }
            }
        });
        gridView.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                int resId = getPreviewIcon(position);
                if (resId == 0) {
                    return false;
                }
                mPreview.setEmoticon(resId);
                mPreview.showWindow();
                return true;
            }
        });

        gridView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                    mPreview.dissWindow();
                    return false;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    mPreview.dissWindow();
                    return false;
                } else {
                    return false;
                }
            }
        });
        mScrollLayout.addView(v);
    }

    private void addAdPage(int index) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.ad_emoticon_flipper, mScrollLayout,
                false);
        GridView gridView = (GridView) v.findViewById(R.id.gv_ad_emoticon_gridview);
        android.view.ViewGroup.LayoutParams params = mSharePanelMain.getLayoutParams();
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            params.height = getResources().getDimensionPixelOffset(R.dimen.share_panel_lan_height);
        } else {
            params.height = getResources().getDimensionPixelOffset(R.dimen.share_panel_port_height);
        }

        mSharePanelMain.setLayoutParams(params);
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            gridView.setNumColumns(mColumnArray[0]);
        } else {
            gridView.setNumColumns(mColumnArray[1]);
        }
        LargeAdapter adapter = new LargeAdapter(getAdIconArray(index));
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!mPreview.isShow()) {
                    String name = getLargeName(position);
                    mListener.doAction(EditEmoticonListener.sendEmoticon, name);
                }
            }
        });
        gridView.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                int resId = getPreviewIcon(position);
                if (resId == 0) {
                    return false;
                }
                mPreview.setEmoticon(resId);
                mPreview.showWindow();
                return true;
            }
        });

        gridView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                    mPreview.dissWindow();
                    return false;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    mPreview.dissWindow();
                    return false;
                } else {
                    return false;
                }
            }
        });
        mScrollLayout.addView(v);
    }

    private void addXmPage(int index) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.xm_emoticon_flipper, mScrollLayout,
                false);
        GridView gridView = (GridView) v.findViewById(R.id.gv_xm_emoticon_gridview);
        android.view.ViewGroup.LayoutParams params = mSharePanelMain.getLayoutParams();
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            params.height = getResources().getDimensionPixelOffset(R.dimen.share_panel_lan_height);
        } else {
            params.height = getResources().getDimensionPixelOffset(R.dimen.share_panel_port_height);
        }

        mSharePanelMain.setLayoutParams(params);
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            gridView.setNumColumns(mColumnArray[0]);
        } else {
            gridView.setNumColumns(mColumnArray[1]);
        }
        LargeAdapter adapter = new LargeAdapter(getXmIconArray(index));
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!mPreview.isShow()) {
                    String name = getLargeName(position);
                    mListener.doAction(EditEmoticonListener.sendEmoticon, name);
                }
            }
        });
        gridView.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                int resId = getPreviewIcon(position);
                if (resId == 0) {
                    return false;
                }
                mPreview.setEmoticon(resId);
                mPreview.showWindow();
                return true;
            }
        });

        gridView.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                    mPreview.dissWindow();
                    return false;
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    mPreview.dissWindow();
                    return false;
                } else {
                    return false;
                }
            }
        });
        mScrollLayout.addView(v);
    }

    /**
     * Return the resource id array of the normal emoticon page at index.
     *
     * @param index
     *            the visiable page index of normal emoticon page.
     * @return the resource id array.
     */
    private int[] getNormalIconArray(int index) {
        int[] source = MessageConsts.emoticonIdList;
        int onePage;
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            onePage = mColumnArray[0] * 4;
        } else {
            onePage = mColumnArray[1] * 2;
        }
        int[] arr = new int[onePage];
        for (int i = 0; i < onePage; i++) {
            int index1 = index * onePage + i;
            if (index1 >= source.length) {
                break;
            }
            arr[i] = source[index * onePage + i];
        }
        return arr;
    }

    /**
     * Return the resource id array of the large emoticon page at index.
     *
     * @param index
     *            the visiable page index of large emoticon page.
     * @return the resource id array.
     */
    private int[] getLargeIconArray(int index) {
        int[] source = MessageConsts.largeIconArr;
        int onePage;
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            onePage = mColumnArray[0] * 2;
        } else {
            onePage = mColumnArray[1];
        }
        int[] arr = new int[onePage];
        for (int i = 0; i < onePage; i++) {
            int index1 = index * onePage + i;
            if (index1 >= source.length) {
                break;
            }
            arr[i] = source[index * onePage + i];
        }
        return arr;
    }

    /**
     * Return the resource id array of the animation emoticon page at index.
     *
     * @param index
     *            the visiable page index of animation emoticon page.
     * @return the resource id array.
     */
    private int[] getDynamicIconArray(int index) {
        int[] source = MessageConsts.dynamicPngIconArr;
        int onePage;
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            onePage = mColumnArray[0] * 2;
        } else {
            onePage = mColumnArray[1];
        }
        int[] arr = new int[onePage];
        for (int i = 0; i < onePage; i++) {
            int index1 = index * onePage + i;
            if (index1 >= source.length) {
                break;
            }
            arr[i] = source[index * onePage + i];
        }
        return arr;
    }

    private int[] getAdIconArray(int index) {
        int[] source = MessageConsts.adPngIconArr;
        int onePage;
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            onePage = mColumnArray[0] * 2;
        } else {
            onePage = mColumnArray[1];
        }
        int[] arr = new int[onePage];
        for (int i = 0; i < onePage; i++) {
            int index1 = index * onePage + i;
            if (index1 >= source.length) {
                break;
            }
            arr[i] = source[index * onePage + i];
        }
        return arr;
    }

    private int[] getXmIconArray(int index) {
        int[] source = MessageConsts.xmPngIconArr;
        int onePage;
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            onePage = mColumnArray[0] * 2;
        } else {
            onePage = mColumnArray[1];
        }
        int[] arr = new int[onePage];
        for (int i = 0; i < onePage; i++) {
            int index1 = index * onePage + i;
            if (index1 >= source.length) {
                break;
            }
            arr[i] = source[index * onePage + i];
        }
        return arr;
    }

    /**
     * Return the coding of the normal emoticon.
     *
     * @param position
     *            the position of gridview.
     * @return the coding of the normal emoticon.
     */
    private String getEmoticonName(int position) {
        int onePage;
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            onePage = mColumnArray[0] * 4;
        } else {
            onePage = mColumnArray[1] * 2;
        }
        if (position >= 20) {
            return null;
        }
        int index = position + onePage * mNormalIndex;
        if (index >= mEmoticonName.length) {
            return null;
        }
        return mEmoticonName[index];
    }

    /**
     * Return the coding of the large emoticon and animation emoticon.
     *
     * @param position
     *            the position of gridview.
     * @return the coding of the large emoticon and animation emoticon.
     */
    private String getLargeName(int position) {
        int onePage;
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            onePage = mColumnArray[0] * 2;
        } else {
            onePage = mColumnArray[1];
        }
        if (mLargeTab.isChecked()) {
            if (position >= onePage) {
                return "";
            }
            return mLargeName[onePage * mLargeIndex + position];
        } else if (mDynamicTab.isChecked()) {
            if (position >= onePage) {
                return "";
            }
            return mDynamicName[onePage * mDynamicIndex + position];
        } else if (mAdTab.isChecked()) {
            if (position >= onePage) {
                return "";
            }
            return mAdName[onePage * mAdIndex + position];
        } else if (mXmTab.isChecked()) {
            if (position >= onePage) {
                return "";
            }
            return mXmName[onePage * mXmIndex + position];
        } else {
            return "";
        }
    }

    /**
     * Return the resource id of the animation emoticon at position.
     *
     * @param position
     *            the position of gridview.
     * @return The resource id.
     */
    private int getPreviewIcon(int position) {
        if (mLargeTab.isChecked()) {
            return 0;
        }
        int onePage;
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            onePage = mColumnArray[0] * 2;
        } else {
            onePage = mColumnArray[1];
        }

        if (position >= onePage) {
            return 0;
        }
        if (mDynamicTab.isChecked()) {
            return MessageConsts.dynamicIconArr[onePage * mDynamicIndex + position];
        } else if (mAdTab.isChecked()) {
            return MessageConsts.adIconArr[onePage * mAdIndex + position];
        } else if (mXmTab.isChecked()) {
            return MessageConsts.xmIconArr[onePage * mXmIndex + position];
        } else {
            return 0;
        }
    }

    /**
     * Sets the handler.
     *
     * @param handler
     *            the new handler
     */
    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    public void recycleView() {
        if (mScrollLayout != null && mScrollLayout.getChildCount() != 0) {
            mScrollLayout.removeAllViews();
        }
    }

    private class EmoticonAdapter extends BaseAdapter {

        private int[] iconArr;

        public EmoticonAdapter(int[] iconArray) {
            iconArr = iconArray;
        }

        @Override
        public int getCount() {
            return iconArr.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                if (MmsConfig.getIpMessagServiceId(mContext) == IpMessageServiceId.NO_SERVICE) {
                    if (mDefaultTab.isChecked()) {
                        convertView = LayoutInflater.from(mContext).inflate(
                                R.layout.default_emoticon_grid_item, null);
                        convertView.setTag(convertView);
                    } else if (mGiftTab.isChecked()) {
                        convertView = LayoutInflater.from(mContext).inflate(
                                R.layout.gift_emoticon_grid_item, null);
                        convertView.setTag(convertView);
                    }
                } else {
                    convertView = LayoutInflater.from(mContext).inflate(R.layout.emoticon_grid_item,
                            null);
                    convertView.setTag(convertView);
                }
            } else {
                convertView = (View) convertView.getTag();
            }

            ImageView ivPre = (ImageView) convertView.findViewById(R.id.iv_emoticon_icon);

            ivPre.setImageResource(iconArr[position]);
            return convertView;
        }
    }

    private class LargeAdapter extends BaseAdapter {

        private int[] iconArr;

        public LargeAdapter(int[] iconArray) {
            iconArr = iconArray;
        }

        @Override
        public int getCount() {
            return iconArr.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                if (mLargeTab.isChecked()) {
                    convertView = LayoutInflater.from(mContext).inflate(
                            R.layout.large_emoticon_grid_item, null);
                } else if (mDynamicTab.isChecked()) {
                    convertView = LayoutInflater.from(mContext).inflate(
                            R.layout.dynamic_emoticon_grid_item, null);
                } else if (mAdTab.isChecked()) {
                    convertView = LayoutInflater.from(mContext).inflate(
                            R.layout.ad_emoticon_grid_item, null);
                } else {
                    convertView = LayoutInflater.from(mContext).inflate(
                            R.layout.xm_emoticon_grid_item, null);
                }
                convertView.setTag(convertView);
            } else {
                convertView = (View) convertView.getTag();
            }
            final ImageView img = (ImageView) convertView.findViewById(R.id.iv_large_emoticon_icon);
            img.setImageResource(iconArr[position]);

            return convertView;
        }
    }

    public void setEditEmoticonListener(EditEmoticonListener l) {
        mListener = l;
    }

    /**
     * The listener interface for receiving editEmoticon events. The class that
     * is interested in processing a editEmoticon event implements this
     * interface, and the object created with that class is registered with a
     * component using the component's
     * <code>setEditEmoticonListener<code> method. When
     * the addEmotion, delEmoticon and sendEmoticon event occurs, that object's appropriate
     * method is invoked.
     *
     * @see EditEmoticonEvent
     */
    public interface EditEmoticonListener {

        public final static int addEmoticon = 0;
        public final static int delEmoticon = 1;
        public final static int sendEmoticon = 2;

        /**
         * Do edit emoticon action.
         *
         * @param type
         *            action type
         * @param emotionName
         *            the coding of emoticon
         */
        public void doAction(int type, String emotionName);
    }

    private void startDelEmoticon() {
        MmsLog.d(TAG, "Delete one emoticon.");
        if (mDelEmoticonThread != null) {
            synchronized (mDelEmoticonThread) {
                stopDelEmoticon();
            }
        }
        mNeedQuickDelete = true;
        mDelEmoticonThread = new DelEmoticonThread();
        synchronized (mDelEmoticonThread) {
            mDelEmoticonThread.start();
        }
    }

    private void stopDelEmoticon() {
        MmsLog.d(TAG, "Stop quick delete.");
        if (mDelEmoticonThread == null) {
            mNeedQuickDelete = false;
            return;
        }
        synchronized (mDelEmoticonThread) {
            mDelEmoticonThread.stopThread();
            mDelEmoticonThread = null;
        }
    }

    private class DelEmoticonThread extends Thread {
        private boolean mStopThread = false;

        public void stopThread() {
            mStopThread = true;
        }

        @Override
        public void run() {
            synchronized (mObject) {
                try {
                    MmsLog.d(TAG, "Wait for quick delete.");
                    mObject.wait(1000);
                } catch (InterruptedException e) {
                }
            }
            Object object = new Object();
            if (mNeedQuickDelete) {
                MmsLog.d(TAG, "Start quick delete. mStopThread = " + mStopThread);
                while (!mStopThread) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            MmsLog.d(TAG, "Quick delete emoticon.");
                            mListener.doAction(EditEmoticonListener.delEmoticon, "");
                        }
                    });
                    synchronized (object) {
                        try {
                            object.wait(100);
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
        }
    }

    /// M: add default/gift panel for common version. @{
    private RadioButton mDefaultTab;
    private RadioButton mGiftTab;
    private String[] mDefaultName;
    private String[] mGiftName;
    private int mDefaultIndex = 0;
    private int mGiftIndex = 0;
    /**
     * Display default emoticon page in common version.
     */
    private void addDefaultPanel() {
        mColumnArray = getResources().getIntArray(R.array.emoticon_column);
        mOrientation = getResources().getConfiguration().orientation;
        if (mScrollLayout.getChildCount() != 0) {
            mScrollLayout.removeAllViews();
        }
        mScrollLayout.setDefaultScreen(mDefaultIndex);
        mOrientation = getResources().getConfiguration().orientation;
        mDotFirst.setVisibility(View.VISIBLE);
        mDotSec.setVisibility(View.VISIBLE);
        mDotThird.setVisibility(View.GONE);
        mDotForth.setVisibility(View.GONE);
        int num = calculateDefaultPageCount(mOrientation);
        for (int i = 0; i < num; i++) {
            addDefaultPage(i);
        }
        mScrollLayout.setToScreen(mDefaultIndex);
        mScrollLayout.setOnScrollToScreen(new OnScrollToScreenListener() {
            @Override
            public void doAction(int whichScreen) {
                mDefaultIndex = whichScreen;
                if (whichScreen == 0) {
                    mDotFirst.setChecked(true);
                } else if (whichScreen == 1) {
                    mDotSec.setChecked(true);
                } else if (whichScreen == 2) {
                    mDotThird.setChecked(true);
                } else {
                    mDotForth.setChecked(true);
                }
            }
        });
    }

    /**
     * Display gift emoticon page in common version.
     */
    private void addGiftPanel() {
        mColumnArray = getResources().getIntArray(R.array.emoticon_column);
        mOrientation = getResources().getConfiguration().orientation;
        if (mScrollLayout.getChildCount() != 0) {
            mScrollLayout.removeAllViews();
        }
        mScrollLayout.setDefaultScreen(mGiftIndex);
        mOrientation = getResources().getConfiguration().orientation;
        mDotFirst.setVisibility(View.VISIBLE);
        mDotSec.setVisibility(View.VISIBLE);
        mDotThird.setVisibility(View.GONE);
        mDotForth.setVisibility(View.GONE);
        int num = calculateGiftPageCount(mOrientation);
        for (int i = 0; i < num; i++) {
            addGiftPage(i);
        }
        mScrollLayout.setToScreen(mGiftIndex);
        mScrollLayout.setOnScrollToScreen(new OnScrollToScreenListener() {
            @Override
            public void doAction(int whichScreen) {
                mGiftIndex = whichScreen;
                if (whichScreen == 0) {
                    mDotFirst.setChecked(true);
                } else if (whichScreen == 1) {
                    mDotSec.setChecked(true);
                } else if (whichScreen == 2) {
                    mDotThird.setChecked(true);
                } else {
                    mDotForth.setChecked(true);
                }
            }
        });
    }

    /**
     * Return the resource id array of the default emoticon page at index.
     *
     * @param index
     *            the visiable page index of default emoticon page.
     * @return the resource id array.
     */
    private int[] getDefaultIconArray(int index) {
        int[] source = MessageConsts.defaultIconArr;
        int onePage;
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            onePage = mColumnArray[0] * 4;
        } else {
            onePage = mColumnArray[1] * 2;
        }
        int[] arr = new int[onePage];
        for (int i = 0; i < onePage; i++) {
            int index1 = index * onePage + i;
            if (index1 >= source.length) {
                break;
            }
            arr[i] = source[index * onePage + i];
        }
        return arr;
    }

    /**
     * Return the resource id array of the gift emoticon page at index.
     *
     * @param index
     *            the visiable page index of gift emoticon page.
     * @return the resource id array.
     */
    private int[] getGiftIconArray(int index) {
        int[] source = MessageConsts.giftIconArr;
        int onePage;
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            onePage = mColumnArray[0] * 4;
        } else {
            onePage = mColumnArray[1] * 2;
        }
        int[] arr = new int[onePage];
        for (int i = 0; i < onePage; i++) {
            int index1 = index * onePage + i;
            if (index1 >= source.length) {
                break;
            }
            arr[i] = source[index * onePage + i];
        }
        return arr;
    }

    /**
     * Return the coding of the default emoticon.
     *
     * @param position
     *            the position of gridview.
     * @return the coding of the default emoticon.
     */
    private String getDefaultName(int position) {
        int onePage;
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            onePage = mColumnArray[0] * 4;
        } else {
            onePage = mColumnArray[1] * 2;
        }
        if (position >= 20) {
            return null;
        }
        int index = position + onePage * mDefaultIndex;
        if (index >= mDefaultName.length) {
            return null;
        }
        return mDefaultName[index];
    }

    /**
     * Return the coding of the gift emoticon.
     *
     * @param position
     *            the position of gridview.
     * @return the coding of the gift emoticon.
     */
    private String getGiftName(int position) {
        int onePage;
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            onePage = mColumnArray[0] * 4;
        } else {
            onePage = mColumnArray[1] * 2;
        }
        if (position >= 20) {
            return null;
        }
        int index = position + onePage * mGiftIndex;
        if (index >= mGiftName.length) {
            return null;
        }
        return mGiftName[index];
    }

    /**
     * Create the child of default emoticon page, and add it to parent.
     *
     * @param index
     *            the index of child.
     */
    private void addDefaultPage(int index) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.default_emoticon_flipper,
                mScrollLayout, false);
        GridView gridView = (GridView) v.findViewById(R.id.gv_default_emoticon_gridview);
        android.view.ViewGroup.LayoutParams params = mSharePanelMain.getLayoutParams();
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            params.height = getResources().getDimensionPixelOffset(R.dimen.share_panel_lan_height);
        } else {
            params.height = getResources().getDimensionPixelOffset(R.dimen.share_panel_port_height);
        }

        mSharePanelMain.setLayoutParams(params);
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            gridView.setNumColumns(mColumnArray[0]);
        } else {
            gridView.setNumColumns(mColumnArray[1]);
        }
        EmoticonAdapter adapter = new EmoticonAdapter(getDefaultIconArray(index));
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = getDefaultName(position);
                if (TextUtils.isEmpty(name)) {
                    return;
                }
                mListener.doAction(EditEmoticonListener.addEmoticon, name);
            }
        });
        mScrollLayout.addView(v);
    }

    /**
     * Create the child of gift emoticon page, and add it to parent.
     *
     * @param index
     *            the index of child.
     */
    private void addGiftPage(int index) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.gift_emoticon_flipper,
                mScrollLayout, false);
        GridView gridView = (GridView) v.findViewById(R.id.gv_gift_emoticon_gridview);
        android.view.ViewGroup.LayoutParams params = mSharePanelMain.getLayoutParams();
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            params.height = getResources().getDimensionPixelOffset(R.dimen.share_panel_lan_height);
        } else {
            params.height = getResources().getDimensionPixelOffset(R.dimen.share_panel_port_height);
        }

        mSharePanelMain.setLayoutParams(params);
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            gridView.setNumColumns(mColumnArray[0]);
        } else {
            gridView.setNumColumns(mColumnArray[1]);
        }
        EmoticonAdapter adapter = new EmoticonAdapter(getGiftIconArray(index));
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = getGiftName(position);
                if (TextUtils.isEmpty(name)) {
                    return;
                }
                mListener.doAction(EditEmoticonListener.addEmoticon, name);
            }
        });
        mScrollLayout.addView(v);
    }
    /**
     * Return the count of default emoticon page.
     *
     * @param orientation
     *            The screen orientation.
     * @return The count of default emoticon page.
     */
    private int calculateDefaultPageCount(int orientation) {
        int onePage;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            onePage = mColumnArray[0] * 4;
        } else {
            onePage = mColumnArray[1] * 2;
        }
        int total = MessageConsts.defaultIconArr.length;
        int count = total / onePage;
        if (total > count * onePage) {
            count++;
        }
        return count;
    }
    /**
     * Return the count of gift emoticon page.
     *
     * @param orientation
     *            The screen orientation.
     * @return The count of gift emoticon page.
     */
    private int calculateGiftPageCount(int orientation) {
        int onePage;
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            onePage = mColumnArray[0] * 4;
        } else {
            onePage = mColumnArray[1] * 2;
        }
        int total = MessageConsts.giftIconArr.length;
        int count = total / onePage;
        if (total > count * onePage) {
            count++;
        }
        return count;
    }
    /// @}
}
