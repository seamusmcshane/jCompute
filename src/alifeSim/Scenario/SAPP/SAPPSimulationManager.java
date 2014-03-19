package alifeSim.Scenario.SAPP;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import alifeSim.Alife.GenericPlant.GenericPlantManager;
import alifeSim.Alife.SimpleAgent.SimpleAgentManager;
import alifeSim.Debug.DebugLogger;
import alifeSim.Gui.View.GUISimulationView;
import alifeSim.Gui.View.SimViewCam;
import alifeSim.Scenario.ScenarioInf;
import alifeSim.Scenario.EndEvents.ScenarioAllPredatorsLTEEndEvent;
import alifeSim.Scenario.EndEvents.ScenarioEndEventInf;
import alifeSim.Scenario.EndEvents.ScenarioAllPreyLTEEndEvent;
import alifeSim.Scenario.EndEvents.ScenarioStepCountEndEvent;
import alifeSim.Simulation.SimulationScenarioManagerInf;
import alifeSim.Simulation.SimulationStats;
import alifeSim.Stats.StatGroup;
import alifeSim.Stats.StatManager;
import alifeSim.Stats.StatGroupSetting;
import alifeSim.World.World;
import alifeSim.World.WorldInf;
import alifeSimGeom.A2DVector2f;


/**
 * This class is the top level manager for the processing sequence a step in the simulation.
 * 
 * It manages the interaction between slick2d drawing and the simulation update loop. (via semaphore)
 * 
 * @author Seamus McShane
 * @version $Revision: 1.0 $
 */
public class SAPPSimulationManager implements SimulationScenarioManagerInf
{
	/** Used to prevent dual Access to the lists - which would cause an exception.
	 * */
	private Semaphore lock = new Semaphore(1, true);

	/** Simulation Agent Manager */
	private SimpleAgentManager simpleAgentManager;
	
	/** Simulation Plant Manager */
	private GenericPlantManager genericPlantManager;

	/** Threads used for processing */
	private int numThreads = 0;

	/** The threaded/barrier manager */
	private BarrierManager barrierManager;

	/** Controls when the barrier is released **/
	private Semaphore barrierControllerSemaphore;
		
	/* The Simulation World. */
	private WorldInf world;

	private SAPPScenario scenario;
	
	private StatManager statManager;

	// The local state of this simulations view
	private SimViewCam simViewCam;
		
	private List<ScenarioEndEventInf> endEvents;
	private String endEvent = "NONE";
		
	/**
	 * Constructor for SimulationManager.
	*/
	public SAPPSimulationManager(SAPPScenario scenario)
	{
		simViewCam = new SimViewCam();
		
		simViewCam.setCamOffset(new A2DVector2f(50,50));
		
		this.scenario = scenario;
		
		setUpBarrierManager();

		setUpWorld();
		
		setUpPlantManager();

		setUpAgentManager();	
		
		setUpStatManager();
		
		setUpEndEvents();
				
	}

