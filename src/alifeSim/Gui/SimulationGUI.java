package alifeSim.Gui;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javax.swing.border.TitledBorder;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JCheckBoxMenuItem;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.ButtonGroup;
import java.awt.Color;
import java.io.File;

import javax.swing.ImageIcon;

import alifeSim.Scenario.ScenarioInf;
import alifeSim.Scenario.ScenarioVT;
import alifeSim.Scenario.Debug.DebugScenario;
import alifeSim.Scenario.SAPP.SAPPScenario;
import alifeSim.Simulation.Simulation;
/**
 * This class manages user interaction in setting up and controlling the
 * simulation. The GUI was designed in WindowBuilder.
 * 
 * @author Seamus McShane
 * @version $Revision: 1.0 $
 */
public class SimulationGUI
{
	/* The GUI frame */
	private static JFrame gui;

	/* Gui Frame Items */
	private static JSlider simRateSlider;
	private static JButton btnGenerate;
	private static JButton btnPause;
	private static JButton btnStart;
	private static JLabel lblSimRateInfo;

	/* The statistics panel */
	private static StatsPanel statsPanel;

	/* retrieved screen sizes */
	static int screenWidth;
	static int screenHeight;
	static int screenHeightMin = 900;

	/*
	 * Window Size pad - certain operating systems like to add thick borders to
	 * windows then report the size with out telling you the border is not
	 * included.
	 */
	static int windowPad = 10;
	static int titlePad = 48;

	/* GUI Size Hard-Coded - minimum size before cropping occurs */
	static int controlGuiWidth = 375;
	static int controlGuiHeightMin = 806;
	static int controlGuiHeight = controlGuiHeightMin;

	/* Start up position - dynamically generated */
	static int controlGuiX = 0;
	static int controlGuiY = 0;

	/* Auto sized simulation view */
	static int viewWidth = 0;
	static int viewHeight = 0;
	static int viewX;
	static int viewY;

	/* Simulation Reference */
	private static Simulation sim;
	private static ScenarioInf simScenario;
	
	// The GUI menu (AutoGenerated code)
	private static JCheckBoxMenuItem chckbxmntmDrawFieldOf;
	private static JPanel simRateInfoPanel;

	private static JMenu mnFrameRate;
	private static JRadioButtonMenuItem rdbtnmntm15FramesPerSecond;
	private static JRadioButtonMenuItem rdbtnmntm60FramesPerSecond;
	private static final ButtonGroup frameRateButtonGroup = new ButtonGroup();
	private static JRadioButtonMenuItem rdbtnmntmUnlimitedFrameRate;
	private static JMenu mnVerticalSync;
	private static JRadioButtonMenuItem rdbtnmntmVsyncOn;
	private static JRadioButtonMenuItem rdbtnmntmVsyncOff;
	private static final ButtonGroup vSyncButtonGroup = new ButtonGroup();
	private static JMenu mnOverlay;
	private static JRadioButtonMenuItem rdbtnmntmOverlayEnabled;
	private static JRadioButtonMenuItem rdbtnmntmOverlayDisabled;
	private static final ButtonGroup overlayButtonGroup = new ButtonGroup();
	private static JCheckBoxMenuItem chckbxmntmDisplayView;
	private static JCheckBoxMenuItem chckbxmntmDrawSimpleBodies;
	private static JCheckBoxMenuItem chckbxmntDrawAgentViews;

	private static JPanel controlPanelBottom;
	private static JLabel lblStepRate;
	private static JLabel lblASPSNo;
	private static JLabel lblSteps;
	private static JLabel lblStepNo;
	private static JLabel label_4;
	private static JLabel lblRunTimeNo;
	private static JPanel ASPSNoPanel;
	private static JPanel stepNoPanel;
	private static JPanel lblRunTimeNoPanel;

	/* The popup for first time users */
	private static boolean simUnlockNotifcationShown = false;

	/* Prevent over clicking the generate button */
	private static boolean generatingSim = false;

	/* Logic */

