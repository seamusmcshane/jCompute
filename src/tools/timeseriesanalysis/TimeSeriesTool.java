package tools.timeseriesanalysis;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerListModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.fftw3;
import org.bytedeco.javacpp.fftw3.fftw_plan;

import jcompute.logging.Logging;
import jcompute.results.logparser.CSV;
import jcompute.results.trace.Trace;
import jcompute.results.trace.samples.DoubleTraceSample;
import jcompute.results.trace.samples.TraceSample;
import jcompute.util.LookAndFeel;
import tools.phaseplot3d.PhasePlotterUtil;

public class TimeSeriesTool implements ActionListener, MouseListener
{
	// Log4j2 Logger
	private static Logger log;
	
	private JFrame gui;
	private JPanel centerPanel;
	private JPanel rightPanel;
	
	private JMenuItem mntmOpen;
	private SingleStatChartPanel chart;
	private DoubleTraceSample[][] histories;
	private String[] names;
	private JPanel toolPanel;
	private JList<String> historyList;
	private DefaultListModel<String> historyListModel;
	private JButton btnDft;
	private JPanel panel;
	private JButton btnPhase;
	private double sampleWindow = 0;
	private double timePeriod = 1;
	private boolean tIsSeconds = false;
	private JScrollPane scrollPane;
	private JPanel fftListPanel;
	
	private JLabel lblMaxFreq;
	private JTextField txtMaxFreq;
	private JLabel lblMaxAmp;
	private JTextField txtMaxAmp;
	private JCheckBox chckbxAvgFilter;
	
	private boolean avgFilter = false;
	private JSpinner spinner;
	
	private PhasePlotterUtil pp3d;
	
	private RecurrencePlot rp;
	private JButton btnRecurrence;
	private JTextField txtRadius;
	private JCheckBox chckbxColoured;
	private JComboBox<String> comboBoxOverSample;
	private JLabel lblOverSampledImage;
	
	private JFrame largeRP;
	
