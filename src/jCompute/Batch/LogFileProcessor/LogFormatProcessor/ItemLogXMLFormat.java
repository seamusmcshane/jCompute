package jCompute.Batch.LogFileProcessor.LogFormatProcessor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jCompute.util.Text;

public class ItemLogXMLFormat implements ItemLogFormatInf
{
	private static Logger log = LoggerFactory.getLogger(ItemLogXMLFormat.class);
	
	private final String logFormat = "ItemLogXMLFormat";
	
	private String logFileName;
	private String logType;
	private int samples;
	
	private String xAxisName;
	private String yAxisName;
	private String zAxisName;
	
	private ArrayList<ItemLogItem> logItems;
	
	public ItemLogXMLFormat(String fileName) throws IOException
	{
		log.info("Processing XML Log");
		
		XMLConfiguration logFile = new XMLConfiguration();
		logFile.setSchemaValidation(true);
		
		// As a stream so we can control when its closed.
		InputStream targetStream = new FileInputStream(new File(fileName));
		
		try
		{
			logFile.load(targetStream);
		}
		catch(ConfigurationException e)
		{
			String message = "failed to load log file, check the file is a log file and that it complies with the schema";
			
			Throwable throwable = new Throwable(message, null);
			
			throwable.setStackTrace(e.getStackTrace());
			
			throw new IOException(throwable);
		}
		
		readHeader(logFile);
		readItems(logFile);
		
		logFile.clear();
		targetStream.close();
		
		// Choose Plot Source
		zAxisName = "StepCount";
	}
	
	/*
	 * *****************************************************************************************************
	 * Format Processing Methods
	 *****************************************************************************************************/
	
	private void readItems(XMLConfiguration logFile)
	{
		// Log Items List
		logItems = new ArrayList<ItemLogItem>();
		
		log.info("Reading Items Values");
		
		String itemsNodePath = "Items";
		List<ConfigurationNode> itemNodes = getConfigurationNodeList(logFile, itemsNodePath, 0);
		
		int itemTotal = itemNodes.size();
		log.info("Items : " + itemTotal);
		
		// XML Log Format assumes Square Surfaces.
		int matrixDim = (int) Math.sqrt((itemTotal) / samples) + 1;
		
		log.info("XML log format processing previous assumed surface is square - this log would be calulated as : " + matrixDim + "x" + matrixDim);
		
		for(ConfigurationNode itemNode : itemNodes)
		{
			logItems.add(readItem(itemNode));
		}
		
		log.info("logItemsSize " + logItems.size());
	}
	
