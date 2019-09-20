package jcompute.batch.itemgenerator;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jcompute.batch.BatchSettings;
import jcompute.batch.ItemManager;
import jcompute.batch.itemstore.ItemStore;
import jcompute.timing.ProgressObj;

public abstract class ItemGenerator
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(ItemGenerator.class);
	
	// Generation Progress
	protected boolean needGenerated;
	
	// Type interface methods
	
	public abstract ArrayList<String> getParameters();
	
	public abstract int getGeneratedItemCount();
	
	// Generators Name
	public abstract String getName();
	
	// The Generators generation method
	public abstract boolean subgenerator(int batchId, ProgressObj progress, ItemManager itemManager, ItemStore itemStore, BatchSettings batchSettings);
	
	// Call back from sub class.
	public ItemGenerator()
	{
		needGenerated = true;
	}
	
	public final boolean generate(int batchId, ProgressObj progress, ItemManager itemManager, ItemStore itemStore, BatchSettings batchSettings)
	{
		if((batchId < 0))
		{
			log.error("Batch Id less than zero : " + batchId);
			
			return false;
		}
		
		if(!needGenerated)
		{
			log.error(getName() + " got call to generate items when items already generated");
			
			return false;
		}
		
		return subgenerator(batchId, progress, itemManager, itemStore, batchSettings);
	}
}
