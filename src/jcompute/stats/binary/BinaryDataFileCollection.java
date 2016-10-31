package jcompute.stats.binary;

import java.util.ArrayList;

/**
 * A small class for managing a list of "BinaryDataFile".
 * Note : Not thread safe.
 * 
 * @author Seamus McShane
 */
public class BinaryDataFileCollection
{
	private final String name;
	private final ArrayList<BinaryDataFile> dataFiles;
	
	/**
	 * @param name
	 * The name for this collection of files.
	 */
	public BinaryDataFileCollection(String name)
	{
		this.name = name;
		dataFiles = new ArrayList<BinaryDataFile>();
	}
	
	public void addDataFile(BinaryDataFile file)
	{
		dataFiles.add(file);
	}
	
	public ArrayList<BinaryDataFile> getDataFiles()
	{
		return dataFiles;
	}
	
	public String getName()
	{
		return name;
	}
}
