package tools.TimeSeriesAnalysis;

import javax.swing.JPanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;

public class PhasePlotArea extends JPanel
{
	private float xScale;
	private float yScale;

	double xvals[];
	double yvals[];
	
	double maxX = Double.MIN_VALUE;
	double maxY = Double.MIN_VALUE;
	
	double minX = Double.MAX_VALUE;
	double minY = Double.MAX_VALUE;

	double xMid;
	double yMid;
	
	double angle = 0;
	
	public PhasePlotArea(double xvals[], double yvals[])
	{
		this.xvals = xvals;
		this.yvals = yvals;

		for(int x = 0; x < xvals.length; x++)
		{
			if(xvals[x] > maxX)
			{
				maxX = xvals[x];
			}
			
			
			if(x < minX)
			{
				minX = x;
			}
		}

		for(int y = 0; y < yvals.length; y++)
		{
			if(yvals[y] > maxY)
			{
				maxY = yvals[y];
			}


			if(y < minY)
			{
				minY = y;
			}
		}
		
		xMid = (maxX - minX) / 2;
		yMid = (maxY - minY) / 2;

	}

	@Override
	public void paint(Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g;
		g2.setStroke(new BasicStroke(1));

		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

		g2.setColor(Color.WHITE);
		g2.clearRect(0, 0, this.getWidth(), this.getHeight());
		g2.setColor(new Color(0, 0, 0, 128));

		int xIndex1 = 0;
		int yIndex1 = 0;
		int xIndex2 = 1;
		int yIndex2 = 1;

		int len = xvals.length;

		xScale = (float) (this.getWidth() / maxX);
		yScale = (float) (this.getHeight() / maxY);

		for(int i = 0; i < len - 1; i++)
		{
			int x1 = (int) (xvals[xIndex1] * xScale);
			int y1 = (int) (yvals[yIndex1] * yScale);
			int x2 = (int) (xvals[xIndex2] * xScale);
			int y2 = (int) (yvals[yIndex2] * yScale);

			g2.drawLine(x1, y1, x2, y2);

			xIndex1 = (xIndex1 + 1) % len;
			yIndex1 = (yIndex1 + 1) % len;
			xIndex2 = (xIndex2 + 1) % len;
			yIndex2 = (yIndex2 + 1) % len;
		}

		int ax = (int) (((maxX*Math.cos(angle * 2 * Math.PI / 360))+xMid)*xScale);
		int ay = (int) (((maxY*Math.sin(angle * 2 * Math.PI / 360))+yMid)*yScale);
		
		g2.setColor(Color.RED);
		g2.setStroke(new BasicStroke(2));
		g2.drawLine(  (int)(xMid*xScale),(int)( yMid*yScale), ax,ay);		

		g2.drawString("Angle " + angle, 100, 100);        
	}
	
	public void setAngle(int angle)
	{
		this.angle = angle;
		
		this.repaint();
	}
}
