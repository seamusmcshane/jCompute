package alifeSim.Gui;

import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.concurrent.Semaphore;
import javax.swing.border.EtchedBorder;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTabbedPane;
import javax.swing.JSlider;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.JCheckBox;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import java.awt.Font;
/**
 * A Custom Panel for controlling the display of Simulation stats in the GUI
 *
 * @author Seamus McShane
 * @version $Revision: 1.0 $
 */
public class StatsPanel extends JPanel
{
	private static final long serialVersionUID = 6716690428061326469L;

	/* Panel Contents */
	private static JPanel simStatCountPanel = new JPanel();
	private static JPanel alifeInfoRow = new JPanel();
	private static JLabel lblPlants = new JLabel("Plants");
	private static JLabel lblPlantNo = new JLabel("0");
	private static JLabel lblPredators = new JLabel("Predators");
	private static JLabel lblPredatorsNo = new JLabel("0");
	private static JLabel lblPrey = new JLabel("Prey");
	private static JLabel lblPreyNo = new JLabel("0");
	private static JLabel lblPlantsMax = new JLabel("0");
	private static JLabel lblPredatorsMax = new JLabel("0");
	private static JLabel lblPreyMax = new JLabel("0");
	
	
	/** The custom panel for drawing the graph */
	private static StatsLineGraphPanel lineGraphPanel;
	private static StatsLorenzGraphPanel lorenzGraphPanel;
	private static StatsStackedGraphPanel stackedGraphPanel;

	/* Counters */
	private static int plantNo = 0;
	private static int preyNo = 0;
	private static int predNo = 0;

	/* Graph Samples - 15 sps * 60 seconds = 900 samples for a minute etc.. */
	private static int samplePeriod = 60;
	private static int sampleNum = 0; // 9000 = 10 mins real-time (15sps) - Set by combox default

	/* Prevents access to the arrays when being regenerated */
	private static Semaphore sampleLock = new Semaphore(1);

	/** The Sample arrays */
	private static int plantSamples[];
	private static int preySamples[];
	private static int predSamples[];

	/* Record of max values for graph scaling */
	private static int plantsMax = 0;
	private static int preyMax = 0;
	private static int predMax = 0;

	/* Graph Scales - click graph */
	private int scaleMode = 2; // 0 = all on own scale, 1 - plants on own scale, prey+pred tied, 2 - all tied

	/* The count of graph updates - used for scroll drawing from lef */
	private static int graphStartVal = 0;

	/* Graph can be seen */
	private static int graphVisible = 0;

	/* Graph State - default */
	private static boolean graphsFull = false;
	
	/* Allows overriding the graph size checks on large screens */
	private static boolean largeScreen=false;

	/* Used to prevent showing sliders in a small area when paused */
	private static boolean paused;

	/* Update the graphs based on ratio of steps */
	private static int graphDrawDiv = 1;

	/* Draw or do not draw the graphs */
	private static boolean drawGraphs = true;
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

	private final JButton btnMode = new JButton("Static Mode");
	private final JPanel rightButtonsPanel = new JPanel();
	private final static JCheckBox chckbxFullSizeGraphCheckBox = new JCheckBox("Full Graphs");
	private final JPanel statsBottomPanel = new JPanel();
	private final JPanel graphSettingsPanel = new JPanel();
	private final JLabel lblGraphDrawDiv = new JLabel("Draw Div");
	private final JComboBox comboBoxGraphDrawDiv = new JComboBox();

	private final JPanel lineGraphContainerPanel = new JPanel();
	private final static JPanel lineGraphbottomPanel = new JPanel();
	private final JButton btnIndependentScale = new JButton("Independent");
	private final JButton btnPredatorpreyLinked = new JButton("Predator/Prey");
	private final JButton btnSameScale = new JButton("Same");
	private final JPanel stackGraphContainerPanel = new JPanel();
	private final JPanel plantNoPanel = new JPanel();
	private final JPanel predatorsNoPanel = new JPanel();
	private final JPanel preyNoPanel = new JPanel();
	private final JLabel lblSamples = new JLabel("Samples");
	private final static JComboBox comboBoxGraphSamples = new JComboBox();
	private final JPanel drawDivPanel = new JPanel();
	private final JPanel samplesPanel = new JPanel();
	private final JLabel label = new JLabel("");
	private final JLabel lblCurrent = new JLabel("Current");
	private final JLabel lblMax = new JLabel("Max");
	private final JPanel plantMaxPanel = new JPanel();
	private final JPanel predatorsMaxPanel = new JPanel();
	private final JPanel preyMaxPanel = new JPanel();

