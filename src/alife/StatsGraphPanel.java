package alife;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JPanel;

public class StatsGraphPanel extends JPanel
{
	
	private static int plantsSamples[];
	private static int preySamples[];
	private static int predSamples[];
	
	private static int graphX;
	private static int graphY;
	private static int graphWidth;
	private static int graphHeight;
	
	private static float sampleNum;
	private static float samplePeriod;
	
	private static float graphSamples=1;
	
	private static float plantMax = 0;
	private static float preyMax = 0;
	private static float predMax = 0;
	
	public StatsGraphPanel(int plantsSamples[],	int preySamples[], int predSamples[], int sampleNum, int samplePeriod)
	{

		this.plantsSamples = plantsSamples;
		this.preySamples = preySamples;
		this.predSamples = predSamples;
		
		this.sampleNum = sampleNum;
		this.samplePeriod = samplePeriod;
		
	}

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
		
	/* Does the plants or agents have the greater point
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
	
	/* Which of the agents has the greater max point in the graph 
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
	
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
				
		Graphics2D g2 = (Graphics2D) g;	
		
				
		calculateGraphSize();
		
		drawPlants(g2);
		
		drawPrey(g2);
		
		drawPred(g2);
		
	}

	public void drawPred(Graphics2D g2)
	{				
		float scaleWidthInterval = graphWidth/ graphSamples;
		float scaleHeightInterval = graphHeight / (predMax+1);
		
		int sampleXVal = 0;
		int sampleYVal = 0;	
		int samplePXVal = 0;
		int samplePYVal = 0; 
		
		g2.setColor(Color.red);
		
		for(int i =0;i<graphSamples;i++)
		{			
			sampleXVal = (int) (i*scaleWidthInterval);
			
			sampleYVal = graphHeight - ((int)(predSamples[i]*scaleHeightInterval));			
			
			g2.drawLine( sampleXVal ,sampleYVal, sampleXVal, sampleYVal);
			
			g2.drawLine(samplePXVal ,samplePYVal, sampleXVal, sampleYVal);
			
			samplePXVal = sampleXVal;
			samplePYVal = sampleYVal;			
		}
	}	
	
	public void drawPrey(Graphics2D g2)
	{				
		float scaleWidthInterval = graphWidth/ graphSamples;
		float scaleHeightInterval = graphHeight / (preyMax+1);
		
		int sampleXVal = 0;
		int sampleYVal = 0;
		int samplePXVal = 0;
		int samplePYVal = 0;   		
		
		g2.setColor(Color.blue);
		
		for(int i =0;i<graphSamples;i++)
		{
			
			sampleXVal = (int) (i*scaleWidthInterval);
			
			sampleYVal = graphHeight - ((int)(preySamples[i]*scaleHeightInterval));			
						
			g2.drawLine(samplePXVal ,samplePYVal, sampleXVal, sampleYVal);
			
			samplePXVal = sampleXVal;
			samplePYVal = sampleYVal;
		}
	}	
	
	public void drawPlants(Graphics2D g2)
	{
				
		float scaleWidthInterval = graphWidth/ graphSamples;
		float scaleHeightInterval = graphHeight / (plantMax+1);
		
		int sampleXVal = 0;
		int sampleYVal = 0;
		int samplePXVal = 0;
		int samplePYVal = 0;    
		
		g2.setColor(Color.green);
		
		for(int i =0;i<graphSamples;i++)
		{			
			sampleXVal = (int) (i*scaleWidthInterval);
			
			sampleYVal = graphHeight - ((int)(plantsSamples[i]*scaleHeightInterval));			
			
			g2.drawLine(samplePXVal ,samplePYVal, sampleXVal, sampleYVal);
			
			samplePXVal = sampleXVal;
			samplePYVal = sampleYVal;
			
			/*System.out.println("graphHeight            :" + graphHeight);
			System.out.println("graphWidth             :" + graphWidth);
			
			System.out.println("graphWidth / sampleNum :" + graphWidth / sampleNum);
			System.out.println(" scaleWidthInterval   : " + scaleWidthInterval);
			System.out.println(" i*scaleWidthInterval : " + i*scaleWidthInterval);
			System.out.println(" i*scaleWidthInterval : " + (int)(i*scaleWidthInterval));
			System.out.println(" i                    : " + i);*/

		}
	}	
	
	private void calculateGraphSize()
	{
		graphX = this.getX();
		graphY = this.getY();
		graphWidth = this.getWidth();
		graphHeight = this.getHeight();
	}

}
