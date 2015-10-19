package jCompute.Cluster.Controller.ControlNode;

import jCompute.JComputeEventBus;
import jCompute.Batch.BatchItem;
import jCompute.Cluster.Controller.ControlNode.Event.NodeAdded;
import jCompute.Cluster.Controller.ControlNode.Event.NodeRemoved;
import jCompute.Cluster.Controller.ControlNode.Event.StatusChanged;
import jCompute.Cluster.Controller.ControlNode.Request.ControlNodeItemRequest;
import jCompute.Cluster.Controller.ControlNode.Request.ControlNodeItemRequest.ControlNodeItemRequestOperation;
import jCompute.Cluster.Controller.ControlNode.Request.ControlNodeItemRequest.ControlNodeItemRequestResult;
import jCompute.Cluster.Controller.Mapping.RemoteSimulationMapping;
import jCompute.Cluster.Controller.NodeManager.NodeManager;
import jCompute.Cluster.Controller.NodeManager.Request.NodeItemRequest;
import jCompute.Cluster.Controller.NodeManager.Request.NodeItemRequest.NodeItemRequestOperation;
import jCompute.Cluster.Controller.NodeManager.Request.NodeItemRequest.NodeItemRequestResult;
import jCompute.Cluster.Node.NodeDetails.NodeInfo;
import jCompute.Cluster.Protocol.NCP;
import jCompute.SimulationManager.Event.SimulationsManagerEvent;
import jCompute.SimulationManager.Event.SimulationsManagerEventType;
import jCompute.Stats.StatExporter.ExportFormat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

public class ControlNode
{
	// SL4J Logger
	private static Logger log = LoggerFactory.getLogger(ControlNode.class);
	
	// Dynamic based on total of active nodes max sims
	private int maxSims = 0;
	
	// Value Used for mapping + Total of the simulation processed
	private int simulationNum = 0;
	
	/* Server Listening Socket */
	private ServerSocket listenSocket;
	
	/* Connections Processed */
	private int connectionNumber = 0;
	
	/* Active Nodes indexed by nodeId */
	private LinkedList<NodeManager> activeNodes;
	
	/* Connecting Nodes List */
	private LinkedList<NodeManager> connectingNodes;
	private boolean allowMulti;
	
	private Timer ncpTimer;
	private int ncpTimerSpeed = 1;
	/*
	 * List of priority re-scheduled Simulations (recovered from nodes that
	 * disappear)
	 */
	private ArrayList<Integer> recoveredSimIds;
	private boolean hasRecoverableSimsIds = false;
	
	/*
	 * Mapping between Nodes/RemoteSimIds and LocalSimIds - indexed by (LOCAL)
	 * simId
	 */
	private HashMap<Integer, RemoteSimulationMapping> localSimulationMap;
	
	private Semaphore controlNodeLock = new Semaphore(1, false);
	
	private int timerCount;
	
	public ControlNode(boolean allowMulti)
	{
		log.info("Starting ControlNode");
		
		this.allowMulti = allowMulti;
		
		log.info("Allow multiple nodes to connect from same address : " + allowMulti);
		
		// Local to remote simulation map
		localSimulationMap = new HashMap<Integer, RemoteSimulationMapping>();
		
		// Recovered Simulation Numbers
		recoveredSimIds = new ArrayList<Integer>();
		
		// List of simulation nodes.
		activeNodes = new LinkedList<NodeManager>();
		connectingNodes = new LinkedList<NodeManager>();
		
		// Register on the event bus
		JComputeEventBus.register(this);
		
		createAndStartRecieveThread();
		
		startNSMCPTimer();
	}
	
	private class NodeManagerComparator implements Comparator<NodeManager>
	{
		@Override
		public int compare(NodeManager node1, NodeManager node2)
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
	
