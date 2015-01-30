package tools.TimeSeriesAnalysis;

import jCompute.Stats.Trace.StatSample;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.awt.GridLayout;

import javax.swing.border.TitledBorder;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

public class FrequencyChart extends JPanel
{
	// SL4J Logger
	private static Logger log = LoggerFactory.getLogger(FrequencyChart.class);

	private static final long serialVersionUID = -3572724823868862025L;

	private String statChartPanelName = "No Panel Name";

	private String totalStatName = "NOTSET";

	private String category;
	private int series = 0;					// Count of number of series

	private JFreeChart statBarChart;
	private ChartPanel statBarChartPanel;
	private DefaultCategoryDataset statDataset;

	private HashMap<String, XYSeries> seriesMap;

	// Axis Range Adjustment
	private double maxValue = 0;
	private double minValue = Double.MAX_VALUE;

	public FrequencyChart(String statChartPanelName, String categoryName, boolean displayTitle)
	{
		// This panels name
		this.statChartPanelName = statChartPanelName;

		// Displayed category
		this.category = categoryName;

		log.info(statChartPanelName + " Chart Panel Created");

		this.setLayout(new BorderLayout());

		if(displayTitle)
		{
			this.add(new JLabel(statChartPanelName), BorderLayout.NORTH);
		}

		seriesMap = new HashMap<String, XYSeries>();

		createBarChart();

	}

	private void createBarChart()
	{
		statDataset = new DefaultCategoryDataset();
		statBarChart = ChartFactory.createBarChart3D(null, null, null, statDataset, PlotOrientation.VERTICAL, true,
				false, false);
		statBarChartPanel = new org.jfree.chart.ChartPanel(statBarChart);
		statBarChartPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Current",
				TitledBorder.CENTER, TitledBorder.TOP, null, null));

		statBarChart.getCategoryPlot().setBackgroundPaint(Color.white);
		statBarChart.getCategoryPlot().setRangeGridlinePaint(Color.LIGHT_GRAY);
		statBarChart.getCategoryPlot().setDomainGridlinePaint(Color.LIGHT_GRAY);

		statBarChart.getCategoryPlot().getRangeAxis().setLowerBound(0);

		statBarChart.getCategoryPlot().getDomainAxis().setLowerMargin(0);
		statBarChart.getCategoryPlot().getDomainAxis().setUpperMargin(0);
		statBarChart.getCategoryPlot().getDomainAxis().setCategoryMargin(0);
		((BarRenderer) statBarChart.getCategoryPlot().getRenderer()).setItemMargin(0);
		((BarRenderer) statBarChart.getCategoryPlot().getRenderer()).setBarPainter(new StandardBarPainter());

		((BarRenderer) statBarChart.getCategoryPlot().getRenderer())
				.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());

		Font font = new Font("SansSerif", Font.PLAIN, 9);

		((BarRenderer) statBarChart.getCategoryPlot().getRenderer()).setBaseItemLabelFont(font);
		((BarRenderer) statBarChart.getCategoryPlot().getRenderer()).setBaseItemLabelsVisible(true);

		((BarRenderer) statBarChart.getCategoryPlot().getRenderer()).setDrawBarOutline(true);

		this.add(statBarChartPanel);
	}

	public String getName()
	{
		return statChartPanelName;
	}

	public void destroy()
	{
		this.removeAll();

		seriesMap = null;

		statBarChart = null;
		statBarChartPanel = null;
		statDataset = null;

	}

	public void removeStat(String name)
	{
		XYSeries tempS = seriesMap.get(name);

		if(tempS != null)
		{
			seriesMap.remove(name);

			statDataset.removeValue(name, category);
		}
	}

	public void populate(double[] array)
	{
		int colorOffset = 0;

		statDataset.setNotify(false);
			
		int samples = array.length;
		for(int s = 0; s < samples; s++)
		{
			double value = array[s];

			if(value > maxValue)
			{
				maxValue = value;
			}
			
			if(value < minValue)
			{
				minValue = value;
			}

			statDataset.setValue(value, String.valueOf(s), category);

			Color color = new Color(Color.HSBtoRGB(((0.13f * colorOffset) - 0.13f), 1f, 1f));

			// Update the series in the bar chart with the new stats color
			statBarChart.getCategoryPlot().getRenderer().setSeriesPaint(s, color);

			// Set the outline on the bar
			statBarChart.getCategoryPlot().getRenderer().setSeriesOutlinePaint(s, Color.black);

			// Outline the bars
			statBarChart.getCategoryPlot().getRenderer().setSeriesOutlineStroke(s, new BasicStroke(1));
			colorOffset++;
		}

		statDataset.setNotify(true);

		statBarChart.getCategoryPlot().getRangeAxis().setUpperBound(maxValue);
		statBarChart.getCategoryPlot().getRangeAxis().setLowerBound(minValue);
		
		statBarChart.removeLegend();
		

	}
}
