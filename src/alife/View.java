package alife;

/* NOTE! The following two imports are for creating executable jar */
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Random;
import java.util.concurrent.Semaphore;

import javax.swing.JFrame;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.CanvasGameContainer;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.MouseListener;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;
/**
 * Simulation class - Gui and Entry Point for starting a Simulation.
 */
public class View extends BasicGame implements MouseListener
{

	/* Window */
	static JFrame frame;
	
	/* Window Position */
	static int x;
	static int y;
	
	/** OpenGl Canvas */
	static CanvasGameContainer simView;
	
	/** OpenGL Canvas Size */
	static int world_view_width;
	static int world_view_height;

	/* Default Graphic frame rate control */
	final static int default_frame_rate = 15;
	static int frame_rate = default_frame_rate; // Frame rate start up at this
	final int frame_rate_gui_interaction = 60;

	/* Simulation Reference */
	static Simulation sim;
	
	/**
	 * This locks the frame rate to the above rate giving what time would have
	 * been used to threads
	 */
	boolean frame_cap = true;

	/** Frame rate should be greater than or equal to refresh rate if used */
	static boolean vsync_toggle = false;

	/** Simulation Performance Indicators */
	int frame_num = 0;
	int step_num = 0;
	double sps = 0; 					// steps per second
	boolean real_time;

	/* Draw slow but accurate circular bodies or faster rectangular ones */
	Boolean true_body_drawing = false;

	/** Toggle for Drawing agent field of views */
	Boolean draw_field_of_views = false;

	/* The translation vector for the camera view */
	public static Vector2f global_translate = new Vector2f(0, 0);

	int steps_todo = 0;

	/* Off screen buffer */
	Graphics bufferGraphics;
	Image buffer;
	int buffer_num = 0;

	/* Stores the mouse vector across updates */
	public Vector2f mouse_pos = new Vector2f(0, 0);

	/* Stores the camera position */
	int camera_margin = 1;

	public Rectangle camera_bound = new Rectangle(camera_margin, camera_margin, world_view_width - (camera_margin), world_view_height - (camera_margin));


	public View()
	{
		super("Simulation View");	
	}

	@Override
	public void init(GameContainer container) throws SlickException
	{		setUpImageBuffer();
		
	}
	
	@Override
	public void update(GameContainer container, int delta) throws SlickException
	{
		// Not Used
	}

	@Override
	public void render(GameContainer container, Graphics g) throws SlickException
	{
		/* Some Linux Drivers have hardware clipping bugs */
		
		g.setWorldClip(camera_bound); // Todo Make setting
		
		doDraw(bufferGraphics);

		/* Always draw the buffer even if it has not changed */
		g.drawImage(buffer, 0, 0);

		/* Gui Overlay */
		g.setColor(Color.white);

		g.drawString("Frame Updates  :" + frame_num, camera_bound.getMinX(), camera_bound.getMinY() + 10);

		g.drawString("Buffer Updates :" + buffer_num, camera_bound.getMinX(), camera_bound.getMinY() + 30);

		g.drawString("FPS            :" + simView.getContainer().getFPS(), camera_bound.getMinX(), camera_bound.getMinY() + 50);

		g.draw(camera_bound);
		
		frame_num++;

	}

	private void doDraw(Graphics g)
	{
		/* Blank the Image buffer */
		g.clear();

		/* Move the entire world to simulate a view moving around */
		g.translate(global_translate.getX(), global_translate.getY());

		sim.drawSim(g);

		/* Performance Indicator */
		buffer_num++;
	}

	/*
	 * Keeps the simulation aware of the mouse position minus any translation of
	 * the view
	 */
	@Override
	public void mouseMoved(int oldx, int oldy, int newx, int newy)
	{
		mouse_pos.set(newx - global_translate.getX(), newy - global_translate.getY());
	}

