/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2012. All rights reserved.
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

package com.mediatek.mms.ipmessage;


public final class IpMessageConsts {

    public static final String STATUS = "status";
    public static final String RESULT = "result";
    public static final String ID = "id";
    public static final String NUMBER = "number";
    public static final String GROUP_START = "7---";
    public static final String IPMESSAGE_NOTIFICATION = "ipmessage_notification";
    public static final String ACTION_GROUP_NOTIFICATION_CLICKED = "com.mediatek.mms.ipmessage.group_notification_clicked";
    public static final String ACTION_DEL_IP_MSG_DONE = "com.mediatek.mms.ipmessage.delIpMsgDone";
    public static final String ACTION_SERVICE_READY = "com.mediatek.mms.ipmessage.service.ready";
    public static final String ACTION_UPGRADE = "com.mediatek.mms.ipmessage.upgrade";

    public static final class NewMessageAction {
        public static final String ACTION_NEW_MESSAGE = "com.mediatek.mms.ipmessage.newMessage";
        public static final String IP_MESSAGE_KEY = "IpMessageKey";
    }

    public static final class RefreshContactList {
        public static final String ACTION_REFRESH_CONTACTS_LIST = "com.mediatek.mms.ipmessage.refreshContactList";
    }

    public static final class RefreshGroupList {
        public static final String ACTION_REFRESH_GROUP_LIST = "com.mediatek.mms.ipmessage.refreshGroupList";
    }

    public static final class UpdateGroup {
        public static final String UPDATE_GROUP_ACTION = "com.mediatek.mms.ipmessage.updateGroup";
        public static final String GROUP_ID = "group_id";
    }

    public static final class ServiceStatus {
        public static final String ACTION_SERVICE_STATUS = "com.mediatek.mms.ipmessage.serviceStatus";
        public static final int ON  = 1;
        public static final int OFF = 0;
    }

    public static final class ImStatus {
        public static final String ACTION_IM_STATUS = "com.mediatek.mms.ipmessage.IMStatus";
        public static final String CONTACT_CURRENT_STATUS = "com.mediatek.mms.ipmessage.ContactStatus";
    }

    public static final class ConnectionStatus {
//        public static final int UNKONW = -2;
//        public static final int STATUS_INIT = 0;
        public static final int STATUS_UNCONNECTED = 1;
//        public static final int STATUS_BLOCKING    = 2;
        public static final int STATUS_CONNECTING  = 3;
        public static final int STATUS_CONNECTED   = 4;
    }

    public static final class SaveHistroy {
        public static final String ACTION_SAVE_HISTROY = "com.mediatek.mms.ipmessage.saveHistroy";
        public static final String SAVE_HISTROY_PERCENTAGE = "com.mediatek.mms.ipmessage.saveHistroyProgress";
        public static final String SAVE_HISTRORY_DONE = "com.mediatek.mms.ipmessage.saveHistroyDone";
        public static final String DOWNLOAD_HISTORY_FILE = "nms.isms.saveHistoryFile";
        public static final int NMS_OK = 0;
        public static final int NMS_ERROR = -1;
        public static final int NMS_EMPTY = -2;
    }

    public static final class ActivationStatus {
        public static final String ACTION_ACTIVATION_STATUS = "com.mediatek.mms.ipmessage.activationStatus";
        public static final int FAILED_TO_ACTIVATE = -1;
        public static final int NOT_ACTIVATED = 0;
        public static final int ACTIVATING    = 1;
        public static final int WAITING_INPUT_NUMBER = 2;
        public static final int ACTIVATED     = 3;
    }

    public static final class IpMessageStatus {
        public static final String ACTION_MESSAGE_STATUS = "com.mediatek.mms.ipmessage.messageStatus";
        public static final String IP_MESSAGE_ID = "com.mediatek.mms.ipmessage.IpMessageRecdId";
        public static final int FAILED = 0;
        public static final int OUTBOX_PENDING = 1; /* not ready to send yet */
        public static final int OUTBOX = 2;
        public static final int SENT   = 3;
        public static final int NOT_DELIVERED = 4;
        public static final int DELIVERED = 5;
        public static final int VIEWED = 6;
        public static final int DRAFT  = 7;
        public static final int INBOX  = 8;

