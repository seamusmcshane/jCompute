package jCompute.Batch.BatchManager;

import jCompute.JComputeEventBus;
import jCompute.Batch.Batch;
import jCompute.Batch.BatchItem;
import jCompute.Batch.Batch.BatchPriority;
import jCompute.Batch.BatchManager.Event.BatchAddedEvent;
import jCompute.Batch.BatchManager.Event.BatchFinishedEvent;
import jCompute.Batch.BatchManager.Event.BatchPositionEvent;
import jCompute.Batch.BatchManager.Event.BatchProgressEvent;
import jCompute.Cluster.Controller.ControlNode;
import jCompute.Datastruct.List.ManagedBypassableQueue;
import jCompute.Datastruct.List.Interface.StoredQueuePosition;
import jCompute.Simulation.Event.SimulationStateChangedEvent;
import jCompute.Simulation.SimulationState.SimState;
import jCompute.Stats.StatExporter;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

public class BatchManager
{
	// SL4J Logger
	private static Logger log = LoggerFactory.getLogger(BatchManager.class);
	
	// Lock
	private Semaphore batchManagerLock = new Semaphore(1, false);
	
	// Batch id counter
	private int batchId = 0;
	
	// ControlNode
	private ControlNode controlNode;
	
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
	
	private ArrayList<CompletedItemsNode> completeItems;
	
	private ArrayList<Integer> recoveredSimIds;
	
	// Scheduler
	private Timer batchScheduler;
	
	// Completed Items Processor
	private Timer batchCompletedItemsProcessor;
	
