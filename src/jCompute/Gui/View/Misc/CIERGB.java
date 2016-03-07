package jCompute.Gui.View.Misc;

public class CIERGB
{
	private static final float[] d50xyz =
	{
		0.9642f, 1.0f, 0.8249f
	};
	
	private static final float[] iccxyz = d50xyz;
	
	private static final float[] d65xyz =
	{
		0.9505f, 1.0f, 1.089f
	};
	
	private static final float[] eexyz =
	{
		1.0f, 1.0f, 1.0f
	};
	
	/**
	 * Simple Analytic Approximations to the CIE XYZ Color Matching Functions
	 * http://jcgt.org/published/0002/02/01/
	 * @param wavelen
	 * @return
	 */
	public static float XYZMultiLobeFit1931X(double wavelen)
	{
		double dParam1 = (wavelen - 442.0) * ((wavelen < 442.0) ? 0.0624 : 0.0374);
		double dParam2 = (wavelen - 599.8) * ((wavelen < 599.8) ? 0.0264 : 0.0323);
		double dParam3 = (wavelen - 501.1) * ((wavelen < 501.1) ? 0.0490 : 0.0382);
		return (float) (0.362 * Math.exp(-0.5 * dParam1 * dParam1) + 1.056 * Math.exp(-0.5 * dParam2 * dParam2) - 0.065 * Math.exp(-0.5 * dParam3 * dParam3));
	}
	
	/**
	 * Simple Analytic Approximations to the CIE XYZ Color Matching Functions
	 * http://jcgt.org/published/0002/02/01/
	 * @param wavelen
	 * @return
	 */
	public static float XYZMultiLobeFit1931Y(double wavelen)
	{
		double dParam1 = (wavelen - 568.8) * ((wavelen < 568.8) ? 0.0213 : 0.0247);
		double dParam2 = (wavelen - 530.9) * ((wavelen < 530.9) ? 0.0613 : 0.0322);
		
		return (float) (0.821f * Math.exp(-0.5f * dParam1 * dParam1) + 0.286 * Math.exp(-0.5 * dParam2 * dParam2));
	}
	
	/**
	 * Simple Analytic Approximations to the CIE XYZ Color Matching Functions
	 * http://jcgt.org/published/0002/02/01/
	 * @param wavelen
	 * @return
	 */
	public static float XYZMultiLobeFit1931Z(double wavelen)
	{
		double dParam1 = (wavelen - 437.0) * ((wavelen < 437.0) ? 0.0845f : 0.0278);
		double dParam2 = (wavelen - 459.0) * ((wavelen < 459.0) ? 0.0385f : 0.0725);
		return (float) (1.217 * Math.exp(-0.5 * dParam1 * dParam1) + 0.681 * Math.exp(-0.5 * dParam2 * dParam2));
	}
	
	public static float[] XYZMultiLobeFit1931XYZ(float wavelen)
	{
		return new float[]
		{
			XYZMultiLobeFit1931X(wavelen), XYZMultiLobeFit1931Y(wavelen), XYZMultiLobeFit1931Z(wavelen)
		};
	}
	
	/**
	 * How to interpret the sRGB color space (specified in IEC 61966-2-1) for ICC profiles
	 * www.color.org/srgb.pdf
	 * @param xyz
	 * @return
	 */
	public static float[] XYZtoRGB(float[] xyz)
	{
		// No luminosity
		if(xyz[1] <= 0)
		{
			xyz[0] = xyz[1] = xyz[2] = 0;
		}
		
		float r = 3.2406255f * xyz[0] + (-1.537208f) * xyz[1] + (-0.4986286f) * xyz[2];
		float g = (-0.9689307f) * xyz[0] + 1.8757561f * xyz[1] + 0.0415175f * xyz[2];
		float b = 0.0557101f * xyz[0] + (-0.2040211f) * xyz[1] + 1.0569959f * xyz[2];
		
		// r = (float) addSRGB(r);
		// g = (float) addSRGB(g);
		// b = (float) addSRGB(b);
		
		r = r < 0 ? 0 : r;
		r = r > 1 ? 1 : r;
		
		g = ((g < 0f) ? 0 : g);
		g = g > 1 ? 1 : g;
		
		b = b < 0 ? 0 : b;
		b = b > 1 ? 1 : b;
		
		return new float[]
		{
			r, g, b
		};
	}
	
	/*
	 * Via XYZ to xyY to xyY conversion.
	 */
	public static float[] scaleLumenanceXYZ(float[] xyz, float maxLum)
	{
		// TO xyY
		double xyY_x = xyz[0] / (xyz[0] + xyz[1] + xyz[2]);
		double xyY_y = xyz[1] / (xyz[0] + xyz[1] + xyz[2]);
		
		double xyY_Y = xyz[1] * 0.7f;
		
		float[] newXYZ = new float[3];
		
		// to XYZ
		newXYZ[0] = (float) (xyY_x * xyY_Y / xyY_y);
		newXYZ[1] = (float) (xyY_Y);
		newXYZ[2] = (float) ((1.0 - xyY_x - xyY_y) * xyY_Y / xyY_y);
		
		return newXYZ;
		
	}
	
	/*
	 * Via XYZ to xyY to xyY conversion.
	 */
	public static float[] capLumenanceXYZ(float[] xyz, float maxLum)
	{
		// TO xyY
		double xyY_x = xyz[0] / (xyz[0] + xyz[1] + xyz[2]);
		double xyY_y = xyz[1] / (xyz[0] + xyz[1] + xyz[2]);
		
		double xyY_Y = xyz[1];
		
		if(xyY_Y > maxLum)
		{
			xyY_Y = maxLum;
		}
		
		float[] newXYZ = new float[3];
		
		// to XYZ
		newXYZ[0] = (float) (xyY_x * xyY_Y / xyY_y);
		newXYZ[1] = (float) (xyY_Y);
		newXYZ[2] = (float) ((1.0 - xyY_x - xyY_y) * xyY_Y / xyY_y);
		
		return newXYZ;
		
	}
	