	/*
	 * Makes sure valid mouse coordinates are used when the mouse leaves and
	 * renters a window that has lost and regained focus. - Prevents view
	 * snapping to strange locations
	 */
	@Override
	public void mousePressed(int button, int x, int y)
	{
		mouse_pos.set(x - global_translate.getX(), y - global_translate.getY());
				
		mouseInteractionModeOn();
		
		//req_sps = 9999;
		
	}

	@Override
	public void mouseReleased(int button, int x, int y)
	{
		mouse_pos.set(x - global_translate.getX(), y - global_translate.getY());
				
		mouseInteractionModeOff();
		
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

	}

	/* Camera is moved by translating all the drawing */
	private void moveCamera(float x, float y)
	{
		global_translate.set(x, y);
	}
	
	public static void exitDisplay()
	{
		Display.destroy();
	}
	
	/* Setup Window */
	private static void setUpWindowDimesions(int xin, int yin, int width, int height)
	{
		/* Position */
		x = xin;
		y = yin;
		
		/* Size */
		world_view_width = width;
		world_view_height = height; 
	}
		
	/* Main Entry Point */
	/**
	 * @wbp.parser.entryPoint
	 */
	public static void displayView(Simulation simIn,int x, int y, int width, int height)
	{
		
		sim = simIn;
		
		setUpWindowDimesions(x,y,width,height);
		
		try
		{

			frame = new JFrame("Simulation");
			//frame.setType(Type.UTILITY);
			frame.setUndecorated(true);
			
			frame.setSize(world_view_width, world_view_height);
			
			frame.setLocation(x, y);
			
			/*
			 * - For stand alone builds un-comment these so the jar will look
			 * for the native libraries correctly
			 */
			// System.setProperty("org.lwjgl.librarypath", new File(new
			// File(System.getProperty("user.dir"), "native"),
			// LWJGLUtil.PLATFORM_WINDOWS_NAME).getAbsolutePath());
			// System.setProperty("net.java.games.input.librarypath",
			// System.getProperty("org.lwjgl.librarypath"));

			simView = new CanvasGameContainer(new View());

			//sim.setDisplayMode(world_view_width,world_view_height, false);
			
			/* Always update */
			simView.getContainer().setUpdateOnlyWhenVisible(false);

			/* Screen size / Window Size */
			// app.setDisplayMode(screen_width, screen_height, false);

			/* Not needed */
			simView.getContainer().setShowFPS(false);

			/* Needed as we now do asynchronous drawing */
			simView.getContainer().setClearEachFrame(true);

			/* Hardware vsync */
			simView.getContainer().setVSync(vsync_toggle);

			/* Always draw even if window not active */
			simView.getContainer().setAlwaysRender(true);

			/* Dont close the app if we close the sim */
			simView.getContainer().setForceExit(false);

			/* Hardware Anti-Aliasing */
			// app.setMultiSample(8);

			// Set sim start up frame rate 
			simView.getContainer().setTargetFrameRate(frame_rate);

			frame.getContentPane().add(simView, BorderLayout.CENTER);

			frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); 

			frame.setVisible(true);
			
			frame.setResizable(false);
			
			simView.start();
			
		}
		catch (SlickException e)
		{
			e.printStackTrace();
		}
	}

	/** Sets up the off-screen buffer image */
	private void setUpImageBuffer()
	{
		try
		{
			buffer = new Image(world_view_width + 1, world_view_height + 1);
			bufferGraphics = buffer.getGraphics();
		}
		catch (SlickException e)
		{
			e.printStackTrace();
		}

	}
	
	private void mouseInteractionModeOn()
	{
		frame_rate = frame_rate_gui_interaction;
		
		simView.getContainer().setTargetFrameRate(frame_rate);

	}

	private void mouseInteractionModeOff()
	{
		frame_rate = default_frame_rate;	
		
		simView.getContainer().setTargetFrameRate(frame_rate);

	}
	
	public static void setFocus()
	{
		simView.requestFocus();
		global_translate.set( 100,100 );
	}
	
}