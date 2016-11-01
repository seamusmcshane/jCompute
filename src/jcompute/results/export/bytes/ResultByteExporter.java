package jcompute.results.export.bytes;

import java.nio.ByteBuffer;

import jcompute.results.export.Result.ExportFormat;

public class ResultByteExporter
{
	public void populateFromByteBuffer(ByteBuffer source)
	{
		log.debug("StatExporter : populating from ByteBuffer");
		
		if((format == ExportFormat.ZXML) || (format == ExportFormat.ZCSV))
		{
			int numFiles = source.getInt();
			log.debug("Num Files " + numFiles);
			
			// Expects 1 file.
			int fNum = source.getInt();
			log.debug("File Num " + fNum);
			
			if(numFiles != 1)
			{
				log.error("More than 1 file detected in archive operation");
			}
			
			if(fNum != 0)
			{
				log.error("File Numbers not correct");
			}
			
			// File Name Len
			int len = source.getInt();
			log.debug("File Name Len " + len);
			
			// Filename
			byte[] fileName = new byte[len];
			source.get(fileName, 0, len);
			log.debug("File Name " + fileName[0]);
			
			// Bin Data Len
			len = source.getInt();
			log.debug("Data Len " + len);
			
			// Bin Data
			traceBinaryData = new byte[len];
			source.get(traceBinaryData, 0, len);
		}
		else
		{
			/*
			 * ***************************************************************************************************
			 * Get Trace Files
			 *****************************************************************************************************/
			
			int traceFiles = source.getInt();
			log.debug("Num Files " + traceFiles);
			
			traceFileNames = new String[traceFiles];
			traceTextData = new String[traceFiles];
			
			for(int t = 0; t < traceFiles; t++)
			{
				int tNum = source.getInt();
				
				if(tNum != t)
				{
					log.error("File Numbers not correct");
				}
				
				log.debug("Trace File Number : " + tNum);
				
				int len = source.getInt();
				byte[] stringBytes = new byte[len];
				
				log.debug("File Name Len " + len);
				
				source.get(stringBytes, 0, len);
				
				// FileName
				traceFileNames[t] = new String(stringBytes);
				
				/*
				 * Remote filenames are sent with out a suffix Append one if required
				 */
				if(!traceFileNameSuffix.equals(""))
				{
					traceFileNames[t] += " " + traceFileNameSuffix;
				}
				
				log.debug("File Name " + traceFileNames[t]);
				
				// FileData
				len = source.getInt();
				stringBytes = new byte[len];
				log.debug("Data Len " + len);
				
				source.get(stringBytes, 0, len);
				traceTextData[t] = new String(stringBytes);
			}
			
			/*
			 * ***************************************************************************************************
			 * Get Collections
			 *****************************************************************************************************/
			
			int numCollections = source.getInt();
			
			binaryCollectionNames = new String[numCollections];
			
			for(int c = 0; c < numCollections; c++)
			{
				// File Name Len Field
				int len = source.getInt();
				byte[] bytes = new byte[len];
				
				// Collection Name
				source.get(bytes, 0, len);
				binaryCollectionNames[c] = new String(bytes);
			}
			
			/*
			 * ***************************************************************************************************
			 * Get Bin Files
			 *****************************************************************************************************/
			
			// Total Bin Files
			int numBinFiles = source.getInt();
			
			binaryFileToCollectionMapping = new int[numBinFiles];
			binaryFileNames = new String[numBinFiles];
			binaryFileData = new byte[numBinFiles][];
			
			for(int b = 0; b < numBinFiles; b++)
			{
				// Collection Number
				binaryFileToCollectionMapping[b] = source.getInt();
				
				// File Number
				int bNum = source.getInt();
				
				if(bNum != b)
				{
					log.error("File Numbers not correct");
				}
				
				log.debug("Bin File Number : " + bNum);
				
				int len = source.getInt();
				byte[] bytes = new byte[len];
				
				log.debug("File Name Len " + len);
				
				source.get(bytes, 0, len);
				
				// FileName
				binaryFileNames[b] = new String(bytes);
				
				log.debug("File Name " + binaryFileNames[b]);
				
				// FileData
				len = source.getInt();
				bytes = new byte[len];
				log.debug("Data Len " + len);
				
				source.get(bytes, 0, len);
				binaryFileData[b] = bytes;
			}
			
		}
		
		int left = source.remaining();
		if(left > 0)
		{
			log.error("Stats not processed fully - bytes left : " + left);
		}
		
	}
	
