package jcompute.configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathExpressionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * XML File modifier.
 * 
 * @author Seamus McShane
 */
public class XMLModifier
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(XMLModifier.class);
	
	private final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	
	private Document document;
	
	public XMLModifier()
	{
	}
	
	/*
	 * *****************************************************************************************************
	 * Loader
	 *****************************************************************************************************/
	
	public boolean loadConfig(String configText)
	{
		
		if(configText == null)
		{
			// Nothing we can do with this
			return false;
		}
		
		if(configText.length() == 0)
		{
			// Can't really use an empty config
			return false;
		}
		
		// Create an input stream around the config text for the document builder
		ByteArrayInputStream configurationTextInputStream = new ByteArrayInputStream(configText.getBytes());
		
		DocumentBuilder builder;
		
		try
		{
			builder = factory.newDocumentBuilder();
		}
		catch(ParserConfigurationException e)
		{
			
			log.error(e.getMessage() + " - Error Creating DocumentBuilder for configuration - : " + configText);
			
			e.printStackTrace();
			
			return false;
		}
		
		try
		{
			document = builder.parse(configurationTextInputStream);
		}
		catch(SAXException e)
		{
			log.error(e.getMessage() + " - Error parsing configuration - : " + configText);
			
			e.printStackTrace();
			
			return false;
		}
		catch(IOException e)
		{
			log.error(e.getMessage() + " : " + configText);
			
			e.printStackTrace();
			
			return false;
		}
		
		return true;
	}
	
	public String getConfigXML()
	{
		StringWriter sw = new StringWriter();
		
		try
		{
			TransformerFactory tff = TransformerFactory.newInstance();
			
			Transformer tf = tff.newTransformer();
			
			tf.transform(new DOMSource(document), new StreamResult(sw));
		}
		catch(TransformerException e)
		{
			log.error(e.getMessage() + " Issue transforming document to string.");
			
			e.printStackTrace();
		}
		
		return sw.toString();
	}
	
	public boolean changeSingleVariableValue(String itemName, String variable, String newValue)
	throws XPathExpressionException
	{
		NodeList itemList = document.getElementsByTagName(itemName);
		
		// Not a single item if this is the case
		if(itemList.getLength() > 1)
		{
			System.out.println("Multiple Elements");
			
			return false;
		}
		else if(itemList.getLength() == 0)
		{
			System.out.println("Element not found");
			
			return false;
		}
		
		Node item = itemList.item(0);
		
		System.out.println("Len : " + itemList.getLength());
		System.out.println("Name : " + item.getNodeName());
		
		return changeVariableValue(item.getChildNodes(), variable, newValue);
	}
	
	public boolean changeGroupVariableValue(String groupPath, String nameElement, String nameValue, String variable,
	String newValue) throws XPathExpressionException
	{
		String[] splitPath = groupPath.split("\\.");
		
		// Not a path
		if(splitPath.length < 1)
		{
			return false;
		}
		
		NodeList groupList = null;
		
		int sl = splitPath.length;
		int s = 0;
		
		// Walk the path to the list of groups
		while(s < sl)
		{
			System.out.println(splitPath[s]);
			
			// Matching Groups
			groupList = document.getElementsByTagName(splitPath[s]);
			
			s++;
		}
		
		int listLength = groupList.getLength();
		
		// The list of found variables
		NodeList searchVariables = null;
		
		for(int n = 0; n < listLength; n++)
		{
			// A group
			Node group = groupList.item(n);
			
			// System.out.println("> " + group.getNodeName());
			// System.out.println("> " + group.getNodeType());
			
			if(group.getNodeType() == Node.ELEMENT_NODE)
			{
				boolean matchVar = listContainsElementNamedWithValue(group.getChildNodes(), nameElement, nameValue);
				
				if(matchVar)
				{
					// The elements of the group found
					searchVariables = group.getChildNodes();
					
					// System.out.println("Match " + matchVar + " nameElement " + nameElement + " nameValue " + nameValue);
					
					break;
				}
			}
		}
		
		if(searchVariables == null)
		{
			return false;
		}
		
		// False if the Variable did not exist even though the group did, or true if everything completed fine
		return changeVariableValue(searchVariables, variable, newValue);
	}
	
	private boolean changeVariableValue(NodeList searchVariables, String variable, String newValue)
	{
		int svl = searchVariables.getLength();
		
		for(int sv = 0; sv < svl; sv++)
		{
			Node svn = searchVariables.item(sv);
			
			if(svn.getNodeType() == Node.ELEMENT_NODE)
			{
				String nn = svn.getNodeName();
				String tc = svn.getTextContent();
				
				if(nn.equals(variable))
				{
					System.out.println(nn + " : " + tc + " > " + newValue);
					svn.setTextContent(newValue);
					
					// Everything fine
					return true;
				}
				
			}
		}
		
		// Variable not found
		return false;
	}
	
	private boolean listContainsElementNamedWithValue(NodeList nodes, String name, String value)
	{
		for(int v = 0; v < nodes.getLength(); v++)
		{
			if(nodes.item(v).getNodeName().equals(name))
			{
				if(nodes.item(v).getTextContent().equals(value))
					
					return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Debug method which outputs the current document contents to standard I/O.
	 * 
	 * @param document
	 * @throws TransformerException
	 */
	public void dumpXML()
	{
		System.out.println(getConfigXML());
	}
}