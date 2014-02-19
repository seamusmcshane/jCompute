package alifeSim.Simulation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class SimulationState
{
	/** Simulation Performance Indicators */
	private long stepStartTime = 0;
	private long stepEndTime = 0;
	private long stepTotalTime = 0; // Total Simulation run-time is the time taken per step for each step	
	
	/** Simulation Step Counter */
	private int simulationSteps;

	/** Simulation Running State */
	private SimStatus status = SimStatus.NEW;
	
	/** 
	 * A list of objects listening to state change events 
	 * Listeners are notified after a status change or
	 * after a simulation step has occurred.
	 * */
	private List<SimulationStateStatListenerInf> statListeners = new ArrayList<SimulationStateStatListenerInf>();
	private List<SimulationStateStatusListenerInf> statusListeners = new ArrayList<SimulationStateStatusListenerInf>();
	
	// To prevent concurrent access when a listener is added and a notification event has occured.
	private Semaphore listenersLock = new Semaphore(1, false);	
	
	public SimulationState()
	{		
		clearSimulationStats();
	}
		
	public void newState()
	{
		status = SimStatus.NEW;
		
		notifyStatusListeners();
		
		notifyStatListeners();
	}
	
	public void runState()
	{
		status = SimStatus.RUNNING;
		
		notifyStatusListeners();
		
		notifyStatListeners();
	}
	
	public void pauseState()
	{
		status = SimStatus.PAUSED;
		
		notifyStatusListeners();
		
		notifyStatListeners();
	}
	
	public void finishState()
	{
		status = SimStatus.FINISHED;
		
		notifyStatusListeners();
		
		notifyStatListeners();
	}	
	
	public SimStatus getStatus()
	{
		return status;
	}
		
	public int getSimulationSteps()
	{
		return simulationSteps;
	}

	public void incrementSimulationSteps()
	{
		simulationSteps++;
		
		notifyStatListeners();
	}
	
	public int getAverageStepRate()
	{
		if(simulationSteps<100)
		{
			return 0;
		}
		
		return (int)((float)simulationSteps/((float)stepTotalTime/1000f));
	}
	
	public void setStepStartTime()
	{
		stepStartTime = System.currentTimeMillis(); // Start time for the average step		
	}

	public void setStepEndTime()
	{
		stepEndTime = System.currentTimeMillis();

		stepTotalTime += stepEndTime - stepStartTime;	
	}
	
	public long getTotalTime()
	{
		return stepTotalTime;
	}
	
	public void clearSimulationStats()
	{
		stepTotalTime = 0;
		simulationSteps = 0;
		
		notifyStatListeners();
	}

	public void addStateStatListener(SimulationStateStatListenerInf listener)
	{
		listenersLock.acquireUninterruptibly();
	    	statListeners.add(listener);
    	listenersLock.release();
	}
	
	private void notifyStatListeners()
	{
		listenersLock.acquireUninterruptibly();
		    for (SimulationStateStatListenerInf listener : statListeners)
		    {
		    	listener.simulationStateStatChanged(stepTotalTime,simulationSteps,getAverageStepRate());
		    }
	   listenersLock.release();
	}
	
	public void addStateStatusListener(SimulationStateStatusListenerInf listener)
	{
		listenersLock.acquireUninterruptibly();
	    	statusListeners.add(listener);
    	listenersLock.release();
	}
	
	private void notifyStatusListeners()
	{
		listenersLock.acquireUninterruptibly();
	    for (SimulationStateStatusListenerInf listener : statusListeners)
	    {
	    	listener.simulationStateStatusChanged(status);
	    }
	    listenersLock.release();
	}

	
	/** State Enum */
	public enum SimStatus
	{
		NEW ("NEW"),
		RUNNING ("RUNNING"),
		PAUSED ("PAUSED"),
		FINISHED ("FINISHED");

	    private final String name;

	    private SimStatus(String name) 
	    {
	        this.name = name;
	    }

	    public String toString()
	    {
	       return name;
	    }
	};
	
}
