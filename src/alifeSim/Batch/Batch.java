package alifeSim.Batch;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

import alifeSim.Debug.DebugLogger;
import alifeSim.Scenario.ScenarioInf;
import alifeSim.Scenario.ScenarioVT;
import alifeSim.Scenario.Math.LotkaVolterra.LotkaVolterraScenario;
import alifeSim.Scenario.SAPP.SAPPScenario;
import alifeSim.Simulation.SimulationManager.SimulationsManagerInf;

public class Batch
{
	/* Batch Attributes */
	private int batchId;
	private String type;
	private String baseScenarioFile;
	private String batchDescription;
	
	private int itemsRequested = 0;
	
	private int batchItems = 0;
	private int completedItems = 0;
	private int itemSamples;
	
	/* Log - total time calc */
	private long startTime;
	
	/* Completed Items avg */
	private long completedItemRunTime;
	
	private String batchStatsExportDir;
	private PrintWriter itemLog;
	private String ParameterName[];
	private String GroupName[];
	private PrintWriter infoLog;
	
	// Our Queue of Items yet to be processed
	private LinkedList<BatchItem> queuedItems;
	
	// The active Items currently being processed.
	private ArrayList<BatchItem> activeItems;
	private int active;
	
	// The Batch Configuration Text
	private StringBuilder batchConfigText;
	
	// The Configuration Processor
	private ScenarioVT batchConfigProcessor;
	
	// The base scenario
	private String baseScenaroFilePath;
	private String basePath;
	
	// Base scenario text
	private StringBuilder baseScenarioText;

	// The Base scenario
	private ScenarioInf baseScenario;
	
	private Semaphore batchLock = new Semaphore(1, false);	
	
	public Batch(int batchId,String fileName) throws IOException
	{
		completedItemRunTime = 0;
		active = 0;
		
		this.batchId = batchId;
		
		queuedItems = new LinkedList<BatchItem>();
			
		activeItems = new ArrayList<BatchItem>();
		
		batchConfigText = new StringBuilder();
		
		basePath = getPath(fileName);
		
		DebugLogger.output("Base Path : " + basePath);	
		
		// Put the file text into the string builder
		readFile(fileName,batchConfigText);
		
		DebugLogger.output("New Batch based on : " + fileName);
		
		processBatchConfig(batchConfigText.toString());
	}
		
	private String getPath(String fileName)
	{
		return Paths.get(fileName).getParent().toString();
	}

	private void processBatchConfig(String fileText) throws IOException
	{
		batchLock.acquireUninterruptibly();
		
		batchConfigProcessor = new ScenarioVT();
		
		batchConfigProcessor.loadConfig(batchConfigText.toString());
		
		batchConfigProcessor.dumpXML();
		
		setBatchDescription();

		setBatchStatExportDir();
		
		setBaseFilePath(fileText);
		
		baseScenarioText = new StringBuilder();

		readFile(baseScenaroFilePath,baseScenarioText);
			
		baseScenario = determinScenarios(baseScenarioText.toString());		
				
		type = baseScenario.getScenarioType();
		DebugLogger.output(type);
		
		generateCombos();
		
		batchLock.release();		
	}

