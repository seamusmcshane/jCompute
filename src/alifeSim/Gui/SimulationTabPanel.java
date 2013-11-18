package alifeSim.Gui;

import javax.swing.JPanel;

import java.awt.BorderLayout;

import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;

import java.awt.GridBagLayout;

import javax.swing.JLabel;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.SwingConstants;

import java.awt.Color;

import javax.swing.JSlider;

import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.Font;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import alifeSim.ChartPanels.StatPanelAbs;
import alifeSim.Scenario.ScenarioCharts;
import alifeSim.Scenario.ScenarioInf;
import alifeSim.Scenario.ScenarioVT;
import alifeSim.Scenario.Debug.DebugScenario;
import alifeSim.Scenario.SAPP.SAPPScenario;
import alifeSim.Simulation.Simulation;
import alifeSim.Simulation.SimulationPerformanceStats;
import alifeSim.Simulation.SimulationPerformanceStatsOutputInf;

public class SimulationTabPanel extends JPanel implements ActionListener, ChangeListener, SimulationPerformanceStatsOutputInf
{
	private static final long serialVersionUID = 5391587818992199457L;

	// Graphs
	JTabbedPane simulationTabPane;
	LinkedList<StatPanelAbs> charts;

	// Editor Releated
	//private JEditorPane scenarioEditor;
	private RSyntaxTextArea scenarioEditor;
	private JLabel lblFilePath;
	private JButton btnOpen;
	private JButton btnSave;
	private JButton btnClose;
	private JCheckBox chckbxEditMode;
	private boolean scenarioLoaded = false;
	private Color normalMode = new Color(240, 240, 255);
	private Color editMode = new Color(255,240, 240);

	// Sim Control
	private JButton btnGenerateSim;
	private JButton btnStartSim;
	private JButton btnPauseSim;
	private JSlider sliderSimStepRate;

	// Sim RT Performance Display
	private JLabel lblAvgStepRate;
	private JLabel lblStepCount;
	private JLabel lblSimRunTime;
	private JLabel lblRequestedStepRate;

	// Sim Related
	private Simulation sim;
	private static boolean generatingSim = false;

	private String state = "New";
	