	/**
	 * 
	 * @param args
	 *            String[]
	 */
	public static void main(String args[])
	{
		System.out.println("Artificial Life Simulation Started.");

		retrieveScreenSize();

		calculateWindowSizes();

		calculateWindowPositions();

		setUpFrame();

		setUpSimulation();

		screenSizeCheck();
		
		// Display the simulation view
		SimulationView.displayView(sim, viewX, viewY, viewWidth, viewHeight);

		setUpToolTips();
		
		// have a simulation ready to go...
		newSim();
	}

	private static void setUpSimulation()
	{
		sim = new Simulation();
	}

	private static void newSim()
	{

		System.out.println("New Simulation");

		/* Cleans up the old simulation threads */
		sim.destroySim();

		if(simScenario==null)
		{			
			determinScenarios(new File("scenarios/default.txt"));
		}
		
		sim.createSim(statsPanel, simScenario);
		
		// Centers the simulated world in the view
		SimulationView.setInitalViewTranslate(viewWidth, viewHeight);
		
		/*
		 * If needed the GC can free old objects now, before the simulation
		 * starts
		 */
		System.gc();

		btnGenerate.setEnabled(true);

		btnStart.setEnabled(true);

		btnPause.setEnabled(false);

		btnPause.setText("   Pause");

		btnPause.setIcon(new ImageIcon(SimulationGUI.class.getResource("/alifeSim/icons/pause.png")));

		simRateSlider.setEnabled(false);

		simRateSlider.setValue(15);

		/* Clear the old stats */
		StatsPanel.clearStats();

		clearGUIStats();
		
		chckbxmntDrawAgentViews.setSelected(true);
		chckbxmntmDrawFieldOf.setSelected(true);

	}

	private static void calculateWindowSizes()
	{
		String hostPlatform = System.getProperty("os.name");

		if (hostPlatform.contains("Windows"))
		{
			viewWidth = screenWidth - controlGuiWidth - (windowPad * 2);

			controlGuiHeight = controlGuiHeightMin;

			if (controlGuiHeight < screenHeight)
			{
				controlGuiHeight = screenHeight - titlePad;

				viewHeight = controlGuiHeight;
			}
			else
			{
				controlGuiHeight = controlGuiHeightMin;
				viewHeight = screenHeight;
			}

		}
		else
			if (hostPlatform.contains("Linux"))
			{
				viewWidth = screenWidth - controlGuiWidth - (windowPad * 2);

				controlGuiHeight = controlGuiHeightMin;

				if (controlGuiHeight < screenHeight)
				{
					controlGuiHeight = screenHeight - titlePad;

					viewHeight = controlGuiHeight;
				}
				else
				{
					controlGuiHeight = controlGuiHeightMin;
					viewHeight = screenHeight;
				}

			}
			else
			// remove the title pad size on mac and linux
			{
				viewWidth = screenWidth - controlGuiWidth - (windowPad * 2);
				viewHeight = screenHeight - (titlePad); // Task manager and Top
														// borders on some os's

				controlGuiHeight = controlGuiHeightMin;

				if (controlGuiHeight < screenHeight - (titlePad * 2))
				{
					controlGuiHeight = screenHeight - (titlePad * 2);
				}
				else
				{
					controlGuiHeight = controlGuiHeightMin;
					viewHeight = screenHeight;
				}
			}
		System.out.println("Detected : " + hostPlatform);

	}

	/*
	 * Notify users they will have problems running with a small resolutions
	 * screen
	 */
	private static void screenSizeCheck()
	{
		if (screenHeight < screenHeightMin)
		{
			String message;
			message = "Your screen height is below the minimum recommended screen height of " + screenHeightMin + " pixels.";

			JOptionPane wariningPane = new JOptionPane(message, JOptionPane.WARNING_MESSAGE);

			JDialog dialog = wariningPane.createDialog(null, "Screen Size Warning");

			dialog.pack();
			dialog.setVisible(true);
		}
		else
			if (screenHeight >= 1024) // 1280*1024 minimum to be treated as a
										// large screen
			{
				guiOnLargeScreen();		 // Set the large set switch on
			}
	}

