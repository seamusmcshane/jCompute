package alife;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;
/**
 * A Custom Panel used for drawing a graph.
 */
public class StatsGraphPanel extends JPanel
{
	/** The references to the sample arrays */
	private int plantsSamples[];
	private int preySamples[];
	private int predSamples[];
	
	private int graphX;
	private int graphY;
	private int graphWidth;
	private int graphHeight;
	
	private float sampleNum;
	private float samplePeriod;
	
	private float graphSamples=1;
	
	private float plantMax = 0;
	private float preyMax = 0;
	private float predMax = 0;
	
	/**
	 * A new Graph Panel
	 * @param plantsSamples
	 * @param preySamples
	 * @param predSamples
	 * @param sampleNum
	 * @param samplePeriod
	 */
	public StatsGraphPanel(int plantsSamples[],	int preySamples[], int predSamples[], int sampleNum, int samplePeriod)
	{

		this.plantsSamples = plantsSamples;
		this.preySamples = preySamples;
		this.predSamples = predSamples;
		
		this.sampleNum = sampleNum;
		this.samplePeriod = samplePeriod;
		
	}

	/**
	 * Updates the graph and draws it on an interval based on stepNo.
	 * @param plantMax
	 * @param preyMax
	 * @param predMax
	 * @param scale_mode
	 * @param stepNo
	 */
	public void updateGraph(float plantMax,float preyMax, float predMax,int scale_mode,int stepNo)
	{
		this.plantMax = plantMax;
		
		this.preyMax = preyMax;
		
		this.predMax = predMax;
		
		if(stepNo<sampleNum)
		{
			graphSamples=stepNo;
		}
		else
		{
			graphSamples=sampleNum;
		}
		
		/* 0 = all on own scale, 1 - plants on own scale, prey+pred tied, 2 - all tied */
		switch(scale_mode)
		{
			case 0:
				// Do nothing.
				break;
			case 1:
				tiePredPreyMax();
				break;
			case 2:
				tieAllMax();
				break;
		}		
		
		/* No need to Draw the graph every step */
		if(stepNo%5 == 0)
		{
			this.repaint();
		}
		
	}
		
	/** 
	 * Does the plants or agents have the greater point
	 * Max is tied to the greater */
	public void tieAllMax()
	{
		float agent_max = tiePredPreyMax();
		
		if(plantMax > agent_max)
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
		if(preyMax > predMax)
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
		
		calculateGraphSize();
		
		drawSamples(g2);

	}

	/** Draws the graph lines */
	public void drawSamples(Graphics2D g2)
	{				
		float scaleWidthInterval = graphWidth/ graphSamples;
		
		float predScaleHeightInterval = graphHeight / (predMax+1);
		float preyScaleHeightInterval = graphHeight / (preyMax+1);
		float plantsScaleHeightInterval = graphHeight / (plantMax+1);

		
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
		for(int i =0;i<graphSamples;i++)
		{			
			/* Plants */
			g2.setColor(Color.GREEN);
			plantsSampleXVal = (int) (i*scaleWidthInterval);
			
			plantsSampleYVal = graphHeight - ((int)(plantsSamples[i]*plantsScaleHeightInterval));			
						
			g2.drawLine(plantsSamplePXVal ,plantsSamplePYVal, plantsSampleXVal, plantsSampleYVal);
			
			plantsSamplePXVal = plantsSampleXVal;
			plantsSamplePYVal = plantsSampleYVal;				
			
			/* Prey */
			g2.setColor(Color.BLUE);
			preySampleXVal = (int) (i*scaleWidthInterval);
			
			preySampleYVal = graphHeight - ((int)(preySamples[i]*preyScaleHeightInterval));			
						
			g2.drawLine(preySamplePXVal ,preySamplePYVal, preySampleXVal, preySampleYVal);
			
			preySamplePXVal = preySampleXVal;
			preySamplePYVal = preySampleYVal;				
		
			/* Predators */
			g2.setColor(Color.red);
			predSampleXVal = (int) (i*scaleWidthInterval);
			
			predSampleYVal = graphHeight - ((int)(predSamples[i]*predScaleHeightInterval));			
						
			g2.drawLine(predSamplePXVal ,predSamplePYVal, predSampleXVal, predSampleYVal);
			
			predSamplePXVal = predSampleXVal;
			predSamplePYVal = predSampleYVal;			
		}
	}	
		
	/** Gets the widths and height of this panel */
	private void calculateGraphSize()
	{
		graphX = this.getX();
		graphY = this.getY();
		graphWidth = this.getWidth()-1;
		graphHeight = this.getHeight()-2;
	}

}
