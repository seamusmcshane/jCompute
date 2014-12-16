package jCompute.SimulationManager;

import jCompute.JComputeEventBus;
import jCompute.Gui.View.View;
import jCompute.Scenario.ScenarioInf;
import jCompute.Scenario.ScenarioManager;
import jCompute.Simulation.Simulation;
import jCompute.Simulation.SimulationState.SimState;
import jCompute.SimulationManager.Event.SimulationsManagerEvent;
import jCompute.SimulationManager.Event.SimulationsManagerEventType;
import jCompute.Stats.StatExporter;
import jCompute.Stats.Groups.StatGroupListenerInf;
import jCompute.Stats.StatExporter.ExportFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimulationsManager implements SimulationsManagerInf
{
	// SL4J Logger
	private static Logger log = LoggerFactory.getLogger(SimulationsManager.class);

	private static Semaphore simulationsManagerLock = new Semaphore(1);

	/* Max Concurrent Simulations */
	private final int maxSims;
	private int activeSims;

	/* Simulation Storage Structure */
	private HashMap<Integer, Simulation> simulations;

	/* Total Count of Simulations Ran - used for simulation ID */
	private int simulationNum;

	/* Active Sims View */
	private View simView;
	private Simulation activeSim;

	public SimulationsManager(int maxSims)
	{
		log.info("Simulations Manager Created");

		this.maxSims = maxSims;

		simulations = new HashMap<Integer, Simulation>();

		this.simulationNum = 0;
		this.activeSims = 0;

		log.info("Max Active Sims : " + maxSims);
	}

	@Override
	public int addSimulation(String scenarioText, int intialStepRate)
	{
		simulationsManagerLock.acquireUninterruptibly();

		if (activeSims < maxSims)
		{
			simulationNum++;
			activeSims++;

			// Create a sim & let sim know its id
			Simulation sim = new Simulation(simulationNum);

			// Validate Scenario
			ScenarioInf scenario = ScenarioManager.getScenario(scenarioText);

			if (sim != null && scenario != null)
			{
				sim.createSimScenario(scenario);

				sim.setReqStepRate(intialStepRate);

				// add sim to map - index on simId
				simulations.put(simulationNum, sim);
			}
			else
			{
				sim.destroySim();
				simulationNum--;

				simulationsManagerLock.release();

				log.error("Tried Adding Sim but failed");

				return -1;
			}

			JComputeEventBus.post(new SimulationsManagerEvent(simulationNum, SimulationsManagerEventType.AddedSim));

			simulationsManagerLock.release();

			return simulationNum;
		}
		else
		{
			log.info("Reached Max Active Sims");

			simulationsManagerLock.release();

			return -1;
		}
	}

	@Override
	public void removeSimulation(int simId)
	{
		simulationsManagerLock.acquireUninterruptibly();

		Simulation sim = simulations.remove(simId);

		activeSims--;

		if (sim != null)
		{
			sim.destroySim();

			if (activeSim == sim)
			{
				// Clear the Active Simulation View Reference
				if (simView != null)
				{
					simView.setViewTarget(null);
				}

				activeSim = null;
			}

			JComputeEventBus.post(new SimulationsManagerEvent(simId, SimulationsManagerEventType.RemovedSim));

			simulationsManagerLock.release();

			return;
		}
		else
		{
			log.error("Tried Removing Sim " + simId + ", but did not find it?");
		}

		simulationsManagerLock.release();
	}

	@Override
	public void startSim(int simId)
	{
		simulationsManagerLock.acquireUninterruptibly();

		log.info("Start Sim " + simId);
		
		Simulation sim = simulations.get(simId);

		sim.startSim();

		simulationsManagerLock.release();
	}

	@Override
	public void pauseSim(int simId)
	{
		simulationsManagerLock.acquireUninterruptibly();

		Simulation sim = simulations.get(simId);

		if (sim != null)
		{
			sim.pauseSim();
		}

		simulationsManagerLock.release();
	}

	@Override
	public String getScenarioText(int simId)
	{
		simulationsManagerLock.acquireUninterruptibly();

		String scenarioText = "No Simulation";

		Simulation sim = simulations.get(simId);

		if (sim != null)
		{			
			scenarioText = sim.getScenarioText();
		}

		simulationsManagerLock.release();

		return scenarioText;
	}

	@Override
	public void setReqSimStepRate(int simId, int stepRate)
	{
		simulationsManagerLock.acquireUninterruptibly();

		Simulation sim = simulations.get(simId);

		if (sim != null)
		{
			sim.setReqStepRate(stepRate);
		}

		simulationsManagerLock.release();

	}

	@Override
	public SimState togglePause(int simId)
	{
		SimState simState = null;

		simulationsManagerLock.acquireUninterruptibly();

		Simulation sim = simulations.get(simId);

		if (sim != null)
		{
			simState = sim.togglePause();
		}

		simulationsManagerLock.release();

		return simState;
	}

	@Override
	public void unPauseSim(int simId)
	{
		simulationsManagerLock.acquireUninterruptibly();

		Simulation sim = simulations.get(simId);

		if (sim != null)
		{
			sim.unPauseSim();
		}

		simulationsManagerLock.release();
	}

	@Override
	public void setActiveSim(int simId)
	{
		simulationsManagerLock.acquireUninterruptibly();

		Simulation sim = simulations.get(simId);

		log.debug("Active Sim : " + simId);

		if (simView != null)
		{
			simView.setViewTarget(sim);
		}

		// We latch the active sim incase we do add a view.
		activeSim = sim;

		simulationsManagerLock.release();
	}

	@Override
	public void setSimView(View simView)
	{
		simulationsManagerLock.acquireUninterruptibly();

		this.simView = simView;

		// Assuming there has been an active sim and there is currently a
		// simView
		if (simView != null)
		{
			// Set the latched active sim in the new view.
			if (activeSim != null)
			{
				simView.setViewTarget(activeSim);
			}
		}

		simulationsManagerLock.release();
	}

	@Override
	public void clearActiveSim()
	{
		simulationsManagerLock.acquireUninterruptibly();

		if (simView != null)
		{
			simView.setViewTarget(null);
		}

		simulationsManagerLock.release();
	}

	/* Will Reset the Camera of the active */
	@Override
	public void resetActiveSimCamera()
	{
		simulationsManagerLock.acquireUninterruptibly();

		if (simView != null)
		{
			simView.resetCamera();
		}

		simulationsManagerLock.release();
	}

	@Override
	public List<Simulation> getSimList()
	{
		simulationsManagerLock.acquireUninterruptibly();

		Set<Integer> simSet = simulations.keySet();
		List<Simulation> list = new LinkedList<Simulation>();

		if (simSet != null)
		{
			for (Integer id : simSet)
			{
				list.add(simulations.get(id));
			}
		}

		simulationsManagerLock.release();

		return list;
	}

	@Override
	public List<Integer> getSimIdList()
	{
		simulationsManagerLock.acquireUninterruptibly();

		Set<Integer> simSet = simulations.keySet();

		List<Integer> list = null;

		if (simSet != null)
		{
			list = new ArrayList<Integer>(simSet);
		}

		simulationsManagerLock.release();

		return list;
	}

	@Override
	public int getMaxSims()
	{
		return maxSims;
	}

	@Override
	public SimState getState(int simId)
	{
		SimState simState = null;

		simulationsManagerLock.acquireUninterruptibly();

		Simulation sim = simulations.get(simId);

		if (sim != null)
		{
			simState = sim.getState();
		}

		simulationsManagerLock.release();

		return simState;
	}

	@Override
	public Set<String> getStatGroupNames(int simId)
	{
		simulationsManagerLock.acquireUninterruptibly();

		Simulation sim = simulations.get(simId);

		Set<String> statGroupNames = null;

		if (sim != null)
		{
			statGroupNames = sim.getStatmanger().getGroupList();
		}

		simulationsManagerLock.release();

		return statGroupNames;
	}

	@Override
	public StatExporter getStatExporter(int simId, String fileNameSuffix, ExportFormat format)
	{
		simulationsManagerLock.acquireUninterruptibly();

		Simulation sim = simulations.get(simId);

		log.info("Creating stat exporter for Simulation " + simId);

		StatExporter exporter = null;
		
		if (sim != null)
		{
			/*
			 * Pause the sim as it will be updating its internal data structures
			 * Only pause sim if running or it will dead lock.
			 */
			if (sim.getState() == SimState.RUNNING)
			{
				sim.pauseSim();
			}

			// Create a stat exporter with export format.
			exporter = new StatExporter(format, fileNameSuffix);

			// populate from the stat manager as data source.
			exporter.populateFromStatManager(sim.getStatmanger());			
		}

		simulationsManagerLock.release();
		
		return exporter;
	}

	@Override
	public boolean isStatGroupGraphingEnabled(int simId, String group)
	{
		simulationsManagerLock.acquireUninterruptibly();

		Simulation sim = simulations.get(simId);

		boolean enabled = false;

		if (sim != null)
		{
			enabled = sim.getStatmanger().getStatGroup(group).getGroupSettings().graphEnabled();
		}

		simulationsManagerLock.release();

		return enabled;
	}

	@Override
	public int getStatGroupGraphSampleWindowSize(int simId, String group)
	{
		simulationsManagerLock.acquireUninterruptibly();

		Simulation sim = simulations.get(simId);

		int window = -1;

		if (sim != null)
		{
			window = sim.getStatmanger().getStatGroup(group).getGroupSettings().getGraphSampleWindow();
		}

		simulationsManagerLock.release();

		return window;
	}

	@Override
	public boolean hasStatGroupTotalStat(int simId, String group)
	{
		simulationsManagerLock.acquireUninterruptibly();

		Simulation sim = simulations.get(simId);

		boolean globalStat = false;

		if (sim != null)
		{
			globalStat = sim.getStatmanger().getStatGroup(group).getGroupSettings().hasTotalStat();
		}

		simulationsManagerLock.release();

		return globalStat;
	}

	@Override
	public void addStatGroupListener(int simId, String group, StatGroupListenerInf listener)
	{
		simulationsManagerLock.acquireUninterruptibly();

		Simulation sim = simulations.get(simId);

		if (sim != null)
		{
			sim.getStatmanger().getStatGroup(group).addStatGroupListener(listener);
		}

		simulationsManagerLock.release();
	}

	@Override
	public void removeStatGroupListener(int simId, String group, StatGroupListenerInf listener)
	{
		simulationsManagerLock.acquireUninterruptibly();

		Simulation sim = simulations.get(simId);

		if (sim != null)
		{
			sim.getStatmanger().getStatGroup(group).removeStatGroupListener(listener);
		}

		simulationsManagerLock.release();
	}

	@Override
	public int getReqSps(int simId)
	{
		int reqSps = -1;

		simulationsManagerLock.acquireUninterruptibly();

		Simulation sim = simulations.get(simId);

		if (sim != null)
		{
			reqSps = sim.getReqSps();
		}

		simulationsManagerLock.release();

		return reqSps;
	}

	@Override
	public void removeAll()
	{
		simulationsManagerLock.acquireUninterruptibly();

		Iterator<Entry<Integer, Simulation>> itr = simulations.entrySet().iterator();

		while (itr.hasNext())
		{
			itr.next().getValue().destroySim();

			itr.remove();
			
			activeSims--;
		}

		simulationsManagerLock.release();
	}

	@Override
	public boolean hasFreeSlot()
	{
		return activeSims < maxSims;
	}

	public int getActiveSims()
	{
		return activeSims;
	}

}
