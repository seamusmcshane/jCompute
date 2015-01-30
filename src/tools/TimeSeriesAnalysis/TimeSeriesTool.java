package tools.TimeSeriesAnalysis;

import jCompute.Stats.Logs.CSVLogParser;
import jCompute.Stats.Trace.SingleStat;
import jCompute.Stats.Trace.StatSample;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import math.jwave.Transform;
import math.jwave.transforms.AncientEgyptianDecomposition;
import math.jwave.transforms.DiscreteFourierTransform;

import org.jtransforms.fft.DoubleFFT_1D;

import javax.swing.JPanel;

import java.awt.GridBagLayout;

import javax.swing.JList;

import java.awt.GridBagConstraints;

import javax.swing.ListSelectionModel;
import javax.swing.JButton;

import java.awt.Insets;

import javax.swing.JTextField;

public class TimeSeriesTool implements WindowListener, ActionListener
{
	private static JFrame gui;
	private JMenuItem mntmOpen;
	private SingleStatChartPanel chart;
	private JMenuItem mntmComputeFft;
	private StatSample[][] histories;
	private String[] names;
	private JPanel toolPanel;
	private JList historyList;
	private DefaultListModel historyListModel;
	private JButton btnDft;
	private JPanel panel;
	private JButton btnPhase;
	
