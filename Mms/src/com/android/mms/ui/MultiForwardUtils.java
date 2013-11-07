/**
 *
 */
package com.android.mms.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Data;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.mms.ExceedMessageSizeException;
import com.android.mms.LogTag;
import com.android.mms.MmsConfig;
import com.android.mms.data.Conversation;
import com.android.mms.data.WorkingMessage;
import com.android.mms.data.WorkingMessage.MessageStatusListener;
import com.android.mms.model.SlideshowModel;
import com.android.mms.R;
import com.android.mms.ui.MessageUtils.ResizeImageResultCallback;
import com.google.android.mms.ContentType;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu.PduBody;
import com.google.android.mms.pdu.PduPart;
import com.google.android.mms.pdu.PduPersister;
import com.mediatek.mms.ipmessage.IpAttachMessage;
import com.mediatek.mms.ipmessage.IpImageMessage;
import com.mediatek.mms.ipmessage.IpLocationMessage;
import com.mediatek.mms.ipmessage.IpMessage;
import com.mediatek.mms.ipmessage.IpMessageConsts;
import com.mediatek.mms.ipmessage.IpMessageConsts.IpMessageType;
import com.mediatek.mms.ipmessage.IpTextMessage;
import com.mediatek.mms.ipmessage.IpVCardMessage;
import com.mediatek.mms.ipmessage.IpVideoMessage;
import com.mediatek.mms.ipmessage.IpVoiceMessage;
import com.mediatek.encapsulation.MmsLog;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;


/**
 * M:
 *
 */
public class MultiForwardUtils {

    private static final String TAG="Mms/MultiForwardUtils";
    public static int getContactSIM(final Context context, final String num) {
        int simId = -1;
        String number = num;
        String formatNumber = MessageUtils.formatNumber(number, context);
        Cursor associateSIMCursor = context.getContentResolver().query(
            Data.CONTENT_URI,
            new String[] {Data.SIM_ASSOCIATION_ID},
            Data.MIMETYPE + "='" + CommonDataKinds.Phone.CONTENT_ITEM_TYPE + "' AND (" + Data.DATA1
                + "=?" + " OR " + Data.DATA1 + "=?" + ") AND (" + Data.SIM_ASSOCIATION_ID
                + "!= -1)", new String[] {number, formatNumber}, null);
        try {
            if ((null != associateSIMCursor) && (associateSIMCursor.getCount() > 0)) {
                associateSIMCursor.moveToFirst();
                // Get only one record is OK
                simId = (Integer) associateSIMCursor.getInt(0);
            } else {
                simId = -1;
            }
        } finally {
            if (associateSIMCursor != null) {
                associateSIMCursor.close();
            }
        }
        return simId;
    }

    private WorkingMessage mWorkingMessage = null;
    private Context mContext = null;
    private Activity mActivity = null;

    public MultiForwardUtils(Activity activity) {
        mContext = activity.getApplicationContext();
        mActivity = activity;
    }

