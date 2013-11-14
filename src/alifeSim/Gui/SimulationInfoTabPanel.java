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
	private JTable table;
	private InfoTabTableModel model;
	
	private int traceAdds=0;
	private Font chartFont = new Font("Sans", Font.BOLD, 12);
	
	// Short Term
	private Chart2D chart2dST;
	private ITrace2D traceST;
	private HashMap<String,ITrace2D> traceMapST;
	private ChartPanel chartPanelST;
	private int stSamWin = 30;
	
	ITrace2D runTime;
	float cOffset=0.8f;
	
	public SimulationInfoTabPanel()
	{
		super();

		setLayout(new BorderLayout(0, 0));
		this.setMinimumSize(new Dimension(350,250));

		JPanel panel = new JPanel();
		panel.setBorder(null);
		panel.setPreferredSize(new Dimension(350,250));
		add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));
		
		JLabel lblTitle = new JLabel("Information Status");
		lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(lblTitle, BorderLayout.NORTH);
		
		setUpTable(panel);	
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
	
	public void update()
	{
		for (InfoTableRow row : model.getRows()) 
		{
				ITrace2D tempT = traceMapST.get(row.getColum(0));
			
				// This is a new stat being detected
				if(tempT == null)
				{
					tempT = new Trace2DLtd(stSamWin);
					tempT.setName(row.getColum(0));
				
					cOffset+=0.13f;
					cOffset=cOffset%1f;
					tempT.setColor( new Color(Color.HSBtoRGB(cOffset,0.9f,1f)));
					tempT.setStroke(new BasicStroke(1));
					traceMapST.put(row.getColum(0),tempT);
					chart2dST.addTrace(tempT);
					tempT.setPointHighlighter(new PointPainterDisc(4));
				}
				
				// Set the values
				tempT.addPoint(traceAdds,Integer.parseInt(row.getColum(3)));		
			
		}
		
		runTime.addPoint(traceAdds,0);
		
		traceAdds++;
		
	}
	
	public void clearTrace(String name)
	{
		ITrace2D tempT = traceMapST.remove(name);
		chart2dST.removeTrace(tempT);		
	}
	public void addRow(String tab, String status, String stepNo, String avgSPS, String runTime)
	{
		model.addRow(tab, status, stepNo, avgSPS,runTime);
	}
	
	public void clearTable()
	{
		model.clearData();
	}
	
	private void setUpTable(JPanel panel)
	{
		model = new InfoTabTableModel();
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setViewportBorder(null);
		panel.add(scrollPane, BorderLayout.CENTER);
		table = new JTable(model);
		table.setRowSelectionAllowed(false);
		table.setBorder(null);
		scrollPane.setViewportView(table);
		table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
	}
	
	// Hidden Class
	private class InfoTabTableModel extends AbstractTableModel
	{
		private static final long serialVersionUID = -3810467809045113741L;
		private List<String> columnNames = new ArrayList<String>();
		private List<InfoTableRow> tableRow;

        public InfoTabTableModel() 
        {

            columnNames.add("Tab");
            columnNames.add("Status");
            columnNames.add("Step No");
            columnNames.add("Avg SPS");
            columnNames.add("Run Time");
            
            clearData();
        }
				        
        public void addRow(String tab, String status, String stepNo,String avgSPS, String runTime)
        {
        	tableRow.add(new InfoTableRow(tab, status, stepNo, avgSPS,runTime));
        	fireTableRowsInserted(tableRow.size() - 1, tableRow.size() - 1);
        }
        
		@Override
		public int getColumnCount()
		{
			return columnNames.size();
		}

		@Override
		public String getColumnName(int index) 
		{
			return columnNames.get(index);			
		}
		
		@Override
		public int getRowCount()
		{
			return tableRow.size();
		}

		@Override
		public Object getValueAt(int index, int col)
		{
			// TODO Auto-generated method stub
			return tableRow.get(index).getColum(col);
		}
		
		public void clearData()
		{
			this.tableRow = new ArrayList<InfoTableRow>();
		}
		
		public List<InfoTableRow> getRows()
		{			
			return tableRow;
		}
	}	
	
	private class InfoTableRow
	{
		private List<String> columns;
		
		public InfoTableRow(String tab, String status, String stepNo,String avgSPS, String runTime)
		{
			super();
			
			this.columns = new ArrayList<>();
			
			columns.add(tab);
			columns.add(status);
			columns.add(stepNo);
			columns.add(avgSPS);
			columns.add(runTime);
			
		}
		
		public String getColum(int index)
		{
			return columns.get(index);
		}
				
	}
	
}
