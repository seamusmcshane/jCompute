package jCompute.Gui.View.Misc;

import java.awt.Color;

public class Pallete
{
	public static int[] HUEPalete(boolean rgba)
	{
		int pallete[] = new int[Constants.PALETTE_SIZE];
		
		// ARGB
		for(int i = 0; i < Constants.PALETTE_SIZE; i++)
		{
			pallete[i] = Color.HSBtoRGB((float) i / Constants.PALETTE_SIZE, 1f, 1f);
		}
		
		if(rgba)
		{
			for(int i = 0; i < Constants.PALETTE_SIZE; i++)
			{
				int val = pallete[i];
				pallete[i] = (val << 8) | ((val >> 24) & 0xFF);
			}
		}
		
		return pallete;
	}
	
}
