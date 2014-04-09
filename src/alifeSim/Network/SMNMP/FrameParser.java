package alifeSim.Network.SMNMP;

import java.nio.ByteBuffer;

public class FrameParser
{
	private ByteBuffer buffer;
	private int frameSize;
	
	public FrameParser()
	{
		//buffer = ByteBuffer.allocate(SNCPProtocol.MaxMessageSize);
	}
	
	public void putFrame(byte[] frame) throws SMNPException
	{

        buffer = ByteBuffer.wrap(frame,0,frame.length);
                
        frameSize = buffer.capacity();

        // The first int is the message length field, it cannot be greater than the size of the buffer.
        if(frameSize > Protocol.MaxFrameSize)
        {
        	throw new SMNPException("Frame Size Invalid");
        }
        
        System.out.println("New Frame : " + frameSize);
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
}
