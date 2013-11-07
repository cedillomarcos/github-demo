/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2010. All rights reserved.
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

package com.android.mms.ui;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.LineHeightSpan;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.mms.R;
import com.android.mms.isms.util.message.SmileyParser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * M: This class provides view of a message in the messages list.
 */
public class CBMessageListItem extends LinearLayout {
    public static final String EXTRA_URLS = "com.android.mms.ExtraUrls";
    public static final int UPDATE_CHANNEL = 15;
    private static final String TAG = "CBMessageListItem";
    private static final StyleSpan STYLE_BOLD = new StyleSpan(Typeface.BOLD);

    private boolean mIsLastItemInList;
    private CBMessageItem mMessageItem;
    private Handler mHandler;
    private View mItemContainer;
    private TextView mBodyTextView;
    private TextView mSimStatus;
    private TextView mDateView;
    private Path mPath = new Path();
    private Paint mPaint = new Paint();
    private static Drawable sDefaultContactImage;
    // add for multi-delete
    private CheckBox mSelectedBox;

    public CBMessageListItem(Context context) {
        super(context);
        if (sDefaultContactImage == null) {
            sDefaultContactImage = context.getResources()
                    .getDrawable(R.drawable.ic_contact_picture);
        }
    }

    public CBMessageListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        int color = mContext.getResources().getColor(R.color.timestamp_color);
        mColorSpan = new ForegroundColorSpan(color);
        if (sDefaultContactImage == null) {
            sDefaultContactImage = context.getResources()
                    .getDrawable(R.drawable.ic_contact_picture);
        }
    }

    @Override
    protected void onFinishInflate() {
        Log.d("MmsLog", "CBMessageListItem.onFinishInflate()");
        super.onFinishInflate();
        mBodyTextView = (TextView) findViewById(R.id.text_view);
        mDateView = (TextView) findViewById(R.id.date_view);
        mItemContainer = findViewById(R.id.mms_layout_view_parent);
        // mItemContainer.setLongClickable(true);
        mSimStatus = (TextView) findViewById(R.id.sim_status);
        // add for multi-delete
        mSelectedBox = (CheckBox) findViewById(R.id.select_check_box);

    }

    public void bind(CBMessageItem msgItem, boolean isLastItem, boolean isDeleteMode) {
        mMessageItem = msgItem;
        mIsLastItemInList = isLastItem;
        setSelectedBackGroud(false);
        if (isDeleteMode) {
            mSelectedBox.setVisibility(View.VISIBLE);
            if (msgItem.isSelected()) {
                setSelectedBackGroud(true);
            }
        } else {
            mSelectedBox.setVisibility(View.GONE);
        }
        setLongClickable(false);
        mItemContainer.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                onMessageListItemClick();
            }
        });
        bindCommonMessage(msgItem);
    }

    public void onMessageListItemClick() {
        if (mSelectedBox != null && mSelectedBox.getVisibility() == View.VISIBLE) {
            if (!mSelectedBox.isChecked()) {
                setSelectedBackGroud(true);
            } else {
                setSelectedBackGroud(false);
            }
            if (null != mHandler) {
                Message msg = Message.obtain(mHandler, MessageListItem.ITEM_CLICK);
                msg.arg1 = (int) mMessageItem.getMessageId();
                msg.sendToTarget();
            }
            return;
        }
    }

    public void unbind() {
        // do nothing
    }

    public CBMessageItem getMessageItem() {
        return mMessageItem;
    }

    public void setMsgListItemHandler(Handler handler) {
        mHandler = handler;
    }

    private void bindCommonMessage(final CBMessageItem msgItem) {
        // Since the message text should be concatenated with the sender's
        // address(or name), I have to display it here instead of
        // displaying it by the Presenter.
        mBodyTextView.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

        // Get and/or lazily set the formatted message from/on the
        // MessageItem. Because the MessageItem instances come from a
        // cache (currently of size ~50), the hit rate on avoiding the
        // expensive formatMessage() call is very high.
        CharSequence formattedMessage = msgItem.getCachedFormattedMessage();
        if (formattedMessage == null) {
            formattedMessage = formatMessage(msgItem.getSubject(), msgItem.getDate(), null);
            msgItem.setCachedFormattedMessage(formattedMessage);
        }
        mBodyTextView.setText(formattedMessage);

        CharSequence formattedTimestamp = formatTimestamp(msgItem, msgItem.getDate());
        mDateView.setText(formattedTimestamp);

        CharSequence formattedSimStatus = formatSimStatus(msgItem);
        if (!TextUtils.isEmpty(formattedSimStatus)) {
            mSimStatus.setVisibility(View.VISIBLE);
            mSimStatus.setText(formattedSimStatus);
        } else {
            mSimStatus.setVisibility(View.GONE);
        }

        requestLayout();
    }

    private LineHeightSpan mSpan = new LineHeightSpan() {
        public void chooseHeight(CharSequence text, int start, int end, int spanstartv, int v,
                FontMetricsInt fm) {
            fm.ascent -= 10;
        }
    };