	private void startNSMCPTimer()
	{
		timerCount = 0;
		ncpTimer = new Timer("NCP Timer");
		ncpTimer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				controlNodeLock.acquireUninterruptibly();
				
				log.debug("NSMCPTimer");
				log.debug("------------------------------------");
				log.debug("Connecting (" + connectingNodes.size() + ")");
				log.debug("------------------------------------");
				for(NodeManager node : connectingNodes)
				{
					log.debug("Node :" + node.getUid());
				}
				log.debug("------------------------------------");
				
				// Detect nodes that are now ready in the connected nodes list
				// and add them to the active nodes
				// Now remove any ready / failed nodes in the connecting nodes list
				Iterator<NodeManager> itr = connectingNodes.iterator();
				while(itr.hasNext())
				{
					NodeManager node = itr.next();
					
					if(node.isReady())
					{
						itr.remove();
						
						activeNodes.add(node);
						
						maxSims += node.getMaxSims();
						
						log.debug("Node " + node.getUid() + " now Active (Max Sims " + maxSims + ")");
						
						// Sort the Node by weighting
						Collections.sort(activeNodes, new NodeManagerComparator());
						
						log.info("------------------------------------");
						log.info("Active (" + activeNodes.size() + ")");
						log.info("------------------------------------");
						for(NodeManager aNode : activeNodes)
						{
							log.info("Node " + aNode.getUid() + ": " + aNode.getWeighting());
							
						}
						log.info("------------------------------------");
						
						JComputeEventBus.post(new NodeAdded(node.getNodeConfig()));
					}
					else if(node.getReadyStateTimeOutValue() == NCP.ReadyStateTimeOut)
					{
						itr.remove();
						node.destroy("Ready State Timeout");
					}
					else if(node.hasFailedReg())
					{
						itr.remove();
						node.destroy("failed to register " + node.getRegFailedReason());
					}
					else
					{
						node.incrementTimeOut(ncpTimerSpeed);
					}
				}
				
				itr = activeNodes.iterator();
				while(itr.hasNext())
				{
					NodeManager node = itr.next();
					
					if(node.isShutdown())
					{
						
						ArrayList<Integer> nodeRecoveredSimIds = node.getRecoverableSimsIds();
						
						Iterator<Integer> nRSIdsIter = nodeRecoveredSimIds.iterator();
						while(nRSIdsIter.hasNext())
						{
							recoveredSimIds.add(nRSIdsIter.next());
						}
						
						// InActive Node Removed
						JComputeEventBus.post(new NodeRemoved(node.getNodeConfig()));
						
						log.debug("Node " + node.getUid() + " no longer Active");
						node.destroy("Node no longer active");
						itr.remove();
						
						maxSims -= node.getMaxSims();
					}
					else
					{
						// Every minute
						if(timerCount % 60 == 0)
						{
							// Seconds to Minutes
							node.triggerNodeStatRequest(timerCount / 60);
						}
					}
					
				}
				
				if(recoveredSimIds.size() > 0)
				{
					hasRecoverableSimsIds = true;
				}
				
				controlNodeLock.release();
				
				JComputeEventBus.post(new StatusChanged(listenSocket.getInetAddress().getHostAddress(),
						String.valueOf(listenSocket.getLocalPort()), String.valueOf(connectingNodes.size()),
						String.valueOf(activeNodes.size()), String.valueOf(maxSims), String.valueOf(simulationNum)));
						
				timerCount += ncpTimerSpeed;
			}
		}, 0, ncpTimerSpeed * 1000);
	}
	
	public boolean hasFreeSlot()
	{
		controlNodeLock.acquireUninterruptibly();
		
		boolean tActive = false;
		
		Iterator<NodeManager> itr = activeNodes.iterator();
		while(itr.hasNext())
		{
			NodeManager node = itr.next();
			
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
	
	private void createAndStartRecieveThread()
	{
		try
		{
			listenSocket = new ServerSocket();
			
			listenSocket.bind(new InetSocketAddress("0.0.0.0", NCP.StandardServerPort));
			
			Thread thread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					log.info("Listening Address : " + listenSocket.getLocalSocketAddress());
					
					while(listenSocket.isBound())
					{
						log.info("Ready for Connections");
						
						try
						{
							Socket nodeSocket = listenSocket.accept();
							
							nodeSocket.setSendBufferSize(32768);
							
							log.info("New Connection from : " + nodeSocket.getRemoteSocketAddress());
							
							// Accept new Connections
							controlNodeLock.acquireUninterruptibly();
							
							// Default to ignoring existing active/connecting
							// nodes
							boolean existingActive = false;
							boolean existingConnecting = false;
							
							// if we do not allow multiple connections from the
							// same address, check for existing
							// active/connecting nodes.
							if(!allowMulti)
							{
								existingActive = existingActiveNode(nodeSocket);
								existingConnecting = existingConnectingNode(nodeSocket);
							}
							
							// If there is existing active node from this
							// address
							if(!existingActive)
							{
								// If there is a existing connecting node -
								// remove it.
								if(existingConnecting)
								{
									NodeManager existingNode = getExistingConnectingNode(nodeSocket);
									
									connectingNodes.remove(existingNode);
									
									existingNode.destroy("A new node from " + existingNode.getAddress() + " has connected");
									
									nodeSocket.close();
								}
								else
								{
									// Add to NodeManager list of connecting
									// node
									connectingNodes.add(new NodeManager(++connectionNumber, nodeSocket));
								}
								
							}
							else
							{
								log.warn("Closing Socket as a Node already exists on :" + nodeSocket.getRemoteSocketAddress());
								nodeSocket.close();
							}
							
							log.debug("------------------------------------");
							log.debug("Added (" + connectingNodes.size() + ")");
							log.debug("------------------------------------");
							for(NodeManager node : connectingNodes)
							{
								log.debug("Node :" + node.getUid());
							}
							log.debug("------------------------------------");
							
							controlNodeLock.release();
							
						}
						catch(IOException e)
						{
							log.error(e.toString());
						}
						
					}
					
				}
				
			});
			
			thread.setName("Recieve Thread");
			thread.start();
		}
		catch(Exception e)
		{
			log.error("Server Recieve Thread Exited : " + e.getMessage());
			
			System.exit(-1);
		}
	}
	
	public boolean existingActiveNode(Socket nodeSocket)
	{
		boolean nodeExists = false;
		
		String socketAddress = nodeSocket.getInetAddress().getHostAddress();
		
		for(NodeManager node : activeNodes)
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
		
		for(NodeManager node : connectingNodes)
		{
			if(node.getAddress().equals(socketAddress))
			{
				nodeExists = true;
				break;
			}
		}
		
		return nodeExists;
	}
	
	public NodeManager getExistingConnectingNode(Socket nodeSocket)
	{
		NodeManager tNode = null;
		
		String socketAddress = nodeSocket.getInetAddress().getHostAddress();
		
		for(NodeManager node : connectingNodes)
		{
			if(node.getAddress().equals(socketAddress))
			{
				tNode = node;
				break;
			}
		}
		
		return tNode;
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
				
				log.debug("NodeItemRequestProcessed " + result.toString() + " Operation " + operation.toString() + " Simulation on Node "
						+ mapping.getNodeUid() + " Local SimId " + mapping.getLocalSimId() + " Remote SimId " + mapping.getRemoteSimId());
						
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
				
				log.error("Node Item Request Failed - " + operation.toString() + " SimId " + mapping.getLocalSimId() + " Node "
						+ mapping.getNodeUid());
						
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
	
	/**
	 * Add an item for processing to the cluster
	 * @param item
	 * @param itemConfig
	 * @param statExportFormat
	 */
	public void addSimulation(BatchItem item, String itemConfig, ExportFormat statExportFormat)
	{
		controlNodeLock.acquireUninterruptibly();
		
		// The item config
		String config = itemConfig;
		
		// Suffix is item Hash
		String fileNameSuffix = item.getItemHash();
		
		// Increment the simulation counter values (1 Base)
		simulationNum++;
		
		// Assign the item a simulation number
		item.setSimId(simulationNum);
		
		// Create a mapping with the newly assigned local simulation number
		RemoteSimulationMapping mapping = new RemoteSimulationMapping(item, simulationNum);
		
		// Stat Export format for this sim
		mapping.setExportFormat(statExportFormat);
		
		// suffix for exported stat files
		mapping.setFileNameSuffix(fileNameSuffix);
		
		// Find the node with a free slot
		log.debug("Finding free node (" + activeNodes.size() + ")");
		for(NodeManager node : activeNodes)
		{
			log.debug("Node " + node.getUid());
			if(node.hasFreeSlot() && node.isRunning())
			{
				log.info("Adding Batch " + item.getBatchId() + " Item " + item.getItemId() + " SampleId " + item.getSampleId() + " to Node " + node.getUid());
				
				// Record Node in mapping
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
		
		JComputeEventBus.post(new SimulationsManagerEvent(simId, SimulationsManagerEventType.RemovedSim));
		
		controlNodeLock.release();
	}
	
	public int getMaxSims()
	{
		return maxSims;
	}
}
