package jCompute.Batch.ItemGenerator;

import java.util.ArrayList;
import java.util.zip.ZipOutputStream;

public abstract class ItemGenerator
{
	public abstract void generate(double[] progress1dArray, String baseScenarioText, String batchStatsExportDir, boolean storeStats,
	boolean statsMethodSingleArchive, int singleArchiveCompressionLevel, int bosBufferSize);

	public abstract String[] getGroupNames();

	public abstract String[] getParameterNames();

	public abstract ArrayList<String> getParameters();

	public abstract ZipOutputStream getResultsZipOut();

	public abstract int getGeneratedItemCount();

}
