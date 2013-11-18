package alifeSim.Scenario;

import java.io.File;
import java.util.LinkedList;



import org.apache.commons.configuration.HierarchicalINIConfiguration;

import alifeSim.ChartPanels.PopulationPanel;
import alifeSim.ChartPanels.StatPanelAbs;
import alifeSim.Simulation.Simulation;


public class ScenarioCharts extends ScenarioVT
{
	private LinkedList<StatPanelAbs> charts;
	private Simulation sim;
	
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
		
		checkPopulationPanel(scenario);
	}
	
	private void checkPopulationPanel(HierarchicalINIConfiguration scenario)
	{
		String section = "Graphs";
		
		if(super.getStringValue(section,"Population") !=null)
		{
			if(super.getStringValue(section,"Population").equalsIgnoreCase("true"))
			{
				System.out.println("Population Chart Enabled");
				charts.add(new PopulationPanel(sim.getSimManager().getStatmanger()));
			}
			else
			{
				System.out.println("Population Chart Disabled");
			}
		}
		
	}
		
	public LinkedList<StatPanelAbs> getCharts()
	{
		return charts;
	}	
	
}
