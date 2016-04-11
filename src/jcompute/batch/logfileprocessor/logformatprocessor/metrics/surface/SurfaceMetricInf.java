package jcompute.batch.logfileprocessor.logformatprocessor.metrics.surface;

public interface SurfaceMetricInf
{
	
	/*
	 * *****************************************************************************************************
	 * Processed Data
	 *****************************************************************************************************/
	
	public double[] getResult();
	
	public double getMin();
	
	public double getMax();
	
	public Type getType();
	
	/*
	 * *****************************************************************************************************
	 * Metric Types
	 *****************************************************************************************************/
	
	public enum Type
	{
		AVERAGE(0), STANDARD_DEVIATION(1), MAX(2);
		
		private final int value;
		
		private Type(int value)
		{
			this.value = value;
		}
		
		public int asInt()
		{
			return value;
		}
	}
}
