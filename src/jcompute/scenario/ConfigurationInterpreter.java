package jcompute.scenario;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import javax.xml.transform.stream.StreamSource;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.XmlSchemaObjectTable;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaType;

import jcompute.stats.StatGroupSetting;

/**
 * XML Configuration File Interpreter.
 */
public class ConfigurationInterpreter
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(ConfigurationInterpreter.class);
	
	// Configuration file and associated schema.
	private XMLConfiguration configurationFile;
	private XmlSchema schema;
	
	// Lazy XML to String updating - updates the string if needed.
	private boolean latchNeedsUpdated;
	private String latchedFileText;
	
	// Statistics
	private List<StatGroupSetting> statSettingsList;
	
	// End Events
	private HashMap<String, Integer> endEvents;
	
	public ConfigurationInterpreter()
	{
		// Statistics settings are managed at the top level.
		statSettingsList = new ArrayList<StatGroupSetting>();
	}
	
	/*
	 * *****************************************************************************************************
	 * Loader
	 *****************************************************************************************************/
	
	public boolean loadConfig(String text)
	{
		boolean status = true;
		
		try
		{
			// Load the text string into the XMLConfiguration
			ByteArrayInputStream configurationTextInputStream = new ByteArrayInputStream(text.getBytes());
			configurationFile = new XMLConfiguration();
			configurationFile.setSchemaValidation(true);
			configurationFile.load(configurationTextInputStream);
			
			// Read the noNamespaceSchemaLocation attribute (expected to be 1) containing the relative schema path.
			FileInputStream schemaFileInputStream = new FileInputStream((String) configurationFile.getRoot().getAttribute(1).getValue());
			
			// Load the schema file and create an XmlSchema object.
			XmlSchemaCollection schemaCol = new XmlSchemaCollection();
			schema = schemaCol.read(new StreamSource(schemaFileInputStream), null);
			
			// The are end events types which are supported globally and some that are specific per scenario.
			// They are read and populated here if they exist - it is up to the schema to allow the element to exist.
			
			// Get the event number - this is ok to be 0 if the file does not have the section end events
			int numEvent = getSubListSize("EndEvents", "Event");
			
			if(numEvent > 0)
			{
				endEvents = new HashMap<String, Integer>();
				
				String section;
				
				for(int i = 0; i < numEvent; i++)
				{
					section = "EndEvents.Event(" + i + ")";
					
					endEvents.put(getStringValue(section, "Name"), getIntValue(section, "Value"));
				}
			}
			
			int statisticsGroups = getSubListSize("Statistics", "Stat");
			
			if(statisticsGroups > 0)
			{
				String section;
				for(int i = 0; i < statisticsGroups; i++)
				{
					section = "Statistics.Stat(" + i + ")";
					statSettingsList.add(new StatGroupSetting(getStringValue(section, "Name"), getBooleanValue(section, "Enabled"), getBooleanValue(section,
					"TotalStat"), getBooleanValue(section, "Graph"), getIntValue(section, "StatSampleRate"), getIntValue(section, "GraphSampleWindow")));
				}
			}
			
			schemaFileInputStream.close();
			configurationTextInputStream.close();
			
			// Store the text as is
			latchedFileText = text;
			latchNeedsUpdated = false;
		}
		catch(ConfigurationException e)
		{
			status = false;
			
			log.error("Error : " + e.toString() + " - " + e.getStackTrace()[0].getMethodName());
			log.error("Check the file matches its schema");
		}
		catch(FileNotFoundException e)
		{
			status = false;
			
			log.error("Schema File Not Found : " + e.toString() + " - " + e.getStackTrace()[0].getMethodName());
		}
		catch(IOException e)
		{
			status = false;
			
			log.error("Error converting text to stream : " + e.toString() + " - " + e.getStackTrace()[0].getMethodName());
		}
		
		return status;
	}
	
	/*
	 * *****************************************************************************************************
	 * Scenario methods
	 *****************************************************************************************************/
	
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
		return configurationFile.getString("Header.Type", ScenarioInf.INVALID);
	}
	
	public boolean endEventIsSet(String eventName)
	{
		if(endEvents == null)
		{
			return false;
		}
		
		return endEvents.containsKey(eventName);
	}
	
	public int getEventValue(String eventName)
	{
		if(endEvents == null)
		{
			return Integer.MIN_VALUE;
		}
		
		return endEvents.get(eventName);
	}
	
	public List<StatGroupSetting> getStatGroupSettingsList()
	{
		return statSettingsList;
	}
	
	/*
	 * *****************************************************************************************************
	 * Section/SubSection Size/Existence methods
	 *****************************************************************************************************/
	
	public int getListSize(String section)
	{
		return configurationFile.configurationsAt(section).size();
	}
	
	public int getSubListSize(String section, String value)
	{
		return configurationFile.configurationsAt(section + "." + value).size();
	}
	
	public int getSubListSize(String path)
	{
		return configurationFile.configurationsAt(path).size();
	}
	
	public boolean hasSection(String string)
	{
		return(getListSize(string) > 0);
	}
	
	/*
	 * *****************************************************************************************************
	 * Value Existence methods
	 *****************************************************************************************************/
	
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
	
	/*
	 * *****************************************************************************************************
	 * Value Getter methods
	 *****************************************************************************************************/
	
	public String getStringValue(String section, String value)
	{
		return configurationFile.getString(section + "." + value);
	}
	
	public String getStringValue(String section, String value, String defaultValue)
	{
		return configurationFile.getString(section + "." + value, defaultValue);
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
	
	public double getDoubleValue(String section, String value, double defaultValue)
	{
		return configurationFile.getDouble(section + "." + value, defaultValue);
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
	
	/*
	 * *****************************************************************************************************
	 * Editing Methods
	 *****************************************************************************************************/
	
	// Change a value in the XML
	public void changeValue(String section, String field, Object value)
	{
		latchNeedsUpdated = true;
		
		configurationFile.setProperty(section + "." + field, value);
	}
	
	// Remove a section from the configuration
	public void removeSection(String string)
	{
		latchNeedsUpdated = true;
		
		configurationFile.clearTree(string);
	}
	
	/*
	 * *****************************************************************************************************
	 * Conformity Methods
	 *****************************************************************************************************/
	
	// Check that at least one element equals the value.
	// Will return false if the value is not found or the path and or field does not exist.
	public boolean atLeastOneElementEqualValue(String path, String field, boolean value)
	{
		boolean status = false;
		
		List<HierarchicalConfiguration> servers = configurationFile.configurationsAt(path);
		for(HierarchicalConfiguration server : servers)
		{
			if(server.getBoolean(field, false))
			{
				status = true;
				break;
			}
		}
		
		return status;
	}
	
	/*
	 * *****************************************************************************************************
	 * Text Conversion Related methods
	 *****************************************************************************************************/
	
	private void updateLatchText()
	{
		if(!latchNeedsUpdated)
		{
			return;
		}
		
		// Update the text from the XML file
		// As it was changed.
		
		latchedFileText = getScenarioXMLText();
		latchNeedsUpdated = false;
	}
	
	// Returns current value of the configuration as a string, updating the string if necessary.
	public String getText()
	{
		// Update the latch text if necessary
		updateLatchText();
		
		return latchedFileText;
	}
	
	// Converts the XMLConfiguration to XML formated text string
	// ByteArrayOutputStream - close is a no op - null allows gc
	@SuppressWarnings("resource")
	private String getScenarioXMLText()
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		try
		{
			configurationFile.save(baos);
		}
		catch(ConfigurationException e)
		{
			log.error("Error getting scenario XML");
			
			baos = null;
			
			return "";
		}
		
		return baos.toString();
	}
	
	/*
	 * *****************************************************************************************************
	 * Debug Methods
	 *****************************************************************************************************/
	
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
	
	/*
	 * *****************************************************************************************************
	 * Internal XML Processing Methods
	 *****************************************************************************************************/
	
	// If type is null then the target does not exist.
	private XmlSchemaType findSubNodeDataType(String startPath, String target)
	{
		String targetPath = startPath + "." + target;
		
		XmlSchemaType dataType = null;
		XmlSchemaObjectTable elements = schema.getElements();
		
		@SuppressWarnings("unchecked")
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
	
	// Iterate over a complex xml type (recursive)
	private XmlSchemaType transverseComplexType(String target, String path, XmlSchemaElement schemaElement)
	{
		XmlSchemaType dataType = null;
		
		// Gets the list of items in the complex type sequence.
		XmlSchemaObjectCollection soc = ((XmlSchemaSequence) ((XmlSchemaComplexType) schemaElement.getSchemaType()).getParticle()).getItems();
		
		@SuppressWarnings("unchecked")
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
}