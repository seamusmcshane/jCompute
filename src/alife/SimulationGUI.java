package alife;

import java.awt.Dimension;
import java.awt.EventQueue;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;
import javax.swing.JToggleButton;
import javax.swing.JCheckBox;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.ButtonGroup;

public class SimulationGUI
{
	private static JFrame gui;

	/** Gui Frame Items */
	private static JSlider simRateSlider;
	private static JButton btnGenerate;
	private static JButton btnPause;
	private static JButton btnStart;
	private static JTextField txtSimRateInfo;

	private static JComboBox comboBoxPreyNumbers;
	private static int preynumbers_selected = 8; // selects 1024	

	private static JComboBox comboBoxPredNumbers;
	private static int predatornumbers_selected = 2; // selects 1024	

	private static JComboBox comboBoxWorldSize;
	private static int worldsize_selected = 1; // selects 1024

	private static JComboBox comboBoxPlantNumbers;
	private static int plant_starting_numbers_selected = 8; // selects 3200

	private static JComboBox comboBoxPlantRegenRate;
	private static int plant_default_regenrate_selected = 7; // Selects	8	

	private static JComboBox comboBoxEnergyAbsorptionRate;
	private static int plant_default_energyabsorptionrate_selected = 7; // Selects 8	

	private static JComboBox comboBoxPlantStartingEnergy;
	private static int plant_starting_energy_selected = 49; // selects 50

	private static JComboBox comboBoxPlantRepoCost;
	private static int plant_default_plantrepocost_selected = 1; // not used DISABLED		

	private static JComboBox comboBoxPreySpeed;
	private static int prey_default_speed_selected = 89; // Selects 0.90

	private static JComboBox comboBoxPredatorSpeed;
	private static int predator_default_speed_selected = 99; // Selects 1.00	

	private static JComboBox comboBoxPreyViewRange;
	private static int prey_default_view_range_selected = 9; // Selects 10

	private static JComboBox comboBoxPredatorViewRange;
	private static int predator_default_view_range_selected = 9; // Selects 10

	private static JComboBox comboBoxPreyDE;
	private static int prey_default_DE_selected = 39; // 0.40

	private static JComboBox comboBoxPredatorDE;
	private static int predator_default_DE_selected = 49; // 0.50	

	private static JComboBox comboBoxPreyREDiv;
	private static int prey_default_REDiv_selected = 1; // 0.50

	private static JComboBox comboBoxPredatorREDiv;
	private static int predator_default_REDiv_selected = 1; // 0.50

	private static JComboBox comboBoxPreyMoveCost;
	private static int prey_default_movecost_selected = 24; // 0.025

	private static JComboBox comboBoxPredatorMoveCost;
	private static int predator_default_movecost_selected = 24; // 0.025

	private static JComboBox comboBoxPreyHungerThres;
	private static int prey_default_hungerthres_selected = 50; // 50

	private static JComboBox comboBoxPredatorHungerThres;
	private static int predator_default_hungerthres_selected = 50; // 50

	private static JComboBox comboBoxPreyConsumptionRate;
	private static int prey_default_consumptionrate_selected = 9; // Selects 9	

	private static JComboBox comboBoxPredatorConsumptionRate;
	private static int predator_default_consumptionrate_selected = 9; // selects 100 (*Not* Enabled)	

	private static JComboBox comboBoxPreyRepoCost;
	private static int prey_default_repocost_selected = 49;		// Selects 50		

	private static JComboBox comboBoxPredRepoCost;
	private static int predator_default_repocost_selected = 54;		// // Selects 55			

	private static JComboBox comboBoxPreyStartingEnergy;
	private static int prey_default_startingenergy_selected = 24;	 // Selects 25		

	private static JComboBox comboBoxPredStartingEnergy;
	private static int predator_default_startingenergy_selected = 24;	// Selects 25			

	private static StatsPanel statsPanel;

	/* static in screen sizes */
	static int screen_width;
	static int screen_height;

	/** Window Size */
	static int pad = 10;

	/* GUI Size Hard-Coded - minimum size before cropping occurs */
	static int control_gui_width = 375;
	static int control_gui_height = 800;

	static int control_gui_x;
	static int control_gui_y;

	static int view_width = 0;
	static int view_height = 0;
	static int view_x;
	static int view_y;

	/* Simulation Reference */
	private static Simulation sim;

	// A param Object to carry variables though the classes.
	private static SimpleAgentManagementSetupParam agentSettings = new SimpleAgentManagementSetupParam();

	public static JPanel agentParamPanel;

	public static JPanel plantParamPanel;

