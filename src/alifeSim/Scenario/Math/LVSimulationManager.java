package alifeSim.Scenario.Math;

import java.util.List;
import java.util.concurrent.Semaphore;

import alifeSim.Gui.NewSimView;
import alifeSim.Simulation.SimulationManagerInf;
import alifeSim.Stats.StatGroup;
import alifeSim.Stats.StatGroupSetting;
import alifeSim.Stats.StatManager;
import alifeSim.Scenario.Math.LVScenario;

public class LVSimulationManager implements SimulationManagerInf
{
	private Semaphore lock = new Semaphore(1, false);
	
	private LVScenario scenario;

	private StatManager statManager;
	
	private LVTwoSpeciesManager lv;
	
	private LVSettings settings;
	
	public LVSimulationManager(LVScenario scenario)
	{
		this.scenario = scenario;	
				
		settings = scenario.settings;
		
		lv = new LVTwoSpeciesManager(settings);
		
		setUpStatManager();
	}
	
	@Override
	public void cleanUp()
	{
		
	}

	@Override
	public void doSimulationUpdate()
	{
		lock.acquireUninterruptibly();
		
			lv.doStep();
		
		lock.release();
	}

	@Override
	public StatManager getStatmanger()
	{
		return statManager;
	}

	@Override
	public void drawSim(NewSimView simView, boolean ignored, boolean ignored2)
	{
		try
		{
			lock.acquire();
			
				lv.drawLV(simView);
			
			lock.release();
			
		}
		catch (InterruptedException e)
		{

		}		

	}

	@Override
	public int getWorldSize()
	{
		return 0;
	}

	@Override
	public void displayDebug()
	{
	
	}

	private void setUpStatManager()
	{
		statManager = new StatManager("LV");
		
		/* Population */ 
		statManager.registerGroup(new StatGroup("Population"));
		statManager.getStatGroup("Population").registerStats(lv.getPopulationStats());
		
		List<StatGroupSetting> statSettings = scenario.getStatGroupSettingsList();
		
		/* This code filters out invalid stat group names in the xml file
		 * Those that are valid are registered above.
		 */
		for(StatGroupSetting statSetting : statSettings)
		{
			if(statManager.containsGroup(statSetting.getName()))
			{
				statManager.setGroupSettings(statSetting.getName(), statSetting);
			}
			else
			{
				System.out.println("Stat Group / Setting " + statSetting.getName() + " Does not EXIST!");
			}
			
		}
		
	}
	
}
