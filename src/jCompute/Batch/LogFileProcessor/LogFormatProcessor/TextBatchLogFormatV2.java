package jCompute.Batch.LogFileProcessor.LogFormatProcessor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Stream;

import org.jzy3d.plot3d.primitives.axes.layout.renderers.ITickRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jCompute.Batch.LogFileProcessor.BatchLogInf;
import jCompute.Batch.LogFileProcessor.Mapper.MapperRemapper;
import jCompute.Batch.LogFileProcessor.Mapper.MapperValuesContainer;
import jCompute.Datastruct.knn.benchmark.TimerObj;

public class TextBatchLogFormatV2 implements BatchLogInf
{
	private static Logger log = LoggerFactory.getLogger(TextBatchLogFormat.class);
	
	public static final int HEADER_LINE_OPTS = 4;
	public static final int MAX_LINE_OPTS = 7;
	
	public static final char OPTION_DELIMITER = ',';
	public static final char SUBOPTION_DELIMITER = ';';
	public static final char FIELD_DELIMITER = '=';
	
	private String logName = "";
	private String logType = "";
	private int samples = 0;
	
	private String xAxisName = "";
	private TickValueMapper xMapper;
	
	private String yAxisName = "";
	private TickValueMapper yMapper;
	
	private String zAxisName = "";
	
	private MapperValuesContainer values;
	
	private ArrayList<TextBatchLogItem> logItems;
	
	private double xValMin = Double.MAX_VALUE;
	private double xValMax = Double.MIN_VALUE;
	private double yValMin = Double.MAX_VALUE;
	private double yValMax = Double.MIN_VALUE;
	private double zValMin = Double.MAX_VALUE;
	private double zValMax = Double.MIN_VALUE;
	
