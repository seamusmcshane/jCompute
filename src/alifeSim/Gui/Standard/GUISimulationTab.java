package alifeSim.Gui.Standard;

import javax.swing.JPanel;

import java.awt.BorderLayout;

import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;

import java.awt.GridBagLayout;

import javax.swing.JLabel;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.SwingConstants;

import java.awt.Color;

import javax.swing.JSlider;

import java.awt.Dimension;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import java.util.zip.GZIPOutputStream;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.Font;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

import alifeSim.Gui.Charts.GlobalStatChartPanel;
import alifeSim.Scenario.ScenarioInf;
import alifeSim.Scenario.ScenarioVT;
import alifeSim.Scenario.Debug.DebugScenario;
import alifeSim.Scenario.Math.LotkaVolterra.LotkaVolterraScenario;
import alifeSim.Scenario.SAPP.SAPPScenario;
import alifeSim.Simulation.SimulationScenarioManagerInf;
import alifeSim.Simulation.SimulationStatListenerInf;
import alifeSim.Simulation.SimulationState.SimState;
import alifeSim.Simulation.SimulationStateListenerInf;
import alifeSim.Simulation.SimulationsManager;
import alifeSim.Stats.StatGroup;
import alifeSim.Stats.StatManager;

public class GUISimulationTab extends JPanel implements ActionListener, ChangeListener, SimulationStateListenerInf, SimulationStatListenerInf
{
	private static final long serialVersionUID = 5391587818992199457L;
	
	private String tabTitle = "New";
	
	// Editor Related
	private RSyntaxTextArea scenarioEditor;
	private JLabel lblFilePath;
	private JButton btnOpen;
	private JButton btnSave;
	private JCheckBox chckbxEditMode;
	private boolean scenarioLoaded = false;
	private Color normalMode;
	private Color editMode;
	private boolean saved = true;

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

	// Because the values in the GUI are only updated on a time interval we latch the values and update it using them
	private long latchTime = 0;
	private int latchStepNo = 0;
	private int latchASPS = 0;
	
	// Sim Related
	private boolean generatingSim = false;
	private boolean simGenerated = false;

	/* Tabs */
	private JTabbedPane simulationTabPane;

	private JPanel simulationScenarioTab;
	private SimulationStatsListPanel simulationStatsListPanel;
	
	private GraphsTabPanel graphsTabPanel;
	
	private SimulationsManager simsManager;
	private StatManager statManager;
	
	/* This Sim */
	private int simId = -1;

	private Timer updateTimer;
	
	/* Tab Status Icons */
	private ImageIcon simRunningIcon = new ImageIcon(GUITabManager.class.getResource("/alifeSim/icons/media-playback-start.png"));
	private ImageIcon simPausedIcon = new ImageIcon(GUITabManager.class.getResource("/alifeSim/icons/media-playback-pause.png"));
	private ImageIcon newTabIcon = new ImageIcon(GUITabManager.class.getResource("/alifeSim/icons/dialog-warning.png"));
	private ImageIcon simNewIcon = new ImageIcon(GUITabManager.class.getResource("/alifeSim/icons/media-playback-stop.png"));
	private ImageIcon simFinishedIcon = new ImageIcon(GUITabManager.class.getResource("/alifeSim/icons/task-complete.png"));
	
	/* Scenario Editor Icons */
	private ImageIcon openScenarioIcon = new ImageIcon(GUISimulationTab.class.getResource("/alifeSim/icons/document-open.png"));
	private ImageIcon saveScenarioIcon = new ImageIcon(GUISimulationTab.class.getResource("/alifeSim/icons/document-save.png"));
	
	/* Button Icons */
	private ImageIcon generateSimIcon = new ImageIcon(GUISimulationTab.class.getResource("/alifeSim/icons/grid.png"));
	private ImageIcon startSimIcon = new ImageIcon(GUISimulationTab.class.getResource("/alifeSim/icons/play.png"));
	private ImageIcon resumeSimIcon = new ImageIcon(GUISimulationTab.class.getResource("/alifeSim/icons/resume.png"));
	private ImageIcon pauseSimIcon = new ImageIcon(GUISimulationTab.class.getResource("/alifeSim/icons/pause.png"));

	/* Chart Tab Icons */
	private ImageIcon simulationStatsExportIcon = new ImageIcon(GUISimulationTab.class.getResource("/alifeSim/icons/kspread.png"));
	private ImageIcon scenarioEditorIcon = new ImageIcon(GUISimulationTab.class.getResource("/alifeSim/icons/accessories-text-editor.png"));
	private ImageIcon simulationStatChartIcon = new ImageIcon(GUISimulationTab.class.getResource("/alifeSim/icons/kchart.png"));

	// Tab Related
	private List<TabStatusChangedListenerInf> tabStatusListeners = new ArrayList<TabStatusChangedListenerInf>();
	private Semaphore listenersLock = new Semaphore(1, false);
	private TabButton title;
	
