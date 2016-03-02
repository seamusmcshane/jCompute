package jCompute.Batch.LogFileProcessor.LogFormatProcessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jCompute.Datastruct.knn.benchmark.TimerObj;

public class TextBatchLogFormat implements LogFormatInf
{
	private static Logger log = LoggerFactory.getLogger(TextBatchLogFormat.class);
	
	private final String logFormat = "TextBatchLogFormat";
	
	private String logFileName;
	private String logType;
	private int samples;
	
	private String xAxisName;
	private String yAxisName;
	private String zAxisName;
	
	private ArrayList<TextBatchLogItem> logItems;
	
	private long processingTime;
	
	public TextBatchLogFormat(String fileName, int maxVal) throws IOException
	{
		logItems = new ArrayList<TextBatchLogItem>();
		
		File file = new File(fileName);
		
		TimerObj to = new TimerObj();
		
		BufferedReader inputFile = new BufferedReader(new FileReader(file));
		
		boolean readingItems = false;
		boolean finished = false;
		
		to.startTimer();
		
		while(!finished)
		{
			if(readingItems)
			{
				// Items
				log.info("finished");
				
				if(inputFile.readLine().equals("[+Items]"))
				{
					readItems(inputFile);
				}
				
				finished = true;
			}
			else
			{
				if(inputFile.readLine().equals("[+Header]"))
				{
					// Header
					readHeader(inputFile);
					
					readingItems = true;
				}
				else
				{
					finished = true;
					log.info("Could not find log file");
				}
			}
		}
		
		to.stopTimer();
		
		inputFile.close();
		
		processingTime = to.getTimeTaken();
	}
	
	/*
	 * *****************************************************************************************************
	 * Format Processing Methods
	 *****************************************************************************************************/
	
	private void readItems(BufferedReader inputFile) throws IOException
	{
		boolean finished = false;
		
		while(!finished)
		{
			String line = inputFile.readLine();
			if(line.equals("[-Items]"))
			{
				finished = true;
			}
			else
			{
				if(line.equals("[+Item]"))
				{
					readItem(inputFile);
				}
			}
		}
	}
	
	private void readItem(BufferedReader inputFile) throws IOException
	{
		TextBatchLogItem item = new TextBatchLogItem();
		
		// Max Coords to set as item pos/values
		int maxCoords = 2;
		
		// Per item Coord Count
		int coord = 0;
		
		for(String line; !(line = inputFile.readLine()).equals("[-Item]");)
		{
			if(line.equals("[+Coordinate]"))
			{
				// Increment count of Coords
				coord++;
				
				int pos[] = new int[2];
				double vals[] = new double[2];
				
				// Read POS 0
				String cline = inputFile.readLine();
				String cpos1 = cline.substring(cline.lastIndexOf('=') + 1, cline.length());
				pos[0] = Integer.parseInt(cpos1);
				
				// Read VAL 0
				cline = inputFile.readLine();
				String cval1 = cline.substring(cline.lastIndexOf('=') + 1, cline.length());
				vals[0] = Double.parseDouble(cval1);
				
				while(!(cline = inputFile.readLine()).equals("[-Coordinate]"))
				{
					log.info("Coordinate contains unexpect data");
				}
				
				cline = inputFile.readLine();
				if(cline.equals("[+Coordinate]"))
				{
					// Increment count of Coords
					coord++;
					
					// Read POS 1
					cline = inputFile.readLine();
					String cpos2 = cline.substring(cline.lastIndexOf('=') + 1, cline.length());
					pos[1] = Integer.parseInt(cpos2);
					
					// Read VAL 0
					cline = inputFile.readLine();
					String cval2 = cline.substring(cline.lastIndexOf('=') + 1, cline.length());
					vals[1] = Double.parseDouble(cval2);
					
					while(!(cline = inputFile.readLine()).equals("[-Coordinate]"))
					{
						log.info("Coordinate contains unexpect data");
					}
				}
				else
				{
					log.info("Error Parsing Coordinates");
				}
				
				// Only set the values for the first two coordinates read (Otherwise 3,4 will overwrite them)
				if(coord <= maxCoords)
				{
					item.setCoordsPos(pos);
					item.setCoordsVals(vals);
				}
				
			}
			else
			{
				int delimiterIndex = line.lastIndexOf('=');
				String field = line.substring(0, delimiterIndex);
				String val = line.substring(delimiterIndex + 1, line.length());
				
				switch(field)
				{
					case "IID":
						item.setItemId(Integer.parseInt(val));
					break;
					case "SID":
						item.setSampleId(Integer.parseInt(val));
					break;
					case "Hash":
						item.setHash(val);
					break;
					case "RunTime":
						item.setRunTime(Integer.parseInt(val));
					break;
					case "EndEvent":
						item.setEndEvent(val);
					break;
					case "StepCount":
						item.setStepCount(Integer.parseInt(val));
					break;
				}
			}
		}
		
		log.debug("Coord : " + coord);
		
		// Reset the coord counted (per item)
		coord = 0;
		logItems.add(item);
	}
	
	private void readHeader(BufferedReader inputFile) throws IOException
	{
		String line = "";
		while(!(line = inputFile.readLine()).equals("[-Header]"))
		{
			
			if(line.equals("[+AxisLabels]"))
			{
				readAxisLabels(inputFile);
			}
			else
			{
				int delimiterIndex = line.lastIndexOf('=');
				String field = line.substring(0, delimiterIndex);
				String val = line.substring(delimiterIndex + 1, line.length());
				
				if(field.equals("Name"))
				{
					this.logFileName = val;
					log.info("LogName :" + logFileName);
				}
				else if(field.equals("LogType"))
				{
					this.logType = val;
					log.info("LogType :" + logType);
					
				}
				else if(field.equals("Samples"))
				{
					this.samples = Integer.parseInt(val);
					log.info("Samples :" + samples);
				}
			}
		}
	}
	
	private void readAxisLabels(BufferedReader inputFile) throws IOException
	{
		int axisCount = 0;
		boolean finished = false;
		while(!finished)
		{
			String id = inputFile.readLine();
			
			if(id.equals("[-AxisLabels]"))
			{
				finished = true;
			}
			else
			{
				String axis = inputFile.readLine();
				String axisName = axis.substring(axis.lastIndexOf('=') + 1, axis.length());
				
				// X / Y Axis for SurfacePlots
				if(axisCount == 0)
				{
					xAxisName = axisName;
				}
				else if(axisCount == 1)
				{
					yAxisName = axisName;
				}
				
				log.info("Axis " + id + " :" + axisName);
				
				axisCount++;
			}
		}
		
		// Choose Plot Source
		zAxisName = "StepCount";
	}
	
	/*
	 * *****************************************************************************************************
	 * Log Format
	 *****************************************************************************************************/
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
	public String getLogFileName()
	{
		return logFileName;
	}
	
	public String getLogType()
	{
		return logType;
	}
	
	public int getSamples()
	{
		return samples;
	}
	
	/*
	 * *****************************************************************************************************
	 * Log Items
	 *****************************************************************************************************/
	public ArrayList<TextBatchLogItem> getLogItems()
	{
		return logItems;
	}
	
	/*
	 * *****************************************************************************************************
	 * Processing Metric
	 *****************************************************************************************************/
	public long getProcessingTime()
	{
		return processingTime;
	}
}
