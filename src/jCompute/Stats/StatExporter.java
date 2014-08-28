package jCompute.Stats;

import jCompute.Debug.DebugLogger;
import jCompute.Simulation.SimulationManager.Network.NSMCProtocol.Messages.NSMCP;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JOptionPane;

import com.sun.org.apache.xerces.internal.util.XMLChar;

public class StatExporter
{
	// File Format for export
	private final ExportFormat format;
	
	// File names
	private String fileNames[];
	private String fileNameSuffix;
	
	// Data
	private String data[];
	
	/**
	 * An object dedicated to exporting simulation stats
	 * Not-Thread safe.
	 * @param format
	 */
	public StatExporter(ExportFormat format,String fileNameSuffix)
	{
		this.format = format;
		this.fileNameSuffix = fileNameSuffix;
	}
	
	public void populateFromStatManager(StatManager sm)
	{
		Set<String> groupList = sm.getGroupList();
		int numFiles = groupList.size();
		
		// FileName
		fileNames = new String[numFiles];
		
		// FileData
		data = new String[numFiles];
		
		int file=0;
		for(String group : groupList)
		{
			//System.out.println("Group : " + group);

			// Set the File Name
			String fileName;
			if(!fileNameSuffix.equals(""))
			{
				fileName = group + " " + fileNameSuffix;
			}
			else
			{
				fileName = group;
			}			
			
			fileNames[file] = fileName;
			
			// File Data
			data[file] =  createStatExportString(sm.getStatGroup(group));
			
			file++;			
		}
	}
	
	/*
	 * Bytes
	 */
	public byte[] toBytes() throws IOException
	{
		int numFiles = fileNames.length;
		
		// Frame Type + Num Files
		int size = 8;
		
		// Calculate String Size
		for(int f=0;f<numFiles;f++)
		{
			// File Num Field
			size += 4;
			// File Name Len Field
			size += 4;
			// File Name Len in bytes
			size += fileNames[f].getBytes().length;
			// Data Len Field
			size += 4;
			// Data Len in bytes
			size += data[f].getBytes().length;			
		}
		
		// Unicode 16 -2bytes chart
		ByteBuffer tbuffer = ByteBuffer.allocate(size);
		
		// Frame Type
		tbuffer.putInt(NSMCP.SimStats);
		
		// Total Stat Files
		tbuffer.putInt(numFiles);
		
		for(int f=0;f<numFiles;f++)
		{
			writeFileToByteBuffer(tbuffer,f,fileNames[f],data[f]);
		}
		
		return tbuffer.array();
	}
	
	public void populateFromStream(DataInputStream source) throws IOException
	{
		System.out.println("StatExporter : populating from DataInputStream");
		
		int numFiles = source.readInt();
		
		fileNames = new String[numFiles];
		data = new String[numFiles];
		
		System.out.println("Num Files " + numFiles);
		
		for(int f=0;f<numFiles;f++)
		{
			int fNum = source.readInt();
			
			if(fNum!=f)
			{
				System.out.println("File Numbers not correct");
			}
			
			System.out.println("File Number : " + fNum);
			
			int len = source.readInt();
			byte[] stringBytes = new byte[len];
			
			System.out.println("File Name Len " + len );
			
			source.readFully(stringBytes, 0, len);
			
			// FileName
			fileNames[f] = new String(stringBytes);
			
			/* Remote filenames are sent with out a suffix
			 * Append one if required
			 */
			if(!fileNameSuffix.equals(""))
			{
				fileNames[f] += " " + fileNameSuffix;
			}
			
			System.out.println("File Name " +  fileNames[f]);

			// FileData
			len = source.readInt();
			stringBytes = new byte[len];
			System.out.println("Data Len " + len );
			
			source.readFully(stringBytes, 0, len);
			data[f] = new String(stringBytes);
			//System.out.println(data[f]);

		}
		
	}
	
	private void writeFileToByteBuffer(ByteBuffer tbuffer, int fileNum, String fileName, String fileData) throws IOException
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
	
	/*
	 * File
	 */
	
	public void exportAllStatsToDir(String directory)
	{
		int numFiles = fileNames.length;
		
		for(int f=0;f<numFiles;f++)
		{
			// Now send the strings to the output writer
			System.out.println("Writing File : " + fileNames[f]+"."+format.getExtension());
			
			writeFiles(directory,fileNames[f],data[f]);
		}
		
	}
	
