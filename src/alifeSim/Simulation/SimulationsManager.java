package alifeSim.Simulation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.Semaphore;

import alifeSim.ChartPanels.StatPanelAbs;
import alifeSim.Gui.NewSimView;
import alifeSim.Scenario.ScenarioInf;
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
		Simulation sim = simulations.get(simId);
		
		StatManager statManager = null;
		
		if(sim!=null)
		{			
			statManager = simulations.get(simId).getSimManager().getStatmanger();	
		}
		
		return statManager;
	}

	public void setReqSimStepRate(int simId, int stepRate)
	{
		Simulation sim = simulations.get(simId);
		
		if(sim!=null)
		{	
			sim.setReqStepRate(stepRate);
		}
		
	}

	public boolean isSimPaused(int simId)
	{
		Simulation sim = simulations.get(simId);
		
		if(sim!=null)
		{	
			return sim.simPaused();
		}

		return false;
	}

	public void unPauseSim(int simId)
	{
		Simulation sim = simulations.get(simId);
		
		if(sim!=null)
		{	
			sim.unPauseSim();
		}
		
	}

	public void destroySimulation(int simId)
	{
		Simulation sim = simulations.get(simId);
		
		if(sim!=null)
		{	
			clearActiveSim();
			
			sim.destroySim();
		}
		
	}

	public void createSimScenario(int simId, ScenarioInf simScenario)
	{
		Simulation sim = simulations.get(simId);
		
		if(sim!=null)
		{	
			sim.createSimScenario(simScenario);
		}
		
	}

	public void setSimOutPutCharts(int simId, LinkedList<StatPanelAbs> charts)
	{
		Simulation sim = simulations.get(simId);
		
		if(sim!=null)
		{	
			sim.setOutPutCharts(charts);
		}
		
	}
	
	public void setActiveSim(int simId)
	{
		Simulation sim = simulations.get(simId);
		
		if(simView!=null)
		{
			simView.setSim(sim);
		}
		
	}

	public void setSimView(NewSimView simView)
	{
		this.simView = simView;
	}
	
	public void clearActiveSim()
	{
		if(simView!=null)
		{
			simView.setSim(null);
		}
		
	}
	
}
