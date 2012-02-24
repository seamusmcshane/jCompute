package alife;

/* NOTE! The following two imports are for creating executable jar */
import java.io.File;
import org.lwjgl.LWJGLUtil;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import org.lwjgl.opengl.Display;

import java.util.Random;

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

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JButton;
import javax.swing.border.EtchedBorder;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;
import javax.swing.UIManager;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.JTextField;
import javax.swing.JProgressBar;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.JSeparator;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;

import net.miginfocom.swing.MigLayout;
/**
 * Simulation class - Gui and Entry Point for starting a Simulation.
 */
public class Simulation extends BasicGame implements MouseListener
{
	/** OpenGl Canvas */
	static CanvasGameContainer sim;

	/** Gui Frame Items */
	private static JSlider simRateSlider;
	private static JButton btnPause;
	
	/** Window Size */
	static int width = 1000;
	static int height = 800;
	
	/** Right Control Panel Size */
	static int controlPanelWidth = 250;
	static int controlPanelHeight = 800;
	
	/** Status Bar Size */
	static int statusPanelWidth = width;
	static int statusPanelHeight = 30;
	
	/** OpenGL Canvas Size */
	static int world_view_width = width - controlPanelWidth-5;  /* Padding of Control Panel is about 5px */
	static int world_view_height = height-statusPanelHeight-50; /* menu bar is about 50px */

	/* Graphic frame rate control */
	static int frame_rate = 60;
	
	/** This locks the frame rate to the above rate giving what time would have
	 *  been used to threads */
	static boolean frame_cap = true;

	/** Frame rate should be greater than or equal to refresh rate if used */
	static boolean vsync_toggle = false;

	/** Simulation Performance Indicators */
	static int frame_num = 0;
	static int step_num = 0;
	static boolean real_time;

	/* Number of Agents */
	int num_agents = 51200;

	/* Draw slow but accurate circular bodies or faster rectangular ones */
	Boolean true_body_drawing = false;

	/** Toggle for Drawing agent field of views */
	Boolean draw_field_of_views = false;

	/** Simulation Agent Manager */
	AgentManager agentManager;

	/** The Simulation World. */
	static World world;

	/*
	 * Size of the world - Pixels - recommended to be power of 2 due to OpenGL
	 * texture limits
	 */
	static int world_size = 700;

	/* The translation vector for the camera view */
	public static Vector2f global_translate = new Vector2f(0, 0);

	/* For this many simulation updates for buffer update */
	public static int draw_div = 0;
	
	private static boolean simPaused = true;
	private static int latched_div=0;
	
	int steps_todo = 0;

	/* Off screen buffer */
	Graphics bufferGraphics;
	Image buffer;
	int buffer_num = 0;

	/* Stores the mouse vector across updates */
	public static Vector2f mouse_pos = new Vector2f(world_size, world_size);

	/* Stores the camera position */
	static int camera_margin = 10;

	public static Rectangle camera_bound = new Rectangle(0 + camera_margin, 0 + camera_margin, world_view_width - (camera_margin * 2), world_view_height - (camera_margin * 2));
	private static JTextField txtSimRateInfo;
	private static JTextField txtAgentno;

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

		agentManager = new AgentManager(num_agents);

		agentManager.setTrueDrawing(true_body_drawing);

		agentManager.setFieldOfViewDrawing(draw_field_of_views);

		int i;

		int x, y, t, s;
		for (i = 0; i < num_agents; i++)
		{

			x = xr.nextInt(world_size) + 1;

			y = yr.nextInt(world_size) + 1;

			agentManager.addNewAgent(new SimpleAgent(i, x, y, new SimpleAgentStats(1f, 5f, 100f, 100f, 25f)));

		}

