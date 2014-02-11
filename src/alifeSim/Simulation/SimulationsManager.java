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
import alifeSim.Simulation.SimulationState.SimStatus;
import alifeSim.Stats.StatManager;

public class SimulationsManager
{
	private static Semaphore simulationsManagerLock = new Semaphore(1);

	/* Max Concurrent Simulations */
	private final int maxSims;
	
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

	public int addSimulation()
	{
		int simId = 0;
		simulationsManagerLock.acquireUninterruptibly();
		
		simulationNum++;
		
		simId = simulationNum;
		
		// add sim to struct - index on id
		simulations.put(simId, new Simulation());
		
		
		simulationsManagerLock.release();
		
		return simId;
	}
	
	public void removeSimulation(int simId)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		Simulation sim = simulations.remove(simId);
		
		if(sim!=null)
		{	
			// Clear the Active Simulation Reference
			simView.setSim(null);
			
			sim.destroySim();
		}
		
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

	public SimStatus togglePause(int simId)
	{
		SimStatus SimStatus = null;
		
		simulationsManagerLock.acquireUninterruptibly();

		Simulation sim = simulations.get(simId);
		
		if(sim!=null)
		{	
			SimStatus = sim.togglePause();
		}
		
		simulationsManagerLock.release();	
		
		return SimStatus;
		
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
	
	public SimStatus getSimStatus(int simId)
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		Simulation sim = simulations.get(simId);
		
		SimStatus status;
		
		if(sim!=null)
		{	
			status = sim.getStatus();
		}
		else
		{
			status = SimStatus.NEW;
		}
		
		simulationsManagerLock.release();
		
		return status;
	}
	
	public int getMaxSims()
	{
		return maxSims;
	}
	
}
