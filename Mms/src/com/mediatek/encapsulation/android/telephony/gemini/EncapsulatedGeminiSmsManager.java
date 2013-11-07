
package com.mediatek.encapsulation.android.telephony.gemini;

import android.telephony.gemini.GeminiSmsManager;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.os.Bundle;
import android.app.PendingIntent;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.telephony.SmsMemoryStatus;

import com.android.internal.telephony.ISms;
import com.mediatek.encapsulation.EncapsulationConstant;
import com.mediatek.encapsulation.android.telephony.EncapsulatedSmsMemoryStatus;

import java.util.List;
import java.util.ArrayList;


public class EncapsulatedGeminiSmsManager {

    /** M: MTK ADD */
    public static int copyTextMessageToIccCardGemini(String scAddress, String address,
            List<String> text, int status, long timestamp, int slotId) {
        if (EncapsulationConstant.USE_MTK_PLATFORM) {
            return GeminiSmsManager.copyTextMessageToIccCardGemini(scAddress, address, text,
                    status, timestamp, slotId);
        } else {
            int result = SmsManager.RESULT_ERROR_GENERIC_FAILURE;
            String isms;
            if (slotId == 0/* Phone.GEMINI_SIM_1 */) {
                isms = "isms";
            } else if (slotId == 1/* Phone.GEMINI_SIM_2 */) {
                isms = "isms2";
            } else {
                isms = null;
            }

            try {
                ISms iccISms = ISms.Stub.asInterface(ServiceManager.getService(isms));
                if (iccISms != null) {
                    result = iccISms.copyTextMessageToIccCard(scAddress, address, text, status,
                            timestamp);
                }
            } catch (RemoteException ex) {
                // ignore it
            }

            return result;
        }
    }

    /** M: MTK ADD */
    public static EncapsulatedSmsMemoryStatus getSmsSimMemoryStatusGemini(int simId) {
        if (EncapsulationConstant.USE_MTK_PLATFORM) {
            SmsMemoryStatus smsMemoryStatus = GeminiSmsManager.getSmsSimMemoryStatusGemini(simId);
            if (smsMemoryStatus != null) {
                return new EncapsulatedSmsMemoryStatus(smsMemoryStatus);
            } else {
                return null;
            }
        } else {
            String isms;
            if (simId == 0/* Phone.GEMINI_SIM_1 */) {
                isms = "isms";
            } else if (simId == 1/* Phone.GEMINI_SIM_2 */) {
                isms = "isms2";
            } else {
                isms = null;
            }
            try {
                ISms iccISms = ISms.Stub.asInterface(ServiceManager.getService(isms));
                if (iccISms != null) {
                    SmsMemoryStatus smsMemoryStatus = iccISms.getSmsSimMemoryStatus();
                    if (smsMemoryStatus != null) {
                        return new EncapsulatedSmsMemoryStatus(smsMemoryStatus);
                    } else {
                        return null;
                    }
                }
            } catch (RemoteException ex) {
                // ignore it
            }

            return null;
        }
    }

    /** M: MTK ADD */
    public static boolean copyMessageToIccGemini(byte[] smsc, byte[] pdu, int status, int simId) {
        if (EncapsulationConstant.USE_MTK_PLATFORM) {
            return GeminiSmsManager.copyMessageToIccGemini(smsc,pdu,status,simId);
        } else {
            /** M: Can not complete for this branch. */
            return SmsManager.getDefault().copyMessageToIcc(smsc, pdu, status);
        }

    }

    /** M: MTK ADD */
    public static void sendMultipartTextMessageWithExtraParamsGemini(String destAddr,
            String scAddr, ArrayList<String> parts, Bundle extraParams, int slotId,
            ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents) {
        if (EncapsulationConstant.USE_MTK_PLATFORM) {
             GeminiSmsManager.sendMultipartTextMessageWithExtraParamsGemini(destAddr, scAddr,
                    parts, extraParams, slotId, sentIntents, deliveryIntents);
        } else {
            /** M: Can not complete for this branch. */
            SmsManager.getDefault().sendMultipartTextMessage(destAddr, scAddr,
                                                parts, sentIntents, deliveryIntents);
        }

    }

    /** M: MTK ADD */
    public static void sendMultipartTextMessageWithEncodingTypeGemini(String destAddr,
            String scAddr, ArrayList<String> parts, int encodingType, int slotId,
            ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents) {
        if (EncapsulationConstant.USE_MTK_PLATFORM) {
             GeminiSmsManager.sendMultipartTextMessageWithEncodingTypeGemini(destAddr, scAddr,
                    parts, encodingType, slotId, sentIntents, deliveryIntents);
        } else {
            /** M: Can not complete for this branch. */
            SmsManager.getDefault().sendMultipartTextMessage(destAddr, scAddr,
                                            parts, sentIntents, deliveryIntents);
        }
    }

    /** M: MTK ADD */
    public static ArrayList<SmsMessage> getAllMessagesFromIccGemini(int simId) {
        if (EncapsulationConstant.USE_MTK_PLATFORM) {
            return GeminiSmsManager.getAllMessagesFromIccGemini(simId);
        } else {
            /** M: Can not complete for this branch. */
            return SmsManager.getAllMessagesFromIcc();
        }
    }

    /** M: MTK ADD */
    public static boolean deleteMessageFromIccGemini(int messageIndex, int simId) {
        if (EncapsulationConstant.USE_MTK_PLATFORM) {
            return GeminiSmsManager.deleteMessageFromIccGemini(messageIndex, simId);
        } else {
            /** M: Can not complete for this branch. */
            return SmsManager.getDefault().deleteMessageFromIcc(messageIndex);
        }
    }

}