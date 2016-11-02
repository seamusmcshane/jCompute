package jcompute.results.export.trace;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jcompute.results.export.ExportFormat;

public class TraceZipCompress implements TraceResultInf
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(TraceZipCompress.class);
	
	private final String[] archiveName;
	
	private final byte[][] archiveData;
	
	public TraceZipCompress(TraceResultInf traceResults, ExportFormat format)
	{
		log.info("Creating TraceZipCompress");
		
		String[] traceFileNames = traceResults.getTraceFileNames();
		byte[][] traceTextData = traceResults.getTraceData();
		
		int numFiles = traceFileNames.length;
		
		archiveName = new String[]
		{
			"traces"
		};
		
		// All files in a single archive
		archiveData = new byte[1][];
		
		// Compress the trace data if required
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
				zipOutput.write(traceTextData[f]);
				
				// Entry end
				zipOutput.closeEntry();
			}
			
			// Archive end
			zipOutput.close();
			
			//
			archiveData[0] = memoryBuffer.toByteArray();
		}
		catch(IOException e)
		{
			JOptionPane.showMessageDialog(null, e.getMessage(), "Compress Data to Zip Failed!!?", JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	public TraceZipCompress(ByteBuffer source)
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
		
		// All files in a single archive
		archiveName = new String[0];
		archiveName[0] = new String(fileName);
		
		// Bin Data Len
		len = source.getInt();
		log.debug("Data Len " + len);
		
		// All files in a single archive
		archiveData = new byte[0][len];
		
		source.get(archiveData[0], 0, len);
	}
	
	@Override
	public String[] getTraceFileNames()
	{
		return archiveName;
	}
	
	@Override
	public byte[][] getTraceData()
	{
		return archiveData;
	}
	
	@Override
	public byte[] toBytes()
	{
		/*
		 * ***************************************************************************************************
		 * Size Calc
		 *****************************************************************************************************/
		
		int size = 0;
		
		// Total files
		size += 4;
		
		// File Num Field
		size += 4;
		
		// File Name Len Field
		size += 4;
		
		// File Name Len in bytes
		size += archiveName[0].getBytes().length;
		
		// Data Len Field
		size += 4;
		
		// Data Len in bytes
		size += archiveData[0].length;
		
		/*
		 * ***************************************************************************************************
		 * Buffer
		 *****************************************************************************************************/
		
		// Buffer
		ByteBuffer tbuffer = ByteBuffer.allocate(size);
		
		/*
		 * ***************************************************************************************************
		 * Put Traces
		 *****************************************************************************************************/
		
		// Total Files (Archive)
		tbuffer.putInt(1);
		
		// Write the archive to the buffer
		writeBinFileToByteBuffer(tbuffer, 0, archiveName[0], archiveData[0]);
		
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
	
	@Override
	public void export(String directory, ExportFormat format)
	{
		try
		{
			String filePath = directory + File.separator + archiveName[0] + "." + "zip";
			// Write the memory buffer out as a file
			BufferedOutputStream fileOut = new BufferedOutputStream(new FileOutputStream(filePath));
			fileOut.write(archiveData[0]);
			fileOut.flush();
			fileOut.close();
			
			log.info("Wrote Archive : " + archiveName[0] + ".zip");
		}
		catch(IOException e)
		{
			JOptionPane.showMessageDialog(null, e.getMessage(), "Could not Write File - " + archiveName[0] + ".zip", JOptionPane.INFORMATION_MESSAGE);
		}
		
	}
}
