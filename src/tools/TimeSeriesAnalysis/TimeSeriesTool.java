package tools.TimeSeriesAnalysis;

import jCompute.Stats.Logs.CSVLogParser;
import jCompute.Stats.Trace.SingleStat;
import jCompute.Stats.Trace.StatSample;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
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
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.fftw3;
import org.bytedeco.javacpp.fftw3.fftw_plan;

import javax.swing.JPanel;

import java.awt.GridBagLayout;

import javax.swing.JList;

import java.awt.GridBagConstraints;

import javax.swing.JButton;

import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JCheckBox;

public class TimeSeriesTool implements WindowListener, ActionListener
{
	private static JFrame				gui;
	private JMenuItem					mntmOpen;
	private static SingleStatChartPanel	chart;
	private StatSample[][]				histories;
	private String[]					names;
	private JPanel						toolPanel;
	private JList						historyList;
	private DefaultListModel			historyListModel;
	private JButton						btnDft;
	private JPanel						panel;
	private JButton						btnPhase;
	private double						sampleWindow	= 0;

	private static JScrollPane			scrollPane;
	private static JPanel				fftListPanel;

	private static int					windowWidth		= 1800;
	private static int					windowheight	= 1000;

	private static int					sidePanelWidth	= 200;
	private static int					sidePanelHeight	= 600;

	private static int					mainPanelWidth;
	private static int					mainPanelHeight;
	private JLabel						lblMaxFreq;
	private JTextField					txtMaxFreq;
	private JLabel						lblMaxAmp;
	private JTextField					txtMaxAmp;
	private JCheckBox chckbxAvgFilter;

	private boolean avgFilter = false;
	