//    TextAppearanceSpan mTextSmallSpan = new TextAppearanceSpan(mContext,
//            android.R.style.TextAppearance_Small);

//    AlignmentSpan.Standard mAlignRight = new AlignmentSpan.Standard(Alignment.ALIGN_OPPOSITE);

    ForegroundColorSpan mColorSpan = null; // set in ctor

//    private ClickableSpan mLinkSpan = new ClickableSpan() {
//        public void onClick(View widget) {
//        }
//    };

    private CharSequence formatMessage(String body, String timestamp, Pattern highlight) {
        SpannableStringBuilder buf = new SpannableStringBuilder();

        if (!TextUtils.isEmpty(body)) {
            // Converts html to spannable if ContentType is "text/html".
            // buf.append(Html.fromHtml(body));
            SmileyParser parser = SmileyParser.getInstance();
            buf.append(parser.addSmileySpans(body));
        }

        if (highlight != null) {
            Matcher m = highlight.matcher(buf.toString());
            while (m.find()) {
                buf.setSpan(new StyleSpan(Typeface.BOLD), m.start(), m.end(), 0);
            }
        }

        return buf;
    }

    private CharSequence formatTimestamp(CBMessageItem msgItem, String timestamp) {
        SpannableStringBuilder buf = new SpannableStringBuilder();
        buf.append(TextUtils.isEmpty(timestamp) ? " " : timestamp);
        buf.setSpan(mSpan, 1, buf.length(), 0);

        // buf.setSpan(mTextSmallSpan, 0, buf.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        // Make the timestamp text not as dark
        buf.setSpan(mColorSpan, 0, buf.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return buf;
    }

    private CharSequence formatSimStatus(CBMessageItem msgItem) {
        SpannableStringBuilder buffer = new SpannableStringBuilder();
        // If we're in the process of sending a message (i.e. pending), then we show a "Sending..."
        // string in place of the timestamp.
        // Add sim info
        int simInfoStart = buffer.length();
        CharSequence simInfo = MessageUtils.getSimInfo(mContext, msgItem.mSimId);
        if (simInfo.length() > 0) {
            buffer.append(" ");
            buffer.append(mContext.getString(R.string.via_without_time_for_recieve));
            simInfoStart = buffer.length();
            buffer.append(" ");
            buffer.append(simInfo);
            buffer.append(" ");
        }

        // buffer.setSpan(mTextSmallSpan, 0, buffer.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        // Make the timestamp text not as dark
        buffer.setSpan(mColorSpan, 0, simInfoStart, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        return buffer;
    }

    public void setSelectedBackGroud(boolean selected) {
        if (selected) {
            mSelectedBox.setChecked(true);
            mSelectedBox.setBackgroundDrawable(null);
            setBackgroundResource(R.drawable.list_selected_holo_light);
        } else {
            setBackgroundDrawable(null);
            mSelectedBox.setChecked(false);
            mSelectedBox.setBackgroundResource(R.drawable.listitem_background);
        }
    }
}
