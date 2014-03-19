package alifeSim.Simulation;

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
import alifeSim.Scenario.Debug.DebugScenario;
import alifeSim.Scenario.Math.LotkaVolterra.LotkaVolterraScenario;
import alifeSim.Scenario.SAPP.SAPPScenario;
import alifeSim.Simulation.SimulationState.SimState;
import alifeSim.Stats.StatManager;

public class SimulationsManager
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

	public int addSimulation()
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		if( activeSims < maxSims)
		{
			simulationNum++;
			activeSims++;
			
			// add sim to struct - index on id & let sim know its id
			simulations.put(simulationNum, new Simulation(simulationNum));		
			
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
	
	public void startSim(int simId)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		Simulation sim = simulations.get(simId);

		sim.startSim();
		
		simulationsManagerLock.release();		
	}
		
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

	public StatManager getStatManager(int simId)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		Simulation sim = simulations.get(simId);
		
		StatManager statManager = null;
		
		if(sim!=null)
		{
			statManager = simulations.get(simId).getSimManager().getStatmanger();	
		}
		
		simulationsManagerLock.release();
		
		return statManager;
	}
	
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
	
	public SimulationScenarioManagerInf getScenarioManager(int simId)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		Simulation sim = simulations.get(simId);
		
		 SimulationScenarioManagerInf scenarioManager = null;
		
		if(sim!=null)
		{			
			scenarioManager = simulations.get(simId).getSimManager();	
		}
		
		simulationsManagerLock.release();
		
		return scenarioManager;
	}

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

	public boolean createSimScenario(int simId, String scenarioText)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		boolean created = false;
		
		Simulation sim = simulations.get(simId);
		
		ScenarioInf scenario = determinScenarios(scenarioText);

		if(sim!=null && scenario!=null)
		{
			sim.createSimScenario(scenario);
			created = true;
		}
		
		simulationsManagerLock.release();
		
		return created;
	}
	
	private ScenarioInf determinScenarios(String text)
	{
		ScenarioVT scenarioParser = null;

		ScenarioInf simScenario = null;

		scenarioParser = new ScenarioVT();

		// To get the type of Scenario object to create.
		scenarioParser.loadConfig(text);

		DebugLogger.output("Scenario Type : " + scenarioParser.getScenarioType());

		if (scenarioParser.getScenarioType().equalsIgnoreCase("DEBUG"))
		{
			DebugLogger.output("Debug File");
			simScenario = new DebugScenario(text);
		}
		else
		{
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
		}

		return simScenario;
	}
	
	/*
	public int addSimulation(String scenarioText)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		if( activeSims < maxSims)
		{
			// Determine Scenario Type
			ScenarioInf scenario = determinScenarios(scenarioText);
			
			// Valid type in scenario
			if(scenario!=null)
			{
				Simulation sim = new Simulation(simulationNum);

				simulationNum++;
				
				activeSims++;
				
				sim.createSimScenario(scenario);
				
				// add sim to struct - index on id & let sim know its id
				simulations.put(simulationNum,sim);		
				
				
				simulationManagerListenerEventNotification(simulationNum,SimulationManagerEvent.AddedSim);				
			}
			else
			{
				simulationNum = -2;
			}

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

	private ScenarioInf determinScenarios(String text)
	{
		ScenarioVT scenarioParser = null;

		ScenarioInf simScenario = null;

		scenarioParser = new ScenarioVT();

		// To get the type of Scenario object to create.
		scenarioParser.loadConfig(text);

		DebugLogger.output("Scenario Type : " + scenarioParser.getScenarioType());

		if (scenarioParser.getScenarioType().equalsIgnoreCase("DEBUG"))
		{
			DebugLogger.output("Debug File");
			simScenario = new DebugScenario(text);
		}
		else
		{
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
		}

		return simScenario;
	}
	*/
	
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
	public void resetActiveSimCamera()
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		if(simView!=null)
		{
			simView.resetCamera();
		}
		
		simulationsManagerLock.release();
	}
	
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
		
	public int getMaxSims()
	{
		return maxSims;
	}

	public int getActiveSims()
	{
		return activeSims;
	}
	
	public void addSimulationManagerListener(SimulationsManagerEventListenerInf listener)
	{
		listenersLock.acquireUninterruptibly();
			simulationsManagerListeners.add(listener);
	    listenersLock.release();
	}
	
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
	};
	
}
