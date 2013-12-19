package alifeSim.Stats;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

import javax.swing.JOptionPane;

import org.apache.commons.lang.StringEscapeUtils;

import com.sun.org.apache.xerces.internal.util.XMLChar;

public class StatManager
{
	private String managerName;
	HashMap<String, StatGroup> map;
	Semaphore statsManagerLock = new Semaphore(1);
	
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
	
	/**http://tools.ietf.org/html/rfc4180#section-2
	 * field_name,field_name,field_name CRLF
     * aaa,bbb,ccc CRLF
     * zzz,yyy,xxx CRLF
	 * @param directory
	 */
	public void exportStatsToCSV(String directory)
	{
		statsManagerLock.acquireUninterruptibly();
				
		System.out.println("Export Stats to CSV");
				
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
			fileNames[groupIndex] = group;
			System.out.println("Group : " + fileNames[groupIndex]);
						
			// The Header Row			
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
			
			// The Data Rows
			// Get the history length of the stats (all the same length in steps)
			int historyLength = statGroup.getStat(statList.get(0)).getHistoryLength();
			Integer[][] statHistorys = new Integer[statCount][historyLength];
			
			// Convert each Linked list to arrays - so we can look up individual indexes quicker later.
			for(statIndex=0;statIndex< statCount;statIndex++)
			{
				statHistorys[statIndex] = statGroup.getStat(statList.get(statIndex)).getHistory().toArray(new Integer[historyLength]);
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

				// The first stat
				statIndex = 0;
				
				// Append the sample from the first stat with a , appended				
				fileData[groupIndex].append(statHistorys[statIndex][history] + ",");

				// Do the same for every history, append , after each sample or a new line after each history
				for(statIndex=1;statIndex< statCount;statIndex++)
				{
					fileData[groupIndex].append(statHistorys[statIndex][history]);
					
					if(statIndex<(statCount-1))
					{
						fileData[groupIndex].append(",");
					}
					else
					{
						fileData[groupIndex].append("\n");
					}
					
				}
				history++;
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
			writeFiles(directory,fileNames[i],fileData[i].toString(),"csv");
		}
		
		statsManagerLock.release();		
	}
	
