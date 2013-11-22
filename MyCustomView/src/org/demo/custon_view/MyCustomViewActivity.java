package org.demo.custon_view;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

public class MyCustomViewActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        View v = new NinePointView(MyCustomViewActivity.this);
        View v = new NinePointLineView(MyCustomViewActivity.this);
        setContentView(v);
        
    }
}