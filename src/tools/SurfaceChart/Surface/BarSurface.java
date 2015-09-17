package tools.SurfaceChart.Surface;

import jCompute.Gui.View.Lib3D.AxisGrid;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;

public class BarSurface
{
	private ModelBatch modelBatch;
	
	private float barSize;
	
	private int gridSize = 1000;
	
	private int columns;
	
	private DecalBatch db;
	
	private BarGrid barGrid;
	
	// Data Limits (min/max)
	float xAxisMin;
	float xAxisMax;
	float yAxisMin;
	float yAxisMax;
	float zAxisMin;
	float zAxisMax;
	
	private AxisGrid axisGrid;
	
	private int[] palette;
	
	public BarSurface(Camera cam, int[] palette)
	{
		modelBatch = new ModelBatch();
		
		// Labels
		db = new DecalBatch(new CameraGroupStrategy(cam));
		
		this.palette = palette;
		
		// setData(cam, 50, 100, 50, 200, null);
	}
	
	private void createBarSurfaceObjects(int xSteps, float xAxisMin, float xAxisMax, int ySteps, float yAxisMin, float yAxisMax,
			float zAxisMin, float zAxisMax, String[] axisNames)
	{
		this.columns = (int) Math.sqrt(xSteps * ySteps);
		this.barSize = gridSize / columns;
		
		barGrid = new BarGrid(gridSize, ySteps, xSteps, palette);
		
		axisGrid = new AxisGrid(gridSize, gridSize / 2, 0.5f);
		
		int tickX = xSteps-1;
		int tickY = ySteps-1;
		int tickZ = 5;
		
		int maxTicks = 10;
		
		if(tickX > maxTicks)
		{
			for(int i=maxTicks;i>0;i--)
			{
				if(tickX % i == 0)
				{
					tickX = i;
					break;
				}
			}
		}
		
		if(tickY > maxTicks)
		{
			for(int i=maxTicks;i>0;i--)
			{
					if(tickY % i == 0)
					{
						tickY = i;
						break;
					}
			}
		}
		
		System.out.println("tickX : " + tickX);
		System.out.println("tickY : " + tickY);
		
		axisGrid.setTickIntervals(tickX,tickY,tickZ);
		axisGrid.setLabelSize(2f);
		axisGrid.setFloorGridDisplayed(false);
		axisGrid.setZAxisDisplayed(false);
		axisGrid.setMinMaxDisplayed(false);
		
		float[][] minMax = new float[3][2];
		minMax[0][0] = xAxisMin;
		minMax[0][1] = xAxisMax;
		minMax[1][0] = yAxisMin;
		minMax[1][1] = yAxisMax;
		minMax[2][0] = zAxisMin;
		minMax[2][1] = zAxisMax;
		
		axisGrid.setAxisRangeMinMax(minMax);
		axisGrid.setValueMinMax(0, 100, 0, 100, 0, 100);
		axisGrid.setLabelSize(2f);
		
		// Axis Names
		axisGrid.setAxisLabels(axisNames);
		axisGrid.setFirstLast(null);
		axisGrid.update();
	}
	
	public void setData(int xSteps, double xAxisMin, double xAxisMax, int ySteps, double yAxisMin, double yAxisMax, double zAxisMin,
			double zAxisMax, double data[][], String[] axisNames)
	{
		if(data == null)
		{
			return;
		}
		
		System.out.println("Data len " + data.length);
		
		if(barGrid != null)
		{
			// axisGrid.dispose();
			barGrid.dispose();
		}
		
		// Square
		// int samples = (data.length - 1) * (data[0].length - 1);
		createBarSurfaceObjects(xSteps, (float) xAxisMin, (float) xAxisMax, ySteps, (float) yAxisMin, (float) yAxisMax, (float) zAxisMin,
				(float) zAxisMax, axisNames);
				
		// Data X/Y Axis Limits
		this.xAxisMin = (float) xAxisMin;
		this.xAxisMax = (float) xAxisMax;
		this.yAxisMin = (float) yAxisMin;
		this.yAxisMax = (float) yAxisMax;
		
		float height = 0;
		
		float valM = (float) (100f / zAxisMax);
		
		System.out.println("vaM " + valM);
		
		// Random r = new Random();
		
		for(int y = 0; y < ySteps; y++)
		{
			for(int x = 0; x < xSteps; x++)
			{
				// height = r.nextFloat() * (100f);
				height = (float) (valM * data[x][y]);
				
				System.out.println(x + "x" + y + " : " + data[x][y] + " > " + height);
				
				barGrid.setBarHeight(y, x, height);
			}
		}
	}
	
	public float getSize()
	{
		return columns * barSize;
	}
	
	public void render(Camera cam, Environment environment)
	{
		if(axisGrid != null)
		{
			axisGrid.render(cam, modelBatch, db, environment);
		}
		
		if(barGrid != null)
		{
			modelBatch.begin(cam);
			barGrid.render(modelBatch, cam, environment);
			modelBatch.end();
		}
		
		db.flush();
	}
	
	public float getXmin()
	{
		return xAxisMin;
	}
	
	public float getXmax()
	{
		return xAxisMax;
	}
	
	public float getYmin()
	{
		return yAxisMin;
	}
	
	public float getYmax()
	{
		return yAxisMax;
	}
	
	public float getZmin()
	{
		return zAxisMin;
	}
	
	public float getZmax()
	{
		return zAxisMax;
	}
	
}
