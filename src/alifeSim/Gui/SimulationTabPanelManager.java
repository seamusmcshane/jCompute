package alifeSim.Gui;

import javax.swing.JTabbedPane;

public class SimulationTabPanelManager extends JTabbedPane
{

	private final int maxTabs = 8;
	private int tabCount = 0;
	
	private SimulationInfoTabPanel simulationInfoTab;
	
	private SimulationTabPanel simulationTabs[];
	
	private static final long serialVersionUID = 1L;

	public SimulationTabPanelManager(int val)
	{
		super(val);
		
		simulationTabs = new SimulationTabPanel[maxTabs];
		
		simulationInfoTab = new SimulationInfoTabPanel();
		
		this.add(simulationInfoTab);
		this.setTitleAt(this.getTabCount()-1, "        Main        ");
		
	}	
		
	public boolean addTab()
	{
		if(tabCount<maxTabs)
		{
			simulationTabs[tabCount] = new SimulationTabPanel();	
			this.add(simulationTabs[tabCount]);
			this.setTitleAt(this.getTabCount()-1, "Simulaton " + (tabCount+1));
			tabCount++;
		}
		else
		{
			return false;
		}
		return true;
	}

}
