package jCompute.Gui.Component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class TablePanel extends JPanel
{
	private static final long serialVersionUID = 7193787210494563482L;
	private JLabel lblTitle;
	private JTable table;
	private JScrollPane scrollPane;
	private DefaultTableModel model;

	/**
	 * Creates a Panel with a table - needs Title and a List of column names
	 * (column titles)
	 * 
	 * @param title
	 * @param columnNames
	 */
	public TablePanel(String title, String columnNames[], boolean alternatingRowColors)
	{
		super();

		setLayout(new BorderLayout(0, 0));

		this.setBorder(null);

		// Title
		lblTitle = new JLabel(title);
		lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
		this.add(lblTitle, BorderLayout.NORTH);

		setUpTable(this, columnNames, alternatingRowColors);

	}

	public TablePanel(String columnNames[], boolean alternatingRowColors)
	{
		super();

		setLayout(new BorderLayout(0, 0));

		this.setBorder(null);

		setUpTable(this, columnNames, alternatingRowColors);

	}

	/**
	 * Creates the JTable with required values
	 * 
	 * @param panel
	 * @param colNames
	 */
	private void setUpTable(JPanel panel, String colNames[], boolean alternatingRowColors)
	{
		model = new DefaultTableModel()
		{
			private static final long serialVersionUID = 3096437320105476853L;

			@Override
		    public boolean isCellEditable(int row, int column) 
		    {
		        return false;
		    }
		};

		for(String colName : colNames)
		{
			model.addColumn(colName);
		}
		
		if (alternatingRowColors)
		{
			table = new JTable(model)
			{
				private static final long serialVersionUID = -3299922426578737865L;

				public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
				{
					Component c = super.prepareRenderer(renderer, row, column);

					if (!isRowSelected(row))
					{
						if (row % 2 == 1)
						{
							c.setBackground(new Color(240, 240, 240));
						}
						else
						{
							c.setBackground(getBackground());
						}
					}
					else
					{
						c.setBackground(getSelectionBackground());
					}
					return c;
				}
			};
		}
		else
		{
			table = new JTable(model);
		}

		scrollPane = new JScrollPane(table);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		panel.add(scrollPane, BorderLayout.CENTER);
		scrollPane.setViewportBorder(null);
		scrollPane.setWheelScrollingEnabled(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

		table.setBackground(Color.white);
		table.setBorder(null);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setCellSelectionEnabled(false);
		table.setColumnSelectionAllowed(false);
		table.setRowSelectionAllowed(true);

	}
	
	/**
	 * Sets a TableCellRenderer to be used for a column
	 * 
	 * @param renderer
	 * @param column
	 */
	public void addColumRenderer(TableCellRenderer renderer, int column)
	{
		table.getColumnModel().getColumn(column).setCellRenderer(renderer);
	}

	public void setDefaultRenderer(Class<?> classType, TableCellRenderer defaultRenderer)
	{
		table.setDefaultRenderer(classType, defaultRenderer);
	}

	public TableCellRenderer getTableHeaderRenderer()
	{
		return table.getTableHeader().getDefaultRenderer();
	}

	/**
	 * Appends a new row to the end of the table
	 * 
	 * @param rowKey
	 * @param columnValues
	 */
	public void addRow(final String columnValueList[])
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				model.addRow(columnValueList);
			}
		});
	}

	/**
	 * Updates a row
	 * 
	 * @param rowKey
	 */
	public void updateRow(final String rowKey, final String columnValueList[])
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				int row = findRow(rowKey);
				
				for(int c=0;c<columnValueList.length;c++)
				{
					model.setValueAt(columnValueList[c], row, c);
				}
				
			}
		});
	}
	
	private int findRow(String rowKey)
	{
		int size = model.getRowCount();
		
		for(int i=0;i<size;i++)
		{
			if(model.getValueAt(i, 0).equals(rowKey))
			{
				return i;				
			}
		}
		return -1;
	}
	
	/**
	 * Removes a row
	 * 
	 * @param rowKey
	 */
	public void removeRow(final String rowKey)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				int row = findRow(rowKey);
				
				model.removeRow(row);
				
			}
		});
		
	}

	/**
	 * Clears the data in the Table
	 */
	public void clearTable()
	{
		model.setRowCount(0);
	}

	public void addMouseListener(MouseAdapter adaptor)
	{
		table.addMouseListener(adaptor);
	}

	/**
	 * Gets a value at a specific coordinate in the table
	 * 
	 * @param row
	 * @param column
	 * @return
	 */
	public String getValueAt(int row, int column)
	{
		String value;

		value = (String) model.getValueAt(row, column);

		return value;
	}

	/**
	 * Returns the number of rows in the table
	 * 
	 * @return
	 */
	public int getRowsCount()
	{
		int rowCount = model.getRowCount();

		return rowCount;
	}

	public void clearSelection()
	{
		table.clearSelection();
	}

	public void updateCell(final String rowKey, final int column, final String columnValue)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{				
				int row = findRow(rowKey);
				
				model.setValueAt(columnValue, row, column);

			}
		});
	}

	public void updateCells(final String rowKey, final int columns[], final String columnValues[])
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				int row = findRow(rowKey);
				
				for(int c=0;c<columnValues.length;c++)
				{
					model.setValueAt(columnValues[c], row, columns[c]);
				}
				
			}
		});
	}

	public void setColumWidth(int column, int pref)
	{
		table.getColumnModel().getColumn(column).setPreferredWidth(pref);
		table.getColumnModel().getColumn(column).setMinWidth(pref);
		table.getColumnModel().getColumn(column).setMaxWidth(pref);
	}

	public JTable getJTable()
	{
		return table;
	}

}