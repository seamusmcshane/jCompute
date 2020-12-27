package jcompute.configuration.batch;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Config")
public class ScenarioFileConfig
{
	private String baseScenarioFileName;
	
	private int itemSamples;
	
	public ScenarioFileConfig()
	{
		
	}
	
	public ScenarioFileConfig(String baseScenarioFileName, int itemSamples)
	{
		this.baseScenarioFileName = baseScenarioFileName;
		this.itemSamples = itemSamples;
	}
	
	@XmlElement(name = "BaseScenarioFileName")
	public void setBaseScenarioFileName(String baseScenarioFileName)
	{
		this.baseScenarioFileName = baseScenarioFileName;
	}
	
	public String getBaseScenarioFileName()
	{
		return baseScenarioFileName;
	}
	
	@XmlElement(name = "ItemSamples")
	public void setItemSamples(int itemSamples)
	{
		this.itemSamples = itemSamples;
	}
	
	public int getItemSamples()
	{
		return itemSamples;
	}
}
