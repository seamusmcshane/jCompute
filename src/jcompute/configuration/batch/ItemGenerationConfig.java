package jcompute.configuration.batch;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import jcompute.batch.itemgenerator.Parameter;
import jcompute.configuration.shared.Event;

@XmlRootElement(name = "ItemGeneration")
@XmlType(propOrder =
{
	"baseScenarioFileName", "samples", "endEvents", "parameters"
})
public class ItemGenerationConfig
{
	private String baseScenarioFileName;
	
	private int samples;
	
	// End Events
	@XmlElementWrapper(name = "EndEvents", required = true) /** Avoids needing to use the EndEvents class */
	@XmlElement(type = Event.class, name = "Event")
	private List<Event> endEvents;
	
	private ParametersConfig parameters;
	
	public ItemGenerationConfig()
	{
		
	}
	
	public ItemGenerationConfig(String baseScenarioFileName, int samples)
	{
		this.baseScenarioFileName = baseScenarioFileName;
		this.samples = samples;
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
	
	@XmlElement(name = "Samples")
	public void setSamples(int samples)
	{
		this.samples = samples;
	}
	
	public int getSamples()
	{
		return samples;
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
	
	@XmlElement(name = "Parameters")
	public void setParameters(ParametersConfig parameters)
	{
		this.parameters = parameters;
	}
	
	public ParametersConfig getParameters()
	{
		return parameters;
	}
	
	public List<Parameter> getParameterList()
	{
		List<Parameter> parametersList = new ArrayList<Parameter>();
		
		// The config list
		List<ParameterConfig> parametersConfigList = parameters.getParameterList();
		
		for(ParameterConfig parameterConfig : parametersConfigList)
		{
			Parameter p = new Parameter(parameterConfig.getType(), parameterConfig.getPath(), parameterConfig
			.getGroupName(), parameterConfig.getParameterName(), parameterConfig.getInitialVal(), parameterConfig
			.getValIncrement(), parameterConfig.getValCombinations());
			
			parametersList.add(p);
		}
		
		return parametersList;
	}
}
