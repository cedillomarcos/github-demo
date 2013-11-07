package com.android.settings.ext;


import android.content.Intent;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;

public interface ISimManagementExt {
    /**
     * Remove the Auto_wap push preference screen
     * @param parent parent preference to set
     */
   void updateSimManagementPref(PreferenceGroup parent);

    /**
     *Update change Data connect dialog state.
     *@Param preferenceFragment
     *@dialogID
     **/
    void dealWithDataConnChanged(Intent intent, boolean isResumed);
    
    /**
     *Show change data connection dialog
     *@Param preferenceFragment
     *@dialogID
     **/
    void showChangeDataConnDialog(PreferenceFragment prefFragment);
        
    /**
     *Set to close sim slot id
     *@param simSlot
     **/
    void setToClosedSimSlot(int simSlot);
}