	private static JCheckBoxMenuItem chckbxmntmDisplayView;

	private static JCheckBoxMenuItem chckbxmntmDrawTrueBodies;

	private static JCheckBoxMenuItem chckbxmntmDrawFieldOf;
	private static JPanel simRateInfoPanel;
	private static JMenu mnParameters;
	private static final ButtonGroup buttonGroup = new ButtonGroup();
	private static JMenuItem mntmUnlock;

	/* Logic */

	/**
	 * @wbp.parser.entryPoint
	 */
	public static void main(String args[])
	{
		retrieveScreenSize();

		calculateWindowSizes();

		calculateWindowPositions();

		setUpFrame();

		setUpSimulation();

		SimulationView.displayView(sim, view_x, view_y, view_width, view_height);

	}

	private static void newSim()
	{

		/*
		 * Main Setup
		 */

		/* World Size */
		int word_size = Integer.parseInt(comboBoxWorldSize.getSelectedItem().toString());

		/* Prey Numbers */
		int prey_no = Integer.parseInt(comboBoxPreyNumbers.getSelectedItem().toString());

		/* Pred Numbers */
		int pred_no = Integer.parseInt(comboBoxPredNumbers.getSelectedItem().toString());

		/* Plant Numbers */
		int plant_no = Integer.parseInt(comboBoxPlantNumbers.getSelectedItem().toString());

		/*
		 * Plants via direct variable passing
		 */

		/* Plant Regeneration Rate */
		int plant_regen_rate = Integer.parseInt(comboBoxPlantRegenRate.getSelectedItem().toString());

		/* Plant Energy Absorption Rate */
		int plant_energy_absorption_rate = Integer.parseInt(comboBoxEnergyAbsorptionRate.getSelectedItem().toString());

		/* Plant Starting Energy */
		int plant_starting_energy = Integer.parseInt(comboBoxPlantStartingEnergy.getSelectedItem().toString());

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

		sim.newSim(statsPanel, word_size, prey_no, pred_no, plant_no, plant_regen_rate, plant_starting_energy, plant_energy_absorption_rate, agentSettings);

		/*
		 * If needed the GC can free old objects now, before the simulation
		 * starts
		 */
		System.gc();

		btnGenerate.setEnabled(true);

		btnStart.setEnabled(true);

		btnPause.setEnabled(false);

		btnPause.setText("Pause");

		simRateSlider.setEnabled(false);

		simRateSlider.setValue(15);

		// Centers the simulated world in the view
		SimulationView.setInitalViewTranslate((view_width / 2) - ((Integer.parseInt(comboBoxWorldSize.getSelectedItem().toString())) / 2), (view_height / 2) - ((Integer.parseInt(comboBoxWorldSize.getSelectedItem().toString())) / 2));

		/* Clear the old stats */
		StatsPanel.clearStats();

	}

	private static void setUpSimulation()
	{
		sim = new Simulation();
	}

	private static void calculateWindowSizes()
	{
		view_width = screen_width - control_gui_width - (pad * 2);
		view_height = screen_height - (48 * 2); // Task manager and Top borders on some os's
	}

	private static void calculateWindowPositions()
	{
		control_gui_x = (screen_width / 2) - ((control_gui_width + view_width) / 2);
		control_gui_y = (screen_height / 2) - (view_height / 2);

		view_x = control_gui_x + (control_gui_width) + pad;
		view_y = control_gui_y;
	}

