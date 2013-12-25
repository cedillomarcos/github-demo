package com.example.widget;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.text.format.Time;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
  
public class ExampleAppWidgetProvider extends AppWidgetProvider{  
    private String[] months={"一月","二月","三月","四月","五月","六月","七月","八月","九月","十月","十一月","十二月"};  
    private String[] days={"星期日","星期一","星期二","星期三","星期四","星期五","星期六"};  
    private ArrayList<Integer> mImages;
    private Context mContext;
    
    @Override  
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,  
            int[] appWidgetIds) {  
        // TODO Auto-generated method stub  
        mContext = context;
        Log.d("", "f");
        RemoteViews remoteViews=new RemoteViews(context.getPackageName(), R.layout.myappwidget);  
        Time time=new Time();  
        time.setToNow();  
        String month=time.year+" "+months[time.month];  
        remoteViews.setTextViewText(R.id.txtDay, new Integer(time.monthDay).toString());  
        remoteViews.setTextViewText(R.id.txtMonth, month);  
        remoteViews.setTextViewText(R.id.txtWeekDay, days[time.weekDay]);  
        Intent intent=new Intent("cn.com.karl.widget.click");  
        PendingIntent pendingIntent=PendingIntent.getBroadcast(context, 0, intent, 0);  
        remoteViews.setOnClickPendingIntent(R.id.layout, pendingIntent);  
        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);  
          
        super.onUpdate(context, appWidgetManager, appWidgetIds);  
    }  
      
    private void findWallpapers() {
        mImages = new ArrayList<Integer>(24);
        final Resources resources = mContext.getResources();
        final String packageName = resources.getResourcePackageName(R.array.wallpapers);
        final String[] extras = resources.getStringArray(R.array.wallpapers);
        
        for (String extra : extras) {
            int res = resources.getIdentifier(extra, "drawable", packageName);
            if (res != 0) {
                    mImages.add(res);
            }
        }
    }
    
    private int getRandomWallpaperPosition(){
        int position = 0;
        int count = mImages.size();
        Random random = new Random();
        position = random.nextInt(count);
        
        return position;
    }
    
    @Override  
    public void onReceive(Context context, Intent intent) {  
        // TODO Auto-generated method stub  
        super.onReceive(context, intent);  
        if(intent.getAction().equals("cn.com.karl.widget.click")){  
            Toast.makeText(context, "点击了widget日历", 1).show();  
            /*WallpaperManager wpm = (WallpaperManager) getActivity().getSystemService(
                    Context.WALLPAPER_SERVICE);
            wpm.setResource(mImages.get(position));*/
            WallpaperManager wpm = WallpaperManager.getInstance(context);
            try {
                wpm.setResource(mImages.get(getRandomWallpaperPosition()));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }    
        }  
    }  
} 