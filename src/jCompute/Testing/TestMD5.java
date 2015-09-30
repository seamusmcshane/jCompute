package jCompute.Testing;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import jCompute.Datastruct.knn.benchmark.TimerObj;
import jCompute.util.Text;

public class TestMD5
{
	public static void main(String args[]) throws UnsupportedEncodingException, NoSuchAlgorithmException
	{
		int threads = 8;
		int blockSize = 10000;
		int inc = threads*blockSize;
		ExecutorService executor = Executors.newFixedThreadPool(threads);
				
		TimerObj timeCount = new TimerObj();
				
		MessageDigest md5Hasher = MessageDigest.getInstance("MD5");
		
		Scanner scanner = new Scanner(System.in);
		
		System.out.println("Target type ");
		System.out.println("1) Hash ");
		System.out.println("2) Password (testing) ");

		int type = scanner.nextInt();
		
		String passString = null;
		byte[] passBytes = null;
		byte[] hashBytes = null;

		if(type == 2)
		{
			System.out.println("Enter target password : ");
			passString = scanner.next();
			passBytes = passString.getBytes();
			hashBytes = md5Hasher.digest(passBytes);
		}
		else
		{
			System.out.println("Enter target hash : ");
			String hash = scanner.next();
			passString = "Unknown Password";
			
			hashBytes = new byte[hash.length()/2];
			
			// Every two digits is our bytes value - we need to join them to assemble the numeric value.
			int hb = 0;
			for(int i=0;i<hash.length();i+=2)
			{
				int val1 = (byte) Character.getNumericValue(hash.charAt(i));
				int val2 = (byte) Character.getNumericValue(hash.charAt(i+1));
				
				hashBytes[hb] = (byte) (val1 << 4);
				hashBytes[hb] = (byte) (hashBytes[hb] | val2);
				hb++;
			}
		}
		scanner.close();
		
		int hashLen = hashBytes.length;

		System.out.println("Target   : " + passString);
		System.out.println("Hash     : " + getHashString(hashBytes));
		System.out.println("HashLen  : " + hashLen);
		
		byte[] cycleHash = new byte[1];
		
		for(int i=0;i<cycleHash.length;i++)
		{
			cycleHash[i] = 0x21;
		}
		
		int wrap = 126;
		long checked = 0;
		
		timeCount.startTimer();
		
		boolean found = false;
		
		HashWorker[] worker = new HashWorker[threads];
		
		for (int i = 0; i < threads; i++) 
		{
			worker[i] = new HashWorker(i);
		}
		
		while(!found)
		{
			CountDownLatch latch = new CountDownLatch(threads);
			
			byte[][][] cycleHashes = new byte[threads][blockSize][];
			for(int i=0;i<threads;i++)
			{
				for(int b=0;b<blockSize;b++)
				{
					cycleHash = updateArray(cycleHash,0,wrap);
					cycleHashes[i][b] = cycleHash;
				}
			}
			
			for (int i = 0; i < threads; i++) 
			{
				worker[i].setData(latch,cycleHashes[i],hashBytes);
				executor.execute(worker[i]);
			}
			
			try
			{
				latch.await();
			}
			catch(InterruptedException e)
			{
				e.printStackTrace();
			}
			
			for (int i = 0; i < threads; i++) 
			{
				if(((HashWorker)worker[i]).found())
				{
					timeCount.stopTimer();
					System.out.println("Time : " + Text.longTimeToDHMSM(timeCount.getTimeTaken()));

					byte[] hash = ((HashWorker)worker[i]).getMD5();
					byte[] string = ((HashWorker)worker[i]).getCycleHash();

					System.out.print("Password Found : " +"("+string.length+")"+ new String(string) + " MD5 " + getHashString(hash) + " ");
					for(int c=0;c<string.length;c++)
					{
						System.out.print(string[c] + " ");
					}
					System.out.print('\n');
			        executor.shutdown();
			        while (!executor.isTerminated()) 
			        {
			        	
			        }
					found = true;
				}
			}
			
			checked+=inc;
			
			if(checked % 10000000 == 0)
			{
				System.out.print(checked + "\t"+ getHashString(cycleHash) + "\t" + new String(cycleHash) + "\t");
				for(int c=0;c<cycleHash.length;c++)
				{
					System.out.print(cycleHash[c] + " ");
				}
				System.out.print('\n');
			}
		}
	}
	
	public static byte[] updateArray(byte[] bytes, int pos, int wrap)
	{
		int cHashLen = bytes.length;
		
		if(incArray(bytes,0,wrap))
		{
			cHashLen++;
			
			byte[] newCycleHash = new byte[cHashLen];
			
			System.arraycopy(bytes, 0, newCycleHash, 0, bytes.length);
			
			return newCycleHash;
		}
		else
		{
			byte[] newCycleHash = new byte[cHashLen];
			System.arraycopy(bytes, 0, newCycleHash, 0, bytes.length);
			return newCycleHash;
		}

	}
	
	public static boolean incArray(byte[] bytes, int pos, int wrap)
	{
		if(pos==bytes.length)
		{
			return true;
		}
		
		bytes[pos]++;
		
		if(bytes[pos] == wrap)
		{
			bytes[pos] = 0x21;
			
			return incArray(bytes,pos+1,wrap);
		}
		
		return false;
	}

	public static String getHashString(byte[] bytes)
	{
		StringBuilder buffer = new StringBuilder();
		
		for(int d = 0; d < bytes.length; d++)
		{
			buffer.append(Integer.toString((bytes[d] & 0xff) + 0x100, 16).substring(1));
		}
		
		return buffer.toString();
	}
	
}

