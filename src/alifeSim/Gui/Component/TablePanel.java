package alifeSim.Gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;

public class SimTablePanel extends JPanel
{
	private static final long serialVersionUID = 7193787210494563482L;
	private JLabel lblTitle;		
	private JTable table;
	private TableModel model;
	
	/**
	 * Creates a Panel with a table - needs Title and a List of column names (column titles)
	 * @param title
	 * @param columnNames
	 */
	public SimTablePanel(String title,String columnNames[])
	{
		super();

		setLayout(new BorderLayout(0, 0));
		this.setMinimumSize(new Dimension(350,250));
		this.setBorder(null);
		this.setPreferredSize(new Dimension(350,250));
		
		// Title
		lblTitle = new JLabel(title);
		lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(lblTitle, BorderLayout.NORTH);
		
		setUpTable(this,columnNames);	

	}
	
	/**
	 * Appends a new row to the end of the table
	 * @param rowKey
	 * @param columnValues
	 */
	public void addRow(String rowKey,String columnValues[])
	{
		model.addRow(rowKey,columnValues);
	}
	
	/**
	 * Clears the data in the Table
	 */
	public void clearTable()
	{
		model.clearData();
	}
	
	/**
	 * Creates the JTable with required values
	 * @param panel
	 * @param colNames
	 */
	private void setUpTable(JPanel panel,String colNames[])
	{
		model = new TableModel(colNames);
		
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
	
	/**
	 * Gets a list of values in an entire column
	 * @param index
	 * @return
	 */
	public List<String>getColumnValues(int index)
	{
		return model.getColumnValues(index);
		
	}
	
	/**
	 * Gets a value at a specific coordinate in the table
	 * @param row
	 * @param column
	 * @return
	 */
	public String getValueAt(int row,int column)
	{
		return (String)model.getValueAt(row, column);
	}
	
	
	/**
	 * Returns the number of rows in the table
	 * @return
	 */
	public int getRowsCount()
	{
		return model.getRowCount();
	}
	
	/** 
	 * Private Table Model Class
	 * @author Seamus McShane
	 *
	 */
	private class TableModel extends AbstractTableModel
	{
		private static final long serialVersionUID = -3810467809045113741L;
		private List<String> columnNames = new ArrayList<String>();
		private List<TableRow> tableRow;

        public TableModel(String names[]) 
        {

    		for(String colName: names)
    		{
    			columnNames.add(colName);
    		}
            
            clearData();
            
        }
				        
        public void addRow(String rowKey,String columnList[])
        {
        	tableRow.add(new TableRow(rowKey,columnList));
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
		public Object getValueAt(int row, int col)
		{
			return tableRow.get(row).getColum(col);
		}
		
		public void clearData()
		{
			this.tableRow = new ArrayList<TableRow>();
		}
		
		public List<TableRow> getRows()
		{			
			return tableRow;
		}
		
		public List<String>getColumnValues(int index)
		{
			List<String>columValues = new LinkedList<String>();
			
			for(TableRow row:tableRow)
			{
				columValues.add(row.getColum(0));
			}
			
			return columValues;			
		}
	}	
	
	/**
	 *  Private Row Class for Table Model
	 * @author Seamus McShane
	 *
	 */
	private class TableRow
	{
		private List<String> columns;
		
		public TableRow(String rowKey,String columNames[])
		{
			super();
			
			this.columns = new ArrayList<>();
			
			columns.add(rowKey);
			
			for(String columName : columNames)
			{
				columns.add(columName);
			}			
		}
		
		public String getColum(int index)
		{
			return columns.get(index);
		}
				
	}
}