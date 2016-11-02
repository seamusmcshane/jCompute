package jcompute.results.custom;

public enum CustomResultFieldType
{
	Unsupported(0), Integer(1), Double(2), Long(3), Float(4), String(5), Boolean(6);
	
	public final int index;
	
	private CustomResultFieldType(int index)
	{
		this.index = index;
	}
	
	public static CustomResultFieldType fromInt(int index)
	{
		switch(index)
		{
			case 1:
			{
				return Integer;
			}
			case 2:
			{
				return Double;
			}
			case 3:
			{
				return Long;
			}
			case 4:
			{
				return Float;
			}
			case 5:
			{
				return String;
			}
			case 6:
			{
				return Boolean;
			}
		}
		
		return Unsupported;
	}
}
