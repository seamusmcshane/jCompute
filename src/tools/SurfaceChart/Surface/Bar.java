package tools.SurfaceChart.Surface;

import tools.SurfaceChart.HueColorPallete;

import com.badlogic.gdx.graphics.g3d.ModelInstance;

public class Bar
{
	private float barSize;
	
	private ModelInstance bar;
	
	private HueColorPallete pallete;

	public Bar(float barSize,HueColorPallete pallete)
	{
		this.pallete = pallete;
	}
}
