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
	
	float scale = 0; 	// Graph Scale
	
	private float sampleNum;
	
	private float graphSamples=1;

	/** Graph Origin */
	float originX=0;
	float originY=0;
	
	float tx=0;
	float ty=0;
	
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
	public void updateGraph(int stepNo)
	{
		
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

	/** Draws the graph line.
	 * Based on http://www.sat.t.u-tokyo.ac.jp/~hideyuki/java/Attract.java
	 * */
	public void drawSamples(Graphics2D g2)
	{				
		
		/* Loops through all three sample arrays */
		for(int i =0;i<graphSamples;i++)
		{			
		    double x=predSamples[i];
		    double y=preySamples[i];
		    double z=plantsSamples[i];		    
		    
		    int gx= ( (int) (x+0.1*(-10*x+10*y)));
		    int gy=((int) (y+0.1*(28*x-y-x*z)) );
		    int gz=(int) (z+0.1*(-8*z/3+x*y));
		    
		    gy=(int)(gy*scale);
		    gz=(int)(gz*scale);
		    
		    if(i==0)
		    {
			    nx=gy;
			    ny=gz;	
		    }
		    cx=gy;
		    cy=gz;
		    // R G B
		    
		    g2.setStroke(new BasicStroke(2));
		    g2.setColor(new Color( (int)(predSamples[i]*0.391)%255, (int)(plantsSamples[i]*0.391)%255 , (int)(preySamples[i]*0.391)%255 ));
		    g2.drawLine((int)(cx+(originX)),(int)(cy+(originY)),(int)(nx+(originX)),(int)(ny+(originY)));

		    nx=cx;
		    ny=cy;
		    
		    /*System.out.println("scaleMax "+scaleMax);
		    System.out.println("gy" + gy);
		    System.out.println("gz "+ gz);*/
		    //System.out.println("gnx" + gnx);
		    //System.out.println("gny" + gny);
		}

	}
	
    public void setZoom(float inZoom)
    {
    	float zoom = (inZoom/100000f);
    	
        if(zoom>0)
        {
            scale=zoom;
        }
        else
        {
        	scale = 1/100000f;
        }

        /* Account for the zoom translate for the origin */
        originX += (tx*scale);
        originY += ty*scale;
        repaint();
    }  
	
    public void setMpos(int x,int y)
    {
        cmX=x;
        cmY=y;
    }
    
    public void moveGraph(int mX,int mY)
    {
        originX=originX+(mX-cmX);
        originY=originY+(mY-cmY);

        tx=(mX-cmX);
        ty=(mX-cmX);        		        	
        
        cmX=mX;
        cmY=mY;
        repaint();
    }
	
    public void resetGraph()
    {
    	calculateGraphSize();
    	originX=(this.getWidth()/4)*3;
    	originY=(this.getHeight()/4);
    	
    	tx=0;
    	ty=0;
    	
    	repaint();
    }
    
	/** Gets the widths and height of this panel - Not used TODO - autoscaling */
	private void calculateGraphSize()
	{
		graphX = this.getX();
		graphY = this.getY();
		graphWidth = this.getWidth()-1;
		graphHeight = this.getHeight()-2;
		
	}

}
