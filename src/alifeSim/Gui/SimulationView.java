package alifeSim.Gui;

import java.awt.Component;
import java.awt.Dimension;

import org.lwjgl.LWJGLUtil; 

import java.io.File; 
import java.util.concurrent.Semaphore;

import org.lwjgl.opengl.Display;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.CanvasGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.MouseListener;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;

import alifeSim.Simulation.Simulation;


/**
 * Simulation View class - This class handles the drawing of the 2d representation of the simulation world..
 * @author Seamus McShane
 * @version $Revision: 1.0 $
 */
public class SimulationView extends BasicGame implements MouseListener
{
	/** OpenGl Canvas */
	private static CanvasGameContainer simView;

	/** OpenGL Canvas Size */
	private static int panelWidth=1024;
	private static int panelHeight=1024;
	
	private final static int lowFrameRate = 15;
	private final static int highFrameRate = 60;

	/** Default Graphic frame rate control */
	private final static int defaultFrameRate = highFrameRate; // Frame rate starts up set at this

	private final static int frameRateGuiInteractionOff = lowFrameRate;
	private final static int frameRateGuiInteractionOn = highFrameRate;

	/** Allows fixing the update rate at the mouseInteraction rate **/
	private static boolean highUpdateRate = true;

	/** Simulation Reference */
	private static Simulation sim;
	private static Semaphore viewLock = new Semaphore(1);

	/** Is Simulation view drawing enabled */
	private static boolean drawSim = false;

	/** Draw true circular bodies or faster rectangular ones */
	private static boolean simpleDrawing = true;

	/** Draw the View range of the agents */
	private static boolean viewRangeDrawing = false;

	/** Draw Views */
	private static boolean viewsDrawing = false;
	/**
	 * This locks the frame rate to the following rate allowing more time
	 * to be used for simulation threads.
	 */
	private boolean frameCap = true;

	/** Frame rate should be greater than or equal to refresh rate if used */
	private static boolean vsyncToggle = false;

	/** Simulation Performance Indicators */
	private int frameNum = 0;

	/** The translation vector for the camera view */
	public static Vector2f globalTranslate = new Vector2f(0, 0);

	/** Stores the mouse vector across updates */
	public Vector2f mousePos = new Vector2f(0, 0);

	/** Stores the camera margin */
	private static int cameraMargin = 0;

	/** Camera View Size */
	public static Rectangle cameraBound;

	/** Toggles the drawing of the text overlay */
	private static boolean overlay = false;

	/** Records status of mouse button */
	private static boolean mouseButtonPressed = false;

	/**
	 * The Simulation View.
	 * @wbp.parser.entryPoint
	 */
	public SimulationView(int pannelWidth,int pannelHeight)
	{
		super("Simulation View");
		this.panelWidth = pannelWidth;
		this.panelHeight = pannelHeight;
		
		globalTranslate.set(25,25);
		
		// Uncomment when building the executable jar for that platform 
		 //buildStandAlone(true);
	}

	/**
	 * Method buildStandAlone.
	 * @param setPath boolean
	 */
	public static void buildStandAlone(boolean setPath)
	{
		String hostPlatform = System.getProperty("os.name");
				
		if(setPath)
		{
			/* Work out the platform */
			if(hostPlatform.contains("Windows"))
			{
				System.setProperty("org.lwjgl.librarypath", new File(new File(System.getProperty("user.dir"), "native"), LWJGLUtil.PLATFORM_WINDOWS_NAME).getAbsolutePath());
			}
			else if (hostPlatform.contains("Linux"))
			{
				System.setProperty("org.lwjgl.librarypath", new File(new File(System.getProperty("user.dir"), "native"), LWJGLUtil.PLATFORM_LINUX_NAME).getAbsolutePath());
			}
			else
			{
				System.setProperty("org.lwjgl.librarypath", new File(new File(System.getProperty("user.dir"), "native"), LWJGLUtil.PLATFORM_MACOSX_NAME).getAbsolutePath());
			}		
			/* Set the correct path */
			System.setProperty("net.java.games.input.librarypath", System.getProperty("org.lwjgl.librarypath"));			
		}
		 
	}
	
	
	/**
	 * Method init.
	 * @param container GameContainer
	 * @throws SlickException * @see org.newdawn.slick.Game#init(GameContainer)
	 */
	@Override
	public void init(GameContainer container) throws SlickException
	{
		// Not Needed
	}

