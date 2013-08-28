package com.natsuyuu.tabi;

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

import android.content.Context;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
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
	private final float RATE = 30;
	private World world;
	private AABB aabb;
	private Vec2 gravity;
	private float timeStep = 1f / 60f;/
	private int iterations = 10;// For result accuracy, large value may cause latency
	// 声明小球的Body，便于后续对小球进行操作
	private Body bodyBall;
	//声明胜利与失败的body，用于判定游戏的胜负
	private Body lostBody1, lostBody2, winBody;
	// 声明屏幕宽高
	private int screenW, screenH;
	// 声明游戏状态
	private final int GAMESTATE_MENU = 0;
	private final int GAMESTATE_HELP = 1;
	private final int GAMESTATE_GAMEING = 2;
	private int gameState = GAMESTATE_MENU;
	// 为了游戏暂停时，失败，胜利能继续可能到游戏中的状态，所以并没有将其写成一个状态
	private boolean gameIsPause, gameIsLost, gameIsWin;
	// Body图片资源
	private Bitmap bmpH, bmpS, bmpSh, bmpSs, bmpBall;
	// 菜单、按钮、游戏背景图片资源
	private Bitmap bmpMenu_help, bmpMenu_play, bmpMenu_exit, bmpMenu_resume, bmpMenu_replay, bmp_menubg, bmp_gamebg, bmpMenuBack, bmp_smallbg, bmpMenu_menu,
			bmp_helpbg, bmpBody_lost, bmpBody_win, bmpWinbg, bmpLostbg;
	// 创建按钮
	private HButton hbHelp, hbPlay, hbExit, hbResume, hbReplay, hbBack, hbMenu;
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
		// --添加一个物理世界--->>
		aabb = new AABB();
		gravity = new Vec2(0, 10);
		aabb.lowerBound.set(-100, -100);
		aabb.upperBound.set(100, 100);
		world = new World(aabb, gravity, true);
		// ---实例化Body图片资源
		
		// 实例菜单、按钮、游戏背景图片资源
		
	}
}