	private ItemLogItem readItem(ConfigurationNode node)
	{
		ItemLogItem item = new ItemLogItem();
		
		List<ConfigurationNode> fieldList = node.getChildren();
		
		for(ConfigurationNode field : fieldList)
		{
			String fieldName = field.getName();
			String fieldValue = (String) field.getValue();
			
			switch(fieldName)
			{
				case "IID":
				{
					// In XML logs IID start from 1, -1 adjusts the offset to maintain compatibility with BatchLogProcessor which expect 0
					item.setItemId(Integer.parseInt(fieldValue) - 1);
				}
				break;
				case "SID":
				{
					item.setSampleId(Integer.parseInt(fieldValue));
				}
				break;
				case "Hash":
				{
					item.setCacheIndex(fieldValue);
				}
				break;
				case "RunTime":
				{
					// RunTime is Stored as a string in XML format
					String runTime = fieldValue;
					
					item.setRunTime((int) Text.stringTimeDHMStoLongMilli(runTime));
				}
				break;
				case "EndEvent":
				{
					item.setEndEvent(fieldValue);
				}
				break;
				case "StepCount":
				{
					item.setStepCount(Integer.parseInt(fieldValue));
				}
				break;
				case "Coordinates":
				{
					List<ConfigurationNode> coordList = field.getChildren();
					
					// Max Supported Coords to set as item pos/values
					int maxCoords = 2;
					
					// Per item Coord Count
					int coord = 0;
					
					int pos[] = new int[maxCoords];
					double vals[] = new double[maxCoords];
					
					// Coordinates are a list of Coordinate(s)
					for(ConfigurationNode coordinate : coordList)
					{
						// Each Coordinate contains a pair of (POS/VAL) fields
						List<ConfigurationNode> pairPosVal = coordinate.getChildren();
						for(ConfigurationNode posVal : pairPosVal)
						{
							String name = posVal.getName();
							String val = (String) posVal.getValue();
							
							switch(name)
							{
								case "Pos":
								{
									// In XML logs Pos start from 1, -1 adjusts the offset to maintain compatibility with BatchLogProcessor which expect 0
									pos[coord] = (Integer.parseInt(val)) - 1;
								}
								break;
								case "Value":
								{
									vals[coord] = Double.parseDouble(val);
								}
								break;
								default:
								{
									log.error("Unhandeld Field " + fieldName + " " + coordinate.getName() + " " + name);
								}
								break;
							}
							
							// log.info("Coord " + name + " value " + val);
						}
						
						coord++;
						
						if(coord == maxCoords)
						{
							break;
						}
					}
					
					// log.info("p = " + pos[0] + "x" + pos[1]);
					// log.info("v = " + vals[0] + "x" + vals[1]);
					
					item.setCoordsPos(pos);
					item.setCoordsVals(vals);
				}
				break;
				default:
					log.error("Unhandled Item Field " + fieldName + " value " + fieldValue);
				break;
			}
		}
		
		return item;
	}
	
	private List<ConfigurationNode> getConfigurationNodeList(XMLConfiguration logFile, String path, int listIndex)
	{
		// A Sub Tree
		List<HierarchicalConfiguration> subTreeNodes = logFile.configurationsAt(path);
		
		// A list of nodes from node at listIndex
		ConfigurationNode node = subTreeNodes != null ? subTreeNodes.get(listIndex).getRootNode() : null;
		
		// Node List of All XML Nodes
		List<ConfigurationNode> itemNodes = node != null ? node.getChildren() : null;
		
		return itemNodes;
	}
	
	private void readHeader(XMLConfiguration logFile)
	{
		// Axis Names are from Axis 0 and Axis 1
		xAxisName = logFile.getString("Header." + "AxisLabels.AxisLabel(" + 0 + ").Name", "X");
		yAxisName = logFile.getString("Header." + "AxisLabels.AxisLabel(" + 1 + ").Name", "Y");
		
		log.info("xAxisName : " + xAxisName);
		log.info("yAxisName : " + yAxisName);
		
		// Samples
		samples = logFile.getInt("Header.SamplesPerItem");
		
		log.info("Samples : " + samples);
		
		logType = logFile.getString("Header." + "LogType", "unset");
		
		log.info("LogType : " + logType);
	}
	
	/*
	 * *****************************************************************************************************
	 * Log Format
	 *****************************************************************************************************/
	@Override
	public String getLogFormat()
	{
		return logFormat;
	}
	
	/*
	 * *****************************************************************************************************
	 * Axis Names
	 *****************************************************************************************************/
	@Override
	public String getXAxisName()
	{
		return xAxisName;
	}
	
	@Override
	public String getYAxisName()
	{
		return yAxisName;
	}
	
	@Override
	public String getZAxisName()
	{
		return zAxisName;
	}
	
	/*
	 * *****************************************************************************************************
	 * Log Info
	 *****************************************************************************************************/
	@Override
	public String getLogFileName()
	{
		return logFileName;
	}
	
	@Override
	public String getLogType()
	{
		return logType;
	}
	
	@Override
	public int getSamples()
	{
		return samples;
	}
	
	/*
	 * *****************************************************************************************************
	 * Log Items
	 *****************************************************************************************************/
	@Override
	public ArrayList<ItemLogItem> getLogItems()
	{
		return logItems;
	}
}
