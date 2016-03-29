package jCompute.Gui.Cluster.Tab;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.DaveKoelle.AlphanumFileNameComparator;
import com.google.common.io.Files;

import jCompute.Cluster.BatchManager.BatchManager;
import jCompute.Gui.Component.Swing.JComputeProgressMonitor;
import jCompute.Gui.Component.Swing.OpenBatchFileTask;
import jCompute.Gui.Component.Swing.SimpleTabPanel;
import jCompute.util.FileUtil;

public class BenchmarkTab extends JPanel
{
	private static final long serialVersionUID = -6582518152127005845L;

	private final String path = "benchmark";

	private ArrayList<JComponent> clist;
	private ArrayList<String> list;
	private int fileCount;

	public BenchmarkTab(BatchManager batchManager, SimpleTabPanel tabpanel)
	{
		BenchmarkTab self = this;

		setLayout(new BorderLayout());

		// The Benchmark panel
		JPanel bench = new JPanel();
		bench.setMinimumSize(new Dimension(500, 300));
		bench.setPreferredSize(new Dimension(500, 300));
		bench.setMaximumSize(new Dimension(500, 300));

		// Align in tab to top left
		JPanel benchContainer = new JPanel(new BorderLayout());
		benchContainer.add(bench, BorderLayout.WEST);
		this.add(benchContainer, BorderLayout.NORTH);

		File[] files = FileUtil.getFilesInDir(path);

		// Sort Files Alpha Numerically by FileName
		Arrays.sort(files, new AlphanumFileNameComparator());

		fileCount = files.length;

		list = new ArrayList<String>();
		clist = new ArrayList<JComponent>();

		bench.setLayout(new GridLayout((fileCount * 2) + 2, 0, 0, 0));

		addPadPanel(bench);

		addTitlePanel(bench, batchManager, self, tabpanel);
		addPadPanel(bench);

		for(File file : files)
		{
			if(Files.getFileExtension(file.getName()).equals("batch"))
			{
				addTestOption(benchmarkStringTrim(file.getName()), bench);
			}
		}

		JButton close = new JButton("Close");
		close.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				close(tabpanel, self);
			}
		});

		JButton start = new JButton("Start");
		start.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				JComputeProgressMonitor openBatchProgressMonitor = new JComputeProgressMonitor(bench, "Adding Benchmark Batches", 0, 100);

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

					if(batchManager != null)
					{
						OpenBatchFileTask openBatchProgressMonitorTask = new OpenBatchFileTask(bench, openBatchProgressMonitor, batchManager, fileList);
						openBatchProgressMonitorTask.start();
					}

					close(tabpanel, self);
				}
			}
		});

		addPadPanel(bench);
		JPanel container = new JPanel();
		bench.add(container);
		container.setLayout(new GridLayout(0, 7, 0, 0));

		addPadPanel(container);
		addPadPanel(container);
		addPadPanel(container);
		addPadPanel(container);
		container.add(close);
		container.add(start);
		addPadPanel(container);
		bench.add(container);
	}

	private void close(SimpleTabPanel tabpanel, BenchmarkTab self)
	{
		// Remove current batch list
		list.clear();

		list = null;

		clist.clear();

		clist = null;

		// Remove
		tabpanel.removeTab(self);
		tabpanel.setSelectedTab(0);
	}

	private void addTitlePanel(JPanel panel, BatchManager batchManager, BenchmarkTab self, SimpleTabPanel tabpanel)
	{
		JPanel container = new JPanel();
		panel.add(container);
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
			@Override
			public void actionPerformed(ActionEvent e)
			{
				// Remove current batch list
				list.clear();

				// Renable all combo boxes and buttons
				for(JComponent comp : clist)
				{
					comp.setEnabled(true);
				}
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

	private void addTestOption(String name, JPanel panel)
	{
		JPanel container = new JPanel();
		panel.add(container);
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
			@Override
			public void actionPerformed(ActionEvent e)
			{
				int size = (int) comboBox.getSelectedItem();
				for(int i = 1; i <= size; i++)
				{
					list.add(name);
				}

				comboBox.setEnabled(false);
				buttton.setEnabled(false);

				// For reset button
				clist.add(comboBox);
				clist.add(buttton);
			}
		});
		container.add(buttton);
	}

	private String benchmarkStringTrim(String batchName)
	{
		return new String(batchName.substring(batchName.indexOf('-') + 1, batchName.lastIndexOf('.')));
	}
}
