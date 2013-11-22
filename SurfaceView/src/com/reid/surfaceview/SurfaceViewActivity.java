
package com.reid.surfaceview;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SurfaceViewActivity extends Activity {
    private static final String TAG = "SurfaceViewActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new MyView(this));
    }
    
    //内部类
    class MyView extends SurfaceView implements SurfaceHolder.Callback{
        SurfaceHolder holder;
        
        public MyView(Context context) {
            super(context);
            // TODO Auto-generated constructor stub
            holder = this.getHolder();
            holder.addCallback(this);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            // TODO Auto-generated method stub
            Log.d(TAG, "surfaceChanged");
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            // TODO Auto-generated method stub
            Log.d(TAG, "surfaceCreated");
            new Thread(new MyThread()).start();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // TODO Auto-generated method stub
            Log.d(TAG, "surfaceDestroyed");
        }
        
        //内部类的内部类
        class MyThread implements Runnable{

            @Override
            public void run() {
                // TODO Auto-generated method stub
                Canvas canvas = holder.lockCanvas(null);//获取画布
                Paint mPaint = new Paint();
                mPaint.setColor(Color.BLUE);
                
                canvas.drawRect(new RectF(40, 60, 80, 80),  mPaint);
                holder.unlockCanvasAndPost(canvas);//解锁画布，提交画好的图像
            }
            
        }
    }
}
