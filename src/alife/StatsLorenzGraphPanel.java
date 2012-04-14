package alife;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

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
	boolean mode_dynamic_based=false;
	float mode_scale=0.15f;
	
	float lmod = 0;
	float lmod_max=0;	
	
	int plantsMax=0;
	int preyMax=0;
	int predMax=0;
	
	/*
	 * Large numbers of Predators rarely appear except in an Ecosystem collapse event.
	 * Thus red is rarely on the graph, for better visuals the color value for predator numbers are boosted by this factor.
	 */
	float predator_graph_boost = 1f; 
	
	/* As above but plants always are in large numbers and get a negative boost */ 
	float plant_graph_negative_factor = 1f;
		
	float zoom = 0; 	// Graph Scale
	float xScale=1;
	float yScale=1;
	
	float max_graph_line_size=1f;
		
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
		
		completeResetGraph();		
		setZoom(100);		
	}

	/**
	 * Updates the graph and draws it on an interval based on stepNo.
	 * @param stepNo
	 */
	public void updateGraph(int plantsMax,int preyMax, int predMax, int stepNo)
	{
		this.plantsMax = plantsMax;
		this.preyMax=preyMax;
		this.predMax=predMax;
		
		if(stepNo<sampleNum)
		{
			graphSamples=stepNo;
		}
		else
		{
			graphSamples=sampleNum;
		}
						
	}
				
	/** Draws the graph */
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
				
		Graphics2D g2 = (Graphics2D) g;	
		
	    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);		    
		
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
		
		if(mode_dynamic_based)
		{
			mode_scale=0.25f;
			
			predator_graph_boost=(float)((float)preyMax/(float)predMax);
			
			// Limit the ratios
			if(predator_graph_boost>4)
			{
				plant_graph_negative_factor=4;
			}					
			
			plant_graph_negative_factor= 1f-((float)plantsMax/(float)(preyMax+predMax+plantsMax));
			
			// Limit the ratios
			if(plant_graph_negative_factor<=0.65f)
			{
				plant_graph_negative_factor=0.65f;
			}			
		
		}
		else // Dynamic mode drawing off
		{
			mode_scale=0.15f;
			plant_graph_negative_factor=0.05f;
			predator_graph_boost = 4; 
			plant_graph_negative_factor = 1;				
		}

		// Find the max
		lmod_max=Math.max(1,plantsMax*plant_graph_negative_factor);					
		lmod_max=Math.max(lmod_max, preyMax);
		lmod_max=Math.max(lmod_max, predMax*predator_graph_boost);
		
		// half the max so lmod will cause the curve to phaze invert on a negative (ie x=50, max = 200, lmod = 100, drawing x = -50 etc)
		lmod=lmod_max/2;	
		
		/* Loops through all three sample arrays */
		for(int i =0;i<graphSamples;i++)
		{			
			// The values we feed to the Lorenz equations.
		    double x=(plantsSamples[i]*plant_graph_negative_factor)-lmod; 	    		
		    double y=(predSamples[i]*predator_graph_boost)-lmod;
		    double z=(preySamples[i])-lmod;
		    /* Lmod is half of the max value of all samples, meaning this can cause X,Y and Z to go negative.
		     * By going negative the other half of the Lorenz system is drawn.	
		     */
		    		    
		    // Modified Lorenz Equations
		    int gx=(int) ( ((x+( (-10*x)    +(10*y))   ) )*(mode_scale));
		    int gy=(int) (((y+( (28*x)     +(y-(x*z))) ) )*(mode_scale));
		    int gz=(int) ((z+( (-8*(z/3)) +(x*y)   ) )*(mode_scale));
		    
		    // Scale the point position by the graph scale.
		    //gx=(int)(gx*scale);
		    gy=(int)(gy*zoom*xScale);
		    gz=(int)(gz*zoom*yScale);
		    
		    if(i==0)
		    {
			    nx=gz;
			    ny=gy;	
		    }
		    
		    // The current point
		    cx=gz;
		    cy=gy;
		     		    
		    // Line Width
		    g2.setStroke(new BasicStroke( (i*(max_graph_line_size/graphSamples))));
		    
		    // R G B A
		    //System.out.println("" +  (int)(predSamples[i]*(255/lmod_max))%255 + "" +  (int)(plantsSamples[i]*(255/lmod_max))%255 + "" +  (int)(preySamples[i]*(255/lmod_max))%255 + "" + (int) (i*(255/graphSamples))%255);
		    g2.setColor(new Color( (int)((predSamples[i]*predator_graph_boost)*(255/lmod_max))%255, (int)((plantsSamples[i]*plant_graph_negative_factor)*(255/lmod_max))%255 , (int)(preySamples[i]*(255/lmod_max))%255 ,(int) (i*(255/graphSamples))%255));		    	


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
	
	public void setXScale(float xScale)
	{
		this.xScale = (xScale+1)/10f;
        repaint();
	}
	
	public void setYScale(float yScale)
	{
		this.yScale = (yScale+1)/10f;
        repaint();
	}	
	
    public void setZoom(float inZoom)
    {
    	inZoom = (inZoom/1000000f);
    	
        if(inZoom>0)
        {
        	zoom=inZoom;
        }
        else
        {
        	zoom = 1/1000000f;
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
	
    public void completeResetGraph()
    {
    	calculateGraphSize();
	    originX=-(cx)+graphWidth/2;
	    originY=-(cy)+graphHeight/2;    	    	
    	lmod=0;
    	lmod_max=0;
    	repaint();
    }
    
    public void resetGraph(int ignored)
    {
    	calculateGraphSize();
	    originX=graphWidth/2;
	    originY=graphHeight/2;    	    	
    	//lmod=0;
    	repaint();    	    	
    }    
    
	/** Gets the widths and height of this panel **/
	private void calculateGraphSize()
	{
		graphX = this.getX();
		graphY = this.getY();
		graphWidth = this.getWidth()-1;
		graphHeight = this.getHeight()-2;
		
	}
	
	public boolean getDynamicMode()
	{
		return mode_dynamic_based;
	}
	
	public void setDynamicMode(boolean mode)
	{
		mode_dynamic_based=mode;
	}	

	public void drawGraph()
	{
		this.repaint();
	}
}
