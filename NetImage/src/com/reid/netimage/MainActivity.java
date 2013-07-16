
package com.reid.netimage;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.reid.service.ImageService;

import java.io.IOException;

public class MainActivity extends Activity {
    private EditText editText;
    private Button button;
    private ImageView imageView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        editText = (EditText)findViewById(R.id.link);
        button = (Button)findViewById(R.id.bnt);
        imageView = (ImageView)findViewById(R.id.img);
        
        button.setOnClickListener(new linkListener());
    }

    private  class linkListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            String path = editText.getText().toString();
            try {
                byte[] data = ImageService.getImage(path);
                Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
                imageView.setImageBitmap(bm);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), R.string.error, Toast.LENGTH_LONG).show();
            }
        }
        
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

}
