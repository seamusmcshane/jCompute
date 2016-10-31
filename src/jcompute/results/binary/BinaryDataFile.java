package jcompute.results.binary;

/**
 * A container for a binary data that can be returned from a finished simulation.
 * 
 * @author Seamus McShane
 */
public class BinaryDataFile
{
	public final String name;
	public final String mediatype;
	public final byte[] bytes;
	
	public BinaryDataFile(String name, String mediatype, byte[] bytes)
	{
		this.name = name;
		this.mediatype = mediatype;
		this.bytes = bytes;
	}
}
