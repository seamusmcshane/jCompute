package alifeSim.Gui;

import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;

import java.awt.Color;
import java.awt.SystemColor;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.ListSelectionModel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

public class SimulationInfoTabPanel extends JPanel
{
	private static final long serialVersionUID = 76641721672552215L;
	private JTable table;
	private InfoTabTableModel model;
	
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
