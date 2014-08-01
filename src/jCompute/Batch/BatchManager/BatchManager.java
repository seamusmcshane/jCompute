package jCompute.Batch.BatchManager;

import jCompute.Batch.Batch;
import jCompute.Batch.BatchItem;
import jCompute.Batch.Batch.BatchPriority;
import jCompute.Debug.DebugLogger;
import jCompute.Simulation.Listener.SimulationStateListenerInf;
import jCompute.Simulation.SimulationManager.SimulationsManagerInf;
import jCompute.Simulation.SimulationManager.Local.SimulationsManagerEventListenerInf;
import jCompute.Simulation.SimulationManager.Local.SimulationsManager.SimulationManagerEvent;
import jCompute.Simulation.SimulationState.SimState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

public class BatchManager implements SimulationsManagerEventListenerInf,SimulationStateListenerInf
{
	// Lock
	private Semaphore batchManagerLock = new Semaphore(1, false);	

	// Batch id counter
	private int batchId = 0;
	
	// Simulations Manager
	private SimulationsManagerInf simsManager;

	// The queues of batches
	private Queue<Batch> fifoQueue;
	private Queue<Batch> fairQueue;
	private int fairQueueLast = 0;
	
	// Finished Batches
	private ArrayList<Batch> finishedBatches;
	
	// The active Items currently being processed from all batches.
	private ArrayList<BatchItem> activeItems; 
	private Semaphore activeItemsLock = new Semaphore(1, false);	

	// Temporary list for completed items.
	private ArrayList<BatchItem> completedItems; 
	private Semaphore completedItemsLock = new Semaphore(1, false);	

	/* Batch Manager Event Listeners */
	private CopyOnWriteArrayList <BatchManagerEventListenerInf> batchManagerListeners = new CopyOnWriteArrayList <BatchManagerEventListenerInf>();
	
	// Scheduler
	private Timer batchSchedulerTimer;
	
	public BatchManager(SimulationsManagerInf simsManager)
	{
		this.simsManager = simsManager;
		
		fifoQueue = new LinkedBlockingQueue<Batch>();
		
		fairQueue = new LinkedBlockingQueue<Batch>();
		
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
		batchManagerLock.acquireUninterruptibly();
		
		Batch tempBatch;
		boolean added = false;
		
		// Try generating a batch and adding it to the queue.
		try
		{
			tempBatch = new Batch(batchId,BatchPriority.STANDARD,fileName);
			
			fairQueue.add(tempBatch);
			
			added = true;
			batchId++;
			
			batchManagerLock.release();
			
			batchManagerListenerBatchAddedNotification(tempBatch);
			
			batchManagerLock.acquireUninterruptibly();
			
		}
		catch (IOException e)
		{			
			added = false;
		}
		
		batchManagerLock.release();
		
		return added;
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
		// Dont block on the completed items
		if(completedItemsLock.tryAcquire())
		{
			// If there are any completed items
			if(completedItems.size() > 0)
			{
				// Processed them
				Iterator<BatchItem> itr = completedItems.iterator();
				
				while(itr.hasNext())
				{
					BatchItem item = itr.next();
				
					Batch batch = findBatch(item.getBatchId());
					
					activeItems.remove(item);
					DebugLogger.output("Processed Completed Item : " + item.getItemId() + " Batch : " + item.getBatchId() + " SimId : " + item.getSimId());

					int simId = item.getSimId();
					
					// Updates Logs/Exports Stats
					batch.setComplete(simsManager,item,simsManager.getSimRunTime(simId),simsManager.getEndEvent(simId),simsManager.getSimStepCount(simId));	
					
					simsManager.removeSimulationStateListener(item.getSimId(), this);

					simsManager.removeSimulation(item.getSimId());
					
					batchManagerListenerBatchProgressNotification(batch);

				}
				
				DebugLogger.output("Processed " + completedItems.size() + " Completed Items");
				
				completedItems = new ArrayList<BatchItem>();	
			}

			completedItemsLock.release();
		}
		
	}
	
	private void schedule()
	{		
		DebugLogger.output("BatchManager Schedule Tick");

		processCompletedItems();
		
		// Is there a free slot
		if(simsManager.getActiveSims() < simsManager.getMaxSims())
		{
			batchManagerLock.acquireUninterruptibly();

			scheduleFifo();
			scheduleFair();
			
			batchManagerLock.release();

		}
		
	}
	
