package alifeSim.Scenario.Math.Mandelbrot.Lib;
import java.awt.Color;

public class MandelbrotPallete
{
	public static int[] HUEPalete(boolean rgba)
	{
		int pallete[] = new int[MandelbrotConstants.PALETTE_SIZE];

		// ARGB
		for (int i = 0; i < MandelbrotConstants.PALETTE_SIZE; i++)
		{
			pallete[i] = Color.HSBtoRGB((float) i / MandelbrotConstants.PALETTE_SIZE, 1f, 1f);
		}
		
		if(rgba)
		{
			for (int i = 0; i < MandelbrotConstants.PALETTE_SIZE; i++)
			{
				int val = pallete[i];
				pallete[i] = (val << 8) | ((val >> 24) & 0xFF);
			}
		}
		
		return pallete;
	}

}
