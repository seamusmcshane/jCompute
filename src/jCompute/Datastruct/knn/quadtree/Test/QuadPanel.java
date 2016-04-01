package jCompute.Datastruct.knn.quadtree.Test;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.List;

import javax.swing.JPanel;

import jCompute.Datastruct.knn.KNNFloatPosInf;

public class QuadPanel extends JPanel
{
	private static final long serialVersionUID = 2594853691732440288L;

	private float[][] lines;
	private List<KNNFloatPosInf> list;
	private int size;

	private float[] search;
	private float diameter = 0;

	private boolean searchValid1NN = false;
	private float[] result;

	private List<KNNFloatPosInf> nearestNeighbours;
	private boolean searchValidKNN = false;

	private float pointSize;

	final static BasicStroke thickLine = new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	final static BasicStroke thinLine = new BasicStroke(1f);

	public QuadPanel(int size, float pointSize)
	{
		this.size = size;

		this.search = new float[2];
		this.result = new float[2];

		this.pointSize = pointSize;
	}

	public void setLines(float[][] lines)
	{
		this.lines = lines;
	}

	public void showSearchPointAndRange(float[] search, float distance)
	{
		this.search[0] = search[0];
		this.search[1] = search[1];
		this.diameter = distance * 2;
	}

	public void setShow1NNResult(float[] result)
	{
		this.result[0] = result[0];
		this.result[1] = result[1];
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
		g2.clearRect(0, 0, size, size);

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
				g2.drawOval((int) (point.getLatchedPos()[0] - pointHalf), (int) (point.getLatchedPos()[1] - pointHalf), (int) pointSize, (int) pointSize);
			}
		}

		if(searchValidKNN)
		{
			for(KNNFloatPosInf point : nearestNeighbours)
			{
				g2.setColor(Color.RED);
				g2.fillOval((int) (point.getLatchedPos()[0] - pointHalf), (int) (point.getLatchedPos()[1] - pointHalf), (int) pointSize, (int) pointSize);
			}
		}

		if(searchValid1NN)
		{
			g2.setColor(Color.BLACK);
			g2.setStroke(thickLine);
			g2.drawLine((int) search[0], (int) search[1], (int) result[0], (int) result[1]);

			g2.setColor(Color.WHITE);
			g2.setStroke(thinLine);
			g2.drawLine((int) search[0], (int) search[1], (int) result[0], (int) result[1]);

			g2.setColor(Color.GREEN);
			g2.fillOval((int) (result[0] - pointHalf), (int) (result[1] - pointHalf), (int) pointSize, (int) pointSize);

			g2.setColor(Color.BLACK);
			g2.setStroke(defaultStroke);
		}

		float radius = diameter / 2;

		g2.setColor(Color.BLUE);
		g2.fillOval((int) (search[0] - pointHalf), (int) (search[1] - pointHalf), (int) pointSize, (int) pointSize);

		g2.setColor(Color.RED);
		g2.drawOval((int) (search[0] - radius), (int) (search[1] - radius), (int) diameter, (int) diameter);
	}

	public void setPoints(List<KNNFloatPosInf> list)
	{
		this.list = list;
	}
}