	private void setUpStatManager()
	{
		statManager = new StatManager("SAPP");
		
		/* Population */ 
		statManager.registerGroup(new StatGroup("Population"));
		statManager.getStatGroup("Population").registerStats(genericPlantManager.getPopulationStats());
		statManager.getStatGroup("Population").registerStats(simpleAgentManager.getPopulationStats());
		
		
		/* Births Deaths */
		statManager.registerGroup(new StatGroup("Births-Deaths"));
		statManager.getStatGroup("Births-Deaths").registerStats(simpleAgentManager.getBirthDeathStats());
		
		/* Agent Stats */
		statManager.registerGroup(new StatGroup("AgentEnergyLevels"));
		statManager.getStatGroup("AgentEnergyLevels").registerStats(simpleAgentManager.getEnergyLevels());
		
		
		/* Agent Stats */
		statManager.registerGroup(new StatGroup("AgentAge"));
		statManager.getStatGroup("AgentAge").registerStats(simpleAgentManager.getAgentAges());
		
		/* Agent Stats */
		statManager.registerGroup(new StatGroup("AgentViewSize"));
		statManager.getStatGroup("AgentViewSize").registerStats(simpleAgentManager.getAgentViewSizes());
		
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
				DebugLogger.output("Stat Group / Setting " + statSetting.getName() + " Does not EXIST!");
			}
			
		}
		
	}
	
	public StatManager getStatmanger()
	{
		return statManager;
	}
	
	/**
	 * Method setUpPlantManager.
	 * @param worldSize int
	 * @param plantNumbers int
	 * @param plantRegenRate int
	 * @param plantStartingEnergy int
	 * @param plantEnergyAbsorptionRate int
	 */
	private void setUpPlantManager()
	{
		genericPlantManager = new GenericPlantManager(world,barrierManager, scenario.plantSettings);
	}

	/**
	 * Method setUpAgentManager.
	 * @param worldSize int
	 * @param agentPreyNumbers int
	 * @param agentPredatorNumbers int
	 * @param agentSettings SimpleAgentManagementSetupParam
	 */
	private void setUpAgentManager()
	{
		simpleAgentManager = new SimpleAgentManager(world,barrierManager, scenario.getAgentSettingsList());
	}

	/**
	 * Sets up the barrier with the auto detected number of threads per processor.
	 */
	private void setUpBarrierManager()
	{
		this.numThreads = Runtime.getRuntime().availableProcessors(); // Ask Java how many CPU threads we can run in parallel
		
		DebugLogger.output("Threads to use for Barrier Tasks : " + numThreads);

		barrierControllerSemaphore = new Semaphore(1, true);

		barrierControllerSemaphore.acquireUninterruptibly();

		barrierManager = new BarrierManager(barrierControllerSemaphore, numThreads);

		barrierManager.start();
	}


	private void setUpWorld()
	{
		world = new World(scenario.worldSettings.getWorldSize(), scenario.worldSettings.getBarrierNum(), scenario.worldSettings.getBarrierScenario());
	}
	
	/**
	 * Initiates the barrier thread shutdown.
	 */
	public void cleanUp()
	{
		lock.acquireUninterruptibly();
		
		/* Clean up */
		barrierManager.cleanUp();
		
		/* Set to null so garbage collector can get to work */
		barrierManager=null;
		
		lock.release();
		
	}
	
	// stage1 and stage3 could be run in parallel - stage 2 is already threaded 
	public void doSimulationUpdate()
	{
		// Get a lock managers to prevent dual access by draw methods
		lock.acquireUninterruptibly();

		// Prepare
		stage1();

		// Distribute
		stage2();

		// Collect
		stage3();

		// Release the lock on the managers
		lock.release();
	}

	private void stage1()
	{
		genericPlantManager.stage1();
		simpleAgentManager.stage1();
	}

	// View barrier operates in this method timeslot
	private void stage2()
	{
		// Prepare views
		genericPlantManager.stage2();
		simpleAgentManager.stage2();

		barrierControllerSemaphore.release();

		barrierControllerSemaphore.acquireUninterruptibly();

	}

	private void stage3()
	{
		genericPlantManager.stage3();
		simpleAgentManager.stage3();
		
		// 
		statManager.notifiyStatListeners();
	}

	
	public void drawSim(GUISimulationView simView,boolean viewRangeDrawing,boolean viewsDrawing)
	{
		try
		{
			lock.acquire();

			world.drawWorld(simView);
			
			genericPlantManager.draw(simView);

			simpleAgentManager.draw(simView,viewRangeDrawing,viewsDrawing);

			// Release the lock on the done list
			lock.release();
		}
		catch (InterruptedException e)
		{
			// Debug DebugLogger.output("Never Got Lock");
		}
	}

	@Override
	public int getWorldSize()
	{
		return world.getWorldBoundingSquareSize();
	}
	
	public void displayDebug()
	{
		barrierManager.displayBarrierTaskDebugStats();
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
				
		setScenarioLTEEndEvents();
		
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
				
				DebugLogger.output("Event Event Occurred : " + event.getName() + " - " + event.getValue());
				
				break;	// No need to check other events
			}
		}
				
		return eventOccurred;
	}

	public void setScenarioLTEEndEvents()
	{
		if(scenario.endEventIsSet("AllPreyLTE"))
		{
			int triggerValue = scenario.getEventValue("AllPreyLTE");
			
			endEvents.add(new ScenarioAllPreyLTEEndEvent(simpleAgentManager,triggerValue));
		}
		
		if(scenario.endEventIsSet("AllPredatorsLTE"))
		{
			int triggerValue = scenario.getEventValue("AllPredatorsLTE");
			
			endEvents.add(new ScenarioAllPredatorsLTEEndEvent(simpleAgentManager,triggerValue));
		}
	}
	
	@Override
	public void setScenarioStepCountEndEvent(SimulationStats simState)
	{
		if(scenario.endEventIsSet("StepCount"))
		{
			int triggerValue = scenario.getEventValue("StepCount");
			
			endEvents.add(new ScenarioStepCountEndEvent(simState,triggerValue));
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
}
