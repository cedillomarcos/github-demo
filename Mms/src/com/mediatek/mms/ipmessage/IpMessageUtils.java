package com.mediatek.mms.ipmessage;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SqliteWrapper;
import android.net.Uri;
import android.provider.Telephony.Sms;

import com.android.mms.data.Conversation;
import com.android.mms.data.WorkingMessage;
import com.android.mms.R;
import com.android.mms.ui.MessageUtils;
import com.mediatek.encapsulation.MmsLog;

import com.mediatek.mms.ipmessage.IpMessage;
import com.mediatek.mms.ipmessage.IpMessageConsts.IpMessageStatus;
import com.mediatek.mms.ipmessage.IpMessageConsts.IpMessageType;

public class IpMessageUtils {
    private static final String ISMS_TAG = "Mms/isms/utils";

    private static final String[] SMS_BODY_PROJECTION_WITH_IPMSG_ID = { Sms._ID, Sms.IPMSG_ID };
    private static final String SMS_DRAFT_WHERE = Sms.TYPE + "=" + Sms.MESSAGE_TYPE_DRAFT;
    private static final int SMS_ID_INDEX = 0;
    private static final int SMS_IPMSG_ID_INDEX = 1;

    public static IpMessage readIpMessageDraft(Context context, Conversation conv, WorkingMessage workingMessage) {
        MmsLog.d(ISMS_TAG, "readIpMessageDraft(): threadId = " + conv.getThreadId());
        long thread_id = conv.getThreadId();

        // If it's an invalid thread or we know there's no draft, don't bother.
        if (thread_id <= 0) {
            MmsLog.d(ISMS_TAG, "readDraftIpMessage(): no draft, thread_id = " + thread_id);
            return null;
        }

        Uri thread_uri = ContentUris.withAppendedId(Sms.Conversations.CONTENT_URI, thread_id);
        String body = "";

        Cursor c = SqliteWrapper.query(context, context.getContentResolver(), thread_uri,
            SMS_BODY_PROJECTION_WITH_IPMSG_ID, SMS_DRAFT_WHERE, null, null);
        long msgId = 0L;
        long ipMsgId = 0L;
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    ipMsgId = c.getLong(SMS_IPMSG_ID_INDEX);
                    if (ipMsgId > 0) {
                        msgId = c.getLong(SMS_ID_INDEX);
                    }
                }
            } finally {
                c.close();
            }
        }

        if (msgId > 0 && ipMsgId > 0) {
            IpMessage ipMessage = MessageUtils.getMessageManager(context).getIpMsgInfo(msgId);
            if (ipMessage != null) {
                ipMessage.setStatus(IpMessageStatus.OUTBOX);
                workingMessage.clearConversation(conv, true);
                MmsLog.d(ISMS_TAG, "readIpMessageDraft(): Get IP message draft, msgId = " + msgId);
                return ipMessage;
            }
        }
        MmsLog.d(ISMS_TAG, "readIpMessageDraft(): No IP message draft, msgId = " + msgId);
        return null;
    }

    public static boolean deleteIpMessageDraft(Context context, Conversation conv, WorkingMessage workingMessage) {
        MmsLog.d(ISMS_TAG, "deleteIpMessageDraft(): threadId = " + conv.getThreadId());
        long thread_id = conv.getThreadId();

        // If it's an invalid thread or we know there's no draft, don't bother.
        if (thread_id <= 0) {
            MmsLog.d(ISMS_TAG, "deleteIpMessageDraft(): no draft, thread_id = " + thread_id);
            return false;
        }

        Uri thread_uri = ContentUris.withAppendedId(Sms.Conversations.CONTENT_URI, thread_id);
        Cursor c = SqliteWrapper.query(context, context.getContentResolver(), thread_uri, SMS_BODY_PROJECTION_WITH_IPMSG_ID,
            SMS_DRAFT_WHERE, null, null);
        long msgId = 0L;
        long ipMsgId = 0L;
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    ipMsgId = c.getLong(SMS_IPMSG_ID_INDEX);
                    if (ipMsgId > 0) {
                        msgId = c.getLong(SMS_ID_INDEX);
                    }
                }
            } finally {
                c.close();
            }
        }

        if (msgId > 0) {
            if (ipMsgId > 0) {
                MessageUtils.getMessageManager(context).deleteIpMsg(new long[]{ msgId }, true, true);
                MmsLog.d(ISMS_TAG, "deleteIpMessageDraft(): Delete IP message draft, msgId = " + msgId);
            } else {
                workingMessage.asyncDeleteDraftSmsMessage(conv);
            }
            return true;
        }
        MmsLog.d(ISMS_TAG, "deleteIpMessageDraft(): No IP message draft, msgId = " + msgId);
        return false;
    }

    public static String getIpMessageCaption(IpMessage ipMessage) {
        MmsLog.d(ISMS_TAG, "getIpMessageCaption()");
        String caption = "";
        int type = ipMessage.getType();
        switch (type) {
        case IpMessageType.PICTURE:
            caption = ((IpImageMessage) ipMessage).getCaption();
            MmsLog.d(ISMS_TAG, "getIpMessageCaption(): Get pic caption, caption = " + caption);
            break;
        case IpMessageType.VOICE:
            caption = ((IpVoiceMessage) ipMessage).getCaption();
            MmsLog.d(ISMS_TAG, "getIpMessageCaption(): Get audio caption, caption = " + caption);
            break;
        case IpMessageType.VIDEO:
            caption = ((IpVideoMessage) ipMessage).getCaption();
            MmsLog.d(ISMS_TAG, "getIpMessageCaption(): Get video caption, caption = " + caption);
            break;
        case IpMessageType.TEXT:
        case IpMessageType.VCARD:
        case IpMessageType.LOCATION:
        case IpMessageType.SKETCH:
        case IpMessageType.CALENDAR:
        case IpMessageType.GROUP_CREATE_CFG:
        case IpMessageType.GROUP_ADD_CFG:
        case IpMessageType.GROUP_QUIT_CFG:
        case IpMessageType.UNKNOWN_FILE:
        case IpMessageType.COUNT:
            break;
        default:
            break;
        }
        return caption;
    }

    public static IpMessage setIpMessageCaption(IpMessage ipMessage, String caption) {
        int type = ipMessage.getType();
        switch (type) {
        case IpMessageType.PICTURE:
            ((IpImageMessage) ipMessage).setCaption(caption);
            break;
        case IpMessageType.VOICE:
            ((IpVoiceMessage) ipMessage).setCaption(caption);
            break;
        case IpMessageType.VIDEO:
            ((IpVideoMessage) ipMessage).setCaption(caption);
            break;
        case IpMessageType.TEXT:
        case IpMessageType.VCARD:
        case IpMessageType.LOCATION:
        case IpMessageType.SKETCH:
        case IpMessageType.CALENDAR:
        case IpMessageType.GROUP_CREATE_CFG:
        case IpMessageType.GROUP_ADD_CFG:
        case IpMessageType.GROUP_QUIT_CFG:
        case IpMessageType.UNKNOWN_FILE:
        case IpMessageType.COUNT:
            break;
        default:
            break;
        }
        return ipMessage;
    }

    public static int getIsmsStatusResourceId(int status) {
        int id = 0;
        if (status == IpMessageStatus.OUTBOX) {
            id = R.drawable.im_meg_status_sending;
        } else if (status == IpMessageStatus.SENT) {
            id = R.drawable.im_meg_status_out;
        } else if (status == IpMessageStatus.DELIVERED) {
            id = R.drawable.im_meg_status_reach;
        } else if (status == IpMessageStatus.FAILED) {
            id = R.drawable.ic_list_alert_sms_failed;
        } else if (status == IpMessageStatus.VIEWED) {
            id = R.drawable.im_meg_status_read;
        } else if (status == IpMessageStatus.NOT_DELIVERED) {
            id = R.drawable.ic_list_alert_sms_failed;
        }
        return id;
    }
}
