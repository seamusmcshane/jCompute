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
		//gui.setBounds(control_gui_x, control_gui_y, 350, 600);

		// For distribution
		gui.setBounds(control_gui_x, control_gui_y, control_gui_width, control_gui_height);

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
		controlPanelTop.setLayout(new BorderLayout(0, 0));
		
		JPanel mainSetupPanel = new JPanel();
		mainSetupPanel.setBorder(new TitledBorder(null, "Simulation Setup", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		controlPanelTop.add(mainSetupPanel, BorderLayout.NORTH);
		mainSetupPanel.setLayout(new GridLayout(2, 4, 0, 0));

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
		controlPanelTop.add(agentParamPanel, BorderLayout.CENTER);
		agentParamPanel.setLayout(new GridLayout(0, 3, 0, 0));
		
		JLabel lbl_spacer = new JLabel("");
		agentParamPanel.add(lbl_spacer);
		
		JLabel lblPrey = new JLabel("Prey");
		lblPrey.setHorizontalAlignment(SwingConstants.CENTER);
		agentParamPanel.add(lblPrey);
		
		JLabel lblPredator = new JLabel("Predator");
		lblPredator.setHorizontalAlignment(SwingConstants.CENTER);
		agentParamPanel.add(lblPredator);
		
		JLabel lblViewRange = new JLabel("View Range");
		lblViewRange.setHorizontalAlignment(SwingConstants.CENTER);
		agentParamPanel.add(lblViewRange);
		
		JComboBox comboBox = new JComboBox();
		agentParamPanel.add(comboBox);
		
		JComboBox comboBox_1 = new JComboBox();
		agentParamPanel.add(comboBox_1);
		
		JLabel lblDigestiveEfficn = new JLabel("Digestive Efficiency");
		lblDigestiveEfficn.setHorizontalAlignment(SwingConstants.CENTER);
		agentParamPanel.add(lblDigestiveEfficn);
		
		JComboBox comboBox_2 = new JComboBox();
		agentParamPanel.add(comboBox_2);
		
		JComboBox comboBox_3 = new JComboBox();
		agentParamPanel.add(comboBox_3);
		
		JLabel lblReproductionDiv = new JLabel("Energy Division");
		lblReproductionDiv.setHorizontalAlignment(SwingConstants.CENTER);
		agentParamPanel.add(lblReproductionDiv);
		
		JSlider slider = new JSlider();
		agentParamPanel.add(slider);
		
		JSlider slider_1 = new JSlider();
		agentParamPanel.add(slider_1);
		
		JLabel lblMovementCost = new JLabel("Movement Cost");
		lblMovementCost.setHorizontalAlignment(SwingConstants.CENTER);
		agentParamPanel.add(lblMovementCost);
		
		JSlider slider_2 = new JSlider();
		agentParamPanel.add(slider_2);
		
		JSlider slider_3 = new JSlider();
		agentParamPanel.add(slider_3);
		
		JPanel plantParamPanel = new JPanel();
		controlPanelTop.add(plantParamPanel, BorderLayout.SOUTH);
		plantParamPanel.setLayout(new GridLayout(0, 2, 0, 0));
		
		JLabel lblStartingEnergy = new JLabel("Starting Energy");
		lblStartingEnergy.setHorizontalAlignment(SwingConstants.CENTER);
		plantParamPanel.add(lblStartingEnergy);
		
		JSlider slider_4 = new JSlider();
		plantParamPanel.add(slider_4);
		
		JLabel lblReproductionCost = new JLabel("Reproduction Cost");
		lblReproductionCost.setHorizontalAlignment(SwingConstants.CENTER);
		plantParamPanel.add(lblReproductionCost);
		
		JSlider slider_5 = new JSlider();
		plantParamPanel.add(slider_5);

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
		sim.newSim(statsPanel, Integer.parseInt(comboBoxWorldSize.getSelectedItem().toString()), Integer.parseInt(comboBoxPreyNumbers.getSelectedItem().toString()), Integer.parseInt(comboBoxPredNumbers.getSelectedItem().toString()), Integer.parseInt(comboBoxPlantNumbers.getSelectedItem().toString()));

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
