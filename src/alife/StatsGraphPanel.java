package alife;

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

	public void updateGraph(float plantMax,float preyMax, float predMax,boolean tiePredPreyMax)
	{
		this.plantMax = plantMax;
		
		this.preyMax = preyMax;
		
		this.predMax = predMax;
		
		if(tiePredPreyMax)
		{
			tiePredPreyMax();
		}
		
		this.repaint();
	}
	
	public void tiePredPreyMax()
	{		
		if(preyMax > predMax)
		{
			predMax = preyMax;
		}
		else
		{
			predMax = preyMax;
		}
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
		float scaleWidthInterval = graphWidth/ sampleNum;
		float scaleHeightInterval = graphHeight / (predMax+1);
		
		int sampleXVal = 0;
		int sampleYVal = 0;
		
		g2.setColor(Color.red);
		
		for(int i =0;i<sampleNum;i++)
		{			
			sampleXVal = (int) (i*scaleWidthInterval);
			
			sampleYVal = graphHeight - ((int)(predSamples[i]*scaleHeightInterval));			
			
			g2.drawLine( sampleXVal ,sampleYVal, sampleXVal, sampleYVal);
		}
	}	
	
	public void drawPrey(Graphics2D g2)
	{				
		float scaleWidthInterval = graphWidth/ sampleNum;
		float scaleHeightInterval = graphHeight / (preyMax+1);
		
		int sampleXVal = 0;
		int sampleYVal = 0;
		
		g2.setColor(Color.blue);
		
		for(int i =0;i<sampleNum;i++)
		{
			
			sampleXVal = (int) (i*scaleWidthInterval);
			
			sampleYVal = graphHeight - ((int)(preySamples[i]*scaleHeightInterval));			
			
			g2.drawLine( sampleXVal ,sampleYVal, sampleXVal, sampleYVal);
		}
	}	
	
	public void drawPlants(Graphics2D g2)
	{
				
		float scaleWidthInterval = graphWidth/ sampleNum;
		float scaleHeightInterval = graphHeight / (plantMax+1);
		
		int sampleXVal = 0;
		int sampleYVal = 0;

		
		g2.setColor(Color.green);
		
		for(int i =0;i<sampleNum;i++)
		{
			
			sampleXVal = (int) (i*scaleWidthInterval);
			
			sampleYVal = graphHeight - ((int)(plantsSamples[i]*scaleHeightInterval));			
			
			g2.drawLine( sampleXVal ,sampleYVal, sampleXVal, sampleYVal);
			
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