	private void startBatchLog(int numCordinates)
	{
		try
		{
			infoLog = new PrintWriter(new BufferedWriter(new FileWriter(batchStatsExportDir+File.separator+"InfoLog.xml", true)));
			itemLog = new PrintWriter(new BufferedWriter(new FileWriter(batchStatsExportDir+File.separator+"ItemLog.xml", true)));
			
			/* Common Log Header */
			itemLog.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
			itemLog.println("<Log xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"schemas/batchItemLog.xsd\">");
			itemLog.println("<Header>");
			itemLog.println("<Name>"+ batchDescription +"</Name>");
			itemLog.println("<LogType>" + "BatchItems" +"</LogType>");
			itemLog.println("<SamplesPerItem>" + itemSamples +"</SamplesPerItem>");
			itemLog.println("<AxisLabels>");
			for(int c=1;c<numCordinates+1;c++)
			{
				itemLog.println("<AxisLabel>");
				
				itemLog.println("<ID>" + c + "</ID>");
				itemLog.println("<Name>"+ GroupName[c-1]+ParameterName[c-1] + "</Name>");
				
				itemLog.println("</AxisLabel>");
			}

			itemLog.println("</AxisLabels>");
			itemLog.println("</Header>");
			itemLog.println("<Items>");
			
			Calendar calender = Calendar.getInstance();
			
			String date = new SimpleDateFormat("yyyy-MMMM-dd").format(calender.getTime());
			String time = new SimpleDateFormat("HH:mm").format(calender.getTime());
			
			// For run time calc			
			startTime = System.currentTimeMillis();
			
			// XML Log File Header
			infoLog.println("<Batch>");		
			infoLog.println("<ID>" + batchId + "</ID>");
			infoLog.println("<ScenarioType>" + type + "</ScenarioType>");
			infoLog.println("<Description>" + batchDescription + "</Description>");
			infoLog.println("<BaseFile>" + baseScenarioFile + "</BaseFile>");	
			infoLog.println("<Start>" + date + " " + time + "</Start>");		
			infoLog.flush();
		}
		catch (IOException e)
		{
			System.out.println("Could not created log file");
		}
	}
	
	private void testAndCreateDir(String dir)
	{
		File directory = new File(dir);

		if (!directory.exists())
		{
			if (directory.mkdir())
			{
				DebugLogger.output("Created " + dir);
			}
			else
			{
				DebugLogger.output("Failed to Create " + dir);
			}
		}
		
	}
	
	private void setBatchStatExportDir()
	{	
		Calendar calender = Calendar.getInstance();
		
		String date = new SimpleDateFormat("yyyy-MMMM-dd").format(calender.getTime());
		String time = new SimpleDateFormat("HHmm").format(calender.getTime());

		DebugLogger.output(date +"+"+  time);
		
		String section = "Config";

		String baseExportDir = batchConfigProcessor.getStringValue(section, "BatchStatsExportDir");
		String batchDirName = batchConfigProcessor.getStringValue(section, "BatchDirName");
				
		testAndCreateDir(baseExportDir);
		//testAndCreateDir(baseExportDir+File.separator+date);
		
		batchStatsExportDir = baseExportDir+File.separator+File.separator+date+" "+"Batch "+batchId+" "+batchDirName+"@"+time;
		
		testAndCreateDir(batchStatsExportDir);
		
		DebugLogger.output("Batch Stats Export Dir : " + batchStatsExportDir);
	}
	
	public String getBatchStatsExportDir()
	{
		return batchStatsExportDir;
	}
	
	private void setBatchDescription()
	{
		String section = "Config";

		batchDescription = batchConfigProcessor.getStringValue(section, "BatchDescription");
				
		DebugLogger.output("Batch Description : " + batchDescription);
	}
	
	private void setBaseFilePath(String fileText)
	{
		String section = "Config";

		baseScenarioFile = batchConfigProcessor.getStringValue(section, "BaseScenarioFileName");
		
		baseScenaroFilePath = basePath + File.separator + baseScenarioFile;
		
		DebugLogger.output("Base Scenario File : " + baseScenaroFilePath);
	}
	
