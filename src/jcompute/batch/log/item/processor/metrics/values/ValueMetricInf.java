package jcompute.batch.log.item.processor.metrics.values;

public interface ValueMetricInf
{
	/*
	 * *****************************************************************************************************
	 * Processed Data
	 *****************************************************************************************************/
	
	public double getResult();
	
	public Type getType();
	
	/*
	 * *****************************************************************************************************
	 * Metric Types
	 *****************************************************************************************************/
	
	public enum Type
	{
		VALUE_TOTAL
	}
}
