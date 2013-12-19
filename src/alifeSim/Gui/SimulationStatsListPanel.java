package alifeSim.Gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.filechooser.FileFilter;

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
				chooseExport();
			}
		}
	
	}	
	
	private void chooseExport()
	{
		System.out.println("Choose Export Directory");

		String exportDirectory = "";
		String fileFormat = "";
		
		JFileChooser filechooser = new JFileChooser(new File("./stats"));

		filechooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		filechooser.setDialogTitle("Choose Export Directory");
		
		filechooser.setSelectedFile(new File("./"));
		
		// Allowable file formats
		filechooser.setAcceptAllFileFilterUsed(false);

		// Comma-Separated Values
		filechooser.addChoosableFileFilter(new ExportFileFilter("csv","Comma-Separated Values") );

		// Attribute-Relation File Format (WEKA)
		filechooser.addChoosableFileFilter(new ExportFileFilter("arff","Attribute-Relation File Format") );
		
		int val = filechooser.showSaveDialog(filechooser);

		if (val == JFileChooser.APPROVE_OPTION)
		{
			try
			{
				exportDirectory = filechooser.getSelectedFile().getCanonicalPath();
				fileFormat = filechooser.getFileFilter().getDescription();
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

		// Export File format
		if(!exportDirectory.equals("CANCELLED"))
		{
			System.out.println("Directory Choosen : " + exportDirectory);

			if(fileFormat.equals("Comma-Separated Values"))
			{
				statManager.exportStatsToCSV(exportDirectory);
			}
			else if(fileFormat.equals("Attribute-Relation File Format"))
			{
				statManager.exportStatsToARFF(exportDirectory);
			}
			else
			{
				System.out.println(fileFormat + " Not Implemented");
			}			

		}

	}
	
	private class ExportFileFilter extends FileFilter 
	{
		String extension;
		String description;
		
		public ExportFileFilter(String extension, String description)
		{
			this.extension = extension;
			this.description = description;
		}
		
		@Override
		public boolean accept(File file)
		{
			return file.getName().toLowerCase().endsWith(extension) || file.isDirectory();
		}

		@Override
		public String getDescription()
		{
			return description;
		}
		
	}
	
}
