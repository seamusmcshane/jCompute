package tools.SurfaceChart;

import java.util.Random;

import tools.Common.AxisGrid;
import tools.SurfaceChart.Surface.BarGrid;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.decals.CameraGroupStrategy;
import com.badlogic.gdx.graphics.g3d.decals.Decal;
import com.badlogic.gdx.graphics.g3d.decals.DecalBatch;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

public class BarSurfaceOri
{
	private ModelBatch modelBatch;

	float trans;
	private float barSize;
	
	private int gridSteps;
	private int gridSize = 1000;

	private HueColorPallete pallete;

	private int columns;
	
	private DecalBatch db;
	private BitmapFont axisFont;
	
	private BarGrid grid;
	
	private Random r = new Random();
	
	// Data Limits (min/max)
	float xAxisMin;
	float xAxisMax;
	float yAxisMin;
	float yAxisMax;
	float zAxisMin;
	float zAxisMax;
		
	private AxisGrid axisGrid;
	
	public BarSurfaceOri(Camera cam, int samples, HueColorPallete pallete)
	{		
		modelBatch = new ModelBatch();
		
		// Labels		
		db = new DecalBatch(new CameraGroupStrategy(cam));
		axisFont = new BitmapFont();
		
		this.columns = (int) Math.sqrt(samples);
		this.barSize = gridSize/columns;

		// 0 + 1 for top/bottom lines
		this.gridSteps = 11;
		
		this.pallete = pallete;
		
		grid = new BarGrid(gridSize,samples,pallete);
				
		axisGrid = new AxisGrid(gridSize,gridSize/2,0.5f);
		
		//axisGrid.setAxisLabels(axisLabels);
		axisGrid.setTickIntervals(5);
		//axisGrid.setMinMax(minMax);
		axisGrid.setLabelSize(2f);
		axisGrid.setZAxisEnabled(false);
		
		//axisGrid.translate(0, 0, 500);
		
		setData(cam,50,100,50,200,null);
		
		axisGrid.update();

	}
	
	public void setData(Camera cam, float xAxisMin,float xAxisMax,float yAxisMin,float yAxisMax,float data[][])
	{
		// Data X/Y Axis Limits
		this.xAxisMin = xAxisMin;
		this.xAxisMax = xAxisMax;
		this.yAxisMin = yAxisMin;
		this.yAxisMax = yAxisMax;
		
		// Pillars
		ModelBuilder modelBuilder = new ModelBuilder();

		float max = columns*columns;
		//float max = barSize*columns/2;
		trans = columns*barSize/2;

		float height = 0;
		
		int c=0;
		for(int y=0;y<columns;y++)
		{
			for(int x=0;x<columns;x++)
			{
				height = r.nextFloat()*(100f);
				//height = 0.1f;
				//height= (((x+1)*(y+1))/max)*100f;
				if(height==0)
				{
					height=0.001f;
				}

				grid.setBarHeight(x, y, height);
				//pillars[y][x].dispose();
			}
		}
		
		grid.setBarHeight(0, 0, 0f);
		

		
	}
	
	public float getSize()
	{
		return columns * barSize;
	}
	
	public void render(Camera cam,Environment environment)
	{
		/*float max = barSize*columns/2;		

		for(int y=0;y<columns;y++)
		{
			for(int x=0;x<columns;x++)
			{
				if(r.nextInt(100) % 100 == 0)
				{
					//float height = grid.getBarHeight(x, y);
					
					//height = (height+1f)%100f;
					
					grid.setBarHeight(x, y, (100f/max)*r.nextFloat()*max);
					
					//grid.setBarHeight(x, y, height);
				}
			}
		}*/
		
		modelBatch.begin(cam);
		
		axisGrid.render(cam, modelBatch,db, environment);
		
		grid.render(modelBatch, cam, environment);
				
		/*Vector3 pos1 = new Vector3();
		gridXAxisInstances1[gridSteps/2].transform.getTranslation(pos1);
		
		Vector3 pos2 = new Vector3();
		gridXAxisInstances2[gridSteps/2].transform.getTranslation(pos2);
		
		Vector3 pos3 = new Vector3();
		gridXAxisInstances3[gridSteps/2].transform.getTranslation(pos3);
		
		Vector3 pos4 = new Vector3();
		gridXAxisInstances4[gridSteps/2].transform.getTranslation(pos4);
				
		float east = cam.position.dst2(pos1);
		float west = cam.position.dst2(pos2);
		float north = cam.position.dst2(pos3);
		float south = cam.position.dst2(pos4);*/

				
		modelBatch.end();
				
		db.flush();
	}
	
	public void drawGridInstances(ModelInstance instances[], Camera cam,Environment environment)
	{
		for(int i=0;i<instances.length;i++)
		{
			modelBatch.render(instances[i], environment);		
		}
	}
	
	public void dispose()
	{

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