	private void readFile(String fileName, StringBuilder destination) throws IOException
	{
		BufferedReader bufferedReader;

		bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName),"ISO_8859_1"));
		
		String sCurrentLine;
		
		while ((sCurrentLine = bufferedReader.readLine()) != null)
		{
			destination.append(sCurrentLine);
		}

		bufferedReader.close();
		
	}
	
	private ScenarioInf determinScenarios(String text)
	{
		ScenarioVT scenarioParser = null;

		ScenarioInf simScenario = null;

		scenarioParser = new ScenarioVT();

		// To get the type of Scenario object to create.
		scenarioParser.loadConfig(text);

		DebugLogger.output("Scenario Type : " + scenarioParser.getScenarioType());

		if (scenarioParser.getScenarioType().equalsIgnoreCase("SAPP"))
		{
			DebugLogger.output("SAPP File");
			simScenario = new SAPPScenario();

			simScenario.loadConfig(text);

		}
		else if(scenarioParser.getScenarioType().equalsIgnoreCase("LV"))
		{
			DebugLogger.output("LV File");
			simScenario = new LotkaVolterraScenario();

			simScenario.loadConfig(text);
		}
		else
		{
			DebugLogger.output("DeterminScenarios :UKNOWN");
		}

		return simScenario;
	}
	
	/*
	 * This method generates all the configuration combinations.
	 */
	private void generateCombos()
	{
		// Get a count of the parameter groups.
		int parameterGroups = batchConfigProcessor.getSubListSize("Parameters","Parameter");
		DebugLogger.output("Number of Parameter Groups : " + parameterGroups);

		// How many times to run each batchItem.
		itemSamples = batchConfigProcessor.getIntValue( "Config", "ItemSamples");
		
		// Array to hold the parameter type (group/single)
		String ParameterType[] = new String[parameterGroups];
		
		// Array to hold the path to group/parameter
		String Path[] = new String[parameterGroups];
		
		// Array to hold the unique identifier for the group.
		GroupName = new String[parameterGroups];
		
		// Array to hold the parameter name that will be changed.
		ParameterName = new String[parameterGroups];
		
		// Initial values of each parameter
		int Intial[] = new int[parameterGroups];
		
		// Increment values of each parameter
		int Increment[] = new int[parameterGroups];
		
		// Combinations for each parameter
		int Combinations[] = new int[parameterGroups];
		
		// Value of the max combination for each parameter
		int IncrementMaxValue[] = new int[parameterGroups];
		
		// The value in the combination at which to increment the value of the parameter
		int IncrementMod[] = new int[parameterGroups];
		
		// Iterate over the detected parameters and populate the arrays
		String section = "";
		for(int p=0;p<parameterGroups;p++)
		{
			// Generate the parameter path in the xml array (0),(1) etc
			DebugLogger.output("Parameter Group : " + p);
			section = "Parameters.Parameter("+p+")";
			
			// Get the type (group/single)
			ParameterType[p] = batchConfigProcessor.getStringValue(section, "Type");
			
			// Populate the path to this parameter.
			Path[p] = batchConfigProcessor.getStringValue(section, "Path");
			
			// Store the group name if this parameter changes one in a group.
			GroupName[p] = "";
			if(ParameterType[p].equalsIgnoreCase("Group"))
			{
				GroupName[p] = batchConfigProcessor.getStringValue(section, "GroupName");
			}
			
			// Store the name of the paramter to change
			ParameterName[p] = batchConfigProcessor.getStringValue(section, "ParameterName");
			
			// Intial value
			Intial[p] = batchConfigProcessor.getIntValue(section, "Intial");
			
			// Increment value
			Increment[p] = batchConfigProcessor.getIntValue(section, "Increment");
			
			// Combinations e.g 2 = initial value + 1 increment 
			Combinations[p] = batchConfigProcessor.getIntValue(section, "Combinations");

			// Max value = Combinations-1 as initial is the first
			IncrementMaxValue[p] = Intial[p] + ((Combinations[p]-1) * Increment[p]);
			
			// Logging
			DebugLogger.output("ParameterType : " + ParameterType[p]);
			DebugLogger.output("Path : " + Path[p]);
			DebugLogger.output("GroupName : " + GroupName[p]);
			DebugLogger.output("ParameterName : " + ParameterName[p]);
			DebugLogger.output("Intial : " + Intial[p]);
			DebugLogger.output("Increment : " + Increment[p]);
			DebugLogger.output("Combinations : " + Combinations[p]);
			DebugLogger.output("IncrementMaxValue : " + IncrementMaxValue[p]);

		}

		
		int currentValues[] = new int[parameterGroups];
		int combinations = 1;
		for(int p=0;p<parameterGroups;p++)
		{
			// Set Initial Values used in generation
			currentValues[p]=Intial[p];
			
			// Calculate Total Combinations
			combinations *= Combinations[p];
			
			if(p>0)
			{
				IncrementMod[p] = IncrementMod[p-1] * Combinations[p];
			}
			else
			{
				// P[0] always increments
				IncrementMod[p]=1;
			}
			DebugLogger.output("Group "+p+ " Increments @Combo%"+IncrementMod[p]);
			
		}
		DebugLogger.output("Combinations " + combinations);
		
		// The temp scenario used to generate the xml.
		ScenarioVT temp;

		String itemName = "";
		
		// Combination space coordinates X,Y,Z..
		ArrayList<Integer> comboCoordinates = new ArrayList<Integer>(parameterGroups);
		for(int p=0;p<parameterGroups;p++)
		{
			// Initialise the coordinates (1 base)
			comboCoordinates.add(1);
		}
		
		// Set the combination Values
		for(int combo=1; combo<combinations+1;combo++)
		{
			// Create a copy of the base scenario
			temp = new ScenarioVT();
			temp.loadConfig(baseScenarioText.toString());
			
			// Start of log line + itemName
			System.out.print("Combo : " + combo);
			itemName = "Combo " + combo;
			
			// Change the value for each parameter group
			for(int p=0;p<parameterGroups;p++)
			{

				// This is a group parameter
				if(ParameterType[p].equalsIgnoreCase("Group"))
				{
					// Log line middle
					System.out.print(" " + Path[p]+"."+GroupName[p]+"."+ParameterName[p] + " " + currentValues[p]);
					itemName = itemName + " " + Path[p]+"."+GroupName[p]+"."+ParameterName[p] + " " + currentValues[p];

					int groups = temp.getSubListSize(Path[p]);

					// Find the correct group that matches the name
					for(int sg=0;sg<groups;sg++)
					{
						String groupSection = Path[sg]+"("+sg+")";
						
						String searchGroupName = temp.getStringValue(groupSection, "Name");
						
						// Found Group 
						if(searchGroupName.equalsIgnoreCase(GroupName[p]))
						{
							// Combo / targetGroupName / Current GroupName
							//DebugLogger.output(c + " " + targetGroupName + " " + GroupName[p]);
							
							// The parameter we want
							//DebugLogger.output(ParameterName[p]);
							
							//String target = Path[p]+"."+ParameterName[p];
							//String target = groupSection+"."+ParameterName[p];								
							
							// Current Value in XML
							//DebugLogger.output("Current Value " + temp.getIntValue(groupSection,ParameterName[p]));
							
							// Find the datatype to change
							String dtype = temp.findDataType(Path[p]+"."+ParameterName[p]);
							
							// Currently only decimal and integer are used.
							if(dtype.equals("boolean"))
							{
								temp.changeValue(groupSection,ParameterName[p],new Boolean(true));
							}
							else if(dtype.equals("string"))
							{
								temp.changeValue(groupSection,ParameterName[p]," ");
							}
							else if(dtype.equals("decimal"))
							{
								temp.changeValue(groupSection,ParameterName[p],currentValues[p]);
							}
							else if (dtype.equals("integer"))
							{
								temp.changeValue(groupSection,ParameterName[p],currentValues[p]);
							}
							else
							{
								// This will not happen unless there is a new datatype added to the XML standards schema.
								DebugLogger.output("DTYPE : " + dtype);
							}
							/*
							DebugLogger.output("New Value " + temp.getIntValue(groupSection,ParameterName[p]));
							DebugLogger.output("Target : " + target);
							DebugLogger.output("Value : " + temp.getValueToString(target, temp.findDataType(target)));
							*/
							
							// Group was found and value was changed now exit search
							break;
						}

					}

				}
				else
				{
					// Log line middle
					System.out.print(" " + Path[p]+"."+ParameterName[p] + " " + currentValues[p]);
					itemName = itemName + " " + Path[p]+"."+ParameterName[p] + " " + currentValues[p];

					// Fine the datatype for this parameter
					String dtype = temp.findDataType(Path[p]+"."+ParameterName[p]);
					
					// Currently only decimal and integer are used.
					if(dtype.equals("boolean"))
					{
						temp.changeValue(Path[p],ParameterName[p],new Boolean(true));
					}
					else if(dtype.equals("string"))
					{
						temp.changeValue(Path[p],ParameterName[p]," ");
					}
					else if(dtype.equals("decimal"))
					{
						temp.changeValue(Path[p],ParameterName[p],currentValues[p]);
					}
					else if (dtype.equals("integer"))
					{
						temp.changeValue(Path[p],ParameterName[p],currentValues[p]);
					}
					else
					{
						// This will not happen unless there is a new datatype added to the XML standards schema.
						DebugLogger.output("DTYPE : " + dtype);
					}
					
				}
				
			}
			// Log line end
			System.out.print("\n");
			
			//DebugLogger.output(temp.getScenarioXMLText());
			System.out.print("ComboPos : " );
			ArrayList<Integer> tempCoord = new ArrayList<Integer>();
			ArrayList<Integer> tempCoordValues = new ArrayList<Integer>();
			for(int p=0;p<parameterGroups;p++)
			{
				System.out.print(comboCoordinates.get(p));
				if(p<(parameterGroups-1))
				{
					System.out.print('x');
				}
				
				tempCoord.add(comboCoordinates.get(p));
				tempCoordValues.add(currentValues[p]);
			}
			System.out.print('\n');
			
			// Add the new Batch Item combo used for batch item id, getScenarioXMLText is the new scenario xml configuration - samples is the number of identical items to generate (used as a sample/average)
			addBatchItem(itemSamples,combo,itemName,temp.getScenarioXMLText(),tempCoord,tempCoordValues);
			
			// Increment the combinatorics values.
			for(int p=0;p<parameterGroups;p++)
			{

				// Work out if the current c value is a increment for this group.
				if( combo % (IncrementMod[p]) == 0)
				{
					currentValues[p] = (currentValues[p]+Increment[p]);						
					
					// Increment after currentValues is greater than IncrementMaxValue
					if(currentValues[p] > IncrementMaxValue[p])
					{
						// Reset to initial value
						currentValues[p] = Intial[p];
					}
					
					// P[0] increments 1 each time, wrap it by the roll over value of its combinations number
					if(p==0)
					{
						// Increment the coordinate by 1
						comboCoordinates.set(p, (comboCoordinates.get(p)%Combinations[p])+1 );
					}
					else
					{
						// Increment the coordinate by 1
						comboCoordinates.set(p, comboCoordinates.get(p)+1);
					}
					
				}
				
			}

		}
		
		// All the items need to get processed, but the ett is influenced by the order (randomise it in an attempt to reduce influence)
		Collections.shuffle(queuedItems);		
	}
	
	public void returnItemToQueue(BatchItem item)
	{
		batchLock.acquireUninterruptibly();
		
		queuedItems.add(item);
		
		activeItems.remove(item);
		
		active = activeItems.size();
		
		batchLock.release();

	}
	
	public BatchItem getNext()
	{
		batchLock.acquireUninterruptibly();
		
		BatchItem temp = queuedItems.remove();
		
		activeItems.add(temp);
		
		active = activeItems.size();

		// Is this the first Item && Sample
		if(itemsRequested == 0)
		{
			startBatchLog(temp.getCoordinates().size());
		}
		
		itemsRequested++;
		
		batchLock.release();
		
		return temp;
	}
	
	public void setComplete(SimulationsManagerInf simsManager,BatchItem item,long runTime,String endEvent, long stepCount)
	{
		batchLock.acquireUninterruptibly();
		
		activeItems.remove(item);
		
		// For estimated complete time calculation
		completedItemRunTime+=runTime;
		
		// Create the item export dir
		testAndCreateDir(batchStatsExportDir+File.separator+item.getItemId());

		String fullExportPath = batchStatsExportDir+File.separator+item.getItemId()+File.separator+item.getSampleId();
		
		// Create the item sample full export path dir
		testAndCreateDir(fullExportPath);
		
		// Export Stats
		//simsManager.exportAllStatsToDir(item.getSimId(), fullExportPath,String.valueOf(item.getItemHash()),"csv");
		simsManager.exportAllStatsToDir(item.getSimId(), fullExportPath,String.valueOf(item.getItemHash()),"xml");
		//simsManager.exportAllStatsToDir(item.getSimId(), fullExportPath,String.valueOf(item.getItemHash()),"arff");
		
		itemLog.println("<Item>");
		itemLog.println("<IID>" + item.getItemId() + "</IID>");	
		itemLog.println("<SID>" + item.getSampleId() + "</SID>");	
		itemLog.println("<Coordinates>");
		ArrayList<Integer> coords = item.getCoordinates();
		ArrayList<Integer> coordsValues = item.getCoordinatesValues();
		for(int c=0;c<coords.size();c++)
		{
			itemLog.println("<Coordinate>");
			itemLog.println("<Pos>" + coords.get(c) + "</Pos>");
			itemLog.println("<Value>" + coordsValues.get(c) + "</Value>");
			itemLog.println("</Coordinate>");
		}
		itemLog.println("</Coordinates>");
		itemLog.println("<Hash>"+item.getItemHash()+"</Hash>");
		itemLog.println("<RunTime>"+alifeSim.util.Text.longTimeToDHMS(runTime)+"</RunTime>");
		itemLog.println("<EndEvent>"+endEvent+"</EndEvent>");
		itemLog.println("<StepCount>"+stepCount+"</StepCount>");	
		itemLog.println("</Item>");		
		
		// Only the first sample needs to save the item config (all identical samples)
		if(item.getSampleId() == 1)
		{	// Save the Item Config
			try
			{
				// All Item samples use same config so overwrite.
				PrintWriter configFile = new PrintWriter(new BufferedWriter(new FileWriter(batchStatsExportDir+File.separator+item.getItemId()+File.separator+"itemconfig-"+item.getItemHash()+".xml", true)));
				
				configFile.write(item.getConfigText());
				configFile.flush();
				configFile.close();
			}
			catch (IOException e)
			{
				System.out.println("Could not save item " + item.getItemId() + " config (Batch " + item.getBatchId() +")");
			}
		}
		completedItems++;	
		
		if(completedItems == batchItems)
		{
			// Close Info Log
			Calendar calender = Calendar.getInstance();
			String date = new SimpleDateFormat("yyyy-MMMM-dd").format(calender.getTime());
			String time = new SimpleDateFormat("HH:mm").format(calender.getTime());	
			infoLog.println("<Finished>"+date + " " + time+"</Finished>");
			infoLog.println("<TotalTime>"+alifeSim.util.Text.longTimeToDHMS(System.currentTimeMillis()-startTime)+"</TotalTime>");
			infoLog.println("<Batch>");
			infoLog.flush();
			infoLog.close();
			
			// Close Batch Log
			itemLog.println("</Items>");
			itemLog.println("</Log>");
			itemLog.flush();
			itemLog.close();
		}
		
		batchLock.release();
	}
	
	public int getRemaining()
	{
		return queuedItems.size();
	}
	
	// Small wrapper around queue add
	private void addBatchItem(int samples,int id,String name,String configText,ArrayList<Integer> coordinates,ArrayList<Integer> coordinatesValues)
	{
		//SID/SampleId is 1 base/ 1=first sample
		for(int sid=1;sid<samples+1;sid++)
		{
			queuedItems.add(new BatchItem(sid,id,batchId,name,configText,coordinates,coordinatesValues));	
		}
		
		batchItems=batchItems+(1*samples);
	}
	
	public int getBatchId()
	{
		return batchId;
	}

	public void setBatchId(int batchId)
	{
		this.batchId = batchId;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getBaseScenarioFile()
	{
		return baseScenarioFile;
	}

	public void setBaseScenarioFile(String baseScenarioFile)
	{
		this.baseScenarioFile = baseScenarioFile;
	}

	public int getBatchItems()
	{
		return batchItems;
	}

	public void setBatchItems(int batchItems)
	{
		this.batchItems = batchItems;
	}

	public int getProgress()
	{
		return (int) (((float)completedItems/(float)batchItems)*100f);
	}

	public int getCompletedItems()
	{
		return completedItems;
	}
	
	public BatchItem[] getQueuedItems()
	{
		return queuedItems.toArray(new BatchItem[queuedItems.size()]);
	}
	
	public BatchItem[] getActiveItems()
	{
		return activeItems.toArray(new BatchItem[activeItems.size()]);
	}
	
	public long getRunTime()
	{
		return System.currentTimeMillis()-startTime;
	}
	
	public long getETT()
	{
		return getRunTime() + ( ( (completedItemRunTime / completedItems) * (batchItems - completedItems) ) / active);
	}
	
}
