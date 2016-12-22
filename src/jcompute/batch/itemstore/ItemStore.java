package jcompute.batch.itemstore;

import java.io.IOException;

public interface ItemStore
{
	public int addData(byte[] data) throws IOException;
	public byte[] getData(int id) throws IOException;
	public void compact();
}
