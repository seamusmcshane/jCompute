package jcompute.testing;

import java.io.File;
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

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import jcompute.configuration.XMLModifier;
import jcompute.util.text.JCText;

public class XML21Test
{
	public static void main(String args[]) throws ParserConfigurationException, SAXException, IOException,
	TransformerException, XPathExpressionException
	{
		String configText = JCText.textFileToString("batch/TEST/SAPP_v1_Test.scenario.xml");
		
		XMLModifier xmlmod = new XMLModifier();
		
		boolean configLoadedOK = xmlmod.loadConfig(configText);
		
		boolean completed = xmlmod.changeGroupVariableValue("Agents.SimpleAgent", "Name", "Predator", "InitialNumbers",
		"50");
		System.out.println("Completed : " + completed);
		
		completed = xmlmod.changeGroupVariableValue("InvalidPath1", "Name", "Predator", "InitialNumbers", "50");
		System.out.println("Completed : " + completed);
		
		completed = xmlmod.changeGroupVariableValue("Invalid.Path2", "Name", "Predator", "InitialNumbers", "50");
		System.out.println("Completed : " + completed);
		
		completed = xmlmod.changeGroupVariableValue("Agents.SimpleAgent", "Name", "ANameNotInTheFile", "InitialNumbers",
		"50");
		System.out.println("Completed : " + completed);
		
		completed = xmlmod.changeSingleVariableValue("World", "Size", "512");
		System.out.println("Completed : " + completed);
		
		completed = xmlmod.changeSingleVariableValue("Plants", "InitialNumbers", "800");
		System.out.println("Completed : " + completed);
		
		completed = xmlmod.changeSingleVariableValue("Plants", "ANameNotInTheFile", "800");
		System.out.println("Completed : " + completed);
		
		completed = xmlmod.changeSingleVariableValue("APathNotInFile", "InitialNumbers", "800");
		System.out.println("Completed : " + completed);
		
		xmlmod.dumpXML();
	}
	
}
