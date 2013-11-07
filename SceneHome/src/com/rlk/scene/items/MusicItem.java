package com.rlk.scene.items;

import android.R.integer;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.MotionEvent;

import com.android.music.IMediaPlaybackService;
import com.rlk.scene.R;
import com.rlk.scene.SceneSurfaceView2;
import com.rlk.scene.Utilities;
import com.rlk.scene.items.MySeekbar.OnProgressChangedListener;
import com.rlk.scene.items.MySeekbar.OnStartTrackingListener;

public class MusicItem extends TouchableDrawableGroup implements ServiceConnection{

		    Bitmap mMusicBg;
		    Bitmap mMusicNext;
		    Bitmap mMusicPre;
		    Bitmap mMusicPlay;
		    Bitmap mMusicPause;
		    Bitmap mMusicSeekBarBg;
		    Bitmap mMusicSeekBarProgress;
		    Bitmap mMusicSeekBarIcon;
			Context mContext;
		    Paint mMusicPaint;
		    int mSeekbarIconWidth;
		    int mSeekbarIconHeight;
		    int mWidth;
		    int mHeight;
		    int mBtnWidth;
		    int mBtnHeight;
		    float mProgress;
		    int mProgressWidth;
		    int mProgressHeight;
		    int mtest=0;
		    long mDuring =0;
		    String mTitle="Unknown";
	        int mMusicPositionX;
	        int mMusicPositionY;
	        boolean Debug=true;
	        private IMediaPlaybackService mService = null;
	        Handler mHandler =new Handler();
	        private boolean isPaused=true;
	        private MyButton playBtn;
	        private MyButton nextBtn;
	        private MyButton preBtn;
	        private MySeekbar mSeekbar;
		    public MusicItem(Context context ,int x,int y ,int width,int height) {
		    	super(context, x, y, width, height);
		        mContext=context;
		        Resources res=mContext.getResources();
		        mMusicBg=BitmapFactory.decodeResource(res, R.drawable.music_bg);
		        mMusicNext=BitmapFactory.decodeResource(res, R.drawable.music_next_enable);
		        mMusicPre=BitmapFactory.decodeResource(res, R.drawable.music_pre_enable);
		        mMusicPlay=BitmapFactory.decodeResource(res, R.drawable.music_play);
		        mMusicPause=BitmapFactory.decodeResource(res, R.drawable.music_pause);
		        mMusicSeekBarBg=BitmapFactory.decodeResource(res, R.drawable.music_progress1);
		        mMusicSeekBarProgress=BitmapFactory.decodeResource(res, R.drawable.music_progress2);
		        mMusicSeekBarIcon=BitmapFactory.decodeResource(res, R.drawable.music_progressicon);
		        mMusicPaint=new Paint();
		        mMusicPaint.setColor(Color.rgb(200, 200, 200));
		        
		        mSeekbarIconWidth=mMusicSeekBarIcon.getWidth();
		        mSeekbarIconHeight=mMusicSeekBarIcon.getHeight();
		        mWidth=mMusicBg.getWidth();
		        //mHeight=mMusicBg.getHeight();
		        mBtnWidth=mMusicNext.getWidth()+30;
		        mBtnHeight=mMusicNext.getHeight()+10;
		        mProgressWidth=mMusicSeekBarProgress.getWidth();
		        mProgressHeight=mMusicSeekBarProgress.getHeight();
		        mMusicPositionX=2;
		        mMusicPositionY=728;
		        playBtn= new MyButton(context, 1418, 316,mBtnWidth, mBtnHeight,R.drawable.music_play);
		        playBtn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(TouchableDrawableItem item, MotionEvent event) {
						// TODO Auto-generated method stub
						if(mService!=null)
						{
							try {
								if(mService.isPlaying())
								{
									mService.pause();
									item.setBackGroudAndInvalidate(mMusicPlay);
								}else {
									mService.play();
									item.setBackGroudAndInvalidate(mMusicPause);
								}
							} catch (RemoteException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}
					}
				});
		        nextBtn= new MyButton(context, 1364, 436,mBtnWidth, mBtnHeight,R.drawable.music_next_enable,R.drawable.music_next_disable);
		        nextBtn.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(TouchableDrawableItem item, MotionEvent event) {
						// TODO Auto-generated method stub
						if(mService!=null)
						{
							try {
								mService.next();
							} catch (RemoteException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}
						
						
					}
				});
		        preBtn=  new MyButton(context, 1450, 436,mBtnWidth, mBtnHeight,R.drawable.music_pre_enable,R.drawable.music_pre_disable);
		        preBtn.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(TouchableDrawableItem item, MotionEvent event) {
						// TODO Auto-generated method stub
						if(mService!=null)
						{
							try {
								mService.prev();
							} catch (RemoteException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
						}
					}
				});
		        mSeekbar=new MySeekbar(context, 1357,417, 179, 4);
		        mSeekbar.setOnProgressChangedListener(new OnProgressChangedListener() {
					
					@Override
					public void OnChanged(TouchableDrawableItem item, int progress) {
						// TODO Auto-generated method stub
						setProgressAndUpdate(progress);
					}
				});
		        mSeekbar.setOnStartTrackingListener(new OnStartTrackingListener() {
					
					@Override
					public void OnStart(TouchableDrawableItem item) {
						// TODO Auto-generated method stub
					}
				});
		        //mSeekbar.isDrawBlackBg=true;
		        addItem(playBtn);
		        addItem(nextBtn);
		        addItem(preBtn);
		        addItem(mSeekbar);
		    }
		    public boolean drawItem(Canvas canvas, int spacX, boolean drawed) {
		    	
		    	updateTouchRect(spacX);
		    	if(!isInsight(spacX) && !drawed)
		    		return false ;
//		    	Log.d("ningyaoyun", "MusicItem drawItem");
		    	canvas.save();
				canvas.translate(-spacX + mMusicPositionX, mMusicPositionY);
		        float rate=mProgress/100f;
		        String mProcessToDraw=duringToString((long) (mDuring*rate));
		        String mDuringToDraw =duringToString((long) (mDuring));
		        Matrix matrix=new Matrix();
		        //canvas.drawBitmap(mMusicBg, matrix, mMusicPaint);
		        
		        
		        //liuwei
		      /*  matrix.setTranslate(mWidth/2-100-mBtnWidth, 50);
		        canvas.drawBitmap(mMusicPre, matrix, mMusicPaint);
		        matrix.setTranslate((mWidth-mBtnWidth)/2, 50);
		        canvas.drawBitmap(mMusicPlay, matrix, mMusicPaint);
		        matrix.setTranslate(mWidth/2+100, 50);
		        canvas.drawBitmap(mMusicNext, matrix, mMusicPaint);*/
		    	/*for (TouchableDrawableItem item : mItems) {
		    		item.drawItem(canvas, spacX);
				}*/
		        //infos
		        mMusicPaint.setTextSize(12);
		        int textWidth= getStringWidth(mDuringToDraw,mMusicPaint);
		        canvas.drawText(duringToString((long) (mDuring*rate)), 60, 20, mMusicPaint);
		        canvas.drawText(duringToString(mDuring), 475-textWidth, 20, mMusicPaint);
		        mMusicPaint.setTextSize(20);
		        int textWidth2= getStringWidth(mTitle,mMusicPaint);
		        canvas.drawText(mTitle, (mWidth-textWidth2)/2, 20, mMusicPaint);
		        //progressbar
		        matrix.setTranslate(53, 30);
		        canvas.drawBitmap(mMusicSeekBarBg, matrix, mMusicPaint);
		        matrix.setScale(rate, 1, 0, 0);
		        matrix.postTranslate(53, 30);
		        canvas.drawBitmap(mMusicSeekBarProgress, matrix, mMusicPaint);
		        matrix.setTranslate(53-mSeekbarIconWidth/2+rate*mProgressWidth, 32-mSeekbarIconHeight/2);
		        canvas.drawBitmap(mMusicSeekBarIcon, matrix, mMusicPaint);
		        canvas.restore();
		        return  super.drawItem(canvas, spacX ,drawed);
		       
		    }
		    
		    private int getStringWidth(String string,Paint paint)
		    {
		    	return (int) paint.measureText(string);
		    }
		    /**
		     * 
		     * @param progress  0~100
		     */
		    public void setProgress(int progress)
		    {
		    	if(progress>=0&&progress<=100)
		    	{
		    		mProgress=progress;
		    	}
		    }
		    
		    public void setProgressAndUpdate(int progress)
		    {
		    	if(progress>=0&&progress<=100)
		    	{
		    		mProgress=progress;
		    	}
		    	float rate =((float)mProgress)/100f;
		    	if(mService!=null)
	        	{  
	        		try {
	        			mService.seek((long)(rate*mDuring));
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	        		catch (Exception e) {
						// TODO: handle exception
					}
	        	}
		    	SceneSurfaceView2.instance.drawScene(SceneSurfaceView2.defaultX,SceneSurfaceView2.defaultY);
		    }
		    
		    public void setProgress(long progress)
		    {
		    	if(progress>=0&&progress<=mDuring)
		    	{
		    		mProgress=((float)progress/(float)mDuring)*100;
		    	}
		    }
		    
		    
		    public void setDuration(long during)
		    {
		    	mDuring=during;
		    }
		    private String duringToString(long during)
		    {
		    		if(during<=0)
		    			return   "00:00";
		    		double duration = 0;
		    		int minute   = 0,
		    			second   = 0,
		    			hour      =0;
		    		duration=during;
		    		duration=Math.ceil(duration/1000);
		    		hour=(int) (duration)/3600;
		    		minute = (int) (duration)/60-hour*60;
		    		second = (int) (duration) %60;
		    		if(hour<=0)
		    		    return String.format("%02d:%02d",minute,second);
		    		else {
		    			return String.format("%02d:%02d:%02d",hour,minute,second);	
					}
		    }
	public void setTitle(String title) {
		    	if(title != null){
			if (title.length() >= 16) {
				mTitle = title.substring(0, 16) + "...";
		    	}else {
		    	mTitle=title;
				}
		    	}		    	
		    }
		    private final Runnable mClockTick = new Runnable () {
		        @Override
		        public void run() {
		        	if(!isPaused)
		        	{
		        		if(mService!=null)
			        	{  
			        		try {
			        			long during=mService.duration();
								setDuration(during);
								if(during<=0)
								{
									playBtn.setEnable(false);
									mSeekbar.setEnable(false);
								}
								else {
									playBtn.setEnable(true);
									mSeekbar.setEnable(true);
								} 
								if(mService.isPlaying())
								{
									playBtn.setBackGroud(mMusicPause);
								}else {
									playBtn.setBackGroud(mMusicPlay);
								}
								setProgress(mService.position());
					        	setTitle(mService.getTrackName());
							} catch (RemoteException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
			        		catch (Exception e) {
								// TODO: handle exception
							}
			        	}
//		        		Log.d("ningyaoyun", "MusicItem mCurScreen=" + SceneSurfaceView2.instance.mCurScreen 
//		        				+ ";isScrollFinished=" + SceneSurfaceView2.instance.isScrollFinished());
		        		if(SceneSurfaceView2.instance.mCurScreen == 0 && SceneSurfaceView2.instance.isScrollFinished()){
		        			SceneSurfaceView2.instance.drawScene(SceneSurfaceView2.defaultX, SceneSurfaceView2.defaultY);	
		        		} 
			            mHandler.postDelayed(mClockTick, 1000);
		        	}
		        }
		    };
			@Override
			public void onServiceConnected(ComponentName arg0, IBinder arg1) {
				// TODO Auto-generated method stub
				mService = IMediaPlaybackService.Stub.asInterface(arg1); 
				if(mService!=null)
				{
					try {
						if(mService.isPlaying())
						{
							playBtn.setBackGroudAndInvalidate(mMusicPause);
						}else {
							playBtn.setBackGroudAndInvalidate(mMusicPlay);
						}
					} catch (RemoteException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				playBtn.setEnable(true);
				nextBtn.setEnable(true);
				preBtn.setEnable(true);
			}
			@Override
			public void onServiceDisconnected(ComponentName arg0) {
				// TODO Auto-generated method stub
				mService=null;
				playBtn.setEnable(false);
				nextBtn.setEnable(false);
				preBtn.setEnable(false);
			}
	public void onPause()
	{
		   isPaused=true;
		   mContext.unbindService(this);
           mHandler.removeCallbacks(mClockTick);
		   
	}
	
	public void stopClock(){
		mHandler.removeCallbacks(mClockTick);
	}

	public void startClock() {
		if (SceneSurfaceView2.instance.mCurScreen == 0) {
			mHandler.removeCallbacks(mClockTick);
			mHandler.post(mClockTick);
		}
	}
	public void onResume()
	{
	  isPaused=false;
      mContext.bindService(new Intent("com.android.music.MediaPlaybackService"), this, Context.BIND_AUTO_CREATE);
      if(SceneSurfaceView2.instance.mCurScreen == 0){
    	  mHandler.removeCallbacks(mClockTick);
          mHandler.post(mClockTick);
      }
      
	}
	public void recycle()
	{
		Utilities.recycleBitmap(mMusicBg);
		Utilities.recycleBitmap(mMusicNext);
		Utilities.recycleBitmap(mMusicPre);
		Utilities.recycleBitmap(mMusicPlay);
		Utilities.recycleBitmap(mMusicSeekBarBg);
		Utilities.recycleBitmap(mMusicSeekBarProgress);
		Utilities.recycleBitmap(mMusicSeekBarIcon);
		Utilities.recycleBitmap(mMusicPause);
		isPaused=true;
	}
	
}