	public TextBatchLogFormatV2(String filePath, int maxVal) throws IOException
	{
		Path path = Paths.get(filePath);
		
		TimerObj to = new TimerObj();
		
		logItems = new ArrayList<TextBatchLogItem>();
		
		Stream<String> headerLines = Files.lines(path);
		Optional<String> oHeader = headerLines.findFirst();
		String header = oHeader.get();
		readHeaderLine(header);
		headerLines.close();
		
		Stream<String> itemLines = Files.readAllLines(path).parallelStream().skip(1L).parallel();
		
		to.startTimer();
		
		itemLines.forEach(s ->
		{
			readItemLine(s);
			// System.out.println(s);
		});
		
		to.stopTimer();
		
		itemLines.close();
		
		HashMap<Integer, Integer> xUnique = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> yUnique = new HashMap<Integer, Integer>();
		
		log.info("Num coords : " + logItems.get(0).getCoordsPos().length);
		
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
		
		log.info("X Dim : " + xDimSize);
		log.info("Y Dim : " + yDimSize);
		
		// System.out.println("xValMin " + xValMin);
		// System.out.println("xValMax " + xValMax);
		// System.out.println("yValMin " + yValMin);
		// System.out.println("yValMax " + yValMax);
		
		log.info("Surface Size : " + xDimSize * yDimSize);
		log.info("Item Total   : " + logItems.size());
		
		values = new MapperValuesContainer(xDimSize, yDimSize, samples);
		
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
				
				log.warn("Item " + i + " Not correct : " + IIDS[i] + " " + samples);
			}
		}
		
		if(itemsSamplesCorrect)
		{
			log.info("All items have correct number of samples(" + samples + ").");
		}
		else
		{
			log.warn("Some items do not have the correct number of samples. ");
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
				
				log.warn("Item " + i + " Not correct : " + SIDS[i] + " " + samples);
			}
			
		}
		
		if(itemsSamplesNumbersCorrect)
		{
			log.info("All sample numbers appear correct(" + logItems.size() / samples + ").");
		}
		else
		{
			log.warn("Sample numbers do not appear correct.");
		}
		
		log.warn("Store Errors " + storeErrors);
		
		values.compute(maxVal);
		
		log.info("xValMax" + xValMax);
		log.info("yValMax" + yValMax);
		
		log.info("xMax" + values.getXMax());
		log.info("yMax" + values.getYMax());
		
		xMapper = new TickValueMapper(values.getXMax(), xValMax);
		yMapper = new TickValueMapper(values.getYMax(), yValMax);
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
		
		System.out.println("Header");
		for(int i = 0; i < options.length; i++)
		{
			String[] kvp = getOptionKVPair(options[i]);
			
			String field = kvp[0];
			String value = kvp[1];
			
			switch(field)
			{
				case "Name":
				{
					System.out.println(field + " : " + value);
					logName = value;
					log.info("LogName :" + logName);
				}
				break;
				case "LogType":
				{
					System.out.println(field + " : " + value);
					this.logType = value;
					log.info("LogType :" + logType);
				}
				break;
				case "Samples":
				{
					System.out.println(field + " : " + value);
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
					
					System.out.print(field + " ");
					
					// Pos + Vals
					for(int c = 0; c < axisInfo.length; c += 2)
					{
						String[] axIdkvp = getOptionKVPair(axisInfo[c]);
						String[] axLakvp = getOptionKVPair(axisInfo[c + 1]);
						
						axisId[c / 2] = Integer.parseInt(axIdkvp[1]);
						axisName[(c + 1) / 2] = axLakvp[1];
						
						System.out.print(axisId[c / 2] + " " + axisName[(c + 1) / 2] + " ");
					}
					System.out.println();
					
					// Hardcoded order
					xAxisName = axisName[0];
					
					log.info("xAxis " + axisId[0] + " :" + axisName);
					
					yAxisName = axisName[1];
					
					log.info("yAxis " + axisId[1] + " :" + axisName);
					
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
		TextBatchLogItem item = new TextBatchLogItem();
		
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
					System.out.println("VaLS " + value);
					
					// num=int;
					int nS = value.indexOf(FIELD_DELIMITER) + 1;
					int nE = value.indexOf(SUBOPTION_DELIMITER);
					String sNum = value.substring(nS, nE);
					int num = Integer.parseInt(sNum);
					System.out.println("Num " + num);
					
					System.out.println("TEST");
					
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
					item.setHash(value);
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
		
		synchronized(logItems)
		{
			logItems.add(item);
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
	public double getZValMin()
	{
		return zValMin;
	}
	
	@Override
	public double getZValMax()
	{
		return zValMax;
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
	public int getYMin()
	{
		return values.getYMin();
	}
	
	@Override
	public int getYMax()
	{
		return values.getYMax();
	}
	
	@Override
	public double getZmax()
	{
		return values.getZMax();
	}
	
	@Override
	public double getZmin()
	{
		return values.getZMin();
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
	 * Jzy3d Compatibility
	 *****************************************************************************************************/
	
	private class TickValueMapper implements ITickRenderer
	{
		double multi = 0;
		
		public TickValueMapper(int coordMax, double valueMax)
		{
			super();
			
			multi = valueMax / coordMax;
		}
		
		@Override
		public String format(double pos)
		{
			double val = (multi * pos);
			
			if(val % 1.0 == 0)
			{
				return String.valueOf((int) (val));
			}
			else
			{
				return String.format("%.3g%n", val);
			}
		}
	}
	
	@Override
	public ITickRenderer getXTickMapper()
	{
		return xMapper;
	}
	
	@Override
	public ITickRenderer getYTickMapper()
	{
		return yMapper;
	}
	
	/*
	 * *****************************************************************************************************
	 * Processed Data
	 *****************************************************************************************************/
	
	@Override
	public MapperRemapper getAvg()
	{
		MapperRemapper avgMap = new MapperRemapper(values, 0);
		
		return avgMap;
	}
	
	@Override
	public double[][] getAvgData()
	{
		return values.getAvgData();
	}
	
	@Override
	public MapperRemapper getStdDev()
	{
		MapperRemapper stdMap = new MapperRemapper(values, 1);
		
		return stdMap;
	}
	
	@Override
	public MapperRemapper getMax()
	{
		MapperRemapper maxMap = new MapperRemapper(values, 2);
		
		return maxMap;
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
	
	@Override
	public void clear()
	{
		logItems.clear();
	}
}
