package jCompute.Batch.LogFileProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jCompute.Batch.LogFileProcessor.LogFormatProcessor.ItemLogFormatInf;
import jCompute.Batch.LogFileProcessor.LogFormatProcessor.ItemLogFormatValuesContainer;
import jCompute.Batch.LogFileProcessor.LogFormatProcessor.ItemLogItem;
import jCompute.Batch.LogFileProcessor.LogFormatProcessor.ItemLogTextFormat;
import jCompute.Batch.LogFileProcessor.LogFormatProcessor.ItemLogTextv2Format;
import jCompute.Batch.LogFileProcessor.LogFormatProcessor.ItemLogXMLFormat;
import jCompute.Batch.LogFileProcessor.LogFormatProcessor.Metrics.Surface.SurfaceMetricInf.Type;
import jCompute.Timing.TimerObj;
import jCompute.util.FileUtil;
import jCompute.util.TimeString;
import jCompute.util.TimeString.TimeStringFormat;

public final class ItemLogProcessor
{
	// Log4j2 Logger
	private static Logger log = LogManager.getLogger(ItemLogProcessor.class);
	
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
	private ItemLogFormatValuesContainer values;
	
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
	
	public ItemLogProcessor(String filePath) throws IOException
	{
		this(filePath, 0, 0, false);
	}
	
	public ItemLogProcessor(String filePath, int rangeMin, int rangeMax) throws IOException
	{
		this(filePath, rangeMin, rangeMax, true);
	}
	
	public ItemLogProcessor(String filePath, int rangeMin, int rangeMax, boolean useRangeLimits) throws IOException
	{
		ItemLogFormatInf logFormatProcessor = detectAndProcessLogFile(filePath);
		
		processLogItems(logFormatProcessor.getLogItems(), rangeMin, rangeMax, useRangeLimits);
	}
	
