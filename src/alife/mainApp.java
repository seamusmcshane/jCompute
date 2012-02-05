package alife;

/* The following two imports are for creating executable jar */
import java.io.File;
import org.lwjgl.LWJGLUtil;

import java.util.Random;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.MouseListener;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;

/*
 * Main App class - the entry point for the starting of the simulation.
 */

public class mainApp extends BasicGame implements MouseListener
{

	static AppGameContainer app;

	/* The translation vector for the camera view */
	public static Vector2f global_translate = new Vector2f(0, 0);

	/* Window or Screen Size */
	static int screen_width = 1024;
	static int screen_height = 768;

	/* Graphic frame rate control */
	static int frame_rate = 60;
	
	/* This locks the frame rate to the above rate giving what time would have been used to threads */ 
	static boolean frame_cap = true; 
	
	/* Frame rate should be greater than or equal to refresh rate if used or set sync_sim_with_frame_rate to true;*/
	static boolean vsync_toggle = false;
	boolean sync_sim_with_frame_rate=false;

	/* Counters */
	static int frame_num = 0;
	static int step_num = 0;

	int num_agents = 1000;

	/* Simulation objects */
	AgentManager agentManager;
	static World world;

	/* Size of the world - Pixels - recommended to be base 10 divisible for grid visuals */
	static int world_size = 1000;

	/* Size of the Agents - Pixes */
	int agent_size = 4;

	/* Draw toggle */
	int draw = 1;

	/* For this many simulation updates draw one frame update */
	int draw_div = 1;

	/* Stores the mouse vector across updates */
	public static Vector2f mouse_pos = new Vector2f(world_size, world_size);

	/* Stores the camera postion */
	static int camera_margin = 0;
	public static Rectangle camera_bound = new Rectangle(0 + camera_margin, 0 + camera_margin, screen_width - (camera_margin * 2), screen_height - (camera_margin * 2));

	/* The Simulation update thread and sync update toggle */
	Thread asyncUpdateThread;
	
	public mainApp()
	{
		super("MainApp");
	}

