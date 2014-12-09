package tools.SurfaceChart;

import com.badlogic.gdx.graphics.Color;

public class HueColorPallete
{
	private Color colors[];
	
	public HueColorPallete(int number)
	{
		colors = new Color[number];
		
		float fTi = 1f/255f;
		float range = 1f/number;
		
		for(int i=0;i<number;i++)
		{
			java.awt.Color temp = new java.awt.Color(java.awt.Color.HSBtoRGB(range*i,1f,1f));
			colors[i] = new Color(fTi*temp.getRed(),fTi*temp.getGreen(),fTi*temp.getBlue(),1f) ;
		}
	}
	
	public Color getColor(int c)
	{
		return colors[c];
	}

	public float getColorCount()
	{
		return colors.length;
	}

}
