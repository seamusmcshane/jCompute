package tools.SurfaceChart.Surface;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

public class BarGrid
{
	private Bar gridBars[][];
	private int columns;
	
	public BarGrid(float gridSize, int samples, int[] pallete)
	{
		ModelBuilder modelBuilder = new ModelBuilder();
		this.columns = (int) Math.sqrt(samples);
		
		float barSize = gridSize / columns;
		float trans = columns * barSize / 2;
		
		gridBars = new Bar[columns][columns];
		for(int y = 0; y < columns; y++)
		{
			for(int x = 0; x < columns; x++)
			{
				gridBars[y][x] = new Bar(modelBuilder, trans, barSize, pallete);
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
		for(int y = 0; y < columns; y++)
		{
			for(int x = 0; x < columns; x++)
			{
				modelBatch.render(gridBars[y][x].getInstance(), environment);
			}
		}
	}
	
	public void dispose()
	{
		for(int y = 0; y < columns; y++)
		{
			for(int x = 0; x < columns; x++)
			{
				gridBars[y][x].dispose();
			}
		}
	}
	
}
