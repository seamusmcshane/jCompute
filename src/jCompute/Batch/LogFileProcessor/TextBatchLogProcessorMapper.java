package jCompute.Batch.LogFileProcessor;

import jCompute.Batch.LogFileProcessor.Mapper.MapperRemapper;
import jCompute.Batch.LogFileProcessor.Mapper.MapperValuesContainer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.jzy3d.plot3d.primitives.axes.layout.renderers.ITickRenderer;

public class TextBatchLogProcessorMapper implements BatchLogInf
{
	private File file;
	
	private String logName = "";
	private String logType = "";
	private int samples = 0;
	private int sufaceWidthHeight = 0;
	
	private String xAxisName = "";
	private TickValueMapper xMapper;
	
	private String yAxisName = "";
	private TickValueMapper yMapper;
	
	private String zAxisName = "";
	
	private MapperValuesContainer values;
	
	private static ArrayList<TextBatchLogItem> logItems;
	
	private double xValMin = Double.MAX_VALUE;
	private double xValMax = Double.MIN_VALUE;
	private double yValMin = Double.MAX_VALUE;
	private double yValMax = Double.MIN_VALUE;
	private double zValMin = Double.MAX_VALUE;
	private double zValMax = Double.MIN_VALUE;
	
	public TextBatchLogProcessorMapper(String fileName)
	{
		logItems = new ArrayList<TextBatchLogItem>();
		
		file = new File(fileName);
		
		try
		{
			BufferedReader inputFile = new BufferedReader(new FileReader(file));
			
			boolean readingItems = false;
			boolean finished = false;
			while(!finished)
			{
				if(readingItems)
				{
					// Items
					System.out.println("finished");
					
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
						System.out.println("Could not find log file");
					}
				}
			}
			
			inputFile.close();
			
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		
		// +1 for array size dim
		sufaceWidthHeight = (int) Math.sqrt((logItems.size() / samples)) + 1;
		
		System.out.println("Surface Size : " + sufaceWidthHeight);
		System.out.println("Item Total   : " + logItems.size());
		
		values = new MapperValuesContainer(sufaceWidthHeight, sufaceWidthHeight, samples);
		
		for(TextBatchLogItem item : logItems)
		{
			// Choose Plot Source
			double val = item.getStepCount();
			zAxisName = "StepCount";
			
			if(false)
			{
				System.out.println("------------------ ");
				System.out.println("Item ");
				System.out.println("IID       :" + item.getItemId());
				System.out.println("SID       :" + item.getSampleId());
				System.out.println("Hash      :" + item.getHash());
				System.out.println("Pos       :" + item.getCoordsPos()[0] + "x" + item.getCoordsPos()[1]);
				System.out.println("Val       :" + item.getCoordsVals()[0] + "x" + item.getCoordsVals()[0]);
				System.out.println("RunTime   :" + item.getRunTime());
				System.out.println("StepCount :" + item.getStepCount());
				System.out.println("EndEvent  :" + item.getEndEvent());
			}
			values.setSampleValue(item.getCoordsPos()[0], item.getCoordsPos()[1], val);
			
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
			
			if(item.getStepCount()  > zValMax)
			{
				zValMax = item.getStepCount() ;
			}
		}
		
		values.compute();
		
		System.out.println("xValMax" + xValMax);
		System.out.println("yValMax" + yValMax);
		
		System.out.println("xMax" + values.getXMax());
		System.out.println("yMax" + values.getYMax());
		
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
		return xValMax;
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
			return String.valueOf((int) (multi * pos));
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
		
		String line;
		while(!(line = inputFile.readLine()).equals("[-Item]"))
		{
			String field = "";
			String val = "";
			
			if(line.equals("[+Coordinate]"))
			{
				int pos[] = new int[2];
				int vals[] = new int[2];
				
				String cline = inputFile.readLine();
				String cpos1 = cline.substring(cline.lastIndexOf('=') + 1, cline.length());
				pos[0] = Integer.parseInt(cpos1);
				
				cline = inputFile.readLine();
				String cval1 = cline.substring(cline.lastIndexOf('=') + 1, cline.length());
				vals[0] = Integer.parseInt(cval1);
				
				while(!(cline = inputFile.readLine()).equals("[-Coordinate]"))
				{
					System.out.println("> 2 Coords");
				}
				
				cline = inputFile.readLine();
				if(cline.equals("[+Coordinate]"))
				{
					cline = inputFile.readLine();
					String cpos2 = cline.substring(cline.lastIndexOf('=') + 1, cline.length());
					pos[1] = Integer.parseInt(cpos2);
					
					cline = inputFile.readLine();
					String cval2 = cline.substring(cline.lastIndexOf('=') + 1, cline.length());
					vals[1] = Integer.parseInt(cval2);
					
					while(!(cline = inputFile.readLine()).equals("[-Coordinate]"))
					{
						System.out.println("> 2 Coords");
					}
				}
				else
				{
					System.out.println("Error Parsing Coords");
				}
				
				item.setCoordsPos(pos);
				item.setCoordsVals(vals);
			}
			else
			{
				field = line.substring(0, line.lastIndexOf('='));
			}
			
			if(field.equals("IID"))
			{
				val = line.substring(line.lastIndexOf('=') + 1, line.length());
				item.setItemId(Integer.parseInt(val));
			}
			else if(field.equals("SID"))
			{
				val = line.substring(line.lastIndexOf('=') + 1, line.length());
				item.setSampleId(Integer.parseInt(val));
			}
			else if(field.equals("Hash"))
			{
				val = line.substring(line.lastIndexOf('=') + 1, line.length());
				item.setHash(val);
			}
			else if(field.equals("RunTime"))
			{
				val = line.substring(line.lastIndexOf('=') + 1, line.length());
				item.setRunTime(Integer.parseInt(val));
			}
			else if(field.equals("EndEvent"))
			{
				val = line.substring(line.lastIndexOf('=') + 1, line.length());
				item.setEndEvent(val);
			}
			else if(field.equals("StepCount"))
			{
				val = line.substring(line.lastIndexOf('=') + 1, line.length());
				item.setStepCount(Integer.parseInt(val));
			}
		}
		
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
				String field = line.substring(0, line.lastIndexOf('='));
				String val = line.substring(line.lastIndexOf('=') + 1, line.length());
				
				if(field.equals("Name"))
				{
					this.logName = val;
					System.out.println("LogName :" + logName);
				}
				else if(field.equals("LogType"))
				{
					this.logType = val;
					System.out.println("LogType :" + logType);
					
				}
				else if(field.equals("Samples"))
				{
					this.samples = Integer.parseInt(val);
					System.out.println("Samples :" + samples);
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
				
				System.out.println("Axis " + id + " :" + axisName);
				
				axisCount++;
			}
		}
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
		MapperRemapper stdMap = new MapperRemapper(values, 0);
		
		return stdMap;
	}
	
	public MapperRemapper getStdDev()
	{
		MapperRemapper stdMap = new MapperRemapper(values, 1);
		
		return stdMap;
	}
	
	public double[][] getAvgData()
	{
		return values.getAvgData();
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
	
}
