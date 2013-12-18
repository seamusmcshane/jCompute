package alifeSim.Gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JButton;

import alifeSim.Stats.StatManager;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SimulationStatsListPanel extends JPanel implements ActionListener
{
	private static final long serialVersionUID = 3144580494990559283L;

	private SimTablePanel table;
	
	private JButton btnExportStats;
	
	private StatManager	statManager;
	
	public SimulationStatsListPanel()
	{		
		setLayout(new BorderLayout(0, 0));
		this.setMinimumSize(new Dimension(350,250));
		
		table = new SimTablePanel("Statistics",new String[]{"Group Name","Sample Categories","Enabled","Graph"});
		
		this.add(table);
		
		JPanel panel = new JPanel();
		table.add(panel, BorderLayout.SOUTH);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.rowHeights = new int[] {20};
		gbl_panel.columnWidths = new int[] {150, 150, 150};
		gbl_panel.columnWeights = new double[]{1.0, 1.0, 1.0};
		gbl_panel.rowWeights = new double[]{1.0};
		panel.setLayout(gbl_panel);
		
		JPanel fillerPanel1 = new JPanel();
		GridBagConstraints gbc_fillerPanel1 = new GridBagConstraints();
		gbc_fillerPanel1.insets = new Insets(0, 0, 0, 5);
		gbc_fillerPanel1.gridx = 0;
		gbc_fillerPanel1.gridy = 0;
		panel.add(fillerPanel1, gbc_fillerPanel1);
		
		JPanel panel_1 = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.insets = new Insets(0, 0, 0, 5);
		gbc_panel_1.gridx = 1;
		gbc_panel_1.gridy = 0;
		panel.add(panel_1, gbc_panel_1);
		btnExportStats = new JButton("Export Statistics");
		btnExportStats.addActionListener(this);
		btnExportStats.setEnabled(false);
		
		GridBagConstraints gbc_btnExportStats = new GridBagConstraints();
		gbc_btnExportStats.fill = GridBagConstraints.BOTH;
		gbc_btnExportStats.gridx = 2;
		gbc_btnExportStats.gridy = 0;
		panel.add(btnExportStats, gbc_btnExportStats);
	}
	
	public void clearTable()
	{
		table.clearTable();
	}
	
	public void addRow(String rowKey,String columnValues[])
	{
		table.addRow(rowKey, columnValues);
	}

	public void setStatManager(StatManager statManager)
	{
		this.statManager = statManager;
	}
	public void setExportEnabled(boolean status)
	{
		btnExportStats.setEnabled(status);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == btnExportStats)
		{
			if(statManager!=null)
			{
				String directory = chooseExportDirectory();
				
				if(!directory.equals("CANCELLED"))
				{
					statManager.exportStats(directory);
					
					System.out.println(directory);
				}

			}
		}
	
	}	
	
	private String chooseExportDirectory()
	{
		System.out.println("Choose Export Directory");

		String exportDirectory = "";
		
		final JFileChooser filechooser = new JFileChooser(new File("./stats"));

		filechooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		filechooser.setDialogTitle("Choose Export Directory");
		
		filechooser.setSelectedFile(new File("./"));

		int val = filechooser.showSaveDialog(filechooser);

		if (val == JFileChooser.APPROVE_OPTION)
		{

			try
			{
				exportDirectory = filechooser.getSelectedFile().getCanonicalPath();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

		}
		else
		{
			System.out.println("Export Cancelled");
			
			exportDirectory = "CANCELLED";
		}

		return exportDirectory;
	}
	
}