        public static final int MO_INVITE = 11;     // notified download file
        public static final int MO_SENDING = 12;    // sending file
        public static final int MO_REJECTED = 13;   // reject sending file
        public static final int MO_SENT = 14;       // file has been sent
        public static final int MO_CANCEL = 15;     // cancel send file
        public static final int MT_INVITED = 21;    // receive a notification for downloading file
        public static final int MT_REJECT = 22;     // reject receive file
        public static final int MT_RECEIVING = 23;  // reveiving file
        public static final int MT_RECEIVED = 24;   // file has been received
        public static final int MT_CANCEL = 25;     // cancel receive file
    }

    public static final class HandleIpMessageAction {
        public static final int TAP = 1;
    }

    public static final class DownloadAttachStatus {
        public static final String ACTION_DOWNLOAD_ATTACH_STATUS = "com.mediatek.mms.ipmessage.downloadAttachStatus";
        public static final String DOWNLOAD_PERCENTAGE = "DownloadPercentage";
        public static final String DOWNLOAD_MSG_ID = "DownloadMsgId";
        public static final String DOWNLOAD_MSG_STATUS = "DownloadMsgStatus";
        public static final int FAILED  = -1;
        public static final int STARTING = 0;
        public static final int DOWNLOADING = 1; // argument is the downloading percentage
        public static final int DONE = 2;
    }

    public static final class SetProfileResult {
        public static final String ACTION_SET_PROFILE_RESULT = "com.mediatek.mms.ipmessage.setProfileResult";
        public static final int SUCCEED = 0;
        public static final int FAILED  = 1; // or -1 is better?
    }

    public static final class BackupMsgStatus {
        public static final String ACTION_BACKUP_MSG_STATUS = "com.mediatek.mms.ipmessage.backupMsgStatus";
        public static final String UPLOADING_PERCENTAGE = "uploadingPercentage";
        public static final int STARTING = 0;
        public static final int UPLOADING = 1; // argument is the downloading percentage
        public static final int FAILED  = 2; // or -1 is better?
    }

    public static final class RestoreMsgStatus {
        public static final String ACTION_RESTORE_MSG_STATUS = "com.mediatek.mms.ipmessage.restoreMsgStatus";
        public static final String DOWNLOAD_PERCENTAGE = "DownloadPercentage";
        public static final int STARTING = 0;
        public static final int DOWNLOADING = 1; // argument is the downloading percentage
        public static final int FAILED  = 2; // or -1 is better?
    }

    public static final class IpMessageType {
        public static final int TEXT = 0;
        public static final int GROUP_CREATE_CFG = 1;
        public static final int GROUP_ADD_CFG = 2;
        public static final int GROUP_QUIT_CFG = 3;
        public static final int PICTURE = 4;
        public static final int VOICE = 5;
        public static final int VCARD = 6;
        public static final int LOCATION = 7;
        public static final int SKETCH = 8;
        public static final int VIDEO = 9;
        public static final int CALENDAR = 10;
        public static final int UNKNOWN_FILE = 11;
        public static final int COUNT = 12;
    }

    public static final class IpMessageMediaTypeFlag {
        public static final int PICTURE = 1 << IpMessageType.PICTURE;
        public static final int VOICE = 1 << IpMessageType.VOICE;
        public static final int VCARD = 1 << IpMessageType.VCARD;
        public static final int LOCATION = 1 << IpMessageType.LOCATION;
        public static final int SKETCH = 1 << IpMessageType.SKETCH;
        public static final int VIDEO = 1 << IpMessageType.VIDEO;
        public static final int CALENDAR = 1 << IpMessageType.CALENDAR;

