package jcompute.testing;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class HashWorker implements Runnable 
{
	private int id;
	private boolean found = false;
	private byte[][] hashes;
	private MessageDigest md5Hasher;
	private byte[] target;
	private CountDownLatch latch;
	private byte[] hash;
	private byte[] md5;
	
	public HashWorker(int id)
	{
		this.id = id;
		try
		{
			md5Hasher = MessageDigest.getInstance("MD5");
		}
		catch(NoSuchAlgorithmException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setData(CountDownLatch latch,byte[][] hashes, byte[] target)
	{
		this.latch = latch;
		this.hashes = hashes;
		this.target = target;
	}
	
	public int getId()
	{
		return id;
	}
	
	public boolean found()
	{
		return found;
	}
	
	public byte[] getCycleHash()
	{
		return hash;
	}
	
	public byte[] getMD5()
	{
		return md5;
	}
	
	@Override
	public void run()
	{
		for(int b=0;b<hashes.length;b++)
		{
			byte[] thash = md5Hasher.digest(hashes[b]);

			if(Arrays.equals(thash, target))
			{
				found = true;
				hash = hashes[b];
				md5 = thash;
				break;
			}
		}
		
		latch.countDown();
	}
	
}
