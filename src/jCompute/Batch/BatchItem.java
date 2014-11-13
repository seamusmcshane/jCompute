package jCompute.Batch;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

public class BatchItem
{
	private int itemId;
	private int batchId;
	private int sampleId;
	private String name;
	private String configText;
	
	// position within the combination space of this items combo.
	private ArrayList<Integer> coordinates;
	
	// Real Values
	private ArrayList<Integer> coordinatesValues;
	private int simId;
	
	private String itemHash;
	
	// Finished Info
	private String endEvent;
	private long stepCount;

	// Times
	private long cpuTime;
	private long netTime;
	
	public BatchItem(int sampleId,int itemId,int batchId,String name,String configText,ArrayList<Integer> coordinates,ArrayList<Integer> coordinatesValues)
	{
		this.sampleId = sampleId;
		this.itemId = itemId;
		this.batchId = batchId;
		this.name = name;
		this.configText = configText;
		
		this.coordinates = coordinates;
		this.coordinatesValues = coordinatesValues;
		
		String toHash = name+configText;
		
		try
		{
			byte[] data = MessageDigest.getInstance("MD5").digest(toHash.getBytes());
			
	        StringBuilder buffer = new StringBuilder();
	        for (int d = 0; d < data.length; d++) 
	        {
	        	buffer.append(Integer.toString((data[d] & 0xff) + 0x100, 16).substring(1));
	        }
	        
	        itemHash = buffer.toString();
			
		}
		catch (NoSuchAlgorithmException e)
		{
			this.itemHash = "MD5 not available";
		}
		
	}
	
	public ArrayList<Integer> getCoordinates()
	{
		return coordinates;
	}
	
	public ArrayList<Integer> getCoordinatesValues()
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
	
	public String getConfigText()
	{
		return configText;
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
	
	public void setComputeFinish(long cpuTime, String endEvent, long stepCount)
	{
		this.cpuTime = cpuTime;
		this.endEvent = endEvent;
		this.stepCount = stepCount;
	}

	public void setNetTime(long netTime)
	{
		this.netTime = netTime;
	}
	
	public long getCPUTime()
	{
		return cpuTime;
	}
	
	public long getNetTime()
	{
		return netTime;
	}
	
	public long getTotalTime()
	{
		return cpuTime+netTime;
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