        public static final int ALL = PICTURE | VOICE | VCARD | LOCATION | SKETCH | VIDEO | CALENDAR ;
    }

    public static final class IpMessageCategory {
        public static final int ALL = 0;
        public static final int FAVOURITE = 1;
        public static final int GROUPCHAT = 2;
        public static final int SPAM = 3;
    }

    public static final class MessageProtocol {
        public static final int IP  = 1;
        public static final int SMS = 2;
        public static final int MMS = 3;
    }

    public static final class IpMessageSendMode {
        public static final int NORMAL  = 0;
        public static final int AUTO = 1;
    }

    public static final class ContactType {
        public static final int NOT_HISSAGE_USER   = 0;
        public static final int HISSAGE_USER       = 1;
        public static final int HISSAGE_GROUP_CHAT = 2;
        public static final int HISSAGE_BROADCAST  = 3;
    }

    public static final class SelectContactType {
        public static final int ALL = 0;
        public static final int IP_MESSAGE_USER = 1;
        public static final int NOT_IP_MESSAGE_USER = 2;
    }

    public static final class ContactStatus {
        public static final String CONTACT_UPDATE = "com.mediatek.mms.ipmessage.contactUpdate";
        public static final int OFFLINE     = 0;
        public static final int ONLINE      = 1;
        public static final int TYPING      = 2;
        public static final int STOP_TYPING = 3;
        public static final int RECORDING   = 4;
        public static final int STOP_RECORDING   = 5;
        public static final int SKETCHING   = 6;
        public static final int STOP_SKETCHING = 7;
        public static final int STATUSCOUNT = 8;
    }

    public static final class SpecialSimId {
        public static final int INVALID_SIM_ID = -1;
        public static final int SINGLE_LOAD_SIM_ID = -2;
        public static final int ALL_SIM_ID = -3;
    }

    public static final class IpMessageServiceId {
        public static final int NO_SERVICE = 0;
        public static final int ISMS_SERVICE = 1;
        public static final int RCSE_SERVICE = 2;
    }

    public static final class ReminderType {
        public static final int REMINDER_INVALID    = 0;
        public static final int REMINDER_INVITE     = 1;
        public static final int REMINDER_ACTIVATE   = 2;
        public static final int REMINDER_SWITCH     = 3;
        public static final int REMINDER_ENABLE     = 4;
    }

    public static final class FeatureId {
        public static final int CHAT_SETTINGS = 1;
        public static final int APP_SETTINGS = 2;
        public static final int ACTIVITION = 3;
        public static final int ACTIVITION_WIZARD = 4;
        public static final int ALL_LOCATION = 5;   /// M: list all location messages in activity.
        public static final int ALL_MEDIA = 6;      /// M: list all media messages in activity.
        public static final int MEDIA_DETAIL = 7;   /// M: displaying media detail info.
        public static final int GROUP_MESSAGE = 8;
        public static final int CONTACT_SELECTION = 9;
        public static final int SKETCH = 10;
        public static final int LOCATION = 11;
        public static final int TERM = 12;
        public static final int SAVE_CHAT_HISTORY = 13;
        public static final int SAVE_ALL_HISTORY = 14;
        public static final int SHARE_CHAT_HISTORY = 15;
        public static final int SHARE_ALL_HISTORY = 16;
        public static final int FILE_TRANSACTION = 17;
    }

    public static final class ResourceId {
        public static final int STR_IPMESSAGE_SETTINGS = 1;
        public static final int STR_IPMESSAGE_RESENT = 2;
        public static final int STR_IPMESSAGE_ACCEPT = 3;
        public static final int STR_IPMESSAGE_REJECT = 4;
    }

