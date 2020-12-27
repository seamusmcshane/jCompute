package jcompute.configuration.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "Stat")
@XmlType(propOrder =
{
	"name", "enabled", "totalStat", "graph", "statSampleRate", "graphSampleWindow"
})
public class Stat
{
	private String name;
	private boolean enabled;
	private boolean totalStat;
	private boolean graph;
	private int statSampleRate;
	private int graphSampleWindow;
	
	public Stat()
	{
		
	}
	
	public Stat(String name, boolean enabled, boolean totalStat, boolean graph, int statSampleRate, int graphSampleWindow)
	{
		this.name = name;
		this.enabled = enabled;
		this.totalStat = totalStat;
		this.graph = graph;
		this.statSampleRate = statSampleRate;
		this.graphSampleWindow = graphSampleWindow;
	}
	
	@XmlElement(name = "Name")
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}
	
	@XmlElement(name = "Enabled")
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}
	
	public boolean isEnabled()
	{
		return enabled;
	}
	
	@XmlElement(name = "TotalStat")
	public void setTotalStat(boolean totalStat)
	{
		this.totalStat = totalStat;
	}
	
	public boolean isTotalStat()
	{
		return totalStat;
	}
	
	@XmlElement(name = "Graph")
	public void setGraph(boolean graph)
	{
		this.graph = graph;
	}
	
	public boolean isGraph()
	{
		return graph;
	}
	
	@XmlElement(name = "StatSampleRate")
	public void setStatSampleRate(int statSampleRate)
	{
		this.statSampleRate = statSampleRate;
	}
	
	public int getStatSampleRate()
	{
		return statSampleRate;
	}
	
	@XmlElement(name = "GraphSampleWindow")
	public void setGraphSampleWindow(int graphSampleWindow)
	{
		this.graphSampleWindow = graphSampleWindow;
	}
	
	public int getGraphSampleWindow()
	{
		return graphSampleWindow;
	}
}
