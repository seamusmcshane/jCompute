package alife;

import java.awt.Dimension;
import java.awt.EventQueue;
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

public class SimulationGUI
{
	private static JFrame gui;

	/** Gui Frame Items */
	private static JSlider simRateSlider;
	private static JButton btnNew;
	private static JButton btnPause;
	private static JButton btnStart;
	private static JTextField txtSimRateInfo;
	private static JComboBox comboBoxAgentNumbers;
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

		gui.setBounds(control_gui_x, control_gui_y, control_gui_width, control_gui_height);
		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gui.getContentPane().setLayout(new BorderLayout(0, 0));

		/* Simulation Speed */
		JPanel statusPanel = new JPanel();
		statusPanel.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		gui.getContentPane().add(statusPanel, BorderLayout.SOUTH);
		// statusPanel.setPreferredSize(new Dimension(statusPanelWidth,
		// statusPanelHeight));
		statusPanel.setLayout(new BorderLayout(0, 0));

		JPanel controlPanel = new JPanel();
		controlPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		// controlPanel.setPreferredSize(new Dimension(controlPanelWidth,
		// controlPanelHeight));

		gui.getContentPane().add(controlPanel, BorderLayout.CENTER);
		controlPanel.setLayout(new BorderLayout(0, 0));

		JPanel controlPanelTop = new JPanel();
		controlPanelTop.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Setup", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		controlPanel.add(controlPanelTop, BorderLayout.NORTH);
		controlPanelTop.setLayout(new MigLayout("", "[][grow]", "[][][]"));

		JLabel lblAgents = new JLabel("Agents");
		controlPanelTop.add(lblAgents, "cell 0 0,alignx trailing");

		comboBoxAgentNumbers = new JComboBox();
		comboBoxAgentNumbers.setModel(new DefaultComboBoxModel(new String[]
		{"100", "200", "400", "800", "1600", "3200", "6400", "12800", "25600", "51200", "102400", "204800"}));
		controlPanelTop.add(comboBoxAgentNumbers, "cell 1 0,growx");
		
		JLabel lblPlants = new JLabel("Plants");
		controlPanelTop.add(lblPlants, "cell 0 1,alignx trailing");
		
		comboBoxPlantNumbers = new JComboBox();
		comboBoxPlantNumbers.setModel(new DefaultComboBoxModel(new String[] {"100", "200", "400", "800", "1600", "3200", "6400", "12800", "25600", "51200", "102400", "204800"}));
		controlPanelTop.add(comboBoxPlantNumbers, "cell 1 1,growx");

		JLabel lblWorldSize = new JLabel("World Size");
		controlPanelTop.add(lblWorldSize, "cell 0 2,alignx trailing");

		comboBoxWorldSize = new JComboBox();
		comboBoxWorldSize.setModel(new DefaultComboBoxModel(new String[]
		{"512", "1024", "2048", "4096", "8192", "16384", "32768"}));
		controlPanelTop.add(comboBoxWorldSize, "cell 1 2,growx");

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
		txtSimRateInfo.setText("0");
		txtSimRateInfo.setColumns(10);
		simRateSlider = new JSlider();
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
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Graph", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		controlPanel.add(panel, BorderLayout.CENTER);
		panel.setLayout(new BorderLayout(0, 0));
		
		JPanel keyPanel = new JPanel();
		keyPanel.setBorder(new TitledBorder(null, "Key", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.add(keyPanel, BorderLayout.SOUTH);
		keyPanel.setLayout(new GridLayout(0, 6, 0, 0));
		
		JLabel lblKeyPlants = new JLabel("Plants");
		lblKeyPlants.setHorizontalAlignment(SwingConstants.CENTER);
		keyPanel.add(lblKeyPlants);
		
		JLabel lblPlantno = new JLabel("plant_no");
		lblPlantno.setHorizontalAlignment(SwingConstants.CENTER);
		keyPanel.add(lblPlantno);
		
		JLabel lblPred = new JLabel("Predators");
		lblPred.setHorizontalAlignment(SwingConstants.CENTER);
		keyPanel.add(lblPred);
		
		JLabel lblPredno = new JLabel("pred_no");
		lblPredno.setHorizontalAlignment(SwingConstants.CENTER);
		keyPanel.add(lblPredno);
		
		JLabel lblPrey = new JLabel("Prey");
		lblPrey.setHorizontalAlignment(SwingConstants.CENTER);
		keyPanel.add(lblPrey);
		
		JLabel lblPreyno = new JLabel("prey_no");
		lblPreyno.setHorizontalAlignment(SwingConstants.CENTER);
		keyPanel.add(lblPreyno);
		
		JPanel graphPanel = new JPanel();
		panel.add(graphPanel, BorderLayout.CENTER);
		btnPause.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				if (sim.simPaused())
				{
					sim.unPauseSim();
					
					simUnPausedState();

				}
				else
				{
					sim.pauseSim();
					
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
		});

		gui.setVisible(true);
		
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
		sim.newSim(Integer.parseInt(comboBoxWorldSize.getSelectedItem().toString()), Integer.parseInt(comboBoxAgentNumbers.getSelectedItem().toString()),Integer.parseInt(comboBoxPlantNumbers.getSelectedItem().toString()));
		
		/* If needed the GC can free old objects now, before the simulation starts */
		System.gc();
		
		btnNew.setEnabled(false);
		
		btnStart.setEnabled(true);
		
		btnPause.setEnabled(false);
		
		btnPause.setText("Pause");
		
		simRateSlider.setEnabled(false);
		
		simRateSlider.setValue(15);

	}

	private static void simStartedState()
	{
		SimulationView.setFocus();
		
		sim.startSim();
		
		btnNew.setEnabled(false);
		
		btnStart.setEnabled(false);
		
		btnPause.setEnabled(true);
		
		simRateSlider.setEnabled(true);
		
		comboBoxAgentNumbers.setEnabled(false);
		
		comboBoxWorldSize.setEnabled(false);	

		comboBoxPlantNumbers.setEnabled(false);	

		
	}
	
	private static void startUpState()
	{
		comboBoxAgentNumbers.setEnabled(true);
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
		
		comboBoxAgentNumbers.setEnabled(true);
		comboBoxWorldSize.setEnabled(true);	
		comboBoxPlantNumbers.setEnabled(true);	

	}
	
	private static void simUnPausedState()
	{
		btnPause.setText("Pause");		
		btnNew.setEnabled(false);

		comboBoxAgentNumbers.setEnabled(false);
		comboBoxWorldSize.setEnabled(false);	
		comboBoxPlantNumbers.setEnabled(false);	
	}
	
}
