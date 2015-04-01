package jCompute.Gui.View.Misc;

import java.awt.Color;

public class Palette
{
	public static final int PALETTE_SIZE = 256;

	public static int[] HUEPalete(boolean rgba)
	{
		int pallete[] = new int[PALETTE_SIZE];
		
		// ARGB
		for(int i = 0; i < PALETTE_SIZE; i++)
		{
			pallete[i] = Color.HSBtoRGB((float) i / PALETTE_SIZE, 1f, 1f);
		}
		
		if(rgba)
		{
			for(int i = 0; i < PALETTE_SIZE; i++)
			{
				int val = pallete[i];
				pallete[i] = (val << 8) | ((val >> 24) & 0xFF);
			}
		}
		
		return pallete;
	}
	
	public static int[] GreyScale(boolean rgba)
	{
		int pallete[] = new int[PALETTE_SIZE];
		
		// ARGB
		for(int i = 0; i < PALETTE_SIZE; i++)
		{
			pallete[i] = new Color(i,i,i,255).getRGB();
		}
		
		if(rgba)
		{
			for(int i = 0; i < PALETTE_SIZE; i++)
			{
				int val = pallete[i];
				pallete[i] = (val << 8) | ((val >> 24) & 0xFF);
			}
		}
		
		return pallete;
	}
	
}
