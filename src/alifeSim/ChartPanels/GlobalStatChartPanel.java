package alifeSim.ChartPanels;

import alifeSim.Stats.StatGroup;
import alifeSim.Stats.StatManager;
import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.pointpainters.PointPainterDisc;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.gui.chart.views.ChartPanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.util.HashMap;
import java.awt.GridLayout;

import javax.swing.border.TitledBorder;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.UIManager;

public class GlobalStatChartPanel extends StatPanelAbs
{
	private static final long serialVersionUID = -3572724823868862025L;
	
	private String name = "stat";
	private String totalStatName = "NOTSET";
	private boolean totalStatEnabled = false;
	
	StatGroup statGroup;
	String groupName = "INVALID";
	String category = "Species";	// Assume for now global = global per species

	JFreeChart statBarChart;
	int series = 0;
	org.jfree.chart.ChartPanel statBarChartPanel;
	DefaultCategoryDataset statDataset;
	Plot statChartPlot;

	int traceAdds;
	Font chartFont = new Font("Sans", Font.BOLD, 12);
	
	// Short Term
	Chart2D chart2dST;
	ITrace2D traceST;
	HashMap<String,ITrace2D> traceMapST;
	ChartPanel chartPanelST;
	int stSamDiv = 100;
	int stSamWin = 100;
	int stSamPer = stSamDiv*stSamWin;
			
	public GlobalStatChartPanel(String name ,StatManager manager, boolean totalStatEnabled, int sampleRate)
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
		
		this.stSamDiv = sampleRate;
		
		System.out.println(name + " Chart Panel Created");
		statGroup = manager.getStatGroup(groupName);		
		setLayout(new GridLayout(2, 1, 0, 0));
		createHistoryChart2DST();
		createBarChart();
		traceAdds = 0;
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

	@Override
	public void destroy()
	{
		statGroup = null;
		groupName = null;
		category = null;
		
		System.out.println(name + " Chart Panel Self Destructing");
	}
	
	@Override
	public void update()
	{
		int totalstat = 0;
		ITrace2D tempT;
		
		if(traceAdds%stSamDiv == 0)
		{
		
			for (String statName : statGroup.getStatList()) 
			{

				tempT = traceMapST.get(statName);
			
				// This is a new stat being detected
				if(tempT == null)
				{
					tempT = new Trace2DLtd(stSamWin);
					tempT.setName(statName);
				
					tempT.setColor(statGroup.getStat(statName).getColor());
					tempT.setStroke(new BasicStroke(1));
					traceMapST.put(statName,tempT);
					chart2dST.addTrace(tempT);
					tempT.setPointHighlighter(new PointPainterDisc(4));
					
					// Update the series in the bar chart with the new stats color
					statBarChart.getCategoryPlot().getRenderer().setSeriesPaint(series,tempT.getColor());
					
					// Update series totals
					series++;
				}
				
				// Set the values
				tempT.addPoint(traceAdds,statGroup.getStat(statName).getLastSample());
				totalstat+=statGroup.getStat(statName).getLastSample();
				statDataset.setValue(statGroup.getStat(statName).getLastSample(), statName, category);
			}
			
			if(totalStatEnabled)
			{
				tempT = traceMapST.get(totalStatName);
				tempT.addPoint(traceAdds,totalstat);
			}
		
		}

		traceAdds++;
		
	}
	
	public String getName()
	{
		return name;
	}
	
}
