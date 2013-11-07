
package com.mediatek.encapsulation.android.telephony;

import android.telephony.CbSmsMessage;
import com.mediatek.encapsulation.EncapsulationConstant;

public class EncapsulatedCbSmsMessage  {
    private CbSmsMessage mCbSmsMessage;

    public EncapsulatedCbSmsMessage(CbSmsMessage cbSmsMessage) {
        if (cbSmsMessage != null) {
            mCbSmsMessage = cbSmsMessage;
        }
    }

    /** M: MTK ADD */
    /**
     * @return the text of message
     */
    public String getMessageBody() {
        if (EncapsulationConstant.USE_MTK_PLATFORM)
            return mCbSmsMessage.getMessageBody();
        else {
            /** M: Can not complete for this branch. */
            return new String();
        }
    }

    /** M: MTK ADD */
    /**
     * @return SIM ID, but now it doesn't return a valid value
     */
    public int getMessageSimId() {
        if (EncapsulationConstant.USE_MTK_PLATFORM)
            return mCbSmsMessage.getMessageSimId();
        else {
            return -1;
        }
    }

    /** M: MTK ADD */
    /**
     * @return message id
     */
    public int getMessageID() {
        if (EncapsulationConstant.USE_MTK_PLATFORM)
            return mCbSmsMessage.getMessageID();
        else {
            /** M: Can not complete for this branch. */
            return 1;
        }
    }

}
