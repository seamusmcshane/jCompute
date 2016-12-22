package jcompute.batch.itemstore;

public final class ItemDiskCacheEntry
{
	private final int id;
	private final byte[] data;
	
	private final long timeAdded;
	private long lastAccessTime;

	public ItemDiskCacheEntry(int id, byte[] data)
	{
		this.id = id;
		this.data = data;

		timeAdded = System.currentTimeMillis();
		lastAccessTime = timeAdded;
	}

	public int getId()
	{
		return this.id;
	}

	// Access count increased on data read.
	public byte[] getData()
	{
		lastAccessTime = System.currentTimeMillis();

		return data;
	}

	public long getLastAccessTime()
	{
		return lastAccessTime;
	}

	public long getTimeAdded()
	{
		return timeAdded;
	}
}
