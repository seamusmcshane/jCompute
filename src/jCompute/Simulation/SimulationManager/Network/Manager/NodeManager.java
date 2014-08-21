package jCompute.Simulation.SimulationManager.Network.Manager;

import jCompute.Debug.DebugLogger;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.NSMCP;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.NSMCP.ProtocolState;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.Node.ConfigurationAck;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.Node.ConfigurationRequest;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.Node.RegistrationReqAck;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.SimulationManager.AddSimReq;
import jCompute.Simulation.SimulationManager.Network.Node.NodeConfiguration;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

public class NodeManager
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
    
	public NodeManager(int uid,Socket socket) throws IOException
	{
		nodeConfig = new NodeConfiguration(); 

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

									msgBoxVarLock.acquireUninterruptibly();
									
									addSimId = input.readInt();
									
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

	public int addSim(String scenarioText,int initialStepRate)
	{
		nodeLock.acquireUninterruptibly();
		
		DebugLogger.output("Node " + nodeConfig.getUid() + " AddSim");
		
		try
		{
			msgBoxVarLock.acquireUninterruptibly();
			
			// Shared variable
			addSimId = -1;
			
			// Create and Send add Sim Req
			output.write(new AddSimReq(scenarioText,initialStepRate).toBytes());
			
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
	
}
