package alifeSim.Gui.Charts;

import alifeSim.Stats.SingleStat;
import alifeSim.Stats.StatGroup;
import alifeSim.Stats.StatGroupListenerInf;
import alifeSim.Stats.StatManager;
import alifeSim.Stats.StatSample;
import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.pointpainters.PointPainterDisc;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.gui.chart.views.ChartPanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.awt.GridLayout;

import javax.swing.border.TitledBorder;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.JPanel;
import javax.swing.UIManager;

public class GlobalStatChartPanel extends JPanel implements StatGroupListenerInf
{
	private static final long serialVersionUID = -3572724823868862025L;
	
	private String name = "stat";
	private String totalStatName = "NOTSET";
	private boolean totalStatEnabled = false;
	
	private String groupName = "INVALID";
	private String category = "Species";	// Assume for now global = global per species

	private JFreeChart statBarChart;
	private int series = 0;
	private org.jfree.chart.ChartPanel statBarChartPanel;
	private DefaultCategoryDataset statDataset;
	private Plot statChartPlot;

	private Font chartFont = new Font("Sans", Font.BOLD, 12);
	
	// Short Term
	private Chart2D chart2dST;
	private ITrace2D traceST;
	private HashMap<String,ITrace2D> traceMapST;
	private ChartPanel chartPanelST;

	private int stSamWin;
			
	public GlobalStatChartPanel(String name ,boolean totalStatEnabled, int sampleWindow)
	{
		// This panels name
		this.name = name;
		
		// Source Stat Group
		
		this.groupName = name;
		this.totalStatEnabled = totalStatEnabled;
		
		if(totalStatEnabled)
		{
			this.totalStatName = "Total "+name;
		}
		
		this.stSamWin = sampleWindow;
		
		System.out.println(name + " Chart Panel Created");

		setLayout(new GridLayout(2, 1, 0, 0));
		createHistoryChart2DST();
		createBarChart();
		//traceAdds = 0;
	}
	
	private void createBarChart()
	{
		statDataset = new DefaultCategoryDataset();
		statBarChart = ChartFactory.createBarChart3D(null, null, null, statDataset, PlotOrientation.VERTICAL, true, false, false);
		statChartPlot = statBarChart.getCategoryPlot();
		statBarChartPanel = new org.jfree.chart.ChartPanel(statBarChart);
		statBarChartPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Current", TitledBorder.CENTER, TitledBorder.TOP, null, null));

		
		add(statBarChartPanel);
	}
	
	private void createHistoryChart2DST()
	{
		traceMapST = new HashMap<String,ITrace2D>();
		chart2dST = new Chart2D();
		chart2dST.setUseAntialiasing(true);
		chart2dST.enablePointHighlighting(false);
		chart2dST.setToolTipType(Chart2D.ToolTipType.VALUE_SNAP_TO_TRACEPOINTS);
		chart2dST.getAxisY().getAxisTitle().setTitle(name);
		chart2dST.getAxisY().getAxisTitle().setTitleFont(chartFont);
		chart2dST.getAxisX().getAxisTitle().setTitle("Step");
		chart2dST.getAxisX().getAxisTitle().setTitleFont(chartFont);
		chart2dST.setGridColor(new Color(192,192,192));
		chart2dST.getAxisY().setPaintGrid(true);
		chart2dST.getAxisX().setPaintGrid(true);
		chart2dST.setBackground(Color.white);
		chartPanelST = new ChartPanel(chart2dST);
		
		chartPanelST.setBorder(new TitledBorder(null, "Historical", TitledBorder.CENTER, TitledBorder.TOP, null, null)); 	
		chartPanelST.setBackground(Color.white);
		chart2dST.setMinPaintLatency(1000);
		add(chartPanelST);	
		
		if(totalStatEnabled)
		{
			ITrace2D tempT = new Trace2DLtd(stSamWin);
			tempT.setName(totalStatName);
			tempT.setColor(Color.black);
			traceMapST.put(tempT.getName(), tempT);
			chart2dST.addTrace(tempT);
		}

	}
	
	public String getName()
	{
		return name;
	}

	@Override
	public void groupStatsUpdated(ArrayList<SingleStat> sampleList)
	{
		int totalstat = 0;
		ITrace2D tempT;	
		
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
			
			tempT = traceMapST.get(name);
			
			// This is a new stat being detected
			if(tempT == null)
			{
				// New Sample Trace for Chart
				tempT = new Trace2DLtd(stSamWin);
				
				// Set Sample Trace Name
				tempT.setName(name);
			
				// Set Sample Trace Color
				tempT.setColor(color);
				
				// Set Line Width
				tempT.setStroke(new BasicStroke(1));
				
				// Add Sample Name+Trace to Index of Known SampleNames
				traceMapST.put(name,tempT);

				// Add Trace to the Chart
				chart2dST.addTrace(tempT);
				
				// Show a circle around samples in the chart
				tempT.setPointHighlighter(new PointPainterDisc(4));
				
				// Update the series in the bar chart with the new stats color
				statBarChart.getCategoryPlot().getRenderer().setSeriesPaint(series,tempT.getColor());
				
				// Update series totals
				series++;				
			}
			
			// Add the values of the sample in the trace at the samples time index
			tempT.addPoint(time,value);
			
			// A totals trace that can be enabled
			totalstat+=value;
			
			statDataset.setValue(value, name, category);
			
		}
		
		if(totalStatEnabled)
		{
			tempT = traceMapST.get(totalStatName);
			tempT.addPoint(time,totalstat);
		}		
		
	}
	
}
