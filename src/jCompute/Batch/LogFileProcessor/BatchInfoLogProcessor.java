package jCompute.Batch.LogFileProcessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class BatchInfoLogProcessor
{
	// Batch
	private int batchId;
	private String scenarioType;
	private String description;
	private String baseFile;
	
	// Items
	private int items;
	private int itemSamples;
	private int uniqueItems;
	private int maxSteps;
	
	// Timing
	private String addedDateTime;
	private String startDateTime;
	private String finishedDateTime;
	private String totalTime;
	
	// Performance
	private long cpuTotalTime;
	private long cpuAvgTime;
	private long iOTotalTime;
	private long iOAvgTime;
	private long itemTotalTime;
	private long itemAvgTime;
	
	// Parameter List
	private ArrayList<String> parameters;
	
	public BatchInfoLogProcessor(String fileName) throws IOException
	{
		File file = new File(fileName);
		
		parameters = new ArrayList<String>();
		
		BufferedReader inputFile = new BufferedReader(new FileReader(file));
		
		boolean finished = false;
		while(!finished)
		{
			String line = inputFile.readLine();
			if(line != null)
			{
				parseLine(line);
			}
			else
			{
				finished = true;
			}
		}
		
		inputFile.close();
	}
	
	private void parseLine(String line)
	{
		// System.out.println("Line " + line);
		
		String part1 = line.substring(0, line.lastIndexOf('='));
		String part2 = line.substring(line.lastIndexOf('=') + 1, line.length());;
		
		// System.out.println("part1 " + part1);
		// System.out.println("part2 " + part2);
		
		if(part1.equals("BatchId"))
		{
			batchId = Integer.parseInt(part2);
		}
		else if(part1.equals("ScenarioType"))
		{
			scenarioType = part2;
		}
		else if(part1.equals("Description"))
		{
			description = part2;
		}
		else if(part1.equals("BaseFile"))
		{
			baseFile = part2;
		}
		else if(part1.equals("Items"))
		{
			items = Integer.parseInt(part2);
		}
		else if(part1.equals("ItemSamples"))
		{
			itemSamples = Integer.parseInt(part2);
		}
		else if(part1.equals("UniqueItems"))
		{
			uniqueItems = Integer.parseInt(part2);
		}
		else if(part1.equals("MaxSteps"))
		{
			maxSteps = Integer.parseInt(part2);
		}
		else if(part1.equals("AddedDateTime"))
		{
			addedDateTime = part2;
		}
		else if(part1.equals("StartDateTime"))
		{
			startDateTime = part2;
		}
		else if(part1.equals("FinishedDateTime"))
		{
			finishedDateTime = part2;
		}
		else if(part1.equals("TotalTime"))
		{
			totalTime = part2;
		}
		else if(part1.equals("CpuTotalTime"))
		{
			cpuTotalTime = Long.parseLong(part2);
		}
		else if(part1.equals("CpuAvgTime"))
		{
			cpuAvgTime = Long.parseLong(part2);
		}
		else if(part1.equals("IOTotalTime"))
		{
			iOTotalTime = Long.parseLong(part2);
		}
		else if(part1.equals("IOAvgTime"))
		{
			iOAvgTime = Long.parseLong(part2);
		}
		else if(part1.equals("ItemTotalTime"))
		{
			itemTotalTime = Long.parseLong(part2);
		}
		else if(part1.equals("ItemAvgTime"))
		{
			itemAvgTime = Long.parseLong(part2);
		}
		else
		{
			// Read Parameter (0) ....
			if(part1.length() > 0)
			{
				if(part1.charAt(0) == '(')
				{
					parameters.add(part1);
					parameters.add(part2);
				}
				else
				{
					System.out.println("Unknown BatchInfo field : " + part1 + " value " + part2);
				}
			}
			else
			{
				System.out.println("BatchInfo NULL");
			}
			
		}
	}
	
	public ArrayList<String> dump()
	{
		ArrayList<String> info = new ArrayList<String>();
		
		info.add("Batch Id");
		info.add(String.valueOf(getBatchId()));
		info.add("Scenario Type");
		info.add(getScenarioType());
		info.add("Description");
		info.add(getDescription());
		info.add("BaseFile");
		info.add(getBaseFile());
		info.add("");
		info.add("");
		info.add("Items");
		info.add(String.valueOf(getItems()));
		info.add("ItemSamples");
		info.add(String.valueOf(getItemSamples()));
		info.add("UniqueItems");
		info.add(String.valueOf(getUniqueItems()));
		info.add("MaxSteps");
		info.add(String.valueOf(getMaxSteps()));
		info.add("");
		info.add("");
		info.add("addedDateTime");
		info.add(addedDateTime);
		info.add("startDateTime");
		info.add(startDateTime);
		info.add("finishedDateTime");
		info.add(finishedDateTime);
		info.add("totalTime");
		info.add(totalTime);
		info.add("");
		info.add("");
		info.add("cpuTotalTime");
		info.add(String.valueOf(cpuTotalTime));
		info.add("cpuAvgTime");
		info.add(String.valueOf(cpuAvgTime));
		info.add("iOTotalTime");
		info.add(String.valueOf(iOTotalTime));
		info.add("iOAvgTime");
		info.add(String.valueOf(iOAvgTime));
		info.add("itemTotalTime");
		info.add(String.valueOf(itemTotalTime));
		info.add("itemAvgTime");
		info.add(String.valueOf(itemAvgTime));
		info.add("");
		info.add("");
		info.addAll(parameters);
		
		System.out.println("Batch Id " + getBatchId());
		// Batch
		System.out.println("scenarioType :" + getScenarioType());
		System.out.println("description : " + getDescription());
		System.out.println("baseFile : " + getBaseFile());
		
		// Items
		System.out.println("items : " + getItems());
		System.out.println("itemSamples : " + getItemSamples());
		System.out.println("uniqueItems : " + getUniqueItems());
		System.out.println("maxSteps : " + getMaxSteps());
		
		// Timing
		System.out.println("addedDateTime : " + addedDateTime);
		System.out.println("startDateTime : " + startDateTime);
		System.out.println("finishedDateTime : " + finishedDateTime);
		System.out.println("totalTime : " + totalTime);
		
		// Performance
		System.out.println("cpuTotalTime : " + cpuTotalTime);
		System.out.println("cpuAvgTime : " + cpuAvgTime);
		System.out.println("iOTotalTime :" + iOTotalTime);
		System.out.println("iOAvgTime : " + iOAvgTime);
		System.out.println("itemTotalTime : " + itemTotalTime);
		System.out.println("itemAvgTime : " + itemAvgTime);
		
		return info;
	}
	
	public int getBatchId()
	{
		return batchId;
	}
	
	public String getScenarioType()
	{
		return scenarioType;
	}
	
	public String getDescription()
	{
		return description;
	}
	
	public String getBaseFile()
	{
		return baseFile;
	}
	
	public int getItems()
	{
		return items;
	}
	
	public int getItemSamples()
	{
		return itemSamples;
	}
	
	public int getUniqueItems()
	{
		return uniqueItems;
	}
	
	public int getMaxSteps()
	{
		return maxSteps;
	}
}
