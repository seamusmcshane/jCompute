package jcompute.cluster.controlnode;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.eventbus.Subscribe;

import jcompute.JComputeEventBus;
import jcompute.batch.BatchItem;
import jcompute.cluster.controlnode.computenodemanager.ComputeNodeManager2;
import jcompute.cluster.controlnode.computenodemanager.event.ComputeNodeManagerItemStateEvent;
import jcompute.cluster.controlnode.computenodemanager.request.NodeItemRequest;
import jcompute.cluster.controlnode.computenodemanager.request.NodeItemRequest.NodeItemRequestOperation;
import jcompute.cluster.controlnode.computenodemanager.request.NodeItemRequest.NodeItemRequestResult;
import jcompute.cluster.controlnode.event.ControlNodeItemStateEvent;
import jcompute.cluster.controlnode.event.NodeEvent;
import jcompute.cluster.controlnode.event.NodeEvent.NodeEventType;
import jcompute.cluster.controlnode.event.StatusChanged;
import jcompute.cluster.controlnode.mapping.RemoteSimulationMapping;
import jcompute.cluster.controlnode.request.ControlNodeItemRequest;
import jcompute.cluster.controlnode.request.ControlNodeItemRequest.ControlNodeItemRequestOperation;
import jcompute.cluster.controlnode.request.ControlNodeItemRequest.ControlNodeItemRequestResult;
import jcompute.cluster.ncp.NCP;
import jcompute.results.export.ExportFormat;
import jcompute.simulationmanager.event.SimulationsManagerEvent;
import jcompute.simulationmanager.event.SimulationsManagerEventType;

