package jcompute.results.export;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jcompute.results.ResultManager;
import jcompute.results.binary.BinaryDataFile;
import jcompute.results.binary.BinaryDataFileCollection;
import jcompute.results.export.file.ExportFileWriter;
import jcompute.results.export.trace.TraceResultInf;
import jcompute.results.export.trace.TraceResults;
import jcompute.results.export.trace.TraceZipCompress;
import jcompute.util.FileUtil;

public class Result
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(Result.class);
	
	// File Format for export
	private final ExportFormat format;
	
	/*
	 * ***************************************************************************************************
	 * Trace Files
	 *****************************************************************************************************/
	
	private final String traceFileNameSuffix;
	private TraceResultInf traceResults;
	
	/*
	 * ***************************************************************************************************
	 * Binary Files
	 *****************************************************************************************************/
	
	// Collections
	private String[] binaryCollectionNames;
	
	// Files
	private int[] binaryFileToCollectionMapping;
	private String[] binaryFileNames;
	private byte[][] binaryFileData;
	
	/**
	 * An object dedicated to exporting simulation stats Not-Thread safe.
	 * 
	 * @param format
	 */
	public Result(ExportFormat format, String fileNameSuffix)
	{
		this.format = format;
		this.traceFileNameSuffix = fileNameSuffix;
	}
	
	public void populateFromResultManager(ResultManager rm)
	{
		log.info("Populating from Result Manager - " + rm.getName());
		
		/*
		 * ***************************************************************************************************
		 * Trace Files
		 *****************************************************************************************************/
		
		traceResults = new TraceResults(rm, format, traceFileNameSuffix);
		
		// Compress the trace data if required
		if((format == ExportFormat.ZXML) || (format == ExportFormat.ZCSV))
		{
			traceResults = new TraceZipCompress(traceResults, format);
		}
		
		/*
		 * ***************************************************************************************************
		 * Binary Files
		 *****************************************************************************************************/
		
		// Binary data files
		ArrayList<BinaryDataFileCollection> binDataColList = rm.getBDFList();
		
		int numCollections = binDataColList.size();
		
		binaryCollectionNames = new String[numCollections];
		
		int fileCount = 0;
		
		for(int c = 0; c < numCollections; c++)
		{
			// Record Collection Name
			binaryCollectionNames[c] = binDataColList.get(c).getName();
			
			// All data files
			ArrayList<BinaryDataFile> filesList = binDataColList.get(c).getDataFiles();
			
			// File count
			fileCount += filesList.size();
		}
		
		binaryFileToCollectionMapping = new int[fileCount];
		binaryFileNames = new String[fileCount];
		binaryFileData = new byte[fileCount][];
		
		for(int c = 0; c < numCollections; c++)
		{
			// File List
			ArrayList<BinaryDataFile> filesList = binDataColList.get(c).getDataFiles();
			
			int flSize = filesList.size();
			
			for(int f = 0; f < flSize; f++)
			{
				// Collection mapping
				binaryFileToCollectionMapping[f] = c;
				
				// FileName Name
				binaryFileNames[f] = filesList.get(f).name;
				
				// File Data
				binaryFileData[f] = filesList.get(f).bytes;
			}
		}
	}
	
	/*
	 * Bytes
	 */
	public byte[] toBytes()
	{
		ByteBuffer tbuffer;
		
		int size = 0;
		
		/*
		 * ***************************************************************************************************
		 * Trace Files + Calc
		 *****************************************************************************************************/
		
		byte[] traces = traceResults.toBytes();
		
		size += traces.length;
		
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
		
		tbuffer.put(traces);
		
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
		
		return tbuffer.array();
	}
	
	public void populateFromByteBuffer(ByteBuffer source)
	{
		log.debug("StatExporter : populating from ByteBuffer");
		
		if((format == ExportFormat.ZXML) || (format == ExportFormat.ZCSV))
		{
			/*
			 * ***************************************************************************************************
			 * Get (Compressed) Trace Files
			 *****************************************************************************************************/
			
			traceResults = new TraceZipCompress(source);
		}
		else
		{
			/*
			 * ***************************************************************************************************
			 * Get Trace Files
			 *****************************************************************************************************/
			
			traceResults = new TraceResults(source, traceFileNameSuffix);
			
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
	
	/**
	 * @param itemExportPath
	 */
	public void exportPerItemTraceResults(String itemExportPath)
	{
		traceResults.export(itemExportPath, format);
	}
	
	public void exportBinResults(String itemExportPath, String collatedStatsDir, int itemid)
	{
		/*
		 * ***************************************************************************************************
		 * Write Bin Files
		 *****************************************************************************************************/
		
		// Collate all bin stats to the top level
		String binDir = collatedStatsDir;
		
		// Bin Files
		int numBinFiles = binaryFileNames.length;
		
		if(binDir == null)
		{
			// If not collated then export to the item export dir
			binDir = itemExportPath;
			
			for(int f = 0; f < numBinFiles; f++)
			{
				// Get the collection from the mappeing
				String collection = binaryCollectionNames[binaryFileToCollectionMapping[f]];
				
				// Write the bin file within the item dir in a collection dir
				ExportFileWriter.WriteBinFile(binDir, collection + File.separator + binaryFileNames[f], binaryFileData[f]);
			}
		}
		else
		{
			// Write each file to a collated collection dir
			for(int c = 0; c < binaryCollectionNames.length; c++)
			{
				// Collection Specific Path
				String exportPaths = collatedStatsDir + File.separator + binaryCollectionNames[c];
				
				FileUtil.createDirIfNotExist(exportPaths);
			}
			
			// As these filenames may be identical we replace them with the item id padded with zeros to the filename
			String itemNameZeroPadded = String.format("%06d", itemid);
			
			for(int f = 0; f < numBinFiles; f++)
			{
				String exportDir = binaryCollectionNames[binaryFileToCollectionMapping[f]];
				
				// Keep file ext
				String ext = FileUtil.getFileNameExtension(binaryFileNames[f]);
				
				ExportFileWriter.WriteBinFile(binDir + File.separator + exportDir, itemNameZeroPadded + "." + ext, binaryFileData[f]);
			}
		}
	}
	
	public void exportAllPerItemResultsToZipArchive(ZipOutputStream zipOut, int itemId, int sampleId)
	{
		// Create Zip Directories
		try
		{
			/*
			 * ***************************************************************************************************
			 * Write Trace Files
			 *****************************************************************************************************/
			
			String[] traceFileNames = traceResults.getTraceFileNames();
			byte[][] traceTextData = traceResults.getTraceData();
			
			int numFiles = traceFileNames.length;
			
			for(int f = 0; f < numFiles; f++)
			{
				// FileName
				zipOut.putNextEntry(new ZipEntry(itemId + "/" + sampleId + "/" + traceFileNames[f] + ".csv"));
				
				// Data
				zipOut.write(traceTextData[f]);
				
				// Entry end
				zipOut.closeEntry();
			}
			
			/*
			 * ***************************************************************************************************
			 * Write Bin Files
			 *****************************************************************************************************/
			
			int numBinFiles = binaryFileNames.length;
			
			for(int f = 0; f < numBinFiles; f++)
			{
				// Get the collection from the mappeing
				String collection = binaryCollectionNames[binaryFileToCollectionMapping[f]];
				
				// Write the bin file within the item dir in a collection dir as binfilename
				zipOut.putNextEntry(new ZipEntry(collection + File.separator + binaryFileNames[f]));
				
				// Data
				zipOut.write(binaryFileData[f]);
				
				// Entry end
				zipOut.closeEntry();
			}
		}
		catch(IOException e)
		{
			log.error("Could not create export files for " + itemId);
			
			e.printStackTrace();
		}
	}
	
	public int getSize()
	{
		int traceFiles = (traceResults == null ? 0 : traceResults.getTraceFileNames().length);
		
		int binFiles = (binaryFileNames == null ? 0 : binaryFileNames.length);
		
		return(traceFiles + binFiles);
	}
}
