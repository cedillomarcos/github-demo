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

import com.android.mms.R;

public class MessageConsts {
    public final static int NMS_SHARE = 0;
    public final static int NMS_LOAD_ALL_MESSAGE = 1;
    public final static int NMS_SCROLL_LIST = 2;
    public final static int[] shareIconArr = { R.drawable.isms_take_a_photo,
            R.drawable.isms_record_a_video, R.drawable.isms_record_an_audio,
            R.drawable.isms_share_contact, R.drawable.isms_choose_a_photo,
            R.drawable.isms_choose_a_video, R.drawable.isms_choose_an_audio,
            R.drawable.isms_share_calendar, R.drawable.isms_add_slideshow};
    public final static int[] ipmsgShareIconArr = { R.drawable.isms_take_a_photo,
            R.drawable.isms_record_a_video, R.drawable.isms_record_an_audio,
            R.drawable.isms_draw_a_sketch, R.drawable.isms_choose_a_photo,
            R.drawable.isms_choose_a_video, R.drawable.isms_choose_an_audio,
            R.drawable.isms_share_location, R.drawable.isms_share_contact,
            R.drawable.isms_share_calendar, R.drawable.isms_add_slideshow};
    public static final int[] emoticonIdList = { R.drawable.emo_small_01, R.drawable.emo_small_02,
            R.drawable.emo_small_03, R.drawable.emo_small_04, R.drawable.emo_small_05,
            R.drawable.emo_small_06, R.drawable.emo_small_07, R.drawable.emo_small_08,
            R.drawable.emo_small_09, R.drawable.emo_small_10, R.drawable.emo_small_11,
            R.drawable.emo_small_12, R.drawable.emo_small_13, R.drawable.emo_small_14,
            R.drawable.emo_small_15, R.drawable.emo_small_16, R.drawable.emo_small_17,
            R.drawable.emo_small_18, R.drawable.emo_small_19, R.drawable.emo_small_20,
            R.drawable.emo_small_21, R.drawable.emo_small_22, R.drawable.emo_small_23,
            R.drawable.emo_small_24, R.drawable.emo_small_25, R.drawable.emo_small_26,
            R.drawable.emo_small_27, R.drawable.emo_small_28, R.drawable.emo_small_29,
            R.drawable.emo_small_30, R.drawable.emo_small_31, R.drawable.emo_small_32,
            R.drawable.emo_small_33, R.drawable.emo_small_34, R.drawable.emo_small_35,
            R.drawable.emo_small_36, R.drawable.emo_small_37, R.drawable.emo_small_38,
            R.drawable.emo_small_39, R.drawable.emo_small_40, R.drawable.good, R.drawable.no,
            R.drawable.ok, R.drawable.victory, R.drawable.seduce, R.drawable.down, R.drawable.rain,
            R.drawable.lightning, R.drawable.sun, R.drawable.microphone, R.drawable.clock,
            R.drawable.email, R.drawable.candle, R.drawable.birthday_cake, R.drawable.gift,
            R.drawable.star, R.drawable.heart, R.drawable.brokenheart, R.drawable.bulb,
            R.drawable.music, R.drawable.shenma, R.drawable.fuyun, R.drawable.rice,
            R.drawable.roses, R.drawable.film, R.drawable.aeroplane, R.drawable.umbrella,
            R.drawable.caonima, R.drawable.penguin, R.drawable.pig };
    public final static int[] largeIconArr = { R.drawable.emo_praise, R.drawable.emo_gift,
            R.drawable.emo_kongfu, R.drawable.emo_shower, R.drawable.emo_scare, R.drawable.emo_ill,
            R.drawable.emo_rich, R.drawable.emo_fly, R.drawable.emo_angry, R.drawable.emo_approve,
            R.drawable.emo_boring, R.drawable.emo_cry, R.drawable.emo_driving,
            R.drawable.emo_eating, R.drawable.emo_happy, R.drawable.emo_hold,
            R.drawable.emo_holiday, R.drawable.emo_love, R.drawable.emo_pray,
            R.drawable.emo_pressure, R.drawable.emo_sing, R.drawable.emo_sleep,
            R.drawable.emo_sports, R.drawable.emo_swimming };
    public final static int[] dynamicIconArr = { R.drawable.emo_dynamic_01,
            R.drawable.emo_dynamic_02, R.drawable.emo_dynamic_03, R.drawable.emo_dynamic_04,
            R.drawable.emo_dynamic_05, R.drawable.emo_dynamic_06, R.drawable.emo_dynamic_07,
            R.drawable.emo_dynamic_08, R.drawable.emo_dynamic_09, R.drawable.emo_dynamic_10,
            R.drawable.emo_dynamic_11, R.drawable.emo_dynamic_12, R.drawable.emo_dynamic_13,
            R.drawable.emo_dynamic_14, R.drawable.emo_dynamic_15, R.drawable.emo_dynamic_16,
            R.drawable.emo_dynamic_17, R.drawable.emo_dynamic_18, R.drawable.emo_dynamic_19,
            R.drawable.emo_dynamic_20, R.drawable.emo_dynamic_21, R.drawable.emo_dynamic_22,
            R.drawable.emo_dynamic_23, R.drawable.emo_dynamic_24 };
    public final static int[] dynamicPngIconArr = { R.drawable.emo_dynamic_01_png,
            R.drawable.emo_dynamic_02_png, R.drawable.emo_dynamic_03_png,
            R.drawable.emo_dynamic_04_png, R.drawable.emo_dynamic_05_png,
            R.drawable.emo_dynamic_06_png, R.drawable.emo_dynamic_07_png,
            R.drawable.emo_dynamic_08_png, R.drawable.emo_dynamic_09_png,
            R.drawable.emo_dynamic_10_png, R.drawable.emo_dynamic_11_png,
            R.drawable.emo_dynamic_12_png, R.drawable.emo_dynamic_13_png,
            R.drawable.emo_dynamic_14_png, R.drawable.emo_dynamic_15_png,
            R.drawable.emo_dynamic_16_png, R.drawable.emo_dynamic_17_png,
            R.drawable.emo_dynamic_18_png, R.drawable.emo_dynamic_19_png,
            R.drawable.emo_dynamic_20_png, R.drawable.emo_dynamic_21_png,
            R.drawable.emo_dynamic_22_png, R.drawable.emo_dynamic_23_png,
            R.drawable.emo_dynamic_24_png };
    public final static int[] adIconArr = { R.drawable.ad01, R.drawable.ad02, R.drawable.ad03,
            R.drawable.ad04, R.drawable.ad05, R.drawable.ad06, R.drawable.ad07, R.drawable.ad08,
            R.drawable.ad09, R.drawable.ad10, R.drawable.ad11, R.drawable.ad12, R.drawable.ad13,
            R.drawable.ad14, R.drawable.ad15, R.drawable.ad16, R.drawable.ad17, R.drawable.ad18,
            R.drawable.ad19, R.drawable.ad20, R.drawable.ad21, R.drawable.ad22, R.drawable.ad23,
            R.drawable.ad24 };
    public final static int[] adPngIconArr = { R.drawable.ad01_png, R.drawable.ad02_png,
            R.drawable.ad03_png, R.drawable.ad04_png, R.drawable.ad05_png, R.drawable.ad06_png,
            R.drawable.ad07_png, R.drawable.ad08_png, R.drawable.ad09_png, R.drawable.ad10_png,
            R.drawable.ad11_png, R.drawable.ad12_png, R.drawable.ad13_png, R.drawable.ad14_png,
            R.drawable.ad15_png, R.drawable.ad16_png, R.drawable.ad17_png, R.drawable.ad18_png,
            R.drawable.ad19_png, R.drawable.ad20_png, R.drawable.ad21_png, R.drawable.ad22_png,
            R.drawable.ad23_png, R.drawable.ad24_png };
    public final static int[] xmIconArr = { R.drawable.xm01, R.drawable.xm02, R.drawable.xm03,
            R.drawable.xm04, R.drawable.xm05, R.drawable.xm06, R.drawable.xm07, R.drawable.xm08,
            R.drawable.xm09, R.drawable.xm10, R.drawable.xm11, R.drawable.xm12, R.drawable.xm13,
            R.drawable.xm14, R.drawable.xm15, R.drawable.xm16, R.drawable.xm17, R.drawable.xm18,
            R.drawable.xm19, R.drawable.xm20, R.drawable.xm21, R.drawable.xm22, R.drawable.xm23,
            R.drawable.xm24 };
    public final static int[] xmPngIconArr = { R.drawable.xm01_png, R.drawable.xm02_png,
            R.drawable.xm03_png, R.drawable.xm04_png, R.drawable.xm05_png, R.drawable.xm06_png,
            R.drawable.xm07_png, R.drawable.xm08_png, R.drawable.xm09_png, R.drawable.xm10_png,
            R.drawable.xm11_png, R.drawable.xm12_png, R.drawable.xm13_png, R.drawable.xm14_png,
            R.drawable.xm15_png, R.drawable.xm16_png, R.drawable.xm17_png, R.drawable.xm18_png,
            R.drawable.xm19_png, R.drawable.xm20_png, R.drawable.xm21_png, R.drawable.xm22_png,
            R.drawable.xm23_png, R.drawable.xm24_png };
    /// M: add for common emoticon panel. @{
    public static final int[] defaultIconArr = { R.drawable.emo_small_01, R.drawable.emo_small_02,
        R.drawable.emo_small_03, R.drawable.emo_small_04, R.drawable.emo_small_05,
        R.drawable.emo_small_06, R.drawable.emo_small_07, R.drawable.emo_small_08,
        R.drawable.emo_small_09, R.drawable.emo_small_10, R.drawable.emo_small_11,
        R.drawable.emo_small_12, R.drawable.emo_small_13, R.drawable.emo_small_14,
        R.drawable.emo_small_15, R.drawable.emo_small_16, R.drawable.emo_small_17,
        R.drawable.emo_small_18, R.drawable.emo_small_19, R.drawable.emo_small_20,
        R.drawable.emo_small_21, R.drawable.emo_small_22, R.drawable.emo_small_23,
        R.drawable.emo_small_24, R.drawable.emo_small_25, R.drawable.emo_small_26,
        R.drawable.emo_small_27, R.drawable.emo_small_28, R.drawable.emo_small_29,
        R.drawable.emo_small_30, R.drawable.emo_small_31, R.drawable.emo_small_32,
        R.drawable.emo_small_33, R.drawable.emo_small_34, R.drawable.emo_small_35,
        R.drawable.emo_small_36, R.drawable.emo_small_37, R.drawable.emo_small_38,
        R.drawable.emo_small_39, R.drawable.emo_small_40};

    public static final int[] giftIconArr = {R.drawable.good, R.drawable.no,
        R.drawable.ok, R.drawable.victory, R.drawable.seduce, R.drawable.down, R.drawable.rain,
        R.drawable.lightning, R.drawable.sun, R.drawable.microphone, R.drawable.clock,
        R.drawable.email, R.drawable.candle, R.drawable.birthday_cake, R.drawable.gift,
        R.drawable.star, R.drawable.heart, R.drawable.brokenheart, R.drawable.bulb,
        R.drawable.music, R.drawable.shenma, R.drawable.fuyun, R.drawable.rice,
        R.drawable.roses, R.drawable.film, R.drawable.aeroplane, R.drawable.umbrella,
        R.drawable.caonima, R.drawable.penguin, R.drawable.pig };
    /// @}
}
