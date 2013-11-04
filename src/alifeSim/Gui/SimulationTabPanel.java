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
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.LineBorder;

import java.awt.Font;

import javax.swing.UIManager;

import alifeSim.Scenario.ScenarioInf;
import alifeSim.Scenario.ScenarioVT;
import alifeSim.Scenario.Debug.DebugScenario;
import alifeSim.Scenario.SAPP.SAPPScenario;
import alifeSim.Simulation.Simulation;

public class SimulationTabPanel extends JPanel implements ActionListener
{
	private static final long serialVersionUID = 5391587818992199457L;

	// Editor Releated
	private JEditorPane scenarioEditor;
	private JLabel lblFilePath;
	private JButton btnOpen;
	private JButton btnSave;
	private JButton btnClose;
	private File scenarioFile;
	private boolean scenarioSelected = false;

	// Sim Control 
	private JButton btnGenerateSim;
	private JButton btnStartSim;
	private JButton btnPauseSim;
	private JSlider sliderSimStepRate;
	
	// Sim Related
	private Simulation sim;
	private static boolean generatingSim = false;

	public SimulationTabPanel()
	{		
		setLayout(new BorderLayout(0, 0));
		JTabbedPane simulationTabPane = new JTabbedPane(JTabbedPane.TOP);
		add(simulationTabPane, BorderLayout.CENTER);

		JPanel simulationInfoTab = new JPanel();
		simulationTabPane.addTab("Information", null, simulationInfoTab, null);
		simulationInfoTab.setLayout(new BorderLayout(0, 0));

		JPanel controlPanel = new JPanel();
		controlPanel.setBorder(new TitledBorder(null, "Control", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		simulationInfoTab.add(controlPanel, BorderLayout.SOUTH);
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

		JLabel label = new JLabel("Step Rate");
		label.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.fill = GridBagConstraints.BOTH;
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.gridx = 0;
		gbc_label.gridy = 0;
		controlPanel.add(label, gbc_label);

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

		JLabel lblAvgStepRate = new JLabel("0");
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

		JLabel lblStepCount = new JLabel("0");
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

		JLabel lblRequestedStepRate = new JLabel("0");
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

		JLabel lblSimRunTime = new JLabel("0");
		lblSimRunTime.setHorizontalAlignment(SwingConstants.CENTER);
		simRunTime.add(lblSimRunTime, BorderLayout.CENTER);

		sliderSimStepRate = new JSlider();
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
		btnGenerateSim.addActionListener(this);
		btnGenerateSim.setToolTipText("Generate a new simuation based on the values of the parameters.");
		btnGenerateSim.setEnabled(true);
		GridBagConstraints gbc_btnGenerateSim = new GridBagConstraints();
		gbc_btnGenerateSim.fill = GridBagConstraints.BOTH;
		gbc_btnGenerateSim.insets = new Insets(0, 0, 0, 5);
		gbc_btnGenerateSim.gridx = 0;
		gbc_btnGenerateSim.gridy = 3;
		controlPanel.add(btnGenerateSim, gbc_btnGenerateSim);

		btnStartSim = new JButton("Start");
		btnStartSim.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) 
			{
				sim.reqSimUpdateRate(15);
				sim.startSim();
			}
		});
		btnStartSim.setToolTipText("Start the simulation.");
		btnStartSim.setEnabled(false);
		GridBagConstraints gbc_btnStartSim = new GridBagConstraints();
		gbc_btnStartSim.fill = GridBagConstraints.BOTH;
		gbc_btnStartSim.insets = new Insets(0, 0, 0, 5);
		gbc_btnStartSim.gridx = 1;
		gbc_btnStartSim.gridy = 3;
		controlPanel.add(btnStartSim, gbc_btnStartSim);

		btnPauseSim = new JButton("   Pause");
		btnPauseSim.setToolTipText("Pause / Unpause the simulation.");
		btnPauseSim.setEnabled(false);
		GridBagConstraints gbc_btnPauseSim = new GridBagConstraints();
		gbc_btnPauseSim.fill = GridBagConstraints.BOTH;
		gbc_btnPauseSim.gridx = 2;
		gbc_btnPauseSim.gridy = 3;
		controlPanel.add(btnPauseSim, gbc_btnPauseSim);

		JPanel scenarioPanel = new JPanel();
		simulationInfoTab.add(scenarioPanel, BorderLayout.CENTER);
		scenarioPanel.setLayout(new BorderLayout(0, 0));

		JPanel scenarioOpenPanel = new JPanel();
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
		btnOpen.addActionListener(this);
		GridBagConstraints gbc_btnOpen = new GridBagConstraints();
		gbc_btnOpen.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnOpen.insets = new Insets(0, 0, 0, 5);
		gbc_btnOpen.gridx = 0;
		gbc_btnOpen.gridy = 0;
		scenarioOpenPanel.add(btnOpen, gbc_btnOpen);

