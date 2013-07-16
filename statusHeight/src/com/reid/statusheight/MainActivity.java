
package com.reid.statusheight;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.Window;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Rect frame = new Rect();
        getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;
        
        int contentTop = getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();

        //statusBarHeight是上面所求的状态栏的高度
        int titleBarHeight = contentTop - statusBarHeight;
        Log.v("liyang", "statusBarHeight = " + statusBarHeight + ", titleBarHeight = " + titleBarHeight);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

}