	/**
	 * Method update.
	 * @param container GameContainer
	 * @param delta int
	 * @throws SlickException * @see org.newdawn.slick.Game#update(GameContainer, int)
	 */
	@Override
	public void update(GameContainer container, int delta) throws SlickException
	{
		// Slick2d update function Not Used, see "Simulation Update Thread" in Simulation class for our update loop 
	}

	/**
	 * Method render.
	 * @param container GameContainer
	 * @param g Graphics
	 * @throws SlickException * @see org.newdawn.slick.Game#render(GameContainer, Graphics) 
	 * @see org.newdawn.slick.Game#render(GameContainer, Graphics)
	 */
	@Override
	public void render(GameContainer container, Graphics g) throws SlickException
	{
		
		if (drawSim)
		{
			/*
			 * AMD Opensource Linux Drivers have hardware clipping bugs (Update
			 * Drivers to Xorg 1.12 for fix)
			 */
			g.setWorldClip(cameraBound);

			/* Draws on the buffer */
			doDraw(g);

			/* Always draw the buffer even if it has not changed */
			//g.drawImage(buffer, 0, 0);

			// View Overlay
			if (overlay)
			{
				g.drawString("Frame Updates     :" + frameNum, cameraBound.getMinX() + 10, cameraBound.getMinY() + 10);

				g.drawString("Frames Per Second :" + simView.getContainer().getFPS(), cameraBound.getMinX() + 10, cameraBound.getMinY() + 50);

				//g.draw(cameraBound);
			}
			frameNum++;
		}
		
	}

	/** Draws the sim view on the image buffer
	 * @param g Graphics
	 */
	private void doDraw(Graphics g)
	{

		/* Blank the Image buffer */
		g.clear();

		/* Move the entire world to simulate a view moving around */
		g.translate(globalTranslate.getX(), globalTranslate.getY());

		// to allow switching the Simulation to draw, this will need to be locked
		if(sim!=null)
		{
			viewLock.acquireUninterruptibly();
			sim.drawSim(g, simpleDrawing, viewRangeDrawing,viewsDrawing);
			viewLock.release();
		}
		
	}

	/**
	 * Makes sure valid mouse coordinates are used when the mouse leaves and
	 * renters a window that has lost and regained focus. - Prevents view
	 * snapping to strange locations
	 * @param button int
	 * @param x int
	 * @param y int
	 * @see org.newdawn.slick.MouseListener#mousePressed(int, int, int) */
	@Override
	public void mousePressed(int button, int x, int y)
	{
		mousePos.set(x - globalTranslate.getX(), y - globalTranslate.getY());

		mouseInteractionModeOn(); // Changes to a higher frame update rate				
	}

	/** Allows moving camera around large worlds via mouse dragging on the simulation view
	 * @param oldx int
	 * @param oldy int
	 * @param newx int
	 * @param newy int
	 * @see org.newdawn.slick.MouseListener#mouseDragged(int, int, int, int) */
	@Override
	public void mouseDragged(int oldx, int oldy, int newx, int newy)
	{
		float x = (newx) - mousePos.getX();
		float y = (newy) - mousePos.getY();

		moveCamera(x, y);
	}

	/**
	 * Method mouseReleased.
	 * @param button int
	 * @param x int
	 * @param y int
	 * @see org.newdawn.slick.MouseListener#mouseReleased(int, int, int) */
	@Override
	public void mouseReleased(int button, int x, int y)
	{
		mousePos.set(x - globalTranslate.getX(), y - globalTranslate.getY());

		mouseInteractionModeOff(); // Changes to a lower frame update rate

	}

	/**
	 * Method mouseWheelMoved.
	 * @param change int
	 * @see org.newdawn.slick.MouseListener#mouseWheelMoved(int) */
	@Override
	public void mouseWheelMoved(int change)
	{

	}

	/** Camera is moved by translating all the drawing 
	 * @param x float
	 * @param y float
	 */
	private void moveCamera(float x, float y)
	{
		globalTranslate.set(x, y);
	}

	public static void exitDisplay()
	{
		Display.destroy();
	}

