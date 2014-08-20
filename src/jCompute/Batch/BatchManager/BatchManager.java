package jCompute.Batch.BatchManager;

import jCompute.JComputeEventBus;
import jCompute.Batch.Batch;
import jCompute.Batch.BatchItem;
import jCompute.Batch.Batch.BatchPriority;
import jCompute.Datastruct.List.ManagedBypassableQueue;
import jCompute.Datastruct.List.Interface.StoredQueuePosition;
import jCompute.Debug.DebugLogger;
import jCompute.Simulation.Event.SimulationStateChangedEvent;
import jCompute.Simulation.SimulationManager.SimulationsManagerInf;
import jCompute.Simulation.SimulationState.SimState;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;

import com.google.common.eventbus.Subscribe;

public class BatchManager
{
	// Lock
	private Semaphore batchManagerLock = new Semaphore(1, false);	

	// Batch id counter
	private int batchId = 0;
	
	// Simulations Manager
	private SimulationsManagerInf simsManager;

	// The queues of batches
	private ManagedBypassableQueue fifoQueue;
	private ManagedBypassableQueue fairQueue;
	private LinkedList<Batch> stoppedBatches;
	
	private int fairQueueLast = 0;
	
	// Finished Batches
	private ArrayList<Batch> finishedBatches;
	
	// The active Items currently being processed from all batches.
	private ArrayList<BatchItem> activeItems; 
	private Semaphore itemsLock = new Semaphore(1, false);
	
	private ArrayList<BatchItem> completeItems; 

	/* Batch Manager Event Listeners */
	private CopyOnWriteArrayList <BatchManagerEventListenerInf> batchManagerListeners = new CopyOnWriteArrayList <BatchManagerEventListenerInf>();
	
	// Scheduler
	private Timer batchSchedulerTimer;
	