    public WorkingMessage convertIpMsgToSmsOrMms(IpMessage ipMessage, Context context,
            Conversation conv) {
        WorkingMessage.MessageStatusListener messageListener = null;
        if(mWorkingMessage==null){
            mWorkingMessage = WorkingMessage.createEmpty(mActivity, messageListener);
            mWorkingMessage.setConversation(conv);
        }
        if (ipMessage == null) {
            return null;
        }
        MmsLog.d(TAG, "multiforward. convertIpMsgToSmsOrMms; ipMessageType:" + ipMessage.getType());
        switch (ipMessage.getType()) {
        case IpMessageType.TEXT:
            IpTextMessage textMessage = (IpTextMessage) ipMessage;
            mWorkingMessage.setText(textMessage.getBody());
            return mWorkingMessage;

        case IpMessageType.PICTURE:
            IpImageMessage imageMessage = (IpImageMessage) ipMessage;
            File imageFile = new File(imageMessage.getPath());
            Uri imageUri = Uri.fromFile(imageFile);
            WorkingMessage imgWorkingMsg = addImage(mActivity, conv, imageUri, ContentType.IMAGE_JPG, true);
            if (imgWorkingMsg != null) {
                imgWorkingMsg.setText(imageMessage.getCaption());
            }
            return imgWorkingMsg;

        case IpMessageType.VOICE:
            IpVoiceMessage voiceMessage = (IpVoiceMessage) ipMessage;
            File voiceFile = new File(voiceMessage.getPath());
            Uri voiceUri = Uri.fromFile(voiceFile);
            WorkingMessage voiceWM = addAudio(mActivity, conv, voiceUri, true);
            if (voiceWM != null) {
                voiceWM.setText(voiceMessage.getCaption());
            }
            return voiceWM;

        case IpMessageType.VCARD:
            IpVCardMessage vCardMessage = (IpVCardMessage) ipMessage;
            File vCardFile = new File(vCardMessage.getPath());
            Uri vCardUri = Uri.fromFile(vCardFile);
            WorkingMessage vCardWM = addVCard(mActivity, conv, vCardUri);
            return vCardWM;

        case IpMessageType.LOCATION:
            WorkingMessage lmWorkingMsg = null;
            IpLocationMessage locationMessage = (IpLocationMessage) ipMessage;
            String locationImgPath = locationMessage.getPath();
            if (!TextUtils.isEmpty(locationImgPath)) {
                File locationFile = new File(locationImgPath);
                Uri locationUri = Uri.fromFile(locationFile);
                lmWorkingMsg = addImage(mActivity, conv, locationUri, ContentType.IMAGE_JPG, true);
            } else {
                String tempFileName = "default_map_small.png";
                boolean convertResult = MessageUtils.createFileForResource(mActivity.getApplicationContext(), tempFileName,
                    R.drawable.default_map_small);
                if (convertResult) {
                    File locationTempFile = mActivity.getFileStreamPath(tempFileName);
                    Uri locationUri = Uri.fromFile(locationTempFile);
                    lmWorkingMsg = addImage(mActivity, conv, locationUri, ContentType.IMAGE_JPG, true);
                }
            }
            lmWorkingMsg.setText(locationMessage.getAddress());
            return lmWorkingMsg;

        case IpMessageType.SKETCH:
            IpImageMessage sketchMessage = (IpImageMessage) ipMessage;
            File sketchFile = new File(sketchMessage.getPath());
            Uri sketchUri = Uri.fromFile(sketchFile);
            WorkingMessage sketchWorkingMsg = addImage(mActivity, conv, sketchUri, ContentType.IMAGE_JPG, true);
            if (sketchWorkingMsg != null) {
                sketchWorkingMsg.setText(sketchMessage.getCaption());
            }
            return sketchWorkingMsg;

        case IpMessageType.VIDEO:
            IpVideoMessage videoMessage = (IpVideoMessage) ipMessage;
            File videoFile = new File(videoMessage.getPath());
            Uri videoUri = Uri.fromFile(videoFile);
            WorkingMessage videoWorkingMsg = addVideo(mActivity, conv, videoUri, true);
            if (videoWorkingMsg != null) {
                videoWorkingMsg.setText(videoMessage.getCaption());
            }
            return videoWorkingMsg;

//        case IpMessageType.UNKNOWN_FILE:
//        case IpMessageType.COUNT:
//            MmsLog.e(ISMS_TAG, "convertIpMessageToMmsOrSms(): Unknown IP message type. type = " + mIpMessage.getType());
//            toastUnsupport(this);
//            return false;
//        case IpMessageType.GROUP_CREATE_CFG:
//        case IpMessageType.GROUP_ADD_CFG:
//        case IpMessageType.GROUP_QUIT_CFG:
//            /// M: group chat type
//            MmsLog.e(ISMS_TAG, "convertIpMessageToMmsOrSms(): Group IP message type. type = " + mIpMessage.getType());
//            toastUnsupport(this);
//            return false;
        default:
            MmsLog.e(TAG, "convertIpMessageToMmsOrSms(): Error IP message type. type = " + ipMessage.getType());
            return null;
        }

    }

