package com.natsuyuu.tabi;

import javax.security.auth.PrivateCredentialPermission;

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
import android.R.string;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
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
	private float timeStep = 1f / 20f;
	// For result accuracy, large value may cause latency
	private int iterations = 5;
	// Main body
	private Body character;
	// Win/Lost collision point
	
	// Screen width & height
	private int screenW, screenH;
	// --- Game Status ---
	private final int GAMESTATE_MENU = 0;
	private final int GAMESTATE_HELP = 1;
	private final int GAMESTATE_GAMEING = 2;
	// TO-DO change status back to menu
	private int gameState = GAMESTATE_GAMEING;
	private boolean gameIsPause, gameIsLost, gameIsWin;
	// Body bitmap resources

	// Menu, button, background bitmap resources
	private Bitmap bmpMenu_help, bmpMenu_play, bmpMenu_exit, bmpMenu_resume, bmpMenu_replay, bmp_menubg, bmp_gamebg, bmpMenuBack, bmp_smallbg, bmpMenu_menu,
			bmp_helpbg, bmpBody_lost, bmpBody_win, bmpWinbg, bmpLostbg;
	// Create button
	
	//Log Tag
	private String TAG = "TABI_status";
	
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
		
		
	}
	

	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
		// TODO Auto-generated method stub
		Body testball = createCircle(0, 0, 50, false);
		screenH = this.getHeight();
		screenW = this.getWidth();
		//Create screen boundary
		Body boundary_bottom = createBoundary(0, screenH, screenW, 1, true);
		Body boundary_left = createBoundary(0, 0, 1,screenH, true);
		Body boundary_right = createBoundary(screenW, 0, 1, screenH, true);
		Body testboundary2 = createBoundary(0, 200, 30, 1, true);
		
		screenW = this.getWidth();
		screenH = this.getHeight();
		paint = new Paint();
		paint.setStyle(Style.STROKE);
		paint.setAntiAlias(true);
		
		flag = true;
		th = new Thread(this);
		th.start();
		Log.d(TAG, "surfaceCreated");
	}
	
	public Body createCircle(float x, float y, float r, boolean isStatic) {
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
		bd.allowSleep = true;
		Body body = world.createBody(bd);
		body.m_userData = new TestBall(x, y, r);
		body.createShape(cd);
		body.setMassFromShapes(); 
		body.allowSleeping(true);
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
		return body;
	}
	
	public void myDraw() {
		try {
			
			canvas = sfh.lockCanvas();
			if(canvas!=null){
				canvas.drawColor(Color.WHITE);
			}
			switch (gameState) {
			case GAMESTATE_MENU:
				break;
			case GAMESTATE_HELP:
				break;
			case GAMESTATE_GAMEING:
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
						tBall.drawSelf(canvas, paint);
					}
					if(body.m_userData instanceof Boundary){
						Boundary bd = (Boundary) body.m_userData;
						bd.drawSelf(canvas, paint);
					}
					body = body.m_next;
				};
				
			
				// --- Pause, win or lost status ---d
				if (gameIsPause || gameIsLost || gameIsWin) {
					// Draw translucent rectangle background
				
				}
				// --- Pause ---
				if (gameIsPause) {
					
				} else
				// --- Lost ---
				if (gameIsLost) {
					
				} else
				// --- Win ---
				if (gameIsWin) {
					
				}
				break;
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
		    	createCircle(0, 0, 10, false);
		    	count = 0;   	
		    }
		    if (destroy_count == 1000) {
		    	//Iterate bodies in world
				Body body = world.getBodyList();
				for (int i = 1; i < 10; i++) {
					world.destroyBody(body);
					body = body.m_next;
				}
				
			}
			if (!gameIsPause && !gameIsLost && !gameIsWin) {
			
				world.step(timeStep, iterations);
				}
		}
	}


	
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
		// TODO Auto-generated method stub
		
	}
	@Override
	public void persist(ContactPoint arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void remove(ContactPoint arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void result(ContactResult arg0) {
		// TODO Auto-generated method stub
		
	}
}
