package jcompute.simulationmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Semaphore;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jcompute.JComputeEventBus;
import jcompute.gui.view.View;
import jcompute.results.export.ExportFormat;
import jcompute.results.export.Result;
import jcompute.results.trace.group.TraceGroupListenerInf;
import jcompute.scenario.ScenarioInf;
import jcompute.scenario.ScenarioPluginManager;
import jcompute.simulation.Simulation;
import jcompute.simulation.SimulationState.SimState;
import jcompute.simulationmanager.event.SimulationsManagerEvent;
import jcompute.simulationmanager.event.SimulationsManagerEventType;
import jcompute.simulationmanager.returnables.AddSimStatus;

public class SimulationsManager
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(SimulationsManager.class);
	
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
	
	public AddSimStatus addSimulation(String scenarioText, int intialStepRate)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		if(activeSims < maxSims)
		{
			simulationNum++;
			activeSims++;
			
			// Create a sim & let sim know its id
			Simulation sim = new Simulation(simulationNum);
			
			// Validate Scenario
			ScenarioInf scenario = ScenarioPluginManager.getScenario(scenarioText);
			
			if(sim != null && scenario != null)
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
				activeSims--;
				simulationsManagerLock.release();
				
				log.error("Add sim failed - unable to get scenario.");
				
				// return -1;
				// Add failed
				return new AddSimStatus(-1, false, null);
			}
			
			JComputeEventBus.post(new SimulationsManagerEvent(simulationNum, SimulationsManagerEventType.AddedSim));
			
			simulationsManagerLock.release();
			
			// return simulationNum;
			
			return new AddSimStatus(simulationNum, scenario.needsData(), scenario.dataFileNames());
		}
		else
		{
			log.warn("Reached Max Active Sims");
			
			simulationsManagerLock.release();
			
			// Add failed
			return new AddSimStatus(-1, false, null);
		}
	}
	
	public void removeSimulation(int simId)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		Simulation sim = simulations.remove(simId);
		
		activeSims--;
		
		if(sim != null)
		{
			sim.destroySim();
			
			if(activeSim == sim)
			{
				// Clear the Active Simulation View Reference
				if(simView != null)
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
	
	public void startSim(int simId, byte[][] data)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		log.info("Start Sim " + simId);
		
		Simulation sim = simulations.get(simId);
		
		sim.startSim(data);
		
		simulationsManagerLock.release();
	}
	
	public void pauseSim(int simId)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		Simulation sim = simulations.get(simId);
		
		if(sim != null)
		{
			sim.pauseSim();
		}
		
		simulationsManagerLock.release();
	}
	
	public String getScenarioText(int simId)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		String scenarioText = "No Simulation";
		
		Simulation sim = simulations.get(simId);
		
		if(sim != null)
		{
			scenarioText = sim.getScenarioText();
		}
		
		simulationsManagerLock.release();
		
		return scenarioText;
	}
	
	public void setReqSimStepRate(int simId, int stepRate)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		Simulation sim = simulations.get(simId);
		
		if(sim != null)
		{
			sim.setReqStepRate(stepRate);
		}
		
		simulationsManagerLock.release();
		
	}
	
	public SimState togglePause(int simId)
	{
		SimState simState = null;
		
		simulationsManagerLock.acquireUninterruptibly();
		
		Simulation sim = simulations.get(simId);
		
		if(sim != null)
		{
			simState = sim.togglePause();
		}
		
		simulationsManagerLock.release();
		
		return simState;
	}
	
	public void unPauseSim(int simId)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		Simulation sim = simulations.get(simId);
		
		if(sim != null)
		{
			sim.unPauseSim();
		}
		
		simulationsManagerLock.release();
	}
	
	public void setActiveSim(int simId)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		Simulation sim = simulations.get(simId);
		
		log.debug("Active Sim : " + simId);
		
		if(simView != null)
		{
			simView.setViewTarget(sim);
		}
		
		// We latch the active sim incase we do add a view.
		activeSim = sim;
		
		simulationsManagerLock.release();
	}
	
	public void setSimView(View simView)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		this.simView = simView;
		
		// Assuming there has been an active sim and there is currently a
		// simView
		if(simView != null)
		{
			// Set the latched active sim in the new view.
			if(activeSim != null)
			{
				simView.setViewTarget(activeSim);
			}
		}
		
		simulationsManagerLock.release();
	}
	
	public void clearActiveSim()
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		if(simView != null)
		{
			simView.setViewTarget(null);
		}
		
		simulationsManagerLock.release();
	}
	
	public List<Simulation> getSimList()
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		Set<Integer> simSet = simulations.keySet();
		List<Simulation> list = new LinkedList<Simulation>();
		
		if(simSet != null)
		{
			for(Integer id : simSet)
			{
				list.add(simulations.get(id));
			}
		}
		
		simulationsManagerLock.release();
		
		return list;
	}
	
	public List<Integer> getSimIdList()
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		Set<Integer> simSet = simulations.keySet();
		
		List<Integer> list = null;
		
		if(simSet != null)
		{
			list = new ArrayList<Integer>(simSet);
		}
		
		simulationsManagerLock.release();
		
		return list;
	}
	
	public boolean hasSimWithId(int simId)
	{
		boolean simIdExists = false;
		
		simulationsManagerLock.acquireUninterruptibly();
		
		Simulation sim = simulations.get(simId);
		
		if(sim != null)
		{
			simIdExists = true;
		}
		
		simulationsManagerLock.release();
		
		return simIdExists;
	}
	
	public int getMaxSims()
	{
		return maxSims;
	}
	
	public SimState getState(int simId)
	{
		SimState simState = null;
		
		simulationsManagerLock.acquireUninterruptibly();
		
		Simulation sim = simulations.get(simId);
		
		if(sim != null)
		{
			simState = sim.getState();
		}
		
		simulationsManagerLock.release();
		
		return simState;
	}
	
	public Set<String> getTraceGroupNames(int simId)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		Simulation sim = simulations.get(simId);
		
		Set<String> statGroupNames = null;
		
		if(sim != null)
		{
			statGroupNames = sim.getResultManager().getTraceGroupNames();
		}
		
		simulationsManagerLock.release();
		
		return statGroupNames;
	}
	
	public Result getResultExporter(int simId, String fileNameSuffix, ExportFormat format)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		Simulation sim = simulations.get(simId);
		
		log.info("Creating result exporter for Simulation " + simId);
		
		Result exporter = null;
		
		if(sim != null)
		{
			/*
			 * Pause the sim as it will be updating its internal data structures
			 * Only pause sim if running or it will dead lock.
			 */
			if(sim.getState() == SimState.RUNNING)
			{
				sim.pauseSim();
			}
			
			// Create a stat exporter with export format.
			exporter = new Result(format, fileNameSuffix);
			
			// populate from the result manager as data source.
			exporter.populateFromResultManager(sim.getResultManager());
		}
		
		simulationsManagerLock.release();
		
		return exporter;
	}
	
	public boolean isTraceGroupGraphingEnabled(int simId, String group)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		Simulation sim = simulations.get(simId);
		
		boolean enabled = false;
		
		if(sim != null)
		{
			enabled = sim.getResultManager().getTraceGroup(group).getGroupSettings().graphEnabled();
		}
		
		simulationsManagerLock.release();
		
		return enabled;
	}
	
	public int getTraceGroupGraphSampleWindowSize(int simId, String group)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		Simulation sim = simulations.get(simId);
		
		int window = -1;
		
		if(sim != null)
		{
			window = sim.getResultManager().getTraceGroup(group).getGroupSettings().getGraphSampleWindow();
		}
		
		simulationsManagerLock.release();
		
		return window;
	}
	
	public boolean hasTraceGroupTotalStat(int simId, String group)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		Simulation sim = simulations.get(simId);
		
		boolean globalStat = false;
		
		if(sim != null)
		{
			globalStat = sim.getResultManager().getTraceGroup(group).getGroupSettings().hasTotalStat();
		}
		
		simulationsManagerLock.release();
		
		return globalStat;
	}
	
	public void addTraceGroupListener(int simId, String group, TraceGroupListenerInf listener)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		Simulation sim = simulations.get(simId);
		
		if(sim != null)
		{
			sim.getResultManager().getTraceGroup(group).addGroupListener(listener);
		}
		
		simulationsManagerLock.release();
	}
	
	public void removeStatGroupListener(int simId, String group, TraceGroupListenerInf listener)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		Simulation sim = simulations.get(simId);
		
		if(sim != null)
		{
			sim.getResultManager().getTraceGroup(group).removeGroupListener(listener);
		}
		
		simulationsManagerLock.release();
	}
	
	public int getReqSps(int simId)
	{
		int reqSps = -1;
		
		simulationsManagerLock.acquireUninterruptibly();
		
		Simulation sim = simulations.get(simId);
		
		if(sim != null)
		{
			reqSps = sim.getReqSps();
		}
		
		simulationsManagerLock.release();
		
		return reqSps;
	}
	
	public void removeAll()
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		Iterator<Entry<Integer, Simulation>> itr = simulations.entrySet().iterator();
		
		while(itr.hasNext())
		{
			itr.next().getValue().destroySim();
			
			itr.remove();
			
			activeSims--;
		}
		
		simulationsManagerLock.release();
	}
	
	public boolean hasFreeSlot()
	{
		return activeSims < maxSims;
	}
	
	public int getActiveSims()
	{
		return activeSims;
	}
	
}
