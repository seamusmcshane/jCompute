package jCompute.cluster.controlnode.computenodemanager.event;

import jCompute.cluster.controlnode.computenodemanager.ComputeNodeManager.NodeManagerState;

public class ComputeNodeManagerStateChange
{
	private int uid;
	private NodeManagerState state;
	
	public ComputeNodeManagerStateChange(int uid, NodeManagerState state)
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
