package alife;

/* The following two imports are for creating executable jar */
import java.io.File;
import org.lwjgl.LWJGLUtil;

import java.util.Random;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.MouseListener;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;

/**
 * Main App class - the entry point for the starting of the simulation.
 * 
 * 
 */

public class Simulation extends BasicGame implements MouseListener
{

	static AppGameContainer app;

	/* Window or Screen Size */
	static int screen_width = 700;
	static int screen_height = 700;

	/* Graphic frame rate control */
	static int frame_rate = 60;
	
	/* This locks the frame rate to the above rate giving what time would have been used to threads */ 
	static boolean frame_cap = true; 
	
	/* Frame rate should be greater than or equal to refresh rate if used */
	static boolean vsync_toggle = false;
	
	/* Counters */
	static int frame_num = 0;
	static int step_num = 0;
	static boolean real_time;

	/* Number of Agents */
	int num_agents = 100;
	
	/* Draw slow but accurate circular bodies or faster rectangular ones */
	Boolean true_body_drawing=false;

	/** Toggle for Drawing agent field of views */
	Boolean draw_field_of_views=true;
	
	/** Simulation Agent Manager */
	AgentManager agentManager;
	
	/** The Simulation World. */
	static World world;

	/* Size of the world - Pixels - recommended to be power of 2 due to OpenGL texture limits */
	static int world_size = 1024;
	
	/* The translation vector for the camera view */
	public static Vector2f global_translate = new Vector2f(0,0);
	
	/* Size of the Agents - Pixels */
	int agent_size = 4;

	/* For this many simulation updates for buffer update */
	int draw_div = 1;
	int steps_todo = 0;
	
	/* Off screen buffer */
	Graphics bufferGraphics;
	Image buffer;
	int buffer_num=0;

	/* Stores the mouse vector across updates */
	public static Vector2f mouse_pos = new Vector2f(world_size, world_size);

	/* Stores the camera position */
	static int camera_margin = 10;
	
	public static Rectangle camera_bound = new Rectangle(0 + camera_margin, 0 + camera_margin, screen_width - (camera_margin * 2), screen_height - (camera_margin * 2));
			
	public Simulation()
	{
		super("Simulator");
	}

	@Override
	public void init(GameContainer container) throws SlickException
	{
		world = new World(world_size);

		/* Random Starting Position */
		Random xr = new Random();	
		Random yr = new Random();

		/* Random Size */
		Random sr = new Random();

		agentManager = new AgentManager(num_agents);

		agentManager.setTrueDrawing(true_body_drawing);
		
		agentManager.setFieldOfViewDrawing(draw_field_of_views);
		
		int i;

		int x, y, t, s;
		for (i = 0; i < num_agents; i++)
		{

			x = xr.nextInt(world_size) + 1;

			y = yr.nextInt(world_size) + 1;

			s = sr.nextInt(agent_size) + 4;

			agentManager.addNewAgent(new SimpleAgent(i, x, y, s));

		}
		
		setUpImageBuffer();

	}

	@Override
	public void update(GameContainer container, int delta) throws SlickException
	{
		/* Not Used */
		steps_todo=0;

		/* Frame Rate is 60 - Update at 1/4 of that for our target sim rate */
		if(frame_num%4 == 0)
		{
			while(steps_todo < draw_div)
			{
				agentManager.doAi();
				steps_todo++;
				step_num++;
			}
		}
	}

	@Override
	public void render(GameContainer container, Graphics g) throws SlickException
	{
		/* Some Linux Drivers have hardware clipping bugs */
		g.setWorldClip(camera_bound); // Todo Make setting
		
		/* Frame Rate is 60 - Sim rate is 1/4 of that for our target sim rate so only redraw the buffer when the sim has updated */
		if(frame_num%4 == 0)
		{
			doDraw(bufferGraphics);
		}
			
		/* Always draw the buffer even if it has not changed */
		g.drawImage(buffer, 0,0);		

		frame_num++;
		
		/* Gui Overlay */
		g.setColor(Color.white);
		
		g.drawString("Alife Sim Test - Number Active Agents : " + num_agents, camera_bound.getMinX(), camera_bound.getMinY());
		
		g.drawString("Step Num : " + step_num, camera_bound.getMinX(), camera_bound.getMinY() + 50);

		/* If the frame rate greater than or equal to the target we are updating in real-time or a multiple of real-time */
		if(app.getFPS() >= frame_rate )
		{
			real_time=true;
		}
		else
		{
			real_time=false;
		}
		
		g.drawString("Frame Updates + ( Real-time : " + Boolean.toString(real_time) +") : " + frame_num, camera_bound.getMinX(), camera_bound.getMinY() + 100);	
				
		g.drawString("Buffer Updates : " + buffer_num, camera_bound.getMinX(), camera_bound.getMinY() + 150);
		
		g.drawString("Frame Rate : " + app.getFPS(), camera_bound.getMinX(), camera_bound.getMinY() + 200);

		g.drawString("Draw Div : " + draw_div, camera_bound.getMinX(), camera_bound.getMinY() + 250);
		
		g.draw(camera_bound);	

	}

	private void doDraw(Graphics g)
	{
		/* Blank the Image buffer */
		g.clear();

		g.translate(global_translate.getX(), global_translate.getY());

		/* TODO Centers the world in view correctly - Clipping problem with agents - Math is here or somewhere else */
		//g.translate(global_translate.getX()+(screen_width/2-(world_size/2)), global_translate.getY()+(screen_height/2-(world_size/2)));

		/* World */
		world.drawWorld(g);

		/* Agents */
		drawAgents(g);		
		
		buffer_num++;

	}
	
	/* Agent draw method */
	private void drawAgents(Graphics g)
	{
		agentManager.drawAI(g);
	}

	/* Keeps the simulation aware of the mouse position minus any translation of the view */
	@Override
	public void mouseMoved(int oldx, int oldy, int newx, int newy)
	{
		mouse_pos.set(newx - global_translate.getX(), newy - global_translate.getY());
	}

	/* Todo - Not used */
	@Override
	public void mousePressed(int button, int x, int y)
	{
		/*if (button == 0)
		{
			Simulation.app.setTargetFrameRate(-1);
		}
		else
		{
			Simulation.app.setTargetFrameRate(15);
		}*/
	}

	/* Allows moving camera around large worlds */
	@Override
	public void mouseDragged(int oldx, int oldy, int newx, int newy)
	{
		float x = (newx) - mouse_pos.getX();
		float y = (newy) - mouse_pos.getY();
		
		moveCamera(x, y);

	}

	@Override
	public void mouseWheelMoved(int change)
	{
		if(change>0)
		{
			draw_div++;
		}
		else
		{
			if(draw_div>0)
			{
				draw_div--;
			}
		}
		
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

			app = new AppGameContainer(new Simulation());

			/* Always update */
			app.setUpdateOnlyWhenVisible(false);

			/* Screen size / Window Size */
			app.setDisplayMode(screen_width, screen_height, false);
			
			/* Not needed */
			app.setShowFPS(false);

			/* Needed as we now do asynchronous drawing */
			app.setClearEachFrame(true);

			/* Hardware vsync */
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
	
	private void setUpImageBuffer()
	{
		try
		{
			buffer = new Image(screen_width+1,screen_height+1);
		}
		catch (SlickException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try
		{
			bufferGraphics = buffer.getGraphics();
		}
		catch (SlickException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}