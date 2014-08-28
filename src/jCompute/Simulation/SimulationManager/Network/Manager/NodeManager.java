package jCompute.Simulation.SimulationManager.Network.Manager;

import jCompute.JComputeEventBus;
import jCompute.Debug.DebugLogger;
import jCompute.Simulation.Simulation;
import jCompute.Simulation.Event.SimulationStateChangedEvent;
import jCompute.Simulation.SimulationManager.Event.SimulationsManagerEvent;
import jCompute.Simulation.SimulationManager.Event.SimulationsManagerEventType;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.NSMCP;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.NSMCP.ProtocolState;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.Node.ConfigurationAck;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.Node.ConfigurationRequest;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.Node.RegistrationReqAck;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.Notification.SimulationStateChanged;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.SimulationManager.AddSimReq;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.SimulationManager.RemoveSimAck;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.SimulationManager.RemoveSimReq;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.SimulationManager.SimulationStatsRequest;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.SimulationManager.StartSimCMD;
import jCompute.Simulation.SimulationManager.Network.Node.NodeConfiguration;
import jCompute.Stats.StatExporter;
import jCompute.Stats.StatExporter.ExportFormat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public class NodeManager
{
	// Locks the node
    private Semaphore nodeLock = new Semaphore(1,false);
	
	// Node configuration
	private NodeConfiguration nodeConfig;
	
	private int activeSims = 0;
	
	// This simulations connected socket
	private final Socket socket;
	
	// Input Stream
	private Thread recieveThread;
	
	// Output Stream
	private DataOutputStream output;
	
	private ProtocolState nodeState;
	
    // Counter for NSMCP state machine
    private int NSMCPReadyTimeOut;
    
    // Is the remote node active. (connection up)
    private boolean active = false;
    
    // Semaphores for methods to wait on
    private Semaphore addSimWait = new Semaphore(0,false);
    private Semaphore remSimWait = new Semaphore(0,false);
    private Semaphore simStatsWait = new Semaphore(0,false);
    
    // Request stats MSG box vars
    private StatExporter statExporter;
    
    // Add Sim MSG box Vars
    private int addSimId = -1;
    
	/* Mapping between Nodes/RemoteSimIds and LocalSimIds - indexed by (REMOTE) simId */
	private ConcurrentHashMap<Integer,RemoteSimulationMapping> remoteSimulationMap;
	
	public NodeManager(int uid,Socket socket) throws IOException
	{
		nodeConfig = new NodeConfiguration();
		
		
		remoteSimulationMap = new ConcurrentHashMap<Integer,RemoteSimulationMapping>(4);
		
		NSMCPReadyTimeOut = 0;
		
		System.out.println("New Node Manager " + uid);
		
		// Internal Connection ID
		nodeConfig.setUid(uid);
		
		// A connected socket
		this.socket = socket;
		
		// Output Stream
		output = new DataOutputStream(socket.getOutputStream());
		
		nodeState = ProtocolState.NEW;
		
		createRecieveThread();
		
	}
	
	private void createRecieveThread()
	{
		// The Receive Thread
		recieveThread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
		        
				try
				{
					DataInputStream input = new DataInputStream (socket.getInputStream());
					
			        int type = -1;
			        
			        active = true;
			        
					while(active)
					{
						// Detect Frame
						type = input.readInt();
						
						switch(type)
						{
							case NSMCP.INVALID :
								// Test Frame or Garbage
								System.out.println("Recieved Invalid Frame");
								
							break;
							case NSMCP.RegReq :
								
								System.out.println("Recieved MNRegReq");
								
									/*
									 * A socket has been connected and we have just received a registration request
									 */
									if(nodeState == ProtocolState.NEW)
									{
										// Create and Send Reg Ack
										byte[] frame = new RegistrationReqAck(nodeConfig.getUid()).toBytes();
										output.write(frame);
										
										System.out.println("Sent MNRegAck UID " + nodeConfig.getUid());
										
										nodeState = ProtocolState.REG;
									}								
								
							break;
							case NSMCP.RegAck :
								
								/*
								 * A socket has been connected,
								 * the remove node has already sent us a reg req
								 * we have sent a reg ack and are awaiting confirmation.
								 * - We get confirmation and request the node configuration.
								 */
								if(nodeState == ProtocolState.REG)
								{
									
									RegistrationReqAck reqAck = new RegistrationReqAck(input);
							
									int ruid = reqAck.getUid();
									
									System.out.println("Recieved MNRegAck UID " + ruid);

									// Check the node is sane (UID should be identical to the one we sent)
									if(nodeConfig.getUid() == ruid)
									{
										System.out.println("Requesting Node Configuration");
										
										byte[] frame = new ConfigurationRequest().toBytes();
										
										output.write(frame);
									
									}
									else
									{
										System.out.println("Recieved BAD MNRegAck UID " + ruid);
										
										nodeState = ProtocolState.END;
									}
									
								}
								
							break;
							case NSMCP.RegNack :
								
								/*
								 * A socket has been connected,
								 * Remote node has decided to cancel the registration
								*/
								if(nodeState == ProtocolState.NEW || nodeState == ProtocolState.REG)
								{
									nodeState = ProtocolState.END;
								}
								
							break;								
							/*
							 * Remove node is about to finish registration.
							 * We are waiting on the node configuration.
							 */
							case NSMCP.ConfAck :
								
								if(nodeState == ProtocolState.REG)
								{
									System.out.println("Recieved Conf Ack");

									ConfigurationAck reqAck = new ConfigurationAck(input);
									
									nodeConfig.setMaxSims(reqAck.getMaxSims());			
									
									System.out.println("Node " + nodeConfig.getUid() + " Max Sims : " + nodeConfig.getMaxSims());
									
									nodeState = ProtocolState.READY;
								}

							break;
							case NSMCP.AddSimReply :
								
								if(nodeState == ProtocolState.READY)
								{
									System.out.println("Recieved Add Sim Reply");
									
									addSimId = input.readInt();
									
									addSimWait.release();
								}
								
							break;
							case NSMCP.SimStateNoti:
								
								if(nodeState == ProtocolState.READY)
								{
									// Create the state object
									SimulationStateChanged stateChanged = new SimulationStateChanged(input);
								
									System.out.println(stateChanged.info());
									
									// find the mapping
									RemoteSimulationMapping mapping = remoteSimulationMap.get(stateChanged.getSimId());
									
									System.out.println("New " + mapping.info());
									
									// Post the event as if from a local simulation
									JComputeEventBus.post(new SimulationStateChangedEvent(mapping.getLocalSimId(),stateChanged.getState(),stateChanged.getRunTime(),stateChanged.getStepCount(),stateChanged.getEndEvent()));
								}
								
							break;
							case NSMCP.SimStats:
								
								if(nodeState == ProtocolState.READY)
								{

									System.out.println("Recieved Sim Stats");

									statExporter.populateFromStream(input);
									
									simStatsWait.release();									
								}
								
							break;
							case NSMCP.RemSimAck:
								if(nodeState == ProtocolState.READY)
								{
									RemoveSimAck removeSimAck = new RemoveSimAck(input);
									
									int simId = removeSimAck.getSimId();

									System.out.println("Recieved RemSimAck : " + simId);
									
									RemoteSimulationMapping mapping = remoteSimulationMap.get(simId);
									
									System.out.println("Remove " + mapping.info());
									
									// Remove the mapping as the remote simulation is gone.
									remoteSimulationMap.remove(simId);
									
									activeSims--;
									
									remSimWait.release();
									
								}
							break;
							default :
								System.out.println("Got Garbage");
							break;
							
						}
					}
					// Exit // Do Node Shutdown
					
				}
				catch (IOException e1)
				{
					System.out.println("Node "  + nodeConfig.getUid() +  " Recieve Thread exited");
					// Exit // Do Node Shutdown
					
					active = false;
					
					nodeState = ProtocolState.END;
					
					// Explicit release of all semaphores
					addSimWait.release();
					remSimWait.release();
					simStatsWait.release();
					
				}
				
			}
		}
		);
		
		// Start Processing
		recieveThread.start();
		
	}
	
	/**
	 * Returns if the node is in the ready state.
	 * @return
	 */
	public boolean isReady()
	{
		if(nodeState == ProtocolState.READY)
		{
			return true;
		}
		
		return false;
	}
	
	public boolean isActive()
	{
		return active;
	}
	
	public void incrementTimeOut()
	{
		NSMCPReadyTimeOut++;
		System.out.println("Node " + nodeConfig.getUid() + " NSMCPReadyTimeOut " +NSMCPReadyTimeOut);
	}
	
	public int getReadyStateTimeOutValue()
	{
		return NSMCPReadyTimeOut;
	}

	public void destroy(String reason)
	{
		   Iterator<Entry<Integer, RemoteSimulationMapping>> itr = remoteSimulationMap.entrySet().iterator();
		   
			while (itr.hasNext())
			{
				int simId = itr.next().getValue().getLocalSimId();
				
				// TODO Recover sims
				JComputeEventBus.post(new SimulationsManagerEvent(simId,SimulationsManagerEventType.RemovedSim));
				
				itr.remove();
			}
			
		try
		{
			System.out.println("Closing Socket for Node " + nodeConfig.getUid() + " " + reason);
			socket.close();
		}
		catch (IOException e)
		{
			System.out.println("Socket already closed");
		}
		
	}
	
	public int getUid()
	{
		return nodeConfig.getUid();
	}

	public int getMaxSims()
	{
		return nodeConfig.getMaxSims();
	}

	public boolean hasFreeSlot()
	{
		
		if(activeSims < nodeConfig.getMaxSims())
		{
			return true;
		}
		
		return false;
	}

	/**
	 * Add a Simulation - Blocking
	 * @param scenarioText
	 * @param initialStepRate
	 * @param mapping
	 * @return
	 */
	public int addSim(String scenarioText,int initialStepRate, RemoteSimulationMapping mapping)
	{
		nodeLock.acquireUninterruptibly();
		
		DebugLogger.output("Node " + nodeConfig.getUid() + " AddSim");
		
		try
		{
			//addSimMsgBoxVarLock.acquireUninterruptibly();
			
			// Shared variable
			addSimId = -1;
			
			// Create and Send add Sim Req
			output.write(new AddSimReq(scenarioText,initialStepRate).toBytes());
		    
		    //addSimMsgBoxVarLock.release();
		    
		    // Wait until we are released (by timer or receive thread)
		    addSimWait.acquireUninterruptibly();

		    //addSimMsgBoxVarLock.acquireUninterruptibly();
		    
		    if(addSimId == -1)
		    {
		    	//addSimMsgBoxVarLock.release();
				nodeLock.release();

		    	return -1;
		    }
		    else
		    {
		    	
		    	mapping.setRemoteSimId(addSimId);
		    	
		    	remoteSimulationMap.put(addSimId, mapping);
		    	
		    	activeSims++;
		    	
		    	//addSimMsgBoxVarLock.release();
				nodeLock.release();

				return addSimId;	
		    }
		    
		}
		catch (IOException e)
		{
			nodeLock.release();
			
			// Connection is gone add sim failed
			DebugLogger.output("Node " + nodeConfig.getUid() + " Error in add Sim");
			
			return -1;
		}

	}

	/**
	 * Removes a simulation.
	 * Blocking.
	 * @param remoteSimId
	 */
	public void removeSim(int remoteSimId)
	{
		nodeLock.acquireUninterruptibly();

		try
		{
			output.write(new RemoveSimReq(remoteSimId).toBytes());
			
			remSimWait.acquireUninterruptibly();
			
		}
		catch (IOException e)
		{
			// Connection is gone...
			DebugLogger.output("Node " + nodeConfig.getUid() + " Error in Start Sim");
		}
		
		nodeLock.release();
	}
	
	public void startSim(int remoteSimId)
	{
		nodeLock.acquireUninterruptibly();

		try
		{
			output.write(new StartSimCMD(remoteSimId).toBytes());
		}
		catch (IOException e)
		{
			// Connection is gone...
			DebugLogger.output("Node " + nodeConfig.getUid() + " Error in Start Sim");

		}
		
		nodeLock.release();
	}
	
	public void exportStats(int remoteSimId, String directory, String fileNameSuffix, ExportFormat format)
	{
		nodeLock.acquireUninterruptibly();

		try
		{			
			// create a new exporter as format could change.
			statExporter = new StatExporter(format,fileNameSuffix);

			// Send the request
			output.write(new SimulationStatsRequest(remoteSimId,format).toBytes());

			simStatsWait.acquireUninterruptibly();
			
			// Got reply now export the stats.
			statExporter.exportAllStatsToDir(directory);
			
		}
		catch (IOException e)
		{
			// Connection is gone...
			DebugLogger.output("Node " + nodeConfig.getUid() + " Error in Start Sim");

		}
		
		nodeLock.release();
	}
	
}
