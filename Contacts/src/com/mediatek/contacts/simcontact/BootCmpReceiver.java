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

package com.mediatek.contacts.simcontact;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.android.internal.telephony.IccCardConstants;
import com.android.internal.telephony.PhoneConstants;
import com.android.internal.telephony.TelephonyIntents;

import com.mediatek.contacts.ContactsFeatureConstants.FeatureOption;

public class BootCmpReceiver extends BroadcastReceiver {
    private static final String TAG = "BootCmpReceiver";
    private static Context sContext = null;

    private static String sIntentSimFileChanged = "android.intent.action.sim.SIM_FILES_CHANGED"; 
    private static String sIntentSimFileChanged2 = "android.intent.action.sim.SIM_FILES_CHANGED_2";

    public void onReceive(Context context, Intent intent) {
        sContext = context;
        Log.i(TAG, "In onReceive ");
        final String action = intent.getAction();
        Log.i(TAG, "action is " + action);

        if (action.equals(TelephonyIntents.ACTION_PHB_STATE_CHANGED)) {
            processPhoneBookChanged(intent);
        } else if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
            processAirplaneModeChanged(intent);
        } else if (action.equals(Intent.ACTION_DUAL_SIM_MODE_CHANGED)) {
            processDualSimModeChanged(intent);
        } else if (action.equals(TelephonyIntents.ACTION_SIM_STATE_CHANGED)) {
            processSimStateChanged(intent);
        } else if (action.equals(sIntentSimFileChanged)) { // SIM REFERSH
            processSimFilesChanged(0);
        } else if (action.equals(sIntentSimFileChanged2)) { // SIM REFRESH
            processSimFilesChanged(1);
            /*
             * } else if (action.equals(Intent.SIM_SETTINGS_INFO_CHANGED)) {
             * processSimInfoUpdateForSettingChanged(intent); }
             */
        } else if (action.equals("android.intent.action.ACTION_SHUTDOWN_IPO")) {
            processIpoShutDown();
        } else if (action.equals("android.intent.action.ACTION_PHONE_RESTART")) {
            processPhoneReset(intent);
        }
    }

    public void startSimService(int slotId, int workType) {
        Intent intent = null;
        if (slotId == 0) {
            intent = new Intent(sContext, StartSIMService.class);
        } else {
            intent = new Intent(sContext, StartSIMService2.class);
        }
        
        intent.putExtra(AbstractStartSIMService.SERVICE_SLOT_KEY, slotId);
        intent.putExtra(AbstractStartSIMService.SERVICE_WORK_TYPE, workType);
        Log.i(TAG, "[startSimService]slotId:" + slotId + "|workType:" + workType);
        sContext.startService(intent);
    }

    void processPhoneBookChanged(Intent intent) {
        Log.i(TAG, "processPhoneBookChanged");
        boolean phbReady = intent.getBooleanExtra("ready", false);
        int slotId = intent.getIntExtra("simId", -10);
        Log.i(TAG, "[processPhoneBookChanged]phbReady:" + phbReady + "|slotId:" + slotId);
        if (phbReady && slotId >= 0) {
            startSimService(slotId, AbstractStartSIMService.SERVICE_WORK_IMPORT);
            /*SIMInfoWrapper simInfoWrapper = SIMInfoWrapper.getSimWrapperInstanceUnCheck();
            if (simInfoWrapper != null) {
                simInfoWrapper.updateSimInfoCache();
            }*/
        }
    }
    
    void processAirplaneModeChanged(Intent intent) {
        Log.i(TAG, "processAirplaneModeChanged");
        boolean isAirplaneModeOn = intent.getBooleanExtra("state", false);
        Log.i(TAG, "[processAirplaneModeChanged]isAirplaneModeOn:" + isAirplaneModeOn);
        if (isAirplaneModeOn) {
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                startSimService(0, AbstractStartSIMService.SERVICE_WORK_REMOVE);
                startSimService(1, AbstractStartSIMService.SERVICE_WORK_REMOVE);
            } else {
                startSimService(0, AbstractStartSIMService.SERVICE_WORK_REMOVE);
            }
        } else {
            if (FeatureOption.MTK_GEMINI_SUPPORT) {
                startSimService(0, AbstractStartSIMService.SERVICE_WORK_IMPORT);
                startSimService(1, AbstractStartSIMService.SERVICE_WORK_IMPORT);
            } else {
                startSimService(0, AbstractStartSIMService.SERVICE_WORK_IMPORT);
            }
        }
    }
    
    /**
     * Dual Sim mode is only for Gemini Feature.
     * 0 for none sim, 1 for sim1 only, 2 for sim2 only, 3 for dual sim
     * And the deefault mode 3
     * 
     * The change map is as following 
     *  
     *              => (Mode 1) <=
     *            ==              == 
     * (Mode 3) <=                  => (Mode 0)
     *            ==              ==
     *              => (Mode 2) <=
     * 
     * @param intent
     */
    void processDualSimModeChanged(Intent intent) {
        Log.i(TAG, "processDualSimModeChanged");
        // Intent.EXTRA_DUAL_SIM_MODE = "mode";
        int type = intent.getIntExtra("mode", -1);
        
        SharedPreferences prefs = sContext.getSharedPreferences(
                "sim_setting_preference", Context.MODE_PRIVATE);
        int prevType = prefs.getInt("dual_sim_mode", 3);
        
        Log.i(TAG, "[processDualSimModeChanged]type:" + type + "|prevType:" + prevType);
        switch (type) {
            case 0:
                if (prevType == 1) {
                    startSimService(0, AbstractStartSIMService.SERVICE_WORK_REMOVE);
                } else if (prevType == 2) {
                    startSimService(1, AbstractStartSIMService.SERVICE_WORK_REMOVE);
                } else {
                    startSimService(0, AbstractStartSIMService.SERVICE_WORK_REMOVE);
                    startSimService(1, AbstractStartSIMService.SERVICE_WORK_REMOVE);
                }
                break;

            case 1:
                if (prevType == 0) {
                    startSimService(0, AbstractStartSIMService.SERVICE_WORK_IMPORT);
                } else if (prevType == 3) {
                    startSimService(1, AbstractStartSIMService.SERVICE_WORK_REMOVE);
                } else {
                    startSimService(0, AbstractStartSIMService.SERVICE_WORK_IMPORT);
                    startSimService(1, AbstractStartSIMService.SERVICE_WORK_REMOVE);
                }
                break;

            case 2:
                if (prevType == 0) {
                    startSimService(1, AbstractStartSIMService.SERVICE_WORK_IMPORT);
                } else if (prevType == 3) {
                    startSimService(0, AbstractStartSIMService.SERVICE_WORK_REMOVE);
                } else {
                    startSimService(1, AbstractStartSIMService.SERVICE_WORK_IMPORT);
                    startSimService(0, AbstractStartSIMService.SERVICE_WORK_REMOVE);
                }
                break;

            case 3:
                if (prevType == 1) {
                    startSimService(1, AbstractStartSIMService.SERVICE_WORK_IMPORT);
                } else if (prevType == 2) {
                    startSimService(0, AbstractStartSIMService.SERVICE_WORK_IMPORT);
                } else {
                    startSimService(0, AbstractStartSIMService.SERVICE_WORK_IMPORT);
                    startSimService(1, AbstractStartSIMService.SERVICE_WORK_IMPORT);
                }
                break;

            default:
                break;
        }
        
      SharedPreferences.Editor editor = prefs.edit();
      editor.putInt("dual_sim_mode", type);
      editor.commit();
    }
    
    void processSimStateChanged(Intent intent) {
        Log.i(TAG, "processSimStateChanged");
        String phoneName = intent.getStringExtra(PhoneConstants.PHONE_NAME_KEY);
        String iccState = intent.getStringExtra(IccCardConstants.INTENT_KEY_ICC_STATE);
        int slotId = intent.getIntExtra(PhoneConstants.GEMINI_SIM_ID_KEY, -1);

        Log.i(TAG, "mPhoneName:" + phoneName + "|mIccStae:" + iccState
                + "|mySlotId:" + slotId);
        // Check SIM state, and start service to remove old sim data if sim
        // is not ready.
        /*if (IccCardConstants.INTENT_VALUE_ICC_ABSENT.equals(iccState)) {
            SIMInfoWrapper simInfoWrapper = SIMInfoWrapper.getSimWrapperInstanceUnCheck();
            if (simInfoWrapper != null) {
                simInfoWrapper.updateSimInfoCache();
            }
        }*/
        if (IccCardConstants.INTENT_VALUE_ICC_ABSENT.equals(iccState)
                || IccCardConstants.INTENT_VALUE_ICC_LOCKED.equals(iccState)
                || IccCardConstants.INTENT_VALUE_LOCKED_NETWORK.equals(iccState)) {
            startSimService(slotId, AbstractStartSIMService.SERVICE_WORK_REMOVE);
        }
        if (IccCardConstants.INTENT_VALUE_ICC_READY.equals(iccState)
                && SimCardUtils.isPhoneBookReady(slotId)) {
            startSimService(slotId, AbstractStartSIMService.SERVICE_WORK_IMPORT);
            /*SIMInfoWrapper simInfoWrapper = SIMInfoWrapper.getSimWrapperInstanceUnCheck();
            if (simInfoWrapper != null) {
                simInfoWrapper.updateSimInfoCache();
            }*/
        }
    }

    void processSimFilesChanged(int slotId) {
        Log.i(TAG, "processSimStateChanged:" + slotId);
        if (SimCardUtils.isPhoneBookReady(slotId)) {
            startSimService(slotId, AbstractStartSIMService.SERVICE_WORK_IMPORT);
        }
    }
    
    /*void processSimInfoUpdateForSettingChanged(Intent intent) {
        Log.i(TAG, "processSimInfoUpdateForSettingChanged:" + intent.toString());
        SIMInfoWrapper simInfoWrapper = SIMInfoWrapper.getSimWrapperInstanceUnCheck();
        if (simInfoWrapper != null) {
            simInfoWrapper.updateSimInfoCache();
        } else {
            SIMInfoWrapper.getDefault();
        }
    }*/
    
    void processIpoShutDown() {
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            startSimService(0, AbstractStartSIMService.SERVICE_WORK_REMOVE);
            startSimService(1, AbstractStartSIMService.SERVICE_WORK_REMOVE);
        } else {
            startSimService(0, AbstractStartSIMService.SERVICE_WORK_REMOVE);
        }
    }

    void processPhoneReset(Intent intent) {
        Log.i(TAG, "processPhoneReset");
        /*SIMInfoWrapper simInfoWrapper = SIMInfoWrapper.getSimWrapperInstanceUnCheck();
        if (simInfoWrapper != null) {
            simInfoWrapper.updateSimInfoCache();
        }*/
        if (FeatureOption.MTK_GEMINI_SUPPORT) {
            int slotId = intent.getIntExtra("SimId", -1);
            if (slotId != -1) {
                Log.i(TAG, "processPhoneReset" + slotId);
                startSimService(slotId, AbstractStartSIMService.SERVICE_WORK_IMPORT);
            }
        } else {
            Log.i(TAG, "processPhoneReset0");
            startSimService(0, AbstractStartSIMService.SERVICE_WORK_IMPORT);
        }
    }
}