	public static void main(String args[])
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			@SuppressWarnings("unused")
			@Override
			public void run()
			{
				new TimeSeriesTool();
			}
		});
	}
	
	public TimeSeriesTool()
	{
		Logging.initTestLevelLogging();
		
		log = LogManager.getLogger(TimeSeriesTool.class);
		
		LookAndFeel.setLookandFeel("default");
		
		// Frame
		gui = new JFrame();
		gui.getContentPane().setLayout(new BorderLayout());
		gui.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		gui.setExtendedState(gui.getExtendedState() | Frame.MAXIMIZED_BOTH);
		/*
		 * gui.setPreferredSize(new Dimension(windowWidth, windowheight));
		 * gui.setMinimumSize(new Dimension(windowWidth, windowheight));
		 */
		
		// Base Panel
		centerPanel = new JPanel();
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWeights = new double[]
		{
			1.0
		};
		gridBagLayout.rowWeights = new double[]
		{
			0.5, 0.5
		};
		centerPanel.setLayout(gridBagLayout);
		
		gui.getContentPane().add(centerPanel, BorderLayout.CENTER);
		
		// Right Panel
		rightPanel = new JPanel();
		rightPanel.setLayout(new BorderLayout());
		
		rp = new RecurrencePlot(500);
		rp.addMouseListener(this);
		rp.setMinimumSize(new Dimension(500, 500));
		rp.setPreferredSize(new Dimension(500, 500));
		
		gui.getContentPane().add(rightPanel, BorderLayout.EAST);
		rightPanel.add(rp, BorderLayout.SOUTH);
		
		toolPanel = new JPanel();
		/*
		 * toolPanel.setPreferredSize(new Dimension(sidePanelWidth,
		 * sidePanelHeight));
		 * toolPanel.setMinimumSize(new Dimension(sidePanelWidth,
		 * sidePanelHeight));
		 */
		rightPanel.add(toolPanel, BorderLayout.NORTH);
		
		GridBagLayout gbl_toolPanel = new GridBagLayout();
		gbl_toolPanel.columnWeights = new double[]
		{
			1.0
		};
		gbl_toolPanel.rowWeights = new double[]
		{
			1.0, 1.0
		};
		toolPanel.setLayout(gbl_toolPanel);
		historyListModel = new DefaultListModel<String>();
		historyList = new JList<String>(historyListModel);
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
		gbl_panel.columnWidths = new int[]
		{
			0, 0, 0
		};
		gbl_panel.rowHeights = new int[]
		{
			0, 0, 0, 0, 0, 0, 0, 0, 0
		};
		gbl_panel.columnWeights = new double[]
		{
			1.0, 1.0, Double.MIN_VALUE
		};
		gbl_panel.rowWeights = new double[]
		{
			1.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE
		};
		panel.setLayout(gbl_panel);
		
		btnDft = new JButton("DFT");
		GridBagConstraints gbc_btnDft = new GridBagConstraints();
		gbc_btnDft.fill = GridBagConstraints.BOTH;
		gbc_btnDft.insets = new Insets(0, 0, 5, 5);
		gbc_btnDft.gridx = 0;
		gbc_btnDft.gridy = 0;
		panel.add(btnDft, gbc_btnDft);
		
		btnPhase = new JButton("Phase");
		btnPhase.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				final int indicies[] = historyList.getSelectedIndices();
				
				if(indicies.length == 2)
				{
					int index1 = indicies[0];
					int index2 = indicies[1];
					if((index1 >= 0) && (index2 >= 0))
					{
						int len = histories[0].length;
						
						double[] array1 = new double[len];
						
						for(int i = 0; i < len; i++)
						{
							array1[i] = histories[index1][i].value;
						}
						
						double[] array2 = new double[len];
						
						for(int i = 0; i < len; i++)
						{
							array2[i] = histories[index2][i].value;
						}
						
						String arrayLabels[] = new String[]
						{
							names[index1], names[index2]
						};
						
						PhaseTool phasePlot = new PhaseTool(arrayLabels, array1, array2);
						
						phasePlot.display();
					}
				}
				if((indicies.length == 3) || (histories.length >= 3))
				{
					if(pp3d != null)
					{
						pp3d.close();
					}
					
					final int index1 = indicies == null ? indicies[0] : 0;
					final int index2 = indicies == null ? indicies[0] : 1;
					final int index3 = indicies == null ? indicies[0] : 2;
					
					javax.swing.SwingUtilities.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							final String arrayLabels[] = new String[]
							{
								names[index1], names[index2], names[index3]
							};
							
							int statsLenght = 3;
							int samples = histories[0].length;
							
							final float[][] data = new float[3][samples];
							
							for(int st = 0; st < statsLenght; st++)
							{
								for(int sam = 0; sam < samples; sam++)
								{
									// OpenGL uses floats
									data[st][sam] = (float) histories[st][sam].value;
								}
							}
							
							pp3d = new PhasePlotterUtil(false);
							
							javax.swing.SwingUtilities.invokeLater(new Runnable()
							{
								@Override
								public void run()
								{
									pp3d.loadData(data, arrayLabels);
								}
							});
						}
						
					});
					
				}
				
			}
		});
		
		chckbxAvgFilter = new JCheckBox("Avg Filter");
		GridBagConstraints gbc_chckbxAvgFilter = new GridBagConstraints();
		gbc_chckbxAvgFilter.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxAvgFilter.gridx = 0;
		gbc_chckbxAvgFilter.gridy = 1;
		panel.add(chckbxAvgFilter, gbc_chckbxAvgFilter);
		chckbxAvgFilter.addActionListener(this);
		
		spinner = new JSpinner();
		spinner.setModel(new SpinnerListModel(new String[]
		{
			"1", "4", "10", "100", "1000"
		}));
		JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner.getEditor();
		editor.getTextField().setEnabled(true);
		editor.getTextField().setEditable(false);
		
		GridBagConstraints gbc_spinner = new GridBagConstraints();
		gbc_spinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinner.insets = new Insets(0, 0, 5, 0);
		gbc_spinner.gridx = 1;
		gbc_spinner.gridy = 1;
		panel.add(spinner, gbc_spinner);
		GridBagConstraints gbc_btnPhase = new GridBagConstraints();
		gbc_btnPhase.insets = new Insets(0, 0, 5, 5);
		gbc_btnPhase.fill = GridBagConstraints.BOTH;
		gbc_btnPhase.gridx = 0;
		gbc_btnPhase.gridy = 2;
		panel.add(btnPhase, gbc_btnPhase);
		
		btnRecurrence = new JButton("Recurrence");
		btnRecurrence.addActionListener(this);
		GridBagConstraints gbc_btnRecurrence = new GridBagConstraints();
		gbc_btnRecurrence.fill = GridBagConstraints.BOTH;
		gbc_btnRecurrence.insets = new Insets(0, 0, 5, 5);
		gbc_btnRecurrence.gridx = 0;
		gbc_btnRecurrence.gridy = 5;
		panel.add(btnRecurrence, gbc_btnRecurrence);
		
		lblMaxFreq = new JLabel("Max Freq");
		GridBagConstraints gbc_lblMaxFreq = new GridBagConstraints();
		gbc_lblMaxFreq.anchor = GridBagConstraints.EAST;
		gbc_lblMaxFreq.insets = new Insets(0, 0, 5, 5);
		gbc_lblMaxFreq.gridx = 0;
		gbc_lblMaxFreq.gridy = 3;
		panel.add(lblMaxFreq, gbc_lblMaxFreq);
		
		txtMaxFreq = new JTextField();
		txtMaxFreq.addActionListener(this);
		txtMaxFreq.setHorizontalAlignment(SwingConstants.CENTER);
		txtMaxFreq.setText("All");
		GridBagConstraints gbc_txtMaxFreq = new GridBagConstraints();
		gbc_txtMaxFreq.insets = new Insets(0, 0, 5, 0);
		gbc_txtMaxFreq.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtMaxFreq.gridx = 1;
		gbc_txtMaxFreq.gridy = 3;
		panel.add(txtMaxFreq, gbc_txtMaxFreq);
		txtMaxFreq.setColumns(10);
		
		lblMaxAmp = new JLabel("Max Amp");
		GridBagConstraints gbc_lblMaxAmp = new GridBagConstraints();
		gbc_lblMaxAmp.anchor = GridBagConstraints.EAST;
		gbc_lblMaxAmp.insets = new Insets(0, 0, 5, 5);
		gbc_lblMaxAmp.gridx = 0;
		gbc_lblMaxAmp.gridy = 4;
		panel.add(lblMaxAmp, gbc_lblMaxAmp);
		
		txtMaxAmp = new JTextField();
		txtMaxAmp.setHorizontalAlignment(SwingConstants.CENTER);
		txtMaxAmp.setText("All");
		GridBagConstraints gbc_txtMaxAmp = new GridBagConstraints();
		gbc_txtMaxAmp.insets = new Insets(0, 0, 5, 0);
		gbc_txtMaxAmp.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtMaxAmp.gridx = 1;
		gbc_txtMaxAmp.gridy = 4;
		panel.add(txtMaxAmp, gbc_txtMaxAmp);
		txtMaxAmp.setColumns(10);
		
		lblOverSampledImage = new JLabel("Over Sampled Image Size");
		lblOverSampledImage.setHorizontalAlignment(SwingConstants.RIGHT);
		GridBagConstraints gbc_lblOverSampledImage = new GridBagConstraints();
		gbc_lblOverSampledImage.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblOverSampledImage.insets = new Insets(0, 0, 5, 5);
		gbc_lblOverSampledImage.gridx = 0;
		gbc_lblOverSampledImage.gridy = 6;
		panel.add(lblOverSampledImage, gbc_lblOverSampledImage);
		
		comboBoxOverSample = new JComboBox<String>();
		comboBoxOverSample.setModel(new DefaultComboBoxModel<String>(new String[]
		{
			"1M", "4M", "9M", "16M", "25M", "36M", "41M", "64M", "81M", "100M", "121M", "144M", "169M", "196M", "225M", "256M"
		}));
		GridBagConstraints gbc_comboBoxOverSample = new GridBagConstraints();
		gbc_comboBoxOverSample.insets = new Insets(0, 0, 5, 0);
		gbc_comboBoxOverSample.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBoxOverSample.gridx = 1;
		gbc_comboBoxOverSample.gridy = 6;
		panel.add(comboBoxOverSample, gbc_comboBoxOverSample);
		
		chckbxColoured = new JCheckBox("Coloured");
		GridBagConstraints gbc_chckbxColoured = new GridBagConstraints();
		gbc_chckbxColoured.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxColoured.gridx = 0;
		gbc_chckbxColoured.gridy = 7;
		panel.add(chckbxColoured, gbc_chckbxColoured);
		
		txtRadius = new JTextField();
		txtRadius.setText("0.05");
		txtRadius.addActionListener(this);
		GridBagConstraints gbc_txtRadius = new GridBagConstraints();
		gbc_txtRadius.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtRadius.gridx = 1;
		gbc_txtRadius.gridy = 7;
		panel.add(txtRadius, gbc_txtRadius);
		txtRadius.setColumns(10);
		txtMaxAmp.addActionListener(this);
		btnDft.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				int indicies[] = historyList.getSelectedIndices();
				
				int passes = Integer.parseInt((String) spinner.getValue());
				
				int width = (int) (centerPanel.getWidth() * 0.9);
				int height = (int) (centerPanel.getHeight() * 0.9);
				
				// Selected
				if(indicies.length > 0)
				{
					addFFTPanel(indicies.length % 4);
					
					// Compute All?
					for(int i = 0; i < indicies.length; i++)
					{
						computeFFTAndAddFFTW3(indicies[i], indicies.length, avgFilter, passes, width, height);
					}
				}
				else
				{
					int len = histories.length % 4;
					
					if(len == 0)
					{
						len = histories.length;
					}
					
					// All
					addFFTPanel(len);
					
					// Compute All?
					for(int i = 0; i < histories.length; i++)
					{
						computeFFTAndAddFFTW3(i, histories.length, avgFilter, passes, width, height);
					}
				}
				
				checkMaxFreq();
				checkMaxAmp();
			}
		});
		
		GridBagConstraints gbc_btnPoinecare = new GridBagConstraints();
		gbc_btnPoinecare.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnPoinecare.insets = new Insets(0, 0, 0, 5);
		gbc_btnPoinecare.gridx = 0;
		gbc_btnPoinecare.gridy = 1;
		
		JMenuBar menuBar = new JMenuBar();
		gui.setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		mntmOpen = new JMenuItem("Open");
		mntmOpen.addActionListener(this);
		mnFile.add(mntmOpen);
		
		gui.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent w)
			{
				doProgramExit();
			}
		});
		
		// Add a default sine wave signal
		addSignalChart("Sine Wave", createSineWave(), names, histories);
		
		int len = histories.length % 4;
		
		if(len == 0)
		{
			len = histories.length;
		}
		
		gui.pack();
		gui.setVisible(true);
		gui.validate();
		
		// All
		addFFTPanel(len);
		int width = (int) (centerPanel.getWidth() * 0.9);
		int height = (int) (centerPanel.getHeight() * 0.9);
		// Compute All?
		for(int i = 0; i < histories.length; i++)
		{
			computeFFTAndAddFFTW3(i, histories.length, avgFilter, 1, width, height);
		}
		
		gui.validate();
		
	}
	
	// SINE WAVE
	public int createSineWave()
	{
		String name = "Sine Wave";
		
		// Sine Wave Frequency
		int hertz = 10;
		int sampleRate = (hertz * 2);
		double overSampleFreq = hertz * sampleRate;
		
		double seconds = 1;
		
		timePeriod = seconds;
		
		tIsSeconds = true;
		
		int timeAxis = (int) (overSampleFreq * seconds);
		
		log.info(timeAxis);
		
		double time = 0;
		
		double dt = (1.0 / overSampleFreq);
		
		double amplitude = 0;
		
		DoubleTraceSample samples[] = new DoubleTraceSample[timeAxis];
		
		for(int i = 0; i < timeAxis; i++)
		{
			amplitude = Math.sin(2.0 * Math.PI * hertz * time) + 1;
			
			samples[i] = new DoubleTraceSample(time, amplitude);
			
			time = i * dt;
		}
		
		int numSamples = timeAxis;
		
		names = new String[1];
		histories = new DoubleTraceSample[1][];
		
		log.info("Got Arrays");
		
		for(int st = 0; st < 1; st++)
		{
			names[st] = new String(name);
			historyListModel.addElement(names[st]);
			histories[st] = samples;
		}
		
		gui.setTitle(name);
		
		return numSamples;
	}
	
	public void addSignalChart(String name, int numSamples, String[] names, DoubleTraceSample[][] histories)
	{
		if(chart != null)
		{
			centerPanel.remove(chart);
			chart = null;
		}
		
		chart = new SingleStatChartPanel(name, true, false, numSamples);
		
		chart.populate(numSamples, names, histories);
		
		GridBagConstraints gbc_chart = new GridBagConstraints();
		gbc_chart.insets = new Insets(0, 0, 5, 5);
		gbc_chart.fill = GridBagConstraints.BOTH;
		gbc_chart.gridx = 0;
		gbc_chart.gridy = 0;
		centerPanel.add(chart, gbc_chart);
	}
	
	public void cleanFFTListPanel()
	{
		if(fftListPanel != null)
		{
			fftListPanel.removeAll();
			fftListPanel.repaint();
		}
		
		gui.revalidate();
	}
	
	public void addFFTPanel(int mul)
	{
		cleanFFTListPanel();
		
		if(fftListPanel == null)
		{
			fftListPanel = new JPanel(new FlowLayout());
		}
		
		if(scrollPane == null)
		{
			GridBagConstraints gbc_panel_1 = new GridBagConstraints();
			gbc_panel_1.insets = new Insets(0, 0, 0, 5);
			gbc_panel_1.fill = GridBagConstraints.BOTH;
			gbc_panel_1.gridx = 0;
			gbc_panel_1.gridy = 1;
			
			scrollPane = new JScrollPane(fftListPanel);
			
			scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			centerPanel.add(scrollPane, gbc_panel_1);
			
			scrollPane.getVerticalScrollBar().setUnitIncrement(25);
		}
		
		int height = (int) (centerPanel.getHeight() * 0.9);
		
		fftListPanel.setPreferredSize(new Dimension(centerPanel.getWidth(), height * mul));
		fftListPanel.setMinimumSize(new Dimension(centerPanel.getWidth(), height * mul));
	}
	
	/* Ensure the user wants to exit then exit the program */
	private void doProgramExit()
	{
		javax.swing.SwingUtilities.invokeLater(new Runnable()
		{
			@Override
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
			
			log.info("Open Dialog");
			
			int val = filechooser.showOpenDialog(filechooser);
			
			if(val == JFileChooser.APPROVE_OPTION)
			{
				// OPEN FILE
				tIsSeconds = false;
				
				log.info("New File Choosen");
				
				// Clear the list items
				historyListModel.clear();
				
				String file = filechooser.getSelectedFile().getAbsolutePath();
				
				String fname = filechooser.getSelectedFile().getName();
				
				gui.setTitle(file);
				
				log.info(file);
				
				try
				{
					CSV parser = new CSV(file);
					
					ArrayList<Trace> stats = parser.getTraces();
					int numSamples = parser.getSampleNum();
					
					names = new String[stats.size()];
					histories = new DoubleTraceSample[stats.size()][];
					
					log.info("Got Arrays");
					
					for(int st = 0; st < stats.size(); st++)
					{
						Trace temp = stats.get(st);
						names[st] = new String(temp.name);
						
						// Populate list items
						historyListModel.addElement(names[st]);
						
						TraceSample[] temp2 =  temp.getHistoryAsArray();
						
						histories[st] = Arrays.copyOf(temp2, temp2.length, DoubleTraceSample[].class);
					}
					
					addSignalChart(fname, numSamples, names, histories);
					
					cleanFFTListPanel();
					
					gui.validate();
				}
				catch(IOException e1)
				{
					log.info("Error opening file");
					
					e1.printStackTrace();
				}
			}
		}
		else if(e.getSource() == txtMaxFreq)
		{
			if(fftListPanel == null)
			{
				return;
			}
			
			checkMaxFreq();
			
		}
		else if(e.getSource() == txtMaxAmp)
		{
			if(fftListPanel == null)
			{
				return;
			}
			
			checkMaxAmp();
			
		}
		else if(e.getSource() == chckbxAvgFilter)
		{
			avgFilter = chckbxAvgFilter.isSelected();
		}
		else if((e.getSource() == btnRecurrence) || (e.getSource() == txtRadius))
		{
			int indicies[] = historyList.getSelectedIndices();
			
			if((indicies.length == 3) | (histories.length >= 3))
			{
				log.info("Drawing");
				
				int statsLenght = 3;
				int samples = histories[0].length;
				
				final double[][] data = new double[3][samples];
				
				for(int st = 0; st < statsLenght; st++)
				{
					for(int sam = 0; sam < samples; sam++)
					{
						// OpenGL uses floats
						data[st][sam] = histories[st][sam].value;
					}
				}
				
				javax.swing.SwingUtilities.invokeLater(new Runnable()
				{
					@Override
					public void run()
					{
						rp.setData(data);
						
						float nVal = 1f;
						
						try
						{
							nVal = Float.parseFloat(txtRadius.getText());
						}
						catch(NumberFormatException exception)
						{
							log.info("Radius not Valid");
						}
						
						float radius = nVal;
						
						rp.enableColor(chckbxColoured.isSelected());
						
						int sqrVal = -1;
						String sel = (String) comboBoxOverSample.getSelectedItem();
						
						switch(sel)
						{
							case "1M":
							{
								sqrVal = 1000;
							}
							break;
							case "4M":
							{
								sqrVal = 2000;
							}
							break;
							case "9M":
							{
								sqrVal = 3000;
							}
							break;
							case "16M":
							{
								sqrVal = 4000;
							}
							break;
							case "25M":
							{
								sqrVal = 5000;
							}
							break;
							case "36M":
							{
								sqrVal = 6000;
							}
							break;
							case "41M":
							{
								sqrVal = 7000;
							}
							break;
							case "64M":
							{
								sqrVal = 8000;
							}
							break;
							case "81M":
							{
								sqrVal = 9000;
							}
							break;
							case "100M":
							{
								sqrVal = 10000;
							}
							break;
							case "121M":
							{
								sqrVal = 11000;
							}
							break;
							case "144M":
							{
								sqrVal = 12000;
							}
							break;
							case "169M":
							{
								sqrVal = 13000;
							}
							break;
							case "196M":
							{
								sqrVal = 14000;
							}
							break;
							case "225M":
							{
								sqrVal = 15000;
							}
							break;
							case "256M":
							{
								sqrVal = 16000;
							}
							break;
						}
						
						rp.createRecurrence(radius, sqrVal);
						rp.repaint();
						rp.revalidate();
					}
				});
			}
		}
	}
	
	private void checkMaxAmp()
	{
		String value = txtMaxAmp.getText();
		
		if(value.equals("All"))
		{
			for(Component comp : fftListPanel.getComponents())
			{
				((SingleStatChartPanel) comp).setAmpMaxAuto();
			}
		}
		else
		{
			double nVal = 0;
			
			try
			{
				nVal = Double.parseDouble(value);
			}
			catch(NumberFormatException exception)
			{
			}
			
			if(nVal == 0)
			{
				for(Component comp : fftListPanel.getComponents())
				{
					((SingleStatChartPanel) comp).setAmpMaxAuto();
				}
			}
			else
			{
				for(Component comp : fftListPanel.getComponents())
				{
					((SingleStatChartPanel) comp).setAmpRangeMax(nVal);
				}
			}
			
		}
	}
	
	private void checkMaxFreq()
	{
		String value = txtMaxFreq.getText();
		
		if(value.equals("All"))
		{
			for(Component comp : fftListPanel.getComponents())
			{
				((SingleStatChartPanel) comp).setFreqMaxAuto();
			}
		}
		else
		{
			double nVal = 0;
			
			try
			{
				nVal = Double.parseDouble(value);
			}
			catch(NumberFormatException exception)
			{
			}
			
			if(nVal == 0)
			{
				for(Component comp : fftListPanel.getComponents())
				{
					((SingleStatChartPanel) comp).setFreqMaxAuto();
				}
			}
			else
			{
				for(Component comp : fftListPanel.getComponents())
				{
					((SingleStatChartPanel) comp).setFreqRangeMax(nVal);
				}
			}
			
		}
	}
	
	private void computeFFTAndAddFFTW3(int num, int charts, boolean avgFilter, int passes, int freqWidth, int freqheight)
	{
		Loader.load(fftw3.class);
		
		String name = names[num];
		
		int len = (histories[num].length);
		
		int resLen = len * 2;
		
		double[] array = new double[len * 2];
		
		double inMax = Double.MIN_VALUE;
		
		// Zero Imaginary Values + Populate Real
		for(int i = 0; i < len; ++i)
		{
			array[2 * i] = histories[num][i].value;
			array[(2 * i) + 1] = 0;
			
			if(array[2 * i] > inMax)
			{
				inMax = array[2 * i];
			}
		}
		
		DoublePointer signal = new DoublePointer(2 * len);
		DoublePointer result = new DoublePointer(resLen);
		
		// fftw3.fftw_plan_with_nthreads(8);
		// fftw3.fftw_init_threads();
		
		fftw_plan plan = fftw3.fftw_plan_dft_1d(len, signal, result, fftw3.FFTW_FORWARD, (int) fftw3.FFTW_ESTIMATE);
		
		signal.put(array);
		
		fftw3.fftw_execute(plan);
		
		SingleStatChartPanel freq = new SingleStatChartPanel(name, true, false, len);
		
		if(charts > 4)
		{
			charts = 4;
		}
		
		freq.setPreferredSize(new Dimension(freqWidth / charts, freqheight / 2));
		freq.setMinimumSize(new Dimension(freqWidth / charts, freqheight / 2));
		freq.setLineWidth(0.9f);
		
		double[] array2 = new double[result.capacity()];
		
		result.get(array2);
		
		// REAL + IMG = norm
		double[] norm = new double[array2.length / 2];
		
		// Seconds or Steps
		if(tIsSeconds)
		{
			// Change of Sample Window Size
			sampleWindow = timePeriod;
		}
		else
		{
			// Change of Sample Window Size
			sampleWindow = len;
		}
		
		// Whole Time Period?
		timePeriod = sampleWindow;
		
		//
		int normI = 0;
		for(int i = 0; i < array2.length; i += 2)
		{
			double real = array2[i];
			double img = array2[i + 1];
			
			double inter = ((real * real) + (img * img));
			
			norm[normI] = Math.sqrt(inter) / sampleWindow;
			
			normI++;
		}
		
		// double[] han = hanningWindow(norm);
		
		log.info("timePeriod " + timePeriod);
		log.info("tIsSeconds " + tIsSeconds);
		
		double maxValue = Double.MIN_VALUE;
		
		DoubleTraceSample[] chartSamples = null;
		
		// double maxT = 0;
		
		if(avgFilter)
		{
			if(tIsSeconds)
			{
				// Change of Sample Window Size
				sampleWindow = timePeriod;
			}
			else
			{
				// Change of Sample Window Size
				sampleWindow = norm.length;
			}
			// maxT = (norm.length / sampleWindow) * timePeriod;
			
			// Needed keep time index during average as it removes samples
			// altering array pos which cannot then be used for freq.
			DoubleTraceSample[] filtered = new DoubleTraceSample[norm.length];
			for(int i = 0; i < norm.length; i++)
			{
				filtered[i] = new DoubleTraceSample((i / sampleWindow) * timePeriod, norm[i]);
			}
			
			int f = passes;
			
			while(f > 0)
			{
				filtered = avgFilter(filtered);
				
				f--;
			}
			
			chartSamples = filtered;
			
		}
		else
		{
			
			// maxT = (norm.length / sampleWindow) * timePeriod;
			
			DoubleTraceSample[] unaveraged = new DoubleTraceSample[result.capacity() / 2];
			
			for(int i = 0; i < unaveraged.length; i++)
			{
				unaveraged[i] = new DoubleTraceSample((i / sampleWindow) * timePeriod, norm[i]);
			}
			
			chartSamples = unaveraged;
		}
		
		// Scale Charts ignoring <5 freq
		for(int i = 0; i < chartSamples.length; i++)
		{
			if((i > 5) && (chartSamples[i].value > maxValue))
			{
				maxValue = chartSamples[i].value;
			}
		}
		
		freq.populateFFT("Frequency", chartSamples);
		
		// freq.setAmpMaxAuto();
		freq.setAmpRangeMax(maxValue);
		freq.setFreqMaxAuto();
		
		// freq.populateFFT("Frequency",array3);
		// freq.populateFFTShift("Frequency",array3,maxT);
		// freq.populateFFTShift("Frequency",array3,maxT);
		
		log.info("Adding FFT");
		
		fftListPanel.add(freq);
		
		gui.revalidate();
		
		fftw3.fftw_destroy_plan(plan);
	}
	
	private DoubleTraceSample[] avgFilter(DoubleTraceSample[] array)
	{
		// Drop 2 samples (start and end)
		DoubleTraceSample[] filtered = new DoubleTraceSample[array.length - 2];
		
		int fill = 0;
		for(int i = 1; i < (array.length - 1); i++)
		{
			double avg = ((array[i - 1].value * 0.25) + (array[i].value * 0.5) + (array[i + 1].value * 0.25));
			
			filtered[fill] = new DoubleTraceSample(array[i].time, avg);
			
			fill++;
		}
		
		return filtered;
	}
	
	@Override
	public void mouseClicked(MouseEvent e)
	{
		if(e.getSource() == rp)
		{
			int indicies[] = historyList.getSelectedIndices();
			
			if((indicies.length == 3) | (histories.length >= 3))
			{
				int sqrVal = -1;
				String sel = (String) comboBoxOverSample.getSelectedItem();
				
				switch(sel)
				{
					case "1M":
					{
						sqrVal = 1000;
					}
					break;
					case "4M":
					{
						sqrVal = 2000;
					}
					break;
					case "9M":
					{
						sqrVal = 3000;
					}
					break;
					case "16M":
					{
						sqrVal = 4000;
					}
					break;
					case "25M":
					{
						sqrVal = 5000;
					}
					break;
					case "36M":
					{
						sqrVal = 6000;
					}
					break;
					case "41M":
					{
						sqrVal = 7000;
					}
					break;
					case "64M":
					{
						sqrVal = 8000;
					}
					break;
					case "81M":
					{
						sqrVal = 9000;
					}
					break;
					case "100M":
					{
						sqrVal = 10000;
					}
					break;
					case "121M":
					{
						sqrVal = 11000;
					}
					break;
					case "144M":
					{
						sqrVal = 12000;
					}
					break;
					case "169M":
					{
						sqrVal = 13000;
					}
					break;
					case "196M":
					{
						sqrVal = 14000;
					}
					break;
					case "225M":
					{
						sqrVal = 15000;
					}
					break;
					case "256M":
					{
						sqrVal = 16000;
					}
					break;
				}
				
				largeRP = new JFrame();
				largeRP.setLayout(new BorderLayout());
				largeRP.setExtendedState(gui.getExtendedState() | Frame.MAXIMIZED_BOTH);
				
				largeRP.setMinimumSize(new Dimension(500, 500));
				largeRP.setPreferredSize(new Dimension(500, 500));
				
				// 1600M
				final int size = sqrVal;
				final RecurrencePlot rp = new RecurrencePlot(sqrVal);
				rp.setPreferredSize(new Dimension(sqrVal, sqrVal));
				
				final JScrollPane sp = new JScrollPane(rp);
				
				largeRP.add(sp, BorderLayout.CENTER);
				
				sp.getViewport().addMouseMotionListener(new MouseAdapter()
				{
					final Point current = new Point();
					
					@Override
					public void mouseDragged(MouseEvent e)
					{
						float scaleX = size / sp.getWidth();
						float scaleY = size / sp.getHeight();
						float scale = Math.max(scaleX, scaleY);
						
						JViewport vport = (JViewport) e.getSource();
						
						Point newPos = new Point((int) (e.getX() * scale), (int) (e.getY() * scale));
						Point view = vport.getViewPosition();
						
						view.translate(current.x - newPos.x, current.y - newPos.y);
						
						rp.scrollRectToVisible(new Rectangle(view, vport.getSize()));
						
						current.setLocation(newPos);
					}
					
					// Need to track mouse or it will snap the view
					@Override
					public void mouseMoved(MouseEvent e)
					{
						float scaleX = size / sp.getWidth();
						float scaleY = size / sp.getHeight();
						float scale = Math.max(scaleX, scaleY);
						
						Point newPos = new Point((int) (e.getX() * scale), (int) (e.getY() * scale));
						
						current.setLocation(newPos);
					}
					
				});
				largeRP.setVisible(true);
				largeRP.revalidate();
				// Data GET
				
				log.info("Drawing");
				
				int statsLenght = 3;
				int samples = histories[0].length;
				
				final double[][] data = new double[3][samples];
				
				for(int st = 0; st < statsLenght; st++)
				{
					for(int sam = 0; sam < samples; sam++)
					{
						data[st][sam] = histories[st][sam].value;
					}
				}
				
				rp.setData(data);
				
				float nVal = 1f;
				
				try
				{
					nVal = Float.parseFloat(txtRadius.getText());
				}
				catch(NumberFormatException exception)
				{
					log.info("Radius not Valid");
				}
				
				float radius = nVal;
				
				rp.enableColor(chckbxColoured.isSelected());
				
				rp.createRecurrence(radius, sqrVal);
				rp.repaint();
				rp.revalidate();
			}
		}
	}
	
	@Override
	public void mousePressed(MouseEvent e)
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void mouseReleased(MouseEvent e)
	{
		
	}
	
	@Override
	public void mouseEntered(MouseEvent e)
	{
		
	}
	
	@Override
	public void mouseExited(MouseEvent e)
	{
		
	}
}
