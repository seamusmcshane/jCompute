package jCompute.Batch.LogFileProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jCompute.Batch.LogFileProcessor.LogFormatProcessor.LogFormatInf;
import jCompute.Batch.LogFileProcessor.LogFormatProcessor.LogFormatValuesContainer;
import jCompute.Batch.LogFileProcessor.LogFormatProcessor.TextBatchLogFormat;
import jCompute.Batch.LogFileProcessor.LogFormatProcessor.TextBatchLogFormatV2;
import jCompute.Batch.LogFileProcessor.LogFormatProcessor.TextBatchLogItem;
import jCompute.Timing.TimerObj;
import jCompute.util.FileUtil;
import jCompute.util.Text;

public class BatchLogProcessor implements BatchLogInf
{
	// SL4J Logger
	private static Logger log = LoggerFactory.getLogger(BatchLogProcessor.class);
	
	private TimerObj to = new TimerObj();
	
	/*
	 * *****************************************************************************************************
	 * Axis Ranges
	 *****************************************************************************************************/
	private double xValMin = Double.MAX_VALUE;
	private double xValMax = Double.MIN_VALUE;
	private double yValMin = Double.MAX_VALUE;
	private double yValMax = Double.MIN_VALUE;
	private double zValMin = Double.MAX_VALUE;
	private double zValMax = Double.MIN_VALUE;
	
	// Data/zValRange
	private double zValRangeMin = Double.MAX_VALUE;
	private double zValRangeMax = Double.MIN_VALUE;
	
	/*
	 * *****************************************************************************************************
	 * Data Values
	 *****************************************************************************************************/
	private LogFormatValuesContainer values;
	
	/*
	 * *****************************************************************************************************
	 * Log Format Returned Data
	 *****************************************************************************************************/
	private String logFormat;
	
	private String xAxisName;
	private String yAxisName;
	private String zAxisName;
	
	private String logFileName;
	private String logType;
	
	private int samples;
	
	private long totalTime;
	
	public BatchLogProcessor(String filePath) throws IOException
	{
		this(filePath, 0, 0, false);
	}
	
	public BatchLogProcessor(String filePath, int rangeMin, int rangeMax) throws IOException
	{
		this(filePath, rangeMin, rangeMax, true);
	}
	
	public BatchLogProcessor(String filePath, int rangeMin, int rangeMax, boolean useRangeLimits) throws IOException
	{
		System.setProperty("log4j.configurationFile", "log/config/log4j2-consoleonly.xml");
		
		LogFormatInf logFormatProcessor = detectAndProcessLogFile(filePath);
		
		processLogItems(logFormatProcessor.getLogItems(), rangeMin, rangeMax, useRangeLimits);
	}
	
	private LogFormatInf detectAndProcessLogFile(String filePath) throws IOException
	{
		to.startTimer();
		
		String fileExtension = FileUtil.getFileNameExtension(filePath);
		log.info("File Extension : " + fileExtension);
		
		LogFormatInf logFormatProcessor = null;
		
		switch(fileExtension)
		{
			case "log":
			{
				logFormatProcessor = new TextBatchLogFormat(filePath);
			}
			break;
			case "v2log":
			{
				logFormatProcessor = new TextBatchLogFormatV2(filePath);
			}
			break;
			case "xml":
			{
				Throwable throwable = new Throwable("XML log format is no longer supported");
				
				throwable.setStackTrace(Thread.currentThread().getStackTrace());
				
				log.error("XML log format is no longer supported " + fileExtension);
				
				throw new IOException(throwable);
			}
			default:
			{
				Throwable throwable = new Throwable("Unsupported Log Format");
				
				throwable.setStackTrace(Thread.currentThread().getStackTrace());
				
				log.error("Unsupported Log Format " + fileExtension);
				
				throw new IOException(throwable);
			}
		}
		
		logFormat = logFormatProcessor.getLogFormat();
		logFileName = logFormatProcessor.getLogFormat();
		
		xAxisName = logFormatProcessor.getXAxisName();
		yAxisName = logFormatProcessor.getYAxisName();
		zAxisName = logFormatProcessor.getZAxisName();
		
		samples = logFormatProcessor.getSamples();
		logFormat = logFormatProcessor.getLogFormat();
		logFormat = logFormatProcessor.getLogFormat();
		
		to.stopTimer();
		
		log.info("Time Processing " + logFormatProcessor.getLogFormat() + " : " + Text.longTimeToDHMSM(to.getTimeTaken()));
		
		totalTime += to.getTimeTaken();
		
		return logFormatProcessor;
	}
	
