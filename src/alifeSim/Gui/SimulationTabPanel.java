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
import javax.swing.JDialog;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.Font;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

import alifeSim.ChartPanels.GlobalStatChartPanel;
import alifeSim.ChartPanels.StatPanelAbs;
import alifeSim.Scenario.ScenarioInf;
import alifeSim.Scenario.ScenarioVT;
import alifeSim.Scenario.Debug.DebugScenario;
import alifeSim.Scenario.Math.LVScenario;
import alifeSim.Scenario.SAPP.SAPPScenario;
import alifeSim.Simulation.SimulationState.SimStatus;
import alifeSim.Simulation.SimulationStatListenerInf;
import alifeSim.Simulation.SimulationStatusListenerInf;
import alifeSim.Simulation.SimulationsManager;
import alifeSim.Stats.StatGroup;
import alifeSim.Stats.StatManager;

public class SimulationTabPanel extends JPanel implements ActionListener, ChangeListener, SimulationStatListenerInf, SimulationStatusListenerInf
{
	private static final long serialVersionUID = 5391587818992199457L;
	
	private String tabTitle = "New";
	
	// Graphs
	private JTabbedPane simulationTabPane;
	private LinkedList<StatPanelAbs> charts;

	// Editor Related
	private RSyntaxTextArea scenarioEditor;
	private JLabel lblFilePath;
	private JButton btnOpen;
	private JButton btnSave;
	private JButton btnClose;
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

	// Sim Related
	private boolean generatingSim = false;
	private boolean simGenerated = false;

	private JPanel simulationScenarioTab;
	private SimulationStatsListPanel simulationStatsListPanel;

	private SimulationsManager simsManager;
	
	private int simId = -1;

	private Timer updateTimer = new Timer();
	private boolean allowUpdate = false;
	
	/** Scenario Editor Icons */
	private ImageIcon openScenarioIcon = new ImageIcon(SimulationTabPanel.class.getResource("/alifeSim/icons/document-open.png"));
	private ImageIcon saveScenarioIcon = new ImageIcon(SimulationTabPanel.class.getResource("/alifeSim/icons/document-save.png"));
	private ImageIcon closeScenarioIcon = new ImageIcon(SimulationTabPanel.class.getResource("/alifeSim/icons/document-close.png"));
	
	private ImageIcon generateSimIcon = new ImageIcon(SimulationTabPanel.class.getResource("/alifeSim/icons/grid.png"));
	private ImageIcon startSimIcon = new ImageIcon(SimulationTabPanel.class.getResource("/alifeSim/icons/play.png"));
	private ImageIcon resumeSimIcon = new ImageIcon(SimulationTabPanel.class.getResource("/alifeSim/icons/resume.png"));
	private ImageIcon pauseSimIcon = new ImageIcon(SimulationTabPanel.class.getResource("/alifeSim/icons/pause.png"));

	private ImageIcon simulationStatsExportIcon = new ImageIcon(SimulationTabPanel.class.getResource("/alifeSim/icons/kspread.png"));
	private ImageIcon scenarioEditorIcon = new ImageIcon(SimulationTabPanel.class.getResource("/alifeSim/icons/accessories-text-editor.png"));
	private ImageIcon simulationStatChartIcon = new ImageIcon(SimulationTabPanel.class.getResource("/alifeSim/icons/kchart.png"));
		
	private List<TabStatusChangedListenerInf> tabStatusListeners = new ArrayList<TabStatusChangedListenerInf>();
	private Semaphore listenersLock = new Semaphore(1, false);
	
