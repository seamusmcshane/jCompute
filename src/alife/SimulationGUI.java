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
import javax.swing.JSeparator;
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

import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;
import javax.swing.JCheckBox;
import javax.swing.border.TitledBorder;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
				
		control_gui_height = total_height;
		
		view_width = (screen_width - control_gui_width) / 2;
		view_height = total_height;

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
		gui.setBounds(control_gui_x, control_gui_y, 350, 600);
		
		// For distribution
		//gui.setBounds(control_gui_x, control_gui_y, control_gui_width, control_gui_height);

		
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui.getContentPane().setLayout(new BorderLayout(0, 0));

		JPanel controlPanel = new JPanel();
		controlPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		// controlPanel.setPreferredSize(new Dimension(controlPanelWidth,
		// controlPanelHeight));

		gui.getContentPane().add(controlPanel, BorderLayout.CENTER);
		controlPanel.setLayout(new BorderLayout(0, 0));

		JPanel controlPanelTop = new JPanel();
		controlPanelTop.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Setup", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		controlPanel.add(controlPanelTop, BorderLayout.NORTH);
		controlPanelTop.setLayout(new GridLayout(0, 4, 5, 5));

		JLabel lblPredS = new JLabel("Predators");
		controlPanelTop.add(lblPredS);

		comboBoxPredNumbers = new JComboBox();
		comboBoxPredNumbers.setModel(new DefaultComboBoxModel(new String[] {"0", "100", "200", "400", "800", "1600", "3200", "6400", "12800", "25600", "51200", "102400"}));
		comboBoxPredNumbers.setSelectedIndex(0);
		controlPanelTop.add(comboBoxPredNumbers);
		
		JLabel lblPreyS = new JLabel("Prey");
		controlPanelTop.add(lblPreyS);
		
		comboBoxPreyNumbers = new JComboBox();
		comboBoxPreyNumbers.setModel(new DefaultComboBoxModel(new String[] {"0", "1", "100", "200", "400", "800", "1600", "3200", "6400", "12800", "25600", "51200", "102400"}));
		comboBoxPreyNumbers.setSelectedIndex(0);
		controlPanelTop.add(comboBoxPreyNumbers);
		
		JLabel lblPlants = new JLabel("Plants");
		controlPanelTop.add(lblPlants);
		
		comboBoxPlantNumbers = new JComboBox();
		comboBoxPlantNumbers.setModel(new DefaultComboBoxModel(new String[] {"0", "100", "200", "400", "800", "1600", "3200", "6400", "12800", "25600", "51200", "102400"}));
		comboBoxPlantNumbers.setSelectedIndex(1);
		controlPanelTop.add(comboBoxPlantNumbers);

		JLabel lblWorldSize = new JLabel("World Size");
		controlPanelTop.add(lblWorldSize);

		comboBoxWorldSize = new JComboBox();
		comboBoxWorldSize.setModel(new DefaultComboBoxModel(new String[] {"512", "1024", "2048", "4096", "8192", "16384", "32768"}));
		comboBoxWorldSize.setSelectedIndex(0);
		controlPanelTop.add(comboBoxWorldSize);

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
		simRateSlider.setValue(15);
		simRateSlider.setSnapToTicks(true);
		row1.add(simRateSlider);
		simRateSlider.setPaintTrack(false);
		simRateSlider.setPaintTicks(true);
		simRateSlider.setMinorTickSpacing(5);
		simRateSlider.setMajorTickSpacing(15);
		simRateSlider.addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent e)
			{
			
				if(simRateSlider.getValue() == 0)
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
		
		JPanel simStatsPanel = new JPanel();
		simStatsPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Stats", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		controlPanel.add(simStatsPanel, BorderLayout.CENTER);
		simStatsPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel statsPanelBottom = new JPanel();
		statsPanelBottom.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Stats", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		simStatsPanel.add(statsPanelBottom, BorderLayout.SOUTH);
		statsPanelBottom.setLayout(new GridLayout(2, 6, 0, 0));
		
		JPanel panel = new JPanel();
		statsPanelBottom.add(panel);
		panel.setLayout(new GridLayout(0, 6, 0, 0));
		
		JLabel lblKeyPlants = new JLabel("Plants");
		panel.add(lblKeyPlants);
		lblKeyPlants.setHorizontalAlignment(SwingConstants.CENTER);
		
		JLabel lblPlantno = new JLabel("0");
		panel.add(lblPlantno);
		lblPlantno.setHorizontalAlignment(SwingConstants.CENTER);
		
		JLabel lblPred = new JLabel("Predators");
		panel.add(lblPred);
		lblPred.setHorizontalAlignment(SwingConstants.CENTER);
		
		JLabel lblPredno = new JLabel("0");
		panel.add(lblPredno);
		lblPredno.setHorizontalAlignment(SwingConstants.CENTER);
		
		JLabel lblPrey = new JLabel("Prey");
		panel.add(lblPrey);
		lblPrey.setHorizontalAlignment(SwingConstants.CENTER);
		
		JLabel lblPreyno = new JLabel("0");
		panel.add(lblPreyno);
		lblPreyno.setHorizontalAlignment(SwingConstants.CENTER);
		
		JPanel panel_1 = new JPanel();
		statsPanelBottom.add(panel_1);
		panel_1.setLayout(new GridLayout(0, 6, 0, 0));
		
		JLabel lblASPS = new JLabel("ASPS");
		lblASPS.setHorizontalAlignment(SwingConstants.CENTER);
		panel_1.add(lblASPS);
		
		JLabel lblAspsno = new JLabel("0");
		lblAspsno.setHorizontalAlignment(SwingConstants.CENTER);
		panel_1.add(lblAspsno);
		
		JLabel lblRunTime = new JLabel("Run Time");
		lblRunTime.setHorizontalAlignment(SwingConstants.CENTER);
		panel_1.add(lblRunTime);
		
		JLabel lblRuntimeno = new JLabel("0");
		lblRuntimeno.setHorizontalAlignment(SwingConstants.CENTER);
		panel_1.add(lblRuntimeno);
		
		JPanel graphPanel = new JPanel();
		graphPanel.setBorder(null);
		simStatsPanel.add(graphPanel, BorderLayout.CENTER);
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
				SimulationView.exitDisplay(); // Tell OpenGL we are done and free the
				// resources used in the canvas. - must be
				// done else sim will lockup.
				System.exit(0);    // Exit the Simulation and let Java free the
								// memory.
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
		sim.newSim(Integer.parseInt(comboBoxWorldSize.getSelectedItem().toString()), 
				Integer.parseInt(comboBoxPreyNumbers.getSelectedItem().toString()),
				Integer.parseInt(comboBoxPredNumbers.getSelectedItem().toString()),
				Integer.parseInt(comboBoxPlantNumbers.getSelectedItem().toString()));
		
		/* If needed the GC can free old objects now, before the simulation starts */
		System.gc();
		
		btnNew.setEnabled(true);
		
		btnStart.setEnabled(true);
		
		btnPause.setEnabled(false);
		
		btnPause.setText("Pause");
		
		simRateSlider.setEnabled(false);
		
		simRateSlider.setValue(15);

		// Centers the simulated world in the view
		SimulationView.setInitalViewTranslate( (view_width/2)- ((Integer.parseInt(comboBoxWorldSize.getSelectedItem().toString())) / 2 ),(view_height/2)- ((Integer.parseInt(comboBoxWorldSize.getSelectedItem().toString())) / 2 ) );

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
