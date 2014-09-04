package jCompute.Scenario.SAPP;

import jCompute.Gui.View.GUISimulationView;
import jCompute.Gui.View.SimViewCam;
import jCompute.Gui.View.Graphics.A2DVector2f;
import jCompute.Scenario.ScenarioInf;
import jCompute.Scenario.EndEvents.ScenarioAllPredatorsLTEEndEvent;
import jCompute.Scenario.EndEvents.ScenarioAllPreyLTEEndEvent;
import jCompute.Scenario.EndEvents.ScenarioEndEventInf;
import jCompute.Scenario.EndEvents.ScenarioStepCountEndEvent;
import jCompute.Scenario.SAPP.Plant.GenericPlantManager;
import jCompute.Scenario.SAPP.SimpleAgent.SimpleAgentManager;
import jCompute.Scenario.SAPP.World.World;
import jCompute.Scenario.SAPP.World.WorldInf;
import jCompute.Simulation.SimulationScenarioManagerInf;
import jCompute.Simulation.SimulationStats;
import jCompute.Stats.StatGroup;
import jCompute.Stats.StatGroupSetting;
import jCompute.Stats.StatManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the top level manager for the processing sequence a step in the
 * simulation. It manages the interaction between drawing and update.
 */
public class SAPPSimulationManager implements SimulationScenarioManagerInf
{
	// SL4J Logger
	private static Logger log = LoggerFactory.getLogger(SAPPSimulationManager.class);

	/**
	 * Used to prevent dual Access to the lists - which would cause an
	 * exception.
	 * */
	private Semaphore lock = new Semaphore(1, true);

	/** Simulation Agent Manager */
	private SimpleAgentManager simpleAgentManager;

	/** Simulation Plant Manager */
	private GenericPlantManager genericPlantManager;

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

		simViewCam.setCamOffset(new A2DVector2f(50, 50));

		this.scenario = scenario;

		setUpWorld();

		genericPlantManager = new GenericPlantManager(world, scenario.plantSettings);

		simpleAgentManager = new SimpleAgentManager(world, scenario.getAgentSettingsList());

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

		/*
		 * This code filters out invalid stat group names in the xml file Those
		 * that are valid are registered above.
		 */
		for (StatGroupSetting statSetting : statSettings)
		{
			if (statManager.containsGroup(statSetting.getName()))
			{
				statManager.setGroupSettings(statSetting.getName(), statSetting);
			}
			else
			{
				log.error("Stat Group / Setting " + statSetting.getName() + " Does not EXIST!");
			}
		}
	}

	public StatManager getStatmanger()
	{
		return statManager;
	}

	private void setUpWorld()
	{
		world = new World(scenario.worldSettings.getWorldSize(), scenario.worldSettings.getBarrierNum(),
				scenario.worldSettings.getBarrierScenario());
	}

	/**
	 * Initiates the barrier thread shutdown.
	 */
	public void cleanUp()
	{
		lock.acquireUninterruptibly();

		lock.release();

	}

	// stage1 and stage3 could be run in parallel - stage 2 is already threaded
	public void doSimulationUpdate()
	{
		// Get a lock managers to prevent dual access by draw methods
		lock.acquireUninterruptibly();

		simpleAgentManager.doStep(genericPlantManager.doStep());

		statManager.notifiyStatListeners();

		// Release the lock on the managers
		lock.release();
	}

	public void drawSim(GUISimulationView simView, boolean viewRangeDrawing, boolean viewsDrawing)
	{
		try
		{
			lock.acquire();

			world.drawWorld(simView);

			genericPlantManager.draw(simView);

			simpleAgentManager.draw(simView, viewRangeDrawing, viewsDrawing);

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

	private void setUpEndEvents()
	{
		endEvents = new ArrayList<ScenarioEndEventInf>();

		setScenarioLTEEndEvents();
	}

	@Override
	public boolean hasEndEventOccurred()
	{
		boolean eventOccurred = false;

		for (ScenarioEndEventInf event : endEvents)
		{
			if (event.checkEvent())
			{
				endEvent = event.getName();

				eventOccurred = true;

				// Output the final update
				statManager.endEventNotifiyStatListeners();

				log.info("Event Event Occurred " + event.getName() + "@" + event.getValue());

				break;	// No need to check other events
			}
		}

		return eventOccurred;
	}

	public void setScenarioLTEEndEvents()
	{
		if (scenario.endEventIsSet("AllPreyLTE"))
		{
			int triggerValue = scenario.getEventValue("AllPreyLTE");

			endEvents.add(new ScenarioAllPreyLTEEndEvent(simpleAgentManager, triggerValue));
		}

		if (scenario.endEventIsSet("AllPredatorsLTE"))
		{
			int triggerValue = scenario.getEventValue("AllPredatorsLTE");

			endEvents.add(new ScenarioAllPredatorsLTEEndEvent(simpleAgentManager, triggerValue));
		}
	}

	@Override
	public void setScenarioStepCountEndEvent(SimulationStats simState)
	{
		if (scenario.endEventIsSet("StepCount"))
		{
			int triggerValue = scenario.getEventValue("StepCount");

			endEvents.add(new ScenarioStepCountEndEvent(simState, triggerValue));
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
