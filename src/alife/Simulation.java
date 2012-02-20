package alife;

/* The following two imports are for creating executable jar */
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import org.lwjgl.LWJGLUtil;
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
import org.newdawn.slick.AppGameContainer;
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
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;
import net.miginfocom.swing.MigLayout;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.JSeparator;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Main App class - the entry point for the starting of the simulation.
 * 
 * 
 */

public class Simulation extends BasicGame implements MouseListener
{

	static CanvasGameContainer sim;

	/* Frame Items */
	private static JSlider slider;
	private static JButton btnPause;
	
	/* Window or Screen Size */
	static int screen_width = 800;
	static int screen_height = 800;

	/* Graphic frame rate control */
	static int frame_rate = 60;
	/*
	 * This locks the frame rate to the above rate giving what time would have
	 * been used to threads
	 */
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
	Boolean true_body_drawing = true;

	/** Toggle for Drawing agent field of views */
	Boolean draw_field_of_views = true;

	/** Simulation Agent Manager */
	AgentManager agentManager;

	/** The Simulation World. */
	static World world;

	/*
	 * Size of the world - Pixels - recommended to be power of 2 due to OpenGL
	 * texture limits
	 */
	static int world_size = 512;

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

	public static Rectangle camera_bound = new Rectangle(0 + camera_margin, 0 + camera_margin, screen_width - (camera_margin * 2), screen_height - (camera_margin * 2));
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

			agentManager.addNewAgent(new SimpleAgent(i, x, y, new SimpleAgentStats(1f, 10f, 100f, 100f, 25f)));

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

		// g.drawString("Frame Rate : " + app.getFPS(), camera_bound.getMinX(),
		// camera_bound.getMinY() + 200);

		g.drawString("Draw Div : " + draw_div, camera_bound.getMinX(), camera_bound.getMinY() + 250);

		g.draw(camera_bound);

	}

	private void doDraw(Graphics g)
	{
		/* Blank the Image buffer */
		g.clear();

		g.translate(global_translate.getX(), global_translate.getY());

		/*
		 * TODO Centers the world in view correctly - Clipping problem with
		 * agents - Math wrong here or somewhere else
		 */
		// g.translate(global_translate.getX()+(screen_width/2-(world_size/2)),
		// global_translate.getY()+(screen_height/2-(world_size/2)));

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

	/*
	 * Keeps the simulation aware of the mouse position minus any translation of
	 * the view
	 */
	@Override
	public void mouseMoved(int oldx, int oldy, int newx, int newy)
	{
		mouse_pos.set(newx - global_translate.getX(), newy - global_translate.getY());
	}

	/* Todo - Not used */
	@Override
	public void mousePressed(int button, int x, int y)
	{
		/*
		 * if (button == 0) { Simulation.app.setTargetFrameRate(-1); } else {
		 * Simulation.app.setTargetFrameRate(15); }
		 */
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

	private static void setupFrame()
	{
		int height = 800;
		JFrame frame = new JFrame("Simulation");
		frame.setResizable(false);
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screenSize = toolkit.getScreenSize();
		frame.setBounds((int) (screenSize.getWidth() / 2) - (500), screenSize.height / 2 - 400, 1000, height);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));

		frame.getContentPane().add(sim, BorderLayout.CENTER);

		JPanel controlPanel = new JPanel();
		controlPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		controlPanel.setPreferredSize(new Dimension(250, 800));

		frame.getContentPane().add(controlPanel, BorderLayout.EAST);
		controlPanel.setLayout(new BorderLayout(0, 0));

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		controlPanel.add(panel, BorderLayout.SOUTH);
		panel.setLayout(new GridLayout(2, 3, 5, 5));

		JLabel label = new JLabel("Sim Rate");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(label);

		/* Simulation Speed */
		txtSimRateInfo = new JTextField();
		txtSimRateInfo.setEditable(false);
		txtSimRateInfo.setText("SimRateInfo");
		txtSimRateInfo.setColumns(10);

		slider = new JSlider();
		slider.setMinimum(1);
		slider.setPaintTicks(true);
		slider.setMinorTickSpacing(5);
		slider.setMajorTickSpacing(10);
		slider.setMaximum(50);
		slider.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				if(!simPaused)
				{
					txtSimRateInfo.setText(Integer.toString(slider.getValue()));
					draw_div = slider.getValue();					
				}

			}
		});
		panel.add(slider);
		slider.setValue(draw_div);
		panel.add(txtSimRateInfo);

		JButton btnStart = new JButton("Start");
		panel.add(btnStart);

		btnPause = new JButton("Pause");
		btnPause.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				if(simPaused)
				{
					btnPause.setText("Pause");
					
					draw_div = latched_div;
					
					slider.setValue(draw_div);
					
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
		panel.add(btnPause);

		JButton btnReset = new JButton("Reset");
		panel.add(btnReset);

		JPanel panel_1 = new JPanel();
		controlPanel.add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new MigLayout("", "[][grow]", "[][]"));

		JLabel lblAgents = new JLabel("Agents");
		panel_1.add(lblAgents, "cell 0 0,alignx trailing");

		txtAgentno = new JTextField();
		txtAgentno.setText("AgentNo");
		panel_1.add(txtAgentno, "cell 1 0,growx");
		txtAgentno.setColumns(10);

		JLabel lblWorldSize = new JLabel("World Size");
		panel_1.add(lblWorldSize, "cell 0 1,alignx trailing");

		JComboBox comboBox = new JComboBox();
		comboBox.setModel(new DefaultComboBoxModel(new String[]
		{"512", "1024", "2048", "4096", "8192", "16384", "32768", "65536", "131072", "262144"}));
		panel_1.add(comboBox, "cell 1 1,growx");

		JPanel statusPanel = new JPanel();
		statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		frame.getContentPane().add(statusPanel, BorderLayout.SOUTH);
		statusPanel.setLayout(new MigLayout("", "[][][][][][][][][]", "[]"));

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
				Display.destroy();
				System.exit(0);
			}
		});
		frame.setVisible(true);

	}

	private void setUpImageBuffer()
	{
		try
		{
			buffer = new Image(screen_width + 1, screen_height + 1);
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