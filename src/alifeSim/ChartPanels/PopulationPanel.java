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
import java.util.concurrent.Semaphore;
import java.awt.GridLayout;

import javax.swing.border.TitledBorder;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import java.awt.BorderLayout;
import javax.swing.UIManager;

public class PopulationPanel extends StatPanelAbs
{
	private static final long serialVersionUID = -3572724823868862025L;
	
	private final String name = "Population";
	
	private Semaphore resizeLock = new Semaphore(1);
		
	StatGroup populationGroup;
	String groupName = "Population";
	String category = "Species";

	JFreeChart populationBarChart;
	org.jfree.chart.ChartPanel populationBarChartPanel;
	DefaultCategoryDataset populationDataset;
	Plot populationChartPlot;

	// 
	Chart2D chart2d;
	ITrace2D trace;
	int traceAdds;
	HashMap<String,ITrace2D> traceMap;
	Font chartFont = new Font("Sans", Font.BOLD, 12);
	ChartPanel chartPanel;
	
	public PopulationPanel(StatManager manager)
	{
		populationGroup = manager.getStatGroup(groupName);		
		setLayout(new GridLayout(2, 1, 0, 0));
		createHistoryChart2D();
		createBarChart();
	}
	
	private void createBarChart()
	{
		populationDataset = new DefaultCategoryDataset();
		populationBarChart = ChartFactory.createBarChart3D(null, null, null, populationDataset, PlotOrientation.VERTICAL, true, false, false);
		populationChartPlot = populationBarChart.getCategoryPlot();
		populationBarChartPanel = new org.jfree.chart.ChartPanel(populationBarChart);
		populationBarChartPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Current Populations", TitledBorder.CENTER, TitledBorder.TOP, null, null));
		for(int i=0;i<100;i++)
		{
			populationBarChart.getCategoryPlot().getRenderer().setSeriesPaint(i,new Color(Color.HSBtoRGB((float)Math.random(),0.9f,1f)));
		}
		
		add(populationBarChartPanel);
	}
	
	private void createHistoryChart2D()
	{
		traceMap = new HashMap<String,ITrace2D>();
		chart2d = new Chart2D();
		chart2d.setUseAntialiasing(true);
		chart2d.enablePointHighlighting(true);
		chart2d.setToolTipType(Chart2D.ToolTipType.VALUE_SNAP_TO_TRACEPOINTS);
		chart2d.getAxisY().getAxisTitle().setTitle("Population");
		chart2d.getAxisY().getAxisTitle().setTitleFont(chartFont);
		chart2d.getAxisX().getAxisTitle().setTitle("Step");
		chart2d.getAxisX().getAxisTitle().setTitleFont(chartFont);
		chart2d.setGridColor(new Color(192,192,192));
		chart2d.getAxisY().setPaintGrid(true);
		chart2d.getAxisX().setPaintGrid(true);
		chart2d.setBackground(Color.white);
		chartPanel = new ChartPanel(chart2d);
		
		chartPanel.setBorder(new TitledBorder(null, "Historical", TitledBorder.CENTER, TitledBorder.TOP, null, null)); 	
		chartPanel.setBackground(Color.white);
		
	/*	trace = new Trace2DLtd(200);
		trace.setColor(Color.RED);
		chart2d.addTrace(trace);*/
		traceAdds = 0;
		add(chartPanel);
		chartPanel.getChart().setLayout(new BorderLayout(0, 0));
		
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
		resizeLock.acquireUninterruptibly();

		for (String statName : populationGroup.getStatList()) 
		{
			
			if(traceAdds%45 == 0)
			{
				ITrace2D tempT = traceMap.get(statName);
				
				if(tempT == null)
				{

					tempT = new Trace2DLtd(100);
					tempT.setName(statName);
				
					tempT.setColor(new Color(Color.HSBtoRGB((float)Math.random(),0.9f,1f)));
					tempT.setStroke(new BasicStroke(2));
					traceMap.put(statName,tempT);
					chart2d.addTrace(tempT);
					tempT.setPointHighlighter(new PointPainterDisc(8));
				}
				
				tempT.addPoint(traceAdds,populationGroup.getStat(statName).getLastSample());
				
				populationDataset.setValue(populationGroup.getStat(statName).getLastSample(), statName, category);
				
			}


		}
		traceAdds++;
		
		resizeLock.release();
	}
	
	public String getName()
	{
		return name;
	}
	
}
