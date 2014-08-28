package jCompute.Simulation.SimulationManager.Local;

import jCompute.JComputeEventBus;
import jCompute.Debug.DebugLogger;
import jCompute.Gui.View.GUISimulationView;
import jCompute.Scenario.ScenarioInf;
import jCompute.Scenario.ScenarioVT;
import jCompute.Scenario.Math.LotkaVolterra.LotkaVolterraScenario;
import jCompute.Scenario.Math.Mandelbrot.MandelbrotScenario;
import jCompute.Scenario.SAPP.SAPPScenario;
import jCompute.Simulation.Simulation;
import jCompute.Simulation.SimulationScenarioManagerInf;
import jCompute.Simulation.SimulationManager.SimulationsManagerInf;
import jCompute.Simulation.SimulationManager.Event.SimulationsManagerEvent;
import jCompute.Simulation.SimulationManager.Event.SimulationsManagerEventType;
import jCompute.Simulation.SimulationState.SimState;
import jCompute.Stats.StatExporter;
import jCompute.Stats.StatExporter.ExportFormat;
import jCompute.Stats.StatGroupListenerInf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

public class SimulationsManager implements SimulationsManagerInf
{
	private static Semaphore simulationsManagerLock = new Semaphore(1);

	/* Max Concurrent Simulations */
	private final int maxSims;
	private int activeSims;	
	
	/* Simulation Storage Structure */
	private HashMap<Integer, Simulation> simulations;
	
	/* Total Count of Simulations Ran - used for simulation ID */
	private int simulationNum;
	
	/* Active Sims View */
	private GUISimulationView simView;
	private Simulation activeSim;
	
	public SimulationsManager(int maxSims)
	{
		DebugLogger.output("Created Simulations Manager");
		
		this.maxSims = maxSims;
		
		simulations = new HashMap<Integer, Simulation>();
		
		this.simulationNum = 0;
		this.activeSims = 0;
		
		DebugLogger.output("Max Active Sims : " + maxSims);		
	}
	
