package jcompute.configuration.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "Color")
@XmlType(propOrder =
{
	"red", "green", "blue"
})
public class Color
{
	private int[] color = new int[3];
	
	public Color()
	{
		
	}
	
	public Color(int red, int green, int blue)
	{
		color[0] = red;
		color[1] = green;
		color[2] = blue;
	}
	
	@XmlElement(name = "Red")
	public void setRed(int red)
	{
		color[0] = red;
	}
	
	public int getRed()
	{
		return color[0];
	}
	
	@XmlElement(name = "Green")
	public void setGreen(int green)
	{
		color[1] = green;
	}
	
	public int getGreen()
	{
		return color[1];
	}
	
	@XmlElement(name = "Blue")
	public void setBlue(int blue)
	{
		color[2] = blue;
	}
	
	public int getBlue()
	{
		return color[2];
	}
}
