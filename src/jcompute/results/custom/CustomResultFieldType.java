package jcompute.results.custom;

public enum CustomResultFieldType
{
	Unsupported(0), Integer(1), Double(2), Long(3), Float(4), String(5);
	
	public final int index;
	
	private CustomResultFieldType(int index)
	{
		this.index = index;
	}
	
	public static CustomResultFieldType getFieldType(Object obj)
	{
		if(obj instanceof Integer)
		{
			return Integer;
		}
		else if(obj instanceof Long)
		{
			return Long;
		}
		else if(obj instanceof Float)
		{
			return Float;
		}
		else if(obj instanceof Double)
		{
			return Double;
		}
		else if(obj instanceof String)
		{
			return String;
		}
		else
		{
			// Do nothing we do not know how to handle this
			return Unsupported;
		}
	}
}
