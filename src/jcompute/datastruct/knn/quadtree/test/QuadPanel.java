package jcompute.datastruct.knn.quadtree.test;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.List;

import javax.swing.JPanel;

import jcompute.datastruct.knn.KNNFloatPosInf;
import jcompute.math.geom.JCVector2f;

public class QuadPanel extends JPanel
{
	private static final long serialVersionUID = 2594853691732440288L;
	
	private float[][] lines;
	private List<KNNFloatPosInf> list;
	private int WIDTH;
	private int HEIGHT;
	
	private JCVector2f search;
	private float diameter = 0;
	
	private boolean searchValid1NN = false;
	private JCVector2f result;
	
	private List<KNNFloatPosInf> nearestNeighbours;
	private boolean searchValidKNN = false;
	
	private float pointSize;
	
	final static BasicStroke thickLine = new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	final static BasicStroke thinLine = new BasicStroke(1f);
	
	public QuadPanel(int WIDTH, int HEIGHT, float pointSize)
	{
		this.WIDTH = WIDTH;
		this.HEIGHT = HEIGHT;
		
		this.search = new JCVector2f(0, 0);
		this.result = new JCVector2f(0, 0);
		
		this.pointSize = pointSize;
	}
	
	public void setLines(float[][] lines)
	{
		this.lines = lines;
	}
	
	public void showSearchPointAndRange(JCVector2f search, float distance)
	{
		this.search.x = search.x;
		this.search.y = search.y;
		this.diameter = distance * 2;
	}
	
	public void setShow1NNResult(JCVector2f result)
	{
		this.result.x = result.x;
		this.result.y = result.y;
		searchValid1NN = true;
	}
	
	public void setShowKNNResult(List<KNNFloatPosInf> nearestNeighbours)
	{
		this.nearestNeighbours = nearestNeighbours;
		searchValidKNN = true;
	}
	
	public void clearSearch()
	{
		searchValid1NN = false;
		searchValidKNN = false;
	}
	
	// Do the panel drawing then draw the tree
	@Override
	public void paintComponent(Graphics g)
	{
		float pointHalf = pointSize / 2;
		
		super.paintComponent(g);
		
		Graphics2D g2 = (Graphics2D) g;
		
		Stroke defaultStroke = g2.getStroke();
		
		g2.setColor(Color.WHITE);
		g2.clearRect(0, 0, WIDTH, HEIGHT);
		
		if(lines != null)
		{
			// Avoid aliasing
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			// Auto change color based on Zoom
			// g2.setColor(new Color(253-zoom,253-zoom,253-zoom));
			
			for(int l = 0; l < lines.length; l += 2)
			{
				// System.out.println(lines[l][0]+"x"+lines[l][1]+"
				// "+lines[l+1][0]+"x"+lines[l+1][1]);
				g2.setColor(Color.BLACK);
				g2.drawLine((int) lines[l][0], (int) lines[l][1], (int) lines[l + 1][0], (int) lines[l + 1][1]);
			}
			
			// draw tree
			// g2.drawString(Integer.toString(originX)+ " " +
			// Integer.toString(originY),originX,originY);
			
		}
		
		if(list != null)
		{
			for(KNNFloatPosInf point : list)
			{
				g2.setColor(Color.BLACK);
				g2.drawOval((int) (point.getXY().x - pointHalf), (int) (point.getXY().y - pointHalf), (int) pointSize, (int) pointSize);
			}
		}
		
		if(searchValidKNN)
		{
			for(KNNFloatPosInf point : nearestNeighbours)
			{
				g2.setColor(Color.RED);
				g2.fillOval((int) (point.getXY().x - pointHalf), (int) (point.getXY().y - pointHalf), (int) pointSize, (int) pointSize);
			}
		}
		
		if(searchValid1NN)
		{
			g2.setColor(Color.BLACK);
			g2.setStroke(thickLine);
			g2.drawLine((int) search.x, (int) search.y, (int) result.x, (int) result.y);
			
			g2.setColor(Color.WHITE);
			g2.setStroke(thinLine);
			g2.drawLine((int) search.x, (int) search.y, (int) result.x, (int) result.y);
			
			g2.setColor(Color.GREEN);
			g2.fillOval((int) (result.x - pointHalf), (int) (result.y - pointHalf), (int) pointSize, (int) pointSize);
			
			g2.setColor(Color.BLACK);
			g2.setStroke(defaultStroke);
		}
		
		float radius = diameter / 2;
		
		g2.setColor(Color.BLUE);
		g2.fillOval((int) (search.x - pointHalf), (int) (search.y - pointHalf), (int) pointSize, (int) pointSize);
		
		g2.setColor(Color.RED);
		g2.drawOval((int) (search.x - radius), (int) (search.y - radius), (int) diameter, (int) diameter);
	}
	
	public void setPoints(List<KNNFloatPosInf> list)
	{
		this.list = list;
	}
}
