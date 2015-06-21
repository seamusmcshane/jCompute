package jCompute.Gui.Component.TableCell;

import jCompute.JComputeEventBus;
import jCompute.Cluster.Controller.NodeManager.NodeManagerState;
import jCompute.Cluster.Controller.Event.NodeManagerStateChangeRequest;
import jCompute.Gui.Component.Swing.TablePanel;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.EventObject;

import javax.swing.JButton;

public class NodeControlButtonRenderer extends AbstractCellEditor
		implements
			TableCellRenderer,
			TableCellEditor,
			ActionListener,
			MouseListener
{
	private static final long serialVersionUID = 9095858955877318230L;
	
	private TablePanel tablePanel;
	private JTable jTable;
	
	private ImageIcon runIcon = null;
	private ImageIcon pauseIcon = null;
	
	private JPanel rPanel;
	
	private JButton rPauseResumeToggle;
	private JButton rShutdownToggle;
	
	private JPanel ePanel;
	private JButton ePauseResumeToggle;
	private JButton eShutdownToggle;
	
	private int editorValue;
	
	public NodeControlButtonRenderer(TablePanel tablePanel, int column, ImageIcon runIcon, ImageIcon pauseIcon, ImageIcon shutdownIcon)
	{
		this.tablePanel = tablePanel;
		jTable = tablePanel.getJTable();
		
		this.runIcon = runIcon;
		this.pauseIcon = pauseIcon;
		
		rPanel = new JPanel();
		rPanel.setLayout(new GridLayout(0, 2, 0, 0));
		rPauseResumeToggle = new JButton("");
		rPauseResumeToggle.setIcon(pauseIcon);
		rPanel.add(rPauseResumeToggle);
		rShutdownToggle = new JButton("");
		rShutdownToggle.setIcon(shutdownIcon);
		rPanel.add(rShutdownToggle);
		
		ePanel = new JPanel();
		ePanel.setLayout(new GridLayout(0, 2, 0, 0));
		ePauseResumeToggle = new JButton("");
		ePauseResumeToggle.setIcon(pauseIcon);
		ePauseResumeToggle.addActionListener(this);
		ePauseResumeToggle.addMouseListener(this);
		ePanel.add(ePauseResumeToggle);
		eShutdownToggle = new JButton("");
		eShutdownToggle.setIcon(shutdownIcon);
		eShutdownToggle.addActionListener(this);
		eShutdownToggle.addMouseListener(this);
		ePanel.add(eShutdownToggle);
		
		TableColumnModel columnModel = tablePanel.getJTable().getColumnModel();
		columnModel.getColumn(column).setCellRenderer(this);
		columnModel.getColumn(column).setCellEditor(this);
		JComputeEventBus.register(this);
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		NodeManagerState state = NodeManagerState.fromInt((int) value);
		
		System.out.println("getTableCellRendererComponent row " + row + " state" + state.toString());
		
		rPanel.setBackground(UIManager.getColor("Table.background"));
		
		switch(state)
		{
			case RUNNING:
				rPauseResumeToggle.setEnabled(true);
				rPauseResumeToggle.setIcon(pauseIcon);
				rShutdownToggle.setEnabled(false);
			break;
			case PAUSING:
				rPauseResumeToggle.setEnabled(false);
				rShutdownToggle.setEnabled(false);
			break;
			case PAUSED:
				rPauseResumeToggle.setIcon(runIcon);
				rPauseResumeToggle.setEnabled(true);
				rShutdownToggle.setEnabled(true);
			break;
			default:
				// Not Enabled
				rPauseResumeToggle.setEnabled(false);
				rShutdownToggle.setEnabled(false);
			break;
		}
		
		return rPanel;
	}
	
	@Override
	public Object getCellEditorValue()
	{
		System.out.println("editorValue " + editorValue);
		
		return editorValue;
	}
	
	@Override
	public boolean shouldSelectCell(EventObject e)
	{
		return true;
	}
	
	@Override
	public boolean isCellEditable(EventObject e)
	{
		return true;
	}
	
	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
	{
		NodeManagerState state = NodeManagerState.fromInt((int) value);
		
		ePanel.setBackground(UIManager.getColor("Table.background"));
		
		switch(state)
		{
			case RUNNING:
				ePauseResumeToggle.setEnabled(true);
				ePauseResumeToggle.setIcon(pauseIcon);
				eShutdownToggle.setEnabled(false);
			break;
			case PAUSING:
				ePauseResumeToggle.setEnabled(false);
				eShutdownToggle.setEnabled(false);
			break;
			case PAUSED:
				ePauseResumeToggle.setIcon(runIcon);
				ePauseResumeToggle.setEnabled(true);
				eShutdownToggle.setEnabled(true);
			break;
			default:
				// Not Enabled
				ePauseResumeToggle.setEnabled(false);
				eShutdownToggle.setEnabled(false);
			break;
		}
		
		this.editorValue = (int) value;
		
		return ePanel;
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		int row = jTable.convertRowIndexToModel(jTable.getEditingRow());
		int uid = (int) tablePanel.getValueAt(row, 0);
		
		System.out.println("Row " + row + " uid " + uid);
		
		if(e.getSource() == ePauseResumeToggle)
		{
			if(ePauseResumeToggle.getIcon() == pauseIcon)
			{
				JComputeEventBus.post(new NodeManagerStateChangeRequest(uid, NodeManagerState.PAUSING));
			}
			else
			{
				JComputeEventBus.post(new NodeManagerStateChangeRequest(uid, NodeManagerState.RUNNING));
			}
			
		}
		else if(e.getSource() == eShutdownToggle)
		{
			int result = JOptionPane.showConfirmDialog(null, "Shutdown node " + uid + "?", "Warning", JOptionPane.YES_NO_OPTION);
			
			if(result == JOptionPane.YES_OPTION)
			{
				JComputeEventBus.post(new NodeManagerStateChangeRequest(uid, NodeManagerState.SHUTDOWN));
			}
		}
		
		fireEditingStopped();
	}
	
	@Override
	public void mouseExited(MouseEvent e)
	{
		// Editing Cell and mouse leaves it
		fireEditingStopped();
	}
	
	@Override
	public void mouseReleased(MouseEvent e)
	{
		// Mouse is release on a cell, handles clicking the panel or disabled
		// buttons.
		fireEditingStopped();
	}
	
	@Override
	public void mouseClicked(MouseEvent e)
	{
		// NA
	}
	
	@Override
	public void mouseEntered(MouseEvent e)
	{
		// NA
	}
	
	@Override
	public void mousePressed(MouseEvent e)
	{
	}
}