	/*
	 * *****************************************************************************************************
	 * Axis Ranges
	 *****************************************************************************************************/
	
	@Override
	public double getXValMin()
	{
		return xValMin;
	}
	
	@Override
	public double getXValMax()
	{
		return xValMax;
		
	}
	
	@Override
	public double getXValRange()
	{
		return xValMax - xValMin;
	}
	
	@Override
	public double getYValMin()
	{
		return yValMin;
	}
	
	@Override
	public double getYValMax()
	{
		return yValMax;
	}
	
	@Override
	public double getYValRange()
	{
		return yValMax - yValMin;
	}
	
	@Override
	public double getZValMin()
	{
		return zValMin;
	}
	
	@Override
	public double getZValMax()
	{
		return zValMax;
	}
	
	@Override
	public double getZValRange()
	{
		return zValRangeMax - zValRangeMin;
	}
	
	@Override
	public double getZValRangeMin()
	{
		return zValRangeMin;
	}
	
	@Override
	public double getZValRangeMax()
	{
		return zValRangeMax;
	}
	
	/*
	 * *****************************************************************************************************
	 * Axis Names
	 *****************************************************************************************************/
	
	@Override
	public String[] getAxisNames()
	{
		return new String[]
		{
			xAxisName, yAxisName, zAxisName
		};
	}
	
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
	 * Axis Limits
	 *****************************************************************************************************/
	
	@Override
	public int getXMax()
	{
		return values.getXMax();
	}
	
	@Override
	public int getXMin()
	{
		return values.getXMin();
	}
	
	@Override
	public int getYMax()
	{
		return values.getYMax();
	}
	
	@Override
	public int getYMin()
	{
		return values.getYMin();
	}
	
	@Override
	public double getZmin()
	{
		return values.getZMin();
	}
	
	@Override
	public double getZmax()
	{
		return values.getZMax();
	}
	
	/*
	 * *****************************************************************************************************
	 * Axis Granularity
	 *****************************************************************************************************/
	
	@Override
	public int getXSteps()
	{
		return values.getXSteps();
	}
	
	@Override
	public int getYSteps()
	{
		return values.getYSteps();
	}
	
	@Override
	public int getNumSamples()
	{
		return values.getSamples();
	}
	
	/*
	 * *****************************************************************************************************
	 * Processed Data
	 *****************************************************************************************************/
	
	public double[] getAvgDataFlat()
	{
		return values.getAvgDataFlat();
	}
	
	@Override
	public double[][] getAvgData2d()
	{
		return values.getAvgData2d();
	}
	
	@Override
	public LogFormatValuesContainer getLogFormatValuesContainer()
	{
		return values;
	}
	
	/*
	 * *****************************************************************************************************
	 * Metrics
	 *****************************************************************************************************/
	
	@Override
	public double getMaxRate()
	{
		return values.getMaxRate();
	}
	
	/*
	 * *****************************************************************************************************
	 * Item methods
	 *****************************************************************************************************/
	
	/*
	 * Validate Items
	 */
	
