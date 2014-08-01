package jCompute.Stats;

import jCompute.Debug.DebugLogger;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JOptionPane;

import com.sun.org.apache.xerces.internal.util.XMLChar;

public class StatManager
{
	private String managerName;
	
	private HashMap<String, StatGroup> map;
	
	private ArrayList<StatGroup> statGroupList;
	
	private Semaphore statsManagerLock = new Semaphore(1);
	
	public StatManager(String managerName)
	{
		this.managerName = managerName;
		map = new HashMap<String, StatGroup>();
	}
	
	// Add a new stat to the stat manager
	public void registerGroup(StatGroup group)
	{		
		statsManagerLock.acquireUninterruptibly();
			map.put(group.getName(), group);
		statsManagerLock.release();
	}
	
	// Add a list of group to the stat manager
	public void registerGroups(List<StatGroup> groupList)
	{
		for (StatGroup group : groupList) 
		{
			registerGroup(group);
		}			
	}
	
	public void setGroupSettings(String groupName,StatGroupSetting setting)
	{
		statsManagerLock.acquireUninterruptibly();
			StatGroup group = map.get(groupName);
			group.setGroupSettings(setting);
		statsManagerLock.release();
	}
	
	// returns a group based on the group name requested
	public StatGroup getStatGroup(String groupName)
	{
		statsManagerLock.acquireUninterruptibly();
			StatGroup group = map.get(groupName);
		statsManagerLock.release();
		
		return group;
	}
	
	public int getStatGroupCount(String groupName)
	{
		statsManagerLock.acquireUninterruptibly();
			int statCount = map.size();
		statsManagerLock.release();
		return statCount;
	}
	
	// An unsorted list of the Stat names in the manager
	public Set<String> getGroupList()
	{
		return map.keySet();
	}
	
	public String getname()
	{
		return managerName;
	}
		
	public boolean containsGroup(String name)
	{
		statsManagerLock.acquireUninterruptibly();
			boolean status = map.containsKey(name);
		statsManagerLock.release();
		
		return status;
	}
	
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
	
	public void addFileExportHeaderARFF(StringBuilder fileData,String group,List<String> statList)
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
	
	public void addFileExportHeaderXML(StringBuilder fileData,String group,List<String> statList)
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
	
	
	public String[] getStats(String format)
	{
		return new String[]{""};
	}
	
	public void exportAllStatsToDir(String directory,String fileNameSuffix, String format)
	{
		statsManagerLock.acquireUninterruptibly();

		Set<String> groupList = getGroupList();
		
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
			
			
			// File Data
			String data = createStatExportString(group,format);
			
			// Now send the strings to the output writer
			//System.out.println("Writing File : " + fileName);
			
			writeFiles(directory,fileName,data,format.toLowerCase());
			
		}
		
		statsManagerLock.release();
	}
	
	private String createStatExportString(String group,String format)
	{
		StringBuilder data = new StringBuilder();		
		
		// Get the Stat Group for export
		StatGroup statGroup = map.get(group);
		
		if(statGroup!=null)
		{

			List<String> statList = statGroup.getStatList();
	
			if(format.equalsIgnoreCase("csv"))
			{
				// Write File Header
				addFileExportHeaderCSV(data,statList);
			}
			else if(format.equalsIgnoreCase("arff"))
			{
				addFileExportHeaderARFF(data,group,statList);
			}
			else
			{
				addFileExportHeaderXML(data,group,statList);
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
				if(format.equalsIgnoreCase("csv") || format.equalsIgnoreCase("arff"))
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
			if(format.equalsIgnoreCase("xml"))
			{
				data.append("</" + xmlString(group) + ">\n");
			}
		
		}
		
		return data.toString();
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
	
	private void writeFiles(String directory,String fileName,String fileData,String extension)
	{

	
		try
		{
			String filePath;
			
			// Compress XML Output into a zip
			if(extension.equalsIgnoreCase("xml"))
			{
				//GZIPOutputStream gzip = new GZIPOutputStream(new FileOutputStream(new File(filePath)));
				//bufferedWriter = new BufferedWriter(new OutputStreamWriter(gzip, "UTF-8"));

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
				zipOutput.putNextEntry(new ZipEntry(fileName+"."+extension));
				
				//Data
				zipOutput.write(fileData.getBytes());
				
				// Entry end
				zipOutput.closeEntry();
				
				// Archive end
				zipOutput.close();
				
				DebugLogger.output("Wrote File : " + fileName+"."+extension);
				
			}
			else
			{

				filePath = directory+File.separator+fileName+"."+extension;
				
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

	private List<StatGroup> getStatGroupList()
	{
		if(statGroupList == null)
		{
			statGroupList = new ArrayList<StatGroup>();
		}		
		
		if(statGroupList.size() != map.size())
		{
			statGroupList = new ArrayList<StatGroup>();
			
			Set<String> groupList = getGroupList();
			
			for(String group : groupList)
			{
				statGroupList.add(map.get(group));
			}			
		}
		
		return statGroupList;
	}
	
	public void notifiyStatListeners()
	{
		for (StatGroup group : getStatGroupList()) 
		{
			group.notifyStatGroupListeners();
		}
	}
	
	public void endEventNotifiyStatListeners()
	{
		for (StatGroup group : getStatGroupList()) 
		{
			group.endEventNotifyStatGroupListeners();
		}
	}

}
