package jCompute.Cluster.Controller;

import jCompute.JComputeEventBus;
import jCompute.Cluster.Controller.Mapping.RemoteSimulationMapping;
import jCompute.Cluster.Node.NodeConfiguration;
import jCompute.Cluster.Protocol.NCP;
import jCompute.SimulationManager.Event.SimulationsManagerEvent;
import jCompute.SimulationManager.Event.SimulationsManagerEventType;
import jCompute.Stats.StatExporter;
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

	public ControlNode()
	{
		log.info("Starting ControlNode");

		localSimulationMap = new HashMap<Integer, RemoteSimulationMapping>();

		recoveredSimIds = new ArrayList<Integer>();

		// List of simulation nodes.
		activeNodes = new LinkedList<NodeManager>();
		connectingNodes = new LinkedList<NodeManager>();

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

						Collections.sort(activeNodes, new NodeManagerComparator());
						log.info("------------------------------------");
						log.info("Active (" + activeNodes.size() + ")");
						log.info("------------------------------------");
						for(NodeManager node : activeNodes)
						{
							log.info("Node " + node.getUid() + ": " + node.getWeighting());

						}
						log.info("------------------------------------");

					}
					else if(tNode.getReadyStateTimeOutValue() == NCP.ReadyStateTimeOut)
					{
						connectingNodes.remove(tNode);
						tNode.destroy("Ready State Timeout");
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

					if(!node.isActive())
					{

						ArrayList<Integer> nodeRecoveredSimIds = node.getRecoverableSimsIds();

						Iterator<Integer> nRSIdsIter = nodeRecoveredSimIds.iterator();
						while(nRSIdsIter.hasNext())
						{
							recoveredSimIds.add(nRSIdsIter.next());
						}

						log.debug("Node " + node.getUid() + " no longer Active");
						node.destroy("Node no longer active");
						itr.remove();

						maxSims -= node.getMaxSims();

					}

				}

				if(recoveredSimIds.size() > 0)
				{
					hasRecoverableSimsIds = true;
				}

				controlNodeLock.release();
			}

		}, 0, 5000);
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

					try
					{
						Thread.sleep(1000);
					}
					catch(InterruptedException e1)
					{

					}
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

							// Add to NodeManager list of connecting node
							connectingNodes.add(new NodeManager(++connectionNumber, nodeSocket));

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
							log.error("Server socket Closed");
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
		}
	}

	/* Simulation Manager Logic */

	public int addSimulation(String scenarioText, int initialStepRate)
	{
		controlNodeLock.acquireUninterruptibly();

		boolean simAdded = false;

		// Find a node with a free slot
		log.debug(" Find a node (" + activeNodes.size() + ")");
		for(NodeManager node : activeNodes)
		{
			log.debug("Node " + node.getUid());
			if(node.hasFreeSlot())
			{
				log.debug(node.getUid() + " hasFreeSlot ");

				/*
				 * 
				 * Valud mapping values are set at various points int the
				 * sequence
				 */

				// remoteId -1 as the remote id is filled in by the NODE and
				// indexed on it
				RemoteSimulationMapping mapping = new RemoteSimulationMapping(node.getUid());

				log.info("Add Simulation to Node " + node.getUid());

				int remoteSimId = node.addSim(scenarioText, initialStepRate, mapping);

				// Incase the remote node goes down while in this method
				if(remoteSimId > 0)
				{
					// Increment the simUID values
					simulationNum++;

					simulations++;

					mapping.setLocalSimId(simulationNum);

					// Locally cache the mapping
					localSimulationMap.put(simulationNum, mapping);

					simAdded = true;

					log.info("Added Simulation to Node " + node.getUid() + " Local SimId " + simulationNum
							+ " Remote SimId " + remoteSimId);

					JComputeEventBus.post(new SimulationsManagerEvent(simulationNum,
							SimulationsManagerEventType.AddedSim));

					break;
				}
				else
				{
					log.warn("Remote Node " + node.getUid() + " Could not add Simulation - Local SimId "
							+ simulationNum + " Remote SimId " + remoteSimId);
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

		// Look up mapping
		RemoteSimulationMapping mapping = localSimulationMap.get(simId);

		log.info("Remove Simulation from Node " + mapping.getNodeUid() + " Local SimId " + simId + " Remote SimId "
				+ mapping.getRemoteSimId());

		NodeManager nodeManager = findNodeManagerFromUID(mapping.getNodeUid());

		nodeManager.removeSim(mapping.getRemoteSimId());

		// Remove the mapping
		localSimulationMap.remove(mapping);

		controlNodeLock.release();

		JComputeEventBus.post(new SimulationsManagerEvent(simId, SimulationsManagerEventType.RemovedSim));

	}

	public void startSim(int simId)
	{
		controlNodeLock.acquireUninterruptibly();

		// Look up mapping
		RemoteSimulationMapping mapping = localSimulationMap.get(simId);

		NodeManager nodeManager = findNodeManagerFromUID(mapping.getNodeUid());

		log.info("Start Simulation on Node " + mapping.getNodeUid() + " Local SimId " + simId + " Remote SimId "
				+ mapping.getRemoteSimId());

		nodeManager.startSim(mapping.getRemoteSimId());

		controlNodeLock.release();
	}

	public StatExporter getStatExporter(int simId, String fileNameSuffix, ExportFormat format)
	{
		controlNodeLock.acquireUninterruptibly();

		// Look up mapping
		RemoteSimulationMapping mapping = localSimulationMap.get(simId);

		log.info("Exports Stats for Simulation on Node " + mapping.getNodeUid() + " Local SimId " + simId
				+ " Remote SimId " + mapping.getRemoteSimId());

		NodeManager nodeManager = findNodeManagerFromUID(mapping.getNodeUid());

		controlNodeLock.release();

		// Here so we don't block during transfer
		StatExporter exporter = nodeManager.getStatExporter(mapping.getRemoteSimId(), fileNameSuffix, format);

		return exporter;
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

	public NodeConfiguration[] getNodesInfo()
	{
		ArrayList<NodeConfiguration> nodeConfigs = new ArrayList<NodeConfiguration>();

		controlNodeLock.acquireUninterruptibly();

		for(NodeManager node : activeNodes)
		{
			nodeConfigs.add(node.getNodeConfig());
		}

		NodeConfiguration array[] = nodeConfigs.toArray(new NodeConfiguration[nodeConfigs.size()]);

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
