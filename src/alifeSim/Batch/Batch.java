package alifeSim.Batch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Deque;

import alifeSim.Scenario.ScenarioInf;
import alifeSim.Scenario.ScenarioVT;
import alifeSim.Scenario.Debug.DebugScenario;
import alifeSim.Scenario.Math.LotkaVolterra.LotkaVolterraScenario;
import alifeSim.Scenario.SAPP.SAPPScenario;

public class Batch
{
	// Our Queue of Simulation Configs
	private Deque<BatchItem> simulationConfigs;
	
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
	
	public Batch(String fileName) throws IOException
	{
		simulationConfigs = new ArrayDeque<BatchItem>();
			
		batchConfigText = new StringBuilder();
		
		basePath = getPath(fileName);
		
		System.out.println("Base Path : " + basePath);	
		
		// Put the file text into the string builder
		readFile(fileName,batchConfigText);
		
		System.out.println("New Batch based on : " + fileName);
		
		processBatchConfig(batchConfigText.toString());

	}
	
	private String getPath(String fileName)
	{
		return Paths.get(fileName).getParent().toString();
	}

	private void processBatchConfig(String fileText) throws IOException
	{
		batchConfigProcessor = new ScenarioVT();
		
		batchConfigProcessor.loadConfig(batchConfigText.toString());
		
		batchConfigProcessor.dumpXML();
		
		setBaseFilePath(fileText);
		
		baseScenarioText = new StringBuilder();

		readFile(baseScenaroFilePath,baseScenarioText);
			
		baseScenario = determinScenarios(baseScenarioText.toString());		
				
		System.out.println(baseScenario.getScenarioType());
		
		generateCombos();
		
	}
	
	private void generateCombos()
	{
		// Get a count of the parameter groups.
		int parameterGroups = batchConfigProcessor.getSubListSize("Parameters","Parameter");
		System.out.println("Number of Parameter Groups : " + parameterGroups);
		
		// Array to hold the parameter type (group/single)
		String ParameterType[] = new String[parameterGroups];
		
		// Array to hold the path to group/parameter
		String Path[] = new String[parameterGroups];
		
		// Array to hold the unique identifier for the group.
		String GroupName[] = new String[parameterGroups];
		
		// Array to hold the parameter name that will be changed.
		String ParameterName[] = new String[parameterGroups];
		
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
			System.out.println("Parameter Group : " + p);
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
			System.out.println("ParameterType : " + ParameterType[p]);
			System.out.println("Path : " + Path[p]);
			System.out.println("GroupName : " + GroupName[p]);
			System.out.println("ParameterName : " + ParameterName[p]);
			System.out.println("Intial : " + Intial[p]);
			System.out.println("Increment : " + Increment[p]);
			System.out.println("Combinations : " + Combinations[p]);
			System.out.println("IncrementMaxValue : " + IncrementMaxValue[p]);

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
			System.out.println("Group "+p+ " Increments @Combo%"+IncrementMod[p]);
			
		}
		System.out.println("Combinations " + combinations);
		
		// The temp scenario used to generate the xml.
		ScenarioVT temp;

		// Set the combination Values
		for(int combo=1; combo<combinations+1;combo++)
		{
			// Create a copy of the base scenario
			temp = new ScenarioVT();
			temp.loadConfig(baseScenarioText.toString());
			
			// Start of log line
			System.out.print("Combo : " + combo + " ");

			// Change the value for each parameter group
			for(int p=0;p<parameterGroups;p++)
			{

				// This is a group parameter
				if(ParameterType[p].equalsIgnoreCase("Group"))
				{
					// Log line middle
					System.out.print(Path[p]+"."+GroupName[p]+"."+ParameterName[p] + " " + currentValues[p] + " ");

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
							//System.out.println(c + " " + targetGroupName + " " + GroupName[p]);
							
							// The parameter we want
							//System.out.println(ParameterName[p]);
							
							//String target = Path[p]+"."+ParameterName[p];
							//String target = groupSection+"."+ParameterName[p];								
							
							// Current Value in XML
							//System.out.println("Current Value " + temp.getIntValue(groupSection,ParameterName[p]));
							
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
								System.out.println("DTYPE : " + dtype);
							}
							/*
							System.out.println("New Value " + temp.getIntValue(groupSection,ParameterName[p]));
							System.out.println("Target : " + target);
							System.out.println("Value : " + temp.getValueToString(target, temp.findDataType(target)));
							*/
							
							// Group was found and value was changed now exit search
							break;
						}

					}

				}
				else
				{
					// Log line middle
					System.out.print(Path[p]+"."+ParameterName[p] + " " + currentValues[p] + " ");

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
						System.out.println("DTYPE : " + dtype);
					}
					
				}
				
			}
			// Log line end
			System.out.print("\n");
			
			
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
					
				}
				
			}

			//System.out.println(temp.getScenarioXMLText());
			
			// Add the new Batch Item combo used for batch item id, getScenarioXMLText is the new scenario xml configuration
			simulationConfigs.add(new BatchItem(combo,temp.getScenarioXMLText()));
			
		}
		
	}
	
	private void setBaseFilePath(String fileText)
	{
		String section = "BaseScenario";

		baseScenaroFilePath = basePath + "\\" + batchConfigProcessor.getStringValue(section, "FileName");
		
		System.out.println("Base Scenario File : " + baseScenaroFilePath);
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
	
	public BatchItem getNext()
	{
		return simulationConfigs.remove();
	}
	
	public int getRemaining()
	{
		return simulationConfigs.size();
	}

	private ScenarioInf determinScenarios(String text)
	{
		ScenarioVT scenarioParser = null;

		ScenarioInf simScenario = null;

		scenarioParser = new ScenarioVT();

		// To get the type of Scenario object to create.
		scenarioParser.loadConfig(text);

		System.out.println("Scenario Type : " + scenarioParser.getScenarioType());

		if (scenarioParser.getScenarioType().equalsIgnoreCase("DEBUG"))
		{
			System.out.println("Debug File");
			simScenario = new DebugScenario(text);
		}
		else
		{
			if (scenarioParser.getScenarioType().equalsIgnoreCase("SAPP"))
			{
				System.out.println("SAPP File");
				simScenario = new SAPPScenario();

				simScenario.loadConfig(text);

			}
			else if(scenarioParser.getScenarioType().equalsIgnoreCase("LV"))
			{
				System.out.println("LV File");
				simScenario = new LotkaVolterraScenario();

				simScenario.loadConfig(text);
			}
			else
			{
				System.out.println("DeterminScenarios :UKNOWN");
			}
		}

		return simScenario;
	}
	
}
