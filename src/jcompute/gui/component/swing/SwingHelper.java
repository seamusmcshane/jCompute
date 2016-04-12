package jcompute.gui.component.swing;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class SwingHelper
{
	private static SwingHelper swingHelper;
	
	private SwingHelper()
	{

	}
	
	public static SwingHelper getInstance()
	{
		if(swingHelper == null)
		{
			swingHelper = new SwingHelper();
		}
		
		return swingHelper;
	}
	
	public int getFontStringWidth(Font font, String string)
	{
		BufferedImage proxy = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = proxy.createGraphics();
		return (int) (font.createGlyphVector(g.getFontRenderContext(), string).getVisualBounds().getWidth());
	}
}
