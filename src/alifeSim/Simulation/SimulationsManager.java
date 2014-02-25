package alifeSim.Simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

import alifeSim.Gui.View.GUISimulationView;
import alifeSim.Scenario.ScenarioInf;
import alifeSim.Simulation.SimulationState.SimState;
import alifeSim.Stats.StatManager;

public class SimulationsManager
{
	private static Semaphore simulationsManagerLock = new Semaphore(1);

	/* Max Concurrent Simulations */
	private final int maxSims;
	
	/* Simulation Storage Structure */
	private HashMap<Integer, Simulation> simulations;
	
	/* Total Count of Simulations Ran - used for simulation ID */
	private int simulationNum;
	
	/* Active Sims View */
	private GUISimulationView simView;
	private Simulation activeSim;	
	
	/* SimulationsManger Listeners */
	private List<SimulationsManagerEventListenerInf> simulationsMangerListeners = new ArrayList<SimulationsManagerEventListenerInf>();
	private Semaphore listenersLock = new Semaphore(1, false);	
	
	public SimulationsManager(int maxSims)
	{
		System.out.println("Created Simulations Manager");
		
		this.maxSims = maxSims;
		
		simulations = new HashMap<Integer, Simulation>();
		
		this.simulationNum = 0;
		
		System.out.println("Max Active Sims : " + maxSims);		
	}

	public int addSimulation()
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		simulationNum++;
		
		// add sim to struct - index on id & let sim know its id
		simulations.put(simulationNum, new Simulation(simulationNum));		
		
		simulationManagerListenerEventNotification(simulationNum,SimulationManagerEvent.AddedSim);

		simulationsManagerLock.release();
		
		return simulationNum;
	}
	
	public void removeSimulation(int simId)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		Simulation sim = simulations.remove(simId);
		
		// Clear the Active Simulation Reference
		if(sim == activeSim && simView!=null)
		{
			simView.setSim(null);
		}
		
		if(sim!=null)
		{
			sim.destroySim();
			
			simulationManagerListenerEventNotification(simId,SimulationManagerEvent.RemovedSim);
			
			if(activeSim == sim)
			{
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

		sim.pauseSim();
		
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

	public void createSimScenario(int simId, ScenarioInf simScenario)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		Simulation sim = simulations.get(simId);
		
		if(sim!=null)
		{
			sim.createSimScenario(simScenario);
		}
		
		simulationsManagerLock.release();
	}
	
	public void setActiveSim(int simId)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		Simulation sim = simulations.get(simId);
		
		System.out.println("Active Sim : " + simId);
		
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
	

		
	public void addSimulationManagerListener(SimulationsManagerEventListenerInf listener)
	{
		listenersLock.acquireUninterruptibly();
			simulationsMangerListeners.add(listener);
	    listenersLock.release();
	}
	
	public void removeSimulationManagerListener(SimulationsManagerEventListenerInf listener)
	{
		listenersLock.acquireUninterruptibly();
			simulationsMangerListeners.remove(listener);
	    listenersLock.release();
	}
	
	private void simulationManagerListenerEventNotification(int simId,SimulationManagerEvent event)
	{
	    for (SimulationsManagerEventListenerInf listener : simulationsMangerListeners)
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
