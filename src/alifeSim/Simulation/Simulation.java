package alifeSim.Simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import alifeSim.Gui.View.GUISimulationView;
import alifeSim.Scenario.ScenarioInf;
import alifeSim.Simulation.SimulationState.SimState;
import alifeSim.Simulation.SimulationState.stateChangedInf;
import alifeSim.Simulation.SimulationStats.statChangedInf;

/**
 * Simulation class
 * This class handles the Simulation activity of the program.
 * It contains the main loop in the form of the asynchronous Simulation Update Thread.
 * Apart from performing a step, it is primarily concerned with timing the step and ensuring statistics are updated..
 * @author Seamus McShane
 * @version $Revision: 1.0 $
 */
public class Simulation implements stateChangedInf, statChangedInf
{
	/* Simulation State */
	private SimulationState simState;
	private List<SimulationStateListenerInf> simStateListeners = new ArrayList<SimulationStateListenerInf>();
		
	private SimulationStats simStats;
	private List<SimulationStatListenerInf> simStatListeners = new ArrayList<SimulationStatListenerInf>();

	// Lock for the listeners
	private Semaphore listenersLock = new Semaphore(1, false);
	
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

	/* Simulation Update Thread */
	private Thread asyncUpdateThread;
	
	/* asyncUpdateThread exit condition */
	private boolean running = true;
	private boolean exited = false;

	/* Busy wait inter-step delay toggle */
	private boolean realtime = true;
	
	/* Simulation Id */
	private int simId = -1;
	
	public Simulation(int simId)
	{
		this.simId = simId;
		
		pause = new Semaphore(0, true); // Starts Paused
		
		simState = new SimulationState(this);	

		simStats = new SimulationStats(this);	

		setupThreads();

		createSimScenario(null);
	}

	/**
	 * Method createSim.
	 */
	public void createSimScenario(ScenarioInf scenario)
	{
		if(scenario!=null)
		{
			System.out.println("Assigning Sim Manager");
			
			simManager = scenario.getSimulationScenarioManager();
			
			// This is a special external end event based on the simulation step count.
			simManager.setScenarioStepCountEndEvent(simStats);
			
			System.out.println("Scenario Type : " + scenario.getScenarioType());

		}
		
		simStats.clearSimulationStats();
		simState.newState();
		
	}

	/**
	 * This method initiates the thread shutdown sequence in the Simulation Manager
	 */
	public void destroySim()
	{
		// Ensure we have the simulation in a state where it is not active.
		
		/*if ( simState.getState() == SimState.RUNNING)
		{
			System.out.println("Pausing... (state|"+simState.getState().toString()+")");

			pauseSim();
		}*/
		
		pause.release();
		
		// Exit the Async Thread
		running = false;
		
		System.out.println("Destroying...");
		
		// Get our current thread.
		Thread thisThread = Thread.currentThread();
		
		// We must until the simulation thread is gone.
		while(!exited)
		{
			try
			{
				// Go to sleep or our busy wait will cause problems
				thisThread.sleep(1);
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Destroyed");

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
		asyncUpdateThread = new Thread(new Runnable()
		{
			public void run()
			{
				Thread thisThread = Thread.currentThread();
				thisThread.setName("Async Update Thread");
				//Thread thisThread = Thread.currentThread();

				/* Top Priority to the simulation thread */
				//thisThread.setPriority(Thread.MAX_PRIORITY);
								
				while (running)
				{
					simUpdate();
					//asyncUpdateThread.yield();
				}
				
				exited = true;
			}
		}, "Simulation Update Thread"

		);

		asyncUpdateThread.start();

	}

	private void simUpdate()
	{
		// The pause semaphore (We do not pause half way through a step)
		pause.acquireUninterruptibly();
		
		if(running)
		{
			simStats.setStepStartTime();
	
			// record step start time for inter-step delay
			timeTotal();
	
			// This single method hides the rest of the sim
			simManager.doSimulationUpdate();
	
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
			
			// Increment the Step counter
			simStats.incrementSimulationSteps();
					
			// Check for an End Event
			if(simManager.hasEndEventOccurred())
			{
				System.out.println("Simulation End Occurrred");
				
				simState.finishState();
				
				// Effectively a dead lock under any other circumstance.
				pause.acquireUninterruptibly();
			}
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
	
	public SimState togglePause()
	{
		
		if(simState.getState() == SimState.RUNNING)
		{
			pauseSim();
		}
		else if(simState.getState() == SimState.PAUSED)
		{
			unPauseSim();
		}
		else
		{
			System.out.println("ATTEMPT to PAUSE Simulation in :" + simState.getState());
		}
		
		return simState.getState();
	}
	
	/**
	 * UnPauses the Sim.
	 */
	public void unPauseSim()
	{
		simState.runState();
				
		pause.release();					// Release the pause semaphore
		
	}

	/**
	 * Pauses the Sim
	 */
	public void pauseSim()
	{
		pause.acquireUninterruptibly();		// Pause the sim

		simState.pauseState();		
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
	public void drawSim(GUISimulationView simView,boolean viewRangeDrawing,boolean viewsDrawing)
	{
		if(simManager!=null)
		{
			simManager.drawSim(simView,viewRangeDrawing,viewsDrawing);
		}
	}
	
	public void addSimulationStateListener(SimulationStateListenerInf listener)
	{
		listenersLock.acquireUninterruptibly();
		
			simStateListeners.add(listener);
			
			listener.simulationStateChanged(simId, simState.getState());
		
		listenersLock.release();	
	}
	
	public void removeSimulationStateListener(SimulationStateListenerInf listener)
	{
		listenersLock.acquireUninterruptibly();
		
			simStateListeners.remove(listener);
		
		listenersLock.release();	
	}
	
	public void addSimulationStatListener(SimulationStatListenerInf listener)
	{
		listenersLock.acquireUninterruptibly();
		
			simStatListeners.add(listener);
		
		listenersLock.release();
	}

	public void removeSimulationStatListener(SimulationStatListenerInf listener)
	{
		listenersLock.acquireUninterruptibly();
		
			simStatListeners.remove(listener);
		
		listenersLock.release();
	}
	
	/*
	 * Call Backs from state + stat objects
	 * (non-Javadoc)
	 * @see alifeSim.Simulation.SimulationState.stateChangedInf#stateChanged(alifeSim.Simulation.SimulationState.SimState)
	 */
	
	@Override
	public void stateChanged(SimState state)
	{
		listenersLock.acquireUninterruptibly();

		for (SimulationStateListenerInf listener : simStateListeners)
	    {
	    	listener.simulationStateChanged(simId, state);
	    }
		
		listenersLock.release();
	}

	@Override
	public void statChanged(long time, int stepNo, int progress, int asps)
	{
		listenersLock.acquireUninterruptibly();

		for (SimulationStatListenerInf listener : simStatListeners)
	    {
	    	listener.simulationStatChanged(simId, time, stepNo,progress,asps);
	    }
		
		listenersLock.release();

	}

	public SimState getState()
	{
		return simState.getState();
	}
	
	public int getReqSps()
	{
		return reqSps;
	}
}