	public GUISimulationTab(GUITabManager tabManager,SimulationsManager simsManager, int simId)
	{
		this.simsManager = simsManager;

		this.simId = simId;
				
		// Tab Close Button
		title = new TabButton(tabManager,this);	
		
		// Layout
		setLayout(new BorderLayout(0, 0));

		// Tab Top = Tab Pane
		simulationTabPane = new JTabbedPane(JTabbedPane.TOP);
		simulationTabPane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
		add(simulationTabPane, BorderLayout.CENTER);
		
		// Scenario Editor
		setUpScenarioEditorTab();
		
		simulationStatsListPanel = new SimulationStatsListPanel();

		graphsTabPanel = new GraphsTabPanel();		

		// Simulation Control GUI
		setUpSimulationContolPanel();
					
		checkTabState();

		/* Pause the active sim or the GUI will compete for every semaphore lock */
		SimState simState = simsManager.getState(simId);
		if(simState == SimState.RUNNING)
		{
			simsManager.pauseSim(simId);
		}
		
		addPanels();
		
		/* If the Sim was Running then resume */
		if(simState == SimState.RUNNING)
		{
			simsManager.unPauseSim(simId);
		}
		
	}

	public void checkTabState()
	{
		// We may be a tab for an active simulation 
		if(simId!=-1)
		{
			SimulationScenarioManagerInf simulationScenario = simsManager.getScenarioManager(simId);
			
			SimState state = simsManager.getState(simId);
			
			statManager = simsManager.getStatManager(simId);
			
			scenarioEditor.setText(simulationScenario.getScenario().getScenarioText());		
			
			scenarioLoaded = true;
			
			registerListeners();
			
			setSimView();
							
			tabTitle = "Simulation " + simId;
			
			setGuiState(state);
			
			// A slow timer to update GUI at a rate independent of SimulationStatChanged notifications.
			updateTimer = new Timer("GUI Stat Update Timer");
			updateTimer.schedule(new TimerTask()
			{
				@Override
				public void run() 
				{
					setTime(latchTime);
					setStepNo(latchStepNo);
					setASPS(latchASPS);
				}
				  
			},0,1000);

		}
	}
	
