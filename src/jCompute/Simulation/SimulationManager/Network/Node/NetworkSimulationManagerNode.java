package jCompute.Simulation.SimulationManager.Network.Node;

import jCompute.Simulation.Simulation;
import jCompute.Simulation.SimulationManager.Local.SimulationsManager;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.NSMCP;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.NSMCPFrameParser;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.NSMCP.ProtocolState;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class NetworkSimulationManagerNode
{
	private static SimulationsManager simsManager = new SimulationsManager(Runtime.getRuntime().availableProcessors());

    public static void main(String argv[])
	{
    	System.out.println("Starting Node");
    	
    	/* Our Configuration */
    	NodeConfiguration nodeConfig = new NodeConfiguration(); 
    	
    	int port = NSMCP.StandardServerPort;    	
    	
    	ProtocolState nodeState = ProtocolState.NEW;
    	
    	NSMCPFrameParser frameParser = new NSMCPFrameParser();

    	String address = "127.0.0.1";
    	
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
	    		mainNodeLoop(nodeConfig, frameParser,clientSocket);
	    		
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
	
    private static void mainNodeLoop(NodeConfiguration nodeConfig, NSMCPFrameParser frameParser, Socket clientSocket) throws IOException
    {

		if(!clientSocket.isClosed())
		{
			System.out.println("Connected");
			
			// Output Stream
			DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
			
			// Input Stream
			DataInputStream input = new DataInputStream (clientSocket.getInputStream());
			
			boolean registered = doRegistration(nodeConfig, frameParser,input,output);
			
			System.out.println("Registered    : " + registered);

			if(registered)
			{
				
				while(!clientSocket.isClosed())
				{
					byte[] frame;
					
					int type = input.readInt();
					int len = input.readInt();
					
					switch (type)
					{
						case NSMCP.AddSimReq:

						System.out.println("Got AddSimReq");

						int stepRate = input.readInt();
						
						System.out.println("Step Rate" + stepRate);
						
						System.out.println("Config Length : " + len);
						StringBuffer config = new StringBuffer();
						
						for(int c=0;c<len;c++)
						{
							config.append(input.readChar());
						}
						
						System.out.println(config.toString());
						
						int simId = simsManager.addSimulation(config.toString(),stepRate);
						
						frame = frameParser.createAddSimReply(simId);
						
						System.out.println("Added Sim " + simId);
						
						output.write(frame);
							
						dumpSimlistToConsole();
						
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
    
	private static boolean doRegistration(NodeConfiguration nodeConfig, NSMCPFrameParser frameParser,DataInputStream input,DataOutputStream output) throws IOException
	{
		boolean finished = false;
		boolean registered = false;
		
		System.out.println("Attempting Registration");

		byte[] frame = frameParser.createRegReq();
		output.write(frame);

		while(!finished)
		{

			int type = input.readInt();
			int len = input.readInt();
			
			switch(type)
			{
				case NSMCP.RegAck : 
					
					System.out.println("Recieved Reg Ack");

					nodeConfig.setUid(input.readInt());
					
					System.out.println("Recieved UID : " + nodeConfig.getUid());
					
					// We Ack the ack
					frame = frameParser.createRegAck(nodeConfig.getUid());
					
					output.write(frame);
					
				break;				
				case NSMCP.ConfReq :
					
					System.out.println("Recieved Conf Req");

					nodeConfig.setMaxSims(simsManager.getMaxSims());
					
					frame = frameParser.createConfAck(nodeConfig.getMaxSims());
					
					System.out.println("Sent Conf Ack");
					System.out.println("Max Sims " + nodeConfig.getMaxSims());
					
					output.write(frame);
					
					registered = true;
					finished = true;
				break;
				default :
					System.out.println("Type " + type);
					registered = false;
					finished = true;
				break;
				
			}
				
		}

		return registered;
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
