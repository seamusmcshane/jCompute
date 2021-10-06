package jcompute.batch.itemgenerator;

public interface ItemGeneratorConfigInf
{
	public String getBaseScenarioText();
	
	public int getItemSamples();
	
	public int getTotalCombinations();
	
	public String[] getGroupName();
	
	public String[] getParameterName();
	
	public String getConfigSchema();
}
