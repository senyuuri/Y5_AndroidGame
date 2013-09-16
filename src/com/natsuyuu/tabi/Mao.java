package com.natsuyuu.tabi;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class Mao {
	private float x;
	private float y;
	private float r;
	private Boolean ground;
	private Bitmap bitmap;

	public Mao(float x, float y, float r, Bitmap bitmap) {
		this.x = x;
		this.y = y;
		this.r = r;
		this.bitmap = bitmap;
		ground = false;
		
	}
	
	public void drawSelf(Canvas canvas, Paint paint) {
		//canvas.drawCircle(x, y, r, paint);
		canvas.drawBitmap(bitmap, x-r, y-r, paint);
	}
	//Set x-coordinate
	public void setX(float x) {
		this.x = x;
	}
	//Set y-coordinate 
	public void setY(float y) {
		this.y = y;
	}

	public void setGround(Boolean g){
		this.ground = g;
	}
	
	//Get radius
	public float getR() {
		return r;
	}
	
	public Boolean getGround(){
		return ground;
	}
}

