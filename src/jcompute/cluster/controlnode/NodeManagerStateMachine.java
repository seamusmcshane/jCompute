package jcompute.cluster.controlnode;

import jcompute.JComputeEventBus;

public class NodeManagerStateMachine
{
	private int nodeId;
	private NodeManagerState state;
	
	public NodeManagerStateMachine(int nodeId)
	{
		state = NodeManagerState.STARTING;
		
		this.nodeId = nodeId;
	}
	
	/**
	 * Will validate and perform the transition from the current state to a target state.
	 * 
	 * @param targetState
	 * @return
	 * If the transition is valid the resulting state will be the target.
	 * Otherwise the resulting state will be the shutdown state as an invalid transition was requested.
	 */
	public synchronized void transitionToState(NodeManagerState targetState)
	{
		NodeManagerState[] validStates = null;
		
		switch(state)
		{
			case STARTING:
				validStates = new NodeManagerState[]
				{
					NodeManagerState.RUNNING
				};
			break;
			case RUNNING:
				validStates = new NodeManagerState[]
				{
					NodeManagerState.PAUSING
				};
			break;
			case PAUSING:
				validStates = new NodeManagerState[]
				{
					NodeManagerState.PAUSED
				};
			break;
			case PAUSED:
				// Now that the State is PAUSED it can switch to RUNNING or proceed to SHUTDOWN.
				validStates = new NodeManagerState[]
				{
					NodeManagerState.RUNNING, NodeManagerState.SHUTTINGDOWN
				};
			break;
			default:
				validStates = new NodeManagerState[]{};
			break;
		}
		
		// Check Valid Trans
		if(containsState(validStates, targetState))
		{
			state = targetState;
		}
		else
		{
			state = NodeManagerState.SHUTDOWN;
		}
		
		// Post our state change event
		JComputeEventBus.post(new NodeManagerStateMachineEvent(nodeId, state.getNumber()));
	}
	
	public synchronized NodeManagerState getState()
	{
		return state;
	}
	
	private boolean containsState(NodeManagerState[] states, NodeManagerState target)
	{
		for(NodeManagerState state : states)
		{
			if(state == target)
			{
				return true;
			}
		}
		
		return false;
	}
	
	/** State */
	public enum NodeManagerState
	{
		// States
		STARTING(0, "Starting"), RUNNING(1, "Running"), PAUSING(2, "Pausing"), PAUSED(3, "Paused"), SHUTTINGDOWN(4, "Shutting Down"), SHUTDOWN(5, "Shutdown");
		
		private final int number;
		private final String name;
		
		private NodeManagerState(int number, String name)
		{
			this.number = number;
			this.name = name;
		}
		
		public int getNumber()
		{
			return number;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
		
		public static NodeManagerState getStateFromNumber(int number)
		{
			for(NodeManagerState state : NodeManagerState.values())
			{
				if(state.getNumber() == number)
				{
					return state;
				}
			}
			
			// Not a valid state number
			return null;
		}
	}
	
	// State change event class
	public class NodeManagerStateMachineEvent
	{
		private int uid;
		private int stateNum;
		
		public NodeManagerStateMachineEvent(int uid, int stateNum)
		{
			this.uid = uid;
			this.stateNum = stateNum;
		}
		
		public int getStateNum()
		{
			return stateNum;
		}
		
		public int getUid()
		{
			return uid;
		}
	}
}