	public SimulationTabPanel()
	{
		setLayout(new BorderLayout(0, 0));
		simulationTabPane = new JTabbedPane(JTabbedPane.TOP);
		add(simulationTabPane, BorderLayout.CENTER);

		JPanel simulationScenarioTab = new JPanel();
		simulationTabPane.addTab("Scenario", null, simulationScenarioTab, null);
		simulationScenarioTab.setLayout(new BorderLayout(0, 0));

		JPanel scenarioPanel = new JPanel();
		simulationScenarioTab.add(scenarioPanel, BorderLayout.CENTER);
		scenarioPanel.setLayout(new BorderLayout(0, 0));

		JPanel scenarioOpenPanel = new JPanel();
		scenarioOpenPanel.setBorder(null);
		scenarioPanel.add(scenarioOpenPanel, BorderLayout.SOUTH);
		GridBagLayout gbl_scenarioOpenPanel = new GridBagLayout();
		
		gbl_scenarioOpenPanel.rowHeights = new int[]
		{0};
		gbl_scenarioOpenPanel.columnWidths = new int[]
		{0, 0, 0};
		gbl_scenarioOpenPanel.columnWeights = new double[]
		{1.0, 1.0, 1.0};
		gbl_scenarioOpenPanel.rowWeights = new double[]
		{1.0};
		scenarioOpenPanel.setLayout(gbl_scenarioOpenPanel);

		btnOpen = new JButton("Open");
		btnOpen.setIcon(new ImageIcon(SimulationTabPanel.class.getResource("/alifeSim/icons/document-open.png")));
		btnOpen.addActionListener(this);
		GridBagConstraints gbc_btnOpen = new GridBagConstraints();
		gbc_btnOpen.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnOpen.insets = new Insets(0, 0, 0, 0);
		gbc_btnOpen.gridx = 0;
		gbc_btnOpen.gridy = 0;
		scenarioOpenPanel.add(btnOpen, gbc_btnOpen);

		btnSave = new JButton("Save");
		btnSave.setIcon(new ImageIcon(SimulationTabPanel.class.getResource("/alifeSim/icons/document-save.png")));
		btnSave.addActionListener(this);
		GridBagConstraints gbc_btnSave = new GridBagConstraints();
		gbc_btnSave.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSave.gridx = 1;
		gbc_btnSave.gridy = 0;
		scenarioOpenPanel.add(btnSave, gbc_btnSave);

		btnClose = new JButton("Close");
		btnClose.setIcon(new ImageIcon(SimulationTabPanel.class.getResource("/alifeSim/icons/document-close.png")));
		btnClose.addActionListener(this);
		GridBagConstraints gbc_btnClose = new GridBagConstraints();
		gbc_btnClose.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnClose.gridx = 2;
		gbc_btnClose.gridy = 0;
		scenarioOpenPanel.add(btnClose, gbc_btnClose);

		JPanel scenarioFilePanel = new JPanel();
		scenarioPanel.add(scenarioFilePanel, BorderLayout.CENTER);
		scenarioFilePanel.setLayout(new BorderLayout(0, 0));

		/*JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scenarioFilePanel.add(scrollPane, BorderLayout.CENTER);*/

		/*scenarioEditor = new JEditorPane();
		scenarioEditor.setFont(new Font("Monospaced", Font.BOLD, 12));
		scenarioEditor.setForeground(new Color(255, 255, 255));
		scenarioEditor.setBackground(new Color(0, 0, 64));
		scenarioEditor.setEditable(false);
		scenarioEditor.setCaretColor(Color.white);
		scrollPane.setViewportView(scenarioEditor);*/

		JPanel filePanel = new JPanel();
		//scrollPane.setColumnHeaderView(filePanel);
		filePanel.setLayout(new BorderLayout(0, 0));

		lblFilePath = new JLabel("No File");
		filePanel.add(lblFilePath);

		chckbxEditMode = new JCheckBox("EditMode");
		chckbxEditMode.addChangeListener(this);
		filePanel.add(chckbxEditMode, BorderLayout.EAST);
		
		scenarioEditor = new RSyntaxTextArea();
		scenarioEditor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
		scenarioEditor.setEditable(false);
		scenarioEditor.setBackground(normalMode);
		
		RTextScrollPane sp = new RTextScrollPane(scenarioEditor);
		scenarioFilePanel.add(sp, BorderLayout.CENTER);
		scenarioFilePanel.add(filePanel, BorderLayout.NORTH);
		
		JPanel controlPanel = new JPanel();
		add(controlPanel, BorderLayout.SOUTH);
		controlPanel.setBorder(new TitledBorder(null, "Control", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagLayout gbl_controlPanel = new GridBagLayout();
		gbl_controlPanel.columnWidths = new int[]
		{112, 112, 112};
		gbl_controlPanel.rowHeights = new int[]
		{31, 31, 31, 31};
		gbl_controlPanel.columnWeights = new double[]
		{1.0, 1.0, 1.0};
		gbl_controlPanel.rowWeights = new double[]
		{0.0, 0.0, 0.0, 0.0};
		controlPanel.setLayout(gbl_controlPanel);

		JLabel lblAverageStepRate = new JLabel("Average Step Rate");
		lblAverageStepRate.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_lblAverageStepRate = new GridBagConstraints();
		gbc_lblAverageStepRate.fill = GridBagConstraints.BOTH;
		gbc_lblAverageStepRate.insets = new Insets(0, 0, 5, 5);
		gbc_lblAverageStepRate.gridx = 0;
		gbc_lblAverageStepRate.gridy = 0;
		controlPanel.add(lblAverageStepRate, gbc_lblAverageStepRate);

		JLabel label_1 = new JLabel("Steps");
		label_1.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_label_1 = new GridBagConstraints();
		gbc_label_1.fill = GridBagConstraints.BOTH;
		gbc_label_1.insets = new Insets(0, 0, 5, 5);
		gbc_label_1.gridx = 1;
		gbc_label_1.gridy = 0;
		controlPanel.add(label_1, gbc_label_1);

		JLabel label_2 = new JLabel("Requested Step Rate");
		label_2.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_label_2 = new GridBagConstraints();
		gbc_label_2.fill = GridBagConstraints.BOTH;
		gbc_label_2.insets = new Insets(0, 0, 5, 0);
		gbc_label_2.gridx = 2;
		gbc_label_2.gridy = 0;
		controlPanel.add(label_2, gbc_label_2);

		JPanel simAverageStepRate = new JPanel();
		simAverageStepRate.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		simAverageStepRate.setBackground(Color.WHITE);
		GridBagConstraints gbc_simAverageStepRate = new GridBagConstraints();
		gbc_simAverageStepRate.fill = GridBagConstraints.BOTH;
		gbc_simAverageStepRate.insets = new Insets(0, 0, 5, 5);
		gbc_simAverageStepRate.gridx = 0;
		gbc_simAverageStepRate.gridy = 1;
		controlPanel.add(simAverageStepRate, gbc_simAverageStepRate);
		simAverageStepRate.setLayout(new BorderLayout(0, 0));

		lblAvgStepRate = new JLabel("0");
		lblAvgStepRate.setHorizontalAlignment(SwingConstants.CENTER);
		simAverageStepRate.add(lblAvgStepRate, BorderLayout.CENTER);

		JPanel simStepTotal = new JPanel();
		simStepTotal.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		simStepTotal.setBackground(Color.WHITE);
		GridBagConstraints gbc_simStepTotal = new GridBagConstraints();
		gbc_simStepTotal.fill = GridBagConstraints.BOTH;
		gbc_simStepTotal.insets = new Insets(0, 0, 5, 5);
		gbc_simStepTotal.gridx = 1;
		gbc_simStepTotal.gridy = 1;
		controlPanel.add(simStepTotal, gbc_simStepTotal);
		simStepTotal.setLayout(new BorderLayout(0, 0));

		lblStepCount = new JLabel("0");
		lblStepCount.setHorizontalAlignment(SwingConstants.CENTER);
		simStepTotal.add(lblStepCount, BorderLayout.CENTER);

		JPanel simRequestedStepRate = new JPanel();
		simRequestedStepRate.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		simRequestedStepRate.setBackground(Color.WHITE);
		GridBagConstraints gbc_simRequestedStepRate = new GridBagConstraints();
		gbc_simRequestedStepRate.fill = GridBagConstraints.BOTH;
		gbc_simRequestedStepRate.insets = new Insets(0, 0, 5, 0);
		gbc_simRequestedStepRate.gridx = 2;
		gbc_simRequestedStepRate.gridy = 1;
		controlPanel.add(simRequestedStepRate, gbc_simRequestedStepRate);
		simRequestedStepRate.setLayout(new BorderLayout(0, 0));

		lblRequestedStepRate = new JLabel("0");
		lblRequestedStepRate.setHorizontalAlignment(SwingConstants.CENTER);
		simRequestedStepRate.add(lblRequestedStepRate, BorderLayout.CENTER);

		JLabel label_3 = new JLabel("Time");
		label_3.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_label_3 = new GridBagConstraints();
		gbc_label_3.fill = GridBagConstraints.BOTH;
		gbc_label_3.insets = new Insets(0, 0, 5, 5);
		gbc_label_3.gridx = 0;
		gbc_label_3.gridy = 2;
		controlPanel.add(label_3, gbc_label_3);

		JPanel simRunTime = new JPanel();
		simRunTime.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		simRunTime.setBackground(Color.WHITE);
		GridBagConstraints gbc_simRunTime = new GridBagConstraints();
		gbc_simRunTime.fill = GridBagConstraints.BOTH;
		gbc_simRunTime.insets = new Insets(0, 0, 5, 5);
		gbc_simRunTime.gridx = 1;
		gbc_simRunTime.gridy = 2;
		controlPanel.add(simRunTime, gbc_simRunTime);
		simRunTime.setLayout(new BorderLayout(0, 0));

		lblSimRunTime = new JLabel("0");
		lblSimRunTime.setHorizontalAlignment(SwingConstants.CENTER);
		simRunTime.add(lblSimRunTime, BorderLayout.CENTER);

		sliderSimStepRate = new JSlider();
		sliderSimStepRate.addChangeListener(this);

		sliderSimStepRate.setValue(15);
		sliderSimStepRate.setToolTipText("Adjust requested step rate.");
		sliderSimStepRate.setSnapToTicks(true);
		sliderSimStepRate.setPreferredSize(new Dimension(25, 20));
		sliderSimStepRate.setPaintTicks(true);
		sliderSimStepRate.setMinorTickSpacing(30);
		sliderSimStepRate.setMinimum(15);
		sliderSimStepRate.setMaximum(300);
		sliderSimStepRate.setMajorTickSpacing(150);
		sliderSimStepRate.setEnabled(false);
		GridBagConstraints gbc_sliderSimStepRate = new GridBagConstraints();
		gbc_sliderSimStepRate.fill = GridBagConstraints.BOTH;
		gbc_sliderSimStepRate.insets = new Insets(0, 0, 5, 0);
		gbc_sliderSimStepRate.gridx = 2;
		gbc_sliderSimStepRate.gridy = 2;
		controlPanel.add(sliderSimStepRate, gbc_sliderSimStepRate);

		btnGenerateSim = new JButton("Generate");
		btnGenerateSim.setIcon(new ImageIcon(SimulationTabPanel.class.getResource("/alifeSim/icons/grid.png")));
		btnGenerateSim.addActionListener(this);
		btnGenerateSim.setToolTipText("Generate a new simuation based on the values of the parameters.");
		btnGenerateSim.setEnabled(false);
		GridBagConstraints gbc_btnGenerateSim = new GridBagConstraints();
		gbc_btnGenerateSim.fill = GridBagConstraints.BOTH;
		gbc_btnGenerateSim.insets = new Insets(0, 0, 0, 5);
		gbc_btnGenerateSim.gridx = 0;
		gbc_btnGenerateSim.gridy = 3;
		controlPanel.add(btnGenerateSim, gbc_btnGenerateSim);

		btnStartSim = new JButton("Start");
		btnStartSim.setIcon(new ImageIcon(SimulationTabPanel.class.getResource("/alifeSim/icons/play.png")));
		btnStartSim.addActionListener(this);
		btnStartSim.setToolTipText("Start the simulation.");
		btnStartSim.setEnabled(false);
		GridBagConstraints gbc_btnStartSim = new GridBagConstraints();
		gbc_btnStartSim.fill = GridBagConstraints.BOTH;
		gbc_btnStartSim.insets = new Insets(0, 0, 0, 5);
		gbc_btnStartSim.gridx = 1;
		gbc_btnStartSim.gridy = 3;
		controlPanel.add(btnStartSim, gbc_btnStartSim);

		btnPauseSim = new JButton("   Pause");
		btnPauseSim.setIcon(new ImageIcon(SimulationTabPanel.class.getResource("/alifeSim/icons/resume.png")));
		btnPauseSim.addActionListener(this);
		btnPauseSim.setToolTipText("Pause / Unpause the simulation.");
		btnPauseSim.setEnabled(false);
		GridBagConstraints gbc_btnPauseSim = new GridBagConstraints();
		gbc_btnPauseSim.fill = GridBagConstraints.BOTH;
		gbc_btnPauseSim.gridx = 2;
		gbc_btnPauseSim.gridy = 3;
		controlPanel.add(btnPauseSim, gbc_btnPauseSim);

	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		// Open Scenario
		if (e.getSource() == btnOpen)
		{
			final JFileChooser filechooser = new JFileChooser(new File("./scenarios"));

			System.out.println("Scenario Open Dialog");

			int val = filechooser.showOpenDialog(filechooser);

			if (val == JFileChooser.APPROVE_OPTION)
			{
				System.out.println("New Scenario Choosen");
				scenarioEditor.removeAll();
				destroySimulation();
				//File scenarioFile = filechooser.getSelectedFile();
				lblFilePath.setText(filechooser.getSelectedFile().getAbsolutePath().toString());
				BufferedReader bufferedReader;
				
				try
				{
					bufferedReader = new BufferedReader(new FileReader(filechooser.getSelectedFile()));
					String sCurrentLine;
					while ((sCurrentLine = bufferedReader.readLine()) != null) 
					{
						scenarioEditor.append(sCurrentLine + "\n");
						//scenarioEditor.insert(sCurrentLine, scenarioEditor.getLineCount());
						//System.out.println(sCurrentLine);
					}
				}
				catch (FileNotFoundException e1)
				{
					System.out.println("File Not Found");
					e1.printStackTrace();
				}
				catch (IOException e1)
				{
					System.out.println("I/O Error");
					e1.printStackTrace();
				}
				
				scenarioLoaded = true;

				// Set the Startup State
				startUpState();

			}

		}
		else
			if (e.getSource() == btnGenerateSim)
			{
				if (scenarioLoaded)
				{
					/* Not already generating Sim */
					if (!generatingSim)
					{
						generatingSim = true;

						/* Create the new Simulation */
						newSim(scenarioEditor.getText());

						generatingSim = false;

					}
				}

			}
			else
				if (e.getSource() == btnPauseSim)
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
				else
					if (e.getSource() == btnStartSim)
					{
						simStartedState();
					}
					else
					{
						System.out.println("Button " + ((JButton) e.getSource()).getText() + " Not Implemented");
					}

	}

	private ScenarioInf determinScenarios(String text)
	{
		ScenarioVT scenarioParser = new ScenarioVT(text);
		ScenarioInf simScenario = null;

		System.out.println("Scenario Type : " + scenarioParser.getScenarioType());

		if (scenarioParser.getScenarioType().equals("DEBUG"))
		{
			System.out.println("Debug File");
			simScenario = new DebugScenario(text);
		}
		else
		{
			if (scenarioParser.getScenarioType().equals("SAPP"))
			{
				System.out.println("SAPP File");
				simScenario = new SAPPScenario(text);
			}
			else
			{
				System.out.println("UKNOWN");
			}
		}

		return simScenario;
	}

	private void destroySimulation()
	{
		System.out.println("Request to Destroy Old Simulation");

		/* Cleans up the old simulation threads */
		if (sim != null)
		{
			// Pause will get the simulation threads to a safe position, i.e not
			// inside a list.
			if (!sim.simPaused())
			{
				sim.pauseSim();
			}

			sim.destroySim();

			System.out.println("Simulation Destroyed");
		}
		else
		{
			System.out.println("A Previous Simulation was not created");
		}

	}

	private void setUpPanels(String text, Simulation sim)
	{
		ScenarioCharts chartDetector = new ScenarioCharts(text,sim);
		
		// Setup the chart/panel list
		if (charts == null)
		{
			charts = new LinkedList<StatPanelAbs>();
		}
		else
		{
			for (StatPanelAbs chartPanel : charts)
			{
				simulationTabPane.remove(chartPanel);
				charts.remove(chartPanel);
				chartPanel.destroy();
			}

		}
		
		// Add the detected Panels
		
		for (StatPanelAbs chartPanel : chartDetector.getCharts())
		{
			charts.add(chartPanel);
			simulationTabPane.addTab(chartPanel.getName(), null,chartPanel);
		}
		
		sim.setOutPutCharts(charts);
	}

	private void newSim(String scenario)
	{
		destroySimulation();

		sim = new Simulation(new SimulationPerformanceStats(this));

		sim.createSim(determinScenarios(scenario));
		SimulationView.setSim(sim);

		setUpPanels(scenario,sim);
		/*
		 * If needed the GC can free old objects now, before the simulation
		 * starts
		 */
		System.gc();

		btnGenerateSim.setEnabled(true);

		btnStartSim.setEnabled(true);

		btnPauseSim.setEnabled(false);

		btnPauseSim.setText("   Pause");

		btnPauseSim.setIcon(new ImageIcon(SimulationTabPanel.class.getResource("/alifeSim/icons/pause.png")));

		sliderSimStepRate.setEnabled(false);

		sliderSimStepRate.setValue(15);

	}

	public Simulation getSimulation()
	{
		return sim;
	}

	/**
	 * The Average Steps per second.
	 * 
	 * @param asps
	 *            int
	 */
	public void setASPS(int asps)
	{
		lblAvgStepRate.setText(Integer.toString(asps));
	}

	/**
	 * The current step number.
	 * 
	 * @param stepNo
	 */
	public void setStepNo(long stepNo)
	{
		lblStepCount.setText(Long.toString(stepNo));
	}

	/**
	 * Displays the current run time of the simulation from a long count in
	 * milliseconds
	 * 
	 * @param time
	 */
	public void setTime(long time)
	{
		time = time / 1000; // seconds
		int days = (int) (time / 86400); // to days
		int hrs = (int) (time / 3600) % 24; // to hrs
		int mins = (int) ((time / 60) % 60);	// to seconds
		int sec = (int) (time % 60);

		lblSimRunTime.setText(String.format("%d:%02d:%02d:%02d", days, hrs, mins, sec));

	}

	private void simStartedState()
	{

		state = "Running";
		
		sim.startSim();

		btnGenerateSim.setEnabled(false);

		btnStartSim.setEnabled(false);

		btnPauseSim.setEnabled(true);

		sliderSimStepRate.setEnabled(true);

	}

	private void startUpState()
	{
		System.out.println("Simulation now in Startup State");

		state = "New";
		
		clearStats();

		btnStartSim.setEnabled(false);
		sliderSimStepRate.setEnabled(false);
		btnPauseSim.setEnabled(false);
		btnGenerateSim.setEnabled(true);
		SimulationView.setSim(null);
	}

	private void simPausedState()
	{
		state = "Paused";
		
		btnPauseSim.setText("Resume");
		btnGenerateSim.setEnabled(true);

		sim.pauseSim();

		btnPauseSim.setIcon(new ImageIcon(SimulationTabPanel.class.getResource("/alifeSim/icons/resume.png")));

	}

	private void simUnPausedState()
	{
		state = "Running";
		
		btnPauseSim.setText("   Pause");
		btnGenerateSim.setEnabled(false);

		sim.unPauseSim();

		btnPauseSim.setIcon(new ImageIcon(SimulationTabPanel.class.getResource("/alifeSim/icons/pause.png")));
	}

	public String getState()
	{
		return state;
	}
	
	@Override
	public void stateChanged(ChangeEvent e)
	{
		if (e.getSource() == sliderSimStepRate)
		{
			if (sim != null)
			{
				// Prevent a 0 value being set
				if (sliderSimStepRate.getValue() == 0)
				{
					lblRequestedStepRate.setText("1");

					// Set the requested update rate
					sim.reqSimUpdateRate(sliderSimStepRate.getValue());
				}
				else
				{
					if (sliderSimStepRate.getValue() < 300)
					{
						lblRequestedStepRate.setText(Integer.toString(sliderSimStepRate.getValue()));

						// Set the requested update rate
						sim.reqSimUpdateRate(sliderSimStepRate.getValue());
					}
					else
					{
						lblRequestedStepRate.setText("Unli");

						// Set the requested update rate
						sim.reqSimUpdateRate(-1);
					}

				}
			}
		}
		else
			if (e.getSource() == chckbxEditMode)
			{
				if (chckbxEditMode.isSelected())
				{
					scenarioEditor.setEditable(true);
					scenarioEditor.setBackground(editMode);

				}
				else
				{
					scenarioEditor.setEditable(false);
					scenarioEditor.setBackground(normalMode);

				}
			}
			else
			{
				System.out.println("stateChanged : " + e.getSource().toString());
			}

	}

	public void destroy()
	{
		destroySimulation();
	}

	@Override
	public void clearStats()
	{
		System.out.println("Simulation Stats Cleared");
		setASPS(0);
		setStepNo(0);
		setTime(0);
	}

	public String getASPS()
	{
		return lblAvgStepRate.getText();
	}

	/**
	 * The current step number.
	 * 
	 * @param stepNo
	 */
	public String getStepNo()
	{
		return lblStepCount.getText();
	}

	/**
	 * Displays the current run time of the simulation from a long count in
	 * milliseconds
	 * 
	 * @param time
	 */
	public String getTime()
	{
		return lblSimRunTime.getText();
	}
}
