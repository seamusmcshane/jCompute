package alifeSim.Batch;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BatchItem
{
	private int itemId;
	private int batchId;
	private String configText;
	
	private int simId;
	
	private String itemHash;
	
	public BatchItem(int itemId,int batchId,String configText)
	{
		this.itemId = itemId;
		this.batchId = batchId;
		this.configText = configText;
		
		String toHash = String.valueOf(itemId)+String.valueOf(batchId)+configText;
		
		try
		{
			byte[] data = MessageDigest.getInstance("SHA-256").digest(toHash.getBytes());
			
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
	
	public int getBatchId()
	{
		return batchId;
	}
	
	public String getItemHash()
	{
		return itemHash;
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
	
}