	public StatsPanel()
	{
		setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Statistics", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		setLayout(new BorderLayout(0, 0));
		tabbedGraphs.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				// Allows the graph update function to work out which tab is visible and therefore which graph to draw
				graphVisible = tabbedGraphs.getSelectedIndex();
			}
		});

		add(tabbedGraphs, BorderLayout.CENTER);

		tabbedGraphs.addTab("Line", null, lineGraphContainerPanel, null);
		lineGraphContainerPanel.setLayout(new BorderLayout(0, 0));
		lineGraphContainerPanel.add(lineGraphbottomPanel, BorderLayout.SOUTH);
		lineGraphbottomPanel.setLayout(new GridLayout(0, 3, 0, 0));
		btnIndependentScale.setToolTipText("All lines drawn on independent scales.");
		btnIndependentScale.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				scaleMode = 0;
				lineGraphPanel.setScaleMode(scaleMode);
			}
		});

		lineGraphbottomPanel.add(btnIndependentScale);
		btnPredatorpreyLinked.setToolTipText("Predator and Prey lines drawn on the same scale.");
		btnPredatorpreyLinked.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				scaleMode = 1;
				lineGraphPanel.setScaleMode(scaleMode);
			}
		});

		lineGraphbottomPanel.add(btnPredatorpreyLinked);
		btnSameScale.setToolTipText("All lines drawn on the same scale.");
		btnSameScale.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				scaleMode = 2;
				lineGraphPanel.setScaleMode(scaleMode);
			}
		});

		lineGraphbottomPanel.add(btnSameScale);
		lineGraphbottomPanel.setVisible(false);

		lineGraphPanel = new StatsLineGraphPanel();
		lineGraphPanel.setToolTipText("<html>\r\nA Graph that allows visualizing the total numbers in each group.<br>\r\n<br>\r\nClick to adjust graph scales.<br>\r\n<br>\r\n<font color =red>Red</font> Line - Predators.<br>\r\n<font color =blue>Blue</font> Line - Prey.<br>\r\n<font color =green>Green</font> Line - Plants.<br>\r\n<br>\r\n</html>\r\n");
		lineGraphContainerPanel.add(lineGraphPanel);
		lineGraphPanel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent arg0)
			{
				if (graphsFull || largeScreen) // Only allow extra interface controls on the large view
				{
					if (lineGraphbottomPanel.isVisible())
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

		lorenzGraphPanel = new StatsLorenzGraphPanel();
		lorenzGraphPanel
				.setToolTipText("<html>\r\nA graph that allows visualizing the amount of chaos in the ecosystem.<br>\r\n<br>\r\n<font color =red>Red</font> Pixels - Predators.<br>\r\n<font color =blue>Blue</font> Pixels - Prey.<br>\r\n<font color =green>Green</font> Pixels - Plants.<br>\r\n<br>\r\nClick to adjust graph view.<br>\r\nStatic Mode - draw graph based on total numbers in each group. (Lorenz)<br>\r\nDynamic Mode - draw graph based on ratios of each group to the others. (Modified Lorenz)<br>\r\n<br>\r\nPredators number representation is boosted in dynamic mode for visual aesthetics.<br>\r\n</html>\r\n");
		lorenzGraphPanel.addComponentListener(new ComponentAdapter()
		{
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
				if (graphsFull || largeScreen) // Only allow extra interface controls on the large view
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
		lorenzZoomSlider.setToolTipText("Adjust zoom.");
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
		xScaleslider.setToolTipText("Adjust scale of X axis drawing.");
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
		btnMode.setToolTipText("Toggle the graph drawing mode.");
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
		btnReset.setToolTipText("Reset the graph adjustments to defaults.");
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
		yScaleslider.setToolTipText("Adjust scale of Y axis drawing.");
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
		lblYscale.setHorizontalAlignment(SwingConstants.CENTER);

		leftPanel.add(lblYscale, BorderLayout.NORTH);

		tabbedGraphs.addTab("Stacked", null, stackGraphContainerPanel, null);

		stackedGraphPanel = new StatsStackedGraphPanel();
		stackedGraphPanel.setToolTipText("<html>\r\nA graph that allows visualizing the group ratios of Artifical Life.\r\n<br>\r\n<font color =red>Red</font> Area- Predators.<br>\r\n<font color =blue>Blue</font> Area- Prey.<br>\r\n<font color =green>Green</font> Area- - Plants.<br>\r\n<br>\r\n</html>\r\n");
		stackedGraphPanel.setBackground(Color.gray);

		stackGraphContainerPanel.setLayout(new BorderLayout(0, 0));
		stackGraphContainerPanel.add(stackedGraphPanel, BorderLayout.CENTER);

		add(statsBottomPanel, BorderLayout.SOUTH);
		statsBottomPanel.setLayout(new BorderLayout(0, 0));
		statsBottomPanel.add(simStatCountPanel);

		simStatCountPanel.setBorder(null);
		simStatCountPanel.setLayout(new GridLayout(1, 6, 0, 0));

		simStatCountPanel.add(alifeInfoRow);
		alifeInfoRow.setLayout(new GridLayout(0, 4, 2, 2));
		
		alifeInfoRow.add(label);

		lblPlants.setHorizontalAlignment(SwingConstants.CENTER);
		alifeInfoRow.add(lblPlants);
		
				lblPredators.setHorizontalAlignment(SwingConstants.CENTER);
				alifeInfoRow.add(lblPredators);
		
				lblPrey.setHorizontalAlignment(SwingConstants.CENTER);
				alifeInfoRow.add(lblPrey);
		lblCurrent.setHorizontalAlignment(SwingConstants.CENTER);
		
		alifeInfoRow.add(lblCurrent);
		plantNoPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		plantNoPanel.setBackground(Color.gray);

		alifeInfoRow.add(plantNoPanel);
		plantNoPanel.setLayout(new BorderLayout(0, 0));
		lblPlantNo.setToolTipText("Plant Numbers.");
		lblPlantNo.setFont(lblPlantNo.getFont().deriveFont(lblPlantNo.getFont().getStyle() | Font.BOLD));
		plantNoPanel.add(lblPlantNo, BorderLayout.CENTER);
		lblPlantNo.setForeground(Color.green);

		lblPlantNo.setHorizontalAlignment(SwingConstants.CENTER);
		predatorsNoPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		predatorsNoPanel.setBackground(Color.gray);

		alifeInfoRow.add(predatorsNoPanel);
		predatorsNoPanel.setLayout(new BorderLayout(0, 0));
		lblPredatorsNo.setToolTipText("Predator Numbers.");
		lblPredatorsNo.setFont(lblPlantNo.getFont().deriveFont(lblPlantNo.getFont().getStyle() | Font.BOLD));
		predatorsNoPanel.add(lblPredatorsNo, BorderLayout.CENTER);
		lblPredatorsNo.setForeground(Color.RED);

		lblPredatorsNo.setHorizontalAlignment(SwingConstants.CENTER);
		preyNoPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		preyNoPanel.setBackground(Color.gray);

		alifeInfoRow.add(preyNoPanel);
		preyNoPanel.setLayout(new BorderLayout(0, 0));
		lblPreyNo.setToolTipText("Prey Numbers.");
		lblPreyNo.setFont(lblPlantNo.getFont().deriveFont(lblPlantNo.getFont().getStyle() | Font.BOLD));
		preyNoPanel.add(lblPreyNo, BorderLayout.CENTER);
		lblPreyNo.setForeground(Color.BLUE);

		lblPreyNo.setHorizontalAlignment(SwingConstants.CENTER);
		lblMax.setHorizontalAlignment(SwingConstants.CENTER);
		
		alifeInfoRow.add(lblMax);
		plantMaxPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		plantMaxPanel.setBackground(Color.GRAY);
		
		alifeInfoRow.add(plantMaxPanel);
		plantMaxPanel.setLayout(new BorderLayout(0, 0));
		lblPlantsMax.setToolTipText("Max Plants in this Sample Period.");
		lblPlantsMax.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblPlantsMax.setForeground(Color.GREEN);
		lblPlantsMax.setHorizontalAlignment(SwingConstants.CENTER);
		
		plantMaxPanel.add(lblPlantsMax, BorderLayout.CENTER);
		predatorsMaxPanel.setBackground(Color.GRAY);
		predatorsMaxPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		
		alifeInfoRow.add(predatorsMaxPanel);
		predatorsMaxPanel.setLayout(new BorderLayout(0, 0));
		lblPredatorsMax.setToolTipText("Max Predators in this Sample Period.");
		lblPredatorsMax.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblPredatorsMax.setForeground(Color.RED);
		lblPredatorsMax.setHorizontalAlignment(SwingConstants.CENTER);
		
		predatorsMaxPanel.add(lblPredatorsMax, BorderLayout.CENTER);
		preyMaxPanel.setBackground(Color.GRAY);
		preyMaxPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		
		alifeInfoRow.add(preyMaxPanel);
		preyMaxPanel.setLayout(new BorderLayout(0, 0));
		lblPreyMax.setToolTipText("Max Prey in this Sample Period.");
		lblPreyMax.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblPreyMax.setForeground(Color.BLUE);
		lblPreyMax.setHorizontalAlignment(SwingConstants.CENTER);
		
		preyMaxPanel.add(lblPreyMax, BorderLayout.CENTER);

		statsBottomPanel.add(graphSettingsPanel, BorderLayout.NORTH);
		graphSettingsPanel.setLayout(new GridLayout(0, 3, 0, 0));
		chckbxFullSizeGraphCheckBox.setToolTipText("Toggle the viewing size of the graph.");
		chckbxFullSizeGraphCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
		graphSettingsPanel.add(chckbxFullSizeGraphCheckBox);

		chckbxFullSizeGraphCheckBox.setEnabled(true);

		/* Set to value of graphs full */
		chckbxFullSizeGraphCheckBox.setSelected(graphsFull);

		chckbxFullSizeGraphCheckBox.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent arg0)
			{
				/* Paused not used anymore */
				//if (Simulation.simPaused())
				{
					if (graphsFull) // Toggle graph size
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

		graphSettingsPanel.add(samplesPanel);
		samplesPanel.setLayout(new GridLayout(0, 2, 0, 0));
		samplesPanel.add(lblSamples);
		lblSamples.setToolTipText("");
		lblSamples.setHorizontalAlignment(SwingConstants.CENTER);
		samplesPanel.add(comboBoxGraphSamples);
		comboBoxGraphSamples.setToolTipText("<html>\r\nAllows changing the length of the sample perioid covered by graphs.<br>\r\n<br>\r\nCalculation -:<br>\r\n 15 steps/sec * 60  * 5 = 4500 samples for five minutes)<br>\r\n<br>\r\nNote 1 : Large sample periods can negatively affect performance.<br>\r\nNote 2 : Changing this value will clear all samples from the current sample period.<br>\r\n</html>");

		comboBoxGraphSamples.setModel(new DefaultComboBoxModel(new String[] {"1125", "2250", "4500", "9000", "18000", "36000"}));

		comboBoxGraphSamples.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				sampleNum = Integer.parseInt(comboBoxGraphSamples.getSelectedItem().toString());

				/* Clear the graph */
				clearStats();
			}
		});
		comboBoxGraphSamples.setSelectedIndex(4);

		graphSettingsPanel.add(drawDivPanel);
		drawDivPanel.setLayout(new GridLayout(0, 2, 0, 0));
		drawDivPanel.add(lblGraphDrawDiv);
		lblGraphDrawDiv.setToolTipText("");
		lblGraphDrawDiv.setHorizontalAlignment(SwingConstants.CENTER);
		drawDivPanel.add(comboBoxGraphDrawDiv);
		comboBoxGraphDrawDiv.setToolTipText("<html>\r\nChanges the drawing rate of the graphs vs step rate of the simulation. <br>\r\n\r\nCalculation :\r\nAverage step rate/draw div = graph update rate~\r\n\r\n</html>");
		comboBoxGraphDrawDiv.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent arg0)
			{
				String drawDivString = comboBoxGraphDrawDiv.getSelectedItem().toString();

				// Enable / Drawing or set the draw div
				if (drawDivString.equalsIgnoreCase("Off"))
				{
					drawGraphs = false;
				}
				else
				{
					drawGraphs = true;

					graphDrawDiv = Integer.parseInt(drawDivString);
				}
			}
		});
		comboBoxGraphDrawDiv.setModel(new DefaultComboBoxModel(new String[]
		{"Off", "1", "3", "5", "15", "30", "60", "120", "240", "300"}));
		comboBoxGraphDrawDiv.setSelectedIndex(1);

		leftPanel.setVisible(false);

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
		if (graphStartVal < sampleNum)
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

			plantSamples[graphStartVal] = pSample;			// Store the new sps sample
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
		
		lblPlantsMax.setText(Integer.toString(plantsMax));

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

		if (graphStartVal < sampleNum)
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

			preySamples[graphStartVal] = pSample;			// Store the new sps sample

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
		
		lblPreyMax.setText(Integer.toString(preyMax));

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

		if (graphStartVal < sampleNum)
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

			predSamples[graphStartVal] = pSample;			// Store the new sps sample

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
		
		lblPredatorsMax.setText(Integer.toString(predMax));
		
	}

	/** The total plant numbers 
	 * @param no int
	 */
	public static void setPlantNo(int no)
	{
		sampleLock.acquireUninterruptibly();

		plantNo = no;

		lblPlantNo.setText(Integer.toString(plantNo));

		addSamplePlantsGraph(no);

		sampleLock.release();

	}

	/** The total number of prey 
	 * @param no int
	 */
	public static void setPreyNo(int no)
	{
		sampleLock.acquireUninterruptibly();

		preyNo = no;

		lblPreyNo.setText(Integer.toString(preyNo));

		addSamplePreyGraph(no);

		sampleLock.release();

	}

	/** The Total number of predators 
     * @param no int
	 */
	public static void setPredNo(int no)
	{
		sampleLock.acquireUninterruptibly();

		predNo = no;

		lblPredatorsNo.setText(Integer.toString(predNo));

		addSamplePredGraph(predNo);

		sampleLock.release();
	}

	/**
	 * The Graph update step.
	 */
	public static void updateGraphs(int stepNo)
	{
		/* These could be threaded - TODO threads for graphs */
		lineGraphPanel.updateGraph(plantsMax, preyMax, predMax, graphStartVal);
		stackedGraphPanel.updateGraph(plantsMax, preyMax, predMax, graphStartVal);
		lorenzGraphPanel.updateGraph(plantsMax, preyMax, predMax, graphStartVal);

		/* Draw the graphs every X no of steps only draw graphs if enabled */
		if (stepNo % graphDrawDiv == 0 && drawGraphs)
		{
			switch (graphVisible)
			{
				case 0 :
					lineGraphPanel.drawGraph();
					break;
				case 1 :
					lorenzGraphPanel.drawGraph();
					break;
				case 2 :
					stackedGraphPanel.drawGraph();
					break;
			}
		}

		graphStartVal++; // Keep count of update

	}

	/** Clears the values in the Arrays of samples */
	public static void clearStats()
	{
		/* Lock the arrays as we are about to clear them */
		sampleLock.acquireUninterruptibly();
		
		/* Current */
		plantNo = 0;
		lblPlantNo.setText(Integer.toString(plantNo));

		predNo = 0;
		lblPredatorsNo.setText(Integer.toString(predNo));

		preyNo = 0;
		lblPreyNo.setText(Integer.toString(preyNo));

		/* Max */
		plantsMax = 0;
		lblPlantsMax.setText(Integer.toString(plantNo));

		predMax = 0;
		lblPredatorsMax.setText(Integer.toString(predMax));

		preyMax = 0;
		lblPreyMax.setText(Integer.toString(preyMax));
		
		graphStartVal = 0; // Graph samples has been cleared start at 0 again.

		plantSamples = new int[sampleNum];
		preySamples = new int[sampleNum];
		predSamples = new int[sampleNum];

		lineGraphPanel.setSampleArrays(plantSamples, preySamples, predSamples, sampleNum);
		stackedGraphPanel.setSampleArrays(plantSamples, preySamples, predSamples, sampleNum);
		lorenzGraphPanel.setSampleArrays(plantSamples, preySamples, predSamples, sampleNum);

		lorenzGraphPanel.completeResetGraph();

		lineGraphPanel.drawGraph();
		lorenzGraphPanel.drawGraph();
		stackedGraphPanel.drawGraph();

		sampleLock.release();

	}

	/**
	 * Method setPaused.
	 * @param ipaused boolean
	 */
	public static void setPaused(boolean ipaused)
	{
		paused = ipaused;

		if (paused)
		{
			//chckbxFullSizeGraphCheckBox.setSelected(false);		
			//chckbxFullSizeGraphCheckBox.setEnabled(true);

			comboBoxGraphSamples.setEnabled(true);

			//setGraphsFull(false);				

		}
		else
		{
			//chckbxFullSizeGraphCheckBox.setSelected(true);			
			//chckbxFullSizeGraphCheckBox.setEnabled(false);

			comboBoxGraphSamples.setEnabled(false);

			//setGraphsFull(true);	
		}

	}

	/**
	 * Method setGraphsFull.
	 * @param status boolean
	 */
	private static void setGraphsFull(boolean status)
	{
		graphsFull = status;

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

	/**
	 * Method setLargeScreen.
	 * @param inLargeScreen boolean
	 */
	public static void setLargeScreen(boolean inLargeScreen)
	{
		largeScreen=inLargeScreen;
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
