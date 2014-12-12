package tools.SurfaceChart.Surface;

import tools.SurfaceChart.HueColorPallete;

import com.badlogic.gdx.graphics.g3d.ModelInstance;

public class BarGrid
{
	private Bar gridBars[][];
	private int columns;
	
	public BarGrid(float gridSize, int samples,HueColorPallete pallete)
	{
		this.columns = (int) Math.sqrt(samples);
		
		gridBars = new Bar[columns][columns];
		
		for(int y=0;y<columns;y++)
		{
			for(int x=0;x<columns;x++)
			{
				gridBars[y][x] = new Bar(gridSize/columns,pallete);
			}
		}
		
	}
	
}
