package com.android.internal.policy.impl.keyguard;

public interface CenterKeyListener {
	int LOCKSCREEN_LEVEL_NORMAL = 0;
    int LOCKSCREEN_LEVEL_CHARGING = 1;
    int LOCKSCREEN_LEVEL_MUSIC_PLAY = 2;
    int LOCKSCREEN_LEVEL_MUSIC_PAUSE = 3;
    int LOCKSCREEN_LEVEL_FM_PLAY = 4;
    int LOCKSCREEN_LEVEL_FM_PAUSE = 5;
    int LOCKSCREEN_LEVEL_FLASH_LIGHT_ON = 6;
    int LOCKSCREEN_LEVEL_FLASH_LIGHT_OFF = 7;
    int LOCKSCREEN_LEVEL_HANDLER_MOVE = 10;
    
    int STATE_FM = 1;
    int STATE_MUSIC = 2;
    int STATE_ERROR = -1;
    
	void unlock();
	void changeState(int state);
	int getCenterKeyState();
	int handleDoubleClick();
	boolean handleLongPressed();
	boolean gotoFM();
	boolean gotoMusic();
}
