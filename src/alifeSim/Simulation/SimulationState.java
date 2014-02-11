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
	private long simulationSteps;

	/** Simulation Running State */
	private SimStatus status = SimStatus.NEW;
	
	/** 
	 * A list of objects listening to state change events 
	 * Listeners are notified after a status change or
	 * after a simulation step has occurred.
	 * */
	private List<SimulationStateListenerInf> listeners = new ArrayList<SimulationStateListenerInf>();
	
	// To prevent concurrent access when a listener is added and a notification event has occured.
	private Semaphore listenersLock = new Semaphore(1, false);	
	
	public SimulationState()
	{		
		clearSimulationStats();
	}
		
	public void run()
	{
		status = SimStatus.RUNNING;
		
		notifyStateListeners();
	}
	
	public void pause()
	{
		status = SimStatus.PAUSED;
		
		notifyStateListeners();
	}
	
	public void finish()
	{
		status = SimStatus.FINISHED;
		
		notifyStateListeners();
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
		
		notifyStateListeners();
	}
	
	public int getAverageStepRate()
	{
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
		
		notifyStateListeners();
	}

	public void addListener(SimulationStateListenerInf listener)
	{
		listenersLock.acquireUninterruptibly();
	    	listeners.add(listener);
    	listenersLock.release();
	}
	
	private void notifyStateListeners()
	{
		listenersLock.acquireUninterruptibly();
		    for (SimulationStateListenerInf listener : listeners)
		    {
		    	listener.simulationStateChanged(status,stepTotalTime,simulationSteps,getAverageStepRate());
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