	public SimulationTabPanel(SimulationsManager simsManager)
	{
		this.simsManager = simsManager;
		
		simId = -1;
				
		setLayout(new BorderLayout(0, 0));

		simulationTabPane = new JTabbedPane(JTabbedPane.TOP);
		simulationTabPane.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
		add(simulationTabPane, BorderLayout.CENTER);

		simulationScenarioTab = new JPanel();
		addScenarioTab();
		simulationScenarioTab.setLayout(new BorderLayout(0, 0));

		simulationStatsListPanel = new SimulationStatsListPanel();
		addSimulationStatsListTab();

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
		btnOpen.setIcon(openScenarioIcon);
		btnOpen.addActionListener(this);
		GridBagConstraints gbc_btnOpen = new GridBagConstraints();
		gbc_btnOpen.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnOpen.insets = new Insets(0, 0, 0, 0);
		gbc_btnOpen.gridx = 0;
		gbc_btnOpen.gridy = 0;
		scenarioOpenPanel.add(btnOpen, gbc_btnOpen);

		btnSave = new JButton("Save");
		btnSave.setIcon(saveScenarioIcon);
		btnSave.addActionListener(this);
		GridBagConstraints gbc_btnSave = new GridBagConstraints();
		gbc_btnSave.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSave.gridx = 1;
		gbc_btnSave.gridy = 0;
		scenarioOpenPanel.add(btnSave, gbc_btnSave);
		btnSave.setEnabled(false);

		btnClose = new JButton("Close");
		btnClose.setIcon(closeScenarioIcon);
		btnClose.addActionListener(this);
		btnClose.setEnabled(false);
		GridBagConstraints gbc_btnClose = new GridBagConstraints();
		gbc_btnClose.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnClose.gridx = 2;
		gbc_btnClose.gridy = 0;
		scenarioOpenPanel.add(btnClose, gbc_btnClose);

		JPanel scenarioFilePanel = new JPanel();
		scenarioPanel.add(scenarioFilePanel, BorderLayout.CENTER);
		scenarioFilePanel.setLayout(new BorderLayout(0, 0));

		JPanel filePanel = new JPanel();
		filePanel.setLayout(new BorderLayout(0, 0));

		lblFilePath = new JLabel("No File");
		filePanel.add(lblFilePath);

		chckbxEditMode = new JCheckBox("EditMode");
		chckbxEditMode.addChangeListener(this);
		filePanel.add(chckbxEditMode, BorderLayout.EAST);
		
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
		sliderSimStepRate.setSnapToTicks(true);
		sliderSimStepRate.addChangeListener(this);

		sliderSimStepRate.setValue(4);
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
		controlPanel.add(sliderSimStepRate, gbc_sliderSimStepRate);

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
		controlPanel.add(btnGenerateSim, gbc_btnGenerateSim);

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
		controlPanel.add(btnStartSim, gbc_btnStartSim);

		btnPauseSim = new JButton("   Pause");
		btnPauseSim.setIcon(resumeSimIcon);
		btnPauseSim.addActionListener(this);
		btnPauseSim.setToolTipText("Pause / Unpause the simulation.");
		btnPauseSim.setEnabled(false);
		GridBagConstraints gbc_btnPauseSim = new GridBagConstraints();
		gbc_btnPauseSim.fill = GridBagConstraints.BOTH;
		gbc_btnPauseSim.gridx = 2;
		gbc_btnPauseSim.gridy = 3;
		controlPanel.add(btnPauseSim, gbc_btnPauseSim);
		
		// A slow timer to update GUI at a rate independent of SimulationStatChanged notifications.
		updateTimer.schedule(new TimerTask()
		{
			  @Override
			  public void run() 
			  {
				  allowUpdate = true;
			  }
			  
		},0,1000);
		
	}