	/*
	 * Data formatter
	 */
	private String createStatExportString(StatGroup statGroup)
	{
		StringBuilder data = new StringBuilder();		
		String name = statGroup.getName();
		
		if(statGroup!=null)
		{

			List<String> statList = statGroup.getStatList();
	
			if(format == ExportFormat.CSV)
			{
				// Write File Header
				addFileExportHeaderCSV(data,statList);
			}
			else if(format == ExportFormat.ARFF)
			{
				addFileExportHeaderARFF(data,name,statList);
			}
			else
			{
				addFileExportHeaderXML(data,name,statList);
			}			
			
			// Get the history length of the stats (which are all the same length in steps)
			int historyLength = statGroup.getStat(statList.get(0)).getHistoryLength();
			
			int statCount = statList.size();
			
			StatSample[][] statHistorys = new StatSample[statCount][historyLength];			
			// Convert each Linked list to arrays - so we can look up individual indexes quicker later.
			for(int statIndex=0;statIndex< statCount;statIndex++)
			{
				statHistorys[statIndex] = statGroup.getStat(statList.get(statIndex)).getHistory().toArray(new StatSample[historyLength]);
			}
	
			int history =0;
			
			// Loop for the length of the stat history (sim run length)
			while(history<historyLength)
			{
				// Write Data Row
				if(format == ExportFormat.CSV || format == ExportFormat.ARFF)
				{
					appendCSVStyleRow(data,statHistorys,history,statList);
				}
				else
				{
					appendXMLRow(data,statHistorys,history,statList);
				}	
				
				history++;
			}
	
			// File Footer
			if(format == ExportFormat.XML)
			{
				data.append("</" + xmlString(name) + ">\n");
			}
		
		}
		
		return data.toString();
	}
	
	
	/*
	 * XML
	 */
	private void addFileExportHeaderXML(StringBuilder fileData,String group,List<String> statList)
	{
		int statCount = statList.size();
		
		// DOCTYPE (DTD)
		fileData.append("<!DOCTYPE "+ xmlString(group) +"\n[\n");

		// Group contains Steps
		fileData.append("<!ELEMENT "+ xmlString(group)+" (Step)>\n");
		
		// Step Contains Stat Types
		fileData.append("<!ELEMENT Step (");
		for(int statIndex=0;statIndex< statCount;statIndex++)
		{
			fileData.append(xmlString(statList.get(statIndex)));
			if(statIndex<(statCount-1))
			{
				fileData.append(",");
			}
		}
		fileData.append(")>\n");
		
		// Each Step has an attribute which is a unique id
		fileData.append("<!ATTLIST Step id ID #REQUIRED>\n");
		
		// Each Stat is an ELEMENT
		for(int statIndex=0;statIndex< statCount;statIndex++)
		{
			fileData.append("<!ELEMENT "+ xmlString(statList.get(statIndex))+" (#PCDATA)>\n");
		}
			
		// End DOCTYPE
		fileData.append("]>\n");
		
		// XML ROOT NODE OPEN
		fileData.append("<" + xmlString(group) + ">\n");		
	}
	
	private void appendXMLRow(StringBuilder data,StatSample[][] statHistorys,int history, List<String> statList)
	{
		int statCount = statList.size();
		
		// Each Row is a Step
		data.append("\t<Step id='"+history+"'>\n");
		
		// Do the same for every history, append , after each sample or a new line after each history
		for(int statIndex=0;statIndex< statCount;statIndex++)
		{
			data.append("\t\t<"+ xmlString(statList.get(statIndex))+">"+statHistorys[statIndex][history].getSample()+"</"+ xmlString(statList.get(statIndex))+">\n");					
		}
		
		// End Step
		data.append("\t</Step>\n");
	}
	
	/**
	 * Method checks a string according to XML entity naming rules and returns a corrected string if needed.
	 * @param text
	 * @return
	 */
	private String xmlString(String text)
	{
	    StringBuilder validString = new StringBuilder();
	    	    	    
	    // XML cannot have numeric first chars or punctuation for names etc    
	    if(!XMLChar.isNameStart(text.charAt(0)))
	    {
	    	// Add a safe first char
	    	validString.append("_");
	    }

	    // Strip invalid chars
	    for(char c:text.toCharArray())
	    {
	        if(XMLChar.isName(c)) 
	        {
	        	validString.append(c);
	        }
	    }

	    // Return a valid string
	    return validString.toString();
	}
	