	@Override
	public int addSimulation(String scenarioText, int intialStepRate)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		if( activeSims < maxSims)
		{
			simulationNum++;
			activeSims++;
			
			// Create a sim & let sim know its id
			Simulation sim = new Simulation(simulationNum);
			
			// Validate Scenario
			ScenarioInf scenario = determinScenarios(scenarioText);

			if(sim!=null && scenario!=null)
			{
				sim.createSimScenario(scenario);
				
				sim.setReqStepRate(intialStepRate);
				
				// add sim to struct - index on simId
				simulations.put(simulationNum, sim);
			}
			else
			{
				sim.destroySim();
				simulationNum--;
				
				simulationsManagerLock.release();
				
				return -1;
			}

			// simulationManagerListenerEventNotification(simulationNum,SimulationManagerEvent.AddedSim);
			JComputeEventBus.post(new SimulationsManagerEvent(simulationNum,SimulationsManagerEventType.AddedSim));
			
			simulationsManagerLock.release();

			return simulationNum;
		}
		else
		{
			DebugLogger.output("Reached Max Active Sims");

			simulationsManagerLock.release();

			return -1;
		}

	}
	
	@Override
	public void removeSimulation(int simId)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		Simulation sim = simulations.remove(simId);
		DebugLogger.output(">>>>>> removeSimulation ("+simId+")");

		activeSims--;
		
		if(sim!=null)
		{
			sim.destroySim();

			if(activeSim == sim)
			{
				// Clear the Active Simulation View  Reference
				if(simView!=null)
				{
					simView.setSim(null);
				}
				
				activeSim = null;
			}
			
			DebugLogger.output(">>>>>> removeSimulation SimulationsManagerEventType.RemovedSim ("+simId+")");

			// simulationManagerListenerEventNotification(simId,SimulationManagerEvent.RemovedSim);
			JComputeEventBus.post(new SimulationsManagerEvent(simId,SimulationsManagerEventType.RemovedSim));
			
			simulationsManagerLock.release();
			
			return;
		}

		
		simulationsManagerLock.release();

	}
	
	@Override
	public void startSim(int simId)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		Simulation sim = simulations.get(simId);

		sim.startSim();
		
		simulationsManagerLock.release();		
	}
		
	@Override
	public void pauseSim(int simId)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		Simulation sim = simulations.get(simId);

		if(sim!=null)
		{
			sim.pauseSim();
		}
		
		simulationsManagerLock.release();		
	}
	
	@Override
	public String getScenarioText(int simId)
	{
		simulationsManagerLock.acquireUninterruptibly();

		String scenarioText = "NONE";
		
		Simulation sim = simulations.get(simId);
		
		SimulationScenarioManagerInf scenarioManager = null;
		
		if(sim!=null)
		{			
			scenarioManager = simulations.get(simId).getSimManager();	
			
			scenarioText = scenarioManager.getScenario().getScenarioText();

		}
		
		simulationsManagerLock.release();	
		
		return scenarioText;
	}
	
	@Override
	public void setReqSimStepRate(int simId, int stepRate)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		Simulation sim = simulations.get(simId);
		
		if(sim!=null)
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
		
		if(sim!=null)
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
		
		if(sim!=null)
		{	
			sim.unPauseSim();
		}
		
		simulationsManagerLock.release();
		
	}
	
	private ScenarioInf determinScenarios(String text)
	{
		ScenarioVT scenarioParser = null;

		ScenarioInf simScenario = null;

		scenarioParser = new ScenarioVT();

		// To get the type of Scenario object to create.
		scenarioParser.loadConfig(text);

		DebugLogger.output("Scenario Type : " + scenarioParser.getScenarioType());

		if (scenarioParser.getScenarioType().equalsIgnoreCase("SAPP"))
		{
			DebugLogger.output("SAPP File");
			simScenario = new SAPPScenario();

			simScenario.loadConfig(text);

		}
		else if(scenarioParser.getScenarioType().equalsIgnoreCase("LV"))
		{
			DebugLogger.output("LV File");
			simScenario = new LotkaVolterraScenario();

			simScenario.loadConfig(text);
		}
		else if(scenarioParser.getScenarioType().equalsIgnoreCase("Mandelbrot"))
		{
			DebugLogger.output("Mandelbrot File");
			simScenario = new MandelbrotScenario();

			simScenario.loadConfig(text);
		}
		else
		{
			DebugLogger.output("DeterminScenarios :UKNOWN");
		}

		return simScenario;
	}
	
	@Override
	public void setActiveSim(int simId)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		Simulation sim = simulations.get(simId);
		
		DebugLogger.output("Active Sim : " + simId);
		
		if(simView!=null)
		{
			simView.setSim(sim);
		}
		
		// We latch the active sim incase we do add a view.
		activeSim = sim;
		
		
		simulationsManagerLock.release();
	}

	@Override
	public void setSimView(GUISimulationView simView)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		this.simView = simView;
		
		// Assuming there has been an active sim and there is currently  a simView
		if(simView!=null)
		{
			// Set the latched active sim in the new view.
			if(activeSim!=null)
			{
				simView.setSim(activeSim);
			}
			
		}

		simulationsManagerLock.release();
	}
	
	@Override
	public void clearActiveSim()
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		if(simView!=null)
		{
			simView.setSim(null);
		}
		
		simulationsManagerLock.release();
		
	}

	/* Will Reset the Camera of the active */
	@Override
	public void resetActiveSimCamera()
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		if(simView!=null)
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

		if(simSet!=null)
		{
			for(Integer id : simSet)
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

		if(simSet!=null)
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
	public int getActiveSims()
	{
		return activeSims;
	}

	public SimState getState(int simId)
	{
		SimState simState = null;
		
		simulationsManagerLock.acquireUninterruptibly();

		Simulation sim = simulations.get(simId);
		
		if(sim!=null)
		{	
			simState = sim.getState();
		}
		
		simulationsManagerLock.release();	
		
		return simState;
		
	}
	
	public String getEndEvent(int simId)
	{
		simulationsManagerLock.acquireUninterruptibly();

		Simulation sim = simulations.get(simId);
		
		String endEvent = null;

		if(sim!=null)
		{	
			endEvent = sim.getSimManager().getEndEvent();
		}
		
		simulationsManagerLock.release();	
		
		return endEvent;
		
	}
	
	@Override
	public Set<String> getStatGroupNames(int simId)
	{
		simulationsManagerLock.acquireUninterruptibly();

		Simulation sim = simulations.get(simId);
		
		Set<String> statGroupNames = null;
		
		if(sim!=null)
		{
			statGroupNames = sim.getSimManager().getStatmanger().getGroupList();
		}
		
		simulationsManagerLock.release();	

		return statGroupNames;
	}
	
	@Override 
	public void exportAllStatsToDir(int simId,String directory,String fileNameSuffix, ExportFormat format)
	{
		simulationsManagerLock.acquireUninterruptibly();

		Simulation sim = simulations.get(simId);
		
		DebugLogger.output("Exporting Stats for " + simId);
		
		if(sim!=null)
		{
			/* Pause the sim as it will be updating its internal data structures
			 * Only pause sim if running or it will dead lock.
			 */
			if(sim.getState() == SimState.RUNNING)
			{
				sim.pauseSim();
			}
			
			
			// Create a stat exporter with a stat manager as data source.
			StatExporter exporter = new StatExporter(sim.getSimManager().getStatmanger(), fileNameSuffix, format);
			
			// Export the stats
			exporter.exportAllStatsToDir(directory);
		}
		
		simulationsManagerLock.release();
	}
	
	@Override
	public boolean isStatGroupGraphingEnabled(int simId, String group)
	{
		simulationsManagerLock.acquireUninterruptibly();

		Simulation sim = simulations.get(simId);
		
		boolean enabled = false;
		
		if(sim!=null)
		{
			enabled = sim.getSimManager().getStatmanger().getStatGroup(group).getGroupSettings().graphEnabled();
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
		
		if(sim!=null)
		{
			window = sim.getSimManager().getStatmanger().getStatGroup(group).getGroupSettings().getGraphSampleWindow();
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
		
		if(sim!=null)
		{
			globalStat = sim.getSimManager().getStatmanger().getStatGroup(group).getGroupSettings().hasTotalStat();
		}
		
		simulationsManagerLock.release();	

		return globalStat;
	}
	
	@Override
	public void addStatGroupListener(int simId, String group, StatGroupListenerInf listener)
	{
		simulationsManagerLock.acquireUninterruptibly();

		Simulation sim = simulations.get(simId);
		
		if(sim!=null)
		{
			sim.getSimManager().getStatmanger().getStatGroup(group).addStatGroupListener(listener);
		}
		
		simulationsManagerLock.release();	

	}
	
	@Override
	public void removeStatGroupListener(int simId, String group, StatGroupListenerInf listener)
	{
		simulationsManagerLock.acquireUninterruptibly();

		Simulation sim = simulations.get(simId);
		
		if(sim!=null)
		{
			sim.getSimManager().getStatmanger().getStatGroup(group).removeStatGroupListener(listener);
		}
		
		simulationsManagerLock.release();	

	}
	
	public int getReqSps(int simId)
	{
		int reqSps = -1;
		
		simulationsManagerLock.acquireUninterruptibly();

		Simulation sim = simulations.get(simId);
		
		if(sim!=null)
		{	
			reqSps = sim.getReqSps();
		}
		
		simulationsManagerLock.release();	
		
		return reqSps;
	}
	
}
