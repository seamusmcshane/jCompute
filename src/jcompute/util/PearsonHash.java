package jcompute.util;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jcompute.math.JCMath;

public final class PearsonHash
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(PearsonHash.class);
	
	// The perfect hash value (if found)
	private int pHash;
	
	// The prime used with the pHash
	private int prime;
	
	// Hashing Table
	private int[] pearsonTable = null;
	
	// Did the generation complete
	private boolean finished = false;
	
	private int[] hashKeys;
	
	public PearsonHash(String[] textList, int triesPerTableSize)
	{
		this.hashKeys = new int[textList.length];
		
		// Clear
		Arrays.fill(hashKeys, -1);
		
		generatePearsonHashTable(textList, triesPerTableSize);
	}
	
	/*
	 * ***************************************************************************************************
	 * Getters
	 *****************************************************************************************************/
	
	/**
	 * Returns a Pearson hash for a byte array (representing text)
	 * 
	 * @param text
	 * @return
	 */
	public int getHash(byte[] text)
	{
		// Pearson hashing with with hash seeded with each strings individual text size.
		int hash = pHash + text.length;
		
		// Pearson hashing
		for(int i = 0; i < text.length; ++i)
		{
			hash = (hash + text[i]) % pearsonTable.length;
			hash = pearsonTable[hash];
		}
		return(hash);
	}
	
	/**
	 * The hash key list
	 * 
	 * @return
	 */
	public int[] getHashKeys()
	{
		return hashKeys;
	}
	
	/*
	 * ***************************************************************************************************
	 * Main Entry method
	 *****************************************************************************************************/
	
	private void generatePearsonHashTable(String[] textList, int triesPerTableSize)
	{
		// Total Keys
		final int numKeys = textList.length;
		
		// Start with table matching the keys
		final int hashingTableStartSize = numKeys;
		
		// This is the maximum temporary working table size we will try.
		final int maxTableSize = 32 * hashingTableStartSize;
		
		// Count of total outer iterations
		int iteration = 0;
		
		// Inner 1 - tries per table
		int tries = 0;
		
		/*
		 *  Brute Force Table Generation.
		 *  
		 *  Start with small table sizes, trying various primes and hash seeds. 
		 */
		int tableSize = hashingTableStartSize;
		for(; tableSize < maxTableSize; tableSize += 2)
		{
			// Hash Table
			pearsonTable = new int[tableSize];
			
			for(int prI = 0; prI < 20; prI++)
			{
				prime = JCMath.getPrime(prI);
				
				generatePearsonTable(prime);
				
				// System.out.println("iteration " + iteration + " HASHING_TABLE " + tableSize + " prime " + prime);
				
				pHash = 0;
				tries = 0;
				
				while(!finished && tries < triesPerTableSize)
				{
					createPearsonKeys(textList);
					
					int total = checkUnique(hashKeys, numKeys);
					
					if(total == 0)
					{
						// System.out.println("Zero collision hash " + pHash);
						
						finished = true;
					}
					else
					{
						// Clear hash keys
						Arrays.fill(hashKeys, -1);
						
						// System.out.println("Try Aborted due to hash collisions " + total + " phash " + phash);
						
						pHash++;
					}
					
					tries++;
				}
				
				if(finished)
				{
					break;
				}
			}
			
			if(finished)
			{
				int[] minimalArray = new int[textList.length];
				
				for(int i = 0; i < hashKeys.length; i++)
				{
					if(hashKeys[i] != -1)
					{
						minimalArray[i] = hashKeys[i];
					}
				}
				
				hashKeys = minimalArray;
				
				break;
			}
			
			iteration++;
		}
		
		if(!finished)
		{
			int col = showNonUnique(hashKeys, numKeys);
			if(col > 0)
			{
				log.error("Gave up after " + iteration + " iterations each with " + tries + " tries there are where still " + col + " collisions in final try");
			}
			
			// Hashing was not possible
			pHash = -1;
		}
	}
	
	/*
	 * ***************************************************************************************************
	 * Helper Methods
	 *****************************************************************************************************/
	
	private int checkUnique(int[] array, final int SIZE)
	{
		int total = 0;
		
		for(int i = 0; i < SIZE; i++)
		{
			int testVal = array[i];
			
			int count = 0;
			for(int t = 0; t < SIZE; t++)
			{
				// Compare
				if(testVal == array[t])
				{
					count++;
				}
			}
			
			if(count > 1)
			{
				total += count;
				
				return total;
			}
		}
		
		return total;
	}
	
	private int showNonUnique(int[] array, final int SIZE)
	{
		int total = 0;
		log.info("=========================================");
		
		for(int i = 0; i < SIZE; i++)
		{
			int iVal = array[i];
			
			int count = 0;
			int pcount = 0;
			for(int t = 0; t < SIZE; t++)
			{
				// Compare
				if(iVal == array[t])
				{
					count++;
					
					if(pcount != count && count > 1 && pcount > 1)
					{
						log.info("|/\\|/\\|/\\|/\\|/\\| Collision @  i " + i + " ival " + iVal + " & t " + t + " tval " + array[t]);
						
						total++;
					}
					
					pcount = count;
				}
			}
			
			if(count == 0)
			{
				log.info(">>>>>>>> No mapping @ " + i + " " + array[i]);
			}
			
			if(count == 1)
			{
				log.info("Mapping @ " + i + " " + array[i]);
			}
			
		}
		
		return total;
	}
	
	/**
	 * Hashes an array of strings 
	 * @param textList
	 */
	private void createPearsonKeys(final String[] textList)
	{
		for(int i = 0; i < textList.length; i++)
		{
			hashKeys[i] = getHash(textList[i].getBytes(StandardCharsets.UTF_8));
		}
	}
	
	/**
	 * Generates a random layout for using in Pearson hashing
	 * 
	 * @param prime
	 */
	private void generatePearsonTable(final int prime)
	{
		// Init table - with incrementing values
		for(int i = 0; i < pearsonTable.length; i++)
		{
			pearsonTable[i] = i;
		}
		
		// A prime
		int p = prime;
		
		// Generate Pseudo Random Table Layout
		for(int j = 0; j < 4; j++)
		{
			for(int i = 0; i < pearsonTable.length; i++)
			{
				// Store I
				int s = pearsonTable[i];
				
				// Generate P index p + s val
				p = (p + s) % pearsonTable.length;
				
				// Swap I and P
				pearsonTable[i] = pearsonTable[p];
				pearsonTable[p] = s;
			}
		}
	}
}
