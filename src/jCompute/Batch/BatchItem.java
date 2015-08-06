package jCompute.Batch;

import java.util.ArrayList;

public class BatchItem
{
	private int itemId;
	private int batchId;
	private int sampleId;
	private String name;
	
	// position within the combination space of this items combo.
	private ArrayList<Integer> coordinates;
	
	// Real Values
	private ArrayList<Double> coordinatesValues;
	private int simId;
	
	private String itemHash;
	
	// Finished Info
	private String endEvent;
	private long stepCount;
	
	// Times
	private long computeTime;
	
	public BatchItem(int sampleId, int itemId, int batchId, String name, String hash, ArrayList<Integer> coordinates,
			ArrayList<Double> coordinatesValues)
	{
		this.sampleId = sampleId;
		this.itemId = itemId;
		this.batchId = batchId;
		this.name = name;
		this.itemHash = hash;
		this.coordinates = coordinates;
		this.coordinatesValues = coordinatesValues;
	}
	
	public ArrayList<Integer> getCoordinates()
	{
		return coordinates;
	}
	
	public ArrayList<Double> getCoordinatesValues()
	{
		return coordinatesValues;
	}
	
	public int getBatchId()
	{
		return batchId;
	}
	
	public String getItemHash()
	{
		return itemHash;
	}
	
	public String getItemName()
	{
		return name;
	}
	
	public int getItemId()
	{
		return itemId;
	}
	
	public void setSimId(int simId)
	{
		this.simId = simId;
	}
	
	public int getSimId()
	{
		return simId;
	}
	
	public int getSampleId()
	{
		return sampleId;
	}
	
	public void setComputeTime(long computeTime, String endEvent, long stepCount)
	{
		this.computeTime = computeTime;
		this.endEvent = endEvent;
		this.stepCount = stepCount;
	}
	
	public long getComputeTime()
	{
		return computeTime;
	}
	
	public String getEndEvent()
	{
		return endEvent;
	}
	
	public long getStepCount()
	{
		return stepCount;
	}
}
