package jCompute.Batch.LogFileProcessor.LogFormatProcessor;

public class ItemLogItem
{
	private int itemId;
	private int sampleId;
	private int[] coordsPos;
	private double[] coordsVals;
	private String hash;
	private int runTime;
	private String endEvent;
	private int stepCount;
	
	public ItemLogItem()
	{
		coordsPos = new int[2];
		coordsVals = new double[2];
	}
	
	public int getItemId()
	{
		return itemId;
	}
	
	public int getSampleId()
	{
		return sampleId;
	}
	
	public int[] getCoordsPos()
	{
		return coordsPos;
	}
	
	public double[] getCoordsVals()
	{
		return coordsVals;
	}
	
	public String getHash()
	{
		return hash;
	}
	
	public int getRunTime()
	{
		return runTime;
	}
	
	public String getEndEvent()
	{
		return endEvent;
	}
	
	public int getStepCount()
	{
		return stepCount;
	}
	
	public void setItemId(int itemId)
	{
		this.itemId = itemId;
	}
	
	public void setSampleId(int sampleId)
	{
		this.sampleId = sampleId;
	}
	
	public void setCoordsPos(int[] pos)
	{
		coordsPos[0] = pos[0];
		coordsPos[1] = pos[1];
	}
	
	public void setCoordsVals(double[] vals)
	{
		coordsVals[0] = vals[0];
		coordsVals[1] = vals[1];
	}
	
	public void setHash(String hash)
	{
		this.hash = hash;
	}
	
	public void setRunTime(int runTime)
	{
		this.runTime = runTime;
	}
	
	public void setEndEvent(String endEvent)
	{
		this.endEvent = endEvent;
	}
	
	public void setStepCount(int stepCount)
	{
		this.stepCount = stepCount;
	}
	
}
