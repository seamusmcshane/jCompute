package alifeSim.Stats;

public class StatGroupSetting
{
	private String name;
	private boolean enabled;
	private boolean totalStat;
	private boolean graph;
	private int graphSampleRate;
	
	public StatGroupSetting(String name, boolean enabled,boolean totalStat, boolean graph, int graphSampleRate)
	{
		super();
		this.name = name;
		this.enabled = enabled;
		this.totalStat = totalStat;
		
		// Cannot enable graphs if stats not enabled
		if(enabled)
		{
			this.graph = graph;
		}
		else
		{
			this.graph = false;
		}
		
		
		this.graphSampleRate = graphSampleRate;
	}
	
	/**
	 * Standard Constructor
	 * Requires a name, but statistic collection and graphing is disabled
	 * @param name
	 */
	public StatGroupSetting(String name)
	{
		super();
		this.name = name;
		this.enabled = false;
		this.graph = enabled;
		this.graphSampleRate = 100;
	}
	
	public String getName()
	{
		return name;
	}

	public boolean statsEnabled()
	{
		return enabled;
	}

	public boolean graphEnabled()
	{
		return graph;
	}

	public int getGraphSampleRate()
	{
		return graphSampleRate;
	}
	
	public boolean hasTotalStat()
	{
		return totalStat;
	}
	
}
