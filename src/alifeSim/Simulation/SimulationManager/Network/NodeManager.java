package alifeSim.Simulation.SimulationManager.Network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class NodeManager
{
	// Internal unique Id for this node
	private int uid;
	
	// This simulations connected socket
	private Socket socket;
	
	// Input Stream
	private DataInputStream input;
	
	// Output Stream
	private DataOutputStream output;
	
	public NodeManager(int uid,Socket socket) throws IOException
	{
		// Internal Connection ID
		this.uid = uid;
		
		// A connected socket
		this.socket = socket;
				
		// Input Stream
		input = new DataInputStream(socket.getInputStream());

		// Output Stream
		output = new DataOutputStream(socket.getOutputStream());
		
		// The Receive Thread
		// receiveThread = new Thread(new RecieveProcessor());
		
		// Protocol Manager
		//this.protocolManager = new SNCPProtocolManager();
		
		// Start Processing
		// receiveThread.start();

	}
	
}
