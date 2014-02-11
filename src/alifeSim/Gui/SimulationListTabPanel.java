package alifeSim.Gui;

import info.monitorenter.gui.chart.Chart2D;
import info.monitorenter.gui.chart.ITrace2D;
import info.monitorenter.gui.chart.pointpainters.PointPainterDisc;
import info.monitorenter.gui.chart.traces.Trace2DLtd;
import info.monitorenter.gui.chart.views.ChartPanel;

import javax.swing.JPanel;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import java.awt.Color;
import java.awt.SystemColor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.ListSelectionModel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

public class SimulationInfoTabPanel extends JPanel
{
	private static final long serialVersionUID = 76641721672552215L;
	
	private SimTablePanel table;
	
	private int traceAdds=0;
	private Font chartFont = new Font("Sans", Font.BOLD, 12);
	
	// Short Term
	private Chart2D chart2dST;
	private ITrace2D traceST;
	private HashMap<String,ITrace2D> traceMapST;
	private ChartPanel chartPanelST;
	private int stSamWin = 300;
	
	ITrace2D runTime;
	float cOffset=0.8f;
	
	public SimulationInfoTabPanel()
	{
		super();

		setLayout(new BorderLayout(0, 0));
		this.setMinimumSize(new Dimension(350,250));
		
		table = new SimTablePanel("Information Status",new String[]{"Tab","Status","Step No","Avg Sps","Run Time"});
		
		this.add(table);
		
		createHistoryChart2DST();		
		
	}
	
	private void createHistoryChart2DST()
	{
		traceMapST = new HashMap<String,ITrace2D>();
		chart2dST = new Chart2D();
		chart2dST.setUseAntialiasing(true);
		//chart2dST.enablePointHighlighting(false);
		//chart2dST.setToolTipType(Chart2D.ToolTipType.VALUE_SNAP_TO_TRACEPOINTS);
		chart2dST.getAxisY().getAxisTitle().setTitle("Step Rate");
		chart2dST.getAxisY().getAxisTitle().setTitleFont(chartFont);
		chart2dST.getAxisX().getAxisTitle().setTitle("");
		chart2dST.getAxisX().getAxisTitle().setTitleFont(chartFont);
		chart2dST.setGridColor(new Color(192,192,192));
		chart2dST.getAxisY().setPaintGrid(true);
		chart2dST.getAxisX().setPaintGrid(true);
		chart2dST.setBackground(Color.white);
		chartPanelST = new ChartPanel(chart2dST);
		
		chartPanelST.setBorder(new TitledBorder(null, "Simulation Performance Graph (30 seconds)", TitledBorder.CENTER, TitledBorder.TOP, null, null)); 	
		chartPanelST.setBackground(Color.white);
		chartPanelST.setPreferredSize(new Dimension(350,250));
		add(chartPanelST, BorderLayout.NORTH);	
		
		runTime = new Trace2DLtd(stSamWin);
		runTime.setName("Run Time");
		
		chart2dST.addTrace(runTime);
		chart2dST.setMinPaintLatency(1000);
		
	}	
	
	public void clearTable()
	{
		table.clearTable();
	}
	
	public void addRow(String rowKey,String columnValues[])
	{
		table.addRow(rowKey, columnValues);
	}
	
	public void update()
	{

		
		for (int row=0;row<table.getRowsCount();row++) 
		{
				String tabName = table.getValueAt(row,0);
				String value = table.getValueAt(row,3);
				ITrace2D tempT = traceMapST.get(tabName);
			
				// This is a new stat being detected
				if(tempT == null)
				{
					tempT = new Trace2DLtd(stSamWin);
					tempT.setName(tabName);
				
					cOffset+=0.13f;
					cOffset=cOffset%1f;
					tempT.setColor( new Color(Color.HSBtoRGB(cOffset,0.9f,1f)));
					tempT.setStroke(new BasicStroke(1));
					traceMapST.put(tabName,tempT);
					chart2dST.addTrace(tempT);
					tempT.setPointHighlighter(new PointPainterDisc(4));
				}
				
				// Set the values
				tempT.addPoint(traceAdds,Integer.parseInt(value));		
			
		}
		
		runTime.addPoint(traceAdds,0);
		
		traceAdds++;
		
	}
	
	public void clearTrace(String name)
	{
		ITrace2D tempT = traceMapST.remove(name);
		
		if(tempT!=null)
		{
			chart2dST.removeTrace(tempT);		
		}
		
	}

	
}
