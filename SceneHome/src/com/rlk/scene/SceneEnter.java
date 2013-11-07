package com.rlk.scene;

import com.rlk.scene.R;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message; 

public class SceneEnter extends Activity {

	private final static int  START_DELAY = 0;
	Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case START_DELAY:
				Intent intent = new Intent();
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				intent.setComponent(new ComponentName("com.rlk.scene","com.rlk.scene.MainActivity")); 
				startActivity(intent);
				finish();
				break;

			default:
				break;
			}
			
		}
		
	};
	@Override
	protected void onCreate(Bundle arg0) { 
		super.onCreate(arg0); 
		setContentView(R.layout.enter_view);
		mHandler.sendEmptyMessageDelayed(START_DELAY, 800);
	}

}