	public void setUpSimulationContolPanel()
	{
		JPanel simulationControlPanel = new JPanel();
		add(simulationControlPanel, BorderLayout.SOUTH);
		simulationControlPanel.setBorder(new TitledBorder(null, "Control", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		GridBagLayout gbl_simulationControlPanel = new GridBagLayout();
		gbl_simulationControlPanel.columnWidths = new int[]
		{112, 112, 112};
		gbl_simulationControlPanel.rowHeights = new int[]
		{31, 31, 31, 31};
		gbl_simulationControlPanel.columnWeights = new double[]
		{1.0, 1.0, 1.0};
		gbl_simulationControlPanel.rowWeights = new double[]
		{0.0, 0.0, 0.0, 0.0};
		simulationControlPanel.setLayout(gbl_simulationControlPanel);

		JLabel lblAverageStepRate = new JLabel("Average Step Rate");
		lblAverageStepRate.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_lblAverageStepRate = new GridBagConstraints();
		gbc_lblAverageStepRate.fill = GridBagConstraints.BOTH;
		gbc_lblAverageStepRate.insets = new Insets(0, 0, 5, 5);
		gbc_lblAverageStepRate.gridx = 0;
		gbc_lblAverageStepRate.gridy = 0;
		simulationControlPanel.add(lblAverageStepRate, gbc_lblAverageStepRate);

		JLabel label_1 = new JLabel("Steps");
		label_1.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_label_1 = new GridBagConstraints();
		gbc_label_1.fill = GridBagConstraints.BOTH;
		gbc_label_1.insets = new Insets(0, 0, 5, 5);
		gbc_label_1.gridx = 1;
		gbc_label_1.gridy = 0;
		simulationControlPanel.add(label_1, gbc_label_1);

		JLabel label_2 = new JLabel("Requested Step Rate");
		label_2.setHorizontalAlignment(SwingConstants.CENTER);
		GridBagConstraints gbc_label_2 = new GridBagConstraints();
		gbc_label_2.fill = GridBagConstraints.BOTH;
		gbc_label_2.insets = new Insets(0, 0, 5, 0);
		gbc_label_2.gridx = 2;
		gbc_label_2.gridy = 0;
		simulationControlPanel.add(label_2, gbc_label_2);

		JPanel simAverageStepRate = new JPanel();
		simAverageStepRate.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		simAverageStepRate.setBackground(Color.WHITE);
		GridBagConstraints gbc_simAverageStepRate = new GridBagConstraints();
		gbc_simAverageStepRate.fill = GridBagConstraints.BOTH;
		gbc_simAverageStepRate.insets = new Insets(0, 0, 5, 5);
		gbc_simAverageStepRate.gridx = 0;
		gbc_simAverageStepRate.gridy = 1;
		simulationControlPanel.add(simAverageStepRate, gbc_simAverageStepRate);
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
		simulationControlPanel.add(simStepTotal, gbc_simStepTotal);
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
		simulationControlPanel.add(simRequestedStepRate, gbc_simRequestedStepRate);
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
		simulationControlPanel.add(label_3, gbc_label_3);

		JPanel simRunTime = new JPanel();
		simRunTime.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		simRunTime.setBackground(Color.WHITE);
		GridBagConstraints gbc_simRunTime = new GridBagConstraints();
		gbc_simRunTime.fill = GridBagConstraints.BOTH;
		gbc_simRunTime.insets = new Insets(0, 0, 5, 5);
		gbc_simRunTime.gridx = 1;
		gbc_simRunTime.gridy = 2;
		simulationControlPanel.add(simRunTime, gbc_simRunTime);
		simRunTime.setLayout(new BorderLayout(0, 0));

		lblSimRunTime = new JLabel("0");
		lblSimRunTime.setHorizontalAlignment(SwingConstants.CENTER);
		simRunTime.add(lblSimRunTime, BorderLayout.CENTER);

		sliderSimStepRate = new JSlider();
				
		sliderSimStepRate.setSnapToTicks(true);
		sliderSimStepRate.addChangeListener(this);
		
		// If there is a sim set the slider to its step rate - otherwise default to 15sps
		if(simId!=-1)
		{
			sliderSimStepRate.setValue(spsToSliderVal(simsManager.getReqSps(simId)));
		}
		else
		{
			sliderSimStepRate.setValue(spsToSliderVal(15));
		}
		
		sliderSimStepRate.setToolTipText("Adjust requested step rate.");
		sliderSimStepRate.setPreferredSize(new Dimension(25, 20));
		sliderSimStepRate.setPaintTicks(true);
		sliderSimStepRate.setMaximum(16);
		sliderSimStepRate.setMajorTickSpacing(4);
		sliderSimStepRate.setEnabled(false);
		GridBagConstraints gbc_sliderSimStepRate = new GridBagConstraints();
		gbc_sliderSimStepRate.fill = GridBagConstraints.BOTH;
		gbc_sliderSimStepRate.insets = new Insets(0, 0, 5, 0);
		gbc_sliderSimStepRate.gridx = 2;
		gbc_sliderSimStepRate.gridy = 2;
		simulationControlPanel.add(sliderSimStepRate, gbc_sliderSimStepRate);

		btnGenerateSim = new JButton("Generate");
		btnGenerateSim.setIcon(generateSimIcon);
		btnGenerateSim.addActionListener(this);
		btnGenerateSim.setToolTipText("Generate a new simuation based on the values of the parameters.");
		btnGenerateSim.setEnabled(false);
		GridBagConstraints gbc_btnGenerateSim = new GridBagConstraints();
		gbc_btnGenerateSim.fill = GridBagConstraints.BOTH;
		gbc_btnGenerateSim.insets = new Insets(0, 0, 0, 5);
		gbc_btnGenerateSim.gridx = 0;
		gbc_btnGenerateSim.gridy = 3;
		simulationControlPanel.add(btnGenerateSim, gbc_btnGenerateSim);

		btnStartSim = new JButton("Start");
		btnStartSim.setIcon(startSimIcon);
		btnStartSim.addActionListener(this);
		btnStartSim.setToolTipText("Start the simulation.");
		btnStartSim.setEnabled(false);
		GridBagConstraints gbc_btnStartSim = new GridBagConstraints();
		gbc_btnStartSim.fill = GridBagConstraints.BOTH;
		gbc_btnStartSim.insets = new Insets(0, 0, 0, 5);
		gbc_btnStartSim.gridx = 1;
		gbc_btnStartSim.gridy = 3;
		simulationControlPanel.add(btnStartSim, gbc_btnStartSim);

		btnPauseSim = new JButton("   Pause");
		btnPauseSim.setIcon(resumeSimIcon);
		btnPauseSim.addActionListener(this);
		btnPauseSim.setToolTipText("Pause / Unpause the simulation.");
		btnPauseSim.setEnabled(false);
		GridBagConstraints gbc_btnPauseSim = new GridBagConstraints();
		gbc_btnPauseSim.fill = GridBagConstraints.BOTH;
		gbc_btnPauseSim.gridx = 2;
		gbc_btnPauseSim.gridy = 3;
		simulationControlPanel.add(btnPauseSim, gbc_btnPauseSim);
	}
	
	public void setUpScenarioEditorTab()
	{
		// Scenario Tab
		simulationScenarioTab = new JPanel();
		simulationScenarioTab.setLayout(new BorderLayout(0, 0));	
		addScenarioTab();
		
		JPanel scenarioEditorContainerPanel = new JPanel();
		simulationScenarioTab.add(scenarioEditorContainerPanel, BorderLayout.CENTER);
		scenarioEditorContainerPanel.setLayout(new BorderLayout(0, 0));

		JPanel scenarioEditorButtonPanel = new JPanel();
		scenarioEditorButtonPanel.setBorder(null);
		scenarioEditorContainerPanel.add(scenarioEditorButtonPanel, BorderLayout.SOUTH);
		GridBagLayout gbl_scenarioEditorButtonPanel = new GridBagLayout();

		gbl_scenarioEditorButtonPanel.rowHeights = new int[]
		{0};
		gbl_scenarioEditorButtonPanel.columnWidths = new int[] {0, 0};
		gbl_scenarioEditorButtonPanel.columnWeights = new double[]
		{1.0, 1.0};
		gbl_scenarioEditorButtonPanel.rowWeights = new double[]
		{1.0};
		scenarioEditorButtonPanel.setLayout(gbl_scenarioEditorButtonPanel);

		btnOpen = new JButton("Open");
		btnOpen.setIcon(openScenarioIcon);
		btnOpen.addActionListener(this);
		GridBagConstraints gbc_btnOpen = new GridBagConstraints();
		gbc_btnOpen.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnOpen.insets = new Insets(0, 0, 0, 0);
		gbc_btnOpen.gridx = 0;
		gbc_btnOpen.gridy = 0;
		scenarioEditorButtonPanel.add(btnOpen, gbc_btnOpen);

		btnSave = new JButton("Save");
		btnSave.setIcon(saveScenarioIcon);
		btnSave.addActionListener(this);
		GridBagConstraints gbc_btnSave = new GridBagConstraints();
		gbc_btnSave.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSave.gridx = 1;
		gbc_btnSave.gridy = 0;
		scenarioEditorButtonPanel.add(btnSave, gbc_btnSave);
		btnSave.setEnabled(false);

		JPanel scenarioEditorPanel = new JPanel();
		scenarioEditorContainerPanel.add(scenarioEditorPanel, BorderLayout.CENTER);
		scenarioEditorPanel.setLayout(new BorderLayout(0, 0));

		JPanel scenarioEditorTopPanel = new JPanel();
		scenarioEditorTopPanel.setLayout(new BorderLayout(0, 0));

		lblFilePath = new JLabel("No File");
		scenarioEditorTopPanel.add(lblFilePath);

		chckbxEditMode = new JCheckBox("EditMode");
		chckbxEditMode.addChangeListener(this);
		scenarioEditorTopPanel.add(chckbxEditMode, BorderLayout.EAST);
		
		scenarioEditor = new RSyntaxTextArea();
		scenarioEditor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);

		scenarioEditor.setCloseMarkupTags(true);
		scenarioEditor.setCloseCurlyBraces(false);
		scenarioEditor.setAnimateBracketMatching(true);
		scenarioEditor.setUseSelectedTextColor(true);
		scenarioEditor.setHyperlinksEnabled(false);
		scenarioEditor.setHighlightSecondaryLanguages(false);
		scenarioEditor.setRoundedSelectionEdges(true);
		scenarioEditor.setAutoIndentEnabled(true);
		scenarioEditor.setTabSize(2);
		scenarioEditor.setFadeCurrentLineHighlight(true);
		scenarioEditor.setBracketMatchingEnabled(false);
		scenarioEditor.setEditable(false);

        Theme theme;
        InputStream in; 
		try
		{
			in = new FileInputStream(new File("S:/AlifeSimWorkSpace/alifesim/editor-themes/dark-mod.xml"));
			theme = Theme.load(in);
			theme.apply(scenarioEditor);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		normalMode = scenarioEditor.getBackground();
		editMode = normalMode.darker();
		scenarioEditor.setFont(new Font("Monospaced", Font.BOLD, 12));

		RTextScrollPane scenarioEditorRTextScrollPane = new RTextScrollPane(scenarioEditor);
		scenarioEditorPanel.add(scenarioEditorRTextScrollPane, BorderLayout.CENTER);
		scenarioEditorPanel.add(scenarioEditorTopPanel, BorderLayout.NORTH);
		
	}
	
	public void addSimulationStatsListTab()
	{
		simulationTabPane.addTab("Supported Statistics", simulationStatsListPanel);
		simulationStatsListPanel.clearTable();
		
		if(statManager!=null)
		{
			Set<String> statGroups = statManager.getGroupList();
			
			for (String group : statGroups)
			{
				simulationStatsListPanel.addRow(group,new String[]{Integer.toString(statManager.getStatGroup(group).getStatList().size()), String.valueOf(statManager.getStatGroup(group).getGroupSettings().statsEnabled()),String.valueOf(statManager.getStatGroup(group).getGroupSettings().graphEnabled())});
			}
			
			// Give the List Panel a reference to the simulations stat manager - so it can initiate an export.
			simulationStatsListPanel.setStatManager(statManager);
		
			simulationTabPane.setIconAt(simulationTabPane.getTabCount() - 1, simulationStatsExportIcon);			
		}

	}

	public void addScenarioTab()
	{
		simulationTabPane.addTab("Scenario", null, simulationScenarioTab, null);
		simulationTabPane.setIconAt(simulationTabPane.getTabCount() - 1, scenarioEditorIcon);
	}

	private boolean discardCurrentSimGenerated()
	{
		if (simGenerated)
		{
			// prompt to save
			String message = "Discard Running Simulation?";

			JOptionPane pane = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);

			// Center Dialog on the GUI
			JDialog dialog = pane.createDialog(this, "Discard Running Simulation");

			dialog.pack();
			dialog.setVisible(true);

			int value = ((Integer) pane.getValue()).intValue();

			if (value == JOptionPane.YES_OPTION)
			{
				simGenerated = false;
				
				return true;
			}
			else
			{
				return false;
			}

		}
		return true;
	}

	private void checkSaved()
	{
		if (!saved)
		{
			// prompt to save
			String message = "Do you want to Save?";

			JOptionPane pane = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);

			// Center Dialog on the GUI
			JDialog dialog = pane.createDialog(this, "Save Scenario");

			dialog.pack();
			dialog.setVisible(true);

			int value = ((Integer) pane.getValue()).intValue();

			if (value == JOptionPane.YES_OPTION)
			{
				saveScenario();
			}

		}
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		// Open Scenario
		if (e.getSource() == btnOpen)
		{
			openScenario();
		}
		else if (e.getSource() == btnGenerateSim)
		{
			if(discardCurrentSimGenerated())
			{
				if (scenarioLoaded)
				{
					/* Not already generating Sim */
					if (!generatingSim)
					{
						generatingSim = true;
						
						/* Create the new Simulation */
						if(newSim(scenarioEditor.getText()))
						{

							simGenerated = true;
							
							clearStats();
							
							// A slow timer to update GUI at a rate independent of SimulationStatChanged notifications.
							updateTimer = new Timer("GUI Stat Update Timer");
							updateTimer.schedule(new TimerTask()
							{
								@Override
								public void run() 
								{
									setTime(latchTime);
									setStepNo(latchStepNo);
									setASPS(latchASPS);
								}
								  
							},0,1000);
						}

					}
					generatingSim = false;
				}
			}
			
		}
		else if (e.getSource() == btnPauseSim)
		{
			simsManager.togglePause(simId);
		}
		else if (e.getSource() == btnStartSim)
		{
			simsManager.startSim(simId);
		}
		else if (e.getSource() == btnSave)
		{
			saveScenario();
		}
		else
		{
			System.out.println("Button " + ((JButton) e.getSource()).getText() + " Not Implemented");
		}

	}