	// High Priority FIFO
	private void scheduleFifo()
	{
		DebugLogger.output("Schedule Fifo");
		
		// Get the first batch - FIFO
		Batch batch = fifoQueue.peek();
		
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

			// Remove first batch as its complete.
			fifoQueue.poll();
			
			batchManagerLock.release();

			// Notify listeners the batch is removed.
			batchManagerListenerBatchFinishedNotification(batch);
			
			batchManagerLock.acquireUninterruptibly();
			
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
	
	private void scheduleFair()
	{
		DebugLogger.output("Schedule Fair");

		int size = fairQueue.size();
		double maxActive = simsManager.getMaxSims()-fifoQueue.size();
		int fairTotal, pos;
		
		if(size > 0)
		{
			pos = fairQueueLast % size;
			fairTotal = (int) Math.ceil(maxActive/size);
			
			DebugLogger.output("Size " + size + " maxActive " + maxActive + " fairTotal " + fairTotal);

		}
		else
		{
			// Queue Empty
			return;
		}
		
		Batch[] fairBatches = fairQueue.toArray(new Batch[fairQueue.size()]);
		
		DebugLogger.output("GOT ARRAY");
		
		Batch batch = null;
		BatchItem item = null;
		
		boolean canContinue = true;
		
		// Cycle over the batches and add one item from each to the run queue		
		do
		{
			batch = fairBatches[pos];
			
			// Is this batch finished
			if(batch.getCompleted() == batch.getBatchItems())
			{
				// Add the batch to the completed list
				finishedBatches.add(batch);

				// Remove batch as its complete.
				fairQueue.remove(batch);
				
				batchManagerLock.release();

				// Notify listeners the batch is removed.
				batchManagerListenerBatchFinishedNotification(batch);
				
				batchManagerLock.acquireUninterruptibly();
				// exit this tick
				canContinue = false;
			}
			else
			{
				if(batch.getRemaining() > 0)
				{
					DebugLogger.output("batch.getActiveItemsCount() " + batch.getActiveItemsCount() + " fairTotal" + fairTotal );
					
					if(batch.getActiveItemsCount() < fairTotal)
					{
						item = batch.getNext();
					
						// Once we cannot add anymore, exit, and return the failed one to the queue		
						if(!scheduleBatchItem(item))
						{
							batch.returnItemToQueue(item);
							canContinue = false;
						}
						else
						{
							pos = (pos + 1) % size;
						}
					}
					else
					{
						pos = (pos + 1) % size;
						
						// Avoid infinite loop due having reached the fair total
						if(batch.getActiveItemsCount() == fairTotal)
						{
							canContinue = false;
						}
					}
				}
				else
				{
					canContinue = false;
				}

			}

		}
		while(canContinue);
		
		fairQueueLast = pos;
		
	}
	
	private boolean scheduleBatchItem(BatchItem item)
	{
		DebugLogger.output("Schedule Batch");

		activeItems.add(item);
		
		batchManagerLock.release();
		
		int simId = simsManager.addSimulation(item.getConfigText(),-1);
		
		batchManagerLock.acquireUninterruptibly();
		
		// If the simulations manager has added a simulation for us
		if(simId>0)
		{
			DebugLogger.output("Setting ID");
			
			item.setSimId(simId);
			
			batchManagerLock.release();
			
			simsManager.addSimulationStateListener(simId, this);
			
			simsManager.startSim(simId);
			
			batchManagerLock.acquireUninterruptibly();
			
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
				
				DebugLogger.output("Recorded Completed Sim " + simId);
				
				BatchItem item = findBatchItemFromSimId(simId);
				
				completedItemsLock.acquireUninterruptibly();
				
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
		
	}
	
	private Batch findBatch(int batchId)
	{
		Iterator<Batch> itr = fifoQueue.iterator();
		
		Batch batch = null;
		Batch tBatch = null;
		
		
		while(itr.hasNext())
		{
			tBatch = itr.next();
			
			if(tBatch.getBatchId() == batchId)
			{
				
				batch = tBatch;
				
				break;
			}

		}
		
		// Search fair queue
		if(batch == null)
		{
			itr = fairQueue.iterator();
			
			batch = null;
			tBatch = null;
			
			while(itr.hasNext())
			{
				tBatch = itr.next();
				
				if(tBatch.getBatchId() == batchId)
				{
					
					batch = tBatch;
					
					break;
				}
	
			}	
		}
		
		// Search finished batches
		if(batch == null)
		{
			itr = finishedBatches.iterator();

			while(itr.hasNext())
			{
				tBatch = itr.next();
				
				if(tBatch.getBatchId() == batchId)
				{
					
					batch = tBatch;
					
					break;
				}

			}
		}		

		return batch;
	}
	
	private BatchItem findBatchItemFromSimId(int simId)
	{
		Iterator<BatchItem> itr = activeItems.iterator();
		
		BatchItem item = null;
		BatchItem tItem = null;
		
		while(itr.hasNext())
		{
			tItem = itr.next();
			
			if(tItem.getSimId() == simId)
			{
				item = tItem;
				break;
			}

		}
		
		return item;
	}
	
	public void addBatchManagerListener(BatchManagerEventListenerInf listener)
	{
		batchManagerListeners.add(listener);
	}
	
	public void removeBatchManagerListener(BatchManagerEventListenerInf listener)
	{
		batchManagerListeners.remove(listener);
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
	
	public String[] getBatchInfo(int batchId)
	{
		batchManagerLock.acquireUninterruptibly();
		
		Batch batch = findBatch(batchId);
		
		String[] info = batch.getBatchInfo();

		batchManagerLock.release();

		return info;
	}

	private BatchItem[] getListItems(int batchId , int queueNum)
	{
		BatchItem[] list = null;
		
		Batch batch = findBatch(batchId);		
		
		switch(queueNum)
		{
			case 0:
				list = batch.getQueuedItems();
			break;
			case 1:
				list = batch.getActiveItems();
			break;
			case 2:
				list = batch.getCompletedItems();
			break;
		}
		
		return list;		
	}

	public BatchItem[] getItemQueue(int batchId)
	{
		return getListItems(batchId,0);
	}
	
	public BatchItem[] getActiveItems(int batchId)
	{
		return getListItems(batchId,1);
	}
	
	public BatchItem[] getCompletedItems(int batchId)
	{
		return getListItems(batchId,2);
	}

}
