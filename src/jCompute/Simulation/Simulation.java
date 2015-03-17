package jCompute.Simulation;

import jCompute.JComputeEventBus;
import jCompute.Gui.View.ViewRendererInf;
import jCompute.Gui.View.ViewTarget;
import jCompute.Scenario.ScenarioInf;
import jCompute.Simulation.Event.SimulationStatChangedEvent;
import jCompute.Simulation.Event.SimulationStateChangedEvent;
import jCompute.Simulation.SimulationState.SimState;
import jCompute.Simulation.SimulationState.stateChangedInf;
import jCompute.Simulation.SimulationStats.statChangedInf;
import jCompute.Stats.StatManager;

import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simulation class
 * This class handles the Simulation activity of the program.
 * It contains the main loop in the form of the asynchronous Simulation Update Thread.
 * Apart from performing a step, it is primarily concerned with timing the step and ensuring statistics are updated..
 * @author Seamus McShane
 * @version $Revision: 1.0 $
 */
public class Simulation implements stateChangedInf, statChangedInf, ViewTarget
{
	// SL4J Logger
	private static Logger log = LoggerFactory.getLogger(Simulation.class);
	
	/* Simulation State */
	private SimulationState simState;
		
	private SimulationStats simStats;
	
	// Inter-step delay Calculations
	private long stepTimeNow;
	private long stepTimeDiff;
	private long stepTimeTotal;
	private long stepTimePrev;

	/* The Simulation manager */
	private SimulationScenarioManagerInf simManager;

	/* The default simulation update rate */
	private int tReqSps = 15;

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
			log.info("Assigning Sim Manager");
			
			simManager = scenario.getSimulationScenarioManager();
			
			// This is a special external end event based on the simulation step count.
			simManager.setScenarioStepCountEndEvent(simStats);
			
			log.info("Scenario Type : " + scenario.getScenarioType());
		}
		
		simStats.clearSimulationStats();
		simState.newState();		
	}

	/**
	 * This method initiates the thread shutdown sequence in the Simulation Manager
	 */
	public void destroySim()
	{		
		// Exit the Async Thread
		running = false;
		
		pause.release();
		
		log.info("Destroying Simulation : " + simId);
		
		// We must until the simulation thread is gone.
		while(!exited)
		{
			Thread.yield();
		}
		log.info("Destroyed");

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
				thisThread.setName("Simulation " + simId);
				
				int reqSps = 0;
				
				while (running)
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
							reqSps = tReqSps;
							
							// Calculate how much we need to wait (in nanoseconds, based on the time taken so far) before proceeding to the next step 
							while (timeTotal() < (1000000000 / reqSps)) // Approximation of what the inter-step delay should be
							{
								// Inter-Step Busy wait delay (66ms~ for 15 steps per second)
								// This will only wait if the step performance level is being exceeded
								// Waiting between steps ensures smooth animiation on the view
								// Yield as we do not want max performance.
								Thread.yield();
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
							simState.finishState(simStats,simManager.getEndEvent());
							
							// Effectively a dead lock under any other circumstance.
							pause.acquireUninterruptibly();
						}
					}
					
					// Allow the simulation to be paused again
					pause.release();	
				}
				
				
				exited = true;
			}
		}, "Simulation Update Thread"

		);

		asyncUpdateThread.start();
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
	 *  If Simulation is paused, this will unpause it.
	 *  If Simulation is running this will pause it.
	 *  If Simulation is CON this method does nothing.
	 *  This method will log an error if called when a Simulation is finished.
	 */	
	public SimState togglePause()
	{
		SimState state = simState.getState();
		
		// Only toggle in the running or paused states (not CON or Finished)
		switch(state)
		{
			case RUNNING:
				pauseSim();
			break;
			case PAUSED:
				unPauseSim();
			break;
			case NEW:
				// This is OK, some methods can call this method during all states, we don't change the pause state during CON.
			break;
			case FINISHED:
				// This is a usage error - cannot toggle pause when finished.
				log.error("Attempt to toggle pause in state " + state.toString());
			break;
			default:
				// This is an error - this switch statement updated.
				log.error("Attempt to toggle pause in unknown state " + state.toString());
		}

		// Return the CON sim state
		return simState.getState();
	}
	
	/**
	 * UnPauses the Sim.
	 */
	public void unPauseSim()
	{
		simState.runState(simStats);
				
		pause.release();					// Release the pause semaphore		
	}

	/**
	 * Pauses the Sim
	 */
	public void pauseSim()
	{
		pause.acquireUninterruptibly();		// Pause the sim

		simState.pauseState(simStats);		
	}

	/**
	 * Method reqSimUpdateRate.
	 * @param steps int
	 */
	public void setReqStepRate(int steps)
	{		
		if (steps > 0)
		{
			tReqSps = steps;
			realtime=true;	
		}
		else
		{
			realtime=false;
			tReqSps = 0;
		}
	}
		
	/*
	 * Call Backs from state + stat objects
	 * (non-Javadoc)
	 * @see alifeSim.Simulation.SimulationState.stateChangedInf#stateChanged(alifeSim.Simulation.SimulationState.SimState)
	 */
	
	@Override
	public void stateChanged(SimState state,SimulationStats simStats, String endEvent)
	{
		log.debug("StateChanged " + simId + " " + state.toString());
		
		JComputeEventBus.post(new SimulationStateChangedEvent(simId,state,simStats.getTotalTime(),simStats.getSimulationSteps(),endEvent));
	}

	@Override
	public void statChanged(long time, int stepNo, int progress, int asps)
	{
		JComputeEventBus.post(new SimulationStatChangedEvent(simId,time, stepNo, progress,  asps));
	}

	public long getTotalTime()
	{
		return simStats.getTotalTime();
	}
	
	public SimState getState()
	{
		return simState.getState();
	}
	
	public int getReqSps()
	{
		return tReqSps;
	}

	public long getTotalSteps()
	{
		return simStats.getSimulationSteps();
	}
	
	public String getScenarioText()
	{
		if(simManager!=null)
		{
			return simManager.getScenario().getScenarioText();
		}
		return "No Scenario Text Loaded";
	}
	
	public String getInfo()
	{
		String simInfo = "Simulation : " + simId;
		
		if(simManager!=null)
		{
			simInfo = simInfo + " " + simManager.getInfo();
		}
		
		return simInfo ;
	}

	public StatManager getStatmanger()
	{
		return simManager.getStatmanger();
	}

	@Override
	public ViewRendererInf getRenderer()
	{
		return simManager.getRenderer();
	}
}