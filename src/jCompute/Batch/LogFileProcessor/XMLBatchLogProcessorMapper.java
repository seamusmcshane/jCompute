package jCompute.Batch.LogFileProcessor;

import jCompute.Batch.LogFileProcessor.Mapper.MapperRemapper;
import jCompute.Batch.LogFileProcessor.Mapper.MapperValuesContainer;

import java.io.File;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.jzy3d.plot3d.primitives.axes.layout.renderers.ITickRenderer;

public class XMLBatchLogProcessorMapper implements BatchLogInf
{
	private MapperValuesContainer values;
	
	private File file;
	private XMLConfiguration logFile;
	
	private double xValMin = Double.MAX_VALUE;
	private double xValMax = Double.MIN_VALUE;
	private double yValMin = Double.MAX_VALUE;
	private double yValMax = Double.MIN_VALUE;
	
	private String xAxisName = "";
	private TickValueMapper xMapper;
	
	private String yAxisName = "";
	private TickValueMapper yMapper;
	
	private String zAxisName = "Step";
	
	private double valMin = Double.MAX_VALUE;
	private double valMax = Double.MIN_VALUE;
	
	public XMLBatchLogProcessorMapper(String fileName)
	{
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
		
		readItems();
	}
	
	public void clear()
	{
		logFile.clear();
	}
	
	private void debugOut(String text)
	{
		// System.out.println(text);
	}
	
	private void readItems()
	{
		String path = "";
		
		int itemTotal = logFile.configurationsAt("Items.Item").size();
		
		debugOut("ItemTotal : " + itemTotal);
		
		xAxisName = logFile.getString("Header." + "AxisLabels.AxisLabel(" + 0 + ").Name", "X");
		yAxisName = logFile.getString("Header." + "AxisLabels.AxisLabel(" + 1 + ").Name", "Y");
		
		int samplesPerItem = logFile.getInt("Header.SamplesPerItem");
		
		int matrixDim = (int) Math.sqrt((itemTotal) / samplesPerItem) + 1;
		
		debugOut("Samples Per Item : " + samplesPerItem);
		
		debugOut(matrixDim + "*" + matrixDim);
		
		System.out.println("Creating Values Container");
		values = new MapperValuesContainer(matrixDim, matrixDim, samplesPerItem);
		
		debugOut(xAxisName);
		debugOut(yAxisName);
		
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
						debugOut("Name " + item.getName());
						
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
									debugOut("Coordiantes " + coordiantes.getName());
									
									for(ConfigurationNode coordiante : coordiantes.getChildren())
									{
										debugOut("Field " + coordiante.getName());
										
										if(coordiante.getName().equals("Pos"))
										{
											coordinateXY[pos] = Integer.parseInt((String) coordiante.getValue());
											
											debugOut("Pos " + coordiante.getValue());
											
											pos++;
										}
										
										if(coordiante.getName().equals("Value"))
										{
											coordinateXYvals[vals] = Integer.parseInt((String) coordiante.getValue());
											debugOut("Value " + coordiante.getValue());
											
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
						
						debugOut("Item " + itemId);
						debugOut("X/Y " + coordinateXY[0] + "//" + coordinateXY[0]);
						debugOut("Vals " + coordinateXYvals[0] + "//" + coordinateXYvals[1]);
						debugOut("EndEvent " + endEvent);
						debugOut("StepCount " + stepCount);
						
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
		
		values.compute();
		
		xMapper = new TickValueMapper(values.getXMax(), xValMax);
		yMapper = new TickValueMapper(values.getYMax(), yValMax);
		
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
	
	public double[][] getAvgData()
	{
		return values.getAvgData();
	}
	
	public String[] getAxisNames()
	{
		return new String[]
		{
			xAxisName, yAxisName, zAxisName
		};
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
}
