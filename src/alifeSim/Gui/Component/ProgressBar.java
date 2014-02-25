package alifeSim.Gui.Component;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormatSymbols;

import javax.swing.JComponent;

/* Class for Progress Bar */
public class ProgressBar extends JComponent
{
	private int progress;
	private Color fg;
	private Color bg;	
	
	public ProgressBar() 
	{
		progress = 0;
		
		fg = Color.black;
		bg = Color.LIGHT_GRAY;
	}
	
	public ProgressBar(Color fg, Color bg) 
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
	
	private Rectangle2D calcStringBounds(Graphics g, String string)
	{
		return g.getFontMetrics(g.getFont()).getStringBounds(string, g);
	}
	
	@Override protected void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		
		// Draw bar
		Rectangle clip = g.getClipBounds();
		g.setColor(bg);
		
		float barWidth = ((float)progress/100);
		
		g.fillRect(clip.x,clip.y, (int)(clip.width*barWidth),clip.height);

		FontMetrics fontMetric = g.getFontMetrics(g.getFont());
		Rectangle2D textSize = fontMetric.getStringBounds(progressToString(), g);
		
		// Draw Label
		g.setColor(fg);
		g.drawString(progressToString(), (int) (clip.width-textSize.getWidth())/2, (int) (clip.height - textSize.getHeight()/2) + fontMetric.getDescent());
	}

}


