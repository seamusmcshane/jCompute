package jcompute.results.export.bdfc;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jcompute.results.ResultManager;
import jcompute.results.binary.BinaryDataFile;
import jcompute.results.binary.BinaryDataFileCollection;
import jcompute.results.export.file.ExportFileWriter;
import jcompute.util.FileUtil;

public class BDFCResult
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(BDFCResult.class);
	
	// Collections
	private String[] binaryCollectionNames;
	
	// Files
	private int[] binaryFileToCollectionMapping;
	private String[] binaryFileNames;
	private byte[][] binaryFileData;
	
	public BDFCResult(ResultManager rm)
	{
		log.info("Creating BDFCResult from Result Manager - " + rm.getName());
		
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
	
	public BDFCResult(ByteBuffer source)
	{
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
	
	public String[] getBinFileNames()
	{
		return binaryFileNames;
	}
	
	public byte[] toBytes()
	{
		int size = 0;
		
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
		
		// Unicode strings are 16bit=2bytes char
		ByteBuffer tbuffer = ByteBuffer.allocate(size);
		
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
	
	public void export(String itemExportPath, String collatedStatsDir, int itemid)
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
}
