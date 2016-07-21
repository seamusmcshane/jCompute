package jcompute.gui.view.graphics;

import java.awt.Color;

public class A2RGBA
{
	public float red;
	public float green;
	public float blue;
	public float alpha;
	
	public A2RGBA(float red, float green, float blue,float alpha)
	{
		super();
		this.red = red;
		this.green = green;
		this.blue = blue;
		this.alpha = alpha;
	}
	
	public A2RGBA(Color color)
	{
		super();
		// Int 255 to 1 float
		this.red = 1f/255f*color.getRed();
		this.green = 1f/255f*color.getGreen();
		this.blue =	1f/255f*color.getBlue();
		this.alpha = 1f/255f*color.getAlpha();
	}
	
	public float getRed()
	{
		return red;
	}
	public void setRed(float red)
	{
		this.red = red;
	}
	public float getGreen()
	{
		return green;
	}
	public void setGreen(float green)
	{
		this.green = green;
	}
	public float getBlue()
	{
		return blue;
	}
	public void setBlue(float blue)
	{
		this.blue = blue;
	}
	public float getAlpha()
	{
		return alpha;
	}
	public void setAlpha(float alpha)
	{
		this.alpha = alpha;
	}
}
