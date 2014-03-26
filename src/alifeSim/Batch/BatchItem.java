package alifeSim.Batch;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedList;

import com.badlogic.gdx.scenes.scene2d.ui.List;

public class BatchItem
{
	private int itemId;
	private int batchId;
	private int sampleId;
	private String name;
	private String configText;
	
	// position within the combination space of this items combo.
	private ArrayList<Integer> coordinates;
	private int simId;
	
	private String itemHash;
	
	public BatchItem(int sampleId,int itemId,int batchId,String name,String configText,ArrayList<Integer> coordinates,ArrayList<Integer> coordinatesValues)
	public BatchItem(int itemId,int batchId,String name,String configText,ArrayList<Integer> coordinates)
	{
		this.sampleId = sampleId;
		this.itemId = itemId;
		this.batchId = batchId;
		this.name = name;
		this.configText = configText;
		
		this.coordinates = coordinates;
		
		String toHash = name+configText;
		
		try
		{
			byte[] data = MessageDigest.getInstance("MD5").digest(toHash.getBytes());
			
	        StringBuffer buffer = new StringBuffer();
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
	
}
