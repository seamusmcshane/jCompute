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
import java.util.concurrent.Semaphore;

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
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.JSeparator;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;

import net.miginfocom.swing.MigLayout;
import javax.swing.JCheckBox;
/**
 * Simulation class - GUI and Entry Point for starting a Simulation.
 */
public class Simulation
{
	/* Default Graphic frame rate control */
	final int default_frame_rate = 15;
	int frame_rate = default_frame_rate; // Frame rate start up at this
	final int frame_rate_gui_interaction = 60;

	/** Simulation Performance Indicators */
	int step_num = 0;
	double sps = 0; 					// steps per second
	boolean real_time;
	
	// Step Per Second Calculation
	private long startTime;
	private long previousTime;
	private long currentTime;
	private long diffTime;	

	private int num_samples = 150;
	private double step_samples[] = new double[num_samples];
	private double tasps; 			// To avoid a cumulative rounding error when calculating the average, a double is use
	private int asps;	 			// Average Steps Per Second as an int for display purposes
	
	// Fixed Step Calculations
	private long stepTimeNow;
	private long stepTimeDiff;
	private long stepTimeTotal;
	private long stepTimePrev;
	private int stepsCurrent;
	
	/* Number of Agents */
	int initial_num_agents=0;
	
	SimulationManager simManager;
	
	/* The translation vector for the camera view */
	public Vector2f global_translate = new Vector2f(0, 0);

	/* For this many simulation updates for buffer update */
	public int req_sps = 15;
	
	/* For increasing req steps by a factor of ten */
	public int stepx10 = 1;

	/* Sim Start/Pause Control */
	private Semaphore pause;
		
	private boolean simPaused = true;
	private boolean simStarted = false;
	private int latched_div = 0;

	/** Simulation Update Thread */
	private Thread asyncUpdateThread;
	
	/** The Simulation World. */
	public World world;

	/** need to sync graph updates with sim updates */
	private StatsPanel stats;
	
	public Simulation()
	{
		setupThreads();
		
		newSim(null,256,0,0,0);
	}

	public void newSim(StatsPanel stats,int world_size,int agent_prey_numbers,int agent_predator_numbers, int plant_numbers)
	{
		
		this.stats = stats;
		
		world = new World(world_size);

		simManager = new SimulationManager(world_size,agent_prey_numbers,agent_predator_numbers,  plant_numbers);
	}
	
	// Simulation Main Thread  
	private void setupThreads()
	{
			pause = new Semaphore(0,true); // Starts Paused
			
			asyncUpdateThread = new Thread(new Runnable()
			{
				public void run()
				{
					Thread thisThread = Thread.currentThread();
	
					/* Top Priority to the simulation thread */
					thisThread.setPriority(Thread.MAX_PRIORITY);
					
					setUpStepsPerSecond();
					
					while (true)
					{
							Thread.yield(); // Allow other threads to run				

							// The pause semaphore (We do not pause half way through a step)
							pause.acquireUninterruptibly();
							
							simManager.doSimulationUpdate();
							
							// Calculate the Steps per Second
							calcStepsPerSecond();					

							// Increment the Step counter
							step_num++;
																					
							// Calculate how much we need to wait (in milliseconds, based on the time taken so far) before proceeding to the next step 
							while(timeTotal() < (1000/req_sps)) // Approximation of what the inter-step delay should be
							{
								// Inter-Step Busy wait delay (66ms~ for 15 steps per second)
							}
							
							resetTotalTime();
							
							stats.updateGraph();
							
							// Allow the simulation to be paused again
							pause.release();
					}
				}
			}
	
			);
	
			asyncUpdateThread.start();	

	}
	
	private void setUpStepsPerSecond()
	{
		startTime = System.currentTimeMillis();
		previousTime = startTime;				// At Start up this is true
		currentTime = System.currentTimeMillis();
		diffTime = currentTime-previousTime;	// Diff time is initialized
	}
	
	// Calculates the Average Steps Per Second
	private void calcStepsPerSecond()
	{
		currentTime = System.currentTimeMillis();	// Current TIme
		
		diffTime = currentTime-previousTime;		// Time between this and the last call
				
		sps = 1000f/(diffTime) ;					// Gives a instantaneous performance indicator of steps per second
				
		previousTime = currentTime;		 			// Stores the current diff for the diff in the next iteration
		
		for(int i=0;i<(num_samples-1);i++)			// Moves the previous samples back by 1, leaves space for the new sps sample 
		{
			step_samples[i] = step_samples[(i+1)];
		}
		
		step_samples[num_samples-1] = sps;			// Store the new sps sample
		
		tasps = 0;									// clear the old total average (or it will increment for ever)
		for(int i=0;i<num_samples;i++)
		{
			tasps+=step_samples[i];					// Total all the steps
		}

		StatsPanel.setASPS(averageStepsPerSecond());
		
	}
	
	// Average the steps thus giving an average steps per second count
	public int averageStepsPerSecond()
	{
		return asps = (int)(tasps/num_samples);			
	}

	// Calculates the total taken between repeated call to this method - used for inter-step time wait
	private long timeTotal()
	{
		stepTimeNow = System.currentTimeMillis();		 // Current Time
		
		stepTimeDiff = stepTimeNow-stepTimePrev; // Time Between this call and the last call
		
		stepTimeTotal+=stepTimeDiff;			 // Total the time between calls
		
		stepTimePrev = stepTimeNow;				 // Set the current time as the previous to the next call
		
		return stepTimeTotal;					// Return the current total
	}
	
	private void resetTotalTime()
	{
		stepTimeTotal=0;
	}
	
	// Called by the start button
	public void startSim()
	{
		latched_div=1;
		simStarted=true;
		unPauseSim();		
	}
	
	// UnPauses the Sim and sets the display frame rate to a less-interactive and less intensive update rate 
	public void unPauseSim()
	{
				
		simPaused = false;					// Sets the logic boolean to indicate to the other parts of the code that the sim is now unpaused.
		
		pause.release();					// Release the pause semaphore
			 
	}

	public boolean simPaused()
	{
		return simPaused;
	}
	
	// Pauses the Sim and sets the display frame rate to a more-interactive and more intensive update rate for better mouse interaction 
	public void pauseSim()
	{
			pause.acquireUninterruptibly();		// Pause the sim
					
			simPaused = true;					// Sets the logic boolean to indicate to the other parts of the code that the sim is now paused.					
	}
		
	public void reqSimUpdateRate(int steps)
	{
		if(steps>0)
		{
			req_sps = steps;
		}
	}
	
	public void drawSim(Graphics g)
	{	
		if(simStarted)
		{
			if(world!=null)
			{
				world.drawWorld(g);
			}
			simManager.drawAgentsAndPlants(g);			
		}
	}
}