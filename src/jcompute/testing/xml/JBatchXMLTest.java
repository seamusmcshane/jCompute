package jcompute.testing.xml;

import java.io.File;
import java.io.IOException;

import jcompute.configuration.JComputeConfiguration;
import jcompute.configuration.JComputeConfigurationUtility;
import jcompute.configuration.batch.BatchJobConfig;
import jcompute.configuration.support.ScenarioTestConfiguration;
import jcompute.scenario.ScenarioInf;
import jcompute.scenario.ScenarioPluginManager;
import jcompute.util.text.JCText;

public class JBatchXMLTest
{
	public static void main(String args[]) throws IOException
	{
		String baseDirectoryPath = "batch" + File.separator + "TEST";
		String batchPath = baseDirectoryPath + File.separator + "SAPP_v1_Test.batch.xml";
		
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
		
		BatchJobConfig config = (BatchJobConfig) JComputeConfigurationUtility.XMLtoConfig(batchPath,
		BatchJobConfig.class);
		
		System.out.println("Batch Version " + config.getHeader().getVersion());
		System.out.println("Batch Type" + config.getHeader().getType());
		System.out.println("End Event" + config.getConfig().getEndEventsList().get(0).getName());
		
		String baseScenarioFileName = config.getConfig().getBaseScenarioFileName();
		
		// Assumes the file is in the same dir as the batch file
		String baseScenaroFilePath = baseDirectoryPath + File.separator + baseScenarioFileName;
		
		System.out.println("Base Scenario " + baseScenaroFilePath);
		
		// Attempt to load the text into a string
		String tempText = JCText.textFileToString(baseScenaroFilePath);
		
		// We need to load the plugins in this test
		ScenarioPluginManager.loadPlugins();
		
		ScenarioInf baseScenario = ScenarioPluginManager.getScenario(tempText);
		
		Class<?> scenarioConfigClass = baseScenario.getScenarioConfigClass();
		
		// JComputeConfigurationUtility.ConfigToXML(config, "test");
		JComputeConfiguration tConfig = JComputeConfigurationUtility.XMLtoConfig(baseScenaroFilePath,
		scenarioConfigClass);
	}
}
