package jcompute.gui.view.renderer.util;

import jcompute.math.trig.JCTrig;

public class FrustumUtil
{
	private FrustumUtil()
	{
		
	}
	
	public static float CalculateFrustumHeight(float distance, float fieldOfView)
	{
		return (float) (JCTrig.tanLutInt((fieldOfView * JCTrig.DEG_TO_RAD) * 0.5f) * distance * 2f);
	}
	
	public static float CalculateFrustumWidth(float frustumHeight, float aspectRatio)
	{
		return frustumHeight * aspectRatio;
	}
}