	private static void guiOnLargeScreen()
	{
		StatsPanel.setLargeScreen(true);	// Set the panel large screen overrides
	}

	private static void calculateWindowPositions()
	{
		String os = System.getProperty("os.name");

		if (os.contains("Windows"))
		{
			/* OS look for Window */
			lookandFeel();

			controlGuiX = (screenWidth / 2) - ((controlGuiWidth + viewWidth) / 2) - (windowPad / 2);
			controlGuiY = 1;

			viewX = controlGuiX + (controlGuiWidth) + windowPad;
			viewY = controlGuiY;
		}
		else
		// mac / linux
		{
			controlGuiX = (screenWidth / 2) - ((controlGuiWidth + viewWidth) / 2) - (windowPad / 2);
			controlGuiY = (screenHeight / 2) - (viewHeight / 2);

			viewX = controlGuiX + (controlGuiWidth) + windowPad;
			viewY = controlGuiY;
		}

	}

	private static void retrieveScreenSize()
	{
		Toolkit toolkit = Toolkit.getDefaultToolkit();

		Dimension screenSize = toolkit.getScreenSize();

		screenWidth = (int) screenSize.getWidth();
		screenHeight = (int) screenSize.getHeight();

		System.out.println("Screen Size :" + screenWidth + "x" + screenHeight);
	}

