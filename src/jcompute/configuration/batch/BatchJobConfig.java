package jcompute.configuration.batch;

import javax.xml.bind.annotation.*;

import jcompute.configuration.JComputeConfiguration;
import jcompute.configuration.xml.Header;

@XmlRootElement(name = "Batch")
@XmlType(propOrder =
{
	"header", "config", "logging", "statistics"
})
public class BatchJobConfig extends JComputeConfiguration
{
	private ItemGenerationConfig config;
	
	private LogConfig logging;
	
	private StatsConfig statistics;
	
	public BatchJobConfig()
	{
		
	}
	
	public BatchJobConfig(Header header, ItemGenerationConfig config, LogConfig logging, StatsConfig statistics)
	{
		super(header);
		this.config = config;
		this.logging = logging;
		this.statistics = statistics;
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
	
	@XmlElement(name = "ItemGeneration")
	public void setConfig(ItemGenerationConfig config)
	{
		this.config = config;
	}
	
	public ItemGenerationConfig getConfig()
	{
		return config;
	}
	
	@XmlElement(name = "Logging")
	public void setLogging(LogConfig logging)
	{
		this.logging = logging;
	}
	
	public LogConfig getLogging()
	{
		return logging;
	}
	
	@XmlElement(name = "Statistics")	
	public void setStatistics(StatsConfig statistics)
	{
		this.statistics = statistics;
	}
	
	public StatsConfig getStatistics()
	{
		return statistics;
	}
}
