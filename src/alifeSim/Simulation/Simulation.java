package alifeSim.Simulation;

import java.util.concurrent.Semaphore;
import org.newdawn.slick.Graphics;

import alifeSim.Gui.SimulationGUI;
import alifeSim.Gui.StatsPanel;
import alifeSim.Scenario.ScenarioInf;
import alifeSim.Scenario.ScenariosTypes.Scenario;
import alifeSim.Scenario.Debug.DebugScenario;
import alifeSim.Scenario.Debug.DebugSimulationManager;
import alifeSim.Scenario.SAPP.SAPPScenario;
import alifeSim.Scenario.SAPP.SAPPSimulationManager;

/**
 * Simulation class
 * This class handles the Simulation activity of the program.
 * It contains the main loop in the form of the asynchronous Simulation Update Thread.
 * Apart from performing a step, it is primarily concerned with timing the step and ensuring statistics are updated..
 * @author Seamus McShane
 * @version $Revision: 1.0 $
 */
public class Simulation
{
	/* Stats */
	private SimulationPerformanceStats simStats;
	

	// Inter-step delay Calculations
	private long stepTimeNow;
	private long stepTimeDiff;
	private long stepTimeTotal;
	private long stepTimePrev;

	/* The Simulation manager */
	private SimulationManagerInf simManager;

	/* The default simulation update rate */
	private int reqSps = 15;

	/* Sim Start/Pause Control */
	private Semaphore pause;

	/* Simulation state */
	private boolean simPaused = true;
	private boolean simStarted = false;

	/* Simulation Update Thread */
	private Thread asyncUpdateThread;
	private boolean running=true;

	private boolean realtime=true;
	
	public Simulation()
	{
		simStats = new SimulationPerformanceStats();		
		setupThreads();

		createSim(null); // Never used - needed for successful startup		
	}

	/**
	 * Method createSim.
	 */
	public void createSim(ScenarioInf scenario)
	{
		if(scenario!=null)
		{
			createScenario(scenario);	
		}		
		
		simStats.clearSimulationStats();
	}
	
	/*
	 * Master Scenario Hander
	 */
	private void createScenario(ScenarioInf scenario)
	{
		System.out.println("Create Scenario");
		
		if(scenario.getScenarioType().equals("DEBUG"))
		{
			/* Switch Scenarios */
			simManager = new DebugSimulationManager((DebugScenario) scenario);
		}
		else if (scenario.getScenarioType().equals("SAPP"))
		{
			/* Switch Scenarios */
			simManager = new SAPPSimulationManager((SAPPScenario) scenario);
		}
		else
		{
			System.out.println("UKNOWN Scenario Type");
		}
	}
	
	/**
	 * This method initiates the thread shutdown sequence in the Simulation Manager
	 */
	public void destroySim()
	{
		if(simManager!=null)
		{
			/* Initiate clean up */
			simManager.cleanUp();
			
			/* Set it to null so the garbage collector can get to work */
			simManager=null;
		}			
	}
	
	/* Simulation Main Thread - The step update loop */
	private void setupThreads()
	{
		pause = new Semaphore(0, true); // Starts Paused

		asyncUpdateThread = new Thread(new Runnable()
		{
			public void run()
			{
				//Thread thisThread = Thread.currentThread();

				/* Top Priority to the simulation thread */
				//thisThread.setPriority(Thread.MAX_PRIORITY);
				
				simStats.setUpStepsPerSecond();
				
				while (running)
				{
						simUpdate();
						asyncUpdateThread.yield();
				}
			}
		}, "Simulation Update Thread"

		);

		asyncUpdateThread.start();

	}

	private void simUpdate()
	{
		// The pause semaphore (We do not pause half way through a step)
		pause.acquireUninterruptibly();
		
		simStats.setStepStartTime();

		// record step start time for inter-step delay
		timeTotal();

		// This single method hides the rest of the sim
		simManager.doSimulationUpdate();

		// Increment the Step counter
		simStats.incrementSimulationSteps();
		
		
		/*if( (simStats.getSimulationSteps() % 100 ) == 0)
		{
			System.out.println("Step : " + simStats.getSimulationSteps());
			simManager.displayDebug();
		}*/
		
		// Calculate the Steps per Second
		simStats.calcStepsPerSecond();

		// Only do interstep wait if ask to run in real-time @ a specific step rate, otherwise do not wait thus run as fast as possible
		if(realtime)
		{
			// Calculate how much we need to wait (in nanoseconds, based on the time taken so far) before proceeding to the next step 
			while (timeTotal() < (1000000000 / reqSps)) // Approximation of what the inter-step delay should be
			{
			// Inter-Step Busy wait delay (66ms~ for 15 steps per second)
			// This will only wait if the step performance level is being exceeded
			// Waiting between steps ensures smooth animiation on the view
			}
		}
		// resets the value calculated in timeTotal()
		resetTotalTime();

		simStats.setStepEndTime();
		
		// Allow the simulation to be paused again
		pause.release();
	
	}

	/**
	 * Calculates the total taken between repeated call to this method - used for inter-step time wait
	 * @return long */
	private long timeTotal()
	{
		stepTimeNow = System.nanoTime();		 // Current Time

		stepTimeDiff = stepTimeNow - stepTimePrev; // Time Between this call and the last call

		stepTimeTotal += stepTimeDiff;			 // Total the time between calls

		stepTimePrev = stepTimeNow;				 // Set the current time as the previous to the next call

		return stepTimeTotal;					 // Return the current total
	}

	private void resetTotalTime()
	{
		stepTimeTotal = 0;
	}

	/**
	 * Called by the start button
	 */
	public void startSim()
	{
		simStarted = true;
		unPauseSim();
	}

	/**
	 * UnPauses the Sim.
	 */
	public void unPauseSim()
	{
		simPaused = false;					// Sets the logic boolean to indicate to the other parts of the code that the sim is now unpaused.
		pause.release();					// Release the pause semaphore
	}

	/**
	 * Method simPaused.
	 * @return boolean */
	public boolean simPaused()
	{
		return simPaused;
	}

	/**
	 * Pauses the Sim and sets the display frame rate to a more-interactive and more intensive update rate for better mouse interaction 
	 */
	public void pauseSim()
	{
		pause.acquireUninterruptibly();		// Pause the sim

		simPaused = true;					// Sets the logic boolean to indicate to the other parts of the code that the sim is now paused.					
	}

	/**
	 * Method reqSimUpdateRate.
	 * @param steps int
	 */
	public void reqSimUpdateRate(int steps)
	{
		if (steps > 0)
		{
			reqSps = steps;
			
			realtime=true;
		}
		else
		{
			reqSps = 0;	
					
			realtime=false;
			
		}

	}

	/**
	 * Method drawSim.
	 * @param g Graphics
	 * @param true_drawing boolean
	 * @param view_range_drawing boolean
	 */
	public void drawSim(Graphics g, boolean true_drawing, boolean view_range_drawing,boolean viewsDrawing)
	{
			simManager.drawSim(g, true_drawing, view_range_drawing,viewsDrawing);
	}	
	
}