	/*
	 * CSV
	 */
	private void addFileExportHeaderCSV(StringBuilder fileData,List<String> statList)
	{
		// CSV Header Row			
		int statCount = statList.size();
		int statIndex = 0;
		fileData.append(statList.get(statIndex) + ",");
		
		System.out.print("Categories : " + statList.get(statIndex));
		
		for(statIndex=1;statIndex< statCount;statIndex++)
		{
			System.out.print(", " + statList.get(statIndex));

			fileData.append(statList.get(statIndex));
			
			if(statIndex<(statCount-1))
			{
				fileData.append(",");
			}
			else
			{
				fileData.append("\n");
			}
			
		}
		System.out.print("\n");
	}
	
	private void appendCSVStyleRow(StringBuilder data,StatSample[][] statHistorys,int history, List<String> statList)
	{
		int statCount = statList.size();
		
		// Append the sample from the first stat with a , appended				
		data.append(statHistorys[0][history].getSample() + ",");

		// Do the same for every history, append , after each sample or a new line after each history
		for(int statIndex=1;statIndex< statCount;statIndex++)
		{
			data.append(statHistorys[statIndex][history].getSample());
			
			if(statIndex<(statCount-1))
			{
				data.append(",");
			}
			else
			{
				data.append("\n");
			}
			
		}
	}
	
	/*
	 * ARFF
	 */
	private void addFileExportHeaderARFF(StringBuilder fileData,String group,List<String> statList)
	{
		// The ARFF HEADER
		fileData.append("% 1. Title : " + group + " Database\n");
		fileData.append("%\n");
		fileData.append("% 2. Sources :\n");
		fileData.append("%		(a) Alife Sim\n");
		fileData.append("%\n");

		// Add Relation Field
		fileData.append("@RELATION "+group+"\n");
		
		int statCount = statList.size();
		
		// The Attribute type rows
		for(int statIndex=0;statIndex<statCount;statIndex++)
		{
			// All Assumed Numeric (All stats currently numeric)
			fileData.append("@ATTRIBUTE '"+ statList.get(statIndex) + "' NUMERIC\n");
		}	
		
		// Begin Data Section
		fileData.append("@DATA\n");
		
	}

	/**
	 * Write a single file out.
	 * @param directory
	 * @param fileName
	 * @param fileData
	 * @param extension
	 */
	private void writeFiles(String directory,String fileName,String fileData)
	{
		String fileExtension = format.getExtension();

		try
		{
			String filePath;
			
			// Compress XML Output into a zip
			if(format == ExportFormat.XML)
			{
				filePath = directory+File.separator+fileName+"."+"zip";

				FileOutputStream fileOutput = new FileOutputStream(filePath);
				
				BufferedOutputStream bufferedOutput = new BufferedOutputStream(fileOutput);
				
				// Create Archive
				ZipOutputStream zipOutput = new ZipOutputStream(bufferedOutput);
				
				// Compressed or Store
				zipOutput.setMethod(ZipOutputStream.DEFLATED);
				
				// Compression level
				zipOutput.setLevel(9);
				
				// Entry start
				zipOutput.putNextEntry(new ZipEntry(fileName+"."+fileExtension));
				
				//Data
				zipOutput.write(fileData.getBytes());
				
				// Entry end
				zipOutput.closeEntry();
				
				// Archive end
				zipOutput.close();
				
				DebugLogger.output("Wrote File : " + fileName+"."+fileExtension);				
			}
			else
			{
				filePath = directory+File.separator+fileName+"."+fileExtension;
				
				FileWriter fileWriter = new FileWriter(filePath);
				BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
				
				bufferedWriter.write(fileData);
				bufferedWriter.close();

			}

			//System.out.print(filePath + "\n");

		}
		catch (IOException e)
		{
	        JOptionPane.showMessageDialog(null, e.getMessage(), "Could not Write File - " + fileName, JOptionPane.INFORMATION_MESSAGE);
		}

	}
	
	public enum ExportFormat
	{
		XML ("Extensible Markup Language" , "xml"),
		CSV ("Comma Separated Values" , "csv"),
		ARFF ("Attribute-Relation File Format" , "arff");
		
	    private final String description;
	    private final String extension;

	    private ExportFormat(String description, String extension) 
	    {
	        this.description = description;
	        this.extension = extension;
	    }
	    
	    public String getDescription()
	    {
	       return description;
	    }
	    
	    public String getExtension()
	    {
	       return extension;
	    }
	    
		public static ExportFormat fromInt(int v)
		{
			ExportFormat format = null;
			switch(v)
			{
				case 0:
					format = ExportFormat.XML;
				break;
				case 1:
					format = ExportFormat.CSV;
				break;
				case 2:
					format = ExportFormat.ARFF;
				break;
				default:
					/* Invalid Usage */
					format = null;
			}
			
			return format;
		}
	}


}