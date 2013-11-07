package com.android.settings;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.ServiceManager;
import android.provider.Telephony;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.android.internal.telephony.ITelephony;
import com.mediatek.common.featureoption.FeatureOption;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class OmacpApn {
    
    private ContentResolver mContentResolver;
    private Context mContext;
    private int mSimId;
    private Uri mUri;
    private String mNumeric;


    private static final String TAG = "OmacpApn";
    
    private static final int SIM_CARD_1 = 0;
    private static final int SIM_CARD_2 = 1;
    private static final int SIM_CARD_SINGLE = 2;
    
    //  ArrayList<Map<String, String>> mApnList;
    
    /**
     * Standard projection for the interesting columns of a normal note.
     */
    protected static final String[] PROJECTION = new String[] {
            Telephony.Carriers._ID,     // 0
            Telephony.Carriers.NAME,    // 1
            Telephony.Carriers.APN,     // 2
            Telephony.Carriers.PROXY,   // 3
            Telephony.Carriers.PORT,    // 4
            Telephony.Carriers.USER,    // 5
            Telephony.Carriers.SERVER,  // 6
            Telephony.Carriers.PASSWORD, // 7
            Telephony.Carriers.MMSC, // 8
            Telephony.Carriers.MCC, // 9
            Telephony.Carriers.MNC, // 10
            Telephony.Carriers.NUMERIC, // 11
            Telephony.Carriers.MMSPROXY,// 12
            Telephony.Carriers.MMSPORT, // 13
            Telephony.Carriers.AUTH_TYPE, // 14
            Telephony.Carriers.TYPE, // 15
            Telephony.Carriers.SOURCE_TYPE, // 16
            Telephony.Carriers.OMACPID,//17
            Telephony.Carriers.NAPID,//18
            Telephony.Carriers.PROXYID,//19
    };

    protected static final int ID_INDEX = 0;
    protected static final int NAME_INDEX = 1;
    protected static final int APN_INDEX = 2;
    protected static final int PROXY_INDEX = 3;
    protected static final int PORT_INDEX = 4;
    protected static final int USER_INDEX = 5;
    protected static final int SERVER_INDEX = 6;
    protected static final int PASSWORD_INDEX = 7;
    protected static final int MMSC_INDEX = 8;
    protected static final int MCC_INDEX = 9;
    protected static final int MNC_INDEX = 10;
    protected static final int NUMERIC_INDEX = 11;
    protected static final int MMSPROXY_INDEX = 12;
    protected static final int MMSPORT_INDEX = 13;
    protected static final int AUTH_TYPE_INDEX = 14;
    protected static final int TYPE_INDEX = 15;
    protected static final int SOURCE_TYPE_INDEX = 16;
    protected static final int APN_ID_INDEX = 17;
    protected static final int NAP_ID_INDEX = 18;
    protected static final int PROXY_ID_INDEX = 19;   
    
    
    public OmacpApn(Context context, int simId) {
        mContentResolver = context.getContentResolver();
        mSimId = simId;
    }
    
    public OmacpApn(Context context, int simId, Uri uri, String numeric) {
        mContentResolver = context.getContentResolver();
        mSimId = simId;
        mUri = uri;
        mNumeric = numeric;
    }
    
    /**
     * 
     * @return (_id, omacpid) pair sets which match numeric
     */
    public ArrayList<HashMap<String, String>> getExistOmacpId() {
        
        ArrayList<HashMap<String, String>> mOmacpIdSet = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> map = new HashMap<String, String>();
        
        
        String where = "numeric=\"" + mNumeric + "\"" + " and omacpid<>\'\'";

        Cursor cursor = mContentResolver.query(
                mUri, 
                new String[] {Telephony.Carriers._ID, Telephony.Carriers.OMACPID}, 
                where, 
                null, 
                Telephony.Carriers.DEFAULT_SORT_ORDER);

        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                map.put(cursor.getString(1), cursor.getString(0));//(omacpid,_id) pair
                mOmacpIdSet.add(map);
                cursor.moveToNext();
            }// end of while
            cursor.close();
        }
        Log.d(TAG, "getExistOmacpId size: " + mOmacpIdSet.size());
        return mOmacpIdSet;

    }

    public long insert(final Context context, ContentValues values) {
        Log.d(TAG, "insert");
        String id = null;
        Cursor cursor = null;

        String spn;
        String imsi;
        String pnn;
        boolean isMVNO = false;

        ITelephony telephony = ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));

        if (FeatureOption.MTK_MVNO_SUPPORT) {
            try {
                if (FeatureOption.MTK_GEMINI_SUPPORT) {
                    isMVNO = telephony.isIccCardProviderAsMvnoGemini(mSimId);
                } else {
                    isMVNO = telephony.isIccCardProviderAsMvno();
                }
            } catch (android.os.RemoteException e) {
                    Log.d(TAG, "RemoteException");
            }
        }
        Log.d(TAG, "isMVNO = " + isMVNO);
        if (FeatureOption.MTK_MVNO_SUPPORT && isMVNO) {
            try {
                if (FeatureOption.MTK_GEMINI_SUPPORT) {
                    spn = telephony.getSpNameInEfSpnGemini(mSimId);
                    imsi = telephony.isOperatorMvnoForImsiGemini(mSimId);
                    pnn = telephony.isOperatorMvnoForEfPnnGemini(mSimId);
                } else {
                    spn = telephony.getSpNameInEfSpn();
                    imsi = telephony.isOperatorMvnoForImsi();
                    pnn = telephony.isOperatorMvnoForEfPnn();
                }
                Log.d(TAG, "spn = " + spn);
                Log.d(TAG, "imsi = " + imsi);
                Log.d(TAG, "pnn = " + pnn);
                
                if (imsi != null && !imsi.isEmpty()) {
                    values.put(Telephony.Carriers.IMSI, imsi);
                } else if (spn != null && !spn.isEmpty()) {
                    values.put(Telephony.Carriers.SPN, spn);
                } else {
                    values.put(Telephony.Carriers.PNN, pnn);
                }
            } catch (android.os.RemoteException e) {
                Log.d(TAG, "RemoteException");
            }
        }

        try {
            Uri newRow = mContentResolver.insert(mUri, values);
            if (newRow != null) {
                Log.d(TAG, "uri = " + newRow);
                if (newRow.getPathSegments().size() == 2) {
                    id = newRow.getLastPathSegment();
                }
            }
        } catch (SQLException e) {
            Log.d(TAG, "insert SQLException happened!");
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        } 
        if (id != null) {
            return Long.parseLong(id);
        } else {
            return -1;
        }
    }

    
}
