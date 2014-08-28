package jCompute.Simulation.SimulationManager.Network.Node;

import jCompute.Simulation.Simulation;
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

public class Node
{
	private static SimulationsManager simsManager = new SimulationsManager(Runtime.getRuntime().availableProcessors());
	// Protect the send socket
	private Semaphore txLock = new Semaphore(1,true);

    public Node(String address)
	{
    	System.out.println("Starting Node");
    	
    	/* Our Configuration */
    	NodeConfiguration nodeConfig = new NodeConfiguration(); 
    	
    	int port = NSMCP.StandardServerPort;    	
    	
    	ProtocolState nodeState = ProtocolState.NEW;
    	
    	
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

							System.out.println("AddSimReq");
	
							AddSimReq req = new AddSimReq(input);
							
							int simId = simsManager.addSimulation(req.getScenarioText(),req.getInitialStepRate());
							
							System.out.println("Added Sim " + simId);
							
							output.write(new AddSimReply(simId).toBytes());
								
							dumpSimlistToConsole();
						
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
					
					// Message to Send
				
					// Send message
					// outputStream.writeBytes("Test");
					
					// Receive Reply
					// inputStream.readLine();
				}
				
			}
		}
		else
		{
			System.out.println("Failed");
		}
		

    }
    
	private static boolean doRegistration(NodeConfiguration nodeConfig,DataInputStream input,DataOutputStream output) throws IOException
	{
		boolean finished = false;
		boolean registered = false;
		
		System.out.println("Attempting Registration");

		// Create a registration request and send it
		byte[] frame = new RegistrationRequest().toBytes();
		output.write(frame);

		// Read the replies
		while(!finished)
		{
			int type = input.readInt();
			
			switch(type)
			{
				case  NSMCP.INVALID:
					System.out.println("Recieved Invalid Frame Type " + type);
					registered = false;
					finished = true;
				break;
				case NSMCP.RegAck : 
					
					System.out.println("Recieved Reg Ack");

					RegistrationReqAck reqAck = new RegistrationReqAck(input);
					
					nodeConfig.setUid(reqAck.getUid());
					
					System.out.println("Recieved UID : " + nodeConfig.getUid());

					// We Ack the Ack					
					output.write(reqAck.toBytes());
					
				break;
				case NSMCP.ConfReq :
					
					System.out.println("Recieved Conf Req");

					// Set our max sims now
					nodeConfig.setMaxSims(simsManager.getMaxSims());
					System.out.println("Max Sims " + nodeConfig.getMaxSims());

					// Create and send the Configuration ack
					frame = new ConfigurationAck(nodeConfig.getMaxSims()).toBytes();					
					output.write(frame);
					
					System.out.println("Sent Conf Ack");

					// Now Registered
					registered = true;
					finished = true;
					
				break;
				
				default :
					System.out.println("Got Garbage");
				break;
				
			}
				
		}

		return registered;
	}
	
	private void sendMessage(byte[] bytes) throws IOException
	{
		txLock.acquireUninterruptibly();
		
		output.write(bytes);
		
		txLock.release();
	}
	
	private static void dumpSimlistToConsole()
	{
		List<Simulation> simList = simsManager.getSimList();
		
		for(Simulation sim : simList)
		{
			System.out.println("Simulation " + sim.getState());
		}
		
		System.out.println("Count : " + simList.size());
		
	}
}
