package jcompute.gui.cluster.tab;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker.StateValue;

import com.DaveKoelle.AlphanumFileNameComparator;
import com.google.common.io.Files;

import jcompute.gui.component.swing.MessageBox;
import jcompute.gui.component.swing.jpanel.GlobalProgressMonitor;
import jcompute.gui.component.swing.jpanel.SimpleTabPanel;
import jcompute.gui.component.swing.swingworker.Loadable;
import jcompute.gui.component.swing.swingworker.LoadableTask;
import jcompute.util.FileUtil;

public class BenchmarkTab extends JPanel implements Loadable, PropertyChangeListener
{
	private static final long serialVersionUID = -6582518152127005845L;
	
	private final String path = "benchmark";
	
	private ArrayList<JComponent> clist;
	private ArrayList<String> list;
	private int fileCount;
	
	// Loadable
	private int[] indexes;
	private String[] filePaths;
	
	// To avoid identical code we delegate our loaded to another loadable
	private Loadable delegator;
	
	// A reference outside of thread context.
	private BenchmarkTab self;
	
	// The container tabpanel
	private SimpleTabPanel tabpanel;
	
	public BenchmarkTab(Loadable loadable, SimpleTabPanel tabpanel)
	{
		this.delegator = loadable;
		this.tabpanel = tabpanel;
		self = this;
		
		// We require a delegate as we cannot not do loading ourself.
		if(!delegator.makeDelegate(self))
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					selfClose();
					
					MessageBox.popup("Benchmarks not available at this time", self);
				}
			});
			
			return;
		}
		
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
		
		addTitlePanel(bench, self, tabpanel);
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
				selfClose();
			}
		});
		
		JButton start = new JButton("Start");
		start.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(delegator == null)
				{
					return;
				}
				
				int benchmarkTotal = list.size();
				
				if(benchmarkTotal < 0)
				{
					return;
				}
				
				filePaths = new String[benchmarkTotal];
				indexes = new int[benchmarkTotal];
				
				// Loop over all the choosen benchmarks in the list (there are allow to be multiple duplicates)
				for(int b = 0; b < benchmarkTotal; b++)
				{
					// Find out which filename they match
					for(File file : files)
					{
						String name = list.get(b);
						String fileName = benchmarkStringTrim(file.getName());
						
						if(name.equals(fileName))
						{
							// Add the path to the list
							filePaths[b] = file.getAbsolutePath();
							
							// Set the index order (sequential)
							indexes[b] = b;
							
							break;
						}
					}
				}
				
				// BenchmarkTab is the "Loadable" here to avoid swing worker(GUI) related code in the batch manager.
				LoadableTask task = new LoadableTask(self);
				
				// Send progress to the GlobalProgressMonitor
				task.addPropertyChangeListener(GlobalProgressMonitor.getInstance());
				
				// BenchmarkTab needs state to auto close
				task.addPropertyChangeListener(self);
				
				task.execute();
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
	
	private void selfClose()
	{
		// Remove current batch list
		if(list != null)
		{
			list.clear();
			
			list = null;
			
			clist.clear();
			
			clist = null;
			
			indexes = null;
			
			filePaths = null;
		}
		
		// Loaders must release any delegates.
		delegator.releaseDelegate(self);
		
		// Remove
		tabpanel.removeTab(self);
		tabpanel.setSelectedTab(0);
	}
	
	private void addTitlePanel(JPanel panel, BenchmarkTab self, SimpleTabPanel tabpanel)
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
	
	@Override
	public void propertyChange(PropertyChangeEvent e)
	{
		Common.handlePropertyChangeEvent(self, e, indexes, filePaths);
		
		// Interested the state changing
		if("state".equals(e.getPropertyName()))
		{
			// Use the properties state.
			StateValue state = (StateValue) e.getNewValue();
			
			// Is the state done
			if(state == StateValue.DONE)
			{
				selfClose();
			}
		}
	}
	
	@Override
	public boolean load(int index)
	{
		return delegator.delegateLoad(self, filePaths[index]);
	}
	
	@Override
	public int[] getIndexes()
	{
		return indexes;
	}
	
	@Override
	public boolean makeDelegate(Loadable requester)
	{
		// Never delegate
		return false;
	}
	
	@Override
	public boolean delegateLoad(Loadable requester, String info)
	{
		// Always fail as we do not delegate
		return false;
	}
	
	@Override
	public void releaseDelegate(Loadable requester)
	{
		// NOP
	}
}
