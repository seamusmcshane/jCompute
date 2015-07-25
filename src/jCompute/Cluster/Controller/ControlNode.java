package jCompute.Cluster.Controller;

import jCompute.JComputeEventBus;
import jCompute.Cluster.Controller.Event.NodeAdded;
import jCompute.Cluster.Controller.Event.NodeRemoved;
import jCompute.Cluster.Controller.Event.StatusChanged;
import jCompute.Cluster.Controller.Mapping.RemoteSimulationMapping;
import jCompute.Cluster.Node.NodeInfo;
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

public class ControlNode
{
	// SL4J Logger
	private static Logger log = LoggerFactory.getLogger(ControlNode.class);
	
	// Dynamic based on total of active nodes max sims
	private int maxSims = 0;
	
	// Value Used for mapping
	private int simulationNum = 0;
	
	// Total of the simulation processed
	private long simulations = 0;
	
	/* Server Listening Socket */
	private ServerSocket listenSocket;
	
	/* Connections Processed */
	private int connectionNumber = 0;
	
	/* Active Nodes indexed by nodeId */
	private LinkedList<NodeManager> activeNodes;
	
	/* Connecting Nodes List */
	private LinkedList<NodeManager> connectingNodes;
	private Timer ncpTimer;
	private int ncpTimerVal = 5;
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
	
