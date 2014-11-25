package tools.SurfacePlotGenerator;

public class MapperValuesContainer
{
	private int ySteps;
	private int xSteps;
	private int samples;
	private double[][][] sampleValues;

	private double[][] avg;

	private double[][] stdDev;

	private int xMin = Integer.MAX_VALUE;
	private int xMax = Integer.MIN_VALUE;
	private int yMin = Integer.MAX_VALUE;
	private int yMax = Integer.MIN_VALUE;

	public MapperValuesContainer(int xSteps, int ySteps, int samples)
	{
		this.ySteps = ySteps;
		this.xSteps = xSteps;
		this.samples = samples;

		this.sampleValues = new double[xSteps][ySteps][samples];

		// Fill the array with invalid values
		for(int x = 0; x < xSteps; x++)
		{
			for(int y = 0; y < ySteps; y++)
			{
				for(int i = 0; i < samples; i++)
				{
					sampleValues[x][y][i] = Double.NEGATIVE_INFINITY;
					// System.out.println(sampleValues[x][y][i]);
				}
			}
		}

	}

	public void compute()
	{
		System.out.println("Averages");
		avg = new double[xSteps][ySteps];
		for(int x = 0; x < xSteps; x++)
		{
			for(int y = 0; y < ySteps; y++)
			{
				double total = 0;

				for(int i = 0; i < samples; i++)
				{
					total += sampleValues[x][y][i];
				}

				avg[x][y] = (total / samples);

				// System.out.println(avg[x][y]);
			}
		}

		System.out.println("Standard Deviations");
		stdDev = new double[xSteps][ySteps];
		for(int x = 0; x < xSteps; x++)
		{
			for(int y = 0; y < ySteps; y++)
			{
				double total = 0;
				for(int i = 0; i < samples; i++)
				{
					total += (sampleValues[x][y][i] - avg[x][y]) * (sampleValues[x][y][i] - avg[x][y]);
				}

				stdDev[x][y] = Math.sqrt(total / samples);
				// System.out.println(stdDev[x][y]);

			}
		}

		sampleValues = null;
	}

	public double getStandardDeviations(int getX, int getY)
	{
		return stdDev[getX][getY];
	}

	public double getAvgs(int getX, int getY)
	{
		return avg[getX][getY];
	}

	public void setSampleValue(int x, int y, double value)
	{
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

		if(y < yMin)
		{
			yMin = y;
		}

		for(int i = 0; i < samples; i++)
		{
			// Find a free slot
			if(sampleValues[x][y][i] == Double.NEGATIVE_INFINITY)
			{
				sampleValues[x][y][i] = value;
				// System.out.println(sampleValues[x][y][i]);
				return;
			}
		}
	}

	public int getXMax()
	{
		return xMax;
	}

	public int getXMin()
	{
		return xMin;
	}

	public int getYMax()
	{
		return yMax;
	}

	public int getYMin()
	{
		return yMin;
	}

	public double getAVGValue(int x, int y)
	{
		return avg[x][y];
	}

	public double getStdDevValue(int x, int y)
	{
		return stdDev[x][y];
	}

	public int getSamples()
	{
		return samples;
	}

	public int getXSteps()
	{
		return xSteps;
	}

	public int getYSteps()
	{
		return ySteps;
	}

}
