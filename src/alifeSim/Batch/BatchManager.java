package alifeSim.Batch;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import alifeSim.Simulation.SimulationsManager;

public class BatchManager
{
	private SimulationsManager simsManager;

	Queue<Batch> batches;
	
	public BatchManager(SimulationsManager simsManager)
	{
		this.simsManager = simsManager;
		
		batches = new LinkedBlockingQueue<Batch>();
	}
	
	public void addBatch(Batch batch)
	{
		batches.add(batch);
		
		// batch finished listener add
	}
	
	
	// batch lister finished == batches remove
}
