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

public class PopulationPanel extends StatPanelAbs
{
	private static final long serialVersionUID = -3572724823868862025L;
	
	private final String name = "Population";
			
	StatGroup populationGroup;
	String groupName = "Population";
	String category = "Species";

	JFreeChart populationBarChart;
	int series = 0;
	org.jfree.chart.ChartPanel populationBarChartPanel;
	DefaultCategoryDataset populationDataset;
	Plot populationChartPlot;

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
			
	public PopulationPanel(StatManager manager)
	{
		System.out.println("Population Chart Panel Created");
		populationGroup = manager.getStatGroup(groupName);		
		setLayout(new GridLayout(2, 1, 0, 0));
		createHistoryChart2DST();
		createBarChart();
		traceAdds = 0;
	}
	
	private void createBarChart()
	{
		populationDataset = new DefaultCategoryDataset();
		populationBarChart = ChartFactory.createBarChart3D(null, null, null, populationDataset, PlotOrientation.VERTICAL, true, false, false);
		populationChartPlot = populationBarChart.getCategoryPlot();
		populationBarChartPanel = new org.jfree.chart.ChartPanel(populationBarChart);
		populationBarChartPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Current", TitledBorder.CENTER, TitledBorder.TOP, null, null));

		
		add(populationBarChartPanel);
	}
	
	private void createHistoryChart2DST()
	{
		traceMapST = new HashMap<String,ITrace2D>();
		chart2dST = new Chart2D();
		chart2dST.setUseAntialiasing(true);
		chart2dST.enablePointHighlighting(false);
		chart2dST.setToolTipType(Chart2D.ToolTipType.VALUE_SNAP_TO_TRACEPOINTS);
		chart2dST.getAxisY().getAxisTitle().setTitle("Population");
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
	}		

	@Override
	public void destroy()
	{
		populationGroup = null;
		groupName = null;
		category = null;
	}
	
	@Override
	public void update()
	{
		if(traceAdds%stSamDiv == 0)
		{
		
			for (String statName : populationGroup.getStatList()) 
			{
				ITrace2D tempT = traceMapST.get(statName);
			
				// This is a new stat being detected
				if(tempT == null)
				{
					tempT = new Trace2DLtd(stSamWin);
					tempT.setName(statName);
				
					tempT.setColor(populationGroup.getStat(statName).getColor());
					tempT.setStroke(new BasicStroke(1));
					traceMapST.put(statName,tempT);
					chart2dST.addTrace(tempT);
					tempT.setPointHighlighter(new PointPainterDisc(4));
					
					// Update the series in the bar chart with the new stats color
					populationBarChart.getCategoryPlot().getRenderer().setSeriesPaint(series,tempT.getColor());
					
					// Update series totals
					series++;
				}
				
				// Set the values
				tempT.addPoint(traceAdds,populationGroup.getStat(statName).getLastSample());
				populationDataset.setValue(populationGroup.getStat(statName).getLastSample(), statName, category);
			}
			
		}

		traceAdds++;
		
	}
	
	public String getName()
	{
		return name;
	}
	
}
