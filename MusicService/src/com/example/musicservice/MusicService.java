package com.example.musicservice;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class MusicService extends Service {
    private static final String TAG="MusicService";
    private MediaPlayer mPlayer;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        Toast.makeText(this, "MusicService onCreate()", Toast.LENGTH_LONG).show();
        Log.d(TAG, "MusicService onCreate()");
        
        mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.music);
        mPlayer.setLooping(true);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        Toast.makeText(this, "MusicService onStartCommand()", Toast.LENGTH_LONG).show();
        Log.d(TAG, "MusicService onStartCommand()");
        
        mPlayer.start();
        
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        Toast.makeText(this, "MusicService onDestroy()", Toast.LENGTH_LONG).show();
        Log.d(TAG, "MusicService onDestroy()");
        
        mPlayer.stop();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        Toast.makeText(this, "MusicService onBind()", Toast.LENGTH_LONG).show();
        Log.d(TAG, "MusicService onBind()");
        
        mPlayer.start();
        
        return null;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // TODO Auto-generated method stub
        Toast.makeText(this, "MusicService onUnbind()", Toast.LENGTH_LONG).show();
        Log.d(TAG, "MusicService onUnbind()");
        
        mPlayer.stop();
        
        return super.onUnbind(intent);
    }
    
}
