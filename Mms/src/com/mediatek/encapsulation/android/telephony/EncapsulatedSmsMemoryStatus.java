
package com.mediatek.encapsulation.android.telephony;

import android.telephony.SmsMemoryStatus;
import android.os.Parcel;
import android.os.Parcelable;

import com.mediatek.encapsulation.EncapsulationConstant;

public class EncapsulatedSmsMemoryStatus  {
    private SmsMemoryStatus mSmsMemoryStatus;
    /** M: MTK ADD */
    public int mUsed;
    public int mTotal;

    public EncapsulatedSmsMemoryStatus(SmsMemoryStatus smsMemoryStatus) {
       if(smsMemoryStatus!=null){
          mSmsMemoryStatus = smsMemoryStatus;
       }
   }

    public SmsMemoryStatus getSmsMemoryStatus() {
        return mSmsMemoryStatus;
    }

    /** M: MTK ADD */
    public EncapsulatedSmsMemoryStatus(int used, int total) {
        mUsed = used;
        mTotal = total;
    }

    /** M: MTK ADD */
    public int getUsed() {
        if (EncapsulationConstant.USE_MTK_PLATFORM) {
            return mSmsMemoryStatus.getUsed();
        } else {
            return 0;
        }
    }

    /** M: MTK ADD */
    public int getTotal() {
        if (EncapsulationConstant.USE_MTK_PLATFORM) {
            return mSmsMemoryStatus.getTotal();
        } else {
            return 0;
        }
    }

    /** M: MTK ADD */
    public int getUnused() {
        if (EncapsulationConstant.USE_MTK_PLATFORM) {
            return mSmsMemoryStatus.getUnused();
        } else {
            return 0;
        }
    }

    /** M: MTK ADD */
    public void reset() {
        if (EncapsulationConstant.USE_MTK_PLATFORM) {
            mSmsMemoryStatus.reset();
        } else {
            mUsed = 0;
            mTotal = 0;
        }
    }

    /** M: MTK ADD */
    public int describeContents() {
        if (EncapsulationConstant.USE_MTK_PLATFORM) {
            return mSmsMemoryStatus.describeContents();
        } else {
            return 0;
        }
    }

    /** M: MTK ADD */
    public void writeToParcel(Parcel dest, int flags) {
        if (EncapsulationConstant.USE_MTK_PLATFORM) {
            mSmsMemoryStatus.writeToParcel(dest, flags);
        } else {
            dest.writeInt(0);
            dest.writeInt(0);
        }
    }

    /** M: MTK ADD */
    public static final Parcelable.Creator<EncapsulatedSmsMemoryStatus> CREATOR =
                    new Parcelable.Creator<EncapsulatedSmsMemoryStatus>() {
        public EncapsulatedSmsMemoryStatus createFromParcel(Parcel source) {
            int used;
            int total;

            used = source.readInt();
            total = source.readInt();
            return new EncapsulatedSmsMemoryStatus(used, total);
        }

        public EncapsulatedSmsMemoryStatus[] newArray(int size) {
            return new EncapsulatedSmsMemoryStatus[size];
        }
    };

}
