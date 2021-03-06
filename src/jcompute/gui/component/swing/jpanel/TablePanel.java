package jcompute.gui.component.swing.jpanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.table.TableCellRenderer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.AbstractTableComparatorChooser;
import ca.odell.glazedlists.gui.TableFormat;
import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.swing.AdvancedListSelectionModel;
import ca.odell.glazedlists.swing.GlazedListsSwing;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import jcompute.gui.component.RowItem;
import jcompute.gui.component.tablecell.ColorConstants;

public class TablePanel<IndexType, RowType extends RowItem<RowType, IndexType>> extends JPanel
{
	private static final long serialVersionUID = 7193787210494563482L;
	
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(TablePanel.class);
	
	private JTable table;
	
	private JScrollPane scrollPane;
	
	// The displayed list
	private EventList<RowType> displayed;
	
	// The base active list
	private EventList<RowType> activeList;
	private AdvancedListSelectionModel<RowType> selectionModel;
	
	private Class<RowType> rowClass;
	
	public static final Color BackgroundColor = ColorConstants.Gainsboro;
	public static final Color RowBackgroundColor = ColorConstants.WhiterSmoke;
	public static final Color RowAlternateBackgroundColor = ColorConstants.WhiterSmoke.brighter();
	public static final Color TitleBackgroundColor = ColorConstants.LightBlue;
	public static final Color GridColor = ColorConstants.Gainsboro;
	
	/**
	 * @wbp.parser.constructor
	 */
	public TablePanel(Class<RowType> rowClass, String title, boolean sortable, boolean rowSelection, boolean hScroll, AbstractMatcherEditor<RowType> editor)
	{
		super();
		
		this.rowClass = rowClass;
		
		setLayout(new BorderLayout(0, 0));
		
		setBorder(null);
		
		if(title != null)
		{
			// Table Title Label
			JPanel titlePanel = new JPanel(new BorderLayout());
			titlePanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
			titlePanel.setBackground(TitleBackgroundColor);
			JLabel lblTitle = new JLabel(title);
			lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
			
			titlePanel.add(lblTitle, BorderLayout.CENTER);
			
			this.add(titlePanel, BorderLayout.NORTH);
		}
		
		setUpTable(sortable, rowSelection, hScroll, editor);
	}
	
	public TablePanel(Class<RowType> rowClass, String title, boolean sortable, boolean rowSelection)
	{
		this(rowClass, sortable, rowSelection);
	}
	
	public TablePanel(Class<RowType> rowClass, boolean sortable, boolean rowSelection)
	{
		this(rowClass, sortable, rowSelection, false);
	}
	
	public TablePanel(Class<RowType> rowClass, boolean sortable, boolean rowSelection, boolean hScroll)
	{
		this(rowClass, null, sortable, rowSelection, hScroll, null);
	}
	
