package alife;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.border.Border;
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
import javax.swing.ImageIcon;
/**
 * This class manages user interaction in setting up and controlling the simulation.
 * The GUI was designed in WindowBuilder.
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

	/* The Combo boxes for setting parameters */
	private static JComboBox comboBoxPreyNumbers;
	private static int preyNumbersSelected = 6; // selects 800	

	private static JComboBox comboBoxPredNumbers;
	private static int predatorNumbersSelected = 3; // selects 100	

	private static JComboBox comboBoxWorldSize;
	private static int worldSizeSelected = 3; // selects 1024
	
	private static JComboBox comboBoxBarrierMode;
	private static int barrierModeSelected = 0; // selects off

	private static JComboBox comboBoxBarrierScenario;
	private static int barrierScenarioSelected = 0; // selects Single Barrier

	private static JComboBox comboBoxPlantNumbers;
	private static int plantStartingNumbersSelected = 5; // selects 400

	private static JComboBox comboBoxPlantRegenRate;
	private static int plantDefaultRegenrateSelected = 8; // Selects	8	

	private static JComboBox comboBoxEnergyAbsorptionRate;
	private static int plantDefaultEnergyAbsorptionRateSelected = 7; // Selects 8	

	private static JComboBox comboBoxPlantStartingEnergy;
	private static int plantStartingEnergySelected = 49; // selects 50

	//private static JComboBox comboBoxPlantRepoCost;
	//private static int plantDefaultPlantRepoCostSelected = 1; // not used DISABLED		

	private static JComboBox comboBoxPreySpeed;
	private static int preyDefaultSpeedSelected = 89; // Selects 0.90

	private static JComboBox comboBoxPredatorSpeed;
	private static int predatorDefaultSpeedSelected = 99; // Selects 1.00	

	private static JComboBox comboBoxPreyViewRange;
	private static int preyDefaultViewRangeSelected = 9; // Selects 10

	private static JComboBox comboBoxPredatorViewRange;
	private static int predatorDefaultViewRangeSelected = 9; // Selects 10

	private static JComboBox comboBoxPreyDE;
	private static int preyDefaultDESelected = 39; // 0.40

	private static JComboBox comboBoxPredatorDE;
	private static int predatorDefaultDESelected = 49; // 0.50	

	private static JComboBox comboBoxPreyREDiv;
	private static int preyDefaultREDivSelected = 1; // 0.50

	private static JComboBox comboBoxPredatorREDiv;
	private static int predatorDefaultREDivSelected = 1; // 0.50

	private static JComboBox comboBoxPreyMoveCost;
	private static int preyDefaultMovecostSelected = 24; // 0.025

	private static JComboBox comboBoxPredatorMoveCost;
	private static int predatorDefaultMovecostSelected = 24; // 0.025

	private static JComboBox comboBoxPreyHungerThres;
	private static int preyDefaultHungerThresSelected = 49; // 50

	private static JComboBox comboBoxPredatorHungerThres;
	private static int predatorDefaultHungerThresSelected = 49; // 50

	private static JComboBox comboBoxPreyConsumptionRate;
	private static int preyDefaultConsumptionRateSelected = 9; // Selects 9	

	private static JComboBox comboBoxPredatorConsumptionRate;
	private static int predatorDefaultConsumptionRateSelected = 9; // selects 100 (*Not* Enabled)	

	private static JComboBox comboBoxPreyRepoCost;
	private static int preyDefaultRepoCostSelected = 49;		// Selects 50		

	private static JComboBox comboBoxPredRepoCost;
	private static int predatorDefaultRepoCostSelected = 54;		// // Selects 55			

	private static JComboBox comboBoxPreyStartingEnergy;
	private static int preyDefaultStartingEnergySelected = 24;	 // Selects 25		

	private static JComboBox comboBoxPredStartingEnergy;
	private static int predatorDefaultStartingEnergySelected = 24;	// Selects 25			

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
	static int controlGuiX=0;
	static int controlGuiY=0;

	/* Auto sized simulation view */
	static int viewWidth = 0;
	static int viewHeight = 0;
	static int viewX;
	static int viewY;

	/* Simulation Reference */
	private static Simulation sim;

	// A reused parameter object to carry variables though the classes.
	private static SimpleAgentManagementSetupParam agentSettings = new SimpleAgentManagementSetupParam();

	public static JPanel agentParamPanel;

	public static JPanel plantParamPanel;

	// The GUI menu (AutoGenerated code) 
	private static JCheckBoxMenuItem chckbxmntmDrawFieldOf;
	private static JPanel simRateInfoPanel;
	private static JMenu mnParameters;
	private static JMenuItem mntmUnlock;
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
	private static JCheckBoxMenuItem chckbxmntmDrawTrueBodies;
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
	private static JLabel lblBarriers;	
	private static JLabel lblBScenario;

	/* The popup for first time users */
	private static boolean simUnlockNotifcationShown=false;

	/* Prevent over clicking the generate button */
	private static boolean generatingSim=false;
	
	/* Logic */

	/**
	
	 * @param args String[]
	 */
	public static void main(String args[])
	{		
		System.out.println("Artificial Life Simulation Started.");
		
		retrieveScreenSize();

		calculateWindowSizes();

		calculateWindowPositions();

		setUpFrame();

		setUpSimulation();

		// Display the simulation view
		SimulationView.displayView(sim, viewX, viewY, viewWidth, viewHeight);

		screenSizeCheck();
		
		setUpToolTips();
	}
	
	private static void newSim()
	{
		/*
		 * Main Setup
		 */

		/* World Size */
		int worldSize = Integer.parseInt(comboBoxWorldSize.getSelectedItem().toString());
		
		/* Barrier Mode */
		int barrierMode;
		
		if(comboBoxBarrierMode.getSelectedItem().toString().equalsIgnoreCase("Off"))
		{
			barrierMode = 0;
		}
		else if(comboBoxBarrierMode.getSelectedItem().toString().equalsIgnoreCase("Open"))
		{
			barrierMode = 1;
		}
		else // comboBoxBarrierMode.getSelectedItem().toString().equalsIgnoreCase("Closed")
		{
			barrierMode = 2;
		}
		
		/* Barrier Scenario */
		int barrierScenario = (Integer.parseInt(comboBoxBarrierScenario.getSelectedItem().toString())-1);

		/* Prey Numbers */
		int preyNo = Integer.parseInt(comboBoxPreyNumbers.getSelectedItem().toString());

		/* Pred Numbers */
		int predNo = Integer.parseInt(comboBoxPredNumbers.getSelectedItem().toString());

		/* Plant Numbers */
		int plantNo = Integer.parseInt(comboBoxPlantNumbers.getSelectedItem().toString());

		/*
		 * Plants via direct variable passing
		 */

		/* Plant Regeneration Rate */
		int plantRegenRate = Integer.parseInt(comboBoxPlantRegenRate.getSelectedItem().toString());

		/* Plant Energy Absorption Rate */
		int plantEnergyAbsorptionRate = Integer.parseInt(comboBoxEnergyAbsorptionRate.getSelectedItem().toString());

		/* Plant Starting Energy */
		int plantStartingEnergy = Integer.parseInt(comboBoxPlantStartingEnergy.getSelectedItem().toString());

		/*
		 * Agents setup via agentSettings object
		 */

		/* Speeds */
		agentSettings.setPreySpeed(Float.parseFloat(comboBoxPreySpeed.getSelectedItem().toString()));
		agentSettings.setPredatorSpeed(Float.parseFloat(comboBoxPredatorSpeed.getSelectedItem().toString()));

		/* View Ranges */
		agentSettings.setPreyViewRange(Integer.parseInt(comboBoxPreyViewRange.getSelectedItem().toString()));
		agentSettings.setPredatorViewRange(Integer.parseInt(comboBoxPredatorViewRange.getSelectedItem().toString()));

		/*
		 * Digestive Efficiency - how much energy consumed is converted to
		 * usable...
		 */
		agentSettings.setPreyDE(Float.parseFloat(comboBoxPreyDE.getSelectedItem().toString()));
		agentSettings.setPredatorDE(Float.parseFloat(comboBoxPredatorDE.getSelectedItem().toString()));

		/* Reproduction Energy Divider */
		agentSettings.setPreyREDiv(Float.parseFloat(comboBoxPreyREDiv.getSelectedItem().toString()));
		agentSettings.setPredatorREDiv(Float.parseFloat(comboBoxPredatorREDiv.getSelectedItem().toString()));

		/* Energy Movement Cost */
		agentSettings.setPreyMoveCost(Float.parseFloat(comboBoxPreyMoveCost.getSelectedItem().toString()));
		agentSettings.setPredatorMoveCost(Float.parseFloat(comboBoxPredatorMoveCost.getSelectedItem().toString()));

		/* Hunger Threshold */
		agentSettings.setPreyHungerThres(Float.parseFloat(comboBoxPreyHungerThres.getSelectedItem().toString()));
		agentSettings.setPredatorHungerThres(Float.parseFloat(comboBoxPredatorHungerThres.getSelectedItem().toString()));

		/* Energy Consumption Rate */
		agentSettings.setPreyConsumptionRate(Float.parseFloat(comboBoxPreyConsumptionRate.getSelectedItem().toString()));
		agentSettings.setPredatorConsumptionRate(100); // Not Used 100%

		/* Reproduction Cost */
		agentSettings.setPreyRepoCost(Float.parseFloat(comboBoxPreyRepoCost.getSelectedItem().toString()));
		agentSettings.setPredRepoCost(Float.parseFloat(comboBoxPredRepoCost.getSelectedItem().toString()));

		/* Starting Energy */
		agentSettings.setPreyStartingEnergy(Integer.parseInt(comboBoxPreyStartingEnergy.getSelectedItem().toString()));
		agentSettings.setPredStartingEnergy(Integer.parseInt(comboBoxPredStartingEnergy.getSelectedItem().toString()));

		System.out.println("New Simulation");
		
		/* Cleans up the old simulation threads */
		sim.destroySim();
		
		sim.newSim(statsPanel, worldSize,barrierMode,barrierScenario, preyNo, predNo, plantNo, plantRegenRate, plantStartingEnergy, plantEnergyAbsorptionRate, agentSettings);

		/*
		 * If needed the GC can free old objects now, before the simulation
		 * starts
		 */
		System.gc();

		btnGenerate.setEnabled(true);

		btnStart.setEnabled(true);

		btnPause.setEnabled(false);

		btnPause.setText("   Pause");

		btnPause.setIcon(new ImageIcon(SimulationGUI.class.getResource("/alife/icons/pause.png")));
		
		simRateSlider.setEnabled(false);

		simRateSlider.setValue(15);

		// Centers the simulated world in the view
		SimulationView.setInitalViewTranslate((viewWidth / 2) - ((Integer.parseInt(comboBoxWorldSize.getSelectedItem().toString())) / 2), (viewHeight / 2) - ((Integer.parseInt(comboBoxWorldSize.getSelectedItem().toString())) / 2));

		/* Clear the old stats */
		StatsPanel.clearStats();
		
		clearGUIStats();

	}

	private static void setUpSimulation()
	{
		sim = new Simulation();
	}

	private static void calculateWindowSizes()
	{
		String hostPlatform = System.getProperty("os.name");
		
		if(hostPlatform.contains("Windows"))
		{
			viewWidth = screenWidth - controlGuiWidth - (windowPad * 2);

			controlGuiHeight = controlGuiHeightMin;

			if (controlGuiHeight < screenHeight )
			{
				controlGuiHeight = screenHeight - titlePad;
				
				viewHeight = controlGuiHeight;
			}			

		}
		else if(hostPlatform.contains("Linux"))
		{
			viewWidth = screenWidth - controlGuiWidth - (windowPad * 2);

			controlGuiHeight = controlGuiHeightMin;

			if (controlGuiHeight < screenHeight )
			{
				controlGuiHeight = screenHeight - titlePad;
				
				viewHeight = controlGuiHeight;
			}			
			
		}
		else // remove the title pad size on mac and linux
		{
			viewWidth = screenWidth - controlGuiWidth - (windowPad * 2);
			viewHeight = screenHeight - (titlePad); // Task manager and Top borders on some os's

			controlGuiHeight = controlGuiHeightMin;

			if (controlGuiHeight < screenHeight - (titlePad * 2))
			{
				controlGuiHeight = screenHeight - (titlePad * 2);
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
		else if(screenHeight >= 1024) // 1280*1024 minimum to be treated as a large screen 
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
		String os= System.getProperty("os.name");

		if(os.contains("Windows"))
		{
			/* OS look for Window */
			lookandFeel();
			
			controlGuiX = (screenWidth / 2) - ((controlGuiWidth + viewWidth) / 2)-(windowPad/2);
			controlGuiY = 1;

			viewX = controlGuiX + (controlGuiWidth) + windowPad;
			viewY = controlGuiY;		
		}
		else // mac / linux
		{
			controlGuiX = (screenWidth / 2) - ((controlGuiWidth + viewWidth) / 2)-(windowPad/2);
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

	/** This method contains sections that are largely auto generated from the editor **/
	private static void setUpFrame()
	{
		gui = new JFrame();
		gui.setResizable(false);
		gui.setTitle("Alife Simulation");
		gui.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // We control the exit

		// GUI Size
		gui.setBounds(controlGuiX, controlGuiY, controlGuiWidth, controlGuiHeight);

		gui.getContentPane().setLayout(new BorderLayout(0, 0));

		JPanel controlPanel = new JPanel();
		controlPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		gui.getContentPane().add(controlPanel, BorderLayout.CENTER);

		controlPanel.setLayout(new BorderLayout(0, 0));

		JPanel controlPanelTop = new JPanel();

		controlPanel.add(controlPanelTop, BorderLayout.NORTH);
		GridBagLayout gblControlPanelTop = new GridBagLayout();
		gblControlPanelTop.columnWidths = new int[]
		{0};
		gblControlPanelTop.rowHeights = new int[]
		{0, 0, 0};
		gblControlPanelTop.columnWeights = new double[]
		{1.0, 1.0};
		gblControlPanelTop.rowWeights = new double[]
		{Double.MIN_VALUE};
		controlPanelTop.setLayout(gblControlPanelTop);

		JPanel mainSetupPanel = new JPanel();
		mainSetupPanel.setBorder(new TitledBorder(null, "Simulation Setup", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbcMainSetupPanel = new GridBagConstraints();
		gbcMainSetupPanel.anchor = GridBagConstraints.NORTH;
		gbcMainSetupPanel.fill = GridBagConstraints.HORIZONTAL;
		gbcMainSetupPanel.insets = new Insets(0, 0, 5, 0);
		gbcMainSetupPanel.gridwidth = 2;
		gbcMainSetupPanel.gridx = 0;
		gbcMainSetupPanel.gridy = 0;
		controlPanelTop.add(mainSetupPanel, gbcMainSetupPanel);
		mainSetupPanel.setLayout(new GridLayout(0, 6, 4, 3));

		JLabel lblPredS = new JLabel("Predators");
		lblPredS.setHorizontalAlignment(SwingConstants.LEFT);
		mainSetupPanel.add(lblPredS);

		comboBoxPredNumbers = new JComboBox();
		comboBoxPredNumbers.setToolTipText("Set the inital Predator numbers.");
		mainSetupPanel.add(comboBoxPredNumbers);
		comboBoxPredNumbers.setModel(new DefaultComboBoxModel(new String[] {"0", "1", "10", "100", "200", "400", "800", "1600", "3200", "6400", "12800", "25600", "51200"}));
		comboBoxPredNumbers.setSelectedIndex(predatorNumbersSelected);

		JLabel lblPreyS = new JLabel("Prey");
		lblPreyS.setHorizontalAlignment(SwingConstants.LEFT);
		mainSetupPanel.add(lblPreyS);

		comboBoxPreyNumbers = new JComboBox();
		comboBoxPreyNumbers.setToolTipText("Set the inital Prey numbers.");
		mainSetupPanel.add(comboBoxPreyNumbers);
		comboBoxPreyNumbers.setModel(new DefaultComboBoxModel(new String[] {"0", "1", "10", "100", "200", "400", "800", "1600", "3200", "6400", "12800", "25600", "51200"}));
		comboBoxPreyNumbers.setSelectedIndex(preyNumbersSelected);

		JLabel lblPlants = new JLabel("Plants");
		lblPlants.setHorizontalAlignment(SwingConstants.LEFT);
		mainSetupPanel.add(lblPlants);

		comboBoxPlantNumbers = new JComboBox();
		comboBoxPlantNumbers.setToolTipText("Set the inital Plant numbers.");
		mainSetupPanel.add(comboBoxPlantNumbers);
		comboBoxPlantNumbers.setModel(new DefaultComboBoxModel(new String[] {"0", "1", "10", "100", "200", "400", "800", "1600", "3200", "6400", "12800", "25600", "51200"}));
		comboBoxPlantNumbers.setSelectedIndex(plantStartingNumbersSelected);

		JLabel lblWorldSize = new JLabel("World Size");
		lblWorldSize.setHorizontalAlignment(SwingConstants.LEFT);
		mainSetupPanel.add(lblWorldSize);

		comboBoxWorldSize = new JComboBox();
		comboBoxWorldSize.setToolTipText("Set the simulated world size.");
		mainSetupPanel.add(comboBoxWorldSize);
		comboBoxWorldSize.setModel(new DefaultComboBoxModel(new String[] {"128", "256", "512", "1024", "2048", "4096", "8192"}));
		comboBoxWorldSize.setSelectedIndex(worldSizeSelected);
		
		lblBarriers = new JLabel("Barriers");
		mainSetupPanel.add(lblBarriers);
		
		comboBoxBarrierMode = new JComboBox();
		comboBoxBarrierMode.setToolTipText("<html>\r\nSelects World Barrier Mode\r\n<br>\r\nOff - No barriers\r\n<br>\r\nOpen - Barriers with open sides\r\n<br>\r\nClosed - Barriers with a single side joining with the world edge.\r\n</html>");
		comboBoxBarrierMode.setModel(new DefaultComboBoxModel(new String[] {"Off", "Open", "Closed"}));
		comboBoxBarrierMode.setSelectedIndex(barrierModeSelected);
		mainSetupPanel.add(comboBoxBarrierMode);
		
		lblBScenario = new JLabel("B Scenario");
		mainSetupPanel.add(lblBScenario);
		
		comboBoxBarrierScenario = new JComboBox();
		comboBoxBarrierScenario.setToolTipText("<html>\r\nSelects Barrier Scenario\r\n<br>\r\n1\t- A single barrier in the midle of the world.\r\n<br>\r\n2 \t- Two barriers dispersed equally.\r\n<br>\r\n3 \t- Three barriers dispersed equally.\r\n<html>");
		comboBoxBarrierScenario.setModel(new DefaultComboBoxModel(new String[] {"1", "2", "3"}));
		comboBoxBarrierScenario.setSelectedIndex(barrierScenarioSelected);
		mainSetupPanel.add(comboBoxBarrierScenario);

		agentParamPanel = new JPanel();
		agentParamPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Agent Parameters", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbcAgentParamPanel = new GridBagConstraints();
		gbcAgentParamPanel.anchor = GridBagConstraints.NORTH;
		gbcAgentParamPanel.gridwidth = 2;
		gbcAgentParamPanel.fill = GridBagConstraints.HORIZONTAL;
		gbcAgentParamPanel.insets = new Insets(0, 0, 5, 0);
		gbcAgentParamPanel.gridx = 0;
		gbcAgentParamPanel.gridy = 1;
		controlPanelTop.add(agentParamPanel, gbcAgentParamPanel);
		agentParamPanel.setLayout(new GridLayout(0, 3, 4, 3));

		JLabel lblSpacer = new JLabel("");
		agentParamPanel.add(lblSpacer);

		JLabel lblPrey = new JLabel("Prey");
		lblPrey.setHorizontalAlignment(SwingConstants.CENTER);
		agentParamPanel.add(lblPrey);

		JLabel lblPredator = new JLabel("Predator");
		lblPredator.setHorizontalAlignment(SwingConstants.CENTER);
		agentParamPanel.add(lblPredator);

		JLabel lblSpeed = new JLabel("Speed");
		lblSpeed.setHorizontalAlignment(SwingConstants.LEFT);
		agentParamPanel.add(lblSpeed);

		comboBoxPreySpeed = new JComboBox();
		comboBoxPreySpeed.setToolTipText("Speed of Prey.");
		comboBoxPreySpeed.setModel(new DefaultComboBoxModel(new String[]
		{" 0.01 ", " 0.02", " 0.03", " 0.04", " 0.05", " 0.06", " 0.07", " 0.08", " 0.09", " 0.10", " 0.11", " 0.12", " 0.13", " 0.14", " 0.15", " 0.16", " 0.17", " 0.18", " 0.19", " 0.20", " 0.21", " 0.22", " 0.23", " 0.24", " 0.25", " 0.26", " 0.27", " 0.28", " 0.29", " 0.30", " 0.31", " 0.32", " 0.33", " 0.34", " 0.35", " 0.36", " 0.37", " 0.38", " 0.39", " 0.40", " 0.41", " 0.42", " 0.43", " 0.44", " 0.45", " 0.46", " 0.47", " 0.48", " 0.49", " 0.50", " 0.51", " 0.52", " 0.53", " 0.54", " 0.55", " 0.56", " 0.57", " 0.58", " 0.59", " 0.60", " 0.61", " 0.62", " 0.63", " 0.64", " 0.65",
				" 0.66", " 0.67", " 0.68", " 0.69", " 0.70", " 0.71", " 0.72", " 0.73", " 0.74", " 0.75", " 0.76", " 0.77", " 0.78", " 0.79", " 0.80", " 0.81", " 0.82", " 0.83", " 0.84", " 0.85", " 0.86", " 0.87", " 0.88", " 0.89", " 0.90", " 0.91", " 0.92", " 0.93", " 0.94", " 0.95", " 0.96", " 0.97", " 0.98", " 0.99", " 1.00"}));
		agentParamPanel.add(comboBoxPreySpeed);
		comboBoxPreySpeed.setSelectedIndex(preyDefaultSpeedSelected);

		comboBoxPredatorSpeed = new JComboBox();
		comboBoxPredatorSpeed.setToolTipText("Speed of Predators.");
		comboBoxPredatorSpeed.setModel(new DefaultComboBoxModel(new String[]
		{" 0.01 ", " 0.02", " 0.03", " 0.04", " 0.05", " 0.06", " 0.07", " 0.08", " 0.09", " 0.10", " 0.11", " 0.12", " 0.13", " 0.14", " 0.15", " 0.16", " 0.17", " 0.18", " 0.19", " 0.20", " 0.21", " 0.22", " 0.23", " 0.24", " 0.25", " 0.26", " 0.27", " 0.28", " 0.29", " 0.30", " 0.31", " 0.32", " 0.33", " 0.34", " 0.35", " 0.36", " 0.37", " 0.38", " 0.39", " 0.40", " 0.41", " 0.42", " 0.43", " 0.44", " 0.45", " 0.46", " 0.47", " 0.48", " 0.49", " 0.50", " 0.51", " 0.52", " 0.53", " 0.54", " 0.55", " 0.56", " 0.57", " 0.58", " 0.59", " 0.60", " 0.61", " 0.62", " 0.63", " 0.64", " 0.65",
				" 0.66", " 0.67", " 0.68", " 0.69", " 0.70", " 0.71", " 0.72", " 0.73", " 0.74", " 0.75", " 0.76", " 0.77", " 0.78", " 0.79", " 0.80", " 0.81", " 0.82", " 0.83", " 0.84", " 0.85", " 0.86", " 0.87", " 0.88", " 0.89", " 0.90", " 0.91", " 0.92", " 0.93", " 0.94", " 0.95", " 0.96", " 0.97", " 0.98", " 0.99", " 1.00"}));
		agentParamPanel.add(comboBoxPredatorSpeed);
		comboBoxPredatorSpeed.setSelectedIndex(predatorDefaultSpeedSelected);

		JLabel lblViewRange = new JLabel("View Range");
		lblViewRange.setHorizontalAlignment(SwingConstants.LEFT);
		agentParamPanel.add(lblViewRange);

		comboBoxPreyViewRange = new JComboBox();
		comboBoxPreyViewRange.setToolTipText("View range of Prey. ( Distance of R in front of the body of the agent, where R is a Radius value)");
		comboBoxPreyViewRange.setModel(new DefaultComboBoxModel(
				new String[]
				{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "90", "91", "92", "93", "94", "95", "96", "97", "98",
						"99", "100"}));
		agentParamPanel.add(comboBoxPreyViewRange);
		comboBoxPreyViewRange.setSelectedIndex(preyDefaultViewRangeSelected);

		comboBoxPredatorViewRange = new JComboBox();
		comboBoxPredatorViewRange.setToolTipText("View range of Predators. ( Distance of R in front of the body of the agent, where R is a Radius value)");
		comboBoxPredatorViewRange.setModel(new DefaultComboBoxModel(
				new String[]
				{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "90", "91", "92", "93", "94", "95", "96", "97", "98",
						"99", "100"}));
		agentParamPanel.add(comboBoxPredatorViewRange);
		comboBoxPredatorViewRange.setSelectedIndex(predatorDefaultViewRangeSelected);

		JLabel lblDigestiveEfficn = new JLabel("Digestive Efficiency");
		lblDigestiveEfficn.setHorizontalAlignment(SwingConstants.LEFT);
		agentParamPanel.add(lblDigestiveEfficn);

		comboBoxPreyDE = new JComboBox();
		comboBoxPreyDE.setToolTipText("How efficiently Prey gain energy from consumption.");
		comboBoxPreyDE.setModel(new DefaultComboBoxModel(new String[]
		{" 0.01 ", " 0.02", " 0.03", " 0.04", " 0.05", " 0.06", " 0.07", " 0.08", " 0.09", " 0.10", " 0.11", " 0.12", " 0.13", " 0.14", " 0.15", " 0.16", " 0.17", " 0.18", " 0.19", " 0.20", " 0.21", " 0.22", " 0.23", " 0.24", " 0.25", " 0.26", " 0.27", " 0.28", " 0.29", " 0.30", " 0.31", " 0.32", " 0.33", " 0.34", " 0.35", " 0.36", " 0.37", " 0.38", " 0.39", " 0.40", " 0.41", " 0.42", " 0.43", " 0.44", " 0.45", " 0.46", " 0.47", " 0.48", " 0.49", " 0.50", " 0.51", " 0.52", " 0.53", " 0.54", " 0.55", " 0.56", " 0.57", " 0.58", " 0.59", " 0.60", " 0.61", " 0.62", " 0.63", " 0.64", " 0.65",
				" 0.66", " 0.67", " 0.68", " 0.69", " 0.70", " 0.71", " 0.72", " 0.73", " 0.74", " 0.75", " 0.76", " 0.77", " 0.78", " 0.79", " 0.80", " 0.81", " 0.82", " 0.83", " 0.84", " 0.85", " 0.86", " 0.87", " 0.88", " 0.89", " 0.90", " 0.91", " 0.92", " 0.93", " 0.94", " 0.95", " 0.96", " 0.97", " 0.98", " 0.99", " 1.00"}));
		agentParamPanel.add(comboBoxPreyDE);
		comboBoxPreyDE.setSelectedIndex(preyDefaultDESelected);

		comboBoxPredatorDE = new JComboBox();
		comboBoxPredatorDE.setToolTipText("How efficiently Predators gain energy from consumption.");
		comboBoxPredatorDE.setModel(new DefaultComboBoxModel(new String[]
		{" 0.01 ", " 0.02", " 0.03", " 0.04", " 0.05", " 0.06", " 0.07", " 0.08", " 0.09", " 0.10", " 0.11", " 0.12", " 0.13", " 0.14", " 0.15", " 0.16", " 0.17", " 0.18", " 0.19", " 0.20", " 0.21", " 0.22", " 0.23", " 0.24", " 0.25", " 0.26", " 0.27", " 0.28", " 0.29", " 0.30", " 0.31", " 0.32", " 0.33", " 0.34", " 0.35", " 0.36", " 0.37", " 0.38", " 0.39", " 0.40", " 0.41", " 0.42", " 0.43", " 0.44", " 0.45", " 0.46", " 0.47", " 0.48", " 0.49", " 0.50", " 0.51", " 0.52", " 0.53", " 0.54", " 0.55", " 0.56", " 0.57", " 0.58", " 0.59", " 0.60", " 0.61", " 0.62", " 0.63", " 0.64", " 0.65",
				" 0.66", " 0.67", " 0.68", " 0.69", " 0.70", " 0.71", " 0.72", " 0.73", " 0.74", " 0.75", " 0.76", " 0.77", " 0.78", " 0.79", " 0.80", " 0.81", " 0.82", " 0.83", " 0.84", " 0.85", " 0.86", " 0.87", " 0.88", " 0.89", " 0.90", " 0.91", " 0.92", " 0.93", " 0.94", " 0.95", " 0.96", " 0.97", " 0.98", " 0.99", " 1.00"}));
		agentParamPanel.add(comboBoxPredatorDE);
		comboBoxPredatorDE.setSelectedIndex(predatorDefaultDESelected);

		JLabel lblReproductionDiv = new JLabel("R/S Energy Div");
		lblReproductionDiv.setHorizontalAlignment(SwingConstants.LEFT);
		agentParamPanel.add(lblReproductionDiv);

		comboBoxPreyREDiv = new JComboBox();
		comboBoxPreyREDiv.setToolTipText("Sets the division between how much energy is used for survival and how much is used for reproduction of a new agent.");
		comboBoxPreyREDiv.setModel(new DefaultComboBoxModel(new String[]
		{"0.25", "0.50", "0.75"}));
		agentParamPanel.add(comboBoxPreyREDiv);
		comboBoxPreyREDiv.setSelectedIndex(preyDefaultREDivSelected);

		comboBoxPredatorREDiv = new JComboBox();
		comboBoxPredatorREDiv.setToolTipText("Sets the division between how much energy is used for survival and how much is used for reproduction of a new agent.");
		comboBoxPredatorREDiv.setModel(new DefaultComboBoxModel(new String[]
		{"0.25", "0.50", "0.75"}));
		agentParamPanel.add(comboBoxPredatorREDiv);
		comboBoxPredatorREDiv.setSelectedIndex(predatorDefaultREDivSelected);

		JLabel lblMovementCost = new JLabel("Movement Cost");
		lblMovementCost.setHorizontalAlignment(SwingConstants.LEFT);
		agentParamPanel.add(lblMovementCost);

		comboBoxPreyMoveCost = new JComboBox();
		comboBoxPreyMoveCost.setToolTipText("The cost in energy for Prey to move.");
		comboBoxPreyMoveCost.setModel(new DefaultComboBoxModel(new String[]
		{"0.001", "0.002", "0.003", "0.004", "0.005", "0.006", "0.007", "0.008", "0.009", "0.010", "0.011", "0.012", "0.013", "0.014", "0.015", "0.016", "0.017", "0.018", "0.019", "0.020", "0.021", "0.022", "0.023", "0.024", "0.025", "0.026", "0.027", "0.028", "0.029", "0.030", "0.031", "0.032", "0.033", "0.034", "0.035", "0.036", "0.037", "0.038", "0.039", "0.040", "0.041", "0.042", "0.043", "0.044", "0.045", "0.046", "0.047", "0.048", "0.049", "0.050", "0.051", "0.052", "0.053", "0.054", "0.055", "0.056", "0.057", "0.058", "0.059", "0.060", "0.061", "0.062", "0.063", "0.064", "0.065",
				"0.066", "0.067", "0.068", "0.069", "0.070", "0.071", "0.072", "0.073", "0.074", "0.075", "0.076", "0.077", "0.078", "0.079", "0.080", "0.081", "0.082", "0.083", "0.084", "0.085", "0.086", "0.087", "0.088", "0.089", "0.090", "0.091", "0.092", "0.093", "0.094", "0.095", "0.096", "0.097", "0.098", "0.099", "0.0100", "0.0101", "0.0102", "0.0103", "0.0104", "0.0105", "0.0106", "0.0107", "0.0108", "0.0109", "0.0110", "0.0111", "0.0112", "0.0113", "0.0114", "0.0115", "0.0116", "0.0117", "0.0118", "0.0119", "0.0120", "0.0121", "0.0122", "0.0123", "0.0124", "0.0125", "0.0126",
				"0.0127", "0.0128", "0.0129", "0.0130", "0.0131", "0.0132", "0.0133", "0.0134", "0.0135", "0.0136", "0.0137", "0.0138", "0.0139", "0.0140", "0.0141", "0.0142", "0.0143", "0.0144", "0.0145", "0.0146", "0.0147", "0.0148", "0.0149", "0.0150", "0.0151", "0.0152", "0.0153", "0.0154", "0.0155", "0.0156", "0.0157", "0.0158", "0.0159", "0.0160", "0.0161", "0.0162", "0.0163", "0.0164", "0.0165", "0.0166", "0.0167", "0.0168", "0.0169", "0.0170", "0.0171", "0.0172", "0.0173", "0.0174", "0.0175", "0.0176", "0.0177", "0.0178", "0.0179", "0.0180", "0.0181", "0.0182", "0.0183", "0.0184",
				"0.0185", "0.0186", "0.0187", "0.0188", "0.0189", "0.0190", "0.0191", "0.0192", "0.0193", "0.0194", "0.0195", "0.0196", "0.0197", "0.0198", "0.0199", "0.0200", "0.0201", "0.0202", "0.0203", "0.0204", "0.0205", "0.0206", "0.0207", "0.0208", "0.0209", "0.0210", "0.0211", "0.0212", "0.0213", "0.0214", "0.0215", "0.0216", "0.0217", "0.0218", "0.0219", "0.0220", "0.0221", "0.0222", "0.0223", "0.0224", "0.0225", "0.0226", "0.0227", "0.0228", "0.0229", "0.0230", "0.0231", "0.0232", "0.0233", "0.0234", "0.0235", "0.0236", "0.0237", "0.0238", "0.0239", "0.0240", "0.0241", "0.0242",
				"0.0243", "0.0244", "0.0245", "0.0246", "0.0247", "0.0248", "0.0249", "0.0250", "0.0251", "0.0252", "0.0253", "0.0254", "0.0255", "0.0256", "0.0257", "0.0258", "0.0259", "0.0260", "0.0261", "0.0262", "0.0263", "0.0264", "0.0265", "0.0266", "0.0267", "0.0268", "0.0269", "0.0270", "0.0271", "0.0272", "0.0273", "0.0274", "0.0275", "0.0276", "0.0277", "0.0278", "0.0279", "0.0280", "0.0281", "0.0282", "0.0283", "0.0284", "0.0285", "0.0286", "0.0287", "0.0288", "0.0289", "0.0290", "0.0291", "0.0292", "0.0293", "0.0294", "0.0295", "0.0296", "0.0297", "0.0298", "0.0299", "0.0300",
				"0.0301", "0.0302", "0.0303", "0.0304", "0.0305", "0.0306", "0.0307", "0.0308", "0.0309", "0.0310", "0.0311", "0.0312", "0.0313", "0.0314", "0.0315", "0.0316", "0.0317", "0.0318", "0.0319", "0.0320", "0.0321", "0.0322", "0.0323", "0.0324", "0.0325", "0.0326", "0.0327", "0.0328", "0.0329", "0.0330", "0.0331", "0.0332", "0.0333", "0.0334", "0.0335", "0.0336", "0.0337", "0.0338", "0.0339", "0.0340", "0.0341", "0.0342", "0.0343", "0.0344", "0.0345", "0.0346", "0.0347", "0.0348", "0.0349", "0.0350", "0.0351", "0.0352", "0.0353", "0.0354", "0.0355", "0.0356", "0.0357", "0.0358",
				"0.0359", "0.0360", "0.0361", "0.0362", "0.0363", "0.0364", "0.0365", "0.0366", "0.0367", "0.0368", "0.0369", "0.0370", "0.0371", "0.0372", "0.0373", "0.0374", "0.0375", "0.0376", "0.0377", "0.0378", "0.0379", "0.0380", "0.0381", "0.0382", "0.0383", "0.0384", "0.0385", "0.0386", "0.0387", "0.0388", "0.0389", "0.0390", "0.0391", "0.0392", "0.0393", "0.0394", "0.0395", "0.0396", "0.0397", "0.0398", "0.0399", "0.0400", "0.0401", "0.0402", "0.0403", "0.0404", "0.0405", "0.0406", "0.0407", "0.0408", "0.0409", "0.0410", "0.0411", "0.0412", "0.0413", "0.0414", "0.0415", "0.0416",
				"0.0417", "0.0418", "0.0419", "0.0420", "0.0421", "0.0422", "0.0423", "0.0424", "0.0425", "0.0426", "0.0427", "0.0428", "0.0429", "0.0430", "0.0431", "0.0432", "0.0433", "0.0434", "0.0435", "0.0436", "0.0437", "0.0438", "0.0439", "0.0440", "0.0441", "0.0442", "0.0443", "0.0444", "0.0445", "0.0446", "0.0447", "0.0448", "0.0449", "0.0450", "0.0451", "0.0452", "0.0453", "0.0454", "0.0455", "0.0456", "0.0457", "0.0458", "0.0459", "0.0460", "0.0461", "0.0462", "0.0463", "0.0464", "0.0465", "0.0466", "0.0467", "0.0468", "0.0469", "0.0470", "0.0471", "0.0472", "0.0473", "0.0474",
				"0.0475", "0.0476", "0.0477", "0.0478", "0.0479", "0.0480", "0.0481", "0.0482", "0.0483", "0.0484", "0.0485", "0.0486", "0.0487", "0.0488", "0.0489", "0.0490", "0.0491", "0.0492", "0.0493", "0.0494", "0.0495", "0.0496", "0.0497", "0.0498", "0.0499", "0.0500", "0.0501", "0.0502", "0.0503", "0.0504", "0.0505", "0.0506", "0.0507", "0.0508", "0.0509", "0.0510", "0.0511", "0.0512", "0.0513", "0.0514", "0.0515", "0.0516", "0.0517", "0.0518", "0.0519", "0.0520", "0.0521", "0.0522", "0.0523", "0.0524", "0.0525", "0.0526", "0.0527", "0.0528", "0.0529", "0.0530", "0.0531", "0.0532",
				"0.0533", "0.0534", "0.0535", "0.0536", "0.0537", "0.0538", "0.0539", "0.0540", "0.0541", "0.0542", "0.0543", "0.0544", "0.0545", "0.0546", "0.0547", "0.0548", "0.0549", "0.0550", "0.0551", "0.0552", "0.0553", "0.0554", "0.0555", "0.0556", "0.0557", "0.0558", "0.0559", "0.0560", "0.0561", "0.0562", "0.0563", "0.0564", "0.0565", "0.0566", "0.0567", "0.0568", "0.0569", "0.0570", "0.0571", "0.0572", "0.0573", "0.0574", "0.0575", "0.0576", "0.0577", "0.0578", "0.0579", "0.0580", "0.0581", "0.0582", "0.0583", "0.0584", "0.0585", "0.0586", "0.0587", "0.0588", "0.0589", "0.0590",
				"0.0591", "0.0592", "0.0593", "0.0594", "0.0595", "0.0596", "0.0597", "0.0598", "0.0599", "0.0600", "0.0601", "0.0602", "0.0603", "0.0604", "0.0605", "0.0606", "0.0607", "0.0608", "0.0609", "0.0610", "0.0611", "0.0612", "0.0613", "0.0614", "0.0615", "0.0616", "0.0617", "0.0618", "0.0619", "0.0620", "0.0621", "0.0622", "0.0623", "0.0624", "0.0625", "0.0626", "0.0627", "0.0628", "0.0629", "0.0630", "0.0631", "0.0632", "0.0633", "0.0634", "0.0635", "0.0636", "0.0637", "0.0638", "0.0639", "0.0640", "0.0641", "0.0642", "0.0643", "0.0644", "0.0645", "0.0646", "0.0647", "0.0648",
				"0.0649", "0.0650", "0.0651", "0.0652", "0.0653", "0.0654", "0.0655", "0.0656", "0.0657", "0.0658", "0.0659", "0.0660", "0.0661", "0.0662", "0.0663", "0.0664", "0.0665", "0.0666", "0.0667", "0.0668", "0.0669", "0.0670", "0.0671", "0.0672", "0.0673", "0.0674", "0.0675", "0.0676", "0.0677", "0.0678", "0.0679", "0.0680", "0.0681", "0.0682", "0.0683", "0.0684", "0.0685", "0.0686", "0.0687", "0.0688", "0.0689", "0.0690", "0.0691", "0.0692", "0.0693", "0.0694", "0.0695", "0.0696", "0.0697", "0.0698", "0.0699", "0.0700", "0.0701", "0.0702", "0.0703", "0.0704", "0.0705", "0.0706",
				"0.0707", "0.0708", "0.0709", "0.0710", "0.0711", "0.0712", "0.0713", "0.0714", "0.0715", "0.0716", "0.0717", "0.0718", "0.0719", "0.0720", "0.0721", "0.0722", "0.0723", "0.0724", "0.0725", "0.0726", "0.0727", "0.0728", "0.0729", "0.0730", "0.0731", "0.0732", "0.0733", "0.0734", "0.0735", "0.0736", "0.0737", "0.0738", "0.0739", "0.0740", "0.0741", "0.0742", "0.0743", "0.0744", "0.0745", "0.0746", "0.0747", "0.0748", "0.0749", "0.0750", "0.0751", "0.0752", "0.0753", "0.0754", "0.0755", "0.0756", "0.0757", "0.0758", "0.0759", "0.0760", "0.0761", "0.0762", "0.0763", "0.0764",
				"0.0765", "0.0766", "0.0767", "0.0768", "0.0769", "0.0770", "0.0771", "0.0772", "0.0773", "0.0774", "0.0775", "0.0776", "0.0777", "0.0778", "0.0779", "0.0780", "0.0781", "0.0782", "0.0783", "0.0784", "0.0785", "0.0786", "0.0787", "0.0788", "0.0789", "0.0790", "0.0791", "0.0792", "0.0793", "0.0794", "0.0795", "0.0796", "0.0797", "0.0798", "0.0799", "0.0800", "0.0801", "0.0802", "0.0803", "0.0804", "0.0805", "0.0806", "0.0807", "0.0808", "0.0809", "0.0810", "0.0811", "0.0812", "0.0813", "0.0814", "0.0815", "0.0816", "0.0817", "0.0818", "0.0819", "0.0820", "0.0821", "0.0822",
				"0.0823", "0.0824", "0.0825", "0.0826", "0.0827", "0.0828", "0.0829", "0.0830", "0.0831", "0.0832", "0.0833", "0.0834", "0.0835", "0.0836", "0.0837", "0.0838", "0.0839", "0.0840", "0.0841", "0.0842", "0.0843", "0.0844", "0.0845", "0.0846", "0.0847", "0.0848", "0.0849", "0.0850", "0.0851", "0.0852", "0.0853", "0.0854", "0.0855", "0.0856", "0.0857", "0.0858", "0.0859", "0.0860", "0.0861", "0.0862", "0.0863", "0.0864", "0.0865", "0.0866", "0.0867", "0.0868", "0.0869", "0.0870", "0.0871", "0.0872", "0.0873", "0.0874", "0.0875", "0.0876", "0.0877", "0.0878", "0.0879", "0.0880",
				"0.0881", "0.0882", "0.0883", "0.0884", "0.0885", "0.0886", "0.0887", "0.0888", "0.0889", "0.0890", "0.0891", "0.0892", "0.0893", "0.0894", "0.0895", "0.0896", "0.0897", "0.0898", "0.0899", "0.0900", "0.0901", "0.0902", "0.0903", "0.0904", "0.0905", "0.0906", "0.0907", "0.0908", "0.0909", "0.0910", "0.0911", "0.0912", "0.0913", "0.0914", "0.0915", "0.0916", "0.0917", "0.0918", "0.0919", "0.0920", "0.0921", "0.0922", "0.0923", "0.0924", "0.0925", "0.0926", "0.0927", "0.0928", "0.0929", "0.0930", "0.0931", "0.0932", "0.0933", "0.0934", "0.0935", "0.0936", "0.0937", "0.0938",
				"0.0939", "0.0940", "0.0941", "0.0942", "0.0943", "0.0944", "0.0945", "0.0946", "0.0947", "0.0948", "0.0949", "0.0950", "0.0951", "0.0952", "0.0953", "0.0954", "0.0955", "0.0956", "0.0957", "0.0958", "0.0959", "0.0960", "0.0961", "0.0962", "0.0963", "0.0964", "0.0965", "0.0966", "0.0967", "0.0968", "0.0969", "0.0970", "0.0971", "0.0972", "0.0973", "0.0974", "0.0975", "0.0976", "0.0977", "0.0978", "0.0979", "0.0980", "0.0981", "0.0982", "0.0983", "0.0984", "0.0985", "0.0986", "0.0987", "0.0988", "0.0989", "0.0990", "0.0991", "0.0992", "0.0993", "0.0994", "0.0995", "0.0996",
				"0.0997", "0.0998", "0.0999"}));
		agentParamPanel.add(comboBoxPreyMoveCost);
		comboBoxPreyMoveCost.setSelectedIndex(preyDefaultMovecostSelected);

		comboBoxPredatorMoveCost = new JComboBox();
		comboBoxPredatorMoveCost.setToolTipText("The cost in energy for Predators to move.");
		comboBoxPredatorMoveCost.setModel(new DefaultComboBoxModel(new String[]
		{"0.001", "0.002", "0.003", "0.004", "0.005", "0.006", "0.007", "0.008", "0.009", "0.010", "0.011", "0.012", "0.013", "0.014", "0.015", "0.016", "0.017", "0.018", "0.019", "0.020", "0.021", "0.022", "0.023", "0.024", "0.025", "0.026", "0.027", "0.028", "0.029", "0.030", "0.031", "0.032", "0.033", "0.034", "0.035", "0.036", "0.037", "0.038", "0.039", "0.040", "0.041", "0.042", "0.043", "0.044", "0.045", "0.046", "0.047", "0.048", "0.049", "0.050", "0.051", "0.052", "0.053", "0.054", "0.055", "0.056", "0.057", "0.058", "0.059", "0.060", "0.061", "0.062", "0.063", "0.064", "0.065",
				"0.066", "0.067", "0.068", "0.069", "0.070", "0.071", "0.072", "0.073", "0.074", "0.075", "0.076", "0.077", "0.078", "0.079", "0.080", "0.081", "0.082", "0.083", "0.084", "0.085", "0.086", "0.087", "0.088", "0.089", "0.090", "0.091", "0.092", "0.093", "0.094", "0.095", "0.096", "0.097", "0.098", "0.099", "0.0100", "0.0101", "0.0102", "0.0103", "0.0104", "0.0105", "0.0106", "0.0107", "0.0108", "0.0109", "0.0110", "0.0111", "0.0112", "0.0113", "0.0114", "0.0115", "0.0116", "0.0117", "0.0118", "0.0119", "0.0120", "0.0121", "0.0122", "0.0123", "0.0124", "0.0125", "0.0126",
				"0.0127", "0.0128", "0.0129", "0.0130", "0.0131", "0.0132", "0.0133", "0.0134", "0.0135", "0.0136", "0.0137", "0.0138", "0.0139", "0.0140", "0.0141", "0.0142", "0.0143", "0.0144", "0.0145", "0.0146", "0.0147", "0.0148", "0.0149", "0.0150", "0.0151", "0.0152", "0.0153", "0.0154", "0.0155", "0.0156", "0.0157", "0.0158", "0.0159", "0.0160", "0.0161", "0.0162", "0.0163", "0.0164", "0.0165", "0.0166", "0.0167", "0.0168", "0.0169", "0.0170", "0.0171", "0.0172", "0.0173", "0.0174", "0.0175", "0.0176", "0.0177", "0.0178", "0.0179", "0.0180", "0.0181", "0.0182", "0.0183", "0.0184",
				"0.0185", "0.0186", "0.0187", "0.0188", "0.0189", "0.0190", "0.0191", "0.0192", "0.0193", "0.0194", "0.0195", "0.0196", "0.0197", "0.0198", "0.0199", "0.0200", "0.0201", "0.0202", "0.0203", "0.0204", "0.0205", "0.0206", "0.0207", "0.0208", "0.0209", "0.0210", "0.0211", "0.0212", "0.0213", "0.0214", "0.0215", "0.0216", "0.0217", "0.0218", "0.0219", "0.0220", "0.0221", "0.0222", "0.0223", "0.0224", "0.0225", "0.0226", "0.0227", "0.0228", "0.0229", "0.0230", "0.0231", "0.0232", "0.0233", "0.0234", "0.0235", "0.0236", "0.0237", "0.0238", "0.0239", "0.0240", "0.0241", "0.0242",
				"0.0243", "0.0244", "0.0245", "0.0246", "0.0247", "0.0248", "0.0249", "0.0250", "0.0251", "0.0252", "0.0253", "0.0254", "0.0255", "0.0256", "0.0257", "0.0258", "0.0259", "0.0260", "0.0261", "0.0262", "0.0263", "0.0264", "0.0265", "0.0266", "0.0267", "0.0268", "0.0269", "0.0270", "0.0271", "0.0272", "0.0273", "0.0274", "0.0275", "0.0276", "0.0277", "0.0278", "0.0279", "0.0280", "0.0281", "0.0282", "0.0283", "0.0284", "0.0285", "0.0286", "0.0287", "0.0288", "0.0289", "0.0290", "0.0291", "0.0292", "0.0293", "0.0294", "0.0295", "0.0296", "0.0297", "0.0298", "0.0299", "0.0300",
				"0.0301", "0.0302", "0.0303", "0.0304", "0.0305", "0.0306", "0.0307", "0.0308", "0.0309", "0.0310", "0.0311", "0.0312", "0.0313", "0.0314", "0.0315", "0.0316", "0.0317", "0.0318", "0.0319", "0.0320", "0.0321", "0.0322", "0.0323", "0.0324", "0.0325", "0.0326", "0.0327", "0.0328", "0.0329", "0.0330", "0.0331", "0.0332", "0.0333", "0.0334", "0.0335", "0.0336", "0.0337", "0.0338", "0.0339", "0.0340", "0.0341", "0.0342", "0.0343", "0.0344", "0.0345", "0.0346", "0.0347", "0.0348", "0.0349", "0.0350", "0.0351", "0.0352", "0.0353", "0.0354", "0.0355", "0.0356", "0.0357", "0.0358",
				"0.0359", "0.0360", "0.0361", "0.0362", "0.0363", "0.0364", "0.0365", "0.0366", "0.0367", "0.0368", "0.0369", "0.0370", "0.0371", "0.0372", "0.0373", "0.0374", "0.0375", "0.0376", "0.0377", "0.0378", "0.0379", "0.0380", "0.0381", "0.0382", "0.0383", "0.0384", "0.0385", "0.0386", "0.0387", "0.0388", "0.0389", "0.0390", "0.0391", "0.0392", "0.0393", "0.0394", "0.0395", "0.0396", "0.0397", "0.0398", "0.0399", "0.0400", "0.0401", "0.0402", "0.0403", "0.0404", "0.0405", "0.0406", "0.0407", "0.0408", "0.0409", "0.0410", "0.0411", "0.0412", "0.0413", "0.0414", "0.0415", "0.0416",
				"0.0417", "0.0418", "0.0419", "0.0420", "0.0421", "0.0422", "0.0423", "0.0424", "0.0425", "0.0426", "0.0427", "0.0428", "0.0429", "0.0430", "0.0431", "0.0432", "0.0433", "0.0434", "0.0435", "0.0436", "0.0437", "0.0438", "0.0439", "0.0440", "0.0441", "0.0442", "0.0443", "0.0444", "0.0445", "0.0446", "0.0447", "0.0448", "0.0449", "0.0450", "0.0451", "0.0452", "0.0453", "0.0454", "0.0455", "0.0456", "0.0457", "0.0458", "0.0459", "0.0460", "0.0461", "0.0462", "0.0463", "0.0464", "0.0465", "0.0466", "0.0467", "0.0468", "0.0469", "0.0470", "0.0471", "0.0472", "0.0473", "0.0474",
				"0.0475", "0.0476", "0.0477", "0.0478", "0.0479", "0.0480", "0.0481", "0.0482", "0.0483", "0.0484", "0.0485", "0.0486", "0.0487", "0.0488", "0.0489", "0.0490", "0.0491", "0.0492", "0.0493", "0.0494", "0.0495", "0.0496", "0.0497", "0.0498", "0.0499", "0.0500", "0.0501", "0.0502", "0.0503", "0.0504", "0.0505", "0.0506", "0.0507", "0.0508", "0.0509", "0.0510", "0.0511", "0.0512", "0.0513", "0.0514", "0.0515", "0.0516", "0.0517", "0.0518", "0.0519", "0.0520", "0.0521", "0.0522", "0.0523", "0.0524", "0.0525", "0.0526", "0.0527", "0.0528", "0.0529", "0.0530", "0.0531", "0.0532",
				"0.0533", "0.0534", "0.0535", "0.0536", "0.0537", "0.0538", "0.0539", "0.0540", "0.0541", "0.0542", "0.0543", "0.0544", "0.0545", "0.0546", "0.0547", "0.0548", "0.0549", "0.0550", "0.0551", "0.0552", "0.0553", "0.0554", "0.0555", "0.0556", "0.0557", "0.0558", "0.0559", "0.0560", "0.0561", "0.0562", "0.0563", "0.0564", "0.0565", "0.0566", "0.0567", "0.0568", "0.0569", "0.0570", "0.0571", "0.0572", "0.0573", "0.0574", "0.0575", "0.0576", "0.0577", "0.0578", "0.0579", "0.0580", "0.0581", "0.0582", "0.0583", "0.0584", "0.0585", "0.0586", "0.0587", "0.0588", "0.0589", "0.0590",
				"0.0591", "0.0592", "0.0593", "0.0594", "0.0595", "0.0596", "0.0597", "0.0598", "0.0599", "0.0600", "0.0601", "0.0602", "0.0603", "0.0604", "0.0605", "0.0606", "0.0607", "0.0608", "0.0609", "0.0610", "0.0611", "0.0612", "0.0613", "0.0614", "0.0615", "0.0616", "0.0617", "0.0618", "0.0619", "0.0620", "0.0621", "0.0622", "0.0623", "0.0624", "0.0625", "0.0626", "0.0627", "0.0628", "0.0629", "0.0630", "0.0631", "0.0632", "0.0633", "0.0634", "0.0635", "0.0636", "0.0637", "0.0638", "0.0639", "0.0640", "0.0641", "0.0642", "0.0643", "0.0644", "0.0645", "0.0646", "0.0647", "0.0648",
				"0.0649", "0.0650", "0.0651", "0.0652", "0.0653", "0.0654", "0.0655", "0.0656", "0.0657", "0.0658", "0.0659", "0.0660", "0.0661", "0.0662", "0.0663", "0.0664", "0.0665", "0.0666", "0.0667", "0.0668", "0.0669", "0.0670", "0.0671", "0.0672", "0.0673", "0.0674", "0.0675", "0.0676", "0.0677", "0.0678", "0.0679", "0.0680", "0.0681", "0.0682", "0.0683", "0.0684", "0.0685", "0.0686", "0.0687", "0.0688", "0.0689", "0.0690", "0.0691", "0.0692", "0.0693", "0.0694", "0.0695", "0.0696", "0.0697", "0.0698", "0.0699", "0.0700", "0.0701", "0.0702", "0.0703", "0.0704", "0.0705", "0.0706",
				"0.0707", "0.0708", "0.0709", "0.0710", "0.0711", "0.0712", "0.0713", "0.0714", "0.0715", "0.0716", "0.0717", "0.0718", "0.0719", "0.0720", "0.0721", "0.0722", "0.0723", "0.0724", "0.0725", "0.0726", "0.0727", "0.0728", "0.0729", "0.0730", "0.0731", "0.0732", "0.0733", "0.0734", "0.0735", "0.0736", "0.0737", "0.0738", "0.0739", "0.0740", "0.0741", "0.0742", "0.0743", "0.0744", "0.0745", "0.0746", "0.0747", "0.0748", "0.0749", "0.0750", "0.0751", "0.0752", "0.0753", "0.0754", "0.0755", "0.0756", "0.0757", "0.0758", "0.0759", "0.0760", "0.0761", "0.0762", "0.0763", "0.0764",
				"0.0765", "0.0766", "0.0767", "0.0768", "0.0769", "0.0770", "0.0771", "0.0772", "0.0773", "0.0774", "0.0775", "0.0776", "0.0777", "0.0778", "0.0779", "0.0780", "0.0781", "0.0782", "0.0783", "0.0784", "0.0785", "0.0786", "0.0787", "0.0788", "0.0789", "0.0790", "0.0791", "0.0792", "0.0793", "0.0794", "0.0795", "0.0796", "0.0797", "0.0798", "0.0799", "0.0800", "0.0801", "0.0802", "0.0803", "0.0804", "0.0805", "0.0806", "0.0807", "0.0808", "0.0809", "0.0810", "0.0811", "0.0812", "0.0813", "0.0814", "0.0815", "0.0816", "0.0817", "0.0818", "0.0819", "0.0820", "0.0821", "0.0822",
				"0.0823", "0.0824", "0.0825", "0.0826", "0.0827", "0.0828", "0.0829", "0.0830", "0.0831", "0.0832", "0.0833", "0.0834", "0.0835", "0.0836", "0.0837", "0.0838", "0.0839", "0.0840", "0.0841", "0.0842", "0.0843", "0.0844", "0.0845", "0.0846", "0.0847", "0.0848", "0.0849", "0.0850", "0.0851", "0.0852", "0.0853", "0.0854", "0.0855", "0.0856", "0.0857", "0.0858", "0.0859", "0.0860", "0.0861", "0.0862", "0.0863", "0.0864", "0.0865", "0.0866", "0.0867", "0.0868", "0.0869", "0.0870", "0.0871", "0.0872", "0.0873", "0.0874", "0.0875", "0.0876", "0.0877", "0.0878", "0.0879", "0.0880",
				"0.0881", "0.0882", "0.0883", "0.0884", "0.0885", "0.0886", "0.0887", "0.0888", "0.0889", "0.0890", "0.0891", "0.0892", "0.0893", "0.0894", "0.0895", "0.0896", "0.0897", "0.0898", "0.0899", "0.0900", "0.0901", "0.0902", "0.0903", "0.0904", "0.0905", "0.0906", "0.0907", "0.0908", "0.0909", "0.0910", "0.0911", "0.0912", "0.0913", "0.0914", "0.0915", "0.0916", "0.0917", "0.0918", "0.0919", "0.0920", "0.0921", "0.0922", "0.0923", "0.0924", "0.0925", "0.0926", "0.0927", "0.0928", "0.0929", "0.0930", "0.0931", "0.0932", "0.0933", "0.0934", "0.0935", "0.0936", "0.0937", "0.0938",
				"0.0939", "0.0940", "0.0941", "0.0942", "0.0943", "0.0944", "0.0945", "0.0946", "0.0947", "0.0948", "0.0949", "0.0950", "0.0951", "0.0952", "0.0953", "0.0954", "0.0955", "0.0956", "0.0957", "0.0958", "0.0959", "0.0960", "0.0961", "0.0962", "0.0963", "0.0964", "0.0965", "0.0966", "0.0967", "0.0968", "0.0969", "0.0970", "0.0971", "0.0972", "0.0973", "0.0974", "0.0975", "0.0976", "0.0977", "0.0978", "0.0979", "0.0980", "0.0981", "0.0982", "0.0983", "0.0984", "0.0985", "0.0986", "0.0987", "0.0988", "0.0989", "0.0990", "0.0991", "0.0992", "0.0993", "0.0994", "0.0995", "0.0996",
				"0.0997", "0.0998", "0.0999"}));
		agentParamPanel.add(comboBoxPredatorMoveCost);
		comboBoxPredatorMoveCost.setSelectedIndex(predatorDefaultMovecostSelected);

		JLabel lblHungerThreshold = new JLabel("Hunger Threshold");
		lblHungerThreshold.setHorizontalAlignment(SwingConstants.LEFT);
		agentParamPanel.add(lblHungerThreshold);

		comboBoxPreyHungerThres = new JComboBox();
		comboBoxPreyHungerThres.setToolTipText("The energy level at which Prey become hungry.");
		comboBoxPreyHungerThres.setModel(new DefaultComboBoxModel(
				new String[]
				{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "90", "91", "92", "93", "94", "95", "96", "97", "98",
						"99", "100"}));
		agentParamPanel.add(comboBoxPreyHungerThres);
		comboBoxPreyHungerThres.setSelectedIndex(preyDefaultHungerThresSelected);

		comboBoxPredatorHungerThres = new JComboBox();
		comboBoxPredatorHungerThres.setToolTipText("The energy level at which Predators become hungry.");
		comboBoxPredatorHungerThres.setModel(new DefaultComboBoxModel(
				new String[]
				{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "90", "91", "92", "93", "94", "95", "96", "97", "98",
						"99", "100"}));
		agentParamPanel.add(comboBoxPredatorHungerThres);
		comboBoxPredatorHungerThres.setSelectedIndex(predatorDefaultHungerThresSelected);

		JLabel lblConsumptionRate = new JLabel("E Consumption Rate");
		lblConsumptionRate.setHorizontalAlignment(SwingConstants.LEFT);
		agentParamPanel.add(lblConsumptionRate);

		comboBoxPreyConsumptionRate = new JComboBox();
		comboBoxPreyConsumptionRate.setToolTipText("The rate at which Prey consume a Plant.");
		comboBoxPreyConsumptionRate.setModel(new DefaultComboBoxModel(
				new String[]
				{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "90", "91", "92", "93", "94", "95", "96", "97", "98",
						"99", "100"}));
		agentParamPanel.add(comboBoxPreyConsumptionRate);
		comboBoxPreyConsumptionRate.setSelectedIndex(preyDefaultConsumptionRateSelected);

		comboBoxPredatorConsumptionRate = new JComboBox();
		comboBoxPredatorConsumptionRate.setToolTipText("The rate at which Predators consume Prey. ( Fixed at 100%)");
		comboBoxPredatorConsumptionRate.setEnabled(false);
		agentParamPanel.add(comboBoxPredatorConsumptionRate);
		comboBoxPredatorConsumptionRate.setModel(new DefaultComboBoxModel(new String[]
		{"10", "20", "30", "40", "50", "60", "70", "80", "90", "100"}));
		comboBoxPredatorConsumptionRate.setSelectedIndex(predatorDefaultConsumptionRateSelected);
		comboBoxPredatorConsumptionRate.setEditable(false);

		JLabel lblReproductionCost = new JLabel("Reproduction Cost");
		lblReproductionCost.setHorizontalAlignment(SwingConstants.LEFT);
		agentParamPanel.add(lblReproductionCost);

		comboBoxPreyRepoCost = new JComboBox();
		comboBoxPreyRepoCost.setToolTipText("The cost of reproduction for Prey. ( 100 * Reproduction Cost)");
		comboBoxPreyRepoCost.setModel(new DefaultComboBoxModel(new String[]
		{"0.01", "0.02", "0.03", "0.04", "0.05", "0.06", "0.07", "0.08", "0.09", "0.10", "0.11", "0.12", "0.13", "0.14", "0.15", "0.16", "0.17", "0.18", "0.19", "0.20", "0.21", "0.22", "0.23", "0.24", "0.25", "0.26", "0.27", "0.28", "0.29", "0.30", "0.31", "0.32", "0.33", "0.34", "0.35", "0.36", "0.37", "0.38", "0.39", "0.40", "0.41", "0.42", "0.43", "0.44", "0.45", "0.46", "0.47", "0.48", "0.49", "0.50", "0.51", "0.52", "0.53", "0.54", "0.55", "0.56", "0.57", "0.58", "0.59", "0.60", "0.61", "0.62", "0.63", "0.64", "0.65", "0.66", "0.67", "0.68", "0.69", "0.70", "0.71", "0.72", "0.73",
				"0.74", "0.75", "0.76", "0.77", "0.78", "0.79", "0.80", "0.81", "0.82", "0.83", "0.84", "0.85", "0.86", "0.87", "0.88", "0.89", "0.90", "0.91", "0.92", "0.93", "0.94", "0.95", "0.96", "0.97", "0.98", "0.99", "1"}));
		agentParamPanel.add(comboBoxPreyRepoCost);
		comboBoxPreyRepoCost.setSelectedIndex(preyDefaultRepoCostSelected);

		comboBoxPredRepoCost = new JComboBox();
		comboBoxPredRepoCost.setToolTipText("The cost of reproduction for Predators. ( 100 * Reproduction Cost)");
		comboBoxPredRepoCost.setModel(new DefaultComboBoxModel(new String[]
		{"0.01", "0.02", "0.03", "0.04", "0.05", "0.06", "0.07", "0.08", "0.09", "0.10", "0.11", "0.12", "0.13", "0.14", "0.15", "0.16", "0.17", "0.18", "0.19", "0.20", "0.21", "0.22", "0.23", "0.24", "0.25", "0.26", "0.27", "0.28", "0.29", "0.30", "0.31", "0.32", "0.33", "0.34", "0.35", "0.36", "0.37", "0.38", "0.39", "0.40", "0.41", "0.42", "0.43", "0.44", "0.45", "0.46", "0.47", "0.48", "0.49", "0.50", "0.51", "0.52", "0.53", "0.54", "0.55", "0.56", "0.57", "0.58", "0.59", "0.60", "0.61", "0.62", "0.63", "0.64", "0.65", "0.66", "0.67", "0.68", "0.69", "0.70", "0.71", "0.72", "0.73",
				"0.74", "0.75", "0.76", "0.77", "0.78", "0.79", "0.80", "0.81", "0.82", "0.83", "0.84", "0.85", "0.86", "0.87", "0.88", "0.89", "0.90", "0.91", "0.92", "0.93", "0.94", "0.95", "0.96", "0.97", "0.98", "0.99", "1"}));
		agentParamPanel.add(comboBoxPredRepoCost);
		comboBoxPredRepoCost.setSelectedIndex(predatorDefaultRepoCostSelected);

		JLabel lblStartingEnergy1 = new JLabel("Starting Energy");
		lblStartingEnergy1.setHorizontalAlignment(SwingConstants.LEFT);
		agentParamPanel.add(lblStartingEnergy1);

		comboBoxPreyStartingEnergy = new JComboBox();
		comboBoxPreyStartingEnergy.setToolTipText("The starting energy level for Prey.");
		comboBoxPreyStartingEnergy.setModel(new DefaultComboBoxModel(new String[]
		{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "90", "91", "92", "93", "94", "95", "96", "97", "98", "99"}));
		agentParamPanel.add(comboBoxPreyStartingEnergy);
		comboBoxPreyStartingEnergy.setSelectedIndex(preyDefaultStartingEnergySelected);

		comboBoxPredStartingEnergy = new JComboBox();
		comboBoxPredStartingEnergy.setToolTipText("The starting energy level for Predators.");
		comboBoxPredStartingEnergy.setModel(new DefaultComboBoxModel(new String[]
		{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "90", "91", "92", "93", "94", "95", "96", "97", "98", "99"}));
		agentParamPanel.add(comboBoxPredStartingEnergy);
		comboBoxPredStartingEnergy.setSelectedIndex(predatorDefaultStartingEnergySelected);

		plantParamPanel = new JPanel();
		plantParamPanel.setBorder(new TitledBorder(null, "Plant Parameters", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbcPlantParamPanel = new GridBagConstraints();
		gbcPlantParamPanel.gridwidth = 2;
		gbcPlantParamPanel.fill = GridBagConstraints.HORIZONTAL;
		gbcPlantParamPanel.anchor = GridBagConstraints.NORTH;
		gbcPlantParamPanel.insets = new Insets(0, 0, 5, 0);
		gbcPlantParamPanel.gridx = 0;
		gbcPlantParamPanel.gridy = 2;
		controlPanelTop.add(plantParamPanel, gbcPlantParamPanel);
		plantParamPanel.setLayout(new GridLayout(0, 4, 4, 3));

		JLabel lblPlantRegenRate = new JLabel("Plant Regen Rate");
		lblPlantRegenRate.setHorizontalAlignment(SwingConstants.LEFT);
		plantParamPanel.add(lblPlantRegenRate);

		comboBoxPlantRegenRate = new JComboBox();
		comboBoxPlantRegenRate.setToolTipText("The number of new Plants that appear each simulation step.");
		comboBoxPlantRegenRate.setModel(new DefaultComboBoxModel(new String[]
		{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "64", "128", "256", "512", "1024", "2048", "4096", "8192", "16384", "32768", "65536", "131072", "262144", "524288", "1048576", "2097152", "4194304"}));
		plantParamPanel.add(comboBoxPlantRegenRate);
		comboBoxPlantRegenRate.setSelectedIndex(plantDefaultRegenrateSelected);

		JLabel lblEnergyAbso = new JLabel("Energy Ab Rate");
		lblEnergyAbso.setHorizontalAlignment(SwingConstants.LEFT);
		plantParamPanel.add(lblEnergyAbso);

		comboBoxEnergyAbsorptionRate = new JComboBox();
		comboBoxEnergyAbsorptionRate.setToolTipText("The rate at which Plants absorb energy from the Sun.");
		comboBoxEnergyAbsorptionRate.setModel(new DefaultComboBoxModel(new String[]
		{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "90", "91", "92", "93", "94", "95", "96", "97", "98", "99"}));
		plantParamPanel.add(comboBoxEnergyAbsorptionRate);
		comboBoxEnergyAbsorptionRate.setSelectedIndex(plantDefaultEnergyAbsorptionRateSelected);

		JLabel lblREnergyDiv = new JLabel("R/S Energy Div");
		lblREnergyDiv.setHorizontalAlignment(SwingConstants.LEFT);
		plantParamPanel.add(lblREnergyDiv);

		JComboBox comboBoxPlantRedDiv = new JComboBox();
		comboBoxPlantRedDiv.setToolTipText("Sets the division between how much energy is used for survival and how much is used for reproduction of a new Plant. (Disabled)");
		comboBoxPlantRedDiv.setEnabled(false);
		comboBoxPlantRedDiv.setModel(new DefaultComboBoxModel(new String[]
		{"0.25", "0.50", "0.75", "1.00"}));
		plantParamPanel.add(comboBoxPlantRedDiv);

		JLabel lblPlantStartingEnergy = new JLabel("Starting Energy");
		lblPlantStartingEnergy.setHorizontalAlignment(SwingConstants.LEFT);
		plantParamPanel.add(lblPlantStartingEnergy);

		comboBoxPlantStartingEnergy = new JComboBox();
		comboBoxPlantStartingEnergy.setToolTipText("The starting energy level for Plants.");
		comboBoxPlantStartingEnergy.setModel(new DefaultComboBoxModel(new String[]
		{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "90", "91", "92", "93", "94", "95", "96", "97", "98", "99"}));
		plantParamPanel.add(comboBoxPlantStartingEnergy);
		comboBoxPlantStartingEnergy.setSelectedIndex(plantStartingEnergySelected);

		/*JLabel lblRepoCost = new JLabel("Repo Cost");
		lblRepoCost.setHorizontalAlignment(SwingConstants.LEFT);
		plantParamPanel.add(lblRepoCost);*/

		/*comboBoxPlantRepoCost = new JComboBox();
		comboBoxPlantRepoCost.setToolTipText("The cost of reproduction for Plants. ( Disabled)");
		comboBoxPlantRepoCost.setEnabled(false);
		comboBoxPlantRepoCost.setModel(new DefaultComboBoxModel(new String[]
		{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "90", "91", "92", "93", "94", "95", "96", "97", "98", "99"}));
		plantParamPanel.add(comboBoxPlantRepoCost);
		comboBoxPlantRepoCost.setSelectedIndex(plantDefaultPlantRepoCostSelected);*/
		
		controlPanelBottom = new JPanel();
		controlPanelBottom.setBorder(new TitledBorder(null, "Control", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		controlPanel.add(controlPanelBottom, BorderLayout.SOUTH);
		GridBagLayout gbl_controlPanelBottom = new GridBagLayout();
		gbl_controlPanelBottom.columnWidths = new int[]{112, 112, 112, 0};
		gbl_controlPanelBottom.rowHeights = new int[]{31, 31, 31, 31, 0};
		gbl_controlPanelBottom.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_controlPanelBottom.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
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
				}
				else
				{
					lblSimRateInfo.setText(Integer.toString(simRateSlider.getValue()));
				}
	
				// Set the requested update rate
				sim.reqSimUpdateRate(simRateSlider.getValue());
	
			}
		});
						
		btnGenerate = new JButton("Generate");
		btnGenerate.setIcon(new ImageIcon(SimulationGUI.class.getResource("/alife/icons/grid.png")));
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
				if(!generatingSim)
				{
					generatingSim=true;
					
					/* Locks the parameters */
					parametersLock();

					/* Do notice for first lock */
					doSimUnlockNotify();

					/* Create the new Simulation */
					newSim();	
					
					generatingSim=false;

				}

			}
		});
				
		btnStart = new JButton("Start");
		btnStart.setIcon(new ImageIcon(SimulationGUI.class.getResource("/alife/icons/play.png")));
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
		btnPause.setIcon(new ImageIcon(SimulationGUI.class.getResource("/alife/icons/pause.png")));
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

		mnParameters = new JMenu("Parameters");
		menuBar.add(mnParameters);

		mntmUnlock = new JMenuItem("Unlock");
		mntmUnlock.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				/* do the parameter unlocking sequence */
				parametersUnlock();
			}
		});
		mnParameters.add(mntmUnlock);

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

		chckbxmntmDrawTrueBodies = new JCheckBoxMenuItem("Draw True Bodies");
		chckbxmntmDrawTrueBodies.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent arg0)
			{
				if (chckbxmntmDrawTrueBodies.isSelected())
				{
					// have been checked
					SimulationView.setTrueDrawing(true);
				}
				else
				{
					// have been unchecked
					SimulationView.setTrueDrawing(false);
				}
			}
		});
		mnAgentDrawing.add(chckbxmntmDrawTrueBodies);

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
				// Unlock the frame rate of the view - as fast as your computer can churn  them out.			
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
		//gui.setAlwaysOnTop(true);

		// We are now in the start up state
		startUpState();

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
			SimulationView.exitDisplay(); // Tell OpenGL we are done and free the resources used in the canvas. - must be done else sim will lockup.        	
			// Do EXIT
			System.exit(0);
		}

	}

	/* Ensure the user wants to exit then exit the program */
	private static void doSimUnlockNotify()
	{
		if(!simUnlockNotifcationShown)
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
			simUnlockNotifcationShown=true;
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

		//SimulationView.setFocus();

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
		
		btnPause.setIcon(new ImageIcon(SimulationGUI.class.getResource("/alife/icons/resume.png")));	
		
	}

	private static void simUnPausedState()
	{
		btnPause.setText("   Pause");
		btnGenerate.setEnabled(false);

		StatsPanel.setPaused(false);

		sim.unPauseSim();
		
		btnPause.setIcon(new ImageIcon(SimulationGUI.class.getResource("/alife/icons/pause.png")));
	}

	/*
	 * Lock the parameters so the user does not get confused, as changing them
	 * will have no effect anyway
	 */
	public static void parametersLock()
	{
		/* Main Setup panel */
		comboBoxPreyNumbers.setEnabled(false);
		comboBoxPredNumbers.setEnabled(false);
		comboBoxWorldSize.setEnabled(false);
		comboBoxPlantNumbers.setEnabled(false);
		comboBoxBarrierScenario.setEnabled(false);
		comboBoxBarrierMode.setEnabled(false);


		/* Agent Param Panel */
		comboBoxPreySpeed.setEnabled(false);
		comboBoxPreyDE.setEnabled(false);
		comboBoxPreyREDiv.setEnabled(false);
		comboBoxPreyViewRange.setEnabled(false);
		comboBoxPreyMoveCost.setEnabled(false);
		comboBoxPreyHungerThres.setEnabled(false);
		comboBoxPreyConsumptionRate.setEnabled(false);
		comboBoxPreyRepoCost.setEnabled(false);
		comboBoxPreyStartingEnergy.setEnabled(false);

		comboBoxPredatorViewRange.setEnabled(false);
		comboBoxPredatorSpeed.setEnabled(false);
		comboBoxPredatorDE.setEnabled(false);
		comboBoxPredatorREDiv.setEnabled(false);
		comboBoxPredatorMoveCost.setEnabled(false);
		comboBoxPredatorHungerThres.setEnabled(false);
		comboBoxPredatorConsumptionRate.setEnabled(false);
		comboBoxPredRepoCost.setEnabled(false);
		comboBoxPredStartingEnergy.setEnabled(false);

		/* Plant Param Panel */
		comboBoxPlantRegenRate.setEnabled(false);
		comboBoxPlantStartingEnergy.setEnabled(false);
		//comboBoxPlantRepoCost.setEnabled(false);
		comboBoxEnergyAbsorptionRate.setEnabled(false);

	}

	/* Warn the user of the consequences and then unlock the parameters */
	public static void parametersUnlock()
	{

		String message;
		message = "Unlocking paramters will end this simulation!\nDo wish to unlock the parameters?";

		JOptionPane pane = new JOptionPane(message, JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_OPTION);

		// Center Dialog on the gui window
		JDialog dialog = pane.createDialog(gui, "Unlock Parameters");

		dialog.pack();
		dialog.setVisible(true);

		int value = ((Integer) pane.getValue()).intValue();

		if (value == JOptionPane.YES_OPTION)
		{
			/* Main Setup panel */
			comboBoxPreyNumbers.setEnabled(true);
			comboBoxPredNumbers.setEnabled(true);
			comboBoxWorldSize.setEnabled(true);
			comboBoxPlantNumbers.setEnabled(true);
			comboBoxBarrierScenario.setEnabled(true);
			comboBoxBarrierMode.setEnabled(true);

			/* Agent Param Panel */
			comboBoxPreySpeed.setEnabled(true);
			comboBoxPreyDE.setEnabled(true);
			comboBoxPreyREDiv.setEnabled(true);
			comboBoxPreyViewRange.setEnabled(true);
			comboBoxPreyMoveCost.setEnabled(true);
			comboBoxPreyHungerThres.setEnabled(true);
			comboBoxPreyConsumptionRate.setEnabled(true);
			comboBoxPreyRepoCost.setEnabled(true);
			comboBoxPreyStartingEnergy.setEnabled(true);

			comboBoxPredatorViewRange.setEnabled(true);
			comboBoxPredatorSpeed.setEnabled(true);
			comboBoxPredatorDE.setEnabled(true);
			comboBoxPredatorREDiv.setEnabled(true);
			comboBoxPredatorMoveCost.setEnabled(true);
			comboBoxPredatorHungerThres.setEnabled(true);

			/*
			 * comboBoxPredatorConsumptionRate is disabled due as feature not
			 * implemented
			 */
			// comboBoxPredatorConsumptionRate.setEnabled(true);

			comboBoxPredRepoCost.setEnabled(true);
			comboBoxPredStartingEnergy.setEnabled(true);

			/* Plant Param Panel */
			comboBoxPlantRegenRate.setEnabled(true);
			comboBoxPlantStartingEnergy.setEnabled(true);

			/* comboBoxPlantRepoCost is disabled due as feature not implemented */
			// comboBoxPlantRepoCost.setEnabled(true);			

			comboBoxEnergyAbsorptionRate.setEnabled(true);

			/* Check needed due to semaphores being used */
			if (!sim.simPaused())
			{
				/* Pause the sim */
				simPausedState();

			}

			/* Disable resume */
			btnPause.setEnabled(false);

			/* Clear the old stats */
			StatsPanel.clearStats();
			clearGUIStats();
		}
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
	 * @param asps int
	 */
	public static void setASPS(int asps)
	{
		lblASPSNo.setText(Integer.toString(asps));
	}

	/**
	 * The curernt step number.
	 * 
	 * @param no
	 */
	public static void setStepNo(int no)
	{
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
