package alifeSim.Network.Node;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import alifeSim.Network.SMNMP.FrameParser;
import alifeSim.Network.SMNMP.Protocol;
import alifeSim.Simulation.SimulationManager.SimulationsManager;

public class NetworkNode
{
	final static int port = Protocol.StandardControlPort;
	
	final static SimulationsManager simsManager = new SimulationsManager(Runtime.getRuntime().availableProcessors());
	
	public static void main(String args[]) throws Exception
	{		
        DatagramSocket socket = new DatagramSocket(port);
        DatagramPacket packet = null;
        
        FrameParser fm = new FrameParser(); 
		
        /* RX Message Buffer */
        byte recieveBuffer[] = new byte[Protocol.MaxFrameSize];
        
        while (true) // Never Quit
        {
            try
            {
                /* Received Frame */
                packet = new DatagramPacket(recieveBuffer, Protocol.MaxFrameSize);                                           
                socket.receive(packet);
                
                // Src address and port
                InetAddress address = packet.getAddress();                
                int port = packet.getPort();               
                
                /* Insert frame into parser */
                fm.putFrame(recieveBuffer);
                
                fm.HexDumpBuffer();
                
				// Determine Operation
				
				// Perform Operation
				
				// Send Appropriate Reply
        
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
	}
	
}
