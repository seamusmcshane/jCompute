package jCompute.Datastruct.cache;

import jCompute.util.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiskCache
{
	// SL4J Logger
	private static Logger log = LoggerFactory.getLogger(DiskCache.class);
	
	private String cacheLocation;
	private boolean compressFiles;
	private int compressionLevel;
	
	public DiskCache(String storageLocation)
	{
		// i.e location/diskCache
		this.cacheLocation = storageLocation + File.separator + "diskCache";
		
		this.compressFiles = false;
		
		initCache();
	}
	
	public DiskCache(String storageLocation, int compressionLevel)
	{
		// i.e location/diskCache
		this.cacheLocation = storageLocation + File.separator + "diskCache";
		
		this.compressFiles = true;
		
		this.compressionLevel = compressionLevel;
		
		initCache();
	}
	
	public String addFile(byte[] contents)
	{
		if(contents == null)
		{
			return null;
		}
		
		// Generate File Hash
		String fileHash = FileUtil.HashFile(contents);
		String filePath = cacheLocation + File.separator + fileHash;
		
		log.debug("Add " + fileHash + " " + filePath);
		
		File file = new File(filePath);
		
		if(!file.exists() && !file.isDirectory())
		{
			byte[] fileContents = contents;
			
			if(compressFiles)
			{
				fileContents = FileUtil.compressBytes(contents, compressionLevel);
			}
			
			try
			{
				FileOutputStream fos = new FileOutputStream(filePath);
				fos.write(fileContents);
				fos.close();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
		
		return fileHash;
	}
	
	public byte[] getFile(String fileHash)
	{
		byte[] contents = null;
		
		String filePath = cacheLocation + File.separator + fileHash;
		File file = new File(filePath);
		
		log.debug("Get " + fileHash + " " + filePath);
		
		if(file.exists() && !file.isDirectory())
		{
			Path path = Paths.get(filePath);
			
			try
			{
				contents = Files.readAllBytes(path);
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
			
			if(compressFiles)
			{
				contents = FileUtil.decompressBytes(contents);
			}
			
		}
		
		return contents;
	}
	
	public void initCache()
	{
		FileUtil.createDirIfNotExist(cacheLocation);
	}
	
	public void clear()
	{
		try
		{
			FileUtils.deleteDirectory(new File(cacheLocation));
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
}
