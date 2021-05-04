package Game; //Adding res to build path: Project, properties, add class folder, select folder.

import java.awt.Graphics;

import java.awt.image.BufferStrategy;
import Assets.Assets;
import Assets.Transition;
import States.BattleState;
import States.GameState;
import States.MenuState;
import States.State;

public class Game implements Runnable{
	
	private Display display;
	
	private int width;
	private int height;
	public String title;
	public boolean running = false;
	
	public static boolean showHitboxes = false;
	public static boolean flag = false;
	public static boolean flag2 = false;
	public static boolean battling = false;
	
	private Thread thread; //own separate game loop
	
	private BufferStrategy bs;
	private Graphics g;
	
	//States
	public State gameState;
	public State menuState;
	public State battleState;
	
	//Input
	private KeyManager keyManager;
	private MouseManager mouseManager;
	
	private GameCamera gameCamera;
	
	private Handler handler;
	private Transition transition;
	
	private int fps;
	private double timePerTick; 
	
	double delta;
	long now;
	long lastTime;
	long timer;
	int ticks;
	
	public Game(String title, int width, int height) {
		this.width = width;
		this.height = height;
		this.title = title;
		keyManager = new KeyManager();
		mouseManager = new MouseManager();
	}
	
	private void init() {
		display = new Display("RPG Game", width, height);
		display.getFrame().addKeyListener(keyManager);
		display.getFrame().addMouseListener(mouseManager);
		display.getFrame().addMouseMotionListener(mouseManager);
		display.getCanvas().addMouseListener(mouseManager);
		display.getCanvas().addMouseMotionListener(mouseManager);
		Assets.init();
		
		handler = new Handler(this);
		gameCamera = new GameCamera(handler, 0, 0);
		
		gameState = new GameState(handler);
		menuState = new MenuState(handler);
		//battleState = new BattleState(handler);
		State.setState(gameState); //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	}
	
	private void tick() { //updates all variables
		keyManager.tick();
		
		if(State.getState() != null) {
			State.getState().tick();
		}
		
		if(flag) {
			flag = false;
			transition = new Transition();
			flag2 = true;
		}
			if(Transition.canStart) {
				Transition.canStart = false;
				battling = true;
				battleState = new BattleState(handler);
				State.setState(handler.getGame().battleState);
			}
	}
	

	
	private void render() { //renders all objects
		bs = display.getCanvas().getBufferStrategy();
		if(bs == null) {
			display.getCanvas().createBufferStrategy(3);
			return;
		}
		g = bs.getDrawGraphics();
		
		//Clears certain portion of screen (in this case the whole screen)
		g.clearRect(0, 0, width, height);
		
		//Draws stuff in the screen-
		
		if(State.getState() != null) {
			State.getState().render(g);
		}
		
		if(flag2) {
			transition.render(g);
		}
		
		//End drawings-
		bs.show();
		g.dispose();
	}


	//starting the thread runs this method
	public void run() {
	
		init();
		
		fps = 60; //amount of times we want to call tick() and render() per one second
		
		 //1 billion because 1 billion nanoseconds in one second, computer measures in nanoseconds
		
		timePerTick = 1000000000 / fps; //maximum amount of time that we have to 
		//execute the tick() and render() methods to reach 60fps
		
		delta = 0; //the amount of time we have until we have to call tick() and render() again
		lastTime = System.nanoTime();
		timer = 0; //time until it gets to one second
		ticks = 0; //amount of ticks it's called, FPS
		
		
		while(running) {

			now = System.nanoTime();
			delta += (now - lastTime) / timePerTick;
			//System.out.println(delta);
			timer += now - lastTime;
			lastTime = now;

			//first updates variables, then renders
			if(delta >= 1) {
		
				tick(); 
				render();
				ticks++;
				delta--;
			}

			if(timer >= 1000000000) { //if timer exceeds one second
				System.out.println("FPS: " + ticks);
				ticks = 0;
				timer = 0;
			}
		}
		
		stop();
	}
	
	public KeyManager getKeyManager() {
		return keyManager;
	}
	
	public MouseManager getMouseManager() {
		return mouseManager;
	}
	
	public GameCamera getGameCamera() {
		return gameCamera;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public synchronized void start() {
		
		if(running == true) {
			return; //checks if already running
		}
		running = true;
		
		//runs this class on a new thread
		thread = new Thread(this);
		
		//calls the run method
		thread.start(); 
	}
	
	public synchronized void stop() {
		if(running == false) {
			return;
		}
		running = false;
		
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}



}
