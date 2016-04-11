package jCompute.Gui.Component.TableCell;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

import jCompute.JComputeEventBus;
import jCompute.Cluster.Controller.NodeManager.NodeManager.NodeManagerState;
import jCompute.Cluster.Controller.NodeManager.Event.NodeManagerStateChangeRequest;
import jCompute.Gui.Cluster.TableRowItems.NodeInfoRowItem;
import jCompute.Gui.Component.swing.jpanel.TablePanel;

public class NodeControlButtonRenderer extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, ActionListener
{
	private static final long serialVersionUID = 9095858955877318230L;

	private TablePanel<Integer, NodeInfoRowItem> tablePanel;
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

	public NodeControlButtonRenderer(TablePanel<Integer, NodeInfoRowItem> tablePanel, int column, ImageIcon runIcon, ImageIcon pauseIcon,
	ImageIcon shutdownIcon)
	{
		this.tablePanel = tablePanel;
		jTable = tablePanel.getJTable();

		this.runIcon = runIcon;
		this.pauseIcon = pauseIcon;

		rPanel = new JPanel();
		rPanel.setLayout(new GridLayout(0, 2, 0, 0));
		rPanel.setBackground(Color.white);
		rPauseResumeToggle = new JButton("");
		rPauseResumeToggle.setIcon(pauseIcon);
		rPanel.add(rPauseResumeToggle);
		rShutdownToggle = new JButton("");
		rShutdownToggle.setIcon(shutdownIcon);
		rPanel.add(rShutdownToggle);

		ePanel = new JPanel();
		ePanel.setLayout(new GridLayout(0, 2, 0, 0));
		ePanel.setBackground(Color.white);

		ePauseResumeToggle = new JButton("");
		ePauseResumeToggle.setIcon(pauseIcon);
		ePauseResumeToggle.addActionListener(this);
		ePauseResumeToggle.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseExited(MouseEvent e)
			{
				// Editing Cell and mouse leaves it
				fireEditingCanceled();
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				// Mouse is release on a cell, handles clicking the panel or disabled
				// buttons.
				fireEditingCanceled();
			}
		});
		ePanel.add(ePauseResumeToggle);
		eShutdownToggle = new JButton("");
		eShutdownToggle.setIcon(shutdownIcon);
		eShutdownToggle.addActionListener(this);
		eShutdownToggle.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseExited(MouseEvent e)
			{
				// Editing Cell and mouse leaves it
				fireEditingCanceled();
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				// Mouse is release on a cell, handles clicking the panel or disabled
				// buttons.
				fireEditingCanceled();
			}
		});
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

		editorValue = (int) value;

		return ePanel;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		int row = jTable.convertRowIndexToModel(jTable.getEditingRow());
		int uid = (int) tablePanel.getValueAt(row, 0);

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
			String desc = (String) tablePanel.getValueAt(row, 9);
			String address = (String) tablePanel.getValueAt(row, 2);

			StringBuilder sb = new StringBuilder();

			sb.append("<html>");
			sb.append("<h3>");
			sb.append("Shutdown Node ?");
			sb.append("</h3>");
			sb.append("Node&emsp;&emsp;&emsp;&emsp;&nbsp;&nbsp;&nbsp;:&emsp;");
			sb.append("<font color=red>");
			sb.append(uid);
			sb.append("</font>");
			sb.append("<br>");
			sb.append("Description&emsp;&emsp;:&emsp;");
			sb.append("<font color=red>");
			sb.append(desc);
			sb.append("</font>");
			sb.append("<br>");
			sb.append("Address&emsp;&emsp;&emsp;&nbsp;&nbsp;:&emsp;");
			sb.append("<font color=red>");
			sb.append(address);
			sb.append("</font>");
			sb.append("</html>");

			JPanel messagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

			messagePanel.add(new JLabel(sb.toString()));

			int result = JOptionPane.showConfirmDialog(tablePanel, messagePanel, "Node Shutdown", JOptionPane.YES_NO_OPTION);

			if(result == JOptionPane.YES_OPTION)
			{
				JComputeEventBus.post(new NodeManagerStateChangeRequest(uid, NodeManagerState.SHUTDOWN));
			}
		}

		fireEditingStopped();
	}
}
