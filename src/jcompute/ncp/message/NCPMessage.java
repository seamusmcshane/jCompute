package jcompute.ncp.message;

public abstract class NCPMessage
{
	public abstract int getType();
	
	public abstract byte[] toBytes();
}
