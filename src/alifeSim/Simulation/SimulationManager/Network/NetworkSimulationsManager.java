package alifeSim.Simulation.SimulationManager.Network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Set;

import alifeSim.Debug.DebugLogger;
import alifeSim.Gui.View.GUISimulationView;
import alifeSim.Simulation.Simulation;
import alifeSim.Simulation.SimulationStatListenerInf;
import alifeSim.Simulation.SimulationManager.SimulationsManagerInf;
import alifeSim.Simulation.SimulationManager.Local.SimulationsManagerEventListenerInf;
import alifeSim.Simulation.SimulationManager.Network.NSMCProtocol.NSMCP;
import alifeSim.Simulation.SimulationState.SimState;
import alifeSim.Simulation.SimulationStateListenerInf;
import alifeSim.Stats.StatGroupListenerInf;


public class NetworkSimulationsManager implements SimulationsManagerInf
{
	/* Server Listening Socket */
	private ServerSocket listenSocket; 
	
	/* Active Connections */ 
	private int activeConnections = 0;
	
	/* Connections Processed */
	private int connectionNumber = 0;
	
	/* Mapping between nodes and Simulations */
	// Nodes
	// Simulations
	
	public NetworkSimulationsManager()
	{
		DebugLogger.output("Started NetworkSimulationsManager");
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

						// Accept new Connections
						Socket nodeSocket;
						
						try
						{
							nodeSocket = listenSocket.accept();
							
							DebugLogger.output("New Connection from : " + nodeSocket.getRemoteSocketAddress());

							// Handle Connection
							// ConnectionHandler connection = new ConnectionHandler(connectionNumber,connectionSocket);
							
							connectionNumber++;

						}
						catch (IOException e)
						{
							
							// TODO - rebind server socket
							DebugLogger.output("Server socket Closed");
						}
						
					}
					
				}
				
			});
			
			thread.start();
		}
		catch(Exception e)
		{
			DebugLogger.output("Server Exited : " + e.getMessage()); 
		}
		
	}

	/* Simulation Manager */
	
	@Override
	public int addSimulation()
	{
		// TODO Auto-generated method stub
		return 0;
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
	public void setReqSimStepRate(int simId, int stepRate)
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
	public boolean createSimScenario(int simId, String scenarioText)
	{
		// TODO Auto-generated method stub
		return false;
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
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getActiveSims()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void addSimulationManagerListener(SimulationsManagerEventListenerInf listener)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeSimulationManagerListener(SimulationsManagerEventListenerInf listener)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addSimulationStateListener(int simId, SimulationStateListenerInf listener)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeSimulationStateListener(int simId, SimulationStateListenerInf listener)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addSimulationStatListener(int simId, SimulationStatListenerInf listener)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeSimulationStatListener(int simId, SimulationStatListenerInf listener)
	{
		// TODO Auto-generated method stub
		
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
	
}
