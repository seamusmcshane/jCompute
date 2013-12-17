package alifeSim.Gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;

public class SimulationStatsListPanel extends JPanel
{
	private static final long serialVersionUID = 3144580494990559283L;

	private SimTablePanel table;
	
	public SimulationStatsListPanel()
	{
		setLayout(new BorderLayout(0, 0));
		this.setMinimumSize(new Dimension(350,250));
		
		table = new SimTablePanel("Statistics",new String[]{"Group Name","Sample Categories","Enabled","Graph"});
		
		this.add(table);
	}
	
	public void clearTable()
	{
		table.clearTable();
	}
	
	public void addRow(String rowKey,String columnValues[])
	{
		table.addRow(rowKey, columnValues);
	}
	
}
