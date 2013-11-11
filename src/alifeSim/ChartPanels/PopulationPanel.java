package alifeSim.ChartPanels;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.category.SlidingCategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import alifeSim.Stats.StatGroup;
import alifeSim.Stats.StatManager;

import java.awt.Color;
import java.awt.GridLayout;
import java.util.HashMap;

public class PopulationPanel extends StatPanelAbs
{
	private static final long serialVersionUID = -3572724823868862025L;
	
	private final String name = "Population";
	
	// Chart 1
	JFreeChart populationBarChart;
	ChartPanel populationBarChartPanel;
	DefaultCategoryDataset populationDataset;
	Plot populationChartPlot;
	
	// Chart 2
	JFreeChart populationAreaChart;
	ChartPanel populationAreaChartPanel;
	
	HashMap<String,XYSeries> map;
	
	XYSeries series1;
	XYSeries series2;
	XYSeries series3;
	XYSeriesCollection populationAreaDataset;
	Plot populationAreaChartPlot;
	
	StatGroup populationGroup;
	String groupName = "Population";
	String category = "Species";
	
	private int totalAdds;
	private int windowSize = 5000;
	
	public PopulationPanel(StatManager manager)
	{
		totalAdds = 0;
		
		map = new HashMap<String,XYSeries>();
		
		setLayout(new GridLayout(2, 1, 0, 0));
		
		populationDataset = new DefaultCategoryDataset();
				
		populationBarChart = ChartFactory.createBarChart3D("Species Populations", null, null, populationDataset, PlotOrientation.VERTICAL, true, false, false);
		

		
		populationChartPlot = populationBarChart.getCategoryPlot();
		
		populationBarChartPanel = new ChartPanel(populationBarChart);
		
		add(populationBarChartPanel);
		//simulationGraph.add(panel);*/	
		
		
		series1 =  new XYSeries("Plants");
		
		series1.setMaximumItemCount(windowSize);
		series2 =  new XYSeries("Predators");
		series2.setMaximumItemCount(windowSize);
		series3 =  new XYSeries("Prey");
		series3.setMaximumItemCount(windowSize);
		
		map.put("Plants", series1);
		map.put("Predators", series2);
		map.put("Prey", series3);

		populationAreaDataset = new XYSeriesCollection();
		populationAreaDataset.setAutoWidth(false);
				
		populationAreaDataset.addSeries(series1);
		populationAreaDataset.addSeries(series2);
		populationAreaDataset.addSeries(series3);
		
		//slidingPopulationAreaDataset = new SlidingXYDataset(populationAreaDataset, 1, 10);
		
		populationAreaChart = ChartFactory.createXYLineChart("Population Dynamic",  "Step", "Population",populationAreaDataset);
		
		//.populationAreaChartPlot = populationAreaChart.getCategoryPlot();
		
		populationAreaChartPanel = new ChartPanel(populationAreaChart);
		
		add(populationAreaChartPanel);
		
		populationGroup = manager.getStatGroup(groupName);
		
		populationBarChart.getCategoryPlot().getRenderer().setSeriesPaint(0, Color.GREEN);
		populationBarChart.getCategoryPlot().getRenderer().setSeriesItemLabelsVisible(0, true);
		populationAreaChart.getXYPlot().getRenderer().setSeriesPaint(0, Color.GREEN);
		
		populationBarChart.getCategoryPlot().getRenderer().setSeriesPaint(1, Color.RED);
		populationBarChart.getCategoryPlot().getRenderer().setSeriesItemLabelsVisible(1, true);
		populationAreaChart.getXYPlot().getRenderer().setSeriesPaint(1, Color.RED);

		populationBarChart.getCategoryPlot().getRenderer().setSeriesPaint(2, Color.BLUE);
		populationBarChart.getCategoryPlot().getRenderer().setSeriesItemLabelsVisible(2, true);
		populationAreaChart.getXYPlot().getRenderer().setSeriesPaint(2, Color.BLUE);
		
		populationBarChart.getCategoryPlot().getRenderer().setSeriesPaint(3, Color.yellow);
		populationBarChart.getCategoryPlot().getRenderer().setSeriesItemLabelsVisible(2, false);
		populationAreaChart.getXYPlot().getRenderer().setSeriesPaint(3, Color.yellow);

	}
	
	@Override
	public void destroy()
	{
		populationBarChart = null;
		populationBarChartPanel = null;
		populationDataset = null;
		populationChartPlot = null;
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
						
			map.get(statName).add(totalAdds, populationGroup.getStat(statName).getLastSample());		
		}
		totalAdds++;	
	}
	
	public String getName()
	{
		return name;
	}
	
}
