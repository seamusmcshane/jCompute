package jCompute.Datastruct.cache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jCompute.util.FileUtil;
import jCompute.util.JCMath;

public class DiskCache implements Comparator<CacheItem>
{
	// SL4J Logger
	private static Logger log = LoggerFactory.getLogger(DiskCache.class);

	// Storage location
	private String diskCacheLocation;
	private boolean compressFiles;
	private int compressionLevel;

	// Total items added
	private int itemsAddedToCache;

	// Mappings
	private int cacheUniqueMappingTable[];
	private int cacheSize;
	private int cacheNonUniqueTable[];
	private int uniqueRatio;

	// Mem Cache
	private final boolean memCacheEnabled;
	private final int MIN_MEM_CACHE_SIZE = 250;
	private final int MAX_MEM_CACHE_SIZE = 10000;
	private final int memCacheSize;
	private int memCacheHit;
	private int memCacheMiss;

	private CacheItem itemMemCache[];
	private int itemsInMemCache;

	// Cache time in millisecond before cache will look at recently access time of the last item. (reduces cache thrashing, but will mean a disk access on misses)
	private final int CACHE_MIN_AGE_PURGE_TIME = 60000;

	// How long to give an item before being valid for removal
	private final int CACHE_LRA_THRESHOLD = 30000;

	// We need to know the size and ratio to create a perfect mapping of duplicates
	public DiskCache(boolean useMemCache, int cacheSize, int uniqueRatio, String storageLocation, int compressionLevel)
	{
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
		cacheNonUniqueTable = new int[uniqueRatio];

		// Mark all entries as free (-1)
		Arrays.fill(cacheNonUniqueTable, -1);

		FileUtil.createDirIfNotExist(diskCacheLocation);
	}

	private void initMemCache()
	{
		itemsInMemCache = 0;

		itemMemCache = new CacheItem[memCacheSize];
	}

	public void clear()
	{
		try
		{
			FileUtils.deleteDirectory(new File(diskCacheLocation));

			itemMemCache = null;
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	public int addData(int uniqueValue, byte[] data) throws IOException
	{
		log.info("Adding Data");

		if(data == null)
		{
			return -1;
		}

		return addDataToCache(uniqueValue, data);
	}

	private int addDataToCache(int uniqueValue, byte[] data) throws IOException
	{
		log.info("addDataToCache");

		// Look up the index in the non unique table or set it
		int cacheNonUniqueIndex = lookForNonUniqueMappingOrSet(uniqueValue);

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
	private int lookForNonUniqueMappingOrSet(int uniqueValue) throws IOException
	{
		for(int i = 0; i < cacheNonUniqueTable.length; i++)
		{
			// look for first free location
			if(cacheNonUniqueTable[i] == -1)
			{
				// New Mapping
				cacheNonUniqueTable[i] = uniqueValue;

				log.info("lookForMappingOrSet new mapping " + i);

				// Return the index of this mapping
				return i;
			}
			else
			{
				// Compare the values
				if(cacheNonUniqueTable[i] == uniqueValue)
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

	public byte[] getData(int uniqueId) throws IOException
	{
		log.info("getData " + uniqueId);

		return getDataFromCache(uniqueId);
	}

	private byte[] getDataFromCache(int uniqueId) throws IOException
	{
		byte[] data = null;

		// The non-unique index mapping of this unique index
		int cacheNonUniqueIndex = cacheUniqueMappingTable[uniqueId];

		// Try mem cache if enabled
		if(memCacheEnabled)
		{
			data = searchMemCache(cacheNonUniqueIndex);
		}

		// null = cache miss - fetch from disk
		if(data == null)
		{
			memCacheMiss++;

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
		else
		{
			memCacheHit++;
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

		// If mem cache is full - remove last if needed
		if(itemsInMemCache == memCacheSize)
		{
			// Sort the items by NLRA
			Arrays.sort(itemMemCache, this);

			for(CacheItem item : itemMemCache)
			{
				log.info("Cache " + item.getId() + " " + item.getTimeAdded() + " " + item.getLastAccessTime());
			}

			long lastItemTA = itemMemCache[itemsInMemCache - 1].getTimeAdded();
			long lastItemLRA = itemMemCache[itemsInMemCache - 1].getLastAccessTime();

			// If the oldest item is over the min time in teh cache and is over the LRA threshold
			if((lastItemTA > CACHE_MIN_AGE_PURGE_TIME) && (lastItemLRA > CACHE_LRA_THRESHOLD))
			{
				log.info("Removing " + itemMemCache[itemsInMemCache - 1].getId());

				// Add this item to the mem cache.
				itemMemCache[itemsInMemCache - 1] = new CacheItem(cacheNonUniqueIndex, data);

				log.info("Added " + itemMemCache[itemsInMemCache - 1].getId());
			}

			for(CacheItem item : itemMemCache)
			{
				log.info("Cache " + item.getId() + " " + item.getTimeAdded() + " " + item.getLastAccessTime());
			}
		}
		else
		{
			boolean inCache = false;

			for(CacheItem item : itemMemCache)
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
				itemMemCache[itemsInMemCache] = new CacheItem(cacheNonUniqueIndex, data);

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
	
	public int getMemCacheMiss()
	{
		return memCacheMiss;
	}

	public int getMemCacheHit()
	{
		return memCacheHit;
	}

	@Override
	public int compare(CacheItem item1, CacheItem item2)
	{
		// Sorts newer first.

		long item1Added = item1.getTimeAdded();
		long item2Added = item2.getTimeAdded();

		// Assumes time value increases
		boolean itemOneOlder = (item1Added < item2Added);

		if(itemOneOlder)
		{
			// Item two is newer and should be above
			return 1;
		}
		else
		{
			// Item one is newer and should go above
			return -1;
		}
	}

	public float getMemHitMissRatio()
	{
		return JCMath.round((memCacheMiss == 0) ? 0 : ((memCacheMiss / (float) memCacheHit) * 100), 2);
	}
}
