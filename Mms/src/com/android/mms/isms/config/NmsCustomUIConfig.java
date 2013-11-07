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
package com.android.mms.isms.config;

public class NmsCustomUIConfig {

    public static final boolean PRIVATEMESSAGE = true;// true:display the
                                                      // private message.
                                                      // false:disappear the
                                                      // private message item in
                                                      // sns setting page.
    public static final boolean AD_SHOW_ALWAYS = false;// true:display AD in
                                                       // mail viewer.
                                                       // false:disappear AD in
                                                       // page.
    public static final boolean NEED_GET_PAY_INFO = false; // true:get pay info
                                                           // from server,
                                                           // false:don't get
    public static final String AD_ADMOB_ID = "-1"; // The ad tag that is used
                                                   // for server, and account
                                                   // all click count.
    public static final boolean NEED_SHOW_WEBPASSWD = true;
    public static final boolean NEED_SHOW_PAY_INFO = true;
    public static final String AD_UNIT_ID = "a14ed30fed9daf2";
    public static final String ROOTDIRECTORY = "iSMS";
    public static final int COMPOSE_MAX_CONTACT_CNT = 50;
    public static final int COMPOSE_OVER_CONTACT_CNT = 30;
    public static final int COMPOSE_MAX_ADDR = 25;
    public static final int MESSAGE_MAX_LENGTH = 2048;
    public static final int CAPTION_MAX_LENGTH = 100;
    public static final int STATUE_MAX_LENGTH = 100;
    public static final int GROUPNAME_MAX_LENGTH = 64;
    public static final int GROUPMEM_MAX_COUNT = 10;
    public static final int PHONENUM_MAX_LENGTH = 30;
    public static final int LOCATION_ADDR_MAX_LENGTH = 100;
    public static final long MAX_FILE_SIZE = 5 * 1024 *1024;
    public static final int MAX_MSG_NUM = 2000;
    public static final int VIDEO_MAX_SIZE = 1024*300;// 300k
    public static final int VIDEO_MAX_DURATION = 30; // 30s
    public static final long AUDIO_MAX_DURATION = 180;
    public static final int MAX_ATTACH_SIZE = 2 * 1024 * 1024;
}
