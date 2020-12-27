package jcompute.testing.xml;

import jcompute.configuration.JComputeConfigurationUtility;
import jcompute.configuration.batch.BatchJobConfig;

public class JXMLTest
{
	public static void main(String args[])
	{
		String filePath = "batch/DemoBatch.batch";
		
		/*File file = new File(filePath);
		
		JAXBContext jaxbContext = JAXBContext.newInstance(BatchJobConfig.class);
		
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		
		BatchJobConfig e = (BatchJobConfig) jaxbUnmarshaller.unmarshal(file);
		
		System.out.println(e.getHeader().getVersion());
		System.out.println(e.getHeader().getType());
		
		System.out.println(e.getConfig().getBaseScenarioFileName());
		System.out.println(e.getConfig().getItemSamples());
		
		System.out.println(e.getLog().hasInfoLogEnabled());
		System.out.println(e.getLog().hasItemLogEnabled());
		
		System.out.println(e.getStats().isStoreEnabled());
		System.out.println(e.getStats().isTraceResultsEnabled());
		System.out.println(e.getStats().isHasBDFCResult());
		System.out.println(e.getStats().isSingleArchiveEnabled());
		System.out.println(e.getStats().getCompressionLevel());
		System.out.println(e.getStats().getStatsExportDir());
		System.out.println(e.getStats().getGroupDir());
		System.out.println(e.getStats().getSubGroupDir());*/
		
		BatchJobConfig config = (BatchJobConfig) JComputeConfigurationUtility.XMLtoConfig(filePath, new BatchJobConfig());
		
		System.out.println(config.getHeader().getVersion());
		System.out.println(config.getHeader().getType());
		
		JComputeConfigurationUtility.ConfigToXML(config, "test");
	}
}
