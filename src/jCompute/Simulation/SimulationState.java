package jCompute.Simulation;

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
	}
	
	public void runState()
	{
		state = SimState.RUNNING;
		
		simCallBack("None");
	}
	
	public void pauseState()
	{
		state = SimState.PAUSED;
		
		simCallBack("None");
		
	}
	
	public void finishState(String endEvent)
	{
		state = SimState.FINISHED;
		
		simCallBack(endEvent);
	}
	
	private void simCallBack(String endEvent)
	{
		sim.stateChanged(state, endEvent);
	}
	
	/** State Enum */
	public enum SimState
	{
		NEW("New"), RUNNING("Running"), PAUSED("Paused"), FINISHED("Finished");
		
		private final String name;
		
		private SimState(String name)
		{
			this.name = name;
		}
		
		public String toString()
		{
			return name;
		}
		
		public static SimState fromInt(int v)
		{
			SimState state = null;
			switch(v)
			{
				case 0:
					state = SimState.NEW;
				break;
				case 1:
					state = SimState.RUNNING;
				break;
				case 2:
					state = SimState.PAUSED;
				break;
				case 3:
					state = SimState.FINISHED;
				break;
				default:
					/* Invalid Usage */
					state = null;
			}
			
			return state;
		}
	};
	
	/** Interface for call back */
	public interface stateChangedInf
	{
		public void stateChanged(SimState state, String endEvent);
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
