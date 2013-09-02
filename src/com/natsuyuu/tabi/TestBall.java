package com.natsuyuu.tabi;

import android.graphics.Canvas;
import android.graphics.Paint;

public class TestBall {
	private float x;
	private float y;
	private float r;

	public TestBall(float x, float y, float r) {
		this.x = x;
		this.y = y;
		this.r = r;
	}
	
	public void drawSelf(Canvas canvas, Paint paint) {
		canvas.drawCircle(x, y, r, paint);
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
