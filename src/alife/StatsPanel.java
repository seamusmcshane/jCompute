package alife;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.swing.border.EtchedBorder;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.JSlider;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.JCheckBox;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
/**
 * A Custom Panel for controlling the display of Simulation stats in the GUI
 *
 */
public class StatsPanel extends JPanel
{

	/* Panel Contents */
	private static JPanel simStatCountPanel = new JPanel();
	private static JPanel alifeInfoRow = new JPanel();
	private static JLabel lblPlants = new JLabel("Plants");
	private static JLabel lblPlantNo = new JLabel("0");
	private static JLabel lblPredators = new JLabel("Predators");
	private static JLabel lblPredatorsNo = new JLabel("0");
	private static JLabel lblPrey = new JLabel("Prey");
	private static JLabel lblPreyNo = new JLabel("0");
	private static JPanel simulationInfoRow = new JPanel();
	private static JLabel lblASPS = new JLabel(" AS/Sec");
	private static JLabel lblASPSNo = new JLabel("0");
	private static JLabel lblRunTime = new JLabel("Time");
	private static JLabel lblRunTimeNo = new JLabel("0");

	/** The custom panel for drawing the graph */
	private static StatsLineGraphPanel lineGraphPanel;
	private static StatsLorenzGraphPanel lorenzGraphPanel;
	private static StatsStackedGraphPanel stackedGraphPanel;

	
	/* Counters */
	private static int ASPS = 0; // Average Steps per second
	private static int stepNo = 0;
	private static int plantNo = 0;
	private static int preyNo = 0;
	private static int predNo = 0;

	/* Graph Samples - 15 sps * 60 seconds = 900 samples for a minute etc.. */
	private static int samplePeriod = 60;
	private static int sampleNum = 9000; // 9000 = 10 mins real-time (15sps)

	/** The Sample arrays */
	private static int plantSamples[] = new int[sampleNum];
	private static int preySamples[] = new int[sampleNum];
	private static int predSamples[] = new int[sampleNum];

	/* Record of max values for graph scaling */
	private static int plantsMax = 0;
	private static int preyMax = 0;
	private static int predMax = 0;

	/* Graph Scales - click graph */
	private int scale_mode = 2; // 0 = all on own scale, 1 - plants on own scale, prey+pred tied, 2 - all tied
	private int graph_visible=0;
	
	/* Graph State */
	private static boolean graphs_full = false;

	/* Used to prevent showing sliders in a small area when paused */
	private static boolean paused;
	
	/* Update the graphs based on ratio of steps */
	private int graph_draw_div = 1;
	
	private final JLabel lblStep = new JLabel("Step No");
	private final static JLabel lblStepNo = new JLabel("0");
	private final JTabbedPane tabbedGraphs = new JTabbedPane(JTabbedPane.TOP);
	private final JPanel lorenzContainerPanel = new JPanel();
	private final static JPanel rightPanel = new JPanel();
	private final JSlider lorenzZoomSlider = new JSlider();
	private final JButton btnReset = new JButton("Reset Graph");
	private final static JPanel bottomPanel = new JPanel();
	private final JSlider xScaleslider = new JSlider();
	private final JLabel lblZoom = new JLabel("Zoom");
	private final JLabel lblXscale = new JLabel("XScale");
	private final static JPanel leftPanel = new JPanel();
	private final JSlider yScaleslider = new JSlider();
	private final JLabel lblYscale = new JLabel("Y Scale");
	
	private final JButton btnMode = new JButton("Free Mode");
	private final JPanel rightButtonsPanel = new JPanel();
	private final static JCheckBox chckbxFullSizeGraphCheckBox = new JCheckBox("Show Full Graphs");
	private final JPanel panel = new JPanel();
	private final JPanel graphSettingsPanel = new JPanel();
	private final JLabel lblGraphDrawDiv = new JLabel("Graph Draw Div");
	private final JComboBox comboBoxGraphDrawDiv = new JComboBox();
		
	private final JPanel lineGraphContainerPanel = new JPanel();
	private final static JPanel lineGraphbottomPanel = new JPanel();
	private final JButton btnIndependentScale = new JButton("Independent");
	private final JButton btnPredatorpreyLinked = new JButton("Predator/Prey");
	private final JButton btnSameScale = new JButton("Same");
	private final JPanel stackGraphContainerPanel = new JPanel();

