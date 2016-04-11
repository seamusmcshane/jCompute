package jcompute.batch;

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
	private ArrayList<Float> coordinatesValues;
	private int simId;

	private int cacheIndex;

	// Finished Info
	private String endEvent;
	private long stepCount;

	// Times
	private long computeTime;

	// Statistics
	private boolean statsEnabled;

	public BatchItem(int itemId, int sampleId, int batchId, String name, int cacheIndex, ArrayList<Integer> coordinates, ArrayList<Float> coordinatesValues,
	boolean statsEnabled)
	{
		this.itemId = itemId;
		this.sampleId = sampleId;
		this.batchId = batchId;
		this.name = name;
		this.cacheIndex = cacheIndex;
		this.coordinates = coordinates;
		this.coordinatesValues = coordinatesValues;
		this.statsEnabled = statsEnabled;
	}

	public ArrayList<Integer> getCoordinates()
	{
		return coordinates;
	}

	public ArrayList<Float> getCoordinatesValues()
	{
		return coordinatesValues;
	}

	public int getBatchId()
	{
		return batchId;
	}

	public int getCacheIndex()
	{
		return cacheIndex;
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

	public boolean hasStatsEnabled()
	{
		return statsEnabled;
	}
}
