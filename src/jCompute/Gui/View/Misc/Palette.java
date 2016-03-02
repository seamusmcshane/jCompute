package jCompute.Gui.View.Misc;

import java.awt.Color;

import jCompute.util.JCMath;

public class Palette
{
	private static final int RED = 0;
	private static final int GREEN = 1;
	private static final int BLUE = 2;
	
	private static final int RGBCOLORBITS = 8;
	private static final int RGBCOLORDEPTH = (int) Math.pow(2, RGBCOLORBITS) - 1;
	
	private static float SPECTURM_RANGE_MIN = 380;
	private static float SPECTURM_RANGE_MAX = 779.999f;
	
	public static int[] GeneratePaletteFromRGBColors(float[][] floatRGB, int paletteSize, float gamma)
	{
		System.out.println("floatRGB " + floatRGB.length);
		System.out.println("floatRGB[] " + floatRGB[0].length);
		
		float[][] xyz = new float[floatRGB.length][3];
		for(int i = 0; i < floatRGB.length; i++)
		{
			xyz[i] = CIERGB.RGBFloatToXYZ(floatRGB[i][0], floatRGB[i][1], floatRGB[i][2]);
		}
		
		float[][] lab = new float[floatRGB.length][3];
		for(int i = 0; i < floatRGB.length; i++)
		{
			lab[i] = CIERGB.xyzToLAB1976(xyz[i]);
		}
		
		float stepSize = (float) paletteSize / (float) (floatRGB.length - 1);
		float modStepSize = (float) Math.ceil(stepSize);
		
		System.out.println("stepSize " + stepSize);
		System.out.println("modStepSize " + modStepSize);
		
		int[] palette = new int[paletteSize];
		
		// palette[0] = floatArrtoPackedInt(CIERGB.XYZtoRGB(CIERGB.lab1976ToXYZ(lab[0])));
		
		float pP = 0;
		int pS = -1;
		int pE = 0;
		
		for(int p = 0; p < paletteSize; p++)
		{
			float labFloatInterpolated[] = new float[3];
			
			pP = ((float) p / (float) modStepSize) % 1;
			
			if(p % modStepSize == 0)
			{
				pS += 1;
				pE += 1;
			}
			
			System.out.println("p " + p);
			System.out.println("pS " + pS);
			System.out.println("pE " + pE);
			System.out.println("pP " + pP);
			
			labFloatInterpolated[0] = interpolate(lab[pS][0], lab[pE][0], pP);
			labFloatInterpolated[1] = interpolate(lab[pS][1], lab[pE][1], pP);
			labFloatInterpolated[2] = interpolate(lab[pS][2], lab[pE][2], pP);
			
			palette[p] = floatArrtoPackedInt(CIERGB.XYZtoRGB(CIERGB.lab1976ToXYZ(labFloatInterpolated)));
			
			// pP = (((p % stepSize) / (float) paletteSize) * stepSize);
			// pP = ((float) (p*(paletteSize/modStepSize)) / (float) paletteSize) % 1;
		}
		
		return palette;
	}
	
	private static int floatArrtoPackedInt(float[] value)
	{
		int red = (int) (value[0] * 255f);
		int green = (int) (value[1] * 255f);
		int blue = (int) (value[2] * 255f);
		
		return (255 << 24) | (red << 16) | (green << 8) | blue;
	}
	
	private static float interpolate(float start, float end, float percentage)
	{
		return (end * percentage) + (start * (1 - percentage));
	}
	
