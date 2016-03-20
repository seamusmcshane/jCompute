package jCompute.Gui.Component.Swing;

import java.awt.Dimension;
import java.awt.Dialog.ModalExclusionType;

import javax.swing.JFrame;

import com.DaveKoelle.AlphanumFileNameComparator;
import com.google.common.io.Files;

import jCompute.Batch.BatchManager.BatchManager;
import jCompute.util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JLabel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import java.awt.GridLayout;
import java.awt.Font;
import javax.swing.SwingConstants;

public class BenchmarkWindow extends JFrame
{
	private static final long serialVersionUID = -6582518152127005845L;
	
	private final String path = "benchmark";
	private ArrayList<String> list;
	private int fileCount;
	
	public static void main(String args[])
	{
		BenchmarkWindow test = new BenchmarkWindow(null);
		
		test.pack();
		test.setLocationRelativeTo(null);
		test.setVisible(true);
	}
	
	public BenchmarkWindow(BatchManager batchManager)
	{
		BenchmarkWindow self = this;
		
		setResizable(false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setType(Type.NORMAL);
		
		setTitle("jCompute");
		setMinimumSize(new Dimension(600, 150));
		setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
		
		File[] files = FileUtil.getFilesInDir(path);
		
		// Sort Files Alpha Numerically by FileName
		Arrays.sort(files, new AlphanumFileNameComparator());
		
		fileCount = files.length;
		
		list = new ArrayList<String>();
		
		getContentPane().setLayout(new GridLayout((fileCount * 2) + 2, 0, 0, 0));
		
		addPadPanel((JPanel) getContentPane());
		
		addTitlePanel(this, batchManager);
		addPadPanel((JPanel) getContentPane());
		
		for(File file : files)
		{
			if(Files.getFileExtension(file.getName()).equals("batch"))
			{
				addTestOption(benchmarkStringTrim(file.getName()));
			}
		}
		
		JButton start = new JButton("Start");
		start.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				JComputeProgressMonitor openBatchProgressMonitor = new JComputeProgressMonitor(getContentPane(), "Adding Benchmark Batches", 0, 100);
				
				int size = list.size();
				
				File[] fileList = new File[size];
				
				if(size > 0)
				{
					for(int f = 0; f < size; f++)
					{
						for(File file : files)
						{
							String name = list.get(f);
							String fileName = benchmarkStringTrim(file.getName());
							
							if(name.equals(fileName))
							{
								fileList[f] = file;
							}
						}
					}
					
					self.dispatchEvent(new WindowEvent(self, WindowEvent.WINDOW_CLOSING));
					
					if(batchManager != null)
					{
						OpenBatchFileTask openBatchProgressMonitorTask = new OpenBatchFileTask(getContentPane(), openBatchProgressMonitor, batchManager, fileList);
						openBatchProgressMonitorTask.start();
					}
				}
			}
		});
		
		addPadPanel((JPanel) getContentPane());
		JPanel container = new JPanel();
		getContentPane().add(container);
		container.setLayout(new GridLayout(0, 7, 0, 0));
		
		addPadPanel(container);
		addPadPanel(container);
		addPadPanel(container);
		addPadPanel(container);
		addPadPanel(container);
		container.add(start);
		addPadPanel(container);
		getContentPane().add(container);
		
	}
	
	private void addTitlePanel(BenchmarkWindow benchmarkWindow, BatchManager batchManager)
	{
		JPanel container = new JPanel();
		getContentPane().add(container);
		container.setLayout(new GridLayout(0, 7, 0, 0));
		
		addPadPanel(container);
		
		JLabel label = new JLabel("Test Name");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setFont(label.getFont().deriveFont(label.getFont().getStyle() | Font.BOLD));
		container.add(label);
		
		addPadPanel(container);
		
		JLabel label_2 = new JLabel("Test Count");
		label_2.setHorizontalAlignment(SwingConstants.CENTER);
		label_2.setFont(label_2.getFont().deriveFont(label_2.getFont().getStyle() | Font.BOLD));
		container.add(label_2);
		
		addPadPanel(container);
		JButton buttton = new JButton("Reset");
		buttton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				BenchmarkWindow reset = new BenchmarkWindow(batchManager);
				reset.pack();
				reset.setLocationRelativeTo(benchmarkWindow);
				reset.setVisible(true);
				
				benchmarkWindow.dispatchEvent(new WindowEvent(benchmarkWindow, WindowEvent.WINDOW_CLOSING));
			}
		});
		container.add(buttton);
		
		addPadPanel(container);
		
	}
	
	private void addPadPanel(JPanel container)
	{
		JLabel pad1 = new JLabel("");
		pad1.setHorizontalAlignment(SwingConstants.CENTER);
		container.add(pad1);
	}
	
	private void addTestOption(String name)
	{
		JPanel container = new JPanel();
		getContentPane().add(container);
		container.setLayout(new GridLayout(0, 7, 0, 0));
		
		addPadPanel(container);
		
		JLabel label = new JLabel(name);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setFont(label.getFont().deriveFont(label.getFont().getStyle() | Font.BOLD));
		container.add(label);
		
		addPadPanel(container);
		
		JComboBox<Integer> comboBox = new JComboBox<Integer>();
		comboBox.addItem(0);
		comboBox.addItem(1);
		comboBox.addItem(2);
		comboBox.addItem(3);
		comboBox.addItem(4);
		comboBox.addItem(5);
		comboBox.addItem(10);
		comboBox.addItem(20);
		comboBox.addItem(50);
		container.add(comboBox);
		
		comboBox.setSelectedIndex(4);
		
		addPadPanel(container);
		
		JButton buttton = new JButton("Add");
		buttton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				int size = (int) comboBox.getSelectedItem();
				for(int i = 1; i <= size; i++)
				{
					list.add(name);
				}
				
				comboBox.setEnabled(false);
				buttton.setEnabled(false);
			}
		});
		container.add(buttton);
	}
	
	private String benchmarkStringTrim(String batchName)
	{
		return new String(batchName.substring(batchName.indexOf('-') + 1, batchName.lastIndexOf('.')));
	}
}
