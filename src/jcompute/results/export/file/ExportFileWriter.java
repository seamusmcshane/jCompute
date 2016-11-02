package jcompute.results.export.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jcompute.results.export.ExportFormat;

public class ExportFileWriter
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(ExportFileWriter.class);
	
	/**
	 * Write a single file out.
	 * 
	 * @param directory
	 * @param fileName
	 * @param fileData
	 * @param extension
	 */
	public static void WriteTextFile(String directory, String fileName, String fileData, ExportFormat format)
	{
		String fileExtension = format.getExtension();
		
		try
		{
			String filePath = directory + File.separator + fileName + "." + fileExtension;
			
			BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(filePath));
			
			bufferedWriter.write(fileData);
			bufferedWriter.close();
			
			// Now send the strings to the output writer
			log.info("Wrote File : " + fileName + "." + fileExtension);
		}
		catch(IOException e)
		{
			JOptionPane.showMessageDialog(null, e.getMessage(), "Could not Write File - " + fileName, JOptionPane.INFORMATION_MESSAGE);
		}
		
	}
	
	public static void WriteBinFile(String directory, String fileName, byte[] fileData, ExportFormat format)
	{
		String fileExtension = format.getExtension();
		
		try
		{
			String filePath = directory + File.separator + fileName + "." + fileExtension;
			
			File file = new File(filePath);
			file.getParentFile().mkdirs();
			
			FileOutputStream stream = new FileOutputStream(new File(filePath));
			
			stream.write(fileData);
			stream.close();
			
			log.info("Wrote File : " + fileName);
		}
		catch(IOException e)
		{
			JOptionPane.showMessageDialog(null, e.getMessage(), "Could not Write File - " + fileName, JOptionPane.INFORMATION_MESSAGE);
		}
	}
	
	public static void WriteBinFile(String directory, String fileName, byte[] fileData)
	{
		try
		{
			String filePath = directory + File.separator + fileName;
			
			File file = new File(filePath);
			file.getParentFile().mkdirs();
			
			FileOutputStream stream = new FileOutputStream(new File(filePath));
			
			stream.write(fileData);
			stream.close();
			
			log.info("Wrote File : " + fileName);
		}
		catch(IOException e)
		{
			JOptionPane.showMessageDialog(null, e.getMessage(), "Could not Write File - " + fileName, JOptionPane.INFORMATION_MESSAGE);
		}
	}
}
