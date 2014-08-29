package jCompute.Simulation.SimulationManager.Network.Manager;

import jCompute.JComputeEventBus;
import jCompute.Debug.DebugLogger;
import jCompute.Gui.View.GUISimulationView;
import jCompute.Simulation.Simulation;
import jCompute.Simulation.SimulationManager.SimulationsManagerInf;
import jCompute.Simulation.SimulationManager.Event.SimulationsManagerEvent;
import jCompute.Simulation.SimulationManager.Event.SimulationsManagerEventType;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.NSMCP;
import jCompute.Simulation.SimulationState.SimState;
import jCompute.Stats.StatExporter.ExportFormat;
import jCompute.Stats.StatGroupListenerInf;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;


public class NetworkSimulationsManager implements SimulationsManagerInf
{
	// Dynamic based on total of active nodes max sims
	private int maxSims = 0;
	private int activeSims = 0;
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
	
	// Nodes/Sockets
	// Simulations
	
	public NetworkSimulationsManager()
	{
		DebugLogger.output("Started NetworkSimulationsManager");
		
		localSimulationMap = new HashMap<Integer,RemoteSimulationMapping>();
		
		recoveredSimIds = new ArrayList<Integer>();
		
		// List of simulation nodes.
		activeNodes = new LinkedList<NodeManager>();
		connectingNodes = new LinkedList<NodeManager>();
		
		createAndStartRecieveThread();
		
		startNSMCPTimer();		

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
				
				System.out.println("NSMCPTimer");
				System.out.println("------------------------------------");
				System.out.println("Connecting ("+connectingNodes.size()+")");
				System.out.println("------------------------------------");
				for(NodeManager node : connectingNodes)
				{
					System.out.println("Node :" + node.getUid());
				}
				System.out.println("------------------------------------");

				if(connectingNodes.size() > 0)
				{
					NodeManager tNode = connectingNodes.getFirst();
					
					if(tNode.isReady())
					{
						connectingNodes.remove(tNode);
						
						activeNodes.add(tNode);
						
						maxSims += tNode.getMaxSims();
						
						System.out.println("Node " + tNode.getUid() + " now Active (Max Sims " + maxSims + ")" );
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
				
				System.out.println("------------------------------------");
				System.out.println("Active ("+activeNodes.size()+")");
				System.out.println("------------------------------------");
				for(NodeManager node : activeNodes)
				{
					System.out.println("Node :" + node.getUid());
				}
				System.out.println("------------------------------------");

				Iterator<NodeManager> itr = activeNodes.iterator();
				
				while(itr.hasNext())
				{
					NodeManager node = itr.next();
					
					// System.out.println("Node " + node.getUid() + " Active " + node.isActive());
					
					if(!node.isActive())
					{
						
						ArrayList<Integer> nodeRecoveredSimIds = node.getRecoverableSimsIds();
						
						Iterator<Integer> nRSIdsIter = nodeRecoveredSimIds.iterator();
						while(nRSIdsIter.hasNext())
						{
							recoveredSimIds.add(nRSIdsIter.next());
							activeSims--;
						}
						
						node.destroy("Node not Active");
						itr.remove();
						
						maxSims -= node.getMaxSims();
						
						System.out.println("Node " + node.getUid() + " no longer Active");
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
			listenSocket = new ServerSocket(NSMCP.StandardServerPort);

			Thread thread = new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					DebugLogger.output("Listening Address : " + listenSocket.getLocalSocketAddress());
					
					while (listenSocket.isBound())
					{
						System.out.println("Listening for Connection");

						try
						{
							Socket nodeSocket = listenSocket.accept();
							DebugLogger.output("New Connection from : " + nodeSocket.getRemoteSocketAddress());	

							// Accept new Connections
							
							networkSimulationsManagerLock.acquireUninterruptibly();

							// Add to NodeManager list of connecting node
							connectingNodes.add(new NodeManager(++connectionNumber,nodeSocket));
							
							System.out.println("------------------------------------");
							System.out.println("Added ("+connectingNodes.size()+")");
							System.out.println("------------------------------------");
							for(NodeManager node : connectingNodes)
							{
								System.out.println("Node :" + node.getUid());
							}
							System.out.println("------------------------------------");
							
							networkSimulationsManagerLock.release();							
							
						}
						catch (IOException e)
						{
							// TODO - rebind server socket?
							DebugLogger.output("Server socket Closed");
						}
						
					}
					
				}
				
			});
			
			thread.start();
		}
		catch(Exception e)
		{
			DebugLogger.output("Server Recieve Thread Exited : " + e.getMessage()); 
		}
	}
	
