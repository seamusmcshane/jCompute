package alife;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
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

import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import javax.swing.border.TitledBorder;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

public class SimulationGUI
{
	private static JFrame gui;

	/** Gui Frame Items */
	private static JSlider simRateSlider;
	private static JButton btnNew;
	private static JButton btnPause;
	private static JButton btnStart;
	private static JTextField txtSimRateInfo;
	private static JComboBox comboBoxPreyNumbers;
	private static JComboBox comboBoxPredNumbers;
	private static JComboBox comboBoxWorldSize;
	private static JComboBox comboBoxPlantNumbers;
	private static JComboBox comboBoxPlantRegenRate;
	
	private static JComboBox comboBoxEnergyAbsorptionRate;
	private static JComboBox comboBoxPlantStartingEnergy;
	
	
	private static JComboBox comboBoxPreySpeed;
	private static int prey_default_speed_selected = 89; // Selects 0.90
	
	private static JComboBox comboBoxPredatorSpeed;
	private static int predator_default_speed_selected = 99; // Selects 1.00	

	private static JComboBox comboBoxPreyViewRange;
	private static int prey_default_view_range_selected = 24; // Selects 25
	
	private static JComboBox comboBoxPredatorViewRange;
	private static int predator_default_view_range_selected = 24; // Selects 25
	
	private static JComboBox comboBoxPreyDE;
	private static JComboBox comboBoxPredatorDE;
	
	private static JComboBox comboBoxPreyREDiv;
	private static JComboBox comboBoxPredatorREDiv;
	
	private static JComboBox comboBoxPreyMoveCost;
	private static JComboBox comboBoxPredatorMoveCost;
	
	private static JComboBox comboBoxPreyHungerThres;
	private static JComboBox comboBoxPredatorHungerThres;
	
	private static JComboBox comboBoxConsumptionRatePrey;
	
	private static JComboBox comboBoxPreyRepoCost;
	private static JComboBox comboBoxPredRepoCost;
	
	private static JComboBox comboBoxPreyStartingEnergy;
	private static JComboBox comboBoxPredStartingEnergy;	
	
	private static StatsPanel statsPanel;

	/* static in screen sizes */
	static int screen_width;
	static int screen_height;

	/** Window Size */
	static int pad = 10;

	static int total_height = 0;
	static int total_width = 0;

	static int control_gui_width = 350; // Hard-Coded as GUI needs certain width needs based on button text size

	static int control_gui_height = 0;
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
		
		/*World Size */
		int word_size = Integer.parseInt(comboBoxWorldSize.getSelectedItem().toString());
		 
		/* Prey Numbers*/
		int prey_no = Integer.parseInt(comboBoxPreyNumbers.getSelectedItem().toString());
		
		 /* Pred Numbers */
		int pred_no = Integer.parseInt(comboBoxPredNumbers.getSelectedItem().toString());
		
		 /* Plant Numbers */
		int plant_no = Integer.parseInt(comboBoxPlantNumbers.getSelectedItem().toString());
		
		/*
		 * Plants via direct variable passing
		 */
		
		 /* Plant Regeneration Rate */
		int plant_regen_rate =  Integer.parseInt(comboBoxPlantRegenRate.getSelectedItem().toString());
		
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
		
		/* Digestive Efficiency */
		agentSettings.setPreyDE(1);
		agentSettings.setPredatorDE(1);
		
		/* Reproduction Energy Divider */
		agentSettings.setPreyREDiv(1);
		agentSettings.setPredatorDE(1);
		
		/* Energy Movement Cost */
		agentSettings.setPreyMoveCost(1);
		agentSettings.setPredatorMoveCost(1);
		
		/* Hunger Threshold */
		agentSettings.setPreyHungerThres(1);
		agentSettings.setPredatorHungerThres(1);
		
		/* Energy Consumption Rate */
		agentSettings.setPreyConsumptionRate(1);
		agentSettings.setPredatorConsumptionRate(1); // Not Used
		
		/* Reproduction Cost */
		agentSettings.setPreyRepoCost(1);
		agentSettings.setPredRepoCost(1);
		
		/* Starting Energy */
		agentSettings.setPreyStartingEnergy(1);
		agentSettings.setPredStartingEnergy(1);
		
		sim.newSim(statsPanel,word_size ,prey_no,pred_no,plant_no,plant_regen_rate,plant_starting_energy,plant_energy_absorption_rate,agentSettings);
		
		
		
