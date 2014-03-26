package tools.SurfacePlotGenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.jzy3d.plot3d.builder.Mapper;
import org.jzy3d.plot3d.primitives.axes.layout.renderers.ITickRenderer;

import alifeSimGeom.A2DPoint2d;

public class BatchLogProcessorMapper extends Mapper
{
	double pos[][];	
	private int samplesPerItem;
		
	private File file;
	private XMLConfiguration logFile;
	
	private int xPosMin = Integer.MAX_VALUE;
	private int xPosMax = Integer.MIN_VALUE;;
	private int yPosMin = Integer.MAX_VALUE;
	private int yPosMax = Integer.MIN_VALUE;;
	private int xSteps = 0;
	private int ySteps = 0;
	
	private int xValMax = Integer.MIN_VALUE;
	private int yValMax = Integer.MIN_VALUE;
	
	private String xAxisName = "";
	private TickValueMapper xMapper;
	
	private String yAxisName = "";
	private TickValueMapper yMapper;

	
	private String zAxisName = "Step";
	
	public BatchLogProcessorMapper(String fileName)
	{
		file = new File(fileName);		
		logFile = new XMLConfiguration();
		logFile.setSchemaValidation(false);
		try
		{
			logFile.load(file);
		}
		catch (ConfigurationException e)
		{
			e.printStackTrace();
		}
		

		readItems();
		
	}
	
	private void readItems()
	{
		String path = "";
		
		int itemTotal = logFile.configurationsAt("Items.Item").size();

		System.out.println("ItemTotal : " + itemTotal);
		
		pos = new double[itemTotal][itemTotal];
		
		xAxisName = logFile.getString("Header."+"AxisLabels.AxisLabel("+0+").Name","X");
		yAxisName = logFile.getString("Header."+"AxisLabels.AxisLabel("+1+").Name","Y");
		
		samplesPerItem = logFile.getInt("Header.SamplesPerItem");
		
		System.out.println(xAxisName);
		System.out.println(yAxisName);
		
		for(int i=0;i<itemTotal;i++)
		{
			path = "Items.Item("+i+")";			
			
			//int coordTotal = logFile.configurationsAt(path+".Coordinates").size();
			//for(int c=1;c<coordTotal;c++)
			//{
				int x = logFile.getInt(path+"."+"Coordinates.Coordinate("+0+").Pos");
				int xVal = logFile.getInt(path+"."+"Coordinates.Coordinate("+0+").Value");
				int y = logFile.getInt(path+"."+"Coordinates.Coordinate("+1+").Pos");
				int yVal = logFile.getInt(path+"."+"Coordinates.Coordinate("+1+").Value");
				int val = Integer.parseInt(logFile.getString(path+"."+"StepCount"));
			//}

			pos[x][y] += val;
			
			System.out.println("pos["+x+"]["+y+"] : " + pos[x][y]);
			
			
			if(x > xPosMax)
			{
				xPosMax = x;
			}
			
			if(x < xPosMin)
			{
				xPosMin = x;
			}
			
			if(y > yPosMax)
			{
				yPosMax = y;
			}
			
			if(x < yPosMin)
			{
				yPosMin = y;
			}
			
			//
			if(xVal > xValMax)
			{
				xValMax = xVal;
			}
			
			if(yVal > yValMax)
			{
				yValMax = yVal;
			}

			
		}		
				
		// Average the samples
		for(int x=0;x<itemTotal;x++)
		{
			for(int y=0;y<itemTotal;y++)
			{
				pos[x][y] = pos[x][y] / samplesPerItem;
			}
		}
		
		xSteps = xPosMax;
		ySteps = yPosMax;
		
		System.out.println("xValMax" + xValMax);
		System.out.println("yValMax" + yValMax);
		
		xMapper = new TickValueMapper(xSteps,xValMax);
		yMapper = new TickValueMapper(ySteps,yValMax);
	}
	
	public ITickRenderer getXTickMapper()
	{
		return xMapper;
	}
	
	public ITickRenderer getYTickMapper()
	{
		return yMapper;
	}
	
	public int getXSteps()
	{
		return xSteps;
	}
	
	public int getYSteps()
	{
		return ySteps;
	}
	
	public int getXmin()
	{
		return xPosMin;
	}

	public int getXmax()
	{
		return xPosMax;
	}

	public int getYmin()
	{
		return yPosMin;
	}

	public int getYmax()
	{
		return yPosMax;
	}
	
	@Override
	public double f(double x, double y)
	{
		return pos[(int) x][(int) y];
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
	
	private class TickValueMapper implements ITickRenderer  
	{
		double multi = 0;
		public TickValueMapper(int coordMax, double valueMax)
		{
			super();

			multi = valueMax/coordMax;
			
		}


		@Override
		public String format(double pos)
		{
			return String.valueOf((int)(multi * pos));
		}
		
	}
	
}
