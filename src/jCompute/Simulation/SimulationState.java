package jCompute.Simulation;

import jCompute.Simulation.SimulationState.SimState;

public class SimulationState
{
	/** Sim ref for call back */
	private Simulation sim;
	
	/** Simulation Running State */
	private SimState state = SimState.NEW;
	
	public SimulationState(Simulation sim)
	{		
		this.sim = sim;
	}
		
	public void newState()
	{
		state = SimState.NEW;
		
		simCallBack();
		
	}
	
	public void runState()
	{
		state = SimState.RUNNING;
		
		simCallBack();
	}
	
	public void pauseState()
	{
		state = SimState.PAUSED;
		
		simCallBack();
		
	}
	
	public void finishState()
	{
		state = SimState.FINISHED;
		
		simCallBack();
	}	
	
	private void simCallBack()
	{
		sim.stateChanged(state);
	}
	
	/** State Enum */
	public enum SimState
	{
		NEW ("New"),
		RUNNING ("Running"),
		PAUSED ("Paused"),
		FINISHED ("Finished");

	    private final String name;

	    private SimState(String name) 
	    {
	        this.name = name;
	    }

	    public String toString()
	    {
	       return name;
	    }
	};
	
	/** Interface for call back */
	public interface stateChangedInf
	{
		public void stateChanged(SimState state);
	}

	/**
	 * Only safe to use inside simulation class.
	 * @return
	 */
	public SimState getState()
	{
		return state;
	}
}