		// comboBoxPreySpeed
		// comboBoxPredSpeed
		
		/*
		 * If needed the GC can free old objects now, before the simulation
		 * starts
		 */
		System.gc();

		btnNew.setEnabled(true);

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
		total_height = (int) (screen_height / 1.5f);

		control_gui_height = screen_height - 48;

		view_width = screen_width - control_gui_width - (pad*2);
		view_height = screen_height - 48;

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

	private static void setUpFrame()
	{

		lookandFeel();

		gui = new JFrame();
		gui.setResizable(false);
		gui.setTitle("Alife Simulation");

		// for editing
		gui.setBounds(control_gui_x, control_gui_y, control_gui_width, control_gui_height);

		// For distribution
		//gui.setBounds(control_gui_x, control_gui_y, 457, 1032);

		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
		gbl_controlPanelTop.columnWidths = new int[]{0};
		gbl_controlPanelTop.rowHeights = new int[]{0, 0, 0};
		gbl_controlPanelTop.columnWeights = new double[]{1.0, 1.0};
		gbl_controlPanelTop.rowWeights = new double[]{Double.MIN_VALUE};
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
						mainSetupPanel.add(comboBoxPredNumbers);
						comboBoxPredNumbers.setModel(new DefaultComboBoxModel(new String[]
						{"0", "1", "10", "100", "200", "400", "800", "1600", "3200", "6400", "12800", "25600", "51200", "102400"}));
						comboBoxPredNumbers.setSelectedIndex(0);	
						
								JLabel lblPreyS = new JLabel("Prey");
								lblPreyS.setHorizontalAlignment(SwingConstants.LEFT);
								mainSetupPanel.add(lblPreyS);
								
										comboBoxPreyNumbers = new JComboBox();
										mainSetupPanel.add(comboBoxPreyNumbers);
										comboBoxPreyNumbers.setModel(new DefaultComboBoxModel(new String[]
										{"0", "1", "10", "100", "200", "400", "800", "1600", "3200", "6400", "12800", "25600", "51200", "102400"}));
										comboBoxPreyNumbers.setSelectedIndex(0);
										
												JLabel lblPlants = new JLabel("Plants");
												lblPlants.setHorizontalAlignment(SwingConstants.LEFT);
												mainSetupPanel.add(lblPlants);
												
														comboBoxPlantNumbers = new JComboBox();
														mainSetupPanel.add(comboBoxPlantNumbers);
														comboBoxPlantNumbers.setModel(new DefaultComboBoxModel(new String[]
														{"0", "1", "10", "100", "200", "400", "800", "1600", "3200", "6400", "12800", "25600", "51200", "102400"}));
														comboBoxPlantNumbers.setSelectedIndex(1);
														
																JLabel lblWorldSize = new JLabel("World Size");
																lblWorldSize.setHorizontalAlignment(SwingConstants.LEFT);
																mainSetupPanel.add(lblWorldSize);
																
																		comboBoxWorldSize = new JComboBox();
																		mainSetupPanel.add(comboBoxWorldSize);
																		comboBoxWorldSize.setModel(new DefaultComboBoxModel(new String[]
																		{"512", "1024", "2048", "4096", "8192", "16384", "32768"}));
																		comboBoxWorldSize.setSelectedIndex(0);
		
		JPanel agentParamPanel = new JPanel();
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
		comboBoxPreySpeed.setModel(new DefaultComboBoxModel(new String[] {"0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9", "0.10", "0.11", "0.12", "0.13", "0.14", "0.15", "0.16", "0.17", "0.18", "0.19", "0.20", "0.21", "0.22", "0.23", "0.24", "0.25", "0.26", "0.27", "0.28", "0.29", "0.30", "0.31", "0.32", "0.33", "0.34", "0.35", "0.36", "0.37", "0.38", "0.39", "0.40", "0.41", "0.42", "0.43", "0.44", "0.45", "0.46", "0.47", "0.48", "0.49", "0.50", "0.51", "0.52", "0.53", "0.54", "0.55", "0.56", "0.57", "0.58", "0.59", "0.60", "0.61", "0.62", "0.63", "0.64", "0.65", "0.66", "0.67", "0.68", "0.69", "0.70", "0.71", "0.72", "0.73", "0.74", "0.75", "0.76", "0.77", "0.78", "0.79", "0.80", "0.81", "0.82", "0.83", "0.84", "0.85", "0.86", "0.87", "0.88", "0.89", "0.90", "0.91", "0.92", "0.93", "0.94", "0.95", "0.96", "0.97", "0.98", "0.99", "1"}));
		agentParamPanel.add(comboBoxPreySpeed);
		comboBoxPreySpeed.setSelectedIndex(prey_default_speed_selected);
				
		comboBoxPredatorSpeed = new JComboBox();
		comboBoxPredatorSpeed.setModel(new DefaultComboBoxModel(new String[] {"0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9", "0.10", "0.11", "0.12", "0.13", "0.14", "0.15", "0.16", "0.17", "0.18", "0.19", "0.20", "0.21", "0.22", "0.23", "0.24", "0.25", "0.26", "0.27", "0.28", "0.29", "0.30", "0.31", "0.32", "0.33", "0.34", "0.35", "0.36", "0.37", "0.38", "0.39", "0.40", "0.41", "0.42", "0.43", "0.44", "0.45", "0.46", "0.47", "0.48", "0.49", "0.50", "0.51", "0.52", "0.53", "0.54", "0.55", "0.56", "0.57", "0.58", "0.59", "0.60", "0.61", "0.62", "0.63", "0.64", "0.65", "0.66", "0.67", "0.68", "0.69", "0.70", "0.71", "0.72", "0.73", "0.74", "0.75", "0.76", "0.77", "0.78", "0.79", "0.80", "0.81", "0.82", "0.83", "0.84", "0.85", "0.86", "0.87", "0.88", "0.89", "0.90", "0.91", "0.92", "0.93", "0.94", "0.95", "0.96", "0.97", "0.98", "0.99", "1"}));
		agentParamPanel.add(comboBoxPredatorSpeed);
		comboBoxPredatorSpeed.setSelectedIndex(predator_default_speed_selected);
		
		JLabel lblViewRange = new JLabel("View Range");
		lblViewRange.setHorizontalAlignment(SwingConstants.LEFT);
		agentParamPanel.add(lblViewRange);
		
		comboBoxPreyViewRange = new JComboBox();
		comboBoxPreyViewRange.setModel(new DefaultComboBoxModel(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "90", "91", "92", "93", "94", "95", "96", "97", "98", "99", "100"}));
		agentParamPanel.add(comboBoxPreyViewRange);
		comboBoxPreyViewRange.setSelectedIndex(prey_default_view_range_selected);		
		