    public WorkingMessage addImage(Activity activity, Conversation conversation, Uri imageUri,
            String mimeType, boolean append) {
        mWorkingMessage = WorkingMessage.createEmpty(activity, mMessageStatusListener);
        mWorkingMessage.setConversation(conversation);
        int result = WorkingMessage.OK;
        if (imageUri != null) {
            try {
                mWorkingMessage.checkSizeBeforeAppend();
            } catch (ExceedMessageSizeException e) {
                result = WorkingMessage.MESSAGE_SIZE_EXCEEDED;
                return null;
            }
            result = mWorkingMessage
                    .setAttachment(WorkingMessage.IMAGE, imageUri, append, mimeType);
//            if (result == WorkingMessage.WARNING_TYPE) {
//                final Uri creationUri = imageUri;
//                final boolean creationAppend = append;
//                mActivity.runOnUiThread(new Runnable() {
//                    public void run() {
//                        showConfirmDialog(creationUri, creationAppend, WorkingMessage.IMAGE,
//                            R.string.confirm_restricted_image);
//                    }
//                });
//                return null;
//            }
            if (result == WorkingMessage.OK) {
                mWorkingMessage.saveAsMms(false);
                return mWorkingMessage;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

//    private void showConfirmDialog(Activity activity, Uri uri, boolean append, int type, int messageId) {
//
//        final Uri mRestrictedMidea = uri;
//        final boolean mRestrictedAppend = append;
//        final int mRestrictedType = type;
//
//        new AlertDialog.Builder(activity)
//        .setTitle(R.string.unsupport_media_type)
//        .setIconAttribute(android.R.attr.alertDialogIcon)
//        .setMessage(messageId)
//        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//            public final void onClick(DialogInterface dialog, int which) {
//                    if (mRestrictedMidea == null || mRestrictedType == WorkingMessage.TEXT
//                        || mWorkingMessage.isDiscarded()) {
//                        return;
//                    }
//                    runAsyncWithDialog(new Runnable() {
//                        public void run() {
//                            int createMode = WorkingMessage.sCreationMode;
//                            WorkingMessage.sCreationMode = 0;
//                            int result = mWorkingMessage.setAttachment(mRestrictedType,
//                                mRestrictedMidea, mRestrictedAppend);
//                            WorkingMessage.sCreationMode = createMode;
//                            int typeId = R.string.type_picture;
//                            if (mRestrictedType == WorkingMessage.AUDIO) {
//                                typeId = R.string.type_audio;
//                            } else if (mRestrictedType == WorkingMessage.VIDEO) {
//                                typeId = R.string.type_video;
//                            }
//                            if (result == WorkingMessage.OK) {
//                                mWorkingMessage.saveAsMms(false);
//                            }
//                        }
//                    }, R.string.adding_attachments_title);
//            }
//        })
//        .setNegativeButton(android.R.string.cancel, null)
//        .show();
//    }

    private static final WorkingMessage.MessageStatusListener mMessageStatusListener = new MessageStatusListener() {

        @Override
        public void onProtocolChanged(boolean mms, boolean needToast) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onPreMmsSent() {
            // TODO Auto-generated method stub
        }

        @Override
        public void onPreMessageSent() {
            // TODO Auto-generated method stub
        }

        @Override
        public void onMessageSent() {
            // TODO Auto-generated method stub
        }

        @Override
        public void onMaxPendingMessagesReached() {
            // TODO Auto-generated method stub
        }

        @Override
        public void onAttachmentError(int error) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void onAttachmentChanged() {
            // TODO Auto-generated method stub
        }
    };

    public WorkingMessage addVideo(Activity activity, Conversation conversation, Uri videoUri,
            boolean append) {
        mWorkingMessage = WorkingMessage.createEmpty(activity, mMessageStatusListener);
        mWorkingMessage.setConversation(conversation);
        if (videoUri != null) {
            int result = WorkingMessage.OK;
            try {
                mWorkingMessage.checkSizeBeforeAppend();
            } catch (ExceedMessageSizeException e) {
                result = WorkingMessage.MESSAGE_SIZE_EXCEEDED;
                return null;
            }
            result = mWorkingMessage.setAttachment(WorkingMessage.VIDEO, videoUri, append);
            // if (result == WorkingMessage.WARNING_TYPE) {
            // final boolean mAppend = append;
            // final Uri mUri = videoUri;
            // mActivity.runOnUiThread(new Runnable() {
            // public void run() {
            // showConfirmDialog(mUri, mAppend, WorkingMessage.VIDEO, R.string.confirm_restricted_video);
            // }
            // });
            // } else {
            // // handleAddAttachmentError(result, R.string.type_video);
            // }
            if (result == WorkingMessage.OK) {
                mWorkingMessage.saveAsMms(false);
                return mWorkingMessage;
            } else {
                return null;
            }
        }
        return null;
    }

    public WorkingMessage addAudio(Activity activity, Conversation conversation, Uri audioUri,
            boolean append) {
        mWorkingMessage = WorkingMessage.createEmpty(activity, mMessageStatusListener);
        mWorkingMessage.setConversation(conversation);
        if (audioUri != null) {
            int result = WorkingMessage.OK;
            try {
                mWorkingMessage.checkSizeBeforeAppend();
            } catch (ExceedMessageSizeException e) {
                result = WorkingMessage.MESSAGE_SIZE_EXCEEDED;
                // handleAddAttachmentError(result, R.string.type_picture);
                return null;
            }
            result = mWorkingMessage.setAttachment(WorkingMessage.AUDIO, audioUri, append);
            // if (result == WorkingMessage.WARNING_TYPE) {
            // final Uri mUriTemp = uri;
            // mActivity.runOnUiThread(new Runnable() {
            // public void run() {
            // showConfirmDialog(mUriTemp, false, WorkingMessage.AUDIO, R.string.confirm_restricted_audio);
            // }
            // });
            // return;
            // }
            if (result == WorkingMessage.OK) {
                mWorkingMessage.saveAsMms(false);
                return mWorkingMessage;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public WorkingMessage addVCard(Activity activity, Conversation conversation, Uri vCardUri){
        mWorkingMessage = WorkingMessage.createEmpty(activity, mMessageStatusListener);
        mWorkingMessage.setConversation(conversation);
        if (vCardUri == null) {
            return null;
        }
        final String filename = getVCardFileName();
        try {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = activity.getContentResolver().openInputStream(vCardUri);
                out = activity.openFileOutput(filename, Context.MODE_PRIVATE);
                byte[] buf = new byte[8096];
                int size = 0;
                while ((size = in.read(buf)) != -1) {
                    out.write(buf, 0, size);
                }
            } finally {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "exception attachVCardByUri ", e);
            return null;
        }
        int type = WorkingMessage.VCARD;
        final File attachFile = activity.getFileStreamPath(filename);
        MmsLog.d(TAG, "setFileAttachment(): attachFile.exists()?=" + attachFile.exists() + ", attachFile.length()=" + attachFile.length());
        final Resources res = activity.getResources();
        if (attachFile.exists() && attachFile.length() > 0) {
            Uri attachUri = Uri.fromFile(attachFile);
            int result = WorkingMessage.OK;
            try {
                mWorkingMessage.checkSizeBeforeAppend();
            } catch (ExceedMessageSizeException e) {
                result = WorkingMessage.MESSAGE_SIZE_EXCEEDED;
                return null;
            }
            result = mWorkingMessage.setAttachment(type, attachUri, false);
            if (result == WorkingMessage.OK) {
                mWorkingMessage.saveAsMms(false);
                return mWorkingMessage;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public WorkingMessage addVCalendar(Activity activity, Conversation conversation,
            Uri vCalendarUri) {
        mWorkingMessage = WorkingMessage.createEmpty(activity, mMessageStatusListener);
        mWorkingMessage.setConversation(conversation);
        if (vCalendarUri == null) {
            Log.e(TAG, "attachVCalendar, oops uri is null");
            return null;
        }
        int result = WorkingMessage.OK;
        try {
            mWorkingMessage.checkSizeBeforeAppend();
        } catch (ExceedMessageSizeException e) {
            result = WorkingMessage.MESSAGE_SIZE_EXCEEDED;
            return null;
        }
        result = mWorkingMessage.setAttachment(WorkingMessage.VCALENDAR, vCalendarUri, false);
        if (result == WorkingMessage.OK) {
            mWorkingMessage.saveAsMms(false);
            return mWorkingMessage;
        } else {
            return null;
        }
    }

    /**
     * M:
     * @return
     */
    private String getVCardFileName() {
        final String fileExtension = ".vcf";
        // base on time stamp
        String name = DateFormat.format("yyyyMMdd_hhmmss", new Date(System.currentTimeMillis()))
                .toString();
        name = name.trim();
        return name + fileExtension;
    }

    private WorkingMessage resizeImage(Activity activity, WorkingMessage workingMessage,
            Uri imageUri, boolean append) {

        if (workingMessage == null || imageUri == null || activity == null) {
            return null;
        }

        PduPart part;
        try {
            UriImage image = new UriImage(activity.getApplicationContext(), imageUri);
            part = image.getResizedImageAsPart(MmsConfig.getMaxImageWidth(), MmsConfig
                    .getMaxImageHeight(), MmsConfig.getUserSetMmsSizeLimit(true)
                - MessageUtils.MESSAGE_OVERHEAD);
        } catch (IllegalArgumentException e) {
            MmsLog.e(TAG, "Unexpected IllegalArgumentException.", e);
            return null;
        }

        PduPersister persister = PduPersister.getPduPersister(activity.getApplicationContext());
        int result;
        Uri messageUri = workingMessage.getMessageUri();
        if (null == messageUri) {
            try {
                messageUri = workingMessage.saveAsMms(true);
            } catch (IllegalStateException e) {
                MmsLog.e(TAG, e.getMessage() + ", go to ConversationList!");
                return null;
            }
        }
        if (messageUri == null) {
            result = WorkingMessage.UNKNOWN_ERROR;
            return null;
        } else {
            try {
                Uri dataUri;
                int mode;
                synchronized (WorkingMessage.sDraftMmsLock) {
                    dataUri = persister.persistPart(part, ContentUris.parseId(messageUri));
                    mode = workingMessage.sCreationMode;
                    workingMessage.sCreationMode = 0;
                    result = workingMessage.setAttachment(WorkingMessage.IMAGE, dataUri, append);
                    if (result == WorkingMessage.OK) {
                        workingMessage.saveAsMms(false);
                    }
                }
                workingMessage.sCreationMode = mode;
                return workingMessage;
            } catch (MmsException e) {
                result = WorkingMessage.UNKNOWN_ERROR;
                return null;
            }
        }
    }
}
