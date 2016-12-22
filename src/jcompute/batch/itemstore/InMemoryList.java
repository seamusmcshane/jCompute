package jcompute.batch.itemstore;

import java.io.IOException;
import java.util.ArrayList;

public class InMemoryList implements ItemStore
{
	private ArrayList<byte[]> list;
	
	public InMemoryList(int size)
	{
		list = new ArrayList<byte[]>();
	}
	
	@Override
	public int addData(byte[] data) throws IOException
	{
		list.add(data);
		
		return list.size() - 1;
	}
	
	@Override
	public byte[] getData(int id) throws IOException
	{
		return list.get(id);
	}
	
	@Override
	public void compact()
	{
		list = null;
	}
}
