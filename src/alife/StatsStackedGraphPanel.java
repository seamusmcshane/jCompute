package alife;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

public class StatsStackedGraphPanel extends JPanel
{
	private static final long serialVersionUID = -8039820081105789703L;

	/** The references to the sample arrays */
	private int plantsSamples[];
	private int preySamples[];
	private int predSamples[];

	private int graphX;
	private int graphY;
	private int graphWidth;
	private int graphHeight;

	private float sampleNum;

	private float graphSamples = 1;

	private float plantMax = 0;
	private float preyMax = 0;
	private float predMax = 0;

	/**
	 * A new Graph Panel
	 */
	public StatsStackedGraphPanel()
	{

	}

	public void setSampleArrays(int plantsSamples[], int preySamples[], int predSamples[], int sampleNum)
	{
		this.plantsSamples = plantsSamples;
		this.preySamples = preySamples;
		this.predSamples = predSamples;

		graphSamples = 1;

		this.sampleNum = sampleNum;
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

		if (currSampleNum < sampleNum)
		{
			graphSamples = currSampleNum;
		}
		else
		{
			graphSamples = sampleNum;
		}

	}

	/** 
	 * Does the plants or agents have the greater point
	 * Max is tied to the greater */
	public void tieAllMax()
	{
		float agent_max = tiePredPreyMax();

		if (plantMax > agent_max)
		{
			predMax = plantMax;
			preyMax = plantMax;
		}
		else
		{
			plantMax = agent_max;
		}
	}

	/**
	 * Which of the agents has the greater max point in the graph 
	 * max is tied to the greater, also returns max for further evaluation */
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

	/** Draws the graph */
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D) g;

		// Too Slow for this graph
		// g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);		    

		calculateGraphSize();

		drawSamples(g2);
	}

	/** Draws the graph lines */
	public void drawSamples(Graphics2D g2)
	{
		if (plantsSamples == null || preySamples == null || predSamples == null)
		{
			return;
		}

		float scaleWidthInterval = graphWidth / sampleNum;

		float total_height_scale = 0;

		float total_val = 0;

		int predSampleXVal = 0;
		int predSampleYVal = 0;

		int plantsSampleXVal = 0;
		int plantsSampleYVal = 0;

		int preySampleXVal = 0;
		int preySampleYVal = 0;

		/* Loops through all three sample arrays */
		for (int i = 0; i < sampleNum; i++)
		{

			total_val = plantsSamples[i] + preySamples[i] + predSamples[i];

			total_height_scale = graphHeight / (total_val + 1);

			/* Plants */
			plantsSampleXVal = (int) (i * scaleWidthInterval);
			plantsSampleYVal = ((int) (plantsSamples[i] * total_height_scale));

			/* Prey */
			preySampleXVal = (int) (i * scaleWidthInterval);
			preySampleYVal = ((int) (preySamples[i] * total_height_scale));

			/* Predator */
			predSampleXVal = (int) (i * scaleWidthInterval);
			predSampleYVal = ((int) (predSamples[i] * total_height_scale));

			if (preySamples[i] > 0)
			{
				g2.setColor(Color.GREEN);
				g2.drawLine(plantsSampleXVal, graphHeight, plantsSampleXVal, graphHeight - plantsSampleYVal);
			}

			if (preySamples[i] > 0)
			{
				g2.setColor(Color.BLUE);
				g2.drawLine(preySampleXVal, graphHeight - plantsSampleYVal, preySampleXVal, predSampleYVal);
			}

			if (predSamples[i] > 0)
			{
				g2.setColor(Color.RED);
				g2.drawLine(predSampleXVal, 0, predSampleXVal, predSampleYVal);
			}
		}
	}

	public void drawGraph()
	{
		this.repaint();
	}

	/** Gets the widths and height of this panel */
	private void calculateGraphSize()
	{
		graphX = this.getX();
		graphY = this.getY();
		graphWidth = this.getWidth() - 1;
		graphHeight = this.getHeight() - 2;
	}

}