	public static float getLumenanceXYZ(float[] xyz)
	{
		return xyz[1];
	}
	
	public static float[] WavelengthToSRGB(float waveLength)
	{
		return XYZtoRGB(XYZMultiLobeFit1931XYZ(waveLength));
	}
	
	public static float[] RGBInttoXYZ(int red, int green, int blue)
	{
		return RGBFloatToXYZ((float) red / 255f, (float) green / 255f, (float) blue / 255f);
	}
	
	public static float[] RGBFloatToXYZ(double red, double green, double blue)
	{
		double nX = red * 0.4123999971730992 + green * 0.35760000265100844 + blue * 0.18050001435233867;
		double nY = red * 0.21259999073612254 + green * 0.7151999842346091 + blue * 0.07220001553015046;
		double nZ = red * 0.01930001704591329 + green * 0.11920004192621718 + blue * 0.9505000471041638;
		
		return new float[]
		{
			(float) nX, (float) nY, (float) nZ
		};
	}
	
	public static double addSRGB(double val)
	{
		if(val <= 0.0031308)
		{
			val = val * 12.92;
		}
		else
		{
			val = Math.pow(1.055 * val, 1.0 / 2.4) - 0.055;
		}
		
		return val;
	}
	
	public static double removeSRGB(double val)
	{
		
		if(val <= 0.04045)
		{
			val = val / 12.92;
		}
		else
		{
			val = (float) Math.pow(((val + 0.055) / 1.055), 2.4);
		}
		
		return val;
	}
	
	public static float[] lab1976ToXYZ(float[] lab)
	{
		float[] refWhite = d65xyz;
		
		// X = 0.9642, Y = 1, Z = 0.8249
		double fy = (lab[0] + 16.0) / 116.0;
		double fx = (lab[1] / 500.0) + fy;
		double fz = fy - (lab[2] / 200.0);
		
		double e = 216.0 / 24389.0;
		double k = 24389.0 / 27.0;
		
		double fx3 = Math.pow(fx, 3);
		double fy3 = Math.pow(fy, 3);
		double fz3 = Math.pow(fz, 3);
		
		double xR;
		double yR;
		double zR;
		
		if(fx3 > e)
		{
			xR = fx3;
		}
		else
		{
			xR = ((116.0 * fx) - 16.0) / k;
		}
		
		if(lab[0] > (k * e))
		{
			yR = fy3;
		}
		else
		{
			yR = lab[0] / k;
		}
		
		if(fz3 > e)
		{
			zR = fz3;
		}
		else
		{
			zR = ((116.0 * fz) - 16.0) / k;
		}
		
		return new float[]
		{
			(float) (xR * refWhite[0]), (float) (yR * refWhite[1]), (float) (zR * refWhite[2])
		};
	}
	
	public static float[] xyzToLAB1976(float[] xyz)
	{
		float[] refWhite = d50xyz;
		
		double e = 216.0 / 24389.0;
		double k = 24389.0 / 27.0;
		
		double xR = xyz[0] / refWhite[0];
		double yR = xyz[1] / refWhite[1];
		double zR = xyz[2] / refWhite[2];
		
		double fX = 0;
		double fY = 0;
		double fZ = 0;
		
		if(xR > e)
		{
			fX = Math.cbrt(xR);
		}
		else
		{
			fX = ((xR * k) + 16.0) / 116.0;
		}
		
		if(yR > e)
		{
			fY = Math.cbrt(yR);
		}
		else
		{
			fY = ((yR * k) + 16.0) / 116.0;
		}
		
		if(zR > e)
		{
			fZ = Math.cbrt(zR);
		}
		else
		{
			fZ = ((zR * k) + 16.0) / 116.0;
		}
		
		float[] lab = new float[3];
		
		lab[0] = (float) ((116.0 * fY) - 16.0);
		lab[1] = (float) (500.0 * (fX - fY));
		lab[2] = (float) (200.0 * (fY - fZ));
		
		return lab;
	}
	
	public static float[] labCHtoLAB(float l, float c, float h)
	{
		return new float[]
		{
			l, (float) (Math.cos(Math.toRadians(h)) * c), (float) (Math.sin(Math.toRadians(h)) * c)
		};
	}
	
	public static float[] labCHtoLAB(float[] lch)
	{
		return new float[]
		{
			lch[0], (float) (Math.cos(Math.toRadians(lch[2])) * lch[1]), (float) (Math.sin(Math.toRadians(lch[2])) * lch[1])
		};
	}
	
	static float[] labtoLabCH(float l, float a, float b)
	{
		float[] lch = new float[3];
		
		lch[0] = l;
		lch[1] = (float) Math.sqrt((a * a) + (b * b));
		lch[2] = (float) Math.atan2(b, a);
		
		return lch;
	}
	
	public static float[] labtoLabCH(float[] lab)
	{
		float[] lch = new float[3];
		
		lch[0] = lab[0];
		lch[1] = (float) Math.sqrt((lab[1] * lab[1]) + (lab[2] * lab[2]));
		lch[2] = (float) Math.atan2(lab[2], lab[1]);
		
		return lch;
	}
	
	public static float lStar(float val)
	{
		double e = 216.0 / 24389.0;
		double k = 24389.0 / 27.0;
		
		if(val < e)
		{
			return (float) ((val * k) / 100f);
		}
		else
		{
			return (float) (Math.pow(1.16 * val, 1.0 / 3.0) - 0.16);
		}
	}
	
}
