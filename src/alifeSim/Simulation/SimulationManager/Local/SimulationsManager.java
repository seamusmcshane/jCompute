package alifeSim.Simulation.SimulationManager.Local;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

import alifeSim.Debug.DebugLogger;
import alifeSim.Gui.View.GUISimulationView;
import alifeSim.Scenario.ScenarioInf;
import alifeSim.Scenario.ScenarioVT;
import alifeSim.Scenario.Math.LotkaVolterra.LotkaVolterraScenario;
import alifeSim.Scenario.SAPP.SAPPScenario;
import alifeSim.Simulation.Simulation;
import alifeSim.Simulation.SimulationScenarioManagerInf;
import alifeSim.Simulation.SimulationStatListenerInf;
import alifeSim.Simulation.SimulationState;
import alifeSim.Simulation.SimulationStateListenerInf;
import alifeSim.Simulation.SimulationManager.SimulationsManagerInf;
import alifeSim.Simulation.SimulationState.SimState;
import alifeSim.Stats.StatGroupListenerInf;
import alifeSim.Stats.StatManager;

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
	
	/* SimulationsManger Listeners */
	private List<SimulationsManagerEventListenerInf> simulationsManagerListeners = new ArrayList<SimulationsManagerEventListenerInf>();
	private Semaphore listenersLock = new Semaphore(1, false);	
	
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

			simulationManagerListenerEventNotification(simulationNum,SimulationManagerEvent.AddedSim);

			simulationsManagerLock.release();
			
			return simulationNum;
		}
		else
		{
			DebugLogger.output("Too Many Sims");

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
		
		if(sim!=null)
		{
			sim.destroySim();
			
			DebugLogger.output("simulationManagerListenerEventNotification");

			simulationManagerListenerEventNotification(simId,SimulationManagerEvent.RemovedSim);
			
			if(activeSim == sim)
			{
				// Clear the Active Simulation View  Reference
				if(simView!=null)
				{
					simView.setSim(null);
				}
				
				activeSim = null;
			}
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
	public long getSimRunTime(int simId)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		Simulation sim = simulations.get(simId);
		
		long runTime = 0;
		
		if(sim!=null)
		{
			runTime = sim.getTotalTime();
		}
		
		simulationsManagerLock.release();
		
		return runTime;
	}
	
	@Override
	public long getSimStepCount(int simId)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		Simulation sim = simulations.get(simId);
		
		long stepCount = 0;
		
		if(sim!=null)
		{
			stepCount = sim.getTotalSteps();
		}
		
		simulationsManagerLock.release();
		
		return stepCount;
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
	
	@Override
	public void addSimulationManagerListener(SimulationsManagerEventListenerInf listener)
	{
		listenersLock.acquireUninterruptibly();
			simulationsManagerListeners.add(listener);
	    listenersLock.release();
	}
	
	@Override
	public void removeSimulationManagerListener(SimulationsManagerEventListenerInf listener)
	{
		listenersLock.acquireUninterruptibly();
			simulationsManagerListeners.remove(listener);
	    listenersLock.release();
	}
	
	private void simulationManagerListenerEventNotification(int simId,SimulationManagerEvent event)
	{
	    for (SimulationsManagerEventListenerInf listener : simulationsManagerListeners)
	    {
	    	listener.SimulationsManagerEvent(simId,event);
	    }
	}
	
	@Override
	public void addSimulationStateListener(int simId,SimulationStateListenerInf listener)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		Simulation sim = simulations.get(simId);
		
		if(sim!=null)
		{
			sim.addSimulationStateListener(listener);
		}
		
		simulationsManagerLock.release();
	}
	
	@Override
	public void removeSimulationStateListener(int simId,SimulationStateListenerInf listener)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		Simulation sim = simulations.get(simId);
		
		if(sim!=null)
		{
			sim.removeSimulationStateListener(listener);
		}
		
		simulationsManagerLock.release();
	}
	
	@Override
	public void addSimulationStatListener(int simId,SimulationStatListenerInf listener)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		Simulation sim = simulations.get(simId);
		
		if(sim!=null)
		{
			sim.addSimulationStatListener(listener);
		}
		
		simulationsManagerLock.release();
	}
	
	@Override
	public void removeSimulationStatListener(int simId,SimulationStatListenerInf listener)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		Simulation sim = simulations.get(simId);
		
		if(sim!=null)
		{
			sim.removeSimulationStatListener(listener);
		}
		
		simulationsManagerLock.release();
	}
	
	public void addStatGroupListener (int simId,String group)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		Simulation sim = simulations.get(simId);
		
		if(sim!=null)
		{
			//sim.addSimulationStatListener(listener);
		}
		
		simulationsManagerLock.release();
	}
	
	public void removeStatGroupListener (int simId,String group)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		Simulation sim = simulations.get(simId);
		
		if(sim!=null)
		{
			//sim.addSimulationStatListener(listener);
		}
		
		simulationsManagerLock.release();
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
	public void exportAllStatsToDir(int simId,String directory,String fileNameSuffix, String format)
	{
		simulationsManagerLock.acquireUninterruptibly();

		Simulation sim = simulations.get(simId);
		
		if(sim!=null)
		{
			/* Pause the sim as it will be updating its internal data structures
			 * Only pause sim if running or it will dead lock.
			 */
			if(sim.getState() == SimState.RUNNING)
			{
				sim.pauseSim();
			}
			sim.getSimManager().getStatmanger().exportAllStatsToDir(directory, fileNameSuffix, format);

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
	
	/** Manager Events */
	public enum SimulationManagerEvent
	{
		AddedSim	("Added Sim"),
		RemovedSim	("Removed Sim");

	    private final String name;

	    private SimulationManagerEvent(String name) 
	    {
	        this.name = name;
	    }

	    public String toString()
	    {
	       return name;
	    }
	}
	
}
