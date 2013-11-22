
package com.example.musicservice;

import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MusicServiceActivity extends Activity {
    private static final String TAG = "MusicService";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_service);
        
        Toast.makeText(this, "MusicServiceActivity onCreate()", Toast.LENGTH_LONG).show();
        Log.d(TAG, "MusicServiceActivity onCreate()");
        
        initlizeViews();
    }

    private void initlizeViews(){
        Button btnStart = (Button)findViewById(R.id.startMusic);
        Button btnStop = (Button)findViewById(R.id.stopMusic);
        Button btnBind = (Button)findViewById(R.id.bindMusic);
        Button btnUnbind = (Button)findViewById(R.id.unbindMusic);
        
        final ServiceConnection conn = new ServiceConnection() {
            
            @Override
            public void onServiceDisconnected(ComponentName name) {
                // TODO Auto-generated method stub
                Toast.makeText(MusicServiceActivity.this, "MusicServiceActivity onServiceDisconnected()", Toast.LENGTH_LONG).show();
                Log.d(TAG, "MusicServiceActivity onServiceDisconnected()");
            }
            
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                // TODO Auto-generated method stub
                Toast.makeText(MusicServiceActivity.this, "MusicServiceActivity onServiceConnected()", Toast.LENGTH_LONG).show();
                Log.d(TAG, "MusicServiceActivity onServiceConnected()");
            }
        };
        
        OnClickListener click = new OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(MusicServiceActivity.this, MusicService.class);
                switch (v.getId()) {
                    case R.id.startMusic:
                        startService(intent);
                        break;
                    case R.id.stopMusic:
                        stopService(intent);
                    case R.id.bindMusic:
                        bindService(intent, conn, Context.BIND_AUTO_CREATE);
                    case R.id.unbindMusic:
                        unbindService(conn);
                    default:
                        break;
                }
            }
            
        };
        
        btnStart.setOnClickListener(click);
        btnStop.setOnClickListener(click);
        btnBind.setOnClickListener(click);
        btnUnbind.setOnClickListener(click);
        
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.music, menu);
        return true;
    }

}
