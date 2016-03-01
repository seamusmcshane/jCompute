package jCompute.Batch.LogFileProcessor;

import jCompute.Batch.LogFileProcessor.Mapper.MapperRemapper;
import jCompute.Batch.LogFileProcessor.Mapper.MapperValuesContainer;
import jCompute.Datastruct.knn.benchmark.TimerObj;
import jCompute.util.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.jzy3d.plot3d.primitives.axes.layout.renderers.ITickRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextBatchLogProcessorMapper implements BatchLogInf
{
	private static Logger log = LoggerFactory.getLogger(TextBatchLogProcessorMapper.class);
	
	private File file;
	
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
	
	public TextBatchLogProcessorMapper(String fileName, int maxVal)
	{
		logItems = new ArrayList<TextBatchLogItem>();
		
		file = new File(fileName);
		
		try
		{
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
			
			log.info("Finished Reading log " + Text.longTimeToDHMSM(to.getTimeTaken()));
			
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
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
	
	public double getXValMin()
	{
		return xValMin;
	}
	
	public double getXValMax()
	{
		return xValMax;
		
	}
	
	public double getYValMin()
	{
		return yValMin;
	}
	
	public double getYValMax()
	{
		return yValMax;
	}
	
	private class TickValueMapper implements ITickRenderer
	{
		double multi = 0;
		
		public TickValueMapper(int coordMax, double valueMax)
		{
			super();
			
			multi = valueMax / (double) coordMax;
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
					this.logName = val;
					log.info("LogName :" + logName);
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
	
	public double getZmax()
	{
		return values.getZMax();
	}
	
	public double getZmin()
	{
		return values.getZMin();
	}
	
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
	
	public int getXSteps()
	{
		return values.getXSteps();
	}
	
	public int getYSteps()
	{
		return values.getYSteps();
	}
	
	public int getSamples()
	{
		return values.getSamples();
	}
	
	public ITickRenderer getXTickMapper()
	{
		return xMapper;
	}
	
	public ITickRenderer getYTickMapper()
	{
		return yMapper;
	}
	
	public MapperRemapper getAvg()
	{
		MapperRemapper avgMap = new MapperRemapper(values, 0);
		
		return avgMap;
	}
	
	public double[][] getAvgData()
	{
		return values.getAvgData();
	}
	
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
	public void clear()
	{
		logItems.clear();
	}
	
	@Override
	public double getMaxRate()
	{
		return values.getMaxRate();
	}
}
