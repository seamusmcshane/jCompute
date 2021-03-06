package jcompute.testing.xml;

import jcompute.configuration.JComputeConfigurationUtility;
import jcompute.configuration.batch.BatchJobConfig;

public class JBatchXMLTest
{
	public static void main(String args[])
	{
		String filePath = "batch/SAPP_v1_Test.batch.xml";
		
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
		
		BatchJobConfig config = (BatchJobConfig) JComputeConfigurationUtility.XMLtoConfig(filePath, BatchJobConfig.class);
		
		System.out.println(config.getHeader().getVersion());
		System.out.println(config.getHeader().getType());
		
		System.out.println(config.getConfig().getEndEventsList().get(0).getName());
		
		JComputeConfigurationUtility.ConfigToXML(config, "test");
	}
}
