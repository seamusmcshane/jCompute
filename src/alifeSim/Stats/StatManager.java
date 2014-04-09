package alifeSim.Stats;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

import javax.swing.JOptionPane;

import alifeSim.Gui.Charts.GlobalStatChartPanel;

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
	
	private void addFileExportHeaderCSV(StringBuilder[] fileData,int groupIndex,List<String> statList)
	{
		// CSV Header Row			
		int statCount = statList.size();
		int statIndex = 0;
		fileData[groupIndex].append(statList.get(statIndex) + ",");
		
		System.out.print("Categories : " + statList.get(statIndex));
		
		for(statIndex=1;statIndex< statCount;statIndex++)
		{
			System.out.print(", " + statList.get(statIndex));

			fileData[groupIndex].append(statList.get(statIndex));
			
			if(statIndex<(statCount-1))
			{
				fileData[groupIndex].append(",");
			}
			else
			{
				fileData[groupIndex].append("\n");
			}
			
		}
		System.out.print("\n");
	}
	
	public void addFileExportHeaderARFF(StringBuilder[] fileData,int groupIndex,String group,List<String> statList)
	{
		// The ARFF HEADER
		fileData[groupIndex].append("% 1. Title : " + group + " Database\n");
		fileData[groupIndex].append("%\n");
		fileData[groupIndex].append("% 2. Sources :\n");
		fileData[groupIndex].append("%		(a) Alife Sim\n");
		fileData[groupIndex].append("%\n");

		// Add Relation Field
		fileData[groupIndex].append("@RELATION "+group+"\n");
		
		int statCount = statList.size();
		
		// The Attribute type rows
		for(int statIndex=0;statIndex<statCount;statIndex++)
		{
			// All Assumed Numeric (All stats currently numeric)
			fileData[groupIndex].append("@ATTRIBUTE '"+ statList.get(statIndex) + "' NUMERIC\n");
		}	
		
		// Begin Data Section
		fileData[groupIndex].append("@DATA\n");
		
	}
	
	public void addFileExportHeaderXML(StringBuilder[] fileData,int groupIndex,String group,List<String> statList)
	{
		int statCount = statList.size();
		
		// DOCTYPE (DTD)
		fileData[groupIndex].append("<!DOCTYPE "+ xmlString(group) +"\n[\n");

		// Group contains Steps
		fileData[groupIndex].append("<!ELEMENT "+ xmlString(group)+" (Step)>\n");
		
		// Step Contains Stat Types
		fileData[groupIndex].append("<!ELEMENT Step (");
		for(int statIndex=0;statIndex< statCount;statIndex++)
		{
			fileData[groupIndex].append(xmlString(statList.get(statIndex)));
			if(statIndex<(statCount-1))
			{
				fileData[groupIndex].append(",");
			}
		}
		fileData[groupIndex].append(")>\n");
		
		// Each Step has an attribute which is a unique id
		fileData[groupIndex].append("<!ATTLIST Step id ID #REQUIRED>\n");
		
		// Each Stat is an ELEMENT
		for(int statIndex=0;statIndex< statCount;statIndex++)
		{
			fileData[groupIndex].append("<!ELEMENT "+ xmlString(statList.get(statIndex))+" (#PCDATA)>\n");
		}
			
		// End DOCTYPE
		fileData[groupIndex].append("]>\n");
		
		// XML ROOT NODE OPEN
		fileData[groupIndex].append("<" + xmlString(group) + ">\n");		
	}
	
	public void exportStats(String directory,String fileNameSuffix, String format)
	{
		statsManagerLock.acquireUninterruptibly();
		
		System.out.println("Exporting Stats");
		
		// The Stats Groups
		Set<String> groupList = getGroupList();
		
		// Each stat group is a new file to output
		String[] fileNames = new String[groupList.size()];
		for(String fileName: fileNames)
		{
			fileName = new String();
		}
		
		// For efficient concatenation we use StringBuilder to build up the output file
		StringBuilder[] fileData = new StringBuilder[groupList.size()];
		for(int i=0;i<fileData.length;i++)
		{
			fileData[i] = new StringBuilder();
		}
		
		System.out.println("Processing Statistic Groups");
		int groupIndex = 0;
		
		// Loop over each stat group
		for (String group : groupList) 
		{
			// Get the Stat Group for export
			StatGroup statGroup = map.get(group);
			List<String> statList = statGroup.getStatList();
			
			// Set the File Name
			if(!fileNameSuffix.equals(""))
			{
				fileNames[groupIndex] = group + " " + fileNameSuffix;
			}
			else
			{
				fileNames[groupIndex] = group;
			}
			System.out.println("Group : " + fileNames[groupIndex]);
						

			if(format.equalsIgnoreCase("csv"))
			{
				// Write File Header
				addFileExportHeaderCSV(fileData,groupIndex,statList);
			}
			else if(format.equalsIgnoreCase("arff"))
			{
				addFileExportHeaderARFF(fileData,groupIndex,group,statList);
			}
			else
			{
				addFileExportHeaderXML(fileData,groupIndex,group,statList);
			}			
			
			// The Data Rows
			
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
			System.out.print("DataRows for " + group + " Progress :");
			
			// For progress output
			int percentage = 0;
			
			// So we don't spam more than once.
			boolean progressTrigger = false;
			
			// Loop for the length of the stat history (sim run length)
			while(history<historyLength)
			{
				// Calculate the %progress
				percentage = (int)(( ((float)history) / ((float)historyLength))*100);				
				
				// Output every 10%
				if( ((percentage%10) == 0) )
				{
					// Dont spam for each sub interval  i.e. 10.0% to 10.9%
					if(!progressTrigger)
					{	
						System.out.print(" " + percentage);
						progressTrigger = true;
					}
				}
				else
				{
					progressTrigger = false;
				}
				

				// Write Data Row
				if(format.equalsIgnoreCase("csv") || format.equalsIgnoreCase("arff"))
				{
					appendCSVStyleRow(fileData,groupIndex,statHistorys,history,statList);
				}
				else
				{
					appendXMLRow(fileData,groupIndex,statHistorys,history,statList);
				}	
				
				history++;
			}

			// File Footer
			if(format.equalsIgnoreCase("xml"))
			{
				fileData[groupIndex].append("</" + xmlString(group) + ">\n");
			}
			
			System.out.print(" 100\n");
			groupIndex++;
		}

		// Now send the strings to the output writer
		System.out.println("Writing Files");
		for(int i=0;i<fileNames.length;i++)
		{
			System.out.print(fileNames[i] + " ");			
			//System.out.print(fileData[i]);
			writeFiles(directory,fileNames[i],fileData[i].toString(),format.toLowerCase());
		}
		
		statsManagerLock.release();		
	}
		
	private void appendXMLRow(StringBuilder[] fileData,int groupIndex,StatSample[][] statHistorys,int history, List<String> statList)
	{
		int statCount = statList.size();
		
		// Each Row is a Step
		fileData[groupIndex].append("\t<Step id='"+history+"'>\n");
		
		// Do the same for every history, append , after each sample or a new line after each history
		for(int statIndex=0;statIndex< statCount;statIndex++)
		{
			fileData[groupIndex].append("\t\t<"+ xmlString(statList.get(statIndex))+">"+statHistorys[statIndex][history].getSample()+"</"+ xmlString(statList.get(statIndex))+">\n");					
		}
		
		// End Step
		fileData[groupIndex].append("\t</Step>\n");
	}
	
	private void appendCSVStyleRow(StringBuilder[] fileData,int groupIndex,StatSample[][] statHistorys,int history, List<String> statList)
	{
		int statCount = statList.size();
		
		// Append the sample from the first stat with a , appended				
		fileData[groupIndex].append(statHistorys[0][history].getSample() + ",");

		// Do the same for every history, append , after each sample or a new line after each history
		for(int statIndex=1;statIndex< statCount;statIndex++)
		{
			fileData[groupIndex].append(statHistorys[statIndex][history].getSample());
			
			if(statIndex<(statCount-1))
			{
				fileData[groupIndex].append(",");
			}
			else
			{
				fileData[groupIndex].append("\n");
			}
			
		}
	}
	
	private void writeFiles(String directory,String fileName,String fileData,String extension)
	{
		String filePath = directory+File.separator+fileName+"."+extension;
		System.out.print(filePath + "\n");
	
		try
		{
			FileWriter fileWriter = new FileWriter(filePath);
			
			BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

			bufferedWriter.write(fileData);
			bufferedWriter.close();
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
