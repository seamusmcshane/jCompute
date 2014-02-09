package alifeSim.Simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

import alifeSim.ChartPanels.StatPanelAbs;
import alifeSim.Gui.NewSimView;
import alifeSim.Scenario.ScenarioInf;
import alifeSim.Simulation.Simulation.SimulationState;
import alifeSim.Stats.StatManager;

public class SimulationsManager
{
	private static Semaphore simulationsManagerLock = new Semaphore(1);

	/* Max Concurrent Simulations */
	private int maxSims;
	
	/* Simulation Storage Struct */
	private HashMap<Integer, Simulation> simulations;
	
	/* Total Count of Simulations Ran - used for simulation ID */
	private int simulationNum;
	
	/* Active Sims View */
	private NewSimView simView;
		
	public SimulationsManager(int maxSims)
	{		
		System.out.println("Created Simulations Manager");
		
		this.maxSims = maxSims;
		
		simulations = new HashMap<Integer, Simulation>();
		
		this.simulationNum = 0;
		
		System.out.println("Max Active Sims : " + maxSims);		
	}

	public int addSimulation(SimulationPerformanceStats stats)
	{
		int simId = 0;
		simulationsManagerLock.acquireUninterruptibly();
		
		simulationNum++;
		
		simId = simulationNum;
		
		// add sim to struct - index on id
		simulations.put(simId, new Simulation(stats));
		
		
		simulationsManagerLock.release();
		
		return simId;
	}
	
	public void removeSimulation(int simId)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		// remove sim id...
				
		
		simulationsManagerLock.release();
	}
	
	public void startSim(int simId)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		Simulation sim = simulations.get(simId);
		
		if(sim!=null)
		{	
			sim.startSim();
		}	
		
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

	public SimulationState isSimPaused(int simId)
	{
		simulationsManagerLock.acquireUninterruptibly();
				
		Simulation sim = simulations.get(simId);
		
		SimulationState state = null;
		
		if(sim!=null)
		{	
			state = sim.simPaused();
		}

		simulationsManagerLock.release();
		
		return state;
	}

	public SimulationState togglePause(int simId)
	{
		SimulationState state = null;
		
		simulationsManagerLock.acquireUninterruptibly();

		Simulation sim = simulations.get(simId);
		
		if(sim!=null)
		{	
			state = sim.togglePause();
		}		
		
		simulationsManagerLock.release();	
		
		return state;
		
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

	public void destroySimulation(int simId)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		Simulation sim = simulations.get(simId);
		
		if(sim!=null)
		{	
			clearActiveSim();
			
			sim.destroySim();
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
	

	public void setSimOutPutCharts(int simId, LinkedList<StatPanelAbs> charts)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		Simulation sim = simulations.get(simId);
		
		if(sim!=null)
		{	
			sim.setOutPutCharts(charts);
		}
		
		simulationsManagerLock.release();
		
	}
	
	public void setActiveSim(int simId)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		Simulation sim = simulations.get(simId);
		
		if(simView!=null)
		{
			simView.setSim(sim);
		}
		
		simulationsManagerLock.release();
	}

	public void setSimView(NewSimView simView)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		this.simView = simView;
		
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
	
	public List<Integer> getSimList()
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
	
	public SimulationState getSimState(int simId)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		Simulation sim = simulations.get(simId);
		
		SimulationState state;
		
		if(sim!=null)
		{	
			state = sim.getState();
		}
		else
		{
			state = SimulationState.NEW;
		}
		
		simulationsManagerLock.release();
		
		return state;
	}
}