	private void writeFiles(String directory,String fileName,String fileData,String extension)
	{
		String filePath = directory+"\\"+fileName+"."+extension;
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

	public void exportStatsToARFF(String directory)
	{
		statsManagerLock.acquireUninterruptibly();
		
		System.out.println("Export Stats to ARFF");
				
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
			// Set the File Name
			fileNames[groupIndex] = group;
			System.out.println("Group : " + fileNames[groupIndex]);
			
			// Get the Stat Group for export
			StatGroup statGroup = map.get(group);
			List<String> statList = statGroup.getStatList();
			
			// The ARFF HEADER
			fileData[groupIndex].append("% 1. Title : " + group + " Database\n");
			fileData[groupIndex].append("%\n");
			fileData[groupIndex].append("% 2. Sources :\n");
			fileData[groupIndex].append("%		(a) Alife Sim\n");
			fileData[groupIndex].append("%\n");

			// Add Relation Field
			fileData[groupIndex].append("@RELATION "+group+"\n");
												
			// The boundary's of the stats
			int statCount = statList.size();
			int statIndex = 0;
			
			// The Attribute type rows
			for(statIndex=0;statIndex<statCount;statIndex++)
			{
				// All Assumed Numeric (All stats currently numeric)
				fileData[groupIndex].append("@ATTRIBUTE '"+ statList.get(statIndex) + "' NUMERIC\n");
			}	
			
			// Begin Data Section
			fileData[groupIndex].append("@DATA\n");
			
			// The Data Rows
			// Get the history length of the stats (all the same length in steps)
			int historyLength = statGroup.getStat(statList.get(0)).getHistoryLength();
			Integer[][] statHistorys = new Integer[statCount][historyLength];
			
			// Convert each Linked list to arrays - so we can look up individual indexes quicker later.
			for(statIndex=0;statIndex< statCount;statIndex++)
			{
				statHistorys[statIndex] = statGroup.getStat(statList.get(statIndex)).getHistory().toArray(new Integer[historyLength]);
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

				// The first stat
				statIndex = 0;
				
				// Append the sample from the first stat with a , appended				
				fileData[groupIndex].append(statHistorys[statIndex][history] + ",");

				// Do the same for every history, append , after each sample or a new line after each history
				for(statIndex=1;statIndex< statCount;statIndex++)
				{
					fileData[groupIndex].append(statHistorys[statIndex][history]);
					
					if(statIndex<(statCount-1))
					{
						fileData[groupIndex].append(",");
					}
					else
					{
						fileData[groupIndex].append("\n");
					}
					
				}
				history++;
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
			writeFiles(directory,fileNames[i],fileData[i].toString(),"arff");
		}
		
		statsManagerLock.release();
	}
	
	public void exportStatsToXML(String directory)
	{
		statsManagerLock.acquireUninterruptibly();
		
		System.out.println("Export Stats to XML");
		
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
			// Set the File Name
			fileNames[groupIndex] = group;
			System.out.println("Group : " + fileNames[groupIndex]);
			
			// Get the Stat Group for export
			StatGroup statGroup = map.get(group);
			List<String> statList = statGroup.getStatList();
			
			// XML HEADER
			
			fileData[groupIndex].append("<?xml version=\"1.0\"?>\n");
						
			// The boundary's of the stats
			int statCount = statList.size();
			int statIndex = 0;
			
			// DOCTYPE (DTD)
			fileData[groupIndex].append("<!DOCTYPE "+ xmlString(group) +"\n[\n");

			// Group contains Steps
			fileData[groupIndex].append("<!ELEMENT "+ xmlString(group)+" (Step)>\n");
			
			// Step Contains Stat Types
			fileData[groupIndex].append("<!ELEMENT Step (");
			for(statIndex=0;statIndex< statCount;statIndex++)
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
			for(statIndex=0;statIndex< statCount;statIndex++)
			{
				fileData[groupIndex].append("<!ELEMENT "+ xmlString(statList.get(statIndex))+" (#PCDATA)>\n");
			}
				
			// End DOCTYPE
			fileData[groupIndex].append("]>\n");
			
			// XML ROOT NODE OPEN
			fileData[groupIndex].append("<" + xmlString(group) + ">\n");			
			
			// The Data Rows
			// Get the history length of the stats (all the same length in steps)
			int historyLength = statGroup.getStat(statList.get(0)).getHistoryLength();
			Integer[][] statHistorys = new Integer[statCount][historyLength];
			
			// Convert each Linked list to arrays - so we can look up individual indexes quicker later.
			for(statIndex=0;statIndex< statCount;statIndex++)
			{
				statHistorys[statIndex] = statGroup.getStat(statList.get(statIndex)).getHistory().toArray(new Integer[historyLength]);
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

				// The first stat
				statIndex = 0;
				
				
				// Each Row is a Step
				fileData[groupIndex].append("\t<Step id='"+history+"'>\n");
				
				// Do the same for every history, append , after each sample or a new line after each history
				for(statIndex=0;statIndex< statCount;statIndex++)
				{
					fileData[groupIndex].append("\t\t<"+ xmlString(statList.get(statIndex))+">"+statHistorys[statIndex][history]+"</"+ xmlString(statList.get(statIndex))+">\n");					
				}
				
				// End Step
				fileData[groupIndex].append("\t</Step>\n");
				
				history++;
			}
			System.out.print(" 100\n");
			
			// XML ROOT NODE CLOSE
			fileData[groupIndex].append("</" + xmlString(group) + ">\n");
			
			groupIndex++;
		}
			
		
		// Now send the strings to the output writer
		System.out.println("Writing Files");
		for(int i=0;i<fileNames.length;i++)
		{
			System.out.print(fileNames[i] + " ");			
			//System.out.print(fileData[i]);
			writeFiles(directory,fileNames[i],fileData[i].toString(),"xml");
		}
		
		statsManagerLock.release();
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

}
