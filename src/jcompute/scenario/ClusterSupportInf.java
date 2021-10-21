package jcompute.scenario;

import java.util.List;

import jcompute.batch.itemgenerator.ItemGenerator;
import jcompute.batch.itemgenerator.ItemGeneratorConfigInf;
import jcompute.batch.itemgenerator.Parameter;
import jcompute.batch.itemstore.ItemStore;
import jcompute.batch.log.item.logger.BatchItemLogInf;

public interface ClusterSupportInf
{
	/**
	 * Only required in cluster mode.
	 * Note The standard item generator is available and recommended.
	 * 
	 * @return A configuration for the Item generator type used.
	 */
	public ItemGeneratorConfigInf getItemGeneratorConfig(List<Parameter> parameterList, String baseScenarioText,
	int itemSamples);
	
	/**
	 * Only required in cluster mode.
	 * Note The standard item generator is available and recommended.
	 * 
	 * @return the item generator which can generate batch items for this scenario type.
	 */
	public ItemGenerator getItemGenerator();
	
	/**
	 * Only required in cluster mode.
	 * Item configs generated by the Item Generator can use signigifant amounts of memory.
	 * Allows controlling where the Item configs are stored.
	 * 
	 * @return An Itemstore in which to place item configs.
	 */
	public ItemStore getItemStore();
	
	/**
	 * @return
	 */
	public BatchItemLogInf getItemLogWriter();
}