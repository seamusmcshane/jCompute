package alife;

import java.awt.BorderLayout;
import java.awt.Frame;
import javax.swing.JFrame;

 
// NOTE! The following three imports are need when creating executable jar 
/*import org.lwjgl.LWJGLException;
import org.lwjgl.LWJGLUtil; 
import java.io.File; */
import org.lwjgl.opengl.Display;
import org.newdawn.slick.BasicGame;
import org.newdawn.slick.CanvasGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.MouseListener;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.geom.Vector2f;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
/**
 * Simulation class - Gui and Entry Point for starting a Simulation.
 */
public class SimulationView extends BasicGame implements MouseListener
{
	/** Window */
	static JFrame frmSimulationView;
	
	/** Window Position */
	static int x;
	static int y;
	
	/** OpenGl Canvas */
	static CanvasGameContainer simView;
	
	/** OpenGL Canvas Size */
	static int world_view_width;
	static int world_view_height;

	/** Default Graphic frame rate control */
	final static int default_frame_rate = 15;
	static int frame_rate = default_frame_rate; // Frame rate starts up set at this
	final static int frame_rate_gui_interaction = 60;

	/** Simulation Reference */
	static Simulation sim;
	
	/** Is Simulation view drawing enabled */
	static boolean draw_sim=true;
	
	/** Draw true circular bodies or faster rectangular ones */
	static boolean true_drawing=false;
	
	/** Draw the View range of the agents */
	static boolean view_range_drawing=false;
	
	/**
	 * This locks the frame rate to the following rate allowing more time
	 * to be used for simulation threads.
	 */
	boolean frame_cap = true;

	/** Frame rate should be greater than or equal to refresh rate if used */
	static boolean vsync_toggle = false;

	/** Simulation Performance Indicators */
	private int frame_num = 0;

	/* Draw slow but accurate circular bodies or faster rectangular ones */
	private Boolean true_body_drawing = false;

	/** Toggle for Drawing agent field of views */
	private Boolean draw_field_of_views = false;

	/** The translation vector for the camera view */
	public static Vector2f global_translate = new Vector2f(0, 0);

	/** Off screen buffer */
	private Graphics bufferGraphics;
	private Image buffer;
	private int buffer_num = 0;

	/** Stores the mouse vector across updates */
	public Vector2f mouse_pos = new Vector2f(0, 0);

	/** Stores the camera margin */
	static int camera_margin = 0;

	/** Camera View Size */
	public static Rectangle camera_bound;
	
	/** Allows fixing the update rate at the mouseInteraction rate **/
	private static boolean highUpdateRate=false;
	
	/** Toggles the drawing of the text overlay */
	private static boolean overlay=false;
	
	/**
	 * The Simulation View.
	 */
	public SimulationView()
	{
		super("Simulation View");	
	}

	@Override
	public void init(GameContainer container) throws SlickException
	{
		/* Creates the buffered graphic */		setUpImageBuffer();	
	}
	
	@Override
	public void update(GameContainer container, int delta) throws SlickException
	{
		// Not Used
	}

	@Override
	public void render(GameContainer container, Graphics g) throws SlickException
	{
		if(draw_sim)
		{
			/* AMD Opensource Linux Drivers have hardware clipping bugs (Update Drivers to Xorg 1.12 for fix) */			
			g.setWorldClip(camera_bound);
			
			/* Draws on the buffer */
			doDraw(bufferGraphics);

			/* Always draw the buffer even if it has not changed */
			g.drawImage(buffer, 0, 0);
 
			// View Overlay
			if(overlay)
			{
				g.drawString("Frame Updates     :" + frame_num, camera_bound.getMinX()+10, camera_bound.getMinY() + 10);

				g.drawString("Buffer Updates    :" + buffer_num, camera_bound.getMinX()+10, camera_bound.getMinY() + 30);

				g.drawString("Frames Per Second :" + simView.getContainer().getFPS(), camera_bound.getMinX()+10, camera_bound.getMinY() + 50);

				g.draw(camera_bound);				
			}					
			frame_num++;			
		}
	}
	
	/** Draws the sim view on the image buffer */
	private void doDraw(Graphics g)
	{
		/* Blank the Image buffer */
		g.clear();

		/* Move the entire world to simulate a view moving around */
		g.translate(global_translate.getX(), global_translate.getY());
		
		sim.drawSim(g,true_drawing,view_range_drawing);

		/* Performance Indicator */
		buffer_num++;
	}
	
	/**
	 * Makes sure valid mouse coordinates are used when the mouse leaves and
	 * renters a window that has lost and regained focus. - Prevents view
	 * snapping to strange locations
	 */
	@Override
	public void mousePressed(int button, int x, int y)
	{
		mouse_pos.set(x - global_translate.getX(), y - global_translate.getY());
				
		mouseInteractionModeOn(); // Changes to a higher frame update rate
		
		//req_sps = 9999;
		
	}
	
	/** Allows moving camera around large worlds via mouse dragging on the simulation view */
	@Override
	public void mouseDragged(int oldx, int oldy, int newx, int newy)
	{
		float x = (newx) - mouse_pos.getX();
		float y = (newy) - mouse_pos.getY();

		moveCamera(x, y);

	}

