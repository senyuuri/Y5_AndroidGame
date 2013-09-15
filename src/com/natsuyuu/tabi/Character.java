package com.natsuyuu.tabi;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class Character {
	private float x;
	private float y;
	private float r;
	private Bitmap bmp;

	public Character(float x, float y, float r, Bitmap bmp) {
		this.x = x;
		this.y = y;
		this.r = r;
		this.bmp = bmp;
	}
	

	public void drawSelf(Canvas canvas, Paint paint) {
		//canvas.drawCircle(x, y, r, paint);
		canvas.drawBitmap(bmp,x-r,y-r-25, paint);
	}
	//Set x-coordinate
	public void setX(float x) {
		this.x = x;
	}
	//Set y-coordinate 
	public void setY(float y) {
		this.y = y;
	}


	//Get radius
	public float getR() {
		return r;
	}
}
