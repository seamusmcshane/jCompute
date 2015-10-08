package jCompute.Scenario;

import jCompute.Stats.StatGroupSetting;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.transform.stream.StreamSource;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.XmlSchemaObjectTable;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XML Configuration File Interpreter.
 */
public class ConfigurationInterpreter
{
	// SL4J Logger
	private static Logger log = LoggerFactory.getLogger(ConfigurationInterpreter.class);
	
	private XmlSchema schema;
	private XMLConfiguration configurationFile;
	private List<StatGroupSetting> statSettingsList;
	private String configurationFileText;
	
	/** Simulation End Events */
	private HashMap<String, Integer> endEvents;
	
	public ConfigurationInterpreter()
	{
		statSettingsList = new ArrayList<StatGroupSetting>();
	}
	
	private void readScenarioEndEvents()
	{
		int numEvent = getSubListSize("EndEvents", "Event");
		
		endEvents = new HashMap<String, Integer>();
		
		String section;
		
		for(int i = 0; i < numEvent; i++)
		{
			section = "EndEvents.Event(" + i + ")";
			
			endEvents.put(getStringValue(section, "Name"), getIntValue(section, "Value"));
		}
		
	}
	
	public void loadConfig(String text)
	{
		this.configurationFileText = text;
		InputStream stream;
		
		try
		{
			stream = new ByteArrayInputStream(configurationFileText.getBytes());
			configurationFile = new XMLConfiguration();
			configurationFile.setSchemaValidation(true);
			configurationFile.load(stream);
			
			XmlSchemaCollection schemaCol = new XmlSchemaCollection();
			schema = schemaCol.read(new StreamSource(new FileInputStream((String) configurationFile.getRoot().getAttribute(1).getValue())), null);
			
			readScenarioEndEvents();
			
		}
		catch(ConfigurationException e)
		{
			log.error("Error : " + e.toString() + " - " + e.getStackTrace()[0].getMethodName());
			log.error("Check the file matches its schema");
		}
		catch(FileNotFoundException e)
		{
			log.error("Schema File Not Found : " + e.toString() + " - " + e.getStackTrace()[0].getMethodName());
		}
		
	}
	
	public int getSubListSize(String section, String value)
	{
		return configurationFile.configurationsAt(section + "." + value).size();
	}
	
	public int getSubListSize(String path)
	{
		return configurationFile.configurationsAt(path).size();
	}
	
	public boolean hasStringValue(String section, String value)
	{
		try
		{
			configurationFile.getString(section + "." + value);
			return true;
		}
		catch(NoSuchElementException e)
		{
			return false;
		}
		
	}
	
	public boolean hasIntValue(String section, String value)
	{
		try
		{
			configurationFile.getInt(section + "." + value);
			return true;
		}
		catch(NoSuchElementException e)
		{
			return false;
		}
		
	}
	
	public boolean hasFloatValue(String section, String value)
	{
		try
		{
			configurationFile.getFloat(section + "." + value);
			return true;
		}
		catch(NoSuchElementException e)
		{
			return false;
		}
		
	}
	
	public boolean hasDoubleValue(String section, String value)
	{
		try
		{
			configurationFile.getDouble(section + "." + value);
			
			return true;
		}
		catch(NoSuchElementException e)
		{
			return false;
		}
		
	}
	
	public String getStringValue(String section, String value)
	{
		return configurationFile.getString(section + "." + value);
	}
	
	public boolean getBooleanValue(String section, String value)
	{
		return configurationFile.getBoolean(section + "." + value);
	}
	
	public boolean getBooleanValue(String section, String value, boolean defaultValue)
	{
		return configurationFile.getBoolean(section + "." + value, defaultValue);
	}
	
	public int getIntValue(String section, String value)
	{
		return configurationFile.getInt(section + "." + value);
	}
	
	public int getIntValue(String section, String value, int defaultValue)
	{
		return configurationFile.getInt(section + "." + value, defaultValue);
	}
	
	public float getFloatValue(String section, String value, float defaultValue)
	{
		return configurationFile.getFloat(section + "." + value, defaultValue);
	}
	
	public float getFloatValue(String section, String value)
	{
		return configurationFile.getFloat(section + "." + value);
	}
	
	public double getDoubleValue(String section, String value)
	{
		return configurationFile.getDouble(section + "." + value);
	}
	
	public XMLConfiguration scenarioFile()
	{
		return configurationFile;
	}
	
	public double getFileVersion()
	{
		return Double.parseDouble(configurationFile.getString("Header.Version", "0.00"));
	}
	
	public String getScenarioType()
	{
		return configurationFile.getString("Header.Type", "Scenario Type Not Set!!!");
	}
	
	/**
	 * Only Sub Class add StatSettings
	 * @param statSetting
	 */
	protected void addStatSettings(StatGroupSetting statSetting)
	{
		statSettingsList.add(statSetting);
	}
	
	public void readStatSettings()
	{
		int statisticsGroups = getSubListSize("Statistics", "Stat");
		
		String section;
		for(int i = 0; i < statisticsGroups; i++)
		{
			section = "Statistics.Stat(" + i + ")";
			addStatSettings(new StatGroupSetting(getStringValue(section, "Name"), getBooleanValue(section, "Enabled"), getBooleanValue(section, "TotalStat"),
					getBooleanValue(section, "Graph"), getIntValue(section, "StatSampleRate"), getIntValue(section, "GraphSampleWindow")));
		}
		
		log.debug("Statistics " + statisticsGroups);
	}
	
