package alife;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;
/**
 * A Panel used for drawing a Lorenze Attractor Graph.
 * @author Seamus McShane
 * @version $Revision: 1.0 $
 */
public class StatsLorenzGraphPanel extends JPanel
{
	private static final long serialVersionUID = 3120518905315513311L;

	/** The references to the sample arrays */
	private int plantsSamples[];
	private int preySamples[];
	private int predSamples[];

	/** Graph Size */
	private int graphX;
	private int graphY;
	private int graphWidth;
	private int graphHeight;

	/*
	 * Used to not attempt to start drawing if the max was changed - not
	 * critical so no need to use a Semaphore
	 */
	boolean drawing = false;

	/* The Draw mode - between static and dynamic ratio based */
	boolean modeDynamicBased = false;
	float modeScale = 0.15f;

	/* Lorenz Modifier Variable - Adjusts the pivot of the equation */
	float lmod = 0;
	float lModMax = 0;

	/* Max values used to scale graph */
	int plantsMax = 0;
	int preyMax = 0;
	int predMax = 0;

	/*
	 * Large numbers of Predators rarely appear except in an Ecosystem collapse
	 * event. Thus red is rarely on the graph, for better visuals the color
	 * value for predator numbers are boosted by this factor.
	 */
	float predatorGraphBoost = 1f;

	/* As above but plants always are in large numbers and get a negative boost */
	float plantGraphNegativeFactor = 1f;

	// Graph Scale + Zoom
	float zoom = 0;
	float xScale = 1;
	float yScale = 1;

	/* The line width */
	float maxGraphLineSize = 1f;

	/* The total Samples Drawn */
	private float maxSampleNum;

	/* Marks the end of the sample range so we dont drawn the unfilled slots */
	private float graphSamples = 1;

	/** Graph Origin */
	float originX = 0;
	float originY = 0;

	/** Line Vectors */
	float cx = originX; /* Current Point */
	float cy = originY;
	float nx = originX; /* Next Point */
	float ny = originY;

	/** Mouse Coordinates */
	private int cmX = 0;
	private int cmY = 0;

	/**
	 * A new Graph Panel
	 **/
	public StatsLorenzGraphPanel()
	{
		completeResetGraph();
		setZoom(100);
	}

	/**
	 * Method setSampleArrays.
	 * @param plantsSamples int[]
	 * @param preySamples int[]
	 * @param predSamples int[]
	 * @param sampleNum int
	 */
	public void setSampleArrays(int plantsSamples[], int preySamples[], int predSamples[], int sampleNum)
	{
		this.plantsSamples = plantsSamples;
		this.preySamples = preySamples;
		this.predSamples = predSamples;

		graphSamples = 1;

		this.maxSampleNum = sampleNum;
	}

	/**
	 * Updates the graph and draws it on an interval based on currSampleNum.
	 * @param currSampleNum
	 * @param plantsMax int
	 * @param preyMax int
	 * @param predMax int
	 */
	public void updateGraph(int plantsMax, int preyMax, int predMax, int currSampleNum)
	{
		this.plantsMax = plantsMax;
		this.preyMax = preyMax;
		this.predMax = predMax;

		if (currSampleNum < maxSampleNum)
		{
			graphSamples = currSampleNum;
		}
		else
		{
			graphSamples = maxSampleNum;
		}

	}

