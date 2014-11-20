package tools.SurfacePlotGenerator;

import java.io.File;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.jzy3d.plot3d.builder.Mapper;
import org.jzy3d.plot3d.primitives.axes.layout.renderers.ITickRenderer;

public class BatchLogProcessorMapper extends Mapper
{
	// Values
	double valueTotals[][];

	// Averages
	double avgs[][];

	// Totals of the deviations
	double stddevTotals[][];

	// Plot Values
	double plotValues[][];

	private int samplesPerItem;

	private File file;
	private XMLConfiguration logFile;

	private int xPosMin = Integer.MAX_VALUE;
	private int xPosMax = Integer.MIN_VALUE;
	private int yPosMin = Integer.MAX_VALUE;
	private int yPosMax = Integer.MIN_VALUE;
	private int xSteps = 0;
	private int ySteps = 0;

	private int xValMax = Integer.MIN_VALUE;
	private int yValMax = Integer.MIN_VALUE;

	private String xAxisName = "";
	private TickValueMapper xMapper;

	private String yAxisName = "";
	private TickValueMapper yMapper;

	private String zAxisName = "Step";

	private double valMin = Double.MAX_VALUE;
	private double valMax = Double.MIN_VALUE;
	
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

		readItemsNew(stdDev);

	}

	private void debugOut(String text)
	{
		// debugOut(text);
	}
	
	private void readItemsNew(boolean doStdDev)
	{
		String path = "";

		int itemTotal = logFile.configurationsAt("Items.Item").size();

		debugOut("ItemTotal : " + itemTotal);

		xAxisName = logFile.getString("Header." + "AxisLabels.AxisLabel(" + 0 + ").Name", "X");
		yAxisName = logFile.getString("Header." + "AxisLabels.AxisLabel(" + 1 + ").Name", "Y");

		samplesPerItem = logFile.getInt("Header.SamplesPerItem");

		int matrixDim = (int) Math.sqrt((itemTotal / samplesPerItem)) + 1;

		debugOut("Samples Per Item : " + samplesPerItem);

		debugOut(matrixDim + "*" + matrixDim);

		System.out.println("Creating Values Array");
		valueTotals = new double[matrixDim][matrixDim];

		System.out.println("Creating Averages Array");
		avgs = new double[matrixDim][matrixDim];

		if(doStdDev)
		{
			debugOut("Creating Deviation Totals Array");
			stddevTotals = new double[matrixDim][matrixDim];
		}

		System.out.println("Initialising Values");

		debugOut(xAxisName);
		debugOut(yAxisName);

		System.out.println("Reading Items Values");

		int mod = itemTotal / 10;

		List<HierarchicalConfiguration> nodes = logFile.configurationsAt("Items");
		int i=0;
		for(HierarchicalConfiguration c : nodes)
		{
			ConfigurationNode node = c.getRootNode();

			List<ConfigurationNode> items = node.getChildren();
			for(ConfigurationNode item : items)
			{
				String name = item.getName();
				if(name!=null)
				{
					// Got A an Item
					if(name.equals("Item"))
					{
						debugOut("Name " + item.getName());

						List<ConfigurationNode> itemFields = item.getChildren();
						
						int itemId = 0;
						int sid;
						int coordinateXY[] = {0,0};
						int coordinateXYvals[] = {0,0};				
						String endEvent = "";
						int stepCount = 0;
						long RunTime = 0;
						
						for(ConfigurationNode itemField : itemFields)
						{
							if(itemField.getName().equals("IID"))
							{
								itemId = Integer.parseInt((String) itemField.getValue());
							}
							
							if(itemField.getName().equals("SID"))
							{
								sid = Integer.parseInt((String) itemField.getValue());
							}
							
							if(itemField.getName().equals("Coordinates"))
							{
								List<ConfigurationNode> coordianteFields = itemField.getChildren();

								int pos = 0;
								int vals = 0;
								for(ConfigurationNode coordiantes : coordianteFields)
								{
									debugOut("Coordiantes " + coordiantes.getName());
									
									for(ConfigurationNode coordiante : coordiantes.getChildren())
									{
										debugOut("Field " + coordiante.getName());

										if(coordiante.getName().equals("Pos"))
										{
											coordinateXY[pos] = Integer.parseInt((String) coordiante.getValue());
											
											debugOut("Pos " +  coordiante.getValue());
											
											pos++;
										}
										
										if(coordiante.getName().equals("Value"))
										{
											coordinateXYvals[vals] = Integer.parseInt((String) coordiante.getValue());
											debugOut("Value " +  coordiante.getValue());

											vals++;
										}
									}
									

									
								}
							}
														
							if(itemField.getName().equals("RunTime"))
							{
								// TOTO output time in secs not date/time
								// runTime = (long)itemField.getValue();
							}
							
							if(itemField.getName().equals("EndEvent"))
							{
								endEvent = (String)itemField.getValue();
							}
							
							if(itemField.getName().equals("StepCount"))
							{
								stepCount = Integer.parseInt((String) itemField.getValue());
							}
							
						}
						
						debugOut("Item " + itemId);		
						debugOut("X/Y " + coordinateXY[0] + "//" + coordinateXY[0]);		
						debugOut("Vals " + coordinateXYvals[0] + "//" + coordinateXYvals[1]);	
						debugOut("EndEvent " + endEvent);		
						debugOut("StepCount " + stepCount);		


						if(coordinateXY[0] > xPosMax)
						{
							xPosMax = coordinateXY[0];
						}

						if(coordinateXY[0] < xPosMin)
						{
							xPosMin = coordinateXY[0];
						}

						if( coordinateXY[1] > yPosMax)
						{
							yPosMax = coordinateXY[1];
						}

						if(coordinateXY[1] < yPosMin)
						{
							yPosMin = coordinateXY[1];
						}

						//
						if(coordinateXYvals[0]  > xValMax)
						{
							xValMax = coordinateXYvals[0] ;
						}

						if(coordinateXYvals[1]  > yValMax)
						{
							yValMax = coordinateXYvals[1] ;
						}

						if(i % mod == 0)
						{
							System.out.printf(" %.2f\n", (double) i / itemTotal);
						}
						i++;
						
						
						int val = stepCount;
						
						/*if(endEvent.equals("AllPredatorsLTEEndEvent"))
						{
							val=1;
						}
						else if(endEvent.equals("AllPreyLTEEndEvent"))
						{
							val=2;

						}
						else
						{
							val=0;
						}*/
						
						if(val > valMax)
						{
							valMax = val;
						}

						if(val < valMin)
						{
							valMin = val;
						}
						
						valueTotals[coordinateXY[0]][coordinateXY[1]] += val;						
					}

				}
				
			}			
		}
		
		xSteps = xPosMax;
		ySteps = yPosMax;

		debugOut("xValMax" + xValMax);
		debugOut("yValMax" + yValMax);

		xMapper = new TickValueMapper(xSteps, xValMax);
		yMapper = new TickValueMapper(ySteps, yValMax);
		
		System.out.println("Calculating Averages");

		// Averages are the plot values
		for(int x = 0; x < matrixDim; x++)
		{
			for(int y = 0; y < matrixDim; y++)
			{
				avgs[x][y] = valueTotals[x][y] / samplesPerItem;
			}
		}
		
		if(doStdDev)
		{
			System.out.println("Calculating Standard Deviation (dev)");

			i=0;
			for(HierarchicalConfiguration c : nodes)
			{
				ConfigurationNode node = c.getRootNode();

				List<ConfigurationNode> items = node.getChildren();
				for(ConfigurationNode item : items)
				{
					String name = item.getName();
					if(name!=null)
					{
						// Got A an Item
						if(name.equals("Item"))
						{
							debugOut("Name " + item.getName());

							List<ConfigurationNode> itemFields = item.getChildren();
							
							int itemId = 0;
							int sid;
							int coordinateXY[] = {0,0};
							int coordinateXYvals[] = {0,0};				
							String endEvent = "";
							int stepCount = 0;
							long RunTime = 0;
							
							for(ConfigurationNode itemField : itemFields)
							{
								if(itemField.getName().equals("IID"))
								{
									itemId = Integer.parseInt((String) itemField.getValue());
								}
								
								if(itemField.getName().equals("SID"))
								{
									sid = Integer.parseInt((String) itemField.getValue());
								}
								
								if(itemField.getName().equals("Coordinates"))
								{
									List<ConfigurationNode> coordianteFields = itemField.getChildren();

									int pos = 0;
									int vals = 0;
									for(ConfigurationNode coordiantes : coordianteFields)
									{
										debugOut("Coordiantes " + coordiantes.getName());
										
										for(ConfigurationNode coordiante : coordiantes.getChildren())
										{
											debugOut("Field " + coordiante.getName());

											if(coordiante.getName().equals("Pos"))
											{
												coordinateXY[pos] = Integer.parseInt((String) coordiante.getValue());
												
												debugOut("Pos " +  coordiante.getValue());
												
												pos++;
											}
											
											if(coordiante.getName().equals("Value"))
											{
												coordinateXYvals[vals] = Integer.parseInt((String) coordiante.getValue());
												debugOut("Value " +  coordiante.getValue());

												vals++;
											}
										}
										

										
									}
								}
															
								if(itemField.getName().equals("RunTime"))
								{
									// TOTO output time in secs not date/time
									// runTime = (long)itemField.getValue();
								}
								
								if(itemField.getName().equals("EndEvent"))
								{
									endEvent = (String)itemField.getValue();
								}
								
								if(itemField.getName().equals("StepCount"))
								{
									stepCount = Integer.parseInt((String) itemField.getValue());
								}
								
							}
							
							debugOut("Item " + itemId);		
							debugOut("X/Y " + coordinateXY[0] + "//" + coordinateXY[0]);		
							debugOut("Vals " + coordinateXYvals[0] + "//" + coordinateXYvals[1]);	
							debugOut("EndEvent " + endEvent);		
							debugOut("StepCount " + stepCount);		


							if(coordinateXY[0] > xPosMax)
							{
								xPosMax = coordinateXY[0];
							}

							if(coordinateXY[0] < xPosMin)
							{
								xPosMin = coordinateXY[0];
							}

							if( coordinateXY[1] > yPosMax)
							{
								yPosMax = coordinateXY[1];
							}

							if(coordinateXY[1] < yPosMin)
							{
								yPosMin = coordinateXY[1];
							}

							//
							if(coordinateXYvals[0]  > xValMax)
							{
								xValMax = coordinateXYvals[0] ;
							}

							if(coordinateXYvals[1]  > yValMax)
							{
								yValMax = coordinateXYvals[1] ;
							}

							if(i % mod == 0)
							{
								System.out.printf(" %.2f\n", (double) i / itemTotal);
							}
							i++;
							
							
							int val=stepCount;
							/*if(endEvent.equals("AllPredatorsLTEEndEvent"))
							{
								val = 1;
							}
							else if(endEvent.equals("AllPreyLTEEndEvent"))
							{
								val = 2;
							}*/

							
							if(val > valMax)
							{
								valMax = val;
							}

							if(val < valMin)
							{
								valMin = val;
							}
							
							stddevTotals[coordinateXY[0]][coordinateXY[1]] += (avgs[coordinateXY[0]][coordinateXY[1]] - val) * (avgs[coordinateXY[0]][coordinateXY[1]] - val);
							
							
						}

					}
					
				}			
			}
			
			System.out.println("Calculating Standard Deviation (totals)");

			plotValues = new double[matrixDim][matrixDim];
			
			// Plot values are standard deviations
			for(int x = 0; x < matrixDim; x++)
			{
				for(int y = 0; y < matrixDim; y++)
				{
					plotValues[x][y] = Math.sqrt(stddevTotals[x][y] / samplesPerItem);
				}
			}
			
		}
		else
		{
			plotValues = avgs;
		}

	}

	private void readItems(boolean doStdDev)
	{
		String path = "";

		int itemTotal = logFile.configurationsAt("Items.Item").size();

		debugOut("ItemTotal : " + itemTotal);

		xAxisName = logFile.getString("Header." + "AxisLabels.AxisLabel(" + 0 + ").Name", "X");
		yAxisName = logFile.getString("Header." + "AxisLabels.AxisLabel(" + 1 + ").Name", "Y");

		samplesPerItem = logFile.getInt("Header.SamplesPerItem");

		int matrixDim = (int) Math.sqrt((itemTotal / samplesPerItem)) + 1;

		debugOut("Samples Per Item : " + samplesPerItem);

		debugOut(matrixDim + "*" + matrixDim);

		debugOut("Creating Plot Array");
		plotValues = new double[matrixDim][matrixDim];

		debugOut("Creating Values Array");
		valueTotals = new double[matrixDim][matrixDim];

		debugOut("Creating Averages Array");
		avgs = new double[matrixDim][matrixDim];

		if(doStdDev)
		{
			debugOut("Creating Deviation Totals Array");
			stddevTotals = new double[matrixDim][matrixDim];
		}

		debugOut("Initialising Values");

		debugOut(xAxisName);
		debugOut(yAxisName);

		debugOut("Reading Items Values");

		int mod = itemTotal / 10;

		for(int i = 0; i < itemTotal; i++)
		{
			path = "Items.Item(" + i + ")";

			// int coordTotal =
			// logFile.configurationsAt(path+".Coordinates").size();
			// for(int c=1;c<coordTotal;c++)
			// {
			int x = logFile.getInt(path + "." + "Coordinates.Coordinate(" + 0 + ").Pos");
			int xVal = logFile.getInt(path + "." + "Coordinates.Coordinate(" + 0 + ").Value");
			int y = logFile.getInt(path + "." + "Coordinates.Coordinate(" + 1 + ").Pos");
			int yVal = logFile.getInt(path + "." + "Coordinates.Coordinate(" + 1 + ").Value");
			int val = Integer.parseInt(logFile.getString(path + "." + "StepCount"));
			// }

			// Loop through the sample array and find the next free slot
			valueTotals[x][y] += val;

			// debugOut("pos["+x+"]["+y+"] : " + pos[x][y]);

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

			if(i % mod == 0)
			{
				System.out.printf(" %.2f\n", (double) i / itemTotal);
			}

		}

		if(doStdDev)
		{
			debugOut("Calculating Standard Deviation (avg)");

			// Calculate All Averages
			for(int x = 0; x < matrixDim; x++)
			{
				for(int y = 0; y < matrixDim; y++)
				{
					avgs[x][y] = valueTotals[x][y] / samplesPerItem;
				}
			}

			debugOut("Reading Items Values - Standard Deviation (totals)");

			for(int i = 0; i < itemTotal; i++)
			{
				path = "Items.Item(" + i + ")";

				// int coordTotal =
				// logFile.configurationsAt(path+".Coordinates").size();
				// for(int c=1;c<coordTotal;c++)
				// {
				int x = logFile.getInt(path + "." + "Coordinates.Coordinate(" + 0 + ").Pos");
				int xVal = logFile.getInt(path + "." + "Coordinates.Coordinate(" + 0 + ").Value");
				int y = logFile.getInt(path + "." + "Coordinates.Coordinate(" + 1 + ").Pos");
				int yVal = logFile.getInt(path + "." + "Coordinates.Coordinate(" + 1 + ").Value");
				int val = Integer.parseInt(logFile.getString(path + "." + "StepCount"));
				// }

				stddevTotals[x][y] += (avgs[x][y] - val) * (avgs[x][y] - val);

				if(i % mod == 0)
				{
					System.out.printf(" %.2f\n", (double) i / itemTotal);
				}
			}

			debugOut("Calculating Standard Deviation (deviation)");

			// Plot values are standard deviations
			for(int x = 0; x < matrixDim; x++)
			{
				for(int y = 0; y < matrixDim; y++)
				{
					plotValues[x][y] = Math.sqrt(stddevTotals[x][y] / samplesPerItem);
				}
			}

		}
		else
		{
			debugOut("Calculating Averages");

			// Averages are the plot values
			for(int x = 0; x < matrixDim; x++)
			{
				for(int y = 0; y < matrixDim; y++)
				{
					plotValues[x][y] = valueTotals[x][y] / samplesPerItem;
				}

			}
		}

		xSteps = xPosMax;
		ySteps = yPosMax;

		debugOut("xValMax" + xValMax);
		debugOut("yValMax" + yValMax);

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

	public double getZmax()
	{
		return valMax;
	}

	public double getZmin()
	{
		return valMin;
	}
}
