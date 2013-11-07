package com.all.weather;




import com.all.weather.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class Icontypesetting extends Activity implements OnClickListener{
	
	TextView curentstyle;
	ImageView icon;
	Button pre;
	Button select;
	Button next;
	int origin;
	int intstyle;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.widgitstyle_setting);		 
		initviews();
		addclickliteners();		
	}
	
	private void initviews()
	{
		String style  = "1";//SettingsManager.getInstance().getWeatherType(this);
		intstyle = Integer.parseInt(style);
		origin = intstyle;
		curentstyle=(TextView)findViewById(R.id.curentstyle);
		curentstyle.setText("("+style+"/3)");
		icon=(ImageView)findViewById(R.id.icon);
		icon.setBackgroundResource(AccuIconMapper.getDrawableIdByIconId(style));
		pre=(Button)findViewById(R.id.pre);
		select=(Button)findViewById(R.id.select);
		next=(Button)findViewById(R.id.next);
	}
	
	private void addclickliteners()
	{
		pre.setOnClickListener(this);
		select.setOnClickListener(this);
		next.setOnClickListener(this);
	}
	
	public void onClick(View v) {
		if(v==pre)
		{
			intstyle--;
			if(intstyle<=0)
				intstyle=3;
			curentstyle.setText("("+intstyle+"/3)");
			icon.setBackgroundResource(AccuIconMapper.getDrawableIdByIconId(""+intstyle));
			  }
		else if(v==next)
		{
			intstyle++;
			if(intstyle>3)
				intstyle=1;			
			curentstyle.setText("("+intstyle+"/3)");
			icon.setBackgroundResource(AccuIconMapper.getDrawableIdByIconId(""+intstyle));
        }
		else
		{
			if(origin != intstyle)
				if(intstyle>3);
//					SettingsManager.getInstance().setWeatherType( ""+1, this, false);
				else
				    ;
//				SettingsManager.getInstance().setWeatherType( ""+intstyle, this, false);
//				SettingsManager.getInstance().setWeatherType(SettingsManager.STORAGEID, "1", this, false);
			
				Intent mIntent = new Intent();	
				setResult(RESULT_OK, mIntent);
				finish();		     
        }
		
	}
}
