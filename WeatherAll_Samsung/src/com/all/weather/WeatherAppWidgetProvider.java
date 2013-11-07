package com.all.weather;



import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class WeatherAppWidgetProvider extends AppWidgetProvider {
	private String Tag = "[WeatherAppWidgetProvider]---";

	@Override
	public void onDisabled(Context context) {
		// TODO Auto-generated method stub
	    Log.i(Tag,"----onDisabled()----");
	    TimeUpdateService.isRemoteView=false;
    Intent mIntent = new Intent();
       mIntent.setClass(context, TimeUpdateService.class);
       PendingIntent mPendingIntent = PendingIntent.getService(context, 0, mIntent, 0);
       mPendingIntent.cancel();
        context.stopService(new Intent().setClass(context, TimeUpdateService.class));
		super.onDisabled(context);
	}  

	@Override
	public void onEnabled(Context context) {
		// TODO Auto-generated method stub
	    Log.i(Tag,"----onEnabled()----");
	    TimeUpdateService.isRemoteView=true;
	    TimeUpdateService.restartUpdateService(context);
		super.onEnabled(context);
	}

	@Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    	Log.i(Tag,"----update()----");
    	 TimeUpdateService.restartUpdateService(context);
//    	context.startService(new Intent().setClass(context, TimeUpdateService.class));        
    }
    @Override
    public void onReceive(Context context, Intent intent) {
    	Log.i(Tag,"-----onreceive()-----"+intent.getAction());
    	//add by xzg for time dont run begin
    	if("android.intent.action.TIME_SET".equals(intent.getAction()));
    	context.startService(new Intent().setClass(context, TimeUpdateService.class));
    	//add by xzg for time dont run end
        super.onReceive(context, intent);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.i(Tag, "----onDelete()-----");
//        context.stopService(new Intent().setClass(context, TimeUpdateService.class));
        super.onDeleted(context, appWidgetIds);
    }

}
