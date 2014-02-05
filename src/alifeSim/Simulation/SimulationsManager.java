package alifeSim.Simulation;

import java.util.HashMap;
import java.util.concurrent.Semaphore;

public class SimulationsManager
{
	private static Semaphore simulationsManagerLock = new Semaphore(1);

	/* Max Concurrent Simulations */
	private int maxSims;
	
	/* Simulation Storage Struct */
	private HashMap<Integer, Simulation> simulations;
	
	/* Total Count of Simulations Ran - used for simulation ID */
	private int simulationNum;
		
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
		// simulations.put(simId, simulation);
		
		simulationsManagerLock.release();
		
		return simId;
	}
	
	public void removeSimulation()
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		// remove sim id...
		
		simulationsManagerLock.release();
	}
	
	public void startSim()
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		
		
		simulationsManagerLock.release();		
	}
	
	public void pauseSim()
	{
		simulationsManagerLock.acquireUninterruptibly();
		
		
		
		simulationsManagerLock.release();		
	}
	
}
