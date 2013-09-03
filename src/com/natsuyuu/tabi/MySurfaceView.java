package com.natsuyuu.tabi;

import java.util.Timer;
import java.util.TimerTask;

import javax.security.auth.PrivateCredentialPermission;
import javax.xml.transform.Templates;

import org.jbox2d.collision.AABB;
import org.jbox2d.collision.CircleDef;
import org.jbox2d.collision.PolygonDef;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.ContactListener;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.ContactPoint;
import org.jbox2d.dynamics.contacts.ContactResult;



import android.R.integer;
import android.app.Service;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.SurfaceHolder.Callback;

public class MySurfaceView extends SurfaceView implements Callback, Runnable, ContactListener {
	private Thread th;
	private SurfaceHolder sfh;
	private Canvas canvas;
	private Paint paint;
	private boolean flag;
	// --- New physics world ---
	// ratio between screen(px) and world(m)
	private final float RATE = 10f;
	private World world;
	private AABB aabb;
	private Vec2 gravity;
	private float timeStep = 1f / 30f;
	// For result accuracy, large value may cause latency
	private int iterations = 5;
	// Main body
	private Body mcharacter;
	
	// Win/Lost collision point
	
	// Screen width & height
	private int screenW, screenH;
	// --- Game Status ---
	private final int GAMESTATE_MENU = 0;
	private final int GAMESTATE_HELP = 1;
	private final int GAMESTATE_GAMEING = 2;
	// TO-DO change status back to menu
	private int gameState = GAMESTATE_MENU;
	private boolean gameIsPause, gameIsLost, gameIsWin;
	// Body bitmap resources

	// Menu, button, background bitmap resources
	private Bitmap bmpMenu_help, bmpMenu_play, bmpMenu_exit, bmpMenu_resume, bmpMenu_replay, bmp_menubg, bmp_gamebg, bmpMenuBack, bmp_smallbg, bmpMenu_menu,
			bmp_helpbg, bmpBody_lost, bmpBody_win, bmpWinbg, bmpLostbg;
	// Create button
	
	// --- Sensor ---
	private SensorManager sm;
	private Sensor sensor;
	private SensorEventListener mySensorListener;
	// Accelerator value
	private float acc_x,acc_y,acc_z,temp_y;

	//Log Tag
	private String TAG = "TABI_status";
	
	//Temp
	private Body testball;
	private Body contact_test;
	private int collision_status;
	
	//Boundary
	private Body boundary_bottom;
	private Body boundary_left;
	private Body boundary_right;
	
	//Game data
	private int life, remainjumb, level, meteoro_num, aim_num, remain_time;
	
	//Timer
	public Timer timer;
	public TimerTask timertask = new TimerTask() { 
		@Override 
		public void run() { 
		// TODO Auto-generated method stub 
			Message message = new Message(); 
			message.what = 1; 
			handler.sendMessage(message); 
		} 
	}; 
	
	
	public MySurfaceView(Context context) {
		super(context);
		this.setKeepScreenOn(true);
		sfh = this.getHolder();
		sfh.addCallback(this);
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Style.STROKE);
		this.setFocusable(true);
		this.setFocusableInTouchMode(true);
		// Instantiation of world
		aabb = new AABB();
		gravity = new Vec2(0, 10);
		aabb.lowerBound.set(-100, -100);
		aabb.upperBound.set(100, 100);
		world = new World(aabb, gravity, true);
		// Instantiation of body bitmaps
		
		// Instantiation of other bitmaps
		
