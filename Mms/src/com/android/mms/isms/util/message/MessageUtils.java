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
package com.android.mms.isms.util.message;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.AlertDialog;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.widget.Toast;

import com.android.mms.R;
import com.android.mms.isms.config.NmsCommonUtils;
import com.android.mms.isms.config.NmsCustomUIConfig;
import com.mediatek.encapsulation.MmsLog;

public class MessageUtils {

    private static final String TAG = "MessageUtils";

    public static SimpleDateFormat SDF1 = new SimpleDateFormat("MM-dd");
    public static final SimpleDateFormat SDF2 = new SimpleDateFormat("HH:mm");

    public static boolean isValidAttach(String path, boolean inspectSize) {
        if (!NmsCommonUtils.isExistsFile(path) || NmsCommonUtils.getFileSize(path) == 0) {
            MmsLog.e(TAG, "isValidAttach: file is not exist, or size is 0");
            return false;
        }
        if (inspectSize && NmsCommonUtils.getFileSize(path) > NmsCustomUIConfig.MAX_ATTACH_SIZE) {
            MmsLog.e(TAG, "file size is too large");
            return false;
        }
        return true;
    }

    public static void createLoseSDCardNotice(Context context, int resId) {
        new AlertDialog.Builder(context).setTitle("No SDcard").setMessage(resId)
                .setPositiveButton(R.string.ipmsg_cancel, null).create().show();
    }

    public static String formatFileSize(int size) {
        String result = "";
        int M = 1024 * 1024;
        int K = 1024;
        if (size > M) {
            int s = size % M / 100;
            if (s == 0) {
                result = size / M + "MB";
            } else {
                result = size / M + "." + s + "MB";
            }
        } else if (size > K) {
            int s = size % K / 100;
            if (s == 0) {
                result = size / K + "KB";
            } else {
                result = size / K + "." + s + "KB";
            }
        } else if (size > 0) {
            result = size + "B";
        } else {
            result = "invalid size";
        }
        return result;
    }

    public static String formatAudioTime(int duration) {
        String result = "";
        if (duration > 60) {
            if (duration % 60 == 0) {
                result = duration / 60 + "'";
            } else {
                result = duration / 60 + "'" + duration % 60 + "\"";
            }
        } else if (duration > 0) {
            result = duration + "\"";
        } else {
            // TODO iSMS replace this string with resource
            result = "no duration";
        }
        return result;
    }

    public static boolean shouldShowTimeDivider(long curTime, long nextTime) {
        Date curDate = new Date(curTime);
        Date nextDate = new Date(nextTime);
        Date cur = new Date(curDate.getYear(), curDate.getMonth(), curDate.getDate(), 0, 0, 0);
        Date next = new Date(nextDate.getYear(), nextDate.getMonth(), nextDate.getDate(), 0, 0, 0);
        return (cur.getTime() != next.getTime());
    }

    public static String getShortTimeString(Context context, long time) {
        int format_flags = DateUtils.FORMAT_NO_NOON_MIDNIGHT
                | DateUtils.FORMAT_CAP_AMPM;
        format_flags |= DateUtils.FORMAT_SHOW_TIME;
        return DateUtils.formatDateTime(context, time, format_flags);

    }

    public static String getTimeDividerString(Context context, long when) {
        Time then = new Time();
        then.set(when);
        Time now = new Time();
        now.setToNow();

        // Basic settings for formatDateTime() we want for all cases.
        int format_flags = DateUtils.FORMAT_NO_NOON_MIDNIGHT | DateUtils.FORMAT_ABBREV_ALL
                | DateUtils.FORMAT_CAP_AMPM;

        // If the message is from a different year, show the date and year.
        if (then.year != now.year) {
            format_flags |= DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_DATE;
        } else if (then.yearDay != now.yearDay) {
            // If it is from a different day than today, show only the date.
            format_flags |= DateUtils.FORMAT_SHOW_DATE;
            Date curDate = new Date();
            Date cur = new Date(curDate.getYear(), curDate.getMonth(), curDate.getDate(), 0, 0, 0);
            long oneDay = 24 * 60 * 60 * 1000;
            long elapsedTime = cur.getTime() - when;
            if (elapsedTime < oneDay && elapsedTime > 0) {
                return context.getResources().getString(R.string.str_ipmsg_yesterday);
            }
        } else {
            return context.getString(R.string.str_ipmsg_today);
        }
        return DateUtils.formatDateTime(context, when, format_flags);
    }

