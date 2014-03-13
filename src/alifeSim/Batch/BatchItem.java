package alifeSim.Batch;

public class BatchItem
{
	private int id;
	private String configText;
	
	public BatchItem(int id,String configText)
	{
		this.configText = configText;
	}
	
	public String getConfigText()
	{
		return configText;
	}
	
	public int getId()
	{
		return id;
	}
}
