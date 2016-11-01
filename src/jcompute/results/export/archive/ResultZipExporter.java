package jcompute.results.export.archive;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JOptionPane;

public class ResultZipExporter
{
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
}
