package jCompute.Batch;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jCompute.Batch.ItemGenerator.ItemGenerator;
import jCompute.Datastruct.cache.DiskCache;
import jCompute.Scenario.ConfigurationInterpreter;
import jCompute.util.FileUtil;
import jCompute.util.JCMath;

public class SAPPItemGenerator extends ItemGenerator
{
	private static Logger log = LoggerFactory.getLogger(SAPPItemGenerator.class);

	private final String GeneratorName = "SAPPItemGenerator";

	private int batchId;

	private ConfigurationInterpreter batchConfigProcessor;
	private LinkedList<BatchItem> destinationItemList;
	private DiskCache itemDiskCache;
	private int itemSamples;

	// Returnable
	private String[] groupName;
	private String[] parameterName;
	private ArrayList<String> parameters;
	private ZipOutputStream resultsZipOut;
	private int itemsCount;

	// Generation Progress
	private boolean needGenerated;

	public SAPPItemGenerator(int batchId, ConfigurationInterpreter batchConfigProcessor, LinkedList<BatchItem> destinationItemList, DiskCache itemDiskCache,
	int itemSamples)
	{
		this.batchId = batchId;
		this.batchConfigProcessor = batchConfigProcessor;
		this.destinationItemList = destinationItemList;
		this.itemDiskCache = itemDiskCache;
		this.itemSamples = itemSamples;

		needGenerated = true;
	}