		comboBoxPredatorViewRange = new JComboBox();
		comboBoxPredatorViewRange.setModel(new DefaultComboBoxModel(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "90", "91", "92", "93", "94", "95", "96", "97", "98", "99", "100"}));
		agentParamPanel.add(comboBoxPredatorViewRange);
		comboBoxPredatorViewRange.setSelectedIndex(predator_default_view_range_selected);		
		
		
		JLabel lblDigestiveEfficn = new JLabel("Digestive Efficiency");
		lblDigestiveEfficn.setHorizontalAlignment(SwingConstants.LEFT);
		agentParamPanel.add(lblDigestiveEfficn);
		
		comboBoxPreyDE = new JComboBox();
		comboBoxPreyDE.setModel(new DefaultComboBoxModel(new String[] {"10", "20", "30", "40", "50", "60", "70", "80", "90", "100"}));
		agentParamPanel.add(comboBoxPreyDE);
		
		comboBoxPredatorDE = new JComboBox();
		comboBoxPredatorDE.setModel(new DefaultComboBoxModel(new String[] {"10", "20", "30", "40", "50", "60", "70", "80", "90", "100"}));
		agentParamPanel.add(comboBoxPredatorDE);
		
		JLabel lblReproductionDiv = new JLabel("R/S Energy Div");
		lblReproductionDiv.setHorizontalAlignment(SwingConstants.LEFT);
		agentParamPanel.add(lblReproductionDiv);
		
		comboBoxPreyREDiv = new JComboBox();
		comboBoxPreyREDiv.setModel(new DefaultComboBoxModel(new String[] {"0.25", "0.50", "0.75", "1.00"}));
		agentParamPanel.add(comboBoxPreyREDiv);
		
		comboBoxPredatorREDiv = new JComboBox();
		comboBoxPredatorREDiv.setModel(new DefaultComboBoxModel(new String[] {"0.25", "0.50", "0.75", "1.00"}));
		agentParamPanel.add(comboBoxPredatorREDiv);
		
		JLabel lblMovementCost = new JLabel("Movement Cost");
		lblMovementCost.setHorizontalAlignment(SwingConstants.LEFT);
		agentParamPanel.add(lblMovementCost);
		
		comboBoxPreyMoveCost = new JComboBox();
		comboBoxPreyMoveCost.setModel(new DefaultComboBoxModel(new String[] {"0.01", "0.02", "0.03", "0.04", "0.05", "0.06", "0.07", "0.08", "0.09", "0.010", "0.011", "0.012", "0.013", "0.014", "0.015", "0.016", "0.017", "0.018", "0.019", "0.020", "0.021", "0.022", "0.023", "0.024", "0.025", "0.026", "0.027", "0.028", "0.029", "0.030", "0.031", "0.032", "0.033", "0.034", "0.035", "0.036", "0.037", "0.038", "0.039", "0.040", "0.041", "0.042", "0.043", "0.044", "0.045", "0.046", "0.047", "0.048", "0.049", "0.050", "0.051", "0.052", "0.053", "0.054", "0.055", "0.056", "0.057", "0.058", "0.059", "0.060", "0.061", "0.062", "0.063", "0.064", "0.065", "0.066", "0.067", "0.068", "0.069", "0.070", "0.071", "0.072", "0.073", "0.074", "0.075", "0.076", "0.077", "0.078", "0.079", "0.080", "0.081", "0.082", "0.083", "0.084", "0.085", "0.086", "0.087", "0.088", "0.089", "0.090", "0.091", "0.092", "0.093", "0.094", "0.095", "0.096", "0.097", "0.098", "0.099"}));
		agentParamPanel.add(comboBoxPreyMoveCost);
		
		comboBoxPredatorMoveCost = new JComboBox();
		comboBoxPredatorMoveCost.setModel(new DefaultComboBoxModel(new String[] {"0.01", "0.02", "0.03", "0.04", "0.05", "0.06", "0.07", "0.08", "0.09", "0.010", "0.011", "0.012", "0.013", "0.014", "0.015", "0.016", "0.017", "0.018", "0.019", "0.020", "0.021", "0.022", "0.023", "0.024", "0.025", "0.026", "0.027", "0.028", "0.029", "0.030", "0.031", "0.032", "0.033", "0.034", "0.035", "0.036", "0.037", "0.038", "0.039", "0.040", "0.041", "0.042", "0.043", "0.044", "0.045", "0.046", "0.047", "0.048", "0.049", "0.050", "0.051", "0.052", "0.053", "0.054", "0.055", "0.056", "0.057", "0.058", "0.059", "0.060", "0.061", "0.062", "0.063", "0.064", "0.065", "0.066", "0.067", "0.068", "0.069", "0.070", "0.071", "0.072", "0.073", "0.074", "0.075", "0.076", "0.077", "0.078", "0.079", "0.080", "0.081", "0.082", "0.083", "0.084", "0.085", "0.086", "0.087", "0.088", "0.089", "0.090", "0.091", "0.092", "0.093", "0.094", "0.095", "0.096", "0.097", "0.098", "0.099"}));
		agentParamPanel.add(comboBoxPredatorMoveCost);
		
		JLabel lblHungerThreshold = new JLabel("Hunger Threshold");
		lblHungerThreshold.setHorizontalAlignment(SwingConstants.LEFT);
		agentParamPanel.add(lblHungerThreshold);
		
		comboBoxPreyHungerThres = new JComboBox();
		comboBoxPreyHungerThres.setModel(new DefaultComboBoxModel(new String[] {"0.25", "0.50", "0.75", "1.00"}));
		agentParamPanel.add(comboBoxPreyHungerThres);
		
		comboBoxPredatorHungerThres = new JComboBox();
		comboBoxPredatorHungerThres.setModel(new DefaultComboBoxModel(new String[] {"0.25", "0.50", "0.75", "1.00"}));
		agentParamPanel.add(comboBoxPredatorHungerThres);
		
		JLabel lblConsumptionRate = new JLabel("E Consumption Rate");
		lblConsumptionRate.setHorizontalAlignment(SwingConstants.LEFT);
		agentParamPanel.add(lblConsumptionRate);
		
		comboBoxConsumptionRatePrey = new JComboBox();
		comboBoxConsumptionRatePrey.setModel(new DefaultComboBoxModel(new String[] {"10", "20", "30", "40", "50", "60", "70", "80", "90", "100"}));
		agentParamPanel.add(comboBoxConsumptionRatePrey);
		
		JLabel lblConsumptionRatePred = new JLabel("100%");
		lblConsumptionRatePred.setHorizontalAlignment(SwingConstants.CENTER);
		agentParamPanel.add(lblConsumptionRatePred);
		
		JLabel lblReproductionCost = new JLabel("Reproduction Cost");
		lblReproductionCost.setHorizontalAlignment(SwingConstants.LEFT);
		agentParamPanel.add(lblReproductionCost);
		
		comboBoxPreyRepoCost = new JComboBox();
		comboBoxPreyRepoCost.setModel(new DefaultComboBoxModel(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "90", "91", "92", "93", "94", "95", "96", "97", "98", "99"}));
		agentParamPanel.add(comboBoxPreyRepoCost);
		
		comboBoxPredRepoCost = new JComboBox();
		comboBoxPredRepoCost.setModel(new DefaultComboBoxModel(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "90", "91", "92", "93", "94", "95", "96", "97", "98", "99"}));
		agentParamPanel.add(comboBoxPredRepoCost);
		
		JLabel lblStartingEnergy_1 = new JLabel("Starting Energy");
		lblStartingEnergy_1.setHorizontalAlignment(SwingConstants.LEFT);
		agentParamPanel.add(lblStartingEnergy_1);
		
		comboBoxPreyStartingEnergy = new JComboBox();
		comboBoxPreyStartingEnergy.setModel(new DefaultComboBoxModel(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "90", "91", "92", "93", "94", "95", "96", "97", "98", "99"}));
		agentParamPanel.add(comboBoxPreyStartingEnergy);
		
		comboBoxPredStartingEnergy = new JComboBox();
		comboBoxPredStartingEnergy.setModel(new DefaultComboBoxModel(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "90", "91", "92", "93", "94", "95", "96", "97", "98", "99"}));
		agentParamPanel.add(comboBoxPredStartingEnergy);
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Plant Parameters", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridwidth = 2;
		gbc_panel.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel.anchor = GridBagConstraints.NORTH;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 2;
		controlPanelTop.add(panel, gbc_panel);
		panel.setLayout(new GridLayout(0, 4, 5, 5));
		
		JLabel lblPlant_regen_rate = new JLabel("Plant Regen Rate");
		lblPlant_regen_rate.setHorizontalAlignment(SwingConstants.LEFT);
		panel.add(lblPlant_regen_rate);
		
		comboBoxPlantRegenRate = new JComboBox();
		comboBoxPlantRegenRate.setModel(new DefaultComboBoxModel(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "16", "32", "64", "128", "256", "512", "1024", "2048", "4096", "8192"}));
		panel.add(comboBoxPlantRegenRate);
		
		JLabel lblEnergyAbso = new JLabel("Energy Ab Rate");
		lblEnergyAbso.setHorizontalAlignment(SwingConstants.LEFT);
		panel.add(lblEnergyAbso);
		
		comboBoxEnergyAbsorptionRate = new JComboBox();
		comboBoxEnergyAbsorptionRate.setEnabled(false);
		comboBoxEnergyAbsorptionRate.setModel(new DefaultComboBoxModel(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "90", "91", "92", "93", "94", "95", "96", "97", "98", "99"}));
		panel.add(comboBoxEnergyAbsorptionRate);
		
		JLabel lblREnergyDiv = new JLabel("R/S Energy Div");
		lblREnergyDiv.setHorizontalAlignment(SwingConstants.LEFT);
		panel.add(lblREnergyDiv);
		
		JComboBox comboBoxPlantRedDiv = new JComboBox();
		comboBoxPlantRedDiv.setEnabled(false);
		comboBoxPlantRedDiv.setModel(new DefaultComboBoxModel(new String[] {"0.25", "0.50", "0.75", "1.00"}));
		panel.add(comboBoxPlantRedDiv);
		
		JLabel lblPlantStartingEnergy = new JLabel("Starting Energy");
		lblPlantStartingEnergy.setHorizontalAlignment(SwingConstants.LEFT);
		panel.add(lblPlantStartingEnergy);
		
		comboBoxPlantStartingEnergy = new JComboBox();
		comboBoxPlantStartingEnergy.setModel(new DefaultComboBoxModel(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "90", "91", "92", "93", "94", "95", "96", "97", "98", "99"}));
		panel.add(comboBoxPlantStartingEnergy);
		
		JLabel lblRepoCost = new JLabel("Repo Cost");
		lblRepoCost.setHorizontalAlignment(SwingConstants.LEFT);
		panel.add(lblRepoCost);
		
		JComboBox comboBoxPlantRepoCost = new JComboBox();
		comboBoxPlantRepoCost.setEnabled(false);
		comboBoxPlantRepoCost.setModel(new DefaultComboBoxModel(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "90", "91", "92", "93", "94", "95", "96", "97", "98", "99"}));
		panel.add(comboBoxPlantRepoCost);

		JPanel controlPanelBottom = new JPanel();
		controlPanel.add(controlPanelBottom, BorderLayout.SOUTH);
		controlPanelBottom.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Control", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		controlPanelBottom.setLayout(new GridLayout(2, 1, 5, 5));

		JPanel row1 = new JPanel();
		controlPanelBottom.add(row1);
		row1.setLayout(new GridLayout(0, 3, 10, 5));

		JLabel lblSimRate = new JLabel("Sim Rate");
		row1.add(lblSimRate);
		lblSimRate.setHorizontalAlignment(SwingConstants.CENTER);

		txtSimRateInfo = new JTextField();
		row1.add(txtSimRateInfo);
		txtSimRateInfo.setHorizontalAlignment(SwingConstants.CENTER);
		txtSimRateInfo.setEditable(false);
		txtSimRateInfo.setText("15");
		txtSimRateInfo.setColumns(10);
		simRateSlider = new JSlider();
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

		btnNew = new JButton("New");
		btnNew.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				newSim();
			}
		});
		row2.add(btnNew);

		btnStart = new JButton("Start");
		row2.add(btnStart);

		btnPause = new JButton("Pause");
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

		JMenuItem mntmNew = new JMenuItem("New");
		mnFile.add(mntmNew);

		JMenuItem mntmOpen = new JMenuItem("Open");
		mnFile.add(mntmOpen);

		JMenuItem mntmSave = new JMenuItem("Save");
		mnFile.add(mntmSave);

		JMenuItem mntmSaveAs = new JMenuItem("Save As");
		mnFile.add(mntmSaveAs);

		JMenuItem mntmQuit = new JMenuItem("Quit");
		mnFile.add(mntmQuit);

		JMenu mnOptions = new JMenu("Options");
		menuBar.add(mnOptions);

		JMenuItem mntmSettings = new JMenuItem("Settings");
		mnOptions.add(mntmSettings);

		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);

		JMenuItem mntmAbout = new JMenuItem("About");
		mnHelp.add(mntmAbout);

		gui.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				SimulationView.exitDisplay(); // Tell OpenGL we are done and free the resources used in the canvas. - must be done else sim will lockup.

				System.exit(0);    // Exit the Simulation and let Java free the memory.
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
		gui.setAlwaysOnTop(true);

		startUpState();

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

		btnNew.setEnabled(false);

		btnStart.setEnabled(false);

		btnPause.setEnabled(true);

		simRateSlider.setEnabled(true);

		comboBoxPreyNumbers.setEnabled(false);

		comboBoxPredNumbers.setEnabled(false);

		comboBoxWorldSize.setEnabled(false);

		comboBoxPlantNumbers.setEnabled(false);

		SimulationView.setFocus();

	}

	private static void startUpState()
	{
		comboBoxPreyNumbers.setEnabled(true);
		comboBoxPredNumbers.setEnabled(true);
		comboBoxWorldSize.setEnabled(true);
		comboBoxPlantNumbers.setEnabled(true);

		btnStart.setEnabled(false);
		simRateSlider.setEnabled(false);
		btnPause.setEnabled(false);

	}

	private static void simPausedState()
	{
		btnPause.setText("Resume");
		btnNew.setEnabled(true);

		comboBoxPreyNumbers.setEnabled(true);
		comboBoxPredNumbers.setEnabled(true);
		comboBoxWorldSize.setEnabled(true);
		comboBoxPlantNumbers.setEnabled(true);

		sim.pauseSim();

	}

	private static void simUnPausedState()
	{
		btnPause.setText("Pause");
		btnNew.setEnabled(false);

		comboBoxPreyNumbers.setEnabled(false);
		comboBoxPredNumbers.setEnabled(false);
		comboBoxWorldSize.setEnabled(false);
		comboBoxPlantNumbers.setEnabled(false);

		sim.unPauseSim();
	}

}
