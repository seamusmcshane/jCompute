package jCompute.Cluster.Controller.ControlNode.Request;

import jCompute.batch.BatchItem;

public class ControlNodeItemRequest
{
	private BatchItem batchItem;
	private ControlNodeItemRequestOperation operation;
	private ControlNodeItemRequestResult result;
	
	public ControlNodeItemRequest(BatchItem batchItem, ControlNodeItemRequestOperation operation, ControlNodeItemRequestResult result)
	{
		this.batchItem = batchItem;
		this.operation = operation;
		this.result = result;
	}

	public BatchItem getBatchItem()
	{
		return batchItem;
	}

	public ControlNodeItemRequestOperation getOperation()
	{
		return operation;
	}

	public ControlNodeItemRequestResult getResult()
	{
		return result;
	}

	/** Result Enum */
	public enum ControlNodeItemRequestResult
	{
		SUCESSFUL("Sucessful"), FAILED("Failed");
		
		private final String name;
		
		private ControlNodeItemRequestResult(String name)
		{
			this.name = name;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
		
		public static ControlNodeItemRequestResult fromInt(int v)
		{
			ControlNodeItemRequestResult state = null;
			switch(v)
			{
				case 0:
					state = ControlNodeItemRequestResult.SUCESSFUL;
				break;
				case 1:
					state = ControlNodeItemRequestResult.FAILED;
				break;
				default:
					/* Invalid Usage */
					state = null;
			}
			
			return state;
		}
	}
	
	/** Operation Enum */
	public enum ControlNodeItemRequestOperation
	{
		ADD("Add"), REMOVE("Remove");
		
		private final String name;
		
		private ControlNodeItemRequestOperation(String name)
		{
			this.name = name;
		}
		
		@Override
		public String toString()
		{
			return name;
		}
		
		public static ControlNodeItemRequestOperation fromInt(int v)
		{
			ControlNodeItemRequestOperation op = null;
			switch(v)
			{
				case 0:
					op = ControlNodeItemRequestOperation.ADD;
				break;
				case 1:
					op = ControlNodeItemRequestOperation.REMOVE;
				break;
				default:
					/* Invalid Usage */
					op = null;
			}
			
			return op;
		}
	}
	
}
