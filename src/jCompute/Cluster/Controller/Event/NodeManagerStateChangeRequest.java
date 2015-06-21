package jCompute.Cluster.Controller.Event;

import jCompute.Cluster.Controller.NodeManager.NodeManagerState;

public class NodeManagerStateChangeRequest
{
	private int uid;
	private NodeManagerState state;
	
	public NodeManagerStateChangeRequest(int uid, NodeManagerState state)
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