	private static void retrieveScreenSize()
	{
		Toolkit toolkit = Toolkit.getDefaultToolkit();

		Dimension screenSize = toolkit.getScreenSize();

		screen_width = (int) screenSize.getWidth();
		screen_height = (int) screenSize.getHeight();
	}

	
	/** This method contains sections that are largely auto generated from the editor **/
	private static void setUpFrame()
	{
		/* OS look */
		lookandFeel();

		gui = new JFrame();
		gui.setResizable(false);
		gui.setTitle("Alife Simulation");
		gui.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		// for editing
		gui.setBounds(control_gui_x, control_gui_y, control_gui_width, control_gui_height);

		// For distribution
		//gui.setBounds(control_gui_x, control_gui_y, 457, 1032);
		gui.getContentPane().setLayout(new BorderLayout(0, 0));

		JPanel controlPanel = new JPanel();
		controlPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		// controlPanel.setPreferredSize(new Dimension(controlPanelWidth,
		// controlPanelHeight));

		gui.getContentPane().add(controlPanel, BorderLayout.CENTER);

		controlPanel.setLayout(new BorderLayout(0, 0));

		JPanel controlPanelTop = new JPanel();

		controlPanel.add(controlPanelTop, BorderLayout.NORTH);
		GridBagLayout gbl_controlPanelTop = new GridBagLayout();
		gbl_controlPanelTop.columnWidths = new int[]
		{0};
		gbl_controlPanelTop.rowHeights = new int[]
		{0, 0, 0};
		gbl_controlPanelTop.columnWeights = new double[]
		{1.0, 1.0};
		gbl_controlPanelTop.rowWeights = new double[]
		{Double.MIN_VALUE};
		controlPanelTop.setLayout(gbl_controlPanelTop);

		JPanel mainSetupPanel = new JPanel();
		mainSetupPanel.setBorder(new TitledBorder(null, "Simulation Setup", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_mainSetupPanel = new GridBagConstraints();
		gbc_mainSetupPanel.anchor = GridBagConstraints.NORTH;
		gbc_mainSetupPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_mainSetupPanel.insets = new Insets(0, 0, 5, 0);
		gbc_mainSetupPanel.gridwidth = 2;
		gbc_mainSetupPanel.gridx = 0;
		gbc_mainSetupPanel.gridy = 0;
		controlPanelTop.add(mainSetupPanel, gbc_mainSetupPanel);
		mainSetupPanel.setLayout(new GridLayout(2, 4, 5, 5));

		JLabel lblPredS = new JLabel("Predators");
		lblPredS.setHorizontalAlignment(SwingConstants.LEFT);
		mainSetupPanel.add(lblPredS);

		comboBoxPredNumbers = new JComboBox();
		comboBoxPredNumbers.setToolTipText("Set the inital Predator numbers.");
		mainSetupPanel.add(comboBoxPredNumbers);
		comboBoxPredNumbers.setModel(new DefaultComboBoxModel(new String[]
		{"0", "1", "10", "100", "200", "400", "800", "1600", "3200", "6400", "12800", "25600", "51200", "102400"}));
		comboBoxPredNumbers.setSelectedIndex(predatornumbers_selected);

		JLabel lblPreyS = new JLabel("Prey");
		lblPreyS.setHorizontalAlignment(SwingConstants.LEFT);
		mainSetupPanel.add(lblPreyS);

		comboBoxPreyNumbers = new JComboBox();
		comboBoxPreyNumbers.setToolTipText("Set the inital Prey numbers.");
		mainSetupPanel.add(comboBoxPreyNumbers);
		comboBoxPreyNumbers.setModel(new DefaultComboBoxModel(new String[]
		{"0", "1", "10", "100", "200", "400", "800", "1600", "3200", "6400", "12800", "25600", "51200", "102400"}));
		comboBoxPreyNumbers.setSelectedIndex(preynumbers_selected);

		JLabel lblPlants = new JLabel("Plants");
		lblPlants.setHorizontalAlignment(SwingConstants.LEFT);
		mainSetupPanel.add(lblPlants);

		comboBoxPlantNumbers = new JComboBox();
		comboBoxPlantNumbers.setToolTipText("Set the inital Plant numbers.");
		mainSetupPanel.add(comboBoxPlantNumbers);
		comboBoxPlantNumbers.setModel(new DefaultComboBoxModel(new String[]
		{"0", "1", "10", "100", "200", "400", "800", "1600", "3200", "6400", "12800", "25600", "51200", "102400"}));
		comboBoxPlantNumbers.setSelectedIndex(plant_starting_numbers_selected);

		JLabel lblWorldSize = new JLabel("World Size");
		lblWorldSize.setHorizontalAlignment(SwingConstants.LEFT);
		mainSetupPanel.add(lblWorldSize);

		comboBoxWorldSize = new JComboBox();
		comboBoxWorldSize.setToolTipText("Set the simulated world size.");
		mainSetupPanel.add(comboBoxWorldSize);
		comboBoxWorldSize.setModel(new DefaultComboBoxModel(new String[]
		{"512", "1024", "2048", "4096", "8192", "16384", "32768"}));
		comboBoxWorldSize.setSelectedIndex(worldsize_selected);

		agentParamPanel = new JPanel();
		agentParamPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Agent Parameters", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_agentParamPanel = new GridBagConstraints();
		gbc_agentParamPanel.anchor = GridBagConstraints.NORTH;
		gbc_agentParamPanel.gridwidth = 2;
		gbc_agentParamPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_agentParamPanel.insets = new Insets(0, 0, 5, 0);
		gbc_agentParamPanel.gridx = 0;
		gbc_agentParamPanel.gridy = 1;
		controlPanelTop.add(agentParamPanel, gbc_agentParamPanel);
		agentParamPanel.setLayout(new GridLayout(0, 3, 5, 5));

		JLabel lbl_spacer = new JLabel("");
		agentParamPanel.add(lbl_spacer);

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
		comboBoxPreySpeed.setSelectedIndex(prey_default_speed_selected);

		comboBoxPredatorSpeed = new JComboBox();
		comboBoxPredatorSpeed.setToolTipText("Speed of Predators.");
		comboBoxPredatorSpeed.setModel(new DefaultComboBoxModel(new String[]
		{" 0.01 ", " 0.02", " 0.03", " 0.04", " 0.05", " 0.06", " 0.07", " 0.08", " 0.09", " 0.10", " 0.11", " 0.12", " 0.13", " 0.14", " 0.15", " 0.16", " 0.17", " 0.18", " 0.19", " 0.20", " 0.21", " 0.22", " 0.23", " 0.24", " 0.25", " 0.26", " 0.27", " 0.28", " 0.29", " 0.30", " 0.31", " 0.32", " 0.33", " 0.34", " 0.35", " 0.36", " 0.37", " 0.38", " 0.39", " 0.40", " 0.41", " 0.42", " 0.43", " 0.44", " 0.45", " 0.46", " 0.47", " 0.48", " 0.49", " 0.50", " 0.51", " 0.52", " 0.53", " 0.54", " 0.55", " 0.56", " 0.57", " 0.58", " 0.59", " 0.60", " 0.61", " 0.62", " 0.63", " 0.64", " 0.65",
				" 0.66", " 0.67", " 0.68", " 0.69", " 0.70", " 0.71", " 0.72", " 0.73", " 0.74", " 0.75", " 0.76", " 0.77", " 0.78", " 0.79", " 0.80", " 0.81", " 0.82", " 0.83", " 0.84", " 0.85", " 0.86", " 0.87", " 0.88", " 0.89", " 0.90", " 0.91", " 0.92", " 0.93", " 0.94", " 0.95", " 0.96", " 0.97", " 0.98", " 0.99", " 1.00"}));
		agentParamPanel.add(comboBoxPredatorSpeed);
		comboBoxPredatorSpeed.setSelectedIndex(predator_default_speed_selected);

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
		comboBoxPreyViewRange.setSelectedIndex(prey_default_view_range_selected);

		comboBoxPredatorViewRange = new JComboBox();
		comboBoxPredatorViewRange.setToolTipText("View range of Predators. ( Distance of R in front of the body of the agent, where R is a Radius value)");
		comboBoxPredatorViewRange.setModel(new DefaultComboBoxModel(
				new String[]
				{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "90", "91", "92", "93", "94", "95", "96", "97", "98",
						"99", "100"}));
		agentParamPanel.add(comboBoxPredatorViewRange);
		comboBoxPredatorViewRange.setSelectedIndex(predator_default_view_range_selected);

		JLabel lblDigestiveEfficn = new JLabel("Digestive Efficiency");
		lblDigestiveEfficn.setHorizontalAlignment(SwingConstants.LEFT);
		agentParamPanel.add(lblDigestiveEfficn);

		comboBoxPreyDE = new JComboBox();
		comboBoxPreyDE.setToolTipText("How efficiently Prey gain energy from consumption.");
		comboBoxPreyDE.setModel(new DefaultComboBoxModel(new String[]
		{" 0.01 ", " 0.02", " 0.03", " 0.04", " 0.05", " 0.06", " 0.07", " 0.08", " 0.09", " 0.10", " 0.11", " 0.12", " 0.13", " 0.14", " 0.15", " 0.16", " 0.17", " 0.18", " 0.19", " 0.20", " 0.21", " 0.22", " 0.23", " 0.24", " 0.25", " 0.26", " 0.27", " 0.28", " 0.29", " 0.30", " 0.31", " 0.32", " 0.33", " 0.34", " 0.35", " 0.36", " 0.37", " 0.38", " 0.39", " 0.40", " 0.41", " 0.42", " 0.43", " 0.44", " 0.45", " 0.46", " 0.47", " 0.48", " 0.49", " 0.50", " 0.51", " 0.52", " 0.53", " 0.54", " 0.55", " 0.56", " 0.57", " 0.58", " 0.59", " 0.60", " 0.61", " 0.62", " 0.63", " 0.64", " 0.65",
				" 0.66", " 0.67", " 0.68", " 0.69", " 0.70", " 0.71", " 0.72", " 0.73", " 0.74", " 0.75", " 0.76", " 0.77", " 0.78", " 0.79", " 0.80", " 0.81", " 0.82", " 0.83", " 0.84", " 0.85", " 0.86", " 0.87", " 0.88", " 0.89", " 0.90", " 0.91", " 0.92", " 0.93", " 0.94", " 0.95", " 0.96", " 0.97", " 0.98", " 0.99", " 1.00"}));
		agentParamPanel.add(comboBoxPreyDE);
		comboBoxPreyDE.setSelectedIndex(prey_default_DE_selected);

		comboBoxPredatorDE = new JComboBox();
		comboBoxPredatorDE.setToolTipText("How efficiently Predators gain energy from consumption.");
		comboBoxPredatorDE.setModel(new DefaultComboBoxModel(new String[]
		{" 0.01 ", " 0.02", " 0.03", " 0.04", " 0.05", " 0.06", " 0.07", " 0.08", " 0.09", " 0.10", " 0.11", " 0.12", " 0.13", " 0.14", " 0.15", " 0.16", " 0.17", " 0.18", " 0.19", " 0.20", " 0.21", " 0.22", " 0.23", " 0.24", " 0.25", " 0.26", " 0.27", " 0.28", " 0.29", " 0.30", " 0.31", " 0.32", " 0.33", " 0.34", " 0.35", " 0.36", " 0.37", " 0.38", " 0.39", " 0.40", " 0.41", " 0.42", " 0.43", " 0.44", " 0.45", " 0.46", " 0.47", " 0.48", " 0.49", " 0.50", " 0.51", " 0.52", " 0.53", " 0.54", " 0.55", " 0.56", " 0.57", " 0.58", " 0.59", " 0.60", " 0.61", " 0.62", " 0.63", " 0.64", " 0.65",
				" 0.66", " 0.67", " 0.68", " 0.69", " 0.70", " 0.71", " 0.72", " 0.73", " 0.74", " 0.75", " 0.76", " 0.77", " 0.78", " 0.79", " 0.80", " 0.81", " 0.82", " 0.83", " 0.84", " 0.85", " 0.86", " 0.87", " 0.88", " 0.89", " 0.90", " 0.91", " 0.92", " 0.93", " 0.94", " 0.95", " 0.96", " 0.97", " 0.98", " 0.99", " 1.00"}));
		agentParamPanel.add(comboBoxPredatorDE);
		comboBoxPredatorDE.setSelectedIndex(predator_default_DE_selected);

		JLabel lblReproductionDiv = new JLabel("R/S Energy Div");
		lblReproductionDiv.setHorizontalAlignment(SwingConstants.LEFT);
		agentParamPanel.add(lblReproductionDiv);

		comboBoxPreyREDiv = new JComboBox();
		comboBoxPreyREDiv.setToolTipText("Sets the division between how much energy is used for survival and how much is used for reproduction of a new agent.");
		comboBoxPreyREDiv.setModel(new DefaultComboBoxModel(new String[]
		{"0.25", "0.50", "0.75"}));
		agentParamPanel.add(comboBoxPreyREDiv);
		comboBoxPreyREDiv.setSelectedIndex(prey_default_REDiv_selected);

		comboBoxPredatorREDiv = new JComboBox();
		comboBoxPredatorREDiv.setToolTipText("Sets the division between how much energy is used for survival and how much is used for reproduction of a new agent.");
		comboBoxPredatorREDiv.setModel(new DefaultComboBoxModel(new String[]
		{"0.25", "0.50", "0.75"}));
		agentParamPanel.add(comboBoxPredatorREDiv);
		comboBoxPredatorREDiv.setSelectedIndex(predator_default_REDiv_selected);

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
		comboBoxPreyMoveCost.setSelectedIndex(prey_default_movecost_selected);

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
		comboBoxPredatorMoveCost.setSelectedIndex(predator_default_movecost_selected);

		JLabel lblHungerThreshold = new JLabel("Hunger Threshold");
		lblHungerThreshold.setHorizontalAlignment(SwingConstants.LEFT);
		agentParamPanel.add(lblHungerThreshold);

		comboBoxPreyHungerThres = new JComboBox();
		comboBoxPreyHungerThres.setToolTipText("The energy level at which Prey become hungry.");
		comboBoxPreyHungerThres
				.setModel(new DefaultComboBoxModel(new String[]
				{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "90", "91", "92", "93", "94", "95", "96", "97",
						"98", "99"}));
		agentParamPanel.add(comboBoxPreyHungerThres);
		comboBoxPreyHungerThres.setSelectedIndex(prey_default_hungerthres_selected);

		comboBoxPredatorHungerThres = new JComboBox();
		comboBoxPredatorHungerThres.setToolTipText("The energy level at which Predators become hungry.");
		comboBoxPredatorHungerThres
				.setModel(new DefaultComboBoxModel(new String[]
				{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "90", "91", "92", "93", "94", "95", "96", "97",
						"98", "99"}));
		agentParamPanel.add(comboBoxPredatorHungerThres);
		comboBoxPredatorHungerThres.setSelectedIndex(predator_default_hungerthres_selected);

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
		comboBoxPreyConsumptionRate.setSelectedIndex(prey_default_consumptionrate_selected);

		comboBoxPredatorConsumptionRate = new JComboBox();
		comboBoxPredatorConsumptionRate.setToolTipText("The rate at which Predators consume Prey. ( Fixed at 100%)");
		comboBoxPredatorConsumptionRate.setEnabled(false);
		agentParamPanel.add(comboBoxPredatorConsumptionRate);
		comboBoxPredatorConsumptionRate.setModel(new DefaultComboBoxModel(new String[]
		{"10", "20", "30", "40", "50", "60", "70", "80", "90", "100"}));
		comboBoxPredatorConsumptionRate.setSelectedIndex(predator_default_consumptionrate_selected);
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
		comboBoxPreyRepoCost.setSelectedIndex(prey_default_repocost_selected);

		comboBoxPredRepoCost = new JComboBox();
		comboBoxPredRepoCost.setToolTipText("The cost of reproduction for Predators. ( 100 * Reproduction Cost)");
		comboBoxPredRepoCost.setModel(new DefaultComboBoxModel(new String[]
		{"0.01", "0.02", "0.03", "0.04", "0.05", "0.06", "0.07", "0.08", "0.09", "0.10", "0.11", "0.12", "0.13", "0.14", "0.15", "0.16", "0.17", "0.18", "0.19", "0.20", "0.21", "0.22", "0.23", "0.24", "0.25", "0.26", "0.27", "0.28", "0.29", "0.30", "0.31", "0.32", "0.33", "0.34", "0.35", "0.36", "0.37", "0.38", "0.39", "0.40", "0.41", "0.42", "0.43", "0.44", "0.45", "0.46", "0.47", "0.48", "0.49", "0.50", "0.51", "0.52", "0.53", "0.54", "0.55", "0.56", "0.57", "0.58", "0.59", "0.60", "0.61", "0.62", "0.63", "0.64", "0.65", "0.66", "0.67", "0.68", "0.69", "0.70", "0.71", "0.72", "0.73",
				"0.74", "0.75", "0.76", "0.77", "0.78", "0.79", "0.80", "0.81", "0.82", "0.83", "0.84", "0.85", "0.86", "0.87", "0.88", "0.89", "0.90", "0.91", "0.92", "0.93", "0.94", "0.95", "0.96", "0.97", "0.98", "0.99", "1"}));
		agentParamPanel.add(comboBoxPredRepoCost);
		comboBoxPredRepoCost.setSelectedIndex(predator_default_repocost_selected);

		JLabel lblStartingEnergy_1 = new JLabel("Starting Energy");
		lblStartingEnergy_1.setHorizontalAlignment(SwingConstants.LEFT);
		agentParamPanel.add(lblStartingEnergy_1);

		comboBoxPreyStartingEnergy = new JComboBox();
		comboBoxPreyStartingEnergy.setToolTipText("The starting energy level for Prey.");
		comboBoxPreyStartingEnergy.setModel(new DefaultComboBoxModel(new String[]
		{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "90", "91", "92", "93", "94", "95", "96", "97", "98", "99"}));
		agentParamPanel.add(comboBoxPreyStartingEnergy);
		comboBoxPreyStartingEnergy.setSelectedIndex(prey_default_startingenergy_selected);

		comboBoxPredStartingEnergy = new JComboBox();
		comboBoxPredStartingEnergy.setToolTipText("The starting energy level for Predators.");
		comboBoxPredStartingEnergy.setModel(new DefaultComboBoxModel(new String[]
		{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "90", "91", "92", "93", "94", "95", "96", "97", "98", "99"}));
		agentParamPanel.add(comboBoxPredStartingEnergy);
		comboBoxPredStartingEnergy.setSelectedIndex(predator_default_startingenergy_selected);

		plantParamPanel = new JPanel();
		plantParamPanel.setBorder(new TitledBorder(null, "Plant Parameters", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_plantParamPanel = new GridBagConstraints();
		gbc_plantParamPanel.gridwidth = 2;
		gbc_plantParamPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_plantParamPanel.anchor = GridBagConstraints.NORTH;
		gbc_plantParamPanel.insets = new Insets(0, 0, 5, 0);
		gbc_plantParamPanel.gridx = 0;
		gbc_plantParamPanel.gridy = 2;
		controlPanelTop.add(plantParamPanel, gbc_plantParamPanel);
		plantParamPanel.setLayout(new GridLayout(0, 4, 5, 5));

		JLabel lblPlant_regen_rate = new JLabel("Plant Regen Rate");
		lblPlant_regen_rate.setHorizontalAlignment(SwingConstants.LEFT);
		plantParamPanel.add(lblPlant_regen_rate);

		comboBoxPlantRegenRate = new JComboBox();
		comboBoxPlantRegenRate.setToolTipText("The number of new Plants that appear each simulation step.");
		comboBoxPlantRegenRate.setModel(new DefaultComboBoxModel(new String[]
		{"1", "2", "3", "4", "5", "6", "7", "8", "16", "32", "64", "128", "256", "512", "1024", "2048", "4096", "8192"}));
		plantParamPanel.add(comboBoxPlantRegenRate);
		comboBoxPlantRegenRate.setSelectedIndex(plant_default_regenrate_selected);

		JLabel lblEnergyAbso = new JLabel("Energy Ab Rate");
		lblEnergyAbso.setHorizontalAlignment(SwingConstants.LEFT);
		plantParamPanel.add(lblEnergyAbso);

		comboBoxEnergyAbsorptionRate = new JComboBox();
		comboBoxEnergyAbsorptionRate.setToolTipText("The rate at which Plants absorb energy from the Sun.");
		comboBoxEnergyAbsorptionRate.setModel(new DefaultComboBoxModel(new String[]
		{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "90", "91", "92", "93", "94", "95", "96", "97", "98", "99"}));
		plantParamPanel.add(comboBoxEnergyAbsorptionRate);
		comboBoxEnergyAbsorptionRate.setSelectedIndex(plant_default_energyabsorptionrate_selected);

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
		comboBoxPlantStartingEnergy.setSelectedIndex(plant_starting_energy_selected);

		JLabel lblRepoCost = new JLabel("Repo Cost");
		lblRepoCost.setHorizontalAlignment(SwingConstants.LEFT);
		plantParamPanel.add(lblRepoCost);

		comboBoxPlantRepoCost = new JComboBox();
		comboBoxPlantRepoCost.setToolTipText("The cost of reproduction for Plants. ( Disabled)");
		comboBoxPlantRepoCost.setEnabled(false);
		comboBoxPlantRepoCost.setModel(new DefaultComboBoxModel(new String[]
		{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "90", "91", "92", "93", "94", "95", "96", "97", "98", "99"}));
		plantParamPanel.add(comboBoxPlantRepoCost);
		comboBoxPlantRepoCost.setSelectedIndex(plant_default_plantrepocost_selected);

		JPanel controlPanelBottom = new JPanel();
		controlPanel.add(controlPanelBottom, BorderLayout.SOUTH);
		controlPanelBottom.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Control", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		controlPanelBottom.setLayout(new GridLayout(2, 1, 5, 5));

		JPanel row1 = new JPanel();
		controlPanelBottom.add(row1);
		row1.setLayout(new GridLayout(0, 3, 10, 5));

		JLabel lblSimRate = new JLabel("Requested Step Rate");
		row1.add(lblSimRate);
		lblSimRate.setHorizontalAlignment(SwingConstants.CENTER);
		
		simRateInfoPanel = new JPanel();
		simRateInfoPanel.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		row1.add(simRateInfoPanel);
		simRateInfoPanel.setLayout(new BorderLayout(0, 0));
		txtSimRateInfo = new JTextField()
		{
		    @Override public void setBorder(Border border) 
		    {
		    	//Override the border setting of this text field to do nothing.
		    }
		};
		txtSimRateInfo.setToolTipText("Requested step rate.");

		simRateInfoPanel.add(txtSimRateInfo, BorderLayout.CENTER);
		txtSimRateInfo.setHorizontalAlignment(SwingConstants.CENTER);
		txtSimRateInfo.setEditable(false);
		txtSimRateInfo.setText("15");
		txtSimRateInfo.setColumns(10);
		simRateSlider = new JSlider();
		simRateSlider.setToolTipText("Adjust requested step rate.");
		simRateSlider.setMinimum(15);
		simRateSlider.setMaximum(300);
		simRateSlider.setValue(15);
		simRateSlider.setSnapToTicks(true);
		row1.add(simRateSlider);
		simRateSlider.setPaintTrack(false);
		simRateSlider.setPaintTicks(true);
		simRateSlider.setMinorTickSpacing(15);
		simRateSlider.setMajorTickSpacing(30);
		simRateSlider.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{

				if (simRateSlider.getValue() == 0)
				{
					txtSimRateInfo.setText("1");
				}
				else
				{
					txtSimRateInfo.setText(Integer.toString(simRateSlider.getValue()));
				}

				sim.reqSimUpdateRate(simRateSlider.getValue());

			}
		});

		JPanel row2 = new JPanel();
		controlPanelBottom.add(row2);
		row2.setLayout(new GridLayout(0, 3, 10, 5));

		btnGenerate = new JButton("Generate");
		btnGenerate.setToolTipText("Generate a new simuation based on the values of the parameters.");
		btnGenerate.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				newSim();
			}
		});
		row2.add(btnGenerate);