	/** Draws the graph 
	 * @param g Graphics
	 */
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D) g;

		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		calculateGraphSize();

		drawSamples(g2);

	}

	/** 
	 * Draws the graph line.
	 * Based on http://www.sat.t.u-tokyo.ac.jp/~hideyuki/java/Attract.java
	 * @param g2 Graphics2D
	 */
	public void drawSamples(Graphics2D g2)
	{
		if (plantsSamples == null || preySamples == null || predSamples == null)
		{
			return;
		}

		drawing = true;

		if (modeDynamicBased)
		{
			modeScale = 0.25f;

			predatorGraphBoost = ((float) preyMax / (float) predMax);

			// Limit the ratios
			if (predatorGraphBoost > 4)
			{
				plantGraphNegativeFactor = 4;
			}

			plantGraphNegativeFactor = 1f - ((float) plantsMax / (float) (preyMax + predMax + plantsMax));

			// Limit the ratios
			if (plantGraphNegativeFactor <= 0.65f)
			{
				plantGraphNegativeFactor = 0.65f;
			}

		}
		else
		// Dynamic mode drawing off
		{
			modeScale = 0.15f;
			plantGraphNegativeFactor = 0.05f;
			predatorGraphBoost = 4;
			plantGraphNegativeFactor = 1;
		}

		// Find the max
		lModMax = Math.max(1, plantsMax * plantGraphNegativeFactor);
		lModMax = Math.max(lModMax, preyMax);
		lModMax = Math.max(lModMax, predMax * predatorGraphBoost);

		// half the max so lmod will cause the curve to phaze invert on a negative (ie x=50, max = 200, lmod = 100, drawing x = -50 etc)
		lmod = lModMax / 2;

		/* Loops through all three sample arrays */
		for (int i = 0; i < graphSamples; i++)
		{
			// The values we feed to the Lorenz equations.
			double x = (plantsSamples[i] * plantGraphNegativeFactor) - lmod;
			double y = (predSamples[i] * predatorGraphBoost) - lmod;
			double z = (preySamples[i]) - lmod;
			/*
			 * Lmod is half of the max value of all samples, meaning this can
			 * cause X,Y and Z to go negative. By going negative the other half
			 * of the Lorenz system is drawn.
			 */

			// Modified Lorenz Equations
			int gx = (int) (((x + ((-10 * x) + (10 * y)))) * (modeScale));
			int gy = (int) (((y + ((28 * x) + (y - (x * z))))) * (modeScale));
			int gz = (int) ((z + ((-8 * (z / 3)) + (x * y))) * (modeScale));

			// Scale the point position by the graph scale.
			//gx=(int)(gx*scale);
			gy = (int) (gy * zoom * xScale);
			gz = (int) (gz * zoom * yScale);

			if (i == 0)
			{
				nx = gz;
				ny = gy;
			}

			// The current point
			cx = gz;
			cy = gy;

			// Line Width
			g2.setStroke(new BasicStroke((i * (maxGraphLineSize / graphSamples))));

			// R G B A
			g2.setColor(new Color((int) ((predSamples[i] * predatorGraphBoost) * (255 / lModMax)) % 255, (int) ((plantsSamples[i] * plantGraphNegativeFactor) * (255 / lModMax)) % 255, (int) (preySamples[i] * (255 / lModMax)) % 255, (int) (i * (255 / graphSamples)) % 255));

			// Draws line based on where the origin is.
			g2.drawLine((int) (cx + (originX)), (int) (-cy + (originY)), (int) (nx + (originX)), (int) (-ny + (originY)));

			// The next lines - next point		    
			nx = cx;
			ny = cy;
		}

		drawing = false;

		// Track the last Sample (follows it on the graph)
		originX = -(cx) + graphWidth / 2f;
		originY = -(-cy) + graphHeight / 2f;

	}

	/**
	 * Method setXScale.
	 * @param xScale float
	 */
	public void setXScale(float xScale)
	{
		this.xScale = (xScale + 1) / 10f;
		repaint();
	}

	/**
	 * Method setYScale.
	 * @param yScale float
	 */
	public void setYScale(float yScale)
	{
		this.yScale = (yScale + 1) / 10f;
		repaint();
	}

	/**
	 * Method setZoom.
	 * @param inZoom float
	 */
	public void setZoom(float inZoom)
	{
		inZoom = (inZoom / 1000000f);

		if (inZoom > 0)
		{
			zoom = inZoom;
		}
		else
		{
			zoom = 1 / 1000000f;
		}

		repaint();
	}

	// Not used anymore
	/**
	 * Method setMpos.
	 * @param x int
	 * @param y int
	 */
	public void setMpos(int x, int y)
	{
		cmX = x;
		cmY = y;
	}

	// Not used anymore
	/**
	 * Method moveGraph.
	 * @param mX int
	 * @param mY int
	 */
	public void moveGraph(int mX, int mY)
	{
		originX = originX + (mX - cmX);
		originY = originY + (mY - cmY);

		cmX = mX;
		cmY = mY;
		repaint();
	}

	public void completeResetGraph()
	{
		calculateGraphSize();
		originX = -(cx) + graphWidth / 2f;
		originY = -(cy) + graphHeight / 2f;
		lmod = 0;
		lModMax = 0;
		repaint();
	}

	/**
	 * Method resetGraph.
	 * @param ignored int
	 */
	public void resetGraph(int ignored)
	{
		calculateGraphSize();
		originX = graphWidth / 2f;
		originY = graphHeight / 2f;
		//lmod=0;
		repaint();
	}

	/** Gets the widths and height of this panel **/
	private void calculateGraphSize()
	{
		graphX = this.getX();
		graphY = this.getY();
		graphWidth = this.getWidth() - 1;
		graphHeight = this.getHeight() - 2;

	}

	/**
	 * Method getDynamicMode.
	 * @return boolean
	 */
	public boolean getDynamicMode()
	{
		return modeDynamicBased;
	}

	/**
	 * Method setDynamicMode.
	 * @param mode boolean
	 */
	public void setDynamicMode(boolean mode)
	{
		modeDynamicBased = mode;
		this.resetGraph(1);
	}

	public void drawGraph()
	{
		this.repaint();
	}
}
