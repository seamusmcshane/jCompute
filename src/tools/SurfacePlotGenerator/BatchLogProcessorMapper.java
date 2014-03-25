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

import alifeSimGeom.A2DPoint2d;

public class BatchLogProcessorMapper extends Mapper
{
	int pos[][];	

	private File file;
	private XMLConfiguration logFile;

	private int xMin = Integer.MAX_VALUE;
	private int xMax = Integer.MIN_VALUE;;
	private int yMin = Integer.MAX_VALUE;
	private int yMax = Integer.MIN_VALUE;;
	private int xSteps = 0;
	private int ySteps = 0;
	
	private String xAxisName = "";
	private String yAxisName = "";
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
		
		int itemTotal = logFile.configurationsAt("Item").size();

		System.out.println("ItemTotal : " + itemTotal);
		
		pos = new int[itemTotal][itemTotal];
		
		for(int i=0;i<itemTotal;i++)
		{
			path = "Item("+i+")";			
			
			int x = (Integer.parseInt(logFile.getString(path+"."+"Coordinate"+1)))-1;
			int y = (Integer.parseInt(logFile.getString(path+"."+"Coordinate"+2)))-1;;
			int val = Integer.parseInt(logFile.getString(path+"."+"StepCount"));
			
			xAxisName = logFile.getString(path+"."+"CoordinateName"+1,"X");
			yAxisName = logFile.getString(path+"."+"CoordinateName"+2,"Y");
			
			pos[x][y] = val;
			
			System.out.println("pos["+x+"]["+y+"] : " + pos[x][y]);
			
			
			if(x > xMax)
			{
				xMax = x;
			}
			
			if(x < xMin)
			{
				xMin = x;
			}
			
			if(y > yMax)
			{
				yMax = y;
			}
			
			if(x < yMin)
			{
				yMin = y;
			}
			
			
		}		
		
		xSteps = xMax;
		ySteps = yMax;
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
		return xMin;
	}

	public int getXmax()
	{
		return xMax;
	}

	public int getYmin()
	{
		return yMin;
	}

	public int getYmax()
	{
		return yMax;
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
	
}
