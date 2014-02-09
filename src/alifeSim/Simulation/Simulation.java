package alifeSim.Simulation;

import java.util.LinkedList;
import java.util.concurrent.Semaphore;

import alifeSim.ChartPanels.StatPanelAbs;
import alifeSim.Gui.NewSimView;
import alifeSim.Scenario.ScenarioInf;

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
	public enum SimulationState
	{		
		NEW ("NEW"),
		RUNNING ("RUNNING"),
		PAUSED ("PAUSED"),
		FINISHED ("FINISHED");

	    private final String name;

	    private SimulationState(String name) 
	    {
	        this.name = name;
	    }

	    public String toString()
	    {
	       return name;
	    }
	};
	
	/* Stats */
	private SimulationPerformanceStats simStats;
	
	// Inter-step delay Calculations
	private long stepTimeNow;
	private long stepTimeDiff;
	private long stepTimeTotal;
	private long stepTimePrev;

	/* The Simulation manager */
	private SimulationScenarioManagerInf simManager;

	/* The default simulation update rate */
	private int reqSps = 15;

	/* Sim Start/Pause Control */
	private Semaphore pause;

	/* Simulation state */
	private SimulationState state = SimulationState.NEW;	

	/* Simulation Update Thread */
	private Thread asyncUpdateThread;
	private boolean running=true;

	private boolean realtime=true;
	
	private LinkedList<StatPanelAbs> charts;
	
	public Simulation(SimulationPerformanceStats stats)
	{
		simStats = stats;	
		setupThreads();

		createSimScenario(null); // Never used - needed for successful startup		
	}

	/**
	 * Method createSim.
	 */
	public void createSimScenario(ScenarioInf scenario)
	{
		if(scenario!=null)
		{
			System.out.println("Assigning Sim Manager");
			simManager = scenario.getSimManager();
			
			System.out.println("Scenario Type : " + scenario.getScenarioType());

		}		
		
		simStats.clearSimulationStats();
	}

	/**
	 * This method initiates the thread shutdown sequence in the Simulation Manager
	 */
	public void destroySim()
	{
		// Pause will get the simulation threads to a safe position, i.e not
		// inside a list.
		if ( simPaused() == SimulationState.PAUSED)
		{
			pauseSim();
		}
		
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
								
				while (running)
				{
						simUpdate();
						//asyncUpdateThread.yield();
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
		
		simStats.updateStatsOutput();
		
		for (StatPanelAbs panel : charts) 
		{
			panel.update();
		}
		
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
		unPauseSim();
	}

	/**
	 *  Toggle Pause/UnPause
	 */
	
	public SimulationState togglePause()
	{
		if(state == SimulationState.RUNNING)
		{
			pauseSim();
		}
		else if(state == SimulationState.PAUSED)
		{
			unPauseSim();
		}
		else
		{
			System.out.println("ATTEMPT to PAUSE Simulation in :" + state);
		}
		
		return state;
	}
	
	/**
	 * UnPauses the Sim.
	 */
	public void unPauseSim()
	{
		state = SimulationState.RUNNING;
		
		System.out.println("Sim " + state.toString());
		
		pause.release();					// Release the pause semaphore
		
	}

	/**
	 * Method simPaused.
	 * @return boolean */
	public SimulationState simPaused()
	{
		return state;
	}

	/**
	 * Pauses the Sim and sets the display frame rate to a more-interactive and more intensive update rate for better mouse interaction 
	 */
	public void pauseSim()
	{
		pause.acquireUninterruptibly();		// Pause the sim

		state = SimulationState.PAUSED;

		System.out.println("Sim " + state.toString());
		
	}

	/**
	 * Method reqSimUpdateRate.
	 * @param steps int
	 */
	public void setReqStepRate(int steps)
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

	public SimulationScenarioManagerInf getSimManager()
	{
		return simManager;
	}
	
	/**
	 * Method drawSim.
	 * @param g Graphics
	 * @param true_drawing boolean
	 * @param view_range_drawing boolean
	 */
	public void drawSim(NewSimView simView,boolean viewRangeDrawing,boolean viewsDrawing)
	{
		if(simManager!=null)
		{
			simManager.drawSim(simView,viewRangeDrawing,viewsDrawing);
		}
	}

	public void setOutPutCharts(LinkedList<StatPanelAbs> charts)
	{
		this.charts = charts;
	}	
	
	public SimulationPerformanceStats getStats()
	{
		return simStats;
	}

	public SimulationState getState()
	{
		return state;
	}

	public SimulationPerformanceStats getSimulationPerformanceStats()
	{
		return simStats;
	}
		
}