	@Override
	public void generate(double[] progress1dArray, String baseScenarioText, String batchStatsExportDir, boolean storeStats, boolean statsMethodSingleArchive,
	int singleArchiveCompressionLevel, int bosBufferSize)
	{
		if(!needGenerated)
		{
			log.error(GeneratorName + " got call to generate items when items already generated");

			return;
		}

		progress1dArray[0] = 0;

		needGenerated = false;

		log.info("Generating Items for Batch " + batchId);

		// Get a count of the parameter groups.
		int parameterGroups = batchConfigProcessor.getSubListSize("Parameters", "Parameter");

		// Array to hold the parameter type (group/single)
		String parameterType[] = new String[parameterGroups];

		// Array to hold the path to group/parameter
		String path[] = new String[parameterGroups];

		// Array to hold the unique identifier for the group.
		groupName = new String[parameterGroups];

		// Array to hold the parameter name that will be changed.
		parameterName = new String[parameterGroups];

		// Base values of each parameter
		double baseValue[] = new double[parameterGroups];

		// Increment values of each parameter
		double increment[] = new double[parameterGroups];

		// steps for each parameter
		int step[] = new int[parameterGroups];

		// Floating point equality ranges
		// Get the number of decimal places
		// Get 10^places
		// divide 1 by 10^places to get
		// n places .1 above the the significant value to test for
		double[] errormargin = new double[parameterGroups];

		// Batch Info Parameters list
		parameters = new ArrayList<String>();

		// Iterate over the detected parameters and read values
		String section = "";
		for(int p = 0; p < parameterGroups; p++)
		{
			// Generate the parameter path in the xml array (0),(1) etc
			log.debug("Parameter Group : " + p);
			section = "Parameters.Parameter(" + p + ")";

			// Get the type (group/single)
			parameterType[p] = batchConfigProcessor.getStringValue(section, "Type");

			// Populate the path to this parameter.
			path[p] = batchConfigProcessor.getStringValue(section, "Path");

			// Store the group name if this parameter changes one in a
			// group.
			groupName[p] = "";
			if(parameterType[p].equalsIgnoreCase("Group"))
			{
				groupName[p] = batchConfigProcessor.getStringValue(section, "GroupName");
			}

			// Store the name of the paramter to change
			parameterName[p] = batchConfigProcessor.getStringValue(section, "ParameterName");

			// Base value
			baseValue[p] = batchConfigProcessor.getDoubleValue(section, "Intial");

			// Increment value
			increment[p] = batchConfigProcessor.getDoubleValue(section, "Increment");

			// Steps for each values
			step[p] = batchConfigProcessor.getIntValue(section, "Combinations");

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

			// Optimise slightly the concatenations
			String pNumString = "(" + p + ") ";

			// Used in batch info.
			parameters.add("");
			parameters.add("");
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
		int combinations = 1;
		for(int s = 0; s < step.length; s++)
		{
			combinations *= step[s];
		}

		// When to increment values in the combos
		int incrementMods[] = new int[parameterGroups];
		int div = combinations;
		for(int p = 0; p < parameterGroups; p++)
		{
			div = div / step[p];
			// Increment depending on the step div
			incrementMods[p] = div;
			log.info("p " + p + " increments every " + incrementMods[p]);
		}

		// Roll over value of the max combination for each parameter
		double maxValue[] = new double[parameterGroups];
		for(int p = 0; p < parameterGroups; p++)
		{
			maxValue[p] = baseValue[p] + (increment[p] * (step[p] - 1));
		}

		// Init combo initial starting bases
		double value[] = new double[parameterGroups];
		for(int p = 0; p < parameterGroups; p++)
		{
			value[p] = baseValue[p];
		}

		// Create if needed

		String zipPath = null;
		if(storeStats && statsMethodSingleArchive)
		{
			// Create and populate Results Zip archive with Directories
			zipPath = batchStatsExportDir + File.separator + "results.zip";

			log.info("Zip Archive : " + zipPath);

			try
			{
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(zipPath), bosBufferSize);

				resultsZipOut = new ZipOutputStream(bos);

				if(singleArchiveCompressionLevel > 9)
				{
					singleArchiveCompressionLevel = 9;
				}

				if(singleArchiveCompressionLevel < 0)
				{
					singleArchiveCompressionLevel = 0;
				}

				resultsZipOut.setMethod(ZipOutputStream.DEFLATED);
				resultsZipOut.setLevel(singleArchiveCompressionLevel);
			}
			catch(FileNotFoundException e1)
			{
				log.error("Could not create create  " + zipPath);

				e1.printStackTrace();
			}
		}

		// Combo x,y,z... parameter spatial grid position
		int pos[] = new int[parameterGroups];

		// The temp scenario used to generate the xml.
		ConfigurationInterpreter temp;

		for(int c = 0; c < combinations; c++)
		{
			// Create Sub Directories in Zip Archive or Directory
			if(storeStats && statsMethodSingleArchive)
			{

				try
				{
					// Create Item Directories
					resultsZipOut.putNextEntry(new ZipEntry(Integer.toString(c) + "/"));
					resultsZipOut.closeEntry();

					for(int sid = 1; sid < (itemSamples + 1); sid++)
					{
						// Create Sample Directories
						resultsZipOut.putNextEntry(new ZipEntry(Integer.toString(c) + "/" + Integer.toString(sid) + "/"));
						resultsZipOut.closeEntry();
					}

				}
				catch(IOException e)
				{
					log.error("Could not create create directory " + c + " in " + zipPath);

					e.printStackTrace();
				}
			}
			else
			{
				// Create the item export dir
				FileUtil.createDirIfNotExist(batchStatsExportDir + File.separator + c);

				for(int sid = 1; sid < (itemSamples + 1); sid++)
				{
					String fullExportPath = batchStatsExportDir + File.separator + c + File.separator + sid;

					// Create the item sample full export path dir
					FileUtil.createDirIfNotExist(fullExportPath);
				}
			}

			// Create a copy of the base scenario
			temp = new ConfigurationInterpreter();
			temp.loadConfig(baseScenarioText);

			StringBuilder itemName = new StringBuilder();

			// Start of log line + itemName
			itemName.append("Combo " + c);

			StringBuilder comboPosString = new StringBuilder();

			// DebugLogger.output(temp.getScenarioXMLText());
			comboPosString.append("ComboPos(");
			ArrayList<Integer> tempCoord = new ArrayList<Integer>();
			ArrayList<Float> tempCoordValues = new ArrayList<Float>();

			for(int p = 0; p < parameterGroups; p++)
			{
				// Increment this parameter? (avoid increment the first
				// combo c>0)
				if(((c % incrementMods[p]) == 0) && (c > 0))
				{
					pos[p] = (pos[p] + 1) % step[p];

					value[p] = (value[p] + increment[p]);

					// Has a roll over occurred ( > with in a calculated
					// epsilon (floating point equalities))
					if(value[p] > (maxValue[p] + errormargin[p]))
					{
						value[p] = baseValue[p];
					}
				}

				// This is a group parameter
				if(parameterType[p].equalsIgnoreCase("Group"))
				{
					// Log line middle
					itemName.append(" " + path[p] + "." + groupName[p] + "." + parameterName[p] + " " + value[p]);

					int groups = temp.getSubListSize(path[p]);

					// Find the correct group that matches the name
					for(int sg = 0; sg < groups; sg++)
					{
						String groupSection = path[sg] + "(" + sg + ")";

						String searchGroupName = temp.getStringValue(groupSection, "Name");

						// Found Group
						if(searchGroupName.equalsIgnoreCase(groupName[p]))
						{
							// Combo / targetGroupName / Current
							// GroupName
							// DebugLogger.output(c + " " +
							// targetGroupName
							// +
							// " " + GroupName[p]);

							// The parameter we want
							// DebugLogger.output(ParameterName[p]);

							// String target =
							// Path[p]+"."+ParameterName[p];
							// String target =
							// groupSection+"."+ParameterName[p];

							// Current Value in XML
							// DebugLogger.output("Current Value " +
							// temp.getIntValue(groupSection,ParameterName[p]));

							// Find the datatype to change
							String dtype = temp.findDataType(path[p] + "." + parameterName[p]);

							// Currently only decimal and integer are
							// supported.
							if(dtype.equals("boolean"))
							{
								temp.changeValue(groupSection, parameterName[p], new Boolean(true));
							}
							else if(dtype.equals("string"))
							{
								temp.changeValue(groupSection, parameterName[p], " ");
							}
							else if(dtype.equals("decimal"))
							{
								temp.changeValue(groupSection, parameterName[p], value[p]);
							}
							else if(dtype.equals("integer"))
							{
								// The configuration file wants Integer
								// values - Cast double to ints
								temp.changeValue(groupSection, parameterName[p], (int) value[p]);
							}
							else
							{
								// This will not happen unless there is
								// a
								// new
								// datatype added to the XML standards
								// schema
								// and we use it.
								log.error("Unknown XML DTYPE : " + dtype);
							}
							/*
							 * DebugLogger.output("New Value " +
							 * temp.getIntValue
							 * (groupSection,ParameterName[p]));
							 * DebugLogger.output("Target : " + target);
							 * DebugLogger.output("Value : " +
							 * temp.getValueToString(target,
							 * temp.findDataType(target)));
							 */

							// Group was found and value was changed now
							// exit
							// search
							break;
						}

					}

				}
				else
				{
					// Log line middle
					itemName.append(" " + path[p] + "." + parameterName[p] + " " + value[p]);

					// Fine the datatype for this parameter
					String dtype = temp.findDataType(path[p] + "." + parameterName[p]);

					// Currently only decimal and integer are used.
					if(dtype.equals("boolean"))
					{
						temp.changeValue(path[p], parameterName[p], new Boolean(true));
					}
					else if(dtype.equals("string"))
					{
						temp.changeValue(path[p], parameterName[p], " ");
					}
					else if(dtype.equals("decimal"))
					{
						temp.changeValue(path[p], parameterName[p], value[p]);
					}
					else if(dtype.equals("integer"))
					{
						temp.changeValue(path[p], parameterName[p], (int) value[p]);
					}
					else
					{
						// This will not happen unless there is a new
						// datatype
						// added to the XML standards schema.
						log.error("Unknown XML DTYPE : " + dtype);
					}

				}

				// Set the pos and val
				tempCoord.add(pos[p]);
				tempCoordValues.add((float) JCMath.round(value[p], 7));

				comboPosString.append(String.valueOf(pos[p]));
				if(p < (parameterGroups - 1))
				{
					comboPosString.append('x');
				}

			}
			// Log line end
			comboPosString.append(")");
			log.debug(comboPosString.toString());
			log.debug(itemName.toString());

			// Add the generated item.
			addItem(itemSamples, c, itemName.toString(), temp.getText(), tempCoord, tempCoordValues, storeStats);

			// Update the current progress
			progress1dArray[0] = ((double) c / (double) combinations) * 100.0;

			// Avoid div by zero on <10 combinations
			if(combinations > 10)
			{
				// Every 10%
				if((c % (combinations / 10)) == 0)
				{
					log.info((int) progress1dArray[0] + "%");
				}
			}

			// END COMBO
		}

		progress1dArray[0] = 100;
		log.info((int) progress1dArray[0] + "%");

		// backgroundGenerate.setName("Item Generation Background Thread Batch " + batchId);

	}

	// Small wrapper around queue add and disk cache
	private void addItem(int samples, int id, String name, String configText, ArrayList<Integer> coordinates, ArrayList<Float> coordinatesValues,
	boolean storeStats)
	{
		byte[] configBytes = null;
		try
		{
			configBytes = configText.getBytes("ISO-8859-1");
		}
		catch(UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}

		String itemHash = itemDiskCache.addFile(configBytes);

		// SID/SampleId is 1 base/ 1=first sample
		for(int sid = 1; sid < (samples + 1); sid++)
		{
			destinationItemList.add(new BatchItem(sid, id, batchId, name, itemHash, coordinates, coordinatesValues, storeStats));
			itemsCount++;
		}

	}

	@Override
	public String[] getGroupNames()
	{
		return groupName;
	}

	@Override
	public String[] getParameterNames()
	{
		return parameterName;
	}

	@Override
	public ArrayList<String> getParameters()
	{
		return parameters;
	}

	@Override
	public ZipOutputStream getResultsZipOut()
	{
		return resultsZipOut;
	}

	@Override
	public int getGeneratedItemCount()
	{
		return itemsCount;
	}

}