	public static void main(String args[])
	{

		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				new TimeSeriesTool();
			}
		});
	}

	public TimeSeriesTool()
	{
		gui = new JFrame();
		gui.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		gui.setPreferredSize(new Dimension(1800, 600));
		gui.setMinimumSize(new Dimension(1800, 600));

		gui.getContentPane().setLayout(new BorderLayout());
		
		toolPanel = new JPanel();
		toolPanel.setPreferredSize(new Dimension(200, 600));
		toolPanel.setMinimumSize(new Dimension(200, 600));
		gui.getContentPane().add(toolPanel, BorderLayout.EAST);
		GridBagLayout gbl_toolPanel = new GridBagLayout();
		gbl_toolPanel.columnWidths = new int[]{0, 0};
		gbl_toolPanel.rowHeights = new int[] {0, 0};
		gbl_toolPanel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_toolPanel.rowWeights = new double[]{1.0, 1.0};
		toolPanel.setLayout(gbl_toolPanel);
		
		historyListModel = new DefaultListModel();
		historyList = new JList(historyListModel);
		GridBagConstraints gbc_historyList = new GridBagConstraints();
		gbc_historyList.insets = new Insets(0, 0, 5, 0);
		gbc_historyList.fill = GridBagConstraints.BOTH;
		gbc_historyList.gridx = 0;
		gbc_historyList.gridy = 0;
		toolPanel.add(historyList, gbc_historyList);
		
		panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 1;
		toolPanel.add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0};
		gbl_panel.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		btnDft = new JButton("DFT");
		GridBagConstraints gbc_btnDft = new GridBagConstraints();
		gbc_btnDft.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnDft.insets = new Insets(0, 0, 5, 5);
		gbc_btnDft.gridx = 0;
		gbc_btnDft.gridy = 0;
		panel.add(btnDft, gbc_btnDft);
		
		btnPhase = new JButton("Phase");
		btnPhase.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				int indicies[] = historyList.getSelectedIndices();
				
				if(indicies.length == 2)
				{
					int index1 = indicies[0];
					int index2 = indicies[1];
					if( index1 >= 0 && index2 >= 0)
					{
						int len = histories[0].length;
						
						double[] array1 = new double[len];
						
						for(int i = 0; i < len; i++)
						{
							array1[i] = histories[index1][i].getSample();
						}
						
						
						double[] array2 = new double[len];
						
						for(int i = 0; i < len; i++)
						{
							array2[i] = histories[index2][i].getSample();
						}
						
						String arrayLabels[] = new String[]{names[index1],names[index2]};
						
						PhaseTool phasePlot = new PhaseTool(arrayLabels,array1,array2);
					}
				}
				
			}
		});
		GridBagConstraints gbc_btnPhase = new GridBagConstraints();
		gbc_btnPhase.insets = new Insets(0, 0, 5, 0);
		gbc_btnPhase.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnPhase.gridx = 1;
		gbc_btnPhase.gridy = 0;
		panel.add(btnPhase, gbc_btnPhase);
		
		GridBagConstraints gbc_btnPoinecare = new GridBagConstraints();
		gbc_btnPoinecare.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnPoinecare.insets = new Insets(0, 0, 0, 5);
		gbc_btnPoinecare.gridx = 0;
		gbc_btnPoinecare.gridy = 1;
		btnDft.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) 
			{
				int indicies[] = historyList.getSelectedIndices();
				
				if(indicies.length == 1)
				{
					int index = indicies[0];
					if( index >= 0)
					{
						computeAndDisplayFFT(index);
						
					}
				}

			}
		});

		JMenuBar menuBar = new JMenuBar();
		gui.setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		mntmOpen = new JMenuItem("Open");
		mntmOpen.addActionListener(this);
		mnFile.add(mntmOpen);

		JMenu mnTools = new JMenu("Tools");
		menuBar.add(mnTools);

		mntmComputeFft = new JMenuItem("Compute FFT");
		mntmComputeFft.addActionListener(this);
		mnTools.add(mntmComputeFft);

		gui.setVisible(true);

		gui.addWindowListener(this);
		
		// SINE WAVE

        // Sine Wave Frequency
        int hertz = 10;  
        
        int cycles = 1;
        int overSample = 30;
        
        // Nyquist
        int sampleRate = hertz*overSample;
        
        int timeAxis = (sampleRate/2)+1;
        
        double test = cycles/1.0/hertz;
        double test2 = 1.0/sampleRate;
        double test3 = test*test2;
        
        System.out.println(timeAxis);    
        System.out.println(test);            
        System.out.println(test2);            
        System.out.println(test3);
        // 
        //float xIncr = (float) (1f/3f*Math.PI)*cycles;
        //float xIncr = -((length/(float)timeAxis)*hertz);
        
        
        double time = 0;
        //double dt = 1f/(float)timeAxis;
        //double dt = (1.0/(double)sampleRate)*((1.0/(double)hertz)*(double)cycles);
        
        //double dt = 1.0/(double)sampleRate*(double)cycles*1.0/(double)hertz;
        double dt = (1.0/sampleRate);
                
        
        double amplitude = 0;
        
        
        
        StatSample samples[] = new StatSample[timeAxis];

        
        int inc = 0;
        for( int i=0; i < timeAxis; i++ )
        {
        	amplitude = Math.sin(2.0*Math.PI*hertz*time);

        	samples[i] = new StatSample(time, amplitude);
        	System.out.println("i " + i + " Time : " + time);
        	time=time+(dt);

        }
        
		gui.setTitle("Sine Wave");

		int numSamples = timeAxis;

		names = new String[1];
		histories = new StatSample[1][];

		System.out.println("Got Arrays");

		for(int st = 0; st < 1; st++)
		{
			names[st] = new String("Sine Wave");
			historyListModel.addElement(names[st]);
			histories[st] = samples;
		}

		if(chart != null)
		{
			gui.getContentPane().remove(chart);
			chart = null;
		}

		chart = new SingleStatChartPanel("Sine Wave", "Sine Wave", true, false, numSamples);

		chart.populate(numSamples, names, histories);

		gui.getContentPane().add(chart, BorderLayout.CENTER);

		gui.validate();
	}

	@Override
	public void windowActivated(WindowEvent arg0)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void windowClosed(WindowEvent arg0)
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void windowClosing(WindowEvent arg0)
	{
		doProgramExit();
	}

	@Override
	public void windowDeactivated(WindowEvent arg0)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeiconified(WindowEvent arg0)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void windowIconified(WindowEvent arg0)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void windowOpened(WindowEvent arg0)
	{
		// TODO Auto-generated method stub

	}

	/* Ensure the user wants to exit then exit the program */
	private void doProgramExit()
	{
		javax.swing.SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				String message;
				message = "Do you want to quit?";

				JOptionPane pane = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);

				// Center Dialog on the GUI
				JDialog dialog = pane.createDialog(gui, "Close Application");

				dialog.pack();
				dialog.setVisible(true);

				int value = ((Integer) pane.getValue()).intValue();

				if(value == JOptionPane.YES_OPTION)
				{
					// Do EXIT
					System.exit(0);
				}
			}
		});

	}

	@Override
	public void actionPerformed(ActionEvent e)
	{

		if(e.getSource() == mntmOpen)
		{
			final JFileChooser filechooser = new JFileChooser(new File("./stats"));

			System.out.println("Open Dialog");

			int val = filechooser.showOpenDialog(filechooser);

			if(val == JFileChooser.APPROVE_OPTION)
			{

				System.out.println("New File Choosen");
				historyListModel.clear();

				String file = filechooser.getSelectedFile().getAbsolutePath();

				String fname = filechooser.getSelectedFile().getName();
				
				gui.setTitle(fname);

				System.out.println(file);

				CSVLogParser parser = new CSVLogParser(file);

				ArrayList<SingleStat> stats = parser.getStats();
				int numSamples = parser.getSampleNum();

				names = new String[stats.size()];
				histories = new StatSample[stats.size()][];

				System.out.println("Got Arrays");

				for(int st = 0; st < stats.size(); st++)
				{
					SingleStat temp = stats.get(st);
					names[st] = new String(temp.getStatName());
					historyListModel.addElement(names[st]);
					histories[st] = temp.getHistoryAsArray();
				}

				if(chart != null)
				{
					gui.getContentPane().remove(chart);
					chart = null;
				}

				chart = new SingleStatChartPanel(fname, fname, true, false, 100000);

				chart.populate(numSamples, names, histories);

				gui.getContentPane().add(chart, BorderLayout.CENTER);

				gui.validate();
				
			}
		}
		else if(e.getSource() == mntmComputeFft)
		{
			computeAndDisplayFFT(0);
		}
	}

	private void computeAndDisplayFFT(int num)
	{
		System.out.println("Compute FFT");

		String name = names[num];		
		
		int len = (histories[num].length);	
		
		/*double[] array = new double[len*2];
		
		for(int i = 0; i < len; i=i+2)
		{
			array[i] = histories[num][i].getSample();
			array[i+1]=0;
		}*/
		
		double[] array = new double[len*2];
		
		for(int i = 0; i < len; ++i)
		{
			array[2*i] = histories[num][i].getSample();
			array[2*i+1]=0;
		}

		/*System.out.println("Data");
		for(int i = 0; i < len; i++)
		{
			System.out.println(array[i]);
		}*/

		// Lenght of the first history
		DoubleFFT_1D fft = new DoubleFFT_1D(len);

		//fft.realForwardFull(array);
		fft.complexForward(array);

		/*System.out.println("Result");
		for(int i = 0; i < len; i++)
		{
			System.out.println(array[i]);
		}*/

		final JFrame results = new JFrame();
		results.getContentPane().setLayout(new BorderLayout());
		
		SingleStatChartPanel freq = new SingleStatChartPanel(name,name,true,false,len*2);
		
		/*
		for(int i = 0; i < len*2; i++)
		{
			array[i] = Math.abs(array[i]);			
		}
		*/
		
		double[] array2 = new double[len];

		int arr2 = 0;
		
		for(int i = 0; i < len*2; i+=2)
		{
			System.out.println(Math.abs(array[i]));
			array2[arr2] = (Math.abs(array[i]));
			
			arr2++;
		}
		
		freq.populateFFT("Frequency",array2);
		//freq.populateFFTShift("Frequency",array2);
		
		results.getContentPane().add(freq,BorderLayout.CENTER);
		results.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		results.setPreferredSize(new Dimension(1800, 600));
		results.setMinimumSize(new Dimension(1800, 600));

		results.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent ev)
			{
				results.setVisible(false);
				results.dispose();
			}
		});
		results.setVisible(true);
	}
	
}
