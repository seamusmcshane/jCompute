package tools.timeseriesanalysis;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;

import jcompute.results.trace.Trace;
import jcompute.results.trace.group.TraceGroupListenerInf;
import jcompute.results.trace.samples.DoubleTraceSample;
import jcompute.results.trace.samples.TraceSample;

public class SingleStatChartPanel extends JPanel implements TraceGroupListenerInf
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(SingleStatChartPanel.class);
	
	private static final long serialVersionUID = -3572724823868862025L;
	
	private String statChartPanelName = "No Panel Name";
	
	private String totalStatName = "NOTSET";
	private boolean totalStatEnabled = false;
	
	private int series = 0;					// Count of number of series
	
	private JFreeChart timeSeriesChart;
	private ChartPanel timeSeriesChartPanel;
	private XYSeriesCollection dataset;
	
	private HashMap<String, XYSeries> seriesMap;
	
	private int sampleWindow;
	
	// Axis Range Adjustment
	private double maxValue = Double.MIN_VALUE;
	private double minValue = Double.MAX_VALUE;
	
	private float lineWidth = 0.35f;
	
	public SingleStatChartPanel()
	{
		this("None", true, false, 0);
	}
	
	public SingleStatChartPanel(String statChartPanelName, boolean displayTitle, boolean totalStatEnabled, int sampleWindow)
	{
		// This panels name
		this.statChartPanelName = statChartPanelName;
		
		// Source Stat Group
		this.totalStatEnabled = totalStatEnabled;
		
		if(totalStatEnabled)
		{
			totalStatName = "Total " + statChartPanelName;
		}
		
		this.sampleWindow = sampleWindow;
		
		log.info(statChartPanelName + " Chart Panel Created");
		
		setLayout(new BorderLayout());
		
		if(displayTitle)
		{
			this.add(new JLabel(statChartPanelName), BorderLayout.NORTH);
		}
		
		seriesMap = new HashMap<String, XYSeries>();
		
		createHistoryChart();
		
		if(totalStatEnabled)
		{
			series++;
		}
	}
	
	private void createHistoryChart()
	{
		dataset = new XYSeriesCollection();
		
		timeSeriesChart = ChartFactory.createXYLineChart(null, null, null, dataset, PlotOrientation.VERTICAL, true, false, false);
		
		timeSeriesChart.getLegend().setWidth(10);
		timeSeriesChart.getXYPlot().setBackgroundPaint(Color.white);
		timeSeriesChart.getXYPlot().setRangeGridlinePaint(Color.LIGHT_GRAY);
		timeSeriesChart.getXYPlot().setDomainGridlinePaint(Color.LIGHT_GRAY);
		timeSeriesChart.getXYPlot().getDomainAxis().setLowerMargin(0);
		timeSeriesChart.getXYPlot().getDomainAxis().setUpperMargin(0);
		timeSeriesChart.getXYPlot().getRangeAxis().setLowerBound(0);
		timeSeriesChartPanel = new ChartPanel(timeSeriesChart);
		
		if(totalStatEnabled)
		{
			XYSeries tempS = new XYSeries(totalStatName);
			
			tempS.setDescription(totalStatName);
			tempS.setMaximumItemCount(sampleWindow);
			
			dataset.addSeries(tempS);
			
			seriesMap.put(tempS.getDescription(), tempS);
			
			timeSeriesChart.getXYPlot().getRenderer().setSeriesPaint(series, Color.black);
			
		}
		
		timeSeriesChartPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Time Series", TitledBorder.CENTER, TitledBorder.TOP, null,
		null));
		
		RectangleInsets rectangleInsets = new RectangleInsets(15F,15F,15F,15F);
		
		timeSeriesChart.setPadding(rectangleInsets);
		
		this.add(timeSeriesChartPanel);
		
	}
	
	@Override
	public String getName()
	{
		return statChartPanelName;
	}
	
	@Override
	public void groupStatsUpdated(ArrayList<Trace> traceList)
	{
		int totalstat = 0;
		XYSeries tempS;
		
		String name = null;
		double time = 0;
		double value = 0;
		Color color;
		
		for(Trace trace : traceList)
		{
			// Note no check for datatype - must be double
			value = ((DoubleTraceSample) trace.getLastSample()).value;
			time = trace.getLastSample().time;
			name = trace.name;
			color = trace.color;
			
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
				
				timeSeriesChart.getXYPlot().getRenderer().setSeriesPaint(series, color);
				
				// Add Sample Name+Trace to Index of Known SampleNames
				seriesMap.put(name, tempS);
				
				// Update series totals
				series++;
			}
			
			// Add the values of the sample in the trace at the samples time
			// index
			tempS.add(time, value);
			
			// A totals trace that can be enabled
			totalstat += value;
			
			if(value > maxValue)
			{
				maxValue = value;
				timeSeriesChart.getXYPlot().getRangeAxis().setUpperBound(maxValue);
			}
			
		}
		
		if(totalStatEnabled)
		{
			tempS = seriesMap.get(totalStatName);
			tempS.add(time, totalstat);
		}
		
		if(totalStatEnabled)
		{
			maxValue = totalstat;
			timeSeriesChart.getXYPlot().getRangeAxis().setUpperBound(maxValue);
		}
		
	}
	
	public void destroy()
	{
		removeAll();
		
		seriesMap = null;
		
		timeSeriesChart = null;
		timeSeriesChartPanel = null;
		dataset = null;
	}
	
	public void removeStat(String name)
	{
		XYSeries tempS = seriesMap.get(name);
		
		if(tempS != null)
		{
			seriesMap.remove(name);
			
			dataset.removeSeries(tempS);
		}
	}
	
	public void statUpdate(String name, int time, double value, int colorOffset)
	{
		XYSeries tempS = seriesMap.get(name);
		
		// This is a new stat being detected
		if(tempS == null)
		{
			Color color = new Color(Color.HSBtoRGB(((0.13f * colorOffset) - 0.13f), 1f, 1f));
			
			// New Sample Trace for Chart
			tempS = new XYSeries(name);
			tempS.setMaximumItemCount(sampleWindow);
			
			// Set Sample Trace Name
			tempS.setDescription(name);
			
			// Set Sample Trace Color
			dataset.addSeries(tempS);
			
			timeSeriesChart.getXYPlot().getRenderer().setSeriesPaint(series, color);
			
			timeSeriesChart.getXYPlot().getRenderer().setSeriesStroke(series, new BasicStroke(lineWidth));
			
			// Add Sample Name+Trace to Index of Known SampleNames
			seriesMap.put(name, tempS);
			
			// Update series totals
			series++;
		}
		
		if(value > maxValue)
		{
			maxValue = value;
			timeSeriesChart.getXYPlot().getRangeAxis().setUpperBound(maxValue);
		}
		
		// Add the values of the sample in the trace at the samples time index
		tempS.add(time, value);
	}
	
	public void populate(int samples, String[] statNames, TraceSample[][] sampleLists)
	{
		int colorOffset = 5;
		int sti = 0;
		for(String name : statNames)
		{
			XYSeries tempS = seriesMap.get(name);
			
			// This is a new stat being detected
			if(tempS == null)
			{
				Color color = new Color(Color.HSBtoRGB(((0.66f * colorOffset)), 1f, 1f));
				//Color color = new Color(Color.HSBtoRGB(((0.13f * colorOffset) - 0.13f), 1f, 1f));
				
				// New Sample Trace for Chart
				tempS = new XYSeries(name);
				tempS.setMaximumItemCount(samples);
				
				// Set Sample Trace Name
				tempS.setDescription(name);
				
				// Set Sample Trace Color
				dataset.addSeries(tempS);
				
				timeSeriesChart.getXYPlot().getRenderer().setSeriesPaint(series, color);
				timeSeriesChart.getXYPlot().getRenderer().setSeriesStroke(series, new BasicStroke(lineWidth));
				
				// Add Sample Name+Trace to Index of Known SampleNames
				seriesMap.put(name, tempS);
				
				// Update series totals
				series++;
			}
			
			DoubleTraceSample[] timeSeries = (DoubleTraceSample[]) sampleLists[sti];
			
			tempS.setNotify(false);
			
			for(int s = 0; s < samples; s++)
			{
				double value = timeSeries[s].value;
				double time = timeSeries[s].time;
				
				if(value > maxValue)
				{
					maxValue = value;
				}
				
				if(value < minValue)
				{
					minValue = value;
				}
				
				// Add the values of the sample in the trace at the samples time
				// index
				tempS.add(time, value);
				
			}
			
			tempS.setNotify(true);
			
			colorOffset++;
			sti++;
		}
		
		timeSeriesChart.getXYPlot().getRangeAxis().setUpperBound(maxValue);
		timeSeriesChart.getXYPlot().getRangeAxis().setLowerBound(minValue);
		
	}
	
	// TIME INDEXED
	public void populateFFT(String name, DoubleTraceSample[] sampleList)
	{
		int samples = sampleList.length;
		
		XYSeries tempS = seriesMap.get(name);
		
		// This is a new stat being detected
		if(tempS == null)
		{
			// Color color = new
			// Color(Color.HSBtoRGB(((0.13f*colorOffset)-0.13f), 1f, 1f));
			
			// New Sample Trace for Chart
			tempS = new XYSeries(name);
			tempS.setMaximumItemCount(samples);
			
			// Set Sample Trace Name
			tempS.setDescription(name);
			
			// Set Sample Trace Color
			dataset.addSeries(tempS);
			
			// timeSeriesChart.getXYPlot().getRenderer().setSeriesPaint(series,
			// color);
			timeSeriesChart.getXYPlot().getRenderer().setSeriesStroke(series, new BasicStroke(lineWidth));
			
			// Add Sample Name+Trace to Index of Known SampleNames
			seriesMap.put(name, tempS);
			
			// Update series totals
			series++;
		}
		
		tempS.setNotify(false);
		
		for(int i = 0; i < samples; i++)
		{
			double value = sampleList[i].value;
			
			if(value > maxValue)
			{
				maxValue = value;
			}
			
			if(value < minValue)
			{
				minValue = value;
			}
			
			// Add the values of the sample in the trace at the samples time
			// index
			tempS.add(sampleList[i].time, value);
		}
		
		tempS.setNotify(true);
		
		timeSeriesChart.getXYPlot().getRangeAxis().setAutoRange(true);
		
	}
	
	public void populateFFTShift(String name, DoubleTraceSample[] sampleList, double maxT)
	{
		int samples = sampleList.length;
		
		XYSeries tempS = seriesMap.get(name);
		
		// This is a new stat being detected
		if(tempS == null)
		{
			// Color color = new
			// Color(Color.HSBtoRGB(((0.13f*colorOffset)-0.13f), 1f, 1f));
			
			// New Sample Trace for Chart
			tempS = new XYSeries(name);
			tempS.setMaximumItemCount(samples);
			
			// Set Sample Trace Name
			tempS.setDescription(name);
			
			// Set Sample Trace Color
			dataset.addSeries(tempS);
			
			// timeSeriesChart.getXYPlot().getRenderer().setSeriesPaint(series,
			// color);
			timeSeriesChart.getXYPlot().getRenderer().setSeriesStroke(series, new BasicStroke(0.3f));
			
			// Add Sample Name+Trace to Index of Known SampleNames
			seriesMap.put(name, tempS);
			
			// Update series totals
			series++;
		}
		
		int per = (int) (samples * 0.01);
		
		tempS.setNotify(false);
		
		// fVals=fs*(-NFFT/2:NFFT/2-1)/NFFT;
		
		double maxV = Double.MIN_VALUE;
		for(int s = 0; s < samples; s++)
		{
			if(sampleList[s].value > maxV)
			{
				maxV = sampleList[s].value;
			}
		}
		
		maxV = 10 * Math.log10(maxV);
		
		for(int s = samples / 2; s >= 0; s--)
		{
			double value = sampleList[s].value;
			
			value = 10 * Math.log10(value);
			
			value = value - maxV;
			
			// value = (Math.sqrt(2)/2) * value;
			
			double time = sampleList[s].time;
			
			if(value > maxValue)
			{
				maxValue = value;
			}
			
			// Add the values of the sample in the trace at the samples time
			// index
			tempS.add(time, value);
			
			if((s % per) == 0)
			{
				// System.out.println(sti + " " + s);
			}
		}
		
		for(int s = samples - 1; s > (samples / 2); s--)
		{
			double value = sampleList[s].value;
			
			value = 10 * Math.log10(value);
			
			value = value - maxV;
			
			// value = (Math.sqrt(2)/2) * value;
			
			double time = sampleList[s].time;
			
			if(value > maxValue)
			{
				maxValue = value;
			}
			
			// Add the values of the sample in the trace at the samples time
			// index
			// tempS.add(time-(maxT)-1, value);
			tempS.add(time - (maxT), value);
			
			if((s % per) == 0)
			{
				// System.out.println(sti + " " + s);
			}
		}
		
		tempS.setNotify(true);
		
		timeSeriesChart.getXYPlot().getDomainAxis().setAutoRange(true);
		timeSeriesChart.getXYPlot().getRangeAxis().setAutoRange(true);
		// timeSeriesChart.getXYPlot().getRangeAxis().setUpperBound(maxValue);
		
	}
	
	public void populateFFT(String name, double[] sampleList)
	{
		int samples = sampleList.length;
		
		XYSeries tempS = seriesMap.get(name);
		
		// This is a new stat being detected
		if(tempS == null)
		{
			// Color color = new
			// Color(Color.HSBtoRGB(((0.13f*colorOffset)-0.13f), 1f, 1f));
			
			// New Sample Trace for Chart
			tempS = new XYSeries(name);
			tempS.setMaximumItemCount(samples);
			
			// Set Sample Trace Name
			tempS.setDescription(name);
			
			// Set Sample Trace Color
			dataset.addSeries(tempS);
			
			// timeSeriesChart.getXYPlot().getRenderer().setSeriesPaint(series,
			// color);
			timeSeriesChart.getXYPlot().getRenderer().setSeriesStroke(series, new BasicStroke(0.3f));
			
			// Add Sample Name+Trace to Index of Known SampleNames
			seriesMap.put(name, tempS);
			
			// Update series totals
			series++;
		}
		
		tempS.setNotify(false);
		
		for(int s = 0; s < samples; s++)
		{
			double value = sampleList[s];
			
			if(value > maxValue)
			{
				maxValue = value;
			}
			
			if(value < minValue)
			{
				minValue = value;
			}
			
			// Add the values of the sample in the trace at the samples time
			// index
			tempS.add(s, value);
		}
		
		tempS.setNotify(true);
		
		timeSeriesChart.getXYPlot().getDomainAxis().setAutoRange(true);
		timeSeriesChart.getXYPlot().getRangeAxis().setAutoRange(true);
		
	}
	
	public void populateFFTShift(String name, double[] sampleList)
	{
		int samples = sampleList.length;
		
		XYSeries tempS = seriesMap.get(name);
		
		// This is a new stat being detected
		if(tempS == null)
		{
			// Color color = new
			// Color(Color.HSBtoRGB(((0.13f*colorOffset)-0.13f), 1f, 1f));
			
			// New Sample Trace for Chart
			tempS = new XYSeries(name);
			tempS.setMaximumItemCount(samples);
			
			// Set Sample Trace Name
			tempS.setDescription(name);
			
			// Set Sample Trace Color
			dataset.addSeries(tempS);
			
			// timeSeriesChart.getXYPlot().getRenderer().setSeriesPaint(series,
			// color);
			timeSeriesChart.getXYPlot().getRenderer().setSeriesStroke(series, new BasicStroke(0.3f));
			
			// Add Sample Name+Trace to Index of Known SampleNames
			seriesMap.put(name, tempS);
			
			// Update series totals
			series++;
		}
		
		int per = (int) (samples * 0.01);
		
		tempS.setNotify(false);
		
		// fVals=fs*(-NFFT/2:NFFT/2-1)/NFFT;
		
		int i = -(samples / 2);
		for(int s = samples / 2; s >= 0; s--)
		{
			double value = sampleList[s];
			
			if(value > maxValue)
			{
				maxValue = value;
			}
			
			// Add the values of the sample in the trace at the samples time
			// index
			tempS.add(i, value);
			
			if((s % per) == 0)
			{
				// System.out.println(sti + " " + s);
			}
			i++;
		}
		
		for(int s = samples - 1; s > (samples / 2); s--)
		{
			double value = sampleList[s];
			
			if(value > maxValue)
			{
				maxValue = value;
			}
			
			// Add the values of the sample in the trace at the samples time
			// index
			tempS.add(i, value);
			
			if((s % per) == 0)
			{
				// System.out.println(sti + " " + s);
			}
			i++;
		}
		
		tempS.setNotify(true);
		
		timeSeriesChart.getXYPlot().getDomainAxis().setAutoRange(true);
		timeSeriesChart.getXYPlot().getRangeAxis().setAutoRange(true);
		// timeSeriesChart.getXYPlot().getRangeAxis().setUpperBound(maxValue);
		
	}
	
	public void setAmpRangeMax(double max)
	{
		timeSeriesChart.getXYPlot().getRangeAxis().setAutoRange(false);
		timeSeriesChart.getXYPlot().getRangeAxis().setUpperBound(max);
	}
	
	public void setAmpMaxAuto()
	{
		timeSeriesChart.getXYPlot().getRangeAxis().setAutoRange(true);
	}
	
	public void setFreqMaxAuto()
	{
		timeSeriesChart.getXYPlot().getDomainAxis().setAutoRange(true);
	}
	
	public void setFreqRangeMax(double max)
	{
		timeSeriesChart.getXYPlot().getDomainAxis().setAutoRange(false);
		timeSeriesChart.getXYPlot().getDomainAxis().setUpperBound(max);
	}
	
	public void setLineWidth(float lineWidth)
	{
		this.lineWidth = lineWidth;
	}
	
}
