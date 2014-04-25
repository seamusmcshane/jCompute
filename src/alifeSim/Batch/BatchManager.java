package alifeSim.Batch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

import alifeSim.Debug.DebugLogger;
import alifeSim.Simulation.SimulationManager.SimulationStateListenerInf;
import alifeSim.Simulation.SimulationManager.SimulationsManagerInf;
import alifeSim.Simulation.SimulationManager.Local.SimulationsManager;
import alifeSim.Simulation.SimulationManager.Local.SimulationsManagerEventListenerInf;
import alifeSim.Simulation.SimulationManager.Local.SimulationsManager.SimulationManagerEvent;
import alifeSim.Simulation.SimulationState.SimState;

public class BatchManager implements SimulationsManagerEventListenerInf,SimulationStateListenerInf
{
	// Simulations Manager
	private SimulationsManagerInf simsManager;

	// The queue of batches
	private Queue<Batch> batches;
	private Semaphore batchesQueueLock = new Semaphore(1, false);	

	// The active Items currently being processed from all batches.
	private ArrayList<BatchItem> activeItems; 
	private Semaphore activeItemsLock = new Semaphore(1, false);	

	// Temporary list for completed items.
	private ArrayList<BatchItem> completedItems; 
	private Semaphore completedItemsLock = new Semaphore(1, false);	

	// Batch id counter
	int batchId = 0;
	
	/* Batch Manager Event Listeners */
	private List<BatchManagerEventListenerInf> batchManagerListeners = new ArrayList<BatchManagerEventListenerInf>();
	private Semaphore listenersLock = new Semaphore(1, false);	
	
	private Timer batchSchedulerTimer;
	
	// Batches in FIFO
	private final int fifo = 0;
	// Batches in fair queue (1 item from each batch until queue full)
	private final int fq = 1;
	
	private int batchScheduleMode = fifo;
	
	public BatchManager(SimulationsManagerInf simsManager)
	{
		this.simsManager = simsManager;
		
		batches = new LinkedBlockingQueue<Batch>();
		
		activeItems = new ArrayList<BatchItem>();
		completedItems = new ArrayList<BatchItem>();
		
		// Batch Scheduler Tick
		batchSchedulerTimer = new Timer("Batch Scheduler Timer");
		batchSchedulerTimer.schedule(new TimerTask()
		{
			@Override
			public void run() 
			{
				schedule();
			}
			  
		},0,1000);
	}
	
	public boolean addBatch(String fileName)
	{
		batchesQueueLock.acquireUninterruptibly();
		
		Batch tempBatch;
		boolean added = false;
		
		// Try generating a batch and adding it to the queue.
		try
		{
			tempBatch = new Batch(batchId,fileName);
			
			batches.add(tempBatch);
			
			added = true;
			batchId++;
			
			batchManagerListenerBatchAddedNotification(tempBatch);
			
		}
		catch (IOException e)
		{			
			added = false;
		}
		
		batchesQueueLock.release();
		
		return added;
	}

	/**
	 * Returns a copy of the batch items queue
	 * @param batchId
	 * @return
	 */
	public BatchItem[] getBatchQueue(int batchId)
	{
		Iterator<Batch> itr = batches.iterator();
		Batch temp = null;
		
		while(itr.hasNext())
		{
			temp = itr.next();
			
			if(temp.getBatchId() == batchId)
			{
				break;
			}
		}
				
		// copy of batch not actual batch
		return temp.getQueuedItems();
	}
	
	/**
	 * Returns a copy of the batch items queue
	 * @param batchId
	 * @return
	 */
	public BatchItem[] getActiveQueue(int batchId)
	{
		Iterator<Batch> itr = batches.iterator();
		Batch temp = null;
		
		while(itr.hasNext())
		{
			temp = itr.next();
			
			if(temp.getBatchId() == batchId)
			{
				break;
			}
		}
				
		// copy of batch not actual batch
		return temp.getActiveItems();
	}
	
	@Override
	public void SimulationsManagerEvent(int simId, SimulationManagerEvent event)
	{
		
		if(event == SimulationManagerEvent.AddedSim)
		{
			/*
				// RegiserStateListener
				simsManger.addSimulationStateListener(simId, simulationListTabPanel);
				
				// RegisterStatsListerner
				simsManger.addSimulationStatListener(simId, simulationListTabPanel);
			*/

		}
		else if( event == SimulationManagerEvent.RemovedSim)
		{
			/*
	        	// UnRegisterStatsListerner
	        	simsManger.removeSimulationStatListener(simId, simulationListTabPanel);
	        	
				// UnRegisterStateListener
				simsManger.removeSimulationStateListener(simId, simulationListTabPanel);
			*/
		}
		else
		{
			DebugLogger.output("Unhandled SimulationManagerEvent in Batch Manager");
		}
		
	}

	private void processCompletedItems()
	{
		completedItemsLock.acquireUninterruptibly();
		
		Iterator<BatchItem> itr = completedItems.iterator();
		
		while(itr.hasNext())
		{
			BatchItem item = itr.next();
		
			Batch batch = findBatch(item.getBatchId());
			
			activeItemsLock.acquireUninterruptibly();
				activeItems.remove(item);
				DebugLogger.output("Processed Completed Item : " + item.getItemId() + " Batch : " + item.getBatchId() + " SimId : " + item.getSimId());
			activeItemsLock.release();
			
			int simId = item.getSimId();
			
			// Updates Logs/Exports Stats
			batch.setComplete(simsManager,item,simsManager.getSimRunTime(simId),simsManager.getEndEvent(simId),simsManager.getSimStepCount(simId));	
			
			simsManager.removeSimulationStateListener(item.getSimId(), this);

			simsManager.removeSimulation(item.getSimId());			
			
			batchManagerListenerBatchProgressNotification(batch);

		}
		
		completedItems = new ArrayList<BatchItem>();
		
		completedItemsLock.release();
	}
	
