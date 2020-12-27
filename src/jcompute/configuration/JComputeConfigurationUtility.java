package jcompute.configuration;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import jcompute.configuration.support.StringSchemaOutputResolver;

public class JComputeConfigurationUtility
{
	public static JComputeConfiguration XMLtoConfig(String configFile, Class<?> c)
	{
		JAXBContext jaxbContext;
		Unmarshaller jaxbUnmarshaller;
		
		String schemaText = JComputeConfigurationUtility.GenerateSchemaFromClass(c);
		
		System.out.println("Schema");
		System.out.println(schemaText);
		
		try
		{
			SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
			
			StringReader sr = new StringReader(schemaText);
			
			Schema schema = sf.newSchema(new StreamSource(sr));
			
			jaxbContext = JAXBContext.newInstance(c);
			
			jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			
			jaxbUnmarshaller.setSchema(schema);
		}
		catch(JAXBException e)
		{
			e.printStackTrace();
			
			return null;
		}
		catch(SAXException e)
		{
			e.printStackTrace();
			
			return null;
		}
		
		JComputeConfiguration jComputeConfiguration;
		
		try
		{
			File file = new File(configFile);
			
			jComputeConfiguration = (JComputeConfiguration) jaxbUnmarshaller.unmarshal(file);
		}
		catch(JAXBException e)
		{
			e.printStackTrace();
			
			return null;
		}
		
		return jComputeConfiguration;
	}
	
	public static JComputeConfiguration XMLTexttoConfig(String text, Class<?> c)
	{
		StringReader reader;
		JAXBContext jaxbContext;
		Unmarshaller jaxbUnmarshaller;
		
		try
		{
			reader = new StringReader(text);
			
			jaxbContext = JAXBContext.newInstance(c);
			
			jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		}
		catch(NullPointerException e)
		{
			e.printStackTrace();
			
			return null;
		}
		catch(JAXBException e)
		{
			e.printStackTrace();
			
			return null;
		}
		
		JComputeConfiguration jComputeConfiguration;
		
		try
		{
			jComputeConfiguration = (JComputeConfiguration) jaxbUnmarshaller.unmarshal(reader);
		}
		catch(JAXBException e)
		{
			e.printStackTrace();
			
			return null;
		}
		
		return jComputeConfiguration;
	}
	
	public static boolean ConfigToXML(JComputeConfiguration config, String outputFilepath)
	{
		
		JAXBContext jaxbContext;
		Marshaller jaxbMarshaller;
		
		try
		{
			jaxbContext = JAXBContext.newInstance(config.getClass());
		}
		catch(JAXBException e1)
		{
			e1.printStackTrace();
			
			return false;
		}
		
		try
		{
			jaxbMarshaller = jaxbContext.createMarshaller();
		}
		catch(JAXBException e)
		{
			e.printStackTrace();
			
			return false;
		}
		
		try
		{
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		}
		catch(PropertyException e)
		{
			e.printStackTrace();
			
			return false;
		}
		
		try
		{
			jaxbMarshaller.marshal(config, System.out);
		}
		catch(JAXBException e)
		{
			e.printStackTrace();
			
			return false;
		}
		
		return true;
	}
	
	public static String ConfigToString(JComputeConfiguration config)
	{
		JAXBContext jaxbContext;
		Marshaller jaxbMarshaller;
		
		try
		{
			jaxbContext = JAXBContext.newInstance(config.getClass());
		}
		catch(JAXBException e1)
		{
			e1.printStackTrace();
			
			return null;
		}
		
		try
		{
			jaxbMarshaller = jaxbContext.createMarshaller();
		}
		catch(JAXBException e)
		{
			e.printStackTrace();
			
			return null;
		}
		
		try
		{
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		}
		catch(PropertyException e)
		{
			e.printStackTrace();
			
			return null;
		}
		
		StringWriter writer = new StringWriter();
		
		try
		{
			jaxbMarshaller.marshal(config, writer);
		}
		catch(JAXBException e)
		{
			e.printStackTrace();
			
			return null;
		}
		
		return writer.toString();
	}
	
	public static String GenerateSchemaFromClass(Class<?> c)
	{
		JAXBContext jaxbContext;
		
		try
		{
			jaxbContext = JAXBContext.newInstance(c);
		}
		catch(JAXBException e)
		{
			e.printStackTrace();
			
			return null;
		}
		
		SchemaOutputResolver sor = new StringSchemaOutputResolver();
		
		// Generate the schema
		try
		{
			jaxbContext.generateSchema(sor);
		}
		catch(IOException e)
		{
			e.printStackTrace();
			
			return null;
		}
		
		// Return the schema in a string
		return ((StringSchemaOutputResolver) sor).getSchema();
	}
}
