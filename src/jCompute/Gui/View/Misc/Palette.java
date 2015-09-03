package jCompute.Gui.View.Misc;

import java.awt.Color;

public class Palette
{
	private static final int RED = 0;
	private static final int GREEN = 1;
	private static final int BLUE = 2;
	
	private static final int RGBCOLORBITS = 8;
	private static final int RGBCOLORDEPTH = (int)Math.pow(2, RGBCOLORBITS)-1;
	
	private static int SPECTURM_RANGE_MIN = 380;
	private static int SPECTURM_RANGE_MAX = 750;
	private static int SPECTURM_RANGE= SPECTURM_RANGE_MAX-SPECTURM_RANGE_MIN;
	
	public static int[] HUEPalete(boolean rgba, int paletteSize)
	{
		int palette[] = new int[paletteSize];
		
		// ARGB
		for(int i = 0; i < paletteSize; i++)
		{
			palette[i] = Color.HSBtoRGB((float) i / paletteSize, 1f, 1f);
		}
		
		if(rgba)
		{
			for(int i = 0; i < paletteSize; i++)
			{
				int val = palette[i];
				palette[i] = (val << 8) | ((val >> 24) & 0xFF);
			}
		}
		
		return palette;
	}
	
	public static int[] SpectrumPalete(boolean rgba, int paletteSize)
	{
		int palette[] = new int[paletteSize];
		
		float min = 450;
		float range = SPECTURM_RANGE_MAX-min;
		
		float step = ((float)range/(float)paletteSize);

		// ARGB
		for(int i = 0; i < paletteSize; i++)
		{
			float[] rgb = WavelengthToFloatRGB(min+(i*step),100);
			int red = (int) (rgb[RED]*RGBCOLORDEPTH);
			int green = (int) (rgb[GREEN]*RGBCOLORDEPTH);
			int blue = (int) (rgb[BLUE]*RGBCOLORDEPTH);
			
			// ARGB
			palette[i] = (255 << 24 )| (red << 16) | (green << 8) | blue ;
		}
		
		if(rgba)
		{
			for(int i = 0; i < paletteSize; i++)
			{
				int val = palette[i];
				palette[i] = (val << 8) | ((val >> 24) & 0xFF);
			}
		}
		
		return palette;
	}
	
	public static int[] SpectrumPaleteWrapped(boolean rgba, int paletteSize)
	{
		int palette[] = new int[paletteSize];
		
		float min = 450;
		float range = SPECTURM_RANGE_MAX-min;
		
		float step = ((float)range/(float)paletteSize*2);

		// ARGB
		for(int i = 0; i < paletteSize/2; i++)
		{
			float[] rgb = WavelengthToFloatRGB(min+(i*step),0);
			int red = (int) (rgb[RED]*RGBCOLORDEPTH);
			int green = (int) (rgb[GREEN]*RGBCOLORDEPTH);
			int blue = (int) (rgb[BLUE]*RGBCOLORDEPTH);
			
			System.out.println("RGB : " + red + " "+ green + " "+blue);
			
			// ARGB
			palette[i] = (255 << 24 )| (red << 16) | (green << 8) | blue ;
		}
		
		int s=0;
		for(int i = paletteSize-1; i > paletteSize/2; i--)
		{
			float[] rgb = WavelengthToFloatRGB(min+(s*step),0);
			int red = (int) (rgb[RED]*RGBCOLORDEPTH);
			int green = (int) (rgb[GREEN]*RGBCOLORDEPTH);
			int blue = (int) (rgb[BLUE]*RGBCOLORDEPTH);
			
			System.out.println("RGB : " + red + " "+ green + " "+blue);
			
			// ARGB
			palette[i] = (255 << 24 )| (red << 16) | (green << 8) | blue ;
			s++;
		}
		
		if(rgba)
		{
			for(int i = 0; i < paletteSize; i++)
			{
				int val = palette[i];
				palette[i] = (val << 8) | ((val >> 24) & 0xFF);
			}
		}
		
		return palette;
	}

	public static float[] WavelengthToFloatRGB(float wavelength, int dropoff)
	{
		float[] rgb = new float[3];
		
		// Violet
		if((wavelength >= SPECTURM_RANGE_MIN) && (wavelength < 450))
		{
			rgb[RED] = -(wavelength - 450) / (450 - SPECTURM_RANGE_MIN);
			rgb[GREEN] = 0;
			rgb[BLUE] = 1;
		}
		// BLUE
		else if((wavelength >= 450) && (wavelength < 495))
		{
			rgb[RED] = 0;
			rgb[GREEN] = (wavelength - 450) / (495 - 450);
			rgb[BLUE] = 1;
		}
		// GREEN
		else if((wavelength >= 495) && (wavelength < 510))
		{
			rgb[RED] = 0;
			rgb[GREEN] = 1;
			rgb[BLUE] = -(wavelength - 510) / (510 - 495);
		}
		// YELLOW
		else if((wavelength >= 510) && (wavelength < 590))
		{
			rgb[RED] = (wavelength - 510) / (590 - 510);
			rgb[GREEN] = 1f;
			rgb[BLUE] = 0;
		}
		// ORANGE
		else if((wavelength >= 590) && (wavelength < 620))
		{
			rgb[RED] = 1;
			rgb[GREEN] = -(wavelength - 620) / (620 - 590);
			rgb[BLUE] = 0;
		}
		// RED
		else if((wavelength >= 620) && (wavelength < SPECTURM_RANGE_MAX))
		{
			// Drop-off - allows the red part of the spectrum to go into the black
			rgb[RED] = (SPECTURM_RANGE_MAX+dropoff - wavelength) / ( (SPECTURM_RANGE_MAX+dropoff) - 620);
			rgb[GREEN] = 0;
			rgb[BLUE] = 0;
		}
		
		return rgb;
	}
	
	public static int[] GreyScale(boolean rgba, int paletteSize)
	{
		int palette[] = new int[paletteSize];
		
		// ARGB
		for(int i = 0; i < paletteSize; i++)
		{
			palette[i] = new Color(i, i, i, 255).getRGB();
		}
		
		if(rgba)
		{
			for(int i = 0; i < paletteSize; i++)
			{
				int val = palette[i];
				palette[i] = (val << 8) | ((val >> 24) & 0xFF);
			}
		}
		
		return palette;
	}
	
	public static int[] BlackToColor(float red, float green, float blue, boolean rgba, int paletteSize)
	{
		int palette[] = new int[paletteSize];
		
		float scale = 1f / paletteSize;
		
		// ARGB
		for(int i = 0; i < paletteSize; i++)
		{
			palette[i] = new Color(red * (scale * i), green * (scale * i), blue * (scale * i), 1f).getRGB();
		}
		
		if(rgba)
		{
			for(int i = 0; i < paletteSize; i++)
			{
				int val = palette[i];
				palette[i] = (val << 8) | ((val >> 24) & 0xFF);
			}
		}
		
		return palette;
	}
	
}