	public List<StatGroupSetting> getStatGroupSettingsList()
	{
		return statSettingsList;
	}
	
	public boolean endEventIsSet(String eventName)
	{
		return endEvents.containsKey(eventName);
	}
	
	public int getEventValue(String eventName)
	{
		return endEvents.get(eventName);
	}
	
	// Change a value in the XML
	public void changeValue(String section, String field, Object value)
	{
		configurationFile.setProperty(section + "." + field, value);
	}
	
	public String getScenarioXMLText()
	{
		ByteArrayOutputStream baos = null;
		
		try
		{
			baos = new ByteArrayOutputStream();
			configurationFile.save(baos);
		}
		catch(ConfigurationException e)
		{
			baos = null;
			log.error("Error getting scenario XML");
		}
		
		if(baos == null)
		{
			return "";
		}
		
		return baos.toString();
	}
	
	public void dumpXML()
	{
		ConfigurationNode rootNode = configurationFile.getRoot();
		
		String rootName = rootNode.getName();
		
		log.debug("Root Element :" + rootName);
		log.debug("Schema : " + rootNode.getAttribute(1).getValue());
		
		Iterator<String> itr = configurationFile.getKeys();
		
		while(itr.hasNext())
		{
			
			String field = itr.next();
			
			log.debug(rootName + "." + field);
			log.debug(stripXMLPath(field) + " : ");
			
			XmlSchemaType type = findSubNodeDataType(rootName, field);
			
			// Need to keep path during iteration AGENT.size WORLD.size
			
			if(type != null)
			{
				log.debug(type.getQName().getLocalPart());
				log.debug(" - " + getValueToString(field, type) + "\n");
			}
			
		}
		log.debug("");
	}
	
	public String getValueToString(String path, XmlSchemaType type)
	{
		String value = "";
		if(type.getQName().getLocalPart().equals("boolean"))
		{
			value = String.valueOf(configurationFile.getBoolean(path));
		}
		else if(type.getQName().getLocalPart().equals("string"))
		{
			value = String.valueOf(configurationFile.getString(path));
		}
		else if(type.getQName().getLocalPart().equals("decimal"))
		{
			value = String.valueOf(configurationFile.getDouble(path));
		}
		else if(type.getQName().getLocalPart().equals("integer"))
		{
			value = String.valueOf(configurationFile.getInt(path));
		}
		else
		{
			value = "UNDEFINED-DATATYPE";
		}
		
		return value;
	}
	
	private String stripXMLPath(String path)
	{
		return path.substring(path.lastIndexOf(".") + 1);
	}
	
	// If type is null then the target does not exist.
	public String findDataType(String target)
	{
		XmlSchemaType type = findSubNodeDataType(configurationFile.getRoot().getName(), target);
		
		return type.getQName().getLocalPart();
	}
	
	// If type is null then the target does not exist.
	private XmlSchemaType findSubNodeDataType(String startPath, String target)
	{
		String targetPath = startPath + "." + target;
		
		XmlSchemaType dataType = null;
		XmlSchemaObjectTable elements = schema.getElements();
		
		Iterator<XmlSchemaElement> itr2 = elements.getValues();
		while(itr2.hasNext())
		{
			XmlSchemaElement element = itr2.next();
			
			if(element.getSchemaType().getClass().equals(XmlSchemaComplexType.class))
			{
				dataType = transverseComplexType(targetPath, element.getName(), element);
				
				if(dataType != null)
				{
					break;
				}
			}
			else
			{
				if(isSimpleTypeTarget(targetPath, element.getName(), element))
				{
					dataType = element.getSchemaType();
					break;
				}
				
			}
			
		}
		return dataType;
	}
	
	// Process a simple type for the target - type will not be null if found.
	private boolean isSimpleTypeTarget(String target, String path, XmlSchemaElement schemaElement)
	{
		if(path.equals(target))
		{
			return schemaElement.getName().equalsIgnoreCase(stripXMLPath(target));
		}
		
		return false;
	}
	
	// Iterate over a complex xml type
	private XmlSchemaType transverseComplexType(String target, String path, XmlSchemaElement schemaElement)
	{
		XmlSchemaType dataType = null;
		
		// Gets the list of items in the complex type sequence.
		XmlSchemaObjectCollection soc = ((XmlSchemaSequence) ((XmlSchemaComplexType) schemaElement.getSchemaType()).getParticle()).getItems();
		
		Iterator<XmlSchemaElement> iterator = soc.getIterator();
		while(iterator.hasNext())
		{
			XmlSchemaElement element = iterator.next();
			
			if(element.getSchemaType().getClass().equals(XmlSchemaComplexType.class))
			{
				dataType = transverseComplexType(target, path + "." + element.getName(), element);
				
				if(dataType != null)
				{
					break;
				}
			}
			else
			{
				if(isSimpleTypeTarget(target, path + "." + element.getName(), element))
				{
					dataType = element.getSchemaType();
					break;
				}
			}
		}
		
		return dataType;
		
	}
	
	public List<StatGroupSetting> getStats()
	{
		return statSettingsList;
	}
	
	public HashMap<String, Integer> getEndEvents()
	{
		return endEvents;
	}
	
	public void setStats(List<StatGroupSetting> statSettingsList)
	{
		this.statSettingsList = statSettingsList;
	}
	
	public void setEndEvents(HashMap<String, Integer> endEvents)
	{
		this.endEvents = endEvents;
	}
	
	public String getText()
	{
		return configurationFileText;
	}
}