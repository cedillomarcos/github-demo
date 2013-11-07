package com.rlk.scene.items;

import android.R.integer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;

public class MyButton extends TouchableDrawableItem {
  Matrix matrix=new Matrix();	
	MyButton(Context context) {
		super(context); 
	}
	public MyButton(Context context,int x,int y ,int width,int height,int id) {
		super(context,x,y,width,height,id); 
	}
	MyButton(Context context,int x,int y ,int width,int height,int id1,int id2) {
		super(context,x,y,width,height,id1,id2); 
	}
}
