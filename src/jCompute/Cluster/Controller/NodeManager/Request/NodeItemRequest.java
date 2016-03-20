package jCompute.Cluster.Controller.NodeManager.Request;

import jCompute.Cluster.Controller.Mapping.RemoteSimulationMapping;

public class NodeItemRequest
{
	private RemoteSimulationMapping mapping;
	private NodeItemRequestOperation operation;
	private NodeItemRequestResult result;
	
	public NodeItemRequest(RemoteSimulationMapping mapping, NodeItemRequestOperation operation)
	{
		this.mapping = mapping;
		this.operation = operation;
	}

	public void setResult(NodeItemRequestResult result)
	{
		this.result = result;
	}
	
	public RemoteSimulationMapping getMapping()
	{
		return mapping;
	}

	public NodeItemRequestOperation getOperation()
	{
		return operation;
	}

	public NodeItemRequestResult getResult()
	{
		return result;
	}

	/** Result Enum */
	public enum NodeItemRequestResult
	{
		SUCESSFUL("Sucessful"), FAILED("Failed");
		
		private final String name;
		
		private NodeItemRequestResult(String name)
		{
			this.name = name;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
		
		public static NodeItemRequestResult fromInt(int v)
		{
			NodeItemRequestResult state = null;
			switch(v)
			{
				case 0:
					state = NodeItemRequestResult.SUCESSFUL;
				break;
				case 1:
					state = NodeItemRequestResult.FAILED;
				break;
				default:
					/* Invalid Usage */
					state = null;
			}
			
			return state;
		}
	}
	
	/** Operation Enum */
	public enum NodeItemRequestOperation
	{
		ADD("Add"), REMOVE("Remove");
		
		private final String name;
		
		private NodeItemRequestOperation(String name)
		{
			this.name = name;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
		
		public static NodeItemRequestOperation fromInt(int v)
		{
			NodeItemRequestOperation op = null;
			switch(v)
			{
				case 0:
					op = NodeItemRequestOperation.ADD;
				break;
				case 1:
					op = NodeItemRequestOperation.REMOVE;
				break;
				default:
					/* Invalid Usage */
					op = null;
			}
			
			return op;
		}
	}
}
