package jcompute.configuration.batch;

import javax.xml.bind.annotation.*;

import jcompute.configuration.JComputeConfiguration;
import jcompute.configuration.xml.Header;

@XmlRootElement(name = "Batch")
public class BatchJobConfig extends JComputeConfiguration
{
	private ScenarioFileConfig config;
	private LogConfig log;
	private StatsConfig stats;
	
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
}
