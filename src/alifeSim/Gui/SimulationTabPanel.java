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

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.LineBorder;
import java.awt.Font;
import javax.swing.UIManager;

public class SimulationTabPanel extends JPanel
{
	JEditorPane scenarioEditor;
	JLabel lblFilePath;
	
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

		JSlider slider = new JSlider();
		slider.setValue(15);
		slider.setToolTipText("Adjust requested step rate.");
		slider.setSnapToTicks(true);
		slider.setPreferredSize(new Dimension(25, 20));
		slider.setPaintTicks(true);
		slider.setMinorTickSpacing(30);
		slider.setMinimum(15);
		slider.setMaximum(300);
		slider.setMajorTickSpacing(150);
		slider.setEnabled(false);
		GridBagConstraints gbc_slider = new GridBagConstraints();
		gbc_slider.fill = GridBagConstraints.BOTH;
		gbc_slider.insets = new Insets(0, 0, 5, 0);
		gbc_slider.gridx = 2;
		gbc_slider.gridy = 2;
		controlPanel.add(slider, gbc_slider);

		JButton button = new JButton("Generate");
		button.setToolTipText("Generate a new simuation based on the values of the parameters.");
		button.setEnabled(true);
		GridBagConstraints gbc_button = new GridBagConstraints();
		gbc_button.fill = GridBagConstraints.BOTH;
		gbc_button.insets = new Insets(0, 0, 0, 5);
		gbc_button.gridx = 0;
		gbc_button.gridy = 3;
		controlPanel.add(button, gbc_button);

		JButton button_1 = new JButton("Start");
		button_1.setToolTipText("Start the simulation.");
		button_1.setEnabled(false);
		GridBagConstraints gbc_button_1 = new GridBagConstraints();
		gbc_button_1.fill = GridBagConstraints.BOTH;
		gbc_button_1.insets = new Insets(0, 0, 0, 5);
		gbc_button_1.gridx = 1;
		gbc_button_1.gridy = 3;
		controlPanel.add(button_1, gbc_button_1);

		JButton button_2 = new JButton("   Pause");
		button_2.setToolTipText("Pause / Unpause the simulation.");
		button_2.setEnabled(false);
		GridBagConstraints gbc_button_2 = new GridBagConstraints();
		gbc_button_2.fill = GridBagConstraints.BOTH;
		gbc_button_2.gridx = 2;
		gbc_button_2.gridy = 3;
		controlPanel.add(button_2, gbc_button_2);

		JPanel scenarioPanel = new JPanel();
		simulationInfoTab.add(scenarioPanel, BorderLayout.CENTER);
		scenarioPanel.setLayout(new BorderLayout(0, 0));

		JPanel scenarioOpenPanel = new JPanel();
		scenarioPanel.add(scenarioOpenPanel, BorderLayout.NORTH);
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

		JButton btnOpen = new JButton("Open");
		btnOpen.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{

				final JFileChooser filechooser = new JFileChooser(new File("./scenarios"));

				int val = filechooser.showOpenDialog(filechooser);

				if (val == JFileChooser.APPROVE_OPTION)
				{
					// System.out.println("Get File");
					File file = filechooser.getSelectedFile();
					// determinScenarios(file);

					/*
					 * if (!sim.simPaused()) { simPausedState(); } // Not
					 * already generating Sim if (!generatingSim) {
					 * generatingSim = true;
					 * 
					 * // Create the new Simulation newSim();
					 * 
					 * generatingSim = false;
					 * 
					 * }
					 */

					StringBuilder text = new StringBuilder();
					String NL = System.getProperty("line.separator");
					Scanner scanner;
					try
					{
						scanner = new Scanner(new FileInputStream(file));

						while (scanner.hasNextLine())
						{
							text.append(scanner.nextLine() + NL);
						}

						scanner.close();
					}
					catch (FileNotFoundException e1)
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					lblFilePath.setText(file.getAbsolutePath());
					scenarioEditor.setText(text.toString());

				}

			}

		});
		GridBagConstraints gbc_btnOpen = new GridBagConstraints();
		gbc_btnOpen.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnOpen.insets = new Insets(0, 0, 0, 5);
		gbc_btnOpen.gridx = 0;
		gbc_btnOpen.gridy = 0;
		scenarioOpenPanel.add(btnOpen, gbc_btnOpen);

		JButton btnSave = new JButton("Save");
		GridBagConstraints gbc_btnSave = new GridBagConstraints();
		gbc_btnSave.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnSave.insets = new Insets(0, 0, 0, 5);
		gbc_btnSave.gridx = 1;
		gbc_btnSave.gridy = 0;
		scenarioOpenPanel.add(btnSave, gbc_btnSave);

		JButton btnClose = new JButton("Close");
		GridBagConstraints gbc_btnClose = new GridBagConstraints();
		gbc_btnClose.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnClose.gridx = 2;
		gbc_btnClose.gridy = 0;
		scenarioOpenPanel.add(btnClose, gbc_btnClose);

		JPanel scenarioFilePanel = new JPanel();
		scenarioPanel.add(scenarioFilePanel, BorderLayout.CENTER);
		scenarioFilePanel.setLayout(new BorderLayout(0, 0));

		JCheckBox chckbxEditMode = new JCheckBox("EditMode");
		scenarioFilePanel.add(chckbxEditMode, BorderLayout.NORTH);
		
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
		
		JPanel filePath = new JPanel();
		scrollPane.setColumnHeaderView(filePath);
		filePath.setLayout(new BorderLayout(0, 0));
		
		lblFilePath = new JLabel("No File");
		filePath.add(lblFilePath);

		JTabbedPane simulationGraph = new JTabbedPane(JTabbedPane.TOP);
		simulationTabPane.addTab("Graph", null, simulationGraph, null);
	}
	private static final long serialVersionUID = 5391587818992199457L;

}