public class ControlNodeServer
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(ControlNodeServer.class);
	
	// Dynamic based on total of active nodes max sims
	private int maxSims = 0;
	
	// Value Used for mapping + Total of the simulation processed
	private int simulationNum = 0;
	
	/* Server Listening Socket */
	private ServerSocket listenSocket;
	
	// Connections Processed
	private int connectionNumber = 0;
	
	// Active Nodes indexed by nodeId
	private LinkedList<ComputeNodeManager2> activeNodes;
	
	/* Connecting Nodes List */
	private LinkedList<ComputeNodeManager2> connectingNodes;
	
	private final boolean allowMulti;
	private final int socketTX;
	private final int socketRX;
	private final boolean tcpNoDelay;
	private final int txFreq;
	
	// List of priority re-scheduled Simulations (recovered from nodes that disappear)
	private ArrayList<Integer> recoveredSimIds;
	private boolean hasRecoverableSimsIds = false;
	
	// Mapping between Nodes/RemoteSimIds and LocalSimIds - indexed by (LOCAL-simId)
	private HashMap<Integer, RemoteSimulationMapping> localSimulationMap;
	
	private Semaphore controlNodeLock = new Semaphore(1, false);
	
	// Maximum nodes that can be pending connection
	private final int MAX_OUTSTANDING_CONNECTIONS = 50;
	
	// In milliseconds
	private final int TickFrequency = 1000;
	
	/// Node Statistics Frequency (1 minute)
	private final int NODE_STATISTICS_FREQUENCY = 60000;
	
	private int tickCount;
	private long lastStatisticsTime;
	
	public ControlNodeServer(boolean allowMulti, int socketTX, int socketRX, boolean tcpNoDelay, int txFreq)
	{
		this.allowMulti = allowMulti;
		this.socketTX = socketTX;
		this.socketRX = socketRX;
		this.tcpNoDelay = tcpNoDelay;
		this.txFreq = txFreq;
		
		log.info("Allow multiple nodes to connect from same address : " + this.allowMulti);
		log.info("TCP TX Buffer : " + this.socketTX);
		log.info("TCP RX Buffer : " + this.socketRX);
		log.info("TCP No Delay : " + this.tcpNoDelay);
		log.info("TX Freq : " + this.txFreq);
		
		// Local to remote simulation map
		localSimulationMap = new HashMap<Integer, RemoteSimulationMapping>();
		
		// Recovered Simulation Numbers
		recoveredSimIds = new ArrayList<Integer>();
		
		// List of simulation nodes.
		activeNodes = new LinkedList<ComputeNodeManager2>();
		connectingNodes = new LinkedList<ComputeNodeManager2>();
	}
	
	public void start()
	{
		log.info("Starting");
		
		// Register on the event bus
		JComputeEventBus.register(this);
		
		try
		{
			listenSocket = new ServerSocket();
			
			// Listening RX must be done before address bind
			listenSocket.setReceiveBufferSize(socketRX);
			
			// Wait on the socket for new connections for TickFrequency ms otherwise perform other processing.
			// This is fine as we dont expect 100k connections
			listenSocket.setSoTimeout(TickFrequency);
			
			listenSocket.bind(new InetSocketAddress("0.0.0.0", NCP.StandardServerPort), MAX_OUTSTANDING_CONNECTIONS);
		}
		catch(Exception e)
		{
			// Unrecoverable error
			log.error("ControlNodeServer Processing Exited : " + e.getMessage());
			
			System.exit(-1);
		}
		
		// Create the processing thread
		Thread processing = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				log.info("Listening Address : " + listenSocket.getLocalSocketAddress());
				
				while(listenSocket.isBound())
				{
					try
					{
						// With TickFrequency Timeout enabled - see catch
						Socket nodeSocket = listenSocket.accept();
						
						log.info("New Connection from : " + nodeSocket.getRemoteSocketAddress());
						
						// We are handling the new connection
						controlNodeLock.acquireUninterruptibly();
						
						// Default to ignoring existing active/connecting nodes
						boolean existingActive = false;
						boolean existingConnecting = false;
						
						// If we do not allow multiple connections from the network same address, check for existing active/connecting nodes.
						if(!allowMulti)
						{
							existingActive = existingActiveNode(nodeSocket);
							existingConnecting = existingConnectingNode(nodeSocket);
						}
						
						// If there is existing active node from this address
						if(!existingActive)
						{
							// Add create a ComputeNodeManager for this ComputeNode and add to connecting lists.
							ComputeNodeManager2 nm = new ComputeNodeManager2(++connectionNumber, nodeSocket, socketTX, tcpNoDelay, txFreq);
							
							connectingNodes.add(nm);
							
							// Start the new ComputeNodeManager
							nm.start();
							
							// Post the new connecting node event
							JComputeEventBus.post(new NodeEvent(NodeEventType.CONNECTING, nm.getNodeConfig()));
							
							// If there is already an existing connecting node but it is not connected yet remove it.
							if(existingConnecting)
							{
								ComputeNodeManager2 existingNode = getExistingConnectingNode(nodeSocket);
								
								connectingNodes.remove(existingNode);
								
								// Post the remove event
								JComputeEventBus.post(new NodeEvent(NodeEventType.REMOVED, existingNode.getNodeConfig()));
							}
						}
						else
						{
							// There is an exiting connected node - give it priority
							log.warn("Closing Socket as a ComputeNode already exists on :" + nodeSocket.getRemoteSocketAddress());
							nodeSocket.close();
						}
						
						log.debug("------------------------------------");
						log.debug("Added (" + connectingNodes.size() + ")");
						log.debug("------------------------------------");
						for(ComputeNodeManager2 node : connectingNodes)
						{
							log.debug("ComputeNode :" + node.getUid());
						}
						log.debug("------------------------------------");
						
						controlNodeLock.release();
					}
					catch(SocketTimeoutException e)
					{
						refreshNodes();
					}
					catch(IOException e)
					{
						log.error(e.toString());
						
					}
				}
			}
		});
		
		// Connection Processing
		processing.setName("ControlNodeServer Processing");
		processing.start();
	}
	
	private void refreshNodes()
	{
		// No new connections
		controlNodeLock.acquireUninterruptibly();
		
		// Detect nodes that are now ready in the connected nodes list and add them to the active nodes
		// Now remove any ready / failed nodes in the connecting nodes list
		Iterator<ComputeNodeManager2> itr = connectingNodes.iterator();
		while(itr.hasNext())
		{
			ComputeNodeManager2 node = itr.next();
			
			if(node.isRunning())
			{
				itr.remove();
				
				activeNodes.add(node);
				
				maxSims += node.getMaxSims();
				
				log.debug("ComputeNode " + node.getUid() + " now Active (Max Sims " + maxSims + ")");
				
				// Sort the ComputeNodes by weighting
				Collections.sort(activeNodes, new NodeManagerComparator());
				
				log.debug("------------------------------------");
				log.debug("Active (" + activeNodes.size() + ")");
				log.debug("------------------------------------");
				for(ComputeNodeManager2 aNode : activeNodes)
				{
					log.debug("ComputeNode " + aNode.getUid() + ": " + aNode.getWeighting());
					
				}
				log.debug("------------------------------------");
				
				JComputeEventBus.post(new NodeEvent(NodeEventType.CONNECTED, node.getNodeConfig()));
			}
		}
		
		// Check existing nodes are still active, and request node statistics from those that are active. (at NODE_STATISTICS_FREQUENCY).
		// If the node is not active recover any outstanding simulation id.
		
		// Request current statistics time every minute
		long currentStatisticsTime = System.currentTimeMillis();
		boolean requestStatistics = ((currentStatisticsTime - lastStatisticsTime) >= NODE_STATISTICS_FREQUENCY);
		
		itr = activeNodes.iterator();
		while(itr.hasNext())
		{
			ComputeNodeManager2 node = itr.next();
			
			if(node.isShutdown())
			{
				ArrayList<Integer> nodeRecoveredSimIds = node.getRecoverableSimsIds();
				
				Iterator<Integer> nRSIdsIter = nodeRecoveredSimIds.iterator();
				while(nRSIdsIter.hasNext())
				{
					recoveredSimIds.add(nRSIdsIter.next());
				}
				
				// Inactive ComputeNode Removed
				JComputeEventBus.post(new NodeEvent(NodeEventType.DISCONNECTED, node.getNodeConfig()));
				
				log.info("ComputeNode " + node.getUid() + " no longer active");
				// node.destroy("ComputeNode no longer active");
				itr.remove();
				
				maxSims -= node.getMaxSims();
			}
			else
			{
				// Request statistics for this node.
				if(requestStatistics)
				{
					// Use tick count as id (ticks up at NODE_STATISTICS_FREQUENCY)
					node.triggerNodeStatRequest(tickCount);
				}
			}
		}
		
		// Statistics where requested
		if(requestStatistics)
		{
			log.info("Node Statistics Tick : " + tickCount);
			lastStatisticsTime = System.currentTimeMillis();
			tickCount++;
		}
		
		if(recoveredSimIds.size() > 0)
		{
			hasRecoverableSimsIds = true;
		}
		
		controlNodeLock.release();
		
		// TODO
		JComputeEventBus.post(new StatusChanged(listenSocket.getInetAddress().getHostAddress(), String.valueOf(listenSocket.getLocalPort()), String.valueOf(
		connectingNodes.size()), String.valueOf(activeNodes.size()), String.valueOf(maxSims), String.valueOf(simulationNum)));
	}
	
	public boolean hasFreeSlot()
	{
		controlNodeLock.acquireUninterruptibly();
		
		boolean tActive = false;
		
		Iterator<ComputeNodeManager2> itr = activeNodes.iterator();
		while(itr.hasNext())
		{
			ComputeNodeManager2 node = itr.next();
			
			tActive |= node.hasFreeSlot();
		}
		
		controlNodeLock.release();
		
		return tActive;
	}
	
	public boolean hasRecoverableSimIds()
	{
		return hasRecoverableSimsIds;
	}
	
	public ArrayList<Integer> getRecoverableSimIds()
	{
		controlNodeLock.acquireUninterruptibly();
		
		ArrayList<Integer> simIds = new ArrayList<Integer>();
		
		Iterator<Integer> itr = recoveredSimIds.iterator();
		while(itr.hasNext())
		{
			simIds.add(itr.next());
		}
		
		recoveredSimIds = new ArrayList<Integer>();
		
		hasRecoverableSimsIds = false;
		
		controlNodeLock.release();
		
		return simIds;
	}
	
	public boolean existingActiveNode(Socket nodeSocket)
	{
		boolean nodeExists = false;
		
		String socketAddress = nodeSocket.getInetAddress().getHostAddress();
		
		for(ComputeNodeManager2 node : activeNodes)
		{
			if(node.getAddress().equals(socketAddress))
			{
				nodeExists = true;
				break;
			}
		}
		
		return nodeExists;
	}
	
	public boolean existingConnectingNode(Socket nodeSocket)
	{
		boolean nodeExists = false;
		
		String socketAddress = nodeSocket.getInetAddress().getHostAddress();
		
		for(ComputeNodeManager2 node : connectingNodes)
		{
			if(node.getAddress().equals(socketAddress))
			{
				nodeExists = true;
				break;
			}
		}
		
		return nodeExists;
	}
	
	public ComputeNodeManager2 getExistingConnectingNode(Socket nodeSocket)
	{
		ComputeNodeManager2 tNode = null;
		
		String socketAddress = nodeSocket.getInetAddress().getHostAddress();
		
		for(ComputeNodeManager2 node : connectingNodes)
		{
			if(node.getAddress().equals(socketAddress))
			{
				tNode = node;
				break;
			}
		}
		
		return tNode;
	}
	
	/**
	 * Add an item for processing to the cluster
	 *
	 * @param item
	 * @param itemConfig
	 * @param statExportFormat
	 */
	public void addSimulation(BatchItem item, String itemConfig, ExportFormat traceExportFormat)
	{
		controlNodeLock.acquireUninterruptibly();
		
		// The item config
		String config = itemConfig;
		
		// Suffix is item cache index
		String fileNameSuffix = String.valueOf(item.getCacheIndex());
		
		// Increment the simulation counter values (1 Base)
		simulationNum++;
		
		// Assign the item a simulation number
		item.setSimId(simulationNum);
		
		// Create a mapping with the newly assigned local simulation number
		RemoteSimulationMapping mapping = new RemoteSimulationMapping(item, simulationNum);
		
		// Stat Export format for this sim
		mapping.setExportFormat(traceExportFormat);
		
		// suffix for exported stat files
		mapping.setFileNameSuffix(fileNameSuffix);
		
		// Find the node with a free slot
		log.debug("Finding free node (" + activeNodes.size() + ")");
		for(ComputeNodeManager2 node : activeNodes)
		{
			log.debug("ComputeNode " + node.getUid());
			if(node.hasFreeSlot() && node.isRunning())
			{
				log.info("Adding Batch " + item.getBatchId() + " Item " + item.getItemId() + " SampleId " + item.getSampleId() + " to ComputeNode " + node
				.getUid());
				
				// Record ComputeNode in mapping
				mapping.setNodeUid(node.getUid());
				
				// Add the item
				node.addSimulation(config, mapping);
				
				break;
			}
		}
		
		controlNodeLock.release();
	}
	
	public void removeCompletedSim(int simId)
	{
		controlNodeLock.acquireUninterruptibly();
		
		// Look up and remove the mapping
		localSimulationMap.remove(simId);
		
		// Post a Removed SimEvent
		JComputeEventBus.post(new SimulationsManagerEvent(simId, SimulationsManagerEventType.RemovedSim));
		
		controlNodeLock.release();
	}
	
	public int getMaxSims()
	{
		return maxSims;
	}
	
	/*
	 * ************************************************************************************************************************************************************
	 * Event Bus Subscribers
	 * ************************************************************************************************************************************************************
	 */
	
	/*
	 * The intermediate NodeManagerItemStateChanged is needed to keep simulation state events processing in order on the event bus, due to add/remove having a intermediate event.
	 */
	@Subscribe
	public void NodeManagerItemStateChanged(ComputeNodeManagerItemStateEvent computeNodeManagerItemStateEvent)
	{
		// Forward the item state change as a control node item state event
		JComputeEventBus.post(new ControlNodeItemStateEvent(computeNodeManagerItemStateEvent.getSimStateEvent()));
	}
	
	@Subscribe
	public void NodeItemRequestProcessed(NodeItemRequest request)
	{
		controlNodeLock.acquireUninterruptibly();
		
		RemoteSimulationMapping mapping = request.getMapping();
		NodeItemRequestOperation operation = request.getOperation();
		NodeItemRequestResult result = request.getResult();
		
		switch(result)
		{
			case SUCESSFUL:
				
				log.debug("NodeItemRequestProcessed " + result.toString() + " Operation " + operation.toString() + " Simulation on ComputeNode " + mapping
				.getNodeUid() + " Local SimId " + mapping.getLocalSimId() + " Remote SimId " + mapping.getRemoteSimId());
				
				switch(operation)
				{
					case ADD:
						// Locally cache the mapping index by the new local sim
						// number
						localSimulationMap.put(mapping.getLocalSimId(), mapping);
						
						JComputeEventBus.post(new ControlNodeItemRequest(mapping.getBatchItem(), ControlNodeItemRequestOperation.ADD,
						ControlNodeItemRequestResult.SUCESSFUL));
					break;
					case REMOVE:
						JComputeEventBus.post(new ControlNodeItemRequest(mapping.getBatchItem(), ControlNodeItemRequestOperation.REMOVE,
						ControlNodeItemRequestResult.SUCESSFUL));
					break;
					default:
						log.error("Unhandled Operation " + operation.toString() + " " + this.getClass() + " NodeItemRequestProcessed");
					break;
				}
				
			break;
			case FAILED:
				
				log.error("ComputeNode Item Request Failed - " + operation.toString() + " SimId " + mapping.getLocalSimId() + " ComputeNode " + mapping
				.getNodeUid());
				
				switch(operation)
				{
					case ADD:
						JComputeEventBus.post(new ControlNodeItemRequest(mapping.getBatchItem(), ControlNodeItemRequestOperation.ADD,
						ControlNodeItemRequestResult.FAILED));
					break;
					case REMOVE:
						JComputeEventBus.post(new ControlNodeItemRequest(mapping.getBatchItem(), ControlNodeItemRequestOperation.REMOVE,
						ControlNodeItemRequestResult.FAILED));
					break;
					default:
						log.error("Unhandled Operation " + operation.toString() + " " + this.getClass() + " NodeItemRequestProcessed");
					break;
				}
				
			break;
		}
		
		controlNodeLock.release();
	}
	
	private class NodeManagerComparator implements Comparator<ComputeNodeManager2>
	{
		@Override
		public int compare(ComputeNodeManager2 node1, ComputeNodeManager2 node2)
		{
			if(node1.getWeighting() < node2.getWeighting())
			{
				return -1;
			}
			else if(node1.getWeighting() > node2.getWeighting())
			{
				return 1;
			}
			
			return 0;
		}
	}
}
