package alifeSim.Gui;

import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JTabbedPane;

public class SimulationTabPanel extends JPanel
{
	public SimulationTabPanel() {
		setLayout(new BorderLayout(0, 0));
		
		JTabbedPane simulationTabPane = new JTabbedPane(JTabbedPane.TOP);
		add(simulationTabPane, BorderLayout.CENTER);
		
		JPanel simulationInfoTab = new JPanel();
		simulationTabPane.addTab("Information", null, simulationInfoTab, null);
		
		JTabbedPane simulationGraph = new JTabbedPane(JTabbedPane.TOP);
		simulationTabPane.addTab("Graph", null, simulationGraph, null);
	}
	private static final long serialVersionUID = 5391587818992199457L;

}
