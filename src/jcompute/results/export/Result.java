package jcompute.results.export;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jcompute.results.ResultManager;
import jcompute.results.binary.BinaryDataFile;
import jcompute.results.binary.BinaryDataFileCollection;
import jcompute.results.export.file.ExportFileWriter;
import jcompute.results.export.format.ARFFExporter;
import jcompute.results.export.format.CSVExporter;
import jcompute.results.export.format.XMLExporter;
import jcompute.results.trace.Trace;
import jcompute.results.trace.group.TraceGroup;
import jcompute.results.trace.samples.TraceSample;
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
	
	// File names
	private String traceFileNames[];
	private String traceFileNameSuffix;
	
	// Data
	private String traceTextData[];
	private byte traceBinaryData[];
	
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
		
		Set<String> groupList = rm.getTraceGroupNames();
		int numFiles = groupList.size();
		
		// FileName
		traceFileNames = new String[numFiles];
		
		// FileData
		traceTextData = new String[numFiles];
		
		int file = 0;
		for(String group : groupList)
		{
			// Set the File Name
			String fileName;
			if(!traceFileNameSuffix.equals(""))
			{
				fileName = group + " " + traceFileNameSuffix;
			}
			else
			{
				fileName = group;
			}
			
			log.info("Adding " + fileName);
			
			traceFileNames[file] = fileName;
			
			// File Data
			traceTextData[file] = createStatExportString(rm.getTraceGroup(group));
			
			file++;
		}
		
		// Compress the trace data if required
		if((format == ExportFormat.ZXML) || (format == ExportFormat.ZCSV))
		{
			try
			{
				String fileExtension = format.getExtension();
				
				// Memory Buffer
				ByteArrayOutputStream memoryBuffer = new ByteArrayOutputStream();
				
				// Create Archive
				ZipOutputStream zipOutput = new ZipOutputStream(memoryBuffer);
				
				// Compression Method - DEFLATED == ZipEntry.DEFLATED
				zipOutput.setMethod(ZipOutputStream.DEFLATED);
				
				// Compression level for DEFLATED
				zipOutput.setLevel(Deflater.BEST_COMPRESSION);
				
				for(int f = 0; f < numFiles; f++)
				{
					// Entry start
					zipOutput.putNextEntry(new ZipEntry(traceFileNames[f] + "." + fileExtension));
					
					// Data
					zipOutput.write(traceTextData[f].getBytes());
					
					// Entry end
					zipOutput.closeEntry();
				}
				
				// Archive end
				zipOutput.close();
				
				traceBinaryData = memoryBuffer.toByteArray();
			}
			catch(IOException e)
			{
				JOptionPane.showMessageDialog(null, e.getMessage(), "Compress Data to Zip Failed!!?", JOptionPane.INFORMATION_MESSAGE);
			}
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
	
	/*
	 * File
	 */
	public void exportTraceResults(String itemExportPath)
	{
		if((format == ExportFormat.ZXML) || (format == ExportFormat.ZCSV))
		{
			/*
			 * ***************************************************************************************************
			 * Write Compressed Trace Files
			 *****************************************************************************************************/
			writeZipArchive(itemExportPath, traceFileNameSuffix);
		}
		else
		{
			/*
			 * ***************************************************************************************************
			 * Write Trace Files
			 *****************************************************************************************************/
			int numFiles = traceFileNames.length;
			
			for(int f = 0; f < numFiles; f++)
			{
				ExportFileWriter.WriteTextFile(itemExportPath, traceFileNames[f], traceTextData[f], format);
			}
		}
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
	
	public void exportAllStatsToZipDir(ZipOutputStream zipOut, int itemId, int sampleId)
	{
		// Create Zip Directories
		try
		{
			/*
			 * ***************************************************************************************************
			 * Write Trace Files
			 *****************************************************************************************************/
			
			int numFiles = traceFileNames.length;
			
			for(int f = 0; f < numFiles; f++)
			{
				// FileName
				zipOut.putNextEntry(new ZipEntry(itemId + "/" + sampleId + "/" + traceFileNames[f] + ".csv"));
				
				// Data
				zipOut.write(traceTextData[f].getBytes());
				
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
	
	private void writeZipArchive(String directory, String name)
	{
		String archiveName = name;
		
		if(name.equals(""))
		{
			archiveName = "stats";
		}
		
		try
		{
			String filePath = directory + File.separator + archiveName + "." + "zip";
			// Write the memory buffer out as a file
			BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(filePath));
			fileOut.write(traceBinaryData);
			fileOut.flush();
			fileOut.close();
			
			log.info("Wrote Archive : " + archiveName + ".zip");
		}
		catch(IOException e)
		{
			JOptionPane.showMessageDialog(null, e.getMessage(), "Could not Write File - " + archiveName + ".zip", JOptionPane.INFORMATION_MESSAGE);
		}
		
	}
	
	/*
	 * Data formatter
	 */
	private String createStatExportString(TraceGroup traceGroup)
	{
		StringBuilder data = new StringBuilder();
		
		if(traceGroup != null)
		{
			String name = traceGroup.getName();
			
			List<String> traceList = traceGroup.getTraceList();
			
			if((format == ExportFormat.CSV) || (format == ExportFormat.ZCSV))
			{
				// Write File Header
				CSVExporter.AddFileExportHeaderCSV(data, traceList);
			}
			else if(format == ExportFormat.ARFF)
			{
				ARFFExporter.AddFileExportHeaderARFF(data, name, traceList);
			}
			else
			{
				XMLExporter.AddFileExportHeaderXML(data, name, traceList);
			}
			
			// Get the history length of the stats (which are all the same
			// length in steps)
			int historyLength = traceGroup.getTrace(traceList.get(0)).getHistoryLength();
			
			int traceCount = traceList.size();
			
			TraceSample[][] traceHistorys = new TraceSample[traceCount][];
			
			// Convert each Linked list to arrays - so we can look up individual
			// indexes quicker later.
			for(int traceIndex = 0; traceIndex < traceCount; traceIndex++)
			{
				Trace trace = traceGroup.getTrace(traceList.get(traceIndex));
				
				traceHistorys[traceIndex] = trace.getHistoryAsArray();
				
				// traceHistorys[statIndex] = traceGroup.getTrace(traceList.get(statIndex)).getHistory().toArray(new StatSample[historyLength]);
			}
			
			int history = 0;
			
			// Loop for the length of the stat history (sim run length)
			while(history < historyLength)
			{
				// Write Data Row
				if((format == ExportFormat.CSV) || (format == ExportFormat.ARFF) || (format == ExportFormat.ZCSV))
				{
					CSVExporter.AppendCSVRow(data, traceHistorys, history, traceList);
				}
				else
				{
					XMLExporter.AppendXMLRow(data, traceHistorys, history, traceList);
				}
				
				history++;
			}
			
			// File Footer
			if((format == ExportFormat.XML) || (format == ExportFormat.ZXML))
			{
//				data.append("</" + xmlString(name) + ">\n");
				
				XMLExporter.AppendCloseSection(data, name);
			}
			
		}
		
		return data.toString();
	}
	
	public int getSize()
	{
		int traceFiles = (traceFileNames == null ? 0 : traceFileNames.length);
		
		int binFiles = (binaryFileNames == null ? 0 : binaryFileNames.length);
		
		return(traceFiles + binFiles);
	}
}
