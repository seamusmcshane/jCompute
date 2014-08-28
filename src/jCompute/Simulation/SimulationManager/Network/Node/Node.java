package jCompute.Simulation.SimulationManager.Network.Node;

import jCompute.JComputeEventBus;
import jCompute.Simulation.Simulation;
import jCompute.Simulation.Event.SimulationStateChangedEvent;
import jCompute.Simulation.SimulationManager.SimulationsManagerInf;
import jCompute.Simulation.SimulationManager.Local.SimulationsManager;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.NSMCP;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.NSMCP.ProtocolState;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.Node.ConfigurationAck;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.Node.RegistrationReqAck;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.Node.RegistrationRequest;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.SimulationManager.AddSimReply;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.SimulationManager.AddSimReq;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.SimulationManager.StartSimCMD;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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

    public Node(String address)
	
	// ProtocolState
	private ProtocolState state = ProtocolState.NEW;
	
    public Node(String address, SimulationsManagerInf simsManager)
	{
    	System.out.println("Starting Node");
    	
    	/* Our Configuration */
    	NodeConfiguration nodeConfig = new NodeConfiguration(); 
    	
    	int port = NSMCP.StandardServerPort;    	
    	
    	
    	JComputeEventBus.register(this);
    	// Disconnect Recovery Loop
    	while(true)
    	{
    		// Connecting to Server
    		Socket clientSocket = null;
    		
			try
			{
				clientSocket = new Socket(address, port);
				
	    		System.out.println("Connecting to : " + address + "@" + port);
	    		
	    		// Main
	    		mainNodeLoop(nodeConfig, clientSocket);
		    		process(nodeConfig, clientSocket);
	    		
	    		// Close Connection
    			if(!clientSocket.isClosed())
    			{
    				clientSocket.close();
    			}

			}
			catch (IOException e)
			{
				System.out.println("No Connection");
			}
			
    	}

	}
	
    private void process(NodeConfiguration nodeConfig, Socket clientSocket)
    {

		if(!clientSocket.isClosed())
		{
			System.out.println("Connected");
			
			// Output Stream
			DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
			
			// Input Stream
			DataInputStream input = new DataInputStream (clientSocket.getInputStream());
			
			boolean registered = doRegistration(nodeConfig, input,output);
			
			System.out.println("Registered    : " + registered);

			if(registered)
			{
				
				while(!clientSocket.isClosed())
				{					
					int type = input.readInt();
					
					switch (type)
					{
						case NSMCP.AddSimReq:
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
						
						case NSMCP.INVALID:
							
						default:
							
						break;
					}
					
				}
				
			}
		}
		else
		{
			System.out.println("Failed");
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