	/**
	 * This method contains sections that are largely auto generated from the
	 * editor
	 **/
	private static void setUpFrame()
	{
		gui = new JFrame();
		gui.setResizable(false);
		gui.setTitle("Alife Simulation");
		gui.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // We control
																	// the exit

		// GUI Size
		gui.setBounds(controlGuiX, controlGuiY, controlGuiWidth, controlGuiHeight);

		gui.getContentPane().setLayout(new BorderLayout(0, 0));

		JPanel controlPanel = new JPanel();
		controlPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		gui.getContentPane().add(controlPanel, BorderLayout.CENTER);

		controlPanel.setLayout(new BorderLayout(0, 0));

		controlPanelBottom = new JPanel();
		controlPanelBottom.setBorder(new TitledBorder(null, "Control", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		controlPanel.add(controlPanelBottom, BorderLayout.SOUTH);
		GridBagLayout gbl_controlPanelBottom = new GridBagLayout();
		gbl_controlPanelBottom.columnWidths = new int[]
		{112, 112, 112, 0};
		gbl_controlPanelBottom.rowHeights = new int[]
		{31, 31, 31, 31, 0};
		gbl_controlPanelBottom.columnWeights = new double[]
		{0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_controlPanelBottom.rowWeights = new double[]
		{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		controlPanelBottom.setLayout(gbl_controlPanelBottom);

		lblStepRate = new JLabel("Step Rate");
		lblStepRate.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_lblStepRate = new GridBagConstraints();
		gbc_lblStepRate.fill = GridBagConstraints.BOTH;
		gbc_lblStepRate.insets = new Insets(0, 0, 5, 5);
		gbc_lblStepRate.gridx = 0;
		gbc_lblStepRate.gridy = 0;
		controlPanelBottom.add(lblStepRate, gbc_lblStepRate);

		lblSteps = new JLabel("Steps");
		lblSteps.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_lblSteps = new GridBagConstraints();
		gbc_lblSteps.fill = GridBagConstraints.BOTH;
		gbc_lblSteps.insets = new Insets(0, 0, 5, 5);
		gbc_lblSteps.gridx = 1;
		gbc_lblSteps.gridy = 0;
		controlPanelBottom.add(lblSteps, gbc_lblSteps);

		JLabel lblSimRate = new JLabel("Requested Step Rate");
		GridBagConstraints gbc_lblSimRate = new GridBagConstraints();
		gbc_lblSimRate.fill = GridBagConstraints.BOTH;
		gbc_lblSimRate.insets = new Insets(0, 0, 5, 0);
		gbc_lblSimRate.gridx = 2;
		gbc_lblSimRate.gridy = 0;
		controlPanelBottom.add(lblSimRate, gbc_lblSimRate);
		lblSimRate.setHorizontalAlignment(SwingConstants.CENTER);

		ASPSNoPanel = new JPanel();
		ASPSNoPanel.setBackground(Color.WHITE);
		ASPSNoPanel.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_ASPSNoPanel = new GridBagConstraints();
		gbc_ASPSNoPanel.fill = GridBagConstraints.BOTH;
		gbc_ASPSNoPanel.insets = new Insets(0, 0, 5, 5);
		gbc_ASPSNoPanel.gridx = 0;
		gbc_ASPSNoPanel.gridy = 1;
		controlPanelBottom.add(ASPSNoPanel, gbc_ASPSNoPanel);
		ASPSNoPanel.setLayout(new BorderLayout(0, 0));

		lblASPSNo = new JLabel("0");
		ASPSNoPanel.add(lblASPSNo);
		lblASPSNo.setToolTipText("Average steps per second over the last 150 steps.");
		lblASPSNo.setHorizontalAlignment(SwingConstants.CENTER);

		stepNoPanel = new JPanel();
		stepNoPanel.setBackground(Color.WHITE);
		stepNoPanel.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_stepNoPanel = new GridBagConstraints();
		gbc_stepNoPanel.fill = GridBagConstraints.BOTH;
		gbc_stepNoPanel.insets = new Insets(0, 0, 5, 5);
		gbc_stepNoPanel.gridx = 1;
		gbc_stepNoPanel.gridy = 1;
		controlPanelBottom.add(stepNoPanel, gbc_stepNoPanel);
		stepNoPanel.setLayout(new BorderLayout(0, 0));

		lblStepNo = new JLabel("0");
		stepNoPanel.add(lblStepNo);
		lblStepNo.setToolTipText("Total number of simulated steps.");
		lblStepNo.setHorizontalAlignment(SwingConstants.CENTER);

		simRateInfoPanel = new JPanel();
		simRateInfoPanel.setBackground(Color.WHITE);
		GridBagConstraints gbc_simRateInfoPanel = new GridBagConstraints();
		gbc_simRateInfoPanel.fill = GridBagConstraints.BOTH;
		gbc_simRateInfoPanel.insets = new Insets(0, 0, 5, 0);
		gbc_simRateInfoPanel.gridx = 2;
		gbc_simRateInfoPanel.gridy = 1;
		controlPanelBottom.add(simRateInfoPanel, gbc_simRateInfoPanel);
		simRateInfoPanel.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		simRateInfoPanel.setLayout(new BorderLayout(0, 0));
		lblSimRateInfo = new JLabel();
		lblSimRateInfo.setToolTipText("Requested step rate.");

		simRateInfoPanel.add(lblSimRateInfo, BorderLayout.CENTER);
		lblSimRateInfo.setHorizontalAlignment(SwingConstants.CENTER);
		lblSimRateInfo.setText("15");

		label_4 = new JLabel("Time");
		label_4.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_label_4 = new GridBagConstraints();
		gbc_label_4.fill = GridBagConstraints.BOTH;
		gbc_label_4.insets = new Insets(0, 0, 5, 5);
		gbc_label_4.gridx = 0;
		gbc_label_4.gridy = 2;
		controlPanelBottom.add(label_4, gbc_label_4);

		lblRunTimeNoPanel = new JPanel();
		lblRunTimeNoPanel.setBackground(Color.WHITE);
		lblRunTimeNoPanel.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_lblRunTimeNoPanel = new GridBagConstraints();
		gbc_lblRunTimeNoPanel.fill = GridBagConstraints.BOTH;
		gbc_lblRunTimeNoPanel.insets = new Insets(0, 0, 5, 5);
		gbc_lblRunTimeNoPanel.gridx = 1;
		gbc_lblRunTimeNoPanel.gridy = 2;
		controlPanelBottom.add(lblRunTimeNoPanel, gbc_lblRunTimeNoPanel);
		lblRunTimeNoPanel.setLayout(new BorderLayout(0, 0));

		lblRunTimeNo = new JLabel("0");
		lblRunTimeNoPanel.add(lblRunTimeNo);
		lblRunTimeNo.setToolTipText("Real-Time counter of how long the simualtion has run.");
		lblRunTimeNo.setHorizontalAlignment(SwingConstants.CENTER);
		simRateSlider = new JSlider();
		simRateSlider.setPaintTicks(true);
		GridBagConstraints gbc_simRateSlider = new GridBagConstraints();
		gbc_simRateSlider.fill = GridBagConstraints.BOTH;
		gbc_simRateSlider.insets = new Insets(0, 0, 5, 0);
		gbc_simRateSlider.gridx = 2;
		gbc_simRateSlider.gridy = 2;
		controlPanelBottom.add(simRateSlider, gbc_simRateSlider);
		simRateSlider.setToolTipText("Adjust requested step rate.");
		simRateSlider.setMinimum(15);
		simRateSlider.setMaximum(300);
		simRateSlider.setValue(15);
		simRateSlider.setSnapToTicks(true);
		simRateSlider.setMinorTickSpacing(15);
		simRateSlider.setMajorTickSpacing(30);
		simRateSlider.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
				// Prevent a 0 value being set
				if (simRateSlider.getValue() == 0)
				{
					lblSimRateInfo.setText("1");

					// Set the requested update rate
					sim.reqSimUpdateRate(simRateSlider.getValue());
				}
				else
				{
					if (simRateSlider.getValue() < 300)
					{
						lblSimRateInfo.setText(Integer.toString(simRateSlider.getValue()));

						// Set the requested update rate
						sim.reqSimUpdateRate(simRateSlider.getValue());
					}
					else
					{
						lblSimRateInfo.setText("Unli");

						// Set the requested update rate
						sim.reqSimUpdateRate(-1);
					}

				}

			}
		});

		btnGenerate = new JButton("Generate");
		btnGenerate.setIcon(new ImageIcon(SimulationGUI.class.getResource("/alifeSim/icons/grid.png")));
		GridBagConstraints gbc_btnGenerate = new GridBagConstraints();
		gbc_btnGenerate.fill = GridBagConstraints.BOTH;
		gbc_btnGenerate.insets = new Insets(0, 0, 0, 5);
		gbc_btnGenerate.gridx = 0;
		gbc_btnGenerate.gridy = 3;
		controlPanelBottom.add(btnGenerate, gbc_btnGenerate);
		btnGenerate.setToolTipText("Generate a new simuation based on the values of the parameters.");
		btnGenerate.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				/* Not already generating Sim */
				if (!generatingSim)
				{
					generatingSim = true;

					/* Create the new Simulation */
					newSim();

					generatingSim = false;

				}
			}
		});

		btnStart = new JButton("Start");
		btnStart.setIcon(new ImageIcon(SimulationGUI.class.getResource("/alifeSim/icons/play.png")));
		GridBagConstraints gbc_btnStart = new GridBagConstraints();
		gbc_btnStart.fill = GridBagConstraints.BOTH;
		gbc_btnStart.insets = new Insets(0, 0, 0, 5);
		gbc_btnStart.gridx = 1;
		gbc_btnStart.gridy = 3;
		controlPanelBottom.add(btnStart, gbc_btnStart);
		btnStart.setToolTipText("Start the simulation.");
		btnStart.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				// Change to the start state
				simStartedState();
			}
		});

		btnPause = new JButton("Pause");
		btnPause.setIcon(new ImageIcon(SimulationGUI.class.getResource("/alifeSim/icons/pause.png")));
		GridBagConstraints gbc_btnPause = new GridBagConstraints();
		gbc_btnPause.fill = GridBagConstraints.BOTH;
		gbc_btnPause.gridx = 2;
		gbc_btnPause.gridy = 3;
		controlPanelBottom.add(btnPause, gbc_btnPause);
		btnPause.setToolTipText("Pause / Unpause the simulation.");
		btnPause.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				// Pause Toggle
				if (sim.simPaused())
				{
					simUnPausedState();
				}
				else
				{
					simPausedState();
				}
			}
		});

		statsPanel = new StatsPanel();

		controlPanel.add(statsPanel, BorderLayout.CENTER);

		JMenuBar menuBar = new JMenuBar();
		gui.setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		JMenuItem mntmOpen = new JMenuItem("Open");
		mnFile.add(mntmOpen);
		mntmOpen.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				// Exit the sim
				doFileOpen();
			}
		});

		JMenuItem mntmQuit = new JMenuItem("Quit");
		mntmQuit.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				// Exit the sim
				doSimExit();
			}
		});
		mnFile.add(mntmQuit);

		JMenu mnViewOptions = new JMenu("View");
		menuBar.add(mnViewOptions);

		chckbxmntmDisplayView = new JCheckBoxMenuItem("Display View");
		chckbxmntmDisplayView.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent arg0)
			{
				if (chckbxmntmDisplayView.isSelected())
				{
					// have been checked
					SimulationView.setVisible(true);
				}
				else
				{
					// have been unchecked
					SimulationView.setVisible(false);
				}

			}
		});
		chckbxmntmDisplayView.setSelected(true);
		mnViewOptions.add(chckbxmntmDisplayView);

		JMenu mnAgentDrawing = new JMenu("Agent Drawing");
		mnViewOptions.add(mnAgentDrawing);

		chckbxmntmDrawSimpleBodies = new JCheckBoxMenuItem("Draw Simple Bodies");
		chckbxmntmDrawSimpleBodies.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent arg0)
			{
				if (chckbxmntmDrawSimpleBodies.isSelected())
				{
					// have been checked
					SimulationView.setSimpleDrawing(false);
				}
				else
				{
					// have been unchecked
					SimulationView.setSimpleDrawing(true);
				}
			}
		});
		mnAgentDrawing.add(chckbxmntmDrawSimpleBodies);
		chckbxmntmDrawSimpleBodies.setSelected(true);

		chckbxmntmDrawFieldOf = new JCheckBoxMenuItem("Draw Field of Views");
		chckbxmntmDrawFieldOf.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent arg0)
			{
				if (chckbxmntmDrawFieldOf.isSelected())
				{
					// have been checked
					SimulationView.setViewRangeDrawing(true);
				}
				else
				{
					// have been unchecked
					SimulationView.setViewRangeDrawing(false);
				}
			}
		});
		mnAgentDrawing.add(chckbxmntmDrawFieldOf);
		
		chckbxmntDrawAgentViews = new JCheckBoxMenuItem("Draw Agent Views");
		chckbxmntDrawAgentViews.addItemListener(new ItemListener() 
		{
			public void itemStateChanged(ItemEvent arg0) 
			{
				if (chckbxmntDrawAgentViews.isSelected())
				{
					// have been checked
					SimulationView.setViewsDrawing(true);
				}
				else
				{
					// have been unchecked
					SimulationView.setViewsDrawing(false);
				}				
			}
		});
		mnAgentDrawing.add(chckbxmntDrawAgentViews);
		
		mnFrameRate = new JMenu("Frame Rate");
		mnViewOptions.add(mnFrameRate);

		rdbtnmntm15FramesPerSecond = new JRadioButtonMenuItem("15 Frames Per Second");
		rdbtnmntm15FramesPerSecond.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				// Change the frame rate of the view to standard (15)
				SimulationView.setStandardUpdateRate();
			}
		});

		frameRateButtonGroup.add(rdbtnmntm15FramesPerSecond);
		mnFrameRate.add(rdbtnmntm15FramesPerSecond);

		rdbtnmntm60FramesPerSecond = new JRadioButtonMenuItem("60 Frames Per Second");
		rdbtnmntm60FramesPerSecond.setSelected(true);
		rdbtnmntm60FramesPerSecond.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				// Change the frame rate of the view to high (60)
				SimulationView.setHighUpdateRate();
			}
		});
		frameRateButtonGroup.add(rdbtnmntm60FramesPerSecond);
		mnFrameRate.add(rdbtnmntm60FramesPerSecond);

		rdbtnmntmUnlimitedFrameRate = new JRadioButtonMenuItem("Unlimited");
		rdbtnmntmUnlimitedFrameRate.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				// Unlock the frame rate of the view - as fast as your computer
				// can churn them out.
				SimulationView.setUnlimitedUpdateRate();
			}
		});

		frameRateButtonGroup.add(rdbtnmntmUnlimitedFrameRate);
		mnFrameRate.add(rdbtnmntmUnlimitedFrameRate);

		mnVerticalSync = new JMenu("Vertical Sync");
		mnViewOptions.add(mnVerticalSync);

		rdbtnmntmVsyncOn = new JRadioButtonMenuItem("VSync On");
		vSyncButtonGroup.add(rdbtnmntmVsyncOn);
		rdbtnmntmVsyncOn.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				// Sync frames to the monitor refresh
				SimulationView.setVerticalSync(true);
			}
		});
		mnVerticalSync.add(rdbtnmntmVsyncOn);

		rdbtnmntmVsyncOff = new JRadioButtonMenuItem("VSync Off");
		rdbtnmntmVsyncOff.setSelected(true);
		vSyncButtonGroup.add(rdbtnmntmVsyncOff);
		rdbtnmntmVsyncOff.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				// Dont Sync frames to the monitor refresh
				SimulationView.setVerticalSync(false);
			}
		});
		mnVerticalSync.add(rdbtnmntmVsyncOff);

		mnOverlay = new JMenu("Overlay");
		mnViewOptions.add(mnOverlay);

		rdbtnmntmOverlayEnabled = new JRadioButtonMenuItem("Enabled");
		rdbtnmntmOverlayEnabled.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				// Enable some view debug text which may be of interest
				SimulationView.setViewOverLay(true);
			}
		});
		overlayButtonGroup.add(rdbtnmntmOverlayEnabled);
		mnOverlay.add(rdbtnmntmOverlayEnabled);

		rdbtnmntmOverlayDisabled = new JRadioButtonMenuItem("Disabled");
		rdbtnmntmOverlayDisabled.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				// Disable the view debug text
				SimulationView.setViewOverLay(false);
			}
		});
		rdbtnmntmOverlayDisabled.setSelected(true);
		overlayButtonGroup.add(rdbtnmntmOverlayDisabled);
		mnOverlay.add(rdbtnmntmOverlayDisabled);

		gui.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				// Exit the sim
				doSimExit();
			}

			public void windowIconified(WindowEvent e)
			{
				// keep the view in the same window state as this frame
				SimulationView.minimise();
			}

			public void windowDeiconified(WindowEvent e)
			{
				// keep the view in the same window state as this frame
				SimulationView.maximise();
			}

		});

		gui.setVisible(true);
		// gui.setAlwaysOnTop(true);

		// We are now in the start up state
		startUpState();

	}

	private static void doFileOpen()
	{
		final JFileChooser filechooser = new JFileChooser(new File("./scenarios"));
		
		int val = filechooser.showOpenDialog(filechooser);
		
		if (val == JFileChooser.APPROVE_OPTION)
		{
			System.out.println("Get File");
			File file = filechooser.getSelectedFile();
			determinScenarios(file);	
			
			if(!sim.simPaused())
			{
				simPausedState();
			}
			/* Not already generating Sim */
			if (!generatingSim)
			{
				generatingSim = true;

				/* Create the new Simulation */
				newSim();

				generatingSim = false;

			}
			
		}
		
	}
	
	private static void determinScenarios(File file)
	{
		ScenarioVT scenario = new ScenarioVT(file);	
		
		System.out.println(scenario.getScenarioType());
		
		if(scenario.getScenarioType().equals("DEBUG"))
		{
			System.out.println("Debug File");
			simScenario = new DebugScenario(file);
		}
		else if(scenario.getScenarioType().equals("SAPP"))
		{			
			System.out.println("SAPP File");			
			simScenario = new SAPPScenario(file);			
		}
		else
		{
			System.out.println("UKNOWN");
		}
	}
	
	/* Ensure the user wants to exit then exit the program */
	private static void doSimExit()
	{

		String message;
		message = "Do you want to quit?";

		JOptionPane pane = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);

		// Center Dialog on the GUI
		JDialog dialog = pane.createDialog(gui, "Close Application");

		dialog.pack();
		dialog.setVisible(true);

		int value = ((Integer) pane.getValue()).intValue();

		if (value == JOptionPane.YES_OPTION)
		{
			SimulationView.exitDisplay(); // Tell OpenGL we are done and free
											// the resources used in the canvas.
											// - must be done else sim will
											// lockup.
			// Do EXIT
			System.exit(0);
		}

	}

	/* Ensure the user wants to exit then exit the program */
	private static void doSimUnlockNotify()
	{
		if (!simUnlockNotifcationShown)
		{
			String message;
			message = "The simulation parameters are now locked.\nThey can be unlocked via the Parameters menu.";

			JOptionPane pane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE);

			// Center Dialog on the GUI
			JDialog dialog = pane.createDialog(gui, "Parameters Locked");

			dialog.pack();
			dialog.setVisible(true);

			int value = ((Integer) pane.getValue()).intValue();

			/* Prevent popup from showing again */
			simUnlockNotifcationShown = true;
		}
	}

	/* Use the java provided system look and feel */
	private static void lookandFeel()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (ClassNotFoundException e1)
		{
			e1.printStackTrace();
		}
		catch (InstantiationException e1)
		{
			e1.printStackTrace();
		}
		catch (IllegalAccessException e1)
		{
			e1.printStackTrace();
		}
		catch (UnsupportedLookAndFeelException e1)
		{
			e1.printStackTrace();
		}
	}

	/* Sets up the timeout values for the tool-tips */
	private static void setUpToolTips()
	{
		ToolTipManager.sharedInstance().setReshowDelay(5000);
		ToolTipManager.sharedInstance().setDismissDelay(10000);
		ToolTipManager.sharedInstance().setInitialDelay(1000);
		ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
	}

	private static void simStartedState()
	{

		sim.startSim();

		btnGenerate.setEnabled(false);

		btnStart.setEnabled(false);

		btnPause.setEnabled(true);

		simRateSlider.setEnabled(true);

		// agentParamPanel.setVisible(false);
		// plantParamPanel.setVisible(false);

		StatsPanel.setPaused(false);

		// SimulationView.setFocus();

	}

	private static void startUpState()
	{
		btnStart.setEnabled(false);
		simRateSlider.setEnabled(false);
		btnPause.setEnabled(false);
	}

	private static void simPausedState()
	{
		btnPause.setText("Resume");
		btnGenerate.setEnabled(true);

		StatsPanel.setPaused(true);

		sim.pauseSim();

		btnPause.setIcon(new ImageIcon(SimulationGUI.class.getResource("/alifeSim/icons/resume.png")));

	}

	private static void simUnPausedState()
	{
		btnPause.setText("   Pause");
		btnGenerate.setEnabled(false);

		StatsPanel.setPaused(false);

		sim.unPauseSim();

		btnPause.setIcon(new ImageIcon(SimulationGUI.class.getResource("/alifeSim/icons/pause.png")));
	}

	/* Allow the view to change our window state to keep in sync with its state */
	public static void maximise()
	{
		gui.setState(Frame.NORMAL);
	}

	/* Allow the view to change our window state to keep in sync with its state */
	public static void minimise()
	{
		gui.setState(Frame.ICONIFIED);
	}

	/**
	 * The Average Steps per second.
	 * 
	 * @param asps
	 *            int
	 */
	public static void setASPS(int asps)
	{
		lblASPSNo.setText(Integer.toString(asps));
	}

	/**
	 * The curernt step number.
	 * 
	 * @param stepNo
	 */
	public static void setStepNo(long stepNo)
	{
		lblStepNo.setText(Long.toString(stepNo));
	}

	/**
	 * Called in the update sim loop - displays the current run time of the
	 * simulation.
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

	public static void clearGUIStats()
	{
		// Average Steps per second
		lblASPSNo.setText(Integer.toString(0));

		/* Set Number */
		lblStepNo.setText(Integer.toString(0));

		/* Reset the time */
		setTime(0);
	}

}
