package jcompute.testing;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XML13Test
{
	public static void main(String args[]) throws ParserConfigurationException, SAXException, IOException, TransformerException
	{
		File file = new File("batch/DemoBatch.batch");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(file);
		
		Node rootNode = document.getFirstChild();
		System.out.println(rootNode.getNodeName());
		
		NodeList nodeList = document.getDocumentElement().getChildNodes();
		
		for (int i = 0; i < nodeList.getLength(); i++)
		{
			System.out.print(nodeList.item(i).getNodeName() + " : ");
			System.out.println(nodeList.item(i).getTextContent());
		}
		
		
	}
	
	private static void dumpXML(Document document) throws TransformerException
	{
		TransformerFactory tff = TransformerFactory.newInstance();
		Transformer tf = tff.newTransformer();
		StringWriter sw = new StringWriter();
		tf.transform(new DOMSource(document), new StreamResult(sw));
		
		System.out.println(sw);
	}
}
