package alifeSim.Gui.Charts;

import alifeSim.Stats.SingleStat;
import alifeSim.Stats.StatGroupListenerInf;

import java.awt.BasicStroke;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.awt.GridLayout;
import javax.swing.border.TitledBorder;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import javax.swing.JPanel;
import javax.swing.UIManager;

public class GlobalStatChartPanel extends JPanel implements StatGroupListenerInf
{
	private static final long serialVersionUID = -3572724823868862025L;
	
	private String statChartPanelName = "No Panel Name";
	
	private String totalStatName = "NOTSET";
	private boolean totalStatEnabled = false;
	
	private String category = "Species";	// Assume for now global = global per species
	private int series = 0;					// Count of number of series
	
	private JFreeChart statBarChart;
	private ChartPanel statBarChartPanel;
	private DefaultCategoryDataset statDataset;
	
	private JFreeChart historyChart;
	private ChartPanel historyChartPanel;
	private XYSeriesCollection dataset;
	
	private HashMap<String,XYSeries> seriesMap;

	private int sampleWindow;
			
	public GlobalStatChartPanel(String statChartPanelName ,boolean totalStatEnabled, int sampleWindow)
	{
		// This panels name
		this.statChartPanelName = statChartPanelName;
		
		// Source Stat Group		
		this.totalStatEnabled = totalStatEnabled;
		
		if(totalStatEnabled)
		{
			this.totalStatName = "Total "+statChartPanelName;
		}
		
		this.sampleWindow = sampleWindow;
		
		System.out.println(statChartPanelName + " Chart Panel Created");

		setLayout(new GridLayout(2, 1, 0, 0));
		seriesMap = new HashMap<String,XYSeries>();

		createHistoryChart();
		createBarChart();
		if(totalStatEnabled)
		{
			series++;
		}
	}
	
	private void createBarChart()
	{
		statDataset = new DefaultCategoryDataset();
		statBarChart = ChartFactory.createBarChart(null, null, null, statDataset, PlotOrientation.VERTICAL, true, false, false);
		statBarChartPanel = new org.jfree.chart.ChartPanel(statBarChart);
		statBarChartPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Current", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		
		statBarChart.getCategoryPlot().setBackgroundPaint(Color.white);
		statBarChart.getCategoryPlot().setRangeGridlinePaint(Color.LIGHT_GRAY);
		statBarChart.getCategoryPlot().setDomainGridlinePaint(Color.LIGHT_GRAY);

		statBarChart.getCategoryPlot().getDomainAxis().setLowerMargin(0);
		statBarChart.getCategoryPlot().getDomainAxis().setUpperMargin(0);
		statBarChart.getCategoryPlot().getDomainAxis().setCategoryMargin(0);
		((BarRenderer)statBarChart.getCategoryPlot().getRenderer()).setItemMargin(0);		
		((BarRenderer)statBarChart.getCategoryPlot().getRenderer()).setBarPainter(new StandardBarPainter());	
		((BarRenderer)statBarChart.getCategoryPlot().getRenderer()).setDrawBarOutline(true);
				
		if(totalStatEnabled)
		{
			statDataset.setValue(0, totalStatName, category);
			statBarChart.getCategoryPlot().getRenderer().setSeriesPaint(series,Color.black);
			statBarChart.getCategoryPlot().getRenderer().setSeriesOutlinePaint(series, Color.black);
		}
		
		add(statBarChartPanel);
	}

	private void createHistoryChart() 
    {
        dataset = new XYSeriesCollection();
        
        historyChart = ChartFactory.createXYLineChart(null, null, null , dataset, PlotOrientation.VERTICAL, true, false, false);

        
        historyChart.getLegend().setWidth(10);
        historyChart.getXYPlot().setBackgroundPaint(Color.white);
        historyChart.getXYPlot().setRangeGridlinePaint(Color.LIGHT_GRAY);
        historyChart.getXYPlot().setDomainGridlinePaint(Color.LIGHT_GRAY);
        historyChart.getXYPlot().getDomainAxis().setLowerMargin(0);
        historyChart.getXYPlot().getDomainAxis().setUpperMargin(0);
        historyChartPanel = new  ChartPanel(historyChart);
        
		if(totalStatEnabled)
		{
	        XYSeries tempS = new XYSeries(totalStatName);
	        
	        tempS.setDescription(totalStatName);
			tempS.setMaximumItemCount(sampleWindow);
			
	        dataset.addSeries(tempS);
	        
	        seriesMap.put(tempS.getDescription(), tempS);
			
			historyChart.getXYPlot().getRenderer().setSeriesPaint(series,Color.black);

		}
		
		historyChartPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Historical", TitledBorder.CENTER, TitledBorder.TOP, null, null));

		add(historyChartPanel);
        
    }
	
	public String getName()
	{
		return statChartPanelName;
	}

	@Override
	public void groupStatsUpdated(ArrayList<SingleStat> sampleList)
	{		
		int totalstat = 0;
		XYSeries tempS;	
		
		String name = null;
		double time = 0;
		double value = 0;
		Color color;
		
		for(SingleStat stat : sampleList)
		{
			value 	= 	stat.getLastSample().getSample();		
			time 	= 	stat.getLastSample().getTime();	
			name 	= 	stat.getStatName();
			color	=	stat.getColor();
			
			tempS = seriesMap.get(name);
			
			// This is a new stat being detected
			if(tempS == null)
			{
				// New Sample Trace for Chart
				tempS = new XYSeries(name);
				tempS.setMaximumItemCount(sampleWindow);				

				// Set Sample Trace Name
				tempS.setDescription(name);
			
				// Set Sample Trace Color
				dataset.addSeries(tempS);
				
				historyChart.getXYPlot().getRenderer().setSeriesPaint(series,color);
				
				// Add Sample Name+Trace to Index of Known SampleNames
				seriesMap.put(name,tempS);				
				
				// Update the series in the bar chart with the new stats color
				statBarChart.getCategoryPlot().getRenderer().setSeriesPaint(series,color);
				
				// Set the outline on the bar
				statBarChart.getCategoryPlot().getRenderer().setSeriesOutlinePaint(series, Color.black);

				// Outline the bars
				statBarChart.getCategoryPlot().getRenderer().setSeriesOutlineStroke(series, new BasicStroke(1));

				// Update series totals
				series++;				
			}
			
			// Add the values of the sample in the trace at the samples time index
			tempS.add(time, value);
			
			// A totals trace that can be enabled
			totalstat+=value;
			
			statDataset.setValue(value, name, category);			
		}
		
		if(totalStatEnabled)
		{
			tempS = seriesMap.get(totalStatName);
			tempS.add(time, totalstat);
			
			statDataset.setValue(totalstat, totalStatName, category);	
		}
		
	}
	
	public void destroy()
	{
		this.removeAll();

		seriesMap = null;

		historyChart = null;
		historyChartPanel = null;
		dataset = null;
		
		statBarChart = null;
		statBarChartPanel = null;
		statDataset = null;
		
	}
	
}
