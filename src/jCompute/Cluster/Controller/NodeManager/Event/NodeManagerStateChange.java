package jCompute.Cluster.Controller.NodeManager.Event;

import jCompute.Cluster.Controller.NodeManager.NodeManager.NodeManagerState;

public class NodeManagerStateChange
{
	private int uid;
	private NodeManagerState state;
	
	public NodeManagerStateChange(int uid, NodeManagerState state)
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
