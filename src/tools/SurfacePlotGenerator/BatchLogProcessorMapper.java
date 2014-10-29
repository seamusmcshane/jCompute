package tools.SurfacePlotGenerator;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.jzy3d.plot3d.builder.Mapper;
import org.jzy3d.plot3d.primitives.axes.layout.renderers.ITickRenderer;

public class BatchLogProcessorMapper extends Mapper
{
	double plotValues[][];
	private ArrayList<Double>[][] surfaceLists;

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

	public BatchLogProcessorMapper(String fileName, int mode)
	{
		boolean stdDev = false;
		if(mode == 0)
		{
			stdDev = false;
		}
		else
		{
			stdDev = true;
		}

		file = new File(fileName);
		logFile = new XMLConfiguration();
		logFile.setSchemaValidation(false);
		try
		{
			logFile.load(file);
		}
		catch(ConfigurationException e)
		{
			System.out.print("Error");
			e.printStackTrace();
		}

		readItems(stdDev);

	}

	private void readItems(boolean doStdDev)
	{
		String path = "";

		int itemTotal = logFile.configurationsAt("Items.Item").size();

		System.out.println("ItemTotal : " + itemTotal);

		xAxisName = logFile.getString("Header." + "AxisLabels.AxisLabel(" + 0 + ").Name", "X");
		yAxisName = logFile.getString("Header." + "AxisLabels.AxisLabel(" + 1 + ").Name", "Y");

		samplesPerItem = logFile.getInt("Header.SamplesPerItem");

		int matrixDim = itemTotal / samplesPerItem;

		System.out.println("Samples Per Item : " + samplesPerItem);

		System.out.println("Creating Plot Array");
		plotValues = new double[matrixDim][matrixDim];

		System.out.println("Creating Surface Lists");
		surfaceLists = new ArrayList[matrixDim][matrixDim];

		System.out.println("Initialising Lists");

		for(int x = 0; x < matrixDim; x++)
		{
			for(int y = 0; y < matrixDim; y++)
			{
				for(int s = 0; s < samplesPerItem; s++)
				{
					// A value to detect the slot is not filled
					surfaceLists[x][y] = new ArrayList<Double>(samplesPerItem);
				}

			}
		}

		System.out.println(xAxisName);
		System.out.println(yAxisName);

		System.out.println("Reading Values");

		for(int i = 0; i < itemTotal; i++)
		{
			path = "Items.Item(" + i + ")";

			int x = logFile.getInt(path + "." + "Coordinates.Coordinate(" + 0 + ").Pos");
			int xVal = logFile.getInt(path + "." + "Coordinates.Coordinate(" + 0 + ").Value");
			int y = logFile.getInt(path + "." + "Coordinates.Coordinate(" + 1 + ").Pos");
			int yVal = logFile.getInt(path + "." + "Coordinates.Coordinate(" + 1 + ").Value");
			int val = Integer.parseInt(logFile.getString(path + "." + "StepCount"));

			surfaceLists[x][y].add((double) val);

			// System.out.println("pos["+x+"]["+y+"] : " + pos[x][y]);

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
		System.out.println("Calulating Plot Values");
		for(int x = 0; x < matrixDim; x++)
		{
			for(int y = 0; y < matrixDim; y++)
			{
				double total = 0;
				double avg = 0;

				for(double val : surfaceLists[x][y])
				{
					total += val;
				}

				avg = total / samplesPerItem;

				if(!doStdDev)
				{
					plotValues[x][y] = avg;
				}
				else
				{
					double stdDevTotal = 0;

					for(double val : surfaceLists[x][y])
					{
						stdDevTotal += (avg - val) * (avg - val);
					}

					// StdDev
					plotValues[x][y] = Math.sqrt(stdDevTotal / samplesPerItem);

					// variance
					// plotValues[x][y] = stdDevTotal/samplesPerItem;
				}

			}
		}

		xSteps = xPosMax;
		ySteps = yPosMax;

		System.out.println("xValMax" + xValMax);
		System.out.println("yValMax" + yValMax);

		xMapper = new TickValueMapper(xSteps, xValMax);
		yMapper = new TickValueMapper(ySteps, yValMax);
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
		return plotValues[(int) x][(int) y];
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

			multi = valueMax / coordMax;

		}

		@Override
		public String format(double pos)
		{
			return String.valueOf((int) (multi * pos));
		}

	}

	public double getValueMax()
	{
		return Math.max(xValMax, yValMax);
	}

}
