package jcompute.configuration.batch;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessOrder;
import javax.xml.bind.annotation.XmlAccessorOrder;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import jcompute.configuration.shared.Event;

@XmlRootElement(name = "Config")
@XmlAccessorOrder(XmlAccessOrder.UNDEFINED)
public class ScenarioFileConfig
{
	private String baseScenarioFileName;
	
	private int itemSamples;
	
	// End Events
	@XmlElementWrapper(name = "EndEvents", required = true) /** Avoids needing to use the EndEvents class */
	@XmlElement(type = Event.class, name = "Event")
	private List<Event> endEvents;
	
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
	
	@XmlTransient
	public void setEndEventsList(List<Event> endEvents)
	{
		this.endEvents = endEvents;
	}
	
	public List<Event> getEndEventsList()
	{
		return endEvents;
	}
}