	/* Main Entry Point for View */
	/**
	 * @param simIn Simulation
	 * @param x int
	 * @param y int
	 * @param width int
	 * @param height int
	 */
	public static Component displayView(Simulation simIn,int width,int height)
	{
		setSim(simIn);

		try
		{

			simView = new CanvasGameContainer(new SimulationView(width,height));

			//sim.setDisplayMode(worldViewWidth,worldViewHeight, false);

			/* Always update */
			simView.getContainer().setUpdateOnlyWhenVisible(false);

			/* Not needed */
			simView.getContainer().setShowFPS(false);

			/* Needed as we now do asynchronous drawing */
			simView.getContainer().setClearEachFrame(true);

			/* Hardware vsync */
			simView.getContainer().setVSync(vsyncToggle);

			/* Always draw even if window not active */
			simView.getContainer().setAlwaysRender(true);

			/* Dont close the app if we close the sim */
			simView.getContainer().setForceExit(false);

			/* Hardware Anti-Aliasing */
			//simView.getContainer().setMultiSample(-1);
			
			// Set sim start up frame rate 
			simView.getContainer().setTargetFrameRate(defaultFrameRate);
			
			simView.setMinimumSize(new Dimension(320,240));

			cameraBound = new Rectangle(cameraMargin, cameraMargin, panelWidth - (cameraMargin * 2), panelHeight - (cameraMargin * 2));
			
			return simView;

		}
		catch (SlickException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public static void setSim(Simulation simIn)
	{
		viewLock.acquireUninterruptibly();
		sim = simIn;
		viewLock.release();
	}
	
	public static void startView()
	{
		try
		{
			simView.start();
		}
		catch (SlickException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void setUnlimitedUpdateRate()
	{
		highUpdateRate = true;

		simView.getContainer().setTargetFrameRate(-1);
	}

	/**
	 * Method setVerticalSync.
	 * @param sync boolean
	 */
	public static void setVerticalSync(boolean sync)
	{
		vsyncToggle = sync;
		simView.getContainer().setVSync(vsyncToggle);
	}

	public static void setHighUpdateRate()
	{
		highUpdateRate = true;

		simView.getContainer().setTargetFrameRate(highFrameRate);

	}

	/**
	 * Method setViewOverLay.
	 * @param ioverlay boolean
	 */
	public static void setViewOverLay(boolean ioverlay)
	{
		overlay = ioverlay;
	}

	public static void setStandardUpdateRate()
	{
		highUpdateRate = false;

		simView.getContainer().setTargetFrameRate(lowFrameRate);

	}

	private void mouseInteractionModeOn()
	{
		if (!highUpdateRate) // Only Toggle if allowed to
		{
			if (!mouseButtonPressed) // Used so the we dont do this repeatedly only the first time
			{

				simView.getContainer().setTargetFrameRate(frameRateGuiInteractionOn);

				mouseButtonPressed = true;
			}

		}
	}

	private void mouseInteractionModeOff()
	{
		if (!highUpdateRate) // Only Toggle if allowed to
		{
			simView.getContainer().setTargetFrameRate(frameRateGuiInteractionOff);

			mouseButtonPressed = false;	// To allow setting interaction on mode again.
		}
	}

	/**
	 * Method setViewRangeDrawing.
	 * @param inViewRangeDrawing boolean
	 */
	public static void setViewRangeDrawing(boolean inViewRangeDrawing)
	{
		viewRangeDrawing = inViewRangeDrawing;
	}

	/**
	 * Method setViewRangeDrawing.
	 * @param inViewRangeDrawing boolean
	 */
	public static void setViewsDrawing(boolean inViewsDrawing)
	{
		viewsDrawing = inViewsDrawing;
	}
	
	/**
	 * Method setSimpleDrawing.
	 * @param inSimpleDrawing boolean
	 */
	public static void setSimpleDrawing(boolean inSimpleDrawing)
	{
		simpleDrawing = inSimpleDrawing;
	}

	/**
	 * Method setVisible.
	 * @param visible boolean
	 */
	public static void setVisible(boolean visible)
	{
			drawSim = visible; // draw if visible
	}

 	/**
 	 * Method setInitalViewTranslate.
	 * @param x int
	 * @param y int
	 */
	/*public static void setInitalViewTranslate(int viewWidth, int viewHeight)
	{
		int worldSize = sim.simManager.getWorldSize();		
		globalTranslate.set((viewWidth / 2) - ((worldSize) / 2), (viewHeight / 2) - ((worldSize) / 2));		
	}*/
	
	/* Parent Frame Size Change */
	public static void updateCameraBound()
	{	

			panelWidth = simView.getContainer().getWidth();
			panelHeight = simView.getContainer().getHeight();
			
			cameraBound = new Rectangle(cameraMargin, cameraMargin, panelWidth - (cameraMargin * 2), panelHeight - (cameraMargin * 2));
	}
	
}