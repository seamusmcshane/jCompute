package jcompute.batch.itemgenerator;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.zip.Deflater;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jcompute.batch.BatchSettings;
import jcompute.batch.batchitem.BatchItem;
import jcompute.batch.batchresults.BatchResultSettings;
import jcompute.batch.itemmanager.ItemManager;
import jcompute.batch.itemstore.ItemDiskCache;
import jcompute.batch.itemstore.ItemStore;
import jcompute.configuration.XMLModifier;
import jcompute.math.JCMath;
import jcompute.timing.ProgressObj;

/**
 * An item generator for scenarios that use the standard scenario format for groups, parameters and values.
 * It supports -
 * Generating scenarios over multi-dimensional ranges of either integer or decimal numbers.
 * It can create duplicate configurations and assign a unique sid to each individually for when multiple samples are required.
 * All export statistic export formats.
 * Supports large numbers of configurations via a small write through memory cache with secondary disk cache.
 * 
 * @author Seamus McShane
 */
public class StandardItemGenerator extends ItemGenerator
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(StandardItemGenerator.class);
	
	private final String GeneratorName = "StandardItemGenerator";
	
	// Items Generated
	private int itemsCount;
	
	private ArrayList<String> parameters;
	
	public StandardItemGenerator()
	{
		super();
	}
	
	@Override
	public String getName()
	{
		return GeneratorName;
	}
	
	// progress1dArray[0] parms here? make method?
	@Override
	public boolean subgenerator(int batchId, ProgressObj progress, ItemManager itemManager, ItemStore itemStore,
	BatchSettings batchSettings)
	{
		log.info("Generating Items for Batch " + batchId);
		
		StandardItemGeneratorConfig config = (StandardItemGeneratorConfig) batchSettings.itemGeneratorConfig;
		
		// Base scenario to be used
		String baseScenarioText = config.getBaseScenarioText();
		
		// Number of Item samples
		int itemSamples = config.getItemSamples();
		
		// Get a count of the parameter groups.
		int numParameterGroups = config.getNumParameterGroups();
		
		// Array to hold the parameter type (group/single)
		String[] parameterType = config.getParameterType();
		
		// Array to hold the path to group/parameter
		String[] path = config.getPath();
		
		// Array to hold the unique identifier for the group.
		String[] groupName = config.getGroupName();
		
		// Array to hold the parameter name that will be changed.
		String[] parameterName = config.getParameterName();
		
		// Base values of each parameter
		double[] baseValue = config.getBaseValue();
		
		// Increment values of each parameter
		double[] increment = config.getIncrement();
		
		// steps for each parameter
		int[] step = config.getStep();
		
		// Floating point equality ranges
		// Get the number of decimal places
		// Get 10^places
		// divide 1 by 10^places to get
		// n places .1 above the the significant value to test for
		double[] errormargin = config.getErrormargin();
		
		// Batch Info Parameters list
		parameters = config.getParameters();
		
		// Calculate Total Combinations
		// int combinations = 1;
		// for(int s = 0; s < step.length; s++)
		// {
		// combinations *= step[s];
		// }
		
		int combinations = config.getTotalCombinations();
		
		// Calculate required cache size and unique ratio (1/1 - we give all duplicate samples the same itemId and configuration)
		int cacheSize = combinations;
		int uniqueRatio = combinations;
		
		// Temp list to store the combos
		ArrayList<BatchItem> tempComboItemList = new ArrayList<BatchItem>(combinations);
		
		// ItemDiskCache is bound to Standard Item Generator
		boolean intemStoreCreated = ((ItemDiskCache) itemStore).init(true, cacheSize, uniqueRatio,
		batchSettings.batchResultSettings.FullBatchStatsExportPath, Deflater.BEST_SPEED);
		
		if(!intemStoreCreated)
		{
			log.error("Could not initialise item store");
			
			return false;
		}
		
		log.info("Created an ItemDiskCache as ItemStore for Batch " + batchId);
		
		// When to increment values in the combos
		int incrementMods[] = new int[numParameterGroups];
		int div = combinations;
		for(int p = 0; p < numParameterGroups; p++)
		{
			div = div / step[p];
			// Increment depending on the step div
			incrementMods[p] = div;
			log.info("p " + p + " increments every " + incrementMods[p]);
		}
		
		// Roll over value of the max combination for each parameter
		double maxValue[] = new double[numParameterGroups];
		for(int p = 0; p < numParameterGroups; p++)
		{
			maxValue[p] = baseValue[p] + (increment[p] * (step[p] - 1));
		}
		
		// Init combo initial starting bases
		double value[] = new double[numParameterGroups];
		for(int p = 0; p < numParameterGroups; p++)
		{
			value[p] = baseValue[p];
		}
		
		BatchResultSettings batchResultSettings = batchSettings.batchResultSettings;
		
		// ZIPOUT
		
		// Combo x,y,z... parameter spatial grid position
		int pos[] = new int[numParameterGroups];
		
		// The temp scenario used to generate the xml.
		XMLModifier temp;
		
		for(int comboNo = 0; comboNo < combinations; comboNo++)
		{
			// Directory Gen >> MOVED
			
			// Create a copy of the base scenario
			temp = new XMLModifier();
			
			// String schemaText = config.getConfigSchema();
			
			// System.out.println(baseScenarioText);
			// System.out.println(schemaText);
			
			temp.loadConfig(baseScenarioText);
			
			StringBuilder itemName = new StringBuilder();
			
			// Start of log line + itemName
			itemName.append("Combo " + comboNo);
			
			StringBuilder comboPosString = new StringBuilder();
			
			comboPosString.append("ComboPos(");
			ArrayList<Integer> tempCoord = new ArrayList<Integer>();
			ArrayList<Float> tempCoordValues = new ArrayList<Float>();
			
			for(int p = 0; p < numParameterGroups; p++)
			{
				// Increment this parameter? (avoid increment the first combo c>0)
				if(((comboNo % incrementMods[p]) == 0) && (comboNo > 0))
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
					
					// REF xmlmod.changeGroupVariableValue("Agents.SimpleAgent", "Name", "Predator", "InitialNumbers","50");
					
					// TODO break if return invalid
					boolean status = temp.changeGroupVariableValue(path[p], "Name", groupName[p], parameterName[p],
					String.valueOf(value[p]));
					
					// OLD temp.changeValue(groupSection, parameterName[p], (int) value[p]);
				}
				else
				{
					// Log line middle
					itemName.append(" " + path[p] + "." + parameterName[p] + " " + value[p]);
					
					// TODO break if return invalid
					boolean completed = temp.changeSingleVariableValue(path[p], parameterName[p], String.valueOf(
					value[p]));
					
				}
				
				// Set the pos and val
				tempCoord.add(pos[p]);
				tempCoordValues.add((float) JCMath.round(value[p], 7));
				
				comboPosString.append(String.valueOf(pos[p]));
				if(p < (numParameterGroups - 1))
				{
					comboPosString.append('x');
				}
				
			}
			// Log line end
			comboPosString.append(")");
			log.debug(comboPosString.toString());
			log.debug(itemName.toString());
			
			// Add the generated item combo.
			try
			{
				byte[] configBytes = temp.getConfigXML().getBytes("ISO-8859-1");
				
				// Add to disk cache and get an cache index for this itemId/config pair
				int cacheIndex = itemStore.addData(configBytes);
				
				// Add the item to the temp combo list (samples created later) 1==Sample one with samples starting from base of 1
				tempComboItemList.add(new BatchItem(comboNo, 1, batchId, itemName.toString(), cacheIndex, tempCoord,
				tempCoordValues, batchResultSettings.ResultsEnabled));
			}
			catch(UnsupportedEncodingException e)
			{
				log.error("Configuration ISO-8859-1 file encoding was reported as not supported");
				
				e.printStackTrace();
			}
			catch(IOException e)
			{
				log.error("Error adding item to disk cache");
				e.printStackTrace();
			}
			
			// Update the current progress
			progress.tick();
			
			// Avoid div by zero on <1 combinations
			if(combinations > 1)
			{
				// Every 1%
				if((comboNo % (combinations / 1)) == 0)
				{
					log.info((int) progress.progressAsPercentage() + "%");
				}
			}
			
			// END COMBO
		}
		
		log.info((int) progress.progressAsPercentage() + "%");
		
		// All the items need to get processed, but the estimated total time (see getETT()) can be influenced by their processing order.
		// Randomise items in an attempt to reduce influence of item difficulty increasing/decreasing with combination order.
		Collections.shuffle(tempComboItemList);
		
		for(BatchItem comboItem : tempComboItemList)
		{
			// Add Sample 1
			// destinationItemList.add(comboItem);
			itemManager.addItem(comboItem);
			
			// Number Item Samples
			// int itemSamples = batchResultSettings.ItemSamples;
			
			// SID/SampleId is 1 base/ 1=first sample - each additional sample for the item will have the same item id and cacheIndex.
			for(int sid = 2; sid < (itemSamples + 1); sid++)
			{
				// Item Id == combo Id
				int comboNo = comboItem.getItemId();
				int cItemBatchId = comboItem.getBatchId();
				String itemName = comboItem.getItemName();
				
				// Identical samples use the same cacheIndex to get the same config
				int cacheIndex = comboItem.getCacheIndex();
				
				ArrayList<Integer> tempCoord = comboItem.getCoordinates();
				ArrayList<Float> tempCoordValues = comboItem.getCoordinatesValues();
				
				boolean storeStats = comboItem.hasStatsEnabled();
				
				// sid identifies the sample with in each combo.
				// destinationItemList.add(new BatchItem(comboNo, sid, cItemBatchId, itemName, cacheIndex, tempCoord, tempCoordValues, storeStats));
				
				itemManager.addItem(new BatchItem(comboNo, sid, cItemBatchId, itemName, cacheIndex, tempCoord,
				tempCoordValues, storeStats));
				
			}
		}
		
		tempComboItemList = null;
		
		// itemsCount = destinationItemList.size();
		itemsCount = itemManager.getTotalItems();
		
		// TODO validate generation
		needGenerated = false;
		
		return true;
	}
	
	@Override
	public int getGeneratedItemCount()
	{
		return itemsCount;
	}
	
	@Override
	public ArrayList<String> getParameters()
	{
		return parameters;
	}
}