	public BatchManager()
	{
		this.controlNode = new ControlNode();
		
		fifoQueue = new ManagedBypassableQueue();
		
		fairQueue = new ManagedBypassableQueue();
		
		finishedBatches = new ArrayList<Batch>(16);
		
		stoppedBatches = new LinkedList<Batch>();
		
		activeItems = new ArrayList<BatchItem>(16);
		
		completeItems = new ArrayList<CompletedItemsNode>();
		
		recoveredSimIds = new ArrayList<Integer>();
		
		// Register on the event bus
		JComputeEventBus.register(this);
		
		// Batch Scheduler Tick
		batchScheduler = new Timer("Batch Scheduler");
		batchScheduler.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				schedule();
			}
			
		}, 0, 100);
		
		// Completed Items Processor Tick
		batchCompletedItemsProcessor = new Timer("Batch Completed Items Processor");
		batchCompletedItemsProcessor.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				processCompletedItems();
				
				recoverItemsFromInactiveNodes();
			}
			
		}, 0, 10000);
		
	}
	
	public boolean addBatch(String filePath)
	{
		Batch tempBatch = null;
		boolean added = false;
		
		batchManagerLock.acquireUninterruptibly();
		
		// Try generating a batch and adding it to the queue. - Default to High
		// priority
		tempBatch = new Batch(batchId, BatchPriority.HIGH);
		
		if(tempBatch.loadConfig(filePath))
		{
			fifoQueue.add(tempBatch);
			
			added = true;
			batchId++;
			
			batchManagerLock.release();
			
			JComputeEventBus.post(new BatchAddedEvent(tempBatch));
			
			log.info("Added Batch : " + batchId);;
		}
		else
		{
			batchManagerLock.release();
			
			added = false;
		}
		
		return added;
	}
	
	private void recoverItemsFromInactiveNodes()
	{
		batchManagerLock.acquireUninterruptibly();
		
		Iterator<Integer> itr;
		ArrayList<Integer> processedIds = new ArrayList<Integer>();
		
		if(controlNode.hasRecoverableSimIds())
		{
			ArrayList<Integer> tIds = controlNode.getRecoverableSimIds();
			itr = tIds.iterator();
			
			while(itr.hasNext())
			{
				recoveredSimIds.add(itr.next());
			}
		}
		
		itr = recoveredSimIds.iterator();
		while(itr.hasNext())
		{
			int simId = itr.next();
			
			BatchItem item = findActiveBatchItemFromSimId(simId);
			
			// System.out.println("SimId " + simId);
			// System.out.println("itemActive " + itemActive);
			// System.out.println("Item " + item);
			
			Batch batch = findBatch(item.getBatchId());
			
			itemsLock.acquireUninterruptibly();
			
			activeItems.remove(item);
			
			itemsLock.release();
			
			log.info("Recovered Item (" + item.getItemId() + "/" + item.getSampleId() + ") from Batch "
					+ item.getBatchId() + " for SimId " + simId);
			
			batch.returnItemToQueue(item);
			
			processedIds.add(simId);
			
		}
		
		for(Integer i : processedIds)
		{
			recoveredSimIds.remove(i);
		}
		
		batchManagerLock.release();
		
	}
	
	private void processCompletedItems()
	{
		itemsLock.acquireUninterruptibly();
		
		Iterator<CompletedItemsNode> itr = completeItems.iterator();
		
		while(itr.hasNext())
		{
			CompletedItemsNode node = itr.next();
			
			Batch batch = null;
			BatchItem item = node.getItem();
			StatExporter exporter = node.getExporter();
			
			batchManagerLock.acquireUninterruptibly();
			
			log.debug("Item : " + item.getItemId());
			batch = findBatch(item.getBatchId());
			
			batchManagerLock.release();
			
			if(batch != null)
			{
				batch.setItemComplete(item, exporter);
				
				log.info("Batch Item Finished : " + batch.getBatchId());
				
				// Batch Progress
				JComputeEventBus.post(new BatchProgressEvent(batch));
				
			}
			else
			{
				log.warn("Simulation Event for NULL batch " + item.getBatchId());
				
				controlNode.removeSimulation(item.getSimId());
			}
			
		}
		
		completeItems = new ArrayList<CompletedItemsNode>();
		
		itemsLock.release();
	}
	
	private void schedule()
	{
		// log.info("Scheduler Tick");
		
		itemsLock.acquireUninterruptibly();
		int size = activeItems.size();
		itemsLock.release();
		
		if(size == 0)
		{
			processCompletedItems();
		}
		
		batchManagerLock.acquireUninterruptibly();
		
		// Schedule from the fifo only if it contains batches
		if(!scheduleFifo())
		{
			batchManagerLock.release();
			
			// Safe to release and recapture here
			batchManagerLock.acquireUninterruptibly();
			scheduleFair();
		}
		
		batchManagerLock.release();
		
	}
	
	// High Priority FIFO
	/**
	 * Schedule batchs from the fifo queue. Returns true if the fifo is active.
	 */
	private boolean scheduleFifo()
	{
		
		// Get the first batch - FIFO
		Batch batch = (Batch) fifoQueue.peek();
		
		if(batch == null)
		{
			// no batches
			return false;
		}
		
		// Is this batch finished
		if(batch.isFinished())
		{
			// Add the batch to the completed list
			finishedBatches.add(batch);
			
			// Remove first batch as its complete.
			fifoQueue.poll();
			
			batchManagerLock.release();
			
			// Notify listeners the batch is removed.
			JComputeEventBus.post(new BatchFinishedEvent(batch));
			
			positionsChangedInQueue(fifoQueue);
			
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
		
		// Is there a free slot
		if(controlNode.hasFreeSlot())
		{
			
			if(batch.itemsNeedGenerated())
			{
				batch.generateItems();
			}
			else
			{
				// While there are items to add and the control node can add
				// them
				while((batch.getRemaining() > 0) && controlNode.hasFreeSlot())
				{
					// dequeue the next item in the batch
					BatchItem item = batch.getNext();
					
					// Schedule it
					if(!scheduleBatchItem(batch, item))
					{
						batch.returnItemToQueue(item);
						break;
					}
					else
					{
						JComputeEventBus.post(new BatchProgressEvent(batch));
					}
				}
			}
			
		}
		
		return true;
	}
	
	private void scheduleFair()
	{
		// log.debug("Schedule Fair");
		
		int size = fairQueue.size();
		double maxActive = controlNode.getMaxSims() - fifoQueue.size();
		int fairTotal, pos;
		
		if(size > 0)
		{
			pos = fairQueueLast % size;
			fairTotal = (int) Math.ceil(maxActive / size);
			
			log.debug("Size " + size + " maxActive " + maxActive + " fairTotal " + fairTotal);
			
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
			// Is there a free slot
			if(controlNode.hasFreeSlot())
			{
				batch = (Batch) fairQueue.get(pos);
				
				if(batch == null)
				{
					// no batches
					return;
				}
				
				if(batch.itemsNeedGenerated())
				{
					batch.generateItems();
				}
				
				// Is this batch finished
				if(batch.isFinished())
				{
					// Add the batch to the completed list
					finishedBatches.add(batch);
					
					// Remove batch as its complete.
					fairQueue.remove(batch);
					
					batchManagerLock.release();
					
					// Notify listeners the batch is removed.
					JComputeEventBus.post(new BatchFinishedEvent(batch));
					
					positionsChangedInQueue(fairQueue);
					
					batchManagerLock.acquireUninterruptibly();
					// exit this tick
					canContinue = false;
				}
				else
				{
					if(batch.getRemaining() > 0)
					{
						log.debug("Batch " + batch.getBatchId() + " ActiveItemsCount " + batch.getActiveItemsCount()
								+ " fairTotal" + fairTotal);
						
						if(batch.getActiveItemsCount() < fairTotal)
						{
							item = batch.getNext();
							
							// Once we cannot add anymore, exit, and return the
							// failed one to the queue
							if(!scheduleBatchItem(batch, item))
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
							
							// Avoid infinite loop due having reached the fair
							// total
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
			else
			{
				canContinue = false;
			}
			
		}
		while(canContinue);
		
		fairQueueLast = pos;
		
	}
	
	private boolean scheduleBatchItem(Batch batch, BatchItem nextItem)
	{
		log.debug("Schedule BatchItem");
		
		String itemConfig = null;
		
		try
		{
			itemConfig = new String(batch.getItemConfig(nextItem.getItemHash()), "ISO-8859-1");
		}
		catch(UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		
		batchManagerLock.release();
		
		int simId = controlNode.addSimulation(itemConfig, batch.getStatExportFormat(), nextItem.getItemHash());
		
		batchManagerLock.acquireUninterruptibly();
		
		// If the control node has added a simulation for us
		if(simId > 0)
		{
			log.debug("Item : " + nextItem.getItemId() + " setting simId " + simId);
			
			nextItem.setSimId(simId);
			
			batchManagerLock.release();
			
			itemsLock.acquireUninterruptibly();
			activeItems.add(nextItem);
			itemsLock.release();
			
			controlNode.startSim(simId);
			
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
		
		log.debug("SimulationStateChangedEvent " + e.getSimId());
		
		switch(state)
		{
			case RUNNING:
			break;
			case NEW:
			break;
			case FINISHED:
				
				BatchItem item = findActiveBatchItemFromSimId(simId);
				
				itemsLock.acquireUninterruptibly();
				
				if(item != null)
				{
					item.setComputeTime(e.getRunTime(), e.getEndEvent(), e.getStepCount());
					
					activeItems.remove(item);
					completeItems.add(new CompletedItemsNode(item, e.getStatExporter()));
					
					// Tell the batch this item is no longer active
					Batch batch = findBatch(item.getBatchId());
					if(batch != null)
					{
						batch.setItemNotActive(item);
					}
					
				}
				
				itemsLock.release();
			
			break;
			case PAUSED:
			break;
			default:
				
				// Ensures this is spotted
				for(int i = 0; i < 100; i++)
				{
					log.error("Invalid/Unhandled SimState passed to Batch Manager for Simulation : " + e.getSimId());
				}
			
			break;
		}
		
	}
	
	private Batch findBatch(int batchId)
	{
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
	
	public String[] getBatchInfo(int batchId)
	{
		batchManagerLock.acquireUninterruptibly();
		
		Batch batch = findBatch(batchId);
		
		String[] info = batch.getBatchInfo();
		
		batchManagerLock.release();
		
		return info;
	}
	
	public void setStatus(int batchId, boolean status)
	{
		batchManagerLock.acquireUninterruptibly();
		
		Batch batch = findBatch(batchId);
		
		batch.setStatus(status);
		
		// Remove batch from queues if disabled and add to stop else the reverse
		// process for enabling
		if(status == false)
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
		
		JComputeEventBus.post(new BatchProgressEvent(batch));
	}
	
	public void setPriority(int batchId, BatchPriority priority)
	{
		batchManagerLock.acquireUninterruptibly();
		
		Batch batch = findBatch(batchId);
		
		if(batch == null)
		{
			batchManagerLock.release();
			return;
		}
		
		// Do not set the priority if its the same
		if(batch.getPriority() != priority)
		{
			// if the batch is enabled switch the queue (as its not in one if
			// not
			// enabled)
			if(batch.getStatus() == true)
			{
				if(batch.getPriority() == BatchPriority.STANDARD)
				{
					// Move to High (Standard to FIFO)
					fairQueue.remove(batch);
					fifoQueue.add(batch);
				}
				else
				{
					// Move To Standard (FIFO to Standard)
					fifoQueue.remove(batch);
					fairQueue.add(batch);
				}
				
			}
			
			// New Priority
			batch.setPriority(priority);
			
			batchManagerLock.release();
			
			positionsChangedInbothQueues();
			
		}
		else
		{
			batchManagerLock.release();
		}
		
	}
	
	/**
	 * This method does the position changed notifications, for when items move
	 * between queues. Positions in both will have changed.
	 */
	private void positionsChangedInbothQueues()
	{
		positionsChangedInQueue(fifoQueue);
		positionsChangedInQueue(fairQueue);
	}
	
	private void positionsChangedInQueue(ManagedBypassableQueue queue)
	{
		Batch tBatch = null;
		Iterator<StoredQueuePosition> itr = queue.iterator();
		
		while(itr.hasNext())
		{
			tBatch = (Batch) itr.next();
			
			log.debug("Batch " + tBatch.getBatchId() + " Pos" + tBatch.getPosition());
			
			JComputeEventBus.post(new BatchPositionEvent(tBatch));
		}
	}
	
	public void moveToFront(int batchId)
	{
		batchManagerLock.acquireUninterruptibly();
		
		Batch batch = findBatch(batchId);
		
		log.debug("Move " + batchId + " to front");
		log.debug("batch was Batch " + batch.getBatchId() + " Pos" + batch.getPosition());
		
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
		
		log.debug("queue " + queue.size());
		
		// Batch Orders Changed refresh all data
		while(itr.hasNext())
		{
			tBatch = (Batch) itr.next();
			
			log.debug("Batch " + tBatch.getBatchId() + " Pos" + tBatch.getPosition());
			
			JComputeEventBus.post(new BatchPositionEvent(tBatch));
		}
		
		batchManagerLock.release();
		
	}
	
	public void moveToEnd(int batchId)
	{
		batchManagerLock.acquireUninterruptibly();
		
		Batch batch = findBatch(batchId);
		
		log.debug("Move " + batchId + " to end");
		log.debug("batch was Batch " + batch.getBatchId() + " Pos" + batch.getPosition());
		
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
		
		log.debug("queue " + queue.size());
		
		// Batch Orders Changed refresh all data
		while(itr.hasNext())
		{
			tBatch = (Batch) itr.next();
			
			log.debug("Batch " + tBatch.getBatchId() + " Pos" + tBatch.getPosition());
			
			JComputeEventBus.post(new BatchPositionEvent(tBatch));
		}
		
		batchManagerLock.release();
		
	}
	
	public void moveForward(int batchId)
	{
		batchManagerLock.acquireUninterruptibly();
		
		Batch batch = findBatch(batchId);
		
		log.debug("Move " + batchId + " to front");
		log.debug("batch was Batch " + batch.getBatchId() + " Pos" + batch.getPosition());
		
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
		
		log.debug("queue " + queue.size());
		
		// Batch Orders Changed refresh all data
		while(itr.hasNext())
		{
			tBatch = (Batch) itr.next();
			
			log.debug("Batch " + tBatch.getBatchId() + " Pos" + tBatch.getPosition());
			
			JComputeEventBus.post(new BatchPositionEvent(tBatch));
		}
		
		batchManagerLock.release();
	}
	
	public void moveBackward(int batchId)
	{
		batchManagerLock.acquireUninterruptibly();
		
		Batch batch = findBatch(batchId);
		
		log.debug("Move " + batchId + " to front");
		log.debug("batch was Batch " + batch.getBatchId() + " Pos" + batch.getPosition());
		
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
		
		log.debug("queue " + queue.size());
		
		// Batch Orders Changed refresh all data
		while(itr.hasNext())
		{
			tBatch = (Batch) itr.next();
			
			log.debug("Batch " + tBatch.getBatchId() + " Pos" + tBatch.getPosition());
			
			JComputeEventBus.post(new BatchPositionEvent(tBatch));
		}
		
		batchManagerLock.release();
	}
	
	public void removeBatch(int batchId)
	{
		batchManagerLock.acquireUninterruptibly();
		
		Batch batch = findBatch(batchId);
		
		boolean status = batch.getStatus();
		
		// Remove batch from one of the queues
		if(status)
		{
			// In Fifo
			if(batch.getPriority() == BatchPriority.HIGH)
			{
				fifoQueue.remove(batch);
				
				positionsChangedInQueue(fifoQueue);
			}
			else
			{
				// In Fair
				fairQueue.remove(batch);
				
				positionsChangedInQueue(fairQueue);
			}
			
		}
		else
		{
			// The batch was stoped
			stoppedBatches.remove(batch);
		}
		
		batchManagerLock.release();
		
	}
	
}
