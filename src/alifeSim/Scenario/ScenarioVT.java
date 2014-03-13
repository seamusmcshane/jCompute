package alifeSim.Scenario;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.XmlSchemaObjectTable;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaType;

import alifeSim.Stats.StatGroupSetting;

/**
 * Base Scenario File reader.
 */

public class ScenarioVT
{
	XmlSchema schema;
	private XMLConfiguration scenario;
	private List<StatGroupSetting> statSettingsList;
	protected String scenarioText;

	/** Simulation End Events */
	private HashMap<String, Integer> endEvents;

	public ScenarioVT()
	{
		statSettingsList = new ArrayList<StatGroupSetting>();
	}

	private void readScenarioEndEvents()
	{
		int numEvent = getSubListSize("EndEvents", "Event");

		endEvents = new HashMap<String, Integer>();

		String section;

		for (int i = 0; i < numEvent; i++)
		{
			section = "EndEvents.Event(" + i + ")";

			endEvents.put(getStringValue(section, "Name"), getIntValue(section, "Value"));
		}

	}

	public void loadConfig(String text)
	{
		this.scenarioText = text;
		InputStream stream;

		try
		{
			stream = new ByteArrayInputStream(text.getBytes());
			scenario = new XMLConfiguration();
			scenario.setSchemaValidation(true);
			scenario.load(stream);

			System.out.println();

			XmlSchemaCollection schemaCol = new XmlSchemaCollection();
			schema = schemaCol.read(new StreamSource(new FileInputStream((String) scenario.getRoot().getAttribute(1).getValue())), null);
			
			readScenarioEndEvents();

		}
		catch (ConfigurationException e)
		{
			System.out.println("Error : " + e.toString() + " - " + e.getStackTrace()[0].getMethodName());
		}
		catch (FileNotFoundException e)
		{
			System.out.println("Schema File Not Found : " + e.toString() + " - " + e.getStackTrace()[0].getMethodName());
		}
	}

	public int getSubListSize(String section, String value)
	{
		return scenario.configurationsAt(section + "." + value).size();
	}
	
	public int getSubListSize(String path)
	{
		return scenario.configurationsAt(path).size();
	}

	public boolean hasStringValue(String section, String value)
	{
		try
		{
			scenario.getString(section + "." + value);
			return true;
		}
		catch (NoSuchElementException e)
		{
			return false;
		}

	}

	public boolean hasIntValue(String section, String value)
	{
		try
		{
			scenario.getInt(section + "." + value);
			return true;
		}
		catch (NoSuchElementException e)
		{
			return false;
		}

	}

	public boolean hasFloatValue(String section, String value)
	{
		try
		{
			scenario.getFloat(section + "." + value);
			return true;
		}
		catch (NoSuchElementException e)
		{
			return false;
		}

	}

	public boolean hasDoubleValue(String section, String value)
	{
		try
		{
			scenario.getDouble(section + "." + value);

			return true;
		}
		catch (NoSuchElementException e)
		{
			return false;
		}

	}

	public String getStringValue(String section, String value)
	{
		return scenario.getString(section + "." + value);
	}

	public boolean getBooleanValue(String section, String value)
	{
		return scenario.getBoolean(section + "." + value);
	}

	public int getIntValue(String section, String value)
	{
		return scenario.getInt(section + "." + value);
	}

	public float getFloatValue(String section, String value)
	{
		return scenario.getFloat(section + "." + value);
	}

	public double getDoubleValue(String section, String value)
	{
		return scenario.getDouble(section + "." + value);
	}

	public XMLConfiguration scenarioFile()
	{
		return scenario;
	}

	public double getScenarioVersion()
	{
		return Double.parseDouble(scenario.getString("Header.Version", "0.00"));
	}

	public String getScenarioType()
	{
		return scenario.getString("Header.Type", "Scenario Type Not Set!!!");
	}

	/**
	 * Only Sub Class add StatSettings
	 * 
	 * @param statSetting
	 */
	protected void addStatSettings(StatGroupSetting statSetting)
	{
		statSettingsList.add(statSetting);
	}

	/** Only called by sub class */
	protected void readStatSettings()
	{
		int statisticsGroups = getSubListSize("Statistics", "Stat");

		String section;
		for (int i = 0; i < statisticsGroups; i++)
		{
			section = "Statistics.Stat(" + i + ")";
			addStatSettings(new StatGroupSetting(getStringValue(section, "Name"), getBooleanValue(section, "Enabled"), getBooleanValue(section, "TotalStat"), getBooleanValue(section, "Graph"), getIntValue(section, "StatSampleRate"), getIntValue(section, "GraphSampleWindow")));
		}

		System.out.println("Statistics " + statisticsGroups);
	}

	public List<StatGroupSetting> getStatGroupSettingsList()
	{
		return statSettingsList;
	}