	private void setUpTable(boolean sortable, boolean rowSelection, boolean hScroll, AbstractMatcherEditor<RowType> editor)
	{
		RowType rowItem;
		
		try
		{
			// Cast the row class to a a RowItem and use the RowItem api for
			// getting the field and their names
			rowItem = rowClass.getConstructor().newInstance();
			
			// Create a new table format via reflection of the rowClass
			TableFormat<RowType> tf = GlazedLists.tableFormat(rowClass, rowItem.getFieldList(), rowItem.getFieldNames(), rowItem.getEditableCells());
			
			SortedList<RowType> sortedList = new SortedList<RowType>(new BasicEventList<RowType>());
			
			if(editor == null)
			{
				displayed = sortedList;
			}
			else
			{
				displayed = new FilterList<RowType>(sortedList, editor);
			}
			
			// Note - prepareRenderer override.
			table = new JTable(GlazedListsSwing.eventTableModelWithThreadProxyList(displayed, tf))
			{
				private static final long serialVersionUID = 1455279281459224269L;
				
				@Override
				public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
				{
					Component c = super.prepareRenderer(renderer, row, column);
					
					if(!isRowSelected(row))
					{
						if((row % 2) == 1)
						{
							c.setBackground(RowBackgroundColor);
						}
						else
						{
							c.setBackground(RowAlternateBackgroundColor);
						}
					}
					
					return c;
				}
			};
			table.setFillsViewportHeight(true);
			table.setBackground(BackgroundColor);
			table.setRowHeight(20);
			table.setShowGrid(true);
			
			table.setGridColor(GridColor);
			
			if(sortable)
			{
				TableComparatorChooser.install(table, sortedList, AbstractTableComparatorChooser.SINGLE_COLUMN);
			}
			selectionModel = GlazedListsSwing.eventSelectionModelWithThreadProxyList(sortedList);
			selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			table.setSelectionModel(selectionModel);
			
			table.setCellSelectionEnabled(false);
			table.setColumnSelectionAllowed(false);
			
			table.setRowSelectionAllowed(rowSelection);
			
			scrollPane = new JScrollPane(table);
			
			if(hScroll == true)
			{
				scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
				table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			}
			else
			{
				table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
				scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			}
			scrollPane.setWheelScrollingEnabled(true);
			scrollPane.getVerticalScrollBar().setUnitIncrement(25);
			
			this.add(scrollPane, BorderLayout.CENTER);
			
			// Set the active list
			activeList = sortedList;
		}
		catch(InstantiationException | IllegalAccessException e)
		{
			log.error("Row Item Reflection : " + rowClass.toString());
			e.printStackTrace();
		}
		catch(IllegalArgumentException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(InvocationTargetException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(NoSuchMethodException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(SecurityException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
	 */
	public void addRow(RowType row)
	{
		activeList.getReadWriteLock().writeLock().lock();
		
		activeList.add(row);
		
		activeList.getReadWriteLock().writeLock().unlock();
	}
	
	/**
	 * Appends a new row to the end of the table and removes the first row if above a threshold value
	 */
	public void addRowTailed(RowType row, int threshold)
	{
		activeList.getReadWriteLock().writeLock().lock();
		
		activeList.add(row);
		
		int size = activeList.size();
		
		if(size > threshold)
		{
			activeList.remove(0);
		}
		
		activeList.getReadWriteLock().writeLock().unlock();
	}
	
	/**
	 * Updates a row
	 *
	 * @param rowKey
	 */
	public void updateRow(final IndexType rowKey, final RowType row)
	{
		activeList.getReadWriteLock().writeLock().lock();
		
		int index = findRow(rowKey);
		
		if(index != -1)
		{
			activeList.set(index, row);
		}
		
		activeList.getReadWriteLock().writeLock().unlock();
	}
	
	private int findRow(IndexType rowKey)
	{
		int index = -1;
		RowType row;
		
		for(int i = 0; i < activeList.size(); i++)
		{
			row = activeList.get(i);
			
			if(row.keyEquals(rowKey))
			{
				index = i;
				break;
			}
		}
		
		return index;
	}
	
	/**
	 * Removes a row
	 *
	 * @param rowKey
	 */
	public void removeRow(final IndexType rowKey)
	{
		activeList.getReadWriteLock().writeLock().lock();
		
		int index = findRow(rowKey);
		
		if(index != -1)
		{
			activeList.remove(index);
		}
		
		activeList.getReadWriteLock().writeLock().unlock();
	}
	
	public void removeFirstRow()
	{
		activeList.getReadWriteLock().writeLock().lock();
		
		if(activeList.size() > 0)
		{
			activeList.remove(0);
		}
		
		activeList.getReadWriteLock().writeLock().unlock();
	}
	
	public int getRows()
	{
		activeList.getReadWriteLock().readLock().lock();
		
		int size = activeList.size();
		
		activeList.getReadWriteLock().readLock().unlock();
		
		return size;
	}
	
	/**
	 * Clears the data in the Table
	 */
	public void clearTable()
	{
		activeList.getReadWriteLock().writeLock().lock();
		
		activeList.clear();
		
		activeList.getReadWriteLock().writeLock().unlock();
		
	}
	
	public void addMouseListener(MouseAdapter adaptor)
	{
		table.addMouseListener(adaptor);
	}
	
	/**
	 * Gets a value at a specific coordinate in the table
	 *
	 * @param rowIndex
	 * @param columnIndex
	 * @return
	 */
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		Object value;
		
		activeList.getReadWriteLock().readLock().lock();
		
		RowType row = activeList.get(rowIndex);
		
		value = row.getFieldValue(columnIndex);
		
		activeList.getReadWriteLock().readLock().unlock();
		
		return value;
	}
	
	/**
	 * Returns the number of rows in the table
	 *
	 * @return
	 */
	public int getRowsCount()
	{
		int size = 0;
		
		activeList.getReadWriteLock().readLock().lock();
		
		size = activeList.size();
		
		activeList.getReadWriteLock().readLock().unlock();
		
		return size;
	}
	
	public void clearSelection()
	{
		table.clearSelection();
	}
	
	public void updateCell(final IndexType rowKey, final int column, final Object columnValue)
	{
		activeList.getReadWriteLock().writeLock().lock();
		
		int index = findRow(rowKey);
		
		if(index != -1)
		{
			RowType row = activeList.get(index);
			
			row.setFieldValue(column, columnValue);
			
			activeList.set(index, row);
		}
		
		activeList.getReadWriteLock().writeLock().unlock();
	}
	
	public void updateCells(final IndexType rowKey, final int columns[], final Object columnValues[])
	{
		activeList.getReadWriteLock().writeLock().lock();
		
		int index = findRow(rowKey);
		
		if(index != -1)
		{
			RowType row = activeList.get(index);
			
			for(int c = 0; c < columns.length; c++)
			{
				row.setFieldValue(columns[c], columnValues[c]);
			}
			
			activeList.set(index, row);
		}
		
		activeList.getReadWriteLock().writeLock().unlock();
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
	
	public void setSelection(int row, int column)
	{
		table.changeSelection(row, column, false, false);
	}
	
	public void scrollToBottom()
	{
		displayed.getReadWriteLock().readLock().lock();
		
		int size = displayed.size();
		
		table.scrollRectToVisible(table.getCellRect(size - 1, 0, true));
		
		displayed.getReadWriteLock().readLock().unlock();
	}
}