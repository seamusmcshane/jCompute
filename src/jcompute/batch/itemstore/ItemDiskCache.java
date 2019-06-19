package jcompute.batch.itemstore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jcompute.util.file.FileUtil;

public class ItemDiskCache implements ItemStore
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(ItemDiskCache.class);
	
	// Storage location
	private String diskCacheLocation;
	private boolean compressFiles;
	private int compressionLevel;
	
	// Total items added
	private int itemsAddedToCache;
	
	// Mappings
	private int cacheUniqueMappingTable[];
	
	private int cacheSize;
	private byte[][] cacheNonUniqueTable;
	private int uniqueRatio;
	
	// Mem Cache
	private final boolean memCacheEnabled;
	private final int MIN_MEM_CACHE_SIZE = 250;
	private final int MAX_MEM_CACHE_SIZE = 10000;
	private final int memCacheSize;
	private int memCacheRequests;
	private int memCacheHits;
	private int memCacheMisses;
	
	private ItemDiskCacheEntry itemMemCache[];
	private int itemsInMemCache;
	
	// Cache time in millisecond before cache will look at recently access time of the last item. (reduces cache thrashing when adding, but will mean a disk
	// access on misses)
	private final int CACHE_MIN_AGE_PURGE_TIME_LIMIT = 60000;
	
	// How long to give an item before being valid for removal
	private final int CACHE_LRA_THRESHOLD = 30000;
	
	private MessageDigest md5Hasher;
	
	// We need to know the size and ratio to create a perfect mapping of duplicates
	public ItemDiskCache(boolean useMemCache, int cacheSize, int uniqueRatio, String storageLocation, int compressionLevel)
	{
		try
		{
			md5Hasher = MessageDigest.getInstance("MD5");
		}
		catch(NoSuchAlgorithmException e)
		{
			e.printStackTrace();
		}
		
		memCacheEnabled = useMemCache;
		
		if(memCacheEnabled)
		{
			// Try 6.25~ percent of cache size
			int tempMemCacheSize = cacheSize / 16;
			
			// check the calculated value
			if(tempMemCacheSize < MIN_MEM_CACHE_SIZE)
			{
				// Too small use the min
				tempMemCacheSize = MIN_MEM_CACHE_SIZE;
			}
			if(tempMemCacheSize > MAX_MEM_CACHE_SIZE)
			{
				// Too big use the max
				tempMemCacheSize = MAX_MEM_CACHE_SIZE;
			}
			
			// Set mem cache size
			memCacheSize = tempMemCacheSize;
			
			log.info("Disk Cache (Mem Cache) Size : " + memCacheSize);
			
			initMemCache();
		}
		else
		{
			// variable is final.
			memCacheSize = 0;
		}
		
		// always log
		log.info("Disk Cache Mem Cache : " + memCacheEnabled);
		
		this.cacheSize = cacheSize;
		
		this.uniqueRatio = uniqueRatio;
		
		if(cacheSize == 0)
		{
			log.error("Max unique zero");
		}
		
		if(uniqueRatio == 0)
		{
			log.error("Non unique is zero");
		}
		
		// i.e location/diskCache
		diskCacheLocation = storageLocation + File.separator + "diskCache";
		log.info("Disk Cache location " + diskCacheLocation);
		
		// always compress
		compressFiles = true;
		
		this.compressionLevel = compressionLevel;
		
		log.info("Disk Cache c/r " + cacheSize + "/" + uniqueRatio);
		initCache();
	}
	
	private void initCache()
	{
		itemsAddedToCache = 0;
		
		cacheUniqueMappingTable = new int[cacheSize];
		cacheNonUniqueTable = new byte[uniqueRatio][];
		
		// Mark all entries as free (-1)
		//Arrays.fill(cacheNonUniqueTable, -1);
		
		FileUtil.createDirIfNotExist(diskCacheLocation);
	}
	
	private void initMemCache()
	{
		itemsInMemCache = 0;
		
		itemMemCache = new ItemDiskCacheEntry[memCacheSize];
	}
	
	@Override
	public void compact()
	{
		try
		{
			FileUtils.deleteDirectory(new File(diskCacheLocation));
			
			itemMemCache = null;
			
			md5Hasher = null;
			cacheNonUniqueTable = null;
			cacheUniqueMappingTable = null;
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	@Override
	public int addData(byte[] data) throws IOException
	{
		log.info("Adding Data");
		
		if(data == null)
		{
			return -1;
		}
		
		return addDataToCache(data);
	}
	
	private int addDataToCache(byte[] data) throws IOException
	{
		log.info("addDataToCache");
		
		int cacheNonUniqueIndex = lookForNonUniqueMappingOrSet(data);
		
		
		// Look up the index in the non unique table or set it
		//		int cacheNonUniqueIndex = lookForNonUniqueMappingOrSet(uniqueValue);
		
		// In unique table set lookup index
		cacheUniqueMappingTable[itemsAddedToCache] = cacheNonUniqueIndex;
		
		log.info("addDataToCache created unique mapping " + itemsAddedToCache + " " + cacheNonUniqueIndex);
		
		// Add to the memcache if enabled and possible
		if(memCacheEnabled)
		{
			updateMemCache(cacheNonUniqueIndex, data);
		}
		
		// Add to disk cache.
		writeDataToDiskCache(cacheNonUniqueIndex, data);
		
		// Increase item count
		itemsAddedToCache++;
		
		return itemsAddedToCache - 1;
	}
	
	// Look up the mapping
	private int lookForNonUniqueMappingOrSet(byte[] data) throws IOException
	{
		byte[] dataHash = md5Hasher.digest(data);

		for(int i = 0; i < cacheNonUniqueTable.length; i++)
		{
			// look for first free location
			if(cacheNonUniqueTable[i] == null)
			{
				// New Mapping
				cacheNonUniqueTable[i] = dataHash;
				
				log.info("lookForMappingOrSet new mapping " + i);
				
				// Return the index of this mapping
				return i;
			}
			else
			{
				// Compare the values
				if(MessageDigest.isEqual(dataHash, cacheNonUniqueTable[i]))
				{
					log.info("lookForMappingOrSet existing mapping " + i);
					
					// Mapping exists
					return i;
				}
			}
		}
		
		// Fatal error we don't remove mappings so one should exist, but it doesn't
		log.error("Mapping index does not exist in cache");
		
		// Throw an IO exception that can be caught rather than allow set an index of -1 and guarantee an array out of bounds.
		throw new IOException("Mapping does not exist in cache");
	}
	
//	// Look up the mapping
//	private int lookForNonUniqueMappingOrSetOLD(int uniqueValue) throws IOException
//	{
//		for(int i = 0; i < cacheNonUniqueTable.length; i++)
//		{
//			// look for first free location
//			if(cacheNonUniqueTable[i] == -1)
//			{
//				// New Mapping
//				cacheNonUniqueTable[i] = uniqueValue;
//				
//				log.info("lookForMappingOrSet new mapping " + i);
//				
//				// Return the index of this mapping
//				return i;
//			}
//			else
//			{
//				// Compare the values
//				if(cacheNonUniqueTable[i] == uniqueValue)
//				{
//					log.info("lookForMappingOrSet existing mapping " + i);
//					
//					// Mapping exists
//					return i;
//				}
//			}
//		}
//		
//		// Fatal error we don't remove mappings so one should exist, but it doesn't
//		log.error("Mapping index does not exist in cache");
//		
//		// Throw an IO exception that can be caught rather than allow set an index of -1 and guarantee an array out of bounds.
//		throw new IOException("Mapping does not exist in cache");
//	}
	
	private void writeDataToDiskCache(int nonUniqueId, byte[] data) throws IOException
	{
		String filePath = diskCacheLocation + File.separator + nonUniqueId;
		
		log.info("Add " + nonUniqueId + " " + filePath);
		
		File file = new File(filePath);
		
		if(!file.exists() && !file.isDirectory())
		{
			byte[] fileContents = data;
			
			if(compressFiles)
			{
				fileContents = FileUtil.compressBytes(fileContents, compressionLevel);
			}
			
			FileOutputStream fos = new FileOutputStream(filePath);
			fos.write(fileContents);
			fos.close();
		}
	}
	
	@Override
	public byte[] getData(int cacheIndex) throws IOException
	{
		log.info("getData " + cacheIndex);
		
		return getDataFromCache(cacheIndex);
	}
	
	private byte[] getDataFromCache(int cacheIndex) throws IOException
	{
		byte[] data = null;
		
		// The non-unique index mapping of this unique index
		int cacheNonUniqueIndex = cacheUniqueMappingTable[cacheIndex];
		
		// Try mem cache if enabled
		if(memCacheEnabled)
		{
			memCacheRequests++;
			
			data = searchMemCache(cacheNonUniqueIndex);
			
			if(data == null)
			{
				memCacheMisses++;
			}
			else
			{
				memCacheHits++;
			}
		}
		
		// null = cache miss - fetch from disk
		if(data == null)
		{
			data = getFileFromDisk(cacheNonUniqueIndex);
			
			// uncompress the data if needed
			if(compressFiles)
			{
				data = FileUtil.decompressBytes(data);
			}
			
			// Update the cache and store uncompressed in memory.
			if(memCacheEnabled)
			{
				// The cache had a miss update it.
				updateMemCache(cacheNonUniqueIndex, data);
			}
		}
		
		return data;
	}
	
	private byte[] getFileFromDisk(int uniqueId) throws IOException
	{
		byte[] fileContents = null;
		
		String filePath = diskCacheLocation + File.separator + uniqueId;
		File file = new File(filePath);
		
		log.info("Get " + uniqueId + " " + filePath);
		
		if(file.exists() && !file.isDirectory())
		{
			Path path = Paths.get(filePath);
			
			fileContents = Files.readAllBytes(path);
		}
		
		return fileContents;
	}
	
	private byte[] searchMemCache(int cacheNonUniqueIndex)
	{
		// Check mem cache;
		for(int i = 0; i < itemsInMemCache; i++)
		{
			if(itemMemCache[i].getId() == cacheNonUniqueIndex)
			{
				log.info("getData found mem cache data for " + cacheNonUniqueIndex);
				
				return itemMemCache[i].getData();
			}
		}
		
		// Not in mem cache
		return null;
	}
	
	private void updateMemCache(int cacheNonUniqueIndex, byte[] data)
	{
		if(data == null)
		{
			log.error("Tried to add null data to mem cache " + cacheNonUniqueIndex);
			
			return;
		}
		
		// If mem cache is full - remove last if allowed by removal time limits
		if(itemsInMemCache == memCacheSize)
		{
			// Sort the items by NLRA
			Arrays.sort(itemMemCache, new ItemDiskCacheComparator());
			
			long lastItemTA = itemMemCache[itemsInMemCache - 1].getTimeAdded();
			long lastItemLRA = itemMemCache[itemsInMemCache - 1].getLastAccessTime();
			long timeNow = System.currentTimeMillis();
			
			// Windows Back
			long purgeTimeWindow = (timeNow - CACHE_MIN_AGE_PURGE_TIME_LIMIT);
			long accesssTimeWindow = (timeNow - CACHE_LRA_THRESHOLD);
			
			// If the oldest item item added time is out side the purgeTimeWindow
			// And if it is outside accesssTimeWindow the remove it and add the new item.
			if((lastItemTA < purgeTimeWindow) && (lastItemLRA < accesssTimeWindow))
			{
				// log.info("purgeTimeWindow " + purgeTimeWindow + " accesssTimeWindow " + accesssTimeWindow);
				// for(CacheItem item : itemMemCache)
				// {
				// log.info("Cache " + item.getId() + " " + item.getTimeAdded() + " " + item.getLastAccessTime());
				// }
				
				log.info("purgeTimeWindow " + purgeTimeWindow + " accesssTimeWindow " + accesssTimeWindow);
				log.info("Removing " + itemMemCache[itemsInMemCache - 1].getId());
				
				// Add this item to the mem cache.
				itemMemCache[itemsInMemCache - 1] = new ItemDiskCacheEntry(cacheNonUniqueIndex, data);
				
				log.info("purgeTimeWindow " + purgeTimeWindow + " accesssTimeWindow " + accesssTimeWindow);
				log.info("Added " + itemMemCache[itemsInMemCache - 1].getId());
				
				// log.info("purgeTimeWindow " + purgeTimeWindow + " accesssTimeWindow " + accesssTimeWindow);
				// for(CacheItem item : itemMemCache)
				// {
				// log.info("Cache " + item.getId() + " " + item.getTimeAdded() + " " + item.getLastAccessTime());
				// }
			}
		}
		else
		{
			boolean inCache = false;
			
			for(ItemDiskCacheEntry item : itemMemCache)
			{
				// Reached end of cache that is not full.
				if(item == null)
				{
					log.info("Item not in cache " + cacheNonUniqueIndex);
					
					inCache = false;
					break;
				}
				
				// Cache contains our item.
				if(item.getId() == cacheNonUniqueIndex)
				{
					log.info("Item not in cache " + cacheNonUniqueIndex);
					
					inCache = true;
					break;
				}
			}
			
			if(!inCache)
			{
				// Add this item to the mem cache that is filling
				itemMemCache[itemsInMemCache] = new ItemDiskCacheEntry(cacheNonUniqueIndex, data);
				
				itemsInMemCache++;
				
				log.info("Added item to mem cache " + itemsAddedToCache + " total " + itemsInMemCache);
			}
			
		}
	}
	
	public int getCacheSize()
	{
		return cacheSize;
	}
	
	public int getUniqueRatio()
	{
		return uniqueRatio;
	}
	
	public boolean getMemCacheEnabled()
	{
		return memCacheEnabled;
	}
	
	public int getMemCacheSize()
	{
		return memCacheSize;
	}
	
	public int getMemCacheRequests()
	{
		return memCacheRequests;
	}
	
	public int getMemCacheMisses()
	{
		return memCacheMisses;
	}
	
	public int getMemCacheHits()
	{
		return memCacheHits;
	}
	
	public float getMemCacheHitRatio()
	{
		return(memCacheHits / (float) memCacheRequests);
	}
	
	public float getMemCacheMissRatio()
	{
		return 1f - getMemCacheHitRatio();
	}
}
