package jcompute.cluster.controlnode.computenodemanager.event;

import jcompute.cluster.controlnode.NodeManagerStateMachine.NodeManagerState;

public class ComputeNodeManagerStateChangeRequest
{
	private int uid;
	private NodeManagerState state;
	
	public ComputeNodeManagerStateChangeRequest(int uid, NodeManagerState state)
	{
		this.uid = uid;
		this.state = state;
	}
	
	public NodeManagerState getState()
	{
		return state;
	}
	
	public int getUid()
	{
		return uid;
	}
}