	/* Simulation Manager Logic */
	
	@Override
	public int addSimulation(String scenarioText, int initialStepRate)
	{		
		networkSimulationsManagerLock.acquireUninterruptibly();

		DebugLogger.output("Add Sim");
		
		boolean simAdded = false;
		
		DebugLogger.output("activeSims < maxSims");
		if( activeSims < maxSims)
		{
			
			// Find a node with a free slot
			DebugLogger.output(" Find a node ("+activeNodes.size()+")");
			for(NodeManager node : activeNodes)
			{
				DebugLogger.output("Node " + node.getUid());
				if(node.hasFreeSlot())
				{
					DebugLogger.output( node.getUid() + " hasFreeSlot ");

					/*
					 * 
					 * Valud mapping values are set at various points int the sequence
					 */
					
					// remoteId -1 as the remote id is filled in by the NODE and indexed on it
					RemoteSimulationMapping mapping = new RemoteSimulationMapping(node.getUid());
					
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
						
						DebugLogger.output("Added Simulation to Node " + node.getUid() + " Local SimId " + simulationNum + " Remote SimId " + remoteSimId);			

						activeSims++;
						
						JComputeEventBus.post(new SimulationsManagerEvent(simulationNum,SimulationsManagerEventType.AddedSim));
						
						break;
					}
					else
					{
						DebugLogger.output("Remote Node " + node.getUid() + " Could not add Simulation - Local SimId " + simulationNum + " Remote SimId " + remoteSimId);
					}

				}

			}
			
			// Most likely A node has gone down mid method - or other network problem.
			if(!simAdded)
			{
				DebugLogger.output("Could not add Simulation - no nodes accepted ");
				
				networkSimulationsManagerLock.release();

				return -1;
			}
			else
			{
				networkSimulationsManagerLock.release();
				
				return simulationNum;
			}

		}
		else
		{
			DebugLogger.output("Max Simulations Reached");

			networkSimulationsManagerLock.release();

			return -1;
		}
		
	}
	
	@Override
	public void removeSimulation(int simId)
	{
		networkSimulationsManagerLock.acquireUninterruptibly();

		// Look up mapping
		RemoteSimulationMapping mapping = localSimulationMap.get(simId);
		
		NodeManager nodeManager = findNodeManagerFromUID(mapping.getNodeUid());
		
		nodeManager.removeSim(mapping.getRemoteSimId());
		
		// Remove the mapping
		localSimulationMap.remove(mapping);
	
		activeSims--;
		
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
		
		nodeManager.startSim(mapping.getRemoteSimId());
		
		networkSimulationsManagerLock.release();
	}

	@Override
	public void exportAllStatsToDir(int simId, String directory, String fileNameSuffix, ExportFormat format)
	{
		networkSimulationsManagerLock.acquireUninterruptibly();

		// Look up mapping
		RemoteSimulationMapping mapping = localSimulationMap.get(simId);
		
		NodeManager nodeManager = findNodeManagerFromUID(mapping.getNodeUid());
		
		nodeManager.exportStats(mapping.getRemoteSimId(), directory, fileNameSuffix, format);
		
		networkSimulationsManagerLock.release();
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
		// DebugLogger.output("Max Sims " + maxSims);

		return maxSims;
	}

	@Override
	public int getActiveSims()
	{
		//DebugLogger.output("Active Sims " + activeSims);

		return activeSims;
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
	public void setSimView(GUISimulationView simView)
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
	public byte[] getStatsAsBytes(int simId, ExportFormat format) throws IOException
	{
		return null;
	}

	@Override
	public void removeAll()
	{
		// TODO Auto-generated method stub
	}
	
}
