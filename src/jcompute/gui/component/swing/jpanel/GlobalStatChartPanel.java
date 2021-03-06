package jcompute.gui.component.swing.jpanel;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.awt.GridLayout;

import javax.swing.border.TitledBorder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import jcompute.results.trace.Trace;
import jcompute.results.trace.Trace.TraceDataType;
import jcompute.results.trace.group.TraceGroupListenerInf;
import jcompute.results.trace.samples.DoubleTraceSample;
import jcompute.results.trace.samples.IntegerTraceSample;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

public class GlobalStatChartPanel extends JPanel implements TraceGroupListenerInf
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(GlobalStatChartPanel.class);
	
	private static final long serialVersionUID = -3572724823868862025L;
	
	private String statChartPanelName = "No Panel Name";
	
	private String totalStatName = "NOTSET";
	private boolean totalStatEnabled = false;
	
	private String category;
	private int series = 0;					// Count of number of series
	
	private JPanel container;
	
	private JFreeChart statBarChart;
	private ChartPanel statBarChartPanel;
	private DefaultCategoryDataset statDataset;
	
	private JFreeChart historyChart;
	private ChartPanel historyChartPanel;
	private XYSeriesCollection dataset;
	
	private HashMap<String, XYSeries> seriesMap;
	
	private int sampleWindow;
	
	// Axis Range Adjustment
	private double maxValue = 1;
	private double minValue = 0;
	
	private boolean dynamicLegend;
	private int dynamicLegendLimit;
	
	public GlobalStatChartPanel(String statChartPanelName, String categoryName, boolean displayTitle, boolean totalStatEnabled, int sampleWindow,
	boolean splitleftRight)
	{
		// This panels name
		this.statChartPanelName = statChartPanelName;
		
		// Displayed category
		this.category = categoryName;
		
		// Source Stat Group
		this.totalStatEnabled = totalStatEnabled;
		
		if(totalStatEnabled)
		{
			this.totalStatName = "Total " + statChartPanelName;
		}
		
		this.sampleWindow = sampleWindow;
		
		log.info(statChartPanelName + " Chart Panel Created");
		
		this.setLayout(new BorderLayout());
		
		container = new JPanel();
		
		this.add(container, BorderLayout.CENTER);
		
		if(displayTitle)
		{
			JLabel title = new JLabel("    " + statChartPanelName);
			title.setFont(this.getFont().deriveFont(Font.BOLD));
			this.add(title, BorderLayout.NORTH);
		}
		
		if(splitleftRight)
		{
			container.setLayout(new GridLayout(1, 2, 0, 0));
			
		}
		else
		{
			container.setLayout(new GridLayout(2, 1, 0, 0));
		}
		seriesMap = new HashMap<String, XYSeries>();
		
		createHistoryChart();
		createBarChart();
		if(totalStatEnabled)
		{
			series++;
		}
	}
	
	public GlobalStatChartPanel(String statChartPanelName, String categoryName, boolean displayTitle, boolean totalStatEnabled, int sampleWindow,
	boolean splitleftRight, int dynamicLegendLimit)
	{
		this(statChartPanelName, categoryName, displayTitle, totalStatEnabled, sampleWindow, splitleftRight);
		
		this.dynamicLegend = true;
		this.dynamicLegendLimit = dynamicLegendLimit;
	}
	
	private void createBarChart()
	{
		statDataset = new DefaultCategoryDataset();
		statBarChart = ChartFactory.createBarChart3D(null, null, null, statDataset, PlotOrientation.VERTICAL, true, false, false);
		statBarChartPanel = new org.jfree.chart.ChartPanel(statBarChart);
		statBarChartPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Current", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		
		statBarChart.getCategoryPlot().setBackgroundPaint(Color.white);
		statBarChart.getCategoryPlot().setRangeGridlinePaint(Color.LIGHT_GRAY);
		statBarChart.getCategoryPlot().setDomainGridlinePaint(Color.LIGHT_GRAY);
		
		statBarChart.getCategoryPlot().getRangeAxis().setLowerBound(0);
		
		statBarChart.getCategoryPlot().getDomainAxis().setLowerMargin(0);
		statBarChart.getCategoryPlot().getDomainAxis().setUpperMargin(0);
		statBarChart.getCategoryPlot().getDomainAxis().setCategoryMargin(0);
		((BarRenderer) statBarChart.getCategoryPlot().getRenderer()).setItemMargin(0);
		((BarRenderer) statBarChart.getCategoryPlot().getRenderer()).setBarPainter(new StandardBarPainter());
		
		((BarRenderer) statBarChart.getCategoryPlot().getRenderer()).setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
		
		Font font = new Font("SansSerif", Font.PLAIN, 9);
		
		((BarRenderer) statBarChart.getCategoryPlot().getRenderer()).setBaseItemLabelFont(font);
		((BarRenderer) statBarChart.getCategoryPlot().getRenderer()).setBaseItemLabelsVisible(true);
		
		((BarRenderer) statBarChart.getCategoryPlot().getRenderer()).setDrawBarOutline(true);
		
		if(totalStatEnabled)
		{
			statDataset.setValue(0, totalStatName, category);
			statBarChart.getCategoryPlot().getRenderer().setSeriesPaint(series, Color.black);
			statBarChart.getCategoryPlot().getRenderer().setSeriesOutlinePaint(series, Color.black);
		}
		
		container.add(statBarChartPanel);
	}
	
	private void createHistoryChart()
	{
		dataset = new XYSeriesCollection();
		
		historyChart = ChartFactory.createXYLineChart(null, null, null, dataset, PlotOrientation.VERTICAL, true, false, false);
		
		historyChart.getLegend().setWidth(10);
		historyChart.getXYPlot().setBackgroundPaint(Color.white);
		historyChart.getXYPlot().setRangeGridlinePaint(Color.LIGHT_GRAY);
		historyChart.getXYPlot().setDomainGridlinePaint(Color.LIGHT_GRAY);
		historyChart.getXYPlot().getDomainAxis().setLowerMargin(0);
		historyChart.getXYPlot().getDomainAxis().setUpperMargin(0);
		historyChart.getXYPlot().getRangeAxis().setLowerBound(0);
		historyChartPanel = new ChartPanel(historyChart);
		
		if(totalStatEnabled)
		{
			XYSeries tempS = new XYSeries(totalStatName);
			
			tempS.setDescription(totalStatName);
			tempS.setMaximumItemCount(sampleWindow);
			
			dataset.addSeries(tempS);
			
			seriesMap.put(tempS.getDescription(), tempS);
			
			historyChart.getXYPlot().getRenderer().setSeriesPaint(series, Color.black);
			
		}
		
		historyChartPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Historical", TitledBorder.CENTER, TitledBorder.TOP, null,
		null));
		
		container.add(historyChartPanel);
		
	}
	
	@Override
	public String getName()
	{
		return statChartPanelName;
	}
	
	@Override
	public void groupStatsUpdated(ArrayList<Trace> sampleList)
	{
		int totalstat = 0;
		XYSeries tempS;
		
		String name = null;
		double time = 0;
		double value = 0;
		Color color;
		
		dataset.setNotify(false);
		statDataset.setNotify(false);
		
		for(Trace stat : sampleList)
		{
			// Note no check for datatype - must be double or integer
			if(stat.type == TraceDataType.Integer)
			{
				value = ((IntegerTraceSample) stat.getLastSample()).value;
			}
			else
			{
				value = ((DoubleTraceSample) stat.getLastSample()).value;
			}
			
			time = stat.getLastSample().time;
			name = stat.name;
			color = stat.color;
			
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
				
				historyChart.getXYPlot().getRenderer().setSeriesPaint(series, color);
				
				// Add Sample Name+Trace to Index of Known SampleNames
				seriesMap.put(name, tempS);
				
				// Update the series in the bar chart with the new stats color
				statBarChart.getCategoryPlot().getRenderer().setSeriesPaint(series, color);
				
				// Set the outline on the bar
				statBarChart.getCategoryPlot().getRenderer().setSeriesOutlinePaint(series, Color.black);
				
				// Outline the bars
				statBarChart.getCategoryPlot().getRenderer().setSeriesOutlineStroke(series, new BasicStroke(1));
				
				// Update series totals
				series++;
			}
			
			// Add the values of the sample in the trace at the samples time
			// index
			tempS.add(time, value);
			
			// A totals trace that can be enabled
			totalstat += value;
			
			checkChartRange(value);
			
			statDataset.setValue(value, name, category);
			
		}
		
		if(totalStatEnabled)
		{
			tempS = seriesMap.get(totalStatName);
			tempS.add(time, totalstat);
			
			statDataset.setValue(totalstat, totalStatName, category);
		}
		
		if(totalStatEnabled)
		{
			checkChartRange(totalstat);
		}
		
		dataset.setNotify(true);
		statDataset.setNotify(true);
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
	
	public void addStat(String name, int colorOffset)
	{
		XYSeries tempS = seriesMap.get(name);
		
		// This is a new stat being detected
		if(tempS == null)
		{
			// Color
			Color color = new Color(Color.HSBtoRGB(((0.13f * colorOffset) - 0.13f), 1f, 1f));
			
			// New Sample Trace for Chart
			tempS = new XYSeries(name);
			tempS.setMaximumItemCount(sampleWindow);
			
			// Set Sample Trace Name
			tempS.setDescription(name);
			
			// Add Sample Name+Trace to Index of Known SampleNames
			seriesMap.put(name, tempS);
			
			// Set Sample Trace Color
			dataset.addSeries(tempS);
			
			int hci = dataset.getSeriesIndex(name);
			historyChart.getXYPlot().getRenderer().setSeriesPaint(hci, color);
			
			// We need to create a value for the series to exist in a bar chart.
			statDataset.setValue(0, name, category);
			int bci = statDataset.getRowIndex(name);
			
			// Update the series in the bar chart with the new stats color
			statBarChart.getCategoryPlot().getRenderer().setSeriesPaint(bci, color);
			// Set the outline on the bar
			statBarChart.getCategoryPlot().getRenderer().setSeriesOutlinePaint(bci, Color.black);
			
			// Outline the bars
			statBarChart.getCategoryPlot().getRenderer().setSeriesOutlineStroke(bci, new BasicStroke(1));
			
			// Update series totals
			series++;
		}
	}
	
	public void removeStat(String name)
	{
		XYSeries tempS = seriesMap.get(name);
		
		if(tempS != null)
		{
			seriesMap.remove(name);
			
			dataset.removeSeries(tempS);
			
			statDataset.removeValue(name, category);
		}
	}
	
	public void statUpdate(String name, int time, double value)
	{
		dataset.setNotify(false);
		statDataset.setNotify(false);
		
		// Legend will cause the graphs to become too small, so disable them
		// when needed
		if(dynamicLegend)
		{
			if(seriesMap.size() > dynamicLegendLimit)
			{
				statBarChart.getLegend().setVisible(false);
				historyChart.getLegend().setVisible(false);
			}
			else
			{
				statBarChart.getLegend().setVisible(true);
				historyChart.getLegend().setVisible(true);
			}
		}
		
		XYSeries tempS = seriesMap.get(name);
		
		checkChartRange(value);
		
		// Add the values of the sample in the trace at the samples time index
		tempS.add(time, value);
		
		statDataset.setValue(value, name, category);
		
		dataset.setNotify(true);
		statDataset.setNotify(true);
	}
	
	private void checkChartRange(double value)
	{
		if(value > maxValue)
		{
			maxValue = value;
			historyChart.getXYPlot().getRangeAxis().setUpperBound(maxValue);
			statBarChart.getCategoryPlot().getRangeAxis().setUpperBound(maxValue);
			
			return;
		}
		
		if(value < minValue)
		{
			minValue = value;
			historyChart.getXYPlot().getRangeAxis().setLowerBound(minValue);
			statBarChart.getCategoryPlot().getRangeAxis().setLowerBound(minValue);
			
			return;
		}
	}
}
