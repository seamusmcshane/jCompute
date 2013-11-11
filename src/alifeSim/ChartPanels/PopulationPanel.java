package alifeSim.ChartPanels;

import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import alifeSim.Simulation.SimulationManagerInf;
import alifeSim.Stats.StatGroup;
import alifeSim.Stats.StatManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Stroke;

public class PopulationPanel extends StatPanelAbs
{
	private static final long serialVersionUID = -3572724823868862025L;
	
	private final String name = "Population";
	
	// Chart 1
	JFreeChart populationBarChart;
	ChartPanel populationBarChartPanel;
	DefaultCategoryDataset populationDataset;
	Plot populationChartPlot;
	BarRenderer barRenderer;
	
	StatGroup populationGroup;
	String groupName = "Population";
	String category = "Species";
	
	public PopulationPanel(StatManager manager)
	{
		setLayout(new GridLayout(2, 1, 0, 0));
		
		populationDataset = new DefaultCategoryDataset();
				
		populationBarChart = ChartFactory.createBarChart3D("Species Populations", null, null, populationDataset, PlotOrientation.VERTICAL, true, false, false);
		
		populationBarChart.getCategoryPlot().getRenderer().setSeriesPaint(0, Color.GREEN);
		populationBarChart.getCategoryPlot().getRenderer().setSeriesItemLabelsVisible(0, false);

		populationBarChart.getCategoryPlot().getRenderer().setSeriesPaint(1, Color.BLUE);
		populationBarChart.getCategoryPlot().getRenderer().setSeriesItemLabelsVisible(1, false);

		populationBarChart.getCategoryPlot().getRenderer().setSeriesPaint(2, Color.RED);
		populationBarChart.getCategoryPlot().getRenderer().setSeriesItemLabelsVisible(2, false);
		
		populationBarChart.getCategoryPlot().getRenderer().setSeriesPaint(3, Color.yellow);
		populationBarChart.getCategoryPlot().getRenderer().setSeriesItemLabelsVisible(2, false);
		
		populationChartPlot = populationBarChart.getCategoryPlot();
		
		populationBarChartPanel = new ChartPanel(populationBarChart);
		
		add(populationBarChartPanel);
		//simulationGraph.add(panel);*/	
		
		populationGroup = manager.getStatGroup(groupName);
		
		// TEst Data
		/*populationDataset.setValue(8000, "Plants", "Species");	
		populationDataset.setValue(1024, "Prey", "Species");
		populationDataset.setValue(43, "Predator 1", "Species");
		populationDataset.setValue(43, "Predator 2", "Species");*/
	}
	
	@Override
	public void destroy()
	{
		populationBarChart = null;
		populationBarChartPanel = null;
		populationDataset = null;
		populationChartPlot = null;
		barRenderer = null;
		populationGroup = null;
		groupName = null;
		category = null;
	}
	
	@Override
	public void update()
	{
		for (String statName : populationGroup.getStatList()) 
		{
			populationDataset.setValue(populationGroup.getStat(statName).getLastSample(), statName, category);
		}
	}
	
	public String getName()
	{
		return name;
	}
	
}