	public StatsPanel()
	{
		setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Graphs", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setLayout(new BorderLayout(0, 0));
		tabbedGraphs.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) 
			{
				// Allows the graph update function to work out which tab is visible and therefore which graph to draw
				graph_visible = tabbedGraphs.getSelectedIndex(); 
			}
		});

		add(tabbedGraphs, BorderLayout.CENTER);
		
		tabbedGraphs.addTab("Line", null, lineGraphContainerPanel, null);
				lineGraphContainerPanel.setLayout(new BorderLayout(0, 0));
				lineGraphContainerPanel.add(lineGraphbottomPanel, BorderLayout.SOUTH);
				lineGraphbottomPanel.setLayout(new GridLayout(0, 3, 0, 0));
				btnIndependentScale.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) 
					{
						scale_mode = 0;
					}
				});
				
				lineGraphbottomPanel.add(btnIndependentScale);
				btnPredatorpreyLinked.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) 
					{
						scale_mode = 1;
					}
				});
				
				lineGraphbottomPanel.add(btnPredatorpreyLinked);
				btnSameScale.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) 
					{
						scale_mode = 2;
					}
				});
				
				lineGraphbottomPanel.add(btnSameScale);
				lineGraphbottomPanel.setVisible(false);
		
				lineGraphPanel = new StatsLineGraphPanel(plantSamples, preySamples, predSamples, sampleNum, samplePeriod);
				lineGraphContainerPanel.add(lineGraphPanel);
				lineGraphPanel.addMouseListener(new MouseAdapter()
				{
					@Override
					public void mouseClicked(MouseEvent arg0)
					{						
						if(graphs_full) // Only allow extra interface controls on the large view
						{
							if(lineGraphbottomPanel.isVisible())
							{
								lineGraphbottomPanel.setVisible(false);
							}
							else
							{
								lineGraphbottomPanel.setVisible(true);							
							}
							
						}
						
					}
				});

		lineGraphPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		lineGraphPanel.setBackground(Color.gray);

		tabbedGraphs.addTab("Lorenz", null, lorenzContainerPanel, null);
		lorenzContainerPanel.setLayout(new BorderLayout(0, 0));

		lorenzGraphPanel = new StatsLorenzGraphPanel(plantSamples, preySamples, predSamples, sampleNum);
		lorenzGraphPanel.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent arg0) 
			{
				repaint();
			}
		});

		lorenzGraphPanel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if(graphs_full) // Only allow extra interface controls on the large view
				{
					if (rightPanel.isVisible())
					{
						rightPanel.setVisible(false);
						leftPanel.setVisible(false);
						bottomPanel.setVisible(false);
					}
					else
					{
						rightPanel.setVisible(true);
						leftPanel.setVisible(true);
						bottomPanel.setVisible(true);
					}					
				}

				e.consume();
			}
		});
		lorenzContainerPanel.add(lorenzGraphPanel, BorderLayout.CENTER);
		lorenzGraphPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));

		lorenzGraphPanel.addMouseMotionListener(new MouseMotionAdapter()
		{
			public void mouseDragged(MouseEvent e)
			{
				//lorenzGraphPanel.moveGraph(e.getX(), e.getY());
				//e.consume();
			}

			public void mouseMoved(MouseEvent e)
			{
				//lorenzGraphPanel.setMpos(e.getX(), e.getY());
				//e.consume();
			}

		});

		lorenzGraphPanel.addMouseWheelListener(new MouseWheelListener()
		{
			// Adjusts the lorenzZoomSlider (Zoom) 
			public void mouseWheelMoved(MouseWheelEvent e)
			{
				lorenzZoomSlider.setValue((lorenzZoomSlider.getValue() + (e.getWheelRotation() * 10)));
			}
		});

		lorenzGraphPanel.setBackground(Color.black);
		lorenzGraphPanel.setLayout(new BoxLayout(lorenzGraphPanel, BoxLayout.X_AXIS));

		lorenzContainerPanel.add(rightPanel, BorderLayout.EAST);
		rightPanel.setLayout(new BorderLayout(0, 0));
		lorenzZoomSlider.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent arg0)
			{
				lorenzGraphPanel.setZoom(lorenzZoomSlider.getValue());
			}
		});

		rightPanel.setVisible(false);

		lorenzZoomSlider.setPaintLabels(true);
		lorenzZoomSlider.setMajorTickSpacing(100);
		lorenzZoomSlider.setMinorTickSpacing(50);
		lorenzZoomSlider.setMaximum(1000);
		lorenzZoomSlider.setValue(100);
		lorenzZoomSlider.setPaintTicks(true);
		lorenzZoomSlider.setOrientation(SwingConstants.VERTICAL);

		rightPanel.add(lorenzZoomSlider, BorderLayout.CENTER);
		lblZoom.setHorizontalAlignment(SwingConstants.CENTER);

		rightPanel.add(lblZoom, BorderLayout.NORTH);

		lorenzContainerPanel.add(bottomPanel, BorderLayout.SOUTH);
		bottomPanel.setLayout(new BorderLayout(0, 0));
		xScaleslider.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				lorenzGraphPanel.setXScale(xScaleslider.getValue());
			}
		});
		xScaleslider.setPaintTicks(true);
		xScaleslider.setPaintLabels(true);
		xScaleslider.setValue(10);
		xScaleslider.setMajorTickSpacing(10);

		bottomPanel.add(xScaleslider);
		lblXscale.setHorizontalAlignment(SwingConstants.CENTER);

		bottomPanel.add(lblXscale, BorderLayout.WEST);
		bottomPanel.add(rightButtonsPanel, BorderLayout.EAST);
		rightButtonsPanel.setLayout(new GridLayout(0, 1, 0, 0));
		btnMode.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				if (lorenzGraphPanel.getDynamicMode())
				{
					btnMode.setText("Static Mode");
					lorenzGraphPanel.setDynamicMode(false);
				}
				else
				{
					btnMode.setText("Dynamic Mode");
					lorenzGraphPanel.setDynamicMode(true);
				}
			}
		});
		rightButtonsPanel.add(btnMode);
		rightButtonsPanel.add(btnReset);
		btnReset.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				xScaleslider.setValue(10);
				yScaleslider.setValue(10);
				lorenzZoomSlider.setValue(100);
				lorenzGraphPanel.resetGraph(1);			
			}
		});
		bottomPanel.setVisible(false);

		lorenzContainerPanel.add(leftPanel, BorderLayout.WEST);
		leftPanel.setLayout(new BorderLayout(0, 0));
		yScaleslider.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				lorenzGraphPanel.setYScale(yScaleslider.getValue());
			}
		});
		yScaleslider.setMajorTickSpacing(10);
		yScaleslider.setValue(10);
		yScaleslider.setPaintLabels(true);
		yScaleslider.setPaintTicks(true);
		yScaleslider.setOrientation(SwingConstants.VERTICAL);

		leftPanel.add(yScaleslider);

		leftPanel.add(lblYscale, BorderLayout.NORTH);
		
		tabbedGraphs.addTab("Stacked", null, stackGraphContainerPanel, null);
		
		stackedGraphPanel = new StatsStackedGraphPanel(plantSamples, preySamples, predSamples, sampleNum);
		stackedGraphPanel.setBackground(Color.gray);

		stackGraphContainerPanel.setLayout(new BorderLayout(0, 0));
		stackGraphContainerPanel.add(stackedGraphPanel, BorderLayout.CENTER);

		add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BorderLayout(0, 0));
		panel.add(simStatCountPanel);

		simStatCountPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Simulation Statistics", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		simStatCountPanel.setLayout(new GridLayout(2, 6, 0, 0));

		simStatCountPanel.add(alifeInfoRow);
		alifeInfoRow.setLayout(new GridLayout(0, 6, 0, 0));

		lblPlants.setHorizontalAlignment(SwingConstants.CENTER);
		alifeInfoRow.add(lblPlants);
		lblPlantNo.setForeground(new Color(0, 128, 0));

		lblPlantNo.setHorizontalAlignment(SwingConstants.CENTER);
		alifeInfoRow.add(lblPlantNo);

		lblPredators.setHorizontalAlignment(SwingConstants.CENTER);
		alifeInfoRow.add(lblPredators);
		lblPredatorsNo.setForeground(Color.RED);

		lblPredatorsNo.setHorizontalAlignment(SwingConstants.CENTER);
		alifeInfoRow.add(lblPredatorsNo);

		lblPrey.setHorizontalAlignment(SwingConstants.CENTER);
		alifeInfoRow.add(lblPrey);
		lblPreyNo.setForeground(Color.BLUE);

		lblPreyNo.setHorizontalAlignment(SwingConstants.CENTER);
		alifeInfoRow.add(lblPreyNo);

		simStatCountPanel.add(simulationInfoRow);
		simulationInfoRow.setLayout(new GridLayout(0, 6, 0, 0));

		lblASPS.setHorizontalAlignment(SwingConstants.CENTER);
		simulationInfoRow.add(lblASPS);

		lblASPSNo.setHorizontalAlignment(SwingConstants.CENTER);
		simulationInfoRow.add(lblASPSNo);
		lblStep.setHorizontalAlignment(SwingConstants.CENTER);

		simulationInfoRow.add(lblStep);
		lblStepNo.setHorizontalAlignment(SwingConstants.CENTER);

		simulationInfoRow.add(lblStepNo);

		lblRunTime.setHorizontalAlignment(SwingConstants.CENTER);
		simulationInfoRow.add(lblRunTime);

		lblRunTimeNo.setHorizontalAlignment(SwingConstants.CENTER);
		simulationInfoRow.add(lblRunTimeNo);

		panel.add(graphSettingsPanel, BorderLayout.NORTH);
		graphSettingsPanel.setLayout(new GridLayout(0, 3, 0, 0));
		graphSettingsPanel.add(chckbxFullSizeGraphCheckBox);
		chckbxFullSizeGraphCheckBox.setEnabled(false);
		chckbxFullSizeGraphCheckBox.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent arg0)
			{
				if (Simulation.simPaused())
				{
					if (graphs_full)
					{
						setGraphsFull(false);
					}
					else
					{
						setGraphsFull(true);
					}
				}
			}

		});
		chckbxFullSizeGraphCheckBox.setVerticalAlignment(SwingConstants.BOTTOM);
		lblGraphDrawDiv.setHorizontalAlignment(SwingConstants.CENTER);

		graphSettingsPanel.add(lblGraphDrawDiv);
		comboBoxGraphDrawDiv.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent arg0)
			{
				graph_draw_div=(Integer.parseInt(comboBoxGraphDrawDiv.getSelectedItem().toString()));
			}
		});
		comboBoxGraphDrawDiv.setModel(new DefaultComboBoxModel(new String[] {"1", "3", "5", "15", "30", "60", "120", "240", "300"}));

		graphSettingsPanel.add(comboBoxGraphDrawDiv);
		leftPanel.setVisible(false);
		//add(graphPanel, BorderLayout.CENTER);
	}

	/**
	 * Starts adding samples at the start of the array until filled, then moves them back 1 and adds at the end
	 * 
	 * @param pSample
	 */
	public static void addSamplePlantsGraph(int pSample)
	{
		/* Assume pSample is the max */
		plantsMax = pSample;

		// If at the start of the sim add the new sample at the current step pos
		if (stepNo < sampleNum)
		{
			// Finds the max value for scaling
			for (int i = 0; i < (sampleNum - 1); i++)
			{
				/* Max Value */
				if (plantSamples[i] > plantsMax)
				{
					plantsMax = plantSamples[i];
				}
			}

			plantSamples[stepNo] = pSample;			// Store the new sps sample
		}
		else
		// if we have steps matches our array max then add the new one at the
		// end
		{
			// Moves the previous samples back by 1, leaves space for the new
			// sps sample
			for (int i = 0; i < (sampleNum - 1); i++)
			{
				plantSamples[i] = plantSamples[(i + 1)];

				// Finds the max value for scaling
				if (plantSamples[i] > plantsMax)
				{
					plantsMax = plantSamples[i];
				}
			}

			plantSamples[sampleNum - 1] = pSample;			// Store the new sps sample
		}
	}

	/**
	 * Starts adding samples at the start of the array until filled, then moves them back 1 and adds at the end
	 * 
	 * @param pSample
	 */
	public static void addSamplePreyGraph(int pSample)
	{
		/* Assume pSample is the max */
		preyMax = pSample;

		if (stepNo < sampleNum)
		{
			// Finds the max value for scaling
			for (int i = 0; i < (sampleNum - 1); i++)
			{
				/* Max Value */
				if (preySamples[i] > preyMax)
				{
					preyMax = preySamples[i];
				}
			}

			preySamples[stepNo] = pSample;			// Store the new sps sample

		}
		else
		{
			// Moves the previous samples back by 1, leaves space for the new
			// sps sample
			for (int i = 0; i < (sampleNum - 1); i++)
			{
				preySamples[i] = preySamples[(i + 1)];

				// Finds the max value for scaling
				if (preySamples[i] > preyMax)
				{
					preyMax = preySamples[i];
				}
			}

			preySamples[sampleNum - 1] = pSample;			// Store the new sps sample
		}
	}

	/**
	 * Starts adding samples at the start of the array until filled, then moves them back 1 and adds at the end
	 * 
	 * @param pSample
	 */
	public static void addSamplePredGraph(int pSample)
	{
		/* Assume pSample is the max */
		predMax = pSample;

		if (stepNo < sampleNum)
		{
			// Finds the max value for scaling
			for (int i = 0; i < (sampleNum - 1); i++)
			{
				/* Max Value */
				if (predSamples[i] > predMax)
				{
					predMax = predSamples[i];
				}
			}

			predSamples[stepNo] = pSample;			// Store the new sps sample

		}
		else
		{
			// Moves the previous samples back by 1, leaves space for the new
			// sps sample
			for (int i = 0; i < (sampleNum - 1); i++)
			{
				predSamples[i] = predSamples[(i + 1)];

				// Finds the max value for scaling
				if (predSamples[i] > predMax)
				{
					predMax = predSamples[i];
				}
			}

			predSamples[sampleNum - 1] = pSample;			// Store the new sps sample
		}
	}

	/** The total plant numbers */
	public static void setPlantNo(int no)
	{
		plantNo = no;

		lblPlantNo.setText(Integer.toString(plantNo));

		addSamplePlantsGraph(no);
	}

	/** The total number of prey */
	public static void setPreyNo(int no)
	{
		preyNo = no;

		lblPreyNo.setText(Integer.toString(preyNo));

		addSamplePreyGraph(no);
	}

	/** The Total number of predators */
	public static void setPredNo(int no)
	{
		predNo = no;

		lblPredatorsNo.setText(Integer.toString(predNo));

		addSamplePredGraph(predNo);
	}

	/**
	 * The Average Steps per second.
	 * 
	 * @param no
	 */
	public static void setASPS(int no)
	{
		ASPS = no;
		lblASPSNo.setText(Integer.toString(ASPS));
	}

	/**
	 * The curernt step number.
	 * 
	 * @param no
	 */
	public static void setStepNo(int no)
	{
		stepNo = no;
		lblStepNo.setText(Integer.toString(no));
	}

	/**
	 * Called in the update sim loop - displays the current run time of the simulation.
	 * 
	 * @param time
	 */
	public static void setTime(long time)
	{
		time = time / 1000; // seconds
		int days = (int) (time / 86400); // to days
		int hrs = (int) (time / 3600) % 24; // to hrs
		int mins = (int) ((time / 60) % 60);	// to seconds
		int sec = (int) (time % 60);

		lblRunTimeNo.setText(String.format("%d:%02d:%02d:%02d", days, hrs, mins, sec));
	}

	/**
	 * The Graph update step.
	 */
	public void updateGraph()
	{
		/* These could be threaded - TODO more threads! */
		lineGraphPanel.updateGraph(plantsMax, preyMax, predMax, scale_mode, stepNo);
		stackedGraphPanel.updateGraph(plantsMax, preyMax, predMax, stepNo);		
		lorenzGraphPanel.updateGraph(plantsMax, preyMax, predMax, stepNo);		

		/* No need to Draw the graphs every step */
		if(stepNo%graph_draw_div == 0)
		{
			switch(graph_visible)
			{
				case 0:
					lineGraphPanel.drawGraph();
				break;
				case 1:
					lorenzGraphPanel.drawGraph();
				break;
				case 2:
					stackedGraphPanel.drawGraph();
				break;
			}
		}
		

	}

	/** Clears the values in the Arrays of samples */
	public static void clearStats()
	{
		for (int i = 0; i < sampleNum; i++)
		{
			plantSamples[i] = 0;
			preySamples[i] = 0;
			predSamples[i] = 0;
		}
		lorenzGraphPanel.completeResetGraph();
	}

	public static void setPaused(boolean ipaused)
	{
		paused = ipaused;
		
		if (paused)
		{		
			chckbxFullSizeGraphCheckBox.setSelected(false);		
			chckbxFullSizeGraphCheckBox.setEnabled(true);
			
			setGraphsFull(false);				
			
		}
		else
		{
			chckbxFullSizeGraphCheckBox.setSelected(true);			
			chckbxFullSizeGraphCheckBox.setEnabled(false);
			
			setGraphsFull(true);	
		}

	}

	private static void setGraphsFull(boolean status)
	{
		graphs_full = status;
		
		if (status) // Hide the panels/show graphs 
		{								
			SimulationGUI.agentParamPanel.setVisible(false);
			SimulationGUI.plantParamPanel.setVisible(false);		
		}
		else 
		{
			hideLorenzPanels();
			hideLinePanels();
			SimulationGUI.agentParamPanel.setVisible(true);
			SimulationGUI.plantParamPanel.setVisible(true);	
		}		
	}
	
	private static void hideLinePanels()
	{
		lineGraphbottomPanel.setVisible(false);							
	}
	
	private static void showLorenzPanels()
	{
		rightPanel.setVisible(true);
		leftPanel.setVisible(true);
		bottomPanel.setVisible(true);
		lorenzGraphPanel.resetGraph(1);
	}

	private static void hideLorenzPanels()
	{
		rightPanel.setVisible(false);
		leftPanel.setVisible(false);
		bottomPanel.setVisible(false);
		lorenzGraphPanel.resetGraph(1);
	}

}
