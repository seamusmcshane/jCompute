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
	
	/* Setup */
	private static JComboBox comboBoxPreyNumbers;
	private static JComboBox comboBoxPredNumbers;
	private static JComboBox comboBoxPlantNumbers;
	private static JComboBox comboBoxWorldSize;	
	
	/* Prey */
	private static JComboBox comboBoxPreyViewRange;	
	private static JComboBox comboBoxPreyDE;	
	private static JComboBox comboBoxPreyREDiv;	
	private static JComboBox comboBoxPreySpeed;	
	private static JComboBox comboBoxPreyMoveCost;	
	private static JComboBox comboBoxPreyHungerThres;	
	private static JComboBox comboBoxPreyConsumptionRate;
	private static JComboBox comboBoxPreyReproductionCost;	
	private static JComboBox comboBoxPreyStartingEnergy;
	
	/* Predators */
	private static JComboBox comboBoxPredatorDE;	
	private static JComboBox comboBoxPredatorViewRange;	
	private static JComboBox comboBoxPredatorSpeed;	
	private static JComboBox comboBoxPredatorMoveCost;
	private static JComboBox comboBoxPredatorHungerThres;
	private static JComboBox comboBoxPredatorsReproductionCost;
	private static JComboBox comboBoxPredatorStartingEnergy;
	private static JComboBox comboBoxPredatorREDiv;
	
	/* Plants */
	private static JComboBox comboBoxPlantEnergyAbsorptionRate;	
	private static int default_plant_energy_absorption_rate_index=0; // Selects 1
	
	private static JComboBox comboBoxPlantRegenRate;
	private static int default_plant_regen_rate_index=0; // Selects 0
	
	private static JComboBox comboBoxPlantREnergyDiv;
	private static int default_Plant_REnergy_Div_index=1; // Selects 50:50
	
	
	private static JComboBox ComboBoxStartingPlantEnergy;	
	private static int default_plant_starting_energy_index=49; // Selects 50	
	
	private static JComboBox comboBoxPlantReproductionCost;
	private static int default_plant_reproduction_cost_index=98; // Selects 99
	
	/* Custom Jpanel for stats */
	private static StatsPanel statsPanel;

	/* static in screen sizes */
	static int screen_width;
	static int screen_height;

	/** Window Size */
	static int pad = 10;

	static int total_height = 0;
	static int total_width = 0;

	static int control_gui_width = 400; // Hard-Coded as GUI needs certain width needs based on button text size

	static int control_gui_height = 0;
	static int control_gui_x;
	static int control_gui_y;

	static int view_width = 0;
	static int view_height = 0;
	static int view_x;
	static int view_y;

	/* Simulation Reference */
	private static Simulation sim;

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
		gbl_controlPanelTop.rowHeights = new int[]{63, 143, 0};
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
				lblPredS.setHorizontalAlignment(SwingConstants.CENTER);
				mainSetupPanel.add(lblPredS);
				
						comboBoxPredNumbers = new JComboBox();
						mainSetupPanel.add(comboBoxPredNumbers);
						comboBoxPredNumbers.setModel(new DefaultComboBoxModel(new String[]
						{"0", "1", "10", "100", "200", "400", "800", "1600", "3200", "6400", "12800", "25600", "51200", "102400"}));
						comboBoxPredNumbers.setSelectedIndex(0);
						
								JLabel lblPreyS = new JLabel("Prey");
								lblPreyS.setHorizontalAlignment(SwingConstants.CENTER);
								mainSetupPanel.add(lblPreyS);
								
										comboBoxPreyNumbers = new JComboBox();
										mainSetupPanel.add(comboBoxPreyNumbers);
										comboBoxPreyNumbers.setModel(new DefaultComboBoxModel(new String[]
										{"0", "1", "10", "100", "200", "400", "800", "1600", "3200", "6400", "12800", "25600", "51200", "102400"}));
										comboBoxPreyNumbers.setSelectedIndex(0);
										
												JLabel lblPlants = new JLabel("Plants");
												lblPlants.setHorizontalAlignment(SwingConstants.CENTER);
												mainSetupPanel.add(lblPlants);
												
														comboBoxPlantNumbers = new JComboBox();
														mainSetupPanel.add(comboBoxPlantNumbers);
														comboBoxPlantNumbers.setModel(new DefaultComboBoxModel(new String[]
														{"0", "1", "10", "100", "200", "400", "800", "1600", "3200", "6400", "12800", "25600", "51200", "102400"}));
														comboBoxPlantNumbers.setSelectedIndex(1);
														
																JLabel lblWorldSize = new JLabel("World Size");
																lblWorldSize.setHorizontalAlignment(SwingConstants.CENTER);
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
		lblSpeed.setHorizontalAlignment(SwingConstants.CENTER);
		agentParamPanel.add(lblSpeed);
		
		comboBoxPreySpeed = new JComboBox();
		agentParamPanel.add(comboBoxPreySpeed);
		
		comboBoxPredatorSpeed = new JComboBox();
		agentParamPanel.add(comboBoxPredatorSpeed);
		
		JLabel lblViewRange = new JLabel("View Range");
		lblViewRange.setHorizontalAlignment(SwingConstants.CENTER);
		agentParamPanel.add(lblViewRange);
		
		comboBoxPreyViewRange = new JComboBox();
		agentParamPanel.add(comboBoxPreyViewRange);
		
		comboBoxPredatorViewRange = new JComboBox();
		agentParamPanel.add(comboBoxPredatorViewRange);
		
		JLabel lblDigestiveEfficn = new JLabel("Digestive Efficiency");
		lblDigestiveEfficn.setHorizontalAlignment(SwingConstants.CENTER);
		agentParamPanel.add(lblDigestiveEfficn);
		
		comboBoxPreyDE = new JComboBox();
		agentParamPanel.add(comboBoxPreyDE);
		
		comboBoxPredatorDE = new JComboBox();
		agentParamPanel.add(comboBoxPredatorDE);
		
		JLabel lblReproductionDiv = new JLabel("R Energy Div");
		lblReproductionDiv.setHorizontalAlignment(SwingConstants.CENTER);
		agentParamPanel.add(lblReproductionDiv);
		
		comboBoxPreyREDiv = new JComboBox();
		agentParamPanel.add(comboBoxPreyREDiv);
		
		comboBoxPredatorREDiv = new JComboBox();
		agentParamPanel.add(comboBoxPredatorREDiv);
		
		JLabel lblMovementCost = new JLabel("Movement Cost");
		lblMovementCost.setHorizontalAlignment(SwingConstants.CENTER);
		agentParamPanel.add(lblMovementCost);
		
		comboBoxPreyMoveCost = new JComboBox();
		agentParamPanel.add(comboBoxPreyMoveCost);
		
		comboBoxPredatorMoveCost = new JComboBox();
		agentParamPanel.add(comboBoxPredatorMoveCost);
		
		JLabel lblHungerThreshold = new JLabel("Hunger Threshold");
		lblHungerThreshold.setHorizontalAlignment(SwingConstants.CENTER);
		agentParamPanel.add(lblHungerThreshold);
		
		comboBoxPreyHungerThres = new JComboBox();
		agentParamPanel.add(comboBoxPreyHungerThres);
		
		comboBoxPredatorHungerThres = new JComboBox();
		agentParamPanel.add(comboBoxPredatorHungerThres);
		
		JLabel lblConsumptionRate = new JLabel("Consumption Rate");
		lblConsumptionRate.setHorizontalAlignment(SwingConstants.CENTER);
		agentParamPanel.add(lblConsumptionRate);
		
		comboBoxPreyConsumptionRate = new JComboBox();
		agentParamPanel.add(comboBoxPreyConsumptionRate);
		
		JLabel lblConsumptionRatePred = new JLabel("1x");
		lblConsumptionRatePred.setHorizontalAlignment(SwingConstants.CENTER);
		agentParamPanel.add(lblConsumptionRatePred);
		
		JLabel lblReproductionCost = new JLabel("Reproduction Cost");
		agentParamPanel.add(lblReproductionCost);
		lblReproductionCost.setHorizontalAlignment(SwingConstants.CENTER);
		
		comboBoxPreyReproductionCost = new JComboBox();
		agentParamPanel.add(comboBoxPreyReproductionCost);
		comboBoxPreyReproductionCost.setModel(new DefaultComboBoxModel(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "90", "91", "92", "93", "94", "95", "96", "97", "98", "99"}));
		
		comboBoxPredatorsReproductionCost = new JComboBox();
		agentParamPanel.add(comboBoxPredatorsReproductionCost);
		
		JLabel lblStartingEnergy = new JLabel("Starting Energy");
		agentParamPanel.add(lblStartingEnergy);
		
		comboBoxPreyStartingEnergy = new JComboBox();
		agentParamPanel.add(comboBoxPreyStartingEnergy);
		
		comboBoxPredatorStartingEnergy = new JComboBox();
		agentParamPanel.add(comboBoxPredatorStartingEnergy);
		
		JPanel plantParamPanel = new JPanel();
		plantParamPanel.setBorder(new TitledBorder(null, "Plant Parameters", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagConstraints gbc_plantParamPanel = new GridBagConstraints();
		gbc_plantParamPanel.gridwidth = 2;
		gbc_plantParamPanel.fill = GridBagConstraints.HORIZONTAL;
		gbc_plantParamPanel.anchor = GridBagConstraints.NORTH;
		gbc_plantParamPanel.insets = new Insets(0, 0, 5, 0);
		gbc_plantParamPanel.gridx = 0;
		gbc_plantParamPanel.gridy = 2;
		controlPanelTop.add(plantParamPanel, gbc_plantParamPanel);
		plantParamPanel.setLayout(new GridLayout(0, 2, 5, 5));
		
		JLabel lblPlant_regen_rate = new JLabel("Plant Regen Rate (Plants/Step)");
		lblPlant_regen_rate.setHorizontalAlignment(SwingConstants.CENTER);
		plantParamPanel.add(lblPlant_regen_rate);
		
		comboBoxPlantRegenRate = new JComboBox();
		comboBoxPlantRegenRate.setModel(new DefaultComboBoxModel(new String[] {"0", "2", "4", "8", "16", "32", "64", "128", "256", "512"}));
		plantParamPanel.add(comboBoxPlantRegenRate);
		comboBoxPlantRegenRate.setSelectedIndex(default_plant_regen_rate_index);		
		
		JLabel lblEnergyAbso = new JLabel("Energy Absob Rate");
		lblEnergyAbso.setHorizontalAlignment(SwingConstants.CENTER);
		plantParamPanel.add(lblEnergyAbso);
		
		comboBoxPlantEnergyAbsorptionRate = new JComboBox();
		comboBoxPlantEnergyAbsorptionRate.setModel(new DefaultComboBoxModel(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "90", "91", "92", "93", "94", "95", "96", "97", "98", "99"}));
		plantParamPanel.add(comboBoxPlantEnergyAbsorptionRate);
		comboBoxPlantEnergyAbsorptionRate.setSelectedIndex(default_plant_energy_absorption_rate_index);
		
		JLabel lblplantREnergyDiv = new JLabel("R Energ Div");
		lblplantREnergyDiv.setHorizontalAlignment(SwingConstants.CENTER);
		plantParamPanel.add(lblplantREnergyDiv);
		
		comboBoxPlantREnergyDiv = new JComboBox();
		comboBoxPlantREnergyDiv.setModel(new DefaultComboBoxModel(new String[] {"25:75", "50:50", "25:75"}));
		plantParamPanel.add(comboBoxPlantREnergyDiv);
		comboBoxPlantREnergyDiv.setSelectedIndex(default_Plant_REnergy_Div_index);
		
		JLabel lblStartingPlantEnergy = new JLabel("Starting Energy");
		plantParamPanel.add(lblStartingPlantEnergy);
		lblStartingPlantEnergy.setHorizontalAlignment(SwingConstants.CENTER);
		
		ComboBoxStartingPlantEnergy = new JComboBox();
		plantParamPanel.add(ComboBoxStartingPlantEnergy);
		ComboBoxStartingPlantEnergy.setModel(new DefaultComboBoxModel(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "90", "91", "92", "93", "94", "95", "96", "97", "98", "99", "100"}));
		ComboBoxStartingPlantEnergy.setSelectedIndex(default_plant_starting_energy_index);		
		
		JLabel lblPlantReproductionCost = new JLabel("Reproduction Cost");
		lblPlantReproductionCost.setHorizontalAlignment(SwingConstants.CENTER);
		plantParamPanel.add(lblPlantReproductionCost);
		
		comboBoxPlantReproductionCost = new JComboBox();
		plantParamPanel.add(comboBoxPlantReproductionCost);
		comboBoxPlantReproductionCost.setModel(new DefaultComboBoxModel(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "90", "91", "92", "93", "94", "95", "96", "97", "98", "99"}));
		comboBoxPlantReproductionCost.setSelectedIndex(default_plant_reproduction_cost_index);

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

	private static void newSim()
	{
		
		
		// Artificial Plant boost
		int plant_regen_rate = Integer.parseInt(comboBoxPlantRegenRate.getSelectedItem().toString());
				
		//(float starting_energy, float max_energy, float absorption_rate,String renergy_div, int base_reproduction_cost)
		GenericPlantStats default_plant_stats = new GenericPlantStats(
				Integer.parseInt(ComboBoxStartingPlantEnergy.getSelectedItem().toString()),
				100,/*Max Energy*/ 
				Integer.parseInt(comboBoxPlantEnergyAbsorptionRate.getSelectedItem().toString()),
				comboBoxPlantREnergyDiv.getSelectedItem().toString(), 
				Integer.parseInt(comboBoxPlantReproductionCost.getSelectedItem().toString()));
		
		// Create a new sim 
		sim.newSim(statsPanel, Integer.parseInt(comboBoxWorldSize.getSelectedItem().toString()),
				Integer.parseInt(comboBoxPreyNumbers.getSelectedItem().toString()), 
				Integer.parseInt(comboBoxPredNumbers.getSelectedItem().toString()), 
				Integer.parseInt(comboBoxPlantNumbers.getSelectedItem().toString()),
				default_plant_stats,plant_regen_rate);

		/*
		 * If needed the GC can free old objects now, 
		 * before the simulation starts.
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