		btnSave = new JButton("Save");
		btnSave.addActionListener(this);
		GridBagConstraints gbc_btnSave = new GridBagConstraints();
		gbc_btnSave.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSave.insets = new Insets(0, 0, 0, 5);
		gbc_btnSave.gridx = 1;
		gbc_btnSave.gridy = 0;
		scenarioOpenPanel.add(btnSave, gbc_btnSave);

		btnClose = new JButton("Close");
		btnClose.addActionListener(this);
		GridBagConstraints gbc_btnClose = new GridBagConstraints();
		gbc_btnClose.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnClose.gridx = 2;
		gbc_btnClose.gridy = 0;
		scenarioOpenPanel.add(btnClose, gbc_btnClose);

		JPanel scenarioFilePanel = new JPanel();
		scenarioPanel.add(scenarioFilePanel, BorderLayout.CENTER);
		scenarioFilePanel.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scenarioFilePanel.add(scrollPane, BorderLayout.CENTER);

		scenarioEditor = new JEditorPane();
		scenarioEditor.setFont(new Font("Monospaced", Font.BOLD, 12));
		scenarioEditor.setForeground(new Color(255, 255, 255));
		scenarioEditor.setBackground(new Color(0, 0, 64));
		scenarioEditor.setEditable(false);
		scrollPane.setViewportView(scenarioEditor);

		JPanel filePanel = new JPanel();
		scrollPane.setColumnHeaderView(filePanel);
		filePanel.setLayout(new BorderLayout(0, 0));

		lblFilePath = new JLabel("No File");
		filePanel.add(lblFilePath);

		JCheckBox chckbxEditMode = new JCheckBox("EditMode");
		filePanel.add(chckbxEditMode, BorderLayout.EAST);

		JTabbedPane simulationGraph = new JTabbedPane(JTabbedPane.TOP);
		simulationTabPane.addTab("Graph", null, simulationGraph, null);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		// Open Scenario
		if(e.getSource() == btnOpen )
		{
			final JFileChooser filechooser = new JFileChooser(new File("./scenarios"));

			int val = filechooser.showOpenDialog(filechooser);

			if (val == JFileChooser.APPROVE_OPTION)
			{
				scenarioFile = filechooser.getSelectedFile();
				try
				{
					
					/* Not already generating Sim */
					if (!generatingSim)
					{
						generatingSim = true;

						/* Create the new Simulation */
						newSim(scenarioFile);

						generatingSim = false;

					}
					 
					
					lblFilePath.setText(scenarioFile.getAbsolutePath());
					scenarioEditor.setPage(scenarioFile.toURI().toURL());
					
					scenarioSelected = true;
					
				}
				catch (IOException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

		}
		else if(e.getSource() == btnGenerateSim)
		{
			if(scenarioSelected)
			{
				determinScenarios(scenarioFile);
			}
		}
		else
		{
			System.out.println("Button " + ((JButton)e.getSource()).getText() + " Not Implemented");
		}
		
	}
	
	private ScenarioInf determinScenarios(File file)
	{
		ScenarioVT scenarioParser = new ScenarioVT(file);
		ScenarioInf simScenario = null;

		System.out.println(scenarioParser.getScenarioType());

		if (scenarioParser.getScenarioType().equals("DEBUG"))
		{
			System.out.println("Debug File");
			simScenario = new DebugScenario(file);
		}
		else
		{
			if (scenarioParser.getScenarioType().equals("SAPP"))
			{
				System.out.println("SAPP File");
				simScenario = new SAPPScenario(file);
			}
			else
			{
				System.out.println("UKNOWN");
			}
		}

		return simScenario;
	}

	private void newSim(File scenario)
	{

		System.out.println("New Simulation : " + scenario.getAbsolutePath());

		/* Cleans up the old simulation threads */
		if(sim !=null)
		{
			// Pause will get the simulation threads to a safe position, i.e not inside a list.
			if(!sim.simPaused())
			{
				sim.pauseSim();
			}
			
			sim.destroySim();
		}

		sim = new Simulation();
		
		sim.createSim(determinScenarios(scenario));
		SimulationView.setSim(sim);

		/*
		 * If needed the GC can free old objects now, before the simulation
		 * starts
		 */
		System.gc();

		btnGenerateSim.setEnabled(true);

		btnStartSim.setEnabled(true);

		btnPauseSim.setEnabled(false);

		btnPauseSim.setText("   Pause");

		btnPauseSim.setIcon(new ImageIcon(SimulationGUI.class.getResource("/alifeSim/icons/pause.png")));

		sliderSimStepRate.setEnabled(false);

		sliderSimStepRate.setValue(15);

	}
	public Simulation getSimualtion()
	{
		return sim;
	}
	
	
}
