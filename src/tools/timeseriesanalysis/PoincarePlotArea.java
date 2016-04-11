package tools.timeseriesanalysis;

import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;

public class PoincarePlotArea extends JPanel
{
	private static final long serialVersionUID = 8642450830875810448L;
	private float xScale;
	private float yScale;

	double maxX = 0;
	double maxY = 0;

	double vals[][];

	double xvals[];
	double yvals[];

	double intersect;

	double angleMax = 0;
	double distMax = 0;

	double previous;
	
	public PoincarePlotArea(double intersect, double xvals[], double yvals[])
	{
		this.intersect = intersect;
		this.xvals = xvals;
		this.yvals = yvals;

		vals = computeVals(xvals, yvals);

	}

	private double[][] computeVals(double xvals[], double yvals[])
	{
		ArrayList<Double> anglesList = new ArrayList<Double>();
		ArrayList<Double> distList = new ArrayList<Double>();

		int len = xvals.length;

		double xMin = Double.MAX_VALUE;
		maxX = Double.MIN_VALUE;
		double yMin = Double.MAX_VALUE;
		maxY = Double.MIN_VALUE;
		double xMid = 0;
		double yMid = 0;

		double x = 0;
		double y = 0;
		for(int i = 0; i < len; i++)
		{
			x = xvals[i];
			y = yvals[i];

			if(x > maxX)
			{
				maxX = x;
			}

			if(y > maxY)
			{
				maxY = y;
			}

			if(x < xMin)
			{
				xMin = x;
			}

			if(y < yMin)
			{
				yMin = y;
			}
		}
		
		xMid = (maxX - xMin) / 2;
		yMid = (maxY - yMin) / 2;

		System.out.println("xMin " + xMin);
		System.out.println("xMax " + maxX);
		System.out.println("yMax " + maxY);
		System.out.println("yMin " + yMin);
		System.out.println("Xmin " + xMin);
		
		System.out.println("xMid " + xMid);
		System.out.println("yMid " + yMid);		
		
		for(int i = 0; i < len; i++)
		{
			double val[] = calculatePolar(xMid, yMid, xvals[i], yvals[i]);

			distList.add(val[0]);
			anglesList.add(val[1]);
		}

		double[] anglesArr = new double[anglesList.size()];
		int angleListSize = anglesList.size();

		for(int i = 0; i < angleListSize; i++)
		{
			anglesArr[i] = anglesList.get(i);

			//System.out.println(anglesArr[i]);

			if(anglesArr[i] > angleMax)
			{
				angleMax = anglesArr[i];
			}
		}

		double[] distArr = new double[distList.size()];
		int distListSize = distList.size();

		for(int i = 0; i < distListSize; i++)
		{
			distArr[i] = distList.get(i);

			if(distArr[i] > distMax)
			{
				distMax = distArr[i];
			}

		}

		return new double[][]
		{
				distArr, anglesArr
		};
	}

	private double[] calculatePolar(double x1, double y1, double x2, double y2)
	{
		double dx = x2 - x1;
		double dy = y2 - y1;

		//System.out.println("ATAN : " + Math.atan2(dy, dx));
		
		double angle = Math.toDegrees(Math.atan2(dy, dx));

		angle = (angle + 360) % 360;
		
		return new double[]
		{
				Math.sqrt(dx + dy), angle
		};
	}

	@Override
	public void paint(Graphics g)
	{
		int pad = 100;

		Graphics2D g2 = (Graphics2D) g;

		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

		g2.setColor(Color.WHITE);
		g2.clearRect(0, 0, this.getWidth(), this.getHeight());
		g2.setColor(new Color(0, 0, 0, 128));

		int len = vals[0].length;

		xScale = (float) (maxX/this.getWidth());
		//yScale = (float) (maxY/this.getHeight());

		// xScale = (float) (this.getWidth() / distMax);
		yScale = (float) (this.getHeight() / distMax);
		//yScale = (float) (this.getHeight() / angleMax);

		previous = intersect;
		
		for(int i = 0; i < len; i++)
		{
			int x = (int) (xvals[i] * xScale);
			//int y = (int) (yvals[i] * yScale);

			double dist = vals[0][i];
			double angle = vals[1][i];

			// int x = (int) (dist * xScale);
			int y = (int) (dist * yScale);
			// int y = (int) (angle * yScale);

			int size = 4;
			if(previous < intersect && angle >= intersect)
			{
				g2.fillOval(x - (size/2) + pad, y - (size/2) + pad, size, size);
				
				//g2.drawLine( x - 1 + pad, y - 1 + pad, x - 1 + pad, y - 1 + pad ); 
				
				//System.out.println(x + " " + y );
			}

			previous = angle;
		}

		g2.drawString("intersect " + intersect, 20, 20);

		g2.drawString("0", pad, this.getHeight() - pad);

		// Base
		g2.drawLine(pad, this.getHeight() - pad, this.getWidth(), this.getHeight() - pad);
		g2.drawString(String.valueOf(maxX), this.getWidth() - pad, this.getHeight() - pad);

		// Left
		g2.drawLine(pad, pad, pad, this.getHeight() - pad);
		g2.drawString(String.valueOf(maxY), pad, pad);

	}
	
	public void setIntersect(int angle)
	{
		intersect = angle;
		
		this.repaint();
	}
}
