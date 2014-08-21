package jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.Node;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import jCompute.Debug.DebugLogger;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.NSMCP;

public class RegistrationReqAck
{
	private int uid;
	// Standard Constructor
	public RegistrationReqAck(int uid)
	{
		this.uid = uid;
	}
	
	// Construct from an input stream
	public RegistrationReqAck(DataInputStream source) throws IOException
	{
		DebugLogger.output("RegistrationReqAck");
		
		uid = source.readInt();
		
		DebugLogger.output("UID : " + uid);
	}
	
	public int getUid()
	{
		return uid;
	}

	public byte[] toBytes()
	{
		ByteBuffer tbuffer = ByteBuffer.allocate(8);  

		// Reg Ack
		tbuffer.putInt(NSMCP.RegAck);
		
		// uid
		tbuffer.putInt(uid);
		
		return tbuffer.array();
	}
}
