package alife;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;
/**
 * A Panel used for drawing a Line graph.
 * @author Seamus McShane
 * @version $Revision: 1.0 $
 */
public class StatsLineGraphPanel extends JPanel
{

	private static final long serialVersionUID = 8946884538574615118L;

	/** The references to the sample arrays */
	private int plantsSamples[];
	private int preySamples[];
	private int predSamples[];

	/** Graph Size */
	private int graphX;
	private int graphY;
	private int graphWidth;
	private int graphHeight;

	/* The total Samples Drawn */
	private float maxSampleNum;

	/* Marks the end of the sample range so we dont drawn the unfilled slots */
	private float graphSamples = 1;

	/*
	 * The Scale Mode 0 Individual Scale 1 Predator + Prey tied to the same
	 * scale 2 All on Same Scale
	 */
	private int scaleMode = 2;

	/* Max values used to scale graph */
	private float plantMax = 0;
	private float preyMax = 0;
	private float predMax = 0;

	/**
	 * A new Graph Panel
	 */
	public StatsLineGraphPanel()
	{

	}

	/**
	 * Method setSampleArrays.
	 * @param plantsSamples int[]
	 * @param preySamples int[]
	 * @param predSamples int[]
	 * @param maxSampleNum int
	 */
	public void setSampleArrays(int plantsSamples[], int preySamples[], int predSamples[], int maxSampleNum)
	{
		this.plantsSamples = plantsSamples;
		this.preySamples = preySamples;
		this.predSamples = predSamples;

		graphSamples = 1;

		this.maxSampleNum = maxSampleNum;
	}

	/**
	 * Updates the graph and draws it on an interval based on currSampleNum.
	 * @param plantMax
	 * @param preyMax
	 * @param predMax
	 * @param currSampleNum
	 */
	public void updateGraph(float plantMax, float preyMax, float predMax, int currSampleNum)
	{
		this.plantMax = plantMax;

		this.preyMax = preyMax;

		this.predMax = predMax;

		switch (scaleMode)
		{
			case 0 :
				// Do nothing. - All on there own scales
				break;
			case 1 :
				tiePredPreyMax(); // Predator, Prey tied to the same scale
				break;
			case 2 :
				tieAllMax();	// Predator, Prey and Plants on the same scale
				break;
		}

		if (currSampleNum < maxSampleNum)
		{
			graphSamples = currSampleNum;
		}
		else
		{
			graphSamples = maxSampleNum;
		}

	}

	/**
	 * Method setScaleMode.
	 * @param scaleMode int
	 */
	public void setScaleMode(int scaleMode)
	{
		this.scaleMode = scaleMode;
	}

	/** 
	 * Does the plants or agents have the greater point
	 * Max is tied to the greater */
	public void tieAllMax()
	{
		float agentMax = tiePredPreyMax();

		if (plantMax > agentMax)
		{
			predMax = plantMax;
			preyMax = plantMax;
		}
		else
		{
			plantMax = agentMax;
		}
	}

	/**
	 * Which of the agents has the greater max point in the graph 
	 * max is tied to the greater, also returns max for further evaluation 
	 * @return float
	 */
	public float tiePredPreyMax()
	{
		float max;
		if (preyMax > predMax)
		{
			max = predMax = preyMax;
		}
		else
		{
			max = predMax = preyMax;
		}

		return max;
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

	/** Draws the graph lines 
	 * @param g2 Graphics2D
	 */
	public void drawSamples(Graphics2D g2)
	{

		if (plantsSamples == null || preySamples == null || predSamples == null)
		{
			return;
		}

		float scaleWidthInterval = graphWidth / graphSamples;

		float predScaleHeightInterval = graphHeight / (predMax + 1);
		float preyScaleHeightInterval = graphHeight / (preyMax + 1);
		float plantsScaleHeightInterval = graphHeight / (plantMax + 1);

		int predSampleXVal = 0;
		int predSampleYVal = 0;
		int predSamplePXVal = 0;
		int predSamplePYVal = 0;

		int plantsSampleXVal = 0;
		int plantsSampleYVal = 0;
		int plantsSamplePXVal = 0;
		int plantsSamplePYVal = 0;

		int preySampleXVal = 0;
		int preySampleYVal = 0;
		int preySamplePXVal = 0;
		int preySamplePYVal = 0;

		/* Loops through all three sample arrays */
		for (int i = 0; i < graphSamples; i++)
		{
			/* Plants */
			g2.setColor(Color.GREEN);
			plantsSampleXVal = (int) (i * scaleWidthInterval);

			plantsSampleYVal = graphHeight - ((int) (plantsSamples[i] * plantsScaleHeightInterval));

			g2.drawLine(plantsSamplePXVal, plantsSamplePYVal, plantsSampleXVal, plantsSampleYVal);

			plantsSamplePXVal = plantsSampleXVal;
			plantsSamplePYVal = plantsSampleYVal;

			/* Prey */
			g2.setColor(Color.BLUE);
			preySampleXVal = (int) (i * scaleWidthInterval);

			preySampleYVal = graphHeight - ((int) (preySamples[i] * preyScaleHeightInterval));

			g2.drawLine(preySamplePXVal, preySamplePYVal, preySampleXVal, preySampleYVal);

			preySamplePXVal = preySampleXVal;
			preySamplePYVal = preySampleYVal;

			/* Predators */
			g2.setColor(Color.red);
			predSampleXVal = (int) (i * scaleWidthInterval);

			predSampleYVal = graphHeight - ((int) (predSamples[i] * predScaleHeightInterval));

			g2.drawLine(predSamplePXVal, predSamplePYVal, predSampleXVal, predSampleYVal);

			predSamplePXVal = predSampleXVal;
			predSamplePYVal = predSampleYVal;

		}
	}

	/** Gets the widths and height of this panel */
	private void calculateGraphSize()
	{
		graphX = this.getX();
		graphY = this.getY();
		graphWidth = this.getWidth() - 1;
		graphHeight = this.getHeight() - 2;
	}

	public void drawGraph()
	{
		this.repaint();
	}

}