	private ItemLogFormatInf detectAndProcessLogFile(String filePath) throws IOException
	{
		to.startTimer();
		
		String fileExtension = FileUtil.getFileNameExtension(filePath);
		log.info("File Extension : " + fileExtension);
		
		ItemLogFormatInf logFormatProcessor = null;
		
		switch(fileExtension)
		{
			case "log":
			{
				logFormatProcessor = new ItemLogTextFormat(filePath);
			}
			break;
			case "v2log":
			{
				logFormatProcessor = new ItemLogTextv2Format(filePath);
			}
			break;
			case "xml":
			{
				logFormatProcessor = new ItemLogXMLFormat(filePath);
			}
			break;
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

		log.info("Time Processing " + logFormatProcessor.getLogFormat() + " : " + TimeString.timeInMillisAsFormattedString(to.getTimeTaken(),
		TimeStringFormat.SM));

		totalTime += to.getTimeTaken();
		
		return logFormatProcessor;
	}
	
	/*
	 * *****************************************************************************************************
	 * LogFile Info
	 *****************************************************************************************************/
	
	public String getLogFileName()
	{
		return logFileName;
	}
	
	public String getLogType()
	{
		return logType;
	}
	
	public String getLogFormat()
	{
		return logFormat;
	}
	
	/*
	 * *****************************************************************************************************
	 * Axis Ranges
	 *****************************************************************************************************/
	
	public double getXValMin()
	{
		return xValMin;
	}
	
	public double getXValMax()
	{
		return xValMax;
		
	}
	
	public double getXValRange()
	{
		return xValMax - xValMin;
	}
	
	public double getYValMin()
	{
		return yValMin;
	}
	
	public double getYValMax()
	{
		return yValMax;
	}
	
	public double getYValRange()
	{
		return yValMax - yValMin;
	}
	
	public double getZValMin()
	{
		return zValMin;
	}
	
	public double getZValMax()
	{
		return zValMax;
	}
	
	public double getZValRange()
	{
		return zValRangeMax - zValRangeMin;
	}
	
	public double getZValRangeMin()
	{
		return zValRangeMin;
	}
	
	public double getZValRangeMax()
	{
		return zValRangeMax;
	}
	
	/*
	 * *****************************************************************************************************
	 * Axis Names
	 *****************************************************************************************************/
	
	public String[] getAxisNames()
	{
		return new String[]
		{
			xAxisName, yAxisName, zAxisName
		};
	}
	
	public String getXAxisName()
	{
		return xAxisName;
	}
	
	public String getYAxisName()
	{
		return yAxisName;
	}
	
	public String getZAxisName()
	{
		return zAxisName;
	}
	
	/*
	 * *****************************************************************************************************
	 * Axis Limits
	 *****************************************************************************************************/
	
	public int getXMax()
	{
		return values.getXMax();
	}
	
	public int getXMin()
	{
		return values.getXMin();
	}
	
	public int getYMax()
	{
		return values.getYMax();
	}
	
	public int getYMin()
	{
		return values.getYMin();
	}
	
	public double getZmin()
	{
		return values.getZMin();
	}
	
	public double getZmax()
	{
		return values.getZMax();
	}
	
	/*
	 * *****************************************************************************************************
	 * Axis Granularity
	 *****************************************************************************************************/
	
	public int getXSteps()
	{
		return values.getXSteps();
	}
	
	public int getYSteps()
	{
		return values.getYSteps();
	}
	
	public int getNumSamples()
	{
		return values.getSamples();
	}
	
	/*
	 * *****************************************************************************************************
	 * Processed Data Metrics
	 *****************************************************************************************************/
	
	public double[][] getDataMetric2dArray(Type metricSource)
	{
		int xSteps = values.getXSteps();
		int ySteps = values.getYSteps();
		
		double[] data1d = getDataMetricArray(metricSource);
		
		// 1d to 2d
		double[][] data2d = new double[xSteps][xSteps];
		for(int y = 0; y < ySteps; y++)
		{
			for(int x = 0; x < xSteps; x++)
			{
				data2d[x][y] = data1d[(x * ySteps) + y];
			}
		}
		
		return data2d;
	}
	
	public double[] getDataMetricArray(Type metricSource)
	{
		return values.getMetricArray(metricSource);
	}
	
	public double getDataMetricMinVal(Type metricSource)
	{
		return values.getDataMetricMin(metricSource);
	}
	
	public double getDataMetricMaxVal(Type metricSource)
	{
		return values.getDataMetricMax(metricSource);
	}
	
	public double getComputedMetric(ComputedMetric computedMetric)
	{
		switch(computedMetric)
		{
			case MAX_RATE:
			{
				return values.getMaxRate();
			}
			default:
				return Double.NaN;
		}
	}
	
	/*
	 * *****************************************************************************************************
	 * Item methods
	 *****************************************************************************************************/
	
	/*
	 * Validate Items
	 */
	
	public void processLogItems(ArrayList<ItemLogItem> logItems, int rangeMin, int rangeMax, boolean useRangeLimits) throws IOException
	{
		to.startTimer();
		
		HashMap<Integer, Integer> xUnique = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> yUnique = new HashMap<Integer, Integer>();
		
		log.debug("Num coords : " + logItems.get(0).getCoordsPos().length);
		
		for(ItemLogItem item : logItems)
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
		
		log.debug("Surface Size : " + (xDimSize * yDimSize));
		log.debug("Item Total   : " + logItems.size());
		
		values = new ItemLogFormatValuesContainer(xDimSize, yDimSize, samples);
		
		int[] IIDS = new int[logItems.size() / samples];
		int[] SIDS = new int[samples];
		
		int storeErrors = 0;
		
		for(ItemLogItem item : logItems)
		{
			// zAxis = StepCount
			double val = item.getStepCount();
			
			// log.info("------------------ ");
			// log.info("Item ");
			// log.info("IID :" + item.getItemId());
			// log.info("SID :" + item.getSampleId());
			// log.info("Hash :" + item.getHash());
			// log.info("Pos :" + item.getCoordsPos()[0] + "x" + item.getCoordsPos()[1]);
			// log.info("Val :" + item.getCoordsVals()[0] + "x" + item.getCoordsVals()[0]);
			// log.info("RunTime :" + item.getRunTime());
			// log.info("StepCount :" + item.getStepCount());
			// log.info("EndEvent :" + item.getEndEvent());
			
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
			if(SIDS[i] == (logItems.size() / samples))
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
			log.info("All sample numbers appear correct(" + (logItems.size() / samples) + ").");
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

		log.info("Processed Items : " + TimeString.timeInMillisAsFormattedString(to.getTimeTaken(), TimeStringFormat.SM));

		totalTime += to.getTimeTaken();
	}
	
	public long getTimeTaken()
	{
		return totalTime;
	}
	
	public enum ComputedMetric
	{
		MAX_RATE
	}
}