	@Override
	public void init(GameContainer container) throws SlickException
	{
		world = new World(world_size);

		/* Random Starting Position */
		Random xr = new Random();	
		Random yr = new Random();

		/* Random Type */
		Random tr = new Random();

		/* Random Size */
		Random sr = new Random();

		agentManager = new AgentManager(num_agents);

		int i;

		int x, y, t, s;
		for (i = 0; i < num_agents; i++)
		{

			x = xr.nextInt(world_size) + 1;

			y = yr.nextInt(world_size) + 1;

			t = tr.nextInt(2) + 1;

			s = sr.nextInt(agent_size) + 4;

			agentManager.addNewAgent(new SimpleAgent(i, x, y, s, t));

		}

		/* Is this Simulation locked to the frame rate */
		if(sync_sim_with_frame_rate==false)
		{				
			asyncUpdateThread = new Thread(new Runnable()
			{
				public void run()
				{
					Thread thisThread = Thread.currentThread();
	
					/* Top Priority to the simulation thread */
					thisThread.setPriority(Thread.MAX_PRIORITY);
	
					while (asyncUpdateThread == thisThread)
					{
						agentManager.doAi();
	
						if (step_num % draw_div == 0)
						{
							setDraw(1);
						}
						else
						{
							setDraw(0);
						}
	
						step_num++;
	
						try
						{
							asyncUpdateThread.sleep(1); /* The thread must give time to the Garbage collector or there will be major stalls when it eventually needs to flush old objects */
						}
						catch (InterruptedException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
	
			);
	
			asyncUpdateThread.start();
		
		}
	}

	@Override
	public void update(GameContainer container, int delta) throws SlickException
	{

		/* Not used - Simulation is now decoupled from frame rate */

	}

	@Override
	public void render(GameContainer container, Graphics g) throws SlickException
	{
		/* Frame Synchronized Updates */
		if(sync_sim_with_frame_rate==true)
		{
			agentManager.doAi();
			step_num++;
		}

		doDraw(g);
	}

	/* Agent draw method */
	private void drawAgents(Graphics g)
	{
		agentManager.drawAI(g);
	}

	/* Keeps the simulation aware of the mouse position minus any translation */
	@Override
	public void mouseMoved(int oldx, int oldy, int newx, int newy)
	{
		mouse_pos.set(newx - global_translate.getX(), newy - global_translate.getY());
	}

	/* Todo - Not used */
	@Override
	public void mousePressed(int button, int x, int y)
	{
		if (button == 1)
		{

		}
	}

	/* Allows moving camera around large worlds */
	@Override
	public void mouseDragged(int oldx, int oldy, int newx, int newy)
	{

		float x = (newx) - mouse_pos.getX();
		float y = (newy) - mouse_pos.getY();

		moveCamera(x, y);

	}

	/* Camera is moved by translating all the drawing */
	private void moveCamera(float x, float y)
	{
		global_translate.set(x, y);
	}

	/* Main Entry Point */
	public static void main(String[] args)
	{
		try
		{
			/*
			 * - For stand alone builds un-comment these so the jar will look
			 * for the native libraries correctly
			 */
			// System.setProperty("org.lwjgl.librarypath", new File(new
			// File(System.getProperty("user.dir"), "native"),
			// LWJGLUtil.PLATFORM_WINDOWS_NAME).getAbsolutePath());
			// System.setProperty("net.java.games.input.librarypath",
			// System.getProperty("org.lwjgl.librarypath"));

			app = new AppGameContainer(new mainApp());

			/* TODO FIX */
			app.setTitle("Alife Sim");

			/* Always update */
			app.setUpdateOnlyWhenVisible(false);

			/* Screen size / Window Size */
			app.setDisplayMode(screen_width, screen_height, false);
			
			/* Not needed */
			app.setShowFPS(false);

			/* Needed as we now do asynchronous drawing */
			app.setClearEachFrame(false);

			/* Hardware vsync - on due as we dont want the graphics drives do crazy amounts of frame updates  */
			app.setVSync(vsync_toggle);
			
			/* Hardware Anti-Aliasing */
			//app.setMultiSample(8);
			
			/* For 1-1 drawing we can enforce a frame cap and watch the simulation in real-time - performance permitting */
			if (frame_cap)
			{
				app.setTargetFrameRate(frame_rate);
			}

			app.start();

		}
		catch (SlickException e)
		{
			e.printStackTrace();
		}
	}
	
	private synchronized void doDraw(Graphics g)
	{
		// Can Flicker if updates take long time
		if (getDraw() == 1)
		{
			g.clear();

			/* Hardware pixel clipping */
			g.setWorldClip(camera_bound);

			/* Center Origin on Screen */
			g.translate(global_translate.getX() + (world_size / 2), global_translate.getY() + (world_size / 2));

			// g.setAntiAlias(false);

			/* World */
			world.drawWorld(g);

			/* Agents */
			drawAgents(g);

			/* Return to 0,0 (Top Left) */
			g.resetTransform();

			/* Gui Overlay */
			g.setColor(Color.white);
			g.drawString("Alife Sim Test - Number Active Agents : " + num_agents + "\n Frame Number : " + frame_num + " Step Num : " + step_num, camera_bound.getMinX(), camera_bound.getMinY());

			g.drawString("FPS : " + app.getFPS(), camera_bound.getMinX(), camera_bound.getMinY() + 50);

			g.draw(camera_bound);

			frame_num++;

		}
		/*
		 * System.out.println("\n global_translate X " + global_translate.getX()
		 * + " global_translate Y " + global_translate.getY());
		 * System.out.println("\n mouse_pos X " + mouse_pos.getX() +
		 * " mouse_pos Y " + mouse_pos.getY());
		 */		
	}
	
	private synchronized void setDraw(int draw)
	{
		this.draw=draw;
	}

	private synchronized int getDraw()
	{
		return draw;		
	}
}