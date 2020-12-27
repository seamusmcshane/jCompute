package jcompute.results.export.format;

import java.util.List;

import jcompute.results.trace.samples.TraceSample;

public class XMLExporter
{
	
	public static void AddFileExportHeaderXML(StringBuilder fileData, String group, List<String> statList)
	{
		int statCount = statList.size();
		
		// DOCTYPE (DTD)
		fileData.append("<!DOCTYPE " + xmlString(group) + "\n[\n");
		
		// Group contains Steps
		fileData.append("<!ELEMENT " + xmlString(group) + " (Step)>\n");
		
		// Step Contains Stat Types
		fileData.append("<!ELEMENT Step (");
		for(int statIndex = 0; statIndex < statCount; statIndex++)
		{
			fileData.append(xmlString(statList.get(statIndex)));
			if(statIndex < (statCount - 1))
			{
				fileData.append(",");
			}
		}
		fileData.append(")>\n");
		
		// Each Step has an attribute which is a unique id
		fileData.append("<!ATTLIST Step id ID #REQUIRED>\n");
		
		// Each Stat is an ELEMENT
		for(int statIndex = 0; statIndex < statCount; statIndex++)
		{
			fileData.append("<!ELEMENT " + xmlString(statList.get(statIndex)) + " (#PCDATA)>\n");
		}
		
		// End DOCTYPE
		fileData.append("]>\n");
		
		// XML ROOT NODE OPEN
		fileData.append("<" + xmlString(group) + ">\n");
	}
	
	public static void AppendXMLRow(StringBuilder data, TraceSample[][] TraceHistorys, int history, List<String> traceList)
	{
		int statCount = traceList.size();
		
		// Each Row is a Step
		data.append("\t<Step id='" + history + "'>\n");
		
		// Do the same for every history, append , after each sample or a new
		// line after each history
		for(int traceIndex = 0; traceIndex < statCount; traceIndex++)
		{
			data.append("\t\t<" + xmlString(traceList.get(traceIndex)) + ">" + TraceHistorys[traceIndex][history].toString() + "</" + xmlString(traceList.get(
			traceIndex)) + ">\n");
		}
		
		// End Step
		data.append("\t</Step>\n");
	}
	
	public static void AppendCloseSection(StringBuilder data, String section)
	{
		data.append("</" + xmlString(section) + ">\n");
	}
	
	/**
	 * Method checks a string according to XML entity naming rules and returns a corrected string if needed.
	 * 
	 * @param text
	 * @return
	 */
	private static String xmlString(String text)
	{
		StringBuilder validString = new StringBuilder();
		
		// XML cannot have numeric first chars or punctuation for names etc
		if(!XMLChar.isNameStart(text.charAt(0)))
		{
			// Add a safe first char
			validString.append("_");
		}
		
		// Strip invalid chars
		for(char c : text.toCharArray())
		{
			if(XMLChar.isName(c))
			{
				validString.append(c);
			}
		}
		
		// Return a valid string
		return validString.toString();
	}
}
