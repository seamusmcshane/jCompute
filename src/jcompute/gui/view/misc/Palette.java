package jcompute.gui.view.misc;

import java.awt.Color;

public class Palette
{
	private static final int RED = 0;
	private static final int GREEN = 1;
	private static final int BLUE = 2;
	
	private static final int RGBCOLORBITS = 8;
	private static final int RGBCOLORDEPTH = (int) Math.pow(2, RGBCOLORBITS) - 1;
	
	private static float SPECTURM_RANGE_MIN = 380;
	private static float SPECTURM_RANGE_MAX = 779.999f;
	
	private static final String[] paletteList = new String[]
	{
		"HUEPalette", "SpectrumPalette", "SpectrumPaletteWrapped", "RGBGreyScale", "LabSpecturmPalette", "LabGreyScale"
	};
	
	public String[] getPaletteList()
	{
		return paletteList;
	}
	
	public static int[] paletteFromPaletteName(String name, boolean rgba, int paletteSize, boolean applySRGB)
	{
		switch(name)
		{
			case "HUEPalette":
			{
				return HUEPalette(rgba, paletteSize, applySRGB);
			}
			case "SpectrumPalette":
			{
				return SpectrumPalette(rgba, paletteSize, applySRGB);
			}
			case "SpectrumPaletteWrapped":
			{
				return SpectrumPaletteWrapped(rgba, paletteSize, applySRGB);
			}
			case "RGBGreyScale":
			{
				return RGBGreyScale(rgba, paletteSize, applySRGB);
			}
			case "LabSpecturmPalette":
			{
				return LabSpecturmPalette(rgba, paletteSize, applySRGB);
			}
			case "LabGreyScale":
			{
				return LabGreyScale(rgba, paletteSize, applySRGB);
			}
		}
		
		return null;
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
	
	public static int[] HUEPalette(boolean rgba, int paletteSize, boolean applySRGB)
	{
		int palette[] = new int[paletteSize];
		
		// ARGB
		for(int i = 0; i < paletteSize; i++)
		{
			palette[i] = Color.HSBtoRGB((float) i / paletteSize, 1f, 1f);
			
			if(applySRGB)
			{
				palette[i] = CIERGB.addSRGB(palette[i]);
			}
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
	
	public static int[] SpectrumPalette(boolean rgba, int paletteSize, boolean applySRGB)
	{
		return SpectrumPalette(rgba, paletteSize, applySRGB, 440, SPECTURM_RANGE_MAX);
	}
	
	public static int[] SpectrumPalette(boolean rgba, int paletteSize, boolean applySRGB, float specMin, float specMax)
	{
		int palette[] = new int[paletteSize];
		
		float min = specMin;
		float range = specMax - min;
		
		float step = (range / paletteSize);
		
		// ARGB
		for(int i = 0; i < paletteSize; i++)
		{
			float[] rgb = waveLengthToRGB(min + (i * step));
			
			if(applySRGB)
			{
				rgb[RED] = (float) CIERGB.addSRGB(rgb[RED]);
				rgb[GREEN] = (float) CIERGB.addSRGB(rgb[GREEN]);
				rgb[BLUE] = (float) CIERGB.addSRGB(rgb[BLUE]);
			}
			
			int red = (int) (rgb[RED] * RGBCOLORDEPTH);
			int green = (int) (rgb[GREEN] * RGBCOLORDEPTH);
			int blue = (int) (rgb[BLUE] * RGBCOLORDEPTH);
			
			// ARGB
			palette[i] = (255 << 24) | (red << 16) | (green << 8) | blue;
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
	
	public static int[] SpectrumPaletteWrapped(boolean rgba, int paletteSize, boolean applySRGB)
	{
		int palette[] = new int[paletteSize];
		
		float min = 450;
		float range = 730 - min;
		
		float step = (range / paletteSize * 2);
		
		// ARGB
		for(int i = 0; i <= paletteSize / 2; i++)
		{
			float[] rgb = waveLengthToRGB(min + (i * step));
			
			if(applySRGB)
			{
				rgb[RED] = (float) CIERGB.addSRGB(rgb[RED]);
				rgb[GREEN] = (float) CIERGB.addSRGB(rgb[GREEN]);
				rgb[BLUE] = (float) CIERGB.addSRGB(rgb[BLUE]);
			}
			
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
			
			if(applySRGB)
			{
				rgb[RED] = (float) CIERGB.addSRGB(rgb[RED]);
				rgb[GREEN] = (float) CIERGB.addSRGB(rgb[GREEN]);
				rgb[BLUE] = (float) CIERGB.addSRGB(rgb[BLUE]);
			}
			
			int red = (int) (rgb[RED] * RGBCOLORDEPTH);
			int green = (int) (rgb[GREEN] * RGBCOLORDEPTH);
			int blue = (int) (rgb[BLUE] * RGBCOLORDEPTH);
			
			// ARGB
			palette[i] = (255 << 24) | (red << 16) | (green << 8) | blue;
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
	
	public static int[] SpectrumPaletteWrapped8(boolean rgba, int paletteSize, boolean applySRGB)
	{
		int wrapSize = paletteSize / 8;
		
		int[] tPalette = SpectrumPaletteWrapped(rgba, wrapSize, applySRGB);
		
		int[] palette = new int[paletteSize];
		
		for(int w = 0; w < paletteSize; w += wrapSize)
		{
			System.arraycopy(tPalette, 0, palette, w, wrapSize);
		}
		
		return palette;
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
	
	public static int[] RGBGreyScale(boolean rgba, int paletteSize, boolean applySRGB)
	{
		int palette[] = new int[paletteSize];
		
		float ci = 1f / paletteSize;
		
		// ARGB
		for(int i = 0; i < paletteSize; i++)
		{
			palette[i] = new Color(i * ci, i * ci, i * ci, 1f).getRGB();
			
			if(applySRGB)
			{
				palette[i] = CIERGB.addSRGB(palette[i]);
			}
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
	
	public static int[] BlackToColor(float red, float green, float blue, boolean rgba, int paletteSize, boolean applySRGB)
	{
		int palette[] = new int[paletteSize];
		
		float scale = 1f / paletteSize;
		
		// ARGB
		for(int i = 0; i < paletteSize; i++)
		{
			palette[i] = new Color(red * (scale * i), green * (scale * i), blue * (scale * i), 1f).getRGB();
			
			if(applySRGB)
			{
				palette[i] = CIERGB.addSRGB(palette[i]);
			}
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
	
	public static int[] LabSpecturmPalette(boolean rgba, int paletteSize, boolean applySRGB)
	{
		int palette[] = new int[paletteSize];
		
		float hBase = 270f;
		float hUpper = -90f;
		
		float hRange = (hUpper - hBase);
		float hStep = hRange / (paletteSize);
		
		float lUpper = 100f;
		float lStep = lUpper / (paletteSize);
		
		float hval = hBase;
		float iVal = 0.1f;
		
		float cUpper = 100f;
		float cStep = cUpper / (paletteSize);
		float cVal = 0;
		
		// ARGB
		for(int i = 0; i < paletteSize; i++)
		{
			// iVal = CIERGB.lStar(iVal / lUpper) * lUpper;
			// cVal = CIERGB.lStar(cVal / cUpper) * cUpper;
			// hval = CIERGB.lStar(hval/hBase)*hBase;
			
			float[] lab = CIERGB.labCHtoLAB(iVal, cUpper - cVal, hval);
			
			// System.out.println("hval : " + hval);
			// System.out.println("iVal : " + iVal);
			// System.out.println("cVal : " + cVal);
			// System.out.println("L : " + lab[0]);
			// System.out.println("A : " + lab[1]);
			// System.out.println("B : " + lab[2]);
			
			float[] rgb = CIERGB.XYZtoRGB(CIERGB.lab1976ToXYZ(lab));
			
			if(applySRGB)
			{
				rgb[RED] = (float) CIERGB.addSRGB(rgb[RED]);
				rgb[GREEN] = (float) CIERGB.addSRGB(rgb[GREEN]);
				rgb[BLUE] = (float) CIERGB.addSRGB(rgb[BLUE]);
			}
			
			int red = (int) (rgb[RED] * RGBCOLORDEPTH);
			int green = (int) (rgb[GREEN] * RGBCOLORDEPTH);
			int blue = (int) (rgb[BLUE] * RGBCOLORDEPTH);
			
			// ARGB
			palette[i] = (255 << 24) | (red << 16) | (green << 8) | blue;
			
			// Adjust vals
			hval = (i * hStep) + hBase;
			iVal = (i * lStep) + 1; // +1
			cVal = (i * cStep) + 1; // +1
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
	
	public static int[] LabGreyScale(boolean rgba, int paletteSize, boolean applySRGB)
	{
		int palette[] = new int[paletteSize];
		
		float lUpper = 100f;
		float lStep = lUpper / (paletteSize);
		
		float iVal = 0.1f;
		
		// ARGB
		for(int i = 0; i < paletteSize; i++)
		{
			float[] lab = CIERGB.labCHtoLAB(iVal, 0, 0);
			
			float[] rgb = CIERGB.XYZtoRGB(CIERGB.lab1976ToXYZ(lab));
			
			if(applySRGB)
			{
				rgb[RED] = (float) CIERGB.addSRGB(rgb[RED]);
				rgb[GREEN] = (float) CIERGB.addSRGB(rgb[GREEN]);
				rgb[BLUE] = (float) CIERGB.addSRGB(rgb[BLUE]);
			}
			
			int red = (int) (rgb[RED] * RGBCOLORDEPTH);
			int green = (int) (rgb[GREEN] * RGBCOLORDEPTH);
			int blue = (int) (rgb[BLUE] * RGBCOLORDEPTH);
			
			// ARGB
			palette[i] = (255 << 24) | (red << 16) | (green << 8) | blue;
			
			// Adjust vals
			iVal = (i * lStep) + 1; // +1
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
	
	// Incomplete
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
			
			pP = (p / modStepSize) % 1;
			
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
	
	public static String RGBtoString(float[] rgb)
	{
		return new String(rgb[0] + "," + rgb[1] + "," + rgb[2]);
	}
	
	public static String RGBtoString(double[] rgb)
	{
		return new String(rgb[0] + "," + rgb[1] + "," + rgb[2]);
	}
}