	private void schedule()
	{
		batchesQueueLock.acquireUninterruptibly();		
		
		
		DebugLogger.output("Schedule");

		processCompletedItems();
		
		// Is there a free slot
		if(simsManager.getActiveSims() < simsManager.getMaxSims() && (batches.size() > 0))
		{
			switch(batchScheduleMode)
			{
				case fifo:
					scheduleFifo();
				break;
				case fq:
					// TODO
				break;
				default:
					DebugLogger.output("Batch schedule mode not set!!!");
				break;
			}
		}
		
		batchesQueueLock.release();
	}
	
	private void scheduleFifo()
	{
		DebugLogger.output("Schedule Fifo Tick");
		
		// Get the first batch
		Batch batch = batches.peek();
		
		if(batch == null)
		{
			// no batches
			return;
		}
		
		// Is this batch finished
		if(batch.getCompletedItems() == batch.getBatchItems())
		{
			// Notify listeners the batch is being removed.
			batchManagerListenerBatchRemovedNotification(batch.getBatchId());
			
			// Remove first batch as its complete.
			batches.poll();
			
			// exit this tick
			return;
		}
		
		// While there are items to add and the simulations manager can add them
		while( (batch.getRemaining() > 0))
		{
			int simId = -1;

			simId = simsManager.addSimulation();
			
			// If the simulations manager has add a simulations slot for us
			if(simId > 0)
			{
				// dequeue the next item in the batch and schedule it.
				scheduleBatchItem(batch.getNext(),simId);
			}
			else
			{
				break;
			}
		
		}
	
	}
	
	private void scheduleBatchItem(BatchItem item, int simId)
	{
		activeItemsLock.acquireUninterruptibly();
			activeItems.add(item);
		activeItemsLock.release();
		
		item.setSimId(simId);
		
		simsManager.createSimScenario(simId, item.getConfigText());
		simsManager.setReqSimStepRate(simId, -1);
		simsManager.addSimulationStateListener(simId, this);
		
		simsManager.startSim(simId);		
	}
	
	@Override
	public void simulationStateChanged(int simId, SimState state)
	{
		
		switch(state)
		{
			case RUNNING:
			break;
			case NEW:
			break;
			case FINISHED:
				
				if(state == state.FINISHED)
				{
					BatchItem item = findBatchItemFromSimId(simId);
					
					completedItemsLock.acquireUninterruptibly();
						completedItems.add(item);
					completedItemsLock.release();

				}
				
			break;
			case PAUSED:
			break;	
			default :
				
				for(int i=0;i<100;i++)
				{
					DebugLogger.output("Invalid/Unhandled SimState passed to Batch Manager");
				}
				
			break;
		}
		
		/* if state finished
		 * 	Get the batchItem that relates to this configuration.
		 * 	tell the sim to export its stats
		 * 	to the batchdirectory/batchitemid/stats.csv
		 * 
		 * remove simulation from SimulationsManager
		 * remove batchItem from the active list.
		 * 
		 * if there is still simulations to run in a batch.
		 * get the next simulation to run.
		 * 
		 * add a new simulation with the next batch items config
		 * encode the batchItem with the SimID.
		 * add the batchItem to the active list, remove from the todo list.
		 * 
		 * 
		 */
		

	}
	
	private Batch findBatch(int batchId)
	{
		Iterator<Batch> itr = batches.iterator();
		
		Batch batch = null;
		
		while(itr.hasNext())
		{
			batch = itr.next();
			
			if(batch.getBatchId() == batchId)
			{
				break;
			}

		}
		return batch;
	}
	
	private BatchItem findBatchItemFromSimId(int simId)
	{
		activeItemsLock.acquireUninterruptibly();
		
		Iterator<BatchItem> itr = activeItems.iterator();
		
		BatchItem item = null;
		
		while(itr.hasNext())
		{
			item = itr.next();
			
			if(item.getSimId() == simId)
			{
				break;
			}

		}
		
		activeItemsLock.release();
		
		return item;
	}
	
	public void addBatchManagerListener(BatchManagerEventListenerInf listener)
	{
		listenersLock.acquireUninterruptibly();
			batchManagerListeners.add(listener);
	    listenersLock.release();
	}
	
	public void removeBatchManagerListener(BatchManagerEventListenerInf listener)
	{
		listenersLock.acquireUninterruptibly();
			batchManagerListeners.remove(listener);
	    listenersLock.release();
	}
	
	private void batchManagerListenerBatchAddedNotification(Batch batch)
	{
	    for (BatchManagerEventListenerInf listener : batchManagerListeners)
	    {
	    	listener.batchAdded(batch.getBatchId(),batch.getBaseScenarioFile(),batch.getType(),batch.getBatchItems(),batch.getProgress(),batch.getCompletedItems());
	    }
	}
	
	private void batchManagerListenerBatchRemovedNotification(int batchId)
	{
	    for (BatchManagerEventListenerInf listener : batchManagerListeners)
	    {
	    	listener.batchRemoved(batchId);
	    }
	}
	
	private void batchManagerListenerBatchProgressNotification(Batch batch)
	{
	    for (BatchManagerEventListenerInf listener : batchManagerListeners)
	    {
	    	listener.batchProgress(batch.getBatchId(),batch.getProgress(),batch.getCompletedItems(),batch.getRunTime(),batch.getETT());
	    }
	}

}
