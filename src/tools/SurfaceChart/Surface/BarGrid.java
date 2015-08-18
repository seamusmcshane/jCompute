package tools.SurfaceChart.Surface;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

public class BarGrid
{
	private Bar gridBars[][];
	private int xColumns;
	private int yColumns;
	
	public BarGrid(float gridSize, int xColumns, int yColumns, int[] pallete)
	{
		ModelBuilder modelBuilder = new ModelBuilder();
		
		this.xColumns = xColumns;
		this.yColumns = yColumns;
		
		float barWidth = gridSize / xColumns;
		float barDepth = gridSize / yColumns;
		
		float xTrans = xColumns * barWidth / 2;
		float yTrans = yColumns * barDepth / 2;
		
		gridBars = new Bar[yColumns][xColumns];
		for(int y = 0; y < yColumns; y++)
		{
			for(int x = 0; x < xColumns; x++)
			{
				gridBars[y][x] = new Bar(modelBuilder, xTrans, barWidth, yTrans, barDepth, pallete);
				gridBars[y][x].setBarLocation(x, y);
			}
		}
	}
	
	public void setBarHeight(int x, int y, float percentage)
	{
		gridBars[y][x].setHeight(percentage);
	}
	
	public float getBarHeight(int x, int y)
	{
		return gridBars[y][x].getHeight();
	}
	
	public void render(ModelBatch modelBatch, Camera cam, Environment environment)
	{
		for(int y = 0; y < yColumns; y++)
		{
			for(int x = 0; x < xColumns; x++)
			{
				modelBatch.render(gridBars[y][x].getInstance(), environment);
			}
		}
	}
	
	public void dispose()
	{
		for(int y = 0; y < yColumns; y++)
		{
			for(int x = 0; x < xColumns; x++)
			{
				gridBars[y][x].dispose();
			}
		}
	}
	
}