	public void addSimulationStatsListTab()
	{
		simulationTabPane.addTab("Supported Statistics", simulationStatsListPanel);
		simulationStatsListPanel.clearTable();

		StatManager statManager = simsManager.getStatManager(simId);
		
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
			if (scenarioLoaded)
			{
				/* Not already generating Sim */
				if (!generatingSim)
				{
					generatingSim = true;

					/* Create the new Simulation */
					newSim(scenarioEditor.getText());

					generatingSim = false;

					simGenerated = true;

				}
			}

		}
		else if (e.getSource() == btnPauseSim)
		{			
			// Pause Toggle
			SimStatus status = simsManager.togglePause(simId);
			
			if(status == SimStatus.PAUSED)
			{
				simPausedStatus();
			}
			else if(status == SimStatus.RUNNING)
			{
				simUnPausedStatus();
			}
			else
			{
				System.out.println("Invalid Status in Pause Button");
			}

		}
		else if (e.getSource() == btnStartSim)
		{
			simStartedState();
		}
		else if (e.getSource() == btnSave)
		{
			saveScenario();
		}
		else if (e.getSource() == btnClose)
		{
			closeScenario();
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
				
				simsManager.removeSimulation(simId);
				
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
				btnClose.setEnabled(true);
				btnSave.setEnabled(true);
				// Set the Startup State
				startUpState();

			}
		}

	}

	private void closeScenario()
	{
		String message;
		message = "Close Scenario?";

		JOptionPane pane = new JOptionPane(message, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);

		// Center Dialog on the GUI
		JDialog dialog = pane.createDialog(this, "Close Scenario");

		dialog.pack();
		dialog.setVisible(true);

		int value = ((Integer) pane.getValue()).intValue();

		if (value == JOptionPane.YES_OPTION)
		{

			if (discardCurrentSimGenerated())
			{
				checkSaved();
				
				removeSimulation();
				
				scenarioEditor.setText("");
				lblFilePath.setText("No File");
				chckbxEditMode.setSelected(false);
				saved = true;
				simGenerated = false;

				// We have no scenario therefore no sim.
				btnGenerateSim.setEnabled(false);
				btnStartSim.setEnabled(false);
				btnPauseSim.setEnabled(false);
				btnClose.setEnabled(false);
				btnSave.setEnabled(false);

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

				FileWriter fileWriter = new FileWriter(fileName + ".xml");
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

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
				simScenario = new LVScenario();

				simScenario.loadConfig(text);
			}
			else
			{
				System.out.println("DeterminScenarios :UKNOWN");
			}
		}

		return simScenario;
	}

	private void setUpPanels()
	{
		// Remove all tabs
		simulationTabPane.removeAll();

		// Clear the Chart List
		charts = new LinkedList<StatPanelAbs>();

		// Re-add the Scenario Tab.
		addScenarioTab();

		// Re-add the Scenario Tab
		addSimulationStatsListTab();

		addChartTabs();
		
		// Set up the Sim with the new chart targets
		simsManager.setSimOutPutCharts(simId,charts);
	}

	private void addChartTabs()
	{
		StatManager statManager = simsManager.getStatManager(simId);
		
		if(statManager!=null)
		{
			Set<String> statGroups = statManager.getGroupList();

			// Collect the enabled Charts
			for (String group : statGroups)
			{
				StatGroup statGroup = statManager.getStatGroup(group);
				
				if(statGroup.getGroupSettings().graphEnabled())
				{
					charts.add(new GlobalStatChartPanel(group,statManager,statGroup.getGroupSettings().hasTotalStat(),statGroup.getGroupSettings().getGraphSampleRate()));
				}

			}		
			
		}

		// Add the detected Panels
		for (StatPanelAbs chartPanel : charts)
		{
			System.out.println("Adding " + chartPanel.getName() + " Chart Panel");
			simulationTabPane.addTab(chartPanel.getName(), null, chartPanel);
			simulationTabPane.setIconAt(simulationTabPane.getTabCount() - 1, simulationStatChartIcon);
		}
		
	}

	private void newSim(String scenario)
	{
		ScenarioInf simScenario = null;

		removeSimulation();

		// Add a new sim and direct its performance stats to this panel.
		simId = simsManager.addSimulation();
						
		tabTitle = "Simulation " + simId;
		
		simScenario = determinScenarios(scenario);

		if (simScenario != null)
		{
			System.out.println("Creating Sim");
			
			// Register as a Stat Listener
			simsManager.addSimulationStatListener(simId, this);
			
			// Register as a Stat Listener
			simsManager.addSimulationStatusListener(simId, this);
			
			simsManager.createSimScenario(simId,simScenario);
			
			simsManager.setActiveSim(simId);
			
			simsManager.resetActiveSimCamera();
			
			setUpPanels();

			btnGenerateSim.setEnabled(true);

			btnStartSim.setEnabled(true);

			btnPauseSim.setEnabled(false);

			btnPauseSim.setText("   Pause");

			btnPauseSim.setIcon(pauseSimIcon);

			sliderSimStepRate.setEnabled(false);

			sliderSimStepRate.setValue(8);
			
		}
		else
		{
			System.out.println("Scenario Failed to Load");
		}

	}

	private void simStartedState()
	{

		simsManager.startSim(simId);

		btnGenerateSim.setEnabled(false);

		btnStartSim.setEnabled(false);

		btnPauseSim.setEnabled(true);

		sliderSimStepRate.setEnabled(true);
		
		simulationStatsListPanel.setExportEnabled(false);

	}

	private void startUpState()
	{
		System.out.println("Simulation now in Startup State");

		tabTitle = "Loaded";
		
		clearStats();

		btnStartSim.setEnabled(false);
		sliderSimStepRate.setEnabled(false);
		btnPauseSim.setEnabled(false);
		btnGenerateSim.setEnabled(true);
	}

	private void simPausedStatus()
	{
		btnPauseSim.setText("Resume");
		btnGenerateSim.setEnabled(true);	

		btnPauseSim.setIcon(resumeSimIcon);
		
		simulationStatsListPanel.setExportEnabled(true);

	}

	private void simUnPausedStatus()
	{
		btnPauseSim.setText("   Pause");
		btnGenerateSim.setEnabled(false);
				
		btnPauseSim.setIcon(pauseSimIcon);
		
		simulationStatsListPanel.setExportEnabled(false);
	}

	@Override
	public void stateChanged(ChangeEvent e)
	{
		if (e.getSource() == sliderSimStepRate)
		{

			// Prevent a 0 value being set
			if (sliderSimStepRate.getValue() <= 1)
			{
				lblRequestedStepRate.setText("1");

				// Set the requested update rate
				simsManager.setReqSimStepRate(simId,1);
				
			}
			else if (sliderSimStepRate.getValue() == 4 )
			{
				lblRequestedStepRate.setText("5");

				// Set the requested update rate
				simsManager.setReqSimStepRate(simId,5);
			}
			else if (sliderSimStepRate.getValue() == 8 )
			{
				lblRequestedStepRate.setText("15");

				// Set the requested update rate
				simsManager.setReqSimStepRate(simId,15);
			}
			else if (sliderSimStepRate.getValue() == 12 )
			{
				lblRequestedStepRate.setText("60");

				// Set the requested update rate
				simsManager.setReqSimStepRate(simId,60);
			}
			else if (sliderSimStepRate.getValue() == 16)
			{
				lblRequestedStepRate.setText("Unli");

				// Set the requested update rate
				simsManager.setReqSimStepRate(simId,-1);
			}
			else
			{
				lblRequestedStepRate.setText("Selecting");
			}

			
			
		}
		else if (e.getSource() == chckbxEditMode)
		{
			if (chckbxEditMode.isSelected())
			{
				scenarioEditor.setEditable(true);
				scenarioEditor.setBackground(editMode);

				btnClose.setEnabled(true);

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
	
	public String getTitle()
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
	private void setStepNo(long stepNo)
	{
		lblStepCount.setText(Long.toString(stepNo));
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
	
	@Override
	public void simulationStatChanged(long time,long stepNo,int asps)
	{
		if(allowUpdate)
		{
			setTime(time);
			setStepNo(stepNo);
			setASPS(asps);
			
			allowUpdate = false;
		}
		
	}

	@Override
	public void simulationStatusChanged(SimStatus status)
	{
		listenersLock.acquireUninterruptibly();
	    for (TabStatusChangedListenerInf listener : tabStatusListeners)
	    {
	    	listener.tabStatusChanged(this,status);
	    }
	    listenersLock.release();
	}
	
	public void addTabStatusListener(TabStatusChangedListenerInf listener)
	{
		listenersLock.acquireUninterruptibly();
	    	tabStatusListeners.add(listener);
    	listenersLock.release();
	}

}
