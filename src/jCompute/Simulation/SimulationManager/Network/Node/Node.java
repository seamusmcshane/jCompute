package jCompute.Simulation.SimulationManager.Network.Node;

import jCompute.JComputeEventBus;
import jCompute.Simulation.Event.SimulationStateChangedEvent;
import jCompute.Simulation.SimulationManager.SimulationsManagerInf;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.NSMCP;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.NSMCP.ProtocolState;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.Node.ConfigurationAck;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.Node.RegistrationReqAck;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.Node.RegistrationRequest;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.Notification.SimulationStateChanged;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.SimulationManager.AddSimReply;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.SimulationManager.AddSimReq;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.SimulationManager.RemoveSimAck;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.SimulationManager.RemoveSimReq;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.SimulationManager.SimulationStatsRequest;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.SimulationManager.StartSimCMD;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.Semaphore;

import com.google.common.eventbus.Subscribe;

public class Node
{
	// Simulation Manager
	private SimulationsManagerInf simsManager;

	// Protect the send socket
	private Semaphore txLock = new Semaphore(1,true);
	
	// To ensure receive frames and events are processed atomically
	private Semaphore rxLockEvents = new Semaphore(1,true);

	// Output Stream
	private DataOutputStream output;
	
	// ProtocolState
	private ProtocolState state = ProtocolState.NEW;
	
    public Node(String address, SimulationsManagerInf simsManager)
	{
    	System.out.println("Starting Node");
    	
    	/* Our Configuration */
    	NodeConfiguration nodeConfig = new NodeConfiguration(); 
    	
    	int port = NSMCP.StandardServerPort;
    	
    	this.simsManager = simsManager;

    	JComputeEventBus.register(this);
    	    	
    	// Disconnect Recovery Loop
    	while(true)
    	{
    		// Connecting to Server
    		Socket clientSocket = null;
    		
    		try
			{
    			// Only attempt to connect every 5 seconds
    			Thread.sleep(5000);
    			
	    		System.out.println("Connecting to : " + address + "@" + port);	    		

				//clientSocket = new Socket(address, port);
				clientSocket = new Socket();
				
				clientSocket.setSendBufferSize(1048576);
				clientSocket.setReceiveBufferSize(32768);
				
				clientSocket.connect(new InetSocketAddress(address,port), 1000);
				
				if(!clientSocket.isClosed())
	    		{
	    			System.out.println("Connected to : " + clientSocket.getRemoteSocketAddress());
	    			System.out.println("We are : " + clientSocket.getLocalSocketAddress());
	    			
		    		// Main
		    		process(nodeConfig, clientSocket);
	    		}
	    		
	    		// Close Connection
    			if(!clientSocket.isClosed())
    			{
    				clientSocket.close();
    			}

			}
			catch (IOException e )
			{
				System.out.println("Connection to " + address + " failed");
			}
			catch (InterruptedException e)
			{
				System.out.println("Sleep interupted");
			}
			
    	}

	}
	
