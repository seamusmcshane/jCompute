package jcompute.batch.logfileprocessor.logformatprocessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jcompute.timing.TimerObj;

public class ItemLogTextFormat implements ItemLogFormatInf
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(ItemLogTextFormat.class);
	
	private final String logFormat = "ItemLogTextFormat";
	
	private String logFileName;
	private String logType;
	private int samples;
	
	private String xAxisName;
	private String yAxisName;
	private String zAxisName;
	
	private ArrayList<ItemLogItem> logItems;
	
	private long processingTime;
	
	public ItemLogTextFormat(String fileName) throws IOException
	{
		logItems = new ArrayList<ItemLogItem>();
		
		File file = new File(fileName);
		
		TimerObj to = new TimerObj();
		
		BufferedReader inputFile = new BufferedReader(new FileReader(file));
		
		boolean readingItems = false;
		boolean finished = false;
		
		to.startTimer();
		
		log.info("Reading Header");
		
		while(!finished)
		{
			if(readingItems)
			{
				
				String line = inputFile.readLine();
				
				if(line == null)
				{
					throw new IOException();
				}
				
				if(line.equals("[+Items]"))
				{
					readItems(inputFile);
				}
				
				// Items
				log.info("Finished Reading Items");
				
				finished = true;
			}
			else
			{
				String line = inputFile.readLine();
				
				if(line == null)
				{
					throw new IOException();
				}
				
				if(line.equals("[+Header]"))
				{
					// Header
					readHeader(inputFile);
					
					readingItems = true;
					
					log.info("Reading Items");
				}
				else
				{
					finished = true;
					
					String message = "File is not a batch item log file.";
					
					log.error(message);
					
					Throwable throwable = new Throwable(message);
					
					throwable.setStackTrace(Thread.currentThread().getStackTrace());
					
					throw new IOException(throwable);
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
		boolean finishedItems = false;
		
		while(!finishedItems)
		{
			String line = inputFile.readLine();
			
			if(line == null)
			{
				throw new IOException();
			}
			
			if(line.equals("[-Items]"))
			{
				finishedItems = true;
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
		ItemLogItem item = new ItemLogItem();
		
		// Max Coords to set as item pos/values
		int maxCoords = 2;
		
		// Per item Coord Count
		int coord = 0;
		
		boolean finishedItem = false;
		
		while(!finishedItem)
		{
			String line = inputFile.readLine();
			
			if(line == null)
			{
				throw new IOException();
			}
			
			if(line.equals("[-Item]"))
			{
				finishedItem = true;
			}
			else if(line.equals("[+Coordinate]"))
			{
				// Increment count of Coords
				coord++;
				
				int pos[] = new int[2];
				double vals[] = new double[2];
				
				// Read POS 0
				line = inputFile.readLine();
				
				if(line == null)
				{
					throw new IOException();
				}
				
				String cpos1 = line.substring(line.lastIndexOf('=') + 1, line.length());
				pos[0] = Integer.parseInt(cpos1);
				
				// Read VAL 0
				line = inputFile.readLine();
				
				if(line == null)
				{
					throw new IOException();
				}
				
				String cval1 = line.substring(line.lastIndexOf('=') + 1, line.length());
				vals[0] = Double.parseDouble(cval1);
				
				int lc = 0;
				do
				{
					line = inputFile.readLine();
					
					if(lc > 0)
					{
						log.info("Coordinate contains unexpect data");
					}
					
					if(line == null)
					{
						throw new IOException();
					}
					lc++;
				}
				while(!(line.equals("[-Coordinate]")));
				
				line = inputFile.readLine();
				
				if(line == null)
				{
					throw new IOException();
				}
				
				if(line.equals("[+Coordinate]"))
				{
					// Increment count of Coords
					coord++;
					
					// Read POS 1
					line = inputFile.readLine();
					
					if(line == null)
					{
						throw new IOException();
					}
					
					String cpos2 = line.substring(line.lastIndexOf('=') + 1, line.length());
					pos[1] = Integer.parseInt(cpos2);
					
					// Read VAL 0
					line = inputFile.readLine();
					
					if(line == null)
					{
						throw new IOException();
					}
					
					String cval2 = line.substring(line.lastIndexOf('=') + 1, line.length());
					vals[1] = Double.parseDouble(cval2);
					
					lc = 0;
					do
					{
						line = inputFile.readLine();
						
						if(lc > 0)
						{
							log.info("Coordinate contains unexpect data");
						}
						
						if(line == null)
						{
							throw new IOException();
						}
						lc++;
					}
					while(!(line.equals("[-Coordinate]")));
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
						item.setCacheIndex(val);
						// Fall through
					case "CacheIndex":
						item.setCacheIndex(val);
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
		
		// Reset the coord counted (per item)
		coord = 0;
		logItems.add(item);
	}
	
	private void readHeader(BufferedReader inputFile) throws IOException
	{
		boolean finishedHeader = false;
		while(!finishedHeader)
		{
			String line = inputFile.readLine();
			
			if(line == null)
			{
				throw new IOException();
			}
			
			if(line.equals("[-Header]"))
			{
				finishedHeader = true;
			}
			else if(line.equals("[+AxisLabels]"))
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
					logFileName = val;
					log.info("LogName :" + logFileName);
				}
				else if(field.equals("LogType"))
				{
					logType = val;
					log.info("LogType :" + logType);
					
				}
				else if(field.equals("Samples"))
				{
					samples = Integer.parseInt(val);
					log.info("Samples :" + samples);
				}
			}
		}
	}
	
	private void readAxisLabels(BufferedReader inputFile) throws IOException
	{
		int axisCount = 0;
		boolean finishedAxis = false;
		
		do
		{
			// Axis Id
			String line = inputFile.readLine();
			
			if(line == null)
			{
				throw new IOException();
			}
			
			if(line.equals("[-AxisLabels]"))
			{
				finishedAxis = true;
			}
			else
			{
				
				log.info("Axis id : " + line);
				// Id not used - not split
				
				// Axis Name
				line = inputFile.readLine();
				
				if(line == null)
				{
					throw new IOException();
				}
				
				line = line.substring(line.lastIndexOf('=') + 1, line.length());
				
				// X / Y Axis for SurfacePlots
				if(axisCount == 0)
				{
					xAxisName = line;
				}
				else if(axisCount == 1)
				{
					yAxisName = line;
				}
				
				log.info("Axis Name : " + line);
				
				axisCount++;
			}
		}
		while(!finishedAxis);
		
		// Choose Plot Source
		zAxisName = "StepCount";
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
	
	/*
	 * *****************************************************************************************************
	 * Processing Metric
	 *****************************************************************************************************/
	public long getProcessingTime()
	{
		return processingTime;
	}
}