    public static final class RemoteActivities {
        public static final String KEY_THREAD_ID        = "thread_id";      // key of thread id, long
        public static final String KEY_CONTACT_ID       = "contact_id";     // key of contact id,
        public static final String KEY_MESSAGE_ID       = "message_id";     // key of message id, int
        public static final String KEY_SIM_ID           = "sim_id";         // key of sim id, int
        public static final String KEY_URI              = "uri";            // key of uri
        public static final String KEY_REQUEST_CODE     = "request_code";   // key of request code, int
        public static final String KEY_NEED_NEW_TASK    = "need_new_task";  // key of need new task, boolean
        public static final String KEY_SIZE             = "size";           // key of size
        public static final String KEY_TYPE             = "type";           // key of type
        public static final String KEY_ARRAY            = "array";          // key of array
        public static final String KEY_BOOLEAN          = "boolean" ;       // key of boolean value
        public static final String KEY_GENERIC_1        = "generic_1";      // key for other cases: array, buddle, etc.
        public static final String KEY_GENERIC_2        = "generic_2";      // key for other cases: array, buddle, etc.
        public static final String KEY_GENERIC_3        = "generic_3";      // key for other cases: array, buddle, etc.

        public static final int ID_CHAT_SETTINGS                = 1;
        public static final int ID_SYSTEM_SETTINGS              = 2;
        public static final int ID_ACTIVITION                   = 3;
        public static final int ID_LOCATION                     = 4;
        public static final int ID_ALL_MEDIA                    = 5;
        public static final int ID_ALL_LOCATION                 = 6;
        public static final int ID_CHAT_DETAILS_BY_THREAD_ID    = 7;
        public static final int ID_CONTACT                      = 8;
        public static final int ID_NON_IPMESSAGE_CONTACT        = 9;
        public static final int ID_NEW_GROUP_CHAT               = 10;
        public static final int ID_QUICK_CONTACT                = 11;
        public static final int ID_MEDIA_DETAIL                 = 12;
        public static final int ID_SKETCH                       = 13;
        public static final int ID_AUDIO                        = 14;
        public static final int ID_SERVICE_CENTER               = 15;
        public static final int ID_PROFILE                      = 16;
        public static final int ID_TERM                         = 17;

        public static final String CHAT_SETTINGS = "content://chat_settings/" + Integer.toString(ID_CHAT_SETTINGS);
        public static final String SYSTEM_SETTINGS = "content://system_settings/" + Integer.toString(ID_SYSTEM_SETTINGS);
        public static final String ACTIVITION = "content://activition/" + Integer.toString(ID_ACTIVITION);
        public static final String LOCATION = "content://location/" + Integer.toString(ID_LOCATION);
        public static final String ALL_MEDIA = "content://all_media/" + Integer.toString(ID_ALL_MEDIA);
        public static final String ALL_LOCATION = "content://all_location/" + Integer.toString(ID_ALL_LOCATION);
        public static final String CHAT_DETAILS_BY_THREAD_ID = "content://chat_details_by_thread_id/" 
                + Integer.toString(ID_CHAT_DETAILS_BY_THREAD_ID);
        public static final String CONTACT = "content://contact_selection/" + Integer.toString(ID_CONTACT);
        public static final String NON_IPMESSAGE_CONTACT = "content://non_ipmessage_contact_selection/" 
                + Integer.toString(ID_NON_IPMESSAGE_CONTACT);
        public static final String NEW_GROUP_CHAT = "content://new_group_chat/" + Integer.toString(ID_NEW_GROUP_CHAT);
        public static final String QUICK_CONTACT = "content://quick_contact/" + Integer.toString(ID_QUICK_CONTACT);
        public static final String MEDIA_DETAIL = "content://media_detail/" + Integer.toString(ID_MEDIA_DETAIL);
        public static final String SKETCH = "content://sketch/" + Integer.toString(ID_SKETCH);
        public static final String AUDIO = "content://audio/" + Integer.toString(ID_AUDIO);
        public static final String SERVICE_CENTER = "content://service_center/" + Integer.toString(ID_SERVICE_CENTER);
        public static final String PROFILE = "content://profile/" + Integer.toString(ID_PROFILE);
        public static final String TERM = "content://term/" + Integer.toString(ID_TERM);
    }
}