	public BatchManager(SimulationsManagerInf simsManager)
	{		
		this.simsManager = simsManager;
		
		fifoQueue = new ManagedBypassableQueue();
		
		fairQueue = new ManagedBypassableQueue();
		
		finishedBatches = new ArrayList<Batch>(16);
		
		stoppedBatches = new LinkedList<Batch>();
		
		activeItems = new ArrayList<BatchItem>(simsManager.getActiveSims());
		
		completeItems = new ArrayList<BatchItem>();
		
		// Register on the event bus
		JComputeEventBus.register(this);
		
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
	
	public boolean addBatch(String filePath)
	{		
		Batch tempBatch = null;
		boolean added = false;

		batchManagerLock.acquireUninterruptibly();

		// Try generating a batch and adding it to the queue.
		tempBatch = new Batch(batchId,BatchPriority.STANDARD);
			
		if(tempBatch.loadConfig(filePath))
		{
			fairQueue.add(tempBatch);
			
			added = true;
			batchId++;
			
			batchManagerLock.release();

			batchManagerListenerBatchAddedNotification(tempBatch);
		}
		else
		{
			batchManagerLock.release();

			added = false;
		}

		return added;
	}
	
	private void schedule()
	{
		DebugLogger.output("BatchManager Schedule Tick");
		
		//simsManager.removeSimulationStateListener(item.getSimId(), this);
		//simsManager.removeSimulation(item.getSimId());
		
		itemsLock.acquireUninterruptibly();
		
		Iterator<BatchItem> itr = completeItems.iterator();
		
		BatchItem item = null;
		int i=0;
		
		while(itr.hasNext())
		{
			item = itr.next();
			
			DebugLogger.output("Going to remove sim " + item.getSimId());
			
			//simsManager.removeSimulationStateListener(item.getSimId(), batchManager);
			simsManager.removeSimulation(item.getSimId());
			
			DebugLogger.output(i + " Processed Completed Item : " + item.getItemId() + " Batch : " + item.getBatchId() + " SimId : " + item.getSimId());
			i++;
		}
		
		completeItems = new ArrayList<BatchItem>();
		
		itemsLock.release();
		
		// Is there a free slot
		if(simsManager.getActiveSims() < simsManager.getMaxSims())
		{
			batchManagerLock.acquireUninterruptibly();

			// Schedule from the fifo only if it contains batches
			if(!scheduleFifo())
			{
				scheduleFair();
			}
			
			batchManagerLock.release();

		}
		
	}
	
	// High Priority FIFO
	/**
	 * Schedule batchs from the fifo queue.
	 * Returns true if the fifo is active.
	 */
	private boolean scheduleFifo()
	{
		DebugLogger.output("Schedule Fifo");
		
		// Get the first batch - FIFO
		Batch batch = (Batch) fifoQueue.peek();
		
		if(batch == null)
		{
			// no batches
			return false;
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
			if(fifoQueue.size() > 0)
			{
				// There is more batches
				return true;
			}
			else
			{
				// There are no more batches
				return false;
			}
			
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
		
		return true;
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
		
		Batch batch = null;
		BatchItem item = null;
		
		boolean canContinue = true;
		
		// Cycle over the batches and add one item from each to the run queue		
		do
		{
			batch = (Batch) fairQueue.get(pos);
			
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
		
		batchManagerLock.release();
		
		int simId = simsManager.addSimulation(item.getConfigText(),-1);
		
		batchManagerLock.acquireUninterruptibly();
		
		// If the simulations manager has added a simulation for us
		if(simId>0)
		{
			DebugLogger.output("Setting ID");
			
			item.setSimId(simId);
			
			batchManagerLock.release();
			
			//simsManager.addSimulationStateListener(simId, batchManager);
			
			itemsLock.acquireUninterruptibly();
				activeItems.add(item);
			itemsLock.release();
			
			simsManager.startSim(simId);
			
			batchManagerLock.acquireUninterruptibly();
			
			return true;
		}
		
		return false;
	}
	
	@Subscribe
	public void SimulationStateChangedEvent(SimulationStateChangedEvent e)
	{
		SimState state = e.getState();
		int simId = e.getSimId();
		
		switch(state)
		{
			case RUNNING:
			break;
			case NEW:
			break;
			case FINISHED:
				
				DebugLogger.output("Recorded Completed Sim " + simId);
				
				BatchItem item = findActiveBatchItemFromSimId(simId);			

				DebugLogger.output("GOT Active item " + item.getItemId());

				Batch batch = findBatch(item.getBatchId());				

				DebugLogger.output("GOT Batch " + batch.getBatchId());

				// Updates Logs/Exports Stats
				batch.setComplete(simsManager,item);	
				
				//simsManager.removeSimulationStateListener(item.getSimId(), this);
				//simsManager.removeSimulation(item.getSimId());
				
				itemsLock.acquireUninterruptibly();
				
				DebugLogger.output(">>>> [BM] simulationStateChanged ("+simId+") - item("+item.getItemId()+")");
				
				activeItems.remove(item);
				completeItems.add(item);
				
				itemsLock.release();
				
				batchManagerListenerBatchProgressNotification(batch);

				
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
		batchManagerLock.acquireUninterruptibly();
		
		Iterator<StoredQueuePosition> qitr = fifoQueue.iterator();
		
		Batch batch = null;
		Batch tBatch = null;
		
		// Search Fifo Queue
		while(qitr.hasNext())
		{
			tBatch = (Batch) qitr.next();
			
			if(tBatch.getBatchId() == batchId)
			{
				
				batch = tBatch;
				
				break;
			}

		}
		
		// Search fair queue
		if(batch == null)
		{
			qitr = fairQueue.iterator();
			
			batch = null;
			tBatch = null;
			
			while(qitr.hasNext())
			{
				tBatch = (Batch) qitr.next();
				
				if(tBatch.getBatchId() == batchId)
				{
					
					batch = tBatch;
					
					break;
				}
	
			}	
		}
		
		Iterator<Batch> litr;

		
		// Search finished batches
		if(batch == null)
		{
			litr = finishedBatches.iterator();

			while(litr.hasNext())
			{
				tBatch = litr.next();
				
				if(tBatch.getBatchId() == batchId)
				{
					
					batch = tBatch;
					
					break;
				}

			}
		}
		
		// Still Null? search stopped list
		if(batch == null)
		{
			litr = stoppedBatches.iterator();
			
			batch = null;
			tBatch = null;
			
			while(litr.hasNext())
			{
				tBatch = litr.next();
				
				if(tBatch.getBatchId() == batchId)
				{
					
					batch = tBatch;
					
					break;
				}
	
			}	
		}

		batchManagerLock.release();
		
		return batch;
	}

	private BatchItem findActiveBatchItemFromSimId(int simId)
	{
		itemsLock.acquireUninterruptibly();

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
		
		itemsLock.release();

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
		Batch batch = findBatch(batchId);
		
		String[] info = batch.getBatchInfo();
		
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

	public void setEnabled(int batchId, boolean enabled)
	{
		Batch batch = findBatch(batchId);
		
		batch.setEnabled(enabled);
		
		batchManagerLock.acquireUninterruptibly();		
		
		// Remove batch from queues if disabled and add to stop else the reverse process for enabling
		if(enabled == false)
		{
			// In Fifo
			if(batch.getPriority() == BatchPriority.HIGH)
			{
				fifoQueue.remove(batch);
			}
			else
			{
				// In Fair
				fairQueue.remove(batch);
			}
			
			stoppedBatches.add(batch);
		}
		else
		{
			// In Fifo
			if(batch.getPriority() == BatchPriority.HIGH)
			{
				fifoQueue.add(batch);
			}
			else
			{
				// In Fair
				fairQueue.add(batch);
			}
			
			stoppedBatches.remove(batch);

		}
		
		batchManagerLock.release();
		
		batchManagerListenerBatchProgressNotification(batch);
	}
	
	public void setPriority(int batchId, BatchPriority priority)
	{
		Batch batch = findBatch(batchId);	
		
		batchManagerLock.acquireUninterruptibly();

		Iterator<StoredQueuePosition> itr;
		ManagedBypassableQueue queue = null;
		
		// if the batch is enabled switch the queue (as its not in one if not enabled)
		if(batch.getEnabled() == true)
		{
			if(batch.getPriority() == BatchPriority.STANDARD)
			{
				// Move to High (Standard to FIFO)
				fairQueue.remove(batch);
				fifoQueue.add(batch);
				
				queue = fifoQueue;

			}
			else
			{
				// Move To Standard (FIFO to Standard)
				fifoQueue.remove(batch);			
				fairQueue.add(batch);
				
				queue = fairQueue;

			}

		}
		
		// New Priority
		batch.setPriority(priority);	
			
		batchManagerLock.release();
		
		Batch tBatch = null;
		itr = queue.iterator();
		
		// Batch Orders Changed refresh all data 
		while(itr.hasNext())
		{
			tBatch = (Batch) itr.next();
			
			DebugLogger.output("Batch " + tBatch.getBatchId() + " Pos" + tBatch.getPosition());
			
			batchManagerListenerBatchQueueQueuePositionChanged(tBatch);
		}
	}

	public void moveToFront(int batchId)
	{
		Batch batch = findBatch(batchId);	

		batchManagerLock.acquireUninterruptibly();

		DebugLogger.output("Move " + batchId + " to front");
		DebugLogger.output("batch was Batch " + batch.getBatchId() + " Pos" + batch.getPosition());
		
		Iterator<StoredQueuePosition> itr;
		ManagedBypassableQueue queue = null;
		
		if(batch.getPriority() == BatchPriority.STANDARD)
		{
			fairQueue.moveToFront(batch);
			
			queue = fairQueue;
		}
		else
		{
			fifoQueue.moveToFront(batch);
			
			queue = fifoQueue;
		}
		
		
		// for the temp reference
		Batch tBatch = null;
		itr = queue.iterator();
		
		DebugLogger.output("queue " + queue.size());
		
		// Batch Orders Changed refresh all data 
		while(itr.hasNext())
		{
			tBatch = (Batch) itr.next();
			
			DebugLogger.output("Batch " + tBatch.getBatchId() + " Pos" + tBatch.getPosition());
			
			batchManagerListenerBatchQueueQueuePositionChanged(tBatch);
		}
		
		batchManagerLock.release();

	}
	
	public void moveToEnd(int batchId)
	{

		Batch batch = findBatch(batchId);	

		batchManagerLock.acquireUninterruptibly();

		DebugLogger.output("Move " + batchId + " to end");
		DebugLogger.output("batch was Batch " + batch.getBatchId() + " Pos" + batch.getPosition());
		
		Iterator<StoredQueuePosition> itr;
		ManagedBypassableQueue queue = null;
		
		if(batch.getPriority() == BatchPriority.STANDARD)
		{
			fairQueue.moveToEnd(batch);
			
			queue = fairQueue;
		}
		else
		{
			fifoQueue.moveToEnd(batch);
			
			queue = fifoQueue;
		}
		
		// for the temp reference
		Batch tBatch = null;
		itr = queue.iterator();
		
		DebugLogger.output("queue " + queue.size());
		
		// Batch Orders Changed refresh all data 
		while(itr.hasNext())
		{
			tBatch = (Batch) itr.next();
			
			DebugLogger.output("Batch " + tBatch.getBatchId() + " Pos" + tBatch.getPosition());
			
			batchManagerListenerBatchQueueQueuePositionChanged(tBatch);
		}
		
		batchManagerLock.release();	
		
	}
	
	public void moveForward(int batchId)
	{
		Batch batch = findBatch(batchId);	

		batchManagerLock.acquireUninterruptibly();

		DebugLogger.output("Move " + batchId + " to front");
		DebugLogger.output("batch was Batch " + batch.getBatchId() + " Pos" + batch.getPosition());
		
		Iterator<StoredQueuePosition> itr;
		ManagedBypassableQueue queue = null;
		
		if(batch.getPriority() == BatchPriority.STANDARD)
		{
			fairQueue.moveForward(batch);
			
			queue = fairQueue;
		}
		else
		{
			fifoQueue.moveForward(batch);
			
			queue = fifoQueue;
		}
		
		
		// for the temp reference
		Batch tBatch = null;
		itr = queue.iterator();
		
		DebugLogger.output("queue " + queue.size());
		
		// Batch Orders Changed refresh all data 
		while(itr.hasNext())
		{
			tBatch = (Batch) itr.next();
			
			DebugLogger.output("Batch " + tBatch.getBatchId() + " Pos" + tBatch.getPosition());
			
			batchManagerListenerBatchQueueQueuePositionChanged(tBatch);
		}
		
		batchManagerLock.release();
	}
	
	public void moveBackward(int batchId)
	{
		Batch batch = findBatch(batchId);	

		batchManagerLock.acquireUninterruptibly();

		DebugLogger.output("Move " + batchId + " to front");
		DebugLogger.output("batch was Batch " + batch.getBatchId() + " Pos" + batch.getPosition());
		
		Iterator<StoredQueuePosition> itr;
		ManagedBypassableQueue queue = null;
		
		if(batch.getPriority() == BatchPriority.STANDARD)
		{
			fairQueue.moveBackward(batch);
			
			queue = fairQueue;
		}
		else
		{
			fifoQueue.moveBackward(batch);
			
			queue = fifoQueue;
		}
		
		
		// for the temp reference
		Batch tBatch = null;
		itr = queue.iterator();
		
		DebugLogger.output("queue " + queue.size());
		
		// Batch Orders Changed refresh all data 
		while(itr.hasNext())
		{
			tBatch = (Batch) itr.next();
			
			DebugLogger.output("Batch " + tBatch.getBatchId() + " Pos" + tBatch.getPosition());
			
			batchManagerListenerBatchQueueQueuePositionChanged(tBatch);
		}
		
		batchManagerLock.release();
	}

	private void batchManagerListenerBatchQueueQueuePositionChanged(Batch batch)
	{
	    for (BatchManagerEventListenerInf listener : batchManagerListeners)
	    {
	    	listener.batchQueuePositionChanged(batch);
	    }
	}

}
