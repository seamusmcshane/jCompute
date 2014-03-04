package alifeSim.Scenario.Math.LotkaVolterra;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import alifeSim.Gui.View.GUISimulationView;
import alifeSim.Gui.View.SimViewCam;
import alifeSim.Simulation.SimulationScenarioManagerInf;
import alifeSim.Simulation.SimulationState;
import alifeSim.Simulation.SimulationStats;
import alifeSim.Stats.StatGroup;
import alifeSim.Stats.StatGroupSetting;
import alifeSim.Stats.StatManager;
import alifeSim.Scenario.ScenarioInf;
import alifeSim.Scenario.EndEvents.ScenarioEndEventInf;
import alifeSim.Scenario.EndEvents.ScenarioStepCountEndEvent;
import alifeSim.Scenario.Math.LotkaVolterra.LotkaVolterraScenario;
import alifeSimGeom.A2DVector2f;

public class LotkaVolterraSimulationManager implements SimulationScenarioManagerInf
{
	private Semaphore lock = new Semaphore(1, false);
	
	private LotkaVolterraScenario scenario;

	private StatManager statManager;
	
	private LotkaVolterraSubTypeInf lv;
	
	private LotkaVolterraTwoAndThreeSpeciesSettings settings;
	
	private SimViewCam simViewCam;

	private ArrayList<ScenarioEndEventInf> endEvents;
	
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
	
	@Override
	public float getCamZoom()
	{
		return simViewCam.getCamZoom();
	}
	
	@Override
	public void resetCamPos(float x,float y)
	{
		simViewCam.resetCamPos(x, y);
	}
	
	@Override
	public void adjCamZoom(float z)
	{
		simViewCam.adjCamZoom(z);	
	}

	@Override
	public void resetCamZoom()
	{
		simViewCam.resetCamZoom();			
	}	
	
	@Override
	public A2DVector2f getCamPos()
	{
		return new A2DVector2f(simViewCam.getCamPosX(),simViewCam.getCamPosY());
	}

	@Override
	public void moveCamPos(float x, float y)
	{
		simViewCam.moveCam(x,y);		
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
			int endStep = scenario.getEventValue("StepCount");
			
			endEvents.add(new ScenarioStepCountEndEvent(simStats,endStep));			
		}		
	}

	@Override
	public ScenarioInf getScenario()
	{
		return scenario;
	}	
	
}
