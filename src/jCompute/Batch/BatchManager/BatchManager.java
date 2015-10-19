package jCompute.Batch.BatchManager;

import jCompute.JComputeEventBus;
import jCompute.Batch.Batch;
import jCompute.Batch.BatchItem;
import jCompute.Batch.BatchManager.Event.BatchAddedEvent;
import jCompute.Batch.BatchManager.Event.BatchFinishedEvent;
import jCompute.Batch.BatchManager.Event.BatchPositionEvent;
import jCompute.Batch.BatchManager.Event.BatchProgressEvent;
import jCompute.Cluster.Controller.ControlNode.ControlNode;
import jCompute.Cluster.Controller.Request.ControlNodeItemRequest;
import jCompute.Cluster.Controller.Request.ControlNodeItemRequest.ControlNodeItemRequestOperation;
import jCompute.Cluster.Controller.Request.ControlNodeItemRequest.ControlNodeItemRequestResult;
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
	private LinkedList<Batch> stoppedBatches;
	
	// Finished Batches
	private ArrayList<Batch> finishedBatches;
	
	// The active Items currently being processed from all batches.
	private ArrayList<BatchItem> activeItems;
	private Semaphore itemsLock = new Semaphore(1, false);
	
	private ArrayList<CompletedItemsNode> completeItems;
	
	// Scheduler
	private Timer batchScheduler;
	
	// Completed Items Processor
	private Timer batchCompletedItemsProcessor;
	
	public BatchManager(boolean allowMulti)
	{
		this.controlNode = new ControlNode(allowMulti);
		
		fifoQueue = new ManagedBypassableQueue();
		
		finishedBatches = new ArrayList<Batch>(16);
		stoppedBatches = new LinkedList<Batch>();
		
		activeItems = new ArrayList<BatchItem>(16);
		completeItems = new ArrayList<CompletedItemsNode>();
		
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
			
		}, 0, 10);
		
		// Completed Items Processor Tick
		batchCompletedItemsProcessor = new Timer("Batch Completed Items Processor");
		batchCompletedItemsProcessor.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				// Process Completed Items
				processCompletedItems();
				
				// Process Items from nodes that have been removed.
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
		tempBatch = new Batch(batchId);
		
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
		if(controlNode.hasRecoverableSimIds())
		{
			ArrayList<Integer> recoveredSimIds = controlNode.getRecoverableSimIds();
			
			Iterator<Integer> itr = recoveredSimIds.iterator();
			while(itr.hasNext())
			{
				int simId = itr.next();
				
				BatchItem item = findActiveBatchItemFromSimId(simId);
				
				// System.out.println("SimId " + simId);
				// System.out.println("itemActive " + itemActive);
				// System.out.println("Item " + item);
				
				batchManagerLock.acquireUninterruptibly();
				Batch batch = findBatch(item.getBatchId());
				batchManagerLock.release();
				
				itemsLock.acquireUninterruptibly();
				activeItems.remove(item);
				itemsLock.release();
				
				log.info("Recovered Item (" + item.getItemId() + "/" + item.getSampleId() + ") from Batch " + item.getBatchId() + " for SimId " + simId);
				
				batch.returnItemToQueue(item);
			}
			
		}
		
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
			
			log.debug("Item : " + item.getItemId());
			batchManagerLock.acquireUninterruptibly();
			batch = findBatch(item.getBatchId());
			batchManagerLock.release();
			
			if(batch != null)
			{
				log.info("Batch " + batch.getBatchId() + " Item " + item.getItemId() + " Sample " + item.getSampleId() + " Finished");
				
				batch.setItemComplete(item, exporter);
				
				// Batch Progress
				JComputeEventBus.post(new BatchProgressEvent(batch));
				
			}
			else
			{
				log.warn("Discarding completed item " + item.getItemId() + " Sample " + item.getSampleId() + " for removed batch " + item.getBatchId());
			}
			
			// Remove the related simulation for this completed sim.
			controlNode.removeCompletedSim(item.getSimId());
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
		
		// If there are no activeItems, initiate completed item processing now
		// as we may have just finished the batch
		if(size == 0)
		{
			processCompletedItems();
		}
		
		batchManagerLock.acquireUninterruptibly();
		scheduleFifo();
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
			
			// batchManagerLock.release();
			
			// Notify listeners the batch is removed.
			JComputeEventBus.post(new BatchFinishedEvent(batch));
			
			positionsChangedInQueue(fifoQueue);
			
			// batchManagerLock.acquireUninterruptibly();
			
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
			
			if(batch.needsInit())
			{
				batch.init();
			}
			else
			{
				// While there are items to add and the control node can add
				// them
				while((batch.getRemaining() > 0) && controlNode.hasFreeSlot())
				{
					// Schedule and item from batch
					scheduleNextBatchItem(batch);
				}
				
				// Post the progress made
				JComputeEventBus.post(new BatchProgressEvent(batch));
			}
			
		}
		
		return true;
	}
	
	/*
	 * private void scheduleFair()
	 * {
	 * // log.debug("Schedule Fair");
	 * int size = fairQueue.size();
	 * double maxActive = controlNode.getMaxSims() - fifoQueue.size();
	 * int fairTotal, pos;
	 * if(size > 0)
	 * {
	 * pos = fairQueueLast % size;
	 * fairTotal = (int) Math.ceil(maxActive / size);
	 * log.debug("Size " + size + " maxActive " + maxActive + " fairTotal " +
	 * fairTotal);
	 * }
	 * else
	 * {
	 * // Queue Empty
	 * return;
	 * }
	 * Batch batch = null;
	 * BatchItem item = null;
	 * boolean canContinue = true;
	 * // Cycle over the batches and add one item from each to the run queue
	 * do
	 * {
	 * // Is there a free slot
	 * if(controlNode.hasFreeSlot())
	 * {
	 * batch = (Batch) fairQueue.get(pos);
	 * if(batch == null)
	 * {
	 * // no batches
	 * return;
	 * }
	 * if(batch.needsInit())
	 * {
	 * batch.init();
	 * }
	 * // Is this batch finished
	 * if(batch.isFinished())
	 * {
	 * // Add the batch to the completed list
	 * finishedBatches.add(batch);
	 * // Remove batch as its complete.
	 * fairQueue.remove(batch);
	 * batchManagerLock.release();
	 * // Notify listeners the batch is removed.
	 * JComputeEventBus.post(new BatchFinishedEvent(batch));
	 * positionsChangedInQueue(fairQueue);
	 * batchManagerLock.acquireUninterruptibly();
	 * // exit this tick
	 * canContinue = false;
	 * }
	 * else
	 * {
	 * if(batch.getRemaining() > 0)
	 * {
	 * log.debug("Batch " + batch.getBatchId() + " ActiveItemsCount " +
	 * batch.getActiveItemsCount() + " fairTotal"
	 * + fairTotal);
	 * if(batch.getActiveItemsCount() < fairTotal)
	 * {
	 * item = batch.getNext();
	 * // Once we cannot add anymore, exit, and return the
	 * // failed one to the queue
	 * if(!scheduleBatchItem(batch, item))
	 * {
	 * batch.returnItemToQueue(item);
	 * canContinue = false;
	 * }
	 * else
	 * {
	 * pos = (pos + 1) % size;
	 * }
	 * }
	 * else
	 * {
	 * pos = (pos + 1) % size;
	 * // Avoid infinite loop due having reached the fair
	 * // total
	 * if(batch.getActiveItemsCount() == fairTotal)
	 * {
	 * canContinue = false;
	 * }
	 * }
	 * }
	 * else
	 * {
	 * canContinue = false;
	 * }
	 * }
	 * }
	 * else
	 * {
	 * canContinue = false;
	 * }
	 * }
	 * while(canContinue);
	 * fairQueueLast = pos;
	 * }
	 */
	
	private boolean scheduleNextBatchItem(Batch batch)
	{
		log.debug("Schedule BatchItem");
		
		// Get the next item in the batch
		BatchItem nextItem = batch.getNext();
		String itemConfig = null;
		
		try
		{
			itemConfig = new String(batch.getItemConfig(nextItem.getItemHash()), "ISO-8859-1");
		}
		catch(UnsupportedEncodingException e)
		{
			log.error(e.getMessage());
			
			e.printStackTrace();
			
			return false;
		}
		
		// Got ItemConfig now we can add the item
		
		batchManagerLock.release();
		
		controlNode.addSimulation(nextItem, itemConfig, batch.getStatExportFormat());
		
		batchManagerLock.acquireUninterruptibly();
		
		return true;
	}
	
	/*
	 * private boolean scheduleBatchItemOLD(Batch batch, BatchItem nextItem)
	 * {
	 * log.debug("Schedule BatchItem");
	 * String itemConfig = null;
	 * try
	 * {
	 * itemConfig = new String(batch.getItemConfig(nextItem.getItemHash()),
	 * "ISO-8859-1");
	 * }
	 * catch(UnsupportedEncodingException e)
	 * {
	 * e.printStackTrace();
	 * return false;
	 * }
	 * batchManagerLock.release();
	 * int simId =
	 * controlNode.addItem(nextItem,itemConfig,batch.getStatExportFormat());
	 * batchManagerLock.acquireUninterruptibly();
	 * // If the control node has added a simulation for us
	 * if(simId > 0)
	 * {
	 * log.debug("Item : " + nextItem.getItemId() + " setting simId " + simId);
	 * nextItem.setSimId(simId);
	 * batchManagerLock.release();
	 * itemsLock.acquireUninterruptibly();
	 * activeItems.add(nextItem);
	 * itemsLock.release();
	 * batchManagerLock.acquireUninterruptibly();
	 * return true;
	 * }
	 * return false;
	 * }
	 */
	
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
		
		// Remove batch from queues if disabled and add to stop else the reverse
		// process for enabling
		if(status == false)
		{
			if(batch.getStatus() != status)
			{
				batch.setStatus(status);
				
				// In Fifo
				fifoQueue.remove(batch);
				
				stoppedBatches.add(batch);
				
				batch.setPosition((Integer.MAX_VALUE));
				
				JComputeEventBus.post(new BatchProgressEvent(batch));
				
				positionsChangedInQueue(fifoQueue);
			}
			
		}
		else
		{
			if(!batch.hasFailed())
			{
				if(batch.getStatus() != status)
				{
					batch.setStatus(status);
					
					// In Fifo
					fifoQueue.add(batch);
					
					stoppedBatches.remove(batch);
					
					JComputeEventBus.post(new BatchProgressEvent(batch));
					
					positionsChangedInQueue(fifoQueue);
				}
			}
		}
		
		batchManagerLock.release();
		
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
		
		fifoQueue.moveToFront(batch);
		
		// for the temp reference
		Batch tBatch = null;
		itr = fifoQueue.iterator();
		
		log.debug("queue " + fifoQueue.size());
		
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
		
		fifoQueue.moveToEnd(batch);
		
		// for the temp reference
		Batch tBatch = null;
		itr = fifoQueue.iterator();
		
		log.debug("queue " + fifoQueue.size());
		
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
		
		fifoQueue.moveForward(batch);
		
		// for the temp reference
		Batch tBatch = null;
		itr = fifoQueue.iterator();
		
		log.debug("queue " + fifoQueue.size());
		
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
		
		fifoQueue.moveBackward(batch);
		
		// for the temp reference
		Batch tBatch = null;
		itr = fifoQueue.iterator();
		
		log.debug("queue " + fifoQueue.size());
		
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
		
		log.info("Batch " + batch.getBatchId() + " " + batch.getFileName() + " Removed");
		
		// Remove batch from one of the queues
		if(status)
		{
			// In Fifo
			fifoQueue.remove(batch);
		}
		else
		{
			// The batch was stoped
			stoppedBatches.remove(batch);
		}
		
		batchManagerLock.release();
		
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
				
				if(item != null)
				{
					item.setComputeTime(e.getRunTime(), e.getEndEvent(), e.getStepCount());
					
					itemsLock.acquireUninterruptibly();
					activeItems.remove(item);
					completeItems.add(new CompletedItemsNode(item, e.getStatExporter()));
					itemsLock.release();
					
					// Tell the batch this item is no longer active
					batchManagerLock.acquireUninterruptibly();
					Batch batch = findBatch(item.getBatchId());
					batchManagerLock.release();
					
					if(batch != null)
					{
						batch.setItemNotActive(item);
					}
					
				}
				else
				{
					log.error("SimulationStateChangedEvent matching item not found for simId " + simId);
				}
				
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
	
	@Subscribe
	public void ControlNodeItemRequestReply(ControlNodeItemRequest request)
	{
		BatchItem batchItem = request.getBatchItem();
		ControlNodeItemRequestOperation operation = request.getOperation();
		ControlNodeItemRequestResult result = request.getResult();
		
		switch(result)
		{
			case SUCESSFUL:
				
				switch(operation)
				{
					case ADD:
						
						//
						itemsLock.acquireUninterruptibly();
						activeItems.add(batchItem);
						itemsLock.release();
						
					break;
					case REMOVE:
					
					// TODO
					
					break;
					
				}
				log.info("ControlNodeItemRequestReply " + result.toString() + "Operation " + operation.toString() + " Batch " + batchItem.getBatchId() + "Item " + batchItem.getItemId() + " Sample "
						+ batchItem.getSampleId() + " Local SimId " + batchItem.getSimId());
						
			break;
			case FAILED:
				
				// BATCH has also failed
				log.error("ControlNodeItemRequestReply " + result.toString() + " Operation " + operation.toString() + " Batch " + batchItem.getBatchId() + " Item " + batchItem.getItemId() + " Sample "
						+ batchItem.getSampleId() + " Local SimId " + batchItem.getSimId());
						
				batchManagerLock.acquireUninterruptibly();
				Batch batch = findBatch(batchItem.getBatchId());
				batchManagerLock.release();
				
				if(batch != null)
				{
					batch.setFailed();
					// Stop Batch Processing
					setStatus(batchItem.getBatchId(), false);
				}
				
				log.error("Processing Batch " + batchItem.getBatchId() + " Failed");
				
			break;
		}
		
	}
}