	public ControlNode()
	{
		log.info("Starting ControlNode");
		
		localSimulationMap = new HashMap<Integer, RemoteSimulationMapping>();
		
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
				// and add them to the active nodes.
				// Must be a for loop as we dont want to stay in this loop.
				for(NodeManager tNode : connectingNodes)
				{
					if(tNode.isReady())
					{
						activeNodes.add(tNode);
						
						maxSims += tNode.getMaxSims();
						
						log.debug("Node " + tNode.getUid() + " now Active (Max Sims " + maxSims + ")");
						
						// Sort the Node by weighting
						Collections.sort(activeNodes, new NodeManagerComparator());
						
						log.info("------------------------------------");
						log.info("Active (" + activeNodes.size() + ")");
						log.info("------------------------------------");
						for(NodeManager node : activeNodes)
						{
							log.info("Node " + node.getUid() + ": " + node.getWeighting());
							
						}
						log.info("------------------------------------");
						
						JComputeEventBus.post(new NodeAdded(tNode.getNodeConfig()));
					}
					else if(tNode.getReadyStateTimeOutValue() == NCP.ReadyStateTimeOut)
					{
						connectingNodes.remove(tNode);
						tNode.destroy("Ready State Timeout");
					}
					else if(tNode.hasFailedReg())
					{
						connectingNodes.remove(tNode);
						tNode.destroy("failed to register " + tNode.getRegFailedReason());
					}
					else
					{
						tNode.incrementTimeOut();
					}
				}
				
				// Now remove ready nodes in the connecting nodes list
				Iterator<NodeManager> itr = connectingNodes.iterator();
				while(itr.hasNext())
				{
					NodeManager node = itr.next();
					
					if(node.isReady())
					{
						itr.remove();
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
						node.triggerNodeStatRequest(timerCount);
					}
					
				}
				
				if(recoveredSimIds.size() > 0)
				{
					hasRecoverableSimsIds = true;
				}
				
				controlNodeLock.release();
				
				JComputeEventBus.post(new StatusChanged(listenSocket.getInetAddress().getHostAddress(), String.valueOf(listenSocket
						.getLocalPort()), String.valueOf(connectingNodes.size()), String.valueOf(activeNodes.size()), String
						.valueOf(maxSims), String.valueOf(simulations)));
				
				timerCount += ncpTimerVal;
			}
		}, 0, ncpTimerVal * 1000);
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
							
							boolean existingActive = existingActiveNode(nodeSocket);
							boolean existingConnecting = existingConnectingNode(nodeSocket);
							
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
							log.info(e.toString());
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
	
	/* Simulation Manager Logic */
	
	public int addSimulation(String scenarioText, ExportFormat statExportFormat, String fileNameSuffix)
	{
		controlNodeLock.acquireUninterruptibly();
		
		boolean simAdded = false;
		
		// Find a node with a free slot
		log.debug(" Find a node (" + activeNodes.size() + ")");
		for(NodeManager node : activeNodes)
		{
			log.debug("Node " + node.getUid());
			if(node.hasFreeSlot() && node.isRunning())
			{
				log.debug(node.getUid() + " hasFreeSlot ");
				
				/*
				 * Valid mapping values are set at various points int the
				 * sequence
				 */
				
				// remoteId -1 as the remote id is filled in by the NODE and
				// indexed on it
				RemoteSimulationMapping mapping = new RemoteSimulationMapping(node.getUid());
				
				log.info("Add Simulation to Node " + node.getUid());
				
				int remoteSimId = node.addSim(scenarioText, mapping);
				
				// Incase the remote node goes down while in this method
				if(remoteSimId > 0)
				{
					// Increment the simUID values
					simulationNum++;
					
					simulations++;
					
					mapping.setLocalSimId(simulationNum);
					
					// Stat Export format for this sim
					mapping.setExportFormat(statExportFormat);
					
					// suffix for exported stat files
					mapping.setFileNameSuffix(fileNameSuffix);
					
					// Locally cache the mapping
					localSimulationMap.put(simulationNum, mapping);
					
					simAdded = true;
					
					log.info("Added Simulation to Node " + node.getUid() + " Local SimId " + simulationNum + " Remote SimId " + remoteSimId);
					
					JComputeEventBus.post(new SimulationsManagerEvent(simulationNum, SimulationsManagerEventType.AddedSim));
					
					break;
				}
				else
				{
					log.warn("Remote Node " + node.getUid() + " Could not add Simulation - Local SimId(pending) " + (simulationNum + 1)
							+ " Remote SimId " + remoteSimId);
				}
				
			}
			
		}
		
		// Most likely A node has gone down mid method - or other network
		// problem.
		if(!simAdded)
		{
			log.error("Could not add Simulation - no nodes accepted ");
			
			controlNodeLock.release();
			
			return -1;
		}
		else
		{
			controlNodeLock.release();
			
			return simulationNum;
		}
		
	}
	
	public void removeSimulation(int simId)
	{
		controlNodeLock.acquireUninterruptibly();
		
		// Look up and remove the mapping
		RemoteSimulationMapping mapping = localSimulationMap.remove(simId);
		
		log.info("Remove Simulation from Node " + mapping.getNodeUid() + " Local SimId " + simId + " Remote SimId "
				+ mapping.getRemoteSimId());
		
		NodeManager nodeManager = findNodeManagerFromUID(mapping.getNodeUid());
		
		nodeManager.removeSim(mapping.getRemoteSimId());
		
		controlNodeLock.release();
		
		JComputeEventBus.post(new SimulationsManagerEvent(simId, SimulationsManagerEventType.RemovedSim));
		
	}
	
	public void startSim(int simId)
	{
		controlNodeLock.acquireUninterruptibly();
		
		// Look up mapping
		RemoteSimulationMapping mapping = localSimulationMap.get(simId);
		
		NodeManager nodeManager = findNodeManagerFromUID(mapping.getNodeUid());
		
		log.info("Start Simulation on Node " + mapping.getNodeUid() + " Local SimId " + simId + " Remote SimId " + mapping.getRemoteSimId());
		
		nodeManager.startSim(mapping.getRemoteSimId());
		
		controlNodeLock.release();
	}
	
	private NodeManager findNodeManagerFromUID(int uid)
	{
		Iterator<NodeManager> itr = activeNodes.iterator();
		NodeManager temp = null;
		NodeManager nodeManager = null;
		
		while(itr.hasNext())
		{
			temp = itr.next();
			
			if(temp.getUid() == uid)
			{
				nodeManager = temp;
				
				break;
			}
			
		}
		
		return nodeManager;
	}
	
	public int getMaxSims()
	{
		return maxSims;
	}
	
	public NodeInfo[] getNodesInfo()
	{
		ArrayList<NodeInfo> nodeConfigs = new ArrayList<NodeInfo>();
		
		controlNodeLock.acquireUninterruptibly();
		
		for(NodeManager node : activeNodes)
		{
			nodeConfigs.add(node.getNodeConfig());
		}
		
		NodeInfo array[] = nodeConfigs.toArray(new NodeInfo[nodeConfigs.size()]);
		
		controlNodeLock.release();
		
		return array;
	}
	
	public String[] getStatus()
	{
		ArrayList<String> status = new ArrayList<String>();
		
		status.add("Address");
		status.add(listenSocket.getInetAddress().getHostAddress());
		
		status.add("Port");
		status.add(String.valueOf(listenSocket.getLocalPort()));
		
		status.add("");
		status.add("");
		
		status.add("Connecting Nodes");
		status.add(String.valueOf(connectingNodes.size()));
		
		status.add("Active Nodes");
		status.add(String.valueOf(activeNodes.size()));
		
		status.add("");
		status.add("");
		
		status.add("Max Active Sims");
		status.add(String.valueOf(maxSims));
		
		status.add("Added Sims");
		status.add(String.valueOf(simulations));
		
		return status.toArray(new String[status.size()]);
	}
	
}