    public static String getDispTimeStr(Context context, long time) {

        boolean bThisYear = false;
        Date inDate = new Date(time);
        Date now = new Date();
        if (now.getYear() == inDate.getYear()) {
            if (now.getMonth() == inDate.getMonth() && now.getDate() == inDate.getDate()) {
                return SDF2.format(time);
            }
            bThisYear = true;
        }

        String fm = Settings.System.getString(context.getContentResolver(),
            Settings.System.DATE_FORMAT);
        if (TextUtils.isEmpty(fm)) {
            fm = getDefaultFM(context, bThisYear);
        } else {
            if (bThisYear) {
                if (fm.startsWith("Y") || fm.startsWith("y")) {
                    fm = fm.replace("Y", "");
                    fm = fm.replace("y", "");
                    int pos = 0;
                    while (pos < fm.length()) {
                        if (Character.isLetter(fm.charAt(pos))) {
                            break;
                        }
                        pos++;
                    }
                    fm = fm.substring(pos);

                } else if (fm.endsWith("Y") || fm.endsWith("y")) {
                    fm = fm.replace("Y", "");
                    fm = fm.replace("y", "");
                    int pos = fm.length() - 1;
                    while (pos >= 0) {
                        if (Character.isLetter(fm.charAt(pos))) {
                            pos += 1;
                            break;
                        }
                        pos--;
                    }
                    fm = fm.substring(0, pos);
                }
            }
        }

        if (TextUtils.isEmpty(fm))
            fm = getDefaultFM(context, bThisYear);

        SDF1 = new SimpleDateFormat(fm);
        return SDF1.format(time);
    }

    private static String getDefaultFM(Context context, boolean bThisYear) {
        String fm;
        char[] order = DateFormat.getDateFormatOrder(context);
        if (order != null) {
            if (bThisYear) {
                if (order[0] == 'y' || order[0] == 'Y') {
                    fm = "" + order[1] + order[1] + "/" + order[2] + order[2];
                } else {
                    fm = "" + order[0] + order[0] + "/" + order[1] + order[1];
                }
            } else {
                fm = "" + order[0] + order[0] + "/" + order[1] + order[1] + "/" + order[2]
                        + order[2];
            }
        } else {
            fm = "MM/DD";
        }
        return fm;
    }


    public static boolean isFileStatusOk(Context context, String path) {
        if (TextUtils.isEmpty(path)) {
            Toast.makeText(context, R.string.str_ipmsg_no_such_file, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!com.android.mms.isms.config.NmsCommonUtils.isExistsFile(path)) {
            Toast.makeText(context, R.string.str_ipmsg_no_such_file, Toast.LENGTH_SHORT).show();
            return false;
        }
        if (com.android.mms.isms.config.NmsCommonUtils.getFileSize(path) > (2 * 1024 * 1024)) {
            Toast.makeText(context, R.string.str_ipmsg_over_file_limit, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    public static boolean isPic(String name) {
        if (TextUtils.isEmpty(name)) {
            return false;
        }
        String path = name.toLowerCase();
        if (path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg")
                || path.endsWith(".bmp") || path.endsWith(".gif")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isVideo(String name) {
        if (TextUtils.isEmpty(name)) {
            return false;
        }
        String path = name.toLowerCase();
        if (path.endsWith(".mp4") || path.endsWith(".3gp")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isAudio(String name) {
        if (TextUtils.isEmpty(name)) {
            return false;
        }
        String extArrayString[] = {".amr", ".ogg", ".mp3", ".aac", ".ape", ".flac", ".wma", ".wav", ".mp2", ".mid"};
        String path = name.toLowerCase();
        for (String ext : extArrayString) {
            if (path.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }
}
