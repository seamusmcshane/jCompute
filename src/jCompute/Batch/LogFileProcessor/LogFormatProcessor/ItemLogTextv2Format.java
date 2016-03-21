package jCompute.Batch.LogFileProcessor.LogFormatProcessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Semaphore;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemLogTextv2Format implements ItemLogFormatInf
{
	private static Logger log = LoggerFactory.getLogger(ItemLogTextv2Format.class);
	
	public static final int HEADER_LINE_OPTS = 4;
	public static final int MAX_LINE_OPTS = 7;
	
	public static final char OPTION_DELIMITER = ',';
	public static final char SUBOPTION_DELIMITER = ';';
	public static final char FIELD_DELIMITER = '=';
	
	private final String logFormat = "ItemLogTextv2Format";
	
	private String logFileName;
	private String logType;
	private int samples;
	
	private String xAxisName;
	private String yAxisName;
	private String zAxisName;
	
	private ArrayList<ItemLogItem> logItems;
	private Semaphore semaphore = new Semaphore(1, false);
	
	public ItemLogTextv2Format(String filePath) throws IOException
	{
		Path path = Paths.get(filePath);
		
		logItems = new ArrayList<ItemLogItem>();
		
		// Read the header line
		Stream<String> headerLines = Files.lines(path);
		Optional<String> oHeader = headerLines.findFirst();
		String header = oHeader.get();
		readHeaderLine(header);
		headerLines.close();
		
		// Create a pool with threads matching processors
		ForkJoinPool forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
		Stream<String> itemLines = Files.readAllLines(path).parallelStream().skip(1L).parallel();
		
		// Submit our stream as a runnable task
		ForkJoinTask<?> task = forkJoinPool.submit(() -> itemLines.forEach(s ->
		{
			readItemLine(s);
		}));
		
		/*
		 * Wait on our task and wait
		 * As this stream is processing a file, we throw an IOException if the task it self throws any exception.
		 */
		try
		{
			task.get();
		}
		catch(InterruptedException | ExecutionException e)
		{
			throw new IOException(e);
		}
		
		// Free up the threads in the pool
		forkJoinPool.shutdown();
		
		itemLines.close();
		
		// Choose Plot Source
		zAxisName = "StepCount";
	}
	
	/*
	 * *****************************************************************************************************
	 * Format Processing Methods
	 *****************************************************************************************************/
	
	/*
	 * Process the log header
	 */
	private void readHeaderLine(String header)
	{
		String[] options = lineToOptions(header, 0, HEADER_LINE_OPTS, OPTION_DELIMITER);
		
		for(int i = 0; i < options.length; i++)
		{
			String[] kvp = getOptionKVPair(options[i]);
			
			String field = kvp[0];
			String value = kvp[1];
			
			switch(field)
			{
				case "Name":
				{
					logFileName = value;
					log.info("LogName :" + logFileName);
				}
				break;
				case "LogType":
				{
					this.logType = value;
					log.info("LogType :" + logType);
				}
				break;
				case "Samples":
				{
					this.samples = Integer.parseInt(value);
					log.info("Samples :" + samples);
				}
				break;
				case "AxisLabels":
				{
					// num=int;
					int nS = value.indexOf(FIELD_DELIMITER) + 1;
					int nE = value.indexOf(SUBOPTION_DELIMITER);
					String sNum = value.substring(nS, nE);
					int num = Integer.parseInt(sNum);
					String[] axisInfo = lineToOptions(value, nE + 1, num * 2, SUBOPTION_DELIMITER);
					
					int[] axisId = new int[num];
					String[] axisName = new String[num];
					
					// Pos + Vals
					for(int c = 0; c < axisInfo.length; c += 2)
					{
						String[] axIdkvp = getOptionKVPair(axisInfo[c]);
						String[] axLakvp = getOptionKVPair(axisInfo[c + 1]);
						
						axisId[c / 2] = Integer.parseInt(axIdkvp[1]);
						axisName[(c + 1) / 2] = axLakvp[1];
					}
					
					// Hardcoded order
					xAxisName = axisName[0];
					
					log.info("xAxis " + axisId[0] + " :" + axisName[0]);
					
					yAxisName = axisName[1];
					
					log.info("yAxis " + axisId[1] + " :" + axisName[1]);
				}
				break;
			}
		}
	}
	
	/*
	 * Reads an item line and adds it to the log items list.
	 */
	private void readItemLine(String itemLine)
	{
		ItemLogItem item = new ItemLogItem();
		
		String[] options = lineToOptions(itemLine, 0, MAX_LINE_OPTS, OPTION_DELIMITER);
		
		for(int i = 0; i < options.length; i++)
		{
			String[] kvp = getOptionKVPair(options[i]);
			
			String field = kvp[0];
			String value = kvp[1];
			
			switch(field)
			{
				case "IID":
					item.setItemId(Integer.parseInt(value));
				break;
				case "SID":
					item.setSampleId(Integer.parseInt(value));
				break;
				case "Coordinates":
				{
					// num=int;
					int nS = value.indexOf(FIELD_DELIMITER) + 1;
					int nE = value.indexOf(SUBOPTION_DELIMITER);
					String sNum = value.substring(nS, nE);
					int num = Integer.parseInt(sNum);
					
					String[] coordOptions = lineToOptions(value, nE + 1, num * 2, SUBOPTION_DELIMITER);
					
					int[] pos = new int[num];
					double[] vals = new double[num];
					
					// Pos + Vals
					for(int c = 0; c < coordOptions.length; c += 2)
					{
						String[] cPkvp = getOptionKVPair(coordOptions[c]);
						String[] cVkvp = getOptionKVPair(coordOptions[c + 1]);
						pos[c / 2] = Integer.parseInt(cPkvp[1]);
						vals[(c + 1) / 2] = Double.valueOf(cVkvp[1]);
					}
					
					item.setCoordsPos(pos);
					item.setCoordsVals(vals);
				}
				break;
				case "Hash":
					item.setCacheIndex(value);
				// Fall through
				case "CacheIndex":
					item.setCacheIndex(value);
				break;
				case "RunTime":
					item.setRunTime(Integer.parseInt(value));
				break;
				case "EndEvent":
					item.setEndEvent(value);
				break;
				case "StepCount":
					item.setStepCount(Integer.parseInt(value));
				break;
			}
			
		}
		
		// Protect the array list.
		// synchronized(logItems)
		{
			semaphore.acquireUninterruptibly();
			logItems.add(item);
			semaphore.release();
		}
	}
	
	/*
	 * Split a line into an array of options
	 */
	private String[] lineToOptions(String line, int start, int maxOptions, char delimiter)
	{
		String[] options = new String[maxOptions];
		
		int cS = start;
		int cE = line.indexOf(delimiter, cS);
		for(int o = 0; o < maxOptions; o++)
		{
			options[o] = line.substring(cS, cE);
			
			cS = cE + 1;
			cE = line.indexOf(delimiter, cS);
			
			if(cE == -1)
			{
				cE = line.length();
			}
		}
		
		return options;
	}
	
	/*
	 * Split an option (Field=Value) into a key value pair;
	 */
	private String[] getOptionKVPair(String option)
	{
		int fS = 0;
		int fE = option.indexOf(FIELD_DELIMITER);
		
		String[] kvp = new String[2];
		
		kvp[0] = option.substring(fS, fE);
		kvp[1] = option.substring(fE + 1, option.length());
		
		return kvp;
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
