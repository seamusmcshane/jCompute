package jCompute.Simulation.SimulationManager.Network.Manager;

import jCompute.JComputeEventBus;
import jCompute.Gui.View.View;
import jCompute.Simulation.Simulation;
import jCompute.Simulation.SimulationManager.SimulationsManagerInf;
import jCompute.Simulation.SimulationManager.Event.SimulationsManagerEvent;
import jCompute.Simulation.SimulationManager.Event.SimulationsManagerEventType;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.NSMCP;
import jCompute.Simulation.SimulationState.SimState;
import jCompute.Stats.StatExporter;
import jCompute.Stats.Groups.StatGroupListenerInf;
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
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NetworkSimulationsManager implements SimulationsManagerInf
{
	// SL4J Logger
	private static Logger log = LoggerFactory.getLogger(NetworkSimulationsManager.class);
	
	// Dynamic based on total of active nodes max sims
	private int maxSims = 0;
		
	private int simulationNum = 0;
	
	/* Server Listening Socket */
	private ServerSocket listenSocket; 

	/* Connections Processed */
	private int connectionNumber = 0;
	
	/* Active Nodes indexed by nodeId */ 
	private LinkedList<NodeManager> activeNodes;
	
	/* Connecting Nodes List */
	private LinkedList<NodeManager> connectingNodes;
	private Timer NSCPTimer;
	
	/* List of priority re-scheduled Simulations
	 * (recovered from nodes that disappear)
	 */
	private ArrayList<Integer> recoveredSimIds;
	private boolean hasRecoverableSimsIds = false;
	
	/* Mapping between Nodes/RemoteSimIds and LocalSimIds - indexed by (LOCAL) simId */
	private HashMap<Integer,RemoteSimulationMapping> localSimulationMap;
	
	private Semaphore networkSimulationsManagerLock = new Semaphore(1,false);
	
	public NetworkSimulationsManager()
	{
		log.info("Starting NetworkSimulationsManager");
		
		localSimulationMap = new HashMap<Integer,RemoteSimulationMapping>();
		
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
		NSCPTimer = new Timer("NSCPTimer");
		NSCPTimer.schedule(new TimerTask()
		{
			@Override
			public void run() 
			{
				networkSimulationsManagerLock.acquireUninterruptibly();
				
				log.debug("NSMCPTimer");
				log.debug("------------------------------------");
				log.debug("Connecting ("+connectingNodes.size()+")");
				log.debug("------------------------------------");
				for(NodeManager node : connectingNodes)
				{
					log.debug("Node :" + node.getUid());
				}
				log.debug("------------------------------------");

				
				// Detect nodes that are now ready in the connected nodes list and add them to the active nodes.
				// Must be a for loop as we dont want to stay in this loop.
				for(NodeManager tNode : connectingNodes)
				{
					if(tNode.isReady())
					{
						activeNodes.add(tNode);
						
						maxSims += tNode.getMaxSims();
						
						log.debug("Node " + tNode.getUid() + " now Active (Max Sims " + maxSims + ")" );

						Collections.sort(activeNodes, new NodeManagerComparator());
						log.info("------------------------------------");
						log.info("Active ("+activeNodes.size()+")");
						log.info("------------------------------------");
						for(NodeManager node : activeNodes)
						{
							log.info("Node :" + node.getUid() + " " + node.getWeighting());
							
						}
						log.info("------------------------------------");
						
					}
					else if(tNode.getReadyStateTimeOutValue() == NSMCP.ReadyStateTimeOut)
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
							//activeSims--;
						}
						
						log.debug("Node " + node.getUid() + " no longer Active");
						node.destroy("Node no longer active");
						itr.remove();
						
						maxSims -= node.getMaxSims();
						
					}
					
				}
				
				if(recoveredSimIds.size()>0)
				{
					hasRecoverableSimsIds = true;
				}
				
				networkSimulationsManagerLock.release();
			}
			
		},0,5000);
	}

	@Override
	public boolean hasFreeSlot()
	{	
		networkSimulationsManagerLock.acquireUninterruptibly();
		
		boolean tActive = false;
		
		Iterator<NodeManager> itr = activeNodes.iterator();
		while(itr.hasNext())
		{
			NodeManager node = itr.next();
			
			tActive |= node.hasFreeSlot();
		}
		
		networkSimulationsManagerLock.release();
		
		return tActive;
	}
	
	@Override
	public boolean hasRecoverableSimIds()
	{
		return hasRecoverableSimsIds;
	}
	
	@Override
	public ArrayList<Integer> getRecoverableSimIds()
	{
		networkSimulationsManagerLock.acquireUninterruptibly();
		
		ArrayList<Integer> simIds = new ArrayList<Integer>();
		
		Iterator<Integer> itr = recoveredSimIds.iterator();
		while(itr.hasNext())
		{
			simIds.add(itr.next());
		}
		
		recoveredSimIds = new ArrayList<Integer>();
		
		hasRecoverableSimsIds = false;
		
		networkSimulationsManagerLock.release();
		
		return simIds;
	}
	
	private void createAndStartRecieveThread()
	{
		try
		{
			listenSocket = new ServerSocket();
			
			listenSocket.bind(new InetSocketAddress("0.0.0.0",NSMCP.StandardServerPort));
			
			Thread thread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					
					try
					{
						Thread.sleep(1000);
					}
					catch (InterruptedException e1)
					{
						
					}
					log.info("Listening Address : " + listenSocket.getLocalSocketAddress());
					
					while (listenSocket.isBound())
					{
						log.info("Ready for Connections");
						
						try
						{
							Socket nodeSocket = listenSocket.accept();
							
							nodeSocket.setSendBufferSize(32768);
							
							log.info("New Connection from : " + nodeSocket.getRemoteSocketAddress());	

							// Accept new Connections
							
							networkSimulationsManagerLock.acquireUninterruptibly();

							// Add to NodeManager list of connecting node
							connectingNodes.add(new NodeManager(++connectionNumber,nodeSocket));
							
							log.debug("------------------------------------");
							log.debug("Added ("+connectingNodes.size()+")");
							log.debug("------------------------------------");
							for(NodeManager node : connectingNodes)
							{
								log.debug("Node :" + node.getUid());
							}
							log.debug("------------------------------------");
							
							networkSimulationsManagerLock.release();							
							
						}
						catch (IOException e)
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
	
	@Override
	public int addSimulation(String scenarioText, int initialStepRate)
	{
		networkSimulationsManagerLock.acquireUninterruptibly();
		
		boolean simAdded = false;
		
		// Find a node with a free slot
		log.debug(" Find a node ("+activeNodes.size()+")");
		for(NodeManager node : activeNodes)
		{
			log.debug("Node " + node.getUid());
			if(node.hasFreeSlot())
			{
				log.debug( node.getUid() + " hasFreeSlot ");

				/*
				 * 
				 * Valud mapping values are set at various points int the sequence
				 */
				
				// remoteId -1 as the remote id is filled in by the NODE and indexed on it
				RemoteSimulationMapping mapping = new RemoteSimulationMapping(node.getUid());
				
				log.info("Add Simulation to Node " + node.getUid());
				
				int remoteSimId = node.addSim(scenarioText,initialStepRate,mapping);
				
				// Incase the remote node goes down while in this method
				if(remoteSimId > 0)
				{
					// Increment the simUID values
					simulationNum++;
					
					mapping.setLocalSimId(simulationNum);
					
					// Locally cache the mapping
					localSimulationMap.put(simulationNum,mapping);
					
					simAdded = true;
					
					log.info("Added Simulation to Node " + node.getUid() + " Local SimId " + simulationNum + " Remote SimId " + remoteSimId);			

					// activeSims++;
					
					JComputeEventBus.post(new SimulationsManagerEvent(simulationNum,SimulationsManagerEventType.AddedSim));
					
					break;
				}
				else
				{
					log.warn("Remote Node " + node.getUid() + " Could not add Simulation - Local SimId " + simulationNum + " Remote SimId " + remoteSimId);
				}

			}

		}
		
		// Most likely A node has gone down mid method - or other network problem.
		if(!simAdded)
		{
			log.error("Could not add Simulation - no nodes accepted ");
			
			networkSimulationsManagerLock.release();

			return -1;
		}
		else
		{
			networkSimulationsManagerLock.release();
			
			return simulationNum;
		}
		
	}
	
	@Override
	public void removeSimulation(int simId)
	{
		networkSimulationsManagerLock.acquireUninterruptibly();

		// Look up mapping
		RemoteSimulationMapping mapping = localSimulationMap.get(simId);
		
		log.info("Remove Simulation from Node " + mapping.getNodeUid() + " Local SimId " + simId + " Remote SimId " + mapping.getRemoteSimId());			
		
		NodeManager nodeManager = findNodeManagerFromUID(mapping.getNodeUid());
		
		nodeManager.removeSim(mapping.getRemoteSimId());
		
		// Remove the mapping
		localSimulationMap.remove(mapping);
	
		// activeSims--;	
		
		networkSimulationsManagerLock.release();
		
		JComputeEventBus.post(new SimulationsManagerEvent(simId,SimulationsManagerEventType.RemovedSim));

	}

	@Override
	public void startSim(int simId)
	{
		networkSimulationsManagerLock.acquireUninterruptibly();

		// Look up mapping
		RemoteSimulationMapping mapping = localSimulationMap.get(simId);
		
		NodeManager nodeManager = findNodeManagerFromUID(mapping.getNodeUid());
		
		log.info("Start Simulation on Node " + mapping.getNodeUid() + " Local SimId " + simId + " Remote SimId " + mapping.getRemoteSimId());			
		
		nodeManager.startSim(mapping.getRemoteSimId());
				
		networkSimulationsManagerLock.release();
	}

	@Override
	public StatExporter getStatExporter(int simId, String fileNameSuffix, ExportFormat format)
	{
		networkSimulationsManagerLock.acquireUninterruptibly();

		// Look up mapping
		RemoteSimulationMapping mapping = localSimulationMap.get(simId);
		
		log.info("Exports Stats for Simulation on Node " + mapping.getNodeUid() + " Local SimId " + simId + " Remote SimId " + mapping.getRemoteSimId());			
		
		NodeManager nodeManager = findNodeManagerFromUID(mapping.getNodeUid());
		
		StatExporter exporter = nodeManager.getStatExporter(mapping.getRemoteSimId(), fileNameSuffix, format);
				
		networkSimulationsManagerLock.release();
		
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
	
	@Override
	public void pauseSim(int simId)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public SimState togglePause(int simId)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getScenarioText(int simId)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void unPauseSim(int simId)
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public List<Simulation> getSimList()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Integer> getSimIdList()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getMaxSims()
	{
		return maxSims;
	}

	@Override
	public SimState getState(int simId)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getReqSps(int simId)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isStatGroupGraphingEnabled(int simId, String group)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getStatGroupGraphSampleWindowSize(int simId, String group)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean hasStatGroupTotalStat(int simId, String group)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Set<String> getStatGroupNames(int simId)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addStatGroupListener(int simId, String group, StatGroupListenerInf listener)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeStatGroupListener(int simId, String group, StatGroupListenerInf listener)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setReqSimStepRate(int simId, int stepRate)
	{
		// NA		
	}
	
	@Override
	public void setActiveSim(int simId)
	{
		// NA
	}

	@Override
	public void setSimView(View simView)
	{
		// NA
	}

	@Override
	public void clearActiveSim()
	{
		// NA
	}

	@Override
	public void resetActiveSimCamera()
	{
		// NA
	}

	@Override
	public void removeAll()
	{
		// TODO Auto-generated method stub
	}
	
}
