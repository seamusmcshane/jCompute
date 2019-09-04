package jcompute.results.export.custom;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jcompute.results.ResultManager;
import jcompute.results.custom.CustomItemResultInf;
import jcompute.results.custom.CustomItemResultParser;

public class CustomItemResult
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(CustomItemResult.class);
	
	private String[] fileNames;
	
	private byte[][] fileData;
	
	public CustomItemResult(ResultManager rm)
	{
		log.info("Creating CustomItemResult from Result Manager - " + rm.getName());
		
		ArrayList<CustomItemResultInf> test = rm.getCustomItemResultList();
		
		int cirNum = test.size();
		
		fileNames = new String[cirNum];
		fileData = new byte[cirNum][];
		
		for(int cir = 0; cir < cirNum; cir++)
		{
			CustomItemResultInf result = test.get(cir);
			
			fileNames[cir] = result.getLogFileName();
			
			fileData[cir] = CustomItemResultParser.CustomItemResultToBytes(result);
		}
	}
	
	public CustomItemResult(ByteBuffer source)
	{
		int cirNum = source.getInt();
		
		fileNames = new String[cirNum];
		fileData = new byte[cirNum][];
		
		for(int cir = 0; cir < cirNum; cir++)
		{
			int tNum = source.getInt();
			
			if(tNum != cir)
			{
				log.error("File Numbers not correct");
			}
			
			int len = source.getInt();
			byte[] stringBytes = new byte[len];
			
			log.debug("File Name Len " + len);
			
			source.get(stringBytes, 0, len);
			
			// FileName
			fileNames[cir] = new String(stringBytes);
			
			log.debug("File Name " + fileNames[cir]);
			
			// FileData
			len = source.getInt();
			stringBytes = new byte[len];
			log.debug("Data Len " + len);
			
			source.get(stringBytes, 0, len);
			fileData[cir] = stringBytes;
		}
	}
	
	public String[] getCirFileNames()
	{
		return fileNames;
	}
	
	public byte[] toBytes()
	{
		int cirNum = fileNames.length;
		
		int size = 0;
		
		// cirNum
		size += 4;
		
		// Calculate Byte Size
		for(int cir = 0; cir < cirNum; cir++)
		{
			// File Num Field
			size += 4;
			// File Name Len Field
			size += 4;
			// File Name Len in bytes
			size += fileNames[cir].getBytes().length;
			// Data Len Field
			size += 4;
			// Data Len in bytes
			size += fileData[cir].length;
		}
		
		// Unicode strings are 16bit=2bytes char
		ByteBuffer tbuffer = ByteBuffer.allocate(size);
		
		// Cir Num
		tbuffer.putInt(cirNum);
		
		// Cir Filename and Data
		for(int cir = 0; cir < cirNum; cir++)
		{
			writeBinFileToByteBuffer(tbuffer, cir, fileNames[cir], fileData[cir]);
		}
		
		return tbuffer.array();
	}
	
	private void writeBinFileToByteBuffer(ByteBuffer tbuffer, int fileNum, String fileName, byte[] binData)
	{
		// Number of file
		tbuffer.putInt(fileNum);
		
		// File Name Len (bytes)
		tbuffer.putInt(fileName.getBytes().length);
		
		// File Name String to bytes
		tbuffer.put(fileName.getBytes());
		
		// File size bytes
		tbuffer.putInt(binData.length);
		
		// File size
		tbuffer.put(binData);
	}

	public byte[][] getCirData()
	{
		return fileData;
	}
}