    private void process(NodeConfiguration nodeConfig, Socket clientSocket)
    {

    	try
    	{
			// Link up output
			output = new DataOutputStream(new BufferedOutputStream(clientSocket.getOutputStream()));
			
			// Input Stream
			final DataInputStream input = new DataInputStream(new BufferedInputStream(clientSocket.getInputStream()));
			
			doRegistration(nodeConfig, input);
			
			// if Registered successfully
			if(state == ProtocolState.READY)
			{
				System.out.println("Registration complete");
				
				// While we have a connection
				while(!clientSocket.isClosed())
				{
					// Get the frame type
					int type = input.readInt();
					
					switch (type)
					{
						case NSMCP.AddSimReq:
						{
							rxLockEvents.acquireUninterruptibly();

							System.out.println("AddSimReq");
	
							AddSimReq req = new AddSimReq(input);
							
							int simId = simsManager.addSimulation(req.getScenarioText(),req.getInitialStepRate());
							
							System.out.println("Added Sim " + simId);
							
							sendMessage(new AddSimReply(simId).toBytes());
							
							rxLockEvents.release();
						}
						break;
						case NSMCP.StartSimCMD:
						
							System.out.println("StartSimCMD");

							StartSimCMD cmd = new StartSimCMD(input);

							simsManager.startSim(cmd.getSimid());
							
						break;
						
						// Default / Invalid
						case NSMCP.INVALID:
						case NSMCP.SimStatsReq:
						
							System.out.println("SimStatsReq");

							SimulationStatsRequest statsReq = new SimulationStatsRequest(input);

							System.out.println("NSMCP.SimStats");
							sendMessage(simsManager.getStatsAsBytes(statsReq.getSimId(), statsReq.getFormat()));
							
						break;
						case NSMCP.RemSimReq:
						{
							System.out.println("RemoveSimReq");
							RemoveSimReq removeSimReq = new RemoveSimReq(input);
							
							int simId = removeSimReq.getSimid();
							
							simsManager.removeSimulation(simId);
							
							sendMessage(new RemoveSimAck(simId).toBytes());
						}
						break;
						default:

							System.out.println("Invalid NSMCP Message Recieved");
							
							state = ProtocolState.END;
							
							clientSocket.close();
						break;
					}
					
				}
    				

    			
    		}
    		else
    		{
    			System.out.println("Registration failed");
    		}
    	}
		catch (IOException e)
		{
			
			// Our connection to the remote manager is gone.
			simsManager.removeAll();
			
			System.out.println("Removed all Simulations as connection lost");
		}

    }
    
	private void doRegistration(NodeConfiguration nodeConfig,DataInputStream input) throws IOException
	{
		boolean finished = false;
		
		System.out.println("Attempting Registration");

		state = ProtocolState.REG;
		
		// Create a registration request and send it
		sendMessage(new RegistrationRequest().toBytes());

		// Read the replies
		while(!finished)
		{
			int type = input.readInt();
			
			switch(type)
			{
				case  NSMCP.INVALID:
					System.out.println("Recieved Invalid Frame Type " + type);
					state = ProtocolState.END;
					finished = true;
				break;
				case NSMCP.RegAck : 
					
					System.out.println("Recieved Reg Ack");

					RegistrationReqAck reqAck = new RegistrationReqAck(input);
					
					nodeConfig.setUid(reqAck.getUid());
					
					System.out.println("Recieved UID : " + nodeConfig.getUid());

					// We Ack the Ack					
					sendMessage(reqAck.toBytes());
					
				break;
				case NSMCP.ConfReq :
					
					System.out.println("Recieved Conf Req");

					// Set our max sims now
					nodeConfig.setMaxSims(simsManager.getMaxSims());
					System.out.println("Max Sims " + nodeConfig.getMaxSims());

					// Create and send the Configuration ack
					sendMessage(new ConfigurationAck(nodeConfig.getMaxSims()).toBytes());
					
					System.out.println("Sent Conf Ack");

					state = ProtocolState.READY;
					
					// Now Registered
					finished = true;
					
				break;
				
				default :
					System.out.println("Got Garbage");
					state = ProtocolState.END;
				break;
				
			}
				
		}

	}
	
	private void sendMessage(byte[] bytes) throws IOException
	{
		txLock.acquireUninterruptibly();
		
		output.write(bytes);
		output.flush();

		txLock.release();
	}
	
	@Subscribe
	public void SimulationStateChangedEvent(SimulationStateChangedEvent e)
	{
		try
		{
			rxLockEvents.acquireUninterruptibly();
			
			sendMessage(new SimulationStateChanged(e).toBytes());
			System.out.println("Sent Simulation State Changed " + e.getSimId() + " " + e.getState().toString());
		
			rxLockEvents.release();
		}
		catch (IOException e1)
		{
			System.out.println("Failed Sending Simulation State Changed " + e.getSimId() + " " + e.getState().toString());
		}
	}
}