	public static void main(String args[])
	{
		windowWidth = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
		windowheight = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();

		mainPanelWidth = windowWidth - (sidePanelWidth + 120);
		mainPanelHeight = windowheight - 120;

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
		gui.setExtendedState(gui.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		gui.setPreferredSize(new Dimension(windowWidth, windowheight));
		gui.setMinimumSize(new Dimension(windowWidth, windowheight));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]
		{
				764, 200
		};
		gridBagLayout.rowHeights = new int[]
		{
				541, 0, 0
		};
		gridBagLayout.columnWeights = new double[]
		{
				1.0, 0.0
		};
		gridBagLayout.rowWeights = new double[]
		{
				1.0, 1.0, Double.MIN_VALUE
		};
		gui.getContentPane().setLayout(gridBagLayout);

		toolPanel = new JPanel();
		toolPanel.setPreferredSize(new Dimension(sidePanelWidth, sidePanelHeight));
		toolPanel.setMinimumSize(new Dimension(sidePanelWidth, sidePanelHeight));
		GridBagConstraints gbc_toolPanel = new GridBagConstraints();
		gbc_toolPanel.insets = new Insets(0, 0, 5, 0);
		gbc_toolPanel.gridx = 1;
		gbc_toolPanel.gridy = 0;
		gui.getContentPane().add(toolPanel, gbc_toolPanel);
		GridBagLayout gbl_toolPanel = new GridBagLayout();
		gbl_toolPanel.columnWidths = new int[]
		{
				0, 0
		};
		gbl_toolPanel.rowHeights = new int[]
		{
				0, 0
		};
		gbl_toolPanel.columnWeights = new double[]
		{
				1.0, Double.MIN_VALUE
		};
		gbl_toolPanel.rowWeights = new double[]
		{
				1.0, 1.0
		};
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
		gbl_panel.columnWidths = new int[]
		{
				0, 0, 0
		};
		gbl_panel.rowHeights = new int[]
		{
				0, 0, 0, 0, 0
		};
		gbl_panel.columnWeights = new double[]
		{
				1.0, 1.0, Double.MIN_VALUE
		};
		gbl_panel.rowWeights = new double[]
		{
				0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE
		};
		panel.setLayout(gbl_panel);

		btnDft = new JButton("DFT");
		GridBagConstraints gbc_btnDft = new GridBagConstraints();
		gbc_btnDft.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnDft.insets = new Insets(0, 0, 5, 5);
		gbc_btnDft.gridx = 0;
		gbc_btnDft.gridy = 0;
		panel.add(btnDft, gbc_btnDft);
		
				btnPhase = new JButton("Phase");
				btnPhase.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						int indicies[] = historyList.getSelectedIndices();

						if(indicies.length == 2)
						{
							int index1 = indicies[0];
							int index2 = indicies[1];
							if(index1 >= 0 && index2 >= 0)
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

								String arrayLabels[] = new String[]
								{
										names[index1], names[index2]
								};

								PhaseTool phasePlot = new PhaseTool(arrayLabels, array1, array2);
							}
						}

					}
				});
				
				chckbxAvgFilter = new JCheckBox("Avg Filter");
				GridBagConstraints gbc_chckbxAvgFilter = new GridBagConstraints();
				gbc_chckbxAvgFilter.insets = new Insets(0, 0, 5, 0);
				gbc_chckbxAvgFilter.gridx = 1;
				gbc_chckbxAvgFilter.gridy = 0;
				panel.add(chckbxAvgFilter, gbc_chckbxAvgFilter);
				chckbxAvgFilter.addActionListener(this);
				GridBagConstraints gbc_btnPhase = new GridBagConstraints();
				gbc_btnPhase.insets = new Insets(0, 0, 5, 5);
				gbc_btnPhase.fill = GridBagConstraints.HORIZONTAL;
				gbc_btnPhase.gridx = 0;
				gbc_btnPhase.gridy = 1;
				panel.add(btnPhase, gbc_btnPhase);

		lblMaxFreq = new JLabel("Max Freq");
		GridBagConstraints gbc_lblMaxFreq = new GridBagConstraints();
		gbc_lblMaxFreq.anchor = GridBagConstraints.EAST;
		gbc_lblMaxFreq.insets = new Insets(0, 0, 5, 5);
		gbc_lblMaxFreq.gridx = 0;
		gbc_lblMaxFreq.gridy = 2;
		panel.add(lblMaxFreq, gbc_lblMaxFreq);

		txtMaxFreq = new JTextField();
		txtMaxFreq.addActionListener(this);
		txtMaxFreq.setHorizontalAlignment(SwingConstants.CENTER);
		txtMaxFreq.setText("All");
		GridBagConstraints gbc_txtMaxFreq = new GridBagConstraints();
		gbc_txtMaxFreq.insets = new Insets(0, 0, 5, 0);
		gbc_txtMaxFreq.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtMaxFreq.gridx = 1;
		gbc_txtMaxFreq.gridy = 2;
		panel.add(txtMaxFreq, gbc_txtMaxFreq);
		txtMaxFreq.setColumns(10);

		lblMaxAmp = new JLabel("Max Amp");
		GridBagConstraints gbc_lblMaxAmp = new GridBagConstraints();
		gbc_lblMaxAmp.anchor = GridBagConstraints.EAST;
		gbc_lblMaxAmp.insets = new Insets(0, 0, 0, 5);
		gbc_lblMaxAmp.gridx = 0;
		gbc_lblMaxAmp.gridy = 3;
		panel.add(lblMaxAmp, gbc_lblMaxAmp);

		txtMaxAmp = new JTextField();
		txtMaxAmp.setHorizontalAlignment(SwingConstants.CENTER);
		txtMaxAmp.setText("All");
		GridBagConstraints gbc_txtMaxAmp = new GridBagConstraints();
		gbc_txtMaxAmp.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtMaxAmp.gridx = 1;
		gbc_txtMaxAmp.gridy = 3;
		panel.add(txtMaxAmp, gbc_txtMaxAmp);
		txtMaxAmp.setColumns(10);
		txtMaxAmp.addActionListener(this);
		btnDft.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				int indicies[] = historyList.getSelectedIndices();
				
				// Selected
				if(indicies.length > 0)
				{
					addFFTPanel(indicies.length % 4);

					// Compute All?
					for(int i = 0; i < indicies.length; i++)
					{
						computeFFTAndAddFFTW3(i, indicies.length,avgFilter);
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
						computeFFTAndAddFFTW3(i, histories.length,avgFilter);
					}
				}

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

		gui.addWindowListener(this);

		// Add a default sine wave signal
		addSignalChart("Sine Wave", createSineWave(), names, histories);

		// All
		addFFTPanel(histories.length % 4);

		// Compute All?
		for(int i = 0; i < histories.length; i++)
		{
			computeFFTAndAddFFTW3(i, histories.length,avgFilter);
		}

		gui.validate();

		gui.setVisible(true);
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

		this.sampleWindow = seconds;

		int timeAxis = (int) (overSampleFreq * seconds);

		System.out.println(timeAxis);

		double time = 0;

		double dt = (1.0 / overSampleFreq);

		double amplitude = 0;

		StatSample samples[] = new StatSample[timeAxis];

		for(int i = 0; i < timeAxis; i++)
		{
			amplitude = Math.sin(2.0 * Math.PI * hertz * time);

			samples[i] = new StatSample(time, amplitude);

			time = i * dt;
		}

		int numSamples = timeAxis;

		names = new String[1];
		histories = new StatSample[1][];

		System.out.println("Got Arrays");

		for(int st = 0; st < 1; st++)
		{
			names[st] = new String(name);
			historyListModel.addElement(names[st]);
			histories[st] = samples;
		}

		gui.setTitle(name);

		return numSamples;
	}

	public static void addSignalChart(String name, int numSamples, String[] names, StatSample[][] histories)
	{
		int panelHeights = mainPanelHeight / 2;

		if(chart != null)
		{
			gui.getContentPane().remove(chart);
			chart = null;
		}

		chart = new SingleStatChartPanel(name, true, false, numSamples);

		chart.setPreferredSize(new Dimension(mainPanelWidth, mainPanelHeight));
		chart.setMinimumSize(new Dimension(mainPanelWidth, mainPanelHeight));

		chart.populate(numSamples, names, histories);

		GridBagConstraints gbc_chart = new GridBagConstraints();
		gbc_chart.insets = new Insets(0, 0, 5, 5);
		gbc_chart.fill = GridBagConstraints.BOTH;
		gbc_chart.gridx = 0;
		gbc_chart.gridy = 0;
		gui.getContentPane().add(chart, gbc_chart);
	}

	public static void cleanFFTListPanel()
	{
		if(fftListPanel != null)
		{
			fftListPanel.removeAll();
			fftListPanel.repaint();
		}

		gui.revalidate();
	}

	public static void addFFTPanel(int mul)
	{
		cleanFFTListPanel();

		if(fftListPanel == null)
		{
			fftListPanel = new JPanel(new FlowLayout());
		}

		fftListPanel.setPreferredSize(new Dimension(mainPanelWidth, (mainPanelHeight / mul)));
		fftListPanel.setMinimumSize(new Dimension(mainPanelWidth, (mainPanelHeight / mul)));

		if(scrollPane == null)
		{
			GridBagConstraints gbc_panel_1 = new GridBagConstraints();
			gbc_panel_1.insets = new Insets(0, 0, 0, 5);
			gbc_panel_1.fill = GridBagConstraints.BOTH;
			gbc_panel_1.gridx = 0;
			gbc_panel_1.gridy = 1;

			scrollPane = new JScrollPane(fftListPanel);
			scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			gui.getContentPane().add(scrollPane, gbc_panel_1);
		}

		scrollPane.setPreferredSize(new Dimension(mainPanelWidth, mainPanelHeight));
		scrollPane.setMinimumSize(new Dimension(mainPanelWidth, mainPanelHeight));

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
				// OPEN FILE

				System.out.println("New File Choosen");

				// Clear the list items
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

					// Populate list items
					historyListModel.addElement(names[st]);

					histories[st] = temp.getHistoryAsArray();
				}

				sampleWindow = histories[0].length;

				addSignalChart(fname, numSamples, names, histories);

				cleanFFTListPanel();

				gui.validate();

			}
		}
		else if(e.getSource() == txtMaxFreq)
		{
			if(fftListPanel == null)
			{
				return;
			}

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
		else if(e.getSource() == txtMaxAmp)
		{
			if(fftListPanel == null)
			{
				return;
			}

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
		else if(e.getSource() == chckbxAvgFilter)
		{
			avgFilter = chckbxAvgFilter.isSelected();
		}

	}

	private void computeFFTAndAddFFTW3(int num, int charts, boolean avgFilter)
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
			array[2 * i] = histories[num][i].getSample();
			array[2 * i + 1] = 0;

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
		freq.setPreferredSize(new Dimension(mainPanelWidth / charts, mainPanelHeight / 2));
		freq.setMinimumSize(new Dimension(mainPanelWidth / charts, mainPanelHeight / 2));
		freq.setLineWidth(0.9f);

		double[] array2 = new double[result.capacity()];

		result.get(array2);

		// REAL + IMG = norm
		double[] norm = new double[array2.length / 2];

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

		double[] han =  hanningWindow(norm);
		
		double mod = sampleWindow;
		// double mod = 1;

		System.out.println("Mod " + mod);

		double maxValue = Double.MIN_VALUE;

		StatSample[] chartSamples = null;
		
		
		StatSample[] unaveraged = new StatSample[result.capacity() / 2];

		double maxT = (unaveraged.length / sampleWindow) * mod;
		
		for(int i = 0; i < unaveraged.length; i++)
		{
			unaveraged[i] = new StatSample((double) ((i / sampleWindow) * mod), norm[i]);
		}
		
		if(avgFilter)
		{
			// Avg Filter (3 - 1/4)
			StatSample[] avg = avgFilter(norm, mod);

			chartSamples = avg;
			
			//freq.populateFFTShift("Frequency", unaveraged,maxT);
		}
		else
		{
			chartSamples = unaveraged;
		}
		
		// Scale Charts ignoreing <5 freq
		for(int i=0;i<chartSamples.length;i++)
		{
			if((i>5) & (chartSamples[i].getSample() > maxValue))
			{
				maxValue = chartSamples[i].getSample();
			}
		}
		
		freq.populateFFT("Frequency", chartSamples);
		
		//freq.setAmpMaxAuto();
		freq.setAmpRangeMax(maxValue);
		freq.setFreqMaxAuto();

		// freq.populateFFT("Frequency",array3);
		// freq.populateFFTShift("Frequency",array3,maxT);
		// freq.populateFFTShift("Frequency",array3,maxT);

		System.out.println("Adding FFT");

		fftListPanel.add(freq);

		gui.revalidate();

		fftw3.fftw_destroy_plan(plan);
	}

	// Hann / Hanning
	public double[] hanningWindow(double[] array)
	{
		int normLen = array.length;
		
		double[] han = new double[normLen];

		for(int i = 0; i < normLen; i++)
		{
			double multiplier = 0.5 * (1 - Math.cos(2 * Math.PI * (i+1) / (normLen+1)));
			han[i] = multiplier * array[i];
		}

		return han;
	}
	
	private void computeFFTAndDisplayFFTW3(int num)
	{
		Loader.load(fftw3.class);

		String name = names[num];

		int len = (histories[num].length);

		int resLen = len * 2;

		double[] array = new double[len * 2];

		double inMax = Double.MIN_VALUE;

		for(int i = 0; i < len; ++i)
		{
			array[2 * i] = histories[num][i].getSample();
			array[2 * i + 1] = 0;

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
		final JFrame results = new JFrame();
		results.getContentPane().setLayout(new BorderLayout());

		SingleStatChartPanel freq = new SingleStatChartPanel(name, true, false, len);

		double[] array2 = new double[result.capacity()];

		result.get(array2);

		// REAL + IMG = norm
		double[] norm = new double[array2.length / 2];

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

		double mod = sampleWindow;
		// double mod = 1;

		System.out.println("Mod " + mod);

		StatSample[] array3 = new StatSample[result.capacity() / 2];

		double maxT = (array3.length / sampleWindow) * mod;

		for(int i = 0; i < array3.length; i++)
		{
			array3[i] = new StatSample((double) ((i / sampleWindow) * mod), norm[i]);
		}

		// Avg Filter (3 - 1/4)

		freq.populateFFT("Frequency", avgFilter(norm, mod));

		// freq.populateFFT("Frequency",array3);
		// freq.populateFFTShift("Frequency",array3,maxT);
		// freq.populateFFTShift("Frequency",array3,maxT);

		results.getContentPane().add(freq, BorderLayout.CENTER);
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
		fftw3.fftw_destroy_plan(plan);
	}

	private StatSample[] avgFilter(double[] array, double mod)
	{
		StatSample[] filtered = new StatSample[array.length - 2];

		int fill = 0;
		for(int i = 1; i < array.length - 1; i++)
		{
			double avg = ((array[i - 1] * 0.25) + (array[i] * 0.5) + (array[i + 1] * 0.25));

			filtered[fill] = new StatSample((double) ((i / sampleWindow) * mod), avg);

			fill++;
		}

		return filtered;
	}
}