		btnStart = new JButton("Start");
		btnStart.setToolTipText("Start the simulation.");
		row2.add(btnStart);

		btnPause = new JButton("Pause");
		btnPause.setToolTipText("Pause / Unpause the simulation.");
		row2.add(btnPause);

		statsPanel = new StatsPanel();

		controlPanel.add(statsPanel, BorderLayout.CENTER);
		btnPause.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
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
		btnStart.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				simStartedState();
			}
		});

		JMenuBar menuBar = new JMenuBar();
		gui.setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		JMenuItem mntmQuit = new JMenuItem("Quit");
		mntmQuit.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				doSimExit();
			}
		});
		mnFile.add(mntmQuit);
		
		mnParameters = new JMenu("Parameters");
		menuBar.add(mnParameters);
		
		mntmUnlock = new JMenuItem("Unlock");
		mntmUnlock.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				/* do the parameter unlocking sequence */
				parametersUnlock();
			}
		});
		mnParameters.add(mntmUnlock);

		JMenu mnOptions = new JMenu("Options");
		menuBar.add(mnOptions);

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
		mnOptions.add(chckbxmntmDisplayView);

		JMenu mnAgentDrawing = new JMenu("Agent Drawing");
		mnOptions.add(mnAgentDrawing);

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

		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);

		JMenuItem mntmAbout = new JMenuItem("About");
		mnHelp.add(mntmAbout);

		gui.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				doSimExit();
			}

			public void windowIconified(WindowEvent e)
			{
				SimulationView.minimise();
			}

			public void windowDeiconified(WindowEvent e)
			{
				SimulationView.maximise();
			}

		});

		gui.setVisible(true);
		//gui.setAlwaysOnTop(true);

		startUpState();

	}

	private static void doSimExit()
	{

		String message;
		message = "Do you want to quit?";

		JOptionPane pane = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);

		JDialog dialog = pane.createDialog(null, "Close Application");

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

	private static void lookandFeel()
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (ClassNotFoundException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		catch (InstantiationException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		catch (IllegalAccessException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		catch (UnsupportedLookAndFeelException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
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

		/* Locks the paramters */
		parametersLock();		
		
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

		//StatsPanel.setPaused(true);

		sim.pauseSim();
	}

	private static void simUnPausedState()
	{
		btnPause.setText("Pause");
		btnGenerate.setEnabled(false);

		StatsPanel.setPaused(false);

		sim.unPauseSim();
	}

	public static void parametersLock()
	{
		/* Main Setup panel */
		comboBoxPreyNumbers.setEnabled(false);
		comboBoxPredNumbers.setEnabled(false);
		comboBoxWorldSize.setEnabled(false);
		comboBoxPlantNumbers.setEnabled(false);
		
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
		comboBoxPlantRepoCost.setEnabled(false);
		comboBoxEnergyAbsorptionRate.setEnabled(false);

	}
	
	public static void parametersUnlock()
	{
		
		String message;
		message = "Unlocking paramters will end this simulation!\nDo wish to unlock the parameters?";

		JOptionPane pane = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);

		JDialog dialog = pane.createDialog(null, "Unlock Parameters");

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
			
			/* comboBoxPredatorConsumptionRate is disabled due as feature not implemented */
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
			if(!sim.simPaused())
			{
				/* Pause the sim */
				simPausedState();
				
				/* Disable resume */
				btnPause.setEnabled(false);				
			}
			
			/* Clear the old stats */
			StatsPanel.clearStats();

		}	
	}
	
	public static void maximise()
	{
		gui.setState(Frame.NORMAL);
	}

	public static void minimise()
	{
		gui.setState(Frame.ICONIFIED);
	}
}
