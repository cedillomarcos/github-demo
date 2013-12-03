
package com.example.mypreference;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MyPreferenceActivity extends Activity {
    
    public static final int SET = Menu.FIRST;
    public static final int EXIT = Menu.FIRST+1;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_preference);
    }

    //创建Menu菜单
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0,SET,0,"设置");
        menu.add(0,EXIT,0,"退出");
        return super.onCreateOptionsMenu(menu);
    }

    //点击Menu菜单选项响应事件 
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
        case 1:
            Intent mIntent = new Intent();
            mIntent.setClass(this, MyPreference.class);
            startActivity(mIntent);
            break;
        case 2:
            finish();
            break;
        }
        return super.onOptionsItemSelected(item);
    }
}