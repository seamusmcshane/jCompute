package tools.SurfaceChart.Surface;

import jCompute.Gui.View.Lib3D.AxisGrid;

import java.util.Random;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;

public class BarSurface
{
	private ModelBatch modelBatch;
	
	float trans;
	private float barSize;
	
	private int gridSize = 1000;
	
	private int columns;
	
	private DecalBatch db;
	
	private BarGrid barGrid;
	
	private Random r = new Random();
	
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
	
	private void createBarSurfaceObjects(int samples, float xAxisMin, float xAxisMax, float yAxisMin, float yAxisMax,
			float zAxisMin, float zAxisMax, String[] axisNames)
	{
		this.columns = (int) Math.sqrt(samples);
		this.barSize = gridSize / columns;
		
		barGrid = new BarGrid(gridSize, samples, palette);
		
		axisGrid = new AxisGrid(gridSize, gridSize / 2, 0.5f);
		
		axisGrid.setTickIntervals(5);
		axisGrid.setLabelSize(2f);
		axisGrid.setFloorGridDisplayed(false);
		axisGrid.setZAxisDisplayed(false);
		axisGrid.setMinMaxDisplayed(false);
		
		axisGrid.setTickIntervals(4);
		
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
	
	public void setData(double xAxisMin, double xAxisMax, double yAxisMin, double yAxisMax, double zAxisMin,
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
		int samples = (data.length - 1) * (data[0].length - 1);
		
		createBarSurfaceObjects(samples, (float) xAxisMin, (float) xAxisMax, (float) yAxisMin, (float) yAxisMax,
				(float) zAxisMin, (float) zAxisMax, axisNames);
		
		// Data X/Y Axis Limits
		this.xAxisMin = (float) xAxisMin;
		this.xAxisMax = (float) xAxisMax;
		this.yAxisMin = (float) yAxisMin;
		this.yAxisMax = (float) yAxisMax;
		
		trans = columns * barSize / 2;
		
		float height = 0;
		
		float valM = (float) (100f / zAxisMax);
		
		int xMax = data.length - 1;
		int yMax = data[0].length - 1;
		
		System.out.println("vaM " + valM);
		
		for(int y = 0; y < yMax; y++)
		{
			for(int x = 0; x < xMax; x++)
			{
				// height = r.nextFloat() * (100f);
				height = (float) (valM * data[x][y]);
				
				System.out.println(x + "x" + y + " : " + data[y][x] + " > " + height);
				
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
