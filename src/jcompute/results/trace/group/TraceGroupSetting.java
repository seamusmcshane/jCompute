package jcompute.results.trace.group;

public class TraceGroupSetting
{
	private String name;
	private boolean enabled;
	private boolean totalStat;
	private boolean graph;
	private int statSampleRate;
	private int graphSampleWindow;
	
	public TraceGroupSetting(String name, boolean enabled,boolean totalStat, boolean graph, int statSampleRate, int graphSampleWindow)
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
		
		
		this.statSampleRate = statSampleRate;

		this.graphSampleWindow = graphSampleWindow;
	}
	
	/**
	 * Standard Constructor
	 * Requires a name, but statistic collection and graphing is disabled
	 * @param name
	 */
	public TraceGroupSetting(String name)
	{
		super();
		this.name = name;
		this.enabled = false;
		this.graph = enabled;
		this.statSampleRate = 100;
		this.graphSampleWindow = 100;
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

	public int getStatSampleRate()
	{
		return statSampleRate;
	}
	
	public boolean hasTotalStat()
	{
		return totalStat;
	}

	public int getGraphSampleWindow()
	{
		return graphSampleWindow;
	}
	
}
