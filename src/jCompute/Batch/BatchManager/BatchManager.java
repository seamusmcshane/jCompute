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
import jCompute.Cluster.Node.NodeConfiguration;
import jCompute.Datastruct.List.ManagedBypassableQueue;
import jCompute.Datastruct.List.Interface.StoredQueuePosition;
import jCompute.Simulation.Event.SimulationStateChangedEvent;
import jCompute.Simulation.SimulationState.SimState;
import jCompute.Stats.StatExporter;
import jCompute.Stats.StatExporter.ExportFormat;
import jCompute.Thread.SimpleNamedThreadFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

	private ArrayList<BatchItem> completeItems;

	// Scheduler
	private Timer batchScheduler;

	// Completed Items Processor
	private Timer batchCompletedItemsProcessor;

	private ExecutorService completedItemsStatFetch = Executors.newFixedThreadPool(Runtime.getRuntime()
			.availableProcessors(), new SimpleNamedThreadFactory("Completed Items Stat Fetch"));

	public BatchManager()
	{
		this.controlNode = new ControlNode();

		fifoQueue = new ManagedBypassableQueue();

		fairQueue = new ManagedBypassableQueue();

		finishedBatches = new ArrayList<Batch>(16);

		stoppedBatches = new LinkedList<Batch>();

		activeItems = new ArrayList<BatchItem>(16);

		completeItems = new ArrayList<BatchItem>();

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

		if(controlNode.hasRecoverableSimIds())
		{
			ArrayList<Integer> recoveredIds = controlNode.getRecoverableSimIds();

			Iterator<Integer> itr = recoveredIds.iterator();
			while(itr.hasNext())
			{
				int simId = itr.next();

				BatchItem item = findActiveBatchItemFromSimId(simId);

				System.out.println("activeItems " + activeItems.size());

				System.out.println("item " + item);

				Batch batch = findBatch(item.getBatchId());

				System.out.println("batch " + batch);

				itemsLock.acquireUninterruptibly();

				activeItems.remove(item);

				itemsLock.release();

				batch.returnItemToQueue(item);
			}

		}

		batchManagerLock.release();

	}

	private void processCompletedItems()
	{
		itemsLock.acquireUninterruptibly();

		Iterator<BatchItem> itr = completeItems.iterator();

		while(itr.hasNext())
		{
			BatchItem item = itr.next();

			Batch batch = null;

			batchManagerLock.acquireUninterruptibly();

			if(item != null)
			{
				log.debug("Item : " + item.getItemId());
				batch = findBatch(item.getBatchId());
			}

			batchManagerLock.release();

			if(batch != null)
			{
				completedItemsStatFetch.execute(new StatFetchTask(controlNode, batch, item, ExportFormat.ZXML));
			}
			else
			{
				log.warn("Simulation Event for NULL batch " + item.getBatchId());

				controlNode.removeSimulation(item.getSimId());
			}

		}

		completeItems = new ArrayList<BatchItem>();

		itemsLock.release();
	}

	private void schedule()
	{
		// log.info("Scheduler Tick");

		recoverItemsFromInactiveNodes();

		// processCompletedItems();

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
		// log.debug("Schedule Fifo");

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

		// While there are items to add and the control node can add them
		while((batch.getRemaining() > 0))
		{
			// Is there a free slot
			if(controlNode.hasFreeSlot())
			{
				// dequeue the next item in the batch
				BatchItem item = batch.getNext();

				// Schedule it
				if(!scheduleBatchItem(item))
				{
					batch.returnItemToQueue(item);
					break;
				}
				else
				{
					JComputeEventBus.post(new BatchProgressEvent(batch));
				}
			}
			else
			{
				break;
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

				// Is this batch finished
				if(batch.getCompleted() == batch.getBatchItems())
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

	private boolean scheduleBatchItem(BatchItem item)
	{
		log.debug("Schedule BatchItem");

		batchManagerLock.release();

		int simId = controlNode.addSimulation(item.getConfigText(), -1);

		batchManagerLock.acquireUninterruptibly();

		// If the control node has added a simulation for us
		if(simId > 0)
		{
			log.debug("Item : " + item.getItemId() + " setting simId " + simId);

			item.setSimId(simId);

			batchManagerLock.release();

			itemsLock.acquireUninterruptibly();
			activeItems.add(item);
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
					item.setComplete(e.getRunTime(), e.getEndEvent(), e.getStepCount());
					activeItems.remove(item);
					completeItems.add(item);
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

	public NodeConfiguration[] getNodesInfo()
	{
		return controlNode.getNodesInfo();
	}

	public String[] getClusterStatus()
	{
		return controlNode.getStatus();
	}

	private BatchItem[] getListItems(int batchId, int queueNum)
	{
		batchManagerLock.acquireUninterruptibly();

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

		batchManagerLock.release();

		return list;
	}

	public BatchItem[] getItemQueue(int batchId)
	{
		return getListItems(batchId, 0);
	}

	public BatchItem[] getActiveItems(int batchId)
	{
		return getListItems(batchId, 1);
	}

	public BatchItem[] getCompletedItems(int batchId)
	{
		return getListItems(batchId, 2);
	}

	public void setEnabled(int batchId, boolean enabled)
	{
		batchManagerLock.acquireUninterruptibly();

		Batch batch = findBatch(batchId);

		batch.setEnabled(enabled);

		// Remove batch from queues if disabled and add to stop else the reverse
		// process for enabling
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
			if(batch.getEnabled() == true)
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

		boolean enabled = batch.getEnabled();

		// Remove batch from one of the queues
		if(enabled)
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

	private class StatFetchTask implements Runnable
	{
		private ControlNode simsManager;
		private Batch batch;
		private BatchItem item;
		private ExportFormat format;

		public StatFetchTask(ControlNode simsManager, Batch batch, BatchItem item, ExportFormat format)
		{
			super();
			this.simsManager = simsManager;
			this.batch = batch;
			this.item = item;
			this.format = format;
		}

		@Override
		public void run()
		{
			long timeStart = System.currentTimeMillis();
			long timeEnd;
			
			// Export Stats // ExportFormat.ZXML
			StatExporter exporter = simsManager.getStatExporter(item.getSimId(), String.valueOf(item.getItemHash()),
					format);

			timeEnd = System.currentTimeMillis();
			
			// Include the stat fetch time in time taken for each items processing
			long total = item.getRunTime() + ( timeEnd-timeStart);
			item.updateRunTime(total);
			
			batch.setItemComplete(item, exporter);

			log.info("Batch Item Finished : " + batch.getBatchId());

			// Batch Progress
			JComputeEventBus.post(new BatchProgressEvent(batch));
		}
	}

}
