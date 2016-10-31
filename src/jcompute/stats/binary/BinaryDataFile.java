package jcompute.stats.binary;

/**
 * A container for a binary data that can be returned from a finished simulation.
 * The data will be wrote to disk in the with the file name provided - no further processing is performed.
 * 
 * @author Seamus McShane
 */
public class BinaryDataFile
{
	public final String name;
	public final byte[] bytes;
	
	public BinaryDataFile(String name, byte[] bytes)
	{
		this.name = name;
		this.bytes = bytes;
	}
}
