package jCompute.Gui.View.Misc;

import java.awt.Color;

public class Palette
{
	public static int[] HUEPalete(boolean rgba, int palleteSize)
	{
		int pallete[] = new int[palleteSize];
		
		// ARGB
		for(int i = 0; i < palleteSize; i++)
		{
			pallete[i] = Color.HSBtoRGB((float) i / palleteSize, 1f, 1f);
		}
		
		if(rgba)
		{
			for(int i = 0; i < palleteSize; i++)
			{
				int val = pallete[i];
				pallete[i] = (val << 8) | ((val >> 24) & 0xFF);
			}
		}
		
		return pallete;
	}
	
	public static int[] GreyScale(boolean rgba, int palleteSize)
	{
		int pallete[] = new int[palleteSize];
		
		// ARGB
		for(int i = 0; i < palleteSize; i++)
		{
			pallete[i] = new Color(i,i,i,255).getRGB();
		}
		
		if(rgba)
		{
			for(int i = 0; i < palleteSize; i++)
			{
				int val = pallete[i];
				pallete[i] = (val << 8) | ((val >> 24) & 0xFF);
			}
		}
		
		return pallete;
	}
	
}
