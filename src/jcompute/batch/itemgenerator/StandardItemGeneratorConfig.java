package jcompute.batch.itemgenerator;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jcompute.math.JCMath;
import jcompute.scenario.ConfigurationInterpreter;

public class StandardItemGeneratorConfig implements ItemGeneratorConfigInf
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(StandardItemGeneratorConfig.class);
	
	private final String baseScenarioText;
	private final int itemSamples;
	
	private final int numParameterGroups;
	
	private final String parameterType[];
	private final String path[];
	
	private final String[] groupName;
	private final String[] parameterName;
	private final ArrayList<String> parameters;
	private final double baseValue[];
	private final double increment[];
	private final int step[];
	private final double errormargin[];
	
	// Calculated total combinations
	private final int totalCombinations;
	
	public StandardItemGeneratorConfig(ConfigurationInterpreter interpreter, String baseScenarioText, int itemSamples)
	{
		// The file to be used to generate configs
		this.baseScenarioText = baseScenarioText;
		
		// Number of item samples
		this.itemSamples = itemSamples;
		
		// Get a count of the parameter groups.
		numParameterGroups = interpreter.getSubListSize("Parameters", "Parameter");
		
		// Array to hold the parameter type (group/single)
		parameterType = new String[numParameterGroups];
		
		// Array to hold the path to group/parameter
		path = new String[numParameterGroups];
		
		// Array to hold the unique identifier for the group.
		groupName = new String[numParameterGroups];
		
		// Array to hold the parameter name that will be changed.
		parameterName = new String[numParameterGroups];
		
		// Base values of each parameter
		baseValue = new double[numParameterGroups];
		
		// Increment values of each parameter
		increment = new double[numParameterGroups];
		
		// steps for each parameter
		step = new int[numParameterGroups];
		
		// Floating point equality ranges
		// Get the number of decimal places
		// Get 10^places
		// divide 1 by 10^places to get
		// n places .1 above the the significant value to test for
		errormargin = new double[numParameterGroups];
		
		// Batch Info Parameters list
		parameters = new ArrayList<String>();
		
		// Iterate over the detected parameters and read values
		String section = "";
		for(int p = 0; p < numParameterGroups; p++)
		{
			// Generate the parameter path in the xml array (0),(1) etc
			log.debug("Parameter Group : " + p);
			section = "Parameters.Parameter(" + p + ")";
			
			// Get the type (group/single)
			parameterType[p] = interpreter.getStringValue(section, "Type");
			
			// Populate the path to this parameter.
			path[p] = interpreter.getStringValue(section, "Path");
			
			// Store the group name if this parameter changes one in a
			// group.
			groupName[p] = "";
			if(parameterType[p].equalsIgnoreCase("Group"))
			{
				groupName[p] = interpreter.getStringValue(section, "GroupName");
			}
			
			// Store the name of the paramter to change
			parameterName[p] = interpreter.getStringValue(section, "ParameterName");
			
			// Base value
			baseValue[p] = interpreter.getDoubleValue(section, "Intial");
			
			// Increment value
			increment[p] = interpreter.getDoubleValue(section, "Increment");
			
			// Steps for each values
			step[p] = interpreter.getIntValue(section, "Combinations");
			
			// Find the number of decimal places
			int places = JCMath.getNumberOfDecimalPlaces(increment[p]);
			boolean incRounded = false;
			
			// if A decimal value - calculate the error margin to use
			// for floating point equality tests
			// else for integer values epsilon is 0
			if(places > 0)
			{
				// We cannot represent error margins for values with
				// more than 14 decimals
				if(places > 14)
				{
					places = 14;
					
					double prev = increment[p];
					
					increment[p] = JCMath.round(increment[p], places);
					
					log.warn("increment " + p + " rounded " + prev + " to " + increment[p]);
					incRounded = true;
				}
				
				// + 1 places to set the range for the unit after the
				// number of decimals places
				errormargin[p] = 1.0 / (Math.pow(10, (places + 1)));
			}
			else
			{
				errormargin[p] = 0;
			}
			
			// Creates the parameters string for batch info
			// Optimise slightly the concatenations
			String pNumString = "(" + p + ") ";
			
			// Used in batch info.
			parameters.add(pNumString + "ParameterType");
			parameters.add(parameterType[p]);
			parameters.add(pNumString + "Path");
			parameters.add(path[p]);
			parameters.add(pNumString + "GroupName");
			parameters.add(groupName[p]);
			parameters.add(pNumString + "ParameterName");
			parameters.add(parameterName[p]);
			parameters.add(pNumString + "Intial");
			parameters.add(String.valueOf(baseValue[p]));
			parameters.add(pNumString + "Increment");
			parameters.add(String.valueOf(increment[p]));
			parameters.add(pNumString + "Steps");
			parameters.add(String.valueOf(step[p]));
			parameters.add(pNumString + "Error Margin");
			parameters.add(String.valueOf(errormargin[p]));
			parameters.add(pNumString + "Increment Rounded");
			parameters.add(String.valueOf(incRounded));
			
			// Logging
			log.info(pNumString + "ParameterType : " + parameterType[p]);
			log.info(pNumString + "Path : " + path[p]);
			log.info(pNumString + "GroupName : " + groupName[p]);
			log.info(pNumString + "ParameterName : " + parameterName[p]);
			log.info(pNumString + "Intial : " + baseValue[p]);
			log.info(pNumString + "Increment : " + increment[p]);
			log.info(pNumString + "Combinations : " + step[p]);
			log.info(pNumString + "Error Margin : " + errormargin[p]);
			log.info(pNumString + "Increment Rounded : " + String.valueOf(incRounded));
		}
		
		// Calculate Total Combinations
		int tTotalCombinations = 1;
		
		for(int s = 0; s < step.length; s++)
		{
			tTotalCombinations *= step[s];
		}
		
		totalCombinations = tTotalCombinations;
	}
	
	@Override
	public int getItemSamples()
	{
		return itemSamples;
	}
	
	@Override
	public String getBaseScenarioText()
	{
		return baseScenarioText;
	}
	
	@Override
	public String[] getGroupName()
	{
		return groupName;
	}
	
	@Override
	public String[] getParameterName()
	{
		return parameterName;
	}
	
	@Override
	public int getTotalCombinations()
	{
		return totalCombinations;
	}
	
	public int getNumParameterGroups()
	{
		return numParameterGroups;
	}
	
	public String[] getParameterType()
	{
		return parameterType;
	}
	
	public String[] getPath()
	{
		return path;
	}
	
	public ArrayList<String> getParameters()
	{
		return parameters;
	}
	
	public double[] getBaseValue()
	{
		return baseValue;
	}
	
	public double[] getIncrement()
	{
		return increment;
	}
	
	public int[] getStep()
	{
		return step;
	}
	
	public double[] getErrormargin()
	{
		return errormargin;
	}
}
