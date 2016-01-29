package jCompute.Gui.Component.TableCell;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormatSymbols;

import javax.swing.JComponent;

/* Class for Progress Bar */
public class ProgressBar extends JComponent
{
	private static final long serialVersionUID = -5090932584537058142L;
	
	private int progress;
	private Color fg;
	private Color bg;
	private Color bar;
	
	public ProgressBar()
	{
		progress = 0;
		
		fg = Color.black;
		bar = Color.LIGHT_GRAY;
		bg = Color.LIGHT_GRAY;
	}
	
	public ProgressBar(Color fg, Color bg, Color bar)
	{
		progress = 0;
		
		this.fg = fg;
		this.bg = bg;
	}
	
	public ProgressBar(int value)
	{
		setProgress(value);
		
		fg = Color.black;
		bg = Color.LIGHT_GRAY;
	}
	
	public void setProgress(int value)
	{
		if(value < 100)
		{
			progress = value;
		}
		else
		{
			progress = 100;
		}
	}
	
	private String progressToString()
	{
		if(progress < 0)
		{
			return new DecimalFormatSymbols().getInfinity();
		}
		
		return Integer.toString(progress) + " %";
	}
	
	@Override
	protected void paintComponent(Graphics g1)
	{
		super.paintComponent(g1);
		
		Graphics2D g2 = (Graphics2D) g1;
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		// Draw bar
		Rectangle clip = g2.getClipBounds();
		g2.setColor(bg);
		g2.fillRect(clip.x, clip.y, clip.width, clip.height);
		
		float barWidth = ((float) progress / 100);
		
		g2.setColor(bar);
		g2.fillRect(clip.x, clip.y, (int) (clip.width * barWidth), clip.height);
		
		FontMetrics fontMetric = g2.getFontMetrics(g2.getFont());
		Rectangle2D textSize = fontMetric.getStringBounds(progressToString(), g2);
		
		// Draw Label
		g2.setColor(fg);
		g2.drawString(progressToString(), (int) (clip.width - textSize.getWidth()) / 2, (int) (clip.height - textSize.getHeight() / 2) + (fontMetric.getDescent() / 2));
	}
	
	public void setBG(Color background)
	{
		this.bg = background;
	}
	
}