	@Override
	public void mouseReleased(int button, int x, int y)
	{
		mouse_pos.set(x - global_translate.getX(), y - global_translate.getY());
				
		mouseInteractionModeOff(); // Changes to a lower frame update rate
		
	}	
	
	@Override
	public void mouseWheelMoved(int change)
	{

	}

	/** Camera is moved by translating all the drawing */
	private void moveCamera(float x, float y)
	{
		global_translate.set(x, y);
	}
	
	public static void exitDisplay()
	{
		Display.destroy();
	}
	
	/** Setup Window */
	private static void setUpWindowDimesions(int xin, int yin, int width, int height)
	{
		/* Position */
		x = xin;
		y = yin;
		
		/* Size */
		world_view_width = width;
		world_view_height = height; 
	}
		
	/* Main Entry Point for View */
	/**
	 * @wbp.parser.entryPoint
	 */
	public static void displayView(Simulation simIn,int x, int y, int width, int height)
	{
		
		sim = simIn;
		
		setUpWindowDimesions(x,y,width,height);
		
		try
		{

			frmSimulationView = new JFrame("Simulation");
			frmSimulationView.setTitle("Simulation View");
			frmSimulationView.addWindowListener(new WindowAdapter() {
				
				public void windowIconified(WindowEvent e)
				{
					SimulationGUI.minimise();
				}

				public void windowDeiconified(WindowEvent e)
				{
					SimulationGUI.maximise();
					SimulationView.maximise();
				}
			});

			BorderLayout borderLayout = (BorderLayout) frmSimulationView.getContentPane().getLayout();
			borderLayout.setVgap(10);
			borderLayout.setHgap(10);
			//frame.setType(Type.UTILITY);
			//frame.setUndecorated(true);
			
			frmSimulationView.setSize(world_view_width, world_view_height);
			
			frmSimulationView.setLocation(x, y);
			//frame.setAlwaysOnTop(true);
			
			/*
			 * - For stand alone builds un-comment these so the jar will look
			 * for the native libraries correctly
			 */
			/* System.setProperty("org.lwjgl.librarypath", new File(new File(System.getProperty("user.dir"), "native"), LWJGLUtil.PLATFORM_WINDOWS_NAME).getAbsolutePath());
			 System.setProperty("net.java.games.input.librarypath", System.getProperty("org.lwjgl.librarypath"));
			 */
			simView = new CanvasGameContainer(new SimulationView());
			
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
			simView.getContainer().setTargetFrameRate(default_frame_rate);

			frmSimulationView.getContentPane().add(simView, BorderLayout.CENTER);

			frmSimulationView.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); 

			frmSimulationView.setVisible(true);
			
			frmSimulationView.setResizable(false);
			
			simView.start();
			
			camera_bound = new Rectangle(camera_margin, camera_margin, world_view_width - (camera_margin * 2), world_view_height - (camera_margin * 2 ));
			
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
	
	public static void setUnlimitedUpdateRate()
	{
		highUpdateRate = true;
		
		frame_rate = frame_rate_gui_interaction;
		
		simView.getContainer().setTargetFrameRate(-1);
	}	
	
	public static void setVerticalSync(boolean sync)
	{
		vsync_toggle = sync;
		simView.getContainer().setVSync(vsync_toggle);
	}
	
	public static void setHighUpdateRate()
	{
		highUpdateRate = true;
		
		frame_rate = frame_rate_gui_interaction;
		
		simView.getContainer().setTargetFrameRate(frame_rate);
		
	}
	
	public static void setViewOverLay(boolean ioverlay)
	{
		overlay = ioverlay;
	}
	
	public static void setStandardUpdateRate()
	{
		highUpdateRate = false;

		frame_rate = default_frame_rate;	
		
		simView.getContainer().setTargetFrameRate(frame_rate);		
		
	}
	
	private void mouseInteractionModeOn()
	{
		if(!highUpdateRate) // Only Toggle if allowed to
		{
			frame_rate = frame_rate_gui_interaction;
			
			simView.getContainer().setTargetFrameRate(frame_rate);		
		}
	}

	private void mouseInteractionModeOff()
	{
		if(!highUpdateRate) // Only Toggle if allowed to
		{		
			frame_rate = default_frame_rate;	
			
			simView.getContainer().setTargetFrameRate(frame_rate);
		}
	}
	
	public static void setInitalViewTranslate(int x,int y)
	{
		global_translate.set( x,y );
	}
	
	public static void setFocus()
	{
		if(simView!=null)
		{
			simView.setFocusable(true);
			simView.requestFocus();
			System.out.println("Got Focus");
			simView.setFocusable(false);

			//simView.transferFocus();			
		}
	}
	
	public static void maximise()
	{
		frmSimulationView.setVisible(true);
		frmSimulationView.setState(Frame.NORMAL);
	}
	
	public static void setViewRangeDrawing(boolean in_view_range_drawing)
	{
		view_range_drawing = in_view_range_drawing;
	}
	
	public static void setTrueDrawing(boolean in_true_drawing)
	{
		true_drawing = in_true_drawing;
	}
	
	public static void setVisible(boolean visible)
	{
		if(frmSimulationView!=null)
		{
			frmSimulationView.setVisible(visible);
			draw_sim = visible; // draw if visible
		}
	}
	
	public static void minimise()
	{
		frmSimulationView.setVisible(false);
	}	

}