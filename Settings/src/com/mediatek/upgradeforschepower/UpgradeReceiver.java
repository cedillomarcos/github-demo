package com.mediatek.upgradeforschepower;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.mediatek.xlog.Xlog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class UpgradeReceiver extends BroadcastReceiver {

    private static final String SCHPWRS_DB_PATH = "data/data/com.android.settings/databases/schpwrs.db";
    private static final String TEMP_DB_PATH = "/data/schpwrs.db";
    private static final String TAG = "UpgradeReceiver";
    private File mSettingSchPwrsDbFile;

    @Override
    public void onReceive(Context context, Intent intent) {
        Xlog.v(TAG, "onReceive = " + intent.getAction());
        mSettingSchPwrsDbFile = new File(SCHPWRS_DB_PATH);
        if (mSettingSchPwrsDbFile.exists()) {
            CopySchPwrsDbTask copySchPwrsDbTask = new CopySchPwrsDbTask();
            copySchPwrsDbTask.execute();
        } else {
            Xlog.w(TAG, "com.android.settings/databases/schpwrs.db does not exist");
        }
    }

    private class CopySchPwrsDbTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... arg0) {
            return copyDbFileToPhoneStorage();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Xlog.v(TAG, "copy db file to data/ result  " + result);
        }
    }

    private boolean copyDbFileToPhoneStorage() {
        File tempDbFile = new File(TEMP_DB_PATH);
        FileInputStream fis = null;
        FileOutputStream fos = null;
        try {
            fis = new FileInputStream(mSettingSchPwrsDbFile);
            fos = new FileOutputStream(tempDbFile);
        } catch (FileNotFoundException e) {
            Xlog.w(TAG, "FileNotFoundException " + e.getMessage());
            return false;
        }
        byte[] buffer = new byte[1024];
        int length = 0;
        try {
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
            fos.flush();
            fos.close();
            fis.close();
        } catch (IOException e) {
            Xlog.w(TAG, "IOException " + e.getMessage());
            return false;
        }
        return true;
    }
}
