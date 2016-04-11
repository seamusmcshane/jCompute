package jCompute.gui.component.swing.logtab;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;

import org.apache.logging.log4j.Level;

import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.Matcher;
import jCompute.gui.cluster.tablerowitems.StandardLogRowItem;
import jCompute.gui.component.swing.jpanel.TablePanel;

public class LogLevelSelect extends AbstractMatcherEditor<StandardLogRowItem> implements ActionListener
{
	private JComboBox<String> comboBoxLevelSelect;

	private TablePanel<Integer, StandardLogRowItem> table;

	public LogLevelSelect(String[] levelNames)
	{
		comboBoxLevelSelect = new JComboBox<String>();
		comboBoxLevelSelect.setModel(new DefaultComboBoxModel<String>(levelNames));

		// handle changes to the list's selection
		comboBoxLevelSelect.addActionListener(this);
	}

	public JComboBox<String> getJComboBox(TablePanel<Integer, StandardLogRowItem> table)
	{
		this.table = table;

		return comboBoxLevelSelect;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		@SuppressWarnings("unchecked")
		String level = (String) ((JComboBox<String>) e.getSource()).getSelectedItem();

		Matcher<StandardLogRowItem> matcher = new LogLinesLogLevelMatcher(level);
		fireChanged(matcher);

		if(table != null)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					table.scrollToBottom();
				}
			});
		}
	}

	private class LogLinesLogLevelMatcher implements Matcher<StandardLogRowItem>
	{
		private String logLevel;

		public LogLinesLogLevelMatcher(String level)
		{
			logLevel = level;
		}

		@Override
		public boolean matches(StandardLogRowItem line)
		{
			// Non selected
			if(logLevel == null)
			{
				return false;
			}

			// Level missing from line.
			if(line == null)
			{
				return false;
			}

			// Level not valid
			if(Level.getLevel(line.getLevel()) == null)
			{
				return false;
			}

			// ALL matches all level, otherwise other levels are exclusive.
			return (Level.getLevel(logLevel) == Level.ALL) ? true : (Level.getLevel(logLevel) == Level.getLevel(line.getLevel()));
		}
	}

}