	/*
	 * Bytes
	 */
	public byte[] toBytes()
	{
		ByteBuffer tbuffer;
		
		int size = 0;
		
		String archiveName = "stats";
		
		if((format == ExportFormat.ZXML) || (format == ExportFormat.ZCSV))
		{
			// Total files
			size += 4;
			
			// File Num Field
			size += 4;
			
			// File Name Len Field
			size += 4;
			
			// File Name Len in bytes
			size += archiveName.getBytes().length;
			
			// Data Len Field
			size += 4;
			
			// Data Len in bytes
			size += traceBinaryData.length;
			
			// Buffer
			tbuffer = ByteBuffer.allocate(size);
			
			// Total Files (Archive)
			tbuffer.putInt(1);
			
			// Write the archive to the buffer
			writeBinFileToByteBuffer(tbuffer, -1, 0, archiveName, traceBinaryData);
		}
		else
		{
			/*
			 * ***************************************************************************************************
			 * Trace Files (Calc)
			 *****************************************************************************************************/
			int numTraceFiles = traceFileNames.length;
			
			// Total Trace files
			size += 4;
			
			// Calculate Trace Size
			for(int t = 0; t < numTraceFiles; t++)
			{
				// File Num Field
				size += 4;
				// File Name Len Field
				size += 4;
				// File Name Len in bytes
				size += traceFileNames[t].getBytes().length;
				// Data Len Field
				size += 4;
				// Data Len in bytes
				size += traceTextData[t].getBytes().length;
			}
			
			/*
			 * ***************************************************************************************************
			 * Bin Collections (Calc)
			 *****************************************************************************************************/
			
			int numCollections = binaryCollectionNames.length;
			
			// Total Collections
			size += 4;
			
			for(int c = 0; c < numCollections; c++)
			{
				// File Name Len Field
				size += 4;
				
				// Collection Name
				size += binaryCollectionNames[c].getBytes().length;
			}
			
			/*
			 * ***************************************************************************************************
			 * Bin Collection Files (Calc)
			 *****************************************************************************************************/
			
			int numBinFiles = binaryFileNames.length;
			
			// Total Bin files
			size += 4;
			
			// Calculate Bin Size
			for(int b = 0; b < numBinFiles; b++)
			{
				// Collection Mapping Field
				size += 4;
				// File Num Field
				size += 4;
				// File Name Len Field
				size += 4;
				// File Name Len in bytes
				size += binaryFileNames[b].getBytes().length;
				// Data Len Field
				size += 4;
				// Data Len in bytes
				size += binaryFileData[b].length;
			}
			
			/*
			 * ***************************************************************************************************
			 * Buffer
			 *****************************************************************************************************/
			
			// Unicode strings are 16bit=2bytes char
			tbuffer = ByteBuffer.allocate(size);
			
			/*
			 * ***************************************************************************************************
			 * Put Traces
			 *****************************************************************************************************/
			
			// Total Trace Files
			tbuffer.putInt(numTraceFiles);
			
			for(int t = 0; t < numTraceFiles; t++)
			{
				writeFileToByteBuffer(tbuffer, t, traceFileNames[t], traceTextData[t]);
			}
			
			/*
			 * ***************************************************************************************************
			 * Put Collection
			 *****************************************************************************************************/
			
			// Collections
			tbuffer.putInt(numCollections);
			
			for(int c = 0; c < numCollections; c++)
			{
				// Collection Name Len Field
				tbuffer.putInt(binaryCollectionNames[c].getBytes().length);
				
				// Collection Name Data
				tbuffer.put(binaryCollectionNames[c].getBytes());
			}
			
			/*
			 * ***************************************************************************************************
			 * Put Bin Files
			 *****************************************************************************************************/
			
			// Total Bin Files
			tbuffer.putInt(numBinFiles);
			
			for(int b = 0; b < numBinFiles; b++)
			{
				writeBinFileToByteBuffer(tbuffer, binaryFileToCollectionMapping[b], b, binaryFileNames[b], binaryFileData[b]);
			}
		}
		
		return tbuffer.array();
	}
	
	private void writeFileToByteBuffer(ByteBuffer tbuffer, int fileNum, String fileName, String fileData)
	{
		// FileNum
		tbuffer.putInt(fileNum);
		
		// File Name Len
		tbuffer.putInt(fileName.getBytes().length);
		
		// File Name
		tbuffer.put(fileName.getBytes());
		
		// Data Lenth
		tbuffer.putInt(fileData.getBytes().length);
		
		// FileData
		tbuffer.put(fileData.getBytes());
	}
	
	private void writeBinFileToByteBuffer(ByteBuffer tbuffer, int collection, int fileNum, String fileName, byte[] binData)
	{
		// Negative is a bypass meaning file is not part of a collection.
		if(collection > -1)
		{
			// Number of file
			tbuffer.putInt(collection);
		}
		
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
}
