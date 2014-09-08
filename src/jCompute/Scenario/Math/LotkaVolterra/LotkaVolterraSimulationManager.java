package jCompute.Scenario.Math.LotkaVolterra;

import jCompute.Gui.View.GUISimulationView;
import jCompute.Gui.View.SimViewCam;
import jCompute.Gui.View.Graphics.A2DVector2f;
import jCompute.Scenario.ScenarioInf;
import jCompute.Scenario.EndEvents.ScenarioEndEventInf;
import jCompute.Scenario.EndEvents.ScenarioStepCountEndEvent;
import jCompute.Scenario.Math.LotkaVolterra.LotkaVolterraScenario;
import jCompute.Simulation.SimulationScenarioManagerInf;
import jCompute.Simulation.SimulationStats;
import jCompute.Stats.StatGroup;
import jCompute.Stats.StatGroupSetting;
import jCompute.Stats.StatManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class LotkaVolterraSimulationManager implements SimulationScenarioManagerInf
{
	private Semaphore lock = new Semaphore(1, false);
	
	private LotkaVolterraScenario scenario;

	private StatManager statManager;
	
	private LotkaVolterraSubTypeInf lv;
	
	private LotkaVolterraTwoAndThreeSpeciesSettings settings;
	
	private SimViewCam simViewCam;

	private ArrayList<ScenarioEndEventInf> endEvents;
	private String endEvent = "None";
	
	public LotkaVolterraSimulationManager(LotkaVolterraScenario scenario)
	{
		simViewCam = new SimViewCam();
		
		simViewCam.setCamOffset(new A2DVector2f(252.125f,50));
		
		this.scenario = scenario;	
				
		settings = scenario.settings;
		
		lv = setSimSubType(settings.getSubType(),settings);
		
		setUpStatManager();
		
		setUpEndEvents();
		
	}
	
	private LotkaVolterraSubTypeInf setSimSubType(String text,LotkaVolterraTwoAndThreeSpeciesSettings settings)
	{
		LotkaVolterraSubTypeInf subType;
		
		if(text.equalsIgnoreCase("Three"))
		{
			subType = new LotkaVolterraThreeSpeciesManager(settings);
		}
		else // Two
		{
			subType = new LotkaVolterraTwoSpeciesManager(settings);
		}
		
		return subType;		
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
	public void drawSim(GUISimulationView simView, boolean ignored, boolean ignored2)
	{
		try
		{
			lock.acquire();
			
				lv.draw(simView);
			
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

	private void setUpStatManager()
	{
		statManager = new StatManager("LV");
		
		/* Population */ 
		statManager.registerGroup(new StatGroup("Population"));
		statManager.getStatGroup("Population").registerStats(lv.getPopulationStats());
		
		lv.setStatManager(statManager);
		
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

	private void setUpEndEvents()
	{
		endEvents = new ArrayList<ScenarioEndEventInf>();	
		
	}
	
	@Override
	public boolean hasEndEventOccurred()
	{
		boolean eventOccurred = false;
		
		for(ScenarioEndEventInf event : endEvents)
		{
			if(event.checkEvent())
			{
				endEvent = event.getName();
				
				eventOccurred = true;
				
				// Output the final update
				statManager.endEventNotifiyStatListeners();
				
				System.out.println("Event Event Occurred : " + event.getName() + " - " + event.getValue());
				
				break;	// No need to check other events
			}
		}
		
		return eventOccurred;
	}

	@Override
	public void setScenarioStepCountEndEvent(SimulationStats simStats)
	{
		if(scenario.endEventIsSet("StepCount"))
		{
			int endStep = scenario.getEndEventTriggerValue("StepCount");
			
			endEvents.add(new ScenarioStepCountEndEvent(simStats,endStep));			
		}		
	}

	@Override
	public ScenarioInf getScenario()
	{
		return scenario;
	}

	@Override
	public String getEndEvent()
	{
		return endEvent;
	}

	@Override
	public SimViewCam getSimViewCam()
	{
		return simViewCam;
	}
	
	@Override
	public String getInfo()
	{
		return "|| Type " + scenario.getScenarioType();
	}
	
}
