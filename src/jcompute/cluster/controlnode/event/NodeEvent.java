package jcompute.cluster.controlnode.event;

import jcompute.cluster.computenode.nodedetails.NodeInfo;

public class NodeEvent
{
	private NodeEventType eventType;
	private NodeInfo nodeConfig;
	
	public NodeEvent(NodeEventType eventType, NodeInfo nodeConfig)
	{
		this.eventType = eventType;
		this.nodeConfig = nodeConfig;
	}
	
	public NodeEventType getEventType()
	{
		return eventType;
	}
	
	public NodeInfo getNodeConfiguration()
	{
		return nodeConfig;
	}
	
	/** State Enum */
	public enum NodeEventType
	{
		CONNECTING("Connecting"), CONNECTED("Connected"), DISCONNECTED("Disconnected");
		
		private final String name;
		
		private NodeEventType(String name)
		{
			this.name = name;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
		
		public static NodeEventType fromInt(int v)
		{
			NodeEventType state = null;
			switch(v)
			{
				case 0:
					state = NodeEventType.CONNECTING;
				break;
				case 1:
					state = NodeEventType.CONNECTED;
				break;
				case 2:
					state = NodeEventType.DISCONNECTED;
				break;
				default:
					/* Invalid Usage */
					state = null;
			}
			
			return state;
		}
	}
}
