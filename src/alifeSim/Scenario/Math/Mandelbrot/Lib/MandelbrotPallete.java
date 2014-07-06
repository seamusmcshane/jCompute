package alifeSim.Scenario.Math.Mandelbrot.Lib;
import java.awt.Color;

public class MandelbrotPallete
{
	public static int[] HUEPalete()
	{
		int pallete[] = new int[MandelbrotConstants.PALETTE_SIZE];

		for (int i = 0; i < MandelbrotConstants.PALETTE_SIZE; i++)
		{
			pallete[i] = Color.HSBtoRGB((float) i / MandelbrotConstants.PALETTE_SIZE, 1f, 1f);
		}
		
		return pallete;
	}

}
