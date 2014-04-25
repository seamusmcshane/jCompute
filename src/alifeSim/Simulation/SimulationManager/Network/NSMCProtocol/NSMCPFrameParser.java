package alifeSim.Simulation.SimulationManager.Network.NSMCProtocol;

import java.nio.ByteBuffer;

public class NSMCPFrameParser
{	
	private ByteBuffer buffer;
	private int frameSize;
	
	public NSMCPFrameParser()
	{
		//buffer = ByteBuffer.allocate(SNCPProtocol.MaxMessageSize);
	}
	
	public void putRecivedFrame(byte[] frame) throws NSMCPException
	{
		if(frame!=null)
		{
			frameSize = frame.length;
			
			if(frameSize > NSMCP.MaxFrameSize)
	        {
	        	throw new NSMCPException("Frame Size Invalid");
	        }
	        
	        buffer = ByteBuffer.wrap(frame,0,frame.length);

	        
	        System.out.println("New Frame : " + frameSize);
		}
		else
		{
			System.out.println("Null Frame in Put");
		}
        
	}
	
	public int getFrameSize()
	{
		return buffer.capacity();
	}
	
	 /*
     * Returns the type of frame, always at position 0.
     * [Used by Server or Client]
     */
    public int getFrameType()
    {
        return buffer.getInt();
    }
	
    /*
     * Dumps the Entire Buffer Contents to the Console
     * [Debug Tool] */
    public void HexDumpBuffer()
    {
        int len = frameSize;
        byte[] buff = new byte[len];        
        
        buffer.rewind();
        
        buffer.get(buff, 0, len);
        
        for (int i = 0; i < len; i++)
        {
            if (i % 2 == 0)
            {
                System.out.print(" ");
            }
            
            if (i % 16 == 0)
            {
                System.out.println();
            }
            System.out.printf("%02x", buff[i]);
        }
        System.out.print("\n");      
    }

    /* Registration Request */
	public byte[] createRegReq()
	{		
		return simpleTypeFrame(NSMCP.RegReq);
	}
	
    /* Node Configuration Request */
	public byte[] createConfReq()
	{		
		return simpleTypeFrame(NSMCP.ConfReq);
	}
	
	/*
	 * Generic method for frames that only have a type
	 */
	private byte[] simpleTypeFrame(int type)
	{
		ByteBuffer tbuffer = ByteBuffer.allocate(8);  
		
		// Reg Req
		tbuffer.putInt(type);
		
		// Nothing follows
		tbuffer.putInt(0);
		
		return tbuffer.array();
	}
	
    /* Node Configuration Request */
	public byte[] createAddSimReq(String scenarioText, int initialStepRate)
	{	
		int slen = scenarioText.length();
		
		// Unicode 16 -2bytes chart
		ByteBuffer tbuffer = ByteBuffer.allocate((slen*2)+8); 
		
		// AddSim Req
		tbuffer.putInt(NSMCP.AddSimReq);
		
		// Intial Step Rate
		tbuffer.putInt(initialStepRate);
		
		// Config follows (len is chars)
		tbuffer.putInt(slen);
		
		for(int c=0;c<slen;c++)
		{
			tbuffer.putChar(scenarioText.charAt(c));
		}
		
		System.out.println("slen " + slen);
		
		return tbuffer.array();
	}
	
	public byte[] createAddSimReply(int simId)
	{
		ByteBuffer tbuffer = ByteBuffer.allocate(12);  
		
		// Reg Req
		tbuffer.putInt(NSMCP.AddSimReply);
		
		// simId follows
		tbuffer.putInt(4);
		
		tbuffer.putInt(simId);
		
		return tbuffer.array();
	}
	
	public byte[] createRegAck(int uid)
	{
		ByteBuffer tbuffer = ByteBuffer.allocate(12);  

		// Reg Ack
		tbuffer.putInt(NSMCP.RegAck);
		
		// uid follows
		tbuffer.putInt(4);		
		
		// uid
		tbuffer.putInt(uid);
		
		return tbuffer.array();
	}

	public byte[] createConfAck(int maxSims)
	{
		ByteBuffer tbuffer = ByteBuffer.allocate(12);  

		// Conf Ack
		tbuffer.putInt(NSMCP.ConfAck);
		
		// maxSims follows
		tbuffer.putInt(4);		
		
		// maxSims
		tbuffer.putInt(maxSims);
		
		return tbuffer.array();
	}
}