	private void openScenario()
	{
		if (discardCurrentSimGenerated())
		{
			checkSaved();

			final JFileChooser filechooser = new JFileChooser(new File("./scenarios"));

			System.out.println("Scenario Open Dialog");

			int val = filechooser.showOpenDialog(filechooser);

			if (val == JFileChooser.APPROVE_OPTION)
			{
				System.out.println("New Scenario Choosen");
				
				detachTabFromSim();
				
				removeSimulation();
				
				// File scenarioFile = filechooser.getSelectedFile();
				lblFilePath.setText(filechooser.getSelectedFile().getName());
				BufferedReader bufferedReader;

				try
				{
					bufferedReader = new BufferedReader(new FileReader(filechooser.getSelectedFile()));
					String sCurrentLine;
					scenarioEditor.setText("");

					while ((sCurrentLine = bufferedReader.readLine()) != null)
					{
						scenarioEditor.append(sCurrentLine + "\n");
					}

					// Switch off Edit mode
					chckbxEditMode.setSelected(false);
					saved = true;
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
				btnSave.setEnabled(true);
				
				// Set the Startup State
				startUpState();

				tabTitle = "Loaded Scenario";
				
			}
		}

	}

	private void saveScenario()
	{
		System.out.println("Save Scenario");
		chckbxEditMode.setSelected(false);

		final JFileChooser filechooser = new JFileChooser(new File("./scenarios"));

		String fileName = lblFilePath.getText();

		if (fileName.equals("No File"))
		{
			fileName = "NewScenario";
		}
		filechooser.setSelectedFile(new File("./scenarios/" + fileName));

		System.out.println("Choose File");
		int val = filechooser.showSaveDialog(filechooser);

		if (val == JFileChooser.APPROVE_OPTION)
		{
			File file = filechooser.getSelectedFile();

			fileName = file.getAbsolutePath().toString();

			System.out.println("Save : " + fileName);

			try
			{
				System.out.println("Saving : " + fileName);

				if (fileName.indexOf(".") > 0)
				{
					fileName = fileName.substring(0, fileName.lastIndexOf("."));
				}

		       // GZIPOutputStream gzip = new GZIPOutputStream(new FileOutputStream(new File(fileName + ".xmlz")));
				
				FileWriter fileWriter = new FileWriter(fileName + ".xml");
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
				
		       // BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(gzip, "UTF-8"));
		        
		        
				bufferedWriter.write(scenarioEditor.getText());
				bufferedWriter.close();

				System.out.println("Saved : " + fileName);
				saved = true;
				
				lblFilePath.setText(filechooser.getSelectedFile().getName()+".xml");
				
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}

		}
		else
		{
			System.out.println("Save Cancelled");
		}

	}

	private ScenarioInf determinScenarios(String text)
	{
		ScenarioVT scenarioParser = null;

		ScenarioInf simScenario = null;

		scenarioParser = new ScenarioVT();

		// To get the type of Scenario object to create.
		scenarioParser.loadConfig(text);

		System.out.println("Scenario Type : " + scenarioParser.getScenarioType());

		if (scenarioParser.getScenarioType().equalsIgnoreCase("DEBUG"))
		{
			System.out.println("Debug File");
			simScenario = new DebugScenario(text);
		}
		else
		{
			if (scenarioParser.getScenarioType().equalsIgnoreCase("SAPP"))
			{
				System.out.println("SAPP File");
				simScenario = new SAPPScenario();

				simScenario.loadConfig(text);

			}
			else if(scenarioParser.getScenarioType().equalsIgnoreCase("LV"))
			{
				System.out.println("LV File");
				simScenario = new LotkaVolterraScenario();

				simScenario.loadConfig(text);
			}
			else
			{
				System.out.println("DeterminScenarios :UKNOWN");
			}
		}

		return simScenario;
	}

	private void addPanels()
	{		
		// Re-add the Scenario Tab.
		addScenarioTab();

		// Re-add the Scenario Tab
		addSimulationStatsListTab();

		addGraphsPanel();		
	}

	private void addGraphsPanel()
	{		
		simulationTabPane.addTab("Charts", graphsTabPanel);
		simulationTabPane.setIconAt(simulationTabPane.getTabCount() - 1, simulationStatChartIcon);
		
		// Re-add the ChartTabs
		addChartTabs();
	}

	private void addChartTabs()
	{		
		if(statManager!=null)
		{
			Set<String> statGroups = statManager.getGroupList();

			LinkedList<GlobalStatChartPanel> charts = new  LinkedList<GlobalStatChartPanel>();
			
			// Collect the enabled Charts
			for (String group : statGroups)
			{
				StatGroup statGroup = statManager.getStatGroup(group);
				
				if(statGroup.getGroupSettings().graphEnabled())
				{
					GlobalStatChartPanel chart = new GlobalStatChartPanel(group,statGroup.getGroupSettings().hasTotalStat(),statGroup.getGroupSettings().getGraphSampleWindow());
													
					statGroup.addStatGroupListener(chart);
					
					charts.add(chart);
				}

			}
			
			graphsTabPanel.addCharts(charts);			
		}
		
	}

	private void registerListeners()
	{
		System.out.println("Register Listeners");
		// State / Stats
		simsManager.addSimulationStateListener(simId, this);
		simsManager.addSimulationStatListener(simId, this);
		this.addTabStatusListener(title);
	}
	
	private void setSimView()
	{
		simsManager.setActiveSim(simId);
		
		simsManager.resetActiveSimCamera();
	}
	
	private boolean newSim(String scenario)
	{
		boolean status = false;
		ScenarioInf simScenario = null;

		detachTabFromSim();
		
		removeSimulation();
				
		// Add a new sim and direct its performance stats to this panel.
		simId = simsManager.addSimulation();
		
		// > 1 if we added a sim
		if(simId!=-1)
		{
			tabTitle = "Simulation " + simId;
			
			simScenario = determinScenarios(scenario);
	
			if (simScenario != null)
			{
				System.out.println("Creating Sim");
					
				simsManager.createSimScenario(simId,simScenario);
	
				statManager = simsManager.getStatManager(simId);
					
				setSimView();
				
				addPanels();
	
				setStepRate(sliderSimStepRate.getValue());
				
				status = true;
				
				registerListeners();
				
			}
			else
			{
				JOptionPane.showMessageDialog(this, "Failed to generate simulation.\nCheck XML Syntax.");
				
				System.out.println("Scenario Failed to Load");
				
				detachTabFromSim();
				
				removeSimulation();
				
			}
		}
		else
		{
			JOptionPane.showMessageDialog(this, "The Limit of " + simsManager.getMaxSims() + " active sims has been reached.");
		}

		return status;
	}

	private int spsToSliderVal(int reqSps)
	{
		int sliderVal = -1;
		
		switch(reqSps)
		{
			case 1:
				sliderVal = 1;
			break;
			case 5:
				sliderVal = 4;
			break;
			case 15:
				sliderVal = 8;
			break;
			case 60:
				sliderVal = 12;
			break;
			case 0:
				sliderVal = 16;
			break;
			default :
				sliderVal = 8;
			break;			
		}	
		
		return sliderVal;
	}
	
	private void setStepRate(int sliderVal)
	{		
		switch(sliderVal)
		{
			case 1:
				lblRequestedStepRate.setText("1");
				simsManager.setReqSimStepRate(simId,1);
			break;
			case 4:
				lblRequestedStepRate.setText("5");
				simsManager.setReqSimStepRate(simId,5);
			break;
			case 8:
				lblRequestedStepRate.setText("15");
				simsManager.setReqSimStepRate(simId,15);
			break;
			case 12:
				lblRequestedStepRate.setText("60");
				simsManager.setReqSimStepRate(simId,60);
			break;
			case 16:
				lblRequestedStepRate.setText("Unli");
				simsManager.setReqSimStepRate(simId,0);
			break;
			default :
				lblRequestedStepRate.setText("Selecting");
			break;
		}
	}
		
	@Override
	public void stateChanged(ChangeEvent e)
	{
		if (e.getSource() == sliderSimStepRate)
		{

			setStepRate(sliderSimStepRate.getValue());		

		}
		else if (e.getSource() == chckbxEditMode)
		{
			if (chckbxEditMode.isSelected())
			{
				scenarioEditor.setEditable(true);
				scenarioEditor.setBackground(editMode);

				saved = false;
				btnSave.setEnabled(true);
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

	public void clearStats()
	{
		System.out.println("Simulation Stats Cleared");
		setASPS(0);
		setStepNo(0);
		setTime(0);
	}

	public int getSimulationId()
	{
		return simId;
	}

	private void removeSimulation()
	{
		if(simId != -1)
		{
			System.out.println("Request to Remove Simulation");

			simsManager.removeSimulation(simId);
			
			simId=-1;
				
			System.out.println("Simulation Removed");	
		}
		else
		{
			System.out.println("No Sim to Remove");
		}
		
		tabTitle = "No Sim";		
	}
	
	private String getTitle()
	{
		return tabTitle;
	}

	/**
	 * The Average Steps per second.
	 * @param asps
	 */
	private void setASPS(int asps)
	{
		lblAvgStepRate.setText(Integer.toString(asps));
	}

	/**
	 * The current step number.
	 * @param stepNo
	 */
	private void setStepNo(int stepNo)
	{
		lblStepCount.setText(Integer.toString(stepNo));
	}

	/**
	 * Displays the current run time of the simulation from a long count in
	 * milliseconds
	 * @param time
	 */
	private void setTime(long time)
	{
		time = time / 1000; // seconds
		int days = (int) (time / 86400); // to days
		int hrs = (int) (time / 3600) % 24; // to hrs
		int mins = (int) ((time / 60) % 60);	// to seconds
		int sec = (int) (time % 60);

		lblSimRunTime.setText(String.format("%d:%02d:%02d:%02d", days, hrs, mins, sec));
	}
	
	private void notifiyTabStatusChangedListeners(SimState state)
	{
		listenersLock.acquireUninterruptibly();
	    for (TabStatusChangedListenerInf listener : tabStatusListeners)
	    {
	    	listener.tabStatusChanged(this,state);
	    }
	    listenersLock.release();
	}
	
	private void addTabStatusListener(TabStatusChangedListenerInf listener)
	{
		listenersLock.acquireUninterruptibly();
	    	tabStatusListeners.add(listener);
    	listenersLock.release();

    	/*
    	 * Needed to avoid race condition where by this tab does not exist in the tab panel
    	 * and a listener has been added when the tab has just been added added to a tab panel manager.
    	 */
	    javax.swing.SwingUtilities.invokeLater(new Runnable() 
	    {
			@Override
			public void run()
			{
		    	if(simId!=-1)
		    	{
					SimState state = simsManager.getState(simId);
					
					notifiyTabStatusChangedListeners(state);
		    	}
			}
	    	
	    });
    		
    	   	
	}
	
	private void removeTabStatusListener(TabStatusChangedListenerInf listener)
	{
		listenersLock.acquireUninterruptibly();
	    	tabStatusListeners.remove(listener);
    	listenersLock.release();
	}

	public void detachTabFromSim()
	{
		System.out.println("Detaching Tab from Simulation");
		if(updateTimer!=null)
		{
			updateTimer.cancel();
			updateTimer = null;
		}
		
		removeChartPanel();
		
		removeTabStatusListener(title);
		
		simsManager.removeSimulationStateListener(simId, this);
		
		simsManager.removeSimulationStatListener(simId, this);
	}

	private void removeChartPanel()
	{				
		// Clean up our tabs which are listening to state groups
		removeChartTabs();
		
		simulationTabPane.remove(graphsTabPanel);
	}
	
	private void removeChartTabs()
	{		
		graphsTabPanel.clearCharts(statManager);
		
		return;		
	}

	@Override
	public void simulationStateChanged(int simId, SimState state)
	{
		setGuiState(state);
	}

	private void setGuiState(SimState state)
	{
		switch(state)
		{
			case RUNNING:
				simRunningState();
			break;
			case NEW:
				generatedState();
			break;
			case FINISHED:
				finishedState();
			break;
			case PAUSED:
				simPausedState();
			break;	
			default :
				
				for(int i=0;i<100;i++)
				{
					System.out.println("Invalid/Unhandled SimState passed to GUI");
				}
				
			break;
		}
		
		notifiyTabStatusChangedListeners(state);
	}
	
	private void simRunningState()
	{
		System.out.println("GUI now in Running State");
		
		btnGenerateSim.setEnabled(false);

		btnStartSim.setEnabled(false);

		btnPauseSim.setEnabled(true);
		
		btnPauseSim.setText("   Pause");
		
		btnPauseSim.setIcon(pauseSimIcon);

		sliderSimStepRate.setEnabled(true);
		
		simulationStatsListPanel.setExportEnabled(false);
	
	}
	
	private void simPausedState()
	{
		System.out.println("GUI now in Paused State");
		
		btnPauseSim.setText("Resume");
		btnPauseSim.setEnabled(true);
		btnGenerateSim.setEnabled(true);	

		btnPauseSim.setIcon(resumeSimIcon);
		
		simulationStatsListPanel.setExportEnabled(true);
		
	}
	
	private void startUpState()
	{
		System.out.println("GUI now in Startup State");
		
		btnStartSim.setEnabled(false);
		sliderSimStepRate.setEnabled(false);
		btnPauseSim.setEnabled(false);
		btnGenerateSim.setEnabled(true);
	}
	
	private void generatedState()
	{
		System.out.println("GUI now in Generated State");

		btnGenerateSim.setEnabled(true);

		btnStartSim.setEnabled(true);
		
		btnPauseSim.setEnabled(false);
		btnPauseSim.setText("   Pause");
		btnPauseSim.setIcon(pauseSimIcon);
		
		sliderSimStepRate.setEnabled(false);	
	}
	
	
	private void finishedState()
	{
		System.out.println("GUI now in Finished State");
		
		btnStartSim.setEnabled(false);
		sliderSimStepRate.setEnabled(false);
		btnPauseSim.setEnabled(false);
		btnGenerateSim.setEnabled(true);
						
		simulationStatsListPanel.setExportEnabled(true);
	}
	
	@Override
	public void simulationStatChanged(int simId, long time, int stepNo, int progress, int asps)
	{
		latchTime = time;
		latchStepNo = stepNo;
		latchASPS = asps;	
	}

	
	public TabButton getTabTitle()
	{
		return title;
	}
	
	private class TabButton extends JPanel implements TabStatusChangedListenerInf
	{
		private GUITabManager tabManager;
		private GUISimulationTab tab;
		private ImageIcon icon;
		private JLabel title;
		JButton closeButton;
		
		public TabButton(final GUITabManager tabManager,final GUISimulationTab tab) 
		{
			this.tabManager = tabManager;
			
			this.tab = tab;
			
			this.setLayout(new FlowLayout(FlowLayout.CENTER, 1, 2));
			
			this.setOpaque(false);
									
			title = new JLabel(tab.getTitle());
			title.setIcon(simNewIcon);
						
			this.add(title);
			
			closeButton = new JButton(Character.toString((char) 0x2573));
			closeButton.setPreferredSize(new Dimension(16,16));
			closeButton.setBorder(null);
			closeButton.setFocusable(false);
			
			closeButton.addMouseListener(new MouseAdapter()
			{
				@Override
				public void mouseClicked(MouseEvent e)
				{
					tabManager.setSelectedComponent(tab);
					tabManager.closeTab();
					
					//System.gc();
				}
			});
			
			this.add(closeButton);
		
		}
		
		@Override
		public void tabStatusChanged(GUISimulationTab tab,SimState state)
		{
			//int index = tabManager.indexOfComponent(tab);
			
		  	if(state == SimState.RUNNING)
		  	{
		  		//tabManager.setIconAt(index, simRunningIcon);		
		  		title.setIcon(simRunningIcon);
		  		title.setText(tab.getTitle());
		  	}
		  	else if(state == SimState.PAUSED)
		  	{
		  		//tabManager.setIconAt(index, simPausedIcon);	
		  		title.setIcon(simPausedIcon);
		  		title.setText(tab.getTitle());

						  		
		  	}
		  	else if(state == SimState.NEW)
		  	{
		  	//	tabManager.setIconAt(index, simNewIcon);	
		  		title.setIcon(simNewIcon);
		  		title.setText(tab.getTitle());
		  	}
		  	else // Finished
		  	{
		  		//tabManager.setIconAt(index, simFinishedIcon);	
		  		title.setIcon(simFinishedIcon);
		  		title.setText(tab.getTitle());
		  	}
		  	
		  	//tabManager.setTitleAt(index, tab.getTitle());
		  	this.repaint();
		}

	}

	public void destroy()
	{
		System.out.println("Destroying Tab for Sim " + simId);
		detachTabFromSim();
	
		removeSimulation();
	}
	
}