		// Start accelerator sensor 
		sm = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
		sensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		//Set sensor listener
		mySensorListener = new SensorEventListener() {
			@Override
			public void onSensorChanged(SensorEvent event) {
				if(gameState == GAMESTATE_GAMEING){
					//x>0, phone turns left
					acc_x = event.values[0]; 
					//y>0, phone turns down
					acc_y = event.values[1];
					//z>0, phone faces upward
					acc_z = event.values[2]; 
					temp_y = mcharacter.getLinearVelocity().y;
					Vec2 vSensorVec2 = new Vec2(-acc_x,temp_y);
					mcharacter.setLinearVelocity(vSensorVec2);
				}
			}
			
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
			}
		};
		
		sm.registerListener(mySensorListener, sensor, SensorManager.SENSOR_DELAY_GAME);
		
		
		
	}
	

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		screenH = this.getHeight();
		screenW = this.getWidth();
		
		mcharacter = createCharacter(0, 0, 50, false);
		//testball.getShapeList().getFilterData().groupIndex = 1;
		
		//testball.getShapeList().getFilterData().maskBits = 2;
		
		//Create screen boundary
		boundary_bottom = createBoundary(0, screenH, screenW, 1, true);
		boundary_left = createBoundary(0, 0, 1,screenH, true);
		boundary_right = createBoundary(screenW, 0, 1, screenH, true);
		Body testboundary2 = createBoundary(0, 200, 30, 1, true);
		
		//Create collision area(basket)
		Body basket_left = createBoundary(300, 300, 1, 50, true);
		Body basket_right = createBoundary(530, 300, 1, 50, true);
		Body basket_bottom = createBoundary(300, 350, 240, 1, true);
		
		
		// Contact listener test
		contact_test = createBoundary(270, 500, 30, 30, true);
		collision_status = 0;
		//contact_test.getShapeList().getFilterData().groupIndex = 2;
		//contact_test.getShapeList().getFilterData().categoryBits = 4;
		contact_test.getShapeList().m_isSensor = true;
		
		screenW = this.getWidth();
		screenH = this.getHeight();
		paint = new Paint();
		paint.setStyle(Style.STROKE);
		paint.setAntiAlias(true);
		world.setContactListener(this);
		
		// Initialise game data
		// life, remainjumb,level, meteoro_num, aim_num, remain_time;
		// TODO SQLite implementation
		life = 3;
		level = 1;
		meteoro_num = 1 + level;
		aim_num = level*10;
		remain_time = 120;
		gameIsLost=false;
		gameIsWin = false;
		gameIsPause = false;
		
		
		flag = true;
		th = new Thread(this);
		th.start();
		Log.d(TAG, "surfaceCreated");
		//TODO change status
		gameState = GAMESTATE_GAMEING;
		timer = new Timer();
		timer.schedule(timertask, 1, 1000);
	}
	
	public Body createCharacter(float x, float y, float r, boolean isStatic) {
		// Create circular skin
		CircleDef cd = new CircleDef(); 
		if (isStatic) {
			cd.density = 0; // Static, no mass
		} else {
			cd.density = 1; // Non-static, have mass
		}
		cd.friction = 0.8f; 
		cd.restitution = 0.8f; 
		cd.radius = r / RATE; 
		// Create rigid body
		BodyDef bd = new BodyDef();
		bd.position.set((x + r) / RATE, (y + r) / RATE); 
		//bd.position.set(x/RATE, y/RATE); 
		// Create body
		Body body = world.createBody(bd);
		body.m_userData = new Character(x, y, r);
		body.createShape(cd);
		body.setMassFromShapes(); 
		body.allowSleeping(false);
		return body;
	}
	
	public Body createMeteoro(float x, float y, float r, boolean isStatic) {
		// Create circular skin
		CircleDef cd = new CircleDef(); 
		if (isStatic) {
			cd.density = 0; // Static, no mass
		} else {
			cd.density = 1; // Non-static, have mass
		}
		cd.friction = 0.8f; 
		cd.restitution = 0.3f; 
		cd.radius = r / RATE; 
		// Create rigid body
		BodyDef bd = new BodyDef();
		bd.position.set((x + r) / RATE, (y + r) / RATE); 
		//bd.position.set(x/RATE, y/RATE); 
		// Create body
		Body body = world.createBody(bd);
		body.m_userData = new TestBall(x, y, r);
		body.createShape(cd);
		body.setMassFromShapes(); 
		body.allowSleeping(false);
		return body;
	}
	
	public Body createBoundary(float x, float y, float width, float height,
			boolean isStatic) {
		// Create polygon skin
		PolygonDef pd = new PolygonDef(); 
		if (isStatic) {
			//Static, no mass
			pd.density = 0;
		} else {
			//Non-static, have mass
			pd.density = 1; 
		}
		pd.friction = 0.8f;
		pd.restitution = 0.3f; 
		pd.setAsBox(width / 2 / RATE, height / 2 / RATE);
		// Create rigid body 
		BodyDef bd = new BodyDef(); 
		bd.position.set((x + width / 2) / RATE, (y + height / 2) / RATE);
		// Create body
		Body body = world.createBody(bd); 
		body.m_userData = new Boundary(x, y, width, height);
		body.createShape(pd);
		body.setMassFromShapes(); 
		body.allowSleeping(false);
		return body;
	}
	
	public void myDraw() {
		try {
			
			canvas = sfh.lockCanvas();
			if(canvas!=null){
				canvas.drawColor(Color.WHITE);
				// --- Test info ---
				canvas.drawText("x:"+acc_x, 500, 20, paint);
				canvas.drawText("y:"+acc_y, 500, 40, paint);
				canvas.drawText("z:"+acc_z, 500, 60, paint);
				/**
				if(collision_status == 1){
					canvas.drawText("cstatus:add",450,80,paint);
				}else if (collision_status == 2) {
					canvas.drawText("cstatus:persist",450,80,paint);
				}else if (collision_status==3) {
					canvas.drawText("cstatus:remove",450,80,paint);
				}else{
					canvas.drawText("cstatus:n/a",450,80,paint);
				};
				**/
				canvas.drawText("Life "+life, 480,100,paint);
				canvas.drawText("Time "+ remain_time, 480, 120, paint);
				canvas.drawText("Meteoro " +meteoro_num, 480, 140, paint);
				canvas.drawText("Aim " +aim_num, 480, 160, paint);
				
				
			}
			switch (gameState) {
			case GAMESTATE_MENU:
				break;
			case GAMESTATE_HELP:
				break;
			case GAMESTATE_GAMEING:
				
				// --- Pause, win or lost status ---d
				if (gameIsPause || gameIsLost || gameIsWin) {
					// Draw translucent rectangle background
					Paint paintB = new Paint();
					paintB.setAlpha(0x77);
					canvas.drawRect(0, 0, screenW, screenH, paintB);
				}
				// --- Pause ---
				if (gameIsPause) {
					
					break;
				} else
				// --- Lost ---
				if (gameIsLost) {
					
					break;
				} else
				// --- Win ---
				if (gameIsWin) {
					canvas.drawText("Game Win", 260, 400, paint);
					break;
				}

				//Draw background

				//Iterate bodies in world
				Body body = world.getBodyList();
				Vec2 mposition;
				for (int i = 1; i < world.getBodyCount(); i++) {
					if(body.m_userData instanceof TestBall){
						mposition = body.getPosition();	
						//Log.d(TAG,"mposition:"+mposition.x+"  "+mposition.y);
						TestBall tBall = (TestBall) body.m_userData;
						tBall.setX(mposition.x*RATE);
						tBall.setY(mposition.y*RATE);
						if(!tBall.getGround()){
							tBall.drawSelf(canvas, paint);
						}else{
							Paint redpaint = new Paint();
							redpaint.setColor(Color.RED);
							redpaint.setStyle(Style.STROKE);
							redpaint.setAntiAlias(true);
							tBall.drawSelf(canvas, redpaint);
						}
						
					}else
					if(body.m_userData instanceof Boundary){
						mposition = body.getPosition();
						Boundary bd = (Boundary) body.m_userData;
						bd.setX(mposition.x*RATE - bd.get_width()/2);
						bd.setY(mposition.y*RATE - bd.get_height()/2);
						bd.drawSelf(canvas, paint);
					}else 
					if(body.m_userData instanceof Character){
						mposition = body.getPosition();
						Character ch = (Character) body.m_userData;
						ch.setX(mposition.x*RATE);
						ch.setY(mposition.y*RATE);
						ch.drawSelf(canvas, paint);
					}
				
					body = body.m_next;
				};
				
			

			}
		} catch (Exception e) {
			Log.e("tabi", "myDraw Error:" + e.toString());
		} finally {
			if (canvas != null)
				sfh.unlockCanvasAndPost(canvas);
		}
	}
	
	
	public int count = 0;
	public int destroy_count = 0;
	public void Logic() {
		count += 1;
		destroy_count +=1;
		switch (gameState) {
		case GAMESTATE_MENU:
			break;
		case GAMESTATE_HELP:
			break;
		case GAMESTATE_GAMEING:
		    if(count == 100){
		    	Body body = createMeteoro(0, 0, 10, false);
		    	//body.getShapeList().getFilterData().groupIndex = 3;
		    	//body.getShapeList().getFilterData().categoryBits = 2;
		    	count = 0;   	
		    }
		    
		    if (destroy_count == 2000) {
		    	//Iterate bodies in world
				Body body = world.getBodyList();
				for (int i = 1; i < 11; i++) {
					world.destroyBody(body);
					body = body.m_next;
					destroy_count = 0;
				}
				
			}
			
			if (!gameIsPause && !gameIsLost && !gameIsWin) {
			
				world.step(timeStep, iterations);
				}
			if(remain_time<0){
				gameIsWin = true;
				timer.cancel();
			}
		}
	}
	
	// --- Game timer ---
	Handler handler = new Handler() { 
		public void handleMessage(Message msg) { 
			switch (msg.what) {      
			      case 1:      
			    	  remain_time -= 1;
			    	  break;
			}
			super.handleMessage(msg); 
		} 
	
	};

	//TODO remain jump count
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (gameState) {
		case GAMESTATE_MENU:
			
			break;
		case GAMESTATE_HELP:
		
			break;
		case GAMESTATE_GAMEING:
			if (gameIsPause || gameIsLost || gameIsWin) {
			
			}
			
			// Get touch point
			int pointX = (int) event.getX();
			int pointY = (int) event.getY();
			//Log.d(TAG, pointX+", "+pointY+": "+event.toString());
			
			//if screen is pressed
			if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
				Vec2 vVelocity = new Vec2(0, -10);
				mcharacter.setLinearVelocity(vVelocity);
				Log.d(TAG,"Try change velocity");
				//remainjumb --;
				
				//press released
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				
				}
			break;
			}
			
		return true;
	}

	
	
	@Override
	public void run() {
		while (flag) {
			myDraw();
			Logic();
			try {
				Thread.sleep((long) timeStep * 1000);
			} catch (Exception e) {
				Log.e("tabi", "Thread Error:" + e.toString());
			}
		}
		
	}
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		flag = false;
	}
	
	@Override
	public void add(ContactPoint arg0) {
		
		
	}
	@Override
	public void persist(ContactPoint arg0) {
		// TODO Auto-generated method stub
		
		//Log.d(TAG, arg0.shape1.getBody().toString()+" "+ arg0.shape2.getBody().toString());
		if((arg0.shape1.getBody() == boundary_bottom) && (arg0.shape2.getBody().m_userData instanceof TestBall)){
			//collision_status = 1;
			TestBall tb = (TestBall) arg0.shape2.getBody().m_userData;
			tb.setGround(true);
		}else if ((arg0.shape2.getBody().m_userData instanceof TestBall)&&(arg0.shape1.getBody() == boundary_bottom) ) {
			TestBall tb = (TestBall) arg0.shape1.getBody().m_userData;
			tb.setGround(true);
		}
	}
	@Override
	public void remove(ContactPoint arg0) {
		if((arg0.shape1.getBody() == boundary_bottom) && (arg0.shape2.getBody().m_userData instanceof TestBall)){
			//collision_status = 1;
			TestBall tb = (TestBall) arg0.shape2.getBody().m_userData;
			tb.setGround(false);
		}else if ((arg0.shape2.getBody().m_userData instanceof TestBall)&&(arg0.shape1.getBody() == boundary_bottom) ) {
			TestBall tb = (TestBall) arg0.shape1.getBody().m_userData;
			tb.setGround(false);
		}

	}
	@Override
	public void result(ContactResult arg0) {
		// TODO Auto-generated method stub
		
	}
}
