package jcompute.batch.log.item.custom.logger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CustomItemResultsSettings
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(CustomItemResultsSettings.class);
	
	// Custom Item Results
	public final boolean Enabled;
	public final ItemLogExportFormat Format;
	public final boolean BatchHeaderInResult;
	public final boolean ItemInfoInResult;
	
	public CustomItemResultsSettings(boolean customResultsEnabled, String customItemResultsFormat, boolean batchHeaderInCustomResult,
	boolean itemInfoInCustomResult)
	{
		Format = ItemLogExportFormat.fromString(customItemResultsFormat);
		
		if(Format != null)
		{
			this.Enabled = customResultsEnabled;
		}
		else
		{
			// Sanity check - this is a bug somewhere else.
			this.Enabled = false;
			
			log.error("Disabling Custom results, as no ItemLogExportFormat set.");
		}
		
		this.BatchHeaderInResult = batchHeaderInCustomResult;
		
		this.ItemInfoInResult = itemInfoInCustomResult;
	}
}
