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
				
		System.out.println("Export Stats");
				
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
			
			// Loop for the lenght of the stat history (sim run length)
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
}
