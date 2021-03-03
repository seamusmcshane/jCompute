package jcompute.configuration.batch;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.*;

import jcompute.batch.itemgenerator.Parameter;
import jcompute.configuration.JComputeConfiguration;
import jcompute.configuration.shared.Event;
import jcompute.configuration.xml.Header;

@XmlRootElement(name = "Batch")
@XmlType(propOrder =
{
	"header", "config", "log", "stats", "parameters"
})
public class BatchJobConfig extends JComputeConfiguration
{
	private ScenarioFileConfig config;
	
	private LogConfig log;
	
	private StatsConfig stats;
	
	private ParametersConfig parameters;
		
	public BatchJobConfig()
	{
		
	}
	
	public BatchJobConfig(Header header, ScenarioFileConfig config, LogConfig log, StatsConfig stats)
	{
		super(header);
		this.config = config;
		this.log = log;
		this.stats = stats;
	}
	
	/*@Override
	@XmlElement(name = "Header")
	public void setHeader(Header header)
	{
		super.setHeader(header);
	}
	
	@Override
	public Header getHeader()
	{
		return super.getHeader();
	}*/
	
	@XmlElement(name = "Config")
	public void setConfig(ScenarioFileConfig config)
	{
		this.config = config;
	}
	
	public ScenarioFileConfig getConfig()
	{
		return config;
	}
	
	@XmlElement(name = "Log")
	public void setLog(LogConfig log)
	{
		this.log = log;
	}
	
	public LogConfig getLog()
	{
		return log;
	}
	@XmlElement(name = "Stats")
	
	public void setStats(StatsConfig stats)
	{
		this.stats = stats;
	}
	
	public StatsConfig getStats()
	{
		return stats;
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
