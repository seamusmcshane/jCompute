package jCompute.Gui.Batch;

import jCompute.Debug.DebugLogger;
import jCompute.Simulation.Listener.SimulationStateListenerInf;
import jCompute.Simulation.SimulationManager.SimulationsManagerInf;
import jCompute.Simulation.SimulationManager.Local.SimulationsManager;
import jCompute.Simulation.SimulationManager.Local.SimulationsManagerEventListenerInf;
import jCompute.Simulation.SimulationManager.Local.SimulationsManager.SimulationManagerEvent;
import jCompute.Simulation.SimulationState.SimState;

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

public class BatchManager implements SimulationsManagerEventListenerInf,SimulationStateListenerInf
{
	// Simulations Manager
	private SimulationsManagerInf simsManager;

	// The queue of batches
	private Queue<Batch> queuedBatches;
	private ArrayList<Batch> finishedBatches;
	
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
		
		queuedBatches = new LinkedBlockingQueue<Batch>();
		finishedBatches = new ArrayList<Batch>(16);
		
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
			
			queuedBatches.add(tempBatch);
			
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
	public BatchItem[] getItemQueue(int batchId)
	{
		batchesQueueLock.acquireUninterruptibly();

		Iterator<Batch> itr = queuedBatches.iterator();
		Batch temp = null;
		
		while(itr.hasNext())
		{
			temp = itr.next();
			
			if(temp.getBatchId() == batchId)
			{
				break;
			}
		}
		
		// If did not find batch in queued batches check the completed batches
		if(temp == null)
		{
			itr = finishedBatches.iterator();
			
			while(itr.hasNext())
			{
				temp = itr.next();
				
				if(temp.getBatchId() == batchId)
				{
					break;
				}
			}
		}
		
		batchesQueueLock.release();
		
		// copy of batch not actual batch
		return temp.getQueuedItems();
	}
	
	/**
	 * Returns a copy of the batch items queue
	 * @param batchId
	 * @return
	 */
	public BatchItem[] getActiveItems(int batchId)
	{
		batchesQueueLock.acquireUninterruptibly();

		Iterator<Batch> itr = queuedBatches.iterator();
		Batch temp = null;
		
		while(itr.hasNext())
		{
			temp = itr.next();
			
			if(temp.getBatchId() == batchId)
			{
				break;
			}
		}
		
		// If did not find batch in queued batches check the completed batches
		if(temp == null)
		{
			itr = finishedBatches.iterator();
			
			while(itr.hasNext())
			{
				temp = itr.next();
				
				if(temp.getBatchId() == batchId)
				{
					break;
				}
			}
		}
		
		batchesQueueLock.release();
		
		// copy of batch not actual batch
		return temp.getActiveItems();
	}
	
	/**
	 * Returns a copy of the batch items queue
	 * @param batchId
	 * @return
	 */
	public BatchItem[] getCompletedItems(int batchId)
	{
		batchesQueueLock.acquireUninterruptibly();

		Iterator<Batch> itr = queuedBatches.iterator();
		Batch temp = null;
		
		while(itr.hasNext())
		{
			temp = itr.next();
			
			if(temp.getBatchId() == batchId)
			{
				break;
			}
		}
		
		// If did not find batch in queued batches check the completed batches
		if(temp == null)
		{
			itr = finishedBatches.iterator();
			
			while(itr.hasNext())
			{
				temp = itr.next();
				
				if(temp.getBatchId() == batchId)
				{
					break;
				}
			}
		}
		
		batchesQueueLock.release();
		
		// copy of batch not actual batch
		return temp.getCompletedItems();
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
			DebugLogger.output("1 setComplete");
			batch.setComplete(simsManager,item,simsManager.getSimRunTime(simId),simsManager.getEndEvent(simId),simsManager.getSimStepCount(simId));	
			
			DebugLogger.output("2 removeSimulationStateListener");
			simsManager.removeSimulationStateListener(item.getSimId(), this);

			DebugLogger.output("3 removeSimulation");
			simsManager.removeSimulation(item.getSimId());
			
			DebugLogger.output("4 batchManagerListenerBatchProgressNotification");
			batchManagerListenerBatchProgressNotification(batch);

		}
		
