package jcompute.configuration.support;

import java.io.IOException;
import java.io.StringWriter;

import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;

public class StringSchemaOutputResolver extends SchemaOutputResolver
{
	private StringWriter sw = new StringWriter();
	
	@Override
	public Result createOutput(String namespaceURI, String suggestedFileName) throws IOException
	{
		StreamResult sr = new StreamResult(sw);
		
		sr.setSystemId(suggestedFileName);
		
		return sr;
	}
	
	public String getSchema()
	{
		return sw.toString();
	}
}