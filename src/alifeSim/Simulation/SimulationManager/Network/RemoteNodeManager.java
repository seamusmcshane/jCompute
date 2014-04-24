package alifeSim.Simulation.SimulationManager.Network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import alifeSim.Debug.DebugLogger;
import alifeSim.Simulation.SimulationManager.Network.NSMCProtocol.NSMCP;
import alifeSim.Simulation.SimulationManager.Network.NSMCProtocol.NSMCP.ProtocolState;
import alifeSim.Simulation.SimulationManager.Network.NSMCProtocol.NSMCPFrameParser;
import alifeSim.Simulation.SimulationManager.Network.Node.NodeConfiguration;

public class RemoteNodeManager
{
	// Locks the node
    private Semaphore nodeLock = new Semaphore(1,false);
	
	// Node configuration
	private static NodeConfiguration nodeConfig;
	
	private static int activeSims = 0;
	
	// This simulations connected socket
	private final Socket socket;
	
	// Input Stream
	private Thread recieveThread;
	
	// Output Stream
	private DataOutputStream output;
	
	private ProtocolState nodeState;
	
    private NSMCPFrameParser frameParser;
    
    // Counter for NSMCP state machine
    private int NSMCPReadyTimeOut;
    
    // Is the remote node active. (connection up)
    private boolean active = false;
    
    // Semaphore for methods to wait on
    private Semaphore nodeWait = new Semaphore(0,false);
    
    // Internal Timer for timing out remote replies
    private final int timeoutSecs = 10;
    private Timer nodeTimer;

    // Protect the variables shared between the receive thread/timer and entry methods
    private Semaphore msgBoxVarLock = new Semaphore(1,false);
    
    // Add Sim Vars
    private boolean addingSim = false;    
    private int addSimTick = 0;  
    private int addSimId = -1;
    
    // Create sim vars
    private boolean creatingScenario = false;
    private int createScenarioTick = 0;  
    private boolean createdScenario = false;    

	public RemoteNodeManager(int uid,Socket socket) throws IOException
	{
		nodeConfig = new NodeConfiguration(); 
		frameParser = new NSMCPFrameParser();	
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
		
		nodeTimer = new Timer("Node " + nodeConfig.getUid() + " Timer");
		nodeTimer.schedule(new TimerTask()
		{
			@Override
			public void run() 
			{
				msgBoxVarLock.acquireUninterruptibly();
				
				if(addingSim)
				{
					addSimTick++;
					
					if(addSimTick == timeoutSecs)
					{
						addSimId = -1;	
						nodeWait.release();
					}
				}
				
				if(creatingScenario)
				{
					createScenarioTick++;
					
					if(createScenarioTick == timeoutSecs)
					{
						createdScenario = false;	
						nodeWait.release();
					}
				}
				
				msgBoxVarLock.release();
			}
		},0,1000);
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
					DataInputStream di = new DataInputStream (socket.getInputStream());
					
			        int type = 0;
			        int len = 0;
			        
			        active = true;
			        
					while(active)
					{
						// Detect Frame
						type = di.readInt();
						
						// Detect Len
						len = di.readInt();
						
						switch(type)
						{
							case NSMCP.INVALID :
								// Test Frame or Garbage
								System.out.println("Recieved Invalid Frame");
								
							break;
							case NSMCP.RegReq :
								
								System.out.println("Recieved MNRegReq");
								
									/*
									 * A socket has been connected and we have just received a registration reg
									 */
									if(nodeState == ProtocolState.NEW)
									{
										// Send Reg Ack
										byte[] frame = frameParser.createRegAck(nodeConfig.getUid());
										
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
									int ruid = di.readInt();
									
									if(nodeConfig.getUid() == ruid)
									{
										System.out.println("Recieved MNRegAck UID " + ruid);
										
										System.out.println("Getting Node Configuration");
										
										byte[] frame = frameParser.createConfReq();
										
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

									nodeConfig.setMaxSims(di.readInt());			
									
									System.out.println("Node " + nodeConfig.getUid() + " Max Sims : " + nodeConfig.getMaxSims());
									
									nodeState = ProtocolState.READY;
								}

							break;
							case NSMCP.AddSimReply :
								
								if(nodeState == ProtocolState.READY)
								{
									System.out.println("Recieved Add Sim Reply");

									msgBoxVarLock.acquireUninterruptibly();
									
									addSimId = di.readInt();
									
									nodeWait.release();
									
									msgBoxVarLock.release();

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

	public int addSim()
	{
		nodeLock.acquireUninterruptibly();
		
		DebugLogger.output("Node " + nodeConfig.getUid() + " AddSim");
		
		try
		{
			msgBoxVarLock.acquireUninterruptibly();
			
			// Shared variable
			addSimId = -1;
			
			// Send add sim frame
			byte[] frame = frameParser.createAddSimReq();
			output.write(frame);
			
			// Start timer
		    addSimTick = 0; 
		    addingSim = true;
		    
		    msgBoxVarLock.release();
		    
		    // Wait until we are released (by timer or receive thread)
		    nodeWait.acquireUninterruptibly();
			
		    msgBoxVarLock.acquireUninterruptibly();
		    
		    if(addSimId == -1)
		    {
		    	msgBoxVarLock.release();
				nodeLock.release();

		    	return -1;
		    }
		    else
		    {
		    	activeSims++;
		    	
		    	msgBoxVarLock.release();
				nodeLock.release();

				return addSimId;	
		    }
		    
		}
		catch (IOException e)
		{
			nodeLock.release();
			
			// Connection is gone add sim failed
			return -1;
		}

	}

	public boolean createSimScenario(int remoteSimId, String scenarioText)
	{
		nodeLock.acquireUninterruptibly();
		
		DebugLogger.output("Node " + nodeConfig.getUid() + " Create Sim Scenario SimId " + remoteSimId);
		
		try
		{
			msgBoxVarLock.acquireUninterruptibly();
			
			// Shared variable
			createdScenario = false;
			
			// Send add sim frame
			byte[] frame = frameParser.createAddSimReq();
			output.write(frame);
			
			// Start timer
		    createScenarioTick = 0; 
		    creatingScenario = true;
		    
		    msgBoxVarLock.release();
		    
		    // Wait until we are released (by timer or receive thread)
		    nodeWait.acquireUninterruptibly();
			
		    //msgBoxVarLock.acquireUninterruptibly();
		    //msgBoxVarLock.release();
			nodeLock.release();
			
			return createdScenario;		    
		}
		catch (IOException e)
		{
			nodeLock.release();

			return false;
		}
		
	}
	
}
