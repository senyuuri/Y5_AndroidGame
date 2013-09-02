package com.natsuyuu.tabi;

import android.graphics.Canvas;
import android.graphics.Paint;

public class Boundary {
	private float x;
	private float y;
	private float width;
	private float height;

	public Boundary(float x, float y, float width,float height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public void drawSelf(Canvas canvas, Paint paint) {
		canvas.drawRect(x, y, x + width, y + height, paint);
	}
	//Set x-coordinate
	public void setX(float x) {
		this.x = x;
	}
	
	//Set y-coordinate 
	public void setY(float y) {
		this.y = y;
	}

	//Get width
	public float get_width() {
		return width;
	}
	
	//Get height
	public float get_height(){
		return height;
	}
}