	public void processLogItems(ArrayList<TextBatchLogItem> logItems, int rangeMin, int rangeMax, boolean useRangeLimits) throws IOException
	{
		to.startTimer();
		
		HashMap<Integer, Integer> xUnique = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> yUnique = new HashMap<Integer, Integer>();
		
		log.debug("Num coords : " + logItems.get(0).getCoordsPos().length);
		
		for(TextBatchLogItem item : logItems)
		{
			int x = item.getCoordsPos()[0];
			int y = item.getCoordsPos()[1];
			xUnique.put(x, x);
			yUnique.put(y, y);
			
			if(item.getCoordsVals()[0] > xValMax)
			{
				xValMax = item.getCoordsVals()[0];
			}
			
			if(item.getCoordsVals()[1] > yValMax)
			{
				yValMax = item.getCoordsVals()[1];
			}
			
			if(item.getCoordsVals()[0] < xValMin)
			{
				xValMin = item.getCoordsVals()[0];
			}
			
			if(item.getCoordsVals()[1] < yValMin)
			{
				yValMin = item.getCoordsVals()[1];
			}
			
			if(item.getStepCount() < zValMin)
			{
				zValMin = item.getStepCount();
			}
			
			if(item.getStepCount() > zValMax)
			{
				zValMax = item.getStepCount();
			}
		}
		
		int xDimSize = xUnique.size();
		int yDimSize = yUnique.size();
		
		log.debug("X Dim : " + xDimSize);
		log.debug("Y Dim : " + yDimSize);
		
		log.debug("xValMin " + xValMin);
		log.debug("xValMax " + xValMax);
		log.debug("yValMin " + yValMin);
		log.debug("yValMax " + yValMax);
		
		log.debug("Surface Size : " + xDimSize * yDimSize);
		log.debug("Item Total   : " + logItems.size());
		
		values = new LogFormatValuesContainer(xDimSize, yDimSize, samples);
		
		int[] IIDS = new int[logItems.size() / samples];
		int[] SIDS = new int[samples];
		
		int storeErrors = 0;
		
		for(TextBatchLogItem item : logItems)
		{
			// zAxis = StepCount
			double val = item.getStepCount();
			
			if(false)
			{
				log.info("------------------ ");
				log.info("Item ");
				log.info("IID       :" + item.getItemId());
				log.info("SID       :" + item.getSampleId());
				log.info("Hash      :" + item.getHash());
				log.info("Pos       :" + item.getCoordsPos()[0] + "x" + item.getCoordsPos()[1]);
				log.info("Val       :" + item.getCoordsVals()[0] + "x" + item.getCoordsVals()[0]);
				log.info("RunTime   :" + item.getRunTime());
				log.info("StepCount :" + item.getStepCount());
				log.info("EndEvent  :" + item.getEndEvent());
			}
			int iid = item.getItemId();
			int sid = item.getSampleId() - 1;
			
			int c0 = item.getCoordsPos()[0];
			int c1 = item.getCoordsPos()[1];
			
			boolean oldLog = false;
			
			if(oldLog)
			{
				iid = iid - 1;
				c0 = c0 - 1;
				c1 = c1 - 1;
			}
			
			IIDS[iid]++;
			SIDS[sid]++;
			// Combo Pos starts at 1, array pos at 0 - index offset corrected here
			boolean stored = values.setSampleValue(c0, c1, val);
			
			if(!stored)
			{
				storeErrors++;
			}
		}
		
		boolean itemsSamplesCorrect = true;
		for(int i = 0; i < IIDS.length; i++)
		{
			// log.info("Unique Items " + IIDS[i]);
			if(IIDS[i] == samples)
			{
				log.debug("Item Samples OK : " + (i + 1));
			}
			else
			{
				itemsSamplesCorrect = false;
				
				log.error("Item " + i + " Not correct : " + IIDS[i] + " " + samples);
			}
		}
		
		if(itemsSamplesCorrect)
		{
			log.info("All items have correct number of samples(" + samples + ").");
		}
		else
		{
			log.error("Some items do not have the correct number of samples. ");
		}
		
		boolean itemsSamplesNumbersCorrect = true;
		for(int i = 0; i < SIDS.length; i++)
		{
			if(SIDS[i] == logItems.size() / samples)
			{
				log.debug("Item Sample Numbers OK : " + (i + 1));
			}
			else
			{
				itemsSamplesNumbersCorrect = false;
				
				log.error("Item " + i + " Not correct : " + SIDS[i] + " " + samples);
			}
			
		}
		
		if(itemsSamplesNumbersCorrect)
		{
			log.info("All sample numbers appear correct(" + logItems.size() / samples + ").");
		}
		else
		{
			log.error("Sample numbers do not appear correct.");
		}
		
		log.warn("Store Errors " + storeErrors);
		
		if(useRangeLimits)
		{
			zValRangeMin = rangeMin;
			zValRangeMax = rangeMax;
		}
		else
		{
			zValRangeMin = zValMin;
			zValRangeMax = zValMax;
		}
		
		values.compute(zValRangeMax);
		
		log.debug("xValMin" + xValMin);
		log.debug("xValMax" + xValMax);
		
		log.debug("yValMin" + yValMin);
		log.debug("yValMax" + yValMax);
		
		log.debug("zValMin" + zValMin);
		log.debug("zValMax" + zValMax);
		
		log.debug("xMax" + values.getXMax());
		log.debug("yMax" + values.getYMax());
		log.debug("zMax" + values.getZMax());
		
		to.stopTimer();
		
		log.info("Processed Items : " + Text.longTimeToDHMSM(to.getTimeTaken()));
		
		totalTime += to.getTimeTaken();
	}
	
	@Override
	public long getTimeTaken()
	{
		return totalTime;
	}
	
}
