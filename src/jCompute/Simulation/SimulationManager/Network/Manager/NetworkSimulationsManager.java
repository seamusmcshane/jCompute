package jCompute.Simulation.SimulationManager.Network.Manager;

import jCompute.Debug.DebugLogger;
import jCompute.Gui.View.GUISimulationView;
import jCompute.Simulation.Simulation;
import jCompute.Simulation.SimulationManager.SimulationsManagerInf;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.NSMCP;
import jCompute.Simulation.SimulationState.SimState;
import jCompute.Stats.StatGroupListenerInf;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
	private ArrayList<NodeManager> activeNodes;
	
	/* Connecting Nodes List */
	private ArrayList<NodeManager> connectingNodes;
	private Timer NSCPTimer;
	
	/* List of priority re-scheduled Simulations
	 * (recovered from nodes that disappear)
	 */
	
	/* Mapping between Nodes/RemoteSimIds and LocalSimIds - indexed by simId */
	private HashMap<Integer,RemoteSimulationMapping> simulationsMap;
	
	private Semaphore networkSimulationsManagerLock = new Semaphore(1,false);
	
	// Nodes/Sockets
	// Simulations
	
	public NetworkSimulationsManager()
	{
		DebugLogger.output("Started NetworkSimulationsManager");
		
		simulationsMap = new HashMap<Integer,RemoteSimulationMapping>();
		
		// List of simulation nodes.
		activeNodes = new ArrayList<NodeManager>();
		connectingNodes = new ArrayList<NodeManager>();
		
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
				
				if(connectingNodes.size() > 0)
				{
					NodeManager node = connectingNodes.get(0);
					
					if(node.isReady())
					{
						connectingNodes.remove(node);
						
						activeNodes.add(node);
						
						maxSims += node.getMaxSims();
						
						System.out.println("Node " + node.getUid() + " now Active (Max Sims " + maxSims + ")" );
					}
					else
					{
						node.incrementTimeOut();
					}
					
					if(node.getReadyStateTimeOutValue() == NSMCP.ReadyStateTimeOut)
					{
						connectingNodes.remove(node);
						node.destroy("Ready State Timeout");
					}
				}
				
				
				Iterator<NodeManager> itr = activeNodes.iterator();
				
				while(itr.hasNext())
				{
					NodeManager node = itr.next();
					
					// System.out.println("Node " + node.getUid() + " Active " + node.isActive());
					
					if(!node.isActive())
					{
						node.destroy("Node not Active");
						itr.remove();
						
						maxSims -= node.getMaxSims();
						
						System.out.println("Node " + node.getUid() + " no longer Active");
					}
				}
				
				networkSimulationsManagerLock.release();
			}
			
		},0,1000);
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
							// Accept new Connections
							Socket nodeSocket = listenSocket.accept();
							
							DebugLogger.output("New Connection from : " + nodeSocket.getRemoteSocketAddress());	
							
							NodeManager tNode = new NodeManager(++connectionNumber,nodeSocket);
							
							networkSimulationsManagerLock.acquireUninterruptibly();
							
							// Add to NodeManager list of connecting node
							connectingNodes.add(tNode);
							
							networkSimulationsManagerLock.release();
							
							System.out.println("Connection " + connectionNumber + " Processed");
							
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
		
		if( activeSims < maxSims)
		{
			DebugLogger.output("activeSims < maxSims");
			
			// Find a node with a free slot
			for(int n=0;n<activeNodes.size();n++)
			{
				DebugLogger.output(" Find a node ");

				NodeManager node = activeNodes.get(n);
				
				if(node.hasFreeSlot())
				{
					DebugLogger.output(" hasFreeSlot ");

					int remoteSimId = node.addSim(scenarioText,initialStepRate);
					
					// Incase the remote node goes down while in this method
					if(remoteSimId > 0)
					{
						
						// Locally cache the mapping
						simulationsMap.put(simulationNum,new RemoteSimulationMapping(simulationNum++,remoteSimId,node.getUid()));
						
						simAdded = true;
						
						DebugLogger.output("Added Simulation to Node " + node.getUid() + " Local SimId " + simulationNum + " Remote SimId " + remoteSimId);			

						activeSims++;
						
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startSim(int simId)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pauseSim(int simId)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public long getSimRunTime(int simId)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getSimStepCount(int simId)
	{
		// TODO Auto-generated method stub
		return 0;
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
	public void setActiveSim(int simId)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSimView(GUISimulationView simView)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clearActiveSim()
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resetActiveSimCamera()
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
		DebugLogger.output("Max Sims " + maxSims);

		return maxSims;
	}

	@Override
	public int getActiveSims()
	{
		DebugLogger.output("Active Sims " + activeSims);

		return activeSims;
	}

	@Override
	public SimState getState(int simId)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getEndEvent(int simId)
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
	public void exportAllStatsToDir(int simId, String directory, String fileNameSuffix, String format)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setReqSimStepRate(int simId, int stepRate)
	{
		// NOT Implemented		
	}
	
}
