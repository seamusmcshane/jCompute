package alife;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Line;

/**
 * This class contains the methods needed to create 
 * and draw the background grid for the world.
 * - No external interactions other than draw which draws an image of the grid.
 */ 
public class WorldGrid
{
				
	/** The Horizontal Lines. */
	private Line[] hlines;
	
	/** The Vertical Lines. */
	private Line[] vlines;
	
	/** Number of lines (generated) */
	private int num;
	
	
	/** The size of the grid. */
	private int size;
	
	/** The step at which to draw each line. */
	private int step;
	
	/** The division at which to draw major lines. */
	private int major_div; /* TODO make world grid Adjustable */
	
	/**
	 * Instantiates a new world grid.
	 *
	 * @param size the size
	 * @param step the step
	 */
	public WorldGrid(int size,int step)
	{
		this.size=size;
		
		this.step = step;
		
		generateGrid();	
		
	}
	
	/** 
	 * Generation Method 
	 * Calculates the positions of all the lines.
	 */
	private void generateGrid()
	{
		num=1+(size/step); /* ie if size = 1024 then lines every 8 */
		
		major_div = step*8; /* If step = 8 then this will be every 64 lines */
		
		hlines = new Line[num];
		vlines = new Line[num];
		
		int x=0;		
		for(int i=0;i<num;i++)
		{			
			hlines[i] = new Line(x,0,x, size);			
			vlines[i] = new Line(0,x,size,x);
			x=x+step;
		}
				
	}
	
	/** 
	 * Draws the grid method on the image object 
	 */
	public void drawGrid(Graphics g)
	{					
			g.setBackground(Color.black);
		
			g.setColor(new Color(25,25,25));
			
			g.setLineWidth(2f);
			
			g.setAntiAlias(true);
			
			for(int i=0;i<num;i++)
			{					
				if(i%major_div == 0)
				{
					g.setColor(new Color(50,50,50));	/* Major Div */
				}
				else
				{
					g.setColor(new Color(25,25,25));	/* Normal Line */
				}
				g.draw(hlines[i]);
				g.draw(vlines[i]);
			}		
	}
	
}
	