		setUpImageBuffer();

	}

	@Override
	public void update(GameContainer container, int delta) throws SlickException
	{
		/* Not Used */
		steps_todo = 0;

		/* Frame Rate is 60 - Update at 1/4 of that for our target sim rate */
		if (frame_num % 4 == 0)
		{
			while (steps_todo < draw_div)
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

		/*
		 * Frame Rate is 60 - Sim rate is 1/4 of that for our target sim rate so
		 * only redraw the buffer when the sim has updated
		 */
		if (frame_num % 4 == 0)
		{
			doDraw(bufferGraphics);
		}

		/* Always draw the buffer even if it has not changed */
		g.drawImage(buffer, 0, 0);

		frame_num++;

		/* Gui Overlay */
		g.setColor(Color.white);

		g.drawString("Alife Sim Test - Number Active Agents : " + num_agents, camera_bound.getMinX(), camera_bound.getMinY());

		g.drawString("Step Num : " + step_num, camera_bound.getMinX(), camera_bound.getMinY() + 50);

		/*
		 * If the frame rate greater than or equal to the target we are updating
		 * in real-time or a multiple of real-time
		 */
		/*
		 * if(app.getFPS() >= frame_rate ) { real_time=true; } else {
		 * real_time=false; }
		 */

		g.drawString("Frame Updates + ( Real-time : " + Boolean.toString(real_time) + ") : " + frame_num, camera_bound.getMinX(), camera_bound.getMinY() + 100);

		g.drawString("Buffer Updates : " + buffer_num, camera_bound.getMinX(), camera_bound.getMinY() + 150);

		g.drawString("Frame Rate : " + sim.getContainer().getFPS(), camera_bound.getMinX(),camera_bound.getMinY() + 200);

		g.drawString("Draw Div : " + draw_div, camera_bound.getMinX(), camera_bound.getMinY() + 250);

		g.draw(camera_bound);

	}

	private void doDraw(Graphics g)
	{
		/* Blank the Image buffer */
		g.clear();

		/* Move the entire world to simulate a view moving around */
		g.translate(global_translate.getX(), global_translate.getY());

		/* World */
		world.drawWorld(g);

		/* Agents */
		drawAgents(g);

		/* Performance Indicator */
		buffer_num++;
	}

	/* Agent draw method */
	private void drawAgents(Graphics g)
	{
		agentManager.drawAI(g);
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

	/* Makes sure valid mouse coordinates are used when the mouse 
	 * leaves and renters a window that has lost and regained focus.  
	 * - Prevents view snapping to strange locations
	 * */
	@Override
	public void mousePressed(int button, int x, int y)
	{
		mouse_pos.set(x - global_translate.getX(), y - global_translate.getY());
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
		if(!simPaused)
		{
			if (change > 0)
			{
				draw_div++;
			}
			else
			{
				if (draw_div > 0)
				{
					draw_div--;
				}
			}
			
			simRateSlider.setValue(draw_div);
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

			sim = new CanvasGameContainer(new Simulation());

			/* Always update */
			sim.getContainer().setUpdateOnlyWhenVisible(false);

			/* Screen size / Window Size */
			// app.setDisplayMode(screen_width, screen_height, false);

			/* Not needed */
			sim.getContainer().setShowFPS(false);

			/* Needed as we now do asynchronous drawing */
			sim.getContainer().setClearEachFrame(true);

			/* Hardware vsync */
			sim.getContainer().setVSync(vsync_toggle);

			/* Always draw even if window not active */
			sim.getContainer().setAlwaysRender(true);

			/* Dont close the app if we close the sim */
			sim.getContainer().setForceExit(false);
			

			/* Hardware Anti-Aliasing */
			// app.setMultiSample(8);

			/*
			 * For 1-1 drawing we can enforce a frame cap and watch the
			 * simulation in real-time - performance permitting
			 */
			if (frame_cap)
			{
				sim.getContainer().setTargetFrameRate(frame_rate);
			}

			setupFrame();

			sim.start();
		}
		catch (SlickException e)
		{
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private static void setupFrame()
	{
		JFrame frame = new JFrame("Simulation");
		frame.setResizable(false);
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screenSize = toolkit.getScreenSize();
		frame.setBounds((int) (screenSize.getWidth() / 2) - (500), screenSize.height / 2 - 400, width, height);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));

		frame.getContentPane().add(sim, BorderLayout.CENTER);

		JPanel controlPanel = new JPanel();
		controlPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		controlPanel.setPreferredSize(new Dimension(controlPanelWidth, controlPanelHeight));

		frame.getContentPane().add(controlPanel, BorderLayout.EAST);
		controlPanel.setLayout(new BorderLayout(0, 0));

		JPanel controlPanelBottom = new JPanel();
		controlPanelBottom.setBorder(new EmptyBorder(5, 5, 5, 5));
		controlPanel.add(controlPanelBottom, BorderLayout.SOUTH);
		controlPanelBottom.setLayout(new GridLayout(2, 3, 5, 5));

		JLabel lblSimRate = new JLabel("Sim Rate");
		lblSimRate.setHorizontalAlignment(SwingConstants.CENTER);
		controlPanelBottom.add(lblSimRate);

		/* Simulation Speed */
		txtSimRateInfo = new JTextField();
		txtSimRateInfo.setEditable(false);
		txtSimRateInfo.setText("SimRateInfo");
		txtSimRateInfo.setColumns(10);

		simRateSlider = new JSlider();
		simRateSlider.setMinimum(1);
		simRateSlider.setPaintTicks(true);
		simRateSlider.setMinorTickSpacing(5);
		simRateSlider.setMajorTickSpacing(10);
		simRateSlider.setMaximum(50);
		simRateSlider.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				if(!simPaused)
				{
					txtSimRateInfo.setText(Integer.toString(simRateSlider.getValue()));
					draw_div = simRateSlider.getValue();					
				}

			}
		});
		controlPanelBottom.add(simRateSlider);
		simRateSlider.setValue(draw_div);
		controlPanelBottom.add(txtSimRateInfo);

		JButton btnStart = new JButton("Start");
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) 
			{
				simPaused = false;
				draw_div=1;
			}
		});
		controlPanelBottom.add(btnStart);

		btnPause = new JButton("Pause");
		btnPause.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				if(simPaused)
				{
					btnPause.setText("Pause");
					
					draw_div = latched_div;
					
					simRateSlider.setValue(draw_div);
					
					simPaused = false;

				}
				else
				{
					btnPause.setText("Resume");
					
					latched_div=draw_div;
					
					draw_div=0;					
					
					simPaused = true;
				}
			}
		});
		controlPanelBottom.add(btnPause);

		JButton btnReset = new JButton("Reset");
		controlPanelBottom.add(btnReset);

		JPanel controlPanelTop = new JPanel();
		controlPanel.add(controlPanelTop, BorderLayout.CENTER);
		controlPanelTop.setLayout(new MigLayout("", "[][grow]", "[][]"));

		JLabel lblAgents = new JLabel("Agents");
		controlPanelTop.add(lblAgents, "cell 0 0,alignx trailing");

		txtAgentno = new JTextField();
		txtAgentno.setText("AgentNo");
		controlPanelTop.add(txtAgentno, "cell 1 0,growx");
		txtAgentno.setColumns(10);

		JLabel lblWorldSize = new JLabel("World Size");
		controlPanelTop.add(lblWorldSize, "cell 0 1,alignx trailing");

		JComboBox comboBox = new JComboBox();
		comboBox.setModel(new DefaultComboBoxModel(new String[]
		{"512", "1024", "2048", "4096", "8192", "16384", "32768"}));
		controlPanelTop.add(comboBox, "cell 1 1,growx");

		JPanel statusPanel = new JPanel();
		statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		frame.getContentPane().add(statusPanel, BorderLayout.SOUTH);
		statusPanel.setLayout(new MigLayout("", "[][][][][][][][][]", "[]"));
		statusPanel.setPreferredSize(new Dimension(statusPanelWidth, statusPanelHeight));

		JLabel lblFrameRate = new JLabel("Frame Rate");
		statusPanel.add(lblFrameRate, "cell 0 0");

		JLabel lblDframerate = new JLabel("dFrame_Rate");
		statusPanel.add(lblDframerate, "cell 1 0");

		JSeparator separator = new JSeparator();
		statusPanel.add(separator, "cell 2 0");

		JLabel lblBufferUpdates = new JLabel("Buffer Updates");
		statusPanel.add(lblBufferUpdates, "cell 3 0");

		JLabel lblDbufferupdates = new JLabel("dBufferUpdates");
		statusPanel.add(lblDbufferupdates, "cell 4 0");

		JSeparator separator_1 = new JSeparator();
		statusPanel.add(separator_1, "cell 5 0");

		JLabel lblSimulationSteps = new JLabel("Simulation Steps");
		statusPanel.add(lblSimulationSteps, "cell 6 0");

		JLabel lblDsimsteps = new JLabel("dSim_Steps");
		statusPanel.add(lblDsimsteps, "cell 7 0");

		JSeparator separator_2 = new JSeparator();
		statusPanel.add(separator_2, "cell 8 0");

		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		JMenuItem mntmNew = new JMenuItem("New");
		mnFile.add(mntmNew);

		JMenuItem mntmOpen = new JMenuItem("Open");
		mnFile.add(mntmOpen);

		JMenuItem mntmSave = new JMenuItem("Save");
		mnFile.add(mntmSave);

		JMenuItem mntmSaveAs = new JMenuItem("Save As");
		mnFile.add(mntmSaveAs);

		JMenuItem mntmQuit = new JMenuItem("Quit");
		mnFile.add(mntmQuit);

		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);

		JMenuItem mntmAbout = new JMenuItem("About");
		mnHelp.add(mntmAbout);

		frame.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				Display.destroy(); // Tell OpenGL we are done and free the resources used in the canvas. - must be done else sim will lockup.
				System.exit(0);    // Exit the Simulation and let Java free the memory.
			}
		});
		frame.setVisible(true);

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

}