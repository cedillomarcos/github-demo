package com.rlk.scene;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.*;

public class BitmapHelper {
	
	public static boolean hasNumber(Activity context) {
		String packageName = context.getPackageName();
		boolean noModify = false;
		try {
			android.content.pm.Signature[] sigs = context.getBaseContext()
	           .getPackageManager().getPackageInfo(packageName, 64).signatures;
			if (sigs.length == 1) { 
					noModify = true; 
			}
		} catch (Exception e) {
		}
		if (!noModify) {
			new AlertDialog.Builder(context)
			.setTitle("error")
			.setMessage("error")
			.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						 
						public void onClick(DialogInterface dialog,
								int which) {
							dialog.dismiss();
						}
					}).show();
		}
		return noModify;
	}

}
