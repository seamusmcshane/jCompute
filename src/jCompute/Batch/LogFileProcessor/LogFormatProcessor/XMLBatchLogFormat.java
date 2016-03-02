package jCompute.Batch.LogFileProcessor.LogFormatProcessor;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.jzy3d.plot3d.primitives.axes.layout.renderers.ITickRenderer;

import jCompute.Batch.LogFileProcessor.BatchLogInf;
import jCompute.Batch.LogFileProcessor.Mapper.MapperRemapper;
import jCompute.Batch.LogFileProcessor.Mapper.MapperValuesContainer;

public class XMLBatchLogFormat implements BatchLogInf
{
	private MapperValuesContainer values;
	
	private File file;
	private XMLConfiguration logFile;
	
	private double xValMin = Double.MAX_VALUE;
	private double xValMax = Double.MIN_VALUE;
	private double yValMin = Double.MAX_VALUE;
	private double yValMax = Double.MIN_VALUE;
	private double zValMin = Double.MAX_VALUE;
	private double zValMax = Double.MIN_VALUE;
	
	private String xAxisName = "";
	private TickValueMapper xMapper;
	
	private String yAxisName = "";
	private TickValueMapper yMapper;
	
	private String zAxisName = "Step";
	
	private double valMin = Double.MAX_VALUE;
	private double valMax = Double.MIN_VALUE;
	
	public XMLBatchLogFormat(String fileName) throws IOException
	{
		file = new File(fileName);
		logFile = new XMLConfiguration();
		logFile.setSchemaValidation(false);
		
		try
		{
			logFile.load(file);
			
			readItems();
		}
		catch(ConfigurationException e)
		{
			Throwable throwable = new Throwable(e.getMessage(), e.getCause());
			
			throwable.setStackTrace(e.getStackTrace());
			
			throw new IOException(throwable);
		}
	}
	
	/*
	 * *****************************************************************************************************
	 * Format Processing Methods
	 *****************************************************************************************************/
	
	private void readItems()
	{
		String path = "";
		
		int itemTotal = logFile.configurationsAt("Items.Item").size();
		
		System.out.println("ItemTotal : " + itemTotal);
		
		xAxisName = logFile.getString("Header." + "AxisLabels.AxisLabel(" + 0 + ").Name", "X");
		yAxisName = logFile.getString("Header." + "AxisLabels.AxisLabel(" + 1 + ").Name", "Y");
		
		int samplesPerItem = logFile.getInt("Header.SamplesPerItem");
		
		int matrixDim = (int) Math.sqrt((itemTotal) / samplesPerItem) + 1;
		
		System.out.println("Samples Per Item : " + samplesPerItem);
		
		System.out.println(matrixDim + "*" + matrixDim);
		
		System.out.println("Creating Values Container");
		values = new MapperValuesContainer(matrixDim, matrixDim, samplesPerItem);
		
		System.out.println(xAxisName);
		System.out.println(yAxisName);
		
		System.out.println("Reading Items Values");
		
		int mod = itemTotal / 10;
		
		List<HierarchicalConfiguration> nodes = logFile.configurationsAt("Items");
		int i = 0;
		for(HierarchicalConfiguration c : nodes)
		{
			ConfigurationNode node = c.getRootNode();
			
			List<ConfigurationNode> items = node.getChildren();
			for(ConfigurationNode item : items)
			{
				String name = item.getName();
				if(name != null)
				{
					// Got A an Item
					if(name.equals("Item"))
					{
						System.out.println("Name " + item.getName());
						
						List<ConfigurationNode> itemFields = item.getChildren();
						
						int itemId = 0;
						int sid;
						int coordinateXY[] = null;
						int coordinateXYvals[] = null;
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
								coordinateXY = new int[coordianteFields.size()];
								coordinateXYvals = new int[coordianteFields.size()];
								for(ConfigurationNode coordiantes : coordianteFields)
								{
									System.out.println("Coordiantes " + coordiantes.getName());
									
									for(ConfigurationNode coordiante : coordiantes.getChildren())
									{
										System.out.println("Field " + coordiante.getName());
										
										if(coordiante.getName().equals("Pos"))
										{
											coordinateXY[pos] = Integer.parseInt((String) coordiante.getValue());
											
											System.out.println("Pos " + coordiante.getValue());
											
											pos++;
										}
										
										if(coordiante.getName().equals("Value"))
										{
											coordinateXYvals[vals] = Integer.parseInt((String) coordiante.getValue());
											System.out.println("Value " + coordiante.getValue());
											
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
								endEvent = (String) itemField.getValue();
							}
							
							if(itemField.getName().equals("StepCount"))
							{
								stepCount = Integer.parseInt((String) itemField.getValue());
							}
							
						}
						
						System.out.println("Item " + itemId);
						System.out.println("X/Y " + coordinateXY[0] + "//" + coordinateXY[0]);
						System.out.println("Vals " + coordinateXYvals[0] + "//" + coordinateXYvals[1]);
						System.out.println("EndEvent " + endEvent);
						System.out.println("StepCount " + stepCount);
						
						//
						if(coordinateXYvals[0] > xValMax)
						{
							xValMax = coordinateXYvals[0];
						}
						
						if(coordinateXYvals[1] > yValMax)
						{
							yValMax = coordinateXYvals[1];
						}
						
						if(coordinateXYvals[0] < xValMin)
						{
							xValMin = coordinateXYvals[0];
						}
						
						if(coordinateXYvals[1] < yValMin)
						{
							yValMin = coordinateXYvals[1];
						}
						
						if(stepCount < zValMin)
						{
							zValMin = stepCount;
						}
						
						if(stepCount > zValMax)
						{
							zValMax = stepCount;
						}
						
						if(i % mod == 0)
						{
							System.out.printf(" %.2f\n", (double) i / itemTotal);
						}
						i++;
						
						int val = stepCount;
						
						/*
						 * if(endEvent.equals("AllPredatorsLTEEndEvent")) {
						 * val=1; } else
						 * if(endEvent.equals("AllPreyLTEEndEvent")) { val=2;
						 * } else { val=0; }
						 */
						
						if(val > valMax)
						{
							valMax = val;
						}
						
						if(val < valMin)
						{
							valMin = val;
						}
						
						values.setSampleValue(coordinateXY[0] - 1, coordinateXY[1] - 1, val);
					}
					
				}
				
			}
		}
		
		values.compute(0);
		
		xMapper = new TickValueMapper(values.getXMax(), xValMax);
		yMapper = new TickValueMapper(values.getYMax(), yValMax);
		
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
		return xValMax;
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
	public int getXMin()
	{
		return values.getXMin();
	}
	
	@Override
	public int getXMax()
	{
		return values.getXMax();
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
	public double getZmin()
	{
		return valMin;
	}
	
	@Override
	public double getZmax()
	{
		return valMax;
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
			return String.valueOf((int) (multi * pos));
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
		MapperRemapper stdMap = new MapperRemapper(values, 0);
		
		return stdMap;
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
		logFile.clear();
	}
}
