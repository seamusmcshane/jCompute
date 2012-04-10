package alife;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;
/**
 * A Custom Panel used for drawing a graph.
 */
public class StatsLorenzGraphPanel extends JPanel
{
	/** The references to the sample arrays */
	private int plantsSamples[];
	private int preySamples[];
	private int predSamples[];
	
	private int graphX;
	private int graphY;
	private int graphWidth;
	private int graphHeight;
	
	boolean drawing = false;
	float lmod = 0;
		
	float scale = 0; 	// Graph Scale
	
	private float sampleNum;
	
	private float graphSamples=1;

	/** Graph Origin */
	float originX=0;
	float originY=0;

	/** Line Vectors */
	float cx=originX;
	float cy=originY;
	float nx=originX;
	float ny=originY;
	
	/** Mouse Coordinates */
    private int cmX=0;
    private int cmY=0;
	
	/**
	 * A new Graph Panel
	 * @param plantsSamples
	 * @param preySamples
	 * @param predSamples
	 * @param sampleNum
	 **/
	public StatsLorenzGraphPanel(int plantsSamples[],	int preySamples[], int predSamples[], int sampleNum)
	{

		this.plantsSamples = plantsSamples;
		this.preySamples = preySamples;
		this.predSamples = predSamples;
		
		this.sampleNum = sampleNum;
		
		resetGraph();		
		setZoom(100);
	}

	/**
	 * Updates the graph and draws it on an interval based on stepNo.
	 * @param stepNo
	 */
	public void updateGraph(int plantsMax,int preyMax, int predMax, int stepNo)
	{
		// No need to ensure this updates, just that it gets updated eventually.
		if(!drawing)
		{
			// Find the max (keeps largest forever)
			lmod=Math.max(lmod,plantsMax);					
			lmod=Math.max(lmod, preyMax);
			lmod=Math.max(lmod, preyMax);
			
			// half the max so lmod will cause the curve to phaze invert on a negative (ie x=50, max = 200, lmod = 100, drawing x = -50 etc)
			lmod=lmod/2;			
		}
		
		if(stepNo<sampleNum)
		{
			graphSamples=stepNo;
		}
		else
		{
			graphSamples=sampleNum;
		}
				
		/* No need to Draw the graph every step */
		if(stepNo%5 == 0)
		{
			this.repaint();
		}
		
	}
				
	/** Draws the graph */
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
				
		Graphics2D g2 = (Graphics2D) g;	
		
		calculateGraphSize();
		
		drawSamples(g2);

	}

	/** 
	 * Draws the graph line.
	 * Based on http://www.sat.t.u-tokyo.ac.jp/~hideyuki/java/Attract.java
	 * */
	public void drawSamples(Graphics2D g2)
	{				
		
		drawing = true;
		
		/* Loops through all three sample arrays */
		for(int i =0;i<graphSamples;i++)
		{			
			// The values we feed to the Lorenz equations.
		    double x=(plantsSamples[i])-lmod; 	    		
		    double y=(predSamples[i])-lmod;
		    double z=(preySamples[i])-lmod;
		    /* Lmod is half of the max value of all samples, meaning this can cause X,Y and Z to go negative.
		     * By going negative the other half of the Lorenz system is drawn.	
		     */
		    
		    
		    // Modified Lorenz Equations
		    int gx=((int)(x+0.1*(-10*x+10*y)));
		    int gy=((int)(y+0.1*(28*x-y-x*z)) );
		    int gz=(int)(z+0.1*(-8*z/3+x*y));
		    
		    // Scale the point position by the graph scale.
		    //gx=(int)(gx*scale);
		    gy=(int)(gy*scale);
		    gz=(int)(gz*scale);
		    
		    if(i==0)
		    {
			    nx=gy;
			    ny=gz;	
		    }
		    
		    // The current point
		    cx=gy;
		    cy=gz;
		     		    
		    // Line Width
		    g2.setStroke(new BasicStroke(1));
		    
		    // R G B
		    g2.setColor(new Color( (int)(predSamples[i]*0.391)%255, (int)(plantsSamples[i]*0.391)%255 , (int)(preySamples[i]*0.391)%255 ));
		    
		    // Draws line based on where the origin is.
		    g2.drawLine((int)(cx+(originX)),(int)(-cy+(originY)),(int)(nx+(originX)),(int)(-ny+(originY)));
		    
		    // The next lines - next point		    
		    nx=cx;
		    ny=cy;

		  /*  System.out.println("originX" + originX);
		    System.out.println("originY "+ originY);
		    System.out.println("cx" + cx);
		    System.out.println("cy" + cy);*/
		}

		drawing = false;
		
		// Track the last Sample (follows it on the graph)
	    originX=-(cx)+graphWidth/2;
	    originY=-(-cy)+graphHeight/2;			
		
	}
	
    public void setZoom(float inZoom)
    {
    	float zoom = (inZoom/1000000f);
    	
        if(zoom>0)
        {
            scale=zoom;
        }
        else
        {
        	scale = 1/1000000f;
        }

        repaint();
    }  
	
    // Not used anymore
    public void setMpos(int x,int y)
    {
        cmX=x;
        cmY=y;
    }
    
    // Not used anymore
    public void moveGraph(int mX,int mY)
    {
        originX=originX+(mX-cmX);
        originY=originY+(mY-cmY);

        cmX=mX;
        cmY=mY;
        repaint();
    }
	
    public void resetGraph()
    {
    	calculateGraphSize();
	    originX=-(cx)+graphWidth/2;
	    originY=-(cy)+graphHeight/2;    	    	
    	repaint();
    	lmod=0;
    }
    
	/** Gets the widths and height of this panel **/
	private void calculateGraphSize()
	{
		graphX = this.getX();
		graphY = this.getY();
		graphWidth = this.getWidth()-1;
		graphHeight = this.getHeight()-2;
		
	}

}
