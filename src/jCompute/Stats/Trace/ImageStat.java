package jCompute.Stats.Trace;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class ImageStat
{
	private String name;
	private BufferedImage image;

	private Graphics2D g2d;
	
	public ImageStat(String name, int planeSize)
	{
		this.name = name;
		
		image = new BufferedImage(planeSize, planeSize+50, BufferedImage.TYPE_INT_ARGB);
		
		// Clear the background
		Graphics g = image.getGraphics();
		g2d = (Graphics2D) g;
		g2d.setBackground(new Color(255,255,255,0));
		image.getGraphics().clearRect(0, 0, image.getWidth(), image.getHeight());
	}
	
	public void addSample(int x, int y)
	{
		g2d.drawRect(x, y, 1, 1);
	}
	
	public void setColor(Color input)
	{
		// Alpha changed
		Color color = new Color(input.getRed(),input.getGreen(),input.getBlue(),63);
		g2d.setColor(color);
	}
	
	public String getName()
	{
		return name;
	}
	
	public BufferedImage getImage()
	{
		image.getGraphics().setColor(Color.WHITE);
		image.getGraphics().drawString(name, 10,image.getHeight()-25);
		
		return image;
	}
}
