package alifeSim.Simulation.SimulationManager.Network.Node;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.List;
import alifeSim.Simulation.Simulation;
import alifeSim.Simulation.SimulationManager.Local.SimulationsManager;
import alifeSim.Simulation.SimulationManager.Network.NSMCProtocol.NSMCP;

public class NetworkSimulationManagerNode
{
	final static int port = NSMCP.StandardServerPort;
	
	final static SimulationsManager simsManager = new SimulationsManager(Runtime.getRuntime().availableProcessors());
	
	public static void main(String argv[]) throws Exception
	{
		// Connect to Server
		Socket clientSocket = new Socket("localhost", port);
		
		// Output Stream
		DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
		
		// Input Stream
		BufferedReader inputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		
		String sendMessage;
		String recievedMessage;
		
		while(!clientSocket.isClosed())
		{
			// Message to Send
		
			// Send message
			// outputStream.writeBytes("Test");
			
			// Receive Reply
			// inputStream.readLine();
		}
		
		// Close Connection
		clientSocket.close();

		
	}
	
	private static void addSimulation()
	{
		
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