	/*
	 * public ScenarioKeyValuePair<String, Integer> getEndEvent(String
	 * eventName) { ScenarioKeyValuePair<String, Integer> event = null;
	 * 
	 * if(endEvents.containsKey(eventName)) { event = new
	 * ScenarioKeyValuePair<String,
	 * Integer>(eventName,endEvents.get(eventName)); }
	 * 
	 * return event; }
	 */

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
		scenario.setProperty(section + "." + field, value);
	}
	
	public String getScenarioXMLText()
	{
		ByteArrayOutputStream baos = null;
		
		try
		{
			baos = new ByteArrayOutputStream();
			scenario.save(baos);
		}
		catch (ConfigurationException e)
		{
			baos = null;
			System.out.println("Error getting scenario XML");
		}
		
		if(baos == null)
		{
			return "";
		}		
		
		return baos.toString();
	}
	
	public void dumpXML()
	{
		System.out.println("TODO");

		ConfigurationNode rootNode = scenario.getRoot();

		String rootName = rootNode.getName();

		System.out.println("Root Element :" + rootName);
		System.out.println("Schema : " + rootNode.getAttribute(1).getValue());

		Iterator<String> itr = scenario.getKeys();
		
		while(itr.hasNext()) 
		{
			 
			String field = itr.next();
			
			System.out.println(rootName+"."+field);
			System.out.print(stripXMLPath(field) + " : ");
			
			XmlSchemaType type = findSubNodeDataType(rootName,field);

			// Need to keep path during iteration AGENT.size WORLD.size
			
			if(type!=null)
			{
				System.out.print(type.getQName().getLocalPart());
				System.out.print(" - " + getValueToString(field,type)+"\n");
			}

		}
		System.out.println("");

		// XmlSchemaElement element = schema.getElementByName(new
		// QName("","Scenario"));
		// System.out.println(element.getName());

		/*XmlSchemaType type = findDataType("ReproductionAndSurvivalDivisor");

		System.out.println(type.toString());*/
		
		
		//System.out.println(schema.getElementByName(test));

		//System.out.println(test.getLocalPart());
		
		// schema.write(System.out);
	}
		
	public String getValueToString(String path,XmlSchemaType type)
	{
		String value = "";
		if(type.getQName().getLocalPart().equals("boolean"))
		{
			value = String.valueOf(scenario.getBoolean(path));
		}
		else if(type.getQName().getLocalPart().equals("string"))
		{
			value = String.valueOf(scenario.getString(path));
		}
		else if(type.getQName().getLocalPart().equals("decimal"))
		{
			value = String.valueOf(scenario.getDouble(path));
		}
		else if (type.getQName().getLocalPart().equals("integer"))
		{
			value = String.valueOf(scenario.getInt(path));
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
		XmlSchemaType type = findSubNodeDataType(scenario.getRoot().getName(),target);
		
		return type.getQName().getLocalPart();
	}
	
	// If type is null then the target does not exist.
	private XmlSchemaType findSubNodeDataType(String startPath,String target)
	{
		String targetPath = startPath+"."+target;
		
		XmlSchemaType dataType = null;
		XmlSchemaObjectTable elements = schema.getElements();

		Iterator<XmlSchemaElement> itr2 = elements.getValues();
		while (itr2.hasNext())
		{
			XmlSchemaElement element = itr2.next();
			
			/*System.out.println("NAME  : " + element.getName());
			System.out.println("TYPE  : " + element.getSchemaTypeName());*/
						
			if(element.getSchemaType().getClass().equals(XmlSchemaComplexType.class))
			{
				//System.out.println("NAME  : " + element.getName());
				dataType = transverseComplexType(targetPath,element.getName(),element);
				
				if(dataType!=null)
				{					
					break;
				}
			}
			else
			{
				if(isSimpleTypeTarget(targetPath,element.getName(),element))
				{
					dataType = element.getSchemaType();					
					break;
				}
				
			}
			
		}
		return dataType;
	}
	
	// Process a simple type for the target - type will not be null if found.
	private boolean isSimpleTypeTarget(String target, String path,XmlSchemaElement schemaElement)
	{
		//XmlSchemaType type = null;
		/*System.out.println("QNAME : " + schemaElement.getQName().toString());
		System.out.println("NAME  : " + schemaElement.getName());
		System.out.println("TYPE  : " + schemaElement.getSchemaTypeName());
		
		System.out.println("NS	  : " + schemaElement.getQName().getNamespaceURI());
		System.out.println("LC	  : " + schemaElement.getQName().getLocalPart());
		System.out.println("PX	  : " + schemaElement.getQName().getPrefix());*/
		//System.out.println("Path : " + path + " Target : " + target);

		if(path.equals(target))
		{
			return schemaElement.getName().equalsIgnoreCase(stripXMLPath(target));
		}
		
		return false;
	}
	
	// Iterate over a complex xml type
	private XmlSchemaType transverseComplexType(String target,String path,XmlSchemaElement schemaElement)
	{
		XmlSchemaType dataType = null;
		
		// Gets the list of items in the complex type sequence.
		XmlSchemaObjectCollection soc = ((XmlSchemaSequence)((XmlSchemaComplexType) schemaElement.getSchemaType()).getParticle()).getItems();
		
		Iterator<XmlSchemaElement> iterator = soc.getIterator();
		while (iterator.hasNext())
		{
			XmlSchemaElement element = iterator.next();
			
			if(element.getSchemaType().getClass().equals(XmlSchemaComplexType.class))
			{
				dataType = transverseComplexType(target,path+"."+element.getName(),element);
				
				if(dataType!=null)
				{
					break;
				}
			}
			else
			{
				if(isSimpleTypeTarget(target,path+"."+element.getName(),element))
				{					
					/*System.out.println(element.getName());
					
					System.out.println(target);*/
					
					dataType = element.getSchemaType();
					
					//System.out.println( element.getSchemaTypeName());

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
	
}