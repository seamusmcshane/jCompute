package jCompute.Batch.LogFileProcessor;

import java.io.IOException;

import org.jzy3d.plot3d.builder.Mapper;
import org.jzy3d.plot3d.primitives.axes.layout.renderers.ITickRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jCompute.Batch.LogFileProcessor.LogFormatProcessor.TextBatchLogProcessor;
import jCompute.Batch.LogFileProcessor.LogFormatProcessor.TextBatchLogProcessorV2;
import jCompute.Batch.LogFileProcessor.LogFormatProcessor.XMLBatchLogProcessor;
import jCompute.util.FileUtil;

public class BatchLogProcessor implements BatchLogInf
{
	// SL4J Logger
	private static Logger log = LoggerFactory.getLogger(BatchLogProcessor.class);
	
	private BatchLogInf realBatchLogProcessor;
	
	public BatchLogProcessor(String filePath, int maxVal) throws IOException
	{
		System.setProperty("log4j.configurationFile", "log/config/log4j2-consoleonly.xml");
		
		String fileExtension = FileUtil.getFileNameExtension(filePath);
		log.info("File Extension : " + fileExtension);
		
		switch(fileExtension)
		{
			case "xml":
			{
				realBatchLogProcessor = new XMLBatchLogProcessor(filePath);
			}
			break;
			case "log":
			{
				realBatchLogProcessor = new TextBatchLogProcessor(filePath, maxVal);
			}
			break;
			case "v2log":
			{
				realBatchLogProcessor = new TextBatchLogProcessorV2(filePath, maxVal);
			}
			break;
			default:
			{
				log.info("Unsupported LogType " + fileExtension);
			}
			break;
		}
	}
	
	@Override
	public int getNumSamples()
	{
		return realBatchLogProcessor.getNumSamples();
	}
	
	/*
	 * *****************************************************************************************************
	 * Axis Ranges
	 *****************************************************************************************************/
	
	@Override
	public double getXValMin()
	{
		return realBatchLogProcessor.getXValMin();
	}
	
	@Override
	public double getXValMax()
	{
		return realBatchLogProcessor.getXValMax();
	}
	
	@Override
	public double getYValMin()
	{
		return realBatchLogProcessor.getYValMin();
	}
	
	@Override
	public double getYValMax()
	{
		return realBatchLogProcessor.getYValMax();
	}
	
	@Override
	public double getZValMin()
	{
		return realBatchLogProcessor.getZValMin();
	}
	
	@Override
	public double getZValMax()
	{
		return realBatchLogProcessor.getZmax();
	}
	
	/*
	 * *****************************************************************************************************
	 * Axis Names
	 *****************************************************************************************************/
	
	@Override
	public String[] getAxisNames()
	{
		return realBatchLogProcessor.getAxisNames();
	}
	
	@Override
	public String getXAxisName()
	{
		return realBatchLogProcessor.getXAxisName();
	}
	
	@Override
	public String getYAxisName()
	{
		return realBatchLogProcessor.getYAxisName();
	}
	
	@Override
	public String getZAxisName()
	{
		return realBatchLogProcessor.getZAxisName();
	}
	
	/*
	 * *****************************************************************************************************
	 * Axis Limits
	 *****************************************************************************************************/
	
	@Override
	public int getXMax()
	{
		return realBatchLogProcessor.getXMax();
	}
	
	@Override
	public int getXMin()
	{
		return realBatchLogProcessor.getXMin();
	}
	
	@Override
	public int getYMax()
	{
		return realBatchLogProcessor.getYMax();
	}
	
	@Override
	public int getYMin()
	{
		return realBatchLogProcessor.getYMin();
	}
	
	@Override
	public double getZmax()
	{
		return realBatchLogProcessor.getZmax();
	}
	
	@Override
	public double getZmin()
	{
		return realBatchLogProcessor.getZmin();
	}
	
	/*
	 * *****************************************************************************************************
	 * Axis Granularity
	 *****************************************************************************************************/
	
	@Override
	public int getXSteps()
	{
		return realBatchLogProcessor.getXSteps();
	}
	
	@Override
	public int getYSteps()
	{
		return realBatchLogProcessor.getYSteps();
	}
	
	/*
	 * *****************************************************************************************************
	 * Jzy3d Compatibility
	 *****************************************************************************************************/
	@Override
	public ITickRenderer getXTickMapper()
	{
		return realBatchLogProcessor.getXTickMapper();
	}
	
	@Override
	public ITickRenderer getYTickMapper()
	{
		return realBatchLogProcessor.getYTickMapper();
	}
	
	/*
	 * *****************************************************************************************************
	 * Processed Data
	 *****************************************************************************************************/
	
	@Override
	public Mapper getAvg()
	{
		return realBatchLogProcessor.getAvg();
	}
	
	/*
	 * Raw Access Method
	 * For high speed processing.
	 */
	@Override
	public double[][] getAvgData()
	{
		return realBatchLogProcessor.getAvgData();
	}
	
	@Override
	public Mapper getStdDev()
	{
		return realBatchLogProcessor.getStdDev();
	}
	
	@Override
	public Mapper getMax()
	{
		return realBatchLogProcessor.getMax();
	}
	
	/*
	 * *****************************************************************************************************
	 * Metrics
	 *****************************************************************************************************/
	
	/*
	 * Ratio of Max values area to plot total area.
	 */
	@Override
	public double getMaxRate()
	{
		return realBatchLogProcessor.getMaxRate();
	}
	
	/*
	 * *****************************************************************************************************
	 * Item methods
	 *****************************************************************************************************/
	
	@Override
	public void clear()
	{
		realBatchLogProcessor.clear();
	}
}
