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
	private List<SimulationStatListenerInf> statListeners = new ArrayList<SimulationStatListenerInf>();
	private List<SimulationStatusListenerInf> statusListeners = new ArrayList<SimulationStatusListenerInf>();
	
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
	}
	
	public void runState()
	{
		status = SimStatus.RUNNING;
		
		notifyStatusListeners();
	}
	
	public void pauseState()
	{
		status = SimStatus.PAUSED;
		
		notifyStatusListeners();
	}
	
	public void finishState()
	{
		status = SimStatus.FINISHED;
		
		notifyStatusListeners();
	}	
	
	public SimStatus getStatus()
	{
		return status;
	}
		
	public long getSimulationSteps()
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

	public void addStatListener(SimulationStatListenerInf listener)
	{
		listenersLock.acquireUninterruptibly();
	    	statListeners.add(listener);
    	listenersLock.release();
	}
	
	private void notifyStatListeners()
	{
		listenersLock.acquireUninterruptibly();
		    for (SimulationStatListenerInf listener : statListeners)
		    {
		    	listener.simulationStatChanged(stepTotalTime,simulationSteps,getAverageStepRate());
		    }
	   listenersLock.release();
	}
	
	public void addStatusListener(SimulationStatusListenerInf listener)
	{
		listenersLock.acquireUninterruptibly();
	    	statusListeners.add(listener);
    	listenersLock.release();
	}
	
	private void notifyStatusListeners()
	{
		listenersLock.acquireUninterruptibly();
	    for (SimulationStatusListenerInf listener : statusListeners)
	    {
	    	listener.simulationStatusChanged(status);
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