	public static int[] HUEPalette(boolean rgba, int paletteSize, float gamma)
	{
		int palette[] = new int[paletteSize];
		
		// ARGB
		for(int i = 0; i < paletteSize; i++)
		{
			palette[i] = Color.HSBtoRGB((float) i / paletteSize, 1f, 1f);
		}
		
		applySRGB(palette, paletteSize);
		
		applyGamma(palette, paletteSize, gamma);
		
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
	
	public static int[] SpectrumPalette(boolean rgba, int paletteSize, float gamma)
	{
		return SpectrumPalette(rgba, paletteSize, gamma, 440, SPECTURM_RANGE_MAX);
	}
	
	public static int[] SpectrumPalette(boolean rgba, int paletteSize, float gamma, float specMin, float specMax)
	{
		int palette[] = new int[paletteSize];
		
		float min = specMin;
		float range = specMax - min;
		
		float step = ((float) range / (float) paletteSize);
		
		// ARGB
		for(int i = 0; i < paletteSize; i++)
		{
			float[] rgb = waveLengthToRGB(min + (i * step));
			
			rgb = CIERGB.XYZtoRGB(CIERGB.capLumenanceXYZ(CIERGB.RGBFloatToXYZ(rgb[0], rgb[1], rgb[2]), 0.8f));
			
			rgb[0] = CIERGB.lStar(rgb[0]);
			rgb[1] = CIERGB.lStar(rgb[1]);
			rgb[2] = CIERGB.lStar(rgb[2]);
			
			int red = (int) (rgb[RED] * RGBCOLORDEPTH);
			int green = (int) (rgb[GREEN] * RGBCOLORDEPTH);
			int blue = (int) (rgb[BLUE] * RGBCOLORDEPTH);
			
			// ARGB
			palette[i] = (255 << 24) | (red << 16) | (green << 8) | blue;
		}
		
		// applySRGB(palette, paletteSize);
		
		// applyGamma(palette, paletteSize, 2.2f);
		
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
	
	public static int[] SpectrumPaletteWrapped(boolean rgba, int paletteSize, float gamma)
	{
		int palette[] = new int[paletteSize];
		
		float min = 450;
		float range = 730 - min;
		
		float step = ((float) range / (float) paletteSize * 2);
		
		// ARGB
		for(int i = 0; i <= paletteSize / 2; i++)
		{
			float[] rgb = waveLengthToRGB(min + (i * step));
			
			int red = (int) (rgb[RED] * RGBCOLORDEPTH);
			int green = (int) (rgb[GREEN] * RGBCOLORDEPTH);
			int blue = (int) (rgb[BLUE] * RGBCOLORDEPTH);
			
			// ARGB
			palette[i] = (255 << 24) | (red << 16) | (green << 8) | blue;
		}
		
		int s = 0;
		for(int i = paletteSize - 1; i > paletteSize / 2; i--)
		{
			float[] rgb = waveLengthToRGB(min + (s * step));
			
			int red = (int) (rgb[RED] * RGBCOLORDEPTH);
			int green = (int) (rgb[GREEN] * RGBCOLORDEPTH);
			int blue = (int) (rgb[BLUE] * RGBCOLORDEPTH);
			
			// ARGB
			palette[i] = (255 << 24) | (red << 16) | (green << 8) | blue;
			s++;
		}
		
		applySRGB(palette, paletteSize);
		
		applyGamma(palette, paletteSize, gamma);
		
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
	
	private static float[] WavelengthToFloatRGB_OLD(float wavelength)
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
			rgb[RED] = -(wavelength - SPECTURM_RANGE_MAX) / (SPECTURM_RANGE_MAX - 620);
			rgb[GREEN] = 0;
			rgb[BLUE] = 0;
		}
		
		return rgb;
	}
	
	/*
	 * Adapted from FORTAN code available from.
	 * http://www.physics.sfasu.edu/astro/color/spectra.html
	 */
	public static float[] waveLengthToRGB(float waveLength)
	{
		float[] rgb = new float[3];
		
		if(waveLength >= 380f && waveLength < 440f)
		{
			rgb[RED] = -(waveLength - 440f) / (440f - 380f);
			rgb[GREEN] = 0;
			rgb[BLUE] = 1;
		}
		else if(waveLength >= 440 && waveLength < 490)
		{
			rgb[RED] = 0;
			rgb[GREEN] = (waveLength - 440f) / (490f - 440f);
			rgb[BLUE] = 1;
		}
		else if(waveLength >= 490 && waveLength < 510)
		{
			rgb[RED] = 0;
			rgb[GREEN] = 1;
			rgb[BLUE] = -(waveLength - 510f) / (510f - 490f);
		}
		else if(waveLength >= 510 && waveLength < 580)
		{
			rgb[RED] = (waveLength - 510f) / (580f - 510f);
			rgb[GREEN] = 1;
			rgb[BLUE] = 0;
		}
		else if(waveLength >= 580f && waveLength < 645f)
		{
			rgb[RED] = 1;
			rgb[GREEN] = -(waveLength - 645f) / (645f - 580f);
			rgb[BLUE] = 0;
		}
		else if(waveLength >= 645f && waveLength < 780f)
		{
			rgb[RED] = 1;
			rgb[GREEN] = 0;
			rgb[BLUE] = 0;
		}
		else
		{
			rgb[RED] = 0;
			rgb[GREEN] = 0;
			rgb[BLUE] = 0;
		}
		
		// Intensity
		float intensity = 1f;
		if(waveLength >= 645f)
		{
			intensity = 0.01f + (0.99f * (780f - waveLength) / (780f - 645f));
		}
		else if(waveLength <= 420f)
		{
			intensity = 0.01f + (0.99f * (waveLength - 380f) / (420f - 380f));
		}
		
		rgb[RED] = rgb[RED] * intensity;
		rgb[GREEN] = rgb[GREEN] * intensity;
		rgb[BLUE] = rgb[BLUE] * intensity;
		
		return rgb;
	}
	
	public static int[] GreyScale(boolean rgba, int paletteSize, float gamma)
	{
		int palette[] = new int[paletteSize];
		
		float ci = 1f / paletteSize;
		
		// ARGB
		for(int i = 0; i < paletteSize; i++)
		{
			palette[i] = new Color(i * ci, i * ci, i * ci, 1f).getRGB();
		}
		
		applySRGB(palette, paletteSize);
		
		applyGamma(palette, paletteSize, gamma);
		
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
	
	public static int[] BlackToColor(float red, float green, float blue, boolean rgba, int paletteSize, float gamma)
	{
		int palette[] = new int[paletteSize];
		
		float scale = 1f / paletteSize;
		
		// ARGB
		for(int i = 0; i < paletteSize; i++)
		{
			palette[i] = new Color(red * (scale * i), green * (scale * i), blue * (scale * i), 1f).getRGB();
		}
		
		applySRGB(palette, paletteSize);
		
		applyGamma(palette, paletteSize, gamma);
		
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
	
	private static float getLuma(int red, int green, int blue)
	{
		float r = 1f / (float) red;
		float g = 1f / (float) green;
		float b = 1f / (float) blue;
		
		return 0.2126f * r + 0.7152f * g + 0.0722f * b;
	}
	
	private static float getLuma(float red, float green, float blue)
	{
		float r = 1f / (float) red;
		float g = 1f / (float) green;
		float b = 1f / (float) blue;
		
		return 0.2126f * r + 0.7152f * g + 0.0722f * b;
	}
	
	private static int[] applySRGB(int[] palette, int paletteSize)
	{
		for(int i = 0; i < paletteSize; i++)
		{
			int red = (palette[i] >> 16) & 0xFF;
			int green = (palette[i] >> 8) & 0xFF;
			int blue = (palette[i] & 0xFF);
			
			red = toSRGB(red);
			green = toSRGB(green);
			blue = toSRGB(blue);
			
			palette[i] = (255 << 24) | (red << 16) | (green << 8) | blue;
		}
		
		return palette;
	}
	
	private static int toSRGB(int cval)
	{
		float fval = cval / 255f;
		
		if(fval <= 0.0031308f)
		{
			fval = fval * 12.92f;
		}
		else
		{
			fval = (float) (Math.pow(1.055f * fval, 1f / 2.4f) - 0.055f);
		}
		
		return (int) (fval * 255f);
	}
	
	private static int[] applyGamma(int[] palette, int paletteSize, float gamma)
	{
		if(gamma == 1)
		{
			return palette;
		}
		
		float newGamma = 1f / gamma;
		
		for(int i = 0; i < paletteSize; i++)
		{
			int red = (palette[i] >> 16) & 0xFF;
			int green = (palette[i] >> 8) & 0xFF;
			int blue = (palette[i] & 0xFF);
			
			red = (int) (255 * (Math.pow((float) red / 255f, newGamma)));
			green = (int) (255 * (Math.pow((float) green / 255f, newGamma)));
			blue = (int) (255 * (Math.pow((float) blue / 255f, newGamma)));
			
			palette[i] = (255 << 24) | (red << 16) | (green << 8) | blue;
		}
		
		return palette;
	}
	
	public static int[] CIESpectrumPalette(boolean rgba, int paletteSize, float gamma)
	{
		return CIESpectrumPalette(rgba, paletteSize, gamma, 450, 700);
	}
	
	public static int[] CIESpectrumPalette(boolean rgba, int paletteSize, float gamma, float specMin, float specMax)
	{
		int palette[] = new int[paletteSize];
		
		float min = specMin;
		float range = specMax - min;
		
		float step = ((float) range / (float) paletteSize);
		
		// ARGB
		for(int i = 0; i < paletteSize; i++)
		{
			float[] rgb = CIERGB.WavelengthToSRGB(min + ((float) i * (float) step));
			
			rgb = CIERGB.XYZtoRGB(CIERGB.scaleLumenanceXYZ(CIERGB.RGBFloatToXYZ(rgb[0], rgb[1], rgb[2]), 0.9f));
			
			rgb[0] = CIERGB.lStar(rgb[0]);
			rgb[1] = CIERGB.lStar(rgb[1]);
			rgb[2] = CIERGB.lStar(rgb[2]);
			
			int red = (int) (rgb[RED] * RGBCOLORDEPTH);
			int green = (int) (rgb[GREEN] * RGBCOLORDEPTH);
			int blue = (int) (rgb[BLUE] * RGBCOLORDEPTH);
			
			// ARGB
			palette[i] = (255 << 24) | (red << 16) | (green << 8) | blue;
		}
		
		// applySRGB(palette, paletteSize);
		
		// applyGamma(palette, paletteSize, gamma);
		
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
	
	public static String RGBtoString(float[] rgb)
	{
		return new String(rgb[0] + "," + rgb[1] + "," + rgb[2]);
	}
	
	public static String RGBtoString(double[] rgb)
	{
		return new String(rgb[0] + "," + rgb[1] + "," + rgb[2]);
	}
	
	public static int[] LabSpecturmPalette(boolean rgba, int paletteSize, float gamma)
	{
		int palette[] = new int[paletteSize];
		
		float hBase = 270f;
		float hUpper = 0;
		
		float hStep = (hUpper - hBase) / (float) (paletteSize);
		
		float lUpper = 100f;
		float lStep = lUpper / (float) (paletteSize);
		
		float hval = hBase;
		float iVal = 1;
		
		// ARGB
		for(int i = 0; i < paletteSize; i++)
		{
			// Adjust vals
			hval = ((float) i * hStep) + hBase;
			iVal = ((float) i+1 * lStep); // +1
			
			float[] lab = CIERGB.labCHtoLAB(iVal, lUpper - iVal, hval);
			
			//System.out.println("hval : " + hval);
			//System.out.println("iVal : " + iVal);
			//System.out.println("L : " + lab[0]);
			//System.out.println("C : " + lab[1]);
			//System.out.println("H : " + lab[2]);
			
			float[] rgb = CIERGB.XYZtoRGB(CIERGB.lab1976ToXYZ(lab));
			
			// rgb[0] = CIERGB.lStar(rgb[0] );
			// rgb[1] = CIERGB.lStar(rgb[1] );
			// rgb[2] = CIERGB.lStar(rgb[2] );
			
			int red = (int) (rgb[RED] * RGBCOLORDEPTH);
			int green = (int) (rgb[GREEN] * RGBCOLORDEPTH);
			int blue = (int) (rgb[BLUE] * RGBCOLORDEPTH);
			
			// ARGB
			palette[i] = (255 << 24) | (red << 16) | (green << 8) | blue;
		}
		
		applySRGB(palette, paletteSize);
		
		// applyGamma(palette, paletteSize, gamma);
		
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