		completedItems = new ArrayList<BatchItem>();
		
		completedItemsLock.release();
	}
	
	private void schedule()
	{
		batchesQueueLock.acquireUninterruptibly();		
		
		DebugLogger.output("BatchManager Schedule Tick");

		processCompletedItems();
		
		// Is there a free slot
		if(simsManager.getActiveSims() < simsManager.getMaxSims() && (queuedBatches.size() > 0))
		{
			switch(batchScheduleMode)
			{
				case fifo:
					scheduleFifo();
				break;
				case fq:
					// TODO Alternative scheduler
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
		DebugLogger.output("Schedule Fifo");
		
		// Get the first batch - FIFO
		Batch batch = queuedBatches.peek();
		
		if(batch == null)
		{
			// no batches
			return;
		}
		
		// Is this batch finished
		if(batch.getCompleted() == batch.getBatchItems())
		{
			// Add the batch to the completed list
			finishedBatches.add(batch);
			
			// Notify listeners the batch is being removed.
			batchManagerListenerBatchFinishedNotification(batch);
			
			// Remove first batch as its complete.
			queuedBatches.poll();
			
			// exit this tick
			return;
		}
		
		// While there are items to add and the simulations manager can add them
		while( (batch.getRemaining() > 0))
		{
			// dequeue the next item in the batch
			BatchItem item = batch.getNext();
			
			// Schedule it
			if(!scheduleBatchItem(item))
			{
				batch.returnItemToQueue(item);
				break;
			}
		
		}
	
	}
	
	private boolean scheduleBatchItem(BatchItem item)
	{
		DebugLogger.output("Schedule Batch");

		activeItemsLock.acquireUninterruptibly();
			activeItems.add(item);
		activeItemsLock.release();
		
		int simId = simsManager.addSimulation(item.getConfigText(),-1);
		
		// If the simulations manager has added a simulation for us
		if(simId>0)
		{
			DebugLogger.output("Settings ID");
			
			item.setSimId(simId);
			
			simsManager.addSimulationStateListener(simId, this);
			
			simsManager.startSim(simId);	
			
			return true;
		}
		
		return false;
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
				
				completedItemsLock.acquireUninterruptibly();
				
				DebugLogger.output("Recorded Completed Sim " + simId);
				
				BatchItem item = findBatchItemFromSimId(simId);

				completedItems.add(item);
					
				completedItemsLock.release();
				
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
		Iterator<Batch> itr = queuedBatches.iterator();
		
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
	    	listener.batchAdded(batch);
	    }
	}
	
	private void batchManagerListenerBatchFinishedNotification(final Batch batch)
	{
	    for (BatchManagerEventListenerInf listener : batchManagerListeners)
	    {
	    	listener.batchFinished(batch);
	    }
	}
		
	private void batchManagerListenerBatchProgressNotification(Batch batch)
	{
	    for (BatchManagerEventListenerInf listener : batchManagerListeners)
	    {
	    	listener.batchProgress(batch);
	    }
	}
	
	public ArrayList<String> getBatchInfo(int batchId)
	{
		batchesQueueLock.acquireUninterruptibly();

		Iterator<Batch> itr = queuedBatches.iterator();
		Batch temp = null;
		
		while(itr.hasNext())
		{
			temp = itr.next();
			
			if(temp.getBatchId() == batchId)
			{
				break;
			}
		}
	
		// If did not find batch in queued batches check the completed batches
		if(temp == null)
		{
			itr = finishedBatches.iterator();
			
			while(itr.hasNext())
			{
				temp = itr.next();
				
				if(temp.getBatchId() == batchId)
				{
					break;
				}
			}
		}
		
		ArrayList<String> info = null;
		
		if(temp != null)
		{
			info = temp.getBatchInfo();
		}
		else
		{
			info = new ArrayList<String>();
		}
		
		batchesQueueLock.release();

		return info;
	}

}
