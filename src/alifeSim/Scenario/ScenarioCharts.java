package alifeSim.Scenario;

import java.io.File;
import java.util.LinkedList;



import org.apache.commons.configuration.HierarchicalINIConfiguration;

import alifeSim.ChartPanels.GlobalStatChartPanel;
import alifeSim.ChartPanels.StatPanelAbs;
import alifeSim.Simulation.Simulation;


public class ScenarioCharts extends ScenarioVT
{
	private LinkedList<StatPanelAbs> charts;
	private Simulation sim;
	private String validChartTypes[] = {"Population",	"Births/Deaths"};
	private boolean validTotalStats[] = {true		,	false};
	
	
	public ScenarioCharts(File file,Simulation sim)
	{
		super(file);
		init(super.scenario, sim);
	}
	
	public ScenarioCharts(String text,Simulation sim)
	{		
		super(text);
		init(super.scenario, sim);
	}

	private void init(HierarchicalINIConfiguration Scenario,Simulation sim)
	{
		this.sim = sim;
		charts = new LinkedList<StatPanelAbs>();
		
		checkChartPanels(scenario);
	}
	
	private void checkChartPanels(HierarchicalINIConfiguration scenario)
	{
		String section = "Graphs";
		int pos =0;
		
		for(String chartName : validChartTypes)
		{
			if(super.getStringValue(section,chartName) !=null)
			{				
				if(super.getStringValue(section,chartName).equalsIgnoreCase("true"))
				{
					System.out.println(chartName +" Chart Requested");
					charts.add(new GlobalStatChartPanel(chartName,sim.getSimManager().getStatmanger(),validTotalStats[pos]));
				}
				else
				{
					System.out.println(chartName + " Chart Disabled");
				}
			}
			pos ++;
		}

	}
		
	public LinkedList<StatPanelAbs> getCharts()
	{
		return charts;
	}	
	
}
